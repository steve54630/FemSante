package com.audreyRetournayDiet.femSante.repository.remote

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.API_URL
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject

@OptIn(ExperimentalCoroutinesApi::class)
class UserManager(private val context: Context) {

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    // 🔧 Utilitaire générique pour éviter de répéter du code
    private suspend fun postRequest(
        endpoint: String,
        parameters: JSONObject,
        successMessage: String? = null
    ): ApiResult = suspendCancellableCoroutine { cont ->

        val request = JsonObjectRequest(
            Request.Method.POST,
            "${API_URL}$endpoint",
            parameters,
            { response ->
                val ok = Utilitaires.onApiResponse(response, context)

                if (ok) {
                    cont.resume(ApiResult.Success(response, successMessage ?: "Succès")) {}
                } else {
                    val errorMsg = response.optString("error", "Erreur inconnue")
                    cont.resume(ApiResult.Failure(errorMsg)) {}
                }
            },
            { error ->
                Log.e("UserManager", error.localizedMessage ?: "Erreur inconnue")
                cont.resume(ApiResult.Failure("Erreur de connexion")) {}
            }
        )

        // Support de l'annulation coroutine
        cont.invokeOnCancellation {
            request.cancel()
        }

        queue.add(request)
    }

    // 🔽 Fonctions lisibles et simples
    suspend fun verifyEmail(parameters: JSONObject): ApiResult =
        postRequest("/user/check-email", parameters)

    suspend fun createUser(parameters: JSONObject): ApiResult =
        postRequest("/user/register", parameters, "Inscription réussie")

    suspend fun connectUser(parameters: JSONObject): ApiResult =
        postRequest("/user/connect", parameters, "Connexion réussie")

    suspend fun changePassword(parameters: JSONObject): ApiResult =
        postRequest("/user/forgotten-password", parameters, "Mot de passe changé")

    suspend fun updateUser(parameters: JSONObject): ApiResult =
        postRequest("/user/update", parameters, "Mise à jour de l'abonnement effectuée")
}
