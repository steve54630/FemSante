package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneralStateDao {
    @Insert
    suspend fun insert(state: GeneralStateEntity): Long

    @Update
    suspend fun update(state: GeneralStateEntity)

    @Delete
    suspend fun delete(state: GeneralStateEntity)

    @Query("SELECT * FROM general_state WHERE id = :id")
    fun getById(id: Long): Flow<GeneralStateEntity?>
}