package com.audreyRetournayDiet.femSante.viewModels.viewers

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.audreyRetournayDiet.femSante.data.entities.AudioUiState
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.VideoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel pilotant le lecteur audio pour les exercices de relaxation/méditation.
 * * ### Rôle :
 * 1. Maintenir la liste des exercices disponibles pour une catégorie donnée.
 * 2. Récupérer dynamiquement l'URL de streaming depuis le serveur.
 * 3. Gérer les états visuels (ProgressBar, Lecteur, Messages d'erreur).
 */
class AudioViewModel(
    private val api: VideoManager,
    initialTitle: String,
    exerciseMap: ArrayList<*>
) : ViewModel() {

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

        viewModelScope.launch {
            // 1. Activation de l'indicateur de chargement
            internalUiState.value = internalUiState.value.copy(isLoading = true)

            // 2. Appel au VideoManager (qui gère aussi les flux audio)
            api.getVideoUrl(exerciseName) { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val url = result.data?.optString("url")

                        if (!url.isNullOrBlank()) {
                            Timber.i("Lien de lecture obtenu pour '$exerciseName'.")
                            // 3. Mise à jour de l'état avec l'URI de lecture
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

    /**
     * Factory pour instancier le ViewModel avec ses paramètres dynamiques.
     */
    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val api: VideoManager,
        private val title: String,
        private val map: ArrayList<*>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Timber.d("Création AudioViewModel - Catégorie: $title")
            return AudioViewModel(api, title, map) as T
        }
    }
}