package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) pour l'état général de santé.
 * * Cette interface gère la persistance des indicateurs de base :
 * - Niveau de douleur (Pain Level)
 * - Qualité du sommeil et niveau d'énergie
 * - Poids et hydratation
 * - Qualité globale de la journée
 */
@Dao
interface GeneralStateDao {

    /**
     * Insère un nouvel état général.
     * @param state L'entité contenant les indicateurs physiques de base.
     * @return L'ID généré par SQLite.
     */
    @Insert
    suspend fun insert(state: GeneralStateEntity): Long

    /**
     * Met à jour un état général existant (ex: modification du poids ou du sommeil).
     */
    @Update
    suspend fun update(state: GeneralStateEntity)

    /**
     * Supprime les données d'état général associées à une entrée.
     */
    @Delete
    suspend fun delete(state: GeneralStateEntity)

    /**
     * Récupère un état général spécifique par son identifiant unique.
     * * @param id L'identifiant technique de la ligne.
     * @return Un [Flow] pour une observation réactive du score de santé.
     */
    @Query("SELECT * FROM general_state WHERE id = :id")
    fun getById(id: Long): Flow<GeneralStateEntity?>
}