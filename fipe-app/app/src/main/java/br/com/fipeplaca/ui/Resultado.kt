@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package br.com.fipeplaca.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.fipeplaca.data.Dinheiro
import br.com.fipeplaca.data.Estatisticas
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.Meses
import br.com.fipeplaca.data.PeriodoHistorico
import br.com.fipeplaca.data.PontoHistorico
import br.com.fipeplaca.data.Projecao
import br.com.fipeplaca.data.Texto
import kotlin.math.roundToInt

/** Cartao principal com o valor FIPE e as acoes (favoritar, compartilhar, copiar codigo). */
@Composable
fun CartaoValorFipe(
    r: FipeResultado,
    favorito: Boolean,
    onFavoritar: () -> Unit,
    onCompartilhar: () -> Unit,
    onCopiarCodigo: () -> Unit,
) {
    val c = Extras.cores
    Surface(shape = MaterialTheme.shapes.large, color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .background(Brush.linearGradient(listOf(c.gradienteInicio, c.gradienteFim)))
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("VALOR FIPE", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
                    if (r.mesReferencia.isNotBlank()) {
                        Text("Referência ${r.mesReferencia}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                    }
                }
                IconButton(onClick = onFavoritar) {
                    Icon(
                        if (favorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (favorito) "Remover dos favoritos" else "Salvar nos favoritos",
                        tint = if (favorito) Color(0xFFFF6B81) else Color.White,
                    )
                }
                IconButton(onClick = onCompartilhar) {
                    Icon(Icons.Filled.Share, contentDescription = "Compartilhar", tint = Color.White)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                r.valor ?: "Indisponível",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                modifier = Modifier.semantics { contentDescription = "Valor FIPE ${r.valor ?: "indisponível"}" },
            )
            Spacer(Modifier.height(10.dp))
            Text("${Texto.marca(r.marca)} ${r.modelo}".trim(), style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (r.anoModelo.isNotBlank()) Etiqueta(r.anoModelo)
                if (r.combustivel.isNotBlank()) Etiqueta(r.combustivel)
                if (r.codigoFipe.isNotBlank()) Etiqueta("FIPE ${r.codigoFipe}", onClick = onCopiarCodigo)
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String, onClick: (() -> Unit)? = null) {
    Text(
        texto,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.16f))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
fun CartaoHistorico(estado: HistoricoUi, onPeriodo: (PeriodoHistorico) -> Unit, onTentar: () -> Unit) {
    Secao(titulo = "Evolução do preço") {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            PeriodoHistorico.entries.forEachIndexed { i, p ->
                SegmentedButton(
                    selected = estado.periodo == p,
                    onClick = { onPeriodo(p) },
                    shape = SegmentedButtonDefaults.itemShape(i, PeriodoHistorico.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        activeBorderColor = MaterialTheme.colorScheme.outline,
                        inactiveBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                    icon = {},
                ) { Text(p.rotulo, maxLines = 1) }
            }
        }
        Spacer(Modifier.height(16.dp))
        AnimatedContent(
            targetState = when {
                estado.carregando -> 0
                estado.erro != null -> 1
                estado.pontos.isNotEmpty() -> 2
                else -> 3
            },
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "historico",
        ) { fase ->
            when (fase) {
                0 -> Column {
                    Text(
                        "Consultando ${(estado.progresso * 100).roundToInt()}% dos meses de referência…",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    val prog by animateFloatAsState(estado.progresso, label = "progresso")
                    LinearProgressIndicator(progress = { prog }, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)))
                    Spacer(Modifier.height(16.dp))
                    Esqueleto(Modifier.fillMaxWidth().height(170.dp))
                }
                1 -> CartaoErro(estado.erro.orEmpty(), "Tentar novamente", onTentar)
                2 -> ConteudoHistorico(estado.pontos)
                else -> Spacer(Modifier.height(1.dp))
            }
        }
    }
}

@Composable
private fun ConteudoHistorico(pontos: List<PontoHistorico>) {
    val e = Estatisticas.de(pontos) ?: return
    Column {
        Row(Modifier.fillMaxWidth()) {
            Indicador("Variação", Estatisticas.percentual(e.variacaoPercentual, 2), corVariacao(e.variacaoPercentual), Modifier.weight(1f))
            Indicador("Diferença", (if (e.diferenca > 0) "+" else "") + Dinheiro.formatar(e.diferenca), corVariacao(e.diferenca), Modifier.weight(1.3f))
        }
        Spacer(Modifier.height(16.dp))
        Grafico(pontos)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniIndicador("Máximo", Dinheiro.formatar(e.maximo.valor), Meses.curto(e.maximo.mesReferencia), Modifier.weight(1f))
            MiniIndicador("Mínimo", Dinheiro.formatar(e.minimo.valor), Meses.curto(e.minimo.mesReferencia), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MiniIndicador("Média", Dinheiro.formatar(e.media), "${pontos.size} meses", Modifier.weight(1f))
            MiniIndicador("Por mês", Estatisticas.percentual(e.variacaoMensalMedia, 2), "variação média", Modifier.weight(1f))
        }
        Projecao.estimar(pontos)?.let { estimado ->
            Spacer(Modifier.height(8.dp))
            val delta = if (pontos.last().valor > 0) (estimado - pontos.last().valor) / pontos.last().valor * 100 else 0.0
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Projeção para 12 meses", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("Estimativa pela tendência do período, não é garantia de preço.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(Dinheiro.formatar(estimado), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                    Text(Estatisticas.percentual(delta), style = MaterialTheme.typography.labelMedium, color = corVariacao(delta))
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Mês a mês", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(4.dp))
        pontos.indices.reversed().forEach { i ->
            val p = pontos[i]
            val anterior = pontos.getOrNull(i - 1)
            val v = anterior?.let { if (it.valor > 0) (p.valor - it.valor) / it.valor * 100 else null }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(Meses.capitalizar(p.mesReferencia), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Text(Dinheiro.formatar(p.valor), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    v?.let { Estatisticas.percentual(it) } ?: "",
                    modifier = Modifier.width(68.dp), textAlign = TextAlign.End,
                    style = MaterialTheme.typography.labelMedium, color = v?.let { corVariacao(it) } ?: Color.Unspecified,
                )
            }
        }
    }
}

@Composable
private fun Indicador(rotulo: String, valor: String, cor: Color, modifier: Modifier) {
    Column(modifier) {
        Text(rotulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.headlineSmall, color = cor, maxLines = 1)
    }
}

@Composable
private fun MiniIndicador(rotulo: String, valor: String, apoio: String, modifier: Modifier) {
    Column(
        modifier.clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp),
    ) {
        Text(rotulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(apoio, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun corVariacao(v: Double): Color = when {
    v > 0.005 -> Extras.cores.alta
    v < -0.005 -> Extras.cores.baixa
    else -> MaterialTheme.colorScheme.onSurface
}

/**
 * Grafico de linha desenhado com Canvas. Tocar ou arrastar mostra o valor do mes;
 * por padrao destaca o mes mais recente.
 */
@Composable
fun Grafico(pontos: List<PontoHistorico>) {
    val cor = MaterialTheme.colorScheme.primary
    val grade = MaterialTheme.colorScheme.outlineVariant
    val fundo = MaterialTheme.colorScheme.surface
    val haptico = LocalHapticFeedback.current
    var selecionado by remember(pontos) { mutableIntStateOf(pontos.lastIndex) }
    val max = pontos.maxOf { it.valor }
    val min = pontos.minOf { it.valor }
    val folga = ((max - min) * 0.12).takeIf { it > 0 } ?: (max * 0.02).coerceAtLeast(1.0)
    val topo = max + folga
    val base = min - folga
    val animacao = remember(pontos) { Animatable(0f) }
    LaunchedEffect(pontos) { animacao.animateTo(1f, tween(700)) }

    fun indicePorX(x: Float, largura: Float): Int =
        if (pontos.size < 2) 0 else (x / largura * (pontos.size - 1)).roundToInt().coerceIn(0, pontos.lastIndex)

    val sel = pontos.getOrNull(selecionado) ?: pontos.last()
    Column {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(Dinheiro.formatar(sel.valor), style = MaterialTheme.typography.titleLarge, color = cor)
            Spacer(Modifier.width(8.dp))
            Text(Meses.capitalizar(sel.mesReferencia), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 3.dp))
        }
        Spacer(Modifier.height(8.dp))
        Box {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .semantics { contentDescription = "Gráfico da evolução do preço em ${pontos.size} meses" }
                    .pointerInput(pontos) {
                        detectTapGestures { o ->
                            selecionado = indicePorX(o.x, size.width.toFloat())
                            haptico.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }
                    .pointerInput(pontos) {
                        detectHorizontalDragGestures { change, _ ->
                            val novo = indicePorX(change.position.x, size.width.toFloat())
                            if (novo != selecionado) {
                                selecionado = novo
                                haptico.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    },
            ) {
                val faixa = (topo - base).takeIf { it > 0 } ?: 1.0
                fun pos(i: Int): Offset {
                    val x = if (pontos.size > 1) i * size.width / (pontos.size - 1) else size.width / 2
                    val y = size.height - ((pontos[i].valor - base) / faixa * size.height).toFloat()
                    return Offset(x, y)
                }
                for (k in 0..4) {
                    val y = size.height * k / 4
                    drawLine(grade, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
                }
                val visiveis = (pontos.size * animacao.value).roundToInt().coerceIn(1, pontos.size)
                val linha = Path()
                val area = Path()
                for (i in 0 until visiveis) {
                    val p = pos(i)
                    if (i == 0) { linha.moveTo(p.x, p.y); area.moveTo(p.x, size.height); area.lineTo(p.x, p.y) }
                    else { linha.lineTo(p.x, p.y); area.lineTo(p.x, p.y) }
                }
                area.lineTo(pos(visiveis - 1).x, size.height)
                area.close()
                drawPath(area, Brush.verticalGradient(listOf(cor.copy(alpha = 0.28f), cor.copy(alpha = 0f))))
                drawPath(linha, cor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                val ps = pos(selecionado.coerceIn(0, pontos.lastIndex))
                drawLine(cor.copy(alpha = 0.5f), Offset(ps.x, 0f), Offset(ps.x, size.height), strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                drawCircle(cor, radius = 7.dp.toPx(), center = ps)
                drawCircle(fundo, radius = 3.5.dp.toPx(), center = ps)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val rotulos = listOf(0, pontos.lastIndex / 2, pontos.lastIndex).distinct()
            rotulos.forEach { i ->
                Text(Meses.curto(pontos[i].mesReferencia), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
