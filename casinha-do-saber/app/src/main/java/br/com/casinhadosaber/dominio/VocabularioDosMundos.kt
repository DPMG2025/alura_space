package br.com.casinhadosaber.dominio

/**
 * Vocabulário das fases 26 a 50 de cada caminho (mundos 3, 4 e 5): palavras grandes e sílabas
 * difíceis, frases curtas, sons das letras, temas novos de inglês, medidas, horas e classificação.
 */
object VocabularioDosMundos {

    // ------------------------------------------------------------------ português

    /** Palavras de 3 e 4 sílabas (alfabetização: palavras grandes). */
    val palavrasGrandes = listOf(
        Palavra("🦋", "BORBOLETA", listOf("BOR", "BO", "LE", "TA")),
        Palavra("🐢", "TARTARUGA", listOf("TAR", "TA", "RU", "GA")),
        Palavra("🐊", "JACARÉ", listOf("JA", "CA", "RÉ")),
        Palavra("🐫", "CAMELO", listOf("CA", "ME", "LO")),
        Palavra("🍭", "PIRULITO", listOf("PI", "RU", "LI", "TO")),
        Palavra("🍍", "ABACAXI", listOf("A", "BA", "CA", "XI")),
        Palavra("🦒", "GIRAFA", listOf("GI", "RA", "FA")),
        Palavra("🥔", "BATATA", listOf("BA", "TA", "TA")),
        Palavra("🚀", "FOGUETE", listOf("FO", "GUE", "TE")),
        Palavra("☎️", "TELEFONE", listOf("TE", "LE", "FO", "NE")),
        Palavra("🎒", "MOCHILA", listOf("MO", "CHI", "LA")),
        Palavra("👕", "CAMISA", listOf("CA", "MI", "SA")),
        Palavra("🍦", "SORVETE", listOf("SOR", "VE", "TE")),
        Palavra("🪟", "JANELA", listOf("JA", "NE", "LA")),
        Palavra("🐔", "GALINHA", listOf("GA", "LI", "NHA")),
        Palavra("🥕", "CENOURA", listOf("CE", "NOU", "RA")),
        Palavra("🦜", "PAPAGAIO", listOf("PA", "PA", "GAI", "O")),
        Palavra("🍉", "MELANCIA", listOf("ME", "LAN", "CI", "A")),
        Palavra("🐒", "MACACO", listOf("MA", "CA", "CO")),
        Palavra("🍅", "TOMATE", listOf("TO", "MA", "TE")),
        Palavra("🍿", "PIPOCA", listOf("PI", "PO", "CA")),
        Palavra("👞", "SAPATO", listOf("SA", "PA", "TO")),
        Palavra("🐴", "CAVALO", listOf("CA", "VA", "LO")),
        Palavra("🍌", "BANANA", listOf("BA", "NA", "NA")),
    )

    /** Palavras com CH, LH, NH e com encontros (BR, PR, TR, FL...): as sílabas "difíceis". */
    val palavrasComplexas = listOf(
        Palavra("🔑", "CHAVE", listOf("CHA", "VE")),
        Palavra("🌧️", "CHUVA", listOf("CHU", "VA")),
        Palavra("🍫", "CHOCOLATE", listOf("CHO", "CO", "LA", "TE")),
        Palavra("👒", "CHAPÉU", listOf("CHA", "PÉU")),
        Palavra("🍃", "FOLHA", listOf("FO", "LHA")),
        Palavra("👁️", "OLHO", listOf("O", "LHO")),
        Palavra("🐰", "COELHO", listOf("CO", "E", "LHO")),
        Palavra("🐔", "GALINHA", listOf("GA", "LI", "NHA")),
        Palavra("🐷", "PORQUINHO", listOf("POR", "QUI", "NHO")),
        Palavra("🤡", "PALHAÇO", listOf("PA", "LHA", "ÇO")),
        Palavra("🍽️", "PRATO", listOf("PRA", "TO")),
        Palavra("🏖️", "PRAIA", listOf("PRAI", "A")),
        Palavra("🧙", "BRUXA", listOf("BRU", "XA")),
        Palavra("🚂", "TREM", listOf("TREM")),
        Palavra("🌸", "FLOR", listOf("FLOR")),
        Palavra("📕", "LIVRO", listOf("LI", "VRO")),
        Palavra("🐯", "TIGRE", listOf("TI", "GRE")),
        Palavra("🐍", "COBRA", listOf("CO", "BRA")),
        Palavra("🦓", "ZEBRA", listOf("ZE", "BRA")),
        Palavra("⭐", "ESTRELA", listOf("ES", "TRE", "LA")),
    )

