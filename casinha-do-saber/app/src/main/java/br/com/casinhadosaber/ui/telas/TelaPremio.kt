package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Categoria
import br.com.casinhadosaber.dominio.Crescimento
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.Historia
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.ParteDaFala
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Produto
import br.com.casinhadosaber.dominio.VozesDaFamilia
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.dominio.tirando
import br.com.casinhadosaber.dominio.usa
import br.com.casinhadosaber.dominio.vestindo
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BalaoDeFala
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.ChuvaDeConfete
import br.com.casinhadosaber.ui.componentes.Confete3D
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.EstrelaDoPremio
import br.com.casinhadosaber.ui.componentes.ExplosaoDeEstrelas
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.IconeDeCasaBranca
import br.com.casinhadosaber.ui.componentes.IconeDoItem
import br.com.casinhadosaber.ui.componentes.Irmaozinho
import br.com.casinhadosaber.ui.componentes.LadoDaCauda
import br.com.casinhadosaber.ui.componentes.MascoteAnimado
import br.com.casinhadosaber.ui.componentes.NoPalco
import br.com.casinhadosaber.ui.componentes.PROPORCAO_CRIANCA
import br.com.casinhadosaber.ui.componentes.PROPORCAO_IRMAO
import br.com.casinhadosaber.ui.componentes.PalcoDaFesta
import br.com.casinhadosaber.ui.componentes.PersonagemDaArte
import br.com.casinhadosaber.ui.componentes.PilulaDeMoedas
import br.com.casinhadosaber.ui.componentes.PresenteQueAbre
import br.com.casinhadosaber.ui.componentes.RaiosDeLuz
import br.com.casinhadosaber.ui.componentes.TextoComContorno
import br.com.casinhadosaber.ui.componentes.apoioDoPresente
import br.com.casinhadosaber.ui.componentes.caminhoEstrela
import br.com.casinhadosaber.ui.componentes.proporcaoDoPresente
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/** Fundo do prêmio (maquete 3, `prototipo/premio.html`): roxo em cima, lilás no meio e rosa embaixo. */
private val FUNDO_DO_PREMIO = Brush.verticalGradient(
    0f to Color(0xFF8A6CFF),
    0.4f to Color(0xFFB783FF),
    0.85f to Color(0xFFFF9FCF),
    1f to Color(0xFFFFC2DC),
)

/** Verde do botão "Pôr na casinha" (`--c`, `--c2` e `--b` do protótipo). */
private val VERDE_DO_BOTAO = Color(0xFF34C46A)
private val LABIO_DO_VERDE = Color(0xFF1C9552)

/**
 * Comemoração ao vencer uma fase, como a maquete 3 (visual brinquedo 3D): fundo roxo com raios, "Parabéns" de
 * contorno, três estrelas 3D, as moedas ganhas em pilhas que se contam e, num pódio, a criança e o irmãozinho
 * torcendo dos lados do presente. Na primeira vitória o presente aparece: a criança toca, ele abre em quadros e a
 * peça nova da casa (o item 3D) sobe de dentro da caixa. Depois das 40 peças, o presente é um [brinde] da loja (já
 * aplicado ao estado pelo domínio) e mais moedinhas.
 */
