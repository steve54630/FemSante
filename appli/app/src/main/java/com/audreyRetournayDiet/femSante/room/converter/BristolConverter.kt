package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.BristolType

/**
 * Convertisseur de types Room pour l'échelle de Bristol (transit digestif).
 * Suit le même pattern que [QualityConverter] : sérialisation en String.
 */
class BristolConverter {

    @TypeConverter
    fun fromBristolType(value: BristolType): String = value.name

    @TypeConverter
    fun toBristolType(value: String): BristolType = BristolType.valueOf(value)
}
