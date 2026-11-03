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
                Column(Modifier.weight(1f)) {
                    Text("📒 Álbum", style = MaterialTheme.typography.headlineMedium, color = Color.White, maxLines = 1)
                    Text(
                        "${ganhas.size} de ${Conquistas.totalDeFigurinhas} figurinhas",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
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
                            Text(t.emoji, fontSize = 22.sp)
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
                        Text("${Mundos.todos[p.mundo].emoji} ${p.titulo}", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center, maxLines = 2)
                        Text(
                            if (p.completa) "Página completa! ✨" else "${p.quantasGanhas} de ${p.figurinhas.size} figurinhas",
                            style = MaterialTheme.typography.titleSmall,
                            color = Cores.TextoSuave,
                        )
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
                                        dica = if (f.dourada) "Figurinha dourada do chefão! 👑" else "Figurinha da fase ${f.fase + 1}!"
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
                    dica ?: "Toque numa figurinha que falta para saber como ganhar.",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    color = if (dica != null) Cores.Texto else Cores.TextoSuave,
                    modifier = Modifier.padding(top = 12.dp).heightIn(min = 48.dp).testTag("dica_do_album"),
                )
            }
        }
    }
}

/** Figurinha colada (colorida, com a borda do caminho ou dourada) ou o lugar dela, com a silhueta. */
@Composable
private fun CartaoDeFigurinha(f: Figurinha, ganha: Boolean, corDoCaminho: Color, onClick: () -> Unit) {
    val borda = if (f.dourada) Cores.Estrela else corDoCaminho
    Box(
        Modifier
            .size(width = 72.dp, height = 88.dp)
            .background(
                if (!ganha) Brush.verticalGradient(listOf(Color(0xFFEDE7DA), Color(0xFFE2DACB)))
                else if (f.dourada) Brush.verticalGradient(listOf(Color(0xFFFFF3B0), Color(0xFFFFC93C)))
                else Brush.verticalGradient(listOf(Color.White, Color(0xFFF4F0FF))),
                RoundedCornerShape(12.dp),
            )
            .border(if (ganha) 3.dp else 2.dp, if (ganha) borda else Color(0x33000000), RoundedCornerShape(12.dp))
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
        Text(
            f.emoji,
            fontSize = 36.sp,
            // A silhueta: o próprio desenho pintado de uma cor só (dá para adivinhar o que vem).
            modifier = if (ganha) Modifier else Modifier
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    drawRect(Color(0x66534A6B), blendMode = BlendMode.SrcIn)
                },
        )
        Text(
            if (f.dourada) "👑" else "${f.fase + 1}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (ganha) Cores.Texto else Cores.TextoSuave,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp),
        )
    }
}
