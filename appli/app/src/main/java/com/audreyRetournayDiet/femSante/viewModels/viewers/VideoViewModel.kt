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
import kotlin.collections.get

class VideoViewModel(
    private val videoManager: VideoManager, // On injecte le manager
    private val videoData: HashMap<*, *>
) : ViewModel() {

    private val internalUiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = internalUiState.asStateFlow()

    init {
        loadVideoData()
    }

    private fun loadVideoData() {
        val title = videoData["Title"]?.toString() ?: ""
        val pdfParam = videoData["PDF"]?.toString() ?: ""

        // On initialise avec le titre mais en mode "Loading"
        internalUiState.value = VideoUiState(
            title = title,
            isLoading = true,
            isPdfVisible = pdfParam == "oui",
            pdfFileName = "$title.pdf"
        )

        // Si l'URL est déjà dans la map, on l'utilise, sinon on appelle l'API
        val existingUrl = videoData["URL"]?.toString()

        if (!existingUrl.isNullOrEmpty()) {
            internalUiState.value = internalUiState.value.copy(
                videoUri = existingUrl.toUri(),
                isLoading = false
            )
        } else {
            // C'est ici que le chargement devient utile !
            videoManager.getVideoUrl(title) { result ->
                when (result) {
                    is ApiResult.Success -> {
                        internalUiState.value = internalUiState.value.copy(
                            videoUri = result.data?.getString("url")?.toUri(),
                            isLoading = false
                        )
                    }
                    is ApiResult.Failure -> {
                        internalUiState.value = internalUiState.value.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun toggleFullScreen() {
        internalUiState.value = internalUiState.value.copy(
            isFullScreen = !internalUiState.value.isFullScreen
        )
    }

    fun setPortraitMode(isPortrait: Boolean) {
        internalUiState.value = internalUiState.value.copy(
            isPortraitVideo = isPortrait
        )
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