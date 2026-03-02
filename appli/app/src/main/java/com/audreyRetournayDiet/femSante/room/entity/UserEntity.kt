package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité représentant l'utilisatrice de l'application.
 * * ### Rôle architectural :
 * C'est la table parente de premier niveau. Son identifiant (`id`) est utilisé
 * comme clé étrangère par [DailyEntryEntity] pour isoler les données de chaque profil.
 *
 * ### Sécurité :
 * L'index unique sur le champ `login` garantit qu'il est impossible de créer
 * deux comptes avec le même identifiant (email ou pseudo) en base de données locale.
 */
@Entity(
    tableName = "user",
    indices = [Index(value = ["login"], unique = true)]
)
data class UserEntity(

    /** Identifiant technique interne (utilisé pour les relations SQL) */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Identifiant de connexion (ex: adresse email ou nom d'utilisatrice) */
    @ColumnInfo(name = "login")
    val login : String = ""

)