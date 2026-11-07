package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.AparenciaDaCasa
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.CamadasDaCasa
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.ComposicaoDaCasa
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaInicio
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
import kotlin.random.Random

/**
 * Casa de fora e tela inicial em camadas 3D (Fase 3 do PRD visual 3D). Capturas novas, para comparar com a
 * maquete `docs/visual-3d/maquetes/1_inicio.png`: `v3d_04_inicio` e `v3d_05_casa_em_camadas` (as etapas da
 * construção, a noite, a chuva, a neve e as cores da loja lado a lado). As capturas 03, 12, 13, 14, 14b, 20 e 20b
 * (`CapturasTest`) mostram a casa nova sem mudar de código.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasCasa3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val gerador = GeradorDeFases(Random(4))

    /** Estado com [n] fases vencidas, espalhadas pelas trilhas, e os [colocados] primeiros itens na casa. */
    private fun estado(n: Int, colocados: Int): EstadoJogo {
        var e = EstadoJogo(perfil = menina)
        val fases = (0 until 10).flatMap { i -> Trilha.entries.map { gerador.fases(it)[i] } }
        for ((k, f) in fases.take(n).withIndex()) e = e.registrarVitoria(f, 1 + k % 3).first
        for (item in ItensDaCasa.ordem.take(colocados)) e = e.colocar(item)
        return e
    }

    private fun montar(conteudo: @Composable () -> Unit) {
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    Box(Modifier.fillMaxSize()) { conteudo() }
                }
            }
        }
    }

    /** A casa é montada pelas camadas (e não pelo desenho antigo): o composto sai, com a casa opaca e o céu vazio. */
    private fun conferirComposto(e: EstadoJogo) {
        val contexto = RuntimeEnvironment.getApplication()
        assertTrue("arte da casa incompleta no manifesto", CamadasDaCasa.disponivel(ArteDoApp.manifesto(contexto)))
        val composto = ComposicaoDaCasa.composto(contexto, CamadasDaCasa.camadas(AparenciaDaCasa.de(e, noite = false)), 400)
        assertNotNull("a casa em camadas não foi composta (WebP ilegível?)", composto)
        val bmp = composto!!.asAndroidBitmap()
        assertEquals(400, bmp.width)
        // O chão desce abaixo da cena (jardim de baixo): 1320 / 800 da largura.
        assertEquals(660, bmp.height)
        assertEquals("o céu da pilha é transparente (o degradê é do app)", 0, android.graphics.Color.alpha(bmp.getPixel(200, 10)))
        // Parede do quarto (meio do piso de cima) e grama do jardim: opacas.
        assertEquals(255, android.graphics.Color.alpha(bmp.getPixel(120, 200)))
        assertEquals(255, android.graphics.Color.alpha(bmp.getPixel(200, 600)))
    }

    @Test fun inicio() {
        val e = estado(20, 24)
        conferirComposto(e)
        montar { TelaInicio(e, true, {}, {}, {}, {}) }
        regra.mainClock.advanceTimeBy(2500)
        // Nada de número abstrato para a criança: nem "x/250" nem "x de 40 coisas" na tela.
        assertTrue("texto com '/' na tela inicial", regra.onAllNodes(hasText("/", substring = true)).fetchSemanticsNodes().isEmpty())
        assertTrue("texto 'de 250' na tela inicial", regra.onAllNodes(hasText("de 250", substring = true)).fetchSemanticsNodes().isEmpty())
        assertTrue("texto 'coisas' na tela inicial", regra.onAllNodes(hasText("coisas", substring = true)).fetchSemanticsNodes().isEmpty())
        // Os quatro caminhos e a casinha continuam tocáveis (os testes do emulador procuram por estes nomes).
        for (t in Trilha.entries) {
            assertTrue("falta o caminho ${t.titulo}", regra.onAllNodes(hasContentDescription("Caminho ${t.titulo}", substring = true)).fetchSemanticsNodes().isNotEmpty())
        }
        assertTrue(regra.onAllNodes(hasContentDescription("Minha casinha", substring = true)).fetchSemanticsNodes().isNotEmpty())
        // Fase 9: o espelho (agora no cabeçalho) não cobre o sol, e a criança fica em primeiro plano, com o dobro do
        // tamanho da casa de montar (mais de 22% da largura, como na maquete 1).
        val espelho = regra.onAllNodes(hasContentDescription("Espelho: trocar a aparência")).fetchSemanticsNodes().map { it.boundsInRoot }
        val sol = regra.onAllNodes(hasContentDescription("Sol"), useUnmergedTree = true).fetchSemanticsNodes().map { it.boundsInRoot }
        assertTrue("o espelho devia estar na tela", espelho.isNotEmpty())
        for (e in espelho) for (s in sol) assertTrue("o espelho cobre o sol: $e x $s", !e.overlaps(s))
        val largura = regra.onRoot().fetchSemanticsNode().boundsInRoot.width
        val crianca = regra.onAllNodes(hasContentDescription("Ana"), useUnmergedTree = true).fetchSemanticsNodes().map { it.boundsInRoot }
        assertTrue("a criança devia estar na casinha", crianca.isNotEmpty())
        assertTrue("a criança do início está pequena: ${crianca.first()}", crianca.first().width >= largura * 0.22f)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_04_inicio.png")
    }

    @Test fun casaEmCamadas() {
        val completa = estado(40, 40)
        conferirComposto(completa)
        val quadros = listOf(
            estado(0, 0),
            estado(1, 1),
            estado(2, 2),
            estado(3, 3),
            estado(4, 4),
            completa,
            completa.copy(clima = 1, corTelhado = 1, corPorta = 1, papelDeParede = 1),
            completa.copy(clima = 2, corTelhado = 2, corPorta = 3, papelDeParede = 2),
            completa.copy(corTelhado = 5, corPorta = 4, papelDeParede = 3),
        )
        montar {
            Column {
                for (linha in quadros.withIndex().chunked(3)) {
                    Row {
                        for ((i, q) in linha) {
                            // 130 x 149,5 dp: a proporção da cena (1,15). A sexta é de noite.
                            Box(Modifier.size(131.dp, 150.65.dp)) {
                                CenaDaCasa(q, Modifier.fillMaxSize(), noite = i == 5 || i == 8)
                            }
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(1200)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_05_casa_em_camadas.png")
    }
}
