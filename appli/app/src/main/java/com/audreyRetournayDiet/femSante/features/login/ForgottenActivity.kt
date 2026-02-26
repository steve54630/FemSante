package com.audreyRetournayDiet.femSante.features.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import kotlinx.coroutines.launch
import org.json.JSONObject

class ForgottenActivity : AppCompatActivity() {

    private lateinit var password: EditText
    private lateinit var confirm: EditText
    private lateinit var email: EditText
    private lateinit var answer: EditText
    private lateinit var changePassword: Button
    private lateinit var questionSpinner: Spinner
    private lateinit var databaseManager: UserManager
    private lateinit var alert: LoadingAlert

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)

        initViews()
        setupSpinner()
        setupListeners(this)
    }

    private fun initViews() {
        password = findViewById(R.id.Password)
        confirm = findViewById(R.id.ChangePassword)
        email = findViewById(R.id.Login)
        answer = findViewById(R.id.Answer)
        changePassword = findViewById(R.id.buttonConnect)
        questionSpinner = findViewById(R.id.spinnerQuestion)
        databaseManager = UserManager(applicationContext)
        alert = LoadingAlert(this)
    }

    private fun setupSpinner() {
        val questions = linkedMapOf(
            1 to "Nom de jeune fille de votre mère ?",
            2 to "Nom de votre 1er animal de compagnie ?",
            3 to "Prénom de votre ami d'enfance ?"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            questions.values.toList()
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        questionSpinner.prompt = "Questions secrètes"
        questionSpinner.adapter = NothingSelectedSpinnerAdapter(
            adapter,
            R.layout.spinner_choice_question,
            this
        )

        // Stocke la map pour réutilisation dans le listener
        questionSpinner.tag = questions
    }

    private fun setupListeners(context: Context) {

        changePassword.setOnClickListener {
            lifecycleScope.launch { onChangePasswordClicked(context) }

        }
    }

    private suspend fun onChangePasswordClicked(context: Context) {
        try {
            val selectedQuestion = questionSpinner.selectedItem
            val questions = questionSpinner.tag as LinkedHashMap<*, *>

            when {
                selectedQuestion == null -> {
                    showToast(context, "Veuillez sélectionner une question")
                }

                password.text.toString() != confirm.text.toString() -> {
                    showToast(context, "Erreur : Mots de passe non identiques")
                }

                !Utilitaires.isValidEmail(email.text.toString()) -> {
                    showToast(context, "Erreur : Format e-mail incorrect")
                }

                else -> {
                    performPasswordChange(questions)
                }
            }
        } catch (_: UninitializedPropertyAccessException) {
            showToast(context, "Veuillez renseigner tous les champs")
        }
    }

    private suspend fun performPasswordChange(questions: LinkedHashMap<*, *>) {
        alert.start()

        val selectedKey = questions.filterValues {
            it == questionSpinner.selectedItem.toString()
        }.keys.firstOrNull()?.toString() ?: ""

        val parameters = JSONObject().apply {
            put("email", email.text.toString())
            put("password", password.text.toString())
            put("answer", answer.text.toString())
            put("id", selectedKey)
        }

        when (val result = databaseManager.changePassword(parameters)) {
            is ApiResult.Success<JSONObject> -> {
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginFragment::class.java))
                finish()
            }

            is ApiResult.Failure -> {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
            }
        }

        alert.close()
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

