package com.audreyRetournayDiet.femSante.data.recommendation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fournit la table de correspondance **contenu ↔ tags journal**, lue une seule fois depuis
 * `assets/content_tags.json` via [ContentTagJsonParser].
 *
 * La table (identifiants de contenu + tags issus du tableau de tagging validé par la
 * diététicienne) reste optionnelle : catalogue vide → [RecommendationEngine] laisse tout le
 * contenu affiché, et la source pourra passer côté API sans toucher au moteur.
 *
 * Injecté par constructeur (`@Inject`) et unique (`@Singleton`) : le JSON n'est parsé qu'à la
 * première utilisation grâce au chargement paresseux.
 */
@Singleton
class ContentTagRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val catalog: Map<ContentRef, Set<JournalTag>> by lazy { load() }

    /** Retourne la table contenu ↔ tags (vide si le chargement a échoué). */
    fun tagsByContent(): Map<ContentRef, Set<JournalTag>> = catalog

    private fun load(): Map<ContentRef, Set<JournalTag>> = try {
        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        ContentTagJsonParser.parse(json).also {
            Timber.i("%d correspondances contenu↔tags chargées depuis %s", it.size, ASSET_FILE)
        }
    } catch (exception: Exception) {
        Timber.e(exception, "Échec du chargement de la table de tags (%s)", ASSET_FILE)
        emptyMap()
    }

    private companion object {
        const val ASSET_FILE = "content_tags.json"
    }
}
