package com.audreyRetournayDiet.femSante.viewmodels.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.cycle.CyclePeriodForecaster
import com.audreyRetournayDiet.femSante.data.report.MedicalReportBuilder
import com.audreyRetournayDiet.femSante.data.report.ReportFormat
import com.audreyRetournayDiet.femSante.data.report.ReportPeriod
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.CycleRepository
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.type.FlowLevel
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.viewmodels.calendar.event.CalendarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel du calendrier de suivi. **Réactif** : la grille (couleurs, marqueurs de règles),
 * le résumé et la saisie cycle de la journée sélectionnée sont exposés via des `Flow` Room.
 * Toute écriture en base ré-émet automatiquement → l'UI se met à jour sans rechargement
 * manuel (`initData`/`loadData`/`onResume`).
 *
 * Seul le **prévisionnel** dépend aussi des préférences (profil, durées) hors base : il est
 * recalculé quand les vraies règles changent (réactif) et sur [refreshForecast] (retour des
 * préférences).
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: DailyRepository,
    private val cycleRepository: CycleRepository,
    private val userStore: UserStore
) : ViewModel() {

    private val _events = MutableSharedFlow<CalendarEvent>()
    /** Événements ponctuels (suppression, erreur, récap prêt). */
    val events: SharedFlow<CalendarEvent> = _events.asSharedFlow()

    private val userId: String = userStore.getUser()?.id ?: ""

    /** Journée observée (par l'écran détail). */
    private val selectedDate = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = selectedDate.asStateFlow()

    /** Map date→douleur pour la coloration de la grille (réactif). */
    val dailyStatus: StateFlow<Map<LocalDate, Int>> =
        repository.observeCalendarStatus(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 0, replayExpirationMillis = 0), emptyMap())

    /** Dates de règles réelles, pour les marqueurs (réactif). */
    val periodDates: StateFlow<Set<LocalDate>> =
        cycleRepository.observePeriodDates(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 0, replayExpirationMillis = 0), emptySet())

    /** Résumé complet de la journée sélectionnée (réactif). */
    val entryResult: StateFlow<DailyEntryFull?> =
        selectedDate
            .flatMapLatest { d -> repository.observeDailyEntry(userId, d) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 0, replayExpirationMillis = 0), null)

    /** Observation de cycle de la journée sélectionnée (réactif). */
    val cycleDay: StateFlow<CycleDayEntity?> =
        selectedDate
            .flatMapLatest { d -> cycleRepository.observeCycleDay(userId, d) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 0, replayExpirationMillis = 0), null)

    /**
     * Jours de règles **prédits** (incrément 3), calculés à la volée et jamais persistés.
     * Dépend des préférences (hors base) → recalculé réactivement quand les règles changent.
     */
    val predictedPeriodDates = MutableStateFlow<Set<LocalDate>>(emptySet())

    // Le prévisionnel est recalculé explicitement quand un écran devient visible
    // ([refreshForecast], appelé en onResume) et après chaque sauvegarde de cycle. On évite
    // un collecteur permanent sur periodDates qui empêcherait la re-requête au retour d'écran.

    /** Sélectionne la journée observée (résumé + saisie cycle). */
    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    /** Recalcule le prévisionnel avec le profil + durées courants (retour des préférences). */
    fun refreshForecast() {
        viewModelScope.launch { computeForecast() }
    }

    private suspend fun computeForecast() {
        val periods = (cycleRepository.getPeriodDates(userId) as? ApiResult.Success)?.data ?: emptySet()
        val recorded = (cycleRepository.getRecordedDates(userId) as? ApiResult.Success)?.data ?: emptySet()
        val predicted = CyclePeriodForecaster.predictPeriodDates(
            periodDates = periods,
            profile = userStore.getCycleProfile(),
            cycleLength = userStore.getCycleLength(),
            periodLength = userStore.getPeriodLength()
        )
        // On n'affiche pas comme "prévu" un jour déjà explicitement traité (confirmé/décoché).
        predictedPeriodDates.value = predicted - recorded
    }

    /**
     * Enregistre l'observation de cycle de la journée sélectionnée. Les Flow (cycleDay,
     * periodDates) se mettent à jour automatiquement ; on réévalue aussi le prévisionnel
     * ici pour couvrir la décoche d'un jour prédit (l'ensemble des règles ne change pas).
     */
    fun saveCycleDay(isPeriod: Boolean, flow: FlowLevel?, spotting: Boolean) {
        if (userId.isEmpty()) return
        viewModelScope.launch {
            cycleRepository.saveCycleDay(userId, selectedDate.value, isPeriod, flow, spotting)
            computeForecast()
        }
    }

    /**
     * Agrège le récap médical sur la période demandée (à partir d'aujourd'hui, en arrière) et
     * émet [CalendarEvent.ReportReady]. L'agrégation reste locale ; la mise en PDF et le
     * partage sont pris en charge par l'UI (qui a besoin du Context / FileProvider).
     */
    fun buildMedicalReport(period: ReportPeriod, format: ReportFormat) {
        if (userId.isEmpty()) {
            viewModelScope.launch { _events.emit(CalendarEvent.Error("Utilisatrice inconnue")) }
            return
        }
        viewModelScope.launch {
            try {
                val to = LocalDate.now()
                val from = to.minusMonths(period.months)
                val entries = (repository.getEntriesBetween(userId, from, to) as? ApiResult.Success)?.data ?: emptyList()
                val cycleDays = (cycleRepository.getCycleDaysBetween(userId, from, to) as? ApiResult.Success)?.data ?: emptyList()
                val report = MedicalReportBuilder.build(from, to, entries, cycleDays, userStore.getCycleProfile())
                _events.emit(CalendarEvent.ReportReady(report, format))
            } catch (e: Exception) {
                Timber.e(e, "Échec construction du récap médical")
                _events.emit(CalendarEvent.Error("Impossible de générer le récap"))
            }
        }
    }

    /** Supprime l'entrée. Le résumé et la grille se rafraîchissent via les Flow. */
    fun deleteData(dailyEntryFull: DailyEntryFull) {
        viewModelScope.launch {
            try {
                val entryId = dailyEntryFull.dailyEntry.id
                val uid = dailyEntryFull.dailyEntry.userId
                when (val result = repository.deleteEntry(uid, entryId)) {
                    is ApiResult.Success -> _events.emit(CalendarEvent.DeleteSuccess)
                    is ApiResult.Failure -> _events.emit(CalendarEvent.Error(result.message))
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception lors de la suppression")
                _events.emit(CalendarEvent.Error("Erreur lors de la suppression"))
            }
        }
    }
}
