package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Produto
import br.com.casinhadosaber.dominio.Categoria
import br.com.casinhadosaber.ui.componentes.pulsando
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
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
import br.com.casinhadosaber.ui.componentes.comodoEm
import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.Lugar
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.IconeDoItem
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.tema.Cores

@Composable
fun TelaCasa(
    estado: EstadoJogo,
    onAtualizar: ((EstadoJogo) -> EstadoJogo) -> Unit,
    onVoltar: () -> Unit,
    onJogar: () -> Unit,
    noiteInicial: Boolean = false,
    /** Abre direto na aba da loja (usado nas capturas de tela). */
    lojaInicial: Boolean = false,
    /** Item recém ganho: entra na casa sozinho, logo que a tela abre. */
    colocarAoAbrir: String? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var pintando by remember { mutableStateOf(false) }
    var noite by remember { mutableStateOf(noiteInicial) }
    var novaEstrutura by remember { mutableStateOf<Estrutura?>(null) }
    var destaque by remember { mutableStateOf<String?>(null) }
    var pulo by remember { mutableIntStateOf(0) }
    var confete by remember { mutableIntStateOf(0) }
    val nome = estado.perfil?.nome.orEmpty()
    val bau = estado.itensNoBau
    val casaCompleta = estado.colocados.size == ItensDaCasa.total
    val vibracao = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        if (colocarAoAbrir != null) return@LaunchedEffect
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
        vibracao.performHapticFeedback(HapticFeedbackType.LongPress)
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

    LaunchedEffect(colocarAoAbrir) {
        val item = colocarAoAbrir?.let { ItensDaCasa.porId[it] } ?: return@LaunchedEffect
        if (item.id !in estado.colocados) {
            delay(500)
            colocarDoBau(item)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
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
                BotaoRedondo(
                    emoji = if (noite) "☀️" else "🌙",
                    descricao = if (noite) "Virar dia" else "Virar noite",
                    onClick = {
                        noite = !noite
                        sons.colocar()
                        narrador.falar(if (noite) listOf(pt("Boa noite!"), en("Good night!")) else listOf(pt("Bom dia!"), en("Good morning!")))
                    },
                    cor = if (noite) Color(0xFF3B4C8C) else Color.White,
                    tamanho = 52.dp,
                )
                Spacer(Modifier.width(8.dp))
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
                contentAlignment = Alignment.BottomCenter,
            ) {
                CenaDaCasa(
                    estado = estado,
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(6.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp)),
                    preencherAltura = true,
                    interativa = true,
                    pintando = pintando,
                    novaEstrutura = novaEstrutura,
                    destaqueId = destaque,
                    puloDaCrianca = pulo,
                    noite = noite,
                    onTocarItem = { item ->
                        sons.toque()
                        narrador.falar(listOf(pt("${item.nome}!"), en(item.nomeEn)))
                    },
                    onMoverItem = { item, pos ->
                        onAtualizar { it.colocar(item, pos) }
                        // Elogio de arrumação: o móvel foi para o cômodo dele.
                        val comodo = comodoEm(pos.x, pos.y - 0.02f)
                        val certo = item.comodo
                        if (comodo != null && comodo == certo) {
                            sons.acerto()
                            narrador.falar("Isso! ${item.comArtigo.replaceFirstChar { it.uppercase() }} fica ${preposicao(comodo)}!")
                        } else if (comodo != null && certo != null) {
                            // Não proíbe (casa engraçada também vale), só dá a dica.
                            narrador.falar("${item.comArtigo.replaceFirstChar { it.uppercase() }} gosta mais de ficar ${preposicao(certo)}!")
                        }
                    },
                    onRecusarItem = { item ->
                        sons.erro()
                        narrador.falar(
                            if (item.lugar == Lugar.DENTRO) {
                                "${item.comArtigo.replaceFirstChar { it.uppercase() }} mora dentro de casa!"
                            } else {
                                "${item.comArtigo.replaceFirstChar { it.uppercase() }} mora lá fora!"
                            },
                        )
                    },
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

            if (pintando) {
                Text(
                    "🎨 👆 🏠",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                )
            }

            Bau(
                itens = bau,
                estado = estado,
                lojaInicial = lojaInicial,
                onEscolher = ::colocarDoBau,
                onJogar = onJogar,
                onComprar = { produto ->
                    val jaTinha = produto.id in estado.comprados
                    val novo = estado.usar(produto)
                    if (novo == null) {
                        sons.erro()
                        val faltam = Loja.preco(produto, estado) - estado.moedas
                        narrador.falar(
                            if (faltam == 1) "Falta só uma moedinha! Jogue mais um pouquinho." else "Faltam $faltam moedinhas! Cada acerto de primeira vale moedinha.",
                        )
                    } else {
                        onAtualizar { it.usar(produto) ?: it }
                        if (!jaTinha) {
                            confete++
                            sons.premio()
                            narrador.falar(
                                if (produto.categoria == Categoria.VISITANTE) "${produto.nome} veio passear no seu jardim!" else "Oba! ${produto.nome}!",
                            )
                        } else {
                            sons.colocar()
                        }
                    }
                },
            )
        }
        Confete(confete)
    }
}

