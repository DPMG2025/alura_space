package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.ParteDoCorpo
import br.com.casinhadosaber.dominio.Perfil
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

/*
 * Escolha das camadas 3D dos personagens (PRD visual 3D, seção 3.2; relatório da Fase 1A) e as contas das
 * animações. Kotlin puro (sem Android nem Compose): o teste de unidade confere tudo contra o manifesto real.
 * A referência de escolha é `arte3d/previa.js` (camadasDaCrianca / camadasDoIrmao).
 */

enum class Humor { FELIZ, PENSANDO, SURPRESO, TORCENDO }

/**
 * Proporção largura/altura da criança: a do quadro da arte 3D em camadas (170 x 260 dp, ver
 * `relatorios/fase1a.md`). O desenho antigo (reserva) tem a sua, `PROPORCAO_DESENHO_CRIANCA`, e fica centrado.
 */
const val PROPORCAO_CRIANCA = 170f / 260f

/**
 * Proporção largura/altura do irmãozinho: a do recorte do quadro da arte 3D (130 x 196 dp, mesma escala da
 * criança). O desenho antigo (reserva, mais gordinho) tem a sua, `PROPORCAO_DESENHO_IRMAO`.
 */
const val PROPORCAO_IRMAO = 130f / 196f

/**
 * Os dois personagens em camadas. [prefixo] é o começo dos ids no manifesto. [apoioNaCaixa] é a altura (fração
 * da caixa do componente) onde ficam os pés: o quadro renderizado tem uma margem embaixo dos pés (para a sombra)
 * e, sem esse ajuste, o personagem pareceria flutuar acima do lugar onde as telas o põem.
 */
enum class PersonagemDaArte(val prefixo: String, val apoioNaCaixa: Float) {
    CRIANCA("crianca", 0.95f),
    IRMAO("irmao", 0.94f),
}

/** Poses dos braços (uma camada cada): parado, tchau em 4 quadros, torcendo e pensando. */
enum class PoseDosBracos(val sufixo: String) {
    PARADO("parado"),
    TCHAU1("tchau1"),
    TCHAU2("tchau2"),
    TCHAU3("tchau3"),
    TCHAU4("tchau4"),
    TORCENDO("torcendo"),
    PENSANDO("pensando"),
}

/** Um quadro do personagem: braços, humor (boca e sobrancelhas), olhos fechados (piscar) e boca aberta (falar). */
data class QuadroDoPersonagem(
    val bracos: PoseDosBracos,
    val humor: Humor,
    val olhosFechados: Boolean = false,
    val bocaAberta: Boolean = false,
)

/** Um pulo com amassar e esticar: [subida] em fração da altura do personagem e a escala de cada eixo. */
data class Pulo(val subida: Float, val escalaX: Float, val escalaY: Float)

/**
 * Onde o personagem fica dentro de uma caixa: o quadro da arte (em pixels da imagem) vezes [escala], com o
 * canto de cima à esquerda em ([esquerda], [topo]). Vale em qualquer unidade (px ou dp): é a da caixa.
 */
data class CaixaDoQuadro(val esquerda: Float, val topo: Float, val escala: Float) {
    fun x(px: Float): Float = esquerda + px * escala
    fun y(px: Float): Float = topo + px * escala
}

/** Chapéu da loja (emoji) em pixels do quadro: centro, base (a aba) e largura. */
data class LugarDoChapeu(val centroX: Float, val base: Float, val largura: Float)

object CamadasDosPersonagens {
    /** `Perfil.estiloCabelo` 0, 1, 2 para cada gênero, como nos ids do manifesto. */
    val ESTILOS_MENINA = listOf("liso", "cacheado", "trancas")
    val ESTILOS_MENINO = listOf("liso", "cacheado", "espetado")

    /** Os 4 quadros do tchau, indo e voltando: 1, 2, 3, 4, 3, 2. */
    private val SEQUENCIA_DO_TCHAU = listOf(
        PoseDosBracos.TCHAU1, PoseDosBracos.TCHAU2, PoseDosBracos.TCHAU3,
        PoseDosBracos.TCHAU4, PoseDosBracos.TCHAU3, PoseDosBracos.TCHAU2,
    )

    fun estiloDoCabelo(perfil: Perfil): String {
        val estilos = if (perfil.genero == Genero.MENINA) ESTILOS_MENINA else ESTILOS_MENINO
        return estilos[perfil.estiloCabelo.coerceIn(0, estilos.lastIndex)]
    }

    /**
     * A criança usa as camadas 3D quando está com a roupa de sempre e sem acessórios: trajes da loja e
     * acessórios ainda não têm camadas (Fase 7), então nesses casos fica o desenho antigo. O chapéu (emoji)
     * funciona com os dois.
     */
    fun usaCamadas(perfil: Perfil): Boolean =
        Loja.trajeDe(perfil) == null && ParteDoCorpo.entries.none { Loja.acessorioDe(perfil, it) != null }

