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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.CadernoEmArquivo
import br.com.casinhadosaber.dominio.ConjuntoDeVozes
import br.com.casinhadosaber.dominio.Data
import br.com.casinhadosaber.dominio.EscreverPalavra
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.GuardaDasVozes
import br.com.casinhadosaber.dominio.Idioma
import br.com.casinhadosaber.dominio.PaginaDoCaderno
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Ponto
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.CartaoMeuCaderno
import br.com.casinhadosaber.ui.telas.CartaoVozesDaFamilia
import br.com.casinhadosaber.ui.telas.PedidoDoMicrofone
import br.com.casinhadosaber.ui.telas.TelaCaderno
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.atividades.AtividadeInterativa
import br.com.casinhadosaber.ui.telas.atividades.ModelosDasLetras
import br.com.casinhadosaber.ui.telas.atividades.TracosDasLetras
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
import java.io.File
import kotlin.math.sin
import kotlin.random.Random

/**
 * Capturas e testes das vozes da família e de escrever com o dedo (prefixo s11_): o cartão das vozes
 * na área dos pais, a atividade de escrever (vazia, no meio da palavra e com o "mostrar como") e o
 * "Meu caderno". Mesmo jeito das outras capturas: relógio parado, narrador e sons desligados.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasVozEscritaTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val gerador = GeradorDeFases(Random(4))
    private val pastaDoApp: File get() = RuntimeEnvironment.getApplication().filesDir

    @Before
    fun limpar() {
        File(pastaDoApp, "vozes_da_familia").deleteRecursively()
        CadernoEmArquivo.em(pastaDoApp).apagarTudo()
        PedidoDoMicrofone.pedidos = 0
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

    private fun capturar(nome: String) {
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    private fun deMexer(atividade: Atividade) =
        Pergunta.deAtividade("Escreva a palavra com o dedo", listOf(pt("Vamos escrever!")), atividade, listOf(pt("Bola: B, O, L, A!")))

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
            cor = Color(0xFF2EC4B6),
            onErro = { r.erros++ },
            onConcluir = { r.concluiu = it },
            modifier = Modifier.fillMaxSize().padding(16.dp),
        )
    }

    /** Toca na ação que escreve a letra da vez pelo modelo (a mesma que o teste no emulador usa). */
    private fun escreverLetraCerta() {
        regra.onNodeWithTag("escrever_letra_certa").performSemanticsAction(SemanticsActions.OnClick)
        regra.mainClock.advanceTimeBy(600)
    }

    private fun descricao(tag: String): String =
        regra.onNodeWithTag(tag).fetchSemanticsNode().config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }.joinToString()

    // ------------------------------------------------------------------ vozes da família

    /** Algumas frases já gravadas pela mamãe (arquivos de mentira só para a tela). */
    private fun gravarDeMentira(c: ConjuntoDeVozes, vararg chaves: String) {
        val g = GuardaDasVozes(File(pastaDoApp, "vozes_da_familia"))
        for (k in chaves) g.arquivo(c, k).apply { parentFile?.mkdirs(); writeBytes(ByteArray(64)) }
    }

    @Test
    fun vozesDaFamilia() {
        gravarDeMentira(ConjuntoDeVozes.MAMAE, "muito_bem", "isso_mesmo", "quase", "oi_nome", "boa_noite")
        gravarDeMentira(ConjuntoDeVozes.PAPAI, "voce_conseguiu")
        montar {
            Column(Modifier.fillMaxSize().background(Cores.Fundo).verticalScroll(rememberScrollState()).padding(20.dp)) {
                CartaoVozesDaFamilia(nome = "Ana", abertoNoComeco = true)
            }
        }
        regra.mainClock.advanceTimeBy(800)
        capturar("s11_01_vozes_da_familia")
        assertEquals("abrir a área dos pais não pede o microfone", 0, PedidoDoMicrofone.pedidos)
        regra.onNodeWithTag("apagar_muito_bem").assertExists()
        regra.onNodeWithTag("ouvir_quase").assertExists()
    }

    @Test
    fun vozesDaFamiliaFechado() {
        montar {
            Column(Modifier.fillMaxSize().background(Cores.Fundo).padding(20.dp)) {
                CartaoVozesDaFamilia(nome = "Leo")
                CartaoMeuCaderno(paginas(3), onAbrir = {})
            }
        }
        regra.mainClock.advanceTimeBy(800)
        capturar("s11_02_vozes_e_caderno_na_area_dos_pais")
        assertEquals(0, PedidoDoMicrofone.pedidos)
    }

    // ------------------------------------------------------------------ escrever com o dedo

    private val bola = EscreverPalavra("BOLA", "⚽")

    @Test
    fun escreverVazio() {
        montar {
            TelaJogo(gerador.fases(Trilha.PORTUGUES)[60], menina, {}, {}, perguntasProntas = listOf(deMexer(bola)))
        }
        regra.mainClock.advanceTimeBy(2500)
        capturar("s11_03_escrever_vazio")
    }

    @Test
    fun escreverNoMeioDaPalavra() {
        montar {
            TelaJogo(gerador.fases(Trilha.PORTUGUES)[60], menina, {}, {}, perguntasProntas = listOf(deMexer(bola)))
        }
        regra.mainClock.advanceTimeBy(2500)
        escreverLetraCerta()
        escreverLetraCerta()
        // Começa a terceira letra com o dedo (um traço do L), ainda sem levantar o dedo para ler.
        regra.onNodeWithTag("escrever_area").performTouchInput {
            val l = TracosDasLetras.tracosDe("L")[0].map { TracosDasLetras.paraTela(it, width.toFloat(), height.toFloat()) }
            down(l[0])
            moveTo(l[1], delayMillis = 60)
        }
        regra.mainClock.advanceTimeBy(300)
        capturar("s11_04_escrever_meio_da_palavra")
        assertTrue(descricao("escrever_casinha_0").contains("Letra B escrita"))
        assertTrue(descricao("escrever_casinha_1").contains("Letra O escrita"))
        assertEquals("jogar nunca pede o microfone", 0, PedidoDoMicrofone.pedidos)
    }

    @Test
    fun escreverMostrarComo() {
        montar { SoAtividade(EscreverPalavra("SAPO", "🐸"), Resultado(), ajudaInicial = true) }
        regra.mainClock.advanceTimeBy(2200)
        capturar("s11_05_escrever_mostrar_como")
    }

    /** Escreve as quatro letras: conclui de primeira e a palavra vai para o caderno. */
    @Test
    fun escreverAteOFimGuardaNoCaderno() {
        val r = Resultado()
        montar { SoAtividade(bola, r) }
        regra.mainClock.advanceTimeBy(1500)
        repeat(4) { escreverLetraCerta() }
        regra.mainClock.advanceTimeBy(1500)
        assertEquals(true, r.concluiu)
        assertEquals(0, r.erros)
        val caderno = CadernoEmArquivo.em(pastaDoApp).ler()
        assertEquals(listOf("BOLA"), caderno.map { it.palavra })
        assertEquals(4, caderno[0].letras.size)
    }

    /** A letra escrita com o dedo de verdade (gesto na área) é lida e vai para a casinha. */
    @Test
    fun escreverComODedo() {
        val r = Resultado()
        montar { SoAtividade(EscreverPalavra("LUA", "🌙"), r) }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithTag("escrever_area").performTouchInput {
            val l = TracosDasLetras.tracosDe("L")[0].map { TracosDasLetras.paraTela(it, width.toFloat(), height.toFloat()) }
            down(l[0])
            for (k in 1..10) moveTo(l[0] + (l[1] - l[0]) * (k / 10f), delayMillis = 20)
            for (k in 1..10) moveTo(l[1] + (l[2] - l[1]) * (k / 10f) + Offset(0f, 3f * sin(k.toFloat())), delayMillis = 20)
            up()
        }
        regra.mainClock.advanceTimeBy(2000)
        assertTrue(descricao("escrever_casinha_0"), descricao("escrever_casinha_0").contains("Letra L escrita"))
        assertEquals(0, r.erros)
        // Um rabisco no lugar do U não vale (e não pune: só conta a tentativa).
        regra.onNodeWithTag("escrever_area").performTouchInput {
            down(Offset(width * 0.2f, height * 0.2f))
            moveTo(Offset(width * 0.8f, height * 0.3f), delayMillis = 30)
            moveTo(Offset(width * 0.2f, height * 0.4f), delayMillis = 30)
            moveTo(Offset(width * 0.8f, height * 0.6f), delayMillis = 30)
            moveTo(Offset(width * 0.3f, height * 0.8f), delayMillis = 30)
            up()
        }
        regra.mainClock.advanceTimeBy(2000)
        assertEquals(1, r.erros)
        assertTrue(descricao("escrever_casinha_1").contains("Letra da vez"))
    }

    @Test
    fun escreverEmIngles() {
        montar { SoAtividade(EscreverPalavra("CAT", "🐱", Idioma.EN), Resultado()) }
        regra.mainClock.advanceTimeBy(1500)
        escreverLetraCerta()
        capturar("s11_06_escrever_ingles")
    }

    // ------------------------------------------------------------------ meu caderno

    /** Palavras "escritas à mão": os modelos das letras com uma ondinha, como o dedo de uma criança. */
    private fun paginas(n: Int): List<PaginaDoCaderno> {
        val r = Random(7)
        val palavras = listOf("BOLA", "GATO", "SAPO", "PATO", "LUA", "CASA", "UVA", "SOL", "MALA", "DADO")
        return List(n) { i ->
            val p = palavras[i % palavras.size]
            val letras = p.map { c ->
                ModelosDasLetras.tracos(c).map { t ->
                    val f = 1f + r.nextFloat() * 2f
                    val fase = r.nextFloat() * 6f
                    val inclina = (r.nextFloat() - 0.5f) * 0.08f
                    t.mapIndexed { k, q ->
                        val s = k / t.size.toFloat()
                        Ponto(q.x + 0.025f * sin(6.28f * f * s + fase) + inclina * (q.y / TracosDasLetras.PROPORCAO - 0.5f), q.y / TracosDasLetras.PROPORCAO + 0.02f * sin(6.28f * s + fase))
                    }
                }
            }
            PaginaDoCaderno(p, Data(2026, 11, 3).diaEpoca - (n - i) / 3, letras)
        }
    }

    @Test
    fun meuCaderno() {
        montar { TelaCaderno(paginas(8), onVoltar = {}) }
        regra.mainClock.advanceTimeBy(800)
        capturar("s11_07_meu_caderno")
    }

    @Test
    fun meuCadernoVazio() {
        montar { TelaCaderno(emptyList(), onVoltar = {}) }
        regra.mainClock.advanceTimeBy(800)
        capturar("s11_08_meu_caderno_vazio")
    }
}
