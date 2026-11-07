package br.com.casinhadosaber.ui.telas.atividades

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Trecho
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.FiguraDaPergunta
import br.com.casinhadosaber.ui.componentes.ID_ESTRELA_CHEIA
import br.com.casinhadosaber.ui.componentes.desenharArte
import br.com.casinhadosaber.ui.componentes.lembrarArteLeve
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

private class Carta(val id: Int, val par: Int, val conteudo: String)

/**
 * Jogo da memória. Virar duas cartas diferentes faz parte da brincadeira (é explorando que se
 * lembra), então só conta como "errou" quem passar de [tolerancia] tentativas sem par: o dobro do
 * número de pares. A partir daí, ao virar uma carta, o par dela brilha.
 *
 * Antes de começar, as cartas ficam abertas por um instante para a criança olhar.
 */
@Composable
internal fun AtividadeMemoria(
    memoria: Atividade.Memoria,
    ativa: Boolean,
    ajudaInicial: Boolean,
    cor: Color,
    onErro: () -> Unit,
    onConcluir: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val vibracao = LocalHapticFeedback.current
    val escopo = rememberCoroutineScope()
    val concluir by rememberUpdatedState(onConcluir)
    val avisarErro by rememberUpdatedState(onErro)

    val cartas = remember(memoria) {
        memoria.pares.flatMapIndexed { i, (a, b) -> listOf(i to a, i to b) }
            .shuffled()
            .mapIndexed { id, (par, conteudo) -> Carta(id, par, conteudo) }
    }
    val tolerancia = memoria.pares.size * 2
    var espiando by remember { mutableStateOf(true) }
    var abertas by remember { mutableStateOf(listOf<Int>()) }
    var achados by remember { mutableStateOf(setOf<Int>()) }
    var semPar by remember { mutableIntStateOf(0) }
    var terminou by remember { mutableStateOf(false) }
    var fechar by remember { mutableStateOf<Job?>(null) }
    val ajuda = ajudaInicial || semPar > tolerancia

    // Espiadinha: todas abertas no começo, depois viram.
    LaunchedEffect(memoria) {
        delay(if (cartas.size <= 6) 1800L else 2600L)
        espiando = false
        sons.toque()
    }

    fun falaDaCarta(c: Carta): List<Trecho> {
        memoria.falas[c.conteudo]?.let { return listOf(it) }
        val figuras = figurasDe(c.conteudo)
        return when {
            ehTexto(c.conteudo) && c.conteudo.all { it.isDigit() } ->
                listOf(pt(Vocabulario.porExtenso(c.conteudo.toInt())))
            ehTexto(c.conteudo) -> listOf(pt(Vocabulario.falaDaLetra(c.conteudo)))
            figuras.size > 1 -> {
                val obj = Vocabulario.objetosParaContar.firstOrNull { it.emoji.replace("\uFE0F", "") == figuras[0].replace("\uFE0F", "") }
                if (obj != null) listOf(pt("${Vocabulario.porExtenso(figuras.size, obj.feminino)} ${obj.nome(figuras.size)}"))
                else listOf(pt(Vocabulario.porExtenso(figuras.size)))
            }
            else -> nomeDaFigura(c.conteudo)?.let { listOf(pt(it)) } ?: emptyList()
        }
    }

    fun virar(c: Carta) {
        if (!ativa || espiando || terminou || c.par in achados || c.id in abertas) return
        // Duas cartas diferentes ainda abertas: a criança já quer a próxima, então fecham na hora.
        if (abertas.size >= 2) {
            fechar?.cancel()
            abertas = emptyList()
        }
        sons.toque()
        val agora = abertas + c.id
        abertas = agora
        if (agora.size < 2) {
            narrador.falar(falaDaCarta(c))
            return
        }
        val outra = cartas[agora[0]]
        if (outra.par == c.par) {
            achados = achados + c.par
            abertas = emptyList()
            sons.colocar()
            vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (achados.size == memoria.pares.size) {
                terminou = true
                narrador.falar(falaDaCarta(c))
                escopo.launch {
                    delay(700)
                    concluir(semPar <= tolerancia)
                }
            } else {
                // Par diferente (número e quantidade, letra e figura): diz os dois.
                val falas = if (outra.conteudo == c.conteudo) falaDaCarta(c) else falaDaCarta(outra) + falaDaCarta(c)
                narrador.falar(falas + pt("Achou um par!"))
            }
        } else {
            semPar++
            if (semPar == tolerancia + 1) {
                avisarErro()
                narrador.falar(falaDaCarta(c) + pt("Não é igual. Olhe a carta que está brilhando!"))
            } else {
                narrador.falar(falaDaCarta(c))
            }
            fechar = escopo.launch {
                delay(1300)
                abertas = emptyList()
            }
        }
    }

    BoxWithConstraints(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val n = cartas.size
        val vao = 10.dp
        // Escolhe o número de colunas que deixa as cartas maiores (cartas em pé, 4 por 5).
        var colunas = 2
        var larguraCarta = 0.dp
        for (c in 2..5) {
            val linhas = (n + c - 1) / c
            val porLargura = (maxWidth - vao * (c - 1)) / c
            val porAltura = ((maxHeight - vao * (linhas - 1)) / linhas) / 1.25f
            val lado = minOf(porLargura, porAltura)
            if (lado > larguraCarta) {
                larguraCarta = lado
                colunas = c
            }
        }
        larguraCarta = minOf(larguraCarta, 170.dp).coerceAtLeast(44.dp)
        val alturaCarta = larguraCarta * 1.25f
        val aberta = abertas.firstOrNull()?.let { cartas[it] }
        val primeiraDica = if (ajuda && abertas.size == 1 && aberta != null) aberta.par else -1

        Column(verticalArrangement = Arrangement.spacedBy(vao), horizontalAlignment = Alignment.CenterHorizontally) {
            for (linha in cartas.chunked(colunas)) {
                Row(horizontalArrangement = Arrangement.spacedBy(vao)) {
                    for (c in linha) {
                        Box {
                            CartaDaMemoria(
                                carta = c,
                                virada = espiando || c.id in abertas || c.par in achados,
                                achada = c.par in achados,
                                brilhar = c.par == primeiraDica && c.id !in abertas,
                                cor = cor,
                                largura = larguraCarta,
                                altura = alturaCarta,
                                onToque = { virar(c) },
                            )
                            if (c.id == cartas.first().id) {
                                MaoGuia(
                                    Modifier.align(Alignment.Center),
                                    ativa = ativa && !espiando && abertas.isEmpty() && achados.isEmpty() && semPar == 0,
                                    espera = if (ajudaInicial) 900L else 3500L,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartaDaMemoria(
    carta: Carta,
    virada: Boolean,
    achada: Boolean,
    brilhar: Boolean,
    cor: Color,
    largura: Dp,
    altura: Dp,
    onToque: () -> Unit,
) {
    val giro by animateFloatAsState(if (virada) 1f else 0f, tween(320), label = "giro")
    val mostrandoFrente = giro > 0.5f
    val forma = RoundedCornerShape(18.dp)
    Box(
        Modifier
            .size(largura, altura)
            .testTag("carta_${carta.par}")
            .semantics { contentDescription = if (mostrandoFrente) carta.conteudo else "Carta" }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onToque,
            )
            .then(if (brilhar) Modifier.pulsando() else Modifier)
            .graphicsLayer {
                // Vira "achatando" a carta: some de lado e volta mostrando a outra face.
                scaleX = abs(cos(giro * PI.toFloat())).coerceAtLeast(0.02f)
            }
            // Par achado: achata, estica e solta estrelinhas.
            .festejo(achada)
            // Carta como tecla de brinquedo (Fase 9): sombra desenhada embaixo (sem a de elevação, borrada), face em
            // degradê com brilho no alto, borda branca por dentro e o lábio mais escuro embaixo.
            .drawBehind { drawRoundRect(Cores.Sombra, topLeft = Offset(0f, LABIO_DA_CARTA.toPx()), size = size, cornerRadius = CornerRadius(18.dp.toPx())) }
            .ladrilho(
                cor = when {
                    !mostrandoFrente -> escurecer(cor, 0.72f)
                    achada -> Cores.Certo
                    else -> Color(0xFFD9CFF0)
                },
                raio = 18.dp,
                fundo = when {
                    !mostrandoFrente -> cor
                    achada -> Color(0xFFDFF7E7)
                    else -> Color.White
                },
                labio = LABIO_DA_CARTA,
            )
            .then(if (brilhar) Modifier.border(5.dp, Cores.Estrela, forma) else Modifier)
            .padding(bottom = LABIO_DA_CARTA),
        contentAlignment = Alignment.Center,
    ) {
        if (mostrandoFrente) {
            FrenteDaCarta(carta.conteudo, largura)
        } else {
            VersoDaCarta(largura)
        }
    }
}

@Composable
private fun VersoDaCarta(largura: Dp) {
    // Verso: bolinhas claras e uma estrela no meio.
    Canvas(Modifier.size(largura * 0.8f)) {
        val r = size.minDimension
        for (i in 0 until 3) for (j in 0 until 3) {
            if (i == 1 && j == 1) continue
            drawCircle(Color.White.copy(alpha = 0.25f), r * 0.06f, Offset(r * (0.2f + 0.3f * i), r * (0.2f + 0.3f * j)))
        }
    }
    // A estrela 3D do progresso (a mesma das plaquinhas), um bitmap pequeno do cache; sem a arte, a ⭐.
    val lado = largura * 0.4f
    val estrela = lembrarArteLeve(ID_ESTRELA_CHEIA, lado)
    if (estrela == null) {
        Text("⭐", fontSize = (largura.value * 0.32f).sp)
    } else {
        Canvas(Modifier.size(lado)) { desenharArte(estrela, center, size.minDimension) }
    }
}

/** Altura do lábio das cartas da memória (fica dentro do tamanho da carta). */
private val LABIO_DA_CARTA = 6.dp

@Composable
private fun FrenteDaCarta(conteudo: String, largura: Dp) {
    if (ehTexto(conteudo)) {
        Text(
            conteudo,
            fontSize = (largura.value * (if (conteudo.length <= 1) 0.5f else 0.36f)).sp,
            fontWeight = FontWeight.Bold,
            color = Cores.Texto,
            style = MaterialTheme.typography.displayLarge,
            maxLines = 1,
        )
        return
    }
    val figuras = figurasDe(conteudo)
    if (figuras.size <= 1) {
        FiguraDaPergunta(conteudo, largura * 0.66f) {
            Text(conteudo, fontSize = (largura.value * 0.5f).sp, lineHeight = (largura.value * 0.6f).sp)
        }
        return
    }
    // Várias figuras (quantidade): grade como num dado.
    val porLinha = if (figuras.size <= 4) 2 else 3
    val celula = largura * 0.84f / porLinha
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (linha in figuras.chunked(porLinha)) {
            Row {
                for (f in linha) {
                    Box(Modifier.size(celula), contentAlignment = Alignment.Center) {
                        FiguraDaPergunta(f, celula * 0.92f) {
                            Text(f, fontSize = (celula.value * 0.72f).sp, lineHeight = (celula.value * 0.85f).sp)
                        }
                    }
                }
            }
        }
    }
}
