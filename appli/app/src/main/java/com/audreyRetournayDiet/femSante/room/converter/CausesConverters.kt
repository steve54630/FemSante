package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause

class CausesConverters {
    @TypeConverter
    fun fromList(value: List<DifficultyCause>): String {
        return value.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toList(value: String): List<DifficultyCause> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { DifficultyCause.valueOf(it) }
    }
}