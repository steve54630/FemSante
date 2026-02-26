package com.audreyRetournayDiet.femSante.viewModels.alim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.PdfRessource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RessourceViewModel : ViewModel() {

    // Utilisation d'un StateFlow pour l'état de la vue
    // Pas d'underscore : on utilise un nom explicite pour l'état interne si nécessaire
    val uiState: StateFlow<List<PdfRessource>> = MutableStateFlow(
        listOf(
            PdfRessource("histamine", "histamine.pdf"),
            PdfRessource("gluten", "gluten.pdf"),
            PdfRessource("ebook", "ebook.pdf")
        )
    )

    // Flow pour gérer la navigation (One-shot event)
    val navigationEvent: MutableSharedFlow<String> = MutableSharedFlow()

    fun onRessourceClicked(id: String) {
        val ressource = uiState.value.find { it.id == id }

        if (ressource != null) {
            viewModelScope.launch {
                navigationEvent.emit(ressource.fileName)
            }
        }
    }
}