package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Colorir
import br.com.casinhadosaber.dominio.DesenhoDeColorir
import br.com.casinhadosaber.dominio.Desenhos
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Historico
import br.com.casinhadosaber.dominio.Obra
import br.com.casinhadosaber.dominio.PontoDoDesenho
import br.com.casinhadosaber.dominio.Tintas
import br.com.casinhadosaber.dominio.desenhosTerminados
import br.com.casinhadosaber.dominio.guardarObra
import br.com.casinhadosaber.dominio.obraDe
import br.com.casinhadosaber.dominio.recomecarObra
import br.com.casinhadosaber.dominio.terminarDesenho
import br.com.casinhadosaber.dominio.tintasLiberadas
import br.com.casinhadosaber.ui.EscalaDeTablet
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.DesenhoNoCanvas
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.componentes.ceuDeBrinquedo
import br.com.casinhadosaber.ui.componentes.corDaTinta
import br.com.casinhadosaber.ui.componentes.desenharObra
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.Raios
import kotlinx.coroutines.delay

/** Ferramentas do Livro de Colorir (o ateliê tem as dele, em [Ferramenta]). */
enum class FerramentaDeCor(val emoji: String, val nome: String) {
    BALDE("🪣", "Balde mágico"),
    PINCEL("🖌️", "Pincel"),
    BORRACHA("🧽", "Borracha"),
}

/** Grossuras do pincel (fração do lado do desenho): fino, médio e grosso. */
private val GrossurasDoPincel = listOf(0.018f, 0.04f, 0.07f)

/** Estrelas em texto: cheias e vazias, sem depender de arte. */
internal fun estrelasEmTexto(n: Int, de: Int = 3): String = "⭐".repeat(n.coerceIn(0, de)) + "☆".repeat((de - n).coerceIn(0, de))

/**
 * Livro de Colorir: a criança escolhe um desenho e pinta com o balde (toca numa região e ela enche, sem passar da
 * linha), com o pincel (o traço fica dentro da região onde o dedo começou, se a trava estiver ligada) ou apaga com a
 * borracha. Tudo é guardado a cada toque. Ao dizer "Pronto!" o desenho terminado paga moedinhas e estrelas, e abre
 * cores novas. Sem tempo, sem nota baixa, sem perder nada.
 */
@Composable
fun TelaColorir(
    estado: EstadoJogo,
    onAtualizar: ((EstadoJogo) -> EstadoJogo) -> Unit,
    onVoltar: () -> Unit,
    onMontarPuzzle: (String) -> Unit,
    /** Para as capturas: abre já neste desenho. */
    abrirDesenho: String? = null,
) {
    var aberto by remember { mutableStateOf(abrirDesenho?.takeIf { it in Desenhos.porId }) }
    val desenho = aberto?.let { Desenhos.porId[it] }
    if (desenho == null) {
        EscolherDesenho(estado, onAbrir = { aberto = it.id }, onVoltar = onVoltar)
    } else {
        PintarDesenho(
            d = desenho,
            estado = estado,
            onAtualizar = onAtualizar,
            onOutro = { aberto = null },
            onMontarPuzzle = onMontarPuzzle,
        )
    }
}

