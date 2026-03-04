-keep class com.family.parentalcontrol.** { *; }
-keep class com.family.parentalcontrol.models.** { *; }
-keep class com.family.parentalcontrol.activities.** { *; }
-keep class com.family.parentalcontrol.services.** { *; }
-keep class com.family.parentalcontrol.receivers.** { *; }
-keep class com.family.parentalcontrol.utils.** { *; }

# Keep all classes in models package
-keepclassmembers class com.family.parentalcontrol.models.** {
    <fields>;
    <methods>;
}

# Preserve annotations
-keepattributes *Annotation*

# Keep Supabase classes
-keep class io.github.postgrest.** { *; }
-keep class io.ktor.** { *; }
-keep class kotlinx.serialization.** { *; }

# Keep AndroidX classes
-keep class androidx.** { *; }

# Suppress warnings
-dontwarn org.jetbrains.kotlin.**
-dontwarn kotlin.**
