# Add project specific ProGuard rules here.
# Global Outcomes PHI Platform ProGuard Configuration

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Hilt classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel

# Keep serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.globaloutcomes.phi.**$$serializer { *; }
-keepclassmembers class com.globaloutcomes.phi.** {
    *** Companion;
}
-keepclasseswithmembers class com.globaloutcomes.phi.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Ktor
-keep class io.ktor.** { *; }

# Keep CameraX
-keep class androidx.camera.** { *; }

# Keep ML Kit
-keep class com.google.mlkit.** { *; }

# Keep Lottie
-keep class com.airbnb.lottie.** { *; }
