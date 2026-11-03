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
import kotlin.math.cos
import kotlin.math.sin

/*
 * Fundos das áreas novas da casa (a casa que cresce), desenhados em Canvas no traço da casa:
 * céu e grama (ou parede e piso nos cômodos) e o miolo de cada área (a água da piscina, a casinha
 * do cachorro, o tanque de areia...). O miolo "nasce" com [progresso] de 0 a 1 na hora da obra.
 *
 * O desenho tem duas camadas: a parada ([desenharFundoParado], refeita só quando a área, a obra ou a noite
 * mudam) e a que mexe ([desenharFundoAnimado]: estrelas, nuvens, água, luzes da pista), que roda a cada
 * quadro e só usa formas simples (sem montar caminhos novos).
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
    "festa" -> Color(0xFF3B2F5C)
    else -> Color(0xFFFFE6D5)
}

private fun escurecida(c: Color, f: Float) = Color(c.red * f, c.green * f, c.blue * f, c.alpha)

/** O fundo inteiro de uma vez (parado e animado). As telas usam as duas camadas separadas. */
fun DrawScope.desenharFundoDaArea(area: AreaDaCasa, progresso: Float, t: Float, noite: Boolean) {
    desenharFundoParado(area, progresso, noite)
    desenharFundoAnimado(area, progresso, t, noite)
}

/** A parte parada do fundo: céu, paisagem, chão, rodapé, o miolo e o vinhetado dos cantos. */
fun DrawScope.desenharFundoParado(area: AreaDaCasa, progresso: Float, noite: Boolean) {
    val ceuDeNoite = noite || area.id == "observatorio"
    if (area.tipo == TipoDeArea.FORA) {
        // Céu mais forte no alto e clarinho no horizonte.
        val ceu = if (ceuDeNoite) {
            listOf(Color(0xFF0E1638), Color(0xFF22306A), Color(0xFF3B4C8C))
        } else {
            listOf(Color(0xFF5FB2F2), Color(0xFF9AD3FF), Color(0xFFDDF1FF))
        }
        drawRect(Brush.verticalGradient(ceu, endY = size.height * H), size = s(1f, H + 0.01f))
        if (ceuDeNoite) {
            // Lua cheia de canto (as estrelinhas piscam na camada de cima).
            drawCircle(Color(0x22FFF3C4), size.width * 0.09f, p(0.88f, 0.08f))
            drawCircle(Color(0xFFFFF3C4), size.width * 0.045f, p(0.88f, 0.08f))
            drawCircle(Color(0x22B0A060), size.width * 0.012f, p(0.875f, 0.07f))
        }
        // Paisagem do fundo: morros e arvorezinhas no horizonte, para o céu não ficar vazio.
        if (area.id != "observatorio") paisagemDoFundo(noite)
        // Neblina clarinha no horizonte (dá distância entre o céu e o chão).
        val nevoa = if (ceuDeNoite) Color(0x337C8CCF) else Color(0x66FFFFFF)
        drawRect(
            Brush.verticalGradient(listOf(Color.Transparent, nevoa, Color.Transparent), startY = size.height * (H - 0.12f), endY = size.height * (H + 0.03f)),
            p(0f, H - 0.12f), s(1f, 0.15f),
        )
        val chao = when (area.id) {
            "observatorio" -> listOf(Color(0xFFB5654A), Color(0xFF8E4A35))
            "gourmet" -> listOf(Color(0xFFD9A46C), Color(0xFFB98552))
            else -> if (noite) listOf(Color(0xFF3F7A45), Color(0xFF2F5E35)) else listOf(Color(0xFF8CD17A), Color(0xFF5BA84C))
        }
        drawRect(Brush.verticalGradient(chao, startY = size.height * H, endY = size.height), p(0f, H), s(1f, 1f - H))
        when (area.id) {
            "observatorio" -> for (i in 0 until 9) {
                val y = H + i * 0.075f
                drawLine(Color(0x33000000), p(0f, y), p(1f, y), size.width * 0.006f)
                drawLine(Color(0x1FFFFFFF), p(0f, y + 0.006f), p(1f, y + 0.006f), size.width * 0.003f)
                // Emendas das tábuas, desencontradas.
                for (k in 0 until 4) {
                    val x = ((k * 0.29f + i * 0.13f) % 1f)
                    drawLine(Color(0x26000000), p(x, y), p(x, y + 0.075f), size.width * 0.004f)
                }
            }
            "gourmet" -> for (i in 0 until 12) drawLine(Color(0x33704010), p(i / 12f, H), p(i / 12f, 1f), size.width * 0.005f)
            else -> for (i in 0 until 22) {
                val x = (i * 0.137f + 0.03f) % 1f
                val y = H + 0.03f + ((i * 0.29f) % 1f) * (0.95f - H)
                drawOval(if (noite) Color(0xFF356B3B) else Color(0xFF6BB85C), p(x - 0.025f, y), s(0.05f, 0.012f))
            }
        }
        // Sombra suave logo abaixo do horizonte (o chão "entra" embaixo da paisagem).
        drawRect(Brush.verticalGradient(listOf(Color(0x22000000), Color.Transparent), startY = size.height * H, endY = size.height * (H + 0.03f)), p(0f, H), s(1f, 0.03f))
    } else {
        val parede = paredeDe(area.id)
        drawRect(Brush.verticalGradient(listOf(parede, escurecida(parede, 0.93f)), endY = size.height * H), size = s(1f, H + 0.01f))
        // Papel de parede de bolinhas suaves.
        for (i in 0 until 9) for (j in 0 until 4) {
            drawCircle(Color.Black.copy(alpha = 0.05f), size.width * 0.012f, p(0.06f + i * 0.11f + (j % 2) * 0.055f, 0.05f + j * 0.08f))
        }
        val piso = when (area.id) {
            "laboratorio", "aquario" -> Color(0xFFDDE7EE)
            "cinema" -> Color(0xFF8E3B46)
            "festa" -> Color(0xFF4A3670)
            else -> Color(0xFFD9A46C)
        }
        drawRect(Brush.verticalGradient(listOf(piso, escurecida(piso, 0.85f)), startY = size.height * H, endY = size.height), p(0f, H), s(1f, 1f - H))
        for (i in 0 until 9) {
            val y = H + 0.07f * i
            drawLine(Color(0x22000000), p(0f, y), p(1f, y), size.width * 0.004f)
            drawLine(Color(0x14FFFFFF), p(0f, y + 0.004f), p(1f, y + 0.004f), size.width * 0.003f)
        }
        // Rodapé: a emenda da parede com o chão, com a sombrinha que ele faz no piso.
        drawRect(Brush.verticalGradient(listOf(Color(0x33000000), Color.Transparent), startY = size.height * H, endY = size.height * (H + 0.035f)), p(0f, H), s(1f, 0.035f))
        drawRect(Color(0xFFFFFBF4), p(0f, H - 0.022f), s(1f, 0.026f))
        drawRect(Color(0x1F000000), p(0f, H - 0.022f), s(1f, 0.004f))
        drawRect(Color(0x26000000), p(0f, H + 0.0f), s(1f, 0.004f))
        if (area.id != "cinema" && area.id != "aquario") janelaDaArea(0.80f, 0.06f, noite)
        if (noite) drawRect(Color(0x55142045), size = s(1f, 1f))
    }
    // O miolo de cada área, que cresce na hora da obra.
    val escala = progresso.coerceIn(0f, 1.2f)
    if (escala > 0f) {
        scale(escala, escala, p(0.5f, CasaQueCresce.MIOLO_BASE + 0.04f)) { miolo(area.id, noite) }
    }
    // Cantos um pouco mais escuros: a cena fica "emoldurada" e o olho vai para o meio.
    drawRect(
        Brush.radialGradient(
            listOf(Color.Transparent, Color.Transparent, Color(0x2E1B1035)),
            center = center,
            radius = maxOf(size.width, size.height) * 0.72f,
        ),
    )
}

