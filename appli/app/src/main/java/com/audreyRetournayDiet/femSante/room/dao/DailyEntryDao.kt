package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
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

    // --- REQUÊTES DE LECTURE ---

    @Transaction
    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND id = :id LIMIT 1")
    suspend fun getFullEntry(userId: String, id: Long): DailyEntryFull?

    // Correction de la jointure : On joint maintenant via gs.entry_id
    @Query("""
        SELECT de.date as date, gs.pain_level as painLevel
        FROM daily_entry de
        JOIN general_state gs ON de.id = gs.entry_id 
        WHERE de.user_id = :userId
    """)
    suspend fun getCalendarStatus(userId: String): List<DatePainStatus>

    // --- INSERTIONS ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyEntry(entry: DailyEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneral(general: GeneralStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPsychological(psy: PsychologicalStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: SymptomStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContext(context: ContextStateEntity)

    @Transaction
    suspend fun insertFullDailyEntry(
        userId: String,
        date: Long,
        general: GeneralStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
        context: ContextStateEntity
    ) {
        // 1. On insère le parent et on récupère son ID généré
        val newId = insertDailyEntry(DailyEntryEntity(userId = userId, date = date))

        // 2. On injecte cet ID dans chaque enfant avant de les insérer
        insertGeneral(general.copy(entryId = newId))
        insertPsychological(psy.copy(entryId = newId))
        insertSymptom(symptom.copy(entryId = newId))
        insertContext(context.copy(entryId = newId))
    }

    // --- SUPPRESSION (Magie de la cascade) ---

    @Query("DELETE FROM daily_entry WHERE user_id = :userId AND id = :id")
    suspend fun deleteFullEntry(userId: String, id: Long)
    @Transaction
    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND date = :timestamp LIMIT 1")
    suspend fun getFullEntryByDate(userId: String, timestamp: Long) : DailyEntryFull?
}