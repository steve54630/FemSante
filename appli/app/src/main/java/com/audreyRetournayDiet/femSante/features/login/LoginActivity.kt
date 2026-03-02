package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.features.main.HomeActivity
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber

/**
 * Activité d'entrée principale de l'application (Launcher).
 * * ### Responsabilités :
 * 1. **Auto-Login** : Vérifie si une session utilisateur existe via [UserStore].
 * 2. **Navigation** : Gère le basculement entre [LoginFragment], [CreateFragment] et [DocFragment].
 * 3. **In-App Updates** : Force la mise à jour immédiate si une version critique est disponible sur le Play Store.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var menu: BottomNavigationView
    private val loginFragment = LoginFragment()
    private val docFragment = DocFragment()
    private val registerFragment = CreateFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate : Démarrage de l'application")

        // --- 1. GESTION DE LA SESSION ---
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

        // --- 2. VÉRIFICATION DES MISES À JOUR ---
        checkInAppUpdate()

        // --- 3. INITIALISATION DE L'INTERFACE ---
        setContentView(R.layout.activity_login)
        menu = findViewById(R.id.bottom_navigation_menu)

        // Affichage du fragment par défaut au lancement
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, loginFragment)
                .commit()
        }

        setupNavigation()
    }

    /**
     * Configure la navigation via la BottomNavigationView.
     * Remplace dynamiquement le fragment affiché dans le conteneur principal.
     */
    private fun setupNavigation() {
        menu.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.login -> {
                    Timber.v("Navigation : Onglet Connexion")
                    loginFragment
                }
                R.id.pdf -> {
                    Timber.v("Navigation : Onglet Documents")
                    docFragment
                }
                R.id.register -> {
                    Timber.v("Navigation : Onglet Inscription")
                    registerFragment
                }
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, it)
                    .commit()
                true
            } ?: false
        }
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
                        // Nouveau contrat ActivityResult pour remplacer startActivityForResult (obsolète)
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