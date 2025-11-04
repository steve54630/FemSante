package com.audreyRetournayDiet.femSante.domain.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var menu: BottomNavigationView
    private var home = MainMenuFragment()
    private var account = AccountFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        menu = findViewById(R.id.bottom_navigation_menu)

        supportFragmentManager.beginTransaction().replace(R.id.container, home).commit()

        menu.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.menu ->{ supportFragmentManager.beginTransaction().replace(R.id.container, home)
                    .commit()
                    return@setOnItemSelectedListener true
                }
                R.id.account -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, account)
                        .commit()
                    return@setOnItemSelectedListener true

                }
            }

            return@setOnItemSelectedListener false

        }
    }

}