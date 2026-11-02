package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/** Pequena animação de "respirar" para chamar a atenção (itens novos no baú, botão principal). */
@Composable
fun Modifier.pulsando(ativo: Boolean = true): Modifier {
    if (!ativo) return this
    val t = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulso")
    val s by t.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(700),
            androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "s",
    )
    return this.scale(s)
}