/** A parte que mexe: estrelas piscando, nuvens passeando e o miolo animado (água, balanço, luzes da pista). */
fun DrawScope.desenharFundoAnimado(area: AreaDaCasa, progresso: Float, t: Float, noite: Boolean) {
    val ceuDeNoite = noite || area.id == "observatorio"
    if (area.tipo == TipoDeArea.FORA) {
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
    }
    val escala = progresso.coerceIn(0f, 1.2f)
    if (escala > 0f) {
        scale(escala, escala, p(0.5f, CasaQueCresce.MIOLO_BASE + 0.04f)) { mioloAnimado(area.id, t) }
    }
}

private fun DrawScope.paisagemDoFundo(noite: Boolean) {
    val longe = if (noite) Color(0xFF2E5A3A) else Color(0xFFA8DC94)
    val perto = if (noite) Color(0xFF346B40) else Color(0xFF8CCB78)
    drawOval(longe, p(-0.25f, H - 0.10f), s(0.85f, 0.22f))
    drawOval(longe, p(0.45f, H - 0.12f), s(0.85f, 0.26f))
    drawOval(perto, p(0.15f, H - 0.06f), s(0.75f, 0.16f))
    // Arvorezinhas no horizonte.
    for ((x, alto) in listOf(0.06f to 0.07f, 0.13f to 0.05f, 0.83f to 0.08f, 0.91f to 0.06f, 0.56f to 0.05f)) {
        drawRect(Color(0xFF8B5E3C), p(x - 0.006f, H - alto * 0.55f), s(0.012f, alto * 0.6f))
        drawCircle(if (noite) Color(0xFF2B5534) else Color(0xFF4FA35B), size.width * alto * 0.55f, p(x, H - alto * 0.75f))
        drawCircle(if (noite) Color(0x1FFFFFFF) else Color(0x33FFFFFF), size.width * alto * 0.22f, p(x - alto * 0.18f, H - alto * 0.92f))
    }
    // Sebe baixa na linha do horizonte.
    for (i in 0 until 16) drawCircle(perto.copy(alpha = 0.9f), size.width * 0.04f, p(i / 15f, H + 0.005f))
}

private fun DrawScope.nuvem(x: Float, y: Float, r: Float) {
    val c = Color.White.copy(alpha = 0.9f)
    drawCircle(c, size.width * r, p(x, y))
    drawCircle(c, size.width * r * 0.8f, p(x - r * 1.1f, y + r * 0.3f))
    drawCircle(c, size.width * r * 0.8f, p(x + r * 1.1f, y + r * 0.3f))
}

