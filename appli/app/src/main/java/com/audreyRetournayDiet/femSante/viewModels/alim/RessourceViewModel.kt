package com.audreyRetournayDiet.femSante.viewModels.alim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.PdfRessource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel gérant la liste des documents de référence (PDFs informatifs).
 * * ### Rôle :
 * 1. Fournir la liste des ressources disponibles (E-books, guides alimentaires).
 * 2. Gérer l'événement unique d'ouverture d'un document spécifique.
 */
class RessourceViewModel : ViewModel() {

    /**
     * État de l'UI contenant la liste des ressources.
     * Note : Ici la liste est codée en dur, mais elle pourrait provenir d'une API
     * ou d'une base de données locale dans une version future.
     */
    val uiState: StateFlow<List<PdfRessource>> = MutableStateFlow(
        listOf(
            PdfRessource("histamine", "histamine.pdf"),
            PdfRessource("gluten", "gluten.pdf"),
            PdfRessource("ebook", "ebook.pdf")
        )
    )

    /**
     * Flux d'événements pour la navigation vers le lecteur PDF.
     * Utilise un SharedFlow car l'ouverture d'un fichier est une action ponctuelle
     * qui ne doit pas être rejouée lors d'une rotation d'écran.
     */
    val navigationEvent: MutableSharedFlow<String> = MutableSharedFlow()

    /**
     * Appelé lorsqu'une carte de ressource est cliquée.
     * @param id L'identifiant unique de la ressource (ex: "histamine").
     */
    fun onRessourceClicked(id: String) {
        Timber.d("Clic sur la ressource ID : $id")

        // Recherche de l'objet correspondant dans l'état actuel
        val ressource = uiState.value.find { it.id == id }

        if (ressource != null) {
            Timber.i("Ressource trouvée : ${ressource.fileName}. Préparation de l'ouverture.")
            viewModelScope.launch {
                // On émet le nom du fichier PDF pour que l'Activity le charge
                navigationEvent.emit(ressource.fileName)
            }
        } else {
            Timber.e("Erreur : L'ID '$id' n'existe pas dans la liste des ressources.")
        }
    }
}