package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.google.android.material.button.MaterialButton

/**
 * Écran d'annonce du premium : affiché quand une utilisatrice sans accès touche un contenu
 * verrouillé (média bien-être ou fiche micronutriment), à la place d'un simple message bloquant.
 * Explique les avantages puis redirige vers le tunnel de paiement (mise à jour d'abonnement).
 */
class PremiumUpsellActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_upsell)

        findViewById<MaterialButton>(R.id.buttonSubscribe).setOnClickListener {
            val user = UserStore(this).getUser()
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("map", hashMapOf("email" to (user?.email ?: ""), "password" to (user?.password ?: "")))
                putExtra("repay", true)
                putExtra("update", "Oui")
            }
            startActivity(intent)
            finish()
        }

        findViewById<MaterialButton>(R.id.buttonLater).setOnClickListener { finish() }
    }
}
