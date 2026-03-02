package com.audreyRetournayDiet.femSante.features.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.features.alim.AlimActivity
import com.audreyRetournayDiet.femSante.features.calendar.view.CalendarActivity
import com.audreyRetournayDiet.femSante.features.corps.BienCorpsActivity
import com.audreyRetournayDiet.femSante.features.tete.BienTeteActivity
import com.audreyRetournayDiet.femSante.features.ToolboxActivity

class MainMenuFragment : Fragment() {

    private val tag = "FRAG_MAIN_MENU"
    private lateinit var tete: Button
    private lateinit var corps: Button
    private lateinit var outils: Button
    private lateinit var alim : Button
    private lateinit var calendarActivity: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView: Affichage du menu principal")
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        initViews(view)
        setupListeners()

        return view
    }

    private fun initViews(view: View) {
        alim = view.findViewById(R.id.buttonAlim)
        tete = view.findViewById(R.id.buttonTete)
        corps = view.findViewById(R.id.buttonCorps)
        outils = view.findViewById(R.id.buttonOutils)
        calendarActivity = view.findViewById(R.id.buttonCalendar)
    }

    private fun setupListeners() {
        // Alimentation
        alim.setOnClickListener {
            Log.i(tag, "Navigation: Vers Alimentation (AlimActivity)")
            startActivity(Intent(activity, AlimActivity::class.java))
        }

        // Bien dans sa tête
        tete.setOnClickListener {
            Log.i(tag, "Navigation: Vers Bien-être Mental (BienTeteActivity)")
            startActivity(Intent(activity, BienTeteActivity::class.java))
        }

        // Bien dans son corps
        corps.setOnClickListener {
            Log.i(tag, "Navigation: Vers Bien-être Physique (BienCorpsActivity)")
            startActivity(Intent(activity, BienCorpsActivity::class.java))
        }

        // Boîte à outils
        outils.setOnClickListener {
            Log.i(tag, "Navigation: Vers Boîte à Outils (ToolboxActivity)")
            startActivity(Intent(activity, ToolboxActivity::class.java))
        }

        // Calendrier / Suivi
        calendarActivity.setOnClickListener {
            Log.i(tag, "Navigation: Vers Calendrier (CalendarActivity)")
            startActivity(Intent(activity, CalendarActivity::class.java))
        }
    }
}