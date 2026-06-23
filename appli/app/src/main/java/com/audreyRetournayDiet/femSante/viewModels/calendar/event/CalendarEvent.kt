package com.audreyRetournayDiet.femSante.viewModels.calendar.event

sealed class CalendarEvent {
    object DeleteSuccess : CalendarEvent()
    data class Error(val message: String) : CalendarEvent()
}