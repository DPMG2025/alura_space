package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Estrutura

// Casa de fora em camadas 3D (PRD visual 3D, Fase 3; ids e ordem em docs/visual-3d/relatorios/fase3a.md).
// Kotlin puro (sem Android nem Compose): o teste de unidade confere a pilha contra o manifesto de verdade.

/**
 * Tudo o que muda o desenho fixo da casa (a pilha de camadas que vira um bitmap só): estruturas, cores da loja,
 * cores dos cômodos, papel de parede, noite, neve no telhado e o abajur (de noite os cômodos ficam mais claros).
 * Igual = mesmo bitmap composto.
 */
data class AparenciaDaCasa(
    val estruturas: Set<Estrutura>,
    /** Cor de cada cômodo (ARGB), para tingir o fundo dele. Sem cor, o fundo fica no neutro claro. */
    val coresDosComodos: Map<Comodo, Long> = emptyMap(),
    /** Índice em `Loja.coresDoTelhado` (0 a 5). */
    val corTelhado: Int = 0,
    /** Índice em `Loja.coresDaPorta` (0 a 4). */
    val corPorta: Int = 0,
    /** Papel de parede da loja: 0 = nenhum, 1 listras, 2 bolinhas, 3 estrelinhas. */
    val papel: Int = 0,
    val noite: Boolean = false,
    /** Neve no telhado (clima 2 com telhado). A chuva e a neve que caem são animadas por cima, fora da pilha. */
    val neveNoTelhado: Boolean = false,
    /** Com o abajur na casa, de noite os cômodos escurecem menos. */
    val abajur: Boolean = false,
) {
    fun tem(e: Estrutura) = e in estruturas

    companion object {
        fun de(estado: EstadoJogo, noite: Boolean): AparenciaDaCasa {
            val estruturas = Estrutura.entries.filterTo(LinkedHashSet()) { estado.temEstrutura(it) }
            return AparenciaDaCasa(
                estruturas = estruturas,
                coresDosComodos = Comodo.entries.associateWith { estado.corDo(it) },
                corTelhado = estado.corTelhado,
                corPorta = estado.corPorta,
                papel = estado.papelDeParede,
                noite = noite,
                neveNoTelhado = estado.clima == 2 && Estrutura.TELHADO in estruturas,
                abajur = noite && "abajur" in estado.colocados,
            )
        }
    }
}

/**
 * Uma camada da pilha: o [id] da arte, a [cor] para multiplicar (tingir o cômodo, escurecer de noite; nula = como
 * está) e a [estrutura] a que ela pertence (para a estrutura nova cair do céu separada do resto).
 */
data class CamadaDaCasa(val id: String, val cor: Long? = null, val estrutura: Estrutura? = null)

object CamadasDaCasa {
    /** Largura e alturas da arte da cena (800 x 920 px = 400 x 460 dp; o chão e o caminho descem até 1320 px). */
    const val LARGURA_DA_ARTE = 800
    const val ALTURA_DA_CENA = 920
    const val ALTURA_DO_CHAO = 1320

    /** De noite tudo fica azulado (multiplicar): o céu, a grama e a casa; as janelas acesas, não. */
    const val NOITE: Long = 0xFF8F98C8

    /** De noite, dentro de casa com o abajur: mais claro. */
    const val NOITE_COM_ABAJUR: Long = 0xFFC2C7E6

    /** Estrelas do céu da noite (duas camadas que se alternam para piscar). */
    val ESTRELAS = listOf("ceu_estrelas_1", "ceu_estrelas_2")

    /** Nuvens de enfeite que passeiam no céu de dia. */
    val NUVENS = listOf("ceu_nuvem_1", "ceu_nuvem_2", "ceu_nuvem_3")

    /** Nuvem cinza dos dias de chuva. */
    const val NUVEM_DE_CHUVA = "ceu_nuvem_chuva"

