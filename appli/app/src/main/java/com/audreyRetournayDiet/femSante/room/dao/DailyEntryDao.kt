package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {
    @Insert
    suspend fun insert(entry: DailyEntryEntity): Long

    @Update
    suspend fun update(entry: DailyEntryEntity)

    @Delete
    suspend fun delete(entry: DailyEntryEntity)

    @Query("SELECT * FROM daily_entry WHERE id = :id")
    fun getById(id: Long): Flow<DailyEntryEntity?>

    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND date = :date")
    fun getByUserAndDate(userId: String, date: Long): Flow<DailyEntryEntity?>
}