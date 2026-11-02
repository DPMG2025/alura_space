package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

@Composable
fun TelaInicio(
    estado: EstadoJogo,
    somLigado: Boolean,
    onAlternarSom: () -> Unit,
    onAbrirCasa: () -> Unit,
    onAbrirTrilha: (Trilha) -> Unit,
    onAreaDosPais: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val nome = estado.perfil?.nome.orEmpty()
    val noBau = estado.itensNoBau.size

    LaunchedEffect(Unit) {
        val oi = if (nome.isBlank()) "Oi!" else "Oi, $nome!"
        narrador.falar(
            when {
                noBau > 0 -> "$oi Tem presente no baú! Toque na casinha para montar."
                estado.fasesVencidas == 0 -> "$oi Vamos construir a sua casinha? Escolha um caminho para jogar."
                else -> "$oi Escolha um caminho para ganhar mais coisas para a sua casa!"
            },
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo, Cores.Fundo))),
    ) {
        // Cabeçalho
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BotaoAdulto(onAreaDosPais)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Casinha do Saber", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(
                    "🏠 ${estado.itensGanhos.size} de ${ItensDaCasa.total} coisas",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
            Spacer(Modifier.width(8.dp))
            BotaoRedondo(
                emoji = if (somLigado) "🔊" else "🔇",
                descricao = if (somLigado) "Desligar som" else "Ligar som",
                onClick = onAlternarSom,
                tamanho = 48.dp,
            )
        }

        // A casinha (toque para montar)
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .semantics {
                        contentDescription = "Minha casinha. Toque para montar."
                        role = Role.Button
                        onClick { onAbrirCasa(); true }
                    }
                    .clickable { sons.toque(); onAbrirCasa() },
            ) {
                CenaDaCasa(estado, Modifier.fillMaxHeight())
                if (noBau > 0) {
                    Box(
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .pulsando()
                            .background(Cores.Estrela, RoundedCornerShape(50))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Text(
                            if (noBau == 1) "🎁 1 presente! Toque aqui" else "🎁 $noBau presentes! Toque aqui",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }

        // Caminhos
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (linha in Trilha.entries.chunked(2)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (t in linha) {
                        BotaoTrilha(t, estado.vencidasNa(t), Modifier.weight(1f)) {
                            sons.toque()
                            onAbrirTrilha(t)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotaoTrilha(trilha: Trilha, vencidas: Int, modifier: Modifier, onClick: () -> Unit) {
    val total = GeradorDeFases.FASES_POR_TRILHA
    BotaoFofo(
        onClick = onClick,
        modifier = modifier,
        cor = Color(trilha.corArgb),
        corBase = Color(trilha.corEscuraArgb),
        descricao = "Caminho ${trilha.titulo}, $vencidas de $total fases",
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(trilha.emoji, style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(trilha.titulo, style = MaterialTheme.typography.titleLarge, color = Color.White, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x40FFFFFF)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(vencidas / total.toFloat())
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White),
                    )
                }
                Text("$vencidas/$total", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            }
        }
    }
}

/** Abre a área dos pais só com um toque longo, para a criança não entrar sem querer. */
@Composable
private fun BotaoAdulto(onAbrir: () -> Unit) {
    val narrador = LocalNarrador.current
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0x33FFFFFF))
            .semantics { contentDescription = "Área dos pais. Toque e segure." }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { narrador.falar("Essa parte é para os adultos.") },
                    onLongPress = { onAbrir() },
                )
            }
            .padding(12.dp),
    ) {
        Text("⚙️", style = MaterialTheme.typography.titleLarge)
    }
}
