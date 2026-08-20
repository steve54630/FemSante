package com.audreyRetournayDiet.femSante.data.fodmap

import org.junit.Assert.assertEquals
import org.junit.Test

class FodmapFilterTest {

    private val foods = listOf(
        FodmapFood("1", "Carotte", FodmapCategory.LEGUMES, FodmapStatus.PAUVRE, "Libre", ""),
        FodmapFood("2", "Céleri branche", FodmapCategory.LEGUMES, FodmapStatus.RICHE, "0g", ""),
        FodmapFood("3", "Cerise", FodmapCategory.FRUITS, FodmapStatus.RICHE, "0g", "")
    )

    @Test
    fun `sans filtre on obtient tout`() {
        assertEquals(3, FodmapFilter.filter(foods, query = "", status = null).size)
    }

    @Test
    fun `filtre par statut`() {
        assertEquals(2, FodmapFilter.filter(foods, query = "", status = FodmapStatus.RICHE).size)
    }

    @Test
    fun `recherche insensible a la casse et aux accents`() {
        assertEquals(1, FodmapFilter.filter(foods, query = "celeri", status = null).size)
        assertEquals(1, FodmapFilter.filter(foods, query = "CÉLERI", status = null).size)
    }

    @Test
    fun `recherche et statut se combinent`() {
        // Seule "Cerise" (riche) contient "cer" ; "Céleri branche" (riche aussi) ne matche pas.
        assertEquals(1, FodmapFilter.filter(foods, query = "cer", status = FodmapStatus.RICHE).size)
    }
}
