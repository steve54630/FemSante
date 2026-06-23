package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité représentant l'état physique général pour une journée donnée.
 * * ### Rôle fonctionnel :
 * Cette table stocke les métriques de base qui servent souvent à générer
 * les "points de couleur" ou les scores de forme dans ton interface Calendrier.
 * * ### Sécurité des données :
 * - **Lien Parent** : Connecté à [DailyEntryEntity] via `entry_id`.
 * - **Cascade** : Si l'entrée principale du journal est supprimée, cet état est
 * automatiquement nettoyé de la base SQLite.
 * - **Performance** : L'index sur `entry_id` accélère les jointures lors de
 * la récupération d'une journée complète.
 */
@Entity(
    tableName = "general_state",
    foreignKeys = [
        ForeignKey(
            entity = DailyEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["entry_id"])]
)
data class GeneralStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Identifiant de la journée de suivi associée */
    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    /** Indique si l'utilisatrice a ressenti de la fatigue ce jour-là */
    @ColumnInfo(name = "is_tired")
    val isTired: Boolean = false,

    /** * Niveau de douleur globale (ex: échelle de 0 à 10).
     * Utilisé pour la coloration visuelle du calendrier de suivi.
     */
    @ColumnInfo(name = "pain_level")
    val painLevel: Int = 0
)