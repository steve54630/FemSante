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

    /**
     * Transforme une liste de zones de douleur en une chaîne de caractères unique.
     * Chaque zone est représentée par son nom technique, séparé par une virgule.
     * * @param value La liste des zones sélectionnées par l'utilisatrice.
     * @return Une chaîne de caractères prête pour le stockage en base.
     */
    @TypeConverter
    fun fromPainZoneList(value: List<PainZone>): String {
        return value.joinToString(",") { it.name }
    }

    /**
     * Reconstruit la liste d'énumérations à partir de la chaîne stockée en base.
     * * @param value La chaîne de caractères récupérée de SQLite.
     * @return Une liste d'objets [PainZone] ou une liste vide si la donnée est absente.
     */
    @TypeConverter
    fun toPainZoneList(value: String): List<PainZone> {
        // Sécurité : évite une erreur lors du split sur une chaîne vide
        if (value.isEmpty()) return emptyList()

        // Découpage de la chaîne et conversion de chaque fragment en Enum
        return value.split(",").map { PainZone.valueOf(it) }
    }
}