package com.audreyRetournayDiet.femSante.shared

import androidx.annotation.ColorRes
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.media.MediaCategory

/**
 * Habillage visuel d'une catégorie de contenu bien-être : chaque thème a sa **teinte douce** de
 * header, tirée de la charte (pastels du calendrier + éclaircis des couleurs de marque).
 *
 * Mapping volontairement **exhaustif** (`when` sans `else`) : ajouter une catégorie sans lui
 * donner de couleur ne compilera pas. Garde l'enum [MediaCategory] libre de toute ressource.
 */
object MediaCategoryUi {

    @ColorRes
    fun headerColor(category: MediaCategory): Int = when (category) {
        // « Bien dans ta tête »
        MediaCategory.ART_THERAPIE -> R.color.day_medium
        MediaCategory.SOPHROLOGIE -> R.color.day_good
        MediaCategory.MEDITATION -> R.color.blue_soft
        MediaCategory.HYPNOSE -> R.color.yellow_soft
        MediaCategory.EMOTIONS -> R.color.day_bad
        // « Bien dans ton corps »
        MediaCategory.YOGA -> R.color.day_good
        MediaCategory.PILATES -> R.color.blue_soft
        MediaCategory.FITNESS -> R.color.day_medium
    }
}
