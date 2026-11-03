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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import br.com.casinhadosaber.ui.TOQUE_MINIMO
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
    /** Espelho 🪞: abre o perfil para trocar a aparência e volta para cá. */
    onEspelho: () -> Unit = {},
    /** Porta 🚪: entra na casa por dentro (isométrica). */
    onEntrar: () -> Unit = {},
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var pintando by remember { mutableStateOf(false) }
    var noite by remember { mutableStateOf(noiteInicial) }
    // Experimentar antes de comprar: o enfeite aparece na casa por alguns segundos.
    var previa by remember { mutableStateOf<Produto?>(null) }
    LaunchedEffect(previa) {
        if (previa != null) {
            delay(6000)
            previa = null
        }
    }
    val estadoNaCena = previa?.let { p -> estado.copy(moedas = Int.MAX_VALUE / 2).usar(p) } ?: estado
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
            sons.zum()
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

    /** O baú e a loja, embaixo da casa (em pé) ou numa coluna ao lado dela (deitada). */
    @Composable
    fun BauDaCasa(modificador: Modifier, lateral: Boolean) {
        Bau(
            modifier = modificador,
            lateral = lateral,
            itens = bau,
            estado = estado,
            lojaInicial = lojaInicial,
            onEscolher = ::colocarDoBau,
            onJogar = onJogar,
            previa = previa,
            onComprar = { produto ->
                val jaTinha = produto.id in estado.comprados
                val preco = Loja.preco(produto, estado)
                if (!jaTinha && previa?.id != produto.id) {
                    // Primeiro toque: só mostra como fica na casa.
                    previa = produto
                    sons.toque()
                    narrador.falar(
                        if (estado.moedas >= preco) {
                            "Olha como fica! Custa $preco moedinhas. Toque de novo para comprar."
                        } else {
                            "Olha como fica! Faltam ${preco - estado.moedas} moedinhas."
                        },
                    )
                    return@Bau
                }
                previa = null
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
                        sons.moeda()
                        if (produto.categoria == Categoria.VISITANTE) sons.piu(Loja.produtos.indexOf(produto))
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

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // Tela deitada (tablet em paisagem): a casa à esquerda e o baú numa coluna à direita.
        val deitada = maxWidth > maxHeight * 1.15f
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

            Row(Modifier.weight(1f).fillMaxWidth()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                CenaDaCasa(
                    estado = estadoNaCena,
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
                            // Não proíbe (casa engraçada também vale), só dá a dica: o cômodo certo brilha de leve na cena.
                            narrador.falar("${item.comArtigo.replaceFirstChar { it.uppercase() }} gosta mais de ficar ${preposicao(certo)}! Olha onde está brilhando.")
                        }
                    },
                    onRecusarItem = { item ->
                        sons.erro()
                        // A cena mostra o "não" (peça volta tremendo, balão 🚫 desenhado) e faz o lugar certo brilhar.
                        val quem = item.comArtigo.replaceFirstChar { it.uppercase() }
                        val certo = item.comodo
                        narrador.falar(
                            when {
                                item.lugar == Lugar.DENTRO && certo != null -> "Aí não! $quem mora dentro de casa, ${preposicao(certo)}. Olha onde está brilhando!"
                                item.lugar == Lugar.DENTRO -> "Aí não! $quem mora dentro de casa. Olha onde está brilhando!"
                                else -> "Aí não! $quem mora lá fora, no jardim. Olha onde está brilhando!"
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
                // Espelho: trocar a aparência a qualquer momento.
                BotaoRedondo(
                    "🪞",
                    "Espelho: trocar a aparência",
                    { sons.toque(); onEspelho() },
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    tamanho = 52.dp,
                )
                // Entrar na casa: só quando a casa já tem porta.
                if (estado.temEstrutura(Estrutura.PORTA) && !pintando) {
                    BotaoFofo(
                        onClick = {
                            sons.toque()
                            narrador.falar("Vamos entrar na casa!")
                            onEntrar()
                        },
                        cor = Color(0xFFFFF3C4),
                        corBase = Color(0xFFE0A800),
                        descricao = "Entrar na casa",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .pulsando(),
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🚪", fontSize = 34.sp)
                            Text("👉", fontSize = 26.sp)
                        }
                    }
                }
            }
            if (deitada) {
                BauDaCasa(Modifier.width(LARGURA_DO_BAU_LATERAL).fillMaxHeight().padding(end = 8.dp, bottom = 8.dp), lateral = true)
            }
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

            if (!deitada) BauDaCasa(Modifier.fillMaxWidth(), lateral = false)
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

/** Largura da coluna do baú quando a tela está deitada (3 cartões por linha). */
private val LARGURA_DO_BAU_LATERAL = 344.dp

@Composable
private fun Bau(
    itens: List<ItemCasa>,
    estado: EstadoJogo,
    modifier: Modifier = Modifier,
    /** Coluna ao lado da casa (paisagem): as listas viram grades que rolam para baixo. */
    lateral: Boolean = false,
    lojaInicial: Boolean = false,
    previa: Produto? = null,
    onEscolher: (ItemCasa) -> Unit,
    onJogar: () -> Unit,
    onComprar: (Produto) -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var naLoja by remember { mutableStateOf(lojaInicial) }
    val forma = if (lateral) RoundedCornerShape(28.dp) else RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Column(
        modifier
            .background(Brush.verticalGradient(listOf(Cores.MadeiraClara, Cores.Madeira)), forma)
            .border(4.dp, Color(0xFF8E5A34), forma)
            .padding(top = 8.dp, bottom = 14.dp),
    ) {
        // Abas: baú de presentes e lojinha. Cada toque também diz em voz alta o que a aba é.
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Aba(
                "🎁" + if (itens.isNotEmpty()) " ${itens.size}" else "",
                if (itens.isEmpty()) "Baú de presentes, vazio" else "Baú de presentes, ${itens.size} para colocar na casa",
                selecionada = !naLoja,
            ) {
                naLoja = false
                sons.toque()
                narrador.falar(
                    when (itens.size) {
                        0 -> "Baú de presentes. Está vazio. Jogue para ganhar mais coisas!"
                        1 -> "Baú de presentes. Tem um presente esperando. Toque nele para colocar na casa!"
                        else -> "Baú de presentes. Tem ${itens.size} presentes esperando. Toque num deles para colocar na casa!"
                    },
                )
            }
            Aba("🛒 🪙 ${estado.moedas}", "Lojinha, você tem ${estado.moedas} moedinhas", selecionada = naLoja) {
                naLoja = true
                sons.toque()
                narrador.falar("Lojinha! Você tem ${estado.moedas} moedinhas. Toque numa coisa para ver como fica na casa.")
            }
        }
        Spacer(Modifier.height(8.dp))
        if (naLoja) {
            val vitrine = Loja.vitrine(estado, Loja.categoriasDeFora)
            if (lateral) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(vitrine, key = { it.id }) { produto -> CartaoDaLoja(produto, estado, previa, onComprar) }
                }
            } else {
                Box {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(vitrine, key = { it.id }) { produto -> CartaoDaLoja(produto, estado, previa, onComprar) }
                    }
                    // Esmaecido na borda direita: mostra que dá para arrastar e ver mais.
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .width(36.dp)
                            .height(110.dp)
                            .background(Brush.horizontalGradient(listOf(Color.Transparent, Cores.Madeira))),
                    )
                }
            }
        } else if (itens.isEmpty()) {
            val proximo = ItensDaCasa.ordem.getOrNull(estado.itensGanhos.size)
            val conquista: @Composable () -> Unit = {
                if (proximo != null) {
                    // O próximo presente aparece "apagadinho", com cadeado: sempre há um objetivo à vista.
                    // Tocar nele conta o que é e como ganhar.
                    Box(
                        Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x33FFFFFF))
                            .semantics { contentDescription = "Próximo presente: ${proximo.nome}. Jogue para ganhar." }
                            .clickable {
                                sons.toque()
                                narrador.falar("O próximo presente é ${proximo.comArtigo}! Jogue para ganhar.")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        IconeDoItem(proximo, 56.dp, Modifier.graphicsLayer { alpha = 0.35f })
                        Text("🔒", fontSize = 24.sp, modifier = Modifier.align(Alignment.BottomEnd))
                    }
                } else {
                    // Casa completa: o troféu é um botão que comemora (sozinho ele não dizia nada).
                    BotaoFofo(
                        onClick = {
                            sons.acerto()
                            narrador.falar(
                                "Você ganhou todas as ${ItensDaCasa.total} coisas! A casa está completinha. " +
                                    "Agora cada fase nova dá um presente da lojinha!",
                            )
                        },
                        cor = Color(0xFFFFF3C4),
                        corBase = Color(0xFFE0A800),
                        descricao = "Casa completa, ${ItensDaCasa.total} de ${ItensDaCasa.total} coisas",
                    ) {
                        Column(
                            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text("🏆", fontSize = 40.sp)
                            Text("🏠 ✔", style = MaterialTheme.typography.titleMedium, color = Cores.Certo)
                        }
                    }
                }
            }
            val jogar: @Composable () -> Unit = {
                BotaoFofo(onClick = onJogar, cor = Cores.Certo, descricao = "Jogar", modifier = Modifier.pulsando()) {
                    Text(
                        "🎮 Jogar",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    )
                }
            }
            if (lateral) {
                Column(
                    Modifier.fillMaxWidth().weight(1f).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                ) {
                    conquista()
                    jogar()
                }
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    conquista()
                    Spacer(Modifier.weight(1f))
                    jogar()
                }
            }
        } else if (lateral) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(itens, key = { it.id }) { item ->
                    ItemDoBau(item, primeiro = item == itens.first()) { onEscolher(item) }
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

/** Um produto da lojinha: miniatura e preço (ou "é seu"). */
@Composable
private fun CartaoDaLoja(produto: Produto, estado: EstadoJogo, previa: Produto?, onComprar: (Produto) -> Unit) {
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
            MiniaturaDoProduto(produto, Modifier.size(52.dp))
            Text(
                when {
                    usando -> "✔ usando"
                    tem -> "✔ é seu"
                    previa?.id == produto.id -> "👆 🪙 $preco"
                    else -> "🪙 $preco"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (tem) Cores.Certo else Cores.Texto,
            )
        }
    }
}

