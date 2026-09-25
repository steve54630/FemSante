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
import com.audreyRetournayDiet.femSante.viewmodels.calendar.EntryViewModel
import com.audreyRetournayDiet.femSante.shared.addTagChips
import com.audreyRetournayDiet.femSante.shared.checkChipByTag
import com.audreyRetournayDiet.femSante.shared.selectedTag
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment de saisie du contexte quotidien pour une entrée du calendrier.
 * * Permet à l'utilisatrice de renseigner :
 * - Son niveau d'activité physique (via des [Chip] dynamiques).
 * - Sa prise de médicaments (avec affichage conditionnel de la liste).
 * - Ses notes sur son alimentation.
 * * Utilise un [EntryViewModel] partagé au niveau de l'activité pour centraliser les données
 * avant la sauvegarde finale en base de données.
 */
@AndroidEntryPoint
class ContextFragment : Fragment(R.layout.fragment_context) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chipGroupActivity = view.findViewById<ChipGroup>(R.id.chipGroupActivity)
        val layoutActivityDetail = view.findViewById<TextInputLayout>(R.id.layoutActivityDetail)
        val etActivityDetail = view.findViewById<TextInputEditText>(R.id.etActivityDetail)
        val switchMedication = view.findViewById<MaterialSwitch>(R.id.switchMedication)
        val layoutMedicationList = view.findViewById<TextInputLayout>(R.id.layoutMedicationList)
        val etMedicationList = view.findViewById<TextInputEditText>(R.id.etMedicationList)
        val etDietMorning = view.findViewById<TextInputEditText>(R.id.etDietMorning)
        val etDietNoon = view.findViewById<TextInputEditText>(R.id.etDietNoon)
        val etDietEvening = view.findViewById<TextInputEditText>(R.id.etDietEvening)

        if (chipGroupActivity.isEmpty()) {
            chipGroupActivity.addTagChips(PhysicalActivity.entries)
        }

        observeState(
            chipGroupActivity, layoutActivityDetail, etActivityDetail,
            switchMedication, layoutMedicationList, etMedicationList,
            etDietMorning, etDietNoon, etDietEvening
        )
        setupInputListeners(
            chipGroupActivity, etActivityDetail, switchMedication, etMedicationList,
            etDietMorning, etDietNoon, etDietEvening
        )
    }

    /**
     * Observe le flux d'état du ViewModel pour synchroniser l'UI.
     * Inclut des vérifications pour éviter les boucles de mise à jour infinies sur les champs texte.
     */
    private fun observeState(
        group: ChipGroup,
        layoutActivityDetail: TextInputLayout,
        etActivityDetail: TextInputEditText,
        switch: MaterialSwitch,
        layout: TextInputLayout,
        etMed: TextInputEditText,
        etDietMorning: TextInputEditText,
        etDietNoon: TextInputEditText,
        etDietEvening: TextInputEditText
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contextState.collect { state ->
                    val activity = state.physicalActivity ?: PhysicalActivity.REPOS
                    group.checkChipByTag(activity)

                    // Précision d'activité : pas pertinente au repos.
                    layoutActivityDetail.isVisible = activity != PhysicalActivity.REPOS
                    if (etActivityDetail.text?.toString() != state.activityDetail) {
                        etActivityDetail.setText(state.activityDetail)
                    }

                    if (switch.isChecked != state.medecineTaken) {
                        switch.isChecked = state.medecineTaken
                    }
                    layout.isVisible = state.medecineTaken

                    // Mise à jour sécurisée des textes (évite de réinitialiser le curseur)
                    if (etMed.text?.toString() != state.medicationList) {
                        etMed.setText(state.medicationList)
                    }
                    if (etDietMorning.text?.toString() != state.dietMorning) {
                        etDietMorning.setText(state.dietMorning)
                    }
                    if (etDietNoon.text?.toString() != state.dietNoon) {
                        etDietNoon.setText(state.dietNoon)
                    }
                    if (etDietEvening.text?.toString() != state.dietEvening) {
                        etDietEvening.setText(state.dietEvening)
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
        etActivityDetail: TextInputEditText,
        switch: MaterialSwitch,
        etMed: TextInputEditText,
        etDietMorning: TextInputEditText,
        etDietNoon: TextInputEditText,
        etDietEvening: TextInputEditText
    ) {
        fun pushUpdate() {
            val activity = group.selectedTag<PhysicalActivity>() ?: PhysicalActivity.REPOS

            viewModel.updateContextState(
                activity = activity,
                activityDetail = etActivityDetail.text?.toString(),
                medicine = switch.isChecked,
                medications = etMed.text?.toString() ?: "",
                dietMorning = etDietMorning.text?.toString(),
                dietNoon = etDietNoon.text?.toString(),
                dietEvening = etDietEvening.text?.toString()
            )
        }

        group.setOnCheckedStateChangeListener { _, _ -> pushUpdate() }

        switch.setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) { // Capture uniquement le clic utilisateur
                pushUpdate()
            }
        }

        etActivityDetail.addTextChangedListener {
            if (etActivityDetail.hasFocus()) pushUpdate()
        }

        etMed.addTextChangedListener {
            if (etMed.hasFocus()) pushUpdate()
        }

        etDietMorning.addTextChangedListener {
            if (etDietMorning.hasFocus()) pushUpdate()
        }

        etDietNoon.addTextChangedListener {
            if (etDietNoon.hasFocus()) pushUpdate()
        }

        etDietEvening.addTextChangedListener {
            if (etDietEvening.hasFocus()) pushUpdate()
        }
    }

}