package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Mesures corporelles d'une journée (suivi bien-être / perte de poids).
 *
 * Sous-état de la journée, comme les autres : lié à [DailyEntryEntity] par `entry_id`
 * ([ForeignKey.CASCADE]). Tous les champs sont **optionnels** (null si non renseignés) —
 * on ne prend que ce qu'on veut suivre. Données strictement locales (RGPD).
 *
 * Rappel produit : le **poids** n'est pas l'indicateur principal ; les **tours** (taille,
 * hanches) reflètent mieux les évolutions. L'UI le rappelle explicitement.
 */
@Entity(
    tableName = "body_measurement",
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
data class BodyMeasurementEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    /** Poids en kilogrammes. */
    @ColumnInfo(name = "weight")
    val weightKg: Double? = null,

    /** Tour de taille en centimètres. */
    @ColumnInfo(name = "waist")
    val waistCm: Double? = null,

    /** Tour de hanches en centimètres. */
    @ColumnInfo(name = "hips")
    val hipsCm: Double? = null,

    /** Tour de cuisses en centimètres. */
    @ColumnInfo(name = "thighs")
    val thighsCm: Double? = null,

    /** Tour de poitrine en centimètres. */
    @ColumnInfo(name = "chest")
    val chestCm: Double? = null,

    /** Tour de bras en centimètres. */
    @ColumnInfo(name = "arms")
    val armsCm: Double? = null
)
