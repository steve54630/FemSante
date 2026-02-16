package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause

class CausesConverters {

    @TypeConverter
    fun fromDifficultyCause(value: DifficultyCause): String = value.name

    @TypeConverter
    fun toDifficultyCause(value: String): DifficultyCause = DifficultyCause.valueOf(value)
}