package br.com.casinhadosaber

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.FundoDecorado
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaInicio
import br.com.casinhadosaber.ui.telas.TelaPais
import br.com.casinhadosaber.ui.telas.TelaPerfil
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * Diagnóstico: cada teste mostra uma peça dos gráficos sozinha, em ordem. Se o emulador cair, o número de
 * testes concluídos no log do CI diz qual peça derrubou.
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class DiagnosticoTelasTest {

    @get:Rule
    val regra = createAndroidComposeRule<ComponentActivity>()

    @org.junit.After
    fun registrarMemoria() {
        val rt = Runtime.getRuntime()
        android.util.Log.w(
            "MEM",
            "java=${(rt.totalMemory() - rt.freeMemory()) / 1048576}MB max=${rt.maxMemory() / 1048576}MB " +
                "nativo=${android.os.Debug.getNativeHeapAllocatedSize() / 1048576}MB",
        )
    }

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)

    private fun mostrar(conteudo: @Composable () -> Unit) {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        val narrador = Narrador(contexto).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    Box(Modifier.fillMaxSize()) { conteudo() }
                }
            }
        }
        regra.waitForIdle()
        regra.mainClock.advanceTimeBy(1_500)
        regra.waitForIdle()
    }

    private fun casa(n: Int): EstadoJogo {
        var e = EstadoJogo(perfil = menina)
        for (item in ItensDaCasa.ordem.take(n)) e = e.colocar(item)
        return e
    }

    @Test fun a1_botao() = mostrar { BotaoFofo(onClick = {}) { Text("Oi") } }

    @Test fun a2_criancaParada() = mostrar { Crianca(menina, Modifier.height(200.dp), animado = false) }

    @Test fun a3_criancaAnimada() = mostrar { Crianca(menina, Modifier.height(200.dp)) }

    @Test fun a4_fundo() = mostrar { FundoDecorado(Cores.Ceu, Modifier.fillMaxSize()) }

    @Test fun a5_perfil() = mostrar { TelaPerfil(null) {} }

    @Test fun a6_inicioVazio() = mostrar { TelaInicio(casa(0), true, {}, {}, {}, {}) }

    @Test fun a7_inicioComCasa() = mostrar { TelaInicio(casa(20), true, {}, {}, {}, {}) }

    @Test fun a8_pais() = mostrar { TelaPais(casa(5), true, {}, true, {}, {}, {}, {}) }
}
