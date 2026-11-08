package br.com.casinhadosaber.ui.telas

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import br.com.casinhadosaber.ui.componentes.SetaDesenhada
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
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.zIndex
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
import br.com.casinhadosaber.dominio.Historia
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.CorDaConta
import br.com.casinhadosaber.ui.componentes.FiguraDaPergunta
import br.com.casinhadosaber.ui.componentes.FigurasNaCena
import br.com.casinhadosaber.ui.componentes.IconeDaBarra
import br.com.casinhadosaber.ui.componentes.IconeDeArteDaBarra
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.Irmaozinho
import br.com.casinhadosaber.ui.componentes.LadoDaCauda
import br.com.casinhadosaber.ui.componentes.PilulaDeContas
import br.com.casinhadosaber.ui.componentes.TextoComContorno
import br.com.casinhadosaber.ui.componentes.balaoDeFala
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.componentes.cenarioDoJogo
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.em
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.telas.atividades.AtividadeInterativa
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val elogios = listOf("Muito bem!", "Isso aí!", "Acertou!", "Você é demais!", "Perfeito!", "Uau, que esperto!")

/**
 * O que está na tela do jogo agora, para o teste no emulador: as perguntas são sorteadas a cada partida,
 * então o teste lê aqui a atividade da vez (como a criança vê na tela) para jogar sem errar.
 */
object PartidaNaTela {
    @Volatile var pergunta: Pergunta? = null
        private set
    @Volatile var indice: Int = -1
        private set
    @Volatile var acertou: Boolean = false
        private set

    internal fun mostrar(pergunta: Pergunta, indice: Int, acertou: Boolean) {
        this.pergunta = pergunta
        this.indice = indice
        this.acertou = acertou
    }

