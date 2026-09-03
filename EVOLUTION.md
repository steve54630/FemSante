# Évolution — FemSanté

Décisions prises sur les prochaines étapes du produit, pour ne pas les reperdre. Document vivant,
à mettre à jour au fil des discussions avec Audrey.

## Séquencement

**v1 (actuelle)** : sortie telle quelle, via la pré-inscription Google Play. On ne bloque pas le
lancement sur les pistes ci-dessous.

**v2** : les pistes listées ici, affinées et priorisées après discussion avec Audrey Retournay
(contenu clinique nécessaire pour la plupart d'entre elles).

## Principe directeur : pas d'IA

Choix délibéré. Cohérent avec l'argument confidentialité du produit (tout reste en local, RGPD
strict) — ajouter de l'IA impliquerait généralement d'envoyer des données à un service tiers, ce
qui contredirait ce positionnement. Les pistes ci-dessous sont donc pensées comme de la logique
déterministe simple (stats locales, questionnaires à score), jamais des appels à un modèle.

## Pistes v2

### 1. Corrélations / tendances entre symptômes
Faire ressortir des tendances (ex. « douleurs abdominales plus fréquentes en phase lutéale »),
calculées en local sur les données déjà journalisées (zones de douleur, causes, sommeil, humeur,
phase du cycle) — aucune saisie supplémentaire. Déjà dans l'ancienne roadmap du README (« Data
Visualization »).

**À valider avec Audrey** : quels croisements sont cliniquement pertinents (vs. trompeurs), seuil
minimum d'occurrences avant d'afficher un pattern, formulation « zéro anxiété » (jamais de lien de
causalité affirmé).

### 2. Questionnaire de dépistage / préparation de rendez-vous
Questionnaire à score fixe, intégré à l'export PDF médical existant, pour préparer une consultation
gynéco.

**À valider avec Audrey** : échelle clinique validée existante à adapter, ou questions sur-mesure ;
pondération et calcul du score ; formulation du résultat (jamais suggérer un diagnostic).

### 3. Fil de contenu éducatif
Articles courts rédigés par Audrey, publiés comme les recettes (fiches JSON structurées, pas de
génération automatique).

**À valider avec Audrey** : sujets prioritaires (5-10 pour démarrer), rythme de publication
soutenable pour elle.

## Écarté volontairement

**Principe** : FemSanté est une boîte à outils pour l'utilisatrice elle-même — pas un outil de mise
en relation avec d'autres personnes. Les fonctionnalités qui font entrer un tiers dans la logique du
produit (même sans risque de fuite de données) sortent de ce cadre.

**Communauté** — en tension directe avec le positionnement local-first/confidentialité. Les apps
concurrentes qui misent sur la communauté (Flo, MyEndometriosisTeam) sont aussi celles qui ont eu
des soucis de confidentialité (Flo Health : 59,5M$ de règlement en 2026 pour partage de données de
cycle avec Google/Meta/Flurry).

**Mode partenaire** — pas juste un problème d'architecture (aucune donnée ne sort jamais du
téléphone, l'export PDF existant est déjà générique et à sa discrétion) : c'est elle qui décide quoi
partager et avec qui, l'app n'a pas à présupposer une relation en particulier. Éventuellement une
évolution lointaine, mais pas dans le but premier de FemSanté.

## Repère marché (instantané, à rafraîchir si daté)

Recherche faite début septembre 2026. Concurrent direct le plus proche : **Endocare** (France,
endo/adéno/SOPK, freemium, essai 7 jours). Avantages actuels de FemSanté : contenu nutrition rédigé
par une vraie diététicienne + micronutriments avec interactions médicament↔nutriment (niveau
clinique non vu ailleurs), confidentialité 100% locale. À revérifier avant toute décision produit
importante — le marché bouge vite.
