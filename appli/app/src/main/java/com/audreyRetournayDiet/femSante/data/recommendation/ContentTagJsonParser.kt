package com.audreyRetournayDiet.femSante.data.recommendation

import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.room.type.PainZone
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Parseur isolé de la table **contenu ↔ tags** (`content_tags.json`).
 *
 * Reconstruit le `Map<ContentRef, Set<JournalTag>>` attendu par [RecommendationEngine] à partir de
 * chaînes : chaque tableau (`zones`, `causes`, `qualities`, `activities`) est mappé sur l'enum
 * correspondant, et `flags` sur les tags-objets ([JournalTag.Digestif], etc.).
 *
 * Indépendant du `Context` (travaille sur une chaîne JSON) → testable en JVM sur un fixture, et
 * prêt pour une source serveur (le repo n'aura qu'à changer d'où vient le JSON).
 */
object ContentTagJsonParser {

    private val gson = Gson()
    private val listType = object : TypeToken<List<ContentTagDto>>() {}.type

    fun parse(json: String): Map<ContentRef, Set<JournalTag>> {
        val dtos: List<ContentTagDto> = gson.fromJson(json, listType) ?: emptyList()
        return dtos.associate { it.toContentRef() to it.toTags() }
    }

    /** Représentation « à plat » d'une ligne du JSON (tableaux de chaînes). */
    private data class ContentTagDto(
        val type: String = "",
        val id: String = "",
        val zones: List<String>? = null,
        val causes: List<String>? = null,
        val qualities: List<String>? = null,
        val activities: List<String>? = null,
        val flags: List<String>? = null
    ) {
        fun toContentRef() = ContentRef(ContentType.valueOf(type), id)

        fun toTags(): Set<JournalTag> = buildSet {
            zones?.forEach { add(JournalTag.Zone(PainZone.valueOf(it))) }
            causes?.forEach { add(JournalTag.Cause(DifficultyCause.valueOf(it))) }
            qualities?.forEach { add(JournalTag.Quality(DayQuality.valueOf(it))) }
            activities?.forEach { add(JournalTag.Activity(PhysicalActivity.valueOf(it))) }
            flags?.forEach { flag ->
                when (flag) {
                    "DIGESTIF" -> add(JournalTag.Digestif)
                    "EMOTIONNELLEMENT_DIFFICILE" -> add(JournalTag.EmotionallementDifficile)
                    "SOS" -> add(JournalTag.Sos)
                }
            }
        }
    }
}
