package br.com.casinhadosaber.ui.componentes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Rect
import android.util.LruCache
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.Perfil
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Personagens em camadas 3D (PRD visual 3D, seção 3.2): as camadas de `assets/arte/` empilhadas na ordem do
 * manifesto, com pele, cabelo e roupa tingidos por multiplicação, compostas UMA vez num bitmap por
 * (camadas, cores, tamanho) e guardadas num cache. Cada quadro de animação só copia o bitmap pronto; os
 * movimentos (respirar, pulo com amassar e esticar, balanço) são transformações da camada do Compose.
 */
object ComposicaoDosPersonagens {
    private data class Chave(
        val ids: List<String>,
        val pele: Long?,
        val cabelo: Long?,
        val roupa: Long?,
        val largura: Int,
        val altura: Int,
    )

    private class Composto(val bitmap: Bitmap, val imagem: ImageBitmap)

    /**
     * Personagens já compostos, medidos em KB: 1/16 da memória do app, entre 4 MB e [TETO_DO_CACHE_KB] (as
     * camadas soltas ficam no cache da [ArteDoApp], que usa 1/8). Uma criança acenando e piscando usa uns 8
     * quadros; uma miniatura parada do guarda-roupa (58 dp) usa um só, de uns 35 KB. O teto existe porque o
     * guarda-roupa cria muitas combinações (cada traje, acessório e chapéu é uma), e num aparelho com muita
     * memória o 1/16 deixaria dezenas de MB de bitmaps presos que nunca mais voltam à tela.
     */
    private val cache = object : LruCache<Chave, Composto>(
        (Runtime.getRuntime().maxMemory() / 1024 / 16).toInt().coerceIn(4 * 1024, TETO_DO_CACHE_KB),
    ) {
        override fun sizeOf(key: Chave, value: Composto): Int = (value.bitmap.byteCount / 1024).coerceAtLeast(1)
    }

    private const val TETO_DO_CACHE_KB = 24 * 1024

    /** null = ainda não testado; depois, se o aparelho consegue ler a arte (WebP com transparência). */
    @Volatile private var leitorFunciona: Boolean? = null

    /**
     * true se este personagem pode ser desenhado em camadas para este perfil, vestido como está (traje,
     * acessórios e chapéu têm camadas desde a Fase 7): todas as camadas no manifesto e a arte legível. Senão,
     * fica o desenho antigo, só como reserva.
     */
    fun podeUsar(contexto: Context, personagem: PersonagemDaArte, perfil: Perfil): Boolean {
        val manifesto = ArteDoApp.manifesto(contexto)
        if (!CamadasDosPersonagens.disponivel(manifesto, personagem, perfil)) return false
        val funciona = leitorFunciona ?: run {
            val cabeca = manifesto.imagens.getValue(CamadasDosPersonagens.cabeca(personagem))
            (ArteDoApp.bitmap(contexto, cabeca, 4) != null).also { leitorFunciona = it }
        }
        return funciona
    }

    /**
     * As [camadas] (já na ordem de desenho) compostas num bitmap de [largura] x [altura] pixels, tingidas com as
     * cores do [perfil]. Nulo se alguma camada não puder ser lida.
     */
    fun composto(contexto: Context, camadas: List<InfoDaArte>, perfil: Perfil, largura: Int, altura: Int): ImageBitmap? {
        val chave = Chave(
            ids = camadas.map { it.id },
            pele = CamadasDosPersonagens.cor(perfil, "pele"),
            cabelo = CamadasDosPersonagens.cor(perfil, "cabelo"),
            roupa = CamadasDosPersonagens.cor(perfil, "roupa"),
            largura = largura,
            altura = altura,
        )
        cache.get(chave)?.let { return it.imagem }
        val bmp = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
        val tela = Canvas(bmp)
        val destino = Rect(0, 0, largura, altura)
        val tinta = Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        for (info in camadas) {
            val amostra = amostraDaArte(info.largura, info.altura, largura, altura)
            val camada = ArteDoApp.bitmap(contexto, info, amostra)
            if (camada == null) {
                bmp.recycle()
                return null
            }
            // Multiplicar pela cor mantém a luz e a sombra do neutro claro e o alfa da camada (o mesmo que
            // ColorFilter.tint(cor, BlendMode.Modulate) e o multiply da prévia em arte3d/previa.js).
            tinta.colorFilter = CamadasDosPersonagens.cor(perfil, info.tingir)?.let { PorterDuffColorFilter(it.toInt(), PorterDuff.Mode.MULTIPLY) }
            tela.drawBitmap(camada, null, destino, tinta)
        }
        bmp.prepareToDraw()
        val composto = Composto(bmp, bmp.asImageBitmap())
        cache.put(chave, composto)
        return composto.imagem
    }

