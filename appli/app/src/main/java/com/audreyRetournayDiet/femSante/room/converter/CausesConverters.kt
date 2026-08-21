package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause

/**
 * Convertisseur de types permettant de stocker une liste d'énumérations [DifficultyCause]
 * dans une seule colonne de texte SQLite.
 * * ### Stratégie de persistance :
 * - **Sérialisation** : Transforme la liste [STRESS, FATIGUE] en une chaîne "STRESS,FATIGUE".
 * - **Désérialisation** : Découpe la chaîne via la virgule pour recréer la liste d'objets Kotlin.
 */
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