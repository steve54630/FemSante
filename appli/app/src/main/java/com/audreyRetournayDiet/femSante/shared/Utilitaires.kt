package com.audreyRetournayDiet.femSante.shared

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.VideoManager
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

/**
 * Singleton regroupant les fonctions utilitaires globales.
 * * ### Rôle :
 * 1. Centraliser les validations de saisie (Email, Password).
 * 2. Gérer le parsing générique des réponses API (PayPal et Backend).
 * 3. Fournir des raccourcis pour les notifications (Toasts) et les lancements de vidéos.
 */
object Utilitaires {

    fun showToast(msg: String, context : Context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * Gère la récupération de l'URL et le lancement de l'Intent Vidéo.
     * Cette fonction permet de déporter la logique réseau hors des Views.
     */
    fun videoLaunch(
        titre: String?,
        pdf: String?,
        intent: Intent,
        context: Context,
    ) {
        val api = VideoManager(context)

        api.getVideoUrl(titre!!) { apiResult ->
            when (apiResult) {
                is ApiResult.Success -> {
                    val extrasVideo = Bundle()
                    val map = HashMap<String, String>()
                    map["Title"] = titre
                    map["URL"] = apiResult.data!!.getString("url")
                    map["PDF"] = pdf!!

                    extrasVideo.putSerializable("map", map)
                    intent.putExtras(extrasVideo)
                    context.startActivity(intent)
                }
                is ApiResult.Failure -> {
                    Toast.makeText(context, apiResult.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Vérifie la conformité de l'email via une expression régulière.
     */
    fun isValidEmail(email: String): Boolean {
        val pattern = "^[A-Za-z0-9]+[A-Za-z0-9_.-]+@[a-z0-9-.]+[.]+[a-z.-]{2,3}$"
        return email.matches(pattern.toRegex())
    }

    /**
     * Valide la force du mot de passe :
     * - Minimum 8 caractères
     * - Au moins une majuscule
     * - Au moins un caractère spécial (@$!%*#?&/_)
     * - Au moins une minuscule
     */
    fun isValidPassword(password: String): Boolean {
        val pattern = "^(?=.*[A-Z])(?=.*[@$!%*#?&/_])(?=.*[a-z])[A-Za-z\\d@$!%*#?&/]{8,}$"
        return password.matches(pattern.toRegex())
    }

    /**
     * Parse spécifiquement la réponse de création d'ordre PayPal.
     * @return L'ID de l'ordre ou une chaîne vide en cas d'erreur.
     */
    fun onPayPalApiResponse(context: Context, response: JSONObject?): String {
        var orderId = ""
        try {
            orderId = response!!.getString("id")
        } catch (e: JSONException) {
            Timber.e("Erreur PayPal JSON : ${e.localizedMessage}")
            showErrorToast(context)
        }
        return orderId
    }

    /**
     * Parse les réponses standards de ton API (success: true/false).
     */
    fun onApiResponse(response: JSONObject, context: Context): Boolean {
        var success = false
        try {
            success = response.getBoolean("success")
            if (!success) {
                val error = response.getString("error")
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        } catch (e: JSONException) {
            Timber.e(e)
            showErrorToast(context)
        }
        return success
    }

    /**
     * Nettoie une chaîne de caractères (ex : retire les guillemets de début et fin).
     */
    fun cleanKey(key: String): String {
        if (key.length < 2) return key
        return key.substring(1, key.length - 1)
    }

    private fun showErrorToast(context: Context) {
        Toast.makeText(
            context,
            "Erreur système, veuillez contacter le fournisseur de l'application",
            Toast.LENGTH_LONG
        ).show()
    }
}