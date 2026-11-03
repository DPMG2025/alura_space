package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.CriancaDaCasa
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.tema.Cores

/*
 * "Quem vai jogar?": aparece ao abrir o app quando o aparelho tem mais de uma criança. Cada criança é um
 * botão grande com o próprio personagem desenhado e o nome (o nome vem só do perfil; em branco vira "Criança 2").
 * Marcas de teste: "quem_vai_jogar", "crianca_<id>".
 */

private val CoresDosCartoes = listOf(
    Color(0xFFFFE3EC), Color(0xFFDFF3FF), Color(0xFFE6F8DC), Color(0xFFFFF1CC),
    Color(0xFFEDE6FF), Color(0xFFFFE6D5), Color(0xFFD9F5F0), Color(0xFFF6E1FF),
)

@Composable
fun TelaQuemVaiJogar(criancas: List<CriancaDaCasa>, onEscolher: (String) -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    LaunchedEffect(Unit) { narrador.falar("Quem vai jogar?") }
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .testTag("quem_vai_jogar"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Quem vai jogar?",
            style = MaterialTheme.typography.headlineLarge,
            color = Cores.Texto,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
        )
        Spacer(Modifier.height(24.dp))
        // Duas por fileira (uma só fica sozinha no meio).
        for (fileira in criancas.withIndex().toList().chunked(2)) {
            Row(
                Modifier.fillMaxWidth().widthIn(max = 560.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            ) {
                for ((i, c) in fileira) {
                    BotaoFofo(
                        onClick = {
                            sons.toque()
                            narrador.parar()
                            onEscolher(c.id)
                        },
                        modifier = Modifier.weight(1f, fill = false).widthIn(max = 200.dp).testTag("crianca_${c.id}"),
                        cor = CoresDosCartoes[i % CoresDosCartoes.size],
                        corBase = Color(0xFFD9CFF0),
                        descricao = c.nomeParaMostrar(),
                    ) {
                        Column(
                            Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                Modifier
                                    .size(width = 130.dp, height = 170.dp)
                                    // Um brilho redondo atrás do personagem e o chãozinho onde ele pisa.
                                    .drawBehind {
                                        drawCircle(
                                            Brush.radialGradient(listOf(Color.White.copy(alpha = 0.95f), Color.White.copy(alpha = 0f)), center, size.width * 0.62f),
                                            size.width * 0.62f,
                                            Offset(center.x, size.height * 0.48f),
                                        )
                                        drawOval(Color(0x1F3D2C5E), Offset(size.width * 0.12f, size.height * 0.92f), Size(size.width * 0.76f, size.height * 0.08f))
                                    },
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                val perfil = c.perfil
                                if (perfil != null) {
                                    Crianca(perfil, Modifier.height(170.dp))
                                } else {
                                    // Criança que o adulto acabou de adicionar e ainda não ganhou personagem.
                                    Text("🌱", style = MaterialTheme.typography.displayLarge)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                c.nomeParaMostrar(),
                                style = MaterialTheme.typography.titleLarge,
                                color = Cores.Texto,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
