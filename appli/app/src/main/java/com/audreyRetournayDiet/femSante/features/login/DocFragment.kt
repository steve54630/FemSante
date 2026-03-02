package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import timber.log.Timber

/**
 * Fragment gérant l'accès aux documents contractuels et légaux.
 * * Ce composant permet à l'utilisatrice de consulter :
 * - Les Conditions Générales d'Utilisation (CGU).
 * - Les Conditions Générales de Vente (CGV).
 * - Les Mentions Légales.
 * - La Politique de Confidentialité.
 * * L'ouverture des documents s'appuie sur une activité spécialisée [PdfActivity]
 * qui reçoit le nom du fichier via un Intent.
 */
class DocFragment : Fragment() {

    private lateinit var cgu : Button
    private lateinit var cgv : Button
    private lateinit var legal: Button
    private lateinit var confidentiality: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        Timber.d("onCreateView : Affichage de l'écran des documents légaux")
        val view = inflater.inflate(R.layout.fragment_doc, container, false)

        initViews(view)
        setupListeners()

        return view
    }

    /**
     * Initialise les références des boutons à partir du layout.
     */
    private fun initViews(view: View) {
        cgu = view.findViewById(R.id.buttonCGU)
        cgv = view.findViewById(R.id.buttonCGV)
        legal = view.findViewById(R.id.buttonLegalMentions)
        confidentiality = view.findViewById(R.id.buttonConfidentiality)
    }

    /**
     * Configure les écouteurs de clics.
     * Utilise le texte du bouton comme nom de fichier pour simplifier la gestion.
     */
    private fun setupListeners() {
        cgu.setOnClickListener { launchPdf(cgu.text.toString()) }
        cgv.setOnClickListener { launchPdf(cgv.text.toString()) }
        legal.setOnClickListener { launchPdf(legal.text.toString()) }
        confidentiality.setOnClickListener { launchPdf(confidentiality.text.toString()) }
    }

    /**
     * Prépare et lance l'affichage d'un document PDF.
     * * @param fileName Nom de base du fichier (récupéré depuis le texte du bouton).
     */
    private fun launchPdf(fileName: String) {
        val pdfName = "$fileName.pdf"
        Timber.i("Action : Demande d'ouverture du PDF -> $pdfName")

        try {
            val intentTarget = Intent(activity, PdfActivity::class.java).apply {
                putExtra("PDF", pdfName)
            }
            startActivity(intentTarget)
        } catch (e: Exception) {
            // Sécurité si PdfActivity n'est pas déclarée ou si le contexte est perdu
            Timber.e(e, "Erreur lors du lancement de PdfActivity pour le fichier $pdfName")
        }
    }
}