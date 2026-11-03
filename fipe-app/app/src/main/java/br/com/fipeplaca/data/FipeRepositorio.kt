package br.com.fipeplaca.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    baseBrasilApi: String = "https://brasilapi.com.br/api/",
    /** Pasta do cache HTTP em disco (permite usar a Tabela sem internet). Null desliga o cache. */
    diretorioCache: java.io.File? = null,
) {
    private val hostV2 = baseV2.toHttpUrl().host


    private val hostV1 = baseV1.toHttpUrl().host

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(25, TimeUnit.SECONDS)
        .apply { if (diretorioCache != null) cache(okhttp3.Cache(diretorioCache, 20L * 1024 * 1024)) }
        // Listas e valores da FIPE mudam uma vez por mes: guarda no cache por 6 horas.
        .addNetworkInterceptor { chain ->
            val resp = chain.proceed(chain.request())
            if (chain.request().url.host == hostV1 && resp.isSuccessful) {
                resp.newBuilder().header("Cache-Control", "public, max-age=21600").removeHeader("Pragma").build()
            } else resp
        }
        // Sem internet: tenta a ultima resposta guardada da Tabela FIPE.
        .addInterceptor { chain ->
            val req = chain.request()
            try {
                chain.proceed(req)
            } catch (e: IOException) {
                if (req.method != "GET" || req.url.host != hostV1) throw e
                val guardada = chain.proceed(req.newBuilder().cacheControl(okhttp3.CacheControl.FORCE_CACHE).build())
                if (guardada.code == 504) { guardada.close(); throw e } else guardada
            }
        }
        .addInterceptor { chain ->
            val req = chain.request()
            val token = prefs.tokenFipe
            // Token opcional da FIPE v2 (aumenta o limite diario de consultas).
            when {
                req.url.host == hostV2 && req.url.toString().startsWith(baseV2) && token.isNotBlank() ->
                    chain.proceed(req.newBuilder().header("X-Subscription-Token", token).build())
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
    private val brasilApi = retrofit(baseBrasilApi).create(BrasilApi::class.java)
    // Consulta de placa sem cache em disco: a URL leva o token e a resposta traz dados do veiculo.
    private val placaApi = Retrofit.Builder()
        .baseUrl("https://localhost/")
        .client(http.newBuilder().cache(null).build())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PlacaApi::class.java)

    private val cacheMarcas = mutableMapOf<TipoVeiculo, List<ItemFipe>>()
    /** Historico mes a mes (classe propria; o repositorio so delega). */
    private val historicoFipe = HistoricoFipe(v2, brasilApi, prefs, ::resolverCodigoAno)

    /** Ajustes de tempo usados nos testes, repassados ao historico. */
    internal var agora: () -> Long
        get() = historicoFipe.agora
        set(v) { historicoFipe.agora = v }
    internal var esperaInicialMs: Long
        get() = historicoFipe.esperaInicialMs
        set(v) { historicoFipe.esperaInicialMs = v; esperaRepositorioMs = v }
    internal var tempoLimiteHistoricoMs: Long
        get() = historicoFipe.tempoLimiteHistoricoMs
        set(v) { historicoFipe.tempoLimiteHistoricoMs = v }
    private var esperaRepositorioMs = 1_000L

    /** Historico de precos do veiculo no periodo (ver [HistoricoFipe]). */
    suspend fun historico(
        r: FipeResultado,
        periodo: PeriodoHistorico,
        diagnostico: (String) -> Unit = {},
        progresso: (feitos: Int, total: Int) -> Unit = { _, _ -> },
    ): ResultadoHistorico = historicoFipe.historico(r, periodo, diagnostico, progresso)

    // ---------- Busca manual (marca, modelo, ano) ----------

    /** Descarta itens incompletos (o Gson ignora a nulidade do Kotlin e pode entregar null). */
    @Suppress("SENSELESS_COMPARISON")
    private fun List<ItemFipe>.validos() = filter { it.codigo != null && it.nome != null }.distinctBy { it.codigo }

    suspend fun marcas(tipo: TipoVeiculo): List<ItemFipe> =
        cacheMarcas[tipo] ?: v1.marcas(tipo.caminhoV1).validos().sortedBy { Texto.chave(Texto.marca(it.nome)) }
            .also { cacheMarcas[tipo] = it }

    suspend fun modelos(tipo: TipoVeiculo, marca: String): List<ItemFipe> =
        v1.modelos(tipo.caminhoV1, marca).modelos.orEmpty().validos()

    suspend fun anos(tipo: TipoVeiculo, marca: String, modelo: String): List<ItemFipe> =
        v1.anos(tipo.caminhoV1, marca, modelo).validos().map { it.copy(nome = Texto.nomeAno(it.nome)) }

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
                    401, 403 -> "Token do serviço de placas inválido ou sem permissão. Confira em Ajustes."
                    402 -> "Sem créditos no serviço de placas."
                    404 -> "Placa não encontrada."
                    429 -> "Limite de consultas de placa atingido. Tente mais tarde."
                    else -> "Erro no serviço de placas (HTTP ${resp.code()})."
                }
            )
        }
        val veiculo = PlacaParser.parse(corpo, placa)
        // Completa valor e codigo do ano pela propria tabela FIPE, quando possivel.
        val fipe = coroutineScope {
            veiculo.fipe.map { r -> async { tentar { completar(r, veiculo.anoModelo) }.getOrDefault(r) } }.awaitAll()
        }
        return veiculo.copy(fipe = fipe)
    }

    /** Atualiza o valor FIPE das versoes de uma placa ja consultada (nao chama o provedor de placas). */
    suspend fun atualizarValores(v: VeiculoPlaca): VeiculoPlaca = coroutineScope {
        val resultados = v.fipe.map { r -> async { tentar { completar(r, v.anoModelo) } } }.awaitAll()
        // Nenhuma versao atualizada: sinaliza a falha (a tela avisa que o valor exibido e o guardado)
        if (resultados.isNotEmpty() && resultados.all { it.isFailure }) throw resultados.first().exceptionOrNull()!!
        v.copy(fipe = resultados.mapIndexed { i, res -> res.getOrDefault(v.fipe[i]) })
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

    companion object {

        /** Valida o modelo de URL do provedor de placas; devolve o problema ou null se estiver ok. */
        fun validarUrlPlaca(url: String): String? = when {
            !url.trim().startsWith("https://") -> "Use um endereço https://"
            "{placa}" !in url -> "Inclua {placa} onde entra a placa"
            else -> null
        }

    }
}
