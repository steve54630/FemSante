package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) pour la gestion du contexte quotidien.
 * * Cette interface définit les interactions avec la table `context_state`.
 * Elle permet de suivre les facteurs externes influençant la santé (ex: hydratation,
 * phase du cycle, écarts alimentaires).
 */
@Dao
interface ContextStateDao {

    /**
     * Insère un nouvel état de contexte en base de données.
     * @param state L'entité contenant les données contextuelles.
     * @return L'identifiant unique (Row ID) généré pour cette insertion.
     */
    @Insert
    suspend fun insert(state: ContextStateEntity): Long

    /**
     * Met à jour les informations d'un contexte existant.
     * @param state L'entité mise à jour (identifiée par sa clé primaire).
     */
    @Update
    suspend fun update(state: ContextStateEntity)

    /**
     * Supprime définitivement un contexte de la base de données.
     */
    @Delete
    suspend fun delete(state: ContextStateEntity)

    /**
     * Récupère le contexte associé à une entrée spécifique du journal.
     * * @param entryId L'identifiant de la journée de suivi correspondante.
     * @return Un [Flow] permettant d'observer en temps réel les changements
     * sur cet état (émet `null` si aucune donnée n'existe pour cet ID).
     */
    @Query("SELECT * FROM context_state WHERE entry_id = :entryId")
    fun getByEntryId(entryId: Long): Flow<ContextStateEntity?>
}