    fun limparCache() = cache.evictAll()
}

/**
 * Desenha o [personagem] no [quadro] ocupando esta área (o quadro da arte cabe inteiro, com os pés em
 * [PersonagemDaArte.apoioNaCaixa] da altura). Devolve false se não deu (camada faltando ou ilegível).
 */
fun DrawScope.desenharPersonagemEmCamadas(
    contexto: Context,
    personagem: PersonagemDaArte,
    perfil: Perfil,
    quadro: QuadroDoPersonagem,
): Boolean {
    if (size.width < 1f || size.height < 1f) return true
    val manifesto = ArteDoApp.manifesto(contexto)
    val camadas = CamadasDosPersonagens.camadas(manifesto, personagem, perfil, quadro) ?: return false
    val ref = camadas.first()
    val caixa = CamadasDosPersonagens.caixaDoQuadro(ref, size.width, size.height, personagem.apoioNaCaixa)
    val larguraNaTela = ref.largura * caixa.escala
    val alturaNaTela = ref.altura * caixa.escala
    // Nunca maior que a arte (ampliar não ganha nitidez, só gasta memória).
    val largura = ceil(larguraNaTela).toInt().coerceIn(1, ref.largura)
    val altura = (largura.toFloat() * ref.altura / ref.largura).roundToInt().coerceAtLeast(1)
    val imagem = ComposicaoDosPersonagens.composto(contexto, camadas, perfil, largura, altura) ?: return false
    drawImage(
        image = imagem,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(largura, altura),
        dstOffset = IntOffset(caixa.esquerda.roundToInt(), caixa.topo.roundToInt()),
        dstSize = IntSize(larguraNaTela.roundToInt(), alturaNaTela.roundToInt()),
    )
    return true
}

/**
 * O personagem em camadas, vivo ou parado, ocupando a caixa inteira ([largura] x [altura]). Roupa, acessórios e
 * os chapéus da loja já vêm na pilha das camadas (a camada do chapéu está no lugar da cabeça do quadro); só um
 * [chapeu] sem camada (emoji desconhecido) vai por cima, pela âncora da cabeça do manifesto. Vivo: respira, pisca, acena em 4 quadros
 * ([acenando] e feliz), mexe a boca ([falando]) e comemora pulando com amassar e esticar (torcendo).
 * [reserva] desenha o personagem antigo se, mesmo assim, a composição falhar.
 */