    /** A camada que cai (emendável na vertical), por clima: 1 chuva, 2 neve. */
    fun doClima(clima: Int): String? = when (clima) {
        1 -> "ceu_chuva"
        2 -> "ceu_neve"
        else -> null
    }

    val COMODOS = Comodo.entries.associateWith { "casa_comodo_" + it.name.lowercase() }

    fun idDoChao(terreno: Boolean) = if (terreno) "casa_etapa_1_terreno" else "casa_etapa_0_vazio"
    /** Telhado e porta pela cor da loja; um índice que não existe fica na primeira cor (como `getOrElse` do desenho antigo). */
    fun idDoTelhado(cor: Int) = "casa_etapa_3_telhado_" + (if (cor in 0 until CORES_DO_TELHADO) cor else 0)
    fun idDaPorta(cor: Int) = "casa_etapa_4_porta_" + (if (cor in 0 until CORES_DA_PORTA) cor else 0)
    fun idDoPapel(papel: Int): String? = if (papel in 1..3) "casa_papel_$papel" else null

    /** Quantas cores a arte tem (as mesmas de `Loja.coresDoTelhado` e `Loja.coresDaPorta`). */
    const val CORES_DO_TELHADO = 6
    const val CORES_DA_PORTA = 5

    /** Camadas que brilham de noite (não escurecem). */
    private val ACESAS = setOf("casa_janelas_noite", "casa_janela_do_telhado_noite")

    /**
     * A pilha fixa da casa para [a], já na ordem de desenho do manifesto (fase3a.md): chão, lago, caminho, cerca,
     * chaminé, cômodos tingidos, papel, janelas, porta, paredes, escada, telhado, janela do telhado e neve.
     * Sem o céu (degradê do app), as estrelas, as nuvens e o clima que cai, que são animados por cima ou por baixo.
     * Janelas, porta e escada só com paredes (como no desenho antigo); a chaminé só com telhado.
     */
    fun camadas(a: AparenciaDaCasa): List<CamadaDaCasa> {
        val noite = if (a.noite) NOITE else null
        val dentro = if (!a.noite) null else if (a.abajur) NOITE_COM_ABAJUR else NOITE
        val pilha = ArrayList<CamadaDaCasa>()
        fun por(id: String, e: Estrutura?, cor: Long? = noite) {
            pilha += CamadaDaCasa(id, if (id in ACESAS) null else cor, e)
        }
        por(idDoChao(a.tem(Estrutura.TERRENO)), null)
        if (a.tem(Estrutura.LAGO)) por("casa_lago", Estrutura.LAGO)
        if (a.tem(Estrutura.CAMINHO)) por("casa_caminho", Estrutura.CAMINHO)
        if (a.tem(Estrutura.CERCA)) por("casa_cerca", Estrutura.CERCA)
        val telhado = a.tem(Estrutura.TELHADO)
        if (telhado && a.tem(Estrutura.CHAMINE)) por("casa_chamine", Estrutura.CHAMINE)
        val paredes = a.tem(Estrutura.PAREDES)
        if (paredes) {
            for ((comodo, id) in COMODOS) {
                val cor = a.coresDosComodos[comodo]
                por(id, Estrutura.PAREDES, if (cor == null) dentro else if (dentro == null) cor else multiplicar(cor, dentro))
            }
            idDoPapel(a.papel)?.let { por(it, Estrutura.PAREDES, dentro) }
            if (a.tem(Estrutura.JANELAS)) por(if (a.noite) "casa_janelas_noite" else "casa_janelas", Estrutura.JANELAS)
            if (a.tem(Estrutura.PORTA)) por(idDaPorta(a.corPorta), Estrutura.PORTA)
            por("casa_etapa_2_paredes", Estrutura.PAREDES)
            if (a.tem(Estrutura.ESCADA)) por("casa_escada", Estrutura.ESCADA)
        }
        if (telhado) {
            por(idDoTelhado(a.corTelhado), Estrutura.TELHADO)
            if (a.noite) por("casa_janela_do_telhado_noite", Estrutura.TELHADO)
            if (a.neveNoTelhado) por("casa_neve_no_telhado", Estrutura.TELHADO)
        }
        return pilha
    }

