package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.BalaoDeFala
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.ContasDeProgresso
import br.com.casinhadosaber.ui.componentes.CorDaConta
import br.com.casinhadosaber.ui.componentes.EstrelasDeProgresso
import br.com.casinhadosaber.ui.componentes.IconeDeTrilha
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.LadoDaCauda
import br.com.casinhadosaber.ui.componentes.PilulaDeValor
import br.com.casinhadosaber.ui.componentes.TextoComContorno
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.componentes.idDoIconeDaTrilha
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 * Sistema de interface do visual brinquedo 3D (Fase 2 do PRD visual 3D), num celular pequeno (360 dp):
 * botões de bloco, botões redondos com os ícones 3D da barra, títulos com contorno, pílulas, contas,
 * estrelas, ícones dos caminhos, balão e cartão. Antes de cada captura confere que a arte existe, que a
 * barra não mostra emoji e que nenhum texto saiu cortado.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h800dp-xhdpi")
class CapturasInterface3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val ceu = Brush.verticalGradient(listOf(Color(0xFF7FC8FF), Color(0xFFBFE3FF), Color(0xFFFFD6C4), Color(0xFFFFC2DC)))

    /** Os emojis da barra que já têm ícone 3D (com a descrição de cada botão). */
    private val daBarra = listOf(
        "✖️" to "Fechar", "⬅️" to "Voltar", "🔊" to "Desligar som", "🔇" to "Ligar som",
        "⚙️" to "Ajustes", "🔒" to "Área dos pais", "🏠" to "Casa", "🛒" to "Lojinha", "▶️" to "Próxima fase",
    )

    private fun montar(conteudo: @Composable () -> Unit) {
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                Box(Modifier.fillMaxSize().background(ceu)) { conteudo() }
            }
        }
        regra.mainClock.advanceTimeBy(600)
    }

    /**
     * Nenhum texto visível sai da tela (cabe em 360 dp). Mede os limites de cada nó de texto contra a raiz.
     * O `hasVisualOverflow` do layout não serve aqui: no Robolectric acusa a Baloo 2 ExtraBold mesmo com o texto
     * inteiro na tela (visto nas execuções 118 e 119 do CI).
     */
    private fun conferirTextosInteiros() {
        val comTexto = SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult)
        val nos = regra.onAllNodes(comTexto, useUnmergedTree = true).fetchSemanticsNodes()
        assertTrue("a tela deveria ter textos", nos.isNotEmpty())
        val raiz = regra.onRoot().fetchSemanticsNode().boundsInRoot
        for (no in nos) {
            val r = no.boundsInRoot
            val texto = no.config.getOrNull(androidx.compose.ui.semantics.SemanticsProperties.Text)?.joinToString { it.text } ?: "?"
            assertTrue("texto fora da tela: '$texto' em $r (tela $raiz)", r.left >= raiz.left - 1f && r.right <= raiz.right + 1f)
        }
    }

    private fun conferirArte(ids: List<String>) {
        val app = RuntimeEnvironment.getApplication()
        for (id in ids) {
            val info = ArteDoApp.info(app, id)
            assertNotNull("'$id' fora do manifesto de assets/arte", info)
            assertNotNull("'$id' não decodificou", ArteDoApp.bitmap(app, info!!, 1))
        }
    }

    @Test fun botoesDeBrinquedo() {
        conferirArte(IconesDaBarra.todos)
        montar {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TextoComContorno("Casinha do Saber", estilo = MaterialTheme.typography.headlineLarge)
                // Botões redondos da barra: ícones 3D no lugar dos emojis.
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((emoji, descricao) in daBarra.take(5)) BotaoRedondo(emoji, descricao, {}, tamanho = 52.dp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for ((emoji, descricao) in daBarra.drop(5)) BotaoRedondo(emoji, descricao, {}, tamanho = 52.dp)
                    BotaoRedondo("🛒", "Lojinha aberta", {}, cor = Cores.Estrela, tamanho = 52.dp)
                }
                // Respostas como na maquete 2: números brancos com contorno escuro em blocos coloridos.
                val respostas = listOf("3" to Color(0xFFFF5A4E), "5" to Color(0xFF3E8EF7), "4" to Color(0xFF34C46A), "6" to Color(0xFFFFB020))
                for (linha in respostas.chunked(2)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for ((n, cor) in linha) {
                            BotaoFofo({}, Modifier.weight(1f).height(84.dp), cor = cor, descricao = "Resposta $n", preencher = true) {
                                TextoComContorno(n, estilo = MaterialTheme.typography.displayMedium, corDoContorno = Cores.ContornoEscuro)
                            }
                        }
                    }
                }
                BotaoFofo({}, Modifier.fillMaxWidth(), cor = Color(0xFF34C46A), descricao = "Pôr na casinha") {
                    Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextoComContorno("Pôr na casinha", estilo = MaterialTheme.typography.headlineMedium, corDoContorno = Cores.ContornoEscuro)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotaoFofo({}, Modifier.weight(1f), cor = Color.White, descricao = "Cancelar") {
                        Text("Cancelar", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 12.dp))
                    }
                    BotaoFofo({}, Modifier.weight(1f), cor = Cores.Roxo, habilitado = false, descricao = "Desabilitado") {
                        Text("Depois", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
        }
        // Nada de emoji de sistema na barra: os botões estão lá, mas os emojis não foram desenhados.
        for ((emoji, descricao) in daBarra) {
            assertEquals(descricao, 1, regra.onAllNodesWithContentDescription(descricao).fetchSemanticsNodes().size)
            assertTrue("o emoji $emoji apareceu na barra", regra.onAllNodesWithText(emoji).fetchSemanticsNodes().isEmpty())
        }
        conferirTextosInteiros()
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_02_interface_botoes.png")
    }

    @Test fun pilulasContasEBaloes() {
        conferirArte(
            CorDaConta.entries.map { it.id } + CorDaConta.VAZIA + listOf("estrela_cheia", "estrela_vazia", "estrela_brilhando", "moeda") +
                Trilha.entries.map { idDoIconeDaTrilha(it) },
        )
        montar {
            Column(
                Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // A barra do jogo como na maquete 2: fechar, contas do progresso e som.
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BotaoRedondo("✖️", "Sair da fase", {}, tamanho = 48.dp)
                    Spacer(Modifier.width(8.dp))
                    ContasDeProgresso(2, 5, Modifier.weight(1f), descricao = "Pergunta 3 de 5")
                    Spacer(Modifier.width(8.dp))
                    BotaoRedondo("🔊", "Ouvir de novo", {}, tamanho = 48.dp)
                }
                // Placar como na maquete 1 e moedas como na 3.
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PilulaDeValor("estrela_cheia", "27", descricao = "27 estrelas")
                    PilulaDeValor("moeda", "+5", descricao = "Mais 5 moedas")
                }
                // Contas de todas as cores e muitas contas numa linha (encolhem para caber).
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ContasDeProgresso(3, 3, cor = CorDaConta.VERMELHA, tamanho = 22.dp, comAtual = false)
                    ContasDeProgresso(1, 3, cor = CorDaConta.AZUL, tamanho = 22.dp)
                }
                ContasDeProgresso(6, 12, Modifier.fillMaxWidth(), cor = CorDaConta.ROXA)
                // Estrelas do caminho (maquete 1) e a última conquistada brilhando.
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    EstrelasDeProgresso(3, total = 5, tamanho = 22.dp, descricao = "3 de 5 estrelas")
                    EstrelasDeProgresso(2, total = 3, tamanho = 30.dp, brilharUltima = true)
                }
                // Ícones 3D dos caminhos.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (t in Trilha.entries) IconeDeTrilha(t, Modifier.size(64.dp), descricao = t.titulo)
                }
                BalaoDeFala("Quantos patinhos tem na lagoa?", Modifier.fillMaxWidth(), cauda = LadoDaCauda.Esquerda)
                Box(Modifier.fillMaxWidth().cartaoFofo(labio = Color(0xFF2EC4B6), raio = 24.dp).padding(16.dp)) {
                    Text("Cartão com lábio da cor do caminho", style = MaterialTheme.typography.titleMedium)
                }
                Box(Modifier.fillMaxWidth().cartaoFofo(raio = 24.dp).padding(16.dp)) {
                    Text("Cartão claro (área dos pais)", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        for (t in Trilha.entries) assertEquals(1, regra.onAllNodesWithContentDescription(t.titulo).fetchSemanticsNodes().size)
        // Progresso sem número abstrato: nenhum "x/y" na tela.
        assertTrue(regra.onAllNodesWithText("/", substring = true).fetchSemanticsNodes().isEmpty())
        conferirTextosInteiros()
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_03_interface_pilulas.png")
    }
}
