package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.PresenteEspecial
import br.com.casinhadosaber.dominio.TipoDeEvento
import br.com.casinhadosaber.ui.tema.Cores

// Enfeites dos eventos do ano (sugestão 4) desenhados por cima da cena da casa, sem pegar toques:
// pisca pisca e árvore no Natal, ovinhos na Páscoa, bandeirinhas na Festa Junina, balões no Dia das
// Crianças e festa com bolo no aniversário. A cena continua respondendo ao dedo por baixo deles.

private val coresDeFesta = listOf(Color(0xFFFF5C9A), Color(0xFFFFC93C), Color(0xFF4D96FF), Color(0xFF3DBE6B), Color(0xFFFF7A3D), Color(0xFF9B6BFF))

/** Os enfeites do [tipo] ocupando a área da cena. [brilho] anima as luzinhas (0 a 1). */
@Composable
fun DecoracaoDoEvento(tipo: TipoDeEvento, modifier: Modifier = Modifier) {
    val transicao = rememberInfiniteTransition(label = "enfeites")
    val brilho by transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "brilho")
    Canvas(modifier.fillMaxSize()) { desenharDecoracao(tipo, brilho) }
}

fun DrawScope.desenharDecoracao(tipo: TipoDeEvento, brilho: Float = 1f) {
    when (tipo) {
        TipoDeEvento.NATAL -> {
            piscaPisca(brilho)
            arvoreDeNatal(Offset(size.width * 0.12f, size.height * 0.97f), size.minDimension * 0.3f)
        }
        TipoDeEvento.PASCOA -> {
            val ovos = listOf(0.06f to 0.95f, 0.17f to 0.97f, 0.83f to 0.96f, 0.93f to 0.94f, 0.28f to 0.985f)
            ovos.forEachIndexed { i, (u, v) -> ovoDePascoa(Offset(size.width * u, size.height * v), size.minDimension * 0.05f, i) }
        }
        TipoDeEvento.FESTA_JUNINA -> {
            bandeirinhas(size.height * 0.05f, size.height * 0.1f, 0)
            bandeirinhas(size.height * 0.13f, size.height * 0.19f, 3)
        }
        TipoDeEvento.DIA_DAS_CRIANCAS -> {
            cachoDeBaloes(Offset(size.width * 0.1f, size.height * 0.36f), size.minDimension * 0.06f, 0, brilho)
            cachoDeBaloes(Offset(size.width * 0.9f, size.height * 0.34f), size.minDimension * 0.06f, 2, brilho)
            pipa(Offset(size.width * 0.78f, size.height * 0.1f), size.minDimension * 0.07f)
        }
        TipoDeEvento.ANIVERSARIO -> {
            bandeirinhas(size.height * 0.05f, size.height * 0.1f, 1)
            cachoDeBaloes(Offset(size.width * 0.1f, size.height * 0.4f), size.minDimension * 0.06f, 1, brilho)
            cachoDeBaloes(Offset(size.width * 0.9f, size.height * 0.38f), size.minDimension * 0.06f, 4, brilho)
            boloDeAniversario(Offset(size.width * 0.86f, size.height * 0.97f), size.minDimension * 0.2f, brilho)
        }
    }
}

/** Fio do pisca pisca caindo em curva no alto da cena, com lâmpadas coloridas que acendem alternadas. */
private fun DrawScope.piscaPisca(brilho: Float) {
    val y0 = size.height * 0.04f
    val curva = size.height * 0.05f
    val n = 14
    val fio = Path().apply {
        moveTo(0f, y0)
        cubicTo(size.width * 0.33f, y0 + curva * 2f, size.width * 0.66f, y0 + curva * 2f, size.width, y0)
    }
    drawPath(fio, Color(0xFF2E5E3A), style = Stroke(size.minDimension * 0.006f))
    for (i in 1 until n) {
        val f = i / n.toFloat()
        // Ponto da curva (bezier cúbica com os mesmos controles do fio).
        val x = size.width * (3 * (1 - f) * (1 - f) * f * 0.33f + 3 * (1 - f) * f * f * 0.66f + f * f * f)
        val y = (1 - f) * (1 - f) * (1 - f) * y0 + 3 * (1 - f) * (1 - f) * f * (y0 + curva * 2f) + 3 * (1 - f) * f * f * (y0 + curva * 2f) + f * f * f * y0
        val cor = coresDeFesta[i % coresDeFesta.size]
        val acesa = if (i % 2 == 0) brilho else 1f - brilho
        val r = size.minDimension * 0.016f
        drawCircle(cor.copy(alpha = 0.25f + 0.35f * acesa), r * 2.2f, Offset(x, y + r * 1.4f))
        drawOval(cor, Offset(x - r * 0.8f, y + r * 0.4f), Size(r * 1.6f, r * 2.2f))
        drawRect(Color(0xFF6B6B6B), Offset(x - r * 0.5f, y - r * 0.1f), Size(r, r * 0.6f))
    }
}