@Composable
fun TelaPremio(
    fase: Fase,
    estrelas: Int,
    item: ItemCasa?,
    perfil: Perfil?,
    moedas: Int = 0,
    temProxima: Boolean,
    onColocarNaCasa: () -> Unit,
    onProxima: () -> Unit,
    onVoltar: () -> Unit,
    /** Presente da loja quando a casa já está completa (ver `ResultadoPartida.brinde`). */
    brinde: Produto? = null,
    /** Fala no lugar de "Você terminou a fase!" (no fim do treino, por exemplo). */
    mensagem: String? = null,
    /** Estado depois da partida: o mascote comemora junto (e cresce, ver [fasesAntes]). */
    estado: EstadoJogo? = null,
    /**
     * Fases vencidas antes desta partida (só numa fase normal; null no treino e na missão). Com ela a tela
     * mostra a cena do chefão vencido pela primeira vez e a festa quando o mascote cresce ou ganha enfeite.
     */
    fasesAntes: Int? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val contexto = LocalContext.current
    var estrelasVisiveis by remember { mutableIntStateOf(0) }
    var aberto by remember { mutableStateOf(false) }
    var confete by remember { mutableIntStateOf(0) }
    val nome = perfil?.nome.orEmpty()
    val vibracao = LocalHapticFeedback.current
    // A peça da casa tem prioridade; sem ela, o brinde da loja.
    val brindeDaLoja = if (item == null) brinde else null
    val temPresente = item != null || brindeDaLoja != null
    // Mascote (sugestão 2) e história (sugestão 3): cenas que aparecem antes da comemoração de sempre.
    val mascote = estado?.mascote?.takeIf { it.escolhido }
    val fasesDepois = estado?.fasesVencidas ?: 0
    val faseNova = fasesAntes != null && fasesDepois > fasesAntes
    var cenaDoChefao by remember { mutableStateOf(if (faseNova) Historia.cenaDoChefao(fase) else null) }
    var festaDoMascote by remember {
        mutableStateOf(
            mascote != null && fasesAntes != null &&
                (Crescimento.cresceu(fasesAntes, fasesDepois) || Crescimento.acessoriosNovos(fasesAntes, fasesDepois).isNotEmpty()),
        )
    }

    fun abrir() {
        if (aberto || !temPresente) return
        aberto = true
        confete++
        vibracao.performHapticFeedback(HapticFeedbackType.LongPress)
        sons.premio()
        if (item != null) {
            val ultima = item == ItensDaCasa.todos.last()
            narrador.falar(
                listOf(pt("Você ganhou ${item.comArtigo}! Em inglês:"), en(item.nomeEn)) +
                    when {
                        ultima -> listOf(pt("Sua casa está completa! Você é um campeão!"))
                        item.obra -> listOf(pt("A sua casa vai crescer! Vamos construir?"))
                        else -> listOf(pt("Vamos colocar na sua casa?"))
                    },
            )
        } else if (brindeDaLoja != null) {
            narrador.falar(falaDoBrinde(brindeDaLoja))
            sons.moeda()
        }
    }

    LaunchedEffect(Unit) {
        // Espera as cenas da história e do mascote terminarem (o narrador fala uma coisa de cada vez).
        snapshotFlow { cenaDoChefao == null && !festaDoMascote }.first { it }
        sons.acerto()
        // A vitória é uma das frases que a família pode gravar (área dos pais); o resto é a voz do aparelho.
        val parabens = if (mensagem != null) ParteDaFala(pt(mensagem)) else VozesDaFamilia.vitoria(nome, estrelas)
        narrador.falarPartes(
            if (temPresente) listOf(parabens, ParteDaFala(pt("Toque no presente para abrir!"))) else listOf(parabens),
        )
        for (i in 1..estrelas) {
            delay(380)
            estrelasVisiveis = i
            sons.toque()
        }
        if (!temPresente) confete++
        if (moedas > 0) {
            delay(250)
            sons.moeda()
        }
    }

    val corDaTrilha = Color(fase.trilha.corArgb)
    // Onde está o presente (ou as estrelas, sem presente): os raios de luz giram atrás dele.
    // (centro guardado na conta da janela; vira fração da tela na hora de desenhar, quando as duas medidas já chegaram)
    var centroNaJanela by remember { mutableStateOf<Offset?>(null) }
    var origemDaTela by remember { mutableStateOf(Offset.Zero) }
    var tamanhoDaTela by remember { mutableStateOf(Size.Zero) }
    fun marcarCentro(c: LayoutCoordinates) {
        centroNaJanela = c.positionInRoot() + Offset(c.size.width / 2f, c.size.height / 2f)
    }
    Box(Modifier.fillMaxSize().background(FUNDO_DO_PREMIO)) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .onGloballyPositioned {
                origemDaTela = it.positionInRoot()
                tamanhoDaTela = Size(it.size.width.toFloat(), it.size.height.toFloat())
            },
    ) {
        // Tela alta: tudo um pouco maior (o pódio continua na mesma largura).
        val alta = maxHeight > 720.dp
        val baixa = maxHeight < 600.dp
        val larguraDaTela = maxWidth
        val centro = centroNaJanela
        val fracao = if (centro != null && tamanhoDaTela.width > 0f && tamanhoDaTela.height > 0f) {
            Offset((centro.x - origemDaTela.x) / tamanhoDaTela.width, (centro.y - origemDaTela.y) / tamanhoDaTela.height)
        } else {
            Offset(0.5f, 0.5f)
        }
        RaiosDeLuz(alfa = 0.3f, quantidade = 16, centroX = fracao.x, centroY = fracao.y)
        // Brilho quente atrás do presente (o `radial-gradient` do protótipo).
        Canvas(Modifier.fillMaxSize()) {
            val c = Offset(size.width * fracao.x, size.height * fracao.y)
            val r = size.minDimension * 0.62f
            drawCircle(
                Brush.radialGradient(
                    0f to Color(0xF2FFF7C8),
                    0.2f to Color(0x8CFFD678),
                    1f to Color(0x00FFBEDC),
                    center = c,
                    radius = r,
                ),
                r,
                c,
            )
        }
        ChuvaDeConfete()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Três blocos (parabéns, palco, botões) distribuídos na altura toda.
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextoComContorno(
                    if (nome.isBlank()) "Parabéns!" else "Parabéns, $nome!",
                    estilo = MaterialTheme.typography.headlineLarge.let {
                        if (alta) it.copy(fontSize = 40.sp, lineHeight = 44.sp) else it
                    },
                    maxLinhas = 2,
                )
                Spacer(Modifier.height(if (alta) 6.dp else 2.dp))
                // Três estrelas 3D: a do meio maior e mais alta, as dos lados inclinadas para fora (maquete 3).
                val lado = if (alta) 84.dp else if (baixa) 58.dp else 70.dp
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.semantics(mergeDescendants = true) {
                        contentDescription = "$estrelas de 3 estrelas"
                    }.then(if (!temPresente) Modifier.onGloballyPositioned(::marcarCentro) else Modifier),
                ) {
                    for (i in 1..3) {
                        val meio = i == 2
                        val tamanho = if (meio) lado * 1.22f else lado
                        val escala = remember { Animatable(0.6f) }
                        LaunchedEffect(estrelasVisiveis >= i) {
                            if (estrelasVisiveis >= i) {
                                escala.animateTo(1.3f, tween(140))
                                escala.animateTo(1f, spring(dampingRatio = 0.4f))
                            }
                        }
                        // Cada estrela que pousa estoura em faíscas (o estouro passa por fora da estrela).
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.offset(y = if (meio) (-lado * 0.16f) else 0.dp)) {
                            EstrelaDoPremio(
                                estrelasVisiveis >= i,
                                tamanho,
                                Modifier.graphicsLayer {
                                    scaleX = escala.value
                                    scaleY = escala.value
                                    rotationZ = when (i) { 1 -> -12f; 3 -> 12f; else -> 0f }
                                },
                            )
                            ExplosaoDeEstrelas(
                                disparo = if (estrelasVisiveis >= i) i else 0,
                                // Sem ocupar lugar: do tamanho da estrela (matchParentSize), mas desenhado em 150 dp
                                // centrado. Antes a caixa crescia para 150 dp durante o estouro e empurrava a tela.
                                modifier = Modifier.matchParentSize().wrapContentSize(unbounded = true).requiredSize(150.dp),
                                quantidade = 10,
                                duracao = 700,
                            )
                        }
                    }
                }
                if (moedas > 0) {
                    Spacer(Modifier.height(4.dp))
                    PilulaDeMoedas(moedas)
                }
            }

            // Meio: o nome do que ganhou (ou o convite para abrir) e o palco com o presente e os irmãos.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val estiloDoConvite = MaterialTheme.typography.titleLarge
                when {
                    item != null -> if (aberto) {
                        TextoComContorno(item.nome, estilo = MaterialTheme.typography.headlineMedium)
                        Text(
                            "Em inglês: ${item.nomeEn}",
                            style = MaterialTheme.typography.titleMedium.copy(shadow = SOMBRA_DO_TITULO),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    } else {
                        TextoComContorno("Um presente para a sua casa!", estilo = estiloDoConvite, espessura = 3.dp, profundidade = 2.dp, maxLinhas = 2)
                    }
                    brindeDaLoja != null -> if (aberto) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextoComContorno(brindeDaLoja.nome, estilo = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f, fill = false), maxLinhas = 2)
                            // As moedinhas extras do brinde, em pilhas que se contam.
                            PilulaDeMoedas(EstadoJogo.MOEDAS_DO_BRINDE, descricao = "Mais ${EstadoJogo.MOEDAS_DO_BRINDE} moedinhas")
                        }
                    } else {
                        TextoComContorno("Um presente da lojinha!", estilo = estiloDoConvite, espessura = 3.dp, profundidade = 2.dp, maxLinhas = 2)
                    }
                    else -> BalaoDeFala(
                        "Você já ganhou o presente desta fase.\nJogue uma fase nova para ganhar mais!",
                        modifier = Modifier.widthIn(max = 340.dp),
                        cauda = LadoDaCauda.Nenhum,
                    )
                }
                Spacer(Modifier.height(4.dp))

                // O chapéu, a roupa ou o acessório ganho aparece na criança só depois de abrir (surpresa).
                val gaveta = brindeDaLoja?.let { b -> Loja.gavetaDe(b) }
                val vestido = perfil?.let {
                    when {
                        brindeDaLoja == null || gaveta == null -> it
                        aberto -> it.vestindo(brindeDaLoja)
                        it.usa(brindeDaLoja) -> it.tirando(gaveta)
                        else -> it
                    }
                }
                val comIrmao = perfil?.irmao?.ligado == true
                // Com o irmãozinho e o mascote juntos, todos encolhem um pouco para caber no pódio.
                val fator = if (comIrmao && mascote?.especie != null) 0.86f else 1f
                val alturaDaCrianca = (if (alta) 150.dp else if (baixa) 100.dp else 124.dp) * fator
                val alturaDoIrmao = alturaDaCrianca * 0.8f
                val larguraDoPresente = if (alta) 206.dp else if (baixa) 150.dp else 180.dp
                val larguraDoPalco = minOf(larguraDaTela - 24.dp, if (alta) 360.dp else 330.dp)
                val apoio = remember { apoioDoPresente(contexto) }
                val proporcao = remember { proporcaoDoPresente(contexto) }
                val pecas = buildList {
                    if (temPresente) {
                        add(
                            NoPalco("centro", larguraDoPresente, larguraDoPresente * proporcao, apoio.first, apoio.second, acima = 16.dp) { m ->
                                PresenteQueAbre(
                                    aberto = aberto,
                                    largura = larguraDoPresente,
                                    modifier = m
                                        .onGloballyPositioned(::marcarCentro)
                                        .semantics { contentDescription = if (aberto) (item?.nome ?: brindeDaLoja?.nome.orEmpty()) else "Presente. Toque para abrir." }
                                        .clickable(enabled = !aberto, role = Role.Button, onClick = ::abrir),
                                    reserva = { r -> Box(r, contentAlignment = Alignment.Center) { CaixaDePresente(Modifier.size(larguraDoPresente * 0.6f)) } },
                                ) { p ->
                                    if (item != null) IconeDoItem(item, larguraDoPresente * 0.52f, p)
                                    else if (brindeDaLoja != null) DesenhoDoBrinde(brindeDaLoja, larguraDoPresente * 0.52f, p, perfil)
                                }
                            },
                        )
                    }
                    if (vestido != null) {
                        add(
                            NoPalco("esquerda", alturaDaCrianca * PROPORCAO_CRIANCA, alturaDaCrianca, apoioY = PersonagemDaArte.CRIANCA.apoioNaCaixa) { m ->
                                Crianca(vestido, m, humor = Humor.TORCENDO)
                            },
                        )
                        if (comIrmao) {
                            add(
                                NoPalco("direita", alturaDoIrmao * PROPORCAO_IRMAO, alturaDoIrmao, apoioY = PersonagemDaArte.IRMAO.apoioNaCaixa) { m ->
                                    Irmaozinho(vestido, m, humor = Humor.TORCENDO)
                                },
                            )
                        }
                        // O mascote comemora junto, do tamanho do estágio dele: no lugar do irmãozinho, ou na
                        // frente, entre a criança e o presente, quando o irmãozinho também está.
                        mascote?.especie?.let { e ->
                            val lado = alturaDaCrianca * 0.62f
                            add(
                                NoPalco(
                                    if (comIrmao) "esquerda" else "direita", lado, lado, apoioY = 0.94f,
                                    desvioX = if (comIrmao) 0.16f else 0f, desvioY = if (comIrmao) 0.3f else 0f,
                                ) { m ->
                                    MascoteAnimado(e, Crescimento.estagio(fasesDepois), m, mascote.acessorioEmUso, mascote.nomeParaMostrar, pulando = true)
                                },
                            )
                        }
                    }
                }
                PalcoDaFesta(larguraDoPalco, pecas)
            }

            // Embaixo: depois de abrir (ou logo, sem presente), "Pôr na casinha" e os botões redondos.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedVisibility(visible = !temPresente || aberto, enter = fadeIn() + scaleIn()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (temPresente) {
                            // "Colocar na casa" e "Ver na casa" continuam sendo o nome do botão (os testes no emulador procuram).
                            val texto = if (item != null) "Pôr na casinha" else "Ver na casinha"
                            BotaoFofo(
                                onClick = onColocarNaCasa,
                                cor = VERDE_DO_BOTAO,
                                corBase = LABIO_DO_VERDE,
                                modifier = Modifier.fillMaxWidth().widthIn(max = 340.dp).padding(horizontal = 16.dp).pulsando(),
                                descricao = if (item != null) "Colocar na casa" else "Ver na casa",
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconeDeCasaBranca(VERDE_DO_BOTAO, Modifier.size(36.dp))
                                    Spacer(Modifier.size(10.dp))
                                    TextoComContorno(texto, estilo = MaterialTheme.typography.headlineMedium, corDoContorno = Cores.ContornoEscuro)
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            BotaoRedondo("⬅️", "Voltar para o caminho", onVoltar, tamanho = 56.dp)
                            if (temProxima) {
                                if (!temPresente) {
                                    BotaoFofo(onClick = onProxima, cor = corDaTrilha, corBase = Color(fase.trilha.corEscuraArgb), modifier = Modifier.pulsando(), descricao = "Próxima fase") {
                                        TextoComContorno(
                                            "Próxima",
                                            estilo = MaterialTheme.typography.headlineMedium,
                                            corDoContorno = Cores.ContornoEscuro,
                                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                                        )
                                    }
                                } else {
                                    BotaoRedondo("▶️", "Próxima fase", onProxima, tamanho = 56.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
        Confete3D(confete)
        cenaDoChefao?.let { texto -> CenaDaHistoria(fase, texto, chefao = true) { cenaDoChefao = null } }
        if (cenaDoChefao == null && festaDoMascote && mascote != null && fasesAntes != null) {
            FestaDoMascote(mascote, fasesAntes, fasesDepois) { festaDoMascote = false }
        }
    }
    }
}

/** O que o narrador diz ao abrir o brinde: o que ganhou, onde já está e as moedinhas extras. */
private fun falaDoBrinde(p: Produto): String {
    val primeira = p.nome.substringBefore(' ')
    val feminino = primeira.endsWith("a") || primeira in setOf("Parede", "Flor", "Neve")
    val artigo = if (feminino) "uma" else "um"
    val oQue = when (p.categoria) {
        Categoria.CLIMA, Categoria.ACESSORIO -> "Você ganhou um presente da lojinha: ${p.nome.lowercase()}!"
        else -> "Você ganhou um presente da lojinha: $artigo ${p.nome.lowercase()}!"
    }
    val onde = when (p.categoria) {
        Categoria.TELHADO, Categoria.PORTA, Categoria.PAREDE -> "Já está na sua casa!"
        Categoria.CHAPEU -> "Olha só, você já está usando!"
        Categoria.VISITANTE -> "Vai passear no seu jardim!"
        Categoria.CLIMA -> if (p.valor == "2") "Agora pode nevar na sua casa!" else "Agora pode chover na sua casa!"
        Categoria.COMIDA -> "Está na geladeira da sua casa!"
        Categoria.ROUPA -> "Está no guarda-roupa, e você já está vestindo!"
        Categoria.ACESSORIO -> "Está no guarda-roupa, e você já está usando!"
    }
    return "$oQue $onde E mais ${EstadoJogo.MOEDAS_DO_BRINDE} moedinhas!"
}

/** Desenho grande do brinde: telhado e porta na cor ganha, amostra do papel de parede, ou o emoji. */
@Composable
private fun DesenhoDoBrinde(p: Produto, lado: Dp, modifier: Modifier = Modifier, perfil: Perfil? = null) {
    val indice = p.valor.toIntOrNull() ?: 0
    Box(modifier.size(lado), contentAlignment = Alignment.Center) {
        when (p.categoria) {
            Categoria.TELHADO -> Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cor = Color(Loja.coresDoTelhado.getOrElse(indice) { Loja.coresDoTelhado[0] })
                val escura = Color(Loja.coresDoTelhadoEscuras.getOrElse(indice) { Loja.coresDoTelhadoEscuras[0] })
                drawRoundRect(Color(0xFFFFE9C7), Offset(w * 0.2f, h * 0.48f), Size(w * 0.6f, h * 0.44f), CornerRadius(w * 0.03f))
                drawRoundRect(Color(0xFFA0522D), Offset(w * 0.43f, h * 0.66f), Size(w * 0.14f, h * 0.26f), CornerRadius(w * 0.05f))
                drawRoundRect(Color(0xFF8FD3FF), Offset(w * 0.26f, h * 0.58f), Size(w * 0.12f, h * 0.11f), CornerRadius(w * 0.02f))
                drawRoundRect(Color(0xFF8FD3FF), Offset(w * 0.62f, h * 0.58f), Size(w * 0.12f, h * 0.11f), CornerRadius(w * 0.02f))
                val telhado = Path().apply {
                    moveTo(w * 0.06f, h * 0.52f); lineTo(w * 0.5f, h * 0.1f); lineTo(w * 0.94f, h * 0.52f); close()
                }
                drawPath(telhado, cor)
                drawPath(telhado, escura, style = Stroke(w * 0.035f))
            }
            Categoria.PORTA -> Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cor = Color(Loja.coresDaPorta.getOrElse(indice) { Loja.coresDaPorta[0] })
                drawRoundRect(Color(0xFFFFE9C7), Offset(w * 0.12f, h * 0.06f), Size(w * 0.76f, h * 0.9f), CornerRadius(w * 0.06f))
                drawRoundRect(cor, Offset(w * 0.27f, h * 0.14f), Size(w * 0.46f, h * 0.82f), CornerRadius(w * 0.23f, w * 0.23f))
                drawRoundRect(Color.Black.copy(alpha = 0.18f), Offset(w * 0.27f, h * 0.14f), Size(w * 0.46f, h * 0.82f), CornerRadius(w * 0.23f, w * 0.23f), style = Stroke(w * 0.025f))
                drawCircle(Color(0xFFFFD166), w * 0.045f, Offset(w * 0.63f, h * 0.58f))
            }
            Categoria.PAREDE -> Canvas(Modifier.fillMaxSize()) {
                val w = size.width
                drawRoundRect(Color(0xFFFFF1C9), cornerRadius = CornerRadius(w * 0.1f))
                val passo = w / 5
                for (i in 0 until 5) for (j in 0 until 5) {
                    val c = Offset(passo * (i + 0.5f), passo * (j + 0.5f))
                    when (p.valor) {
                        "1" -> if (i % 2 == 0 && j == 0) drawRect(Color(0x33EF476F), Offset(passo * i + passo * 0.25f, 0f), Size(passo / 2, size.height))
                        "2" -> drawCircle(Color(0x554D96FF), passo * 0.2f, c)
                        else -> if ((i + j) % 2 == 0) drawPath(caminhoEstrela(c, passo * 0.38f), Color(0xFFFFC93C))
                    }
                }
                drawRoundRect(Color(0xFFE6B98F), cornerRadius = CornerRadius(w * 0.1f), style = Stroke(w * 0.04f))
            }
            // Roupa e acessório: a própria criança vestida, com o ícone do presente no cantinho.
            Categoria.ROUPA, Categoria.ACESSORIO -> if (perfil != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Crianca(perfil.vestindo(p), Modifier.height(lado * 0.92f), animado = false)
                    Text(
                        p.icone,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = (lado.value * 0.26f).sp, lineHeight = (lado.value * 0.3f).sp),
                        maxLines = 1,
                        modifier = Modifier.align(Alignment.BottomEnd),
                    )
                }
            } else {
                Text(
                    p.icone,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = (lado.value * 0.62f).sp, lineHeight = (lado.value * 0.72f).sp),
                    maxLines = 1,
                )
            }
            else -> Text(
                p.icone,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = (lado.value * 0.62f).sp, lineHeight = (lado.value * 0.72f).sp),
                maxLines = 1,
            )
        }
    }
}

