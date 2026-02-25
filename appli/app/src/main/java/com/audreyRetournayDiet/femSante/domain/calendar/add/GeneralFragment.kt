package com.audreyRetournayDiet.femSante.domain.calendar.add

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.viewModels.EntryViewModel
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch

class GeneralFragment : Fragment(R.layout.fragment_general) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sliderPain = view.findViewById<Slider>(R.id.sliderPain)
        val switchTired = view.findViewById<MaterialSwitch>(R.id.switchTired)

        // --- 1. OBSERVATION (UI <- ViewModel) ---
        // On écoute les changements d'état pour mettre à jour les composants
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.generalState.collect { state ->
                    // On met à jour sans déclencher les listeners récursivement
                    if (sliderPain.value != state.painLevel.toFloat()) {
                        sliderPain.value = state.painLevel.toFloat()
                    }
                    if (switchTired.isChecked != state.isTired) {
                        switchTired.isChecked = state.isTired
                    }
                }
            }
        }

        // --- 2. ACTIONS (UI -> ViewModel) ---

        sliderPain.addOnChangeListener { _, value, fromUser ->
            // "fromUser" évite de boucler si la mise à jour vient du collect
            if (fromUser) {
                viewModel.updateGeneralState(
                    pain = value.toInt(),
                    tired = switchTired.isChecked
                )
            }
        }

        switchTired.setOnCheckedChangeListener { buttonView, isChecked ->
            // On ne met à jour le VM que si c'est une action réelle (clic/swipe)
            if (buttonView.isPressed) {
                viewModel.updateGeneralState(
                    pain = sliderPain.value.toInt(),
                    tired = isChecked
                )
            }
        }
    }
}