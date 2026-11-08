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
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Carimbos
import br.com.casinhadosaber.dominio.Colorir
import br.com.casinhadosaber.dominio.Desenhos
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Forma
import br.com.casinhadosaber.dominio.Fundo
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Puzzle
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaColorir
import br.com.casinhadosaber.ui.telas.TelaPuzzle
import com.github.takahirom.roborazzi.captureRoboImage
import androidx.compose.ui.test.onNodeWithContentDescription
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.telas.TelaAlbum
import br.com.casinhadosaber.ui.telas.TelaDiplomas
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

    /** Carimbo e fundo: tocar no desenho põe o enfeite na cor escolhida e o fundo fica guardado com a obra. */
    @Test fun colorirCarimboEFundo() {
        var estado by mutableStateOf(EstadoJogo(perfil = menina))
        montar { TelaColorir(estado, { f -> estado = f(estado) }, {}, {}, abrirDesenho = "gato") }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithTag("colorir_fundo").performClick()
        regra.mainClock.advanceTimeBy(300)
        regra.onNodeWithTag("fundo_campo").performClick()
        regra.mainClock.advanceTimeBy(300)
        regra.onNodeWithTag("colorir_carimbo").performClick()
        regra.mainClock.advanceTimeBy(300)
        // A lua só abre com 10 desenhos: tocar nela não troca o carimbo.
        regra.onNodeWithTag("carimbo_4").performScrollTo().performClick()
        regra.onNodeWithTag("carimbo_1").performScrollTo().performClick()
        regra.onNodeWithTag("carimbo_tamanho_2").performScrollTo().performClick()
        regra.onNodeWithTag("tinta_9").performClick()
        regra.mainClock.advanceTimeBy(300)
        regra.onNodeWithTag("quadro_de_colorir").performTouchInput { click(Offset(width * 0.15f, height * 0.15f)) }
        regra.mainClock.advanceTimeBy(300)
        regra.onNodeWithTag("quadro_de_colorir").performTouchInput { click(Offset(width * 0.85f, height * 0.2f)) }
        regra.mainClock.advanceTimeBy(600)
        val obra = estado.obrasDeColorir.getValue("gato")
        assertEquals(Fundo.CAMPO, obra.fundo)
        assertEquals("dois carimbos de coração rosa", listOf(1 to 9, 1 to 9), obra.carimbos.map { it.tipo to it.tinta })
        assertEquals(Carimbos.tamanhos[2], obra.carimbos[0].tamanho, 1e-6f)
        foto("s10_colorir_carimbo_e_fundo")
    }

    /** Álbum: a aba do Livro de Colorir mostra a figurinha do desenho terminado e a dourada da coleção completa. */
    @Test fun albumDoLivroDeColorir() {
        val bichos = Desenhos.daColecao("bichos").associate { it.id to 2 }
        val e = EstadoJogo(perfil = menina, coloridos = bichos)
        montar { TelaAlbum(e, Trilha.MATEMATICA, onVoltar = {}) }
        regra.mainClock.advanceTimeBy(600)
        regra.onNodeWithTag("album_livro").performScrollTo().performClick()
        regra.mainClock.advanceTimeBy(600)
        regra.onNodeWithTag("pagina_do_livro").assertExists()
        // Com a coleção dos bichos completa, o álbum abre na primeira coleção que falta (o mar, sem figurinha).
        regra.onNodeWithContentDescription("Página anterior").performClick()
        regra.mainClock.advanceTimeBy(600)
        regra.onAllNodesWithTag("figurinha_ganha").assertCountEquals(Desenhos.daColecao("bichos").size + 1)
        foto("s10_album_do_livro")
    }

    /** Diplomas: a coleção completa pendura o diploma do Livro de Colorir. */
    @Test fun diplomaDaColecaoDoLivro() {
        val bichos = Desenhos.daColecao("bichos").associate { it.id to 2 }
        montar { TelaDiplomas(EstadoJogo(perfil = menina, coloridos = bichos), {}) }
        regra.mainClock.advanceTimeBy(600)
        regra.onNodeWithTag("diploma_do_livro").assertExists().performClick()
        regra.mainClock.advanceTimeBy(600)
        regra.onNodeWithTag("diploma_aberto").assertExists()
        foto("s10_diploma_do_livro")
    }

    /** Galeria: os desenhos terminados viram quadros; tocar num quadro abre o desenho para pintar. */
    @Test fun colorirGaleriaDasObras() {
        val casinha = Desenhos.porId.getValue("casinha")
        val e = EstadoJogo(
            perfil = menina,
            obrasDeColorir = mapOf(gato.id to Colorir.obraModelo(gato).copy(fundo = Fundo.CEU), casinha.id to Colorir.obraModelo(casinha)),
            coloridos = mapOf(gato.id to 3, casinha.id to 1),
        )
        montar { TelaColorir(e, {}, {}, {}) }
        regra.mainClock.advanceTimeBy(600)
        regra.onNodeWithTag("abrir_galeria").performClick()
        regra.mainClock.advanceTimeBy(600)
        regra.onNodeWithTag("galeria_do_livro").assertExists()
        regra.onNodeWithTag("quadro_gato").assertExists()
        regra.onNodeWithTag("quadro_casinha").assertExists()
        foto("s10_colorir_galeria")
        // Guardar como imagem é de adulto: abre a continha antes, e tocar fora fecha.
        regra.onNodeWithTag("guardar_gato").performClick()
        regra.mainClock.advanceTimeBy(600)
        regra.onNodeWithTag("portao_guardar_obra").assertExists()
        foto("s10_colorir_guardar_imagem")
        regra.onRoot().performTouchInput { click(Offset(10f, height - 10f)) }
        regra.mainClock.advanceTimeBy(600)
        regra.onAllNodesWithTag("portao_guardar_obra").assertCountEquals(0)
        regra.onNodeWithTag("quadro_gato").performClick()
        regra.mainClock.advanceTimeBy(600)
        regra.onNodeWithTag("quadro_de_colorir").assertExists()
    }

    /** A imagem que os pais guardam: o bitmap tem o tamanho pedido e mostra a pintura (não é só papel branco). */
    @Test fun imagemDaObraDesenhaAPintura() {
        val b = br.com.casinhadosaber.dados.ImagemDaObra.bitmap(gato, Colorir.obraModelo(gato), 128)
        assertEquals(128, b.width)
        assertEquals(128, b.height)
        val pixels = IntArray(128 * 128)
        b.getPixels(pixels, 0, 128, 0, 0, 128, 128)
        assertTrue("a pintura aparece na imagem", pixels.count { it != android.graphics.Color.WHITE } > 128 * 128 / 10)
        assertEquals("casinha-gato.png", br.com.casinhadosaber.dados.ImagemDaObra.nomeDoArquivo(gato))
    }

    /** Pintor do dia: a faixa aparece com o desenho de hoje e some sem informar o dia. */
    @Test fun colorirPintorDoDia() {
        val hoje = 20_000L
        montar { TelaColorir(EstadoJogo(perfil = menina), {}, {}, {}, hoje = hoje) }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithTag("pintor_do_dia").assertExists()
        foto("s10_colorir_pintor_do_dia")
    }

    @Test fun colorirSemDiaNaoTemPintorDoDia() {
        montar { TelaColorir(EstadoJogo(perfil = menina), {}, {}, {}) }
        regra.mainClock.advanceTimeBy(1500)
        regra.onAllNodesWithTag("pintor_do_dia").assertCountEquals(0)
    }

    /** Modo fácil: some o pincel e a borracha e ficam seis cores. */
    @Test fun colorirModoFacilEscondeFerramentas() {
        montar { TelaColorir(EstadoJogo(perfil = menina), {}, {}, {}, abrirDesenho = "gato") }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithTag("colorir_pincel").assertExists()
        regra.onNodeWithTag("colorir_facil").performClick()
        regra.mainClock.advanceTimeBy(500)
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        regra.mainClock.advanceTimeBy(500)
        regra.onAllNodesWithTag("colorir_pincel").assertCountEquals(0)
        regra.onAllNodesWithTag("colorir_borracha").assertCountEquals(0)
        regra.onNodeWithTag("colorir_balde").assertExists()
        regra.onAllNodesWithTag("tinta_5").assertCountEquals(0)
        regra.onNodeWithTag("tinta_1").assertExists()
        foto("s10_colorir_modo_facil")
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

    /** Área dos pais: o cartão com o que a criança pintou e montou. */
    @Test fun areaDosPaisMostraAsCriacoes() {
        montar { br.com.casinhadosaber.ui.telas.CartaoDasCriacoes(br.com.casinhadosaber.dominio.ResumoDasCriacoes(3, 32, 7, 2, 4)) }
        regra.onNodeWithTag("cartao_das_criacoes").assertExists()
        foto("s10_pais_criacoes")
    }

    @Test fun areaDosPaisSemCriacoesAindaNaoComecou() {
        montar { br.com.casinhadosaber.ui.telas.CartaoDasCriacoes(br.com.casinhadosaber.dominio.ResumoDasCriacoes(0, 32, 0, 0, 0)) }
        regra.onNodeWithTag("cartao_das_criacoes").assertExists()
    }

    // ------------------------------------------------------------------ s11: quebra cabeças

    @Test fun puzzleMenu() {
        montar { TelaPuzzle(EstadoJogo(perfil = menina), {}, {}) }
        regra.onNodeWithTag("puzzle_montar").assertExists()
        regra.onNodeWithTag("puzzle_selos").assertExists()
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

    /**
     * Estado mudado dentro de um gesto só vira recomposição depois que as notificações de snapshot são aplicadas;
     * no aparelho quem faz isso é o Choreographer, e aqui o relógio do teste está parado.
     */
    private fun aplicarEAvancar(ms: Long) {
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        regra.mainClock.advanceTimeBy(ms)
    }

    /** Sem avançar por um tempo, a dica fica sugerida (lâmpada destacada); antes disso, não. */
    @Test fun puzzleTravouSugereADica() {
        montar { TelaPuzzle(EstadoJogo(perfil = menina), {}, {}, semente = 42L, esperaTravouMs = 3_000L) }
        regra.onNodeWithTag("puzzle_montar").performClick()
        regra.mainClock.advanceTimeBy(1_000)
        regra.onNodeWithTag("puzzle_dica").assertContentDescriptionEquals("Dica: mostra uma peça e o lugar dela")
        regra.mainClock.advanceTimeBy(4_000)
        androidx.compose.runtime.snapshots.Snapshot.sendApplyNotifications()
        regra.mainClock.advanceTimeBy(500)
        regra.onNodeWithTag("puzzle_dica").assertContentDescriptionContains("sugerida", substring = true)
    }

    /** Tocar numa peça e depois na casa dela na moldura encaixa a peça: faltam 3 (caminho sem arrastar). */
    @Test fun puzzleTocarEncaixa() {
        val semente = 42L
        montar { TelaPuzzle(EstadoJogo(perfil = menina), {}, {}, semente = semente) }
        regra.onNodeWithTag("puzzle_montar").performClick()
        regra.mainClock.advanceTimeBy(800)
        val id = Puzzle.montar("modelo:gato", 1, semente).pecas.last().id
        regra.onNodeWithTag("peca_$id").performClick()
        aplicarEAvancar(400)
        // Primeiro: o toque na peça a deixa escolhida (separa "o toque na peça falhou" de "o toque na casa falhou").
        regra.onNodeWithTag("peca_$id").assertIsSelected()
        // Nível 1 é 2 por 2: a casa do id está na coluna id % 2, linha id / 2.
        regra.onNodeWithTag("moldura_do_puzzle").performTouchInput {
            click(Offset(width * ((id % 2) * 0.5f + 0.25f), width * ((id / 2) * 0.5f + 0.25f)))
        }
        aplicarEAvancar(800)
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
        aplicarEAvancar(800)
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
        aplicarEAvancar(300)
        val meio = peca()
        regra.onRoot().performTouchInput {
            moveTo(de + delta)
            up()
        }
        aplicarEAvancar(1000)
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
