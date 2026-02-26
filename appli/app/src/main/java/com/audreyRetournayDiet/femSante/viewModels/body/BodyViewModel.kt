package com.audreyRetournayDiet.femSante.viewModels.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.BodyNavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BodyViewModel : ViewModel() {

    // Événements de navigation (uniques et non persistants)
    private val navigationSharedFlow = MutableSharedFlow<BodyNavigationEvent>()
    val navigationEvent: SharedFlow<BodyNavigationEvent> = navigationSharedFlow.asSharedFlow()

    fun onYogaClicked() {
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.NavigateToYoga)
        }
    }

    fun onPilatesClicked() {
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Pilates", "non"))
        }
    }

    fun onFitnessClicked() {
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Fitness", "non"))
        }
    }

}