private fun DrawScope.janelaDaArea(x: Float, y: Float, noite: Boolean) {
    if (noite) drawCircle(Color(0x33FFE08A), size.width * 0.13f, p(x + 0.075f, y + 0.075f))
    drawRoundRect(Color(0xFFC98B5A), p(x - 0.01f, y - 0.01f), s(0.17f, 0.17f), CornerRadius(size.width * 0.02f))
    drawRect(
        if (noite) Brush.verticalGradient(listOf(Color(0xFF1B2450), Color(0xFF3B4C8C)), startY = size.height * y, endY = size.height * (y + 0.15f))
        else Brush.verticalGradient(listOf(Color(0xFF7CC6FF), Color(0xFFCDEBFF)), startY = size.height * y, endY = size.height * (y + 0.15f)),
        p(x, y), s(0.15f, 0.15f),
    )
    // Reflexo de luz no vidro.
    drawLine(Color.White.copy(alpha = 0.5f), p(x + 0.02f, y + 0.055f), p(x + 0.05f, y + 0.015f), size.width * 0.008f, StrokeCap.Round)
    drawLine(Color(0xFFC98B5A), p(x + 0.075f, y), p(x + 0.075f, y + 0.15f), size.width * 0.01f)
    drawLine(Color(0xFFC98B5A), p(x, y + 0.075f), p(x + 0.15f, y + 0.075f), size.width * 0.01f)
    // Peitoril com sombrinha.
    drawRoundRect(Color(0xFFB5794A), p(x - 0.02f, y + 0.155f), s(0.19f, 0.018f), CornerRadius(size.width * 0.006f))
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

/** Sombra de contato do miolo no chão (o tapete, a piscina e o deque "pousam" no chão). */
private fun DrawScope.sombraDoMiolo(folga: Float, oval: Boolean) {
    val topo = p(ME - folga + 0.01f, MT - folga + 0.02f)
    val tamanho = s(MD - ME + 2 * folga, MB - MT + 2 * folga)
    if (oval) drawOval(Color(0x29000000), topo, tamanho) else drawRoundRect(Color(0x29000000), topo, tamanho, CornerRadius(size.width * 0.04f))
}

private fun DrawScope.cerca(y: Float, cor: Color = Color(0xFFF3E1C2)) {
    val sombra = escurecida(cor, 0.8f)
    drawRect(cor, p(0f, y - 0.05f), s(1f, 0.012f))
    drawRect(cor, p(0f, y - 0.025f), s(1f, 0.012f))
    for (i in 0 until 14) {
        drawRoundRect(sombra, p(i / 14f + 0.014f, y - 0.072f), s(0.035f, 0.08f), CornerRadius(size.width * 0.012f))
        drawRoundRect(cor, p(i / 14f + 0.01f, y - 0.075f), s(0.035f, 0.08f), CornerRadius(size.width * 0.012f))
    }
}

/** Tábuas na horizontal dentro de um retângulo (frações), com veios de madeira desencontrados. */
private fun DrawScope.tabuas(x: Float, y: Float, w: Float, h: Float, base: Color, n: Int) {
    drawRect(base, p(x, y), s(w, h))
    for (i in 0 until n) {
        val yy = y + h * i / n
        if (i > 0) drawLine(escurecida(base, 0.72f), p(x, yy), p(x + w, yy), size.width * 0.004f)
        val emenda = x + w * (((i * 0.37f) % 1f) * 0.8f + 0.1f)
        drawLine(escurecida(base, 0.8f), p(emenda, yy), p(emenda, yy + h / n), size.width * 0.003f)
        drawLine(Color(0x1FFFFFFF), p(x, yy + h / n * 0.3f), p(x + w, yy + h / n * 0.3f), size.width * 0.002f)
    }
}

private fun DrawScope.miolo(id: String, noite: Boolean) {
    when (id) {
        "quarto2" -> {
            sombraDoMiolo(0.03f, oval = true)
            ovalMiolo(Color(0xFFFFB3C7))
            ovalMiolo(Color(0xFFFFD6E2), folga = -0.03f)
            for (i in 0 until 5) drawPath(caminhoEstrela(p(0.1f + i * 0.17f, 0.30f), size.width * 0.015f), Color(0x66FFC93C))
        }
        "pet" -> {
            cerca(H + 0.02f)
            sombraDoMiolo(0.03f, oval = false)
            retMiolo(Color(0xFFE8D3A8))
            // Casinha do cachorro no fundo do miolo, com o nome em cima.
            val x = 0.40f
            val y = MT - 0.04f
            drawRect(Color(0xFFE5484D), p(x, y), s(0.2f, 0.12f))
            drawRect(Color(0x26000000), p(x + 0.15f, y), s(0.05f, 0.12f))
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
        "piscina" -> piscina()
        "parquinho" -> {
            // A armação do balanço (o assento balança na camada de cima) e o tanque de areia no miolo.
            drawLine(Color(0xFFE5484D), p(0.04f, H + 0.13f), p(0.10f, H - 0.07f), size.width * 0.014f)
            drawLine(Color(0xFFE5484D), p(0.30f, H + 0.13f), p(0.24f, H - 0.07f), size.width * 0.014f)
            drawLine(Color(0xFFE5484D), p(0.09f, H - 0.07f), p(0.25f, H - 0.07f), size.width * 0.016f)
            sombraDoMiolo(0.04f, oval = false)
            retMiolo(Color(0xFFB98552), folga = 0.04f, raio = 0.03f)
            retMiolo(Color(0xFFF6D98B), folga = 0.02f, raio = 0.02f)
        }
        "horta" -> {
            cerca(H + 0.02f, Color(0xFFD9B48A))
            for (k in 0 until 4) {
                val y = MT + 0.02f + k * 0.075f
                drawRoundRect(Color(0x26000000), p(ME - 0.015f, y + 0.008f), s(MD - ME + 0.04f, 0.05f), CornerRadius(size.width * 0.02f))
                drawRoundRect(Color(0xFF8B5A35), p(ME - 0.02f, y), s(MD - ME + 0.04f, 0.05f), CornerRadius(size.width * 0.02f))
                for (i in 0 until 8) drawCircle(Color(0xFF4CAF50), size.width * 0.008f, p(ME + i * 0.075f, y + 0.012f))
            }
            // Macieira no canto.
            drawRect(Color(0xFF8B5A35), p(0.88f, H - 0.12f), s(0.04f, 0.16f))
            drawCircle(Color(0xFF3F8A3A), size.width * 0.09f, p(0.91f, H - 0.15f))
            drawCircle(Color(0xFF4F9B44), size.width * 0.08f, p(0.89f, H - 0.17f))
            for (i in 0 until 4) drawCircle(Color(0xFFE5484D), size.width * 0.012f, p(0.86f + (i % 2) * 0.07f, H - 0.20f + (i / 2) * 0.07f))
        }
        "brinquedos" -> {
            sombraDoMiolo(0.04f, oval = true)
            val cores = listOf(Color(0xFFFF6F6F), Color(0xFFFFC93C), Color(0xFF6BD08A), Color(0xFF6FB7FF), Color(0xFFB38CFF))
            cores.forEachIndexed { i, c -> ovalMiolo(c, folga = 0.04f - i * 0.025f) }
            // Prateleira de brinquedos na parede.
            drawRect(Color(0x22000000), p(0.06f, 0.24f), s(0.5f, 0.012f))
            drawRect(Color(0xFFC98B5A), p(0.06f, 0.22f), s(0.5f, 0.02f))
        }
        "arvore" -> casaNaArvore(noite)
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
            for (k in 0 until 3) {
                drawLine(Color(0x33000000), p(0.025f + k * 0.03f, 0f), p(0.025f + k * 0.03f, H), size.width * 0.006f)
                drawLine(Color(0x33000000), p(0.925f + k * 0.03f, 0f), p(0.925f + k * 0.03f, H), size.width * 0.006f)
            }
            sombraDoMiolo(0.03f, oval = false)
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
            sombraDoMiolo(0.0f, oval = false)
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
            sombraDoMiolo(0.03f, oval = true)
            ovalMiolo(Color(0xFF6FB7FF))
            ovalMiolo(Color(0xFFBFE1FF), folga = -0.03f)
        }
        "observatorio" -> observatorio()
        "fazendinha" -> {
            // Celeiro vermelho e o cercadinho de feno.
            drawRect(Color(0xFFD7263D), p(0.66f, H - 0.20f), s(0.28f, 0.22f))
            drawRect(Color(0x26000000), p(0.88f, H - 0.20f), s(0.06f, 0.22f))
            val tel = Path().apply {
                moveTo(p(0.63f, H - 0.20f).x, p(0.63f, H - 0.20f).y)
                lineTo(p(0.80f, H - 0.31f).x, p(0.80f, H - 0.31f).y)
                lineTo(p(0.97f, H - 0.20f).x, p(0.97f, H - 0.20f).y)
                close()
            }
            drawPath(tel, Color(0xFF8E2B2B))
            drawRect(Color.White, p(0.75f, H - 0.10f), s(0.10f, 0.12f), style = Stroke(size.width * 0.008f))
            sombraDoMiolo(0.03f, oval = false)
            retMiolo(Color(0xFFE9C46A), folga = 0.03f)
            cerca(MT - 0.01f, Color(0xFFD9B48A))
        }
        "quadra" -> {
            sombraDoMiolo(0.05f, oval = false)
            retMiolo(Color(0xFF2FB36A), folga = 0.05f, raio = 0.01f)
            drawRect(Color.White, p(ME - 0.03f, MT - 0.03f), s(MD - ME + 0.06f, MB - MT + 0.06f), style = Stroke(size.width * 0.008f))
            drawLine(Color.White, p(0.5f, MT - 0.03f), p(0.5f, MB + 0.03f), size.width * 0.008f)
            drawCircle(Color.White, size.width * 0.07f, p(0.5f, (MT + MB) / 2), style = Stroke(size.width * 0.008f))
        }
        "aquario" -> {
            // Aquário grande que ocupa a parede e o miolo (as bolhas sobem na camada de cima).
            drawRoundRect(Color(0xFF455A64), p(ME - 0.06f, 0.06f), s(MD - ME + 0.12f, MB - 0.02f), CornerRadius(size.width * 0.02f))
            drawRect(
                Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFF0277BD)), startY = size.height * 0.08f, endY = size.height * MB),
                p(ME - 0.045f, 0.08f), s(MD - ME + 0.09f, MB - 0.06f),
            )
            drawRect(Color(0xFFF6D98B), p(ME - 0.045f, MB - 0.04f), s(MD - ME + 0.09f, 0.02f))
            drawLine(Color(0x55FFFFFF), p(ME - 0.03f, 0.10f), p(ME - 0.03f, MB - 0.06f), size.width * 0.008f)
        }
        "cinema" -> {
            drawRect(Color(0xFFB71C1C), p(0f, 0f), s(0.12f, H))
            drawRect(Color(0xFFB71C1C), p(0.88f, 0f), s(0.12f, H))
            drawRect(Color.White, p(0.16f, 0.04f), s(0.68f, H - 0.08f))
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
            sombraDoMiolo(0.0f, oval = false)
            for (i in 0 until 8) for (j in 0 until 3) {
                val c = if ((i + j) % 2 == 0) Color(0xFFFFFFFF) else Color(0xFFB0BEC5)
                drawRect(c, p(ME + i * (MD - ME) / 8, MT + 0.02f + j * (MB - MT) / 3), s((MD - ME) / 8, (MB - MT) / 3))
            }
        }
        "estufa" -> {
            // Estrutura de vidro: céu entre os caixilhos brancos, e o canteiro no miolo.
            for (i in 0..6) drawLine(Color.White, p(i / 6f, 0f), p(i / 6f, H), size.width * 0.008f)
            drawLine(Color.White, p(0f, 0.15f), p(1f, 0.15f), size.width * 0.008f)
            sombraDoMiolo(0.03f, oval = false)
            retMiolo(Color(0xFF8B5A35), folga = 0.03f)
            for (i in 0 until 10) drawCircle(Color(0xFF4CAF50), size.width * 0.012f, p(ME + i * 0.06f, MT + 0.04f + (i % 2) * 0.12f))
        }
        "festa" -> salaoDeFestas()
        else -> retMiolo(Color(0x33FFFFFF))
    }
}

