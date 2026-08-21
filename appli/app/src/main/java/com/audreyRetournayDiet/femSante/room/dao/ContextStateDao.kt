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

    @Insert
    suspend fun insert(state: ContextStateEntity): Long

    @Update
    suspend fun update(state: ContextStateEntity)

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