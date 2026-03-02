package com.audreyRetournayDiet.femSante.viewModels.alim

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.PdfRessource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RessourceViewModel : ViewModel() {

    private val tag = "VM_RESSOURCE"

    val uiState: StateFlow<List<PdfRessource>> = MutableStateFlow(
        listOf(
            PdfRessource("histamine", "histamine.pdf"),
            PdfRessource("gluten", "gluten.pdf"),
            PdfRessource("ebook", "ebook.pdf")
        )
    )

    val navigationEvent: MutableSharedFlow<String> = MutableSharedFlow()

    fun onRessourceClicked(id: String) {
        Log.d(tag, "Clic sur la ressource ID : $id")

        val ressource = uiState.value.find { it.id == id }

        if (ressource != null) {
            Log.i(tag, "Ressource trouvée : ${ressource.fileName}. Émission de l'événement de navigation.")
            viewModelScope.launch {
                navigationEvent.emit(ressource.fileName)
            }
        } else {
            Log.e(tag, "Erreur critique : Aucune ressource correspondante à l'ID '$id' dans la liste actuelle.")
            // Optionnel : émettre une erreur vers l'UI si nécessaire
        }
    }
}