package com.audreyRetournayDiet.femSante.utilitaires

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.github.barteksc.pdfviewer.PDFView

class PdfActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)
        pdfView = findViewById(R.id.pdfView)
        val pdf = intent!!.extras!!.getString("PDF")
        try {
        pdfView.fromAsset(pdf).load()}
        catch (e : NullPointerException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }

    }
}