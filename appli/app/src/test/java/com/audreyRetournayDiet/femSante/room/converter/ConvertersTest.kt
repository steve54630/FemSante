package com.audreyRetournayDiet.femSante.room.converter

import com.audreyRetournayDiet.femSante.room.type.BristolType
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.FlowLevel
import com.audreyRetournayDiet.femSante.room.type.PainZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests des TypeConverters Room (sérialisation aller-retour). Pure logique JVM.
 */
class ConvertersTest {

    private val painZoneConverter = PainZoneConverter()
    private val bristolConverter = BristolConverter()
    private val flowConverter = FlowConverter()
    private val qualityConverter = QualityConverter()

    @Test
    fun `PainZone aller-retour preserve toutes les zones dont ABDOMEN`() {
        val zones = listOf(PainZone.BASSIN, PainZone.ABDOMEN, PainZone.TETE)
        val csv = painZoneConverter.fromPainZoneList(zones)
        val restored = painZoneConverter.toPainZoneList(csv)
        assertEquals(zones, restored)
    }

    @Test
    fun `PainZone liste vide donne une liste vide`() {
        assertTrue(painZoneConverter.toPainZoneList("").isEmpty())
        assertEquals("", painZoneConverter.fromPainZoneList(emptyList()))
    }

    @Test
    fun `Bristol aller-retour`() {
        val value = BristolType.TYPE_4
        assertEquals(value, bristolConverter.toBristolType(bristolConverter.fromBristolType(value)))
    }

    @Test
    fun `Flow nullable aller-retour`() {
        assertEquals(FlowLevel.ABONDANT, flowConverter.toFlowLevel(flowConverter.fromFlowLevel(FlowLevel.ABONDANT)))
        assertNull(flowConverter.fromFlowLevel(null))
        assertNull(flowConverter.toFlowLevel(null))
    }

    @Test
    fun `DayQuality aller-retour`() {
        val value = DayQuality.MAUVAISE
        assertEquals(value, qualityConverter.toDayQuality(qualityConverter.fromDayQuality(value)))
    }
}
