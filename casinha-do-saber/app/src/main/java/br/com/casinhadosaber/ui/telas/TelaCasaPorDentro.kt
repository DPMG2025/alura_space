package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.audio.SonsDaCasa
import br.com.casinhadosaber.dominio.Acao
import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Caixa
import br.com.casinhadosaber.dominio.Categoria
import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Estrutura
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Mapa
import br.com.casinhadosaber.dominio.Movel
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.PlantaDaCasa
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.Produto
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Forma
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.PROPORCAO_CRIANCA
import br.com.casinhadosaber.ui.componentes.Projecao
import br.com.casinhadosaber.ui.componentes.caixa
import br.com.casinhadosaber.ui.componentes.caminhoEstrela
import br.com.casinhadosaber.ui.componentes.clarear
import br.com.casinhadosaber.ui.componentes.desenharCrianca
import br.com.casinhadosaber.ui.componentes.desenharFormas
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.componentes.sombrearFormas
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Altura da criança, nas unidades de altura da planta (a parede tem 3). */
private const val ALTURA_CRIANCA = 2.15f

/** Velocidade da criança andando, em ladrilhos por segundo. */
private const val VELOCIDADE = 2.6f

private val Contorno = Color(0x33000000)
private val Escuro = Color(0x990E1530)
private val Madeira = Color(0xFFC98B5B)
private val MadeiraClara = Color(0xFFE6B98F)
private val Branco = Color(0xFFF7F9FB)
private val Metal = Color(0xFFB0BEC5)
private val CoresDoArcoIris = listOf(Color(0xFFEF476F), Color(0xFFFFA62B), Color(0xFFFFD166), Color(0xFF3DBE6B), Color(0xFF4D96FF))

/**
 * A casa por dentro, em perspectiva isométrica: a criança anda pelos cômodos (toque no chão),
 * abre a geladeira e o guarda-roupa, acende as luzes, liga a TV e a torneira. A lojinha daqui vende
 * comidas (para a geladeira) e roupas (para o guarda-roupa).
 */
