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
import br.com.casinhadosaber.ui.tema.Raios
import br.com.casinhadosaber.ui.componentes.EnfeiteDoFundo
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.componentes.FundoDecorado
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.translate

/** Altura de cada mundo no mapa (ilha redonda, nome e a barrinha das 10 fases). */
private val ALTURA_DO_MUNDO = 196.dp
private val TAMANHO_DA_ILHA = 104.dp
private val zigueZagueDoMapa = listOf(0.28f, 0.72f)

/** Como está um mundo do caminho para a criança. */
private class SituacaoDoMundo(val mundo: Mundo, val vencidas: Int, val completo: Boolean, val atual: Boolean, val alcancado: Boolean, val chefaoVencido: Boolean)

/**
 * A fase da vez no caminho: a primeira aberta e ainda não vencida (null com tudo vencido), começando do mundo
 * aberto pelo "Descobrir o nível" ([EstadoJogo.liberadaAte]), como `EstadoJogo.faseDaVez` no caminho.
 */
fun faseDaVez(estado: EstadoJogo, trilha: Trilha): Int? {
    val total = Mundos.todos.size * Mundos.FASES_POR_MUNDO
    val desde = estado.liberadaAte[trilha] ?: 0
    fun daVez(n: Int) = EstadoJogo.chave(trilha, n) !in estado.estrelas && estado.liberada(trilha, n)
    return (desde until total).firstOrNull(::daVez) ?: (0 until total).firstOrNull(::daVez)
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
    val situacoes = remember(estado.estrelas, estado.liberadaAte, trilha) {
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
        // A rolagem para no começo de um mundo (o de antes do da vez, inteiro no alto): nenhuma ilha fica
        // cortada embaixo do cabeçalho, e o mundo da vez aparece logo abaixo dele.
        val alvo = with(densidade) { (ALTURA_DO_MUNDO * ((mundoDaVez ?: 24) - 1).coerceAtLeast(0)).roundToPx() }
        if (alvo > 0) rolagem.scrollTo(alvo)
    }

    EscalaDeTablet {
        FundoDecorado(cor, Modifier.fillMaxSize(), enfeite = EnfeiteDoFundo.Estrelas) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                BotaoRedondo("⬅️", "Voltar para o caminho", onVoltar, tamanho = 52.dp)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("🗺️ Mapa: ${trilha.titulo}", style = MaterialTheme.typography.titleLarge.copy(shadow = SOMBRA_DO_TEXTO), color = Color.White, maxLines = 1)
                    Text("🏅 $completos de ${Mundos.todos.size} mundos completos", style = MaterialTheme.typography.titleSmall.copy(shadow = SOMBRA_DO_TEXTO), color = Color.White, maxLines = 1)
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
                            translate(top = 3.dp.toPx()) { drawPath(caminho, Cores.Sombra, style = Stroke(18.dp.toPx(), cap = StrokeCap.Round)) }
                            drawPath(caminho, Color(0x8CFFFFFF), style = Stroke(16.dp.toPx(), cap = StrokeCap.Round))
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
                // Esmaecido no alto: as ilhas que passam por baixo do cabeçalho somem macio, sem corte duro.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(Brush.verticalGradient(0f to cor.copy(alpha = 0.6f), 0.4f to cor.copy(alpha = 0.3f), 1f to cor.copy(alpha = 0f))),
                )
            }
        }
        }
    }
}

/** Sombra ameixa suave dos textos brancos do cabeçalho. */
private val SOMBRA_DO_TEXTO = Shadow(Color(0x553D2C5E), Offset(0f, 3f), 6f)

private val LARGURA_DA_ILHA: Dp = 168.dp

/** Selo do cadeado dos mundos ainda fechados (lilás clarinho, diferente da coroa). */
private val SELO_FECHADO = Color(0xFFE6E0F5)

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
                    // Sombra suave embaixo da ilha (descola do caminho).
                    .drawBehind { drawCircle(Cores.Sombra, size.minDimension / 2, center.copy(y = center.y + 5.dp.toPx())) }
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
            // Selo do canto: mundo ainda fechado mostra o cadeado (num selo lilás, sem coroa); aberto mostra o
            // chefão (a 10ª fase), coroa apagadinha num selo branco tracejado até vencer e dourada depois.
            val dourado = s.chefaoVencido
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .size(36.dp)
                    .drawBehind { drawCircle(Cores.Sombra, size.minDimension / 2, center.copy(y = center.y + 2.dp.toPx())) }
                    .background(
                        when {
                            !s.alcancado -> SELO_FECHADO
                            dourado -> Cores.Estrela
                            else -> Color.White
                        },
                        CircleShape,
                    )
                    .then(
                        when {
                            dourado -> Modifier.border(2.dp, Color.White, CircleShape)
                            s.alcancado -> Modifier.drawBehind {
                                val r = size.minDimension / 2 - 1.5.dp.toPx()
                                drawCircle(
                                    Cores.Estrela,
                                    r,
                                    style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
                                )
                            }
                            else -> Modifier.border(2.dp, Cores.Contorno, CircleShape)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    !s.alcancado -> Text("🔒", fontSize = 17.sp)
                    else -> Text("👑", fontSize = 18.sp, modifier = Modifier.alpha(if (dourado) 1f else 0.4f))
                }
            }
            if (s.completo) {
                Text("🏅", fontSize = 30.sp, modifier = Modifier.align(Alignment.BottomStart).offset(x = 6.dp, y = (-2).dp))
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
                .cartaoFofo(raio = Raios.Medio)
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
