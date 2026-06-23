package com.audreyRetournayDiet.femSante.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Première migration du projet (v1 -> v2).
 *
 * Ajoute deux colonnes additives, sans toucher aux données existantes :
 * - `symptom_state.bristol_type` : transit digestif (échelle de Bristol).
 * - `psychological_state.is_emotionally_difficult_day` : état moral du jour,
 *   distinct de `day_quality` qui reste l'état physique global.
 *
 * `PainZone.ABDOMEN` n'apparaît pas ici : c'est une valeur d'enum sérialisée en
 * texte CSV (voir [com.audreyRetournayDiet.femSante.room.converter.PainZoneConverter]),
 * elle ne nécessite donc aucun changement de schéma SQL.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE symptom_state ADD COLUMN bristol_type TEXT NOT NULL DEFAULT 'NON_RENSEIGNE'"
        )
        db.execSQL(
            "ALTER TABLE psychological_state ADD COLUMN is_emotionally_difficult_day INTEGER NOT NULL DEFAULT 0"
        )
    }
}
