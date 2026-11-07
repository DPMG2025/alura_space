package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.Desenho
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Estrutura
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Passeio
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Lugar
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.TipoDeEvento
import br.com.casinhadosaber.ui.tema.Fredoka
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Altura da cena dividida pela largura. */
const val PROPORCAO_CENA = 1.15f

/** Medidas da casa em frações da cena (as mesmas do protótipo). */
private object Planta {
    const val ESQ = 0.10f
    const val DIR = 0.90f
    const val TOPO = 0.30f
    const val MEIO = 0.535f
    const val BASE = 0.78f
    const val DIVISAO_CIMA = 0.52f
    const val DIVISAO_BAIXO = 0.58f
}

/** Qual cômodo fica no ponto (x, y) da cena, para o modo de pintar. */
fun comodoEm(x: Float, y: Float): Comodo? {
    if (x !in Planta.ESQ..Planta.DIR) return null
    return when (y) {
        in Planta.TOPO..Planta.MEIO -> if (x < Planta.DIVISAO_CIMA) Comodo.QUARTO else Comodo.BANHEIRO
        in Planta.MEIO..Planta.BASE -> if (x < Planta.DIVISAO_BAIXO) Comodo.SALA else Comodo.COZINHA
        else -> null
    }
}

/**
 * A casinha completa: céu, terreno, estruturas, objetos e a criança.
 *
 * Quando [interativa] é verdadeiro, a criança pode tocar e arrastar os objetos.
 * Arrastar um objeto para baixo da cena devolve o objeto ao baú.
 *
 * Com [preencherAltura] numa tela alta, o jardim de baixo também faz parte da cena: as coisas
 * do jardim podem ser soltas lá, e as que a criança ainda não mexeu já começam lá.
 */
@Composable
fun CenaDaCasa(
    estado: EstadoJogo,
    modifier: Modifier = Modifier,
    interativa: Boolean = false,
    pintando: Boolean = false,
    novaEstrutura: Estrutura? = null,
    destaqueId: String? = null,
    puloDaCrianca: Int = 0,
    noite: Boolean = false,
    /** Uso interno: quem chama já desenhou o céu (modo de preencher a altura). */
    semCeu: Boolean = false,
    /** Uso interno: os visitantes passeiam no jardim de baixo, desenhados por quem chama. */
    semVisitantes: Boolean = false,
    /** Ocupa toda a altura dada: a casa fica embaixo e o céu continua para cima (sem faixas vazias). */
    preencherAltura: Boolean = false,
    onTocarItem: (ItemCasa) -> Unit = {},
    onMoverItem: (ItemCasa, Posicao) -> Unit = { _, _ -> },
    onGuardarItem: (ItemCasa) -> Unit = {},
    /** O objeto foi solto num lugar onde ele não mora (cama no jardim, árvore na sala) e voltou. */
    onRecusarItem: (ItemCasa) -> Unit = {},
    onPintar: (Comodo) -> Unit = {},
    onTocarCrianca: () -> Unit = {},
    onMoverCrianca: (Posicao) -> Unit = {},
    /** Toque no irmãozinho (ele pula; quem chama faz ele falar). */
    onTocarIrmao: () -> Unit = {},
    /** Toque num visitante da semana ou da loja (o emoji dele): ele pula, quem chama fala o nome. */
    onTocarVisitante: (String) -> Unit = {},
    /** Evento do dia: os enfeites ficam no fundo da cena (atrás das coisas), seguindo o beiral do telhado. */
    evento: TipoDeEvento? = null,
    /**
     * Tamanho da criança e do irmãozinho em relação ao normal. A tela inicial (maquete 1) põe os dois em primeiro
     * plano, com o dobro do tamanho; a casa de montar usa 1 (eles cabem nos cômodos).
     */
    escalaDasCriancas: Float = 1f,
) {
    val tempo = rememberInfiniteTransition(label = "cena")
    val t by tempo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "tempo",
    )
    val construcao = remember { Animatable(1f) }
    LaunchedEffect(novaEstrutura) {
        if (novaEstrutura != null) {
            construcao.snapTo(0f)
            construcao.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow))
        }
    }

    if (preencherAltura) {
        BoxWithConstraints(modifier.clipToBounds(), contentAlignment = Alignment.TopCenter) {
            val alturaCena = minOf(maxWidth * PROPORCAO_CENA, maxHeight)
            // A sobra de altura vai mais para o jardim (65%) do que para o céu (35%).
            val sobra = maxHeight - alturaCena
            val ceuExtra = sobra * 0.35f
            val jardim = sobra * 0.65f
            val temJardimExtra = jardim > 70.dp
            val corteDoChao = alturaCena * 0.22f + jardim
            val contexto = LocalContext.current
            val emCamadas = remember(contexto) { CamadasDaCasa.disponivel(ArteDoApp.manifesto(contexto)) }
            Canvas(Modifier.fillMaxSize()) {
                if (emCamadas) {
                    // Casa em camadas 3D: o chão da arte já traz o jardim de baixo (até meia largura abaixo da cena);
                    // aqui só o céu e a grama lisa da arte para o que sobrar embaixo e dos lados (tela larga), a partir
                    // da metade da cena, onde a grama da arte já cobre tudo.
                    val topoChao = (ceuExtra + alturaCena * 0.5f).toPx()
                    if (!semCeu) drawRect(Brush.verticalGradient(coresDoCeu(noite), endY = topoChao))
                    drawRect(if (noite) GramaDaArteDeNoite else GramaDaArte, Offset(0f, topoChao), Size(size.width, size.height - topoChao))
                    return@Canvas
                }
                if (!semCeu) drawRect(Brush.verticalGradient(coresDoCeu(noite), endY = size.height - corteDoChao.toPx()))
                // Grama: dos lados da casa e o jardim que continua para baixo, com tufinhos e flores.
                val chao = if (noite) listOf(Color(0xFF3F7A45), Color(0xFF2F5E35)) else listOf(Color(0xFF7CC56B), Color(0xFF4F9B44))
                val topoChao = size.height - corteDoChao.toPx()
                drawRect(Brush.verticalGradient(chao, startY = topoChao, endY = size.height), Offset(0f, topoChao), Size(size.width, corteDoChao.toPx()))
                val baseCena = ceuExtra.toPx() + alturaCena.toPx()
                val altoJardim = size.height - baseCena
                if (altoJardim > 40f) jardimDeBaixo(baseCena, altoJardim, noite, estado.temEstrutura(Estrutura.CAMINHO), estado.temEstrutura(Estrutura.TERRENO))
                for (i in 0 until 24) {
                    val x = ((i * 0.137f + 0.03f) % 1f) * size.width
                    val y = baseCena + ((i * 0.29f) % 1f) * (size.height - baseCena)
                    if (y < baseCena + 4f) continue
                    if (i % 4 == 0) {
                        drawCircle(Color(0xFFFFE066), 5f, Offset(x, y))
                        drawCircle(Color.White, 3f, Offset(x - 6f, y))
                        drawCircle(Color.White, 3f, Offset(x + 6f, y))
                    } else {
                        drawOval(if (noite) Color(0xFF356B3B) else Color(0xFF6BB85C), Offset(x - 14f, y - 4f), Size(28f, 8f))
                    }
                }
            }
            // A cena desce até o fim do jardim de baixo: os objetos podem morar lá também.
            CenaPlana(
                estado = estado,
                modifier = Modifier.padding(top = ceuExtra).height(if (temJardimExtra) alturaCena + jardim else alturaCena),
                interativa = interativa,
                pintando = pintando,
                novaEstrutura = novaEstrutura,
                destaqueId = destaqueId,
                puloDaCrianca = puloDaCrianca,
                noite = noite,
                semCeu = true,
                semVisitantes = temJardimExtra,
                jardimExtra = if (temJardimExtra) jardim / alturaCena else 0f,
                t = t,
                progresso = { construcao.value },
                onTocarItem = onTocarItem,
                onMoverItem = onMoverItem,
                onGuardarItem = onGuardarItem,
                onRecusarItem = onRecusarItem,
                onPintar = onPintar,
                onTocarCrianca = onTocarCrianca,
                onMoverCrianca = onMoverCrianca,
                onTocarIrmao = onTocarIrmao,
                onTocarVisitante = onTocarVisitante,
                evento = evento,
                escalaDasCriancas = escalaDasCriancas,
            )
            // Visitantes passeiam numa "pista" no alto do jardim de baixo, passando na frente dos objetos.
            if (temJardimExtra) {
                val relogio = rememberRelogioDoPasseio()
                val densidade = LocalDensity.current
                val larguraPx = with(densidade) { maxWidth.toPx() }
                val topoJardim = with(densidade) { (ceuExtra + alturaCena).toPx() }
                val alturaJardim = with(densidade) { jardim.toPx() }
                val tamanho = larguraPx * 0.08f
                estado.visitantes.forEachIndexed { i, bicho ->
                    // Pista acima do canteiro (que fica entre 30% e 70% da altura do jardim de baixo).
                    val pe = topoJardim + alturaJardim * (0.06f + 0.1f * (i % 2)) + tamanho * 1.2f
                    key(bicho) { VisitanteAndando(bicho, i, relogio, larguraPx, pe, tamanho, onTocarVisitante) }
                }
            }
        }
        return
    }
    CenaPlana(
        estado = estado,
        modifier = modifier,
        interativa = interativa,
        pintando = pintando,
        novaEstrutura = novaEstrutura,
        destaqueId = destaqueId,
        puloDaCrianca = puloDaCrianca,
        noite = noite,
        semCeu = semCeu,
        semVisitantes = semVisitantes,
        jardimExtra = 0f,
        t = t,
        progresso = { construcao.value },
        onTocarItem = onTocarItem,
        onMoverItem = onMoverItem,
        onGuardarItem = onGuardarItem,
        onRecusarItem = onRecusarItem,
        onPintar = onPintar,
        onTocarCrianca = onTocarCrianca,
        onMoverCrianca = onMoverCrianca,
        onTocarIrmao = onTocarIrmao,
        onTocarVisitante = onTocarVisitante,
        evento = evento,
        escalaDasCriancas = escalaDasCriancas,
    )
}

/**
 * Converte entre a posição guardada e a posição na tela, por causa do jardim de baixo.
 *
 * [extra] é a altura do jardim de baixo em frações da altura da cena (0 quando não há).
 * Na posição guardada, y de 1 a 2 é a fração do jardim de baixo (ver [ItemCasa]); na tela,
 * esse pedaço vai de 1 até 1 + [extra]. Sem jardim de baixo, quem estava lá aparece na grama da cena.
 */
private class JardimDeBaixo(val extra: Float) {
    /** Espaço útil (o pé não encosta na borda arredondada do cartão). */
    private val util = if (extra > 0.03f) extra - 0.015f else 0f

    /** O pé mais baixo que um objeto pode ter na tela. */
    val fundo get() = 1f + util

