package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.Objeto
import br.com.casinhadosaber.dominio.Vocabulario

/**
 * Vocabulário das fases 51 a 250 do caminho Números (matemática) (mundos 6 a 25, plano em docs/plano-1000-fases.md).
 * Só o arquivo deste caminho usa este objeto: outro caminho que precisar da mesma lista copia ou
 * pede para mover para o vocabulário comum, para os caminhos poderem crescer em paralelo sem conflito.
 * O vocabulário das fases 1 a 50 continua em Vocabulario e VocabularioDosMundos.
 */
object VocabularioNumeros {

    // ------------------------------------------------------------------ números falados

    private val centenasM = listOf("", "cem", "duzentos", "trezentos", "quatrocentos", "quinhentos", "seiscentos", "setecentos", "oitocentos", "novecentos")
    private val centenasF = listOf("", "cem", "duzentas", "trezentas", "quatrocentas", "quinhentas", "seiscentas", "setecentas", "oitocentas", "novecentas")

    /** Número por extenso de 0 a 1000 ("trezentos e quarenta e cinco", "duzentas e uma" no feminino). */
    fun extenso(n: Int, feminino: Boolean = false): String {
        require(n in 0..1000) { "Número $n fora de 0 a 1000" }
        if (n <= 100) return Vocabulario.porExtenso(n, feminino)
        if (n == 1000) return "mil"
        val c = n / 100
        val r = n % 100
        val nomeC = if (c == 1) "cento" else (if (feminino) centenasF else centenasM)[c]
        return if (r == 0) nomeC else "$nomeC e ${Vocabulario.porExtenso(r, feminino)}"
    }

    private val ordinaisM = listOf("", "primeiro", "segundo", "terceiro", "quarto", "quinto", "sexto", "sétimo", "oitavo", "nono", "décimo")

    /** Ordinal falado de 1 a 10 ("terceiro", "terceira"). */
    fun ordinal(n: Int, feminino: Boolean = false): String {
        require(n in 1..10) { "Ordinal $n fora de 1 a 10" }
        val m = ordinaisM[n]
        return if (feminino) m.dropLast(1) + "a" else m
    }

    /** Ordinal escrito: 3º (ou 3ª). */
    fun ordinalEscrito(n: Int, feminino: Boolean = false) = if (feminino) "${n}ª" else "${n}º"

    // ------------------------------------------------------------------ temas dos mundos (coisas para contar)

