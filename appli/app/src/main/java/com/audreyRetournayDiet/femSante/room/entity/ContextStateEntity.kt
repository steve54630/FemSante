package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity

@Entity(tableName = "context")
data class ContextStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id : Long = 0L,

    @ColumnInfo(name = "physical_activity")
    val physicalActivity: PhysicalActivity = PhysicalActivity.REPOS,

    @ColumnInfo(name = "medecine_taken")
    val tookMedication : Boolean = false,

    @ColumnInfo(name = "medication_list")
    val medicationList : List<String> = emptyList<String>(),

    @ColumnInfo(name = "diet")
    val dietNotes : String? = ""
)