package com.audreyRetournayDiet.femSante.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration v5 -> v6 : table `body_measurement` (mesures corporelles).
 *
 * Sous-état additif lié à la journée ([com.audreyRetournayDiet.femSante.room.entity.BodyMeasurementEntity]),
 * même schéma que celui généré par Room : clé étrangère vers `daily_entry(id)` en CASCADE,
 * index sur `entry_id`, colonnes de mesure `REAL` nullables.
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `body_measurement` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `entry_id` INTEGER NOT NULL,
                `weight` REAL,
                `waist` REAL,
                `hips` REAL,
                `thighs` REAL,
                `chest` REAL,
                `arms` REAL,
                FOREIGN KEY(`entry_id`) REFERENCES `daily_entry`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_body_measurement_entry_id` ON `body_measurement` (`entry_id`)"
        )
    }
}
