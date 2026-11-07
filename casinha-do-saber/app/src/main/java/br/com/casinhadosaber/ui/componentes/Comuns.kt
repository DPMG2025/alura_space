package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.Degrades
import br.com.casinhadosaber.ui.tema.Raios

/**
 * Botão "fofinho" com cara de bloco de brinquedo (visual 3D, `.bloco` de `docs/visual-3d/prototipo/ui2.css`):
 * face colorida em degradê ([Degrades.bloco]), lábio mais escuro embaixo ([corBase], [profundidade]), brilho
 * macio no alto à esquerda ([brilho]), borda branca ([contorno] diferente de nulo) e sombra suave ([sombra]).
 * Ao apertar, afunda até o lábio e amassa (1,04 × 0,94), voltando com mola.
 * Desabilitado fica numa versão clarinha da própria cor (nunca cinza).
 */
@Composable
fun BotaoFofo(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color = Cores.Roxo,
    corBase: Color = baseDe(cor),
    forma: Shape = RoundedCornerShape(24.dp),
    descricao: String? = null,
    habilitado: Boolean = true,
    profundidade: Dp = 6.dp,
    /** Ocupa todo o espaço que o pai der (para botões que crescem com a tela). */
    preencher: Boolean = false,
    /** Brilho macio no alto da face. */
    brilho: Boolean = true,
    /** Com cor, o botão ganha a borda branca do brinquedo e um fio desta cor por fora do lábio; `null` tira os dois. */
    contorno: Color? = Cores.Contorno,
    /** Sombra suave embaixo do lábio. */
    sombra: Boolean = true,
    conteudo: @Composable () -> Unit,
) {
    BlocoDeBrinquedo(
        onClick, modifier, cor, corBase, forma, descricao, habilitado, profundidade, preencher, brilho, contorno, sombra,
        redondoClaro = false, conteudo = conteudo,
    )
}

/** O [BotaoFofo] por dentro; [redondoClaro] troca a face pela do botão redondo (`.redondo`, luz em cima à esquerda). */
@Composable
private fun BlocoDeBrinquedo(
    onClick: () -> Unit,
    modifier: Modifier,
    cor: Color,
    corBase: Color,
    forma: Shape,
    descricao: String?,
    habilitado: Boolean,
    profundidade: Dp,
    preencher: Boolean,
    brilho: Boolean,
    contorno: Color?,
    sombra: Boolean,
    redondoClaro: Boolean,
    conteudo: @Composable () -> Unit,
) {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val afundar by animateFloatAsState(if (pressionado) 1f else 0f, spring(stiffness = Spring.StiffnessHigh), label = "afundar")
    // Mola mais solta: ao soltar passa um pouquinho do ponto e volta ("boing").
    val amassar by animateFloatAsState(
        if (pressionado) 1f else 0f,
        spring(dampingRatio = 0.4f, stiffness = 700f),
        label = "amassar",
    )
    val corFace = if (habilitado) cor else clarear(cor, 0.5f)
    val corDaBase = if (habilitado) corBase else clarear(corBase, 0.45f)
    Box(
        modifier
            .graphicsLayer {
                scaleX = 1f + 0.04f * amassar
                scaleY = 1f - 0.06f * amassar
                transformOrigin = TransformOrigin(0.5f, 1f)
            }
            .semantics { descricao?.let { contentDescription = it } }
            .clickable(interacao, indication = null, enabled = habilitado, role = Role.Button, onClick = onClick)
            .baseDoBotao(forma, corDaBase, profundidade, if (sombra) Cores.Sombra else null, contorno)
            .padding(bottom = profundidade),
        // Com tamanho mínimo vindo de fora (fillMaxWidth, size), a face ocupa esse tamanho todo.
        propagateMinConstraints = true,
    ) {
        Box(
            Modifier
                .then(if (preencher) Modifier.fillMaxSize() else Modifier)
                .offset { IntOffset(0, (profundidade.toPx() * afundar).roundToInt()) }
                .clip(forma)
                .faceDeBrinquedo(corFace, forma, brilho, bordaBranca = contorno != null, redonda = redondoClaro)
                .graphicsLayer { alpha = if (habilitado) 1f else 0.8f },
            contentAlignment = Alignment.Center,
        ) {
            conteudo()
        }
    }
}

