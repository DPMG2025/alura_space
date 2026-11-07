package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Cena
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.ComposicaoDoJogo
import br.com.casinhadosaber.ui.componentes.FigurasDaPergunta
import br.com.casinhadosaber.ui.componentes.QuadroDoJogo
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaJogo
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
 * O jogo no visual brinquedo 3D (Fase 4 do PRD visual 3D). Capturas novas, para comparar com a maquete
 * `docs/visual-3d/maquetes/2_jogo.png`: `v3d_11_jogo_contar` (os patinhos 3D na lagoa da ilha dos números, as duas
 * crianças grandes ao lado do balão, as contas 3D na barra e as respostas em blocos coloridos) e
 * `v3d_12_jogo_letras` (as figuras 3D nas respostas, na ilha das letras). As capturas 05 a 10, 24 a 31, 62 a 66 e
 * as do jogo com irmão mostram as telas novas sem mudar de código.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasJogo3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val ana = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1, irmao = Irmao(genero = Genero.MENINO, nome = "Theo"))
    private val leo = Perfil("Leo", Genero.MENINO, pele = 0, cabelo = 3)

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

    /** A arte está no manifesto e decodifica (sem isso, a tela cairia no cenário desenhado e nos emojis). */
    private fun conferirArte(ids: List<String>) {
        val contexto = RuntimeEnvironment.getApplication()
        for (id in ids) {
            val info = ArteDoApp.info(contexto, id)
            assertNotNull("'$id' não está no manifesto", info)
            if (id.startsWith("jogo_")) {
                assertNotNull("'$id' não compôs", ComposicaoDoJogo.camada(contexto, info!!, 400))
            } else {
                assertNotNull("'$id' não decodificou", ArteDoApp.bitmap(contexto, info!!, 2))
            }
        }
    }

    /**
     * O que os testes do emulador procuram continua lá (o balão começa com "🔊", a resposta certa tem a marca), e a
     * barra não tem emoji de sistema (o ✖ e o 🔊 são ícones 3D; as bolinhas com ★ viraram contas 3D).
     */
    private fun conferirTela(instrucao: String) {
        val balao = regra.onAllNodes(hasText("🔊", substring = true)).fetchSemanticsNodes()
        val textos = balao.flatMap { it.config.getOrNull(SemanticsProperties.Text).orEmpty() }.joinToString(" ") { it.text }
        assertTrue("o balão deveria dizer \"$instrucao\": $textos", instrucao in textos)
        assertEquals(1, regra.onAllNodes(hasTestTag("opcao_certa")).fetchSemanticsNodes().size)
        assertEquals(3, regra.onAllNodes(hasTestTag("opcao")).fetchSemanticsNodes().size)
        assertTrue(regra.onAllNodes(hasContentDescription("Sair da fase")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasContentDescription("Ouvir de novo")).fetchSemanticsNodes().isNotEmpty())
        for (e in listOf("✖", "★")) {
            assertTrue("$e na tela", regra.onAllNodes(hasText(e, substring = true)).fetchSemanticsNodes().isEmpty())
        }
    }

    @Test fun jogoContar() {
        val contexto = RuntimeEnvironment.getApplication()
        val manifesto = ArteDoApp.manifesto(contexto)
        assertEquals("fig_pato", FigurasDaPergunta.idPara(manifesto, "🦆"))
        assertEquals("lagoa", FigurasDaPergunta.lugarDaCena(manifesto, List(4) { "🦆" }))
        conferirArte(listOf(QuadroDoJogo.idDaIlha(Trilha.MATEMATICA), QuadroDoJogo.idDaCena("lagoa"), "fig_pato", "conta_amarela", "conta_vazia"))
        val instrucao = "Quantos patinhos tem na lagoa?"
        val pergunta = Pergunta(
            instrucao,
            listOf(pt(instrucao)),
            Cena.Emojis("🦆🦆🦆🦆"),
            listOf("3", "5", "4", "6").map { Opcao(Visual.Texto(it), listOf(pt(it))) },
            indiceCorreto = 2,
        )
        montar {
            TelaJogo(
                Fase(Trilha.MATEMATICA, 1, "Contar até 5", "🦆"),
                ana,
                {},
                {},
                perguntasProntas = listOf(pergunta, pergunta, pergunta, pergunta, pergunta),
            )
        }
        regra.mainClock.advanceTimeBy(2500)
        conferirTela(instrucao)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_11_jogo_contar.png")
    }

    @Test fun jogoLetras() {
        conferirArte(listOf(QuadroDoJogo.idDaIlha(Trilha.PORTUGUES), "fig_porco", "fig_gato", "fig_cachorro", "fig_sapo"))
        val instrucao = "Qual começa com P?"
        val bichos = listOf("🐷" to "porco", "🐱" to "gato", "🐶" to "cachorro", "🐸" to "sapo")
        val pergunta = Pergunta(
            instrucao,
            listOf(pt(instrucao)),
            Cena.Palavra("P"),
            bichos.map { (e, nome) -> Opcao(Visual.Emoji(e), listOf(pt(nome))) },
            indiceCorreto = 0,
        )
        montar {
            TelaJogo(Fase(Trilha.PORTUGUES, 3, "Primeira letra", "🐷"), leo, {}, {}, perguntasProntas = listOf(pergunta, pergunta, pergunta))
        }
        regra.mainClock.advanceTimeBy(2500)
        conferirTela(instrucao)
        // As respostas dizem o nome do bicho (a figura 3D é decorativa, como o emoji era).
        assertTrue(regra.onAllNodes(hasContentDescription("porco")).fetchSemanticsNodes().isNotEmpty())
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_12_jogo_letras.png")
    }
}
