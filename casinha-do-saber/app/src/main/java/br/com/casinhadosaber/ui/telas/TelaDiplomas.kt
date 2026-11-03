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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Conquistas
import br.com.casinhadosaber.dominio.Diploma
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.EscalaDeTablet
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.tema.Cores

/**
 * Parede dos diplomas: um quadro para cada mundo completo (as 10 fases vencidas), separados por caminho.
 * Tocar num diploma abre ele grande, com o nome da criança, e o narrador lê. Sem diploma ainda, a parede
 * mostra o mundo que está mais perto de completar.
 */
@Composable
fun TelaDiplomas(
    estado: EstadoJogo,
    onVoltar: () -> Unit,
    /** Para as capturas: já abre com este diploma grande. */
    abrirDiploma: Diploma? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val nome = estado.perfil?.nome
    val diplomas = remember(estado.estrelas) { Conquistas.diplomas(estado.estrelas) }
    var aberto by remember { mutableStateOf(abrirDiploma) }
    var confete by remember { mutableIntStateOf(if (abrirDiploma != null) 1 else 0) }

    LaunchedEffect(Unit) {
        narrador.falar(
            if (diplomas.isEmpty()) "Parede dos diplomas! Complete as dez fases de um mundo para ganhar o primeiro."
            else "Parede dos diplomas! Você já tem ${diplomas.size}. Toque num diploma para ver.",
        )
    }

    EscalaDeTablet {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFFF6E7D0), Color(0xFFEBD3B0))))
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    BotaoRedondo("⬅️", "Voltar", onVoltar, tamanho = 52.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("📜 Diplomas", style = MaterialTheme.typography.headlineMedium, maxLines = 1)
                        Text(
                            "${diplomas.size} de ${Trilha.entries.size * Mundos.todos.size} mundos completos",
                            style = MaterialTheme.typography.titleSmall,
                            color = Cores.TextoSuave,
                            maxLines = 1,
                        )
                    }
                }
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    if (diplomas.isEmpty()) {
                        ParedeVazia(estado)
                    }
                    for (t in Trilha.entries) {
                        val daTrilha = diplomas.filter { it.trilha == t }
                        if (daTrilha.isEmpty()) continue
                        Text("${t.emoji} ${t.titulo}", style = MaterialTheme.typography.titleLarge, color = Color(t.corEscuraArgb))
                        for (linha in daTrilha.chunked(3)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                for (d in linha) {
                                    QuadroDeDiploma(d, Modifier.weight(1f)) {
                                        sons.premio()
                                        aberto = d
                                        confete++
                                        narrador.falar(d.texto(nome))
                                    }
                                }
                                repeat(3 - linha.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
            aberto?.let { d -> DiplomaGrande(d, nome) { sons.toque(); aberto = null } }
            Confete(confete)
        }
    }
}

/** Sem diploma ainda: uma moldura vazia e o mundo mais perto de completar, para dar vontade. */
@Composable
private fun ParedeVazia(estado: EstadoJogo) {
    val perto = Trilha.entries.flatMap { t ->
        Mundos.todos.map { m -> Triple(t, m, (m.primeiraFase..m.ultimaFase).count { EstadoJogo.chave(t, it) in estado.estrelas }) }
    }.maxByOrNull { it.third }
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(width = 140.dp, height = 110.dp)
                .border(8.dp, Color(0xFFC98B5B), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) { Text("❔", fontSize = 40.sp) }
        Spacer(Modifier.height(12.dp))
        Text(
            "Complete as 10 fases de um mundo para pendurar o primeiro diploma!",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (perto != null && perto.third > 0) {
            val (t, m, n) = perto
            Spacer(Modifier.height(8.dp))
            Text(
                "${t.emoji} ${m.emoji} Mundo ${m.numero}: ${m.nome}: $n de ${Mundos.FASES_POR_MUNDO}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(t.corEscuraArgb),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun QuadroDeDiploma(d: Diploma, modifier: Modifier, onClick: () -> Unit) {
    val m = Mundos.todos[d.mundo]
    BotaoFofo(
        onClick = onClick,
        modifier = modifier.testTag("diploma"),
        cor = Color(0xFFC98B5B),
        corBase = Color(0xFF8D5A3B),
        forma = RoundedCornerShape(8.dp),
        descricao = d.titulo + ", caminho " + d.trilha.titulo,
        profundidade = 4.dp,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .background(Color(0xFFFFF6DC), RoundedCornerShape(4.dp))
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(m.emoji, fontSize = 30.sp)
            Text("Mundo ${m.numero}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Cores.Texto, maxLines = 1)
            Text(m.nome, fontSize = 11.sp, color = Cores.TextoSuave, maxLines = 1, textAlign = TextAlign.Center)
            Text("🏅", fontSize = 18.sp)
        }
    }
}

/** O diploma aberto, grande, com o texto ([Diploma.texto]) e o nome da criança. Toque em qualquer lugar fecha. */
@Composable
private fun DiplomaGrande(d: Diploma, nome: String?, onFechar: () -> Unit) {
    val m = Mundos.todos[d.mundo]
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable(remember { MutableInteractionSource() }, indication = null, onClick = onFechar)
            .semantics { contentDescription = "Fechar o diploma" },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(20.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .background(Color(0xFFC98B5B), RoundedCornerShape(10.dp))
                .padding(10.dp)
                .background(Color(0xFFFFF6DC), RoundedCornerShape(6.dp))
                .border(2.dp, Color(d.trilha.corArgb), RoundedCornerShape(6.dp))
                .padding(20.dp)
                .testTag("diploma_aberto"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("DIPLOMA", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color(d.trilha.corEscuraArgb), letterSpacing = 3.sp)
            Text("${d.trilha.emoji}  ${m.emoji}  🏅", fontSize = 34.sp, modifier = Modifier.padding(vertical = 10.dp))
            if (!nome.isNullOrBlank()) {
                Text(nome, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
            }
            Text(d.texto(nome), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Text("✨", fontSize = 26.sp)
            Spacer(Modifier.height(10.dp))
            BotaoRedondo("✖", "Fechar", onFechar, tamanho = 52.dp)
        }
    }
}
