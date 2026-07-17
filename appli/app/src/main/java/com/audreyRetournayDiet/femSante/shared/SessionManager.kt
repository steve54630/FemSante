package com.audreyRetournayDiet.femSante.shared

import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralise la session utilisateur côté app : accès au token et **rafraîchissement
 * silencieux** en cas de token expiré/révoqué.
 *
 * Stratégie hybride (cf. auth Sanctum) : quand un appel protégé renvoie 401, on tente
 * d'abord [refreshToken] — une reconnexion transparente avec les identifiants déjà
 * stockés — pour ne renvoyer l'utilisatrice vers l'écran de login **que** si cette
 * reconnexion échoue.
 */
@Singleton
class SessionManager @Inject constructor(
    private val userManager: UserManager,
    private val userStore: UserStore
) {

    fun getToken(): String? = userStore.getToken()

    fun clearSession() = userStore.clearSession()

    /**
     * Rejoue une connexion avec les identifiants stockés localement pour obtenir un
     * nouveau token. Retourne `true` si un token frais a été récupéré et sauvegardé.
     *
     * Tout ce qui n'est pas un succès franc (identifiants invalides, abonnement expiré,
     * réseau…) est considéré comme un échec → l'appelant basculera vers le login manuel.
     */
    suspend fun refreshToken(): Boolean {
        val user = userStore.getUser() ?: return false

        val params = JSONObject().apply {
            put("email", user.email)
            put("password", user.password)
        }

        return when (val result = userManager.connectUser(params)) {
            is ApiResult.Success -> {
                val token = result.data?.optString("token").orEmpty()
                if (token.isNotBlank()) {
                    userStore.saveToken(token)
                    Timber.i("Session rafraîchie silencieusement (nouveau token).")
                    true
                } else {
                    Timber.w("Refresh : réponse sans token.")
                    false
                }
            }
            is ApiResult.Failure -> {
                Timber.w("Refresh silencieux échoué : ${result.message}")
                false
            }
        }
    }
}