private fun DrawScope.arvoreDeNatal(base: Offset, altura: Float) {
    val l = altura * 0.75f
    drawRect(Color(0xFF8A5A35), Offset(base.x - l * 0.08f, base.y - altura * 0.14f), Size(l * 0.16f, altura * 0.14f))
    for (k in 0..2) {
        val topo = base.y - altura * (0.45f + k * 0.2f)
        val baixo = base.y - altura * (0.12f + k * 0.2f)
        val larg = l * (0.5f - k * 0.12f)
        val camada = Path().apply { moveTo(base.x, topo); lineTo(base.x + larg, baixo); lineTo(base.x - larg, baixo); close() }
        drawPath(camada, Color(0xFF2E9E4F))
        drawPath(camada, Color(0xFF1F7A3A), style = Stroke(altura * 0.015f))
    }
    // Bolinhas.
    val bolas = listOf(-0.22f to 0.2f, 0.18f to 0.26f, -0.1f to 0.4f, 0.14f to 0.48f, -0.05f to 0.62f, 0.25f to 0.17f)
    bolas.forEachIndexed { i, (u, v) -> drawCircle(coresDeFesta[i % coresDeFesta.size], altura * 0.035f, Offset(base.x + l * u, base.y - altura * v)) }
    drawPath(caminhoEstrela(Offset(base.x, base.y - altura * 0.88f), altura * 0.09f), Color(0xFFFFC93C))
}

private fun DrawScope.ovoDePascoa(c: Offset, r: Float, i: Int) {
    val cor = coresDeFesta[i % coresDeFesta.size]
    val clara = Color(cor.red * 0.5f + 0.5f, cor.green * 0.5f + 0.5f, cor.blue * 0.5f + 0.5f)
    drawOval(Color(0x22000000), Offset(c.x - r, c.y - r * 0.15f), Size(r * 2f, r * 0.4f))
    drawOval(clara, Offset(c.x - r * 0.8f, c.y - r * 2.1f), Size(r * 1.6f, r * 2.1f))
    // Listra em zigue zague e bolinhas.
    val zig = Path().apply {
        moveTo(c.x - r * 0.78f, c.y - r * 1.05f)
        for (k in 1..6) lineTo(c.x - r * 0.78f + k * r * 0.26f, c.y - r * (if (k % 2 == 0) 1.05f else 1.3f))
    }
    drawPath(zig, cor, style = Stroke(r * 0.18f, cap = StrokeCap.Round))
    drawCircle(cor, r * 0.13f, Offset(c.x - r * 0.3f, c.y - r * 0.55f))
    drawCircle(cor, r * 0.13f, Offset(c.x + r * 0.3f, c.y - r * 0.55f))
    drawCircle(cor, r * 0.13f, Offset(c.x, c.y - r * 1.65f))
}

/** Fileira de bandeirinhas de festa junina presas num barbante de ponta a ponta. */
private fun DrawScope.bandeirinhas(yBorda: Float, yMeio: Float, deslocamento: Int) {
    val n = 11
    fun yDoFio(x: Float): Float {
        val f = x / size.width
        return yBorda + (yMeio - yBorda) * 4f * f * (1 - f)
    }
    val fio = Path().apply {
        moveTo(0f, yBorda)
        quadraticTo(size.width / 2f, 2 * yMeio - yBorda, size.width, yBorda)
    }
    drawPath(fio, Color(0xFF8A5A35), style = Stroke(size.minDimension * 0.005f))
    val larg = size.width / n
    for (i in 0 until n) {
        val x0 = i * larg + larg * 0.12f
        val x1 = x0 + larg * 0.76f
        val xm = (x0 + x1) / 2f
        val y0 = yDoFio(x0)
        val y1 = yDoFio(x1)
        val alt = larg * 0.85f
        val b = Path().apply {
            moveTo(x0, y0); lineTo(x1, y1)
            lineTo(x1, y1 + alt); lineTo(xm, (y0 + y1) / 2f + alt * 0.7f); lineTo(x0, y0 + alt)
            close()
        }
        drawPath(b, coresDeFesta[(i + deslocamento) % coresDeFesta.size])
    }
}

