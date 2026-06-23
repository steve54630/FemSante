package com.audreyRetournayDiet.femSante.viewModels.login

import com.audreyRetournayDiet.femSante.features.login.LoginActivity
import com.audreyRetournayDiet.femSante.features.login.PaymentActivity
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import org.json.JSONObject
import timber.log.Timber

/**
 * Gère la logique de création de compte et le tunnel d'abonnement.
 * * Ce composant utilise des callbacks (`onLoading`, `onError`, `onSuccess`) pour
 * communiquer avec l'UI, ce qui le rend très flexible pour être utilisé
 * dans des Fragments ou des Activities.
 */
class CreateViewModel(
    private val userManager: UserManager,
    private val onLoading: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val onSuccess: (Class<*>, HashMap<String, String>?) -> Unit
) {

    /**
     * Processus d'inscription standard (Gratuit).
     * Effectue une double vérification : disponibilité de l'email PUIS création.
     */
    suspend fun test(params: JSONObject) {
        Timber.d("Début du processus d'inscription gratuite")
        onLoading(true)

        val email = params.optString("email")
        val emailJson = JSONObject().apply { put("email", email) }

        // 1. Vérification de la disponibilité de l'email
        Timber.d("Vérification disponibilité email : $email")
        when (val emailCheck = userManager.verifyEmail(emailJson)) {
            is ApiResult.Success -> {
                Timber.i("Email disponible. Tentative de création...")

                // 2. Création effective de l'utilisateur
                when (val createCheck = userManager.createUser(params)) {
                    is ApiResult.Success -> {
                        Timber.i("Compte créé avec succès pour : $email")
                        onLoading(false)
                        onSuccess(LoginActivity::class.java, null)
                    }
                    is ApiResult.Failure -> {
                        Timber.e("Échec création compte : ${createCheck.message}")
                        onLoading(false)
                        onError(createCheck.message)
                    }
                }
            }
            is ApiResult.Failure -> {
                Timber.w("Email indisponible ou invalide : $email")
                onLoading(false)
                onError(emailCheck.message)
            }
        }
    }

    /**
     * Processus d'abonnement payant.
     * Vérifie l'email avant de rediriger l'utilisatrice vers la plateforme de paiement.
     */
    suspend fun subscribe(params: JSONObject, map: HashMap<String, String>) {
        Timber.d("Début du processus d'abonnement (paiement)")
        onLoading(true)

        val email = params.optString("email")
        val emailJson = JSONObject().apply { put("email", email) }

        Timber.d("Vérification email avant redirection paiement : $email")
        when (val result = userManager.verifyEmail(emailJson)) {
            is ApiResult.Success -> {
                Timber.i("Email validé. Redirection vers PaymentActivity.")
                onLoading(false)
                // On passe la Map contenant les détails de l'offre choisie
                onSuccess(PaymentActivity::class.java, map)
            }
            is ApiResult.Failure -> {
                Timber.w("Interruption : Email déjà pris ou invalide.")
                onLoading(false)
                onError(result.message)
            }
        }
    }
}