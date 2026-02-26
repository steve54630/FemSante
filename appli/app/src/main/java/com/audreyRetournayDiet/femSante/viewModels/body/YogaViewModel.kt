package com.audreyRetournayDiet.femSante.viewModels.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.BodyNavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class YogaViewModel : ViewModel() {

    private val navigationSharedFlow = MutableSharedFlow<BodyNavigationEvent>()
    val navigationEvent: SharedFlow<BodyNavigationEvent> = navigationSharedFlow.asSharedFlow()

    fun onFlowClicked() {
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("SOS Douleur", "non"))
        }
    }

    fun onCalmClicked() {
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Calme intérieur", "non"))
        }
    }

    fun onBeginnerClicked() {
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Débutant au Yoga", "non"))
        }
    }
}