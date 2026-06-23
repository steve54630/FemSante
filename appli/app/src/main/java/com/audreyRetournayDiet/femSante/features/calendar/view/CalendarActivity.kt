package com.audreyRetournayDiet.femSante.features.calendar.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.features.calendar.add.EntryAddActivity
import com.audreyRetournayDiet.femSante.data.cycle.CyclePhase
import com.audreyRetournayDiet.femSante.data.cycle.CyclePhaseCalculator
import com.audreyRetournayDiet.femSante.repository.local.CycleRepository
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import com.audreyRetournayDiet.femSante.room.type.FlowLevel
import com.audreyRetournayDiet.femSante.viewModels.calendar.CalendarViewModel
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.core.view.isNotEmpty
import timber.log.Timber

/**
 * Activité principale du calendrier de suivi des symptômes.
 * * Cette activité affiche un calendrier mensuel interactif permettant de :
 * - Visualiser l'historique des douleurs via des pastilles de couleur (Vert/Jaune/Rouge).
 * - Consulter le détail d'une journée via un BottomSheet coulissant.
 * - Naviguer vers la création ou l'édition d'une entrée quotidienne.
 * * Elle utilise la bibliothèque `kizitonwose/CalendarView` et communique avec le [CalendarViewModel]
 * pour la récupération des données en base locale.
 */