@Composable
fun TelaCasaPorDentro(
    estado: EstadoJogo,
    onAtualizar: ((EstadoJogo) -> EstadoJogo) -> Unit,
    onVoltar: () -> Unit,
    /** Abre o espelho (a tela do perfil) para trocar a aparência. */
    onEspelho: () -> Unit = {},
    /** Para as capturas de tela: já abre com a geladeira, o guarda-roupa ou a lojinha abertos. */
    abrirInicial: Acao = Acao.NENHUMA,
    lojaInicial: Boolean = false,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val sonsCasa = remember(sons) { SonsDaCasa(sons) }
    DisposableEffect(sonsCasa) { onDispose { sonsCasa.desligar() } }
    val medidor = rememberTextMeasurer()
    val cena = remember {
        Cena().apply {
            geladeiraAberta = abrirInicial == Acao.GELADEIRA
            guardaRoupaAberto = abrirInicial == Acao.GUARDA_ROUPA
        }
    }
    var lojaAberta by remember { mutableStateOf(lojaInicial) }
    var confete by remember { mutableIntStateOf(0) }
    var jaAndou by remember { mutableStateOf(false) }

    // Experimentar antes de comprar (igual à loja de fora): a roupa aparece na criança por alguns segundos.
    var previa by remember { mutableStateOf<Produto?>(null) }
    LaunchedEffect(previa) {
        if (previa != null) {
            delay(6000)
            previa = null
        }
    }
    val estadoNaCena = remember(estado, previa) { previa?.let { p -> estado.copy(moedas = Int.MAX_VALUE / 2).usar(p) } ?: estado }
    val perfil = estadoNaCena.perfil ?: Perfil()
    val moveis = remember(estado.fasesVencidas) { PlantaDaCasa.moveisDe(estado) }
    val mapa = remember(moveis) { Mapa(moveis) }
    val temGeladeira = moveis.any { it.acao == Acao.GELADEIRA }
    val geometria = remember { RefGeometria() }

    LaunchedEffect(Unit) {
        if (abrirInicial == Acao.NENHUMA && !lojaInicial) {
            narrador.falar(listOf(pt("Que casa linda por dentro! Toque no chão para andar. Em inglês, casa é:"), en("house")))
        }
    }

    // Laço de quadros: anda pelo caminho, faz a câmera seguir e passa o tempo das animações.
    // Só mexe em estados lidos no desenho: não recompõe a tela a cada quadro.
    LaunchedEffect(Unit) {
        var ultimo = -1L
        var distancia = 0f
        while (true) {
            withFrameNanos { agora ->
                val dt = if (ultimo < 0) 0f else ((agora - ultimo) / 1e9f).coerceAtMost(0.05f)
                ultimo = agora
                cena.tempo += dt
                val caminho = cena.caminho
                if (cena.indice < caminho.size) {
                    val alvo = caminho[cena.indice]
                    val dx = alvo.x - cena.x
                    val dy = alvo.y - cena.y
                    val d = hypot(dx, dy)
                    val passo = VELOCIDADE * dt
                    if (abs(dx - dy) > 0.02f) cena.virado = dx - dy < 0
                    if (d <= passo) {
                        cena.x = alvo.x
                        cena.y = alvo.y
                        cena.indice++
                        if (cena.indice >= caminho.size) {
                            cena.andando = false
                            val chegar = cena.aoChegar
                            cena.aoChegar = null
                            chegar?.invoke()
                        }
                    } else {
                        cena.x += dx / d * passo
                        cena.y += dy / d * passo
                        cena.andando = true
                    }
                    cena.passada += passo
                    distancia += passo
                    if (distancia > 0.75f) {
                        distancia = 0f
                        sonsCasa.passo()
                    }
                }
                val g = geometria.g
                if (g != null && cena.seguir) {
                    val alvoCam = ((cena.x - cena.y) * g.proj.meioL).coerceIn(-g.camMax, g.camMax)
                    cena.camX += (alvoCam - cena.camX) * min(1f, dt * 3.5f)
                }
            }
        }
    }

    fun andarPara(p: Posicao, aoChegar: (() -> Unit)? = null) {
        val c = mapa.caminho(Posicao(cena.x, cena.y), p)
        cena.caminho = c
        cena.indice = 0
        cena.seguir = true
        cena.aoChegar = aoChegar
        if (c.isEmpty()) {
            cena.aoChegar = null
            aoChegar?.invoke()
        } else {
            val fim = c.last()
            cena.marca = fim
            cena.marcaEm = cena.tempo
        }
    }

    fun falarNome(m: Movel, extra: String? = null) {
        narrador.falar(listOf(pt("${m.nome}! Em inglês:"), en(m.nomeEn)) + (extra?.let { listOf(pt(it)) } ?: emptyList()))
    }

    fun interagir(m: Movel) {
        cena.tocadoId = m.id
        cena.tocadoEm = cena.tempo
        val t = cena.tempo
        when (m.acao) {
            Acao.GELADEIRA -> {
                sonsCasa.porta()
                cena.geladeiraAberta = !cena.geladeiraAberta
                cena.guardaRoupaAberto = false
                if (cena.geladeiraAberta) {
                    val comidas = Loja.naGeladeira(estado)
                    falarNome(m, if (comidas.isEmpty()) "Está vazia! Compre comidas na lojinha." else "Toque numa comida para comer!")
                }
            }
            Acao.GUARDA_ROUPA -> {
                sonsCasa.porta()
                cena.guardaRoupaAberto = !cena.guardaRoupaAberto
                cena.geladeiraAberta = false
                if (cena.guardaRoupaAberto) falarNome(m, "Escolha uma roupa!")
            }
            Acao.TORNEIRA -> {
                sonsCasa.agua()
                cena.aguaAte = t + 1.8f
                narrador.falar(listOf(pt("Água! Vamos lavar as mãos. Em inglês:"), en("water")))
            }
            Acao.TV -> {
                cena.tvLigada = !cena.tvLigada
                if (cena.tvLigada) {
                    sonsCasa.tv()
                    narrador.falar(listOf(pt("Televisão ligada! Em inglês:"), en("TV")))
                } else {
                    sonsCasa.clique()
                    narrador.falar("Televisão desligada.")
                }
            }
            Acao.FOGAO -> {
                sonsCasa.chiado()
                cena.fogaoAte = t + 5f
                narrador.falar(listOf(pt("Hora de cozinhar! Cuidado, é quente! Fogão em inglês:"), en("stove")))
            }
            Acao.BANHEIRA -> {
                sonsCasa.bolhas()
                cena.bolhasAte = t + 3.5f
                narrador.falar(listOf(pt("Hora do banho, com bolhas! Em inglês:"), en("bath")))
            }
            Acao.CAMA -> {
                cena.dormindoAte = t + 3.5f
                narrador.falar(listOf(pt("Que soninho! Boa noite! Em inglês:"), en("Good night!")))
            }
            Acao.DESCARGA -> {
                sonsCasa.descarga()
                cena.descargaAte = t + 1.8f
                narrador.falar("Puxou a descarga! Agora lava as mãos!")
            }
            Acao.ESPELHO -> {
                sons.toque()
                narrador.falar(listOf(pt("Espelho! Em inglês:"), en("mirror")))
                onEspelho()
                return
            }
            Acao.NENHUMA -> {
                sons.toque()
                falarNome(m)
            }
        }
        if (!jaAndou) jaAndou = true
        // A criança vai até o móvel (sem atravessar paredes).
        andarPara(mapa.pertoDo(m))
    }

    fun alternarLuz(c: Comodo) {
        sonsCasa.clique()
        val apagada = c in cena.luzApagada
        cena.luzApagada = if (apagada) cena.luzApagada - c else cena.luzApagada + c
        narrador.falar(if (apagada) listOf(pt("Luz acesa!"), en("Light on!")) else listOf(pt("Luz apagada!"), en("Light off!")))
    }

    fun tocarNaCena(toque: Offset) {
        val g = geometria.g ?: return
        val wx = toque.x + cena.camX
        val wy = toque.y
        // 1) lâmpadas
        for (c in Comodo.entries) {
            val l = g.lampadas[c.ordinal]
            if (hypot(wx - l.x, wy - l.y) < g.raioLampada) {
                alternarLuz(c)
                return
            }
        }
        // 2) a própria criança
        val pe = g.proj.ponto(cena.x, cena.y)
        if (wx in pe.x - g.larguraCrianca / 2..pe.x + g.larguraCrianca / 2 && wy in pe.y - g.alturaCrianca..pe.y) {
            cena.puloEm = cena.tempo
            sons.toque()
            val nome = estado.perfil?.nome.orEmpty()
            narrador.falar(
                listOf(
                    if (nome.isBlank()) "Oi! Eu moro aqui!" else "Oi! Eu sou $nome e moro aqui!",
                    "Que casa gostosa!",
                    "Vamos passear pelos cômodos?",
                    "Iupiii!",
                ).random(),
            )
            return
        }
        // 3) móveis, da frente para trás
        for (i in g.pecas.indices.reversed()) {
            val p = g.pecas[i]
            val m = p.movel ?: continue
            if (p.toque.contains(Offset(wx, wy))) {
                interagir(m)
                return
            }
        }
        for (p in g.naParede) {
            val m = p.movel ?: continue
            if (p.toque.contains(Offset(wx, wy))) {
                interagir(m)
                return
            }
        }
        // 4) o chão: anda até lá
        val gx = g.proj.gradeX(wx, wy)
        val gy = g.proj.gradeY(wx, wy)
        val lado = PlantaDaCasa.LADO.toFloat()
        if (gx in -0.3f..lado + 0.3f && gy in -0.3f..lado + 0.3f) {
            jaAndou = true
            sons.toque()
            andarPara(Posicao(gx.coerceIn(0.05f, lado - 0.05f), gy.coerceIn(0.05f, lado - 0.05f)))
        }
    }

    val tocar by rememberUpdatedState<(Offset) -> Unit>({ o -> tocarNaCena(o) })

    fun comprar(produto: Produto) {
        val jaTinha = produto.id in estado.comprados
        val preco = Loja.preco(produto, estado)
        if (produto.categoria == Categoria.COMIDA && !temGeladeira) {
            sons.erro()
            narrador.falar("Primeiro a casa precisa da geladeira! Ela vem num presente. Vamos jogar?")
            return
        }
        if (!jaTinha && previa?.id != produto.id) {
            previa = produto
            sons.toque()
            if (produto.categoria == Categoria.COMIDA) {
                cena.comendo = produto.valor
                cena.comendoAte = cena.tempo + 6f
            }
            val nome = listOf(pt("${produto.nome}! Em inglês:"), en(produto.nomeEn.ifBlank { produto.nome }))
            narrador.falar(
                nome + pt(
                    if (estado.moedas >= preco) "Custa $preco moedinhas. Toque de novo para comprar." else "Faltam ${preco - estado.moedas} moedinhas.",
                ),
            )
            return
        }
        previa = null
        val novo = estado.usar(produto)
        if (novo == null) {
            sons.erro()
            val faltam = preco - estado.moedas
            narrador.falar(if (faltam == 1) "Falta só uma moedinha! Jogue mais um pouquinho." else "Faltam $faltam moedinhas! Cada acerto de primeira vale moedinha.")
            return
        }
        onAtualizar { it.usar(produto) ?: it }
        if (jaTinha) {
            sons.toque()
            return
        }
        confete++
        sons.moeda()
        when (produto.categoria) {
            Categoria.COMIDA -> {
                cena.tocadoId = "geladeira"
                cena.tocadoEm = cena.tempo
                narrador.falar("Oba! ${produto.nome}! Guardei na geladeira.")
            }
            Categoria.ROUPA -> narrador.falar("Oba! ${produto.nome}! Ficou lindo! Está no guarda-roupa.")
            else -> narrador.falar("Oba! ${produto.nome}!")
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFB9E4FF), Color(0xFFDDF3D0), Color(0xFFBFE3A8))))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BotaoRedondo("⬅️", "Voltar para o jardim", onVoltar, tamanho = 52.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "🪙 ${estado.moedas}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .semantics { contentDescription = "Você tem ${estado.moedas} moedinhas" },
                )
                Spacer(Modifier.weight(1f))
                BotaoRedondo("🪞", "Espelho: trocar a aparência", { sons.toque(); onEspelho() }, tamanho = 52.dp)
                Spacer(Modifier.width(8.dp))
                BotaoRedondo(
                    "🛒",
                    if (lojaAberta) "Fechar a lojinha" else "Lojinha de comidas e roupas",
                    {
                        lojaAberta = !lojaAberta
                        cena.geladeiraAberta = false
                        cena.guardaRoupaAberto = false
                        sons.toque()
                        if (lojaAberta) narrador.falar("Lojinha! Comidas para a geladeira e roupas para o guarda-roupa.")
                    },
                    cor = if (lojaAberta) Cores.Estrela else Color.White,
                    tamanho = 52.dp,
                )
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                CenaIsometrica(
                    estado = estadoNaCena,
                    perfil = perfil,
                    moveis = moveis,
                    cena = cena,
                    medidor = medidor,
                    ref = geometria,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = "Casa por dentro. Toque no chão para andar." }
                        .pointerInput(Unit) { detectTapGestures(onTap = { tocar(it) }) }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, arrasto ->
                                change.consume()
                                val g = geometria.g ?: return@detectHorizontalDragGestures
                                cena.seguir = false
                                cena.camX = (cena.camX - arrasto).coerceIn(-g.camMax, g.camMax)
                            }
                        },
                )
                if (!jaAndou && abrirInicial == Acao.NENHUMA && !lojaInicial) {
                    MaoGuia(Modifier.align(Alignment.Center).padding(top = 120.dp), espera = 2500)
                }
                if (cena.geladeiraAberta) {
                    PainelGeladeira(
                        comidas = Loja.naGeladeira(estado),
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onComer = { c ->
                            sonsCasa.nham()
                            cena.comendo = c.valor
                            cena.comendoAte = cena.tempo + 2.5f
                            narrador.falar(listOf(pt("Nham nham! ${c.nome}! Em inglês:"), en(c.nomeEn)))
                        },
                        onLoja = {
                            cena.geladeiraAberta = false
                            lojaAberta = true
                            sons.toque()
                        },
                        onFechar = {
                            sonsCasa.porta()
                            cena.geladeiraAberta = false
                        },
                    )
                }
                if (cena.guardaRoupaAberto) {
                    PainelGuardaRoupa(
                        estado = estado,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onVestir = { traje ->
                            onAtualizar { it.vestir(traje) }
                            cena.puloEm = cena.tempo
                            sons.colocar()
                            val t = Loja.trajes.firstOrNull { it.id == traje }
                            narrador.falar(if (t == null) listOf(pt("A roupa de sempre!")) else listOf(pt("${t.nome}! Em inglês:"), en(t.nomeEn)))
                        },
                        onExperimentar = { p ->
                            cena.guardaRoupaAberto = false
                            lojaAberta = true
                            comprar(p)
                        },
                        onFechar = {
                            sonsCasa.porta()
                            cena.guardaRoupaAberto = false
                        },
                    )
                }
            }

            if (lojaAberta) {
                Lojinha(
                    estado = estado,
                    previa = previa,
                    temGeladeira = temGeladeira,
                    onComprar = { p -> comprar(p) },
                )
            }
        }
        Confete(confete)
    }
}

