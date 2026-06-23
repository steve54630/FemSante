package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause

/**
 * Convertisseur de types permettant de stocker une liste d'énumérations [DifficultyCause]
 * dans une seule colonne de texte SQLite.
 * * ### Stratégie de persistance :
 * - **Sérialisation** : Transforme la liste [STRESS, FATIGUE] en une chaîne "STRESS,FATIGUE".
 * - **Désérialisation** : Découpe la chaîne via la virgule pour recréer la liste d'objets Kotlin.
 */
class CausesConverters {

    /**
     * Convertit une liste de causes de difficulté en une chaîne de caractères CSV (Comma-Separated Values).
     * * @param value La liste d'énums sélectionnée par l'utilisatrice.
     * @return Une chaîne de caractères où chaque énum est séparé par une virgule.
     */
    @TypeConverter
    fun fromList(value: List<DifficultyCause>): String {
        // joinToString transforme la liste en "NOM1,NOM2,NOM3"
        return value.joinToString(",") { it.name }
    }

    /**
     * Reconstruit la liste d'énumérations à partir de la chaîne de caractères stockée en base.
     * * @param value La chaîne récupérée de SQLite (ex: "STRESS, HORMONES").
     * @return Une liste typée [DifficultyCause] prête à être utilisée dans l'application.
     */
    @TypeConverter
    fun toList(value: String): List<DifficultyCause> {
        // Gestion de la sécurité : si la base est vide, on retourne une liste vide
        if (value.isEmpty()) return emptyList()

        // On découpe la chaîne et on convertit chaque nom en sa constante Enum correspondante
        return value.split(",").map { DifficultyCause.valueOf(it) }
    }
}