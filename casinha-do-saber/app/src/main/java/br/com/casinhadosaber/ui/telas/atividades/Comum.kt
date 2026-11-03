package br.com.casinhadosaber.ui.telas.atividades

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Pescaria
import br.com.casinhadosaber.dominio.EstourarBaloes
import br.com.casinhadosaber.dominio.LigarPares
import br.com.casinhadosaber.dominio.QuebraCabeca
import br.com.casinhadosaber.dominio.Trem
import br.com.casinhadosaber.dominio.Balanca
import br.com.casinhadosaber.dominio.CacaNaCena
import br.com.casinhadosaber.dominio.SigaASequencia
import br.com.casinhadosaber.dominio.PintarPorCodigo
import br.com.casinhadosaber.dominio.Ditado
import br.com.casinhadosaber.dominio.Domino
import br.com.casinhadosaber.dominio.EscondeEsconde
import br.com.casinhadosaber.dominio.CartasViradas
import br.com.casinhadosaber.dominio.Mercadinho
import br.com.casinhadosaber.dominio.Relogio
import br.com.casinhadosaber.dominio.ProgramarRobo
import br.com.casinhadosaber.dominio.MontarFrase
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadePescaria
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeBaloes
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeLigarPares
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeQuebraCabeca
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeTrem
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeBalanca
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeCacaNaCena
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeSigaASequencia
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadePintar
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeDitado
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeDomino
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeEscondeEsconde
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeCartasViradas
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeMercadinho
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeRelogio
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeProgramarRobo
import br.com.casinhadosaber.ui.telas.atividades.novas.AtividadeMontarFrase
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.ui.componentes.MaoGuia
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Desenha a atividade de mexer de uma pergunta (cesta, memória, montar a palavra, traçar).
 *
 * Toda atividade termina chamando [onConcluir] uma única vez, dizendo se a criança fez sem errar
 * (conta como "acertou de primeira"). [onErro] avisa a tela do jogo de cada erro (a criança
 * personagem fica pensativa); o som e a fala do erro ficam por conta da própria atividade.
 */
