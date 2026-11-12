# kotlinx.serialization: keep generated serializers for @Serializable classes
# (saved games and navigation routes are serialized by name).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class com.hoagies.games.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.hoagies.games.**$$serializer { *; }
