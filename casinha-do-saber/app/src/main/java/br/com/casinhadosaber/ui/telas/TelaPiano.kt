package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.audio.SonsDoPiano
import br.com.casinhadosaber.dominio.Musica
import br.com.casinhadosaber.dominio.Nota
import br.com.casinhadosaber.dominio.Piano
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.EscalaDeTablet
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay

/**
 * Piano do modo criativo: oito teclas grandes e coloridas (de dó a dó) que tocam na hora, sem internet.
 * Livre, ou seguindo uma música de [Piano.musicas]: a tecla da vez brilha e só avança quando a criança toca nela
 * (tocar outra tecla só faz som, nunca é erro). Sem estrelas e sem relógio.
 */
@Composable
fun TelaPiano(
    onVoltar: () -> Unit,
    /** Para as capturas: abre já seguindo esta música, nesta nota. */
    musicaInicial: String? = null,
    posicaoInicial: Int = 0,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val piano = remember(sons) { SonsDoPiano(sons) }
    DisposableEffect(piano) { onDispose { piano.desligar() } }

    var musica by remember { mutableStateOf(musicaInicial?.let { Piano.porId[it] }) }
    var posicao by remember { mutableIntStateOf(posicaoInicial) }
    var apertada by remember { mutableStateOf<Nota?>(null) }
    var confete by remember { mutableIntStateOf(0) }
    /** Ouvindo a música inteira antes de tocar (a tecla de cada nota acende sozinha). */
    var demonstrando by remember { mutableStateOf(false) }
    var acesaNaDemonstracao by remember { mutableStateOf<Nota?>(null) }

    LaunchedEffect(Unit) {
        if (musicaInicial == null) narrador.falar(listOf(pt("Piano! Toque nas teclas coloridas. Piano em inglês:"), en("piano")))
    }

    LaunchedEffect(demonstrando, musica) {
        val m = musica
        if (!demonstrando || m == null) return@LaunchedEffect
        delay(400)
        for (n in m.notas) {
            acesaNaDemonstracao = n.nota
            piano.tocar(n.nota)
            delay(330L * n.tempos)
            acesaNaDemonstracao = null
            delay(60)
        }
        demonstrando = false
        narrador.falar("Agora é a sua vez! Toque na tecla que brilha.")
    }

    fun tocar(n: Nota) {
        piano.tocar(n)
        apertada = n
        val m = musica ?: return
        if (demonstrando || Piano.terminou(m, posicao)) return
        val antes = posicao
        posicao = Piano.seguir(m, posicao, n)
        if (posicao != antes && Piano.terminou(m, posicao)) {
            confete++
            narrador.falar("Que lindo! Você tocou ${m.titulo}!")
        }
    }

    fun escolher(m: Musica?) {
        sons.toque()
        musica = m
        posicao = 0
        demonstrando = false
        if (m == null) {
            narrador.falar("Tocar livre! Invente a sua música.")
        } else {
            narrador.falar("${m.titulo}! Toque na tecla que brilha. Ou toque em ouvir, para escutar primeiro.")
        }
    }

    val proxima = musica?.let { m -> m.notas.getOrNull(posicao)?.nota }?.takeIf { !demonstrando }

    EscalaDeTablet {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFFE3E0FF), Cores.Fundo)))
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BotaoRedondo("⬅️", "Voltar para a casa", onVoltar, tamanho = 52.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("🎹 Piano", style = MaterialTheme.typography.headlineMedium, maxLines = 1, modifier = Modifier.weight(1f))
                    val m = musica
                    if (m != null) {
                        BotaoRedondo(
                            if (demonstrando) "⏹️" else "👂",
                            if (demonstrando) "Parar de ouvir" else "Ouvir a música",
                            {
                                sons.toque()
                                posicao = 0
                                demonstrando = !demonstrando
                            },
                            tamanho = 52.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                        BotaoRedondo("🔁", "Começar de novo", { sons.toque(); posicao = 0; demonstrando = false }, tamanho = 52.dp)
                    }
                }

                // Escolher: livre ou uma das músicas
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ChipDeMusica("🎵", "Livre", musica == null) { escolher(null) }
                    for (m in Piano.musicas) ChipDeMusica(m.emoji, m.titulo, musica?.id == m.id) { escolher(m) }
                }

                // Partitura: as notas da música como bolinhas coloridas (as tocadas ficam marcadas)
                val m = musica
                Box(Modifier.fillMaxWidth().height(86.dp).padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    if (m == null) {
                        Text(
                            "Toque nas teclas e invente uma música!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Cores.TextoSuave,
                            textAlign = TextAlign.Center,
                        )
                    } else if (Piano.terminou(m, posicao)) {
                        Text(
                            "🎉 Você tocou ${m.titulo}! 🎉",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.pulsando(),
                        )
                    } else {
                        Partitura(m, posicao)
                    }
                }

                // Teclado
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 16.dp),
                ) {
                    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(ESPACO_ENTRE_TECLAS)) {
                        for (n in Nota.entries) {
                            Tecla(
                                nota = n,
                                brilhando = n == proxima || n == acesaNaDemonstracao,
                                apertada = apertada == n,
                                onTocar = { tocar(n) },
                                onSoltar = { if (apertada == n) apertada = null },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    }
                    // Teclinhas pretas de enfeite, como num piano de verdade (só desenho: o toque vai para a tecla de baixo).
                    TeclasPretas(Nota.entries.size, Modifier.matchParentSize())
                }
            }
            Confete(confete)
        }
    }
}

