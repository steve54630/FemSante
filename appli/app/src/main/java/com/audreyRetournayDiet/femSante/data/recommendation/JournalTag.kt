package com.audreyRetournayDiet.femSante.data.recommendation

import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.room.type.PainZone
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity

/**
 * Tag de contenu pour le moteur de recommandation "Pour toi aujourd'hui".
 *
 * Chaque variante s'appuie directement sur les enums déjà utilisés par le journal de
 * symptômes (aucune taxonomie parallèle). Un contenu (PDF, vidéo, audio) est associé à
 * un `Set<JournalTag>` ; l'état du jour est traduit en `Set<JournalTag>` via
 * [com.audreyRetournayDiet.femSante.data.recommendation.toJournalTags] pour calculer
 * le nombre de tags en commun.
 */
sealed interface JournalTag {
    data class Zone(val zone: PainZone) : JournalTag
    data class Cause(val cause: DifficultyCause) : JournalTag
    data class Quality(val quality: DayQuality) : JournalTag
    data class Activity(val activity: PhysicalActivity) : JournalTag

    /** Symptôme digestif (lié à l'échelle de Bristol), indépendant de la douleur générale. */
    data object Digestif : JournalTag

    /**
     * Journée difficile sur le plan émotionnel/moral, distincte de [Quality] qui
     * évalue l'état physique global du jour. Reflète `isEmotionallyDifficultDay`.
     */
    data object EmotionallementDifficile : JournalTag

    /** Contenu de crise, à proposer uniquement en mode SOS (`painLevel` >= 6). */
    data object Sos : JournalTag
}
