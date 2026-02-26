package com.audreyRetournayDiet.femSante.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.features.login.ForgottenActivity
import com.audreyRetournayDiet.femSante.features.login.PaymentActivity
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity

class AccountFragment : Fragment() {

    private lateinit var cgu: Button
    private lateinit var cgv: Button
    private lateinit var legal: Button
    private lateinit var confidentiality: Button
    private lateinit var passwordChange: Button
    private lateinit var login: TextView
    private lateinit var update: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // Récupération des vues
        login = view.findViewById(R.id.textViewLogin)
        cgu = view.findViewById(R.id.buttonCGU)
        cgv = view.findViewById(R.id.buttonCGV)
        legal = view.findViewById(R.id.buttonLegalMentions)
        confidentiality = view.findViewById(R.id.buttonConfidentiality)
        passwordChange = view.findViewById(R.id.buttonPasswordChanged)
        update = view.findViewById(R.id.buttonUpdateAbonnement)

        val userStore = UserStore(requireContext())
        login.text = userStore.getUser()?.email
        update.visibility = if (userStore.getUser()?.lifetimeAccess == false) View.VISIBLE else View.INVISIBLE

        // Observer l'utilisateur

        // Listener update (une seule fois)
        update.setOnClickListener {
            val intentTarget = Intent(activity, PaymentActivity::class.java).apply {
                putExtra(
                    "map",
                    hashMapOf("email" to userStore.getUser()?.email, "password" to userStore.getUser()?.password)
                )
                putExtra("repay", true)
                putExtra("update", "Oui")
            }
            startActivity(intentTarget)
        }

        // Listeners pour les PDF
        listOf(cgu, cgv, legal, confidentiality).forEach { button ->
            button.setOnClickListener {
                val intentTarget = Intent(activity, PdfActivity::class.java)
                intentTarget.putExtra("PDF", "${button.text}.pdf")
                startActivity(intentTarget)
            }
        }

        // Changement de mot de passe
        passwordChange.setOnClickListener {
            startActivity(Intent(activity, ForgottenActivity::class.java))
        }

        return view
    }
}
