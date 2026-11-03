package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.CenarioDoLivrinho
import br.com.casinhadosaber.ui.componentes.caminhoEstrela

/**
 * A ilustração de uma página do livrinho: o cenário desenhado em Canvas (céu, chão e detalhes de cada lugar)
 * e as figuras da página em emoji, grandes, em cima do chão. Sem animação: a página fica parada para ler.
 */
@Composable
fun CenaDoLivrinho(cenario: CenarioDoLivrinho, figuras: List<String>, modifier: Modifier = Modifier) {
    Box(
        modifier
            .shadow(6.dp, RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .border(5.dp, Color.White, RoundedCornerShape(28.dp))
            .clearAndSetSemantics { },
    ) {
        Canvas(Modifier.fillMaxSize()) { desenharCenario(cenario) }
        // Emojis grandes ficam borrados (são imagenzinhas ampliadas): o maior fica em 84 sp.
        val tamanho = when (figuras.size) { 1 -> 84; 2 -> 72; 3 -> 62; else -> 52 }
        Row(
            Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 14.dp, start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            for (f in figuras) {
                // Cada figura pisa no chão: uma sombrinha oval embaixo dela.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(f, fontSize = tamanho.sp, lineHeight = (tamanho * 1.1f).sp)
                    Box(
                        Modifier
                            .offset(y = (-8).dp)
                            .size(width = (tamanho * 0.75f).dp, height = 9.dp)
                            .background(Color(0x2E000000), RoundedCornerShape(50)),
                    )
                }
            }
        }
    }
}

