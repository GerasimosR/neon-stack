# ===========================================================================
# Neon Stack — Production R8 / Proguard Optimization Rules
# ===========================================================================

# 1. Core Neon Stack Engine & Rules (Preserve data classes & enums)
-keep class com.neonstack.engine.** { *; }
-keepclassmembers enum com.neonstack.engine.** { *; }

# 2. Rogue Spire Deckbuilder & Relic Catalog
-keep class com.neonstack.engine.rogue.** { *; }
-keepclassmembers enum com.neonstack.engine.rogue.** { *; }

# 3. Data Persistence & Arcade Leaderboard Serialization
-keep class com.neonstack.data.** { *; }
-keepclassmembers class com.neonstack.data.LeaderboardEntry {
    <fields>;
    <init>(...);
}

# 4. MVI Architecture (UiState, Intent, UiEffects)
-keep class com.neonstack.ui.mvi.** { *; }

# 5. AGSL Runtime Shaders & Hardware Visual FX
-keep class com.neonstack.ui.shaders.** { *; }
-keep class com.neonstack.ui.NeonStackCanvasView** { *; }

# 6. Audio Synthesizer & Polyphonic Chiptune Engine
-keep class com.neonstack.audio.** { *; }

# 7. Kotlin Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# 8. Jetpack Compose
-keep class androidx.compose.ui.platform.** { *; }
