package com.audreyRetournayDiet.femSante.room.database

import android.content.Context
import androidx.room.Room

class DatabaseProvider {

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fem_sante_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}