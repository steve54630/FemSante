package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

@Suppress("UNCHECKED_CAST")
class PsychologicalFragment : Fragment(R.layout.fragment_psychological_state) {

    private val tag = "FRAG_PSYCH"
    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated : Initialisation de l'état psychologique")

        val chipGroupQuality = view.findViewById<ChipGroup>(R.id.chipGroupDayQuality)
        val chipGroupCauses = view.findViewById<ChipGroup>(R.id.chipGroupDifficultyCauses)
        val layoutAutres = view.findViewById<View>(R.id.layoutAutres)
        val etAutres = view.findViewById<TextInputEditText>(R.id.etAutresPsychological)

        // 1. Initialisation dynamique des Chips
        if (chipGroupQuality.isEmpty()) {
            Log.d(tag, "Génération des Chips pour Qualité et Causes")
            setupChips(chipGroupQuality, DayQuality.entries)
            setupChips(chipGroupCauses, DifficultyCause.entries)
        }

        // --- 2. OBSERVATION (VM -> UI) ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.psychologicalState.collect { state ->
                    Log.v(tag, "Sync UI : Qualité=${state.dayQuality}, Nb Causes=${state.difficultyCauses.size}")

                    // Qualité (Single Selection)
                    selectChipByTag(chipGroupQuality, state.dayQuality)

                    // Causes (Multi Selection)
                    selectChipsByTags(chipGroupCauses, state.difficultyCauses)

                    // Visibilité du champ "Autre"
                    val hasOther = state.difficultyCauses.contains(DifficultyCause.AUTRE)
                    if (layoutAutres.isVisible != hasOther) {
                        Log.d(tag, "Visibilité 'Autre' modifiée : $hasOther")
                        layoutAutres.visibility = if (hasOther) View.VISIBLE else View.GONE
                    }

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
            val notes = etAutres.text?.toString()

            Log.d(tag, "Action : pushUpdate -> Qualité: $quality, Causes: $causes")
            viewModel.updatePsychologicalState(quality, causes, notes)
        }

        chipGroupQuality.setOnCheckedStateChangeListener { _, _ ->
            Log.v(tag, "UI Change : Qualité modifiée")
            pushUpdate()
        }

        chipGroupCauses.setOnCheckedStateChangeListener { _, _ ->
            Log.v(tag, "UI Change : Sélection des causes modifiée")
            pushUpdate()
        }

        etAutres.addTextChangedListener {
            if (etAutres.hasFocus()) {
                Log.v(tag, "UI Change : Saisie texte 'Autre'")
                pushUpdate()
            }
        }
    }

    // --- Helpers Utilitaires ---

    private fun <T : Enum<T>> setupChips(group: ChipGroup, entries: List<T>) {
        entries.forEach { entry ->
            val chip = Chip(requireContext()).apply {
                text = entry.name
                isCheckable = true
                this.tag = entry // On stocke l'objet Enum dans le tag de la vue
                id = View.generateViewId()
            }
            group.addView(chip)
        }
    }

    private fun selectChipByTag(group: ChipGroup, tagToSelect: Any?) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            if (chip.tag == tagToSelect) {
                if (!chip.isChecked) {
                    Log.v(tag, "Sync UI : Sélection Qualité -> $tagToSelect")
                    chip.isChecked = true
                }
                return
            }
        }
    }

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