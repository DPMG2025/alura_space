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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
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
import br.com.casinhadosaber.dominio.Crescimento
import br.com.casinhadosaber.dominio.Data
import br.com.casinhadosaber.dominio.Eventos
import br.com.casinhadosaber.dominio.PresenteEspecial
import br.com.casinhadosaber.dominio.Surpresas
import br.com.casinhadosaber.dominio.escolherMascote
import br.com.casinhadosaber.dominio.nomearMascote
import br.com.casinhadosaber.dominio.presentesGanhos
import br.com.casinhadosaber.dominio.receberPresenteDoEvento
import br.com.casinhadosaber.dominio.vestirMascote
import br.com.casinhadosaber.ui.componentes.MascoteAnimado
import br.com.casinhadosaber.ui.componentes.PrateleiraDePresentes
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import br.com.casinhadosaber.dominio.AreaDaCasa
import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.ui.componentes.CenaDaArea
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.EstiloDaPilula
import br.com.casinhadosaber.ui.componentes.EstrelasDeProgresso
import br.com.casinhadosaber.ui.componentes.FiguraDaPergunta
import br.com.casinhadosaber.ui.componentes.IconeDaCoisa
import br.com.casinhadosaber.ui.componentes.IconeDeArteDaBarra
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.Pilula
import br.com.casinhadosaber.ui.componentes.ProgressoVisivel
import br.com.casinhadosaber.ui.componentes.TextoComContorno
import br.com.casinhadosaber.ui.componentes.miniaturaVestindo
import br.com.casinhadosaber.ui.componentes.pilula
import br.com.casinhadosaber.dominio.Perfil
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import kotlinx.coroutines.launch

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
    /**
     * Data de hoje (`repo.hojeData()`): liga os enfeites do evento do dia, o presente do evento e o visitante
     * da semana (que também entra na lojinha). Null (capturas antigas) = dia comum, sem visitante.
     */
    hoje: Data? = null,
    /** Pergunta qual mascote a criança quer, se ela ainda não escolheu (o app liga; as capturas antigas não). */
    perguntarMascote: Boolean = false,
    /** Abre mostrando uma área nova da casa que cresce (id de [CasaQueCresce.areas]); usado nas capturas. */
    paginaInicial: String? = null,
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
    // Eventos do ano e surpresa da semana (sugestão 4): tudo pela data recebida, nada lê o relógio aqui.
    val evento = remember(hoje, estado.perfil?.aniversario) { hoje?.let { Eventos.principal(it, estado.perfil?.aniversario) } }
    val visitaDaSemana = remember(hoje) { hoje?.let { Surpresas.daSemana(it) }?.visitante }
    val novidadesDaLoja = remember(hoje) { hoje?.let { Surpresas.liberadas(it) }.orEmpty() }
    var presenteDoEvento by remember { mutableStateOf<PresenteEspecial?>(null) }
    // Mascote (sugestão 2): a escolha aparece uma vez, ao abrir a casa sem um presente chegando.
    var escolhendoMascote by remember { mutableStateOf(perguntarMascote && !estado.mascote.escolhido && colocarAoAbrir == null) }
    var noCantinho by remember { mutableStateOf(false) }
    val estadoNaCena = (previa?.let { p -> estado.copy(moedas = Int.MAX_VALUE / 2).usar(p) } ?: estado).let { e ->
        // O visitante da semana passeia de graça no jardim, junto com os comprados.
        visitaDaSemana?.let { v -> e.copy(visitantes = e.visitantes + v.valor) } ?: e
    }
    var novaEstrutura by remember { mutableStateOf<Estrutura?>(null) }
    var destaque by remember { mutableStateOf<String?>(null) }
    var pulo by remember { mutableIntStateOf(0) }
    // Vez da fala do irmãozinho quando a criança toca nele (as falas giram, sem sorteio).
    var falasDoIrmao by remember { mutableIntStateOf(0) }
    var confete by remember { mutableIntStateOf(0) }
    val nome = estado.perfil?.nome.orEmpty()
    val vibracao = LocalHapticFeedback.current
    // Casa que cresce: cada área construída vira uma página ao lado da casa (arrastar o fundo para o lado).
    val areasProntas = CasaQueCresce.construidas(estado)
    val pager = rememberPagerState(initialPage = (areasProntas.indexOfFirst { it.id == paginaInicial } + 1).coerceAtLeast(0)) { 1 + areasProntas.size }
    val escopo = rememberCoroutineScope()
    val areaNaTela: AreaDaCasa? = if (areasProntas.isEmpty()) null else areasProntas.getOrNull(pager.currentPage - 1)
    var areaNova by remember { mutableStateOf<String?>(null) }
    var irParaArea by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(irParaArea, areasProntas.size) {
        val alvo = irParaArea ?: return@LaunchedEffect
        val i = areasProntas.indexOfFirst { it.id == alvo }
        if (i >= 0) {
            pager.animateScrollToPage(i + 1)
            irParaArea = null
        }
    }
    // O baú mostra primeiro as coisas da área que está na tela.
    val bau = estado.itensNoBau.let { b -> if (areaNaTela == null) b else b.sortedBy { if (it.area == areaNaTela.id) 0 else 1 } }
    val pecasDaCasa = estado.colocados.keys.count { id -> ItensDaCasa.porId[id]?.area == null }

    // Presente do evento: entregue uma vez por evento e ano, com a saudação do narrador.
    LaunchedEffect(evento, escolhendoMascote) {
        val ev = evento ?: return@LaunchedEffect
        if (escolhendoMascote) return@LaunchedEffect
        val presente = estado.receberPresenteDoEvento(ev).second ?: return@LaunchedEffect
        onAtualizar { it.receberPresenteDoEvento(ev).first }
        delay(1200)
        presenteDoEvento = presente
        confete++
        sons.premio()
        narrador.falar("${ev.saudacao(nome.ifBlank { null })} Você ganhou: ${presente.nome}!")
        delay(9000)
        presenteDoEvento = null
    }

    LaunchedEffect(Unit) {
        if (colocarAoAbrir != null || escolhendoMascote) return@LaunchedEffect
        val obraEsperando = bau.firstOrNull { it.obra }
        narrador.falar(
            when {
                obraEsperando != null -> "A sua casa vai crescer! Toque no martelinho do baú para construir ${obraEsperando.comArtigo}!"
                bau.isNotEmpty() -> "Toque num presente do baú para colocar na casa!"
                estado.colocados.isEmpty() -> "Jogue nos caminhos para ganhar coisas para a casa!"
                else -> "Arraste as coisas com o dedo para arrumar a casa do seu jeito!"
            },
        )
    }

    /** Toque numa coisa da casa ou de uma área: diz o nome; os bichinhos falam do jeito deles. */
    fun tocarNoItem(item: ItemCasa) {
        when (CasaQueCresce.tipoDe(item.id)) {
            "cachorro", "poodle" -> {
                sons.piu(2)
                narrador.falar(listOf(pt("Au au! ${item.nome}! Em inglês:"), en("Woof woof! ${item.nomeEn}")))
            }
            "gato" -> {
                sons.piu(3)
                narrador.falar(listOf(pt("Miau! ${item.nome}! Em inglês:"), en("Meow! ${item.nomeEn}")))
            }
            else -> {
                sons.toque()
                narrador.falar(listOf(pt("${item.nome}!"), en(item.nomeEn)))
            }
        }
    }

    fun colocarDoBau(item: ItemCasa) {
        val area = item.area?.let { CasaQueCresce.porId[it] }
        // Coisa de uma área que ainda não foi construída: primeiro a obra.
        if (area != null && !item.obra && area.obra.id !in estado.colocados) {
            colocarDoBau(area.obra)
            return
        }
        pintando = false
        vibracao.performHapticFeedback(HapticFeedbackType.LongPress)
        onAtualizar { it.colocar(item) }
        if (area != null) {
            irParaArea = area.id
            if (item.obra) {
                areaNova = area.id
                confete++
                sons.zum()
                narrador.falar(listOf(pt("A casa cresceu! Construímos ${item.comArtigo}! Em inglês:"), en(item.nomeEn)))
            } else {
                destaque = item.id
                sons.colocar()
                narrador.falar(listOf(pt("${item.nome}! Em inglês:"), en(item.nomeEn)))
                val p = CasaQueCresce.progresso(estado, area)
                if (p.colocadas + 1 == p.total) {
                    confete++
                    narrador.falar("Uau! Você completou ${area.comArtigo}! E a casa vai crescer mais!")
                }
            }
            pulo++
            return
        }
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
        if (pecasDaCasa + 1 == ItensDaCasa.totalDaCasa) {
            confete++
            narrador.falar("Uau! A casa está completinha! Parabéns! E ela ainda vai crescer!")
        }
    }

    LaunchedEffect(colocarAoAbrir) {
        val item = colocarAoAbrir?.let { ItensDaCasa.porId[it] } ?: return@LaunchedEffect
        if (item.id !in estado.colocados) {
            delay(500)
            colocarDoBau(item)
        }
    }

    /**
     * Deitada, a coluna do baú também reúne os atalhos grandes (espelho, pintar, entrar e jogar):
     * eles saem de cima da cena e do cabeçalho, para não aparecerem duas vezes.
     */
    val atalhosDaColuna: @Composable () -> Unit = {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AtalhoGrande("🪞", "Espelho", "Espelho: trocar a aparência", Modifier.weight(1f)) {
                    sons.toque()
                    onEspelho()
                }
                if (estado.temEstrutura(Estrutura.PAREDES)) {
                    AtalhoGrande(
                        "🎨",
                        if (pintando) "Pronto" else "Pintar",
                        if (pintando) "Parar de pintar" else "Pintar as paredes",
                        Modifier.weight(1f),
                        cor = if (pintando) Cores.Estrela else Color.White,
                    ) {
                        pintando = !pintando
                        sons.toque()
                        if (pintando) narrador.falar("Toque num cômodo para trocar a cor da parede!")
                    }
                }
                if (estado.temEstrutura(Estrutura.PORTA)) {
                    AtalhoGrande(
                        "🏠",
                        "Entrar",
                        "Entrar na casa",
                        Modifier.weight(1f),
                        cor = Color(0xFFFFF3C4),
                        corBase = Color(0xFFE0A800),
                    ) {
                        pintando = false
                        sons.toque()
                        narrador.falar("Vamos entrar na casa!")
                        onEntrar()
                    }
                }
            }
            BotaoFofo(
                onClick = onJogar,
                cor = Cores.Certo,
                descricao = "Jogar",
                // Pulsa só quando não há presente esperando no baú (aí o presente é a vez dele).
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (bau.isEmpty()) Modifier.pulsando() else Modifier),
            ) {
                RotuloDoJogar(Modifier.fillMaxWidth().padding(vertical = 10.dp))
            }
        }
    }

    /** O baú e a loja, embaixo da casa (em pé) ou numa coluna ao lado dela (deitada). */
    @Composable
    fun BauDaCasa(modificador: Modifier, lateral: Boolean, comAtalhos: Boolean = false) {
        Bau(
            modifier = modificador,
            lateral = lateral,
            atalhosLaterais = if (lateral && comAtalhos) atalhosDaColuna else null,
            itens = bau,
            estado = estado,
            lojaInicial = lojaInicial,
            extras = novidadesDaLoja,
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
            .background(CeuDaCasa)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // Tela deitada (tablet em paisagem): a casa à esquerda e o baú numa coluna à direita.
        val deitada = maxWidth > maxHeight * 1.15f
        // Com altura de sobra (tablet deitado), a coluna ganha os atalhos grandes e eles saem da cena e do
        // cabeçalho. No celular deitado não cabem: tudo fica como antes.
        val atalhosNaColuna = deitada && maxHeight >= ALTURA_PARA_ATALHOS
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BotaoRedondo("⬅️", "Voltar", onVoltar, tamanho = 52.dp)
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Título branco com contorno (o nome da área quando ela está na tela) e o progresso em
                    // estrelas que se veem: nada de "x de 40 coisas" para a criança (o número fica na descrição).
                    TituloQueCabe(areaNaTela?.nome ?: if (nome.isBlank()) "Minha casinha" else "Casa de $nome")
                    val progressoNaTela = areaNaTela?.let { CasaQueCresce.progresso(estado, it) }
                    val feitas = progressoNaTela?.colocadas ?: pecasDaCasa
                    val total = progressoNaTela?.total ?: ItensDaCasa.totalDaCasa
                    EstrelasDeProgresso(
                        ProgressoVisivel.estrelasAcesas(feitas, total, ESTRELAS_DA_CASA),
                        total = ESTRELAS_DA_CASA,
                        tamanho = 20.dp,
                        brilharUltima = feitas >= total,
                        descricao = progressoNaTela?.rotulo ?: "A casa tem $pecasDaCasa de ${ItensDaCasa.totalDaCasa} coisas",
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
                    // De noite, um azul-lavanda claro: o glifo roxo do sol 3D continua lendo bem.
                    cor = if (noite) Color(0xFFC9CCFF) else Color.White,
                    tamanho = 52.dp,
                )
                Spacer(Modifier.width(8.dp))
                if (estado.temEstrutura(Estrutura.PAREDES) && !atalhosNaColuna) {
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

            if (areasProntas.isNotEmpty()) {
                LugaresDaCasa(estado, areasProntas, pager.currentPage) { i ->
                    sons.toque()
                    escopo.launch { pager.animateScrollToPage(i) }
                    val a = areasProntas.getOrNull(i - 1)
                    narrador.falar(if (a == null) listOf(pt("A casa!")) else listOf(pt("${a.nome}! Em inglês:"), en(a.nomeEn)))
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
                val formaDaCena = Modifier
                    .fillMaxSize()
                    .shadow(6.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                val casaDeFora: @Composable () -> Unit = {
                    CenaDaCasa(
                        estado = estadoNaCena,
                        modifier = formaDaCena,
                        preencherAltura = true,
                        interativa = true,
                        pintando = pintando,
                        novaEstrutura = novaEstrutura,
                        destaqueId = destaque,
                        puloDaCrianca = pulo,
                        noite = noite,
                        onTocarItem = { item -> tocarNoItem(item) },
                        onTocarVisitante = { emoji ->
                            val nomeDoBicho = (Loja.produtos + Surpresas.catalogo).firstOrNull { it.categoria == Categoria.VISITANTE && it.valor == emoji }?.nome
                            narrador.falar(if (nomeDoBicho != null) "Oi, ${nomeDoBicho.lowercase()}! Que pulo!" else "Oi, bichinho!")
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
                        onTocarIrmao = {
                            narrador.falar(br.com.casinhadosaber.dominio.Irmao.falaAoTocar(estado.perfil, falasDoIrmao++))
                        },
                        evento = evento?.tipo,
                    )
                }
                if (areasProntas.isEmpty()) {
                    casaDeFora()
                } else {
                    // A casa e as áreas novas lado a lado: arrastar o fundo (não um objeto) passa de uma para outra.
                    HorizontalPager(pager, Modifier.fillMaxSize(), pageSpacing = 8.dp, key = { if (it == 0) "casa" else areasProntas.getOrNull(it - 1)?.id ?: "pagina$it" }) { pagina ->
                        val area = areasProntas.getOrNull(pagina - 1)
                        if (area == null) {
                            casaDeFora()
                        } else {
                            CenaDaArea(
                                area = area,
                                estado = estadoNaCena,
                                modifier = formaDaCena,
                                noite = noite,
                                construindo = areaNova == area.id,
                                destaqueId = destaque,
                                evento = evento?.tipo,
                                onTocarItem = { item -> tocarNoItem(item) },
                                onMoverItem = { item, pos -> onAtualizar { it.colocar(item, pos) } },
                                onGuardarItem = { item ->
                                    onAtualizar { it.guardar(item) }
                                    sons.toque()
                                    narrador.falar("Guardei no baú.")
                                },
                            )
                        }
                    }
                }
                // Os enfeites do evento do dia ficam dentro da cena, no fundo (atrás das coisas e dos botões).
                // Presentes dos eventos ficam para sempre numa prateleira no alto.
                if (areaNaTela == null) PrateleiraDePresentes(estado.presentesGanhos, Modifier.align(Alignment.TopCenter).padding(top = 14.dp))
                // O mascote mora na casa: tocar abre o cantinho dele (enfeites e quanto falta para crescer).
                estado.mascote.especie?.let { especie ->
                    Box(
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 6.dp, bottom = 6.dp)
                            .size(80.dp)
                            .shadow(4.dp, CircleShape)
                            .background(Color.White, CircleShape)
                            .border(3.dp, Color(0xFFFFE08A), CircleShape)
                            .clip(CircleShape)
                            .testTag("mascote_na_casa")
                            .clickable {
                                sons.piu(1)
                                noCantinho = true
                            },
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        MascoteAnimado(especie, Crescimento.estagio(estado.fasesVencidas), Modifier.size(74.dp), estado.mascote.acessorioEmUso, estado.mascote.nomeParaMostrar)
                    }
                }
                presenteDoEvento?.let { p ->
                    AvisoDoPresente(p, evento?.tipo?.nome.orEmpty(), Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)) { presenteDoEvento = null }
                }
                // Espelho: trocar a aparência a qualquer momento (deitada, ele fica na coluna ao lado).
                if (!atalhosNaColuna) BotaoRedondo(
                    "🪞",
                    "Espelho: trocar a aparência",
                    { sons.toque(); onEspelho() },
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    tamanho = 52.dp,
                )
                // Entrar na casa: só quando a casa já tem porta.
                if (estado.temEstrutura(Estrutura.PORTA) && !pintando && !atalhosNaColuna && areaNaTela == null) {
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
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            ImagemDeArte(IconesDaBarra.CASA, Modifier.size(40.dp)) { Text("🚪", fontSize = 34.sp) }
                            ImagemDeArte(ID_ICONE_AVANCAR, Modifier.size(30.dp)) { Text("👉", fontSize = 26.sp) }
                        }
                    }
                }
            }
            if (deitada) {
                BauDaCasa(Modifier.width(LARGURA_DO_BAU_LATERAL).fillMaxHeight().padding(end = 8.dp, bottom = 8.dp), lateral = true, comAtalhos = atalhosNaColuna)
            }
            }

            if (pintando) {
                // Dica sem palavras: o pincel, o dedo e a casa.
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ImagemDeArte(IconesDaBarra.PINTAR, Modifier.size(36.dp)) { Text("🎨", style = MaterialTheme.typography.headlineMedium) }
                    ImagemDeArte(ID_MAO_TOQUE, Modifier.size(36.dp)) { Text("👆", style = MaterialTheme.typography.headlineMedium) }
                    ImagemDeArte(IconesDaBarra.CASA, Modifier.size(36.dp)) { Text("🏠", style = MaterialTheme.typography.headlineMedium) }
                }
            }

            if (!deitada) BauDaCasa(Modifier.fillMaxWidth(), lateral = false)
        }
        Confete(confete)
        if (escolhendoMascote) {
            EscolhaDoMascote { especie, nomeDoMascote ->
                onAtualizar { it.escolherMascote(especie).nomearMascote(nomeDoMascote) }
                escolhendoMascote = false
                confete++
            }
        }
        if (noCantinho) {
            CantinhoDoMascote(
                mascote = estado.mascote,
                fasesVencidas = estado.fasesVencidas,
                nomeDaCrianca = nome.ifBlank { null },
                onVestir = { a -> onAtualizar { it.vestirMascote(a) } },
                onFechar = { noCantinho = false },
            )
        }
    }
}

