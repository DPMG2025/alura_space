package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import br.com.casinhadosaber.ui.componentes.caminhoEstrela
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.AcessorioMascote
import br.com.casinhadosaber.dominio.Crescimento
import br.com.casinhadosaber.dominio.EspecieMascote
import br.com.casinhadosaber.dominio.EstagioMascote
import br.com.casinhadosaber.dominio.Mascote
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.VeuDoModal
import br.com.casinhadosaber.ui.componentes.MascoteAnimado
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.componentes.ContasDeProgresso
import br.com.casinhadosaber.ui.componentes.CorDaConta
import br.com.casinhadosaber.ui.componentes.FiguraLeveDoEmoji
import br.com.casinhadosaber.ui.componentes.IconeDaBarra
import br.com.casinhadosaber.ui.componentes.IconeDeArteDaBarra
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.LabioDoVerde
import br.com.casinhadosaber.ui.componentes.PoseDoMascote
import br.com.casinhadosaber.ui.componentes.TextoDoBotao
import br.com.casinhadosaber.ui.componentes.VerdeDoPronto
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay

// Telas do mascote (sugestão 2): a escolha da primeira vez, o cantinho com os enfeites e a festa de crescer.
// O mascote nunca fica triste, doente ou com fome: só cresce e ganha enfeites com as fases vencidas.

/** Fundo escurecido que segura os toques (nada atrás dele responde enquanto ele está aberto). */
@Composable
private fun FundoDoAviso(conteudo: @Composable () -> Unit) {
    VeuDoModal { conteudo() }
}

@Composable
private fun CartaoDoAviso(modifier: Modifier = Modifier, conteudo: @Composable () -> Unit) {
    Box(
        modifier
            .padding(16.dp)
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            // Cartão do brinquedo: corpo claro em degradê, lábio lilás embaixo, borda branca e sombra macia.
            .cartaoFofo(cor = Cores.Fundo, raio = 32.dp, labio = Cores.LabioClaro, profundidade = 6.dp)
            .clip(RoundedCornerShape(32.dp))
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) { conteudo() }
}

/**
 * Primeira vez: a criança escolhe o amiguinho (cachorrinho, gatinho ou dragãozinho) e, se quiser, dá um nome.
 * Marcas de teste: "mascote_CACHORRINHO", "mascote_GATINHO", "mascote_DRAGAOZINHO" e "mascote_pronto".
 */
