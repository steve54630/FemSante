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
import com.audreyRetournayDiet.femSante.utilitaires.DatabaseManager
import com.audreyRetournayDiet.femSante.utilitaires.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires
import org.json.JSONObject


class CreateActivity : AppCompatActivity() {

    private lateinit var password: EditText
    private lateinit var confirm: EditText
    private lateinit var email: EditText
    private lateinit var answer: EditText
    private lateinit var subscribe: Button
    private lateinit var test: Button
    private lateinit var databaseManager: DatabaseManager
    private lateinit var questionSpinner: Spinner
    private lateinit var name: EditText
    private val mapQuestion = LinkedHashMap<Int, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_suscribe)
        name = findViewById(R.id.Name)
        answer = findViewById(R.id.textViewAnswer)
        password = findViewById(R.id.Password)
        confirm = findViewById(R.id.password)
        email = findViewById(R.id.Login)
        subscribe = findViewById(R.id.buttonConnect)
        test = findViewById(R.id.buttonTestSubscribe)
        databaseManager = DatabaseManager(applicationContext)
        questionSpinner = findViewById(R.id.spinnerQuestion)

        mapQuestion[1] = "Nom de jeune fille de votre mère"
        mapQuestion[2] = "Nom de votre 1er animal de compagnie"
        mapQuestion[3] = "Prénom de votre meilleur(e) ami(e) d'enfance"

        val listQuestion = ArrayList<String>()

        for (item in mapQuestion) {
            listQuestion.add(item.value)
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listQuestion)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        questionSpinner.prompt = "Questions secrètes"
        questionSpinner.adapter =
            NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_question, this)

        subscribe.setOnClickListener {

            if (verifyChamp()) {
                try {
                    val search = Utilitaires.cleanKey(
                        mapQuestion.filterValues { it == questionSpinner.selectedItem.toString() }
                            .keys.toString()
                    )

                    val map = HashMap<String, String>()
                    map["email"] = email.text.toString()
                    map["password"] = password.text.toString()
                    map["answer"] = answer.text.toString()
                    map["name"] = name.text.toString()
                    map["id"] = search

                    val intent = Intent(this, PaymentActivity::class.java)

                    intent.putExtra("map", map)

                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Aucune question sélectionnée", Toast.LENGTH_SHORT).show()
                }
            }


        }

        test.setOnClickListener {

            if (verifyChamp()) {
                try {
                    val parameters = JSONObject()
                    parameters.put("email", email.text.toString())
                    parameters.put("password", password.text.toString())
                    parameters.put("answer", answer.text.toString())
                    parameters.put("days", "7")
                    parameters.put("name", name.text.toString())
                    parameters.put("id", Utilitaires.cleanKey(
                        mapQuestion.filterValues { it == questionSpinner.selectedItem.toString() }
                            .keys.toString()
                    ))

                    Utilitaires.registerCreation(databaseManager, parameters, this, this)
                } catch (e: Exception) {
                    Toast.makeText(this, "Aucune question sélectionnée", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun verifyChamp(): Boolean {

        var success = false

        if (email.text.toString() == "") {
            Toast.makeText(this, "Email non renseigné", Toast.LENGTH_SHORT).show()
        } else if (password.text.toString() == "") {
            Toast.makeText(this, "Mot de passe non renseigné", Toast.LENGTH_SHORT)
                .show()
        } else if (password.text.toString() != confirm.text.toString()) {
            Toast.makeText(this, "Mots de passe non identiques", Toast.LENGTH_SHORT).show()
        } else if (answer.text.toString() == "") {
            Toast.makeText(this, "Réponse à la question secréte non renseigné", Toast.LENGTH_SHORT)
                .show()
        } else if (name.text.toString() == "") {
            Toast.makeText(this, "Prénom non renseigné", Toast.LENGTH_SHORT).show()
        } else {
            success = true
        }

        return success
    }
}