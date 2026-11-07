package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Livrinhos
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pintura
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaAtelie
import br.com.casinhadosaber.ui.telas.TelaConviteNivel
import br.com.casinhadosaber.ui.telas.TelaEstante
import br.com.casinhadosaber.ui.telas.TelaLivrinho
import br.com.casinhadosaber.ui.telas.TelaMapa
import br.com.casinhadosaber.ui.telas.TelaPiano
import br.com.casinhadosaber.ui.telas.TelaTrilha
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
 * Fase 8B, grupo B, no visual brinquedo 3D: cada captura põe várias telas lado a lado, cada uma num celular de
 * 360 x 720 dp (sem a escala de tablet), para conferir de uma vez que nada fica cortado na tela mais estreita.
 *
 * * `v3d_16_trilha_e_mapa`: o caminho com o botão do mapa e as estrelas do caminho, o caminho novo com
 *   "Já sei bastante!" e o presente do próximo prêmio, o mapa dos 25 mundos com contas e medalhas, o convite do
 *   nível com a Corujinha e o caminho todo vencido (cinco estrelas na pílula, nunca "250/250").
 * * `v3d_17_atelie_piano_livros`: o ateliê (paleta, lápis, balde e borracha 3D), o piano seguindo a música, a
 *   estante, a página do livrinho (as páginas em contas) e a pergunta do livrinho (balão com o som 3D).
 *
 * As capturas 04, 04b, 40, 41, 51, 61, s5_*, s6_* e s10_* destas telas mudam sozinhas.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1800dp-h720dp-hdpi")
