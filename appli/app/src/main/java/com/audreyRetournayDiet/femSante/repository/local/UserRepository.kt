package com.audreyRetournayDiet.femSante.repository.local

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.room.dao.UserDao
import com.audreyRetournayDiet.femSante.room.entity.UserEntity
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
import timber.log.Timber

/**
 * Repository gérant la persistance locale des données utilisateurs.
 * * Ce composant sert d'interface entre le [UserDao] et le reste de l'application.
 * Il transforme les entités de base de données [UserEntity] en formats d'échange
 * standardisés ([JSONObject]) pour faciliter le traitement dans les ViewModels.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Enregistre une nouvelle utilisatrice dans la base de données locale.
     * * @param user L'entité contenant les informations de connexion et de profil.
     * @return Un [ApiResult] contenant l'ID généré par Room en cas de succès.
     */
    suspend fun addUser(user: UserEntity) : ApiResult<JSONObject> {
        return try {
            Timber.d("Tentative d'ajout d'un nouvel utilisateur : ${user.login}")

            // Insertion dans Room (l'ID est retourné automatiquement par l'annotation @Insert)
            val userId = userDao.insert(user)

            val json = JSONObject()
            json.put("id" , userId)

            Timber.i("Utilisateur ajouté avec succès. ID généré : $userId")
            ApiResult.Success(json, "Utilisateur ajouté avec succès")
        } catch (e: Exception) {
            // Capture les erreurs potentielles (ex : contrainte d'unicité sur le login)
            Timber.e(e, "Erreur fatale lors de l'insertion de l'utilisateur : ${user.login}")
            ApiResult.Failure("Erreur lors de l'ajout de l'utilisateur : ${e.localizedMessage}")
        }
    }

    /**
     * Recherche une utilisatrice en base de données à partir de son identifiant (login).
     * * @param login L'email ou le nom d'utilisateur utilisé pour la recherche.
     * @return Un [ApiResult] contenant les données essentielles de l'utilisateur ou une erreur si non trouvées.
     */
    suspend fun getUser(login: String): ApiResult<JSONObject> {
        return try {
            Timber.d("Recherche de l'utilisateur en BDD locale : $login")

            // On récupère le premier élément du Flow ou null s'il n'y a pas de correspondance
            val user = userDao.getByLogin(login).firstOrNull()

            if (user != null) {
                val data = JSONObject().apply {
                    put("id", user.id)
                    put("login", user.login)
                }
                Timber.i("Utilisateur '$login' trouvé (ID: ${user.id})")
                ApiResult.Success(data, "Utilisateur trouvé en BDD local")
            } else {
                Timber.w("Aucun utilisateur trouvé pour le login : $login")
                ApiResult.Failure("Utilisateur non trouvé en BDD local")
            }
        } catch (e: Exception) {
            Timber.e(e, "Erreur DB lors de la récupération de : $login")
            ApiResult.Failure("Erreur DB : ${e.localizedMessage}")
        }
    }
}