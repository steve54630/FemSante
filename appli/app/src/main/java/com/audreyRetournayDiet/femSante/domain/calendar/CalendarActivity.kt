package com.audreyRetournayDiet.femSante.domain.calendar

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.UserStore
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.viewModels.CalendarViewModel
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
import java.time.format.TextStyle
import java.util.Locale

@SuppressLint("SetTextI18n")
class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var monthText: TextView
    private lateinit var prevMonth: ImageButton
    private lateinit var nextMonth: ImageButton
    private lateinit var dailyViewSection: View
    private lateinit var userStore: UserStore

    private val viewModel: CalendarViewModel by viewModels {
        val database = DatabaseProvider.getDatabase(this)
        val repository = DailyRepository(database.dailyDao())
        CalendarViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_calendar)

        calendarView = findViewById(R.id.calendarView)
        monthText = findViewById(R.id.monthText)
        prevMonth = findViewById(R.id.btnPrevMonth)
        nextMonth = findViewById(R.id.btnNextMonth)
        dailyViewSection = findViewById(R.id.dailyView)

        userStore = UserStore(this)
        val userId = userStore.getUser()?.id ?: return

        Log.d("Calendar", "UserID: $userId")
        viewModel.initData(userId)
        val today = LocalDate.now()
        // On demande au ViewModel de bosser
        viewModel.loadData(userId, today)

        val currentMonth = YearMonth.now()
        updateMonthTitle(currentMonth)
        setupDaysOfWeek()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.date.collect {
                        calendarView.notifyCalendarChanged()
                        updateUiState(viewModel.entryResult.value)
                    }
                }

                launch {
                    viewModel.entryResult.collect { entry ->
                        Log.i("Calendar", "Entry = $entry")
                        updateUiState(entry)
                    }
                }

                launch {
                    viewModel.dailyStatus.collect {
                        calendarView.notifyCalendarChanged()
                    }
                }
            }

        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                val date = data.date
                val painLevel = viewModel.dailyStatus.value[date]
                container.textView.text = date.dayOfMonth.toString()

                if (date == LocalDate.now()) {
                    container.dotView.isVisible = true
                    container.dotView.backgroundTintList = ColorStateList.valueOf(Color.CYAN)
                } else if (painLevel != null) {
                    container.dotView.isVisible = true

                    // Logique de couleur selon la douleur
                    val color = when {
                        painLevel >= 7 -> Color.RED        // Grosse douleur
                        painLevel >= 4 -> Color.YELLOW     // Douleur moyenne
                        else -> Color.GREEN               // Tout va bien
                    }
                    container.dotView.backgroundTintList = ColorStateList.valueOf(color)
                } else {
                    container.dotView.isVisible = false
                }

                if (data.position == DayPosition.MonthDate) {
                    container.view.alpha = 1f
                    container.view.isEnabled = true
                    val isSelected = date == viewModel.date.value
                    container.textView.setTextColor(if (isSelected) Color.RED else Color.BLACK)
                    container.textView.setTypeface(
                        null,
                        if (isSelected) Typeface.BOLD else Typeface.NORMAL
                    )
                } else {
                    container.view.alpha = 0.3f
                    container.view.isEnabled = true
                }
            }
        }

        calendarView.setup(
            currentMonth.minusMonths(12),
            currentMonth.plusMonths(12),
            DayOfWeek.MONDAY
        )
        calendarView.scrollToMonth(currentMonth)

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
        val switcher =
            dailyViewSection.findViewById<android.widget.ViewSwitcher>(R.id.dailyViewSwitcher)

        if (entry != null) {
            switcher.displayedChild = 1
        } else {
            switcher.displayedChild = 0
            val date = viewModel.date.value
            Log.i("Calendar", "${viewModel.date.value}")
            val formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRANCE)
            dailyViewSection.findViewById<TextView>(R.id.tvEmptyDate)?.text =
                "Pas de suivi le ${date.format(formatter)}"
        }
    }

    private fun setupDaysOfWeek() {
        val titlesContainer = findViewById<LinearLayout>(R.id.titlesContainer)
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
        daysOfWeek.forEach { dayOfWeek ->
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
                val date = day.date
                if (day.position != DayPosition.MonthDate) {
                    calendarView.scrollToMonth(YearMonth.from(date))
                }
                if (viewModel.date.value != date) {
                    onDateSelected(date)
                }
            }
        }
    }
}