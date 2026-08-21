package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.DayQuality

/**
 * Convertisseur de types pour Room dédié à la perception qualitative de la journée.
 * * Cette classe permet de persister l'énumération [DayQuality] (ex: EXCELLENT, GOOD, BAD)
 * dans SQLite en la transformant en chaîne de caractères.
 * * ### Rôle dans l'application :
 * Utilisé dans le journal de bord pour enregistrer le ressenti global de l'utilisatrice
 * à la fin de chaque journée.
 */
class QualityConverter {

    @TypeConverter
    fun fromDayQuality(value: DayQuality): String = value.name

    @TypeConverter
    fun toDayQuality(value: String): DayQuality = DayQuality.valueOf(value)
}