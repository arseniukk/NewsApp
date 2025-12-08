# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# --- Retrofit & OkHttp ---
-keepattributes Signature
-keepattributes Exceptions

# Зберігаємо класи, які використовуються для мережевих запитів
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# --- Kotlinx Serialization ---
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.** { *; }

# --- Наші моделі даних ---
# Важливо зберегти імена полів у DTO, щоб JSON парсився правильно
-keep class com.example.newsapp.network.** { *; }
-keep class com.example.newsapp.rss.** { *; }
-keep class com.example.newsapp.Article { *; }

# --- Room Database ---
-keep class androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# --- Виправлення для SimpleXML (RSS) ---
# Дозволяє R8 коректно обробляти бібліотеку для XML без помилок
-keep class org.simpleframework.xml.** { *; }
-keepclassmembers class * {
    @org.simpleframework.xml.* *;
}
-dontwarn org.simpleframework.xml.stream.**
-dontwarn org.xmlpull.v1.**