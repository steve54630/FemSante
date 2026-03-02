package com.audreyRetournayDiet.femSante.features.calendar.view

import android.view.View
import android.widget.TextView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import timber.log.Timber
import java.util.Locale

/**
 * Objet utilitaire pour le formatage et l'affichage des données du calendrier.
 * * Il centralise la logique de transformation des Enums et des listes d'entités
 * en chaînes de caractères lisibles pour l'utilisatrice dans l'interface "Détail du jour".
 */
object CalendarUtils {

    /**
     * Remplit les composants d'une vue avec les données d'une entrée quotidienne complète.
     * * @param v La vue racine (généralement le contenu du BottomSheet).
     * @param entry L'objet [DailyEntryFull] contenant toutes les relations (Symptômes, Psychologie, etc.).
     */
    fun updateDailyView(v: View, entry: DailyEntryFull) {
        Timber.d("Mise à jour de la vue quotidienne pour l'entrée ID: ${entry.dailyEntry.id}")

        try {
            with(entry) {
                // --- État Général ---
                updateText(v, R.id.tvIsTired, "Fatigue : ${if (generalState?.isTired == true) "Fatiguée" else "En forme"}")
                updateText(v, R.id.tvPainLevel, "Douleur : ${generalState?.painLevel ?: 0}/10")

                // --- État Psychologique ---
                val quality = psychologicalState?.dayQuality?.format() ?: "Non renseignée"
                updateText(v, R.id.tvDayQuality, "Qualité du jour : $quality")
                updateText(v, R.id.tvDifficultyCauses, "Causes : ${psychologicalState?.format() ?: "Aucune"}")

                // --- Symptômes ---
                val pains = symptomsState?.localizedPains?.joinToString(", ") { it.name }?.ifEmpty { "Aucune" } ?: "Aucune"
                updateText(v, R.id.tvLocalizedPains, "Zones : $pains")
                updateText(v, R.id.tvHasNausea, "Nausées : ${if (symptomsState?.hasNausea == true) "Oui" else "Non"}")

                // --- Contexte ---
                val activity = contextState?.physicalActivity?.name?.lowercase()?.capitalize() ?: "Repos"
                updateText(v, R.id.tvPhysicalActivity, "Activité : $activity")
                updateText(v, R.id.tvTookMedication, "Traitement : ${contextState?.formatMedication() ?: "Non"}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Erreur critique lors du formatage des données du jour")
        }
    }

    /**
     * Met à jour de manière sécurisée le texte d'un TextView.
     */
    private fun updateText(root: View, id: Int, text: String) {
        val textView = root.findViewById<TextView>(id)
        if (textView == null) {
            Timber.w("Composant TextView introuvable pour l'ID: $id (Vérifiez le layout XML)")
        }
        textView?.text = text
    }

    /** Formatage de la qualité du jour (ex: "MOYENNE" -> "Moyenne") */
    private fun DayQuality.format() = name.lowercase().capitalize()

    /** * Formate la liste des causes psychologiques.
     * Gère l'ajout intelligent du champ "Autre" à la fin de la liste.
     */
    private fun PsychologicalStateEntity.format(): String {
        val items = difficultyCauses.filter { it != DifficultyCause.AUTRE }.map { it.name }.toMutableList()
        if (difficultyCauses.contains(DifficultyCause.AUTRE) && !autres.isNullOrBlank()) {
            items.add(autres)
        }
        return items.joinToString(", ").ifEmpty { "Aucune" }
    }

    /** Formate l'état de la médication avec le détail si présent. */
    private fun ContextStateEntity.formatMedication(): String {
        return if (medecineTaken) {
            if (!medicationList.isNullOrBlank()) "Oui ($medicationList)" else "Oui"
        } else "Non"
    }

    /** Extension utilitaire pour mettre une majuscule à la première lettre. */
    private fun String.capitalize() = replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}