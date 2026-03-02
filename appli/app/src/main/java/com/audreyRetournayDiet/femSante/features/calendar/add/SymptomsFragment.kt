package com.audreyRetournayDiet.femSante.features.calendar.add

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
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment gérant la saisie des symptômes physiques localisés.
 * * Ce composant permet de :
 * - Sélectionner plusieurs zones de douleur ([PainZone]) via des Chips prédéfinis.
 * - Signaler la présence de nausées via un switch.
 * - Saisir des observations complémentaires par écrit.
 * * Contrairement aux autres fragments, celui-ci utilise un mapping manuel entre les IDs
 * de ressources XML et les valeurs de l'énumération [PainZone].
 */
class SymptomsFragment : Fragment(R.layout.fragment_symptom) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated : Initialisation des symptômes localisés")

        val chipGroupPainZones = view.findViewById<ChipGroup>(R.id.chipGroupPainZones)
        val switchNausea = view.findViewById<MaterialSwitch>(R.id.switchNausea)
        val etOthers = view.findViewById<TextInputEditText>(R.id.etOthersSymptoms)

        observeSymptomState(view, chipGroupPainZones, switchNausea, etOthers)
        setupInteractionListeners(chipGroupPainZones, switchNausea, etOthers)
    }

    /**
     * Observe le StateFlow du ViewModel pour synchroniser l'UI.
     * Effectue la conversion inverse des valeurs de l'Enum vers les IDs de Chips XML.
     */
    private fun observeSymptomState(
        view: View,
        chipGroup: ChipGroup,
        switch: MaterialSwitch,
        editText: TextInputEditText
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.symptomState.collect { state ->
                    Timber.v("Sync UI : Nausée=${state.hasNausea}, Zones=${state.localizedPains}")

                    // Synchronisation des Chips (Multiple Selection)
                    // On décoche tout pour éviter les duplications avant de recocher selon l'état
                    chipGroup.clearCheck()
                    state.localizedPains.forEach { zone ->
                        val chipId = when (zone) {
                            PainZone.BASSIN -> R.id.chipPelvis
                            PainZone.LOMBAIRES -> R.id.chipLowerBack
                            PainZone.SEINS -> R.id.chipBreasts
                            PainZone.TETE -> R.id.chipHead
                            PainZone.AUTRE -> -1
                        }
                        if (chipId != -1) {
                            view.findViewById<Chip>(chipId)?.let { chip ->
                                if (!chip.isChecked) chip.isChecked = true
                            }
                        }
                    }

                    // Synchronisation du Switch
                    if (switch.isChecked != state.hasNausea) {
                        switch.isChecked = state.hasNausea
                    }

                    // Synchronisation de l'EditText (Anti-boucle pour préserver le focus et le curseur)
                    if (editText.text?.toString() != state.others) {
                        editText.setText(state.others)
                    }
                }
            }
        }
    }

    /**
     * Configure les écouteurs pour capter les actions de l'utilisatrice.
     * Convertit les IDs de Chips sélectionnés en valeurs de l'énumération [PainZone].
     */
    private fun setupInteractionListeners(
        chipGroup: ChipGroup,
        switch: MaterialSwitch,
        editText: TextInputEditText
    ) {
        /** Envoie l'état consolidé des symptômes vers le ViewModel */
        fun syncWithViewModel() {
            val selectedEnums = chipGroup.checkedChipIds.mapNotNull { id ->
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
                nausea = switch.isChecked,
                notes = editText.text?.toString()
            )
        }

        chipGroup.setOnCheckedStateChangeListener { _, _ ->
            syncWithViewModel()
        }

        switch.setOnCheckedChangeListener { buttonView, _ ->
            // On ne déclenche la mise à jour que sur une pression réelle (clic utilisateur)
            if (buttonView.isPressed) {
                syncWithViewModel()
            }
        }

        editText.addTextChangedListener {
            // Protection : n'envoie la mise à jour que si l'utilisateur est en train de saisir
            if (editText.hasFocus()) {
                syncWithViewModel()
            }
        }
    }
}