package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.audreyRetournayDiet.femSante.room.type.PainZone

@Entity(
    tableName = "symptom_state",
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
data class SymptomStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    @ColumnInfo(name = "pain_zone")
    val localizedPains : List<PainZone> = emptyList(),

    @ColumnInfo(name = "nausea")
    val hasNausea : Boolean = false,

    @ColumnInfo(name = "others")
    val others : String? = ""
)