    /** Soltar abaixo disto guarda o objeto no baú. */
    val limiteDoBau get() = if (util > 0f) 1f + extra + 0.03f else 1.04f

    fun naTela(p: Posicao): Posicao {
        if (p.y <= 1f) return p
        val f = (p.y - 1f).coerceIn(0f, 1f)
        return Posicao(p.x, if (util > 0f) 1f + f * util else 0.84f + f * 0.15f)
    }

    fun paraGuardar(p: Posicao): Posicao =
        if (p.y <= 1f || util <= 0f) p else Posicao(p.x, 1f + ((p.y - 1f) / util).coerceIn(0f, 1f))

    fun limitar(p: Posicao) = Posicao(p.x.coerceIn(0.03f, 0.97f), p.y.coerceIn(0.08f, fundo))
}

/** A cena em si (céu, casa, objetos e criança), com o jardim de baixo opcional embaixo dela. */
@Composable
private fun CenaPlana(
    estado: EstadoJogo,
    modifier: Modifier,
    interativa: Boolean,
    pintando: Boolean,
    novaEstrutura: Estrutura?,
    destaqueId: String?,
    puloDaCrianca: Int,
    noite: Boolean,
    semCeu: Boolean,
    semVisitantes: Boolean,
    /** Altura do jardim de baixo em frações da altura da cena (0 quando não há). */
    jardimExtra: Float,
    t: Float,
    progresso: () -> Float,
    onTocarItem: (ItemCasa) -> Unit,
    onMoverItem: (ItemCasa, Posicao) -> Unit,
    onGuardarItem: (ItemCasa) -> Unit,
    onRecusarItem: (ItemCasa) -> Unit,
    onPintar: (Comodo) -> Unit,
    onTocarCrianca: () -> Unit,
    onMoverCrianca: (Posicao) -> Unit,
    onTocarVisitante: (String) -> Unit,
    onTocarIrmao: () -> Unit = {},
    evento: TipoDeEvento? = null,
    escalaDasCriancas: Float = 1f,
) {
    val jardim = remember(jardimExtra) { JardimDeBaixo(jardimExtra) }
    val relogio = rememberRelogioDoPasseio()
    // Jardim de baixo com pelo menos 20% da largura: as coisas de jardim ainda não arrastadas começam lá.
    val usarLugaresDoJardim = jardimExtra * PROPORCAO_CENA >= 0.2f
    BoxWithConstraints(
        modifier
            .aspectRatio(1f / (PROPORCAO_CENA * (1f + jardimExtra)))
            .clipToBounds(),
    ) {
        val larguraPx = constraints.maxWidth.toFloat()
        val alturaPx = larguraPx * PROPORCAO_CENA
        // Degradês da cena guardados (só mudam com o tamanho e com a noite), para não criar a cada quadro.
        val pinceis = remember(larguraPx, noite) { PinceisDaCasa(alturaPx, noite) }
        // A casa em camadas 3D (composta uma vez por aparência e tamanho); nula = desenho antigo, inteiro.
        val aparencia = AparenciaDaCasa.de(estado, noite)
        val casa3d = lembrarCasaEmCamadas(aparencia, estado.clima, novaEstrutura, larguraPx.roundToInt())
        val contexto = LocalContext.current

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { alturaPx.toDp() })
                .then(
                    if (interativa && pintando) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures { o ->
                                comodoEm(o.x / size.width, o.y / size.height)?.let(onPintar)
                            }
                        }
                    } else {
                        Modifier
                    },
                ),
        ) {
            if (casa3d != null) {
                desenharCasaEmCamadas(casa3d, estado, t, novaEstrutura, progresso(), noite, pinceis, semCeu)
            } else {
                desenharCasa(estado, t, novaEstrutura, progresso(), noite, pinceis, semCeu)
            }
        }

        // Enfeites do evento do dia: no fundo (atrás das coisas, da criança e dos botões), no beiral do telhado.
        if (evento != null) {
            val temTelhado = estado.temEstrutura(Estrutura.TELHADO)
            val beiral = remember(temTelhado, jardimExtra) {
                if (temTelhado) BeiralDaCasa(Offset(0.04f, 0.315f), Offset(0.5f, 0.075f), Offset(0.96f, 0.315f), cerca = 0.80f, alturaDaCena = 1f / (1f + jardimExtra)) else null
            }
            EnfeitesDoEvento(evento, Modifier.fillMaxSize(), beiral)
        }

        if (!estado.temEstrutura(Estrutura.TERRENO)) {
            val densidade = LocalDensity.current
            Text(
                text = "Minha casa\nvai ficar aqui!",
                modifier = Modifier
                    .offset { IntOffset((larguraPx * 0.36f).roundToInt(), (alturaPx * 0.625f).roundToInt()) }
                    .size(with(densidade) { (larguraPx * 0.28f).toDp() }, with(densidade) { (alturaPx * 0.085f).toDp() }),
                textAlign = TextAlign.Center,
                color = Color(0xFF6B3E1F),
                style = TextStyle(
                    fontFamily = Fredoka,
                    fontSize = with(densidade) { (larguraPx * 0.034f).toSp() },
                    lineHeight = with(densidade) { (larguraPx * 0.04f).toSp() },
                    fontWeight = FontWeight.ExtraBold,
                ),
            )
        }

        // Recusa (cama no jardim): a peça volta tremendo, com um balão "não" desenhado em cima dela.
        // Sugestão: o lugar certo brilha (forte quando a peça foi recusada, suave quando só foi para o
        // cômodo "errado", que vale, mas a dica ajuda).
        var vezes by remember { mutableIntStateOf(0) }
        var recusa by remember { mutableStateOf<Recusa?>(null) }
        var sugestao by remember { mutableStateOf<Sugestao?>(null) }
        LaunchedEffect(recusa?.vez) {
            if (recusa != null) {
                delay(DURACAO_DA_RECUSA)
                recusa = null
            }
        }
        sugestao?.let { s ->
            key(s.vez) {
                BrilhoNoLugar(s, larguraPx, alturaPx, jardimExtra) { if (sugestao?.vez == s.vez) sugestao = null }
            }
        }

        for (item in ItensDaCasa.ordem) {
            if (item.ehEstrutura) continue
            // À noite não tem arco-íris, pipa nem borboleta.
            if (noite && item.id in setOf("arcoiris", "pipa", "borboleta")) continue
            val guardada = estado.colocados[item.id] ?: continue
            val lugarNoJardim = ItensDaCasa.noJardimDeBaixo[item.id]
            val naoMexido = guardada == Posicao(item.x, item.y)
            val pos = jardim.naTela(if (usarLugaresDoJardim && lugarNoJardim != null && naoMexido) lugarNoJardim else guardada)
            // O cachorrinho e o gatinho passeiam em volta do lugar deles (menos dentro de casa, para não atravessar parede).
            val bicho = bichoDesenhadoDe(item.id)
            val semente = item.id.hashCode()
            val alcance = if (bicho == null) 0f else Passeio.alcance(pos.x, if (dentroDaCasa(pos, estado)) 0.035f else 0.08f)
            // Os bichos desenhados (corpo inteiro) ficam bem maiores que os emojis: lá fora quase o dobro,
            // dentro de casa um pouco menos, para caber ao lado do sofá.
            val escalaDoBicho = when {
                bicho == null -> 1f
                dentroDaCasa(pos, estado) -> 1.4f
                else -> 1.9f
            }
            // O item pela arte 3D (`item_<id>`, a lua de noite no lugar do sol); sem ela, o desenho antigo.
            val arte = ArteDoApp.info(contexto, CamadasDaCasa.idDoItem(item.id, noite))
            val passoDoBicho: (() -> br.com.casinhadosaber.dominio.Passo)? =
                if (bicho == null) null else ({ Passeio.em(relogio.value.toLong(), semente) })
            key(item.id) { ObjetoNaCena(
                item = item,
                pos = pos,
                larguraCena = larguraPx,
                alturaCena = alturaPx,
                larguraRelativa = item.tamanho * escalaDoBicho,
                interativo = interativa && !pintando,
                aparecer = item.id == destaqueId,
                recusas = recusa?.takeIf { it.item.id == item.id }?.vez ?: 0,
                vida = if (bicho != null) Vida.PARADO else vidaDo(item.id),
                tempo = t,
                noite = noite,
                deriva = if (bicho == null) null else ({ Passeio.em(relogio.value.toLong(), semente).deslocamento * alcance * larguraPx }),
                pularAoTocar = bicho != null,
                // A arte 3D já traz a sombra de contato (no que pisa no chão) e escurece de noite pelo filtro.
                sombra = arte == null && bicho == null && temSombraDeContato(item.id),
                escurecerDeNoite = arte == null,
                onTocar = { onTocarItem(item) },
                onSoltar = { nova ->
                    if (nova.y > jardim.limiteDoBau) {
                        onGuardarItem(item)
                        null
                    } else {
                        val destino = encaixar(item.id, jardim.limitar(nova), estado)
                        if (destino == null) {
                            vezes++
                            recusa = Recusa(item, pos, vezes)
                            lugarCertoDe(item)?.let { sugestao = Sugestao(it, forte = true, vez = vezes) }
                            onRecusarItem(item)
                            pos
                        } else {
                            val certo = item.comodo
                            val caiu = comodoEm(destino.x, destino.y - 0.02f)
                            if (certo != null && caiu != null && caiu != certo) {
                                vezes++
                                sugestao = Sugestao(LugarCerto.NoComodo(certo), forte = false, vez = vezes)
                            }
                            destino.also { onMoverItem(item, jardim.paraGuardar(it)) }
                        }
                    }
                },
            ) { w ->
                val antigo: @Composable () -> Unit = {
                    when {
                        bicho != null -> BichoDesenhado(bicho.first, bicho.second, { Passeio.em(relogio.value.toLong(), semente) })
                        noite && item.id == "sol" -> EmojiGrande("🌙", w)
                        else -> ConteudoDoItem(item, w)
                    }
                }
                if (arte != null) ItemDaArte(arte, w, noite, passoDoBicho, antigo) else antigo()
            } }
        }

        recusa?.let { r -> key(r.vez) { BalaoDeNao(r, larguraPx, alturaPx) } }

        // Visitantes comprados na loja passeiam pelo jardim, cada um no seu ritmo.
        if (!semVisitantes) estado.visitantes.forEachIndexed { i, bicho ->
            val y = 0.86f + (i % 3) * 0.045f
            key(bicho) { VisitanteAndando(bicho, i, relogio, larguraPx, y * alturaPx, larguraPx * 0.065f, onTocarVisitante) }
        }

        val perfil = estado.perfil
        if (perfil != null) {
            // Maiores (tela inicial), os bonecos ficam inteiros dentro da cena: nada cortado nas bordas.
            val posCrianca = jardim.naTela(estado.posicaoCrianca).let { p ->
                if (escalaDasCriancas > 1f) p.copy(x = dentroDaCena(p.x, LARGURA_DA_CRIANCA_NA_CASA * escalaDasCriancas)) else p
            }
            if (perfil.irmao.ligado) {
                // O irmãozinho fica sempre ao lado da criança (do lado que tiver espaço) e vai junto quando ela
                // muda de lugar. Arrastado, ele volta correndo para perto dela. Desenhado antes da criança, fica
                // um pouquinho atrás dela.
                val lado = if (posCrianca.x > 1f - 0.14f * escalaDasCriancas) -1f else 1f
                val xDoIrmao = posCrianca.x + lado * 0.1f * escalaDasCriancas
                val posIrmao = Posicao(
                    if (escalaDasCriancas > 1f) dentroDaCena(xDoIrmao, LARGURA_DO_IRMAO_NA_CASA * escalaDasCriancas) else xDoIrmao,
                    posCrianca.y - 0.004f,
                )
                key("irmao") {
                    ObjetoNaCena(
                        item = null,
                        pos = posIrmao,
                        larguraCena = larguraPx,
                        alturaCena = alturaPx,
                        larguraRelativa = LARGURA_DO_IRMAO_NA_CASA * escalaDasCriancas,
                        proporcao = 1f / PROPORCAO_IRMAO,
                        interativo = interativa && !pintando,
                        aparecer = false,
                        noite = noite,
                        tempo = t,
                        pularAoTocar = true,
                        descricao = perfil.irmao.nomeDeFrase,
                        onTocar = onTocarIrmao,
                        onSoltar = { posIrmao },
                    ) { _ ->
                        Irmaozinho(perfil, Modifier.fillMaxSize(), acenando = true)
                    }
                }
            }
            ObjetoNaCena(
                item = null,
                pos = posCrianca,
                larguraCena = larguraPx,
                alturaCena = alturaPx,
                larguraRelativa = LARGURA_DA_CRIANCA_NA_CASA * escalaDasCriancas,
                proporcao = 1f / PROPORCAO_CRIANCA,
                interativo = interativa && !pintando,
                aparecer = false,
                pulo = puloDaCrianca,
                noite = noite,
                tempo = t,
                descricao = perfil.nome.ifBlank { "Você" },
                onTocar = onTocarCrianca,
                onSoltar = { nova ->
                    val destino = encaixar("crianca", jardim.limitar(nova), estado) ?: posCrianca
                    onMoverCrianca(jardim.paraGuardar(destino))
                    destino
                },
            ) { _ ->
                Crianca(perfil, Modifier.fillMaxSize(), acenando = true)
            }
        }

        // Chuva ou neve da arte 3D: por cima de tudo (itens e crianças também), nunca dentro de casa.
        val chuva = casa3d?.clima
        if (chuva != null) {
            val paredes = estado.temEstrutura(Estrutura.PAREDES)
            val telhado = estado.temEstrutura(Estrutura.TELHADO)
            val velocidade = if (estado.clima == 1) 2f else 1f
            Canvas(Modifier.fillMaxSize()) { desenharClimaEmCamadas(chuva, t, velocidade, paredes, telhado, alturaPx) }
        }
    }
}

