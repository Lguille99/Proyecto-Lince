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
