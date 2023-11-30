package com.audreyRetournayDiet.femSante.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.PAYPAL_CALL_API
import com.audreyRetournayDiet.femSante.PAYPAL_CLIENT_ID
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.RETURN_URL_CARD
import com.audreyRetournayDiet.femSante.RETURN_URL_PAYPAL
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
    private lateinit var accessToken: String
    private lateinit var payPal: PayPalButton
    private lateinit var payPalCard: Button
    private lateinit var number: EditText
    private lateinit var month: EditText
    private lateinit var year: EditText
    private lateinit var codeSecurity: EditText
    private lateinit var cardLayout: ConstraintLayout
    private lateinit var paypalLayout: RelativeLayout
    private lateinit var parametersMap: HashMap<*, *>
    private lateinit var check: CheckBox
    private lateinit var switchPay: SwitchCompat
    private lateinit var buyout: TextView
    private lateinit var valueSubscription: String
    private lateinit var reductionValue: EditText
    private lateinit var reductionButton: Button
    private var reduction = 0
    private var repay = false

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(intent)
        intent = newIntent
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("SetTextI18n")
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
        switchPay = findViewById(R.id.switch1)
        databaseManager = DatabaseManager(this)
        cardLayout = findViewById(R.id.cardLayout)
        paypalLayout = findViewById(R.id.paypalLayout)
        buyout = findViewById(R.id.textViewBuyout)
        reductionValue = findViewById(R.id.editTextReduc)
        reductionButton = findViewById(R.id.buttonReduc)
        parametersMap = intent.getSerializableExtra("map", HashMap::class.java)!!

        repay= intent.getBooleanExtra("repay", false)

        //Accés aux conditions générales de vente
        cgv.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${cgv.text}.pdf")
            startActivity(intentTarget)
        }

        //Verification du code de réduction
        reductionButton.setOnClickListener {

            try {
                val params = JSONObject()
                params.put("reductionCode", reductionValue.text.toString())

                val url =
                    "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/reductionVerify.php"

                val request =
                    JsonObjectRequest(Request.Method.POST, url, params, { reponse ->
                        if (reponse.getBoolean("success")) {
                            Toast.makeText(this, "Code de réduction accepté", Toast.LENGTH_SHORT)
                                .show()
                            reduction = reponse.getInt("reduction")
                            if (registerSpinner.selectedItemId != (-1).toLong()) {
                                val searchValue = Utilitaires.cleanKey(
                                    mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }
                                        .keys.toString())

                                val searchTabValue = searchValue.split(";")
                                valueSubscription = searchTabValue[1]
                                applyReduction()
                                buyout.text = "$valueSubscription €"
                            }
                        } else {
                            Toast.makeText(this, reponse.getString("error"), Toast.LENGTH_SHORT)
                                .show()
                        }
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

        }

        //Changement du mode de paiement
        switchPay.setOnCheckedChangeListener { _, isChecked ->
            cardLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            paypalLayout.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        //Initialisation du choix de l'abonnement
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

        registerSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (registerSpinner.selectedItemId != (-1).toLong()) {
                    val search = Utilitaires.cleanKey(
                        mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }
                            .keys.toString())

                    val searchTab = search.split(";")
                    valueSubscription = searchTab[1]
                    if (reduction != 0) {
                        applyReduction()
                    }
                    buyout.text = "$valueSubscription €"
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        //Initialisation de PayPal
        val config = CoreConfig(PAYPAL_CLIENT_ID, environment = Environment.SANDBOX)
        val cardClient = CardClient(this, config)
        val payPalNativeClient = PayPalNativeCheckoutClient(this.application, config, RETURN_URL_PAYPAL)

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

        cardClient.approveOrderListener = object : ApproveOrderListener {

            override fun onApproveOrderCanceled() {
                Toast.makeText(this@PaymentActivity, "Paiement annulé", Toast.LENGTH_SHORT).show()
            }

            override fun onApproveOrderFailure(error: PayPalSDKError) {
                Toast.makeText(this@PaymentActivity, "Erreur avec le paiement", Toast.LENGTH_SHORT)
                    .show()
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

        payPal.setOnClickListener {
            if (check.isChecked) {
                try {
                    val params = JSONObject()
                    params.put("clientId", PAYPAL_CLIENT_ID)
                    params.put("price", valueSubscription)

                   val request =
                        JsonObjectRequest(Request.Method.POST, PAYPAL_CALL_API, params, { reponse ->
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
                } catch (_: UninitializedPropertyAccessException) {
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

        payPalCard.setOnClickListener {

            if (check.isChecked) {
                try {
                    val card = Card(
                        number = "${number.text}",
                        expirationMonth = "${month.text}",
                        expirationYear = "${year.text}",
                        securityCode = "${codeSecurity.text}"
                    )

                    val params = JSONObject()
                    params.put("clientId", PAYPAL_CLIENT_ID)
                    params.put("price", valueSubscription)

                    val request =
                        JsonObjectRequest(Request.Method.POST, PAYPAL_CALL_API, params, { reponse ->
                            orderId = Utilitaires.onPayPalApiResponse(
                                context = this,
                                response = reponse
                            )

                            accessToken = reponse.getString("access_token")

                            val cardRequest = CardRequest(
                                orderId = orderId,
                                card = card,
                                returnUrl = RETURN_URL_CARD,
                                sca = SCA.SCA_ALWAYS
                            )

                            cardClient.approveOrder(this, cardRequest)

                        }, { err ->
                            Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT)
                                .show()
                            Log.e("Connexion", err.localizedMessage!!)
                        })

                    Volley.newRequestQueue(this).add(request)
                } catch (_: UninitializedPropertyAccessException) {
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

    private fun applyReduction() {
        val search = Utilitaires.cleanKey(
            mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }
                .keys.toString())

        val searchTab = search.split(";")
        val year = searchTab[0]

        if (year == "365" || year == "A vie") {
            val price: Double = valueSubscription.toDouble()
            valueSubscription = "${price - ((price * reduction) / 100)}"
        }
    }

    private fun captureOrder(orderId: String?) {

        val params = JSONObject()
        params.put("orderId", orderId)
        params.put("accessToken", accessToken)

        val url =
            "https://www.audreyretournay-dieteticiennenutritionniste.fr/actions/paypalCapture.php"

        val request = JsonObjectRequest(Request.Method.POST, url, params, { response ->

            if (response.getBoolean("success")) {
                Toast.makeText(this@PaymentActivity, "Paiement réussi", Toast.LENGTH_SHORT).show()

                if(!repay) {
                var search = Utilitaires.cleanKey(
                    mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }
                        .keys.toString())
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
                )}
                else {
                    var search = Utilitaires.cleanKey(
                        mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }
                            .keys.toString())
                    val searchTab = search.split(";")
                    search = searchTab[0]

                    val parameters = JSONObject()
                    parameters.put("email", parametersMap["email"])
                    parameters.put("password", parametersMap["password"])
                    parameters.put("days", search)
                    parameters.put("update", intent.extras!!.getString("update") == "Oui")

                    Utilitaires.updateAccount(
                        databaseManager,
                        parameters,
                        this@PaymentActivity,
                        this@PaymentActivity
                    )
                }
            } else {
                Toast.makeText(this, response.getString("error"), Toast.LENGTH_SHORT).show()
            }
        }, { err ->
            Toast.makeText(this, "Erreur de connexion", Toast.LENGTH_SHORT)
                .show()
            Log.e("Connexion", err.localizedMessage!!)
        })

        Volley.newRequestQueue(this).add(request)

    }

}