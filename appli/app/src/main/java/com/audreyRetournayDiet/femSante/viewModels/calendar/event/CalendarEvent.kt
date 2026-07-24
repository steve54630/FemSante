package com.audreyRetournayDiet.femSante.viewModels.calendar.event

import com.audreyRetournayDiet.femSante.data.report.MedicalReport
import com.audreyRetournayDiet.femSante.data.report.ReportFormat

sealed class CalendarEvent {
    object DeleteSuccess : CalendarEvent()
    data class Error(val message: String) : CalendarEvent()

    /** Récap médical agrégé, prêt à être rendu en PDF puis partagé par l'utilisatrice. */
    data class ReportReady(val report: MedicalReport, val format: ReportFormat) : CalendarEvent()
}
