package com.kmab.gulp.core.data

import android.content.Context
import androidx.room.Room
import com.kmab.gulp.core.domain.DrinkKind
import com.kmab.gulp.core.domain.DrinkRecord
import com.kmab.gulp.core.domain.DrinkRules
import com.kmab.gulp.core.domain.DrinkCsv
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

class GulpRepository(private val dao: DrinkDao, private val afterWrite: suspend () -> Unit = {}) {
    private val writeMutex = Mutex()
    private var version = 0L
    private var outstanding: UndoTicket? = null

    val records: Flow<List<DrinkRecord>> = dao.observeAll().map { list -> list.map { it.asRecord() } }

    /** Latest recorded entry, used only to suppress a redundant notification after a drink. */
    suspend fun latestTimeMillis(): Long? = records.first().asSequence().filter { it.timeMillis <= System.currentTimeMillis() }.maxOfOrNull { it.timeMillis }

    suspend fun add(kind: DrinkKind, ml: Int, atMillis: Long = System.currentTimeMillis()): UndoTicket = writeMutex.withLock {
        DrinkRules.checkAmount(ml)
        val id = dao.insert(DrinkEntity(amountMl = ml, drinkType = kind.name, timestampMillis = atMillis))
        notifyWidgets()
        issue(UndoAction.Remove(id), "Drink added")
    }

    suspend fun edit(id: Long, kind: DrinkKind, ml: Int, atMillis: Long): UndoTicket? = writeMutex.withLock {
        DrinkRules.checkAmount(ml)
        val before = dao.findById(id) ?: return@withLock null
        val edited = before.copy(amountMl = ml, drinkType = kind.name, timestampMillis = atMillis)
        if (edited == before) return@withLock null
        if (dao.update(edited) == 0) return@withLock null
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
                    .build().also { db = it }
            }
            return GulpRepository(instance.drinks(), afterWrite = { GulpWidgetRefresh.refresh(context.applicationContext) })
        }
    }
}
