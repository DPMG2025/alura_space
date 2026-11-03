package br.com.fipeplaca.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException

/**
 * Repete a chamada quando a falha e passageira (limite de requisicoes, erro do servidor
 * ou rede), esperando um pouco mais a cada vez. Erros definitivos (404, 402) sobem na hora.
 */
internal suspend fun <T> comNovasTentativas(esperaInicialMs: Long, tentativas: Int = 4, bloco: suspend () -> T): T {
    var espera = esperaInicialMs
    repeat(tentativas - 1) {
        try {
            return bloco()
        } catch (e: HttpException) {
            if (e.code() != 429 && e.code() < 500) throw e
        } catch (e: IOException) {
            // falha de rede passageira: tenta de novo
        }
        delay(espera)
        espera *= 2
    }
    return bloco()
}

/**
 * Como runCatching, mas sem engolir o cancelamento: uma consulta cancelada (o usuario limpou
 * a tela ou abriu outra) nao pode terminar "com sucesso" e sobrescrever o estado novo.
 */
internal inline fun <T> tentar(bloco: () -> T): Result<T> = try {
    Result.success(bloco())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}

internal const val HORA = 3_600_000L
