package br.com.fipeplaca.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

/**
 * Historico de precos FIPE mes a mes: escolhe as fontes (BrasilAPI e API v2), respeita os meses que
 * exigem token (402), repete falhas passageiras, consulta os meses mais recentes primeiro e aplica
 * um tempo limite total, devolvendo o que chegou.
 */
class HistoricoFipe internal constructor(
    private val v2: FipeV2Api,
    private val brasilApi: BrasilApi,
    private val config: ConfigConexao,
    private val resolverCodigoAno: suspend (FipeResultado) -> String?,
) {
    private var cacheReferencias: List<ReferenciaV2>? = null
    private var referenciasEm = 0L
    @Volatile private var brasilApiIndisponivelAte = 0L
    /** Maior codigo de referencia que respondeu 402 sem token (meses ate ele nao sao pedidos de novo por 1 hora). */
    @Volatile private var limite402: Int? = null
    @Volatile private var limite402Ate = 0L
    private val travaRef = Mutex()

    /** Relogio, espera entre tentativas e tempo limite (substituiveis nos testes). */
    internal var agora: () -> Long = System::currentTimeMillis
    internal var esperaInicialMs = 1_000L
    internal var tempoLimiteHistoricoMs = 20_000L


    /**
     * Meses de referencia (mais recente primeiro). Usa a BrasilAPI e, se ela falhar,
     * a lista da API v2. Os codigos sao os mesmos nas duas fontes.
     */
    private suspend fun referencias(): List<ReferenciaV2> = travaRef.withLock {
        // Lista de meses vale por 6 horas (na virada do mes surge uma referencia nova)
        cacheReferencias?.takeIf { agora() - referenciasEm < 6 * HORA } ?: (
            tentar { comNovasTentativas(esperaInicialMs, tentativas = 2) { brasilApi.referencias() }.map { ReferenciaV2(it.codigo.toString(), it.mes.trim()) } }
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
        (if (estado.brasilApiForaDoAr.get()) null else tentar { comNovasTentativas(esperaInicialMs, tentativas = 2) { brasilApi.precos(r.codigoFipe, ref.codigo) } }
            .onFailure {
                diagnostico("${ref.codigo} ${ref.mes} brasilapi: ${descrever(it)}")
                if (it is IOException || (it is HttpException && it.code() >= 500)) {
                    estado.brasilApiForaDoAr.set(true)
                    brasilApiIndisponivelAte = agora() + HORA // renova: nao tenta de novo nas proximas buscas
                }
            }
            .getOrNull())
            ?.let { lista -> escolherPreco(lista, ano.toIntOrNull(), r.combustivel) }
            ?.let { p ->
                val num = Dinheiro.paraDouble(p.valor)
                if (num != null) return PontoHistorico((p.mesReferencia ?: ref.mes).trim(), p.valor.orEmpty(), num)
            }

        val codigoRef = ref.codigo.toIntOrNull()
        val janela = limite402.takeIf { config.tokenFipe.isBlank() && agora() < limite402Ate }
        if (janela != null && codigoRef != null && codigoRef <= janela) {
            estado.exigemToken.incrementAndGet() // ja se sabe que este mes exige token
            return null
        }
        val doV2 = tentar { comNovasTentativas(esperaInicialMs) { v2.valorPorCodigo(r.tipo.caminhoV2, r.codigoFipe, codigoAno, ref.codigo) } }
            .onFailure {
                diagnostico("${ref.codigo} ${ref.mes} v2: ${descrever(it)}")
                if (it is HttpException && it.code() == 429) estado.limiteAtingido.set(true)
                if (!(it is HttpException && it.code() in setOf(402, 404))) estado.falhasPassageiras.incrementAndGet()
                if (it is HttpException && it.code() == 402) {
                    estado.exigemToken.incrementAndGet()
                    if (config.tokenFipe.isBlank() && codigoRef != null) synchronized(this) {
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
        val terminou = kotlinx.coroutines.withTimeoutOrNull(tempoLimiteHistoricoMs) {
            // Fila ordenada do mais recente para o mais antigo, consumida por 4 trabalhadores:
            // se o tempo acabar, ficam garantidamente os meses que mais importam.
            val fila = ArrayDeque(meses.asReversed())
            List(4) {
                async {
                    while (true) {
                        val ref = synchronized(fila) { fila.removeFirstOrNull() } ?: break
                        valorNoMes(r, codigoAno, ref, estado, diagnostico)?.let { obtidos[ref.codigo] = it }
                        val agoraFeitos = synchronized(this@HistoricoFipe) { ++feitos }
                        progresso(agoraFeitos, meses.size)
                    }
                }
            }.awaitAll()
        } != null
        if (!terminou) diagnostico("tempo limite do historico atingido")
        val pontos = meses.mapNotNull { obtidos[it.codigo] }.toMutableList() // ordem cronologica
        // O valor atual (ja conhecido) entra como ultimo ponto quando a lista de meses ainda nao o inclui.
        val atual = Dinheiro.paraDouble(r.valor)?.takeIf { r.mesReferencia.isNotBlank() }
            ?.let { PontoHistorico(r.mesReferencia.trim(), r.valor.orEmpty(), it) }
        if (atual != null && pontos.none { Texto.chave(it.mesReferencia) == Texto.chave(atual.mesReferencia) }) pontos += atual
        if (pontos.isEmpty() && meses.isNotEmpty()) {
            throw PlacaParser.ErroProvedor(
                when {
                    estado.exigemToken.get() > 0 -> "O histórico deste período exige um token da FIPE com acesso ao histórico (veja Ajustes)."
                    estado.limiteAtingido.get() -> "Limite de consultas da FIPE atingido por agora. Tente novamente mais tarde."
                    !terminou -> "A FIPE demorou demais para responder. Tente novamente."
                    else -> "Não foi possível obter o histórico agora. Verifique a conexão e tente novamente."
                }
            )
        }
        // Mesmo mes vindo das duas fontes nao deve repetir
        val unicos = pontos.distinctBy { Meses.indice(it.mesReferencia) ?: Texto.chave(it.mesReferencia).hashCode() }
            .sortedBy { Meses.indice(it.mesReferencia) ?: Int.MAX_VALUE }
        ResultadoHistorico(unicos, meses.size, estado.exigemToken.get(), completo = terminou && estado.falhasPassageiras.get() == 0)
    }

    /** Estado compartilhado entre as consultas mensais de uma mesma busca. */
    private class EstadoBusca {
        val brasilApiForaDoAr = java.util.concurrent.atomic.AtomicBoolean(false)
        val exigemToken = java.util.concurrent.atomic.AtomicInteger(0)
        val falhasPassageiras = java.util.concurrent.atomic.AtomicInteger(0)
        val limiteAtingido = java.util.concurrent.atomic.AtomicBoolean(false)
    }

}
