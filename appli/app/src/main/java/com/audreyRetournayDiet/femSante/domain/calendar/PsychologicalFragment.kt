package com.audreyRetournayDiet.femSante.domain.calendar

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.viewModels.EntryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText

class PsychologicalFragment : Fragment(R.layout.fragment_psychological_state) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroupQuality = view.findViewById<ChipGroup>(R.id.chipGroupDayQuality)
        val chipGroupCauses = view.findViewById<ChipGroup>(R.id.chipGroupDifficultyCauses)
        val etAutres = view.findViewById<TextInputEditText>(R.id.etAutresPsychological)

        // 1. Génération des Chips pour DayQuality (Enum)
        DayQuality.entries.forEach { quality ->
            val chip = Chip(requireContext()).apply {
                text = quality.name
                isCheckable = true
                id = View.generateViewId()
                tag = quality
            }
            chipGroupQuality.addView(chip)
        }

        // 2. Génération des Chips pour DifficultyCause (Enum)
        DifficultyCause.entries.forEach { cause ->
            val chip = Chip(requireContext()).apply {
                text = cause.name
                isCheckable = true
                id = View.generateViewId()
                tag = cause
            }
            chipGroupCauses.addView(chip)
        }

        // --- Listeners pour mettre à jour le ViewModel ---

        chipGroupQuality.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: -1)
            val quality = selectedChip?.tag as? DayQuality ?: DayQuality.MOYENNE // Valeur par défaut

            updateState(quality, chipGroupCauses, etAutres)
        }

        chipGroupCauses.setOnCheckedStateChangeListener { group, checkedIds ->
            val selectedChip = group.findViewById<Chip>(checkedIds.firstOrNull() ?: -1)
            val cause = selectedChip?.tag as? DifficultyCause

            val selectedQualityChip = chipGroupQuality.findViewById<Chip>(chipGroupQuality.checkedChipId)
            val quality = selectedQualityChip?.tag as? DayQuality ?: DayQuality.MOYENNE

            viewModel.updatePsychologicalState(quality, cause, etAutres.text?.toString())
        }

        etAutres.addTextChangedListener {
            val selectedQualityChip = chipGroupQuality.findViewById<Chip>(chipGroupQuality.checkedChipId)
            val quality = selectedQualityChip?.tag as? DayQuality ?: DayQuality.MOYENNE

            val selectedCauseChip = chipGroupCauses.findViewById<Chip>(chipGroupCauses.checkedChipId)
            val cause = selectedCauseChip?.tag as? DifficultyCause

            viewModel.updatePsychologicalState(quality, cause, it?.toString())
        }
    }

    private fun updateState(quality: DayQuality, groupCauses: ChipGroup, etAutres: TextInputEditText) {
        val selectedCauseChip = groupCauses.findViewById<Chip>(groupCauses.checkedChipId)
        val cause = selectedCauseChip?.tag as? DifficultyCause
        viewModel.updatePsychologicalState(quality, cause, etAutres.text?.toString())
    }
}