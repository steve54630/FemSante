package com.audreyRetournayDiet.femSante.viewModels.body

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.BodyNavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class YogaViewModel : ViewModel() {

    private val tag = "VM_YOGA"

    private val navigationSharedFlow = MutableSharedFlow<BodyNavigationEvent>()
    val navigationEvent: SharedFlow<BodyNavigationEvent> = navigationSharedFlow.asSharedFlow()

    fun onFlowClicked() {
        Log.i(tag, "Action : Sélection séance SOS Douleur")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("SOS Douleur", "non"))
        }
    }

    fun onCalmClicked() {
        Log.i(tag, "Action : Sélection séance Calme intérieur")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Calme intérieur", "non"))
        }
    }

    fun onBeginnerClicked() {
        Log.i(tag, "Action : Sélection séance Débutant au Yoga")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Débutant au Yoga", "non"))
        }
    }
}