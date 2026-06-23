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

    /**
     * Convertit l'objet [DayQuality] en son nom de constante String pour le stockage SQL.
     * @param value L'état qualitatif sélectionné (ex: DayQuality.TOP).
     * @return Le nom de l'énumération (ex: "TOP").
     */
    @TypeConverter
    fun fromDayQuality(value: DayQuality): String = value.name

    /**
     * Reconstruit l'objet [DayQuality] à partir de la valeur textuelle lue en base de données.
     * @param value La chaîne de caractères stockée (ex: "MEDIUM").
     * @return L'instance correspondante de l'énumération [DayQuality].
     */
    @TypeConverter
    fun toDayQuality(value: String): DayQuality = DayQuality.valueOf(value)
}