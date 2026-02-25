package com.audreyRetournayDiet.femSante.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.entity.*
import com.audreyRetournayDiet.femSante.room.type.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class EntryViewModel(private val repository: DailyRepository) : ViewModel() {

    private val eventChannel = MutableSharedFlow<EntryEvent>()
    val events = eventChannel.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    // --- INITIALISATION DES ÉTATS (PROPRE) ---
    // On passe entryId = 0L car il sera généré par la DB lors du save.

    private val _generalState = MutableStateFlow(GeneralStateEntity(entryId = 0L))
    val generalState = _generalState.asStateFlow()

    private val _contextState = MutableStateFlow(
        ContextStateEntity(
            entryId = 0L,
            physicalActivity = PhysicalActivity.REPOS,
            medicationList = "",
            diet = ""
        )
    )
    val contextState = _contextState.asStateFlow()

    private val _psychologicalState = MutableStateFlow(
        PsychologicalStateEntity(entryId = 0L, dayQuality = DayQuality.MOYENNE)
    )
    val psychologicalState = _psychologicalState.asStateFlow()

    private val _symptomState = MutableStateFlow(SymptomStateEntity(entryId = 0L))
    val symptomState = _symptomState.asStateFlow()

    // --- LOGIQUE MÉTIER ---

    fun setDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun updateGeneralState(pain: Int, tired: Boolean) {
        _generalState.value = _generalState.value.copy(painLevel = pain, isTired = tired)
    }

    fun updatePsychologicalState(quality: DayQuality, cause: List<DifficultyCause>, autres: String?) {
        _psychologicalState.value = _psychologicalState.value.copy(
            dayQuality = quality,
            difficultyCauses = cause,
            autres = autres
        )
    }

    fun updateContextState(activity: PhysicalActivity, medicine: Boolean, medications: String, diet: String?) {
        _contextState.value = _contextState.value.copy(
            physicalActivity = activity,
            medecineTaken = medicine,
            medicationList = medications,
            diet = diet ?: "" // On s'assure que ce n'est pas null pour l'entité
        )
    }

    fun updateSymptomState(pains: List<PainZone>, nausea: Boolean, notes: String?) {
        _symptomState.value = _symptomState.value.copy(
            localizedPains = pains,
            hasNausea = nausea,
            others = notes
        )
    }

    fun saveAllData(userID: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dateMillis = _selectedDate.value
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                // On envoie les entités telles quelles au repository
                val result = repository.saveCompleteEntry(
                    userId = userID,
                    date = dateMillis,
                    general = _generalState.value,
                    context = _contextState.value,
                    psy = _psychologicalState.value,
                    symptom = _symptomState.value
                )

                _isLoading.value = false
                when (result) {
                    is ApiResult.Success -> eventChannel.emit(EntryEvent.Success)
                    is ApiResult.Failure -> eventChannel.emit(EntryEvent.Error(result.message))
                }
            } catch (e: Exception) {
                _isLoading.value = false
                e.printStackTrace()
                eventChannel.emit(EntryEvent.Error(e.localizedMessage ?: "Erreur de sauvegarde"))
            }
        }
    }

    fun loadExistingData(userId: String, date: LocalDate) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getDailyEntry(userId, date)

            if (result is ApiResult.Success && result.data != null) {
                val data = result.data
                // Si les données existent, on peuple les StateFlow
                // Note: On utilise le Elvis operator pour parer à toute nullité du DTO
                _generalState.value = data.generalState ?: GeneralStateEntity(entryId = 0L)
                _psychologicalState.value = data.psychologicalState ?: PsychologicalStateEntity(entryId = 0L)
                _symptomState.value = data.symptomsState ?: SymptomStateEntity(entryId = 0L)
                _contextState.value = data.contextState ?: ContextStateEntity(entryId = 0L)
            } else {
                resetStates()
            }
            _isLoading.value = false
        }
    }

    private fun resetStates() {
        _generalState.value = GeneralStateEntity(entryId = 0L)
        _psychologicalState.value = PsychologicalStateEntity(entryId = 0L, dayQuality = DayQuality.MOYENNE)
        _symptomState.value = SymptomStateEntity(entryId = 0L)
        _contextState.value = ContextStateEntity(
            entryId = 0L,
            physicalActivity = PhysicalActivity.REPOS,
            medicationList = "",
            diet = ""
        )
    }

    class Factory(private val repository: DailyRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EntryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EntryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class EntryEvent {
    object Success : EntryEvent()
    data class Error(val message: String) : EntryEvent()
}