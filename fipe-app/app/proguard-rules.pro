# Modelos usados pelo Gson
-keep class br.com.fipeplaca.data.** { *; }
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.**
-dontwarn okio.**
