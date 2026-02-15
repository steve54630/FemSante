package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PsychologicalStateDao {
    @Insert
    suspend fun insert(state: PsychologicalStateEntity): Long

    @Update
    suspend fun update(state: PsychologicalStateEntity)

    @Delete
    suspend fun delete(state: PsychologicalStateEntity)

    @Query("SELECT * FROM psychological_state WHERE id = :id")
    fun getById(id: Long): Flow<PsychologicalStateEntity?>
}