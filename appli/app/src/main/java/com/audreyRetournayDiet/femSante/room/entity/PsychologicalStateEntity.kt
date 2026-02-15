package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause

@Entity(tableName = "psychological_state")
data class PsychologicalStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "day_quality")
    val dayQuality : DayQuality = DayQuality.BONNE,

    @ColumnInfo(name = "causes")
    val difficultyCauses : DifficultyCause? = null,

    @ColumnInfo(name = "autres")
    val autres : String? = null
)