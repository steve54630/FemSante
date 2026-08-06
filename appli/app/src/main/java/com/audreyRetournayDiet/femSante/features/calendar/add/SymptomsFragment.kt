package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.PainZone
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Onglet « Corps & douleur » : douleur **par zone** via la carte du corps, plus nausée et
 * notes libres. On touche une zone → une **modale (BottomSheet)** propose le réglage
 * d'intensité (1–10) ; la carte reste visible au-dessus et se colore en direct. La douleur
 * globale est dérivée par le ViewModel (max des zones).
 */
@AndroidEntryPoint
class SymptomsFragment : Fragment(R.layout.fragment_symptom) {

    private val viewModel: EntryViewModel by activityViewModels()

    /** Intensité par défaut proposée quand on touche une zone encore vierge. */
    private val defaultIntensity = 5

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bodyMap = view.findViewById<BodyPainMapView>(R.id.bodyPainMap)
        val toggle = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleFaceBack)
        val switchNausea = view.findViewById<MaterialSwitch>(R.id.switchNausea)
        val etOthers = view.findViewById<TextInputEditText>(R.id.etOthersSymptoms)

        toggle.check(R.id.btnFace)

        bodyMap.onChanged = { map -> viewModel.updatePainByZone(map) }
        bodyMap.onZoneSelected = { zone -> showIntensitySheet(bodyMap, zone) }

        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) bodyMap.showBack = (checkedId == R.id.btnBack)
        }

        setupExtras(switchNausea, etOthers)
        observeState(bodyMap, switchNausea, etOthers)
    }

    /** Modale d'intensité pour la zone touchée. Le réglage colore la carte en direct. */
    private fun showIntensitySheet(bodyMap: BodyPainMapView, zone: PainZone) {
        val sheet = BottomSheetDialog(requireContext())
        val content = layoutInflater.inflate(R.layout.bottom_sheet_pain_intensity, null)
        val tvZone = content.findViewById<TextView>(R.id.tvZoneName)
        val slider = content.findViewById<Slider>(R.id.sliderIntensity)
        val btnRemove = content.findViewById<MaterialButton>(R.id.btnRemoveZone)
        val btnValidate = content.findViewById<MaterialButton>(R.id.btnValidate)

        // Zone vierge -> intensité par défaut, colorée immédiatement (feedback).
        val current = bodyMap.intensityOf(zone)
        val level = if (current in 1..10) current else defaultIntensity
        if (current !in 1..10) bodyMap.setIntensity(zone, level)

        tvZone.text = zoneLabel(zone)
        slider.value = level.toFloat()

        slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) bodyMap.setIntensity(zone, value.toInt())
        }
        btnRemove.setOnClickListener {
            bodyMap.setIntensity(zone, 0)
            sheet.dismiss()
        }
        btnValidate.setOnClickListener { sheet.dismiss() }
        sheet.setOnDismissListener { bodyMap.clearSelection() }

        sheet.setContentView(content)
        sheet.show()
    }

    private fun setupExtras(switchNausea: MaterialSwitch, etOthers: TextInputEditText) {
        fun push() = viewModel.updateSymptomExtras(switchNausea.isChecked, etOthers.text?.toString())

        switchNausea.setOnCheckedChangeListener { buttonView, _ ->
            if (buttonView.isPressed) push()
        }
        etOthers.addTextChangedListener {
            if (etOthers.hasFocus()) push()
        }
    }

    /** Synchronise l'UI depuis le ViewModel (préchargement en édition, cohérence d'état). */
    private fun observeState(
        bodyMap: BodyPainMapView,
        switchNausea: MaterialSwitch,
        etOthers: TextInputEditText
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.symptomState.collect { state ->
                    Timber.v("Sync UI : zones=${state.painByZone}, nausée=${state.hasNausea}")
                    bodyMap.setPainByZone(state.painByZone)
                    if (switchNausea.isChecked != state.hasNausea) switchNausea.isChecked = state.hasNausea
                    if (etOthers.text?.toString() != state.others) etOthers.setText(state.others)
                }
            }
        }
    }

    private fun zoneLabel(zone: PainZone): String = when (zone) {
        PainZone.BASSIN -> "Bassin"
        PainZone.LOMBAIRES -> "Lombaires"
        PainZone.SEINS -> "Seins"
        PainZone.TETE -> "Tête"
        PainZone.ABDOMEN -> "Abdomen"
        PainZone.BRAS -> "Bras"
        PainZone.CUISSES -> "Cuisses"
        PainZone.HAUT_DOS -> "Haut du dos"
        PainZone.JAMBES -> "Jambes"
        PainZone.AUTRE -> "Autre"
    }
}
