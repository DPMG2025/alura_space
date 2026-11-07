package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.PecasDoPremio
import br.com.casinhadosaber.ui.componentes.PilulaDeMoedas
import br.com.casinhadosaber.ui.componentes.TextoComContorno
import br.com.casinhadosaber.ui.componentes.contraste
import br.com.casinhadosaber.ui.componentes.faceDesligada
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaInicio
import br.com.casinhadosaber.ui.telas.TelaPremio
import br.com.casinhadosaber.ui.telas.TelaQuase
import com.github.takahirom.roborazzi.captureRoboImage
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
import kotlin.random.Random

/**
 * Fase 9 do PRD visual 3D (acabamento): os cinco pontos da revisão independente, cada um num celular de 360 x 720 dp
 * lado a lado, numa captura só (`v3d_19_polimento`):
 *
 * 1. Tela inicial: os bonecos em primeiro plano com o dobro do tamanho, o espelho no cabeçalho (fora do sol), as
 *    estrelas vazias na cor do cartão, os passarinhos sem corte na borda e a missão com moedas que se contam.
 * 2. Prêmio aberto: "+5" ao lado das cinco moedas e "Em inglês: bed" numa pílula clara.
 * 3. Moedas e botões: o número só ao lado de moedas que dão a mesma conta; botões desligados foscos e legíveis.
 * 4. Nomes longos da loja e do guarda-roupa em duas linhas, centralizados, sem reticências e sem encostar.
 * 5. Quase: o presente 3D na pílula e o anel do toque dentro do botão "De novo".
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w1800dp-h720dp-hdpi")
class CapturasPolimento3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val ana = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1, irmao = Irmao(genero = Genero.MENINO, nome = "Theo"))
    private val leo = Perfil("Leo", Genero.MENINO, pele = 0, cabelo = 3)
    private val gerador = GeradorDeFases(Random(4))

    /** Estado com [n] fases vencidas, espalhadas pelas trilhas, e os [colocados] primeiros itens na casa (o sol entre eles). */
    private fun estado(n: Int, colocados: Int): EstadoJogo {
        var e = EstadoJogo(perfil = ana)
        val fases = (0 until 10).flatMap { i -> Trilha.entries.map { gerador.fases(it)[i] } }
        for ((k, f) in fases.take(n).withIndex()) e = e.registrarVitoria(f, 1 + k % 3).first
        for (item in ItensDaCasa.ordem.take(colocados)) e = e.colocar(item)
        return e
    }

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

    private fun conferirArte(ids: List<String>) {
        val contexto = RuntimeEnvironment.getApplication()
        for (id in ids) {
            val info = ArteDoApp.info(contexto, id)
            assertNotNull("'$id' não está no manifesto", info)
            assertNotNull("'$id' não decodificou", ArteDoApp.bitmap(contexto, info!!, 2))
        }
    }

    private fun existe(descricao: String) =
        regra.onAllNodes(hasContentDescription(descricao), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

    private fun limites(descricao: String) =
        regra.onAllNodes(hasContentDescription(descricao), useUnmergedTree = true).fetchSemanticsNodes().map { it.boundsInRoot }

    /** Nenhum texto passa da borda do seu celular, nenhum tem reticências e nenhum progresso aparece como "3/250". */
    private fun conferirTextos() {
        val comTexto = SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult)
        val nos = regra.onAllNodes(comTexto, useUnmergedTree = true).fetchSemanticsNodes()
        val largura = with(regra.density) { LARGURA.dp.toPx() }
        val fracao = Regex("""\d+\s*/\s*\d+""")
        for (no in nos) {
            val texto = no.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }.orEmpty()
            assertTrue("progresso como número na tela: '$texto'", !fracao.containsMatchIn(texto))
            assertFalse("reticências em '$texto'", texto.contains('…'))
            val r = no.boundsInRoot
            if (r.width <= 0f || dentroDeRolagemDeLado(no)) continue
            val painel = (r.left / largura).toInt()
            assertTrue("texto passa da borda do celular: '$texto' em $r", r.left >= painel * largura - 1f && r.right <= (painel + 1) * largura + 1f)
        }
    }

    private fun dentroDeRolagemDeLado(no: androidx.compose.ui.semantics.SemanticsNode): Boolean {
        var p = no.parent
        while (p != null) {
            if (p.config.getOrNull(SemanticsProperties.HorizontalScrollAxisRange) != null) return true
            p = p.parent
        }
        return false
    }

    /** Os nomes longos: no máximo duas linhas, sem sobrar texto escondido, e nenhum encosta no vizinho. */
    private fun conferirNomes(quantos: Int) {
        val caixas = (0 until quantos).map { i ->
            val no = regra.onAllNodes(hasTestTag("nome_$i"), useUnmergedTree = true).fetchSemanticsNodes().single()
            val resultado = mutableListOf<TextLayoutResult>()
            no.config[SemanticsActions.GetTextLayoutResult].action?.invoke(resultado)
            val texto = no.config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }.orEmpty()
            assertTrue("'$texto' sem medida do texto", resultado.isNotEmpty())
            assertTrue("'$texto' em ${resultado[0].lineCount} linhas", resultado[0].lineCount <= 2)
            assertFalse("'$texto' não coube em duas linhas", resultado[0].didOverflowHeight || resultado[0].didOverflowWidth)
            no.boundsInRoot
        }
        for (i in caixas.indices) for (j in i + 1 until caixas.size) {
            assertFalse("os nomes $i e $j se encostam: ${caixas[i]} x ${caixas[j]}", caixas[i].overlaps(caixas[j]))
        }
    }

    @Test fun polimento() {
        conferirArte(
            listOf("estrela_vazia", "moeda", PecasDoPremio.PRESENTE_FECHADO, "tela_diploma_rolo", PecasDoPremio.idDoPremio("cama")) +
                PecasDoPremio.PILHAS.map(PecasDoPremio::idDasMoedas),
        )
        val inicio = estado(20, 24)
        val longos = listOf(
            "pijama", "dinossauro", "realeza", "futebol",
        ).map { id -> Loja.trajes.first { it.id == id }.let { it.nome to ana.copy(traje = it.id) } } +
            listOf("oculos", "pantufas", "presilha", "tiara").map { id ->
                Loja.acessorios.first { it.id == id }.let { it.nome to leo.copy(acessorios = mapOf(it.parte to it.id)) }
            }
        montar(
            listOf(
                { TelaInicio(inicio, true, {}, {}, {}, {}, missaoDisponivel = true, onEstante = {}) },
                { TelaPremio(gerador.fases(Trilha.INGLES)[2], 3, ItensDaCasa.porId.getValue("cama"), ana, 5, true, {}, {}, {}) },
                { MoedasEBotoes() },
                { NomesLongos(longos) },
                { TelaQuase(gerador.fases(Trilha.LOGICA)[3], 3, leo, {}, {}) },
            ),
        )
        regra.onAllNodes(hasContentDescription("Presente. Toque para abrir.")).onFirst().performClick()
        regra.mainClock.advanceTimeBy(1600)

        // 1. Início: o espelho não cobre o sol; a criança em primeiro plano (o dobro: mais de 22% da largura).
        val espelho = limites("Espelho: trocar a aparência")
        val sol = limites("Sol")
        assertTrue("o espelho devia estar na tela", espelho.isNotEmpty())
        assertTrue("o sol devia estar na casinha", sol.isNotEmpty())
        for (e in espelho) for (s in sol) assertFalse("o espelho cobre o sol: $e x $s", e.overlaps(s))
        val larguraDoPainel = with(regra.density) { LARGURA.dp.toPx() }
        val crianca = limites("Ana").filter { it.left < larguraDoPainel }
        assertTrue("a criança devia estar no início", crianca.isNotEmpty())
        assertTrue("a criança do início está pequena: ${crianca.first()}", crianca.first().width >= larguraDoPainel * 0.22f)
        assertTrue(existe("Missão do dia, vale moedinhas"))
        // 2. Prêmio: as moedas com o número e o nome em inglês numa pílula.
        assertTrue(existe("Mais 5 moedinhas"))
        assertTrue(regra.onAllNodes(hasText("Em inglês: bed")).fetchSemanticsNodes().isNotEmpty())
        // 3. Moedas que se contam e botões desligados legíveis (3:1 até no alto da face).
        for (n in listOf(1, 2, 3, 5, 6)) assertTrue("faltam as $n moedas", existe("Mais $n moedinhas"))
        for (cor in CORES_DOS_BOTOES) {
            val face = faceDesligada(cor)
            assertTrue("botão desligado $cor sem contraste", contraste(lerp(face, Color.White, 0.22f), Color.White) >= 3f)
        }
        // 4. Nomes longos em duas linhas.
        conferirNomes(longos.size)
        // 5. Quase: o anel do toque dentro do botão.
        anelDentroDoBotao(regra)
        conferirTextos()
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_19_polimento.png")
    }

    /** As moedas do prêmio, da missão e do relembrar, e os botões desligados de várias cores. */
    @Composable
    private fun MoedasEBotoes() {
        Column(
            Modifier.fillMaxSize().background(FUNDO).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextoComContorno("Moedas que se contam", estilo = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PilulaDeMoedas(1)
                PilulaDeMoedas(2)
                PilulaDeMoedas(3)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PilulaDeMoedas(5)
                PilulaDeMoedas(6)
            }
            TextoComContorno("Desligados", estilo = MaterialTheme.typography.titleLarge)
            for ((rotulo, cor) in listOf("Depois" to Cores.Roxo, "Pronto" to Color(0xFF34C46A), "Comprar" to Color(0xFFFFA020))) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BotaoFofo({}, Modifier.weight(1f), cor = cor, descricao = "$rotulo ligado") {
                        Text(rotulo, style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.padding(vertical = 12.dp))
                    }
                    BotaoFofo({}, Modifier.weight(1f), cor = cor, habilitado = false, descricao = "$rotulo desligado") {
                        Text(rotulo, style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
        }
    }

    /** As miniaturas do guarda-roupa e da loja com os nomes mais longos, quatro por linha em 360 dp. */
    @Composable
    private fun NomesLongos(longos: List<Pair<String, Perfil>>) {
        Column(
            Modifier.fillMaxSize().background(FUNDO).padding(horizontal = 4.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TextoComContorno("Nomes longos", estilo = MaterialTheme.typography.titleLarge)
            for ((linha, itens) in longos.withIndex().chunked(4).withIndex()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    for ((i, par) in itens) {
                        val (nome, perfil) = par
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Crianca(perfil, Modifier.height(if (linha == 0) 130.dp else 100.dp), humor = Humor.FELIZ, animado = false)
                            Text(
                                nome,
                                Modifier.padding(horizontal = 2.dp).testTag("nome_$i"),
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                color = Color(0xFF3A2766),
                                maxLines = 2,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val LARGURA = 360
        val FUNDO = Brush.verticalGradient(listOf(Color(0xFF7FC8FF), Color(0xFFBFE3FF), Color(0xFFFFD6C4), Color(0xFFFFC2DC)))
        val CORES_DOS_BOTOES = listOf(Cores.Roxo, Color(0xFF34C46A), Color(0xFFFFA020), Color(0xFFFF5A4E), Color(0xFF3E8EF7), Color(0xFFFFD040))
    }
}
