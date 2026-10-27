package com.kmab.gulp.core.data

import android.content.Context
import androidx.room.Room
import com.kmab.gulp.core.domain.DrinkKind
import com.kmab.gulp.core.domain.DrinkRecord
import com.kmab.gulp.core.domain.DrinkRules
import com.kmab.gulp.core.domain.DrinkCsv
import com.kmab.gulp.core.domain.DrinkType
import com.kmab.gulp.core.domain.DrinkTypeCatalog
import kotlinx.coroutines.flow.combine
import java.util.UUID
import com.kmab.gulp.widget.GulpWidgetRefresh
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Undo tokens are single-use. Concurrent edits are serialized to avoid lost writes. */
sealed interface UndoAction {
    data class Remove(val id: Long) : UndoAction
    data class Restore(val record: DrinkRecord) : UndoAction
}

data class UndoTicket(val sequence: Long, val action: UndoAction, val message: String)

class GulpRepository(private val dao: DrinkDao, private val afterWrite: suspend () -> Unit = {},
                     private val typesDao: DrinkTypeDao? = null) {
    private val writeMutex = Mutex()
    private var version = 0L
    private var outstanding: UndoTicket? = null

    val records: Flow<List<DrinkRecord>> = dao.observeAll().map { list -> list.map { it.asRecord() } }

    val drinkTypes: Flow<List<DrinkType>> = typesDao?.observeAll()?.map { rows ->
        // Persisted overrides take precedence, including edits to the four starter drinks.
        // Keep the starter ordering stable so existing shortcuts remain easy to find.
        val saved = rows.map { it.asType() }
        val byId = saved.associateBy { it.id }
        DrinkTypeCatalog.builtins.map { builtin -> byId[builtin.id] ?: builtin } +
            saved.filter { type -> type.id !in DrinkTypeCatalog.builtins.map { it.id } }
    } ?: kotlinx.coroutines.flow.flowOf(DrinkTypeCatalog.builtins)

    suspend fun saveDrinkType(type: DrinkType): DrinkType {
        DrinkTypeCatalog.checked(type.name, type.iconId, type.defaultAmountMl)
        require(DrinkTypeCatalog.validId(type.id)) { "Invalid drink ID" }
        // The starter types can be personalized. Stable IDs and historical record snapshots
        // preserve the meaning of previously logged drinks.
        val clean = type.copy(name = DrinkTypeCatalog.sanitizeName(type.name))
        requireNotNull(typesDao) { "Drink catalog unavailable" }.upsert(DrinkTypeEntity.from(clean))
        notifyWidgets()
        return clean
    }

    suspend fun newDrinkType(name: String, iconId: String, defaultMl: Int): DrinkType {
        DrinkTypeCatalog.checked(name, iconId, defaultMl)
        return saveDrinkType(DrinkType("custom_${UUID.randomUUID().toString().replace("-", "")}",
            DrinkTypeCatalog.sanitizeName(name), iconId, defaultMl))
    }

    suspend fun archiveDrinkType(id: String) {
        require(id !in DrinkTypeCatalog.builtins.map { it.id }) { "Built-in types cannot be archived" }
        val catalog = requireNotNull(typesDao)
        val saved = catalog.find(id) ?: return
        catalog.upsert(saved.copy(archived = true))
        notifyWidgets()
    }

    /** Latest recorded entry, used only to suppress a redundant notification after a drink. */
    suspend fun latestTimeMillis(): Long? = records.first().asSequence().filter { it.timeMillis <= System.currentTimeMillis() }.maxOfOrNull { it.timeMillis }

    suspend fun add(kind: DrinkKind, ml: Int, atMillis: Long = System.currentTimeMillis()): UndoTicket = writeMutex.withLock {
        DrinkRules.checkAmount(ml)
        val id = dao.insert(DrinkEntity(amountMl = ml, drinkType = kind.name, timestampMillis = atMillis,
            drinkTypeId = kind.name.lowercase(), drinkName = kind.title, drinkIcon = DrinkTypeCatalog.defaultIcon(kind)))
        notifyWidgets()
        issue(UndoAction.Remove(id), "Drink added")
    }

    suspend fun add(type: DrinkType, ml: Int = type.defaultAmountMl,
                    atMillis: Long = System.currentTimeMillis()): UndoTicket = writeMutex.withLock {
        DrinkRules.checkAmount(ml)
        val id = dao.insert(DrinkEntity(amountMl = ml, drinkType = type.legacyKind.name,
            timestampMillis = atMillis, drinkTypeId = type.id, drinkName = type.name, drinkIcon = type.iconId))
        notifyWidgets()
        issue(UndoAction.Remove(id), "Drink added")
    }

    suspend fun edit(id: Long, kind: DrinkKind, ml: Int, atMillis: Long): UndoTicket? = writeMutex.withLock {
        DrinkRules.checkAmount(ml)
        val before = dao.findById(id) ?: return@withLock null
        val edited = before.copy(amountMl = ml, drinkType = kind.name, timestampMillis = atMillis,
            drinkTypeId = kind.name.lowercase(), drinkName = kind.title,
            drinkIcon = DrinkTypeCatalog.defaultIcon(kind))
        if (edited == before) return@withLock null
        if (dao.update(edited) == 0) return@withLock null
        notifyWidgets()
        issue(UndoAction.Restore(before.asRecord()), "Drink updated")
    }

    suspend fun edit(id: Long, type: DrinkType, ml: Int, atMillis: Long): UndoTicket? = writeMutex.withLock {
        DrinkRules.checkAmount(ml)
        val before = dao.findById(id) ?: return@withLock null
        val updated = before.copy(amountMl = ml, drinkType = type.legacyKind.name,
            timestampMillis = atMillis, drinkTypeId = type.id, drinkName = type.name,
            drinkIcon = type.iconId)
        if (updated == before || dao.update(updated) == 0) return@withLock null
        notifyWidgets()
        issue(UndoAction.Restore(before.asRecord()), "Drink updated")
    }

    suspend fun delete(id: Long): UndoTicket? = writeMutex.withLock {
        val old = dao.findById(id) ?: return@withLock null
        if (dao.delete(old) == 0) return@withLock null
        notifyWidgets()
        issue(UndoAction.Restore(old.asRecord()), "Drink deleted")
    }

    suspend fun undo(ticket: UndoTicket): Boolean = writeMutex.withLock {
        if (ticket != outstanding) return@withLock false
        when (val action = ticket.action) {
            is UndoAction.Remove -> dao.findById(action.id)?.let { dao.delete(it) }
            is UndoAction.Restore -> dao.insert(DrinkEntity.from(action.record))
        }
        outstanding = null
        notifyWidgets()
        true
    }

    /** Remove records irreversibly only after explicit UI confirmation. */
    suspend fun deleteAll(): Int = writeMutex.withLock {
        val deleted = dao.deleteAll()
        outstanding = null
        if (deleted > 0) notifyWidgets()
        deleted
    }

    suspend fun exportCsv(): String = DrinkCsv.encode(records.first())

    private suspend fun notifyWidgets() {
        // A launcher or missing widget must never prevent saving a drink.
        runCatching { afterWrite() }.onFailure { Log.w("gulp-widgets", "Widget refresh failed", it) }
    }

    private fun issue(action: UndoAction, message: String): UndoTicket =
        UndoTicket(++version, action, message).also { outstanding = it }

    companion object {
        @Volatile private var db: GulpDatabase? = null
        fun create(context: Context): GulpRepository {
            val instance = db ?: synchronized(this) {
                db ?: Room.databaseBuilder(context.applicationContext, GulpDatabase::class.java, "gulp_records.db")
                    .addMigrations(MIGRATION_1_2)
                    .build().also { db = it }
            }
            return GulpRepository(instance.drinks(), afterWrite = { GulpWidgetRefresh.refresh(context.applicationContext) }, typesDao = instance.types())
        }
    }
}
