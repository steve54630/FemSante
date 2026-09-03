# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn com.cardinalcommerce.dependencies.internal.minidev.asm.Accessor
-dontwarn com.cardinalcommerce.dependencies.internal.minidev.asm.BeansAccess
-dontwarn com.cardinalcommerce.dependencies.internal.minidev.asm.ConvertDate
-dontwarn com.cardinalcommerce.dependencies.internal.minidev.asm.FieldFilter
-dontwarn org.bouncycastle.jce.provider.BouncyCastleProvider
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE
-dontwarn org.slf4j.impl.StaticLoggerBinder

# SQLCipher (chiffrement de la base Room) - conserver les classes natives/JNI
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }
-dontwarn net.zetetic.database.**

# Gson (parsing des assets JSON du catalogue + persistance locale) - sans ces règles, la
# minification renomme les champs des classes modèles et Gson ne retrouve plus les clés JSON
# correspondantes en release (contenu vide, liste de courses perdue), alors que tout fonctionne
# normalement en debug (non minifié).
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.audreyRetournayDiet.femSante.data.recipe.** { <fields>; }
-keep class com.audreyRetournayDiet.femSante.data.media.** { <fields>; }
-keep class com.audreyRetournayDiet.femSante.data.micronutrient.** { <fields>; }
-keep class com.audreyRetournayDiet.femSante.data.fodmap.** { <fields>; }
-keep class com.audreyRetournayDiet.femSante.data.recommendation.** { <fields>; }
-keep class com.audreyRetournayDiet.femSante.data.resource.** { <fields>; }
-keep class com.audreyRetournayDiet.femSante.data.toolbox.** { <fields>; }
-keep class com.audreyRetournayDiet.femSante.repository.local.ShoppingListStore$StoredSelection { <fields>; }