    /** As sílabas difíceis que aparecem como opção (começo de palavra). */
    val silabasComplexas = listOf("CHA", "CHU", "CHO", "PRA", "PRAI", "BRU", "TREM", "FLOR", "CRA", "GRA", "FRU", "BLU", "TRO", "PLA", "CLA")

    /** Palavras curtinhas para montar letra por letra. */
    val palavrasCurtas = listOf(
        Palavra("☀️", "SOL", listOf("SOL")),
        Palavra("🌙", "LUA", listOf("LU", "A")),
        Palavra("🌊", "MAR", listOf("MAR")),
        Palavra("🐭", "RATO", listOf("RA", "TO")),
        Palavra("🐱", "GATO", listOf("GA", "TO")),
        Palavra("🦆", "PATO", listOf("PA", "TO")),
        Palavra("⚽", "BOLA", listOf("BO", "LA")),
        Palavra("🐸", "SAPO", listOf("SA", "PO")),
        Palavra("🎲", "DADO", listOf("DA", "DO")),
        Palavra("🏠", "CASA", listOf("CA", "SA")),
        Palavra("🐮", "VACA", listOf("VA", "CA")),
        Palavra("🍇", "UVA", listOf("U", "VA")),
        Palavra("🥚", "OVO", listOf("O", "VO")),
        Palavra("🧚", "FADA", listOf("FA", "DA")),
        Palavra("🦭", "FOCA", listOf("FO", "CA")),
        Palavra("🧳", "MALA", listOf("MA", "LA")),
        Palavra("🎂", "BOLO", listOf("BO", "LO")),
        Palavra("🐻", "URSO", listOf("UR", "SO")),
        Palavra("🦷", "DENTE", listOf("DEN", "TE")),
        Palavra("🔑", "CHAVE", listOf("CHA", "VE")),
    )

    /** Frases curtinhas para ler: cada uma combina com uma figura só. */
    data class Frase(val texto: String, val emoji: String)

    val frases = listOf(
        Frase("O SOL BRILHA", "☀️"),
        Frase("A CHUVA CAI", "🌧️"),
        Frase("O AVIÃO VOA", "✈️"),
        Frase("O PEIXE NADA", "🐟"),
        Frase("O GALO CANTA", "🐓"),
        Frase("O GATO MIA", "🐱"),
        Frase("O CÃO LATE", "🐶"),
        Frase("O BEBÊ DORME", "👶"),
        Frase("A LUA É BELA", "🌙"),
        Frase("O BOLO É DOCE", "🎂"),
        Frase("A VACA DÁ LEITE", "🐄"),
        Frase("O BARCO NO MAR", "⛵"),
        Frase("A BOLA ROLA", "⚽"),
        Frase("O SAPO PULA", "🐸"),
        Frase("A FLOR É ROSA", "🌸"),
        Frase("O TREM APITA", "🚂"),
    )

    /** Sons que dá para "esticar" (fonético): a letra, como o narrador faz o som e uma palavra que começa com ele. */
    data class SomDaLetra(val letra: String, val som: String, val palavra: String, val emoji: String)

    val sonsDasLetras = listOf(
        SomDaLetra("M", "mmmmm", "macaco", "🐒"),
        SomDaLetra("S", "sssss", "sapo", "🐸"),
        SomDaLetra("F", "fffff", "foca", "🦭"),
        SomDaLetra("V", "vvvvv", "vaca", "🐮"),
        SomDaLetra("Z", "zzzzz", "zebra", "🦓"),
        SomDaLetra("L", "lllll", "lua", "🌙"),
        SomDaLetra("N", "nnnnn", "navio", "🚢"),
        SomDaLetra("R", "rrrrr", "rato", "🐭"),
        SomDaLetra("A", "aaaaa", "abelha", "🐝"),
        SomDaLetra("O", "ooooo", "ovo", "🥚"),
        SomDaLetra("U", "uuuuu", "uva", "🍇"),
        SomDaLetra("I", "iiiii", "ilha", "🏝️"),
    )

    /** Consoantes das famílias silábicas (BA BE BI BO BU). Sem C e G, que mudam de som com E e I. */
    val familias = listOf("B", "P", "M", "L", "T", "D", "F", "V", "N", "S", "R", "J")

    // ------------------------------------------------------------------ inglês

    val escola = listOf(
        PalavraEn("pencil", "lápis", "✏️"),
        PalavraEn("book", "livro", "📕"),
        PalavraEn("backpack", "mochila", "🎒"),
        PalavraEn("scissors", "tesoura", "✂️"),
        PalavraEn("ruler", "régua", "📏"),
        PalavraEn("crayon", "giz de cera", "🖍️"),
        PalavraEn("computer", "computador", "💻"),
        PalavraEn("school", "escola", "🏫"),
    )

