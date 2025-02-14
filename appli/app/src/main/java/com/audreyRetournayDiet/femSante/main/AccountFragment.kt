package com.audreyRetournayDiet.femSante.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.login.ForgottenActivity
import com.audreyRetournayDiet.femSante.login.PaymentActivity
import com.audreyRetournayDiet.femSante.utilitaires.PdfActivity

class AccountFragment : Fragment() {

    private lateinit var cgu: Button
    private lateinit var cgv: Button
    private lateinit var legal: Button
    private lateinit var confidentiality: Button
    private lateinit var passwordChange: Button
    private lateinit var login: TextView
    private lateinit var update: Button
    private lateinit var map: HashMap<*, *>

    @SuppressLint("NewApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val view = inflater.inflate(R.layout.fragment_account, container, false)

        map = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                    requireActivity().intent.getSerializableExtra("map", HashMap::class.java)!!
            else -> @Suppress("DEPRECATION") requireActivity().intent.getSerializableExtra("map")
                    as HashMap<*, *>
        }

        login = view.findViewById(R.id.textViewLogin)
        cgu = view.findViewById(R.id.buttonCGU)
        cgv = view.findViewById(R.id.buttonCGV)
        legal = view.findViewById(R.id.buttonLegalMentions)
        confidentiality = view.findViewById(R.id.buttonConfidentiality)
        passwordChange = view.findViewById(R.id.buttonPasswordChanged)
        update = view.findViewById(R.id.buttonUpdateAbonnement)

        login.text = map["login"].toString()

        cgu.setOnClickListener {
            val intentTarget = Intent(activity, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${cgu.text}.pdf")
            startActivity(intentTarget)
        }

        cgv.setOnClickListener {
            val intentTarget = Intent(activity, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${cgv.text}.pdf")
            startActivity(intentTarget)
        }

        legal.setOnClickListener {
            val intentTarget = Intent(activity, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${legal.text}.pdf")
            startActivity(intentTarget)
        }

        confidentiality.setOnClickListener {
            val intentTarget = Intent(activity, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${confidentiality.text}.pdf")
            startActivity(intentTarget)
        }

        passwordChange.setOnClickListener {
            startActivity(Intent(activity, ForgottenActivity::class.java))
        }

        when (requireActivity().intent.extras!!.getBoolean("A vie")) {
            true -> update.visibility = View.GONE
            false -> update.visibility = View.VISIBLE
        }

        update.setOnClickListener {
            val intentTarget = Intent(activity, PaymentActivity::class.java)

            val parameters = HashMap<String, String>()
            parameters["email"] = map["login"].toString()
            parameters["password"] = map["password"].toString()

            intentTarget.putExtra("map", parameters)
            intentTarget.putExtra("repay", true)
            intentTarget.putExtra("update", "Oui")

            startActivity(intentTarget)

        }

        return view
    }

}