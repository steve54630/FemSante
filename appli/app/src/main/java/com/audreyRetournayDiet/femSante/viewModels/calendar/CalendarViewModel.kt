package com.audreyRetournayDiet.femSante.viewModels.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.CycleRepository
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.type.FlowLevel
import com.audreyRetournayDiet.femSante.viewModels.calendar.event.CalendarEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * ViewModel gérant la logique du calendrier de suivi de santé.
 * * ### Architecture :
 * - **StateFlow (`dailyStatus`)** : Gère la "vue d'ensemble" (Map date → niveau de douleur).
 * - **StateFlow (`entryResult`)** : Contient l'objet complet [DailyEntryFull] de la journée sélectionnée.
 * - **SharedFlow (`_events`)** : Notifie l'UI des succès ou échecs de suppression.
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: DailyRepository,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<CalendarEvent>()

    // Résultat détaillé de la journée sélectionnée
    val entryResult = MutableStateFlow<DailyEntryFull?>(null)

    // Date actuellement affichée ou sélectionnée par l'utilisatrice
    val date = MutableStateFlow<LocalDate>(LocalDate.now())

    /** * Map utilisée par le composant Calendrier pour afficher les pastilles de couleur.
     * Associe une date à un niveau de douleur (0-10).
     */
    val dailyStatus = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())

    // --- Suivi de cycle ---

    /** Observation de cycle de la journée sélectionnée (null si rien de saisi). */
    val cycleDay = MutableStateFlow<CycleDayEntity?>(null)

    /** Dates de règles, pour le marqueur visuel du calendrier. */
    val periodDates = MutableStateFlow<Set<LocalDate>>(emptySet())

    /**
     * Charge tous les statuts (date + douleur) pour un utilisateur.
     * Utilisé pour "allumer" les jours renseignés dans le calendrier.
     */
    fun initData(userId: String) {
        viewModelScope.launch {
            Timber.d("Initialisation du calendrier pour l'utilisateur : $userId")
            when (val result = repository.getCalendarStatus(userId)) {
                is ApiResult.Success -> {
                    // Conversion du Long (timestamp) en LocalDate pour faciliter l'usage UI
                    val statusMap = result.data?.associate {
                        Instant.ofEpochMilli(it.date)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate() to it.painLevel
                    } ?: emptyMap()

                    Timber.i("Calendrier initialisé : ${statusMap.size} jours avec données")
                    dailyStatus.value = statusMap
                }
                is ApiResult.Failure -> Timber.e("Erreur initData : ${result.message}")
            }

            // Chargement des dates de règles pour le marqueur du calendrier
            when (val periodResult = cycleRepository.getPeriodDates(userId)) {
                is ApiResult.Success -> periodDates.value = periodResult.data ?: emptySet()
                is ApiResult.Failure -> Timber.e("Erreur chargement règles : ${periodResult.message}")
            }
        }
    }

    /**
     * Récupère l'intégralité des données (Symptômes, Psychologie, etc.) pour une date précise.
     */
    fun loadData(userId: String, selectedDate: LocalDate) {
        viewModelScope.launch {
            Timber.d("Chargement des données pour la date : $selectedDate")
            date.value = selectedDate
            when (val result = repository.getDailyEntryByDate(userId, selectedDate)) {
                is ApiResult.Success -> {
                    entryResult.value = result.data
                    if (result.data == null) Timber.d("Aucune entrée existante pour $selectedDate")
                }
                is ApiResult.Failure -> {
                    Timber.e("Erreur chargement date $selectedDate : ${result.message}")
                    entryResult.value = null
                }
            }

            // Chargement de l'observation de cycle pour la date sélectionnée
            when (val cycleResult = cycleRepository.getCycleDay(userId, selectedDate)) {
                is ApiResult.Success -> cycleDay.value = cycleResult.data
                is ApiResult.Failure -> {
                    Timber.e("Erreur chargement cycle $selectedDate : ${cycleResult.message}")
                    cycleDay.value = null
                }
            }
        }
    }

    /**
     * Enregistre l'observation de cycle de la journée sélectionnée puis rafraîchit
     * l'état local (observation du jour + dates de règles pour le marqueur).
     */
    fun saveCycleDay(
        userId: String,
        selectedDate: LocalDate,
        isPeriod: Boolean,
        flow: FlowLevel?,
        spotting: Boolean
    ) {
        viewModelScope.launch {
            when (val result = cycleRepository.saveCycleDay(userId, selectedDate, isPeriod, flow, spotting)) {
                is ApiResult.Success -> {
                    cycleDay.value = cycleRepository.getCycleDay(userId, selectedDate).let {
                        (it as? ApiResult.Success)?.data
                    }
                    val current = periodDates.value.toMutableSet()
                    if (isPeriod) current.add(selectedDate) else current.remove(selectedDate)
                    periodDates.value = current
                }
                is ApiResult.Failure -> Timber.e("Échec sauvegarde cycle : ${result.message}")
            }
        }
    }

    /**
     * Supprime l'entrée sélectionnée et met à jour l'état local immédiatement.
     */
    fun deleteData(dailyEntryFull: DailyEntryFull) {
        viewModelScope.launch {
            try {
                val entryId = dailyEntryFull.dailyEntry.id
                val userId = dailyEntryFull.dailyEntry.userId
                val entryDate = Instant.ofEpochMilli(dailyEntryFull.dailyEntry.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                when (val result = repository.deleteEntry(userId, entryId)) {
                    is ApiResult.Success -> {
                        // Nettoyage de l'UI
                        entryResult.value = null

                        // Mise à jour de la map globale pour retirer la couleur du calendrier
                        val currentMap = dailyStatus.value.toMutableMap()
                        currentMap.remove(entryDate)
                        dailyStatus.value = currentMap

                        _events.emit(CalendarEvent.DeleteSuccess)
                    }
                    is ApiResult.Failure -> _events.emit(CalendarEvent.Error(result.message))
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception lors de la suppression")
                _events.emit(CalendarEvent.Error("Erreur lors de la suppression"))
            }
        }
    }

}