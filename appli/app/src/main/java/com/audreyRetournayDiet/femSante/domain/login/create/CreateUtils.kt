package com.audreyRetournayDiet.femSante.domain.login.create

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.audreyRetournayDiet.femSante.domain.login.LoginActivity
import com.audreyRetournayDiet.femSante.domain.login.payment.PaymentActivity
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import org.json.JSONObject

class CreateUtils(
    private val context: Context,
    private val userManager: UserManager,
) {

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    suspend fun test(params: JSONObject) {
        val emailJson = JSONObject().apply { put("email", params.getString("email")) }

        // Vérifier si l'email est valide
        when (val result = userManager.verifyEmail(emailJson)) {
            is ApiResult.Success -> {
                // Créer l'utilisateur
                val apiResult = userManager.createUser(params)
                if (apiResult is ApiResult.Success<JSONObject>) {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                }
                else if (apiResult is ApiResult.Failure) {
                    showToast(result.message)
                }
            }
            is ApiResult.Failure -> showToast(result.message)
        }
    }

    suspend fun subscribe(parameters: JSONObject, map: HashMap<String, String>) {
            when (val result = userManager.verifyEmail(parameters) ) {
                is ApiResult.Success -> {
                    val intent = Intent(context, PaymentActivity::class.java)
                    intent.putExtra("map", map)
                    context.startActivity(intent)
                }
                is ApiResult.Failure -> showToast(result.message)
            }
        }
    }
