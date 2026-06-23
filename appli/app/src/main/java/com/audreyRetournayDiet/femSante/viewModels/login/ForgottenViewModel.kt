package com.audreyRetournayDiet.femSante.viewModels.login

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import org.json.JSONObject
import timber.log.Timber

/**
 * ViewModel dédié à la procédure de réinitialisation du mot de passe.
 * * ### Fonctionnement :
 * Ce composant agit comme un médiateur entre la vue (Activity/Fragment) et le [UserManager].
 * Il utilise des fonctions d'ordre supérieur (callbacks) pour notifier l'UI de l'état
 * de la requête réseau.
 */
class ForgottenViewModel(
    private val userManager: UserManager,
    private val onLoading: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val onSuccess: (String) -> Unit
) {

    /**
     * Lance la procédure de changement de mot de passe auprès de l'API.
     * * @param parameters Objet JSON contenant au minimum l'email et le nouveau mot de passe.
     */
    suspend fun changePassword(parameters: JSONObject) {
        val email = parameters.optString("email", "Inconnu")
        Timber.d("Tentative de changement de mot de passe pour : $email")

        // Active l'indicateur visuel de chargement sur l'UI
        onLoading(true)

        when (val result = userManager.changePassword(parameters)) {
            is ApiResult.Success -> {
                Timber.i("Succès : Mot de passe réinitialisé pour l'utilisateur : $email")
                onLoading(false)
                // On transmet le message de succès (ex : "Un email de confirmation a été envoyé")
                onSuccess(result.message)
            }

            is ApiResult.Failure -> {
                Timber.w("Échec du changement de mot de passe ($email) : ${result.message}")
                onLoading(false)
                // On transmet l'erreur métier (ex : "Email non trouvé" ou "Mot de passe trop court")
                onError(result.message)
            }
        }
    }
}