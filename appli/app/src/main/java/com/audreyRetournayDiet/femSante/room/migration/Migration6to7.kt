package com.audreyRetournayDiet.femSante.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration v6 -> v7 : détail de l'activité physique + alimentation par moment de la journée.
 *
 * - `activity_detail` (context_state) : précision libre sur l'activité (nombre de pas pour la
 *   marche, type de sport...), en complément du niveau REPOS/MARCHE/SPORT existant.
 * - `diet` (un seul champ libre) devient trois champs par moment de la journée (`diet_morning`,
 *   `diet_noon`, `diet_evening`). L'ancien contenu est repris dans `diet_evening` par défaut —
 *   impossible de deviner à quel moment il se rapportait, ce champ étant le plus proche d'une
 *   notion générique de "repas du jour".
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `context_state` ADD COLUMN `activity_detail` TEXT")
        db.execSQL("ALTER TABLE `context_state` ADD COLUMN `diet_morning` TEXT")
        db.execSQL("ALTER TABLE `context_state` ADD COLUMN `diet_noon` TEXT")
        db.execSQL("ALTER TABLE `context_state` ADD COLUMN `diet_evening` TEXT")
        db.execSQL("UPDATE `context_state` SET `diet_evening` = `diet` WHERE `diet` IS NOT NULL AND `diet` != ''")
        db.execSQL("ALTER TABLE `context_state` DROP COLUMN `diet`")
    }
}
