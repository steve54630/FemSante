package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "symptom_state")
data class SymptomStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "pain_zone")
    val localizedPains : List<String> = emptyList<String>(),

    @ColumnInfo(name = "nausea")
    val hasNausea : Boolean = false,

    @ColumnInfo(name = "others")
    val others : String? = ""
)