# CLAUDE.md — FemSanté

Guidance for Claude Code (and any developer) working in this repository.

## What this project is

**FemSanté** is a native **Android** app for women's health (endometriosis / PCOS "SOPK" /
digestive troubles), built with the dietician **Audrey Retournay**. It lets users:

- **Track a daily journal** (body pain map, mood, difficulty causes, sleep, gratitude, physical
  activity, medication, diet) and their **menstrual cycle** (real + predicted period days, phase).
- Browse **native recipe fiches**, build a **shopping list** (grouped by aisle, with per-recipe
  multipliers), and read a **micronutrients** module (fiches + drug↔nutrient interactions).
- Watch/listen to **well-being content** ("Bien dans ta tête" videos/audios, "Bien dans ton corps"
  yoga/pilates/fitness) via secure streaming.
- Export a **medical report** (PDF) of their tracked data.

The UI language is **French**. The visual identity is a warm "soleil / joie" (sun / joy) palette.

## Tech stack

- **Kotlin**, **Android Views + XML layouts** (`findViewById`). **Not Jetpack Compose** — do not
  introduce Compose without an explicit, scoped migration decision.
- **MVVM**: Activity/Fragment → `@HiltViewModel` → Repository → Room / DataStore / remote API.
- **Hilt** 2.52 (DI), **Room** 2.6 + **SQLCipher** (encrypted local DB), Coroutines / Flow /
  StateFlow, **Material 3**, **media3 ExoPlayer** 1.2 (HLS streaming), Volley (legacy network),
  Gson, DataStore Preferences, EncryptedSharedPreferences.
- Build: **Gradle** (Groovy DSL), KSP. `compileSdk 36`, `minSdk 26`, `targetSdk 35`.
- Code package (`namespace`): `com.audreyRetournayDiet.femSante`
  (note the `applicationId` differs: `com.audreyretournaydiet.femsanteapp`).

## Project layout

```
FemSante/                     ← repo root (git; branch: develop, main: master)
├── appli/                    ← the Android project
│   ├── app/                  ← the app module (:app)
│   ├── local.properties      ← SECRETS, not committed (see local.properties.example)
│   └── build.gradle, settings.gradle, gradlew…
├── api/                      ← legacy PHP endpoints (historical)
├── docs/                     ← business docs, tagging tables (for Audrey)
├── KeyStore                  ← release signing (keep secret, do not commit)
└── README.md, CLAUDE.md
```

The **Laravel backend is a separate project** (not in this repo — e.g. `femsante-api/`). It issues
Sanctum tokens, resolves signed HLS media URLs, and manages subscriptions (`VALID_DATE`, and the
freemium `acces` flag — see below).

### `res/` is split by feature (non-standard)

`app/build.gradle` sets `sourceSets.main.res.srcDirs` to **every subdirectory of `res/` as its own
res root**: `res/alim`, `res/calendar`, `res/common`, `res/corps`, `res/login`, `res/main`,
`res/payment`, `res/tete`, `res/utils`, `res/spinner`. Shared resources (colors, dimens, themes,
strings, common drawables/fonts) live in **`res/common`**. Keep this sourceSets block if you move
the project.

### Package structure (`app/src/main/java/.../femSante/`)

`data/` (models + pure logic), `features/` (Activities/Fragments), `viewmodels/` (`@HiltViewModel`),
`repository/{local,remote}`, `room/{dao,entity,converter,migration,type}`, `shared/` (utilities,
viewers), `di/`.

## Conventions & hard constraints

- **Hilt: constructor injection ONLY.** Field injection (`@Inject lateinit var`) **crashes** the
  build (Kotlin metadata vs Dagger 2.52). Inject via `@HiltViewModel` constructors and access from
  the UI with `by viewModels()`.
- **Bottom-nav hosts use add/hide/show**, never `replace()` on reused fragment instances — the
  latter silently breaks state retention and Flow reactivity (fixed in `HomeActivity`,
  `AlimActivity`, `EntryAddActivity`).
- **Content is data-driven (JSON assets), not hard-coded.** Catalogue content lives in
  `assets/*.json` — `recipes.json`, `media.json`, `micronutrients.json` +
  `nutrient_interactions.json`, and `content_tags.json` (the journal↔content tagging table for the
  "Pour toi" recommendation engine). Each has an **isolated pure parser** (`*JsonParser`, Gson +
  `TypeToken`) feeding a **`@Singleton` repository** that lazy-loads the asset once. Pure logic
  (`RecommendationEngine`, `*Filter`, `DailyRecipeSelector`) takes the catalogue **as a parameter**
  — it never reaches into a hard-coded table. Goal: the app skeleton runs without data (empty
  catalogue = graceful fallback), and the source can move to the API by swapping only the repository.
  When adding catalogue content, extend the JSON — do not reintroduce Kotlin `object` tables.
