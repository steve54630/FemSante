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

class AudioViewModel(
    private val api: VideoManager,
    private val initialTitle: String,
    private val exerciseMap: ArrayList<*>
) : ViewModel() {

    private val internalUiState = MutableStateFlow(
        AudioUiState(mainTitle = initialTitle, exercises = exerciseMap)
    )
    val uiState: StateFlow<AudioUiState> = internalUiState.asStateFlow()

    fun onExerciseSelected(exerciseName: String) {
        viewModelScope.launch {
            // 1. On affiche le chargement
            internalUiState.value = internalUiState.value.copy(isLoading = true)

            api.getVideoUrl(exerciseName) { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val url = result.data?.getString("url")
                        internalUiState.value = internalUiState.value.copy(
                            currentAudioUri = url?.toUri(),
                            isPlayerVisible = true,
                            isLoading = false, // 2. Succès : on coupe
                            errorMessage = null
                        )
                    }
                    is ApiResult.Failure -> {
                        internalUiState.value = internalUiState.value.copy(
                            isLoading = false, // 2. Erreur : on coupe aussi
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun onNothingSelected() {
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
            return AudioViewModel(api, title, map) as T
        }
    }
}