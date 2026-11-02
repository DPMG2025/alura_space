package br.com.casinhadosaber.dominio

/** Coisa que dá para contar, com o nome certinho no singular, no plural e o gênero. */
data class Objeto(val emoji: String, val singular: String, val plural: String, val feminino: Boolean) {
    fun nome(qtd: Int) = if (qtd == 1) singular else plural
}

/** Palavra em português com figura e sílabas (para letras, sílabas e rimas). */
data class Palavra(val emoji: String, val texto: String, val silabas: List<String>, val rima: String? = null) {
    val inicial: String get() = texto.first().toString()
}

/** Palavra em inglês com a tradução e a figura (ou a cor). */
data class PalavraEn(val en: String, val pt: String, val emoji: String = "", val cor: Long? = null)

object Vocabulario {

    val objetosParaContar = listOf(
        Objeto("🍎", "maçã", "maçãs", true),
        Objeto("🐢", "tartaruga", "tartarugas", true),
        Objeto("🐟", "peixinho", "peixinhos", false),
        Objeto("🎈", "balão", "balões", false),
        Objeto("🐥", "pintinho", "pintinhos", false),
        Objeto("🌸", "flor", "flores", true),
        Objeto("🚗", "carrinho", "carrinhos", false),
        Objeto("🍓", "morango", "morangos", false),
        Objeto("🦋", "borboleta", "borboletas", true),
        Objeto("⚽", "bola", "bolas", true),
        Objeto("🐞", "joaninha", "joaninhas", true),
        Objeto("🧁", "bolinho", "bolinhos", false),
    )

