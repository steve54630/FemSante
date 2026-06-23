package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.audreyRetournayDiet.femSante.room.type.FlowLevel

/**
 * Observation de cycle pour une journée donnée (suivi menstruel, incrément 1).
 *
 * Table **indépendante** du journal de symptômes ([DailyEntryEntity]) : le suivi de cycle
 * fonctionne même sans saisie du journal. Indexée par `(user_id, date)` pour garantir une
 * seule observation par jour et par utilisatrice (upsert).
 *
 * Données strictement locales (RGPD) ; l'effacement se fait par suppression de la base
 * entière sur suppression de compte (cf. DatabaseProvider.clearDatabase).
 */
@Entity(
    tableName = "cycle_day",
    indices = [Index(value = ["user_id", "date"], unique = true)]
)
data class CycleDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: String,

    /** Timestamp du jour (normalisé à 00:00:00), comme le journal. */
    @ColumnInfo(name = "date")
    val date: Long,

    /** Règles ce jour-là. */
    @ColumnInfo(name = "is_period")
    val isPeriod: Boolean = false,

    /** Abondance du flux — pertinent uniquement si [isPeriod] est vrai, sinon null. */
    @ColumnInfo(name = "flow")
    val flow: FlowLevel? = null,

    /** Spotting (pertes légères hors règles). */
    @ColumnInfo(name = "spotting")
    val spotting: Boolean = false
)
