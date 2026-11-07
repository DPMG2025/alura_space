package br.com.casinhadosaber.ui.componentes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.tema.Baloo2
import br.com.casinhadosaber.ui.tema.Cores

// Peças novas do visual brinquedo 3D (Fase 2 do PRD visual 3D; `docs/visual-3d/prototipo/ui2.css`).

/**
 * Título ou número em branco com contorno escuro e um "lábio" da cor do contorno embaixo (`.titulo-brinquedo`
 * do protótipo). Sobre blocos coloridos, use [corDoContorno] = [Cores.ContornoEscuro] (`.contorno-escuro`).
 *
 * Só o texto de cima vai para a acessibilidade; as camadas de contorno são decorativas. A caixa ganha meia
 * [espessura] em volta e a [profundidade] embaixo, para o contorno nunca ser cortado.
 */
@Composable
fun TextoComContorno(
    texto: String,
    modifier: Modifier = Modifier,
    estilo: TextStyle = MaterialTheme.typography.headlineLarge,
    cor: Color = Color.White,
    corDoContorno: Color = Cores.ContornoDoTitulo,
    corDoLabio: Color = if (corDoContorno == Cores.ContornoDoTitulo) Cores.LabioDoTitulo else corDoContorno,
    espessura: Dp = 4.dp,
    profundidade: Dp = 3.dp,
    maxLinhas: Int = 1,
    alinhamento: TextAlign = TextAlign.Center,
) {
    val traco = with(LocalDensity.current) { espessura.toPx() }
    val contorno = Stroke(width = traco, join = StrokeJoin.Round)
    val folga = espessura / 2
    Box(modifier.padding(start = folga, end = folga, top = folga, bottom = folga + profundidade), contentAlignment = Alignment.Center) {
        val decorativo = Modifier.clearAndSetSemantics { }
        if (profundidade > 0.dp) {
            // Lábio: o texto inteiro (contorno e miolo) na cor do lábio, descido.
            Text(texto, decorativo.offset(y = profundidade), style = estilo.copy(color = corDoLabio, drawStyle = contorno), maxLines = maxLinhas, overflow = TextOverflow.Ellipsis, textAlign = alinhamento)
            Text(texto, decorativo.offset(y = profundidade), style = estilo.copy(color = corDoLabio), maxLines = maxLinhas, overflow = TextOverflow.Ellipsis, textAlign = alinhamento)
        }
        Text(texto, decorativo, style = estilo.copy(color = corDoContorno, drawStyle = contorno), maxLines = maxLinhas, overflow = TextOverflow.Ellipsis, textAlign = alinhamento)
        Text(texto, style = estilo.copy(color = cor), maxLines = maxLinhas, overflow = TextOverflow.Ellipsis, textAlign = alinhamento)
    }
}

/** Estilo do texto das pílulas: Baloo 2 ExtraBold roxo (`.pilula` do protótipo). */
val EstiloDaPilula = TextStyle(fontFamily = Baloo2, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 26.sp, color = Cores.TextoDaPilula)

/**
 * Desenho da pílula atrás do conteúdo: branco para lilás, lábio lilás de 5 dp embaixo (reserva o espaço com
 * padding) e sombra macia. Serve para placares, a trilha das contas e etiquetas.
 */
fun Modifier.pilula(corDoCorpo: Color = Color.White, corDoLabio: Color = Cores.LabioClaro): Modifier = this
    .padding(bottom = LABIO_DA_PILULA)
    .drawWithCache {
        val r = CornerRadius(size.height / 2f)
        val corpo = Brush.verticalGradient(listOf(corDoCorpo, if (corDoCorpo == Color.White) Color(0xFFF1ECFF) else corDoCorpo))
        val labio = LABIO_DA_PILULA.toPx()
        onDrawBehind {
            drawRoundRect(Cores.Sombra.copy(alpha = 0.10f), topLeft = Offset(0f, labio + 6.dp.toPx()), size = size, cornerRadius = r)
            drawRoundRect(Cores.Sombra.copy(alpha = 0.14f), topLeft = Offset(0f, labio + 2.dp.toPx()), size = size, cornerRadius = r)
            drawRoundRect(corDoLabio, topLeft = Offset(0f, labio), size = size, cornerRadius = r)
            drawRoundRect(corpo, cornerRadius = r)
            drawRoundRect(Color.White, cornerRadius = r, style = Stroke(2.dp.toPx()))
        }
    }

private val LABIO_DA_PILULA = 5.dp

/**
 * Pílula clara com conteúdo em linha (ícone e número, contas...). Com [descricao], vira um nó só para a
 * acessibilidade com esse nome.
 */