@SuppressLint("SetTextI18n")
class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var monthText: TextView
    private lateinit var prevMonth: ImageButton
    private lateinit var nextMonth: ImageButton
    private lateinit var dailyViewSection: View
    private lateinit var userStore: UserStore
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    // --- Vues de saisie du cycle (dans le bottom sheet) ---
    private lateinit var switchPeriod: MaterialSwitch
    private lateinit var labelFlow: View
    private lateinit var chipGroupFlow: ChipGroup
    private lateinit var switchSpotting: MaterialSwitch
    private lateinit var textCycleProfile: TextView
    private lateinit var textCyclePhase: TextView

    /** Empêche les listeners de re-sauvegarder pendant qu'on remplit l'UI par programmation. */
    private var isBindingCycle = false

    private val viewModel: CalendarViewModel by viewModels {
        val database = DatabaseProvider.getDatabase(this)
        val repository = DailyRepository(database.dailyDao())
        val cycleRepository = CycleRepository(database.cycleDao())
        CalendarViewModel.Factory(repository, cycleRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_calendar)

        setupViews()
        setupCycleInputs()
        initCalendar()
        collectStateFlows()
        promptCycleProfileIfNeeded()
    }

    /**
     * Initialise les composants de la vue et le BottomSheet.
     */
    private fun setupViews() {
        calendarView = findViewById(R.id.calendarView)
        monthText = findViewById(R.id.monthText)
        prevMonth = findViewById(R.id.btnPrevMonth)
        nextMonth = findViewById(R.id.btnNextMonth)
        dailyViewSection = findViewById(R.id.dailyView)
        bottomSheetBehavior = BottomSheetBehavior.from(dailyViewSection)

        switchPeriod = findViewById(R.id.switchPeriod)
        labelFlow = findViewById(R.id.labelFlow)
        chipGroupFlow = findViewById(R.id.chipGroupFlow)
        switchSpotting = findViewById(R.id.switchSpotting)
        textCycleProfile = findViewById(R.id.textCycleProfile)
        textCyclePhase = findViewById(R.id.textCyclePhase)

        userStore = UserStore(this)

        prevMonth.setOnClickListener {
            calendarView.scrollToMonth(calendarView.findFirstVisibleMonth()!!.yearMonth.minusMonths(1))
        }
        nextMonth.setOnClickListener {
            calendarView.scrollToMonth(calendarView.findFirstVisibleMonth()!!.yearMonth.plusMonths(1))
        }
    }

    /**
     * Configure les collecteurs de données réactifs (StateFlow).
     * Gère la mise à jour des pastilles de couleur et de l'affichage détaillé.
     */
    private fun collectStateFlows() {
        val userId = userStore.getUser()?.id ?: return Timber.e("UserID null")
        viewModel.initData(userId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Déclenche le re-rendu du calendrier lors d'un changement de sélection
                launch {
                    viewModel.date.collect {
                        calendarView.notifyCalendarChanged()
                        // La phase dépend de la date sélectionnée, même les jours sans
                        // saisie : on recalcule ici (cycleDay ne ré-émet pas si null->null).
                        updatePhaseIndicator()
                    }
                }
                // Met à jour la section détaillée (BottomSheet) quand une entrée est chargée
                launch {
                    viewModel.entryResult.collect { entry ->
                        updateUiState(entry)
                    }
                }
                // Actualise les indicateurs de douleur (points de couleur)
                launch {
                    viewModel.dailyStatus.collect {
                        calendarView.notifyCalendarChanged()
                    }
                }
                // Pré-remplit la saisie cycle selon la journée sélectionnée
                launch {
                    viewModel.cycleDay.collect { bindCycleInputs(it) }
                }
                // Rafraîchit les marqueurs de règles sur la grille + l'indicateur de phase
                launch {
                    viewModel.periodDates.collect {
                        calendarView.notifyCalendarChanged()
                        updatePhaseIndicator()
                    }
                }
            }
        }
    }

    /**
     * Configure les écouteurs de la saisie cycle. Toute modification (par l'utilisatrice)
     * déclenche une sauvegarde immédiate via le ViewModel. Le flag [isBindingCycle] évite
     * que le pré-remplissage par programmation ne re-déclenche une sauvegarde.
     */
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

        textCycleProfile.setOnClickListener { showCycleProfileDialog() }
        refreshCycleProfileLabel()
    }

    /** Met à jour le libellé du profil de cycle affiché (cliquable pour le modifier). */
    private fun refreshCycleProfileLabel() {
        textCycleProfile.text = getString(R.string.cycle_profile_current, cycleProfileLabel(userStore.getCycleProfile()))
    }

    private fun cycleProfileLabel(profile: CycleProfile): String = when (profile) {
        CycleProfile.REGULIER -> getString(R.string.cycle_profile_regular)
        CycleProfile.IRREGULIER -> getString(R.string.cycle_profile_irregular)
        CycleProfile.ABSENT_OU_PILULE -> getString(R.string.cycle_profile_absent)
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

        switchPeriod.isChecked = cycleDay?.isPeriod == true
        switchSpotting.isChecked = cycleDay?.spotting == true
        updateFlowVisibility(switchPeriod.isChecked)

        chipGroupFlow.clearCheck()
        when (cycleDay?.flow) {
            FlowLevel.LEGER -> chipGroupFlow.check(R.id.chipFlowLight)
            FlowLevel.MOYEN -> chipGroupFlow.check(R.id.chipFlowMedium)
            FlowLevel.ABONDANT -> chipGroupFlow.check(R.id.chipFlowHeavy)
            null -> { /* aucun flux sélectionné */ }
        }

        isBindingCycle = false
        updatePhaseIndicator()
    }

    /**
     * Met à jour l'indicateur de phase pour la date sélectionnée, conditionné par le
     * profil (masqué si absent/pilule). Incrément 2 : affichage de la phase courante
     * uniquement, sans aucune prédiction ni compte à rebours.
     */
    private fun updatePhaseIndicator() {
        val phase = CyclePhaseCalculator.calculate(
            periodDates = viewModel.periodDates.value,
            target = viewModel.date.value,
            profile = userStore.getCycleProfile()
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

    /** Récupère l'état des contrôles et enregistre l'observation pour la date sélectionnée. */
    private fun saveCycleInputs() {
        val userId = userStore.getUser()?.id ?: return
        val flow = when (chipGroupFlow.checkedChipId) {
            R.id.chipFlowLight -> FlowLevel.LEGER
            R.id.chipFlowMedium -> FlowLevel.MOYEN
            R.id.chipFlowHeavy -> FlowLevel.ABONDANT
            else -> null
        }
        viewModel.saveCycleDay(
            userId = userId,
            selectedDate = viewModel.date.value,
            isPeriod = switchPeriod.isChecked,
            flow = flow,
            spotting = switchSpotting.isChecked
        )
    }

    /**
     * Demande le profil de cycle à la première ouverture du calendrier (zéro anxiété :
     * conditionne phases/prédictions des incréments suivants). Modifiable plus tard.
     */
    private fun promptCycleProfileIfNeeded() {
        if (userStore.hasCycleProfile()) return
        showCycleProfileDialog(cancelable = false)
    }

    /**
     * Affiche le dialog de choix du profil de cycle. Utilisé au premier lancement
     * (non annulable) et pour une modification ultérieure (annulable) via le libellé
     * cliquable de la section cycle.
     */
    private fun showCycleProfileDialog(cancelable: Boolean = true) {
        val labels = arrayOf(
            getString(R.string.cycle_profile_regular),
            getString(R.string.cycle_profile_irregular),
            getString(R.string.cycle_profile_absent)
        )
        val profiles = arrayOf(
            CycleProfile.REGULIER,
            CycleProfile.IRREGULIER,
            CycleProfile.ABSENT_OU_PILULE
        )
        // NB : ne pas utiliser setMessage() avec setItems() — le message masque la liste.
        AlertDialog.Builder(this)
            .setTitle(R.string.cycle_profile_title)
            .setItems(labels) { _, which ->
                userStore.setCycleProfile(profiles[which])
                refreshCycleProfileLabel()
                updatePhaseIndicator()
            }
            .setCancelable(cancelable)
            .show()
    }

    /**
     * Configure le comportement et l'apparence des cellules du calendrier.
     */
    private fun initCalendar() {
        setupDaysOfWeek()

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val date = data.date
                val painLevel = viewModel.dailyStatus.value[date]

                container.textView.text = date.dayOfMonth.toString()

                // Marqueur de règles (suivi de cycle)
                container.periodMarker.isVisible =
                    data.position == DayPosition.MonthDate && date in viewModel.periodDates.value

                // Logique d'affichage de la pastille (Point)
                when {
                    date == LocalDate.now() -> {
                        container.dotView.isVisible = true
                        container.dotView.backgroundTintList = ColorStateList.valueOf(Color.CYAN)
                    }
                    painLevel != null -> {
                        container.dotView.isVisible = true
                        val color = when {
                            painLevel >= 7 -> Color.RED
                            painLevel >= 4 -> Color.YELLOW
                            else -> Color.GREEN
                        }
                        container.dotView.backgroundTintList = ColorStateList.valueOf(color)
                    }
                    else -> container.dotView.isVisible = false
                }

                // Gestion du style visuel (Mois courant vs Hors mois / Sélection)
                if (data.position == DayPosition.MonthDate) {
                    container.view.alpha = 1f
                    val isSelected = date == viewModel.date.value
                    container.textView.setTextColor(if (isSelected) Color.RED else Color.BLACK)
                    container.textView.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
                } else {
                    container.view.alpha = 0.3f
                }
            }
        }

        calendarView.setup(YearMonth.now().minusMonths(12), YearMonth.now().plusMonths(12), DayOfWeek.MONDAY)
        calendarView.scrollToMonth(YearMonth.now())
        calendarView.monthScrollListener = { updateMonthTitle(it.yearMonth) }
    }

    /**
     * Gère la transition entre l'affichage "Vide" et l'affichage "Détail" du jour.
     * Utilise un [ViewSwitcher] pour basculer entre les deux états.
     * @param entry L'entrée complète récupérée en base, ou null si aucune donnée n'existe.
     */
    private fun updateUiState(entry: DailyEntryFull?) {
        val switcher = dailyViewSection.findViewById<ViewSwitcher>(R.id.dailyViewSwitcher)

        if (entry != null) {
            if (switcher.displayedChild != 1) switcher.displayedChild = 1
            CalendarUtils.updateDailyView(switcher.currentView, entry)

            switcher.currentView.findViewById<MaterialButton>(R.id.btnEdit)?.setOnClickListener {
                // entry.dailyEntry.date est un timestamp epoch (Long) : on le convertit en
                // LocalDate ISO, sinon EntryAddActivity.handleIntentData crashe en tentant
                // LocalDate.parse() sur une chaîne de millisecondes.
                val selectedDate = Instant.ofEpochMilli(entry.dailyEntry.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                val intent = Intent(this, EntryAddActivity::class.java).apply {
                    putExtra("ID", entry.dailyEntry.id)
                    putExtra("isEditMode", true)
                    putExtra("selectedDate", selectedDate.toString())
                }
                startActivity(intent)
            }

            switcher.currentView.findViewById<MaterialButton>(R.id.btnDelete)?.setOnClickListener {
                showDeleteConfirmation()
            }
        } else {
            switcher.displayedChild = 0
            val date = viewModel.date.value
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRANCE)
            dailyViewSection.findViewById<TextView>(R.id.tvEmptyDate)?.text = "Pas de suivi le ${date.format(formatter)}"

            dailyViewSection.findViewById<Button>(R.id.btnCreateEntry)?.setOnClickListener {
                startActivity(Intent(this, EntryAddActivity::class.java).apply {
                    putExtra("selectedDate", date.toString())
                    putExtra("isEditMode", false)
                })
            }
        }
    }

    /**
     * Affiche une boîte de dialogue pour confirmer la suppression d'un suivi.
     */
    private fun showDeleteConfirmation() {
        val currentEntry = viewModel.entryResult.value ?: return
        AlertDialog.Builder(this)
            .setTitle("Supprimer le suivi ?")
            .setMessage("Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteData(currentEntry)
            }
            .setNegativeButton("Anuler", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchissement automatique au retour d'EntryAddActivity
        userStore.getUser()?.id?.let {
            viewModel.initData(it)
            viewModel.loadData(it, viewModel.date.value)
        }
    }

    /**
     * Génère dynamiquement les titres des jours de la semaine (LUN, MAR, etc.).
     */
    private fun setupDaysOfWeek() {
        val titlesContainer = findViewById<LinearLayout>(R.id.titlesContainer)
        if (titlesContainer.isNotEmpty()) titlesContainer.removeAllViews()
        daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY).forEach { dayOfWeek ->
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRANCE).uppercase()
            }
            titlesContainer.addView(textView)
        }
    }

    private fun updateMonthTitle(yearMonth: YearMonth) {
        monthText.text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.FRANCE).replaceFirstChar { it.uppercase() }} ${yearMonth.year}"
    }

    /**
     * Conteneur de vue pour une cellule de jour du calendrier.
     * Gère le clic sur une date pour charger les détails correspondants.
     */
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val dotView: View = view.findViewById(R.id.priorityDot)
        val periodMarker: View = view.findViewById(R.id.periodMarker)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position != DayPosition.MonthDate) {
                    calendarView.scrollToMonth(YearMonth.from(day.date))
                }
                if (viewModel.date.value != day.date) {
                    userStore.getUser()?.id?.let { userId ->
                        viewModel.loadData(userId, day.date)
                    }
                }
            }
        }
    }
}