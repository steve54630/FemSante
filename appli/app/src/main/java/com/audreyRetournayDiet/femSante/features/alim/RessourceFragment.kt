package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewModels.alim.RessourceViewModel
import kotlinx.coroutines.launch

class RessourceFragment : Fragment() {

    private val tag = "FRAG_RESSOURCE"
    private val viewModel: RessourceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView : Initialisation de la vue")
        return inflater.inflate(R.layout.fragment_ressource, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonHistamine = view.findViewById<Button>(R.id.buttonHistamine)
        val buttonGluten = view.findViewById<Button>(R.id.buttonGluten)
        val buttonEBook = view.findViewById<Button>(R.id.buttonEBook)

        // Configuration des clics avec logging de l'action
        buttonHistamine.setOnClickListener {
            Log.d(tag, "Clic bouton : Histamine")
            viewModel.onRessourceClicked("histamine")
        }
        buttonGluten.setOnClickListener {
            Log.d(tag, "Clic bouton : Gluten")
            viewModel.onRessourceClicked("gluten")
        }
        buttonEBook.setOnClickListener {
            Log.d(tag, "Clic bouton : EBook")
            viewModel.onRessourceClicked("ebook")
        }

        collectNavigationEvents()
    }

    private fun collectNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Utilisation de repeatOnLifecycle pour éviter les collectes en arrière-plan
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { pdfName ->
                    Log.i(tag, "Événement de navigation reçu : Ouverture de $pdfName")

                    val intentTarget = Intent(requireActivity(), PdfActivity::class.java)
                    intentTarget.putExtra("PDF", pdfName)

                    try {
                        startActivity(intentTarget)
                    } catch (e: Exception) {
                        Log.e(tag, "Erreur lors du lancement de PdfActivity pour $pdfName", e)
                    }
                }
            }
        }
    }
}