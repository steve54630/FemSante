package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO du suivi de cycle (incrément 1).
 */
@Dao
interface CycleDayDao {

    /**
     * Insère ou remplace l'observation du jour. Le conflit sur l'index unique
     * `(user_id, date)` garantit une seule ligne par jour (upsert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: CycleDayEntity): Long

    @Query("SELECT * FROM cycle_day WHERE user_id = :userId AND date = :date LIMIT 1")
    suspend fun getByDate(userId: String, date: Long): CycleDayEntity?

    /** Version réactive de [getByDate] (saisie cycle de l'écran détail). */
    @Query("SELECT * FROM cycle_day WHERE user_id = :userId AND date = :date LIMIT 1")
    fun observeByDate(userId: String, date: Long): Flow<CycleDayEntity?>

    /** Version réactive de [getPeriodDates] (marqueurs de règles de la grille). */
    @Query("SELECT date FROM cycle_day WHERE user_id = :userId AND is_period = 1")
    fun observePeriodDates(userId: String): Flow<List<Long>>

    @Query("SELECT * FROM cycle_day WHERE user_id = :userId ORDER BY date ASC")
    suspend fun getAll(userId: String): List<CycleDayEntity>

    /** Dates (timestamps) des jours de règles, pour le marqueur du calendrier. */
    @Query("SELECT date FROM cycle_day WHERE user_id = :userId AND is_period = 1")
    suspend fun getPeriodDates(userId: String): List<Long>

    /**
     * Dates (timestamps) de tous les jours explicitement saisis (règles ou non). Sert au
     * prévisionnel : un jour déjà traité par l'utilisatrice (confirmé ou décoché) ne doit
     * plus apparaître comme prédit.
     */
    @Query("SELECT date FROM cycle_day WHERE user_id = :userId")
    suspend fun getRecordedDates(userId: String): List<Long>
}
