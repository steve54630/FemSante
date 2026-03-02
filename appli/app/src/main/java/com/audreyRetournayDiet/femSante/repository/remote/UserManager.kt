package com.audreyRetournayDiet.femSante.repository.remote

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.API_URL
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

@OptIn(ExperimentalCoroutinesApi::class)
class UserManager(private val context: Context) {

    private val tag = "REPO_USER_REMOTE"
    private val queue: RequestQueue = Volley.newRequestQueue(context)

    private suspend fun postRequest(
        endpoint: String,
        parameters: JSONObject,
        successMessage: String? = null
    ): ApiResult<JSONObject> = suspendCancellableCoroutine { cont ->

        val url = "${API_URL}$endpoint"
        Log.d(tag, "Appel réseau : $endpoint | Params: $parameters")

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            parameters,
            { response ->
                try {
                    val ok = Utilitaires.onApiResponse(response, context)

                    if (ok) {
                        Log.i(tag, "Succès sur $endpoint : ${response.optString("message", "Pas de détail")}")
                        cont.resume(ApiResult.Success(response, successMessage ?: "Succès"))
                    } else {
                        val errorMsg = response.optString("error", "Erreur serveur")
                        Log.w(tag, "Refus serveur sur $endpoint : $errorMsg")
                        cont.resume(ApiResult.Failure(errorMsg))
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Erreur de parsing sur $endpoint", e)
                    cont.resume(ApiResult.Failure("Format de réponse invalide"))
                }
            },
            { error ->
                val errorMessage = error.localizedMessage ?: "Problème réseau ou Timeout"
                Log.e(tag, "Erreur connexion sur $endpoint : $errorMessage")
                cont.resume(ApiResult.Failure("Erreur de connexion au serveur"))
            }
        )

        cont.invokeOnCancellation {
            Log.d(tag, "Requête $endpoint annulée par la Coroutine")
            request.cancel()
        }

        queue.add(request)
    }

    suspend fun verifyEmail(parameters: JSONObject): ApiResult<JSONObject> =
        postRequest("/user/check-email", parameters)

    suspend fun createUser(parameters: JSONObject): ApiResult<JSONObject> =
        postRequest("/user/register", parameters, "Inscription réussie")

    suspend fun connectUser(parameters: JSONObject): ApiResult<JSONObject> =
        postRequest("/user/connect", parameters, "Connexion réussie")

    suspend fun changePassword(parameters: JSONObject): ApiResult<JSONObject> =
        postRequest("/user/forgotten-password", parameters, "Mot de passe changé")

    suspend fun updateUser(parameters: JSONObject): ApiResult<JSONObject> =
        postRequest("/user/update", parameters, "Mise à jour de l'abonnement effectuée")
}