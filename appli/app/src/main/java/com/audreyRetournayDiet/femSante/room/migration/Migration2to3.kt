package com.audreyRetournayDiet.femSante.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration v2 -> v3 : ajoute la table `cycle_day` (suivi de cycle, incrément 1).
 *
 * Table additive et indépendante du journal. Les définitions de colonnes correspondent
 * exactement au schéma généré par Room pour [com.audreyRetournayDiet.femSante.room.entity.CycleDayEntity]
 * (pas de clause DEFAULT, car l'entité n'utilise pas `@ColumnInfo(defaultValue = ...)`).
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `cycle_day` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `user_id` TEXT NOT NULL,
                `date` INTEGER NOT NULL,
                `is_period` INTEGER NOT NULL,
                `flow` TEXT,
                `spotting` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_cycle_day_user_id_date` ON `cycle_day` (`user_id`, `date`)"
        )
    }
}
