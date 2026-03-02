package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
import timber.log.Timber

/**
 * Fragment gérant la création de compte utilisateur.
 * * Ce composant assure :
 * - La saisie des informations d'identification (Nom, Email, Password).
 * - La configuration d'une question de sécurité via un [Spinner].
 * - La validation locale des données (format email, force du mot de passe).
 * - La coordination avec le [CreateViewModel] pour l'appel API d'inscription.
 */
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

    /**
     * Map des questions de sécurité disponibles.
     * Les clés numériques correspondent aux IDs attendus par l'API.
     */
    private val mapQuestion = linkedMapOf(
        1 to "Nom de jeune fille de votre mère",
        2 to "Nom de votre 1er animal de compagnie",
        3 to "Prénom de votre meilleur(e) ami(e) d'enfance"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Timber.d("onCreateView : Création de la vue inscription")
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupSpinner()
        setupViewModel()
        setupListeners(view)
    }

    /**
     * Lie les composants du layout XML aux propriétés de la classe.
     */
    private fun initViews(view: View) {
        name = view.findViewById(R.id.Name)
        email = view.findViewById(R.id.Login)
        password = view.findViewById(R.id.Password)
        confirm = view.findViewById(R.id.ChangePassword)
        answer = view.findViewById(R.id.Answer)
        questionSpinner = view.findViewById(R.id.spinnerQuestion)
        alert = LoadingAlert(requireActivity())
    }

    /**
     * Initialise le ViewModel avec ses callbacks de gestion d'état.
     */
    private fun setupViewModel() {
        createViewModel = CreateViewModel(
            userManager = UserManager(requireContext()),
            onLoading = { isLoading ->
                Timber.v("Loading state: $isLoading")
                if (isLoading) alert.start() else alert.close()
            },
            onError = { msg ->
                Timber.e("Erreur Inscription : $msg")
                Utilitaires.showToast(msg, requireContext())
            },
            onSuccess = { cls, extras ->
                Timber.i("Inscription réussie ! Navigation vers ${cls.simpleName}")
                navigateTo(cls, extras)
            }
        )
    }

    /**
     * Configure le Spinner pour le choix de la question de sécurité.
     * Utilise [NothingSelectedSpinnerAdapter] pour afficher un texte d'invite par défaut.
     */
    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            mapQuestion.values.toList()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Emballage de l'adapter pour gérer le "Sélectionnez une question..."
        questionSpinner.adapter = NothingSelectedSpinnerAdapter(
            adapter,
            R.layout.spinner_choice_question,
            requireContext()
        )

        questionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // p2 > 0 car l'index 0 est réservé au layout "NothingSelected"
                if (p2 > 0) {
                    val selectedText = questionSpinner.selectedItem.toString()
                    chooseQuestion = mapQuestion.entries.find { it.value == selectedText }?.key?.toString() ?: ""
                    Timber.v("Question sélectionnée : ID $chooseQuestion")
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    /**
     * Définit les actions au clic sur les boutons du formulaire.
     */
    private fun setupListeners(view: View) {
        view.findViewById<Button>(R.id.buttonConnect).setOnClickListener {
            Timber.d("Clic : Bouton Inscription")
            if (validateFields()) {
                val map = buildUserMap()
                Timber.i("Envoi de la requête d'inscription pour : ${email.text}")
                lifecycleScope.launch { createViewModel.subscribe(JSONObject(map as Map<*, *>), map) }
            }
        }

        view.findViewById<Button>(R.id.buttonTestSubscribe).setOnClickListener {
            Timber.d("Clic : Bouton Test Validation")
            if (validateFields()) {
                val map = buildUserMap()
                lifecycleScope.launch { createViewModel.test(JSONObject(map as Map<*, *>)) }
            }
        }
    }

    /**
     * Gère la transition vers l'activité suivante après succès.
     */
    private fun navigateTo(cls: Class<*>, extras: HashMap<String, String>?) {
        val intent = Intent(requireContext(), cls).apply {
            extras?.let { putExtra("map", it) }
        }
        startActivity(intent)
    }

    /**
     * Construit la structure de données utilisateur pour l'envoi API.
     */
    private fun buildUserMap() = hashMapOf(
        "email" to email.text.toString(),
        "password" to password.text.toString(),
        "name" to name.text.toString(),
        "answer" to answer.text.toString(),
        "id" to chooseQuestion
    )

    /**
     * Effectue les vérifications de validité sur l'ensemble des champs.
     * @return true si tous les critères sont respectés, false sinon.
     */
    private fun validateFields(): Boolean {
        if (chooseQuestion.isEmpty()) {
            Timber.w("Validation échouée : Question non sélectionnée")
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

        return errorMsg?.let {
            Timber.w("Validation échouée : $it")
            Utilitaires.showToast(it, requireContext())
            false
        } ?: true
    }
}