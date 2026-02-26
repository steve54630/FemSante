package com.audreyRetournayDiet.femSante.data.entities

sealed class ToolboxNavigationEvent {
    data class NavigateToPdf(val fileName: String) : ToolboxNavigationEvent()
}