@Composable
fun AtividadeInterativa(
    atividade: Atividade,
    /** Fala do enunciado (repetida quando a criança precisa de ajuda). */
    fala: List<Trecho>,
    /** Só a pergunta da vez aceita toques (a anterior ainda pode estar saindo da tela). */
    ativa: Boolean,
    /** Exemplo resolvido junto (rodada facilitada): a ajuda aparece desde o começo. */
    ajudaInicial: Boolean,
    cor: Color,
    onErro: () -> Unit,
    onConcluir: (semErro: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (atividade) {
        is Atividade.Cesta -> AtividadeCesta(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is Atividade.Memoria -> AtividadeMemoria(atividade, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is Atividade.MontarPalavra -> AtividadeMontarPalavra(atividade, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is Atividade.Tracar -> AtividadeTracar(atividade, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        Atividade.Escolha -> Unit
        // Tipos novos (docs/plano-1000-fases.md): cada um tem a sua tela em atividades/novas/.
        is Pescaria -> AtividadePescaria(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is EstourarBaloes -> AtividadeBaloes(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is LigarPares -> AtividadeLigarPares(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is QuebraCabeca -> AtividadeQuebraCabeca(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is Trem -> AtividadeTrem(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is Balanca -> AtividadeBalanca(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is CacaNaCena -> AtividadeCacaNaCena(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is SigaASequencia -> AtividadeSigaASequencia(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is PintarPorCodigo -> AtividadePintar(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is Ditado -> AtividadeDitado(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is Domino -> AtividadeDomino(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is EscondeEsconde -> AtividadeEscondeEsconde(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is CartasViradas -> AtividadeCartasViradas(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is Mercadinho -> AtividadeMercadinho(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is Relogio -> AtividadeRelogio(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is ProgramarRobo -> AtividadeProgramarRobo(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
        is MontarFrase -> AtividadeMontarFrase(atividade, fala, ativa, ajudaInicial, cor, onErro, onConcluir, modifier)
    }
}

/** Quantos erros até a ajuda aparecer (igual às perguntas de tocar). */
internal const val ERROS_PARA_AJUDA = 2

// ------------------------------------------------------------------------------------- peças que se arrastam

/**
 * Uma peça que a criança arrasta. A posição na tela é: lugar de descanso (calculado por quem desenha)
 * + [desvio] (animação de ir e voltar) + [arrasto] (o dedo).
 */
@Stable
internal class PecaMovel {
    val desvio = Animatable(Offset.Zero, Offset.VectorConverter)
    var arrasto by mutableStateOf(Offset.Zero)
    var arrastando by mutableStateOf(false)
    val tremida = Animatable(0f)

    /**
     * A peça foi solta: ela continua onde o dedo largou ([deslocamento] em relação ao seu novo lugar
     * de descanso, já atualizado pelo chamador) e desliza até lá.
     */
    suspend fun pousar(deslocamento: Offset, spec: AnimationSpec<Offset>) {
        desvio.snapTo(deslocamento)
        arrasto = Offset.Zero
        arrastando = false
        desvio.animateTo(Offset.Zero, spec)
    }

    suspend fun tremer() {
        for (x in listOf(10f, -10f, 7f, -7f, 3f, 0f)) tremida.animateTo(x, tween(55))
    }
}

// ------------------------------------------------------------------------------------- mãozinha que mostra o gesto

/**
 * A mãozinha guia ([MaoGuia]) andando pelos [pontos] (em pixels, no espaço do pai): mostra o gesto de
 * arrastar ou de traçar. Aparece depois de [espera] ms.
 */
@Composable
internal fun MaoQueSegue(pontos: List<Offset>, ativa: Boolean, espera: Long = 3500L) {
    if (!ativa || pontos.isEmpty()) return
    val t = rememberInfiniteTransition(label = "maoQueSegue")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "p")
    // O ponto de toque da MaoGuia fica a 20dp do canto (o círculo que "pinga").
    val meio = with(LocalDensity.current) { 20.dp.toPx() }
    MaoGuia(
        Modifier.offset {
            val k = FastOutSlowInEasing.transform(((p - 0.2f) / 0.6f).coerceIn(0f, 1f))
            val pos = pontoNoCaminho(pontos, k)
            IntOffset((pos.x - meio).roundToInt(), (pos.y - meio).roundToInt())
        },
        ativa = true,
        espera = espera,
    )
}

/**
 * Mostra o gesto de ARRASTAR (e não de tocar): a mãozinha aperta a figura em [origem], leva uma
 * cópia dela ([carga]) até [destino] deixando um rastro de bolinhas, solta e some; depois repete.
 * Fica numa camada por cima (sem receber toques). Aparece [espera] ms depois de [ativa].
 * Posições em pixels, no espaço do pai (que deve ocupar a área toda da atividade).
 */
@Composable
internal fun GestoDeArrastar(
    origem: Offset,
    destino: Offset,
    ativa: Boolean,
    cor: Color,
    espera: Long = 0L,
    tamanhoCarga: Dp = 72.dp,
    /** Cópia da figura que a mão carrega (vazio: a mão vai sozinha). */
    carga: @Composable () -> Unit = {},
) {
    var visivel by remember { mutableStateOf(false) }
    LaunchedEffect(ativa, espera) {
        visivel = false
        if (ativa) {
            delay(espera)
            visivel = true
        }
    }
    if (!visivel || !ativa) return
    val t = rememberInfiniteTransition(label = "gestoDeArrastar")
    val f by t.animateFloat(0f, 1f, infiniteRepeatable(tween(3400, easing = LinearEasing)), label = "f")
    val densidade = LocalDensity.current
    val meiaCarga = with(densidade) { tamanhoCarga.toPx() / 2f }
    val ladoMao = 60.dp
    val ladoMaoPx = with(densidade) { ladoMao.toPx() }

    // Fases: 0 a 0,14 a mão chega e aperta; 0,14 a 0,7 leva; 0,7 a 0,8 solta; até 1 some.
    fun andou(f: Float) = FastOutSlowInEasing.transform(((f - 0.14f) / 0.56f).coerceIn(0f, 1f))
    fun onde(f: Float) = origem + (destino - origem) * andou(f)
    fun opacidade(f: Float) = when {
        f < 0.08f -> f / 0.08f
        f > 0.84f -> ((1f - f) / 0.16f).coerceIn(0f, 1f)
        else -> 1f
    }

    // Rastro de bolinhas e o alvo piscando.
    Canvas(Modifier.fillMaxSize()) {
        val a = opacidade(f)
        val bolinha = 7.dp.toPx()
        val efeito = PathEffect.dashPathEffect(floatArrayOf(0.1f, 17.dp.toPx()))
        drawLine(Color.White.copy(alpha = 0.55f * a), origem, destino, strokeWidth = bolinha * 1.5f, cap = StrokeCap.Round, pathEffect = efeito)
        drawLine(cor.copy(alpha = 0.3f * a), origem, destino, strokeWidth = bolinha, cap = StrokeCap.Round, pathEffect = efeito)
        if (andou(f) > 0f) {
            drawLine(cor.copy(alpha = 0.95f * a), origem, onde(f), strokeWidth = bolinha * 1.25f, cap = StrokeCap.Round, pathEffect = efeito)
        }
        val anel = (f * 2f) % 1f
        drawCircle(
            cor.copy(alpha = 0.7f * a * (1f - anel)),
            radius = 16.dp.toPx() + 26.dp.toPx() * anel,
            center = destino,
            style = Stroke(4.dp.toPx()),
        )
    }
    // A cópia da figura, carregada pela mão.
    run {
        Box(
            Modifier
                .offset {
                    val q = onde(f)
                    IntOffset((q.x - meiaCarga).roundToInt(), (q.y - meiaCarga).roundToInt())
                }
                .size(tamanhoCarga)
                .graphicsLayer {
                    val pegou = ((f - 0.08f) / 0.06f).coerceIn(0f, 1f)
                    val soltou = ((f - 0.72f) / 0.12f).coerceIn(0f, 1f)
                    alpha = 0.9f * pegou * (1f - soltou)
                    val escala = 1.12f - 0.3f * soltou
                    scaleX = escala
                    scaleY = escala
                },
            contentAlignment = Alignment.Center,
        ) { carga() }
    }
    // A mão: a ponta do dedo fica no centro da figura; aperta ao pegar e solta no destino.
    Box(
        Modifier
            .offset {
                val q = onde(f)
                IntOffset((q.x - ladoMaoPx * 0.4f).roundToInt(), (q.y - ladoMaoPx * 0.08f).roundToInt())
            }
            .size(ladoMao)
            .graphicsLayer {
                alpha = opacidade(f)
                val aperto = when {
                    f < 0.06f -> 0f
                    f < 0.14f -> (f - 0.06f) / 0.08f
                    f < 0.72f -> 1f
                    f < 0.8f -> 1f - (f - 0.72f) / 0.08f
                    else -> 0f
                }
                val escala = 1f - 0.16f * aperto
                scaleX = escala
                scaleY = escala
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.4f, 0.08f)
            },
    ) {
        Text("👆", fontSize = 44.sp, lineHeight = 50.sp)
    }
}

/** Ponto a uma fração [k] (0 a 1) do comprimento da linha [pontos]. */
internal fun pontoNoCaminho(pontos: List<Offset>, k: Float): Offset {
    if (pontos.size == 1) return pontos[0]
    val trechos = pontos.zipWithNext()
    val total = trechos.sumOf { (a, b) -> (b - a).getDistance().toDouble() }.toFloat()
    if (total <= 0f) return pontos[0]
    var falta = k.coerceIn(0f, 1f) * total
    for ((a, b) in trechos) {
        val d = (b - a).getDistance()
        if (falta <= d && d > 0f) return a + (b - a) * (falta / d)
        falta -= d
    }
    return pontos.last()
}

// ------------------------------------------------------------------------------------- figuras e nomes

/** Separa um texto de emojis em figuras (respeita seletor de variação, tom de pele e emojis unidos). */
internal fun figurasDe(s: String): List<String> {
    val partes = mutableListOf<String>()
    var i = 0
    while (i < s.length) {
        val inicio = i
        i += Character.charCount(s.codePointAt(i))
        while (i < s.length) {
            val cp = s.codePointAt(i)
            when {
                cp == 0xFE0F || cp == 0x20E3 || cp in 0x1F3FB..0x1F3FF -> i += Character.charCount(cp)
                cp == 0x200D -> {
                    i += 1
                    if (i < s.length) i += Character.charCount(s.codePointAt(i))
                }
                else -> break
            }
        }
        partes += s.substring(inicio, i)
    }
    return partes
}

/** Letra, número ou sílaba (e não emoji)? */
internal fun ehTexto(s: String) = s.isNotEmpty() && s.all { it.isLetterOrDigit() }

private fun semVariacao(s: String) = s.replace("\uFE0F", "")

/** Nome em português das figuras que o app conhece (para a criança ouvir o nome do que tocou). */
private val nomesDasFiguras: Map<String, String> by lazy {
    val m = mutableMapOf<String, String>()
    with(Vocabulario) {
        (animais + frutas + corpo + casa + brinquedos + roupas + comidas + tempo + familia + acoes + veiculos + formas)
            .forEach { if (it.emoji.isNotEmpty()) m.putIfAbsent(semVariacao(it.emoji), it.pt) }
        todasAsPalavras.forEach { m.putIfAbsent(semVariacao(it.emoji), it.texto.lowercase()) }
        objetosParaContar.forEach { m.putIfAbsent(semVariacao(it.emoji), it.singular) }
    }
    m
}

internal fun nomeDaFigura(emoji: String): String? = nomesDasFiguras[semVariacao(emoji)]
