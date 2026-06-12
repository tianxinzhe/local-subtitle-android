# Add project specific ProGuard rules here.

# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep model classes
-keep class com.lemonsubtitle.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
