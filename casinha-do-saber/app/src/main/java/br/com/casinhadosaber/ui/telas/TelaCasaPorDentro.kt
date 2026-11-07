package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.audio.SonsDaCasa
import br.com.casinhadosaber.dominio.Acao
import br.com.casinhadosaber.dominio.Andar
import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Caixa
import br.com.casinhadosaber.dominio.Categoria
import br.com.casinhadosaber.dominio.comMovelEm
import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Estrutura
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Mapa
import br.com.casinhadosaber.dominio.Movel
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.PlantaDaCasa
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.PrateleiraDaGeladeira
import br.com.casinhadosaber.dominio.Produto
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.enfeiteDesenhado
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Forma
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.MaoQueArrasta
import br.com.casinhadosaber.ui.componentes.BotoesDoCantinho
import br.com.casinhadosaber.ui.componentes.CantinhoDaSala
import br.com.casinhadosaber.ui.componentes.formasDaSalaCriativa
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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.ui.componentes.AparenciaDoAndar
import br.com.casinhadosaber.ui.componentes.ArteDaCasaPorDentro
import br.com.casinhadosaber.ui.componentes.CamadasDaCasaPorDentro
import br.com.casinhadosaber.ui.componentes.CamadasDosPersonagens
import br.com.casinhadosaber.ui.componentes.CascaDoAndar
import br.com.casinhadosaber.ui.componentes.ComposicaoDosPersonagens
import br.com.casinhadosaber.ui.componentes.FundoDoAndar
import br.com.casinhadosaber.ui.componentes.GrupoDaCasca
import br.com.casinhadosaber.ui.componentes.PROPORCAO_IRMAO
import br.com.casinhadosaber.ui.componentes.PersonagemDaArte
import br.com.casinhadosaber.ui.componentes.PoseDosBracos
import br.com.casinhadosaber.ui.componentes.QuadroDoPersonagem
import br.com.casinhadosaber.ui.componentes.SpriteNaTela
import br.com.casinhadosaber.ui.componentes.desenharIrmaozinho
import br.com.casinhadosaber.ui.componentes.desenharPersonagemEmCamadas
import br.com.casinhadosaber.ui.componentes.desenharSprite
import br.com.casinhadosaber.ui.componentes.multiplicarPor
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.Fredoka
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/** Altura da criança, nas unidades de altura da planta (a parede tem 3). */
private const val ALTURA_CRIANCA = 2.15f

/**
 * Espera [ms] contando pelos quadros da tela (e não pelo relógio de parede): no app é o mesmo, e nas capturas
 * de tela o tempo anda junto com o relógio do teste.
 */
private suspend fun esperarQuadros(ms: Long) {
    val inicio = withFrameNanos { it }
    while (withFrameNanos { it } - inicio < ms * 1_000_000L) {
        // só espera o próximo quadro
    }
}

/**
 * Como [esperarQuadros], para laços sem fim (a dica que volta de tempos em tempos): nos testes com relógio
 * automático (emulador) o laço para aqui e a tela consegue ficar ociosa; no app e nas capturas conta normal.
 */
private suspend fun esperarQuadrosSemFim(ms: Long) {
    val inicio = withInfiniteAnimationFrameNanos { it }
    while (withInfiniteAnimationFrameNanos { it } - inicio < ms * 1_000_000L) {
        // só espera o próximo quadro
    }
}

/** Velocidade da criança andando, em ladrilhos por segundo. */
private const val VELOCIDADE = 2.6f

private val Contorno = Color(0x33000000)
private val Escuro = Color(0x990E1530)
private val Madeira = Color(0xFFC98B5B)
private val MadeiraClara = Color(0xFFE6B98F)
private val Branco = Color(0xFFF7F9FB)
private val Metal = Color(0xFFB0BEC5)
private val CoresDoArcoIris = listOf(Color(0xFFEF476F), Color(0xFFFFA62B), Color(0xFFFFD166), Color(0xFF3DBE6B), Color(0xFF4D96FF))

/** As mesmas cores das peças na casa de fora (os emojis 🛏️ e 🛋️ da fonte Noto são azuis). */
private val CorDoCobertor = Color(0xFF4F8FE6)
private val CorDoSofa = Color(0xFF4A90E2)

/** Fundo das abas da geladeira (a aba escolhida "cola" na prateleira de baixo). */
private val CorDaPrateleira = Color(0xFFDDF3FF)

