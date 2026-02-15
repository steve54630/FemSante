package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity

class ActivityConverter {

    @TypeConverter
    fun fromPhysicalActivity(value: PhysicalActivity): String = value.name

    @TypeConverter
    fun toPhysicalActivity(value: String): PhysicalActivity = PhysicalActivity.valueOf(value)

}