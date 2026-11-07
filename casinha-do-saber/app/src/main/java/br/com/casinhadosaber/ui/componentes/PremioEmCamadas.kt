package br.com.casinhadosaber.ui.componentes

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Peças com a arte do Prêmio e do Quase (Fase 5 do PRD visual 3D). Todas as imagens passam por ImagemDeArte ou
// pelo cache da ArteDoApp, decodificadas no tamanho em que aparecem; nada fica guardado fora do cache.

/** Ponto da arte como fração da imagem, ou [padrao] quando a âncora (ou a imagem) falta no manifesto. */
private fun fracaoDaAncora(info: InfoDaArte?, nome: String, padrao: Pair<Float, Float>): Pair<Float, Float> {
    val ponto = info?.ancoras?.get(nome) ?: return padrao
    return info.fracao(ponto)
}

/**
 * Onde fica a base da caixa nos quadros do presente (frações do quadro): no meio da caixa (a âncora `boca`, não o
 * meio do quadro, que inclui a tampa voando à direita) e na altura do apoio.
 */
fun apoioDoPresente(contexto: Context): Pair<Float, Float> {
    val info = ArteDoApp.info(contexto, PecasDoPremio.quadroDoPresente(0)) ?: return 0.5f to 0.92f
    val boca = fracaoDaAncora(info, "boca", 0.5f to 0.6f)
    return boca.first to info.fracao(info.apoio).second
}

/** Proporção altura/largura dos quadros do presente (220 x 240 dp na arte). */
fun proporcaoDoPresente(contexto: Context): Float =
    ArteDoApp.info(contexto, PecasDoPremio.quadroDoPresente(0))?.let { it.altura.toFloat() / it.largura } ?: (240f / 220f)

/**
 * O presente que abre em quadros (`premio_presente_abrindo_0..4`): fechado, dá pulinhos chamando a criança e um
 * anel de toque aparece depois de um tempo; ao [aberto], passa pelos 5 quadros (a tampa sobe e voa, a luz acende)
 * e o [premio] sobe de dentro da caixa (da âncora `boca`) para o facho de luz, com o clarão girando atrás.
 * Sem a arte, [reserva] desenha a caixa fechada e o prêmio aparece no lugar dela.
 */
@Composable
fun PresenteQueAbre(
    aberto: Boolean,
    largura: Dp,
    modifier: Modifier = Modifier,
    reserva: @Composable (Modifier) -> Unit,
    premio: @Composable (Modifier) -> Unit,
) {
    val contexto = LocalContext.current
    val animado = animacoesLigadas()
    val proporcao = remember { proporcaoDoPresente(contexto) }
    val altura = largura * proporcao
    var quadro by remember { mutableStateOf(if (aberto) PecasDoPremio.QUADROS_DO_PRESENTE - 1 else 0) }
    val subida = remember { Animatable(if (aberto) 1f else 0f) }
    LaunchedEffect(aberto) {
        if (!aberto || quadro == PecasDoPremio.QUADROS_DO_PRESENTE - 1) return@LaunchedEffect
        if (!animado) {
            quadro = PecasDoPremio.QUADROS_DO_PRESENTE - 1
            subida.snapTo(1f)
            return@LaunchedEffect
        }
        while (quadro < PecasDoPremio.QUADROS_DO_PRESENTE - 1) {
            quadro++
            if (quadro == PecasDoPremio.QUADRO_DO_PREMIO) break
            delay(PecasDoPremio.DURACAO_DO_QUADRO)
        }
        // Do quadro do prêmio em diante, a caixa termina de abrir enquanto o prêmio sobe.
        coroutineScope {
            launch { subida.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 260f)) }
            while (quadro < PecasDoPremio.QUADROS_DO_PRESENTE - 1) {
                delay(PecasDoPremio.DURACAO_DO_QUADRO)
                quadro++
            }
        }
    }
    val infoDoQuadro = remember(quadro) { ArteDoApp.info(contexto, PecasDoPremio.quadroDoPresente(quadro)) }
    val temArte = infoDoQuadro != null
    val boca = remember(infoDoQuadro) { fracaoDaAncora(infoDoQuadro, "boca", 0.5f to 0.6f) }
    val movimento = rememberInfiniteTransition(label = "presente")
    // Fechado, a caixa dá pulinhos chamando; aberto, o clarão gira devagar.
    val chamar by movimento.animateFloat(-3.5f, 3.5f, infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "chamar")
    val giro by movimento.animateFloat(0f, 360f, infiniteRepeatable(tween(16000, easing = LinearEasing)), label = "giro")
    val densidade = LocalDensity.current
    Box(modifier.size(largura, altura)) {
        if (aberto && temArte) {
            val lado = largura * 1.05f
            ImagemDeArte(
                PecasDoPremio.CLARAO,
                Modifier
                    .size(lado)
                    .offset { with(densidade) { IntOffset((boca.first * largura.toPx() - lado.toPx() / 2).roundToInt(), (boca.second * altura.toPx() - lado.toPx() * 0.62f).roundToInt()) } }
                    .graphicsLayer {
                        rotationZ = if (animado) giro else 0f
                        alpha = subida.value.coerceIn(0f, 1f)
                    },
            )
        }
        ImagemDeArte(
            PecasDoPremio.quadroDoPresente(quadro),
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Os pulinhos giram a caixa pela base (a âncora da boca, embaixo).
                    transformOrigin = TransformOrigin(boca.first, 0.85f)
                    rotationZ = if (!aberto && animado) chamar else 0f
                },
        ) { if (!aberto) reserva(Modifier.fillMaxSize()) }
        if (aberto) {
            val lado = largura * 0.52f
            premio(
                Modifier
                    .size(lado)
                    .offset {
                        with(densidade) {
                            IntOffset(
                                (boca.first * largura.toPx() - lado.toPx() / 2).roundToInt(),
                                if (temArte) (boca.second * altura.toPx() - lado.toPx() / 2).roundToInt()
                                else ((altura.toPx() - lado.toPx()) / 2).roundToInt(),
                            )
                        }
                    }
                    .graphicsLayer {
                        val s = subida.value
                        val escala = 0.25f + 0.75f * s
                        scaleX = escala
                        scaleY = escala
                        // Sai da boca e sobe para o facho de luz, acima da caixa.
                        translationY = if (temArte) -s * altura.toPx() * 0.36f else 0f
                        alpha = (s * 3f).coerceIn(0f, 1f)
                    },
            )
        } else if (animado) {
            AnelDeToque(
                Modifier
                    .size(largura * 0.6f)
                    .offset { with(densidade) { IntOffset((boca.first * largura.toPx() - largura.toPx() * 0.3f).roundToInt(), (boca.second * altura.toPx() - largura.toPx() * 0.22f).roundToInt()) } },
            )
        }
    }
}