private fun DrawScope.mioloAnimado(id: String, t: Float) {
    val onda = sin(t * 2f * PI.toFloat())
    when (id) {
        "piscina" -> aguaQueMexe(t)
        "parquinho" -> rotate(12f * onda, p(0.17f, H - 0.07f)) {
            drawLine(Color(0xFF6B4B2A), p(0.14f, H - 0.07f), p(0.14f, H + 0.06f), size.width * 0.005f)
            drawLine(Color(0xFF6B4B2A), p(0.20f, H - 0.07f), p(0.20f, H + 0.06f), size.width * 0.005f)
            drawRoundRect(Color(0xFFFFC93C), p(0.13f, H + 0.06f), s(0.08f, 0.015f), CornerRadius(size.width * 0.006f))
        }
        "aquario" -> for (i in 0 until 7) {
            drawCircle(Color.White.copy(alpha = 0.5f), size.width * 0.008f, p(ME + 0.05f + (i * 0.083f) % (MD - ME), 0.6f - ((t + i * 0.14f) % 1f) * 0.45f))
        }
        "cinema" -> drawRect(Color(0xFFFFF8E1).copy(alpha = 0.6f + 0.4f * ((onda + 1f) / 2f)), p(0.18f, 0.06f), s(0.64f, H - 0.12f))
        "arvore" -> escadaDeCorda(t)
        "festa" -> luzesDaPista(t)
    }
}

// ---------------------------------------------------------------------------------------------- piscina

