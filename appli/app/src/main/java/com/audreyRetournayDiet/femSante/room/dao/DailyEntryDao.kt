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

/**
 * DAO Maître pour la gestion des entrées quotidiennes du journal de bord.
 * * Cette classe orchestre la persistance multi-tables. Une "entrée" n'est pas une simple ligne,
 * mais un assemblage de 5 entités distinctes reliées par l'ID de l'entrée (`entry_id`).
 * * L'utilisation d'une classe `abstract` permet de définir des fonctions `open` avec
 * une logique personnalisée tout en laissant Room générer le code SQL de base.
 */
@Dao
abstract class DailyEntryDao {

    // --- LECTURE ---

    /**
     * Récupère l'intégralité des données d'une journée via son ID.
     * @return Un [DailyEntryFull] (DTO) qui contient l'objet parent et ses 4 enfants.
     */
    @Transaction
    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND id = :id LIMIT 1")
    abstract suspend fun getFullEntry(userId: String, id: Long): DailyEntryFull?

    /**
     * Récupère les données d'une journée via un timestamp (utile pour le calendrier).
     */
    @Transaction
    @Query("SELECT * FROM daily_entry WHERE user_id = :userId AND date = :timestamp LIMIT 1")
    abstract suspend fun getFullEntryByDate(userId: String, timestamp: Long): DailyEntryFull?

    /**
     * Extrait les informations minimales pour l'affichage visuel du calendrier.
     * Effectue une jointure SQL pour corréler la date et le niveau de douleur.
     */
    @Query("""
        SELECT de.date as date, gs.pain_level as painLevel
        FROM daily_entry de
        JOIN general_state gs ON de.id = gs.entry_id 
        WHERE de.user_id = :userId
    """)
    abstract suspend fun getCalendarStatus(userId: String): List<DatePainStatus>

    // --- INSERTIONS DE BASE ---
    // On utilise REPLACE pour écraser automatiquement les données si un ID identique existe.

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

    /**
     * Insère une journée complète de manière atomique.
     * Si une seule des insertions échoue, toute la transaction est annulée.
     */
    @Transaction
    open suspend fun insertFullDailyEntry(
        userId: String,
        date: Long,
        general: GeneralStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
        context: ContextStateEntity
    ) {
        // 1. On crée le parent (DailyEntryEntity) et on récupère son ID auto-généré
        val newId = insertDailyEntry(DailyEntryEntity(userId = userId, date = date))

        // 2. On injecte cet ID dans tous les sous-états avant de les sauvegarder
        saveSubStates(newId, general, psy, symptom, context)
    }

    /**
     * Met à jour une journée existante.
     * Utilise `let` pour s'assurer que l'ID existe avant de tenter la mise à jour.
     */
    @Transaction
    open suspend fun editFullDailyEntry(
        userId: String,
        id: Long,
        general: GeneralStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
        context: ContextStateEntity
    ) {
        getFullEntry(userId, id)?.let { existing ->
            saveSubStates(existing.dailyEntry.id, general, psy, symptom, context)
        }
    }

    /**
     * Méthode utilitaire pour factoriser l'assignation de l'ID parent aux enfants.
     * Utilise `.copy()` pour garder l'immuabilité des entités.
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

    /**
     * Supprime l'entrée parente.
     * Note : Si tes Foreign Keys sont configurées avec ON DELETE CASCADE,
     * cela supprimera automatiquement tous les sous-états associés.
     */
    @Query("DELETE FROM daily_entry WHERE user_id = :userId AND id = :id")
    abstract suspend fun deleteFullEntry(userId: String, id: Long)
}