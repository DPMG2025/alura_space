package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Conquistas
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Mundo
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.EscalaDeTablet
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

/** Altura de cada mundo no mapa (ilha redonda, nome e a barrinha das 10 fases). */
private val ALTURA_DO_MUNDO = 196.dp
private val TAMANHO_DA_ILHA = 104.dp
private val zigueZagueDoMapa = listOf(0.28f, 0.72f)

/** Como está um mundo do caminho para a criança. */
private class SituacaoDoMundo(val mundo: Mundo, val vencidas: Int, val completo: Boolean, val atual: Boolean, val alcancado: Boolean, val chefaoVencido: Boolean)

/** A fase da vez no caminho: a primeira aberta e ainda não vencida (null com tudo vencido). */
fun faseDaVez(estado: EstadoJogo, trilha: Trilha): Int? {
    val total = Mundos.todos.size * Mundos.FASES_POR_MUNDO
    return (0 until total).firstOrNull { n ->
        EstadoJogo.chave(trilha, n) !in estado.estrelas && (n == 0 || EstadoJogo.chave(trilha, n - 1) in estado.estrelas)
    }
}

/**
 * Mapa grande do caminho: os 25 mundos de uma vez, em zigue-zague. Os completos brilham com o diploma, o da vez
 * tem o alfinete "você está aqui", e cada um mostra o chefão (👑, dourado quando vencido) e quantas fases já foram.
 */
