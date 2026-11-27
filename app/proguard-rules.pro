-keepattributes *Annotation*,InnerClasses,Signature,Exceptions,EnclosingMethod

-dontwarn org.osmdroid.**
-keep class org.osmdroid.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.slf4j.**
-dontwarn org.apache.**

-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * { @androidx.room.* <fields>; }
-keepclassmembers class * { @androidx.room.* <methods>; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class retrofit2.** { *; }
-keepclassmembers,allowobfuscation,allowshrinking interface * {
  @retrofit2.http.* <methods>;
}
-keepclasseswithmembers class * {
  @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Moshi
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class * {
  @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class * implements java.io.Serializable { *; }
-keepclassmembers class * {
  kotlinx.serialization.KSerializer Companion;
}

# Room (already above; keep entities)
-keep class * extends androidx.room.RoomDatabase { *; }
