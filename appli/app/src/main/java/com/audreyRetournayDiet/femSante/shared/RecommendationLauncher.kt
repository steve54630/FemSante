package com.audreyRetournayDiet.femSante.shared

import android.content.Context
import android.content.Intent
import com.audreyRetournayDiet.femSante.data.recommendation.ContentRef
import com.audreyRetournayDiet.femSante.data.recommendation.ContentType
import com.audreyRetournayDiet.femSante.features.alim.AlimActivity
import com.audreyRetournayDiet.femSante.shared.viewers.AudioActivity
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity
import timber.log.Timber

/**
 * Ouvre un contenu recommandé en réutilisant les viewers existants, sans modifier leurs
 * internes. Les contrats de lancement diffèrent selon le type :
 *
 * - **PDF outil / ressource** : [PdfActivity] directement (fichier à la racine des assets).
 * - **Vidéo** : [Utilitaires.videoLaunch] (récupère l'URL via VideoManager, gère le 403).
 * - **Audio** : [AudioActivity] avec la **playlist parente** (le viewer attend une playlist
 *   + un titre, pas un item isolé).
 * - **PDF recette** : le tag cible une catégorie de repas, pas un fichier précis → on ouvre
 *   le hub Nutrition ([AlimActivity]) plutôt qu'un PDF unique.
 */
object RecommendationLauncher {

    /**
     * Playlists audio reproduites depuis SophroActivity / BienTeteActivity (données déjà
     * codées en dur dans l'app). Centralisées ici pour pouvoir ouvrir la playlist parente
     * d'une piste recommandée sans toucher à ces écrans.
     */
    private val audioPlaylists: List<Pair<String, List<String>>> = listOf(
        "Sophronisations" to listOf("Base vivantielle", "Déplacement du négatif"),
        "Hypnose" to listOf("Auto hypnose pour le stress", "Auto-hypnose pour l'apaisement"),
        "Méditations" to listOf("Calmer la colère", "Calmer la douleur", "Confiance en soi", "Relaxation")
    )

    fun launch(context: Context, content: ContentRef) {
        when (content.type) {
            ContentType.PDF -> launchPdf(context, content.id)
            ContentType.VIDEO -> Utilitaires.videoLaunch(
                content.id, "non", Intent(context, VideoActivity::class.java), context
            )
            ContentType.AUDIO -> launchAudio(context, content.id)
        }
    }

    private fun launchPdf(context: Context, id: String) {
        if (id.endsWith(".pdf")) {
            context.startActivity(Intent(context, PdfActivity::class.java).apply {
                putExtra("PDF", id)
            })
        } else {
            // Recette : on ne dispose que de la catégorie -> hub Nutrition.
            context.startActivity(Intent(context, AlimActivity::class.java))
        }
    }

    private fun launchAudio(context: Context, trackTitle: String) {
        val playlist = audioPlaylists.firstOrNull { (_, tracks) -> trackTitle in tracks }
        if (playlist != null) {
            context.startActivity(Intent(context, AudioActivity::class.java).apply {
                putExtra("map", ArrayList(playlist.second))
                putExtra("Titre", playlist.first)
            })
        } else {
            Timber.w("Aucune playlist parente trouvée pour la piste audio : $trackTitle")
        }
    }
}
