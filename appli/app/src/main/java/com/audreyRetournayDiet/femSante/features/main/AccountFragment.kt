package com.audreyRetournayDiet.femSante.features.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.viewModels.AccountViewModel
import com.audreyRetournayDiet.femSante.features.login.ForgottenActivity
import com.audreyRetournayDiet.femSante.features.login.PaymentActivity
import com.audreyRetournayDiet.femSante.features.login.LoginActivity
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private val tag = "FRAG_ACCOUNT"
    private lateinit var viewModel: AccountViewModel
    private lateinit var cgu: Button
    private lateinit var cgv: Button
    private lateinit var legal: Button
    private lateinit var confidentiality: Button
    private lateinit var passwordChange: Button
    private lateinit var login: TextView
    private lateinit var update: Button
    private lateinit var logout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Log.d(tag, "onCreateView: Initialisation du profil utilisateur")
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        val userStore = UserStore(requireContext())
        viewModel = AccountViewModel(userStore)

        initViews(view)
        setupObservers()
        setupStaticListeners()

        // Lancement du chargement des données
        viewModel.loadUserProfile()

        return view
    }

    private fun initViews(view: View) {
        login = view.findViewById(R.id.textViewLogin)
        cgu = view.findViewById(R.id.buttonCGU)
        cgv = view.findViewById(R.id.buttonCGV)
        legal = view.findViewById(R.id.buttonLegalMentions)
        confidentiality = view.findViewById(R.id.buttonConfidentiality)
        passwordChange = view.findViewById(R.id.buttonPasswordChanged)
        update = view.findViewById(R.id.buttonUpdateAbonnement)
        logout = view.findViewById(R.id.buttonLogout)
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is AccountViewModel.AccountState.Success -> {
                            val user = state.user
                            Log.i(tag, "Profil chargé : ${user.email} (Lifetime: ${user.lifetimeAccess})")

                            login.text = user.email

                            // Logique d'affichage du bouton de paiement
                            update.visibility = if (user.lifetimeAccess) View.INVISIBLE else View.VISIBLE

                            update.setOnClickListener {
                                Log.d(tag, "Clic: Mise à jour de l'abonnement pour ${user.email}")
                                val intentTarget = Intent(activity, PaymentActivity::class.java).apply {
                                    putExtra("map", hashMapOf("email" to user.email, "password" to user.password))
                                    putExtra("repay", true)
                                    putExtra("update", "Oui")
                                }
                                startActivity(intentTarget)
                            }
                        }
                        is AccountViewModel.AccountState.LoggedOut -> {
                            Log.i(tag, "État : Déconnecté. Redirection login.")
                            navigateToLogin()
                        }

                        else -> {
                            Log.e(tag, "Erreur dans le ViewModel")
                        }
                    }
                }
            }
        }
    }

    private fun setupStaticListeners() {
        logout.setOnClickListener {
            Log.d(tag, "Clic: Tentative de déconnexion")
            viewModel.logout()
        }

        // Navigation vers les documents PDF
        val pdfMap = mapOf(
            cgu to "Conditions Générales d'Utilisation.pdf",
            cgv to "Conditions Générales de Vente.pdf",
            legal to "Mentions Légales.pdf",
            confidentiality to "Politique de Confidentialité.pdf"
        )

        pdfMap.forEach { (button, fileName) ->
            button.setOnClickListener {
                Log.d(tag, "Ouverture PDF : $fileName")
                startActivity(Intent(activity, PdfActivity::class.java).apply {
                    putExtra("PDF", fileName)
                })
            }
        }

        passwordChange.setOnClickListener {
            Log.d(tag, "Navigation: Changement de mot de passe")
            startActivity(Intent(activity, ForgottenActivity::class.java))
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }
}