package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import br.com.casinhadosaber.ui.componentes.escurecer
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Cena
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BalaoDeFala
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.AtividadeInterativa
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val elogios = listOf("Muito bem!", "Isso aí!", "Acertou!", "Você é demais!", "Perfeito!", "Uau, que esperto!")

@Composable
fun TelaJogo(
    fase: Fase,
    perfil: Perfil?,
    onSair: () -> Unit,
    /** Um item por pergunta (sem contar o exemplo): acertou de primeira? */
    onConcluir: (resultados: List<Boolean>) -> Unit,
    /** Depois de duas tentativas sem vencer, cada pergunta vem com só duas opções. */
    facilitada: Boolean = false,
    /** Perguntas já escolhidas (missão do dia); se vier vazio, sorteia as da fase. */
    perguntasProntas: List<Pergunta> = emptyList(),
    /**
     * Treino sem fim: cada pergunta nova vem daqui, recebendo os resultados até agora (para a dificuldade
     * se ajustar). O ✖ termina o treino e entrega os resultados em [onConcluir].
     */
    treino: ((resultados: List<Boolean>) -> Pergunta)? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val escopo = rememberCoroutineScope()
    // Na rodada facilitada, a primeira pergunta é um exemplo resolvido junto (a resposta brilha e não conta).
    val perguntas = remember(fase, facilitada) {
        val iniciais = when {
            treino != null -> listOf(treino(emptyList()))
            else -> {
                val normais = perguntasProntas.ifEmpty { GeradorDeFases().perguntas(fase) }
                if (facilitada) listOf(GeradorDeFases().perguntas(fase, 1).first()) + normais else normais
            }
        }
        mutableStateListOf<Pergunta>().apply { addAll(iniciais) }
    }
    var indice by remember { mutableIntStateOf(0) }
    var resultados by remember { mutableStateOf(listOf<Boolean>()) }
    /** Como cada pergunta já feita terminou (índice → acertou de primeira?), para as bolinhas de cima. */
    var comoFoi by remember { mutableStateOf(mapOf<Int, Boolean>()) }
    var pulo by remember { mutableIntStateOf(0) }
    var erradas by remember { mutableStateOf(setOf<Int>()) }
    var acertou by remember { mutableStateOf(false) }
    var humor by remember { mutableStateOf(Humor.FELIZ) }
    var confete by remember { mutableIntStateOf(0) }
    val pergunta = perguntas[indice]
    val exemplo = facilitada && indice == 0
    val cor = Color(fase.trilha.corArgb)
    val vibracao = LocalHapticFeedback.current

    LaunchedEffect(indice) {
        delay(350)
        if (exemplo && pergunta.atividade != Atividade.Escolha) {
            // Atividade de mexer: a ajuda (brilho e mãozinha) aparece desde o começo.
            narrador.falar(listOf(pt("Vamos fazer uma juntos?")) + pergunta.fala + listOf(pt("Siga a mãozinha e o que está brilhando.")))
        } else if (exemplo) {
            narrador.falar(
                listOf(pt("Vamos fazer uma juntos?")) + pergunta.fala + pergunta.reforco +
                    listOf(pt("A resposta é a que está brilhando. Toque nela!")),
            )
        } else {
            narrador.falar(pergunta.fala)
        }
    }

    /** Acertou (escolha ou atividade de mexer): som, confete, elogio e próxima pergunta. */
    fun comemorar(dePrimeira: Boolean) {
        acertou = true
        if (!exemplo) resultados = resultados + dePrimeira
        comoFoi = comoFoi + (indice to (dePrimeira || exemplo))
        humor = Humor.FELIZ
        pulo++
        vibracao.performHapticFeedback(HapticFeedbackType.LongPress)
        confete++
        sons.acerto()
        val elogio = elogios.random()
        narrador.falar(listOf(pt(elogio)) + pergunta.reforco)
        val caracteres = elogio.length + pergunta.reforco.sumOf { it.texto.length }
        escopo.launch {
            delay((1200L + caracteres * 65L).coerceAtMost(5000L))
            if (treino != null) {
                // Treino sem fim: a próxima pergunta já vem ajustada aos acertos e erros até aqui.
                perguntas.add(treino(resultados))
                indice++
                erradas = emptySet()
                acertou = false
            } else if (indice < perguntas.lastIndex) {
                indice++
                erradas = emptySet()
                acertou = false
            } else {
                onConcluir(resultados)
            }
        }
    }

    fun responder(i: Int) {
        if (acertou || i in erradas) return
        if (i == pergunta.indiceCorreto) {
            comemorar(erradas.isEmpty())
        } else {
            erradas = erradas + i
            humor = Humor.PENSANDO
            sons.erro()
            if (erradas.size >= 2) {
                // Ajuda: depois de dois erros, a resposta certa brilha e a pergunta é repetida.
                narrador.falar(pergunta.opcoes[i].fala + pt("Vamos juntos? Olhe a que está brilhando.") + pergunta.fala)
            } else {
                narrador.falar(pergunta.opcoes[i].fala + pt("Tente outra vez!"))
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cor.copy(alpha = 0.35f), Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            // barra de cima
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (treino != null) {
                    BotaoRedondo("✖️", "Terminar o treino", { onConcluir(resultados) }, tamanho = 48.dp)
                } else {
                    BotaoRedondo("✖️", "Sair da fase", onSair, tamanho = 48.dp)
                }
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    // Bolinhas do progresso, uma cor por estado (nunca vermelho: vermelho parece erro):
                    // verde com estrela = acertou de primeira; amarelo com estrela = acertou com ajuda;
                    // branca com borda na cor do caminho = a da vez; branca clarinha = ainda vem.
                    // No treino (sem fim), só as últimas bolinhas aparecem.
                    val visiveis = if (treino != null) maxOf(0, indice - 5)..indice else perguntas.indices
                    for (i in visiveis) {
                        val resultado = comoFoi[i]
                        val daVez = i == indice && resultado == null
                        val tamanho = if (i == indice) 24.dp else 18.dp
                        val (fundo, borda) = when {
                            resultado == true -> Cores.Certo to Color(0xFF2A9452)
                            resultado == false -> Cores.Estrela to Color(0xFFE0A800)
                            daVez -> Color.White to escurecer(cor)
                            else -> Color.White to Color(0x33000000)
                        }
                        Box(
                            Modifier
                                .padding(horizontal = 4.dp)
                                .size(tamanho)
                                .background(fundo, CircleShape)
                                .border(if (daVez) 4.dp else 3.dp, borda, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (resultado != null) {
                                Text("★", color = Color.White, fontSize = (tamanho.value * 0.6f).sp, lineHeight = (tamanho.value * 0.7f).sp)
                            }
                        }
                    }
                }
                BotaoRedondo("🔊", "Ouvir de novo", { narrador.falar(pergunta.fala) }, tamanho = 48.dp)
            }

            // guia
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val deMexer = pergunta.atividade != Atividade.Escolha
                if (pergunta.cena != Cena.Nenhuma || deMexer) {
                    perfil?.let { CriancaQuePula(it, pulo, acertou, humor, falando = narrador.falando, altura = if (deMexer) 76.dp else 96.dp) }
                }
                Spacer(Modifier.width(8.dp))
                BalaoDeFala(
                    "🔊 " + pergunta.instrucao,
                    Modifier
                        .weight(1f)
                        .clickable(onClickLabel = "Ouvir de novo") { narrador.falar(pergunta.fala) },
                )
            }

            AnimatedContent(
                targetState = indice,
                transitionSpec = {
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                },
                modifier = Modifier.weight(1f),
                label = "pergunta",
            ) { i ->
                val p = perguntas[i]
                val opcoes = @Composable { alturaGrande: androidx.compose.ui.unit.Dp? ->
                    Opcoes(
                        pergunta = p,
                        erradas = if (i == indice) erradas else emptySet(),
                        acertou = i == indice && acertou,
                        onTocar = ::responder,
                        mostrarCerta = facilitada && i == 0,
                        alturaGrande = alturaGrande,
                    )
                }
                if (p.atividade != Atividade.Escolha) {
                    // Atividades de mexer (cesta, memória, montar a palavra, traçar): terminam no mesmo
                    // fluxo das perguntas de tocar (acertou de primeira se fez sem errar).
                    AtividadeInterativa(
                        atividade = p.atividade,
                        fala = p.fala,
                        ativa = i == indice && !acertou,
                        ajudaInicial = facilitada && i == 0,
                        cor = cor,
                        onErro = { if (i == indice) humor = Humor.PENSANDO },
                        onConcluir = { semErro -> if (i == indice && !acertou) comemorar(semErro) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 12.dp),
                    )
                } else {
                    // Mesmo esqueleto em todos os jogos: figura no meio, respostas sempre embaixo.
                    Column(Modifier.fillMaxSize()) {
                        if (p.cena == Cena.Nenhuma) {
                            // Sem figura: as respostas (que são a própria pergunta) ocupam toda a altura livre.
                            BoxWithConstraints(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                            ) {
                                val alturaRespostas = minOf(maxHeight * 0.6f, 420.dp)
                                Column(Modifier.fillMaxSize()) {
                                    // Espaço de cima: a criança "conta" a pergunta, grandona, mexendo a boca.
                                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        perfil?.let { CriancaQuePula(it, pulo, acertou, humor, falando = narrador.falando, altura = 150.dp) }
                                    }
                                    opcoes(alturaRespostas)
                                }
                            }
                        } else {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CenaDaPergunta(p.cena)
                            }
                            opcoes(null)
                        }
                    }
                }
            }
        }
        Confete(confete)
    }
}

