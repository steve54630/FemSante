package com.audreyRetournayDiet.femSante.features.alim

import com.audreyRetournayDiet.femSante.data.fodmap.FodmapFood

/** Ligne affichée : l'aliment + son état de dépli (portion/remarques visibles ou non). */
data class FodmapRow(
    val food: FodmapFood,
    val expanded: Boolean
)
