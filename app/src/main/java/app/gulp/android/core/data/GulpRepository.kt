package app.gulp.android.core.data

import android.content.Context
import androidx.room.Room
import app.gulp.android.core.domain.DrinkKind
import app.gulp.android.core.domain.DrinkRecord
import app.gulp.android.core.domain.DrinkRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Undo tokens are single-use. Concurrent edits are serialized to avoid lost writes. */
sealed interface UndoAction {
    data class Remove(val id: Long) : UndoAction
    data class Restore(val record: DrinkRecord) : UndoAction
}

data class UndoTicket(val sequence: Long, val action: UndoAction, val message: String)

class GulpRepository(private val dao: DrinkDao) {
    private val writeMutex = Mutex()
    private var version = 0L
    private var outstanding: UndoTicket? = null

    val records: Flow<List<DrinkRecord>> = dao.observeAll().map { list -> list.map { it.asRecord() } }

    suspend fun add(kind: DrinkKind, ml: Int, atMillis: Long = System.currentTimeMillis()): UndoTicket = writeMutex.withLock {
        DrinkRules.checkAmount(ml)
        val id = dao.insert(DrinkEntity(amountMl = ml, drinkType = kind.name, timestampMillis = atMillis))
        issue(UndoAction.Remove(id), "Drink added")
    }

    suspend fun edit(id: Long, kind: DrinkKind, ml: Int, atMillis: Long): UndoTicket? = writeMutex.withLock {
        DrinkRules.checkAmount(ml)
        val before = dao.findById(id) ?: return@withLock null
        val edited = before.copy(amountMl = ml, drinkType = kind.name, timestampMillis = atMillis)
        if (edited == before) return@withLock null
        if (dao.update(edited) == 0) return@withLock null
        issue(UndoAction.Restore(before.asRecord()), "Drink updated")
    }

    suspend fun delete(id: Long): UndoTicket? = writeMutex.withLock {
        val old = dao.findById(id) ?: return@withLock null
        if (dao.delete(old) == 0) return@withLock null
        issue(UndoAction.Restore(old.asRecord()), "Drink deleted")
    }

    suspend fun undo(ticket: UndoTicket): Boolean = writeMutex.withLock {
        if (ticket != outstanding) return@withLock false
        when (val action = ticket.action) {
            is UndoAction.Remove -> dao.findById(action.id)?.let { dao.delete(it) }
            is UndoAction.Restore -> dao.insert(DrinkEntity.from(action.record))
        }
        outstanding = null
        true
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
            return GulpRepository(instance.drinks())
        }
    }
}
