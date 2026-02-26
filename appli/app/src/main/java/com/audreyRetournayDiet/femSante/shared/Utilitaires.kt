package com.audreyRetournayDiet.femSante.shared

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.VideoManager
import org.json.JSONException
import org.json.JSONObject

object Utilitaires {

    fun showToast(msg: String, context : Context) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

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
                    map["Title"]=titre
                    map["URL"] = apiResult.data!!.getString("url")
                    Log.e("url", map["URL"]!!)
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

    fun isValidEmail(email: String): Boolean {
        val pattern = "^[A-Za-z0-9]+[A-Za-z0-9_.-]+@[a-z0-9-.]+[.]+[a-z.-]{2,3}$"
        return email.matches(pattern.toRegex())
    }

    fun isValidPassword(password: String): Boolean {
        val pattern = "^(?=.*[A-Z])(?=.*[@$!%*#?&/_])(?=.*[a-z])[A-Za-z\\d@$!%*#?&/]{8,}$"
        return password.matches(pattern.toRegex())
    }

    fun onPayPalApiResponse(context: Context, response: JSONObject?): String {

        var orderId = ""

        try {
            orderId = response!!.getString("id")
        } catch (e: JSONException) {
            Log.e("Connexion", e.localizedMessage!!)
            Toast.makeText(
                context,
                "Erreur système, veuillez contacter le fournisseur de l'application",
                Toast.LENGTH_LONG
            ).show()
        }

        return orderId
    }

    fun onApiResponse(response: JSONObject, context: Context): Boolean {
        var success = false

        try {
            success = response.getBoolean("success")
            if (!success) {
                val error = response.getString("error")
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(
                context,
                "Erreur système, veuillez contacter le fournisseur de l'application",
                Toast.LENGTH_LONG
            ).show()
        }

        return success
    }

    fun cleanKey(key: String): String {

        var keyTreated = key.substring(1)
        keyTreated = keyTreated.substring(0, keyTreated.length - 1)

        return keyTreated
    }

}