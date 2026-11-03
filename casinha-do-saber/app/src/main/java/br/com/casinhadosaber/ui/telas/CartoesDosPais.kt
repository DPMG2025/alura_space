package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.Aniversario
import br.com.casinhadosaber.dominio.Calendario
import br.com.casinhadosaber.dominio.Data
import br.com.casinhadosaber.dominio.DesafioDosPais
import br.com.casinhadosaber.dominio.Habilidade
import br.com.casinhadosaber.dominio.Pais
import br.com.casinhadosaber.dominio.RelatorioSemanal
import br.com.casinhadosaber.dominio.TemaFavorito
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.tema.Cores

/*
 * Cartões novos da área dos pais (sugestões 1 e 7): desafio do dia para fazer junto, relatório da semana,
 * aniversário e temas favoritos. Todos ficam atrás do portão de adulto (TelaPais).
 * Marcas de teste: "fizemos", "desafio_feito", "aniversario_dia_menos", "aniversario_dia_mais",
 * "aniversario_mes_menos", "aniversario_mes_mais", "aniversario_salvar", "aniversario_tirar", "tema_<NOME>".
 */

private val Lilas = Color(0xFFD9CFF0)
private val LilasClaro = Color(0xFFF4F0FF)

private val NomesDosMeses = listOf(
    "janeiro", "fevereiro", "março", "abril", "maio", "junho",
    "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
)

private fun Data.curta() = "${dia}/${mes.toString().padStart(2, '0')}"

@Composable
internal fun CartaoDosPais(modifier: Modifier = Modifier, conteudo: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp),
    ) { conteudo() }
}

