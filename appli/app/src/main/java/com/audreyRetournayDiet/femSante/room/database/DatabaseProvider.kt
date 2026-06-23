package com.audreyRetournayDiet.femSante.room.database

import android.content.Context
import androidx.room.Room
import com.audreyRetournayDiet.femSante.room.migration.MIGRATION_1_2
import com.audreyRetournayDiet.femSante.room.migration.MIGRATION_2_3
import com.audreyRetournayDiet.femSante.shared.DatabaseKeyProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/**
 * Fournisseur unique (Singleton) pour la base de données Room.
 * * Cette classe assure qu'une seule instance de [AppDatabase] est créée
 * et partagée à travers toute l'application, évitant ainsi les fuites
 * de mémoire et les conflits d'accès aux fichiers SQLite.
 *
 * La base est **chiffrée au repos via SQLCipher** (données médicales locales, RGPD) :
 * la passphrase provient de [DatabaseKeyProvider] (Keystore), jamais codée en dur.
 */
class DatabaseProvider {

    companion object {

        private const val DB_NAME = "fem_sante_db"

        /**
         * L'instance unique de la base de données.
         * L'annotation @Volatile garantit que les modifications de cette variable
         * sont immédiatement visibles par tous les threads (sécurité multi-thread).
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Récupère l'instance de la base de données ou la crée si elle n'existe pas.
         * * @param context Le contexte de l'application (utilisé pour localiser le fichier DB).
         * @return L'instance unique de [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            // "Double-checked locking" : on vérifie si l'instance existe déjà
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): AppDatabase {
            // L'artefact sqlcipher-android ne charge pas la lib native automatiquement :
            // il faut le faire explicitement avant d'ouvrir la base.
            System.loadLibrary("sqlcipher")

            // Passphrase de chiffrement issue du Keystore (SQLCipher)
            val passphrase = DatabaseKeyProvider.getOrCreatePassphrase(appContext)
            val factory = SupportOpenHelperFactory(passphrase)

            return Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }

        /**
         * Efface définitivement la base de données locale (toutes les données médicales).
         *
         * Destiné au **droit à l'effacement RGPD** : à appeler depuis le flux de
         * suppression de compte (avec [com.audreyRetournayDiet.femSante.shared.UserStore.clearSession]).
         * Volontairement **non appelé à la déconnexion** : un logout ne supprime pas
         * l'historique local de l'utilisatrice.
         */
        fun clearDatabase(context: Context) {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                context.applicationContext.deleteDatabase(DB_NAME)
            }
        }
    }
}
