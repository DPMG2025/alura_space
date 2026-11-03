package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import br.com.casinhadosaber.ui.componentes.caminhoEstrela
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Conquistas
import br.com.casinhadosaber.dominio.Diploma
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.EscalaDeTablet
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.tema.Cores

/**
 * Parede dos diplomas: um quadro para cada mundo completo (as 10 fases vencidas), separados por caminho.
 * Tocar num diploma abre ele grande, com o nome da criança, e o narrador lê. Sem diploma ainda, a parede
 * mostra o mundo que está mais perto de completar.
 */
@Composable
fun TelaDiplomas(
    estado: EstadoJogo,
    onVoltar: () -> Unit,
    /** Para as capturas: já abre com este diploma grande. */
    abrirDiploma: Diploma? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val nome = estado.perfil?.nome
    val diplomas = remember(estado.estrelas) { Conquistas.diplomas(estado.estrelas) }
    var aberto by remember { mutableStateOf(abrirDiploma) }
    var confete by remember { mutableIntStateOf(if (abrirDiploma != null) 1 else 0) }

    LaunchedEffect(Unit) {
        narrador.falar(
            if (diplomas.isEmpty()) "Parede dos diplomas! Complete as dez fases de um mundo para ganhar o primeiro."
            else "Parede dos diplomas! Você já tem ${diplomas.size}. Toque num diploma para ver.",
        )
    }

    EscalaDeTablet {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFFF6E7D0), Color(0xFFEBD3B0))))
                    .drawBehind { desenharPapelDeParede() }
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    BotaoRedondo("⬅️", "Voltar", onVoltar, tamanho = 52.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("📜 Diplomas", style = MaterialTheme.typography.headlineMedium, maxLines = 1)
                        Text(
                            "${diplomas.size} de ${Trilha.entries.size * Mundos.todos.size} mundos completos",
                            style = MaterialTheme.typography.titleSmall,
                            color = Cores.TextoSuave,
                            maxLines = 1,
                        )
                    }
                }
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (diplomas.isEmpty()) {
                        ParedeVazia(estado)
                    }
                    for (t in Trilha.entries) {
                        val daTrilha = diplomas.filter { it.trilha == t }
                        if (daTrilha.isEmpty()) continue
                        Text("${t.emoji} ${t.titulo}", style = MaterialTheme.typography.titleLarge, color = Color(t.corEscuraArgb))
                        for (linha in daTrilha.chunked(3)) {
                            // Fileira incompleta fica centralizada (meio espaço vazio de cada lado).
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                val vazios = 3 - linha.size
                                if (vazios > 0) Spacer(Modifier.weight(vazios / 2f))
                                for (d in linha) {
                                    QuadroDeDiploma(d, Modifier.weight(1f)) {
                                        sons.premio()
                                        aberto = d
                                        confete++
                                        narrador.falar(d.texto(nome))
                                    }
                                }
                                if (vazios > 0) Spacer(Modifier.weight(vazios / 2f))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
            aberto?.let { d -> DiplomaGrande(d, nome) { sons.toque(); aberto = null } }
            Confete(confete)
        }
    }
}

/** Sem diploma ainda: uma moldura vazia e o mundo mais perto de completar, para dar vontade. */
@Composable
private fun ParedeVazia(estado: EstadoJogo) {
    val perto = Trilha.entries.flatMap { t ->
        Mundos.todos.map { m -> Triple(t, m, (m.primeiraFase..m.ultimaFase).count { EstadoJogo.chave(t, it) in estado.estrelas }) }
    }.maxByOrNull { it.third }
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(width = 140.dp, height = 110.dp)
                .border(8.dp, Color(0xFFC98B5B), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) { Text("❔", fontSize = 40.sp) }
        Spacer(Modifier.height(12.dp))
        Text(
            "Complete as 10 fases de um mundo para pendurar o primeiro diploma!",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (perto != null && perto.third > 0) {
            val (t, m, n) = perto
            Spacer(Modifier.height(8.dp))
            Text(
                "${t.emoji} ${m.emoji} Mundo ${m.numero}: ${m.nome}: $n de ${Mundos.FASES_POR_MUNDO}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(t.corEscuraArgb),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun QuadroDeDiploma(d: Diploma, modifier: Modifier, onClick: () -> Unit) {
    val m = Mundos.todos[d.mundo]
    BotaoFofo(
        onClick = onClick,
        modifier = modifier.testTag("diploma"),
        cor = Color(0xFFC98B5B),
        corBase = Color(0xFF8D5A3B),
        forma = RoundedCornerShape(8.dp),
        descricao = d.titulo + ", caminho " + d.trilha.titulo,
        profundidade = 4.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFFFFFAE8), Color(0xFFFBEBC4))), RoundedCornerShape(4.dp))
                .border(1.5.dp, Color(d.trilha.corArgb).copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(m.emoji, fontSize = 30.sp)
            Text("Mundo ${m.numero}", style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), fontWeight = FontWeight.Bold, color = Cores.Texto, maxLines = 1)
            Text(m.nome, style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp, fontWeight = FontWeight.Normal), color = Cores.TextoSuave, maxLines = 1, textAlign = TextAlign.Center)
            Canvas(Modifier.padding(top = 2.dp).size(26.dp)) {
                desenharSelo(Offset(size.width / 2f, size.height * 0.42f), size.minDimension * 0.36f, Color(d.trilha.corArgb), comFitas = true)
            }
        }
    }
}

