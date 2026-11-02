package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Cena
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BalaoDeFala
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Estrela
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val elogios = listOf("Muito bem!", "Isso aí!", "Acertou!", "Você é demais!", "Perfeito!", "Uau, que esperto!")

@Composable
fun TelaJogo(
    fase: Fase,
    perfil: Perfil?,
    onSair: () -> Unit,
    onConcluir: (erros: Int) -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val escopo = rememberCoroutineScope()
    val perguntas = remember(fase) { GeradorDeFases().perguntas(fase) }
    var indice by remember { mutableIntStateOf(0) }
    var erros by remember { mutableIntStateOf(0) }
    var erradas by remember { mutableStateOf(setOf<Int>()) }
    var acertou by remember { mutableStateOf(false) }
    var humor by remember { mutableStateOf(Humor.FELIZ) }
    var confete by remember { mutableIntStateOf(0) }
    val pergunta = perguntas[indice]
    val cor = Color(fase.trilha.corArgb)

    LaunchedEffect(indice) {
        delay(350)
        narrador.falar(pergunta.fala)
    }

    fun responder(i: Int) {
        if (acertou || i in erradas) return
        if (i == pergunta.indiceCorreto) {
            acertou = true
            humor = Humor.FELIZ
            confete++
            sons.acerto()
            val elogio = elogios.random()
            narrador.falar(listOf(pt(elogio)) + pergunta.reforco)
            val caracteres = elogio.length + pergunta.reforco.sumOf { it.texto.length }
            escopo.launch {
                delay((1200L + caracteres * 65L).coerceAtMost(5000L))
                if (indice < perguntas.lastIndex) {
                    indice++
                    erradas = emptySet()
                    acertou = false
                } else {
                    onConcluir(erros)
                }
            }
        } else {
            erros++
            erradas = erradas + i
            humor = Humor.PENSANDO
            sons.erro()
            narrador.falar(pergunta.opcoes[i].fala + pt("Tente outra vez!"))
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cor.copy(alpha = 0.35f), Cores.Fundo))),
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            // barra de cima
            Row(verticalAlignment = Alignment.CenterVertically) {
                BotaoRedondo("✖️", "Sair da fase", onSair, tamanho = 48.dp)
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    for (i in perguntas.indices) {
                        val feita = i < indice || (i == indice && acertou)
                        Estrela(feita, Modifier.padding(horizontal = 3.dp), tamanho = if (i == indice) 30.dp else 24.dp)
                    }
                }
                BotaoRedondo("🔊", "Ouvir de novo", { narrador.falar(pergunta.fala) }, tamanho = 48.dp)
            }

            // guia
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                perfil?.let { Crianca(it, Modifier.height(96.dp), acenando = acertou, humor = humor) }
                Spacer(Modifier.width(8.dp))
                BalaoDeFala(pergunta.instrucao, Modifier.weight(1f))
            }

            AnimatedContent(
                targetState = indice,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                },
                modifier = Modifier.weight(1f),
                label = "pergunta",
            ) { i ->
                val p = perguntas[i]
                Column(Modifier.fillMaxSize()) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CenaDaPergunta(p.cena)
                    }
                    Opcoes(
                        pergunta = p,
                        erradas = if (i == indice) erradas else emptySet(),
                        acertou = i == indice && acertou,
                        onTocar = ::responder,
                    )
                }
            }
        }
        Confete(confete)
    }
}

// ------------------------------------------------------------------------------------- cena

/** Quantas figuras há num texto de emojis (ignorando seletores de variação). */
private fun contarFiguras(s: String): Int =
    s.codePoints().filter { it != 0xFE0F && it != 0x200D }.count().toInt()

private fun separarFiguras(s: String): List<String> {
    val partes = mutableListOf<String>()
    var i = 0
    while (i < s.length) {
        val cp = s.codePointAt(i)
        var fim = i + Character.charCount(cp)
        if (fim < s.length && s.codePointAt(fim) == 0xFE0F) fim++
        partes += s.substring(i, fim)
        i = fim
    }
    return partes
}

