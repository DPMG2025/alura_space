package br.com.casinhadosaber.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.ui.tema.Cores
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Arte 3D "leve" (Fase 8B): ícones pequenos (estrelas, contas, cadeado, notinha) lidos uma vez do cache da
// [ArteDoApp] e desenhados várias vezes num único Canvas. Uma fileira de 10 contas é um nó só, e não dez
// [ImagemDeArte] (cada uma é um BoxWithConstraints com subcomposição). Para telas com muitas peças repetidas,
// como o caminho (até 20 fases) e o mapa (25 mundos).

/**
 * A imagem [id] da arte já decodificada para caber num quadrado de [lado] (pelo cache da [ArteDoApp]: as telas
 * que pedem o mesmo ícone no mesmo tamanho dividem um bitmap só), ou nula se a arte faltar.
 */
@Composable
fun lembrarArteLeve(id: String, lado: Dp): ImageBitmap? {
    val contexto = LocalContext.current
    val px = with(LocalDensity.current) { lado.roundToPx() }.coerceAtLeast(1)
    return remember(id, px) {
        val info = ArteDoApp.info(contexto, id) ?: return@remember null
        val maior = maxOf(info.largura, info.altura).coerceAtLeast(1)
        val alvoL = (px.toLong() * info.largura / maior).toInt().coerceAtLeast(1)
        val alvoA = (px.toLong() * info.altura / maior).toInt().coerceAtLeast(1)
        ArteDoApp.bitmap(contexto, info, amostraDaArte(info.largura, info.altura, alvoL, alvoA))?.asImageBitmap()
    }
}

/**
 * Desenha [imagem] inteira, com a proporção dela, dentro do quadrado de [lado] px centrado em [centro]. Com [filtro]
 * (por exemplo `ColorFilter.tint(cor, BlendMode.SrcIn)`), sai a silhueta numa cor só.
 */
fun DrawScope.desenharArte(imagem: ImageBitmap, centro: Offset, lado: Float, alfa: Float = 1f, filtro: ColorFilter? = null) {
    val maior = maxOf(imagem.width, imagem.height).coerceAtLeast(1).toFloat()
    val l = (imagem.width / maior * lado).roundToInt().coerceAtLeast(1)
    val a = (imagem.height / maior * lado).roundToInt().coerceAtLeast(1)
    drawImage(
        image = imagem,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(imagem.width, imagem.height),
        dstOffset = IntOffset((centro.x - l / 2f).roundToInt(), (centro.y - a / 2f).roundToInt()),
        dstSize = IntSize(l, a),
        alpha = alfa,
        colorFilter = filtro,
        filterQuality = FilterQuality.Medium,
    )
}

/**
 * Fase 9 (K1): [imagem] com um contorno de [espessura] px em volta (a silhueta tingida por [contorno], desenhada em
 * oito direções por baixo), para uma figura clarinha (copo de leite, ovo, nuvem) não sumir sobre a grama clara.
 * Sem blur nem camada: nove `drawImage` do mesmo bitmap pequeno.
 */
fun DrawScope.desenharArteComContorno(imagem: ImageBitmap, centro: Offset, lado: Float, contorno: ColorFilter, espessura: Float) {
    val l = (lado - espessura * 2f).coerceAtLeast(1f)
    for (k in 0 until 8) {
        val a = k * PI / 4.0
        desenharArte(imagem, Offset(centro.x + cos(a).toFloat() * espessura, centro.y + sin(a).toFloat() * espessura), l, 1f, contorno)
    }
    desenharArte(imagem, centro, l)
}

/**
 * Fase 9 (K1): a figura 3D de [emoji] (`fig_*`, campo `emojis` do manifesto, [FigurasDaPergunta]) desenhada num
 * Canvas pelo bitmap pequeno do cache (sem o BoxWithConstraints da [ImagemDeArte]: serve para listas, como os 25
 * mundos do mapa ou os marcadores da estante). Sem modelo, o próprio emoji ([reserva]). Com [silhueta], a figura
 * sai pintada de uma cor só (o que ainda falta ganhar). É decorativa: quem fala o nome é a peça em volta.
 */
