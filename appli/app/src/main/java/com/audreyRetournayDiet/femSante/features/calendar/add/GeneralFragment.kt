package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment gérant les indicateurs généraux de santé quotidienne.
 * * Il permet à l'utilisatrice de renseigner :
 * - Son niveau de douleur via un [Slider].
 * - Son état de fatigue via un [MaterialSwitch].
 * * Ce fragment utilise le [EntryViewModel] partagé par l'activité pour maintenir la cohérence
 * des données durant le processus de création ou d'édition d'une entrée.
 */
class GeneralFragment : Fragment(R.layout.fragment_general) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sliderPain = view.findViewById<Slider>(R.id.sliderPain)
        val switchTired = view.findViewById<MaterialSwitch>(R.id.switchTired)

        observeGeneralState(sliderPain, switchTired)
        setupInputListeners(sliderPain, switchTired)
    }

    /**
     * Observe les changements d'état du ViewModel pour mettre à jour les composants UI.
     * Les vérifications d'égalité évitent de redéclencher des animations inutiles.
     */
    private fun observeGeneralState(slider: Slider, switch: MaterialSwitch) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.generalState.collect { state ->
                    Timber.v("Sync UI : Douleur=${state.painLevel}, Fatigue=${state.isTired}")

                    if (slider.value != state.painLevel.toFloat()) {
                        slider.value = state.painLevel.toFloat()
                    }

                    if (switch.isChecked != state.isTired) {
                        switch.isChecked = state.isTired
                    }
                }
            }
        }
    }

    /**
     * Configure les écouteurs d'événements pour transmettre les saisies utilisateur au ViewModel.
     * Utilise des gardes (fromUser, isPressed) pour s'assurer que seules les actions
     * manuelles déclenchent une mise à jour.
     */
    private fun setupInputListeners(slider: Slider, switch: MaterialSwitch) {
        slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                Timber.d("Action : Niveau de douleur réglé sur $value")
                viewModel.updateGeneralState(
                    pain = value.toInt(),
                    tired = switch.isChecked
                )
            }
        }

        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                Timber.d("Action : État de fatigue modifié -> $isChecked")
                viewModel.updateGeneralState(
                    pain = slider.value.toInt(),
                    tired = isChecked
                )
            }
        }
    }
}