package com.audreyRetournayDiet.femSante.features.alim

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AlimActivity : AppCompatActivity() {

    private lateinit var menu: BottomNavigationView
    private var alim = AlimFragment()
    private var doc = RessourceFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alim)

        menu = findViewById(R.id.bottom_navigation_menu)

        supportFragmentManager.beginTransaction().replace(R.id.container, alim).commit()

        menu.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.alim ->{ supportFragmentManager.beginTransaction().replace(R.id.container, alim)
                    .commit()
                    return@setOnItemSelectedListener true
                }
                R.id.pdf -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, doc)
                        .commit()
                    return@setOnItemSelectedListener true
                }
            }

            return@setOnItemSelectedListener false
        }
    }

}