package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import com.audreyRetournayDiet.femSante.room.entity.DatePainStatus
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import com.audreyRetournayDiet.femSante.room.type.DatePainResult
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyEntryDao {

    @Query("SELECT * FROM daily_entry WHERE id = :id")
    fun getById(id: Long): Flow<DailyEntryEntity?>

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

    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND date = :date")
    fun getByUserAndDate(userId: String, date: Long): Flow<DailyEntryEntity?>

    @Query("""SELECT de.date, gs.pain_level
    FROM daily_entry de
    INNER JOIN general_state gs ON de.general_state_id = gs.id
    WHERE de.user_id = :userId""")
    fun getPainLevelsByDate(userId: String): Flow<List<DatePainResult>>

    @Insert
    suspend fun insertGeneral(general: GeneralStateEntity): Long

    @Insert
    suspend fun insertPsychological(psy: PsychologicalStateEntity): Long

    @Insert
    suspend fun insertSymptom(symptom: SymptomStateEntity): Long

    @Insert
    suspend fun insertContext(context: ContextStateEntity): Long

    @Insert
    suspend fun insertDailyEntry(entry: DailyEntryEntity): Long

    @Transaction
    suspend fun insertFullDailyEntry(
        userId: String,
        date: Long,
        general: GeneralStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
        context: ContextStateEntity
    ) {
        val genId = insertGeneral(general)
        val psyId = insertPsychological(psy)
        val symId = insertSymptom(symptom)
        val conId = insertContext(context)

        val fullEntry = DailyEntryEntity(
            userId = userId,
            date = date,
            generalStateId = genId,
            psychologicalStateId = psyId,
            symptomsStateId = symId,
            contextStateId = conId
        )

        // 3. On insère le pivot
        insertDailyEntry(fullEntry)
    }

}