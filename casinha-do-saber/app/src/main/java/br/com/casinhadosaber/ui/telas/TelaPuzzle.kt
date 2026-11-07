package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Colorir
import br.com.casinhadosaber.dominio.DesenhoDeColorir
import br.com.casinhadosaber.dominio.Desenhos
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Obra
import br.com.casinhadosaber.dominio.Puzzle
import br.com.casinhadosaber.dominio.nivelDePuzzleAberto
import br.com.casinhadosaber.dominio.pecasDouradas
import br.com.casinhadosaber.dominio.venceuPuzzle
import br.com.casinhadosaber.ui.EscalaDeTablet
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.DesenhoNoCanvas
import br.com.casinhadosaber.ui.componentes.PecaDeQuebraCabeca
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.componentes.ceuDeBrinquedo
import br.com.casinhadosaber.ui.componentes.desenharObra
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.Raios
import kotlin.random.Random

private enum class TipoDeJogo { MONTAR, DESLIZAR }

/** O que a criança escolheu jogar. [fonte] = "modelo:id" (a versão bonita) ou "obra:id" (a pintura dela). */
private data class Partida(val fonte: String, val nivel: Int, val tipo: TipoDeJogo, val semente: Long)

/** Desenho e obra de uma fonte. A pintura da criança só vale se ela terminou o desenho; senão usa o modelo. */
private fun resolverFonte(estado: EstadoJogo, fonte: String): Pair<DesenhoDeColorir, Obra>? {
    val tipo = fonte.substringBefore(':')
    val d = Desenhos.porId[fonte.substringAfter(':')] ?: return null
    val obra = estado.obrasDeColorir[d.id]
    return if (tipo == "obra" && obra != null && d.id in estado.coloridos) d to obra else d to Colorir.obraModelo(d)
}

/** Lado do quebra cabeça de deslizar para cada nível: de 2 por 2 a 4 por 4. */
private fun ladoDoDeslizante(nivel: Int): Int = (2 + (nivel - 1) / 3).coerceIn(2, 4)

/**
 * Quebra cabeças: monta a imagem arrastando as peças para a moldura (o ímã encaixa quando chega perto) ou desliza as
 * peças para o espaço vazio. Oito níveis por coleção, do mais fácil ao mais difícil, sem tempo e sem perder nada.
 * A imagem é um desenho do Livro de Colorir, na versão bonita ou na pintura da própria criança.
 */
@Composable
fun TelaPuzzle(
    estado: EstadoJogo,
    onAtualizar: ((EstadoJogo) -> EstadoJogo) -> Unit,
    onVoltar: () -> Unit,
    /** Para o prêmio do Livro de Colorir: abre já com a pintura da criança nesta fonte. */
    fonteInicial: String? = null,
    /** Para os testes: a mesma bagunça de peças toda vez. Fora disso, sorteia. */
    semente: Long? = null,
) {
    var partida by remember { mutableStateOf<Partida?>(null) }
    val p = partida
    if (p == null) {
        EscolherPuzzle(
            estado = estado,
            fonteInicial = fonteInicial,
            semente = semente,
            onJogar = { partida = it },
            onVoltar = onVoltar,
        )
    } else {
        val (d, obra) = resolverFonte(estado, p.fonte) ?: run { partida = null; return }
        JogarPuzzle(
            partida = p,
            d = d,
            obra = obra,
            estado = estado,
            onAtualizar = onAtualizar,
            onNovaPartida = { partida = it },
            onMenu = { partida = null },
        )
    }
}

