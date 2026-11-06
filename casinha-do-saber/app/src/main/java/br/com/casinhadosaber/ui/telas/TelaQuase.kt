package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Irmaozinho
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

/**
 * A fase não foi vencida (menos de 3 acertos de primeira). Sem bronca: o presente
 * continua fechado, tremendo de vontade de abrir, e a criança é convidada a tentar de novo.
 */
@Composable
fun TelaQuase(
    fase: Fase,
    acertos: Int,
    perfil: Perfil?,
    onTentarDeNovo: () -> Unit,
    onVoltar: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val tremor = rememberInfiniteTransition(label = "tremor")
    val giro by tremor.animateFloat(-6f, 6f, infiniteRepeatable(tween(140), RepeatMode.Reverse), label = "giro")

    LaunchedEffect(Unit) {
        sons.toque()
        narrador.falar(
            "Quase! Você acertou $acertos de primeira. O presente quer sair da caixa! " +
                "Para abrir, acerte quatro de primeira. Vamos tentar de novo?",
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(fase.trilha.corArgb), Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Quase!", style = MaterialTheme.typography.displayLarge, color = Color.White)
        Spacer(Modifier.height(8.dp))
        // Cinco bolinhas: verdes as que acertou de primeira; o alvo (4) fica marcado.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { k ->
                Box(
                    Modifier
                        .size(if (k == EstadoJogo.MINIMO_PARA_VENCER - 1) 30.dp else 24.dp)
                        .background(if (k < acertos) Cores.Certo else Color.White, CircleShape)
                        .border(3.dp, if (k == EstadoJogo.MINIMO_PARA_VENCER - 1) Cores.Estrela else Color(0x33000000), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (k == EstadoJogo.MINIMO_PARA_VENCER - 1) Text("🎁", fontSize = 14.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        // O presente é desenhado (e não um emoji ampliado, que fica borrado): nítido em qualquer tela.
        // Um holofote suave desce de cima e ilumina o presente.
        Box(
            Modifier
                .size(190.dp)
                .drawBehind {
                    val c = center
                    val feixe = Path().apply {
                        moveTo(c.x - 26.dp.toPx(), c.y - 420.dp.toPx())
                        lineTo(c.x + 26.dp.toPx(), c.y - 420.dp.toPx())
                        lineTo(c.x + size.width * 0.5f, c.y)
                        lineTo(c.x - size.width * 0.5f, c.y)
                        close()
                    }
                    drawPath(feixe, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0f), Color.White.copy(alpha = 0.32f)), startY = c.y - 420.dp.toPx(), endY = c.y))
                    drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0f)), c, size.width * 0.75f), size.width * 0.75f, c)
                }
                .background(Color.White.copy(alpha = 0.88f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            PresenteFechado(Modifier.size(120.dp).graphicsLayer { rotationZ = giro })
            Text("🔒", fontSize = 32.sp, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp))
        }
        Spacer(Modifier.height(12.dp))
        perfil?.let {
            Row(verticalAlignment = Alignment.Bottom) {
                Crianca(it, Modifier.height(130.dp), humor = Humor.TORCENDO)
                // O irmãozinho dá tchau, animando para tentar de novo.
                if (it.irmao.ligado) Irmaozinho(it, Modifier.height(86.dp), acenando = true)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            BotaoRedondo("🗺️", "Voltar para o caminho", onVoltar, tamanho = 60.dp)
            Box {
                BotaoFofo(onClick = onTentarDeNovo, cor = Cores.Certo, modifier = Modifier.pulsando(), descricao = "Tentar de novo") {
                    Text(
                        "🔄 De novo",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
                MaoGuia(Modifier.align(Alignment.BottomEnd).offset(x = 30.dp, y = 34.dp), espera = 2500)
            }
        }
    }
}

/** Caixa de presente desenhada em Canvas: tampa, fita vermelha, laço com contorno e brilho. */
@Composable
private fun PresenteFechado(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val contorno = Color(0xFF8A3B12)
        val traco = w * 0.025f
        val caixa = Color(0xFFFFB238)
        val caixaSombra = Color(0xFFF08A1C)
        val fita = Color(0xFFE53950)
        // sombra no chão
        drawOval(Color(0x29000000), Offset(w * 0.14f, h * 0.9f), Size(w * 0.72f, h * 0.08f))
        // corpo da caixa (com o lado direito mais escuro)
        val corpoTopo = h * 0.45f
        val corpo = androidx.compose.ui.geometry.Rect(w * 0.17f, corpoTopo, w * 0.83f, h * 0.93f)
        drawRoundRect(caixa, corpo.topLeft, corpo.size, CornerRadius(w * 0.04f))
        drawRect(caixaSombra, Offset(w * 0.6f, corpoTopo), Size(w * 0.23f, corpo.height - w * 0.04f))
        drawRoundRect(contorno, corpo.topLeft, corpo.size, CornerRadius(w * 0.04f), style = Stroke(traco))
        // tampa, um pouco mais larga
        val tampa = androidx.compose.ui.geometry.Rect(w * 0.11f, h * 0.33f, w * 0.89f, h * 0.48f)
        drawRoundRect(caixa, tampa.topLeft, tampa.size, CornerRadius(w * 0.04f))
        drawRect(caixaSombra, Offset(w * 0.6f, tampa.top + traco), Size(w * 0.29f - traco, tampa.height - traco * 2))
        drawRect(Color(0x55FFFFFF), Offset(w * 0.14f, tampa.top + traco), Size(w * 0.4f, tampa.height * 0.28f))
        drawRoundRect(contorno, tampa.topLeft, tampa.size, CornerRadius(w * 0.04f), style = Stroke(traco))
        // fita vertical e na tampa
        drawRect(fita, Offset(w * 0.44f, tampa.top), Size(w * 0.12f, corpo.bottom - tampa.top))
        drawLine(contorno, Offset(w * 0.44f, tampa.top), Offset(w * 0.44f, corpo.bottom), traco * 0.8f)
        drawLine(contorno, Offset(w * 0.56f, tampa.top), Offset(w * 0.56f, corpo.bottom), traco * 0.8f)
        drawLine(Color(0x66FFFFFF), Offset(w * 0.47f, tampa.top + traco), Offset(w * 0.47f, corpo.bottom - traco), traco * 0.8f)
        // laço: duas alças e as pontinhas
        val no = Offset(w * 0.5f, h * 0.31f)
        for (lado in listOf(-1f, 1f)) {
            val alca = Path().apply {
                moveTo(no.x, no.y)
                cubicTo(no.x + lado * w * 0.12f, no.y - h * 0.26f, no.x + lado * w * 0.36f, no.y - h * 0.16f, no.x + lado * w * 0.24f, no.y + h * 0.01f)
                cubicTo(no.x + lado * w * 0.18f, no.y + h * 0.04f, no.x + lado * w * 0.08f, no.y + h * 0.02f, no.x, no.y)
                close()
            }
            drawPath(alca, fita)
            drawPath(alca, contorno, style = Stroke(traco, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            drawLine(Color(0x66FFFFFF), Offset(no.x + lado * w * 0.1f, no.y - h * 0.1f), Offset(no.x + lado * w * 0.18f, no.y - h * 0.11f), traco, StrokeCap.Round)
        }
        drawCircle(fita, w * 0.065f, no)
        drawCircle(contorno, w * 0.065f, no, style = Stroke(traco))
        // brilhinhos
        drawCircle(Color.White, w * 0.022f, Offset(w * 0.25f, h * 0.55f))
        drawCircle(Color(0xAAFFFFFF), w * 0.014f, Offset(w * 0.3f, h * 0.62f))
    }
}
