package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.audreyRetournayDiet.femSante.room.type.PainZone

@Entity(tableName = "symptom_state")
data class SymptomStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "pain_zone")
    val localizedPains : List<PainZone> = emptyList(),

    @ColumnInfo(name = "nausea")
    val hasNausea : Boolean = false,

    @ColumnInfo(name = "others")
    val others : String? = ""
)