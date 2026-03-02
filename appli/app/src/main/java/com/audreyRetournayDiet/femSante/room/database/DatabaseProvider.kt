package com.audreyRetournayDiet.femSante.room.database

import android.content.Context
import androidx.room.Room

/**
 * Fournisseur unique (Singleton) pour la base de données Room.
 * * Cette classe assure qu'une seule instance de [AppDatabase] est créée
 * et partagée à travers toute l'application, évitant ainsi les fuites
 * de mémoire et les conflits d'accès aux fichiers SQLite.
 */
class DatabaseProvider {

    companion object {
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
                // Si elle est nulle, on verrouille l'accès pour éviter que deux threads
                // ne créent deux bases en même temps.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fem_sante_db" // Nom du fichier SQLite sur le disque
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}