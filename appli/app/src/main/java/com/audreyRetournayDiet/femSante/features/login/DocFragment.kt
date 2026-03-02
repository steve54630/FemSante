package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity

class DocFragment : Fragment() {

    private val tag = "FRAG_DOC_LEGAL"
    private lateinit var cgu : Button
    private lateinit var cgv : Button
    private lateinit var legal: Button
    private lateinit var confidentiality: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Log.d(tag, "onCreateView : Affichage de l'écran des documents légaux")
        val view = inflater.inflate(R.layout.fragment_doc, container, false)

        cgu = view.findViewById(R.id.buttonCGU)
        cgv = view.findViewById(R.id.buttonCGV)
        legal = view.findViewById(R.id.buttonLegalMentions)
        confidentiality = view.findViewById(R.id.buttonConfidentiality)

        setupListeners()

        return view
    }

    private fun setupListeners() {
        cgu.setOnClickListener { launchPdf(cgu.text.toString()) }
        cgv.setOnClickListener { launchPdf(cgv.text.toString()) }
        legal.setOnClickListener { launchPdf(legal.text.toString()) }
        confidentiality.setOnClickListener { launchPdf(confidentiality.text.toString()) }
    }

    private fun launchPdf(fileName: String) {
        val pdfName = "$fileName.pdf"
        Log.i(tag, "Action : Demande d'ouverture du PDF -> $pdfName")

        try {
            val intentTarget = Intent(activity, PdfActivity::class.java)
            intentTarget.putExtra("PDF", pdfName)
            startActivity(intentTarget)
        } catch (e: Exception) {
            Log.e(tag, "Erreur lors du lancement de PdfActivity pour le fichier $pdfName", e)
        }
    }
}