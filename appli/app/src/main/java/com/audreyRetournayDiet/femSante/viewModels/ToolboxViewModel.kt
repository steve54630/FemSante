package com.audreyRetournayDiet.femSante.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.PdfNavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel gérant la "Boîte à Outils" (Fiches conseils PDF).
 * * ### Rôle :
 * 1. Faire la correspondance (mapping) entre un clic sur l'UI (ID) et un fichier physique.
 * 2. Émettre un événement de navigation vers le lecteur PDF de l'application.
 */
class ToolboxViewModel : ViewModel() {

    // Flux d'événements pour la navigation vers le PDF sélectionné
    private val navigationSharedFlow = MutableSharedFlow<PdfNavigationEvent>()
    val navigationEvent: SharedFlow<PdfNavigationEvent> = navigationSharedFlow.asSharedFlow()

    /**
     * Traite le clic sur une carte ou un bouton de la boîte à outils.
     * @param toolId Identifiant technique défini dans l'interface (ex: 1 pour massage).
     */
    fun onToolClicked(toolId: Int) {
        Timber.d("Outil cliqué avec l'ID : $toolId")

        // Mapping centralisé des ressources PDF
        val fileName = when (toolId) {
            1 -> "automassage_ventre.pdf"
            2 -> "bouillote.pdf"
            3 -> "douleurs_abdominales.pdf"
            4 -> "emotional_tempest.pdf"
            5 -> "emotional_tempest_oil.pdf"
            6 -> "infusion_digestion.pdf"
            7 -> "infusions_menstruations.pdf"
            else -> ""
        }

        if (fileName.isNotEmpty()) {
            Timber.i("Résolution de l'outil réussie : Envoi vers $fileName")
            viewModelScope.launch {
                // Émission de l'événement vers le Fragment/Activity
                navigationSharedFlow.emit(PdfNavigationEvent.NavigateToPdf(fileName))
            }
        } else {
            // Sécurité : évite une navigation vide si un ID est mal renseigné dans le XML
            Timber.e("Erreur de mapping : Aucun fichier PDF associé à l'ID technique $toolId")
        }
    }
}