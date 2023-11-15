package com.audreyRetournayDiet.femSante.utilitaires

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class DatabaseManager(context: Context) {

    private var context: Context? = null
    private var queue: RequestQueue? = null

    init {
        this.context = context
        this.queue = Volley.newRequestQueue(context)
    }

    fun createUser(
        parameters: JSONObject,
        context: Context,
        activity: AppCompatActivity,
        intent: Intent,
    ) {
        val url =
            "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/createAccountNew.php"

        readRequest(url, context, activity, intent, "Inscription réussie", parameters)
    }

    fun connectUser(
        parameters: JSONObject,
        context: Context,
        activity: AppCompatActivity,
        intent: Intent,
    ) {
        val url =
            "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/connectUser.php"

       readRequest(url, context, activity, intent, "Connexion réussie", parameters)
    }

    fun changePassword(
        parameters: JSONObject,
        context: Context,
        activity: AppCompatActivity,
        intent: Intent,
    ) {
        val url =
            "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/forgottenPassword.php"

        readRequest(url, context, activity, intent, "Mot de passe changé", parameters)
    }

    private fun readRequest(
        url: String,
        context: Context,
        activity: AppCompatActivity,
        intent: Intent,
        message: String?,
        parameters: JSONObject?,
    ) {

        val request = JsonObjectRequest(Request.Method.POST, url, parameters, { response ->
            val result = Utilitaires.onApiResponse(response, context)
            if (result) {
                if (message != null) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                activity.finish()
                context.startActivity(intent)
            }
        }, { error ->
            Toast.makeText(context, "Erreur de connexion", Toast.LENGTH_LONG).show()
            Log.e("Connexion", error.localizedMessage!!)
        })

        this.queue!!.add(request)
    }

}