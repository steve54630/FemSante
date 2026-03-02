package com.audreyRetournayDiet.femSante.viewModels.viewers

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.audreyRetournayDiet.femSante.data.entities.VideoUiState
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.VideoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VideoViewModel(
    private val videoManager: VideoManager,
    private val videoData: HashMap<*, *>
) : ViewModel() {

    private val tag = "VM_VIDEO_PLAYER"

    private val internalUiState = MutableStateFlow(VideoUiState())
    val uiState: StateFlow<VideoUiState> = internalUiState.asStateFlow()

    init {
        loadVideoData()
    }

    private fun loadVideoData() {
        val title = videoData["Title"]?.toString() ?: "Vidéo inconnue"
        val pdfParam = videoData["PDF"]?.toString() ?: "non"
        val existingUrl = videoData["URL"]?.toString()

        Log.d(tag, "Initialisation vidéo : '$title' | PDF requis : $pdfParam")

        // État initial
        internalUiState.value = VideoUiState(
            title = title,
            isLoading = true,
            isPdfVisible = pdfParam == "oui",
            pdfFileName = "$title.pdf"
        )

        if (!existingUrl.isNullOrEmpty()) {
            Log.i(tag, "Utilisation de l'URL directe fournie dans les paramètres")
            internalUiState.value = internalUiState.value.copy(
                videoUri = existingUrl.toUri(),
                isLoading = false
            )
        } else {
            Log.d(tag, "Aucune URL directe. Appel au VideoManager pour : $title")
            videoManager.getVideoUrl(title) { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val url = result.data?.optString("url")
                        if (!url.isNullOrBlank()) {
                            Log.i(tag, "URL récupérée avec succès pour la vidéo : $title")
                            internalUiState.value = internalUiState.value.copy(
                                videoUri = url.toUri(),
                                isLoading = false
                            )
                        } else {
                            Log.e(tag, "Réponse API succès mais URL vide pour : $title")
                            internalUiState.value = internalUiState.value.copy(isLoading = false)
                        }
                    }
                    is ApiResult.Failure -> {
                        Log.e(tag, "Erreur API lors de la récupération vidéo : ${result.message}")
                        internalUiState.value = internalUiState.value.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun toggleFullScreen() {
        val newState = !internalUiState.value.isFullScreen
        Log.v(tag, "Mode plein écran : $newState")
        internalUiState.value = internalUiState.value.copy(isFullScreen = newState)
    }

    fun setPortraitMode(isPortrait: Boolean) {
        Log.v(tag, "Orientation portrait forcée : $isPortrait")
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