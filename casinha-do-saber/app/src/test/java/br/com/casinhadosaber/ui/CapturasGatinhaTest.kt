package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.vestindo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Itens da gatinha fofa (orelhinhas, boné, laço grande, bigodes, máscara, fantasia e vestido de lacinhos). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasGatinhaTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val ids = listOf(
        "acessorioorelhasgato", "acessoriobonegato", "acessoriolacogrande", "acessoriobigodes",
        "acessoriomascaragata", "roupagatinha", "roupalacos", "acessoriobigodes",
    )

    @Test
    fun itensDaGatinhaExistemNaLoja() {
        for (id in ids) {
            val p = Loja.porId[id]
            assertTrue("$id não está na loja", p != null)
            assertTrue("$id sem nome em inglês", p!!.nomeEn.isNotBlank())
        }
    }

    @Test
    fun capturaDosItens() {
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                Column(Modifier.fillMaxSize().background(Cores.Fundo).padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (linha in ids.take(7).chunked(4)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (id in linha) {
                                val vestida = menina.vestindo(Loja.porId.getValue(id))
                                Crianca(vestida, Modifier.weight(1f).height(230.dp), animado = false)
                            }
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(1500)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s12_gatinha_itens.png")
    }
}
