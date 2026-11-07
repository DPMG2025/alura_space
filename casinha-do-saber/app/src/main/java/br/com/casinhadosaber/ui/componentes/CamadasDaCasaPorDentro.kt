package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Andar
import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Estrutura
import br.com.casinhadosaber.dominio.Movel
import br.com.casinhadosaber.dominio.PlantaDaCasa

// Casa por dentro com a arte 3D (PRD visual 3D, Fase 6; ids e regras em docs/visual-3d/relatorios/fase6a.md).
// Kotlin puro (sem Android nem Compose): o teste de unidade confere o alinhamento contra o manifesto de verdade.

/** Um ponto da planta: [x] e [y] em ladrilhos, [z] em unidades de altura (a parede tem 3). */
data class PontoDaPlanta(val x: Float, val y: Float, val z: Float = 0f)

/** Onde uma imagem da arte cai na tela, em pixels (canto de cima à esquerda e tamanho). */
data class LugarDaArte(val esquerda: Float, val topo: Float, val largura: Float, val altura: Float) {
    val direita get() = esquerda + largura
    val baixo get() = topo + altura
}

/**
 * As contas da projeção isométrica 2:1 (as mesmas de [Projecao], que as usa): azimute de 45 graus e elevação de
 * 30 graus, com uma unidade de altura medindo [UNIDADE_Z] da largura do ladrilho.
 */
object GradeIsometrica {
    const val UNIDADE_Z = 0.56f

    fun x(ladrilho: Float, origemX: Float, gx: Float, gy: Float): Float = origemX + (gx - gy) * (ladrilho / 2f)

    fun y(ladrilho: Float, origemY: Float, gx: Float, gy: Float, z: Float = 0f): Float =
        origemY + (gx + gy) * (ladrilho / 4f) - z * (ladrilho * UNIDADE_Z)
}

/**
 * Tudo o que muda o fundo fixo de um andar (laje, pisos, tapete, paredes tingidas, papel de parede, janelas, porta
 * e acabamento), que vira UM bitmap por andar. Igual = mesmo bitmap.
 */
data class AparenciaDoAndar(
    val andar: Andar,
    /** Cor (ARGB) de cada cômodo do andar, para tingir a parede por multiplicação. */
    val cores: Map<Comodo, Long>,
    /** Papel de parede da loja: 0 nenhum, 1 listras, 2 bolinhas, 3 estrelinhas (desenhado no app, por cima da parede). */
    val papel: Int = 0,
    val janelas: Boolean = false,
    val porta: Boolean = false,
    val corPorta: Int = 0,
    val noite: Boolean = false,
    /** Onde está o tapete (a referência dele), ou nulo se o andar não tem tapete. */
    val tapete: PontoDaPlanta? = null,
) {
    companion object {
        fun de(estado: EstadoJogo, andar: Andar, moveis: List<Movel>, noite: Boolean = false): AparenciaDoAndar = AparenciaDoAndar(
            andar = andar,
            cores = PlantaDaCasa.comodosDo(andar).associateWith { estado.corDo(it) },
            papel = estado.papelDeParede,
            janelas = estado.temEstrutura(Estrutura.JANELAS),
            porta = andar == Andar.DE_BAIXO && estado.temEstrutura(Estrutura.PORTA),
            corPorta = estado.corPorta,
            noite = noite,
            tapete = moveis.firstOrNull { it.noChao && it.andar == andar }?.let { CamadasDaCasaPorDentro.referenciaDoMovel(it) },
        )
    }
}

/** Grupos do fundo do andar, na ordem de desenho (cada um tem a sua reserva vetorial no app). */
enum class GrupoDaCasca { LAJE, PISO, TAPETE, PAREDE, PAPEL, JANELA, PORTA, ACABAMENTO }

/**
 * Uma peça do fundo do andar: o [id] da arte (nulo no papel de parede, que é desenhado no app), a [referencia] da
 * planta que cai no apoio da imagem, a [cor] para multiplicar (paredes) e, conforme o grupo, o [comodo] ou o
 * [indice] da janela.
 */
data class CascaDoAndar(
    val id: String?,
    val grupo: GrupoDaCasca,
    val referencia: PontoDaPlanta,
    val cor: Long? = null,
    val comodo: Comodo? = null,
    val indice: Int = 0,
)

object CamadasDaCasaPorDentro {
    const val PREFIXO = "dentro_"

