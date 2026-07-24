package com.audreyRetournayDiet.femSante.shared

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Fournit la passphrase de chiffrement de la base Room (SQLCipher).
 *
 * La passphrase est générée aléatoirement une seule fois, puis conservée dans des
 * [EncryptedSharedPreferences] dont la clé maître est gérée par l'**Android Keystore**.
 * Elle n'est donc jamais codée en dur ni stockée en clair — conforme aux exigences RGPD
 * de protection des données de santé au repos.
 */
object DatabaseKeyProvider {

    private const val PREFS_NAME = "secure_db_prefs"
    private const val KEY_NAME = "db_passphrase"
    private const val KEY_SIZE_BYTES = 32 // clé 256 bits

    /**
     * Retourne la passphrase existante, ou en génère une nouvelle (au premier lancement).
     */
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = securePrefs(context)

        prefs.getString(KEY_NAME, null)?.let { stored ->
            return Base64.decode(stored, Base64.NO_WRAP)
        }

        val passphrase = ByteArray(KEY_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit(commit = true) { putString(KEY_NAME, Base64.encodeToString(passphrase, Base64.NO_WRAP)) }
        return passphrase
    }

    private fun securePrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
