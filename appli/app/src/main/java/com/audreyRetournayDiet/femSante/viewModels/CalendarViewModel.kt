package com.audreyRetournayDiet.femSante.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class CalendarViewModel(private val repository: DailyRepository) : ViewModel() {

    val entryResult = MutableStateFlow<DailyEntryFull?>(null)
    val date = MutableStateFlow<LocalDate>(LocalDate.now())
    val dailyStatus = MutableStateFlow<Map<LocalDate, Int>>(emptyMap())

    fun initData(userId: String) {
        viewModelScope.launch {
            when (val result = repository.getCalendarStatus(userId)) {
                is ApiResult.Success -> {
                    // On transforme la liste en Map ici si le Repo ne le fait pas déjà
                    dailyStatus.value = result.data!!.associate {
                        Instant.ofEpochMilli(it.date)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate() to it.painLevel
                    }
                }
                is ApiResult.Failure -> {
                    Log.e("CalendarVM", result.message)
                }
            }
        }
    }

    fun loadData(userId: String, selectedDate: LocalDate) {
        viewModelScope.launch {
            // On informe l'UI de la nouvelle date immédiatement
            date.value = selectedDate

            when (val result = repository.getDailyEntry(userId, selectedDate)) {
                is ApiResult.Success -> {
                    entryResult.value = result.data
                }
                is ApiResult.Failure -> {
                    Log.e("CalendarVM", result.message)
                    entryResult.value = null
                }
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