package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa

// Que imagem 3D mostra cada coisa das áreas novas da casa (Fase 8 do PRD visual 3D). As áreas ainda não têm
// arte própria: aproveita a de outras fases quando ela é a mesma coisa. Kotlin puro (testável na JVM).

/**
 * A imagem 3D de uma coisa de área: [id] da arte e se ela é um item da casa ([comoItem]: `item_<id>`, posto pelo
 * ponto de apoio do manifesto, já com sombra de contato) ou uma figura das perguntas (`fig_*`, cabe na caixa).
 * [bicho]: o cachorrinho e o gatinho 3D, que viram para onde andam.
 */
data class ArteDaCoisa(val id: String, val comoItem: Boolean, val bicho: Boolean = false)

object ArteDasAreas {
    /** Tipos que são o mesmo bicho 3D da casa (de qualquer emoji): o cachorrinho e o gatinho. */
    private val BICHOS = mapOf("cachorro" to "item_cachorro", "gato" to "item_gato")

    /**
     * A arte de [item] (uma coisa de área, não a obra), ou nulo (fica o emoji ou o desenho antigo):
     * 1. cachorro e gato: o bicho 3D da casa;
     * 2. o mesmo tipo e o mesmo emoji de uma das 40 peças da casa (que não seja estrutura): `item_<tipo>`;
     * 3. o emoji tem figura 3D das perguntas: `fig_*` ([FigurasDaPergunta]).
     */
    fun para(manifesto: ManifestoDaArte, item: ItemCasa): ArteDaCoisa? {
        if (item.area == null || item.obra) return null
        val tipo = CasaQueCresce.tipoDe(item.id)
        BICHOS[tipo]?.let { id -> if (id in manifesto.imagens) return ArteDaCoisa(id, comoItem = true, bicho = true) }
        val daCasa = ItensDaCasa.ordem.firstOrNull { it.id == tipo }
        if (daCasa != null && !daCasa.ehEstrutura &&
            FigurasDaPergunta.normalizar(daCasa.icone) == FigurasDaPergunta.normalizar(item.icone)
        ) {
            val id = CamadasDaCasa.idDoItem(tipo, noite = false)
            if (id in manifesto.imagens) return ArteDaCoisa(id, comoItem = true)
        }
        val figura = FigurasDaPergunta.idPara(manifesto, item.icone) ?: return null
        return ArteDaCoisa(figura, comoItem = false)
    }
}