    val natureza = listOf(
        PalavraEn("tree", "árvore", "🌳"),
        PalavraEn("flower", "flor", "🌸"),
        PalavraEn("leaf", "folha", "🍃"),
        PalavraEn("mountain", "montanha", "⛰️"),
        PalavraEn("sea", "mar", "🌊"),
        PalavraEn("volcano", "vulcão", "🌋"),
        PalavraEn("plant", "plantinha", "🌱"),
        PalavraEn("mushroom", "cogumelo", "🍄"),
        PalavraEn("cactus", "cacto", "🌵"),
    )

    val sentimentos = listOf(
        PalavraEn("happy", "feliz", "😀"),
        PalavraEn("sad", "triste", "😢"),
        PalavraEn("angry", "bravo", "😠"),
        PalavraEn("scared", "com medo", "😨"),
        PalavraEn("sleepy", "com sono", "😴"),
        PalavraEn("surprised", "surpreso", "😲"),
        PalavraEn("sick", "doente", "🤒"),
    )

    val profissoes = listOf(
        PalavraEn("police officer", "policial", "👮"),
        PalavraEn("builder", "pedreiro", "👷"),
        PalavraEn("doctor", "médico", "🩺"),
        PalavraEn("firefighter", "bombeiro", "🚒"),
        PalavraEn("farmer", "fazendeiro", "🚜"),
        PalavraEn("cook", "cozinheiro", "🍳"),
        PalavraEn("artist", "artista", "🎨"),
        PalavraEn("mail carrier", "carteiro", "📮"),
    )

    val verbos = listOf(
        PalavraEn("walk", "andar", "🚶"),
        PalavraEn("cry", "chorar", "😭"),
        PalavraEn("laugh", "rir", "😂"),
        PalavraEn("drink", "beber", "🥤"),
        PalavraEn("write", "escrever", "✍️"),
        PalavraEn("play", "brincar", "🎮"),
        PalavraEn("listen", "escutar", "🎧"),
        PalavraEn("climb", "escalar", "🧗"),
    )

    val animaisSelvagens = listOf(
        PalavraEn("elephant", "elefante", "🐘"),
        PalavraEn("giraffe", "girafa", "🦒"),
        PalavraEn("zebra", "zebra", "🦓"),
        PalavraEn("tiger", "tigre", "🐯"),
        PalavraEn("bear", "urso", "🐻"),
        PalavraEn("snake", "cobra", "🐍"),
        PalavraEn("bee", "abelha", "🐝"),
        PalavraEn("owl", "coruja", "🦉"),
        PalavraEn("penguin", "pinguim", "🐧"),
        PalavraEn("turtle", "tartaruga", "🐢"),
    )

    val opostos = listOf(
        PalavraEn("big", "grande", "🐘"),
        PalavraEn("small", "pequeno", "🐭"),
        PalavraEn("hot", "quente", "🔥"),
        PalavraEn("cold", "frio", "🥶"),
        PalavraEn("fast", "rápido", "🐆"),
        PalavraEn("slow", "devagar", "🐌"),
        PalavraEn("day", "dia", "🌞"),
        PalavraEn("night", "noite", "🌃"),
    )

    /** Frases simples em inglês ("I like...", "I am...", "I can...") com a figura que combina. */
    data class FraseEn(val en: String, val pt: String, val emoji: String)

    val gosto = listOf(
        FraseEn("I like apples", "Eu gosto de maçãs", "🍎"),
        FraseEn("I like bananas", "Eu gosto de bananas", "🍌"),
        FraseEn("I like ice cream", "Eu gosto de sorvete", "🍦"),
        FraseEn("I like pizza", "Eu gosto de pizza", "🍕"),
        FraseEn("I like dogs", "Eu gosto de cachorros", "🐶"),
        FraseEn("I like cats", "Eu gosto de gatos", "🐱"),
        FraseEn("I like cake", "Eu gosto de bolo", "🎂"),
        FraseEn("I like milk", "Eu gosto de leite", "🥛"),
        FraseEn("I like books", "Eu gosto de livros", "📚"),
        FraseEn("I like the sun", "Eu gosto do sol", "☀️"),
    )

    val souEPosso = listOf(
        FraseEn("I am happy", "Eu estou feliz", "😀"),
        FraseEn("I am sad", "Eu estou triste", "😢"),
        FraseEn("I am sleepy", "Eu estou com sono", "😴"),
        FraseEn("I am sick", "Eu estou doente", "🤒"),
        FraseEn("I can swim", "Eu sei nadar", "🏊"),
        FraseEn("I can run", "Eu sei correr", "🏃"),
        FraseEn("I can dance", "Eu sei dançar", "💃"),
        FraseEn("I can sing", "Eu sei cantar", "🎤"),
        FraseEn("I can read", "Eu sei ler", "📖"),
        FraseEn("I can climb", "Eu sei escalar", "🧗"),
    )

