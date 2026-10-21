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
import com.kmab.gulp.core.domain.DrinkKind
import com.kmab.gulp.core.domain.DrinkRecord
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "drink_records")
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amountMl: Int,
    val drinkType: String,
    val timestampMillis: Long
) {
    fun asRecord(): DrinkRecord = DrinkRecord(id, amountMl, DrinkKind.fromStored(drinkType), timestampMillis)

    companion object {
        fun from(record: DrinkRecord) = DrinkEntity(record.id, record.amountMl, record.kind.name, record.timeMillis)
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

@Database(entities = [DrinkEntity::class], version = 1, exportSchema = false)
abstract class GulpDatabase : RoomDatabase() {
    abstract fun drinks(): DrinkDao
}
