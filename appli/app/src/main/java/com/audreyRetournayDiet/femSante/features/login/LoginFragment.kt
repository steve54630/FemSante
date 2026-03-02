package com.audreyRetournayDiet.femSante.features.login

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
import timber.log.Timber

/**
 * Fragment gérant l'authentification des utilisateurs.
 *
 * Ce composant assure le pont entre le service d'authentification distant (API),
 * la base de données locale (Room) et la gestion de session (SharedPreferences).
 *
 * ### Flux de connexion :
 * 1. Validation des champs locaux.
 * 2. Appel au [UserManager] pour l'authentification réseau.
 * 3. Synchronisation avec [UserRepository] pour s'assurer que l'utilisateur existe en local.
 * 4. Persistance de la session dans [UserStore] avant redirection.
 */
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
        Timber.d("onCreateView : Initialisation du formulaire de connexion")
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        initViews(view)
        setupListeners()

        return view
    }

    /**
     * Initialise les instances des composants UI et les accès aux données (DB & Repository).
     */
    private fun initViews(view: View) {
        db = DatabaseProvider.getDatabase(view.context)
        password = view.findViewById(R.id.Password)
        email = view.findViewById(R.id.Login)
        connect = view.findViewById(R.id.buttonConnect)
        forgotPassword = view.findViewById(R.id.buttonForgotten)

        userManager = UserManager(view.context)
        alert = LoadingAlert(requireActivity())
        userRepository = UserRepository(db.userDao())
    }

    /**
     * Configure les actions de clics sur les boutons de connexion et de récupération.
     */
    private fun setupListeners() {
        connect.setOnClickListener {
            Timber.v("Clic : Tentative de connexion")
            onConnectClicked()
        }

        forgotPassword.setOnClickListener {
            Timber.d("Navigation : Mot de passe oublié")
            startActivity(Intent(activity, ForgottenActivity::class.java))
        }
    }

    /**
     * Gère la logique de clic sur le bouton de connexion.
     * Effectue une validation locale simple avant de lancer l'appel API via une Coroutine.
     */
    private fun onConnectClicked() {
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString().trim()

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Timber.w("Validation échouée : champs vides")
            showError("Veuillez saisir les champs demandés")
            return
        }

        alert.start()

        lifecycleScope.launch {
            try {
                val parameters = JSONObject().apply {
                    put("email", emailText)
                    put("password", passwordText)
                }

                Timber.i("Requête API : Connexion pour $emailText")
                when (val apiResult = userManager.connectUser(parameters)) {
                    is ApiResult.Success<JSONObject> -> {
                        Timber.d("API Success : Utilisateur authentifié")
                        handleLoginSuccess(apiResult, emailText, passwordText)
                    }
                    is ApiResult.Failure -> {
                        Timber.e("API Failure : ${apiResult.message}")
                        showError(apiResult.message)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Exception lors de la connexion")
                showError("Une erreur inattendue est survenue")
            } finally {
                alert.close()
            }
        }
    }

    /**
     * Finalise la session utilisateur après une réponse API positive.
     * Cette méthode orchestre la synchronisation entre la base Room et le stockage de session.
     *
     * @param apiResult Résultat de l'API contenant les informations de compte (ex: lifetimeAccess).
     * @param emailText Email utilisé pour la connexion.
     * @param passwordText Mot de passe utilisé (pour le stockage sécurisé en session).
     */
    private suspend fun handleLoginSuccess(
        apiResult: ApiResult.Success<JSONObject>,
        emailText: String,
        passwordText: String
    ) {
        // Extraction des droits d'accès
        val lifetimeAccess = apiResult.data?.optBoolean("lifetimeAccess", false) ?: false
        Timber.v("Droits d'accès : Premium=$lifetimeAccess")

        // 1️⃣ Synchronisation Locale (Room)
        // Recherche de l'utilisateur ou création s'il se connecte pour la première fois sur ce device
        Timber.d("Room : Recherche de l'utilisateur en local")
        val userId: String? = when (val userResult = userRepository.getUser(emailText)) {
            is ApiResult.Success -> {
                val id = userResult.data?.getString("id")
                Timber.d("Room : Utilisateur trouvé (ID: $id)")
                id
            }
            is ApiResult.Failure -> {
                Timber.i("Room : Nouvel utilisateur sur ce device, création du profil local")
                when (val addResult = userRepository.addUser(UserEntity(login = emailText))) {
                    is ApiResult.Success -> {
                        val newId = addResult.data?.getString("id")
                        Timber.d("Room : Profil créé avec succès (ID: $newId)")
                        newId
                    }
                    is ApiResult.Failure -> {
                        Timber.e("Room Error : Impossible d'ajouter l'utilisateur")
                        showError(addResult.message)
                        null
                    }
                }
            }
        }

        if (userId == null) {
            Timber.e("Critique : Aucun ID utilisateur disponible pour finaliser la session")
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

        UserStore(requireContext()).saveUser(newUser)
        Timber.i("Session : Utilisateur $emailText sauvegardé dans le Store")

        // 3️⃣ Navigation finale
        navigateToHome()
    }

    /**
     * Redirige l'utilisateur vers l'écran d'accueil et ferme l'activité de login.
     */
    private fun navigateToHome() {
        Timber.d("Navigation -> HomeActivity")
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        requireActivity().apply {
            startActivity(intent)
            finish() // Sécurité : empêche le retour arrière vers le login
        }
    }

    /**
     * Affiche un message d'erreur via un Toast.
     */
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}