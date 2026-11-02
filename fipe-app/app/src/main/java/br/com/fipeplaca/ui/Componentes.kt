@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package br.com.fipeplaca.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.fipeplaca.data.Dinheiro
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.PeriodoHistorico
import br.com.fipeplaca.data.PontoHistorico
import java.util.Locale

@Composable
fun Info(rotulo: String, valor: String?) {
    if (valor.isNullOrBlank()) return
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(rotulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp))
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CartaoErro(mensagem: String, acao: String? = null, onAcao: () -> Unit = {}) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(mensagem, color = MaterialTheme.colorScheme.onErrorContainer)
            if (acao != null) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onAcao) { Text(acao) }
            }
        }
    }
}

@Composable
fun CartaoValorFipe(r: FipeResultado, destaque: Boolean = true) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (destaque) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Valor de mercado (FIPE)", style = MaterialTheme.typography.labelLarge)
            Text(
                r.valor ?: "Valor indisponível",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            if (r.mesReferencia.isNotBlank()) {
                Text("Referência: ${r.mesReferencia}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Info("Marca", r.marca)
            Info("Modelo", r.modelo)
            Info("Ano modelo", r.anoModelo)
            Info("Combustível", r.combustivel)
            Info("Código FIPE", r.codigoFipe)
            r.score?.let { if (it in 1..99) Info("Compatibilidade", "$it%") }
        }
    }
}

@Composable
fun CartaoHistorico(estado: HistoricoUi, onPeriodo: (PeriodoHistorico) -> Unit, onTentar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Histórico do preço FIPE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodoHistorico.entries.forEach { p ->
                    FilterChip(
                        selected = estado.periodo == p,
                        onClick = { onPeriodo(p) },
                        label = { Text(p.rotulo) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            when {
                estado.carregando -> {
                    Text("Consultando os meses de referência…", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(progress = { estado.progresso }, modifier = Modifier.fillMaxWidth())
                }
                estado.erro != null -> CartaoErro(estado.erro, "Tentar novamente", onTentar)
                estado.pontos.isNotEmpty() -> ConteudoHistorico(estado.pontos)
            }
        }
    }
}

@Composable
private fun ConteudoHistorico(pontos: List<PontoHistorico>) {
    val primeiro = pontos.first()
    val ultimo = pontos.last()
    val variacao = if (primeiro.valor > 0) (ultimo.valor - primeiro.valor) / primeiro.valor * 100 else 0.0
    val diferenca = ultimo.valor - primeiro.valor
    val corVar = corVariacao(variacao)

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("Variação no período", style = MaterialTheme.typography.labelMedium)
            Text(
                "${sinal(variacao)}${"%.2f".format(Locale("pt", "BR"), variacao)}%",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = corVar,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Diferença", style = MaterialTheme.typography.labelMedium)
            Text(
                (if (diferenca >= 0) "+" else "") + Dinheiro.formatar(diferenca),
                style = MaterialTheme.typography.titleMedium, color = corVar,
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Grafico(pontos)
    Spacer(Modifier.height(12.dp))
    Text("Mês a mês", style = MaterialTheme.typography.titleSmall)
    HorizontalDivider(Modifier.padding(vertical = 4.dp))
    // Mais recente primeiro
    pontos.indices.reversed().forEach { i ->
        val p = pontos[i]
        val anterior = pontos.getOrNull(i - 1)
        val v = anterior?.let { if (it.valor > 0) (p.valor - it.valor) / it.valor * 100 else null }
        Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(p.mesReferencia.replaceFirstChar { it.uppercase() }, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(p.valorTexto.ifBlank { Dinheiro.formatar(p.valor) }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                v?.let { "${sinal(it)}${"%.1f".format(Locale("pt", "BR"), it)}%" } ?: "",
                modifier = Modifier.width(64.dp), textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall, color = v?.let { corVariacao(it) } ?: Color.Unspecified,
            )
        }
    }
}

private fun sinal(v: Double) = if (v > 0) "+" else ""

@Composable
private fun corVariacao(v: Double): Color = when {
    v > 0.005 -> Color(0xFF2E7D32)
    v < -0.005 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}

/** Grafico de linha simples desenhado com Canvas, sem bibliotecas externas. */
@Composable
fun Grafico(pontos: List<PontoHistorico>) {
    val cor = MaterialTheme.colorScheme.primary
    val grade = MaterialTheme.colorScheme.outlineVariant
    val max = pontos.maxOf { it.valor }
    val min = pontos.minOf { it.valor }
    Column {
        Text(Dinheiro.formatar(max), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Canvas(Modifier.fillMaxWidth().height(160.dp).padding(vertical = 6.dp)) {
            val amplitude = (max - min).takeIf { it > 0 } ?: 1.0
            val passoX = if (pontos.size > 1) size.width / (pontos.size - 1) else 0f
            fun pos(i: Int): Offset {
                val x = if (pontos.size > 1) i * passoX else size.width / 2
                val y = size.height - ((pontos[i].valor - min) / amplitude * size.height).toFloat()
                return Offset(x, if (max == min) size.height / 2 else y)
            }
            for (k in 0..3) {
                val y = size.height * k / 3
                drawLine(grade, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            }
            val linha = Path()
            val area = Path()
            pontos.indices.forEach { i ->
                val p = pos(i)
                if (i == 0) { linha.moveTo(p.x, p.y); area.moveTo(p.x, size.height); area.lineTo(p.x, p.y) }
                else { linha.lineTo(p.x, p.y); area.lineTo(p.x, p.y) }
            }
            area.lineTo(pos(pontos.lastIndex).x, size.height)
            area.close()
            drawPath(area, Brush.verticalGradient(listOf(cor.copy(alpha = 0.30f), Color.Transparent)))
            drawPath(linha, cor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            pontos.indices.forEach { i -> drawCircle(cor, radius = 3.dp.toPx(), center = pos(i)) }
            drawCircle(Color.White, radius = 2.dp.toPx(), center = pos(pontos.lastIndex))
        }
        Text(Dinheiro.formatar(min), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(pontos.first().mesReferencia, style = MaterialTheme.typography.labelSmall)
            if (pontos.size > 1) Text(pontos.last().mesReferencia, style = MaterialTheme.typography.labelSmall)
        }
    }
}
