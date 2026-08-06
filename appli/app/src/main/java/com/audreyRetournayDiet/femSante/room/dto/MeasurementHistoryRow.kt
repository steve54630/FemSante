package com.audreyRetournayDiet.femSante.room.dto

/**
 * Ligne d'historique de mesures pour les courbes de tendance : la date de la journée
 * (jointe depuis `daily_entry`) et les valeurs de mesure du jour (nullables).
 *
 * Résultat de requête (POJO Room) : les noms de champs correspondent aux alias SQL.
 */
data class MeasurementHistoryRow(
    val date: Long,
    val weight: Double?,
    val waist: Double?,
    val hips: Double?,
    val thighs: Double?,
    val chest: Double?,
    val arms: Double?
)