/**
 * A casa por dentro, em perspectiva isométrica: a mesma casa de fora, com os dois andares ligados pela
 * escada (em cima, quarto e banheiro; embaixo, sala e cozinha) e as cores de parede escolhidas pela criança.
 * A criança anda pelos cômodos (toque no chão), sobe e desce a escada, abre a geladeira e o guarda-roupa,
 * acende as luzes, liga a TV e a torneira. A lojinha daqui vende comidas (para a geladeira) e roupas
 * (para o guarda-roupa).
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
    /** Para as capturas de tela: a dica de arrastar começa logo (sem esperar a criança ficar parada). */
    dicaLogo: Boolean = false,
    /** Cantinho criativo da sala (ateliê, piano, diplomas e álbum). Sem eles, os botões não aparecem. */
    cantinho: CantinhoDaSala? = null,
    /** De noite as janelas mostram o céu escuro (a arte das janelas tem as duas versões). */
    noite: Boolean = false,
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
            // O guarda-roupa fica no quarto, lá em cima: a criança já começa no andar de cima.
            if (abrirInicial == Acao.GUARDA_ROUPA) {
                andar = Andar.DE_CIMA
                x = PlantaDaCasa.peDaEscada.x
                y = PlantaDaCasa.peDaEscada.y
            }
            // O irmãozinho começa ao lado da criança, num ladrilho livre.
            irmaoAoLado(Mapa(PlantaDaCasa.moveisDe(estado), andar))
        }
    }
    var lojaAberta by remember { mutableStateOf(lojaInicial) }
    var confete by remember { mutableIntStateOf(0) }
    var jaAndou by remember { mutableStateOf(false) }
    // Dica de arrastar: a mãozinha segura um móvel e leva para o lado (na primeira visita e, enquanto a criança
    // nunca arrastou nada, de novo depois de um tempo parada).
    var maoDaDica by remember { mutableStateOf<Offset?>(null) }
    var maoApertando by remember { mutableStateOf(false) }
    /** Segundos sem tocar na cena (a dica só aparece com a criança parada). */
    var segundosParada by remember { mutableIntStateOf(0) }
    var arrastouAqui by remember { mutableStateOf(false) }
    var tarefaDaDica by remember { mutableStateOf<Job?>(null) }
    var falasDoIrmao by remember { mutableIntStateOf(0) }
    val estadoAtual by rememberUpdatedState(estado)

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
    val moveis = remember(estado.fasesVencidas, estado.moveisDentro) { PlantaDaCasa.moveisDe(estado) }
    val mapas = remember(moveis) { Andar.entries.associateWith { Mapa(moveis, it) } }
    val temGeladeira = moveis.any { it.acao == Acao.GELADEIRA }
    val geometria = remember { RefGeometria() }
    val andarDaCrianca = cena.andar

    // Arrastando um móvel: a cena mostra o móvel já no ladrilho de destino, com o realce verde ou vermelho.
    // Só recompõe quando o móvel muda de ladrilho (não a cada movimento do dedo).
    val arrastandoId = cena.arrastando
    val arrasteDx = cena.arrasteDx
    val arrasteDy = cena.arrasteDy
    val (moveisNaCena, destaque) = remember(moveis, arrastandoId, arrasteDx, arrasteDy) {
        val m = arrastandoId?.let { id -> moveis.firstOrNull { it.id == id } }
        if (m == null) {
            moveis to null
        } else {
            val valido = PlantaDaCasa.mover(moveis, m.id, arrasteDx, arrasteDy)
            val mostrados = valido ?: PlantaDaCasa.deslocar(moveis, m.id, arrasteDx, arrasteDy)
            mostrados to Destaque(m.andar, PlantaDaCasa.ladrilhosDe(m.deslocado(arrasteDx, arrasteDy)), valido != null)
        }
    }

    LaunchedEffect(Unit) {
        if (abrirInicial == Acao.NENHUMA && !lojaInicial) {
            narrador.falar(listOf(pt("Que casa linda por dentro! Toque no chão para andar, e na escada para subir. Em inglês, casa é:"), en("house")))
        }
    }

    // Relógio das animações: laço sem fim, então usa o quadro de "animação infinita". Nos testes com relógio
    // automático (emulador) ele fica parado e a tela consegue ficar ociosa; no app e nas capturas anda normal.
    // Só mexe em estados lidos no desenho: não recompõe a tela a cada quadro.
    LaunchedEffect(Unit) {
        var ultimo = -1L
        while (true) {
            withInfiniteAnimationFrameNanos { agora ->
                val dt = if (ultimo < 0) 0f else ((agora - ultimo) / 1e9f).coerceAtMost(0.05f)
                ultimo = agora
                cena.tempo += dt
            }
        }
    }

    // Caminhada: só pede quadros enquanto há caminho para andar (cada novo caminho recomeça o laço).
    LaunchedEffect(Unit) {
        snapshotFlow { cena.trocasDeCaminho }.collectLatest {
            var ultimo = -1L
            var distancia = 0f
            while (cena.indice < cena.caminho.size) {
                withFrameNanos { agora ->
                    val dt = if (ultimo < 0) 0f else ((agora - ultimo) / 1e9f).coerceAtMost(0.05f)
                    ultimo = agora
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
                        cena.seguir()
                        cena.passada += passo
                        distancia += passo
                        if (distancia > 0.75f) {
                            distancia = 0f
                            sonsCasa.passo()
                        }
                    }
                }
            }
        }
    }

    fun trocarDeAndar(novo: Andar) {
        cena.caminho = emptyList()
        cena.indice = 0
        cena.andando = false
        cena.andar = novo
        cena.x = PlantaDaCasa.peDaEscada.x
        cena.y = PlantaDaCasa.peDaEscada.y
        cena.trocouEm = cena.tempo
        cena.marca = null
        cena.irmaoAoLado(mapas.getValue(novo))
        sonsCasa.passo()
        narrador.falar(
            if (novo == Andar.DE_CIMA) listOf(pt("Subimos a escada! Aqui em cima tem o quarto e o banheiro. Em inglês:"), en("upstairs"))
            else listOf(pt("Descemos a escada! Aqui embaixo tem a sala e a cozinha. Em inglês:"), en("downstairs")),
        )
    }

    /** Anda até [destino] no [andar]; se for no outro andar, passa pela escada antes. */
    fun andarPara(destino: Posicao, andar: Andar = cena.andar, aoChegar: (() -> Unit)? = null) {
        if (andar != cena.andar) {
            andarPara(PlantaDaCasa.peDaEscada, cena.andar) {
                trocarDeAndar(andar)
                andarPara(destino, andar, aoChegar)
            }
            return
        }
        val c = mapas.getValue(andar).caminho(Posicao(cena.x, cena.y), destino)
        cena.caminho = c
        cena.indice = 0
        cena.aoChegar = aoChegar
        if (c.isEmpty()) {
            cena.aoChegar = null
            aoChegar?.invoke()
        } else {
            cena.marca = c.last()
            cena.marcaAndar = andar
            cena.marcaEm = cena.tempo
        }
    }

    fun usarEscada() {
        jaAndou = true
        val destino = cena.andar.outro
        andarPara(PlantaDaCasa.peDaEscada) { trocarDeAndar(destino) }
    }

    fun falarNome(m: Movel, extra: String? = null) {
        narrador.falar(listOf(pt("${m.nome}! Em inglês:"), en(m.nomeEn)) + (extra?.let { listOf(pt(it)) } ?: emptyList()))
    }

    fun interagir(m: Movel) {
        if (m.andar != cena.andar) {
            // Móvel do outro andar: a criança sobe (ou desce) a escada e vai até ele.
            jaAndou = true
            sons.toque()
            andarPara(mapas.getValue(m.andar).pertoDo(m), m.andar) { interagir(m) }
            return
        }
        cena.tocadoId = m.id
        cena.tocadoEm = cena.tempo
        val t = cena.tempo
        when (m.acao) {
            Acao.ESCADA -> {
                sons.toque()
                usarEscada()
                return
            }
            Acao.GELADEIRA -> {
                sonsCasa.porta()
                cena.geladeiraAberta = !cena.geladeiraAberta
                cena.guardaRoupaAberto = false
                if (cena.geladeiraAberta) {
                    lojaAberta = false
                    val comidas = Loja.naGeladeira(estado)
                    falarNome(m, if (comidas.isEmpty()) "Está vazia! Compre comidas na lojinha." else "Toque numa comida para comer!")
                }
            }
            Acao.GUARDA_ROUPA -> {
                sonsCasa.porta()
                cena.guardaRoupaAberto = !cena.guardaRoupaAberto
                cena.geladeiraAberta = false
                if (cena.guardaRoupaAberto) {
                    lojaAberta = false
                    falarNome(m, "Escolha uma roupa!")
                }
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
            Acao.PATINHO -> {
                sonsCasa.quack()
                narrador.falar(listOf(pt("Quac, quac! Patinho de borracha! Pato em inglês:"), en("duck")))
            }
            Acao.LIXEIRA -> {
                sonsCasa.tampa()
                narrador.falar(listOf(pt("Lixo no lixo! Lixeira em inglês:"), en("trash can")))
            }
            Acao.NENHUMA -> {
                sons.toque()
                falarNome(m)
            }
        }
        if (!jaAndou) jaAndou = true
        // A criança vai até o móvel (sem atravessar paredes).
        andarPara(mapas.getValue(m.andar).pertoDo(m))
    }

    fun alternarLuz(c: Comodo) {
        sonsCasa.clique()
        val apagada = c in cena.luzApagada
        cena.luzApagada = if (apagada) cena.luzApagada - c else cena.luzApagada + c
        narrador.falar(if (apagada) listOf(pt("Luz acesa!"), en("Light on!")) else listOf(pt("Luz apagada!"), en("Light off!")))
    }

    fun tocarNaCena(toque: Offset) {
        val g = geometria.g ?: return
        // 1) lâmpadas
        for (ga in g.andares) for (luz in ga.luzes) {
            if (hypot(toque.x - luz.lampada.x, toque.y - luz.lampada.y) < g.raioLampada) {
                alternarLuz(luz.comodo)
                return
            }
        }
        // 2) a própria criança
        val daCrianca = g.geo(cena.andar)
        if (daCrianca != null) {
            val pe = daCrianca.proj.ponto(cena.x, cena.y)
            if (toque.x in pe.x - g.larguraCrianca / 2..pe.x + g.larguraCrianca / 2 && toque.y in pe.y - g.alturaCrianca..pe.y) {
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
            // 2b) o irmãozinho
            if (g.comIrmao) {
                val pi = daCrianca.proj.ponto(cena.irmaoX, cena.irmaoY)
                if (toque.x in pi.x - g.larguraIrmao / 2..pi.x + g.larguraIrmao / 2 && toque.y in pi.y - g.alturaIrmao..pi.y) {
                    cena.puloEm = cena.tempo
                    sons.toque()
                    narrador.falar(Irmao.falaAoTocar(estado.perfil, falasDoIrmao++))
                    return
                }
            }
        }
        // 3) móveis, da frente para trás (o andar de baixo é desenhado por último)
        movelNoToque(g, toque)?.let {
            interagir(it)
            return
        }
        // 4) o chão: anda até lá (no andar tocado)
        val largura = PlantaDaCasa.LARGURA.toFloat()
        val fundo = PlantaDaCasa.FUNDO.toFloat()
        for (ga in g.andares.asReversed()) {
            val gx = ga.proj.gradeX(toque.x, toque.y)
            val gy = ga.proj.gradeY(toque.x, toque.y)
            if (gx in -0.3f..largura + 0.3f && gy in -0.3f..fundo + 0.3f) {
                jaAndou = true
                sons.toque()
                andarPara(Posicao(gx.coerceIn(0.05f, largura - 0.05f), gy.coerceIn(0.05f, fundo - 0.05f)), ga.andar)
                return
            }
        }
    }

    val tocar by rememberUpdatedState<(Offset) -> Unit>({ o -> tocarNaCena(o) })

    // ---- arrastar móveis: segurar (ou já sair arrastando) e soltar noutro ladrilho livre do mesmo andar

    /** O móvel que um dedo pousado em [toque] arrastaria (null: lâmpada, criança, chão ou móvel preso). */
    fun alvoDoArraste(toque: Offset): Movel? {
        val g = geometria.g ?: return null
        for (ga in g.andares) for (luz in ga.luzes) {
            if (hypot(toque.x - luz.lampada.x, toque.y - luz.lampada.y) < g.raioLampada) return null
        }
        g.geo(cena.andar)?.let { ga ->
            val pe = ga.proj.ponto(cena.x, cena.y)
            if (toque.x in pe.x - g.larguraCrianca / 2..pe.x + g.larguraCrianca / 2 && toque.y in pe.y - g.alturaCrianca..pe.y) return null
            val pi = ga.proj.ponto(cena.irmaoX, cena.irmaoY)
            if (g.comIrmao && toque.x in pi.x - g.larguraIrmao / 2..pi.x + g.larguraIrmao / 2 && toque.y in pi.y - g.alturaIrmao..pi.y) return null
        }
        val tocado = movelNoToque(g, toque) ?: return null
        val id = PlantaDaCasa.quemArrasta(tocado) ?: return null
        return moveis.firstOrNull { it.id == id }
    }

    fun comecarArraste(m: Movel, toque: Offset, segurou: Boolean) {
        val ga = geometria.g?.geo(m.andar) ?: return
        cena.arrasteDe = Posicao(ga.proj.gradeX(toque.x, toque.y), ga.proj.gradeY(toque.x, toque.y))
        cena.arrasteDx = 0
        cena.arrasteDy = 0
        cena.arrastando = m.id
        cena.arrasteDaDica = false
        jaAndou = true
        sonsCasa.pegar()
        if (segurou) narrador.falar("Arraste para mudar de lugar!")
    }

    fun moverArraste(toque: Offset) {
        val id = cena.arrastando ?: return
        val m = moveis.firstOrNull { it.id == id } ?: return
        val ga = geometria.g?.geo(m.andar) ?: return
        val dx = (ga.proj.gradeX(toque.x, toque.y) - cena.arrasteDe.x).roundToInt()
        val dy = (ga.proj.gradeY(toque.x, toque.y) - cena.arrasteDe.y).roundToInt()
        val (lx, ly) = PlantaDaCasa.limitarDeslocamento(m, dx, dy)
        if (lx != cena.arrasteDx || ly != cena.arrasteDy) {
            cena.arrasteDx = lx
            cena.arrasteDy = ly
        }
    }

    fun cancelarArraste() {
        cena.arrastando = null
        cena.arrasteDx = 0
        cena.arrasteDy = 0
    }

    fun soltarArraste() {
        val id = cena.arrastando ?: return
        val dx = cena.arrasteDx
        val dy = cena.arrasteDy
        cancelarArraste()
        val antes = moveis.firstOrNull { it.id == id } ?: return
        cena.tocadoId = id
        cena.tocadoEm = cena.tempo
        if (dx == 0 && dy == 0) {
            sonsCasa.pousar()
            return
        }
        val novos = PlantaDaCasa.mover(moveis, id, dx, dy)
        if (novos == null) {
            sons.erro()
            narrador.falar("Aí não cabe! Procure um lugar verde.")
            return
        }
        val m = novos.first { it.id == id }
        arrastouAqui = true
        onAtualizar { it.comMovelEm(id, m.x0, m.y0) }
        sonsCasa.pousar()
        narrador.falar(listOf(pt("${m.nome} mudou de lugar! Em inglês:"), en(m.nomeEn)))
        // O caminho da criança muda: se ela estava andando nesse andar, para; se o móvel caiu onde ela está, ela dá um passinho.
        if (cena.andar == antes.andar) {
            cena.caminho = emptyList()
            cena.indice = 0
            cena.andando = false
            cena.aoChegar = null
            val mapa = Mapa(novos, antes.andar)
            if (!mapa.livre(floor(cena.x).toInt(), floor(cena.y).toInt())) {
                val (lx, ly) = mapa.livreMaisPerto(floor(cena.x).toInt(), floor(cena.y).toInt())
                cena.x = lx + 0.5f
                cena.y = ly + 0.5f
            }
            if (!mapa.livre(floor(cena.irmaoX).toInt(), floor(cena.irmaoY).toInt())) cena.irmaoAoLado(mapa)
        }
    }

    val pegarParaArrastar by rememberUpdatedState<(Offset) -> Movel?>({ o -> alvoDoArraste(o) })
    val comecar by rememberUpdatedState<(Movel, Offset, Boolean) -> Unit>({ m, o, s -> comecarArraste(m, o, s) })
    val arrastar by rememberUpdatedState<(Offset) -> Unit>({ o -> moverArraste(o) })
    val soltar by rememberUpdatedState<() -> Unit>({ soltarArraste() })
    val cancelar by rememberUpdatedState<() -> Unit>({ cancelarArraste() })

    /** Um móvel do andar da criança e um passo pequeno que cabe (e não cai em cima dela), para a dica. */
    fun movelParaADica(): Pair<Movel, Pair<Int, Int>>? =
        PlantaDaCasa.sugestaoDeArraste(moveis, cena.andar, floor(cena.x).toInt() to floor(cena.y).toInt())

    fun limparDica() {
        if (cena.arrasteDaDica) {
            cena.arrasteDaDica = false
            cancelarArraste()
        }
        maoDaDica = null
        maoApertando = false
    }

    /** A mãozinha segura o móvel, leva um ladrilho para o lado (com o realce verde) e solta; ele volta ao lugar. */
    suspend fun mostrarDicaDeArrastar() {
        val (m, passo) = movelParaADica() ?: return
        val (dx, dy) = passo
        fun pontoDo(f: Float): Offset? = geometria.g?.geo(m.andar)?.proj?.ponto(m.centroX + dx * f, m.centroY + dy * f, m.z + m.altura * 0.45f)
        narrador.falar("Psiu! Os móveis mudam de lugar. Segure um e arraste!")
        try {
            repeat(2) {
                maoDaDica = pontoDo(0f) ?: return
                maoApertando = false
                esperarQuadros(800)
                maoApertando = true
                sonsCasa.pegar()
                cena.arrasteDx = 0
                cena.arrasteDy = 0
                cena.arrastando = m.id
                cena.arrasteDaDica = true
                esperarQuadros(400)
                val quadros = 20
                for (k in 1..quadros) {
                    val f = k / quadros.toFloat()
                    maoDaDica = pontoDo(f) ?: return
                    val ix = (dx * f).roundToInt()
                    val iy = (dy * f).roundToInt()
                    if (ix != cena.arrasteDx || iy != cena.arrasteDy) {
                        cena.arrasteDx = ix
                        cena.arrasteDy = iy
                    }
                    esperarQuadros(45)
                }
                esperarQuadros(800)
                // Solta: é só de mentirinha, o móvel volta para o lugar dele.
                maoApertando = false
                cena.arrasteDaDica = false
                cancelarArraste()
                sonsCasa.pousar()
                esperarQuadros(900)
            }
        } finally {
            limparDica()
        }
    }

    /** Qualquer toque na cena para a dica na hora (a criança assume) e reinicia a contagem do tempo parada. */
    fun tocouNaCena() {
        segundosParada = 0
        tarefaDaDica?.cancel()
        tarefaDaDica = null
        limparDica()
    }
    val aoTocarNaCena by rememberUpdatedState<() -> Unit>({ tocouNaCena() })

    LaunchedEffect(Unit) {
        if (abrirInicial != Acao.NENHUMA || lojaInicial) return@LaunchedEffect
        // Contagem em segundos (pelos quadros da tela).
        var segundos = 0
        var ultimaDica = -1
        while (true) {
            esperarQuadrosSemFim(1000)
            segundos++
            segundosParada++
            if (estadoAtual.moveisDentro.isNotEmpty() || arrastouAqui) return@LaunchedEffect
            val primeira = !estadoAtual.viuDicaDeArrastar && ultimaDica < 0
            val hora = if (primeira) {
                if (dicaLogo) segundos >= 1 else segundos >= 8 && segundosParada >= 4
            } else {
                segundosParada >= 30 && (ultimaDica < 0 || segundos - ultimaDica >= 60)
            }
            val livre = cena.arrastando == null && !cena.andando && !lojaAberta && !cena.geladeiraAberta && !cena.guardaRoupaAberto
            if (hora && livre) {
                ultimaDica = segundos
                val tarefa = launch { mostrarDicaDeArrastar() }
                tarefaDaDica = tarefa
                tarefa.join()
                tarefaDaDica = null
                if (!estadoAtual.viuDicaDeArrastar) onAtualizar { it.copy(viuDicaDeArrastar = true) }
            }
        }
    }

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
            Categoria.ROUPA, Categoria.ACESSORIO -> narrador.falar("Oba! ${produto.nome}! Ficou lindo! Está no guarda-roupa.")
            else -> narrador.falar("Oba! ${produto.nome}!")
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFA9DCFF), Color(0xFFDDF3D0), Color(0xFFBFE3A8), Color(0xFFA6D58C))))
            .drawWithCache {
                // Gramado de fundo: morrinhos suaves no terço de baixo e os cantos um pouco mais escuros
                // (o verde de baixo não fica vazio). Montado uma vez por tamanho.
                val morroLonge = Color(0xFFB5DE9C)
                val morroPerto = Color(0xFFA2D488)
                val cantos = Brush.radialGradient(
                    listOf(Color.Transparent, Color.Transparent, Color(0x2A2E5A1E)),
                    center = Offset(size.width / 2f, size.height * 0.55f),
                    radius = maxOf(size.width, size.height) * 0.75f,
                )
                onDrawBehind {
                    drawOval(morroLonge, Offset(-size.width * 0.3f, size.height * 0.70f), Size(size.width * 1.0f, size.height * 0.22f))
                    drawOval(morroLonge, Offset(size.width * 0.35f, size.height * 0.72f), Size(size.width * 1.0f, size.height * 0.2f))
                    drawOval(morroPerto, Offset(-size.width * 0.1f, size.height * 0.82f), Size(size.width * 1.2f, size.height * 0.3f))
                    for (i in 0 until 18) {
                        val x = ((i * 0.137f + 0.05f) % 1f) * size.width
                        val y = size.height * (0.76f + ((i * 0.29f) % 1f) * 0.22f)
                        drawOval(Color(0x2A3E7F2E), Offset(x - size.width * 0.03f, y), Size(size.width * 0.06f, size.width * 0.014f))
                    }
                    drawRect(cantos)
                }
            }
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BotaoRedondo("⬅️", "Voltar para o jardim", onVoltar, tamanho = 52.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "🪙 ${estado.moedas}",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .semantics { contentDescription = "Você tem ${estado.moedas} moedinhas" },
                )
                Spacer(Modifier.weight(1f))
                BotaoDoAndar(andarDaCrianca) { sons.toque(); usarEscada() }
                Spacer(Modifier.width(6.dp))
                BotaoRedondo("🪞", "Espelho: trocar a aparência", { sons.toque(); onEspelho() }, tamanho = 52.dp)
                Spacer(Modifier.width(6.dp))
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

            // A cena ocupa só o espaço livre entre a barra de cima e o painel aberto, e a casa cabe nele inteira.
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                CenaIsometrica(
                    estado = estadoNaCena,
                    perfil = perfil,
                    moveis = moveisNaCena,
                    andarDaCrianca = andarDaCrianca,
                    cena = cena,
                    medidor = medidor,
                    ref = geometria,
                    destaque = destaque,
                    noite = noite,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = "Casa por dentro, com dois andares. Toque no chão para andar e na escada para subir ou descer. " +
                                "Segure e arraste um móvel para mudar de lugar."
                        }
                        .pointerInput(Unit) {
                            // Tocar e soltar: anda ou mexe no móvel (como antes). Segurar um móvel, ou sair arrastando
                            // em cima dele, leva o móvel junto, ladrilho por ladrilho.
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                aoTocarNaCena()
                                val alvo = pegarParaArrastar(down.position)
                                if (alvo == null) {
                                    val up = waitForUpOrCancellation()
                                    if (up != null) {
                                        up.consume()
                                        tocar(up.position)
                                    }
                                    return@awaitEachGesture
                                }
                                val limite = down.uptimeMillis + viewConfiguration.longPressTimeoutMillis
                                var ultimo = down
                                var segurou = false
                                var arrastando = false
                                while (!arrastando) {
                                    val resta = (limite - ultimo.uptimeMillis).coerceAtLeast(1L)
                                    val evento = withTimeoutOrNull(resta) { awaitPointerEvent() }
                                    if (evento == null) {
                                        segurou = true
                                        arrastando = true
                                    } else {
                                        val ch = evento.changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
                                        if (ch.isConsumed) return@awaitEachGesture
                                        ultimo = ch
                                        if (!ch.pressed) {
                                            ch.consume()
                                            tocar(ch.position)
                                            return@awaitEachGesture
                                        }
                                        if ((ch.position - down.position).getDistance() > viewConfiguration.touchSlop) arrastando = true
                                    }
                                }
                                comecar(alvo, down.position, segurou)
                                var terminou = false
                                try {
                                    arrastar(ultimo.position)
                                    while (true) {
                                        val ch = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                                        ch.consume()
                                        if (!ch.pressed) {
                                            terminou = true
                                            soltar()
                                            break
                                        }
                                        arrastar(ch.position)
                                    }
                                } finally {
                                    if (!terminou) cancelar()
                                }
                            }
                        },
                )
                val mao = maoDaDica
                if (mao != null) {
                    MaoQueArrasta(
                        maoApertando,
                        Modifier.testTag("mao_da_dica").offset { IntOffset((mao.x - 20.dp.toPx()).roundToInt(), (mao.y - 20.dp.toPx()).roundToInt()) },
                    )
                } else if (!jaAndou && abrirInicial == Acao.NENHUMA && !lojaInicial) {
                    MaoGuia(Modifier.align(Alignment.Center).padding(top = 120.dp), espera = 2500)
                }
            }

            // Cantinho criativo (ateliê, piano, diplomas e álbum): uma fileira embaixo da cena, fora da casa,
            // escondida enquanto a geladeira, o guarda-roupa ou a lojinha estão abertos.
            if (cantinho != null && !cena.geladeiraAberta && !cena.guardaRoupaAberto && !lojaAberta) {
                BotoesDoCantinho(
                    estado, cantinho.onAtelie, cantinho.onPiano, cantinho.onDiplomas, cantinho.onAlbum,
                    Modifier.align(Alignment.CenterHorizontally).padding(bottom = 10.dp),
                )
            }
            if (cena.geladeiraAberta) {
                PainelGeladeira(
                    comidas = Loja.naGeladeira(estado),
                    onAba = { p, vazia ->
                        sons.toque()
                        narrador.falar(
                            listOf(pt("${p.nome}! Em inglês:"), en(p.nomeEn)) +
                                if (vazia) listOf(pt("Ainda está vazia. Compre comidas na lojinha!")) else emptyList(),
                        )
                    },
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
            // ---- Guarda-roupa (roupas, chapéus e acessórios): início do bloco ----
            if (cena.guardaRoupaAberto) {
                PainelGuardaRoupa(
                    estado = estado,
                    perfilNaCena = perfil,
                    onVestir = { p ->
                        if (p.id in estado.comprados) {
                            // Já é seu: veste (o provador fala o nome).
                            onAtualizar { it.vestirProduto(p) }
                            cena.puloEm = cena.tempo
                            sons.colocar()
                        } else {
                            // Ainda não é seu: o primeiro toque experimenta, o segundo compra (como na lojinha).
                            comprar(p)
                            cena.puloEm = cena.tempo
                        }
                    },
                    onTirar = { gaveta ->
                        onAtualizar { it.tirar(gaveta) }
                        cena.puloEm = cena.tempo
                    },
                    onFechar = {
                        sonsCasa.porta()
                        cena.guardaRoupaAberto = false
                    },
                )
            }
            // ---- Guarda-roupa: fim do bloco ----
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
        // Cômodos novos da casa que cresce (quarto novo, sala de brinquedos...): portinhas na lateral (ComodosNovos.kt).
        ComodosNovosDaCasa(estado, onAtualizar)
    }
}

