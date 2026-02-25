package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "general_state",
    foreignKeys = [
        ForeignKey(
            entity = DailyEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE // C'est ici que la magie opère
        )
    ],
    indices = [Index(value = ["entry_id"])]
)
data class GeneralStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "entry_id")
    val entryId: Long, // Référence obligatoire vers la table principale

    @ColumnInfo(name = "is_tired")
    val isTired: Boolean = false,

    @ColumnInfo(name = "pain_level")
    val painLevel: Int = 0
)