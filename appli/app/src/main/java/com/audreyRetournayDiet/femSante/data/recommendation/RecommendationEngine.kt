package com.audreyRetournayDiet.femSante.data.recommendation

import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.type.PainZone

/** Seuil de douleur à partir duquel on bascule en mode SOS (décision diététicienne). */
private const val SOS_PAIN_THRESHOLD = 6

/** Ordre de priorité en cas d'égalité de pertinence (décision diététicienne). */
private val TYPE_PRIORITY = mapOf(
    ContentType.AUDIO to 0,
    ContentType.VIDEO to 1,
    ContentType.PDF to 2,
    ContentType.TOOLBOX to 3
)

/**
 * Résultat de la recommandation pour un contenu donné.
 * @param matchingTagCount score de pertinence pondéré : tags communs (hors zones) + zones
 * ciblées où l'utilisatrice a mal, pondérées par l'intensité.
 */
data class Recommendation(val content: ContentRef, val matchingTagCount: Int)

/**
 * Calcule les recommandations de contenu pour la journée, à partir de la dernière
 * entrée connue du journal (ou `null` si aucune saisie n'existe encore).
 *
 * Règles validées avec la diététicienne :
 * - Pas de saisie du jour -> tout le contenu reste affiché, sans filtrage ni tri
 *   (le journal n'est pas un péage).
 * - `painLevel` >= [SOS_PAIN_THRESHOLD] -> mode SOS : seul le contenu tagué
 *   [JournalTag.Sos] est proposé.
 * - Sinon -> logique OU (un seul tag commun suffit), trié par **score de pertinence**
 *   décroissant : tags communs (hors zones) + zones ciblées où l'utilisatrice a mal,
 *   pondérées par l'intensité (léger 1 / modéré 2 / fort 3). Départage par type de
 *   contenu (Audio > Vidéo > PDF) en cas d'égalité.
 */
object RecommendationEngine {

    fun recommend(
        latestEntry: DailyEntryFull?,
        catalog: Map<ContentRef, Set<JournalTag>>
    ): List<Recommendation> {
        if (latestEntry == null) {
            // Aucune saisie : on affiche tout, sans note de pertinence ni tri imposé.
            return catalog.keys.map { Recommendation(it, matchingTagCount = 0) }
        }

        val painLevel = latestEntry.generalState?.painLevel ?: 0
        val entryTags = latestEntry.toJournalTags()
        val entryNonZoneTags = entryTags.filterNot { it is JournalTag.Zone }.toSet()

        // Zones douloureuses + leur intensité (nouveau système de douleur par zone).
        val painByZone = latestEntry.symptomsState?.painByZone ?: emptyMap()
        val painfulZones = painByZone.keys + (latestEntry.symptomsState?.localizedPains ?: emptyList())

        val isSosMode = painLevel >= SOS_PAIN_THRESHOLD
        val candidates = if (isSosMode) {
            catalog.filterValues { tags -> JournalTag.Sos in tags }
        } else {
            catalog
        }

        return candidates
            .map { (content, tags) -> Recommendation(content, score(tags, entryNonZoneTags, painfulZones, painByZone)) }
            .filter { isSosMode || it.matchingTagCount > 0 || entryTags.isEmpty() }
            .sortedWith(
                compareByDescending<Recommendation> { it.matchingTagCount }
                    .thenBy { TYPE_PRIORITY[it.content.type] ?: Int.MAX_VALUE }
            )
    }

    /**
     * Score de pertinence : nombre de tags communs (hors zones) + zones ciblées où
     * l'utilisatrice a mal, **pondérées par l'intensité** (léger 1–3 = 1, modéré 4–6 = 2,
     * fort 7–10 = 3). Une douleur forte sur une zone fait donc remonter le contenu qui la cible.
     */
    private fun score(
        contentTags: Set<JournalTag>,
        entryNonZoneTags: Set<JournalTag>,
        painfulZones: Set<PainZone>,
        painByZone: Map<PainZone, Int>
    ): Int {
        val nonZoneMatches = contentTags.count { it !is JournalTag.Zone && it in entryNonZoneTags }
        val zoneScore = contentTags.filterIsInstance<JournalTag.Zone>()
            .filter { it.zone in painfulZones }
            .sumOf { zoneWeight(painByZone[it.zone]) }
        return nonZoneMatches + zoneScore
    }

    /** Poids d'une zone selon son intensité (null = ancienne saisie sans intensité → 1). */
    private fun zoneWeight(intensity: Int?): Int = when {
        intensity == null -> 1
        intensity >= 7 -> 3
        intensity >= 4 -> 2
        else -> 1
    }
}

/** Traduit l'état du jour en tags journal, pour le calcul d'intersection. */
fun DailyEntryFull.toJournalTags(): Set<JournalTag> {
    val tags = mutableSetOf<JournalTag>()

    symptomsState?.let { symptom ->
        // Zones douloureuses : ancienne saisie binaire ([localizedPains]) + nouvelle saisie
        // par intensité (clés de [painByZone]). L'ensemble déduplique naturellement.
        symptom.localizedPains.forEach { tags += JournalTag.Zone(it) }
        symptom.painByZone.keys.forEach { tags += JournalTag.Zone(it) }
        if (symptom.hasNausea) tags += JournalTag.Digestif
    }

    psychologicalState?.let { psy ->
        tags += JournalTag.Quality(psy.dayQuality)
        psy.difficultyCauses.forEach { tags += JournalTag.Cause(it) }
        if (psy.isEmotionallyDifficultDay) tags += JournalTag.EmotionallementDifficile
    }

    contextState?.let { context ->
        context.physicalActivity?.let { tags += JournalTag.Activity(it) }
    }

    return tags
}
