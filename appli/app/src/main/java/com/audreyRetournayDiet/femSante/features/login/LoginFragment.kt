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

    private val tag = "FRAG_LOGIN"
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
        Log.d(tag, "onCreateView : Initialisation du formulaire de connexion")
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        db = DatabaseProvider.getDatabase(view.context)
        password = view.findViewById(R.id.Password)
        email = view.findViewById(R.id.Login)
        connect = view.findViewById(R.id.buttonConnect)
        forgotPassword = view.findViewById(R.id.buttonForgotten)
        userManager = UserManager(view.context)
        alert = LoadingAlert(requireActivity())
        userRepository = UserRepository(db.userDao())

        connect.setOnClickListener {
            Log.v(tag, "Clic : Tentative de connexion")
            onConnectClicked()
        }

        forgotPassword.setOnClickListener {
            Log.d(tag, "Navigation : Mot de passe oublié")
            startActivity(Intent(activity, ForgottenActivity::class.java))
        }

        return view
    }

    private fun onConnectClicked() {
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString().trim()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Log.w(tag, "Validation échouée : champs vides")
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

                Log.i(tag, "Requête API : Connexion pour $emailText")
                when (val apiResult = userManager.connectUser(parameters)) {
                    is ApiResult.Success<JSONObject> -> {
                        Log.d(tag, "API Success : Utilisateur authentifié")
                        handleLoginSuccess(apiResult, emailText, passwordText)
                    }
                    is ApiResult.Failure -> {
                        Log.e(tag, "API Failure : ${apiResult.message}")
                        showError(apiResult.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception lors de la connexion", e)
                showError("Une erreur inattendue est survenue")
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
        val lifetimeAccess = apiResult.data?.optBoolean("lifetimeAccess", false) ?: false
        Log.v(tag, "Droits d'accès : Premium=$lifetimeAccess")

        // 1️⃣ Synchronisation Locale (Room)
        Log.d(tag, "Room : Recherche de l'utilisateur en local")
        val userId: String? = when (val userResult = userRepository.getUser(emailText)) {
            is ApiResult.Success -> {
                val id = userResult.data?.getString("id")
                Log.d(tag, "Room : Utilisateur trouvé (ID: $id)")
                id
            }
            is ApiResult.Failure -> {
                Log.i(tag, "Room : Nouvel utilisateur, création du profil local")
                when (val addResult = userRepository.addUser(UserEntity(login = emailText))) {
                    is ApiResult.Success -> {
                        val newId = addResult.data?.getString("id")
                        Log.d(tag, "Room : Profil créé avec succès (ID: $newId)")
                        newId
                    }
                    is ApiResult.Failure -> {
                        Log.e(tag, "Room Error : Impossible d'ajouter l'utilisateur")
                        showError(addResult.message)
                        null
                    }
                }
            }
        }

        if (userId == null) {
            Log.e(tag, "Critique : Aucun ID utilisateur disponible pour finaliser la session")
            showError("Erreur d'initialisation du profil")
            return
        }

        // 2️⃣ Mise à jour du UserStore (SharedPreferences)
        val newUser = AppUser(
            id = userId,
            lifetimeAccess = lifetimeAccess,
            email = emailText,
            password = passwordText
        )

        val userStore = UserStore(requireContext())
        userStore.saveUser(newUser)
        Log.i(tag, "Session : Utilisateur $emailText sauvegardé dans le Store")

        // 3️⃣ Navigation
        navigateToHome()
    }

    private fun navigateToHome() {
        Log.d(tag, "Navigation -> HomeActivity")
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        requireActivity().startActivity(intent)
        requireActivity().finish()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}