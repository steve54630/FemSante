package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.view.View
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment de saisie du contexte quotidien pour une entrée du calendrier.
 * * Permet à l'utilisatrice de renseigner :
 * - Son niveau d'activité physique (via des [Chip] dynamiques).
 * - Sa prise de médicaments (avec affichage conditionnel de la liste).
 * - Ses notes sur son alimentation.
 * * Utilise un [EntryViewModel] partagé au niveau de l'activité pour centraliser les données
 * avant la sauvegarde finale en base de données.
 */
class ContextFragment : Fragment(R.layout.fragment_context) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroupActivity = view.findViewById<ChipGroup>(R.id.chipGroupActivity)
        val switchMedication = view.findViewById<MaterialSwitch>(R.id.switchMedication)
        val layoutMedicationList = view.findViewById<TextInputLayout>(R.id.layoutMedicationList)
        val etMedicationList = view.findViewById<TextInputEditText>(R.id.etMedicationList)
        val etDietNotes = view.findViewById<TextInputEditText>(R.id.etDietNotes)

        // Génération des options d'activité physique à partir de l'Enum PhysicalActivity
        if (chipGroupActivity.isEmpty()) {
            setupPhysicalActivityChips(chipGroupActivity)
        }

        observeState(chipGroupActivity, switchMedication, layoutMedicationList, etMedicationList, etDietNotes)
        setupInputListeners(chipGroupActivity, switchMedication, etMedicationList, etDietNotes)
    }

    /**
     * Génère dynamiquement les Chips basés sur l'énumération [PhysicalActivity].
     */
    private fun setupPhysicalActivityChips(group: ChipGroup) {
        Timber.d("Initialisation : Génération des Chips d'activité")
        PhysicalActivity.entries.forEach { activity ->
            val chip = Chip(requireContext()).apply {
                text = activity.name
                isCheckable = true
                this.tag = activity
                id = View.generateViewId()
            }
            group.addView(chip)
        }
    }

    /**
     * Observe le flux d'état du ViewModel pour synchroniser l'UI.
     * Inclut des vérifications pour éviter les boucles de mise à jour infinies sur les champs texte.
     */
    private fun observeState(
        group: ChipGroup,
        switch: MaterialSwitch,
        layout: TextInputLayout,
        etMed: TextInputEditText,
        etDiet: TextInputEditText
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contextState.collect { state ->
                    // Synchronisation de l'activité sélectionnée
                    selectChipByTag(group, state.physicalActivity ?: PhysicalActivity.REPOS)

                    // État des médicaments
                    if (switch.isChecked != state.medecineTaken) {
                        switch.isChecked = state.medecineTaken
                    }
                    layout.isVisible = state.medecineTaken

                    // Mise à jour sécurisée des textes (évite de réinitialiser le curseur)
                    if (etMed.text?.toString() != state.medicationList) {
                        etMed.setText(state.medicationList)
                    }
                    if (etDiet.text?.toString() != state.diet) {
                        etDiet.setText(state.diet)
                    }
                }
            }
        }
    }

    /**
     * Configure les écouteurs de saisie pour envoyer les modifications au ViewModel.
     * Utilise des vérifications de focus/pression pour ne capturer que les actions utilisateur réelles.
     */
    private fun setupInputListeners(
        group: ChipGroup,
        switch: MaterialSwitch,
        etMed: TextInputEditText,
        etDiet: TextInputEditText
    ) {
        // Fonction locale pour centraliser l'envoi des données vers le ViewModel
        fun pushUpdate() {
            val selectedChipId = group.checkedChipId
            val activity = group.findViewById<Chip>(selectedChipId)?.tag as? PhysicalActivity ?: PhysicalActivity.REPOS

            viewModel.updateContextState(
                activity = activity,
                medicine = switch.isChecked,
                medications = etMed.text?.toString() ?: "",
                diet = etDiet.text?.toString()
            )
        }

        group.setOnCheckedStateChangeListener { _, _ -> pushUpdate() }

        switch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) { // Capture uniquement le clic utilisateur
                pushUpdate()
            }
        }

        etMed.addTextChangedListener {
            if (etMed.hasFocus()) pushUpdate()
        }

        etDiet.addTextChangedListener {
            if (etDiet.hasFocus()) pushUpdate()
        }
    }

    /**
     * Parcourt les enfants du [ChipGroup] pour cocher celui correspondant au tag cible.
     * @param targetTag La valeur de l'énumération à sélectionner.
     */
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