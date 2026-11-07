package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.AcessorioMascote
import br.com.casinhadosaber.dominio.EspecieMascote
import br.com.casinhadosaber.dominio.Trilha

// Peças da Fase 8 (grupo A: entrada, mascote, descanso e conquistas) que não dependem do Android: os ids da arte
// 3D (`mascote_*`, `tela_*`, `ui_mao_*`, ver `docs/visual-3d/relatorios/fase8a.md`) e onde cada enfeite do
// mascote fica em cima da imagem, pelas âncoras do manifesto.

/** As poses dos mascotes 3D (todas com a mesma moldura, para trocar sem pular). */
enum class PoseDoMascote(val sufixo: String, val sufixoDaCoruja: String) {
    PARADO("parado", "parada"),
    FALANDO("falando", "falando"),
    FELIZ("feliz", "feliz"),
    DORMINDO("dormindo", "dormindo"),
    ;

    companion object {
        /** A pose de quem está [dormindo], [falando] (bico ou boca aberta) ou [feliz] (pulando); senão, parado. */
        fun de(feliz: Boolean = false, falando: Boolean = false, dormindo: Boolean = false): PoseDoMascote = when {
            dormindo -> DORMINDO
            falando -> FALANDO
            feliz -> FELIZ
            else -> PARADO
        }
    }
}

object PecasDoMascote {
    /** `mascote_<bicho>_<pose>` (cachorrinho, gatinho, dragaozinho). */
    fun id(especie: EspecieMascote, pose: PoseDoMascote): String = "mascote_${especie.name.lowercase()}_${pose.sufixo}"

    /** A Corujinha Professora: `mascote_coruja_<pose>` (parada, falando, feliz, dormindo). */
    fun idDaCoruja(pose: PoseDoMascote): String = "mascote_coruja_${pose.sufixoDaCoruja}"

    val idsDaArte: List<String> =
        EspecieMascote.entries.flatMap { e -> PoseDoMascote.entries.map { id(e, it) } } + PoseDoMascote.entries.map(::idDaCoruja)

    /**
     * Onde fica o meio do enfeite [acessorio] numa imagem de mascote com [info], como fração da imagem (0 a 1).
     * Usa as âncoras `cabeca` e `topoDaCabeca` e o apoio (os pés); sem âncora, uma posição média dos bichos.
     * A arte 3D ainda não tem os enfeites (pendência da 8A): o app põe o emoji do enfeite neste ponto.
     */
    fun lugarDoEnfeite(acessorio: AcessorioMascote, info: InfoDaArte): Pair<Float, Float> {
        val (cx, cy) = info.ancoras["cabeca"]?.let(info::fracao) ?: (0.53f to 0.39f)
        val (tx, ty) = info.ancoras["topoDaCabeca"]?.let(info::fracao) ?: (cx to cy - 0.18f)
        val (_, py) = info.fracao(info.apoio)
        val corpo = py - cy
        val (x, y) = when (acessorio) {
            AcessorioMascote.LACINHO -> tx + 0.12f to ty + 0.04f
            AcessorioMascote.BONE -> tx to ty
            AcessorioMascote.COROA -> tx to ty - 0.03f
            AcessorioMascote.OCULOS -> cx to cy + 0.01f
            AcessorioMascote.CACHECOL -> cx to cy + corpo * 0.4f
            AcessorioMascote.MEDALHA -> cx to cy + corpo * 0.58f
            AcessorioMascote.MOCHILA -> cx + 0.17f to cy + corpo * 0.55f
            AcessorioMascote.CAPA -> cx - 0.22f to cy + corpo * 0.45f
        }
        return x.coerceIn(0.05f, 0.95f) to y.coerceIn(0.05f, 0.95f)
    }
}

/** Ids da arte das telas de entrada, conquistas e descanso. */
object PecasDasTelas {
    const val MAO_TOQUE = "ui_mao_toque"
    const val MAO_APERTANDO = "ui_mao_toque_apertando"
    const val ILHA = "item_terreno"
    const val NUVEM_GRANDE = "ceu_nuvem_1"
    const val NUVEM_PEQUENA = "ceu_nuvem_2"
    const val DIPLOMA_ROLO = "tela_diploma_rolo"
    const val MEDALHA = "tela_medalha"
    const val SELO = "tela_selo"
    const val FIGURINHA_DOURADA = "tela_figurinha_dourada"
    const val FIGURINHA_VAZIA = "tela_figurinha_vazia"
    const val LUA_DORMINDO = "tela_lua_dormindo"
    const val ESTRELA_NOITE = "tela_estrela_noite"
    const val ZZZ = "tela_zzz"

    /** O fundo da figurinha: a borda na cor do caminho, a dourada do chefão ou o lugar vazio pontilhado. */
    fun figurinha(trilha: Trilha, dourada: Boolean, ganha: Boolean): String = when {
        !ganha -> FIGURINHA_VAZIA
        dourada -> FIGURINHA_DOURADA
        else -> "tela_figurinha_${trilha.name.lowercase()}"
    }

    val idsDaArte: List<String> = listOf(
        MAO_TOQUE, MAO_APERTANDO, ILHA, NUVEM_GRANDE, NUVEM_PEQUENA, DIPLOMA_ROLO, MEDALHA, SELO,
        FIGURINHA_DOURADA, FIGURINHA_VAZIA, LUA_DORMINDO, ESTRELA_NOITE, ZZZ,
    ) + Trilha.entries.map { figurinha(it, dourada = false, ganha = true) }
}
