package com.audreyRetournayDiet.femSante.repository.local

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.room.dao.DailyEntryDao
import com.audreyRetournayDiet.femSante.room.dao.DailyEntryDao.DatePainStatus
import com.audreyRetournayDiet.femSante.room.dto.DailyEntryFull
import java.time.LocalDate
import java.time.ZoneId

class DailyRepository(private val dao: DailyEntryDao) {

    suspend fun getCalendarStatus(userId: String) : ApiResult<List<DatePainStatus>> {
        try {
            val results = dao.getCalendarStatus(userId)
            return ApiResult.Success(results, "Données du calendrier récupéré avec succès")
        } catch (e: Exception) {
            return ApiResult.Failure("Erreur : ${e.localizedMessage}")
        }
    }

    suspend fun getDailyEntry(userId: String, date : LocalDate) : ApiResult<DailyEntryFull?> {
        try {
            val timestamp = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val data = dao.getFullEntry(userId, timestamp)
            return ApiResult.Success(data, "")
        }
        catch (e: Exception) {
            return ApiResult.Failure("Erreur lors de la récupération des données du jour $date : ${e.localizedMessage}")
        }
    }

}