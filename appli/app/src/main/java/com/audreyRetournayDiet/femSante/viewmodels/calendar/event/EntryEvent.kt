package com.audreyRetournayDiet.femSante.viewmodels.calendar.event

sealed class EntryEvent {
    object Success : EntryEvent()
    data class Error(val message: String) : EntryEvent()
}