/** Estado da cena que muda a cada quadro ou a cada toque. Lido só no desenho, para não recompor a tela toda. */
@Stable
private class Cena {
    var x by mutableFloatStateOf(PlantaDaCasa.entrada.x)
    var y by mutableFloatStateOf(PlantaDaCasa.entrada.y)
    var tempo by mutableFloatStateOf(0f)
    var camX by mutableFloatStateOf(0f)
    var passada by mutableFloatStateOf(0f)
    var andando by mutableStateOf(false)
    var virado by mutableStateOf(false)
    var seguir = true
    var caminho: List<Posicao> = emptyList()
    var indice = 0
    var aoChegar: (() -> Unit)? = null
    var marca by mutableStateOf<Posicao?>(null)
    var marcaEm by mutableFloatStateOf(-10f)
    var puloEm by mutableFloatStateOf(-10f)
    var luzApagada by mutableStateOf(emptySet<Comodo>())
    var geladeiraAberta by mutableStateOf(false)
    var guardaRoupaAberto by mutableStateOf(false)
    var tvLigada by mutableStateOf(false)
    var fogaoAte by mutableFloatStateOf(-1f)
    var aguaAte by mutableFloatStateOf(-1f)
    var bolhasAte by mutableFloatStateOf(-1f)
    var descargaAte by mutableFloatStateOf(-1f)
    var dormindoAte by mutableFloatStateOf(-1f)
    var comendo by mutableStateOf<String?>(null)
    var comendoAte by mutableFloatStateOf(-1f)
    var tocadoId by mutableStateOf<String?>(null)
    var tocadoEm by mutableFloatStateOf(-10f)
}

/** A geometria pronta (guardada para os toques saberem onde está cada coisa). */
private class RefGeometria {
    var g: Geometria? = null
}

/** Um móvel (ou pedaço de parede) já convertido em formas para desenhar. */
private class Peca(
    val movel: Movel?,
    val caixa: Caixa,
    val comodo: Comodo?,
    val formas: List<Forma>,
    val toque: Rect,
    val ancora: Offset,
    val fechado: List<Forma> = emptyList(),
    val aberto: List<Forma> = emptyList(),
    val emoji: TextLayoutResult? = null,
    val emojiEm: Offset = Offset.Zero,
    /** Pontos usados pelas animações (bico da torneira, bocas do fogão...). */
    val pontos: List<Offset> = emptyList(),
    val tela: Path? = null,
    val telaRect: Rect = Rect.Zero,
    val brilho: Brush? = null,
    val emojisAbertos: List<Pair<TextLayoutResult, Offset>> = emptyList(),
)

private class Geometria(
    val proj: Projecao,
    val camMax: Float,
    val chao: List<Forma>,
    val chaoDoComodo: Array<Path>,
    val paredes: List<Forma>,
    val paredesDoComodo: Array<List<Forma>>,
    val padrao: Path?,
    val corDoPadrao: Color,
    val naParede: List<Peca>,
    val pecas: List<Peca>,
    val lampadas: Array<Offset>,
    val cordas: Array<Offset>,
    val cupulas: Array<Path>,
    val brilhoDasLampadas: Array<Brush>,
    val raioLampada: Float,
    val alturaCrianca: Float,
    val larguraCrianca: Float,
    val chapeu: TextLayoutResult?,
    val chapeuFonte: Float,
    val comidas: Map<String, TextLayoutResult>,
    val zzz: TextLayoutResult,
    val sombraDaCasa: Rect,
    val traco: Stroke,
    val tracoFino: Stroke,
)

@Composable
private fun CenaIsometrica(
    estado: EstadoJogo,
    perfil: Perfil,
    moveis: List<Movel>,
    cena: Cena,
    medidor: TextMeasurer,
    ref: RefGeometria,
    modifier: Modifier,
) {
    Box(
        modifier.drawWithCache {
            val g = montarGeometria(size, this, estado, perfil, moveis, medidor)
            ref.g = g
            onDrawBehind { desenharCena(g, cena, perfil) }
        },
    )
}

// ---------------------------------------------------------------------------------------------
// Montagem da geometria (uma vez por tamanho de tela e por mudança no estado)
// ---------------------------------------------------------------------------------------------

private fun medirEmoji(medidor: TextMeasurer, densidade: Density, emoji: String, px: Float): TextLayoutResult =
    medidor.measure(
        AnnotatedString(emoji),
        style = TextStyle(fontSize = (px / (densidade.density * densidade.fontScale)).sp, textAlign = TextAlign.Center),
    )

private fun corDoPiso(c: Comodo): Pair<Color, Color> = when (c) {
    Comodo.QUARTO -> Color(0xFFEBCB9F) to Color(0xFFDDB98A)
    Comodo.BANHEIRO -> Color(0xFFE6F6FC) to Color(0xFFC3E5F2)
    Comodo.SALA -> Color(0xFFF2D3A6) to Color(0xFFE4BF8E)
    Comodo.COZINHA -> Color(0xFFFFF4E0) to Color(0xFFF4C9A6)
}

private fun emojiDe(id: String): String? = when (id) {
    "ursinho" -> "🧸"
    "violao" -> "🎸"
    "livros" -> "📚"
    "planta" -> "🪴"
    "gato" -> "🐱"
    "bolo" -> "🎂"
    "vaso" -> "🚽"
    else -> null
}

