package com.audreyRetournayDiet.femSante.viewModels.login

import android.util.Log
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.PAYPAL_CLIENT_ID
import com.audreyRetournayDiet.femSante.RETURN_URL_CARD
import com.audreyRetournayDiet.femSante.RETURN_URL_PAYPAL
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.PaymentManager
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.paypal.android.cardpayments.ApproveOrderListener
import com.paypal.android.cardpayments.Card
import com.paypal.android.cardpayments.CardClient
import com.paypal.android.cardpayments.CardRequest
import com.paypal.android.cardpayments.CardResult
import com.paypal.android.cardpayments.threedsecure.SCA
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutClient
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutListener
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutRequest
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.collections.get

class PaymentViewModel(
    private val context: AppCompatActivity,
    private val userManager: UserManager,
    private val parametersMap: HashMap<*, *>,
    private val repay: Boolean,
    private val update: Boolean,
    private val mapPrice: LinkedHashMap<String, String>,
    private val registerSpinner: () -> Spinner,
    private val onLoading: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
    private val onNavigationRequired: (Boolean, JSONObject?) -> Unit
) {
    private lateinit var accessToken: String
    private val config = CoreConfig(PAYPAL_CLIENT_ID, Environment.LIVE)
    private val cardClient = CardClient(context, config)
    private val payPalNativeClient =
        PayPalNativeCheckoutClient(context.application, config, RETURN_URL_PAYPAL)

    init {
        setupPayPalListeners()
    }

    private fun setupPayPalListeners() {
        payPalNativeClient.listener = object : PayPalNativeCheckoutListener {
            override fun onPayPalCheckoutStart() { onLoading(false) }
            override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) { validateOrder(result.orderId) }
            override fun onPayPalCheckoutCanceled() { onError("Opération annulée") }
            override fun onPayPalCheckoutFailure(error: PayPalSDKError) { handleError(error) }
        }

        cardClient.approveOrderListener = object : ApproveOrderListener {
            override fun onApproveOrderSuccess(result: CardResult) { validateOrder(result.orderId) }
            override fun onApproveOrderCanceled() { onLoading(false); onError("Paiement annulé") }
            override fun onApproveOrderFailure(error: PayPalSDKError) { handleError(error) }
            override fun onApproveOrderThreeDSecureDidFinish() {}
            override fun onApproveOrderThreeDSecureWillLaunch() { onLoading(false) }
        }
    }

    fun startPayPalPayment(price: String) {
        initiatePayment(price) { orderId ->
            payPalNativeClient.startCheckout(PayPalNativeCheckoutRequest(orderId))
        }
    }

    suspend fun startCardPayment(card: Card, price: String) {
        initiatePayment(price) { orderId ->
            val request = CardRequest(orderId, card, RETURN_URL_CARD, SCA.SCA_ALWAYS)
            cardClient.approveOrder(context, request)
        }
    }

    private fun initiatePayment(price: String, onOrderIdReady: (String) -> Unit) {
        onLoading(true)
        val params = JSONObject().put("clientId", PAYPAL_CLIENT_ID).put("price", price)
        PaymentManager(context).payPalCall(params) { result ->
            if (result is ApiResult.Success) {
                val orderId = Utilitaires.onPayPalApiResponse(context, result.data)
                accessToken = result.data!!.getString("access_token")
                onOrderIdReady(orderId)
            } else if (result is ApiResult.Failure) {
                onLoading(false)
                onError(result.message)
            }
        }
    }

    private fun validateOrder(orderId: String?) {
        onLoading(true)
        val params = JSONObject().put("orderId", orderId).put("accessToken", accessToken)
        PaymentManager(context).captureOrder(params) { result ->
            if (result is ApiResult.Success) finalizeUserRegistration()
            else { onLoading(false); onError("Échec capture") }
        }
    }

    private fun finalizeUserRegistration() {
        context.lifecycleScope.launch {
            val selectedLabel = registerSpinner().selectedItem.toString()
            val days = mapPrice.entries.find { it.value == selectedLabel }?.key?.split(";")?.get(0) ?: "30"

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
                is ApiResult.Failure -> onError(result.message)
            }
        }
    }

    private fun handleError(error: PayPalSDKError) {
        onLoading(false)
        Log.e("Payment", error.errorDescription)
        onError("Erreur: ${error.errorDescription}")
    }
}