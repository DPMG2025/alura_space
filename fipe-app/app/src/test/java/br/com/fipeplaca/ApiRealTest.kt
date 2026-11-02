package br.com.fipeplaca

import br.com.fipeplaca.data.ConfigConexao
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.PeriodoHistorico
import br.com.fipeplaca.data.Texto
import br.com.fipeplaca.data.TipoVeiculo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * Verificacao contra as APIs reais da FIPE. So roda com FIPE_ONLINE=true (passo proprio no CI),
 * para confirmar que os formatos das respostas continuam os esperados.
 */
class ApiRealTest {
    private val repo = FipeRepositorio(object : ConfigConexao {
        override val tokenPlaca = ""
        override val urlPlaca = ""
        override val tokenFipe = System.getenv("FIPE_API_TOKEN").orEmpty()
    })

    @Before fun somenteOnline() = assumeTrue(System.getProperty("fipe.online") == "true")

    /** Relatorio gravado em build/outputs/api-real.txt (o CI versiona em docs/). */
    private fun log(texto: String) {
        println(texto)
        val f = java.io.File("build/outputs/api-real.txt")
        f.parentFile.mkdirs()
        f.appendText(texto + "\n")
    }

    private fun registrar(nome: String, bloco: suspend () -> Unit) = runBlocking {
        log("=== $nome ===")
        try {
            bloco()
            log("OK")
        } catch (t: Throwable) {
            log("FALHOU: ${t::class.java.name}: ${t.message}")
            if (t is retrofit2.HttpException) log("HTTP ${t.code()} corpo: ${t.response()?.errorBody()?.string()?.take(500)}")
            log(t.stackTraceToString().lines().take(12).joinToString("\n"))
            throw t
        }
    }

    @Test fun fluxoCompletoComHistorico() = registrar("fluxo completo") {
        val marcas = repo.marcas(TipoVeiculo.CARROS)
        log("Marcas de carros: ${marcas.size}")
        assertTrue(marcas.size > 50)
        val vw = marcas.first { Texto.chave(it.nome).contains("volkswagen") }
        val modelos = repo.modelos(TipoVeiculo.CARROS, vw.codigo)
        val gol = modelos.firstOrNull { Texto.chave(it.nome).startsWith("gol 1.0") } ?: modelos.first()
        val anos = repo.anos(TipoVeiculo.CARROS, vw.codigo, gol.codigo)
        val ano = anos.first { !it.codigo.startsWith("32000") }
        val r = repo.valor(TipoVeiculo.CARROS, vw.codigo, gol.codigo, ano.codigo)
        log("Valor: ${r.marca} ${r.modelo} ${r.anoModelo} ${r.combustivel} ${r.codigoFipe} = ${r.valor} (${r.mesReferencia})")
        assertTrue(r.valor!!.startsWith("R$"))
        assertTrue(r.codigoFipe.matches(Regex("\\d{6}-\\d")))

        // Respostas brutas da API v2, para conferir o formato
        val http = okhttp3.OkHttpClient()
        fun bruto(url: String) = http.newCall(okhttp3.Request.Builder().url(url).build()).execute().use { "HTTP ${it.code} " + it.body?.string()?.take(1500) }
        log("references: " + bruto("https://fipe.parallelum.com.br/api/v2/references").take(400))
        log("anos por codigo: " + bruto("https://fipe.parallelum.com.br/api/v2/cars/${r.codigoFipe}/years"))
        log("valor por codigo: " + bruto("https://fipe.parallelum.com.br/api/v2/cars/${r.codigoFipe}/years/${r.codigoAno}"))

        val hist = repo.historico(r, PeriodoHistorico.MESES_12, diagnostico = { log("  falha mes $it") })
        log("Historico 12 meses: ${hist.size} pontos")
        hist.forEach { log("  ${it.mesReferencia}: ${it.valorTexto}") }
        assertTrue(hist.size >= 6)
        val noAno = repo.historico(r, PeriodoHistorico.ANO_ATUAL)
        log("No ano: ${noAno.size} pontos")
        assertTrue(noAno.isNotEmpty())
    }

    @Test fun motosECaminhoes() = registrar("motos e caminhoes") {
        val motos = repo.marcas(TipoVeiculo.MOTOS)
        val caminhoes = repo.marcas(TipoVeiculo.CAMINHOES)
        log("Marcas de motos: ${motos.size}, caminhoes: ${caminhoes.size}")
        assertTrue(motos.any { Texto.chave(it.nome).contains("honda") })
        assertTrue(caminhoes.any { Texto.chave(it.nome).contains("scania") })
    }
}
