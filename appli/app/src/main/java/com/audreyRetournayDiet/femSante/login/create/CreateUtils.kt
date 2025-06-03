package com.audreyRetournayDiet.femSante.login.create

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.audreyRetournayDiet.femSante.login.LoginActivity
import com.audreyRetournayDiet.femSante.login.payment.PaymentActivity
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.UserManager
import org.json.JSONObject

class CreateUtils(
    private val context: Context,
    private val userManager: UserManager,
) {

    fun test(params: JSONObject) {

        UserManager(context).verifyEmail(JSONObject().apply {
            put("email", params.getString("email"))
        }) { result ->

            when (result) {
                is ApiResult.Success -> {
                    userManager.createUser(params) { apiResult ->

                        when (apiResult) {
                            is ApiResult.Success -> {
                                Toast.makeText(
                                    context,
                                    apiResult.message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(context, LoginActivity::class.java)

                                context.startActivity(intent)
                            }

                            is ApiResult.Failure -> {
                                Toast.makeText(
                                    context,
                                    apiResult.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }
                }

                is ApiResult.Failure -> {
                    Toast.makeText(
                        context,
                        result.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    }

    fun subscribe(parameters: JSONObject, map: HashMap<String, String>) {
        UserManager(context).verifyEmail(parameters) { result ->

            when (result) {
                is ApiResult.Success -> {
                    val intent = Intent(context, PaymentActivity::class.java)
                    intent.putExtra("map", map)
                    context.startActivity(intent)
                }

                is ApiResult.Failure -> {
                    Toast.makeText(
                        context,
                        result.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

    }
}