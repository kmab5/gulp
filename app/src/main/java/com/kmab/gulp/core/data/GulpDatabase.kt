package com.kmab.gulp.core.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kmab.gulp.core.domain.DrinkType
import com.kmab.gulp.core.domain.DrinkTypeCatalog
import com.kmab.gulp.core.domain.DrinkKind
import com.kmab.gulp.core.domain.DrinkRecord
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "drink_records")
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amountMl: Int,
    val drinkType: String,
    val timestampMillis: Long,
    val drinkTypeId: String? = null,
    val drinkName: String? = null,
    val drinkIcon: String? = null
) {
    fun asRecord(): DrinkRecord {
        val kind = DrinkKind.fromStored(drinkType)
        return DrinkRecord(id, amountMl, kind, timestampMillis,
            drinkTypeId ?: kind.name.lowercase(), drinkName ?: kind.title,
            drinkIcon ?: DrinkTypeCatalog.defaultIcon(kind))
    }

    companion object {
        fun from(record: DrinkRecord) = DrinkEntity(record.id, record.amountMl, record.kind.name, record.timeMillis,
            record.drinkTypeId, record.drinkName, record.drinkIcon)
    }
}

@Dao
interface DrinkDao {
    @Query("SELECT * FROM drink_records ORDER BY timestampMillis DESC, id DESC")
    fun observeAll(): Flow<List<DrinkEntity>>

    @Query("SELECT * FROM drink_records WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): DrinkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DrinkEntity): Long

    @Update
    suspend fun update(entry: DrinkEntity): Int

    @Delete
    suspend fun delete(entry: DrinkEntity): Int

    @Query("DELETE FROM drink_records")
    suspend fun deleteAll(): Int
}

@Entity(tableName = "drink_types")
data class DrinkTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconId: String,
    val defaultAmountMl: Int,
    val legacyKind: String,
    val archived: Boolean = false
) {
    fun asType() = DrinkType(id, name, iconId, defaultAmountMl, DrinkKind.fromStored(legacyKind), archived)
    companion object {
        fun from(type: DrinkType) = DrinkTypeEntity(type.id, type.name, type.iconId,
            type.defaultAmountMl, type.legacyKind.name, type.archived)
    }
}

@Dao
interface DrinkTypeDao {
    @Query("SELECT * FROM drink_types ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<DrinkTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(type: DrinkTypeEntity)

    @Query("SELECT * FROM drink_types WHERE id = :id LIMIT 1")
    suspend fun find(id: String): DrinkTypeEntity?
}

/** Preserves v1 records, timestamps and legacy kinds. Only adds nullable snapshot fields. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE drink_records ADD COLUMN drinkTypeId TEXT")
        db.execSQL("ALTER TABLE drink_records ADD COLUMN drinkName TEXT")
        db.execSQL("ALTER TABLE drink_records ADD COLUMN drinkIcon TEXT")
        db.execSQL("""CREATE TABLE IF NOT EXISTS drink_types (
            id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, iconId TEXT NOT NULL,
            defaultAmountMl INTEGER NOT NULL, legacyKind TEXT NOT NULL, archived INTEGER NOT NULL)""")
        // Historical entries retain the exact legacy kind. No amount or timestamp modifications.
        db.execSQL("UPDATE drink_records SET drinkTypeId = lower(drinkType), drinkName = " +
            "CASE drinkType WHEN 'WATER' THEN 'Water' WHEN 'COFFEE' THEN 'Coffee' " +
            "WHEN 'TEA' THEN 'Tea' ELSE 'Other' END, drinkIcon = lower(drinkType) " +
            "WHERE drinkTypeId IS NULL")
    }
}

@Database(entities = [DrinkEntity::class, DrinkTypeEntity::class], version = 2, exportSchema = false)
abstract class GulpDatabase : RoomDatabase() {
    abstract fun drinks(): DrinkDao
    abstract fun types(): DrinkTypeDao
}
