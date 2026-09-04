package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.subscription.SubscriptionOffer
import com.audreyRetournayDiet.femSante.data.subscription.SubscriptionOffers
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

/**
 * Écran d'annonce du premium : affiché quand une utilisatrice sans accès touche un contenu
 * verrouillé (média bien-être ou fiche micronutriment), à la place d'un simple message bloquant.
 * Explique les avantages, propose les tarifs (choix mémorisé) puis redirige vers le tunnel de
 * paiement, offre pré-sélectionnée.
 */
class PremiumUpsellActivity : AppCompatActivity() {

    private var selectedOffer: SubscriptionOffer = SubscriptionOffers.all.first { it.recommended }
    private val offerCards = mutableMapOf<SubscriptionOffer, MaterialCardView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium_upsell)

        buildOfferCards()
        refreshOfferSelection()

        findViewById<MaterialButton>(R.id.buttonSubscribe).setOnClickListener {
            val user = UserStore(this).getUser()
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("map", hashMapOf("email" to (user?.email ?: ""), "password" to (user?.password ?: "")))
                putExtra("repay", true)
                putExtra("update", "Oui")
                putExtra("preselectedOfferKey", selectedOffer.key)
            }
            startActivity(intent)
            finish()
        }

        findViewById<MaterialButton>(R.id.buttonLater).setOnClickListener { finish() }
    }

    private fun buildOfferCards() {
        val container = findViewById<LinearLayout>(R.id.offersContainer)
        (listOf(SubscriptionOffers.FREE_TRIAL) + SubscriptionOffers.all).forEach { offer ->
            val card = layoutInflater.inflate(R.layout.item_subscription_offer, container, false) as MaterialCardView
            card.findViewById<TextView>(R.id.textPeriod).text = offer.periodLabel
            card.findViewById<TextView>(R.id.textPrice).text = offer.price
            card.findViewById<TextView>(R.id.textDescription).text = offer.description
            card.findViewById<TextView>(R.id.textMonthly).apply {
                visibility = if (offer.monthlyEquivalent != null) View.VISIBLE else View.GONE
                text = offer.monthlyEquivalent
            }
            card.findViewById<TextView>(R.id.textBadge).apply {
                visibility = if (offer.recommended) View.VISIBLE else View.GONE
                text = getString(R.string.premium_upsell_recommended_badge)
            }
            card.setOnClickListener {
                selectedOffer = offer
                refreshOfferSelection()
            }
            offerCards[offer] = card
            container.addView(card)
        }
    }

    private fun refreshOfferSelection() {
        offerCards.forEach { (offer, card) ->
            card.strokeColor = ContextCompat.getColor(
                this,
                if (offer.key == selectedOffer.key) R.color.orange_app else android.R.color.transparent
            )
        }

        val isFreeTrial = selectedOffer.key == SubscriptionOffers.FREE_TRIAL.key
        findViewById<MaterialButton>(R.id.buttonSubscribe).text = getString(
            if (isFreeTrial) R.string.free_trial_activate_button else R.string.premium_upsell_cta
        )
    }
}
