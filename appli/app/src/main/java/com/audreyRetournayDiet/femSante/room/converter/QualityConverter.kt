package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.DayQuality


class QualityConverter {

    @TypeConverter
    fun fromDayQuality(value: DayQuality): String = value.name

    @TypeConverter
    fun toDayQuality(value: String): DayQuality = DayQuality.valueOf(value)
}