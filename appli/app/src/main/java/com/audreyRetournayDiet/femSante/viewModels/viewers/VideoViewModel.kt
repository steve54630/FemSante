package com.audreyRetournayDiet.femSante.viewModels.viewers

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.VideoUiState
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.VideoManager
import com.audreyRetournayDiet.femSante.shared.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel pilotant le lecteur vidéo interactif.
 *
 * ### Fonctionnalités :
 * 1. **Récupération hybride** : Supporte les URLs directes ou les appels API dynamiques.
 * 2. **Gestion d'état (UI State)** : Gère le plein écran, l'orientation et le chargement.
 * 3. **Support PDF** : Détecte si un document d'accompagnement doit être proposé à l'utilisatrice.
 */
@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoManager: VideoManager,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Métadonnées vidéo (Title/URL/PDF) issues de l'extra "map" de l'Intent, exposé par
    // le SavedStateHandle.
    private val videoData: HashMap<*, *> =
        savedStateHandle.get<HashMap<*, *>>("map") ?: hashMapOf<String, String>()

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
            requestUrl(title, isRetry = false)
        }
    }

    /**
     * Demande l'URL de streaming. Sur 401 (token mort), tente un rafraîchissement
     * silencieux puis **un seul** nouvel essai ; si le refresh échoue, signale la session
     * expirée à l'UI (redirection login).
     */
    private fun requestUrl(title: String, isRetry: Boolean) {
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
                    if (result.isAuthError && !isRetry) {
                        Timber.w("401 vidéo : tentative de rafraîchissement de session.")
                        viewModelScope.launch {
                            if (sessionManager.refreshToken()) {
                                requestUrl(title, isRetry = true)
                            } else {
                                internalUiState.value = internalUiState.value.copy(
                                    isLoading = false, sessionExpired = true
                                )
                            }
                        }
                    } else {
                        Timber.e("Échec récupération vidéo : ${result.message}")
                        internalUiState.value = internalUiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
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

}