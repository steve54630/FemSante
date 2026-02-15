package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContextStateDao {
    @Insert
    suspend fun insert(state: ContextStateEntity): Long

    @Update
    suspend fun update(state: ContextStateEntity)

    @Delete
    suspend fun delete(state: ContextStateEntity)

    @Query("SELECT * FROM context WHERE id = :id")
    fun getById(id: Long): Flow<ContextStateEntity?>
}