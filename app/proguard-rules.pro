# Retrofit + Gson
-keep class dev.solsynth.solian.data.model.** { *; }
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Wear Compose
-keep class androidx.wear.compose.** { *; }
-dontwarn androidx.wear.compose.**

# Gson
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
