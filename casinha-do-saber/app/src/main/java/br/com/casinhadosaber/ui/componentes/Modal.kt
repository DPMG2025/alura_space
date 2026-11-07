package br.com.casinhadosaber.ui.componentes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.layout.layout
import androidx.compose.ui.zIndex
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.Raios

// Janela por cima da tela (sistema de design, A6): véu violeta, cartão creme e fita com o título.

/**
 * Véu violeta que cobre a tela e segura os toques (nada atrás dele responde). Mais claro no meio,
 * para o cartão "acender". Com [onTocarFora], tocar fora do conteúdo chama a função (fechar).
 *
 * O véu é um irmão atrás do [conteudo] (e não o pai dele): assim o toque no véu não junta o conteúdo da
 * janela num nó só de acessibilidade ("Fechar"), e cada texto e botão da janela continua sendo lido e achado
 * sozinho. Com [titulo], a janela é anunciada como painel com esse nome.
 */
@Composable
fun VeuDoModal(
    modifier: Modifier = Modifier,
    onTocarFora: (() -> Unit)? = null,
    descricaoFora: String? = null,
    titulo: String? = null,
    conteudo: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .fillMaxSize()
            .then(if (titulo != null) Modifier.semantics { paneTitle = titulo } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .matchParentSize()
                .drawWithCache {
                    val pincel = Brush.radialGradient(
                        listOf(Cores.Veu.copy(alpha = Cores.Veu.alpha * 0.75f), Cores.Veu, lerp(Cores.Veu, Color(0xFF1E1440), 0.35f)),
                        center = Offset(size.width / 2, size.height * 0.45f),
                        radius = maxOf(size.width, size.height) * 0.75f,
                    )
                    onDrawBehind { drawRect(pincel) }
                }
                .then(
                    if (onTocarFora != null) {
                        Modifier.clickable(remember { MutableInteractionSource() }, indication = null) { onTocarFora() }
                    } else {
                        // Sem fechar por fora: só segura os toques (sem virar um botão para a acessibilidade).
                        Modifier.pointerInput(Unit) { detectTapGestures { } }
                    },
                )
                .then(if (descricaoFora != null) Modifier.semantics { contentDescription = descricaoFora } else Modifier),
        )
        conteudo()
    }
}

/** Altura da fita do título e quanto ela sobe acima do cartão. */
private val ALTURA_DA_FITA = 52.dp
private val FITA_ACIMA = 24.dp

/**
 * Cartão das janelas: corpo creme com borda branca, contorno e sombra suave, e uma fita colorida com o
 * [titulo] presa no alto (cauda dobrada dos dois lados). Com [onFechar], mostra o botão ✖ no canto.
 * O conteúdo fica numa coluna centralizada; com [rolavel], rola quando não cabe.
 */
@Composable
fun CartaoModal(
    titulo: String?,
    modifier: Modifier = Modifier,
    corDaFita: Color = Cores.Roxo,
    larguraMaxima: Dp = 520.dp,
    rolavel: Boolean = true,
    onFechar: (() -> Unit)? = null,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .padding(16.dp)
            .widthIn(max = larguraMaxima)
            .fillMaxWidth()
            // Segura os toques dentro do cartão (não fecha a janela pelo véu). Sem `clickable`: ele juntaria
            // todo o conteúdo do cartão num nó só para a acessibilidade.
            .pointerInput(Unit) { detectTapGestures { } }
            .then(if (titulo != null) Modifier.semantics { paneTitle = titulo } else Modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (titulo != null) {
            // A fita fica por cima do cartão e não chega no ✖ (título longo quebra a linha e a fita cresce).
            FitaDoTitulo(
                titulo,
                corDaFita,
                Modifier
                    .zIndex(1f)
                    .padding(horizontal = if (onFechar != null) 46.dp else 12.dp),
            )
        }
        val sobe = if (titulo != null) FITA_ACIMA else 0.dp
        Box(
            Modifier
                .fillMaxWidth()
                .subir(sobe)
                .corpoDoCartao(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .then(if (rolavel) Modifier.verticalScroll(rememberScrollState()) else Modifier)
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = sobe + 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = conteudo,
            )
            if (onFechar != null) {
                BotaoRedondo(
                    "✖",
                    "Fechar",
                    onFechar,
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-14).dp),
                    tamanho = 48.dp,
                )
            }
        }
    }
}

/** Sobe [quanto] (para baixo da fita) sem deixar um buraco embaixo: a altura medida diminui junto. */
private fun Modifier.subir(quanto: Dp): Modifier = layout { medida, limites ->
    val p = medida.measure(limites)
    val d = quanto.roundToPx()
    layout(p.width, (p.height - d).coerceAtLeast(0)) { p.place(0, -d) }
}

/**
 * Corpo creme com borda branca de 4 dp, contorno ameixa por fora e sombra suave embaixo. Tudo desenhado
 * atrás do conteúdo (o `border` do Compose pinta por cima e cortava o ✖ que encosta na borda).
 */
