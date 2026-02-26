package com.audreyRetournayDiet.femSante.features.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.PaymentManager
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewModels.login.PaymentViewModel
import com.paypal.android.cardpayments.Card
import com.paypal.android.paymentbuttons.PayPalButton
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.collections.get

class PaymentActivity : AppCompatActivity() {

    private lateinit var alert: LoadingAlert
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
    private lateinit var check: CheckBox
    private lateinit var switchPay: SwitchCompat
    private lateinit var buyout: TextView
    private lateinit var reductionValue: EditText
    private lateinit var reductionButton: Button

    private lateinit var paymentViewModel: PaymentViewModel // Notre "ViewModel"
    private lateinit var parametersMap: HashMap<*, *>

    private var valueSubscription: String = ""
    private var reduction: Int = 0
    private var update: Boolean = false
    private var repay: Boolean = false

    private val mapPrice = linkedMapOf(
        "30;7.00" to "1 mois : 7€",
        "180;35.00" to "6 mois : 35€",
        "365;63.00" to "1 an : 63€",
        "A vie;250.00" to "Accès à vie : 250€"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        initViews()
        setupSpinner()

        // Initialisation de notre "ViewModel" avec ses callbacks
        paymentViewModel = PaymentViewModel(
            context = this,
            userManager = userManager,
            parametersMap = parametersMap,
            repay = repay,
            update = update,
            mapPrice = mapPrice,
            registerSpinner = { registerSpinner },
            onLoading = { isLoading -> if (isLoading) alert.start() else alert.close() },
            onError = { msg -> Utilitaires.showToast(msg, this) },
            onNavigationRequired = { isRepay, data -> navigateToLogin(isRepay, data) }
        )

        setupListeners()
    }

    private fun initViews() {
        alert = LoadingAlert(this)
        userManager = UserManager(this)

        number = findViewById(R.id.numberCard)
        month = findViewById(R.id.editTextMonth)
        year = findViewById(R.id.editTextYear)
        codeSecurity = findViewById(R.id.securityCode)
        check = findViewById(R.id.checkBoxPayment)
        payPal = findViewById(R.id.buttonPayPal)
        payPalCard = findViewById(R.id.buttonCreditCard)
        registerSpinner = findViewById(R.id.spinnerPrix)
        switchPay = findViewById(R.id.switch1)
        cardLayout = findViewById(R.id.cardLayout)
        paypalLayout = findViewById(R.id.paypalLayout)
        buyout = findViewById(R.id.textViewBuyout)
        reductionValue = findViewById(R.id.editTextReduc)
        reductionButton = findViewById(R.id.buttonReduc)

        repay = intent.getBooleanExtra("repay", false)
        update = intent.getStringExtra("update") == "Oui"

        parametersMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("map", HashMap::class.java)!!
        } else {
            @Suppress("DEPRECATION") intent.getSerializableExtra("map") as HashMap<*, *>
        }
    }

    private fun setupSpinner() {
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, mapPrice.values.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        registerSpinner.adapter =
            NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_paiement, this)

        registerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 > 0) refreshPriceDisplay()
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        switchPay.setOnCheckedChangeListener { _, isChecked ->
            cardLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            paypalLayout.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        reductionButton.setOnClickListener {
            if (registerSpinner.selectedItemId == -1L) {
                Utilitaires.showToast("Veuillez sélectionner un abonnement", this)
                return@setOnClickListener
            }
            applyReductionFromApi()
        }

        payPal.setOnClickListener {
            if (validateForm()) paymentViewModel.startPayPalPayment(valueSubscription)
        }

        payPalCard.setOnClickListener {
            if (validateForm()) {
                val card = Card(
                    number.text.toString(),
                    month.text.toString(),
                    year.text.toString(),
                    codeSecurity.text.toString()
                )
                lifecycleScope.launch { paymentViewModel.startCardPayment(card, valueSubscription) }
            }
        }

        findViewById<Button>(R.id.buttonCGV).setOnClickListener {
            val intent = Intent(this, PdfActivity::class.java).apply { putExtra("PDF", "Conditions Générales de Vente.pdf") }
            startActivity(intent)
        }
    }

    private fun applyReductionFromApi() {
        alert.start()
        val params = JSONObject().put("reductionCode", reductionValue.text.toString())
        PaymentManager(this).applyReduction(params) { result ->
            alert.close()
            if (result is ApiResult.Success) {
                reduction = result.data?.optInt("reduction") ?: 0
                refreshPriceDisplay()
                Utilitaires.showToast(result.data?.optString("message") ?: "Code appliqué", this)
            } else if (result is ApiResult.Failure) {
                Utilitaires.showToast(result.message, this)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refreshPriceDisplay() {
        val selectedLabel = registerSpinner.selectedItem.toString()
        val technicalKey = mapPrice.entries.find { it.value == selectedLabel }?.key ?: return

        val (days, basePrice) = technicalKey.split(";")
        val originalPrice = basePrice.toDouble()

        val finalPrice = if (reduction > 0 && (days == "365" || days == "A vie")) {
            originalPrice * (1 - reduction / 100.0)
        } else {
            originalPrice
        }

        valueSubscription = BigDecimal(finalPrice).setScale(2, RoundingMode.HALF_EVEN).toString()
        buyout.text = "$valueSubscription €"
    }

    private fun navigateToLogin(isRepay: Boolean, data: JSONObject?) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            if (!isRepay) {
                val map = hashMapOf(
                    "login" to (parametersMap["email"] ?: ""),
                    "password" to (parametersMap["password"] ?: "")
                )
                putExtra("map", map)
                putExtra("A vie", data?.optBoolean("A vie", false))
            }
        }
        startActivity(intent)
        finish()
    }

    private fun validateForm(): Boolean {
        if (registerSpinner.selectedItemId == -1L) {
            Utilitaires.showToast("Choisissez un abonnement", this)
            return false
        }
        if (!check.isChecked) {
            Utilitaires.showToast("Veuillez accepter les CGV", this)
            return false
        }
        return true
    }
}