/**
 * Face do bloco de brinquedo desenhada atrás do conteúdo (use depois do `clip` da [forma]): degradê da cor,
 * sombra interna embaixo (a face "arredonda" para o lábio), brilho em cima e a borda branca a 75%.
 * [redonda]: a face clara do botão redondo, com o ponto de luz em cima à esquerda (`radial-gradient` do protótipo).
 */
fun Modifier.faceDeBrinquedo(
    cor: Color,
    forma: Shape = RoundedCornerShape(24.dp),
    brilho: Boolean = true,
    bordaBranca: Boolean = true,
    redonda: Boolean = false,
): Modifier = drawWithCache {
    val w = size.width
    val h = size.height
    if (w <= 0f || h <= 0f) return@drawWithCache onDrawBehind { }
    val face: Brush = if (redonda) {
        val clara = if (cor == Color.White) Color(0xFFE9E2FF) else lerp(cor, Color.Black, 0.06f)
        // Ponto de luz: branco no botão claro; nos coloridos, a própria cor bem clareada (não uma mancha branca).
        val luz = if (cor == Color.White) Color.White else lerp(cor, Color.White, 0.55f)
        Brush.radialGradient(
            0f to luz, 0.18f to luz, 0.30f to lerp(luz, clara, 0.15f), 1f to clara,
            center = Offset(w * 0.35f, h * 0.28f),
            radius = maxOf(w, h) * 0.85f,
        )
    } else {
        Degrades.bloco(cor)
    }
    // Sombra interna do pé (inset 0 -6px 10px): só na faixa de baixo.
    val pe = Brush.verticalGradient(
        0f to Color.Transparent, 0.72f to Color.Transparent,
        1f to (if (redonda) Color(0x2E7850C8) else Color(0x24000000)),
    )
    val borda = minOf(3.dp.toPx(), minOf(w, h) * 0.06f)
    val contornoDaFace = forma.createOutline(size, layoutDirection, this)
    onDrawBehind {
        drawRect(face)
        drawRect(pe)
        if (brilho && !redonda) {
            // radial-gradient(120% 70% at 30% 0%, branco .55 até 30%, some em 55%): elipse achatada no alto.
            val cx = w * 0.30f
            val raio = w * 1.2f
            scale(scaleX = 1f, scaleY = (h * 0.7f) / raio, pivot = Offset(cx, 0f)) {
                drawCircle(
                    Brush.radialGradient(
                        0f to Color.White.copy(alpha = 0.55f), 0.30f to Color.White.copy(alpha = 0.50f), 0.55f to Color.Transparent,
                        center = Offset(cx, 0f),
                        radius = raio,
                    ),
                    radius = raio,
                    center = Offset(cx, 0f),
                )
            }
        }
        if (bordaBranca) drawOutline(contornoDaFace, Color.White.copy(alpha = if (redonda) 1f else 0.75f), style = Stroke(borda * 2f))
    }
}

/**
 * Base 3D do [BotaoFofo] desenhada atrás de tudo: a sombra suave (duas camadas que somem para baixo),
 * a base escura deslocada [profundidade] para baixo e o contorno dela. Funciona com qualquer [Shape].
 */
private fun Modifier.baseDoBotao(forma: Shape, cor: Color, profundidade: Dp, sombra: Color?, contorno: Color?): Modifier =
    drawWithCache {
        val p = profundidade.toPx()
        // Botão mais baixo que a própria profundidade (ou ainda sem tamanho): nada para desenhar.
        // Altura negativa faria o createOutline da forma arredondada travar o app.
        val tamanho = Size(size.width.coerceAtLeast(0f), (size.height - p).coerceAtLeast(0f))
        if (tamanho.isEmpty()) return@drawWithCache onDrawBehind { }
        val contornoDaBase = forma.createOutline(tamanho, layoutDirection, this)
        val traco = Stroke(2.dp.toPx())
        val passo = 2.dp.toPx()
        onDrawBehind {
            if (sombra != null) {
                translate(top = p + passo * 2) { drawOutline(contornoDaBase, sombra.copy(alpha = sombra.alpha * 0.5f)) }
                translate(top = p + passo) { drawOutline(contornoDaBase, sombra) }
            }
            translate(top = p) {
                drawOutline(contornoDaBase, cor)
                if (contorno != null) drawOutline(contornoDaBase, contorno, style = traco)
            }
        }
    }

