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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.AcessorioMascote
import br.com.casinhadosaber.dominio.Crescimento
import br.com.casinhadosaber.dominio.EspecieMascote
import br.com.casinhadosaber.dominio.EstagioMascote
import br.com.casinhadosaber.dominio.Mascote
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.MascoteAnimado
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay

// Telas do mascote (sugestão 2): a escolha da primeira vez, o cantinho com os enfeites e a festa de crescer.
// O mascote nunca fica triste, doente ou com fome: só cresce e ganha enfeites com as fases vencidas.

/** Fundo escurecido que segura os toques (nada atrás dele responde enquanto ele está aberto). */
@Composable
private fun FundoDoAviso(conteudo: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x99231A3A))
            .clickable(remember { MutableInteractionSource() }, indication = null) { },
        contentAlignment = Alignment.Center,
    ) { conteudo() }
}

@Composable
private fun CartaoDoAviso(modifier: Modifier = Modifier, conteudo: @Composable () -> Unit) {
    Box(
        modifier
            .padding(16.dp)
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(32.dp))
            .background(Cores.Fundo, RoundedCornerShape(32.dp))
            .border(4.dp, Color.White, RoundedCornerShape(32.dp))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) { conteudo() }
}

/**
 * Primeira vez: a criança escolhe o amiguinho (cachorrinho, gatinho ou dragãozinho) e, se quiser, dá um nome.
 * Marcas de teste: "mascote_CACHORRINHO", "mascote_GATINHO", "mascote_DRAGAOZINHO" e "mascote_pronto".
 */
@Composable
fun EscolhaDoMascote(onEscolher: (EspecieMascote, String) -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var escolhida by remember { mutableStateOf<EspecieMascote?>(null) }
    var nome by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        narrador.falar("Escolha um amiguinho para morar na sua casa! Ele cresce junto com você.")
    }
    FundoDoAviso {
        CartaoDoAviso {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Escolha o seu amiguinho!", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (e in EspecieMascote.entries) {
                        val sel = escolhida == e
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (sel) Color(0xFFFFF3C4) else Color.White)
                                .border(if (sel) 5.dp else 3.dp, if (sel) Cores.Estrela else Color(0xFFEDE6FF), RoundedCornerShape(24.dp))
                                .clickable(role = Role.Button) {
                                    escolhida = e
                                    sons.toque()
                                    narrador.falar("${e.nome}!")
                                }
                                .semantics { contentDescription = e.nome }
                                .testTag("mascote_${e.name}")
                                .padding(horizontal = 2.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            MascoteAnimado(e, EstagioMascote.BEBE, Modifier.fillMaxWidth(), pulando = sel)
                            Text(e.nome, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, maxLines = 1, softWrap = false, fontSize = 13.sp)
                        }
                    }
                }
                if (escolhida != null) {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = Mascote.limparNome(it) },
                        label = { Text("Nome do amiguinho (se quiser)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        textStyle = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                BotaoFofo(
                    onClick = {
                        val e = escolhida ?: return@BotaoFofo
                        sons.premio()
                        val quem = nome.trim().ifEmpty { e.nome }
                        narrador.falar("Oba! $quem vai morar com você! Cada fase nova faz $quem crescer.")
                        onEscolher(e, nome)
                    },
                    habilitado = escolhida != null,
                    cor = if (escolhida != null) Cores.Certo else Color(0xFFBDBDBD),
                    descricao = "Pronto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mascote_pronto")
                        .then(if (escolhida != null) Modifier.pulsando() else Modifier),
                ) {
                    Text(
                        "✔ Pronto",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                    )
                }
            }
        }
    }
}

/**
 * O cantinho do mascote (tocar nele na casa): o mascote grande, quanto falta para crescer e os enfeites
 * já liberados para escolher. Os enfeites que faltam aparecem com cadeado e dizem quantas fases faltam.
 */
