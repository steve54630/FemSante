package com.audreyRetournayDiet.femSante.data.media

/**
 * Filtrage pur des contenus média pour l'écran de navigation : d'abord par **type**
 * (Vidéos / Audios via le sélecteur), puis par **thème** (chips).
 */
object MediaFilter {

    /** Contenus du type donné, éventuellement restreints à un thème (`null` = tous les thèmes). */
    fun filter(items: List<MediaItem>, type: MediaType, category: MediaCategory?): List<MediaItem> =
        items.filter { it.type == type && (category == null || it.category == category) }

    /** Thèmes réellement présents pour un type donné (pour n'afficher que les chips utiles), ordonnés. */
    fun categoriesFor(items: List<MediaItem>, type: MediaType): List<MediaCategory> =
        items.filter { it.type == type }
            .map { it.category }
            .distinct()
            .sortedBy { it.ordinal }
}
