# FemSanté — Plateforme de Santé Féminine & Bien-être 🌸

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg)](https://developer.android.com/)
[![Laravel](https://img.shields.io/badge/Laravel-11-red.svg)](https://laravel.com/)
[![PayPal](https://img.shields.io/badge/Payment-PayPal-003087.svg)](https://developer.paypal.com/)
[![License](https://img.shields.io/badge/License-Audrey_Retournay-green.svg)](https://www.audreyretournay-dieteticiennenutritionniste.com/)

FemSanté est une solution mobile et web complète conçue pour accompagner les femmes souffrant de pathologies spécifiques (Endométriose, SOPK, troubles digestifs). Ce projet allie une application **Android Native** performante à un **Backend Laravel** automatisé pour le suivi des symptômes et la gestion du contenu premium.

🤝 **En partenariat avec [Audrey Retournay](https://www.audreyretournay-dieteticiennenutritionniste.com/)**, diététicienne spécialisée en santé hormonale.

---

## Sommaire

- [Stack Technique](#-stack-technique)
- [Fonctionnalités Clés](#-fonctionnalités-clés)
- [Prérequis & Installation](#-prérequis--installation)
- [Architecture du Projet](#-architecture-du-projet)
- [Sécurité & RGPD](#-sécurité--rgpd)
- [Roadmap & Évolutions](#-roadmap--évolutions)
- [Licence & Crédits](#-licence--crédits)

---

## 🛠️ Stack Technique

### Mobile (Android)

- **Langage** : Kotlin avec Coroutines & Flow pour la gestion asynchrone.
- **Architecture** : MVVM (Model-View-ViewModel) pour une séparation nette de la logique métier et de l'UI.
- **Injection de dépendances** : Hilt (Dagger) — ViewModels injectés (`@HiltViewModel`), modules centralisant la fourniture des repositories, DAOs et du `UserStore`.
- **Persistance** : Room Database avec TypeConverters personnalisés, **chiffrée au repos via SQLCipher** (données de santé locales, conformité RGPD).
- **Multimédia** : Intégration d'ExoPlayer (Audio/Vidéo) et moteur de rendu PDF sécurisé.
- **UI/UX** : Navigation modulaire par Fragments, TabLayout dynamique et gestion des ressources via Assets.
- **Tests** : batterie de tests unitaires JVM (logique pure, repositories via faux DAO, ViewModels via MockK + coroutines-test).

### Backend (API & Admin)

- **Framework** : Laravel 11 (API Restful).
- **Paiement** : Flux de paiement natif PayPal (CreateOrder / CaptureOrder).
- **Automatisations** : Génération de factures PDF via DomPDF et notifications SMTP via Queues Laravel.
- **Sécurité** : Gestion des clés API en mode BYOK (Bring Your Own Key) et chiffrement des données sensibles.

---

## 📌 Fonctionnalités Clés

### Suivi des Symptômes & Calendrier

Un journal de bord intelligent permettant de cartographier ses douleurs et son état général :

- **Saisie granulaire** : Zones de douleur (dont abdomen), humeur, niveau de stress, transit (échelle de Bristol) et contexte.
- **Synchronisation locale** : Les données sont persistées en base Room (chiffrée) pour une consultation rapide sans latence.

### Suivi du Cycle Menstruel

Pensé pour un public endométriose / SOPK, avec une règle directrice : **zéro anxiété**.

- **Saisie quotidienne** : règles, abondance du flux, spotting — directement dans le calendrier.
- **Profil de cycle** (régulier / irrégulier / absent ou pilule) qui **conditionne tout l'affichage** : estimation de phase pour les profils exploitables, **jamais de compte à rebours** pour les cycles irréguliers, cadran masqué pour les profils sans cycle.
- **Estimation de phase** (menstruelle / folliculaire / ovulation / lutéale) calculée localement depuis l'historique, sans prédiction hasardeuse.

### Recommandations contextuelles « Pour toi aujourd'hui »

Le journal pilote des suggestions de contenu, **sans jamais bloquer l'accès au reste de l'app** :

- **Moteur de tags** réutilisant les enums du journal (zones, causes, qualité du jour…) — pas de taxonomie parallèle.
- **Logique métier validée avec la diététicienne** : mode « SOS » au-delà d'un seuil de douleur, tri par pertinence, repli neutre sans saisie.
- **Carrousel** sur l'accueil renvoyant vers les lecteurs existants (PDF / vidéo / audio).

### Monétisation & Facturation Automatisée

Système professionnel de gestion des abonnements :

1. **Intention** : Génération d'un ticket proforma côté serveur.
2. **Transaction** : Capture du paiement sécurisé via le SDK PayPal.
3. **Validation** : Transformation du ticket en facture officielle et envoi automatique par email au format PDF.

### Ressources & Multimédia

- **Nutrition** : Indexation dynamique des PDF de recettes via un scan automatique des dossiers `assets`.
- **Méditation & Sport** : Lecteur audio et vidéo permettant un basculement fluide entre les contenus sans interruption de l'interface.

---

## 🚀 Prérequis & Installation

- **Android Studio** récent, avec **JDK 17+** pour Gradle et **Android SDK 36** installé.
- Accès au dépôt privé Maven Cardinal/PayPal (identifiants fournis par le responsable projet).
- Le **backend Laravel** (`femsante-api`, dépôt séparé) déployé et accessible pour l'authentification, le paiement et le streaming média.

```bash
cd appli/

# Copier le modèle de configuration locale et renseigner les valeurs réelles
cp local.properties.example local.properties
# → sdk.dir, API_URL, PAYPAL_CLIENT_ID, RETURN_URL_CARD, RETURN_URL_PAYPAL,
#   JFROG_USER / JFROG_PASSWORD (dépôt privé, ou variables d'environnement équivalentes)

# Build debug en ligne de commande (ou ouvrir appli/ dans Android Studio)
./gradlew :app:assembleDebug
```

> `local.properties`, le `KeyStore/` (signature release) et les identifiants Maven privés ne sont **jamais versionnés** — voir `local.properties.example` pour le détail des clés attendues.

Les tests unitaires (logique métier pure : filtres, parseurs, ViewModels) se lancent depuis Android Studio ou `./gradlew test`.

---

## 📂 Architecture du Projet

### Android (Structure Modulaire)

```text
appli/app/src/main/java/com/audreyRetournayDiet/femSante/
├── data/                    # Modèles de données et logique métier pure
│   ├── cycle/               # Calcul de phase du cycle (CyclePhaseCalculator)
│   ├── entities/            # Objets de données (VideoUiState, AppUser, etc.)
│   ├── fodmap/              # Module régime FODMAP
│   ├── media/               # Catalogue vidéos/audios bien-être
│   ├── micronutrient/       # Fiches micronutriments + interactions médicamenteuses
│   ├── recipe/              # Recettes natives
│   ├── recommendation/      # Moteur de recommandation + tags (RecommendationEngine)
│   ├── report/              # Export du rapport médical (PDF)
│   ├── resource/            # Ressources statiques diverses
│   └── toolbox/             # Utilitaires partagés côté data
│
├── di/                      # Modules Hilt (injection de dépendances)
│
├── features/                # Logique métier organisée par modules (Domain)
│   ├── alim/                # Module Nutrition / Recettes
│   ├── calendar/            # Module Suivi de cycle et symptômes
│   ├── corps/               # Module Yoga et exercices physiques
│   ├── login/               # Gestion de l'authentification et paiement
│   ├── main/                # Accueil, menus, section « Pour toi aujourd'hui »
│   ├── tete/                # Module Sophrologie et Art-thérapie
│   └── toolbox/             # Écrans transverses (outils, réglages…)
│
├── repository/              # Gestion des sources de données (API & Local)
│   ├── local/               # Accès Room (DailyRepository, CycleRepository)
│   └── remote/              # Appels API (VideoManager, etc.)
│
├── room/                    # Configuration de la base de données SQLite (Room)
│   ├── converter/           # TypeConverters (PainZone, Bristol, Flow…)
│   ├── dao/                 # Data Access Objects
│   ├── database/            # AppDatabase + DatabaseProvider (SQLCipher)
│   ├── dto/                 # Objets de transfert entre couches
│   ├── entity/              # Entités (DailyEntry, CycleDay…)
│   ├── migration/           # Migrations Room versionnées (1→2, 2→3…)
│   └── type/                # Enums métier (PainZone, CycleProfile…)
│
├── shared/                  # Composants réutilisables (viewers, UserStore, clés DB…)
│   └── viewers/             # Lecteurs de médias (AudioActivity, VideoActivity, PdfActivity)
│
└── viewmodels/              # ViewModels pilotant les vues (UiState), par module
    ├── alim/
    ├── calendar/
    ├── login/
    ├── main/
    └── viewers/

appli/app/src/test/           # Tests unitaires JVM (converters, repo, ViewModels, logique)
```

### Backend (Laravel)

- `Services/Paypal/` : Services dédiés à la communication avec l'API PayPal.
- `Models/` : Modèles Eloquent (User, Invoice) avec conventions de nommage strictes.
- `Mail/` : Classes Mailables gérant les notifications de paiement.
- `resources/views/pdf/` : Templates Blade pour le rendu des factures.

---

## 🔒 Sécurité & RGPD

- **Données de santé strictement locales** : aucune donnée médicale (journal, cycle) n'est envoyée au backend.
- **Chiffrement au repos** : base Room chiffrée via SQLCipher ; la passphrase est générée aléatoirement et stockée dans l'Android Keystore (jamais en dur).
- **Droit à l'effacement** : suppression de la base prévue lors de la suppression de compte.
- **Configuration sensible** : les secrets (tokens API, identifiants de dépôt privé) doivent être fournis via `local.properties` (non versionné) et exposés par `BuildConfig` — voir `local.properties.example`.

## 📈 Roadmap & Évolutions

- [x] **Double Synchronisation Git** : Miroir automatique GitHub / GitLab.
- [x] **Système de Facturation** : Automatisation complète post-paiement.
- [x] **Recommandations contextuelles** : contenu suggéré selon le journal du jour.
- [x] **Suivi de cycle menstruel** : saisie, profil et estimation de phase (zéro anxiété).
- [x] **Chiffrement local (RGPD)** : base Room protégée par SQLCipher.
- [x] **Injection de dépendances** : migration complète vers Hilt.
- [x] **Tests unitaires** : couverture de la logique métier (reco, cycle, converters).
- [ ] **Prédictions de cycle** : anticipation des prochaines règles (profils réguliers).
- [ ] **Data Visualization** : Graphiques d'évolution des symptômes.
- [ ] **CI** : exécution automatique des tests à chaque PR.
- [ ] **Déploiement iOS** : Migration de la logique métier vers Swift/SwiftUI.

---

## 📄 Licence & Crédits

Ce projet est la propriété intellectuelle d'**Audrey Retournay**. Toute utilisation du code ou des ressources sans autorisation est strictement interdite.
