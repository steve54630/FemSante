package com.audreyRetournayDiet.femSante.shared

import android.app.Activity
import android.content.Intent
import com.audreyRetournayDiet.femSante.features.login.LoginActivity

/**
 * Réaction commune à une session irrécupérable (refresh silencieux échoué) : on efface
 * la session locale et on renvoie l'utilisatrice vers l'écran de connexion, sans laisser
 * d'écran précédent dans la pile.
 */
fun Activity.redirectToLoginAfterSessionExpiry() {
    UserStore(this).clearSession()
    startActivity(
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    )
    finish()
}
