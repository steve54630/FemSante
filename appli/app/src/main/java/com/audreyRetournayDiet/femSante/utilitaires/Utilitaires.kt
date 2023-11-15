package com.audreyRetournayDiet.femSante.utilitaires

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.login.LoginActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.regex.Pattern


object Utilitaires {

    fun videoLaunch(
        titre: String?,
        pdf: String?,
        intent: Intent,
        context: Context,
    ) {
        val extrasVideo = Bundle()
        val map = HashMap<String, String>()
        map["Title"] = titre!!
        map["PDF"] = pdf!!
        extrasVideo.putSerializable("map", map)

        intent.putExtras(extrasVideo)

        context.startActivity(intent)
    }

    fun isValidEmail(email: String): Boolean {
        val pattern: Pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
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

    fun cleanKey(key : String) : String {

        var keyTreated = key.substring(1)
        keyTreated = keyTreated.substring(0, keyTreated.length - 1)

        return keyTreated
    }

    fun registerCreation(databaseManager : DatabaseManager, parameters: JSONObject, packageContext: Context, activity: AppCompatActivity) {
        val intent = Intent(packageContext, LoginActivity::class.java)

        databaseManager.createUser(
            parameters,
            packageContext,
            activity,
            intent
        )
    }
}