/** Largura da criança na cena da casa (fração da largura), no tamanho normal. */
private const val LARGURA_DA_CRIANCA_NA_CASA = 0.13f

/**
 * Largura do irmãozinho na cena da casa (fração da largura): dois terços da altura da criança, que tem 0,13 de
 * largura e proporção [PROPORCAO_CRIANCA].
 */
private const val LARGURA_DO_IRMAO_NA_CASA = LARGURA_DA_CRIANCA_NA_CASA / PROPORCAO_CRIANCA * 0.66f * PROPORCAO_IRMAO

/** O centro [x] (fração da largura) de um boneco de [largura] puxado para dentro da cena, sem passar das bordas. */
private fun dentroDaCena(x: Float, largura: Float): Float = x.coerceIn(largura / 2f, 1f - largura / 2f)

/** Pequenas animações que deixam a casa viva. */
enum class Vida { PARADO, BALANCAR, RESPIRAR, BOIAR, VOAR, DERIVAR, BRILHAR, DANCAR }

fun vidaDo(id: String): Vida = when (id) {
    "arvore", "flores", "planta", "girassol" -> Vida.BALANCAR
    "cachorro", "gato", "ursinho" -> Vida.RESPIRAR
    "pato" -> Vida.BOIAR
    "borboleta", "passarinho" -> Vida.VOAR
    "nuvem", "arcoiris" -> Vida.DERIVAR
    "estrela", "abajur", "sol" -> Vida.BRILHAR
    "pipa", "balao" -> Vida.DANCAR
    else -> Vida.PARADO
}

private fun androidx.compose.ui.graphics.GraphicsLayerScope.aplicarVida(
    vida: Vida,
    fase: Float,
    larguraCena: Float,
    alturaCena: Float,
    densidade: Float,
) {
    val a = fase * 2f * Math.PI.toFloat()
    val seno = kotlin.math.sin(a)
    when (vida) {
        Vida.PARADO -> Unit
        Vida.BALANCAR -> rotationZ = 3f * seno
        Vida.RESPIRAR -> scaleY *= 1f + 0.05f * kotlin.math.sin(a * 2f)
        Vida.BOIAR -> {
            translationY = 3f * densidade * seno
            rotationZ = 5f * kotlin.math.cos(a)
        }
        Vida.VOAR -> {
            translationX = 0.04f * larguraCena * seno
            translationY = 0.025f * alturaCena * kotlin.math.sin(a * 2f)
            scaleX *= 0.75f + 0.25f * kotlin.math.abs(kotlin.math.cos(a * 6f))
        }
        Vida.DERIVAR -> translationX = 0.03f * larguraCena * seno
        Vida.BRILHAR -> {
            scaleX *= 1f + 0.06f * seno
            scaleY *= 1f + 0.06f * seno
        }
        Vida.DANCAR -> {
            rotationZ = 10f * seno
            translationY = 4f * densidade * kotlin.math.cos(a * 2f)
        }
    }
}

/** Objetos que ficam na parede ou em cima de outro objeto: não caem até o chão. */
private val objetosDeParede = setOf("abajur", "quadro", "espelho", "livros", "bolo", "ursinho")

/** O que pisa no chão ganha sombra de contato (nada do céu, da parede, nem o que boia ou voa). */
private fun temSombraDeContato(id: String): Boolean =
    ItensDaCasa.lugarDe(id) != Lugar.CEU && id !in objetosDeParede &&
        vidaDo(id).let { it != Vida.BOIAR && it != Vida.VOAR && it != Vida.DERIVAR && it != Vida.DANCAR }

/** Está dentro das paredes da casa? */
fun dentroDaCasa(p: Posicao, estado: EstadoJogo): Boolean =
    estado.temEstrutura(Estrutura.PAREDES) && p.x in 0.11f..0.89f && p.y in 0.28f..0.80f

/**
 * Para onde vai um objeto solto em [p]. Móveis pousam no piso do cômodo; coisas do jardim, na grama.
 * Devolve null se o objeto não mora ali (a cama não vai para o jardim, a árvore não entra na sala).
 */
fun encaixar(id: String, p: Posicao, estado: EstadoJogo): Posicao? {
    val dentro = dentroDaCasa(p, estado)
    return when (ItensDaCasa.lugarDe(id)) {
        Lugar.CEU -> p
        Lugar.DENTRO -> when {
            !dentro -> null
            id in objetosDeParede -> desviarDasJanelas(id, p)
            else -> pousar(p, dentro = true)
        }
        Lugar.FORA -> if (dentro) null else pousar(p, dentro = false)
        Lugar.QUALQUER -> if (id in objetosDeParede) p else pousar(p, dentro)
    }
}

/** Janelas (x1, y1, x2, y2), em frações da cena. */
private val janelas = listOf(
    floatArrayOf(0.37f, 0.33f, 0.49f, 0.45f),
    floatArrayOf(0.43f, 0.565f, 0.55f, 0.68f),
    floatArrayOf(0.64f, 0.565f, 0.76f, 0.68f),
    floatArrayOf(0.78f, 0.35f, 0.86f, 0.43f),
)

/** Quadros, espelhos e livros não cobrem as janelas: são empurrados para o lado mais perto. */
private fun desviarDasJanelas(id: String, p: Posicao): Posicao {
    val meia = (ItensDaCasa.porId[id]?.tamanho ?: 0.06f) / 2
    val cy = p.y - meia
    for (j in janelas) {
        if (p.x + meia > j[0] && p.x - meia < j[2] && cy > j[1] && cy < j[3]) {
            val esquerda = j[0] - meia - 0.005f
            val direita = j[2] + meia + 0.005f
            return Posicao(if (p.x - esquerda < direita - p.x) esquerda else direita, p.y)
        }
    }
    return p
}

private fun pousar(p: Posicao, dentro: Boolean): Posicao {
    val y = when {
        dentro && p.y < 0.535f -> 0.528f
        dentro -> 0.775f
        p.y < 0.82f -> 0.84f
        else -> p.y
    }
    return Posicao(p.x, y)
}

