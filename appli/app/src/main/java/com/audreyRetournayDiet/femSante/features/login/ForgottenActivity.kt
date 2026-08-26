package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.audreyRetournayDiet.femSante.viewmodels.login.ForgottenViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber

/**
 * Activité gérant la réinitialisation du mot de passe oublié.
 * * Le processus repose sur la vérification d'une question de sécurité
 * définie lors de l'inscription. L'utilisatrice doit fournir :
 * - Son email de compte.
 * - La réponse à sa question secrète.
 * - Le nouveau mot de passe souhaité.
 * * En cas de succès, l'utilisatrice est redirigée vers [LoginActivity].
 */
class ForgottenActivity : AppCompatActivity() {

    private lateinit var password: EditText
    private lateinit var confirm: EditText
    private lateinit var email: EditText
    private lateinit var answer: EditText
    private lateinit var changePasswordBtn: Button
    private lateinit var questionDropdown: AutoCompleteTextView
    private lateinit var alert: LoadingAlert
    private lateinit var forgottenViewModel: ForgottenViewModel

    private var selectedQuestionKey: String? = null

    /**
     * Map des questions de sécurité.
     * Doit être identique à celle utilisée dans [CreateFragment].
     */
    private val questionsMap = linkedMapOf(
        1 to "Nom de jeune fille de votre mère ?",
        2 to "Nom de votre 1er animal de compagnie ?",
        3 to "Prénom de votre ami d'enfance ?"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)
        Timber.d("onCreate : Initialisation de la récupération de mot de passe")

        initViews()
        setupQuestionDropdown()
        setupViewModel()
        setupListeners()
    }

    private fun initViews() {
        password = findViewById(R.id.Password)
        confirm = findViewById(R.id.ChangePassword)
        email = findViewById(R.id.Login)
        answer = findViewById(R.id.Answer)
        changePasswordBtn = findViewById(R.id.buttonConnect)
        questionDropdown = findViewById(R.id.dropdownQuestion)
        alert = LoadingAlert(this)
    }

    private fun setupViewModel() {
        forgottenViewModel = ForgottenViewModel(
            userManager = UserManager(applicationContext),
            onLoading = { isLoading ->
                Timber.v("État de chargement : $isLoading")
                if (isLoading) alert.start() else alert.close()
            },
            onError = { msg ->
                Timber.e("Erreur lors du changement de MDP : $msg")
                Utilitaires.showToast(msg, this)
            },
            onSuccess = { msg ->
                Timber.i("Succès : Mot de passe réinitialisé")
                Utilitaires.showToast(msg, this)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        )
    }

    /** Configure le menu déroulant pour le choix de la question de sécurité. */
    private fun setupQuestionDropdown() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            questionsMap.values.toList()
        )
        questionDropdown.setAdapter(adapter)

        questionDropdown.setOnItemClickListener { _, _, _, _ ->
            val selectedText = questionDropdown.text.toString()
            selectedQuestionKey = questionsMap.entries.find { it.value == selectedText }?.key?.toString()
            Timber.v("Question sélectionnée : ID $selectedQuestionKey")
        }
    }

    private fun setupListeners() {
        changePasswordBtn.setOnClickListener {
            Timber.d("Clic : Bouton de réinitialisation")

            if (validateFields()) {
                Timber.d("Tentative de réinitialisation avec question ID : $selectedQuestionKey")

                val params = JSONObject().apply {
                    put("email", email.text.toString().trim())
                    put("password", password.text.toString())
                    put("answer", answer.text.toString().trim())
                    put("id", selectedQuestionKey ?: "")
                }

                lifecycleScope.launch {
                    forgottenViewModel.changePassword(params)
                }
            }
        }
    }

    /**
     * Valide la cohérence des données saisies avant l'envoi au serveur.
     * @return true si le formulaire est valide.
     */
    private fun validateFields(): Boolean {
        val emailStr = email.text.toString().trim()
        val passStr = password.text.toString()
        val confirmStr = confirm.text.toString()
        val answerStr = answer.text.toString().trim()

        val error = when {
            // Vérifie que l'utilisateur a bien choisi une question (pas le placeholder)
            selectedQuestionKey == null -> "Veuillez sélectionner une question"
            emailStr.isEmpty() || !Utilitaires.isValidEmail(emailStr) -> "Format e-mail incorrect"
            passStr.isEmpty() -> "Veuillez saisir un mot de passe"
            passStr != confirmStr -> "Mots de passe non identiques"
            answerStr.isEmpty() -> "Veuillez saisir votre réponse secrète"
            else -> null
        }

        return error?.let {
            Timber.w("Validation échouée : $it")
            Utilitaires.showToast(it, this)
            false
        } ?: true
    }
}