/** Sombra ameixa suave do texto branco. */
private val SOMBRA_DO_TITULO = Shadow(Color(0x553D2C5E), Offset(0f, 4f), 8f)

/**
 * A caixa de presente desenhada: corpo rosa com fita dourada, tampa mais larga, laço de duas alças,
 * brilho no alto e contorno ameixa (mesmo traço dos botões). Reserva do presente 3D, quando a arte falta.
 */
@Composable
private fun CaixaDePresente(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val contorno = Cores.Texto.copy(alpha = 0.45f)
        val traco = Stroke(w * 0.022f)
        val rosa = Color(0xFFFF5D8F)
        val rosaEscuro = Color(0xFFE0447A)
        val fita = Color(0xFFFFC93C)
        val fitaEscura = Color(0xFFE8A817)
        // Sombra no chão
        drawOval(Cores.Sombra, Offset(w * 0.14f, h * 0.86f), Size(w * 0.72f, h * 0.1f))
        // Corpo
        val corpoTopo = h * 0.42f
        val corpo = Size(w * 0.68f, h * 0.48f)
        val corpoEsq = Offset(w * 0.16f, corpoTopo)
        drawRoundRect(Brush.verticalGradient(listOf(rosa, rosaEscuro), corpoTopo, corpoTopo + corpo.height), corpoEsq, corpo, CornerRadius(w * 0.06f))
        drawRect(Color.Black.copy(alpha = 0.08f), Offset(corpoEsq.x, corpoTopo), Size(corpo.width, h * 0.06f))
        drawRect(Brush.horizontalGradient(listOf(fitaEscura, fita, fitaEscura), w * 0.42f, w * 0.58f), Offset(w * 0.42f, corpoTopo), Size(w * 0.16f, corpo.height))
        drawRoundRect(contorno, corpoEsq, corpo, CornerRadius(w * 0.06f), style = traco)
        // Tampa
        val tampaTopo = h * 0.30f
        val tampa = Size(w * 0.80f, h * 0.15f)
        val tampaEsq = Offset(w * 0.10f, tampaTopo)
        drawRoundRect(Brush.verticalGradient(listOf(Color(0xFFFF7FA6), rosa), tampaTopo, tampaTopo + tampa.height), tampaEsq, tampa, CornerRadius(w * 0.05f))
        drawRect(fita, Offset(w * 0.42f, tampaTopo), Size(w * 0.16f, tampa.height))
        // Brilho da tampa
        drawRoundRect(Color.White.copy(alpha = 0.45f), Offset(w * 0.15f, tampaTopo + tampa.height * 0.18f), Size(w * 0.22f, tampa.height * 0.24f), CornerRadius(tampa.height * 0.12f))
        drawRoundRect(contorno, tampaEsq, tampa, CornerRadius(w * 0.05f), style = traco)
        // Laço: duas alças, as pontas da fita e o nó
        val no = Offset(w * 0.5f, tampaTopo)
        for (lado in listOf(-1f, 1f)) {
            val ponta = Path().apply {
                moveTo(no.x, no.y)
                lineTo(no.x + lado * w * 0.14f, no.y + h * 0.13f)
                lineTo(no.x + lado * w * 0.07f, no.y + h * 0.14f)
                close()
            }
            drawPath(ponta, fitaEscura)
            val alca = Path().apply {
                moveTo(no.x, no.y)
                cubicTo(no.x + lado * w * 0.02f, no.y - h * 0.36f, no.x + lado * w * 0.48f, no.y - h * 0.24f, no.x + lado * w * 0.05f, no.y + h * 0.02f)
                close()
            }
            drawPath(alca, fita)
            drawPath(alca, contorno, style = traco)
        }
        drawCircle(fitaEscura, w * 0.06f, no)
        drawCircle(Color.White.copy(alpha = 0.5f), w * 0.022f, no + Offset(-w * 0.018f, -w * 0.018f))
        drawCircle(contorno, w * 0.06f, no, style = traco)
    }
}
