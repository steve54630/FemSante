package com.audreyRetournayDiet.femSante.features.login

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.features.login.CreateFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class LoginActivity : AppCompatActivity() {

    private lateinit var menu: BottomNavigationView
    private var login = LoginFragment()
    private var doc = DocFragment()
    private var register = CreateFragment()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        checkInAppUpdate()

        setContentView(R.layout.activity_login)

        menu = findViewById(R.id.bottom_navigation_menu)

        supportFragmentManager.beginTransaction().replace(R.id.container, login).commit()

        menu.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.login ->{ supportFragmentManager.beginTransaction().replace(R.id.container, login)
                    .commit()
                    return@setOnItemSelectedListener true
                }
                R.id.pdf -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, doc)
                        .commit()
                    return@setOnItemSelectedListener true
                }
                R.id.register -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, register)
                        .commit()
                    return@setOnItemSelectedListener true
                }
            }

            return@setOnItemSelectedListener false

        }
    }

    private fun checkInAppUpdate() {

        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // an activity result launcher registered via registerForActivityResult
                    registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
                        // handle callback
                        if (result.resultCode != RESULT_OK) {
                            Toast.makeText(applicationContext, "Erreur lors de la mise à jour. Mise à jour nécessaire", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
            }
        }
    }

}