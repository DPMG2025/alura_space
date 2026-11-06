package br.com.casinhadosaber.ui.componentes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp

/*
 * Rosto das crianças desenhadas (a criança do jogo e o irmãozinho): olhos grandes e brilhantes, sobrancelhas
 * que mudam com o humor, boca aberta de riso, narizinho e bochechas rosadas. Tudo é medido pelo raio [r] da
 * cabeça e pelo [centro] dela, então serve para qualquer tamanho de cabeça.
 *
 * Os olhos ficam sempre em centro ± 0,36 r na horizontal e 0,12 r abaixo do centro: os óculos, a máscara e o
 * tapa-olho do guarda-roupa contam com esse lugar.
 */

/** Cor dos traços do rosto (boca, nariz, cílios): marrom avermelhado, mais macio que o preto. */
private val TracoDoRosto = Color(0xFF5B2A22)

/** Luz e sombra na cabeça: brilho em cima à esquerda e um tom mais quente embaixo, para a cabeça ter volume. */
fun DrawScope.sombrearCabeca(centro: Offset, r: Float) {
    drawCircle(
        Brush.radialGradient(
            0f to Color(0x38FFFFFF), 0.55f to Color(0x00FFFFFF),
            center = Offset(centro.x - r * 0.35f, centro.y - r * 0.4f), radius = r * 1.1f,
        ),
        r, centro,
    )
    drawCircle(
        Brush.radialGradient(
            0.62f to Color(0x00B25A3C), 1f to Color(0x24B25A3C),
            center = Offset(centro.x - r * 0.15f, centro.y - r * 0.2f), radius = r * 1.25f,
        ),
        r, centro,
    )
}

/** Bochechas rosadas que se desfazem na pele (mais fortes quando a criança está torcendo). */
fun DrawScope.desenharBochechas(centro: Offset, r: Float, humor: Humor) {
    val forca = if (humor == Humor.TORCENDO) 0x8C else 0x70
    for (lado in listOf(-1f, 1f)) {
        val c = Offset(centro.x + lado * r * 0.6f, centro.y + r * 0.42f)
        drawOval(
            Brush.radialGradient(
                0f to Color(0xFF6E82).copy(alpha = forca / 255f), 1f to Color(0x00FF6E82),
                center = c, radius = r * 0.2f,
            ),
            topLeft = Offset(c.x - r * 0.22f, c.y - r * 0.15f), size = Size(r * 0.44f, r * 0.3f),
        )
    }
}

/** Narizinho: um arquinho curto, só uma sugestão. */
fun DrawScope.desenharNariz(centro: Offset, r: Float, pele: Color) {
    val cor = lerp(pele, Color(0xFF7A3B2E), 0.45f)
    drawArc(
        cor, startAngle = 20f, sweepAngle = 140f, useCenter = false,
        topLeft = Offset(centro.x - r * 0.06f, centro.y + r * 0.21f), size = Size(r * 0.12f, r * 0.08f),
        style = Stroke(r * 0.045f, cap = StrokeCap.Round),
    )
}

/**
 * Olhos e sobrancelhas. Abertos: grandes, escuros embaixo e mais claros em cima, com um brilho grande e um
 * pequeno. Torcendo: fechadinhos de alegria (^ ^). Piscando: um arco para baixo. Pensando: olhando para cima,
 * com uma sobrancelha levantada. [cilios] põe dois cílios no canto de fora (menina).
 */
