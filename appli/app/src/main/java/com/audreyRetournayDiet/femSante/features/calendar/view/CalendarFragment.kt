package com.audreyRetournayDiet.femSante.features.calendar.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.report.MedicalReport
import com.audreyRetournayDiet.femSante.data.report.MedicalReportPdfGenerator
import com.audreyRetournayDiet.femSante.data.report.MedicalReportSharer
import com.audreyRetournayDiet.femSante.data.report.ReportFormat
import com.audreyRetournayDiet.femSante.data.report.ReportPeriod
import com.audreyRetournayDiet.femSante.features.main.PreferencesActivity
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.viewmodels.calendar.CalendarViewModel
import com.audreyRetournayDiet.femSante.viewmodels.calendar.event.CalendarEvent
import com.google.android.material.button.MaterialButton
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Calendrier de suivi (onglet de [HomeActivity]). Calendrier « classique » : la grille
 * affiche la qualité des journées (cases teintées) et les règles (réelles / prévues),
 * et un tap sur un jour ouvre [DayDetailActivity].
 *
 * Toute la saisie et le résumé du jour vivent dans l'écran détail, ce qui garde la grille
 * simple et sans état de sélection.
 */
@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var monthText: TextView
    private lateinit var prevMonth: ImageButton
    private lateinit var nextMonth: ImageButton
    private lateinit var userStore: UserStore

    private val viewModel: CalendarViewModel by viewModels()

    /** Ce que l'utilisatrice veut faire du récap une fois généré : partager ou enregistrer. */
    private enum class ReportAction { SHARE, SAVE }
    private var pendingAction = ReportAction.SHARE

    /** PDF en attente d'écriture vers l'emplacement choisi (flux « Enregistrer »). */
    private var pendingSaveFile: File? = null

    /** Sélecteur d'emplacement système (SAF) pour enregistrer le PDF sur l'appareil. */
    private val saveDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> onSaveLocationPicked(uri) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_calendar, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        initCalendar()
        collectStateFlows()
        promptCycleProfileIfNeeded()
    }

    private fun setupViews(view: View) {
        calendarView = view.findViewById(R.id.calendarView)
        monthText = view.findViewById(R.id.monthText)
        prevMonth = view.findViewById(R.id.btnPrevMonth)
        nextMonth = view.findViewById(R.id.btnNextMonth)

        userStore = UserStore(requireContext())

        prevMonth.setOnClickListener {
            calendarView.scrollToMonth(calendarView.findFirstVisibleMonth()!!.yearMonth.minusMonths(1))
        }
        nextMonth.setOnClickListener {
            calendarView.scrollToMonth(calendarView.findFirstVisibleMonth()!!.yearMonth.plusMonths(1))
        }

        view.findViewById<MaterialButton>(R.id.btnMedicalReport).setOnClickListener {
            showReportDialog()
        }

        view.findViewById<MaterialButton>(R.id.btnMeasureTrend).setOnClickListener {
            startActivity(Intent(requireContext(), MeasureTrendActivity::class.java))
        }
    }

    /**
     * Dialog du récap médical : choix de la période (1/3/6 mois) et du niveau de détail.
     * À la validation, on délègue l'agrégation au ViewModel (le PDF + le partage suivent via
     * [CalendarEvent.ReportReady]).
     */
    private fun showReportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_report_options, null)
        val groupPeriod = dialogView.findViewById<RadioGroup>(R.id.groupPeriod)
        val groupFormat = dialogView.findViewById<RadioGroup>(R.id.groupFormat)

        fun requestReport(action: ReportAction) {
            val period = when (groupPeriod.checkedRadioButtonId) {
                R.id.periodOne -> ReportPeriod.ONE_MONTH
                R.id.periodSix -> ReportPeriod.SIX_MONTHS
                else -> ReportPeriod.THREE_MONTHS
            }
            val format = if (groupFormat.checkedRadioButtonId == R.id.formatJournal)
                ReportFormat.SUMMARY_AND_JOURNAL else ReportFormat.SUMMARY_ONLY
            pendingAction = action
            viewModel.buildMedicalReport(period, format)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.report_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.report_share) { _, _ -> requestReport(ReportAction.SHARE) }
            .setNeutralButton(R.string.report_save) { _, _ -> requestReport(ReportAction.SAVE) }
            .setNegativeButton(R.string.report_cancel, null)
            .show()
    }

    /**
     * Génère le PDF (hors thread principal) dans le cache privé, puis selon le choix de
     * l'utilisatrice : ouvre le sélecteur de partage, ou le sélecteur d'emplacement pour
     * enregistrer une copie sur l'appareil.
     */
    private fun handleReportReady(report: MedicalReport, format: ReportFormat) {
        val action = pendingAction
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    val target = MedicalReportSharer.newReportFile(requireContext())
                    MedicalReportPdfGenerator().generate(report, format, target)
                }
                when (action) {
                    ReportAction.SHARE -> MedicalReportSharer.share(requireContext(), file)
                    ReportAction.SAVE -> {
                        pendingSaveFile = file
                        saveDocumentLauncher.launch(file.name)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Échec génération du récap médical")
                Toast.makeText(requireContext(), R.string.report_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Copie le PDF généré vers l'emplacement choisi par l'utilisatrice (ou ignore si annulé). */
    private fun onSaveLocationPicked(uri: Uri?) {
        val file = pendingSaveFile
        pendingSaveFile = null
        if (uri == null || file == null) return
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                file.inputStream().use { it.copyTo(out) }
            }
            Toast.makeText(requireContext(), R.string.report_saved, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Timber.e(e, "Échec enregistrement du récap médical")
            Toast.makeText(requireContext(), R.string.report_error, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Collecteurs réactifs pour la grille (couleurs + marqueurs réels/prévus). Les données
     * viennent de `Flow` Room via le ViewModel : toute écriture met à jour la grille toute
     * seule. Liés au cycle de vie de la vue (auto-annulés en onDestroyView).
     */
    private fun collectStateFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dailyStatus.collect { calendarView.notifyCalendarChanged() }
                }
                launch {
                    viewModel.periodDates.collect { calendarView.notifyCalendarChanged() }
                }
                launch {
                    viewModel.predictedPeriodDates.collect { calendarView.notifyCalendarChanged() }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is CalendarEvent.ReportReady -> handleReportReady(event.report, event.format)
                            is CalendarEvent.Error ->
                                Toast.makeText(requireContext(), event.message, Toast.LENGTH_SHORT).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    /**
     * Invite (une seule fois) l'utilisatrice à renseigner ses cycles dans ses préférences.
     * Ne se ré-affiche pas à chaque ouverture de l'onglet pour ne pas gêner la grille.
     */
    private fun promptCycleProfileIfNeeded() {
        if (userStore.hasCycleProfile() || userStore.hasSeenCyclePrompt()) return
        userStore.setCyclePromptSeen()
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.cycle_profile_title)
            .setMessage(R.string.cycle_invite_message)
            .setPositiveButton(R.string.cycle_invite_configure) { _, _ ->
                startActivity(Intent(requireContext(), PreferencesActivity::class.java))
            }
            .setNegativeButton(R.string.cycle_invite_later, null)
            .show()
    }

    /** Configure l'apparence des cellules et le tap (ouverture de l'écran détail). */
    private fun initCalendar() {
        setupDaysOfWeek()

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val date = data.date
                val painLevel = viewModel.dailyStatus.value[date]
                val inMonth = data.position == DayPosition.MonthDate

                container.textView.text = date.dayOfMonth.toString()

                // Marqueur de règles : plein pour le réel, en contour pour le prévu.
                val realPeriod = inMonth && date in viewModel.periodDates.value
                val predictedPeriod = inMonth && !realPeriod && date in viewModel.predictedPeriodDates.value
                when {
                    realPeriod -> {
                        container.periodMarker.isVisible = true
                        container.periodMarker.setBackgroundResource(R.drawable.bg_period_marker)
                    }
                    predictedPeriod -> {
                        container.periodMarker.isVisible = true
                        container.periodMarker.setBackgroundResource(R.drawable.bg_period_marker_predicted)
                    }
                    else -> container.periodMarker.isVisible = false
                }

                // Teinte de fond selon la qualité de la journée (case entièrement colorée)
                val tintRes = when {
                    !inMonth || painLevel == null -> android.R.color.transparent
                    painLevel >= 7 -> R.color.day_bad
                    painLevel >= 4 -> R.color.day_medium
                    else -> R.color.day_good
                }
                container.tile.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(requireContext(), tintRes))

                // Description pour lecteur d'écran : numéro + qualité + règles (sinon TalkBack ne
                // lit que le numéro, et l'info portée par la couleur est perdue).
                container.view.contentDescription =
                    buildDayDescription(date, inMonth, painLevel, realPeriod, predictedPeriod)

                // Contour du jour courant : le tap ouvre l'écran détail (pas de sélection persistante)
                val isToday = inMonth && date == LocalDate.now()
                container.tile.foreground =
                    if (isToday) ContextCompat.getDrawable(requireContext(), R.drawable.bg_ring_today) else null

                if (inMonth) {
                    container.view.alpha = 1f
                    container.textView.setTextColor(Color.BLACK)
                    container.textView.setTypeface(null, if (isToday) Typeface.BOLD else Typeface.NORMAL)
                } else {
                    container.view.alpha = 0.3f
                }
            }
        }

        calendarView.setup(YearMonth.now().minusMonths(12), YearMonth.now().plusMonths(12), DayOfWeek.MONDAY)
        calendarView.scrollToMonth(YearMonth.now())
        calendarView.monthScrollListener = { updateMonthTitle(it.yearMonth) }
    }

    override fun onResume() {
        super.onResume()
        // La grille se met à jour toute seule (Flow). On réévalue seulement le prévisionnel,
        // car il dépend des préférences (profil/durées) qui ont pu changer hors base.
        viewModel.refreshForecast()
    }

    /** Génère les titres des jours de la semaine (LUN, MAR, …). */
    private fun setupDaysOfWeek() {
        val titlesContainer = requireView().findViewById<LinearLayout>(R.id.titlesContainer)
        if (titlesContainer.isNotEmpty()) titlesContainer.removeAllViews()
        daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY).forEach { dayOfWeek ->
            val textView = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRANCE).uppercase()
            }
            titlesContainer.addView(textView)
        }
    }

    /** Description accessible d'une case : « 12, journée difficile, règles ». */
    private fun buildDayDescription(
        date: LocalDate,
        inMonth: Boolean,
        painLevel: Int?,
        realPeriod: Boolean,
        predictedPeriod: Boolean
    ): String {
        val parts = mutableListOf(date.dayOfMonth.toString())
        if (inMonth && painLevel != null) {
            parts.add(
                getString(
                    when {
                        painLevel >= 7 -> R.string.day_quality_bad
                        painLevel >= 4 -> R.string.day_quality_medium
                        else -> R.string.day_quality_good
                    }
                )
            )
        }
        when {
            realPeriod -> parts.add(getString(R.string.day_cd_period))
            predictedPeriod -> parts.add(getString(R.string.day_cd_period_predicted))
        }
        return parts.joinToString(", ")
    }

    private fun updateMonthTitle(yearMonth: YearMonth) {
        monthText.text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.FRANCE).replaceFirstChar { it.uppercase() }} ${yearMonth.year}"
    }

    /** Conteneur d'une cellule de jour. Un tap ouvre l'écran détail de la journée. */
    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val tile: View = view.findViewById(R.id.dayTile)
        val periodMarker: View = view.findViewById(R.id.periodMarker)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position != DayPosition.MonthDate) {
                    calendarView.scrollToMonth(YearMonth.from(day.date))
                } else {
                    startActivity(DayDetailActivity.intent(requireContext(), day.date))
                }
            }
        }
    }
}