    /** Braços para o humor: torcendo e pensando têm pose própria; feliz acenando dá tchau ([quadroDoTchau]). */
    fun bracos(humor: Humor, quadroDoTchau: PoseDosBracos? = null): PoseDosBracos = when (humor) {
        Humor.TORCENDO -> PoseDosBracos.TORCENDO
        Humor.PENSANDO -> PoseDosBracos.PENSANDO
        Humor.FELIZ -> quadroDoTchau ?: PoseDosBracos.PARADO
        Humor.SURPRESO -> PoseDosBracos.PARADO
    }

    /** O quadro do tchau numa [fase] do ciclo (0 a 1). */
    fun quadroDoTchau(fase: Float): PoseDosBracos {
        val f = fase - floor(fase)
        val i = (f * SEQUENCIA_DO_TCHAU.size).toInt().coerceIn(0, SEQUENCIA_DO_TCHAU.lastIndex)
        return SEQUENCIA_DO_TCHAU[i]
    }

    private fun nome(humor: Humor): String = when (humor) {
        Humor.FELIZ -> "feliz"
        Humor.TORCENDO -> "torcendo"
        Humor.PENSANDO -> "pensando"
        Humor.SURPRESO -> "surpreso"
    }

    private fun olhos(q: QuadroDoPersonagem): String = if (q.olhosFechados) {
        "fechado"
    } else {
        when (q.humor) {
            Humor.FELIZ -> "aberto"
            Humor.TORCENDO -> "alegre"
            Humor.PENSANDO -> "pensando"
            Humor.SURPRESO -> "surpreso"
        }
    }

    /** Falando, a boca alterna entre a do humor e a aberta (surpreso). */
    private fun boca(q: QuadroDoPersonagem): String = if (q.bocaAberta) "surpreso" else nome(q.humor)

    /** Ids das camadas da criança (sem ordem; [empilhar] ordena pelo manifesto). */
    fun idsDaCrianca(perfil: Perfil, q: QuadroDoPersonagem): List<String> {
        val menina = perfil.genero == Genero.MENINA
        val g = if (menina) "menina" else "menino"
        val estilo = estiloDoCabelo(perfil)
        val ids = mutableListOf(
            "crianca_sombra",
            "crianca_cabelo_tras_${g}_$estilo",
            "crianca_cabelo_brilho_tras_${g}_$estilo",
            "crianca_corpo",
            "crianca_pes_$g",
            "crianca_roupa_$g",
            "crianca_roupa_${g}_estampa",
            "crianca_cabeca",
            "crianca_boca_${boca(q)}",
            "crianca_olhos_${olhos(q)}${if (menina) "_cilios" else ""}",
            "crianca_sobrancelhas_${nome(q.humor)}",
            "crianca_cabelo_frente_${g}_$estilo",
            "crianca_cabelo_brilho_${g}_$estilo",
            "crianca_bracos_${q.bracos.sufixo}",
        )
        if (menina && estilo == "trancas") ids += "crianca_enfeite_trancas"
        // O laço da menina (coques e cacheado) some com chapéu ou acessório no cabelo, como no desenho antigo.
        val semNadaNaCabeca = perfil.chapeu.isEmpty() && Loja.acessorioDe(perfil, ParteDoCorpo.CABELO) == null
        if (menina && estilo != "trancas" && semNadaNaCabeca) ids += "crianca_laco"
        return ids
    }

    /** Ids das camadas do irmãozinho: mesma pele e cabelo da criança, gênero de `perfil.irmao`. */
    fun idsDoIrmao(perfil: Perfil, q: QuadroDoPersonagem): List<String> {
        val menina = perfil.irmao.genero == Genero.MENINA
        val g = if (menina) "menina" else "menino"
        val ids = mutableListOf(
            "irmao_sombra",
            "irmao_corpo",
            "irmao_pes_$g",
            "irmao_roupa_$g",
            "irmao_cabeca",
            "irmao_boca_${boca(q)}",
            "irmao_olhos_${olhos(q)}${if (menina) "_cilios" else ""}",
            "irmao_sobrancelhas_${nome(q.humor)}",
            "irmao_cabelo_$g",
            "irmao_cabelo_brilho_$g",
            "irmao_bracos_${q.bracos.sufixo}",
        )
        if (menina) ids += "irmao_laco"
        return ids
    }

    fun ids(personagem: PersonagemDaArte, perfil: Perfil, q: QuadroDoPersonagem): List<String> = when (personagem) {
        PersonagemDaArte.CRIANCA -> idsDaCrianca(perfil, q)
        PersonagemDaArte.IRMAO -> idsDoIrmao(perfil, q)
    }

    /** As camadas na ordem de desenho (de trás para a frente), ou nulo se alguma faltar no manifesto. */
    fun empilhar(manifesto: ManifestoDaArte, ids: List<String>): List<InfoDaArte>? {
        val camadas = ids.map { manifesto.imagens[it] ?: return null }
        return camadas.sortedBy { it.ordem }
    }

    /** Todos os quadros que as animações podem pedir (para conferir de uma vez que nada falta). */
    fun todosOsQuadros(): List<QuadroDoPersonagem> =
        PoseDosBracos.entries.flatMap { b ->
            Humor.entries.flatMap { h ->
                listOf(false, true).flatMap { olhos -> listOf(false, true).map { boca -> QuadroDoPersonagem(b, h, olhos, boca) } }
            }
        }