/**
 * Faixa de brilho em forma de pílula no terço de cima (o "doce" dos botões). Use depois do `clip`
 * e do `background` da forma, para o brilho ficar dentro dela.
 */
fun Modifier.brilhoDoTopo(intensidade: Float = 1f): Modifier = drawBehind {
    val ladoX = maxOf(5.dp.toPx(), size.width * 0.09f)
    val topo = maxOf(3.dp.toPx(), size.height * 0.07f)
    val altura = size.height * 0.34f
    val largura = size.width - ladoX * 2
    if (largura <= 0f || altura <= 0f) return@drawBehind
    drawRoundRect(
        Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.45f * intensidade), Color.White.copy(alpha = 0.06f * intensidade)),
            startY = topo,
            endY = topo + altura,
        ),
        topLeft = Offset(ladoX, topo),
        size = Size(largura, altura),
        cornerRadius = CornerRadius(altura / 2f),
    )
}

/**
 * Ladrilho/cartão do sistema de design desenhado atrás do conteúdo, no visual de brinquedo: corpo [cor] em
 * degradê para um lilás quase branco, brilho no alto, "lábio" colorido de [profundidade] embaixo (com [labio];
 * reserva o espaço com um padding), contorno fino ([contorno]) e sombra macia em duas camadas.
 * Para respostas, peças e painéis: `Modifier.cartaoFofo(labio = corDaTrilha)`.
 */
fun Modifier.cartaoFofo(
    cor: Color = Cores.Cartao,
    raio: Dp = Raios.Medio,
    labio: Color? = null,
    profundidade: Dp = 4.dp,
    contorno: Color? = Cores.Contorno,
    sombra: Boolean = true,
): Modifier = this
    .padding(bottom = if (labio != null) profundidade else 0.dp)
    .drawWithCache {
        val r = CornerRadius(raio.toPx())
        val p = if (labio != null) profundidade.toPx() else 0f
        val corpo = Brush.verticalGradient(listOf(lerp(cor, Color.White, 0.3f), cor, lerp(cor, Color(0xFFEDE6FF), 0.3f)))
        val traco = Stroke(2.dp.toPx())
        val brilhoAlto = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0f)),
            startY = 0f,
            endY = minOf(size.height * 0.4f, 28.dp.toPx()),
        )
        val dentro = 3.dp.toPx()
        onDrawBehind {
            if (sombra) {
                drawRoundRect(Cores.Sombra.copy(alpha = Cores.Sombra.alpha * 0.45f), topLeft = Offset(0f, p + 6.dp.toPx()), size = size, cornerRadius = r)
                drawRoundRect(Cores.Sombra, topLeft = Offset(0f, p + 2.dp.toPx()), size = size, cornerRadius = r)
            }
            if (labio != null) {
                drawRoundRect(labio, topLeft = Offset(0f, p), size = size, cornerRadius = r)
                if (contorno != null) drawRoundRect(contorno, topLeft = Offset(0f, p), size = size, cornerRadius = r, style = traco)
            }
            drawRoundRect(corpo, size = size, cornerRadius = r)
            if (size.width > dentro * 2 && size.height > dentro * 2) {
                drawRoundRect(
                    brilhoAlto,
                    topLeft = Offset(dentro, dentro),
                    size = Size(size.width - dentro * 2, size.height - dentro * 2),
                    cornerRadius = CornerRadius((raio.toPx() - dentro).coerceAtLeast(0f)),
                )
            }
            if (contorno != null) drawRoundRect(contorno, size = size, cornerRadius = r, style = traco)
        }
    }

fun escurecer(c: Color, fator: Float = 0.78f) = Color(c.red * fator, c.green * fator, c.blue * fator, c.alpha)

/** Cor da base 3D de um botão [cor]: um tom mais escuro dela; para o branco, um lilás claro (o cinza parecia sujo). */
fun baseDe(cor: Color): Color = if (cor == Color.White) BaseLilas else escurecer(cor)

