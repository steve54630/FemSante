package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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

class PaymentActivity : AppCompatActivity() {

    private val tag = "ACT_PAYMENT" // Tag unique pour le Logcat
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

    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var parametersMap: HashMap<*, *>

    private var valueSubscription: String = ""
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
        Log.d(tag, "onCreate: Initialisation de l'écran de paiement")

        initViews()
        setupSpinner()
        setupViewModel()
        setupListeners()
    }

    private fun initViews() {
        alert = LoadingAlert(this)
        userManager = UserManager(this)

        // Binding classique
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

        Log.i(tag, "Contexte: Repay=$repay, Update=$update")

        parametersMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("map", HashMap::class.java)!!
        } else {
            @Suppress("DEPRECATION") intent.getSerializableExtra("map") as HashMap<*, *>
        }
    }

    private fun setupViewModel() {
        paymentViewModel = PaymentViewModel(
            context = this,
            userManager = userManager,
            parametersMap = parametersMap,
            repay = repay,
            update = update,
            mapPrice = mapPrice,
            onLoading = { isLoading ->
                Log.v(tag, "Loading: $isLoading")
                if (isLoading) alert.start() else alert.close()
            },
            onError = { msg ->
                Log.e(tag, "Erreur ViewModel: $msg")
                Utilitaires.showToast(msg, this)
            },
            onPriceCalculated = { price ->
                valueSubscription = price
                buyout.text = "$price €"
                Log.d(tag, "UI mise à jour: Nouveau prix total = $price €")
            },
            onNavigationRequired = { isRepay, data ->
                Log.i(tag, "Succès transaction: Navigation vers Login (isRepay=$isRepay)")
                navigateToLogin(isRepay, data)
            }
        )
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mapPrice.values.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        registerSpinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_paiement, this)

        registerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 > 0) {
                    val label = registerSpinner.selectedItem.toString()
                    Log.v(tag, "Spinner: Sélection de l'offre '$label'")
                    paymentViewModel.updateSelection(label)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        switchPay.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) "CARTE" else "PAYPAL"
            Log.d(tag, "Mode de paiement switché vers: $mode")
            cardLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            paypalLayout.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        reductionButton.setOnClickListener {
            val code = reductionValue.text.toString().trim()
            Log.i(tag, "Action: Tentative d'application du code promo '$code'")
            if (registerSpinner.selectedItemId == -1L) {
                Log.w(tag, "Annulation: Aucun abonnement sélectionné pour la réduction")
                Utilitaires.showToast("Veuillez sélectionner un abonnement", this)
                return@setOnClickListener
            }
            applyReductionFromApi(code)
        }

        payPal.setOnClickListener {
            Log.d(tag, "Clic: Bouton PayPal")
            if (validateForm()) {
                Log.i(tag, "Lancement du flux PayPal Native pour $valueSubscription €")
                paymentViewModel.startPayPalPayment(valueSubscription)
            }
        }

        payPalCard.setOnClickListener {
            Log.d(tag, "Clic: Bouton Carte Bancaire")
            if (validateForm()) {
                Log.i(tag, "Lancement du flux Carte (SCA) pour $valueSubscription €")
                // On ne logue JAMAIS le contenu de l'objet Card pour des raisons de sécurité (PCI DSS)
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
            Log.d(tag, "Navigation: Consultation des CGV")
            startActivity(Intent(this, PdfActivity::class.java).apply {
                putExtra("PDF", "Conditions Générales de Vente.pdf")
            })
        }
    }

    private fun applyReductionFromApi(code: String) {
        if (code.isEmpty()) return
        alert.start()
        val params = JSONObject().put("reductionCode", code)

        PaymentManager(this).applyReduction(params) { result ->
            alert.close()
            when (result) {
                is ApiResult.Success -> {
                    val percent = result.data?.optInt("reduction") ?: 0
                    Log.i(tag, "Succès API Réduction: -$percent% appliqué via le code '$code'")
                    paymentViewModel.applyReduction(percent)
                    Utilitaires.showToast(result.data?.optString("message") ?: "Code appliqué", this)
                }
                is ApiResult.Failure -> {
                    Log.w(tag, "Échec API Réduction: ${result.message} pour le code '$code'")
                    Utilitaires.showToast(result.message, this)
                }
            }
        }
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
        return when {
            registerSpinner.selectedItemId == -1L -> {
                Log.w(tag, "Validation: Aucun abonnement choisi")
                Utilitaires.showToast("Choisissez un abonnement", this)
                false
            }
            !check.isChecked -> {
                Log.w(tag, "Validation: CGV non acceptées")
                Utilitaires.showToast("Veuillez accepter les CGV", this)
                false
            }
            else -> {
                Log.v(tag, "Validation: OK")
                true
            }
        }
    }
}