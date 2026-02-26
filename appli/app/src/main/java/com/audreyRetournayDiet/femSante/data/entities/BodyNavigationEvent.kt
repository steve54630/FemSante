package com.audreyRetournayDiet.femSante.data.entities

sealed class BodyNavigationEvent {
    object NavigateToYoga : BodyNavigationEvent()
    data class LaunchVideo(val category: String, val isPremium: String) : BodyNavigationEvent()
}