private fun DrawScope.cachoDeBaloes(base: Offset, r: Float, cor0: Int, balanco: Float) {
    val posicoes = listOf(-0.9f to -2.6f, 0.9f to -2.7f, 0f to -3.6f)
    posicoes.forEachIndexed { i, (u, v) ->
        val c = Offset(base.x + u * r + (balanco - 0.5f) * r * 0.2f, base.y + v * r)
        drawLine(Color(0x99FFFFFF), base, Offset(c.x, c.y + r * 1.1f), r * 0.06f)
        val cor = coresDeFesta[(cor0 + i) % coresDeFesta.size]
        drawOval(cor, Offset(c.x - r * 0.85f, c.y - r), Size(r * 1.7f, r * 2.1f))
        drawOval(Color.White.copy(alpha = 0.45f), Offset(c.x - r * 0.5f, c.y - r * 0.7f), Size(r * 0.35f, r * 0.6f))
        val no = Path().apply { moveTo(c.x - r * 0.15f, c.y + r * 1.22f); lineTo(c.x + r * 0.15f, c.y + r * 1.22f); lineTo(c.x, c.y + r * 1.05f); close() }
        drawPath(no, cor)
    }
}

private fun DrawScope.pipa(c: Offset, r: Float) {
    val losango = Path().apply { moveTo(c.x, c.y - r); lineTo(c.x + r * 0.75f, c.y); lineTo(c.x, c.y + r * 1.3f); lineTo(c.x - r * 0.75f, c.y); close() }
    drawPath(losango, Color(0xFFFF5C9A))
    drawLine(Color(0xFFFFC93C), Offset(c.x, c.y - r), Offset(c.x, c.y + r * 1.3f), r * 0.08f)
    drawLine(Color(0xFFFFC93C), Offset(c.x - r * 0.75f, c.y), Offset(c.x + r * 0.75f, c.y), r * 0.08f)
    val rabo = Path().apply {
        moveTo(c.x, c.y + r * 1.3f)
        cubicTo(c.x - r * 0.6f, c.y + r * 2f, c.x + r * 0.6f, c.y + r * 2.5f, c.x - r * 0.2f, c.y + r * 3.2f)
    }
    drawPath(rabo, Color(0xFF4D96FF), style = Stroke(r * 0.08f, cap = StrokeCap.Round))
    for (k in 1..3) drawCircle(coresDeFesta[k], r * 0.12f, Offset(c.x + (if (k % 2 == 0) r * 0.2f else -r * 0.15f), c.y + r * (1.4f + k * 0.55f)))
}

private fun DrawScope.boloDeAniversario(base: Offset, largura: Float, brilho: Float) {
    val a1 = largura * 0.32f
    val a2 = largura * 0.26f
    drawOval(Color(0x22000000), Offset(base.x - largura * 0.6f, base.y - largura * 0.06f), Size(largura * 1.2f, largura * 0.12f))
    drawRoundRect(Color(0xFFF5F0FF), Offset(base.x - largura * 0.6f, base.y - largura * 0.07f), Size(largura * 1.2f, largura * 0.07f), CornerRadius(largura * 0.03f))
    drawRoundRect(Color(0xFFFF9FC0), Offset(base.x - largura * 0.5f, base.y - largura * 0.07f - a1), Size(largura, a1), CornerRadius(largura * 0.06f))
    drawRoundRect(Color(0xFFFFE6F0), Offset(base.x - largura * 0.36f, base.y - largura * 0.07f - a1 - a2), Size(largura * 0.72f, a2), CornerRadius(largura * 0.06f))
    // Cobertura escorrendo e confeitos.
    for (k in 0..4) drawCircle(Color.White, largura * 0.07f, Offset(base.x - largura * 0.4f + k * largura * 0.2f, base.y - largura * 0.07f - a1))
    for (k in 0..5) drawCircle(coresDeFesta[k], largura * 0.025f, Offset(base.x - largura * 0.38f + k * largura * 0.15f, base.y - largura * 0.07f - a1 * 0.45f))
    // Velinhas acesas.
    for (k in -1..1) {
        val x = base.x + k * largura * 0.2f
        val topo = base.y - largura * 0.07f - a1 - a2
        drawRect(coresDeFesta[k + 2], Offset(x - largura * 0.025f, topo - largura * 0.16f), Size(largura * 0.05f, largura * 0.16f))
        drawOval(Color(0xFFFFC93C).copy(alpha = 0.7f + 0.3f * brilho), Offset(x - largura * 0.035f, topo - largura * 0.27f), Size(largura * 0.07f, largura * 0.1f))
        drawOval(Color(0xFFFF7A3D), Offset(x - largura * 0.015f, topo - largura * 0.23f), Size(largura * 0.03f, largura * 0.05f))
    }
}

/** Prateleira com os presentes dos eventos já ganhos (ficam para sempre na casa). */
@Composable
fun PrateleiraDePresentes(presentes: List<PresenteEspecial>, modifier: Modifier = Modifier) {
    if (presentes.isEmpty()) return
    Row(
        modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Cores.MadeiraClara, RoundedCornerShape(16.dp))
            .border(3.dp, Cores.Madeira, RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 2.dp)
            .semantics { contentDescription = "Presentes especiais: " + presentes.joinToString(", ") { it.nome } },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (p in presentes.takeLast(5)) Text(p.emoji, fontSize = 26.sp)
    }
}
