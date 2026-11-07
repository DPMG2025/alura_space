package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.dominio.ItemCasa
import br.com.casinhadosaber.dominio.ItensDaCasa
import kotlin.math.abs

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

    /**
     * A imagem 3D da silhueta do próximo presente (o cartão com cadeado do baú vazio), ou nulo (fica o desenho
     * antigo): a peça da casa (`item_<id>`), a coisa da área ([para]), a figura do emoji (a obra: a vaca da
     * fazendinha, a boia da piscina...) e, por último, o ícone da área da obra.
     */
    fun silhuetaK2(manifesto: ManifestoDaArte, item: ItemCasa): String? {
        if (item.area == null) {
            val id = CamadasDaCasa.idDoItem(item.id, noite = false)
            return id.takeIf { it in manifesto.imagens } ?: FigurasDaPergunta.idPara(manifesto, item.icone)
        }
        para(manifesto, item)?.let { return it.id }
        FigurasDaPergunta.idPara(manifesto, item.icone)?.let { return it }
        return if (item.obra) IconesDaCasaK2.daArea(item.area).takeIf { it in manifesto.imagens } else null
    }

    /**
     * Onde a coisa aparece na ilha: a posição guardada; enquanto ela está no lugar de partida (a grade do domínio,
     * que a criança ainda não mexeu), um desvio pequeno e fixo (vem do id) espalha as coisas para não parecer
     * fileira. A criança arrastou, vale exatamente onde ela soltou. Fica sempre dentro da ilha.
     */
    fun posicaoNaIlhaK2(item: ItemCasa, x: Float, y: Float): Pair<Float, Float> {
        val naPartida = abs(x - item.x) < 0.001f && abs(y - item.y) < 0.001f
        if (!naPartida) return x to y
        val h = item.id.hashCode().let { it xor (it ushr 15) } and 0x7FFFFFFF
        val dx = ((h % 11) - 5) / 5f * 0.035f
        val dy = (((h / 11) % 9) - 4) / 4f * 0.022f
        // O pé não sobe acima do horizonte (o que é de chão continua no chão) nem sai pelas bordas.
        val noChao = y > CasaQueCresce.HORIZONTE
        val ny = (y + dy).coerceIn(if (noChao) CasaQueCresce.HORIZONTE + 0.06f else 0.08f, 0.98f)
        return (x + dx).coerceIn(0.05f, 0.95f) to ny
    }
}
