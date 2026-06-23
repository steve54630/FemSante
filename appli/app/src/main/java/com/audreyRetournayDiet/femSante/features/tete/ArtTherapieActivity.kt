package com.audreyRetournayDiet.femSante.features.tete

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.Utilitaires.videoLaunch
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity
import timber.log.Timber

/**
 * Activité dédiée à l'Art-Thérapie dans le module "Bien dans sa tête".
 *
 * Cette interface permet à l'utilisatrice de choisir une émotion spécifique
 * pour accéder à un contenu thérapeutique vidéo adapté.
 *
 * ### Fonctionnement :
 * - Propose 4 émotions : Joie, Tristesse, Colère, Peur.
 * - Utilise la méthode utilitaire [videoLaunch] pour préparer l'Intent et
 * lancer la [VideoActivity].
 */
class ArtTherapieActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_art_therapie)
        Timber.d("onCreate: Chargement de l'écran Art-Thérapie")

        setupListeners()
    }

    /**
     * Configure les écouteurs de clics pour les boutons d'émotions.
     * Utilise un mapping (ID de ressource -> Titre) pour factoriser la logique de lancement.
     */
    private fun setupListeners() {
        // Préparation de l'Intent de destination
        val intentVideo = Intent(this, VideoActivity::class.java)

        // Mapping des IDs de ressources vers les titres de vidéos correspondants
        val emotionConfig = mapOf(
            R.id.buttonJoy to "Joie",
            R.id.buttonSadness to "Tristesse",
            R.id.buttonAnger to "Colère",
            R.id.buttonFear to "Peur"
        )

        // Itération sur la map pour attacher les listeners
        emotionConfig.forEach { (resId, title) ->
            findViewById<Button>(resId).setOnClickListener {
                Timber.i("Action: Lancement vidéo Art-Thérapie -> ${title.uppercase()}")

                // Appel de la fonction utilitaire partagée
                // "oui" indique probablement un paramètre spécifique au lecteur vidéo (ex: plein écran ou loop)
                videoLaunch(title, "oui", intentVideo, this)
            }
        }
    }
}