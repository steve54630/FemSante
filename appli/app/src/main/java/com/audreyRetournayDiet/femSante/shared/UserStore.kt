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
            apply()
        }
    }

    fun getUser(): AppUser? {
        val id = sharedPreferences.getString("user_id", null) ?: return null
        val email = sharedPreferences.getString("user_email", "") ?: ""
        val password = sharedPreferences.getString("user_password", "") ?: ""
        val aVie = sharedPreferences.getBoolean("user_avie", false)

        return AppUser(id, aVie, email, password)
    }

    fun clearSession() {
        sharedPreferences.edit { clear() }
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
        sharedPreferences.edit { putString("cycle_profile", profile.name) }
    }

    /** Indique si l'utilisatrice a déjà renseigné son profil de cycle (pour le prompt initial). */
    fun hasCycleProfile(): Boolean = sharedPreferences.contains("cycle_profile")
}