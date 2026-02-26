package com.audreyRetournayDiet.femSante.repository.local

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.room.dao.UserDao
import com.audreyRetournayDiet.femSante.room.entity.UserEntity
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject

class UserRepository(private val userDao: UserDao) {

    suspend fun addUser(user: UserEntity) : ApiResult<JSONObject> {
        try {
            val userId = userDao.insert(user)
            val json = JSONObject()
            json.put("id" , userId)
            return ApiResult.Success(json, "Utilisateur ajouté avec succès")
        } catch (e: Exception) {
            return ApiResult.Failure("Erreur lors de l'ajout de l'utilisateur : ${e.localizedMessage}")
        }
    }

    suspend fun getUser(login: String): ApiResult<JSONObject> {
        return try {
            val user = userDao.getByLogin(login).firstOrNull()
            if (user != null) {
                    val data = JSONObject().apply {
                        put("id", user.id)
                        put("login", user.login)
                    }
                    ApiResult.Success(data, "Utilisateur trouvé en BDD local")
                } else {
                    ApiResult.Failure("Utilisateur non trouvé en BDD local")
                }

        } catch (e: Exception) {
            ApiResult.Failure("Erreur DB : ${e.localizedMessage}")
        }
    }

}