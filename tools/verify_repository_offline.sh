#!/usr/bin/env bash
# Compiles the repository and DAO against minimal Room/Android stubs and runs real Kotlin coroutine logic.
# Catches Kotlin type/syntax and undo bugs; it is NOT an Android Room/KSP build.
set -euo pipefail
cd "$(dirname "$0")/.."
COROUTINES="$(dirname "$(command -v kotlinc)")/../lib/kotlinx-coroutines-core-jvm.jar"
if [ ! -f "$COROUTINES" ]; then
  echo 'SKIP: Kotlin CLI kotlinx-coroutines-core-jvm.jar unavailable' >&2
  exit 0
fi
DIR="$(mktemp -d)"; trap 'rm -rf "$DIR"' EXIT
mkdir -p "$DIR/stubs"
cat > "$DIR/stubs/Room.kt" <<'KT'
package androidx.room
import android.content.Context
annotation class Dao
annotation class Database(val entities: Array<kotlin.reflect.KClass<*>>, val version: Int, val exportSchema: Boolean)
annotation class Delete
annotation class Entity(val tableName: String)
annotation class Insert(val onConflict: Int = 0)
annotation class PrimaryKey(val autoGenerate: Boolean = false)
annotation class Query(val value: String)
annotation class Update
object OnConflictStrategy { const val REPLACE = 1 }
abstract class RoomDatabase
object Room {
  fun <T: RoomDatabase> databaseBuilder(context: Context, klass: Class<T>, name: String): Builder<T> = Builder()
  class Builder<T: RoomDatabase> { fun addMigrations(vararg migrations: androidx.room.migration.Migration): Builder<T> = this; fun build(): T = error("stub") }
}
KT
cat > "$DIR/stubs/Migration.kt" <<'KT'
package androidx.room.migration
import androidx.sqlite.db.SupportSQLiteDatabase
abstract class Migration(val startVersion: Int, val endVersion: Int) {
    abstract fun migrate(db: SupportSQLiteDatabase)
}
KT
cat > "$DIR/stubs/SQLite.kt" <<'KT'
package androidx.sqlite.db
interface SupportSQLiteDatabase { fun execSQL(statement: String) }
KT
cat > "$DIR/stubs/Context.kt" <<'KT'
package android.content
open class Context { val applicationContext: Context get() = this }
KT
cat > "$DIR/stubs/RepoCheck.kt" <<'KT'
import com.kmab.gulp.core.data.*
import com.kmab.gulp.core.domain.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
class InMemoryDao : DrinkDao {
    private val flow = MutableStateFlow<List<DrinkEntity>>(emptyList())
    private var next = 1L
    override fun observeAll(): Flow<List<DrinkEntity>> = flow
    override suspend fun findById(id: Long): DrinkEntity? = flow.value.find { it.id == id }
    override suspend fun insert(entry: DrinkEntity): Long {
        val id = if (entry.id == 0L) next++ else entry.id
        flow.value = (flow.value.filterNot { it.id == id } + entry.copy(id = id))
            .sortedWith(compareByDescending<DrinkEntity> { it.timestampMillis }.thenByDescending { it.id })
        return id
    }
    override suspend fun update(entry: DrinkEntity): Int {
        if (findById(entry.id) == null) return 0
        flow.value = flow.value.map { if (it.id == entry.id) entry else it }
        return 1
    }
    override suspend fun delete(entry: DrinkEntity): Int {
        if (findById(entry.id) == null) return 0
        flow.value = flow.value.filterNot { it.id == entry.id }
        return 1
    }
    override suspend fun deleteAll(): Int {
        val count = flow.value.size
        flow.value = emptyList()
        return count
    }
}
fun main() = runBlocking {
    val repo = GulpRepository(InMemoryDao())
    val add = repo.add(DrinkKind.WATER, 250, 1000L)
    check(repo.records.first().single().amountMl == 250)
    val id = repo.records.first().single().id
    val edit = repo.edit(id, DrinkKind.COFFEE, 175, 2000L)!!
    check(repo.records.first().single().kind == DrinkKind.COFFEE)
    check(!repo.undo(add)) // newer edit superseded the earlier undo
    check(repo.undo(edit))
    check(repo.records.first().single().amountMl == 250)
    val del = repo.delete(id)!!
    check(repo.records.first().isEmpty())
    check(repo.undo(del))
    check(repo.records.first().single().id == id)
    check(!repo.undo(del))
    val last = repo.add(DrinkKind.TEA, 225)
    check(repo.undo(last))
    check(repo.records.first().size == 1)
    check(repo.exportCsv().lines()[0] == DrinkCsv.HEADER)
    check(repo.deleteAll() == 1)
    check(repo.records.first().isEmpty())
    check(repo.deleteAll() == 0)
    println("PASS: repository add/edit/delete/undo/export/erase contract (Room API stubs)")
}
KT
cat > "$DIR/stubs/Log.kt" <<'KT'
package android.util
object Log { fun w(tag: String, message: String, tr: Throwable): Int = 0 }
KT
cat > "$DIR/stubs/Widget.kt" <<'KT'
package com.kmab.gulp.widget
import android.content.Context
object GulpWidgetRefresh { suspend fun refresh(context: Context) {} }
KT
kotlinc "$DIR/stubs/Room.kt" "$DIR/stubs/Migration.kt" "$DIR/stubs/SQLite.kt" "$DIR/stubs/Context.kt" "$DIR/stubs/Log.kt" "$DIR/stubs/Widget.kt" "$DIR/stubs/RepoCheck.kt" \
    app/src/main/java/com/kmab/gulp/core/domain/DrinkModels.kt \
    app/src/main/java/com/kmab/gulp/core/domain/DrinkType.kt \
    app/src/main/java/com/kmab/gulp/core/domain/DrinkCsv.kt \
    app/src/main/java/com/kmab/gulp/core/data/GulpDatabase.kt \
    app/src/main/java/com/kmab/gulp/core/data/GulpRepository.kt \
    -classpath "$COROUTINES" -include-runtime -d "$DIR/repo.jar"
java -cp "$DIR/repo.jar:$COROUTINES" RepoCheckKt