/** Um objeto posicionado pelo pé, que pode ser tocado e arrastado (também nas áreas novas, [CenaDaArea]). */
@Composable
internal fun ObjetoNaCena(
    item: ItemCasa?,
    pos: Posicao,
    larguraCena: Float,
    alturaCena: Float,
    interativo: Boolean,
    aparecer: Boolean,
    onTocar: () -> Unit,
    /** Muda (aumenta) cada vez que o objeto é recusado: ele treme ao chegar de volta. */
    recusas: Int = 0,
    /** Recebe onde o objeto foi solto e devolve onde ele fica (ou null se foi guardado no baú). */
    onSoltar: (Posicao) -> Posicao?,
    larguraRelativa: Float = item?.tamanho ?: 0.1f,
    proporcao: Float = proporcaoDoItem(item),
    pulo: Int = 0,
    vida: Vida = Vida.PARADO,
    tempo: Float = 0f,
    noite: Boolean = false,
    descricao: String = item?.nome ?: "",
    /**
     * Passeio (bichinhos): quanto o objeto está deslocado para o lado agora, em pixels. Lido só na hora de
     * posicionar, então o passeio não recompõe a cena. O toque e o arrasto acompanham o bicho.
     */
    deriva: (() -> Float)? = null,
    /** Bichinhos dão um pulinho de alegria quando a criança toca neles. */
    pularAoTocar: Boolean = false,
    /** Sombra de contato (uma elipse suave embaixo do pé), para o objeto não "flutuar" no chão. */
    sombra: Boolean = false,
    /** Inclinação fixa do objeto, em graus (escolhida uma vez por objeto, nunca sorteada a cada quadro). */
    inclinacao: Float = 0f,
    /** Pequeno desvio para o lado, em pixels, também fixo por objeto (quebra a cara de grade). */
    desvioX: Float = 0f,
    /**
     * De noite, azular o conteúdo numa camada própria (que recorta na caixa). false para quem escurece sozinho e
     * desenha além da caixa (os itens da arte 3D, com a sombra e a folga deles).
     */
    escurecerDeNoite: Boolean = true,
    conteudo: @Composable (larguraPx: Float) -> Unit,
) {
    val faisca = remember { Animatable(1f) }
    val sons = br.com.casinhadosaber.ui.LocalSons.current
    val vibracao = androidx.compose.ui.platform.LocalHapticFeedback.current
    // Cada objeto começa a animação num ponto diferente, para não mexerem todos juntos.
    val defasagem = remember(item?.id) { ((item?.id?.hashCode() ?: 7) and 0xFF) / 255f }
    val densidade = LocalDensity.current
    val w = larguraRelativa * larguraCena
    val h = w * proporcao
    var arrasto by remember { mutableStateOf(Offset.Zero) }
    var arrastando by remember { mutableStateOf(false) }
    val escala = remember { Animatable(if (aparecer) 0f else 1f) }
    val salto = remember { Animatable(0f) }
    val tremor = remember { Animatable(0f) }
    val escopo = rememberCoroutineScope()

    LaunchedEffect(recusas) {
        if (recusas > 0) {
            // Espera a volta (a mola) quase terminar e balança a cabeça: "não".
            delay(320)
            val forca = with(densidade) { 9.dp.toPx() }
            for (f in listOf(1f, -1f, 0.8f, -0.8f, 0.5f, -0.5f, 0f)) tremor.animateTo(forca * f, tween(55))
        }
    }

    LaunchedEffect(aparecer) {
        if (aparecer) {
            escala.snapTo(0f)
            escala.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow))
        }
    }
    LaunchedEffect(pulo) {
        if (pulo > 0) {
            salto.animateTo(-h * 0.25f, tween(160))
            salto.animateTo(0f, spring(dampingRatio = 0.4f))
        }
    }

    // Área de toque de pelo menos 48dp, mesmo para objetos pequenos (dedos de criança).
    val minimo = with(densidade) { 48.dp.toPx() }
    val toqueW = maxOf(w, minimo)
    val toqueH = maxOf(h, minimo)
    val x = pos.x * larguraCena - toqueW / 2 + arrasto.x
    val y = pos.y * alturaCena - toqueH + arrasto.y
    Box(
        Modifier
            .offset { IntOffset((x + if (arrastando) 0f else deriva?.invoke() ?: 0f).roundToInt(), (y + salto.value).roundToInt()) }
            .size(with(densidade) { toqueW.toDp() }, with(densidade) { toqueH.toDp() })
            .zIndex(if (arrastando) 10f else 0f)
            .graphicsLayer {
                val s = escala.value * if (arrastando) 1.15f else 1f
                scaleX = s
                scaleY = s
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                if (!arrastando) aplicarVida(vida, (tempo + defasagem) % 1f, larguraCena, alturaCena, densidade.density)
                if (!arrastando) {
                    rotationZ += inclinacao
                    translationX += desvioX
                }
                translationX += tremor.value
            }
            .then(
                if (sombra) {
                    Modifier.drawBehind {
                        // A sombra fica no chão quando o objeto pula (o salto sobe o objeto, não a sombra).
                        val altura = (-salto.value).coerceAtLeast(0f)
                        val encolhe = 1f / (1f + altura / (h * 0.6f))
                        val largura = w * 0.78f * encolhe * (if (arrastando) 0.8f else 1f)
                        val alto = maxOf(w * 0.13f, 3f) * encolhe
                        val pe = size.height + altura - alto * 0.18f
                        val forca = if (arrastando) 0.5f else 1f
                        drawOval(SombraDeContato.copy(alpha = 0.12f * forca * encolhe), Offset(size.width / 2 - largura * 0.62f, pe - alto * 0.62f), Size(largura * 1.24f, alto * 1.24f))
                        drawOval(SombraDeContato.copy(alpha = 0.2f * forca * encolhe), Offset(size.width / 2 - largura / 2, pe - alto / 2), Size(largura, alto))
                    }
                } else {
                    Modifier
                },
            )
            .semantics {
                contentDescription = descricao
                role = Role.Button
            }
            .then(
                if (interativo) {
                    Modifier
                        .pointerInput(pos) {
                            detectTapGestures {
                                onTocar()
                                if (pularAoTocar) escopo.launch {
                                    salto.animateTo(-h * 0.45f, tween(170))
                                    salto.animateTo(0f, spring(dampingRatio = 0.35f))
                                }
                                escopo.launch {
                                    faisca.snapTo(0f)
                                    faisca.animateTo(1f, tween(700))
                                }
                                escopo.launch {
                                    escala.animateTo(1.25f, tween(90))
                                    escala.animateTo(1f, spring(dampingRatio = 0.3f))
                                }
                            }
                        }
                        .pointerInput(pos) {
                            detectDragGestures(
                                onDragStart = {
                                    // O bicho que passeava fica parado onde estava, embaixo do dedo.
                                    deriva?.let { arrasto += Offset(it(), 0f) }
                                    arrastando = true
                                    sons.pegar()
                                    vibracao.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                },
                                onDragEnd = {
                                    arrastando = false
                                    val solto = Posicao(pos.x + arrasto.x / larguraCena, pos.y + arrasto.y / alturaCena)
                                    val final = onSoltar(solto)
                                    if (final == null) {
                                        arrasto = Offset.Zero
                                    } else {
                                        sons.pousar()
                                        vibracao.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        // O objeto "cai" de onde foi solto até o chão do cômodo, com um quique.
                                        val diferenca = Offset((solto.x - final.x) * larguraCena, (solto.y - final.y) * alturaCena)
                                        arrasto = diferenca
                                        escopo.launch {
                                            animate(0f, 1f, animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMediumLow)) { f, _ ->
                                                arrasto = diferenca * (1f - f)
                                            }
                                        }
                                    }
                                },
                                onDragCancel = {
                                    arrastando = false
                                    arrasto = Offset.Zero
                                },
                            ) { mudanca, delta ->
                                mudanca.consume()
                                arrasto += delta
                            }
                        }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            Modifier
                .size(with(densidade) { w.toDp() }, with(densidade) { h.toDp() })
                .then(
                    if (escurecerDeNoite) {
                        Modifier
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                            .drawWithContent {
                                drawContent()
                                // À noite tudo fica azulado, menos o que brilha (as janelas, desenhadas na cena).
                                if (noite) drawRect(Color(0x70142045), blendMode = BlendMode.SrcAtop)
                            }
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            conteudo(w)
        }
        if (faisca.value < 1f) {
            Canvas(Modifier.matchParentSize()) {
                val p = faisca.value
                val centro = Offset(size.width / 2, size.height / 2)
                val raio = size.minDimension * (0.4f + 0.6f * p)
                for (k in 0 until 6) {
                    val ang = k * (Math.PI.toFloat() / 3f) + p
                    val c = Offset(centro.x + kotlin.math.cos(ang) * raio, centro.y + kotlin.math.sin(ang) * raio)
                    drawPath(caminhoEstrela(c, 5.dp.toPx() * (1f - p * 0.5f)), Color(0xFFFFD84D).copy(alpha = 1f - p))
                }
            }
        }
    }
}

/** Cor das sombras de contato (um roxo bem escuro, mais macio que o preto puro). */
private val SombraDeContato = Color(0xFF2B1D3A)

fun proporcaoDoItem(item: ItemCasa?): Float = item?.let { ItensDaCasa.proporcaoDe(it) } ?: 1f

/** Desenha o item: emoji ou móvel desenhado. Também é usado no baú. */
@Composable
fun ConteudoDoItem(item: ItemCasa, larguraPx: Float, modifier: Modifier = Modifier) {
    val desenho = item.desenho
    val bicho = bichoDesenhadoDe(item.id)
    if (bicho != null) {
        // O cachorrinho e o gatinho de corpo inteiro também no baú e no prêmio (parados, abanando o rabo).
        BichoDesenhado(bicho.first, bicho.second, { PARADO_NO_BAU }, modifier)
    } else {
        val antigo: @Composable () -> Unit = {
            if (desenho != null) {
                Canvas(modifier.fillMaxSize()) { desenharMovel(desenho) }
            } else {
                EmojiGrande(item.icone, larguraPx, modifier)
            }
        }
        // No baú e no prêmio também a arte 3D do item (a imagem inteira cabe na caixa); sem ela, o desenho antigo.
        val arte = ArteDoApp.info(LocalContext.current, CamadasDaCasa.idDoItem(item.id, noite = false))
        if (arte != null) ImagemDeArte(arte.id, modifier.fillMaxSize(), reserva = antigo) else antigo()
    }
}

/** Ícone de um item dentro de um quadrado de lado [lado] (baú, prêmio), sem distorcer os móveis desenhados. */
@Composable
fun IconeDoItem(item: ItemCasa, lado: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    val proporcao = proporcaoDoItem(item)
    val largura = if (proporcao > 1f) lado / proporcao else lado
    val larguraPx = with(LocalDensity.current) { largura.toPx() }
    Box(modifier.size(lado), contentAlignment = Alignment.Center) {
        Box(Modifier.size(largura, largura * proporcao), contentAlignment = Alignment.Center) {
            ConteudoDoItem(item, larguraPx)
        }
    }
}

@Composable
fun EmojiGrande(emoji: String, larguraPx: Float, modifier: Modifier = Modifier) {
    val tamanho = with(LocalDensity.current) { (larguraPx * 0.84f).toSp() }
    Text(
        text = emoji,
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontFamily = Fredoka,
            fontSize = tamanho,
            lineHeight = tamanho * 1.05f,
            fontWeight = FontWeight.Normal,
            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
        ),
        maxLines = 1,
        softWrap = false,
    )
}

// ---------------------------------------------------------------------------------------- desenho

private val CorMadeira = Color(0xFFC98B5A)
private val CorMoldura = Color(0xFFE8C9A0)
private val CorVidro = Color(0xFFA9E1FF)
private val CorLuz = Color(0xFFFFE08A)

/** Cores do céu (forte no alto, clarinho no horizonte), iguais na cena e na faixa de céu de cima. */
private fun coresDoCeu(noite: Boolean) =
    if (noite) listOf(Color(0xFF0E1638), Color(0xFF22306A), Color(0xFF3B4C8C)) else listOf(Color(0xFF7CC4FA), Color(0xFFB8E2FF), Color(0xFFFFE6D6))

/** A grama lisa da arte 3D (a última linha do chão), para continuar o jardim de baixo nas telas muito altas. */
private val GramaDaArte = Color(0xFFA9E56F)
private val GramaDaArteDeNoite = Color(CamadasDaCasa.multiplicar(0xFFA9E56F, CamadasDaCasa.NOITE))

