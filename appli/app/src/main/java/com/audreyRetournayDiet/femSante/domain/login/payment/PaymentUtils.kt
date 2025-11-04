package com.audreyRetournayDiet.femSante.domain.login.payment

import android.content.Intent
import android.util.Log
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.PAYPAL_CLIENT_ID
import com.audreyRetournayDiet.femSante.RETURN_URL_CARD
import com.audreyRetournayDiet.femSante.RETURN_URL_PAYPAL
import com.audreyRetournayDiet.femSante.domain.login.LoginActivity
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.PaymentManager
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.utilitaires.LoadingAlert
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires
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
import me.bush.translator.Language
import me.bush.translator.Translator
import org.json.JSONObject

class PaymentUtils(
    private val context: AppCompatActivity,
    private val alert: LoadingAlert,
    private val userManager: UserManager,
    private var parametersMap: HashMap<*, *>,
    private var repay: Boolean,
    private var update: Boolean,
    private var mapPrice: LinkedHashMap<String, String>,
    private var registerSpinner: () -> Spinner,
) {

    private lateinit var accessToken: String
    private var translator: Translator = Translator()
    private val config = CoreConfig(PAYPAL_CLIENT_ID, Environment.LIVE)
    private val cardClient = CardClient(context, config)
    private val payPalNativeClient =
        PayPalNativeCheckoutClient(context.application, config, RETURN_URL_PAYPAL)

    init {
        setupPayPalListener()
    }

    private fun setupPayPalListener() {

        payPalNativeClient.listener = object : PayPalNativeCheckoutListener {
            override fun onPayPalCheckoutStart() {
                alert.close()
            }

            override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) {
                validateOrder(result.orderId)
            }

            override fun onPayPalCheckoutCanceled() {
                Toast.makeText(
                    context,
                    "Opération annulée",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onPayPalCheckoutFailure(error: PayPalSDKError) {
                val errorMessage = translator.translateBlocking(
                    error.errorDescription,
                    Language.FRENCH,
                    Language.ENGLISH
                )
                Toast.makeText(
                    context,
                    errorMessage.translatedText,
                    Toast.LENGTH_LONG
                ).show()
                Log.e("Connexion", error.message ?: "")
            }

        }

        cardClient.approveOrderListener = object : ApproveOrderListener {

            override fun onApproveOrderSuccess(result: CardResult) {
                validateOrder(result.orderId)
            }

            override fun onApproveOrderCanceled() {
                alert.close()
                Toast.makeText(context, "Paiement annulé", Toast.LENGTH_SHORT).show()
            }

            override fun onApproveOrderFailure(error: PayPalSDKError) {
                alert.close()
                val errorMessage = translator.translateBlocking(
                    error.errorDescription,
                    Language.FRENCH,
                    Language.ENGLISH
                )
                Toast.makeText(
                    context,
                    errorMessage.translatedText,
                    Toast.LENGTH_LONG
                ).show()
                Log.e("Connexion", error.message ?: "")
            }

            override fun onApproveOrderThreeDSecureDidFinish() {}
            override fun onApproveOrderThreeDSecureWillLaunch() {
                alert.close()
                Toast.makeText(
                    context,
                    "Lancement de l'authentification",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    fun startPayPalPayment(price: String) {
        alert.start()
        val params = JSONObject().apply {
            put("clientId", PAYPAL_CLIENT_ID)
            put("price", price)
        }

        PaymentManager(context).payPalCall(params) { result ->

            when (result) {
                is ApiResult.Success -> {
                    val orderId = Utilitaires.onPayPalApiResponse(context, result.data)
                    accessToken = result.data!!.getString("access_token")
                    val request = PayPalNativeCheckoutRequest(orderId)
                    payPalNativeClient.startCheckout(request)
                }

                is ApiResult.Failure -> {
                    alert.close()
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun startCardPayment(card: Card, price: String) {
        alert.start()
        val params = JSONObject().apply {
            put("clientId", PAYPAL_CLIENT_ID)
            put("price", price)
        }

        PaymentManager(context).payPalCall(params) { result ->

            when (result) {
                is ApiResult.Success -> {
                    val orderId = Utilitaires.onPayPalApiResponse(context, result.data!!)
                    accessToken = result.data.getString("access_token")
                    val cardRequest = CardRequest(
                        orderId = orderId,
                        card = card,
                        returnUrl = RETURN_URL_CARD,
                        sca = SCA.SCA_ALWAYS
                    )
                    cardClient.approveOrder(context, cardRequest)
                }

                is ApiResult.Failure -> {
                    alert.close()
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateOrder(orderId: String?) {
        alert.start()

        val params = JSONObject().apply {
            put("orderId", orderId)
            put("accessToken", accessToken)
        }

        PaymentManager(context).captureOrder(params) { result ->
            if (result is ApiResult.Success) {

                context.lifecycleScope.launch {
                    val search = Utilitaires.cleanKey(
                        mapPrice.filterValues { it == registerSpinner().selectedItem.toString() }
                            .keys.toString()
                    ).split(";")[0]

                    val params =
                        JSONObject().apply {
                            put("email", parametersMap["email"])
                            put("password", parametersMap["password"])
                            put("days", search)
                            if (!repay) {
                                put("answer", parametersMap["answer"])
                                put("name", parametersMap["name"])
                                put("id", parametersMap["id"])
                            } else {
                                put("update", update)
                            }
                        }

                    val apiResult = if (!repay) {
                        userManager.createUser(params)
                    } else {
                        userManager.updateUser(params)
                    }

                    handleUserManagerResult(apiResult)
                }

            } else if (result is ApiResult.Failure) {
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun handleUserManagerResult(apiResult: ApiResult) {
        if (apiResult is ApiResult.Success) {
            Toast.makeText(context, apiResult.message, Toast.LENGTH_SHORT).show()
            val intent = Intent(context, LoginActivity::class.java)

            if (!repay) {
                val map: HashMap<String, *> = hashMapOf(
                    "login" to (parametersMap["email"] ?: ""),
                    "password" to (parametersMap["password"] ?: "")
                )
                intent.putExtra("A vie", apiResult.data!!.getBoolean("A vie"))
                intent.putExtra("map", map)
            }

            context.finish()
            context.startActivity(intent)
            alert.close()

        } else if (apiResult is ApiResult.Failure) {
            Toast.makeText(context, apiResult.message, Toast.LENGTH_SHORT).show()
            alert.close()
        }
    }
}
