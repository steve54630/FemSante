package com.audreyRetournayDiet.femSante.repository.local

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.room.dao.DailyEntryDao
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.DatePainStatus
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import java.time.LocalDate
import java.time.ZoneId

class DailyRepository(private val dao: DailyEntryDao) {

    suspend fun getCalendarStatus(userId: String): ApiResult<List<DatePainStatus>> {
        try {
            val results = dao.getCalendarStatus(userId)
            return ApiResult.Success(results, "Données du calendrier récupéré avec succès")
        } catch (e: Exception) {
            return ApiResult.Failure("Erreur : ${e.localizedMessage}")
        }
    }

    suspend fun deleteEntry(userId: String, date: Long): ApiResult<String> {
        return try {
            // On appelle la méthode du DAO qu'on a définie plus tôt
            dao.deleteFullEntry(userId, date)
            ApiResult.Success(message = "Suppression réussie", data = null)
        } catch (e: Exception) {
            ApiResult.Failure(e.message ?: "Erreur lors de la suppression")
        }
    }

    suspend fun getDailyEntrybyID(userId: String, id: Long): ApiResult<DailyEntryFull?> {
        try {
            val data = dao.getFullEntry(userId, id)
            return ApiResult.Success(data, "")
        } catch (e: Exception) {
            return ApiResult.Failure("Erreur lors de la récupération des données de l'ID $id : ${e.localizedMessage}")
        }
    }

    suspend fun getDailyEntrybyDate(userId: String, date: LocalDate): ApiResult<DailyEntryFull?> {
        try {
            val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val data = dao.getFullEntryByDate(userId, timestamp)
            return ApiResult.Success(data, "")
        } catch (e: Exception) {
            return ApiResult.Failure("Erreur lors de la récupération des données pour la date $date: ${e.localizedMessage}")
        }
    }

    suspend fun saveCompleteEntry(
        userId: String,
        date: Long,
        general: GeneralStateEntity,
        context: ContextStateEntity,
        psy: PsychologicalStateEntity,
        symptom: SymptomStateEntity,
    ): ApiResult<String> {
        return try {
            dao.insertFullDailyEntry(
                userId, date, general, psy, symptom, context
            )
            ApiResult.Success(null, "Succès lors de l'ajout des données")
        } catch (e: Exception) {
            ApiResult.Failure("Erreur lors de l'enregistrement des données : ${e.localizedMessage}")
        }
    }

}