private fun montarGeometria(
    tamanho: Size,
    densidade: Density,
    estado: EstadoJogo,
    perfil: Perfil,
    moveis: List<Movel>,
    medidor: TextMeasurer,
): Geometria {
    val lado = PlantaDaCasa.LADO.toFloat()
    val h = PlantaDaCasa.ALTURA_PAREDE
    // O ladrilho cresce até a casa ocupar a altura; em celular a casa fica mais larga que a tela e a câmera segue a criança.
    val tw = min(tamanho.height / 6.5f, tamanho.width / 8.4f * 1.3f).coerceAtLeast(1f)
    val alturaTotal = lado * tw / 2f + (h + 0.35f) * tw * 0.56f
    val origemY = (tamanho.height - alturaTotal) / 2f + h * tw * 0.56f + tw * 0.1f
    val proj = Projecao(tw, tamanho.width / 2f, origemY)
    val camMax = max(0f, lado * proj.meioL + tw * 0.25f - tamanho.width / 2f)
    val dp = densidade.density
    val traco = Stroke(max(1f, 1.2f * dp))
    val tracoFino = Stroke(max(1f, 0.8f * dp))

    // ---- chão
    val chao = ArrayList<Forma>()
    val chaoDoComodo = Array(4) { Path() }
    // espessura do piso (a casa parece uma maquete)
    chao += Forma(proj.paredeX(lado, 0f, lado, -0.35f, 0f), Color(0xFFB98A5E))
    chao += Forma(proj.paredeY(lado, 0f, lado, -0.35f, 0f), Color(0xFFD6A574))
    for (c in Comodo.entries) {
        val l = PlantaDaCasa.limitesDo(c)
        val (claro, escuro) = corDoPiso(c)
        val pClaro = Path()
        val pEscuro = Path()
        with(proj) {
            for (cy in l.y0.toInt() until l.y1.toInt()) for (cx in l.x0.toInt() until l.x1.toInt()) {
                (if ((cx + cy) % 2 == 0) pClaro else pEscuro).losango(cx.toFloat(), cy.toFloat(), cx + 1f, cy + 1f)
            }
        }
        chao += Forma(pClaro, claro, contorno = false)
        chao += Forma(pEscuro, escuro, contorno = false)
        chaoDoComodo[c.ordinal] = proj.piso(l.x0, l.y0, l.x1, l.y1)
    }
    for (m in moveis) if (m.noChao) {
        chao += Forma(proj.piso(m.x0, m.y0, m.x1, m.y1, 0.01f), Color(0xFFB9A8FF), contorno = false)
        chao += Forma(proj.piso(m.x0 + 0.15f, m.y0 + 0.15f, m.x1 - 0.15f, m.y1 - 0.15f, 0.012f), Color(0xFFD4CAFF), contorno = false)
        chao += Forma(proj.piso(m.x0 + 0.5f, m.y0 + 0.5f, m.x1 - 0.5f, m.y1 - 0.5f, 0.014f), Color(0xFFFFD6E8), contorno = false)
    }

    // ---- paredes do fundo (com a cor de cada cômodo, a mesma da casa de fora)
    val paredes = ArrayList<Forma>()
    val paredesDoComodo = Array(4) { ArrayList<Forma>() }
    val m4 = PlantaDaCasa.MEIO.toFloat()
    fun paredeFundoY(c: Comodo, x0: Float, x1: Float) {
        val cor = Color(estado.corDo(c))
        val f = listOf(
            Forma(proj.paredeY(0f, x0, x1, 0f, h), cor, contorno = false),
            Forma(proj.paredeY(0f, x0, x1, 0f, 0.16f), escurecer(cor, 0.82f), contorno = false),
        )
        paredes += f
        paredesDoComodo[c.ordinal].addAll(f)
    }
    fun paredeFundoX(c: Comodo, y0: Float, y1: Float) {
        val cor = escurecer(Color(estado.corDo(c)), 0.9f)
        val f = listOf(
            Forma(proj.paredeX(0f, y0, y1, 0f, h), cor, contorno = false),
            Forma(proj.paredeX(0f, y0, y1, 0f, 0.16f), escurecer(cor, 0.82f), contorno = false),
        )
        paredes += f
        paredesDoComodo[c.ordinal].addAll(f)
    }
    paredeFundoY(Comodo.QUARTO, 0f, m4)
    paredeFundoY(Comodo.BANHEIRO, m4, lado)
    paredeFundoX(Comodo.QUARTO, 0f, m4)
    paredeFundoX(Comodo.SALA, m4, lado)
    // janelas (quando a casa já tem janelas)
    if (estado.temEstrutura(Estrutura.JANELAS)) {
        val vidro = Color(0xFFBDE7FF)
        val moldura = Color(0xFFFFFFFF)
        for ((a, b) in listOf(0.3f to 1.3f, 6.2f to 7.4f)) {
            val c = if (a < m4) Comodo.QUARTO else Comodo.BANHEIRO
            val f = listOf(
                Forma(proj.paredeY(0f, a - 0.08f, b + 0.08f, 1.32f, 2.58f), moldura),
                Forma(proj.paredeY(0f, a, b, 1.4f, 2.5f), vidro, contorno = false),
                Forma(proj.paredeY(0f, (a + b) / 2 - 0.03f, (a + b) / 2 + 0.03f, 1.4f, 2.5f), moldura, contorno = false),
                Forma(proj.paredeY(0f, a, b, 1.92f, 1.98f), moldura, contorno = false),
            )
            paredes += f
            paredesDoComodo[c.ordinal].addAll(f)
        }
        val (a, b) = 4.3f to 5.3f
        val f = listOf(
            Forma(proj.paredeX(0f, a - 0.08f, b + 0.08f, 1.32f, 2.58f), moldura),
            Forma(proj.paredeX(0f, a, b, 1.4f, 2.5f), vidro, contorno = false),
            Forma(proj.paredeX(0f, (a + b) / 2 - 0.03f, (a + b) / 2 + 0.03f, 1.4f, 2.5f), moldura, contorno = false),
            Forma(proj.paredeX(0f, a, b, 1.92f, 1.98f), moldura, contorno = false),
        )
        paredes += f
        paredesDoComodo[Comodo.SALA.ordinal].addAll(f)
    }
    // espessura e acabamento no alto das paredes
    val acabamento = Color(0xFFFDF3E4)
    paredes += Forma(proj.piso(-0.14f, -0.14f, lado, 0f, h), acabamento)
    paredes += Forma(proj.piso(-0.14f, 0f, 0f, lado, h), acabamento)
    paredes += Forma(proj.paredeY(lado, -0.14f, 0f, -0.35f, h), Color(0xFFE8D6BE))
    paredes += Forma(proj.paredeX(lado, -0.14f, 0f, -0.35f, h), Color(0xFFD9C3A5))

    // papel de parede comprado na loja (listras, bolinhas ou estrelinhas)
    val padrao: Path? = when (estado.papelDeParede) {
        1 -> Path().apply {
            var t = 0.25f
            while (t < lado) {
                addPath(proj.paredeY(0f, t - 0.06f, t + 0.06f, 0.16f, h))
                addPath(proj.paredeX(0f, t - 0.06f, t + 0.06f, 0.16f, h))
                t += 0.5f
            }
        }
        2, 3 -> Path().apply {
            var t = 0.35f
            var linha = 0
            while (t < lado) {
                var z = 0.5f + (linha % 2) * 0.3f
                while (z < h - 0.2f) {
                    for (p in listOf(proj.ponto(t, 0f, z), proj.ponto(0f, t, z))) {
                        if (estado.papelDeParede == 2) addOval(Rect(p, tw * 0.05f)) else addPath(caminhoEstrela(p, tw * 0.07f))
                    }
                    z += 0.6f
                }
                t += 0.6f
                linha++
            }
        }
        else -> null
    }
    val corDoPadrao = if (estado.papelDeParede == 3) Color(0x80FFC93C) else Color(0x1F000000)

    // ---- móveis
    val naParede = ArrayList<Peca>()
    val soltas = ArrayList<Peca>()
    for (m in moveis) {
        if (m.noChao) continue
        val p = montarPeca(m, proj, estado, perfil, medidor, densidade)
        if (m.naParede) naParede += p else soltas += p
    }
    // pedaços das paredes internas (baixinhas, cortadas para ver dentro)
    for (c in PlantaDaCasa.paredesInternas) {
        val cor = Color(0xFFF3E6D3)
        soltas += Peca(
            movel = null,
            caixa = c,
            comodo = null,
            formas = proj.caixa(c.x0, c.y0, c.x1, c.y1, c.z0, c.z1, cor),
            toque = Rect.Zero,
            ancora = Offset.Zero,
        )
    }
    val ordem = PlantaDaCasa.ordenarParaDesenho(soltas.map { it.caixa })
    val pecas = ordem.map { soltas[it] }

    // ---- lâmpadas penduradas
    val lampadas = Array(4) { i ->
        val c = PlantaDaCasa.centroDo(Comodo.entries[i])
        proj.ponto(c.x, c.y, 2.62f)
    }
    val cordas = Array(4) { i ->
        val c = PlantaDaCasa.centroDo(Comodo.entries[i])
        proj.ponto(c.x, c.y, 3.5f)
    }
    val cupulas = Array(4) { i ->
        val p = lampadas[i]
        val r = tw * 0.2f
        Path().apply {
            moveTo(p.x - r * 0.35f, p.y - r * 0.9f)
            lineTo(p.x + r * 0.35f, p.y - r * 0.9f)
            lineTo(p.x + r, p.y - r * 0.05f)
            lineTo(p.x - r, p.y - r * 0.05f)
            close()
        }
    }
    val brilho = Array(4) { i ->
        Brush.radialGradient(listOf(Color(0x99FFF3B0), Color(0x00FFF3B0)), center = lampadas[i], radius = tw * 0.9f)
    }

    // ---- criança, chapéu e comidas na mão
    val alturaCrianca = ALTURA_CRIANCA * proj.unidadeZ
    val larguraCrianca = alturaCrianca * PROPORCAO_CRIANCA
    val chapeuFonte = larguraCrianca * 0.34f * 2f * 0.9f * 0.82f
    val chapeu = perfil.chapeu.takeIf { it.isNotEmpty() }?.let { medirEmoji(medidor, densidade, it, chapeuFonte) }
    val comidas = Loja.comidas.associate { (e, _, _) -> e to medirEmoji(medidor, densidade, e, larguraCrianca * 0.42f) }
    val zzz = medirEmoji(medidor, densidade, "💤", larguraCrianca * 0.5f)
    val fim = proj.ponto(lado, lado)
    val sombraDaCasa = Rect(proj.x(0f, lado) - tw * 0.6f, fim.y - tw * 0.6f, proj.x(lado, 0f) + tw * 0.6f, fim.y + tw * 0.5f)

    return Geometria(
        proj = proj,
        camMax = camMax,
        chao = chao,
        chaoDoComodo = chaoDoComodo,
        paredes = paredes,
        paredesDoComodo = Array(4) { paredesDoComodo[it] },
        padrao = padrao,
        corDoPadrao = corDoPadrao,
        naParede = naParede,
        pecas = pecas,
        lampadas = lampadas,
        cordas = cordas,
        cupulas = cupulas,
        brilhoDasLampadas = brilho,
        raioLampada = max(tw * 0.4f, 26f * dp),
        alturaCrianca = alturaCrianca,
        larguraCrianca = larguraCrianca,
        chapeu = chapeu,
        chapeuFonte = chapeuFonte,
        comidas = comidas,
        zzz = zzz,
        sombraDaCasa = sombraDaCasa,
        traco = traco,
        tracoFino = tracoFino,
    )
}