/**
 * Botão redondo claro da barra (fechar, voltar, som, ajustes...): face branca com luz em cima à esquerda, lábio
 * lilás, borda branca (`.redondo` do protótipo). Quando a arte 3D tem o ícone de [emoji] ([IconesDaBarra]), ele
 * aparece no lugar do emoji (glifo roxo de plástico); os outros emojis continuam até a arte deles existir.
 * Com [cor] diferente de branco, a face fica dessa cor (por exemplo, amarela quando a lojinha está aberta).
 */
@Composable
fun BotaoRedondo(
    emoji: String,
    descricao: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color = Color.White,
    tamanho: Dp = 56.dp,
    corBase: Color = baseDe(cor),
    habilitado: Boolean = true,
) {
    val icone = remember(emoji, descricao) { IconesDaBarra.para(emoji, descricao) }
    BlocoDeBrinquedo(
        onClick = onClick,
        modifier = modifier,
        cor = cor,
        corBase = corBase,
        forma = CircleShape,
        descricao = descricao,
        habilitado = habilitado,
        profundidade = 5.dp,
        preencher = false,
        brilho = true,
        contorno = Cores.Contorno,
        sombra = true,
        redondoClaro = true,
    ) {
        Box(Modifier.size(tamanho), contentAlignment = Alignment.Center) {
            val emojiDeReserva: @Composable () -> Unit = { Text(emoji, fontSize = (tamanho.value * 0.45f).sp) }
            if (icone == null) {
                emojiDeReserva()
            } else {
                IconeDeArteDaBarra(icone, Modifier.size(tamanho * 0.56f), reserva = emojiDeReserva)
            }
        }
    }
}

/**
 * Um ícone da barra em arte 3D (glifo roxo), espelhado quando pedido, com [reserva] se a imagem faltar.
 * Decorativo: quem fala o nome é o botão em volta.
 */
@Composable
fun IconeDeArteDaBarra(icone: IconeDaBarra, modifier: Modifier = Modifier, reserva: (@Composable () -> Unit)? = null) {
    ImagemDeArte(
        icone.id,
        modifier.then(if (icone.espelhado) Modifier.graphicsLayer { scaleX = -1f } else Modifier),
        reserva = reserva,
    )
}

/** Base dos botões brancos: lilás sólido (`.redondo` do protótipo; a base translúcida de antes sumia sobre a cena). */
private val BaseLilas = Cores.LabioLilas

/**
 * Uma estrela de plástico dourado (arte 3D `estrela_cheia`; acima de 48 dp, a `estrela3d` grande) ou a apagada
 * (`estrela_vazia`). Sem a arte, a estrela desenhada de antes.
 */
@Composable
fun Estrela(acesa: Boolean, modifier: Modifier = Modifier, tamanho: Dp = 24.dp) {
    val id = when {
        !acesa -> ID_ESTRELA_VAZIA
        tamanho > 48.dp -> "estrela3d"
        else -> ID_ESTRELA_CHEIA
    }
    ImagemDeArte(id, modifier.size(tamanho)) {
        Canvas(Modifier.size(tamanho)) {
            val c = Offset(size.width / 2, size.height / 2)
            drawPath(caminhoEstrela(c, size.minDimension / 2), if (acesa) Cores.Estrela else Cores.EstrelaApagada)
        }
    }
}


@Composable
fun FileiraDeEstrelas(quantas: Int, modifier: Modifier = Modifier, tamanho: Dp = 22.dp, total: Int = 3) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(total) { Estrela(it < quantas, tamanho = tamanho) }
    }
}

/** De que lado sai a ponta do [BalaoDeFala] (para onde está quem fala). */
enum class LadoDaCauda { Nenhum, Esquerda, Direita, Baixo }

/**
 * Balão de fala da criança personagem (`.balao` do protótipo): branco para lilás de cima para baixo, lábio
 * lilás embaixo, sombra macia e uma ponta apontando para quem fala ([cauda]; no jogo a criança fica à esquerda).
 */
@Composable
fun BalaoDeFala(texto: String, modifier: Modifier = Modifier, cauda: LadoDaCauda = LadoDaCauda.Esquerda) {
    Box(
        modifier
            .balaoDeFala(cauda)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(texto, style = MaterialTheme.typography.titleMedium)
    }
}

