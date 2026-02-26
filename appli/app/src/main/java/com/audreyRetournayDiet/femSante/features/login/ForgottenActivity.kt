package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.audreyRetournayDiet.femSante.viewModels.login.ForgottenViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject

class ForgottenActivity : AppCompatActivity() {

    private lateinit var password: EditText
    private lateinit var confirm: EditText
    private lateinit var email: EditText
    private lateinit var answer: EditText
    private lateinit var changePasswordBtn: Button
    private lateinit var questionSpinner: Spinner
    private lateinit var alert: LoadingAlert
    private lateinit var forgottenViewModel: ForgottenViewModel

    private val questionsMap = linkedMapOf(
        1 to "Nom de jeune fille de votre mère ?",
        2 to "Nom de votre 1er animal de compagnie ?",
        3 to "Prénom de votre ami d'enfance ?"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)

        initViews()
        setupSpinner()
        setupViewModel()
        setupListeners()
    }

    private fun initViews() {
        password = findViewById(R.id.Password)
        confirm = findViewById(R.id.ChangePassword)
        email = findViewById(R.id.Login)
        answer = findViewById(R.id.Answer)
        changePasswordBtn = findViewById(R.id.buttonConnect)
        questionSpinner = findViewById(R.id.spinnerQuestion)
        alert = LoadingAlert(this)
    }

    private fun setupViewModel() {
        forgottenViewModel = ForgottenViewModel(
            userManager = UserManager(applicationContext),
            onLoading = { isLoading -> if (isLoading) alert.start() else alert.close() },
            onError = { msg -> Utilitaires.showToast(msg, this) },
            onSuccess = { msg ->
                Utilitaires.showToast(msg, this)
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        )
    }

    private fun setupSpinner() {
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, questionsMap.values.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        questionSpinner.adapter =
            NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_question, this)
    }

    private fun setupListeners() {
        changePasswordBtn.setOnClickListener {
            if (validateFields()) {
                val selectedKey = questionsMap.entries.find {
                    it.value == questionSpinner.selectedItem?.toString()
                }?.key?.toString() ?: ""

                val params = JSONObject().apply {
                    put("email", email.text.toString().trim())
                    put("password", password.text.toString())
                    put("answer", answer.text.toString().trim())
                    put("id", selectedKey)
                }

                lifecycleScope.launch {
                    forgottenViewModel.changePassword(params)
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        val emailStr = email.text.toString().trim()
        val passStr = password.text.toString()
        val confirmStr = confirm.text.toString()
        val answerStr = answer.text.toString().trim()

        val error = when {
            questionSpinner.selectedItem == null -> "Veuillez sélectionner une question"
            emailStr.isEmpty() || !Utilitaires.isValidEmail(emailStr) -> "Format e-mail incorrect"
            passStr.isEmpty() -> "Veuillez saisir un mot de passe"
            passStr != confirmStr -> "Mots de passe non identiques"
            answerStr.isEmpty() -> "Veuillez saisir votre réponse secrète"
            else -> null
        }

        return error?.let {
            Utilitaires.showToast(it, this)
            false
        } ?: true
    }
}