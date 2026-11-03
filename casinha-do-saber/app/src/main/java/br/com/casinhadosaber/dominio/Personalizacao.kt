package br.com.casinhadosaber.dominio

import kotlin.random.Random

/**
 * Personalização (sugestão 1): aniversário, temas favoritos e frases com o nome da criança.
 *
 * O nome da criança nunca fica escrito no código: vem só de [Perfil.nome], digitado no aparelho.
 * Quando o nome está vazio, as frases têm uma versão sem nome que soa igualmente natural.
 */

/** Dia e mês do aniversário (o ano não é guardado: não precisamos da idade, só da festa). */
data class Aniversario(val dia: Int, val mes: Int) {
    init {
        require(mes in 1..12 && dia in 1..Calendario.diasNoMes(mes, 2024)) { "Data de aniversário inválida" }
    }

    /** O aniversário cai neste dia? Quem nasceu em 29 de fevereiro comemora em 28 de fevereiro nos anos sem 29. */
    fun ehNoDia(data: Data): Boolean {
        if (data.mes != mes) return false
        if (dia == 29 && mes == 2 && !Calendario.bissexto(data.ano)) return data.dia == 28
        return data.dia == dia
    }

    companion object {
        /** Lê "dia/mes" (como é guardado). Texto vazio, estragado ou data impossível vira null. */
        fun deTexto(s: String?): Aniversario? {
            val p = s.orEmpty().split("/")
            if (p.size != 2) return null
            val d = p[0].toIntOrNull() ?: return null
            val m = p[1].toIntOrNull() ?: return null
            return runCatching { Aniversario(d, m) }.getOrNull()
        }

        fun paraTexto(a: Aniversario?): String = a?.let { "${it.dia}/${it.mes}" }.orEmpty()
    }
}

/**
 * Temas de que a criança mais gosta (no máximo [MAXIMO] no perfil). [emojis] são figuras que já existem no
 * vocabulário dos caminhos e combinam com o tema; [objetosExtras] entram só nas perguntas de contar quando o
 * tema foi escolhido (nunca mudam as fases de quem não escolheu nada).
 */
