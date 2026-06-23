package com.audreyRetournayDiet.femSante.features.login

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
import timber.log.Timber

/**
 * Activité gérant le tunnel de paiement de l'application.
 *
 * Cette interface permet à l'utilisatrice de souscrire à un abonnement (1, 6, 12 mois ou accès à vie).
 * Elle supporte deux modes de paiement principaux :
 * 1. **PayPal** : Via le bouton SDK natif.
 * 2. **Carte Bancaire** : Saisie sécurisée des informations de carte.
 *
 * ### Fonctionnalités clés :
 * - Calcul dynamique des prix selon l'abonnement choisi.
 * - Application de codes de réduction via l'API.
 * - Gestion des contextes de paiement : premier achat, mise à jour (`update`) ou régularisation (`repay`).
 */
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

    private lateinit var paymentViewModel: PaymentViewModel
    private lateinit var parametersMap: HashMap<*, *>

    private var valueSubscription: String = ""
    private var update: Boolean = false
    private var repay: Boolean = false

    /**
     * Map de correspondance entre les IDs techniques d'abonnement et les libellés UI.
     * Format clé : "durée_jours;prix_facial"
     */
    private val mapPrice = linkedMapOf(
        "30;7.00" to "1 mois : 7€",
        "180;35.00" to "6 mois : 35€",
        "365;63.00" to "1 an : 63€",
        "A vie;250.00" to "Accès à vie : 250€"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)
        Timber.d("onCreate: Initialisation de l'écran de paiement")

        initViews()
        setupSpinner()
        setupViewModel()
        setupListeners()
    }

    /**
     * Lie les composants XML et récupère les données transmises par l'Intent.
     * Gère la compatibilité pour la récupération de la HashMap selon la version d'Android.
     */
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

        Timber.i("Contexte de paiement: Repay=$repay, Update=$update")

        // Récupération sécurisée des paramètres d'inscription
        parametersMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("map", HashMap::class.java)!!
        } else {
            @Suppress("DEPRECATION") intent.getSerializableExtra("map") as HashMap<*, *>
        }
    }

    /**
     * Initialise le ViewModel avec les callbacks de mise à jour d'interface et de navigation.
     */
    private fun setupViewModel() {
        paymentViewModel = PaymentViewModel(
            context = this,
            userManager = userManager,
            parametersMap = parametersMap,
            repay = repay,
            update = update,
            mapPrice = mapPrice,
            onLoading = { isLoading ->
                Timber.v("Loading state: $isLoading")
                if (isLoading) alert.start() else alert.close()
            },
            onError = { msg ->
                Timber.e("Erreur de paiement détectée: $msg")
                Utilitaires.showToast(msg, this)
            },
            onPriceCalculated = { price ->
                // Mise à jour du prix final affiché (après réduction éventuelle)
                valueSubscription = price
                buyout.text = "$price €"
                Timber.d("UI: Prix mis à jour = $price €")
            },
            onNavigationRequired = { isRepay, data ->
                Timber.i("Succès transaction: Redirection utilisateur")
                navigateToLogin(isRepay, data)
            }
        )
    }

    /**
     * Configure le Spinner de sélection des offres.
     * Utilise un adaptateur spécial pour forcer l'utilisatrice à faire un choix explicite.
     */
    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mapPrice.values.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        registerSpinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_paiement, this)

        registerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 > 0) { // Index 0 réservé au placeholder
                    val label = registerSpinner.selectedItem.toString()
                    Timber.v("Offre choisie : $label")
                    paymentViewModel.updateSelection(label)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    /**
     * Définit les listeners pour le switch de mode de paiement et les validations de formulaire.
     */
    private fun setupListeners() {
        // Bascule entre l'interface PayPal et l'interface Carte Bancaire
        switchPay.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) "CARTE" else "PAYPAL"
            Timber.d("Changement de mode : $mode")
            cardLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
            paypalLayout.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        // Gestion du code promo
        reductionButton.setOnClickListener {
            val code = reductionValue.text.toString().trim()
            if (registerSpinner.selectedItemId == -1L) {
                Utilitaires.showToast("Veuillez d'abord sélectionner un abonnement", this)
                return@setOnClickListener
            }
            applyReductionFromApi(code)
        }

        // Paiement PayPal Native
        payPal.setOnClickListener {
            if (validateForm()) {
                Timber.i("Lancement PayPal : $valueSubscription €")
                paymentViewModel.startPayPalPayment(valueSubscription)
            }
        }

        // Paiement Carte Bancaire
        payPalCard.setOnClickListener {
            if (validateForm()) {
                Timber.i("Lancement Paiement Carte : $valueSubscription €")
                // Création sécurisée de l'objet Card (PCI-DSS compliant : pas de log des données)
                val card = Card(
                    number.text.toString(),
                    month.text.toString(),
                    year.text.toString(),
                    codeSecurity.text.toString()
                )
                lifecycleScope.launch { paymentViewModel.startCardPayment(card, valueSubscription) }
            }
        }

        // Consultation des CGV (obligatoire pour le paiement)
        findViewById<Button>(R.id.buttonCGV).setOnClickListener {
            startActivity(Intent(this, PdfActivity::class.java).apply {
                putExtra("PDF", "Conditions Générales de Vente.pdf")
            })
        }
    }

    /**
     * Interroge l'API pour vérifier la validité d'un code de réduction.
     * En cas de succès, demande au ViewModel de recalculer le prix final.
     */
    private fun applyReductionFromApi(code: String) {
        if (code.isEmpty()) return
        alert.start()
        val params = JSONObject().put("reductionCode", code)

        PaymentManager(this).applyReduction(params) { result ->
            alert.close()
            when (result) {
                is ApiResult.Success -> {
                    val percent = result.data?.optInt("reduction") ?: 0
                    Timber.i("Code promo valide: -$percent%")
                    paymentViewModel.applyReduction(percent)
                    Utilitaires.showToast(result.data?.optString("message") ?: "Réduction appliquée", this)
                }
                is ApiResult.Failure -> {
                    Timber.w("Code promo invalide: ${result.message}")
                    Utilitaires.showToast(result.message, this)
                }
            }
        }
    }

    /**
     * Redirige vers l'écran de connexion après un paiement réussi.
     * Transmet les identifiants pour faciliter la première connexion.
     */
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

    /**
     * Vérifie les pré-requis métier avant de déclencher une transaction.
     */
    private fun validateForm(): Boolean {
        return when {
            registerSpinner.selectedItemId == -1L -> {
                Utilitaires.showToast("Veuillez choisir une offre d'abonnement", this)
                false
            }
            !check.isChecked -> {
                Utilitaires.showToast("Vous devez accepter les conditions de vente (CGV)", this)
                false
            }
            else -> true
        }
    }
}