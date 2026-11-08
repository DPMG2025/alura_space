package br.com.casinhadosaber.dominio

/**
 * Figurinhas e diplomas do Livro de Colorir (Fase 10). Como em [Conquistas], tudo sai de [EstadoJogo.coloridos]
 * (os desenhos terminados, que já são salvos): saves antigos ganham as figurinhas e os diplomas na hora, nada é
 * sorteado e nada se perde.
 */

/** Figurinha de um desenho terminado, ou a dourada da coleção completa ([desenhoId] null). */
data class FigurinhaDoLivro(val colecao: String, val desenhoId: String?, val emoji: String) {
    val dourada: Boolean get() = desenhoId == null
    val id: String get() = if (desenhoId == null) "livro:$colecao:ouro" else "livro:$desenhoId"

    /** Como ganhar (para a silhueta da figurinha que falta). */
    val comoGanhar: String
        get() = if (desenhoId == null) "Termine todos os desenhos da coleção"
        else "Termine o desenho ${Desenhos.porId[desenhoId]?.titulo ?: ""}".trimEnd()
}

/** Uma página do álbum do Livro de Colorir: uma coleção, com uma figurinha por desenho e a dourada no fim. */
data class PaginaDoLivro(val colecao: String, val nome: String, val emoji: String, val figurinhas: List<FigurinhaDoLivro>, val ganhas: Set<String>) {
    fun ganhou(f: FigurinhaDoLivro): Boolean = f.id in ganhas
    val quantasGanhas: Int get() = figurinhas.count { it.id in ganhas }
    val completa: Boolean get() = quantasGanhas == figurinhas.size
}

/** Diploma de uma coleção do Livro de Colorir inteira terminada. */
data class DiplomaDeColecao(val colecao: String) {
    val id: String get() = "livro:$colecao"
    val nome: String get() = ConquistasDoLivro.nomeSemEmoji(colecao)
    val emoji: String get() = ConquistasDoLivro.emojiDaColecao(colecao)
    val titulo: String get() = "Diploma da coleção $nome"

    fun texto(nome: String?): String = FrasesComNome.montar(
        "{nome} pintou toda a coleção ${this.nome} do Livro de Colorir!",
        "Você pintou toda a coleção ${this.nome} do Livro de Colorir!",
        nome,
    )
}

object ConquistasDoLivro {
    fun emojiDaColecao(colecao: String): String = Desenhos.nomeDaColecao(colecao).substringBefore(' ')

    fun nomeSemEmoji(colecao: String): String = Desenhos.nomeDaColecao(colecao).substringAfter(' ')

    /** As figurinhas da coleção: uma por desenho, na ordem do livro, e a dourada. */
    fun figurinhasDaColecao(colecao: String): List<FigurinhaDoLivro> =
        Desenhos.daColecao(colecao).map { FigurinhaDoLivro(colecao, it.id, it.emoji) } + FigurinhaDoLivro(colecao, null, emojiDaColecao(colecao))

    private fun completa(colecao: String, coloridos: Map<String, Int>): Boolean {
        val dela = Desenhos.daColecao(colecao)
        return dela.isNotEmpty() && dela.all { it.id in coloridos }
    }

    fun ganhou(coloridos: Map<String, Int>, f: FigurinhaDoLivro): Boolean =
        if (f.desenhoId == null) completa(f.colecao, coloridos) else f.desenhoId in coloridos

    fun figurinhasGanhas(coloridos: Map<String, Int>): Set<String> =
        Desenhos.colecoes.flatMapTo(mutableSetOf()) { (c, _) -> figurinhasDaColecao(c).filter { ganhou(coloridos, it) }.map { it.id } }

    /** As páginas do álbum, uma por coleção, na ordem do livro. */
    fun paginas(coloridos: Map<String, Int>): List<PaginaDoLivro> {
        val ganhas = figurinhasGanhas(coloridos)
        return Desenhos.colecoes.map { (c, _) -> PaginaDoLivro(c, nomeSemEmoji(c), emojiDaColecao(c), figurinhasDaColecao(c), ganhas) }
    }

    val totalDeFigurinhas: Int get() = Desenhos.colecoes.sumOf { (c, _) -> Desenhos.daColecao(c).size + 1 }

    /** Diplomas das coleções completas, na ordem do livro. */
    fun diplomas(coloridos: Map<String, Int>): List<DiplomaDeColecao> =
        Desenhos.colecoes.filter { (c, _) -> completa(c, coloridos) }.map { DiplomaDeColecao(it.first) }

    /** Figurinhas ganhas entre [antes] e [depois] (para a tela de prêmio do desenho). */
    fun figurinhasNovas(antes: Map<String, Int>, depois: Map<String, Int>): List<FigurinhaDoLivro> {
        val jaTinha = figurinhasGanhas(antes)
        return Desenhos.colecoes.flatMap { (c, _) -> figurinhasDaColecao(c) }.filter { it.id !in jaTinha && ganhou(depois, it) }
    }
}
