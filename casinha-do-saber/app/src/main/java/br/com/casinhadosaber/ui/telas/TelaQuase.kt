package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.AnelDeToque
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.CorDaConta
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Estrela
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.Irmaozinho
import br.com.casinhadosaber.ui.componentes.PecasDoPremio
import br.com.casinhadosaber.ui.componentes.Pilula
import br.com.casinhadosaber.ui.componentes.TextoComContorno
import br.com.casinhadosaber.ui.componentes.animacoesLigadas
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay

/** Céu do Quase: azul em cima, pêssego e rosa embaixo (o céu das ilhas do PRD), mais calmo que o roxo do prêmio. */
private val FUNDO_DO_QUASE = Brush.verticalGradient(
    0f to Color(0xFF8FCBFF),
    0.55f to Color(0xFFFFDCC4),
    1f to Color(0xFFFFC6DA),
)

/**
 * A fase não foi vencida (menos de 4 acertos de primeira). Sem bronca: a estrela da fase está dormindo e a
 * sementinha cresce um pouco a cada acerto de primeira (`quase_semente_1..4`; a flor é vencer a fase). As contas
 * mostram os acertos, com o presente fechado na quarta, e a criança é convidada a tentar de novo.
 */
@Composable
fun TelaQuase(
    fase: Fase,
    acertos: Int,
    perfil: Perfil?,
    onTentarDeNovo: () -> Unit,
    onVoltar: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val animado = animacoesLigadas()
    val minimo = EstadoJogo.MINIMO_PARA_VENCER
    val estagio = PecasDoPremio.estagioDaSemente(acertos, minimo)
    // A semente nasce pequena e cresce, quadro a quadro, até o tamanho que os acertos deram.
    var visivel by remember { mutableIntStateOf(if (animado) 1 else estagio) }
    val pulo = remember { Animatable(1f) }
    val boia = rememberInfiniteTransition(label = "estrelaDormindo")
    val sobe by boia.animateFloat(-4f, 4f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "sobe")

    LaunchedEffect(Unit) {
        sons.toque()
        narrador.falar(
            "Quase! Você acertou $acertos de primeira. O presente quer sair da caixa! " +
                "Para abrir, acerte quatro de primeira. Vamos tentar de novo?",
        )
    }
    LaunchedEffect(Unit) {
        while (visivel < estagio) {
            delay(650)
            visivel++
        }
    }
    LaunchedEffect(visivel) {
        if (visivel > 1) {
            pulo.snapTo(0.82f)
            pulo.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 300f))
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(FUNDO_DO_QUASE)) {
        val alta = maxHeight > 720.dp
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TextoComContorno("Quase!", estilo = MaterialTheme.typography.displayLarge)
            TextoComContorno(
                "A sementinha está crescendo!",
                estilo = MaterialTheme.typography.titleLarge,
                espessura = 3.dp,
                profundidade = 2.dp,
                maxLinhas = 2,
            )
            Spacer(Modifier.height(10.dp))
            // Cinco contas: verdes as que acertou de primeira; depois da quarta (o alvo), o presente fechado.
            Pilula(descricao = "Você acertou $acertos de primeira. Para abrir o presente, acerte $minimo.") {
                repeat(5) { k ->
                    val alvo = k == minimo - 1
                    ImagemDeArte(if (k < acertos) CorDaConta.VERDE.id else CorDaConta.VAZIA, Modifier.size(if (alvo) 30.dp else 24.dp))
                    if (alvo) {
                        // O presente 3D (`premio_presente_fechado`) grande o bastante para se ver o laço e o brilho,
                        // maior que as contas (nunca o 🎁 do sistema).
                        ImagemDeArte(
                            PecasDoPremio.PRESENTE_FECHADO,
                            Modifier.size(TAMANHO_DO_PRESENTE),
                            descricao = null,
                        ) { PresenteFechado(Modifier.size(TAMANHO_DO_PRESENTE)) }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // A cena: luz macia, a estrela da fase dormindo (ela acorda quando a fase for vencida) e a semente.
            val alturaDaCena = if (alta) 250.dp else 200.dp
            Box(Modifier.size(alturaDaCena * 1.2f, alturaDaCena)) {
                Canvas(Modifier.fillMaxSize()) {
                    val c = Offset(size.width * 0.45f, size.height * 0.62f)
                    val r = size.minDimension * 0.6f
                    drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.75f), Color.White.copy(alpha = 0f)), c, r), r, c)
                }
                ImagemDeArte(
                    PecasDoPremio.ESTRELA_DORMINDO,
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(alturaDaCena * 0.42f)
                        .graphicsLayer { translationY = if (animado) sobe.dp.toPx() else 0f; rotationZ = 8f },
                    descricao = "A estrela está dormindo",
                ) { Estrela(false, tamanho = alturaDaCena * 0.36f) }
                ImagemDeArte(
                    PecasDoPremio.idDaSemente(visivel),
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = -alturaDaCena * 0.06f)
                        .size(alturaDaCena * 0.62f, alturaDaCena * 0.86f)
                        .graphicsLayer {
                            transformOrigin = TransformOrigin(0.5f, 0.8f)
                            scaleX = 2f - pulo.value
                            scaleY = pulo.value
                        },
                    descricao = "A sementinha",
                )
            }
            Spacer(Modifier.height(8.dp))
            perfil?.let {
                Row(verticalAlignment = Alignment.Bottom) {
                    Crianca(it, Modifier.height(if (alta) 140.dp else 116.dp), humor = Humor.TORCENDO)
                    // O irmãozinho dá tchau, animando para tentar de novo.
                    if (it.irmao.ligado) Irmaozinho(it, Modifier.height(if (alta) 100.dp else 84.dp), acenando = true)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                BotaoRedondo("⬅️", "Voltar para o caminho", onVoltar, tamanho = 56.dp)
                Box {
                    BotaoFofo(onClick = onTentarDeNovo, cor = Cores.Certo, modifier = Modifier.pulsando(), descricao = "Tentar de novo") {
                        TextoComContorno(
                            "De novo",
                            estilo = MaterialTheme.typography.headlineMedium,
                            corDoContorno = Cores.ContornoEscuro,
                            // Com o anel do toque, o botão guarda um cantinho à direita para ele (sem cobrir o texto).
                            modifier = Modifier.padding(start = 26.dp, end = if (animado) 26.dp + ANEL_DO_BOTAO + 6.dp else 26.dp, top = 12.dp, bottom = 12.dp),
                        )
                    }
                    // O anel que pinga fica dentro da face do botão, no canto da direita: nunca passa da borda.
                    if (animado) {
                        AnelDeToque(
                            Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 10.dp, bottom = LABIO_DO_BOTAO)
                                .size(ANEL_DO_BOTAO)
                                .testTag("anel_de_toque"),
                            espera = 2500,
                        )
                    }
                }
            }
        }
    }
}

