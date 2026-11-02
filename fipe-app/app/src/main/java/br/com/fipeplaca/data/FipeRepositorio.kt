package br.com.fipeplaca.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class FipeRepositorio(
    private val prefs: ConfigConexao,
    baseV1: String = "https://parallelum.com.br/fipe/api/v1/",
    baseV2: String = "https://fipe.parallelum.com.br/api/v2/",
    baseOficial: String = "https://veiculos.fipe.org.br/api/veiculos/",
) {
    private val hostOficial = baseOficial.toHttpUrl().host
    private val hostV2 = baseV2.toHttpUrl().host


    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val req = chain.request()
            val token = prefs.tokenFipe
            // Token opcional da FIPE v2 (aumenta o limite diario de consultas).
            when {
                req.url.host == hostV2 && req.url.toString().startsWith(baseV2) && token.isNotBlank() ->
                    chain.proceed(req.newBuilder().header("X-Subscription-Token", token).build())
                req.url.host == hostOficial && req.url.toString().startsWith(baseOficial) ->
                    chain.proceed(
                        req.newBuilder()
                            .header("Referer", "https://veiculos.fipe.org.br/")
                            .header("User-Agent", "Mozilla/5.0 (Linux; Android) FipePelaPlaca")
                            .build()
                    )
                else -> chain.proceed(req)
            }
        }
        .build()

    private fun retrofit(base: String) = Retrofit.Builder()
        .baseUrl(base)
        .client(http)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val v1 = retrofit(baseV1).create(FipeV1Api::class.java)
    private val v2 = retrofit(baseV2).create(FipeV2Api::class.java)
    private val oficial = retrofit(baseOficial).create(FipeOficialApi::class.java)
    private val placaApi = retrofit("https://localhost/").create(PlacaApi::class.java)

    private val cacheMarcas = mutableMapOf<TipoVeiculo, List<ItemFipe>>()
    private var cacheReferencias: List<ReferenciaV2>? = null
    private val travaRef = Mutex()

    // ---------- Busca manual (marca, modelo, ano) ----------

    suspend fun marcas(tipo: TipoVeiculo): List<ItemFipe> =
        cacheMarcas[tipo] ?: v1.marcas(tipo.caminhoV1).sortedBy { Texto.chave(it.nome) }
            .also { cacheMarcas[tipo] = it }

    suspend fun modelos(tipo: TipoVeiculo, marca: String): List<ItemFipe> =
        v1.modelos(tipo.caminhoV1, marca).modelos

    suspend fun anos(tipo: TipoVeiculo, marca: String, modelo: String): List<ItemFipe> =
        v1.anos(tipo.caminhoV1, marca, modelo).map { it.copy(nome = Texto.nomeAno(it.nome)) }

    suspend fun valor(tipo: TipoVeiculo, marca: String, modelo: String, ano: String): FipeResultado {
        val r = v1.valor(tipo.caminhoV1, marca, modelo, ano)
        return FipeResultado(
            tipo = tipo,
            marca = r.marca.orEmpty(),
            modelo = r.modelo.orEmpty(),
            anoModelo = r.anoModelo?.toString()?.let(Texto::nomeAno).orEmpty(),
            combustivel = r.combustivel.orEmpty(),
            codigoFipe = r.codigoFipe.orEmpty(),
            mesReferencia = r.mesReferencia?.trim().orEmpty(),
            valor = r.valor,
            codigoAno = ano,
        )
    }

    // ---------- Busca pela placa ----------

    fun temTokenPlaca(): Boolean = prefs.tokenPlaca.isNotBlank() || !prefs.urlPlaca.contains("{token}")

    suspend fun consultarPlaca(placaDigitada: String): VeiculoPlaca {
        val placa = Placa.normalizar(placaDigitada)
        val url = prefs.urlPlaca
            .replace("{placa}", placa)
            .replace("{token}", URLEncoder.encode(prefs.tokenPlaca, "UTF-8"))
        val resp = placaApi.consultar(url)
        val corpo = resp.body()
        if (!resp.isSuccessful || corpo == null) {
            throw PlacaParser.ErroProvedor(
                when (resp.code()) {
                    401, 403 -> "Token do serviço de placas inválido ou sem permissão. Confira em Configurações."
                    402 -> "Sem créditos no serviço de placas."
                    404 -> "Placa não encontrada."
                    429 -> "Limite de consultas de placa atingido. Tente mais tarde."
                    else -> "Erro no serviço de placas (HTTP ${resp.code()})."
                }
            )
        }
        val veiculo = PlacaParser.parse(corpo, placa)
        // Completa valor e codigo do ano pela propria tabela FIPE, quando possivel.
        val fipe = veiculo.fipe.map { r -> runCatching { completar(r, veiculo.anoModelo) }.getOrDefault(r) }
        return veiculo.copy(fipe = fipe)
    }

    private suspend fun completar(r: FipeResultado, anoModeloVeiculo: String?): FipeResultado {
        val ano = r.anoModelo.ifBlank { anoModeloVeiculo.orEmpty() }
        val codigoAno = r.codigoAno ?: resolverCodigoAno(r.copy(anoModelo = ano)) ?: return r
        val v = v2.valorPorCodigo(r.tipo.caminhoV2, r.codigoFipe, codigoAno)
        return r.copy(
            codigoAno = codigoAno,
            anoModelo = ano.ifBlank { v.anoModelo?.toString().orEmpty() }.let(Texto::nomeAno),
            marca = r.marca.ifBlank { v.marca.orEmpty() },
            modelo = r.modelo.ifBlank { v.modelo.orEmpty() },
            combustivel = r.combustivel.ifBlank { v.combustivel.orEmpty() },
            valor = v.valor ?: r.valor,
            mesReferencia = v.mesReferencia?.trim() ?: r.mesReferencia,
        )
    }

    /** Descobre o codigo do ano FIPE (ex.: "2016-1") a partir do ano modelo e combustivel. */
    suspend fun resolverCodigoAno(r: FipeResultado): String? {
        r.codigoAno?.let { return it }
        val anos = v2.anosPorCodigo(r.tipo.caminhoV2, r.codigoFipe)
        val ano = r.anoModelo.filter { it.isDigit() }.ifBlank { if ("zero" in Texto.chave(r.anoModelo)) "32000" else "" }
        val doAno = anos.filter { it.codigo.substringBefore('-') == ano }
        if (doAno.isEmpty()) return null
        val comb = Texto.chave(r.combustivel).take(4)
        return (doAno.firstOrNull { comb.isNotEmpty() && Texto.chave(it.nome).contains(comb) } ?: doAno.first()).codigo
    }

    // ---------- Historico de precos ----------

    /**
     * Meses de referencia (mais recente primeiro). Usa o servico oficial e, se ele falhar,
     * a lista da API v2. Os codigos sao os mesmos nas duas fontes.
     */
    private suspend fun referencias(): List<ReferenciaV2> = travaRef.withLock {
        cacheReferencias ?: (
            runCatching { comNovasTentativas { oficial.referencias() }.map { ReferenciaV2(it.codigo.toString(), it.mes.trim()) } }
                .getOrNull()?.takeIf { it.isNotEmpty() }
                ?: v2.referencias()
            )
            .sortedByDescending { it.codigo.toIntOrNull() ?: 0 }
            .also { cacheReferencias = it }
    }

    /** Valor de um mes: primeiro no servico oficial, depois na API v2. Null se o mes nao existir. */
    private suspend fun valorNoMes(r: FipeResultado, codigoAno: String, ref: ReferenciaV2, diagnostico: (String) -> Unit): PontoHistorico? {
        val ano = codigoAno.substringBefore('-')
        val combustivel = codigoAno.substringAfter('-', "1")
        val doOficial = runCatching {
            comNovasTentativas {
                oficial.valor(
                    mapOf(
                        "codigoTabelaReferencia" to ref.codigo,
                        "codigoMarca" to "",
                        "codigoModelo" to "",
                        "codigoTipoVeiculo" to r.tipo.codigoOficial.toString(),
                        "anoModelo" to ano,
                        "codigoTipoCombustivel" to combustivel,
                        "tipoVeiculo" to r.tipo.nomeOficial,
                        "modeloCodigoExterno" to r.codigoFipe,
                        "tipoConsulta" to "codigo",
                    )
                )
            }
        }.onFailure { diagnostico("${ref.codigo} ${ref.mes} oficial: ${descrever(it)}") }.getOrNull()
        Dinheiro.paraDouble(doOficial?.valor)?.let { num ->
            return PontoHistorico((doOficial?.mesReferencia ?: ref.mes).trim(), doOficial?.valor.orEmpty(), num)
        }
        if (doOficial?.erro != null) diagnostico("${ref.codigo} ${ref.mes} oficial: ${doOficial.erro}")

        val doV2 = runCatching { comNovasTentativas { v2.valorPorCodigo(r.tipo.caminhoV2, r.codigoFipe, codigoAno, ref.codigo) } }
            .onFailure { diagnostico("${ref.codigo} ${ref.mes} v2: ${descrever(it)}") }
            .getOrNull() ?: return null
        val num = Dinheiro.paraDouble(doV2.valor) ?: return null
        return PontoHistorico((doV2.mesReferencia ?: ref.mes).trim(), doV2.valor.orEmpty(), num)
    }

    private fun descrever(t: Throwable) = "${t::class.java.simpleName} ${(t as? HttpException)?.code() ?: ""} ${t.message.orEmpty()}".trim()

    /**
     * Busca o valor do veiculo em cada mes de referencia do periodo.
     * Meses em que o veiculo nao constava na tabela sao ignorados.
     */
    suspend fun historico(
        r: FipeResultado,
        periodo: PeriodoHistorico,
        diagnostico: (String) -> Unit = {},
        progresso: (feitos: Int, total: Int) -> Unit = { _, _ -> },
    ): List<PontoHistorico> = coroutineScope {
        val codigoAno = resolverCodigoAno(r)
            ?: throw PlacaParser.ErroProvedor("Ano modelo não encontrado na tabela FIPE para este código.")
        val meses = Referencias.filtrar(referencias(), periodo)
        val limite = Semaphore(2)
        var feitos = 0
        val pontos = meses.map { ref ->
            async {
                limite.withPermit {
                    val ponto = valorNoMes(r, codigoAno, ref, diagnostico)
                    synchronized(this@FipeRepositorio) { feitos++ }
                    progresso(feitos, meses.size)
                    ponto
                }
            }
        }.awaitAll().filterNotNull()
        if (pontos.isEmpty() && meses.isNotEmpty()) {
            throw PlacaParser.ErroProvedor("Não foi possível obter o histórico agora. Verifique a conexão e tente novamente.")
        }
        // Mesmo mes vindo das duas fontes nao deve repetir
        pontos.distinctBy { Texto.chave(it.mesReferencia) }
    }

    /**
     * Repete a chamada quando a falha e passageira (limite de requisicoes, erro do servidor
     * ou rede), esperando um pouco mais a cada vez. Erros definitivos (404) sobem na hora.
     */
    private suspend fun <T> comNovasTentativas(tentativas: Int = 4, bloco: suspend () -> T): T {
        var espera = esperaInicialMs
        repeat(tentativas - 1) {
            try {
                return bloco()
            } catch (e: HttpException) {
                if (e.code() != 429 && e.code() < 500) throw e
            } catch (e: IOException) {
                // falha de rede passageira: tenta de novo
            }
            kotlinx.coroutines.delay(espera)
            espera *= 2
        }
        return bloco()
    }

    /** Espera antes da primeira nova tentativa (menor nos testes). */
    internal var esperaInicialMs = 1_000L

    companion object {
        fun mensagemErro(t: Throwable): String = when (t) {
            is PlacaParser.ErroProvedor -> t.message ?: "Erro na consulta."
            is HttpException -> when (t.code()) {
                429 -> "Limite diário de consultas da FIPE atingido. Informe um token da FIPE em Configurações ou tente amanhã."
                404 -> "Não encontrado na tabela FIPE."
                401, 403 -> "Token da FIPE inválido. Confira em Configurações."
                else -> "Erro no servidor (HTTP ${t.code()})."
            }
            is IOException -> "Sem conexão com a internet ou servidor indisponível."
            else -> t.message ?: "Erro inesperado."
        }
    }
}
