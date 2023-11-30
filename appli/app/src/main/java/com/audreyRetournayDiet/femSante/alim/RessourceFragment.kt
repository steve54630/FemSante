package com.audreyRetournayDiet.femSante.alim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.PdfActivity

class RessourceFragment : Fragment() {

    private lateinit var histamine: Button
    private lateinit var ebook: Button
    private lateinit var gluten: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_ressource, container, false)

        histamine = view.findViewById(R.id.buttonHistamine)
        gluten = view.findViewById(R.id.buttonGluten)
        ebook = view.findViewById(R.id.buttonEBook)


        histamine.setOnClickListener {
            val intentTarget = Intent(activity, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${histamine.text}.pdf")
            startActivity(intentTarget)
        }

        ebook.setOnClickListener {
            val intentTarget = Intent(activity, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${ebook.text}.pdf")
            startActivity(intentTarget)
        }

        gluten.setOnClickListener {
            val intentTarget = Intent(activity, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${gluten.text}.pdf")
            startActivity(intentTarget)
        }

        return view
    }

}