package com.audreyRetournayDiet.femSante.viewModels.viewers

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.audreyRetournayDiet.femSante.data.entities.VideoUiState
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.VideoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

/**
 * ViewModel pilotant le lecteur vidéo interactif.
 *
 * ### Fonctionnalités :
 * 1. **Récupération hybride** : Supporte les URLs directes ou les appels API dynamiques.
 * 2. **Gestion d'état (UI State)** : Gère le plein écran, l'orientation et le chargement.
 * 3. **Support PDF** : Détecte si un document d'accompagnement doit être proposé à l'utilisatrice.
 */
class VideoViewModel(
    private val videoManager: VideoManager,
    private val videoData: HashMap<*, *>
) : ViewModel() {

    // État réactif de l'écran vidéo
    private val internalUiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = internalUiState.asStateFlow()

    init {
        loadVideoData()
    }

    /**
     * Initialise les métadonnées et récupère la source vidéo.
     */
    private fun loadVideoData() {
        val title = videoData["Title"]?.toString() ?: "Vidéo inconnue"
        val pdfParam = videoData["PDF"]?.toString() ?: "non"
        val existingUrl = videoData["URL"]?.toString()

        Timber.d("Initialisation vidéo : '$title' | PDF requis : $pdfParam")

        // Configuration de l'état initial (avec ou sans bouton PDF)
        internalUiState.value = VideoUiState(
            title = title,
            isLoading = true,
            isPdfVisible = pdfParam == "oui",
            pdfFileName = "$title.pdf"
        )

        // Cas 1 : L'URL est déjà présente dans les paramètres (optimisation)
        if (!existingUrl.isNullOrEmpty()) {
            Timber.i("Utilisation de l'URL directe fournie")
            internalUiState.value = internalUiState.value.copy(
                videoUri = existingUrl.toUri(),
                isLoading = false
            )
        }
        // Cas 2 : On doit demander l'URL au serveur
        else {
            Timber.d("Appel au VideoManager pour récupérer le flux : $title")
            videoManager.getVideoUrl(title) { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val url = result.data?.optString("url")
                        if (!url.isNullOrBlank()) {
                            Timber.i("URL récupérée avec succès.")
                            internalUiState.value = internalUiState.value.copy(
                                videoUri = url.toUri(),
                                isLoading = false
                            )
                        } else {
                            Timber.e("Succès API mais contenu vide pour : $title")
                            internalUiState.value = internalUiState.value.copy(isLoading = false)
                        }
                    }
                    is ApiResult.Failure -> {
                        Timber.e("Échec récupération vidéo : ${result.message}")
                        internalUiState.value = internalUiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }

    /**
     * Bascule entre le mode fenêtré et le mode plein écran.
     */
    fun toggleFullScreen() {
        val newState = !internalUiState.value.isFullScreen
        Timber.v("Mode plein écran : $newState")
        internalUiState.value = internalUiState.value.copy(isFullScreen = newState)
    }

    /**
     * Définit si la vidéo doit être affichée en format portrait (ex : format réseaux sociaux).
     */
    fun setPortraitMode(isPortrait: Boolean) {
        Timber.v("Orientation portrait forcée : $isPortrait")
        internalUiState.value = internalUiState.value.copy(isPortraitVideo = isPortrait)
    }

    class Factory(
        private val videoManager: VideoManager,
        private val data: HashMap<*, *>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VideoViewModel(videoManager, data) as T
        }
    }
}