/**
 * Anéis brancos que "pingam" onde tocar (no lugar da mãozinha de emoji): aparecem depois de [espera] ms e se
 * abrem um atrás do outro. Decorativo.
 */
@Composable
fun AnelDeToque(modifier: Modifier = Modifier, espera: Long = 1200) {
    var visivel by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(espera)
        visivel = true
    }
    if (!visivel) return
    val t = rememberInfiniteTransition(label = "anel")
    val fase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1300, easing = LinearEasing)), label = "fase")
    Canvas(modifier.graphicsLayer()) {
        val r = size.minDimension / 2
        for (k in 0..1) {
            val p = (fase + k * 0.5f) % 1f
            drawCircle(Color.White.copy(alpha = 0.85f * (1f - p)), r * (0.3f + 0.7f * p), style = Stroke(5.dp.toPx() * (1f - p * 0.6f)))
        }
    }
}

/**
 * A pílula das moedas ganhas: as pilhas de moedas 3D que se contam (`premio_moedas_N`) e, ao lado, o número
 * (sempre acompanhado da imagem). Sem a arte, a moeda solta (`moeda`).
 */
@Composable
fun PilulaDeMoedas(moedas: Int, modifier: Modifier = Modifier, descricao: String = "Mais $moedas moedinhas") {
    val pilhas = remember(moedas) { PecasDoPremio.pilhasDeMoedas(moedas) }
    Pilula(modifier, descricao) {
        for (p in pilhas) {
            ImagemDeArte(PecasDoPremio.idDasMoedas(p), Modifier.size(40.dp)) { ImagemDeArte("moeda", Modifier.size(28.dp)) }
        }
        androidx.compose.material3.Text("+$moedas", maxLines = 1)
    }
}

/** A estrela grande do prêmio (`estrela3d`); a que não foi ganha fica de plástico cinza claro e translúcida. */
@Composable
fun EstrelaDoPremio(acesa: Boolean, tamanho: Dp, modifier: Modifier = Modifier) {
    val cinza = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    ImagemDeArte(
        PecasDoPremio.ESTRELA_GRANDE,
        modifier.size(tamanho).graphicsLayer { if (!acesa) alpha = 0.55f },
        filtro = if (acesa) null else cinza,
    ) { Estrela(acesa, tamanho = tamanho) }
}

