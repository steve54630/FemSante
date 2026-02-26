package com.audreyRetournayDiet.femSante.features.calendar.view

import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import java.util.Locale

// Formatage de la qualité (Moyenne, Bonne, etc.)
fun DayQuality.format() = name.lowercase().capitalize()

// Formatage des causes psychologiques
fun PsychologicalStateEntity.format() : String {
    val items = difficultyCauses.filter { it != DifficultyCause.AUTRE }.map { it.name }.toMutableList()
    if (difficultyCauses.contains(DifficultyCause.AUTRE) && !autres.isNullOrBlank()) {
        items.add(autres)
    }
    return items.joinToString(", ").ifEmpty { "Aucune" }
}

// Formatage du traitement (String simple)
fun ContextStateEntity.formatMedication() : String {
    return if (medecineTaken) {
        if (medicationList!!.isNotBlank()) "Oui ($medicationList)" else "Oui"
    } else "Non"
}

// Helper pour capitaliser proprement sans Locale.ROOT partout
fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }