package com.audreyRetournayDiet.femSante.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.R.layout
import com.audreyRetournayDiet.femSante.alim.AlimActivity
import com.audreyRetournayDiet.femSante.corps.BienCorpsActivity
import com.audreyRetournayDiet.femSante.tete.BienTeteActivity
import com.audreyRetournayDiet.femSante.utilitaires.ToolboxActivity

class MainMenuFragment : Fragment() {

    private lateinit var tete: Button
    private lateinit var corps: Button
    private lateinit var outils: Button
    private lateinit var alim : Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        val view = inflater.inflate(layout.fragment_main, container, false)
        alim = view.findViewById(R.id.buttonAlim)
        tete = view.findViewById(R.id.buttonTete)
        corps = view.findViewById(R.id.buttonCorps)
        outils = view.findViewById(R.id.buttonOutils)

        alim.setOnClickListener {
            startActivity(Intent(activity, AlimActivity::class.java))
        }

        tete.setOnClickListener {
            startActivity(Intent(activity, BienTeteActivity::class.java))
        }

        corps.setOnClickListener {
            startActivity(Intent(activity, BienCorpsActivity::class.java))
        }

        outils.setOnClickListener {
            startActivity(Intent(activity, ToolboxActivity::class.java))
        }

        return view
    }
}