package com.audreyRetournayDiet.femSante.domain.login.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import kotlinx.coroutines.launch
import org.json.JSONObject


class CreateFragment : Fragment() {

    private lateinit var password: EditText
    private lateinit var confirm: EditText
    private lateinit var email: EditText
    private lateinit var answer: EditText
    private lateinit var subscribe: Button
    private lateinit var test: Button
    private lateinit var userManager: UserManager
    private lateinit var questionSpinner: Spinner
    private lateinit var name: EditText
    private lateinit var alert: LoadingAlert
    private val mapQuestion = linkedMapOf(
        1 to "Nom de jeune fille de votre mère",
        2 to "Nom de votre 1er animal de compagnie",
        3 to "Prénom de votre meilleur(e) ami(e) d'enfance"
    )
    private lateinit var chooseQuestion: String
    private lateinit var createUtils: CreateUtils

    private fun initViews(view: View) {

        name = view.findViewById(R.id.Name)
        answer = view.findViewById(R.id.Answer)
        password = view.findViewById(R.id.Password)
        confirm = view.findViewById(R.id.ChangePassword)
        email = view.findViewById(R.id.Login)
        subscribe = view.findViewById(R.id.buttonConnect)
        test = view.findViewById(R.id.buttonTestSubscribe)
        userManager = UserManager(requireContext())
        questionSpinner = view.findViewById(R.id.spinnerQuestion)
        alert = LoadingAlert(requireActivity())
        createUtils = CreateUtils(requireActivity(), userManager)

    }

    private fun setupSpinner() {

        val listQuestion = mapQuestion.values.toList()

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listQuestion)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        questionSpinner.prompt = "Questions secrètes"
        questionSpinner.adapter = NothingSelectedSpinnerAdapter(
            adapter, R.layout.spinner_choice_question, requireContext()
        )

        questionSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (questionSpinner.selectedItemId != (-1).toLong()) {
                    val search =
                        Utilitaires.cleanKey(
                            mapQuestion.filterValues
                        { it == questionSpinner.selectedItem.toString() }.keys.toString()
                        )

                    chooseQuestion = search
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        initViews(view)

        setupSpinner()

        subscribe.setOnClickListener {

            if (questionSpinner.selectedItemId == (-1).toLong()) {
                Toast.makeText(activity, "Aucune question sélectionnée", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (verifyChamp()) {

                alert.start()

                val parameters = JSONObject()
                parameters.put("email", email.text.toString())

                val map = HashMap<String, String>()
                map["email"] = email.text.toString()
                map["password"] = password.text.toString()
                map["answer"] = answer.text.toString()
                map["name"] = name.text.toString()
                map["id"] = chooseQuestion

                lifecycleScope.launch {
                    createUtils.subscribe(parameters, map)

                    alert.close()
                }

            }
        }

        test.setOnClickListener {

            if (questionSpinner.selectedItemId == (-1).toLong()) {
                Toast.makeText(activity, "Aucune question sélectionnée", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (verifyChamp()) {

                alert.start()

                val params = buildUserParams()

                lifecycleScope.launch {
                    createUtils.test(params)

                    alert.close()
                }

            }
        }
        return view
    }

    private fun buildUserParams(): JSONObject {
        return JSONObject().apply {
            put("email", email.text.toString())
            put("password", password.text.toString())
            put("answer", answer.text.toString())
            put("name", name.text.toString())
            put("id", chooseQuestion)
        }
    }

    private fun verifyChamp(): Boolean {

        val context = activity ?: return false

        return when {
            name.text.isNullOrBlank() -> Utilitaires.showToast("Prénom non renseigné", context)
            email.text.isNullOrBlank() -> Utilitaires.showToast("Email non renseigné", context)
            !Utilitaires.isValidEmail(email.text.toString()) -> Utilitaires.showToast(
                "Format email incorrect : abc@example.fr", context
            )

            password.text.isNullOrBlank() -> Utilitaires.showToast(
                "Mot de passe non renseigné", context
            )

            !Utilitaires.isValidPassword(password.text.toString()) -> Utilitaires.showToast(
                "Format de mot de passe : règle non respectée", context
            )

            password.text.toString() != confirm.text.toString() -> Utilitaires.showToast(
                "Mots de passe non identiques", context
            )

            answer.text.isNullOrBlank() -> Utilitaires.showToast(
                "Réponse à la question secrète non renseignée", context
            )

            chooseQuestion.isBlank() -> Utilitaires.showToast(
                "Veuillez sélectionner une question secrète", context
            )

            else -> true
        }
    }

}