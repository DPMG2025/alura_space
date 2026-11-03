package br.com.casinhadosaber.dominio

/**
 * Mascote da casa (sugestão 2). O mascote só cresce: o estágio vem das fases vencidas (cada fase conta
 * uma vez, mesmo repetida), então nunca diminui, nunca fica doente, triste nem com fome.
 */
enum class EspecieMascote(val nome: String, val emoji: String) {
    CACHORRINHO("Cachorrinho", "🐶"),
    GATINHO("Gatinho", "🐱"),
    DRAGAOZINHO("Dragãozinho", "🐲"),
}

/** Estágios de crescimento: [fasesMinimas] fases vencidas (somando os 4 caminhos) para chegar a ele. */
enum class EstagioMascote(val nome: String, val fasesMinimas: Int, val escala: Float) {
    BEBE("Bebê", 0, 0.6f),
    PEQUENO("Pequeno", 10, 0.72f),
    CRIANCA("Criança", 40, 0.84f),
    GRANDE("Grande", 100, 0.94f),
    CAMPEAO("Campeão", 250, 1f),
}

/** Enfeite do mascote, liberado com [fasesMinimas] fases vencidas. Nunca se compra e nunca se perde. */
enum class AcessorioMascote(val nome: String, val emoji: String, val fasesMinimas: Int) {
    LACINHO("Lacinho", "🎀", 3),
    BONE("Boné", "🧢", 15),
    OCULOS("Óculos", "👓", 30),
    CACHECOL("Cachecol", "🧣", 60),
    MOCHILA("Mochila", "🎒", 120),
    MEDALHA("Medalha", "🏅", 200),
    COROA("Coroa", "👑", 400),
    CAPA("Capa de estrelas", "✨", 700),
}

/**
 * O mascote salvo. [especie] null = a criança ainda não escolheu. [nome] é o que a criança deu ao mascote
 * (vazio usa o nome da espécie). [acessorio] é o nome do [AcessorioMascote] em uso, ou vazio.
 */
data class Mascote(
    val especie: EspecieMascote? = null,
    val nome: String = "",
    val acessorio: String = "",
) {
    val escolhido: Boolean get() = especie != null

    /** O nome que aparece e é falado: o escolhido pela criança, ou o da espécie. */
    val nomeParaMostrar: String get() = nome.trim().ifEmpty { especie?.nome.orEmpty() }

    val acessorioEmUso: AcessorioMascote? get() = AcessorioMascote.entries.firstOrNull { it.name == acessorio }

    companion object {
        const val TAMANHO_MAXIMO_DO_NOME = 14

        /** Tira o separador do save e limita o tamanho. */
        fun limparNome(nome: String): String = nome.replace("|", "").replace("\n", " ").trim().take(TAMANHO_MAXIMO_DO_NOME)

        /** "ESPECIE|nome|ACESSORIO". */
        fun paraTexto(m: Mascote): String = listOf(m.especie?.name.orEmpty(), limparNome(m.nome), m.acessorio).joinToString("|")

        /** Saves antigos (sem a chave) ou texto estragado viram "ainda não escolheu". */
        fun deTexto(s: String?): Mascote {
            val p = s.orEmpty().split("|")
            val especie = EspecieMascote.entries.firstOrNull { it.name == p.getOrNull(0) } ?: return Mascote()
            val acessorio = p.getOrNull(2).orEmpty().takeIf { a -> AcessorioMascote.entries.any { it.name == a } }.orEmpty()
            return Mascote(especie, limparNome(p.getOrNull(1).orEmpty()), acessorio)
        }
    }
}

object Crescimento {
    /** O estágio para [fasesVencidas] (negativos contam como zero). */
    fun estagio(fasesVencidas: Int): EstagioMascote =
        EstagioMascote.entries.last { fasesVencidas.coerceAtLeast(0) >= it.fasesMinimas }

    /** O próximo estágio, ou null no último. */
    fun proximo(fasesVencidas: Int): EstagioMascote? = EstagioMascote.entries.getOrNull(estagio(fasesVencidas).ordinal + 1)

    /** Quantas fases faltam para crescer de novo (0 no último estágio). */
    fun faltamParaCrescer(fasesVencidas: Int): Int = proximo(fasesVencidas)?.let { it.fasesMinimas - fasesVencidas.coerceAtLeast(0) } ?: 0

    /** De 0 a 1: quanto do caminho até o próximo estágio já foi feito (1 no último). */
    fun progressoNoEstagio(fasesVencidas: Int): Float {
        val atual = estagio(fasesVencidas)
        val prox = proximo(fasesVencidas) ?: return 1f
        return (fasesVencidas.coerceAtLeast(0) - atual.fasesMinimas).toFloat() / (prox.fasesMinimas - atual.fasesMinimas)
    }

    fun acessoriosLiberados(fasesVencidas: Int): List<AcessorioMascote> =
        AcessorioMascote.entries.filter { fasesVencidas >= it.fasesMinimas }

    /** O mascote cresceu entre [antes] e [depois] fases vencidas? (para a festa de crescimento) */
    fun cresceu(antes: Int, depois: Int): Boolean = estagio(depois).ordinal > estagio(antes).ordinal

    /** Acessórios que acabaram de ser liberados entre [antes] e [depois]. */
    fun acessoriosNovos(antes: Int, depois: Int): List<AcessorioMascote> =
        acessoriosLiberados(depois) - acessoriosLiberados(antes).toSet()

    /** O que o mascote fala ao tocar nele: sempre alegre. [indice] varia a frase sem sorteio. */
    fun fala(mascote: Mascote, fasesVencidas: Int, nomeDaCrianca: String?, indice: Int = 0): String {
        val m = mascote.nomeParaMostrar.ifEmpty { "Seu amigo" }
        val frases = listOf(
            FrasesComNome.montar("Oi, {nome}! Eu sou $m!", "Oi! Eu sou $m!", nomeDaCrianca),
            "Vamos aprender mais uma coisa hoje?",
            when (proximo(fasesVencidas)) {
                null -> "Eu já sou campeão! Obrigado por cuidar de mim!"
                else -> "Cada fase nova me ajuda a crescer!"
            },
            FrasesComNome.montar("Eu gosto muito de brincar com você, {nome}!", "Eu gosto muito de brincar com você!", nomeDaCrianca),
        )
        return frases[Math.floorMod(indice, frases.size)]
    }
}
