# Add project specific ProGuard rules here.

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.globaloutcomes.phi.**$$serializer { *; }
-keepclassmembers class com.globaloutcomes.phi.** {
    *** Companion;
}
-keepclasseswithmembers class com.globaloutcomes.phi.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep data classes
-keep class com.globaloutcomes.phi.data.local.entities.** { *; }
-keep class com.globaloutcomes.phi.domain.model.** { *; }
