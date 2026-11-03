package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.Historia
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Corujinha
import br.com.casinhadosaber.ui.componentes.caminhoEstrela
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

/**
 * Cena curta da história (sugestão 3): a Corujinha Professora conta, num balão, o que acontece no mundo,
 * com o amigo do caminho ao lado. O narrador lê o texto; o botão grande segue em frente. [chefao] troca o título e o botão para a festa da vitória do chefão.
 * Marca de teste do botão: "historia_continuar".
 */
@Composable
fun CenaDaHistoria(fase: Fase, texto: String, chefao: Boolean = false, onContinuar: () -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val mundo = Mundos.de(fase)
    val capitulo = Historia.capitulo(fase)
    val cor = Color(mundo.corArgb)
    val corDaTrilha = Color(fase.trilha.corArgb)
    LaunchedEffect(texto) { narrador.falar(texto) }
    fun seguir() {
        narrador.parar()
        sons.toque()
        onContinuar()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(corDaTrilha, cor, Cores.Fundo)))
            // Segura os toques: nada atrás da cena responde enquanto ela está aberta.
            .clickable(remember { MutableInteractionSource() }, indication = null) { }
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // Estrelinhas e morrinho ao fundo, no estilo das outras telas.
        Canvas(Modifier.fillMaxSize()) {
            for (k in 0 until 10) {
                val x = ((k * 0.41f + 0.13f) % 1f) * size.width
                val y = ((k * 0.29f + 0.05f) % 0.55f) * size.height
                drawPath(caminhoEstrela(Offset(x, y), (5 + (k % 3) * 3).dp.toPx()), Color.White.copy(alpha = 0.4f))
            }
            val morro = Path().apply {
                moveTo(0f, size.height * 0.82f)
                cubicTo(size.width * 0.3f, size.height * 0.74f, size.width * 0.7f, size.height * 0.76f, size.width, size.height * 0.8f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(morro, Cores.Grama.copy(alpha = 0.55f))
        }
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // O texto e os desenhos rolam em telas baixas; o botão fica sempre à vista, embaixo.
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    if (chefao) "${mundo.emoji} Você venceu o chefão!" else "${mundo.emoji} ${mundo.titulo}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                if (chefao) {
                    Text(capitulo.guardiao, style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
                }
                // Balão de fala da Corujinha, com a pontinha apontando para ela.
                Box(
                    Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(28.dp))
                        .background(Color.White, RoundedCornerShape(28.dp))
                        .border(4.dp, Color(0xFFEDE6FF), RoundedCornerShape(28.dp))
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Corujinha Professora",
                                style = MaterialTheme.typography.titleMedium,
                                color = Cores.Roxo,
                                modifier = Modifier.weight(1f),
                            )
                            BotaoRedondo("🔊", "Ouvir de novo", { narrador.falar(texto) }, tamanho = 48.dp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(texto, style = MaterialTheme.typography.titleLarge, color = Cores.Texto)
                    }
                }
                Canvas(Modifier.size(width = 40.dp, height = 22.dp)) {
                    val ponta = Path().apply {
                        moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width * 0.3f, size.height); close()
                    }
                    drawPath(ponta, Color.White)
                }
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 180.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Corujinha(Modifier.height(210.dp), falando = true)
                    Spacer(Modifier.size(12.dp))
                    // O amigo do caminho, junto da Corujinha.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(capitulo.amigo.emoji, fontSize = 72.sp)
                        Text(
                            capitulo.amigo.nome.substringBefore(","),
                            style = MaterialTheme.typography.titleMedium,
                            color = Cores.Texto,
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            BotaoFofo(
                onClick = ::seguir,
                cor = Cores.Certo,
                descricao = if (chefao) "Oba!" else "Vamos lá!",
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .testTag("historia_continuar")
                    .pulsando(),
            ) {
                Text(
                    if (chefao) "🎉 Oba!" else "▶ Vamos lá!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                )
            }
        }
    }
}
