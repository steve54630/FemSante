package com.audreyRetournayDiet.femSante.domain.calendar.add

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.viewModels.EntryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import androidx.core.view.isEmpty

@Suppress("UNCHECKED_CAST")
class PsychologicalFragment : Fragment(R.layout.fragment_psychological_state) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroupQuality = view.findViewById<ChipGroup>(R.id.chipGroupDayQuality)
        val chipGroupCauses = view.findViewById<ChipGroup>(R.id.chipGroupDifficultyCauses)
        val layoutAutres = view.findViewById<View>(R.id.layoutAutres) // Le TextInputLayout
        val etAutres = view.findViewById<TextInputEditText>(R.id.etAutresPsychological)

        // 1. Initialisation
        if (chipGroupQuality.isEmpty()) {
            setupChips(chipGroupQuality, DayQuality.entries)
            setupChips(chipGroupCauses, DifficultyCause.entries)
        }

        // --- 2. OBSERVATION (VM -> UI) ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.psychologicalState.collect { state ->
                    // Qualité (Single Selection)
                    selectChipByTag(chipGroupQuality, state.dayQuality)

                    // Causes (Multi Selection)
                    selectChipsByTags(chipGroupCauses, state.difficultyCauses)

                    // Visibilité du champ "Autre"
                    val hasOther = state.difficultyCauses.contains(DifficultyCause.AUTRE)
                    layoutAutres.visibility = if (hasOther) View.VISIBLE else View.GONE

                    // Texte "Autres"
                    if (etAutres.text?.toString() != state.autres) {
                        etAutres.setText(state.autres)
                    }
                }
            }
        }

        // --- 3. ACTIONS (UI -> VM) ---

        fun pushUpdate() {
            val quality = getSelectedTag<DayQuality>(chipGroupQuality) ?: DayQuality.MOYENNE
            val causes = getSelectedTags<DifficultyCause>(chipGroupCauses)

            // Logique métier : si "AUTRE" n'est pas sélectionné, on envoie null pour le texte
            val notes = if (causes.contains(DifficultyCause.AUTRE)) {
                etAutres.text?.toString()
            } else {
                null
            }

            viewModel.updatePsychologicalState(quality, causes, notes)
        }

        chipGroupQuality.setOnCheckedStateChangeListener { _, _ -> pushUpdate() }

        chipGroupCauses.setOnCheckedStateChangeListener { _, _ ->
            pushUpdate()
        }

        etAutres.addTextChangedListener {
            if (etAutres.hasFocus()) pushUpdate()
        }
    }

    // --- Helpers Utilitaires adaptés ---

    private fun <T : Enum<T>> setupChips(group: ChipGroup, entries: List<T>) {
        entries.forEach { entry ->
            val chip = Chip(requireContext()).apply {
                text = entry.name
                isCheckable = true
                tag = entry
                id = View.generateViewId()
            }
            group.addView(chip)
        }
    }

    // Pour la Single Selection (Qualité)
    private fun selectChipByTag(group: ChipGroup, tagToSelect: Any?) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            if (chip.tag == tagToSelect) {
                if (!chip.isChecked) chip.isChecked = true
                return
            }
        }
    }

    // Pour la Multi Selection (Causes)
    private fun selectChipsByTags(group: ChipGroup, tagsToSelect: List<DifficultyCause>) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            val shouldBeChecked = tagsToSelect.contains(chip.tag)
            if (chip.isChecked != shouldBeChecked) {
                chip.isChecked = shouldBeChecked
            }
        }
    }

    private fun <T> getSelectedTag(group: ChipGroup): T? {
        val selectedId = group.checkedChipId
        return group.findViewById<Chip>(selectedId)?.tag as T?
    }

    private fun <T> getSelectedTags(group: ChipGroup): List<T> {
        return group.checkedChipIds.mapNotNull { id ->
            group.findViewById<Chip>(id)?.tag as? T
        }
    }
}