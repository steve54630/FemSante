package com.audreyRetournayDiet.femSante.features.calendar.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
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

    private val tag = "ACT_CALENDAR"
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
        Log.d(tag, "onCreate : Initialisation du calendrier")

        calendarView = findViewById(R.id.calendarView)
        monthText = findViewById(R.id.monthText)
        prevMonth = findViewById(R.id.btnPrevMonth)
        nextMonth = findViewById(R.id.btnNextMonth)
        dailyViewSection = findViewById(R.id.dailyView)
        bottomSheetBehavior = BottomSheetBehavior.from(dailyViewSection)

        userStore = UserStore(this)
        val userId = userStore.getUser()?.id

        if (userId == null) {
            Log.e(tag, "Utilisateur non connecté, impossible de charger le calendrier")
            finish()
            return
        }

        // Init Data
        viewModel.initData(userId)
        updateMonthTitle(YearMonth.now())
        setupDaysOfWeek()

        // Observations Flow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Changement de date sélectionnée
                launch {
                    viewModel.date.collect { date ->
                        Log.v(tag, "Date sélectionnée modifiée : $date")
                        calendarView.notifyCalendarChanged()
                        updateUiState(viewModel.entryResult.value)
                    }
                }
                // Mise à jour du contenu du jour
                launch {
                    viewModel.entryResult.collect { entry ->
                        Log.d(tag, "Observation : Détail du jour chargé (${if(entry != null) "Données" else "Vide"})")
                        updateUiState(entry)
                    }
                }
                // Mise à jour des pastilles (douleur)
                launch {
                    viewModel.dailyStatus.collect { status ->
                        Log.i(tag, "Mise à jour des pastilles : ${status.size} jours suivis trouvés")
                        calendarView.notifyCalendarChanged()
                    }
                }
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
                    container.textView.setTypeface(null, if (isSelected) Typeface.BOLD else Typeface.NORMAL)
                } else {
                    container.view.alpha = 0.3f
                }
            }
        }

        calendarView.setup(YearMonth.now().minusMonths(12), YearMonth.now().plusMonths(12), DayOfWeek.MONDAY)
        calendarView.scrollToMonth(YearMonth.now())
        calendarView.monthScrollListener = {
            Log.v(tag, "Mois affiché : ${it.yearMonth}")
            updateMonthTitle(it.yearMonth)
        }

        prevMonth.setOnClickListener { calendarView.scrollToMonth(calendarView.findFirstVisibleMonth()!!.yearMonth.minusMonths(1)) }
        nextMonth.setOnClickListener { calendarView.scrollToMonth(calendarView.findFirstVisibleMonth()!!.yearMonth.plusMonths(1)) }
    }

    private fun onDateSelected(date: LocalDate) {
        val userId = userStore.getUser()?.id ?: return
        Log.i(tag, "Clic utilisateur sur la date : $date")
        viewModel.loadData(userId, date)
    }

    private fun updateUiState(entry: DailyEntryFull?) {
        val switcher = dailyViewSection.findViewById<ViewSwitcher>(R.id.dailyViewSwitcher)

        if (entry != null) {
            if (switcher.displayedChild != 1) switcher.displayedChild = 1
            CalendarUtils.updateDailyView(switcher.currentView, entry)

            switcher.currentView.findViewById<MaterialButton>(R.id.btnEdit)?.setOnClickListener {
                Log.d(tag, "Navigation : Modifier l'entrée ID ${entry.dailyEntry.id}")
                startActivity(Intent(this, EntryAddActivity::class.java).apply {
                    putExtra("ID", entry.dailyEntry.id)
                    putExtra("isEditMode", true)
                    putExtra("selectedDate", entry.dailyEntry.date.toString())
                })
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
                Log.d(tag, "Navigation : Créer une entrée pour le $date")
                startActivity(Intent(this, EntryAddActivity::class.java).apply {
                    putExtra("selectedDate", date.toString())
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
            .setPositiveButton("Supprimer") { _, _ ->
                Log.w(tag, "Action : Suppression de l'entrée du ${currentEntry.dailyEntry.date}")
                viewModel.deleteData(currentEntry)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        Log.d(tag, "onResume : Rafraîchissement des données du calendrier")
        val userId = userStore.getUser()?.id
        if (userId != null) {
            viewModel.initData(userId)
            viewModel.loadData(userId, viewModel.date.value)
        }
    }

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