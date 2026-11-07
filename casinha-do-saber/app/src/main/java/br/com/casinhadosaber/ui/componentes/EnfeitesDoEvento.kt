package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.PresenteEspecial
import br.com.casinhadosaber.dominio.TipoDeEvento
import br.com.casinhadosaber.ui.tema.Cores

// Enfeites dos eventos do ano (sugestão 4): pisca pisca e árvore no Natal, ovinhos na Páscoa, bandeirinhas na
// Festa Junina, balões no Dia das Crianças e festa com bolo no aniversário. Ficam no FUNDO da cena (atrás das
// coisas, da criança e dos botões) e, na casa, seguem o beiral do telhado: não cobrem o espelho, a porta nem o sol.

private val coresDeFesta = listOf(Color(0xFFFF5C9A), Color(0xFFFFC93C), Color(0xFF4D96FF), Color(0xFF3DBE6B), Color(0xFFFF7A3D), Color(0xFF9B6BFF))

/**
 * Onde fica o telhado da casa, para os enfeites seguirem o beiral. Pontos em frações da largura (x) e da
 * altura da cena da casa (y); [alturaDaCena] é quanto da altura do desenho a cena ocupa (o resto é o jardim
 * de baixo) e [cerca] é a altura (fração da cena) onde os balões ficam amarrados.
 */
class BeiralDaCasa(val esquerda: Offset, val cume: Offset, val direita: Offset, val cerca: Float, val alturaDaCena: Float = 1f)

/**
 * Os enfeites do [tipo] no fundo da cena. Com [beiral] (a casa de fora), as bandeirinhas e o pisca pisca
 * seguem o telhado; sem ele (as áreas novas), ficam num fio baixinho no alto. A parte parada fica numa
 * camada própria (desenhada uma vez); só as luzinhas e os balões mexem a cada quadro.
 */
@Composable
fun EnfeitesDoEvento(tipo: TipoDeEvento, modifier: Modifier = Modifier, beiral: BeiralDaCasa? = null) {
    val transicao = rememberInfiniteTransition(label = "enfeites")
    val brilho = transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "brilho")
    Box(modifier) {
        Spacer(Modifier.fillMaxSize().graphicsLayer().drawBehind { enfeitesParados(tipo, beiral) })
        // Camada própria também para o que mexe: o brilho redesenha só esta camada, e não a cena em volta.
        Spacer(Modifier.fillMaxSize().graphicsLayer().drawBehind { enfeitesQueMexem(tipo, beiral, brilho.value) })
    }
}

/** Os enfeites do [tipo] ocupando a área da cena. Prefira [EnfeitesDoEvento] atrás das coisas da cena. */
@Composable
fun DecoracaoDoEvento(tipo: TipoDeEvento, modifier: Modifier = Modifier) {
    EnfeitesDoEvento(tipo, modifier.fillMaxSize())
}

/** Pontos do telhado em pixels: esquerda, cume e direita, um tiquinho abaixo da borda (o fio fica "pregado" no beiral). */
private class Telhado(val a: Offset, val c: Offset, val b: Offset, val unidade: Float)

private fun DrawScope.telhadoDe(beiral: BeiralDaCasa): Telhado {
    val alturaCena = size.height * beiral.alturaDaCena
    val abaixo = alturaCena * 0.012f
    fun pt(o: Offset) = Offset(o.x * size.width, o.y * alturaCena + abaixo)
    return Telhado(pt(beiral.esquerda), pt(beiral.cume), pt(beiral.direita), size.width)
}

private fun lerp(a: Offset, b: Offset, f: Float) = Offset(a.x + (b.x - a.x) * f, a.y + (b.y - a.y) * f)

/** Bandeirinhas presas num fio reto de [de] até [ate], penduradas na vertical. */
private fun DrawScope.bandeirinhasNoFio(de: Offset, ate: Offset, largura: Float, deslocamento: Int) {
    drawLine(Color(0xFF8A5A35), de, ate, largura * 0.06f, StrokeCap.Round)
    val comprimento = (ate - de).getDistance()
    val n = (comprimento / (largura * 1.25f)).toInt().coerceAtLeast(2)
    val alt = largura * 0.95f
    for (i in 0 until n) {
        val p0 = lerp(de, ate, (i + 0.14f) / n)
        val p1 = lerp(de, ate, (i + 0.86f) / n)
        val meio = lerp(p0, p1, 0.5f)
        val b = Path().apply {
            moveTo(p0.x, p0.y); lineTo(p1.x, p1.y)
            lineTo(p1.x, p1.y + alt); lineTo(meio.x, meio.y + alt * 0.68f); lineTo(p0.x, p0.y + alt)
            close()
        }
        drawPath(b, coresDeFesta[(i + deslocamento) % coresDeFesta.size])
        // Dobrinha clara no alto da bandeira e contorno fininho, para ler bem em cima do telhado.
        drawLine(Color.White.copy(alpha = 0.45f), Offset(p0.x, p0.y + alt * 0.12f), Offset(p1.x, p1.y + alt * 0.12f), largura * 0.06f)
        drawPath(b, Color(0x33000000), style = Stroke(largura * 0.035f))
    }
}

