package com.audreyRetournayDiet.femSante.repository.local

import com.audreyRetournayDiet.femSante.room.dao.DailyEntryDao
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.DatePainStatus
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId

/**
 * Repository gérant le journal de bord quotidien (Daily Entries).
 *
 * Ce composant orchestre les interactions avec la base de données Room pour le suivi
 * de l'état de santé général, psychologique et symptomatique de l'utilisatrice.
 *
 * ### Architecture des données :
 * Une "entrée quotidienne" est une agrégation complexe de plusieurs entités liées :
 * - [GeneralStateEntity] : État global (sommeil, énergie).
 * - [ContextStateEntity] : Facteurs externes (météo, cycle, alimentation).
 * - [PsychologicalStateEntity] : État émotionnel et stress.
 * - [SymptomStateEntity] : Douleurs et symptômes physiques précis.
 */
class DailyRepository(private val dao: DailyEntryDao) {

    /**
     * Récupère un résumé léger (Statut de douleur/état) pour l'affichage du calendrier.
     * @param userId Identifiant unique de l'utilisatrice.
     * @return Une liste de [DatePainStatus] pour colorer ou annoter les jours du calendrier.
     */
    suspend fun getCalendarStatus(userId: String): ApiResult<List<DatePainStatus>> {
        return try {
            val startTime = System.currentTimeMillis()
            val results = dao.getCalendarStatus(userId)
            val duration = System.currentTimeMillis() - startTime
            Timber.d("Calendrier chargé : ${results.size} entrées en ${duration}ms (User: $userId)")
            ApiResult.Success(results, "Données du calendrier récupérées avec succès")
        } catch (e: Exception) {
            Timber.e(e, "Échec récupération calendrier user $userId")
            ApiResult.Failure("Erreur calendrier : ${e.localizedMessage}")
        }
    }

    /**
     * Supprime l'intégralité d'une entrée quotidienne et ses données liées.
     * @param userId Identifiant de l'utilisatrice propriétaire.
     * @param id Identifiant de l'entrée principale.
     */
    suspend fun deleteEntry(userId: String, id: Long): ApiResult<String> {
        return try {
            Timber.i("Suppression de l'entrée ID: $id (User: $userId)")
            dao.deleteFullEntry(userId, id)
            ApiResult.Success("Success", "Suppression réussie")
        } catch (e: Exception) {
            Timber.e(e, "Erreur suppression ID: $id")
            ApiResult.Failure(e.message ?: "Erreur lors de la suppression")
        }
    }

    /**
     * Récupère une entrée complète par son ID technique.
     * @return Un [DailyEntryFull] contenant l'agrégation de tous les états de la journée.
     */
    suspend fun getDailyEntryByID(userId: String, id: Long): ApiResult<DailyEntryFull?> {
        return try {
            val data = dao.getFullEntry(userId, id)
            if (data == null) Timber.w("Aucune donnée trouvée pour l'ID: $id")
            ApiResult.Success(data, "Donnée récupére pour l'entrée $id")
        } catch (e: Exception) {
            Timber.e(e, "Erreur lecture ID: $id")
            ApiResult.Failure("Erreur ID $id : ${e.localizedMessage}")
        }
    }

    /**
     * Récupère les données d'une journée spécifique à partir d'un objet [LocalDate].
     * Gère la conversion de la date en timestamp pour la recherche en base.
     */
    suspend fun getDailyEntryByDate(userId: String, date: LocalDate): ApiResult<DailyEntryFull?> {
        val timestamp = dateToTimestamp(date)
        return try {
            val data = dao.getFullEntryByDate(userId, timestamp)
            Timber.d("Lecture date: $date (Timestamp: $timestamp)")
            ApiResult.Success(data, "Donnée récupéré pour la date $timestamp")
        } catch (e: Exception) {
            Timber.e(e, "Erreur récupération date $date")
            ApiResult.Failure("Erreur pour la date $date : ${e.localizedMessage}")
        }
    }

    /**
     * Met à jour l'ensemble des composants d'une entrée existante.
     * Utilise une transaction interne au DAO pour modifier toutes les entités liées.
     */
    suspend fun updateCompleteEntry(
        userId: String,
        id: Long,
        general: GeneralStateEntity,
        context: ContextStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
    ): ApiResult<String> {
        return try {
            dao.editFullDailyEntry(userId, id, general, psy, symptom, context)
            Timber.i("Mise à jour réussie - ID: $id")
            ApiResult.Success("Success", "Données mises à jour avec succès")
        } catch (e: Exception) {
            Timber.e(e, "Échec update ID: $id")
            ApiResult.Failure("Erreur lors de la mise à jour : ${e.localizedMessage}")
        }
    }

    /**
     * Enregistre une nouvelle journée de suivi.
     * @param date Le timestamp représentant le jour (début de journée).
     */
    suspend fun saveCompleteEntry(
        userId: String,
        date: Long,
        general: GeneralStateEntity,
        context: ContextStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
    ): ApiResult<String> {
        return try {
            dao.insertFullDailyEntry(userId, date, general, psy, symptom, context)
            Timber.i("Nouvelle entrée enregistrée - Date: $date")
            ApiResult.Success("Success", "Données enregistrées avec succès")
        } catch (e: Exception) {
            Timber.e(e, "Échec sauvegarde date $date")
            ApiResult.Failure("Erreur lors de l'enregistrement : ${e.localizedMessage}")
        }
    }

    /**
     * Utilitaire de conversion : Transforme une [LocalDate] en Timestamp (Long)
     * calibré au début de la journée (00:00:00) selon le fuseau horaire du système.
     */
    private fun dateToTimestamp(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}