/** O diploma aberto, grande, com o texto ([Diploma.texto]) e o nome da criança. Toque em qualquer lugar fecha. */
@Composable
private fun DiplomaGrande(d: Diploma, nome: String?, onFechar: () -> Unit) {
    val m = Mundos.todos[d.mundo]
    val cor = Color(d.trilha.corArgb)
    val corEscura = Color(d.trilha.corEscuraArgb)
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x99231A3A))
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onFechar)
            .semantics { contentDescription = "Fechar o diploma" },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(20.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .shadow(18.dp, RoundedCornerShape(10.dp))
                .drawBehind { desenharCertificado(cor, corEscura) }
                .padding(horizontal = 30.dp, vertical = 26.dp)
                .testTag("diploma_aberto"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Faixa de fita com o título.
            Box(
                Modifier
                    .drawBehind { desenharFaixa(cor, corEscura) }
                    .padding(start = 30.dp, end = 30.dp, top = 4.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("DIPLOMA", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp, letterSpacing = 4.sp), fontWeight = FontWeight.Bold, color = Color.White)
            }
            Text("${d.trilha.emoji}  ${m.emoji}", fontSize = 32.sp, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
            if (!nome.isNullOrBlank()) {
                Text(nome, style = MaterialTheme.typography.headlineMedium, color = corEscura, textAlign = TextAlign.Center)
                // Linha de assinatura embaixo do nome.
                Box(Modifier.padding(top = 2.dp, bottom = 8.dp).width(160.dp).height(2.dp).background(corEscura.copy(alpha = 0.35f)))
            }
            Text(d.texto(nome), style = MaterialTheme.typography.titleLarge, color = Cores.Texto, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Spacer(Modifier.size(52.dp))
                // O selo dourado, com as duas fitinhas na cor do caminho.
                Canvas(Modifier.size(104.dp)) {
                    desenharSelo(Offset(size.width / 2f, size.height * 0.42f), size.minDimension * 0.34f, cor, comFitas = true)
                }
                BotaoRedondo("✖", "Fechar", onFechar, tamanho = 52.dp)
            }
        }
    }
}

/** Papel de parede listradinho, bem clarinho, atrás dos quadros, com rodapé de madeira. */
private fun DrawScope.desenharPapelDeParede() {
    val passo = 28.dp.toPx()
    var x = passo / 2f
    while (x < size.width) {
        drawRect(Color(0x17FFFFFF), Offset(x, 0f), Size(passo * 0.45f, size.height))
        x += passo
    }
    val rodape = 22.dp.toPx()
    drawRect(
        Brush.verticalGradient(listOf(Color(0xFFC98B5B), Color(0xFF9C6A43)), startY = size.height - rodape, endY = size.height),
        Offset(0f, size.height - rodape), Size(size.width, rodape),
    )
    drawRect(Color(0x33FFFFFF), Offset(0f, size.height - rodape), Size(size.width, 2.dp.toPx()))
}

