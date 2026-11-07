package br.com.casinhadosaber.ui.componentes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.util.LruCache
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import br.com.casinhadosaber.dominio.Estrutura
import br.com.casinhadosaber.dominio.Gesto
import br.com.casinhadosaber.dominio.Passo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * A pilha fixa da casa ([CamadasDaCasa.camadas]) composta UMA vez num bitmap por (camadas, largura), como os
 * personagens ([ComposicaoDosPersonagens]). Cada camada da cena decodificada pesa 3 a 4 MB: elas são lidas sem
 * passar pelo cache da [ArteDoApp], desenhadas no composto e recicladas na hora. Só o composto fica na memória.
 */
object ComposicaoDaCasa {
    private data class Chave(val camadas: List<CamadaDaCasa>, val largura: Int)

    private class Composto(val bitmap: Bitmap, val imagem: ImageBitmap)

    /**
     * Compostos medidos em KB: 1/16 da memória do app, mas sempre com lugar para uns três (um composto da casa
     * com o chão tem até 800 x 1320 px, 4,2 MB): um despejo no meio da tela faria a casa ser composta de novo.
     */
    private val cache = object : LruCache<Chave, Composto>(
        (Runtime.getRuntime().maxMemory() / 1024 / 16).toInt().coerceAtLeast(14 * 1024),
    ) {
        override fun sizeOf(key: Chave, value: Composto): Int = (value.bitmap.byteCount / 1024).coerceAtLeast(1)
    }

    /**
     * As [camadas] compostas num bitmap de [largura] px (no máximo a largura da arte: ampliar não ganha nitidez);
     * a altura acompanha a camada mais alta (o chão desce até 1,65 da largura). Cada camada é desenhada em (0, 0)
     * com a largura toda, tingida por multiplicação quando tem cor. Nulo se faltar ou não der para ler uma camada.
     */
    fun composto(contexto: Context, camadas: List<CamadaDaCasa>, largura: Int): ImageBitmap? {
        if (camadas.isEmpty() || largura <= 0) return null
        val pilha = CamadasDaCasa.empilhar(ArteDoApp.manifesto(contexto), camadas) ?: return null
        val l = largura.coerceIn(1, CamadasDaCasa.LARGURA_DA_ARTE)
        val chave = Chave(camadas, l)
        cache.get(chave)?.let { return it.imagem }
        val proporcao = pilha.maxOf { it.second.altura.toFloat() / it.second.largura }
        val a = (l * proporcao).roundToInt().coerceAtLeast(1)
        val bmp = try {
            Bitmap.createBitmap(l, a, Bitmap.Config.ARGB_8888)
        } catch (e: OutOfMemoryError) {
            return null
        }
        val tela = Canvas(bmp)
        val tinta = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        for ((camada, info) in pilha) {
            val alturaDaCamada = (l.toFloat() * info.altura / info.largura).roundToInt().coerceAtLeast(1)
            val amostra = amostraDaArte(info.largura, info.altura, l, alturaDaCamada)
            val lida = ArteDoApp.decodificar(contexto, info, amostra)
            if (lida == null) {
                bmp.recycle()
                return null
            }
            // Multiplicar mantém a luz e a sombra do neutro claro e o alfa da camada (como nos personagens).
            tinta.colorFilter = camada.cor?.let { PorterDuffColorFilter(it.toInt(), PorterDuff.Mode.MULTIPLY) }
            tela.drawBitmap(lida, null, Rect(0, 0, l, alturaDaCamada), tinta)
            lida.recycle()
        }
        bmp.prepareToDraw()
        val composto = Composto(bmp, bmp.asImageBitmap())
        cache.put(chave, composto)
        return composto.imagem
    }

    fun limparCache() = cache.evictAll()
}

/**
 * O que a cena precisa para desenhar a casa em camadas: o composto da casa e as camadas animadas (estrelas que
 * piscam, nuvens que passeiam, a nuvem de chuva e a chuva ou a neve que caem). [nova] traz a casa partida em três
 * (abaixo, a estrutura nova e acima) enquanto a estrutura nova cai do céu.
 */
