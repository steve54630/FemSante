package com.audreyRetournayDiet.femSante.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration v4 -> v5 : sommeil et journal de gratitude (onglet « Moral & sommeil »).
 *
 * Colonnes additives **nullables** (donc sans `NOT NULL` ni `DEFAULT`, conformément au
 * schéma généré par Room pour des champs `Int?` / `String?`) :
 * - `general_state.bed_time`, `general_state.wake_time` : heures de coucher/réveil (minutes).
 * - `psychological_state.gratitude` : note positive du jour.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE general_state ADD COLUMN bed_time INTEGER")
        db.execSQL("ALTER TABLE general_state ADD COLUMN wake_time INTEGER")
        db.execSQL("ALTER TABLE psychological_state ADD COLUMN gratitude TEXT")
    }
}