/** Desenho pequeno do produto: telhado ou porta na cor certa, amostra do papel de parede, ou o emoji. */
@Composable
private fun MiniaturaDoProduto(p: Produto, modifier: Modifier) {
    when (p.categoria) {
        Categoria.TELHADO -> androidx.compose.foundation.Canvas(modifier) {
            val cor = Color(Loja.coresDoTelhado[p.valor.toInt()])
            drawRect(Color(0xFFFFE9C7), androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.5f), androidx.compose.ui.geometry.Size(size.width * 0.6f, size.height * 0.45f))
            val tri = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.05f, size.height * 0.55f); lineTo(size.width * 0.5f, size.height * 0.1f); lineTo(size.width * 0.95f, size.height * 0.55f); close()
            }
            drawPath(tri, cor)
        }
        Categoria.PORTA -> androidx.compose.foundation.Canvas(modifier) {
            val cor = Color(Loja.coresDaPorta[p.valor.toInt()])
            drawRoundRect(cor, androidx.compose.ui.geometry.Offset(size.width * 0.25f, size.height * 0.1f), androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.85f), androidx.compose.ui.geometry.CornerRadius(size.width * 0.25f))
            drawCircle(Color(0xFFFFD166), size.width * 0.05f, androidx.compose.ui.geometry.Offset(size.width * 0.65f, size.height * 0.55f))
        }
        Categoria.PAREDE -> androidx.compose.foundation.Canvas(modifier) {
            drawRoundRect(Color(0xFFFFF1C9), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f))
            val passo = size.width / 5
            for (i in 0 until 5) for (j in 0 until 5) {
                val c = androidx.compose.ui.geometry.Offset(passo * (i + 0.5f), passo * (j + 0.5f))
                when (p.valor) {
                    "1" -> if (i % 2 == 0) drawRect(Color(0x33000000), androidx.compose.ui.geometry.Offset(passo * i, 0f), androidx.compose.ui.geometry.Size(passo / 2, size.height))
                    "2" -> drawCircle(Color(0x33000000), passo * 0.2f, c)
                    else -> if ((i + j) % 2 == 0) drawPath(br.com.casinhadosaber.ui.componentes.caminhoEstrela(c, passo * 0.35f), Color(0x66FFC93C))
                }
            }
        }
        else -> Text(p.icone, fontSize = 34.sp, maxLines = 1, modifier = modifier.wrapContentSize())
    }
}

@Composable
private fun Aba(texto: String, descricao: String, selecionada: Boolean, onClick: () -> Unit) {
    Text(
        texto,
        style = MaterialTheme.typography.titleLarge,
        color = Cores.Texto,
        modifier = Modifier
            .heightIn(min = TOQUE_MINIMO)
            .clip(RoundedCornerShape(50))
            .background(if (selecionada) Cores.Estrela else Color.White.copy(alpha = 0.4f))
            .border(if (selecionada) 3.dp else 0.dp, if (selecionada) Color(0xFFE0A800) else Color.Transparent, RoundedCornerShape(50))
            .semantics { contentDescription = descricao }
            .clickable(onClick = onClick)
            .wrapContentHeight(Alignment.CenterVertically)
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
