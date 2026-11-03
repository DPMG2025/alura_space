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
 * Botão "fofinho": tem uma base mais escura embaixo (efeito 3D) e afunda quando é pressionado.
 *
 * Visual do sistema de design: faixa de brilho no terço de cima ([brilho]), contorno de 2 dp
 * ([contorno], ameixa a 25%, que sobre qualquer cor vira um tom mais escuro dela) e sombra suave
 * embaixo da base ([sombra]). Ao apertar, além de afundar, amassa (1,04 × 0,94) e volta com mola.
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
    /** Faixa de brilho no terço de cima. */
    brilho: Boolean = true,
    /** Cor do contorno de 2 dp da face e da base; `null` tira o contorno. */
    contorno: Color? = Cores.Contorno,
    /** Sombra suave embaixo da base. */
    sombra: Boolean = true,
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
                .background(corFace)
                .then(if (brilho) Modifier.brilhoDoTopo() else Modifier)
                .then(if (contorno != null) Modifier.border(2.dp, contorno, forma) else Modifier)
                .graphicsLayer { alpha = if (habilitado) 1f else 0.8f },
            contentAlignment = Alignment.Center,
        ) {
            conteudo()
        }
    }
}

/**
 * Base 3D do [BotaoFofo] desenhada atrás de tudo: a sombra suave (duas camadas que somem para baixo),
 * a base escura deslocada [profundidade] para baixo e o contorno dela. Funciona com qualquer [Shape].
 */
private fun Modifier.baseDoBotao(forma: Shape, cor: Color, profundidade: Dp, sombra: Color?, contorno: Color?): Modifier =
    drawWithCache {
        val p = profundidade.toPx()
        val tamanho = Size(size.width, size.height - p)
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
 * Ladrilho/cartão do sistema de design desenhado atrás do conteúdo: sombra suave, "lábio" colorido de
 * [profundidade] embaixo (reserva o espaço com um padding), corpo [cor] com um leve degradê e contorno.
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
        val corpo = Brush.verticalGradient(listOf(cor, lerp(cor, Color(0xFFEDE6FF), 0.25f)))
        val traco = Stroke(2.dp.toPx())
        onDrawBehind {
            if (sombra) drawRoundRect(Cores.Sombra, topLeft = Offset(0f, p + 3.dp.toPx()), size = size, cornerRadius = r)
            if (labio != null) {
                drawRoundRect(labio, topLeft = Offset(0f, p), size = size, cornerRadius = r)
                if (contorno != null) drawRoundRect(contorno, topLeft = Offset(0f, p), size = size, cornerRadius = r, style = traco)
            }
            drawRoundRect(corpo, size = size, cornerRadius = r)
            if (contorno != null) drawRoundRect(contorno, size = size, cornerRadius = r, style = traco)
        }
    }

fun escurecer(c: Color, fator: Float = 0.78f) = Color(c.red * fator, c.green * fator, c.blue * fator, c.alpha)

/** Cor da base 3D de um botão [cor]: um tom mais escuro dela; para o branco, um lilás claro (o cinza parecia sujo). */
fun baseDe(cor: Color): Color = if (cor == Color.White) BaseLilas else escurecer(cor)

/** Botão redondo pequeno com um emoji (voltar, som, ouvir de novo). Disco sólido com base lilás. */
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
    BotaoFofo(
        onClick = onClick,
        modifier = modifier,
        cor = cor,
        corBase = corBase,
        forma = CircleShape,
        descricao = descricao,
        habilitado = habilitado,
        profundidade = 4.dp,
    ) {
        Box(Modifier.size(tamanho), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = (tamanho.value * 0.45f).sp)
        }
    }
}

/** Base dos botões brancos: lilás claro e sólido (a base translúcida de antes sumia sobre a cena). */
private val BaseLilas = Color(0xFFD9CFF2)

@Composable
fun Estrela(acesa: Boolean, modifier: Modifier = Modifier, tamanho: Dp = 24.dp) {
    Canvas(modifier.size(tamanho)) {
        val c = Offset(size.width / 2, size.height / 2)
        drawPath(caminhoEstrela(c, size.minDimension / 2), if (acesa) Cores.Estrela else Cores.EstrelaApagada)
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
 * Balão de fala da criança personagem: branco para lilás de cima para baixo, contorno lilás, sombra
 * suave e uma ponta apontando para quem fala ([cauda]; na tela do jogo a criança fica à esquerda).
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

/**
 * Desenho do balão atrás do conteúdo, num caminho só (corpo arredondado unido à ponta), para o
 * contorno passar por fora da ponta sem emenda. Reserva com padding o espaço da ponta.
 */
fun Modifier.balaoDeFala(
    cauda: LadoDaCauda = LadoDaCauda.Esquerda,
    raio: Dp = 20.dp,
    corDoContorno: Color = Color(0xFFE2D8FF),
): Modifier = this
    .padding(
        start = if (cauda == LadoDaCauda.Esquerda) CAUDA else 0.dp,
        end = if (cauda == LadoDaCauda.Direita) CAUDA else 0.dp,
        bottom = (if (cauda == LadoDaCauda.Baixo) CAUDA else 0.dp) + 3.dp,
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
        val traco = Stroke(3.dp.toPx())
        val sombraY = 3.dp.toPx()
        onDrawBehind {
            translate(top = sombraY) { drawPath(forma, Cores.Sombra.copy(alpha = 0.16f)) }
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
        Text(
            "👆",
            fontSize = 40.sp,
            modifier = Modifier
                .padding(start = 12.dp, top = 14.dp)
                .offset(y = (-8).dp + 8.dp * desce),
        )
    }
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
        Text(
            "👆",
            fontSize = 40.sp,
            modifier = Modifier
                .padding(start = 12.dp, top = 14.dp)
                .offset(y = if (apertando) 0.dp else (-8).dp),
        )
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
