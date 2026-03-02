package com.audreyRetournayDiet.femSante.room.dao

import android.util.Log
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

@Dao
abstract class DailyEntryDao {

    // --- LECTURE ---

    @Transaction
    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND id = :id LIMIT 1")
    abstract suspend fun getFullEntry(userId: String, id: Long): DailyEntryFull?

    @Transaction
    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND date = :timestamp LIMIT 1")
    abstract suspend fun getFullEntryByDate(userId: String, timestamp: Long): DailyEntryFull?

    @Query("SELECT id FROM daily_entry WHERE user_id = :userId AND date = :timestamp LIMIT 1")
    abstract suspend fun getIdByDate(userId: String, timestamp: Long): Long?

    @Query("""
        SELECT de.date as date, gs.pain_level as painLevel
        FROM daily_entry de
        JOIN general_state gs ON de.id = gs.entry_id 
        WHERE de.user_id = :userId
    """)
    abstract suspend fun getCalendarStatus(userId: String): List<DatePainStatus>

    // --- INSERTIONS DE BASE ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDailyEntry(entry: DailyEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertGeneral(general: GeneralStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPsychological(psy: PsychologicalStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertSymptom(symptom: SymptomStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertContext(context: ContextStateEntity)

    // --- LOGIQUE MÉTIER (TRANSACTIONS) ---

    @Transaction
    open suspend fun insertFullDailyEntry(
        userId: String,
        date: Long,
        general: GeneralStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
        context: ContextStateEntity
    ) {
        // Crée le parent et récupère l'ID
        val newId = insertDailyEntry(DailyEntryEntity(userId = userId, date = date))
        // Insère tous les détails
        saveSubStates(newId, general, psy, symptom, context)
    }

    @Transaction
    open suspend fun editFullDailyEntry(
        userId: String,
        id: Long,
        general: GeneralStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
        context: ContextStateEntity
    ) {
        Log.i("ID", "${general.id} $id ${getFullEntry(userId, id)}")
        // Récupère l'ID existant de manière sécurisée (sans !!)
        getFullEntry(userId, id)?.let { existing ->
            saveSubStates(existing.dailyEntry.id, general, psy, symptom, context)
        }
    }

    /**
     * Fonction utilitaire privée pour injecter l'ID dans les entités enfants
     */
    private suspend fun saveSubStates(
        entryId: Long,
        general: GeneralStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
        context: ContextStateEntity
    ) {
        insertGeneral(general.copy(entryId = entryId))
        insertPsychological(psy.copy(entryId = entryId))
        insertSymptom(symptom.copy(entryId = entryId))
        insertContext(context.copy(entryId = entryId))
    }

    // --- SUPPRESSION ---

    @Query("DELETE FROM daily_entry WHERE user_id = :userId AND id = :id")
    abstract suspend fun deleteFullEntry(userId: String, id: Long)
}