@Composable
private fun ChipDeMusica(emoji: String, titulo: String, escolhida: Boolean, onClick: () -> Unit) {
    BotaoFofo(
        onClick = onClick,
        cor = if (escolhida) Cores.Estrela else Color.White,
        corBase = Color(0x33000000),
        forma = RoundedCornerShape(50),
        descricao = titulo + if (escolhida) ", escolhida" else "",
        profundidade = 4.dp,
    ) {
        Row(Modifier.height(52.dp).padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.width(6.dp))
            Text(titulo, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
    }
}

/** A música em bolinhas: a da vez maior, as já tocadas com ✔ (mostra uma janelinha em volta da posição). */
@Composable
private fun Partitura(m: Musica, posicao: Int) {
    val inicio = (posicao - 2).coerceIn(0, (m.notas.size - 8).coerceAtLeast(0))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in inicio until (inicio + 8).coerceAtMost(m.notas.size)) {
            val n = m.notas[i]
            val vez = i == posicao
            val largura = if (n.tempos > 1) 50.dp else 34.dp
            Box(
                Modifier
                    .size(width = if (vez) largura + 8.dp else largura, height = if (vez) 42.dp else 34.dp)
                    .background(Color(n.nota.corArgb).copy(alpha = if (i < posicao) 0.35f else 1f), RoundedCornerShape(50))
                    .border(if (vez) 3.dp else 0.dp, if (vez) Color.White else Color.Transparent, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (i < posicao) "✔" else n.nota.nome,
                    color = if (i < posicao) Color.White else Cores.Texto,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (vez) 16.sp else 13.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Uma tecla: toca no momento em que o dedo encosta (não espera soltar), e afunda enquanto está apertada. */
@Composable
private fun Tecla(nota: Nota, brilhando: Boolean, apertada: Boolean, onTocar: () -> Unit, onSoltar: () -> Unit, modifier: Modifier) {
    val tocar by rememberUpdatedState(onTocar)
    val soltar by rememberUpdatedState(onSoltar)
    val cor = Color(nota.corArgb)
    val afundar by animateFloatAsState(if (apertada) 1f else 0f, tween(80), label = "tecla")
    val agudo = nota == Nota.DO_AGUDO
    // Notinhas musicais que sobem da tecla a cada toque.
    val subida = remember { Animatable(1f) }
    LaunchedEffect(apertada) {
        if (apertada) {
            subida.snapTo(0f)
            subida.animateTo(1f, tween(950, easing = LinearOutSlowInEasing))
        }
    }
    Box(
        modifier
            .drawWithContent {
                // Brilho em volta da tecla apertada (na cor dela).
                if (afundar > 0f) {
                    for ((folgaDp, alfa) in listOf(9f to 0.18f, 5f to 0.35f)) {
                        val folga = folgaDp.dp.toPx() * afundar
                        drawRoundRect(
                            cor.copy(alpha = alfa * afundar),
                            Offset(-folga, -folga + 6.dp.toPx() * afundar),
                            Size(size.width + folga * 2, size.height + folga * 2 - 6.dp.toPx() * afundar),
                            CornerRadius(14.dp.toPx() + folga),
                        )
                    }
                }
                drawContent()
                val t = subida.value
                if (t < 1f) {
                    val alfa = (1f - t).coerceIn(0f, 1f)
                    val r = size.width * 0.16f
                    desenharNotinha(Offset(size.width * 0.34f, -t * size.height * 0.28f + size.height * 0.03f), r, escurecer(cor, 0.75f).copy(alpha = alfa), colcheia = true)
                    if (t > 0.15f) {
                        desenharNotinha(Offset(size.width * 0.68f, -(t - 0.15f) * size.height * 0.24f + size.height * 0.05f), r * 0.8f, escurecer(cor, 0.75f).copy(alpha = alfa), colcheia = false)
                    }
                }
            }
            .testTag("tecla_${nota.name.lowercase()}")
            .semantics {
                contentDescription = "Tecla ${nota.nome}" + (if (agudo) " agudo" else "") + if (brilhando) ", a da vez" else ""
                role = Role.Button
                onClick { tocar(); true }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown().consume()
                    tocar()
                    waitForUpOrCancellation()
                    soltar()
                }
            }
            .padding(top = 6.dp * afundar),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 6.dp)
                .background(escurecer(cor, 0.7f), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 6.dp, topEnd = 6.dp)),
        )
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = 6.dp * (1f - afundar))
                .then(if (brilhando) Modifier.pulsando() else Modifier)
                .background(
                    Brush.verticalGradient(listOf(Color.White, if (apertada) escurecer(cor, 0.9f) else cor)),
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 6.dp, topEnd = 6.dp),
                )
                .drawWithContent {
                    drawContent()
                    // Tampinha da tecla: um brilho de verniz no alto e uma faixa colorida embaixo.
                    val m = size.width * 0.16f
                    drawRoundRect(
                        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.85f), Color.White.copy(alpha = 0f)), startY = 8.dp.toPx(), endY = size.height * 0.3f),
                        Offset(m, 8.dp.toPx()), Size(size.width - m * 2, size.height * 0.3f - 8.dp.toPx()), CornerRadius(size.width * 0.3f),
                    )
                    drawRoundRect(
                        escurecer(cor, 0.88f).copy(alpha = 0.55f),
                        Offset(4.dp.toPx(), size.height - 10.dp.toPx()), Size(size.width - 8.dp.toPx(), 5.dp.toPx()), CornerRadius(3.dp.toPx()),
                    )
                }
                .border(
                    if (brilhando) 4.dp else 1.5.dp,
                    if (brilhando) Cores.Estrela else escurecer(cor, 0.7f).copy(alpha = 0.45f),
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 6.dp, topEnd = 6.dp),
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 12.dp)) {
                if (brilhando) Text("⭐", fontSize = 22.sp, modifier = Modifier.scale(1.1f))
                Text(nota.nome, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Cores.Texto, maxLines = 1)
            }
        }
    }
}

