package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dados.GuardaDeFalhas
import br.com.casinhadosaber.dominio.RegistroDeFalhas
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.CartaoDeFechamentos
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Registro de fechamentos do app: gravar no aparelho, mostrar na área dos pais e apagar. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasFechamentosTest {

    @get:Rule
    val regra = createComposeRule()

    private val app get() = RuntimeEnvironment.getApplication()

    @Before
    fun limpar() = GuardaDeFalhas.limpar(app)

    private fun montar() {
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                Column(Modifier.fillMaxSize().background(Cores.Fundo).padding(20.dp)) { CartaoDeFechamentos() }
            }
        }
        regra.mainClock.advanceTimeBy(600)
    }

    @Test fun semFechamentoOCartaoNaoAparece() {
        montar()
        regra.onAllNodesWithTag("cartao_fechamentos").assertCountEquals(0)
    }

    @Test fun fechamentoGuardadoApareceEApagaSome() {
        val erro = OutOfMemoryError("Failed to allocate a 33177612 byte allocation")
        GuardaDeFalhas.anotar(app, RegistroDeFalhas.montar(1_762_610_000_000L, "Premio MATEMATICA fase 10", erro))
        GuardaDeFalhas.anotar(app, RegistroDeFalhas.montar(1_762_611_000_000L, "Album", IllegalStateException("x")))
        val lidas = GuardaDeFalhas.ler(app)
        assertEquals(2, lidas.size)
        assertEquals("o mais novo primeiro", "Album", lidas[0].tela)
        assertTrue(lidas[1].tipo.endsWith("OutOfMemoryError"))
        montar()
        regra.onNodeWithTag("cartao_fechamentos").assertExists()
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s7_fechamentos.png")
        regra.onNodeWithTag("apagar_fechamentos").performClick()
        regra.mainClock.advanceTimeBy(600)
        regra.onAllNodesWithTag("cartao_fechamentos").assertCountEquals(0)
        assertTrue(GuardaDeFalhas.ler(app).isEmpty())
    }
}
