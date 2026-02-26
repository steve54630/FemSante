package com.audreyRetournayDiet.femSante.viewModels.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalendarViewModel(private val repository: DailyRepository) : ViewModel() {

    // Pour notifier l'UI des succès/erreurs (Toasts, Navigation)
    private val _events = MutableSharedFlow<CalendarEvent>()

    val entryResult = MutableStateFlow<DailyEntryFull?>(null)
    val date = MutableStateFlow<LocalDate>(LocalDate.now())

    // Map pour les points de couleur sur le calendrier
    val dailyStatus = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())

    fun initData(userId: String) {
        viewModelScope.launch {
            when (val result = repository.getCalendarStatus(userId)) {
                is ApiResult.Success -> {
                    dailyStatus.value = result.data?.associate {
                        Instant.ofEpochMilli(it.date)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate() to it.painLevel
                    } ?: emptyMap()
                }

                is ApiResult.Failure -> Log.e("CalendarVM", result.message)
            }
        }
    }

    fun loadData(userId: String, selectedDate: LocalDate) {
        viewModelScope.launch {
            date.value = selectedDate
            when (val result = repository.getDailyEntrybyDate(userId, selectedDate)) {
                is ApiResult.Success -> entryResult.value = result.data
                is ApiResult.Failure -> {
                    Log.e("CalendarVM", result.message)
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

                // Appel au repository (assure-toi que deleteEntry accepte l'ID technique)
                when (val result = repository.deleteEntry(userId, entryId)) {
                    is ApiResult.Success -> {
                        // 1. On vide le résumé affiché
                        entryResult.value = null

                        // 2. On met à jour la map du calendrier pour enlever le point
                        val currentMap = dailyStatus.value.toMutableMap()
                        currentMap.remove(entryDate)
                        dailyStatus.value = currentMap

                        // 3. On informe la vue
                        _events.emit(CalendarEvent.DeleteSuccess)
                    }

                    is ApiResult.Failure -> {
                        _events.emit(CalendarEvent.Error(result.message))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

// Events spécifiques au calendrier
sealed class CalendarEvent {
    object DeleteSuccess : CalendarEvent()
    data class Error(val message: String) : CalendarEvent()
}