private fun tamanhoParaFiguras(n: Int): TextUnit = when {
    n <= 1 -> 110.sp
    n <= 3 -> 72.sp
    n <= 6 -> 56.sp
    else -> 44.sp
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CenaDaPergunta(cena: Cena) {
    when (cena) {
        Cena.Nenhuma -> Unit
        is Cena.Emojis -> {
            val n = contarFiguras(cena.texto)
            Text(
                cena.texto,
                fontSize = tamanhoParaFiguras(n),
                lineHeight = tamanhoParaFiguras(n) * 1.15f,
                textAlign = TextAlign.Center,
                modifier = Modifier.pulsando(n == 1),
            )
        }
        is Cena.Palavra -> Text(
            cena.texto,
            style = MaterialTheme.typography.displayLarge.copy(letterSpacing = 4.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp),
        )
        is Cena.Conta -> Conta(cena)
        is Cena.Sequencia -> FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val tamanho = if (cena.itens.size > 6) 52.dp else 62.dp
            for (item in cena.itens) {
                val pergunta = item == "?"
                Box(
                    Modifier
                        .size(tamanho)
                        .then(if (pergunta) Modifier.pulsando() else Modifier)
                        .background(if (pergunta) Cores.Estrela else Color.White, RoundedCornerShape(16.dp))
                        .border(3.dp, if (pergunta) Color(0xFFE0A800) else Color(0xFFEDE6FF), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        item,
                        fontSize = (tamanho.value * 0.55f).sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Conta(c: Cena.Conta) {
    val tamanho = if (c.a + c.b > 6) 34.sp else 44.sp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (c.soma) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.Center,
            ) {
                GrupoDeFiguras(c.emoji, c.a, 0, tamanho)
                Text("+", style = MaterialTheme.typography.displayMedium, modifier = Modifier.align(Alignment.CenterVertically))
                GrupoDeFiguras(c.emoji, c.b, 0, tamanho)
            }
        } else {
            // Na subtração, as figuras que "foram embora" ficam apagadas e riscadas.
            GrupoDeFiguras(c.emoji, c.a, c.b, tamanho)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "${c.a} ${if (c.soma) "+" else "−"} ${c.b} = ?",
            style = MaterialTheme.typography.displayMedium,
            color = Cores.Texto,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrupoDeFiguras(emoji: String, total: Int, riscadas: Int, tamanho: TextUnit) {
    FlowRow(
        Modifier
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        maxItemsInEachRow = 5,
    ) {
        for (i in 0 until total) {
            val saiu = i >= total - riscadas
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = tamanho, modifier = Modifier.alpha(if (saiu) 0.3f else 1f))
                if (saiu) {
                    Canvas(Modifier.size((tamanho.value * 1.1f).dp)) {
                        val m = size.width * 0.15f
                        drawLine(Cores.Errado, Offset(m, m), Offset(size.width - m, size.height - m), 6.dp.toPx(), StrokeCap.Round)
                        drawLine(Cores.Errado, Offset(size.width - m, m), Offset(m, size.height - m), 6.dp.toPx(), StrokeCap.Round)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------- opções

@Composable
private fun Opcoes(pergunta: Pergunta, erradas: Set<Int>, acertou: Boolean, onTocar: (Int) -> Unit) {
    val grupos = pergunta.opcoes.any { (it.visual as? Visual.Emoji)?.let { v -> contarFiguras(v.emoji) > 1 } == true }
    val n = pergunta.opcoes.size
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when {
            grupos -> pergunta.opcoes.forEachIndexed { i, o ->
                CartaoOpcao(o, i, pergunta, erradas, acertou, onTocar, Modifier.fillMaxWidth(), altura = 84.dp)
            }
            n == 4 -> for (linha in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in linha * 2 until linha * 2 + 2) {
                        CartaoOpcao(pergunta.opcoes[i], i, pergunta, erradas, acertou, onTocar, Modifier.weight(1f))
                    }
                }
            }
            else -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pergunta.opcoes.forEachIndexed { i, o ->
                    CartaoOpcao(o, i, pergunta, erradas, acertou, onTocar, Modifier.weight(1f), altura = 130.dp)
                }
            }
        }
    }
}

@Composable
private fun CartaoOpcao(
    opcao: Opcao,
    i: Int,
    pergunta: Pergunta,
    erradas: Set<Int>,
    acertou: Boolean,
    onTocar: (Int) -> Unit,
    modifier: Modifier,
    altura: androidx.compose.ui.unit.Dp = 112.dp,
) {
    val certa = acertou && i == pergunta.indiceCorreto
    val errada = i in erradas
    val apagada = acertou && !certa
    val cor by animateColorAsState(
        when {
            certa -> Cores.Certo
            errada -> Color(0xFFFFE1E1)
            else -> Color.White
        },
        label = "cor",
    )
    val tremida = remember { Animatable(0f) }
    val pulo = remember { Animatable(1f) }
    LaunchedEffect(errada) {
        if (errada) for (x in listOf(14f, -14f, 9f, -9f, 4f, 0f)) tremida.animateTo(x, tween(45))
    }
    LaunchedEffect(certa) {
        if (certa) {
            pulo.animateTo(1.12f, tween(120))
            pulo.animateTo(1f, tween(160))
        }
    }
    BotaoFofo(
        onClick = { onTocar(i) },
        cor = cor,
        corBase = if (certa) Color(0xFF2A9452) else Color(0xFFD9CFF0),
        descricao = descreverOpcao(opcao),
        modifier = modifier
            .graphicsLayer {
                translationX = tremida.value
                scaleX = pulo.value
                scaleY = pulo.value
                alpha = if (apagada || errada) 0.45f else 1f
            },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = altura)
                .padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            VisualDaOpcao(opcao.visual, certa)
            if (certa || errada) {
                Text(
                    if (certa) "✔" else "✖",
                    color = if (certa) Color.White else Cores.Errado,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }
    }
}

private fun descreverOpcao(o: Opcao): String = when (val v = o.visual) {
    is Visual.Texto -> v.texto
    is Visual.Emoji -> o.fala.joinToString(" ") { it.texto }.ifBlank { v.emoji }
    is Visual.Cor -> o.fala.joinToString(" ") { it.texto }.ifBlank { "Cor" }
}

@Composable
private fun VisualDaOpcao(v: Visual, certa: Boolean) {
    when (v) {
        is Visual.Texto -> Text(
            v.texto,
            style = MaterialTheme.typography.displayLarge,
            color = if (certa) Color.White else Cores.Texto,
            maxLines = 1,
        )
        is Visual.Emoji -> {
            val figuras = separarFiguras(v.emoji)
            if (figuras.size > 1) {
                Text(v.emoji, fontSize = if (figuras.size > 4) 30.sp else 38.sp, textAlign = TextAlign.Center)
            } else {
                Text(v.emoji, fontSize = (54f * v.escala).sp, lineHeight = (60f * v.escala).sp)
            }
        }
        is Visual.Cor -> Box(
            Modifier
                .size(76.dp)
                .background(Color(v.argb), CircleShape)
                .border(4.dp, Color(0x22000000), CircleShape),
        )
    }
}
