package com.audreyRetournayDiet.femSante.repository.remote

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.API_URL
import com.audreyRetournayDiet.femSante.AUTHORIZATION_HEADERS
import com.audreyRetournayDiet.femSante.repository.ApiResult
import org.json.JSONObject
import timber.log.Timber

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
class VideoManager(private val context: Context) {

    // Initialisation de la file de requêtes Volley pour le réseau
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
                // Extraction de l'URL brute depuis la réponse JSON du serveur
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
            onComplete(ApiResult.Failure(message))
        }) {
            /**
             * Injection du Token d'authentification pour sécuriser l'accès au média.
             * Le serveur rejette la requête si ce Header est absent ou invalide.
             */
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params.putAll(super.getHeaders())
                params["Authorization"] = "Bearer $AUTHORIZATION_HEADERS"
                Timber.v("Headers : Token Bearer injecté dans la requête")
                return params
            }
        }

        volley.add(request)
    }
}