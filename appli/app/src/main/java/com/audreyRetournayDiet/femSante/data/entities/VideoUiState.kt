package com.audreyRetournayDiet.femSante.data.entities

data class VideoUiState(
    val title: String = "",
    val videoUri: android.net.Uri? = null,
    val isPdfVisible: Boolean = false,
    val isFullScreen: Boolean = false,
    val isPortraitVideo: Boolean = true,
    val isLoading: Boolean = false,
    val pdfFileName: String = ""
)