    val fazenda = listOf(
        Objeto("🐄", "vaca", "vacas", true), Objeto("🐷", "porquinho", "porquinhos", false),
        Objeto("🐔", "galinha", "galinhas", true), Objeto("🐑", "ovelha", "ovelhas", true),
        Objeto("🐴", "cavalo", "cavalos", false), Objeto("🦆", "pato", "patos", false),
        Objeto("🐐", "cabra", "cabras", true), Objeto("🐇", "coelho", "coelhos", false),
        Objeto("🐕", "cachorro", "cachorros", false), Objeto("🐈", "gato", "gatos", false),
        Objeto("🦃", "peru", "perus", false), Objeto("🐓", "galo", "galos", false),
    )
    val mar = listOf(
        Objeto("🐠", "peixinho", "peixinhos", false), Objeto("🐙", "polvo", "polvos", false),
        Objeto("🦀", "caranguejo", "caranguejos", false), Objeto("🐚", "concha", "conchas", true),
        Objeto("🐡", "baiacu", "baiacus", false), Objeto("🐬", "golfinho", "golfinhos", false),
        Objeto("🦑", "lula", "lulas", true), Objeto("🐳", "baleia", "baleias", true),
        Objeto("🦐", "camarão", "camarões", false), Objeto("🐢", "tartaruga", "tartarugas", true),
    )
    val selva = listOf(
        Objeto("🐒", "macaco", "macacos", false), Objeto("🍌", "banana", "bananas", true),
        Objeto("🦜", "papagaio", "papagaios", false), Objeto("🐍", "cobra", "cobras", true),
        Objeto("🐸", "sapo", "sapos", false), Objeto("🥥", "coco", "cocos", false),
        Objeto("🐯", "tigre", "tigres", false), Objeto("🦥", "preguiça", "preguiças", true),
    )
    val polo = listOf(
        Objeto("🐧", "pinguim", "pinguins", false), Objeto("❄️", "floco de neve", "flocos de neve", false),
        Objeto("⛄", "boneco de neve", "bonecos de neve", false), Objeto("🧊", "cubo de gelo", "cubos de gelo", false),
        Objeto("🦭", "foca", "focas", true), Objeto("🐟", "peixe", "peixes", false),
    )
    val circo = listOf(
        Objeto("🤡", "palhaço", "palhaços", false), Objeto("🎈", "balão", "balões", false),
        Objeto("🍿", "pipoca", "pipocas", true), Objeto("🎩", "cartola", "cartolas", true),
        Objeto("🦁", "leão", "leões", false), Objeto("🐘", "elefante", "elefantes", false),
    )
    val deserto = listOf(
        Objeto("🐫", "camelo", "camelos", false), Objeto("🌵", "cacto", "cactos", false),
        Objeto("🦂", "escorpião", "escorpiões", false), Objeto("🌴", "palmeira", "palmeiras", true),
        Objeto("🦎", "lagarto", "lagartos", false),
    )
    val cidade = listOf(
        Objeto("🚌", "ônibus", "ônibus", false), Objeto("🚗", "carro", "carros", false),
        Objeto("🚲", "bicicleta", "bicicletas", true), Objeto("🚕", "táxi", "táxis", false),
        Objeto("🏠", "casa", "casas", true), Objeto("🚦", "semáforo", "semáforos", false),
    )
    val castelo = listOf(
        Objeto("👑", "coroa", "coroas", true), Objeto("🛡️", "escudo", "escudos", false),
        Objeto("🗝️", "chave", "chaves", true), Objeto("🐉", "dragão", "dragões", false),
        Objeto("🦄", "unicórnio", "unicórnios", false),
    )
    val dinossauros = listOf(
        Objeto("🦕", "dinossauro", "dinossauros", false), Objeto("🦖", "tiranossauro", "tiranossauros", false),
        Objeto("🥚", "ovo", "ovos", false), Objeto("🦴", "osso", "ossos", false), Objeto("🌿", "planta", "plantas", true),
    )
    val ilha = listOf(
        Objeto("💎", "joia", "joias", true), Objeto("🪙", "moeda", "moedas", true),
        Objeto("🦜", "papagaio", "papagaios", false), Objeto("⚓", "âncora", "âncoras", true),
        Objeto("🐚", "concha", "conchas", true), Objeto("🗺️", "mapa", "mapas", false),
    )
    val doces = listOf(
        Objeto("🍭", "pirulito", "pirulitos", false), Objeto("🍬", "bala", "balas", true),
        Objeto("🍫", "chocolate", "chocolates", false), Objeto("🧁", "bolinho", "bolinhos", false),
        Objeto("🍩", "rosquinha", "rosquinhas", true), Objeto("🍪", "biscoito", "biscoitos", false),
    )
    val bosque = listOf(
        Objeto("🍄", "cogumelo", "cogumelos", false), Objeto("🧚", "fada", "fadas", true),
        Objeto("🦉", "coruja", "corujas", true), Objeto("🐿️", "esquilo", "esquilos", false),
        Objeto("🌰", "castanha", "castanhas", true), Objeto("🦔", "ouriço", "ouriços", false),
    )
    val vulcao = listOf(
        Objeto("🪨", "pedra", "pedras", true), Objeto("🦎", "lagartixa", "lagartixas", true),
        Objeto("🔥", "chama", "chamas", true), Objeto("🌋", "vulcão", "vulcões", false),
    )
    val nuvens = listOf(
        Objeto("☁️", "nuvem", "nuvens", true), Objeto("⭐", "estrela", "estrelas", true),
        Objeto("🪁", "pipa", "pipas", true), Objeto("🎈", "balão", "balões", false),
        Objeto("🕊️", "pombinha", "pombinhas", true),
    )
    val cristal = listOf(
        Objeto("💎", "cristal", "cristais", false), Objeto("🔦", "lanterna", "lanternas", true),
        Objeto("🦇", "morcego", "morcegos", false), Objeto("💍", "anel", "anéis", false),
    )
    val parque = listOf(
        Objeto("🎠", "cavalinho", "cavalinhos", false), Objeto("🎟️", "ingresso", "ingressos", false),
        Objeto("🍦", "sorvete", "sorvetes", false), Objeto("🎈", "balão", "balões", false),
        Objeto("🍭", "pirulito", "pirulitos", false),
    )
    val robos = listOf(
        Objeto("🤖", "robô", "robôs", false), Objeto("🔩", "parafuso", "parafusos", false),
        Objeto("⚙️", "engrenagem", "engrenagens", true), Objeto("🔋", "bateria", "baterias", true),
        Objeto("💡", "lâmpada", "lâmpadas", true),
    )
    val savana = listOf(
        Objeto("🦁", "leão", "leões", false), Objeto("🦒", "girafa", "girafas", true),
        Objeto("🦓", "zebra", "zebras", true), Objeto("🐘", "elefante", "elefantes", false),
        Objeto("🦛", "hipopótamo", "hipopótamos", false), Objeto("🐆", "guepardo", "guepardos", false),
    )
    val cores = listOf(
        Objeto("🖍️", "giz de cera", "gizes de cera", false), Objeto("🖌️", "pincel", "pincéis", false),
        Objeto("🎨", "paleta", "paletas", true), Objeto("🌈", "arco-íris", "arco-íris", false),
        Objeto("🎈", "balão", "balões", false),
    )
    val estrelas = listOf(
        Objeto("⭐", "estrela", "estrelas", true), Objeto("🌙", "lua", "luas", true),
        Objeto("🪐", "planeta", "planetas", false), Objeto("🚀", "foguete", "foguetes", false),
        Objeto("☄️", "cometa", "cometas", false),
    )