    /** Ladrilho das imagens em dp (o manifesto também traz em `iso.ladrilho`). Com a escala 2, 128 px. */
    const val LADRILHO_DA_ARTE_DP = 64

    /** Canto (0, 0, 0) do andar: a referência das cascas (laje, piso, parede, acabamento). */
    val ORIGEM = PontoDaPlanta(0f, 0f, 0f)

    /** Centro (em x) das janelas retas de cada andar, as mesmas da fachada. */
    fun janelasRetas(andar: Andar): List<Float> = if (andar == Andar.DE_CIMA) listOf(3.22f) else listOf(2.1f, 6.05f)

    /** Meia largura das janelas retas, em ladrilhos (o vidro; a moldura passa 0,08 de cada lado). */
    const val MEIA_JANELA = 0.5f

    /** Base das janelas retas (a moldura) e da janela redonda do banheiro. */
    const val BASE_DA_JANELA = 1.32f
    const val BASE_DA_JANELA_REDONDA = 1.55f

    /** Centro da janela redonda: em cima do lugar de sempre da banheira. */
    val centroDaJanelaRedonda: Float get() = PlantaDaCasa.porId.getValue("banheira").centroX

    /** Referência da porta da rua, na parede x = 0 da sala. */
    val referenciaDaPorta: PontoDaPlanta get() = PontoDaPlanta(0f, (PlantaDaCasa.portaDaRua.x + PlantaDaCasa.portaDaRua.y) / 2f, 0f)

    fun nomeDoAndar(a: Andar) = if (a == Andar.DE_BAIXO) "de_baixo" else "de_cima"

    fun nomeDoComodo(c: Comodo) = c.name.lowercase()

    /** Pixels da arte por ladrilho (`iso.ladrilho` x escala do manifesto). */
    fun ladrilhoDaArte(info: InfoDaArte, escala: Int): Float = ((info.numeroIso("ladrilho") ?: LADRILHO_DA_ARTE_DP.toDouble()) * escala).toFloat()

    /** Fator da imagem para a tela: o ladrilho da tela em pixels dividido pelo ladrilho da arte (128 px). */
    fun fator(ladrilhoNaTela: Float, info: InfoDaArte, escala: Int): Float = ladrilhoNaTela / ladrilhoDaArte(info, escala)

    /**
     * Onde a imagem [info] cai na tela: o canto em `Projecao.ponto(referencia) - apoio x k`, com o tamanho da imagem
     * vezes k (k = [fator]). [origemX], [origemY] são o canto (0, 0) do chão do andar na tela.
     */
    fun lugar(info: InfoDaArte, escala: Int, ladrilho: Float, origemX: Float, origemY: Float, referencia: PontoDaPlanta): LugarDaArte {
        val k = fator(ladrilho, info, escala)
        val px = GradeIsometrica.x(ladrilho, origemX, referencia.x, referencia.y)
        val py = GradeIsometrica.y(ladrilho, origemY, referencia.x, referencia.y, referencia.z)
        return LugarDaArte(px - info.apoio.x * k, py - info.apoio.y * k, info.largura * k, info.altura * k)
    }

    /** Uma âncora da imagem já na tela (para a imagem desenhada em [lugar]); nula se a imagem não tem essa âncora. */
    fun ancora(info: InfoDaArte, lugar: LugarDaArte, nome: String): Pair<Float, Float>? {
        val a = info.ancoras[nome] ?: return null
        val k = lugar.largura / info.largura
        return (lugar.esquerda + a.x * k) to (lugar.topo + a.y * k)
    }

    /** O id da imagem de um móvel da planta ([aberto]: a geladeira e o guarda-roupa abertos). */
    fun idDoMovel(m: Movel, aberto: Boolean = false): String = when (m.id) {
        "escada" -> "dentro_escada"
        "escadaDeCima" -> "dentro_escada_de_cima"
        "geladeira" -> if (aberto) "dentro_geladeira_aberta" else "dentro_geladeira"
        "guardaroupa" -> if (aberto) "dentro_guardaroupa_aberto" else "dentro_guardaroupa"
        else -> PREFIXO + m.id
    }

    /** Móveis que abrem (e precisam das duas imagens). */
    fun abre(m: Movel): Boolean = m.id == "geladeira" || m.id == "guardaroupa"

