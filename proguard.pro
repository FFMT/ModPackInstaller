-libraryjars <java.home>/lib/rt.jar

-dontoptimize
-dontobfuscate
-dontpreverify
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn **

-keep class fr.minecraftforgefrance.installer.Installer {
    public static void main(java.lang.String[]);
}

-keepclassmembers class * {
    static final %                *;
    static final java.lang.String *;
}

-keep public class fr.minecraftforgefrance.**.** {
    public protected <methods>;
}

-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
