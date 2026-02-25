package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.audreyRetournayDiet.femSante.room.converter.CausesConverters
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause


@Entity(
    tableName = "psychological_state",
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
data class PsychologicalStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "day_quality")
    val dayQuality : DayQuality = DayQuality.BONNE,

    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    @ColumnInfo(name = "causes")
    @param:TypeConverters(CausesConverters::class)
    val difficultyCauses : List<DifficultyCause> = emptyList(),

    @ColumnInfo(name = "autres")
    val autres : String? = null
)