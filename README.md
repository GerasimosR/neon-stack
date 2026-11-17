# Neon Stack — Arcade Edition (Android)

An original neon roguelike block deckbuilder and cyber-arcade game for Android, built with **Jetpack Compose (Material 3)**, **MVI (Model-View-Intent)** with `StateFlow`, **Hardware VSYNC Choreographer** (up to 120Hz LTPO), **AGSL Shaders** (Android 13+), **Procedural Polyphonic Synthwave Chiptune BGM**, **Pro Calibration Studio**, and an **Arcade Hall of Fame**.

---

## Dual Game Modes

### 1. Rogue Spire (Flagship Roguelike Deckbuilder)
- **Floor-by-Floor Ascent:** Each floor sets a clear Line Quota ($8 + 2 \times (\text{floor} - 1)$ lines), with **Boss Floors** every 3rd floor.
- **Relic Drafting System (`RelicDraftDialog.kt`):** Draft 1 of 3 game-bending **Relics** after each floor:
  - **Midas Touch** *(Common)*: 25% of spawned blocks turn Gold (+400 bonus points each when cleared).
  - **Overhang Drill** *(Rare)*: Hard drops drill through 1 obstructed overhang block beneath the landing site.
  - **Chrono Buffer** *(Common)*: Expands Lock Delay to 850ms and permits up to 25 movement/rotation resets.
  - **Quad Surge** *(Legendary)*: 4-line clears award 3x points and instantly vaporize the bottom-most row.
  - **Dual Vault Chamber** *(Rare)*: Enables vaulting pieces twice before requiring a lock.
  - **Beam Conduit** *(Legendary)*: Injects an additional straight Beam into every 9-piece bag cycle.
  - **Kinetic Core** *(Rare)*: Hard dropping from 10+ rows height triggers a shockwave clearing adjacent blocks.
  - **Laser Cleaner** *(Common)*: Vaporizes the bottom-most row automatically after completing each floor.
  - **Gravity Anchor** *(Common)*: Soft drop moves at hyper-speed and scores 3 points per cell.

### 2. Endless Mode (9×16 Arcade)
- **Original 9×16 Mobile-Native Playfield** paired with a **9-Shape Prism Roster** (7 four-cell poly-cores plus two signature 3-cell shapes: **Elbow** and **Pulse**).
- **Custom Omni-Kick Table & Overclock Spins:** Rewards tight rotational tucks on both **Crown** and **Elbow** pieces, with Surge ($1.5\times$) and Chain multipliers (`150 / 400 / 750 / 1200` base scoring curve).

---

## Signature Visual & Audio Identity

- **Neon Glass Circuit Modules (`NeonStackCanvasView.kt`):** Dark obsidian translucent cores framed by glowing neon circuit rims, center diamond micro-nodes, and fused energy bridges linking adjacent cells of the active piece.
- **Laser Drop-Rails:** Vertical translucent projection rails descend from the active piece to luminous surface impact pads for zero-clutter landing alignment.
- **Unified Top Command Dock:** Sleek `VAULT` + horizontal `QUEUE` bar above the centered `9×16` playfield across both Arcade Pad and Gesture modes.
- **Original Synthwave Chiptune Engine (`ChiptuneMusicEngine.kt`):** Procedural 3-channel synthesis of the original *"Neon Pulse"* D-minor soundtrack with smooth level-reactive BPM scaling (120 → 183 BPM).
- **AGSL Runtime Shaders (`AgslShaderManager.kt`):** Android 13+ shockwave refraction on 4-line Quad Clears, chromatic aberration on Hard Drop slams, and optional retro CRT scanlines via `RenderEffect.createChainEffect`.

---

## Building & Testing

```powershell
./gradlew assembleDebug
./gradlew testDebugUnitTest
```