private fun DrawScope.piscina() {
    // Deque de madeira em volta da piscina, com tábuas.
    sombraDoMiolo(0.11f, oval = false)
    retMiolo(Color(0xFFD9A46C), folga = 0.11f, raio = 0.03f)
    for (i in 0 until 14) {
        val y = MT - 0.11f + i * (MB - MT + 0.22f) / 14f
        drawLine(Color(0x338A5A2B), p(ME - 0.11f, y), p(MD + 0.11f, y), size.width * 0.004f)
        drawLine(Color(0x1FFFFFFF), p(ME - 0.11f, y + 0.004f), p(MD + 0.11f, y + 0.004f), size.width * 0.002f)
    }
    // Guarda-sol listrado e plantas nos cantos do deque (desenhados, não são prêmios).
    drawOval(Color(0x26000000), p(ME - 0.13f, MT - 0.05f), s(0.13f, 0.025f))
    drawLine(Color(0xFF8A5A2B), p(ME - 0.07f, MT - 0.16f), p(ME - 0.07f, MT - 0.04f), size.width * 0.008f)
    val guardaSol = Path().apply {
        moveTo(p(ME - 0.17f, MT - 0.13f).x, p(ME - 0.17f, MT - 0.13f).y)
        quadraticTo(p(ME - 0.07f, MT - 0.24f).x, p(ME - 0.07f, MT - 0.24f).y, p(ME + 0.03f, MT - 0.13f).x, p(ME + 0.03f, MT - 0.13f).y)
        close()
    }
    drawPath(guardaSol, Color(0xFFFF7A7A))
    for (k in listOf(-0.05f, 0.05f)) drawLine(Color.White.copy(alpha = 0.85f), p(ME - 0.07f, MT - 0.185f), p(ME - 0.07f + k * 1.4f, MT - 0.13f), size.width * 0.012f)
    for ((x, y) in listOf(MD + 0.08f to MT - 0.08f, ME - 0.08f to MB + 0.08f, MD + 0.08f to MB + 0.08f)) {
        drawOval(Color(0x26000000), p(x - 0.03f, y + 0.02f), s(0.06f, 0.015f))
        drawRoundRect(Color(0xFFC0673A), p(x - 0.025f, y - 0.01f), s(0.05f, 0.04f), CornerRadius(size.width * 0.008f))
        for (k in -1..1) drawOval(if (k == 0) Color(0xFF2F8E3D) else Color(0xFF3FA34D), p(x - 0.012f + k * 0.022f, y - 0.07f), s(0.024f, 0.065f))
    }
    // Borda de pedra branca com os azulejinhos.
    retMiolo(Color(0xFFF7F4EE), folga = 0.05f)
    val passo = (MD - ME + 0.1f) / 14f
    for (i in 1 until 14) {
        val x = ME - 0.05f + i * passo
        drawLine(Color(0x1F5D4037), p(x, MT - 0.05f), p(x, MT - 0.02f), size.width * 0.003f)
        drawLine(Color(0x1F5D4037), p(x, MB + 0.02f), p(x, MB + 0.05f), size.width * 0.003f)
    }
    // A água: funda no fundo, clarinha na frente, com o azulejo do fundo aparecendo.
    val agua = Brush.verticalGradient(listOf(Color(0xFF2D9CDB), Color(0xFF5BC6F5), Color(0xFF8FE0FF)), startY = size.height * (MT - 0.02f), endY = size.height * (MB + 0.02f))
    drawRoundRect(agua, p(ME - 0.02f, MT - 0.02f), s(MD - ME + 0.04f, MB - MT + 0.04f), CornerRadius(size.width * 0.04f))
    for (i in 1 until 10) {
        val x = ME - 0.02f + i * (MD - ME + 0.04f) / 10f
        drawLine(Color(0x1AFFFFFF), p(x, MT), p(x, MB), size.width * 0.003f)
    }
    for (j in 1 until 6) {
        val y = MT - 0.02f + j * (MB - MT + 0.04f) / 6f
        drawLine(Color(0x1AFFFFFF), p(ME, y), p(MD, y), size.width * 0.003f)
    }
    // Sombra da borda dentro da água (a parede da piscina).
    drawRect(Brush.verticalGradient(listOf(Color(0x401565C0), Color.Transparent), startY = size.height * (MT - 0.02f), endY = size.height * (MT + 0.03f)), p(ME - 0.005f, MT - 0.02f), s(MD - ME + 0.01f, 0.05f))
    // Escadinha de metal no canto da borda, fora das boias da primeira fileira.
    for (dx in listOf(0f, 0.04f)) drawLine(Color(0xFFB0BEC5), p(MD - 0.015f + dx, MT - 0.08f), p(MD - 0.015f + dx, MT + 0.06f), size.width * 0.01f, StrokeCap.Round)
    for (k in 0 until 3) drawLine(Color(0xFF90A4AE), p(MD - 0.015f, MT - 0.03f + k * 0.035f), p(MD + 0.025f, MT - 0.03f + k * 0.035f), size.width * 0.006f)
}

/** Brilhos de luz no fundo da água (cáusticas) e as ondinhas da superfície: só linhas e bolinhas. */
private fun DrawScope.aguaQueMexe(t: Float) {
    val a = t * 2f * PI.toFloat()
    val larg = MD - ME - 0.06f
    // Cáusticas: risquinhos claros que se mexem devagar, em rede.
    for (i in 0 until 22) {
        val bx = ME + 0.03f + ((i * 0.618f) % 1f) * larg
        val by = MT + 0.03f + ((i * 0.377f) % 1f) * (MB - MT - 0.06f)
        val fase = a + i * 0.9f
        val x = bx + 0.012f * sin(fase)
        val y = by + 0.008f * cos(fase * 1.3f)
        val d = 0.022f + 0.008f * sin(fase * 0.7f)
        val luz = Color.White.copy(alpha = 0.16f + 0.1f * (0.5f + 0.5f * sin(fase)))
        drawLine(luz, p(x - d, y), p(x, y - d * 0.6f), size.width * 0.005f, StrokeCap.Round)
        drawLine(luz, p(x, y - d * 0.6f), p(x + d, y + d * 0.2f), size.width * 0.005f, StrokeCap.Round)
    }
    // Ondinhas da superfície.
    for (k in 0 until 4) {
        val y = MT + 0.05f + k * 0.07f
        var anterior = p(ME + 0.02f, y)
        for (i in 1..8) {
            val x = ME + 0.02f + i * (MD - ME - 0.04f) / 8
            val ponto = p(x, y + 0.008f * sin(i * 1.6f + sin(a) * 3f + k))
            drawLine(Color.White.copy(alpha = 0.45f), anterior, ponto, size.width * 0.006f, StrokeCap.Round)
            anterior = ponto
        }
    }
    // Brilho do sol na água.
    drawOval(Color.White.copy(alpha = 0.18f + 0.08f * sin(a)), p(0.30f, MT + 0.01f), s(0.16f, 0.03f))
}