@Composable
fun TelaMapa(
    trilha: Trilha,
    estado: EstadoJogo,
    onVoltar: () -> Unit,
    /** Abre o álbum de figurinhas na página deste mundo. */
    onAlbum: (Int) -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val daVez = faseDaVez(estado, trilha)
    val mundoDaVez = daVez?.let { it / Mundos.FASES_POR_MUNDO }
    val situacoes = remember(estado.estrelas, trilha) {
        Mundos.todos.map { m ->
            val vencidas = (m.primeiraFase..m.ultimaFase).count { EstadoJogo.chave(trilha, it) in estado.estrelas }
            SituacaoDoMundo(
                mundo = m,
                vencidas = vencidas,
                completo = Conquistas.mundoCompleto(estado.estrelas, trilha, m.indice),
                atual = m.indice == mundoDaVez,
                alcancado = mundoDaVez == null || m.indice <= mundoDaVez,
                chefaoVencido = EstadoJogo.chave(trilha, m.ultimaFase) in estado.estrelas,
            )
        }
    }
    val completos = situacoes.count { it.completo }
    val cor = Color(trilha.corArgb)
    val rolagem = rememberScrollState()
    val densidade = LocalDensity.current

    LaunchedEffect(trilha) {
        val m = mundoDaVez?.let { Mundos.todos[it] }
        narrador.falar(
            "Mapa do caminho ${trilha.titulo}! " +
                (if (m != null) "Você está no mundo ${m.numero}: ${m.nome}. " else "Você completou todos os mundos! ") +
                if (completos > 0) "Os mundos que brilham já estão completos." else "Complete um mundo para ele brilhar.",
        )
        val alvo = with(densidade) { (ALTURA_DO_MUNDO * (mundoDaVez ?: 24) - 140.dp).roundToPx() }
        if (alvo > 0) rolagem.scrollTo(alvo)
    }

    EscalaDeTablet {
        Column(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(cor, Color(0xFFBFE3A8))))
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                BotaoRedondo("⬅️", "Voltar para o caminho", onVoltar, tamanho = 52.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("🗺️ Mapa: ${trilha.titulo}", style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 1)
                    Text("🏅 $completos de ${Mundos.todos.size} mundos completos", style = MaterialTheme.typography.titleSmall, color = Color.White, maxLines = 1)
                }
                BotaoRedondo("📒", "Álbum de figurinhas", { sons.toque(); onAlbum(mundoDaVez ?: 0) }, tamanho = 52.dp)
            }
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val largura = maxWidth
                Box(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rolagem)
                        .height(ALTURA_DO_MUNDO * situacoes.size + 24.dp),
                ) {
                    // A trilha pontilhada entre as ilhas (dourada até o mundo da vez).
                    Canvas(Modifier.fillMaxSize()) {
                        val pontos = situacoes.indices.map { i ->
                            Offset(largura.toPx() * zigueZagueDoMapa[i % 2], (ALTURA_DO_MUNDO * i + TAMANHO_DA_ILHA / 2 + 12.dp).toPx())
                        }
                        for (i in 0 until pontos.size - 1) {
                            val a = pontos[i]
                            val b = pontos[i + 1]
                            val caminho = Path().apply {
                                moveTo(a.x, a.y)
                                cubicTo(a.x, (a.y + b.y) / 2, b.x, (a.y + b.y) / 2, b.x, b.y)
                            }
                            val feito = situacoes[i + 1].alcancado
                            drawPath(caminho, Color(0x66FFFFFF), style = Stroke(16.dp.toPx(), cap = StrokeCap.Round))
                            drawPath(
                                caminho,
                                if (feito) Cores.Estrela else Color(0x99FFFFFF),
                                style = Stroke(6.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14.dp.toPx(), 10.dp.toPx()))),
                            )
                        }
                    }
                    for ((i, s) in situacoes.withIndex()) {
                        val x = largura * zigueZagueDoMapa[i % 2]
                        IlhaDoMundo(
                            s = s,
                            trilha = trilha,
                            modifier = Modifier.offset(x = x - LARGURA_DA_ILHA / 2, y = ALTURA_DO_MUNDO * i + 12.dp),
                            onClick = {
                                sons.toque()
                                val m = s.mundo
                                narrador.falar(
                                    "Mundo ${m.numero}: ${m.nome}. " + when {
                                        s.completo -> "Completo! Você ganhou o diploma."
                                        s.atual -> "Você está aqui! Já venceu ${s.vencidas} de ${Mundos.FASES_POR_MUNDO} fases."
                                        s.alcancado -> "Já venceu ${s.vencidas} de ${Mundos.FASES_POR_MUNDO} fases."
                                        else -> "Ainda vamos chegar lá!"
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

private val LARGURA_DA_ILHA: Dp = 168.dp

@Composable
private fun IlhaDoMundo(s: SituacaoDoMundo, trilha: Trilha, modifier: Modifier, onClick: () -> Unit) {
    val m = s.mundo
    Column(
        modifier
            .width(LARGURA_DA_ILHA)
            .testTag("mundo_${m.indice}")
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Mundo ${m.numero}, ${m.nome}" + when {
                    s.completo -> ", completo"
                    s.atual -> ", você está aqui"
                    else -> ""
                }
            }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(TAMANHO_DA_ILHA + 24.dp), contentAlignment = Alignment.Center) {
            if (s.completo) {
                // Brilho dos mundos completos
                Box(
                    Modifier
                        .size(TAMANHO_DA_ILHA + 24.dp)
                        .background(Brush.radialGradient(listOf(Color(0xCCFFE680), Color(0x00FFE680))), CircleShape),
                )
            }
            Box(
                Modifier
                    .size(TAMANHO_DA_ILHA)
                    .then(if (s.atual) Modifier.pulsando() else Modifier)
                    // Mundo ainda longe: a ilha fica clarinha (opaca, para a trilha não aparecer através dela).
                    .background(if (s.alcancado) Color(m.corArgb) else lerp(Color(m.corArgb), Color(0xFFF2F2F2), 0.6f), CircleShape)
                    .border(
                        if (s.completo || s.atual) 5.dp else 3.dp,
                        when {
                            s.completo -> Cores.Estrela
                            s.atual -> Color.White
                            else -> Color(trilha.corEscuraArgb).copy(alpha = 0.5f)
                        },
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(m.emoji, fontSize = 46.sp, modifier = Modifier.alpha(if (s.alcancado) 1f else 0.5f))
            }
            // Chefão do mundo (a 10ª fase): coroa dourada quando vencido
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .size(36.dp)
                    .background(if (s.chefaoVencido) Cores.Estrela else Color.White, CircleShape)
                    .border(2.dp, Color(0x33000000), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("👑", fontSize = 18.sp, modifier = Modifier.alpha(if (s.chefaoVencido) 1f else 0.45f))
            }
            if (s.completo) {
                Text("🏅", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 6.dp, y = (-2).dp))
            } else if (!s.alcancado) {
                Text("🔒", fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 8.dp, y = (-4).dp))
            }
            if (s.atual) {
                Text("📍", fontSize = 34.sp, modifier = Modifier.align(Alignment.TopStart).offset(x = 4.dp).pulsando())
            }
        }
        Text(
            "${m.numero}. ${m.nome}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 2.dp),
        )
        // As 10 fases do mundo em bolinhas
        Row(Modifier.padding(top = 4.dp)) {
            for (k in 0 until Mundos.FASES_POR_MUNDO) {
                Box(
                    Modifier
                        .padding(1.5.dp)
                        .size(9.dp)
                        .background(if (k < s.vencidas) Cores.Estrela else Color(0x55FFFFFF), CircleShape),
                )
            }
        }
    }
}