/** Converte um móvel em formas prontas (desenho isométrico feito de caixinhas). */
private fun montarPeca(m: Movel, proj: Projecao, estado: EstadoJogo, perfil: Perfil, medidor: TextMeasurer, densidade: Density): Peca {
    val (x0, y0, x1, y1) = listOf(m.x0, m.y0, m.x1, m.y1)
    val z0 = m.z
    val z1 = m.z + m.altura
    val cx = m.centroX
    val cy = m.centroY
    val tw = proj.ladrilho
    var toque = proj.contorno(x0, y0, x1, y1, z0, z1)
    // Área de toque de pelo menos 48dp (dedos de criança).
    val minimo = 48f * densidade.density
    if (toque.width < minimo || toque.height < minimo) {
        val c = toque.center
        toque = Rect(c.x - max(toque.width, minimo) / 2, c.y - max(toque.height, minimo) / 2, c.x + max(toque.width, minimo) / 2, c.y + max(toque.height, minimo) / 2)
    }
    val ancora = proj.ponto(cx, cy, z0)
    val caixaDoMovel = PlantaDaCasa.caixaDo(m)
    fun peca(
        formas: List<Forma>,
        fechado: List<Forma> = emptyList(),
        aberto: List<Forma> = emptyList(),
        pontos: List<Offset> = emptyList(),
        tela: Path? = null,
        brilho: Brush? = null,
        emojisAbertos: List<Pair<TextLayoutResult, Offset>> = emptyList(),
    ) = Peca(
        m, caixaDoMovel, m.comodo, formas, toque, ancora, fechado, aberto,
        pontos = pontos, tela = tela, telaRect = tela?.getBounds() ?: Rect.Zero, brilho = brilho, emojisAbertos = emojisAbertos,
    )

    emojiDe(m.id)?.let { e ->
        val escala = when (m.id) {
            "planta" -> 0.85f
            "vaso" -> 0.8f
            "violao" -> 0.8f
            "gato" -> 0.6f
            "ursinho" -> 0.62f
            else -> 0.55f
        }
        val layout = medirEmoji(medidor, densidade, e, tw * escala)
        val base = proj.ponto(cx, cy, z0)
        val sombra = Path().apply {
            addOval(Rect(base.x - tw * escala * 0.42f, base.y - tw * 0.07f, base.x + tw * escala * 0.42f, base.y + tw * 0.07f))
        }
        val em = Offset(base.x - layout.size.width / 2f, base.y - layout.size.height * 0.9f)
        val t = Rect(em, Size(layout.size.width.toFloat(), layout.size.height.toFloat()))
        val c = t.center
        val tq = Rect(c.x - max(t.width, minimo) / 2, c.y - max(t.height, minimo) / 2, c.x + max(t.width, minimo) / 2, c.y + max(t.height, minimo) / 2)
        return Peca(m, caixaDoMovel, m.comodo, listOf(Forma(sombra, Color(0x26000000), contorno = false)), tq, base, emoji = layout, emojiEm = em)
    }

    return with(proj) {
        when (m.id) {
            "cama" -> {
                val cobertor = Color(perfil.corDaRoupa)
                peca(
                    caixa(x0, y0, x1, y0 + 0.14f, 0f, 1.05f, Madeira) +
                        caixa(x0, y0 + 0.14f, x1, y1, 0f, 0.3f, Madeira) +
                        caixa(x0 + 0.04f, y0 + 0.14f, x1 - 0.04f, y1 - 0.04f, 0.3f, 0.47f, Branco) +
                        caixa(x0 + 0.16f, y0 + 0.22f, x1 - 0.16f, y0 + 0.62f, 0.47f, 0.6f, Color.White) +
                        caixa(x0 + 0.02f, y0 + 0.85f, x1 - 0.02f, y1 - 0.02f, 0.3f, 0.52f, clarear(cobertor, 0.25f)) +
                        Forma(paredeY(y1 - 0.02f + 0.001f, x0 + 0.02f, x1 - 0.02f, 0.38f, 0.44f), escurecer(cobertor, 0.9f), contorno = false),
                )
            }
            "criado" -> peca(
                caixa(x0, y0, x1, y1, 0f, 0.5f, Madeira) +
                    Forma(paredeY(y1 + 0.001f, x0 + 0.08f, x1 - 0.08f, 0.26f, 0.29f), escurecer(Madeira, 0.7f), contorno = false),
            )
            "abajur" -> {
                val r = 0.2f
                peca(
                    caixa(cx - 0.08f, cy - 0.08f, cx + 0.08f, cy + 0.08f, z0, z0 + 0.06f, Color(0xFFD4A24C)) +
                        caixa(cx - 0.02f, cy - 0.02f, cx + 0.02f, cy + 0.02f, z0 + 0.06f, z0 + 0.42f, Color(0xFFD4A24C)) +
                        caixa(cx - r, cy - r, cx + r, cy + r, z0 + 0.4f, z0 + 0.62f, Color(0xFFFFE08A)),
                    brilho = Brush.radialGradient(listOf(Color(0x80FFF0A0), Color(0x00FFF0A0)), center = ponto(cx, cy, z0 + 0.5f), radius = tw * 0.7f),
                    pontos = listOf(ponto(cx, cy, z0 + 0.5f)),
                )
            }
            "guardaroupa" -> {
                val xm = (x0 + x1) / 2
                val meia = (x1 - x0) / 2 - 0.04f
                val portaCor = Color(0xFFD9A06E)
                val roupas = ArrayList<Forma>()
                // Cabides: as roupas que a criança tem (as compradas primeiro, depois as cores de sempre).
                val cores = Loja.noGuardaRoupa(estado).map { corDoTraje(it.valor, perfil) } + Aparencia.roupas.map { Color(it) }
                var a = x0 + 0.14f
                var i = 0
                while (a + 0.2f < x1 - 0.1f && i < cores.size) {
                    roupas += Forma(paredeY(y1 - 0.05f, a, a + 0.2f, 0.95f, 1.75f), cores[i])
                    a += 0.26f
                    i++
                }
                peca(
                    caixa(x0, y0, x1, y1, 0f, 2.2f, Madeira) + caixa(x0 - 0.04f, y0, x1 + 0.04f, y1 + 0.04f, 2.2f, 2.3f, escurecer(Madeira, 0.85f)),
                    fechado = listOf(
                        Forma(paredeY(y1 + 0.001f, x0 + 0.06f, xm - 0.02f, 0.08f, 2.12f), portaCor),
                        Forma(paredeY(y1 + 0.001f, xm + 0.02f, x1 - 0.06f, 0.08f, 2.12f), portaCor),
                        Forma(paredeY(y1 + 0.002f, xm - 0.12f, xm - 0.07f, 1.0f, 1.3f), Color(0xFF8D5A3B), contorno = false),
                        Forma(paredeY(y1 + 0.002f, xm + 0.07f, xm + 0.12f, 1.0f, 1.3f), Color(0xFF8D5A3B), contorno = false),
                    ),
                    aberto = listOf(
                        Forma(paredeY(y1 + 0.001f, x0 + 0.06f, x1 - 0.06f, 0.08f, 2.12f), Color(0xFF5A3A22)),
                        Forma(paredeY(y1 - 0.04f, x0 + 0.08f, x1 - 0.08f, 1.78f, 1.82f), Metal, contorno = false),
                    ) + roupas + listOf(
                        Forma(paredeX(x0 + 0.06f, y1, y1 + meia, 0.08f, 2.12f), clarear(portaCor, 0.15f)),
                        Forma(paredeX(x1 - 0.06f, y1, y1 + meia, 0.08f, 2.12f), portaCor),
                    ),
                )
            }
            "pia" -> {
                val bico = ponto(cx, y0 + 0.3f, 1.0f)
                val bacia = ponto(cx, y0 + 0.36f, 0.84f)
                peca(
                    caixa(x0, y0, x1, y1, 0f, 0.75f, Color.White) +
                        caixa(x0 - 0.03f, y0, x1 + 0.03f, y1 + 0.04f, 0.75f, 0.83f, Color(0xFFB9D7E8)) +
                        Forma(piso(cx - 0.32f, y0 + 0.16f, cx + 0.32f, y1 - 0.08f, 0.831f), Color(0xFF9CC9DD)) +
                        caixa(cx - 0.04f, y0 + 0.05f, cx + 0.04f, y0 + 0.13f, 0.83f, 1.06f, Metal) +
                        caixa(cx - 0.03f, y0 + 0.13f, cx + 0.03f, y0 + 0.3f, 1.0f, 1.06f, Metal),
                    pontos = listOf(bico, bacia),
                )
            }
            "espelho" -> peca(
                listOf(
                    Forma(paredeY(0.005f, x0 - 0.06f, x1 + 0.06f, z0 - 0.06f, z1 + 0.06f), Color(0xFFD4A24C)),
                    Forma(paredeY(0.006f, x0, x1, z0, z1), Color(0xFFCFEFFF), contorno = false),
                    Forma(paredeY(0.007f, x0 + 0.15f, x0 + 0.25f, z0 + 0.2f, z1 - 0.1f), Color(0x99FFFFFF), contorno = false),
                ),
            )
            "quadro" -> peca(
                listOf(
                    Forma(paredeX(0.005f, y0 - 0.06f, y1 + 0.06f, z0 - 0.06f, z1 + 0.06f), Color(0xFF8D5A3B)),
                    Forma(paredeX(0.006f, y0, y1, z0, z1), Color(0xFFBDE7FF), contorno = false),
                    Forma(paredeX(0.007f, y0, y1, z0, z0 + 0.3f), Color(0xFF7CC56B), contorno = false),
                    Forma(Path().apply { addOval(Rect(ponto(0.007f, y0 + 0.35f, z1 - 0.2f), tw * 0.07f)) }, Color(0xFFFFC93C), contorno = false),
                ),
            )
            "banheira" -> peca(
                caixa(x0, y0, x1, y1, 0f, 0.6f, Color.White) +
                    caixa(x0 + 0.1f, y0, x1, y0 + 0.08f, 0.6f, 0.62f, Branco) +
                    Forma(piso(x0 + 0.12f, y0 + 0.12f, x1 - 0.12f, y1 - 0.12f, 0.601f), Color(0xFF8FD3FF), contorno = false) +
                    caixa(x1 - 0.35f, y0 + 0.02f, x1 - 0.28f, y0 + 0.1f, 0.6f, 0.85f, Metal),
                pontos = listOf(ponto(cx, cy, 0.6f), ponto(x0 + 0.2f, cy, 0.6f), ponto(x1 - 0.2f, cy, 0.6f)),
            )
            "tv" -> {
                val tela = paredeY(y1 - 0.08f + 0.002f, x0 + 0.12f, x1 - 0.12f, 0.56f, 1.24f)
                peca(
                    caixa(x0, y0, x1, y1, 0f, 0.45f, Color(0xFF8D5A3B)) +
                        caixa(x0 + 0.06f, y1 - 0.2f, x1 - 0.06f, y1 - 0.08f, 0.5f, 1.3f, Color(0xFF2B2B35)) +
                        caixa((x0 + x1) / 2 - 0.08f, y1 - 0.18f, (x0 + x1) / 2 + 0.08f, y1 - 0.1f, 0.45f, 0.5f, Color(0xFF2B2B35)) +
                        Forma(tela, Color(0xFF15151C), contorno = false),
                    tela = tela,
                )
            }
            "sofa" -> {
                val cor = Color(0xFFFF8A80)
                val meioY = (y0 + y1) / 2
                peca(
                    caixa(x0, y0, x1, y1, 0f, 0.36f, escurecer(cor, 0.9f)) +
                        caixa(x0, y0, x0 + 0.28f, y1, 0.36f, 0.88f, cor) +
                        caixa(x0 + 0.28f, y0, x1, y0 + 0.25f, 0.36f, 0.6f, cor) +
                        caixa(x0 + 0.28f, y0 + 0.27f, x1 - 0.03f, meioY - 0.02f, 0.36f, 0.5f, clarear(cor, 0.2f)) +
                        caixa(x0 + 0.28f, meioY + 0.02f, x1 - 0.03f, y1 - 0.27f, 0.36f, 0.5f, clarear(cor, 0.2f)) +
                        caixa(x0 + 0.28f, y1 - 0.25f, x1, y1, 0.36f, 0.6f, cor),
                )
            }
            "geladeira" -> {
                val profundidade = y1 - y0
                val comidas = Loja.naGeladeira(estado)
                val emojis = ArrayList<Pair<TextLayoutResult, Offset>>()
                comidas.take(9).forEachIndexed { i, c ->
                    val prateleira = i / 3
                    val z = 0.12f + prateleira * 0.5f
                    val y = y0 + 0.18f + (i % 3) * (profundidade - 0.3f) / 2f
                    val l = medirEmoji(medidor, densidade, c.valor, tw * 0.26f)
                    val p = ponto(x1, y, z)
                    emojis += l to Offset(p.x - l.size.width / 2f, p.y - l.size.height * 0.92f)
                }
                peca(
                    caixa(x0, y0, x1, y1, 0f, 2.0f, Color(0xFFF4F7FA)),
                    fechado = listOf(
                        Forma(paredeX(x1 + 0.001f, y0 + 0.04f, y1 - 0.04f, 1.3f, 1.34f), Color(0xFFC9D3DB), contorno = false),
                        Forma(paredeX(x1 + 0.002f, y0 + 0.1f, y0 + 0.16f, 0.75f, 1.2f), Metal, contorno = false),
                        Forma(paredeX(x1 + 0.002f, y0 + 0.1f, y0 + 0.16f, 1.45f, 1.75f), Metal, contorno = false),
                    ),
                    aberto = listOf(
                        Forma(paredeX(x1 + 0.001f, y0 + 0.06f, y1 - 0.06f, 0.08f, 1.94f), Color(0xFFDDF3FF)),
                        Forma(paredeX(x1 + 0.002f, y0 + 0.06f, y1 - 0.06f, 0.6f, 0.63f), Color.White, contorno = false),
                        Forma(paredeX(x1 + 0.002f, y0 + 0.06f, y1 - 0.06f, 1.1f, 1.13f), Color.White, contorno = false),
                        Forma(paredeX(x1 + 0.002f, y0 + 0.06f, y1 - 0.06f, 1.5f, 1.53f), Color.White, contorno = false),
                        Forma(paredeY(y0, x1, x1 + profundidade - 0.02f, 0.05f, 1.95f), Color(0xFFE8EEF2)),
                        Forma(paredeY(y0 + 0.001f, x1 + 0.08f, x1 + profundidade - 0.1f, 0.5f, 0.56f), Color(0xFFB9D7E8), contorno = false),
                        Forma(paredeY(y0 + 0.001f, x1 + 0.08f, x1 + profundidade - 0.1f, 1.2f, 1.26f), Color(0xFFB9D7E8), contorno = false),
                    ),
                    emojisAbertos = emojis,
                )
            }
            "fogao" -> {
                val bocas = listOf(
                    Posicao(x0 + 0.22f, y0 + 0.22f), Posicao(x1 - 0.22f, y0 + 0.22f),
                    Posicao(x0 + 0.22f, y1 - 0.22f), Posicao(x1 - 0.22f, y1 - 0.22f),
                )
                val formas = caixa(x0, y0, x1, y1, 0f, 0.85f, Color.White) +
                    Forma(paredeX(x1 + 0.001f, y0 + 0.15f, y1 - 0.15f, 0.18f, 0.6f), Color(0xFF3A3A44)) +
                    Forma(paredeX(x1 + 0.002f, y0 + 0.15f, y1 - 0.15f, 0.66f, 0.7f), Metal, contorno = false) +
                    bocas.map { b -> Forma(piso(b.x - 0.12f, b.y - 0.12f, b.x + 0.12f, b.y + 0.12f, 0.851f), Color(0xFF4A4A55), contorno = false) }
                peca(formas, pontos = bocas.map { ponto(it.x, it.y, 0.86f) })
            }
            "mesa" -> {
                val cor = MadeiraClara
                val perna = 0.07f
                peca(
                    caixa(x0 + 0.08f, y0 + 0.08f, x0 + 0.08f + perna, y0 + 0.08f + perna, 0f, 0.64f, Madeira) +
                        caixa(x1 - 0.08f - perna, y0 + 0.08f, x1 - 0.08f, y0 + 0.08f + perna, 0f, 0.64f, Madeira) +
                        caixa(x0 + 0.08f, y1 - 0.08f - perna, x0 + 0.08f + perna, y1 - 0.08f, 0f, 0.64f, Madeira) +
                        caixa(x1 - 0.08f - perna, y1 - 0.08f - perna, x1 - 0.08f, y1 - 0.08f, 0f, 0.64f, Madeira) +
                        caixa(x0, y0, x1, y1, 0.64f, 0.72f, cor) +
                        Forma(piso(x0 + 0.2f, y0 + 0.2f, x1 - 0.2f, y1 - 0.2f, 0.721f), Color(0x55FFFFFF), contorno = false),
                )
            }
            else -> peca(caixa(x0, y0, x1, y1, z0, z1, MadeiraClara))
        }
    }
}

