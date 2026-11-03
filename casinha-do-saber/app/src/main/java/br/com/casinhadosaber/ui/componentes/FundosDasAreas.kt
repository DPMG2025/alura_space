package br.com.casinhadosaber.ui.componentes

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import br.com.casinhadosaber.dominio.AreaDaCasa
import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.dominio.TipoDeArea
import kotlin.math.PI
import kotlin.math.sin

/*
 * Fundos das áreas novas da casa (a casa que cresce), desenhados em Canvas no traço da casa:
 * céu e grama (ou parede e piso nos cômodos) e o miolo de cada área (a água da piscina, a casinha
 * do cachorro, o tanque de areia...). O miolo "nasce" com [progresso] de 0 a 1 na hora da obra.
 */

private const val H = CasaQueCresce.HORIZONTE

private fun DrawScope.p(x: Float, y: Float) = Offset(x * size.width, y * size.height)
private fun DrawScope.s(w: Float, h: Float) = Size(w * size.width, h * size.height)

/** Cor da parede de cada cômodo novo. */
private fun paredeDe(id: String): Color = when (id) {
    "quarto2" -> Color(0xFFE8E0FF)
    "brinquedos" -> Color(0xFFFFF1C9)
    "estudio" -> Color(0xFFFFE3EC)
    "biblioteca" -> Color(0xFFE2F0D9)
    "aquario" -> Color(0xFFD7F3FF)
    "cinema" -> Color(0xFF4A3B6B)
    "laboratorio" -> Color(0xFFE9F6FF)
    else -> Color(0xFFFFE6D5)
}

fun DrawScope.desenharFundoDaArea(area: AreaDaCasa, progresso: Float, t: Float, noite: Boolean) {
    val ceuDeNoite = noite || area.id == "observatorio"
    if (area.tipo == TipoDeArea.FORA) {
        val ceu = if (ceuDeNoite) listOf(Color(0xFF16204A), Color(0xFF3B4C8C)) else listOf(Color(0xFF7EC8FF), Color(0xFFCDEBFF))
        drawRect(Brush.verticalGradient(ceu, endY = size.height * H), size = s(1f, H + 0.01f))
        if (ceuDeNoite) {
            for (i in 0 until 26) {
                val x = ((i * 0.381f + 0.05f) % 1f)
                val y = ((i * 0.613f + 0.02f) % 1f) * H * 0.9f
                val brilho = 0.5f + 0.5f * sin((t + i * 0.13f) * 2f * PI.toFloat())
                drawCircle(Color.White.copy(alpha = 0.4f + 0.5f * brilho), size.width * (0.003f + (i % 3) * 0.002f), p(x, y))
            }
        } else {
            nuvem(0.18f + 0.04f * sin(t * 2f * PI.toFloat()), 0.10f, 0.05f)
            nuvem(0.74f - 0.03f * sin(t * 2f * PI.toFloat()), 0.18f, 0.04f)
        }
        val chao = when (area.id) {
            "observatorio" -> listOf(Color(0xFFB5654A), Color(0xFF8E4A35))
            "gourmet" -> listOf(Color(0xFFD9A46C), Color(0xFFB98552))
            else -> if (noite) listOf(Color(0xFF3F7A45), Color(0xFF2F5E35)) else listOf(Color(0xFF8CD17A), Color(0xFF5BA84C))
        }
        drawRect(Brush.verticalGradient(chao, startY = size.height * H, endY = size.height), p(0f, H), s(1f, 1f - H))
        when (area.id) {
            "observatorio" -> for (i in 0 until 9) drawLine(Color(0x33000000), p(0f, H + i * 0.075f), p(1f, H + i * 0.075f), size.width * 0.006f)
            "gourmet" -> for (i in 0 until 12) drawLine(Color(0x33704010), p(i / 12f, H), p(i / 12f, 1f), size.width * 0.005f)
            else -> for (i in 0 until 22) {
                val x = (i * 0.137f + 0.03f) % 1f
                val y = H + 0.03f + ((i * 0.29f) % 1f) * (0.95f - H)
                drawOval(if (noite) Color(0xFF356B3B) else Color(0xFF6BB85C), p(x - 0.025f, y), s(0.05f, 0.012f))
            }
        }
    } else {
        val parede = paredeDe(area.id)
        drawRect(parede, size = s(1f, H + 0.01f))
        // Papel de parede de bolinhas suaves e o rodapé.
        for (i in 0 until 9) for (j in 0 until 4) {
            drawCircle(Color.Black.copy(alpha = 0.05f), size.width * 0.012f, p(0.06f + i * 0.11f + (j % 2) * 0.055f, 0.05f + j * 0.08f))
        }
        val piso = when (area.id) {
            "laboratorio", "aquario" -> Color(0xFFDDE7EE)
            "cinema" -> Color(0xFF8E3B46)
            "festa" -> Color(0xFF6A4C93)
            else -> Color(0xFFD9A46C)
        }
        drawRect(Brush.verticalGradient(listOf(piso, piso.copy(red = piso.red * 0.85f, green = piso.green * 0.85f, blue = piso.blue * 0.85f)), startY = size.height * H, endY = size.height), p(0f, H), s(1f, 1f - H))
        for (i in 0 until 9) drawLine(Color(0x22000000), p(0f, H + 0.07f * i), p(1f, H + 0.07f * i), size.width * 0.004f)
        drawRect(Color(0xFFFFFFFF).copy(alpha = 0.85f), p(0f, H - 0.012f), s(1f, 0.016f))
        if (area.id != "cinema" && area.id != "aquario") janelaDaArea(0.80f, 0.06f, noite)
        if (noite) drawRect(Color(0x55142045), size = s(1f, 1f))
    }
    // O miolo de cada área, que cresce na hora da obra.
    val pivo = p(0.5f, CasaQueCresce.MIOLO_BASE + 0.04f)
    scale(progresso.coerceIn(0f, 1.2f), progresso.coerceIn(0f, 1.2f), pivo) {
        miolo(area.id, t, noite)
    }
}

