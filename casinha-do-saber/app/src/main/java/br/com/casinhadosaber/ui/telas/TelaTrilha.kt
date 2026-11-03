package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.PROPORCAO_CRIANCA
import br.com.casinhadosaber.ui.componentes.FileiraDeEstrelas
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.launch

/** Altura de cada degrau do caminho: bolinha (88 + 6 de base) e plaquinha de até 2 linhas com estrelas. */
private val ALTURA_PASSO = 168.dp
private val TAMANHO_BOLINHA = 88.dp
/** Distância do topo do degrau até o topo da bolinha. */
private val TOPO_BOLINHA = 14.dp
/** Largura reservada para a plaquinha com o nome (cabe inteira mesmo na bolinha mais à esquerda em 360dp). */
private val LARGURA_PLACA = 160.dp
private val ALTURA_CRIANCA = 104.dp

/** A cada [FASES_POR_MARCO] fases, um marco (bandeirinha que vira troféu) ajuda a criança a se orientar. */
private const val FASES_POR_MARCO = 5

/** Posição horizontal (fração da largura) de cada fase, formando um caminho em zigue-zague. */
private val zigueZague = listOf(0.5f, 0.74f, 0.5f, 0.26f)

private fun posicaoX(indice: Int): Float = zigueZague[indice.mod(zigueZague.size)]

private fun ehMarco(fase: Fase): Boolean = (fase.numero + 1) % FASES_POR_MARCO == 0