    /** As coisas do tema do mundo [mundo] (de 6 a 25, como no plano). */
    fun tema(mundo: Int): List<Objeto> = when (mundo) {
        6 -> fazenda; 7 -> mar; 8 -> selva; 9 -> polo; 10 -> circo
        11 -> deserto; 12 -> cidade; 13 -> castelo; 14 -> dinossauros; 15 -> ilha
        16 -> doces; 17 -> bosque; 18 -> vulcao; 19 -> nuvens; 20 -> cristal
        21 -> parque; 22 -> robos; 23 -> savana; 24 -> cores; else -> estrelas
    }

    // ------------------------------------------------------------------ tempo

    val diasDaSemana = listOf("domingo", "segunda", "terça", "quarta", "quinta", "sexta", "sábado")
    val meses = listOf("janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro")

    /** Relógios das meias horas: 1h30 a 12h30. */
    val relogiosMeia = listOf("🕜", "🕝", "🕞", "🕟", "🕠", "🕡", "🕢", "🕣", "🕤", "🕥", "🕦", "🕧")

    /** Hora falada: "três horas", "três e meia", "três e quinze", "meio-dia". */
    fun falaDaHora(h: Int, m: Int = 0): String {
        val hora = when (h) { 12 -> "meio-dia"; 1 -> "uma"; else -> extenso(h, true) }
        return when {
            m == 0 && h == 12 -> "meio-dia"
            m == 0 && h == 1 -> "uma hora"
            m == 0 -> "$hora horas"
            m == 30 -> "$hora e meia"
            else -> "$hora e ${extenso(m)}"
        }
    }

    /** Hora escrita: 3h, 3h30, 3h05. */
    fun horaEscrita(h: Int, m: Int = 0) = if (m == 0) "${h}h" else "${h}h" + m.toString().padStart(2, '0')

    // ------------------------------------------------------------------ medidas

    /** Uma coisa e a unidade com que a gente mede (centímetro, quilo, litro, hora). */
    data class Medida(val emoji: String, val coisa: String, val unidade: String)

    val unidades = listOf("centímetros", "quilos", "litros", "horas")
    val medidas = listOf(
        Medida("✏️", "o tamanho do lápis", "centímetros"), Medida("📏", "o comprimento da fita", "centímetros"),
        Medida("🐛", "o tamanho da minhoca", "centímetros"), Medida("🍉", "o peso da melancia", "quilos"),
        Medida("🎃", "o peso da abóbora", "quilos"), Medida("🥔", "o peso do saco de batatas", "quilos"),
        Medida("🥛", "o leite da caixinha", "litros"), Medida("🪣", "a água do balde", "litros"),
        Medida("🧃", "o suco da garrafa", "litros"), Medida("⚽", "o tempo do jogo de bola", "horas"),
        Medida("😴", "o tempo da soneca", "horas"), Medida("🎬", "o tempo do filme", "horas"),
    )

