package com.audreyRetournayDiet.femSante.data.media

/** Nature d'un contenu de bien-être : à écouter ou à regarder. */
enum class MediaType { VIDEO, AUDIO }

/** Thème d'un contenu (sert de chips de filtre). L'ordre de déclaration = ordre d'affichage. */
enum class MediaCategory(val label: String) {
    // Module « Bien dans ta tête »
    ART_THERAPIE("Art-thérapie"),
    SOPHROLOGIE("Sophrologie"),
    MEDITATION("Méditation"),
    HYPNOSE("Hypnose"),
    EMOTIONS("Émotions"),
    // Module « Bien dans ton corps »
    YOGA("Yoga"),
    PILATES("Pilates"),
    FITNESS("Fitness")
}

/**
 * Un contenu de bien-être (vidéo ou audio).
 *
 * [title] est à la fois le libellé affiché **et** la clé de résolution du flux côté lecteur
 * (l'API renvoie l'URL de streaming à partir de ce titre) — il doit donc rester identique aux
 * noms attendus par `VideoActivity` / `AudioActivity`.
 *
 * [pdf] : la vidéo est accompagnée d'un support PDF téléchargeable (cas des vidéos d'art-thérapie).
 * Transmis au lecteur via le flag « oui »/« non » attendu par `videoLaunch`.
 */
data class MediaItem(
    val title: String,
    val type: MediaType,
    val category: MediaCategory,
    val premium: Boolean,
    val pdf: Boolean = false
)
