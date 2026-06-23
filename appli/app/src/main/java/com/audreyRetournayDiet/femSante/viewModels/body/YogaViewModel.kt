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
 * ViewModel dédié à la sélection des séances de Yoga.
 * * ### Rôle fonctionnel :
 * Proposer des parcours de Yoga ciblés selon l'état de l'utilisatrice (douleur, stress, niveau).
 * Il réutilise le canal de navigation [BodyNavigationEvent] pour lancer le lecteur vidéo.
 */
class YogaViewModel : ViewModel() {

    // Flux d'événements partagé pour déclencher les actions de l'interface
    private val navigationSharedFlow = MutableSharedFlow<BodyNavigationEvent>()
    val navigationEvent: SharedFlow<BodyNavigationEvent> = navigationSharedFlow.asSharedFlow()

    /**
     * Séance "SOS Douleur" :
     * Focus sur le soulagement des douleurs pelviennes ou lombaires (Yoga thérapeutique).
     */
    fun onFlowClicked() {
        Timber.i("Action : Sélection séance SOS Douleur")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("SOS Douleur", "non"))
        }
    }

    /**
     * Séance "Calme intérieur" :
     * Focus sur la réduction du cortisol et l'apaisement du système nerveux.
     */
    fun onCalmClicked() {
        Timber.i("Action : Sélection séance Calme intérieur")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Calme intérieur", "non"))
        }
    }

    /**
     * Séance "Débutant au Yoga" :
     * Introduction aux postures de base (Asanas) pour les nouvelles pratiquantes.
     */
    fun onBeginnerClicked() {
        Timber.i("Action : Sélection séance Débutant au Yoga")
        viewModelScope.launch {
            navigationSharedFlow.emit(BodyNavigationEvent.LaunchVideo("Débutant au Yoga", "non"))
        }
    }
}