enum class TemaFavorito(val nome: String, val emoji: String, val emojis: Set<String>, val objetosExtras: List<Objeto> = emptyList()) {
    ANIMAIS(
        "Animais", "🐶",
        setOf("🐶", "🐱", "🐭", "🐰", "🦊", "🐻", "🐼", "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐒", "🐔", "🐧", "🐦", "🐤", "🐥",
            "🦆", "🦉", "🐴", "🐝", "🐛", "🦋", "🐌", "🐞", "🐢", "🐍", "🐘", "🦒", "🦓", "🐄", "🐖", "🐑", "🐐", "🐓", "🐇", "🦔", "🐿️",
            "🐟", "🐠", "🐬", "🐳", "🐙", "🦀", "🐊", "🐫", "🐪", "🦜", "🐕", "🐈", "🐁", "🦌", "🦩"),
    ),
    DINOSSAUROS(
        "Dinossauros", "🦕", setOf("🦕", "🦖", "🥚", "🌋", "🦴"),
        listOf(Objeto("🦕", "dinossauro", "dinossauros", false), Objeto("🦖", "tiranossauro", "tiranossauros", false)),
    ),
    ESPACO(
        "Espaço", "🚀", setOf("🚀", "⭐", "🌟", "🌙", "🪐", "☀️", "🛸", "👽", "🌍", "🌎", "☄️"),
        listOf(Objeto("🚀", "foguete", "foguetes", false), Objeto("⭐", "estrela", "estrelas", true), Objeto("🪐", "planeta", "planetas", false)),
    ),
    PRINCESAS_E_CASTELOS(
        "Princesas e castelos", "🏰", setOf("👑", "🏰", "👸", "🤴", "🦄", "💎", "🪄", "🌸", "🎀", "🐉"),
        listOf(Objeto("👑", "coroa", "coroas", true), Objeto("💎", "joia", "joias", true)),
    ),
    FUNDO_DO_MAR(
        "Fundo do mar", "🐠", setOf("🐟", "🐠", "🐡", "🐬", "🐳", "🐋", "🦈", "🐙", "🦀", "🦞", "🦐", "🐚", "🐢", "🌊", "⚓"),
        listOf(Objeto("🐠", "peixe colorido", "peixes coloridos", false), Objeto("🐙", "polvo", "polvos", false)),
    ),
    CARROS(
        "Carros", "🚗", setOf("🚗", "🚕", "🚙", "🚌", "🚓", "🚑", "🚒", "🚜", "🚲", "🚂", "🚁", "✈️", "🚀", "⛵", "🚢", "🛴", "🏎️"),
        listOf(Objeto("🚌", "ônibus", "ônibus", false)),
    ),
    COMIDAS(
        "Comidas", "🍓", setOf("🍎", "🍌", "🍇", "🍉", "🍓", "🍒", "🍐", "🍊", "🍋", "🥕", "🌽", "🍞", "🧀", "🥚", "🥛", "🍕", "🍰", "🧁",
            "🍦", "🍪", "🍫", "🍭", "🍬", "🍯", "🍔", "🥝", "🍍", "🥑", "🍅"),
        listOf(Objeto("🍌", "banana", "bananas", true), Objeto("🍪", "biscoito", "biscoitos", false)),
    ),
    MUSICA(
        "Música", "🎵", setOf("🎵", "🎶", "🎸", "🥁", "🎹", "🎺", "🎻", "🎤", "🔔", "🪕"),
        listOf(Objeto("🥁", "tambor", "tambores", false), Objeto("🎸", "violão", "violões", false)),
    ),
    ESPORTES(
        "Esportes", "⚽", setOf("⚽", "🏀", "🎾", "🏐", "🏈", "⚾", "🏆", "🥇", "🚲", "🛹", "🏊", "⛸️", "🥅"),
        listOf(Objeto("🏀", "bola de basquete", "bolas de basquete", true), Objeto("🎾", "bolinha de tênis", "bolinhas de tênis", true)),
    ),
    FAZENDA(
        "Fazenda", "🐄", setOf("🐄", "🐮", "🐷", "🐖", "🐑", "🐐", "🐔", "🐓", "🐤", "🐥", "🐣", "🦆", "🐴", "🐎", "🚜", "🌽", "🥚", "🥛", "🌾", "🐇"),
        listOf(Objeto("🐄", "vaquinha", "vaquinhas", true), Objeto("🐷", "porquinho", "porquinhos", false)),
    ),
    ;

    companion object {
        const val MAXIMO = 3

        /** Lê "ANIMAIS,ESPACO" (como é guardado): nomes desconhecidos somem, repetidos também, no máximo [MAXIMO]. */
        fun deTexto(s: String?): List<TemaFavorito> =
            s.orEmpty().split(",").mapNotNull { n -> entries.firstOrNull { it.name == n.trim() } }.distinct().take(MAXIMO)

        fun paraTexto(temas: List<TemaFavorito>): String = temas.distinct().take(MAXIMO).joinToString(",") { it.name }

        /** Liga ou desliga um tema na escolha do perfil. Com [MAXIMO] já escolhidos, um novo não entra. */
        fun alternar(atuais: List<TemaFavorito>, tema: TemaFavorito): List<TemaFavorito> = when {
            tema in atuais -> atuais - tema
            atuais.size >= MAXIMO -> atuais
            else -> atuais + tema
        }
    }
}

/**
 * Preferir o vocabulário dos temas favoritos, sem mudar a dificuldade: a escolha é sempre feita dentro da
 * mesma lista que a fase já usaria. Sem temas (ou sem nenhuma figura do tema na lista), o sorteio é
 * exatamente o de antes (mesma chamada ao [Random]), então fases e testes antigos não mudam.
 */
object PreferenciaDeTema {
    /** Chance de usar uma figura do tema quando há uma na lista (o resto continua variado). */
    const val CHANCE = 0.5

    fun emojisDe(temas: Collection<TemaFavorito>): Set<String> = temas.flatMapTo(mutableSetOf()) { it.emojis }

