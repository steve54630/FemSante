package com.audreyRetournayDiet.femSante.utilitaires

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R.id
import com.audreyRetournayDiet.femSante.R.layout

class ToolboxActivity : AppCompatActivity() {

    private lateinit var outils1 : Button
    private lateinit var outils2 : Button
    private lateinit var outils3 : Button
    private lateinit var outils4 : Button
    private lateinit var outils5 : Button
    private lateinit var outils6 : Button
    private lateinit var outils7 : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_toolbox)

        outils1 = findViewById(id.button1)
        outils2 = findViewById(id.buttonHistamine)
        outils3 = findViewById(id.button3)
        outils4 = findViewById(id.button4)
        outils5 = findViewById(id.button5)
        outils6 = findViewById(id.button6)
        outils7 = findViewById(id.button7)

        outils1.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF","automassage_ventre.pdf" )
            startActivity(intentTarget)
        }

        outils2.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF","bouillote.pdf" )
            startActivity(intentTarget)
        }

        outils3.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF","douleurs_abdominales.pdf" )
            startActivity(intentTarget)
        }

        outils4.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF","emotional_tempest.pdf" )
            startActivity(intentTarget)
        }

        outils5.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF","emotional_tempest_oil.pdf" )
            startActivity(intentTarget)
        }

        outils6.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF","infusion_digestion.pdf" )
            startActivity(intentTarget)
        }

        outils7.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF","infusions_menstruations.pdf" )
            startActivity(intentTarget)
        }

    }
}