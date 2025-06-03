package com.audreyRetournayDiet.femSante.repository

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.PAYPAL_CALL_API
import org.json.JSONObject

class PaymentManager(private val context: AppCompatActivity) {

    private val volley = Volley.newRequestQueue(this.context)

    fun applyReduction(
        params: JSONObject,
        onComplete: (ApiResult) -> Unit,
    ) {
        val url =
            "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/reductionVerify.php"

        val request =
            JsonObjectRequest(Request.Method.POST, url, params, { reponse ->

                val success = reponse.getBoolean("success")

                if (success) {

                    val json = JSONObject()
                    json.put("result", reponse.getInt("reduction"))

                    onComplete(ApiResult.Success(json, "Réduction accepté"))

                } else {

                    onComplete(ApiResult.Failure(reponse.getString("error")))

                }
            }, { err ->

                Log.e("Connexion", err.localizedMessage!!)

                onComplete(ApiResult.Failure("Erreur de connexion"))

            })

        volley.add(request)
    }


    fun payPalCall(
        params: JSONObject,
        onComplete: (ApiResult) -> Unit,
    ) {
        val request =
            JsonObjectRequest(Request.Method.POST, PAYPAL_CALL_API, params, { reponse ->

                onComplete(ApiResult.Success(reponse, ""))

            }, { err ->

                Log.e("Connexion", err.localizedMessage ?: "Erreur inconnue")

                onComplete(ApiResult.Failure("Erreur de connexion"))
            })

        volley.add(request)
    }

    fun captureOrder(
        params: JSONObject,
        onComplete: (ApiResult) -> Unit,
    ) {

        val url =
            "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/paypalCapture.php"

        val request = JsonObjectRequest(Request.Method.POST, url, params, { response ->

            if (response.getBoolean("success")) {

                onComplete(ApiResult.Success(null, "Paiement réussi"))

            } else {

                onComplete(ApiResult.Failure(response.getString("error")))
            }
        }, { err ->

            Log.e("Paiement", err.localizedMessage!!)

            onComplete(ApiResult.Failure("Erreur de connexion"))
        })

        volley.add(request)

    }
}