private fun corDoTraje(traje: String, perfil: Perfil): Color = when (traje) {
    "pijama" -> Color(0xFF3B4C8C)
    "abelha" -> Color(0xFFFFC93C)
    "astronauta" -> Color(0xFFF2F4F8)
    "heroi" -> Color(0xFFE63946)
    "fada" -> Color(0xFFD9C2FF)
    else -> clarear(Color(perfil.corDaRoupa), 0.3f)
}

// ---------------------------------------------------------------------------------------------
// Desenho de cada quadro (sem montar caminhos novos; só lê a geometria pronta e o estado da cena)
// ---------------------------------------------------------------------------------------------

private fun DrawScope.desenharCena(g: Geometria, cena: Cena, perfil: Perfil) {
    val t = cena.tempo
    val apagadas = cena.luzApagada
    translate(left = -cena.camX) {
        // sombra da casa no gramado
        drawOval(Color(0x2A2E5A1E), topLeft = g.sombraDaCasa.topLeft, size = g.sombraDaCasa.size)
        // chão e paredes do fundo
        desenharFormas(g.chao, Contorno, g.tracoFino)
        for (c in Comodo.entries) if (c in apagadas) drawPath(g.chaoDoComodo[c.ordinal], Escuro)
        desenharFormas(g.paredes, Contorno, g.tracoFino)
        g.padrao?.let { drawPath(it, g.corDoPadrao) }
        for (c in Comodo.entries) if (c in apagadas) sombrearFormas(g.paredesDoComodo[c.ordinal], Escuro)
        for (i in g.naParede.indices) desenharPeca(g, g.naParede[i], cena, apagadas)

        // marca de onde a criança vai
        val marca = cena.marca
        val idadeMarca = t - cena.marcaEm
        if (marca != null && idadeMarca < 0.7f) {
            val p = g.proj.ponto(marca.x, marca.y)
            val r = g.proj.ladrilho * (0.15f + idadeMarca * 0.4f)
            drawOval(
                Color.White.copy(alpha = 0.9f * (1f - idadeMarca / 0.7f)),
                topLeft = Offset(p.x - r, p.y - r / 2f), size = Size(r * 2f, r),
                style = g.traco,
            )
        }

        // móveis e paredes internas, do fundo para a frente, com a criança no lugar certo
        val x = cena.x
        val y = cena.y
        for (i in g.pecas.indices) {
            val p = g.pecas[i]
            if (PlantaDaCasa.criancaNaFrente(x, y, p.caixa)) desenharPeca(g, p, cena, apagadas)
        }
        desenharCriancaNaCena(g, cena, perfil)
        for (i in g.pecas.indices) {
            val p = g.pecas[i]
            if (!PlantaDaCasa.criancaNaFrente(x, y, p.caixa)) desenharPeca(g, p, cena, apagadas)
        }

        // lâmpadas penduradas (tocar acende e apaga)
        for (c in Comodo.entries) {
            val i = c.ordinal
            val acesa = c !in apagadas
            val l = g.lampadas[i]
            drawLine(Color(0xFF6B5B4B), g.cordas[i], l, strokeWidth = max(1f, g.proj.ladrilho * 0.02f))
            if (acesa) drawCircle(g.brilhoDasLampadas[i], radius = g.proj.ladrilho * 0.9f, center = l)
            drawCircle(if (acesa) Color(0xFFFFF3B0) else Color(0xFFB0B4C0), radius = g.proj.ladrilho * 0.09f, center = Offset(l.x, l.y + g.proj.ladrilho * 0.02f))
            drawPath(g.cupulas[i], if (acesa) Color(0xFFFF9F6E) else Color(0xFFB57B62))
            drawPath(g.cupulas[i], Contorno, style = g.tracoFino)
        }
    }
}

