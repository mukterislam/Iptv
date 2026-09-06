# Add project specific ProGuard rules here.

# Keep data models
-keep class com.example.data.model.** { *; }
-keep class com.example.data.repository.SavedPlaylist { *; }

# Media3 / ExoPlayer rules
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.exoplayer.hls.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.common.** { *; }
-dontwarn androidx.media3.**

# OkHttp & Okio rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Room
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
