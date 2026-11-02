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
    private var cacheReferencias: List<ReferenciaV2>? = null
    private var referenciasEm = 0L
    @Volatile private var brasilApiIndisponivelAte = 0L
    /** Maior codigo de referencia que respondeu 402 sem token (meses ate ele nao sao pedidos de novo por 1 hora). */
    @Volatile private var limite402: Int? = null
    @Volatile private var limite402Ate = 0L
    /** Relogio (substituivel nos testes). */
    internal var agora: () -> Long = System::currentTimeMillis
    private val travaRef = Mutex()

    // ---------- Busca manual (marca, modelo, ano) ----------

    /** Descarta itens incompletos (o Gson ignora a nulidade do Kotlin e pode entregar null). */
    @Suppress("SENSELESS_COMPARISON")
    private fun List<ItemFipe>.validos() = filter { it.codigo != null && it.nome != null }

    suspend fun marcas(tipo: TipoVeiculo): List<ItemFipe> =
        cacheMarcas[tipo] ?: v1.marcas(tipo.caminhoV1).validos().sortedBy { Texto.chave(it.nome) }
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
        val fipe = v.fipe.map { r -> async { tentar { completar(r.copy(codigoAno = r.codigoAno), v.anoModelo) }.getOrDefault(r) } }.awaitAll()
        v.copy(fipe = fipe)
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
     * Meses de referencia (mais recente primeiro). Usa a BrasilAPI e, se ela falhar,
     * a lista da API v2. Os codigos sao os mesmos nas duas fontes.
     */
    private suspend fun referencias(): List<ReferenciaV2> = travaRef.withLock {
        // Lista de meses vale por 6 horas (na virada do mes surge uma referencia nova)
        cacheReferencias?.takeIf { agora() - referenciasEm < 6 * HORA } ?: (
            tentar { comNovasTentativas(tentativas = 2) { brasilApi.referencias() }.map { ReferenciaV2(it.codigo.toString(), it.mes.trim()) } }
                .getOrNull()?.takeIf { it.isNotEmpty() }
                ?: run {
                    // BrasilAPI fora do ar: nao sera consultada mes a mes por 1 hora (consulta mais rapida)
                    brasilApiIndisponivelAte = agora() + HORA
                    v2.referencias()
                }
            )
            .sortedByDescending { it.codigo.toIntOrNull() ?: 0 }
            .also { cacheReferencias = it; referenciasEm = agora() }
    }

    /** Valor de um mes: BrasilAPI e, se falhar, API v2. Null se o mes nao existir. */
    private suspend fun valorNoMes(
        r: FipeResultado,
        codigoAno: String,
        ref: ReferenciaV2,
        estado: EstadoBusca,
        diagnostico: (String) -> Unit,
    ): PontoHistorico? {
        val ano = codigoAno.substringBefore('-')

        // Se a BrasilAPI ja falhou com erro de servidor nesta busca, nao insiste (falha rapida).
        (if (estado.brasilApiForaDoAr.get()) null else tentar { comNovasTentativas(tentativas = 2) { brasilApi.precos(r.codigoFipe, ref.codigo) } }
            .onFailure {
                diagnostico("${ref.codigo} ${ref.mes} brasilapi: ${descrever(it)}")
                if (it is IOException || (it is HttpException && it.code() >= 500)) estado.brasilApiForaDoAr.set(true)
            }
            .getOrNull())
            ?.let { lista -> escolherPreco(lista, ano.toIntOrNull(), r.combustivel) }
            ?.let { p ->
                val num = Dinheiro.paraDouble(p.valor)
                if (num != null) return PontoHistorico((p.mesReferencia ?: ref.mes).trim(), p.valor.orEmpty(), num)
            }

        val codigoRef = ref.codigo.toIntOrNull()
        val janela = limite402.takeIf { prefs.tokenFipe.isBlank() && agora() < limite402Ate }
        if (janela != null && codigoRef != null && codigoRef <= janela) {
            estado.exigemToken.incrementAndGet() // ja se sabe que este mes exige token
            return null
        }
        val doV2 = tentar { comNovasTentativas { v2.valorPorCodigo(r.tipo.caminhoV2, r.codigoFipe, codigoAno, ref.codigo) } }
            .onFailure {
                diagnostico("${ref.codigo} ${ref.mes} v2: ${descrever(it)}")
                if (it is HttpException && it.code() == 402) {
                    estado.exigemToken.incrementAndGet()
                    if (prefs.tokenFipe.isBlank() && codigoRef != null) synchronized(this) {
                        limite402 = maxOf(limite402 ?: codigoRef, codigoRef)
                        limite402Ate = agora() + HORA
                    }
                }
            }
            .getOrNull() ?: return null
        val num = Dinheiro.paraDouble(doV2.valor) ?: return null
        return PontoHistorico((doV2.mesReferencia ?: ref.mes).trim(), doV2.valor.orEmpty(), num)
    }

    /** Entre os anos do codigo, pega o do ano modelo (e combustivel, se houver mais de um). */
    private fun escolherPreco(lista: List<PrecoBrasilApi>, ano: Int?, combustivel: String): PrecoBrasilApi? {
        val doAno = lista.filter { it.anoModelo == ano }
        if (doAno.size <= 1) return doAno.firstOrNull()
        val chave = Texto.chave(combustivel).take(4)
        return doAno.firstOrNull { chave.isNotEmpty() && Texto.chave(it.combustivel.orEmpty()).contains(chave) } ?: doAno.first()
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
    ): ResultadoHistorico = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val codigoAno = resolverCodigoAno(r)
            ?: throw PlacaParser.ErroProvedor("Ano modelo não encontrado na tabela FIPE para este código.")
        val meses = Referencias.filtrar(referencias(), periodo, Referencias.ano(r.mesReferencia))
        val estado = EstadoBusca().apply { brasilApiForaDoAr.set(agora() < brasilApiIndisponivelAte) }
        var feitos = 0
        progresso(0, meses.size)
        val obtidos = java.util.concurrent.ConcurrentHashMap<String, PontoHistorico>()
        // Tempo limite total: depois dele, mostra o que ja chegou em vez de esperar indefinidamente.
        kotlinx.coroutines.withTimeoutOrNull(tempoLimiteHistoricoMs) {
            // Fila ordenada do mais recente para o mais antigo, consumida por 4 trabalhadores:
            // se o tempo acabar, ficam garantidamente os meses que mais importam.
            val fila = ArrayDeque(meses.asReversed())
            List(4) {
                async {
                    while (true) {
                        val ref = synchronized(fila) { fila.removeFirstOrNull() } ?: break
                        valorNoMes(r, codigoAno, ref, estado, diagnostico)?.let { obtidos[ref.codigo] = it }
                        synchronized(this@FipeRepositorio) { feitos++ }
                        progresso(feitos, meses.size)
                    }
                }
            }.awaitAll()
        } ?: diagnostico("tempo limite do historico atingido")
        val pontos = meses.mapNotNull { obtidos[it.codigo] }.toMutableList() // ordem cronologica
        // O valor atual (ja conhecido) entra como ultimo ponto quando a lista de meses ainda nao o inclui.
        val atual = Dinheiro.paraDouble(r.valor)?.takeIf { r.mesReferencia.isNotBlank() }
            ?.let { PontoHistorico(r.mesReferencia.trim(), r.valor.orEmpty(), it) }
        if (atual != null && pontos.none { Texto.chave(it.mesReferencia) == Texto.chave(atual.mesReferencia) }) pontos += atual
        if (pontos.isEmpty() && meses.isNotEmpty()) {
            throw PlacaParser.ErroProvedor(
                if (estado.exigemToken.get() > 0) "O histórico deste período exige um token da FIPE com acesso ao histórico (veja Ajustes)."
                else "Não foi possível obter o histórico agora. Verifique a conexão e tente novamente."
            )
        }
        // Mesmo mes vindo das duas fontes nao deve repetir
        val unicos = pontos.distinctBy { Meses.indice(it.mesReferencia) ?: Texto.chave(it.mesReferencia).hashCode() }
            .sortedBy { Meses.indice(it.mesReferencia) ?: Int.MAX_VALUE }
        ResultadoHistorico(unicos, meses.size, estado.exigemToken.get())
    }

    /** Estado compartilhado entre as consultas mensais de uma mesma busca. */
    private class EstadoBusca {
        val brasilApiForaDoAr = java.util.concurrent.atomic.AtomicBoolean(false)
        val exigemToken = java.util.concurrent.atomic.AtomicInteger(0)
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

    /** Tempo maximo de uma busca de historico inteira. */
    internal var tempoLimiteHistoricoMs = 20_000L

    /**
     * Como runCatching, mas sem engolir o cancelamento: uma consulta cancelada (o usuario limpou
     * a tela ou abriu outra) nao pode terminar "com sucesso" e sobrescrever o estado novo.
     */
    private inline fun <T> tentar(bloco: () -> T): Result<T> = try {
        Result.success(bloco())
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

    companion object {
        private const val HORA = 3_600_000L

        /** Valida o modelo de URL do provedor de placas; devolve o problema ou null se estiver ok. */
        fun validarUrlPlaca(url: String): String? = when {
            !url.trim().startsWith("https://") -> "Use um endereço https://"
            "{placa}" !in url -> "Inclua {placa} onde entra a placa"
            else -> null
        }

        fun mensagemErro(t: Throwable): String = when (t) {
            is PlacaParser.ErroProvedor -> t.message ?: "Erro na consulta."
            is HttpException -> when (t.code()) {
                429 -> "Limite diário de consultas da FIPE atingido. Informe um token da FIPE em Ajustes ou tente amanhã."
                404 -> "Não encontrado na tabela FIPE."
                401, 403 -> "Token da FIPE inválido. Confira em Ajustes."
                else -> "Erro no servidor (HTTP ${t.code()})."
            }
            is java.net.UnknownServiceException -> "O endereço do serviço precisa usar https. Confira em Ajustes."
            is com.google.gson.JsonParseException -> "O serviço respondeu num formato inesperado. Confira o endereço em Ajustes."
            is IllegalArgumentException -> "Endereço do serviço inválido. Confira em Ajustes."
            is java.io.InterruptedIOException -> "O serviço demorou demais para responder. Tente novamente."
            is IOException -> "Sem conexão com a internet ou servidor indisponível."
            else -> "Não foi possível concluir a consulta. Tente novamente."
        }
    }
}
