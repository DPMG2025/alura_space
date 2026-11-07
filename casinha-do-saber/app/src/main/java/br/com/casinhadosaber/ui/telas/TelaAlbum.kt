package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Conquistas
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Figurinha
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.EscalaDeTablet
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalContext
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.ContasDeProgresso
import br.com.casinhadosaber.ui.componentes.FiguraLeveDoEmoji
import br.com.casinhadosaber.ui.componentes.ID_ESTRELA_CHEIA
import br.com.casinhadosaber.ui.componentes.CorDaConta
import br.com.casinhadosaber.ui.componentes.IconeDaBarra
import br.com.casinhadosaber.ui.componentes.IconeDeArteDaBarra
import br.com.casinhadosaber.ui.componentes.IconeDeTrilha
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.PecasDasTelas
import br.com.casinhadosaber.ui.componentes.PilulaDeValor
import br.com.casinhadosaber.ui.componentes.TituloDeBrinquedo
import br.com.casinhadosaber.ui.tema.Cores

/**
 * Álbum de figurinhas: uma página por mundo em cada caminho (11 figurinhas: uma por fase com 3 estrelas e a
 * dourada do chefão). As ganhas aparecem coloridas; as que faltam são silhuetas, e tocar nelas diz como ganhar.
 * Tudo vem de [EstadoJogo.estrelas]: nada é sorteado e nada se perde.
 */
@Composable
fun TelaAlbum(
    estado: EstadoJogo,
    trilhaInicial: Trilha,
    /** Página para abrir (índice do mundo); fora da faixa abre no mundo da vez. */
    mundoInicial: Int = -1,
    onVoltar: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var trilha by remember { mutableStateOf(trilhaInicial) }
    fun paginaDaVez(t: Trilha): Int = ((faseDaVez(estado, t) ?: (Mundos.todos.size * Mundos.FASES_POR_MUNDO - 1)) / Mundos.FASES_POR_MUNDO)
    var pagina by remember { mutableIntStateOf(if (mundoInicial in Mundos.todos.indices) mundoInicial else paginaDaVez(trilhaInicial)) }
    var dica by remember { mutableStateOf<String?>(null) }
    val ganhas = remember(estado.estrelas) { Conquistas.figurinhasGanhas(estado.estrelas) }
    val paginas = remember(trilha, estado.estrelas) { Conquistas.album(trilha, estado.estrelas) }
    val p = paginas[pagina.coerceIn(paginas.indices)]

    LaunchedEffect(Unit) {
        narrador.falar("Álbum de figurinhas! Ganhe três estrelas numa fase para ganhar a figurinha dela.")
    }

    fun irPara(nova: Int) {
        if (nova !in paginas.indices) return
        sons.toque()
        pagina = nova
        dica = null
        val m = Mundos.todos[nova]
        narrador.falar("Mundo ${m.numero}: ${m.nome}.")
    }

    EscalaDeTablet {
        Column(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(trilha.corArgb), Cores.Fundo)))
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                BotaoRedondo("⬅️", "Voltar", onVoltar, tamanho = 52.dp)
                Spacer(Modifier.width(10.dp))
                IconeDeArteDaBarra(IconeDaBarra(IconesDaBarra.ALBUM), Modifier.size(40.dp)) { Text("📒", fontSize = 28.sp) }
                TituloDeBrinquedo("Álbum", Modifier.weight(1f).padding(horizontal = 4.dp), estilo = MaterialTheme.typography.headlineMedium, maxLinhas = 1)
                // Quantas figurinhas já tem: a figurinha dourada e o número, numa pílula (o total fica na descrição).
                PilulaDeValor(
                    PecasDasTelas.FIGURINHA_DOURADA,
                    "${ganhas.size}",
                    descricao = "${ganhas.size} de ${Conquistas.totalDeFigurinhas} figurinhas",
                )
            }
            // Um separador por caminho
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (t in Trilha.entries) {
                    val escolhido = t == trilha
                    BotaoFofo(
                        onClick = {
                            sons.toque()
                            trilha = t
                            pagina = paginaDaVez(t)
                            dica = null
                            narrador.falar("Figurinhas do caminho ${t.titulo}.")
                        },
                        cor = if (escolhido) Color.White else Color(t.corArgb),
                        corBase = Color(t.corEscuraArgb),
                        forma = RoundedCornerShape(50),
                        descricao = "Caminho ${t.titulo}" + if (escolhido) ", aberto" else "",
                        profundidade = 4.dp,
                    ) {
                        Row(Modifier.height(48.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconeDeTrilha(t, Modifier.size(30.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(t.titulo, style = MaterialTheme.typography.titleMedium, color = if (escolhido) Cores.Texto else Color.White, maxLines = 1)
                        }
                    }
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // A página: título do mundo com setas para passar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotaoRedondo("◀️", "Página anterior", { irPara(pagina - 1) }, cor = if (pagina > 0) Color.White else Color(0xFFEDEDED), tamanho = 52.dp)
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FiguraLeveDoEmoji(Mundos.todos[p.mundo].emoji, 32.dp)
                            Spacer(Modifier.width(4.dp))
                            Text(p.titulo, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, maxLines = 2)
                        }
                        if (p.completa) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconeLeve(ID_ESTRELA_CHEIA, 18.dp) { }
                                Spacer(Modifier.width(4.dp))
                                Text("Página completa!", style = MaterialTheme.typography.titleSmall, color = Cores.TextoSuave)
                            }
                        } else {
                            // As figurinhas da página como contas: as coladas na cor do caminho (nada de "3 de 11").
                            ContasDeProgresso(
                                p.quantasGanhas,
                                p.figurinhas.size,
                                Modifier.padding(top = 4.dp),
                                cor = corDasContas(trilha),
                                tamanho = 18.dp,
                                comAtual = false,
                                descricao = "${p.quantasGanhas} de ${p.figurinhas.size} figurinhas",
                            )
                        }
                    }
                    BotaoRedondo("▶️", "Próxima página", { irPara(pagina + 1) }, cor = if (pagina < paginas.lastIndex) Color.White else Color(0xFFEDEDED), tamanho = 52.dp)
                }
                Spacer(Modifier.height(10.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFBF0), RoundedCornerShape(20.dp))
                        .border(3.dp, Color(trilha.corArgb), RoundedCornerShape(20.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    for (linha in p.figurinhas.chunked(4)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (f in linha) {
                                CartaoDeFigurinha(f, p.ganhou(f), Color(trilha.corArgb)) {
                                    if (p.ganhou(f)) {
                                        sons.colocar()
                                        // Na tela, sem o número da fase (a descrição da figurinha ainda diz qual é).
                                        dica = if (f.dourada) "Figurinha dourada do chefão!" else "Figurinha colada! Você ganhou três estrelas."
                                        narrador.falar(dica ?: "")
                                    } else {
                                        sons.toque()
                                        dica = f.comoGanhar
                                        narrador.falar(f.comoGanhar)
                                    }
                                }
                            }
                        }
                    }
                }
                Text(
                    dica ?: if (p.completa) "Você ganhou todas as figurinhas deste mundo!" else "Toque numa figurinha que falta para saber como ganhar.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = if (dica != null) Cores.Texto else Cores.TextoSuave,
                    modifier = Modifier.padding(top = 12.dp).heightIn(min = 48.dp).testTag("dica_do_album"),
                )
            }
        }
    }
}

