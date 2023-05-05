# Optimize and shrink code
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose

# Preserve annotations, which may be required by libraries
-keepattributes *Annotation*

# Preserve all public classes, along with their public and protected fields and methods
-keep public class image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.** {
  public protected *;
}

# Don't obfuscate the package name
-keep class image.converter.convert.png.jpg.jpeg.webp.pdf.gif.photo.convert.ai.** { *; }

# Keep any classes and methods with names that are used by the Android operating system
-keepnames class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator CREATOR;
}

# Remove unused code
-assumenosideeffects class android.util.Log {
  public static *** d(...);
  public static *** v(...);
}

# Remove debug information
-optimizations !debug



