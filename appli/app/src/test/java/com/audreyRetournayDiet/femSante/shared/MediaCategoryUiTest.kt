package com.audreyRetournayDiet.femSante.shared

import com.audreyRetournayDiet.femSante.data.media.MediaCategory
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Vérifie que, sur un même écran, chaque thème a une teinte de header **distincte** (sinon la
 * thématisation ne se voit pas). On teste les groupes de catégories par écran directement, sans
 * dépendre du catalogue de contenus (qui est de la donnée, pas de la logique).
 */
class MediaCategoryUiTest {

    private fun distinctColorCount(categories: List<MediaCategory>): Int =
        categories.map { MediaCategoryUi.headerColor(it) }.distinct().size

    @Test
    fun `les themes de l'ecran tete ont des teintes distinctes`() {
        val tete = listOf(
            MediaCategory.ART_THERAPIE,
            MediaCategory.SOPHROLOGIE,
            MediaCategory.MEDITATION,
            MediaCategory.HYPNOSE,
            MediaCategory.EMOTIONS
        )
        assertEquals(tete.size, distinctColorCount(tete))
    }

    @Test
    fun `les themes de l'ecran corps ont des teintes distinctes`() {
        val corps = listOf(MediaCategory.YOGA, MediaCategory.PILATES, MediaCategory.FITNESS)
        assertEquals(corps.size, distinctColorCount(corps))
    }
}
