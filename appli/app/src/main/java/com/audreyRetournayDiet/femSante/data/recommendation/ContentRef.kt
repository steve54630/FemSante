package com.audreyRetournayDiet.femSante.data.recommendation

/**
 * Identité unifiée d'un contenu de la bibliothèque, utilisée uniquement par le moteur
 * de recommandation pour associer des tags.
 *
 * Les catalogues existants ont chacun leur propre identité technique (nom de fichier PDF,
 * titre de vidéo, titre de piste audio, id de fiche outil) et leurs propres viewers
 * ([com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity],
 * [com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity],
 * [com.audreyRetournayDiet.femSante.shared.viewers.AudioActivity],
 * [com.audreyRetournayDiet.femSante.features.toolbox.ToolboxFileDetailActivity]) —
 * [ContentRef] ne remplace rien de tout cela, il sert uniquement de clé dans la table de
 * tagging.
 *
 * @param id Identifiant tel qu'utilisé aujourd'hui par le contenu (nom de fichier PDF,
 * titre de vidéo, titre de piste audio, [ToolboxAdvice.id][com.audreyRetournayDiet.femSante.data.toolbox.ToolboxAdvice])
 * — pas un nouvel identifiant inventé.
 */
data class ContentRef(val type: ContentType, val id: String)

enum class ContentType {
    PDF,
    VIDEO,
    AUDIO,
    TOOLBOX
}
