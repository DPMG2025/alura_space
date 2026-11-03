package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Corujinha
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

/**
 * Tela da Corujinha Professora com um balão e um ou dois botões grandes: o convite do "Relembrar" e do
 * "Descobrir o nível" e o fim de cada um. O narrador lê o texto ao abrir. Nunca abre sozinha: só quando a
 * criança (ou o adulto) toca no botão que leva até ela.
 * Marcas de teste: [marcaSim] e [marcaNao].
 */
@Composable
fun ConviteDaCorujinha(
    titulo: String,
    texto: String,
    botaoSim: String,
    onSim: () -> Unit,
    marcaSim: String,
    cor: Color = Cores.Roxo,
    botaoNao: String? = null,
    onNao: () -> Unit = {},
    marcaNao: String = "",
    /** Algo pequeno embaixo do balão (moedinhas ganhas, mundo sugerido). */
    destaque: String? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    LaunchedEffect(texto) { narrador.falar(texto) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cor.copy(alpha = 0.75f), Color(0xFFEDE6FF), Cores.Fundo)))
            .clickable(remember { MutableInteractionSource() }, indication = null) { }
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(titulo, style = MaterialTheme.typography.headlineMedium, color = Color.White, textAlign = TextAlign.Center)
                Box(
                    Modifier
                        .widthIn(max = 520.dp)
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(28.dp))
                        .background(Color.White, RoundedCornerShape(28.dp))
                        .border(4.dp, Color(0xFFEDE6FF), RoundedCornerShape(28.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Corujinha Professora", style = MaterialTheme.typography.titleMedium, color = Cores.Roxo, modifier = Modifier.weight(1f))
                            BotaoRedondo("🔊", "Ouvir de novo", { narrador.falar(texto) }, tamanho = 48.dp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(texto, style = MaterialTheme.typography.titleLarge, color = Cores.Texto)
                    }
                }
                Corujinha(Modifier.height(170.dp), falando = narrador.falando)
                if (destaque != null) {
                    Text(
                        destaque,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Cores.Texto,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(50))
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                if (botaoNao != null) {
                    BotaoFofo(
                        onClick = { narrador.parar(); sons.toque(); onNao() },
                        cor = Color.White,
                        corBase = Color(0xFFD9CFF0),
                        descricao = botaoNao,
                        modifier = Modifier.testTag(marcaNao),
                    ) {
                        Text(botaoNao, style = MaterialTheme.typography.titleLarge, color = Cores.TextoSuave, modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp))
                    }
                }
                BotaoFofo(
                    onClick = { narrador.parar(); sons.toque(); onSim() },
                    cor = Cores.Certo,
                    descricao = botaoSim,
                    modifier = Modifier.testTag(marcaSim).pulsando(),
                ) {
                    Text(botaoSim, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp))
                }
            }
        }
    }
}

/** Convite do Relembrar: "Vamos!" começa a rodada, "Agora não" volta (sem perder nada). */
@Composable
fun TelaConviteRelembrar(nome: String?, quantas: Int, onVamos: () -> Unit, onAgoraNao: () -> Unit) {
    ConviteDaCorujinha(
        titulo = "🦉 Relembrar",
        texto = br.com.casinhadosaber.dominio.Relembrar.convite(nome, quantas),
        botaoSim = "Vamos!",
        onSim = onVamos,
        marcaSim = "relembrar_vamos",
        botaoNao = "Agora não",
        onNao = onAgoraNao,
        marcaNao = "relembrar_agora_nao",
        destaque = "+${br.com.casinhadosaber.dominio.Relembrar.MOEDAS} 🪙",
    )
}

/** Fim da rodada: sempre um elogio (errar não tira nada) e as moedinhas ganhas. */
@Composable
fun TelaFimRelembrar(nome: String?, acertos: Int, total: Int, moedas: Int, onContinuar: () -> Unit) {
    val quem = if (nome.isNullOrBlank()) "Você" else nome
    val texto = when {
        total > 0 && acertos == total -> "$quem lembrou de tudo! Relembrar deixa a cabeça forte."
        acertos * 2 >= total -> "Muito bem, $quem! Relembrar é assim: um pouquinho de cada vez."
        else -> "Que bom que você treinou! Daqui a uns dias a gente relembra de novo, juntinhos."
    }
    ConviteDaCorujinha(
        titulo = "🦉 Relembrar",
        texto = texto,
        botaoSim = "Continuar",
        onSim = onContinuar,
        marcaSim = "relembrar_continuar",
        destaque = if (moedas > 0) "+$moedas 🪙" else "⭐",
    )
}

/**
 * Botão do Relembrar no início e no caminho: a Corujinha pequena com o número de coisinhas.
 * Só aparece quando há algo para relembrar hoje; tocar abre o convite (nunca começa sozinho).
 */
@Composable
fun BotaoRelembrar(quantas: Int, onClick: () -> Unit, modifier: Modifier = Modifier, comTexto: Boolean = true) {
    val sons = LocalSons.current
    BotaoFofo(
        onClick = { sons.toque(); onClick() },
        cor = Color(0xFFEDE6FF),
        corBase = Cores.Roxo,
        descricao = "Relembrar com a Corujinha: $quantas para hoje",
        modifier = modifier.testTag("abrir_relembrar"),
    ) {
        Row(Modifier.padding(horizontal = if (comTexto) 12.dp else 6.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                Corujinha(Modifier.size(width = 50.dp, height = 56.dp))
                Text(
                    "$quantas",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 14.dp, y = (-4).dp)
                        .background(Color(0xFFFF8A3D), CircleShape)
                        .padding(horizontal = 7.dp),
                )
            }
            if (comTexto) {
                Spacer(Modifier.width(6.dp))
                Text("Relembrar", style = MaterialTheme.typography.titleLarge, color = Cores.Roxo)
            }
        }
    }
}

/** As perguntas da rodada: uma pergunta nova de cada fase, na ordem de [fases] (mesmo gerador das fases). */
fun perguntasDoRelembrar(gerador: GeradorDeFases, fases: List<Pair<Trilha, Int>>): Pair<Fase, List<Pergunta>>? {
    if (fases.isEmpty()) return null
    val todas = fases.map { (t, n) -> gerador.fases(t)[n] }
    return todas.first() to todas.map { gerador.perguntaDaFase(it) }
}
