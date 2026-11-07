package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Cena
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.tema.Cores

// Cenas desenhadas das perguntas (Canvas): tampinha do sudoku, material dourado e labirinto.

/**
 * Casinha tampada do sudoku: uma tampinha de madeira fechada com puxador, bem diferente de uma casa
 * sem figura (a criança não precisa descobrir o que tem embaixo).
 */
@Composable
internal fun Tampinha(lado: androidx.compose.ui.unit.Dp) {
    Canvas(Modifier.size(lado)) {
        val r = androidx.compose.ui.geometry.CornerRadius(size.minDimension * 0.17f)
        val madeira = Color(0xFFE8C48A)
        drawRoundRect(Color(0xFFC9A066), cornerRadius = r)
        drawRoundRect(madeira, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.9f), cornerRadius = r)
        // veios da madeira
        for (k in 1..3) {
            val y = size.height * (0.2f + k * 0.17f)
            drawLine(Color(0x33A0743A), Offset(size.width * 0.14f, y), Offset(size.width * 0.86f, y), size.minDimension * 0.03f, StrokeCap.Round)
        }
        // puxador
        drawCircle(Color(0xFFA0743A), size.minDimension * 0.13f, Offset(size.width / 2f, size.height * 0.45f))
        drawCircle(Color(0xFFD9A85E), size.minDimension * 0.08f, Offset(size.width / 2f - size.minDimension * 0.02f, size.height * 0.43f))
    }
}

/**
 * Material dourado: barrinhas de dez cubinhos em pé, lado a lado, e os cubinhos soltos em colunas de cinco
 * (como numa caixinha de dez), separados das barrinhas. Cada cubinho tem frente, topo e lado, como madeira.
 */
@Composable
internal fun MaterialDourado(m: Cena.MaterialDourado) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val colunasSoltas = (m.unidades + 4) / 5
        // Largura em cubinhos: cada barrinha ocupa 1,6 (com o vão), o vão do meio 1,2, cada coluna de soltos 1,75.
        val larguraEmCubos = m.dezenas * 1.6f + (if (m.unidades > 0) 1.2f + colunasSoltas * 1.75f else 0f) + 0.4f
        val cubo = minOf((maxWidth - 40.dp) / larguraEmCubos, (maxHeight - 40.dp) / 10.9f, 30.dp)
        Box(
            Modifier
                .cartaoFofo(raio = 28.dp, labio = Cores.LabioClaro)
                .padding(16.dp),
        ) {
            Canvas(Modifier.size(cubo * larguraEmCubos, cubo * 10.9f)) {
                val c = cubo.toPx()
                val prof = c * 0.32f // profundidade do topo e do lado (o "3D")
                val face = Color(0xFFF6C453)
                val topo = Color(0xFFFFE08A)
                val lado = Color(0xFFD99A2B)
                val linha = Color(0xFFB57A1C)
                val traco = c * 0.06f
                fun cubinho(x: Float, y: Float) {
                    // y é o topo da face da frente
                    val p = androidx.compose.ui.graphics.Path()
                    p.moveTo(x, y); p.lineTo(x + prof, y - prof); p.lineTo(x + c + prof, y - prof); p.lineTo(x + c, y); p.close()
                    drawPath(p, topo)
                    drawPath(p, linha, style = androidx.compose.ui.graphics.drawscope.Stroke(traco))
                    val l = androidx.compose.ui.graphics.Path()
                    l.moveTo(x + c, y); l.lineTo(x + c + prof, y - prof); l.lineTo(x + c + prof, y + c - prof); l.lineTo(x + c, y + c); l.close()
                    drawPath(l, lado)
                    drawPath(l, linha, style = androidx.compose.ui.graphics.drawscope.Stroke(traco))
                    drawRect(face, Offset(x, y), androidx.compose.ui.geometry.Size(c, c))
                    drawRect(linha, Offset(x, y), androidx.compose.ui.geometry.Size(c, c), style = androidx.compose.ui.graphics.drawscope.Stroke(traco))
                    drawLine(Color(0x88FFFFFF), Offset(x + c * 0.18f, y + c * 0.2f), Offset(x + c * 0.18f, y + c * 0.55f), c * 0.08f, StrokeCap.Round)
                }
                val chao = size.height - c * 0.1f
                var x = c * 0.1f
                // barrinhas: dez cubinhos colados, de baixo para cima (o de cima por último, para o topo aparecer)
                repeat(m.dezenas) {
                    for (k in 0 until 10) cubinho(x, chao - c * (k + 1))
                    x += c * 1.6f
                }
                if (m.unidades > 0) {
                    x += c * 1.2f
                    var restam = m.unidades
                    while (restam > 0) {
                        val nessa = minOf(5, restam)
                        // soltos com um respiro entre eles: dá para contar um por um
                        for (k in 0 until nessa) cubinho(x, chao - c - k * c * 1.55f)
                        restam -= nessa
                        x += c * 1.75f
                    }
                }
            }
        }
    }
}

/**
 * Labirinto: um gramado com árvores e um caminho de terra com pedrinhas, da criança (no começo) até a casinha
 * (na chegada). O quadro já vem justo ao caminho, então quase não sobra mato.
 */