/** A cor das contas de cada caminho (perto da cor do caminho). */
private fun corDasContas(t: Trilha): CorDaConta = when (t) {
    Trilha.MATEMATICA -> CorDaConta.LARANJA
    Trilha.PORTUGUES -> CorDaConta.VERDE
    Trilha.INGLES -> CorDaConta.ROXA
    Trilha.LOGICA -> CorDaConta.VERMELHA
}

/**
 * Figurinha colada (o quadrinho 3D com a borda do caminho, ou o dourado do chefão) ou o lugar dela (o quadrinho
 * pontilhado), com o desenho no meio da janelinha (âncora `centro` da arte): colorido se ganhou, silhueta se falta.
 * Sem a arte, o cartão desenhado de antes.
 */
@Composable
private fun CartaoDeFigurinha(f: Figurinha, ganha: Boolean, corDoCaminho: Color, onClick: () -> Unit) {
    val borda = if (f.dourada) Cores.Estrela else corDoCaminho
    // Figurinha colada fica um tiquinho torta (sempre igual para a mesma fase), como no papel.
    val torta = if (ganha && !f.dourada) ((f.fase * 37) % 7 - 3) * 0.9f else 0f
    val id = PecasDasTelas.figurinha(f.trilha, f.dourada, ganha)
    val contexto = LocalContext.current
    val info = remember(id) { ArteDoApp.info(contexto, id) }
    // O meio da janelinha, em relação ao meio do quadro (a arte tem a mesma proporção do quadro).
    val (cx, cy) = info?.ancoras?.get("centro")?.let(info::fracao) ?: (0.5f to 0.5f)
    Box(
        Modifier
            .size(width = 72.dp, height = 86.dp)
            .graphicsLayer { rotationZ = torta }
            .testTag(if (ganha) "figurinha_ganha" else "figurinha_falta")
            .semantics {
                contentDescription = if (ganha) {
                    if (f.dourada) "Figurinha dourada do chefão" else "Figurinha da fase ${f.fase + 1}"
                } else {
                    "Figurinha que falta. ${f.comoGanhar}"
                }
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        ImagemDeArte(id, Modifier.matchParentSize()) {
            Box(
                Modifier
                    .shadow(if (ganha) 4.dp else 0.dp, RoundedCornerShape(12.dp))
                    .background(
                        if (!ganha) Brush.verticalGradient(listOf(Color(0xFFEDE7DA), Color(0xFFE2DACB)))
                        else if (f.dourada) Brush.verticalGradient(listOf(Color(0xFFFFF3B0), Color(0xFFFFC93C)))
                        else Brush.verticalGradient(listOf(Color.White, Color(0xFFF4F0FF))),
                        RoundedCornerShape(12.dp),
                    )
                    .border(if (ganha) 3.dp else 2.dp, if (ganha) borda else Color(0x33000000), RoundedCornerShape(12.dp)),
            )
        }
        // O desenho da figurinha: a figura 3D (sem modelo, o emoji). Falta ganhar: a silhueta numa cor só (dá para
        // adivinhar o que vem). Sem o número da fase na figurinha: ele fica na descrição, para o adulto.
        val silhueta = Modifier.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
            drawContent()
            drawRect(Color(0x66534A6B), blendMode = BlendMode.SrcIn)
        }
        FiguraLeveDoEmoji(
            f.emoji,
            40.dp,
            Modifier.offset(x = 72.dp * (cx - 0.5f), y = 86.dp * (cy - 0.5f)),
            silhueta = if (ganha) null else Color(0x66534A6B),
        ) {
            Text(f.emoji, fontSize = 32.sp, modifier = if (ganha) Modifier else silhueta)
        }
    }
}
