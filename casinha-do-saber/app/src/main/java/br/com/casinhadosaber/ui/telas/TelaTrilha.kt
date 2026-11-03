package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.PROPORCAO_CRIANCA
import br.com.casinhadosaber.ui.componentes.FileiraDeEstrelas
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.launch
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Density
import br.com.casinhadosaber.ui.EscalaDeTablet
import kotlin.math.roundToInt

/** Altura de cada degrau do caminho: bolinha (88 + 6 de base) e plaquinha de até 2 linhas com estrelas. */
private val ALTURA_PASSO = 168.dp
private val TAMANHO_BOLINHA = 88.dp
/** Distância do topo do degrau até o topo da bolinha. */
private val TOPO_BOLINHA = 14.dp
/** Largura reservada para a plaquinha com o nome (cabe inteira mesmo na bolinha mais à esquerda em 360dp). */
private val LARGURA_PLACA = 160.dp
private val ALTURA_CRIANCA = 104.dp
/** Espaço antes do primeiro degrau, dentro da rolagem. */
private val ESPACO_NO_TOPO = 12.dp
/** Espaço no fim da rolagem para o botão de treino não cobrir a última fase. */
private val ESPACO_DO_TREINO = 96.dp

/** A cada [FASES_POR_MARCO] fases, um marco (bandeirinha que vira troféu) ajuda a criança a se orientar. */
private const val FASES_POR_MARCO = 5

/** Tamanho de tela (já com a escala de tablet) em que o caminho fica no tamanho original. */
private val LARGURA_DE_REFERENCIA = 400.dp
private val ALTURA_DE_REFERENCIA = 620.dp

/**
 * Posição horizontal (fração da largura) de cada fase, formando um caminho em zigue-zague.
 * Os saltos de 0,26 da largura deixam o trecho vertical do caminho fora da plaquinha (meia placa
 * de 80 dp mais o pontinho de 7 dp) já em 360 dp: 0,26 x 360 = 93,6 dp.
 */
private val zigueZague = listOf(0.5f, 0.76f, 0.5f, 0.24f)

/** Raio das curvas do caminho (cantos arredondados do traçado em "L" e em "U"). */
private val RAIO_DA_CURVA = 36.dp

/** Paisagem: distância do centro da bolinha da ponta da fileira até a borda (cabe a volta em "U"). */
private val MARGEM_SERPENTE = 120.dp

/**
 * Paisagem: distância horizontal até o trecho de volta, fora da plaquinha (80 dp + pontinho + folga).
 * Com a margem de 120 dp, a volta fica a 24 dp da borda: o pontinho (7 dp de raio) aparece inteiro.
 */
private val AFASTAMENTO_DA_VOLTA = 96.dp

/** Paisagem: distância mínima entre bolinhas vizinhas na mesma fileira (as plaquinhas não se encostam). */
private val ESPACO_MINIMO = 176.dp

/** Constante para desenhar um quarto de círculo com curva de Bézier cúbica. */
private const val KAPPA = 0.5523f

/**
 * Onde cada fase fica no caminho. Em pé: uma fase por degrau, em zigue-zague.
 * Deitada (tela larga): fileiras de [colunas] fases, indo e voltando como num tabuleiro, para
 * caberem muito mais fases na tela de uma vez.
 */
private class Disposicao(val largura: Dp, val colunas: Int) {
    val serpente: Boolean get() = colunas > 1
    private val espaco: Dp = if (colunas > 1) (largura - MARGEM_SERPENTE * 2) / (colunas - 1) else 0.dp

    fun linha(indice: Int): Int = if (serpente) indice / colunas else indice

    fun linhas(total: Int): Int = if (total <= 0) 0 else linha(total - 1) + 1

    fun x(indice: Int): Dp {
        if (!serpente) return largura * zigueZague[indice.mod(zigueZague.size)]
        val l = indice / colunas
        val j = indice % colunas
        val coluna = if (l % 2 == 0) j else colunas - 1 - j
        return MARGEM_SERPENTE + espaco * coluna
    }
}

private fun disposicaoDoCaminho(largura: Dp, altura: Dp): Disposicao {
    if (largura <= altura) return Disposicao(largura, 1)
    val cabem = ((largura - MARGEM_SERPENTE * 2) / ESPACO_MINIMO).toInt() + 1
    return Disposicao(largura, if (cabem < 3) 1 else cabem.coerceAtMost(6))
}

private fun ehMarco(fase: Fase): Boolean = (fase.numero + 1) % FASES_POR_MARCO == 0

