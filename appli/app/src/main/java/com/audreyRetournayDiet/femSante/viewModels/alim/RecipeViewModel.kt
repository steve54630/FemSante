package com.audreyRetournayDiet.femSante.viewModels.alim

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.RecipeUiState
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel gérant l'affichage d'une liste de recettes et le détail d'une sélection.
 * * @param initialTitle Le titre de la catégorie (ex: "Mes Petits-Déjeuners").
 * @param recipeMap Dictionnaire associant le nom technique (clé) au nom affichable (valeur).
 * @param folderPath Chemin du dossier contenant les PDFs dans les Assets.
 * @param getResourceId Lambda permettant de récupérer dynamiquement l'ID d'une image.
 */
class RecipeViewModel(
    initialTitle: String,
    private val recipeMap: HashMap<String, String>,
    private val folderPath: String,
    private val getResourceId: (String) -> Int
) : ViewModel() {

    // État de l'UI : contient la liste des noms, le titre, et l'image de la recette sélectionnée.
    private val internalUiState = MutableStateFlow(
        RecipeUiState(title = initialTitle, recipeNames = recipeMap.values.toList())
    )
    val uiState: StateFlow<RecipeUiState> = internalUiState.asStateFlow()

    // Événement de navigation : émet le chemin du PDF à ouvrir.
    private val navigationSharedFlow = MutableSharedFlow<String>()
    val navigationEvent: SharedFlow<String> = navigationSharedFlow.asSharedFlow()

    // Stocke la clé technique de la recette actuellement sélectionnée.
    private var currentSearchKey: String? = null

    /**
     * Appelé quand l'utilisatrice clique sur une recette dans la liste.
     * Recherche l'image correspondante et met à jour l'état de l'écran.
     */
    fun onRecipeSelected(displayName: String) {
        Timber.d("Sélection de la recette : $displayName")

        // Retrouve la clé technique à partir du nom affiché
        val rawKey = recipeMap.filterValues { it == displayName }.keys.toString()
        val cleanKey = Utilitaires.cleanKey(rawKey)

        currentSearchKey = cleanKey

        // Tentative de récupération de l'image (drawable) portant le même nom que la clé
        val resId = getResourceId(cleanKey)

        if (resId == 0) {
            Timber.w("Aucune image trouvée pour la clé : $cleanKey")
        } else {
            Timber.d("Image chargée avec succès (ResID: $resId)")
        }

        // Mise à jour de l'état pour afficher l'image et le bouton PDF
        internalUiState.value = internalUiState.value.copy(
            isRecipeSelected = true,
            imageResourceId = resId
        )
    }

    /**
     * Déclenche l'événement pour ouvrir le fichier PDF associé à la recette.
     */
    fun onOpenPdfClicked() {
        currentSearchKey?.let { key ->
            val pdfPath = "$folderPath/$key.pdf"
            Timber.i("Demande d'ouverture du PDF : $pdfPath")

            viewModelScope.launch {
                navigationSharedFlow.emit(pdfPath)
            }
        } ?: Timber.e("Erreur : Aucune recette n'est sélectionnée.")
    }

    /**
     * Factory pour injecter dynamiquement les données reçues de l'écran précédent
     * et fournir l'accès aux ressources Android.
     */
    class Factory(
        private val context: Context,
        private val title: String,
        private val map: HashMap<String, String>,
        private val path: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecipeViewModel(title, map, path) { search ->
                // Recherche dynamique de l'ID de ressource par son nom
                context.resources.getIdentifier(search, "drawable", context.packageName)
            } as T
        }
    }
}