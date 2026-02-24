package com.audreyRetournayDiet.femSante.domain.calendar

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.PainZone
import com.audreyRetournayDiet.femSante.viewModels.EntryViewModel
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText

class SymptomsFragment : Fragment(R.layout.fragment_symptom) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroupPainZones = view.findViewById<ChipGroup>(R.id.chipGroupPainZones)
        val switchNausea = view.findViewById<MaterialSwitch>(R.id.switchNausea)
        val etOthers = view.findViewById<TextInputEditText>(R.id.etOthersSymptoms)

        // Fonction locale pour synchroniser l'UI vers le ViewModel
        fun syncWithViewModel() {
            // On mappe l'ID du Chip vers la valeur Enum correspondante
            val selectedEnums = chipGroupPainZones.checkedChipIds.mapNotNull { id ->
                when (id) {
                    R.id.chipPelvis -> PainZone.BASSIN
                    R.id.chipLowerBack -> PainZone.LOMBAIRES
                    R.id.chipBreasts -> PainZone.SEINS
                    R.id.chipHead -> PainZone.TETE
                    else -> PainZone.AUTRE
                }
            }

            viewModel.updateSymptomState(
                pains = selectedEnums, // Maintenant une List<PainZone>
                nausea = switchNausea.isChecked,
                notes = etOthers.text?.toString()
            )
        }

        // Listeners
        chipGroupPainZones.setOnCheckedStateChangeListener { _, _ -> syncWithViewModel() }
        switchNausea.setOnCheckedChangeListener { _, _ -> syncWithViewModel() }
        etOthers.addTextChangedListener { syncWithViewModel() }
    }
}