package com.audreyRetournayDiet.femSante.data

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class AppUser(
    val id: String,
    val lifetimeAccess: Boolean,
    val email: String,
    val password: String,
)

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
}