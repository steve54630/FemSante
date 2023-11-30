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
import com.audreyRetournayDiet.femSante.CONNECT_USER_API
import com.audreyRetournayDiet.femSante.CREATE_USER_API
import com.audreyRetournayDiet.femSante.FORGOTTEN_PASSWORD_API
import com.audreyRetournayDiet.femSante.UPDATE_USER_API
import com.audreyRetournayDiet.femSante.login.PaymentActivity
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
        readRequest(CREATE_USER_API, context, activity, intent, "Inscription réussie", parameters)
    }

    fun connectUser(
        parameters: JSONObject,
        context: Context,
        activity: AppCompatActivity,
        intent: Intent,
    ) {
        readRequest(CONNECT_USER_API, context, activity, intent, "Connexion réussie", parameters)
    }

    fun updateUser(
        parameters: JSONObject,
        context: Context,
        activity: AppCompatActivity,
        intent: Intent,
    ) {
        val request = JsonObjectRequest(Request.Method.POST, UPDATE_USER_API, parameters, { response ->
            val result = Utilitaires.onApiResponse(response, context)
            if (result) {
                Toast.makeText(context, "Mise à jour de l'abonnement effectué", Toast.LENGTH_SHORT)
                    .show()
                activity.finish()
                context.startActivity(intent)
            } else {
                Toast.makeText(context, response.getString("error"), Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            Toast.makeText(context, "Erreur de connexion", Toast.LENGTH_LONG).show()
            Log.e("Connexion", error.localizedMessage!!)
        })

        this.queue!!.add(request)
    }

    fun changePassword(
        parameters: JSONObject,
        context: Context,
        activity: AppCompatActivity,
        intent: Intent,
    ) {


        readRequest(FORGOTTEN_PASSWORD_API, context, activity, intent, "Mot de passe changé", parameters)
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
                val map = HashMap<String, String>()
                map["login"] = parameters!!["email"].toString()
                map["password"] = parameters["password"].toString()

                intent.putExtra("A vie", response.getBoolean("A vie"))
                intent.putExtra("map", map)

                activity.finish()
                context.startActivity(intent)
            } else {
                Toast.makeText(context, response.getString("error"), Toast.LENGTH_SHORT)
                    .show()
                if (response.getBoolean("repay")) {
                    val intentRepay = Intent(context, PaymentActivity::class.java)

                    val parametersRepay = HashMap<String, String>()
                    parametersRepay["email"] = parameters!!["email"].toString()
                    parametersRepay["password"] = parameters["password"].toString()

                    intentRepay.putExtra("repay", true)
                    intentRepay.putExtra("map", parametersRepay)
                    intentRepay.putExtra("update", "Non")

                    context.startActivity(intentRepay)
                }
            }
        }, { error ->
            Toast.makeText(context, "Erreur de connexion", Toast.LENGTH_LONG).show()
            Log.e("Connexion", error.localizedMessage!!)
        })

        this.queue!!.add(request)
    }

}