package com.audreyRetournayDiet.femSante.login

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.MainActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.DatabaseManager
import com.audreyRetournayDiet.femSante.utilitaires.PdfActivity
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var password: EditText
    private lateinit var email: EditText
    private lateinit var connect: Button
    private lateinit var forgotPassword: Button
    private lateinit var subscribe: Button
    private lateinit var databaseManager: DatabaseManager
    private lateinit var cgu: Button
    private var mLastClickTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        password = findViewById(R.id.Password)
        email = findViewById(R.id.Login)
        connect = findViewById(R.id.buttonConnect)
        forgotPassword = findViewById(R.id.buttonForgotten)
        databaseManager = DatabaseManager(applicationContext)
        subscribe = findViewById(R.id.buttonSubscribe)
        cgu = findViewById(R.id.buttonCGU)

        cgu.setOnClickListener {
            val intentTarget = Intent(this, PdfActivity::class.java)
            intentTarget.putExtra("PDF", "${cgu.text}.pdf")
            startActivity(intentTarget)
        }

        connect.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()

            if (email.text.toString() == "" || password.text.toString() == "") {
                Toast.makeText(this, "Veuillez saisir les champs demandés", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val parameters = JSONObject()
                parameters.put("email", email.text.toString())
                parameters.put("password", password.text.toString())
                databaseManager.connectUser(
                    parameters,
                    this,
                    this,
                    Intent(this, MainActivity::class.java)
                )
            }
        }

        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgottenActivity::class.java))
        }

        subscribe.setOnClickListener {
            startActivity(Intent(this, CreateActivity::class.java))
        }

    }
}