@Composable
fun FiguraLeveDoEmoji(
    emoji: String,
    tamanho: Dp,
    modifier: Modifier = Modifier,
    alfa: Float = 1f,
    silhueta: Color? = null,
    reserva: (@Composable () -> Unit)? = null,
) {
    val contexto = LocalContext.current
    val id = remember(emoji) { FigurasDaPergunta.idPara(ArteDoApp.manifesto(contexto), emoji) }
    val imagem = if (id != null) lembrarArteLeve(id, tamanho) else null
    if (imagem == null) {
        Box(modifier.size(tamanho).graphicsLayer { alpha = alfa }, contentAlignment = Alignment.Center) {
            if (reserva != null) {
                reserva()
            } else {
                Text(emoji, fontSize = (tamanho.value * 0.78f).sp, lineHeight = (tamanho.value * 0.95f).sp, maxLines = 1, softWrap = false)
            }
        }
    } else {
        val filtro = remember(silhueta) { silhueta?.let { ColorFilter.tint(it, BlendMode.SrcIn) } }
        Canvas(modifier.size(tamanho)) { desenharArte(imagem, center, size.minDimension, alfa, filtro) }
    }
}

/**
 * Uma fileira de ícones da arte ([ids], da esquerda para a direita, cada um com a sua [escalas] opcional) num
 * Canvas só. Sem a arte, cada um vira o desenho de [reserva] (estrela, bolinha). Com [descricao], é um nó só na
 * acessibilidade; sem ela, é decorativa.
 */
@Composable
fun FileiraDeArteLeve(
    ids: List<String>,
    tamanho: Dp,
    modifier: Modifier = Modifier,
    espaco: Dp = 2.dp,
    escalas: List<Float>? = null,
    descricao: String? = null,
    reserva: DrawScope.(id: String, centro: Offset, raio: Float) -> Unit = { _, c, r -> drawCircle(Cores.Contorno, r, c) },
) {
    if (ids.isEmpty()) return
    val maiorEscala = escalas?.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    val distintos = remember(ids) { ids.distinct() }
    val lado = tamanho * maiorEscala
    // Um bitmap por id diferente (no máximo três ou quatro), lidos uma vez.
    val imagens = HashMap<String, ImageBitmap?>(distintos.size)
    for (id in distintos) imagens[id] = lembrarArteLeveSemChave(id, lado)
    val semantica = if (descricao != null) Modifier.clearAndSetSemantics { contentDescription = descricao } else Modifier.clearAndSetSemantics { }
    Canvas(
        modifier
            .then(semantica)
            .size(width = tamanho * ids.size + espaco * (ids.size - 1) + (lado - tamanho), height = lado),
    ) {
        val t = tamanho.toPx()
        val e = espaco.toPx()
        for ((i, id) in ids.withIndex()) {
            val escala = escalas?.getOrNull(i) ?: 1f
            val centro = Offset(t * i + e * i + size.height / 2f, size.height / 2f)
            val l = t * escala
            val img = imagens[id]
            if (img != null) desenharArte(img, centro, l) else reserva(id, centro, l / 2f)
        }
    }
}

/** [lembrarArteLeve] chamado num laço de tamanho variável: o `remember` fica num grupo com a chave do id. */
@Composable
private fun lembrarArteLeveSemChave(id: String, lado: Dp): ImageBitmap? =
    androidx.compose.runtime.key(id) { lembrarArteLeve(id, lado) }

/** Estrelas do progresso numa fileira leve (`estrela_cheia`/`estrela_vazia`): [cheias] de [total]. */
@Composable
fun EstrelasLeves(cheias: Int, total: Int, tamanho: Dp, modifier: Modifier = Modifier, descricao: String? = null) {
    val n = total.coerceAtLeast(0)
    val acesas = cheias.coerceIn(0, n)
    FileiraDeArteLeve(
        ids = List(n) { if (it < acesas) ID_ESTRELA_CHEIA else ID_ESTRELA_VAZIA },
        tamanho = tamanho,
        modifier = modifier,
        espaco = 1.dp,
        descricao = descricao,
        reserva = { id, c, r -> drawPath(caminhoEstrela(c, r), if (id == ID_ESTRELA_CHEIA) Cores.Estrela else Cores.EstrelaApagada) },
    )
}

/**
 * Contas do progresso numa fileira leve, sem pílula: [feitas] de [total] na [cor], a da vez (com [comAtual])
 * amarela e maior, as que faltam peroladas. Para listas longas na mesma tela (o mapa tem 25 mundos).
 */
