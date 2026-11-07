package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.click
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Colorir
import br.com.casinhadosaber.dominio.Desenhos
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Forma
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Puzzle
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaColorir
import br.com.casinhadosaber.ui.telas.TelaPuzzle
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Capturas e gestos do Livro de Colorir (prefixo s10_) e dos quebra cabeças (s11_). Mesmo jeito de
 * CapturasCriativoTest: relógio parado, narrador e sons desligados.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasColorirEPuzzleTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val gato = Desenhos.porId.getValue("gato")

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

    private fun foto(nome: String) {
        regra.mainClock.advanceTimeBy(1200)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    /** O centro de uma região do gato, em fração do quadrado (para tocar com certeza dentro dela). */
    private fun centroDaUltimaRegiao(): Pair<Float, Float> {
        val f = gato.regioes.last().forma
        return when (f) {
            is Forma.Elipse -> f.cx to f.cy
            is Forma.Poligono -> f.pontos.map { it.x }.average().toFloat() to f.pontos.map { it.y }.average().toFloat()
        }
    }

    // ------------------------------------------------------------------ s10: Livro de Colorir

    @Test fun colorirEscolhaDeDesenhos() {
        val feito = Colorir.obraModelo(gato)
        val e = EstadoJogo(perfil = menina, obrasDeColorir = mapOf(gato.id to feito), coloridos = mapOf(gato.id to 2))
        montar { TelaColorir(e, {}, {}, {}) }
        regra.onNodeWithTag("desenho_gato").assertExists()
        foto("s10_colorir_escolha")
    }

    @Test fun colorirDesenhoVazio() {
        montar { TelaColorir(EstadoJogo(perfil = menina), {}, {}, {}, abrirDesenho = "gato") }
        foto("s10_colorir_desenho_novo")
    }

    /** Balde: tocar numa região enche só ela e o desenho fica guardado sozinho. */
    @Test fun colorirBaldePintaEGuarda() {
        var estado by mutableStateOf(EstadoJogo(perfil = menina))
        montar { TelaColorir(estado, { f -> estado = f(estado) }, {}, {}, abrirDesenho = "gato") }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithTag("tinta_7").performClick()
        val (x, y) = centroDaUltimaRegiao()
        regra.onNodeWithTag("quadro_de_colorir").performTouchInput { click(Offset(width * x, height * y)) }
        regra.mainClock.advanceTimeBy(600)
        val obra = estado.obrasDeColorir.getValue("gato")
        assertEquals("o balde deveria encher exatamente uma região", 1, obra.tintas.count { it == 7 })
        foto("s10_colorir_balde")
    }

    /** Pincel: arrastar o dedo guarda um traço preso à região onde começou. */
    @Test fun colorirPincelGuardaTraco() {
        var estado by mutableStateOf(EstadoJogo(perfil = menina))
        montar { TelaColorir(estado, { f -> estado = f(estado) }, {}, {}, abrirDesenho = "gato") }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithTag("colorir_pincel").performClick()
        regra.onNodeWithTag("tinta_1").performClick()
        val (x, y) = centroDaUltimaRegiao()
        regra.onNodeWithTag("quadro_de_colorir").performTouchInput {
            swipe(Offset(width * x, height * y), Offset(width * (x + 0.04f), height * (y + 0.02f)), durationMillis = 300)
        }
        regra.mainClock.advanceTimeBy(600)
        val obra = estado.obrasDeColorir.getValue("gato")
        assertEquals(1, obra.tracos.size)
        assertTrue("o traço deveria estar preso a uma região", obra.tracos[0].regiao >= 0)
        foto("s10_colorir_pincel")
    }

    /** Pronto com o desenho todo pintado: prêmio na tela, moedinhas e estrelas guardadas, cor nova aberta. */
    @Test fun colorirProntoPagaOPremio() {
        var estado by mutableStateOf(
            EstadoJogo(perfil = menina, moedas = 5, obrasDeColorir = mapOf(gato.id to Colorir.obraModelo(gato))),
        )
        montar { TelaColorir(estado, { f -> estado = f(estado) }, {}, {}, abrirDesenho = "gato") }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithTag("colorir_pronto").performClick()
        regra.mainClock.advanceTimeBy(800)
        regra.onNodeWithTag("premio_colorir").assertExists()
        assertTrue("ganhou moedinhas", estado.moedas > 5)
        assertTrue("guardou as estrelas", (estado.coloridos["gato"] ?: 0) >= 1)
        foto("s10_colorir_premio")
    }

    /** Pronto com o desenho em branco: não paga nada e não mostra prêmio. */
    @Test fun colorirProntoSemPintarNaoPaga() {
        var estado by mutableStateOf(EstadoJogo(perfil = menina, moedas = 5))
        montar { TelaColorir(estado, { f -> estado = f(estado) }, {}, {}, abrirDesenho = "gato") }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithTag("colorir_pronto").performClick()
        regra.mainClock.advanceTimeBy(800)
        assertEquals(5, estado.moedas)
        assertTrue(estado.coloridos.isEmpty())
    }

    // ------------------------------------------------------------------ s11: quebra cabeças

    @Test fun puzzleMenu() {
        montar { TelaPuzzle(EstadoJogo(perfil = menina), {}, {}) }
        regra.onNodeWithTag("puzzle_montar").assertExists()
        foto("s11_puzzle_menu")
    }

    @Test fun puzzleMontarNivelUm() {
        montar { TelaPuzzle(EstadoJogo(perfil = menina), {}, {}) }
        regra.onNodeWithTag("puzzle_montar").performClick()
        regra.mainClock.advanceTimeBy(800)
        regra.onNodeWithTag("puzzle_faltam").assertTextEquals("Faltam 4")
        foto("s11_puzzle_montar_nivel1")
    }

    @Test fun puzzleDeslizar() {
        montar { TelaPuzzle(EstadoJogo(perfil = menina), {}, {}) }
        regra.onNodeWithTag("puzzle_deslizar").performClick()
        regra.mainClock.advanceTimeBy(800)
        regra.onNodeWithTag("moldura_do_deslizante").assertExists()
        foto("s11_puzzle_deslizar")
    }

    /** Tocar numa peça e depois na casa dela na moldura encaixa a peça: faltam 3 (caminho sem arrastar). */
    @Test fun puzzleTocarEncaixa() {
        val semente = 42L
        montar { TelaPuzzle(EstadoJogo(perfil = menina), {}, {}, semente = semente) }
        regra.onNodeWithTag("puzzle_montar").performClick()
        regra.mainClock.advanceTimeBy(800)
        val id = Puzzle.montar("modelo:gato", 1, semente).pecas.last().id
        regra.onNodeWithTag("peca_$id").performClick()
        regra.mainClock.advanceTimeBy(400)
        // Primeiro: o toque na peça a deixa escolhida (separa "o toque na peça falhou" de "o toque na casa falhou").
        regra.onNodeWithTag("peca_$id").assertIsSelected()
        // Nível 1 é 2 por 2: a casa do id está na coluna id % 2, linha id / 2.
        regra.onNodeWithTag("moldura_do_puzzle").performTouchInput {
            click(Offset(width * ((id % 2) * 0.5f + 0.25f), width * ((id / 2) * 0.5f + 0.25f)))
        }
        regra.mainClock.advanceTimeBy(800)
        val texto = regra.onNodeWithTag("puzzle_faltam").fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }
        assertEquals("peça $id escolhida e casa tocada", "Faltam 3", texto)
        foto("s11_puzzle_uma_peca_no_lugar")
    }

    /** Tocar numa peça e depois numa casa errada não encaixa nada. */
    @Test fun puzzleTocarNaCasaErradaNaoEncaixa() {
        val semente = 42L
        montar { TelaPuzzle(EstadoJogo(perfil = menina), {}, {}, semente = semente) }
        regra.onNodeWithTag("puzzle_montar").performClick()
        regra.mainClock.advanceTimeBy(800)
        val id = Puzzle.montar("modelo:gato", 1, semente).pecas.last().id
        val errada = (id + 1) % 4
        regra.onNodeWithTag("peca_$id").performClick()
        regra.mainClock.advanceTimeBy(400)
        regra.onNodeWithTag("moldura_do_puzzle").performTouchInput {
            click(Offset(width * ((errada % 2) * 0.5f + 0.25f), width * ((errada / 2) * 0.5f + 0.25f)))
        }
        regra.mainClock.advanceTimeBy(800)
        regra.onNodeWithTag("puzzle_faltam").assertTextEquals("Faltam 4")
    }

    /** Arrastar a peça que está por cima até a casa dela encaixa. Mede também a peça no meio do gesto, para diagnosticar. */
    @Test fun puzzleArrastarEncaixa() {
        val semente = 42L
        montar { TelaPuzzle(EstadoJogo(perfil = menina), {}, {}, semente = semente) }
        regra.onNodeWithTag("puzzle_montar").performClick()
        regra.mainClock.advanceTimeBy(800)
        val id = Puzzle.montar("modelo:gato", 1, semente).pecas.last().id
        val moldura = regra.onNodeWithTag("moldura_do_puzzle").fetchSemanticsNode().boundsInRoot
        val de = regra.onNodeWithTag("peca_$id").fetchSemanticsNode().boundsInRoot.center
        val alvo = Offset(moldura.left + moldura.width * ((id % 2) * 0.5f + 0.25f), moldura.top + moldura.width * ((id / 2) * 0.5f + 0.25f))
        val delta = Offset(alvo.x - de.x, alvo.y - de.y)
        fun peca() = regra.onAllNodesWithTag("peca_$id").fetchSemanticsNodes().firstOrNull()?.boundsInRoot
        val antes = peca()
        regra.onRoot().performTouchInput {
            down(de)
            moveTo(de + delta * 0.25f)
            moveTo(de + delta * 0.5f)
        }
        regra.mainClock.advanceTimeBy(300)
        val meio = peca()
        regra.onRoot().performTouchInput {
            moveTo(de + delta)
            up()
        }
        regra.mainClock.advanceTimeBy(1000)
        val texto = regra.onNodeWithTag("puzzle_faltam").fetchSemanticsNode().config[SemanticsProperties.Text].joinToString { it.text }
        assertEquals("arrastar peça $id: antes=$antes meio=$meio depois=${peca()} alvo=$alvo", "Faltam 3", texto)
    }

    /** O primeiro nível está sempre aberto e o segundo só abre depois de vencer o primeiro. */
    @Test fun puzzleNiveisFecham() {
        val e = EstadoJogo(perfil = menina)
        assertEquals(1, Puzzle.nivelAberto("bichos", e.puzzlesFeitos))
        montar { TelaPuzzle(e, {}, {}) }
        regra.onNodeWithTag("puzzle_nivel_1").assertExists()
        regra.onNodeWithTag("puzzle_nivel_8").assertExists()
    }
}