/** Pontos onde as lâmpadas do pisca pisca ficam, ao longo do fio (com uma barriguinha entre os pregos). */
private inline fun pontosDoFio(de: Offset, ate: Offset, porTrecho: Int, trechos: Int, barriga: Float, cada: (Offset, Int) -> Unit) {
    var k = 0
    for (t in 0 until trechos) {
        val a = lerp(de, ate, t / trechos.toFloat())
        val b = lerp(de, ate, (t + 1) / trechos.toFloat())
        for (i in 1..porTrecho) {
            val f = i / (porTrecho + 1f)
            val p = lerp(a, b, f)
            cada(Offset(p.x, p.y + barriga * 4f * f * (1 - f)), k++)
        }
    }
}

private fun DrawScope.fioDoPiscaPisca(de: Offset, ate: Offset, trechos: Int, barriga: Float, largura: Float) {
    val fio = Path().apply {
        moveTo(de.x, de.y)
        for (t in 0 until trechos) {
            val a = lerp(de, ate, t / trechos.toFloat())
            val b = lerp(de, ate, (t + 1) / trechos.toFloat())
            val m = lerp(a, b, 0.5f)
            quadraticTo(m.x, m.y + barriga * 2f, b.x, b.y)
        }
    }
    drawPath(fio, Color(0xFF2E5E3A), style = Stroke(largura, cap = StrokeCap.Round))
}

/** Os trechos do pisca pisca da casa: as duas águas do telhado e o beiral de baixo. */
private inline fun Telhado.cadaTrechoDeLuz(cada: (de: Offset, ate: Offset, trechos: Int) -> Unit) {
    cada(a, c, 4)
    cada(c, b, 4)
    cada(Offset(a.x + unidade * 0.06f, a.y), Offset(b.x - unidade * 0.06f, b.y), 6)
}

private inline fun DrawScope.pontosDeLuzDaCasa(telhado: Telhado, cada: (Offset, Int) -> Unit) {
    var base = 0
    val barriga = size.width * 0.012f
    telhado.cadaTrechoDeLuz { de, ate, trechos ->
        pontosDoFio(de, ate, 2, trechos, barriga) { p, k -> cada(p, base + k) }
        base += trechos * 2
    }
}

private inline fun DrawScope.piscaPiscaNoAlto(cada: (Offset, Int) -> Unit) {
    val y = size.height * 0.07f
    pontosDoFio(Offset(0f, y), Offset(size.width, y), 4, 3, size.height * 0.012f, cada)
}

private fun DrawScope.soqueteELampada(p: Offset, i: Int, r: Float) {
    val cor = coresDeFesta[i % coresDeFesta.size]
    drawRect(Color(0xFF5B5B5B), Offset(p.x - r * 0.45f, p.y - r * 0.1f), Size(r * 0.9f, r * 0.6f))
    drawOval(cor, Offset(p.x - r * 0.75f, p.y + r * 0.4f), Size(r * 1.5f, r * 2.1f))
    drawOval(Color.White.copy(alpha = 0.55f), Offset(p.x - r * 0.4f, p.y + r * 0.65f), Size(r * 0.4f, r * 0.7f))
}

private fun DrawScope.brilhoDaLampada(p: Offset, i: Int, r: Float, brilho: Float) {
    val acesa = if (i % 2 == 0) brilho else 1f - brilho
    val cor = coresDeFesta[i % coresDeFesta.size]
    val c = Offset(p.x, p.y + r * 1.45f)
    drawCircle(cor.copy(alpha = 0.10f + 0.22f * acesa), r * 2.3f, c)
    drawCircle(Color.White.copy(alpha = 0.35f * acesa), r * 0.45f, c)
}