/** O papel do certificado: pergaminho, borda dupla na cor do caminho e enfeites nos quatro cantos. */
private fun DrawScope.desenharCertificado(cor: Color, corEscura: Color) {
    val raio = CornerRadius(10.dp.toPx())
    drawRoundRect(Brush.radialGradient(listOf(Color(0xFFFFFCEE), Color(0xFFF6E4B8)), center, size.maxDimension * 0.75f), cornerRadius = raio)
    val m1 = 9.dp.toPx()
    val m2 = 15.dp.toPx()
    drawRoundRect(cor, Offset(m1, m1), Size(size.width - m1 * 2, size.height - m1 * 2), CornerRadius(4.dp.toPx()), style = Stroke(3.dp.toPx()))
    drawRoundRect(Color(0xFFE0B23C), Offset(m2, m2), Size(size.width - m2 * 2, size.height - m2 * 2), CornerRadius(2.dp.toPx()), style = Stroke(1.5.dp.toPx()))
    // Enfeite de cada canto: losango dourado com bolinhas, virado para dentro.
    val r = 9.dp.toPx()
    for ((sx, sy) in listOf(1f to 1f, -1f to 1f, 1f to -1f, -1f to -1f)) {
        val c = Offset(if (sx > 0) m1 else size.width - m1, if (sy > 0) m1 else size.height - m1)
        val losango = Path().apply {
            moveTo(c.x, c.y - r); lineTo(c.x + r, c.y); lineTo(c.x, c.y + r); lineTo(c.x - r, c.y); close()
        }
        drawPath(losango, Color(0xFFFFC93C))
        drawPath(losango, corEscura, style = Stroke(1.5.dp.toPx()))
        drawCircle(corEscura, 2.dp.toPx(), c)
        drawCircle(cor, 2.5.dp.toPx(), Offset(c.x + sx * r * 1.7f, c.y))
        drawCircle(cor, 2.5.dp.toPx(), Offset(c.x, c.y + sy * r * 1.7f))
    }
}

/** Fita do título: a faixa da frente e as pontas dobradas atrás, cortadas em V. */
private fun DrawScope.desenharFaixa(cor: Color, corEscura: Color) {
    val w = size.width
    val h = size.height
    val aba = h * 0.6f
    for (lado in listOf(-1f, 1f)) {
        val x0 = if (lado < 0) aba * 0.4f else w - aba * 0.4f
        val ponta = Path().apply {
            moveTo(x0, h * 0.25f)
            lineTo(x0 + lado * aba, h * 0.25f)
            lineTo(x0 + lado * aba * 0.6f, h * 0.62f)
            lineTo(x0 + lado * aba, h)
            lineTo(x0, h)
            close()
        }
        drawPath(ponta, corEscura)
    }
    drawRoundRect(cor, Offset(0f, 0f), Size(w, h * 0.86f), CornerRadius(4.dp.toPx()))
    drawRect(Color(0x38FFFFFF), Offset(0f, h * 0.08f), Size(w, h * 0.14f))
    drawRect(corEscura.copy(alpha = 0.45f), Offset(0f, h * 0.86f - 2.dp.toPx()), Size(w, 2.dp.toPx()))
}

/** Selo dourado de borda ondulada, com estrela no meio e (se [comFitas]) duas fitinhas penduradas. */
private fun DrawScope.desenharSelo(c: Offset, r: Float, fita: Color, comFitas: Boolean) {
    if (comFitas) {
        for (lado in listOf(-1f, 1f)) {
            val topo = Offset(c.x + lado * r * 0.25f, c.y + r * 0.4f)
            val p = Path().apply {
                moveTo(topo.x - r * 0.28f, topo.y)
                lineTo(topo.x + r * 0.28f, topo.y)
                lineTo(topo.x + lado * r * 0.45f + r * 0.25f, c.y + r * 1.6f)
                lineTo(topo.x + lado * r * 0.45f, c.y + r * 1.4f)
                lineTo(topo.x + lado * r * 0.45f - r * 0.25f, c.y + r * 1.6f)
                close()
            }
            drawPath(p, fita)
            drawPath(p, Color(0x33000000), style = Stroke(r * 0.04f))
        }
    }
    val dentes = 22
    val borda = Path().apply {
        for (i in 0 until dentes * 2) {
            val a = (i * PI / dentes).toFloat()
            val rr = if (i % 2 == 0) r else r * 0.88f
            val p = Offset(c.x + cos(a) * rr, c.y + sin(a) * rr)
            if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
        close()
    }
    drawPath(borda, Brush.radialGradient(listOf(Color(0xFFFFE27A), Color(0xFFE0A800)), Offset(c.x - r * 0.3f, c.y - r * 0.3f), r * 1.3f))
    drawPath(borda, Color(0xFFB07A00), style = Stroke(r * 0.05f))
    drawCircle(Color(0xFFB07A00), r * 0.68f, c, style = Stroke(r * 0.05f))
    drawPath(caminhoEstrela(c, r * 0.5f), Color(0xFFFFF3B0))
    drawPath(caminhoEstrela(c, r * 0.5f), Color(0xFFB07A00), style = Stroke(r * 0.04f))
    drawCircle(Color(0x66FFFFFF), r * 0.12f, Offset(c.x - r * 0.45f, c.y - r * 0.45f))
}
