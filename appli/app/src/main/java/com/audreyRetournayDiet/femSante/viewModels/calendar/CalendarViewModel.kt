package com.audreyRetournayDiet.femSante.viewModels.calendar

import android.util.Log
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalendarViewModel(private val repository: DailyRepository) : ViewModel() {

    private val tag = "VM_CALENDAR"

    private val _events = MutableSharedFlow<CalendarEvent>()

    val entryResult = MutableStateFlow<DailyEntryFull?>(null)
    val date = MutableStateFlow<LocalDate>(LocalDate.now())

    val dailyStatus = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())

    fun initData(userId: String) {
        viewModelScope.launch {
            Log.d(tag, "Initialisation du calendrier pour l'utilisateur : $userId")
            when (val result = repository.getCalendarStatus(userId)) {
                is ApiResult.Success -> {
                    val statusMap = result.data?.associate {
                        Instant.ofEpochMilli(it.date)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate() to it.painLevel
                    } ?: emptyMap()

                    Log.i(tag, "Calendrier initialisé : ${statusMap.size} jours avec données")
                    dailyStatus.value = statusMap
                }

                is ApiResult.Failure -> Log.e(tag, "Erreur initData : ${result.message}")
            }
        }
    }

    fun loadData(userId: String, selectedDate: LocalDate) {
        viewModelScope.launch {
            Log.d(tag, "Chargement des données pour la date : $selectedDate")
            date.value = selectedDate
            when (val result = repository.getDailyEntryByDate(userId, selectedDate)) {
                is ApiResult.Success -> {
                    entryResult.value = result.data
                    if (result.data == null) Log.d(tag, "Aucune entrée existante pour $selectedDate")
                }
                is ApiResult.Failure -> {
                    Log.e(tag, "Erreur lors du chargement de la date $selectedDate : ${result.message}")
                    entryResult.value = null
                }
            }
        }
    }

    fun deleteData(dailyEntryFull: DailyEntryFull) {
        viewModelScope.launch {
            try {
                val entryId = dailyEntryFull.dailyEntry.id
                val userId = dailyEntryFull.dailyEntry.userId
                val entryDate = Instant.ofEpochMilli(dailyEntryFull.dailyEntry.date)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                Log.i(tag, "Demande de suppression - ID: $entryId | Date: $entryDate")

                when (val result = repository.deleteEntry(userId, entryId)) {
                    is ApiResult.Success -> {
                        entryResult.value = null
                        val currentMap = dailyStatus.value.toMutableMap()
                        currentMap.remove(entryDate)
                        dailyStatus.value = currentMap

                        Log.i(tag, "Suppression réussie et mise à jour de la map calendrier")
                        _events.emit(CalendarEvent.DeleteSuccess)
                    }

                    is ApiResult.Failure -> {
                        Log.e(tag, "Échec de suppression BDD : ${result.message}")
                        _events.emit(CalendarEvent.Error(result.message))
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception lors de la suppression", e)
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