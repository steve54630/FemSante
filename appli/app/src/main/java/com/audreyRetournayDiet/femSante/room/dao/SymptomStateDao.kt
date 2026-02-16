package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomStateDao {
    @Insert
    suspend fun insert(state: SymptomStateEntity): Long

    @Update
    suspend fun update(state: SymptomStateEntity)

    @Delete
    suspend fun delete(state: SymptomStateEntity)

    @Query("SELECT * FROM symptom_state WHERE id = :id")
    fun getById(id: Long): Flow<SymptomStateEntity?>
}