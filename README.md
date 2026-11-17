# Neon Stack — Arcade Edition (Android)

A modern neon falling-block arcade and roguelike deckbuilder Android game engineered with **Jetpack Compose (Material 3)**, **MVI (Model-View-Intent)** with `StateFlow`, **Hardware VSYNC Choreographer** (up to 120Hz LTPO), **AGSL Shaders** (Android 13+), **Procedural Polyphonic Synthwave Chiptune BGM**, **Pro Calibration Studio**, an **Arcade Hall of Fame Leaderboard**, and **Rogue Spire Mode (Roguelike / Deckbuilder)**.

---

## Dual Game Modes

### 1. Endless Mode (Classic Arcade)
- **Precision 10×18 Playfield & Wall-Kick Engine**:
  - Multi-test wall & floor kick rotation tables for all 7 synthwave prism shapes.
  - 7-Bag Randomizer.
  - 500ms lock delay with 15 movement/rotation resets.
  - 3-Corner Pivot Spin detection (Full vs Mini) with back-to-back ($1.5\times$) bonuses and combos.
  - Endless progression with dynamic gravity acceleration and level scaling.

### 2. Rogue Spire Mode (Roguelike Deckbuilder)
- **Floor-by-Floor Progression**:
  - Each floor sets a clear Line Quota ($8 + 2 \times (\text{floor} - 1)$ lines).
  - **Boss Floors:** Every 3rd floor triggers high-stakes Boss Encounters.
- **Relic Drafting System (`RelicDraftDialog.kt`)**:
  - Upon clearing a floor, players draft from 3 game-altering **Relics**:
    - **Midas Touch** *(Common)*: 25% of spawned blocks turn Gold, awarding +400 bonus points each when cleared.
    - **Overhang Drill** *(Rare)*: Hard drops drill through 1 obstructed overhang block directly beneath the landing site.
    - **Chrono Buffer** *(Common)*: Expands Lock Delay to 850ms and permits up to 25 rotation/movement resets.
    - **Quad Surge** *(Legendary)*: 4-line clears award 3x points and instantly vaporize the bottom-most row.
    - **Dual Hold Chamber** *(Rare)*: Enables holding pieces twice before requiring a lock.
    - **Beam Conduit** *(Legendary)*: Injects an additional straight Beam block into every 7-piece bag cycle.
    - **Kinetic Core** *(Rare)*: Hard dropping from 10+ rows height triggers a mini shockwave clearing adjacent blocks.
    - **Laser Cleaner** *(Common)*: Vaporizes the bottom-most row automatically after completing each floor.
    - **Gravity Anchor** *(Common)*: Soft drop moves at terminal hyper-speed and scores 3 points per cell.

---

## Architecture

```
                       +-----------------------------------+
                       |         Choreographer VSYNC       |
                       |    (60Hz, 90Hz, 120Hz LTPO)       |
                       +-----------------+-----------------+
                                         |
                                         v
+------------------+             +----------------------+            +----------------------+
|   User Intents   |  ------->   |  NeonStackViewModel  |  ------->  |   NeonStackUiState   |
| • Mode Switch    |             |     (MVI Pattern)    |            |     (StateFlow)      |
| • Relic Draft    |             +----------+-----------+            +----------+-----------+
| • Touch Gestures |                        |                                   |
| • Leaderboard    |                        | Effects                           v
| • Calibration    |                        v                        +----------------------+
+------------------+             +----------------------+            |   Jetpack Compose    |
                                 |  NeonStackUiEffect   |            | • Material 3 HUD     |
                                 | • Procedural BGM     |            | • Hall of Fame       |
                                 | • AudioTrack SFX     |            | • Rogue Spire Draft  |
                                 | • Vibrator Waves     |            | • Neo-Arcade Pad     |
                                 +----------------------+            | • AGSL Shaders       |
                                                                     +----------------------+
```

---

## Key Features

- **Arcade Hall of Fame (`LeaderboardDialog.kt`)**: Top 10 local leaderboard with rank medals, interactive 6-letter arcade tag entry, and persistent stats per mode.
- **Procedural 3-Channel Synthwave Chiptune Engine (`ChiptuneMusicEngine.kt`)**: Synthesizes the original *"Neon Pulse"* D-minor synthwave chiptune soundtrack in real time (pulse lead, triangle octave bass, 15-bit LFSR percussion) with smooth level-reactive BPM scaling (120 → 183 BPM).
- **Next-Gen AGSL Shaders (`AgslShaderManager.kt`)**: Android 13+ shockwave refraction on 4-line Quad Clears, chromatic aberration on Hard Drop slams, and optional retro CRT scanlines via `RenderEffect.createChainEffect`.
- **Pro Calibration Studio (`CalibrationDialog.kt`)**: Adjustable DAS (80–260ms), ARR (0–65ms), and Soft Drop repeat rate (15–80ms), persisted in `SharedPreferences`.
- **Dual Control Schemes**: Sculpted Neo-Arcade dual-thumb pad or full-screen 1:1 gesture controls.

---

## Building & Testing

```powershell
./gradlew assembleDebug
./gradlew testDebugUnitTest
```
