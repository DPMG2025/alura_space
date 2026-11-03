package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.Historia
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Corujinha
import br.com.casinhadosaber.ui.componentes.caminhoEstrela
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

/**
 * Cena curta da história (sugestão 3): a Corujinha Professora conta, num balão, o que acontece no mundo,
 * com o amigo do caminho ao lado. O narrador lê o texto; o botão grande segue em frente. [chefao] troca o título e o botão para a festa da vitória do chefão.
 * Marca de teste do botão: "historia_continuar".
 */
@Composable
fun CenaDaHistoria(fase: Fase, texto: String, chefao: Boolean = false, onContinuar: () -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val mundo = Mundos.de(fase)
    val capitulo = Historia.capitulo(fase)
    val cor = Color(mundo.corArgb)
    val corDaTrilha = Color(fase.trilha.corArgb)
    LaunchedEffect(texto) { narrador.falar(texto) }
    fun seguir() {
        narrador.parar()
        sons.toque()
        onContinuar()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(corDaTrilha, cor, Cores.Fundo)))
            // Segura os toques: nada atrás da cena responde enquanto ela está aberta.
            .clickable(remember { MutableInteractionSource() }, indication = null) { }
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // Estrelinhas e morrinho ao fundo, no estilo das outras telas.
        Canvas(Modifier.fillMaxSize()) {
            for (k in 0 until 10) {
                val x = ((k * 0.41f + 0.13f) % 1f) * size.width
                val y = ((k * 0.29f + 0.05f) % 0.55f) * size.height
                drawPath(caminhoEstrela(Offset(x, y), (5 + (k % 3) * 3).dp.toPx()), Color.White.copy(alpha = 0.4f))
            }
            desenharMorrosComFlores()
        }
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // O texto e os desenhos rolam em telas baixas; o botão fica sempre à vista, embaixo.
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    if (chefao) "${mundo.emoji} Você venceu o chefão!" else "${mundo.emoji} ${mundo.titulo}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                if (chefao) {
                    Text(capitulo.guardiao, style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
                }
                // Balão de fala da Corujinha, com a pontinha grudada embaixo, apontando para ela.
                Column(Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(28.dp))
                            .background(Color.White, RoundedCornerShape(28.dp))
                            .border(4.dp, Color(0xFFEDE6FF), RoundedCornerShape(28.dp))
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Corujinha Professora",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Cores.Roxo,
                                    modifier = Modifier.weight(1f),
                                )
                                BotaoRedondo("🔊", "Ouvir de novo", { narrador.falar(texto) }, tamanho = 48.dp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(texto, style = MaterialTheme.typography.titleLarge, color = Cores.Texto)
                        }
                    }
                    // A pontinha sobe 6 dp para dentro do balão (cobre a borda) e desce até perto da Corujinha.
                    Canvas(
                        Modifier
                            .padding(start = 72.dp)
                            .offset(y = (-6).dp)
                            .size(width = 44.dp, height = 30.dp),
                    ) {
                        val ponta = Path().apply {
                            moveTo(0f, 0f)
                            lineTo(size.width, 0f)
                            quadraticTo(size.width * 0.55f, size.height * 0.45f, size.width * 0.18f, size.height)
                            quadraticTo(size.width * 0.28f, size.height * 0.45f, 0f, 0f)
                            close()
                        }
                        drawPath(ponta, Color.White)
                        // Borda lilás só nos dois lados (em cima ela se junta ao balão).
                        val lados = Path().apply {
                            moveTo(0f, 6.dp.toPx())
                            quadraticTo(size.width * 0.28f, size.height * 0.45f, size.width * 0.18f, size.height)
                            quadraticTo(size.width * 0.55f, size.height * 0.45f, size.width, 6.dp.toPx())
                        }
                        drawPath(lados, Color(0xFFEDE6FF), style = Stroke(4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawRect(Color.White, Offset(4.dp.toPx(), 0f), Size(size.width - 8.dp.toPx(), 6.dp.toPx() + 1f))
                    }
                }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 180.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Corujinha(Modifier.height(210.dp), falando = true)
                    Spacer(Modifier.size(12.dp))
                    // O amigo do caminho, junto da Corujinha, num pratinho redondo com sombra.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(104.dp)
                                .drawBehind {
                                    drawOval(Color(0x33000000), Offset(size.width * 0.1f, size.height * 0.86f), Size(size.width * 0.8f, size.height * 0.16f))
                                }
                                .shadow(4.dp, CircleShape)
                                .background(Brush.radialGradient(listOf(Color.White, Color(0xFFFFF3D6))), CircleShape)
                                .border(4.dp, cor.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(capitulo.amigo.emoji, fontSize = 52.sp, lineHeight = 60.sp)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            capitulo.amigo.nome.substringBefore(","),
                            style = MaterialTheme.typography.titleMedium,
                            color = Cores.Texto,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(50))
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            BotaoFofo(
                onClick = ::seguir,
                cor = Cores.Certo,
                descricao = if (chefao) "Oba!" else "Vamos lá!",
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .testTag("historia_continuar")
                    .pulsando(),
            ) {
                Text(
                    if (chefao) "🎉 Oba!" else "▶ Vamos lá!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                )
            }
        }
    }
}

