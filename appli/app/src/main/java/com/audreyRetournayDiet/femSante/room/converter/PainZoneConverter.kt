package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.PainZone

/**
 * Convertisseur de types Room pour la gestion des zones de douleur.
 * * Cette classe permet de persister une liste d'énumérations [PainZone] dans SQLite
 * en les sérialisant sous forme de chaîne de caractères (format CSV).
 * * ### Exemple de transformation :
 * - En Kotlin : `listOf(PainZone.PELVIS, PainZone.LOWER_BACK)`
 * - En SQL : `"PELVIS,LOWER_BACK"`
 */
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