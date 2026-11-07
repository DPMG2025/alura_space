package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Conquistas
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Mundo
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.EscalaDeTablet
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.Raios
import br.com.casinhadosaber.ui.componentes.cartaoFofo
import br.com.casinhadosaber.ui.componentes.ContasLeves
import br.com.casinhadosaber.ui.componentes.CorDaConta
import br.com.casinhadosaber.ui.componentes.EstrelasLeves
import br.com.casinhadosaber.ui.componentes.ProgressoVisivel
import br.com.casinhadosaber.ui.componentes.FiguraLeveDoEmoji
import br.com.casinhadosaber.ui.componentes.IconeDeTrilha
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.ceuDeBrinquedo
import br.com.casinhadosaber.ui.componentes.escurecer
import br.com.casinhadosaber.ui.componentes.pilula
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.translate

/** Altura de cada mundo no mapa (ilha redonda, nome e a barrinha das 10 fases). */
private val ALTURA_DO_MUNDO = 196.dp
private val TAMANHO_DA_ILHA = 104.dp
private val zigueZagueDoMapa = listOf(0.28f, 0.72f)

/** Como está um mundo do caminho para a criança. */
private class SituacaoDoMundo(val mundo: Mundo, val vencidas: Int, val completo: Boolean, val atual: Boolean, val alcancado: Boolean, val chefaoVencido: Boolean)

/**
 * A fase da vez no caminho: a primeira aberta e ainda não vencida (null com tudo vencido), começando do mundo
 * aberto pelo "Descobrir o nível" ([EstadoJogo.liberadaAte]), como `EstadoJogo.faseDaVez` no caminho.
 */
fun faseDaVez(estado: EstadoJogo, trilha: Trilha): Int? {
    val total = Mundos.todos.size * Mundos.FASES_POR_MUNDO
    val desde = estado.liberadaAte[trilha] ?: 0
    fun daVez(n: Int) = EstadoJogo.chave(trilha, n) !in estado.estrelas && estado.liberada(trilha, n)
    return (desde until total).firstOrNull(::daVez) ?: (0 until total).firstOrNull(::daVez)
}

/**
 * Mapa grande do caminho: os 25 mundos de uma vez, em zigue-zague. Os completos brilham com o diploma, o da vez
 * tem o alfinete 3D "você está aqui", e cada um mostra o chefão (a coroa: o ícone do chefão apagadinho até vencer, a
 * coroa 3D dourada depois) e quantas fases já foram, em contas. Nenhum número na tela (só na acessibilidade).
 */