fun DrawScope.desenharOlhos(
    centro: Offset,
    r: Float,
    olhosFechados: Boolean,
    humor: Humor,
    cilios: Boolean,
    corDaSobrancelha: Color,
    sobrancelhas: Boolean = true,
) {
    val escuro = Color(0xFF2B1A14)
    for (lado in listOf(-1f, 1f)) {
        val c = Offset(centro.x + lado * r * 0.36f, centro.y + r * 0.12f)
        val largura = r * 0.3f
        val altura = r * 0.38f
        when {
            humor == Humor.TORCENDO -> {
                // ^ ^ : olhinhos fechados de tanta alegria.
                val p = Path().apply {
                    moveTo(c.x - largura * 0.6f, c.y + altura * 0.12f)
                    quadraticTo(c.x, c.y - altura * 0.55f, c.x + largura * 0.6f, c.y + altura * 0.12f)
                }
                drawPath(p, escuro, style = Stroke(r * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            olhosFechados -> {
                val p = Path().apply {
                    moveTo(c.x - largura * 0.55f, c.y)
                    quadraticTo(c.x, c.y + altura * 0.35f, c.x + largura * 0.55f, c.y)
                }
                drawPath(p, escuro, style = Stroke(r * 0.065f, cap = StrokeCap.Round))
            }
            else -> {
                val olhar = if (humor == Humor.PENSANDO) Offset(r * 0.03f, -r * 0.05f) else Offset.Zero
                val topo = Offset(c.x - largura / 2f, c.y - altura / 2f)
                drawOval(
                    Brush.verticalGradient(
                        0f to escuro, 0.55f to Color(0xFF3C2418), 1f to Color(0xFF7A4A2E),
                        startY = topo.y, endY = topo.y + altura,
                    ),
                    topLeft = topo, size = Size(largura, altura),
                )
                // brilho grande (em cima, do lado da luz) e brilho pequeno (embaixo, do outro lado)
                drawCircle(Color.White, r * 0.072f, Offset(c.x + r * 0.035f, c.y - r * 0.07f) + olhar)
                drawCircle(Color(0xD9FFFFFF), r * 0.033f, Offset(c.x - r * 0.055f, c.y + r * 0.08f) + olhar)
                // Pálpebra de cima: um traço que acompanha o alto do olho e dá contorno ao olhar.
                drawArc(
                    escuro, startAngle = 200f, sweepAngle = 140f, useCenter = false,
                    topLeft = Offset(topo.x - r * 0.01f, topo.y - r * 0.005f), size = Size(largura + r * 0.02f, altura),
                    style = Stroke(r * 0.045f, cap = StrokeCap.Round),
                )
                if (cilios) {
                    // Um cílio só, curvinho, no canto de fora.
                    val base = Offset(c.x + lado * largura * 0.44f, c.y - altura * 0.28f)
                    val p = Path().apply {
                        moveTo(base.x, base.y)
                        quadraticTo(base.x + lado * r * 0.08f, base.y - r * 0.01f, base.x + lado * r * 0.11f, base.y - r * 0.09f)
                    }
                    drawPath(p, escuro, style = Stroke(r * 0.04f, cap = StrokeCap.Round))
                }
            }
        }
        if (!sobrancelhas) continue
        // Sobrancelhas: arquinhos acima dos olhos. Surpreso levanta as duas; pensando levanta só uma.
        val sobe = when {
            humor == Humor.SURPRESO -> r * 0.06f
            humor == Humor.PENSANDO && lado < 0 -> r * 0.07f
            humor == Humor.TORCENDO -> r * 0.04f
            else -> 0f
        }
        val inclinacao = if (humor == Humor.PENSANDO && lado > 0) r * 0.03f else 0f
        val y = c.y - r * 0.32f - sobe
        val p = Path().apply {
            moveTo(c.x - r * 0.12f, y + r * 0.03f - inclinacao * lado)
            quadraticTo(c.x, y - r * 0.04f, c.x + r * 0.12f, y + r * 0.03f + inclinacao * lado)
        }
        drawPath(p, corDaSobrancelha, style = Stroke(r * 0.06f, cap = StrokeCap.Round))
    }
}

/**
 * Boca. Feliz: sorriso aberto com a linguinha. Torcendo: bocão de "eba!". Surpreso (e falando): um "o".
 * Pensando: boquinha de lado.
 */
fun DrawScope.desenharBoca(centro: Offset, r: Float, humor: Humor) {
    val boca = Color(0xFF8E2F2A)
    val lingua = Color(0xFFFF8A8A)
    val y = centro.y + r * 0.36f
    when (humor) {
        Humor.FELIZ, Humor.TORCENDO -> {
            val meia = if (humor == Humor.TORCENDO) r * 0.22f else r * 0.17f
            val fundo = if (humor == Humor.TORCENDO) r * 0.3f else r * 0.2f
            val forma = Path().apply {
                moveTo(centro.x - meia, y)
                quadraticTo(centro.x, y + r * 0.04f, centro.x + meia, y)
                cubicTo(centro.x + meia * 0.9f, y + fundo, centro.x - meia * 0.9f, y + fundo, centro.x - meia, y)
                close()
            }
            drawPath(forma, boca)
            clipPath(forma) {
                drawOval(lingua, Offset(centro.x - meia * 0.7f, y + fundo * 0.5f), Size(meia * 1.4f, fundo * 0.9f))
                // dentinhos de cima
                drawRect(Color(0xF2FFFFFF), Offset(centro.x - meia * 0.75f, y - r * 0.02f), Size(meia * 1.5f, fundo * 0.2f))
            }
            drawPath(forma, TracoDoRosto, style = Stroke(r * 0.035f, join = StrokeJoin.Round))
        }
        Humor.SURPRESO -> {
            val topo = Offset(centro.x - r * 0.085f, y - r * 0.01f)
            val tam = Size(r * 0.17f, r * 0.2f)
            drawOval(boca, topo, tam)
            drawOval(lingua, Offset(topo.x + tam.width * 0.18f, topo.y + tam.height * 0.5f), Size(tam.width * 0.64f, tam.height * 0.42f))
            drawOval(TracoDoRosto, topo, tam, style = Stroke(r * 0.035f))
        }
        Humor.PENSANDO -> {
            val p = Path().apply {
                moveTo(centro.x - r * 0.12f, y + r * 0.1f)
                quadraticTo(centro.x + r * 0.02f, y + r * 0.04f, centro.x + r * 0.14f, y + r * 0.02f)
            }
            drawPath(p, TracoDoRosto, style = Stroke(r * 0.065f, cap = StrokeCap.Round))
        }
    }
}

/**
 * Franja com mechas: a calota do cabelo termina em três pontinhas que descem sobre a testa (em vez de uma
 * borda reta). [curta] sobe as pontas (menino).
 */
fun franjaComMechas(centro: Offset, r: Float, curta: Boolean): Path = Path().apply {
    val d = if (curta) -0.06f else 0f
    fun x(u: Float) = centro.x + u * r
    fun y(v: Float) = centro.y + (v + d) * r
    arcTo(Rect(centro, r * 1.04f), 183.6f, 172.8f, forceMoveTo = true)
    // da direita para a esquerda: mechas redondinhas que descem na testa
    quadraticTo(x(0.95f), y(-0.4f), x(0.7f), y(-0.5f))
    cubicTo(x(0.68f), y(-0.3f), x(0.5f), y(-0.22f), x(0.4f), y(-0.26f))
    cubicTo(x(0.36f), y(-0.42f), x(0.2f), y(-0.55f), x(0.1f), y(-0.56f))
    cubicTo(x(0.1f), y(-0.36f), x(-0.06f), y(-0.24f), x(-0.2f), y(-0.28f))
    cubicTo(x(-0.24f), y(-0.44f), x(-0.4f), y(-0.55f), x(-0.5f), y(-0.55f))
    cubicTo(x(-0.52f), y(-0.36f), x(-0.68f), y(-0.26f), x(-0.8f), y(-0.3f))
    quadraticTo(x(-0.98f), y(-0.2f), x(-1.03f), y(-0.05f))
    close()
}