@Composable
fun TelaTrilha(
    trilha: Trilha,
    estado: EstadoJogo,
    onVoltar: () -> Unit,
    onJogar: (Fase) -> Unit,
    /** Treino sem fim do caminho (aparece depois da primeira fase vencida). */
    onTreinar: () -> Unit = {},
    /** Mapa grande com os 25 mundos do caminho (sem ele, o botão 🗺️ não aparece). */
    onMapa: (() -> Unit)? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val todas = remember(trilha) { GeradorDeFases().fases(trilha) }
    val atual = todas.firstOrNull { estado.liberada(it) && estado.estrelasDa(it) == 0 }
    // Fase para mostrar ao abrir: a atual ou, com tudo vencido, a última vencida.
    val indiceEmFoco = atual?.numero ?: todas.indexOfLast { estado.estrelasDa(it) > 0 }.coerceAtLeast(0)
    // São 250 fases: o caminho mostra até o fim do mundo seguinte ao da vez (o resto aparece quando a
    // criança chega perto). Menos coisa na tela e sempre um mundo novo para descobrir.
    val fases = remember(todas, indiceEmFoco) {
        todas.take(minOf(todas.size, (indiceEmFoco / Mundos.FASES_POR_MUNDO + 2) * Mundos.FASES_POR_MUNDO))
    }
    val proximoPremio = ItensDaCasa.ordem.getOrNull(estado.fasesVencidas)
    val cor = Color(trilha.corArgb)
    val corEscura = Color(trilha.corEscuraArgb)
    val rolagem = rememberScrollState()

    val mundoAtual = Mundos.daFase(indiceEmFoco)
    val podeTreinar = estado.vencidasNa(trilha) > 0

    LaunchedEffect(trilha) {
        narrador.falar(
            trilha.chamada + " Você está no mundo ${mundoAtual.numero}: ${mundoAtual.nome}. Toque na bolinha que está brilhando." +
                if (podeTreinar) " Ou toque em treinar, para praticar o que já aprendeu." else "",
        )
    }

    // No tablet (também quando a tela é usada sozinha, como nas capturas) tudo cresce na mesma proporção.
    EscalaDeTablet {
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cor, Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BotaoRedondo("⬅️", "Voltar", onVoltar, tamanho = 52.dp)
            if (onMapa != null) {
                Spacer(Modifier.width(8.dp))
                BotaoRedondo("🗺️", "Mapa dos mundos", { sons.toque(); onMapa() }, tamanho = 52.dp)
            }
            Spacer(Modifier.width(12.dp))
            // Com o botão do mapa a barra fica cheia: o emoji do caminho sai (a cor e o nome já dizem qual é).
            if (onMapa == null) {
                Text(trilha.emoji, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.width(8.dp))
            }
            // O nome nunca é cortado: se não couber (celular estreito), a letra diminui um pouco até caber.
            val estilo = MaterialTheme.typography.headlineLarge
            var escalaDoTitulo by remember(trilha) { mutableFloatStateOf(1f) }
            Text(
                trilha.titulo,
                style = estilo.copy(fontSize = estilo.fontSize * escalaDoTitulo),
                color = Color.White,
                maxLines = 1,
                softWrap = false,
                onTextLayout = { if (it.hasVisualOverflow && escalaDoTitulo > 0.6f) escalaDoTitulo -= 0.08f },
                modifier = Modifier.weight(1f),
            )
            if (proximoPremio != null) {
                // Próximo prêmio da casa, como um chip na barra (nada passa por baixo dele).
                Row(
                    Modifier
                        .background(Color.White, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .semantics(mergeDescendants = true) { contentDescription = "Próximo prêmio: ${proximoPremio.nome}" },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("🎁", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(4.dp))
                    Text(proximoPremio.icone, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.pulsando())
                }
            } else {
                Text("${estado.vencidasNa(trilha)}/${todas.size}", style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
        }

        BoxWithConstraints(Modifier.fillMaxSize()) {
            // Caminho largo e alto (tablet em pé): bolinhas, plaquinhas e criança crescem juntas, até 30%,
            // para o caminho não ficar fininho no meio da tela. Na paisagem a altura manda e fica 1.
            val escalaDoCaminho = minOf(maxWidth / LARGURA_DE_REFERENCIA, maxHeight / ALTURA_DE_REFERENCIA).coerceIn(1f, 1.3f)
            val densidadeDeFora = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(densidadeDeFora.density * escalaDoCaminho, densidadeDeFora.fontScale),
            ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
            val alturaVisivel = maxHeight
            val densidade = LocalDensity.current
            val disposicao = remember(maxWidth, maxHeight) { disposicaoDoCaminho(maxWidth, maxHeight) }
            // Ao abrir, a fase atual aparece perto do meio da tela (com 25 fases ela pode estar bem lá embaixo).
            // O topo da área visível cai sempre no começo de um degrau: nenhuma plaquinha fica cortada
            // embaixo do cabeçalho.
            LaunchedEffect(trilha) {
                val centroDaFase = ALTURA_PASSO * disposicao.linha(indiceEmFoco) + TOPO_BOLINHA + TAMANHO_BOLINHA / 2
                // A rolagem não passa do fim do caminho: o degrau escolhido é o último que ainda alinha o topo
                // (perto do fim, arredondar para cima deixava a rolagem parar no meio de uma plaquinha).
                val conteudo = ALTURA_PASSO * disposicao.linhas(fases.size) + 40.dp + ESPACO_NO_TOPO
                val maximo = (conteudo - alturaVisivel).coerceAtLeast(0.dp)
                val ultimoDegrau = ((maximo - ESPACO_NO_TOPO) / ALTURA_PASSO).toInt().coerceAtLeast(0)
                val degraus = ((centroDaFase - alturaVisivel / 2) / ALTURA_PASSO).roundToInt().coerceIn(0, ultimoDegrau)
                // O espaço do topo também sai da tela: ela começa exatamente no começo do degrau.
                val alvo = if (degraus == 0) 0 else with(densidade) { (ALTURA_PASSO * degraus + ESPACO_NO_TOPO).roundToPx() }
                if (alvo > 0) {
                    // Pula para perto e desliza o finalzinho: a criança vê o caminho andando até ela.
                    rolagem.scrollTo((alvo - with(densidade) { 160.dp.roundToPx() }).coerceAtLeast(0))
                    rolagem.animateScrollTo(alvo)
                }
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rolagem)
                    .padding(top = ESPACO_NO_TOPO),
            ) {
                val alturaTotal = ALTURA_PASSO * disposicao.linhas(fases.size) + 40.dp + if (podeTreinar) ESPACO_DO_TREINO else 0.dp
                val corCaminho = Color.White.copy(alpha = 0.85f)
                Canvas(Modifier.fillMaxWidth().height(alturaTotal)) {
                    // Cada mundo (10 fases) numa faixa de cor própria, para a criança ver que entrou num lugar novo.
                    for (m in Mundos.todos) {
                        if (m.primeiraFase >= fases.size) break
                        val topo = ALTURA_PASSO.toPx() * disposicao.linha(m.primeiraFase)
                        val fim = ALTURA_PASSO.toPx() * (disposicao.linha(minOf(m.ultimaFase, fases.lastIndex)) + 1)
                        val margem = 6.dp.toPx()
                        drawRoundRect(
                            Color(m.corArgb).copy(alpha = 0.45f),
                            topLeft = Offset(margem, topo + margem / 2),
                            size = Size(size.width - margem * 2, fim - topo - margem),
                            cornerRadius = CornerRadius(28.dp.toPx()),
                        )
                    }
                    // Só o caminho pontilhado: os marcos (bandeirinha e troféu) já dividem o caminho em pedacinhos.
                    // O traçado nunca desce em diagonal por baixo da bolinha: as plaquinhas ficam embaixo delas,
                    // então ele sai sempre de lado, na altura do centro da bolinha, e só desce longe da plaquinha.
                    val caminho = caminhoDasFases(disposicao, fases.size, ALTURA_PASSO.toPx(), TOPO_BOLINHA.toPx() + TAMANHO_BOLINHA.toPx() / 2)
                    drawPath(
                        caminho,
                        corCaminho,
                        style = Stroke(
                            width = 14.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(1.dp.toPx(), 17.dp.toPx())),
                        ),
                    )
                }
                for (fase in fases) {
                    val liberada = estado.liberada(fase)
                    val estrelas = estado.estrelasDa(fase)
                    val eAtual = fase == atual
                    val cx = disposicao.x(fase.numero)
                    val topo = ALTURA_PASSO * disposicao.linha(fase.numero) + TOPO_BOLINHA
                    Box(
                        Modifier
                            .offset(x = cx - LARGURA_PLACA / 2, y = topo)
                            .width(LARGURA_PLACA),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        BolinhaDaFase(
                            fase = fase,
                            cor = cor,
                            corEscura = corEscura,
                            liberada = liberada,
                            estrelas = estrelas,
                            atual = eAtual,
                            marco = ehMarco(fase),
                            onTocar = {
                                if (liberada) {
                                    sons.toque()
                                    onJogar(fase)
                                } else {
                                    sons.erro()
                                    narrador.falar("Essa ainda está fechada. Termine a fase de antes!")
                                }
                            },
                        )
                    }
                    if (Mundos.comecaMundo(fase) && !disposicao.serpente) {
                        // Plaquinha do mundo no alto do degrau, acima do caminho e do lado oposto ao da criança.
                        val mundo = Mundos.de(fase)
                        val direita = disposicao.x(fase.numero + 1) > cx
                        val inicioX = if (direita) cx + TAMANHO_BOLINHA / 2 + 6.dp else 4.dp
                        val larguraMax = if (direita) disposicao.largura - inicioX - 4.dp else cx - TAMANHO_BOLINHA / 2 - 10.dp
                        Row(
                            Modifier
                                .offset(x = inicioX, y = ALTURA_PASSO * disposicao.linha(fase.numero) + 4.dp)
                                .widthIn(max = larguraMax)
                                .background(Color.White.copy(alpha = 0.92f), RoundedCornerShape(50))
                                .border(2.dp, Color(mundo.corArgb), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .semantics(mergeDescendants = true) { contentDescription = mundo.titulo },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(mundo.emoji, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Mundo ${mundo.numero}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = corEscura,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (eAtual) {
                        // A criança fica ao lado da fase atual, sempre do lado com mais espaço (nunca sai da tela).
                        val n = fase.numero
                        val lado = if (disposicao.serpente) {
                            // Na fileira, fica no vão entre a fase atual e a vizinha (de pé no caminho).
                            val vizinha = when {
                                n + 1 < fases.size && disposicao.linha(n + 1) == disposicao.linha(n) -> n + 1
                                n > 0 && disposicao.linha(n - 1) == disposicao.linha(n) -> n - 1
                                else -> -1
                            }
                            when {
                                vizinha >= 0 -> if (disposicao.x(vizinha) > cx) 1 else -1
                                cx > disposicao.largura / 2 -> -1
                                else -> 1
                            }
                        } else {
                            val fracao = cx / disposicao.largura
                            when {
                                fracao < 0.5f -> 1
                                fracao > 0.5f -> -1
                                disposicao.x(n + 1) > cx -> -1
                                else -> 1
                            }
                        }
                        val larguraCrianca = ALTURA_CRIANCA * PROPORCAO_CRIANCA
                        val x = if (lado > 0) cx + TAMANHO_BOLINHA / 2 + 10.dp else cx - TAMANHO_BOLINHA / 2 - 10.dp - larguraCrianca
                        estado.perfil?.let { perfil ->
                            Crianca(
                                perfil,
                                Modifier
                                    .offset(x = x, y = topo - 8.dp)
                                    .height(ALTURA_CRIANCA),
                                acenando = true,
                            )
                        }
                    }
                }
                Spacer(Modifier.height(alturaTotal))
            }
            }
            }
            // Esmaecido no topo, alto o bastante para nada parecer cortado embaixo do cabeçalho.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(Brush.verticalGradient(listOf(cor, cor.copy(alpha = 0f)))),
            )
            if (podeTreinar) {
                // Esmaecido embaixo: as bolinhas que passam por trás do botão somem suavemente.
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(ESPACO_DO_TREINO + 16.dp)
                        .background(Brush.verticalGradient(listOf(Cores.Fundo.copy(alpha = 0f), Cores.Fundo.copy(alpha = 0.92f), Cores.Fundo))),
                )
                // Treino sem fim: botão grande, sempre à mão embaixo do caminho.
                BotaoFofo(
                    onClick = {
                        sons.toque()
                        onTreinar()
                    },
                    cor = Color.White,
                    corBase = corEscura,
                    descricao = "Treinar: perguntas sem fim das fases que você já venceu",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                ) {
                    Row(
                        Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("🏋️", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(8.dp))
                        Text("Treinar", style = MaterialTheme.typography.headlineSmall, color = corEscura, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    }
}

/**
 * O traçado do caminho, passando pelo centro de cada bolinha (o trecho embaixo da bolinha fica escondido).
 * Em pé: sai de lado até a coluna da próxima fase, faz a curva e desce reto até ela ("L").
 * Deitada: reto dentro da fileira e, na ponta, uma volta em "U" por fora das plaquinhas.
 * Assim os pontinhos nunca passam por trás dos nomes das fases.
 */
private fun DrawScope.caminhoDasFases(disposicao: Disposicao, total: Int, passo: Float, centroY: Float): Path {
    val caminho = Path()
    if (total <= 0) return caminho
    val raio = RAIO_DA_CURVA.toPx()
    val solto = raio * (1f - KAPPA)
    fun xDa(i: Int) = disposicao.x(i).toPx()
    fun yDa(i: Int) = passo * disposicao.linha(i) + centroY
    caminho.moveTo(xDa(0), yDa(0))
    for (i in 0 until total - 1) {
        val x0 = xDa(i)
        val y0 = yDa(i)
        val x1 = xDa(i + 1)
        val y1 = yDa(i + 1)
        when {
            disposicao.linha(i) == disposicao.linha(i + 1) -> caminho.lineTo(x1, y1)
            disposicao.serpente -> {
                // Volta em "U" do lado de fora da ponta da fileira.
                val s = if (x0 > size.width / 2) 1f else -1f
                val xv = x0 + s * AFASTAMENTO_DA_VOLTA.toPx()
                caminho.lineTo(xv - s * raio, y0)
                caminho.cubicTo(xv - s * solto, y0, xv, y0 + solto, xv, y0 + raio)
                caminho.lineTo(xv, y1 - raio)
                caminho.cubicTo(xv, y1 - solto, xv - s * solto, y1, xv - s * raio, y1)
                caminho.lineTo(x1, y1)
            }
            else -> {
                // "L": de lado na altura da bolinha, curva e desce reto até o topo da próxima.
                val s = if (x1 > x0) 1f else -1f
                caminho.lineTo(x1 - s * raio, y0)
                caminho.cubicTo(x1 - s * solto, y0, x1, y0 + solto, x1, y0 + raio)
                caminho.lineTo(x1, y1)
            }
        }
    }
    return caminho
}

@Composable
private fun BolinhaDaFase(
    fase: Fase,
    cor: Color,
    corEscura: Color,
    liberada: Boolean,
    estrelas: Int,
    atual: Boolean,
    marco: Boolean,
    onTocar: () -> Unit,
) {
    val tremida = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            BotaoFofo(
                onClick = {
                    onTocar()
                    if (!liberada) {
                        escopo.launch {
                            for (x in listOf(10f, -10f, 6f, -6f, 0f)) tremida.animateTo(x, tween(50))
                        }
                    }
                },
                cor = if (liberada) Color.White else Color(0xFFE0E0E0),
                corBase = if (liberada) corEscura else Color(0xFFBDBDBD),
                forma = CircleShape,
                descricao = "Fase ${fase.numero + 1}, ${fase.titulo}" + when {
                    !liberada -> ", fechada"
                    estrelas > 0 -> ", $estrelas estrelas"
                    else -> ""
                },
                modifier = Modifier
                    .graphicsLayer { translationX = tremida.value }
                    .then(if (atual) Modifier.pulsando() else Modifier),
            ) {
                Box(
                    Modifier
                        .size(TAMANHO_BOLINHA - 6.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (liberada) {
                        Canvas(Modifier.fillMaxSize()) {
                            // Marco: anel dourado mais grosso, para destacar a cada 5 fases.
                            if (marco) drawCircle(Cores.Estrela, style = Stroke(8.dp.toPx()))
                            drawCircle(cor, radius = size.minDimension / 2 - (if (marco) 6.dp.toPx() else 0f), style = Stroke(5.dp.toPx()))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(fase.emoji, style = MaterialTheme.typography.headlineLarge)
                            Text("${fase.numero + 1}", style = MaterialTheme.typography.titleMedium, color = corEscura)
                        }
                        if (estrelas > 0) {
                            // Fase vencida: um "certinho" na cor do caminho.
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .size(26.dp)
                                    .background(cor, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) { Text("✔", color = Color.White, style = MaterialTheme.typography.titleMedium) }
                        }
                    } else {
                        // Fechada: mostra o desenho da fase apagadinho e um cadeado pequeno.
                        if (marco) {
                            Canvas(Modifier.fillMaxSize()) { drawCircle(Cores.Estrela.copy(alpha = 0.45f), style = Stroke(6.dp.toPx())) }
                        }
                        Text(fase.emoji, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.graphicsLayer { alpha = 0.3f })
                        Text("🔒", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.BottomEnd))
                    }
                }
            }
            if (marco) {
                // Bandeirinha do marco; vira troféu quando a fase é vencida.
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-6).dp, y = (-4).dp)
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                        .border(3.dp, Cores.Estrela, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (estrelas > 0) "🏆" else "🚩", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        // Nome da fase numa plaquinha (não se mistura com o caminho pontilhado), e as estrelas embaixo.
        Column(
            Modifier
                .padding(top = 2.dp)
                .widthIn(max = LARGURA_PLACA)
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(14.dp))
                .padding(horizontal = 10.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                fase.titulo,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = if (liberada) Cores.Texto else Cores.TextoSuave,
            )
            if (estrelas > 0) FileiraDeEstrelas(estrelas, tamanho = 14.dp)
        }
    }
}
