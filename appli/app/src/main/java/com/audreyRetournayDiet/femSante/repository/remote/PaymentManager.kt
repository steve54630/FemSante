package com.audreyRetournayDiet.femSante.repository.remote

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.API_URL
import com.audreyRetournayDiet.femSante.repository.ApiResult
import org.json.JSONObject

class PaymentManager(private val context: AppCompatActivity) {

    private val tag = "REPO_PAYMENT"
    private val volley = Volley.newRequestQueue(this.context)

    fun applyReduction(
        params: JSONObject,
        onComplete: (ApiResult<JSONObject>) -> Unit,
    ) {
        val url = "${API_URL}/paypal/reduction"
        Log.d(tag, "Tentative d'application de réduction. Params: $params")

        val request = JsonObjectRequest(Request.Method.POST, url, params, { reponse ->
            try {
                val success = reponse.getBoolean("success")
                if (success) {
                    val reductionValue = reponse.getInt("reduction")
                    val json = JSONObject().apply { put("result", reductionValue) }

                    Log.i(tag, "Code promo accepté : -$reductionValue%")
                    onComplete(ApiResult.Success(json, "Réduction accepté"))
                } else {
                    val errorMsg = reponse.optString("error", "Code invalide")
                    Log.w(tag, "Réduction refusée par le serveur : $errorMsg")
                    onComplete(ApiResult.Failure(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(tag, "Erreur lors du traitement de la réponse réduction", e)
                onComplete(ApiResult.Failure("Erreur de traitement serveur"))
            }
        }, { err ->
            Log.e(tag, "Erreur réseau (Reduction): ${err.localizedMessage ?: "Timeout/Coupure"}")
            onComplete(ApiResult.Failure("Erreur de connexion"))
        })

        volley.add(request)
    }

    fun payPalCall(
        params: JSONObject,
        onComplete: (ApiResult<JSONObject>) -> Unit,
    ) {
        val url = "${API_URL}/paypal/create-order"
        Log.d(tag, "Initialisation commande PayPal. Params: $params")

        val request = JsonObjectRequest(Request.Method.POST, url, params, { reponse ->
            Log.i(tag, "Commande PayPal créée avec succès")
            onComplete(ApiResult.Success(reponse, ""))
        }, { err ->
            Log.e(tag, "Erreur réseau (CreateOrder): ${err.localizedMessage ?: "Erreur inconnue"}")
            onComplete(ApiResult.Failure("Erreur de connexion"))
        })

        volley.add(request)
    }

    fun captureOrder(
        params: JSONObject,
        onComplete: (ApiResult<JSONObject>) -> Unit,
    ) {
        val url = "${API_URL}/paypal/capture-order"
        Log.d(tag, "Tentative de capture de commande. Order: ${params.optString("orderID")}")

        val request = JsonObjectRequest(Request.Method.POST, url , params, { response ->
            try {
                if (response.getBoolean("success")) {
                    Log.i(tag, "Paiement capturé et validé avec succès")
                    onComplete(ApiResult.Success(null, "Paiement réussi"))
                } else {
                    val errorMsg = response.optString("error", "Échec de capture")
                    Log.e(tag, "Le serveur a refusé la capture : $errorMsg")
                    onComplete(ApiResult.Failure(errorMsg))
                }
            } catch (e: Exception) {
                Log.e(tag, "Erreur parsing capture", e)
                onComplete(ApiResult.Failure("Erreur lors de la validation finale"))
            }
        }, { err ->
            Log.e(tag, "Erreur réseau critique (Capture): ${err.localizedMessage ?: "Erreur inconnue"}")
            onComplete(ApiResult.Failure("Erreur de connexion"))
        })

        volley.add(request)
    }
}