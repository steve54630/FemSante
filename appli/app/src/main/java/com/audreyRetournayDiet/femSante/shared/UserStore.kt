package com.audreyRetournayDiet.femSante.shared

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.audreyRetournayDiet.femSante.data.entities.AppUser
import com.audreyRetournayDiet.femSante.room.type.CycleProfile

class UserStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_user_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveUser(user: AppUser) {
        sharedPreferences.edit().apply {
            putString("user_id", user.id)
            putString("user_email", user.email)
            putString("user_password", user.password)
            putBoolean("user_avie", user.lifetimeAccess)
            putBoolean("user_access", user.hasAccess)
            apply()
        }
    }

    fun getUser(): AppUser? {
        val id = sharedPreferences.getString("user_id", null) ?: return null
        val email = sharedPreferences.getString("user_email", "") ?: ""
        val password = sharedPreferences.getString("user_password", "") ?: ""
        val aVie = sharedPreferences.getBoolean("user_avie", false)
        // Repli sur « à vie » si l'accès n'a pas encore été enregistré (sessions antérieures).
        val hasAccess = sharedPreferences.getBoolean("user_access", aVie)

        return AppUser(id, aVie, email, password, hasAccess)
    }

    /**
     * **Point unique** de vérification d'accès aux contenus premium (cadenas, gating).
     *
     * Basé sur [AppUser.hasAccess] (statut d'abonnement/accès renvoyé par l'API, distinct de
     * l'abonnement à vie) : c'est le seul endroit à faire évoluer si la logique freemium change.
     */
    fun hasContentAccess(): Boolean = getUser()?.hasAccess ?: false

    fun clearSession() {
        sharedPreferences.edit { clear() }
    }

    // --- Onboarding ---

    /** Écrans de bienvenue affichés une seule fois, avant la première connexion. */
    fun hasSeenOnboarding(): Boolean = sharedPreferences.getBoolean("onboarding_seen", false)

    fun setOnboardingSeen() {
        sharedPreferences.edit { putBoolean("onboarding_seen", true) }
    }

    // --- Token d'authentification API (Sanctum) ---

    /** Enregistre le token personnel reçu au login (utilisé en Bearer pour les flux vidéo/audio). */
    fun saveToken(token: String) {
        sharedPreferences.edit { putString("auth_token", token) }
    }

    /** Token courant, ou null si absent. Effacé par [clearSession]. */
    fun getToken(): String? = sharedPreferences.getString("auth_token", null)

    // --- Profil de cycle (suivi menstruel) ---

    /**
     * Profil de cycle déclaré. Tant que l'utilisatrice n'a pas répondu, on considère le
     * cycle comme [CycleProfile.IRREGULIER] (mode prudent : aucune prédiction).
     */
    fun getCycleProfile(): CycleProfile {
        val stored = sharedPreferences.getString("cycle_profile", null) ?: return CycleProfile.IRREGULIER
        return runCatching { CycleProfile.valueOf(stored) }.getOrDefault(CycleProfile.IRREGULIER)
    }

    fun setCycleProfile(profile: CycleProfile) {
        sharedPreferences.edit(commit = true) { putString("cycle_profile", profile.name) }
    }

    /** Indique si l'utilisatrice a déjà renseigné son profil de cycle (pour le prompt initial). */
    fun hasCycleProfile(): Boolean = sharedPreferences.contains("cycle_profile")

    /** Invitation à renseigner le cycle : affichée une seule fois pour ne pas gêner l'onglet. */
    fun hasSeenCyclePrompt(): Boolean = sharedPreferences.getBoolean("cycle_prompt_seen", false)

    fun setCyclePromptSeen() {
        sharedPreferences.edit { putBoolean("cycle_prompt_seen", true) }
    }

    // --- Paramètres de cycle déclarés (dans le profil) ---

    /** Durée moyenne du cycle déclarée par l'utilisatrice (jours). Défaut : 28. */
    fun getCycleLength(): Int = sharedPreferences.getInt("cycle_length", DEFAULT_CYCLE_LENGTH)

    fun setCycleLength(days: Int) {
        sharedPreferences.edit(commit = true) { putInt("cycle_length", days) }
    }

    /** Durée des règles déclarée (jours). Défaut : 5. */
    fun getPeriodLength(): Int = sharedPreferences.getInt("period_length", DEFAULT_PERIOD_LENGTH)

    fun setPeriodLength(days: Int) {
        sharedPreferences.edit(commit = true) { putInt("period_length", days) }
    }

    companion object {
        const val DEFAULT_CYCLE_LENGTH = 28
        const val DEFAULT_PERIOD_LENGTH = 5
    }
}