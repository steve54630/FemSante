package com.audreyRetournayDiet.femSante.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.audreyRetournayDiet.femSante.data.entities.AppUser
import com.audreyRetournayDiet.femSante.shared.UserStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AccountViewModel(
    private val userStore: UserStore
) : ViewModel() {

    private val tag = "VM_ACCOUNT"

    // État de l'écran : contient l'utilisateur ou un signal de déconnexion
    sealed class AccountState {
        object Idle : AccountState()
        data class Success(val user: AppUser) : AccountState()
        object LoggedOut : AccountState()
    }

    private val _state = MutableStateFlow<AccountState>(AccountState.Idle)
    val state: StateFlow<AccountState> = _state

    // Charger les infos au démarrage
    fun loadUserProfile() {
        Log.d(tag, "Tentative de chargement du profil utilisateur depuis le UserStore")

        val user = userStore.getUser()
        if (user != null) {
            Log.i(tag, "Profil chargé avec succès pour l'utilisateur ID: ${user.id}")
            _state.value = AccountState.Success(user)
        } else {
            Log.w(tag, "Aucune session active trouvée dans le UserStore. Redirection vers LoggedOut.")
            _state.value = AccountState.LoggedOut
        }
    }

    // Action de déconnexion
    fun logout() {
        Log.i(tag, "Action de déconnexion déclenchée par l'utilisateur.")
        userStore.clearSession()

        // On vérifie que le clear a bien fonctionné (Audit de sécurité)
        if (userStore.getUser() == null) {
            Log.d(tag, "Session effacée avec succès du stockage local.")
        } else {
            Log.e(tag, "Erreur critique : La session n'a pas pu être effacée du UserStore !")
        }

        _state.value = AccountState.LoggedOut
    }
}