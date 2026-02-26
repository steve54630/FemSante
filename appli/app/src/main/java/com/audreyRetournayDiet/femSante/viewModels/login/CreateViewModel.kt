package com.audreyRetournayDiet.femSante.viewModels.login

import com.audreyRetournayDiet.femSante.features.login.LoginActivity
import com.audreyRetournayDiet.femSante.features.login.PaymentActivity
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import org.json.JSONObject

class CreateViewModel(
    private val userManager: UserManager,
    private val onLoading: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val onSuccess: (Class<*>, HashMap<String, String>?) -> Unit
) {

    suspend fun test(params: JSONObject) {
        onLoading(true)
        val emailJson = JSONObject().apply { put("email", params.getString("email")) }

        // 1. Vérification de l'email
        when (val emailCheck = userManager.verifyEmail(emailJson)) {
            is ApiResult.Success -> {
                // 2. Création de l'utilisateur (cas test / gratuit)
                when (val createCheck = userManager.createUser(params)) {
                    is ApiResult.Success -> {
                        onLoading(false)
                        onSuccess(LoginActivity::class.java, null)
                    }
                    is ApiResult.Failure -> {
                        onLoading(false)
                        onError(createCheck.message)
                    }
                }
            }
            is ApiResult.Failure -> {
                onLoading(false)
                onError(emailCheck.message)
            }
        }
    }

    suspend fun subscribe(params: JSONObject, map: HashMap<String, String>) {
        onLoading(true)
        val emailJson = JSONObject().apply { put("email", params.getString("email")) }

        when (val result = userManager.verifyEmail(emailJson)) {
            is ApiResult.Success -> {
                onLoading(false)
                onSuccess(PaymentActivity::class.java, map)
            }
            is ApiResult.Failure -> {
                onLoading(false)
                onError(result.message)
            }
        }
    }
}