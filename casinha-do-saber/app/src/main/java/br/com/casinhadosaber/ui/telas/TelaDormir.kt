package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.PortaoDeAdulto
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.tema.Formas
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Fim do tempo do dia: a casinha vai dormir até amanhã (ou até um adulto liberar). */
@Composable
fun TelaDormir(estado: EstadoJogo, onLiberar: () -> Unit) {
    val narrador = LocalNarrador.current

    LaunchedEffect(Unit) {
        // "Good night!" vira o "Boa noite!" gravado pela família, quando houver (área dos pais).
        narrador.falarPartes(
            listOf(
                br.com.casinhadosaber.dominio.ParteDaFala(pt("Que dia divertido! Agora a casinha vai dormir. Até amanhã!")),
                br.com.casinhadosaber.dominio.ParteDaFala(en("Good night!"), "boa_noite"),
            ),
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .ceuDaNoite()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Boa noite! 🌙", style = MaterialTheme.typography.displayMedium, color = Color.White)
        Spacer(Modifier.height(12.dp))
        CenaDaCasa(
            estado,
            Modifier
                .shadow(12.dp, Formas.Grande, ambientColor = Color(0xFF05081A), spotColor = Color(0xFF05081A))
                .clip(Formas.Grande)
                .border(3.dp, Color.White.copy(alpha = 0.25f), Formas.Grande)
                .weight(1f, fill = false),
            noite = true,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "A casinha volta amanhã! 💤",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        // Só um adulto libera (continha de multiplicação). Sem isso, a casinha acorda sozinha amanhã.
        var mostrarPortao by remember { mutableStateOf(false) }
        if (mostrarPortao) {
            PortaoDeAdulto(onLiberar = onLiberar)
        } else {
            // Botão sólido (o translúcido de antes parecia desligado), num azul da noite.
            BotaoFofo(onClick = { mostrarPortao = true }, cor = Color(0xFF3B4C8C), profundidade = 4.dp) {
                Text(
                    "🔒 Adulto",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
        }
    }
}

/** Céu da noite: azul fundo que clareia um pouco embaixo, com estrelinhas que piscam devagar. */
@Composable
private fun Modifier.ceuDaNoite(): Modifier {
    val fase by rememberInfiniteTransition(label = "noite")
        .animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "pisca")
    return drawWithCache {
        val ceu = Brush.verticalGradient(listOf(Color(0xFF0F1638), Color(0xFF16204A), Color(0xFF2B3A78)))
        val estrela = Path()
        onDrawBehind {
            drawRect(ceu)
            for (k in 0 until ESTRELAS_DA_NOITE.size / 3) {
                val a = 2f * PI.toFloat() * (fase + k * 0.23f)
                val alfa = 0.35f + 0.35f * (0.5f + 0.5f * sin(a))
                val cx = ESTRELAS_DA_NOITE[k * 3] * size.width
                val cy = ESTRELAS_DA_NOITE[k * 3 + 1] * size.height
                val r = ESTRELAS_DA_NOITE[k * 3 + 2].dp.toPx()
                estrela.reset()
                estrela.moveTo(cx, cy - r)
                estrela.quadraticTo(cx, cy, cx + r, cy)
                estrela.quadraticTo(cx, cy, cx, cy + r)
                estrela.quadraticTo(cx, cy, cx - r, cy)
                estrela.quadraticTo(cx, cy, cx, cy - r)
                estrela.close()
                drawPath(estrela, Color.White.copy(alpha = alfa))
            }
        }
    }
}

/** x, y (fração da tela) e tamanho (dp) das estrelinhas. */
private val ESTRELAS_DA_NOITE = floatArrayOf(
    0.08f, 0.05f, 6f, 0.30f, 0.11f, 4f, 0.86f, 0.06f, 7f, 0.62f, 0.03f, 5f,
    0.94f, 0.22f, 4f, 0.05f, 0.30f, 5f, 0.18f, 0.82f, 4f, 0.90f, 0.78f, 6f,
    0.50f, 0.93f, 4f, 0.74f, 0.90f, 5f,
)
