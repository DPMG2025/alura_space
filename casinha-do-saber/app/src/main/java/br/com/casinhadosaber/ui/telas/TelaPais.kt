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
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.ui.platform.LocalContext
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Fase
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import br.com.casinhadosaber.ui.LocalNarrador
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
    // Caminhos abertos na lista de fases (nomes separados por vírgula, para sobreviver a girar a tela).
    // Começa com o caminho da fase que está sendo praticada, se houver.
    val praticandoInicial = remember(estado) { faseMaisDificil(estado, gerador) }
    var abertos by rememberSaveable { mutableStateOf(praticandoInicial?.first?.trilha?.name.orEmpty()) }

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
        // A fase liberada em que mais erra de primeira (últimas 10 respostas).
        val praticando = praticandoInicial
        if (praticando != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Praticando agora: ${praticando.first.trilha.emoji} ${praticando.first.titulo} (${praticando.second}% de primeira). " +
                    dicaOffline(praticando.first.trilha),
                style = MaterialTheme.typography.bodyLarge,
                color = Cores.Texto,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Toque num caminho para ver as ${GeradorDeFases.FASES_POR_TRILHA} fases. As porcentagens são dos acertos de primeira nas últimas 10 respostas de cada fase.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        for (t in Trilha.entries) {
            Spacer(Modifier.height(10.dp))
            CaminhoRecolhivel(
                trilha = t,
                fases = gerador.fases(t),
                estado = estado,
                praticando = praticando?.first,
                aberto = t.name in abertos.split(","),
                onAlternar = {
                    val lista = abertos.split(",").filter { it.isNotEmpty() }
                    abertos = (if (t.name in lista) lista - t.name else lista + t.name).joinToString(",")
                },
            )
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
        if (!LocalNarrador.current.vozesBoas) {
            Spacer(Modifier.height(12.dp))
            val contexto = LocalContext.current
            BotaoFofo(
                onClick = {
                    runCatching {
                        contexto.startActivity(
                            Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    }
                },
                cor = Color.White,
                corBase = Color(0xFFD9CFF0),
            ) {
                Text("🗣️ Instalar vozes melhores (português e inglês)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
            }
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
                "• Para ganhar o presente da fase, é preciso acertar pelo menos ${EstadoJogo.MINIMO_PARA_VENCER} de 5 de primeira. Assim a casa cresce junto com o aprendizado.\n" +
                "• Toda fase revisa uma pergunta de uma fase anterior.\n" +
                "• Cada fase nova vencida dá uma peça para a casa. Montar a casa (tocar, arrastar, pintar) treina coordenação e dá vontade de continuar. " +
                "Depois das ${ItensDaCasa.total} peças, cada fase nova dá um enfeite da lojinha e mais ${EstadoJogo.MOEDAS_DO_BRINDE} moedinhas.\n" +
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

/** A fase liberada em que a criança mais erra de primeira (abaixo de 80%), com a porcentagem. */
private fun faseMaisDificil(estado: EstadoJogo, gerador: GeradorDeFases): Pair<Fase, Int>? =
    Trilha.entries.flatMap { gerador.fases(it) }
        .filter { estado.liberada(it) }
        .mapNotNull { f -> estado.precisaoDaFase(EstadoJogo.chave(f.trilha, f.numero))?.let { f to it } }
        .filter { it.second < 80 }
        .minByOrNull { it.second }

private val CinzaSemJogo = Color(0xFFD0D0D0)
private val CinzaFechada = Color(0xFFEDEDED)

private fun corDaFase(estado: EstadoJogo, f: Fase): Color {
    val p = estado.precisaoDaFase(EstadoJogo.chave(f.trilha, f.numero))
    return when {
        p != null && p >= 80 -> Cores.Certo
        p != null -> Cores.Estrela
        estado.estrelasDa(f) > 0 -> Cores.Certo
        estado.liberada(f) -> CinzaSemJogo
        else -> CinzaFechada
    }
}

/**
 * Um caminho na lista de progresso: o cabeçalho resume (vencidas, acertos de primeira, fases para reforçar,
 * uma faixa com uma cor por fase) e, ao tocar, abre a lista das fases.
 */
@Composable
private fun CaminhoRecolhivel(
    trilha: Trilha,
    fases: List<Fase>,
    estado: EstadoJogo,
    praticando: Fase?,
    aberto: Boolean,
    onAlternar: () -> Unit,
) {
    val cor = Color(trilha.corArgb)
    val vencidas = estado.vencidasNa(trilha)
    val precisao = estado.precisao(trilha)
    val reforcar = fases.count { f -> estado.precisaoDaFase(EstadoJogo.chave(f.trilha, f.numero))?.let { it < 80 } == true }
    val giro by animateFloatAsState(if (aberto) 90f else 0f, label = "seta")
    val resumo = buildString {
        append("$vencidas de ${fases.size} vencidas")
        append(if (precisao == null) " • ainda não jogou" else " • $precisao% de primeira")
        if (reforcar > 0) append(" • $reforcar para reforçar")
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cor.copy(alpha = 0.08f))
            .border(2.dp, cor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClickLabel = if (aberto) "Recolher" else "Ver as fases", onClick = onAlternar)
                .semantics(mergeDescendants = true) {
                    stateDescription = if (aberto) "aberto" else "recolhido"
                }
                .padding(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${trilha.emoji} ${trilha.titulo}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("$vencidas/${fases.size}", style = MaterialTheme.typography.titleMedium, color = Color(trilha.corEscuraArgb))
                Spacer(Modifier.width(8.dp))
                Text("▶", style = MaterialTheme.typography.titleMedium, color = Cores.TextoSuave, modifier = Modifier.rotate(giro))
            }
            Text(resumo, style = MaterialTheme.typography.bodyMedium, color = Cores.TextoSuave)
            Spacer(Modifier.height(6.dp))
            // Uma fatia por fase: verde (80% ou mais), amarela (aprendendo), cinza (não jogou), clarinha (fechada).
            Row(Modifier.fillMaxWidth().height(10.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (f in fases) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(corDaFase(estado, f), RoundedCornerShape(3.dp)),
                    )
                }
            }
        }
        AnimatedVisibility(visible = aberto, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 10.dp)) {
                for (f in fases) {
                    val e = estado.estrelasDa(f)
                    val p = estado.precisaoDaFase(EstadoJogo.chave(f.trilha, f.numero))
                    val destaque = f == praticando
                    if (f.numero > 0 && f.numero % 5 == 0) {
                        // Divisória a cada 5 fases, como os marcos do caminho da criança.
                        Box(Modifier.fillMaxWidth().padding(vertical = 4.dp).height(1.dp).background(cor.copy(alpha = 0.3f)))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (destaque) Modifier.background(Cores.Estrela.copy(alpha = 0.25f), RoundedCornerShape(8.dp)) else Modifier)
                            .padding(vertical = 3.dp, horizontal = 4.dp),
                    ) {
                        Box(Modifier.size(12.dp).background(corDaFase(estado, f), RoundedCornerShape(50)))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${f.numero + 1}. ${f.titulo}" + if (destaque) "  (praticando agora)" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (estado.liberada(f)) Cores.Texto else Cores.TextoSuave,
                            modifier = Modifier.weight(1f),
                        )
                        if (p != null) Text("$p%  ", style = MaterialTheme.typography.bodyMedium, color = Cores.TextoSuave)
                        when {
                            e > 0 -> FileiraDeEstrelas(e, tamanho = 16.dp)
                            estado.liberada(f) -> Text("ainda não", style = MaterialTheme.typography.bodyMedium, color = Cores.TextoSuave)
                            else -> Text("🔒", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

private fun dicaOffline(t: Trilha): String = when (t) {
    Trilha.MATEMATICA -> "Fora da tela: contem juntos os degraus da escada ou as frutas na mesa."
    Trilha.PORTUGUES -> "Fora da tela: brinquem de achar coisas da casa que começam com a mesma letra."
    Trilha.INGLES -> "Fora da tela: digam juntos o nome das cores das roupas em inglês."
    Trilha.LOGICA -> "Fora da tela: façam filas de brinquedos (carrinho, bola, carrinho, bola...) e peça o próximo."
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
