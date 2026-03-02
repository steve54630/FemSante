package com.audreyRetournayDiet.femSante.features.main

import android.content.Intent
import android.os.Bundle
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
import timber.log.Timber

/**
 * Fragment gérant l'espace "Mon Compte" de l'utilisatrice.
 * * Ce composant permet de :
 * - Visualiser les informations de profil (email, statut d'accès).
 * - Gérer l'abonnement (accès au tunnel de paiement si non premium à vie).
 * - Consulter les documents légaux (CGU, CGV, etc.) via [PdfActivity].
 * - Modifier le mot de passe via [ForgottenActivity].
 * - Se déconnecter proprement de l'application.
 */
class AccountFragment : Fragment() {

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
        Timber.d("onCreateView: Initialisation du profil utilisateur")
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // Initialisation du ViewModel avec injection manuelle du UserStore
        val userStore = UserStore(requireContext())
        viewModel = AccountViewModel(userStore)

        initViews(view)
        setupObservers()
        setupStaticListeners()

        // Déclenchement du chargement initial du profil
        viewModel.loadUserProfile()

        return view
    }

    /**
     * Lie les composants du layout aux propriétés de la classe.
     */
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

    /**
     * Observe l'état du ViewModel (StateFlow) pour mettre à jour l'UI en temps réel.
     * Utilise [repeatOnLifecycle] pour garantir une collecte sécurisée liée au cycle de vie.
     */
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleAccountState(state)
                }
            }
        }
    }

    /**
     * Traite les différents états émis par le ViewModel.
     * * @param state L'état actuel du compte utilisateur.
     */
    private fun handleAccountState(state: AccountViewModel.AccountState) {
        when (state) {
            is AccountViewModel.AccountState.Success -> {
                val user = state.user
                Timber.i("Profil chargé : ${user.email} (Lifetime: ${user.lifetimeAccess})")

                login.text = user.email

                // Gestion dynamique du bouton d'abonnement :
                // Masqué si l'utilisatrice possède déjà l'accès à vie.
                update.visibility = if (user.lifetimeAccess) View.INVISIBLE else View.VISIBLE

                update.setOnClickListener {
                    Timber.d("Navigation : Tunnel de paiement (Mise à jour pour ${user.email})")
                    val intentTarget = Intent(activity, PaymentActivity::class.java).apply {
                        putExtra("map", hashMapOf("email" to user.email, "password" to user.password))
                        putExtra("repay", true)
                        putExtra("update", "Oui")
                    }
                    startActivity(intentTarget)
                }
            }
            is AccountViewModel.AccountState.LoggedOut -> {
                Timber.i("Déconnexion réussie. Redirection vers LoginActivity.")
                navigateToLogin()
            }
            else -> { Timber.e("Erreur détectée dans le profil") }
        }
    }

    /**
     * Configure les clics sur les boutons dont la logique est indépendante de l'état utilisateur.
     */
    private fun setupStaticListeners() {
        // Gestion de la déconnexion
        logout.setOnClickListener {
            Timber.d("Action : Demande de déconnexion")
            viewModel.logout()
        }

        // Mapping des boutons PDF pour éviter la redondance de code
        val pdfMap = mapOf(
            cgu to "Conditions Générales d'Utilisation.pdf",
            cgv to "Conditions Générales de Vente.pdf",
            legal to "Mentions Légales.pdf",
            confidentiality to "Politique de Confidentialité.pdf"
        )

        pdfMap.forEach { (button, fileName) ->
            button.setOnClickListener {
                Timber.d("Visualisation document : $fileName")
                startActivity(Intent(activity, PdfActivity::class.java).apply {
                    putExtra("PDF", fileName)
                })
            }
        }

        // Redirection vers l'écran de changement de mot de passe
        passwordChange.setOnClickListener {
            Timber.d("Navigation : Écran de modification du mot de passe")
            startActivity(Intent(activity, ForgottenActivity::class.java))
        }
    }

    /**
     * Redirige l'utilisatrice vers l'écran de connexion en vidant la pile d'activités.
     */
    private fun navigateToLogin() {
        val intent = Intent(activity, LoginActivity::class.java).apply {
            // Empêche l'utilisateur de revenir en arrière après déconnexion
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }
}