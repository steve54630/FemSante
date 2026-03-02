package com.audreyRetournayDiet.femSante.viewModels.body

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.BodyNavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class BodyViewModel : ViewModel() {

    private val tag = "VM_BODY"

    private val navigationSharedFlow = MutableSharedFlow<BodyNavigationEvent>()
    val navigationEvent: SharedFlow<BodyNavigationEvent> = navigationSharedFlow.asSharedFlow()

    fun onYogaClicked() {
        Log.d(tag, "Clic : Navigation vers le menu Yoga")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.NavigateToYoga)
        }
    }

    fun onPilatesClicked() {
        Log.i(tag, "Clic : Lancement flux vidéo Pilates")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Pilates", "non"))
        }
    }

    fun onFitnessClicked() {
        Log.i(tag, "Clic : Lancement flux vidéo Fitness")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Fitness", "non"))
        }
    }
}