/** Desafio do dia em família (com quem cuida da criança): longe da tela; o adulto marca "Fizemos!" e a criança ganha moedinhas, uma vez por dia. */
@Composable
fun CartaoDesafioDosPais(desafio: DesafioDosPais, feito: Boolean, onFizemos: () -> Unit) {
    CartaoDosPais {
        Text("🤝 Desafio do dia em família", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "${desafio.area.emoji} ${desafio.area.nome}",
            style = MaterialTheme.typography.titleMedium,
            color = Cores.Roxo,
        )
        Spacer(Modifier.height(6.dp))
        Text(desafio.texto, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
        if (feito) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .background(Cores.Certo.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
                    .testTag("desafio_feito"),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("✅", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Feito hoje! A criança ganhou ${Pais.MOEDAS_DO_DESAFIO} moedinhas 🪙. Amanhã tem um desafio novo.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Cores.Texto,
                )
            }
        } else {
            BotaoFofo(onClick = onFizemos, cor = Cores.Certo, modifier = Modifier.fillMaxWidth().testTag("fizemos")) {
                Text(
                    "✔ Fizemos!",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Depois de fazer juntos, toque em Fizemos: a criança ganha ${Pais.MOEDAS_DO_DESAFIO} moedinhas (uma vez por dia).",
                style = MaterialTheme.typography.bodyMedium,
                color = Cores.TextoSuave,
            )
        }
    }
}

/** Relatório da semana (segunda a domingo), só com o que ficou guardado no aparelho. */
@Composable
fun CartaoRelatorioDaSemana(relatorio: RelatorioSemanal) {
    CartaoDosPais {
        Text("📅 Resumo da semana", style = MaterialTheme.typography.titleLarge)
        Text(
            "De ${relatorio.inicio.curta()} a ${relatorio.fim.curta()}",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        Spacer(Modifier.height(10.dp))
        if (relatorio.vazio) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(LilasClaro, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🌱", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ainda não há partidas nesta semana. Quando a criança jogar, o resumo aparece aqui.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Numero("⏱️", "${relatorio.minutosJogados}", if (relatorio.minutosJogados == 1) "minuto" else "minutos", Modifier.weight(1f))
                Numero("📆", "${relatorio.diasJogados}", if (relatorio.diasJogados == 1) "dia" else "dias", Modifier.weight(1f))
                Numero("⭐", "${relatorio.fasesNovas}", if (relatorio.fasesNovas == 1) "fase nova" else "fases novas", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "${relatorio.partidas} ${if (relatorio.partidas == 1) "partida" else "partidas"}, " +
                    "${relatorio.fasesVencidas} ${if (relatorio.fasesVencidas == 1) "fase vencida" else "fases vencidas"}.",
                style = MaterialTheme.typography.bodyLarge,
            )
            val porCaminho = Trilha.entries.mapNotNull { t -> relatorio.minutosPorCaminho[t]?.let { t to it } }
            if (porCaminho.isNotEmpty()) {
                Text(
                    porCaminho.joinToString(" • ") { (t, m) -> "${t.emoji} ${t.titulo}: $m min" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cores.TextoSuave,
                )
            }
            if (relatorio.pontosFortes.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("💪 Pontos fortes", style = MaterialTheme.typography.titleMedium, color = Cores.Certo)
                for (h in relatorio.pontosFortes) LinhaHabilidade(h, Cores.Certo)
            }
            if (relatorio.paraReforcar.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("🌱 Para reforçar com calma", style = MaterialTheme.typography.titleMedium, color = Color(0xFFB7791F))
                for (h in relatorio.paraReforcar) LinhaHabilidade(h, Cores.Estrela)
            }
        }
        if (relatorio.sugestoes.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("💬 Ideias para conversar", style = MaterialTheme.typography.titleMedium)
            for (s in relatorio.sugestoes) {
                Text("• $s", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Este resumo fica só neste aparelho e guarda os últimos ${Pais.DIAS_GUARDADOS} dias.",
            style = MaterialTheme.typography.bodySmall,
            color = Cores.TextoSuave,
        )
    }
}

@Composable
private fun Numero(emoji: String, valor: String, rotulo: String, modifier: Modifier) {
    Column(
        modifier
            .background(LilasClaro, RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Text(valor, style = MaterialTheme.typography.headlineMedium, color = Cores.Roxo)
        Text(rotulo, style = MaterialTheme.typography.bodyMedium, color = Cores.TextoSuave, textAlign = TextAlign.Center)
    }
}

@Composable
private fun LinhaHabilidade(h: Habilidade, cor: Color) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(cor, RoundedCornerShape(50)))
        Spacer(Modifier.width(8.dp))
        Text("${h.trilha.emoji} ${h.nome}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text("${h.precisao}% de primeira", style = MaterialTheme.typography.bodyMedium, color = Cores.TextoSuave)
    }
}

/**
 * Aniversário da criança: dia e mês com botões grandes de menos e mais (sem teclado nem calendário pequeno).
 * O ano não é pedido. No dia, a casinha comemora.
 */
@Composable
fun CartaoAniversario(atual: Aniversario?, onSalvar: (Aniversario?) -> Unit, habilitado: Boolean = true) {
    var diaEscolhido by rememberSaveable(atual) { mutableIntStateOf(atual?.dia ?: 1) }
    var mes by rememberSaveable(atual) { mutableIntStateOf(atual?.mes ?: 1) }
    // 2024 é bissexto: 29 de fevereiro pode ser escolhido (nos outros anos a festa é no dia 28).
    val maximo = Calendario.diasNoMes(mes, 2024)
    val dia = diaEscolhido.coerceAtMost(maximo)
    val mudou = atual == null || atual.dia != dia || atual.mes != mes
    CartaoDosPais {
        Text("🎂 Aniversário da criança", style = MaterialTheme.typography.titleLarge)
        Text(
            if (atual == null) "Ainda não escolhido. No dia, a casinha faz festa e o narrador dá os parabéns."
            else "Guardado: ${atual.dia} de ${NomesDosMeses[atual.mes - 1]}.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        if (!habilitado) return@CartaoDosPais
        Spacer(Modifier.height(10.dp))
        Seletor(
            rotulo = "Dia",
            valor = "$dia",
            marca = "aniversario_dia",
            onMenos = { diaEscolhido = if (dia <= 1) maximo else dia - 1 },
            onMais = { diaEscolhido = if (dia >= maximo) 1 else dia + 1 },
        )
        Spacer(Modifier.height(10.dp))
        Seletor(
            rotulo = "Mês",
            valor = NomesDosMeses[mes - 1],
            marca = "aniversario_mes",
            onMenos = { mes = if (mes <= 1) 12 else mes - 1 },
            onMais = { mes = if (mes >= 12) 1 else mes + 1 },
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BotaoFofo(
                onClick = { onSalvar(Aniversario(dia, mes)) },
                cor = if (mudou) Cores.Roxo else Lilas,
                habilitado = mudou,
                modifier = Modifier.testTag("aniversario_salvar"),
            ) {
                Text(
                    if (mudou) "Salvar $dia de ${NomesDosMeses[mes - 1]}" else "Salvo ✔",
                    color = if (mudou) Color.White else Cores.Texto,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(14.dp),
                )
            }
            if (atual != null) {
                BotaoFofo(onClick = { onSalvar(null) }, cor = Color.White, corBase = Lilas, modifier = Modifier.testTag("aniversario_tirar")) {
                    Text("Tirar", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(14.dp))
                }
            }
        }
    }
}

@Composable
private fun Seletor(rotulo: String, valor: String, marca: String, onMenos: () -> Unit, onMais: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(rotulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(48.dp))
        BotaoFofo(onClick = onMenos, cor = Color.White, corBase = Lilas, descricao = "$rotulo: menos", modifier = Modifier.testTag("${marca}_menos")) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                Text("−", style = MaterialTheme.typography.headlineMedium)
            }
        }
        Box(Modifier.weight(1f).widthIn(min = 72.dp), contentAlignment = Alignment.Center) {
            Text(valor, style = MaterialTheme.typography.headlineSmall, color = Cores.Roxo, textAlign = TextAlign.Center)
        }
        BotaoFofo(onClick = onMais, cor = Color.White, corBase = Lilas, descricao = "$rotulo: mais", modifier = Modifier.testTag("${marca}_mais")) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

/** Até [TemaFavorito.MAXIMO] temas favoritos: as perguntas de contar e de palavras preferem figuras deles. */
@Composable
fun CartaoTemasFavoritos(escolhidos: List<TemaFavorito>, onAlternar: (TemaFavorito) -> Unit, habilitado: Boolean = true) {
    CartaoDosPais {
        Text("💜 Temas favoritos", style = MaterialTheme.typography.titleLarge)
        Text(
            "Escolha até ${TemaFavorito.MAXIMO}. Algumas perguntas passam a usar figuras desses temas, sem mudar a dificuldade. " +
                "Escolhidos: ${escolhidos.size} de ${TemaFavorito.MAXIMO}.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        if (!habilitado) return@CartaoDosPais
        Spacer(Modifier.height(10.dp))
        val cheio = escolhidos.size >= TemaFavorito.MAXIMO
        for (linha in TemaFavorito.entries.chunked(2)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                for (t in linha) {
                    val marcado = t in escolhidos
                    val apagado = cheio && !marcado
                    BotaoFofo(
                        onClick = { onAlternar(t) },
                        cor = if (marcado) Cores.Roxo else Color.White,
                        corBase = if (marcado) escurecerRoxo else Lilas,
                        habilitado = !apagado,
                        profundidade = 4.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tema_${t.name}")
                            .semantics { selected = marcado },
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .then(if (marcado) Modifier else Modifier.border(2.dp, Lilas, RoundedCornerShape(24.dp)))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(t.emoji, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                t.nome,
                                style = MaterialTheme.typography.titleSmall,
                                color = when {
                                    marcado -> Color.White
                                    apagado -> Cores.TextoSuave.copy(alpha = 0.6f)
                                    else -> Cores.Texto
                                },
                                modifier = Modifier.weight(1f),
                            )
                            if (marcado) Text("✔", color = Color.White, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                if (linha.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private val escurecerRoxo = Color(0xFF4B44C2)
