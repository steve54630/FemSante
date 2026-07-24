package com.audreyRetournayDiet.femSante.repository.local

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.room.dao.CycleDayDao
import com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity
import com.audreyRetournayDiet.femSante.room.type.FlowLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId

/**
 * Repository du suivi de cycle (incrément 1) : lecture/écriture des observations
 * quotidiennes (règles, abondance, spotting). Données strictement locales.
 */
class CycleRepository(private val dao: CycleDayDao) {

    /**
     * Enregistre (upsert) l'observation de cycle d'un jour. Si rien n'est renseigné
     * (pas de règles, pas de spotting), l'entrée est tout de même persistée pour
     * refléter le choix explicite de l'utilisatrice.
     */
    suspend fun saveCycleDay(
        userId: String,
        date: LocalDate,
        isPeriod: Boolean,
        flow: FlowLevel?,
        spotting: Boolean
    ): ApiResult<String> {
        return try {
            dao.upsert(
                CycleDayEntity(
                    userId = userId,
                    date = dateToTimestamp(date),
                    isPeriod = isPeriod,
                    // L'abondance n'a de sens que si les règles sont cochées.
                    flow = if (isPeriod) flow else null,
                    spotting = spotting
                )
            )
            ApiResult.Success("Success", "Observation de cycle enregistrée")
        } catch (e: Exception) {
            Timber.e(e, "Échec sauvegarde cycle pour $date")
            ApiResult.Failure("Erreur lors de l'enregistrement du cycle : ${e.localizedMessage}")
        }
    }

    suspend fun getCycleDay(userId: String, date: LocalDate): ApiResult<CycleDayEntity?> {
        return try {
            ApiResult.Success(dao.getByDate(userId, dateToTimestamp(date)), "Observation récupérée")
        } catch (e: Exception) {
            Timber.e(e, "Échec lecture cycle pour $date")
            ApiResult.Failure("Erreur lecture cycle : ${e.localizedMessage}")
        }
    }

    /** Ensemble des dates de règles, pour le marqueur visuel du calendrier. */
    suspend fun getPeriodDates(userId: String): ApiResult<Set<LocalDate>> {
        return try {
            val dates = dao.getPeriodDates(userId).map { timestampToDate(it) }.toSet()
            ApiResult.Success(dates, "Dates de règles récupérées")
        } catch (e: Exception) {
            Timber.e(e, "Échec lecture des dates de règles")
            ApiResult.Failure("Erreur lecture des dates de règles : ${e.localizedMessage}")
        }
    }

    /**
     * Ensemble des dates explicitement saisies (règles ou non), pour exclure du
     * prévisionnel les jours déjà traités par l'utilisatrice (confirmés ou décochés).
     */
    suspend fun getRecordedDates(userId: String): ApiResult<Set<LocalDate>> {
        return try {
            val dates = dao.getRecordedDates(userId).map { timestampToDate(it) }.toSet()
            ApiResult.Success(dates, "Dates saisies récupérées")
        } catch (e: Exception) {
            Timber.e(e, "Échec lecture des dates saisies")
            ApiResult.Failure("Erreur lecture des dates saisies : ${e.localizedMessage}")
        }
    }

    /** Observe (réactif) l'observation de cycle d'une journée (saisie de l'écran détail). */
    fun observeCycleDay(userId: String, date: LocalDate): Flow<CycleDayEntity?> =
        dao.observeByDate(userId, dateToTimestamp(date))

    /** Observe (réactif) l'ensemble des dates de règles (marqueurs de la grille). */
    fun observePeriodDates(userId: String): Flow<Set<LocalDate>> =
        dao.observePeriodDates(userId).map { list -> list.map { timestampToDate(it) }.toSet() }

    private fun dateToTimestamp(date: LocalDate): Long =
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun timestampToDate(timestamp: Long): LocalDate =
        java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
}
