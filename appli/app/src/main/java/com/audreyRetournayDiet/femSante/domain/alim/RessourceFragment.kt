package com.audreyRetournayDiet.femSante.domain.alim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewModels.alim.RessourceViewModel
import kotlinx.coroutines.launch

class RessourceFragment : Fragment() {

    private val viewModel: RessourceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ressource, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonHistamine = view.findViewById<Button>(R.id.buttonHistamine)
        val buttonGluten = view.findViewById<Button>(R.id.buttonGluten)
        val buttonEBook = view.findViewById<Button>(R.id.buttonEBook)

        buttonHistamine.setOnClickListener { viewModel.onRessourceClicked("histamine") }
        buttonGluten.setOnClickListener { viewModel.onRessourceClicked("gluten") }
        buttonEBook.setOnClickListener { viewModel.onRessourceClicked("ebook") }

        collectNavigationEvents()
    }

    private fun collectNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigationEvent.collect { pdfName ->
                val intentTarget = Intent(requireActivity(), PdfActivity::class.java)
                intentTarget.putExtra("PDF", pdfName)
                startActivity(intentTarget)
            }
        }
    }
}