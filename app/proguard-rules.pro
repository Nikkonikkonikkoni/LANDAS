# LANDAS Release ProGuard Rules
-keep class ph.edu.wvsu.landas.data.model.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn org.bouncycastle.**
