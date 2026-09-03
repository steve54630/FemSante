package com.audreyRetournayDiet.femSante.viewmodels.login

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.PAYPAL_CLIENT_ID
import com.audreyRetournayDiet.femSante.PAYPAL_ENVIRONMENT
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
    private val onPriceCalculated: (originalPrice: String, finalPrice: String) -> Unit,
    private val onNavigationRequired: (Boolean, JSONObject?) -> Unit
) {
    private lateinit var accessToken: String

    private var currentReduction: Int = 0
    private var currentReductionCode: String? = null
    private var currentSelectedKey: String? = null

    // Configuration du SDK Core — environnement piloté par PAYPAL_ENVIRONMENT
    // (local.properties, non versionné). Défaut LIVE si absent.
    private val config = CoreConfig(
        PAYPAL_CLIENT_ID,
        if (PAYPAL_ENVIRONMENT == "SANDBOX") Environment.SANDBOX else Environment.LIVE
    )

    private val cardClient = CardClient(context, config)
    private val payPalNativeClient = PayPalNativeCheckoutClient(context.application, config, RETURN_URL_PAYPAL)

    init {
        setupPayPalListeners()
    }

    /**
     * Met à jour la sélection de l'utilisatrice depuis le Spinner/UI.
     */
    fun updateSelection(selectedLabel: String) {
        currentSelectedKey = mapPrice.entries.find { it.value == selectedLabel }?.key
        calculateFinalPrice()
    }

    /**
     * Applique un code promo ou une réduction automatique.
     * Le code lui-même est conservé (et pas seulement le pourcentage) : c'est lui qui
     * sera revalidé par le serveur à la création de la commande — jamais le pourcentage
     * calculé ici, qui n'est qu'une prévisualisation.
     */
    fun applyReduction(reductionPercent: Int, code: String) {
        currentReduction = reductionPercent
        currentReductionCode = code
        calculateFinalPrice()
    }

    /** true si un code promo valide s'applique réellement au palier actuellement choisi. */
    fun isReductionEligibleForCurrentOffer(): Boolean {
        val days = currentSelectedKey?.split(";")?.get(0) ?: return false
        return days == "365" || days == "A vie"
    }

    /**
     * Calcule le prix affiché (indicatif — le serveur recalcule et fait foi à la création
     * de la commande). Règle métier : la réduction ne s'applique que sur les forfaits
     * 1 an (365) ou A vie. Transmet le prix d'origine ET le prix final, pour que l'UI
     * puisse montrer l'écart quand une réduction s'applique.
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
        val formattedOriginal = BigDecimal(basePrice).setScale(2, RoundingMode.HALF_EVEN).toString()
        val formattedFinal = BigDecimal(finalPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
        Timber.d("Prix calculé : $formattedFinal € (Réduc: $currentReduction%)")
        onPriceCalculated(formattedOriginal, formattedFinal)
    }

    private fun setupPayPalListeners() {
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

    fun startPayPalPayment() {
        initiatePayment { orderId ->
            payPalNativeClient.startCheckout(PayPalNativeCheckoutRequest(orderId))
        }
    }

    fun startCardPayment(card: Card) {
        initiatePayment { orderId ->
            val request = CardRequest(orderId, card, RETURN_URL_CARD, SCA.SCA_ALWAYS)
            cardClient.approveOrder(context, request)
        }
    }

    /**
     * Étape 1 : Création de l'ordre côté serveur.
     * On ne transmet plus de prix : uniquement le palier choisi (et le code promo brut,
     * jamais un pourcentage) — c'est le serveur qui calcule et fait foi sur le montant.
     */
    private fun initiatePayment(onOrderIdReady: (String) -> Unit) {
        onLoading(true)
        val days = currentSelectedKey?.split(";")?.get(0)
        val params = JSONObject().apply {
            put("email", parametersMap["email"])
            put("days", days)
            currentReductionCode?.let { put("reductionCode", it) }
        }

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

    /**
     * Active l'essai gratuit de 7 jours — aucun paiement, ne passe ni par PayPal ni par la carte.
     * Pour une inscription (pas encore de compte), crée d'abord le compte en gratuit, puis
     * active l'essai ; pour un compte existant (repay), active directement.
     */
    fun activateFreeTrial() {
        context.lifecycleScope.launch {
            onLoading(true)

            if (!repay) {
                val registerParams = JSONObject().apply {
                    put("email", parametersMap["email"])
                    put("password", parametersMap["password"])
                    put("answer", parametersMap["answer"])
                    put("name", parametersMap["name"])
                    put("id", parametersMap["id"])
                }

                val registerResult = userManager.createUser(registerParams)
                if (registerResult is ApiResult.Failure) {
                    onLoading(false)
                    onError("Erreur inscription : ${registerResult.message}")
                    return@launch
                }
            }

            val trialParams = JSONObject().apply {
                put("email", parametersMap["email"])
                put("password", parametersMap["password"])
            }

            val result = userManager.activateFreeTrial(trialParams)
            onLoading(false)

            when (result) {
                is ApiResult.Success -> onNavigationRequired(repay, result.data)
                is ApiResult.Failure -> onError("Erreur activation essai : ${result.message}")
            }
        }
    }

    private fun handleError(error: PayPalSDKError) {
        onLoading(false)
        Timber.e("Erreur SDK PayPal : ${error.errorDescription}")
        onError("Erreur technique PayPal : ${error.errorDescription}")
    }
}