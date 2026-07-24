package com.audreyRetournayDiet.femSante.features.calendar.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
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
import com.audreyRetournayDiet.femSante.features.main.PreferencesActivity
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.viewModels.calendar.CalendarViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
 * Le fragment ne porte plus de fiche intégrée : toute la saisie et le résumé du jour
 * vivent dans l'écran détail, ce qui garde la grille simple et sans état de sélection.
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

                // Contour du jour courant (plus de sélection persistante : le tap ouvre l'écran détail)
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
