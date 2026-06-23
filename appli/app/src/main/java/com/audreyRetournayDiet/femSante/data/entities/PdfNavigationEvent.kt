package com.audreyRetournayDiet.femSante.data.entities

sealed class PdfNavigationEvent {
    data class NavigateToPdf(val fileName: String) : PdfNavigationEvent()
}