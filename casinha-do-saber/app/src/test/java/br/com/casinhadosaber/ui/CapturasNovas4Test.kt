package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.CartasViradas
import br.com.casinhadosaber.dominio.Ditado
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Idioma
import br.com.casinhadosaber.dominio.MontarFrase
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.ProgramarRobo
import br.com.casinhadosaber.dominio.SigaASequencia
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.atividades.AtividadeInterativa
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

/**
 * Capturas e testes de jogar até o fim das telas de Siga a sequência, Ditado, Cartas viradas,
 * Programar o robô e Montar a frase (prefixo n4_). Cada teste de "concluir" usa as marcas de teste
 * das telas, do mesmo jeito que o teste no emulador pode usar.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasNovas4Test {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val menino = Perfil("Leo", Genero.MENINO, pele = 0, cabelo = 3)
    private val gerador = GeradorDeFases(Random(4))

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

    private fun capturar(nome: String, tempo: Long = 2500, acao: (() -> Unit)? = null, conteudo: @Composable () -> Unit) {
        montar(conteudo)
        regra.mainClock.advanceTimeBy(tempo)
        if (acao != null) {
            acao()
            regra.mainClock.advanceTimeBy(1200)
        }
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    private fun deMexer(instrucao: String, atividade: Atividade) =
        Pergunta.deAtividade(instrucao, listOf(pt(instrucao)), atividade, listOf(pt("Muito bem!")))

    /** Só a atividade, sem a tela do jogo (para os testes de concluir e de ajuda). */
    private class Resultado {
        var concluiu: Boolean? = null
        var erros = 0
    }

    @Composable
    private fun SoAtividade(atividade: Atividade, r: Resultado, ajudaInicial: Boolean = false) {
        AtividadeInterativa(
            atividade = atividade,
            fala = emptyList(),
            ativa = true,
            ajudaInicial = ajudaInicial,
            cor = Color(0xFF6C63FF),
            onErro = { r.erros++ },
            onConcluir = { r.concluiu = it },
            modifier = Modifier.fillMaxSize().padding(16.dp),
        )
    }

    private fun clicar(tag: String, depois: Long = 400) {
        regra.onNodeWithTag(tag).performClick()
        regra.mainClock.advanceTimeBy(depois)
    }

    // ------------------------------------------------------------------ dados

    private val siga = SigaASequencia(
        botoes = listOf(
            Opcao(Visual.Emoji("🐶"), listOf(en("dog"))),
            Opcao(Visual.Emoji("🐱"), listOf(en("cat"))),
            Opcao(Visual.Emoji("🐮"), listOf(en("cow"))),
            Opcao(Visual.Emoji("🐸"), listOf(en("frog"))),
        ),
        cores = listOf(0xFFFF6B6B, 0xFF4D96FF, 0xFFFFC93C, 0xFF3DBE6B),
        sequencia = listOf(0, 2, 1, 3),
    )
    private val ditado = Ditado(listOf("G", "A", "T", "O"), listOf("P", "M"), figura = "🐱")
    private val ditadoIngles = Ditado(listOf("D", "O", "G"), listOf("B", "T"), idioma = Idioma.EN)
    private val cartas = CartasViradas(
        cartas = listOf("4", "1", "6", "2", "5", "3").map { Opcao(Visual.Texto(it)) },
        ordem = listOf(1, 3, 5, 0, 4, 2),
    )
    private val robo = ProgramarRobo(linhas = 4, colunas = 4, inicio = 0, chegada = 15, obstaculos = setOf(5, 10), maxComandos = 6)
    private val roboLaco = ProgramarRobo(linhas = 5, colunas = 5, inicio = 0, chegada = 24, obstaculos = setOf(6, 12, 18), maxComandos = 6, repetir = true)
    private val frase = MontarFrase(listOf("O", "gato", "bebe", "leite."), listOf("pula"), figura = "🐱")
    private val fraseIngles = MontarFrase(listOf("I", "like", "apples."), listOf("dogs"), idioma = Idioma.EN, figura = "🍎")

    // ------------------------------------------------------------------ capturas

    /** O botão do cachorro aceso, mostrando o primeiro passo da sequência. */
    @Test fun sigaASequencia() = capturar("n4_01_siga_a_sequencia", tempo = 2100) {
        TelaJogo(gerador.fases(Trilha.INGLES)[3], menina, {}, {}, perguntasProntas = listOf(deMexer("Repita a sequência", siga)))
    }

    /** Depois de mostrar: a vez da criança, com dois toques certos (bolinhas verdes). */
    @Test fun sigaASequenciaVez() = capturar(
        "n4_02_siga_a_sequencia_vez",
        tempo = 8000,
        acao = {
            clicar("seq_botao_0")
            clicar("seq_botao_2")
        },
    ) {
        TelaJogo(gerador.fases(Trilha.INGLES)[3], menino, {}, {}, perguntasProntas = listOf(deMexer("Repita a sequência", siga)))
    }

    @Test fun ditadoComFigura() = capturar(
        "n4_03_ditado",
        acao = {
            clicar("ditado_certa_0")
            clicar("ditado_certa_1")
        },
    ) {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[13], menina, {}, {}, perguntasProntas = listOf(deMexer("Escreva a palavra", ditado)))
    }

    /** Ditado em inglês sem figura, no exemplo (a tecla certa brilha e a mãozinha mostra o gesto). */
    @Test fun ditadoAjuda() = capturar("n4_04_ditado_ajuda", tempo = 3000) {
        SoAtividade(ditadoIngles, Resultado(), ajudaInicial = true)
    }

    /** Cartas viradas depois da espiadinha, com o 1 e o 2 já na fila de cima. */
    @Test fun cartasViradas() = capturar(
        "n4_05_cartas_viradas",
        tempo = 3500,
        acao = {
            clicar("virada_ordem_0", 1200)
            clicar("virada_ordem_1", 1200)
        },
    ) {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[3], menino, {}, {}, perguntasProntas = listOf(deMexer("Vire em ordem: 1, 2, 3", cartas)))
    }

    /** Carta errada aberta um instante (antes de virar de volta). */
    @Test fun cartasViradasErro() = capturar(
        "n4_06_cartas_viradas_erro",
        tempo = 3500,
        acao = { regra.onNodeWithTag("virada_ordem_4").performClick() },
    ) {
        SoAtividade(cartas, Resultado())
    }

    @Test fun programarRobo() = capturar(
        "n4_07_programar_robo",
        acao = {
            clicar("robo_seta_DIREITA")
            clicar("robo_seta_DIREITA")
        },
    ) {
        TelaJogo(gerador.fases(Trilha.LOGICA)[3], menina, {}, {}, perguntasProntas = listOf(deMexer("Leve o robô até a bateria", robo)))
    }

    /** Ajuda do robô: pegadas do caminho e a seta certa brilhando; bloco 2×. */
    @Test fun programarRoboAjuda() = capturar(
        "n4_08_programar_robo_ajuda",
        acao = {
            clicar("robo_seta_DIREITA")
            clicar("robo_repetir")
        },
    ) {
        SoAtividade(roboLaco, Resultado(), ajudaInicial = true)
    }

    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    @Test fun programarRoboCelularPequeno() = capturar("n4_09_programar_robo_celular_pequeno") {
        TelaJogo(gerador.fases(Trilha.LOGICA)[3], menino, {}, {}, perguntasProntas = listOf(deMexer("Leve o robô até a bateria", robo)))
    }

    @Test fun montarFrase() = capturar(
        "n4_10_montar_frase",
        acao = { arrastar("frase_certa_0", "frase_lugar_0") },
    ) {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[13], menino, {}, {}, perguntasProntas = listOf(deMexer("Monte a frase", frase)))
    }

    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    @Test fun montarFraseInglesAjuda() = capturar("n4_11_montar_frase_ingles_ajuda", tempo = 3000) {
        SoAtividade(fraseIngles, Resultado(), ajudaInicial = true)
    }

    // ------------------------------------------------------------------ jogar até o fim

    private fun arrastar(de: String, para: String) {
        val alvo = regra.onNodeWithTag(para).fetchSemanticsNode().boundsInRoot
        val peca = regra.onNodeWithTag(de)
        val origem = peca.fetchSemanticsNode().boundsInRoot
        peca.performTouchInput { swipe(center, alvo.center - origem.topLeft, durationMillis = 400) }
        regra.mainClock.advanceTimeBy(900)
    }

    @Test fun sigaASequenciaConclui() {
        val r = Resultado()
        montar { SoAtividade(siga, r) }
        regra.mainClock.advanceTimeBy(8000)
        for (i in siga.sequencia) clicar("seq_botao_$i")
        regra.mainClock.advanceTimeBy(2500)
        assertEquals(true, r.concluiu)
        assertEquals(0, r.erros)
    }

    @Test fun sigaASequenciaErroMostraDeNovo() {
        val r = Resultado()
        montar { SoAtividade(siga, r) }
        regra.mainClock.advanceTimeBy(8000)
        clicar("seq_botao_3")
        assertEquals(1, r.erros)
        // Mostra a sequência de novo e a criança repete inteira.
        regra.mainClock.advanceTimeBy(9000)
        for (i in siga.sequencia) clicar("seq_botao_$i")
        regra.mainClock.advanceTimeBy(2500)
        assertEquals(false, r.concluiu)
        assertEquals(1, r.erros)
    }

    @Test fun ditadoErroDepoisConclui() {
        val r = Resultado()
        montar { SoAtividade(ditado, r) }
        regra.mainClock.advanceTimeBy(1500)
        regra.onAllNodesWithTag("ditado_extra")[0].performClick()
        regra.mainClock.advanceTimeBy(600)
        assertEquals(1, r.erros)
        for (k in ditado.partes.indices) clicar("ditado_certa_$k", 700)
        regra.mainClock.advanceTimeBy(4000)
        assertEquals(false, r.concluiu)
    }

    @Test fun ditadoArrastandoSemErro() {
        val r = Resultado()
        montar { SoAtividade(ditadoIngles, r) }
        regra.mainClock.advanceTimeBy(1500)
        // A primeira peça vai arrastada; as outras tocadas, como num teclado.
        arrastar("ditado_certa_0", "ditado_certa_2")
        assertEquals("soltar fora das casinhas não conta erro", 0, r.erros)
        for (k in ditadoIngles.partes.indices) clicar("ditado_certa_$k", 700)
        regra.mainClock.advanceTimeBy(4000)
        assertEquals(true, r.concluiu)
    }

    @Test fun cartasViradasConclui() {
        val r = Resultado()
        montar { SoAtividade(cartas, r) }
        regra.mainClock.advanceTimeBy(4500)
        for (k in cartas.ordem.indices) clicar("virada_ordem_$k", 1300)
        regra.mainClock.advanceTimeBy(2500)
        assertEquals(true, r.concluiu)
        assertEquals(0, r.erros)
    }

    @Test fun programarRoboConclui() {
        val r = Resultado()
        montar { SoAtividade(robo, r) }
        regra.mainClock.advanceTimeBy(1000)
        // Primeiro um programa curto demais: o robô para antes e conta um erro.
        clicar("robo_seta_DIREITA")
        clicar("robo_play", 5000)
        assertEquals(1, r.erros)
        assertNull(r.concluiu)
        repeat(2) { clicar("robo_seta_DIREITA") }
        repeat(3) { clicar("robo_seta_BAIXO") }
        clicar("robo_play", 9000)
        assertEquals(false, r.concluiu)
    }

    @Test fun programarRoboComLacoSemErro() {
        val r = Resultado()
        montar { SoAtividade(roboLaco, r) }
        regra.mainClock.advanceTimeBy(1000)
        // 4 para a direita e 4 para baixo, com o bloco 2 vezes: → 2× → 2× ↓ 2× ↓ 2×.
        repeat(2) {
            clicar("robo_seta_DIREITA")
            clicar("robo_repetir")
        }
        repeat(2) {
            clicar("robo_seta_BAIXO")
            clicar("robo_repetir")
        }
        clicar("robo_play", 12000)
        assertEquals(true, r.concluiu)
        assertEquals(0, r.erros)
    }

    @Test fun montarFraseConclui() {
        val r = Resultado()
        montar { SoAtividade(frase, r) }
        regra.mainClock.advanceTimeBy(1500)
        for (k in frase.palavras.indices) arrastar("frase_certa_$k", "frase_lugar_$k")
        regra.mainClock.advanceTimeBy(4000)
        assertEquals(true, r.concluiu)
        assertEquals(0, r.erros)
    }
}
