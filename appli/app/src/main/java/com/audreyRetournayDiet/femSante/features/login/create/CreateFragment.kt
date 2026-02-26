package com.audreyRetournayDiet.femSante.features.login.create

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.audreyRetournayDiet.femSante.viewModels.login.CreateViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject

class CreateFragment : Fragment() {

    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirm: EditText
    private lateinit var answer: EditText
    private lateinit var questionSpinner: Spinner
    private lateinit var alert: LoadingAlert
    private lateinit var createViewModel: CreateViewModel

    private var chooseQuestion: String = ""

    private val mapQuestion = linkedMapOf(
        1 to "Nom de jeune fille de votre mère",
        2 to "Nom de votre 1er animal de compagnie",
        3 to "Prénom de votre meilleur(e) ami(e) d'enfance"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_register, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupSpinner()
        setupViewModel()
        setupListeners(view)
    }

    private fun initViews(view: View) {
        name = view.findViewById(R.id.Name)
        email = view.findViewById(R.id.Login)
        password = view.findViewById(R.id.Password)
        confirm = view.findViewById(R.id.ChangePassword)
        answer = view.findViewById(R.id.Answer)
        questionSpinner = view.findViewById(R.id.spinnerQuestion)
        alert = LoadingAlert(requireActivity())
    }

    private fun setupViewModel() {
        createViewModel = CreateViewModel(
            userManager = UserManager(requireContext()),
            onLoading = { isLoading -> if (isLoading) alert.start() else alert.close() },
            onError = { msg -> Utilitaires.showToast(msg, requireContext()) },
            onSuccess = { cls, extras -> navigateTo(cls, extras) }
        )
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, mapQuestion.values.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        questionSpinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_question, requireContext())

        questionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 > 0) {
                    val selectedText = questionSpinner.selectedItem.toString()
                    chooseQuestion = mapQuestion.entries.find { it.value == selectedText }?.key?.toString() ?: ""
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.buttonConnect).setOnClickListener {
            if (validateFields()) {
                val map = buildUserMap()
                lifecycleScope.launch { createViewModel.subscribe(JSONObject(map as Map<*, *>), map) }
            }
        }

        view.findViewById<Button>(R.id.buttonTestSubscribe).setOnClickListener {
            if (validateFields()) {
                val map = buildUserMap()
                lifecycleScope.launch { createViewModel.test(JSONObject(map as Map<*, *>)) }
            }
        }
    }

    private fun navigateTo(cls: Class<*>, extras: HashMap<String, String>?) {
        val intent = Intent(requireContext(), cls).apply {
            extras?.let { putExtra("map", it) }
        }
        startActivity(intent)
    }

    private fun buildUserMap() = hashMapOf(
        "email" to email.text.toString(),
        "password" to password.text.toString(),
        "name" to name.text.toString(),
        "answer" to answer.text.toString(),
        "id" to chooseQuestion
    )

    private fun validateFields(): Boolean {
        if (chooseQuestion.isEmpty()) {
            Utilitaires.showToast("Aucune question sélectionnée", requireContext())
            return false
        }
        val errorMsg = when {
            name.text.isBlank() -> "Prénom non renseigné"
            email.text.isBlank() -> "Email non renseigné"
            !Utilitaires.isValidEmail(email.text.toString()) -> "Format email incorrect"
            password.text.isBlank() -> "Mot de passe non renseigné"
            !Utilitaires.isValidPassword(password.text.toString()) -> "Format mot de passe non respecté"
            password.text.toString() != confirm.text.toString() -> "Mots de passe non identiques"
            answer.text.isBlank() -> "Réponse non renseignée"
            else -> null
        }
        return errorMsg?.let { Utilitaires.showToast(it, requireContext()); false } ?: true
    }
}