package com.audreyRetournayDiet.femSante.room.entity

/**
 * Objet de transfert léger (Data Projection) pour la vue Calendrier.
 * * ### Rôle :
 * Au lieu de charger l'intégralité des données de santé (ce qui serait lourd),
 * ce DTO permet de récupérer uniquement le strict nécessaire pour colorer
 * les jours du calendrier.
 * * ### Utilisation :
 * Utilisé par la requête `getCalendarStatus` dans le [com.audreyRetournayDiet.femSante.room.dao.DailyEntryDao].
 */
data class DatePainStatus(
    /** Timestamp de la journée (normalisé à minuit) */
    val date: Long,

    /** * Niveau de douleur extrait de GeneralStateEntity.
     * Permet de déterminer la couleur du point (ex: 0 = Vert, 5 = Orange, 10 = Rouge).
     */
    val painLevel: Int
)