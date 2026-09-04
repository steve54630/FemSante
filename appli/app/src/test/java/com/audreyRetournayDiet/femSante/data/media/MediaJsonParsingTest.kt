package com.audreyRetournayDiet.femSante.data.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Teste la **logique de parsing** du catalogue média sur un mini-fixture dédié
 * (`test/resources/media_sample.json`).
 *
 * On ne parse **pas** le vrai `assets/media.json` : c'est de la donnée de prod (destinée à passer
 * côté API un jour) — la coupler aux tests les rendrait fragiles. Le fixture ne valide que le
 * parseur (résolution des enums, valeurs par défaut).
 */
class MediaJsonParsingTest {

    private val media: List<MediaItem> by lazy {
        val json = javaClass.getResourceAsStream("/media_sample.json")!!
            .bufferedReader().use { it.readText() }
        MediaJsonParser.parse(json)
    }

    @Test
    fun `parse toutes les entrees du fixture`() {
        assertEquals(6, media.size)
    }

    @Test
    fun `types categories et modules resolus par nom`() {
        // Gson mettrait le champ à null si le libellé ne correspondait à aucune valeur d'enum
        // (même si le type Kotlin est déclaré non-nullable) — d'où ce garde-fou explicite.
        assertTrue(media.all { it.type != null && it.category != null && it.module != null })
        assertEquals(4, media.count { it.module == MediaModule.TETE })
        assertEquals(2, media.count { it.module == MediaModule.CORPS })
    }

    @Test
    fun `le champ pdf vaut false par defaut et true quand present`() {
        assertEquals(1, media.count { it.pdf })
        assertEquals(MediaCategory.ART_THERAPIE, media.single { it.pdf }.category)
    }
}
