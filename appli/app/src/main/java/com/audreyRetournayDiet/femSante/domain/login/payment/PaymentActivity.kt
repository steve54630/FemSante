package com.audreyRetournayDiet.femSante.domain.login.payment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.PaymentManager
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.paypal.android.cardpayments.Card
import com.paypal.android.paymentbuttons.PayPalButton
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

class PaymentActivity : AppCompatActivity() {

    private lateinit var alert: LoadingAlert
    private lateinit var cgv: Button
    private lateinit var registerSpinner: Spinner
    private lateinit var userManager: UserManager
    private lateinit var payPal: PayPalButton
    private lateinit var payPalCard: Button
    private lateinit var number: EditText
    private lateinit var month: EditText
    private lateinit var year: EditText
    private lateinit var codeSecurity: EditText
    private lateinit var cardLayout: LinearLayout
    private lateinit var paypalLayout: FrameLayout
    private lateinit var parametersMap: HashMap<*, *>
    private lateinit var check: CheckBox
    private lateinit var switchPay: SwitchCompat
    private lateinit var buyout: TextView
    private lateinit var valueSubscription: String
    private lateinit var reductionValue: EditText
    private lateinit var reductionButton: Button
    private var update = false
    private var reduction = 0
    private var repay = false
    private lateinit var paymentUtils: PaymentUtils

    //Initialisation du choix de l'abonnement
    private val mapPrice = linkedMapOf(
        "30;7.00" to "1 mois : 7€",
        "180;35.00" to "6 mois : 35€",
        "365;63.00" to "1 an : 63€",
        "A vie;250.00" to "Accès à vie : 250€"
    )

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        intent = newIntent
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_payment)

        initValues()

        lifecycleScope.launch { paymentUtils = PaymentUtils(
            this@PaymentActivity,
            alert,
            userManager,
            parametersMap,
            repay,
            update,
            mapPrice
        ) { registerSpinner }
        }

        setupUiListeners()


        //Changement du mode de paiement
        switchPay.setOnCheckedChangeListener { _, isChecked ->
            cardLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            paypalLayout.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        val listPrice = mapPrice.values.toList()

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listPrice)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        registerSpinner.prompt = "Abonnements possibles"
        registerSpinner.adapter =
            NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_paiement, this)

    }

    private fun applyReduction(year: String) {
        if (year == "365" || year == "A vie") {
            val price: Double = valueSubscription.toDouble()
            val value: Double = price - ((price * reduction) / 100)
            valueSubscription = "${BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN)}"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUiListeners() {

        reductionButton.setOnClickListener {

            try {
                alert.start()

                val params = JSONObject()
                params.put("reductionCode", reductionValue.text.toString())

                PaymentManager(this).applyReduction(params) { result ->
                    when (result) {
                        is ApiResult.Success<JSONObject> -> {
                            Toast.makeText(
                                this,
                                result.data!!.getString("message"),
                                Toast.LENGTH_SHORT
                            ).show()

                            if (registerSpinner.selectedItemId != (-1).toLong()) {
                                val searchValue =
                                    Utilitaires.cleanKey(mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }.keys.toString())

                                val searchTabValue = searchValue.split(";")
                                valueSubscription = searchTabValue[1]
                                val yearValue = searchTabValue[0]
                                reduction = result.data.getInt("reduction")
                                applyReduction(yearValue)
                                buyout.text = "$valueSubscription €"
                            }
                            alert.close()
                        }

                        is ApiResult.Failure -> {
                            alert.close()
                            Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            } catch (_: NullPointerException) {
                Toast.makeText(
                    this, "Veuillez sélectionnez un abonnement", Toast.LENGTH_SHORT
                ).show()
            }

        }


        cgv.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${cgv.text}.pdf")
            startActivity(intentTarget)
        }

        registerSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (registerSpinner.selectedItemId != (-1).toLong()) {
                    val search =
                        Utilitaires.cleanKey(mapPrice.filterValues { it == registerSpinner.selectedItem.toString() }.keys.toString())

                    val searchTab = search.split(";")
                    valueSubscription = searchTab[1]

                    if (reduction != 0) {
                        applyReduction(searchTab[0])
                    }
                    buyout.text = "$valueSubscription €"
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        payPal.setOnClickListener {
            alert.start()

            if (registerSpinner.selectedItemId == (-1).toLong()) {
                Toast.makeText(this, "Veuillez sélectionner un abonnement", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (check.isChecked) {
                paymentUtils.startPayPalPayment(valueSubscription)
            }

        }

        payPalCard.setOnClickListener {

            alert.start()

            if (check.isChecked) {
                val card = Card(
                    number = "${number.text}",
                    expirationMonth = "${month.text}",
                    expirationYear = "${year.text}",
                    securityCode = "${codeSecurity.text}"
                )

                lifecycleScope.launch { paymentUtils.startCardPayment(card, valueSubscription) }


            }
        }
    }

    private fun initValues() {

        alert = LoadingAlert(this)
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
        userManager = UserManager(this)
        cardLayout = findViewById(R.id.cardLayout)
        paypalLayout = findViewById(R.id.paypalLayout)
        buyout = findViewById(R.id.textViewBuyout)
        reductionValue = findViewById(R.id.editTextReduc)
        reductionButton = findViewById(R.id.buttonReduc)

        repay = intent.getBooleanExtra("repay", false)

        update = (intent.getStringExtra("update") == "Oui")

        parametersMap = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->

                intent.getSerializableExtra("map", HashMap::class.java)!!

            else -> @Suppress("DEPRECATION") intent.getSerializableExtra("map") as HashMap<*, *>
        }

    }

}