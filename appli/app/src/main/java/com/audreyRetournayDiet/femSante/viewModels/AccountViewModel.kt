package com.audreyRetournayDiet.femSante.viewModels

import androidx.lifecycle.ViewModel
import com.audreyRetournayDiet.femSante.data.entities.AppUser
import com.audreyRetournayDiet.femSante.shared.UserStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * ViewModel gérant les informations du compte et la fin de session.
 *
 * ### Rôle :
 * 1. Extraire les informations de l'utilisatrice depuis le stockage local ([UserStore]).
 * 2. Gérer la déconnexion sécurisée en nettoyant les préférences.
 * 3. Notifier l'UI pour rediriger vers l'écran de Login si aucune session n'est active.
 */
class AccountViewModel(
    private val userStore: UserStore
) : ViewModel() {

    /**
     * Représente les différents états possibles de l'écran Mon Compte.
     */
    sealed class AccountState {
        object Idle : AccountState() // État initial au chargement
        data class Success(val user: AppUser) : AccountState() // Profil prêt à l'affichage
        object LoggedOut : AccountState() // Signal pour quitter l'écran
    }

    private val _state = MutableStateFlow<AccountState>(AccountState.Idle)
    val state: StateFlow<AccountState> = _state

    /**
     * Charge les données de l'utilisatrice.
     * Si aucune donnée n'est trouvée, déclenche l'état [com.audreyRetournayDiet.femSante.viewModels.AccountViewModel.AccountState.LoggedOut].
     */
    fun loadUserProfile() {
        Timber.d("Tentative de chargement du profil utilisateur depuis le UserStore")

        val user = userStore.getUser()
        if (user != null) {
            Timber.i("Profil chargé avec succès pour l'utilisateur ID: ${user.id}")
            _state.value = AccountState.Success(user)
        } else {
            Timber.w("Aucune session active trouvée. Redirection vers LoggedOut.")
            _state.value = AccountState.LoggedOut
        }
    }

    /**
     * Supprime les jetons d'accès et les infos utilisateur du téléphone.
     * Effectue un audit de sécurité pour confirmer l'effacement.
     */
    fun logout() {
        Timber.i("Action de déconnexion déclenchée par l'utilisateur.")
        userStore.clearSession()

        // Vérification de sécurité pour s'assurer qu'aucune donnée résiduelle ne persiste
        if (userStore.getUser() == null) {
            Timber.d("Session effacée avec succès du stockage local.")
        } else {
            Timber.e("Erreur critique : La session n'a pas pu être effacée !")
        }

        _state.value = AccountState.LoggedOut
    }
}