@Composable
fun ContasLeves(
    feitas: Int,
    total: Int,
    tamanho: Dp,
    modifier: Modifier = Modifier,
    cor: CorDaConta = CorDaConta.VERDE,
    comAtual: Boolean = false,
    descricao: String? = null,
) {
    val estados = ProgressoVisivel.contas(feitas, total, comAtual)
    FileiraDeArteLeve(
        ids = estados.map {
            when (it) {
                EstadoDaConta.FEITA -> cor.id
                EstadoDaConta.ATUAL -> CorDaConta.AMARELA.id
                EstadoDaConta.FALTA -> CorDaConta.VAZIA
            }
        },
        escalas = estados.map { if (it == EstadoDaConta.ATUAL) 1.2f else if (it == EstadoDaConta.FALTA) 0.85f else 1f },
        tamanho = tamanho,
        modifier = modifier,
        espaco = 1.dp,
        descricao = descricao,
        reserva = { id, c, r -> drawCircle(if (id == CorDaConta.VAZIA) Color.White.copy(alpha = 0.6f) else Cores.Estrela, r * 0.85f, c) },
    )
}

// -------------------------------------------------------------------------------- céu das telas

/** O céu do app (maquetes 1 e 2): azul em cima, clarinho no meio, pêssego e rosa embaixo. */
val CeuDeBrinquedo: Brush = Brush.verticalGradient(
    0f to Color(0xFF7CC4FA),
    0.38f to Color(0xFFB8E2FF),
    0.66f to Color(0xFFFFE6D6),
    1f to Color(0xFFFFD0DC),
)

/** Azul do alto do [CeuDeBrinquedo] (para esmaecer o que passa por baixo do cabeçalho). */
val AzulDoCeu = Color(0xFF7CC4FA)

/** Rosado de baixo do [CeuDeBrinquedo]. */
val RosaDoCeu = Color(0xFFFFD0DC)

/** Verdes da grama da ilha (os da arte, `A9E56F`). */
val GramaDaIlha = Color(0xFFA9E56F)
private val GramaDaIlhaEscura = Color(0xFF7FCB4F)

/**
 * Fundo das telas no visual brinquedo 3D: o céu em degradê, três nuvens fofas paradas e, com [comGrama], os
 * morrinhos de grama da ilha embaixo. Tudo em vetor, montado uma vez por tamanho e nada se mexe: nenhum bitmap,
 * nenhuma animação (mais leve que o [FundoDecorado] de bolhas, que redesenhava a cada quadro).
 */
fun Modifier.ceuDeBrinquedo(comGrama: Boolean = true): Modifier = drawWithCache {
    val w = size.width
    val h = size.height
    val nuvens = listOf(Offset(w * 0.16f, h * 0.10f) to 0.9f, Offset(w * 0.78f, h * 0.20f) to 1.1f, Offset(w * 0.44f, h * 0.42f) to 0.7f)
    val unidade = minOf(w, 520.dp.toPx()) * 0.06f
    val tras = Path()
    val frente = Path()
    if (comGrama) {
        tras.moveTo(0f, h * 0.90f)
        tras.cubicTo(w * 0.25f, h * 0.84f, w * 0.55f, h * 0.89f, w * 0.75f, h * 0.86f)
        tras.cubicTo(w * 0.88f, h * 0.84f, w * 0.96f, h * 0.86f, w, h * 0.87f)
        tras.lineTo(w, h); tras.lineTo(0f, h); tras.close()
        frente.moveTo(0f, h * 0.95f)
        frente.cubicTo(w * 0.3f, h * 0.90f, w * 0.7f, h * 0.92f, w, h * 0.94f)
        frente.lineTo(w, h); frente.lineTo(0f, h); frente.close()
    }
    val gramaDaFrente = Brush.verticalGradient(listOf(GramaDaIlha, GramaDaIlhaEscura), startY = h * 0.9f, endY = h)
    onDrawBehind {
        drawRect(CeuDeBrinquedo)
        for ((c, e) in nuvens) nuvemFofa(c, unidade * e)
        if (comGrama) {
            drawPath(tras, GramaDaIlha.copy(alpha = 0.75f))
            drawPath(frente, gramaDaFrente)
        }
    }
}

/** Nuvem de algodão: três bolas brancas sobre uma base achatada, com um fio lilás embaixo (sombra de contato). */
private fun DrawScope.nuvemFofa(c: Offset, r: Float) {
    drawOval(Color(0x1F7C6BC4), Offset(c.x - r * 1.9f, c.y + r * 0.35f), Size(r * 3.8f, r * 0.7f))
    val branco = Color.White.copy(alpha = 0.92f)
    drawOval(branco, Offset(c.x - r * 1.9f, c.y - r * 0.2f), Size(r * 3.8f, r * 0.9f))
    drawCircle(branco, r * 0.75f, Offset(c.x - r * 0.9f, c.y))
    drawCircle(branco, r, Offset(c.x + r * 0.1f, c.y - r * 0.35f))
    drawCircle(branco, r * 0.7f, Offset(c.x + r * 1.05f, c.y - r * 0.05f))
}
