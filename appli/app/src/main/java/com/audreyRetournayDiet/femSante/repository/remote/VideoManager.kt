package com.audreyRetournayDiet.femSante.repository.remote

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.API_URL
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.shared.UserStore
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

/**
 * Gestionnaire de récupération des flux vidéo sécurisés.
 *
 * Cette classe communique avec le backend pour transformer un titre de vidéo
 * en une URL de streaming exploitable. Elle assure la sécurité du contenu
 * en injectant systématiquement un jeton d'authentification Bearer.
 *
 * ### Flux de fonctionnement :
 * 1. L'application demande l'URL pour un titre donné (ex : "Méditation").
 * 2. Le serveur valide les droits d'accès (abonnement actif).
 * 3. Le serveur renvoie une URL JSON que le [com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity] pourra charger.
 */
class VideoManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userStore: UserStore
) {

    private val volley = Volley.newRequestQueue(this.context)

    /**
     * Interroge l'API pour obtenir l'URL de streaming d'une vidéo spécifique.
     *
     * @param title Le nom technique de la vidéo (doit correspondre au stockage serveur).
     * @param onComplete Callback retournant l'URL encapsulée dans un [ApiResult].
     */
    fun getVideoUrl(title: String, onComplete: (ApiResult<JSONObject>) -> Unit) {

        val urlBase = "${API_URL}/video/generate-url?video="
        val url = "$urlBase$title"

        Timber.d("Requête API Vidéo : $title | Endpoint: $url")

        // Création d'une requête personnalisée pour inclure les Headers d'authentification
        val request : JsonObjectRequest = object : JsonObjectRequest(Method.GET, url, null, { res ->
            try {
                val videoUrl = res.getString("url")
                val json = JSONObject().apply { put("url", videoUrl) }

                Timber.i("Succès : URL vidéo récupérée pour '$title'")
                onComplete(ApiResult.Success(json, "Vidéo prête à la lecture"))
            } catch (e: Exception) {
                Timber.e(e, "Erreur lors de la lecture du JSON vidéo pour $title")
                onComplete(ApiResult.Failure("Erreur technique lors de la préparation de la vidéo"))
            }
        }, { err ->
            // Gestion fine des erreurs HTTP (notamment l'expiration de session)
            val status = err.networkResponse?.statusCode
            Timber.e("Erreur réseau vidéo $title | Status HTTP: $status")

            val message = when (status) {
                401 -> "Accès refusé : votre session a expiré"
                403 -> "Abonnement requis pour voir cette vidéo"
                404 -> "Vidéo introuvable sur le serveur"
                else -> "Impossible de charger la vidéo pour le moment"
            }
            // On signale spécifiquement le 401 pour permettre un rafraîchissement de session.
            onComplete(ApiResult.Failure(message, isAuthError = status == 401))
        }) {
            /**
             * Injection du token personnel de l'utilisatrice (Sanctum) et de l'en-tête
             * Accept JSON (sans lui, le serveur tente une redirection "login" sur 401).
             */
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params.putAll(super.getHeaders())
                params["Authorization"] = "Bearer ${userStore.getToken().orEmpty()}"
                params["Accept"] = "application/json"
                return params
            }
        }

        volley.add(request)
    }
}