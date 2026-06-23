package com.audreyRetournayDiet.femSante.viewModels.calendar.event

sealed class EntryEvent {
    object Success : EntryEvent()
    data class Error(val message: String) : EntryEvent()
}