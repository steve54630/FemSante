package com.audreyRetournayDiet.femSante.repository.remote

import android.content.Context
import android.util.Log
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.API_URL
import com.audreyRetournayDiet.femSante.AUTHORIZATION_HEADERS
import com.audreyRetournayDiet.femSante.repository.ApiResult
import org.json.JSONObject

class VideoManager(private val context: Context) {

    private val tag = "REPO_VIDEO"
    private val volley = Volley.newRequestQueue(this.context)

    fun getVideoUrl(title: String, onComplete: (ApiResult<JSONObject>) -> Unit) {

        val urlBase = "${API_URL}/video/generate-url?video="
        val url = "$urlBase$title"

        Log.d(tag, "Demande d'URL pour la vidéo : $title | URL: $url")

        val request : JsonObjectRequest = object : JsonObjectRequest(Method.GET, url, null, { res ->
            try {
                val videoUrl = res.getString("url")
                val json = JSONObject().apply { put("url", videoUrl) }

                Log.i(tag, "URL vidéo générée avec succès pour : $title")
                onComplete(ApiResult.Success(json, "Vidéo en préparation"))
            } catch (e: Exception) {
                Log.e(tag, "Erreur de parsing de l'URL vidéo pour $title", e)
                onComplete(ApiResult.Failure("Format de réponse vidéo invalide"))
            }
        }, { err ->
            val status = err.networkResponse?.statusCode
            Log.e(tag, "Échec récupération vidéo $title | Code HTTP: $status | Erreur: ${err.message}")

            val message = if (status == 401) "Session expirée ou accès refusé" else "Impossible de charger la vidéo"
            onComplete(ApiResult.Failure(message))
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params.putAll(super.getHeaders())
                params["Authorization"] = "Bearer $AUTHORIZATION_HEADERS"
                Log.v(this@VideoManager.tag, "Headers envoyés avec le token Bearer")
                return params
            }
        }

        volley.add(request)
    }
}