/** Uma coisa no palco: de pé na âncora [ancora] do pódio (deslocada [desvioX] da largura), com o seu [conteudo]. */
class NoPalco(
    val ancora: String,
    val largura: Dp,
    val altura: Dp,
    val apoioX: Float = 0.5f,
    val apoioY: Float = 1f,
    val acima: Dp = 0.dp,
    val desvioX: Float = 0f,
    val desvioY: Float = 0f,
    val conteudo: @Composable (Modifier) -> Unit,
)

/** Âncoras do pódio na arte (`premio_podio`), para quando a imagem faltar. */
private val ANCORAS_DO_PODIO = mapOf(
    "centro" to (0.5f to 0.31f),
    "esquerda" to (0.22f to 0.34f),
    "direita" to (0.78f to 0.34f),
    "fundo" to (0.5f to 0.18f),
)

/**
 * O pódio branco de faixa amarela (`premio_podio`) com as [pecas] de pé nas âncoras dele (`esquerda`, `direita`,
 * `centro`), desenhadas na ordem dada (as de trás primeiro). O palco cresce para cima só o que as peças pedem.
 */
@Composable
fun PalcoDaFesta(largura: Dp, pecas: List<NoPalco>, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val info = remember { ArteDoApp.info(contexto, PecasDoPremio.PODIO) }
    val alturaDoPodio = largura * (info?.let { it.altura.toFloat() / it.largura } ?: (120f / 340f))
    val palco = remember(info, largura, pecas) {
        montarPalco(
            largura.value,
            alturaDoPodio.value,
            pecas.map { p ->
                val a = fracaoDaAncora(info, p.ancora, ANCORAS_DO_PODIO[p.ancora] ?: (0.5f to 0.3f))
                PecaDoPalco(a.first + p.desvioX, a.second + p.desvioY, p.largura.value, p.altura.value, p.apoioX, p.apoioY, p.acima.value)
            },
        )
    }
    Box(modifier.size(largura, palco.altura.dp)) {
        ImagemDeArte(PecasDoPremio.PODIO, Modifier.offset(y = palco.topoDoPodio.dp).size(largura, alturaDoPodio)) {
            // Sem a arte: uma elipse branca de palco.
            Canvas(Modifier.fillMaxSize()) {
                drawOval(Color(0x33231A3A), Offset(0f, size.height * 0.2f), size.copy(height = size.height * 0.8f))
                drawOval(Color.White, Offset.Zero, size.copy(height = size.height * 0.75f))
            }
        }
        for ((i, p) in pecas.withIndex()) {
            val c = palco.caixas[i]
            p.conteudo(Modifier.offset(c.x.dp, c.y.dp).size(c.largura.dp, c.altura.dp))
        }
    }
}

/** Um papel de confete solto: imagem, posição de partida, velocidade, giro e tamanho. */
private class PapelDaArte(
    val imagem: ImageBitmap,
    val angulo: Float,
    val velocidade: Float,
    val giro: Float,
    val escala: Float,
    val x: Float,
    val fase: Float,
)

/** Os confetes 3D decodificados para [lado] pixels (do cache da [ArteDoApp]); vazio sem a arte. */
@Composable
private fun lembrarConfetes(lado: Dp): List<ImageBitmap> {
    val contexto = LocalContext.current
    val px = with(LocalDensity.current) { lado.roundToPx() }
    return remember(px) {
        PecasDoPremio.idsDoConfete.mapNotNull { id ->
            val info = ArteDoApp.info(contexto, id) ?: return@mapNotNull null
            ArteDoApp.bitmap(contexto, info, amostraDaArte(info.largura, info.altura, px, px))?.asImageBitmap()
        }
    }
}

/**
 * Explosão de confete 3D (`premio_confete_*`: papéis, estrelas, bolinhas e serpentinas de seis cores) que sai do
 * meio da tela. Muda o [disparo] para soltar outra. Sem a arte, o [Confete] desenhado.
 */