@Composable
fun TelaMapa(
    trilha: Trilha,
    estado: EstadoJogo,
    onVoltar: () -> Unit,
    /** Abre o álbum de figurinhas na página deste mundo. */
    onAlbum: (Int) -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val daVez = faseDaVez(estado, trilha)
    val mundoDaVez = daVez?.let { it / Mundos.FASES_POR_MUNDO }
    val situacoes = remember(estado.estrelas, estado.liberadaAte, trilha) {
        Mundos.todos.map { m ->
            val vencidas = (m.primeiraFase..m.ultimaFase).count { EstadoJogo.chave(trilha, it) in estado.estrelas }
            SituacaoDoMundo(
                mundo = m,
                vencidas = vencidas,
                completo = Conquistas.mundoCompleto(estado.estrelas, trilha, m.indice),
                atual = m.indice == mundoDaVez,
                alcancado = mundoDaVez == null || m.indice <= mundoDaVez,
                chefaoVencido = EstadoJogo.chave(trilha, m.ultimaFase) in estado.estrelas,
            )
        }
    }
    val completos = situacoes.count { it.completo }
    val cor = Color(trilha.corArgb)
    val rolagem = rememberScrollState()
    val densidade = LocalDensity.current

    LaunchedEffect(trilha) {
        val m = mundoDaVez?.let { Mundos.todos[it] }
        narrador.falar(
            "Mapa do caminho ${trilha.titulo}! " +
                (if (m != null) "Você está no mundo ${m.numero}: ${m.nome}. " else "Você completou todos os mundos! ") +
                if (completos > 0) "Os mundos que brilham já estão completos." else "Complete um mundo para ele brilhar.",
        )
        // A rolagem para no começo de um mundo (o de antes do da vez, inteiro no alto): nenhuma ilha fica
        // cortada embaixo do cabeçalho, e o mundo da vez aparece logo abaixo dele.
        val alvo = with(densidade) { (ALTURA_DO_MUNDO * ((mundoDaVez ?: 24) - 1).coerceAtLeast(0)).roundToPx() }
        if (alvo > 0) rolagem.scrollTo(alvo)
    }

    EscalaDeTablet {
        // Céu de brinquedo parado (vetor): sem as estrelinhas animadas, que redesenhavam a tela inteira.
        Box(Modifier.fillMaxSize().ceuDeBrinquedo(comGrama = false)) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                BotaoRedondo("⬅️", "Voltar para o caminho", onVoltar, tamanho = 52.dp)
                Spacer(Modifier.width(8.dp))
                IconeDeTrilha(trilha, Modifier.size(40.dp))
                Spacer(Modifier.width(4.dp))
                Column(Modifier.weight(1f)) {
                    TituloQueCabe("Mapa: ${trilha.titulo}", estilo = MaterialTheme.typography.headlineSmall)
                    // As medalhas dos mundos completos numa pilulazinha: a medalha e cinco estrelas que acendem com os
                    // mundos completos (nenhum número: "2 de 25" fica só para a acessibilidade).
                    Row(
                        Modifier
                            .clearAndSetSemantics { contentDescription = "$completos de ${Mundos.todos.size} mundos completos" }
                            .pilula()
                            .padding(horizontal = 10.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconeLeve("tela_medalha", 22.dp) { Text("🏅", fontSize = 16.sp) }
                        Spacer(Modifier.width(4.dp))
                        EstrelasLeves(ProgressoVisivel.estrelasAcesas(completos, Mundos.todos.size, 5), total = 5, tamanho = 15.dp)
                    }
                }
                Spacer(Modifier.width(6.dp))
                BotaoRedondo("📒", "Álbum de figurinhas", { sons.toque(); onAlbum(mundoDaVez ?: 0) }, tamanho = 52.dp)
            }
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val largura = maxWidth
                Box(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rolagem)
                        .height(ALTURA_DO_MUNDO * situacoes.size + 24.dp),
                ) {
                    // A trilha pontilhada entre as ilhas (dourada até o mundo da vez).
                    Canvas(Modifier.fillMaxSize()) {
                        val pontos = situacoes.indices.map { i ->
                            Offset(largura.toPx() * zigueZagueDoMapa[i % 2], (ALTURA_DO_MUNDO * i + TAMANHO_DA_ILHA / 2 + 12.dp).toPx())
                        }
                        for (i in 0 until pontos.size - 1) {
                            val a = pontos[i]
                            val b = pontos[i + 1]
                            val caminho = Path().apply {
                                moveTo(a.x, a.y)
                                cubicTo(a.x, (a.y + b.y) / 2, b.x, (a.y + b.y) / 2, b.x, b.y)
                            }
                            val feito = situacoes[i + 1].alcancado
                            translate(top = 3.dp.toPx()) { drawPath(caminho, Cores.Sombra, style = Stroke(18.dp.toPx(), cap = StrokeCap.Round)) }
                            drawPath(caminho, Color(0x8CFFFFFF), style = Stroke(16.dp.toPx(), cap = StrokeCap.Round))
                            drawPath(
                                caminho,
                                if (feito) Cores.Estrela else Color(0x99FFFFFF),
                                style = Stroke(6.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(14.dp.toPx(), 10.dp.toPx()))),
                            )
                        }
                    }
                    for ((i, s) in situacoes.withIndex()) {
                        val x = largura * zigueZagueDoMapa[i % 2]
                        IlhaDoMundo(
                            s = s,
                            trilha = trilha,
                            modifier = Modifier.offset(x = x - LARGURA_DA_ILHA / 2, y = ALTURA_DO_MUNDO * i + 12.dp),
                            onClick = {
                                sons.toque()
                                val m = s.mundo
                                narrador.falar(
                                    "Mundo ${m.numero}: ${m.nome}. " + when {
                                        s.completo -> "Completo! Você ganhou o diploma."
                                        s.atual -> "Você está aqui! Já venceu ${s.vencidas} de ${Mundos.FASES_POR_MUNDO} fases."
                                        s.alcancado -> "Já venceu ${s.vencidas} de ${Mundos.FASES_POR_MUNDO} fases."
                                        else -> "Ainda vamos chegar lá!"
                                    },
                                )
                            },
                        )
                    }
                }
                // Esmaecido no alto: as ilhas que passam por baixo do cabeçalho somem macio, sem corte duro.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .background(Brush.verticalGradient(0f to CEU_DO_MAPA.copy(alpha = 0.75f), 0.4f to CEU_DO_MAPA.copy(alpha = 0.35f), 1f to CEU_DO_MAPA.copy(alpha = 0f))),
                )
            }
        }
        }
    }
}