/**
 * Dois morros (um mais claro atrás) com florzinhas, moitas e uma arvorezinha, para o chão da história não
 * ficar vazio. Tudo em posições fixas.
 */
private fun DrawScope.desenharMorrosComFlores() {
    val w = size.width
    val h = size.height
    val tras = Path().apply {
        moveTo(0f, h * 0.76f)
        cubicTo(w * 0.2f, h * 0.68f, w * 0.45f, h * 0.73f, w * 0.62f, h * 0.7f)
        cubicTo(w * 0.8f, h * 0.67f, w * 0.92f, h * 0.71f, w, h * 0.73f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(tras, Color(0xFFB6E3A0).copy(alpha = 0.8f))
    val frente = Path().apply {
        moveTo(0f, h * 0.82f)
        cubicTo(w * 0.3f, h * 0.74f, w * 0.7f, h * 0.76f, w, h * 0.8f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(frente, Brush.verticalGradient(listOf(Color(0xFF94D47C), Color(0xFF6DB85A)), startY = h * 0.75f, endY = h))
    // Arvorezinha no morro de trás, no cantinho da direita.
    val tronco = Offset(w * 0.95f, h * 0.745f)
    val u = h * 0.012f
    drawRoundRect(Color(0xFF9A6A43), Offset(tronco.x - u, tronco.y - u * 4f), Size(u * 2f, u * 5f), CornerRadius(u))
    drawCircle(Color(0xFF5FAE4E), u * 3.6f, Offset(tronco.x, tronco.y - u * 6f))
    drawCircle(Color(0xFF6CC05A), u * 2.6f, Offset(tronco.x - u * 2.4f, tronco.y - u * 4.6f))
    drawCircle(Color(0x33FFFFFF), u * 1.2f, Offset(tronco.x - u * 1.2f, tronco.y - u * 7.2f))
    // Moitas.
    for ((x, y) in listOf(0.1f to 0.79f, 0.58f to 0.765f)) {
        val c = Offset(w * x, h * y)
        drawCircle(Color(0xFF5FAE4E), u * 2.4f, c)
        drawCircle(Color(0xFF5FAE4E), u * 1.9f, Offset(c.x - u * 2.4f, c.y + u * 0.6f))
        drawCircle(Color(0xFF5FAE4E), u * 1.9f, Offset(c.x + u * 2.4f, c.y + u * 0.6f))
    }
    // Florzinhas espalhadas no morro da frente.
    val cores = listOf(Color(0xFFFF7AA2), Color(0xFFFFD166), Color.White, Color(0xFFB79CFF))
    val flores = listOf(0.05f to 0.86f, 0.18f to 0.82f, 0.3f to 0.9f, 0.42f to 0.8f, 0.55f to 0.88f, 0.68f to 0.81f, 0.8f to 0.9f, 0.93f to 0.84f, 0.36f to 0.96f, 0.74f to 0.97f)
    for ((i, f) in flores.withIndex()) {
        val c = Offset(w * f.first, h * f.second)
        val r = u * 0.9f
        drawLine(Color(0xFF4E9A3E), c, Offset(c.x, c.y + r * 2.6f), r * 0.45f, StrokeCap.Round)
        for (k in 0 until 5) {
            val a = k * 1.2566f
            drawCircle(cores[i % cores.size], r * 0.72f, Offset(c.x + cos(a) * r, c.y + sin(a) * r))
        }
        drawCircle(Color(0xFFFFB13C), r * 0.55f, c)
    }
}