class CapturasTelas3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)

    private fun montar(paineis: List<@Composable () -> Unit>) {
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons, LocalEscalaAplicada provides true) {
                    Row(Modifier.fillMaxSize()) {
                        for (p in paineis) {
                            Box(Modifier.width(LARGURA.dp).fillMaxHeight().clipToBounds()) { p() }
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(2500)
    }

    /** Em Números os mundos 1 e 2 completos e o 3 pela metade; Letras, Inglês e Lógica com algumas fases. */
    private fun progresso(): EstadoJogo {
        val estrelas = HashMap<String, Int>()
        for (n in 0 until 25) estrelas[EstadoJogo.chave(Trilha.MATEMATICA, n)] = if (n < 10) 3 else 1 + n % 3
        for (n in 0 until 62) estrelas[EstadoJogo.chave(Trilha.PORTUGUES, n)] = 2 + n % 2
        for (n in 0 until 3) estrelas[EstadoJogo.chave(Trilha.LOGICA, n)] = 2
        var e = EstadoJogo(perfil = menina, estrelas = estrelas, livrinhosLidos = setOf("pato", "bola", "chuva"))
        for (item in ItensDaCasa.ordem.take(30)) e = e.colocar(item)
        return e
    }

    /** O caminho de Inglês inteiro vencido (sem próximo prêmio: a pílula mostra as cinco estrelas). */
    private fun inglesCompleto(): EstadoJogo {
        val estrelas = HashMap<String, Int>()
        for (n in 0 until 250) estrelas[EstadoJogo.chave(Trilha.INGLES, n)] = 3
        return EstadoJogo(perfil = menina, estrelas = estrelas)
    }

    private fun conferirArte(ids: List<String>) {
        val contexto = RuntimeEnvironment.getApplication()
        for (id in ids) {
            val info = ArteDoApp.info(contexto, id)
            assertNotNull("'$id' não está no manifesto", info)
            assertNotNull("'$id' não decodificou", ArteDoApp.bitmap(contexto, info!!, 2))
        }
    }

    private fun existe(descricao: String, substring: Boolean = false) =
        regra.onAllNodes(hasContentDescription(descricao, substring = substring)).fetchSemanticsNodes().isNotEmpty()

    private fun existeTag(tag: String) = regra.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    /**
     * Nenhum texto passa da borda do seu celular (360 dp) e nenhum progresso aparece como "3/250" para a criança.
     * Mede os limites de cada nó de texto, como `CapturasInterface3DTest` (o `hasVisualOverflow` não serve no
     * Robolectric com a Baloo 2).
     */
    private fun conferirTextos() {
        val comTexto = SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult)
        val nos = regra.onAllNodes(comTexto, useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("as telas deveriam ter textos", nos.isNotEmpty())
        val largura = with(regra.density) { LARGURA.dp.toPx() }
        val fracao = Regex("""\d+\s*/\s*\d+""")
        for (no in nos) {
            val texto = no.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }.orEmpty()
            assertTrue("progresso como número na tela: '$texto'", !fracao.containsMatchIn(texto))
            val r = no.boundsInRoot
            // O que está numa fileira que rola de lado (as músicas do piano) pode passar da borda: é só rolar.
            if (r.width <= 0f || dentroDeRolagemDeLado(no)) continue
            val painel = (r.left / largura).toInt()
            assertTrue(
                "texto passa da borda do celular: '$texto' em $r",
                r.left >= painel * largura - 1f && r.right <= (painel + 1) * largura + 1f,
            )
        }
    }

    private fun dentroDeRolagemDeLado(no: SemanticsNode): Boolean {
        var p = no.parent
        while (p != null) {
            if (p.config.getOrNull(SemanticsProperties.HorizontalScrollAxisRange) != null) return true
            p = p.parent
        }
        return false
    }

    @Test fun trilhaEMapa() {
        conferirArte(
            listOf(
                "icone_trilha_matematica", "icone_trilha_logica", "icone_trilha_ingles", "icone_mapa", "icone_album",
                "estrela_cheia", "estrela_vazia", "conta_laranja", "conta_vazia", "premio_coroa", "premio_trofeu",
                "premio_presente_fechado", "tela_medalha", "icone_pais", "moeda",
            ),
        )
        montar(
            listOf(
                { TelaTrilha(Trilha.MATEMATICA, progresso(), {}, {}, onTreinar = {}, onMapa = {}, relembrar = 2) },
                { TelaTrilha(Trilha.LOGICA, EstadoJogo(perfil = menina), {}, {}, onDescobrirNivel = {}) },
                { TelaMapa(Trilha.MATEMATICA, progresso(), {}, {}) },
                { TelaConviteNivel(Trilha.MATEMATICA, "Ana", onComecar = {}, onAgoraNao = {}) },
                { TelaTrilha(Trilha.INGLES, inglesCompleto(), {}, {}, onTreinar = {}) },
            ),
        )
        // O que os testes do emulador procuram continua lá.
        assertTrue(existe("Mapa dos mundos"))
        assertTrue(existe("Voltar para o caminho"))
        assertTrue(existe("Álbum de figurinhas"))
        assertTrue(existe("Mundo 1, Jardim, completo"))
        assertTrue(existe("Fase 1, ", substring = true))
        assertTrue(existeTag("abrir_nivel"))
        assertTrue(existeTag("nivel_agora_nao"))
        assertTrue(existeTag("nivel_comecar"))
        assertTrue(existeTag("abrir_relembrar"))
        assertTrue(existe("250 de 250 fases vencidas"))
        conferirTextos()
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_16_trilha_e_mapa.png")
    }

    @Test fun atelieePianoELivros() {
        conferirArte(
            listOf(
                "tela_paleta", "tela_pincel", "tela_lapis", "tela_balde", "tela_borracha", "tela_piano",
                "tela_nota_musical", "tela_livro_fechado", "tela_livro_aberto", "icone_avancar", "icone_som_ligado",
                "icone_desfazer", "icone_papel_novo", "icone_lixeira", "ui_mao_toque", "ui_mao_toque_apertando",
            ),
        )
        val desenho = Pintura("d1", List(Pintura.LADO * Pintura.LADO) { i -> if ((i / Pintura.LADO + i % Pintura.LADO) % 5 == 0) 6 else 0 })
        val comDesenho = progresso().copy(desenhos = listOf(desenho))
        val livro = Livrinhos.porId.getValue("pato")
        montar(
            listOf(
                { TelaAtelie(comDesenho, {}, {}, abrirDesenho = "d1") },
                { TelaPiano(onVoltar = {}, musicaInicial = "brilha", posicaoInicial = 2) },
                { TelaEstante(progresso(), onAbrir = {}, onVoltar = {}) },
                { TelaLivrinho(livro, "Ana", onTerminar = {}, onVoltar = {}, inicio = 1) },
                { TelaLivrinho(livro, "Ana", onTerminar = {}, onVoltar = {}, inicio = livro.paginas.size) },
            ),
        )
        assertTrue(existeTag("quadro_de_pintar"))
        assertTrue(existeTag("ferramenta_balde"))
        assertTrue(existeTag("tecla_do"))
        assertTrue(existeTag("livrinho_pato"))
        assertTrue(existeTag("pagina_seguinte"))
        assertTrue(existeTag("resposta_certa"))
        assertTrue(existe("Página 2 de ${livro.paginas.size}"))
        assertTrue(existe("3 de ${Livrinhos.todos.size} livrinhos lidos"))
        conferirTextos()
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_17_atelie_piano_livros.png")
    }

    private companion object {
        /** Largura de cada celular na captura (o menor do PRD). */
        const val LARGURA = 360
    }
}