    /**
     * A referência de um móvel: o centro da base da pegada, `(centroX, centroY, z)`; pendurados na parede (quadro e
     * espelho), `(centroX, 0, z)`. Um móvel arrastado usa a mesma imagem com a referência no lugar novo.
     */
    fun referenciaDoMovel(m: Movel): PontoDaPlanta =
        if (m.naParede) PontoDaPlanta(m.centroX, 0f, m.z) else PontoDaPlanta(m.centroX, m.centroY, m.z)

    /** Pedaço da parede interna (um ladrilho): referência no meio da base. */
    fun referenciaDaParedeInterna(x0: Float, y0: Float, x1: Float, y1: Float): PontoDaPlanta = PontoDaPlanta((x0 + x1) / 2f, (y0 + y1) / 2f, 0f)

    const val PAREDE_INTERNA = "dentro_parede_interna"

    /** Lâmpada do cômodo (o fio sobe 0,88 na imagem): no meio do cômodo, a 2,62; no banheiro, mais à direita. */
    fun luzDo(c: Comodo): PontoDaPlanta =
        if (c == Comodo.BANHEIRO) PontoDaPlanta(7.6f, 2.0f, 2.85f) else PlantaDaCasa.centroDo(c).let { PontoDaPlanta(it.x, it.y, 2.62f) }

    fun idDaLampada(acesa: Boolean) = if (acesa) "dentro_lampada_acesa" else "dentro_lampada_apagada"

    /**
     * O fundo do andar, de trás para a frente (a ordem do manifesto: laje 0, pisos 1, paredes 2, janelas e porta 2,5,
     * acabamento 3). O tapete entra depois dos pisos e o papel de parede (do app) logo depois das paredes.
     */
    fun cascas(ap: AparenciaDoAndar): List<CascaDoAndar> = buildList {
        val andar = nomeDoAndar(ap.andar)
        val comodos = PlantaDaCasa.comodosDo(ap.andar)
        add(CascaDoAndar("dentro_laje_$andar", GrupoDaCasca.LAJE, ORIGEM))
        for (c in comodos) add(CascaDoAndar("dentro_piso_${nomeDoComodo(c)}", GrupoDaCasca.PISO, ORIGEM, comodo = c))
        ap.tapete?.let { add(CascaDoAndar("dentro_tapete", GrupoDaCasca.TAPETE, it, comodo = Comodo.SALA)) }
        for (c in comodos) add(CascaDoAndar("dentro_parede_${nomeDoComodo(c)}", GrupoDaCasca.PAREDE, ORIGEM, ap.cores[c], c))
        if (ap.papel in 1..3) add(CascaDoAndar(null, GrupoDaCasca.PAPEL, ORIGEM, indice = ap.papel))
        if (ap.janelas) {
            val noite = if (ap.noite) "_noite" else ""
            janelasRetas(ap.andar).forEachIndexed { i, cx ->
                add(CascaDoAndar("dentro_janela$noite", GrupoDaCasca.JANELA, PontoDaPlanta(cx, 0f, BASE_DA_JANELA), indice = i))
            }
            if (ap.andar == Andar.DE_CIMA) {
                add(
                    CascaDoAndar(
                        "dentro_janela_redonda$noite", GrupoDaCasca.JANELA,
                        PontoDaPlanta(centroDaJanelaRedonda, 0f, BASE_DA_JANELA_REDONDA), indice = -1,
                    ),
                )
            }
        }
        if (ap.porta) add(CascaDoAndar("dentro_porta_${ap.corPorta.coerceIn(0, 4)}", GrupoDaCasca.PORTA, referenciaDaPorta))
        add(CascaDoAndar("dentro_acabamento_$andar", GrupoDaCasca.ACABAMENTO, ORIGEM))
    }

    /** Todos os ids que a casa por dentro pode pedir (para o teste conferir o manifesto). */
    fun idsPossiveis(): Set<String> = buildSet {
        for (a in Andar.entries) {
            for (noite in listOf(false, true)) for (cor in 0..4) {
                val ap = AparenciaDoAndar(a, emptyMap(), 1, janelas = true, porta = a == Andar.DE_BAIXO, corPorta = cor, noite = noite, tapete = PontoDaPlanta(0f, 0f))
                cascas(ap).mapNotNullTo(this) { it.id }
            }
        }
        for (m in PlantaDaCasa.moveis) {
            add(idDoMovel(m))
            if (abre(m)) add(idDoMovel(m, aberto = true))
        }
        add(PAREDE_INTERNA)
        add(idDaLampada(true))
        add(idDaLampada(false))
    }
}
