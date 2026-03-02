package com.audreyRetournayDiet.femSante.features.calendar.view

import android.util.Log
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

    private const val TAG = "UTIL_CALENDAR"

    fun updateDailyView(v: View, entry: DailyEntryFull) {
        Log.d(TAG, "Mise à jour de la vue quotidienne pour l'entrée ID: ${entry.dailyEntry.id}")

        try {
            with(entry) {
                // État Général
                updateText(v, R.id.tvIsTired, "Fatigue : ${if (generalState?.isTired == true) "Fatiguée" else "En forme"}")
                updateText(v, R.id.tvPainLevel, "Douleur : ${generalState?.painLevel ?: 0}/10")

                // État Psychologique
                val quality = psychologicalState?.dayQuality?.format() ?: "Non renseignée"
                updateText(v, R.id.tvDayQuality, "Qualité du jour : $quality")
                updateText(v, R.id.tvDifficultyCauses, "Causes : ${psychologicalState?.format() ?: "Aucune"}")

                // Symptômes
                val pains = symptomsState?.localizedPains?.joinToString(", ") { it.name }?.ifEmpty { "Aucune" } ?: "Aucune"
                updateText(v, R.id.tvLocalizedPains, "Zones : $pains")
                updateText(v, R.id.tvHasNausea, "Nausées : ${if (symptomsState?.hasNausea == true) "Oui" else "Non"}")

                // Contexte
                val activity = contextState?.physicalActivity?.name?.lowercase()?.capitalize() ?: "Repos"
                updateText(v, R.id.tvPhysicalActivity, "Activité : $activity")
                updateText(v, R.id.tvTookMedication, "Traitement : ${contextState?.formatMedication() ?: "Non"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur critique lors du formatage des données du jour", e)
        }
    }

    private fun updateText(root: View, id: Int, text: String) {
        val textView = root.findViewById<TextView>(id)
        if (textView == null) {
            Log.w(TAG, "Composant TextView introuvable pour l'ID: $id (Vérifiez le layout XML)")
        }
        textView?.text = text
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
            if (!medicationList.isNullOrBlank()) "Oui ($medicationList)" else "Oui"
        } else "Non"
    }

    private fun String.capitalize() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}