package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "general_state")
data class GeneralStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "is_tired")
    val isTired: Boolean = false,      // false = en forme par défaut

    @ColumnInfo(name = "pain_level")
    val painLevel: Int = 0             // 0 = pas de douleur par défaut
)