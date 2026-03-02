package com.audreyRetournayDiet.femSante.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.audreyRetournayDiet.femSante.room.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) pour la gestion des profils utilisateurs.
 * * Cette interface permet de gérer l'identité locale de l'utilisatrice.
 * Elle est utilisée lors de la phase de connexion (Login) ou de création
 * de compte pour vérifier si les données de l'utilisatrice sont déjà
 * présentes dans la base SQLite du téléphone.
 */
@Dao
interface UserDao {

    /**
     * Insère une nouvelle utilisatrice en base de données.
     * * L'utilisation de [OnConflictStrategy.IGNORE] est une sécurité :
     * si une utilisatrice avec le même identifiant existe déjà, Room n'écrasera
     * pas les données existantes et retournera -1.
     * * @param user L'entité contenant le login et les informations de profil.
     * @return L'ID (RowID) de l'utilisatrice créée ou -1 en cas de conflit.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity): Long

    /**
     * Récupère le profil complet d'une utilisatrice à partir de son identifiant (email/login).
     * * @param userId Le login unique de l'utilisatrice.
     * @return Un [Flow] permettant d'observer les changements du profil en temps réel.
     * Émet `null` si aucune correspondance n'est trouvée.
     */
    @Query("SELECT * FROM user WHERE login = :userId LIMIT 1")
    fun getByLogin(userId: String): Flow<UserEntity?>
}