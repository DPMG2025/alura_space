package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import br.com.casinhadosaber.ui.componentes.caminhoEstrela
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.MaoGuia
import androidx.compose.foundation.border
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Categoria
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Produto
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.dominio.tirando
import br.com.casinhadosaber.dominio.usa
import br.com.casinhadosaber.dominio.vestindo
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.EnfeiteDoFundo
import br.com.casinhadosaber.ui.componentes.ExplosaoDeEstrelas
import br.com.casinhadosaber.ui.componentes.RaiosDeLuz
import br.com.casinhadosaber.ui.componentes.FundoDecorado
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import br.com.casinhadosaber.ui.componentes.IconeDoItem
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.Irmaozinho
import br.com.casinhadosaber.ui.componentes.Estrela
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.first
import br.com.casinhadosaber.dominio.Crescimento
import br.com.casinhadosaber.dominio.Historia
import br.com.casinhadosaber.ui.componentes.MascoteAnimado

/**
 * Comemoração ao vencer uma fase. Na primeira vitória, um presente aparece:
 * a criança toca para abrir e descobre a peça nova da casa. Depois das 40 peças,
 * o presente é um [brinde] da loja (já aplicado ao estado pelo domínio) e mais moedinhas.
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
        val parabens = if (mensagem != null) br.com.casinhadosaber.dominio.ParteDaFala(pt(mensagem))
        else br.com.casinhadosaber.dominio.VozesDaFamilia.vitoria(nome, estrelas)
        narrador.falarPartes(
            if (temPresente) listOf(parabens, br.com.casinhadosaber.dominio.ParteDaFala(pt("Toque no presente para abrir!"))) else listOf(parabens),
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
    FundoDecorado(corDaTrilha, Modifier.fillMaxSize(), enfeite = EnfeiteDoFundo.Estrelas) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .onGloballyPositioned {
                origemDaTela = it.positionInRoot()
                tamanhoDaTela = Size(it.size.width.toFloat(), it.size.height.toFloat())
            },
    ) {
        // Tela alta: a criança cresce e fica num "palco" (morrinho) que ocupa o terço de baixo.
        val alta = maxHeight > 720.dp
        val centro = centroNaJanela
        val fracao = if (centro != null && tamanhoDaTela.width > 0f && tamanhoDaTela.height > 0f) {
            Offset((centro.x - origemDaTela.x) / tamanhoDaTela.width, (centro.y - origemDaTela.y) / tamanhoDaTela.height)
        } else {
            Offset(0.5f, 0.42f)
        }
        RaiosDeLuz(alfa = 0.32f, quantidade = 14, centroX = fracao.x, centroY = fracao.y)
        Canvas(Modifier.fillMaxSize()) {
            val topo = size.height * 0.80f
            drawOval(corDaTrilha.copy(alpha = 0.16f), Offset(-size.width * 0.35f, topo - size.height * 0.04f), Size(size.width * 1.7f, size.height * 0.5f))
            drawOval(corDaTrilha.copy(alpha = 0.22f), Offset(-size.width * 0.15f, topo), Size(size.width * 1.3f, size.height * 0.4f))
            // Brilho do palco: luz no alto do morrinho, onde a criança comemora.
            drawOval(
                Brush.radialGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), Offset(size.width / 2, topo + size.height * 0.04f), size.width * 0.5f),
                Offset(0f, topo - size.height * 0.08f),
                Size(size.width, size.height * 0.24f),
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // Três blocos (parabéns, presente, criança e botões) distribuídos na altura toda.
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (nome.isBlank()) "Parabéns!" else "Parabéns, $nome!",
                    style = MaterialTheme.typography.headlineLarge.copy(shadow = SOMBRA_DO_TITULO),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = if (!temPresente) Modifier.onGloballyPositioned(::marcarCentro) else Modifier,
                ) {
                    for (i in 1..3) {
                        val escala = remember { Animatable(0.6f) }
                        LaunchedEffect(estrelasVisiveis >= i) {
                            if (estrelasVisiveis >= i) {
                                escala.animateTo(1.3f, tween(140))
                                escala.animateTo(1f, spring(dampingRatio = 0.4f))
                            }
                        }
                        // Cada estrela que pousa estoura em faíscas (o estouro passa por fora da estrela).
                        Box(contentAlignment = Alignment.Center) {
                            Estrela(
                                estrelasVisiveis >= i,
                                Modifier.graphicsLayer { scaleX = escala.value; scaleY = escala.value },
                                tamanho = 64.dp,
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
                    Spacer(Modifier.height(6.dp))
                    Text("+$moedas 🪙", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                }
            }

            // Meio: o presente (ou o recado, quando a fase já tinha dado presente).
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (item != null) {
                    Text(
                        if (aberto) "Você ganhou:" else "Um presente para a sua casa!",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Presente(
                        modifier = Modifier.onGloballyPositioned(::marcarCentro),
                        nomeAberto = item.nome,
                        aberto = aberto,
                        onToque = { sons.chacoalhar(); vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                        onAbrir = ::abrir,
                    ) { m -> IconeDoItem(item, 130.dp, m) }
                    if (aberto) {
                        Text(item.nome, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                        Text("Em inglês: ${item.nomeEn}", style = MaterialTheme.typography.titleMedium, color = Cores.TextoSuave)
                    }
                } else if (brindeDaLoja != null) {
                    Text(
                        if (aberto) "Você ganhou:" else "Um presente da lojinha!",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Presente(
                        modifier = Modifier.onGloballyPositioned(::marcarCentro),
                        nomeAberto = brindeDaLoja.nome,
                        aberto = aberto,
                        onToque = { sons.chacoalhar(); vibracao.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                        onAbrir = ::abrir,
                    ) { m -> DesenhoDoBrinde(brindeDaLoja, 130.dp, m, perfil) }
                    if (aberto) {
                        Text(brindeDaLoja.nome, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(6.dp))
                        // As moedinhas extras do brinde, num selo dourado.
                        Row(
                            Modifier
                                .background(Cores.Estrela, RoundedCornerShape(50))
                                .border(3.dp, Color(0xFFE0A800), RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .semantics(mergeDescendants = true) { contentDescription = "Mais ${EstadoJogo.MOEDAS_DO_BRINDE} moedinhas" },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("+${EstadoJogo.MOEDAS_DO_BRINDE} 🪙", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                } else {
                    Text(
                        "Você já ganhou o presente desta fase.\nJogue uma fase nova para ganhar mais!",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Embaixo: a criança comemorando no morrinho e, depois de abrir, os botões.
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                perfil?.let {
                    // O chapéu, a roupa ou o acessório ganho aparece na criança só depois de abrir (surpresa).
                    val gaveta = brindeDaLoja?.let { b -> Loja.gavetaDe(b) }
                    val vestido = when {
                        brindeDaLoja == null || gaveta == null -> it
                        aberto -> it.vestindo(brindeDaLoja)
                        it.usa(brindeDaLoja) -> it.tirando(gaveta)
                        else -> it
                    }
                    // Com o irmãozinho e o mascote juntos, os três encolhem um pouco para caber lado a lado.
                    val fator = if (it.irmao.ligado && mascote?.especie != null) 0.82f else 1f
                    val alturaCheia = when {
                        !temPresente -> if (alta) 210.dp else 170.dp
                        aberto -> if (alta) 124.dp else 110.dp
                        else -> if (alta) 210.dp else 160.dp
                    }
                    val altura = alturaCheia * fator
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Crianca(vestido, Modifier.height(altura), acenando = true)
                        // O irmãozinho comemora junto, com os bracinhos para cima.
                        if (it.irmao.ligado) {
                            Irmaozinho(it, Modifier.height(altura * 0.66f), humor = Humor.TORCENDO)
                        }
                        // O mascote comemora ao lado da criança, do tamanho do estágio dele.
                        mascote?.especie?.let { e ->
                            MascoteAnimado(e, Crescimento.estagio(fasesDepois), Modifier.size(altura * 0.62f), mascote.acessorioEmUso, mascote.nomeParaMostrar, pulando = true)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                AnimatedVisibility(visible = !temPresente || aberto, enter = fadeIn() + scaleIn()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        if (temPresente) {
                            val texto = if (item != null) "🏠 Colocar na casa" else "🏠 Ver na casa"
                            BotaoFofo(onClick = onColocarNaCasa, cor = Cores.Certo, modifier = Modifier.pulsando(), descricao = texto.removePrefix("🏠 ")) {
                                Text(
                                    texto,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            BotaoRedondo("🗺️", "Voltar para o caminho", onVoltar, tamanho = 60.dp)
                            if (temProxima) {
                                if (!temPresente) {
                                    BotaoFofo(onClick = onProxima, cor = corDaTrilha, corBase = Color(fase.trilha.corEscuraArgb), modifier = Modifier.pulsando(), descricao = "Próxima fase") {
                                        Text("Próxima ▶", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp))
                                    }
                                } else {
                                    BotaoRedondo("▶️", "Próxima fase", onProxima, tamanho = 60.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
        Confete(confete)
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

@Composable
private fun Presente(
    modifier: Modifier = Modifier,
    nomeAberto: String,
    aberto: Boolean,
    onToque: () -> Unit,
    onAbrir: () -> Unit,
    conteudo: @Composable (Modifier) -> Unit,
) {
    var toques by remember { mutableIntStateOf(0) }
    val escala = remember { Animatable(1f) }
    val giro = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()
    val raios = rememberInfiniteTransition(label = "raios")
    val anguloRaios by raios.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "angulo")
    // Mesmo sem tocar, a caixa dá pulinhos chamando a criança.
    val chamar by raios.animateFloat(-4f, 4f, infiniteRepeatable(tween(260), RepeatMode.Reverse), label = "chamar")

    LaunchedEffect(aberto) {
        if (aberto) {
            escala.snapTo(0.2f)
            escala.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow))
        }
    }

    fun tocar() {
        if (aberto) return
        // Um toque só abre o presente.
        toques++
        onAbrir()
    }

    Box(
        modifier
            .size(230.dp)
            .semantics { contentDescription = if (aberto) nomeAberto else "Presente. Toque para abrir." }
            .clickable(enabled = !aberto, role = Role.Button, onClick = ::tocar),
        contentAlignment = Alignment.Center,
    ) {
        if (aberto) {
            Canvas(Modifier.fillMaxSize()) {
                val c = center
                val r = size.minDimension / 2
                rotate(anguloRaios, c) {
                    for (k in 0 until 12) {
                        val a1 = Math.toRadians(k * 30.0)
                        val a2 = Math.toRadians(k * 30.0 + 14)
                        val raio = Path().apply {
                            moveTo(c.x, c.y)
                            lineTo(c.x + (cos(a1) * r).toFloat(), c.y + (sin(a1) * r).toFloat())
                            lineTo(c.x + (cos(a2) * r).toFloat(), c.y + (sin(a2) * r).toFloat())
                            close()
                        }
                        drawPath(raio, Color(0x66FFE680))
                    }
                }
                drawCircle(Color.White.copy(alpha = 0.9f), r * 0.68f, c)
            }
            conteudo(Modifier.graphicsLayer { scaleX = escala.value; scaleY = escala.value })
        } else {
            MaoGuia(Modifier.align(Alignment.BottomEnd), espera = 1200)
            Box(
                Modifier
                    .size(170.dp)
                    .drawBehind { drawCircle(Cores.Sombra, size.minDimension / 2, center.copy(y = center.y + 5.dp.toPx())) }
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFF1ECFF))))
                    .border(3.dp, Color.White, CircleShape),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Caixa desenhada (e não o emoji, que ficava borrado ao crescer): nítida em qualquer tamanho.
                CaixaDePresente(
                    Modifier
                        .size(112.dp)
                        .graphicsLayer {
                            rotationZ = giro.value + if (toques == 0) chamar else 0f
                            scaleX = escala.value
                            scaleY = escala.value
                        },
                )
            }
        }
    }
    Spacer(Modifier.fillMaxWidth().height(8.dp))
}

/** Sombra ameixa suave do título branco. */
private val SOMBRA_DO_TITULO = Shadow(Color(0x553D2C5E), Offset(0f, 4f), 8f)

/**
 * A caixa de presente desenhada: corpo rosa com fita dourada, tampa mais larga, laço de duas alças,
 * brilho no alto e contorno ameixa (mesmo traço dos botões). Vetorial, então não borra ao crescer.
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