/** Aviso do presente do evento (some sozinho ou com um toque). */
@Composable
private fun AvisoDoPresente(p: PresenteEspecial, evento: String, modifier: Modifier, onFechar: () -> Unit) {
    Row(
        modifier
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(4.dp, Cores.Estrela, RoundedCornerShape(24.dp))
            .clickable(onClick = onFechar)
            .semantics(mergeDescendants = true) { contentDescription = "Presente de $evento: ${p.nome}" }
            .heightIn(min = TOQUE_MINIMO)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FiguraDaPergunta(p.emoji, 46.dp) { Text(p.emoji, fontSize = 40.sp) }
        Column {
            Text("Presente de $evento!", style = MaterialTheme.typography.titleMedium, color = Cores.TextoSuave)
            Text(p.nome, style = MaterialTheme.typography.titleLarge)
        }
    }
}

private fun preposicao(c: Comodo) = when (c) {
    Comodo.QUARTO -> "no quarto"
    Comodo.BANHEIRO -> "no banheiro"
    Comodo.SALA -> "na sala"
    Comodo.COZINHA -> "na cozinha"
}

/** Céu da casa (maquete 1): azul em cima, clarinho, pêssego e rosa embaixo, como na tela inicial. */
private val CeuDaCasa = Brush.verticalGradient(
    0f to Color(0xFF7CC4FA),
    0.38f to Color(0xFFB8E2FF),
    0.70f to Color(0xFFFFE6D6),
    1f to Color(0xFFFFD0DC),
)

