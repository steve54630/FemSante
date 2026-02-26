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
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.viewModels.calendar.CalendarViewModel
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.core.view.isNotEmpty

@SuppressLint("SetTextI18n")
class CalendarActivity : AppCompatActivity() {

    private lateinit var oldSelectedDate: LocalDate
    private lateinit var calendarView: CalendarView
    private lateinit var monthText: TextView
    private lateinit var prevMonth: ImageButton
    private lateinit var nextMonth: ImageButton
    private lateinit var dailyViewSection: View
    private lateinit var userStore: UserStore
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private val viewModel: CalendarViewModel by viewModels {
        val database = DatabaseProvider.getDatabase(this)
        val repository = DailyRepository(database.dailyDao())
        CalendarViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_calendar)

        // Initialisation des vues
        calendarView = findViewById(R.id.calendarView)
        monthText = findViewById(R.id.monthText)
        prevMonth = findViewById(R.id.btnPrevMonth)
        nextMonth = findViewById(R.id.btnNextMonth)
        dailyViewSection = findViewById(R.id.dailyView)
        bottomSheetBehavior = BottomSheetBehavior.from(dailyViewSection)

        oldSelectedDate = LocalDate.now()

        userStore = UserStore(this)
        val userId = userStore.getUser()?.id ?: return

        // Init Data
        viewModel.initData(userId)

        updateMonthTitle(YearMonth.now())
        setupDaysOfWeek()

        // Observations Flow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.date.collect {
                        calendarView.notifyCalendarChanged(); updateUiState(
                        viewModel.entryResult.value
                    )
                    }
                }
                launch { viewModel.entryResult.collect { updateUiState(it) } }
                launch { viewModel.dailyStatus.collect { calendarView.notifyCalendarChanged() } }
            }
        }

        // Binder du calendrier
        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val date = data.date
                val painLevel = viewModel.dailyStatus.value[date]
                container.textView.text = date.dayOfMonth.toString()

                // Point aujourd'hui (Cyan) ou Douleur (RGB)
                if (date == LocalDate.now()) {
                    container.dotView.isVisible = true
                    container.dotView.backgroundTintList = ColorStateList.valueOf(Color.CYAN)
                } else if (painLevel != null) {
                    container.dotView.isVisible = true
                    val color = when {
                        painLevel >= 7 -> Color.RED
                        painLevel >= 4 -> Color.YELLOW
                        else -> Color.GREEN
                    }
                    container.dotView.backgroundTintList = ColorStateList.valueOf(color)
                } else {
                    container.dotView.isVisible = false
                }

                // Style de sélection
                if (data.position == DayPosition.MonthDate) {
                    container.view.alpha = 1f
                    val isSelected = date == viewModel.date.value
                    container.textView.setTextColor(if (isSelected) Color.RED else Color.BLACK)
                    container.textView.setTypeface(
                        null,
                        if (isSelected) Typeface.BOLD else Typeface.NORMAL
                    )
                } else {
                    container.view.alpha = 0.3f
                }
            }
        }

        calendarView.setup(
            YearMonth.now().minusMonths(12),
            YearMonth.now().plusMonths(12),
            DayOfWeek.MONDAY
        )
        calendarView.scrollToMonth(YearMonth.now())
        calendarView.monthScrollListener = { updateMonthTitle(it.yearMonth) }

        prevMonth.setOnClickListener {
            calendarView.findFirstVisibleMonth()
                ?.let { calendarView.scrollToMonth(it.yearMonth.minusMonths(1)) }
        }
        nextMonth.setOnClickListener {
            calendarView.findFirstVisibleMonth()
                ?.let { calendarView.scrollToMonth(it.yearMonth.plusMonths(1)) }
        }
    }

    private fun onDateSelected(date: LocalDate) {
        val userId = userStore.getUser()?.id ?: return
        viewModel.loadData(userId, date)
    }

    private fun updateUiState(entry: DailyEntryFull?) {
        val switcher = dailyViewSection.findViewById<ViewSwitcher>(R.id.dailyViewSwitcher)

        if (entry != null) {
            // MODE RÉSUMÉ (Affichage des données)
            if (switcher.displayedChild != 1) {
                switcher.displayedChild = 1
            }
            CalendarUtils.updateDailyView(switcher.currentView, entry)

            // Bouton Modifier
            switcher.currentView.findViewById<MaterialButton>(R.id.btnEdit)?.setOnClickListener {
                startActivity(Intent(this, EntryAddActivity::class.java).apply {
                    putExtra("ID", viewModel.entryResult.value!!.dailyEntry.id)
                    putExtra("isEditMode", true)
                })
            }

            // Bouton Supprimer
            switcher.currentView.findViewById<MaterialButton>(R.id.btnDelete)?.setOnClickListener {
                showDeleteConfirmation()
            }

        } else {
            // MODE VIDE (Création)
            switcher.displayedChild = 0
            val date = viewModel.date.value
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRANCE)
            dailyViewSection.findViewById<TextView>(R.id.tvEmptyDate)?.text =
                "Pas de suivi le ${date.format(formatter)}"

            dailyViewSection.findViewById<Button>(R.id.btnCreateEntry)?.setOnClickListener {
                startActivity(Intent(this, EntryAddActivity::class.java).apply {
                    putExtra("selectedDate", date.toString())
                })
            }
        }
    }

    private fun showDeleteConfirmation() {
        val currentEntry = viewModel.entryResult.value ?: return

        AlertDialog.Builder(this)
            .setTitle("Supprimer le suivi ?")
            .setMessage("Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                // On envoie l'objet complet au ViewModel pour qu'il récupère l'ID
                viewModel.deleteData(currentEntry)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Rafraîchissement forcé au retour d'une activité ou switch d'app
        val userId = userStore.getUser()?.id
        if (userId != null) {
            viewModel.initData(userId) // Recharge les pastilles
            viewModel.loadData(userId, viewModel.date.value) // Recharge le détail
        }
    }

    // --- Helpers UI ---

    private fun setupDaysOfWeek() {
        val titlesContainer = findViewById<LinearLayout>(R.id.titlesContainer)
        if (titlesContainer.isNotEmpty()) titlesContainer.removeAllViews()
        daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY).forEach { dayOfWeek ->
            val textView = TextView(this).apply {
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRANCE).uppercase()
            }
            titlesContainer.addView(textView)
        }
    }

    private fun updateMonthTitle(yearMonth: YearMonth) {
        monthText.text = "${
            yearMonth.month.getDisplayName(TextStyle.FULL, Locale.FRANCE)
                .replaceFirstChar { it.uppercase() }
        } ${yearMonth.year}"
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val textView: TextView = view.findViewById(R.id.calendarDayText)
        val dotView: View = view.findViewById(R.id.priorityDot)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position != DayPosition.MonthDate) {
                    calendarView.scrollToMonth(YearMonth.from(day.date))
                }
                if (viewModel.date.value != day.date) onDateSelected(day.date)
            }
        }
    }
}