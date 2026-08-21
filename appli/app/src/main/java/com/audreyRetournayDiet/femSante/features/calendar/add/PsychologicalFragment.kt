package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isEmpty
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.viewmodels.calendar.EntryViewModel
import com.audreyRetournayDiet.femSante.shared.addTagChips
import com.audreyRetournayDiet.femSante.shared.checkChipByTag
import com.audreyRetournayDiet.femSante.shared.checkChipsByTags
import com.audreyRetournayDiet.femSante.shared.selectedTag
import com.audreyRetournayDiet.femSante.shared.selectedTags
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import timber.log.Timber
import java.util.Locale

/**
 * Fragment gérant l'état psychologique et émotionnel de l'utilisatrice.
 *
 * Ce composant permet de :
 * - Évaluer la qualité globale de la journée ([DayQuality]).
 * - Identifier les causes potentielles de difficultés ([DifficultyCause]) via une sélection multiple.
 * - Saisir des précisions textuelles si la cause "AUTRE" est sélectionnée.
 *
 * Les options sont générées dynamiquement à partir des Enums pour garantir la cohérence
 * avec la base de données Room.
 */
@AndroidEntryPoint
class PsychologicalFragment : Fragment(R.layout.fragment_psychological_state) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated : Initialisation de l'état psychologique")

        val chipGroupQuality = view.findViewById<ChipGroup>(R.id.chipGroupDayQuality)
        val chipGroupCauses = view.findViewById<ChipGroup>(R.id.chipGroupDifficultyCauses)
        val layoutAutres = view.findViewById<View>(R.id.layoutAutres)
        val etAutres = view.findViewById<TextInputEditText>(R.id.etAutresPsychological)
        val switchTired = view.findViewById<MaterialSwitch>(R.id.switchTired)
        val btnBedTime = view.findViewById<MaterialButton>(R.id.btnBedTime)
        val btnWakeTime = view.findViewById<MaterialButton>(R.id.btnWakeTime)
        val tvSleepDuration = view.findViewById<TextView>(R.id.tvSleepDuration)
        val etGratitude = view.findViewById<TextInputEditText>(R.id.etGratitude)

        setupFatigue(switchTired)
        setupSleep(btnBedTime, btnWakeTime, tvSleepDuration)
        setupGratitude(etGratitude)

        // Génération dynamique des options pour éviter la maintenance manuelle du XML
        if (chipGroupQuality.isEmpty()) {
            Timber.d("Génération des Chips pour Qualité et Causes")
            chipGroupQuality.addTagChips(DayQuality.entries)
            chipGroupCauses.addTagChips(DifficultyCause.entries)
        }

        observePsychologicalState(chipGroupQuality, chipGroupCauses, layoutAutres, etAutres)
        setupInteractionListeners(chipGroupQuality, chipGroupCauses, etAutres)
    }

    /**
     * Observe le StateFlow du ViewModel pour synchroniser l'interface utilisateur.
     * Gère la sélection des chips, la visibilité du champ "Autre" et le contenu textuel.
     */
    private fun observePsychologicalState(
        groupQuality: ChipGroup,
        groupCauses: ChipGroup,
        layoutAutres: View,
        etAutres: TextInputEditText
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.psychologicalState.collect { state ->
                    Timber.v("Sync UI : Qualité=${state.dayQuality}, Nb Causes=${state.difficultyCauses.size}")

                    groupQuality.checkChipByTag(state.dayQuality)
                    groupCauses.checkChipsByTags(state.difficultyCauses)

                    val hasOther = state.difficultyCauses.contains(DifficultyCause.AUTRE)
                    if (layoutAutres.isVisible != hasOther) {
                        layoutAutres.isVisible = hasOther
                    }

                    // Mise à jour du texte (Anti-boucle pour préserver le curseur)
                    if (etAutres.text?.toString() != state.autres) {
                        etAutres.setText(state.autres)
                    }
                }
            }
        }
    }

    /**
     * Configure les listeners de changement d'état pour les composants de saisie.
     * Transmet les modifications au ViewModel via une fonction de mise à jour locale.
     */
    private fun setupInteractionListeners(
        groupQuality: ChipGroup,
        groupCauses: ChipGroup,
        etAutres: TextInputEditText
    ) {
        fun pushUpdate() {
            val quality = groupQuality.selectedTag<DayQuality>() ?: DayQuality.MOYENNE
            val causes = groupCauses.selectedTags<DifficultyCause>()
            val notes = etAutres.text?.toString()

            Timber.d("Action : pushUpdate -> Qualité: $quality, Causes: $causes")
            viewModel.updatePsychologicalState(quality, causes, notes)
        }

        groupQuality.setOnCheckedStateChangeListener { _, _ -> pushUpdate() }
        groupCauses.setOnCheckedStateChangeListener { _, _ -> pushUpdate() }

        // On n'écoute la saisie que si l'utilisateur a le focus (évite les rebonds VM->UI)
        etAutres.addTextChangedListener {
            if (etAutres.hasFocus()) {
                pushUpdate()
            }
        }
    }

    /**
     * Fatigue du jour : observe l'état général et ne remonte que la fatigue au ViewModel.
     */
    private fun setupFatigue(switchTired: MaterialSwitch) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.generalState.collect { state ->
                    if (switchTired.isChecked != state.isTired) switchTired.isChecked = state.isTired
                }
            }
        }
        switchTired.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) viewModel.updateTired(isChecked)
        }
    }

    /**
     * Sommeil : deux sélecteurs d'heure (coucher / réveil) via [MaterialTimePicker], et la
     * durée dérivée (gère le passage de minuit). Les heures sont stockées en minutes.
     */
    private fun setupSleep(btnBed: MaterialButton, btnWake: MaterialButton, tvDuration: TextView) {
        fun render(bed: Int?, wake: Int?) {
            btnBed.text = if (bed != null) getString(R.string.sleep_bedtime_value, formatTime(bed))
            else getString(R.string.sleep_bedtime)
            btnWake.text = if (wake != null) getString(R.string.sleep_waketime_value, formatTime(wake))
            else getString(R.string.sleep_waketime)
            tvDuration.text = if (bed != null && wake != null)
                getString(R.string.sleep_duration, durationText(bed, wake))
            else getString(R.string.sleep_duration_empty)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.generalState.collect { render(it.bedTimeMinutes, it.wakeTimeMinutes) }
            }
        }

        btnBed.setOnClickListener {
            showTimePicker(viewModel.generalState.value.bedTimeMinutes ?: (23 * 60), R.string.sleep_bedtime_title, "bedtime") { m ->
                viewModel.updateSleep(m, viewModel.generalState.value.wakeTimeMinutes)
            }
        }
        btnWake.setOnClickListener {
            showTimePicker(viewModel.generalState.value.wakeTimeMinutes ?: (7 * 60), R.string.sleep_waketime_title, "waketime") { m ->
                viewModel.updateSleep(viewModel.generalState.value.bedTimeMinutes, m)
            }
        }
    }

    private fun showTimePicker(initialMinutes: Int, titleRes: Int, tag: String, onPicked: (Int) -> Unit) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(initialMinutes / 60)
            .setMinute(initialMinutes % 60)
            .setTitleText(getString(titleRes))
            .build()
        picker.addOnPositiveButtonClickListener { onPicked(picker.hour * 60 + picker.minute) }
        picker.show(childFragmentManager, tag)
    }

    /** Journal de gratitude : le positif du jour (préchargé en édition). */
    private fun setupGratitude(etGratitude: TextInputEditText) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.psychologicalState.collect { state ->
                    if (etGratitude.text?.toString() != state.gratitude) etGratitude.setText(state.gratitude)
                }
            }
        }
        etGratitude.addTextChangedListener {
            if (etGratitude.hasFocus()) viewModel.updateGratitude(etGratitude.text?.toString())
        }
    }

    private fun formatTime(minutes: Int): String =
        String.format(Locale.FRANCE, "%02d:%02d", minutes / 60, minutes % 60)

    private fun durationText(bed: Int, wake: Int): String {
        var minutes = wake - bed
        if (minutes <= 0) minutes += 24 * 60
        return "${minutes / 60} h ${String.format(Locale.FRANCE, "%02d", minutes % 60)}"
    }

}