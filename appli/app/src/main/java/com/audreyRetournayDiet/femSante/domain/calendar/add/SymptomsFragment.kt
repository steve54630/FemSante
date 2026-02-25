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
import com.audreyRetournayDiet.femSante.room.type.PainZone
import com.audreyRetournayDiet.femSante.viewModels.EntryViewModel
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SymptomsFragment : Fragment(R.layout.fragment_symptom) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroupPainZones = view.findViewById<ChipGroup>(R.id.chipGroupPainZones)
        val switchNausea = view.findViewById<MaterialSwitch>(R.id.switchNausea)
        val etOthers = view.findViewById<TextInputEditText>(R.id.etOthersSymptoms)

        // --- 1. OBSERVATION (VM -> UI) ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.symptomState.collect { state ->
                    // Synchronisation des Chips (Multiple Selection)
                    // On décoche tout avant de recocher les zones présentes dans l'état
                    chipGroupPainZones.clearCheck()
                    state.localizedPains.forEach { zone ->
                        val chipId = when (zone) {
                            PainZone.BASSIN -> R.id.chipPelvis
                            PainZone.LOMBAIRES -> R.id.chipLowerBack
                            PainZone.SEINS -> R.id.chipBreasts
                            PainZone.TETE -> R.id.chipHead
                            PainZone.AUTRE -> -1 // Gérer si nécessaire
                        }
                        if (chipId != -1) chipGroupPainZones.check(chipId)
                    }

                    // Synchronisation Switch
                    if (switchNausea.isChecked != state.hasNausea) {
                        switchNausea.isChecked = state.hasNausea
                    }

                    // Synchronisation EditText (Protection du curseur)
                    if (etOthers.text?.toString() != state.others) {
                        etOthers.setText(state.others)
                    }
                }
            }
        }

        // --- 2. ACTIONS (UI -> VM) ---

        fun syncWithViewModel() {
            val selectedEnums = chipGroupPainZones.checkedChipIds.mapNotNull { id ->
                when (id) {
                    R.id.chipPelvis -> PainZone.BASSIN
                    R.id.chipLowerBack -> PainZone.LOMBAIRES
                    R.id.chipBreasts -> PainZone.SEINS
                    R.id.chipHead -> PainZone.TETE
                    else -> null
                }
            }

            viewModel.updateSymptomState(
                pains = selectedEnums,
                nausea = switchNausea.isChecked,
                notes = etOthers.text?.toString()
            )
        }

        chipGroupPainZones.setOnCheckedStateChangeListener { _, _ ->
            // On vérifie le focus ou l'action pour éviter les boucles infinies
            syncWithViewModel()
        }

        switchNausea.setOnCheckedChangeListener { buttonView, _ ->
            if (buttonView.isPressed) syncWithViewModel()
        }

        etOthers.addTextChangedListener {
            if (etOthers.hasFocus()) syncWithViewModel()
        }
    }
}