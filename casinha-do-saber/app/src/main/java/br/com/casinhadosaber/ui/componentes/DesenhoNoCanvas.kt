package br.com.casinhadosaber.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import br.com.casinhadosaber.dominio.Carimbo
import br.com.casinhadosaber.dominio.DesenhoDeColorir
import br.com.casinhadosaber.dominio.Fundo
import br.com.casinhadosaber.dominio.Forma
import br.com.casinhadosaber.dominio.Obra
import br.com.casinhadosaber.dominio.Tinta
import br.com.casinhadosaber.dominio.TipoDeTinta
import br.com.casinhadosaber.dominio.Tintas
import br.com.casinhadosaber.dominio.Traco

/** Cor de papel das regiões ainda sem tinta. */
private val Papel = Color.White
private val ContornoDoDesenho = Color(0xFF3D2C5E)
private val ArcoIris = listOf(
    Color(0xFFEF476F), Color(0xFFFF9F1C), Color(0xFFFFD166), Color(0xFF3DBE6B), Color(0xFF4D96FF), Color(0xFF9B6BFF),
)

fun corDaTinta(t: Tinta): Color = Color(t.argb)

/** O caminho da região no quadrado de lado [lado] com o canto em [origem]. */
private fun caminhoDa(f: Forma, origem: Offset, lado: Float): Path = Path().apply {
    when (f) {
        is Forma.Elipse -> addOval(
            Rect(
                origem.x + (f.cx - f.rx) * lado, origem.y + (f.cy - f.ry) * lado,
                origem.x + (f.cx + f.rx) * lado, origem.y + (f.cy + f.ry) * lado,
            ),
        )
        is Forma.Poligono -> {
            f.pontos.forEachIndexed { i, p ->
                val x = origem.x + p.x * lado
                val y = origem.y + p.y * lado
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
    }
}

private fun limites(f: Forma): Rect = when (f) {
    is Forma.Elipse -> Rect(f.cx - f.rx, f.cy - f.ry, f.cx + f.rx, f.cy + f.ry)
    is Forma.Poligono -> Rect(f.pontos.minOf { it.x }, f.pontos.minOf { it.y }, f.pontos.maxOf { it.x }, f.pontos.maxOf { it.y })
}

/** Enche o [caminho] com a tinta [t]: lisa, metálica (degradê de brilho), arco-íris ou brilhante (com estrelinhas). */
private fun DrawScope.encher(caminho: Path, caixa: Rect, origem: Offset, lado: Float, t: Tinta, indice: Int) {
    val base = corDaTinta(t)
    val x0 = origem.x + caixa.left * lado
    val y0 = origem.y + caixa.top * lado
    val x1 = origem.x + caixa.right * lado
    val y1 = origem.y + caixa.bottom * lado
    when (t.tipo) {
        TipoDeTinta.LISA -> drawPath(caminho, base)
        TipoDeTinta.METAL -> drawPath(
            caminho,
            Brush.linearGradient(
                listOf(androidx.compose.ui.graphics.lerp(base, Color.White, 0.55f), base, androidx.compose.ui.graphics.lerp(base, Color.Black, 0.25f), base),
                start = Offset(x0, y0), end = Offset(x1, y1),
            ),
        )
        TipoDeTinta.ARCO_IRIS -> drawPath(caminho, Brush.linearGradient(ArcoIris, start = Offset(x0, y0), end = Offset(x1, y1)))
        TipoDeTinta.BRILHANTE -> {
            drawPath(caminho, base)
            // Estrelinhas brancas em lugares fixos da região (sempre as mesmas, para não piscar).
            clipPath(caminho) {
                for (k in 0 until 7) {
                    val fx = ((indice * 37 + k * 53) % 97) / 97f
                    val fy = ((indice * 71 + k * 29) % 89) / 89f
                    val cx = x0 + (x1 - x0) * fx
                    val cy = y0 + (y1 - y0) * fy
                    val r = lado * (0.008f + (k % 3) * 0.004f)
                    drawCircle(Color.White.copy(alpha = 0.85f), r, Offset(cx, cy))
                }
            }
        }
    }
}

private fun caminhoDoTraco(t: Traco, origem: Offset, lado: Float): Path = Path().apply {
    t.pontos.forEachIndexed { i, p ->
        val x = origem.x + p.x * lado
        val y = origem.y + p.y * lado
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    // Um toque sem arrastar vira uma bolinha (um traço de um ponto só).
    if (t.pontos.size == 1) lineTo(origem.x + t.pontos[0].x * lado + 0.1f, origem.y + t.pontos[0].y * lado)
}

/** O fundo da obra, no quadrado inteiro, atrás das regiões. */
private fun DrawScope.desenharFundo(f: Fundo, origem: Offset, lado: Float) {
    if (f == Fundo.NENHUM) return
    val tl = origem
    val tamanho = Size(lado, lado)
    fun nuvem(cx: Float, cy: Float, r: Float) {
        val branco = Color.White.copy(alpha = 0.9f)
        drawCircle(branco, r * lado, Offset(origem.x + cx * lado, origem.y + cy * lado))
        drawCircle(branco, r * 0.8f * lado, Offset(origem.x + (cx - r * 1.1f) * lado, origem.y + (cy + r * 0.25f) * lado))
        drawCircle(branco, r * 0.8f * lado, Offset(origem.x + (cx + r * 1.1f) * lado, origem.y + (cy + r * 0.25f) * lado))
    }
    when (f) {
        Fundo.NENHUM -> Unit
        Fundo.CEU -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8ED1FF), Color(0xFFDFF3FF)), startY = tl.y, endY = tl.y + lado), tl, tamanho)
            drawCircle(Color(0xFFFFD166), lado * 0.08f, Offset(tl.x + lado * 0.86f, tl.y + lado * 0.13f))
            nuvem(0.18f, 0.14f, 0.05f)
            nuvem(0.62f, 0.08f, 0.04f)
            nuvem(0.82f, 0.86f, 0.045f)
        }
        Fundo.CAMPO -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8ED1FF), Color(0xFFDFF3FF)), startY = tl.y, endY = tl.y + lado * 0.62f), tl, Size(lado, lado * 0.62f))
            drawRect(Brush.verticalGradient(listOf(Color(0xFF8BD96B), Color(0xFF3DBE6B)), startY = tl.y + lado * 0.62f, endY = tl.y + lado), Offset(tl.x, tl.y + lado * 0.62f), Size(lado, lado * 0.38f))
            drawCircle(Color(0xFFFFD166), lado * 0.07f, Offset(tl.x + lado * 0.86f, tl.y + lado * 0.12f))
            nuvem(0.2f, 0.12f, 0.045f)
            // Florzinhas no gramado.
            for (k in 0 until 6) {
                val c = Offset(tl.x + lado * (0.08f + k * 0.17f), tl.y + lado * (0.9f - (k % 2) * 0.06f))
                drawCircle(if (k % 2 == 0) Color(0xFFFF6FAE) else Color(0xFFFFD166), lado * 0.014f, c)
            }
        }
        Fundo.MAR -> {
            drawRect(Brush.verticalGradient(listOf(Color(0xFFBFE8FF), Color(0xFF8ED1FF)), startY = tl.y, endY = tl.y + lado * 0.45f), tl, Size(lado, lado * 0.45f))
            drawRect(Brush.verticalGradient(listOf(Color(0xFF4D96FF), Color(0xFF2E6FD8)), startY = tl.y + lado * 0.45f, endY = tl.y + lado), Offset(tl.x, tl.y + lado * 0.45f), Size(lado, lado * 0.55f))
            val onda = Stroke(width = lado * 0.012f, cap = StrokeCap.Round)
            for (linha in 0 until 3) {
                val y = tl.y + lado * (0.58f + linha * 0.14f)
                val p = Path().apply {
                    moveTo(tl.x, y)
                    var x = 0f
                    while (x < 1f) {
                        quadraticTo(tl.x + (x + 0.05f) * lado, y - lado * 0.03f, tl.x + (x + 0.1f) * lado, y)
                        x += 0.1f
                    }
                }
                drawPath(p, Color.White.copy(alpha = 0.55f), style = onda)
            }
            nuvem(0.75f, 0.12f, 0.045f)
        }
    }
}

