package com.audreyRetournayDiet.femSante.repository.local

import android.util.Log
import com.audreyRetournayDiet.femSante.room.dao.DailyEntryDao
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.DatePainStatus
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import java.time.LocalDate
import java.time.ZoneId

class DailyRepository(private val dao: DailyEntryDao) {

    private val tag = "REPO_DAILY"

    suspend fun getCalendarStatus(userId: String): ApiResult<List<DatePainStatus>> {
        return try {
            val startTime = System.currentTimeMillis()
            val results = dao.getCalendarStatus(userId)
            val duration = System.currentTimeMillis() - startTime
            Log.d(tag, "Calendrier chargé : ${results.size} entrées en ${duration}ms (User: $userId)")
            ApiResult.Success(results, "Données du calendrier récupérées avec succès")
        } catch (e: Exception) {
            Log.e(tag, "Échec récupération calendrier user $userId", e)
            ApiResult.Failure("Erreur calendrier : ${e.localizedMessage}")
        }
    }

    suspend fun deleteEntry(userId: String, id: Long): ApiResult<String> {
        return try {
            Log.i(tag, "Suppression de l'entrée ID: $id (User: $userId)")
            dao.deleteFullEntry(userId, id)
            ApiResult.Success("Success", "Suppression réussie")
        } catch (e: Exception) {
            Log.e(tag, "Erreur suppression ID: $id", e)
            ApiResult.Failure(e.message ?: "Erreur lors de la suppression")
        }
    }

    suspend fun getDailyEntryByID(userId: String, id: Long): ApiResult<DailyEntryFull?> {
        return try {
            val data = dao.getFullEntry(userId, id)
            if (data == null) Log.w(tag, "Aucune donnée trouvée pour l'ID: $id")
            ApiResult.Success(data, "Donnée récupére pour l'entrée $id")
        } catch (e: Exception) {
            Log.e(tag, "Erreur lecture ID: $id", e)
            ApiResult.Failure("Erreur ID $id : ${e.localizedMessage}")
        }
    }

    suspend fun getDailyEntryByDate(userId: String, date: LocalDate): ApiResult<DailyEntryFull?> {
        val timestamp = dateToTimestamp(date)
        return try {
            val data = dao.getFullEntryByDate(userId, timestamp)
            Log.d(tag, "Lecture date: $date (Timestamp: $timestamp)")
            ApiResult.Success(data, "Donnée récupéré pour la date $timestamp")
        } catch (e: Exception) {
            Log.e(tag, "Erreur récupération date $date", e)
            ApiResult.Failure("Erreur pour la date $date : ${e.localizedMessage}")
        }
    }

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
            Log.i(tag, "Mise à jour réussie - ID: $id")
            ApiResult.Success("Success", "Données mises à jour avec succès")
        } catch (e: Exception) {
            Log.e(tag, "Échec update ID: $id", e)
            ApiResult.Failure("Erreur lors de la mise à jour : ${e.localizedMessage}")
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
            dao.insertFullDailyEntry(userId, date, general, psy, symptom, context)
            Log.i(tag, "Nouvelle entrée enregistrée - Date: $date")
            ApiResult.Success("Success", "Données enregistrées avec succès")
        } catch (e: Exception) {
            Log.e(tag, "Échec sauvegarde date $date", e)
            ApiResult.Failure("Erreur lors de l'enregistrement : ${e.localizedMessage}")
        }
    }

    private fun dateToTimestamp(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}