/** Botão da escada na barra de cima: uma casinha de dois andares com o andar da criança aceso. */
@Composable
private fun BotaoDoAndar(andar: Andar, onClick: () -> Unit) {
    val emCima = andar == Andar.DE_CIMA
    BotaoFofo(
        onClick = onClick,
        cor = Color.White,
        corBase = Color(0x33000000),
        forma = CircleShape,
        descricao = if (emCima) "Descer a escada para a sala e a cozinha" else "Subir a escada para o quarto e o banheiro",
        profundidade = 4.dp,
    ) {
        Canvas(Modifier.size(52.dp)) {
            val w = size.width
            val h = size.height
            val esq = w * 0.24f
            val dir = w * 0.76f
            val topo = h * 0.42f
            val meio = h * 0.6f
            val base = h * 0.78f
            val telhado = Path().apply {
                moveTo(w * 0.16f, topo + h * 0.01f)
                lineTo(w * 0.5f, h * 0.16f)
                lineTo(w * 0.84f, topo + h * 0.01f)
                close()
            }
            drawPath(telhado, Color(0xFFEF476F))
            val aceso = Cores.Estrela
            val apagado = Color(0xFFE3E7EE)
            drawRect(if (emCima) aceso else apagado, Offset(esq, topo), Size(dir - esq, meio - topo))
            drawRect(if (emCima) apagado else aceso, Offset(esq, meio), Size(dir - esq, base - meio))
            drawRect(Color(0xFF8E5A34), Offset(esq, topo), Size(dir - esq, base - topo), style = Stroke(w * 0.035f))
            drawLine(Color(0xFF8E5A34), Offset(esq, meio), Offset(dir, meio), strokeWidth = w * 0.035f)
            // degraus da escada, do lado esquerdo
            val degrau = (base - topo) / 4f
            for (k in 0 until 4) {
                val y = base - (k + 1) * degrau
                drawRect(Color(0xFFC98B5B), Offset(esq, y), Size(w * (0.2f - k * 0.04f), degrau))
            }
            // setinha: para onde a escada leva
            val sx = w * 0.62f
            val sy = if (emCima) meio + (base - meio) * 0.5f else topo + (meio - topo) * 0.5f
            val ponta = if (emCima) 1f else -1f
            val seta = Path().apply {
                moveTo(sx - w * 0.07f, sy - ponta * h * 0.035f)
                lineTo(sx + w * 0.07f, sy - ponta * h * 0.035f)
                lineTo(sx, sy + ponta * h * 0.06f)
                close()
            }
            drawPath(seta, Color(0xFF3B4C8C))
        }
    }
}

/** Estado da cena que muda a cada quadro ou a cada toque. Lido só no desenho, para não recompor a tela toda. */
@Stable
private class Cena {
    var andar by mutableStateOf(Andar.DE_BAIXO)
    var x by mutableFloatStateOf(PlantaDaCasa.entrada.x)
    var y by mutableFloatStateOf(PlantaDaCasa.entrada.y)
    var tempo by mutableFloatStateOf(0f)
    var passada by mutableFloatStateOf(0f)
    var andando by mutableStateOf(false)
    var virado by mutableStateOf(false)
    /** Quantas vezes o caminho mudou: acorda o laço da caminhada. */
    var trocasDeCaminho by mutableIntStateOf(0)
    var caminho: List<Posicao> = emptyList()
        set(v) {
            field = v
            trocasDeCaminho++
        }
    var indice = 0
    var aoChegar: (() -> Unit)? = null
    var marca by mutableStateOf<Posicao?>(null)
    var marcaAndar by mutableStateOf(Andar.DE_BAIXO)
    var marcaEm by mutableFloatStateOf(-10f)
    var trocouEm by mutableFloatStateOf(-10f)
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

    /** Móvel sendo arrastado (ou null) e quantos ladrilhos ele já andou na grade. */
    var arrastando by mutableStateOf<String?>(null)
    var arrasteDx by mutableIntStateOf(0)
    var arrasteDy by mutableIntStateOf(0)
    /** Onde o dedo pegou o móvel, em ladrilhos do chão (o deslocamento conta a partir daqui). */
    var arrasteDe = Posicao(0f, 0f)
    /** O arraste em andamento é só a dica da mãozinha (de mentirinha: nada é salvo e o móvel volta). */
    var arrasteDaDica = false

    /** O irmãozinho: anda atrás da criança, pelo mesmo caminho ([rastro]), a [DISTANCIA_DO_IRMAO] ladrilhos dela. */
    var irmaoX by mutableFloatStateOf(PlantaDaCasa.entrada.x + 0.55f)
    var irmaoY by mutableFloatStateOf(PlantaDaCasa.entrada.y - 0.3f)
    var irmaoVirado by mutableStateOf(false)
    private val rastro = ArrayList<Posicao>()

    /** Põe o irmãozinho ao lado da criança (ao entrar e ao trocar de andar), num ladrilho livre do [mapa]. */
    fun irmaoAoLado(mapa: Mapa?) {
        val lados = listOf(0.55f to -0.3f, -0.3f to 0.55f, 0.55f to 0.3f, 0.3f to -0.55f)
        val (dx, dy) = lados.firstOrNull { (dx, dy) ->
            val nx = x + dx
            val ny = y + dy
            nx in 0.1f..PlantaDaCasa.LARGURA - 0.1f && ny in 0.1f..PlantaDaCasa.FUNDO - 0.1f &&
                (mapa == null || mapa.livre(floor(nx).toInt(), floor(ny).toInt()))
        } ?: (0f to 0f)
        irmaoX = x + dx
        irmaoY = y + dy
        rastro.clear()
        rastro += Posicao(irmaoX, irmaoY)
        rastro += Posicao(x, y)
    }

    /** A criança andou: o irmãozinho segue o rastro dela, sem cortar caminho por cima dos móveis. */
    fun seguir() {
        if (rastro.isEmpty()) rastro += Posicao(irmaoX, irmaoY)
        rastro += Posicao(x, y)
        // do fim (a criança) para trás, até completar a distância
        var falta = DISTANCIA_DO_IRMAO
        var i = rastro.size - 1
        var px = rastro[i].x
        var py = rastro[i].y
        while (i > 0) {
            val a = rastro[i - 1]
            val d = hypot(px - a.x, py - a.y)
            if (d >= falta) {
                val f = if (d > 0f) falta / d else 0f
                px += (a.x - px) * f
                py += (a.y - py) * f
                falta = 0f
                break
            }
            falta -= d
            px = a.x
            py = a.y
            i--
        }
        if (falta > 0f) return // ainda perto: fica onde está
        if (abs(px - irmaoX - (py - irmaoY)) > 0.005f) irmaoVirado = px - irmaoX - (py - irmaoY) < 0
        irmaoX = px
        irmaoY = py
        // o que ficou para trás do irmãozinho não serve mais
        repeat(i - 1) { if (rastro.size > 2) rastro.removeAt(0) }
        if (i >= 1) rastro[0] = Posicao(px, py)
    }
}

/** Quanto o irmãozinho fica atrás da criança, em ladrilhos. */
private const val DISTANCIA_DO_IRMAO = 0.75f

/** Realce dos ladrilhos de destino enquanto um móvel é arrastado: verde se cabe, vermelho se não. */
private class Destaque(val andar: Andar, val ladrilhos: List<Pair<Int, Int>>, val valido: Boolean)

/** A geometria pronta (guardada para os toques saberem onde está cada coisa). */
private class RefGeometria {
    var g: Geometria? = null
}

/** O móvel tocado em [toque], da frente para trás (o andar de baixo é desenhado por último). */
private fun movelNoToque(g: Geometria, toque: Offset): Movel? {
    for (ga in g.andares.asReversed()) {
        for (i in ga.pecas.indices.reversed()) {
            val p = ga.pecas[i]
            val m = p.movel ?: continue
            if (p.toque.contains(toque)) return m
        }
        for (p in ga.naParede) {
            val m = p.movel ?: continue
            if (p.toque.contains(toque)) return m
        }
    }
    return null
}

/**
 * Um móvel (ou pedaço de parede) já convertido em formas para desenhar. Com a arte 3D, [sprite] (e [spriteAberto],
 * na geladeira e no guarda-roupa) toma o lugar das formas; as formas continuam medindo a área de toque.
 */
private data class Peca(
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
    val sprite: SpriteNaTela? = null,
    val spriteAberto: SpriteNaTela? = null,
    /** Roupas nos cabides do guarda-roupa aberto (vetoriais, por cima da imagem aberta). */
    val extrasAbertos: List<Forma> = emptyList(),
    /** Os [extrasAbertos] só aparecem à direita desta linha (a porta da esquerda fica na frente deles). */
    val recorteAberto: Float = Float.NEGATIVE_INFINITY,
)

/** A luz de um cômodo: a lâmpada pendurada e o que escurece quando ela apaga. */
private class Luz(
    val comodo: Comodo,
    val lampada: Offset,
    val corda: Offset,
    val cupula: Path,
    val brilho: Brush,
    val chao: Path,
    val paredes: List<Forma>,
    /** A lâmpada da arte 3D, acesa e apagada (sem ela, a cúpula vetorial). */
    val acesa: SpriteNaTela? = null,
    val apagada: SpriteNaTela? = null,
)

/** Um andar pronto para desenhar, com a sua própria projeção (os dois andares podem aparecer empilhados). */
private class GeoAndar(
    val andar: Andar,
    val proj: Projecao,
    /** O fundo fixo composto num bitmap (arte 3D); com ele, [chao] e [paredes] ficam vazios. */
    val fundo: FundoDoAndar?,
    val chao: List<Forma>,
    val paredes: List<Forma>,
    /** Quadros pintados e diplomas da sala (vetoriais, por cima da parede). */
    val salaCriativa: List<Forma>,
    val naParede: List<Peca>,
    val pecas: List<Peca>,
    val luzes: List<Luz>,
    val sombra: Rect?,
    val realce: Forma? = null,
)

private class Geometria(
    val andares: List<GeoAndar>,
    /** Telhado do andar de cima (visto por dentro, como numa casinha de bonecas aberta), ou vazio. */
    val telhado: List<Forma>,
    /** Céu livre acima da casa, em pixels (em pé sobra uma faixa: ganha sol e nuvens), e a largura da cena. */
    val ceu: Float,
    val larguraDaCena: Float,
    val ladrilho: Float,
    val raioLampada: Float,
    val alturaCrianca: Float,
    val larguraCrianca: Float,
    val chapeu: TextLayoutResult?,
    val comidas: Map<String, TextLayoutResult>,
    val zzz: TextLayoutResult,
    val traco: Stroke,
    val tracoFino: Stroke,
    /** A arte 3D da tela (para os personagens em camadas), ou nula. */
    val arte: ArteDaCasaPorDentro?,
    /** Personagens em camadas 3D (senão, o desenho antigo). */
    val criancaEmCamadas: Boolean,
    val irmaoEmCamadas: Boolean,
    /** O irmãozinho anda junto (perfil com irmão ligado), com esta altura e largura na tela. */
    val comIrmao: Boolean,
    val alturaIrmao: Float,
    val larguraIrmao: Float,
) {
    fun geo(a: Andar): GeoAndar? = andares.firstOrNull { it.andar == a }
}

