package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Acao
import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.dominio.Categoria
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Gaveta
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.PrateleiraDaGeladeira
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.IconesDaCasaK2
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaCasa
import br.com.casinhadosaber.ui.telas.TelaCasaPorDentro
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
 * Fase 9, segunda rodada de Kotlin, grupo K2 (casa de fora e por dentro, áreas novas, guarda-roupa e espelho, noite
 * do interior). Captura nova `v3d_21_casa_e_areas_k2`, quatro telas lado a lado:
 * a casa por dentro de noite com o guarda-roupa aberto (céu escuro, lua 3D, abas 3D das gavetas e o retrato sem o
 * vazio branco), a geladeira aberta de dia (comidas 3D, abas 3D das prateleiras, setas 3D da escada, moeda 3D), a
 * horta (abas das áreas com `icone_area_*`, figuras 3D espalhadas na ilha) e o baú vazio com a silhueta cinza do
 * próximo presente. Nenhuma captura antiga foi renomeada.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w820dp-h1240dp-xhdpi")
class CapturasCasaEAreasK2Test {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1, traje = "listras")

    /** [fases] vencidas; com [colocarTudo], tudo o que foi ganho está posto (baú vazio, áreas construídas). */
    private fun estado(fases: Int, colocarTudo: Boolean = true): EstadoJogo {
        val estrelas = (0 until fases).associate { "${Trilha.entries[it % 4].name}:${it / 4}" to 3 }
        var e = EstadoJogo(perfil = menina, estrelas = estrelas)
        if (colocarTudo) {
            for (item in e.itensGanhos.filter { it.area == null }) e = e.colocar(item)
            for (item in e.itensGanhos.filter { it.area != null }) e = e.colocar(item)
        }
        return e
    }

    /** A casa inteira, com comidas na geladeira e roupas no guarda-roupa (como a captura 33). */
    private fun casaCheia(): EstadoJogo {
        val compras = Loja.produtos.filter { it.categoria == Categoria.COMIDA }.take(6).map { it.id } +
            Loja.produtos.filter { it.categoria == Categoria.ROUPA }.take(3).map { it.id }
        return estado(40).copy(moedas = 23, comprados = compras.toSet())
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

    private fun existeDescricao(texto: String) =
        regra.onAllNodes(hasContentDescription(texto, substring = true)).fetchSemanticsNodes().isNotEmpty()

    /** Nenhum texto visível com estes emojis de sistema (a arte 3D está no lugar deles). */
    private fun semEmojis(vararg emojis: String) {
        for (e in emojis) {
            val nos = regra.onAllNodes(hasText(e, substring = true), useUnmergedTree = true).fetchSemanticsNodes()
            assertTrue("emoji de sistema '$e' na tela", nos.isEmpty())
        }
    }

    /** Toda a arte nova da rodada existe e é lida. */
    private fun conferirArte() {
        val app = RuntimeEnvironment.getApplication()
        val ids = listOf(IconesDaBarra.SUBIR, IconesDaBarra.DESCER, IconesDaBarra.CERTO, IconesDaBarra.MARTELO, "item_sol", "item_sol_noite", "tela_zzz") +
            IconesDaCasaK2.fixos + CasaQueCresce.areas.map { IconesDaCasaK2.daArea(it.id) }
        for (id in ids) {
            val info = ArteDoApp.info(app, id)
            assertNotNull("'$id' fora do manifesto", info)
            assertNotNull("'$id' não decodificou", ArteDoApp.bitmap(app, info!!, 2))
        }
    }

    @Test fun casaEAreasK2() {
        conferirArte()
        val horta = CasaQueCresce.porId.getValue("horta")
        val comAreas = estado(600)
        assertTrue("a horta não foi construída", horta.obra.id in comAreas.colocados)
        // Baú vazio, com um próximo presente à vista (a silhueta cinza).
        val bauVazio = estado(5)
        assertNotNull("sem próximo presente", ItensDaCasa.proximo(bauVazio.fasesVencidas))
        val forma = RoundedCornerShape(18.dp)
        montar {
            Column(Modifier.fillMaxSize().padding(4.dp)) {
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Box(Modifier.weight(1f).fillMaxSize().padding(4.dp).clip(forma)) {
                        TelaCasaPorDentro(casaCheia(), {}, {}, abrirInicial = Acao.GUARDA_ROUPA, noite = true)
                    }
                    Box(Modifier.weight(1f).fillMaxSize().padding(4.dp).clip(forma)) {
                        TelaCasaPorDentro(casaCheia(), {}, {}, abrirInicial = Acao.GELADEIRA)
                    }
                }
                Row(Modifier.weight(1f).fillMaxWidth()) {
                    Box(Modifier.weight(1f).fillMaxSize().padding(4.dp).clip(forma)) {
                        TelaCasa(comAreas, {}, {}, {}, paginaInicial = "horta")
                    }
                    Box(Modifier.weight(1f).fillMaxSize().padding(4.dp).clip(forma)) {
                        TelaCasa(bauVazio, {}, {}, {})
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(2500)
        // Descrições e marcas de sempre (os testes do emulador e as capturas antigas procuram por elas).
        assertTrue(existeDescricao("Casa por dentro"))
        assertTrue(existeDescricao("Você tem 23 moedinhas"))
        assertTrue(existeDescricao("Guarda-roupa aberto"))
        assertTrue(existeDescricao("Geladeira aberta"))
        for (g in Gaveta.entries) assertTrue("aba ${g.nome}", existeDescricao(g.nome))
        for (p in PrateleiraDaGeladeira.entries) assertTrue("aba ${p.nome}", existeDescricao(p.nome))
        assertTrue(regra.onAllNodes(hasText("Horta e pomar")).fetchSemanticsNodes().isNotEmpty())
        assertTrue(existeDescricao("Próximo presente"))
        assertTrue(existeDescricao("Baú de presentes"))
        // Moeda, certo, presente e setas: 3D, nunca o emoji de sistema.
        semEmojis("🪙", "✔", "🎁", "⬆", "⬇", "🔨")
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_21_casa_e_areas_k2.png")
    }

    /** Em 360 dp: o painel do guarda-roupa (título ao lado do retrato) e a pílula da moeda não cortam texto. */
    @Config(qualifiers = "w360dp-h740dp-xhdpi")
    @Test fun guardaRoupaEm360dp() {
        montar { TelaCasaPorDentro(casaCheia(), {}, {}, abrirInicial = Acao.GUARDA_ROUPA) }
        regra.mainClock.advanceTimeBy(2500)
        assertTrue(regra.onAllNodes(hasText("Guarda-roupa")).fetchSemanticsNodes().isNotEmpty())
        val comTexto = SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult)
        val raiz = regra.onRoot().fetchSemanticsNode().boundsInRoot
        for (no in regra.onAllNodes(comTexto, useUnmergedTree = true).fetchSemanticsNodes()) {
            val r = no.boundsInRoot
            val texto = no.config.getOrElse(SemanticsProperties.Text) { emptyList() }.joinToString { it.text }
            assertTrue("texto fora da tela: '$texto' em $r (tela $raiz)", r.left >= raiz.left - 1f && r.right <= raiz.right + 1f)
        }
        semEmojis("🪙", "⬆", "⬇")
    }
}