    /** Recipientes, do que cabe menos para o que cabe mais. */
    val recipientes = listOf("🥄" to "a colher", "☕" to "a xícara", "🥤" to "o copo", "🫖" to "o bule", "🪣" to "o balde", "🛁" to "a banheira")

    /** O que se mede em litros (líquidos) e o que não se mede. */
    val liquidos = listOf("🥛" to "leite", "🧃" to "suco", "💧" to "água", "🍵" to "chá", "🥤" to "refrigerante")
    val naoLiquidos = listOf("✏️" to "lápis", "🍎" to "maçã", "📏" to "régua", "🧱" to "tijolo", "🎲" to "dado", "🥕" to "cenoura")

    /** Pesos do castelo: o que vai no prato e o nome. */
    val pesados = listOf("🎃" to "a abóbora", "🍉" to "a melancia", "🧀" to "o queijo", "🍍" to "o abacaxi", "🥔" to "o saco de batatas")

    // ------------------------------------------------------------------ geometria

    data class Forma(val emoji: String, val nome: String, val lados: Int, val feminino: Boolean = false)

    val formasPlanas = listOf(
        Forma("🔺", "triângulo", 3), Forma("🟦", "quadrado", 4), Forma("🔴", "círculo", 0),
        Forma("▬", "retângulo", 4), Forma("🔶", "losango", 4),
    )

    data class Solido(val emoji: String, val nome: String, val objeto: String, val rola: Boolean)

    val solidos = listOf(
        Solido("🎲", "cubo", "o dado", false), Solido("⚽", "esfera", "a bola", true),
        Solido("🥫", "cilindro", "a lata", true), Solido("🍦", "cone", "a casquinha", true),
        Solido("📦", "bloco retangular", "a caixa", false),
    )

    /** Figuras do quebra-cabeça da ilha. */
    val figurasDaIlha = listOf("🏝️", "🦜", "🐢", "⚓", "🐙", "🦀", "⛵", "🐬")

    // ------------------------------------------------------------------ pintar por código

    /** Uma cor de tinta e o nome falado. */
    val tintas = listOf(
        0xFFE53935 to "vermelho", 0xFF43A047 to "verde", 0xFFFDD835 to "amarelo", 0xFF1E88E5 to "azul",
        0xFF8D6E63 to "marrom", 0xFFEC407A to "rosa", 0xFFFB8C00 to "laranja", 0xFF8E24AA to "roxo",
    )

    /** Desenho quadriculado: "." fica branco; cada letra é uma cor. */
    data class Desenho(val nome: String, val linhas: List<String>, val cores: Map<Char, Long>)

    val desenhos = listOf(
        Desenho("uma casa", listOf("..a..", ".aaa.", "aaaaa", "bbcbb", "bbcbb"), mapOf('a' to 0xFFE53935, 'b' to 0xFFFDD835, 'c' to 0xFF8D6E63)),
        Desenho("uma árvore", listOf(".aaa.", "aaaaa", ".aaa.", "..b..", "..b.."), mapOf('a' to 0xFF43A047, 'b' to 0xFF8D6E63)),
        Desenho("uma flor", listOf(".aaa.", "aabaa", ".aaa.", "..c..", ".cc.."), mapOf('a' to 0xFFEC407A, 'b' to 0xFFFDD835, 'c' to 0xFF43A047)),
        Desenho("um sorvete", listOf(".aaa.", "aaaaa", ".bbb.", "..b..", "..b.."), mapOf('a' to 0xFFEC407A, 'b' to 0xFFFB8C00)),
        Desenho("um barco", listOf("..a..", ".aa..", "aaa..", "bbbbb", ".bbb."), mapOf('a' to 0xFFFDD835, 'b' to 0xFF1E88E5)),
        Desenho("um coração", listOf("aa.aa", "aaaaa", "abaaa", ".aaa.", "..a.."), mapOf('a' to 0xFFE53935, 'b' to 0xFFEC407A)),
    )

