package com.audreyRetournayDiet.femSante.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.audreyRetournayDiet.femSante.EMAIL_EXIST_API
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.DatabaseManager
import com.audreyRetournayDiet.femSante.utilitaires.LoadingAlert
import com.audreyRetournayDiet.femSante.utilitaires.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires
import org.json.JSONObject


class CreateFragment : Fragment() {

    private lateinit var password: EditText
    private lateinit var confirm: EditText
    private lateinit var email: EditText
    private lateinit var answer: EditText
    private lateinit var subscribe: Button
    private lateinit var test: Button
    private lateinit var databaseManager: DatabaseManager
    private lateinit var questionSpinner: Spinner
    private lateinit var name: EditText
    private lateinit var alert: LoadingAlert
    private val mapQuestion = LinkedHashMap<Int, String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)
        name = view.findViewById(R.id.Name)
        answer = view.findViewById(R.id.textViewAnswer)
        password = view.findViewById(R.id.Password)
        confirm = view.findViewById(R.id.password)
        email = view.findViewById(R.id.Login)
        subscribe = view.findViewById(R.id.buttonConnect)
        test = view.findViewById(R.id.buttonTestSubscribe)
        databaseManager = DatabaseManager(requireContext())
        questionSpinner = view.findViewById(R.id.spinnerQuestion)
        alert = LoadingAlert(requireActivity())

        mapQuestion[1] = "Nom de jeune fille de votre mère"
        mapQuestion[2] = "Nom de votre 1er animal de compagnie"
        mapQuestion[3] = "Prénom de votre meilleur(e) ami(e) d'enfance"

        val listQuestion = ArrayList<String>()

        for (item in mapQuestion) {
            listQuestion.add(item.value)
        }

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listQuestion)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        questionSpinner.prompt = "Questions secrètes"
        questionSpinner.adapter =
            NothingSelectedSpinnerAdapter(
                adapter,
                R.layout.spinner_choice_question,
                requireContext()
            )

        subscribe.setOnClickListener {

            if (verifyChamp()) {

                alert.startAlertDialog()

                val parameters = JSONObject()
                parameters.put("email", email.text.toString())

                val request =
                    JsonObjectRequest(
                        Request.Method.POST,
                        EMAIL_EXIST_API,
                        parameters,
                        { response ->
                            alert.closeAlertDialog()
                            val success = response.getBoolean("success")
                            if (success) {
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

                                    val intent = Intent(activity, PaymentActivity::class.java)

                                    intent.putExtra("map", map)

                                    startActivity(intent)

                                } catch (e: Exception) {
                                    Toast.makeText(
                                        activity,
                                        "Aucune question sélectionnée",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            } else {
                                Toast.makeText(
                                    activity,
                                    response.getString("error"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        },
                        { error ->
                            alert.closeAlertDialog()
                            Toast.makeText(context, "Erreur de connexion", Toast.LENGTH_LONG).show()
                            Log.e("Connexion", error.localizedMessage!!)
                        })

                Volley.newRequestQueue(requireContext()).add(request)
            }
        }

        test.setOnClickListener {

            if (verifyChamp()) {

                alert.startAlertDialog()

                val parameters = JSONObject()
                parameters.put("email", email.text.toString())

                val request =
                    JsonObjectRequest(
                        Request.Method.POST,
                        EMAIL_EXIST_API,
                        parameters,
                        { response ->
                            val success = response.getBoolean("success")
                            if (success) {
                                try {
                                    val params = JSONObject()
                                    params.put("email", email.text.toString())
                                    params.put("password", password.text.toString())
                                    params.put("answer", answer.text.toString())
                                    params.put("days", "7")
                                    params.put("name", name.text.toString())
                                    params.put("id", Utilitaires.cleanKey(
                                        mapQuestion.filterValues { it == questionSpinner.selectedItem.toString() }
                                            .keys.toString()
                                    ))

                                    Utilitaires.registerCreation(
                                        databaseManager,
                                        params,
                                        requireContext(),
                                        activity as AppCompatActivity,
                                        alert
                                    )
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        activity,
                                        "Aucune question sélectionnée",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                    alert.closeAlertDialog()
                                }
                            } else {
                                Toast.makeText(
                                    activity,
                                    response.getString("error"),
                                    Toast.LENGTH_SHORT
                                ).show()
                                alert.closeAlertDialog()
                            }

                        }, { error ->
                            alert.closeAlertDialog()
                            Toast.makeText(context, "Erreur de connexion", Toast.LENGTH_LONG).show()
                            Log.e("Connexion", error.localizedMessage!!)
                        })

                Volley.newRequestQueue(requireContext()).add(request)
            }
        }
        return view
    }


    private fun verifyChamp(): Boolean {

        var success = false

        if (name.text.toString() == "") {
            Toast.makeText(activity, "Prénom non renseigné", Toast.LENGTH_SHORT).show()
        } else if (email.text.toString() == "") {
            Toast.makeText(activity, "Email non renseigné", Toast.LENGTH_SHORT).show()
        } else if (!Utilitaires.isValidEmail(email.text.toString())) {
            Toast.makeText(
                activity,
                "Format email incorrect : abc@example.fr",
                Toast.LENGTH_SHORT
            )
                .show()
        } else if (password.text.toString() == "") {
            Toast.makeText(activity, "Mot de passe non renseigné", Toast.LENGTH_SHORT)
                .show()
        } else if (!Utilitaires.isValidPassword(password.text.toString())) {
            Toast.makeText(
                activity,
                "Format de mot de passe : régle non respecté",
                Toast.LENGTH_SHORT
            )
                .show()
        } else if (password.text.toString() != confirm.text.toString()) {
            Toast.makeText(activity, "Mots de passe non identiques", Toast.LENGTH_SHORT).show()
        } else if (answer.text.toString() == "") {
            Toast.makeText(
                activity,
                "Réponse à la question secréte non renseigné",
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            success = true
        }

        return success
    }

}