@Composable
private fun EscolherPuzzle(
    estado: EstadoJogo,
    fonteInicial: String?,
    semente: Long?,
    onJogar: (Partida) -> Unit,
    onVoltar: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var escolhido by remember { mutableStateOf(fonteInicial?.substringAfter(':')?.takeIf { it in Desenhos.porId } ?: Desenhos.todos.first().id) }
    var comMinhaPintura by remember { mutableStateOf(fonteInicial?.startsWith("obra:") == true) }
    val d = Desenhos.porId.getValue(escolhido)
    val temPintura = d.id in estado.coloridos
    val aberto = estado.nivelDePuzzleAberto(d.colecao)
    var nivel by remember(escolhido) { mutableIntStateOf(Puzzle.sugerir(maxOf(1, aberto - 1), estado.puzzleSeguidas, false, aberto)) }
    LaunchedEffect(Unit) { narrador.falar("Quebra cabeças! Escolha uma imagem e um nível.") }

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
                TituloQueCabe("Quebra cabeças", Modifier.weight(1f), estilo = MaterialTheme.typography.headlineMedium)
                Text(
                    "🧩 ${estado.pecasDouradas}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Cores.Texto,
                    modifier = Modifier.semantics { contentDescription = "Você tem ${estado.pecasDouradas} peças douradas" }.testTag("pecas_douradas"),
                )
            }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Escolha a imagem", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cores.Texto)
                for (linha in Desenhos.todos.chunked(4)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (x in linha) {
                            val sel = x.id == escolhido
                            Box(
                                Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .border(if (sel) 4.dp else 1.dp, if (sel) Cores.Estrela else Color(0x33000000), RoundedCornerShape(12.dp))
                                    .clickable { sons.toque(); escolhido = x.id; narrador.falar(x.titulo) }
                                    .semantics { contentDescription = x.titulo + if (sel) ", escolhido" else "" }
                                    .testTag("puzzle_imagem_${x.id}"),
                            ) {
                                DesenhoNoCanvas(x, Colorir.obraModelo(x), Modifier.fillMaxSize().padding(3.dp))
                            }
                        }
                        repeat(4 - linha.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                if (temPintura) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        for ((rotulo, minha) in listOf("Imagem bonita" to false, "Minha pintura" to true)) {
                            BotaoFofo(
                                onClick = { sons.toque(); comMinhaPintura = minha },
                                cor = if (comMinhaPintura == minha) Cores.Estrela else Color.White,
                                corBase = Color(0x33000000),
                                modifier = Modifier.weight(1f).testTag(if (minha) "puzzle_minha_pintura" else "puzzle_imagem_bonita"),
                                descricao = rotulo + if (comMinhaPintura == minha) ", escolhido" else "",
                                profundidade = 4.dp,
                            ) { Text(rotulo, color = Cores.Texto, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) }
                        }
                    }
                }

                Text("Escolha o nível", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Cores.Texto)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    for (n in Puzzle.niveis) {
                        val liberado = n.numero <= aberto
                        val sel = nivel == n.numero
                        val estrelas = estado.puzzlesFeitos[Puzzle.chave(d.id, n.numero)] ?: 0
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!liberado) Color(0xFFDDDDE6) else if (sel) Cores.Estrela else Color.White)
                                .border(if (sel) 3.dp else 1.dp, Color(0x33000000), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (liberado) {
                                        sons.toque(); nivel = n.numero
                                        narrador.falar("Nível ${n.numero}, ${n.pecas} peças.")
                                    } else {
                                        sons.erro()
                                        narrador.falar("Monte o nível ${aberto} desta coleção para abrir este!")
                                    }
                                }
                                .padding(vertical = 6.dp)
                                .semantics {
                                    contentDescription = "Nível ${n.numero}, ${n.pecas} peças" + if (!liberado) ", fechado" else if (estrelas > 0) ", $estrelas estrelas" else ""
                                }
                                .testTag("puzzle_nivel_${n.numero}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(if (liberado) "${n.numero}" else "🔒", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Cores.Texto)
                            Text("${n.pecas}", fontSize = 11.sp, color = Cores.TextoSuave)
                            Text(if (estrelas > 0) "⭐$estrelas" else " ", fontSize = 10.sp)
                        }
                    }
                }
                val g = Puzzle.nivel(nivel)
                Text(
                    "Nível $nivel: ${g.pecas} peças. Monte arrastando as peças, ou deslize as peças para o espaço vazio.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Cores.Texto,
                )

                Spacer(Modifier.height(8.dp))
            }
            // Os botões de jogar ficam fixos embaixo: a criança não precisa rolar a lista para achar.
            val fonte = (if (comMinhaPintura && temPintura) "obra:" else "modelo:") + d.id
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotaoFofo(
                    onClick = { sons.colocar(); onJogar(Partida(fonte, nivel, TipoDeJogo.MONTAR, semente ?: Random.nextLong())) },
                    cor = Cores.Roxo,
                    modifier = Modifier.weight(1f).testTag("puzzle_montar"),
                    descricao = "Montar o quebra cabeça",
                ) { Text("🧩 Montar", color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp)) }
                BotaoFofo(
                    onClick = { sons.colocar(); onJogar(Partida(fonte, nivel, TipoDeJogo.DESLIZAR, semente ?: Random.nextLong())) },
                    cor = Cores.Certo,
                    modifier = Modifier.weight(1f).testTag("puzzle_deslizar"),
                    descricao = "Deslizar as peças",
                ) { Text("🔀 Deslizar", color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp)) }
            }
        }
    }
}

