package br.com.fipeplaca.ui

import br.com.fipeplaca.data.PlacaParser
import retrofit2.HttpException
import java.io.IOException

/** Traduz falhas tecnicas em mensagens para o usuario (fica na camada de interface, fora dos dados). */
object MensagensErro {
    fun de(t: Throwable): String = when (t) {
        is PlacaParser.ErroProvedor -> t.message ?: "Erro na consulta."
        is HttpException -> {
            // O token da FIPE so vale para a API v2: as mensagens dependem de qual servico respondeu
            val v2 = t.response()?.raw()?.request?.url?.encodedPath?.contains("/api/v2/") == true
            when (t.code()) {
            429 -> if (v2) "Limite diário de consultas da FIPE atingido. Informe um token da FIPE em Ajustes ou tente amanhã."
                   else "Limite de consultas da tabela FIPE atingido por agora. Tente novamente em alguns minutos."
            404 -> "Não encontrado na tabela FIPE."
            401, 403 -> if (v2) "Token da FIPE inválido. Confira em Ajustes." else "Acesso recusado pelo serviço. Tente novamente mais tarde."
            else -> "Erro no servidor (HTTP ${t.code()})."
            }
        }
        is java.net.UnknownServiceException -> "O endereço do serviço precisa usar https. Confira em Ajustes."
        is com.google.gson.JsonParseException -> "O serviço respondeu num formato inesperado. Confira o endereço em Ajustes."
        is IllegalArgumentException -> "Endereço do serviço inválido. Confira em Ajustes."
        is java.io.InterruptedIOException -> "O serviço demorou demais para responder. Tente novamente."
        is IOException -> "Sem conexão com a internet ou servidor indisponível."
        else -> "Não foi possível concluir a consulta. Tente novamente."
    }
}