@Composable
private fun CenaIsometrica(
    estado: EstadoJogo,
    perfil: Perfil,
    moveis: List<Movel>,
    andarDaCrianca: Andar,
    cena: Cena,
    medidor: TextMeasurer,
    ref: RefGeometria,
    destaque: Destaque?,
    noite: Boolean,
    modifier: Modifier,
) {
    val densidade = LocalDensity.current
    val contexto = LocalContext.current
    // A arte 3D da casa por dentro fica só enquanto a tela está aberta (nada de bitmap em objeto estático).
    val arte = remember(contexto) { ArteDaCasaPorDentro(contexto) }
    DisposableEffect(arte) { onDispose { arte.limpar() } }
    Box(
        modifier
            // A geometria já fica pronta no layout (antes do primeiro desenho): a dica da mãozinha e os toques
            // precisam dela mesmo quando a tela ainda não foi desenhada. O fundo de cada andar é composto uma vez
            // (cache da arte por andar, aparência e tamanho): a segunda montagem só o reaproveita.
            .onSizeChanged { s ->
                if (s.width > 0 && s.height > 0) {
                    ref.g = montarGeometria(Size(s.width.toFloat(), s.height.toFloat()), densidade, estado, perfil, moveis, andarDaCrianca, medidor, destaque, arte, noite)
                }
            }
            .drawWithCache {
            val g = montarGeometria(size, this, estado, perfil, moveis, andarDaCrianca, medidor, destaque, arte, noite)
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
        style = TextStyle(fontFamily = Fredoka, fontSize = (px / (densidade.density * densidade.fontScale)).sp, textAlign = TextAlign.Center),
    )

/** Piso de cada cômodo: tábuas de madeira no quarto e na sala, lajotas lisas no banheiro e na cozinha (sem xadrez). */
private class Piso(val base: Color, val linha: Color, val madeira: Boolean)

private fun pisoDo(c: Comodo): Piso = when (c) {
    Comodo.QUARTO -> Piso(Color(0xFFE9C39A), Color(0xFFD3A677), madeira = true)
    Comodo.SALA -> Piso(Color(0xFFE6BD90), Color(0xFFCF9F6F), madeira = true)
    Comodo.BANHEIRO -> Piso(Color(0xFFE6F5FA), Color(0xFFC7E3EE), madeira = false)
    Comodo.COZINHA -> Piso(Color(0xFFFFF4E3), Color(0xFFEBD5B8), madeira = false)
}

private fun montarGeometria(
    tamanho: Size,
    densidade: Density,
    estado: EstadoJogo,
    perfil: Perfil,
    moveis: List<Movel>,
    andarDaCrianca: Andar,
    medidor: TextMeasurer,
    destaque: Destaque? = null,
    arte: ArteDaCasaPorDentro? = null,
    noite: Boolean = false,
): Geometria {
    val dp = densidade.density
    // Enquadramento: a planta inteira cabe na largura (menos as margens) e fica no meio do espaço livre.
    // Quando cabem, os dois andares aparecem empilhados, como na casa de fora; senão, só o andar da criança.
    val margemX = 16f * dp
    val margemY = 8f * dp
    val livreL = (tamanho.width - 2 * margemX).coerceAtLeast(1f)
    val livreA = (tamanho.height - 2 * margemY).coerceAtLeast(1f)
    val enq = PlantaDaCasa.enquadramento()
    val doisAndares = PlantaDaCasa.mostrarDoisAndares(livreL, livreA, enq)
    val visiveis = if (doisAndares) listOf(Andar.DE_CIMA, Andar.DE_BAIXO) else listOf(andarDaCrianca)
    val unidades = enq.altura + if (doisAndares) enq.entreAndares else 0f
    var tw = PlantaDaCasa.ladrilhoQueCabe(livreL, livreA, doisAndares, enq)
    // Telhado: em pé, a largura manda e sobra céu em cima da casa. Quando o andar de cima está no alto e a casa
    // já tem telhado, ele ocupa essa faixa (só se couber quase sem diminuir a casa).
    val acimaDoTelhado = ALTURA_DO_TELHADO_NA_TELA
    val twComTelhado = min(livreL / enq.largura, livreA / (unidades + acimaDoTelhado))
    val comTelhado = visiveis.first() == Andar.DE_CIMA && estado.temEstrutura(Estrutura.TELHADO) && twComTelhado >= tw * 0.97f
    if (comTelhado) tw = twComTelhado
    val alturaTotal = tw * (unidades + if (comTelhado) acimaDoTelhado else 0f)
    val topo = (tamanho.height - alturaTotal) / 2f + if (comTelhado) tw * acimaDoTelhado else 0f
    val largura = PlantaDaCasa.LARGURA.toFloat()
    val fundo = PlantaDaCasa.FUNDO.toFloat()
    // O meio da planta na tela fica em x(L/2, F/2): a origem anda para a esquerda o quanto a planta é mais larga que funda.
    val origemX = tamanho.width / 2f - (largura - fundo) * tw / 4f
    val traco = Stroke(max(1f, 1.2f * dp))
    val tracoFino = Stroke(max(1f, 0.8f * dp))

    val andares = visiveis.mapIndexed { i, a ->
        val origemY = topo + tw * (enq.acima + i * enq.entreAndares)
        val proj = Projecao(tw, origemX, origemY)
        // A sombra no gramado fica só embaixo do andar que pisa no chão (ou do andar sozinho).
        val noChao = a == Andar.DE_BAIXO || !doisAndares
        montarAndar(a, proj, noChao, estado, perfil, moveis.filter { it.andar == a }, medidor, densidade, destaque?.takeIf { it.andar == a }, arte, noite, tracoFino)
    }

    // ---- criança, chapéu e comidas na mão
    val proj0 = andares.first().proj
    val alturaCrianca = ALTURA_CRIANCA * proj0.unidadeZ
    val larguraCrianca = alturaCrianca * PROPORCAO_CRIANCA
    val chapeuFonte = larguraCrianca * 0.34f * 2f * 0.9f * 0.82f
    val chapeu = perfil.chapeu.takeIf { it.isNotEmpty() }?.let { medirEmoji(medidor, densidade, it, chapeuFonte) }
    val comidas = Loja.comidas.associate { (e, _, _) -> e to medirEmoji(medidor, densidade, e, larguraCrianca * 0.42f) }
    val zzz = medirEmoji(medidor, densidade, "💤", larguraCrianca * 0.5f)
    // O irmãozinho do tamanho da casa de fora (dois terços da criança).
    val alturaIrmao = alturaCrianca * 0.66f

    return Geometria(
        andares = andares,
        telhado = if (comTelhado) montarTelhado(andares.first().proj, estado) else emptyList(),
        ceu = ((tamanho.height - alturaTotal) / 2f).coerceAtLeast(0f),
        larguraDaCena = tamanho.width,
        ladrilho = tw,
        raioLampada = max(tw * 0.4f, 26f * dp),
        alturaCrianca = alturaCrianca,
        larguraCrianca = larguraCrianca,
        chapeu = chapeu,
        comidas = comidas,
        zzz = zzz,
        traco = traco,
        tracoFino = tracoFino,
        arte = arte,
        criancaEmCamadas = arte != null && ComposicaoDosPersonagens.podeUsar(arte.contexto, PersonagemDaArte.CRIANCA, perfil),
        irmaoEmCamadas = arte != null && perfil.irmao.ligado && ComposicaoDosPersonagens.podeUsar(arte.contexto, PersonagemDaArte.IRMAO, perfil),
        comIrmao = perfil.irmao.ligado,
        alturaIrmao = alturaIrmao,
        larguraIrmao = alturaIrmao * PROPORCAO_IRMAO,
    )
}

/** Altura do telhado em z, acima do alto das paredes (cumeeira no meio do fundo da casa). */
private const val SUBIDA_DO_TELHADO = 2.8f
/** Espessura das telhas em z (a faixa colorida que contorna o telhado aberto). */
private const val TELHA = 0.3f

/**
 * Quanto o telhado sobe na tela acima do canto mais alto das paredes, em ladrilhos: a cumeeira (com as telhas)
 * fica SUBIDA * unidadeZ acima e FUNDO / 2 * 1/4 abaixo (ela está no meio do fundo da casa).
 */
private val ALTURA_DO_TELHADO_NA_TELA = (SUBIDA_DO_TELHADO + TELHA) * 0.56f - PlantaDaCasa.FUNDO / 8f + 0.06f

/**
 * O telhado do andar de cima, como numa casinha de bonecas aberta: a água do fundo vista por dentro (o forro de
 * madeira com as vigas), a empena da esquerda com uma janelinha redonda, as telhas na cor do telhado de fora
 * contornando o corte e a chaminé (quando a casa já tem) aparecendo atrás.
 */
private fun montarTelhado(proj: Projecao, estado: EstadoJogo): List<Forma> = with(proj) {
    val l = PlantaDaCasa.LARGURA.toFloat()
    val f = PlantaDaCasa.FUNDO.toFloat()
    val h = PlantaDaCasa.ALTURA_PAREDE
    val r = SUBIDA_DO_TELHADO
    val yr = f / 2f
    val zr = h + r
    val beiral = 0.3f
    val ye = -0.14f - beiral
    fun zDa(y: Float) = h + r * (y + 0.14f) / (yr + 0.14f)
    val ze = zDa(ye)
    val xd = l + 0.15f
    val cor = Color(Loja.coresDoTelhado.getOrElse(estado.corTelhado) { Loja.coresDoTelhado[0] })
    val borda = Color(Loja.coresDoTelhadoEscuras.getOrElse(estado.corTelhado) { Loja.coresDoTelhadoEscuras[0] })
    val formas = ArrayList<Forma>()
    // Chaminé atrás da água do fundo: só a ponta aparece acima da cumeeira.
    if (estado.temEstrutura(Estrutura.CHAMINE)) {
        formas += caixa(5.7f, 0.35f, 6.35f, 1.0f, h, zr + 0.75f, Color(0xFFB5523B))
        formas += caixa(5.62f, 0.27f, 6.43f, 1.08f, zr + 0.75f, zr + 0.9f, Color(0xFF8E3B2A))
    }
    // Forro de madeira (a água do fundo vista por dentro), com as vigas.
    formas += Forma(quad(ponto(0f, ye, ze), ponto(xd, ye, ze), ponto(xd, yr, zr), ponto(0f, yr, zr)), Color(0xFFF2D8B0))
    var x = 1f
    while (x < xd - 0.2f) {
        formas += Forma(quad(ponto(x - 0.07f, ye, ze), ponto(x + 0.07f, ye, ze), ponto(x + 0.07f, yr, zr), ponto(x - 0.07f, yr, zr)), Color(0xFFD9AE7E), contorno = false)
        x += 1.6f
    }
    // Empena da esquerda (a parede em triângulo), na cor do quarto, com uma janelinha redonda.
    val corDaEmpena = escurecer(Color(estado.corDo(Comodo.QUARTO)), 0.95f)
    formas += Forma(quad(ponto(0f, 0f, h), ponto(0f, f, h), ponto(0f, yr, zr), ponto(0f, yr, zr)), corDaEmpena)
    fun circuloX(cy: Float, cz: Float, raio: Float): Path = Path().apply {
        for (k in 0..28) {
            val a = k * 2.0 * PI / 28
            val p = ponto(0.002f, cy + raio * kotlin.math.cos(a).toFloat(), cz + raio * sin(a).toFloat())
            if (k == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
        }
        close()
    }
    formas += Forma(circuloX(yr, h + 1.05f, 0.5f), Color.White)
    formas += Forma(circuloX(yr, h + 1.05f, 0.38f), Color(0xFFBFE6FF))
    formas += Forma(quad(ponto(0.003f, yr - 0.03f, h + 0.67f), ponto(0.003f, yr + 0.03f, h + 0.67f), ponto(0.003f, yr + 0.03f, h + 1.43f), ponto(0.003f, yr - 0.03f, h + 1.43f)), Color.White, contorno = false)
    // Telhas: a faixa colorida no corte da cumeeira, na ponta da direita e por cima da empena.
    val yf = f + beiral
    val zf = h + r * (f - yf) / (f - yr)
    formas += Forma(quad(ponto(-0.15f, yr, zr), ponto(xd, yr, zr), ponto(xd, yr, zr + TELHA), ponto(-0.15f, yr, zr + TELHA)), cor)
    formas += Forma(quad(ponto(xd, ye, ze), ponto(xd, yr, zr), ponto(xd, yr, zr + TELHA), ponto(xd, ye, ze + TELHA)), borda)
    formas += Forma(quad(ponto(-0.15f, ye, ze), ponto(-0.15f, yr, zr), ponto(-0.15f, yr, zr + TELHA), ponto(-0.15f, ye, ze + TELHA)), cor)
    formas += Forma(quad(ponto(-0.15f, yr, zr), ponto(-0.15f, yf, zf), ponto(-0.15f, yf, zf + TELHA), ponto(-0.15f, yr, zr + TELHA)), cor)
    formas
}

/**
 * Monta um andar: o fundo fixo (piso, paredes do fundo com a cor de cada cômodo, janelas, porta, acabamento), os
 * móveis e as luzes. Com a arte 3D, o fundo vira um bitmap só (composto uma vez por andar, aparência e tamanho) e os
 * móveis viram imagens; o que faltar na arte cai no desenho vetorial antigo, peça por peça.
 */
private fun montarAndar(
    andar: Andar,
    proj: Projecao,
    noChao: Boolean,
    estado: EstadoJogo,
    perfil: Perfil,
    moveis: List<Movel>,
    medidor: TextMeasurer,
    densidade: Density,
    destaque: Destaque? = null,
    arte: ArteDaCasaPorDentro? = null,
    noite: Boolean = false,
    tracoFino: Stroke = Stroke(1f),
): GeoAndar {
    val tw = proj.ladrilho
    val lado = PlantaDaCasa.LARGURA.toFloat()
    val fundo = PlantaDaCasa.FUNDO.toFloat()
    val h = PlantaDaCasa.ALTURA_PAREDE

    // ---- fundo fixo: laje, pisos, tapete, paredes, papel de parede, janelas, porta e acabamento
    val aparencia = AparenciaDoAndar.de(estado, andar, moveis, noite)
    val vetorial = { c: CascaDoAndar, p: Projecao -> formasDaCasca(c, aparencia, p, moveis) }
    val fundoDoAndar = arte?.fundo(aparencia, tw, Contorno, tracoFino.width, vetorial)
    val chao = ArrayList<Forma>()
    val paredes = ArrayList<Forma>()
    if (fundoDoAndar == null) {
        for (c in CamadasDaCasaPorDentro.cascas(aparencia)) {
            val f = vetorial(c, proj)
            if (c.grupo == GrupoDaCasca.LAJE || c.grupo == GrupoDaCasca.PISO || c.grupo == GrupoDaCasca.TAPETE) chao += f else paredes += f
        }
    }
    // quadros pintados no ateliê e diplomas, nas paredes da sala
    val salaCriativa = formasDaSalaCriativa(andar, proj, estado)
    // o que escurece quando a luz do cômodo apaga: o piso e as paredes do fundo dele
    val chaoDoComodo = HashMap<Comodo, Path>()
    val paredesDoComodo = HashMap<Comodo, List<Forma>>()
    for (c in PlantaDaCasa.comodosDo(andar)) {
        val l = PlantaDaCasa.limitesDo(c)
        chaoDoComodo[c] = proj.piso(l.x0, l.y0, l.x1, l.y1)
        paredesDoComodo[c] = buildList {
            add(Forma(proj.paredeY(0f, l.x0, l.x1, 0f, h), Escuro, contorno = false))
            if (l.x0 == 0f) add(Forma(proj.paredeX(0f, 0f, fundo, 0f, h), Escuro, contorno = false))
        }
    }

    // ---- móveis
    val naParede = ArrayList<Peca>()
    val soltas = ArrayList<Peca>()
    for (m in moveis) {
        if (m.noChao) continue
        val p = montarPeca(m, proj, estado, perfil, medidor, densidade, arte)
        if (m.naParede) naParede += p else soltas += p
    }
    // pedaços da parede interna (baixinha, cortada para ver dentro)
    for (c in PlantaDaCasa.paredesInternas(andar)) {
        val ref = CamadasDaCasaPorDentro.referenciaDaParedeInterna(c.x0, c.y0, c.x1, c.y1)
        val sprite = arte?.sprite(CamadasDaCasaPorDentro.PAREDE_INTERNA, tw, proj.origemX, proj.origemY, ref)
        soltas += Peca(
            movel = null,
            caixa = c,
            comodo = null,
            formas = if (sprite != null) emptyList() else proj.caixa(c.x0, c.y0, c.x1, c.y1, c.z0, c.z1, Color(0xFFF3E6D3)),
            toque = Rect.Zero,
            ancora = Offset.Zero,
            sprite = sprite,
        )
    }
    val ordem = PlantaDaCasa.ordenarParaDesenho(soltas.map { it.caixa })
    val pecas = ordem.map { soltas[it] }

    // ---- lâmpadas penduradas, uma por cômodo
    val luzes = PlantaDaCasa.comodosDo(andar).map { c ->
        // No banheiro, a lâmpada do meio caía em cima do patinho da banheira: lá ela fica mais à direita,
        // longe do patinho, e com o fio mais curto.
        val ponto = CamadasDaCasaPorDentro.luzDo(c)
        val l = proj.ponto(ponto.x, ponto.y, ponto.z)
        val r = tw * 0.2f
        Luz(
            comodo = c,
            lampada = l,
            corda = proj.ponto(ponto.x, ponto.y, 3.5f),
            cupula = Path().apply {
                moveTo(l.x - r * 0.35f, l.y - r * 0.9f)
                lineTo(l.x + r * 0.35f, l.y - r * 0.9f)
                lineTo(l.x + r, l.y - r * 0.05f)
                lineTo(l.x - r, l.y - r * 0.05f)
                close()
            },
            brilho = Brush.radialGradient(listOf(Color(0x99FFF3B0), Color(0x00FFF3B0)), center = l, radius = tw * 0.9f),
            chao = chaoDoComodo.getValue(c),
            paredes = paredesDoComodo[c] ?: emptyList(),
            acesa = arte?.sprite(CamadasDaCasaPorDentro.idDaLampada(true), tw, proj.origemX, proj.origemY, ponto),
            apagada = arte?.sprite(CamadasDaCasaPorDentro.idDaLampada(false), tw, proj.origemX, proj.origemY, ponto),
        )
    }

    val fim = proj.ponto(lado, fundo)
    val sombra = if (noChao) Rect(proj.x(0f, fundo) - tw * 0.6f, fim.y - tw * 0.6f, proj.x(lado, 0f) + tw * 0.6f, fim.y + tw * 0.3f) else null
    // realce dos ladrilhos para onde o móvel arrastado vai (verde: cabe; vermelho: não cabe)
    val realce = destaque?.let { d ->
        val caminho = Path()
        with(proj) { for ((x, y) in d.ladrilhos) caminho.losango(x + 0.06f, y + 0.06f, x + 0.94f, y + 0.94f, 0.02f) }
        Forma(caminho, if (d.valido) Color(0x8C3DBE6B) else Color(0x8CEF476F))
    }
    return GeoAndar(andar, proj, fundoDoAndar, chao, paredes, salaCriativa, naParede, pecas, luzes, sombra, realce)
}

/**
 * O desenho vetorial antigo de uma peça do fundo do andar (a reserva quando a imagem falta, e o papel de parede,
 * que é sempre do app), na projeção [proj].
 */
private fun formasDaCasca(c: CascaDoAndar, ap: AparenciaDoAndar, proj: Projecao, moveis: List<Movel>): List<Forma> {
    val tw = proj.ladrilho
    val lado = PlantaDaCasa.LARGURA.toFloat()
    val fundo = PlantaDaCasa.FUNDO.toFloat()
    val h = PlantaDaCasa.ALTURA_PAREDE
    val meio = PlantaDaCasa.meio(ap.andar).toFloat()
    return when (c.grupo) {
        // espessura do piso (a casa parece uma maquete)
        GrupoDaCasca.LAJE -> listOf(
            Forma(proj.paredeX(lado, 0f, fundo, -0.35f, 0f), Color(0xFFB98A5E)),
            Forma(proj.paredeY(fundo, 0f, lado, -0.35f, 0f), Color(0xFFD6A574)),
        )
        GrupoDaCasca.PISO -> {
            val cm = c.comodo ?: return emptyList()
            val l = PlantaDaCasa.limitesDo(cm)
            val piso = pisoDo(cm)
            val linhas = Path()
            with(proj) {
                if (piso.madeira) {
                    // tábuas compridas, com as emendas desencontradas
                    val tabua = 0.5f
                    var y = l.y0
                    var fileira = 0
                    while (y < l.y1 - 0.01f) {
                        linhas.losango(l.x0, y - 0.012f, l.x1, y + 0.012f)
                        var x = l.x0 + if (fileira % 2 == 0) 1.3f else 0.6f
                        while (x < l.x1 - 0.1f) {
                            linhas.losango(x - 0.012f, y, x + 0.012f, min(y + tabua, l.y1))
                            x += 1.6f
                        }
                        y += tabua
                        fileira++
                    }
                } else {
                    // lajotas: rejunte fininho, da mesma cor clara
                    var x = l.x0 + 1f
                    while (x < l.x1 - 0.01f) {
                        linhas.losango(x - 0.012f, l.y0, x + 0.012f, l.y1)
                        x += 1f
                    }
                    var y = l.y0 + 1f
                    while (y < l.y1 - 0.01f) {
                        linhas.losango(l.x0, y - 0.012f, l.x1, y + 0.012f)
                        y += 1f
                    }
                }
            }
            listOf(Forma(proj.piso(l.x0, l.y0, l.x1, l.y1), piso.base, contorno = false), Forma(linhas, piso.linha, contorno = false))
        }
        GrupoDaCasca.TAPETE -> moveis.firstOrNull { it.noChao }?.let { m ->
            listOf(
                Forma(proj.piso(m.x0, m.y0, m.x1, m.y1, 0.01f), Color(0xFFF2D9A0), contorno = false),
                Forma(proj.piso(m.x0 + 0.15f, m.y0 + 0.15f, m.x1 - 0.15f, m.y1 - 0.15f, 0.012f), Color(0xFFFFF0C9), contorno = false),
                Forma(proj.piso(m.x0 + 0.45f, m.y0 + 0.45f, m.x1 - 0.45f, m.y1 - 0.45f, 0.014f), Color(0xFFF7C9A8), contorno = false),
            )
        } ?: emptyList()
        // paredes do fundo (com a cor de cada cômodo, a mesma da casa de fora)
        GrupoDaCasca.PAREDE -> {
            val cm = c.comodo ?: return emptyList()
            val l = PlantaDaCasa.limitesDo(cm)
            val cor = Color(ap.cores[cm] ?: cm.cores[0])
            val formas = ArrayList<Forma>()
            formas += Forma(proj.paredeY(0f, l.x0, l.x1, 0f, h), cor, contorno = false)
            formas += Forma(proj.paredeY(0f, l.x0, l.x1, 0f, 0.16f), escurecer(cor, 0.82f), contorno = false)
            if (l.x0 == 0f) {
                val corX = escurecer(cor, 0.9f)
                formas += Forma(proj.paredeX(0f, 0f, fundo, 0f, h), corX, contorno = false)
                formas += Forma(proj.paredeX(0f, 0f, fundo, 0f, 0.16f), escurecer(corX, 0.82f), contorno = false)
            }
            // azulejos no banheiro (até meia parede) e na cozinha (faixa atrás do fogão), como na fachada
            val faixa = when (cm) {
                Comodo.BANHEIRO -> 0.16f to 1.25f
                Comodo.COZINHA -> 0.9f to 1.55f
                else -> null
            }
            if (faixa != null) {
                val (z0, z1) = faixa
                val rejunte = Path()
                var x = l.x0 + 0.35f
                while (x < l.x1) {
                    rejunte.addPath(proj.paredeY(0.002f, x - 0.01f, x + 0.01f, z0, z1))
                    x += 0.35f
                }
                var z = z0 + 0.3f
                while (z < z1) {
                    rejunte.addPath(proj.paredeY(0.002f, l.x0, l.x1, z - 0.012f, z + 0.012f))
                    z += 0.3f
                }
                formas += Forma(proj.paredeY(0.001f, l.x0, l.x1, z0, z1), Color(0x30FFFFFF), contorno = false)
                formas += Forma(rejunte, Color(0x14000000), contorno = false)
            }
            formas
        }
        // papel de parede comprado na loja (listras, bolinhas ou estrelinhas): sempre desenhado pelo app
        GrupoDaCasca.PAPEL -> {
            val padrao = when (ap.papel) {
                1 -> Path().apply {
                    var t = 0.25f
                    while (t < lado) {
                        addPath(proj.paredeY(0f, t - 0.06f, t + 0.06f, 0.16f, h))
                        if (t < fundo) addPath(proj.paredeX(0f, t - 0.06f, t + 0.06f, 0.16f, h))
                        t += 0.5f
                    }
                }
                2, 3 -> Path().apply {
                    var t = 0.35f
                    var linha = 0
                    while (t < lado) {
                        var z = 0.5f + (linha % 2) * 0.3f
                        while (z < h - 0.2f) {
                            val pontos = if (t < fundo) listOf(proj.ponto(t, 0f, z), proj.ponto(0f, t, z)) else listOf(proj.ponto(t, 0f, z))
                            for (p in pontos) {
                                if (ap.papel == 2) addOval(Rect(p, tw * 0.05f)) else addPath(caminhoEstrela(p, tw * 0.07f))
                            }
                            z += 0.6f
                        }
                        t += 0.6f
                        linha++
                    }
                }
                else -> null
            }
            if (padrao == null) emptyList() else listOf(Forma(padrao, if (ap.papel == 3) Color(0x80FFC93C) else Color(0x1F000000), contorno = false))
        }
        // janelas: uma em cada cômodo, como na fachada; a do banheiro é redonda
        GrupoDaCasca.JANELA -> {
            val vidro = Color(0xFFBDE7FF)
            val moldura = Color(0xFFFFFFFF)
            if (c.indice < 0) {
                val cx = c.referencia.x
                listOf(
                    Forma(proj.circuloY(0.004f, cx, 2.05f, 0.5f), moldura),
                    Forma(proj.circuloY(0.005f, cx, 2.05f, 0.4f), vidro, contorno = false),
                )
            } else {
                val a = c.referencia.x - CamadasDaCasaPorDentro.MEIA_JANELA
                val b = c.referencia.x + CamadasDaCasaPorDentro.MEIA_JANELA
                listOf(
                    Forma(proj.paredeY(0.004f, a - 0.08f, b + 0.08f, 1.32f, 2.58f), moldura),
                    Forma(proj.paredeY(0.005f, a, b, 1.4f, 2.5f), vidro, contorno = false),
                    Forma(proj.paredeY(0.006f, (a + b) / 2 - 0.03f, (a + b) / 2 + 0.03f, 1.4f, 2.5f), moldura, contorno = false),
                    Forma(proj.paredeY(0.006f, a, b, 1.92f, 1.98f), moldura, contorno = false),
                    // cortininhas cor-de-rosa, iguais às de fora
                    Forma(proj.paredeY(0.007f, a - 0.1f, a + 0.06f, 1.3f, 2.62f), Color(0xFFFF8FB1), contorno = false),
                    Forma(proj.paredeY(0.007f, b - 0.06f, b + 0.1f, 1.3f, 2.62f), Color(0xFFFF8FB1), contorno = false),
                )
            }
        }
        // a porta da rua, na sala (com a cor escolhida na loja de fora)
        GrupoDaCasca.PORTA -> {
            val corPorta = Color(Loja.coresDaPorta.getOrElse(ap.corPorta) { Loja.coresDaPorta[0] })
            val (a, b) = PlantaDaCasa.portaDaRua.x to PlantaDaCasa.portaDaRua.y
            val centro = proj.ponto(0.006f, (a + b) / 2, 1.75f)
            listOf(
                Forma(proj.paredeX(0.004f, a - 0.08f, b + 0.08f, 0f, 2.25f), Color(0xFFFDF3E4)),
                Forma(proj.paredeX(0.005f, a, b, 0f, 2.15f), corPorta),
                Forma(Path().apply { addOval(Rect(centro, tw * 0.13f)) }, Color(0xFFFFE9A8)),
                Forma(Path().apply { addOval(Rect(proj.ponto(0.006f, b - 0.18f, 1.0f), tw * 0.045f)) }, Color(0xFFFFD166), contorno = false),
            )
        }
        // a divisória de madeira entre os dois cômodos, e a espessura e o acabamento no alto das paredes
        GrupoDaCasca.ACABAMENTO -> {
            val acabamento = Color(0xFFFDF3E4)
            listOf(
                Forma(proj.paredeY(0.003f, meio - 0.05f, meio + 0.05f, 0f, h), Color(0xFFE9D8C4), contorno = false),
                Forma(proj.piso(-0.14f, -0.14f, lado, 0f, h), acabamento),
                Forma(proj.piso(-0.14f, 0f, 0f, fundo, h), acabamento),
                Forma(proj.paredeY(fundo, -0.14f, 0f, -0.35f, h), Color(0xFFE8D6BE)),
                Forma(proj.paredeX(lado, -0.14f, 0f, -0.35f, h), Color(0xFFD9C3A5)),
            )
        }
    }
}

/** Escurece as imagens de um cômodo com a luz apagada (perto do véu escuro que cobre as formas). */
private val FiltroEscuro = multiplicarPor(0xFF6F7590)

/** Onde termina a porta da esquerda do guarda-roupa aberto, em pixels de `dentro_guardaroupa_aberto` (as roupas ficam à direita). */
private const val BORDA_DA_PORTA_DO_GUARDA_ROUPA = 118f

/**
 * O móvel com a arte 3D: a imagem no lugar das formas (que continuam medindo a área de toque), com o que fica
 * vetorial (tela da TV, comidas da geladeira, roupas do guarda-roupa, água, bolhas e chamas) nos pontos da imagem.
 * Sem a imagem (ou sem as duas, nos móveis que abrem), o desenho antigo.
 */
private fun montarPeca(m: Movel, proj: Projecao, estado: EstadoJogo, perfil: Perfil, medidor: TextMeasurer, densidade: Density, arte: ArteDaCasaPorDentro?): Peca {
    val p = montarPecaVetorial(m, proj, estado, perfil, medidor, densidade)
    if (arte == null) return p
    val tw = proj.ladrilho
    val ref = CamadasDaCasaPorDentro.referenciaDoMovel(m)
    fun sprite(aberto: Boolean) = arte.sprite(CamadasDaCasaPorDentro.idDoMovel(m, aberto), tw, proj.origemX, proj.origemY, ref)
    val fechado = sprite(false) ?: return p
    val aberto = if (CamadasDaCasaPorDentro.abre(m)) sprite(true) ?: return p else null
    fun ancora(s: SpriteNaTela, nome: String): Offset? = CamadasDaCasaPorDentro.ancora(s.info, s.lugar, nome)?.let { (x, y) -> Offset(x, y) }
    val base = p.copy(sprite = fechado, spriteAberto = aberto, formas = emptyList(), fechado = emptyList(), aberto = emptyList())
    return when (m.id) {
        "tv" -> {
            // A tela azul da TV da arte (medida na imagem): o desenho animado aparece só nela.
            val tela = proj.paredeY(m.y1 + 0.002f, m.x0 + 0.14f, m.x1 - 0.22f, 0.3f, 0.79f)
            base.copy(tela = tela, telaRect = tela.getBounds())
        }
        "pia" -> {
            val bico = ancora(fechado, "bico")
            val bacia = ancora(fechado, "bacia")
            if (bico != null && bacia != null) base.copy(pontos = listOf(bico, bacia)) else base
        }
        "banheira" -> ancora(fechado, "agua")?.let { agua -> base.copy(pontos = listOf(agua) + p.pontos.drop(1)) } ?: base
        "geladeira" -> {
            val s = aberto ?: return base
            base.copy(
                emojisAbertos = p.emojisAbertos.mapIndexed { i, (l, em) ->
                    val a = ancora(s, "comida_$i") ?: return@mapIndexed l to em
                    l to Offset(a.x - l.size.width / 2f, a.y - l.size.height * 0.92f)
                },
            )
        }
        "guardaroupa" -> {
            val s = aberto ?: return base
            // As roupas que a criança tem (as compradas primeiro, depois as cores de sempre), uma por cabide.
            val cores = Loja.noGuardaRoupa(estado).map { corDoTraje(it.valor, perfil) } + Aparencia.roupas.map { Color(it) }
            // Meia largura de uma roupa ao longo do cabideiro (0,1 ladrilho na direção y da planta).
            val meia = Offset(-tw * 0.05f, tw * 0.025f)
            val uz = proj.unidadeZ
            val roupas = ArrayList<Forma>()
            for (i in 0 until 6) {
                val a = ancora(s, "cabide_$i") ?: continue
                val cor = cores.getOrNull(i) ?: break
                val topo = a.y + uz * 0.04f
                val pe = a.y + uz * 0.78f
                roupas += Forma(
                    proj.quad(
                        Offset(a.x - meia.x, topo - meia.y), Offset(a.x + meia.x, topo + meia.y),
                        Offset(a.x + meia.x, pe + meia.y), Offset(a.x - meia.x, pe - meia.y),
                    ),
                    cor,
                )
            }
            val k = s.lugar.largura / s.info.largura
            base.copy(extrasAbertos = roupas, recorteAberto = s.lugar.esquerda + BORDA_DA_PORTA_DO_GUARDA_ROUPA * k)
        }
        else -> base
    }
}

/** Converte um móvel em formas prontas (desenho isométrico feito de caixinhas). */
private fun montarPecaVetorial(m: Movel, proj: Projecao, estado: EstadoJogo, perfil: Perfil, medidor: TextMeasurer, densidade: Density): Peca {
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

    if (m.acao == Acao.ESCADA) return montarEscada(m, proj, medidor, densidade, toque, ancora, caixaDoMovel)
    if (m.id == "patinho") return montarPatinho(m, proj, caixaDoMovel, minimo)
    montarEnfeite(m, proj, caixaDoMovel, minimo)?.let { return it }

    return with(proj) {
        when (m.id) {
            "cama" -> {
                val cobertor = CorDoCobertor
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
                // Encostado na parede x = 0: a frente (com as portas) olha para +x.
                val ym = (y0 + y1) / 2
                val meia = (y1 - y0) / 2 - 0.04f
                val portaCor = Color(0xFFD9A06E)
                val roupas = ArrayList<Forma>()
                // Cabides: as roupas que a criança tem (as compradas primeiro, depois as cores de sempre).
                val cores = Loja.noGuardaRoupa(estado).map { corDoTraje(it.valor, perfil) } + Aparencia.roupas.map { Color(it) }
                var a = y0 + 0.14f
                var i = 0
                while (a + 0.2f < y1 - 0.1f && i < cores.size) {
                    roupas += Forma(paredeX(x1 - 0.05f, a, a + 0.2f, 0.95f, 1.75f), cores[i])
                    a += 0.26f
                    i++
                }
                peca(
                    caixa(x0, y0, x1, y1, 0f, 2.2f, Madeira) + caixa(x0, y0 - 0.04f, x1 + 0.04f, y1 + 0.04f, 2.2f, 2.3f, escurecer(Madeira, 0.85f)),
                    fechado = listOf(
                        Forma(paredeX(x1 + 0.001f, y0 + 0.06f, ym - 0.02f, 0.08f, 2.12f), portaCor),
                        Forma(paredeX(x1 + 0.001f, ym + 0.02f, y1 - 0.06f, 0.08f, 2.12f), portaCor),
                        Forma(paredeX(x1 + 0.002f, ym - 0.12f, ym - 0.07f, 1.0f, 1.3f), Color(0xFF8D5A3B), contorno = false),
                        Forma(paredeX(x1 + 0.002f, ym + 0.07f, ym + 0.12f, 1.0f, 1.3f), Color(0xFF8D5A3B), contorno = false),
                    ),
                    aberto = listOf(
                        Forma(paredeX(x1 + 0.001f, y0 + 0.06f, y1 - 0.06f, 0.08f, 2.12f), Color(0xFF5A3A22)),
                        Forma(paredeX(x1 + 0.002f, y0 + 0.08f, y1 - 0.08f, 1.78f, 1.82f), Metal, contorno = false),
                    ) + roupas + listOf(
                        Forma(paredeY(y0 + 0.06f, x1, x1 + meia, 0.08f, 2.12f), portaCor),
                        Forma(paredeY(y1 - 0.06f, x1, x1 + meia, 0.08f, 2.12f), clarear(portaCor, 0.15f)),
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
                    Forma(paredeY(0.005f, x0 - 0.06f, x1 + 0.06f, z0 - 0.06f, z1 + 0.06f), Color(0xFFD4A24C)),
                    Forma(paredeY(0.006f, x0, x1, z0, z1), Color(0xFFBDE7FF), contorno = false),
                    Forma(paredeY(0.007f, x0, x1, z0, z0 + 0.25f), Color(0xFF7CC56B), contorno = false),
                    Forma(Path().apply { addOval(Rect(ponto(x1 - 0.22f, 0.007f, z1 - 0.18f), tw * 0.06f)) }, Color(0xFFFFC93C), contorno = false),
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
                // Azul como o da fachada, encostado na parede do fundo (o encosto atrás, os braços dos lados).
                val cor = CorDoSofa
                val meioX = (x0 + x1) / 2
                val braco = 0.24f
                peca(
                    caixa(x0, y0, x1, y1, 0f, 0.36f, escurecer(cor, 0.9f)) +
                        caixa(x0, y0, x1, y0 + 0.28f, 0.36f, 0.95f, cor) +
                        caixa(x0, y0 + 0.28f, x0 + braco, y1, 0.36f, 0.62f, cor) +
                        caixa(x0 + braco + 0.02f, y0 + 0.28f, meioX - 0.02f, y1 - 0.03f, 0.36f, 0.5f, clarear(cor, 0.2f)) +
                        caixa(meioX + 0.02f, y0 + 0.28f, x1 - braco - 0.02f, y1 - 0.03f, 0.36f, 0.5f, clarear(cor, 0.2f)) +
                        caixa(x1 - braco, y0 + 0.28f, x1, y1, 0.36f, 0.62f, cor),
                )
            }
            "geladeira" -> {
                // Encostada na parede da cozinha, de frente; a porta abre para a direita (dobradiça em x1).
                val largura = x1 - x0
                val comidas = Loja.naGeladeira(estado)
                val emojis = ArrayList<Pair<TextLayoutResult, Offset>>()
                comidas.take(9).forEachIndexed { i, c ->
                    val prateleira = i / 3
                    val z = 0.12f + prateleira * 0.5f
                    val x = x0 + 0.18f + (i % 3) * (largura - 0.36f) / 2f
                    val l = medirEmoji(medidor, densidade, c.valor, tw * 0.26f)
                    val p = ponto(x, y1, z)
                    emojis += l to Offset(p.x - l.size.width / 2f, p.y - l.size.height * 0.92f)
                }
                peca(
                    caixa(x0, y0, x1, y1, 0f, 2.0f, Color(0xFFF4F7FA)),
                    fechado = listOf(
                        Forma(paredeY(y1 + 0.001f, x0 + 0.04f, x1 - 0.04f, 1.3f, 1.34f), Color(0xFFC9D3DB), contorno = false),
                        Forma(paredeY(y1 + 0.002f, x0 + 0.1f, x0 + 0.16f, 0.75f, 1.2f), Metal, contorno = false),
                        Forma(paredeY(y1 + 0.002f, x0 + 0.1f, x0 + 0.16f, 1.45f, 1.75f), Metal, contorno = false),
                    ),
                    aberto = listOf(
                        Forma(paredeY(y1 + 0.001f, x0 + 0.06f, x1 - 0.06f, 0.08f, 1.94f), Color(0xFFDDF3FF)),
                        Forma(paredeY(y1 + 0.002f, x0 + 0.06f, x1 - 0.06f, 0.6f, 0.63f), Color.White, contorno = false),
                        Forma(paredeY(y1 + 0.002f, x0 + 0.06f, x1 - 0.06f, 1.1f, 1.13f), Color.White, contorno = false),
                        Forma(paredeY(y1 + 0.002f, x0 + 0.06f, x1 - 0.06f, 1.5f, 1.53f), Color.White, contorno = false),
                        Forma(paredeX(x1, y1, y1 + largura - 0.02f, 0.05f, 1.95f), Color(0xFFE8EEF2)),
                        Forma(paredeX(x1 + 0.001f, y1 + 0.08f, y1 + largura - 0.1f, 0.5f, 0.56f), Color(0xFFB9D7E8), contorno = false),
                        Forma(paredeX(x1 + 0.001f, y1 + 0.08f, y1 + largura - 0.1f, 1.2f, 1.26f), Color(0xFFB9D7E8), contorno = false),
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
                    Forma(paredeY(y1 + 0.001f, x0 + 0.15f, x1 - 0.15f, 0.18f, 0.6f), Color(0xFF3A3A44)) +
                    Forma(paredeY(y1 + 0.002f, x0 + 0.15f, x1 - 0.15f, 0.66f, 0.7f), Metal, contorno = false) +
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

/**
 * O patinho de borracha amarelo boiando na banheira, desenhado à mão: corpo redondo com o rabinho
 * levantado, cabeça grande, bico laranja, olho brilhante, asinha e bochecha rosada.
 */
private fun montarPatinho(m: Movel, proj: Projecao, caixa: Caixa, minimo: Float): Peca {
    val tw = proj.ladrilho
    val base = proj.ponto(m.centroX, m.centroY, m.z)
    val s = tw * 0.62f
    val amarelo = Color(0xFFFFD43B)
    val corpo = Path().apply { addOval(Rect(base.x - s * 0.42f, base.y - s * 0.4f, base.x + s * 0.4f, base.y + s * 0.03f)) }
    val rabo = Path().apply {
        moveTo(base.x + s * 0.2f, base.y - s * 0.3f)
        cubicTo(base.x + s * 0.46f, base.y - s * 0.36f, base.x + s * 0.46f, base.y - s * 0.36f, base.x + s * 0.52f, base.y - s * 0.52f)
        cubicTo(base.x + s * 0.5f, base.y - s * 0.12f, base.x + s * 0.5f, base.y - s * 0.12f, base.x + s * 0.3f, base.y - s * 0.05f)
        close()
    }
    val centroCabeca = Offset(base.x - s * 0.2f, base.y - s * 0.56f)
    val raio = s * 0.25f
    val cabeca = Path().apply { addOval(Rect(centroCabeca, raio)) }
    val silhueta = Path().apply {
        val corpoERabo = Path().apply { op(corpo, rabo, PathOperation.Union) }
        op(corpoERabo, cabeca, PathOperation.Union)
    }
    val bico = Path().apply {
        moveTo(centroCabeca.x - raio * 0.7f, centroCabeca.y - raio * 0.02f)
        cubicTo(centroCabeca.x - raio * 1.6f, centroCabeca.y - raio * 0.05f, centroCabeca.x - raio * 1.6f, centroCabeca.y - raio * 0.05f, centroCabeca.x - raio * 1.5f, centroCabeca.y + raio * 0.22f)
        cubicTo(centroCabeca.x - raio * 1.2f, centroCabeca.y + raio * 0.48f, centroCabeca.x - raio * 1.2f, centroCabeca.y + raio * 0.48f, centroCabeca.x - raio * 0.62f, centroCabeca.y + raio * 0.36f)
        close()
    }
    val asa = Path().apply {
        moveTo(base.x - s * 0.04f, base.y - s * 0.25f)
        cubicTo(base.x + s * 0.16f, base.y - s * 0.36f, base.x + s * 0.16f, base.y - s * 0.36f, base.x + s * 0.3f, base.y - s * 0.22f)
        cubicTo(base.x + s * 0.14f, base.y - s * 0.05f, base.x + s * 0.14f, base.y - s * 0.05f, base.x - s * 0.04f, base.y - s * 0.25f)
        close()
    }
    val olho = Offset(centroCabeca.x - raio * 0.22f, centroCabeca.y - raio * 0.18f)
    val formas = listOf(
        // ondinhas na água em volta
        Forma(Path().apply { addOval(Rect(base.x - s * 0.62f, base.y - s * 0.07f, base.x + s * 0.62f, base.y + s * 0.13f)) }, Color(0x66FFFFFF), contorno = false),
        Forma(silhueta, amarelo),
        Forma(Path().apply { addOval(Rect(base.x - s * 0.3f, base.y - s * 0.36f, base.x - s * 0.02f, base.y - s * 0.26f)) }, Color(0x88FFFFFF), contorno = false),
        Forma(asa, Color(0xFFF5B700), contorno = false),
        Forma(bico, Color(0xFFFF8A1F)),
        Forma(Path().apply { addOval(Rect(Offset(centroCabeca.x + raio * 0.2f, centroCabeca.y + raio * 0.32f), raio * 0.16f)) }, Color(0x66FF6F91), contorno = false),
        Forma(Path().apply { addOval(Rect(olho, raio * 0.17f)) }, Color(0xFF2B2B35), contorno = false),
        Forma(Path().apply { addOval(Rect(Offset(olho.x - raio * 0.05f, olho.y - raio * 0.06f), raio * 0.06f)) }, Color.White, contorno = false),
    )
    val c = Offset(base.x, base.y - s * 0.35f)
    val lado = max(s * 1.2f, minimo)
    val toque = Rect(c.x - lado / 2, c.y - lado / 2, c.x + lado / 2, c.y + lado / 2)
    return Peca(m, caixa, m.comodo, formas, toque, base)
}

/** Um enfeite desenhado à mão (gato, ursinho, violão...) vira uma peça tocável, com área de toque de dedo de criança. */
private fun montarEnfeite(m: Movel, proj: Projecao, caixa: Caixa, minimo: Float): Peca? {
    val f = enfeiteDesenhado(m, proj) ?: return null
    val b = proj.ponto(m.centroX, m.centroY, m.z)
    val caixaDeToque = f.fold(Rect(b.x, b.y, b.x, b.y)) { acc, forma ->
        val r = forma.caminho.getBounds()
        Rect(min(acc.left, r.left), min(acc.top, r.top), max(acc.right, r.right), max(acc.bottom, r.bottom))
    }
    val c = caixaDeToque.center
    val toque = Rect(
        c.x - max(caixaDeToque.width, minimo) / 2, c.y - max(caixaDeToque.height, minimo) / 2,
        c.x + max(caixaDeToque.width, minimo) / 2, c.y + max(caixaDeToque.height, minimo) / 2,
    )
    return Peca(m, caixa, m.comodo, f, toque, b)
}


/** A escada: embaixo, degraus de madeira subindo para o fundo; em cima, o vão com os degraus descendo e o corrimão. */
private fun montarEscada(m: Movel, proj: Projecao, medidor: TextMeasurer, densidade: Density, toque: Rect, ancora: Offset, caixaDaPeca: Caixa): Peca {
    val (x0, y0, x1, y1) = listOf(m.x0, m.y0, m.x1, m.y1)
    val tw = proj.ladrilho
    val formas = ArrayList<Forma>()
    val sobe = m.andar == Andar.DE_BAIXO
    with(proj) {
        if (sobe) {
            val n = 7
            val d = (y1 - y0) / n
            val hs = m.altura / n
            for (i in 0 until n) formas += caixa(x0, y0, x1, y1 - i * d, i * hs, (i + 1) * hs, if (i % 2 == 0) Madeira else clarear(Madeira, 0.08f))
        } else {
            val n = 5
            val d = (y1 - y0) / n
            formas += Forma(piso(x0, y0, x1, y1, 0.004f), Color(0xFF5C3D28))
            for (k in 0 until n) {
                formas += Forma(piso(x0 + 0.06f, y0 + k * d + d * 0.45f, x1 - 0.06f, y0 + (k + 1) * d, 0.006f), escurecer(Madeira, 0.75f + k * 0.05f), contorno = false)
            }
            // corrimão do lado de dentro do quarto
            var y = y0 + 0.1f
            while (y < y1 - 0.05f) {
                formas += caixa(x1 - 0.06f, y, x1 - 0.01f, y + 0.05f, 0f, m.altura - 0.08f, Madeira)
                y += 0.5f
            }
            formas += caixa(x1 - 0.08f, y0, x1, y1, m.altura - 0.08f, m.altura, escurecer(Madeira, 0.9f))
        }
    }
    // Setinha que balança em cima da escada: para onde ela leva. A de descer fica logo acima do
    // corrimão e mais para a frente da escada, longe da lâmpada pendurada no meio do quarto.
    val seta = medirEmoji(medidor, densidade, if (sobe) "⬆️" else "⬇️", tw * 0.42f)
    val topo = if (sobe) {
        proj.ponto((x0 + x1) / 2, (y0 + y1) / 2, m.altura + 0.15f)
    } else {
        proj.ponto((x0 + x1) / 2, y0 + (y1 - y0) * 0.72f, m.altura + 0.05f)
    }
    val em = Offset(topo.x - seta.size.width / 2f, topo.y - seta.size.height)
    // A escada inteira é tocável, e também a setinha.
    val alvo = Rect(min(toque.left, em.x), min(toque.top, em.y), max(toque.right, em.x + seta.size.width), max(toque.bottom, em.y + seta.size.height))
    return Peca(m, caixaDaPeca, m.comodo, formas, alvo, ancora, emoji = seta, emojiEm = em)
}

private fun corDoTraje(traje: String, perfil: Perfil): Color = when (traje) {
    "pijama" -> Color(0xFF3B4C8C)
    "abelha" -> Color(0xFFFFC93C)
    "astronauta" -> Color(0xFFF2F4F8)
    "heroi" -> Color(0xFFE63946)
    "fada" -> Color(0xFFD9C2FF)
    // Roupas novas do guarda-roupa: a mesma cor com que a criança aparece vestida.
    "vestido", "macacao", "futebol", "bailarina", "chuva", "medico", "chef", "pirata", "realeza", "dinossauro" ->
        br.com.casinhadosaber.ui.componentes.corDaRoupaDoTraje(traje, perfil)
    else -> clarear(Color(perfil.corDaRoupa), 0.3f)
}

// ---------------------------------------------------------------------------------------------
// Desenho de cada quadro (sem montar caminhos novos; só lê a geometria pronta e o estado da cena)
// ---------------------------------------------------------------------------------------------

/** Sol e nuvenzinhas na faixa de céu acima da casa (só quando sobra céu, como no celular em pé). */
private fun DrawScope.desenharCeu(g: Geometria, t: Float) {
    val tw = g.ladrilho
    val livre = g.ceu
    if (livre < tw * 0.9f) return
    val w = g.larguraDaCena
    // Sol no alto à direita (o telhado desce para a direita: ali sobra mais céu), com raios girando devagar.
    val sol = Offset(w * 0.86f, livre * 0.5f + tw * 0.55f)
    val raio = tw * 0.36f
    drawCircle(Color(0x33FFE066), raio * 1.9f, sol)
    rotate(t * 12f, pivot = sol) {
        for (k in 0 until 10) {
            val a = k * (2 * PI.toFloat() / 10)
            val de = Offset(sol.x + kotlin.math.cos(a) * raio * 1.25f, sol.y + sin(a) * raio * 1.25f)
            val ate = Offset(sol.x + kotlin.math.cos(a) * raio * 1.6f, sol.y + sin(a) * raio * 1.6f)
            drawLine(Color(0xFFFFC93C), de, ate, strokeWidth = tw * 0.07f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
    drawCircle(Color(0xFFFFD84D), raio, sol)
    drawCircle(Color(0xFFFFE98A), raio * 0.62f, Offset(sol.x - raio * 0.2f, sol.y - raio * 0.2f))
    // Nuvens fofas que passeiam um pouquinho para os lados.
    fun nuvem(c: Offset, tamanho: Float) {
        val cor = Color(0xF2FFFFFF)
        drawOval(cor, topLeft = Offset(c.x - tamanho * 0.75f, c.y - tamanho * 0.18f), size = Size(tamanho * 1.5f, tamanho * 0.42f))
        drawCircle(cor, tamanho * 0.32f, Offset(c.x - tamanho * 0.3f, c.y - tamanho * 0.08f))
        drawCircle(cor, tamanho * 0.42f, Offset(c.x + tamanho * 0.12f, c.y - tamanho * 0.2f))
        drawCircle(cor, tamanho * 0.26f, Offset(c.x + tamanho * 0.5f, c.y + tamanho * 0.0f))
    }
    nuvem(Offset(w * 0.42f + sin(t * 0.25f) * tw * 0.3f, livre * 0.42f), tw * 1.1f)
    nuvem(Offset(w * 0.12f + sin(t * 0.2f + 2f) * tw * 0.2f, livre * 0.8f), tw * 0.8f)
}

private fun DrawScope.desenharCena(g: Geometria, cena: Cena, perfil: Perfil) {
    val t = cena.tempo
    val apagadas = cena.luzApagada
    // O andar de cima primeiro: o de baixo fica na frente, como na casa de fora.
    desenharCeu(g, t)
    desenharFormas(g.telhado, Contorno, g.tracoFino)
    for (ga in g.andares) {
        val proj = ga.proj
        ga.sombra?.let {
            // Brilho macio no gramado em volta da casa (a casa "acende" o chão) e, por cima, a sombra.
            for (k in 3 downTo 1) {
                val f = 1f + k * 0.22f
                drawOval(
                    Color(0x1FFFFBE0),
                    topLeft = Offset(it.center.x - it.width * f / 2f, it.center.y - it.height * f / 2f),
                    size = Size(it.width * f, it.height * f),
                )
            }
            drawOval(Color(0x2A2E5A1E), topLeft = it.topLeft, size = it.size)
        }
        // chão e paredes do fundo: o bitmap composto do andar (arte 3D) ou as formas antigas
        val fundo = ga.fundo
        if (fundo != null) {
            val s = g.ladrilho / fundo.ladrilho
            val x0 = (proj.origemX + fundo.esquerda * s).roundToInt()
            val y0 = (proj.origemY + fundo.topo * s).roundToInt()
            drawImage(
                image = fundo.imagem,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(fundo.imagem.width, fundo.imagem.height),
                dstOffset = IntOffset(x0, y0),
                dstSize = IntSize((fundo.imagem.width * s).roundToInt(), (fundo.imagem.height * s).roundToInt()),
                filterQuality = FilterQuality.Low,
            )
        }
        desenharFormas(ga.chao, Contorno, g.tracoFino)
        for (luz in ga.luzes) if (luz.comodo in apagadas) drawPath(luz.chao, Escuro)
        desenharFormas(ga.paredes, Contorno, g.tracoFino)
        desenharFormas(ga.salaCriativa, Contorno, g.tracoFino)
        for (luz in ga.luzes) if (luz.comodo in apagadas) sombrearFormas(luz.paredes, Escuro)
        for (i in ga.naParede.indices) desenharPeca(g, ga.naParede[i], cena, apagadas)
        ga.realce?.let {
            val pulso = 0.75f + 0.25f * sin(t * 7f)
            drawPath(it.caminho, it.cor, alpha = pulso)
            drawPath(it.caminho, Color.White.copy(alpha = 0.8f), style = g.traco)
        }

        // marca de onde a criança vai
        val marca = cena.marca
        val idadeMarca = t - cena.marcaEm
        if (marca != null && cena.marcaAndar == ga.andar && idadeMarca < 0.7f) {
            val p = proj.ponto(marca.x, marca.y)
            val r = g.ladrilho * (0.15f + idadeMarca * 0.4f)
            drawOval(
                Color.White.copy(alpha = 0.9f * (1f - idadeMarca / 0.7f)),
                topLeft = Offset(p.x - r, p.y - r / 2f), size = Size(r * 2f, r),
                style = g.traco,
            )
        }

        // móveis e paredes internas, do fundo para a frente, com a criança (e o irmãozinho) no lugar certo: quem
        // está mais ao fundo entra primeiro; cada peça vai antes do primeiro personagem que fica na frente dela.
        if (cena.andar == ga.andar) {
            val x = cena.x
            val y = cena.y
            val irmaoAntes = g.comIrmao && cena.irmaoX + cena.irmaoY < x + y
            val ax = if (irmaoAntes) cena.irmaoX else x
            val ay = if (irmaoAntes) cena.irmaoY else y
            val bx = if (irmaoAntes) x else cena.irmaoX
            val by = if (irmaoAntes) y else cena.irmaoY
            fun primeiro() = if (irmaoAntes) desenharIrmaoNaCena(g, proj, cena, perfil) else desenharCriancaNaCena(g, proj, cena, perfil)
            fun segundo() = if (irmaoAntes) desenharCriancaNaCena(g, proj, cena, perfil) else desenharIrmaoNaCena(g, proj, cena, perfil)
            for (i in ga.pecas.indices) {
                val p = ga.pecas[i]
                if (PlantaDaCasa.criancaNaFrente(ax, ay, p.caixa)) desenharPeca(g, p, cena, apagadas)
            }
            primeiro()
            if (g.comIrmao) {
                for (i in ga.pecas.indices) {
                    val p = ga.pecas[i]
                    if (!PlantaDaCasa.criancaNaFrente(ax, ay, p.caixa) && PlantaDaCasa.criancaNaFrente(bx, by, p.caixa)) desenharPeca(g, p, cena, apagadas)
                }
                segundo()
            }
            for (i in ga.pecas.indices) {
                val p = ga.pecas[i]
                val depois = !PlantaDaCasa.criancaNaFrente(ax, ay, p.caixa) && (!g.comIrmao || !PlantaDaCasa.criancaNaFrente(bx, by, p.caixa))
                if (depois) desenharPeca(g, p, cena, apagadas)
            }
            // acabou de subir ou descer a escada: um brilho em volta da criança
            val idadeTroca = t - cena.trocouEm
            if (idadeTroca < 0.6f) {
                val pe = proj.ponto(x, y)
                val f = idadeTroca / 0.6f
                val centro = Offset(pe.x, pe.y - g.alturaCrianca * 0.5f)
                drawCircle(Color.White.copy(alpha = 0.55f * (1f - f)), radius = g.alturaCrianca * (0.4f + 0.5f * f), center = centro)
                for (k in 0 until 6) {
                    val ang = k * PI.toFloat() / 3f + f * 2f
                    val r = g.alturaCrianca * (0.35f + 0.4f * f)
                    drawCircle(Cores.Estrela.copy(alpha = 1f - f), radius = g.ladrilho * 0.05f, center = Offset(centro.x + r * kotlin.math.cos(ang), centro.y + r * sin(ang)))
                }
            }
        } else {
            for (i in ga.pecas.indices) desenharPeca(g, ga.pecas[i], cena, apagadas)
        }

        // lâmpadas penduradas (tocar acende e apaga)
        for (luz in ga.luzes) {
            val acesa = luz.comodo !in apagadas
            val l = luz.lampada
            val sprite = if (acesa) luz.acesa else luz.apagada
            if (sprite != null) {
                // A lâmpada da arte (com o fio); o brilho continua do app.
                if (acesa) drawCircle(luz.brilho, radius = g.ladrilho * 0.9f, center = l)
                desenharSprite(sprite)
                continue
            }
            drawLine(Color(0xFF6B5B4B), luz.corda, l, strokeWidth = max(1f, g.ladrilho * 0.02f))
            if (acesa) drawCircle(luz.brilho, radius = g.ladrilho * 0.9f, center = l)
            drawCircle(if (acesa) Color(0xFFFFF3B0) else Color(0xFFB0B4C0), radius = g.ladrilho * 0.09f, center = Offset(l.x, l.y + g.ladrilho * 0.02f))
            drawPath(luz.cupula, if (acesa) Color(0xFFFF9F6E) else Color(0xFFB57B62))
            drawPath(luz.cupula, Contorno, style = g.tracoFino)
        }
    }
}

private fun DrawScope.desenharPeca(g: Geometria, p: Peca, cena: Cena, apagadas: Set<Comodo>) {
    val m = p.movel
    val t = cena.tempo
    val idade = if (m != null && cena.tocadoId == m.id) t - cena.tocadoEm else 10f
    val arrastado = cena.arrastando
    if (m != null && arrastado != null && (m.id == arrastado || PlantaDaCasa.apoios[m.id] == arrastado)) {
        // O móvel arrastado flutua um pouquinho acima do chão (o realce embaixo faz a sombra).
        val alto = g.ladrilho * (0.16f + 0.03f * sin(t * 7f))
        translate(top = -alto) { desenharPecaSemPulo(g, p, cena, apagadas) }
    } else if (idade < 0.45f) {
        // pulinho de quem foi tocado
        val s = 1f + 0.14f * sin(idade / 0.45f * PI.toFloat()) * (1f - idade / 0.45f)
        scale(s, s, pivot = p.ancora) { desenharPecaSemPulo(g, p, cena, apagadas) }
    } else if (m?.id == "patinho") {
        // O patinho balança devagar na água.
        translate(top = sin(t * 2.4f) * g.ladrilho * 0.02f) { desenharPecaSemPulo(g, p, cena, apagadas) }
    } else {
        desenharPecaSemPulo(g, p, cena, apagadas)
    }
}

private fun DrawScope.desenharPecaSemPulo(g: Geometria, p: Peca, cena: Cena, apagadas: Set<Comodo>) {
    val t = cena.tempo
    val escuro = p.comodo != null && p.comodo in apagadas
    val acao = p.movel?.acao
    val aberto = when (acao) {
        Acao.GELADEIRA -> cena.geladeiraAberta
        Acao.GUARDA_ROUPA -> cena.guardaRoupaAberto
        else -> false
    }
    val sprite = if (aberto) p.spriteAberto ?: p.sprite else p.sprite
    if (sprite != null) desenharSprite(sprite, if (escuro) FiltroEscuro else null)
    desenharFormas(p.formas, Contorno, g.tracoFino)
    if (aberto) {
        if (p.extrasAbertos.isNotEmpty()) {
            // Roupas nos cabides, atrás da porta da esquerda (que fica na frente delas).
            clipRect(left = p.recorteAberto.coerceAtLeast(-1e6f)) {
                desenharFormas(p.extrasAbertos, Contorno, g.tracoFino)
                if (escuro) sombrearFormas(p.extrasAbertos, Escuro)
            }
        }
        desenharFormas(p.aberto, Contorno, g.tracoFino)
        for (i in p.emojisAbertos.indices) {
            val (l, em) = p.emojisAbertos[i]
            drawText(l, topLeft = em)
        }
    } else {
        desenharFormas(p.fechado, Contorno, g.tracoFino)
    }
    p.emoji?.let {
        // A setinha da escada balança devagar, chamando para subir ou descer.
        val balanco = if (acao == Acao.ESCADA) sin(t * 3.2f) * g.ladrilho * 0.06f else 0f
        drawText(it, topLeft = Offset(p.emojiEm.x, p.emojiEm.y + balanco), alpha = if (escuro) 0.55f else 1f)
    }
    if (escuro) {
        sombrearFormas(p.formas, Escuro)
        sombrearFormas(if (aberto) p.aberto else p.fechado, Escuro)
    }
    val tw = g.ladrilho
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

private fun DrawScope.desenharCriancaNaCena(g: Geometria, proj: Projecao, cena: Cena, perfil: Perfil) {
    val t = cena.tempo
    val pe = proj.ponto(cena.x, cena.y)
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
                    // Em camadas 3D (a mesma criança das outras telas, com a roupa e o chapéu da loja); se não der,
                    // o desenho antigo.
                    val quadro = QuadroDoPersonagem(PoseDosBracos.PARADO, humor, olhosFechados)
                    val arte = g.arte
                    val emCamadas = g.criancaEmCamadas && arte != null &&
                        desenharPersonagemEmCamadas(arte.contexto, PersonagemDaArte.CRIANCA, perfil, quadro)
                    if (!emCamadas) desenharCrianca(perfil, null, olhosFechados, humor)
                }
            }
            // chapéu em emoji (o mesmo cálculo de Crianca.kt): só no desenho antigo ou num chapéu sem camada
            if (!g.criancaEmCamadas || CamadasDosPersonagens.chapeuEmEmoji(perfil)) g.chapeu?.let { l ->
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

/**
 * O irmãozinho andando junto (atrás da criança, pelo mesmo caminho), em camadas 3D como nas outras telas; se não
 * der, o desenho antigo. Dorme e pula junto com a criança.
 */
private fun DrawScope.desenharIrmaoNaCena(g: Geometria, proj: Projecao, cena: Cena, perfil: Perfil) {
    if (!g.comIrmao) return
    val t = cena.tempo
    val pe = proj.ponto(cena.irmaoX, cena.irmaoY)
    val altura = g.alturaIrmao
    val largura = g.larguraIrmao
    val andando = cena.andando
    val balanco = if (andando) abs(sin(cena.passada * PI.toFloat() * 1.6f + 1.2f)) * altura * 0.05f else 0f
    val idadePulo = t - cena.puloEm - 0.12f
    val pulo = if (idadePulo in 0f..0.5f) sin(idadePulo / 0.5f * PI.toFloat()) * altura * 0.18f else 0f
    val olhosFechados = t < cena.dormindoAte || (t % 3.1f) > 2.98f
    if (pulo > 0f) drawOval(Color(0x22000000), topLeft = Offset(pe.x - largura * 0.35f, pe.y - largura * 0.08f), size = Size(largura * 0.7f, largura * 0.16f))
    translate(pe.x - largura / 2f, pe.y - altura * 0.985f - balanco - pulo) {
        inset(0f, 0f, size.width - largura, size.height - altura) {
            // Ginga de bebê: balança de leve de um lado para o outro.
            val ginga = sin(t * 2.2f) * 2.5f
            scale(if (cena.irmaoVirado) -1f else 1f, 1f, pivot = center) {
                rotate(ginga, pivot = Offset(size.width / 2f, size.height)) {
                    val quadro = QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ, olhosFechados)
                    val arte = g.arte
                    val emCamadas = g.irmaoEmCamadas && arte != null &&
                        desenharPersonagemEmCamadas(arte.contexto, PersonagemDaArte.IRMAO, perfil, quadro)
                    if (!emCamadas) desenharIrmaozinho(perfil, null, olhosFechados, Humor.FELIZ)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Painéis: geladeira aberta, guarda-roupa aberto e a lojinha
// ---------------------------------------------------------------------------------------------

@Composable
private fun Painel(
    descricao: String,
    onFechar: () -> Unit,
    cabecalho: @Composable RowScope.() -> Unit,
    conteudo: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .shadow(10.dp, RoundedCornerShape(28.dp))
            .background(Color.White, RoundedCornerShape(28.dp))
            .border(4.dp, Color(0xFFEDE6FF), RoundedCornerShape(28.dp))
            .padding(12.dp)
            .semantics { contentDescription = descricao },
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            cabecalho()
            Spacer(Modifier.width(8.dp))
            BotaoRedondo("✖️", "Fechar", onFechar, tamanho = 48.dp, modifier = Modifier.align(Alignment.CenterVertically))
        }
        conteudo()
    }
}

@Composable
private fun PainelGeladeira(
    comidas: List<Produto>,
    onAba: (PrateleiraDaGeladeira, Boolean) -> Unit,
    onComer: (Produto) -> Unit,
    onLoja: () -> Unit,
    onFechar: () -> Unit,
) {
    var aba by remember { mutableStateOf(PrateleiraDaGeladeira.inicial(comidas)) }
    val daAba = PrateleiraDaGeladeira.filtrar(comidas, aba)
    Painel("Geladeira aberta", onFechar, cabecalho = {
        // Abas de verdade: fundo de aba, a escolhida colada na prateleira, toque de 64 dp e narração.
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            for (p in PrateleiraDaGeladeira.entries) {
                val escolhida = p == aba
                val quantas = PrateleiraDaGeladeira.filtrar(comidas, p).size
                Box(
                    Modifier
                        .weight(1f)
                        .height(if (escolhida) 68.dp else 64.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(if (escolhida) CorDaPrateleira else Color(0xFFEEF1F6))
                        .selectable(
                            selected = escolhida,
                            role = Role.Tab,
                            onClick = {
                                aba = p
                                onAba(p, quantas == 0)
                            },
                        )
                        .semantics { contentDescription = "${p.nome}: $quantas" },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        p.icone,
                        fontSize = if (escolhida) 34.sp else 28.sp,
                        modifier = Modifier.graphicsLayer { alpha = if (escolhida) 1f else 0.65f },
                    )
                    if (quantas > 0) {
                        Text(
                            "$quantas",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 5.dp, end = 6.dp)
                                .size(20.dp)
                                .background(Cores.Certo, CircleShape),
                        )
                    }
                }
            }
        }
    }) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    CorDaPrateleira,
                    RoundedCornerShape(topStart = if (aba.ordinal == 0) 0.dp else 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
                )
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (daAba.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🫙", fontSize = 48.sp)
                    Spacer(Modifier.weight(1f))
                    BotaoFofo(onClick = onLoja, cor = Cores.Certo, descricao = "Comprar comidas", modifier = Modifier.pulsando()) {
                        Text("🛒 ${aba.icone}", style = MaterialTheme.typography.headlineMedium, color = Color.White, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
                    }
                }
            } else {
                for (linha in daAba.chunked(4)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (c in linha) {
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .semantics { contentDescription = c.nome }
                                    .clickable { onComer(c) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(c.icone, fontSize = 36.sp)
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

// ---- Guarda-roupa (roupas, chapéus e acessórios): painel ----
/**
 * Guarda-roupa aberto: a criança grande (com o que está experimentando) e o provador com uma aba por gaveta.
 * O que ainda não é seu aparece com o preço: tocar experimenta e tocar de novo compra.
 */
@Composable
private fun PainelGuardaRoupa(
    estado: EstadoJogo,
    perfilNaCena: Perfil,
    onVestir: (Produto) -> Unit,
    onTirar: (br.com.casinhadosaber.dominio.Gaveta) -> Unit,
    onFechar: () -> Unit,
) {
    Painel("Guarda-roupa aberto", onFechar, cabecalho = {
        Box(
            Modifier
                .size(width = 96.dp, height = 112.dp)
                .background(Color(0xFFFFF0F6), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Crianca(perfilNaCena, Modifier.height(104.dp), acenando = true)
        }
        Spacer(Modifier.weight(1f))
    }) {
        Spacer(Modifier.height(8.dp))
        br.com.casinhadosaber.ui.componentes.Provador(
            perfil = perfilNaCena,
            possui = { it.id in estado.comprados },
            onVestir = onVestir,
            onTirar = onTirar,
            precoDe = { Loja.preco(it, estado) },
        )
    }
}
// ---- Guarda-roupa: fim do painel ----

/**
 * Lista que rola para o lado e mostra que tem mais: a borda some num degradê (onde há mais cartões)
 * e, ao abrir, a lista dá um balancinho para o lado e volta, convidando a arrastar.
 */
@Composable
private fun ListaComDica(contentPadding: PaddingValues, espaco: Dp, conteudo: LazyListScope.() -> Unit) {
    val lista = rememberLazyListState()
    val balanco = with(LocalDensity.current) { 56.dp.toPx() }
    LaunchedEffect(lista) {
        delay(700)
        if (lista.canScrollForward) {
            lista.animateScrollBy(balanco, tween(durationMillis = 450))
            delay(150)
            lista.animateScrollBy(-balanco, tween(durationMillis = 550))
        }
    }
    LazyRow(
        state = lista,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(espaco),
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val borda = min(40.dp.toPx(), size.width / 4f)
                if (lista.canScrollForward) {
                    drawRect(
                        Brush.horizontalGradient(listOf(Color.Black, Color.Transparent), startX = size.width - borda, endX = size.width),
                        topLeft = Offset(size.width - borda, 0f),
                        size = Size(borda, size.height),
                        blendMode = BlendMode.DstIn,
                    )
                }
                if (lista.canScrollBackward) {
                    drawRect(
                        Brush.horizontalGradient(listOf(Color.Transparent, Color.Black), startX = 0f, endX = borda),
                        size = Size(borda, size.height),
                        blendMode = BlendMode.DstIn,
                    )
                }
            },
        content = conteudo,
    )
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
        ListaComDica(contentPadding = PaddingValues(horizontal = 14.dp), espaco = 12.dp) {
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
                        if (produto.categoria == Categoria.ROUPA || produto.categoria == Categoria.ACESSORIO || produto.categoria == Categoria.CHAPEU) {
                            // A criança vestindo a peça (camadas 3D, miniatura parada), com o ícone pequeno no canto.
                            Box(Modifier.height(52.dp)) {
                                Crianca(
                                    br.com.casinhadosaber.ui.componentes.miniaturaVestindo(estado.perfil ?: Perfil(), produto),
                                    Modifier.height(52.dp),
                                    animado = false,
                                )
                                Text(produto.icone, fontSize = 14.sp, maxLines = 1, modifier = Modifier.align(Alignment.TopEnd).offset(x = 22.dp))
                            }
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