/**
 * A casa em camadas 3D ([CasaEmCamadas]): céu, estrelas que piscam, nuvens que passeiam, a nuvem de chuva, a pilha
 * da casa composta (ou partida em três enquanto a estrutura [nova] cai), a fumaça da chaminé e o contorno pontilhado
 * do terreno vazio. Os itens, as crianças e o clima que cai vêm por cima, fora daqui.
 */
private fun DrawScope.desenharCasaEmCamadas(
    c: CasaEmCamadas,
    e: EstadoJogo,
    t: Float,
    nova: Estrutura?,
    progresso: Float,
    noite: Boolean,
    pinceis: PinceisDaCasa,
    semCeu: Boolean,
) {
    val W = size.width
    val H = size.height
    fun X(v: Float) = v * W
    fun Y(v: Float) = v * H
    val volta = 2f * Math.PI.toFloat()

    if (!semCeu) drawRect(pinceis.ceu)
    // As duas camadas de estrelas se revezam: umas acendem enquanto as outras apagam.
    if (c.estrelas.isNotEmpty()) {
        val s = kotlin.math.sin(t * volta)
        c.estrelas.forEachIndexed { i, img -> camadaDaCena(img, alfa = 0.6f + 0.4f * (if (i % 2 == 0) s else -s)) }
    }
    if (!noite) {
        // Nuvens de enfeite indo e voltando pela metade direita do céu (o sol e o arco-íris ficam à esquerda).
        c.nuvens.take(2).forEachIndexed { k, img ->
            val x = 0.62f + 0.14f * (1f + kotlin.math.sin((t + k * 0.5f) * volta))
            imagemEm(img, X(x - 0.07f), Y(0.07f + k * 0.06f), X(0.15f - k * 0.03f), alfa = 0.92f)
        }
        // Passarinhos de enfeite (a arte 3D não tem): os mesmos risquinhos de antes.
        passarinhos(t)
    }
    c.nuvemDeChuva?.let {
        imagemEm(it, X(0.56f), Y(0.03f), X(0.36f))
        imagemEm(it, X(0.02f), Y(0.07f), X(0.28f))
    }

    val partida = c.nova
    if (partida != null && nova != null && progresso < 1f) {
        // Estrutura nova caindo do céu com um quique, entre o que fica atrás e o que fica na frente dela.
        partida.abaixo?.let { camadaDaCena(it) }
        partida.nova?.let { camadaDaCena(it, dy = -(1f - progresso) * H * 0.25f) }
        partida.acima?.let { camadaDaCena(it) }
    } else {
        camadaDaCena(c.casa)
    }

    // Fumaça da chaminé (a arte 3D não tem): bolinhas brancas subindo, como antes.
    if (e.temEstrutura(Estrutura.TELHADO) && e.temEstrutura(Estrutura.CHAMINE)) {
        for (i in 0 until 3) {
            val fase = (t + i / 3f) % 1f
            drawCircle(
                Color.White.copy(alpha = 0.85f * (1f - fase)),
                radius = X(0.018f + 0.025f * fase),
                center = Offset(X(0.715f + 0.08f * fase), Y(0.095f - 0.09f * fase)),
            )
        }
    }

    // Terreno ainda vazio: o contorno pontilhado da casa que vai nascer ali.
    if (!e.temEstrutura(Estrutura.PAREDES)) {
        val tracejado = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(X(0.025f), X(0.018f)))
        val contorno = Path().apply {
            moveTo(X(Planta.ESQ), Y(Planta.BASE)); lineTo(X(Planta.ESQ), Y(Planta.TOPO)); lineTo(X(0.04f), Y(0.315f))
            lineTo(X(0.5f), Y(0.075f)); lineTo(X(0.96f), Y(0.315f)); lineTo(X(Planta.DIR), Y(Planta.TOPO)); lineTo(X(Planta.DIR), Y(Planta.BASE))
        }
        drawPath(contorno, Color.White.copy(alpha = 0.75f), style = Stroke(X(0.008f), pathEffect = tracejado, join = StrokeJoin.Round))
    }
}

/**
 * Três passarinhos de enfeite (risquinhos) atravessando o céu, cada um no seu tempo. Nunca são cortados pela
 * borda da cena: voam só entre as margens (a asa inteira dentro) e aparecem e somem devagar perto delas.
 */
private fun DrawScope.passarinhos(t: Float) {
    val W = size.width
    val H = size.height
    val asa = W * 0.012f
    val margem = asa * 2f
    for (k in 0 until 3) {
        val p = (t + k * 0.31f) % 1f
        val bx = margem + p * (W - 2f * margem)
        val by = H * (0.22f + k * 0.03f) + kotlin.math.sin((t * 6f + k) * Math.PI.toFloat()) * W * 0.006f
        val alfa = alfaDoPassarinho(p)
        if (alfa <= 0f) continue
        val cor = Color(0xFF3D2C5E).copy(alpha = alfa)
        drawLine(cor, Offset(bx - asa, by - asa * 0.5f), Offset(bx, by), 3f)
        drawLine(cor, Offset(bx, by), Offset(bx + asa, by - asa * 0.5f), 3f)
    }
}

/** Transparência do passarinho em [p] (0 a 1 da travessia): some nos 10% de cada ponta. */
private fun alfaDoPassarinho(p: Float): Float = (minOf(p, 1f - p) / 0.1f).coerceIn(0f, 1f)

/**
 * A chuva ou a neve da arte 3D ([img], emendável na vertical) caindo por toda a caixa, [velocidade] imagens por
 * volta do relógio [t] (um número inteiro, para a volta emendar). Paredes e telhado são recortados um de cada vez,
 * como no desenho antigo, e nunca chove dentro de casa. [alturaDaCena] é a altura da cena em px.
 */
private fun DrawScope.desenharClimaEmCamadas(img: ImageBitmap, t: Float, velocidade: Float, paredes: Boolean, telhado: Boolean, alturaDaCena: Float) {
    val W = size.width
    val H = alturaDaCena
    val semParedes = Path().apply {
        if (paredes) addRect(Rect(W * Planta.ESQ, H * Planta.TOPO, W * Planta.DIR, H * Planta.BASE))
    }
    val semTelhado = Path().apply {
        if (telhado) {
            moveTo(W * 0.04f, H * 0.315f); lineTo(W * 0.5f, H * 0.075f); lineTo(W * 0.96f, H * 0.315f); close()
        }
    }
    val passo = (W * img.height / img.width).roundToInt().coerceAtLeast(1)
    val desce = ((t * velocidade) % 1f) * passo
    clipPath(semParedes, clipOp = androidx.compose.ui.graphics.ClipOp.Difference) {
        clipPath(semTelhado, clipOp = androidx.compose.ui.graphics.ClipOp.Difference) {
            var y = desce - passo
            while (y < size.height) {
                camadaDaCena(img, dy = y)
                y += passo
            }
        }
    }
}

/** Degradês da cena da casa, montados uma vez por tamanho (a cena redesenha a cada quadro). */
private class PinceisDaCasa(H: Float, noite: Boolean) {
    val ceu = Brush.verticalGradient(coresDoCeu(noite), endY = H * 0.8f)
    /** Neblina no horizonte, por cima dos morros: dá distância entre o céu e o jardim. */
    val nevoa = Brush.verticalGradient(
        listOf(Color.Transparent, if (noite) Color(0x337C8CCF) else Color(0x80FFFFFF), Color.Transparent),
        startY = H * 0.56f, endY = H * 0.80f,
    )
    /** Sombra do telhado na parede, logo abaixo do beiral. */
    val sombraDoBeiral = Brush.verticalGradient(listOf(Color(0x4D3A2010), Color.Transparent), startY = H * Planta.TOPO, endY = H * (Planta.TOPO + 0.065f))
    /** Sombra do piso de cima na parede do térreo. */
    val sombraDoPiso = Brush.verticalGradient(listOf(Color(0x333A2010), Color.Transparent), startY = H * (Planta.MEIO + 0.012f), endY = H * (Planta.MEIO + 0.05f))
}

