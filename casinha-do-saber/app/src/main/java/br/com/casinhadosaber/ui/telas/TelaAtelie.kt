package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Atelie
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Pintura
import br.com.casinhadosaber.dominio.alternarNaParede
import br.com.casinhadosaber.dominio.apagarDesenho
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.dominio.salvarDesenho
import br.com.casinhadosaber.ui.EscalaDeTablet
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.ContasLeves
import br.com.casinhadosaber.ui.componentes.CorDaConta
import br.com.casinhadosaber.ui.componentes.DesenhoDaPintura
import br.com.casinhadosaber.ui.componentes.corDaPaleta
import br.com.casinhadosaber.ui.componentes.coresEmIngles
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.componentes.IconeDeArteDaBarra
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.componentes.ceuDeBrinquedo
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius

/** Ferramentas do ateliê: lápis (toque e arraste), balde (pinta a mancha toda) e borracha. */
enum class Ferramenta(val emoji: String, val nome: String) { LAPIS("✏️", "Lápis"), BALDE("🪣", "Balde de tinta"), BORRACHA("🧽", "Borracha") }

/** A arte 3D de cada ferramenta (`tela_lapis`, `tela_balde`, `tela_borracha`; Fase 8A). */
private val Ferramenta.idDaArte: String
    get() = when (this) {
        Ferramenta.LAPIS -> "tela_lapis"
        Ferramenta.BALDE -> "tela_balde"
        Ferramenta.BORRACHA -> "tela_borracha"
    }

/**
 * Ateliê (modo criativo): um quadro de 12 por 12 quadradinhos para pintar com o dedo, sem certo nem errado,
 * sem estrelas e sem relógio. Cada traço é guardado sozinho (nada se perde ao sair); cabem
 * [Atelie.MAXIMO_DE_DESENHOS] desenhos, e até [Atelie.MAXIMO_NA_PAREDE] ficam pendurados na sala.
 */