private fun DrawScope.desenharCenario(c: CenarioDoLivrinho) {
    val w = size.width
    val h = size.height
    val ceu = Color(c.ceu)
    val chao = Color(c.chao)
    drawRect(Brush.verticalGradient(listOf(ceu, Color.White.copy(alpha = 0.6f).compositeSobre(ceu))))
    val horizonte = h * 0.62f
    when (c) {
        CenarioDoLivrinho.NOITE, CenarioDoLivrinho.CEU -> {
            for (k in 0 until 14) {
                val x = ((k * 0.37f + 0.07f) % 1f) * w
                val y = ((k * 0.23f + 0.04f) % 0.5f) * h
                drawPath(caminhoEstrela(Offset(x, y), (3 + k % 3 * 2).dp.toPx()), Color(0xFFFFF3B0).copy(alpha = 0.85f))
            }
            drawCircle(Color(0xFFFFF1A8), radius = h * 0.09f, center = Offset(w * 0.82f, h * 0.18f))
            drawCircle(ceu, radius = h * 0.08f, center = Offset(w * 0.86f, h * 0.16f))
        }
        CenarioDoLivrinho.CASA, CenarioDoLivrinho.ESCOLA, CenarioDoLivrinho.FESTA -> {
            // Parede com janela (dentro de casa ou da escola) e, na festa, bandeirinhas.
            drawRect(Color(c.ceu), size = Size(w, horizonte))
            val jx = w * 0.62f
            val jy = h * 0.1f
            drawRoundRect(Color(0xFFBDE8FF), topLeft = Offset(jx, jy), size = Size(w * 0.28f, h * 0.26f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f))
            drawRect(Color.White, topLeft = Offset(jx + w * 0.135f, jy), size = Size(6.dp.toPx(), h * 0.26f))
            drawRect(Color.White, topLeft = Offset(jx, jy + h * 0.125f), size = Size(w * 0.28f, 6.dp.toPx()))
            if (c == CenarioDoLivrinho.ESCOLA) {
                drawRoundRect(Color(0xFF2F6B4F), topLeft = Offset(w * 0.06f, h * 0.08f), size = Size(w * 0.46f, h * 0.3f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f))
                drawRect(Color.White.copy(alpha = 0.7f), topLeft = Offset(w * 0.1f, h * 0.18f), size = Size(w * 0.2f, 4.dp.toPx()))
                drawRect(Color.White.copy(alpha = 0.7f), topLeft = Offset(w * 0.1f, h * 0.26f), size = Size(w * 0.3f, 4.dp.toPx()))
            }
            if (c == CenarioDoLivrinho.FESTA) {
                val cores = listOf(Color(0xFFFF6FAE), Color(0xFFFFC93C), Color(0xFF4D96FF), Color(0xFF3DBE6B))
                for (i in 0 until 9) {
                    val x0 = w * i / 9f
                    val p = Path().apply { moveTo(x0, h * 0.04f); lineTo(x0 + w / 9f, h * 0.04f); lineTo(x0 + w / 18f, h * 0.13f); close() }
                    drawPath(p, cores[i % cores.size])
                }
            }
        }
        else -> {
            drawCircle(Color(0xFFFFD54F), radius = h * 0.08f, center = Offset(w * 0.84f, h * 0.17f))
            nuvem(Offset(w * 0.22f, h * 0.16f), h * 0.05f)
            nuvem(Offset(w * 0.55f, h * 0.1f), h * 0.04f)
        }
    }
    // Chão: morro ondulado (ou o mar, a areia e o lago).
    val morro = Path().apply {
        moveTo(0f, horizonte)
        cubicTo(w * 0.3f, horizonte - h * 0.06f, w * 0.7f, horizonte + h * 0.04f, w, horizonte - h * 0.02f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    when (c) {
        CenarioDoLivrinho.MAR -> {
            drawRect(chao, topLeft = Offset(0f, horizonte - h * 0.04f), size = Size(w, h))
            for (k in 0 until 4) {
                val y = horizonte + h * (0.06f + k * 0.08f)
                val onda = Path().apply {
                    moveTo(0f, y)
                    var x = 0f
                    while (x < w) { quadraticTo(x + w / 16f, y - h * 0.025f, x + w / 8f, y); x += w / 8f }
                }
                drawPath(onda, Color.White.copy(alpha = 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
            }
        }
        CenarioDoLivrinho.PRAIA -> {
            drawRect(Color(0xFF4FA9DB), topLeft = Offset(0f, horizonte - h * 0.08f), size = Size(w, h * 0.12f))
            drawPath(morro, chao)
        }
        CenarioDoLivrinho.LAGO -> {
            drawPath(morro, chao)
            drawOval(Color(0xFF6EC6F0), topLeft = Offset(w * 0.12f, horizonte + h * 0.12f), size = Size(w * 0.76f, h * 0.2f))
        }
        else -> drawPath(morro, chao)
    }
    if (c == CenarioDoLivrinho.MATA || c == CenarioDoLivrinho.JARDIM) {
        // Árvores ao fundo (mata) ou florzinhas (jardim).
        for (k in 0 until 5) {
            val x = w * (0.08f + k * 0.21f)
            if (c == CenarioDoLivrinho.MATA) {
                drawRect(Color(0xFF8B5A3C), topLeft = Offset(x - 5.dp.toPx(), horizonte - h * 0.1f), size = Size(10.dp.toPx(), h * 0.12f))
                drawCircle(Color(0xFF3E8E4E), radius = h * 0.08f, center = Offset(x, horizonte - h * 0.14f))
            } else {
                val y = horizonte + h * (0.05f + (k % 2) * 0.04f)
                drawCircle(listOf(Color(0xFFFF6FAE), Color(0xFFFFC93C), Color.White)[k % 3], radius = 5.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

private fun DrawScope.nuvem(c: Offset, r: Float) {
    val branco = Color.White.copy(alpha = 0.9f)
    drawCircle(branco, r, c)
    drawCircle(branco, r * 0.8f, c + Offset(r * 1.1f, r * 0.15f))
    drawCircle(branco, r * 0.75f, c + Offset(-r * 1.05f, r * 0.2f))
}

/** Mistura simples de uma cor com transparência por cima de outra (para o degradê do céu). */
private fun Color.compositeSobre(fundo: Color): Color = Color(
    red = red * alpha + fundo.red * (1 - alpha),
    green = green * alpha + fundo.green * (1 - alpha),
    blue = blue * alpha + fundo.blue * (1 - alpha),
)
