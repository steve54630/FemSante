package com.audreyRetournayDiet.femSante.domain.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.viewModels.EntryViewModel
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider

class GeneralFragment : Fragment(R.layout.fragment_general) {
    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Récupération des vues via ton format préféré
        val sliderPain = view.findViewById<Slider>(R.id.sliderPain)
        val switchTired = view.findViewById<MaterialSwitch>(R.id.switchTired)

        // 1. Ecoute du Slider (Douleur)
        sliderPain.addOnChangeListener { _, value, _ ->
            viewModel.updateGeneralState(
                pain = value.toInt(),
                tired = switchTired.isChecked
            )
        }

        // 2. Ecoute du Switch (Fatigue)
        switchTired.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateGeneralState(
                pain = sliderPain.value.toInt(),
                tired = isChecked
            )
        }
    }
}