private fun preposicao(c: Comodo) = when (c) {
    Comodo.QUARTO -> "no quarto"
    Comodo.BANHEIRO -> "no banheiro"
    Comodo.SALA -> "na sala"
    Comodo.COZINHA -> "na cozinha"
}

@Composable
private fun Bau(
    itens: List<ItemCasa>,
    estado: EstadoJogo,
    lojaInicial: Boolean = false,
    onEscolher: (ItemCasa) -> Unit,
    onJogar: () -> Unit,
    onComprar: (Produto) -> Unit,
) {
    var naLoja by remember { mutableStateOf(lojaInicial) }
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Cores.MadeiraClara, Cores.Madeira)),
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            )
            .border(4.dp, Color(0xFF8E5A34), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(top = 8.dp, bottom = 14.dp),
    ) {
        // Abas: baú de presentes e loja de estrelas.
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Aba("🧺" + if (itens.isNotEmpty()) " ${itens.size}" else "", "Baú de presentes", selecionada = !naLoja) { naLoja = false }
            Aba("🪙 ${estado.moedas}", "Loja, você tem ${estado.moedas} moedinhas", selecionada = naLoja) { naLoja = true }
        }
        Spacer(Modifier.height(8.dp))
        if (naLoja) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(Loja.vitrine(estado), key = { it.id }) { produto ->
                    val tem = produto.id in estado.comprados
                    val preco = Loja.preco(produto, estado)
                    val podeComprar = tem || estado.moedas >= preco
                    val usando = Loja.emUso(produto, estado)
                    BotaoFofo(
                        onClick = { onComprar(produto) },
                        cor = if (usando) Color(0xFFDFF7E6) else Color.White,
                        corBase = Color(0xFF8E5A34),
                        descricao = produto.nome + if (tem) ", é seu" else ", $preco moedinhas",
                        modifier = Modifier.graphicsLayer { alpha = if (podeComprar) 1f else 0.5f },
                    ) {
                        Column(Modifier.width(92.dp).padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(produto.icone, fontSize = 34.sp, maxLines = 1)
                            Text(
                                when {
                                    usando -> "✔"
                                    tem -> "♻️"
                                    else -> "🪙 $preco"
                                },
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        } else if (itens.isEmpty()) {
            val proximo = ItensDaCasa.ordem.getOrNull(estado.itensGanhos.size)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (proximo != null) {
                    // O próximo presente aparece "apagadinho", com cadeado: sempre há um objetivo à vista.
                    Box(Modifier.size(80.dp).background(Color(0x33FFFFFF), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                        IconeDoItem(proximo, 56.dp, Modifier.graphicsLayer { alpha = 0.35f })
                        Text("🔒", fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomEnd))
                    }
                } else {
                    Text("🏆", fontSize = 48.sp)
                }
                Spacer(Modifier.weight(1f))
                BotaoFofo(onClick = onJogar, cor = Cores.Certo, descricao = "Jogar", modifier = Modifier.pulsando()) {
                    Text(
                        "🎮 Jogar",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
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
private fun Aba(texto: String, descricao: String, selecionada: Boolean, onClick: () -> Unit) {
    Text(
        texto,
        style = MaterialTheme.typography.titleLarge,
        color = Cores.Texto,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selecionada) Color.White else Color(0xFFFFD27A))
            .semantics { contentDescription = descricao }
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 6.dp),
    )
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
                if (primeiro) MaoGuia(Modifier.align(Alignment.BottomStart).offset(x = (-6).dp, y = 22.dp), espera = 1500)
            }
            Text(item.nome, style = MaterialTheme.typography.bodyMedium, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}
