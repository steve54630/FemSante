package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.PainZone

/**
 * Convertisseur Room pour la douleur **par zone** : associe chaque zone à une intensité
 * (1 à 10). Sérialise la `Map<PainZone, Int>` en texte, distinct de [PainZoneConverter]
 * (qui, lui, gère la simple liste de zones sans intensité).
 *
 * ### Format de stockage :
 * - En Kotlin : `mapOf(PainZone.BASSIN to 7, PainZone.TETE to 3)`
 * - En SQL : `"BASSIN:7,TETE:3"`
 *
 * La lecture est tolérante : toute entrée malformée ou zone inconnue (ancienne base,
 * enum renommé) est ignorée plutôt que de faire planter la lecture.
 */
class PainZoneLevelConverter {

    @TypeConverter
    fun fromPainByZone(value: Map<PainZone, Int>): String {
        return value.entries.joinToString(",") { "${it.key.name}:${it.value}" }
    }

    @TypeConverter
    fun toPainByZone(value: String): Map<PainZone, Int> {
        if (value.isEmpty()) return emptyMap()
        return value.split(",").mapNotNull { part ->
            val pieces = part.split(":")
            if (pieces.size != 2) return@mapNotNull null
            val zone = runCatching { PainZone.valueOf(pieces[0]) }.getOrNull() ?: return@mapNotNull null
            val level = pieces[1].toIntOrNull()?.takeIf { it in 1..10 } ?: return@mapNotNull null
            zone to level
        }.toMap()
    }
}
