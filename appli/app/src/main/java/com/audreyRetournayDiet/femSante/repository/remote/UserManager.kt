package com.audreyRetournayDiet.femSante.repository.remote

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.API_URL
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@OptIn(ExperimentalCoroutinesApi::class)
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    private suspend fun postRequest(
        endpoint: String,
        parameters: JSONObject,
        successMessage: String? = null
    ): ApiResult<JSONObject> = suspendCancellableCoroutine { cont ->

        val url = "${API_URL}$endpoint"
        Timber.d("Appel réseau : $endpoint | Params: $parameters")

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            parameters,
            { response ->
                try {
                    val ok = Utilitaires.onApiResponse(response, context)

                    if (ok) {
                        Timber.i(
                            "Succès sur $endpoint : ${
                                response.optString(
                                    "message",
                                    "Pas de détail"
                                )
                            }"
                        )
                        cont.resume(ApiResult.Success(response, successMessage ?: "Succès"))
                    } else {
                        val errorMsg = response.optString("error", "Erreur serveur")
                        Timber.w("Refus serveur sur $endpoint : $errorMsg")
                        cont.resume(ApiResult.Failure(errorMsg))
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Erreur de parsing sur $endpoint")
                    cont.resume(ApiResult.Failure("Format de réponse invalide"))
                }
            },
            { error ->
                // Volley route tout code HTTP non-2xx ici sans exposer le corps de la réponse
                // par défaut — nos endpoints renvoient pourtant un JSON exploitable même en
                // erreur (`{"error": "..."}`). On le relit pour ne pas perdre ce message métier
                // derrière un générique "Erreur de connexion au serveur".
                val body = error.networkResponse?.data
                val serverError = body?.let {
                    try {
                        JSONObject(String(it, Charsets.UTF_8)).optString("error", null)
                    } catch (e: Exception) {
                        null
                    }
                }
                val errorMessage = serverError ?: error.localizedMessage ?: "Problème réseau ou Timeout"
                Timber.e("Erreur connexion sur $endpoint : $errorMessage")
                cont.resume(ApiResult.Failure(serverError ?: "Erreur de connexion au serveur"))
            }
        )

        cont.invokeOnCancellation {
            Timber.d("Requête $endpoint annulée par la Coroutine")
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

    suspend fun activateFreeTrial(parameters: JSONObject): ApiResult<JSONObject> =
        postRequest("/user/free-trial", parameters, "Essai gratuit activé")
}