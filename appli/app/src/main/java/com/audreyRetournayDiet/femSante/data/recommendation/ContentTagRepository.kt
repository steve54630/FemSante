package com.audreyRetournayDiet.femSante.data.recommendation

import com.audreyRetournayDiet.femSante.data.recommendation.JournalTag.Activity
import com.audreyRetournayDiet.femSante.data.recommendation.JournalTag.Cause
import com.audreyRetournayDiet.femSante.data.recommendation.JournalTag.Digestif
import com.audreyRetournayDiet.femSante.data.recommendation.JournalTag.EmotionallementDifficile
import com.audreyRetournayDiet.femSante.data.recommendation.JournalTag.Quality
import com.audreyRetournayDiet.femSante.data.recommendation.JournalTag.Sos
import com.audreyRetournayDiet.femSante.data.recommendation.JournalTag.Zone
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.room.type.PainZone
import com.audreyRetournayDiet.femSante.room.type.PhysicalActivity

/**
 * Table de correspondance contenu <-> tags journal, **synchronisée** avec le tableau validé
 * `tagging_contenus.csv` (racine du repo). Les identifiants de contenu (titres/fichiers) sont
 * ceux réellement utilisés par l'app ; seuls les tags proviennent du CSV.
 *
 * Les zones de douleur taguées ici sont exploitées par [RecommendationEngine] **pondérées par
 * l'intensité par zone** saisie dans le journal (une zone forte pèse plus qu'une zone légère).
 * Le CSV contient désormais des colonnes pour toutes les zones (dont Seins, Tête, Bras,
 * Cuisses, Haut du dos, Jambes) ; celles-ci restent à taguer par la diététicienne.
 *
 * La colonne Premium du CSV n'est pas reprise ici (gérée côté abonnement).
 */
object ContentTagRepository {

    private fun pdf(id: String) = ContentRef(ContentType.PDF, id)
    private fun video(id: String) = ContentRef(ContentType.VIDEO, id)
    private fun audio(id: String) = ContentRef(ContentType.AUDIO, id)

    private fun zone(z: PainZone) = Zone(z)
    private fun cause(c: DifficultyCause) = Cause(c)
    private fun quality(q: DayQuality) = Quality(q)
    private fun activity(a: PhysicalActivity) = Activity(a)

    val tagsByContent: Map<ContentRef, Set<JournalTag>> = mapOf(
        // --- PDF - Boîte à outils ---
        pdf("automassage_ventre.pdf") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.ABDOMEN), Digestif, EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),
        pdf("bouillote.pdf") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.LOMBAIRES), zone(PainZone.ABDOMEN),
            EmotionallementDifficile, Sos, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS)
        ),
        pdf("douleurs_abdominales.pdf") to setOf(
            zone(PainZone.ABDOMEN), Digestif, EmotionallementDifficile, Sos,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),
        pdf("emotional_tempest.pdf") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), cause(DifficultyCause.TRISTESSE),
            EmotionallementDifficile, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS)
        ),
        pdf("emotional_tempest_oil.pdf") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), cause(DifficultyCause.TRISTESSE),
            EmotionallementDifficile, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS)
        ),
        pdf("infusion_digestion.pdf") to setOf(
            zone(PainZone.ABDOMEN), Digestif, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS), activity(PhysicalActivity.MARCHE)
        ),
        pdf("infusions_menstruations.pdf") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.ABDOMEN), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS), activity(PhysicalActivity.MARCHE)
        ),

        // --- PDF - Ressources documentaires ---
        pdf("histamine.pdf") to setOf(zone(PainZone.ABDOMEN), Digestif),
        pdf("gluten.pdf") to setOf(zone(PainZone.ABDOMEN), Digestif),
        pdf("ebook.pdf") to emptySet(),

        // --- PDF - Recettes (par catégorie de dossier) ---
        pdf("breakfast") to setOf(Digestif),
        pdf("entries") to setOf(Digestif),
        pdf("main_courses") to setOf(Digestif),
        pdf("desserts") to setOf(Digestif),

        // --- Vidéo - Yoga ---
        video("SOS Douleurs") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.LOMBAIRES), zone(PainZone.ABDOMEN),
            EmotionallementDifficile, Sos, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS)
        ),
        video("Calme intérieur") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile, quality(DayQuality.MOYENNE),
            activity(PhysicalActivity.REPOS), activity(PhysicalActivity.MARCHE)
        ),
        video("Débutant au Yoga") to setOf(
            quality(DayQuality.MOYENNE), activity(PhysicalActivity.MARCHE), activity(PhysicalActivity.SPORT)
        ),

        // --- Vidéo - Corps et Mouvement ---
        video("Pilates") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.LOMBAIRES), quality(DayQuality.MOYENNE),
            activity(PhysicalActivity.SPORT)
        ),
        video("Fitness") to setOf(activity(PhysicalActivity.SPORT)),

        // --- Vidéo - Sophrologie dynamique ---
        video("Pompage des épaules") to setOf(
            zone(PainZone.LOMBAIRES), cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS), activity(PhysicalActivity.MARCHE)
        ),
        video("Exercice du miroir") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.TRISTESSE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.MARCHE)
        ),
        video("Eventails") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.MARCHE)
        ),
        video("Soufflet thoracique") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS), activity(PhysicalActivity.MARCHE)
        ),

        // --- Vidéo - Art-thérapie ---
        video("Joie") to setOf(activity(PhysicalActivity.MARCHE), activity(PhysicalActivity.SPORT)),
        video("Tristesse") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.TRISTESSE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),
        video("Colère") to setOf(
            cause(DifficultyCause.COLERE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),
        video("Peur") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),

        // --- Vidéo - directe ---
        video("Intelligence émotionnelle") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), cause(DifficultyCause.TRISTESSE),
            EmotionallementDifficile, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.MARCHE)
        ),

        // --- Audio - Sophrologie profonde ---
        audio("Base vivantielle") to setOf(
            cause(DifficultyCause.STRESS), quality(DayQuality.MOYENNE), activity(PhysicalActivity.REPOS)
        ),
        audio("Déplacement du négatif") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), cause(DifficultyCause.TRISTESSE),
            EmotionallementDifficile, Sos, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS)
        ),

        // --- Audio - Hypnose ---
        audio("Auto hypnose pour le stress") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),
        audio("Auto-hypnose pour l'apaisement") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS), activity(PhysicalActivity.MARCHE)
        ),

        // --- Audio - Méditation ---
        audio("Calmer la colère") to setOf(
            cause(DifficultyCause.COLERE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),
        audio("Calmer la douleur") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.LOMBAIRES), zone(PainZone.ABDOMEN),
            EmotionallementDifficile, Sos, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS)
        ),
        audio("Confiance en soi") to setOf(
            cause(DifficultyCause.TRISTESSE), quality(DayQuality.MOYENNE),
            activity(PhysicalActivity.MARCHE), activity(PhysicalActivity.SPORT)
        ),
        audio("Relaxation") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS), activity(PhysicalActivity.MARCHE)
        )
    )
}
