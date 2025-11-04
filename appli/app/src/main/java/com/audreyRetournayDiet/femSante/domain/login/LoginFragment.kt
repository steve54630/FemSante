package com.audreyRetournayDiet.femSante.domain.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.domain.main.HomeActivity
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.UserRepository
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.room.database.AppDatabase
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.room.entity.UserEntity
import com.audreyRetournayDiet.femSante.utilitaires.LoadingAlert
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginFragment : Fragment() {

    private lateinit var password: EditText
    private lateinit var email: EditText
    private lateinit var connect: Button
    private lateinit var forgotPassword: Button
    private lateinit var userManager: UserManager
    private lateinit var alert: LoadingAlert
    private lateinit var db: AppDatabase
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        db = DatabaseProvider.getDatabase(requireContext())
        password = view.findViewById(R.id.Password)
        email = view.findViewById(R.id.Login)
        connect = view.findViewById(R.id.buttonConnect)
        forgotPassword = view.findViewById(R.id.buttonForgotten)
        userManager = UserManager(view.context)
        alert = LoadingAlert(requireActivity())
        userRepository = UserRepository(db.userDao())

        connect.setOnClickListener { onConnectClicked() }

        forgotPassword.setOnClickListener {
            startActivity(Intent(activity, ForgottenActivity::class.java))
        }

        return view
    }

    private fun onConnectClicked() {
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString().trim()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez saisir les champs demandés", Toast.LENGTH_SHORT).show()
            return
        }

        alert.start()

        lifecycleScope.launch {
            try {
                val parameters = JSONObject().apply {
                    put("email", emailText)
                    put("password", passwordText)
                }

                when (val apiResult = userManager.connectUser(parameters)) {
                    is ApiResult.Success -> handleLoginSuccess(apiResult, emailText, passwordText)
                    is ApiResult.Failure -> showError(apiResult.message)
                }
            } finally {
                alert.close()
            }
        }
    }

    private suspend fun handleLoginSuccess(
        apiResult: ApiResult.Success,
        emailText: String,
        passwordText: String
    ) {
        Toast.makeText(requireContext(), apiResult.message, Toast.LENGTH_SHORT).show()

        val intent = Intent(requireActivity(), HomeActivity::class.java).apply {
            putExtra("A vie", apiResult.data?.getBoolean("A vie") == true)
            putExtra("map", hashMapOf("login" to emailText, "password" to passwordText))
        }

        val userResult = userRepository.getUser(emailText)
        if (userResult is ApiResult.Failure) {
            val addResult = userRepository.addUser(UserEntity(login = emailText))
            if (addResult is ApiResult.Failure) showError(addResult.message)
        }

        navigateToHome(intent)
    }


    private fun navigateToHome(intent: Intent) {
        requireActivity().finish()
        requireActivity().startActivity(intent)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
