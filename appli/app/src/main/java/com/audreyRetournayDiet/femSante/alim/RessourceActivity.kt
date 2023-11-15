package com.audreyRetournayDiet.femSante.alim

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.PdfActivity

class RessourceActivity : AppCompatActivity() {

    private lateinit var histamine: Button
    private lateinit var ebook: Button
    private lateinit var gluten: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ressource)
        histamine = findViewById(R.id.buttonHistamine)
        gluten = findViewById(R.id.buttonGluten)
        ebook = findViewById(R.id.buttonEBook)


        histamine.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${histamine.text}.pdf")
            startActivity(intentTarget)
        }

        ebook.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${ebook.text}.pdf")
            startActivity(intentTarget)
        }

        gluten.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${gluten.text}.pdf")
            startActivity(intentTarget)
        }

    }

}