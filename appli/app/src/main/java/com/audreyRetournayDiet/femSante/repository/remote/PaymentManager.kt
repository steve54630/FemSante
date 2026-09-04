package com.audreyRetournayDiet.femSante.repository.remote

import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.API_URL
import com.audreyRetournayDiet.femSante.repository.ApiResult
import org.json.JSONObject
import timber.log.Timber

/**
 * Gestionnaire des transactions de paiement et de facturation (Remote).
 *
 * Cette classe centralise les appels API vers le backend pour :
 * 1. **Vérifier les codes promo** : Application dynamique de remises.
 * 2. **Initialiser PayPal** : Création d'une commande (Order) côté serveur.
 * 3. **Capturer le paiement** : Validation finale après l'accord de l'utilisatrice.
 *
 * Utilise [Volley] pour la gestion de la file d'attente des requêtes réseau.
 */
class PaymentManager(private val context: AppCompatActivity) {

    private val volley = Volley.newRequestQueue(this.context)

    /**
     * Volley route tout code HTTP non-2xx vers le listener d'erreur sans exposer le corps de
     * la réponse. Nos endpoints renvoient pourtant un JSON exploitable même en erreur : soit
     * `{"error": "..."}` (nos 4xx/5xx métier), soit `{"errors": {"champ": ["..."]}}` (422 de
     * validation Laravel). On relit ce corps pour ne pas perdre ce message derrière un
     * générique "Erreur de connexion".
     */
    private fun serverErrorMessage(err: VolleyError, fallback: String): String {
        val body = err.networkResponse?.data ?: return fallback
        return try {
            val json = JSONObject(String(body, Charsets.UTF_8))
            if (json.has("error")) return json.optString("error", fallback)
            val errors = json.optJSONObject("errors") ?: return fallback
            val firstField = errors.keys().asSequence().firstOrNull() ?: return fallback
            errors.optJSONArray(firstField)?.optString(0) ?: fallback
        } catch (e: Exception) {
            fallback
        }
    }

    /**
     * Soumet un code de réduction au serveur pour validation.
     * * @param params Un [JSONObject] contenant la clé "reductionCode".
     * @param onComplete Callback retournant un [ApiResult] avec le pourcentage de remise.
     */
    fun applyReduction(
        params: JSONObject,
        onComplete: (ApiResult<JSONObject>) -> Unit,
    ) {
        val url = "${API_URL}/paypal/reduction"
        Timber.d("Tentative d'application de réduction. Params: $params")

        val request = JsonObjectRequest(Request.Method.POST, url, params, { reponse ->
            try {
                val success = reponse.getBoolean("success")
                if (success) {
                    val reductionValue = reponse.getInt("reduction")
                    // On encapsule la valeur dans un objet JSON pour rester cohérent avec ApiResult
                    val json = JSONObject().apply { put("reduction", reductionValue) }

                    Timber.i("Code promo accepté : -$reductionValue%")
                    onComplete(ApiResult.Success(json, "Réduction accepté"))
                } else {
                    val errorMsg = reponse.optString("error", "Code invalide")
                    Timber.w("Réduction refusée par le serveur : $errorMsg")
                    onComplete(ApiResult.Failure(errorMsg))
                }
            } catch (e: Exception) {
                Timber.e(e, "Erreur lors du traitement de la réponse réduction")
                onComplete(ApiResult.Failure("Erreur de traitement serveur"))
            }
        }, { err ->
            val message = serverErrorMessage(err, "Erreur de connexion")
            Timber.e("Erreur réseau (Reduction): $message")
            onComplete(ApiResult.Failure(message))
        })

        volley.add(request)
    }

    /**
     * Crée une commande PayPal sur le serveur.
     * Cette étape est nécessaire avant d'afficher l'interface de paiement à l'utilisatrice.
     * * @param params Palier demandé (email, days, code promo éventuel) — le serveur calcule
     * et fait foi sur le prix, plus aucun montant n'est transmis par l'app.
     */
    fun payPalCall(
        params: JSONObject,
        onComplete: (ApiResult<JSONObject>) -> Unit,
    ) {
        val url = "${API_URL}/paypal/create-order"
        Timber.d("Initialisation commande PayPal. Params: $params")

        val request = JsonObjectRequest(Request.Method.POST, url, params, { reponse ->
            Timber.i("Commande PayPal créée avec succès")
            onComplete(ApiResult.Success(reponse, ""))
        }, { err ->
            val message = serverErrorMessage(err, "Erreur de connexion")
            Timber.e("Erreur réseau (CreateOrder): $message")
            onComplete(ApiResult.Failure(message))
        })

        volley.add(request)
    }

    /**
     * Valide et capture les fonds après que l'utilisatrice a approuvé le paiement sur PayPal.
     * C'est cette étape qui confirme définitivement l'abonnement en base de données.
     * * @param params Doit contenir "orderID".
     */
    fun captureOrder(
        params: JSONObject,
        onComplete: (ApiResult<JSONObject>) -> Unit,
    ) {
        val url = "${API_URL}/paypal/capture-order"
        Timber.d("Tentative de capture de commande. Order: ${params.optString("orderID")}")

        val request = JsonObjectRequest(Request.Method.POST, url , params, { response ->
            try {
                if (response.getBoolean("success")) {
                    Timber.i("Paiement capturé et validé avec succès")
                    onComplete(ApiResult.Success(null, "Paiement réussi"))
                } else {
                    val errorMsg = response.optString("error", "Échec de capture")
                    Timber.e("Le serveur a refusé la capture : $errorMsg")
                    onComplete(ApiResult.Failure(errorMsg))
                }
            } catch (e: Exception) {
                Timber.e(e, "Erreur parsing capture")
                onComplete(ApiResult.Failure("Erreur lors de la validation finale"))
            }
        }, { err ->
            val message = serverErrorMessage(err, "Erreur de connexion")
            Timber.e("Erreur réseau critique (Capture): $message")
            onComplete(ApiResult.Failure(message))
        })

        volley.add(request)
    }

    /**
     * Signale l'abandon d'une commande jamais approuvée par l'utilisatrice, pour qu'elle
     * passe "cancelled" côté serveur plutôt que de rester "created" indéfiniment.
     * * @param params Doit contenir "orderId".
     */
    fun cancelOrder(
        params: JSONObject,
        onComplete: (ApiResult<JSONObject>) -> Unit,
    ) {
        val url = "${API_URL}/paypal/cancel-order"
        Timber.d("Annulation de commande. Order: ${params.optString("orderId")}")

        val request = JsonObjectRequest(Request.Method.POST, url, params, { reponse ->
            Timber.i("Commande marquée annulée côté serveur")
            onComplete(ApiResult.Success(reponse, ""))
        }, { err ->
            val message = serverErrorMessage(err, "Erreur de connexion")
            Timber.e("Erreur réseau (CancelOrder): $message")
            onComplete(ApiResult.Failure(message))
        })

        volley.add(request)
    }
}