/** Quantas estrelas mostram o quanto a casa (ou a área na tela) já está pronta. */
private const val ESTRELAS_DA_CASA = 5

// Ids da arte 3D usados aqui (Fases 2, 5 e 8A).
private const val ID_ICONE_AVANCAR = "icone_avancar"
private const val ID_MAO_TOQUE = "ui_mao_toque"
private const val ID_TROFEU = "premio_trofeu"
private const val ID_PRESENTE = "premio_presente_fechado"
private const val ID_MOEDA = "moeda"

/** Altura mínima da tela deitada para a coluna do baú ter também os atalhos grandes. */
private val ALTURA_PARA_ATALHOS = 460.dp

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
    /** Atalhos grandes no pé da coluna lateral (só fora da loja). Com eles, o botão Jogar sai do baú vazio. */
    atalhosLaterais: (@Composable () -> Unit)? = null,
    /** Produtos que não estão em [Loja.produtos] (os visitantes semanais já liberados, ver [Surpresas]). */
    extras: List<Produto> = emptyList(),
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
            .veiosDeMadeira(forma)
            .border(4.dp, Color(0xFF8E5A34), forma)
            .padding(top = 8.dp, bottom = 14.dp),
    ) {
        // Abas: baú de presentes e lojinha. Cada toque também diz em voz alta o que a aba é.
        Row(Modifier.padding(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Aba(
                if (itens.isEmpty()) "Baú de presentes, vazio" else "Baú de presentes, ${itens.size} para colocar na casa",
                selecionada = !naLoja,
                conteudo = {
                    ImagemDeArte(ID_PRESENTE, Modifier.size(34.dp)) { Text("🎁") }
                    // Quantos presentes esperam: poucos, contáveis (como as moedas), e só quando há algum.
                    if (itens.isNotEmpty()) Text("${itens.size}", maxLines = 1)
                },
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
            Aba(
                "Lojinha, você tem ${estado.moedas} moedinhas",
                selecionada = naLoja,
                conteudo = {
                    ImagemDeArte(IconesDaBarra.LOJA, Modifier.size(30.dp)) { Text("🛒") }
                    ImagemDeArte(ID_MOEDA, Modifier.size(26.dp)) { Text("🪙") }
                    Text("${estado.moedas}", maxLines = 1)
                },
            ) {
                naLoja = true
                sons.toque()
                narrador.falar("Lojinha! Você tem ${estado.moedas} moedinhas. Toque numa coisa para ver como fica na casa.")
            }
        }
        Spacer(Modifier.height(8.dp))
        if (naLoja) {
            val vitrine = Loja.vitrine(estado, Loja.categoriasDeFora).let { v ->
                if (extras.isEmpty()) v else (v + extras.filter { e -> v.none { it.id == e.id } }).sortedWith(compareBy({ it.id in estado.comprados }, { it.precoBase }))
            }
            if (lateral) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
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
            val proximo = ItensDaCasa.proximo(estado.fasesVencidas)
            val proximoEhObra = proximo?.obra == true
            val conquista: @Composable () -> Unit = {
                if (proximo != null) {
                    // O próximo presente aparece "apagadinho", com cadeado: sempre há um objetivo à vista.
                    // Tocar nele conta o que é e como ganhar.
                    Box(
                        Modifier
                            .size(80.dp)
                            .shadow(3.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.verticalGradient(listOf(Color(0xFFFFF4DC), Color(0xFFF1DDBB))))
                            .border(3.dp, Color(0xFFC8955E), RoundedCornerShape(20.dp))
                            .semantics { contentDescription = "Próximo presente: ${proximo.nome}. Jogue para ganhar." }
                            .clickable {
                                sons.toque()
                                narrador.falar(
                                    if (proximoEhObra) "O próximo presente é ${proximo.comArtigo}! A casa vai crescer! Jogue mais uma fase para ganhar."
                                    else "O próximo presente é ${proximo.comArtigo}! Jogue para ganhar.",
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        // Silhueta: o desenho do presente pintado de uma cor só (dá para adivinhar o que é).
                        IconeDoItem(
                            proximo,
                            54.dp,
                            Modifier
                                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                                .drawWithContent {
                                    drawContent()
                                    drawRect(Color(0xFF8A6342), blendMode = BlendMode.SrcIn)
                                },
                        )
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(3.dp)
                                .size(30.dp)
                                .shadow(2.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Cores.Estrela, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            ImagemDeArte(IconesDaBarra.PAIS, Modifier.size(20.dp)) { Text("🔒", fontSize = 15.sp) }
                        }
                    }
                } else {
                    // Casa completa: o troféu é um botão que comemora (sozinho ele não dizia nada).
                    BotaoFofo(
                        onClick = {
                            sons.acerto()
                            narrador.falar(
                                "Você ganhou todas as ${ItensDaCasa.total} coisas! A casa cresceu até o fim e está completinha. " +
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
                            ImagemDeArte(ID_TROFEU, Modifier.size(52.dp)) { Text("🏆", fontSize = 40.sp) }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ImagemDeArte(IconesDaBarra.CASA, Modifier.size(24.dp)) { Text("🏠", style = MaterialTheme.typography.titleMedium) }
                                Text(" ✔", style = MaterialTheme.typography.titleMedium, color = Cores.Certo)
                            }
                        }
                    }
                }
            }
            val jogar: @Composable () -> Unit = {
                BotaoFofo(onClick = onJogar, cor = Cores.Certo, descricao = "Jogar", modifier = Modifier.pulsando()) {
                    RotuloDoJogar(Modifier.padding(horizontal = 18.dp, vertical = 10.dp))
                }
            }
            if (lateral) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        // Com os atalhos embaixo, o baú vazio vira um cartão que ocupa a coluna toda (sem vão no meio).
                        .then(
                            if (atalhosLaterais != null) {
                                Modifier
                                    .background(Color(0x40FFFFFF), RoundedCornerShape(22.dp))
                                    .border(2.dp, Color(0x55FFFFFF), RoundedCornerShape(22.dp))
                                    .padding(12.dp)
                            } else {
                                Modifier.padding(4.dp)
                            },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(if (atalhosLaterais != null) 10.dp else 24.dp, Alignment.CenterVertically),
                ) {
                    if (atalhosLaterais != null) {
                        // Baú vazio na coluna: o próximo presente em destaque, com o nome para os pais lerem
                        // e a barrinha de quantas coisas a casa já tem.
                        conquista()
                        Text(
                            if (proximo != null) "Próximo presente: ${proximo.nome}" else "Casa completinha!",
                            style = MaterialTheme.typography.titleMedium,
                            color = Cores.Texto,
                            textAlign = TextAlign.Center,
                        )
                        BarraDaCasa(estado.itensGanhos.size, CasaQueCresce.metaAtual(estado.fasesVencidas))
                    } else {
                        conquista()
                        jogar()
                    }
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
                modifier = Modifier.weight(1f),
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
        if (lateral && !naLoja && atalhosLaterais != null) {
            Spacer(Modifier.height(10.dp))
            atalhosLaterais()
        }
    }
}

/**
 * Quanto da casa já está pronto, numa pílula: a casinha 3D e estrelas que acendem (nunca "x/40"; o número fica
 * só na descrição de acessibilidade).
 */
@Composable
private fun BarraDaCasa(tem: Int, total: Int) {
    Pilula(descricao = "A casa tem $tem de $total coisas") {
        ImagemDeArte(IconesDaBarra.CASA, Modifier.size(30.dp)) { Text("🏠") }
        EstrelasDeProgresso(
            ProgressoVisivel.estrelasAcesas(tem, total, ESTRELAS_DA_CASA),
            total = ESTRELAS_DA_CASA,
            tamanho = 24.dp,
            brilharUltima = tem >= total,
        )
    }
}

/** Título branco com contorno que diminui para caber numa linha (nomes longos de áreas e crianças em 360 dp). */
@Composable
private fun TituloQueCabe(texto: String) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val base = MaterialTheme.typography.headlineMedium
        // Baloo 2 negrito: cerca de 0,55 em por letra; a folga é o contorno de 4 dp dos dois lados.
        val cabe = ((maxWidth.value - 8f) / (texto.length.coerceAtLeast(1) * 0.55f)).coerceIn(16f, base.fontSize.value)
        TextoComContorno(
            texto,
            estilo = base.copy(fontSize = cabe.sp, lineHeight = (cabe * 1.15f).sp),
            espessura = 4.dp,
            profundidade = 2.dp,
        )
    }
}

/** "Jogar" no botão verde: o triângulo de tocar num círculo branco e a palavra com contorno escuro (maquete 3). */
@Composable
private fun RotuloDoJogar(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(38.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            ImagemDeArte(IconesDaBarra.TOCAR, Modifier.size(28.dp)) { Text("🎮", fontSize = 22.sp) }
        }
        TextoComContorno("Jogar", estilo = MaterialTheme.typography.headlineMedium, corDoContorno = Cores.ContornoEscuro, espessura = 4.dp, profundidade = 2.dp)
    }
}

/** Atalho grande da coluna lateral (paisagem): ícone 3D (ou emoji) e uma palavra, do tamanho da mão da criança. */
@Composable
private fun AtalhoGrande(
    emoji: String,
    rotulo: String,
    descricao: String,
    modifier: Modifier = Modifier,
    cor: Color = Color.White,
    corBase: Color = Color(0xFF8E5A34),
    onClick: () -> Unit,
) {
    BotaoFofo(onClick = onClick, modifier = modifier, cor = cor, corBase = corBase, descricao = descricao) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(min = TOQUE_MINIMO)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val icone = remember(emoji) { IconesDaBarra.para(emoji) }
            val doEmoji: @Composable () -> Unit = { Text(emoji, fontSize = 34.sp, maxLines = 1) }
            if (icone == null) doEmoji() else IconeDeArteDaBarra(icone, Modifier.size(40.dp), reserva = doEmoji)
            Text(rotulo, style = MaterialTheme.typography.titleMedium, color = Cores.Texto, maxLines = 1)
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
            MiniaturaDoProduto(produto, estado, Modifier.size(52.dp))
            if (tem) {
                Text(
                    if (usando) "✔ usando" else "✔ é seu",
                    style = MaterialTheme.typography.titleMedium,
                    color = Cores.Certo,
                    maxLines = 1,
                )
            } else {
                // O preço como no placar das maquetes: a moeda 3D e o número (com a mãozinha na prévia).
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (previa?.id == produto.id) ImagemDeArte(ID_MAO_TOQUE, Modifier.size(22.dp)) { Text("👆", style = MaterialTheme.typography.titleMedium) }
                    ImagemDeArte(ID_MOEDA, Modifier.size(22.dp)) { Text("🪙", style = MaterialTheme.typography.titleMedium) }
                    Text("$preco", style = EstiloDaPilula.copy(fontSize = 20.sp, lineHeight = 22.sp), maxLines = 1)
                }
            }
        }
    }
}

