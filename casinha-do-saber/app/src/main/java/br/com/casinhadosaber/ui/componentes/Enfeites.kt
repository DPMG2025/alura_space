package br.com.casinhadosaber.ui.componentes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import br.com.casinhadosaber.dominio.Movel
import kotlin.math.PI

// ---------------------------------------------------------------------------------------------
// Enfeites desenhados à mão (no mesmo traço do patinho e dos móveis): gato, ursinho, violão, plantinha,
// livros, bolo, lixeira e vaso sanitário. Nada de emoji ao lado dos móveis vetoriais.
// ---------------------------------------------------------------------------------------------

private fun oval(cx: Float, cy: Float, rx: Float, ry: Float) = Path().apply { addOval(Rect(cx - rx, cy - ry, cx + rx, cy + ry)) }

private fun poligono(pontos: List<Offset>) = Path().apply {
    moveTo(pontos[0].x, pontos[0].y)
    for (i in 1 until pontos.size) lineTo(pontos[i].x, pontos[i].y)
    close()
}

private fun unir(formas: List<Path>): Path {
    var acc = formas[0]
    for (i in 1 until formas.size) acc = Path().apply { op(acc, formas[i], PathOperation.Union) }
    return acc
}

/** Uma folha pontuda da [base] até a [ponta], com a largura [w] no meio. */
private fun folha(base: Offset, ponta: Offset, w: Float): Path {
    val dx = ponta.x - base.x
    val dy = ponta.y - base.y
    val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
    val nx = -dy / len * w
    val ny = dx / len * w
    return Path().apply {
        moveTo(base.x, base.y)
        cubicTo(base.x + dx * 0.25f + nx, base.y + dy * 0.25f + ny, base.x + dx * 0.7f + nx, base.y + dy * 0.7f + ny, ponta.x, ponta.y)
        cubicTo(base.x + dx * 0.7f - nx, base.y + dy * 0.7f - ny, base.x + dx * 0.25f - nx, base.y + dy * 0.25f - ny, base.x, base.y)
        close()
    }
}

/** Cilindro em pé visto de cima em perspectiva: lateral (com a base redonda) e a tampa, centrado em [b]. */
private fun cilindro(b: Offset, r: Float, h: Float, cor: Color, corDoTopo: Color = clarear(cor, 0.2f)): List<Forma> {
    val ry = r * 0.5f
    val lateral = unir(listOf(Path().apply { addRect(Rect(b.x - r, b.y - h, b.x + r, b.y)) }, oval(b.x, b.y, r, ry)))
    return listOf(Forma(lateral, cor), Forma(oval(b.x, b.y - h, r, ry), corDoTopo))
}

/**
 * As formas de um enfeite desenhado à mão, já na tela (ou null se o [m] não é um enfeite).
 * O ponto de apoio é o centro do pé do móvel, na altura em que ele está.
 */
