package com.audreyRetournayDiet.femSante.domain.calendar

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity
import com.audreyRetournayDiet.femSante.viewModels.EntryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText

class ContextFragment : Fragment(R.layout.fragment_context) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroupActivity = view.findViewById<ChipGroup>(R.id.chipGroupActivity)
        val switchMedication = view.findViewById<MaterialSwitch>(R.id.switchMedication)
        val etMedicationList = view.findViewById<TextInputEditText>(R.id.etMedicationList)
        val etDietNotes = view.findViewById<TextInputEditText>(R.id.etDietNotes)

        // 1. Génération des Chips pour PhysicalActivity (Enum)
        PhysicalActivity.entries.forEach { activity ->
            val chip = Chip(requireContext()).apply {
                text = activity.name
                isCheckable = true
                id = View.generateViewId()
                tag = activity
            }
            chipGroupActivity.addView(chip)
        }

        // --- Fonctions de mise à jour ---

        fun updateViewModel() {
            val selectedChipId = chipGroupActivity.checkedChipId
            val selectedActivity = chipGroupActivity.findViewById<Chip>(selectedChipId)?.tag as? PhysicalActivity
                ?: PhysicalActivity.REPOS // Valeur par défaut si besoin

            // Transformation de la String en List<String> pour le ViewModel
            val medications = etMedicationList.text?.toString()
                ?.split(",", "\n")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() } ?: emptyList()

            viewModel.updateContextState(
                activity = selectedActivity,
                medicine = switchMedication.isChecked,
                medications = medications,
                diet = etDietNotes.text?.toString()
            )
        }

        // --- Listeners ---

        chipGroupActivity.setOnCheckedStateChangeListener { _, _ -> updateViewModel() }

        switchMedication.setOnCheckedChangeListener { _, _ -> updateViewModel() }

        etMedicationList.addTextChangedListener { updateViewModel() }

        etDietNotes.addTextChangedListener { updateViewModel() }
    }
}