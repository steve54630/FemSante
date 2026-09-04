package com.audreyRetournayDiet.femSante.data.subscription

/**
 * Une offre d'abonnement premium.
 *
 * [key] suit le format historique "durée_jours;prix_facial" (ex. "365;69.90" ou "A vie;250.00"),
 * consommé tel quel par [com.audreyRetournayDiet.femSante.viewmodels.login.PaymentViewModel]
 * pour calculer le prix final et déterminer l'éligibilité aux réductions.
 */
data class SubscriptionOffer(
    val key: String,
    val label: String,
    val periodLabel: String,
    val price: String,
    val monthlyEquivalent: String? = null,
    val description: String,
    val recommended: Boolean = false
)

/** Source unique des tarifs premium, partagée entre l'écran d'accroche et le tunnel de paiement. */
object SubscriptionOffers {

    val all = listOf(
        SubscriptionOffer(
            key = "30;9.90",
            label = "1 mois : 9,90€",
            periodLabel = "1 MOIS",
            price = "9,90 €",
            description = "Sans engagement, résiliable à tout moment"
        ),
        SubscriptionOffer(
            key = "90;19.90",
            label = "3 mois : 19,90€",
            periodLabel = "3 MOIS",
            price = "19,90 €",
            monthlyEquivalent = "6,63 € / mois",
            description = "Pour installer durablement tes routines à chaque cycle"
        ),
        SubscriptionOffer(
            key = "365;69.90",
            label = "12 mois : 69,90€",
            periodLabel = "12 MOIS",
            price = "69,90 €",
            monthlyEquivalent = "5,82 € / mois",
            description = "La transformation complète sur une année entière",
            recommended = true
        ),
        SubscriptionOffer(
            key = "A vie;250.00",
            label = "Accès à vie : 250€",
            periodLabel = "À VIE",
            price = "250 €",
            description = "Un seul investissement pour un accès illimité et garanti à vie."
        )
    )

    /**
     * Essai gratuit de 7 jours — sa propre carte sur l'écran d'accroche premium
     * ([com.audreyRetournayDiet.femSante.features.login.PremiumUpsellActivity], ajoutée à [all] en
     * tête de liste) et une entrée du menu déroulant du tunnel de paiement
     * ([com.audreyRetournayDiet.femSante.features.login.PaymentActivity], via [asMapPrice]).
     */
    val FREE_TRIAL = SubscriptionOffer(
        key = "free;0.00",
        label = "Essai gratuit : 7 jours offerts",
        periodLabel = "ESSAI GRATUIT",
        price = "0 €",
        description = "7 jours d'accès complet"
    )

    /** Compatibilité avec le menu déroulant et le calcul de prix de `PaymentActivity`. */
    val asMapPrice: LinkedHashMap<String, String>
        get() = LinkedHashMap((all + FREE_TRIAL).associate { it.key to it.label })

    fun byKey(key: String?): SubscriptionOffer? = (all + FREE_TRIAL).find { it.key == key }
}