fun enfeiteDesenhado(m: Movel, proj: Projecao): List<Forma>? {
    val tw = proj.ladrilho
    val b = proj.ponto(m.centroX, m.centroY, m.z)
    val f = ArrayList<Forma>()
    fun sombra(rx: Float) { f += Forma(oval(b.x, b.y, rx, rx * 0.28f), Color(0x26000000), contorno = false) }
    fun liso(p: Path, c: Color) { f += Forma(p, c, contorno = false) }
    val olho = Color(0xFF2B2B35)
    when (m.id) {
        "gato" -> {
            val s = tw * 0.66f
            val laranja = Color(0xFFF4A340)
            val listra = Color(0xFFD97E22)
            sombra(s * 0.34f)
            val hx = b.x
            val hy = b.y - s * 0.62f
            // rabo enrolado para cima, atrás do corpo
            val rabo = Path().apply {
                moveTo(b.x + s * 0.18f, b.y - s * 0.04f)
                cubicTo(b.x + s * 0.56f, b.y - s * 0.02f, b.x + s * 0.62f, b.y - s * 0.32f, b.x + s * 0.46f, b.y - s * 0.52f)
                lineTo(b.x + s * 0.38f, b.y - s * 0.47f)
                cubicTo(b.x + s * 0.5f, b.y - s * 0.32f, b.x + s * 0.44f, b.y - s * 0.12f, b.x + s * 0.16f, b.y - s * 0.14f)
                close()
            }
            f += Forma(rabo, laranja)
            val orelhaE = poligono(listOf(Offset(hx - s * 0.23f, hy - s * 0.06f), Offset(hx - s * 0.21f, hy - s * 0.33f), Offset(hx - s * 0.04f, hy - s * 0.2f)))
            val orelhaD = poligono(listOf(Offset(hx + s * 0.23f, hy - s * 0.06f), Offset(hx + s * 0.21f, hy - s * 0.33f), Offset(hx + s * 0.04f, hy - s * 0.2f)))
            f += Forma(unir(listOf(oval(b.x, b.y - s * 0.24f, s * 0.29f, s * 0.26f), oval(hx, hy, s * 0.25f, s * 0.22f), orelhaE, orelhaD)), laranja)
            liso(poligono(listOf(Offset(hx - s * 0.19f, hy - s * 0.1f), Offset(hx - s * 0.185f, hy - s * 0.26f), Offset(hx - s * 0.08f, hy - s * 0.18f))), Color(0xFFFF9EB5))
            liso(poligono(listOf(Offset(hx + s * 0.19f, hy - s * 0.1f), Offset(hx + s * 0.185f, hy - s * 0.26f), Offset(hx + s * 0.08f, hy - s * 0.18f))), Color(0xFFFF9EB5))
            // listrinhas na testa e no corpo
            for (k in -1..1) liso(oval(hx + k * s * 0.07f, hy - s * 0.15f, s * 0.022f, s * 0.05f), listra)
            liso(oval(b.x - s * 0.24f, b.y - s * 0.3f, s * 0.04f, s * 0.016f), listra)
            liso(oval(b.x + s * 0.24f, b.y - s * 0.3f, s * 0.04f, s * 0.016f), listra)
            // barriga, patinhas e focinho claros
            liso(oval(b.x, b.y - s * 0.2f, s * 0.15f, s * 0.17f), Color(0xFFFFF1DC))
            f += Forma(oval(b.x - s * 0.11f, b.y - s * 0.03f, s * 0.075f, s * 0.045f), Color(0xFFFFF1DC))
            f += Forma(oval(b.x + s * 0.11f, b.y - s * 0.03f, s * 0.075f, s * 0.045f), Color(0xFFFFF1DC))
            liso(oval(hx - s * 0.05f, hy + s * 0.07f, s * 0.06f, s * 0.045f), Color(0xFFFFF1DC))
            liso(oval(hx + s * 0.05f, hy + s * 0.07f, s * 0.06f, s * 0.045f), Color(0xFFFFF1DC))
            liso(poligono(listOf(Offset(hx - s * 0.03f, hy + s * 0.025f), Offset(hx + s * 0.03f, hy + s * 0.025f), Offset(hx, hy + s * 0.06f))), Color(0xFFFF6F91))
            // olhinhos com brilho e bochechas
            for (lado in listOf(-1f, 1f)) {
                liso(oval(hx + lado * s * 0.095f, hy - s * 0.02f, s * 0.03f, s * 0.04f), olho)
                liso(oval(hx + lado * s * 0.095f - s * 0.008f, hy - s * 0.035f, s * 0.011f, s * 0.011f), Color.White)
                liso(oval(hx + lado * s * 0.16f, hy + s * 0.06f, s * 0.035f, s * 0.022f), Color(0x55FF6F91))
                // bigodes
                liso(Path().apply { addRect(Rect(hx + lado * s * 0.12f - s * 0.07f, hy + s * 0.07f, hx + lado * s * 0.12f + s * 0.07f, hy + s * 0.077f)) }, Color(0x99FFFFFF))
            }
        }
        "ursinho" -> {
            val s = tw * 0.64f
            val marrom = Color(0xFFB07443)
            val claro = Color(0xFFE6BE91)
            sombra(s * 0.34f)
            val hx = b.x
            val hy = b.y - s * 0.64f
            val corpo = unir(
                listOf(
                    oval(b.x, b.y - s * 0.28f, s * 0.25f, s * 0.25f),
                    oval(b.x - s * 0.17f, b.y - s * 0.08f, s * 0.12f, s * 0.09f),
                    oval(b.x + s * 0.17f, b.y - s * 0.08f, s * 0.12f, s * 0.09f),
                    oval(b.x - s * 0.25f, b.y - s * 0.3f, s * 0.08f, s * 0.13f),
                    oval(b.x + s * 0.25f, b.y - s * 0.3f, s * 0.08f, s * 0.13f),
                    oval(hx - s * 0.17f, hy - s * 0.16f, s * 0.085f, s * 0.085f),
                    oval(hx + s * 0.17f, hy - s * 0.16f, s * 0.085f, s * 0.085f),
                    oval(hx, hy, s * 0.22f, s * 0.2f),
                ),
            )
            f += Forma(corpo, marrom)
            liso(oval(hx - s * 0.17f, hy - s * 0.16f, s * 0.045f, s * 0.045f), claro)
            liso(oval(hx + s * 0.17f, hy - s * 0.16f, s * 0.045f, s * 0.045f), claro)
            liso(oval(b.x, b.y - s * 0.26f, s * 0.14f, s * 0.15f), claro)
            liso(oval(b.x - s * 0.19f, b.y - s * 0.07f, s * 0.055f, s * 0.05f), claro)
            liso(oval(b.x + s * 0.19f, b.y - s * 0.07f, s * 0.055f, s * 0.05f), claro)
            liso(oval(hx, hy + s * 0.07f, s * 0.1f, s * 0.075f), claro)
            liso(oval(hx, hy + s * 0.045f, s * 0.04f, s * 0.028f), Color(0xFF4A2C1A))
            for (lado in listOf(-1f, 1f)) {
                liso(oval(hx + lado * s * 0.085f, hy - s * 0.03f, s * 0.028f, s * 0.033f), olho)
                liso(oval(hx + lado * s * 0.085f - s * 0.008f, hy - s * 0.043f, s * 0.01f, s * 0.01f), Color.White)
            }
            // lacinho vermelho no pescoço
            val ly = b.y - s * 0.47f
            f += Forma(poligono(listOf(Offset(b.x, ly), Offset(b.x - s * 0.13f, ly - s * 0.06f), Offset(b.x - s * 0.13f, ly + s * 0.06f))), Color(0xFFEF476F))
            f += Forma(poligono(listOf(Offset(b.x, ly), Offset(b.x + s * 0.13f, ly - s * 0.06f), Offset(b.x + s * 0.13f, ly + s * 0.06f))), Color(0xFFEF476F))
            liso(oval(b.x, ly, s * 0.035f, s * 0.035f), Color(0xFFC72C55))
        }
        "violao" -> {
            val s = tw * 1.0f
            sombra(s * 0.24f)
            val ang = Math.toRadians(-14.0)
            val co = kotlin.math.cos(ang).toFloat()
            val se = kotlin.math.sin(ang).toFloat()
            // (dx, dy) em relação à base, inclinado como se estivesse encostado na parede
            fun r(dx: Float, dy: Float) = Offset(b.x + (dx * co - dy * se) * s, b.y - s * 0.02f + (dx * se + dy * co) * s)
            fun bola(dx: Float, dy: Float, raio: Float) = r(dx, dy).let { c -> oval(c.x, c.y, raio * s, raio * s) }
            fun faixa(x0: Float, y0: Float, x1: Float, y1: Float, l0: Float, l1: Float) =
                poligono(listOf(r(x0 - l0, y0), r(x0 + l0, y0), r(x1 + l1, y1), r(x1 - l1, y1)))
            f += Forma(faixa(0f, -0.5f, 0f, -0.96f, 0.036f, 0.03f), Color(0xFF7A4A2A))
            f += Forma(faixa(0f, -0.95f, 0f, -1.1f, 0.055f, 0.045f), Color(0xFF5A3520))
            for (k in 0..2) {
                liso(bola(-0.065f, -0.98f - k * 0.045f, 0.016f), Color(0xFFE8E2D0))
                liso(bola(0.065f, -0.98f - k * 0.045f, 0.016f), Color(0xFFE8E2D0))
            }
            f += Forma(unir(listOf(bola(0f, -0.21f, 0.215f), bola(0f, -0.47f, 0.165f))), Color(0xFFA85A22))
            liso(unir(listOf(bola(0f, -0.21f, 0.19f), bola(0f, -0.47f, 0.14f))), Color(0xFFE6913F))
            liso(bola(-0.07f, -0.27f, 0.07f), Color(0x33FFFFFF))
            liso(bola(0f, -0.42f, 0.062f), Color(0xFF3B2414))
            f += Forma(faixa(0f, -0.15f, 0f, -0.19f, 0.08f, 0.08f), Color(0xFF5A3520))
            for (k in -1..1) liso(faixa(k * 0.016f, -0.17f, k * 0.014f, -0.98f, 0.003f, 0.003f), Color(0xCCFFFFFF))
        }
        "planta" -> {
            val s = tw * 0.82f
            sombra(s * 0.26f)
            val topo = b.y - s * 0.36f
            val verdes = listOf(Color(0xFF3E9B3A), Color(0xFF55B84F), Color(0xFF2F8A33))
            // folhas de trás, depois o vaso de barro, depois as folhas da frente
            val atras = listOf(-120f, -95f, -70f, -55f, -135f)
            val frente = listOf(-160f, -20f, -110f, -80f, -40f, -145f)
            fun folhaEm(grau: Float, comp: Float, i: Int) {
                val a = Math.toRadians(grau.toDouble())
                val base = Offset(b.x, topo + s * 0.02f)
                val ponta = Offset(base.x + kotlin.math.cos(a).toFloat() * comp * s, base.y + kotlin.math.sin(a).toFloat() * comp * s)
                f += Forma(folha(base, ponta, s * 0.085f), verdes[i % verdes.size])
                liso(folha(base, Offset(base.x + (ponta.x - base.x) * 0.85f, base.y + (ponta.y - base.y) * 0.85f), s * 0.008f), Color(0x66FFFFFF))
            }
            atras.forEachIndexed { i, g -> folhaEm(g, 0.62f, i) }
            val vaso = poligono(listOf(Offset(b.x - s * 0.24f, topo), Offset(b.x + s * 0.24f, topo), Offset(b.x + s * 0.17f, b.y), Offset(b.x - s * 0.17f, b.y)))
            f += Forma(unir(listOf(vaso, oval(b.x, b.y, s * 0.17f, s * 0.05f))), Color(0xFFE07A4F))
            f += Forma(Path().apply { addRect(Rect(b.x - s * 0.27f, topo - s * 0.02f, b.x + s * 0.27f, topo + s * 0.07f)) }, Color(0xFFC8623A))
            liso(oval(b.x, topo - s * 0.02f, s * 0.24f, s * 0.045f), Color(0xFF6B4226))
            liso(Path().apply { addRect(Rect(b.x - s * 0.15f, topo + s * 0.12f, b.x - s * 0.1f, b.y - s * 0.05f)) }, Color(0x33FFFFFF))
            frente.forEachIndexed { i, g -> folhaEm(g, if (i < 2) 0.42f else 0.5f, i + 1) }
        }
        "livros" -> with(proj) {
            val cores = listOf(Color(0xFFE85D5D), Color(0xFF5DBB63), Color(0xFF4D96FF), Color(0xFFFFC93C))
            val recuo = listOf(0f, 0.05f, 0.02f, 0.08f)
            val altura = listOf(0.12f, 0.1f, 0.12f, 0.09f)
            var z = 0f
            sombra(tw * 0.24f)
            for (i in cores.indices) {
                val x0 = m.x0 + 0.02f + recuo[i]
                val x1 = m.x1 - 0.02f - recuo[(i + 2) % 4] * 0.5f
                val y0 = m.y0 + 0.03f + recuo[(i + 1) % 4]
                val y1 = m.y1 - 0.03f - recuo[i] * 0.5f
                val z1 = z + altura[i]
                f += caixa(x0, y0, x1, y1, z, z1, cores[i])
                // as folhas brancas aparecem na frente de cada livro
                liso(paredeY(y1 + 0.001f, x0 + 0.03f, x1 - 0.02f, z + 0.022f, z1 - 0.022f), Color(0xFFFFF8EA))
                z = z1
            }
        }
        "bolo" -> {
            val r = tw * 0.2f
            val h1 = tw * 0.15f
            f += Forma(oval(b.x, b.y, r * 1.35f, r * 0.62f), Color.White)
            f += cilindro(b, r, h1, Color(0xFFFFB3C8), Color(0xFFFFF5F8))
            // cobertura escorrendo na frente
            for (k in 0..6) {
                val a = PI * (0.08 + k * 0.14)
                liso(oval(b.x + r * kotlin.math.cos(a).toFloat(), b.y - h1 + r * 0.5f * kotlin.math.sin(a).toFloat() + r * 0.06f, r * 0.12f, r * 0.15f), Color(0xFFFFF5F8))
            }
            val b2 = Offset(b.x, b.y - h1)
            val r2 = r * 0.62f
            val h2 = tw * 0.11f
            f += cilindro(b2, r2, h2, Color(0xFFFFB3C8), Color(0xFFFFF5F8))
            for (k in 0..4) {
                val a = PI * (0.1 + k * 0.2)
                liso(oval(b2.x + r2 * kotlin.math.cos(a).toFloat(), b2.y - h2 + r2 * 0.5f * kotlin.math.sin(a).toFloat() + r2 * 0.06f, r2 * 0.13f, r2 * 0.17f), Color(0xFFFFF5F8))
            }
            // morangos na borda de baixo e a velinha no meio
            for (k in 0..4) {
                val a = PI * (1.1 + k * 0.2)
                liso(oval(b.x + r * 0.82f * kotlin.math.cos(a).toFloat(), b.y - h1 + r * 0.41f * kotlin.math.sin(a).toFloat() - r * 0.06f, r * 0.08f, r * 0.08f), Color(0xFFE63946))
            }
            val topo = b2.y - h2
            f += Forma(Path().apply { addRect(Rect(b.x - r * 0.06f, topo - r * 0.55f, b.x + r * 0.06f, topo)) }, Color(0xFF8FD3FF))
            liso(Path().apply { addRect(Rect(b.x - r * 0.06f, topo - r * 0.38f, b.x + r * 0.06f, topo - r * 0.3f)) }, Color(0xFFFF8FB1))
            liso(folha(Offset(b.x, topo - r * 0.55f), Offset(b.x, topo - r * 0.9f), r * 0.09f), Color(0xFFFFA62B))
            liso(folha(Offset(b.x, topo - r * 0.58f), Offset(b.x, topo - r * 0.78f), r * 0.04f), Color(0xFFFFE066))
        }
        "lixeira" -> {
            val r = tw * 0.19f
            val h = 0.5f * proj.unidadeZ
            sombra(r * 1.1f)
            f += cilindro(b, r * 0.92f, h, Color(0xFF7FB4C8), Color(0xFF5E93A8))
            for (k in -2..2) liso(Path().apply { addRect(Rect(b.x + k * r * 0.34f - r * 0.025f, b.y - h * 0.86f, b.x + k * r * 0.34f + r * 0.025f, b.y - h * 0.08f)) }, Color(0x334A7A8C))
            f += cilindro(Offset(b.x, b.y - h), r, r * 0.12f, Color(0xFF5E93A8), Color(0xFF8CC3D6))
            f += Forma(oval(b.x, b.y - h - r * 0.2f, r * 0.22f, r * 0.1f), Color(0xFF4A7A8C))
        }
        "vaso" -> with(proj) {
            val cx = m.centroX
            // caixa da descarga encostada na parede, pé do vaso e o assento redondo
            f += caixa(m.x0 + 0.06f, m.y0, m.x1 - 0.06f, m.y0 + 0.17f, 0.32f, 0.74f, Color(0xFFF7F9FB))
            liso(oval(proj.x(cx, m.y0 + 0.09f), proj.y(cx, m.y0 + 0.09f, 0.74f), tw * 0.035f, tw * 0.018f), Color(0xFFB8C4CC))
            f += caixa(cx - 0.11f, m.y0 + 0.2f, cx + 0.11f, m.y0 + 0.45f, 0f, 0.28f, Color(0xFFF7F9FB))
            val assento = proj.ponto(cx, m.y0 + 0.36f, 0.3f)
            val rx = tw * 0.19f
            f += Forma(unir(listOf(oval(assento.x, assento.y, rx, rx * 0.55f), Path().apply { addRect(Rect(assento.x - rx, assento.y - 0f, assento.x + rx, assento.y + tw * 0.06f)) }, oval(assento.x, assento.y + tw * 0.06f, rx, rx * 0.55f))), Color(0xFFE9EEF2))
            f += Forma(oval(assento.x, assento.y, rx, rx * 0.55f), Color.White)
            liso(oval(assento.x, assento.y + rx * 0.04f, rx * 0.66f, rx * 0.34f), Color(0xFFBFE6F7))
        }
        else -> return null
    }
    return f
}