private val CAUDA = 14.dp
private val LABIO_DO_BALAO = 5.dp

/**
 * Desenho do balão atrás do conteúdo, num caminho só (corpo arredondado unido à ponta), para o
 * contorno passar por fora da ponta sem emenda. Reserva com padding o espaço da ponta.
 */
fun Modifier.balaoDeFala(
    cauda: LadoDaCauda = LadoDaCauda.Esquerda,
    raio: Dp = 24.dp,
    corDoContorno: Color = Color(0xFFEAE3FF),
): Modifier = this
    .padding(
        start = if (cauda == LadoDaCauda.Esquerda) CAUDA else 0.dp,
        end = if (cauda == LadoDaCauda.Direita) CAUDA else 0.dp,
        bottom = (if (cauda == LadoDaCauda.Baixo) CAUDA else 0.dp) + LABIO_DO_BALAO,
    )
    .drawWithCache {
        val r = raio.toPx().coerceAtMost(size.minDimension / 2f)
        val c = CAUDA.toPx()
        val corpo = Path().apply { addRoundRect(RoundRect(0f, 0f, size.width, size.height, CornerRadius(r))) }
        val ponta = Path()
        val w = size.width
        val h = size.height
        when (cauda) {
            LadoDaCauda.Esquerda -> {
                val y = (h * 0.55f).coerceIn(r * 0.6f, h - r * 0.6f)
                val m = minOf(12.dp.toPx(), h * 0.32f)
                ponta.moveTo(c * 0.5f, y - m)
                ponta.cubicTo(-c * 0.1f, y - m * 0.2f, -c * 0.5f, y + m * 0.4f, -c, y + m * 0.9f)
                ponta.cubicTo(-c * 0.2f, y + m * 0.9f, c * 0.2f, y + m * 0.6f, c * 0.5f, y + m * 0.5f)
                ponta.close()
            }
            LadoDaCauda.Direita -> {
                val y = (h * 0.55f).coerceIn(r * 0.6f, h - r * 0.6f)
                val m = minOf(12.dp.toPx(), h * 0.32f)
                ponta.moveTo(w - c * 0.5f, y - m)
                ponta.cubicTo(w + c * 0.1f, y - m * 0.2f, w + c * 0.5f, y + m * 0.4f, w + c, y + m * 0.9f)
                ponta.cubicTo(w + c * 0.2f, y + m * 0.9f, w - c * 0.2f, y + m * 0.6f, w - c * 0.5f, y + m * 0.5f)
                ponta.close()
            }
            LadoDaCauda.Baixo -> {
                val x = (w * 0.22f).coerceIn(r, maxOf(r, w - r))
                val m = 12.dp.toPx()
                ponta.moveTo(x - m, h - c * 0.5f)
                ponta.cubicTo(x - m * 0.2f, h + c * 0.1f, x - m * 0.6f, h + c * 0.6f, x - m * 1.2f, h + c)
                ponta.cubicTo(x + m * 0.2f, h + c * 0.8f, x + m * 0.6f, h + c * 0.2f, x + m, h - c * 0.5f)
                ponta.close()
            }
            LadoDaCauda.Nenhum -> Unit
        }
        val forma = if (cauda == LadoDaCauda.Nenhum) corpo else Path.combine(PathOperation.Union, corpo, ponta)
        val traco = Stroke(2.dp.toPx())
        val labio = LABIO_DO_BALAO.toPx()
        onDrawBehind {
            // Sombra macia, o lábio lilás sólido logo embaixo (`.balao`: 0 6px 0 #d7ccf7) e o corpo branco.
            translate(top = labio + 5.dp.toPx()) { drawPath(forma, Cores.Sombra.copy(alpha = 0.10f)) }
            translate(top = labio + 2.dp.toPx()) { drawPath(forma, Cores.Sombra.copy(alpha = 0.12f)) }
            translate(top = labio) { drawPath(forma, Cores.LabioClaro) }
            drawPath(forma, Degrades.Cartao)
            drawPath(forma, corDoContorno, style = traco)
        }
    }

/**
 * Mãozinha que mostra onde tocar, para quem ainda não lê. Aparece depois de [espera] ms
 * sem a criança tocar, com um círculo que "pinga" como um toque.
 */
