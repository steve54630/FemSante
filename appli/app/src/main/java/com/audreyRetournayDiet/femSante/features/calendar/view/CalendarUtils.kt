package com.audreyRetournayDiet.femSante.features.calendar.view

import android.view.View
import android.widget.TextView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.BodyMeasurementEntity
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.room.type.PainZone
import timber.log.Timber
import java.util.Locale

/**
 * Formatage et remplissage du résumé du jour (écran détail). Aligné sur les onglets de
 * saisie : Corps & douleur / Moral & sommeil / Mode de vie / Mesures.
 *
 * Principe : on **n'affiche que ce qui est renseigné** — chaque ligne vide est masquée, et
 * la carte Mesures disparaît si aucune mesure n'existe.
 */
object CalendarUtils {

    fun updateDailyView(v: View, entry: DailyEntryFull) {
        try {
            with(entry) {
                setLine(v, R.id.tvPainByZone, painSummary(symptomsState))
                setLine(v, R.id.tvHasNausea, if (symptomsState?.hasNausea == true) "Nausées : oui" else null)
                setLine(v, R.id.tvOtherSymptoms, symptomsState?.others?.takeIf { it.isNotBlank() }?.let { "Notes : $it" })

                setLine(v, R.id.tvDayQuality, "Moral : ${psychologicalState?.dayQuality?.format() ?: "—"}")
                setLine(v, R.id.tvDifficultyCauses, psychologicalState?.causesOrNull()?.let { "Causes : $it" })
                setLine(v, R.id.tvIsTired, if (generalState?.isTired == true) "Fatigue : oui" else null)
                setLine(v, R.id.tvSleep, sleepSummary(generalState))
                setLine(v, R.id.tvGratitude, psychologicalState?.gratitude?.takeIf { it.isNotBlank() }?.let { "Gratitude : $it" })

                val activity = contextState?.physicalActivity?.name?.lowercase()?.capitalize() ?: "Repos"
                setLine(v, R.id.tvPhysicalActivity, "Activité : $activity")
                setLine(v, R.id.tvTookMedication, contextState?.medicationOrNull()?.let { "Traitement : $it" })
                setLine(v, R.id.tvDiet, contextState?.diet?.takeIf { it.isNotBlank() }?.let { "Alimentation : $it" })

                val measures = measuresSummary(measurement)
                setLine(v, R.id.tvMeasures, measures)
                v.findViewById<View>(R.id.cardMeasures)?.visibility = if (measures == null) View.GONE else View.VISIBLE
            }
        } catch (e: Exception) {
            Timber.e(e, "Erreur lors du formatage du résumé du jour")
        }
    }

    /** Douleur par zone avec intensité (fallback sur l'ancienne liste sans intensité). */
    private fun painSummary(symptom: com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity?): String {
        val byZone = symptom?.painByZone ?: emptyMap()
        if (byZone.isNotEmpty()) {
            val parts = byZone.entries.sortedByDescending { it.value }
                .joinToString(" · ") { "${zoneLabel(it.key)} ${it.value}" }
            return "Douleur : $parts"
        }
        val zones = symptom?.localizedPains ?: emptyList()
        if (zones.isNotEmpty()) return "Douleur : ${zones.joinToString(", ") { zoneLabel(it) }}"
        return "Douleur : aucune"
    }

    private fun sleepSummary(general: GeneralStateEntity?): String? {
        val bed = general?.bedTimeMinutes ?: return null
        val wake = general.wakeTimeMinutes ?: return null
        var minutes = wake - bed
        if (minutes <= 0) minutes += 24 * 60
        val duration = "${minutes / 60} h ${"%02d".format(minutes % 60)}"
        return "Sommeil : $duration (${formatTime(bed)} → ${formatTime(wake)})"
    }

    private fun measuresSummary(m: BodyMeasurementEntity?): String? {
        if (m == null) return null
        val parts = buildList {
            m.weightKg?.let { add("Poids ${num(it)} kg") }
            m.waistCm?.let { add("Taille ${num(it)} cm") }
            m.hipsCm?.let { add("Hanches ${num(it)} cm") }
            m.thighsCm?.let { add("Cuisses ${num(it)} cm") }
            m.chestCm?.let { add("Poitrine ${num(it)} cm") }
            m.armsCm?.let { add("Bras ${num(it)} cm") }
        }
        return parts.joinToString(" · ").ifEmpty { null }
    }

    /** Affiche la ligne, ou la masque si le texte est vide. */
    private fun setLine(root: View, id: Int, text: String?) {
        val tv = root.findViewById<TextView>(id) ?: return
        if (text.isNullOrBlank()) {
            tv.visibility = View.GONE
        } else {
            tv.visibility = View.VISIBLE
            tv.text = text
        }
    }

    private fun formatTime(minutes: Int): String =
        String.format(Locale.FRANCE, "%02d:%02d", minutes / 60, minutes % 60)

    private fun num(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()

    private fun zoneLabel(zone: PainZone): String = when (zone) {
        PainZone.BASSIN -> "Bassin"
        PainZone.LOMBAIRES -> "Lombaires"
        PainZone.SEINS -> "Seins"
        PainZone.TETE -> "Tête"
        PainZone.ABDOMEN -> "Abdomen"
        PainZone.BRAS -> "Bras"
        PainZone.CUISSES -> "Cuisses"
        PainZone.HAUT_DOS -> "Haut du dos"
        PainZone.JAMBES -> "Jambes"
        PainZone.AUTRE -> "Autre"
    }

    private fun DayQuality.format() = name.lowercase().capitalize()

    /** Causes de difficulté (avec le champ « Autre » ajouté), ou null si aucune. */
    private fun PsychologicalStateEntity.causesOrNull(): String? {
        val items = difficultyCauses.filter { it != DifficultyCause.AUTRE }.map { it.name.lowercase().capitalize() }.toMutableList()
        if (difficultyCauses.contains(DifficultyCause.AUTRE) && !autres.isNullOrBlank()) items.add(autres)
        return items.joinToString(", ").ifEmpty { null }
    }

    /** État de la médication avec détail, ou null si aucun traitement. */
    private fun ContextStateEntity.medicationOrNull(): String? {
        if (!medecineTaken) return null
        return if (!medicationList.isNullOrBlank()) "oui ($medicationList)" else "oui"
    }

    private fun String.capitalize() = replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}
