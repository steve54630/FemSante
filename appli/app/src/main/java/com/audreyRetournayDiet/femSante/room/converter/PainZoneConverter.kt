package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.PainZone

class PainZoneConverter {

    @TypeConverter
    fun fromPainZoneList(value: List<PainZone>): String {
        return value.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toPainZoneList(value: String): List<PainZone> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { PainZone.valueOf(it) }
    }
}