    fun limpar() {
        pergunta = null
        indice = -1
        acertou = false
    }
}

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
    /**
     * Com [treino]: depois de cada acerto, se devolver true a partida termina e entrega os resultados em
     * [onConcluir] (o "Descobrir o nível" para sozinho; o treino sem fim não passa nada).
     */
    fimDoTreino: ((resultados: List<Boolean>) -> Boolean)? = null,
    /** Mostra a abertura do mundo (Corujinha Professora) antes da primeira fase dele ([Historia.introducao]). */
    comHistoria: Boolean = false,
) {
    // Abertura do mundo: a cena da história vem antes das perguntas (só na primeira fase do mundo).
    val abertura = remember(fase) { if (comHistoria && treino == null && perguntasProntas.isEmpty()) Historia.introducao(fase) else null }
    var historiaVista by remember(fase) { mutableStateOf(abertura == null) }
    if (!historiaVista && abertura != null) {
        CenaDaHistoria(fase, abertura) { historiaVista = true }
        return
    }
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val escopo = rememberCoroutineScope()
    // Na rodada facilitada, a primeira pergunta é um exemplo resolvido junto (a resposta brilha e não conta).
    // A rodada vale do começo ao fim da partida: ao vencer, o jogo grava o resultado e a fase deixa de ser
    // "facilitada" no estado; refazer a lista de perguntas nesse instante (6 viravam 5) fechava o app na vitória.
    val rodadaFacilitada = remember(fase) { facilitada }
    val perguntas = remember(fase) {
        val iniciais = when {
            treino != null -> listOf(treino(emptyList()))
            else -> {
                // Os temas favoritos do perfil deixam as figuras de contar e das letras mais do gosto da criança.
                val gerador = GeradorDeFases(temasFavoritos = perfil?.temasFavoritos.orEmpty())
                val normais = perguntasProntas.ifEmpty { gerador.perguntas(fase) }
                if (rodadaFacilitada) listOf(gerador.perguntas(fase, 1).first()) + normais else normais
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
    // Nunca além da última pergunta, mesmo que a lista mude durante a recomposição.
    val pergunta = perguntas[indice.coerceIn(0, perguntas.lastIndex)]
    val exemplo = rodadaFacilitada && indice == 0
    val cor = Color(fase.trilha.corArgb)
    val vibracao = LocalHapticFeedback.current
    SideEffect { PartidaNaTela.mostrar(pergunta, indice, acertou) }

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
        // Um elogio sim, outro não, com o nome da criança (de Perfil.nome; sem nome, a versão sem nome).
        // Com voz da família gravada (área dos pais), o elogio sai com a voz do pai ou da mãe.
        val elogio = if (indice % 2 == 0) br.com.casinhadosaber.dominio.VozesDaFamilia.elogio(perfil?.nome, indice / 2)
        else br.com.casinhadosaber.dominio.VozesDaFamilia.parte(elogios.random())
        val partes = listOf(elogio) + pergunta.reforco.map { br.com.casinhadosaber.dominio.ParteDaFala(it) }
        // Gravação da família no meio: ela pode durar até 8 s, mais que a estimativa pelo texto.
        val gravadas = narrador.familia.paraTocar()
        val comGravacao = partes.any { it.chave != null && it.chave in gravadas }
        narrador.falarPartes(partes)
        val caracteres = elogio.trecho.texto.length + pergunta.reforco.sumOf { it.texto.length }
        escopo.launch {
            if (comGravacao) {
                // Espera a fala com a gravação terminar (no máximo 9 s no total, para nunca travar a partida).
                delay(1200L)
                kotlinx.coroutines.withTimeoutOrNull(7_800L) {
                    androidx.compose.runtime.snapshotFlow { narrador.falando }.first { !it }
                }
            } else {
                delay((1200L + caracteres * 65L).coerceAtMost(5000L))
            }
            if (treino != null && fimDoTreino?.invoke(resultados) == true) {
                onConcluir(resultados)
            } else if (treino != null) {
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
        // Opção que não existe nesta pergunta (toque na pergunta que ainda está saindo da tela): ignora.
        if (acertou || i in erradas || i !in pergunta.opcoes.indices) return
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
                val incentivo = if (indice % 2 == 1) br.com.casinhadosaber.dominio.VozesDaFamilia.incentivo(perfil?.nome, indice / 2)
                else br.com.casinhadosaber.dominio.VozesDaFamilia.parte("Tente outra vez!")
                narrador.falarPartes(pergunta.opcoes[i].fala.map { br.com.casinhadosaber.dominio.ParteDaFala(it) } + incentivo)
            }
        }
    }

    // Treino: o ✖ e o voltar do Android terminam do mesmo jeito (guardam moedinhas e histórico), uma vez só
    // (durante a troca de tela esta tela ainda está composta e um segundo toque não pode registrar de novo).
    var treinoTerminado by remember { mutableStateOf(false) }
    fun terminarTreino() {
        if (treinoTerminado) return
        treinoTerminado = true
        onConcluir(resultados)
    }
    if (treino != null) BackHandler { terminarTreino() }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            // Cenário (céu em degradê e a ilha 3D do caminho; sem a arte, o cenário desenhado na cor do caminho),
            // opaco e parado: no jogo nada se mexe atrás das atividades (e a tela não é redesenhada à toa).
            .cenarioDoJogo(cor, fase.trilha)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        // As crianças grandes ao lado do balão, como na maquete 2; em telas baixas (celular pequeno, deitado), menores.
        val telaBaixa = maxHeight < 700.dp
        Column(Modifier.fillMaxSize().padding(12.dp)) {
            // barra de cima (por cima da criança: o pulo e o braço que acena nunca cobrem o ✖)
            Row(Modifier.zIndex(1f), verticalAlignment = Alignment.CenterVertically) {
                if (treino != null) {
                    BotaoRedondo("✖️", "Terminar o treino", { terminarTreino() }, tamanho = 48.dp)
                } else {
                    BotaoRedondo("✖️", "Sair da fase", onSair, tamanho = 48.dp)
                }
                // Contas 3D do progresso numa pílula (maquete 2), uma cor por estado (nunca vermelho: vermelho
                // parece erro): verde = acertou de primeira; azul = acertou com ajuda; amarela e maior = a da vez;
                // perolada = ainda vem. No treino (sem fim), só as últimas contas aparecem.
                val visiveis = if (treino != null) maxOf(0, indice - 5)..indice else perguntas.indices
                val contas = visiveis.map { i ->
                    when (comoFoi[i]) {
                        true -> CorDaConta.VERDE.id to 1f
                        false -> CorDaConta.AZUL.id to 1f
                        null -> if (i == indice) CorDaConta.AMARELA.id to 1.25f else CorDaConta.VAZIA to 0.9f
                    }
                }
                Box(Modifier.weight(1f).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                    PilulaDeContas(contas, tamanho = 24.dp)
                }
                BotaoRedondo("🔊", "Ouvir de novo", { narrador.falar(pergunta.fala) }, tamanho = 48.dp)
            }

            // guia
            Row(
                Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val deMexer = pergunta.atividade != Atividade.Escolha
                // A ponta do balão só aparece quando a criança está ao lado dele (sem ela, o balão não aponta para o nada).
                val comCrianca = perfil != null && (pergunta.cena != Cena.Nenhuma || deMexer)
                if (comCrianca) {
                    val altura = when {
                        deMexer -> if (telaBaixa) 76.dp else 96.dp
                        else -> if (telaBaixa) 100.dp else 132.dp
                    }
                    perfil?.let { CriancaEIrmao(it, pulo, acertou, humor, falando = narrador.falando, altura = altura) }
                }
                Spacer(Modifier.width(8.dp))
                BalaoDaPergunta(
                    pergunta.instrucao,
                    Modifier
                        .weight(1f)
                        .clickable(onClickLabel = "Ouvir de novo") { narrador.falar(pergunta.fala) },
                    cauda = if (comCrianca) LadoDaCauda.Esquerda else LadoDaCauda.Nenhum,
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
                        // Só a pergunta da vez responde: durante a troca, a que está saindo ainda aparece na tela.
                        onTocar = { opcao -> if (i == indice) responder(opcao) },
                        mostrarCerta = rodadaFacilitada && i == 0,
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
                        ajudaInicial = rodadaFacilitada && i == 0,
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
                                        perfil?.let { CriancaEIrmao(it, pulo, acertou, humor, falando = narrador.falando, altura = if (telaBaixa) 130.dp else 170.dp) }
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
                                CenaDaPergunta(p.cena, perfil, fase.trilha)
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

/**
 * A criança e, ao lado dela, o irmãozinho (quando está ligado na área dos pais). Os dois torcem juntos no
 * acerto (o irmão pula um tiquinho depois, para não parecer um boneco só) e o irmão fica pensativo no erro.
 */
@Composable
private fun CriancaEIrmao(
    perfil: Perfil,
    pulo: Int,
    acertou: Boolean,
    humor: Humor,
    falando: Boolean,
    altura: androidx.compose.ui.unit.Dp,
) {
    Row(verticalAlignment = Alignment.Bottom) {
        CriancaQuePula(perfil, pulo, acertou, humor, falando = falando, altura = altura)
        if (perfil.irmao.ligado) {
            val salto = remember { Animatable(0f) }
            LaunchedEffect(pulo) {
                if (pulo > 0) {
                    delay(140)
                    salto.animateTo(-12f, tween(120))
                    salto.animateTo(0f, spring(dampingRatio = 0.3f))
                }
            }
            val humorDoIrmao = when {
                acertou -> Humor.TORCENDO
                humor == Humor.PENSANDO -> Humor.PENSANDO
                else -> Humor.FELIZ
            }
            // Na maquete 2 o irmãozinho tem uns 80% da altura da irmã.
            Irmaozinho(
                perfil,
                Modifier
                    .height(altura * 0.8f)
                    .graphicsLayer { translationY = salto.value * density },
                humor = humorDoIrmao,
            )
        }
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
            // Pulo baixinho (14 dp): com o braço levantado, a criança não chega na barra de cima.
            salto.animateTo(-14f, tween(130))
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
private fun CenaDaPergunta(cena: Cena, perfil: Perfil?, trilha: Trilha) {
    when (cena) {
        Cena.Nenhuma -> Unit
        is Cena.Emojis -> BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val figuras = separarFiguras(cena.texto)
            val n = figuras.size
            // Várias figuras iguais (contar): numa cena 3D da ilha, como os patinhos na lagoa da maquete 2.
            val deContar = n >= 2 && figuras.none { it.isBlank() } && figuras.distinct().size == 1
            when {
                deContar -> FigurasNaCena(figuras, trilha, Modifier.fillMaxSize())
                // Uma figura só: grande, em pé na grama da ilha, pulsando.
                n == 1 -> {
                    val lado = minOf(maxWidth * 0.62f, maxHeight * 0.9f, 190.dp)
                    FiguraDaPergunta(figuras[0], lado, Modifier.pulsando())
                }
                else -> {
                    // Figuras diferentes, numa ordem que importa (lista, balança): a grade de antes, num cartão.
                    // Até 6: grade de 3 por linha (como num dado); mais que isso: 5 por linha (como os dedos da mão).
                    val porLinha = if (n <= 3) n else if (n <= 6) 3 else 5
                    val linhas = (n + porLinha - 1) / porLinha
                    val tamanho = minOf(maxWidth * 0.8f / porLinha, maxHeight * 0.7f / linhas, 150.dp)
                    Column(
                        Modifier
                            .cartaoFofo(cor = Color.White.copy(alpha = 0.9f), raio = 32.dp)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        for (linha in figuras.chunked(porLinha)) {
                            Row {
                                for (f in linha) {
                                    Box(Modifier.size(tamanho), contentAlignment = Alignment.Center) {
                                        FiguraDaPergunta(f, tamanho * 0.92f) {
                                            Text(f, fontSize = (tamanho.value * 0.75f).sp, lineHeight = (tamanho.value * 0.85f).sp)
                                        }
                                    }
                                }
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
                .cartaoFofo(raio = 24.dp, labio = Cores.LabioClaro)
                .padding(horizontal = 24.dp, vertical = 8.dp),
        )
        is Cena.Conta -> Conta(cena)
        is Cena.Grade -> Grade(cena)
        is Cena.MaterialDourado -> MaterialDourado(cena)
        is Cena.Labirinto -> Labirinto(cena, perfil)
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
                        FiguraDaPergunta(item, tamanho * 0.86f) {
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
                .cartaoFofo(cor = Color.White.copy(alpha = 0.9f), raio = 20.dp)
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
                        if (item == Cena.Grade.TAMPADA) {
                            Tampinha(lado)
                            continue
                        }
                        Box(
                            Modifier
                                .size(lado)
                                .then(if (pergunta) Modifier.pulsando() else Modifier)
                                .background(if (pergunta) Cores.Estrela else Color.White, RoundedCornerShape(12.dp))
                                .border(if (pergunta) 4.dp else 2.dp, if (pergunta) Color(0xFFE0A800) else Color(0xFFEDE6FF), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (item.isNotEmpty()) {
                                FiguraDaPergunta(item, lado * 0.86f) {
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
            .cartaoFofo(raio = 20.dp, labio = Cores.LabioClaro)
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        maxItemsInEachRow = 5,
    ) {
        for (i in 0 until total) {
            val saiu = i >= total - riscadas
            Box(contentAlignment = Alignment.Center) {
                FiguraDaPergunta(emoji, (tamanho.value * 1.15f).dp, Modifier.alpha(if (saiu) 0.3f else 1f)) {
                    Text(emoji, fontSize = tamanho)
                }
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

/**
 * Cores dos blocos das respostas (maquete 2: vermelho, azul, verde e amarelo), com o lábio de cada uma.
 * Perguntas de cor ("Toque na cor", "What color is it?") ficam com os blocos claros de antes: um bloco colorido
 * ali confundiria a resposta.
 */
private val CoresDasRespostas = listOf(
    Color(0xFFFF5A4E) to Color(0xFFC4342E),
    Color(0xFF3E8EF7) to Color(0xFF2366C4),
    Color(0xFF34C46A) to Color(0xFF1D9150),
    Color(0xFFFFB020) to Color(0xFFD08100),
)

private val PalavraCor = Regex("""\bcor(es)?\b""")

private fun respostasColoridas(pergunta: Pergunta): Boolean {
    if (pergunta.opcoes.any { it.visual is Visual.Cor }) return false
    val instrucao = pergunta.instrucao.lowercase()
    return !(PalavraCor.containsMatchIn(instrucao) || "color" in instrucao || "colour" in instrucao)
}

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
    val coloridas = remember(pergunta) { respostasColoridas(pergunta) }
    val corDe = { i: Int -> if (coloridas) CoresDasRespostas[i % CoresDasRespostas.size] else null }
    // Com a ajuda ligada (dois erros ou exemplo), as outras respostas ficam mais apagadas e a certa pulsa.
    val comAjuda = !acertou && (erradas.size >= 2 || mostrarCerta)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when {
            grupos -> BoxWithConstraints {
                // Altura do cartão = 3 linhas de figuras + margem; nunca um cartão enorme e vazio.
                val larguraCartao = (maxWidth - 20.dp) / 3
                val celula = minOf(larguraCartao / 2.3f, 64.dp)
                val alturaCartao = minOf(alturaGrande ?: 150.dp, celula * 3 + 36.dp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pergunta.opcoes.forEachIndexed { i, o ->
                    CartaoOpcao(o, i, pergunta, erradas, acertou, onTocar, Modifier.weight(1f), altura = alturaCartao, mostrarCerta = mostrarCerta, comoGrupo = true, corDoBloco = corDe(i), comAjuda = comAjuda)
                }
                }
            }
            n == 4 -> for (linha in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in linha * 2 until linha * 2 + 2) {
                        CartaoOpcao(pergunta.opcoes[i], i, pergunta, erradas, acertou, onTocar, Modifier.weight(1f), altura = alturaGrande?.let { (it - 10.dp) / 2 } ?: 112.dp, mostrarCerta = mostrarCerta, corDoBloco = corDe(i), comAjuda = comAjuda)
                    }
                }
            }
            else -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pergunta.opcoes.forEachIndexed { i, o ->
                    CartaoOpcao(o, i, pergunta, erradas, acertou, onTocar, Modifier.weight(1f), altura = alturaGrande ?: 130.dp, mostrarCerta = mostrarCerta, corDoBloco = corDe(i), comAjuda = comAjuda)
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
    /** Cor do bloco de brinquedo e do lábio dele; nulo: o bloco claro (perguntas de cor). */
    corDoBloco: Pair<Color, Color>? = null,
    /** A ajuda está ligada nesta pergunta (as outras respostas ficam mais apagadas). */
    comAjuda: Boolean = false,
) {
    val certa = acertou && i == pergunta.indiceCorreto
    val errada = i in erradas
    val ajuda = !acertou && (erradas.size >= 2 || mostrarCerta) && i == pergunta.indiceCorreto
    val apagada = acertou && !certa
    // Números e palavras ficam brancos com contorno em cima da cor viva; figuras, num tom clarinho da cor (com o
    // lábio na cor viva), para a figura aparecer bem.
    val deTexto = opcao.visual is Visual.Texto
    val corDoRepouso = when {
        corDoBloco == null -> Color.White
        deTexto -> corDoBloco.first
        else -> lerp(corDoBloco.first, Color.White, 0.62f)
    }
    val cor by animateColorAsState(
        when {
            certa -> Cores.Certo
            corDoBloco != null -> corDoRepouso
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
        corBase = when {
            certa -> Color(0xFF2A9452)
            corDoBloco != null -> corDoBloco.second
            else -> Color(0xFFD9CFF0)
        },
        descricao = descreverOpcao(opcao),
        modifier = modifier
            // Marca usada só pelos testes automáticos no emulador (não aparece para a criança).
            .testTag(if (i == pergunta.indiceCorreto) "opcao_certa" else "opcao")
            .then(if (ajuda) Modifier.pulsando() else Modifier)
            .graphicsLayer {
                rotationZ = tremida.value
                scaleX = pulo.value
                scaleY = pulo.value
                alpha = when {
                    apagada || errada -> 0.45f
                    comAjuda && !ajuda && corDoBloco != null -> 0.6f
                    else -> 1f
                }
            },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = altura)
                .padding(8.dp),
            contentAlignment = if (comoGrupo) Alignment.TopCenter else Alignment.Center,
        ) {
            VisualDaOpcao(opcao.visual, certa, altura, comoGrupo, sobreCor = corDoBloco != null && deTexto)
            if (certa || errada) {
                MarcaDaResposta(certa, Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

/** O certinho (branco, na resposta certa) ou o xis (vermelho, na errada), desenhados num selo redondo. */
@Composable
private fun MarcaDaResposta(certa: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier.size(26.dp)) {
        val r = size.minDimension / 2f
        drawCircle(if (certa) Color(0xFF2A9452) else Color.White, r)
        drawCircle(Color.White, r, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
        val traco = 3.5.dp.toPx()
        val tinta = if (certa) Color.White else Cores.Errado
        if (certa) {
            drawLine(tinta, Offset(r * 0.55f, r * 1.02f), Offset(r * 0.88f, r * 1.36f), traco, StrokeCap.Round)
            drawLine(tinta, Offset(r * 0.88f, r * 1.36f), Offset(r * 1.48f, r * 0.66f), traco, StrokeCap.Round)
        } else {
            drawLine(tinta, Offset(r * 0.62f, r * 0.62f), Offset(r * 1.38f, r * 1.38f), traco, StrokeCap.Round)
            drawLine(tinta, Offset(r * 1.38f, r * 0.62f), Offset(r * 0.62f, r * 1.38f), traco, StrokeCap.Round)
        }
    }
}

private fun descreverOpcao(o: Opcao): String = when (val v = o.visual) {
    is Visual.Texto -> v.texto
    is Visual.Emoji -> o.fala.joinToString(" ") { it.texto }.ifBlank { v.emoji }
    is Visual.Cor -> o.fala.joinToString(" ") { it.texto }.ifBlank { "Cor" }
    is Visual.Sombra -> "Sombra"
    is Visual.Seta -> o.fala.joinToString(" ") { it.texto }.ifBlank { "Seta" }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisualDaOpcao(
    v: Visual,
    certa: Boolean,
    altura: androidx.compose.ui.unit.Dp = 150.dp,
    comoGrupo: Boolean = false,
    /** Em cima de um bloco de cor viva: número branco com contorno escuro (`.contorno-escuro`). */
    sobreCor: Boolean = false,
) {
    when (v) {
        is Visual.Texto -> {
            // Palavras e valores maiores (CHAVE, R$ 10) diminuem para caber inteiros no cartão.
            val estilo = MaterialTheme.typography.displayLarge.let { e ->
                when (v.texto.length) {
                    in 0..3 -> e
                    4 -> e.copy(fontSize = 44.sp)
                    5 -> e.copy(fontSize = 36.sp)
                    else -> e.copy(fontSize = 30.sp)
                }
            }
            if (sobreCor || certa) {
                TextoComContorno(v.texto, estilo = estilo, corDoContorno = Cores.ContornoEscuro)
            } else {
                Text(v.texto, style = estilo, color = Cores.Texto, maxLines = 1)
            }
        }
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
                                        FiguraDaPergunta(f, celula * 0.94f) {
                                            Text(f, fontSize = (celula.value * 0.78f).sp, lineHeight = (celula.value * 0.9f).sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val base = minOf(altura.value * 0.42f, 100f)
                FiguraDaPergunta(v.emoji, (base * 1.3f * v.escala).dp) {
                    Text(v.emoji, fontSize = (base * v.escala).sp, lineHeight = (base * 1.12f * v.escala).sp)
                }
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
        is Visual.Seta -> SetaDesenhada(
            v.direcao,
            Modifier.size(minOf(altura * 0.62f, 84.dp)),
            cor = if (certa) Color.White else Color(0xFFFF9F43),
            corSombra = if (certa) Color(0xFF2A9452) else Color(0xFFD9772B),
        )
        is Visual.Cor -> Box(
            Modifier
                .size(76.dp)
                .background(Color(v.argb), CircleShape)
                .border(4.dp, Color(0x22000000), CircleShape),
        )
    }
}

/**
 * O balão com a instrução da pergunta. O alto-falante do começo é o ícone 3D de som (sem emoji de sistema); para a
 * acessibilidade e os testes no emulador o texto continua começando com "🔊" (o texto alternativo do ícone).
 */
@Composable
private fun BalaoDaPergunta(instrucao: String, modifier: Modifier = Modifier, cauda: LadoDaCauda = LadoDaCauda.Esquerda) {
    val texto = remember(instrucao) {
        buildAnnotatedString {
            appendInlineContent(ICONE_DO_SOM, "🔊")
            append(" ")
            append(instrucao)
        }
    }
    Box(
        modifier
            .balaoDeFala(cauda)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(texto, style = MaterialTheme.typography.titleMedium, inlineContent = IconeDoSomNoBalao)
    }
}

private const val ICONE_DO_SOM = "som"

/** O ícone 3D de som dentro do texto do balão (com o emoji de reserva, se a arte faltar). */
private val IconeDoSomNoBalao = mapOf(
    ICONE_DO_SOM to InlineTextContent(Placeholder(1.25.em, 1.1.em, PlaceholderVerticalAlign.TextCenter)) {
        IconeDeArteDaBarra(IconeDaBarra(IconesDaBarra.SOM_LIGADO), Modifier.fillMaxSize()) { Text("🔊") }
    },
)