// ---------------------------------------------------------------------------------------------- casa na árvore

private fun DrawScope.casaNaArvore(noite: Boolean) {
    // Tronco grosso com casca (um lado mais escuro) e raízes.
    drawOval(Color(0x26000000), p(0.38f, MT - 0.025f), s(0.24f, 0.04f))
    drawRoundRect(Color(0xFF8B5A35), p(0.43f, 0.10f), s(0.14f, MT - 0.10f), CornerRadius(size.width * 0.03f))
    drawRoundRect(Color(0xFF6E4527), p(0.52f, 0.10f), s(0.05f, MT - 0.10f), CornerRadius(size.width * 0.03f))
    for (k in 0 until 4) {
        val x = 0.45f + k * 0.03f
        drawLine(Color(0x33000000), p(x, 0.26f + (k % 2) * 0.04f), p(x + 0.004f, MT - 0.04f - (k % 3) * 0.03f), size.width * 0.004f, StrokeCap.Round)
    }
    drawOval(Color(0xFF8B5A35), p(0.39f, MT - 0.03f), s(0.08f, 0.035f))
    drawOval(Color(0xFF6E4527), p(0.53f, MT - 0.03f), s(0.08f, 0.035f))
    // Copa em dois tons: a de trás mais escura, a da frente mais clara, com luz no alto à esquerda.
    val fundoDaCopa = Color(0xFF3B8A3A)
    val frente = Color(0xFF5BAE4E)
    drawCircle(fundoDaCopa, size.width * 0.23f, p(0.52f, 0.09f))
    drawCircle(fundoDaCopa, size.width * 0.16f, p(0.27f, 0.17f))
    drawCircle(fundoDaCopa, size.width * 0.16f, p(0.74f, 0.17f))
    drawCircle(frente, size.width * 0.19f, p(0.47f, 0.08f))
    drawCircle(frente, size.width * 0.13f, p(0.29f, 0.15f))
    drawCircle(frente, size.width * 0.13f, p(0.69f, 0.15f))
    for ((x, y, r) in listOf(Triple(0.24f, 0.11f, 0.05f), Triple(0.40f, 0.03f, 0.06f), Triple(0.63f, 0.09f, 0.045f))) {
        drawCircle(Color(0xFF7CC66A), size.width * r, p(x, y))
    }
    // Plataforma de tábuas com viga embaixo e cerquinha.
    drawRect(Color(0x33000000), p(0.32f, 0.225f), s(0.36f, 0.012f))
    drawRect(Color(0xFF7A4B2A), p(0.31f, 0.205f), s(0.38f, 0.022f))
    for (k in 0 until 10) drawLine(Color(0x33000000), p(0.31f + k * 0.038f, 0.205f), p(0.31f + k * 0.038f, 0.227f), size.width * 0.003f)
    for (k in 0 until 2) drawLine(Color(0xFF6B4B2A), p(0.35f + k * 0.30f, 0.227f), p(0.43f + k * 0.14f, 0.30f), size.width * 0.012f, StrokeCap.Round)
    // A casinha de tábuas.
    tabuas(0.36f, 0.08f, 0.28f, 0.125f, Color(0xFFD9A46C), 6)
    drawRect(Color(0x22000000), p(0.58f, 0.08f), s(0.06f, 0.125f))
    val telhado = Path().apply {
        moveTo(p(0.33f, 0.085f).x, p(0.33f, 0.085f).y)
        lineTo(p(0.50f, 0.0f).x, p(0.50f, 0.0f).y)
        lineTo(p(0.67f, 0.085f).x, p(0.67f, 0.085f).y)
        close()
    }
    drawPath(telhado, Color(0xFFE5484D))
    for (k in 1 until 3) {
        val f = k / 3f
        drawLine(Color(0x33000000), p(0.50f - 0.17f * f, 0.085f * f), p(0.50f + 0.17f * f, 0.085f * f), size.width * 0.004f)
    }
    drawPath(telhado, Color(0xFFB8323A), style = Stroke(size.width * 0.008f))
    // Janela com luz (à noite acesa) e portinha.
    drawRect(Color(0xFF8B5A35), p(0.39f, 0.105f), s(0.105f, 0.075f))
    drawRect(if (noite) Color(0xFFFFE08A) else Color(0xFFA9E1FF), p(0.4f, 0.11f), s(0.085f, 0.065f))
    drawLine(Color(0xFF8B5A35), p(0.4425f, 0.11f), p(0.4425f, 0.175f), size.width * 0.006f)
    drawLine(Color.White.copy(alpha = 0.55f), p(0.405f, 0.15f), p(0.425f, 0.12f), size.width * 0.006f, StrokeCap.Round)
    drawRoundRect(Color(0xFF8B5A35), p(0.535f, 0.12f), s(0.06f, 0.085f), CornerRadius(size.width * 0.012f))
    drawCircle(Color(0xFFFFD166), size.width * 0.006f, p(0.58f, 0.165f))
    // Toalha de piquenique xadrez, com sombra e barrinha.
    drawRect(Color(0x26000000), p(ME + 0.012f, MT + 0.035f), s(MD - ME, MB - MT))
    for (i in 0 until 8) for (j in 0 until 4) {
        val c = if ((i + j) % 2 == 0) Color(0xFFE5484D) else Color(0xFFFFF6F0)
        drawRect(c, p(ME + i * (MD - ME) / 8, MT + 0.02f + j * (MB - MT) / 4), s((MD - ME) / 8, (MB - MT) / 4))
    }
    drawRect(Color(0x1A000000), p(ME, MT + 0.02f), s(MD - ME, MB - MT), style = Stroke(size.width * 0.006f))
}

