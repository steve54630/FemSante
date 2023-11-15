package com.audreyRetournayDiet.femSante

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R.id
import com.audreyRetournayDiet.femSante.R.layout
import com.audreyRetournayDiet.femSante.alim.AlimActivity
import com.audreyRetournayDiet.femSante.corps.BienCorpsActivity
import com.audreyRetournayDiet.femSante.tete.BienTeteActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tete: Button
    private lateinit var corps: Button
    private lateinit var outils: Button
    private lateinit var journal : Button
    private lateinit var alim : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        alim = findViewById(id.buttonAlim)
        tete = findViewById(id.buttonTete)
        corps = findViewById(id.buttonCorps)
        outils = findViewById(id.buttonOutils)
        journal = findViewById(id.boutonJournal)

        alim.setOnClickListener {
            startActivity(Intent(this, AlimActivity::class.java))
        }

        tete.setOnClickListener {
            startActivity(Intent(this, BienTeteActivity::class.java))
        }

        corps.setOnClickListener {
            startActivity(Intent(this, BienCorpsActivity::class.java))
        }

        outils.setOnClickListener {
            startActivity(Intent(this, ToolboxActivity::class.java))
        }

        journal.setOnClickListener {
            //startActivity(Intent(this, SymptomJournal::class.java))
        }
    }
}