/** O presente 3D dentro da pílula das contas. */
private val TAMANHO_DO_PRESENTE = 46.dp

/** O anel do toque no botão "De novo" (cabe na altura da face) e o lábio do botão (6 dp, o padrão do BotaoFofo). */
private val ANEL_DO_BOTAO = 44.dp
private val LABIO_DO_BOTAO = 6.dp

/** Caixa de presente desenhada em Canvas (reserva do presente 3D nas contas): tampa, fita vermelha, laço e brilho. */
@Composable
private fun PresenteFechado(modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val contorno = Color(0xFF8A3B12)
        val traco = w * 0.025f
        val caixa = Color(0xFFFFB238)
        val caixaSombra = Color(0xFFF08A1C)
        val fita = Color(0xFFE53950)
        // sombra no chão
        drawOval(Color(0x29000000), Offset(w * 0.14f, h * 0.9f), Size(w * 0.72f, h * 0.08f))
        // corpo da caixa (com o lado direito mais escuro)
        val corpoTopo = h * 0.45f
        val corpo = androidx.compose.ui.geometry.Rect(w * 0.17f, corpoTopo, w * 0.83f, h * 0.93f)
        drawRoundRect(caixa, corpo.topLeft, corpo.size, CornerRadius(w * 0.04f))
        drawRect(caixaSombra, Offset(w * 0.6f, corpoTopo), Size(w * 0.23f, corpo.height - w * 0.04f))
        drawRoundRect(contorno, corpo.topLeft, corpo.size, CornerRadius(w * 0.04f), style = Stroke(traco))
        // tampa, um pouco mais larga
        val tampa = androidx.compose.ui.geometry.Rect(w * 0.11f, h * 0.33f, w * 0.89f, h * 0.48f)
        drawRoundRect(caixa, tampa.topLeft, tampa.size, CornerRadius(w * 0.04f))
        drawRect(caixaSombra, Offset(w * 0.6f, tampa.top + traco), Size(w * 0.29f - traco, tampa.height - traco * 2))
        drawRect(Color(0x55FFFFFF), Offset(w * 0.14f, tampa.top + traco), Size(w * 0.4f, tampa.height * 0.28f))
        drawRoundRect(contorno, tampa.topLeft, tampa.size, CornerRadius(w * 0.04f), style = Stroke(traco))
        // fita vertical e na tampa
        drawRect(fita, Offset(w * 0.44f, tampa.top), Size(w * 0.12f, corpo.bottom - tampa.top))
        drawLine(contorno, Offset(w * 0.44f, tampa.top), Offset(w * 0.44f, corpo.bottom), traco * 0.8f)
        drawLine(contorno, Offset(w * 0.56f, tampa.top), Offset(w * 0.56f, corpo.bottom), traco * 0.8f)
        drawLine(Color(0x66FFFFFF), Offset(w * 0.47f, tampa.top + traco), Offset(w * 0.47f, corpo.bottom - traco), traco * 0.8f)
        // laço: duas alças e as pontinhas
        val no = Offset(w * 0.5f, h * 0.31f)
        for (lado in listOf(-1f, 1f)) {
            val alca = Path().apply {
                moveTo(no.x, no.y)
                cubicTo(no.x + lado * w * 0.12f, no.y - h * 0.26f, no.x + lado * w * 0.36f, no.y - h * 0.16f, no.x + lado * w * 0.24f, no.y + h * 0.01f)
                cubicTo(no.x + lado * w * 0.18f, no.y + h * 0.04f, no.x + lado * w * 0.08f, no.y + h * 0.02f, no.x, no.y)
                close()
            }
            drawPath(alca, fita)
            drawPath(alca, contorno, style = Stroke(traco, join = androidx.compose.ui.graphics.StrokeJoin.Round))
            drawLine(Color(0x66FFFFFF), Offset(no.x + lado * w * 0.1f, no.y - h * 0.1f), Offset(no.x + lado * w * 0.18f, no.y - h * 0.11f), traco, StrokeCap.Round)
        }
        drawCircle(fita, w * 0.065f, no)
        drawCircle(contorno, w * 0.065f, no, style = Stroke(traco))
        // brilhinhos
        drawCircle(Color.White, w * 0.022f, Offset(w * 0.25f, h * 0.55f))
        drawCircle(Color(0xAAFFFFFF), w * 0.014f, Offset(w * 0.3f, h * 0.62f))
    }
}
