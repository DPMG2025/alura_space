package br.com.casinhadosaber.dominio

/**
 * Domínio visível (sugestão 6): diplomas por mundo e álbum de figurinhas.
 *
 * Tudo é calculado a partir de [EstadoJogo.estrelas] (o progresso que já é salvo), então funciona para
 * saves antigos sem nenhum campo novo: quem já tinha fases vencidas ganha as figurinhas e os diplomas na hora.
 * Nada é sorteado e nada se perde (as estrelas de uma fase só sobem).
 */

/** Diploma de um mundo completo: as 10 fases do mundo vencidas (com pelo menos 1 estrela). */
data class Diploma(val trilha: Trilha, val mundo: Int) {
    val id: String get() = "${trilha.name}:m$mundo"
    val titulo: String get() = "Diploma do ${Mundos.todos[mundo].titulo}"

    /** Texto do diploma (com o nome da criança, se houver). */
    fun texto(nome: String?): String = FrasesComNome.montar(
        "{nome} completou o ${Mundos.todos[mundo].titulo} no caminho ${trilha.titulo}!",
        "Você completou o ${Mundos.todos[mundo].titulo} no caminho ${trilha.titulo}!",
        nome,
    )
}

/**
 * Figurinha do álbum. Cada mundo tem 11 por caminho: uma por fase vencida com 3 estrelas ([dourada] false,
 * [fase] é o número da fase) e a dourada do chefão, ganha ao vencer a 10ª fase do mundo com qualquer estrela.
 */
data class Figurinha(val trilha: Trilha, val mundo: Int, val posicao: Int, val emoji: String, val dourada: Boolean) {
    /** Fase que dá esta figurinha (para a dourada, a fase do chefão). */
    val fase: Int get() = mundo * Mundos.FASES_POR_MUNDO + if (dourada) Mundos.FASES_POR_MUNDO - 1 else posicao
    val id: String get() = if (dourada) "${trilha.name}:m$mundo:ouro" else "${trilha.name}:$fase"

    /** Como a criança ganha esta figurinha (para mostrar na silhueta da figurinha que falta). */
    val comoGanhar: String get() = if (dourada) "Vença o desafio final do mundo" else "Ganhe 3 estrelas na fase ${fase + 1}"
}

/** Uma página do álbum: um mundo de um caminho, com as figurinhas e quais já foram ganhas. */
data class PaginaDoAlbum(val trilha: Trilha, val mundo: Int, val figurinhas: List<Figurinha>, val ganhas: Set<String>) {
    val titulo: String get() = Mundos.todos[mundo].titulo
    fun ganhou(f: Figurinha): Boolean = f.id in ganhas
    val quantasGanhas: Int get() = figurinhas.count { it.id in ganhas }
    val completa: Boolean get() = quantasGanhas == figurinhas.size
}

object Conquistas {
    const val FIGURINHAS_POR_PAGINA = Mundos.FASES_POR_MUNDO + 1

    /** Dez figuras do tema de cada mundo (a mesma para os quatro caminhos; a moldura muda com o caminho). */
    private val temas: List<List<String>> = listOf(
        listOf("🌷", "🌻", "🌹", "🐝", "🦋", "🐞", "🐛", "🌼", "🍄", "🐌"),
        listOf("🌳", "🦉", "🦊", "🐿️", "🦌", "🍂", "🌲", "🐻", "🦔", "🌰"),
        listOf("🐚", "🦀", "⛱️", "🌊", "🐢", "🍉", "🩴", "⚓", "🪣", "☀️"),
        listOf("⛰️", "🏔️", "🐐", "🦅", "🌄", "⛺", "🥾", "🪨", "🌨️", "🦙"),
        listOf("🚀", "🪐", "🌙", "⭐", "☄️", "🛸", "🌍", "🔭", "🛰️", "🌌"),
        listOf("🐄", "🐷", "🐑", "🐔", "🐴", "🚜", "🌽", "🐐", "🥚", "🐓"),
        listOf("🐠", "🐙", "🐬", "🐳", "🦈", "🦞", "🐡", "🦑", "🐟", "🦐"),
        listOf("🐒", "🦜", "🐍", "🐆", "🦥", "🌴", "🐸", "🦍", "🍌", "🐊"),
        listOf("🐧", "❄️", "⛄", "🦭", "🧊", "🛷", "🧤", "🎿", "🐋", "🧣"),
        listOf("🎪", "🤡", "🎈", "🎠", "🦁", "🐘", "🎩", "🎭", "🍿", "🎟️"),
        listOf("🐫", "🌵", "🦂", "🐪", "🏜️", "🦎", "🌞", "🏺", "🧭", "🐾"),
        listOf("🏙️", "🚌", "🚕", "🚦", "🏥", "🏫", "🚒", "🚓", "🏪", "🚲"),
        listOf("🏰", "👑", "🐉", "🛡️", "🗝️", "💎", "🦄", "🪄", "📜", "🔔"),
        listOf("🦕", "🦖", "🥚", "🌋", "🦴", "🌿", "🐢", "🦎", "🐊", "🌴"),
        listOf("🏝️", "🦜", "💰", "🗺️", "⛵", "🪙", "⚓", "🥥", "🐚", "🔦"),
        listOf("🍭", "🍬", "🍫", "🧁", "🍩", "🍪", "🍰", "🍦", "🍮", "🎂"),
        listOf("🍄", "🧚", "🦄", "🌈", "✨", "🐇", "🦋", "🌙", "🌸", "🪄"),
        listOf("🌋", "🔥", "🪨", "🦎", "💎", "♨️", "🐉", "☁️", "🌶️", "🌄"),
        listOf("☁️", "🌈", "🎈", "🪁", "🕊️", "⛅", "🌤️", "🦢", "🌬️", "⭐"),
        listOf("💎", "🔮", "🦇", "🪨", "🕯️", "🔦", "⛏️", "💍", "🌟", "🗿"),
        listOf("🎡", "🎢", "🎠", "🍿", "🎈", "🍦", "🎯", "🎪", "🧸", "🎟️"),
        listOf("🤖", "⚙️", "🔋", "💡", "🔧", "🛸", "📡", "🕹️", "💻", "🔩"),
        listOf("🦁", "🦒", "🦓", "🐘", "🦏", "🐆", "🦛", "🌳", "🐃", "🦩"),
        listOf("🌈", "🎨", "🖍️", "🟥", "🟧", "🟨", "🟩", "🟦", "🟪", "🦚"),
        listOf("🌟", "⭐", "🌠", "👑", "🏰", "🌙", "✨", "🔭", "🎆", "🏆"),
    )