    private val unidadesM = listOf("zero", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez")
    private val unidadesF = listOf("zero", "uma", "duas", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez")
    val numerosEn = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten")

    fun porExtenso(n: Int, feminino: Boolean = false): String =
        (if (feminino) unidadesF else unidadesM).getOrElse(n) { n.toString() }

    val vogais = listOf("A", "E", "I", "O", "U")
    val consoantes = listOf("B", "C", "D", "F", "G", "L", "M", "N", "P", "R", "S", "T", "V", "Z")

    /** Como o narrador fala o nome de cada letra. */
    val nomeDaLetra = mapOf(
        "A" to "á", "B" to "bê", "C" to "cê", "D" to "dê", "E" to "é", "F" to "éfe", "G" to "gê",
        "I" to "i", "J" to "jota", "L" to "éle", "M" to "ême", "N" to "êne", "O" to "ó", "P" to "pê",
        "R" to "érre", "S" to "ésse", "T" to "tê", "U" to "u", "V" to "vê", "Z" to "zê",
    )

    fun falaDaLetra(letra: String) = nomeDaLetra[letra] ?: letra.lowercase()

    val palavrasComVogal = listOf(
        Palavra("🐝", "ABELHA", listOf("A", "BE", "LHA")),
        Palavra("✈️", "AVIÃO", listOf("A", "VI", "ÃO"), rima = "ÃO"),
        Palavra("🐘", "ELEFANTE", listOf("E", "LE", "FAN", "TE")),
        Palavra("⭐", "ESTRELA", listOf("ES", "TRE", "LA")),
        Palavra("⛪", "IGREJA", listOf("I", "GRE", "JA")),
        Palavra("🏝️", "ILHA", listOf("I", "LHA")),
        Palavra("🥚", "OVO", listOf("O", "VO")),
        Palavra("🚌", "ÔNIBUS", listOf("Ô", "NI", "BUS")),
        Palavra("🍇", "UVA", listOf("U", "VA")),
        Palavra("🐻", "URSO", listOf("UR", "SO")),
        Palavra("🦄", "UNICÓRNIO", listOf("U", "NI", "CÓR", "NIO")),
    )

    val palavrasComConsoante = listOf(
        Palavra("⚽", "BOLA", listOf("BO", "LA")),
        Palavra("🍌", "BANANA", listOf("BA", "NA", "NA")),
        Palavra("🍬", "BALA", listOf("BA", "LA"), rima = "ALA"),
        Palavra("🏠", "CASA", listOf("CA", "SA")),
        Palavra("🐴", "CAVALO", listOf("CA", "VA", "LO"), rima = "ALO"),
        Palavra("🎲", "DADO", listOf("DA", "DO")),
        Palavra("🦷", "DENTE", listOf("DEN", "TE")),
        Palavra("🧚", "FADA", listOf("FA", "DA")),
        Palavra("🦭", "FOCA", listOf("FO", "CA")),
        Palavra("🐱", "GATO", listOf("GA", "TO"), rima = "ATO"),
        Palavra("🐓", "GALO", listOf("GA", "LO"), rima = "ALO"),
        Palavra("🌙", "LUA", listOf("LU", "A")),
        Palavra("🦁", "LEÃO", listOf("LE", "ÃO"), rima = "ÃO"),
        Palavra("🐒", "MACACO", listOf("MA", "CA", "CO")),
        Palavra("🧳", "MALA", listOf("MA", "LA"), rima = "ALA"),
        Palavra("✋", "MÃO", listOf("MÃO"), rima = "ÃO"),
        Palavra("🚢", "NAVIO", listOf("NA", "VI", "O")),
        Palavra("🦆", "PATO", listOf("PA", "TO"), rima = "ATO"),
        Palavra("🍞", "PÃO", listOf("PÃO"), rima = "ÃO"),
        Palavra("🍿", "PIPOCA", listOf("PI", "PO", "CA")),
        Palavra("🐭", "RATO", listOf("RA", "TO"), rima = "ATO"),
        Palavra("🐸", "SAPO", listOf("SA", "PO")),
        Palavra("👞", "SAPATO", listOf("SA", "PA", "TO"), rima = "ATO"),
        Palavra("🍅", "TOMATE", listOf("TO", "MA", "TE")),
        Palavra("🐮", "VACA", listOf("VA", "CA")),
        Palavra("🦓", "ZEBRA", listOf("ZE", "BRA")),
    )

    val todasAsPalavras = palavrasComVogal + palavrasComConsoante

    val coresBasicas = listOf(
        PalavraEn("red", "vermelho", cor = 0xFFE53935),
        PalavraEn("blue", "azul", cor = 0xFF1E88E5),
        PalavraEn("yellow", "amarelo", cor = 0xFFFDD835),
        PalavraEn("green", "verde", cor = 0xFF43A047),
    )

    val todasAsCores = coresBasicas + listOf(
        PalavraEn("orange", "laranja", cor = 0xFFFB8C00),
        PalavraEn("purple", "roxo", cor = 0xFF8E24AA),
        PalavraEn("pink", "rosa", cor = 0xFFF48FB1),
        PalavraEn("black", "preto", cor = 0xFF212121),
        PalavraEn("white", "branco", cor = 0xFFFFFFFF),
        PalavraEn("brown", "marrom", cor = 0xFF795548),
    )

    val animais = listOf(
        PalavraEn("cat", "gato", "🐱"),
        PalavraEn("dog", "cachorro", "🐶"),
        PalavraEn("cow", "vaca", "🐮"),
        PalavraEn("duck", "pato", "🦆"),
        PalavraEn("fish", "peixe", "🐟"),
        PalavraEn("bird", "passarinho", "🐦"),
        PalavraEn("horse", "cavalo", "🐴"),
        PalavraEn("pig", "porquinho", "🐷"),
        PalavraEn("lion", "leão", "🦁"),
        PalavraEn("frog", "sapo", "🐸"),
        PalavraEn("monkey", "macaco", "🐒"),
        PalavraEn("rabbit", "coelho", "🐰"),
    )

    val frutas = listOf(
        PalavraEn("apple", "maçã", "🍎"),
        PalavraEn("banana", "banana", "🍌"),
        PalavraEn("grapes", "uva", "🍇"),
        PalavraEn("orange", "laranja", "🍊"),
        PalavraEn("strawberry", "morango", "🍓"),
        PalavraEn("watermelon", "melancia", "🍉"),
        PalavraEn("pineapple", "abacaxi", "🍍"),
        PalavraEn("lemon", "limão", "🍋"),
        PalavraEn("pear", "pera", "🍐"),
        PalavraEn("cherry", "cereja", "🍒"),
    )

    val corpo = listOf(
        PalavraEn("eye", "olho", "👁️"),
        PalavraEn("ear", "orelha", "👂"),
        PalavraEn("nose", "nariz", "👃"),
        PalavraEn("mouth", "boca", "👄"),
        PalavraEn("hand", "mão", "✋"),
        PalavraEn("foot", "pé", "🦶"),
        PalavraEn("tooth", "dente", "🦷"),
        PalavraEn("leg", "perna", "🦵"),
    )

    val casa = listOf(
        PalavraEn("bed", "cama", "🛏️"),
        PalavraEn("chair", "cadeira", "🪑"),
        PalavraEn("door", "porta", "🚪"),
        PalavraEn("window", "janela", "🪟"),
        PalavraEn("sofa", "sofá", "🛋️"),
        PalavraEn("lamp", "lâmpada", "💡"),
        PalavraEn("clock", "relógio", "🕰️"),
        PalavraEn("house", "casa", "🏠"),
        PalavraEn("bathtub", "banheira", "🛁"),
    )

    val brinquedos = listOf(
        PalavraEn("ball", "bola", "⚽"),
        PalavraEn("car", "carro", "🚗"),
        PalavraEn("teddy bear", "ursinho", "🧸"),
        PalavraEn("kite", "pipa", "🪁"),
        PalavraEn("train", "trem", "🚂"),
        PalavraEn("balloon", "balão", "🎈"),
        PalavraEn("book", "livro", "📚"),
        PalavraEn("drum", "tambor", "🥁"),
        PalavraEn("robot", "robô", "🤖"),
    )

    /** Pares que combinam (lógica): o primeiro precisa do segundo. */
    data class Par(val de: String, val nomeDe: String, val para: String, val nomePara: String)

    val pares = listOf(
        Par("🐶", "cachorro", "🦴", "osso"),
        Par("🐝", "abelha", "🍯", "mel"),
        Par("🐰", "coelho", "🥕", "cenoura"),
        Par("🐵", "macaco", "🍌", "banana"),
        Par("🌧️", "chuva", "☂️", "guarda-chuva"),
        Par("🦶", "pé", "👟", "tênis"),
        Par("🦷", "dente", "🪥", "escova de dente"),
        Par("🔒", "cadeado", "🔑", "chave"),
        Par("🐔", "galinha", "🥚", "ovo"),
        Par("🐄", "vaca", "🥛", "leite"),
        Par("✏️", "lápis", "📝", "papel"),
        Par("🐟", "peixe", "🌊", "água"),
    )

    /** Grupos para a fase "Qual é...?" da lógica. */
    data class Grupo(val pergunta: String, val itens: List<String>)

    val grupos = listOf(
        Grupo("Qual destes é um bichinho?", listOf("🐶", "🐱", "🐮", "🐷", "🐸", "🦁", "🐰", "🦆")),
        Grupo("Qual destes é de comer?", listOf("🍎", "🍌", "🍇", "🍓", "🥕", "🍞", "🧀", "🍉")),
        Grupo("Qual destes é um brinquedo?", listOf("🧸", "⚽", "🪁", "🎈", "🪀", "🧩", "🥁", "🚂")),
        Grupo("Qual destes é de vestir?", listOf("👕", "👖", "👗", "🧢", "🧦", "👟", "🧤", "🧣")),
    )

    /** Emojis variados e fáceis de distinguir, para lógica. */
    val figurasSimples = listOf(
        "🍎", "🌟", "🐶", "🐱", "🚗", "⚽", "🌸", "🐟", "🎈", "🍌", "🐸", "🦋", "🍓", "🐥", "🧸", "🌙",
    )

    /** Figuras que aparecem do mesmo tamanho (círculos e formas cheias), para a fila ficar uniforme. */
    val figurasDeSequencia = listOf("🔴", "🔵", "🟡", "🟢", "🟣", "🟠", "⭐", "❤️")
}
