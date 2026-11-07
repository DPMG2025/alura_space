package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.PecasDoPremio
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaPremio
import br.com.casinhadosaber.ui.telas.TelaQuase
import com.github.takahirom.roborazzi.captureRoboImage
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
 * Prêmio e Quase no visual brinquedo 3D (Fase 5 do PRD visual 3D). Capturas novas, para comparar com a maquete
 * `docs/visual-3d/maquetes/3_premio.png`: `v3d_06_premio` (o presente aberto, a cama saindo da caixa, o pódio com a
 * criança e o irmãozinho torcendo) e `v3d_07_quase` (a estrela dormindo e a semente crescida). As capturas 11, 11b,
 * 38, 39, 57 e 73 mostram as telas novas sem mudar de código.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasPremio3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val ana = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1, irmao = Irmao(genero = Genero.MENINO, nome = "Theo"))
    private val leo = Perfil("Leo", Genero.MENINO, pele = 0, cabelo = 3)
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

    /** Toda a arte das duas telas está no manifesto e decodifica (sem isso, as telas cairiam nos desenhos de reserva). */
    private fun conferirArte(ids: List<String>) {
        val contexto = RuntimeEnvironment.getApplication()
        for (id in ids) {
            val info = ArteDoApp.info(contexto, id)
            assertNotNull("'$id' não está no manifesto", info)
            assertNotNull("'$id' não decodificou", ArteDoApp.bitmap(contexto, info!!, 2))
        }
    }

    /** Nada de emoji de sistema nas duas telas (moeda, mãozinha, cadeado, setas), só arte 3D. */
    private fun semEmojis(vararg emojis: String) {
        for (e in emojis) {
            assertTrue("emoji $e na tela", regra.onAllNodes(hasText(e, substring = true)).fetchSemanticsNodes().isEmpty())
        }
    }

    @Test fun premio() {
        conferirArte(PecasDoPremio.idsDaArte + PecasDoPremio.idDoPremio("cama"))
        montar { TelaPremio(gerador.fases(Trilha.INGLES)[2], 3, ItensDaCasa.porId.getValue("cama"), ana, 5, true, {}, {}, {}) }
        regra.mainClock.advanceTimeBy(2500)
        // Os nomes que os testes do emulador procuram continuam: o presente fechado e, depois de abrir, o botão.
        regra.onAllNodes(hasContentDescription("Presente. Toque para abrir.")).onFirst().performClick()
        regra.mainClock.advanceTimeBy(1600)
        assertTrue(regra.onAllNodes(hasContentDescription("Colocar na casa")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasContentDescription("Mais 5 moedinhas")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasText("Pôr na casinha")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasContentDescription("Voltar para o caminho")).fetchSemanticsNodes().isNotEmpty())
        semEmojis("🪙", "👆", "🏛", "🏠", "🗺")
        // O nome em inglês numa pílula clara (antes, branco sobre o lilás claro do fundo).
        assertTrue(regra.onAllNodes(hasText("Em inglês: bed")).fetchSemanticsNodes().isNotEmpty())
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_06_premio.png")
    }

    @Test fun quase() {
        conferirArte(listOf(PecasDoPremio.ESTRELA_DORMINDO, PecasDoPremio.idDaSemente(3), PecasDoPremio.PRESENTE_FECHADO))
        montar { TelaQuase(gerador.fases(Trilha.LOGICA)[3], 3, leo, {}, {}) }
        // A semente cresce do 1 ao 3 (três acertos de primeira).
        regra.mainClock.advanceTimeBy(3000)
        assertTrue(regra.onAllNodes(hasContentDescription("Tentar de novo")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasContentDescription("Voltar para o caminho")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasContentDescription("Você acertou 3", substring = true)).fetchSemanticsNodes().isNotEmpty())
        semEmojis("🔒", "🎁", "🔄", "👆", "🗺")
        anelDentroDoBotao(regra)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_07_quase.png")
    }
}

/** O anel do toque do "De novo" (já visível) fica inteiro dentro do botão: não passa da borda direita. */
internal fun anelDentroDoBotao(regra: androidx.compose.ui.test.junit4.ComposeContentTestRule) {
    val aneis = regra.onAllNodes(androidx.compose.ui.test.hasTestTag("anel_de_toque"), useUnmergedTree = true).fetchSemanticsNodes()
    assertTrue("o anel do toque devia estar na tela", aneis.isNotEmpty())
    val botao = regra.onAllNodes(hasContentDescription("Tentar de novo")).fetchSemanticsNodes().first().boundsInRoot
    for (a in aneis) {
        val r = a.boundsInRoot
        assertTrue("anel do toque fora do botão: $r (botão $botao)", r.left >= botao.left - 1f && r.right <= botao.right + 1f && r.top >= botao.top - 1f && r.bottom <= botao.bottom + 1f)
    }
}
