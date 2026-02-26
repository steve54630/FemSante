package com.audreyRetournayDiet.femSante.features.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.entities.AppUser
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.features.main.HomeActivity
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.local.UserRepository
import com.audreyRetournayDiet.femSante.repository.remote.UserManager
import com.audreyRetournayDiet.femSante.room.database.AppDatabase
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.room.entity.UserEntity
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
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
                    is ApiResult.Success<JSONObject> -> handleLoginSuccess(apiResult, emailText, passwordText)
                    is ApiResult.Failure -> showError(apiResult.message)
                }
            } finally {
                alert.close()
            }
        }
    }

    private suspend fun handleLoginSuccess(
        apiResult: ApiResult.Success<JSONObject>,
        emailText: String,
        passwordText: String
    ) {
        Toast.makeText(requireContext(), apiResult.message, Toast.LENGTH_SHORT).show()

        val lifetimeAccess = apiResult.data?.getBoolean("lifetimeAccess") == true

        val intent = Intent(requireActivity(), HomeActivity::class.java)

        // 1️⃣ Vérifier si l'utilisateur existe déjà
        val userId: String? = when (val userResult = userRepository.getUser(emailText)) {

            is ApiResult.Success -> {
                userResult.data?.getString("id")
            }

            is ApiResult.Failure -> {
                // 2️⃣ Ajouter l’utilisateur s’il n’existe pas
                when (val addResult = userRepository.addUser(UserEntity(login = emailText))) {
                    is ApiResult.Success -> addResult.data?.getString("id")
                    is ApiResult.Failure -> {
                        showError(addResult.message)
                        return
                    }
                }
            }
        }

        if (userId == null) {
            showError("Impossible de récupérer l'identifiant utilisateur")
            return
        }

        // 3️⃣ Construire l’utilisateur pour le store
        val newUser = AppUser(
            id = userId,
            lifetimeAccess = lifetimeAccess,
            email = emailText,
            password = passwordText
        )

        val userStore = UserStore(requireContext())

        // 4️⃣ Mise à jour du store (global pour toute l'app)
        userStore.saveUser(newUser)

        Log.d("Login", "User stocké : ${userStore.getUser()?.id}")

        // 5️⃣ Navigation
        navigateToHome(intent)
    }


    private fun navigateToHome(intent: Intent) {
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}
