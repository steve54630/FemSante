package com.audreyRetournayDiet.femSante.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.PdfNavigationEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ToolboxViewModel : ViewModel() {

    private val tag = "VM_TOOLBOX"

    private val navigationSharedFlow = MutableSharedFlow<PdfNavigationEvent>()
    val navigationEvent: SharedFlow<PdfNavigationEvent> = navigationSharedFlow.asSharedFlow()

    fun onToolClicked(toolId: Int) {
        Log.d(tag, "Outil cliqué avec l'ID : $toolId")

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
            Log.i(tag, "Résolution de l'outil réussie : Envoi vers $fileName")
            viewModelScope.launch {
                navigationSharedFlow.emit(PdfNavigationEvent.NavigateToPdf(fileName))
            }
        } else {
            // Cas critique : un clic est détecté mais aucun fichier n'est associé
            Log.e(tag, "Erreur de mapping : Aucun fichier PDF associé à l'ID technique $toolId")
        }
    }
}