@Composable
internal fun PersonagemEmCamadas(
    personagem: PersonagemDaArte,
    perfil: Perfil,
    humor: Humor,
    acenando: Boolean,
    falando: Boolean,
    vivo: Boolean,
    largura: Dp,
    altura: Dp,
    chapeu: String,
    /** Ritmo do piscar em ms (criança e irmão em ritmos diferentes, para não piscarem juntos). */
    ritmoDoPiscar: Int,
    /** Balanço de lado (o irmãozinho ginga); 0 = não ginga. */
    ginga: Float,
    reserva: DrawScope.(QuadroDoPersonagem) -> Unit,
) {
    val contexto = LocalContext.current
    val origem = TransformOrigin(0.5f, personagem.apoioNaCaixa)
    if (!vivo) {
        val quadro = QuadroDoPersonagem(
            bracos = CamadasDosPersonagens.bracos(humor, if (acenando) PoseDosBracos.TCHAU2 else null),
            humor = humor,
        )
        Box(Modifier.fillMaxSize()) {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .drawBehind { if (!desenharPersonagemEmCamadas(contexto, personagem, perfil, quadro)) reserva(quadro) },
            )
            ChapeuNaCabeca(personagem, chapeu, largura, altura)
        }
        return
    }
    val transicao = rememberInfiniteTransition(label = personagem.prefixo)
    val piscar = transicao.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(ritmoDoPiscar, easing = LinearEasing)),
        label = "piscar",
    )
    val respiro = transicao.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "respiro",
    )
    val balanco = if (ginga != 0f) {
        transicao.animateFloat(-1f, 1f, infiniteRepeatable(tween(1300), RepeatMode.Reverse), label = "ginga")
    } else {
        null
    }
    val tchau: State<Float>? = if (acenando && humor == Humor.FELIZ) {
        transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(720, easing = LinearEasing)), label = "tchau")
    } else {
        null
    }
    val boca: State<Float>? = if (falando) {
        transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(320, easing = LinearEasing)), label = "boca")
    } else {
        null
    }
    val festa: State<Float>? = if (humor == Humor.TORCENDO) {
        transicao.animateFloat(0f, 1f, infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "comemorar")
    } else {
        null
    }
    // O quadro só muda quando o olho fecha ou abre, o braço troca de quadro ou a boca abre ou fecha: o desenho
    // é refeito nessas horas (e não a cada quadro da animação).
    val quadro = remember(humor, piscar, tchau, boca) {
        derivedStateOf {
            QuadroDoPersonagem(
                bracos = CamadasDosPersonagens.bracos(humor, tchau?.let { CamadasDosPersonagens.quadroDoTchau(it.value) }),
                humor = humor,
                olhosFechados = piscar.value > 0.96f,
                bocaAberta = (boca?.value ?: 0f) > 0.5f,
            )
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                val pulo = festa?.let { CamadasDosPersonagens.pulo(it.value) }
                if (pulo != null) {
                    translationY = -pulo.subida * size.height
                    scaleX = pulo.escalaX
                    scaleY = pulo.escalaY
                } else {
                    // Respiração: o corpo sobe e desce bem de leve, a partir dos pés.
                    scaleY = respiro.value * (if (balanco != null) 1f + (balanco.value + 1f) * 0.006f else 1f)
                }
                if (balanco != null) rotationZ = balanco.value * ginga
                transformOrigin = origem
            },
    ) {
        Spacer(
            Modifier
                .fillMaxSize()
                .drawBehind {
                    val q = quadro.value
                    if (!desenharPersonagemEmCamadas(contexto, personagem, perfil, q)) reserva(q)
                },
        )
        ChapeuNaCabeca(personagem, chapeu, largura, altura)
    }
}

/** Um chapéu sem camada 3D (emoji) na cabeça do personagem em camadas, pela âncora `topoDaCabeca` do manifesto. */
@Composable
private fun ChapeuNaCabeca(personagem: PersonagemDaArte, chapeu: String, largura: Dp, altura: Dp) {
    if (chapeu.isEmpty()) return
    val contexto = LocalContext.current
    val cabeca = remember(personagem) { ArteDoApp.info(contexto, CamadasDosPersonagens.cabeca(personagem)) } ?: return
    val lugar = CamadasDosPersonagens.lugarDoChapeu(cabeca) ?: return
    val caixa = CamadasDosPersonagens.caixaDoQuadro(cabeca, largura.value, altura.value, personagem.apoioNaCaixa)
    val larguraDoChapeu = (lugar.largura * caixa.escala).dp
    val fonte = larguraDoChapeu * 0.82f
    val base = caixa.y(lugar.base).dp
    val centro = caixa.x(lugar.centroX).dp
    val densidade = LocalDensity.current
    Box(
        Modifier
            .offset(x = centro - larguraDoChapeu / 2, y = base - fonte * 1.05f)
            .size(larguraDoChapeu, fonte * 1.1f),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            chapeu,
            fontSize = with(densidade) { fonte.toSp() },
            lineHeight = with(densidade) { (fonte * 1.05f).toSp() },
        )
    }
}
