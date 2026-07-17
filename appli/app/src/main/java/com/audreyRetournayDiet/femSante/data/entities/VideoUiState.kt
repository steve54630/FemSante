package com.audreyRetournayDiet.femSante.data.entities

data class VideoUiState(
    val title: String = "",
    val videoUri: android.net.Uri? = null,
    val isPdfVisible: Boolean = false,
    val isFullScreen: Boolean = false,
    val isPortraitVideo: Boolean = true,
    val isLoading: Boolean = false,
    val pdfFileName: String = "",
    /** Message d'erreur à afficher (ex : abonnement requis, vidéo introuvable). */
    val errorMessage: String? = null,
    /** Session irrécupérable (refresh échoué) : l'UI doit rediriger vers le login. */
    val sessionExpired: Boolean = false
)
