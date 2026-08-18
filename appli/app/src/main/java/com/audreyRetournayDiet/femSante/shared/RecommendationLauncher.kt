package com.audreyRetournayDiet.femSante.shared

import android.content.Context
import android.content.Intent
import com.audreyRetournayDiet.femSante.data.recommendation.ContentRef
import com.audreyRetournayDiet.femSante.data.recommendation.ContentType
import com.audreyRetournayDiet.femSante.features.alim.AlimActivity
import com.audreyRetournayDiet.femSante.shared.viewers.AudioActivity
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity

/**
 * Ouvre un contenu recommandé en réutilisant les viewers existants, sans modifier leurs
 * internes. Les contrats de lancement diffèrent selon le type :
 *
 * - **PDF outil / ressource** : [PdfActivity] directement (fichier à la racine des assets).
 * - **Vidéo** : [Utilitaires.videoLaunch] (récupère l'URL via VideoManager, gère le 403).
 * - **Audio** : [AudioActivity] avec la **piste** recommandée (le lecteur joue un contenu unique).
 * - **PDF recette** : le tag cible une catégorie de repas, pas un fichier précis → on ouvre
 *   le hub Nutrition ([AlimActivity]) plutôt qu'un PDF unique.
 */
object RecommendationLauncher {

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
        context.startActivity(Intent(context, AudioActivity::class.java).apply {
            putExtra(AudioActivity.EXTRA_TITLE, trackTitle)
            putExtra(AudioActivity.EXTRA_TRACK, trackTitle)
        })
    }
}
