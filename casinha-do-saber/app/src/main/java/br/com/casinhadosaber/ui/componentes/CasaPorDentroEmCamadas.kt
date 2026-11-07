package br.com.casinhadosaber.ui.componentes

import android.content.Context
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import br.com.casinhadosaber.dominio.PlantaDaCasa
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.Canvas as ComposeCanvas

/** Uma imagem da arte já decodificada no tamanho de uso e o lugar dela na tela. */
class SpriteNaTela(val imagem: ImageBitmap, val lugar: LugarDaArte, val info: InfoDaArte)

/**
 * O fundo fixo de um andar composto num bitmap só. ([esquerda], [topo]) é o canto do bitmap em relação ao canto
 * (0, 0) do chão do andar, em pixels do bitmap; [ladrilho] é o ladrilho do bitmap em pixels (no máximo o da arte).
 */
class FundoDoAndar(val imagem: ImageBitmap, val esquerda: Float, val topo: Float, val ladrilho: Float)

/** Desenha uma imagem da arte no lugar dela (com um filtro opcional para tingir ou escurecer). */
fun DrawScope.desenharSprite(s: SpriteNaTela, filtro: ColorFilter? = null) {
    val l = s.lugar
    val x0 = l.esquerda.roundToInt()
    val y0 = l.topo.roundToInt()
    drawImage(
        image = s.imagem,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(s.imagem.width, s.imagem.height),
        dstOffset = IntOffset(x0, y0),
        dstSize = IntSize((l.direita.roundToInt() - x0).coerceAtLeast(1), (l.baixo.roundToInt() - y0).coerceAtLeast(1)),
        colorFilter = filtro,
        filterQuality = FilterQuality.Low,
    )
}

/** Multiplicar pela cor (tingir mantendo a luz e a sombra da arte, como nos personagens e na casa de fora). */
fun multiplicarPor(cor: Long): ColorFilter = ColorFilter.tint(Color(cor), BlendMode.Modulate)

/**
 * A arte 3D da casa por dentro, guardada só enquanto a tela está aberta (nada de bitmap em objeto estático: quem
 * cria é a tela, num `remember`, e solta ao sair). Guarda:
 * * as imagens dos móveis, decodificadas já reduzidas para o tamanho em que aparecem (poucas dezenas, pequenas);
 * * o fundo fixo de cada andar (laje, pisos, tapete, paredes tingidas, papel, janelas, porta e acabamento)
 *   composto UMA vez num bitmap por (andar, aparência, tamanho), no máximo [MAX_FUNDOS] (um por andar). As
 *   camadas do fundo são lidas sem cache e recicladas logo depois de desenhadas no composto.
 */
class ArteDaCasaPorDentro(val contexto: Context) {
    val manifesto: ManifestoDaArte = ArteDoApp.manifesto(contexto)
    val escala: Int get() = manifesto.escala

    fun info(id: String?): InfoDaArte? = id?.let { manifesto.imagens[it] }

    private val imagens = HashMap<String, ImageBitmap>()
    private val falhas = HashSet<String>()

    private data class ChaveDoFundo(val aparencia: AparenciaDoAndar, val ladrilho: Int)

    private val fundos = LinkedHashMap<ChaveDoFundo, FundoDoAndar?>()

    /** A imagem [info] decodificada para aparecer [k] vezes o tamanho da arte (nula se faltar ou não der para ler). */
    fun imagem(info: InfoDaArte, k: Float): ImageBitmap? {
        val alvoL = (info.largura * k).roundToInt().coerceAtLeast(1)
        val alvoA = (info.altura * k).roundToInt().coerceAtLeast(1)
        val amostra = amostraDaArte(info.largura, info.altura, alvoL, alvoA)
        val chave = "${info.id}@$amostra"
        imagens[chave]?.let { return it }
        if (chave in falhas) return null
        if (imagens.size >= MAX_IMAGENS) imagens.clear()
        val bmp = try {
            ArteDoApp.decodificar(contexto, info, amostra)
        } catch (e: OutOfMemoryError) {
            null
        }
        if (bmp == null) {
            falhas += chave
            return null
        }
        bmp.prepareToDraw()
        return bmp.asImageBitmap().also { imagens[chave] = it }
    }

    /** A imagem [id] com o apoio na [referencia] da planta, para a projeção dada; nula se faltar. */
    fun sprite(id: String, ladrilho: Float, origemX: Float, origemY: Float, referencia: PontoDaPlanta): SpriteNaTela? {
        val info = info(id) ?: return null
        val lugar = CamadasDaCasaPorDentro.lugar(info, escala, ladrilho, origemX, origemY, referencia)
        val img = imagem(info, lugar.largura / info.largura) ?: return null
        return SpriteNaTela(img, lugar, info)
    }

