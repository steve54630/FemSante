package com.audreyRetournayDiet.femSante.room.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration v3 -> v4 : ajoute la douleur **par zone** (carte du corps).
 *
 * Colonne additive `symptom_state.pain_by_zone` : `Map<PainZone, Int>` sérialisée en texte
 * (voir [com.audreyRetournayDiet.femSante.room.converter.PainZoneLevelConverter]). La
 * définition (`TEXT NOT NULL DEFAULT ''`) correspond exactement au schéma généré par Room
 * pour le champ `painByZone` (`@ColumnInfo(defaultValue = "")`).
 *
 * Les nouvelles valeurs d'enum (Bras, Cuisses, Haut du dos, Jambes) sont sérialisées par
 * nom : aucun changement de schéma SQL nécessaire pour elles.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE symptom_state ADD COLUMN pain_by_zone TEXT NOT NULL DEFAULT ''"
        )
    }
}
