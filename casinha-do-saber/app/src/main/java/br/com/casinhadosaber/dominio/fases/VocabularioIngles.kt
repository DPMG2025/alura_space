package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.PalavraEn
import br.com.casinhadosaber.dominio.Vocabulario
import br.com.casinhadosaber.dominio.VocabularioDosMundos.FraseEn

/**
 * Vocabulário das fases 51 a 250 do caminho Inglês (mundos 6 a 25, plano em docs/plano-1000-fases.md).
 * Só o arquivo deste caminho usa este objeto: outro caminho que precisar da mesma lista copia ou
 * pede para mover para o vocabulário comum, para os caminhos poderem crescer em paralelo sem conflito.
 * O vocabulário das fases 1 a 50 continua em Vocabulario e VocabularioDosMundos.
 *
 * Tudo que tem [PalavraEn.en] ou [FraseEn.en] é falado com a voz em inglês; [PalavraEn.pt] e
 * [FraseEn.pt] são a tradução falada em português.
 */
object VocabularioIngles {

    // ------------------------------------------------------------------ números até 100

    private val dezenasEn = listOf("", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")

    /** O número [n] (0 a 100) por extenso em inglês: 42 = "forty-two". */
    fun numeroEn(n: Int): String {
        require(n in 0..100) { "Número fora de 0..100" }
        return when {
            n <= 20 -> Vocabulario.numerosEn[n]
            n == 100 -> "one hundred"
            n % 10 == 0 -> dezenasEn[n / 10]
            else -> dezenasEn[n / 10] + "-" + Vocabulario.numerosEn[n % 10]
        }
    }

    // ------------------------------------------------------------------ mundo 6: fazenda

    val fazenda = listOf(
        PalavraEn("cow", "vaca", "🐮"),
        PalavraEn("pig", "porco", "🐷"),
        PalavraEn("horse", "cavalo", "🐴"),
        PalavraEn("sheep", "ovelha", "🐑"),
        PalavraEn("goat", "cabra", "🐐"),
        PalavraEn("chicken", "galinha", "🐔"),
        PalavraEn("duck", "pato", "🦆"),
        PalavraEn("rooster", "galo", "🐓"),
        PalavraEn("turkey", "peru", "🦃"),
        PalavraEn("rabbit", "coelho", "🐰"),
        PalavraEn("dog", "cachorro", "🐶"),
        PalavraEn("cat", "gato", "🐱"),
    )

    /** O bicho da fazenda e o som que ele faz em inglês (a vaca diz "moo"). */
    data class SomDeBicho(val bicho: PalavraEn, val som: String)

    val sonsDaFazenda = listOf(
        SomDeBicho(fazenda[0], "moo"),
        SomDeBicho(fazenda[1], "oink"),
        SomDeBicho(fazenda[2], "neigh"),
        SomDeBicho(fazenda[3], "baa"),
        SomDeBicho(fazenda[6], "quack"),
        SomDeBicho(fazenda[7], "cock-a-doodle-doo"),
        SomDeBicho(fazenda[10], "woof"),
        SomDeBicho(fazenda[11], "meow"),
        SomDeBicho(fazenda[5], "cluck"),
    )

    /** Coisas da fazenda que não são bichos (para a pescaria "só os bichos"). */
    val coisasDaFazenda = listOf(
        PalavraEn("tractor", "trator", "🚜"),
        PalavraEn("corn", "milho", "🌽"),
        PalavraEn("milk", "leite", "🥛"),
        PalavraEn("egg", "ovo", "🥚"),
        PalavraEn("apple", "maçã", "🍎"),
        PalavraEn("carrot", "cenoura", "🥕"),
    )

    // ------------------------------------------------------------------ mundo 7: fundo do mar

    val mar = listOf(
        PalavraEn("fish", "peixe", "🐟"),
        PalavraEn("whale", "baleia", "🐳"),
        PalavraEn("dolphin", "golfinho", "🐬"),
        PalavraEn("octopus", "polvo", "🐙"),
        PalavraEn("crab", "caranguejo", "🦀"),
        PalavraEn("shark", "tubarão", "🦈"),
        PalavraEn("turtle", "tartaruga", "🐢"),
        PalavraEn("shrimp", "camarão", "🦐"),
        PalavraEn("squid", "lula", "🦑"),
        PalavraEn("shell", "concha", "🐚"),
    )

    /** Bicho do mar com a cor que ele tem na figura: "a red crab". */
    data class BichoColorido(val bicho: PalavraEn, val cor: PalavraEn) {
        val en: String get() = "a ${cor.en} ${bicho.en}"
        val pt: String get() = "${artigoPt(bicho.pt)} ${bicho.pt} ${cor.pt}"
    }

    private fun artigoPt(palavra: String) =
        if (palavra in setOf("baleia", "tartaruga", "lula", "concha", "ovelha", "cabra", "galinha", "vaca", "cobra", "girafa", "zebra")) "uma" else "um"

    val gray = PalavraEn("gray", "cinza", cor = 0xFF9E9E9E)

    private fun cor(en: String) = (Vocabulario.todasAsCores + gray).first { it.en == en }

    val marColorido = listOf(
        BichoColorido(mar[4], cor("red")),
        BichoColorido(mar[1], cor("blue")),
        BichoColorido(mar[0], cor("blue")),
        BichoColorido(mar[6], cor("green")),
        BichoColorido(mar[5], gray),
        BichoColorido(mar[3], cor("pink")),
        BichoColorido(mar[7], cor("orange")),
        BichoColorido(mar[2], cor("blue")),
    )

    /** Cores que a criança já conhece com nome em inglês, mais o cinza (gray). */
    val cores: List<PalavraEn> = Vocabulario.todasAsCores + gray

    // ------------------------------------------------------------------ mundo 8: selva (adjetivos)

    val adjetivos = listOf(
        PalavraEn("big", "grande", "🐘"),
        PalavraEn("small", "pequeno", "🐭"),
        PalavraEn("long", "comprido", "🐍"),
        PalavraEn("short", "curto", "✏️"),
        PalavraEn("tall", "alto", "🦒"),
        PalavraEn("fast", "rápido", "🐆"),
        PalavraEn("slow", "devagar", "🐌"),
    )

    val frasesDaSelva = listOf(
        FraseEn("a big elephant", "um elefante grande", "🐘"),
        FraseEn("a small mouse", "um ratinho pequeno", "🐭"),
        FraseEn("a long snake", "uma cobra comprida", "🐍"),
        FraseEn("a tall giraffe", "uma girafa alta", "🦒"),
        FraseEn("a fast cheetah", "um guepardo rápido", "🐆"),
        FraseEn("a slow snail", "um caracol devagar", "🐌"),
        FraseEn("a small bee", "uma abelha pequena", "🐝"),
        FraseEn("a big gorilla", "um gorila grande", "🦍"),
        FraseEn("a tall tree", "uma árvore alta", "🌴"),
        FraseEn("a long crocodile", "um crocodilo comprido", "🐊"),
    )

    val selva = listOf(
        PalavraEn("monkey", "macaco", "🐒"),
        PalavraEn("parrot", "papagaio", "🦜"),
        PalavraEn("snake", "cobra", "🐍"),
        PalavraEn("crocodile", "crocodilo", "🐊"),
        PalavraEn("gorilla", "gorila", "🦍"),
        PalavraEn("sloth", "bicho-preguiça", "🦥"),
        PalavraEn("tiger", "tigre", "🐯"),
        PalavraEn("frog", "sapo", "🐸"),
    )

    // ------------------------------------------------------------------ mundo 9: polo norte (roupas)

    val roupasDeFrio = listOf(
        PalavraEn("coat", "casaco", "🧥"),
        PalavraEn("scarf", "cachecol", "🧣"),
        PalavraEn("gloves", "luvas", "🧤"),
        PalavraEn("boots", "botas", "🥾"),
        PalavraEn("socks", "meias", "🧦"),
    )

    val roupasDeCalor = listOf(
        PalavraEn("shorts", "bermuda", "🩳"),
        PalavraEn("T-shirt", "camiseta", "👕"),
        PalavraEn("sunglasses", "óculos de sol", "🕶️"),
        PalavraEn("swimsuit", "maiô", "🩱"),
        PalavraEn("sandals", "chinelos", "🩴"),
        PalavraEn("cap", "boné", "🧢"),
    )

    val roupas = roupasDeFrio + roupasDeCalor

    val poloNorte = listOf(
        PalavraEn("penguin", "pinguim", "🐧"),
        PalavraEn("polar bear", "urso polar", "🐻‍❄️"),
        PalavraEn("seal", "foca", "🦭"),
        PalavraEn("snowman", "boneco de neve", "⛄"),
        PalavraEn("snow", "neve", "❄️"),
        PalavraEn("ice", "gelo", "🧊"),
        PalavraEn("ice skates", "patins de gelo", "⛸️"),
        PalavraEn("sled", "trenó", "🛷"),
    )

    // ------------------------------------------------------------------ mundo 10: circo (números)

    val idades = 3..10

    val circo = listOf(
        PalavraEn("clown", "palhaço", "🤡"),
        PalavraEn("juggler", "malabarista", "🤹"),
        PalavraEn("tent", "lona do circo", "🎪"),
        PalavraEn("ticket", "ingresso", "🎟️"),
        PalavraEn("popcorn", "pipoca", "🍿"),
        PalavraEn("balloon", "balão", "🎈"),
        PalavraEn("magic wand", "varinha mágica", "🪄"),
        PalavraEn("seal", "foca", "🦭"),
    )

    // ------------------------------------------------------------------ mundo 11: deserto (preposições)

    val preposicoes = listOf(
        PalavraEn("in", "dentro", "📦"),
        PalavraEn("on", "em cima", "🔝"),
        PalavraEn("under", "embaixo", "⬇️"),
        PalavraEn("next to", "ao lado", "↔️"),
        PalavraEn("behind", "atrás", "🙈"),
    )

    /** Lugares onde o bichinho se esconde (frase "The cat is under the bed"). */
    val esconderijos = listOf(
        PalavraEn("box", "caixa", "📦"),
        PalavraEn("bed", "cama", "🛏️"),
        PalavraEn("tree", "árvore", "🌳"),
        PalavraEn("car", "carro", "🚗"),
        PalavraEn("chair", "cadeira", "🪑"),
        PalavraEn("cactus", "cacto", "🌵"),
        PalavraEn("rock", "pedra", "🪨"),
        PalavraEn("tent", "barraca", "⛺"),
    )

    val deserto = listOf(
        PalavraEn("camel", "camelo", "🐫"),
        PalavraEn("cactus", "cacto", "🌵"),
        PalavraEn("sun", "sol", "☀️"),
        PalavraEn("scorpion", "escorpião", "🦂"),
        PalavraEn("lizard", "lagarto", "🦎"),
        PalavraEn("desert", "deserto", "🏜️"),
        PalavraEn("water", "água", "💧"),
        PalavraEn("rock", "pedra", "🪨"),
    )

    // ------------------------------------------------------------------ mundo 12: cidade

    val lugares = listOf(
        PalavraEn("school", "escola", "🏫"),
        PalavraEn("park", "parque", "🏞️"),
        PalavraEn("hospital", "hospital", "🏥"),
        PalavraEn("market", "mercado", "🛒"),
        PalavraEn("bank", "banco", "🏦"),
        PalavraEn("house", "casa", "🏠"),
        PalavraEn("church", "igreja", "⛪"),
        PalavraEn("hotel", "hotel", "🏨"),
        PalavraEn("post office", "correio", "🏤"),
        PalavraEn("museum", "museu", "🏛️"),
        PalavraEn("zoo", "zoológico", "🦁"),
        PalavraEn("restaurant", "restaurante", "🍽️"),
    )

    val transportes = listOf(
        PalavraEn("car", "carro", "🚗"),
        PalavraEn("bus", "ônibus", "🚌"),
        PalavraEn("taxi", "táxi", "🚕"),
        PalavraEn("ambulance", "ambulância", "🚑"),
        PalavraEn("fire truck", "caminhão de bombeiro", "🚒"),
        PalavraEn("police car", "carro de polícia", "🚓"),
        PalavraEn("helicopter", "helicóptero", "🚁"),
        PalavraEn("subway", "metrô", "🚇"),
        PalavraEn("motorcycle", "moto", "🏍️"),
        PalavraEn("bike", "bicicleta", "🚲"),
        PalavraEn("plane", "avião", "✈️"),
        PalavraEn("boat", "barco", "⛵"),
    )

    val profissoes = listOf(
        PalavraEn("doctor", "médico", "🧑‍⚕️"),
        PalavraEn("teacher", "professor", "🧑‍🏫"),
        PalavraEn("police officer", "policial", "👮"),
        PalavraEn("firefighter", "bombeiro", "🧑‍🚒"),
        PalavraEn("cook", "cozinheiro", "🧑‍🍳"),
        PalavraEn("farmer", "fazendeiro", "🧑‍🌾"),
        PalavraEn("pilot", "piloto", "🧑‍✈️"),
        PalavraEn("astronaut", "astronauta", "🧑‍🚀"),
    )

    /** Quem trabalha onde: "The doctor works at the hospital". */
    val quemTrabalhaOnde = listOf(
        FraseEn("The doctor works at the hospital", "O médico trabalha no hospital", "🏥"),
        FraseEn("The teacher works at the school", "O professor trabalha na escola", "🏫"),
        FraseEn("The cook works at the restaurant", "O cozinheiro trabalha no restaurante", "🍽️"),
        FraseEn("The farmer works on the farm", "O fazendeiro trabalha na fazenda", "🚜"),
        FraseEn("The pilot works on the plane", "O piloto trabalha no avião", "✈️"),
        FraseEn("The firefighter works at the fire station", "O bombeiro trabalha no quartel", "🚒"),
    )

    // ------------------------------------------------------------------ mundo 13: castelo (casa)

    val comodos = listOf(
        PalavraEn("bedroom", "quarto", "🛏️"),
        PalavraEn("kitchen", "cozinha", "🍳"),
        PalavraEn("bathroom", "banheiro", "🛁"),
        PalavraEn("living room", "sala", "🛋️"),
        PalavraEn("garden", "jardim", "🌷"),
        PalavraEn("garage", "garagem", "🚗"),
    )

    val moveis = listOf(
        PalavraEn("bed", "cama", "🛏️"),
        PalavraEn("chair", "cadeira", "🪑"),
        PalavraEn("sofa", "sofá", "🛋️"),
        PalavraEn("lamp", "lâmpada", "💡"),
        PalavraEn("door", "porta", "🚪"),
        PalavraEn("window", "janela", "🪟"),
        PalavraEn("bathtub", "banheira", "🛁"),
        PalavraEn("toilet", "privada", "🚽"),
        PalavraEn("mirror", "espelho", "🪞"),
        PalavraEn("TV", "televisão", "📺"),
        PalavraEn("clock", "relógio", "🕰️"),
        PalavraEn("shower", "chuveiro", "🚿"),
    )

    val castelo = listOf(
        PalavraEn("king", "rei", "🤴"),
        PalavraEn("queen", "rainha", "👸"),
        PalavraEn("crown", "coroa", "👑"),
        PalavraEn("castle", "castelo", "🏰"),
        PalavraEn("dragon", "dragão", "🐉"),
        PalavraEn("key", "chave", "🔑"),
        PalavraEn("sword", "espada", "🗡️"),
        PalavraEn("shield", "escudo", "🛡️"),
    )

    val haUmNoComodo = listOf(
        FraseEn("There is a bed in the bedroom", "Tem uma cama no quarto", "🛏️"),
        FraseEn("There is a sofa in the living room", "Tem um sofá na sala", "🛋️"),
        FraseEn("There is a bathtub in the bathroom", "Tem uma banheira no banheiro", "🛁"),
        FraseEn("There is a lamp in the bedroom", "Tem uma lâmpada no quarto", "💡"),
        FraseEn("There is a TV in the living room", "Tem uma televisão na sala", "📺"),
        FraseEn("There is a toilet in the bathroom", "Tem uma privada no banheiro", "🚽"),
        FraseEn("There is a flower in the garden", "Tem uma flor no jardim", "🌷"),
        FraseEn("There are two chairs in the kitchen", "Tem duas cadeiras na cozinha", "🪑🪑"),
        FraseEn("There are three books on the shelf", "Tem três livros na estante", "📚"),
    )

    // ------------------------------------------------------------------ mundo 14: dinossauros (can)

    val acoes = listOf(
        PalavraEn("jump", "pular", "🤸"),
        PalavraEn("run", "correr", "🏃"),
        PalavraEn("swim", "nadar", "🏊"),
        PalavraEn("fly", "voar", "🕊️"),
        PalavraEn("climb", "escalar", "🧗"),
        PalavraEn("dance", "dançar", "💃"),
        PalavraEn("sing", "cantar", "🎤"),
        PalavraEn("ride a bike", "andar de bicicleta", "🚴"),
        PalavraEn("clap", "bater palmas", "👏"),
        PalavraEn("wave", "dar tchau", "👋"),
        PalavraEn("kick", "chutar", "⚽"),
        PalavraEn("read", "ler", "📖"),
    )

    /** Quem consegue fazer a ação ("Who can fly?"), e quem não consegue. */
    data class QuemPode(val acao: PalavraEn, val podem: List<String>, val naoPodem: List<String>)

    val quemPode = listOf(
        QuemPode(acoes[3], listOf("🐦", "🦋", "🐝", "🦅", "🦜"), listOf("🐘", "🐢", "🐟", "🐷", "🐌")),
        QuemPode(acoes[2], listOf("🐟", "🐬", "🐳", "🐙", "🦆"), listOf("🦋", "🐝", "🐌", "🐛", "🐜")),
        QuemPode(acoes[0], listOf("🐸", "🦘", "🐇", "🐒", "🦗"), listOf("🐌", "🐢", "🐙", "🐛", "🐟")),
        QuemPode(acoes[4], listOf("🐒", "🐱", "🦥", "🧗", "🐿️"), listOf("🐟", "🐳", "🐬", "🐙", "🐌")),
    )

    val podeNaoPode = listOf(
        FraseEn("I can jump", "Eu sei pular", "🤸"),
        FraseEn("I can swim", "Eu sei nadar", "🏊"),
        FraseEn("I can run", "Eu sei correr", "🏃"),
        FraseEn("I can ride a bike", "Eu sei andar de bicicleta", "🚴"),
        FraseEn("A bird can fly", "O passarinho sabe voar", "🐦"),
        FraseEn("A fish can swim", "O peixe sabe nadar", "🐟"),
        FraseEn("A fish can't fly", "O peixe não sabe voar", "🐟"),
        FraseEn("An elephant can't fly", "O elefante não sabe voar", "🐘"),
        FraseEn("A snail can't run", "O caracol não sabe correr", "🐌"),
        FraseEn("A frog can jump", "O sapo sabe pular", "🐸"),
    )

    val dinossauros = listOf(
        PalavraEn("dinosaur", "dinossauro", "🦕"),
        PalavraEn("T-rex", "tiranossauro", "🦖"),
        PalavraEn("egg", "ovo", "🥚"),
        PalavraEn("bone", "osso", "🦴"),
        PalavraEn("volcano", "vulcão", "🌋"),
        PalavraEn("footprint", "pegada", "🐾"),
    )

    /** Comandos do "Simon says" (o mestre mandou), com a figura do gesto. */
    val comandos = listOf(
        PalavraEn("clap your hands", "bata palmas", "👏"),
        PalavraEn("touch your nose", "toque no nariz", "👃"),
        PalavraEn("wave", "dê tchau", "👋"),
        PalavraEn("jump", "pule", "🤸"),
        PalavraEn("stomp your feet", "bata os pés", "🦶"),
        PalavraEn("touch your ears", "toque nas orelhas", "👂"),
        PalavraEn("thumbs up", "joinha", "👍"),
        PalavraEn("sit down", "sente", "🪑"),
    )

    // ------------------------------------------------------------------ mundo 15: ilha (dias)

    val diasDaSemana = listOf(
        PalavraEn("Sunday", "domingo"),
        PalavraEn("Monday", "segunda-feira"),
        PalavraEn("Tuesday", "terça-feira"),
        PalavraEn("Wednesday", "quarta-feira"),
        PalavraEn("Thursday", "quinta-feira"),
        PalavraEn("Friday", "sexta-feira"),
        PalavraEn("Saturday", "sábado"),
    )

    val ilha = listOf(
        PalavraEn("treasure", "tesouro", "💰"),
        PalavraEn("map", "mapa", "🗺️"),
        PalavraEn("pirate", "pirata", "🏴‍☠️"),
        PalavraEn("ship", "navio", "🚢"),
        PalavraEn("island", "ilha", "🏝️"),
        PalavraEn("parrot", "papagaio", "🦜"),
        PalavraEn("coconut", "coco", "🥥"),
        PalavraEn("gem", "pedra preciosa", "💎"),
    )

    // ------------------------------------------------------------------ mundo 16: doces (comida)

    val doces = listOf(
        PalavraEn("candy", "bala", "🍬"),
        PalavraEn("lollipop", "pirulito", "🍭"),
        PalavraEn("chocolate", "chocolate", "🍫"),
        PalavraEn("cake", "bolo", "🍰"),
        PalavraEn("cookie", "biscoito", "🍪"),
        PalavraEn("ice cream", "sorvete", "🍦"),
        PalavraEn("donut", "rosquinha", "🍩"),
        PalavraEn("cupcake", "bolinho", "🧁"),
        PalavraEn("pie", "torta", "🥧"),
        PalavraEn("honey", "mel", "🍯"),
    )

    val saudaveis = listOf(
        PalavraEn("apple", "maçã", "🍎"),
        PalavraEn("carrot", "cenoura", "🥕"),
        PalavraEn("broccoli", "brócolis", "🥦"),
        PalavraEn("milk", "leite", "🥛"),
        PalavraEn("bread", "pão", "🍞"),
        PalavraEn("cheese", "queijo", "🧀"),
        PalavraEn("juice", "suco", "🧃"),
        PalavraEn("egg", "ovo", "🥚"),
        PalavraEn("rice", "arroz", "🍚"),
        PalavraEn("banana", "banana", "🍌"),
    )

    val comidas = doces + saudaveis

    // ------------------------------------------------------------------ mundo 17: bosque (corpo)

    val rostoECorpo = listOf(
        PalavraEn("eyes", "olhos", "👀"),
        PalavraEn("ear", "orelha", "👂"),
        PalavraEn("nose", "nariz", "👃"),
        PalavraEn("mouth", "boca", "👄"),
        PalavraEn("tooth", "dente", "🦷"),
        PalavraEn("tongue", "língua", "👅"),
        PalavraEn("hand", "mão", "✋"),
        PalavraEn("arm", "braço", "💪"),
        PalavraEn("leg", "perna", "🦵"),
        PalavraEn("foot", "pé", "🦶"),
        PalavraEn("finger", "dedo", "☝️"),
        PalavraEn("hair", "cabelo", "💇"),
        PalavraEn("brain", "cérebro", "🧠"),
        PalavraEn("heart", "coração", "❤️"),
        PalavraEn("face", "rosto", "🙂"),
        PalavraEn("head", "cabeça", "🗣️"),
    )

    /** "I have two eyes": a parte do corpo e quantas a gente tem. */
    data class QuantasPartes(val en: String, val pt: String, val emoji: String, val quantas: Int)

    val quantasPartes = listOf(
        QuantasPartes("eyes", "olhos", "👀", 2),
        QuantasPartes("ears", "orelhas", "👂", 2),
        QuantasPartes("nose", "nariz", "👃", 1),
        QuantasPartes("mouth", "boca", "👄", 1),
        QuantasPartes("hands", "mãos", "🙌", 2),
        QuantasPartes("fingers", "dedos", "🖐️", 10),
        QuantasPartes("feet", "pés", "🦶", 2),
        QuantasPartes("legs", "pernas", "🦵", 2),
        QuantasPartes("arms", "braços", "💪", 2),
        QuantasPartes("head", "cabeça", "🗣️", 1),
    )

    val bosque = listOf(
        PalavraEn("fairy", "fada", "🧚"),
        PalavraEn("mushroom", "cogumelo", "🍄"),
        PalavraEn("unicorn", "unicórnio", "🦄"),
        PalavraEn("owl", "coruja", "🦉"),
        PalavraEn("fox", "raposa", "🦊"),
        PalavraEn("butterfly", "borboleta", "🦋"),
        PalavraEn("squirrel", "esquilo", "🐿️"),
        PalavraEn("hedgehog", "ouriço", "🦔"),
    )

    // ------------------------------------------------------------------ mundo 18: vulcão (sentimentos)

    val sentimentos = listOf(
        PalavraEn("happy", "feliz", "😀"),
        PalavraEn("sad", "triste", "😢"),
        PalavraEn("angry", "bravo", "😠"),
        PalavraEn("scared", "com medo", "😨"),
        PalavraEn("tired", "cansado", "🥱"),
        PalavraEn("surprised", "surpreso", "😲"),
        PalavraEn("sick", "doente", "🤒"),
        PalavraEn("hungry", "com fome", "🤤"),
        PalavraEn("excited", "animado", "🤩"),
        PalavraEn("hot", "com calor", "🥵"),
        PalavraEn("cold", "com frio", "🥶"),
        PalavraEn("silly", "bobinho", "🤪"),
    )

    // ------------------------------------------------------------------ mundo 19: nuvens (tempo e estações)

    val tempo = listOf(
        PalavraEn("sunny", "ensolarado", "☀️"),
        PalavraEn("rainy", "chuvoso", "🌧️"),
        PalavraEn("cloudy", "nublado", "☁️"),
        PalavraEn("snowy", "com neve", "🌨️"),
        PalavraEn("windy", "com vento", "🌬️"),
        PalavraEn("stormy", "com tempestade", "⛈️"),
        PalavraEn("hot", "quente", "🥵"),
        PalavraEn("cold", "frio", "🥶"),
    )

    val estacoes = listOf(
        PalavraEn("spring", "primavera", "🌸"),
        PalavraEn("summer", "verão", "🏖️"),
        PalavraEn("fall", "outono", "🍂"),
        PalavraEn("winter", "inverno", "⛄"),
    )

    val ceu = listOf(
        PalavraEn("cloud", "nuvem", "☁️"),
        PalavraEn("rainbow", "arco-íris", "🌈"),
        PalavraEn("sun", "sol", "☀️"),
        PalavraEn("kite", "pipa", "🪁"),
        PalavraEn("bird", "passarinho", "🐦"),
        PalavraEn("plane", "avião", "✈️"),
        PalavraEn("balloon", "balão", "🎈"),
        PalavraEn("umbrella", "guarda-chuva", "☂️"),
        PalavraEn("lightning", "raio", "⚡"),
        PalavraEn("snowflake", "floco de neve", "❄️"),
    )

    // ------------------------------------------------------------------ mundo 20: cristal (alfabeto)

    /** Palavras curtas de três letras (consoante, vogal, consoante) para soletrar: cat, dog, sun. */
    val palavrasCvc = listOf(
        PalavraEn("cat", "gato", "🐱"),
        PalavraEn("dog", "cachorro", "🐶"),
        PalavraEn("sun", "sol", "☀️"),
        PalavraEn("pig", "porco", "🐷"),
        PalavraEn("hat", "chapéu", "🎩"),
        PalavraEn("bed", "cama", "🛏️"),
        PalavraEn("bus", "ônibus", "🚌"),
        PalavraEn("cup", "xícara", "☕"),
        PalavraEn("fox", "raposa", "🦊"),
        PalavraEn("hen", "galinha", "🐔"),
        PalavraEn("box", "caixa", "📦"),
        PalavraEn("bug", "inseto", "🐛"),
        PalavraEn("pen", "caneta", "🖊️"),
        PalavraEn("map", "mapa", "🗺️"),
        PalavraEn("red", "vermelho", "🔴"),
        PalavraEn("six", "seis", "6️⃣"),
    )

    /** Palavras de quatro letras para o fim do mundo do alfabeto. */
    val palavrasDeQuatro = listOf(
        PalavraEn("fish", "peixe", "🐟"),
        PalavraEn("frog", "sapo", "🐸"),
        PalavraEn("duck", "pato", "🦆"),
        PalavraEn("star", "estrela", "⭐"),
        PalavraEn("cake", "bolo", "🍰"),
        PalavraEn("bear", "urso", "🐻"),
        PalavraEn("moon", "lua", "🌙"),
        PalavraEn("tree", "árvore", "🌳"),
        PalavraEn("ball", "bola", "⚽"),
        PalavraEn("milk", "leite", "🥛"),
    )

    /** Palavra que começa com a letra (som da letra em inglês): "B is for ball". */
    val letraEPalavra = listOf(
        "A" to PalavraEn("apple", "maçã", "🍎"),
        "B" to PalavraEn("ball", "bola", "⚽"),
        "C" to PalavraEn("cat", "gato", "🐱"),
        "D" to PalavraEn("dog", "cachorro", "🐶"),
        "E" to PalavraEn("egg", "ovo", "🥚"),
        "F" to PalavraEn("fish", "peixe", "🐟"),
        "G" to PalavraEn("goat", "cabra", "🐐"),
        "H" to PalavraEn("hat", "chapéu", "🎩"),
        "I" to PalavraEn("ice cream", "sorvete", "🍦"),
        "J" to PalavraEn("juice", "suco", "🧃"),
        "K" to PalavraEn("kite", "pipa", "🪁"),
        "L" to PalavraEn("lion", "leão", "🦁"),
        "M" to PalavraEn("moon", "lua", "🌙"),
        "N" to PalavraEn("nose", "nariz", "👃"),
        "O" to PalavraEn("octopus", "polvo", "🐙"),
        "P" to PalavraEn("pig", "porco", "🐷"),
        "Q" to PalavraEn("queen", "rainha", "👸"),
        "R" to PalavraEn("rabbit", "coelho", "🐰"),
        "S" to PalavraEn("sun", "sol", "☀️"),
        "T" to PalavraEn("tiger", "tigre", "🐯"),
        "U" to PalavraEn("umbrella", "guarda-chuva", "☂️"),
        "V" to PalavraEn("violin", "violino", "🎻"),
        "W" to PalavraEn("whale", "baleia", "🐳"),
        "Y" to PalavraEn("yo-yo", "ioiô", "🪀"),
        "Z" to PalavraEn("zebra", "zebra", "🦓"),
    )

    /** Palavras de uso frequente (sight words) que a criança aprende a reconhecer de olho. */
    val sightWords = listOf("the", "and", "is", "see", "look", "go", "can", "my", "you", "we", "like", "it", "up", "big", "little", "yes", "no", "play", "here", "said")

    // ------------------------------------------------------------------ mundo 21: parque (plural)

    val brinquedos = listOf(
        PalavraEn("ball", "bola", "⚽"),
        PalavraEn("car", "carrinho", "🚗"),
        PalavraEn("kite", "pipa", "🪁"),
        PalavraEn("doll", "boneca", "🪆"),
        PalavraEn("teddy bear", "ursinho", "🧸"),
        PalavraEn("robot", "robô", "🤖"),
        PalavraEn("balloon", "balão", "🎈"),
        PalavraEn("yo-yo", "ioiô", "🪀"),
        PalavraEn("puzzle", "quebra-cabeça", "🧩"),
        PalavraEn("train", "trenzinho", "🚂"),
    )

    /** Plural de cada brinquedo (o inglês muda: "two balls", "three teddy bears"). */
    fun plural(en: String): String = when {
        en == "yo-yo" -> "yo-yos"
        en.endsWith("s") || en.endsWith("x") || en.endsWith("sh") || en.endsWith("ch") -> en + "es"
        en == "puppy" -> "puppies"
        else -> en + "s"
    }

    val esportesEMusica = listOf(
        PalavraEn("soccer", "futebol", "⚽"),
        PalavraEn("basketball", "basquete", "🏀"),
        PalavraEn("tennis", "tênis", "🎾"),
        PalavraEn("swimming", "natação", "🏊"),
        PalavraEn("guitar", "violão", "🎸"),
        PalavraEn("piano", "piano", "🎹"),
        PalavraEn("drum", "tambor", "🥁"),
        PalavraEn("violin", "violino", "🎻"),
        PalavraEn("trumpet", "trompete", "🎺"),
        PalavraEn("volleyball", "vôlei", "🏐"),
    )

    val parque = listOf(
        PalavraEn("Ferris wheel", "roda-gigante", "🎡"),
        PalavraEn("roller coaster", "montanha-russa", "🎢"),
        PalavraEn("carousel", "carrossel", "🎠"),
        PalavraEn("popcorn", "pipoca", "🍿"),
        PalavraEn("cotton candy", "algodão-doce", "🍭"),
        PalavraEn("ticket", "ingresso", "🎟️"),
    )

    // ------------------------------------------------------------------ mundo 22: robôs (my, your, he, she)

    val familia = listOf(
        PalavraEn("mom", "mamãe", "👩"),
        PalavraEn("dad", "papai", "👨"),
        PalavraEn("sister", "irmã", "👧"),
        PalavraEn("brother", "irmão", "👦"),
        PalavraEn("baby", "bebê", "👶"),
        PalavraEn("grandma", "vovó", "👵"),
        PalavraEn("grandpa", "vovô", "👴"),
        PalavraEn("family", "família", "👨‍👩‍👧‍👦"),
    )

    val meuSeu = listOf(
        FraseEn("This is my robot", "Este é o meu robô", "🤖"),
        FraseEn("This is my dog", "Este é o meu cachorro", "🐶"),
        FraseEn("This is your ball", "Esta é a sua bola", "⚽"),
        FraseEn("This is your cake", "Este é o seu bolo", "🎂"),
        FraseEn("He is my brother", "Ele é o meu irmão", "👦"),
        FraseEn("She is my sister", "Ela é a minha irmã", "👧"),
        FraseEn("She is my mom", "Ela é a minha mãe", "👩"),
        FraseEn("He is my dad", "Ele é o meu pai", "👨"),
        FraseEn("He is my grandpa", "Ele é o meu avô", "👴"),
        FraseEn("She is my grandma", "Ela é a minha avó", "👵"),
    )

    /** Presente contínuo: "He is running". */
    val fazendoAgora = listOf(
        FraseEn("He is running", "Ele está correndo", "🏃"),
        FraseEn("She is dancing", "Ela está dançando", "💃"),
        FraseEn("He is swimming", "Ele está nadando", "🏊"),
        FraseEn("She is reading", "Ela está lendo", "📖"),
        FraseEn("He is sleeping", "Ele está dormindo", "😴"),
        FraseEn("She is singing", "Ela está cantando", "🎤"),
        FraseEn("He is eating", "Ele está comendo", "😋"),
        FraseEn("She is painting", "Ela está pintando", "🎨"),
        FraseEn("He is climbing", "Ele está escalando", "🧗"),
        FraseEn("She is riding a bike", "Ela está andando de bicicleta", "🚴"),
    )

    val robos = listOf(
        PalavraEn("robot", "robô", "🤖"),
        PalavraEn("battery", "bateria", "🔋"),
        PalavraEn("gear", "engrenagem", "⚙️"),
        PalavraEn("computer", "computador", "💻"),
        PalavraEn("magnet", "ímã", "🧲"),
        PalavraEn("light bulb", "lâmpada", "💡"),
        PalavraEn("rocket", "foguete", "🚀"),
        PalavraEn("phone", "telefone", "📱"),
    )

    // ------------------------------------------------------------------ mundo 23: savana (descrever)

    /** Bicho da savana com tamanho e cor: "a big gray elephant". */
    data class Descricao(val tamanho: PalavraEn, val cor: PalavraEn, val bicho: PalavraEn) {
        val en: String get() = "a ${tamanho.en} ${cor.en} ${bicho.en}"
        val pt: String get() = "${bicho.pt} ${tamanho.pt} e ${cor.pt}"
    }

    private val big = adjetivos[0]
    private val small = adjetivos[1]
    private val tall = adjetivos[4]
    private val long = adjetivos[2]

    val savana = listOf(
        PalavraEn("elephant", "elefante", "🐘"),
        PalavraEn("giraffe", "girafa", "🦒"),
        PalavraEn("lion", "leão", "🦁"),
        PalavraEn("zebra", "zebra", "🦓"),
        PalavraEn("rhino", "rinoceronte", "🦏"),
        PalavraEn("hippo", "hipopótamo", "🦛"),
        PalavraEn("monkey", "macaco", "🐒"),
        PalavraEn("snake", "cobra", "🐍"),
        PalavraEn("ostrich", "avestruz", "🦤"),
        PalavraEn("crocodile", "crocodilo", "🐊"),
    )

    val descricoes = listOf(
        Descricao(big, gray, savana[0]),
        Descricao(tall, cor("yellow"), savana[1]),
        Descricao(big, cor("yellow"), savana[2]),
        Descricao(big, gray, savana[4]),
        Descricao(big, cor("purple"), savana[5]),
        Descricao(small, cor("brown"), savana[6]),
        Descricao(long, cor("green"), savana[7]),
        Descricao(long, cor("green"), savana[9]),
    )

    // ------------------------------------------------------------------ mundo 24: cores (perguntas)

    /** Uma pergunta curta em inglês com a resposta certa e respostas que não servem para ela. */
    data class PerguntaEResposta(val pergunta: String, val resposta: String, val emoji: String, val pt: String)

    val perguntasERespostas = listOf(
        PerguntaEResposta("What is it?", "It is a cat.", "🐱", "O que é? É um gato."),
        PerguntaEResposta("What is it?", "It is a ball.", "⚽", "O que é? É uma bola."),
        PerguntaEResposta("What is it?", "It is an apple.", "🍎", "O que é? É uma maçã."),
        PerguntaEResposta("What color is it?", "It is red.", "🍎", "Que cor é? É vermelho."),
        PerguntaEResposta("What color is it?", "It is yellow.", "🍌", "Que cor é? É amarelo."),
        PerguntaEResposta("What color is it?", "It is green.", "🐸", "Que cor é? É verde."),
        PerguntaEResposta("Where is it?", "It is in the box.", "📦", "Onde está? Está dentro da caixa."),
        PerguntaEResposta("Where is it?", "It is under the bed.", "🛏️", "Onde está? Está embaixo da cama."),
        PerguntaEResposta("How many?", "Three.", "🍓🍓🍓", "Quantos? Três."),
        PerguntaEResposta("How are you?", "I am fine, thank you.", "😀", "Como vai? Estou bem, obrigado."),
        PerguntaEResposta("What is your name?", "My name is Ana.", "👧", "Qual é o seu nome? Meu nome é Ana."),
        PerguntaEResposta("How old are you?", "I am six.", "🎂", "Quantos anos você tem? Tenho seis."),
    )

    // ------------------------------------------------------------------ mundo 25: estrelas (espaço e histórias)

    val espaco = listOf(
        PalavraEn("star", "estrela", "⭐"),
        PalavraEn("moon", "lua", "🌙"),
        PalavraEn("sun", "sol", "☀️"),
        PalavraEn("planet", "planeta", "🪐"),
        PalavraEn("rocket", "foguete", "🚀"),
        PalavraEn("astronaut", "astronauta", "🧑‍🚀"),
        PalavraEn("Earth", "Terra", "🌍"),
        PalavraEn("comet", "cometa", "☄️"),
        PalavraEn("alien", "extraterrestre", "👽"),
        PalavraEn("telescope", "telescópio", "🔭"),
        PalavraEn("satellite", "satélite", "🛰️"),
        PalavraEn("flying saucer", "disco voador", "🛸"),
    )

    /** Historinha curta em inglês com uma pergunta de compreensão e a figura que responde. */
    data class Historia(
        val frases: List<String>,
        val pergunta: String,
        val certa: String,
        val erradas: List<String>,
        val pt: String,
    )

    val historias = listOf(
        Historia(listOf("Tom has a red ball.", "He plays in the park."), "What does Tom have?", "⚽", listOf("🍎", "🐶", "🚗"), "O Tom tem uma bola vermelha e brinca no parque."),
        Historia(listOf("Ana has a little cat.", "The cat likes milk."), "What does the cat like?", "🥛", listOf("🍕", "🥕", "🍫"), "A Ana tem um gatinho que gosta de leite."),
        Historia(listOf("It is cold.", "Leo wears a coat and a scarf."), "What does Leo wear?", "🧥", listOf("🩳", "🩱", "🕶️"), "Está frio e o Leo veste casaco e cachecol."),
        Historia(listOf("The frog is green.", "It can jump very high."), "What can the frog do?", "🤸", listOf("🕊️", "📖", "🎤"), "O sapo é verde e pula bem alto."),
        Historia(listOf("Bia goes to school by bus.", "She has a blue backpack."), "How does Bia go to school?", "🚌", listOf("🚲", "✈️", "⛵"), "A Bia vai para a escola de ônibus."),
        Historia(listOf("It is sunny today.", "We go to the beach."), "Where do we go?", "🏖️", listOf("🏫", "🏥", "🏦"), "Hoje está sol e vamos à praia."),
        Historia(listOf("The astronaut flies in a rocket.", "She goes to the moon."), "Where does she go?", "🌙", listOf("🌳", "🏠", "🌊"), "A astronauta vai à lua de foguete."),
        Historia(listOf("Davi is hungry.", "He eats a big pizza."), "What does Davi eat?", "🍕", listOf("🍦", "🍌", "🥚"), "O Davi está com fome e come uma pizza."),
        Historia(listOf("The bird is in the tree.", "It sings a song."), "Where is the bird?", "🌳", listOf("📦", "🚗", "🛏️"), "O passarinho está na árvore cantando."),
        Historia(listOf("Malu is sad.", "Her kite is broken."), "How is Malu?", "😢", listOf("😀", "😠", "😴"), "A Malu está triste porque a pipa quebrou."),
        Historia(listOf("Grandma makes a cake.", "It is chocolate cake."), "Who makes the cake?", "👵", listOf("👶", "👮", "🤡"), "A vovó faz um bolo de chocolate."),
        Historia(listOf("The dog is under the bed.", "He is sleeping."), "Where is the dog?", "🛏️", listOf("🌳", "🚗", "🪑"), "O cachorro está dormindo embaixo da cama."),
    )

    // ------------------------------------------------------------------ opostos (mundos 8 e 18)

    /** Pares de opostos em inglês, com a tradução: big e small. */
    data class Oposto(val a: PalavraEn, val b: PalavraEn)

    val opostos = listOf(
        Oposto(PalavraEn("big", "grande", "🐘"), PalavraEn("small", "pequeno", "🐭")),
        Oposto(PalavraEn("long", "comprido", "🐍"), PalavraEn("short", "curto", "🐛")),
        Oposto(PalavraEn("fast", "rápido", "🐆"), PalavraEn("slow", "devagar", "🐌")),
        Oposto(PalavraEn("hot", "quente", "🔥"), PalavraEn("cold", "frio", "🧊")),
        Oposto(PalavraEn("happy", "feliz", "😀"), PalavraEn("sad", "triste", "😢")),
        Oposto(PalavraEn("day", "dia", "🌞"), PalavraEn("night", "noite", "🌃")),
        Oposto(PalavraEn("up", "para cima", "⬆️"), PalavraEn("down", "para baixo", "⬇️")),
        Oposto(PalavraEn("open", "aberto", "📖"), PalavraEn("closed", "fechado", "📕")),
        Oposto(PalavraEn("wet", "molhado", "💦"), PalavraEn("dry", "seco", "🏜️")),
    )
}
