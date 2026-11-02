package br.com.fipeplaca

import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import br.com.fipeplaca.data.ConfigConexao
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.FipeRepositorio
import br.com.fipeplaca.ui.PlacaViewModel
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
import java.util.concurrent.TimeUnit

/** Testes do ViewModel da placa com o app real (Robolectric) e servidor HTTP simulado. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PlacaViewModelTest {
    private lateinit var servidor: MockWebServer
    private lateinit var app: FipeApp
    @Volatile private var atrasoPlacaSeg = 0L

    private fun json(corpo: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(corpo)

    @Before fun iniciar() {
        servidor = MockWebServer()
        servidor.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val p = request.path.orEmpty()
                return when {
                    p.startsWith("/placa/") -> json(
                        """{"MARCA":"VW","MODELO":"GOL","anoModelo":"2019","fipe":{"dados":[
                           {"codigo_fipe":"000001-1","ano_modelo":"2019","texto_modelo":"Gol A","texto_valor":"R$ 40.000,00","score":90,"mes_referencia":"novembro de 2026"},
                           {"codigo_fipe":"000002-2","ano_modelo":"2019","texto_modelo":"Gol B","texto_valor":"R$ 50.000,00","score":80,"mes_referencia":"novembro de 2026"}]}}"""
                    ).setBodyDelay(atrasoPlacaSeg, TimeUnit.SECONDS)
                    p.matches(Regex("/v2/cars/[0-9-]+/years")) -> json("""[{"code":"2019-1","name":"2019 Gasolina"}]""")
                    p.startsWith("/v2/cars/000001-1/years/2019-1") -> json("""{"price":"R$ 40.000,00","referenceMonth":"novembro de 2026"}""")
                    p.startsWith("/v2/cars/000002-2/years/2019-1") -> json("""{"price":"R$ 50.000,00","referenceMonth":"novembro de 2026"}""")
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        servidor.start()
        app = RuntimeEnvironment.getApplication() as FipeApp
        val url = "http://${servidor.hostName}:${servidor.port}"
        app.repositorio = FipeRepositorio(
            object : ConfigConexao {
                override val tokenPlaca = "tok"
                override val urlPlaca = "$url/placa/{placa}/{token}"
                override val tokenFipe = ""
            },
            baseV1 = "$url/v1/", baseV2 = "$url/v2/", baseBrasilApi = "$url/brasil/",
        )
        app.salvos.limparTudo()
        app.salvos.aguardar()
    }

    @After fun parar() = servidor.shutdown()

    /** Processa a fila da thread principal ate a condicao valer (a rede roda em outras threads). */
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

    @Test fun versaoEscolhidaEMantidaAoConsultarDeNovo() {
        val vm = PlacaViewModel(app, SavedStateHandle())
        vm.alterarPlaca("ABC1234")
        vm.consultar()
        esperar("primeira consulta") { vm.estado.value.veiculo != null && !vm.estado.value.carregando }
        assertEquals(0, vm.estado.value.selecionado)

        vm.selecionarVersao(1)
        esperar("versao gravada") { app.salvos.buscar(ConsultaSalva.idPlaca("ABC1234"))?.versao == 1 }
        val salvo = app.salvos.buscar(ConsultaSalva.idPlaca("ABC1234"))!!
        assertEquals("000002-2", salvo.principal!!.codigoFipe) // salvos mostram a versao escolhida

        vm.consultar()
        esperar("segunda consulta") { vm.estado.value.veiculo != null && !vm.estado.value.carregando }
        assertEquals(1, vm.estado.value.selecionado) // abre na versao ja escolhida
        val depois = app.salvos.buscar(ConsultaSalva.idPlaca("ABC1234"))!!
        assertEquals(1, depois.versao)
        // historico local so tem valores da versao 1 (R$ 50.000), sem misturar com a versao 0
        assertTrue(depois.historicoLocal.orEmpty().all { it.valor == 50000.0 })
    }

    @Test fun limparDuranteAConsultaNaoTrazResultadoAntigo() {
        atrasoPlacaSeg = 2
        val vm = PlacaViewModel(app, SavedStateHandle())
        vm.alterarPlaca("ABC1234")
        vm.consultar()
        assertTrue(vm.estado.value.carregando)
        vm.limpar()
        Thread.sleep(2_600)
        shadowOf(Looper.getMainLooper()).idle()
        app.salvos.aguardar()
        assertNull(vm.estado.value.veiculo)
        assertNull(app.salvos.buscar(ConsultaSalva.idPlaca("ABC1234"))) // consulta cancelada nao vai para os salvos
    }

    @Test fun placaDigitadaSobreviveAoEncerramentoDoApp() {
        val estadoSalvo = SavedStateHandle()
        PlacaViewModel(app, estadoSalvo).alterarPlaca("xyz9a87")
        // Novo ViewModel com o mesmo estado salvo (como depois de o sistema encerrar o processo)
        assertEquals("XYZ9A87", PlacaViewModel(app, estadoSalvo).estado.value.placa)
    }
}
