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

    private val viewModel: CalendarViewModel by viewModels {
        val database = DatabaseProvider.getDatabase(this)
        val repository = DailyRepository(database.dailyDao())
        CalendarViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_calendar)

        setupViews()
        initCalendar()
        collectStateFlows()
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
            }
        }
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
                val intent = Intent(this, EntryAddActivity::class.java).apply {
                    putExtra("ID", entry.dailyEntry.id)
                    putExtra("isEditMode", true)
                    putExtra("selectedDate", entry.dailyEntry.date.toString())
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