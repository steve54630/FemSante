package com.audreyRetournayDiet.femSante.repository

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.CONNECT_USER_API
import com.audreyRetournayDiet.femSante.CREATE_USER_API
import com.audreyRetournayDiet.femSante.EMAIL_EXIST_API
import com.audreyRetournayDiet.femSante.FORGOTTEN_PASSWORD_API
import com.audreyRetournayDiet.femSante.UPDATE_USER_API
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires
import org.json.JSONObject

class UserManager(private var context: Context) {

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    fun verifyEmail(
        parameters: JSONObject,
        onComplete: (ApiResult) -> Unit,
    ) {

        val request =
            JsonObjectRequest(Request.Method.POST, EMAIL_EXIST_API, parameters, {

                    response ->

                val success = response.getBoolean("success")

                val message = if (success) "" else response.getString("error")

                onComplete(ApiResult.Success(null, message))

            }, {

                onComplete(ApiResult.Failure("Erreur de connexion"))

            })

        this.queue.add(request)
    }

    fun createUser(
        parameters: JSONObject,
        onComplete: (ApiResult) -> Unit,
    ) {

        val request =
            JsonObjectRequest(Request.Method.POST, CREATE_USER_API, parameters, { response ->
                val result = Utilitaires.onApiResponse(response, context)

                if (result) {

                    onComplete(ApiResult.Success(response, "Inscription réussie"))

                } else {

                    onComplete(ApiResult.Failure(response.getString("error")))
                }
            },
                { error ->

                    Log.e("Connexion", error.localizedMessage ?: "Erreur inconnue")

                    onComplete(ApiResult.Failure("Erreur de connexion"))

                })

        this.queue.add(request)
    }

    fun connectUser(
        parameters: JSONObject,
        onComplete: (ApiResult) -> Unit,
    ) {
        val request =
            JsonObjectRequest(Request.Method.POST, CONNECT_USER_API, parameters, { response ->
                val result = Utilitaires.onApiResponse(response, context)

                if (result) {

                    onComplete(ApiResult.Success(response, "Connexion réussie"))

                } else {

                    onComplete(ApiResult.Failure(response.getString("error")))
                }
            },
                { error ->

                    Log.e("Connexion", error.localizedMessage ?: "Erreur inconnue")

                    onComplete(ApiResult.Failure("Erreur de connexion"))

                })

        this.queue.add(request)
    }

    fun changePassword(
        parameters: JSONObject,
        onComplete: (ApiResult) -> Unit,
    ) {
        val request =
            JsonObjectRequest(Request.Method.POST, FORGOTTEN_PASSWORD_API, parameters, { response ->
                val result = Utilitaires.onApiResponse(response, context)

                if (result) {

                    onComplete(ApiResult.Success(null, "Mot de passe changé"))

                } else {

                    onComplete(ApiResult.Failure(response.getString("error")))
                }
            },
                { error ->

                    Log.e("Connexion", error.localizedMessage ?: "Erreur inconnue")

                    onComplete(ApiResult.Failure("Erreur de connexion"))

                })

        this.queue.add(request)
    }

    fun updateUser(
        parameters: JSONObject,
        onComplete: (ApiResult) -> Unit,
    ) {

        val request =
            JsonObjectRequest(Request.Method.POST, UPDATE_USER_API, parameters, { response ->
                val result = Utilitaires.onApiResponse(response, context)

                if (result) {

                    val message = "Mise à jour de l'abonnement effectué"

                    onComplete(ApiResult.Success(null, message))

                } else {

                    onComplete(ApiResult.Failure(response.getString("error")))
                }
            },
                { error ->

                    Log.e("Connexion", error.localizedMessage ?: "Erreur inconnue")

                    onComplete(ApiResult.Failure("Erreur de connexion"))

                })

        this.queue.add(request)
    }
}