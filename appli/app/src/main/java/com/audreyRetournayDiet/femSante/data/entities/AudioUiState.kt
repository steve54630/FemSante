package com.audreyRetournayDiet.femSante.data.entities

data class AudioUiState(
    val mainTitle: String = "",
    val exercises: ArrayList<*> = ArrayList<Any>(),
    val currentAudioUri: android.net.Uri? = null,
    val isPlayerVisible: Boolean = false,
    val errorMessage: String? = null,
    val isLoading : Boolean = false
)