package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity

@Entity(
    tableName = "context_state",
    foreignKeys = [ForeignKey(
        entity = DailyEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entry_id"],
        onDelete = ForeignKey.CASCADE
    )], indices = [Index(value = ["entry_id"])]
)
data class ContextStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    @ColumnInfo(name = "entry_id") val entryId: Long, // <--- CETTE LIGNE MANQUAIT

    @ColumnInfo(name = "physical_activity") val physicalActivity: PhysicalActivity? = PhysicalActivity.REPOS,
    @ColumnInfo(name = "medecine_taken") val medecineTaken: Boolean = false,
    @ColumnInfo(name = "medication_list") val medicationList: String? = "",
    @ColumnInfo(name = "diet") val diet: String? = ""
)