    /** Sorteia um item de [lista], preferindo (metade das vezes) os que têm figura de algum dos [temas]. */
    fun <T> escolher(lista: List<T>, temas: Collection<TemaFavorito>, random: Random, emojiDe: (T) -> String): T {
        if (temas.isEmpty()) return lista.random(random)
        val emojis = emojisDe(temas)
        val doTema = lista.filter { emojiDe(it) in emojis }
        if (doTema.isEmpty() || random.nextDouble() >= CHANCE) return lista.random(random)
        return doTema.random(random)
    }

    /**
     * Objeto para as perguntas de contar: sem temas, igual a antes ([Vocabulario.objetosParaContar]); com temas,
     * metade das vezes vem um objeto do tema (da lista de contar ou dos [TemaFavorito.objetosExtras]).
     */
    fun objetoParaContar(temas: Collection<TemaFavorito>, random: Random): Objeto {
        val base = Vocabulario.objetosParaContar
        if (temas.isEmpty()) return base.random(random)
        val emojis = emojisDe(temas)
        val doTema = (base.filter { it.emoji in emojis } + temas.flatMap { it.objetosExtras }).distinctBy { it.emoji }
        if (doTema.isEmpty() || random.nextDouble() >= CHANCE) return base.random(random)
        return doTema.random(random)
    }
}

/**
 * Frases do narrador com o nome da criança. Cada função recebe o nome (de [Perfil.nome]) e um índice
 * qualquer (um contador da tela) para variar a frase sem sorteio. Nome vazio usa a versão sem nome.
 */
object FrasesComNome {
    private const val N = "{nome}"

    private val elogios = listOf(
        "Muito bem, $N!" to "Muito bem!",
        "Isso mesmo, $N!" to "Isso mesmo!",
        "Você conseguiu, $N!" to "Você conseguiu!",
        "Que orgulho, $N!" to "Que orgulho!",
        "Mandou bem, $N!" to "Mandou bem!",
        "Parabéns, $N! Você pensou direitinho." to "Parabéns! Você pensou direitinho.",
    )

    private val incentivos = listOf(
        "Vamos tentar de novo, $N? Você consegue!" to "Vamos tentar de novo? Você consegue!",
        "Quase, $N! Olhe com calma." to "Quase! Olhe com calma.",
        "Errar faz parte de aprender, $N." to "Errar faz parte de aprender.",
    )

    private val chegadas = listOf(
        "Oi, $N! Que bom ver você!" to "Oi! Que bom ver você!",
        "Olá, $N! Vamos brincar e aprender?" to "Olá! Vamos brincar e aprender?",
        "$N chegou! A casinha ficou feliz!" to "Você chegou! A casinha ficou feliz!",
    )

    /** O nome limpo (sem espaços nas pontas), ou vazio. */
    fun nomeLimpo(nome: String?): String = nome.orEmpty().trim()

    /** Troca {nome} pelo nome; sem nome, usa a [semNome]. Útil também para textos da história. */
    fun montar(comNome: String, semNome: String, nome: String?): String {
        val n = nomeLimpo(nome)
        return if (n.isEmpty()) semNome else comNome.replace(N, n)
    }

    private fun da(lista: List<Pair<String, String>>, nome: String?, indice: Int): String {
        val (com, sem) = lista[Math.floorMod(indice, lista.size)]
        return montar(com, sem, nome)
    }

    /** "Muito bem, Ana!" (ou "Muito bem!" sem nome). */
    fun elogio(nome: String?, indice: Int = 0): String = da(elogios, nome, indice)

    /** Depois de um erro: sempre carinhoso, nunca bronca. */
    fun incentivo(nome: String?, indice: Int = 0): String = da(incentivos, nome, indice)

    /** Ao abrir o app. */
    fun chegada(nome: String?, indice: Int = 0): String = da(chegadas, nome, indice)

    /** No dia do aniversário. */
    fun parabensAniversario(nome: String?): String =
        montar("Feliz aniversário, $N! Hoje a casinha está em festa por você!", "Feliz aniversário! Hoje a casinha está em festa por você!", nome)

    /** Fase vencida, com o número de estrelas. */
    fun vitoria(nome: String?, estrelas: Int): String = when {
        estrelas >= 3 -> montar("Três estrelas, $N! Incrível!", "Três estrelas! Incrível!", nome)
        else -> montar("Você venceu, $N! Muito bem!", "Você venceu! Muito bem!", nome)
    }
}