/** Céu logo abaixo do cabeçalho (o esmaecido do alto usa esta cor). */
private val CEU_DO_MAPA = Color(0xFF8CCDFB)

/** A cor das contas de cada caminho no mapa (perto da cor do caminho). */
private fun corDasContas(trilha: Trilha): CorDaConta = when (trilha) {
    Trilha.MATEMATICA -> CorDaConta.LARANJA
    Trilha.PORTUGUES -> CorDaConta.VERDE
    Trilha.INGLES -> CorDaConta.ROXA
    Trilha.LOGICA -> CorDaConta.VERMELHA
}

private val LARGURA_DA_ILHA: Dp = 168.dp

/** Selo do cadeado dos mundos ainda fechados (lilás clarinho, diferente da coroa). */
private val SELO_FECHADO = Color(0xFFE6E0F5)

@Composable
private fun IlhaDoMundo(s: SituacaoDoMundo, trilha: Trilha, modifier: Modifier, onClick: () -> Unit) {
    val m = s.mundo
    Column(
        modifier
            .width(LARGURA_DA_ILHA)
            .testTag("mundo_${m.indice}")
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = "Mundo ${m.numero}, ${m.nome}" + when {
                    s.completo -> ", completo"
                    s.atual -> ", você está aqui"
                    else -> ""
                }
            }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(TAMANHO_DA_ILHA + 24.dp), contentAlignment = Alignment.Center) {
            if (s.completo) {
                // Brilho dos mundos completos
                Box(
                    Modifier
                        .size(TAMANHO_DA_ILHA + 24.dp)
                        .background(Brush.radialGradient(listOf(Color(0xCCFFE680), Color(0x00FFE680))), CircleShape),
                )
            }
            // Mundo ainda longe: a ilha fica clarinha (opaca, para a trilha não aparecer através dela).
            val corDaIlha = if (s.alcancado) Color(m.corArgb) else lerp(Color(m.corArgb), Color(0xFFF2F2F2), 0.6f)
            Box(
                Modifier
                    .size(TAMANHO_DA_ILHA)
                    .then(if (s.atual) Modifier.pulsando() else Modifier)
                    // Disco de brinquedo: sombra macia e lábio mais escuro embaixo (círculos, sem blur).
                    .drawBehind {
                        val r = size.minDimension / 2
                        drawCircle(Cores.Sombra, r, center.copy(y = center.y + 9.dp.toPx()))
                        drawCircle(escurecer(corDaIlha, 0.72f), r, center.copy(y = center.y + 5.dp.toPx()))
                    }
                    .background(Brush.verticalGradient(listOf(lerp(corDaIlha, Color.White, 0.35f), corDaIlha)), CircleShape)
                    // Brilho de verniz no alto à esquerda.
                    .drawBehind {
                        drawOval(
                            Color.White.copy(alpha = 0.35f),
                            Offset(size.width * 0.2f, size.height * 0.1f),
                            androidx.compose.ui.geometry.Size(size.width * 0.38f, size.height * 0.2f),
                        )
                    }
                    .border(
                        if (s.completo || s.atual) 5.dp else 3.dp,
                        when {
                            s.completo -> Cores.Estrela
                            s.atual -> Color.White
                            else -> Color(trilha.corEscuraArgb).copy(alpha = 0.5f)
                        },
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // A figura 3D do mundo (bitmap pequeno do cache, num Canvas: são 25 ilhas); sem modelo, o emoji.
                FiguraLeveDoEmoji(m.emoji, 64.dp, alfa = if (s.alcancado) 1f else 0.5f) {
                    Text(m.emoji, fontSize = 46.sp, modifier = Modifier.alpha(if (s.alcancado) 1f else 0.5f))
                }
            }
            // Selo do canto: mundo ainda fechado mostra o cadeado (num selo lilás, sem coroa); aberto mostra o
            // chefão (a 10ª fase), coroa apagadinha num selo branco tracejado até vencer e dourada depois.
            val dourado = s.chefaoVencido
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 6.dp)
                    .size(36.dp)
                    .drawBehind { drawCircle(Cores.Sombra, size.minDimension / 2, center.copy(y = center.y + 2.dp.toPx())) }
                    .background(
                        when {
                            !s.alcancado -> SELO_FECHADO
                            dourado -> Cores.Estrela
                            else -> Color.White
                        },
                        CircleShape,
                    )
                    .then(
                        when {
                            dourado -> Modifier.border(2.dp, Color.White, CircleShape)
                            s.alcancado -> Modifier.drawBehind {
                                val r = size.minDimension / 2 - 1.5.dp.toPx()
                                drawCircle(
                                    Cores.Estrela,
                                    r,
                                    style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))),
                                )
                            }
                            else -> Modifier.border(2.dp, Cores.Contorno, CircleShape)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Cadeado e coroa 3D: um bitmap pequeno de cada, dividido pelos 25 mundos.
                when {
                    !s.alcancado -> IconeLeve(IconesDaBarra.PAIS, 24.dp) { Text("🔒", fontSize = 17.sp) }
                    dourado -> IconeLeve("premio_coroa", 26.dp) { Text("👑", fontSize = 18.sp) }
                    else -> IconeLeve("icone_no_chefao", 26.dp, alfa = 0.55f) {
                        IconeLeve("premio_coroa", 26.dp, alfa = 0.4f) { Text("👑", fontSize = 18.sp, modifier = Modifier.alpha(0.4f)) }
                    }
                }
            }
            if (s.completo) {
                IconeLeve("tela_medalha", 40.dp, Modifier.align(Alignment.BottomStart).offset(x = 4.dp, y = 2.dp)) {
                    Text("🏅", fontSize = 30.sp)
                }
            }
            if (s.atual) {
                IconeLeve("icone_alfinete", 40.dp, Modifier.align(Alignment.TopStart).offset(x = 2.dp).pulsando()) {
                    Text("📍", fontSize = 34.sp)
                }
            }
        }
        Text(
            // Só o nome: o número do mundo fica na descrição ("Mundo 3, Praia").
            m.nome,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier
                .cartaoFofo(raio = Raios.Medio)
                .padding(horizontal = 10.dp, vertical = 2.dp),
        )
        // As 10 fases do mundo em contas 3D (as vencidas na cor do caminho), num Canvas só por mundo.
        ContasLeves(
            feitas = s.vencidas,
            total = Mundos.FASES_POR_MUNDO,
            tamanho = 13.dp,
            cor = corDasContas(trilha),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
