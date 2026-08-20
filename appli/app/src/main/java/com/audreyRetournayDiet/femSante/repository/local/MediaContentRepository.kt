package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import com.audreyRetournayDiet.femSante.data.media.MediaItem
import com.audreyRetournayDiet.femSante.data.media.MediaJsonParser
import com.audreyRetournayDiet.femSante.data.media.MediaModule
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fournit le catalogue des contenus bien-être (vidéos/audios), lu une seule fois depuis
 * `assets/media.json`. Remplace l'ancien objet codé en dur `MediaCatalog`.
 *
 * **Point de bascule serveur** : la source des données est isolée dans [load]. Pour passer à un
 * contenu piloté par l'API (ajouter une vidéo sans republier l'app), il suffira de remplacer la
 * lecture de l'asset par un appel réseau ici — les écrans n'ont pas à changer.
 *
 * Injecté par constructeur et unique (`@Singleton`). Même patron que [MicronutrientContentRepository].
 */
@Singleton
class MediaContentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val allMedia: List<MediaItem> by lazy { load() }

    /** Contenus d'un module (« Bien dans ta tête » / « Bien dans ton corps »), dans l'ordre du fichier. */
    fun forModule(module: MediaModule): List<MediaItem> = allMedia.filter { it.module == module }

    private fun load(): List<MediaItem> = try {
        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        MediaJsonParser.parse(json).also { Timber.i("%d contenus média chargés depuis %s", it.size, ASSET_FILE) }
    } catch (exception: Exception) {
        Timber.e(exception, "Échec du chargement du catalogue média (%s)", ASSET_FILE)
        emptyList()
    }

    private companion object {
        const val ASSET_FILE = "media.json"
    }
}
