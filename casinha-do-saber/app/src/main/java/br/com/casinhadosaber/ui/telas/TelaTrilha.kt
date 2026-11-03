package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
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
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.launch

private val ALTURA_PASSO = 132.dp
private val TAMANHO_BOLINHA = 88.dp

/** Posição horizontal (fração) de cada fase, formando um caminho em zigue-zague. */
private val zigueZague = listOf(0.5f, 0.75f, 0.5f, 0.25f, 0.5f, 0.75f, 0.5f, 0.25f, 0.5f, 0.75f)

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
    val proximoPremio = ItensDaCasa.ordem.getOrNull(estado.fasesVencidas)
    val cor = Color(trilha.corArgb)
    val rolagem = rememberScrollState()
    val densidade = LocalDensity.current

    LaunchedEffect(trilha) {
        narrador.falar(trilha.chamada + " Toque na bolinha que está brilhando.")
        // Rola até a fase atual.
        val indice = atual?.numero ?: 0
        rolagem.animateScrollTo(with(densidade) { (ALTURA_PASSO * (indice - 1).coerceAtLeast(0)).roundToPx() })
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
            Text(trilha.titulo, style = MaterialTheme.typography.headlineLarge, color = Color.White, modifier = Modifier.weight(1f))
            Text(
                "${estado.vencidasNa(trilha)}/${fases.size}",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )
        }

        if (proximoPremio != null) {
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Próximo prêmio para a casa:", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Text(proximoPremio.icone, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.pulsando())
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
                for (i in 0 until fases.size - 1) {
                    val a = Offset(size.width * zigueZague[i], passo * i + passo / 2)
                    val b = Offset(size.width * zigueZague[i + 1], passo * (i + 1) + passo / 2)
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
                val cx = largura * zigueZague[fase.numero]
                val topo = ALTURA_PASSO * fase.numero + (ALTURA_PASSO - TAMANHO_BOLINHA) / 2
                Box(
                    Modifier
                        .offset(x = cx - TAMANHO_BOLINHA / 2, y = topo)
                        .width(TAMANHO_BOLINHA),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    BolinhaDaFase(
                        fase = fase,
                        cor = cor,
                        corEscura = Color(trilha.corEscuraArgb),
                        liberada = liberada,
                        estrelas = estrelas,
                        atual = eAtual,
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
                    // A criança fica ao lado da fase atual, do lado com mais espaço.
                    val proximo = zigueZague.getOrNull(fase.numero + 1) ?: zigueZague[fase.numero - 1]
                    val ladoCrianca = if (proximo > zigueZague[fase.numero]) -1 else 1
                    estado.perfil?.let { perfil ->
                        Crianca(
                            perfil,
                            Modifier
                                .offset(x = cx + (TAMANHO_BOLINHA / 2 + 40.dp) * ladoCrianca - 32.dp, y = topo - 12.dp)
                                .height(104.dp),
                            acenando = true,
                        )
                    }
                }
            }
            Spacer(Modifier.height(alturaTotal))
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
    onTocar: () -> Unit,
) {
    val tremida = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        drawCircle(cor, style = Stroke(5.dp.toPx()))
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
                    Text(fase.emoji, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.graphicsLayer { alpha = 0.3f })
                    Text("🔒", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.BottomEnd))
                }
            }
        }
        // Nome da fase numa plaquinha (não se mistura com o caminho pontilhado), e as estrelas embaixo.
        Text(
            fase.titulo + if (estrelas > 0) "  " + "★".repeat(estrelas) + "☆".repeat(3 - estrelas) else "",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = if (liberada) Cores.Texto else Cores.TextoSuave,
            softWrap = false,
            modifier = Modifier
                .padding(top = 2.dp)
                .wrapContentWidth(unbounded = true)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(50))
                .padding(horizontal = 10.dp, vertical = 2.dp),
        )

    }
}