@Composable
private fun CriancaQuePula(
    perfil: Perfil,
    pulo: Int,
    acertou: Boolean,
    humor: Humor,
    falando: Boolean = false,
    altura: androidx.compose.ui.unit.Dp = 96.dp,
) {
    val salto = remember { Animatable(0f) }
    LaunchedEffect(pulo) {
        if (pulo > 0) {
            salto.animateTo(-28f, tween(150))
            salto.animateTo(0f, spring(dampingRatio = 0.35f))
        }
    }
    Crianca(
        perfil,
        Modifier
            .height(altura)
            .graphicsLayer { translationY = salto.value * density },
        acenando = acertou,
        humor = humor,
        falando = falando && !acertou,
    )
}

// ------------------------------------------------------------------------------------- cena

/** Quantas figuras há num texto de emojis (ignorando seletores de variação). */
private fun contarFiguras(s: String): Int =
    s.codePoints().filter { it != 0xFE0F && it != 0x200D }.count().toInt()

private fun separarFiguras(s: String): List<String> {
    val partes = mutableListOf<String>()
    var i = 0
    while (i < s.length) {
        val cp = s.codePointAt(i)
        var fim = i + Character.charCount(cp)
        if (fim < s.length && s.codePointAt(fim) == 0xFE0F) fim++
        partes += s.substring(i, fim)
        i = fim
    }
    return partes
}

