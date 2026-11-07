# PulseMind 360 ProGuard / R8 Keep Rules

# Jetpack Compose & Material 3
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Room Database Entities & DAOs
-keep class com.dev.myapplication.data.local.entity.** { *; }
-keepclassmembers class com.dev.myapplication.data.local.entity.** { *; }
-keep interface com.dev.myapplication.data.local.dao.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# Domain Models & Core Contracts
-keep class com.dev.myapplication.domain.** { *; }
-keepclassmembers class com.dev.myapplication.domain.** { *; }

# Hilt & Dependency Injection
-keep class dagger.hilt.** { *; }
-keepclassmembers class dagger.hilt.** { *; }
-keep class com.dev.myapplication.di.** { *; }

# Retrofit & Gson
-keep class retrofit2.** { *; }
-keepclassmembers class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers class com.google.gson.** { *; }

# WorkManager Workers
-keep class androidx.work.** { *; }
-keepclassmembers class androidx.work.** { *; }
-keep class com.dev.myapplication.data.worker.** { *; }