@Composable
private fun EscolherDesenho(estado: EstadoJogo, onAbrir: (DesenhoDeColorir) -> Unit, onVoltar: () -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    LaunchedEffect(Unit) { narrador.falar("Livro de colorir! Escolha um desenho para pintar.") }
    EscalaDeTablet {
        Column(
            Modifier
                .fillMaxSize()
                .ceuDeBrinquedo(comGrama = false)
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                BotaoRedondo("⬅️", "Voltar para a casa", onVoltar, tamanho = 52.dp)
                Spacer(Modifier.width(8.dp))
                TituloQueCabe("Livro de colorir", Modifier.weight(1f), estilo = MaterialTheme.typography.headlineMedium)
                Text(
                    "${estado.desenhosTerminados} de ${Desenhos.todos.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Cores.Texto,
                    modifier = Modifier.semantics { contentDescription = "Você terminou ${estado.desenhosTerminados} desenhos de ${Desenhos.todos.size}" },
                )
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for ((colecao, nome) in Desenhos.colecoes) {
                    val daColecao = Desenhos.daColecao(colecao)
                    val feitos = daColecao.count { it.id in estado.coloridos }
                    Text(
                        "$nome  $feitos de ${daColecao.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Cores.Texto,
                    )
                    for (linha in daColecao.chunked(2)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (d in linha) {
                                val obra = estado.obraDe(d)
                                val estrelas = estado.coloridos[d.id] ?: 0
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .cartaoFofo(raio = Raios.Grande)
                                        .clickable {
                                            sons.toque()
                                            narrador.falar(d.titulo)
                                            onAbrir(d)
                                        }
                                        .padding(8.dp)
                                        .testTag("desenho_${d.id}")
                                        .semantics {
                                            contentDescription = d.titulo + if (estrelas > 0) ", terminado com $estrelas estrelas" else ""
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    DesenhoNoCanvas(d, obra, Modifier.fillMaxWidth().aspectRatio(1f), soContorno = obra.vazia())
                                    Text("${d.emoji} ${d.titulo}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Cores.Texto, textAlign = TextAlign.Center)
                                    Text(estrelasEmTexto(estrelas), fontSize = 14.sp)
                                }
                            }
                            if (linha.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PintarDesenho(
    d: DesenhoDeColorir,
    estado: EstadoJogo,
    onAtualizar: ((EstadoJogo) -> EstadoJogo) -> Unit,
    onOutro: () -> Unit,
    onMontarPuzzle: (String) -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var hist by remember(d.id) { mutableStateOf(Historico.de(estado.obraDe(d))) }
    var previa by remember(d.id) { mutableStateOf<Obra?>(null) }
    var tinta by remember { mutableIntStateOf(1) }
    var ferramenta by remember { mutableStateOf(FerramentaDeCor.BALDE) }
    var grossura by remember { mutableIntStateOf(1) }
    var dentroDaLinha by remember { mutableStateOf(true) }
    var realce by remember(d.id) { mutableIntStateOf(-1) }
    var premio by remember(d.id) { mutableStateOf<Colorir.Premio?>(null) }
    val estadoAtual by rememberUpdatedState(estado)
    val atualizar by rememberUpdatedState(onAtualizar)

    LaunchedEffect(d.id) { narrador.falar("${d.titulo}! Escolha uma cor e toque no desenho.") }
    LaunchedEffect(realce) {
        if (realce >= 0) {
            delay(2200)
            realce = -1
        }
    }

    fun aplicar(nova: Obra) {
        if (nova == hist.atual) return
        hist = hist.fazer(nova)
        atualizar { it.guardarObra(nova) }
    }

    val liberadas = estado.tintasLiberadas
    if (tinta !in liberadas.map { it.id }) tinta = 1
    val terminados = estado.desenhosTerminados

    EscalaDeTablet {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .ceuDeBrinquedo(comGrama = false)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BotaoRedondo("⬅️", "Escolher outro desenho", { sons.toque(); onOutro() }, tamanho = 52.dp)
                    Spacer(Modifier.width(6.dp))
                    TituloQueCabe(d.titulo, Modifier.weight(1f), estilo = MaterialTheme.typography.headlineSmall)
                    BotaoRedondo(
                        "↩️", "Desfazer",
                        {
                            if (hist.podeDesfazer) {
                                sons.toque()
                                hist = hist.desfazer()
                                val o = hist.atual
                                atualizar { it.guardarObra(o) }
                            }
                        },
                        cor = if (hist.podeDesfazer) Color.White else Color(0xFFEDEDED),
                        tamanho = 48.dp,
                        modifier = Modifier.testTag("colorir_desfazer"),
                    )
                    Spacer(Modifier.width(6.dp))
                    BotaoRedondo(
                        "↪️", "Refazer",
                        {
                            if (hist.podeRefazer) {
                                sons.toque()
                                hist = hist.refazer()
                                val o = hist.atual
                                atualizar { it.guardarObra(o) }
                            }
                        },
                        cor = if (hist.podeRefazer) Color.White else Color(0xFFEDEDED),
                        tamanho = 48.dp,
                        modifier = Modifier.testTag("colorir_refazer"),
                    )
                }

                // O desenho: quadrado, do maior tamanho que couber.
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                    val lado = if (maxWidth < maxHeight) maxWidth else maxHeight
                    Canvas(
                        Modifier
                            .size(lado)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .testTag("quadro_de_colorir")
                            .semantics { contentDescription = "${d.titulo}. Toque para pintar." }
                            .pointerInput(d.id) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    down.consume()
                                    val ladoPx = size.width.toFloat()
                                    fun ponto(o: Offset) = PontoDoDesenho((o.x / ladoPx).coerceIn(0f, 1f), (o.y / ladoPx).coerceIn(0f, 1f))
                                    val base = hist.atual
                                    when (ferramenta) {
                                        FerramentaDeCor.BALDE -> {
                                            val p = ponto(down.position)
                                            val nova = Colorir.balde(d, base, p.x, p.y, tinta)
                                            if (nova != base) sons.colocar()
                                            aplicar(nova)
                                        }
                                        FerramentaDeCor.BORRACHA -> {
                                            var o = base
                                            val p0 = ponto(down.position)
                                            o = Colorir.borracha(d, o, p0.x, p0.y)
                                            previa = o
                                            while (true) {
                                                val ev = awaitPointerEvent()
                                                val ch = ev.changes.firstOrNull { it.id == down.id } ?: break
                                                if (!ch.pressed) break
                                                ch.consume()
                                                val p = ponto(ch.position)
                                                o = Colorir.borracha(d, o, p.x, p.y)
                                                previa = o
                                            }
                                            previa = null
                                            aplicar(o)
                                        }
                                        FerramentaDeCor.PINCEL -> {
                                            val pontos = mutableListOf(ponto(down.position))
                                            previa = Colorir.pincel(d, base, pontos.toList(), tinta, GrossurasDoPincel[grossura], dentroDaLinha)
                                            while (true) {
                                                val ev = awaitPointerEvent()
                                                val ch = ev.changes.firstOrNull { it.id == down.id } ?: break
                                                if (!ch.pressed) break
                                                ch.consume()
                                                pontos += ponto(ch.position)
                                                previa = Colorir.pincel(d, base, pontos.toList(), tinta, GrossurasDoPincel[grossura], dentroDaLinha)
                                            }
                                            val acabado = Colorir.pincel(d, base, pontos.toList(), tinta, GrossurasDoPincel[grossura], dentroDaLinha)
                                            previa = null
                                            aplicar(acabado)
                                        }
                                    }
                                }
                            },
                    ) {
                        desenharObra(d, previa ?: hist.atual, size.minDimension, realce = realce)
                    }
                }

                // Ferramentas
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (f in FerramentaDeCor.entries) {
                        BotaoFofo(
                            onClick = {
                                sons.toque()
                                ferramenta = f
                                narrador.falar(
                                    when (f) {
                                        FerramentaDeCor.BALDE -> "Balde mágico! Toque num pedaço do desenho."
                                        FerramentaDeCor.PINCEL -> "Pincel! Arraste o dedo."
                                        FerramentaDeCor.BORRACHA -> "Borracha! Apaga a tinta do pedaço."
                                    },
                                )
                            },
                            cor = if (ferramenta == f) Cores.Estrela else Color.White,
                            corBase = Color(0x33000000),
                            descricao = f.nome + if (ferramenta == f) ", escolhido" else "",
                            modifier = Modifier.testTag("colorir_${f.name.lowercase()}"),
                            profundidade = 4.dp,
                        ) {
                            Box(Modifier.size(width = 56.dp, height = 48.dp), contentAlignment = Alignment.Center) { Text(f.emoji, fontSize = 26.sp) }
                        }
                    }
                    if (ferramenta == FerramentaDeCor.PINCEL) {
                        for (g in GrossurasDoPincel.indices) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (grossura == g) Cores.Estrela else Color.White)
                                    .border(2.dp, Color(0x33000000), CircleShape)
                                    .clickable { sons.toque(); grossura = g }
                                    .semantics { contentDescription = "Pincel ${listOf("fino", "médio", "grosso")[g]}" }
                                    .testTag("colorir_grossura_$g"),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(Modifier.size((8 + g * 8).dp).clip(CircleShape).background(Cores.Texto))
                            }
                        }
                        BotaoFofo(
                            onClick = {
                                sons.toque()
                                dentroDaLinha = !dentroDaLinha
                                narrador.falar(if (dentroDaLinha) "Dentro da linha! A tinta não passa do contorno." else "Pincel livre! Pinte por cima de tudo.")
                            },
                            cor = if (dentroDaLinha) Cores.Certo else Color.White,
                            corBase = Color(0x33000000),
                            descricao = if (dentroDaLinha) "Dentro da linha, ligado" else "Dentro da linha, desligado",
                            modifier = Modifier.testTag("colorir_dentro_da_linha"),
                            profundidade = 4.dp,
                        ) {
                            Box(Modifier.size(width = 48.dp, height = 40.dp), contentAlignment = Alignment.Center) {
                                Text(if (dentroDaLinha) "🔒" else "🔓", fontSize = 22.sp)
                            }
                        }
                    }
                    BotaoFofo(
                        onClick = {
                            sons.toque()
                            val r = Colorir.proximaRegiao(hist.atual)
                            if (r >= 0) {
                                realce = r
                                narrador.falar("Que tal pintar este pedacinho?")
                            } else {
                                narrador.falar("Tudo pintado! Toque em Pronto.")
                            }
                        },
                        cor = Color.White,
                        corBase = Color(0x33000000),
                        descricao = "Dica: mostra o próximo pedaço para pintar",
                        modifier = Modifier.testTag("colorir_dica"),
                        profundidade = 4.dp,
                    ) {
                        Box(Modifier.size(width = 48.dp, height = 40.dp), contentAlignment = Alignment.Center) { Text("💡", fontSize = 22.sp) }
                    }
                }

                // Estojo: as cores que já abriram e as que ainda vão abrir.
                Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (linha in Tintas.todas.drop(1).chunked(10)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)) {
                            for (t in linha) {
                                val aberta = terminados >= t.liberadaCom
                                val escolhida = tinta == t.id && ferramenta != FerramentaDeCor.BORRACHA
                                Box(
                                    Modifier
                                        .size(if (escolhida) 38.dp else 34.dp)
                                        .clip(CircleShape)
                                        .background(if (aberta) corDaTinta(t) else Color(0xFFDDDDE6))
                                        .border(if (escolhida) 4.dp else 2.dp, if (escolhida) Color.White else Color(0x33000000), CircleShape)
                                        .semantics {
                                            contentDescription = if (aberta) "Cor ${t.nome}" else "Cor ${t.nome}, ainda fechada. Termine mais ${t.liberadaCom - terminados} desenhos"
                                        }
                                        .testTag("tinta_${t.id}")
                                        .clickable {
                                            if (aberta) {
                                                sons.toque()
                                                tinta = t.id
                                                if (ferramenta == FerramentaDeCor.BORRACHA) ferramenta = FerramentaDeCor.BALDE
                                                narrador.falar(t.nome.replaceFirstChar { it.uppercase() })
                                            } else {
                                                sons.erro()
                                                val faltam = t.liberadaCom - terminados
                                                narrador.falar(
                                                    if (faltam == 1) "Esta cor abre quando você terminar mais um desenho!"
                                                    else "Esta cor abre quando você terminar mais $faltam desenhos!",
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (!aberta) Text("🔒", fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    BotaoFofo(
                        onClick = {
                            sons.toque()
                            hist = Historico.de(Colorir.obraVazia(d))
                            atualizar { it.recomecarObra(d) }
                            narrador.falar("Papel novo!")
                        },
                        cor = Color.White,
                        corBase = Color(0x33000000),
                        descricao = "Recomeçar o desenho",
                        modifier = Modifier.testTag("colorir_recomecar"),
                        profundidade = 4.dp,
                    ) { Box(Modifier.size(width = 56.dp, height = 48.dp), contentAlignment = Alignment.Center) { Text("🧹", fontSize = 26.sp) } }
                    BotaoFofo(
                        onClick = {
                            val atual = hist.atual
                            val (_, p) = estadoAtual.terminarDesenho(d, atual)
                            if (p == null) {
                                sons.erro()
                                realce = Colorir.proximaRegiao(atual)
                                narrador.falar("Faltam uns pedacinhos! Olhe onde está piscando.")
                            } else {
                                atualizar { it.terminarDesenho(d, atual).first }
                                sons.premio()
                                premio = p
                                narrador.falar(
                                    "Que lindo! " + when (p.estrelas) {
                                        3 -> "Três estrelas!"
                                        2 -> "Duas estrelas!"
                                        else -> "Uma estrela!"
                                    },
                                )
                            }
                        },
                        cor = Cores.Certo,
                        modifier = Modifier.weight(1f).testTag("colorir_pronto"),
                        descricao = "Pronto, terminei o desenho",
                    ) {
                        Text("✔ Pronto!", color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }

            val p = premio
            if (p != null) {
                PremioDoColorir(
                    d = d,
                    obra = hist.atual,
                    premio = p,
                    onContinuar = { premio = null },
                    onOutro = { premio = null; onOutro() },
                    onMontar = { onMontarPuzzle("obra:${d.id}") },
                )
            }
        }
    }
}

@Composable
private fun PremioDoColorir(
    d: DesenhoDeColorir,
    obra: Obra,
    premio: Colorir.Premio,
    onContinuar: () -> Unit,
    onOutro: () -> Unit,
    onMontar: () -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(Color(0x99000000)).pointerInput(Unit) { detectTapGestures { } }.windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(20.dp).cartaoFofo(raio = Raios.Enorme).padding(16.dp).testTag("premio_colorir"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Que lindo!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Cores.Texto)
            DesenhoNoCanvas(d, obra, Modifier.fillMaxWidth(0.7f).aspectRatio(1f))
            Text(estrelasEmTexto(premio.estrelas), fontSize = 30.sp, modifier = Modifier.semantics { contentDescription = "${premio.estrelas} estrelas" })
            if (premio.moedas > 0) {
                Text("+${premio.moedas} moedinhas 🪙", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Cores.Texto)
            }
            val nova = premio.tintaNova
            if (nova != null) {
                Text("Cor nova: ${nova.nome}!", style = MaterialTheme.typography.titleMedium, color = Cores.Texto, fontWeight = FontWeight.Bold)
            }
            if (premio.colecaoCompleta) {
                Text("Você terminou a coleção inteira! 🏆", style = MaterialTheme.typography.titleMedium, color = Cores.Texto, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            BotaoFofo(onClick = onMontar, cor = Cores.Roxo, modifier = Modifier.fillMaxWidth().testTag("premio_montar"), descricao = "Montar um quebra cabeça com a minha pintura") {
                Text("🧩 Montar quebra cabeça", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotaoFofo(onClick = onOutro, cor = Color.White, modifier = Modifier.weight(1f).testTag("premio_outro"), descricao = "Escolher outro desenho") {
                    Text("Outro desenho", color = Cores.Texto, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 4.dp))
                }
                BotaoFofo(onClick = onContinuar, cor = Color.White, modifier = Modifier.weight(1f).testTag("premio_continuar"), descricao = "Continuar pintando") {
                    Text("Continuar", color = Cores.Texto, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
