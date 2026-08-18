package com.audreyRetournayDiet.femSante.viewmodels.viewers

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.AudioUiState
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
 * ViewModel pilotant le lecteur audio pour les exercices de relaxation/méditation.
 * * ### Rôle :
 * 1. Maintenir la liste des exercices disponibles pour une catégorie donnée.
 * 2. Récupérer dynamiquement l'URL de streaming depuis le serveur.
 * 3. Gérer les états visuels (ProgressBar, Lecteur, Messages d'erreur).
 */
@HiltViewModel
class AudioViewModel @Inject constructor(
    private val api: VideoManager,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Les paramètres (titre + playlist) proviennent des extras de l'Intent, exposés
    // automatiquement par le SavedStateHandle pour un ViewModel scoppé à l'Activity.
    private val initialTitle: String = savedStateHandle.get<String>("Titre") ?: ""
    private val exerciseMap: List<String> =
        savedStateHandle.get<ArrayList<String>>("map")?.map { it.toString() } ?: emptyList()

    // État unique de l'interface (Source de vérité)
    private val internalUiState = MutableStateFlow(
        AudioUiState(mainTitle = initialTitle, exercises = exerciseMap)
    )
    val uiState: StateFlow<AudioUiState> = internalUiState.asStateFlow()

    /**
     * Appelé quand l'utilisatrice clique sur un exercice de la liste.
     * Déclenche l'appel réseau pour obtenir le lien de streaming.
     */
    fun onExerciseSelected(exerciseName: String) {
        Timber.d("Exercice sélectionné : $exerciseName. Requête URL en cours...")
        internalUiState.value = internalUiState.value.copy(isLoading = true)
        requestUrl(exerciseName, isRetry = false)
    }

    /**
     * Demande l'URL de lecture. En cas de 401 (token mort), tente un rafraîchissement
     * silencieux puis **un seul** nouvel essai ; si le refresh échoue, signale la session
     * expirée à l'UI (redirection login).
     */
    private fun requestUrl(exerciseName: String, isRetry: Boolean) {
        api.getVideoUrl(exerciseName) { result ->
            when (result) {
                is ApiResult.Success -> {
                    val url = result.data?.optString("url")
                    if (!url.isNullOrBlank()) {
                        Timber.i("Lien de lecture obtenu pour '$exerciseName'.")
                        internalUiState.value = internalUiState.value.copy(
                            currentAudioUri = url.toUri(),
                            isPlayerVisible = true,
                            isLoading = false,
                            errorMessage = null
                        )
                    } else {
                        Timber.e("Erreur : Réponse API vide pour $exerciseName")
                        internalUiState.value = internalUiState.value.copy(
                            isLoading = false,
                            errorMessage = "Lien de lecture invalide"
                        )
                    }
                }
                is ApiResult.Failure -> {
                    if (result.isAuthError && !isRetry) {
                        Timber.w("401 audio : tentative de rafraîchissement de session.")
                        viewModelScope.launch {
                            if (sessionManager.refreshToken()) {
                                requestUrl(exerciseName, isRetry = true)
                            } else {
                                internalUiState.value = internalUiState.value.copy(
                                    isLoading = false, sessionExpired = true
                                )
                            }
                        }
                    } else {
                        Timber.e("Échec API : ${result.message}")
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
     * Réinitialise le lecteur et cache l'interface de lecture.
     */
    fun onNothingSelected() {
        Timber.d("Réinitialisation du lecteur (fermeture/arrêt)")
        internalUiState.value = internalUiState.value.copy(
            isPlayerVisible = false,
            currentAudioUri = null
        )
    }
}