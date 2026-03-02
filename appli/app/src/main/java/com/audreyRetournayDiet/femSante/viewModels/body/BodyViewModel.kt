package com.audreyRetournayDiet.femSante.viewModels.body

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.BodyNavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel pilotant les interactions de l'écran "Corps & Mouvement".
 * * ### Rôle :
 * 1. Centraliser les clics utilisateur sur les différentes disciplines sportives.
 * 2. Émettre des événements de navigation ou d'action spécifiques (Yoga, Vidéos).
 */
class BodyViewModel : ViewModel() {

    // Flux d'événements unique pour toutes les actions de l'écran "Body"
    private val navigationSharedFlow = MutableSharedFlow<BodyNavigationEvent>()
    val navigationEvent: SharedFlow<BodyNavigationEvent> = navigationSharedFlow.asSharedFlow()

    /**
     * Gère le clic sur la section Yoga.
     * Redirige généralement vers un sous-menu de catégories (Doux, Dynamique, etc.).
     */
    fun onYogaClicked() {
        Timber.d("Clic : Navigation vers le menu Yoga")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.NavigateToYoga)
        }
    }

    /**
     * Gère le clic sur la section Pilates.
     * Déclenche directement le lancement d'une interface vidéo ou d'une playlist.
     */
    fun onPilatesClicked() {
        Timber.i("Clic : Lancement flux vidéo Pilates")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Pilates", "non"))
        }
    }

    /**
     * Gère le clic sur la section Fitness.
     * Identique au Pilates, mais avec des paramètres spécifiques à la discipline.
     */
    fun onFitnessClicked() {
        Timber.i("Clic : Lancement flux vidéo Fitness")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Fitness", "non"))
        }
    }
}