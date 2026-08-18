package com.audreyRetournayDiet.femSante.shared

import com.audreyRetournayDiet.femSante.data.media.MediaCatalog
import com.audreyRetournayDiet.femSante.data.media.MediaItem
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Vérifie que, sur un même écran, chaque thème a une teinte de header **distincte** (sinon la
 * thématisation ne se voit pas). La réutilisation d'une teinte entre écrans différents (ex.
 * vert Sophro/Yoga) est volontaire et n'est pas testée.
 */
class MediaCategoryUiTest {

    private fun distinctColors(items: List<MediaItem>): Int =
        items.map { it.category }.distinct().map { MediaCategoryUi.headerColor(it) }.distinct().size

    @Test
    fun `les themes de l'ecran tete ont des teintes distinctes`() {
        val themes = MediaCatalog.tete.map { it.category }.distinct()
        assertEquals(themes.size, distinctColors(MediaCatalog.tete))
    }

    @Test
    fun `les themes de l'ecran corps ont des teintes distinctes`() {
        val themes = MediaCatalog.corps.map { it.category }.distinct()
        assertEquals(themes.size, distinctColors(MediaCatalog.corps))
    }
}