private fun DrawScope.desenharPeca(g: Geometria, p: Peca, cena: Cena, apagadas: Set<Comodo>) {
    val m = p.movel
    val t = cena.tempo
    val idade = if (m != null && cena.tocadoId == m.id) t - cena.tocadoEm else 10f
    if (idade < 0.45f) {
        // pulinho de quem foi tocado
        val s = 1f + 0.14f * sin(idade / 0.45f * PI.toFloat()) * (1f - idade / 0.45f)
        scale(s, s, pivot = p.ancora) { desenharPecaSemPulo(g, p, cena, apagadas) }
    } else {
        desenharPecaSemPulo(g, p, cena, apagadas)
    }
}

private fun DrawScope.desenharPecaSemPulo(g: Geometria, p: Peca, cena: Cena, apagadas: Set<Comodo>) {
    val t = cena.tempo
    val escuro = p.comodo != null && p.comodo in apagadas
    desenharFormas(p.formas, Contorno, g.tracoFino)
    val acao = p.movel?.acao
    val aberto = when (acao) {
        Acao.GELADEIRA -> cena.geladeiraAberta
        Acao.GUARDA_ROUPA -> cena.guardaRoupaAberto
        else -> false
    }
    if (aberto) {
        desenharFormas(p.aberto, Contorno, g.tracoFino)
        for (i in p.emojisAbertos.indices) {
            val (l, em) = p.emojisAbertos[i]
            drawText(l, topLeft = em)
        }
    } else {
        desenharFormas(p.fechado, Contorno, g.tracoFino)
    }
    p.emoji?.let { drawText(it, topLeft = p.emojiEm, alpha = if (escuro) 0.55f else 1f) }
    if (escuro) {
        sombrearFormas(p.formas, Escuro)
        sombrearFormas(if (aberto) p.aberto else p.fechado, Escuro)
    }
    val tw = g.proj.ladrilho
    when (p.movel?.id) {
        "abajur" -> if (!escuro) p.brilho?.let { drawCircle(it, radius = tw * 0.7f, center = p.pontos[0]) }
        "pia" -> if (t < cena.aguaAte) {
            val bico = p.pontos[0]
            val bacia = p.pontos[1]
            val onda = sin(t * 30f) * tw * 0.006f
            drawLine(Color(0xCC4FB3FF), bico, Offset(bacia.x + onda, bacia.y), strokeWidth = tw * 0.045f)
            for (k in 0 until 3) {
                val f = ((t * 3f + k / 3f) % 1f)
                drawCircle(Color(0xAAFFFFFF), tw * 0.015f, Offset(bacia.x + (k - 1) * tw * 0.05f * f, bacia.y - tw * 0.03f * f))
            }
        }
        "banheira" -> if (t < cena.bolhasAte) {
            val base = p.pontos[0]
            val esq = p.pontos[1]
            val dir = p.pontos[2]
            for (k in 0 until 12) {
                val f = ((t * 0.7f + k * 0.083f) % 1f)
                val u = ((k * 0.37f) % 1f)
                val px = esq.x + (dir.x - esq.x) * u + sin(t * 3f + k) * tw * 0.04f
                val py = esq.y + (dir.y - esq.y) * u - f * tw * 0.9f
                drawCircle(Color(0xCCFFFFFF), tw * (0.03f + (k % 3) * 0.015f), Offset(px, py), alpha = 1f - f)
                drawCircle(Color(0x664FB3FF), tw * (0.03f + (k % 3) * 0.015f), Offset(px, py), style = g.tracoFino, alpha = 1f - f)
            }
            drawOval(Color(0x88FFFFFF), topLeft = Offset(base.x - tw * 0.5f, base.y - tw * 0.12f), size = Size(tw, tw * 0.2f))
        }
        "fogao" -> if (t < cena.fogaoAte) {
            for (i in p.pontos.indices) {
                val b = p.pontos[i]
                val tremor = 1f + 0.18f * sin(t * 22f + i * 1.7f)
                drawCircle(Color(0xCC4D96FF), tw * 0.07f, b)
                drawOval(Color(0xE6FFA62B), topLeft = Offset(b.x - tw * 0.04f, b.y - tw * 0.13f * tremor), size = Size(tw * 0.08f, tw * 0.13f * tremor))
            }
        }
        "tv" -> if (cena.tvLigada) {
            val tela = p.tela
            if (tela != null) {
                val r = p.telaRect
                clipPath(tela) {
                    // Desenho animado simples: céu que muda de cor, sol pulando e um arco-íris.
                    val fase = (t * 0.25f) % 1f
                    drawRect(Color.hsv(200f + 40f * sin(fase * 2 * PI.toFloat()), 0.45f, 1f), r.topLeft, r.size)
                    for (k in CoresDoArcoIris.indices) {
                        drawCircle(CoresDoArcoIris[k], r.width * (0.5f - k * 0.07f), Offset(r.center.x, r.bottom + r.height * 0.15f), style = Stroke(r.width * 0.06f))
                    }
                    val pulo = abs(sin(t * 4f)) * r.height * 0.35f
                    drawCircle(Color(0xFFFFE066), r.height * 0.13f, Offset(r.left + r.width * (0.2f + 0.6f * ((t * 0.3f) % 1f)), r.top + r.height * 0.55f - pulo))
                }
            }
        }
        "vaso" -> if (t < cena.descargaAte) {
            val c = p.ancora
            val f = (cena.descargaAte - t) / 1.8f
            drawCircle(Color(0x994FB3FF), tw * 0.12f * f, Offset(c.x, c.y - tw * 0.32f), style = Stroke(tw * 0.03f))
        }
    }
}

