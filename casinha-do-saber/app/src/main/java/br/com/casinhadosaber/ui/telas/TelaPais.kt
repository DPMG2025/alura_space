package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.FileiraDeEstrelas
import br.com.casinhadosaber.ui.componentes.PortaoDeAdulto
import br.com.casinhadosaber.ui.tema.Cores
import kotlin.random.Random

/** Área dos adultos: protegida por uma continha que a criança pequena ainda não resolve sozinha. */
@Composable
fun TelaPais(
    estado: EstadoJogo,
    somLigado: Boolean,
    onAlternarSom: () -> Unit,
    musicaLigada: Boolean,
    onAlternarMusica: () -> Unit,
    onEditarPersonagem: () -> Unit,
    onApagarTudo: () -> Unit,
    onVoltar: () -> Unit,
    limiteMinutos: Int = 0,
    onAlternarLimite: () -> Unit = {},
) {
    var liberado by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxSize()
            .background(Cores.Fundo)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotaoRedondo("⬅️", "Voltar", onVoltar, tamanho = 48.dp)
            Spacer(Modifier.width(12.dp))
            Text("Área dos pais", style = MaterialTheme.typography.headlineMedium)
        }
        Spacer(Modifier.height(16.dp))
        if (!liberado) {
            PortaoDeAdulto(onLiberar = { liberado = true })
        } else {
            Painel(estado, somLigado, onAlternarSom, musicaLigada, onAlternarMusica, onEditarPersonagem, onApagarTudo, limiteMinutos, onAlternarLimite)
        }
    }
}

@Composable
private fun Painel(
    estado: EstadoJogo,
    somLigado: Boolean,
    onAlternarSom: () -> Unit,
    musicaLigada: Boolean,
    onAlternarMusica: () -> Unit,
    onEditarPersonagem: () -> Unit,
    onApagarTudo: () -> Unit,
    limiteMinutos: Int,
    onAlternarLimite: () -> Unit,
) {
    var confirmar by remember { mutableStateOf(false) }
    val gerador = remember { GeradorDeFases() }

    Cartao {
        Text("🔒 Sem internet • Sem anúncios • Sem coleta de dados", style = MaterialTheme.typography.titleMedium, color = Cores.Certo)
    }

    Cartao {
        Text("O que a criança está aprendendo", style = MaterialTheme.typography.titleLarge)
        Text(
            "Tempo de jogo: ${estado.segundosJogados / 60} min. Acertos de primeira por caminho:",
            style = MaterialTheme.typography.bodyLarge,
        )
        for (t in Trilha.entries) {
            val p = estado.precisao(t)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${t.emoji} ${t.titulo}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(120.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .height(14.dp)
                        .background(Color(0xFFEDE6FF), RoundedCornerShape(50)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth((p ?: 0) / 100f)
                            .height(14.dp)
                            .background(Color(t.corArgb), RoundedCornerShape(50)),
                    )
                }
                Text(if (p == null) "  ainda não" else "  $p%", style = MaterialTheme.typography.bodyMedium)
            }
            val dica = when {
                p == null -> null
                p >= 80 -> "Indo muito bem!"
                p >= 60 -> "Aprendendo: vale jogar junto e conversar sobre as respostas."
                else -> "Precisa de ajuda: repita as fases anteriores e jogue junto."
            }
            if (dica != null) Text(dica, style = MaterialTheme.typography.bodyMedium, color = Cores.TextoSuave)
        }
    }

    Cartao {
        Text("Progresso", style = MaterialTheme.typography.titleLarge)
        Text(
            "Fases vencidas: ${estado.fasesVencidas} de ${Trilha.entries.size * GeradorDeFases.FASES_POR_TRILHA}. " +
                "Itens da casa: ${estado.itensGanhos.size} de ${ItensDaCasa.total}.",
            style = MaterialTheme.typography.bodyLarge,
        )
        for (t in Trilha.entries) {
            Spacer(Modifier.height(10.dp))
            Text("${t.emoji} ${t.titulo}: ${estado.vencidasNa(t)}/${GeradorDeFases.FASES_POR_TRILHA}", style = MaterialTheme.typography.titleMedium)
            for (f in gerador.fases(t)) {
                val e = estado.estrelasDa(f)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
                    Text("${f.numero + 1}. ${f.titulo}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    if (e > 0) FileiraDeEstrelas(e, tamanho = 16.dp) else Text("ainda não", style = MaterialTheme.typography.bodyMedium, color = Cores.TextoSuave)
                }
            }
        }
    }

    Cartao {
        Text("Configurações", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotaoFofo(onClick = onAlternarSom, cor = Color.White, corBase = Color(0xFFD9CFF0)) {
                Text(if (somLigado) "🔊 Som ligado" else "🔇 Som desligado", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
            }
            BotaoFofo(onClick = onAlternarMusica, cor = Color.White, corBase = Color(0xFFD9CFF0)) {
                Text(if (musicaLigada) "🎵 Música ligada" else "🎵 Música desligada", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        BotaoFofo(onClick = onAlternarLimite, cor = Color.White, corBase = Color(0xFFD9CFF0)) {
            Text(
                if (limiteMinutos == 0) "⏰ Tempo de jogo: livre" else "⏰ Tempo de jogo: $limiteMinutos min por dia",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp),
            )
        }
        Text(
            "Com limite, a casinha \"vai dormir\" quando o tempo do dia acaba. Ela acorda sozinha no dia seguinte, ou antes, se um adulto responder a continha.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        Spacer(Modifier.height(12.dp))
        BotaoFofo(onClick = onEditarPersonagem, cor = Color.White, corBase = Color(0xFFD9CFF0)) {
            Text("🧒 Trocar o personagem", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
        }
        Spacer(Modifier.height(12.dp))
        BotaoFofo(onClick = { confirmar = true }, cor = Cores.Errado) {
            Text("Apagar todo o progresso", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
        }
    }

    Cartao {
        Text("Como o app ensina", style = MaterialTheme.typography.titleLarge)
        Text(
            "• Tudo é narrado em voz alta, então a criança joga sem saber ler.\n" +
                "• Cada caminho vai do mais fácil ao mais difícil: primeiro contar, depois somar e tirar; " +
                "primeiro vogais, depois letras, sílabas e rimas; cores e bichos em inglês; padrões e sequências na lógica.\n" +
                "• Errar não tira nada: a criança ouve o nome do que tocou e tenta de novo. Depois de dois erros, a resposta certa brilha.\n" +
                "• Para ganhar o presente da fase, é preciso acertar pelo menos 3 de 5 de primeira. Assim a casa cresce junto com o aprendizado.\n" +
                "• Toda fase revisa uma pergunta de uma fase anterior.\n" +
                "• Cada fase nova vencida dá uma peça para a casa. Montar a casa (tocar, arrastar, pintar) treina coordenação e dá vontade de continuar.\n" +
                "• Sessões curtas, de 10 a 15 minutos, funcionam melhor nessa idade. Jogue junto e comemore cada peça!\n" +
                "• O app não usa internet, não tem anúncios e não coleta dados. Para ouvir as palavras em inglês, " +
                "instale a voz em inglês nas configurações de voz (Texto para fala) do celular.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Apagar tudo?") },
            text = { Text("A casa, as estrelas e o personagem serão apagados. Isso não pode ser desfeito.") },
            confirmButton = { TextButton(onClick = { confirmar = false; onApagarTudo() }) { Text("Apagar", color = Cores.Errado) } },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Cartao(conteudo: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) { conteudo() }
}
