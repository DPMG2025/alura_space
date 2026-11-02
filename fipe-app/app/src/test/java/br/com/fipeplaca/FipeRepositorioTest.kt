package br.com.fipeplaca

import br.com.fipeplaca.data.ConfigConexao
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.PeriodoHistorico
import br.com.fipeplaca.data.TipoVeiculo
import kotlinx.coroutines.launch
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

    private fun repo(token: String = "tok", tokenFipe: String = "", cache: java.io.File? = null) = FipeRepositorio(
        object : ConfigConexao {
            override val tokenPlaca = token
            override val urlPlaca = "http://${servidor.hostName}:${servidor.port}/placa/{placa}/{token}"
            override val tokenFipe = tokenFipe
        },
        baseV1 = servidor.url("/v1/").toString(),
        baseV2 = servidor.url("/v2/").toString(),
        baseBrasilApi = servidor.url("/brasil/").toString(),
        diretorioCache = cache,
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

    @Test fun historicoUsaV2QuandoBrasilApiFalha() = runTest {
        rotas = { p ->
            when {
                p == "/v2/references" -> json("""[{"code":"2","month":"fev/2026"},{"code":"1","month":"jan/2026"}]""")
                p.startsWith("/v2/cars/X/years/2019-1?reference=") -> json("""{"price":"R$ 9.000,00","referenceMonth":"r${p.last()}"}""")
                else -> json("{}", 500)
            }
        }
        val alvo = br.com.fipeplaca.data.FipeResultado(TipoVeiculo.CARROS, "", "", "2019", "", "X", "", null, codigoAno = "2019-1")
        val pontos = repo().apply { esperaInicialMs = 1 }.historico(alvo, PeriodoHistorico.MESES_12)
        assertEquals(2, pontos.size)
    }

    @Test fun historicoPelaBrasilApi() = runTest {
        rotas = { p ->
            when {
                p == "/brasil/fipe/tabelas/v1" -> json((339 downTo 328).joinToString(",", "[", "]") { "{\"codigo\":$it,\"mes\":\"m$it/2026 \"}" })
                p.startsWith("/brasil/fipe/preco/v1/005490-9?tabela_referencia=") -> {
                    val ref = p.substringAfter("=").toInt()
                    json("""[{"valor":"R$ 1,00","anoModelo":2022,"combustivel":"Flex","mesReferencia":"m$ref"},
                             {"valor":"R$ ${50000 + ref},00","anoModelo":2023,"combustivel":"Flex","mesReferencia":"m$ref de 2026 "}]""")
                }
                else -> MockResponse().setResponseCode(404)
            }
        }
        val alvo = br.com.fipeplaca.data.FipeResultado(TipoVeiculo.CARROS, "VW", "Gol", "2023", "Flex", "005490-9", "", null, codigoAno = "2023-5")
        val pontos = repo().historico(alvo, PeriodoHistorico.MESES_12)
        assertEquals(12, pontos.size)
        assertEquals(50339.0, pontos.last().valor, 0.0) // ano 2023 escolhido, mais recente no fim
        assertTrue(pedidos.none { it.path!!.startsWith("/v2/") })
    }

    @Test fun cenarioRealSemTokenFalhaRapidoEExplicaParcial() = runTest {
        rotas = { p ->
            when {
                p.startsWith("/brasil/") -> json("{}", 500) // proxy fora do ar
                p == "/v2/references" -> json((112 downTo 101).joinToString(",", "[", "]") { "{\"code\":\"$it\",\"month\":\"${it - 100}/2026\"}" })
                p.startsWith("/v2/cars/X/years/2019-1?reference=") -> {
                    val ref = p.substringAfter("=").toInt()
                    if (ref >= 110) json("""{"price":"R$ 9.000,00","referenceMonth":"r$ref"}""") else json("{}", 402)
                }
                else -> MockResponse().setResponseCode(404)
            }
        }
        val alvo = br.com.fipeplaca.data.FipeResultado(TipoVeiculo.CARROS, "", "", "2019", "", "X", "", null, codigoAno = "2019-1")
        val res = repo().apply { esperaInicialMs = 1 }.historico(alvo, PeriodoHistorico.MESES_12)
        assertEquals(3, res.size)
        assertEquals(12, res.solicitados)
        assertEquals(9, res.exigemToken)
        // Depois do primeiro erro 500, a BrasilAPI nao e mais consultada (2 tentativas x ate 4 em paralelo)
        assertTrue(pedidos.count { it.path!!.startsWith("/brasil/fipe/preco") } <= 8)
        // 402 nao e repetido e, depois do primeiro, os meses mais antigos nem sao pedidos (3 com valor + ate 4 em paralelo)
        assertTrue(pedidos.count { it.path!!.startsWith("/v2/cars/X/years/2019-1?reference=") } <= 7)
    }

    @Test fun historicoSemNenhumaFonteDaMensagemClara() = runTest {
        rotas = { p -> if (p == "/v2/references") json("""[{"code":"1","month":"jan/2026"},{"code":"2","month":"fev/2026"}]""") else json("{}", 402) }
        val alvo = br.com.fipeplaca.data.FipeResultado(TipoVeiculo.CARROS, "", "", "2019", "", "X", "", null, codigoAno = "2019-1")
        val e = runCatching { repo().historico(alvo, PeriodoHistorico.MESES_12) }.exceptionOrNull()!!
        assertTrue(FipeRepositorio.mensagemErro(e).contains("histórico"))
    }

    @Test fun consultaCanceladaNaoTerminaComSucesso() = kotlinx.coroutines.runBlocking {
        rotas = { p ->
            when {
                p.startsWith("/placa/") -> json("""{"MARCA":"VW","MODELO":"GOL","anoModelo":"2019","fipe":{"dados":[{"codigo_fipe":"X","ano_modelo":"2019"}]}}""")
                p.startsWith("/v2/cars/X/years") -> json("""[{"code":"2019-1","name":"2019"}]""").setBodyDelay(3, java.util.concurrent.TimeUnit.SECONDS)
                else -> MockResponse().setResponseCode(404)
            }
        }
        var terminou = false
        val job = launch(kotlinx.coroutines.Dispatchers.IO) { repo().consultarPlaca("ABC1234"); terminou = true }
        kotlinx.coroutines.delay(500)
        job.cancel()
        job.join()
        assertTrue(job.isCancelled)
        assertTrue("consulta cancelada nao pode concluir", !terminou)
    }

    @Test fun historicoRespeitaTempoLimiteEDevolveParcial() = kotlinx.coroutines.runBlocking {
        rotas = { p ->
            when {
                p == "/v2/references" -> json((112 downTo 101).joinToString(",", "[", "]") { "{\"code\":\"$it\",\"month\":\"${it - 100}/2026\"}" })
                p.endsWith("reference=112") -> json("""{"price":"R$ 9.000,00","referenceMonth":"12/2026"}""")
                p.contains("reference=") -> json("""{"price":"R$ 1,00"}""").setBodyDelay(5, java.util.concurrent.TimeUnit.SECONDS)
                else -> MockResponse().setResponseCode(404)
            }
        }
        val r = repo().apply { tempoLimiteHistoricoMs = 1_500 }
        val alvo = br.com.fipeplaca.data.FipeResultado(TipoVeiculo.CARROS, "", "", "2026", "", "X", "", null, codigoAno = "2026-1")
        val inicio = System.currentTimeMillis()
        val res = r.historico(alvo, PeriodoHistorico.MESES_12)
        assertTrue("demorou ${System.currentTimeMillis() - inicio} ms", System.currentTimeMillis() - inicio < 4_000)
        assertEquals(listOf(9000.0), res.map { it.valor })
    }

    @Test fun tabelaFuncionaSemInternetPeloCache() = runTest {
        val pasta = java.nio.file.Files.createTempDirectory("cache-fipe").toFile()
        rotas = { p -> if (p == "/v1/carros/marcas") json("""[{"codigo":"21","nome":"Fiat"}]""") else MockResponse().setResponseCode(404) }
        assertEquals("Fiat", repo(cache = pasta).marcas(TipoVeiculo.CARROS).single().nome)
        val url = servidor.url("/").toString()
        servidor.shutdown() // sem internet
        val offline = FipeRepositorio(
            object : ConfigConexao { override val tokenPlaca = ""; override val urlPlaca = ""; override val tokenFipe = "" },
            baseV1 = url + "v1/", baseV2 = url + "v2/", baseBrasilApi = url + "brasil/", diretorioCache = pasta,
        )
        assertEquals("Fiat", offline.marcas(TipoVeiculo.CARROS).single().nome)
    }

    @Test fun itensIncompletosSaoDescartados() = runTest {
        rotas = { p -> if (p == "/v1/carros/marcas") json("""[{"codigo":"1"},{"codigo":"21","nome":"Fiat"}]""") else MockResponse().setResponseCode(404) }
        assertEquals(listOf("Fiat"), repo().marcas(TipoVeiculo.CARROS).map { it.nome })
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
