package com.audreyRetournayDiet.femSante.room.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.audreyRetournayDiet.femSante.room.migration.MIGRATION_1_2
import com.audreyRetournayDiet.femSante.room.migration.MIGRATION_2_3
import com.audreyRetournayDiet.femSante.shared.DatabaseKeyProvider
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * Fournisseur unique (Singleton) pour la base de données Room.
 *
 * Configuré en mode TRUNCATE pour garantir une écriture/lecture synchrone immédiate avec SQLCipher,
 * parfaitement adapté aux relectures explicites (ex: en onResume).
 */
class DatabaseProvider {

    companion object {

        private const val DB_NAME = "fem_sante_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): AppDatabase {
            System.loadLibrary("sqlcipher")

            val passphrase = DatabaseKeyProvider.getOrCreatePassphrase(appContext)
            val factory = SupportOpenHelperFactory(passphrase)

            return Room.databaseBuilder(appContext, AppDatabase::class.java, DB_NAME)
                .openHelperFactory(factory)
                // Mode TRUNCATE : Désactive le WAL. Indispensable avec SQLCipher pour éviter
                // que les requêtes 'suspend' directes ne lisent des pages périmées isolées en mémoire.
                .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                // 🟢 OUTIL DE DIAGNOSTIC : Log les requêtes exécutées pour voir EXACTEMENT ce que la base observe
                .setQueryCallback({ sqlQuery, bindArgs ->
                    Timber.d("Room SQL Executed -> %s | Args: %s", sqlQuery, bindArgs)
                }, Executors.newSingleThreadExecutor())
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
        }

        fun clearDatabase(context: Context) {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                context.applicationContext.deleteDatabase(DB_NAME)
            }
        }
    }
}