@Composable
private fun JogarPuzzle(
    partida: Partida,
    d: DesenhoDeColorir,
    obra: Obra,
    estado: EstadoJogo,
    onAtualizar: ((EstadoJogo) -> EstadoJogo) -> Unit,
    onNovaPartida: (Partida) -> Unit,
    onMenu: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val estadoAtual by rememberUpdatedState(estado)
    val atualizar by rememberUpdatedState(onAtualizar)

    var montagem by remember(partida) { mutableStateOf(Puzzle.montar(partida.fonte, partida.nivel, partida.semente)) }
    var deslizante by remember(partida) { mutableStateOf(Puzzle.deslizar(ladoDoDeslizante(partida.nivel), partida.semente)) }
    var arrasto by remember(partida) { mutableStateOf<Pair<Int, Offset>?>(null) }
    var dicaDeslizante by remember(partida) { mutableIntStateOf(-1) }
    var dicaPeca by remember(partida) { mutableIntStateOf(-1) }
    var dicas by remember(partida) { mutableIntStateOf(0) }
    var imaLargo by remember { mutableStateOf(partida.nivel <= 2) }
    var sombra by remember { mutableStateOf(partida.nivel <= 4) }
    var bordas by remember { mutableStateOf(false) }
    var premio by remember(partida) { mutableStateOf<Puzzle.Premio?>(null) }
    val atual by rememberUpdatedState(montagem)
    var estrelasGanhas by remember(partida) { mutableIntStateOf(0) }

    LaunchedEffect(partida) { narrador.falar(if (partida.tipo == TipoDeJogo.MONTAR) "Arraste cada peça para o lugar dela." else "Deslize as peças para o espaço vazio.") }
    LaunchedEffect(dicaPeca, dicaDeslizante) {
        if (dicaPeca >= 0 || dicaDeslizante >= 0) {
            kotlinx.coroutines.delay(2200)
            dicaPeca = -1
            dicaDeslizante = -1
        }
    }

    fun vencer(estrelas: Int, usouDica: Boolean) {
        val (_, p) = estadoAtual.venceuPuzzle(d.id, partida.nivel, estrelas, usouDica)
        atualizar { it.venceuPuzzle(d.id, partida.nivel, estrelas, usouDica).first }
        sons.premio()
        estrelasGanhas = estrelas
        premio = p
        narrador.falar("Você montou! " + when (estrelas) { 3 -> "Três estrelas!"; 2 -> "Duas estrelas!"; else -> "Uma estrela!" })
    }

    EscalaDeTablet {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .ceuDeBrinquedo(comGrama = false)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BotaoRedondo("⬅️", "Voltar para o menu dos quebra cabeças", { sons.toque(); onMenu() }, tamanho = 52.dp)
                    Spacer(Modifier.width(8.dp))
                    TituloQueCabe(d.titulo, Modifier.weight(1f), estilo = MaterialTheme.typography.headlineSmall)
                    val falta = if (partida.tipo == TipoDeJogo.MONTAR) montagem.faltam else -1
                    if (falta >= 0) {
                        Text(
                            "Faltam $falta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Cores.Texto,
                            modifier = Modifier.testTag("puzzle_faltam"),
                        )
                    }
                }

                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.TopCenter) {
                    val altura = if (partida.tipo == TipoDeJogo.MONTAR) Puzzle.BANDEJA_ATE else 1f
                    val lado = minOf(maxWidth, maxHeight / altura)
                    val ladoPx = with(LocalDensity.current) { lado.toPx() }

                    if (partida.tipo == TipoDeJogo.MONTAR) {
                        val m = montagem
                        val g = m.grade
                        val larguraPeca = lado * m.largura
                        val alturaPeca = lado * m.altura
                        val dicaCasa = dicaPeca
                        Box(Modifier.size(lado, lado * altura)) {
                            // Moldura e bandeja.
                            Canvas(Modifier.fillMaxSize().testTag("moldura_do_puzzle")) {
                                val s = size.width
                                drawRoundRect(Color.White, Offset.Zero, Size(s, s), androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()))
                                if (sombra) {
                                    desenharObra(d, obra, s)
                                    drawRect(Color.White.copy(alpha = 0.8f), Offset.Zero, Size(s, s))
                                }
                                for (c in 1 until g.colunas) drawLine(Color(0x33000000), Offset(c * s / g.colunas, 0f), Offset(c * s / g.colunas, s), 1.5f)
                                for (l in 1 until g.linhas) drawLine(Color(0x33000000), Offset(0f, l * s / g.linhas), Offset(s, l * s / g.linhas), 1.5f)
                                if (dicaCasa >= 0) {
                                    drawRect(
                                        Color(0xFFFFD166).copy(alpha = 0.7f),
                                        Offset(Puzzle.alvoX(m, dicaCasa) * s, Puzzle.alvoY(m, dicaCasa) * s),
                                        Size(s / g.colunas, s / g.linhas),
                                    )
                                }
                                drawRoundRect(
                                    Color(0x33FFFFFF),
                                    Offset(0f, Puzzle.BANDEJA_DE * s - 8.dp.toPx()),
                                    Size(s, (Puzzle.BANDEJA_ATE - Puzzle.BANDEJA_DE) * s + 16.dp.toPx()),
                                    androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                                )
                                drawRoundRect(Color(0xFF7A4B2A), Offset.Zero, Size(s, s), androidx.compose.ui.geometry.CornerRadius(12.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(4.dp.toPx()))
                            }

                            val soltasNaBorda = m.pecas.any { !it.colocada && Puzzle.ehBorda(m, it.id) }
                            for (peca in m.pecas) {
                                val emArrasto = arrasto?.takeIf { it.first == peca.id }
                                val x = emArrasto?.second?.x ?: peca.x
                                val y = emArrasto?.second?.y ?: peca.y
                                val apagada = bordas && soltasNaBorda && !peca.colocada && !Puzzle.ehBorda(m, peca.id)
                                Box(
                                    Modifier
                                        .offset(lado * x, lado * y)
                                        .size(larguraPeca, alturaPeca)
                                        .zIndex(if (emArrasto != null) 20f else if (peca.colocada) 0f else 5f)
                                        .alpha(if (apagada) 0.3f else 1f)
                                        .then(
                                            if (peca.colocada) Modifier
                                            else Modifier
                                                .border(1.5.dp, Color(0x88000000), RoundedCornerShape(3.dp))
                                                .pointerInput(peca.id, ladoPx, imaLargo) {
                                                    detectDragGestures(
                                                        onDragStart = {
                                                            val p = atual.pecas.firstOrNull { it.id == peca.id }
                                                            if (p != null) arrasto = peca.id to Offset(p.x, p.y)
                                                            sons.toque()
                                                        },
                                                        onDrag = { change, arrastoPx ->
                                                            change.consume()
                                                            val a = arrasto
                                                            if (a != null && a.first == peca.id) {
                                                                arrasto = peca.id to Offset(a.second.x + arrastoPx.x / ladoPx, a.second.y + arrastoPx.y / ladoPx)
                                                            }
                                                        },
                                                        onDragEnd = {
                                                            val a = arrasto
                                                            arrasto = null
                                                            if (a != null && a.first == peca.id) {
                                                                val antes = atual
                                                                val depois = Puzzle.soltar(antes, peca.id, a.second.x, a.second.y, imaLargo)
                                                                montagem = depois
                                                                val encaixou = depois.pecas.first { it.id == peca.id }.colocada
                                                                if (encaixou) sons.acerto() else sons.pousar()
                                                                if (depois.venceu) vencer(Puzzle.estrelas(depois), depois.dicas > 0)
                                                            }
                                                        },
                                                        onDragCancel = { arrasto = null },
                                                    )
                                                }
                                                .testTag("peca_${peca.id}"),
                                        )
                                        .semantics { contentDescription = "Peça ${peca.id + 1}" + if (peca.colocada) ", no lugar" else "" },
                                ) {
                                    PecaDeQuebraCabeca(d, obra, peca.id % g.colunas, peca.id / g.colunas, g.colunas, g.linhas)
                                }
                            }
                        }
                    } else {
                        val dz = deslizante
                        val celula = lado / dz.lado
                        Box(Modifier.size(lado).testTag("moldura_do_deslizante").clip(RoundedCornerShape(12.dp)).background(Color.White)) {
                            for ((i, n) in dz.casas.withIndex()) {
                                if (n == 0 && !dz.resolvido) continue
                                val numero = if (n == 0) dz.lado * dz.lado else n
                                val col = i % dz.lado
                                val lin = i / dz.lado
                                Box(
                                    Modifier
                                        .offset(celula * col, celula * lin)
                                        .size(celula)
                                        .border(if (dicaDeslizante == i) 4.dp else 1.5.dp, if (dicaDeslizante == i) Cores.Estrela else Color(0x88000000), RoundedCornerShape(3.dp))
                                        .clickable(enabled = !dz.resolvido) {
                                            val novo = Puzzle.tocar(deslizante, i)
                                            if (novo != deslizante) {
                                                sons.colocar()
                                                deslizante = novo
                                                if (novo.resolvido) vencer(Puzzle.estrelasDoDeslizante(novo, dicas), dicas > 0)
                                            } else {
                                                sons.erro()
                                            }
                                        }
                                        .semantics { contentDescription = "Peça $numero" }
                                        .testTag("casa_$i"),
                                ) {
                                    PecaDeQuebraCabeca(d, obra, (numero - 1) % dz.lado, (numero - 1) / dz.lado, dz.lado, dz.lado)
                                }
                            }
                        }
                    }
                }

                // Ajudas: dica, ímã largo, sombra e bordas primeiro.
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    @Composable
                    fun Ajuda(emoji: String, descricao: String, ligada: Boolean, tag: String, onClick: () -> Unit) {
                        BotaoFofo(
                            onClick = onClick,
                            cor = if (ligada) Cores.Estrela else Color.White,
                            corBase = Color(0x33000000),
                            descricao = descricao + if (ligada) ", ligado" else "",
                            modifier = Modifier.testTag(tag),
                            profundidade = 4.dp,
                        ) { Box(Modifier.size(width = 56.dp, height = 48.dp), contentAlignment = Alignment.Center) { Text(emoji, fontSize = 24.sp) } }
                    }
                    Ajuda("💡", "Dica: mostra uma peça e o lugar dela", false, "puzzle_dica") {
                        sons.toque()
                        dicas++
                        if (partida.tipo == TipoDeJogo.MONTAR) {
                            val (id, nova) = Puzzle.dica(montagem)
                            if (id >= 0) {
                                montagem = nova
                                dicaPeca = id
                                narrador.falar("A peça ${id + 1} vai aqui.")
                            }
                        } else {
                            dicaDeslizante = Puzzle.dicaDeslizante(deslizante)
                            narrador.falar("Toque na peça que está piscando.")
                        }
                    }
                    if (partida.tipo == TipoDeJogo.MONTAR) {
                        Ajuda("🧲", "Ímã largo: a peça gruda de mais longe", imaLargo, "puzzle_ima") { sons.toque(); imaLargo = !imaLargo }
                        Ajuda("👁️", "Sombra: mostra o desenho de fundo", sombra, "puzzle_sombra") { sons.toque(); sombra = !sombra }
                        Ajuda("🔲", "Bordas primeiro", bordas, "puzzle_bordas") { sons.toque(); bordas = !bordas }
                    }
                }
            }

            val pr = premio
            if (pr != null) {
                PremioDoPuzzle(
                    d = d,
                    obra = obra,
                    premio = pr,
                    estrelas = estrelasGanhas,
                    proximoNivel = Puzzle.sugerir(partida.nivel, estadoAtual.puzzleSeguidas, false, estadoAtual.nivelDePuzzleAberto(d.colecao)),
                    onProximo = { n -> onNovaPartida(partida.copy(nivel = n, semente = Random.nextLong())) },
                    onDeNovo = { onNovaPartida(partida.copy(semente = Random.nextLong())) },
                    onMenu = onMenu,
                )
            }
        }
    }
}

