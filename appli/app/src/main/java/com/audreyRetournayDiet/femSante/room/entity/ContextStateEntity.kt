package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity

/**
 * Entité représentant le contexte externe et les habitudes d'une journée.
 * * ### Architecture de données :
 * - **Clé Étrangère** : Liée à [DailyEntryEntity]. Si la journée est supprimée,
 * ce contexte l'est aussi automatiquement ([ForeignKey.CASCADE]).
 * - **Index** : L'index sur `entry_id` optimise les performances de recherche
 * lors de la reconstruction de l'objet complet [DailyEntryFull].
 */
@Entity(
    tableName = "context_state",
    foreignKeys = [ForeignKey(
        entity = DailyEntryEntity::class,
        parentColumns = ["id"],
        childColumns = ["entry_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["entry_id"])]
)
data class ContextStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Identifiant de la journée parente (Lien indispensable pour la cohérence) */
    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    /** Niveau d'exercice physique (utilise le convertisseur d'Enum) */
    @ColumnInfo(name = "physical_activity")
    val physicalActivity: PhysicalActivity? = PhysicalActivity.REPOS,

    /**
     * Précision libre sur l'activité (ex. « 8000 pas », « Yoga 45 min ») — texte libre plutôt
     * qu'un compteur strict, pour couvrir aussi bien la marche que le sport pratiqué.
     */
    @ColumnInfo(name = "activity_detail")
    val activityDetail: String? = null,

    /** Indicateur de prise de traitement médicamenteux */
    @ColumnInfo(name = "medecine_taken")
    val medecineTaken: Boolean = false,

    /** Détails des médicaments ou compléments alimentaires */
    @ColumnInfo(name = "medication_list")
    val medicationList: String? = "",

    @ColumnInfo(name = "diet_morning")
    val dietMorning: String? = null,

    @ColumnInfo(name = "diet_noon")
    val dietNoon: String? = null,

    @ColumnInfo(name = "diet_evening")
    val dietEvening: String? = null
)