private fun Modifier.corpoDoCartao(): Modifier = drawBehind {
    val raio = Raios.Enorme.toPx()
    val r = CornerRadius(raio)
    val fora = 2.dp.toPx()
    val borda = 4.dp.toPx()
    drawRoundRect(Cores.Sombra, topLeft = Offset(0f, 8.dp.toPx()), size = size, cornerRadius = r)
    drawRoundRect(Cores.Sombra.copy(alpha = 0.1f), topLeft = Offset(-fora, 12.dp.toPx()), size = Size(size.width + fora * 2, size.height), cornerRadius = r)
    drawRoundRect(Cores.Contorno, topLeft = Offset(-fora, -fora), size = Size(size.width + fora * 2, size.height + fora * 2), cornerRadius = CornerRadius(raio + fora))
    drawRoundRect(Color.White, size = size, cornerRadius = r)
    drawRoundRect(
        Cores.Creme,
        topLeft = Offset(borda, borda),
        size = Size(size.width - borda * 2, size.height - borda * 2),
        cornerRadius = CornerRadius(raio - borda),
    )
}

/**
 * A fita do título: faixa com brilho no alto e duas caudas mais escuras, dobradas para trás, com o
 * corte em "V". O texto é branco com uma sombra da própria cor (contraste em qualquer [cor]).
 */
@Composable
fun FitaDoTitulo(titulo: String, cor: Color, modifier: Modifier = Modifier) {
    val escura = escurecer(cor, 0.72f)
    Box(
        modifier
            .heightIn(min = ALTURA_DA_FITA)
            .widthIn(min = 200.dp, max = 400.dp)
            .drawWithCache {
                val w = size.width
                val h = size.height
                val cauda = 30.dp.toPx()
                val desce = 10.dp.toPx()
                val dobra = 8.dp.toPx()
                val v = 10.dp.toPx()
                fun caudaDe(esquerda: Boolean): Path = Path().apply {
                    val x0 = if (esquerda) dobra else w - dobra
                    val fora = if (esquerda) -cauda + dobra else w + cauda - dobra
                    moveTo(x0, desce)
                    lineTo(fora, desce)
                    lineTo(fora + (if (esquerda) v else -v), desce + (h - desce) / 2)
                    lineTo(fora, h + desce)
                    lineTo(x0, h + desce)
                    close()
                }
                val esquerda = caudaDe(true)
                val direita = caudaDe(false)
                // Triângulos da dobra: a parte que "passa por trás" da faixa, mais escura ainda.
                val dobraE = Path().apply { moveTo(dobra, h); lineTo(dobra, h + desce); lineTo(dobra * 2.2f, h); close() }
                val dobraD = Path().apply { moveTo(w - dobra, h); lineTo(w - dobra, h + desce); lineTo(w - dobra * 2.2f, h); close() }
                val corDaDobra = escurecer(cor, 0.5f)
                val r = CornerRadius(Raios.Pequeno.toPx())
                val faixa = Brush.verticalGradient(listOf(lerp(cor, Color.White, 0.15f), cor))
                val traco = Stroke(2.dp.toPx())
                onDrawBehind {
                    drawPath(esquerda, escura)
                    drawPath(direita, escura)
                    drawPath(esquerda, Cores.Contorno, style = traco)
                    drawPath(direita, Cores.Contorno, style = traco)
                    drawPath(dobraE, corDaDobra)
                    drawPath(dobraD, corDaDobra)
                    drawRoundRect(Cores.Sombra, topLeft = Offset(0f, 3.dp.toPx()), size = size, cornerRadius = r)
                    drawRoundRect(faixa, cornerRadius = r)
                    drawRoundRect(Cores.Brilho, topLeft = Offset(6.dp.toPx(), 4.dp.toPx()), size = Size(w - 12.dp.toPx(), h * 0.28f), cornerRadius = CornerRadius(h * 0.14f))
                    drawRoundRect(Cores.Contorno, cornerRadius = r, style = traco)
                }
            }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Título de brinquedo: branco com contorno e lábio da cor escura da fita (até duas linhas).
        TextoComContorno(
            titulo,
            modifier = Modifier.semantics(mergeDescendants = true) { heading() },
            estilo = MaterialTheme.typography.headlineSmall,
            corDoContorno = escurecer(cor, 0.55f),
            espessura = 4.dp,
            profundidade = 2.dp,
            maxLinhas = 2,
        )
    }
}

/**
 * A janela inteira: [VeuDoModal] + [CartaoModal]. Com [fecharTocandoFora], tocar no véu também fecha.
 */
@Composable
fun JanelaModal(
    titulo: String?,
    onFechar: (() -> Unit)?,
    modifier: Modifier = Modifier,
    corDaFita: Color = Cores.Roxo,
    fecharTocandoFora: Boolean = true,
    larguraMaxima: Dp = 520.dp,
    conteudo: @Composable ColumnScope.() -> Unit,
) {
    VeuDoModal(
        onTocarFora = if (fecharTocandoFora) onFechar else null,
        // Nome diferente do ✖ ("Fechar"), para os dois não se confundirem (nem nos testes).
        descricaoFora = if (fecharTocandoFora && onFechar != null) "Fechar tocando fora da janela" else null,
    ) {
        CartaoModal(titulo, modifier, corDaFita, larguraMaxima, onFechar = onFechar, conteudo = conteudo)
    }
}
