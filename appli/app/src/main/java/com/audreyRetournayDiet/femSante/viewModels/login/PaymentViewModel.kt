package com.audreyRetournayDiet.femSante.viewModels.login

import android.util.Log
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
import java.math.BigDecimal
import java.math.RoundingMode

class PaymentViewModel(
    private val context: AppCompatActivity,
    private val userManager: UserManager,
    private val parametersMap: HashMap<*, *>,
    private val repay: Boolean,
    private val update: Boolean,
    private val mapPrice: LinkedHashMap<String, String>,
    // On retire le Spinner d'ici ! La vue passera l'info via une fonction.
    private val onLoading: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val onPriceCalculated: (String) -> Unit, // Callback pour mettre à jour le TextView
    private val onNavigationRequired: (Boolean, JSONObject?) -> Unit
) {
    private val tag = "VM_PAYMENT"
    private lateinit var accessToken: String

    private var currentReduction: Int = 0
    private var currentSelectedKey: String? = null

    private val config = CoreConfig(PAYPAL_CLIENT_ID, Environment.LIVE)
    private val cardClient = CardClient(context, config)
    private val payPalNativeClient = PayPalNativeCheckoutClient(context.application, config, RETURN_URL_PAYPAL)

    init {
        setupPayPalListeners()
    }

    // --- LOGIQUE MÉTIER DES PRIX (Anciennement dans l'Activity) ---

    fun updateSelection(selectedLabel: String) {
        currentSelectedKey = mapPrice.entries.find { it.value == selectedLabel }?.key
        calculateFinalPrice()
    }

    fun applyReduction(reductionPercent: Int) {
        currentReduction = reductionPercent
        calculateFinalPrice()
    }

    private fun calculateFinalPrice() {
        val key = currentSelectedKey ?: return
        val split = key.split(";")
        val days = split[0]
        val basePrice = split[1].toDouble()

        // Règle métier : réduction seulement sur 1 an ou A vie
        val finalPrice = if (currentReduction > 0 && (days == "365" || days == "A vie")) {
            basePrice * (1 - currentReduction / 100.0)
        } else {
            basePrice
        }

        val formattedPrice = BigDecimal(finalPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
        Log.d(tag, "Prix calculé : $formattedPrice € (Base: $basePrice, Reduc: $currentReduction%)")
        onPriceCalculated(formattedPrice)
    }

    // --- LOGIQUE DE PAIEMENT ---

    private fun setupPayPalListeners() {
        payPalNativeClient.listener = object : PayPalNativeCheckoutListener {
            override fun onPayPalCheckoutStart() {
                onLoading(false)
                Log.d(tag, "PayPal Native: Start")
            }
            override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) {
                validateOrder(result.orderId)
            }
            override fun onPayPalCheckoutCanceled() {
                onError("Opération annulée")
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
                onError("Paiement annulé")
            }
            override fun onApproveOrderFailure(error: PayPalSDKError) {
                handleError(error)
            }
            override fun onApproveOrderThreeDSecureDidFinish() {}
            override fun onApproveOrderThreeDSecureWillLaunch() { onLoading(false) }
        }
    }

    fun startPayPalPayment(price: String) {
        initiatePayment(price) { orderId ->
            payPalNativeClient.startCheckout(PayPalNativeCheckoutRequest(orderId))
        }
    }

    fun startCardPayment(card: Card, price: String) {
        initiatePayment(price) { orderId ->
            val request = CardRequest(orderId, card, RETURN_URL_CARD, SCA.SCA_ALWAYS)
            cardClient.approveOrder(context, request)
        }
    }

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

    private fun validateOrder(orderId: String?) {
        onLoading(true)
        val params = JSONObject().put("orderId", orderId).put("accessToken", accessToken)

        PaymentManager(context).captureOrder(params) { result ->
            if (result is ApiResult.Success) {
                finalizeUserRegistration()
            } else if (result is ApiResult.Failure) {
                onLoading(false)
                onError("Échec de la validation finale")
            }
        }
    }

    private fun finalizeUserRegistration() {
        context.lifecycleScope.launch {
            // On récupère les jours depuis la clé sélectionnée stockée dans le VM
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
                is ApiResult.Failure -> onError("Paiement validé mais erreur profil : ${result.message}")
            }
        }
    }

    private fun handleError(error: PayPalSDKError) {
        onLoading(false)
        Log.e(tag, "SDK Error: ${error.errorDescription}")
        onError("Erreur: ${error.errorDescription}")
    }
}