    /** true se o manifesto tem todas as camadas de que este personagem pode precisar (senão, desenho antigo). */
    fun disponivel(manifesto: ManifestoDaArte, personagem: PersonagemDaArte, perfil: Perfil): Boolean =
        manifesto.imagens.containsKey(cabeca(personagem)) &&
            todosOsQuadros().all { empilhar(manifesto, ids(personagem, perfil, it)) != null }

    /** Id da camada da cabeça (a que tem as âncoras do rosto e do chapéu). */
    fun cabeca(personagem: PersonagemDaArte): String = "${personagem.prefixo}_cabeca"

    /** Cor (ARGB) para tingir uma camada por multiplicação, ou nulo para desenhar como está. */
    fun cor(perfil: Perfil, tingir: String?): Long? = when (tingir) {
        "pele" -> Aparencia.peles.getOrElse(perfil.pele) { Aparencia.peles[0] }
        "cabelo" -> Aparencia.cabelos.getOrElse(perfil.cabelo) { Aparencia.cabelos[0] }
        "roupa" -> perfil.corDaRoupa
        else -> null
    }

    /**
     * O quadro [info] dentro de uma caixa de [largura] x [altura]: cabe inteiro (como ContentScale.Fit), centrado
     * na horizontal, com os pés (o apoio) em [apoioNaCaixa] da altura.
     */
    fun caixaDoQuadro(info: InfoDaArte, largura: Float, altura: Float, apoioNaCaixa: Float): CaixaDoQuadro {
        val escala = min(largura / info.largura, altura / info.altura)
        return CaixaDoQuadro(
            esquerda = largura / 2f - info.apoio.x * escala,
            topo = altura * apoioNaCaixa - info.apoio.y * escala,
            escala = escala,
        )
    }

    /** Raio da cabeça em pixels do quadro: do centro ao topo (âncoras `cabeca` e `topoDaCabeca`). */
    fun raioDaCabeca(cabeca: InfoDaArte): Float? {
        val centro = cabeca.ancoras["cabeca"] ?: return null
        val topo = cabeca.ancoras["topoDaCabeca"] ?: return null
        return (centro.y - topo.y).toFloat().takeIf { it > 0f }
    }

    /**
     * Chapéu pela âncora da cabeça: centrado no centro da cabeça, com a aba um pouco abaixo do topo (cobre a
     * calota e o alto do cabelo, sem chegar aos olhos) e quase da largura da cabeça.
     */
    fun lugarDoChapeu(cabeca: InfoDaArte): LugarDoChapeu? {
        val centro = cabeca.ancoras["cabeca"] ?: return null
        val topo = cabeca.ancoras["topoDaCabeca"] ?: return null
        val r = raioDaCabeca(cabeca) ?: return null
        return LugarDoChapeu(centroX = centro.x.toFloat(), base = topo.y + r * 0.5f, largura = r * 1.9f)
    }

    /**
     * Pulo de comemorar com amassar e esticar, numa [fase] do ciclo (0 a 1): agacha (amassa), sai esticado,
     * sobe em parábola, chega esticado, amassa ao pousar e volta ao normal. As escalas seguem quadros-chave
     * ligados em linha reta (sem saltos) e quase não mudam o volume (escalaX x escalaY perto de 1).
     */
    fun pulo(fase: Float): Pulo {
        val f = fase - floor(fase)
        val subida = if (f in INICIO_DO_VOO..FIM_DO_VOO) {
            (sin(((f - INICIO_DO_VOO) / (FIM_DO_VOO - INICIO_DO_VOO)) * PI.toFloat()) * ALTURA_DO_PULO).coerceAtLeast(0f)
        } else {
            0f
        }
        return Pulo(subida, interpolar(CHAVES_X, f), interpolar(CHAVES_Y, f))
    }

    private const val INICIO_DO_VOO = 0.18f
    private const val FIM_DO_VOO = 0.72f
    private const val ALTURA_DO_PULO = 0.14f

    /** (fase, escala) dos quadros-chave. */
    private val CHAVES_Y = listOf(0f to 1f, 0.12f to 0.88f, 0.2f to 1.08f, 0.45f to 1f, 0.68f to 1.06f, 0.78f to 0.9f, 0.9f to 1.02f, 1f to 1f)
    private val CHAVES_X = listOf(0f to 1f, 0.12f to 1.1f, 0.2f to 0.94f, 0.45f to 1f, 0.68f to 0.95f, 0.78f to 1.09f, 0.9f to 0.99f, 1f to 1f)

    private fun interpolar(chaves: List<Pair<Float, Float>>, f: Float): Float {
        for (i in 1 until chaves.size) {
            val (f1, v1) = chaves[i]
            if (f <= f1) {
                val (f0, v0) = chaves[i - 1]
                val t = if (f1 > f0) (f - f0) / (f1 - f0) else 1f
                return v0 + (v1 - v0) * t
            }
        }
        return chaves.last().second
    }
}
