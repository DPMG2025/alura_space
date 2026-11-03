package br.com.casinhadosaber.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import br.com.casinhadosaber.dominio.AreaDaCasa
import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Passeio
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.TipoDeEvento
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Uma área nova da casa que cresce (piscina, área do pet, quarto novo...): o fundo desenhado em Canvas e
 * as coisas que a criança já colocou, que ela arrasta com o dedo como na casa. Soltar abaixo da área
 * guarda a coisa de volta no baú. Nas áreas não há lugar proibido: cada coisa fica onde a criança quiser.
 *
 * As posições são frações da largura e da altura da área; o tamanho das coisas vem da largura, limitada
 * pela altura (a área deitada não deixa as coisas gigantes).
 *
 * Com [construindo], a obra acontece na frente da criança: o miolo cresce de baixo para cima, com
 * martelinhos e plaquinhas de obra que somem quando termina.
 */
@Composable
fun CenaDaArea(
    area: AreaDaCasa,
    estado: EstadoJogo,
    modifier: Modifier = Modifier,
    interativa: Boolean = true,
    noite: Boolean = false,
    construindo: Boolean = false,
    destaqueId: String? = null,
    /** Evento do dia (Natal, Festa Junina...): os enfeites ficam no fundo, atrás das coisas e dos botões. */
    evento: TipoDeEvento? = null,
    onTocarItem: (ItemCasa) -> Unit = {},
    onMoverItem: (ItemCasa, Posicao) -> Unit = { _, _ -> },
    onGuardarItem: (ItemCasa) -> Unit = {},
) {
    val transicao = rememberInfiniteTransition(label = "area")
    val t by transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "tempo")
    val relogio = rememberRelogioDoPasseio()
    val obra = remember(area.id) { Animatable(if (construindo) 0f else 1f) }
    LaunchedEffect(construindo, area.id) {
        if (construindo) {
            obra.snapTo(0f)
            obra.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessVeryLow))
        }
    }
    BoxWithConstraints(
        modifier
            .clipToBounds()
            .testTag("area_${area.id}")
            .semantics { contentDescription = area.nome },
    ) {
        val larguraPx = constraints.maxWidth.toFloat()
        val alturaPx = constraints.maxHeight.toFloat()
        val unidade = minOf(larguraPx, alturaPx / ItensDaCasa.PROPORCAO_DA_CENA)
        // O fundo parado fica numa camada própria (só redesenha quando a área, a obra ou a noite mudam);
        // por cima, só o que mexe (estrelas, água, luzes da pista).
        FundoParadoDaArea(area, obra, noite)
        Canvas(Modifier.fillMaxSize()) { desenharFundoAnimado(area, obra.value, t, noite) }
        if (evento != null) EnfeitesDoEvento(evento, Modifier.fillMaxSize())

        // Do fundo para a frente: quem está mais embaixo (mais perto) é desenhado por último.
        val presentes = area.coisas.mapNotNull { item ->
            estado.colocados[item.id]?.let { g -> item to Posicao(g.x.coerceIn(0.03f, 0.97f), g.y.coerceIn(0.08f, 1f)) }
        }.sortedBy { it.second.y }
        for ((item, pos) in presentes) {
            val bichoDesenhado = bichoDesenhadoDe(item.id)
            val passeia = CasaQueCresce.passeia(item.id)
            val semente = item.id.hashCode()
            val alcance = if (passeia) Passeio.alcance(pos.x, 0.1f) else 0f
            // Profundidade: lá no fundo as coisas ficam menores (0,8x) e aqui na frente, maiores (1,1x).
            val profundidade = 0.8f + 0.3f * ((pos.y - 0.08f) / 0.92f).coerceIn(0f, 1f)
            val larguraRelativa = item.tamanho * unidade / larguraPx * (if (bichoDesenhado != null) 1.7f else 1f) * profundidade
            // Jeitinho de cada coisa (fixo, vem do id): um tico torta e um tico para o lado, para não parecer grade.
            val jeito = (semente xor (semente ushr 16)) and 0x7FFFFFFF
            val inclinacao = if (passeia || bichoDesenhado != null) 0f else ((jeito % 9) - 4) * 0.9f
            val desvio = if (passeia) 0f else (((jeito / 9) % 7) - 3) / 3f * larguraRelativa * larguraPx * 0.06f
            // Sombra de contato só no chão (o que está no céu ou na parede não tem chão embaixo).
            val noChao = pos.y > CasaQueCresce.HORIZONTE + 0.03f && bichoDesenhado == null
            key(item.id) {
                ObjetoNaCena(
                    item = item,
                    pos = pos,
                    larguraCena = larguraPx,
                    alturaCena = alturaPx,
                    larguraRelativa = larguraRelativa,
                    sombra = noChao,
                    inclinacao = inclinacao,
                    desvioX = desvio,
                    interativo = interativa,
                    aparecer = item.id == destaqueId,
                    vida = if (passeia) Vida.PARADO else vidaDo(CasaQueCresce.tipoDe(item.id)),
                    tempo = t,
                    noite = noite,
                    deriva = if (passeia) ({ Passeio.em(relogio.value.toLong(), semente).deslocamento * alcance * larguraPx }) else null,
                    pularAoTocar = passeia,
                    onTocar = { onTocarItem(item) },
                    onSoltar = { nova ->
                        if (nova.y > 1.04f) {
                            onGuardarItem(item)
                            null
                        } else {
                            val destino = Posicao(nova.x.coerceIn(0.04f, 0.96f), nova.y.coerceIn(0.10f, 0.99f))
                            onMoverItem(item, destino)
                            destino
                        }
                    },
                ) { w ->
                    when {
                        bichoDesenhado != null -> BichoDesenhado(bichoDesenhado.first, bichoDesenhado.second, { Passeio.em(relogio.value.toLong(), semente) })
                        passeia -> Box(
                            Modifier.graphicsLayer {
                                val p = Passeio.em(relogio.value.toLong(), semente)
                                transformOrigin = TransformOrigin(0.5f, 1f)
                                // Os emojis de bicho olham para a esquerda: vira quando anda para a direita.
                                scaleX = if (p.paraDireita) -1f else 1f
                                if (p.gesto == br.com.casinhadosaber.dominio.Gesto.SENTADO) scaleY = 0.9f
                                if (p.gesto == br.com.casinhadosaber.dominio.Gesto.ANDANDO) {
                                    translationY = -kotlin.math.abs(sin(p.passada * 2f * Math.PI.toFloat())) * w * 0.08f
                                }
                            },
                        ) { ConteudoDoItem(item, w) }
                        else -> ConteudoDoItem(item, w)
                    }
                }
            }
        }

        // Obra: martelinhos e plaquinhas enquanto o miolo cresce.
        val progresso = obra.value
        if (progresso < 0.98f) {
            val densidade = LocalDensity.current
            val lado = unidade * 0.12f
            val pontos = listOf(0.28f to 0.62f, 0.5f to 0.52f, 0.72f to 0.66f)
            pontos.forEachIndexed { i, (x, y) ->
                val pulo = kotlin.math.abs(sin((t * 4f + i * 0.33f) * Math.PI.toFloat())) * lado * 0.5f
                Box(
                    Modifier
                        .offset { IntOffset((x * larguraPx - lado / 2).roundToInt(), (y * alturaPx - lado - pulo).roundToInt()) }
                        .size(with(densidade) { lado.toDp() })
                        .graphicsLayer {
                            alpha = ((1f - progresso) * 1.4f).coerceIn(0f, 1f)
                            rotationZ = -25f + 50f * kotlin.math.abs(sin((t * 4f + i * 0.33f) * Math.PI.toFloat()))
                        },
                ) { EmojiGrande(if (i == 1) "🚧" else "🔨", lado) }
            }
        }
    }
}

/** O fundo parado da área, numa camada própria: a cena mexe a cada quadro, mas esta camada só é refeita quando muda. */
@Composable
private fun FundoParadoDaArea(area: AreaDaCasa, obra: Animatable<Float, AnimationVector1D>, noite: Boolean) {
    Canvas(Modifier.fillMaxSize().graphicsLayer()) { desenharFundoParado(area, obra.value, noite) }
}
