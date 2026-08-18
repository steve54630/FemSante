# Note de Cadrage Fonctionnel & Métier — Application Mobile

**Destinataire :** Audrey  
**Auteur :** Développeur  
**Objet :** Évolution UX/UI (Espace « Pour toi »), Digitalisation des Recettes & Corrélation Symptômes/Contenus  
**Version :** 1.2 — Document de travail & d'arbitrage  

---

## 1. Refonte de l'Espace « Pour toi » : Structuration par Domaines

Afin d'offrir une navigation plus claire et d'améliorer l'expérience utilisateur, l'onglet **« Pour toi »** évolue d'un flux de contenus génériques vers **4 domaines structurés** :

| Domaine | Proposition de valeur utilisateur | Algorithme & Source |
| :--- | :--- | :--- |
| 🍽️ **Ta recette du jour** | Recette personnalisée selon la phase du cycle et les symptômes enregistrés. | Matrice de taggage nutritionnel (§2) |
| 🌿 **Soulager ta douleur** | Automassages, phytothérapie et exercices ciblés sur les zones douloureuses. | Cartographie des douleurs (§3) |
| 🧘 **Bouger en douceur** | Séances de yoga, Pilates et mobilité adaptées au niveau d'énergie. | Indexation par intensité physique |
| 💜 **Apaiser le mental** | Modules de sophrologie, méditation et hypnose personnalisés. | Indexation par état émotionnel |

> **Règle de gestion prioritaire (Algorithme de douleur) :**  
> L'affichage des contenus priorise en temps réel les réponses aux zones anatomiques présentant le niveau de douleur le plus élevé déclaré par l'utilisatrice.

### ❓ Arbitrage n°1
*Valides-tu la nomenclature de ces 4 domaines ? Souhaites-tu ajuster un intitulé ou intégrer une thématique complémentaire ?*

---

## 2. Digitalisation et Structuration du Catalogue de Recettes

### 2.1. Déploiement des fiches recettes natives
Pour remplacer la consultation de fichiers PDF figés, j'ai développé une **interface native dédiée** :
* **Format dynamique :** En-tête thématique, métadonnées (temps, portions), sectorisation des ingrédients (*ex. « Pour la garniture »*) et étapes pas-à-pas.
* **Mise en valeur des conseils :** Encadrés d'expertise nutritionnelle intégrés.
* **Accessibilité :** Lien direct pour télécharger/imprimer la version PDF originale.

### 2.2. Automatisation des données & Fichiers CSV
J'ai intégralement extrait et structuré les données de tes **20 fiches recettes PDF** dans deux fichiers CSV opérationnels :

1. **`Recettes.csv` (Référentiel maîtres)**  
   * Informations descriptives renseignées à 100 %.
   * *À compléter par Audrey :*
     * **Phase hormonale :** `Menstruelle`, `Folliculaire`, `Ovulatoire`, `Lutéale` (ou `Toutes`).
     * **Tags fonctionnels :** `Sans gluten`, `Anti-inflammatoire`, `Basse histamine`, `Express`, etc.

2. **`Ingredients.csv` (Moteur de liste de courses)**  
   * Contient ~160 lignes d'ingrédients catégorisés par rayon d'achat (*Fruits & Légumes, Épicerie sèche, Produits frais, Condiments, etc.*) pour générer la liste de courses automatique.

### ❓ Arbitrage n°2
*Le vocabulaire proposé pour les phases hormonales et les tags nutritionnels te semble-t-il exhaustif et adapté à la pratique ?*

### ⚠️ Points d'attention — Corrections requises sur 3 recettes
Lors de l'extraction, 3 écarts ont été identifiés dans les PDF source :
1. **Tofu fumé, purée de carottes et panais :** Le titre mentionne le *panais*, mais la liste des ingrédients indique des *navets*. Quel ingrédient retenir ?
2. **Blinis avocat - saumon :** Les *œufs* (jaune + blancs en neige) sont dans la préparation mais absents de la liste d'ingrédients. Quelle quantité exacte ?
3. **Cake au chocolat léger :** Les *œufs* sont mentionnés dans les étapes mais pas dans les ingrédients. Quelle quantité intégrer ?

### ❓ Arbitrage n°3
*Peux-tu me confirmer les corrections à apporter à ces 3 fiches recettes ?*

---

## 3. Corrélation Symptômes & Contenus : Extension de la Cartographie

Le module de suivi de la douleur évolue vers une **localisation anatomique précise** couplée à une échelle d'intensité.

Nouveau référentiel de zones cibles à couvrir :
* **Zones existantes :** Abdomen, Bassin, Lombaires, etc.
* **Nouvelles zones :** `Seins`, `Tête`, `Bras`, `Cuisses`, `Haut du dos`, `Jambes`.

### ❓ Arbitrage n°4
*Quels contenus (existants ou à concevoir) préconises-tu pour cibler ces 6 nouvelles zones (ex. types de tisanes, étirements spécifiques, auto-massages) ?*

---

## 4. Feuille de Route & Responsabilités

| Projet / Action | Responsable | Statut |
| :--- | :--- | :--- |
| Développement de la cartographie des douleurs & fiches natives | Développeur | **Terminé** |
| Validation des 4 questions d'arbitrage métier | Audrey | **En attente** |
| Saisie des phases/tags dans `Recettes.csv` et revue de `Ingredients.csv` | Audrey | **En attente** |
| Câblage du filtrage dynamique par phase et module Liste de courses | Développeur | **À venir** |