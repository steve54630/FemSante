package com.audreyRetournayDiet.femSante.viewModels.login

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.PAYPAL_CLIENT_ID
import com.audreyRetournayDiet.femSante.RETURN_URL_CARD
import com.audreyRetournayDiet.femSante.RETURN_URL_PAYPAL
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.PaymentManager
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.paypal.android.cardpayments.*
import com.paypal.android.cardpayments.threedsecure.SCA
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalnativepayments.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * ViewModel orchestrant le tunnel d'achat sécurisé.
 * * ### Architecture :
 * - **Calcul Métier** : Gère les règles de prix et promotions.
 * - **SDK Intégration** : Pilote les flux Native PayPal et Cartes Bancaires.
 * - **Callback System** : Communique l'état (Loading, Error, Navigation) à l'UI sans couplage fort.
 */
class PaymentViewModel(
    private val context: AppCompatActivity,
    private val userManager: UserManager,
    private val parametersMap: HashMap<*, *>,
    private val repay: Boolean,
    private val update: Boolean,
    private val mapPrice: LinkedHashMap<String, String>,
    private val onLoading: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val onPriceCalculated: (String) -> Unit,
    private val onNavigationRequired: (Boolean, JSONObject?) -> Unit
) {
    private lateinit var accessToken: String

    private var currentReduction: Int = 0
    private var currentSelectedKey: String? = null

    // Configuration du SDK Core (Environnement LIVE pour la mise en production)
    private val config = CoreConfig(PAYPAL_CLIENT_ID, Environment.LIVE)

    // Clients spécifiques pour les deux modes de paiement
    private val cardClient = CardClient(context, config)
    private val payPalNativeClient = PayPalNativeCheckoutClient(context.application, config, RETURN_URL_PAYPAL)

    init {
        setupPayPalListeners()
    }

    // --- LOGIQUE MÉTIER DES PRIX ---

    /**
     * Met à jour la sélection de l'utilisatrice depuis le Spinner/UI.
     */
    fun updateSelection(selectedLabel: String) {
        currentSelectedKey = mapPrice.entries.find { it.value == selectedLabel }?.key
        calculateFinalPrice()
    }

    /**
     * Applique un code promo ou une réduction automatique.
     */
    fun applyReduction(reductionPercent: Int) {
        currentReduction = reductionPercent
        calculateFinalPrice()
    }

    /**
     * Calcule le prix final.
     * Règle métier : la réduction ne s'applique que sur les forfaits 1 an (365) ou A vie.
     */
    private fun calculateFinalPrice() {
        val key = currentSelectedKey ?: return
        val split = key.split(";")
        val days = split[0]
        val basePrice = split[1].toDouble()

        val finalPrice = if (currentReduction > 0 && (days == "365" || days == "A vie")) {
            basePrice * (1 - currentReduction / 100.0)
        } else {
            basePrice
        }

        // Formatage monétaire strict (2 décimales, arrondi bancaire)
        val formattedPrice = BigDecimal(finalPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
        Timber.d("Prix calculé : $formattedPrice € (Réduc: $currentReduction%)")
        onPriceCalculated(formattedPrice)
    }

    // --- LOGIQUE DE PAIEMENT (FLUX SDK) ---

    private fun setupPayPalListeners() {
        // Listener pour le bouton "PayPal" (Application Native)
        payPalNativeClient.listener = object : PayPalNativeCheckoutListener {
            override fun onPayPalCheckoutStart() {
                onLoading(false)
                Timber.d("PayPal Native: Démarrage du flux")
            }
            override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) {
                validateOrder(result.orderId)
            }
            override fun onPayPalCheckoutCanceled() {
                onError("Opération annulée par l'utilisatrice")
            }
            override fun onPayPalCheckoutFailure(error: PayPalSDKError) {
                handleError(error)
            }
        }

        // Listener pour le formulaire de Carte Bancaire
        cardClient.approveOrderListener = object : ApproveOrderListener {
            override fun onApproveOrderSuccess(result: CardResult) {
                validateOrder(result.orderId)
            }
            override fun onApproveOrderCanceled() {
                onLoading(false)
                onError("Paiement par carte annulé")
            }
            override fun onApproveOrderFailure(error: PayPalSDKError) {
                handleError(error)
            }
            override fun onApproveOrderThreeDSecureDidFinish() {}
            override fun onApproveOrderThreeDSecureWillLaunch() { onLoading(false) }
        }
    }

    /** Lancement du paiement via l'App PayPal */
    fun startPayPalPayment(price: String) {
        initiatePayment(price) { orderId ->
            payPalNativeClient.startCheckout(PayPalNativeCheckoutRequest(orderId))
        }
    }

    /** Lancement du paiement par saisie de Carte Bancaire */
    fun startCardPayment(card: Card, price: String) {
        initiatePayment(price) { orderId ->
            val request = CardRequest(orderId, card, RETURN_URL_CARD, SCA.SCA_ALWAYS)
            cardClient.approveOrder(context, request)
        }
    }

    /** Étape 1 : Création de l'ordre côté serveur */
    private fun initiatePayment(price: String, onOrderIdReady: (String) -> Unit) {
        onLoading(true)
        val params = JSONObject().put("clientId", PAYPAL_CLIENT_ID).put("price", price)

        PaymentManager(context).payPalCall(params) { result ->
            when (result) {
                is ApiResult.Success -> {
                    val orderId = Utilitaires.onPayPalApiResponse(context, result.data)
                    accessToken = result.data?.optString("access_token") ?: ""
                    onOrderIdReady(orderId)
                }
                is ApiResult.Failure -> {
                    onLoading(false)
                    onError(result.message)
                }
            }
        }
    }

    /** Étape 2 : Capture finale de l'argent après approbation utilisateur */
    private fun validateOrder(orderId: String?) {
        onLoading(true)
        val params = JSONObject().put("orderId", orderId).put("accessToken", accessToken)

        PaymentManager(context).captureOrder(params) { result ->
            if (result is ApiResult.Success) {
                finalizeUserRegistration()
            } else if (result is ApiResult.Failure) {
                onLoading(false)
                onError("Paiement non capturé par le serveur.")
            }
        }
    }

    /** Étape 3 : Validation du compte utilisatrice en BDD après succès financier */
    private fun finalizeUserRegistration() {
        context.lifecycleScope.launch {
            val days = currentSelectedKey?.split(";")?.get(0) ?: "30"

            val userParams = JSONObject().apply {
                put("email", parametersMap["email"])
                put("password", parametersMap["password"])
                put("days", days)
                if (!repay) {
                    put("answer", parametersMap["answer"])
                    put("name", parametersMap["name"])
                    put("id", parametersMap["id"])
                } else {
                    put("update", update)
                }
            }

            val result = if (repay) userManager.updateUser(userParams) else userManager.createUser(userParams)

            onLoading(false)
            when (result) {
                is ApiResult.Success -> onNavigationRequired(repay, result.data)
                is ApiResult.Failure -> onError("Erreur profil : ${result.message}")
            }
        }
    }

    private fun handleError(error: PayPalSDKError) {
        onLoading(false)
        Timber.e("Erreur SDK PayPal : ${error.errorDescription}")
        onError("Erreur technique PayPal : ${error.errorDescription}")
    }
}