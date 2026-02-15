package com.audreyRetournayDiet.femSante.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class EntryViewModel(private val repository: DailyRepository) : ViewModel() {

    private val eventChannel = MutableSharedFlow<EntryEvent>()
    val events = eventChannel.asSharedFlow()
    val contextState = MutableStateFlow(ContextStateEntity())
    val generalState = MutableStateFlow(GeneralStateEntity())
    val psychologicalState = MutableStateFlow(PsychologicalStateEntity())
    val symptomState = MutableStateFlow(SymptomStateEntity())

    fun updateGeneralState(pain: Int, tired: Boolean) {
        generalState.value = generalState.value.copy(
            painLevel = pain, isTired = tired
        )
    }

    fun updateContextState(
        activity: PhysicalActivity,
        medicine: Boolean,
        medications: List<String>,
        diet: String?,
    ) {
        contextState.value = contextState.value.copy(
            physicalActivity = activity,
            tookMedication = medicine,
            medicationList = medications,
            dietNotes = diet
        )
    }

    fun updatePsychologicalState(quality: DayQuality, cause: DifficultyCause?, autres: String?) {
        psychologicalState.value = psychologicalState.value.copy(
            dayQuality = quality, difficultyCauses = cause, autres = autres
        )
    }

    fun updateSymptomState(pains: List<String>, nausea: Boolean, notes: String?) {
        symptomState.value = symptomState.value.copy(
            localizedPains = pains, hasNausea = nausea, others = notes
        )
    }

    fun saveAllDate(userID: String) {
        viewModelScope.launch {
            val currentGeneral = generalState.value
            val currentContext = contextState.value
            val currentSymptom = symptomState.value
            val currentPsychological = psychologicalState.value
            val date =
                LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val repository = repository.saveCompleteEntry(
                userID,
                date,
                currentGeneral,
                currentContext,
                currentPsychological,
                currentSymptom
            )

            when (repository) {
                is ApiResult.Success -> {
                    eventChannel.emit(EntryEvent.Success)
                }

                is ApiResult.Failure -> {
                    eventChannel.emit(EntryEvent.Error(repository.message))
                }
            }
        }
    }

    fun loadExistingData(userId: String, date: Long) {
        viewModelScope.launch {
            // On récupère l'entrée complète via ta fonction getFullEntry
            val existingEntry =
                repository.getDailyEntry(userId, LocalDate.ofEpochDay(date / 86400000))

            if (existingEntry is ApiResult.Success && existingEntry.data != null) {
                val data = existingEntry.data
                // On met à jour nos StateFlow. L'UI (Fragments) va se mettre à jour toute seule !
                generalState.value = data.generalState
                psychologicalState.value = data.psychologicalState
                symptomState.value = data.symptomsState
                contextState.value = data.contextState
            }
        }
    }

}

sealed class EntryEvent {
    object Success : EntryEvent()
    data class Error(val message: String) : EntryEvent()
}