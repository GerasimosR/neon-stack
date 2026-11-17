# ===========================================================================
# Neon Stack — Production R8 / Proguard Optimization Rules
# ===========================================================================

# 1. Core Neon Stack Engine & Rules (Preserve data classes & enums)
-keep class com.seniordev.neonstack.engine.** { *; }
-keepclassmembers enum com.seniordev.neonstack.engine.** { *; }

# 2. Rogue Spire Deckbuilder & Relic Catalog
-keep class com.seniordev.neonstack.engine.rogue.** { *; }
-keepclassmembers enum com.seniordev.neonstack.engine.rogue.** { *; }

# 3. Data Persistence & Arcade Leaderboard Serialization
-keep class com.seniordev.neonstack.data.** { *; }
-keepclassmembers class com.seniordev.neonstack.data.LeaderboardEntry {
    <fields>;
    <init>(...);
}

# 4. MVI Architecture (UiState, Intent, UiEffects)
-keep class com.seniordev.neonstack.ui.mvi.** { *; }

# 5. AGSL Runtime Shaders & Hardware Visual FX
-keep class com.seniordev.neonstack.ui.shaders.** { *; }
-keep class com.seniordev.neonstack.ui.NeonStackCanvasView** { *; }

# 6. Audio Synthesizer & Polyphonic Chiptune Engine
-keep class com.seniordev.neonstack.audio.** { *; }

# 7. Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# 8. Jetpack Compose
-keep class androidx.compose.ui.platform.** { *; }