@Composable
fun EscolhaDoMascote(onEscolher: (EspecieMascote, String) -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var escolhida by remember { mutableStateOf<EspecieMascote?>(null) }
    var nome by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        narrador.falar("Escolha um amiguinho para morar na sua casa! Ele cresce junto com você.")
    }
    FundoDoAviso {
        CartaoDoAviso {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Escolha o seu amiguinho!", style = MaterialTheme.typography.headlineMedium, color = Cores.Texto, textAlign = TextAlign.Center)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (e in EspecieMascote.entries) {
                        val sel = escolhida == e
                        val escala by animateFloatAsState(if (sel) 1.06f else 1f, spring(dampingRatio = 0.45f), label = "cartao_mascote")
                        Column(
                            Modifier
                                .weight(1f)
                                .graphicsLayer { scaleX = escala; scaleY = escala }
                                .shadow(if (sel) 8.dp else 3.dp, RoundedCornerShape(24.dp))
                                .clip(RoundedCornerShape(24.dp))
                                .background(if (sel) Brush.verticalGradient(listOf(Color(0xFFFFF8D9), Color(0xFFFFE89A))) else Brush.verticalGradient(listOf(Color.White, Color(0xFFF6F2FF))))
                                .border(if (sel) 5.dp else 3.dp, if (sel) Cores.Estrela else Color(0xFFEDE6FF), RoundedCornerShape(24.dp))
                                .clickable(role = Role.Button) {
                                    escolhida = e
                                    sons.toque()
                                    narrador.falar("${e.nome}!")
                                }
                                .semantics { contentDescription = e.nome }
                                .testTag("mascote_${e.name}")
                                .padding(horizontal = 2.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // O bebê é desenhado pequeno dentro do quadrado dele: aqui ele aparece 1,5 vez maior,
                            // em cima de um tapetinho redondo.
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(0.92f)
                                    .drawBehind {
                                        drawOval(
                                            Color(0x1F6C63FF),
                                            Offset(size.width * 0.12f, size.height * 0.8f),
                                            Size(size.width * 0.76f, size.height * 0.16f),
                                        )
                                    },
                                contentAlignment = Alignment.BottomCenter,
                            ) {
                                MascoteAnimado(
                                    e,
                                    EstagioMascote.BEBE,
                                    pose = if (sel) PoseDoMascote.FELIZ else PoseDoMascote.PARADO,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = 1.5f
                                            scaleY = 1.5f
                                            transformOrigin = TransformOrigin(0.5f, 0.97f)
                                        },
                                    pulando = sel,
                                )
                            }
                            Text(e.nome, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, maxLines = 1, softWrap = false, fontSize = 13.sp)
                        }
                    }
                }
                if (escolhida != null) {
                    OutlinedTextField(
                        value = nome,
                        onValueChange = { nome = Mascote.limparNome(it) },
                        label = { Text("Nome do amiguinho (se quiser)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        textStyle = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                BotaoFofo(
                    onClick = {
                        val e = escolhida ?: return@BotaoFofo
                        sons.premio()
                        val quem = nome.trim().ifEmpty { e.nome }
                        narrador.falar("Oba! $quem vai morar com você! Cada fase nova faz $quem crescer.")
                        onEscolher(e, nome)
                    },
                    habilitado = escolhida != null,
                    // Sem escolha ainda o BotaoFofo clareia o próprio verde (e não um cinza que parece quebrado).
                    cor = VerdeDoPronto,
                    corBase = LabioDoVerde,
                    descricao = "Pronto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mascote_pronto")
                        .then(if (escolhida != null) Modifier.pulsando() else Modifier),
                ) {
                    TextoDoBotao("Pronto!", Modifier.fillMaxWidth().padding(vertical = 10.dp))
                }
            }
        }
    }
}

/**
 * O cantinho do mascote (tocar nele na casa): o mascote grande, quanto falta para crescer e os enfeites
 * já liberados para escolher. Os enfeites que faltam aparecem com cadeado e dizem quantas fases faltam.
 */
