package com.audreyRetournayDiet.femSante.utilitaires

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
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
        activity: Activity,
        intent: Intent,
        alert: LoadingAlert
    ) {
        readRequest(CREATE_USER_API, context, activity, intent, "Inscription réussie", parameters, alert, "create")
    }

    fun connectUser(
        parameters: JSONObject,
        context: Context,
        activity: Activity,
        intent: Intent,
        alert: LoadingAlert
    ) {
        readRequest(CONNECT_USER_API, context, activity, intent, "Connexion réussie", parameters, alert, "connect")
    }

    fun changePassword(
        parameters: JSONObject,
        context: Context,
        activity: Activity,
        intent: Intent,
        alert: LoadingAlert,
    ) {

        readRequest(FORGOTTEN_PASSWORD_API, context, activity, intent, "Mot de passe changé", parameters, alert, "change")
    }

    fun updateUser(
        parameters: JSONObject,
        context: Context,
        activity: Activity,
        intent: Intent,
        alert: LoadingAlert
    ) {
        val request = JsonObjectRequest(Request.Method.POST, UPDATE_USER_API, parameters, { response ->
            val result = Utilitaires.onApiResponse(response, context)
            if (result) {
                Toast.makeText(context, "Mise à jour de l'abonnement effectué", Toast.LENGTH_SHORT)
                    .show()

                alert.closeAlertDialog()
                activity.finish()
                context.startActivity(intent)
            } else {
                alert.closeAlertDialog()
                Toast.makeText(context, response.getString("error"), Toast.LENGTH_SHORT).show()
            }
        }, { error ->
            alert.closeAlertDialog()
            Toast.makeText(context, "Erreur de connexion", Toast.LENGTH_LONG).show()
            Log.e("Connexion", error.localizedMessage!!)
        })

        this.queue!!.add(request)
    }

    private fun readRequest(
        url: String,
        context: Context,
        activity: Activity,
        intent: Intent,
        message: String?,
        parameters: JSONObject?,
        alert : LoadingAlert,
        type : String
    ) {

        val request = JsonObjectRequest(Request.Method.POST, url, parameters, { response ->
            val result = Utilitaires.onApiResponse(response, context)

            val map = HashMap<String, String>()
            map["login"] = parameters!!["email"].toString()
            map["password"] = parameters["password"].toString()

            if (result) {
                alert.closeAlertDialog()
                if (message != null) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                if (type == "connect") {

                intent.putExtra("A vie", response.getBoolean("A vie"))
                intent.putExtra("map", map)}

                activity.finish()
                context.startActivity(intent)
            } else {
                alert.closeAlertDialog()
                Toast.makeText(context, response.getString("error"), Toast.LENGTH_SHORT)
                    .show()
                if (url == UPDATE_USER_API && response.getBoolean("repay")) {
                    val intentRepay = Intent(context, PaymentActivity::class.java)

                    intentRepay.putExtra("repay", true)
                    intentRepay.putExtra("map", map)
                    intentRepay.putExtra("update", "Non")

                    context.startActivity(intentRepay)
                }
            }
        }, { error ->
            alert.closeAlertDialog()
            Toast.makeText(context, "Erreur de connexion", Toast.LENGTH_LONG).show()
            Log.e("Connexion", error.localizedMessage!!)
        })

        this.queue!!.add(request)
    }

}