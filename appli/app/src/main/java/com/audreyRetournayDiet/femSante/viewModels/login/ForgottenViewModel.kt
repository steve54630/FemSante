package com.audreyRetournayDiet.femSante.viewModels.login

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import org.json.JSONObject

class ForgottenViewModel(
    private val userManager: UserManager,
    private val onLoading: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val onSuccess: (String) -> Unit
) {

    suspend fun changePassword(parameters: JSONObject) {
        onLoading(true)

        when (val result = userManager.changePassword(parameters)) {
            is ApiResult.Success -> {
                onLoading(false)
                onSuccess(result.message ?: "Mot de passe modifié avec succès")
            }

            is ApiResult.Failure -> {
                onLoading(false)
                onError(result.message)
            }
        }
    }
}