    /** Cores de botão do siga a sequência. */
    val coresDosBotoes = listOf(0xFFE53935, 0xFF1E88E5, 0xFF43A047, 0xFFFDD835)

    // ------------------------------------------------------------------ dinheiro

    data class Produto(val emoji: String, val nome: String)

    val produtos = listOf(
        Produto("🍎", "a maçã"), Produto("🍞", "o pão"), Produto("🥛", "o leite"), Produto("🧃", "o suco"),
        Produto("🍌", "a banana"), Produto("🧀", "o queijo"), Produto("🍪", "o biscoito"), Produto("📒", "o caderno"),
        Produto("🖍️", "o giz de cera"), Produto("⚽", "a bola"), Produto("🧸", "o ursinho"), Produto("🍦", "o sorvete"),
    )

    /** Valor em centavos escrito: R$ 5, R$ 0,50, R$ 2,25. */
    fun dinheiroEscrito(centavos: Int): String {
        val r = centavos / 100
        val c = centavos % 100
        return if (c == 0) "R$ $r" else "R$ $r," + c.toString().padStart(2, '0')
    }

    /** Valor em centavos falado: "cinco reais", "cinquenta centavos", "dois reais e vinte e cinco centavos". */
    fun dinheiroFalado(centavos: Int): String {
        val r = centavos / 100
        val c = centavos % 100
        val reais = when (r) { 0 -> ""; 1 -> "um real"; else -> "${extenso(r)} reais" }
        val cent = when (c) { 0 -> ""; 1 -> "um centavo"; else -> "${extenso(c)} centavos" }
        return listOf(reais, cent).filter { it.isNotEmpty() }.joinToString(" e ").ifEmpty { "zero real" }
    }

    // ------------------------------------------------------------------ escrever números (ditado)

    /** Nomes de números divididos em pedaços (sílabas ou letras) para o ditado. */
    val nomesEmPedacos = mapOf(
        5 to listOf("CIN", "CO"), 7 to listOf("SE", "TE"), 8 to listOf("OI", "TO"), 9 to listOf("NO", "VE"),
        10 to listOf("D", "E", "Z"), 11 to listOf("ON", "ZE"), 12 to listOf("DO", "ZE"), 13 to listOf("TRE", "ZE"),
        15 to listOf("QUIN", "ZE"), 20 to listOf("VIN", "TE"), 30 to listOf("TRIN", "TA"), 40 to listOf("QUA", "REN", "TA"),
        50 to listOf("CIN", "QUEN", "TA"), 60 to listOf("SES", "SEN", "TA"), 70 to listOf("SE", "TEN", "TA"),
        80 to listOf("OI", "TEN", "TA"), 90 to listOf("NO", "VEN", "TA"), 100 to listOf("C", "E", "M"),
    )

    // ------------------------------------------------------------------ chance

    val bolinhas = listOf("🔴" to "vermelha", "🔵" to "azul", "🟢" to "verde", "🟡" to "amarela")
    val chances = listOf("impossível", "pouco provável", "muito provável", "certo")

    // ------------------------------------------------------------------ régua de dificuldade

    /**
     * A régua de cada mundo do caminho Números: [teto] é o maior número com que a criança mexe nas fases
     * do mundo (perguntas e peças), [passos] é quantos passos tem a conta mais comprida e [ideias] é
     * quantas ideias de matemática a criança já viu até ali (cada mundo soma uma). O nível nunca cai de
     * um mundo para o seguinte (conferido em FasesNumerosTest, junto com o teto nas perguntas sorteadas).
     */
    data class Regua(val mundo: Int, val teto: Int, val passos: Int, val ideias: Int) {
        val nivel: Int get() = ideias + passos * 10 + when { teto <= 30 -> 0; teto <= 100 -> 20; else -> 40 }
    }

    val regua: List<Regua> = (6..25).map { m ->
        val teto = when { m <= 8 -> 30; m <= 17 -> 100; else -> 1000 }
        val passos = if (m >= 19) 2 else 1
        Regua(m, teto, passos, m)
    }
}