@Composable
internal fun Labirinto(lab: Cena.Labirinto, perfil: Perfil?) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val lado = minOf((maxWidth - 32.dp) / lab.colunas, (maxHeight - 32.dp) / lab.linhas, 104.dp)
        val caminho = lab.caminho.map { it / lab.colunas to it % lab.colunas }
        // Tabuleiro de grama como um bloco de brinquedo: lábio verde-escuro embaixo e borda branca.
        Box(
            Modifier
                .cartaoFofo(cor = Color(0xFF7CC35A), raio = 24.dp, labio = Color(0xFF4E9137), profundidade = 6.dp, contorno = Color.White)
                .padding(10.dp),
        ) {
            Canvas(Modifier.size(lado * lab.colunas, lado * lab.linhas)) {
                val l = lado.toPx()
                fun centro(cel: Pair<Int, Int>) = Offset((cel.second + 0.5f) * l, (cel.first + 0.5f) * l)
                // gramado com tufinhos
                drawRoundRect(Color(0xFF9BD770), cornerRadius = androidx.compose.ui.geometry.CornerRadius(l * 0.2f))
                for (lin in 0 until lab.linhas) for (col in 0 until lab.colunas) {
                    val o = Offset(col * l, lin * l)
                    drawCircle(Color(0x338BC34A), l * 0.05f, Offset(o.x + l * 0.2f, o.y + l * 0.82f))
                    drawCircle(Color(0x338BC34A), l * 0.04f, Offset(o.x + l * 0.8f, o.y + l * 0.15f))
                }
                // caminho de terra (borda mais escura por baixo, depois o miolo claro)
                val trilha = androidx.compose.ui.graphics.Path().apply {
                    caminho.forEachIndexed { i, cel -> val p = centro(cel); if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) }
                }
                drawPath(trilha, Color(0xFFC99A5B), style = androidx.compose.ui.graphics.drawscope.Stroke(l * 0.74f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                drawPath(trilha, Color(0xFFF3DCA6), style = androidx.compose.ui.graphics.drawscope.Stroke(l * 0.62f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                // uma pedrinha em cada casinha entre o começo e a chegada (dá para contar)
                for (i in 1 until caminho.size - 1) {
                    val p = centro(caminho[i])
                    drawOval(Color(0xFFB8A48A), Offset(p.x - l * 0.17f, p.y - l * 0.09f), androidx.compose.ui.geometry.Size(l * 0.34f, l * 0.22f))
                    drawOval(Color(0xFFE4D9C8), Offset(p.x - l * 0.16f, p.y - l * 0.12f), androidx.compose.ui.geometry.Size(l * 0.32f, l * 0.2f))
                }
                // árvores no mato
                val noCaminho = caminho.toSet()
                for (lin in 0 until lab.linhas) for (col in 0 until lab.colunas) {
                    if ((lin to col) in noCaminho) continue
                    val c = centro(lin to col)
                    drawOval(Color(0x2A2E5A1C), Offset(c.x - l * 0.28f, c.y + l * 0.24f), androidx.compose.ui.geometry.Size(l * 0.56f, l * 0.14f))
                    drawRoundRect(Color(0xFF8B5A33), Offset(c.x - l * 0.06f, c.y + l * 0.02f), androidx.compose.ui.geometry.Size(l * 0.12f, l * 0.28f), androidx.compose.ui.geometry.CornerRadius(l * 0.03f))
                    val copa = if ((lin + col) % 2 == 0) Color(0xFF3E9B3A) else Color(0xFF4CAF50)
                    drawCircle(copa, l * 0.2f, Offset(c.x - l * 0.12f, c.y - l * 0.04f))
                    drawCircle(copa, l * 0.2f, Offset(c.x + l * 0.12f, c.y - l * 0.04f))
                    drawCircle(copa, l * 0.23f, Offset(c.x, c.y - l * 0.18f))
                    drawCircle(Color(0x55FFFFFF), l * 0.07f, Offset(c.x - l * 0.07f, c.y - l * 0.26f))
                }
                // a casinha de chegada
                val f = centro(caminho.last())
                val s = l * 0.36f
                drawOval(Color(0x33000000), Offset(f.x - s * 0.9f, f.y + s * 0.62f), androidx.compose.ui.geometry.Size(s * 1.8f, s * 0.3f))
                drawRect(Color(0xFFFFF1D6), Offset(f.x - s * 0.7f, f.y - s * 0.1f), androidx.compose.ui.geometry.Size(s * 1.4f, s * 0.85f))
                val telhado = androidx.compose.ui.graphics.Path().apply {
                    moveTo(f.x - s * 0.95f, f.y - s * 0.05f); lineTo(f.x, f.y - s * 0.9f); lineTo(f.x + s * 0.95f, f.y - s * 0.05f); close()
                }
                drawPath(telhado, Color(0xFFEF476F))
                drawPath(telhado, Color(0xFFB5244A), style = androidx.compose.ui.graphics.drawscope.Stroke(l * 0.025f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
                drawRoundRect(Color(0xFF8B5A33), Offset(f.x - s * 0.2f, f.y + s * 0.3f), androidx.compose.ui.geometry.Size(s * 0.4f, s * 0.45f), androidx.compose.ui.geometry.CornerRadius(s * 0.08f))
                drawRect(Color(0xFF8FD3FF), Offset(f.x + s * 0.3f, f.y + s * 0.08f), androidx.compose.ui.geometry.Size(s * 0.28f, s * 0.24f))
                drawCircle(Color(0xFFFFD43B), s * 0.2f, Offset(f.x, f.y - s * 0.38f))
            }
            // a criança no começo do caminho
            val (li, ci) = caminho.first()
            Box(
                Modifier
                    .padding(start = lado * ci, top = lado * li)
                    .size(lado),
                contentAlignment = Alignment.Center,
            ) {
                if (perfil != null) {
                    Crianca(perfil, Modifier.height(lado * 0.92f))
                } else {
                    Text("🧒", fontSize = (lado.value * 0.6f).sp)
                }
            }
        }
    }
}

