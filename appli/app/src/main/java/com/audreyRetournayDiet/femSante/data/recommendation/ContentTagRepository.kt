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
 * Table de correspondance contenu <-> tags journal, transcrite du tableau validé avec
 * la diététicienne (`tagging_contenus.csv` à la racine du repo). La colonne Premium du
 * CSV n'est volontairement pas reprise ici (ignorée pour les tests, cf. discussion).
 *
 * Stockage statique en code plutôt qu'en table Room : les trois catalogues de contenu
 * (PDF, vidéo, audio) sont déjà entièrement codés en dur ailleurs dans l'app — on garde
 * la même cohérence ici. À migrer vers une table si un besoin de curation à distance
 * apparaît plus tard.
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
            zone(PainZone.BASSIN), zone(PainZone.ABDOMEN), Digestif,
            EmotionallementDifficile, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        pdf("bouillote.pdf") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.LOMBAIRES), zone(PainZone.ABDOMEN),
            EmotionallementDifficile, Sos, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        pdf("douleurs_abdominales.pdf") to setOf(
            zone(PainZone.ABDOMEN), Digestif, EmotionallementDifficile, Sos,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        pdf("emotional_tempest.pdf") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), cause(DifficultyCause.TRISTESSE),
            EmotionallementDifficile, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        pdf("emotional_tempest_oil.pdf") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), cause(DifficultyCause.TRISTESSE),
            EmotionallementDifficile, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        pdf("infusion_digestion.pdf") to setOf(
            zone(PainZone.ABDOMEN), Digestif, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE),
            activity(PhysicalActivity.REPOS)
        ),
        pdf("infusions_menstruations.pdf") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.ABDOMEN), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
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
            EmotionallementDifficile, Sos, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        video("Calme intérieur") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),
        video("Débutant au Yoga") to setOf(
            quality(DayQuality.MOYENNE), activity(PhysicalActivity.REPOS), activity(PhysicalActivity.MARCHE)
        ),

        // --- Vidéo - Corps et Mouvement ---
        video("Pilates") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.LOMBAIRES), quality(DayQuality.MOYENNE),
            activity(PhysicalActivity.SPORT)
        ),
        video("Fitness") to setOf(activity(PhysicalActivity.SPORT)),

        // --- Vidéo - Sophrologie dynamique ---
        video("Épaules") to setOf(
            zone(PainZone.LOMBAIRES), cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),
        video("Miroir") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.TRISTESSE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), activity(PhysicalActivity.REPOS)
        ),
        video("Éventails") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), activity(PhysicalActivity.REPOS)
        ),
        video("Respiration thoracique") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),

        // --- Vidéo - Art-thérapie ---
        video("Joie") to setOf(activity(PhysicalActivity.REPOS), activity(PhysicalActivity.MARCHE)),
        video("Tristesse") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.TRISTESSE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        video("Colère") to setOf(
            cause(DifficultyCause.COLERE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        video("Peur") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),

        // --- Vidéo - directe ---
        video("Gestion des émotions") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), cause(DifficultyCause.TRISTESSE),
            EmotionallementDifficile, quality(DayQuality.MOYENNE), activity(PhysicalActivity.REPOS)
        ),

        // --- Audio - Sophrologie profonde ---
        audio("Base vivantielle") to setOf(cause(DifficultyCause.STRESS), quality(DayQuality.MAUVAISE)),
        audio("Déplacement du négatif") to setOf(
            cause(DifficultyCause.STRESS), cause(DifficultyCause.COLERE), cause(DifficultyCause.TRISTESSE),
            EmotionallementDifficile, Sos, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),

        // --- Audio - Hypnose ---
        audio("Auto hypnose pour le stress") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        audio("Auto-hypnose pour l'apaisement") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        ),

        // --- Audio - Méditation ---
        audio("Calmer la colère") to setOf(
            cause(DifficultyCause.COLERE), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        audio("Calmer la douleur") to setOf(
            zone(PainZone.BASSIN), zone(PainZone.LOMBAIRES), zone(PainZone.ABDOMEN),
            EmotionallementDifficile, Sos, quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE)
        ),
        audio("Confiance en soi") to setOf(
            cause(DifficultyCause.TRISTESSE), activity(PhysicalActivity.MARCHE), activity(PhysicalActivity.SPORT)
        ),
        audio("Relaxation") to setOf(
            cause(DifficultyCause.STRESS), EmotionallementDifficile,
            quality(DayQuality.MOYENNE), quality(DayQuality.MAUVAISE), activity(PhysicalActivity.REPOS)
        )
    )
}
