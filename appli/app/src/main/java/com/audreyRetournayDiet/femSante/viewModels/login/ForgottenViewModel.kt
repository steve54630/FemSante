package com.audreyRetournayDiet.femSante.viewModels.login

import android.util.Log
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import org.json.JSONObject

class ForgottenViewModel(
    private val userManager: UserManager,
    private val onLoading: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val onSuccess: (String) -> Unit
) {

    private val tag = "VM_FORGOTTEN_PWD"

    suspend fun changePassword(parameters: JSONObject) {
        val email = parameters.optString("email", "Inconnu")
        Log.d(tag, "Tentative de changement de mot de passe pour : $email")

        onLoading(true)

        when (val result = userManager.changePassword(parameters)) {
            is ApiResult.Success -> {
                Log.i(tag, "Succès : Mot de passe réinitialisé pour l'utilisateur : $email")
                onLoading(false)
                onSuccess(result.message)
            }

            is ApiResult.Failure -> {
                Log.w(tag, "Échec du changement de mot de passe ($email) : ${result.message}")
                onLoading(false)
                onError(result.message)
            }
        }
    }
}