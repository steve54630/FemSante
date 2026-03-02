package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.util.Log
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

class GeneralFragment : Fragment(R.layout.fragment_general) {

    private val tag = "FRAG_GENERAL"
    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated : Initialisation de l'état général")

        val sliderPain = view.findViewById<Slider>(R.id.sliderPain)
        val switchTired = view.findViewById<MaterialSwitch>(R.id.switchTired)

        // --- 1. OBSERVATION (UI <- ViewModel) ---
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.generalState.collect { state ->
                    Log.v(tag, "Sync UI : Douleur=${state.painLevel}, Fatigue=${state.isTired}")

                    // Mise à jour du slider si la valeur diffère
                    if (sliderPain.value != state.painLevel.toFloat()) {
                        sliderPain.value = state.painLevel.toFloat()
                    }

                    // Mise à jour du switch si la valeur diffère
                    if (switchTired.isChecked != state.isTired) {
                        switchTired.isChecked = state.isTired
                    }
                }
            }
        }

        // --- 2. ACTIONS (UI -> ViewModel) ---

        sliderPain.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                Log.d(tag, "Action Utilisateur : Changement douleur -> $value")
                viewModel.updateGeneralState(
                    pain = value.toInt(),
                    tired = switchTired.isChecked
                )
            }
        }

        switchTired.setOnCheckedChangeListener { buttonView, isChecked ->
            // On ne met à jour le VM que si c'est une action réelle (clic/swipe)
            if (buttonView.isPressed) {
                Log.d(tag, "Action Utilisateur : Switch fatigue -> $isChecked")
                viewModel.updateGeneralState(
                    pain = sliderPain.value.toInt(),
                    tired = isChecked
                )
            }
        }
    }
}