private fun DrawScope.enfeitesParados(tipo: TipoDeEvento, beiral: BeiralDaCasa?) {
    val telhado = beiral?.let { telhadoDe(it) }
    val m = size.minDimension
    val pe = size.height * 0.985f
    when (tipo) {
        TipoDeEvento.NATAL -> {
            if (telhado != null) {
                telhado.cadaTrechoDeLuz { de, ate, trechos -> fioDoPiscaPisca(de, ate, trechos, size.width * 0.012f, size.width * 0.005f) }
                pontosDeLuzDaCasa(telhado) { p, k -> soqueteELampada(p, k, size.width * 0.011f) }
            } else {
                val y = size.height * 0.07f
                fioDoPiscaPisca(Offset(0f, y), Offset(size.width, y), 3, size.height * 0.012f, m * 0.005f)
                piscaPiscaNoAlto { p, k -> soqueteELampada(p, k, m * 0.014f) }
            }
            arvoreDeNatal(Offset(size.width * 0.9f, pe), minOf(m * 0.26f, size.height * 0.3f))
        }
        TipoDeEvento.PASCOA -> {
            val ovos = listOf(0.25f to 0.975f, 0.37f to 0.99f, 0.48f to 0.97f, 0.6f to 0.99f, 0.94f to 0.975f)
            ovos.forEachIndexed { i, (u, v) -> ovoDePascoa(Offset(size.width * u, size.height * v), m * 0.045f, i) }
        }
        TipoDeEvento.FESTA_JUNINA -> {
            if (telhado != null) {
                val larg = size.width * 0.045f
                bandeirinhasNoFio(telhado.a, telhado.c, larg, 0)
                bandeirinhasNoFio(telhado.c, telhado.b, larg, 3)
                bandeirinhasNoFio(Offset(telhado.a.x + larg, telhado.a.y), Offset(telhado.b.x - larg, telhado.b.y), larg * 0.7f, 1)
            } else {
                val y = size.height * 0.07f
                bandeirinhasNoFio(Offset(0f, y), Offset(size.width, y), size.width * 0.04f, 0)
            }
        }
        TipoDeEvento.DIA_DAS_CRIANCAS -> {
            if (telhado == null) pipa(Offset(size.width * 0.78f, size.height * 0.1f), m * 0.06f)
        }
        TipoDeEvento.ANIVERSARIO -> {
            if (telhado != null) {
                val larg = size.width * 0.045f
                bandeirinhasNoFio(telhado.a, telhado.c, larg, 1)
                bandeirinhasNoFio(telhado.c, telhado.b, larg, 4)
            } else {
                val y = size.height * 0.07f
                bandeirinhasNoFio(Offset(0f, y), Offset(size.width, y), size.width * 0.04f, 1)
            }
            boloDeAniversario(Offset(size.width * 0.86f, size.height * 0.97f), m * 0.18f, 1f, chamas = false)
        }
    }
}

private fun DrawScope.enfeitesQueMexem(tipo: TipoDeEvento, beiral: BeiralDaCasa?, brilho: Float) {
    val telhado = beiral?.let { telhadoDe(it) }
    val m = size.minDimension
    when (tipo) {
        TipoDeEvento.NATAL -> {
            if (telhado != null) {
                pontosDeLuzDaCasa(telhado) { p, k -> brilhoDaLampada(p, k, size.width * 0.011f, brilho) }
            } else {
                piscaPiscaNoAlto { p, k -> brilhoDaLampada(p, k, m * 0.014f, brilho) }
            }
        }
        TipoDeEvento.DIA_DAS_CRIANCAS, TipoDeEvento.ANIVERSARIO -> {
            val r = m * 0.05f
            val cor0 = if (tipo == TipoDeEvento.ANIVERSARIO) 1 else 0
            if (beiral != null) {
                // Amarrados na cerca, dos dois lados da casa (longe do sol, da porta e do espelho).
                val y = size.height * beiral.alturaDaCena * beiral.cerca
                cachoDeBaloes(Offset(size.width * 0.045f, y), r, cor0, brilho)
                cachoDeBaloes(Offset(size.width * 0.955f, y), r, cor0 + 2, 1f - brilho)
            } else {
                cachoDeBaloes(Offset(size.width * 0.1f, size.height * 0.4f), r, cor0, brilho)
                cachoDeBaloes(Offset(size.width * 0.9f, size.height * 0.38f), r, cor0 + 2, 1f - brilho)
            }
            if (tipo == TipoDeEvento.ANIVERSARIO) chamasDasVelas(Offset(size.width * 0.86f, size.height * 0.97f), m * 0.18f, brilho)
        }
        else -> Unit
    }
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
    drawOval(Color(0x2A000000), Offset(base.x - l * 0.45f, base.y - altura * 0.035f), Size(l * 0.9f, altura * 0.07f))
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
        drawOval(cor, Offset(c.x - r * 0.14f, c.y + r * 1.02f), Size(r * 0.28f, r * 0.2f))
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

private fun DrawScope.boloDeAniversario(base: Offset, largura: Float, brilho: Float, chamas: Boolean = true) {
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
    }
    if (chamas) chamasDasVelas(base, largura, brilho)
}

/** As chaminhas das velas do bolo (a parte que tremula). */
private fun DrawScope.chamasDasVelas(base: Offset, largura: Float, brilho: Float) {
    val topo = base.y - largura * 0.07f - largura * 0.32f - largura * 0.26f
    for (k in -1..1) {
        val x = base.x + k * largura * 0.2f
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
        // A figura 3D do presente quando existe (o emoji fica de reserva).
        for (p in presentes.takeLast(5)) FiguraDaPergunta(p.emoji, 32.dp) { Text(p.emoji, fontSize = 26.sp) }
    }
}
