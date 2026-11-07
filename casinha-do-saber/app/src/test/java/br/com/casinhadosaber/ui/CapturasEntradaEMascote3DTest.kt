package br.com.casinhadosaber.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.AcessorioMascote
import br.com.casinhadosaber.dominio.CriancaDaCasa
import br.com.casinhadosaber.dominio.Diploma
import br.com.casinhadosaber.dominio.EspecieMascote
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Mascote
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.PecasDasTelas
import br.com.casinhadosaber.ui.componentes.PecasDoMascote
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.AvisoDeConquistas
import br.com.casinhadosaber.ui.telas.CantinhoDoMascote
import br.com.casinhadosaber.ui.telas.EscolhaDoMascote
import br.com.casinhadosaber.ui.telas.NovasConquistas
import br.com.casinhadosaber.ui.telas.TelaAlbum
import br.com.casinhadosaber.ui.telas.TelaBoasVindas
import br.com.casinhadosaber.ui.telas.TelaDiplomas
import br.com.casinhadosaber.ui.telas.TelaDormir
import br.com.casinhadosaber.ui.telas.TelaPerfil
import br.com.casinhadosaber.ui.telas.TelaQuemVaiJogar
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

/**
 * Fase 8, grupo A do PRD visual 3D: telas de entrada, do mascote, de descanso e das conquistas. Cada captura põe
 * várias telas lado a lado, cada uma num painel de 360 dp (o celular pequeno, onde nada pode ficar cortado):
 *
 * * `v3d_14_entrada`: escolha do personagem (os bonecos 3D na ilhinha, sem a mão 👆 entre cartões), quem vai jogar
 *   e as boas vindas;
 * * `v3d_15_mascote_e_conquistas`: escolha do mascote (gatinho marcado, pose feliz), cantinho do mascote
 *   (dragãozinho de lacinho, feliz depois de falar), álbum com o aviso de figurinha, diploma aberto (medalha 3D) e boa noite (lua 3D).
 *
 * Antes de capturar: a arte no manifesto e decodificável, os nomes que os testes do emulador procuram e nenhum
 * texto saindo do painel. Cada painel recebe a configuração de um celular de 360 dp (a escala do tablet não entra).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class CapturasEntradaEMascote3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val ana = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1, irmao = Irmao(genero = Genero.MENINO, nome = "Theo"))
    private val leo = Perfil("Leo", Genero.MENINO, pele = 4, cabelo = 0, estiloCabelo = 2)

    private fun montar(paineis: List<Pair<String, @Composable () -> Unit>>) {
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    Row(Modifier.fillMaxSize()) {
                        for ((nome, conteudo) in paineis) {
                            // Cada painel é um celular de 360 dp: a configuração dele não é a da folha larga.
                            val celular = Configuration(LocalConfiguration.current).apply {
                                screenWidthDp = PAINEL
                                smallestScreenWidthDp = PAINEL
                                orientation = Configuration.ORIENTATION_PORTRAIT
                            }
                            CompositionLocalProvider(LocalConfiguration provides celular) {
                                Box(Modifier.width(PAINEL.dp).fillMaxHeight().testTag("painel_$nome")) { conteudo() }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun conferirArte(ids: List<String>) {
        val contexto = RuntimeEnvironment.getApplication()
        for (id in ids) {
            val info = ArteDoApp.info(contexto, id)
            assertNotNull("'$id' não está no manifesto", info)
            assertNotNull("'$id' não decodificou", ArteDoApp.bitmap(contexto, info!!, 4))
        }
    }

    /** Nenhum texto passa da borda do painel dele (360 dp): mede cada nó de texto contra o painel. */
    private fun conferirTextosNosPaineis(nomes: List<String>) {
        val comTexto = SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult)
        for (nome in nomes) {
            val painel = regra.onAllNodes(hasTestTag("painel_$nome")).fetchSemanticsNodes().single().boundsInRoot
            val nos = regra.onAllNodes(comTexto, useUnmergedTree = true).fetchSemanticsNodes()
                .filter { it.boundsInRoot.center.x in painel.left..painel.right && !dentroDeRolagemHorizontal(it) }
            for (no in nos) {
                val r = no.boundsInRoot
                val texto = no.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }.orEmpty()
                assertTrue("[$nome] texto fora do painel: '$texto' em $r", r.left >= painel.left - 1f && r.right <= painel.right + 1f)
            }
        }
    }

    /** Texto numa fileira que rola para o lado (os caminhos do álbum) pode passar da borda: é só rolar. */
    private fun dentroDeRolagemHorizontal(no: SemanticsNode): Boolean {
        var p = no.parent
        while (p != null) {
            if (SemanticsProperties.HorizontalScrollAxisRange in p.config) return true
            p = p.parent
        }
        return false
    }

    private fun semEmojis(vararg emojis: String) {
        for (e in emojis) {
            assertTrue("emoji $e na tela", regra.onAllNodes(hasText(e, substring = true), useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        }
    }

    private fun existe(descricao: String) =
        assertTrue("'$descricao' sumiu", regra.onAllNodes(hasContentDescription(descricao)).fetchSemanticsNodes().isNotEmpty())

    @Test
    @Config(sdk = [34], qualifiers = "w1080dp-h800dp-hdpi")
    fun entrada() {
        conferirArte(PecasDasTelas.idsDaArte.filter { !it.startsWith("tela_") })
        var casa = EstadoJogo(perfil = ana)
        for (item in ItensDaCasa.ordem.take(1)) casa = casa.colocar(item)
        val criancas = listOf(
            CriancaDaCasa("1", ana),
            CriancaDaCasa("2", leo),
            CriancaDaCasa("3", null),
        )
        montar(
            listOf(
                "perfil" to @Composable { TelaPerfil(null) {} },
                "quem" to @Composable { TelaQuemVaiJogar(criancas) {} },
                "boas_vindas" to @Composable { TelaBoasVindas(casa) {} },
            ),
        )
        regra.mainClock.advanceTimeBy(3000)
        // O que o JogoNoEmuladorTest procura: o botão "Menina" e o título das boas vindas.
        assertTrue(regra.onAllNodes(hasText("Menina")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasText("Vamos construir a sua casinha!")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasTestTag("crianca_2")).fetchSemanticsNodes().isNotEmpty())
        existe("Menino")
        semEmojis("👆", "🌱")
        conferirTextosNosPaineis(listOf("perfil", "quem", "boas_vindas"))
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_14_entrada.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w1800dp-h800dp-hdpi")
    fun mascoteEConquistas() {
        conferirArte(PecasDoMascote.idsDaArte.filter { "gatinho" in it || "coruja" in it } + PecasDasTelas.idsDaArte.filter { it.startsWith("tela_") })
        val estrelas = HashMap<String, Int>()
        // Mundo 1 completo (diploma) e, no mundo 2, figurinhas coladas e uma que falta (a fase 12, com 2 estrelas).
        for (n in 0 until 14) estrelas[EstadoJogo.chave(Trilha.MATEMATICA, n)] = if (n == 11) 2 else 3
        var progresso = EstadoJogo(perfil = ana, estrelas = estrelas)
        for (item in ItensDaCasa.ordem.take(20)) progresso = progresso.colocar(item)
        val antes = progresso.estrelas - EstadoJogo.chave(Trilha.MATEMATICA, 13)
        val novas = NovasConquistas.entre(antes, progresso.estrelas)
        montar(
            listOf(
                "escolha" to @Composable { EscolhaDoMascote { _, _ -> } },
                "cantinho" to @Composable {
                    CantinhoDoMascote(Mascote(EspecieMascote.DRAGAOZINHO, "Faísca", AcessorioMascote.LACINHO.name), 45, "Ana", {}, {})
                },
                "album" to @Composable {
                    Box(Modifier.fillMaxSize()) {
                        TelaAlbum(progresso, Trilha.MATEMATICA, 1, onVoltar = {})
                        AvisoDeConquistas(novas, "Ana", {}, {}, Modifier.align(Alignment.BottomCenter))
                    }
                },
                "diploma" to @Composable { TelaDiplomas(progresso, {}, abrirDiploma = Diploma(Trilha.MATEMATICA, 0)) },
                "dormir" to @Composable { TelaDormir(progresso) {} },
            ),
        )
        regra.mainClock.advanceTimeBy(600)
        regra.onNode(hasTestTag("mascote_GATINHO")).performClick()
        regra.mainClock.advanceTimeBy(2400)
        // Marcas e nomes de antes continuam (CapturasMascoteTest, CapturasCriativoTest, emulador).
        assertTrue(regra.onAllNodes(hasTestTag("mascote_pronto")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasTestTag("figurinha_ganha")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasTestTag("aviso_conquista")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(regra.onAllNodes(hasTestTag("diploma_aberto")).fetchSemanticsNodes().isNotEmpty())
        existe("Faísca")
        existe("Adulto")
        semEmojis("👆", "📒", "📜", "🔒", "✔")
        conferirTextosNosPaineis(listOf("escolha", "cantinho", "album", "diploma", "dormir"))
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_15_mascote_e_conquistas.png")
    }

    private companion object {
        const val PAINEL = 360
    }
}
