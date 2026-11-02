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

    @Test fun fluxoCompletoComHistorico() = runBlocking {
        val marcas = repo.marcas(TipoVeiculo.CARROS)
        println("Marcas de carros: ${marcas.size}")
        assertTrue(marcas.size > 50)
        val vw = marcas.first { Texto.chave(it.nome).contains("volkswagen") }
        val modelos = repo.modelos(TipoVeiculo.CARROS, vw.codigo)
        val gol = modelos.firstOrNull { Texto.chave(it.nome).startsWith("gol 1.0") } ?: modelos.first()
        val anos = repo.anos(TipoVeiculo.CARROS, vw.codigo, gol.codigo)
        val ano = anos.first { !it.codigo.startsWith("32000") }
        val r = repo.valor(TipoVeiculo.CARROS, vw.codigo, gol.codigo, ano.codigo)
        println("Valor: ${r.marca} ${r.modelo} ${r.anoModelo} ${r.combustivel} ${r.codigoFipe} = ${r.valor} (${r.mesReferencia})")
        assertTrue(r.valor!!.startsWith("R$"))
        assertTrue(r.codigoFipe.matches(Regex("\\d{6}-\\d")))

        val hist = repo.historico(r, PeriodoHistorico.MESES_12)
        println("Historico 12 meses: ${hist.size} pontos")
        hist.forEach { println("  ${it.mesReferencia}: ${it.valorTexto}") }
        assertTrue(hist.size >= 6)
        val noAno = repo.historico(r, PeriodoHistorico.ANO_ATUAL)
        println("No ano: ${noAno.size} pontos")
        assertTrue(noAno.isNotEmpty())
    }

    @Test fun motosECaminhoes() = runBlocking {
        val motos = repo.marcas(TipoVeiculo.MOTOS)
        val caminhoes = repo.marcas(TipoVeiculo.CAMINHOES)
        println("Marcas de motos: ${motos.size}, caminhoes: ${caminhoes.size}")
        assertTrue(motos.any { Texto.chave(it.nome).contains("honda") })
        assertTrue(caminhoes.any { Texto.chave(it.nome).contains("scania") })
    }
}
