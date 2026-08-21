package com.audreyRetournayDiet.femSante.features.calendar.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase
import com.audreyRetournayDiet.femSante.data.cycle.CyclePhaseCalculator
import com.audreyRetournayDiet.femSante.features.calendar.add.EntryAddActivity
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.type.FlowLevel
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.viewmodels.calendar.CalendarViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Écran détail d'une journée (calendrier classique : tap sur un jour → cet écran).
 *
 * Écran détail d'une journée : saisie cycle (avec pré-cochage prévisionnel opt-out), phase
 * estimée, et résumé du jour (ou état vide → [EntryAddActivity]).
 * Réutilise [CalendarViewModel] pour lire/écrire les données de la date reçue en extra.
 */
@AndroidEntryPoint
class DayDetailActivity : AppCompatActivity() {

    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var userStore: UserStore
    private lateinit var selectedDate: LocalDate

    private lateinit var switchPeriod: MaterialSwitch
    private lateinit var labelFlow: View
    private lateinit var chipGroupFlow: ChipGroup
    private lateinit var switchSpotting: MaterialSwitch
    private lateinit var textCyclePhase: TextView
    private lateinit var textPeriodPrediction: TextView

    /** Empêche les listeners de re-sauvegarder pendant qu'on remplit l'UI par programmation. */
    private var isBindingCycle = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_detail)

        userStore = UserStore(this)
        selectedDate = intent.getStringExtra(EXTRA_DATE)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()

        bindViews()
        setupTitle()
        setupCycleInputs()
        collectStateFlows()
        loadDay()
    }

    private fun bindViews() {
        switchPeriod = findViewById(R.id.switchPeriod)
        labelFlow = findViewById(R.id.labelFlow)
        chipGroupFlow = findViewById(R.id.chipGroupFlow)
        switchSpotting = findViewById(R.id.switchSpotting)
        textCyclePhase = findViewById(R.id.textCyclePhase)
        textPeriodPrediction = findViewById(R.id.textPeriodPrediction)

        // État initial déterministe du ViewSwitcher : vue vide affichée, vue données masquée.
        findViewById<ViewSwitcher>(R.id.dailyViewSwitcher).let { sw ->
            sw.getChildAt(1).visibility = View.GONE
            sw.getChildAt(0).visibility = View.VISIBLE
            sw.displayedChild = 0
        }
    }

    private fun setupTitle() {
        val formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRANCE)
        findViewById<TextView>(R.id.textDayTitle).text =
            selectedDate.format(formatter).replaceFirstChar { it.uppercase() }
    }

    private fun collectStateFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.entryResult.collect { updateUiState(it) } }
                launch { viewModel.cycleDay.collect { bindCycleInputs(it) } }
                launch {
                    // Les prévisions viennent d'être (re)calculées : réévalue le pré-cochage.
                    viewModel.predictedPeriodDates.collect { bindCycleInputs(viewModel.cycleDay.value) }
                }
                launch {
                    // Les dates de règles alimentent le calcul de phase.
                    viewModel.periodDates.collect { updatePhaseIndicator() }
                }
            }
        }
    }

    private fun loadDay() {
        // Sélectionne la journée : le résumé et la saisie cycle sont ensuite observés via des
        // Flow Room (mise à jour automatique après une édition, sans rechargement manuel).
        viewModel.selectDate(selectedDate)
        // Le prévisionnel dépend aussi des préférences (hors base) → on le réévalue ici.
        viewModel.refreshForecast()
    }

    override fun onResume() {
        super.onResume()
        // Le résumé se met à jour tout seul (Flow) au retour d'EntryAddActivity ; on réévalue
        // seulement le prévisionnel (préférences éventuellement modifiées).
        loadDay()
    }

    private fun setupCycleInputs() {
        switchPeriod.setOnCheckedChangeListener { _, isChecked ->
            updateFlowVisibility(isChecked)
            if (!isBindingCycle) saveCycleInputs()
        }
        switchSpotting.setOnCheckedChangeListener { _, _ ->
            if (!isBindingCycle) saveCycleInputs()
        }
        chipGroupFlow.setOnCheckedStateChangeListener { _, _ ->
            if (!isBindingCycle) saveCycleInputs()
        }
    }

    /** Affiche l'abondance uniquement quand les règles sont cochées. */
    private fun updateFlowVisibility(periodChecked: Boolean) {
        val visibility = if (periodChecked) View.VISIBLE else View.GONE
        labelFlow.visibility = visibility
        chipGroupFlow.visibility = visibility
    }

    /** Remplit les contrôles cycle à partir de l'observation chargée (ou des valeurs par défaut). */
    private fun bindCycleInputs(cycleDay: CycleDayEntity?) {
        isBindingCycle = true

        // Jour prédit (et pas encore saisi) : on pré-coche les règles à titre de prévision.
        val isPredictedDay = cycleDay == null && selectedDate in viewModel.predictedPeriodDates.value

        switchPeriod.isChecked = cycleDay?.isPeriod == true || isPredictedDay
        switchSpotting.isChecked = cycleDay?.spotting == true
        updateFlowVisibility(switchPeriod.isChecked)
        textPeriodPrediction.isVisible = isPredictedDay

        chipGroupFlow.clearCheck()
        when (cycleDay?.flow) {
            FlowLevel.LEGER -> chipGroupFlow.check(R.id.chipFlowLight)
            FlowLevel.MOYEN -> chipGroupFlow.check(R.id.chipFlowMedium)
            FlowLevel.ABONDANT -> chipGroupFlow.check(R.id.chipFlowHeavy)
            null -> { }
        }

        isBindingCycle = false
        updatePhaseIndicator()
    }

    /** Phase estimée du jour (masquée tant que le profil n'est pas renseigné). */
    private fun updatePhaseIndicator() {
        if (!userStore.hasCycleProfile()) {
            textCyclePhase.visibility = View.GONE
            return
        }
        val phase = CyclePhaseCalculator.calculate(
            periodDates = viewModel.periodDates.value,
            target = selectedDate,
            profile = userStore.getCycleProfile(),
            cycleLength = userStore.getCycleLength(),
            periodLength = userStore.getPeriodLength()
        )
        if (phase == null) {
            textCyclePhase.visibility = View.GONE
            return
        }
        textCyclePhase.visibility = View.VISIBLE
        textCyclePhase.text = getString(R.string.cycle_phase_label, phaseLabel(phase))
    }

    private fun phaseLabel(phase: CyclePhase): String = when (phase) {
        CyclePhase.MENSTRUELLE -> getString(R.string.cycle_phase_menstruelle)
        CyclePhase.FOLLICULAIRE -> getString(R.string.cycle_phase_folliculaire)
        CyclePhase.OVULATION -> getString(R.string.cycle_phase_ovulation)
        CyclePhase.LUTEALE -> getString(R.string.cycle_phase_luteale)
        CyclePhase.INDETERMINEE -> getString(R.string.cycle_phase_indeterminee)
    }

    private fun saveCycleInputs() {
        val flow = when (chipGroupFlow.checkedChipId) {
            R.id.chipFlowLight -> FlowLevel.LEGER
            R.id.chipFlowMedium -> FlowLevel.MOYEN
            R.id.chipFlowHeavy -> FlowLevel.ABONDANT
            else -> null
        }
        viewModel.saveCycleDay(
            isPeriod = switchPeriod.isChecked,
            flow = flow,
            spotting = switchSpotting.isChecked
        )
    }

    /** Bascule entre l'affichage "Vide" et "Détail" du jour via le [ViewSwitcher]. */
    private fun updateUiState(entry: DailyEntryFull?) {
        val switcher = findViewById<ViewSwitcher>(R.id.dailyViewSwitcher)
        val emptyView = switcher.getChildAt(0)
        val dataView = switcher.getChildAt(1)

        if (entry != null) {
            emptyView.visibility = View.GONE
            dataView.visibility = View.VISIBLE
            if (switcher.displayedChild != 1) switcher.displayedChild = 1
            CalendarUtils.updateDailyView(switcher.currentView, entry)

            switcher.currentView.findViewById<MaterialButton>(R.id.btnEdit)?.setOnClickListener {
                val entryDate = Instant.ofEpochMilli(entry.dailyEntry.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                startActivity(Intent(this, EntryAddActivity::class.java).apply {
                    putExtra("ID", entry.dailyEntry.id)
                    putExtra("isEditMode", true)
                    putExtra("selectedDate", entryDate.toString())
                })
            }

            switcher.currentView.findViewById<MaterialButton>(R.id.btnDelete)?.setOnClickListener {
                showDeleteConfirmation()
            }
        } else {
            dataView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            switcher.displayedChild = 0
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRANCE)
            findViewById<TextView>(R.id.tvEmptyDate)?.text =
                getString(R.string.day_no_tracking, selectedDate.format(formatter))

            findViewById<Button>(R.id.btnCreateEntry)?.setOnClickListener {
                startActivity(Intent(this, EntryAddActivity::class.java).apply {
                    putExtra("selectedDate", selectedDate.toString())
                    putExtra("isEditMode", false)
                })
            }
        }
    }

    private fun showDeleteConfirmation() {
        val currentEntry = viewModel.entryResult.value ?: return
        AlertDialog.Builder(this)
            .setTitle("Supprimer le suivi ?")
            .setMessage("Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ -> viewModel.deleteData(currentEntry) }
            .setNegativeButton("Annuler", null)
            .show()
    }

    companion object {
        private const val EXTRA_DATE = "selectedDate"

        fun intent(context: Context, date: LocalDate): Intent =
            Intent(context, DayDetailActivity::class.java).putExtra(EXTRA_DATE, date.toString())
    }
}
