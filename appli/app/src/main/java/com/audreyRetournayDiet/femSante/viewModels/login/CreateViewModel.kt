package com.audreyRetournayDiet.femSante.viewModels.login

import android.util.Log
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

    private val tag = "VM_CREATE_USER"

    suspend fun test(params: JSONObject) {
        Log.d(tag, "Début du processus de test (inscription gratuite)")
        onLoading(true)

        val email = params.optString("email")
        val emailJson = JSONObject().apply { put("email", email) }

        // 1. Vérification de l'email
        Log.d(tag, "Vérification disponibilité email : $email")
        when (val emailCheck = userManager.verifyEmail(emailJson)) {
            is ApiResult.Success -> {
                Log.i(tag, "Email disponible. Tentative de création de compte...")

                // 2. Création de l'utilisateur
                when (val createCheck = userManager.createUser(params)) {
                    is ApiResult.Success -> {
                        Log.i(tag, "Compte créé avec succès pour : $email")
                        onLoading(false)
                        onSuccess(LoginActivity::class.java, null)
                    }
                    is ApiResult.Failure -> {
                        Log.e(tag, "Échec création compte : ${createCheck.message}")
                        onLoading(false)
                        onError(createCheck.message)
                    }
                }
            }
            is ApiResult.Failure -> {
                Log.w(tag, "Email déjà utilisé ou invalide ($email) : ${emailCheck.message}")
                onLoading(false)
                onError(emailCheck.message)
            }
        }
    }

    suspend fun subscribe(params: JSONObject, map: HashMap<String, String>) {
        Log.d(tag, "Début du processus d'abonnement (paiement)")
        onLoading(true)

        val email = params.optString("email")
        val emailJson = JSONObject().apply { put("email", email) }

        Log.d(tag, "Vérification email avant paiement : $email")
        when (val result = userManager.verifyEmail(emailJson)) {
            is ApiResult.Success -> {
                Log.i(tag, "Email validé. Redirection vers l'écran de paiement.")
                onLoading(false)
                onSuccess(PaymentActivity::class.java, map)
            }
            is ApiResult.Failure -> {
                Log.w(tag, "Interruption abonnement : Email non valide ou déjà pris ($email)")
                onLoading(false)
                onError(result.message)
            }
        }
    }
}