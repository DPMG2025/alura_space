package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Gaveta
import br.com.casinhadosaber.dominio.PrateleiraDaGeladeira
import br.com.casinhadosaber.dominio.Trilha

// Peças do visual brinquedo 3D que não dependem do Android (testáveis na JVM): que ícone da arte entra no
// lugar de cada emoji da barra, os ids dos ícones dos caminhos e como o progresso vira coisas que se veem.

/** Um ícone da barra (arte 3D `icone_*`) e se ele aparece espelhado (a seta de voltar vira a de avançar). */
data class IconeDaBarra(val id: String, val espelhado: Boolean = false)

object IconesDaBarra {
    const val FECHAR = "icone_fechar"
    const val VOLTAR = "icone_voltar"
    const val SOM_LIGADO = "icone_som_ligado"
    const val SOM_DESLIGADO = "icone_som_desligado"
    const val AJUSTES = "icone_ajustes"
    const val PAIS = "icone_pais"
    const val CASA = "icone_casa"
    const val LOJA = "icone_loja"
    const val MAPA = "icone_mapa"
    const val ALBUM = "icone_album"
    const val ESTANTE = "icone_estante"
    const val ESPELHO = "icone_espelho"
    const val SOL = "icone_sol"
    const val LUA = "icone_lua"
    const val PINTAR = "icone_pintar"
    const val TOCAR = "icone_tocar"
    const val PARAR = "icone_parar"
    const val GRAVAR = "icone_gravar"
    const val OUVIR = "icone_ouvir"
    const val REPETIR = "icone_repetir"
    const val DESFAZER = "icone_desfazer"
    const val PAPEL_NOVO = "icone_papel_novo"
    const val LIXEIRA = "icone_lixeira"

    // Botões soltos da Fase 9C (lote 3 de arte).
    const val SUBIR = "icone_subir"
    const val DESCER = "icone_descer"
    const val BUSSOLA = "icone_bussola"
    const val DICA = "icone_dica"
    const val MARTELO = "icone_martelo"
    const val MARCADOR = "icone_marcador"
    const val VASSOURA = "icone_vassoura"
    const val BROTO = "icone_broto"
    const val ALFINETE = "icone_alfinete"
    const val ESCREVER = "icone_escrever"
    const val TREINO = "icone_no_treino"
    const val BANDEIRA = "icone_no_marco"

    /** O "✔" sobre fundo claro; sobre fundo colorido, use [CERTO_BRANCO] (não está em [porEmoji]). */
    const val CERTO = "icone_certo"
    const val CERTO_BRANCO = "icone_certo_branco"
    const val BAU = "icone_bau"

    /** Todos os ids que o [BotaoRedondo] pode pedir à arte. */
    val todos = listOf(
        FECHAR, VOLTAR, SOM_LIGADO, SOM_DESLIGADO, AJUSTES, PAIS, CASA, LOJA,
        MAPA, ALBUM, ESTANTE, ESPELHO, SOL, LUA, PINTAR, TOCAR, PARAR, GRAVAR, OUVIR, REPETIR, DESFAZER, PAPEL_NOVO, LIXEIRA,
        SUBIR, DESCER, BUSSOLA, DICA, MARTELO, MARCADOR, VASSOURA, BROTO, ALFINETE, ESCREVER, TREINO, BANDEIRA,
        CERTO, CERTO_BRANCO, BAU,
    )