@Composable
fun Confete3D(disparo: Int, modifier: Modifier = Modifier, quantidade: Int = 40) {
    if (disparo == 0) return
    val imagens = lembrarConfetes(24.dp)
    if (imagens.isEmpty()) {
        Confete(disparo, modifier, quantidade)
        return
    }
    val progresso = remember(disparo) { Animatable(0f) }
    val papeis = remember(disparo, imagens) {
        val r = Random(disparo)
        List(quantidade) {
            PapelDaArte(
                imagem = imagens[r.nextInt(imagens.size)],
                angulo = (r.nextFloat() * 160f + 190f) * (PI.toFloat() / 180f),
                velocidade = 0.6f + r.nextFloat() * 0.6f,
                giro = r.nextFloat() * 720f - 360f,
                escala = 0.8f + r.nextFloat() * 0.6f,
                x = 0f,
                fase = 0f,
            )
        }
    }
    LaunchedEffect(disparo) { progresso.animateTo(1f, tween(1600, easing = LinearEasing)) }
    val terminou = remember(progresso) { derivedStateOf { progresso.value >= 1f } }
    if (terminou.value) return
    Canvas(modifier.fillMaxSize().graphicsLayer()) {
        val t = progresso.value
        val origem = Offset(size.width / 2, size.height * 0.55f)
        val alcance = size.minDimension * 0.75f
        val alfa = if (t < 0.7f) 1f else ((1f - t) / 0.3f).coerceIn(0f, 1f)
        val base = 24.dp.toPx()
        for (p in papeis) {
            val d = alcance * p.velocidade * t
            val c = Offset(origem.x + cos(p.angulo) * d, origem.y + sin(p.angulo) * d + size.height * 0.45f * t * t)
            desenharPapel(p.imagem, c, base * p.escala, p.giro * t, alfa)
        }
    }
}

/**
 * Confete 3D caindo devagar pela tela inteira, sem parar (o fundo de festa da maquete 3). Uma camada só, que
 * redesenha poucas imagens pequenas; com as animações do sistema desligadas, os papéis ficam parados.
 */
@Composable
fun ChuvaDeConfete(modifier: Modifier = Modifier, quantidade: Int = 22) {
    val imagens = lembrarConfetes(20.dp)
    if (imagens.isEmpty()) return
    val papeis = remember(imagens) {
        val r = Random(5)
        List(quantidade) { i ->
            PapelDaArte(
                imagem = imagens[(i * 7 + r.nextInt(3)) % imagens.size],
                angulo = 0f,
                velocidade = 0.6f + r.nextFloat() * 0.5f,
                giro = r.nextFloat() * 300f - 150f,
                escala = 0.6f + r.nextFloat() * 0.5f,
                x = (i + r.nextFloat()) / quantidade,
                fase = r.nextFloat(),
            )
        }
    }
    val tempo = if (animacoesLigadas()) {
        rememberInfiniteTransition(label = "chuva").animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "t")
    } else {
        null
    }
    Spacer(
        modifier
            .fillMaxSize()
            .graphicsLayer()
            .drawWithCache {
                val base = 20.dp.toPx()
                onDrawBehind {
                    val t = tempo?.value ?: 0f
                    for (p in papeis) {
                        val y = ((p.fase + t * p.velocidade) % 1f) * (size.height + base * 2) - base
                        val x = p.x * size.width + sin((t * 2f + p.fase) * 2f * PI.toFloat()) * base
                        desenharPapel(p.imagem, Offset(x, y), base * p.escala, p.giro * (t * 6f + p.fase), 0.9f)
                    }
                }
            },
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.desenharPapel(imagem: ImageBitmap, c: Offset, lado: Float, giro: Float, alfa: Float) {
    val l = lado.roundToInt().coerceAtLeast(1)
    rotate(giro, c) {
        drawImage(
            imagem,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(imagem.width, imagem.height),
            dstOffset = IntOffset((c.x - l / 2f).roundToInt(), (c.y - l / 2f).roundToInt()),
            dstSize = IntSize(l, l),
            alpha = alfa,
        )
    }
}

/** A casinha branca do botão "Pôr na casinha" (como no protótipo): telhado em traço, corpo e a porta da cor do botão. */
@Composable
fun IconeDeCasaBranca(corDaPorta: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val u = size.minDimension / 24f
        val telhado = Path().apply {
            moveTo(3 * u, 11.5f * u); lineTo(12 * u, 4 * u); lineTo(21 * u, 11.5f * u)
        }
        val corpo = Path().apply {
            moveTo(6 * u, 10.5f * u); lineTo(6 * u, 20 * u); lineTo(18 * u, 20 * u); lineTo(18 * u, 10.5f * u); lineTo(12 * u, 5.6f * u); close()
        }
        // Sombra escura translúcida por baixo, como o contorno dos textos brancos do botão.
        val sombra = Offset(0f, 1.2f * u)
        translate(sombra.x, sombra.y) {
            drawPath(telhado, Color(0x40000000), style = Stroke(2.6f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
            drawPath(corpo, Color(0x40000000))
        }
        drawPath(corpo, Color.White)
        drawPath(telhado, Color.White, style = Stroke(2.6f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawRoundRect(corDaPorta, Offset(10.2f * u, 14 * u), androidx.compose.ui.geometry.Size(3.6f * u, 6 * u), androidx.compose.ui.geometry.CornerRadius(u))
    }
}
