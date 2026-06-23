package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity

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

    @Query("SELECT * FROM cycle_day WHERE user_id = :userId ORDER BY date ASC")
    suspend fun getAll(userId: String): List<CycleDayEntity>

    /** Dates (timestamps) des jours de règles, pour le marqueur du calendrier. */
    @Query("SELECT date FROM cycle_day WHERE user_id = :userId AND is_period = 1")
    suspend fun getPeriodDates(userId: String): List<Long>
}
