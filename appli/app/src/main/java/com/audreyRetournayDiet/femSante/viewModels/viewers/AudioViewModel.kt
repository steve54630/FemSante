package com.audreyRetournayDiet.femSante.viewModels.viewers

import android.util.Log
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

class AudioViewModel(
    private val api: VideoManager,
    initialTitle: String,
    exerciseMap: ArrayList<*>
) : ViewModel() {

    private val tag = "VM_AUDIO_PLAYER"

    private val internalUiState = MutableStateFlow(
        AudioUiState(mainTitle = initialTitle, exercises = exerciseMap)
    )
    val uiState: StateFlow<AudioUiState> = internalUiState.asStateFlow()

    fun onExerciseSelected(exerciseName: String) {
        Log.d(tag, "Exercice sélectionné : $exerciseName. Requête URL en cours...")

        viewModelScope.launch {
            // 1. On affiche le chargement
            internalUiState.value = internalUiState.value.copy(isLoading = true)

            api.getVideoUrl(exerciseName) { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val url = result.data?.optString("url")

                        if (!url.isNullOrBlank()) {
                            Log.i(tag, "URL récupérée avec succès pour '$exerciseName'. Préparation du lecteur.")
                            internalUiState.value = internalUiState.value.copy(
                                currentAudioUri = url.toUri(),
                                isPlayerVisible = true,
                                isLoading = false,
                                errorMessage = null
                            )
                        } else {
                            Log.e(tag, "Succès API mais URL vide ou manquante dans la réponse JSON")
                            internalUiState.value = internalUiState.value.copy(
                                isLoading = false,
                                errorMessage = "Lien de lecture invalide"
                            )
                        }
                    }
                    is ApiResult.Failure -> {
                        Log.e(tag, "Échec récupération URL pour '$exerciseName' : ${result.message}")
                        internalUiState.value = internalUiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun onNothingSelected() {
        Log.d(tag, "Réinitialisation du lecteur (fermeture/arrêt)")
        internalUiState.value = internalUiState.value.copy(
            isPlayerVisible = false,
            currentAudioUri = null
        )
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val api: VideoManager,
        private val title: String,
        private val map: ArrayList<*>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Log.d("VM_FACTORY", "Création AudioViewModel - Catégorie: $title")
            return AudioViewModel(api, title, map) as T
        }
    }
}