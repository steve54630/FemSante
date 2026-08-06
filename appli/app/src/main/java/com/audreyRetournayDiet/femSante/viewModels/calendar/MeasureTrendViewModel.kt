package com.audreyRetournayDiet.femSante.viewModels.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.dto.MeasurementHistoryRow
import com.audreyRetournayDiet.femSante.shared.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Charge l'historique des mesures (local) pour l'écran « Mon évolution ». L'activité
 * construit ensuite la série de la métrique sélectionnée à partir de ces lignes.
 */
@HiltViewModel
class MeasureTrendViewModel @Inject constructor(
    private val repository: DailyRepository,
    private val userStore: UserStore
) : ViewModel() {

    private val _history = MutableStateFlow<List<MeasurementHistoryRow>>(emptyList())
    val history = _history.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val userId = userStore.getUser()?.id ?: return@launch
            val result = repository.getMeasurementHistory(userId)
            if (result is ApiResult.Success) _history.value = result.data!!
        }
    }
}
