# FemSanté — Plateforme de Santé Féminine & Bien-être 🌸

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue.svg)](https://kotlinlang.org/)
[![Laravel](https://img.shields.io/badge/Laravel-11-red.svg)](https://laravel.com/)
[![PayPal](https://img.shields.io/badge/Payment-PayPal-003087.svg)](https://developer.paypal.com/)
[![License](https://img.shields.io/badge/License-Audrey_Retournay-green.svg)](https://www.audreyretournay-dieteticiennenutritionniste.com/)

FemSanté est une solution mobile et web complète conçue pour accompagner les femmes souffrant de pathologies spécifiques (Endométriose, SOPK, troubles digestifs). Ce projet allie une application **Android Native** performante à un **Backend Laravel** automatisé pour le suivi des symptômes et la gestion du contenu premium.

🤝 **En partenariat avec [Audrey Retournay](https://www.audreyretournay-dieteticiennenutritionniste.com/)**, diététicienne spécialisée en santé hormonale.

---

## 🛠️ Stack Technique

### Mobile (Android)

- **Langage** : Kotlin avec Coroutines & Flow pour la gestion asynchrone.
- **Architecture** : MVVM (Model-View-ViewModel) pour une séparation nette de la logique métier et de l'UI.
- **Persistance** : Room Database avec TypeConverters personnalisés pour le stockage des symptômes.
- **Multimédia** : Intégration d'ExoPlayer (Audio/Vidéo) et moteur de rendu PDF sécurisé.
- **UI/UX** : Navigation modulaire par Fragments, TabLayout dynamique et gestion des ressources via Assets.

### Backend (API & Admin)

- **Framework** : Laravel 11 (API Restful).
- **Paiement** : Flux de paiement natif PayPal (CreateOrder / CaptureOrder).
- **Automatisations** : Génération de factures PDF via DomPDF et notifications SMTP via Queues Laravel.
- **Sécurité** : Gestion des clés API en mode BYOK (Bring Your Own Key) et chiffrement des données sensibles.

---

## 📌 Fonctionnalités Clés

### 📅 Suivi des Symptômes & Calendrier

Un journal de bord intelligent permettant de cartographier ses douleurs et son état général :

- **Saisie granulaire** : Zones de douleur, humeur, niveau de stress et contexte.
- **Synchronisation locale** : Les données sont persistées en base Room pour une consultation rapide sans latence.

### 💳 Monétisation & Facturation Automatisée

Système professionnel de gestion des abonnements :

1. **Intention** : Génération d'un ticket proforma côté serveur.
2. **Transaction** : Capture du paiement sécurisé via le SDK PayPal.
3. **Validation** : Transformation du ticket en facture officielle et envoi automatique par email au format PDF.

### 🥗 Ressources & Multimédia

- **Nutrition** : Indexation dynamique des PDF de recettes via un scan automatique des dossiers `assets`.
- **Méditation & Sport** : Lecteur audio et vidéo permettant un basculement fluide entre les contenus sans interruption de l'interface.

---

## 📂 Architecture du Projet

### Android (Structure Modulaire)

```text
app/src/main/java/com/audreyRetournayDiet/femSante/
├── data/                    # Modèles de données et entités (UiState)
│   └── entities/            # Objets de données (VideoUiState, AppUser, etc.)
│
├── features/                # Logique métier organisée par modules (Domain)
│   ├── alim/                # Module Nutrition / Recettes
│   ├── calendar/            # Module Suivi de cycle et symptômes
│   ├── corps/               # Module Yoga et exercices physiques
│   ├── login/               # Gestion de l'authentification et paiement
│   ├── main/                # Accueil et menus principaux
│   └── tete/                # Module Sophrologie et Art-thérapie
│
├── repository/              # Gestion des sources de données (API & Local)
│   ├── local/               # Accès Room / SharedPreferences
│   └── remote/              # Appels API (VideoManager, etc.)
│
├── room/                    # Configuration de la base de données SQLite (Room)
│   ├── dao/                 # Data Access Objects
│   └── type/                # Convertisseurs et types personnalisés
│
├── shared/                  # Composants réutilisables par toute l'app
│   ├── adapters/            # Adaptateurs génériques (ex: NothingSelectedSpinnerAdapter)
│   ├── ui/                  # Composants graphiques communs (ex: LoadingAlert)
│   ├── viewers/             # Lecteurs de médias (AudioActivity, VideoActivity, PdfActivity)
│   └── utils/               # Classes utilitaires (Utilitaires, UserStore)
│
└── viewModels/              # ViewModels pilotant les vues (UiState)
    └── viewers/             # ViewModels spécifiques aux lecteurs de médias
```

### Backend (Laravel)

- `Services/Paypal/` : Services dédiés à la communication avec l'API PayPal.
- `Models/` : Modèles Eloquent (User, Invoice) avec conventions de nommage strictes.
- `Mail/` : Classes Mailables gérant les notifications de paiement.
- `resources/views/pdf/` : Templates Blade pour le rendu des factures.

---

## 📈 Roadmap & Évolutions

- [x] **Double Synchronisation Git** : Miroir automatique GitHub / GitLab.
- [x] **Système de Facturation** : Automatisation complète post-paiement.
- [ ] **Data Visualization** : Graphiques d'évolution des symptômes.
- [ ] **Déploiement iOS** : Migration de la logique métier vers Swift/SwiftUI.

---

## 📄 Licence & Crédits

Ce projet est la propriété intellectuelle d'**Audrey Retournay**. Toute utilisation du code ou des ressources sans autorisation est strictement interdite.
