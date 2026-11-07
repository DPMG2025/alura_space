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
import br.com.casinhadosaber.dominio.DesenhoDeColorir
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
    desenho.regioes.forEachIndexed { i, regiao ->
        val caminho = caminhoDa(regiao.forma, origem, lado)
        val tinta = if (soContorno) 0 else obra.tintas.getOrElse(i) { 0 }
        if (tinta == 0) drawPath(caminho, Papel) else encher(caminho, limites(regiao.forma), origem, lado, Tintas.todas[tinta], i)
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
