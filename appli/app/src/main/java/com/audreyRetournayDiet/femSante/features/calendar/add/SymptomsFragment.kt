package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.PainZone
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SymptomsFragment : Fragment(R.layout.fragment_symptom) {

    private val tag = "FRAG_SYMPTOMS"
    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated : Initialisation des symptômes localisés")

        val chipGroupPainZones = view.findViewById<ChipGroup>(R.id.chipGroupPainZones)
        val switchNausea = view.findViewById<MaterialSwitch>(R.id.switchNausea)
        val etOthers = view.findViewById<TextInputEditText>(R.id.etOthersSymptoms)

        // --- 1. OBSERVATION (VM -> UI) ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.symptomState.collect { state ->
                    Log.v(tag, "Sync UI : Nausée=${state.hasNausea}, Zones=${state.localizedPains}")

                    // Synchronisation des Chips (Multiple Selection)
                    // On décoche tout avant de recocher pour garantir la cohérence
                    chipGroupPainZones.clearCheck()
                    state.localizedPains.forEach { zone ->
                        val chipId = when (zone) {
                            PainZone.BASSIN -> R.id.chipPelvis
                            PainZone.LOMBAIRES -> R.id.chipLowerBack
                            PainZone.SEINS -> R.id.chipBreasts
                            PainZone.TETE -> R.id.chipHead
                            PainZone.AUTRE -> -1
                        }
                        if (chipId != -1) {
                            val chip = view.findViewById<Chip>(chipId)
                            if (chip != null && !chip.isChecked) {
                                chip.isChecked = true
                            }
                        }
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
                    else -> {
                        Log.w(tag, "Action : Chip ID inconnu détecté ($id)")
                        null
                    }
                }
            }

            Log.d(tag, "Action : Mise à jour Symptômes -> Zones: $selectedEnums, Nausée: ${switchNausea.isChecked}")

            viewModel.updateSymptomState(
                pains = selectedEnums,
                nausea = switchNausea.isChecked,
                notes = etOthers.text?.toString()
            )
        }

        chipGroupPainZones.setOnCheckedStateChangeListener { _, _ ->
            Log.v(tag, "UI Change : Sélection des zones de douleur modifiée")
            syncWithViewModel()
        }

        switchNausea.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                Log.v(tag, "UI Change : Switch nausée basculé à $isChecked")
                syncWithViewModel()
            }
        }

        etOthers.addTextChangedListener {
            if (etOthers.hasFocus()) {
                Log.v(tag, "UI Change : Saisie autres symptômes")
                syncWithViewModel()
            }
        }
    }
}