/** A escada de corda balança de leve, presa na plataforma. */
private fun DrawScope.escadaDeCorda(t: Float) {
    val ang = 2.5f * sin(t * 2f * PI.toFloat())
    rotate(ang, p(0.65f, 0.227f)) {
        val topo = 0.227f
        val corda = Color(0xFFC9A27A)
        val curva = 0.012f
        // Cordas com uma barriguinha (em pedaços retos, sem montar caminho).
        for (lado in 0 until 2) {
            val x0 = 0.61f + lado * 0.07f
            var anterior = p(x0, topo)
            for (i in 1..8) {
                val f = i / 8f
                val ponto = p(x0 + 0.03f * f + curva * sin(f * PI.toFloat()), topo + (MT - 0.02f - topo) * f)
                drawLine(corda, anterior, ponto, size.width * 0.007f, StrokeCap.Round)
                anterior = ponto
            }
        }
        for (i in 1 until 7) {
            val f = i / 7f
            val y = topo + (MT - 0.02f - topo) * f
            val dx = 0.03f * f + curva * sin(f * PI.toFloat())
            drawLine(Color(0xFF6B4B2A), p(0.605f + dx, y + 0.004f), p(0.685f + dx, y + 0.004f), size.width * 0.012f, StrokeCap.Round)
            drawLine(Color(0xFF9C6B3F), p(0.605f + dx, y), p(0.685f + dx, y), size.width * 0.01f, StrokeCap.Round)
        }
    }
}

// ---------------------------------------------------------------------------------------------- observatório

private fun DrawScope.observatorio() {
    // Muretinha no fundo do terraço.
    drawRect(Color(0xFF7A3B2A), p(0f, H - 0.015f), s(1f, 0.03f))
    for (i in 0 until 12) drawRect(Color(0xFF8E4A35), p(i / 12f + 0.01f, H - 0.04f), s(0.03f, 0.03f))
    // A torre da cúpula, com portinha.
    val baseTopo = H - 0.10f
    drawRect(Brush.horizontalGradient(listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE)), startX = size.width * 0.29f, endX = size.width * 0.71f), p(0.29f, baseTopo), s(0.42f, 0.11f))
    for (k in 1 until 6) drawLine(Color(0x22000000), p(0.29f + k * 0.07f, baseTopo), p(0.29f + k * 0.07f, H + 0.01f), size.width * 0.003f)
    drawRoundRect(Color(0xFF546E7A), p(0.455f, baseTopo + 0.035f), s(0.09f, 0.075f), CornerRadius(size.width * 0.02f))
    drawCircle(Color(0xFFFFD166), size.width * 0.006f, p(0.53f, baseTopo + 0.075f))
    // A cúpula: metade de um oval, com gomos (meridianos) e a fenda aberta para o céu.
    val domoTopo = p(0.26f, baseTopo - 0.13f)
    val domoTam = s(0.48f, 0.26f)
    drawArc(Brush.horizontalGradient(listOf(Color(0xFFF5F7F8), Color(0xFFB0BEC5), Color(0xFF78909C)), startX = domoTopo.x, endX = domoTopo.x + domoTam.width), 180f, 180f, true, domoTopo, domoTam)
    for (k in 1..3) {
        val f = 1f - k * 0.27f
        val w = domoTam.width * f
        drawArc(Color(0x26000000), 180f, 180f, false, Offset(domoTopo.x + (domoTam.width - w) / 2, domoTopo.y), Size(w, domoTam.height), style = Stroke(size.width * 0.003f))
    }
    drawLine(Color(0x26000000), p(0.26f, baseTopo - 0.04f), p(0.74f, baseTopo - 0.04f), size.width * 0.003f)
    // Fenda.
    drawRect(Color(0xFF141C45), p(0.47f, baseTopo - 0.128f), s(0.06f, 0.128f))
    drawCircle(Color.White.copy(alpha = 0.8f), size.width * 0.004f, p(0.49f, baseTopo - 0.09f))
    drawCircle(Color.White.copy(alpha = 0.6f), size.width * 0.003f, p(0.51f, baseTopo - 0.05f))
    drawLine(Color(0xFF607D8B), p(0.47f, baseTopo - 0.128f), p(0.47f, baseTopo), size.width * 0.004f)
    drawLine(Color(0xFF607D8B), p(0.53f, baseTopo - 0.128f), p(0.53f, baseTopo), size.width * 0.004f)
    // Aba da cúpula.
    drawRoundRect(Color(0xFF78909C), p(0.25f, baseTopo - 0.008f), s(0.50f, 0.016f), CornerRadius(size.width * 0.008f))
    // O telescópio saindo pela fenda, apontado para as estrelas.
    val de = p(0.50f, baseTopo - 0.05f)
    val ate = p(0.68f, baseTopo - 0.20f)
    drawLine(Color(0xFF37474F), de, ate, size.width * 0.05f, StrokeCap.Round)
    drawLine(Color(0xFFECEFF1), de, ate, size.width * 0.036f, StrokeCap.Round)
    val meio = Offset((de.x + ate.x) / 2, (de.y + ate.y) / 2)
    drawLine(Color(0xFFFFC93C), Offset(meio.x - size.width * 0.012f, meio.y - size.width * 0.014f), Offset(meio.x + size.width * 0.012f, meio.y + size.width * 0.014f), size.width * 0.012f)
    drawCircle(Color(0xFF263238), size.width * 0.022f, ate)
    drawCircle(Color(0xFF81D4FA), size.width * 0.012f, ate)
    // Deque redondo de madeira, com a rosa dos ventos de mapa de estrelas.
    sombraDoMiolo(0.03f, oval = true)
    ovalMiolo(Color(0xFF8E5A34), folga = 0.03f)
    ovalMiolo(Color(0xFFC98B5A), folga = 0.0f)
    ovalMiolo(Color(0xFFD9A06A), folga = -0.06f)
    val c = p(0.5f, (MT + MB) / 2f)
    val rx = (MD - ME) * size.width / 2f
    val ry = (MB - MT) * size.height / 2f
    for (k in 0 until 8) {
        val ang = k * PI.toFloat() / 4f
        val f = if (k % 2 == 0) 0.95f else 0.6f
        drawLine(Color(0x338E5A34), c, Offset(c.x + cos(ang) * rx * f, c.y + sin(ang) * ry * f), size.width * (if (k % 2 == 0) 0.006f else 0.004f), StrokeCap.Round)
    }
    drawPath(caminhoEstrela(c, size.width * 0.03f), Color(0x66FFC93C))
}

