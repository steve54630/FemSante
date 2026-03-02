package com.audreyRetournayDiet.femSante.viewModels.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.viewModels.calendar.event.CalendarEvent
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
class CalendarViewModel(private val repository: DailyRepository) : ViewModel() {

    private val _events = MutableSharedFlow<CalendarEvent>()

    // Résultat détaillé de la journée sélectionnée
    val entryResult = MutableStateFlow<DailyEntryFull?>(null)

    // Date actuellement affichée ou sélectionnée par l'utilisatrice
    val date = MutableStateFlow<LocalDate>(LocalDate.now())

    /** * Map utilisée par le composant Calendrier pour afficher les pastilles de couleur.
     * Associe une date à un niveau de douleur (0-10).
     */
    val dailyStatus = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())

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

    class Factory(private val repository: DailyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
    }
}