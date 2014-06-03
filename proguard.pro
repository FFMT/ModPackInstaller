-libraryjars <java.home>/lib/rt.jar

-dontoptimize
-dontobfuscate
-dontpreverify
-dontwarn javax.annotation.**
-dontwarn javax.inject.**
-dontwarn **

-keep class fr.minecraftforgefrance.installer.ModPackInstaller {
    public static void main(java.lang.String[]);
}