private fun tamanhoParaFiguras(n: Int): TextUnit = when {
    n <= 1 -> 110.sp
    n <= 3 -> 72.sp
    n <= 6 -> 56.sp
    else -> 44.sp
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CenaDaPergunta(cena: Cena) {
    when (cena) {
        Cena.Nenhuma -> Unit
        is Cena.Emojis -> BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val figuras = separarFiguras(cena.texto)
            val n = figuras.size
            // Até 6: grade de 3 por linha (como num dado); mais que isso: 5 por linha (como os dedos da mão).
            val porLinha = if (n <= 3) n else if (n <= 6) 3 else 5
            val linhas = (n + porLinha - 1) / porLinha
            val tamanho = minOf(maxWidth * 0.8f / porLinha, maxHeight * 0.7f / linhas, 150.dp)
            Column(
                Modifier
                    .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .then(if (n == 1) Modifier.pulsando() else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for (linha in figuras.chunked(porLinha)) {
                    Row {
                        for (f in linha) {
                            Box(Modifier.size(tamanho), contentAlignment = Alignment.Center) {
                                Text(f, fontSize = (tamanho.value * 0.75f).sp, lineHeight = (tamanho.value * 0.85f).sp)
                            }
                        }
                    }
                }
            }
        }
        is Cena.Palavra -> Text(
            cena.texto,
            style = MaterialTheme.typography.displayLarge.copy(letterSpacing = 4.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 8.dp),
        )
        is Cena.Conta -> Conta(cena)
        is Cena.Grade -> Grade(cena)
        is Cena.Sequencia -> BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            // Todas as figuras numa linha só, para a fila ficar clara.
            val n = cena.itens.size
            // Até 7 numa linha; filas maiores quebram de 6 em 6 (múltiplo de 2 e de 3: o padrão fica alinhado).
            val porLinha = if (n <= 7) n else 6
            val tamanho = ((maxWidth - 6.dp * (porLinha - 1)) / porLinha).coerceAtMost(68.dp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            for (linhaDaFila in cena.itens.chunked(porLinha)) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (item in linhaDaFila) {
                    val pergunta = item == "?"
                    Box(
                        Modifier
                            .size(tamanho)
                            .then(if (pergunta) Modifier.pulsando() else Modifier)
                            .background(if (pergunta) Cores.Estrela else Color.White, RoundedCornerShape(16.dp))
                            .border(3.dp, if (pergunta) Color(0xFFE0A800) else Color(0xFFEDE6FF), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            item,
                            fontSize = (tamanho.value * 0.55f).sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                }
            }
            }
        }
    }
}

/**
 * Quadro em linhas e colunas: labirinto, sudoku de figuras e gráfico. "" é uma casinha vazia
 * (branca) e "?" é a casinha da pergunta, amarela e pulsando como nas filas.
 */
@Composable
private fun Grade(g: Cena.Grade) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val vao = 4.dp
        val vaoDoBloco = if (g.blocos > 0) 8.dp else 0.dp
        val blocosNaLinha = if (g.blocos > 0) g.colunas / g.blocos - 1 else 0
        val blocosNaColuna = if (g.blocos > 0) g.linhas / g.blocos - 1 else 0
        val pelaLargura = (maxWidth - vao * (g.colunas - 1) - vaoDoBloco * blocosNaLinha - 24.dp) / g.colunas
        val pelaAltura = (maxHeight - vao * (g.linhas - 1) - vaoDoBloco * blocosNaColuna - 24.dp) / g.linhas
        val lado = minOf(pelaLargura, pelaAltura, 72.dp).coerceAtLeast(24.dp)
        Column(
            Modifier
                .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (l in 0 until g.linhas) {
                if (l > 0) Spacer(Modifier.height(if (g.blocos > 0 && l % g.blocos == 0) vao + vaoDoBloco else vao))
                Row {
                    for (c in 0 until g.colunas) {
                        if (c > 0) Spacer(Modifier.width(if (g.blocos > 0 && c % g.blocos == 0) vao + vaoDoBloco else vao))
                        val item = g.itens[l * g.colunas + c]
                        val pergunta = item == "?"
                        Box(
                            Modifier
                                .size(lado)
                                .then(if (pergunta) Modifier.pulsando() else Modifier)
                                .background(if (pergunta) Cores.Estrela else Color.White, RoundedCornerShape(12.dp))
                                .border(2.dp, if (pergunta) Color(0xFFE0A800) else Color(0xFFEDE6FF), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (item.isNotEmpty()) {
                                Text(
                                    item,
                                    fontSize = (lado.value * 0.58f).sp,
                                    lineHeight = (lado.value * 0.7f).sp,
                                    fontWeight = if (pergunta) FontWeight.Bold else null,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Conta(c: Cena.Conta) {
    val tamanho = if (c.a + c.b > 6) 34.sp else 44.sp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (c.soma) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.Center,
            ) {
                GrupoDeFiguras(c.emoji, c.a, 0, tamanho)
                Text("+", style = MaterialTheme.typography.displayMedium, modifier = Modifier.align(Alignment.CenterVertically))
                GrupoDeFiguras(c.emoji, c.b, 0, tamanho)
            }
        } else {
            // Na subtração, as figuras que "foram embora" ficam apagadas e riscadas.
            GrupoDeFiguras(c.emoji, c.a, c.b, tamanho)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "${c.a} ${if (c.soma) "+" else "−"} ${c.b} = ?",
            style = MaterialTheme.typography.displayMedium,
            color = Cores.Texto,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GrupoDeFiguras(emoji: String, total: Int, riscadas: Int, tamanho: TextUnit) {
    FlowRow(
        Modifier
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        maxItemsInEachRow = 5,
    ) {
        for (i in 0 until total) {
            val saiu = i >= total - riscadas
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = tamanho, modifier = Modifier.alpha(if (saiu) 0.3f else 1f))
                if (saiu) {
                    Canvas(Modifier.size((tamanho.value * 1.1f).dp)) {
                        val m = size.width * 0.15f
                        drawLine(Cores.Errado, Offset(m, m), Offset(size.width - m, size.height - m), 6.dp.toPx(), StrokeCap.Round)
                        drawLine(Cores.Errado, Offset(size.width - m, m), Offset(m, size.height - m), 6.dp.toPx(), StrokeCap.Round)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------------------------------- opções

@Composable
private fun Opcoes(
    pergunta: Pergunta,
    erradas: Set<Int>,
    acertou: Boolean,
    onTocar: (Int) -> Unit,
    mostrarCerta: Boolean = false,
    /** Altura disponível quando as respostas ocupam a tela toda (jogos sem figura). */
    alturaGrande: androidx.compose.ui.unit.Dp? = null,
) {
    val grupos = pergunta.opcoes.any { (it.visual as? Visual.Emoji)?.let { v -> contarFiguras(v.emoji) > 1 } == true }
    val n = pergunta.opcoes.size
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when {
            grupos -> BoxWithConstraints {
                // Altura do cartão = 3 linhas de figuras + margem; nunca um cartão enorme e vazio.
                val larguraCartao = (maxWidth - 20.dp) / 3
                val celula = minOf(larguraCartao / 2.3f, 64.dp)
                val alturaCartao = minOf(alturaGrande ?: 150.dp, celula * 3 + 36.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pergunta.opcoes.forEachIndexed { i, o ->
                    CartaoOpcao(o, i, pergunta, erradas, acertou, onTocar, Modifier.weight(1f), altura = alturaCartao, mostrarCerta = mostrarCerta, comoGrupo = true)
                }
                }
            }
            n == 4 -> for (linha in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in linha * 2 until linha * 2 + 2) {
                        CartaoOpcao(pergunta.opcoes[i], i, pergunta, erradas, acertou, onTocar, Modifier.weight(1f), altura = alturaGrande?.let { (it - 10.dp) / 2 } ?: 112.dp, mostrarCerta = mostrarCerta)
                    }
                }
            }
            else -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pergunta.opcoes.forEachIndexed { i, o ->
                    CartaoOpcao(o, i, pergunta, erradas, acertou, onTocar, Modifier.weight(1f), altura = alturaGrande ?: 130.dp, mostrarCerta = mostrarCerta)
                }
            }
        }
    }
}

@Composable
private fun CartaoOpcao(
    opcao: Opcao,
    i: Int,
    pergunta: Pergunta,
    erradas: Set<Int>,
    acertou: Boolean,
    onTocar: (Int) -> Unit,
    modifier: Modifier,
    altura: androidx.compose.ui.unit.Dp = 112.dp,
    mostrarCerta: Boolean = false,
    /** Opção de quantidade: até 1 figura vai na grade, do mesmo tamanho das outras. */
    comoGrupo: Boolean = false,
) {
    val certa = acertou && i == pergunta.indiceCorreto
    val errada = i in erradas
    val ajuda = !acertou && (erradas.size >= 2 || mostrarCerta) && i == pergunta.indiceCorreto
    val apagada = acertou && !certa
    val cor by animateColorAsState(
        when {
            certa -> Cores.Certo
            errada -> Color(0xFFFFE1E1)
            ajuda -> Color(0xFFFFF3C4)
            else -> Color.White
        },
        label = "cor",
    )
    val tremida = remember { Animatable(0f) }
    val pulo = remember { Animatable(1f) }
    LaunchedEffect(errada) {
        if (errada) for (x in listOf(7f, -7f, 5f, -5f, 2f, 0f)) tremida.animateTo(x, tween(50))
    }
    LaunchedEffect(certa) {
        if (certa) {
            pulo.animateTo(1.15f, tween(110))
            pulo.animateTo(1f, spring(dampingRatio = 0.35f))
        }
    }
    BotaoFofo(
        onClick = { onTocar(i) },
        cor = cor,
        corBase = if (certa) Color(0xFF2A9452) else Color(0xFFD9CFF0),
        descricao = descreverOpcao(opcao),
        modifier = modifier
            // Marca usada só pelos testes automáticos no emulador (não aparece para a criança).
            .testTag(if (i == pergunta.indiceCorreto) "opcao_certa" else "opcao")
            .then(if (ajuda) Modifier.pulsando() else Modifier)
            .graphicsLayer {
                rotationZ = tremida.value
                scaleX = pulo.value
                scaleY = pulo.value
                alpha = if (apagada || errada) 0.45f else 1f
            },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = altura)
                .padding(8.dp),
            contentAlignment = if (comoGrupo) Alignment.TopCenter else Alignment.Center,
        ) {
            VisualDaOpcao(opcao.visual, certa, altura, comoGrupo)
            if (certa || errada) {
                Text(
                    if (certa) "✔" else "✖",
                    color = if (certa) Color.White else Cores.Errado,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
        }
    }
}

private fun descreverOpcao(o: Opcao): String = when (val v = o.visual) {
    is Visual.Texto -> v.texto
    is Visual.Emoji -> o.fala.joinToString(" ") { it.texto }.ifBlank { v.emoji }
    is Visual.Cor -> o.fala.joinToString(" ") { it.texto }.ifBlank { "Cor" }
    is Visual.Sombra -> "Sombra"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisualDaOpcao(v: Visual, certa: Boolean, altura: androidx.compose.ui.unit.Dp = 150.dp, comoGrupo: Boolean = false) {
    when (v) {
        is Visual.Texto -> Text(
            v.texto,
            // Palavras e valores maiores (CHAVE, R$ 10) diminuem para caber inteiros no cartão.
            style = MaterialTheme.typography.displayLarge.let { estilo ->
                when (v.texto.length) {
                    in 0..3 -> estilo
                    4 -> estilo.copy(fontSize = 44.sp)
                    5 -> estilo.copy(fontSize = 36.sp)
                    else -> estilo.copy(fontSize = 30.sp)
                }
            },
            color = if (certa) Color.White else Cores.Texto,
            maxLines = 1,
        )
        is Visual.Emoji -> {
            val figuras = separarFiguras(v.emoji)
            if (figuras.size > 1 || comoGrupo) {
                // Todas as quantidades na mesma grade de 2 colunas, com o mesmo tamanho de casinha:
                // "mais" sempre parece maior, sem a altura enganar.
                BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    val celula = minOf(maxWidth / 2.3f, (altura - 24.dp) / 3f, 64.dp)
                    Column(horizontalAlignment = Alignment.Start) {
                        for (linha in figuras.chunked(2)) {
                            Row {
                                for (f in linha) {
                                    Box(Modifier.size(celula), contentAlignment = Alignment.Center) {
                                        Text(f, fontSize = (celula.value * 0.78f).sp, lineHeight = (celula.value * 0.9f).sp)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val base = minOf(altura.value * 0.42f, 100f)
                Text(v.emoji, fontSize = (base * v.escala).sp, lineHeight = (base * 1.12f * v.escala).sp)
            }
        }
        is Visual.Sombra -> Text(
            v.emoji,
            fontSize = 54.sp,
            modifier = Modifier
                .graphicsLayer(compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    drawRect(Color(0xFF3D2C5E), blendMode = androidx.compose.ui.graphics.BlendMode.SrcIn)
                },
        )
        is Visual.Cor -> Box(
            Modifier
                .size(76.dp)
                .background(Color(v.argb), CircleShape)
                .border(4.dp, Color(0x22000000), CircleShape),
        )
    }
}