@Composable
fun Pilula(
    modifier: Modifier = Modifier,
    descricao: String? = null,
    conteudo: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .then(if (descricao != null) Modifier.clearAndSetSemantics { contentDescription = descricao } else Modifier)
            .pilula()
            .heightIn(min = 40.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ProvideTextStyle(EstiloDaPilula) { conteudo() }
    }
}

/** A pílula do placar: um ícone da arte (moeda, estrela...) e um valor curto ("27", "+5"), como nas maquetes. */
@Composable
fun PilulaDeValor(
    icone: String,
    valor: String,
    modifier: Modifier = Modifier,
    descricao: String? = null,
    reserva: (@Composable () -> Unit)? = null,
) {
    Pilula(modifier, descricao) {
        ImagemDeArte(icone, Modifier.size(28.dp), reserva = reserva)
        Text(valor, maxLines = 1)
    }
}

/**
 * Fileira de estrelas 3D: [cheias] de [total] acesas (`estrela_cheia`/`estrela_vazia`). Com [brilharUltima],
 * a última acesa é a `estrela_brilhando` (halo e faíscas, maior que a casa dela, sem empurrar as outras).
 * Para a criança, é assim que o progresso aparece: estrelas que se veem, nunca "3/250".
 */
@Composable
fun EstrelasDeProgresso(
    cheias: Int,
    modifier: Modifier = Modifier,
    total: Int = 3,
    tamanho: Dp = 24.dp,
    espaco: Dp = 2.dp,
    brilharUltima: Boolean = false,
    descricao: String? = null,
) {
    val acesas = cheias.coerceIn(0, total.coerceAtLeast(0))
    Row(
        modifier.then(if (descricao != null) Modifier.clearAndSetSemantics { contentDescription = descricao } else Modifier),
        horizontalArrangement = Arrangement.spacedBy(espaco),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total.coerceAtLeast(0)) { i ->
            val brilha = brilharUltima && i == acesas - 1
            if (brilha) {
                Box(Modifier.size(tamanho), contentAlignment = Alignment.Center) {
                    ImagemDeArte(
                        ID_ESTRELA_BRILHANDO,
                        Modifier.size(tamanho).graphicsLayer { scaleX = 1.5f; scaleY = 1.5f },
                    ) { Estrela(true, tamanho = tamanho) }
                }
            } else {
                Estrela(i < acesas, tamanho = tamanho)
            }
        }
    }
}

/**
 * As contas do progresso numa pílula (maquete 2): [feitas] contas de [cor], a da vez em amarelo e maior,
 * e as que faltam peroladas (`conta_*`). As contas diminuem para caber na largura dada, até 14 dp.
 */
@Composable
fun ContasDeProgresso(
    feitas: Int,
    total: Int,
    modifier: Modifier = Modifier,
    cor: CorDaConta = CorDaConta.VERDE,
    tamanho: Dp = 28.dp,
    comAtual: Boolean = true,
    descricao: String? = null,
) {
    val contas = ProgressoVisivel.contas(feitas, total, comAtual)
    if (contas.isEmpty()) return
    BoxWithConstraints(modifier) {
        val espaco = 4.dp
        val folgaDaPilula = 24.dp + 6.dp
        val cabe = if (maxWidth == Dp.Infinity) tamanho else (maxWidth - folgaDaPilula - espaco * (contas.size - 1)) / (contas.size + 0.25f)
        val lado = minOf(tamanho, cabe).coerceAtLeast(14.dp)
        Row(
            Modifier
                .then(if (descricao != null) Modifier.clearAndSetSemantics { contentDescription = descricao } else Modifier)
                .pilula()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(espaco),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (estado in contas) {
                val (id, escala) = when (estado) {
                    EstadoDaConta.FEITA -> cor.id to 1f
                    EstadoDaConta.ATUAL -> CorDaConta.AMARELA.id to 1.25f
                    EstadoDaConta.FALTA -> CorDaConta.VAZIA to 0.9f
                }
                ImagemDeArte(id, Modifier.size(lado * escala))
            }
        }
    }
}

/** O ícone 3D de um caminho (blocos 1 2 3, blocos A B C, globo, argolas); sem a arte, o emoji do caminho. */
@Composable
fun IconeDeTrilha(trilha: Trilha, modifier: Modifier = Modifier, descricao: String? = null) {
    ImagemDeArte(idDoIconeDaTrilha(trilha), modifier, descricao = descricao) {
        Box(contentAlignment = Alignment.Center) {
            Text(trilha.emoji, style = MaterialTheme.typography.headlineLarge)
        }
    }
}