@Composable
fun TelaTrilha(
    trilha: Trilha,
    estado: EstadoJogo,
    onVoltar: () -> Unit,
    onJogar: (Fase) -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val fases = remember(trilha) { GeradorDeFases().fases(trilha) }
    val atual = fases.firstOrNull { estado.liberada(it) && estado.estrelasDa(it) == 0 }
    // Fase para mostrar ao abrir: a atual ou, com tudo vencido, a última vencida.
    val indiceEmFoco = atual?.numero ?: fases.indexOfLast { estado.estrelasDa(it) > 0 }.coerceAtLeast(0)
    val proximoPremio = ItensDaCasa.ordem.getOrNull(estado.fasesVencidas)
    val cor = Color(trilha.corArgb)
    val corEscura = Color(trilha.corEscuraArgb)
    val rolagem = rememberScrollState()
    val densidade = LocalDensity.current

    LaunchedEffect(trilha) {
        narrador.falar(trilha.chamada + " Toque na bolinha que está brilhando.")
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cor, Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BotaoRedondo("⬅️", "Voltar", onVoltar, tamanho = 52.dp)
            Spacer(Modifier.width(12.dp))
            Text(trilha.emoji, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(8.dp))
            Text(
                trilha.titulo,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            if (proximoPremio != null) {
                // Próximo prêmio da casa, como um chip na barra (nada passa por baixo dele).
                Row(
                    Modifier
                        .background(Color.White, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .semantics(mergeDescendants = true) { contentDescription = "Próximo prêmio: ${proximoPremio.nome}" },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🎁", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(4.dp))
                    Text(proximoPremio.icone, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.pulsando())
                }
            } else {
                Text("${estado.vencidasNa(trilha)}/${fases.size}", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            val alturaVisivel = maxHeight
            // Ao abrir, a fase atual aparece no meio da tela (com 25 fases ela pode estar bem lá embaixo).
            LaunchedEffect(trilha) {
                val centroDaFase = 12.dp + ALTURA_PASSO * indiceEmFoco + TOPO_BOLINHA + TAMANHO_BOLINHA / 2
                val alvo = with(densidade) { (centroDaFase - alturaVisivel / 2).roundToPx() }.coerceAtLeast(0)
                if (alvo > 0) {
                    // Pula para perto e desliza o finalzinho: a criança vê o caminho andando até ela.
                    rolagem.scrollTo((alvo - with(densidade) { 160.dp.roundToPx() }).coerceAtLeast(0))
                    rolagem.animateScrollTo(alvo)
                }
            }
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rolagem)
                    .padding(top = 12.dp),
            ) {
                val largura = maxWidth
                val alturaTotal = ALTURA_PASSO * fases.size + 40.dp
                val corCaminho = Color.White.copy(alpha = 0.85f)
                Canvas(Modifier.fillMaxWidth().height(alturaTotal)) {
                    val passo = ALTURA_PASSO.toPx()
                    val centroY = TOPO_BOLINHA.toPx() + TAMANHO_BOLINHA.toPx() / 2
                    // Faixas suaves a cada 5 fases: o caminho fica dividido em "pedacinhos" fáceis de ver.
                    val margem = 8.dp.toPx()
                    for (g in 0 until (fases.size + FASES_POR_MARCO - 1) / FASES_POR_MARCO) {
                        if (g % 2 == 1) {
                            val y0 = passo * g * FASES_POR_MARCO + margem
                            val y1 = passo * minOf((g + 1) * FASES_POR_MARCO, fases.size) - margem
                            drawRoundRect(
                                Color.White.copy(alpha = 0.22f),
                                Offset(margem, y0),
                                Size(size.width - 2 * margem, y1 - y0),
                                CornerRadius(28.dp.toPx()),
                            )
                        }
                    }
                    for (i in 0 until fases.size - 1) {
                        val a = Offset(size.width * posicaoX(i), passo * i + centroY)
                        val b = Offset(size.width * posicaoX(i + 1), passo * (i + 1) + centroY)
                        drawLine(
                            corCaminho, a, b, strokeWidth = 14.dp.toPx(), cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 34f)),
                        )
                    }
                }
                for (fase in fases) {
                    val liberada = estado.liberada(fase)
                    val estrelas = estado.estrelasDa(fase)
                    val eAtual = fase == atual
                    val fracao = posicaoX(fase.numero)
                    val cx = largura * fracao
                    val topo = ALTURA_PASSO * fase.numero + TOPO_BOLINHA
                    Box(
                        Modifier
                            .offset(x = cx - LARGURA_PLACA / 2, y = topo)
                            .width(LARGURA_PLACA),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        BolinhaDaFase(
                            fase = fase,
                            cor = cor,
                            corEscura = corEscura,
                            liberada = liberada,
                            estrelas = estrelas,
                            atual = eAtual,
                            marco = ehMarco(fase),
                            onTocar = {
                                if (liberada) {
                                    sons.toque()
                                    onJogar(fase)
                                } else {
                                    sons.erro()
                                    narrador.falar("Essa ainda está fechada. Termine a fase de antes!")
                                }
                            },
                        )
                    }
                    if (eAtual) {
                        // A criança fica ao lado da fase atual, sempre do lado com mais espaço (nunca sai da tela).
                        val lado = when {
                            fracao < 0.5f -> 1
                            fracao > 0.5f -> -1
                            posicaoX(fase.numero + 1) > fracao -> -1
                            else -> 1
                        }
                        val larguraCrianca = ALTURA_CRIANCA * PROPORCAO_CRIANCA
                        val x = if (lado > 0) cx + TAMANHO_BOLINHA / 2 + 10.dp else cx - TAMANHO_BOLINHA / 2 - 10.dp - larguraCrianca
                        estado.perfil?.let { perfil ->
                            Crianca(
                                perfil,
                                Modifier
                                    .offset(x = x, y = topo - 8.dp)
                                    .height(ALTURA_CRIANCA),
                                acenando = true,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(alturaTotal))
            }
            // Esmaecido no topo: o que sobe some suavemente, sem corte reto.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .background(Brush.verticalGradient(listOf(cor, cor.copy(alpha = 0f)))),
            )
        }
    }
}

@Composable
private fun BolinhaDaFase(
    fase: Fase,
    cor: Color,
    corEscura: Color,
    liberada: Boolean,
    estrelas: Int,
    atual: Boolean,
    marco: Boolean,
    onTocar: () -> Unit,
) {
    val tremida = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            BotaoFofo(
                onClick = {
                    onTocar()
                    if (!liberada) {
                        escopo.launch {
                            for (x in listOf(10f, -10f, 6f, -6f, 0f)) tremida.animateTo(x, tween(50))
                        }
                    }
                },
                cor = if (liberada) Color.White else Color(0xFFE0E0E0),
                corBase = if (liberada) corEscura else Color(0xFFBDBDBD),
                forma = CircleShape,
                descricao = "Fase ${fase.numero + 1}, ${fase.titulo}" + when {
                    !liberada -> ", fechada"
                    estrelas > 0 -> ", $estrelas estrelas"
                    else -> ""
                },
                modifier = Modifier
                    .graphicsLayer { translationX = tremida.value }
                    .then(if (atual) Modifier.pulsando() else Modifier),
            ) {
                Box(
                    Modifier
                        .size(TAMANHO_BOLINHA - 6.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (liberada) {
                        Canvas(Modifier.fillMaxSize()) {
                            // Marco: anel dourado mais grosso, para destacar a cada 5 fases.
                            if (marco) drawCircle(Cores.Estrela, style = Stroke(8.dp.toPx()))
                            drawCircle(cor, radius = size.minDimension / 2 - (if (marco) 6.dp.toPx() else 0f), style = Stroke(5.dp.toPx()))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(fase.emoji, style = MaterialTheme.typography.headlineLarge)
                            Text("${fase.numero + 1}", style = MaterialTheme.typography.titleMedium, color = corEscura)
                        }
                        if (estrelas > 0) {
                            // Fase vencida: um "certinho" na cor do caminho.
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .size(26.dp)
                                    .background(cor, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) { Text("✔", color = Color.White, style = MaterialTheme.typography.titleMedium) }
                        }
                    } else {
                        // Fechada: mostra o desenho da fase apagadinho e um cadeado pequeno.
                        if (marco) {
                            Canvas(Modifier.fillMaxSize()) { drawCircle(Cores.Estrela.copy(alpha = 0.45f), style = Stroke(6.dp.toPx())) }
                        }
                        Text(fase.emoji, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.graphicsLayer { alpha = 0.3f })
                        Text("🔒", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.BottomEnd))
                    }
                }
            }
            if (marco) {
                // Bandeirinha do marco; vira troféu quando a fase é vencida.
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-6).dp, y = (-4).dp)
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                        .border(3.dp, Cores.Estrela, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (estrelas > 0) "🏆" else "🚩", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        // Nome da fase numa plaquinha (não se mistura com o caminho pontilhado), e as estrelas embaixo.
        Column(
            Modifier
                .padding(top = 2.dp)
                .widthIn(max = LARGURA_PLACA)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                fase.titulo,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = if (liberada) Cores.Texto else Cores.TextoSuave,
            )
            if (estrelas > 0) FileiraDeEstrelas(estrelas, tamanho = 14.dp)
        }
    }
}
