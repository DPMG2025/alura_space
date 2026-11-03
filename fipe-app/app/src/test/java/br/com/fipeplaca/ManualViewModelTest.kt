package br.com.fipeplaca

import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import br.com.fipeplaca.data.ConfigConexao
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.ItemFipe
import br.com.fipeplaca.data.TipoVeiculo
import br.com.fipeplaca.ui.Etapa
import br.com.fipeplaca.ui.ManualViewModel
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

/** Testes do ViewModel da Tabela FIPE com dependencias injetadas e servidor simulado. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ManualViewModelTest {
    private lateinit var servidor: MockWebServer
    private lateinit var app: FipeApp
    private lateinit var repo: FipeRepositorio
    private val pedidosMarcas = AtomicInteger(0)
    @Volatile private var foraDoAr = false

    private fun json(corpo: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(corpo)

    @Before fun iniciar() {
        servidor = MockWebServer()
        servidor.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (foraDoAr) return MockResponse().setResponseCode(503)
                return when (request.path) {
                    "/v1/carros/marcas" -> { pedidosMarcas.incrementAndGet(); json("""[{"codigo":"21","nome":"Fiat"}]""") }
                    "/v1/carros/marcas/21/modelos" -> json("""{"modelos":[{"codigo":1,"nome":"Uno"}],"anos":[]}""")
                    "/v1/carros/marcas/21/modelos/1/anos" -> json("""[{"codigo":"2012-1","nome":"2012 Gasolina"}]""")
                    "/v1/carros/marcas/21/modelos/1/anos/2012-1" -> json(
                        """{"Valor":"R$ 20.000,00","Marca":"Fiat","Modelo":"Uno","AnoModelo":2012,"Combustivel":"Gasolina",
                           "CodigoFipe":"001234-5","MesReferencia":"novembro de 2026"}""")
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        servidor.start()
        val url = "http://${servidor.hostName}:${servidor.port}"
        repo = FipeRepositorio(
            object : ConfigConexao { override val tokenPlaca = ""; override val urlPlaca = ""; override val tokenFipe = "" },
            baseV1 = "$url/v1/", baseV2 = "$url/v2/", baseBrasilApi = "$url/brasil/",
        ).apply { esperaInicialMs = 1 }
        app = RuntimeEnvironment.getApplication() as FipeApp
        app.salvos.limparTudo()
        app.salvos.aguardar()
    }

    @After fun parar() = servidor.shutdown()

    private fun esperar(descricao: String, condicao: () -> Boolean) {
        val limite = System.currentTimeMillis() + 8_000
        while (System.currentTimeMillis() < limite) {
            shadowOf(Looper.getMainLooper()).idle()
            app.salvos.aguardar()
            if (condicao()) return
            Thread.sleep(20)
        }
        throw AssertionError("Tempo esgotado: $descricao")
    }

    @Test fun marcasSoCarregamAoAbrirAAbaETrilhaCompleta() {
        val vm = ManualViewModel(repo, app.salvos, SavedStateHandle())
        Thread.sleep(200)
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(0, pedidosMarcas.get()) // nada de rede antes de abrir a aba
        vm.garantirCarregado()
        esperar("marcas") { vm.estado.value.marcas.isNotEmpty() }
        vm.selecionarMarca(vm.estado.value.marcas.single())
        esperar("modelos") { vm.estado.value.modelos.isNotEmpty() }
        vm.selecionarModelo(vm.estado.value.modelos.single())
        esperar("anos") { vm.estado.value.anos.isNotEmpty() }
        vm.selecionarAno(vm.estado.value.anos.single())
        esperar("valor") { vm.estado.value.resultado != null }
        assertEquals("R$ 20.000,00", vm.estado.value.resultado!!.valor)
        // Tocar no tipo ja ativo nao apaga o resultado
        vm.selecionarTipo(TipoVeiculo.CARROS)
        assertEquals(Etapa.RESULTADO, vm.estado.value.etapa)
    }

    @Test fun avisoDeFalhaAoAtualizarSalvoELimpoAoVoltar() {
        foraDoAr = true
        val vm = ManualViewModel(repo, app.salvos, SavedStateHandle())
        val r = FipeResultado(TipoVeiculo.CARROS, "Fiat", "Uno", "2012", "Gasolina", "001234-5", "outubro de 2026", "R$ 19.000,00", codigoAno = "2012-1")
        vm.abrirSalvo(ConsultaSalva("M:x", 1, resultado = r, marca = ItemFipe("21", "Fiat"), modelo = ItemFipe("1", "Uno"), ano = ItemFipe("2012-1", "2012 Gasolina")))
        esperar("aviso de falha") { vm.estado.value.avisoAtualizacao != null }
        assertEquals("R$ 19.000,00", vm.estado.value.resultado!!.valor) // mostra o valor guardado
        vm.voltar()
        assertNull(vm.estado.value.avisoAtualizacao)
        assertTrue(!vm.estado.value.atualizando)
    }

    @Test fun trilhaRestauradaDepoisDeEncerrarOApp() {
        val estado = SavedStateHandle()
        val vm = ManualViewModel(repo, app.salvos, estado)
        vm.garantirCarregado()
        esperar("marcas") { vm.estado.value.marcas.isNotEmpty() }
        vm.selecionarMarca(vm.estado.value.marcas.single())
        esperar("modelos") { vm.estado.value.modelos.isNotEmpty() }
        // Novo ViewModel com o mesmo estado salvo (processo recriado)
        val outro = ManualViewModel(repo, app.salvos, estado)
        assertEquals(Etapa.MODELOS, outro.estado.value.etapa)
        assertEquals("Fiat", outro.estado.value.marca!!.nome)
    }
}
