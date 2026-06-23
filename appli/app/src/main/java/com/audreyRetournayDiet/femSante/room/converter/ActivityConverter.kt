package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity

/**
 * Convertisseur de types pour Room permettant la persistance de l'énumération [PhysicalActivity].
 * * Puisque SQLite ne supporte pas nativement les types Enum, cette classe transforme :
 * 1. L'objet [PhysicalActivity] en [String] pour l'écriture en base de données.
 * 2. La [String] stockée en objet [PhysicalActivity] lors de la lecture.
 */
class ActivityConverter {

    /**
     * Transforme une instance de l'énumération en sa représentation textuelle (Nom de la constante).
     * @param value L'activité physique à convertir (ex: LOW, MODERATE, HIGH).
     * @return Le nom de l'énumération sous forme de chaîne de caractères.
     */
    @TypeConverter
    fun fromPhysicalActivity(value: PhysicalActivity): String = value.name

    /**
     * Reconstruit l'instance de l'énumération à partir de son nom stocké en base de données.
     * @param value La chaîne de caractères récupérée de SQLite.
     * @return L'objet [PhysicalActivity] correspondant.
     * @throws IllegalArgumentException si la valeur en base ne correspond à aucune constante de l'Enum.
     */
    @TypeConverter
    fun toPhysicalActivity(value: String): PhysicalActivity = PhysicalActivity.valueOf(value)

}