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
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.ui.componentes.MaoGuia
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