    // Só emojis que o app já usa, escritos como nas telas (a lista do teste de emojis no emulador sai do código).
    private val porEmoji = mapOf(
        "✖️" to IconeDaBarra(FECHAR),
        "⬅️" to IconeDaBarra(VOLTAR), "◀️" to IconeDaBarra(VOLTAR),
        "➡️" to IconeDaBarra(VOLTAR, espelhado = true), "▶️" to IconeDaBarra(VOLTAR, espelhado = true),
        "🔊" to IconeDaBarra(SOM_LIGADO),
        "🔇" to IconeDaBarra(SOM_DESLIGADO),
        "⚙️" to IconeDaBarra(AJUSTES),
        "🔒" to IconeDaBarra(PAIS),
        "🏠" to IconeDaBarra(CASA),
        "🛒" to IconeDaBarra(LOJA), "🛍️" to IconeDaBarra(LOJA),
        "🗺️" to IconeDaBarra(MAPA), "📒" to IconeDaBarra(ALBUM), "📚" to IconeDaBarra(ESTANTE), "🪞" to IconeDaBarra(ESPELHO),
        "☀️" to IconeDaBarra(SOL), "🌙" to IconeDaBarra(LUA), "🎨" to IconeDaBarra(PINTAR),
        "⏹️" to IconeDaBarra(PARAR), "⏺️" to IconeDaBarra(GRAVAR), "👂" to IconeDaBarra(OUVIR), "🔁" to IconeDaBarra(REPETIR),
        "↩️" to IconeDaBarra(DESFAZER), "📄" to IconeDaBarra(PAPEL_NOVO), "🗑️" to IconeDaBarra(LIXEIRA),
        // Fase 9C: só botões soltos. Emojis que também são conteúdo (🎈 🍎 👕 ⚽...) mapeiam pelo enum/id
        // ([IconesDaCasaK2]), nunca aqui.
        "⬆️" to IconeDaBarra(SUBIR), "⬇️" to IconeDaBarra(DESCER), "🧭" to IconeDaBarra(BUSSOLA), "💡" to IconeDaBarra(DICA),
        "🔨" to IconeDaBarra(MARTELO), "🔖" to IconeDaBarra(MARCADOR), "🧹" to IconeDaBarra(VASSOURA), "🌱" to IconeDaBarra(BROTO),
        "📍" to IconeDaBarra(ALFINETE), "✏️" to IconeDaBarra(ESCREVER), "🏋️" to IconeDaBarra(TREINO), "🚩" to IconeDaBarra(BANDEIRA),
        "✔️" to IconeDaBarra(CERTO),
    ).mapKeys { it.key.replace("️", "") }

    /**
     * O ícone 3D que substitui [emoji] num botão da barra, ou nulo (o emoji continua, até a arte dele existir).
     * O seletor de variação (U+FE0F) é ignorado. "▶" com [descricao] de ouvir ou tocar é o "play" (icone_tocar), não "avançar".
     */
    fun para(emoji: String, descricao: String = ""): IconeDaBarra? {
        val limpo = emoji.replace("️", "").trim()
        if (limpo == "▶" && (descricao.startsWith("Ouvir") || descricao.startsWith("Tocar"))) return IconeDaBarra(TOCAR)
        return porEmoji[limpo]
    }
}

/**
 * Ícones 3D das abas e linhas da casa (Fase 9C, lote 3), escolhidos pelo enum ou pelo id, não pelo emoji:
 * 🎈 🍎 👕 🥕 ⚽ 🧸 também são conteúdo (com `fig_*`). Kotlin puro (testável na JVM).
 */
object IconesDaCasaK2 {
    /** Presente fechado 3D (o 🎁 do baú) e a moeda 3D (o 🪙). */
    const val PRESENTE = "premio_presente_fechado"
    const val MOEDA = "moeda"

    // Linhas do espelho (TelaPerfil).
    const val PELE = "icone_pele"
    const val COR_DO_CABELO = "icone_cor_do_cabelo"
    const val PENTEADO = "icone_penteado"
    const val ROUPA = "icone_roupa"

    /** O ícone de uma área da casa que cresce (pílulas dos lugares, cadeado da próxima): `icone_area_<id>`. */
    fun daArea(idDaArea: String): String = "icone_area_$idDaArea"

