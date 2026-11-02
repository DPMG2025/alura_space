package br.com.fipeplaca

import br.com.fipeplaca.data.ConfigConexao
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.PeriodoHistorico
import br.com.fipeplaca.data.TipoVeiculo
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

/** Testa o repositorio contra um servidor HTTP simulado (FIPE v1, v2 e provedor de placas). */
class FipeRepositorioTest {
    private lateinit var servidor: MockWebServer
    private val pedidos = CopyOnWriteArrayList<RecordedRequest>()
    private var rotas: (String) -> MockResponse = { MockResponse().setResponseCode(404) }

    private fun json(corpo: String, codigo: Int = 200) =
        MockResponse().setResponseCode(codigo).setHeader("Content-Type", "application/json").setBody(corpo)

    private fun repo(token: String = "tok", tokenFipe: String = "") = FipeRepositorio(
        object : ConfigConexao {
            override val tokenPlaca = token
            override val urlPlaca = "http://${servidor.hostName}:${servidor.port}/placa/{placa}/{token}"
            override val tokenFipe = tokenFipe
        },
        baseV1 = servidor.url("/v1/").toString(),
        baseV2 = servidor.url("/v2/").toString(),
        baseOficial = servidor.url("/oficial/").toString(),
    )

    @Before fun iniciar() {
        servidor = MockWebServer()
        servidor.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                pedidos += request
                return rotas(request.path.orEmpty())
            }
        }
        servidor.start()
    }

    @After fun parar() = servidor.shutdown()

    @Test fun buscaManualCompleta() = runTest {
        rotas = { p ->
            when {
                p == "/v1/carros/marcas" -> json("""[{"codigo":"59","nome":"VW - VolksWagen"},{"codigo":"21","nome":"Fiat"}]""")
                p == "/v1/carros/marcas/59/modelos" -> json("""{"modelos":[{"codigo":5940,"nome":"Gol 1.0"}],"anos":[]}""")
                p == "/v1/carros/marcas/59/modelos/5940/anos" -> json("""[{"codigo":"32000-1","nome":"32000 Gasolina"},{"codigo":"2019-1","nome":"2019 Gasolina"}]""")
                p == "/v1/carros/marcas/59/modelos/5940/anos/2019-1" -> json(
                    """{"TipoVeiculo":1,"Valor":"R$ 48.512,00","Marca":"VW - VolksWagen","Modelo":"Gol 1.0","AnoModelo":2019,
                       "Combustivel":"Gasolina","CodigoFipe":"005340-6","MesReferencia":"novembro de 2026 ","SiglaCombustivel":"G"}""")
                else -> MockResponse().setResponseCode(404)
            }
        }
        val r = repo()
        val marcas = r.marcas(TipoVeiculo.CARROS)
        assertEquals(listOf("Fiat", "VW - VolksWagen"), marcas.map { it.nome }) // ordenadas
        assertEquals("5940", r.modelos(TipoVeiculo.CARROS, "59").single().codigo) // numero vira texto
        assertEquals("Zero KM Gasolina", r.anos(TipoVeiculo.CARROS, "59", "5940").first().nome)
        val v = r.valor(TipoVeiculo.CARROS, "59", "5940", "2019-1")
        assertEquals("R$ 48.512,00", v.valor)
        assertEquals("2019-1", v.codigoAno)
        assertEquals("novembro de 2026", v.mesReferencia)
        r.marcas(TipoVeiculo.CARROS)
        assertEquals(1, pedidos.count { it.path == "/v1/carros/marcas" }) // cache de marcas
    }

    @Test fun placaCompletaValorPeloCodigoFipe() = runTest {
        rotas = { p ->
            when {
                p.startsWith("/placa/RTA2C19/tok") -> json(
                    """{"MARCA":"VW","MODELO":"GOL 1.0","anoModelo":"2019","fipe":{"dados":[
                       {"codigo_fipe":"005340-6","ano_modelo":"2019","combustivel":"Gasolina","score":90,"texto_modelo":"Gol 1.0"}]}}""")
                p == "/v2/cars/005340-6/years" -> json("""[{"code":"2020-1","name":"2020 Gasolina"},{"code":"2019-1","name":"2019 Gasolina"}]""")
                p == "/v2/cars/005340-6/years/2019-1" -> json(
                    """{"brand":"VW - VolksWagen","codeFipe":"005340-6","fuel":"Gasolina","model":"Gol 1.0","modelYear":2019,
                       "price":"R$ 48.512,00","referenceMonth":"novembro de 2026","vehicleType":1}""")
                else -> MockResponse().setResponseCode(404)
            }
        }
        val v = repo().consultarPlaca("rta-2c19")
        val f = v.fipe.single()
        assertEquals("R$ 48.512,00", f.valor)
        assertEquals("2019-1", f.codigoAno)
        assertEquals("VW - VolksWagen", f.marca)
        assertEquals("novembro de 2026", f.mesReferencia)
    }

    @Test fun historicoIgnoraMesesAusentesEUsaToken() = runTest {
        val refs = (300 downTo 277).joinToString(",", "[", "]") { c ->
            val mesesAtras = 300 - c
            val mes = 11 - mesesAtras // novembro = 11
            val ano = 2026 + Math.floorDiv(mes - 1, 12)
            val m = Math.floorMod(mes - 1, 12) + 1
            """{"code":"$c","month":"$m/$ano"}"""
        }
        rotas = { p ->
            when {
                p == "/v2/references" -> json(refs)
                p.startsWith("/v2/cars/005340-6/years/2019-1?reference=") -> {
                    val ref = p.substringAfter("reference=").toInt()
                    if (ref == 295) MockResponse().setResponseCode(404) // mes sem o veiculo na tabela
                    else json("""{"price":"R$ ${40000 + (ref - 277) * 100},00","referenceMonth":"ref $ref"}""")
                }
                else -> MockResponse().setResponseCode(404)
            }
        }
        val r = repo(tokenFipe = "segredo")
        val alvo = br.com.fipeplaca.data.FipeResultado(TipoVeiculo.CARROS, "VW", "Gol", "2019", "Gasolina", "005340-6", "", null, codigoAno = "2019-1")
        var ultimoProgresso = 0f
        val doze = r.historico(alvo, PeriodoHistorico.MESES_12) { f, t -> ultimoProgresso = f.toFloat() / t }
        assertEquals(doze.map { it.mesReferencia }.toString(), 11, doze.size) // 12 meses menos o ausente
        assertEquals("ref 300", doze.last().mesReferencia) // ordem cronologica
        assertTrue(doze.zipWithNext().all { (a, b) -> a.valor < b.valor })
        assertEquals(1f, ultimoProgresso)
        assertEquals(10, r.historico(alvo, PeriodoHistorico.ANO_ATUAL).size) // jan a nov de 2026 (11) menos o ausente (295 = jun)
        assertEquals(23, r.historico(alvo, PeriodoHistorico.MESES_24).size)
        assertTrue(pedidos.filter { it.path!!.startsWith("/v2/cars") }.all { it.getHeader("X-Subscription-Token") == "segredo" })
        assertEquals(1, pedidos.count { it.path == "/v2/references" }) // referencias em cache
    }

    @Test fun historicoRecuperaLimitePassageiro() = runTest {
        val tentativas = java.util.concurrent.ConcurrentHashMap<String, Int>()
        rotas = { p ->
            when {
                p == "/v2/references" -> json((112 downTo 101).joinToString(",", "[", "]") { "{\"code\":\"$it\",\"month\":\"${it - 100}/2026\"}" })
                p.startsWith("/v2/cars/X/years/2019-1?reference=") -> {
                    val n = tentativas.merge(p, 1, Int::plus)!!
                    if (n < 3) json("{}", 429) else json("{\"price\":\"R$ 10.000,00\",\"referenceMonth\":\"${p.substringAfter("reference=")}\"}")
                }
                else -> MockResponse().setResponseCode(404)
            }
        }
        val r = repo().apply { esperaInicialMs = 1 }
        val alvo = br.com.fipeplaca.data.FipeResultado(TipoVeiculo.CARROS, "", "", "2019", "", "X", "", null, codigoAno = "2019-1")
        val pontos = r.historico(alvo, PeriodoHistorico.MESES_12)
        assertEquals(12, pontos.size) // todos recuperados apos duas respostas 429
    }

    @Test fun historicoPeloServicoOficial() = runTest {
        rotas = { p ->
            when (p) {
                "/oficial/ConsultarTabelaDeReferencia" ->
                    json((339 downTo 316).joinToString(",", "[", "]") { "{\"Codigo\":$it,\"Mes\":\"mes$it/2026 \"}" })
                "/oficial/ConsultarValorComTodosParametros" -> MockResponse().setResponseCode(200) // respondido abaixo
                else -> if (p.startsWith("/v2/cars/005490-9/years/2023-5?reference=338"))
                    json("""{"price":"R$ 54.401,00","referenceMonth":"mes338/2026"}""")
                else MockResponse().setResponseCode(402)
            }
        }
        // O corpo do POST define a resposta do servico oficial
        servidor.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                pedidos += request
                if (request.path == "/oficial/ConsultarValorComTodosParametros") {
                    val corpo = request.body.clone().readUtf8()
                    listOf("anoModelo=2023", "codigoTipoCombustivel=5", "modeloCodigoExterno=005490-9", "tipoConsulta=codigo",
                        "tipoVeiculo=carro", "codigoTipoVeiculo=1").forEach { require(it in corpo) { "faltou $it em $corpo" } }
                    val ref = Regex("codigoTabelaReferencia=(\\d+)").find(corpo)!!.groupValues[1].toInt()
                    return if (ref == 338) json("""{"codigo":"0","erro":"nadaencontrado"}""")
                    else json("""{"Valor":"R$ ${50000 + ref},00","MesReferencia":"mes$ref/2026 ","CodigoFipe":"005490-9"}""")
                }
                return rotas(request.path.orEmpty())
            }
        }
        val alvo = br.com.fipeplaca.data.FipeResultado(TipoVeiculo.CARROS, "VW", "Gol", "2023", "Flex", "005490-9", "", null, codigoAno = "2023-5")
        val falhas = mutableListOf<String>()
        val pontos = repo().historico(alvo, PeriodoHistorico.MESES_24, diagnostico = { falhas += it })
        assertEquals(24, pontos.size) // 23 do oficial + 1 recuperado na v2
        assertEquals("R$ 54.401,00", pontos.first { it.mesReferencia == "mes338/2026" }.valorTexto)
        val post = pedidos.first { it.path == "/oficial/ConsultarValorComTodosParametros" }
        assertEquals("https://veiculos.fipe.org.br/", post.getHeader("Referer"))
        assertTrue(falhas.any { it.contains("nadaencontrado") })
    }

    @Test fun historicoSemNenhumaFonteDaMensagemClara() = runTest {
        rotas = { p -> if (p == "/v2/references") json("""[{"code":"1","month":"jan/2026"},{"code":"2","month":"fev/2026"}]""") else json("{}", 402) }
        val alvo = br.com.fipeplaca.data.FipeResultado(TipoVeiculo.CARROS, "", "", "2019", "", "X", "", null, codigoAno = "2019-1")
        val e = runCatching { repo().historico(alvo, PeriodoHistorico.MESES_12) }.exceptionOrNull()!!
        assertTrue(FipeRepositorio.mensagemErro(e).contains("histórico"))
    }

    @Test fun errosAmigaveis() = runTest {
        rotas = { p -> if (p.startsWith("/placa/")) json("""{"message":"x"}""", 401) else json("{}", 429) }
        val e1 = runCatching { repo().consultarPlaca("ABC1234") }.exceptionOrNull()!!
        assertTrue(FipeRepositorio.mensagemErro(e1).contains("Token do serviço de placas"))
        val e2 = runCatching { repo().marcas(TipoVeiculo.MOTOS) }.exceptionOrNull()!!
        assertTrue(FipeRepositorio.mensagemErro(e2).contains("Limite"))
        servidor.shutdown()
        val e3 = runCatching { repo().marcas(TipoVeiculo.CAMINHOES) }.exceptionOrNull()!!
        assertTrue(FipeRepositorio.mensagemErro(e3).contains("conexão"))
    }

    @Test fun placaSemTokenNaoConsulta() {
        assertTrue(!repo(token = "").temTokenPlaca())
    }
}
