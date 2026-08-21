package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) pour le suivi des symptômes physiques.
 * * Cette interface gère la persistance des manifestations cliniques quotidiennes :
 * - Localisation des douleurs (via PainZoneConverter).
 * - Troubles digestifs (ballonnements, transit).
 * - Signes dermatologiques ou hormonaux (acné, tension mammaire).
 * - Intensité spécifique de chaque symptôme.
 */
@Dao
interface SymptomStateDao {

    @Insert
    suspend fun insert(state: SymptomStateEntity): Long

    @Update
    suspend fun update(state: SymptomStateEntity)

    @Delete
    suspend fun delete(state: SymptomStateEntity)

    /**
     * Récupère le détail des symptômes via son identifiant unique.
     * * @param id L'identifiant technique de la ligne.
     * @return Un [Flow] pour observer les changements (ex: affichage dynamique
     * d'un récapitulatif des symptômes dans le journal).
     */
    @Query("SELECT * FROM symptom_state WHERE id = :id")
    fun getById(id: Long): Flow<SymptomStateEntity?>
}