package br.com.casinhadosaber.ui.componentes

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Projeção isométrica 2:1 (o estilo "2,5D" dos jogos de casinha). Converte ladrilhos (x, y) e altura (z)
 * em pixels, e pixels do chão de volta em ladrilhos (para tocar no chão e andar).
 *
 * [ladrilho] é a largura de um ladrilho na tela; ([origemX], [origemY]) é onde fica o canto (0, 0) do chão.
 */
class Projecao(val ladrilho: Float, val origemX: Float, val origemY: Float) {
    val meioL = ladrilho / 2f
    val meioA = ladrilho / 4f

    /** Uma unidade de altura na tela (um pouco maior que a aresta do ladrilho, para os móveis "crescerem"). */
    val unidadeZ = ladrilho * 0.56f

    fun x(gx: Float, gy: Float): Float = origemX + (gx - gy) * meioL
    fun y(gx: Float, gy: Float, z: Float = 0f): Float = origemY + (gx + gy) * meioA - z * unidadeZ
    fun ponto(gx: Float, gy: Float, z: Float = 0f) = Offset(x(gx, gy), y(gx, gy, z))

    /** Ponto da tela (no chão) de volta para ladrilhos. */
    fun gradeX(sx: Float, sy: Float): Float = ((sx - origemX) / meioL + (sy - origemY) / meioA) / 2f
    fun gradeY(sx: Float, sy: Float): Float = ((sy - origemY) / meioA - (sx - origemX) / meioL) / 2f

    fun quad(a: Offset, b: Offset, c: Offset, d: Offset): Path = Path().apply {
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        lineTo(d.x, d.y)
        close()
    }

    /** Losango deitado na altura [z] (piso, tampo de mesa, água da banheira). */
    fun piso(x0: Float, y0: Float, x1: Float, y1: Float, z: Float = 0f): Path =
        quad(ponto(x0, y0, z), ponto(x1, y0, z), ponto(x1, y1, z), ponto(x0, y1, z))

    /** Acrescenta um losango a um caminho que já existe (vários ladrilhos num caminho só). */
    fun Path.losango(x0: Float, y0: Float, x1: Float, y1: Float, z: Float = 0f) {
        val a = ponto(x0, y0, z); val b = ponto(x1, y0, z); val c = ponto(x1, y1, z); val d = ponto(x0, y1, z)
        moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); lineTo(d.x, d.y); close()
    }

    /** Face em pé no plano x = [x] (a câmera vê o lado de +x). */
    fun paredeX(x: Float, y0: Float, y1: Float, z0: Float, z1: Float): Path =
        quad(ponto(x, y0, z0), ponto(x, y1, z0), ponto(x, y1, z1), ponto(x, y0, z1))

    /** Face em pé no plano y = [y] (a câmera vê o lado de +y). */
    fun paredeY(y: Float, x0: Float, x1: Float, z0: Float, z1: Float): Path =
        quad(ponto(x0, y, z0), ponto(x1, y, z0), ponto(x1, y, z1), ponto(x0, y, z1))

    /** Retângulo da tela que contém a caixa (para tocar). */
    fun contorno(x0: Float, y0: Float, x1: Float, y1: Float, z0: Float, z1: Float): Rect =
        Rect(x(x0, y1), y(x0, y0, z1), x(x1, y0), y(x1, y1, z0))
}

/** Um pedaço do desenho já pronto: o caminho, a cor e se leva contorno. Calculado uma vez, desenhado muitas. */
class Forma(val caminho: Path, val cor: Color, val contorno: Boolean = true)

fun clarear(c: Color, f: Float) = Color(c.red + (1 - c.red) * f, c.green + (1 - c.green) * f, c.blue + (1 - c.blue) * f, c.alpha)

/** Caixa sólida: face +y, face +x (na sombra) e o topo (iluminado). Na ordem certa para desenhar. */
fun Projecao.caixa(x0: Float, y0: Float, x1: Float, y1: Float, z0: Float, z1: Float, cor: Color, contorno: Boolean = true): List<Forma> =
    listOf(
        Forma(paredeY(y1, x0, x1, z0, z1), cor, contorno),
        Forma(paredeX(x1, y0, y1, z0, z1), escurecer(cor, 0.84f), contorno),
        Forma(piso(x0, y0, x1, y1, z1), clarear(cor, 0.14f), contorno),
    )

/** Desenha formas prontas, sem alocar nada (laço por índice). */
fun DrawScope.desenharFormas(formas: List<Forma>, corDoContorno: Color, traco: Stroke) {
    for (i in formas.indices) {
        val f = formas[i]
        drawPath(f.caminho, f.cor)
        if (f.contorno) drawPath(f.caminho, corDoContorno, style = traco)
    }
}

/** Escurece formas já desenhadas (cômodo com a luz apagada). */
fun DrawScope.sombrearFormas(formas: List<Forma>, sombra: Color) {
    for (i in formas.indices) drawPath(formas[i].caminho, sombra)
}