    private fun venceu(estrelas: Map<String, Int>, trilha: Trilha, fase: Int): Boolean =
        (estrelas[EstadoJogo.chave(trilha, fase)] ?: 0) >= 1

    // ------------------------------------------------------------------ diplomas

    /** O mundo [mundo] do caminho [trilha] está completo (as 10 fases com pelo menos 1 estrela)? */
    fun mundoCompleto(estrelas: Map<String, Int>, trilha: Trilha, mundo: Int): Boolean {
        val m = Mundos.todos.getOrNull(mundo) ?: return false
        return (m.primeiraFase..m.ultimaFase).all { venceu(estrelas, trilha, it) }
    }

    /** Todos os diplomas ganhos, por caminho e mundo, na ordem. */
    fun diplomas(estrelas: Map<String, Int>): List<Diploma> =
        Trilha.entries.flatMap { t -> Mundos.todos.indices.filter { mundoCompleto(estrelas, t, it) }.map { Diploma(t, it) } }

    /** Diplomas que apareceram entre o progresso [antes] e [depois] (para comemorar ao voltar de uma fase). */
    fun diplomasNovos(antes: Map<String, Int>, depois: Map<String, Int>): List<Diploma> =
        diplomas(depois) - diplomas(antes).toSet()

    // ------------------------------------------------------------------ álbum

    /** As 11 figurinhas da página do mundo [mundo] no caminho [trilha]. */
    fun figurinhasDoMundo(trilha: Trilha, mundo: Int): List<Figurinha> {
        val m = mundo.coerceIn(0, Mundos.todos.lastIndex)
        val emojis = temas[m]
        return emojis.mapIndexed { i, e -> Figurinha(trilha, m, i, e, dourada = false) } +
            Figurinha(trilha, m, Mundos.FASES_POR_MUNDO, Mundos.todos[m].emoji, dourada = true)
    }

    /** Ganhou a figurinha? Fase com 3 estrelas, ou chefão vencido para a dourada. */
    fun ganhou(estrelas: Map<String, Int>, f: Figurinha): Boolean =
        if (f.dourada) venceu(estrelas, f.trilha, f.fase) else (estrelas[EstadoJogo.chave(f.trilha, f.fase)] ?: 0) >= 3

    /** Ids de todas as figurinhas ganhas (os quatro caminhos). */
    fun figurinhasGanhas(estrelas: Map<String, Int>): Set<String> =
        Trilha.entries.flatMapTo(mutableSetOf()) { t ->
            Mundos.todos.indices.flatMap { figurinhasDoMundo(t, it) }.filter { ganhou(estrelas, it) }.map { it.id }
        }

    /** As 25 páginas do álbum de um caminho. */
    fun album(trilha: Trilha, estrelas: Map<String, Int>): List<PaginaDoAlbum> {
        val ganhas = figurinhasGanhas(estrelas)
        return Mundos.todos.indices.map { PaginaDoAlbum(trilha, it, figurinhasDoMundo(trilha, it), ganhas) }
    }

    /** Figurinhas ganhas entre [antes] e [depois] (para a tela de prêmio mostrar a figurinha nova). */
    fun figurinhasNovas(antes: Map<String, Int>, depois: Map<String, Int>): List<Figurinha> {
        val jaTinha = figurinhasGanhas(antes)
        return Trilha.entries.flatMap { t -> Mundos.todos.indices.flatMap { figurinhasDoMundo(t, it) } }
            .filter { it.id !in jaTinha && ganhou(depois, it) }
    }

    /** Total possível no álbum inteiro (4 caminhos, 25 mundos, 11 por mundo). */
    val totalDeFigurinhas: Int get() = Trilha.entries.size * Mundos.todos.size * FIGURINHAS_POR_PAGINA
}
