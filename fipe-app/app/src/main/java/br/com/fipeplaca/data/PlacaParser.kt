package br.com.fipeplaca.data

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Converte a resposta do provedor de placas em [VeiculoPlaca].
 * Feito de forma tolerante: procura as chaves sem diferenciar maiusculas e aceita
 * variacoes de nome, para funcionar com a API Placas e provedores parecidos.
 */
object PlacaParser {

    class ErroProvedor(mensagem: String) : Exception(mensagem)

    fun parse(json: JsonElement, placa: String): VeiculoPlaca {
        val raiz = when {
            json.isJsonObject -> json.asJsonObject
            json.isJsonArray && json.asJsonArray.size() > 0 && json.asJsonArray[0].isJsonObject ->
                json.asJsonArray[0].asJsonObject
            else -> throw ErroProvedor("Resposta inesperada do serviço de placas.")
        }
        // Alguns provedores embrulham os dados em "data", "dados" ou "veiculo".
        val obj = listOf("data", "dados", "veiculo", "result").firstNotNullOfOrNull { raiz.obj(it) }
            ?.takeIf { it.str("marca", "modelo") != null } ?: raiz
        val extra = obj.obj("extra")

        val marca = obj.str("MARCA", "marca", "brand") ?: extra?.str("marca")
        val modelo = obj.str("MODELO", "modelo", "model", "SUBMODELO") ?: extra?.str("modelo")
        if (marca == null && modelo == null) {
            val msg = raiz.str("mensagemRetorno", "message", "mensagem", "erro", "error")
            throw ErroProvedor(msg ?: "Placa não encontrada.")
        }

        val fipe = lerFipe(obj)
        val codigoTipo = obj.obj("fipe")?.arr("dados")?.firstOrNull()?.asObj()?.int("tipo_modelo")
            ?: tipoPorTexto(obj.str("segmento", "tipo", "tipo_veiculo") ?: extra?.str("tipo_veiculo", "segmento"))
        val tipo = TipoVeiculo.porCodigo(codigoTipo)

        return VeiculoPlaca(
            placa = Placa.normalizar(obj.str("placa") ?: placa),
            marca = marca,
            modelo = modelo,
            anoFabricacao = obj.str("ano", "anoFabricacao", "ano_fabricacao") ?: extra?.str("ano_fabricacao"),
            anoModelo = obj.str("anoModelo", "ano_modelo") ?: extra?.str("ano_modelo"),
            cor = obj.str("cor", "color"),
            combustivel = obj.str("combustivel") ?: extra?.str("combustivel"),
            municipio = obj.str("municipio", "cidade") ?: extra?.str("municipio"),
            uf = obj.str("uf", "estado") ?: extra?.str("uf"),
            situacao = obj.str("situacao"),
            chassiFinal = obj.str("chassi")?.takeLast(6),
            tipo = tipo,
            fipe = fipe.map { it.copy(tipo = tipo) },
        )
    }

    private fun tipoPorTexto(texto: String?): Int? {
        val t = Texto.chave(texto ?: return null)
        return when {
            "moto" in t || "ciclo" in t -> 2
            "caminh" in t || "onibus" in t -> 3
            else -> 1
        }
    }

    private fun lerFipe(obj: JsonObject): List<FipeResultado> {
        val lista: JsonArray = obj.obj("fipe")?.arr("dados")
            ?: obj.arr("fipe")
            ?: obj.arr("fipes")
            ?: return emptyList()
        return lista.mapNotNull { el ->
            val f = el.asObj() ?: return@mapNotNull null
            val codigo = f.str("codigo_fipe", "codigoFipe", "codeFipe") ?: return@mapNotNull null
            FipeResultado(
                tipo = TipoVeiculo.CARROS,
                marca = f.str("texto_marca", "marca") ?: "",
                modelo = f.str("texto_modelo", "modelo") ?: "",
                anoModelo = f.str("ano_modelo", "anoModelo") ?: "",
                combustivel = f.str("combustivel") ?: "",
                codigoFipe = codigo,
                mesReferencia = f.str("mes_referencia", "mesReferencia") ?: "",
                valor = f.str("texto_valor", "valor", "Valor"),
                score = f.int("score"),
            )
        }.sortedByDescending { it.score ?: 0 }
    }

    // Auxiliares de leitura sem diferenciar maiusculas
    private fun JsonObject.busca(chave: String): JsonElement? =
        get(chave) ?: entrySet().firstOrNull { it.key.equals(chave, ignoreCase = true) }?.value

    private fun JsonObject.str(vararg chaves: String): String? = chaves.firstNotNullOfOrNull { k ->
        val v = busca(k)
        if (v != null && v.isJsonPrimitive) v.asString.trim().takeIf { it.isNotEmpty() } else null
    }

    private fun JsonObject.int(chave: String): Int? =
        busca(chave)?.takeIf { it.isJsonPrimitive }?.asString?.toDoubleOrNull()?.toInt()

    private fun JsonObject.obj(chave: String): JsonObject? = busca(chave)?.takeIf { it.isJsonObject }?.asJsonObject
    private fun JsonObject.arr(chave: String): JsonArray? = busca(chave)?.takeIf { it.isJsonArray }?.asJsonArray
    private fun JsonElement.asObj(): JsonObject? = takeIf { it.isJsonObject }?.asJsonObject
}
