package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.dominio.EspecieMascote
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Mascote
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaCasa
import br.com.casinhadosaber.ui.telas.TelaCasaPorDentro
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Capturas da casa que cresce (prefixo s8_): a casa com 40, 130, 340 e 1000 fases vencidas, as áreas novas
 * (piscina, área do pet, observatório), uma área sendo construída, o quarto novo dentro da casa e o cachorrinho
 * passeando no jardim em dois momentos. Mesmo jeito das outras capturas: relógio parado, narrador e sons desligados.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasCasaQueCresceTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)

    /**
     * [fases] vencidas, as 40 peças colocadas e, se [construir], cada área ganha construída com todas as coisas
     * já ganhas no lugar de partida. O mascote é o cachorrinho, para ver que ele é diferente do cachorrinho da casa.
     */
    private fun estado(fases: Int, construir: Boolean = true): EstadoJogo {
        val estrelas = (0 until fases).associate { "${Trilha.entries[it % 4].name}:${it / 4}" to 3 }
        var e = EstadoJogo(perfil = menina, estrelas = estrelas, mascote = Mascote(EspecieMascote.CACHORRINHO, "Bolinha"))
        for (item in ItensDaCasa.ordem) e = e.colocar(item)
        if (construir) {
            for (item in e.itensGanhos.filter { it.area != null }) e = e.colocar(item)
        }
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

    private fun foto(nome: String) = regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")

    private fun capturar(nome: String, tempo: Long = 2500, conteudo: @Composable () -> Unit) {
        montar(conteudo)
        regra.mainClock.advanceTimeBy(tempo)
        foto(nome)
    }

    @Test fun casa40() = capturar("s8_01_casa_40_fases") { TelaCasa(estado(40), {}, {}, {}) }

    @Test fun casa130() = capturar("s8_02_casa_130_fases_lugares") { TelaCasa(estado(130), {}, {}, {}) }

    @Test fun piscina130() = capturar("s8_03_piscina_130_fases") { TelaCasa(estado(130), {}, {}, {}, paginaInicial = "piscina") }

    @Test fun pet340() = capturar("s8_04_area_do_pet_340_fases") { TelaCasa(estado(340), {}, {}, {}, paginaInicial = "pet") }

    @Test fun arvore340() = capturar("s8_05_casa_na_arvore_340_fases") { TelaCasa(estado(340), {}, {}, {}, paginaInicial = "arvore") }

    @Test fun casa1000() = capturar("s8_06_casa_1000_fases") { TelaCasa(estado(1000), {}, {}, {}) }

    @Test fun observatorio1000() = capturar("s8_07_observatorio_1000_fases") { TelaCasa(estado(1000), {}, {}, {}, paginaInicial = "observatorio") }

    @Test fun festa1000() = capturar("s8_08_salao_de_festas_1000_fases") { TelaCasa(estado(1000), {}, {}, {}, paginaInicial = "festa") }

    /** Save antigo com 60 fases: o baú já traz a obra do quarto novo, com o martelinho. */
    @Test fun saveAntigo60() = capturar("s8_09_save_antigo_60_fases_obra_no_bau") { TelaCasa(estado(60, construir = false), {}, {}, {}) }

    /** A piscina sendo construída: a criança toca na obra do baú e a cena vai até a área nova. */
    @Test
    fun obraDaPiscina() {
        var e by mutableStateOf(estado(127).let { s -> s.copy(colocados = s.colocados.filterKeys { !it.startsWith("piscina.") }) })
        montar { TelaCasa(e, { mudanca -> e = mudanca(e) }, {}, {}) }
        regra.mainClock.advanceTimeBy(1500)
        foto("s8_10_obra_no_bau")
        regra.onAllNodes(hasContentDescription("Piscina. Toque para colocar na casa.")).onFirst().performClick()
        regra.mainClock.advanceTimeBy(900)
        foto("s8_11_piscina_em_obra")
        regra.mainClock.advanceTimeBy(5000)
        foto("s8_12_piscina_construida")
        assertTrue("a obra da piscina foi guardada", "piscina.obra" in e.colocados)
        assertEquals(listOf("piscina"), e.itensNoBau.mapNotNull { it.area }.distinct())
    }

    /** Arrastar uma coisa dentro da área nova muda a posição guardada. */
    @Test
    fun arrastarNaArea() {
        var e by mutableStateOf(estado(130))
        montar { TelaCasa(e, { mudanca -> e = mudanca(e) }, {}, {}, paginaInicial = "piscina") }
        regra.mainClock.advanceTimeBy(1500)
        val antes = e.colocados.getValue("piscina.boia")
        regra.onAllNodes(hasContentDescription("Boia")).onFirst().performTouchInput {
            down(center)
            repeat(10) { moveBy(androidx.compose.ui.geometry.Offset(-width * 0.12f, -height * 0.2f)) }
            up()
        }
        regra.mainClock.advanceTimeBy(1500)
        val depois = e.colocados.getValue("piscina.boia")
        assertNotEquals(antes, depois)
        assertTrue("a boia foi para cima e para a esquerda", depois.x < antes.x && depois.y < antes.y)
    }

    /**
     * Tocar no lugar leva até ele: o título passa a ser o nome da área e as estrelas do cabeçalho contam o
     * progresso dela (o "x/y" fica só na descrição, visual 3D: nada de número abstrato para a criança).
     */
    @Test
    fun lugaresDaCasa() {
        montar { TelaCasa(estado(340), {}, {}, {}) }
        regra.mainClock.advanceTimeBy(1500)
        regra.onAllNodesWithTag("lugar_quarto2").onFirst().performClick()
        regra.mainClock.advanceTimeBy(1500)
        val area = CasaQueCresce.porId.getValue("quarto2")
        val progresso = CasaQueCresce.progresso(estado(340), area)
        assertTrue(regra.onAllNodes(androidx.compose.ui.test.hasText(area.nome)).fetchSemanticsNodes().isNotEmpty())
        // A pílula do lugar e as estrelas do cabeçalho dizem o progresso para a acessibilidade.
        assertTrue(regra.onAllNodes(hasContentDescription(progresso.rotulo)).fetchSemanticsNodes().size >= 2)
        foto("s8_13_quarto_novo_pelo_lugar")
    }

    /** O quarto novo dentro da casa: a portinha na lateral e o cômodo aberto. */
    @Test
    fun quartoNovoPorDentro() {
        var e by mutableStateOf(estado(80))
        montar { TelaCasaPorDentro(e, { mudanca -> e = mudanca(e) }, {}) }
        regra.mainClock.advanceTimeBy(2500)
        foto("s8_14_casa_por_dentro_com_porta_do_quarto_novo")
        regra.onAllNodesWithTag("comodo_novo_quarto2").onFirst().performClick()
        regra.mainClock.advanceTimeBy(1500)
        foto("s8_15_quarto_novo_aberto")
    }

    /** O cachorrinho da casa passeia no jardim: em dois momentos ele está em lugares (e jeitos) diferentes. */
    @Test
    fun cachorrinhoPasseando() {
        montar { TelaCasa(estado(40), {}, {}, {}) }
        val lugares = mutableListOf<Float>()
        regra.mainClock.advanceTimeBy(1000)
        repeat(16) {
            regra.mainClock.advanceTimeBy(1000)
            lugares += regra.onAllNodes(hasContentDescription("Cachorrinho")).onFirst().fetchSemanticsNode().boundsInRoot.left
            if (it == 1) foto("s8_16_cachorrinho_passeando_1")
            if (it == 6) foto("s8_17_cachorrinho_passeando_2")
        }
        assertTrue("o cachorrinho deveria andar: $lugares", lugares.max() - lugares.min() > 20f)
    }
}
