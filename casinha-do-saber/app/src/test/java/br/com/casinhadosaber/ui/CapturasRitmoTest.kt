package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Data
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Livrinhos
import br.com.casinhadosaber.dominio.Nivelamento
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.RegistroDeSessao
import br.com.casinhadosaber.dominio.Relembrar
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.aplicarNivel
import br.com.casinhadosaber.dominio.paraRelembrar
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.CartaoDoRitmo
import br.com.casinhadosaber.ui.telas.TelaConviteNivel
import br.com.casinhadosaber.ui.telas.TelaConviteRelembrar
import br.com.casinhadosaber.ui.telas.TelaEstante
import br.com.casinhadosaber.ui.telas.TelaFimRelembrar
import br.com.casinhadosaber.ui.telas.TelaInicio
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.TelaLivrinho
import br.com.casinhadosaber.ui.telas.TelaResultadoNivel
import br.com.casinhadosaber.ui.telas.TelaTrilha
import br.com.casinhadosaber.ui.telas.perguntasDoRelembrar
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
import kotlin.random.Random

/**
 * Capturas do "no ritmo da criança" (prefixo s10_): Relembrar (convite da Corujinha, rodada e fim), Descobrir o
 * nível (botão no caminho, convite, pergunta, resultado e caminho aberto), a estante de livrinhos, uma página, a
 * pergunta do fim e o marcador. Mesmo jeito das outras capturas: relógio parado, narrador e sons desligados.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasRitmoTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val hoje = Data(2026, 11, 4).diaEpoca

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

    private fun capturar(nome: String, tempo: Long = 2000, conteudo: @Composable () -> Unit) {
        montar(conteudo)
        regra.mainClock.advanceTimeBy(tempo)
        foto(nome)
    }

    /** Letras até o mundo 6 e Números até o mundo 2; erros de primeira ontem em 3 fases (para relembrar hoje). */
    private fun progresso(): EstadoJogo {
        val estrelas = HashMap<String, Int>()
        for (n in 0 until 62) estrelas[EstadoJogo.chave(Trilha.PORTUGUES, n)] = 2 + n % 2
        for (n in 0 until 14) estrelas[EstadoJogo.chave(Trilha.MATEMATICA, n)] = 3
        val sessoes = listOf(
            RegistroDeSessao(hoje - 1, Trilha.MATEMATICA, 12, 2, 4, 5, 220),
            RegistroDeSessao(hoje - 1, Trilha.PORTUGUES, 55, 0, 3, 5, 260),
            RegistroDeSessao(hoje - 1, Trilha.PORTUGUES, 58, 2, 4, 5, 240),
        )
        var e = EstadoJogo(perfil = menina, estrelas = estrelas, sessoes = sessoes, moedas = 12, livrinhosLidos = setOf("pato", "bola", "chuva"))
        for (item in ItensDaCasa.ordem.take(30)) e = e.colocar(item)
        return e
    }

    // ------------------------------------------------------------------ Relembrar

    @Test fun inicioComCorujinhaDoRelembrar() {
        val e = progresso()
        assertEquals(3, e.paraRelembrar(hoje).size)
        capturar("s10_01_inicio_com_relembrar") {
            TelaInicio(e, true, {}, {}, {}, {}, relembrar = e.paraRelembrar(hoje).size, onEstante = {})
        }
        regra.onNodeWithTag("abrir_relembrar").assertExistsCompat()
        regra.onNodeWithTag("abrir_estante").assertExistsCompat()
    }

    @Test fun conviteDoRelembrar() = capturar("s10_02_convite_relembrar") {
        TelaConviteRelembrar("Ana", 3, onVamos = {}, onAgoraNao = {})
    }

    @Test fun rodadaDoRelembrar() {
        val e = progresso()
        val fases = Relembrar.fasesDaRodada(e.paraRelembrar(hoje))
        val (fase, perguntas) = perguntasDoRelembrar(GeradorDeFases(Random(4)), fases)!!
        assertEquals(Relembrar.PERGUNTAS, perguntas.size)
        capturar("s10_03_rodada_relembrar") {
            TelaJogo(fase = fase, perfil = menina, perguntasProntas = perguntas, onSair = {}, onConcluir = {})
        }
    }

    @Test fun fimDoRelembrar() = capturar("s10_04_fim_relembrar") {
        TelaFimRelembrar("Ana", 4, 5, Relembrar.MOEDAS) {}
    }

    // ------------------------------------------------------------------ Descobrir o nível

    @Test fun caminhoNovoOfereceNivelEEstante() {
        val e = progresso().copy(sessoes = listOf(RegistroDeSessao(hoje - 1, Trilha.LOGICA, 0, 0, 2, 5, 200)))
        assertEquals(1, e.paraRelembrar(hoje, Trilha.LOGICA).size)
        capturar("s10_05_caminho_com_nivel_e_relembrar") {
            TelaTrilha(Trilha.LOGICA, e, {}, {}, relembrar = 1, onDescobrirNivel = {})
        }
        regra.onNodeWithTag("abrir_nivel").assertExistsCompat()
    }

    @Test fun conviteDoNivel() = capturar("s10_06_convite_nivel") {
        TelaConviteNivel(Trilha.MATEMATICA, "Ana", onComecar = {}, onAgoraNao = {})
    }

    @Test fun perguntaDoNivel() {
        val gerador = GeradorDeFases(Random(9))
        val fases = gerador.fases(Trilha.MATEMATICA)
        // Depois de acertar no mundo 1 e no 4, a pergunta da vez é do mundo 7.
        val antes = listOf(true, true)
        assertEquals(64, Nivelamento.de(Trilha.MATEMATICA, antes).faseDaPergunta)
        capturar("s10_07_nivel_pergunta") {
            TelaJogo(
                fase = fases.first(),
                perfil = menina,
                onSair = {},
                onConcluir = {},
                treino = { r -> gerador.perguntaDaFase(fases[Nivelamento.de(Trilha.MATEMATICA, antes + r).faseDaPergunta]) },
                fimDoTreino = { r -> Nivelamento.de(Trilha.MATEMATICA, antes + r).terminou },
            )
        }
    }

    @Test fun resultadoDoNivelECaminhoAberto() {
        val n = Nivelamento.de(Trilha.MATEMATICA, listOf(true, true, false, false, true, true, false, false))
        assertTrue(n.terminou)
        assertEquals(7, n.mundoSugerido)
        capturar("s10_08_resultado_nivel") { TelaResultadoNivel(n, "Ana") {} }
    }

    @Test fun caminhoDepoisDoNivel() {
        val n = Nivelamento.de(Trilha.INGLES, listOf(true, true, false, false, true, true, false, false))
        val e = EstadoJogo(perfil = menina).aplicarNivel(n)
        assertEquals(0, e.fasesVencidas)
        capturar("s10_09_caminho_depois_do_nivel", tempo = 3000) {
            TelaTrilha(Trilha.INGLES, e, {}, {}, onDescobrirNivel = {})
        }
        // Já fez o nível: não oferece de novo.
        assertEquals(0, regra.onAllNodesWithTag("abrir_nivel").fetchSemanticsNodes().size)
    }

    @Test fun cartaoDosPais() = capturar("s10_10_pais_ritmo") {
        val e = progresso().aplicarNivel(Nivelamento.de(Trilha.INGLES, listOf(true, true, true, false, false, true)))
        Column(Modifier.fillMaxSize().background(Cores.Fundo).verticalScroll(rememberScrollState()).padding(20.dp)) {
            CartaoDoRitmo(e, hoje, onDescobrir = {}, onDesfazer = {})
        }
    }

    // ------------------------------------------------------------------ Livrinhos

    @Test fun estante() {
        val e = progresso()
        assertTrue(Livrinhos.liberado(Livrinhos.porId.getValue("balao"), e))
        capturar("s10_11_estante") { TelaEstante(e, onAbrir = {}, onVoltar = {}) }
    }

    @Test fun paginaDoLivrinhoTocarPalavraEVirar() {
        val livro = Livrinhos.porId.getValue("pato")
        capturar("s10_12_livrinho_pagina") { TelaLivrinho(livro, "Ana", onTerminar = {}, onVoltar = {}) }
        // Tocar numa palavra (ela fica marcada) e virar a página.
        regra.onNodeWithTag("palavra_1").performClick()
        regra.mainClock.advanceTimeBy(300)
        regra.onNodeWithTag("pagina_seguinte").performClick()
        regra.mainClock.advanceTimeBy(800)
        regra.onNodeWithTag("palavra_3").performClick()
        regra.mainClock.advanceTimeBy(300)
        foto("s10_13_livrinho_pagina_2")
    }

    @Test fun livrinhoAteOFim() {
        val livro = Livrinhos.porId.getValue("bola")
        var terminou = 0
        montar { TelaLivrinho(livro, "Ana", onTerminar = { terminou++ }, onVoltar = {}) }
        regra.mainClock.advanceTimeBy(500)
        repeat(livro.paginas.size) {
            regra.onNodeWithTag("pagina_seguinte").performClick()
            regra.mainClock.advanceTimeBy(500)
        }
        // A pergunta: um erro (só tenta de novo), depois a certa.
        regra.onAllNodesWithTag("resposta").onFirst().performClick()
        regra.mainClock.advanceTimeBy(600)
        foto("s10_14_livrinho_pergunta")
        regra.onNodeWithTag("resposta_certa").performClick()
        regra.mainClock.advanceTimeBy(3000)
        // Segunda pergunta.
        regra.onNodeWithTag("resposta_certa").performClick()
        regra.mainClock.advanceTimeBy(3000)
        regra.mainClock.advanceTimeBy(1500)
        foto("s10_15_livrinho_marcador")
        assertEquals(1, terminou)
        regra.onNodeWithTag("livrinho_estante").assertExistsCompat()
    }

    @Test fun perguntaComAjudaDepoisDeDoisErros() {
        val livro = Livrinhos.porId.getValue("sopa")
        montar { TelaLivrinho(livro, null, onTerminar = {}, onVoltar = {}, inicio = livro.paginas.size) }
        regra.mainClock.advanceTimeBy(500)
        val erradas = regra.onAllNodesWithTag("resposta")
        erradas[0].performClick()
        regra.mainClock.advanceTimeBy(300)
        erradas[1].performClick()
        regra.mainClock.advanceTimeBy(800)
        foto("s10_16_livrinho_pergunta_com_ajuda")
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertExistsCompat() {
        fetchSemanticsNode()
    }
}