    /**
     * O fundo do andar com esta [aparencia] para um ladrilho de [ladrilhoNaTela] px, composto uma vez (ou lido do
     * cache). Uma casca sem imagem (ou ilegível) e o papel de parede vêm de [vetorial] (as formas do desenho antigo,
     * na projeção do bitmap), com contorno [corDoContorno] e traço de [tracoNaTela] px na tela. Nulo se nem o
     * bitmap couber na memória: aí a tela desenha tudo em vetor.
     */
    fun fundo(
        aparencia: AparenciaDoAndar,
        ladrilhoNaTela: Float,
        corDoContorno: Color,
        tracoNaTela: Float,
        vetorial: (CascaDoAndar, Projecao) -> List<Forma>,
    ): FundoDoAndar? {
        val cascas = CamadasDaCasaPorDentro.cascas(aparencia)
        val daArte = info(cascas.first().id)?.let { CamadasDaCasaPorDentro.ladrilhoDaArte(it, escala) } ?: 128f
        // Nunca maior que a arte: ampliar não ganha nitidez, só gasta memória (o composto é esticado na tela).
        val tb = min(ladrilhoNaTela, daArte).roundToInt().coerceAtLeast(8)
        val chave = ChaveDoFundo(aparencia, tb)
        if (fundos.containsKey(chave)) return fundos[chave]
        val fundo = compor(cascas, tb.toFloat(), corDoContorno, tracoNaTela * tb / ladrilhoNaTela, vetorial)
        while (fundos.size >= MAX_FUNDOS) fundos.remove(fundos.keys.first())
        fundos[chave] = fundo
        return fundo
    }

    private fun compor(
        cascas: List<CascaDoAndar>,
        tb: Float,
        corDoContorno: Color,
        traco: Float,
        vetorial: (CascaDoAndar, Projecao) -> List<Forma>,
    ): FundoDoAndar? {
        // Limites: o andar inteiro (laje, paredes e acabamento) e cada imagem, com o chão em (0, 0).
        val l = PlantaDaCasa.LARGURA.toFloat()
        val f = PlantaDaCasa.FUNDO.toFloat()
        val h = PlantaDaCasa.ALTURA_PAREDE
        val cantos = listOf(
            PontoDaPlanta(0f, f, -0.35f), PontoDaPlanta(l, 0f, -0.35f), PontoDaPlanta(l, f, -0.35f),
            PontoDaPlanta(-0.14f, -0.14f, h), PontoDaPlanta(l, -0.14f, h), PontoDaPlanta(-0.14f, f, h),
        )
        var esq = Float.MAX_VALUE
        var topo = Float.MAX_VALUE
        var dir = -Float.MAX_VALUE
        var baixo = -Float.MAX_VALUE
        for (p in cantos) {
            val x = GradeIsometrica.x(tb, 0f, p.x, p.y)
            val y = GradeIsometrica.y(tb, 0f, p.x, p.y, p.z)
            esq = min(esq, x); dir = max(dir, x); topo = min(topo, y); baixo = max(baixo, y)
        }
        val lugares = cascas.map { c ->
            info(c.id)?.let { CamadasDaCasaPorDentro.lugar(it, escala, tb, 0f, 0f, c.referencia) }
        }
        for (lg in lugares) if (lg != null) {
            esq = min(esq, lg.esquerda); dir = max(dir, lg.direita); topo = min(topo, lg.topo); baixo = max(baixo, lg.baixo)
        }
        esq = floor(esq) - 2f
        topo = floor(topo) - 2f
        val w = (ceil(dir) + 2f - esq).toInt().coerceAtLeast(1)
        val a = (ceil(baixo) + 2f - topo).toInt().coerceAtLeast(1)
        val imagem = try {
            ImageBitmap(w, a)
        } catch (e: OutOfMemoryError) {
            return null
        }
        val proj = Projecao(tb, -esq, -topo)
        val stroke = Stroke(max(0.6f, traco))
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, ComposeCanvas(imagem), Size(w.toFloat(), a.toFloat())) {
            for ((i, c) in cascas.withIndex()) {
                val info = info(c.id)
                val lugar = lugares[i]
                var feito = false
                if (info != null && lugar != null) {
                    val k = lugar.largura / info.largura
                    val amostra = amostraDaArte(info.largura, info.altura, (info.largura * k).roundToInt().coerceAtLeast(1), (info.altura * k).roundToInt().coerceAtLeast(1))
                    val lida = try {
                        ArteDoApp.decodificar(contexto, info, amostra)
                    } catch (e: OutOfMemoryError) {
                        null
                    }
                    if (lida != null) {
                        desenharSprite(
                            SpriteNaTela(lida.asImageBitmap(), LugarDaArte(lugar.esquerda - esq, lugar.topo - topo, lugar.largura, lugar.altura), info),
                            c.cor?.let { multiplicarPor(it) },
                        )
                        lida.recycle()
                        feito = true
                    }
                }
                if (!feito) desenharFormas(vetorial(c, proj), corDoContorno, stroke)
            }
        }
        imagem.prepareToDraw()
        return FundoDoAndar(imagem, esq, topo, tb)
    }

    /** Solta tudo (a tela saiu). Os bitmaps não são reciclados à força: o coletor de lixo cuida deles. */
    fun limpar() {
        imagens.clear()
        falhas.clear()
        fundos.clear()
    }

    private companion object {
        /** Um fundo por andar (os dois andares podem aparecer juntos). */
        const val MAX_FUNDOS = 2

        /** Móveis, lâmpadas e parede interna dos dois andares, nos dois estados: sobra folga. */
        const val MAX_IMAGENS = 72
    }
}
