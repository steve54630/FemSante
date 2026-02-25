package com.audreyRetournayDiet.femSante.domain.calendar.add

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity
import com.audreyRetournayDiet.femSante.viewModels.EntryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class ContextFragment : Fragment(R.layout.fragment_context) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroupActivity = view.findViewById<ChipGroup>(R.id.chipGroupActivity)
        val switchMedication = view.findViewById<MaterialSwitch>(R.id.switchMedication)
        val layoutMedicationList = view.findViewById<TextInputLayout>(R.id.layoutMedicationList)
        val etMedicationList = view.findViewById<TextInputEditText>(R.id.etMedicationList)
        val etDietNotes = view.findViewById<TextInputEditText>(R.id.etDietNotes)

        // 1. Initialisation des Chips (Une seule fois)
        if (chipGroupActivity.childCount == 0) {
            PhysicalActivity.entries.forEach { activity ->
                val chip = Chip(requireContext()).apply {
                    text = activity.name
                    isCheckable = true
                    tag = activity
                    id = View.generateViewId()
                }
                chipGroupActivity.addView(chip)
            }
        }

        // --- 2. OBSERVATION (VM -> UI) ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contextState.collect { state ->
                    // Synchronisation de l'activité (Chip)
                    selectChipByTag(chipGroupActivity, state.physicalActivity!!)

                    // Gestion du switch et de la visibilité du champ
                    if (switchMedication.isChecked != state.medecineTaken) {
                        switchMedication.isChecked = state.medecineTaken
                    }
                    layoutMedicationList.isVisible = state.medecineTaken

                    // Mise à jour des textes (uniquement si changement pour préserver le curseur)
                    if (etMedicationList.text?.toString() != state.medicationList) {
                        etMedicationList.setText(state.medicationList)
                    }

                    if (etDietNotes.text?.toString() != state.diet) {
                        etDietNotes.setText(state.diet)
                    }
                }
            }
        }

        // --- 3. ACTIONS (UI -> VM) ---

        fun pushUpdate() {
            val selectedChipId = chipGroupActivity.checkedChipId
            val activity = chipGroupActivity.findViewById<Chip>(selectedChipId)?.tag as? PhysicalActivity
                ?: PhysicalActivity.REPOS

            viewModel.updateContextState(
                activity = activity,
                medicine = switchMedication.isChecked,
                medications = etMedicationList.text?.toString() ?: "", // C'est une String maintenant
                diet = etDietNotes.text?.toString()
            )
        }

        chipGroupActivity.setOnCheckedStateChangeListener { _, _ -> pushUpdate() }

        switchMedication.setOnCheckedChangeListener { button, isChecked ->
            layoutMedicationList.isVisible = isChecked
            if (button.isPressed) pushUpdate()
        }

        etMedicationList.addTextChangedListener {
            if (etMedicationList.hasFocus()) pushUpdate()
        }

        etDietNotes.addTextChangedListener {
            if (etDietNotes.hasFocus()) pushUpdate()
        }
    }

    private fun selectChipByTag(group: ChipGroup, targetTag: PhysicalActivity) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            if (chip.tag == targetTag) {
                if (!chip.isChecked) chip.isChecked = true
                break
            }
        }
    }
}