    /** O ícone da aba de uma gaveta do guarda-roupa e do espelho. */
    fun daGaveta(g: Gaveta): String = when (g) {
        Gaveta.ROUPAS -> "icone_roupas"
        Gaveta.CHAPEUS -> "icone_chapeus"
        Gaveta.ROSTO -> "icone_rosto"
        Gaveta.PESCOCO -> "icone_pescoco"
        Gaveta.COSTAS -> "icone_costas"
        Gaveta.MAO -> "icone_mao"
        Gaveta.PES -> "icone_pes"
        Gaveta.CABELO -> "icone_cabelo"
    }

    /** O ícone da aba de uma prateleira da geladeira. */
    fun daPrateleira(p: PrateleiraDaGeladeira): String = when (p) {
        PrateleiraDaGeladeira.FRUTAS -> "icone_frutas"
        PrateleiraDaGeladeira.LEITE -> "icone_leite"
        PrateleiraDaGeladeira.CONGELADOR -> "icone_congelador"
    }

    /** Os ids fixos desta tabela (as áreas à parte), para o teste conferir no manifesto. */
    val fixos: List<String> get() = listOf(PRESENTE, MOEDA, PELE, COR_DO_CABELO, PENTEADO, ROUPA) +
        Gaveta.entries.map(::daGaveta) + PrateleiraDaGeladeira.entries.map(::daPrateleira)
}

/** Id da arte 3D do ícone de um caminho (blocos 1 2 3, blocos A B C, globo, argolas). */
fun idDoIconeDaTrilha(trilha: Trilha): String = "icone_trilha_" + trilha.name.lowercase()

/** Ids das estrelas 3D do progresso (a grande do prêmio é `estrela3d`). */
const val ID_ESTRELA_CHEIA = "estrela_cheia"
const val ID_ESTRELA_VAZIA = "estrela_vazia"
const val ID_ESTRELA_BRILHANDO = "estrela_brilhando"

/** As cores das contas do progresso, na ordem de `CORES_DAS_CONTAS` da arte (`arte3d/src/objetos/interface.js`). */
enum class CorDaConta(val sufixo: String) {
    VERMELHA("vermelha"), LARANJA("laranja"), AMARELA("amarela"), VERDE("verde"), AZUL("azul"), ROXA("roxa");

    val id: String get() = "conta_$sufixo"

    companion object {
        const val VAZIA = "conta_vazia"
    }
}

/** Como fica cada conta da fileira. */
enum class EstadoDaConta { FEITA, ATUAL, FALTA }

/** Progresso como coisas que se veem (contas, estrelas), nunca "3/250". */
object ProgressoVisivel {
    /**
     * As [total] contas de uma fileira com [feitas] já feitas: as feitas, a da vez (se ainda faltar alguma e
     * [comAtual]) e as que faltam. Valores fora da faixa são presos nela.
     */
    fun contas(feitas: Int, total: Int, comAtual: Boolean = true): List<EstadoDaConta> {
        val n = total.coerceAtLeast(0)
        val f = feitas.coerceIn(0, n)
        return List(n) { i ->
            when {
                i < f -> EstadoDaConta.FEITA
                i == f && comAtual -> EstadoDaConta.ATUAL
                else -> EstadoDaConta.FALTA
            }
        }
    }

    /**
     * Quantas de [estrelas] estrelas acender para [vencidas] de [total] fases (por exemplo, 5 estrelas por caminho):
     * cada estrela vale uma fatia igual; a primeira acende já com a primeira fase vencida (a criança vê que andou),
     * e a última só com tudo vencido.
     */
    fun estrelasAcesas(vencidas: Int, total: Int, estrelas: Int): Int {
        if (total <= 0 || estrelas <= 0 || vencidas <= 0) return 0
        if (vencidas >= total) return estrelas
        val pelaFatia = (vencidas.toLong() * estrelas / total).toInt()
        return pelaFatia.coerceIn(1, estrelas - 1)
    }
}
