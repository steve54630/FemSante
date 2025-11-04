package com.audreyRetournayDiet.femSante.domain.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.PdfActivity

class DocFragment : Fragment() {

    private lateinit var cgu : Button
    private lateinit var cgv : Button
    private lateinit var legal: Button
    private lateinit var confidentiality: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_doc, container, false)

        cgu = view.findViewById(R.id.buttonCGU)
        cgv = view.findViewById(R.id.buttonCGV)
        legal = view.findViewById(R.id.buttonLegalMentions)
        confidentiality = view.findViewById(R.id.buttonConfidentiality)

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

        return view
    }

}