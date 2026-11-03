package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.CacaNaCena
import br.com.casinhadosaber.dominio.EscondeEsconde
import br.com.casinhadosaber.dominio.EstourarBaloes
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItemNaCena
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Pescaria
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaJogo
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

/**
 * Capturas e jogadas automáticas de Pescaria, Balões, Caça na cena e Esconde esconde (prefixo n1_).
 * Mesmo jeito de CapturasTest: relógio parado, narrador e sons desligados.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasNovas1Test {

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

    private fun numero(n: Int, nome: String) = Opcao(Visual.Texto("$n"), listOf(pt(nome)))
    private fun letra(l: String) = Opcao(Visual.Texto(l), listOf(pt(l.lowercase())))
    private fun figura(e: String, nome: String) = Opcao(Visual.Emoji(e), listOf(pt(nome)))

    private val pescaria = deMexer(
        "Pesque os peixes com 7",
        Pescaria(
            listOf(numero(7, "sete"), numero(3, "três"), numero(7, "sete"), numero(5, "cinco"), numero(7, "sete"), numero(2, "dois")),
            setOf(0, 2, 4),
        ),
    )
    private val pescariaFiguras = deMexer(
        "Pesque as frutas",
        Pescaria(listOf(figura("🍎", "maçã"), figura("🐶", "cachorro"), figura("🍌", "banana"), figura("⚽", "bola")), setOf(0, 2)),
    )
    private val baloes = deMexer(
        "Estoure de 1 até 5",
        EstourarBaloes(
            listOf(numero(3, "três"), numero(1, "um"), letra("A"), numero(2, "dois"), numero(5, "cinco"), numero(4, "quatro"), letra("B")),
            listOf(1, 3, 0, 5, 4),
            emOrdem = true,
        ),
    )
    private val baloesIngles = deMexer(
        "Pop the animals",
        EstourarBaloes(
            listOf(
                Opcao(Visual.Emoji("🐱"), listOf(en("cat"))), Opcao(Visual.Emoji("🍎"), listOf(en("apple"))),
                Opcao(Visual.Emoji("🐶"), listOf(en("dog"))), Opcao(Visual.Emoji("🚗"), listOf(en("car"))),
            ),
            listOf(0, 2),
        ),
    )
    private val caca = deMexer(
        "Ache as 3 estrelas",
        CacaNaCena(
            0,
            listOf(
                ItemNaCena(figura("⭐", "estrela"), 0.1f, 0.1f), ItemNaCena(figura("🐱", "gato"), 0.5f, 0.3f),
                ItemNaCena(figura("⭐", "estrela"), 0.85f, 0.55f), ItemNaCena(figura("🍎", "maçã"), 0.2f, 0.6f),
                ItemNaCena(figura("⭐", "estrela"), 0.4f, 0.9f), ItemNaCena(figura("🐶", "cachorro"), 0.9f, 0.95f, 1.3f),
                ItemNaCena(figura("🌸", "flor"), 0.65f, 0.1f, 0.7f), ItemNaCena(figura("🚗", "carro"), 0.1f, 0.95f),
            ),
            setOf(0, 2, 4),
        ),
    )
    private val cacaNoMar = deMexer(
        "Ache o que começa com P",
        CacaNaCena(
            6,
            listOf(
                ItemNaCena(figura("🐟", "peixe"), 0.15f, 0.2f), ItemNaCena(figura("🐙", "polvo"), 0.75f, 0.35f),
                ItemNaCena(figura("🦀", "caranguejo"), 0.3f, 0.75f), ItemNaCena(figura("🐢", "tartaruga"), 0.85f, 0.85f),
                ItemNaCena(figura("🐬", "golfinho"), 0.5f, 0.55f, 1.2f),
            ),
            setOf(0, 1),
        ),
    )
    private val escondeComTrocas = deMexer(
        "Siga o cachorrinho! Onde ele está?",
        EscondeEsconde("🐶", listOf(numero(3, "três"), numero(7, "sete"), numero(5, "cinco")), certo = 1, trocas = 3),
    )
    private val escondeCores = deMexer(
        "The cat is behind the green box",
        EscondeEsconde(
            "🐱",
            listOf(
                Opcao(Visual.Cor(0xFFE53935), listOf(en("red"))), Opcao(Visual.Cor(0xFF1E88E5), listOf(en("blue"))),
                Opcao(Visual.Cor(0xFF43A047), listOf(en("green"))), Opcao(Visual.Cor(0xFFFDD835), listOf(en("yellow"))),
            ),
            certo = 2,
        ),
    )

    /** Tempo até o esconde esconde aceitar toques: aceno, esconder e [trocas] trocas, com folga. */
    private fun esperaDoEsconde(trocas: Int) = 5000L + trocas * 1600L

    // ------------------------------------------------------------------ capturas

    @Test fun pescariaNumeros() = capturar("n1_01_pescaria_numeros") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, {}, perguntasProntas = listOf(pescaria))
    }

    /** Depois de dois peixes errados, as frutas certas brilham (a rodada facilitada trocaria a pergunta). */
    @Test fun pescariaComAjuda() = capturar(
        "n1_02_pescaria_ajuda",
        acao = {
            repeat(2) {
                regra.onAllNodesWithTag("peixe_errado")[it].performSemanticsAction(SemanticsActions.OnClick)
                regra.mainClock.advanceTimeBy(1500)
            }
        },
    ) {
        TelaJogo(gerador.fases(Trilha.LOGICA)[2], menino, {}, {}, perguntasProntas = listOf(pescariaFiguras))
    }

    @Test fun pescariaPescouUm() = capturar(
        "n1_03_pescaria_pescou_um",
        acao = { regra.onAllNodesWithTag("peixe_certo").onFirst().performClick() },
    ) {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, {}, perguntasProntas = listOf(pescaria))
    }

    @Test fun baloesEmOrdem() = capturar("n1_04_baloes_em_ordem") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[3], menino, {}, {}, perguntasProntas = listOf(baloes))
    }

    @Test fun baloesIngles() = capturar(
        "n1_05_baloes_ingles_estourou",
        acao = { regra.onNodeWithTag("balao_certo_0").performSemanticsAction(SemanticsActions.OnClick) },
    ) {
        TelaJogo(gerador.fases(Trilha.INGLES)[3], menina, {}, {}, perguntasProntas = listOf(baloesIngles))
    }

    @Test fun cacaNaCena() = capturar("n1_06_caca_na_cena") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[4], menina, {}, {}, perguntasProntas = listOf(caca))
    }

    @Test fun cacaNoFundoDoMarComAjuda() = capturar(
        "n1_07_caca_fundo_do_mar_ajuda",
        acao = {
            // Dois erros: as respostas certas brilham.
            regra.onAllNodesWithTag("cena_errado").onFirst().performClick()
            regra.mainClock.advanceTimeBy(800)
            regra.onAllNodesWithTag("cena_errado")[1].performClick()
        },
    ) {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[4], menino, {}, {}, perguntasProntas = listOf(cacaNoMar))
    }

    @Test fun escondeTrocando() = capturar("n1_08_esconde_trocando", tempo = 5600) {
        TelaJogo(gerador.fases(Trilha.LOGICA)[4], menina, {}, {}, perguntasProntas = listOf(escondeComTrocas))
    }

    @Test fun escondeAchou() = capturar(
        "n1_09_esconde_achou",
        tempo = esperaDoEsconde(3),
        acao = { regra.onNodeWithTag("esconderijo_certo").performClick() },
    ) {
        TelaJogo(gerador.fases(Trilha.LOGICA)[4], menina, {}, {}, perguntasProntas = listOf(escondeComTrocas))
    }

    @Test fun escondeCoresErrou() = capturar(
        "n1_10_esconde_cores_surpresa",
        tempo = esperaDoEsconde(0),
        acao = { regra.onAllNodesWithTag("esconderijo_errado").onFirst().performClick() },
    ) {
        TelaJogo(gerador.fases(Trilha.INGLES)[4], menino, {}, {}, perguntasProntas = listOf(escondeCores))
    }

    // ------------------------------------------------------------------ jogadas completas (marcas de teste)

    /** Joga a pergunta com [toques] e devolve o que a TelaJogo entregou no fim. */
    private fun jogar(pergunta: Pergunta, espera: Long = 1500, toques: () -> Unit): List<Boolean>? {
        var resultado: List<Boolean>? = null
        montar {
            TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, { resultado = it }, perguntasProntas = listOf(pergunta))
        }
        regra.mainClock.advanceTimeBy(espera)
        toques()
        regra.mainClock.advanceTimeBy(8000)
        return resultado
    }

    private fun tocarTodos(marca: String, porSemantica: Boolean = false) {
        repeat(20) {
            val nos = regra.onAllNodesWithTag(marca).fetchSemanticsNodes()
            if (nos.isEmpty()) return
            val no = regra.onAllNodesWithTag(marca).onFirst()
            if (porSemantica) no.performSemanticsAction(SemanticsActions.OnClick) else no.performClick()
            regra.mainClock.advanceTimeBy(1000)
        }
    }

    @Test fun pescariaSemErroContaDePrimeira() {
        val r = jogar(pescaria) { tocarTodos("peixe_certo") }
        assertEquals("pescar só os certos deveria valer de primeira", listOf(true), r)
    }

    @Test fun pescariaComErroNaoContaDePrimeira() {
        val r = jogar(pescaria) {
            regra.onAllNodesWithTag("peixe_errado").onFirst().performClick()
            regra.mainClock.advanceTimeBy(500)
            tocarTodos("peixe_certo")
        }
        assertEquals(listOf(false), r)
    }

    @Test fun baloesNaOrdemContamDePrimeira() {
        val r = jogar(baloes) {
            for (k in 0 until 5) {
                regra.onNodeWithTag("balao_certo_$k").performSemanticsAction(SemanticsActions.OnClick)
                regra.mainClock.advanceTimeBy(600)
            }
        }
        assertEquals(listOf(true), r)
    }

    @Test fun baloesForaDaOrdemContamErro() {
        val r = jogar(baloes) {
            regra.onNodeWithTag("balao_certo_1").performSemanticsAction(SemanticsActions.OnClick)
            regra.mainClock.advanceTimeBy(600)
            for (k in 0 until 5) {
                regra.onNodeWithTag("balao_certo_$k").performSemanticsAction(SemanticsActions.OnClick)
                regra.mainClock.advanceTimeBy(600)
            }
        }
        assertEquals(listOf(false), r)
    }

    @Test fun cacaAchaTudoDePrimeira() {
        val r = jogar(caca) { tocarTodos("cena_certo") }
        assertEquals(listOf(true), r)
    }

    @Test fun escondeAchaDePrimeira() {
        val r = jogar(escondeComTrocas, espera = esperaDoEsconde(3)) {
            regra.onNodeWithTag("esconderijo_certo").performClick()
        }
        assertEquals(listOf(true), r)
    }

    @Test fun escondeComErroNaoContaDePrimeira() {
        val r = jogar(escondeCores, espera = esperaDoEsconde(0)) {
            regra.onAllNodesWithTag("esconderijo_errado").onFirst().performClick()
            regra.mainClock.advanceTimeBy(1500)
            // O esconderijo já aberto não conta outro erro.
            regra.onAllNodesWithTag("esconderijo_aberto").onFirst().performClick()
            regra.mainClock.advanceTimeBy(500)
            regra.onNodeWithTag("esconderijo_certo").performClick()
        }
        assertEquals(listOf(false), r)
    }
}
