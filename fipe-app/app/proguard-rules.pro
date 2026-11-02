# Modelos lidos e gravados pelo Gson: nomes dos campos preservados (inclusive entre versoes do app,
# para que os salvos continuem legiveis depois de uma atualizacao). O resto do codigo e reduzido e ofuscado.
-keep class br.com.fipeplaca.data.ItemFipe { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.ModelosResposta { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.ValorV1 { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.ItemV2 { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.ReferenciaV2 { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.ValorV2 { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.ReferenciaBrasilApi { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.PrecoBrasilApi { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.ConsultaSalva { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.VeiculoPlaca { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.FipeResultado { <fields>; <init>(...); }
-keep class br.com.fipeplaca.data.PontoHistorico { <fields>; <init>(...); }
-keep enum br.com.fipeplaca.data.TipoVeiculo { *; }

-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.**
-dontwarn okio.**
