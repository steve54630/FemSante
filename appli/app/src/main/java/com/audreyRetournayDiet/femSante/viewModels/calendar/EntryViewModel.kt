package com.audreyRetournayDiet.femSante.viewmodels.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.entity.*
import com.audreyRetournayDiet.femSante.room.type.*
import com.audreyRetournayDiet.femSante.viewmodels.calendar.event.EntryEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * ViewModel gérant le formulaire de saisie quotidienne (Entry).
 * * ### Architecture et Rôles :
 * 1. **Gestion d'États Granulaires** : Utilise un StateFlow par entité [General, Context, Psy, Symptom]
 * pour optimiser les recompositions UI.
 * 2. **Mode Hybride** : Gère nativement l'insertion (nouveau jour) et l'édition (mise à jour via ID).
 * 3. **Validation et Persistance** : Coordonne la sauvegarde multi-tables via le [DailyRepository].
 */
@HiltViewModel
class EntryViewModel @Inject constructor(
    private val repository: DailyRepository
) : ViewModel() {

    // Canal d'événements à usage unique (Succès/Erreur de navigation)
    private val eventChannel = MutableSharedFlow<EntryEvent>()
    val events = eventChannel.asSharedFlow()

    // Indicateur du mode (true = édition d'une entrée existante)
    private val _editChannel = MutableStateFlow(value = false)
    val edit = _editChannel.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    // --- États du Formulaire (Un StateFlow par section de l'entité DailyEntryFull) ---

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

    private val _measurementState = MutableStateFlow(BodyMeasurementEntity(entryId = 0L))
    val measurementState = _measurementState.asStateFlow()

    // --- setters de configuration ---

    fun setDate(date: LocalDate) {
        Timber.d("Date du formulaire fixée sur : $date")
        _selectedDate.value = date
    }

    fun setEdit(edit: Boolean) {
        Timber.d("Mode édition activé : $edit")
        _editChannel.value = edit
    }

    // --- Fonctions de mise à jour des états (Appelées par l'UI via Listeners/Binding) ---

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

    /**
     * Douleur par zone (carte du corps). On garde [SymptomStateEntity.localizedPains]
     * synchronisé sur les zones douloureuses (compat recos), et on **dérive** la douleur
     * globale ([GeneralStateEntity.painLevel]) = intensité maximale des zones, qui pilote
     * la coloration du calendrier et le mode SOS.
     */
    fun updatePainByZone(painByZone: Map<PainZone, Int>) {
        val maxLevel = painByZone.values.maxOrNull() ?: 0
        _symptomState.value = _symptomState.value.copy(
            painByZone = painByZone,
            localizedPains = painByZone.keys.toList()
        )
        _generalState.value = _generalState.value.copy(painLevel = maxLevel)
    }

    /** Symptômes hors douleur localisée (nausée, notes) — la douleur passe par la carte. */
    fun updateSymptomExtras(nausea: Boolean, notes: String?) {
        _symptomState.value = _symptomState.value.copy(hasNausea = nausea, others = notes)
    }

    /** Fatigue du jour (déplacée dans l'onglet « Moral & sommeil »). */
    fun updateTired(tired: Boolean) {
        _generalState.value = _generalState.value.copy(isTired = tired)
    }

    /** Sommeil : heures de coucher / réveil en minutes depuis minuit (null = non renseigné). */
    fun updateSleep(bedMinutes: Int?, wakeMinutes: Int?) {
        _generalState.value = _generalState.value.copy(
            bedTimeMinutes = bedMinutes,
            wakeTimeMinutes = wakeMinutes
        )
    }

    /** Journal de gratitude : le positif du jour. */
    fun updateGratitude(text: String?) {
        _psychologicalState.value = _psychologicalState.value.copy(gratitude = text)
    }

    /** Mesures corporelles (poids + tours), toutes optionnelles (null si non renseignées). */
    fun updateMeasurements(
        weight: Double?, waist: Double?, hips: Double?,
        thighs: Double?, chest: Double?, arms: Double?
    ) {
        _measurementState.value = _measurementState.value.copy(
            weightKg = weight, waistCm = waist, hipsCm = hips,
            thighsCm = thighs, chestCm = chest, armsCm = arms
        )
    }

    /**
     * Sauvegarde l'ensemble des données en base.
     * Bascule automatiquement entre 'save' (INSERT) et 'update' (UPDATE) selon l'état de [_editChannel].
     */
    fun saveAllData(userID: String, id : Long?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Conversion de la date locale en timestamp pour Room
                val dateMillis = _selectedDate.value
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                Timber.i("Lancement sauvegarde - Mode: ${if (edit.value) "UPDATE (ID: $id)" else "INSERT"} | Date: ${_selectedDate.value}")

                val result = if (!edit.value) {
                    repository.saveCompleteEntry(
                        userId = userID,
                        date = dateMillis,
                        general = _generalState.value,
                        context = _contextState.value,
                        psy = _psychologicalState.value,
                        symptom = _symptomState.value,
                        measurement = _measurementState.value
                    )
                } else {
                    repository.updateCompleteEntry(
                        userId = userID,
                        id = id!!,
                        general = _generalState.value,
                        context = _contextState.value,
                        psy = _psychologicalState.value,
                        symptom = _symptomState.value,
                        measurement = _measurementState.value
                    )
                }

                _isLoading.value = false
                when (result) {
                    is ApiResult.Success -> {
                        Timber.i("Données enregistrées avec succès en BDD")
                        eventChannel.emit(EntryEvent.Success)
                    }
                    is ApiResult.Failure -> {
                        Timber.e("Échec de sauvegarde : ${result.message}")
                        eventChannel.emit(EntryEvent.Error(result.message))
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Timber.e(e, "Exception critique lors de la sauvegarde")
                eventChannel.emit(EntryEvent.Error(e.localizedMessage ?: "Erreur de sauvegarde"))
            }
        }
    }

    /**
     * Charge les données d'une journée existante pour pré-remplir le formulaire.
     */
    fun loadExistingData(userId: String, id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            Timber.d("Chargement des données existantes pour ID technique : $id")

            val result = repository.getDailyEntryByID(userId, id)

            if (result is ApiResult.Success && result.data != null) {
                val data = result.data
                // Mapping des données récupérées vers les StateFlow locaux
                _generalState.value = data.generalState ?: GeneralStateEntity(entryId = 0L)
                _psychologicalState.value = data.psychologicalState ?: PsychologicalStateEntity(entryId = 0L)
                _symptomState.value = data.symptomsState ?: SymptomStateEntity(entryId = 0L)
                _contextState.value = data.contextState ?: ContextStateEntity(entryId = 0L)
                _measurementState.value = data.measurement ?: BodyMeasurementEntity(entryId = 0L)
                Timber.i("Formulaire pré-rempli avec les données de l'ID : $id")
            } else {
                Timber.w("Aucune donnée trouvée pour l'ID $id, remise à zéro des états")
                resetStates()
            }
            _isLoading.value = false
        }
    }

    /**
     * Réinitialise tous les champs du formulaire aux valeurs par défaut.
     */
    private fun resetStates() {
        Timber.d("Reset complet des états du formulaire")
        _generalState.value = GeneralStateEntity(entryId = 0L)
        _psychologicalState.value = PsychologicalStateEntity(entryId = 0L, dayQuality = DayQuality.MOYENNE)
        _symptomState.value = SymptomStateEntity(entryId = 0L)
        _contextState.value = ContextStateEntity(
            entryId = 0L,
            physicalActivity = PhysicalActivity.REPOS,
            medicationList = "",
            diet = ""
        )
        _measurementState.value = BodyMeasurementEntity(entryId = 0L)
    }
}