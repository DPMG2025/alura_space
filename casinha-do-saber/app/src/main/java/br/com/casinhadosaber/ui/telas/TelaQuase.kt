package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

/**
 * A fase não foi vencida (menos de 3 acertos de primeira). Sem bronca: o presente
 * continua fechado, tremendo de vontade de abrir, e a criança é convidada a tentar de novo.
 */
@Composable
fun TelaQuase(
    fase: Fase,
    acertos: Int,
    perfil: Perfil?,
    onTentarDeNovo: () -> Unit,
    onVoltar: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val tremor = rememberInfiniteTransition(label = "tremor")
    val giro by tremor.animateFloat(-6f, 6f, infiniteRepeatable(tween(140), RepeatMode.Reverse), label = "giro")

    LaunchedEffect(Unit) {
        sons.toque()
        narrador.falar(
            "Quase! Você acertou $acertos de primeira. O presente quer sair da caixa! " +
                "Para abrir, acerte três de primeira. Vamos tentar de novo?",
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(fase.trilha.corArgb), Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Quase!", style = MaterialTheme.typography.displayLarge, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text(
            "Acerte 3 de primeira para abrir o presente",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Box(Modifier.size(190.dp).background(Color.White.copy(alpha = 0.85f), CircleShape), contentAlignment = Alignment.Center) {
            Text("🎁", fontSize = 96.sp, lineHeight = 108.sp, modifier = Modifier.graphicsLayer { rotationZ = giro })
            Text("🔒", fontSize = 36.sp, modifier = Modifier.align(Alignment.BottomEnd))
        }
        Spacer(Modifier.height(12.dp))
        perfil?.let { Crianca(it, Modifier.height(130.dp), acenando = true, humor = Humor.FELIZ) }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            BotaoRedondo("🗺️", "Voltar para o caminho", onVoltar, tamanho = 60.dp)
            BotaoFofo(onClick = onTentarDeNovo, cor = Cores.Certo, modifier = Modifier.pulsando(), descricao = "Tentar de novo") {
                Text(
                    "🔄 Tentar de novo",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
    }
}