    /**
     * As [camadas] com a informação do manifesto, em ordem de desenho (pela `ordem` do manifesto; empate fica na
     * ordem dada). Nulo se faltar alguma: aí a casa inteira fica no desenho antigo.
     */
    fun empilhar(manifesto: ManifestoDaArte, camadas: List<CamadaDaCasa>): List<Pair<CamadaDaCasa, InfoDaArte>>? {
        val comInfo = camadas.map { c -> c to (manifesto.imagens[c.id] ?: return null) }
        return comInfo.sortedBy { it.second.ordem }
    }

    /** true se a arte da casa está toda no manifesto: todas as pilhas possíveis, o céu e o clima. */
    fun disponivel(manifesto: ManifestoDaArte): Boolean = idsPossiveis().all { it in manifesto.imagens }

    /** Todos os ids que a casa de fora pode pedir à arte (pilhas, céu, clima e os itens soltos). */
    fun idsPossiveis(): Set<String> = buildSet {
        add(idDoChao(false)); add(idDoChao(true))
        addAll(listOf("casa_lago", "casa_caminho", "casa_cerca", "casa_chamine", "casa_janelas", "casa_janelas_noite"))
        addAll(listOf("casa_etapa_2_paredes", "casa_escada", "casa_janela_do_telhado_noite", "casa_neve_no_telhado"))
        addAll(COMODOS.values)
        for (p in 1..3) add("casa_papel_$p")
        for (c in 0 until CORES_DO_TELHADO) add(idDoTelhado(c))
        for (c in 0 until CORES_DA_PORTA) add(idDaPorta(c))
        addAll(ESTRELAS); addAll(NUVENS); add(NUVEM_DE_CHUVA)
        add(doClima(1)!!); add(doClima(2)!!)
    }

    /** Multiplica duas cores ARGB canal a canal (o mesmo que tingir duas vezes). */
    fun multiplicar(a: Long, b: Long): Long {
        fun canal(c: Long, s: Int) = ((c shr s) and 0xFF)
        var r = 0L
        for (s in listOf(24, 16, 8, 0)) r = r or ((canal(a, s) * canal(b, s) + 127) / 255 shl s)
        return r
    }

    // ------------------------------------------------------------------ itens soltos

    /** Id da arte de um item da casa (`item_<id do domínio>`); de noite o sol vira a lua. */
    fun idDoItem(id: String, noite: Boolean): String = if (noite && id == "sol") "item_sol_noite" else "item_$id"

    /**
     * A imagem do item é um pouco mais larga que a caixa do item (a arte tem uma folguinha em volta), como na
     * prévia do pipeline (`arte3d/previa_casa.js`).
     */
    const val FOLGA_DO_ITEM = 1.08f

    /** Itens que brilham sozinhos e não escurecem de noite (a lua, a estrela de campeão). */
    val ITENS_ACESOS = setOf("item_sol_noite", "item_estrela")

    /**
     * Onde desenhar a imagem de um item de [larguraDoItem] (em px) com o pé no ponto (0, 0): o ponto de apoio do
     * manifesto (meio da base) cai no pé. Devolve (esquerda, topo, largura, altura) em px.
     */
    fun lugarDoItem(info: InfoDaArte, larguraDoItem: Float): LugarNaCena {
        val k = larguraDoItem * FOLGA_DO_ITEM / info.largura
        return LugarNaCena(-info.apoio.x * k, -info.apoio.y * k, info.largura * k, info.altura * k)
    }
}

/** Um retângulo em pixels (relativo a um ponto de referência). */
data class LugarNaCena(val esquerda: Float, val topo: Float, val largura: Float, val altura: Float) {
    val direita get() = esquerda + largura
    val base get() = topo + altura
}
