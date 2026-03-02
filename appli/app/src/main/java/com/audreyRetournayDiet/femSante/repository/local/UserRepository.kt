package com.audreyRetournayDiet.femSante.repository.local

import android.util.Log
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.room.dao.UserDao
import com.audreyRetournayDiet.femSante.room.entity.UserEntity
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject

class UserRepository(private val userDao: UserDao) {

    private val tag = "REPO_USER"

    suspend fun addUser(user: UserEntity) : ApiResult<JSONObject> {
        return try {
            Log.d(tag, "Tentative d'ajout d'un nouvel utilisateur : ${user.login}")
            val userId = userDao.insert(user)

            val json = JSONObject()
            json.put("id" , userId)

            Log.i(tag, "Utilisateur ajouté avec succès. ID généré : $userId")
            ApiResult.Success(json, "Utilisateur ajouté avec succès")
        } catch (e: Exception) {
            Log.e(tag, "Erreur fatale lors de l'insertion de l'utilisateur : ${user.login}", e)
            ApiResult.Failure("Erreur lors de l'ajout de l'utilisateur : ${e.localizedMessage}")
        }
    }

    suspend fun getUser(login: String): ApiResult<JSONObject> {
        return try {
            Log.d(tag, "Recherche de l'utilisateur en BDD locale : $login")
            val user = userDao.getByLogin(login).firstOrNull()

            if (user != null) {
                val data = JSONObject().apply {
                    put("id", user.id)
                    put("login", user.login)
                }
                Log.i(tag, "Utilisateur '$login' trouvé (ID: ${user.id})")
                ApiResult.Success(data, "Utilisateur trouvé en BDD local")
            } else {
                Log.w(tag, "Aucun utilisateur trouvé pour le login : $login")
                ApiResult.Failure("Utilisateur non trouvé en BDD local")
            }
        } catch (e: Exception) {
            Log.e(tag, "Erreur DB lors de la récupération de : $login", e)
            ApiResult.Failure("Erreur DB : ${e.localizedMessage}")
        }
    }
}