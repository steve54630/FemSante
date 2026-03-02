package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) pour le suivi de l'état psychologique.
 *
 * Cette interface gère la persistance des indicateurs émotionnels et cognitifs :
 * - Niveau de stress et charge mentale.
 * - Émotions dominantes de la journée.
 * - Libido et vie intime.
 * - Symptômes de type "brouillard mental" ou irritabilité.
 */
@Dao
interface PsychologicalStateDao {

    /**
     * Insère un nouvel état psychologique pour une journée donnée.
     * @param state L'entité regroupant les scores de bien-être mental.
     * @return L'ID unique de la ligne créée dans SQLite.
     */
    @Insert
    suspend fun insert(state: PsychologicalStateEntity): Long

    /**
     * Met à jour les données psychologiques (ex: modification du niveau de stress).
     */
    @Update
    suspend fun update(state: PsychologicalStateEntity)

    /**
     * Supprime l'entrée psychologique associée.
     */
    @Delete
    suspend fun delete(state: PsychologicalStateEntity)

    /**
     * Récupère un état psychologique précis via son identifiant technique.
     *
     * @param id L'identifiant de la ligne dans la table `psychological_state`.
     * @return Un [Flow] permettant d'observer dynamiquement l'évolution du bien-être mental.
     */
    @Query("SELECT * FROM psychological_state WHERE id = :id")
    fun getById(id: Long): Flow<PsychologicalStateEntity?>
}