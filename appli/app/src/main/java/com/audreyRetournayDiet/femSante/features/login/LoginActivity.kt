package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class LoginActivity : AppCompatActivity() {

    private val tag = "ACT_LOGIN"
    private lateinit var menu: BottomNavigationView
    private val login = LoginFragment()
    private val doc = DocFragment()
    private val register = CreateFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate : Démarrage de l'application")

        // 1. Initialiser le store et vérifier la session
        val userStore = UserStore(this)
        val savedUser = userStore.getUser()

        if (savedUser != null) {
            Log.i(tag, "Session détectée : Redirection auto vers Home pour ${savedUser.email}")
            val intent = Intent(this, HomeActivity::class.java).apply {
                putExtra("SHOW_WELCOME_MESSAGE", true)
                putExtra("USER_EMAIL", savedUser.email)
            }
            startActivity(intent)
            finish()
            return
        }

        Log.d(tag, "Aucune session active : Affichage de l'interface de connexion")

        // Vérification des mises à jour Play Store
        checkInAppUpdate()

        setContentView(R.layout.activity_login)

        menu = findViewById(R.id.bottom_navigation_menu)

        // Fragment par défaut
        supportFragmentManager.beginTransaction().replace(R.id.container, login).commit()

        menu.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.login -> {
                    Log.v(tag, "Navigation : Onglet Connexion")
                    login
                }
                R.id.pdf -> {
                    Log.v(tag, "Navigation : Onglet Documents")
                    doc
                }
                R.id.register -> {
                    Log.v(tag, "Navigation : Onglet Inscription")
                    register
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

    private fun checkInAppUpdate() {
        Log.d(tag, "Vérification de la disponibilité d'une mise à jour...")
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                Log.i(tag, "Mise à jour immédiate disponible")
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
                            if (result.resultCode != RESULT_OK) {
                                Log.e(tag, "Échec ou annulation de la mise à jour obligatoire")
                                Toast.makeText(applicationContext, "Mise à jour nécessaire pour continuer", Toast.LENGTH_SHORT).show()
                            }
                        },
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Erreur lors du lancement du flux de mise à jour", e)
                }
            } else {
                Log.d(tag, "L'application est à jour")
            }
        }
    }
}