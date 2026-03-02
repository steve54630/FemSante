package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.audreyRetournayDiet.femSante.room.type.PainZone

/**
 * Entité représentant les symptômes physiques spécifiques d'une journée.
 * * ### Rôle dans l'application :
 * Cette table permet de stocker les manifestations cliniques (douleurs localisées,
 * nausées, etc.) afin d'identifier des schémas récurrents au cours du cycle
 * ou après certains repas.
 * * ### Sécurité des données :
 * - **Clé Étrangère** : Liée à [DailyEntryEntity]. La suppression de la journée
 * entraîne la purge automatique des symptômes ([ForeignKey.CASCADE]).
 * - **Indexation** : L'index sur `entry_id` garantit une récupération rapide
 * lors de l'affichage du récapitulatif journalier.
 */
@Entity(
    tableName = "symptom_state",
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
data class SymptomStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Identifiant de la journée parente dans le journal */
    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    /** * Liste des zones anatomiques douloureuses (ex: PELVIS, LOMBAIRES, SEINS).
     * Room utilise [com.audreyRetournayDiet.femSante.room.converter.PainZoneConverter] pour sérialiser cette liste en texte.
     */
    @ColumnInfo(name = "pain_zone")
    val localizedPains : List<PainZone> = emptyList(),

    /** Indicateur de nausées ou troubles digestifs hauts */
    @ColumnInfo(name = "nausea")
    val hasNausea : Boolean = false,

    /** Champ libre pour décrire d'autres symptômes (ex: acné, maux de tête) */
    @ColumnInfo(name = "others")
    val others : String? = ""
)