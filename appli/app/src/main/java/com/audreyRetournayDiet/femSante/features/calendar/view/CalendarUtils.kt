package com.audreyRetournayDiet.femSante.features.calendar.view

import android.view.View
import android.widget.TextView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import java.util.Locale

object CalendarUtils {

    fun updateDailyView(v: View, entry: DailyEntryFull) {
        with(entry) {
            updateText(v, R.id.tvIsTired, "Fatigue : ${if (generalState!!.isTired) "Fatiguée" else "En forme"}")
            updateText(v, R.id.tvPainLevel, "Douleur : ${generalState.painLevel}/10")
            updateText(v, R.id.tvDayQuality, "Qualité du jour : ${psychologicalState!!.dayQuality.format()}")
            updateText(v, R.id.tvDifficultyCauses, "Causes : ${psychologicalState.format()}")

            val pains = symptomsState!!.localizedPains.joinToString(", ") { it.name }.ifEmpty { "Aucune" }
            updateText(v, R.id.tvLocalizedPains, "Zones : $pains")
            updateText(v, R.id.tvHasNausea, "Nausées : ${if (symptomsState.hasNausea) "Oui" else "Non"}")
            updateText(v, R.id.tvPhysicalActivity, "Activité : ${contextState!!.physicalActivity!!.name.lowercase().capitalize()}")
            updateText(v, R.id.tvTookMedication, "Traitement : ${contextState.formatMedication()}")
        }
    }

    private fun updateText(root: View, id: Int, text: String) {
        root.findViewById<TextView>(id)?.text = text
    }

    private fun DayQuality.format() = name.lowercase().capitalize()

    private fun PsychologicalStateEntity.format(): String {
        val items = difficultyCauses.filter { it != DifficultyCause.AUTRE }.map { it.name }.toMutableList()
        if (difficultyCauses.contains(DifficultyCause.AUTRE) && !autres.isNullOrBlank()) {
            items.add(autres)
        }
        return items.joinToString(", ").ifEmpty { "Aucune" }
    }

    private fun ContextStateEntity.formatMedication(): String {
        return if (medecineTaken) {
            if (medicationList!!.isNotBlank()) "Oui ($medicationList)" else "Oui"
        } else "Non"
    }

    private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}