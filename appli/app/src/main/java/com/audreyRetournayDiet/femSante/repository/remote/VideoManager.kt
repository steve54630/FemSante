package com.audreyRetournayDiet.femSante.repository.remote

import android.content.Context
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.API_URL
import com.audreyRetournayDiet.femSante.AUTHORIZATION_HEADERS
import com.audreyRetournayDiet.femSante.repository.ApiResult
import org.json.JSONObject

class VideoManager(private val context: Context) {

    private val volley = Volley.newRequestQueue(this.context)

    fun getVideoUrl(title: String, onComplete: (ApiResult) -> Unit) {

        val urlBase =
            "${API_URL}/video/generate-url?video="

        val url = "$urlBase$title"

        val request : JsonObjectRequest = object : JsonObjectRequest(Method.GET, url, null, { res ->

            val json = JSONObject()
            json.put("url", res.getString("url"))

            onComplete(ApiResult.Success(json, "Vidéo en préparation"))
        }, { err ->

            onComplete(ApiResult.Failure(err.message ?: "Erreur inconnue"))
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params.putAll(super.getHeaders())
                params["Authorization"] = "Bearer $AUTHORIZATION_HEADERS"
                return params
            }
        }

        volley.add(request)
    }

}