package com.audreyRetournayDiet.femSante.features

import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxAdvice
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxCategory

/**
 * Ligne du RecyclerView de la Boîte à outils : en-tête de section (catégorie), fiche, ou lien vers
 * le lexique des plantes (rattaché à la section Phytothérapie, voir [ToolboxCategory]).
 */
sealed interface ToolboxRow {
    data class Header(val category: ToolboxCategory) : ToolboxRow
    data class Item(val advice: ToolboxAdvice) : ToolboxRow
    data object PlantLexiconLink : ToolboxRow
}