private fun DrawScope.nuvem(x: Float, y: Float, r: Float) {
    val c = Color.White.copy(alpha = 0.9f)
    drawCircle(c, size.width * r, p(x, y))
    drawCircle(c, size.width * r * 0.8f, p(x - r * 1.1f, y + r * 0.3f))
    drawCircle(c, size.width * r * 0.8f, p(x + r * 1.1f, y + r * 0.3f))
}

private fun DrawScope.janelaDaArea(x: Float, y: Float, noite: Boolean) {
    drawRoundRect(Color(0xFFC98B5A), p(x - 0.01f, y - 0.01f), s(0.17f, 0.17f), CornerRadius(size.width * 0.02f))
    drawRect(if (noite) Color(0xFF26305E) else Color(0xFFA9E1FF), p(x, y), s(0.15f, 0.15f))
    drawLine(Color(0xFFC98B5A), p(x + 0.075f, y), p(x + 0.075f, y + 0.15f), size.width * 0.01f)
    drawLine(Color(0xFFC98B5A), p(x, y + 0.075f), p(x + 0.15f, y + 0.075f), size.width * 0.01f)
}

private val ME = CasaQueCresce.MIOLO_ESQ
private val MD = CasaQueCresce.MIOLO_DIR
private val MT = CasaQueCresce.MIOLO_TOPO
private val MB = CasaQueCresce.MIOLO_BASE

/** Retângulo arredondado do miolo, um pouco maior que a área dos objetos. */
private fun DrawScope.retMiolo(cor: Color, folga: Float = 0.03f, raio: Float = 0.05f) =
    drawRoundRect(cor, p(ME - folga, MT - folga), s(MD - ME + 2 * folga, MB - MT + 2 * folga), CornerRadius(size.width * raio))

private fun DrawScope.ovalMiolo(cor: Color, folga: Float = 0.03f) =
    drawOval(cor, p(ME - folga, MT - folga), s(MD - ME + 2 * folga, MB - MT + 2 * folga))

private fun DrawScope.cerca(y: Float, cor: Color = Color(0xFFF3E1C2)) {
    drawRect(cor, p(0f, y - 0.05f), s(1f, 0.012f))
    drawRect(cor, p(0f, y - 0.025f), s(1f, 0.012f))
    for (i in 0 until 14) drawRoundRect(cor, p(i / 14f + 0.01f, y - 0.075f), s(0.035f, 0.08f), CornerRadius(size.width * 0.012f))
}