/** O desenho de um carimbo com centro em [c] e diâmetro [d] (em pixels), na cor [cor]. Tudo vetorial, sem fonte. */
fun DrawScope.desenharCarimbo(tipo: Int, c: Offset, d: Float, cor: Color) {
    val r = d / 2f
    val borda = Stroke(width = (d * 0.06f).coerceAtLeast(1f), join = StrokeJoin.Round)
    val contorno = ContornoDoDesenho.copy(alpha = 0.55f)
    when (tipo) {
        0 -> { // estrelinha de 5 pontas
            val p = Path()
            for (i in 0 until 10) {
                val ang = -Math.PI / 2 + i * Math.PI / 5
                val raio = if (i % 2 == 0) r else r * 0.45f
                val x = c.x + (raio * kotlin.math.cos(ang)).toFloat()
                val y = c.y + (raio * kotlin.math.sin(ang)).toFloat()
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            p.close()
            drawPath(p, cor)
            drawPath(p, contorno, style = borda)
        }
        1 -> { // coração
            val p = Path().apply {
                moveTo(c.x, c.y + r * 0.85f)
                cubicTo(c.x - r * 1.25f, c.y + r * 0.05f, c.x - r * 0.75f, c.y - r * 1.0f, c.x, c.y - r * 0.4f)
                cubicTo(c.x + r * 0.75f, c.y - r * 1.0f, c.x + r * 1.25f, c.y + r * 0.05f, c.x, c.y + r * 0.85f)
                close()
            }
            drawPath(p, cor)
            drawPath(p, contorno, style = borda)
        }
        2 -> { // flor: cinco pétalas e o miolo amarelo
            for (i in 0 until 5) {
                val ang = -Math.PI / 2 + i * 2 * Math.PI / 5
                val pc = Offset(c.x + (r * 0.55f * kotlin.math.cos(ang)).toFloat(), c.y + (r * 0.55f * kotlin.math.sin(ang)).toFloat())
                drawCircle(cor, r * 0.42f, pc)
                drawCircle(contorno, r * 0.42f, pc, style = borda)
            }
            drawCircle(Color(0xFFFFD166), r * 0.32f, c)
            drawCircle(contorno, r * 0.32f, c, style = borda)
        }
        3 -> { // pegadas: a almofada e quatro dedinhos
            drawOval(cor, Offset(c.x - r * 0.45f, c.y - r * 0.05f), Size(r * 0.9f, r * 0.8f))
            for ((dx, dy) in listOf(-0.62f to -0.3f, -0.22f to -0.68f, 0.22f to -0.68f, 0.62f to -0.3f)) {
                drawCircle(cor, r * 0.2f, Offset(c.x + dx * r, c.y + dy * r))
            }
        }
        4 -> { // lua crescente
            val lua = Path().apply { addOval(Rect(c.x - r, c.y - r, c.x + r, c.y + r)) }
            val corte = Path().apply { addOval(Rect(c.x - r * 0.35f, c.y - r * 1.05f, c.x + r * 1.35f, c.y + r * 0.65f)) }
            val p = Path().apply { op(lua, corte, androidx.compose.ui.graphics.PathOperation.Difference) }
            drawPath(p, cor)
            drawPath(p, contorno, style = borda)
        }
        else -> { // borboleta: quatro asas e o corpinho
            for ((dx, dy, s) in listOf(Triple(-0.48f, -0.32f, 0.5f), Triple(0.48f, -0.32f, 0.5f), Triple(-0.38f, 0.38f, 0.36f), Triple(0.38f, 0.38f, 0.36f))) {
                val asa = Rect(c.x + dx * r - s * r, c.y + dy * r - s * r * 0.85f, c.x + dx * r + s * r, c.y + dy * r + s * r * 0.85f)
                drawOval(cor, asa.topLeft, asa.size)
                drawOval(contorno, asa.topLeft, asa.size, style = borda)
            }
            drawOval(ContornoDoDesenho, Offset(c.x - r * 0.1f, c.y - r * 0.7f), Size(r * 0.2f, r * 1.4f))
        }
    }
}

private fun DrawScope.desenharCarimbos(carimbos: List<Carimbo>, origem: Offset, lado: Float) {
    for (k in carimbos) {
        desenharCarimbo(k.tipo, Offset(origem.x + k.x * lado, origem.y + k.y * lado), k.tamanho * lado, corDaTinta(Tintas.todas[k.tinta]))
    }
}

/**
 * Desenha a [obra] do [desenho] num quadrado de lado [lado] com o canto em [origem]: para cada região (de baixo para
 * cima) a tinta, o pincel preso a ela e o contorno. Com [soContorno] as regiões ficam todas brancas.
 * [realce] = região que pisca (a dica do "qual pintar agora").
 */
fun DrawScope.desenharObra(
    desenho: DesenhoDeColorir,
    obra: Obra,
    lado: Float,
    origem: Offset = Offset.Zero,
    soContorno: Boolean = false,
    realce: Int = -1,
) {
    if (!soContorno) desenharFundo(obra.fundo, origem, lado)
    desenho.regioes.forEachIndexed { i, regiao ->
        val caminho = caminhoDa(regiao.forma, origem, lado)
        val tinta = if (soContorno) 0 else obra.tintas.getOrElse(i) { 0 }
        // Com um fundo escolhido, as regiões de cenário (as que vão de um lado ao outro do quadrado, como o céu e o
        // chão de muitos desenhos) ficam transparentes enquanto não têm tinta e deixam o fundo aparecer. As figuras
        // continuam em papel branco, para os contornos de trás não aparecerem através delas.
        val cenario = obra.fundo != Fundo.NENHUM && !soContorno && limites(regiao.forma).width >= 0.95f
        if (tinta == 0) { if (!cenario) drawPath(caminho, Papel) } else encher(caminho, limites(regiao.forma), origem, lado, Tintas.todas[tinta], i)
        if (!soContorno) {
            for (t in obra.tracos) {
                if (t.regiao != i) continue
                clipPath(caminho) {
                    val cor = Tintas.todas[t.tinta]
                    val traco = caminhoDoTraco(t, origem, lado)
                    val estilo = Stroke(width = t.largura * lado, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    if (cor.tipo == TipoDeTinta.LISA || cor.tipo == TipoDeTinta.BRILHANTE) {
                        drawPath(traco, corDaTinta(cor), style = estilo)
                    } else if (cor.tipo == TipoDeTinta.ARCO_IRIS) {
                        drawPath(traco, Brush.linearGradient(ArcoIris, start = origem, end = Offset(origem.x + lado, origem.y + lado)), style = estilo)
                    } else {
                        drawPath(traco, Brush.linearGradient(listOf(Color.White, corDaTinta(cor), Color.Black.copy(alpha = 0.4f)), start = origem, end = Offset(origem.x + lado, origem.y + lado)), style = estilo)
                    }
                }
            }
        }
        if (i == realce) drawPath(caminho, Color(0xFFFFD166).copy(alpha = 0.6f))
        drawPath(caminho, ContornoDoDesenho, style = Stroke(width = (lado * 0.010f).coerceAtLeast(1.5f), join = StrokeJoin.Round))
    }
    // Os traços livres (sem região) ficam por cima de tudo, sem o contorno por cima.
    for (t in obra.tracos) {
        if (t.regiao != -1) continue
        val cor = Tintas.todas[t.tinta]
        val estilo = Stroke(width = t.largura * lado, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(caminhoDoTraco(t, origem, lado), corDaTinta(cor), style = estilo)
    }
    // Os carimbos são o último enfeite, por cima de tudo.
    if (!soContorno) desenharCarimbos(obra.carimbos, origem, lado)
}

/** O desenho num quadrado que ocupa o espaço do [modifier]. Miniaturas, prêmio e galeria. */
@Composable
fun DesenhoNoCanvas(
    desenho: DesenhoDeColorir,
    obra: Obra,
    modifier: Modifier = Modifier,
    soContorno: Boolean = false,
    realce: Int = -1,
) {
    Canvas(modifier) {
        val lado = minOf(size.width, size.height)
        desenharObra(desenho, obra, lado, Offset((size.width - lado) / 2f, (size.height - lado) / 2f), soContorno, realce)
    }
}

/** Uma peça de quebra-cabeça: o pedaço (coluna, linha) de uma grade [colunas] por [linhas] da obra. */
@Composable
fun PecaDeQuebraCabeca(
    desenho: DesenhoDeColorir,
    obra: Obra,
    coluna: Int,
    linha: Int,
    colunas: Int,
    linhas: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.fillMaxSize()) {
        // A peça mostra o pedaço da arte inteira que cai na casa dela; a arte tem o tamanho de toda a moldura.
        val larguraDaPeca = size.width
        val alturaDaPeca = size.height
        val arte = minOf(larguraDaPeca * colunas, alturaDaPeca * linhas)
        val deslocamento = Offset(-coluna * larguraDaPeca, -linha * alturaDaPeca)
        clipPath(Path().apply { addRect(Rect(Offset.Zero, Size(larguraDaPeca, alturaDaPeca))) }) {
            desenharObra(desenho, obra, arte, deslocamento)
        }
    }
}
