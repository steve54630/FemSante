package com.audreyRetournayDiet.femSante.login

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.UserManager
import com.audreyRetournayDiet.femSante.utilitaires.LoadingAlert
import com.audreyRetournayDiet.femSante.utilitaires.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires
import org.json.JSONObject

class ForgottenActivity : AppCompatActivity() {

    private lateinit var password: EditText
    private lateinit var confirm: EditText
    private lateinit var email: EditText
    private lateinit var answer: EditText
    private lateinit var changePassword: Button
    private lateinit var databaseManager: UserManager
    private lateinit var questionSpinner: Spinner
    private var alert = LoadingAlert(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgotten_password)
        password = findViewById(R.id.Password)
        confirm = findViewById(R.id.ChangePassword)
        email = findViewById(R.id.Login)
        answer = findViewById(R.id.Answer)
        changePassword = findViewById(R.id.buttonConnect)
        databaseManager = UserManager(applicationContext)
        questionSpinner = findViewById(R.id.spinnerQuestion)

        val mapQuestion = LinkedHashMap<Int, String>()
        mapQuestion[1] = "Nom de jeune fille de votre mère?"
        mapQuestion[2] = "Nom de votre 1er animal de compagnie?"
        mapQuestion[3] = "Prénom de votre ami d'enfance?"

        val listQuestion = ArrayList<String>()

        for (item in mapQuestion) {
            listQuestion.add(item.value)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listQuestion)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        questionSpinner.prompt = "Questions secrètes"
        questionSpinner.adapter =
            NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_question, this)


        changePassword.setOnClickListener {
            try {
                if (questionSpinner.selectedItemId != (-1).toLong()) {
                    if (password.text.toString() == confirm.text.toString()) {
                        if (Utilitaires.isValidEmail(email.text.toString())) {

                            alert.start()

                            val intent = Intent(this, LoginFragment::class.java)
                            var search =
                                mapQuestion.filterValues { it == questionSpinner.selectedItem.toString() }
                                    .keys.toString()
                            search = search.substring(1)
                            search = search.substring(0, search.length - 1)
                            val parameters = JSONObject()
                            parameters.put("email", email.text.toString())
                            parameters.put("password", password.text.toString())
                            parameters.put("answer", answer.text.toString())
                            parameters.put("id", search)

                            databaseManager.changePassword(parameters) { result ->

                                when (result) {
                                    is ApiResult.Success -> {
                                        Toast.makeText(this, result.message, Toast.LENGTH_SHORT)
                                            .show()

                                        this.finish()
                                        this.startActivity(intent)
                                    }

                                    is ApiResult.Failure -> {
                                        alert.close()
                                        Toast.makeText(this, result.message, Toast.LENGTH_LONG)
                                            .show()
                                    }
                                }

                            }
                        } else {
                            alert.close()
                            Toast.makeText(
                                this,
                                "Erreur : Format e-mail incorrect",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    } else {
                        alert.close()
                        Toast.makeText(
                            this,
                            "Erreur : Mots de passe non identiques",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Veuillez sélectionner une question",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: UninitializedPropertyAccessException) {
                Toast.makeText(
                    this,
                    "Veuillez rensiegner tous les champs",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}