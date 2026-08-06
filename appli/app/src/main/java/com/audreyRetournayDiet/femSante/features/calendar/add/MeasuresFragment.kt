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
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Onglet « Mesures » : poids et tours (taille, hanches, cuisses, poitrine, bras). Tous
 * facultatifs. La saisie accepte la virgule décimale (fr) ; les valeurs vides valent null.
 * Le poids est présenté sans être l'indicateur principal (cf. note dans le layout).
 */
@AndroidEntryPoint
class MeasuresFragment : Fragment(R.layout.fragment_measures) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etWeight = view.findViewById<TextInputEditText>(R.id.etWeight)
        val etWaist = view.findViewById<TextInputEditText>(R.id.etWaist)
        val etHips = view.findViewById<TextInputEditText>(R.id.etHips)
        val etThighs = view.findViewById<TextInputEditText>(R.id.etThighs)
        val etChest = view.findViewById<TextInputEditText>(R.id.etChest)
        val etArms = view.findViewById<TextInputEditText>(R.id.etArms)
        val fields = listOf(etWeight, etWaist, etHips, etThighs, etChest, etArms)

        fun push() = viewModel.updateMeasurements(
            weight = parse(etWeight), waist = parse(etWaist), hips = parse(etHips),
            thighs = parse(etThighs), chest = parse(etChest), arms = parse(etArms)
        )

        fields.forEach { field ->
            field.addTextChangedListener { if (field.hasFocus()) push() }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.measurementState.collect { m ->
                    bind(etWeight, m.weightKg)
                    bind(etWaist, m.waistCm)
                    bind(etHips, m.hipsCm)
                    bind(etThighs, m.thighsCm)
                    bind(etChest, m.chestCm)
                    bind(etArms, m.armsCm)
                }
            }
        }
    }

    private fun parse(field: TextInputEditText): Double? =
        field.text?.toString()?.trim()?.replace(',', '.')?.toDoubleOrNull()

    /** Anti-boucle : ne réécrit pas le champ que l'utilisatrice est en train d'éditer. */
    private fun bind(field: TextInputEditText, value: Double?) {
        val text = format(value)
        if (!field.hasFocus() && field.text?.toString() != text) field.setText(text)
    }

    private fun format(value: Double?): String = value?.let {
        if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
    } ?: ""
}
