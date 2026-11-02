package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Estrutura
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.IconeDoItem
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

@Composable
fun TelaCasa(
    estado: EstadoJogo,
    onAtualizar: ((EstadoJogo) -> EstadoJogo) -> Unit,
    onVoltar: () -> Unit,
    onJogar: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var pintando by remember { mutableStateOf(false) }
    var novaEstrutura by remember { mutableStateOf<Estrutura?>(null) }
    var destaque by remember { mutableStateOf<String?>(null) }
    var pulo by remember { mutableIntStateOf(0) }
    var confete by remember { mutableIntStateOf(0) }
    val nome = estado.perfil?.nome.orEmpty()
    val bau = estado.itensNoBau
    val casaCompleta = estado.colocados.size == ItensDaCasa.total

    LaunchedEffect(Unit) {
        narrador.falar(
            when {
                bau.isNotEmpty() -> "Toque num presente do baú para colocar na casa!"
                estado.colocados.isEmpty() -> "Jogue nos caminhos para ganhar coisas para a casa!"
                else -> "Arraste as coisas com o dedo para arrumar a casa do seu jeito!"
            },
        )
    }

    fun colocarDoBau(item: ItemCasa) {
        pintando = false
        onAtualizar { it.colocar(item) }
        if (item.ehEstrutura) {
            novaEstrutura = item.estrutura
            confete++
            sons.premio()
            narrador.falar(listOf(pt("Construímos ${item.comArtigo}! Em inglês:"), en(item.nomeEn)))
        } else {
            destaque = item.id
            sons.colocar()
            val primeiroObjeto = estado.colocados.keys.none { ItensDaCasa.porId[it]?.ehEstrutura == false }
            narrador.falar(
                listOf(pt("${item.nome}! Em inglês:"), en(item.nomeEn)) +
                    if (primeiroObjeto) listOf(pt("Você pode arrastar com o dedo para onde quiser!")) else emptyList(),
            )
        }
        pulo++
        if (estado.colocados.size + 1 == ItensDaCasa.total) {
            confete++
            narrador.falar("Uau! A casa está completinha! Parabéns!")
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo))),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BotaoRedondo("⬅️", "Voltar", onVoltar, tamanho = 52.dp)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (nome.isBlank()) "Minha casinha" else "Casa de $nome",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        maxLines = 1,
                    )
                    Text(
                        "${estado.colocados.size} de ${ItensDaCasa.total} coisas",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
                if (estado.temEstrutura(Estrutura.PAREDES)) {
                    BotaoRedondo(
                        emoji = "🎨",
                        descricao = if (pintando) "Parar de pintar" else "Pintar as paredes",
                        onClick = {
                            pintando = !pintando
                            sons.toque()
                            if (pintando) narrador.falar("Toque num cômodo para trocar a cor da parede!")
                        },
                        cor = if (pintando) Cores.Estrela else Color.White,
                        tamanho = 52.dp,
                    )
                } else {
                    Spacer(Modifier.size(52.dp))
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                CenaDaCasa(
                    estado = estado,
                    modifier = Modifier
                        .fillMaxHeight()
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp)),
                    interativa = true,
                    pintando = pintando,
                    novaEstrutura = novaEstrutura,
                    destaqueId = destaque,
                    puloDaCrianca = pulo,
                    onTocarItem = { item ->
                        sons.toque()
                        narrador.falar(listOf(pt("${item.nome}!"), en(item.nomeEn)))
                    },
                    onMoverItem = { item, pos -> onAtualizar { it.colocar(item, pos) } },
                    onGuardarItem = { item ->
                        onAtualizar { it.guardar(item) }
                        sons.toque()
                        narrador.falar("Guardei no baú.")
                    },
                    onPintar = { comodo ->
                        onAtualizar { it.pintar(comodo) }
                        sons.colocar()
                        narrador.falar(comodo.nome)
                    },
                    onTocarCrianca = {
                        pulo++
                        val frases = listOf(
                            if (nome.isBlank()) "Oi! Que casa linda!" else "Oi! Eu sou $nome! Que casa linda!",
                            "Eu adoro a minha casinha!",
                            "Vamos jogar para ganhar mais coisas?",
                            "Iupiii!",
                        )
                        narrador.falar(frases.random())
                    },
                    onMoverCrianca = { pos: Posicao -> onAtualizar { it.copy(posicaoCrianca = pos) } },
                )
            }

            Text(
                when {
                    pintando -> "🎨 Toque num cômodo para pintar"
                    casaCompleta -> "🏆 Casa completa! Arraste as coisas para arrumar"
                    bau.isNotEmpty() -> "👇 Toque num presente do baú"
                    else -> "✋ Arraste para arrumar • Leve para o baú para guardar"
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )

            Bau(bau, onEscolher = ::colocarDoBau, onJogar = onJogar)
        }
        Confete(confete)
    }
}

@Composable
private fun Bau(itens: List<ItemCasa>, onEscolher: (ItemCasa) -> Unit, onJogar: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Cores.MadeiraClara, Cores.Madeira)),
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            )
            .border(4.dp, Color(0xFF8E5A34), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(top = 10.dp, bottom = 14.dp),
    ) {
        Text(
            "🧺 Baú de presentes" + if (itens.isNotEmpty()) " (${itens.size})" else "",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 18.dp),
        )
        Spacer(Modifier.height(8.dp))
        if (itens.isEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Vazio! Jogue para ganhar mais coisas.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.weight(1f),
                )
                BotaoFofo(onClick = onJogar, cor = Cores.Certo, descricao = "Jogar") {
                    Text(
                        "🎮 Jogar",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    )
                }
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(itens, key = { it.id }) { item ->
                    ItemDoBau(item, primeiro = item == itens.first()) { onEscolher(item) }
                }
            }
        }
    }
}

@Composable
private fun ItemDoBau(item: ItemCasa, primeiro: Boolean, onClick: () -> Unit) {
    BotaoFofo(
        onClick = onClick,
        cor = Color.White,
        corBase = Color(0xFF8E5A34),
        descricao = "${item.nome}. Toque para colocar na casa.",
        modifier = if (primeiro) Modifier.pulsando() else Modifier,
    ) {
        Column(
            Modifier
                .width(92.dp)
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                IconeDoItem(item, 60.dp)
                if (item.ehEstrutura) {
                    Text("🔨", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.BottomEnd))
                }
            }
            Text(item.nome, style = MaterialTheme.typography.bodyMedium, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}
