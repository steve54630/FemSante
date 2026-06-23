package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité maîtresse représentant une "journée" dans le journal de bord.
 *
 * ### Rôle architectural :
 * Cette table sert de pivot. Elle ne contient pas les détails de santé elle-même,
 * mais elle porte l'ID (`id`) que toutes les autres tables utilisent comme clé étrangère.
 *
 * ### Contraintes de sécurité :
 * - **Unicité** : L'index unique sur `[user_id, date]` empêche de créer deux journées
 * identiques pour la même personne.
 * - **Intégrité** : Si le compte utilisateur est supprimé, toutes ses entrées
 * disparaissent par cascade.
 */
@Entity(
    tableName = "daily_entry",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        // Empêche les doublons : une seule entrée par jour et par utilisateur
        Index(value = ["user_id", "date"], unique = true)
    ]
)
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Identifiant de l'utilisatrice (lié à UserEntity) */
    @ColumnInfo(name = "user_id")
    val userId: String,

    /** * Timestamp représentant le jour (généralement normalisé à 00:00:00).
     * Utilisé pour le tri et l'affichage dans le calendrier.
     */
    @ColumnInfo(name = "date")
    val date: Long
)