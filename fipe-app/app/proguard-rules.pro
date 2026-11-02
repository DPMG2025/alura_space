# Modelos usados pelo Gson
-keep class br.com.fipeplaca.data.** { *; }
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson com tipos genericos (lista de salvos) no modo completo do R8
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class br.com.fipeplaca.data.ConsultaSalva { *; }