// ---------------------------------------------------------------------------------------------- salão de festas

private val coresDaFesta = listOf(Color(0xFFFF6F6F), Color(0xFFFFC93C), Color(0xFF6BD08A), Color(0xFF6FB7FF), Color(0xFFB38CFF))

private fun DrawScope.salaoDeFestas() {
    // Bandeirinhas no alto da parede.
    for (i in 0 until 12) {
        val x = i / 12f
        val tri = Path().apply {
            moveTo(p(x, 0.04f).x, p(x, 0.04f).y)
            lineTo(p(x + 1 / 12f, 0.04f).x, p(x + 1 / 12f, 0.04f).y)
            lineTo(p(x + 0.5f / 12f, 0.10f).x, p(x + 0.5f / 12f, 0.10f).y)
            close()
        }
        drawPath(tri, coresDaFesta[i % coresDaFesta.size])
    }
    drawLine(Color(0xFF8A5A35), p(0f, 0.04f), p(1f, 0.04f), size.width * 0.004f)
    // Globo espelhado pendurado.
    val globo = p(0.5f, 0.2f)
    val r = size.width * 0.06f
    drawLine(Color(0xFFB0BEC5), p(0.5f, 0.04f), Offset(globo.x, globo.y - r), size.width * 0.004f)
    drawCircle(Color(0x1FFFFFFF), r * 1.7f, globo)
    drawCircle(Color(0xFFB0BEC5), r, globo)
    for (k in -2..2) {
        drawLine(Color(0x55FFFFFF), Offset(globo.x - r, globo.y + k * r * 0.38f), Offset(globo.x + r, globo.y + k * r * 0.38f), size.width * 0.002f)
        drawLine(Color(0x55546E7A), Offset(globo.x + k * r * 0.38f, globo.y - r), Offset(globo.x + k * r * 0.38f, globo.y + r), size.width * 0.002f)
    }
    drawCircle(Color.White.copy(alpha = 0.8f), r * 0.25f, Offset(globo.x - r * 0.4f, globo.y - r * 0.4f))
    // Brilho no chão em volta da pista, que vai sumindo para as bordas.
    val centroDaPista = p(0.5f, (MT + MB) / 2f)
    drawOval(
        Brush.radialGradient(listOf(Color(0x66FFB3E6), Color(0x22B38CFF), Color.Transparent), center = centroDaPista, radius = size.width * 0.55f),
        p(ME - 0.25f, MT - 0.16f), s(MD - ME + 0.5f, MB - MT + 0.32f),
    )
    // A pista: quadradinhos coloridos apagados (as luzes acendem na camada de cima), com moldura.
    drawRoundRect(Color(0x40000000), p(ME - 0.015f, MT + 0.015f), s(MD - ME + 0.03f, MB - MT + 0.03f), CornerRadius(size.width * 0.012f))
    drawRoundRect(Color(0xFF2B2140), p(ME - 0.01f, MT + 0.01f), s(MD - ME + 0.02f, MB - MT + 0.02f), CornerRadius(size.width * 0.012f))
    for (i in 0 until 6) for (j in 0 until 3) {
        val cor = coresDaFesta[(i + j) % coresDaFesta.size]
        drawRect(cor.copy(alpha = 0.5f), p(ME + i * (MD - ME) / 6, MT + 0.02f + j * (MB - MT) / 3), s((MD - ME) / 6 - 0.004f, (MB - MT) / 3 - 0.004f))
    }
}

private fun DrawScope.luzesDaPista(t: Float) {
    // Quadradinhos que acendem em onda.
    val passo = (t * 6).toInt()
    for (i in 0 until 6) for (j in 0 until 3) {
        if (((i + j + passo) % 3) != 0) continue
        val cor = coresDaFesta[(i + j) % coresDaFesta.size]
        val x = ME + i * (MD - ME) / 6
        val y = MT + 0.02f + j * (MB - MT) / 3
        drawRect(cor, p(x, y), s((MD - ME) / 6 - 0.004f, (MB - MT) / 3 - 0.004f))
        drawRect(Color.White.copy(alpha = 0.35f), p(x + 0.01f, y + 0.008f), s((MD - ME) / 6 - 0.03f, 0.012f))
    }
    // Fachos de luz do globo, girando devagar (faixas largas e transparentes).
    val globo = p(0.5f, 0.2f)
    for (k in 0 until 3) {
        val ang = (t * 2f * PI.toFloat()) + k * 2.1f
        val alvo = p(0.5f + 0.32f * sin(ang), MT + 0.12f + 0.08f * cos(ang * 1.3f))
        drawLine(coresDaFesta[k + 1].copy(alpha = 0.16f), globo, alvo, size.width * 0.07f, StrokeCap.Round)
        drawCircle(coresDaFesta[k + 1].copy(alpha = 0.22f), size.width * 0.06f, alvo)
    }
    // Pontinhos de brilho do globo.
    for (k in 0 until 4) {
        val a = sin(t * 2f * PI.toFloat() * 2f + k * 1.7f)
        if (a > 0.3f) drawCircle(Color.White.copy(alpha = a), size.width * 0.006f, Offset(globo.x + (k - 1.5f) * size.width * 0.025f, globo.y + ((k % 2) - 0.5f) * size.width * 0.04f))
    }
}