private fun DrawScope.desenharCasa(e: EstadoJogo, t: Float, nova: Estrutura?, progresso: Float, noite: Boolean, pinceis: PinceisDaCasa, semCeu: Boolean = false) {
    val W = size.width
    val H = size.height
    fun X(v: Float) = v * W
    fun Y(v: Float) = v * H

    /** Estruturas novas "caem" do céu com um quique. */
    fun camada(estrutura: Estrutura, bloco: DrawScope.() -> Unit) {
        if (!e.temEstrutura(estrutura)) return
        if (estrutura == nova && progresso < 1f) {
            withTransform({ translate(0f, -(1f - progresso) * H * 0.25f) }) { bloco() }
        } else {
            bloco()
        }
    }

    // céu
    if (!semCeu) drawRect(pinceis.ceu)
    if (noite) {
        for (i in 0 until 28) {
            val x = ((i * 0.137f + 0.05f) % 1f)
            val y = ((i * 0.071f + 0.02f) % 0.62f)
            val brilho = 0.45f + 0.55f * kotlin.math.abs(kotlin.math.sin((t + i * 0.13f) * Math.PI.toFloat()))
            drawCircle(Color.White.copy(alpha = brilho), radius = X(0.004f + (i % 3) * 0.002f), center = Offset(X(x), Y(y)))
        }
    }
    // Nuvens e passarinhos de enfeite, sempre passeando (a cena nunca fica parada).
    if (!noite) {
        for (k in 0 until 2) {
            // Passeiam só pela metade direita do céu (o sol e o arco-íris ficam à esquerda).
            val x = 0.64f + ((t * 0.3f + k * 0.5f) % 1f) * 0.30f
            nuvemDeFundo(X(x), Y(0.11f + k * 0.05f), X(0.05f))
        }
        passarinhos(t)
    }
    // morros
    drawOval(if (noite) Color(0xFF4C7F55) else Color(0xFFA8DD8F), topLeft = Offset(X(-0.2f), Y(0.68f)), size = Size(X(0.7f), Y(0.24f)))
    drawOval(if (noite) Color(0xFF447549) else Color(0xFF98D47E), topLeft = Offset(X(0.45f), Y(0.70f)), size = Size(X(0.8f), Y(0.2f)))
    // neblina do horizonte por cima dos morros
    drawRect(pinceis.nevoa, topLeft = Offset(0f, Y(0.56f)), size = Size(W, Y(0.24f)))

    // chão
    val temTerreno = e.temEstrutura(Estrutura.TERRENO)
    val chao = when {
        temTerreno && noite -> listOf(Color(0xFF3F7A45), Color(0xFF2F5E35))
        temTerreno -> listOf(Color(0xFF7CC56B), Color(0xFF5FAE52))
        noite -> listOf(Color(0xFF7A6450), Color(0xFF5E4C3C))
        else -> listOf(Color(0xFFC9A27A), Color(0xFFA97F57))
    }
    drawRect(Brush.verticalGradient(chao, startY = Y(0.78f), endY = H), topLeft = Offset(0f, Y(0.78f)), size = Size(W, H - Y(0.78f)))
    if (temTerreno) {
        for (i in 0 until 14) {
            val x = (i * 0.073f + 0.03f) % 1f
            val y = 0.82f + ((i * 37) % 17) / 100f
            drawOval(Color(0xFF6BB85C), topLeft = Offset(X(x - 0.018f), Y(y - 0.006f)), size = Size(X(0.036f), Y(0.012f)))
        }
    } else {
        placaDoTerreno(W, H)
    }

    camada(Estrutura.CERCA) {
        val tinta = Color.White
        val borda = Color(0xFFD9CBB8)
        for ((a, b) in listOf(0f to 0.10f, 0.90f to 1f)) {
            drawRect(tinta, Offset(X(a), Y(0.755f)), Size(X(b - a), Y(0.012f)))
            drawRect(tinta, Offset(X(a), Y(0.785f)), Size(X(b - a), Y(0.012f)))
            var x = a + 0.005f
            while (x < b) {
                val tabua = Path().apply {
                    moveTo(X(x), Y(0.80f)); lineTo(X(x), Y(0.745f)); lineTo(X(x + 0.01f), Y(0.735f))
                    lineTo(X(x + 0.02f), Y(0.745f)); lineTo(X(x + 0.02f), Y(0.80f)); close()
                }
                drawPath(tabua, tinta)
                drawPath(tabua, borda, style = Stroke(1.5f))
                x += 0.03f
            }
        }
    }

    camada(Estrutura.LAGO) {
        drawOval(Color(0xFF4FB3E8), Offset(X(0.68f), Y(0.87f)), Size(X(0.29f), Y(0.11f)))
        val brilho = 0.02f * kotlin.math.sin(t * 2 * Math.PI).toFloat()
        drawOval(Color(0xFF8FD6FA), Offset(X(0.73f + brilho), Y(0.895f)), Size(X(0.12f), Y(0.03f)))
    }

    camada(Estrutura.CAMINHO) {
        for ((x, y, s) in listOf(Triple(0.31f, 0.80f, 0.03f), Triple(0.29f, 0.85f, 0.035f), Triple(0.32f, 0.905f, 0.04f), Triple(0.29f, 0.965f, 0.045f))) {
            drawOval(Color(0xFFD8D2C6), Offset(X(x - s), Y(y) - X(s) * 0.45f), Size(X(2 * s), X(s) * 0.9f))
        }
    }

    // Terreno ainda vazio: o contorno pontilhado da casa que vai nascer ali.
    if (!e.temEstrutura(Estrutura.PAREDES)) {
        val tracejado = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(X(0.025f), X(0.018f)))
        val contorno = Path().apply {
            moveTo(X(Planta.ESQ), Y(Planta.BASE)); lineTo(X(Planta.ESQ), Y(Planta.TOPO)); lineTo(X(0.04f), Y(0.315f))
            lineTo(X(0.5f), Y(0.075f)); lineTo(X(0.96f), Y(0.315f)); lineTo(X(Planta.DIR), Y(Planta.TOPO)); lineTo(X(Planta.DIR), Y(Planta.BASE))
        }
        drawPath(contorno, Color.White.copy(alpha = 0.75f), style = Stroke(X(0.008f), pathEffect = tracejado, join = StrokeJoin.Round))
    }

    // chaminé fica atrás do telhado
    if (e.temEstrutura(Estrutura.TELHADO)) {
        camada(Estrutura.CHAMINE) {
            drawRect(Color(0xFFB5523B), Offset(X(0.68f), Y(0.12f)), Size(X(0.07f), Y(0.14f)))
            drawRect(Color(0xFF8E3B28), Offset(X(0.67f), Y(0.11f)), Size(X(0.09f), Y(0.025f)))
            for (i in 0 until 3) {
                val fase = (t + i / 3f) % 1f
                drawCircle(
                    Color.White.copy(alpha = 0.85f * (1f - fase)),
                    radius = X(0.018f + 0.025f * fase),
                    center = Offset(X(0.715f + 0.08f * fase), Y(0.10f - 0.09f * fase)),
                )
            }
        }
    }

    camada(Estrutura.PAREDES) {
        val L = Planta.ESQ; val R = Planta.DIR; val T = Planta.TOPO; val M = Planta.MEIO; val B = Planta.BASE
        // A casa pousa no chão: sombra macia embaixo dela.
        drawOval(Color(0x1F1B2A10), Offset(X(L - 0.04f), Y(B - 0.012f)), Size(X(R - L + 0.08f), Y(0.04f)))
        drawRect(Color(e.corDo(Comodo.QUARTO)), Offset(X(L), Y(T)), Size(X(Planta.DIVISAO_CIMA - L), Y(M - T)))
        drawRect(Color(e.corDo(Comodo.BANHEIRO)), Offset(X(Planta.DIVISAO_CIMA), Y(T)), Size(X(R - Planta.DIVISAO_CIMA), Y(M - T)))
        drawRect(Color(e.corDo(Comodo.SALA)), Offset(X(L), Y(M)), Size(X(Planta.DIVISAO_BAIXO - L), Y(B - M)))
        drawRect(Color(e.corDo(Comodo.COZINHA)), Offset(X(Planta.DIVISAO_BAIXO), Y(M)), Size(X(R - Planta.DIVISAO_BAIXO), Y(B - M)))
        papelDeParede(e.papelDeParede, X(L), Y(T), X(R - L), Y(B - T))

        // azulejos do banheiro e da cozinha
        val linha = Color(0x0F000000)
        for ((a, b, faixa) in listOf(Triple(Planta.DIVISAO_CIMA, R, 0.44f to M), Triple(Planta.DIVISAO_BAIXO, R, 0.69f to B))) {
            var x = a
            while (x < b) { drawLine(linha, Offset(X(x), Y(faixa.first)), Offset(X(x), Y(faixa.second)), 1.5f); x += 0.035f }
            var y = faixa.first
            while (y < faixa.second) { drawLine(linha, Offset(X(a), Y(y)), Offset(X(b), Y(y)), 1.5f); y += 0.025f }
        }
        // pisos de madeira e parede do meio
        drawRect(CorMadeira, Offset(X(L), Y(M - 0.012f)), Size(X(R - L), Y(0.024f)))
        drawRect(CorMadeira, Offset(X(L), Y(B - 0.008f)), Size(X(R - L), Y(0.012f)))
        drawRect(Color(0xFFE9D8C4), Offset(X(Planta.DIVISAO_CIMA - 0.005f), Y(T)), Size(X(0.01f), Y(M - T - 0.012f)))
        // Sombra do piso de cima no térreo e sombrinha nos cantos de cada cômodo (dá fundo às paredes).
        drawRect(pinceis.sombraDoPiso, Offset(X(L), Y(M + 0.012f)), Size(X(R - L), Y(0.04f)))
        for (x in listOf(L, Planta.DIVISAO_CIMA + 0.005f)) drawRect(Color(0x0F000000), Offset(X(x), Y(T)), Size(X(0.012f), Y(M - T - 0.012f)))
        for (x in listOf(L, Planta.DIVISAO_BAIXO)) drawRect(Color(0x0F000000), Offset(X(x), Y(M + 0.012f)), Size(X(0.012f), Y(B - M - 0.02f)))
        if (noite) {
            // Com o abajur, a casa fica mais clarinha por dentro.
            val escuro = if ("abajur" in e.colocados) Color(0x33101A40) else Color(0x66101A40)
            drawRect(escuro, Offset(X(L), Y(T)), Size(X(R - L), Y(B - T)))
        }
        drawRect(CorMoldura, Offset(X(L), Y(T)), Size(X(R - L), Y(B - T)), style = Stroke(X(0.018f)))
    }

    if (e.temEstrutura(Estrutura.PAREDES)) {
        camada(Estrutura.JANELAS) {
            val vidro = if (noite) CorLuz else CorVidro
            janela(X(0.39f), Y(0.35f), X(0.08f), Y(0.08f), vidro)
            janela(X(0.45f), Y(0.585f), X(0.08f), Y(0.075f), vidro)
            janela(X(0.66f), Y(0.585f), X(0.08f), Y(0.075f), vidro)
            drawCircle(vidro, X(0.035f), Offset(X(0.82f), Y(0.39f)))
            drawCircle(Color.White, X(0.035f), Offset(X(0.82f), Y(0.39f)), style = Stroke(X(0.012f)))
        }
        camada(Estrutura.PORTA) {
            val r = X(0.04f)
            val porta = Path().apply {
                addRoundRect(
                    RoundRect(
                        Rect(Offset(X(0.265f), Y(0.615f)), Size(X(0.085f), Y(0.16f))),
                        topLeft = CornerRadius(r), topRight = CornerRadius(r),
                        bottomLeft = CornerRadius.Zero, bottomRight = CornerRadius.Zero,
                    ),
                )
            }
            val corPorta = Color(Loja.coresDaPorta.getOrElse(e.corPorta) { Loja.coresDaPorta[0] })
            drawPath(porta, corPorta)
            drawPath(porta, escurecer(corPorta, 0.7f), style = Stroke(3f))
            drawCircle(Color(0xFFFFD166), X(0.009f), Offset(X(0.335f), Y(0.70f)))
            drawCircle(Color(0xFFFFE9A8), X(0.017f), Offset(X(0.3075f), Y(0.645f)))
        }
        camada(Estrutura.ESCADA) {
            val degraus = 7
            val alturaDegrau = 0.235f / degraus
            for (i in 0 until degraus) {
                val y = Planta.BASE - 0.008f - (i + 1) * alturaDegrau
                val largura = 0.14f - i * 0.018f
                drawRect(CorMadeira, Offset(X(0.105f), Y(y)), Size(X(largura), Y(alturaDegrau) + 1f))
                drawRect(Color(0xFFDDA577), Offset(X(0.105f), Y(y)), Size(X(largura), Y(0.006f)))
            }
            drawRect(Color(0xFFE9D8C4), Offset(X(0.105f), Y(Planta.MEIO - 0.012f)), Size(X(0.06f), Y(0.024f)))
        }
    }

    // (o clima é desenhado por cima de tudo, no fim)
    if (e.temEstrutura(Estrutura.PAREDES) && e.temEstrutura(Estrutura.TELHADO)) {
        camada(Estrutura.TELHADO) {
            drawRect(pinceis.sombraDoBeiral, Offset(X(Planta.ESQ), Y(Planta.TOPO)), Size(X(Planta.DIR - Planta.ESQ), Y(0.065f)))
        }
    }
    camada(Estrutura.TELHADO) {
        val telhado = Path().apply {
            moveTo(X(0.04f), Y(0.315f)); lineTo(X(0.5f), Y(0.075f)); lineTo(X(0.96f), Y(0.315f)); close()
        }
        val cor = Color(Loja.coresDoTelhado.getOrElse(e.corTelhado) { Loja.coresDoTelhado[0] })
        val borda = Color(Loja.coresDoTelhadoEscuras.getOrElse(e.corTelhado) { Loja.coresDoTelhadoEscuras[0] })
        drawPath(telhado, if (noite) escurecer(cor, 0.7f) else cor)
        drawPath(telhado, if (noite) escurecer(borda, 0.7f) else borda, style = Stroke(X(0.012f), join = StrokeJoin.Round))
        for (k in 1 until 5) {
            val y = 0.075f + k * 0.048f
            val metade = (y - 0.075f) / 0.24f * 0.46f
            drawLine(Color(0x14000000), Offset(X(0.5f - metade), Y(y)), Offset(X(0.5f + metade), Y(y)), 3f)
        }
        drawCircle(Color.White, X(0.04f), Offset(X(0.5f), Y(0.215f)))
        drawCircle(if (noite) CorLuz else CorVidro, X(0.03f), Offset(X(0.5f), Y(0.215f)))
    }

    // Chuva ou neve compradas na loja, só do lado de fora (nunca dentro dos cômodos).
    // Paredes e telhado são recortados um de cada vez: num caminho só, o retângulo e o triângulo
    // giram em sentidos opostos e a faixa onde se sobrepõem (a base do telhado) ficaria de fora do
    // recorte, deixando pingos passarem ali.
    val paredes = Path().apply {
        if (e.temEstrutura(Estrutura.PAREDES)) addRect(Rect(X(Planta.ESQ), Y(Planta.TOPO), X(Planta.DIR), Y(Planta.BASE)))
    }
    val telhadoDoClima = Path().apply {
        if (e.temEstrutura(Estrutura.TELHADO)) {
            moveTo(X(0.04f), Y(0.315f)); lineTo(X(0.5f), Y(0.075f)); lineTo(X(0.96f), Y(0.315f)); close()
        }
    }
    if (e.clima != 0) {
        clipPath(paredes, clipOp = androidx.compose.ui.graphics.ClipOp.Difference) {
            clipPath(telhadoDoClima, clipOp = androidx.compose.ui.graphics.ClipOp.Difference) { desenharClima(e.clima, t, W, H) }
        }
    }
    // Com neve, o telhado ganha uma camadinha branca.
    if (e.clima == 2 && e.temEstrutura(Estrutura.TELHADO)) {
        val neve = Path().apply {
            moveTo(X(0.04f), Y(0.315f)); lineTo(X(0.5f), Y(0.075f)); lineTo(X(0.96f), Y(0.315f))
            lineTo(X(0.93f), Y(0.318f)); lineTo(X(0.5f), Y(0.095f)); lineTo(X(0.07f), Y(0.318f)); close()
        }
        drawPath(neve, Color.White)
    }
}