@Composable
fun CantinhoDoMascote(
    mascote: Mascote,
    fasesVencidas: Int,
    nomeDaCrianca: String?,
    onVestir: (AcessorioMascote?) -> Unit,
    onFechar: () -> Unit,
) {
    val especie = mascote.especie ?: return
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val estagio = Crescimento.estagio(fasesVencidas)
    val liberados = Crescimento.acessoriosLiberados(fasesVencidas)
    var fala by remember { mutableIntStateOf(0) }
    // Enquanto o mascote "fala" (logo ao abrir e a cada toque), a pose 3D é a de falar; depois, feliz.
    var falando by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { narrador.falar(Crescimento.fala(mascote, fasesVencidas, nomeDaCrianca, 0)) }
    LaunchedEffect(fala) {
        falando = true
        delay(1800)
        falando = false
    }
    FundoDoAviso {
        CartaoDoAviso {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(mascote.nomeParaMostrar, style = MaterialTheme.typography.headlineMedium, color = Cores.Texto, textAlign = TextAlign.Center)
                Text("${especie.nome} ${estagio.nome.lowercase()}", style = MaterialTheme.typography.titleMedium, color = Cores.TextoSuave)
                Box(
                    Modifier
                        .size(190.dp)
                        .clickable(role = Role.Button) {
                            fala++
                            sons.piu(fala)
                            narrador.falar(Crescimento.fala(mascote, fasesVencidas, nomeDaCrianca, fala))
                        },
                ) {
                    MascoteAnimado(
                        especie,
                        estagio,
                        Modifier.fillMaxSize(),
                        mascote.acessorioEmUso,
                        mascote.nomeParaMostrar,
                        pulando = true,
                        pose = if (falando) PoseDoMascote.FALANDO else PoseDoMascote.FELIZ,
                    )
                }
                // Até o próximo estágio: 5 contas que se enchem (o número fica para o adulto, embaixo).
                val faltam = Crescimento.faltamParaCrescer(fasesVencidas)
                val cheias = if (faltam == 0) 5 else (Crescimento.progressoNoEstagio(fasesVencidas) * 5).toInt().coerceIn(0, 4)
                // Nada de "Faltam 55 fases": as contas mostram o caminho; o número fica só na descrição (e na fala).
                ContasDeProgresso(
                    cheias,
                    5,
                    cor = CorDaConta.VERDE,
                    tamanho = 30.dp,
                    comAtual = faltam > 0,
                    descricao = when (faltam) {
                        0 -> "Já é campeão"
                        1 -> "Falta 1 fase nova para crescer"
                        else -> "Faltam $faltam fases novas para crescer"
                    },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (faltam == 0) {
                        IconeLeve("premio_trofeu", 30.dp) { Text("🏆", fontSize = 22.sp) }
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        if (faltam == 0) "Já é campeão!" else "Jogue fases novas para crescer!",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }
                Text("Enfeites", style = MaterialTheme.typography.titleLarge)
                // Enfeites em fileiras de 4 botões grandes.
                AcessorioMascote.entries.chunked(4).forEach { linha ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (a in linha) {
                            val livre = a in liberados
                            val usando = mascote.acessorio == a.name
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (usando) Color(0xFFFFF3C4) else if (livre) Color.White else Color(0xFFECE8F2))
                                    .border(if (usando) 4.dp else 2.dp, if (usando) Cores.Estrela else Color(0xFFEDE6FF), RoundedCornerShape(20.dp))
                                    .clickable(role = Role.Button) {
                                        if (livre) {
                                            sons.colocar()
                                            narrador.falar(if (usando) "Tirei ${a.nome.lowercase()}." else "${a.nome}!")
                                            onVestir(a)
                                        } else {
                                            sons.toque()
                                            val f = a.fasesMinimas - fasesVencidas
                                            narrador.falar(if (f == 1) "Esse enfeite chega com mais uma fase nova!" else "Esse enfeite chega com mais $f fases novas!")
                                        }
                                    }
                                    .semantics {
                                        contentDescription = if (livre) a.nome else "${a.nome}, ainda guardado, chega com ${a.fasesMinimas} fases"
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                if (livre) {
                                    FiguraLeveDoEmoji(a.emoji, 38.dp) { Text(a.emoji, fontSize = 30.sp) }
                                    // O nome encolhe até caber no cartão ("Cachecol"), sempre com folga dos dois lados.
                                    RotuloQueCabe(
                                        a.nome.substringBefore(' '),
                                        MaterialTheme.typography.labelLarge,
                                        Cores.Texto,
                                        Modifier.padding(horizontal = 6.dp),
                                    )
                                } else {
                                    // Ainda guardado: o presente 3D fechado com o cadeado do caminho, e uma barrinha que
                                    // enche com as fases vencidas (no lugar de "60", "120"... que a criança não lê).
                                    Box(Modifier.size(40.dp)) {
                                        IconeLeve("premio_presente_fechado", 38.dp, Modifier.align(Alignment.Center), alfa = 0.85f) {
                                            IconeDeArteDaBarra(IconeDaBarra(IconesDaBarra.PAIS), Modifier.size(36.dp)) { Text("🔒", fontSize = 30.sp) }
                                        }
                                        IconeLeve("icone_no_bloqueado", 20.dp, Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 2.dp)) { }
                                    }
                                    BarraDoEnfeiteGuardado(
                                        fasesVencidas.toFloat() / a.fasesMinimas.coerceAtLeast(1),
                                        Modifier.padding(top = 6.dp, bottom = 4.dp, start = 10.dp, end = 10.dp).fillMaxWidth().height(10.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                BotaoFofo(onClick = { sons.toque(); onFechar() }, cor = VerdeDoPronto, corBase = LabioDoVerde, descricao = "Fechar", modifier = Modifier.fillMaxWidth()) {
                    TextoDoBotao("Pronto!", Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }
            }
        }
    }
}

/**
 * Festa de crescimento na tela de prêmio: o mascote aparece no estágio novo, pulando, com confete, e o
 * narrador conta a novidade (e os enfeites novos, se vieram junto). Marca de teste: "festa_mascote".
 */
@Composable
fun FestaDoMascote(mascote: Mascote, fasesAntes: Int, fasesDepois: Int, onFechar: () -> Unit) {
    val especie = mascote.especie ?: return
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val cresceu = Crescimento.cresceu(fasesAntes, fasesDepois)
    val estagio = Crescimento.estagio(fasesDepois)
    val novos = Crescimento.acessoriosNovos(fasesAntes, fasesDepois)
    val quem = mascote.nomeParaMostrar
    var confete by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        sons.premio()
        confete++
        val partes = buildList {
            if (cresceu) add("Uau! $quem cresceu! Agora é ${estagio.nome.lowercase()}!")
            if (novos.isNotEmpty()) add("$quem ganhou ${novos.joinToString(" e ") { it.nome.lowercase() }}!")
        }
        narrador.falar(partes.joinToString(" "))
        delay(700)
        confete++
    }
    FundoDoAviso {
        CartaoDoAviso(Modifier.testTag("festa_mascote")) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (cresceu) "$quem cresceu!" else "Presente para $quem!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Cores.Texto,
                    textAlign = TextAlign.Center,
                )
                if (cresceu) {
                    Text("Agora é ${estagio.nome.lowercase()}! 🎉", style = MaterialTheme.typography.titleLarge, color = Cores.Roxo, textAlign = TextAlign.Center)
                }
                // Raios girando e uma explosão de estrelinhas atrás do mascote que cresceu.
                Box(Modifier.size(230.dp), contentAlignment = Alignment.Center) {
                    RaiosDeFesta(Modifier.matchParentSize())
                    MascoteAnimado(
                        especie,
                        estagio,
                        Modifier.size(210.dp),
                        novos.lastOrNull() ?: mascote.acessorioEmUso,
                        quem,
                        pulando = true,
                        pose = PoseDoMascote.FELIZ,
                    )
                }
                if (novos.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        for (a in novos) {
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .background(Color.White, CircleShape)
                                    .border(3.dp, Cores.Estrela, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) { Text(a.emoji, fontSize = 32.sp) }
                        }
                    }
                    Text("Enfeite novo! Escolha na casa.", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(4.dp))
                BotaoFofo(onClick = { sons.toque(); onFechar() }, cor = VerdeDoPronto, corBase = LabioDoVerde, descricao = "Oba!", modifier = Modifier.fillMaxWidth().pulsando()) {
                    TextoDoBotao("Oba!", Modifier.fillMaxWidth().padding(vertical = 10.dp))
                }
            }
        }
        Confete(confete)
    }
}

/** Barrinha de brinquedo (trilho lilás, enchimento verde com brilho) de quanto já foi até o enfeite chegar. */
@Composable
private fun BarraDoEnfeiteGuardado(fracao: Float, modifier: Modifier) {
    Canvas(modifier) {
        val r = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
        drawRoundRect(Color(0xFFDCD3EE), cornerRadius = r)
        val f = fracao.coerceIn(0f, 1f)
        if (f > 0f) {
            val w = maxOf(size.height, size.width * f)
            drawRoundRect(Color(0xFF5FBF4A), size = Size(w, size.height), cornerRadius = r)
            drawRoundRect(Color(0xFF8EE07A), topLeft = Offset(0f, 0f), size = Size(w, size.height * 0.55f), cornerRadius = r)
        }
        drawRoundRect(Color.White, cornerRadius = r, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
    }
}

/** Texto de uma linha que diminui a letra (até 60%) quando não cabe na largura, em vez de ser cortado. */
@Composable
private fun RotuloQueCabe(texto: String, estilo: TextStyle, cor: Color, modifier: Modifier = Modifier) {
    var escala by remember(texto, estilo) { mutableFloatStateOf(1f) }
    var pronto by remember(texto, estilo) { mutableStateOf(false) }
    Text(
        texto,
        style = estilo.copy(fontSize = estilo.fontSize * escala),
        color = cor,
        maxLines = 1,
        softWrap = false,
        textAlign = TextAlign.Center,
        modifier = modifier.drawWithContent { if (pronto) drawContent() },
        onTextLayout = { r ->
            if (r.didOverflowWidth && escala > 0.6f) escala *= 0.9f else pronto = true
        },
    )
}

/** Raios dourados que giram devagar, um brilho redondo e estrelinhas que pulsam, atrás do mascote da festa. */
@Composable
private fun RaiosDeFesta(modifier: Modifier) {
    val festa = rememberInfiniteTransition(label = "raios")
    val giro = festa.animateFloat(0f, 360f, infiniteRepeatable(tween(14_000, easing = LinearEasing)), label = "giro")
    val pulso = festa.animateFloat(0.85f, 1.1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulso")
    // Formas e degradês montados uma vez por tamanho; a cada quadro só giram e pulsam (numa camada própria).
    Spacer(
        modifier
            .graphicsLayer()
            .drawWithCache {
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.minDimension / 2f
                val raios = Path()
                val quantos = 14
                for (k in 0 until quantos) {
                    val a0 = (k * 2 * PI / quantos).toFloat()
                    val a1 = a0 + (PI / quantos).toFloat() * 0.75f
                    raios.moveTo(c.x, c.y)
                    raios.lineTo(c.x + cos(a0) * r * 1.1f, c.y + sin(a0) * r * 1.1f)
                    raios.lineTo(c.x + cos(a1) * r * 1.1f, c.y + sin(a1) * r * 1.1f)
                    raios.close()
                }
                val luzDosRaios = Brush.radialGradient(listOf(Color(0x99FFD54F), Color(0x00FFD54F)), c, r * 1.05f)
                // Explosão: um brilho redondo com borda de estrela (no tamanho do pulso 1, depois só escala).
                val burst = Path().apply {
                    val pontas = 16
                    for (i in 0 until pontas * 2) {
                        val a = (i * PI / pontas).toFloat()
                        val rr = if (i % 2 == 0) r * 0.62f else r * 0.5f
                        val p = Offset(c.x + cos(a) * rr, c.y + sin(a) * rr)
                        if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    }
                    close()
                }
                val luzDoBurst = Brush.radialGradient(listOf(Color(0xFFFFF6C8), Color(0xFFFFE27A)), c, r * 0.62f)
                val estrelas = List(6) { k ->
                    val a = (k * PI / 3).toFloat() + 0.3f
                    val d = r * (0.78f + 0.06f * (k % 2))
                    val centro = Offset(c.x + cos(a) * d, c.y + sin(a) * d)
                    centro to caminhoEstrela(centro, r * (if (k % 2 == 0) 0.07f else 0.05f))
                }
                onDrawBehind {
                    val g = giro.value
                    val pu = pulso.value
                    rotate(g, c) { drawPath(raios, luzDosRaios) }
                    // O burst gira bem devagar no sentido contrário (0,0087 rad por grau de giro).
                    rotate(-g * 0.0087f * 180f / PI.toFloat(), c) {
                        scale(pu, c) { drawPath(burst, luzDoBurst) }
                    }
                    drawCircle(Color.White.copy(alpha = 0.55f), r * 0.38f, c)
                    for ((k, estrela) in estrelas.withIndex()) {
                        scale(if (k % 2 == 0) pu else 1.95f - pu, estrela.first) { drawPath(estrela.second, Color(0xFFFFC93C)) }
                    }
                }
            },
    )
}
