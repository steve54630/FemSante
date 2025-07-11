package com.audreyRetournayDiet.femSante.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.main.HomeActivity
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.UserManager
import com.audreyRetournayDiet.femSante.utilitaires.LoadingAlert
import org.json.JSONObject

class LoginFragment : Fragment() {

    private lateinit var password: EditText
    private lateinit var email: EditText
    private lateinit var connect: Button
    private lateinit var forgotPassword: Button
    private lateinit var userManager: UserManager
    private lateinit var alert: LoadingAlert

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        password = view.findViewById(R.id.Password)
        email = view.findViewById(R.id.Login)
        connect = view.findViewById(R.id.buttonConnect)
        forgotPassword = view.findViewById(R.id.buttonForgotten)
        userManager = UserManager(view.context)
        alert = LoadingAlert(requireActivity())

        connect.setOnClickListener {

            alert.start()

            if (email.text.toString() == "" || password.text.toString() == "") {
                Toast.makeText(
                    view.context,
                    "Veuillez saisir les champs demandés",
                    Toast.LENGTH_SHORT
                )
                    .show()
                alert.close()
            } else {
                val parameters = JSONObject()
                parameters.put("email", email.text.toString())
                parameters.put("password", password.text.toString())
                userManager.connectUser(parameters) { apiResult ->

                    when (apiResult) {
                        is ApiResult.Success -> {
                            Toast.makeText(context, apiResult.message, Toast.LENGTH_SHORT).show()

                            val intent = Intent(activity, HomeActivity::class.java)

                            intent.putExtra("A vie", apiResult.data!!.getBoolean("A vie"))
                            intent.putExtra(
                                "map", hashMapOf(
                                    "login" to email.text.toString(),
                                    "password" to password.text.toString()
                                )
                            )

                            alert.close()

                            requireActivity().finish()
                            requireActivity().startActivity(intent)

                        }

                        is ApiResult.Failure -> {
                            alert.close()
                            Toast.makeText(context, apiResult.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        forgotPassword.setOnClickListener {
            startActivity(Intent(activity, ForgottenActivity::class.java))
        }

        return view

    }
}