internal class CasaEmCamadas(
    val casa: ImageBitmap,
    val estrelas: List<ImageBitmap>,
    val nuvens: List<ImageBitmap>,
    val nuvemDeChuva: ImageBitmap?,
    val clima: ImageBitmap?,
    val nova: CasaPartida?,
)

/** A casa partida em volta da estrutura que está caindo (só durante a animação de construir). */
internal class CasaPartida(val abaixo: ImageBitmap?, val nova: ImageBitmap?, val acima: ImageBitmap?)

/**
 * A casa em camadas para este estado e esta largura (px), ou nulo se a arte da casa não estiver toda no manifesto
 * ou não puder ser lida: aí fica o desenho antigo, inteiro.
 */
@Composable
internal fun lembrarCasaEmCamadas(aparencia: AparenciaDaCasa, clima: Int, nova: Estrutura?, largura: Int): CasaEmCamadas? {
    val contexto = LocalContext.current
    val disponivel = remember(contexto) { CamadasDaCasa.disponivel(ArteDoApp.manifesto(contexto)) }
    val casa = remember(disponivel, aparencia, largura) {
        if (disponivel) ComposicaoDaCasa.composto(contexto, CamadasDaCasa.camadas(aparencia), largura) else null
    }
    val noite = aparencia.noite
    val animadas = remember(disponivel, noite, clima, largura) {
        if (!disponivel || largura <= 0) {
            null
        } else {
            fun ler(id: String, alvo: Float): ImageBitmap? {
                val info = ArteDoApp.info(contexto, id) ?: return null
                val l = alvo.roundToInt().coerceAtLeast(1)
                val amostra = amostraDaArte(info.largura, info.altura, l, (l.toFloat() * info.altura / info.largura).roundToInt())
                return ArteDoApp.bitmap(contexto, info, amostra)?.asImageBitmap()
            }
            val w = largura.toFloat()
            Animadas(
                estrelas = if (noite) CamadasDaCasa.ESTRELAS.mapNotNull { ler(it, w) } else emptyList(),
                nuvens = if (noite) emptyList() else CamadasDaCasa.NUVENS.mapNotNull { ler(it, w * 0.2f) },
                nuvemDeChuva = if (clima == 1) ler(CamadasDaCasa.NUVEM_DE_CHUVA, w * 0.32f) else null,
                clima = CamadasDaCasa.doClima(clima)?.let { ler(it, w) },
            )
        }
    }
    // A estrutura nova cai separada do resto: a casa partida em três, em meia resolução (é só durante a queda).
    val partida = remember(disponivel, aparencia, nova, largura) {
        if (!disponivel || nova == null || nova == Estrutura.TERRENO || !aparencia.tem(nova)) {
            null
        } else {
            val camadas = CamadasDaCasa.camadas(aparencia)
            val pilha = CamadasDaCasa.empilhar(ArteDoApp.manifesto(contexto), camadas)
            val minimo = pilha?.filter { it.first.estrutura == nova }?.minOfOrNull { it.second.ordem }
            if (pilha == null || minimo == null) {
                null
            } else {
                val meia = (largura / 2).coerceAtLeast(1)
                fun compor(lista: List<CamadaDaCasa>) = if (lista.isEmpty()) null else ComposicaoDaCasa.composto(contexto, lista, meia)
                val daNova = pilha.filter { it.first.estrutura == nova }
                val resto = pilha.filter { it.first.estrutura != nova }
                CasaPartida(
                    abaixo = compor(resto.filter { it.second.ordem <= minimo }.map { it.first }),
                    nova = compor(daNova.map { it.first }),
                    acima = compor(resto.filter { it.second.ordem > minimo }.map { it.first }),
                )
            }
        }
    }
    if (casa == null || animadas == null) return null
    return CasaEmCamadas(casa, animadas.estrelas, animadas.nuvens, animadas.nuvemDeChuva, animadas.clima, partida)
}

