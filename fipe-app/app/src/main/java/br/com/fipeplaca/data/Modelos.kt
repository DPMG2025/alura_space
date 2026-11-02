package br.com.fipeplaca.data

import com.google.gson.annotations.SerializedName

/** Tipos de veiculo da tabela FIPE, com o caminho usado em cada versao da API. */
enum class TipoVeiculo(val rotulo: String, val caminhoV1: String, val caminhoV2: String) {
    CARROS("Carros", "carros", "cars"),
    MOTOS("Motos", "motos", "motorcycles"),
    CAMINHOES("Caminhões", "caminhoes", "trucks");

    companion object {
        fun porCodigo(codigo: Int?): TipoVeiculo = when (codigo) {
            2 -> MOTOS
            3 -> CAMINHOES
            else -> CARROS
        }
    }
}

/** Item generico (marca, modelo ou ano) da API FIPE v1. */
data class ItemFipe(
    @SerializedName("codigo") val codigo: String,
    @SerializedName("nome") val nome: String,
)

data class ModelosResposta(
    @SerializedName("modelos") val modelos: List<ItemFipe>? = emptyList(),
    @SerializedName("anos") val anos: List<ItemFipe> = emptyList(),
)

data class ValorV1(
    @SerializedName("TipoVeiculo") val tipoVeiculo: Int? = null,
    @SerializedName("Valor") val valor: String? = null,
    @SerializedName("Marca") val marca: String? = null,
    @SerializedName("Modelo") val modelo: String? = null,
    @SerializedName("AnoModelo") val anoModelo: Int? = null,
    @SerializedName("Combustivel") val combustivel: String? = null,
    @SerializedName("CodigoFipe") val codigoFipe: String? = null,
    @SerializedName("MesReferencia") val mesReferencia: String? = null,
)

/** Item da API FIPE v2 (anos por codigo FIPE). */
data class ItemV2(
    @SerializedName("code") val codigo: String,
    @SerializedName("name") val nome: String,
)

data class ReferenciaV2(
    @SerializedName("code") val codigo: String,
    @SerializedName("month") val mes: String,
)

data class ValorV2(
    @SerializedName("brand") val marca: String? = null,
    @SerializedName("codeFipe") val codigoFipe: String? = null,
    @SerializedName("fuel") val combustivel: String? = null,
    @SerializedName("model") val modelo: String? = null,
    @SerializedName("modelYear") val anoModelo: Int? = null,
    @SerializedName("price") val valor: String? = null,
    @SerializedName("referenceMonth") val mesReferencia: String? = null,
    @SerializedName("vehicleType") val tipoVeiculo: Int? = null,
)

/** Resultado FIPE normalizado, independente da origem (manual ou placa). */
data class FipeResultado(
    val tipo: TipoVeiculo,
    val marca: String,
    val modelo: String,
    val anoModelo: String,
    val combustivel: String,
    val codigoFipe: String,
    val mesReferencia: String,
    val valor: String?,
    /** Codigo do ano no formato da FIPE (ex.: "2016-1"), usado no historico. */
    val codigoAno: String? = null,
    /** Grau de confianca do provedor de placa (0 a 100), quando houver. */
    val score: Int? = null,
)

/** Dados do veiculo retornados pela consulta de placa. */
data class VeiculoPlaca(
    val placa: String,
    val marca: String?,
    val modelo: String?,
    val anoFabricacao: String?,
    val anoModelo: String?,
    val cor: String?,
    val combustivel: String?,
    val municipio: String?,
    val uf: String?,
    val situacao: String?,
    val chassiFinal: String?,
    val tipo: TipoVeiculo,
    val fipe: List<FipeResultado>,
)

/** Ponto do historico de precos: mes de referencia e valor. */
data class PontoHistorico(
    val mesReferencia: String,
    val valorTexto: String,
    val valor: Double,
)

/**
 * Historico obtido: os pontos (como lista) e quantos meses foram pedidos e quantos
 * ficaram de fora por exigirem token com acesso ao historico (HTTP 402).
 */
data class ResultadoHistorico(
    val pontos: List<PontoHistorico>,
    val solicitados: Int,
    val exigemToken: Int,
) : List<PontoHistorico> by pontos

enum class PeriodoHistorico(val rotulo: String) {
    ANO_ATUAL("No ano"),
    MESES_12("12 meses"),
    MESES_24("24 meses"),
}

/** Mes de referencia na BrasilAPI. */
data class ReferenciaBrasilApi(
    @SerializedName("codigo") val codigo: Int,
    @SerializedName("mes") val mes: String,
)

/** Preco FIPE na BrasilAPI (uma entrada por ano modelo e combustivel do codigo). */
data class PrecoBrasilApi(
    @SerializedName("valor") val valor: String? = null,
    @SerializedName("anoModelo") val anoModelo: Int? = null,
    @SerializedName("combustivel") val combustivel: String? = null,
    @SerializedName("mesReferencia") val mesReferencia: String? = null,
    @SerializedName("codigoFipe") val codigoFipe: String? = null,
)