private fun DrawScope.desenharCriancaNaCena(g: Geometria, cena: Cena, perfil: Perfil) {
    val t = cena.tempo
    val pe = g.proj.ponto(cena.x, cena.y)
    val altura = g.alturaCrianca
    val largura = g.larguraCrianca
    val andando = cena.andando
    val passada = cena.passada
    // Andando: sobe e desce a cada passo e balança um pouquinho. Pulo quando é tocada ou troca de roupa.
    val balanco = if (andando) abs(sin(passada * PI.toFloat() * 1.6f)) * altura * 0.05f else 0f
    val idadePulo = t - cena.puloEm
    val pulo = if (idadePulo < 0.5f) sin(idadePulo / 0.5f * PI.toFloat()) * altura * 0.18f else 0f
    val dormindo = t < cena.dormindoAte
    val comendo = cena.comendo?.takeIf { t < cena.comendoAte }
    val humor = when {
        comendo != null && ((t * 4f) % 1f) < 0.5f -> Humor.SURPRESO
        else -> Humor.FELIZ
    }
    val olhosFechados = dormindo || (t % 3.8f) > 3.68f
    val topo = pe.y - altura * 0.985f - balanco - pulo
    val esquerda = pe.x - largura / 2f
    // sombra no chão fica no lugar quando pula
    if (pulo > 0f) drawOval(Color(0x22000000), topLeft = Offset(pe.x - largura * 0.35f, pe.y - largura * 0.08f), size = Size(largura * 0.7f, largura * 0.16f))
    translate(esquerda, topo) {
        inset(0f, 0f, size.width - largura, size.height - altura) {
            val inclinar = if (andando) sin(passada * PI.toFloat() * 1.6f) * 3f else 0f
            scale(if (cena.virado) -1f else 1f, 1f, pivot = center) {
                rotate(inclinar, pivot = Offset(size.width / 2f, size.height)) {
                    desenharCrianca(perfil, null, olhosFechados, humor)
                }
            }
            // chapéu (o mesmo cálculo de Crianca.kt)
            g.chapeu?.let { l ->
                val raio = largura * 0.34f
                val baseDoChapeu = altura * 0.27f - raio * 0.45f
                drawText(l, topLeft = Offset(largura / 2f - l.size.width / 2f, baseDoChapeu - l.size.height * 0.95f))
            }
            if (comendo != null) {
                g.comidas[comendo]?.let { l ->
                    val dx = if (cena.virado) largura * 0.05f else largura * 0.95f
                    drawText(l, topLeft = Offset(dx - l.size.width / 2f, altura * 0.42f - l.size.height / 2f))
                }
            }
            if (dormindo) {
                val sobe = ((t * 0.8f) % 1f)
                drawText(g.zzz, topLeft = Offset(largura * 0.7f, -altura * 0.1f - sobe * altura * 0.2f), alpha = 1f - sobe * 0.6f)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Painéis: geladeira aberta, guarda-roupa aberto e a lojinha
// ---------------------------------------------------------------------------------------------

@Composable
private fun Painel(modifier: Modifier, titulo: String, descricao: String, onFechar: () -> Unit, conteudo: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(10.dp)
            .shadow(10.dp, RoundedCornerShape(28.dp))
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(4.dp, Color(0xFFEDE6FF), RoundedCornerShape(28.dp))
            .padding(12.dp)
            .semantics { contentDescription = descricao },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(titulo, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            BotaoRedondo("✖️", "Fechar", onFechar, tamanho = 48.dp)
        }
        Spacer(Modifier.height(8.dp))
        conteudo()
    }
}

@Composable
private fun PainelGeladeira(comidas: List<Produto>, modifier: Modifier, onComer: (Produto) -> Unit, onLoja: () -> Unit, onFechar: () -> Unit) {
    Painel(modifier, "🧊 🍎 🥛", "Geladeira aberta", onFechar) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFDDF3FF), RoundedCornerShape(20.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (comidas.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🫙", fontSize = 48.sp)
                    Spacer(Modifier.weight(1f))
                    BotaoFofo(onClick = onLoja, cor = Cores.Certo, descricao = "Comprar comidas", modifier = Modifier.pulsando()) {
                        Text("🛒 🍎", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
                    }
                }
            } else {
                for (linha in comidas.chunked(5)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (c in linha) {
                            Box(
                                Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .semantics { contentDescription = c.nome }
                                    .clickable { onComer(c) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(c.icone, fontSize = 34.sp)
                            }
                        }
                    }
                    // prateleira
                    Box(Modifier.fillMaxWidth().height(4.dp).background(Color.White, RoundedCornerShape(50)))
                }
            }
        }
    }
}

@Composable
private fun PainelGuardaRoupa(estado: EstadoJogo, modifier: Modifier, onVestir: (String) -> Unit, onExperimentar: (Produto) -> Unit, onFechar: () -> Unit) {
    val perfil = estado.perfil ?: Perfil()
    val minhas = Loja.noGuardaRoupa(estado).map { it.valor }.toSet()
    Painel(modifier, "🚪 👕 👗", "Guarda-roupa aberto", onFechar) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            item(key = "sempre") {
                CabideDaRoupa(perfil.copy(traje = ""), perfil.traje.isEmpty(), "Roupa de sempre", null) { onVestir("") }
            }
            items(Loja.trajes, key = { it.id }) { traje ->
                val tem = traje.id in minhas
                val produto = Loja.porId.getValue("roupa${traje.id}")
                CabideDaRoupa(
                    perfil.copy(traje = traje.id),
                    usando = perfil.traje == traje.id,
                    descricao = traje.nome + if (tem) "" else ", ${Loja.preco(produto, estado)} moedinhas",
                    preco = if (tem) null else Loja.preco(produto, estado),
                ) {
                    if (tem) onVestir(traje.id) else onExperimentar(produto)
                }
            }
        }
    }
}

@Composable
private fun CabideDaRoupa(perfil: Perfil, usando: Boolean, descricao: String, preco: Int?, onClick: () -> Unit) {
    BotaoFofo(
        onClick = onClick,
        cor = if (usando) Color(0xFFDFF7E6) else Color(0xFFFFF6EC),
        corBase = Color(0xFF8E5A34),
        descricao = descricao,
        modifier = Modifier.graphicsLayer { alpha = if (preco == null) 1f else 0.7f },
    ) {
        Column(Modifier.width(84.dp).padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Crianca(perfil, Modifier.height(96.dp))
            Text(
                when {
                    usando -> "✔"
                    preco != null -> "🪙 $preco"
                    else -> "👆"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (usando) Cores.Certo else Cores.Texto,
            )
        }
    }
}

@Composable
private fun Lojinha(estado: EstadoJogo, previa: Produto?, temGeladeira: Boolean, onComprar: (Produto) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Cores.MadeiraClara, Cores.Madeira)),
                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            )
            .border(4.dp, Color(0xFF8E5A34), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(top = 12.dp, bottom = 14.dp),
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Loja.vitrine(estado, Loja.categoriasDeDentro), key = { it.id }) { produto ->
                val tem = produto.id in estado.comprados
                val preco = Loja.preco(produto, estado)
                val trancado = produto.categoria == Categoria.COMIDA && !temGeladeira
                val podeComprar = !trancado && (tem || estado.moedas >= preco)
                val usando = produto.categoria != Categoria.COMIDA && Loja.emUso(produto, estado)
                BotaoFofo(
                    onClick = { onComprar(produto) },
                    cor = if (usando) Color(0xFFDFF7E6) else Color.White,
                    corBase = Color(0xFF8E5A34),
                    descricao = produto.nome + if (tem) ", é seu" else ", $preco moedinhas",
                    modifier = Modifier.graphicsLayer { alpha = if (podeComprar) 1f else 0.5f },
                ) {
                    Column(Modifier.width(92.dp).padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (produto.categoria == Categoria.ROUPA) {
                            Crianca((estado.perfil ?: Perfil()).copy(traje = produto.valor, chapeu = ""), Modifier.height(52.dp))
                        } else {
                            Text(produto.icone, fontSize = 34.sp, maxLines = 1)
                        }
                        Text(
                            when {
                                trancado -> "🔒"
                                usando -> "✔ usando"
                                tem && produto.categoria == Categoria.COMIDA -> "✔ 🧊"
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
        }
    }
}
