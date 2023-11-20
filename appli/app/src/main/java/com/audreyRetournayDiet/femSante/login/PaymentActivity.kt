package com.audreyRetournayDiet.femSante.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.PAYPAL_CLIENT_ID
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.DatabaseManager
import com.audreyRetournayDiet.femSante.utilitaires.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.utilitaires.PdfActivity
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
import com.paypal.android.paymentbuttons.PayPalButton
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutClient
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutListener
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutRequest
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutResult
import org.json.JSONObject

class PaymentActivity : AppCompatActivity() {

    private lateinit var orderId: String
    private lateinit var cgv: Button
    private val mapPrice = LinkedHashMap<String, String>()
    private lateinit var registerSpinner: Spinner
    private lateinit var databaseManager: DatabaseManager
    private lateinit var accessToken : String
    private lateinit var payPal: PayPalButton
    private lateinit var payPalCard: Button
    private lateinit var number: TextView
    private lateinit var month: TextView
    private lateinit var year: TextView
    private lateinit var codeSecurity: TextView
    private lateinit var parametersMap: HashMap<*, *>
    private lateinit var check: CheckBox

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(intent)
        intent = newIntent
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        number = findViewById(R.id.numberCard)
        month = findViewById(R.id.editTextMonth)
        year = findViewById(R.id.editTextYear)
        codeSecurity = findViewById(R.id.securityCode)
        check = findViewById(R.id.checkBoxPayment)
        cgv = findViewById(R.id.buttonCGV)
        payPal = findViewById(R.id.buttonPayPal)
        payPalCard = findViewById(R.id.buttonCreditCard)
        registerSpinner = findViewById(R.id.spinnerPrix)

