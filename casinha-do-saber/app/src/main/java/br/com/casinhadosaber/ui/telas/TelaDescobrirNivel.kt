package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.dominio.Nivelamento
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.paraRelembrar
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.IconeDeTrilha
import androidx.compose.foundation.layout.size
import br.com.casinhadosaber.ui.tema.Cores

/** Convite do "Descobrir o nível" de um caminho: umas perguntas que sobem pelos mundos. Opcional. */
@Composable
fun TelaConviteNivel(trilha: Trilha, nome: String?, onComecar: () -> Unit, onAgoraNao: () -> Unit) {
    val quem = if (nome.isNullOrBlank()) "você" else nome
    ConviteDaCorujinha(
        titulo = "Descobrir o nível",
        icone = { IconeDeTrilha(trilha, Modifier.size(48.dp)) },
        texto = "Será que $quem já sabe bastante de ${trilha.titulo}? Vou fazer umas perguntinhas, cada vez mais difíceis. " +
            "Não tem problema errar: é só para saber por onde começar.",
        botaoSim = "Começar",
        onSim = onComecar,
        marcaSim = "nivel_comecar",
        cor = Color(trilha.corArgb),
        botaoNao = "Agora não",
        onNao = onAgoraNao,
        marcaNao = "nivel_agora_nao",
    )
}

/** Fim do "Descobrir o nível": a Corujinha conta até onde o caminho abriu. */
@Composable
fun TelaResultadoNivel(nivel: Nivelamento, nome: String?, onContinuar: () -> Unit) {
    val mundo = Mundos.todos[nivel.mundoSugerido - 1]
    ConviteDaCorujinha(
        titulo = nivel.trilha.titulo,
        icone = { IconeDeTrilha(nivel.trilha, Modifier.size(48.dp)) },
        texto = nivel.resultado(nome),
        botaoSim = "Ver o caminho",
        onSim = onContinuar,
        marcaSim = "nivel_continuar",
        cor = Color(nivel.trilha.corArgb),
        destaque = "${mundo.emoji} Mundo ${mundo.numero}",
    )
}

/** Botão do caminho na primeira visita (nenhuma fase vencida): "Já sei bastante!". */
@Composable
fun BotaoDescobrirNivel(trilha: Trilha, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val sons = LocalSons.current
    BotaoFofo(
        onClick = { sons.toque(); onClick() },
        cor = Color.White,
        corBase = Color(trilha.corEscuraArgb),
        descricao = "Descobrir o nível: já sei bastante",
        modifier = modifier.testTag("abrir_nivel"),
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🧭", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(8.dp))
            Text("Já sei bastante!", style = MaterialTheme.typography.titleLarge, color = Color(trilha.corEscuraArgb), fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Área dos pais: o ritmo da criança. Quantas coisinhas estão para relembrar e, por caminho, o "Descobrir o nível"
 * (fazer agora ou desfazer, voltando a abrir fase por fase).
 */
@Composable
fun CartaoDoRitmo(estado: EstadoJogo, hoje: Long, onDescobrir: (Trilha) -> Unit, onDesfazer: (Trilha) -> Unit) {
    CartaoDosPais {
        Text("🦉 No ritmo da criança", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        val hojeRelembrar = estado.paraRelembrar(hoje).size
        Text(
            if (hojeRelembrar == 0) {
                "Relembrar: nada para hoje. Quando a criança erra de primeira, a fase volta para uma revisão curta depois de 1, 3, 7 e 14 dias."
            } else {
                "Relembrar: $hojeRelembrar ${if (hojeRelembrar == 1) "fase" else "fases"} para hoje. A Corujinha aparece no início e no caminho; nunca é obrigatório."
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(12.dp))
        Text("Descobrir o nível", style = MaterialTheme.typography.titleMedium, color = Cores.Roxo)
        Text(
            "Umas 8 a 10 perguntas que sobem pelos mundos. Abre as fases até o mundo sugerido, sem dar estrelas nem presentes.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (t in Trilha.entries) {
                val ate = estado.liberadaAte[t]
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .background(Color(t.corArgb).copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("${t.emoji} ${t.titulo}", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when {
                                ate != null -> "Aberto até o mundo ${Mundos.daFase(ate).numero}"
                                t in estado.nivelFeito -> "Feito: começa do mundo 1"
                                else -> "Ainda não feito"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Cores.TextoSuave,
                        )
                    }
                    if (ate != null || t in estado.nivelFeito) {
                        BotaoFofo(onClick = { onDesfazer(t) }, cor = Color.White, corBase = Color(0xFFD9CFF0), modifier = Modifier.testTag("nivel_desfazer_${t.name}")) {
                            Text("Desfazer", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp))
                        }
                    } else {
                        BotaoFofo(onClick = { onDescobrir(t) }, cor = Color(t.corArgb), corBase = Color(t.corEscuraArgb), modifier = Modifier.testTag("nivel_fazer_${t.name}")) {
                            Text("Fazer", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp))
                        }
                    }
                }
            }
        }
    }
}