@Composable
fun MaoGuia(modifier: Modifier = Modifier, ativa: Boolean = true, espera: Long = 3500) {
    var visivel by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(ativa) {
        visivel = false
        if (ativa) {
            kotlinx.coroutines.delay(espera)
            visivel = true
        }
    }
    if (!visivel) return
    val t = rememberInfiniteTransition(label = "mao")
    val fase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1100)), label = "fase")
    Box(modifier.size(64.dp), contentAlignment = Alignment.TopStart) {
        Canvas(Modifier.size(40.dp)) {
            val p = (fase * 1.6f).coerceAtMost(1f)
            drawCircle(Color.White.copy(alpha = 0.8f * (1f - p)), radius = size.minDimension * (0.2f + 0.5f * p), style = androidx.compose.ui.graphics.drawscope.Stroke(4.dp.toPx()))
        }
        val desce = if (fase < 0.25f) fase * 4f else if (fase < 0.5f) (0.5f - fase) * 4f else 0f
        MaoDeToque(apertando = desce > 0.7f, subida = 8.dp * (1f - desce))
    }
}

/** Onde fica a ponta do dedo da mãozinha, a partir do canto de cima à esquerda da caixa de 64 dp. */
private val PONTA_DA_MAO = 20.dp

/**
 * A mãozinha 3D (`ui_mao_toque` ou, com o dedo encostado, `ui_mao_toque_apertando`): a âncora `ponta` do
 * manifesto fica em [PONTA_DA_MAO], [subida] acima. Sem a arte, o emoji 👆 de antes. Decorativa.
 */
@Composable
private fun MaoDeToque(apertando: Boolean, subida: Dp) {
    val id = if (apertando) PecasDasTelas.MAO_APERTANDO else PecasDasTelas.MAO_TOQUE
    val contexto = androidx.compose.ui.platform.LocalContext.current
    val info = remember(id) { ArteDoApp.info(contexto, id) }
    val emoji: @Composable () -> Unit = {
        Text("👆", fontSize = 40.sp, modifier = Modifier.padding(start = 12.dp, top = 14.dp).offset(y = -subida))
    }
    if (info == null) {
        emoji()
        return
    }
    val lado = 56.dp
    val (fx, fy) = info.ancoras["ponta"]?.let(info::fracao) ?: (0.36f to 0.16f)
    ImagemDeArte(
        id,
        Modifier
            .offset(x = PONTA_DA_MAO - lado * fx, y = PONTA_DA_MAO - lado * fy - subida)
            .size(lado),
    )
}

/**
 * A mãozinha da [MaoGuia] segurando e arrastando (a dica de que dá para mudar as coisas de lugar).
 * Quem chama posiciona e move: a ponta do dedo fica a 20 dp da esquerda e do topo, como na [MaoGuia].
 * [apertando]: o dedo encostado, com um anel firme embaixo; solta, o anel volta a "pingar".
 */
@Composable
fun MaoQueArrasta(apertando: Boolean, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "maoQueArrasta")
    val pulso by t.animateFloat(0f, 1f, infiniteRepeatable(tween(900)), label = "pulso")
    Box(modifier.size(64.dp), contentAlignment = Alignment.TopStart) {
        Canvas(Modifier.size(40.dp)) {
            val traco = androidx.compose.ui.graphics.drawscope.Stroke(4.dp.toPx())
            if (apertando) {
                drawCircle(Color.White.copy(alpha = 0.5f), radius = size.minDimension * 0.34f)
                drawCircle(Color.White, radius = size.minDimension * 0.34f, style = traco)
            } else {
                drawCircle(Color.White.copy(alpha = 0.8f * (1f - pulso)), radius = size.minDimension * (0.2f + 0.5f * pulso), style = traco)
            }
        }
        MaoDeToque(apertando = apertando, subida = if (apertando) 0.dp else 8.dp)
    }
}

/** Pequena animação de "respirar" para chamar a atenção (itens novos no baú, botão principal). */
@Composable
fun Modifier.pulsando(ativo: Boolean = true): Modifier {
    if (!ativo) return this
    val transicao = rememberInfiniteTransition(label = "pulso")
    val escala: Float by transicao.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "escala",
    )
    return this.scale(escala)
}
