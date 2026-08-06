package com.audreyRetournayDiet.femSante.room.converter

import com.audreyRetournayDiet.femSante.room.type.PainZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests du convertisseur douleur par zone (sérialisation Map<PainZone,Int> <-> texte).
 */
class PainZoneLevelConverterTest {

    private val converter = PainZoneLevelConverter()

    @Test
    fun `map vide donne chaine vide et inversement`() {
        assertEquals("", converter.fromPainByZone(emptyMap()))
        assertTrue(converter.toPainByZone("").isEmpty())
    }

    @Test
    fun `aller-retour preserve les zones et intensites`() {
        val map = mapOf(PainZone.BASSIN to 7, PainZone.TETE to 3, PainZone.HAUT_DOS to 10)
        val restored = converter.toPainByZone(converter.fromPainByZone(map))
        assertEquals(map, restored)
    }

    @Test
    fun `lecture tolerante ignore les entrees malformees`() {
        // zone inconnue, intensité non numérique, hors bornes, format cassé
        val restored = converter.toPainByZone("BASSIN:7,INCONNUE:5,TETE:abc,CUISSES:0,CUISSES:12,BROKEN")
        assertEquals(mapOf(PainZone.BASSIN to 7), restored)
    }
}