@Composable
private fun PremioDoPuzzle(
    d: DesenhoDeColorir,
    obra: Obra,
    premio: Puzzle.Premio,
    estrelas: Int,
    proximoNivel: Int,
    onProximo: (Int) -> Unit,
    onDeNovo: () -> Unit,
    onMenu: () -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(Color(0x99000000)).pointerInput(Unit) { detectTapGestures { } }.windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(20.dp).cartaoFofo(raio = Raios.Enorme).padding(16.dp).testTag("premio_puzzle"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Você montou!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Cores.Texto)
            DesenhoNoCanvas(d, obra, Modifier.fillMaxWidth(0.6f).aspectRatio(1f))
            Text(estrelasEmTexto(estrelas), fontSize = 30.sp, modifier = Modifier.semantics { contentDescription = "$estrelas estrelas" })
            if (premio.moedas > 0) Text("+${premio.moedas} moedinhas 🪙", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Cores.Texto)
            if (premio.pecaDourada > 0) {
                Text(
                    if (premio.pecaDourada == 1) "Peça dourada nova! 🧩" else "${premio.pecaDourada} peças douradas novas! 🧩",
                    style = MaterialTheme.typography.titleMedium, color = Cores.Texto, fontWeight = FontWeight.Bold,
                )
            }
            val marco = premio.marco
            if (marco != null) Text(marco.nome + "! 🏅", style = MaterialTheme.typography.titleMedium, color = Cores.Texto, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            if (premio.colecaoCompleta) Text("Você venceu a coleção inteira! 🏆", style = MaterialTheme.typography.titleMedium, color = Cores.Texto, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            BotaoFofo(
                onClick = { onProximo(proximoNivel) },
                cor = Cores.Roxo,
                modifier = Modifier.fillMaxWidth().testTag("premio_proximo"),
                descricao = "Próximo quebra cabeça, nível $proximoNivel",
            ) { Text("Próximo: nível $proximoNivel", color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 4.dp)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BotaoFofo(onClick = onDeNovo, cor = Color.White, modifier = Modifier.weight(1f).testTag("premio_de_novo"), descricao = "Montar de novo") {
                    Text("De novo", color = Cores.Texto, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 4.dp))
                }
                BotaoFofo(onClick = onMenu, cor = Color.White, modifier = Modifier.weight(1f).testTag("premio_menu"), descricao = "Voltar ao menu") {
                    Text("Menu", color = Cores.Texto, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}
