# 📋 Cahier des Charges : Projet FemSante

## 1. Présentation de l'Entreprise
**Audrey Retournay Diet** est une structure spécialisée dans l'accompagnement nutritionnel et le bien-être féminin. Forte d'une expertise en diététique et en santé hormonale, l'entreprise souhaite offrir un outil numérique permettant aux patientes de devenir actrices de leur santé entre deux consultations. L'approche est centrée sur l'écoute du corps et la compréhension des cycles.

---

## 2. Objectif de l'Application Mobile
L'application **FemSante** a pour mission de digitaliser le "journal de bord" papier souvent utilisé en santé féminine.
* **Auto-suivi :** Permettre une saisie rapide et quotidienne des données de santé.
* **Analyse :** Identifier des corrélations entre l'hygiène de vie (contexte) et les symptômes physiques/psychologiques.
* **Aide au diagnostic :** Fournir une base de données fiable et précise à présenter aux professionnels de santé (gynécologues, nutritionnistes, endocrinologues).

---

## 3. La Cible du Projet
* **Cible Principale :** Femmes actives (18-45 ans) attentives à leur équilibre hormonal ou souffrant de pathologies chroniques (Endométriose, SOPK, SPM sévère).
* **Cible Secondaire :** Professionnels de santé qui utilisent les données récoltées pour affiner leurs protocoles de soin.

---

## 4. Périmètre du Projet
Le projet initial se concentre sur une application **Android Native** (Kotlin).
* **Inclus :** Authentification, calendrier interactif, formulaire de saisie multi-étapes, stockage local sécurisé (Room).
* **Exclu (Phase 1) :** Version iOS, synchronisation cloud multi-appareils, forum communautaire, conseils nutritionnels automatisés par IA.

---

## 5. Charte Graphique
L'identité visuelle doit inspirer la sérénité, la confiance et la clarté...
* **Typographie :** Polices sans-serif (ex: *Montserrat* ou *Open Sans*) pour un aspect moderne et professionnel.
* **Iconographie :** Pictogrammes doux et arrondis pour illustrer les symptômes sans aspect "médical" anxiogène.

---

## 6. Description du Parcours Utilisateur
1. **Onboarding :** L'utilisatrice crée son compte et renseigne son profil de base.
2. **Accueil (Le Calendrier) :** Vue mensuelle affichant des pastilles de couleur (intensité de douleur).
3. **Saisie (Le Stepper) :** * L'utilisatrice clique sur un jour.
    * Elle remplit 4 sections : **Général** (Douleur/Énergie) > **Symptômes** > **Psychologie** > **Contexte**.
4. **Consultation :** En cliquant sur un jour passé, elle visualise le résumé de ses saisies et peut les éditer si besoin.



---

## 7. Spécificités Techniques et Fonctionnelles

### Spécificités Fonctionnelles
* **Gestion CRUD complète :** Créer, Lire, Mettre à jour et Supprimer des entrées journalières.
* **Système de "REPLACE" intelligent :** Mise à jour des sous-tables sans perte d'intégrité via des transactions SQL.
* **Mode hors-ligne :** Accès permanent aux données même sans réseau.

### Export et Collaboration Médicale
#### Fonctionnalité : Rapport d'Expertise Local (PDF)
* **Objectif :** Transformer les données du calendrier en un document de synthèse structuré pour faciliter la consultation médicale ou diététique.
* **Mode de génération :** 100% Local (On-device generation). Aucune donnée n'est envoyée vers un serveur tiers.
* **Contenu du rapport :**
    * **En-tête :** Identité (Prénom/Nom), période du rapport (ex: "Février 2026").
    * **Vue Calendrier :** Grille mensuelle simplifiée reprenant les indicateurs visuels (Orange pour la chaleur/inflammation).
    * **Tableau de corrélations :** Liste automatique des jours où la douleur a été > 7 ou la chaleur présente, avec le contexte alimentaire associé.
    * **Graphiques d'évolution :** Courbe de fatigue et de stress sur la période.
* **Technique :** * API `android.graphics.pdf.PdfDocument`.
    * Partage via `Intent.ACTION_VIEW` et `FileProvider`.

### Spécificités Techniques
* **Langage :** Kotlin 1.9+.
* **Architecture :** MVVM (Model-View-ViewModel).
* **Persistance :** Room Database avec gestion des relations 1:1 et 1:N.
* **Injection :** Factory Pattern pour les ViewModels.
* **Sécurité :** Stockage des préférences utilisateur via `EncryptedSharedPreferences`.



---

## 8. Prestations Attendues
* **Développement Mobile :** Codage de l'interface et de la logique métier sous Android Studio.
* **Tests QA :** Vérification de la non-régression (notamment sur l'édition des données).
* **Design UI/UX :** Création des maquettes des fragments et du calendrier.
* **Documentation :** Livraison d'un guide technique sur la structure de la base de données.

---

## 9. Planning et Délais
Le projet est découpé en cycles de 2 semaines (Sprints) :
* **Semaine 1-2 :** Mise en place de l'architecture Room et de l'authentification.
* **Semaine 3-4 :** Développement du calendrier et de la logique de sélection de date.
* **Semaine 5-6 :** Création du formulaire multi-étapes (Fragments) et logique de sauvegarde/édition.
* **Semaine 7 :** Tests de charge, debug et polissage UI.
* **Semaine 8 :** Préparation au déploiement (Alpha test).