/**
 * Desenho pequeno do produto: telhado ou porta na cor certa, amostra do papel de parede, o chapéu na cabeça da
 * criança (camadas 3D, miniatura parada) ou a figura 3D do emoji (o emoji fica de reserva).
 */
@Composable
private fun MiniaturaDoProduto(p: Produto, estado: EstadoJogo, modifier: Modifier) {
    when (p.categoria) {
        Categoria.CHAPEU -> Box(modifier, contentAlignment = Alignment.BottomCenter) {
            Crianca(miniaturaVestindo(estado.perfil ?: Perfil(), p), Modifier.fillMaxHeight(), animado = false)
        }
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
        else -> Box(modifier, contentAlignment = Alignment.Center) {
            FiguraDaPergunta(p.icone, 48.dp) { Text(p.icone, fontSize = 34.sp, maxLines = 1) }
        }
    }
}

/**
 * Aba do baú (presentes, lojinha): pílula de brinquedo com ícones 3D e um número curto; a escolhida fica amarela
 * com o lábio dourado. Um nó só para a acessibilidade, com a [descricao].
 */
@Composable
private fun Aba(descricao: String, selecionada: Boolean, conteudo: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit, onClick: () -> Unit) {
    Row(
        Modifier
            .semantics {
                contentDescription = descricao
                selected = selecionada
            }
            .clickable(role = Role.Tab, onClick = onClick)
            .pilula(
                corDoCorpo = if (selecionada) Cores.Estrela else Color.White,
                corDoLabio = if (selecionada) Color(0xFFE0A800) else Color(0xFFD9B184),
            )
            .heightIn(min = TOQUE_MINIMO - 5.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        androidx.compose.material3.ProvideTextStyle(EstiloDaPilula) { conteudo() }
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
                IconeDaCoisa(item, 60.dp)
                if (item.ehEstrutura) {
                    Text("🔨", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.BottomEnd))
                }
                if (primeiro) MaoGuia(Modifier.align(Alignment.BottomStart).offset(x = (-6).dp, y = 22.dp), espera = 1500)
            }
            Text(item.nome, style = MaterialTheme.typography.bodyMedium, maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}

/**
 * Lugares da casa que cresce: a casa e cada área construída, com estrelinhas de quanto já está arrumado
 * (o "3/44" fica só na descrição de acessibilidade).
 * Tocar leva até o lugar (também dá para arrastar o fundo para o lado). No fim, a próxima área, trancada,
 * diz quantas fases faltam para ela chegar: sempre há um objetivo à vista.
 */
@Composable
private fun LugaresDaCasa(estado: EstadoJogo, areas: List<AreaDaCasa>, paginaAtual: Int, onIr: (Int) -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val proxima = CasaQueCresce.proxima(estado.fasesVencidas)
    // O lugar que está na tela fica sempre à vista na fileira.
    val lista = androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(paginaAtual) { lista.animateScrollToItem((paginaAtual - 1).coerceAtLeast(0)) }
    LazyRow(
        Modifier
            .fillMaxWidth()
            .testTag("lugares_da_casa"),
        state = lista,
        contentPadding = PaddingValues(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(areas.size + 1) { i ->
            val area = areas.getOrNull(i - 1)
            val selecionado = i == paginaAtual
            val progresso = area?.let { CasaQueCresce.progresso(estado, it) }
            // Pílula do lugar: o ícone e, nas áreas, três estrelinhas de quanto já está arrumado (nunca "3/44").
            Row(
                Modifier
                    .semantics { contentDescription = progresso?.rotulo ?: "Casa" }
                    .testTag(if (area == null) "lugar_casa" else "lugar_${area.id}")
                    .clickable { onIr(i) }
                    .pilula(
                        corDoCorpo = if (selecionado) Cores.Estrela else Color.White,
                        corDoLabio = if (selecionado) Color(0xFFE0A800) else Cores.LabioClaro,
                    )
                    .heightIn(min = TOQUE_MINIMO - 10.dp)
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (area == null) {
                    ImagemDeArte(IconesDaBarra.CASA, Modifier.size(34.dp)) { Text("🏠", style = MaterialTheme.typography.titleLarge) }
                } else {
                    FiguraDaPergunta(area.icone, 30.dp) { Text(area.icone, style = MaterialTheme.typography.titleLarge, maxLines = 1) }
                    EstrelasDeProgresso(
                        ProgressoVisivel.estrelasAcesas(progresso!!.colocadas, progresso.total, 3),
                        total = 3,
                        tamanho = 15.dp,
                        espaco = 0.dp,
                    )
                }
            }
        }
        if (proxima != null) {
            item {
                val (area, faltam) = proxima
                Row(
                    Modifier
                        .semantics { contentDescription = "${area.nome}: faltam $faltam fases" }
                        .clickable {
                            sons.toque()
                            narrador.falar(
                                if (faltam == 1) "Falta só uma fase para a casa ganhar ${area.comArtigo}!"
                                else "Faltam $faltam fases para a casa ganhar ${area.comArtigo}!",
                            )
                        }
                        .graphicsLayer { alpha = 0.75f }
                        .pilula(corDoCorpo = Color(0xFFEDE7FF))
                        .heightIn(min = TOQUE_MINIMO - 10.dp)
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    ImagemDeArte(IconesDaBarra.PAIS, Modifier.size(24.dp)) { Text("🔒", style = MaterialTheme.typography.titleMedium) }
                    FiguraDaPergunta(area.icone, 30.dp) { Text(area.icone, style = MaterialTheme.typography.titleLarge, maxLines = 1) }
                }
            }
        }
    }
}

/**
 * Veios de madeira e chanfro por dentro do painel do baú: linhas onduladas bem suaves, um brilho no alto e uma
 * sombra embaixo. Montado uma vez por tamanho (drawWithCache), recortado na [forma] do painel.
 */
private fun Modifier.veiosDeMadeira(forma: androidx.compose.ui.graphics.Shape): Modifier = drawWithCache {
    val contorno = Path().apply { addOutline(forma.createOutline(size, layoutDirection, this@drawWithCache)) }
    val passo = 14.dp.toPx()
    val veios = Path()
    var y = passo * 0.6f
    var k = 0
    while (y < size.height) {
        val amplitude = passo * (0.18f + 0.1f * (k % 3))
        val onda = size.width / (2.5f + (k % 4))
        veios.moveTo(0f, y)
        var x = 0f
        while (x < size.width) {
            veios.quadraticTo(x + onda / 2f, y + if ((x / onda).toInt() % 2 == 0) amplitude else -amplitude, x + onda, y)
            x += onda
        }
        y += passo * (0.8f + 0.25f * ((k * 7) % 3))
        k++
    }
    // Nós da madeira, em lugares fixos.
    val nos = listOf(0.18f to 0.55f, 0.72f to 0.3f, 0.5f to 0.85f).map { (u, v) -> Offset(size.width * u, size.height * v) }
    val traco = androidx.compose.ui.graphics.drawscope.Stroke(1.2.dp.toPx())
    val chanfro = 3.dp.toPx()
    val brilho = Brush.verticalGradient(listOf(Color(0x40FFFFFF), Color.Transparent), startY = 0f, endY = chanfro * 4f)
    val sombra = Brush.verticalGradient(listOf(Color.Transparent, Color(0x30000000)), startY = size.height - chanfro * 5f, endY = size.height)
    onDrawBehind {
        clipPath(contorno) {
            drawPath(veios, Color(0x1A5A3414), style = traco)
            for (n in nos) {
                drawOval(Color(0x1A5A3414), Offset(n.x - passo * 0.7f, n.y - passo * 0.25f), Size(passo * 1.4f, passo * 0.5f), style = traco)
                drawOval(Color(0x145A3414), Offset(n.x - passo * 0.35f, n.y - passo * 0.12f), Size(passo * 0.7f, passo * 0.24f))
            }
            drawRect(brilho, size = Size(size.width, chanfro * 4f))
            drawRect(sombra, topLeft = Offset(0f, size.height - chanfro * 5f), size = Size(size.width, chanfro * 5f))
        }
    }
}