@Composable
fun CantinhoDoMascote(
    mascote: Mascote,
    fasesVencidas: Int,
    nomeDaCrianca: String?,
    onVestir: (AcessorioMascote?) -> Unit,
    onFechar: () -> Unit,
) {
    val especie = mascote.especie ?: return
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val estagio = Crescimento.estagio(fasesVencidas)
    val liberados = Crescimento.acessoriosLiberados(fasesVencidas)
    var fala by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { narrador.falar(Crescimento.fala(mascote, fasesVencidas, nomeDaCrianca, 0)) }
    FundoDoAviso {
        CartaoDoAviso {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(mascote.nomeParaMostrar, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text("${especie.nome} ${estagio.nome.lowercase()}", style = MaterialTheme.typography.titleMedium, color = Cores.TextoSuave)
                Box(
                    Modifier
                        .size(190.dp)
                        .clickable(role = Role.Button) {
                            fala++
                            sons.piu(fala)
                            narrador.falar(Crescimento.fala(mascote, fasesVencidas, nomeDaCrianca, fala))
                        },
                ) {
                    MascoteAnimado(especie, estagio, Modifier.fillMaxSize(), mascote.acessorioEmUso, mascote.nomeParaMostrar, pulando = true)
                }
                // Barrinha até o próximo estágio.
                val faltam = Crescimento.faltamParaCrescer(fasesVencidas)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFEDE6FF)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(Crescimento.progressoNoEstagio(fasesVencidas).coerceIn(0.04f, 1f))
                            .height(22.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Cores.Certo),
                    )
                }
                Text(
                    when {
                        faltam == 0 -> "Já é campeão! 🏆"
                        faltam == 1 -> "Falta 1 fase nova para crescer!"
                        else -> "Faltam $faltam fases novas para crescer!"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Text("Enfeites", style = MaterialTheme.typography.titleLarge)
                // Enfeites em fileiras de 4 botões grandes.
                AcessorioMascote.entries.chunked(4).forEach { linha ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (a in linha) {
                            val livre = a in liberados
                            val usando = mascote.acessorio == a.name
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (usando) Color(0xFFFFF3C4) else if (livre) Color.White else Color(0xFFECE8F2))
                                    .border(if (usando) 4.dp else 2.dp, if (usando) Cores.Estrela else Color(0xFFEDE6FF), RoundedCornerShape(20.dp))
                                    .clickable(role = Role.Button) {
                                        if (livre) {
                                            sons.colocar()
                                            narrador.falar(if (usando) "Tirei ${a.nome.lowercase()}." else "${a.nome}!")
                                            onVestir(a)
                                        } else {
                                            sons.toque()
                                            val f = a.fasesMinimas - fasesVencidas
                                            narrador.falar(if (f == 1) "Esse enfeite chega com mais uma fase nova!" else "Esse enfeite chega com mais $f fases novas!")
                                        }
                                    }
                                    .semantics { contentDescription = if (livre) a.nome else "${a.nome}, ainda guardado" }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(if (livre) a.emoji else "🔒", fontSize = 30.sp)
                                Text(
                                    if (livre) a.nome.substringBefore(' ') else "${a.fasesMinimas}",
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    color = if (livre) Cores.Texto else Cores.TextoSuave,
                                )
                            }
                        }
                    }
                }
                BotaoFofo(onClick = { sons.toque(); onFechar() }, cor = Cores.Certo, descricao = "Fechar", modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "✔ Pronto",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

/**
 * Festa de crescimento na tela de prêmio: o mascote aparece no estágio novo, pulando, com confete, e o
 * narrador conta a novidade (e os enfeites novos, se vieram junto). Marca de teste: "festa_mascote".
 */
@Composable
fun FestaDoMascote(mascote: Mascote, fasesAntes: Int, fasesDepois: Int, onFechar: () -> Unit) {
    val especie = mascote.especie ?: return
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val cresceu = Crescimento.cresceu(fasesAntes, fasesDepois)
    val estagio = Crescimento.estagio(fasesDepois)
    val novos = Crescimento.acessoriosNovos(fasesAntes, fasesDepois)
    val quem = mascote.nomeParaMostrar
    var confete by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        sons.premio()
        confete++
        val partes = buildList {
            if (cresceu) add("Uau! $quem cresceu! Agora é ${estagio.nome.lowercase()}!")
            if (novos.isNotEmpty()) add("$quem ganhou ${novos.joinToString(" e ") { it.nome.lowercase() }}!")
        }
        narrador.falar(partes.joinToString(" "))
        delay(700)
        confete++
    }
    FundoDoAviso {
        CartaoDoAviso(Modifier.testTag("festa_mascote")) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (cresceu) "$quem cresceu!" else "Presente para $quem!",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
                if (cresceu) {
                    Text("Agora é ${estagio.nome.lowercase()}! 🎉", style = MaterialTheme.typography.titleLarge, color = Cores.Roxo, textAlign = TextAlign.Center)
                }
                MascoteAnimado(
                    especie,
                    estagio,
                    Modifier.size(210.dp),
                    novos.lastOrNull() ?: mascote.acessorioEmUso,
                    quem,
                    pulando = true,
                )
                if (novos.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        for (a in novos) {
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .background(Color.White, CircleShape)
                                    .border(3.dp, Cores.Estrela, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) { Text(a.emoji, fontSize = 32.sp) }
                        }
                    }
                    Text("Enfeite novo! Escolha na casa.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(4.dp))
                BotaoFofo(onClick = { sons.toque(); onFechar() }, cor = Cores.Certo, descricao = "Oba!", modifier = Modifier.fillMaxWidth().pulsando()) {
                    Text(
                        "🎉 Oba!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                    )
                }
            }
        }
        Confete(confete)
    }
}