- **Privacy / RGPD — local-first.** Journal, cycle and medical data stay **strictly on-device**
  (Room + SQLCipher). Never send personal health data to a server. Only streaming media and
  auth/subscription go through the API.
- **Freemium access** = `AppUser.hasAccess`, fed by the API JSON key **`acces`** (true = active/
  lifetime access, false = free). Gate premium content via `UserStore.hasContentAccess()`. Do not
  use `lifetimeAccess` for gating (it is display-only "à vie").
- **UI charte ("zéro anxiété")**: French copy; warm yellow/orange primary; **pink `#C2185B` is
  reserved for period markers only**. `lato_bold` is used **only for big screen titles** via
  `AppTextAppearance.TitleFancy` (3-size scale: base 40sp / `.Compact` 32sp / `.Hero` 52sp) —
  chosen over the earlier handwriting font (`nothing_you_could_do`, removed) as easier on the eyes;
  `dinpro` for everything else.
- **Design tokens** live in `res/common/values/`: `colors.xml` (charte), `dimens.xml`
  (`space_xs/s/m/l`, `card_*`), `themes.xml` (Material 3 roles mapped to the charte). The app is
  **single-look (light)** — there is intentionally no dark mode (`values-night` removed).
- **Accessibility**: touch targets ≥ 48dp; icon-only controls carry a `contentDescription`;
  never rely on colour as the sole information vector (e.g. calendar cells expose a spoken
  description).
- **Naming**: packages lowercase (e.g. `viewmodels`), files PascalCase.
- **Commits**: do **not** add a `Co-Authored-By` trailer. Thematic, conventional messages in French.
- **Versioning**: bump `versionCode`/`versionName` in `appli/app/build.gradle` whenever a major update
  is shipped (new feature set, not a trivial fix), so the release version stays a clear signal of
  what's actually in a given build — do this as part of the change, not as an afterthought.

## Build, run, test

1. Copy `appli/local.properties.example` → `appli/local.properties` and fill:
   - `sdk.dir` (Android SDK path)
   - `API_URL`, `PAYPAL_CLIENT_ID`, `RETURN_URL_CARD`, `RETURN_URL_PAYPAL` (exposed via `BuildConfig`)
   - `JFROG_USER` / `JFROG_PASSWORD` (private Cardinal/PayPal SDK Maven repo; can also be env vars)
2. Build/run from Android Studio, or `./gradlew :app:assembleDebug` in `appli/`.
3. **Tests**: pure-logic objects (`RecipeFilter`, `*JsonParser`, `DailyRecipeSelector`,
   `ShoppingListBuilder`, `MediaFilter`, `MicronutrientFilter`, `RecommendationEngine`…) are
   unit-tested on the JVM. Tests load **dedicated mini-fixtures** from `src/test/resources/`
   (`*_sample.json`, via `javaClass.getResourceAsStream("/…")`) — **never** the production
   `assets/*.json`, so the skeleton stays testable when the data eventually moves to the API.
   **The developer (Steve) runs the tests himself — do not run `gradle test` on their behalf.**

## Recommendations if you relocate / clone the project elsewhere

Things that are **not** in git and must be recreated or carried over:

1. **`appli/local.properties`** — all secrets above. Recreate from `local.properties.example`.
2. **`KeyStore`** (release signing) — needed for signed/release builds. Keep it out of git, store
   it securely (password manager / secret vault).
3. **Private Maven credentials** (`JFROG_USER`/`JFROG_PASSWORD`) for the Cardinal/PayPal SDK — the
   build fails to resolve those dependencies without them.
4. **The Laravel backend** must be deployed and reachable; point `API_URL` at it. The backend must
   return the `acces` boolean in the login response for freemium gating to work.
5. **SQLCipher passphrase** handling — verify how the DB key is derived/stored on the new machine.

Cleanup you can safely do when moving:

- Delete the JVM crash artifacts at `appli/` (`hs_err_pid*.log`, `replay_pid*.log`) and `appli/build/`.
- Keep the custom `sourceSets { res.srcDirs = … }` block — the feature-split `res/` layout depends
  on it.

Environment notes:

- Primary dev is on **Windows**. Git normalises to **LF in the repo, CRLF in the working copy**.
  Beware **case-only renames**: with `core.ignorecase`, a `git reset` can flatten a case rename back
  to the old casing — rename directories via a temporary name (`git mv a a_tmp && git mv a_tmp A`).
- Requires Android SDK 36 and a JDK compatible with AGP/Kotlin 2.1.