private val ESPACO_ENTRE_TECLAS: Dp = 4.dp

/** Onde ficam as teclinhas pretas: entre dó e ré, ré e mi, fá e sol, sol e lá, lá e si (como no piano). */
private val PRETAS_DEPOIS_DE = listOf(0, 1, 3, 4, 5)

@Composable
private fun TeclasPretas(teclas: Int, modifier: Modifier) {
    Canvas(modifier) {
        val espaco = ESPACO_ENTRE_TECLAS.toPx()
        val larg = (size.width - espaco * (teclas - 1)) / teclas
        val pw = larg * 0.42f
        val ph = size.height * 0.2f
        for (i in PRETAS_DEPOIS_DE) {
            if (i + 1 >= teclas) continue
            val cx = (i + 1) * larg + i * espaco + espaco / 2f
            val topo = 6.dp.toPx()
            drawRoundRect(Color(0x33000000), Offset(cx - pw / 2f + 2.dp.toPx(), topo + 3.dp.toPx()), Size(pw, ph), CornerRadius(pw * 0.25f))
            drawRoundRect(
                Brush.verticalGradient(listOf(Color(0xFF4A4060), Color(0xFF231C33)), startY = topo, endY = topo + ph),
                Offset(cx - pw / 2f, topo), Size(pw, ph), CornerRadius(pw * 0.25f),
            )
            drawRoundRect(Color(0x40FFFFFF), Offset(cx - pw * 0.3f, topo + ph * 0.08f), Size(pw * 0.16f, ph * 0.7f), CornerRadius(pw * 0.08f))
        }
    }
}

/** Uma notinha musical desenhada (bolinha, haste e, na colcheia, a bandeirinha). */
private fun DrawScope.desenharNotinha(c: Offset, r: Float, cor: Color, colcheia: Boolean) {
    rotate(-20f, c) { drawOval(cor, Offset(c.x - r, c.y - r * 0.72f), Size(r * 2f, r * 1.44f)) }
    val topo = Offset(c.x + r * 0.85f, c.y - r * 3.2f)
    drawLine(cor, Offset(c.x + r * 0.85f, c.y - r * 0.2f), topo, r * 0.32f, StrokeCap.Round)
    if (colcheia) {
        val bandeira = Path().apply {
            moveTo(topo.x, topo.y)
            cubicTo(topo.x + r * 1.2f, topo.y + r * 0.5f, topo.x + r * 1.4f, topo.y + r * 1.2f, topo.x + r * 0.8f, topo.y + r * 2f)
            cubicTo(topo.x + r * 0.9f, topo.y + r * 1.3f, topo.x + r * 0.5f, topo.y + r * 0.9f, topo.x, topo.y + r * 0.9f)
            close()
        }
        drawPath(bandeira, cor)
    }
}