        cgv.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${cgv.text}.pdf")
            startActivity(intentTarget)
        }

        val config = CoreConfig(PAYPAL_CLIENT_ID, environment = Environment.SANDBOX)
        val cardClient = CardClient(this, config)
        val returnUrl = "com.audreyretournaydiet.femsante://paypalpay"
        val payPalNativeClient = PayPalNativeCheckoutClient(this.application, config, returnUrl)

        cardClient.approveOrderListener = object : ApproveOrderListener {

            override fun onApproveOrderCanceled() {
                Toast.makeText(this@PaymentActivity, "Paiement annulé", Toast.LENGTH_SHORT).show()
            }

            override fun onApproveOrderFailure(error: PayPalSDKError) {
                Toast.makeText(this@PaymentActivity, "Erreur avec le paiement", Toast.LENGTH_SHORT)
                    .show()
                Log.e("PayPal", error.localizedMessage!!)
            }

            override fun onApproveOrderSuccess(result: CardResult) {
                captureOrder(result.orderId)
            }

            override fun onApproveOrderThreeDSecureDidFinish() {
                Toast.makeText(this@PaymentActivity, "Authentification réussi", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onApproveOrderThreeDSecureWillLaunch() {
                Toast.makeText(
                    this@PaymentActivity,
                    "Lancement de l'authentification",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }

        parametersMap = intent.getSerializableExtra("map", HashMap::class.java)!!

        // écouteur pour les paiements paypal
        payPalNativeClient.listener = object : PayPalNativeCheckoutListener {
            override fun onPayPalCheckoutCanceled() {
                Toast.makeText(
                    this@PaymentActivity,
                    "Paiement annulé par l'utilisateur",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onPayPalCheckoutFailure(error: PayPalSDKError) {
                Toast.makeText(this@PaymentActivity, "Erreur avec le paiement", Toast.LENGTH_SHORT)
                    .show()
                Log.e("Connexion", error.message!!)
            }

            override fun onPayPalCheckoutStart() {
                Toast.makeText(this@PaymentActivity, "Connexion à PayPal", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) {
                captureOrder(result.orderId)
            }

        }

        payPal.setOnClickListener {
            if (check.isChecked) {
                try {
                    var search = Utilitaires.cleanKey(
                        mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }
                            .keys.toString())

                    val searchTab = search.split(";")
                    search = searchTab[1]


                    val params = JSONObject()
                    params.put("clientId", PAYPAL_CLIENT_ID)
                    params.put("price", search)

                    val url =
                        "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/paypal.php"

                    val request =
                        JsonObjectRequest(Request.Method.POST, url, params, { reponse ->
                            orderId = Utilitaires.onPayPalApiResponse(
                                context = this,
                                response = reponse
                            )

                            accessToken = reponse.getString("access_token")

                            val payPalCheckoutRequest = PayPalNativeCheckoutRequest(
                                orderId
                            )

                            payPalNativeClient.startCheckout(payPalCheckoutRequest)

                        }, { err ->
                            Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT)
                                .show()
                            Log.e("Connexion", err.localizedMessage!!)
                        })

                    Volley.newRequestQueue(this).add(request)
                } catch (_: NullPointerException) {
                    Toast.makeText(
                        this,
                        "Veuillez sélectionnez un abonnement",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Veuillez accepter les conditions générales de vente.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        mapPrice["30;7.00"] = "1 mois : 7€"
        mapPrice["180;35.00"] = "6 mois : 35€"
        mapPrice["365;63.00"] = "1 an : 63€"
        mapPrice["A vie;250.00"] = "Accès à vie : 250€"

        val listPrice = ArrayList<String>()

        for (item in mapPrice) {
            listPrice.add(item.value)
        }


        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listPrice)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        registerSpinner.prompt = "Abonnements possibles"
        registerSpinner.adapter =
            NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_paiement, this)

        payPalCard.setOnClickListener {

            if (check.isChecked) {
                try {
                    val card = Card(
                        number = "${number.text}",
                        expirationMonth = "${month.text}",
                        expirationYear = "${year.text}",
                        securityCode = "${codeSecurity.text}"
                    )

                    var search = Utilitaires.cleanKey(
                        mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }
                            .keys.toString())

                    val searchTab = search.split(";")
                    search = searchTab[1]

                    val params = JSONObject()
                    params.put("clientId", PAYPAL_CLIENT_ID)
                    params.put("price", search)

                    val url =
                        "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/paypal.php"

                    val request =
                        JsonObjectRequest(Request.Method.POST, url, params, { reponse ->
                            orderId = Utilitaires.onPayPalApiResponse(
                                context = this,
                                response = reponse
                            )

                            accessToken = reponse.getString("access_token")

                            val cardRequest = CardRequest(
                                orderId = orderId,
                                card = card,
                                returnUrl = "com.audreyretournaydiet.femsante.cardpaiement://return_url",
                                sca = SCA.SCA_ALWAYS
                            )

                            Toast.makeText(this, cardRequest.returnUrl, Toast.LENGTH_SHORT).show()

                            cardClient.approveOrder(this, cardRequest)

                        }, { err ->
                            Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT)
                                .show()
                            Log.e("Connexion", err.localizedMessage!!)
                        })

                    Volley.newRequestQueue(this).add(request)
                } catch (_: NullPointerException) {
                    Toast.makeText(
                        this,
                        "Veuillez sélectionnez un abonnement",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Veuillez accepter les conditions générales de vente.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }

    private fun captureOrder(orderId: String?) {

        val params = JSONObject()
        params.put("orderId", orderId)
        params.put("accessToken", accessToken)

        val url =
            "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/paypalCapture.php"

        val request = JsonObjectRequest(Request.Method.POST, url, params, { response ->

           if (response.getBoolean("status")) {
                Toast.makeText(this@PaymentActivity, "Paiement réussi", Toast.LENGTH_SHORT).show()

                var search =
                    mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }
                        .keys.toString()
                val searchTab = search.split(";")
                search = searchTab[0]

                val parameters = JSONObject()
                parameters.put("email", parametersMap["email"])
                parameters.put("password", parametersMap["password"])
                parameters.put("answer", parametersMap["answer"])
                parameters.put("days", search)
                parameters.put("name", parametersMap["name"])
                parameters.put("id", parametersMap["id"])

                Utilitaires.registerCreation(
                    databaseManager,
                    parameters,
                    this@PaymentActivity,
                    this@PaymentActivity
                )
            } else {
                Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show()
            }
        }
        , { err ->
            Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT)
                .show()
            Log.e("Connexion", err.localizedMessage!!)
        })

        Volley.newRequestQueue(this).add(request)

    }

}