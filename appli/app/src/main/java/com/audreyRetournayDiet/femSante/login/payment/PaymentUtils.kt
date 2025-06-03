package com.audreyRetournayDiet.femSante.login.payment

import android.content.Intent
import android.util.Log
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.PAYPAL_CLIENT_ID
import com.audreyRetournayDiet.femSante.RETURN_URL_CARD
import com.audreyRetournayDiet.femSante.RETURN_URL_PAYPAL
import com.audreyRetournayDiet.femSante.login.LoginActivity
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.PaymentManager
import com.audreyRetournayDiet.femSante.repository.UserManager
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
                    "Opération annulé",
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
                Log.e("Connexion", error.message!!)
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
                Log.e("Connexion", error.message!!)
            }

            override fun onApproveOrderThreeDSecureDidFinish() {
            }

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
                    // Sauvegarder accessToken si besoin
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
        val params = JSONObject()
        params.put("orderId", orderId)
        params.put("accessToken", accessToken)

        PaymentManager(context).captureOrder(params) { result ->

            when (result) {
                is ApiResult.Success -> {

                    var search = Utilitaires.cleanKey(
                        mapPrice.filterValues { it == registerSpinner().selectedItem.toString() }
                            .keys.toString())
                    val searchTab = search.split(";")
                    search = searchTab[0]

                    if (!repay) {

                        val parameters = JSONObject()
                        parameters.put("email", parametersMap["email"])
                        parameters.put("password", parametersMap["password"])
                        parameters.put("answer", parametersMap["answer"])
                        parameters.put("days", search)
                        parameters.put("name", parametersMap["name"])
                        parameters.put("id", parametersMap["id"])

                        userManager.createUser(parameters) { apiResult ->

                            when (apiResult) {
                                is ApiResult.Success -> {
                                    Toast.makeText(context, apiResult.message, Toast.LENGTH_SHORT)
                                        .show()

                                    val map: HashMap<String, *> = hashMapOf(
                                        "login" to (parametersMap["email"] ?: ""),
                                        "password" to (parametersMap["password"] ?: "")
                                    )

                                    val intent = Intent(context, LoginActivity::class.java)

                                    intent.putExtra("A vie", apiResult.data!!.getBoolean("A vie"))
                                    intent.putExtra("map", map)

                                    context.startActivity(intent)
                                }

                                is ApiResult.Failure -> {
                                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                        }

                    } else {

                        val parameters = JSONObject()
                        parameters.put("email", parametersMap["email"])
                        parameters.put("password", parametersMap["password"])
                        parameters.put("days", search)
                        parameters.put("update", update)

                        userManager.updateUser(parameters) { apiResult ->

                            when (apiResult) {
                                is ApiResult.Success -> {
                                    Toast.makeText(
                                        context,
                                        apiResult.message,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    alert.close()

                                    val intent = Intent(context, LoginActivity::class.java)

                                    context.finish()
                                    context.startActivity(intent)
                                }

                                is ApiResult.Failure -> {
                                    Toast.makeText(
                                        context,
                                        apiResult.message,
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    alert.close()
                                }
                            }
                        }
                    }
                }

                is ApiResult.Failure -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

}