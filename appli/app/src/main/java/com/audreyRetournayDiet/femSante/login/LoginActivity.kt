package com.audreyRetournayDiet.femSante.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class LoginActivity : AppCompatActivity() {

    private lateinit var menu: BottomNavigationView
    private var login = LoginFragment()
    private var doc = DocFragment()
    private var register = CreateFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        menu = findViewById(R.id.bottom_navigation_menu)

        supportFragmentManager.beginTransaction().replace(R.id.container, login).commit()

        menu.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.login ->{ supportFragmentManager.beginTransaction().replace(R.id.container, login)
                    .commit()
                    return@setOnItemSelectedListener true
                }
                R.id.pdf -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, doc)
                        .commit()
                    return@setOnItemSelectedListener true
                }
                R.id.register -> {
                    supportFragmentManager.beginTransaction().replace(R.id.container, register)
                        .commit()
                    return@setOnItemSelectedListener true
                }
            }

            return@setOnItemSelectedListener false

        }
    }

}