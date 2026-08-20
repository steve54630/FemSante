package com.audreyRetournayDiet.femSante.shared

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Couvre la coercition tolérante de [FlexibleBoolean] : le flag freemium `acces` doit être lu vrai
 * quelle que soit la sérialisation choisie par l'API (booléen, tinyint, chaîne), pour ne jamais
 * bloquer une abonnée payante sur une différence de type.
 */
class FlexibleBooleanTest {

    @Test
    fun `booleen natif`() {
        assertTrue(FlexibleBoolean.of(true))
        assertFalse(FlexibleBoolean.of(false))
    }

    @Test
    fun `tinyint 1 et 0`() {
        assertTrue(FlexibleBoolean.of(1))
        assertFalse(FlexibleBoolean.of(0))
    }

    @Test
    fun `chaines vraies insensibles a la casse et espaces`() {
        assertTrue(FlexibleBoolean.of("true"))
        assertTrue(FlexibleBoolean.of("TRUE"))
        assertTrue(FlexibleBoolean.of("1"))
        assertTrue(FlexibleBoolean.of(" oui "))
    }

    @Test
    fun `chaines fausses ou inconnues`() {
        assertFalse(FlexibleBoolean.of("false"))
        assertFalse(FlexibleBoolean.of("0"))
        assertFalse(FlexibleBoolean.of("n'importe quoi"))
    }

    @Test
    fun `null et types non geres retournent le defaut`() {
        assertFalse(FlexibleBoolean.of(null))
        assertTrue(FlexibleBoolean.of(null, default = true))
        assertFalse(FlexibleBoolean.of(listOf(1, 2)))
    }
}
