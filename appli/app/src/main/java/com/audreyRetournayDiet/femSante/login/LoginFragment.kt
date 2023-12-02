package com.audreyRetournayDiet.femSante.login

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.main.HomeActivity
import com.audreyRetournayDiet.femSante.utilitaires.DatabaseManager
import com.audreyRetournayDiet.femSante.utilitaires.LoadingAlert
import org.json.JSONObject

class LoginFragment : Fragment() {

    private lateinit var password: EditText
    private lateinit var email: EditText
    private lateinit var connect: Button
    private lateinit var forgotPassword: Button
    private lateinit var databaseManager: DatabaseManager
    private lateinit var alert: LoadingAlert
    private var mLastClickTime: Long = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        password = view.findViewById(R.id.Password)
        email = view.findViewById(R.id.Login)
        connect = view.findViewById(R.id.buttonConnect)
        forgotPassword = view.findViewById(R.id.buttonForgotten)
        databaseManager = DatabaseManager(view.context)
        alert = LoadingAlert(requireActivity())

        connect.setOnClickListener {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return@setOnClickListener
            }
            mLastClickTime = SystemClock.elapsedRealtime()

            alert.startAlertDialog()

            if (email.text.toString() == "" || password.text.toString() == "") {
                Toast.makeText(view.context, "Veuillez saisir les champs demandés", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val parameters = JSONObject()
                parameters.put("email", email.text.toString())
                parameters.put("password", password.text.toString())
                databaseManager.connectUser(
                    parameters,
                    view.context, activity as AppCompatActivity,
                    Intent(activity, HomeActivity::class.java),
                    alert
                )
            }
        }

        forgotPassword.setOnClickListener {
            startActivity(Intent(activity, ForgottenActivity::class.java))
        }

        return view

    }
}