    /** Coisas com uma cor bem conhecida, para "What color is it?" (o nome em inglês da cor). */
    val coisasColoridas = listOf(
        "🍌" to "yellow", "🌻" to "yellow", "🍋" to "yellow",
        "🍎" to "red", "🍓" to "red", "🍒" to "red",
        "🥦" to "green", "🐸" to "green", "🥒" to "green",
        "🍆" to "purple", "🍇" to "purple",
        "🍊" to "orange", "🥕" to "orange", "🎃" to "orange",
        "🐷" to "pink", "🦩" to "pink",
        "🐻" to "brown", "🍫" to "brown",
        "💙" to "blue", "🐳" to "blue",
        "🖤" to "black", "🎱" to "black",
        "☁️" to "white", "🥛" to "white",
    )

    // ------------------------------------------------------------------ matemática e lógica

    /** Coisas em ordem, da mais leve para a mais pesada (medidas). */
    val doLeveAoPesado = listOf(
        "🪶" to "pena", "🍎" to "maçã", "🐈" to "gato", "🐕" to "cachorro", "🐄" to "vaca", "🐘" to "elefante",
    )

    /** Coisas em ordem, da mais baixa para a mais alta. */
    val doBaixoAoAlto = listOf(
        "🐜" to "formiga", "🐁" to "ratinho", "🐈" to "gato", "🧒" to "criança", "🦒" to "girafa", "🏢" to "prédio",
    )

    /** Relógios das horas cheias, de 1 a 12. */
    val relogios = listOf("🕐", "🕑", "🕒", "🕓", "🕔", "🕕", "🕖", "🕗", "🕘", "🕙", "🕚", "🕛")

    /** Grupos para "qual não é da turma?" (intrusos), com o nome do grupo. */
    val turmas = listOf(
        "frutas" to listOf("🍎", "🍌", "🍇", "🍓", "🍉", "🍍", "🍐", "🍒"),
        "bichos" to listOf("🐶", "🐱", "🐮", "🐷", "🐸", "🦁", "🐰", "🐵"),
        "veículos" to listOf("🚗", "🚌", "🚂", "✈️", "⛵", "🚲", "🚚", "🚀"),
        "roupas" to listOf("👕", "👖", "👗", "🧢", "🧦", "👟", "🧤", "🧣"),
        "brinquedos" to listOf("🧸", "⚽", "🪁", "🎈", "🪀", "🧩", "🥁", "🎲"),
        "coisas do céu" to listOf("☀️", "🌙", "⭐", "☁️", "🌈", "⚡"),
    )

    /** Círculo e quadrado de cada cor (classificação dupla: cor e forma). */
    data class CorEForma(val nomeCor: String, val circulo: String, val quadrado: String)

    val coresEFormas = listOf(
        CorEForma("vermelho", "🔴", "🟥"),
        CorEForma("azul", "🔵", "🟦"),
        CorEForma("amarelo", "🟡", "🟨"),
        CorEForma("verde", "🟢", "🟩"),
        CorEForma("roxo", "🟣", "🟪"),
        CorEForma("laranja", "🟠", "🟧"),
    )

    /** Classificação dupla na cesta: só o que é do grupo E tem a característica pedida. */
    data class Criterio(val nome: String, val certos: List<String>, val quaseCertos: List<String>)

    val criteriosDuplos = listOf(
        Criterio("as frutas vermelhas", listOf("🍎", "🍓", "🍒"), listOf("🍌", "🍋", "🍐", "🚗", "❤️", "🎈")),
        Criterio("as frutas amarelas", listOf("🍌", "🍋", "🍍"), listOf("🍎", "🍇", "🍐", "🌻", "⭐", "🐥")),
        Criterio("os bichos que voam", listOf("🐦", "🦋", "🐝", "🦉"), listOf("🐶", "🐢", "🐟", "✈️", "🎈")),
        Criterio("os bichos que nadam", listOf("🐟", "🐬", "🐙", "🦆"), listOf("🐶", "🐱", "🐦", "⛵", "🌊")),
        Criterio("os veículos que voam", listOf("✈️", "🚁", "🚀"), listOf("🚗", "🚌", "⛵", "🐦", "☁️")),
        Criterio("as coisas verdes de comer", listOf("🥦", "🥒", "🍏"), listOf("🍎", "🥕", "🐸", "🌳", "🍌")),
    )
}