private fun DrawScope.desenharClima(clima: Int, t: Float, W: Float, H: Float) {
    fun X(v: Float) = v * W
    fun Y(v: Float) = v * H
    when (clima) {
        1 -> for (i in 0 until 55) {
            val x = ((i * 0.173f) % 1f)
            val y = ((i * 0.071f + t * 2f) % 1f)
            drawLine(Color(0xCC3F7FD9), Offset(X(x), Y(y)), Offset(X(x - 0.01f), Y(y + 0.04f)), X(0.006f))
        }
        2 -> for (i in 0 until 36) {
            val x = ((i * 0.173f) % 1f) + 0.01f * kotlin.math.sin((t * 4f + i) * Math.PI.toFloat())
            val y = ((i * 0.071f + t * 0.8f) % 1f)
            drawCircle(Color.White.copy(alpha = 0.9f), X(0.006f + (i % 3) * 0.002f), Offset(X(x), Y(y)))
        }
    }
}

/** Nuvem feita de um único caminho (as bolhas se juntam sem marcas de sobreposição). */
private fun DrawScope.nuvemDeFundo(x: Float, y: Float, r: Float) {
    val nuvem = Path().apply {
        addOval(Rect(Offset(x, y), r * 0.6f))
        addOval(Rect(Offset(x + r * 0.7f, y - r * 0.2f), r * 0.8f))
        addOval(Rect(Offset(x + r * 1.4f, y), r * 0.6f))
        addRoundRect(RoundRect(Rect(x, y - r * 0.1f, x + r * 1.4f, y + r * 0.6f), CornerRadius(r * 0.3f)))
    }
    drawPath(nuvem, Color.White.copy(alpha = 0.8f))
}

/**
 * O pedaço de jardim que aparece em telas altas: canteiro, arbustos, cogumelos e o caminho continuando.
 * Os enfeites ficam nas bordas e embaixo, deixando o meio livre para os objetos (ver ItensDaCasa.noJardimDeBaixo).
 */
private fun DrawScope.jardimDeBaixo(topo: Float, alto: Float, noite: Boolean, temCaminho: Boolean, temGrama: Boolean) {
    if (!temGrama) return
    val w = size.width
    val folha = if (noite) Color(0xFF2E5F34) else Color(0xFF4FA046)
    // arbustos nos cantos
    for ((cx, r) in listOf(0.06f to 0.07f, 0.94f to 0.08f)) {
        val c = Offset(w * cx, topo + alto * 0.45f)
        drawCircle(folha, w * r, c)
        drawCircle(folha, w * r * 0.8f, c + Offset(w * r * 0.8f * (if (cx < 0.5f) 1f else -1f), w * r * 0.2f))
    }
    // canteiro com flores, embaixo à direita
    val alturaCanteiro = minOf(alto * 0.3f, w * 0.11f)
    val canteiro = Rect(Offset(w * 0.64f, topo + alto * 0.94f - alturaCanteiro), Size(w * 0.26f, alturaCanteiro))
    drawRoundRect(if (noite) Color(0xFF5A4232) else Color(0xFF8D5A3B), canteiro.topLeft, canteiro.size, CornerRadius(12f))
    for (i in 0 until 6) {
        val fx = canteiro.left + canteiro.width * (0.12f + i * 0.15f)
        val fy = canteiro.top + canteiro.height * 0.45f
        drawLine(folha, Offset(fx, fy), Offset(fx, fy + canteiro.height * 0.3f), 4f)
        val corFlor = listOf(Color(0xFFFF6FAE), Color(0xFFFFD166), Color(0xFFB57BFF))[i % 3]
        drawCircle(if (noite) escurecer(corFlor, 0.6f) else corFlor, canteiro.width * 0.045f, Offset(fx, fy))
    }
    // caminho de pedrinhas continuando até a borda
    if (temCaminho) {
        var y = topo + alto * 0.1f
        var k = 0
        while (y < topo + alto) {
            val x = w * (0.30f + if (k % 2 == 0) 0.01f else -0.01f)
            drawOval(Color(0xFFD8D2C6), Offset(x - w * 0.045f, y), Size(w * 0.09f, w * 0.04f))
            y += w * 0.07f; k++
        }
    }
    // cogumelos
    for (cx in listOf(0.14f, 0.48f)) {
        val base = Offset(w * cx, topo + alto * 0.85f)
        drawRect(Color(0xFFF4E9D8), Offset(base.x - 5f, base.y - 16f), Size(10f, 16f))
        drawArc(Color(0xFFEF476F), 180f, 180f, true, Offset(base.x - 18f, base.y - 30f), Size(36f, 28f))
        drawCircle(Color.White, 3.5f, Offset(base.x - 6f, base.y - 22f))
        drawCircle(Color.White, 3f, Offset(base.x + 7f, base.y - 20f))
    }
}

/** Desenhos do papel de parede (comprados na loja): listras, bolinhas ou estrelinhas. */
private fun DrawScope.papelDeParede(tipo: Int, x0: Float, y0: Float, w: Float, h: Float) {
    if (tipo == 0) return
    val cor = Color(0x14000000)
    val passo = w / 18f
    when (tipo) {
        1 -> {
            var x = x0
            while (x < x0 + w) { drawRect(cor, Offset(x, y0), Size(passo / 2, h)); x += passo }
        }
        2 -> {
            var y = y0 + passo / 2
            var linha = 0
            while (y < y0 + h) {
                var x = x0 + if (linha % 2 == 0) passo / 2 else passo
                while (x < x0 + w) { drawCircle(cor, passo * 0.18f, Offset(x, y)); x += passo }
                y += passo; linha++
            }
        }
        else -> {
            var y = y0 + passo
            var linha = 0
            while (y < y0 + h) {
                var x = x0 + if (linha % 2 == 0) passo else passo * 2
                while (x < x0 + w) { drawPath(caminhoEstrela(Offset(x, y), passo * 0.3f), Color(0x26FFC93C)); x += passo * 2 }
                y += passo * 1.5f; linha++
            }
        }
    }
}

private fun DrawScope.janela(x: Float, y: Float, w: Float, h: Float, vidro: Color = CorVidro) {
    if (vidro == CorLuz) {
        // Luz de dentro escapando pela janela, num halo macio.
        drawCircle(CorLuz.copy(alpha = 0.12f), w * 1.5f, Offset(x + w / 2, y + h / 2))
        drawCircle(CorLuz.copy(alpha = 0.22f), w * 1.05f, Offset(x + w / 2, y + h / 2))
    }
    val r = CornerRadius(w * 0.08f)
    drawRect(Color(0xFFFF8FAB), Offset(x - w * 0.15f, y - h * 0.15f), Size(w * 0.15f, h * 1.3f))
    drawRect(Color(0xFFFF8FAB), Offset(x + w, y - h * 0.15f), Size(w * 0.15f, h * 1.3f))
    drawRoundRect(vidro, Offset(x, y), Size(w, h), r)
    drawRoundRect(Color.White, Offset(x, y), Size(w, h), r, style = Stroke(w * 0.15f))
    // Reflexo do céu no vidro (de dia), em dois risquinhos inclinados.
    if (vidro != CorLuz) {
        drawLine(Color.White.copy(alpha = 0.6f), Offset(x + w * 0.12f, y + h * 0.38f), Offset(x + w * 0.32f, y + h * 0.12f), w * 0.07f, StrokeCap.Round)
        drawLine(Color.White.copy(alpha = 0.45f), Offset(x + w * 0.62f, y + h * 0.88f), Offset(x + w * 0.86f, y + h * 0.58f), w * 0.05f, StrokeCap.Round)
    }
    drawLine(Color.White, Offset(x + w / 2, y), Offset(x + w / 2, y + h), w * 0.075f)
    drawLine(Color.White, Offset(x, y + h / 2), Offset(x + w, y + h / 2), w * 0.075f)
    // Sombrinha do peitoril embaixo da janela.
    drawRect(Color(0x1A000000), Offset(x - w * 0.15f, y + h * 1.15f), Size(w * 1.3f, h * 0.08f))
}

