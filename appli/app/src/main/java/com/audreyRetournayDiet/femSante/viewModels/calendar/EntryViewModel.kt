package com.audreyRetournayDiet.femSante.viewModels.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.entity.*
import com.audreyRetournayDiet.femSante.room.type.*
import com.audreyRetournayDiet.femSante.viewModels.calendar.event.EntryEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class EntryViewModel(private val repository: DailyRepository) : ViewModel() {

    private val tag = "VM_ENTRY"

    private val eventChannel = MutableSharedFlow<EntryEvent>()
    val events = eventChannel.asSharedFlow()

    private val _editChannel = MutableStateFlow(value = false)
    val edit = _editChannel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())

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

    fun setDate(date: LocalDate) {
        Log.d(tag, "Date du formulaire fixée sur : $date")
        _selectedDate.value = date
    }

    fun setEdit(edit: Boolean) {
        Log.d(tag, "Mode édition activé : $edit")
        _editChannel.value = edit
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
            diet = diet ?: ""
        )
    }

    fun updateSymptomState(pains: List<PainZone>, nausea: Boolean, notes: String?) {
        _symptomState.value = _symptomState.value.copy(
            localizedPains = pains,
            hasNausea = nausea,
            others = notes
        )
    }

    fun saveAllData(userID: String, id : Long?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val dateMillis = _selectedDate.value
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                Log.i(tag, "Lancement sauvegarde - Mode: ${if (edit.value) "UPDATE (ID: $id)" else "INSERT"} | Date: ${_selectedDate.value}")

                val result = if (!edit.value) {
                    repository.saveCompleteEntry(
                        userId = userID,
                        date = dateMillis,
                        general = _generalState.value,
                        context = _contextState.value,
                        psy = _psychologicalState.value,
                        symptom = _symptomState.value
                    )
                } else {
                    repository.updateCompleteEntry(
                        userId = userID,
                        id = id!!,
                        general = _generalState.value,
                        context = _contextState.value,
                        psy = _psychologicalState.value,
                        symptom = _symptomState.value
                    )
                }

                _isLoading.value = false
                when (result) {
                    is ApiResult.Success -> {
                        Log.i(tag, "Données enregistrées avec succès en BDD")
                        eventChannel.emit(EntryEvent.Success)
                    }
                    is ApiResult.Failure -> {
                        Log.e(tag, "Échec de sauvegarde : ${result.message}")
                        eventChannel.emit(EntryEvent.Error(result.message))
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e(tag, "Exception critique lors de la sauvegarde", e)
                eventChannel.emit(EntryEvent.Error(e.localizedMessage ?: "Erreur de sauvegarde"))
            }
        }
    }

    fun loadExistingData(userId: String, id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d(tag, "Chargement des données existantes pour ID technique : $id")

            val result = repository.getDailyEntryByID(userId, id)

            if (result is ApiResult.Success && result.data != null) {
                val data = result.data
                _generalState.value = data.generalState ?: GeneralStateEntity(entryId = 0L)
                _psychologicalState.value = data.psychologicalState ?: PsychologicalStateEntity(entryId = 0L)
                _symptomState.value = data.symptomsState ?: SymptomStateEntity(entryId = 0L)
                _contextState.value = data.contextState ?: ContextStateEntity(entryId = 0L)
                Log.i(tag, "Formulaire pré-rempli avec les données de l'ID : $id")
            } else {
                Log.w(tag, "Aucune donnée trouvée pour l'ID $id, remise à zéro des états")
                resetStates()
            }
            _isLoading.value = false
        }
    }

    private fun resetStates() {
        Log.d(tag, "Reset complet des états du formulaire")
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