private fun DrawScope.miolo(id: String, t: Float, noite: Boolean) {
    val onda = sin(t * 2f * PI.toFloat())
    when (id) {
        "quarto2" -> {
            ovalMiolo(Color(0xFFFFB3C7))
            ovalMiolo(Color(0xFFFFD6E2), folga = -0.03f)
            for (i in 0 until 5) drawPath(caminhoEstrela(p(0.1f + i * 0.17f, 0.30f), size.width * 0.015f), Color(0x66FFC93C))
        }
        "pet" -> {
            cerca(H + 0.02f)
            retMiolo(Color(0xFFE8D3A8))
            // Casinha do cachorro no fundo do miolo, com o nome em cima.
            val x = 0.40f
            val y = MT - 0.04f
            drawRect(Color(0xFFE5484D), p(x, y), s(0.2f, 0.12f))
            val telhado = Path().apply {
                moveTo(p(x - 0.03f, y).x, p(x - 0.03f, y).y)
                lineTo(p(x + 0.1f, y - 0.09f).x, p(x + 0.1f, y - 0.09f).y)
                lineTo(p(x + 0.23f, y).x, p(x + 0.23f, y).y)
                close()
            }
            drawPath(telhado, Color(0xFF8E5A34))
            drawOval(Color(0xFF3B2314), p(x + 0.055f, y + 0.035f), s(0.09f, 0.11f))
            for (i in 0 until 6) drawCircle(Color(0x338E5A34), size.width * 0.012f, p(0.28f + i * 0.09f, 0.70f + (i % 2) * 0.04f))
        }
        "piscina" -> {
            retMiolo(Color.White, folga = 0.05f)
            drawRoundRect(
                Brush.verticalGradient(listOf(Color(0xFF7FD8FF), Color(0xFF2D9CDB)), startY = size.height * (MT - 0.02f), endY = size.height * (MB + 0.02f)),
                p(ME - 0.02f, MT - 0.02f), s(MD - ME + 0.04f, MB - MT + 0.04f), CornerRadius(size.width * 0.04f),
            )
            for (k in 0 until 4) {
                val y = MT + 0.05f + k * 0.07f
                val pth = Path().apply {
                    moveTo(p(ME + 0.02f, y).x, p(ME, y).y)
                    for (i in 1..8) {
                        val x = ME + 0.02f + i * (MD - ME - 0.04f) / 8
                        lineTo(p(x, 0f).x, p(0f, y + 0.008f * sin(i * 1.6f + onda * 3f + k)).y)
                    }
                }
                drawPath(pth, Color.White.copy(alpha = 0.45f), style = Stroke(width = size.width * 0.006f, cap = StrokeCap.Round))
            }
            // Escadinha de metal na borda.
            for (dx in listOf(0f, 0.05f)) drawLine(Color(0xFFB0BEC5), p(MD - 0.08f + dx, MT - 0.08f), p(MD - 0.08f + dx, MT + 0.06f), size.width * 0.01f, StrokeCap.Round)
        }
        "parquinho" -> {
            // Balanço no fundo (balança devagar) e o tanque de areia no miolo.
            drawLine(Color(0xFFE5484D), p(0.04f, H + 0.13f), p(0.10f, H - 0.07f), size.width * 0.014f)
            drawLine(Color(0xFFE5484D), p(0.30f, H + 0.13f), p(0.24f, H - 0.07f), size.width * 0.014f)
            drawLine(Color(0xFFE5484D), p(0.09f, H - 0.07f), p(0.25f, H - 0.07f), size.width * 0.016f)
            rotate(12f * onda, p(0.17f, H - 0.07f)) {
                drawLine(Color(0xFF6B4B2A), p(0.14f, H - 0.07f), p(0.14f, H + 0.06f), size.width * 0.005f)
                drawLine(Color(0xFF6B4B2A), p(0.20f, H - 0.07f), p(0.20f, H + 0.06f), size.width * 0.005f)
                drawRoundRect(Color(0xFFFFC93C), p(0.13f, H + 0.06f), s(0.08f, 0.015f), CornerRadius(size.width * 0.006f))
            }
            retMiolo(Color(0xFFB98552), folga = 0.04f, raio = 0.03f)
            retMiolo(Color(0xFFF6D98B), folga = 0.02f, raio = 0.02f)
        }
        "horta" -> {
            cerca(H + 0.02f, Color(0xFFD9B48A))
            for (k in 0 until 4) {
                val y = MT + 0.02f + k * 0.075f
                drawRoundRect(Color(0xFF8B5A35), p(ME - 0.02f, y), s(MD - ME + 0.04f, 0.05f), CornerRadius(size.width * 0.02f))
                for (i in 0 until 8) drawCircle(Color(0xFF4CAF50), size.width * 0.008f, p(ME + i * 0.075f, y + 0.012f))
            }
            // Macieira no canto.
            drawRect(Color(0xFF8B5A35), p(0.88f, H - 0.12f), s(0.04f, 0.16f))
            drawCircle(Color(0xFF4F9B44), size.width * 0.09f, p(0.90f, H - 0.16f))
            for (i in 0 until 4) drawCircle(Color(0xFFE5484D), size.width * 0.012f, p(0.86f + (i % 2) * 0.07f, H - 0.20f + (i / 2) * 0.07f))
        }
        "brinquedos" -> {
            val cores = listOf(Color(0xFFFF6F6F), Color(0xFFFFC93C), Color(0xFF6BD08A), Color(0xFF6FB7FF), Color(0xFFB38CFF))
            cores.forEachIndexed { i, c -> ovalMiolo(c, folga = 0.04f - i * 0.025f) }
            // Prateleira de brinquedos na parede.
            drawRect(Color(0xFFC98B5A), p(0.06f, 0.22f), s(0.5f, 0.02f))
        }
        "arvore" -> {
            // Tronco grosso, copa grande e a casinha de madeira lá em cima, com escada.
            drawRoundRect(Color(0xFF8B5A35), p(0.43f, 0.10f), s(0.14f, MT - 0.10f), CornerRadius(size.width * 0.03f))
            drawCircle(Color(0xFF4F9B44), size.width * 0.22f, p(0.50f, 0.10f))
            drawCircle(Color(0xFF5BAE4E), size.width * 0.15f, p(0.30f, 0.16f))
            drawCircle(Color(0xFF5BAE4E), size.width * 0.15f, p(0.70f, 0.16f))
            drawRect(Color(0xFFD9A46C), p(0.36f, 0.08f), s(0.28f, 0.14f))
            val telhado = Path().apply {
                moveTo(p(0.33f, 0.08f).x, p(0.33f, 0.08f).y)
                lineTo(p(0.50f, 0.0f).x, p(0.50f, 0.0f).y)
                lineTo(p(0.67f, 0.08f).x, p(0.67f, 0.08f).y)
                close()
            }
            drawPath(telhado, Color(0xFFE5484D))
            drawRect(if (noite) Color(0xFFFFE08A) else Color(0xFFA9E1FF), p(0.45f, 0.11f), s(0.1f, 0.07f))
            drawLine(Color(0xFF6B4B2A), p(0.60f, 0.22f), p(0.64f, MT), size.width * 0.008f)
            drawLine(Color(0xFF6B4B2A), p(0.66f, 0.22f), p(0.70f, MT), size.width * 0.008f)
            for (i in 1 until 6) {
                val f = i / 6f
                drawLine(Color(0xFF6B4B2A), p(0.60f + 0.04f * f, 0.22f + (MT - 0.22f) * f), p(0.66f + 0.04f * f, 0.22f + (MT - 0.22f) * f), size.width * 0.007f)
            }
            // Toalha de piquenique xadrez.
            for (i in 0 until 8) for (j in 0 until 4) {
                val c = if ((i + j) % 2 == 0) Color(0xFFE5484D) else Color.White
                drawRect(c, p(ME + i * (MD - ME) / 8, MT + 0.02f + j * (MB - MT) / 4), s((MD - ME) / 8, (MB - MT) / 4))
            }
        }
        "flores" -> {
            ovalMiolo(Color(0xFFBDBDBD), folga = 0.05f)
            ovalMiolo(Color(0xFF8CD17A), folga = 0.0f)
            for (i in 0 until 14) {
                val c = listOf(Color(0xFFFF6FAE), Color(0xFFFFC93C), Color(0xFFFFFFFF), Color(0xFFB38CFF))[i % 4]
                drawCircle(c, size.width * 0.012f, p((i * 0.071f + 0.02f) % 1f, H + 0.02f + (i % 3) * 0.015f))
            }
        }
        "estudio" -> {
            // Cortinas nas laterais e um palquinho de madeira.
            drawRect(Color(0xFFD7263D), p(0f, 0f), s(0.10f, H))
            drawRect(Color(0xFFD7263D), p(0.90f, 0f), s(0.10f, H))
            retMiolo(Color(0xFF9C6B3F), folga = 0.03f, raio = 0.02f)
            retMiolo(Color(0xFFC98B5A), folga = 0.015f, raio = 0.02f)
            for (i in 0 until 5) drawPath(caminhoEstrela(p(0.25f + i * 0.12f, 0.18f), size.width * 0.012f), Color(0x88FFC93C))
        }
        "gourmet" -> {
            // Churrasqueira de tijolinhos no fundo e a mesa com toalha no miolo.
            drawRect(Color(0xFFB5654A), p(0.06f, H - 0.18f), s(0.26f, 0.22f))
            for (k in 0 until 5) drawLine(Color(0x55FFFFFF), p(0.06f, H - 0.16f + k * 0.04f), p(0.32f, H - 0.16f + k * 0.04f), size.width * 0.004f)
            drawRect(Color(0xFF555555), p(0.10f, H - 0.12f), s(0.18f, 0.05f))
            drawRect(Color(0xFF8E4A35), p(0.15f, H - 0.30f), s(0.08f, 0.12f))
            retMiolo(Color(0xFF8E5A34), folga = 0.0f, raio = 0.03f)
            retMiolo(Color(0xFFFFF4E0), folga = -0.015f, raio = 0.03f)
            drawLine(Color(0xFFE5484D), p(ME, MT + 0.02f), p(MD, MT + 0.02f), size.width * 0.01f)
        }
        "biblioteca" -> {
            // Estantes com lombadas coloridas e um tapete.
            for (prat in 0 until 2) {
                val y = 0.10f + prat * 0.11f
                drawRect(Color(0xFF8E5A34), p(0.04f, y + 0.09f), s(0.66f, 0.015f))
                for (i in 0 until 14) {
                    val c = listOf(Color(0xFFE5484D), Color(0xFF3D8BFD), Color(0xFF2FB36A), Color(0xFFFFC93C), Color(0xFFB38CFF))[(i + prat) % 5]
                    drawRect(c, p(0.05f + i * 0.045f, y + 0.02f + (i % 3) * 0.005f), s(0.035f, 0.07f - (i % 3) * 0.005f))
                }
            }
            ovalMiolo(Color(0xFF6FB7FF))
            ovalMiolo(Color(0xFFBFE1FF), folga = -0.03f)
        }
        "observatorio" -> {
            // Cúpula do telescópio no fundo e o deque redondo de madeira.
            drawArc(Color(0xFFCFD8DC), 180f, 180f, true, p(0.30f, H - 0.18f), s(0.40f, 0.36f))
            drawRect(Color(0xFF90A4AE), p(0.47f, H - 0.18f), s(0.06f, 0.18f))
            ovalMiolo(Color(0xFF8E5A34), folga = 0.03f)
            ovalMiolo(Color(0xFFC98B5A), folga = 0.0f)
        }
        "fazendinha" -> {
            // Celeiro vermelho e o cercadinho de feno.
            drawRect(Color(0xFFD7263D), p(0.66f, H - 0.20f), s(0.28f, 0.22f))
            val tel = Path().apply {
                moveTo(p(0.63f, H - 0.20f).x, p(0.63f, H - 0.20f).y)
                lineTo(p(0.80f, H - 0.31f).x, p(0.80f, H - 0.31f).y)
                lineTo(p(0.97f, H - 0.20f).x, p(0.97f, H - 0.20f).y)
                close()
            }
            drawPath(tel, Color(0xFF8E2B2B))
            drawRect(Color.White, p(0.75f, H - 0.10f), s(0.10f, 0.12f), style = Stroke(size.width * 0.008f))
            retMiolo(Color(0xFFE9C46A), folga = 0.03f)
            cerca(MT - 0.01f, Color(0xFFD9B48A))
        }
        "quadra" -> {
            retMiolo(Color(0xFF2FB36A), folga = 0.05f, raio = 0.01f)
            drawRect(Color.White, p(ME - 0.03f, MT - 0.03f), s(MD - ME + 0.06f, MB - MT + 0.06f), style = Stroke(size.width * 0.008f))
            drawLine(Color.White, p(0.5f, MT - 0.03f), p(0.5f, MB + 0.03f), size.width * 0.008f)
            drawCircle(Color.White, size.width * 0.07f, p(0.5f, (MT + MB) / 2), style = Stroke(size.width * 0.008f))
        }
        "aquario" -> {
            // Aquário grande que ocupa a parede e o miolo.
            drawRoundRect(Color(0xFF455A64), p(ME - 0.06f, 0.06f), s(MD - ME + 0.12f, MB - 0.02f), CornerRadius(size.width * 0.02f))
            drawRect(
                Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFF0277BD)), startY = size.height * 0.08f, endY = size.height * MB),
                p(ME - 0.045f, 0.08f), s(MD - ME + 0.09f, MB - 0.06f),
            )
            for (i in 0 until 7) drawCircle(Color.White.copy(alpha = 0.5f), size.width * 0.008f, p(ME + 0.05f + (i * 0.083f) % (MD - ME), 0.6f - ((t + i * 0.14f) % 1f) * 0.45f))
            drawRect(Color(0xFFF6D98B), p(ME - 0.045f, MB - 0.04f), s(MD - ME + 0.09f, 0.02f))
        }
        "cinema" -> {
            drawRect(Color(0xFFB71C1C), p(0f, 0f), s(0.12f, H))
            drawRect(Color(0xFFB71C1C), p(0.88f, 0f), s(0.12f, H))
            drawRect(Color.White, p(0.16f, 0.04f), s(0.68f, H - 0.08f))
            drawRect(Color(0xFFFFF8E1).copy(alpha = 0.6f + 0.4f * ((onda + 1f) / 2f)), p(0.18f, 0.06f), s(0.64f, H - 0.12f))
            retMiolo(Color(0xFFD7263D), folga = 0.0f, raio = 0.02f)
        }
        "lago" -> {
            ovalMiolo(Color(0xFF6D8B5A), folga = 0.05f)
            ovalMiolo(Color(0xFF4FC3F7), folga = 0.03f)
            // Pontezinha de madeira em arco.
            drawArc(Color(0xFF8E5A34), 180f, 180f, false, p(0.36f, MT - 0.06f), s(0.28f, 0.14f), style = Stroke(size.width * 0.03f))
            for (i in 0 until 6) drawLine(Color(0xFF4F9B44), p(0.12f + i * 0.015f, MB), p(0.11f + i * 0.02f, MB - 0.08f - (i % 2) * 0.03f), size.width * 0.006f)
        }
        "laboratorio" -> {
            for (i in 0 until 10) for (j in 0 until 4) drawRect(Color(0x223D8BFD), p(i * 0.1f + (j % 2) * 0.05f, j * 0.08f), s(0.045f, 0.035f))
            drawRect(Color(0xFF90A4AE), p(0.05f, H - 0.14f), s(0.5f, 0.03f))
            for (i in 0 until 8) for (j in 0 until 3) {
                val c = if ((i + j) % 2 == 0) Color(0xFFFFFFFF) else Color(0xFFB0BEC5)
                drawRect(c, p(ME + i * (MD - ME) / 8, MT + 0.02f + j * (MB - MT) / 3), s((MD - ME) / 8, (MB - MT) / 3))
            }
        }
        "estufa" -> {
            // Estrutura de vidro: céu entre os caixilhos brancos, e o canteiro no miolo.
            for (i in 0..6) drawLine(Color.White, p(i / 6f, 0f), p(i / 6f, H), size.width * 0.008f)
            drawLine(Color.White, p(0f, 0.15f), p(1f, 0.15f), size.width * 0.008f)
            retMiolo(Color(0xFF8B5A35), folga = 0.03f)
            for (i in 0 until 10) drawCircle(Color(0xFF4CAF50), size.width * 0.012f, p(ME + i * 0.06f, MT + 0.04f + (i % 2) * 0.12f))
        }
        "festa" -> {
            // Bandeirinhas e pista de dança colorida.
            val cores = listOf(Color(0xFFFF6F6F), Color(0xFFFFC93C), Color(0xFF6BD08A), Color(0xFF6FB7FF), Color(0xFFB38CFF))
            for (i in 0 until 12) {
                val x = i / 12f
                val tri = Path().apply {
                    moveTo(p(x, 0.04f).x, p(x, 0.04f).y)
                    lineTo(p(x + 1 / 12f, 0.04f).x, p(x + 1 / 12f, 0.04f).y)
                    lineTo(p(x + 0.5f / 12f, 0.10f).x, p(x + 0.5f / 12f, 0.10f).y)
                    close()
                }
                drawPath(tri, cores[i % cores.size])
            }
            for (i in 0 until 6) for (j in 0 until 3) {
                val acesa = ((i + j + (t * 6).toInt()) % 3) == 0
                drawRect(cores[(i + j) % cores.size].copy(alpha = if (acesa) 1f else 0.55f), p(ME + i * (MD - ME) / 6, MT + 0.02f + j * (MB - MT) / 3), s((MD - ME) / 6 - 0.004f, (MB - MT) / 3 - 0.004f))
            }
        }
        else -> retMiolo(Color(0x33FFFFFF))
    }
}
