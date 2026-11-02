package br.com.fipeplaca.data

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/** API publica da tabela FIPE (Parallelum v1): marcas, modelos, anos e valor. */
interface FipeV1Api {
    @GET("{tipo}/marcas")
    suspend fun marcas(@Path("tipo") tipo: String): List<ItemFipe>

    @GET("{tipo}/marcas/{marca}/modelos")
    suspend fun modelos(@Path("tipo") tipo: String, @Path("marca") marca: String): ModelosResposta

    @GET("{tipo}/marcas/{marca}/modelos/{modelo}/anos")
    suspend fun anos(
        @Path("tipo") tipo: String,
        @Path("marca") marca: String,
        @Path("modelo") modelo: String,
    ): List<ItemFipe>

    @GET("{tipo}/marcas/{marca}/modelos/{modelo}/anos/{ano}")
    suspend fun valor(
        @Path("tipo") tipo: String,
        @Path("marca") marca: String,
        @Path("modelo") modelo: String,
        @Path("ano") ano: String,
    ): ValorV1
}

/** API FIPE v2: consulta por codigo FIPE e por mes de referencia (historico). */
interface FipeV2Api {
    @GET("references")
    suspend fun referencias(): List<ReferenciaV2>

    @GET("{tipo}/{codigoFipe}/years")
    suspend fun anosPorCodigo(
        @Path("tipo") tipo: String,
        @Path("codigoFipe") codigoFipe: String,
    ): List<ItemV2>

    @GET("{tipo}/{codigoFipe}/years/{ano}")
    suspend fun valorPorCodigo(
        @Path("tipo") tipo: String,
        @Path("codigoFipe") codigoFipe: String,
        @Path("ano") ano: String,
        @Query("reference") referencia: String? = null,
    ): ValorV2
}

/** Consulta de placa em provedor configuravel (URL completa montada no app). */
interface PlacaApi {
    @GET
    suspend fun consultar(@Url url: String): Response<JsonElement>
}

/**
 * Servico do site oficial da FIPE. Aceita qualquer mes de referencia sem token,
 * por isso e a fonte principal do historico.
 */
interface FipeOficialApi {
    @POST("ConsultarTabelaDeReferencia")
    suspend fun referencias(): List<ReferenciaOficial>

    @FormUrlEncoded
    @POST("ConsultarValorComTodosParametros")
    suspend fun valor(@FieldMap campos: Map<String, String>): ValorOficial
}