@Composable
fun TelaAtelie(
    estado: EstadoJogo,
    onAtualizar: ((EstadoJogo) -> EstadoJogo) -> Unit,
    onVoltar: () -> Unit,
    /** Para as capturas: abre já com este desenho no quadro. */
    abrirDesenho: String? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val desenhos = estado.desenhos
    var pintura by remember {
        mutableStateOf(
            desenhos.firstOrNull { it.id == abrirDesenho }
                ?: if (desenhos.size < Atelie.MAXIMO_DE_DESENHOS) Pintura.vazio(Atelie.novoId(desenhos)) else desenhos.last(),
        )
    }
    var cor by remember { mutableIntStateOf(2) }
    var ferramenta by remember { mutableStateOf(Ferramenta.LAPIS) }
    var desfazer by remember { mutableStateOf(listOf<Pintura>()) }
    /** Desenho com o 🗑️ tocado uma vez: o segundo toque apaga. */
    var apagando by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    val estadoAtual by rememberUpdatedState(estado)

    LaunchedEffect(Unit) {
        narrador.falar(listOf(pt("Ateliê! Escolha uma cor e pinte com o dedo. Pintar em inglês:"), en("paint")))
    }

    /** Os desenhos guardados depois de guardar [p] (papel em branco que nunca foi guardado não entra). */
    fun guardadosCom(p: Pintura): List<Pintura> {
        val e = estadoAtual
        if (p.vazio && e.desenhos.none { it.id == p.id }) return e.desenhos
        return (e.salvarDesenho(p) ?: e).desenhos
    }

    /** Guarda o desenho do quadro (só se já tem tinta ou se já estava guardado). */
    fun guardar(p: Pintura) {
        if (p.vazio && estadoAtual.desenhos.none { it.id == p.id }) return
        onAtualizar { it.salvarDesenho(p) ?: it }
    }

    fun mudar(nova: Pintura) {
        if (nova == pintura) return
        desfazer = (desfazer + pintura).takeLast(30)
        pintura = nova
    }

    fun novo() {
        val guardados = guardadosCom(pintura)
        guardar(pintura)
        if (guardados.size >= Atelie.MAXIMO_DE_DESENHOS) {
            sons.erro()
            val texto = "A pasta está cheia! Apague um desenho para começar outro."
            aviso = texto
            narrador.falar(texto)
            return
        }
        sons.colocar()
        pintura = Pintura.vazio(Atelie.novoId(guardados))
        desfazer = emptyList()
        aviso = null
        narrador.falar("Papel novo! Vamos pintar?")
    }

    EscalaDeTablet {
        Column(
            Modifier
                .fillMaxSize()
                .ceuDeBrinquedo(comGrama = false)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                BotaoRedondo("⬅️", "Voltar para a casa", { guardar(pintura); onVoltar() }, tamanho = 52.dp)
                Spacer(Modifier.width(6.dp))
                // A paleta 3D no lugar do 🎨 e o título em branco com contorno, que diminui se não couber.
                ImagemDeArte("tela_paleta", Modifier.size(44.dp)) { Text("🎨", style = MaterialTheme.typography.headlineMedium) }
                Spacer(Modifier.width(2.dp))
                TituloQueCabe("Ateliê", Modifier.weight(1f), estilo = MaterialTheme.typography.headlineMedium)
                BotaoRedondo(
                    "↩️", "Desfazer",
                    {
                        val anterior = desfazer.lastOrNull()
                        if (anterior != null) {
                            sons.toque()
                            desfazer = desfazer.dropLast(1)
                            pintura = anterior
                            guardar(anterior)
                        }
                    },
                    cor = if (desfazer.isEmpty()) Color(0xFFEDEDED) else Color.White,
                    tamanho = 52.dp,
                )
                Spacer(Modifier.width(8.dp))
                BotaoRedondo("📄", "Papel novo", { novo() }, tamanho = 52.dp)
            }

            // O quadro: quadrado, do maior tamanho que couber.
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                val lado = if (maxWidth < maxHeight) maxWidth else maxHeight
                QuadroDePintar(
                    pintura = pintura,
                    // Lidos na hora do toque (e não do último desenho da tela): um traço logo depois de outro,
                    // ou logo depois de trocar a cor, já parte do quadro e da cor certos.
                    lerPintura = { pintura },
                    lerCor = { if (ferramenta == Ferramenta.BORRACHA) 0 else cor },
                    lerFerramenta = { ferramenta },
                    onComeco = { desfazer = (desfazer + pintura).takeLast(30) },
                    onMudar = { pintura = it },
                    onFim = { guardar(it) },
                    modifier = Modifier.size(lado),
                )
            }

            // Ferramentas
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                for (f in Ferramenta.entries) {
                    BotaoFofo(
                        onClick = {
                            sons.toque()
                            ferramenta = f
                            narrador.falar(
                                when (f) {
                                    Ferramenta.LAPIS -> listOf(pt("Lápis! Toque e arraste o dedo. Em inglês:"), en("pencil"))
                                    Ferramenta.BALDE -> listOf(pt("Balde de tinta! Toque para pintar um pedaço inteiro. Em inglês:"), en("bucket"))
                                    Ferramenta.BORRACHA -> listOf(pt("Borracha! Apaga a tinta. Em inglês:"), en("eraser"))
                                },
                            )
                        },
                        cor = if (ferramenta == f) Cores.Estrela else Color.White,
                        corBase = Color(0x33000000),
                        descricao = f.nome + if (ferramenta == f) ", escolhido" else "",
                        modifier = Modifier.testTag("ferramenta_${f.name.lowercase()}"),
                        profundidade = 4.dp,
                    ) {
                        Box(Modifier.size(width = 72.dp, height = 56.dp), contentAlignment = Alignment.Center) {
                            ImagemDeArte(f.idDaArte, Modifier.size(48.dp)) { Text(f.emoji, fontSize = 28.sp) }
                        }
                    }
                }
                BotaoFofo(
                    onClick = {
                        if (!pintura.vazio) {
                            sons.pousar()
                            mudar(pintura.limpar())
                            guardar(pintura)
                            narrador.falar("Quadro limpinho! Se quiser, toque em desfazer.")
                        }
                    },
                    cor = Color.White,
                    corBase = Color(0x33000000),
                    descricao = "Limpar o quadro",
                    profundidade = 4.dp,
                ) {
                    Box(Modifier.size(width = 56.dp, height = 56.dp), contentAlignment = Alignment.Center) {
                        IconeLeve("icone_vassoura", 42.dp) { Text("🧹", fontSize = 28.sp) }
                    }
                }
            }

            // Paleta (o branco é a borracha: fica de fora)
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (linha in (1 until Atelie.paleta.size).chunked(6)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
                        for (i in linha) {
                            val escolhida = cor == i && ferramenta != Ferramenta.BORRACHA
                            Box(
                                Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(corDaPaleta(i))
                                    // Gotinha de tinta de plástico: brilho no alto à esquerda e sombra embaixo.
                                    .drawBehind {
                                        drawCircle(Color(0x26000000), size.minDimension * 0.5f, center.copy(y = center.y + size.height * 0.12f))
                                        drawOval(
                                            Color.White.copy(alpha = 0.45f),
                                            Offset(size.width * 0.22f, size.height * 0.12f),
                                            Size(size.width * 0.32f, size.height * 0.2f),
                                        )
                                    }
                                    .border(if (escolhida) 5.dp else 2.dp, if (escolhida) Color.White else Color(0x33000000), CircleShape)
                                    .semantics { contentDescription = "Cor ${Atelie.paleta[i].second}" }
                                    .testTag("cor_$i")
                                    .clickable {
                                        sons.toque()
                                        cor = i
                                        if (ferramenta == Ferramenta.BORRACHA) ferramenta = Ferramenta.LAPIS
                                        narrador.falar(listOf(pt("${Atelie.paleta[i].second.replaceFirstChar { it.uppercase() }}! Em inglês:"), en(coresEmIngles[i])))
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (escolhida) {
                                    // O certinho 3D: branco sobre as cores fortes, roxo sobre as clarinhas (amarelo e bege).
                                    val clara = i == 4 || i == 11
                                    IconeLeve(if (clara) "icone_certo" else "icone_certo_branco", 30.dp) {
                                        Text("✔", color = if (clara) Cores.Texto else Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Galeria(
                estado = estado,
                aberta = pintura.id,
                apagando = apagando,
                aviso = aviso,
                onAbrir = { d ->
                    guardar(pintura)
                    sons.toque()
                    pintura = d
                    desfazer = emptyList()
                    apagando = null
                    aviso = null
                },
                onParede = { d ->
                    guardar(pintura)
                    val ia = d.id !in estado.quadrosNaParede
                    if (ia && estado.desenhos.none { it.id == d.id }) return@Galeria
                    sons.colocar()
                    onAtualizar { it.alternarNaParede(d.id) }
                    narrador.falar(
                        if (ia) "Pendurado na parede da sala! Cabem ${Atelie.MAXIMO_NA_PAREDE} quadros." else "Tirei da parede. Ele continua guardado aqui.",
                    )
                },
                onApagar = { d ->
                    if (apagando != d.id) {
                        sons.toque()
                        apagando = d.id
                        narrador.falar("Quer mesmo apagar este desenho? Toque de novo na lixeira.")
                    } else {
                        sons.pousar()
                        apagando = null
                        aviso = null
                        onAtualizar { it.apagarDesenho(d.id) }
                        if (d.id == pintura.id) {
                            pintura = Pintura.vazio(Atelie.novoId(estadoAtual.desenhos.filter { it.id != d.id }))
                            desfazer = emptyList()
                        }
                        narrador.falar("Desenho apagado.")
                    }
                },
            )
        }
    }
}

/** O quadro de 12 por 12: tocar pinta um quadradinho, arrastar faz um traço, o balde enche a mancha. */
@Composable
private fun QuadroDePintar(
    pintura: Pintura,
    lerPintura: () -> Pintura,
    lerCor: () -> Int,
    lerFerramenta: () -> Ferramenta,
    onComeco: () -> Unit,
    onMudar: (Pintura) -> Unit,
    onFim: (Pintura) -> Unit,
    modifier: Modifier,
) {
    val leitura by rememberUpdatedState(Triple(lerPintura, lerCor, lerFerramenta))
    val comeco by rememberUpdatedState(onComeco)
    val mudar by rememberUpdatedState(onMudar)
    val fim by rememberUpdatedState(onFim)
    Box(
        modifier
            // Moldura de madeira com um lábio escuro e sombra macia desenhados (sem a sombra borrada de elevação,
            // que num quadro deste tamanho pesava), um friso claro por dentro e o papel encaixado.
            .drawBehind {
                val r = CornerRadius(12.dp.toPx())
                drawRoundRect(Cores.Sombra, Offset(0f, 9.dp.toPx()), size, r)
                drawRoundRect(Color(0xFF7A4B2A), Offset(0f, 5.dp.toPx()), size, r)
            }
            .background(Brush.linearGradient(listOf(Color(0xFFE0A774), Color(0xFFC98B5B), Color(0xFFA86F43))), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF8A5A36), RoundedCornerShape(12.dp))
            .padding(6.dp)
            .border(2.dp, Color(0xFFF2CFA4), RoundedCornerShape(4.dp))
            .padding(2.dp)
            .testTag("quadro_de_pintar")
            .semantics { contentDescription = "Quadro de pintar. Toque e arraste o dedo para pintar." },
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    fun celula(o: Offset): Pair<Int, Int> {
                        val c = size.width.toFloat() / Pintura.LADO
                        val l = (o.y / c).toInt().coerceIn(0, Pintura.LADO - 1)
                        val k = (o.x / c).toInt().coerceIn(0, Pintura.LADO - 1)
                        return l to k
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        comeco()
                        val (l0, k0) = celula(down.position)
                        // A pintura do gesto fica aqui: entre dois quadros da tela o dedo pode andar várias casinhas.
                        val (lerP, lerC, lerF) = leitura
                        val corAtual = lerC()
                        var p = lerP()
                        if (lerF() == Ferramenta.BALDE) {
                            p = p.encher(l0, k0, corAtual)
                            mudar(p)
                        } else {
                            p = p.pintar(l0, k0, corAtual)
                            mudar(p)
                            var ultima = l0 to k0
                            while (true) {
                                val evento = awaitPointerEvent()
                                val ch = evento.changes.firstOrNull { it.id == down.id } ?: break
                                if (!ch.pressed) break
                                ch.consume()
                                val nova = celula(ch.position)
                                if (nova != ultima) {
                                    p = p.tracar(ultima.first, ultima.second, nova.first, nova.second, corAtual)
                                    mudar(p)
                                    ultima = nova
                                }
                            }
                        }
                        fim(p)
                    }
                },
        ) {
            val lado = Pintura.LADO
            val c = size.width / lado
            drawRect(Color(0xFFFFFDF8))
            for (l in 0 until lado) for (k in 0 until lado) {
                val i = pintura.cor(l, k)
                if (i != 0) drawRect(corDaPaleta(i), Offset(k * c, l * c), Size(c + 0.5f, c + 0.5f))
            }
            desenharTexturaDePapel()
            val linha = Color(0x1F000000)
            for (i in 0..lado) {
                drawLine(linha, Offset(i * c, 0f), Offset(i * c, size.height), 1.5f)
                drawLine(linha, Offset(0f, i * c), Offset(size.width, i * c), 1.5f)
            }
            // Sombrinha da moldura caindo no papel (em cima e à esquerda).
            drawRect(Brush.verticalGradient(listOf(Color(0x2E000000), Color.Transparent), endY = c * 0.5f), size = Size(size.width, c * 0.5f))
            drawRect(Brush.horizontalGradient(listOf(Color(0x24000000), Color.Transparent), endX = c * 0.4f), size = Size(c * 0.4f, size.height))
            drawRect(Color(0x33000000), style = Stroke(2f))
        }
    }
}

/** Lado da miniatura de cada desenho guardado (do tamanho da dupla de botões embaixo dela). */
private val LADO_DA_MINIATURA = 92.dp

/** Altura da fileira de desenhos guardados (miniatura de 92 dp mais os botões de 44 dp). */
private val ALTURA_DA_GALERIA = 152.dp

/** Grãozinhos e fibras do papel, sempre nos mesmos lugares (sem sorteio a cada quadro). */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.desenharTexturaDePapel() {
    var semente = 7919L
    fun proximo(): Float {
        semente = (semente * 1103515245L + 12345L) and 0x7fffffffL
        return (semente % 10000L) / 10000f
    }
    val r = size.minDimension * 0.004f
    repeat(220) {
        val x = proximo() * size.width
        val y = proximo() * size.height
        val escuro = proximo() < 0.6f
        drawCircle(if (escuro) Color(0x0F5A3E36) else Color(0x33FFFFFF), r * (0.6f + proximo()), Offset(x, y))
    }
    repeat(40) {
        val x = proximo() * size.width
        val y = proximo() * size.height
        val dx = (proximo() - 0.5f) * size.width * 0.05f
        val dy = (proximo() - 0.5f) * size.width * 0.02f
        drawLine(Color(0x0D5A3E36), Offset(x, y), Offset(x + dx, y + dy), r * 0.7f)
    }
}

/** Os desenhos guardados: tocar abre; 🖼️ pendura na sala (ou tira); 🗑️ duas vezes apaga. */
@Composable
private fun Galeria(
    estado: EstadoJogo,
    aberta: String,
    apagando: String?,
    aviso: String?,
    onAbrir: (Pintura) -> Unit,
    onParede: (Pintura) -> Unit,
    onApagar: (Pintura) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
        if (aviso != null) {
            Text(
                aviso,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFB5244A),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            )
        } else {
            ContasDaGaleria(estado.desenhos.size, estado.quadrosNaParede.size)
        }
        Spacer(Modifier.height(4.dp))
        if (estado.desenhos.isEmpty()) {
            // Mesma altura da fileira de desenhos: quando o primeiro desenho é guardado, o quadro não pula.
            Box(Modifier.fillMaxWidth().height(ALTURA_DA_GALERIA), contentAlignment = Alignment.Center) {
                Row(
                    Modifier.padding(horizontal = 12.dp).cartaoFofo(raio = 20.dp, labio = Cores.LabioClaro).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ImagemDeArte("tela_pincel", Modifier.size(56.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Pinte alguma coisa: o desenho fica guardado aqui sozinho!",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            return@Column
        }
        LazyRow(
            Modifier.height(ALTURA_DA_GALERIA),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(estado.desenhos.asReversed(), key = { it.id }) { d ->
                val naParede = d.id in estado.quadrosNaParede
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(LADO_DA_MINIATURA)
                            .drawBehind { drawRoundRect(Cores.Sombra, Offset(0f, 3.dp.toPx()), size, CornerRadius(8.dp.toPx())) }
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(if (d.id == aberta) 4.dp else 3.dp, if (d.id == aberta) Cores.Roxo else Color(0xFFC98B5B), RoundedCornerShape(8.dp))
                            .padding(7.dp)
                            .clickable { onAbrir(d) }
                            .semantics { contentDescription = "Desenho guardado" + if (naParede) ", pendurado na sala" else "" },
                    ) {
                        DesenhoDaPintura(d, Modifier.fillMaxSize())
                        if (naParede) {
                            IconeLeve(ID_DO_QUADRO, 30.dp, Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-10).dp)) {
                                Text("🖼️", fontSize = 18.sp)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                        BotaoPequeno(
                            "🖼️", if (naParede) "Tirar da parede" else "Pendurar na sala",
                            if (naParede) Cores.Estrela else Color.White,
                            idDaArte = ID_DO_QUADRO,
                        ) { onParede(d) }
                        BotaoPequeno(
                            "🗑️", if (apagando == d.id) "Toque de novo para apagar" else "Apagar desenho",
                            if (apagando == d.id) Color(0xFFFF8A8A) else Color.White,
                        ) { onApagar(d) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotaoPequeno(emoji: String, descricao: String, cor: Color, idDaArte: String? = null, onClick: () -> Unit) {
    // 🗑️ vira a lixeira 3D da barra (IconesDaBarra); 🖼️ vira o quadro 3D da casa ([idDaArte]).
    val icone = remember(emoji) { IconesDaBarra.para(emoji) }
    BotaoFofo(onClick = onClick, cor = cor, corBase = escurecer(cor, 0.7f), descricao = descricao, forma = RoundedCornerShape(12.dp), profundidade = 3.dp) {
        Box(Modifier.size(width = 44.dp, height = 44.dp), contentAlignment = Alignment.Center) {
            if (idDaArte != null) {
                IconeLeve(idDaArte, 34.dp) { Text(emoji, fontSize = 20.sp) }
            } else if (icone == null) {
                Text(emoji, fontSize = 20.sp)
            } else {
                IconeDeArteDaBarra(icone, Modifier.size(28.dp)) { Text(emoji, fontSize = 20.sp) }
            }
        }
    }
}

/** O quadrinho 3D da casa (`item_quadro`): pendurar o desenho na sala (no lugar do 🖼️). */
private const val ID_DO_QUADRO = "item_quadro"

/**
 * Quantos desenhos estão guardados e quantos estão na sala, como contas que se contam (no lugar de "Meus desenhos:
 * 1 de 12" e "na sala: 0 de 3"): o pincel e uma conta por desenho guardado, o quadro e uma conta por quadro na sala.
 * Os números ficam só na descrição.
 */
@Composable
private fun ContasDaGaleria(desenhos: Int, naSala: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .clearAndSetSemantics {
                contentDescription = "Meus desenhos: $desenhos de ${Atelie.MAXIMO_DE_DESENHOS}. Na sala: $naSala de ${Atelie.MAXIMO_NA_PAREDE}"
            },
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconeLeve("tela_pincel", 26.dp) { Text("🖌️", fontSize = 16.sp) }
        ContasLeves(desenhos, Atelie.MAXIMO_DE_DESENHOS, tamanho = 11.dp, cor = CorDaConta.ROXA)
        Spacer(Modifier.width(10.dp))
        IconeLeve(ID_DO_QUADRO, 26.dp) { Text("🖼️", fontSize = 16.sp) }
        ContasLeves(naSala, Atelie.MAXIMO_NA_PAREDE, tamanho = 11.dp, cor = CorDaConta.AMARELA)
    }
}
