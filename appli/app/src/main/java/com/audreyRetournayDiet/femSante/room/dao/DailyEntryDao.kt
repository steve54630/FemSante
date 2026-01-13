package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import com.audreyRetournayDiet.femSante.room.type.DatePainResult
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {
    @Insert
    suspend fun insert(entry: DailyEntryEntity): Long

    @Update
    suspend fun update(entry: DailyEntryEntity)

    @Delete
    suspend fun delete(entry: DailyEntryEntity)

    @Transaction
    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND date = :date LIMIT 1")
    suspend fun getFullEntry(userId: String, date: Long): DailyEntryFull?

    @Query("""
    SELECT daily_entry.date as date, general_state.pain_level as painLevel
    FROM daily_entry 
    JOIN general_state ON daily_entry.general_state_id = general_state.id 
    WHERE daily_entry.user_id = :userId
""")
    suspend fun getCalendarStatus(userId: String): List<DatePainStatus>

    // Petit DTO pour transporter la donnée
    data class DatePainStatus(val date: Long, val painLevel: Int)

    @Query("SELECT * FROM daily_entry WHERE id = :id")
    fun getById(id: Long): Flow<DailyEntryEntity?>

    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND date = :date")
    fun getByUserAndDate(userId: String, date: Long): Flow<DailyEntryEntity?>

    @Query("""SELECT de.date, gs.pain_level
    FROM daily_entry de
    INNER JOIN general_state gs ON de.general_state_id = gs.id
    WHERE de.user_id = :userId""")
    fun getPainLevelsByDate(userId: String): Flow<List<DatePainResult>>

}