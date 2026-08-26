package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.features.main.HomeActivity
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber

/**
 * Activité d'entrée principale de l'application (Launcher).
 * * ### Responsabilités :
 * 1. **Onboarding** : redirige vers [OnboardingActivity] au tout premier lancement.
 * 2. **Auto-Login** : Vérifie si une session utilisateur existe via [UserStore].
 * 3. **Navigation** : Connexion par défaut, avec liens vers [CreateFragment] et [DocFragment].
 * 4. **In-App Updates** : Force la mise à jour immédiate si une version critique est disponible sur le Play Store.
 */
class LoginActivity : AppCompatActivity() {

    private val loginFragment = LoginFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate : Démarrage de l'application")

        val userStore = UserStore(this)
        val savedUser = userStore.getUser()

        if (savedUser != null) {
            Timber.i("Session détectée : Redirection auto vers Home pour ${savedUser.email}")
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("SHOW_WELCOME_MESSAGE", true)
                putExtra("USER_EMAIL", savedUser.email)
            }
            startActivity(intent)
            finish() // On ferme LoginActivity pour ne pas revenir en arrière
            return
        }

        if (!userStore.hasSeenOnboarding()) {
            Timber.i("Premier lancement : redirection vers l'onboarding")
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        checkInAppUpdate()

        setContentView(R.layout.activity_login)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, loginFragment)
                .commit()
        }
    }

    /** Affiche un écran par-dessus la connexion, avec retour possible (bouton système). */
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun showRegister() {
        Timber.v("Navigation : Inscription")
        showFragment(CreateFragment())
    }

    fun showDocs() {
        Timber.v("Navigation : Documents légaux")
        showFragment(DocFragment())
    }

    fun showLogin() {
        Timber.v("Navigation : Connexion")
        supportFragmentManager.popBackStack()
    }

    /**
     * Utilise l'API Google Play Core pour vérifier si une mise à jour est disponible.
     * En cas de mise à jour "IMMEDIATE", l'utilisateur ne peut pas utiliser l'app sans l'installer.
     */
    private fun checkInAppUpdate() {
        Timber.d("Vérification de la disponibilité d'une mise à jour...")
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                Timber.i("Mise à jour immédiate disponible")
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
                            if (result.resultCode != RESULT_OK) {
                                Timber.e("Échec ou annulation de la mise à jour obligatoire")
                                Toast.makeText(applicationContext, "Mise à jour nécessaire pour continuer", Toast.LENGTH_SHORT).show()
                                // Optionnel : finish() si la mise à jour est réellement bloquante
                            }
                        },
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Erreur lors du lancement du flux de mise à jour")
                }
            } else {
                Timber.d("L'application est à jour")
            }
        }
    }
}