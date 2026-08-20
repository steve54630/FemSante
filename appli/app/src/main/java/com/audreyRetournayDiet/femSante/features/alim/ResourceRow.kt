package com.audreyRetournayDiet.femSante.features.alim

import com.audreyRetournayDiet.femSante.data.resource.ResourceDocument

/**
 * Ligne du RecyclerView de l'écran « Ressources » : une fiche PDF (données) ou un raccourci fixe
 * vers un module natif (destination de navigation, pas du contenu — ne vient donc pas du JSON).
 */
sealed interface ResourceRow {
    data class Document(val document: ResourceDocument) : ResourceRow
    data object MicronutrientsShortcut : ResourceRow
    data object FodmapShortcut : ResourceRow
}
