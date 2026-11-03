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
import androidx.compose.ui.draw.shadow
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

/**
 * Botão "fofinho": tem uma base mais escura embaixo (efeito 3D) e afunda quando é pressionado.
 */
@Composable
fun BotaoFofo(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color = Cores.Roxo,
    corBase: Color = escurecer(cor),
    forma: Shape = RoundedCornerShape(24.dp),
    descricao: String? = null,
    habilitado: Boolean = true,
    profundidade: Dp = 6.dp,
    /** Ocupa todo o espaço que o pai der (para botões que crescem com a tela). */
    preencher: Boolean = false,
    conteudo: @Composable () -> Unit,
) {
    val interacao = remember { MutableInteractionSource() }
    val pressionado by interacao.collectIsPressedAsState()
    val afundar by animateFloatAsState(if (pressionado) 1f else 0f, spring(stiffness = Spring.StiffnessHigh), label = "afundar")
    Box(
        modifier
            .semantics { descricao?.let { contentDescription = it } }
            .clickable(interacao, indication = null, enabled = habilitado, role = Role.Button, onClick = onClick)
            .padding(bottom = profundidade),
    ) {
        Box(
            Modifier
                .matchParentSize()
                .offset(y = profundidade)
                .clip(forma)
                .background(corBase),
        )
        Box(
            Modifier
                .then(if (preencher) Modifier.fillMaxSize() else Modifier)
                .offset(y = profundidade * afundar)
                .clip(forma)
                .background(cor),
            contentAlignment = Alignment.Center,
        ) {
            conteudo()
        }
    }
}

fun escurecer(c: Color, fator: Float = 0.78f) = Color(c.red * fator, c.green * fator, c.blue * fator, c.alpha)

/** Botão redondo pequeno com um emoji (voltar, som, ouvir de novo). */
@Composable
fun BotaoRedondo(
    emoji: String,
    descricao: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cor: Color = Color.White,
    tamanho: Dp = 56.dp,
) {
    BotaoFofo(
        onClick = onClick,
        modifier = modifier,
        cor = cor,
        corBase = Color(0x33000000),
        forma = CircleShape,
        descricao = descricao,
        profundidade = 4.dp,
    ) {
        Box(Modifier.size(tamanho), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = (tamanho.value * 0.45f).sp)
        }
    }
}

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

/** Balão de fala da criança personagem. */
@Composable
fun BalaoDeFala(texto: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(3.dp, Color(0xFFEDE6FF), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(texto, style = MaterialTheme.typography.titleMedium)
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
