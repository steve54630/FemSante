package com.audreyRetournayDiet.femSante.data.media

import com.audreyRetournayDiet.femSante.data.media.MediaCategory.ART_THERAPIE
import com.audreyRetournayDiet.femSante.data.media.MediaCategory.EMOTIONS
import com.audreyRetournayDiet.femSante.data.media.MediaCategory.FITNESS
import com.audreyRetournayDiet.femSante.data.media.MediaCategory.HYPNOSE
import com.audreyRetournayDiet.femSante.data.media.MediaCategory.MEDITATION
import com.audreyRetournayDiet.femSante.data.media.MediaCategory.PILATES
import com.audreyRetournayDiet.femSante.data.media.MediaCategory.SOPHROLOGIE
import com.audreyRetournayDiet.femSante.data.media.MediaCategory.YOGA
import com.audreyRetournayDiet.femSante.data.media.MediaType.AUDIO
import com.audreyRetournayDiet.femSante.data.media.MediaType.VIDEO

/**
 * Catalogue des contenus de bien-être, centralisé (auparavant codé en dur dans chaque écran).
 *
 * Les titres sont les noms réels attendus par les lecteurs ; les indicateurs `premium` sont
 * repris du tableau de tagging validé par la diététicienne.
 */
object MediaCatalog {

    /** Contenus du module « Bien dans ta tête ». */
    val tete: List<MediaItem> = listOf(
        // --- Vidéos ---
        MediaItem("Joie", VIDEO, ART_THERAPIE, premium = false, pdf = true),
        MediaItem("Tristesse", VIDEO, ART_THERAPIE, premium = true, pdf = true),
        MediaItem("Colère", VIDEO, ART_THERAPIE, premium = false, pdf = true),
        MediaItem("Peur", VIDEO, ART_THERAPIE, premium = true, pdf = true),
        MediaItem("Pompage des épaules", VIDEO, SOPHROLOGIE, premium = true),
        MediaItem("Exercice du miroir", VIDEO, SOPHROLOGIE, premium = true),
        MediaItem("Eventails", VIDEO, SOPHROLOGIE, premium = true),
        MediaItem("Soufflet thoracique", VIDEO, SOPHROLOGIE, premium = false),
        MediaItem("Intelligence émotionnelle", VIDEO, EMOTIONS, premium = true),
        // --- Audios ---
        MediaItem("Base vivantielle", AUDIO, SOPHROLOGIE, premium = false),
        MediaItem("Déplacement du négatif", AUDIO, SOPHROLOGIE, premium = true),
        MediaItem("Calmer la colère", AUDIO, MEDITATION, premium = true),
        MediaItem("Calmer la douleur", AUDIO, MEDITATION, premium = true),
        MediaItem("Confiance en soi", AUDIO, MEDITATION, premium = true),
        MediaItem("Relaxation", AUDIO, MEDITATION, premium = false),
        MediaItem("Auto hypnose pour le stress", AUDIO, HYPNOSE, premium = false),
        MediaItem("Auto-hypnose pour l'apaisement", AUDIO, HYPNOSE, premium = true)
    )

    /**
     * Contenus du module « Bien dans ton corps » (vidéos uniquement).
     *
     * Les titres sont les clés réelles attendues par le lecteur (`videoLaunch`). « Débutant au
     * Yoga » est le contenu gratuit d'appel ; les autres séances sont premium (réglage à
     * confirmer avec la diététicienne).
     */
    val corps: List<MediaItem> = listOf(
        // --- Yoga ---
        MediaItem("Débutant au Yoga", VIDEO, YOGA, premium = false),
        MediaItem("SOS Douleurs", VIDEO, YOGA, premium = true),
        MediaItem("Calme intérieur", VIDEO, YOGA, premium = true),
        // --- Pilates / Fitness ---
        MediaItem("Pilates", VIDEO, PILATES, premium = true),
        MediaItem("Fitness", VIDEO, FITNESS, premium = true)
    )
}