private class Animadas(
    val estrelas: List<ImageBitmap>,
    val nuvens: List<ImageBitmap>,
    val nuvemDeChuva: ImageBitmap?,
    val clima: ImageBitmap?,
)

/**
 * Desenha [imagem] com a largura toda da cena, alinhada no topo (como toda camada da cena), descida [dy] px.
 * O que passa da altura do [DrawScope] (o chão que continua no jardim de baixo) quem corta é a caixa da cena.
 */
internal fun DrawScope.camadaDaCena(imagem: ImageBitmap, dy: Float = 0f, alfa: Float = 1f, filtro: ColorFilter? = null) {
    val w = size.width
    val h = w * imagem.height / imagem.width
    drawImage(
        image = imagem,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(imagem.width, imagem.height),
        dstOffset = IntOffset(0, dy.roundToInt()),
        dstSize = IntSize(w.roundToInt(), h.roundToInt()),
        alpha = alfa,
        colorFilter = filtro,
    )
}

/** Desenha [imagem] com [largura] px e o canto de cima à esquerda em [x], [y]. */
internal fun DrawScope.imagemEm(imagem: ImageBitmap, x: Float, y: Float, largura: Float, alfa: Float = 1f) {
    val altura = largura * imagem.height / imagem.width
    drawImage(
        image = imagem,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(imagem.width, imagem.height),
        dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
        dstSize = IntSize(largura.roundToInt(), altura.roundToInt()),
        alpha = alfa,
    )
}

/** Filtro da noite para as imagens soltas (itens), o mesmo da pilha da casa. */
internal val FiltroDaNoite: ColorFilter = ColorFilter.tint(Color(CamadasDaCasa.NOITE), BlendMode.Modulate)

/**
 * Um item da casa pela arte 3D (`item_<id>`), com o pé (o ponto de apoio do manifesto) no meio da base desta
 * caixa e a largura do item ([larguraPx]) um pouco folgada ([CamadasDaCasa.FOLGA_DO_ITEM]). A imagem pode passar
 * da caixa (a arte tem sombra de contato e folga em volta): quem chama não pode recortar a caixa.
 * Com [passo] (cachorrinho e gatinho), ele vira para onde anda e dá pulinhos nas passadas.
 * Se a imagem não puder ser lida, mostra [reserva].
 */
@Composable
internal fun ItemDaArte(
    info: InfoDaArte,
    larguraPx: Float,
    noite: Boolean,
    passo: (() -> Passo)? = null,
    reserva: @Composable () -> Unit,
) {
    val contexto = LocalContext.current
    val lugar = CamadasDaCasa.lugarDoItem(info, larguraPx)
    val amostra = amostraDaArte(info.largura, info.altura, lugar.largura.roundToInt(), lugar.altura.roundToInt())
    val imagem = remember(info, amostra) { ArteDoApp.bitmap(contexto, info, amostra)?.asImageBitmap() }
    if (imagem == null) {
        reserva()
        return
    }
    val filtro = if (noite && info.id !in CamadasDaCasa.ITENS_ACESOS) FiltroDaNoite else null
    Spacer(
        Modifier
            .fillMaxSize()
            .drawBehind {
                val p = passo?.invoke()
                val pe = Offset(size.width / 2f, size.height)
                val pulinho = if (p != null && p.gesto == Gesto.ANDANDO) abs(sin(p.passada * PI.toFloat())) * lugar.altura * 0.05f else 0f
                withTransform({ if (p != null && !p.paraDireita) scale(-1f, 1f, pe) }) {
                    drawImage(
                        image = imagem,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(imagem.width, imagem.height),
                        dstOffset = IntOffset((pe.x + lugar.esquerda).roundToInt(), (pe.y + lugar.topo - pulinho).roundToInt()),
                        dstSize = IntSize(lugar.largura.roundToInt(), lugar.altura.roundToInt()),
                        colorFilter = filtro,
                    )
                }
            },
    )
}
