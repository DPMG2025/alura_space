package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.tema.TemaCasinha
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Arte 3D pré-renderizada (Fase 0 do PRD visual 3D): as imagens de teste de `assets/arte/` aparecem
 * numa captura, no tamanho de uso e reduzidas, sobre o céu em degradê das maquetes.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasArte3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val ids = listOf("estrela3d", "moeda", "patinho")

    @Test fun arteDeTeste() {
        // O manifesto e os WebP com alfa são lidos dos assets do app (falha aqui = arte não empacotada ou WebP não decodificado).
        val contexto = RuntimeEnvironment.getApplication()
        for (id in ids) {
            val info = ArteDoApp.info(contexto, id)
            assertNotNull("'$id' fora do manifesto de assets/arte", info)
            val bmp = ArteDoApp.bitmap(contexto, info!!, 1)
            assertNotNull("'$id' não decodificou", bmp)
            assertEquals(info.largura, bmp!!.width)
            assertTrue("'$id' deveria ter transparência", bmp.hasAlpha())
        }

        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF7FC8FF), Color(0xFFBFE3FF), Color(0xFFFFD6C4), Color(0xFFFFC2DC)))),
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
                    ) {
                        Text("Arte 3D de teste", fontSize = 30.sp, color = Color(0xFF3A2766))
                        // tamanho de uso do manifesto
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                            for (id in ids) ImagemDeArte(id, descricao = id)
                        }
                        // pequenas: decodificadas com redução (inSampleSize 2)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            for (id in ids) ImagemDeArte(id, Modifier.size(48.dp), descricao = "$id pequeno")
                        }
                        // grande, acima do tamanho de uso
                        ImagemDeArte("patinho", Modifier.size(220.dp), descricao = "patinho grande")
                        // id que não existe: mostra a reserva
                        ImagemDeArte("nao_existe", Modifier.size(48.dp)) { Text("🦆", fontSize = 32.sp) }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(500)
        for (id in ids) {
            assertEquals(1, regra.onAllNodesWithContentDescription(id).fetchSemanticsNodes().size)
            assertEquals(1, regra.onAllNodesWithContentDescription("$id pequeno").fetchSemanticsNodes().size)
        }
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_00_arte_de_teste.png")
    }
}