private fun DrawScope.placaDoTerreno(W: Float, H: Float) {
    drawRect(Color(0xFF8D5A3B), Offset(W * 0.495f, H * 0.70f), Size(W * 0.012f, H * 0.12f))
    drawRoundRect(Color(0xFFF4D6A0), Offset(W * 0.36f, H * 0.62f), Size(W * 0.28f, H * 0.09f), CornerRadius(W * 0.02f))
    drawRoundRect(Color(0xFF8D5A3B), Offset(W * 0.36f, H * 0.62f), Size(W * 0.28f, H * 0.09f), CornerRadius(W * 0.02f), style = Stroke(W * 0.006f))
    // "tijolinhos" esperando a construção
    for (i in 0 until 3) {
        drawRoundRect(Color(0xFFD86A4A), Offset(W * (0.70f + i * 0.055f), H * 0.90f), Size(W * 0.05f, H * 0.025f), CornerRadius(4f))
    }
    drawRoundRect(Color(0xFFD86A4A), Offset(W * 0.7275f, H * 0.874f), Size(W * 0.05f, H * 0.025f), CornerRadius(4f))
}

/** Móveis sem emoji, desenhados para ocupar toda a área disponível. */
fun DrawScope.desenharMovel(d: Desenho) {
    val w = size.width
    val h = size.height
    val contorno = Color(0xFFB8C4D0)
    when (d) {
        Desenho.GELADEIRA -> {
            drawRoundRect(Color(0xFFF4F7FB), Offset.Zero, size, CornerRadius(w * 0.15f))
            drawRoundRect(contorno, Offset.Zero, size, CornerRadius(w * 0.15f), style = Stroke(2f))
            drawLine(contorno, Offset(0f, h * 0.38f), Offset(w, h * 0.38f), 2f)
            drawRect(Color(0xFF9AA8B6), Offset(w * 0.72f, h * 0.1f), Size(w * 0.08f, h * 0.18f))
            drawRect(Color(0xFF9AA8B6), Offset(w * 0.72f, h * 0.48f), Size(w * 0.08f, h * 0.22f))
            drawCircle(Color(0xFFFF6B6B), w * 0.08f, Offset(w * 0.35f, h * 0.2f))
            drawCircle(Color(0xFF4D96FF), w * 0.07f, Offset(w * 0.3f, h * 0.55f))
        }
        Desenho.FOGAO -> {
            val topo = h * 0.08f
            for (dx in listOf(0.28f, 0.72f)) drawOval(Color(0xFF3A3F47), Offset(w * dx - w * 0.16f, 0f), Size(w * 0.32f, topo * 1.4f))
            drawRoundRect(Color.White, Offset(0f, topo), Size(w, h - topo), CornerRadius(w * 0.1f))
            drawRoundRect(contorno, Offset(0f, topo), Size(w, h - topo), CornerRadius(w * 0.1f), style = Stroke(2f))
            for (dx in listOf(0.25f, 0.5f, 0.75f)) drawCircle(Color(0xFFFF6B6B), w * 0.05f, Offset(w * dx, topo + h * 0.12f))
            drawRoundRect(Color(0xFF3A3F47), Offset(w * 0.12f, topo + h * 0.25f), Size(w * 0.76f, h * 0.5f), CornerRadius(w * 0.06f))
            drawRect(Color(0xFFFFB347), Offset(w * 0.2f, topo + h * 0.3f), Size(w * 0.6f, h * 0.04f))
        }
        Desenho.MESA -> {
            val tampo = h * 0.14f
            drawRoundRect(Color(0xFFB0703C), Offset(0f, tampo * 0.6f), Size(w, tampo), CornerRadius(w * 0.04f))
            drawRoundRect(Color(0xF2FFFFFF), Offset(w * 0.05f, 0f), Size(w * 0.9f, tampo * 0.9f), CornerRadius(w * 0.03f))
            drawRect(Color(0xFFB0703C), Offset(w * 0.08f, tampo), Size(w * 0.08f, h - tampo))
            drawRect(Color(0xFFB0703C), Offset(w * 0.84f, tampo), Size(w * 0.08f, h - tampo))
        }
    }
}

// ---------------------------------------------------------------------------------------- dicas de lugar

/** Quanto tempo o balão "não" fica em cima da peça recusada (ms). */
private const val DURACAO_DA_RECUSA = 2600L

/** Onde a peça deveria ir: um cômodo, qualquer lugar dentro de casa, ou o jardim. */
sealed interface LugarCerto {
    data class NoComodo(val comodo: Comodo) : LugarCerto
    data object DentroDeCasa : LugarCerto
    data object NoJardim : LugarCerto
}

/** O lugar que brilha para ajudar com [item] (null para o que pode ir em qualquer lugar). */
fun lugarCertoDe(item: ItemCasa): LugarCerto? = item.comodo?.let { LugarCerto.NoComodo(it) } ?: when (item.lugar) {
    Lugar.DENTRO -> LugarCerto.DentroDeCasa
    Lugar.FORA -> LugarCerto.NoJardim
    Lugar.CEU, Lugar.QUALQUER -> null
}

/** [pos] é a posição na tela (já convertida do jardim de baixo), para onde a peça voltou. */
private data class Recusa(val item: ItemCasa, val pos: Posicao, val vez: Int)

private data class Sugestao(val lugar: LugarCerto, val forte: Boolean, val vez: Int)

/** Retângulo do lugar (frações da largura e da altura da cena; o jardim de baixo passa de 1). */
private fun retanguloDe(lugar: LugarCerto, jardimExtra: Float): Rect = when (lugar) {
    is LugarCerto.NoComodo -> when (lugar.comodo) {
        Comodo.QUARTO -> Rect(Planta.ESQ, Planta.TOPO, Planta.DIVISAO_CIMA, Planta.MEIO)
        Comodo.BANHEIRO -> Rect(Planta.DIVISAO_CIMA, Planta.TOPO, Planta.DIR, Planta.MEIO)
        Comodo.SALA -> Rect(Planta.ESQ, Planta.MEIO, Planta.DIVISAO_BAIXO, Planta.BASE)
        Comodo.COZINHA -> Rect(Planta.DIVISAO_BAIXO, Planta.MEIO, Planta.DIR, Planta.BASE)
    }
    LugarCerto.DentroDeCasa -> Rect(Planta.ESQ, Planta.TOPO, Planta.DIR, Planta.BASE)
    LugarCerto.NoJardim -> Rect(0.02f, Planta.BASE + 0.025f, 0.98f, 1f + jardimExtra - 0.02f)
}

/**
 * O lugar certo pisca devagar com uma luz dourada e some sozinho. Forte: 3 piscadas (peça recusada).
 * Suave: 2 piscadas mais fracas (só uma dica, sem punir).
 */
@Composable
private fun BrilhoNoLugar(s: Sugestao, larguraPx: Float, alturaPx: Float, jardimExtra: Float, onFim: () -> Unit) {
    val luz = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        repeat(if (s.forte) 3 else 2) {
            luz.animateTo(1f, tween(380))
            luz.animateTo(0.35f, tween(380))
        }
        luz.animateTo(0f, tween(500))
        onFim()
    }
    val r = retanguloDe(s.lugar, jardimExtra)
    val maximo = if (s.forte) 1f else 0.6f
    Canvas(
        Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Lugar certo brilhando" },
    ) {
        val a = luz.value * maximo
        val canto = CornerRadius(12.dp.toPx())
        val topoEsq = Offset(r.left * larguraPx, r.top * alturaPx)
        val tamanho = Size(r.width * larguraPx, r.height * alturaPx)
        drawRoundRect(Color(0xFFFFF3B0).copy(alpha = 0.5f * a), topoEsq, tamanho, canto)
        drawRoundRect(Color(0xFFFFC93C).copy(alpha = a), topoEsq, tamanho, canto, style = Stroke(6.dp.toPx()))
        // Estrelinhas nos cantos, para chamar o olho.
        val raio = 7.dp.toPx() * (0.6f + 0.4f * luz.value)
        for (c in listOf(topoEsq, Offset(topoEsq.x + tamanho.width, topoEsq.y), Offset(topoEsq.x, topoEsq.y + tamanho.height), Offset(topoEsq.x + tamanho.width, topoEsq.y + tamanho.height))) {
            drawPath(caminhoEstrela(c, raio), Color(0xFFFFD84D).copy(alpha = a))
        }
    }
}

/**
 * Balão "não" sobre a peça recusada: um círculo vermelho cortado por cima da miniatura da peça,
 * sem texto (a criança ainda não lê). Aparece com um pulinho e some sozinho.
 */
@Composable
private fun BalaoDeNao(r: Recusa, larguraPx: Float, alturaPx: Float) {
    val densidade = LocalDensity.current
    val pop = remember { Animatable(0f) }
    LaunchedEffect(Unit) { pop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow)) }
    val pos = r.pos
    val alturaDaPeca = r.item.tamanho * larguraPx * proporcaoDoItem(r.item)
    val lado = maxOf(larguraPx * 0.15f, with(densidade) { 56.dp.toPx() })
    val x = (pos.x * larguraPx - lado / 2).coerceIn(0f, larguraPx - lado)
    val y = (pos.y * alturaPx - alturaDaPeca - lado * 1.08f).coerceAtLeast(0f)
    Box(
        Modifier
            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            .size(with(densidade) { lado.toDp() })
            .zIndex(12f)
            .graphicsLayer {
                scaleX = pop.value
                scaleY = pop.value
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
            }
            .semantics { contentDescription = "Não pode aqui" },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val raio = size.minDimension * 0.44f
            val c = Offset(size.width / 2, size.height * 0.46f)
            // Rabinho do balão apontando para a peça.
            val rabo = Path().apply {
                moveTo(c.x - raio * 0.28f, c.y + raio * 0.8f)
                lineTo(c.x, size.height)
                lineTo(c.x + raio * 0.28f, c.y + raio * 0.8f)
                close()
            }
            drawPath(rabo, Color.White)
            drawCircle(Color.White, raio, c)
            drawCircle(Color(0xFFFFCDD2), raio, c, style = Stroke(3.dp.toPx()))
        }
        Box(Modifier.padding(bottom = with(densidade) { (lado * 0.08f).toDp() }), contentAlignment = Alignment.Center) {
            IconeDoItem(r.item, with(densidade) { (lado * 0.48f).toDp() }, Modifier.graphicsLayer { alpha = 0.85f })
            Canvas(Modifier.size(with(densidade) { (lado * 0.66f).toDp() })) {
                val vermelho = Color(0xFFE53935)
                val traco = size.minDimension * 0.11f
                val raio = size.minDimension / 2 - traco / 2
                drawCircle(vermelho, raio, style = Stroke(traco))
                val d = raio * 0.7071f
                drawLine(vermelho, Offset(center.x - d, center.y - d), Offset(center.x + d, center.y + d), traco, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
        }
    }
}
