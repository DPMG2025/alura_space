package br.com.casinhadosaber.dominio.fases

/**
 * Vocabulário das fases 51 a 250 do caminho Lógica (mundos 6 a 25, plano em docs/plano-1000-fases.md).
 * Só o arquivo deste caminho usa este objeto: outro caminho que precisar da mesma lista copia ou
 * pede para mover para o vocabulário comum, para os caminhos poderem crescer em paralelo sem conflito.
 * O vocabulário das fases 1 a 50 continua em Vocabulario e VocabularioDosMundos.
 */
object VocabularioLogica {

    // ------------------------------------------------------------------ temas dos mundos

    /** Figuras do tema de um mundo: (emoji, nome com artigo). */
    data class Tema(val nome: String, val figuras: List<Pair<String, String>>) {
        val emojis: List<String> get() = figuras.map { it.first }
        fun nomeDe(emoji: String): String = figuras.firstOrNull { it.first == emoji }?.second ?: ""
    }

    /** Um tema por mundo novo: o índice 0 é o mundo 6 (Fazenda), o 19 é o mundo 25 (Castelo das Estrelas). */
    val temas = listOf(
        Tema("fazenda", listOf("🐄" to "a vaca", "🐷" to "o porco", "🐔" to "a galinha", "🐑" to "a ovelha", "🐴" to "o cavalo", "🦆" to "o pato", "🐐" to "a cabra", "🚜" to "o trator", "🌽" to "o milho", "🥚" to "o ovo")),
        Tema("fundo do mar", listOf("🐠" to "o peixe", "🐙" to "o polvo", "🦀" to "o caranguejo", "🐬" to "o golfinho", "🐳" to "a baleia", "🦈" to "o tubarão", "🐚" to "a concha", "🐡" to "o baiacu", "🦑" to "a lula", "🐢" to "a tartaruga")),
        Tema("selva", listOf("🐒" to "o macaco", "🦜" to "o papagaio", "🐍" to "a cobra", "🐯" to "o tigre", "🐘" to "o elefante", "🦎" to "o lagarto", "🐊" to "o jacaré", "🍌" to "a banana", "🌴" to "a palmeira", "🦋" to "a borboleta")),
        Tema("polo norte", listOf("🐧" to "o pinguim", "🦭" to "a foca", "⛄" to "o boneco de neve", "❄️" to "o floco de neve", "🐋" to "a baleia", "🧊" to "o gelo", "🧣" to "o cachecol", "🧤" to "a luva", "🎿" to "o esqui", "🛷" to "o trenó")),
        Tema("circo", listOf("🎪" to "a tenda", "🤡" to "o palhaço", "🦁" to "o leão", "🎈" to "o balão", "🎩" to "a cartola", "🤹" to "o malabarista", "🐘" to "o elefante", "🍿" to "a pipoca", "🥁" to "o tambor", "🎺" to "o trompete")),
        Tema("deserto", listOf("🐫" to "o camelo", "🌵" to "o cacto", "🦂" to "o escorpião", "🐍" to "a cobra", "☀️" to "o sol", "🏜️" to "o deserto", "🦎" to "o lagarto", "🌴" to "a palmeira", "🐪" to "o dromedário", "💧" to "a gota de água")),
        Tema("cidade", listOf("🚗" to "o carro", "🚌" to "o ônibus", "🚦" to "o semáforo", "🏪" to "a loja", "🚲" to "a bicicleta", "🏢" to "o prédio", "🚕" to "o táxi", "🚒" to "o caminhão de bombeiro", "🏥" to "o hospital", "🏫" to "a escola")),
        Tema("castelo", listOf("🏰" to "o castelo", "👑" to "a coroa", "🐉" to "o dragão", "🛡️" to "o escudo", "🗡️" to "a espada", "🧙" to "o mago", "🦄" to "o unicórnio", "🔑" to "a chave", "💍" to "o anel", "🐴" to "o cavalo")),
        Tema("vale dos dinossauros", listOf("🦕" to "o dinossauro pescoçudo", "🦖" to "o tiranossauro", "🥚" to "o ovo", "🌋" to "o vulcão", "🌿" to "a folha", "🦴" to "o osso", "🦎" to "o lagarto", "🌴" to "a palmeira", "🐢" to "a tartaruga", "🐊" to "o jacaré")),
        Tema("ilha do tesouro", listOf("🏝️" to "a ilha", "🏴‍☠️" to "a bandeira pirata", "💰" to "o saco de moedas", "🗺️" to "o mapa", "🦜" to "o papagaio", "⚓" to "a âncora", "🐚" to "a concha", "🥥" to "o coco", "⛵" to "o barco", "💎" to "o diamante")),
        Tema("fábrica de doces", listOf("🍭" to "o pirulito", "🍬" to "a bala", "🍫" to "o chocolate", "🍩" to "a rosquinha", "🧁" to "o bolinho", "🍰" to "o pedaço de bolo", "🍪" to "o biscoito", "🍦" to "o sorvete", "🎂" to "o bolo", "🍮" to "o pudim")),
        Tema("bosque encantado", listOf("🍄" to "o cogumelo", "🧚" to "a fada", "🦄" to "o unicórnio", "🌳" to "a árvore", "🦉" to "a coruja", "🐿️" to "o esquilo", "🌸" to "a flor", "🦋" to "a borboleta", "🐌" to "o caracol", "🌙" to "a lua")),
        Tema("vulcão", listOf("🌋" to "o vulcão", "🔥" to "o fogo", "🐉" to "o dragão", "🦖" to "o tiranossauro", "🌶️" to "a pimenta", "⛑️" to "o capacete", "🧯" to "o extintor", "☄️" to "o cometa", "🦎" to "o lagarto", "💥" to "a explosão")),
        Tema("reino das nuvens", listOf("☁️" to "a nuvem", "🌈" to "o arco-íris", "🪁" to "a pipa", "🎈" to "o balão", "🦅" to "a águia", "✈️" to "o avião", "🌤️" to "o sol com nuvem", "🕊️" to "a pomba", "⚡" to "o raio", "☔" to "o guarda-chuva")),
        Tema("caverna de cristal", listOf("💎" to "o diamante", "🔮" to "a bola de cristal", "🦇" to "o morcego", "🕯️" to "a vela", "💍" to "o anel", "🔦" to "a lanterna", "👑" to "a coroa", "🗝️" to "a chave antiga", "⛏️" to "a picareta", "🕷️" to "a aranha")),
        Tema("parque de diversões", listOf("🎡" to "a roda gigante", "🎢" to "a montanha russa", "🎠" to "o carrossel", "🍿" to "a pipoca", "🎟️" to "o ingresso", "🎈" to "o balão", "🍭" to "o pirulito", "🤡" to "o palhaço", "🧸" to "o ursinho", "🍦" to "o sorvete")),
        Tema("cidade dos robôs", listOf("🤖" to "o robô", "⚙️" to "a engrenagem", "🔋" to "a bateria", "🔩" to "o parafuso", "💡" to "a lâmpada", "🛸" to "o disco voador", "📡" to "a antena", "🔧" to "a chave inglesa", "🧲" to "o ímã", "💻" to "o computador")),
        Tema("savana", listOf("🦁" to "o leão", "🦒" to "a girafa", "🦓" to "a zebra", "🐘" to "o elefante", "🦛" to "o hipopótamo", "🐆" to "o leopardo", "🦏" to "o rinoceronte", "🐃" to "o búfalo", "🌳" to "a árvore", "🐊" to "o jacaré")),
        Tema("vale das cores", listOf("🌈" to "o arco-íris", "🎨" to "a paleta", "🖍️" to "o giz de cera", "🖌️" to "o pincel", "🌷" to "a tulipa", "🦋" to "a borboleta", "🎈" to "o balão", "🍭" to "o pirulito", "🦜" to "o papagaio", "🌻" to "o girassol")),
        Tema("castelo das estrelas", listOf("🌟" to "a estrela brilhante", "⭐" to "a estrela", "🌙" to "a lua", "🪐" to "o planeta", "🚀" to "o foguete", "☄️" to "o cometa", "👑" to "a coroa", "🏰" to "o castelo", "🔭" to "o telescópio", "🌠" to "a estrela cadente")),
    )

    /** O tema do mundo [mundo] (de 6 a 25). */
    fun tema(mundo: Int): Tema = temas[(mundo - 6).coerceIn(0, temas.lastIndex)]

    // ------------------------------------------------------------------ formas e cores

    /** Formas desenhadas em todas as cores (todas masculinas: "o círculo azul"). */
    val formas = listOf("círculo", "quadrado", "coração")
    val formasPlural = listOf("círculos", "quadrados", "corações")

    /** Cores no masculino (para as formas) e no feminino (para as caixas). */
    val cores = listOf("vermelho", "azul", "amarelo", "verde", "roxo", "laranja")
    val coresFeminino = listOf("vermelha", "azul", "amarela", "verde", "roxa", "laranja")

    /** Cor de cada uma das [cores] para desenhar (botões, tinta, cartas). */
    val coresArgb = listOf(0xFFE53935, 0xFF1E88E5, 0xFFFDD835, 0xFF43A047, 0xFF8E24AA, 0xFFFB8C00)

    private val desenhosDasFormas = listOf(
        listOf("🔴", "🔵", "🟡", "🟢", "🟣", "🟠"),
        listOf("🟥", "🟦", "🟨", "🟩", "🟪", "🟧"),
        listOf("❤️", "💙", "💛", "💚", "💜", "🧡"),
    )

    /** O emoji da forma [forma] na cor [cor] (índices em [formas] e [cores]). */
    fun figura(forma: Int, cor: Int): String = desenhosDasFormas[forma][cor]

    /** Caixas coloridas (as quadradas), para as pistas de peso. */
    fun caixa(cor: Int): String = desenhosDasFormas[1][cor]

    /** As cores do arco-íris na ordem: vermelho, laranja, amarelo, verde, azul, roxo (índices em [cores]). */
    val arcoIris = listOf(0, 5, 2, 3, 1, 4)

    // ------------------------------------------------------------------ grupos (diagramas)

    /** Uma característica: [sim] completa "o que ..." e [nao] é o contrário. */
    data class Rotulo(val id: String, val sim: String, val nao: String)

    val rotulos = listOf(
        Rotulo("fazenda", "mora na fazenda", "não mora na fazenda"),
        Rotulo("mar", "mora no mar", "não mora no mar"),
        Rotulo("selva", "mora na selva", "não mora na selva"),
        Rotulo("gelo", "mora no gelo", "não mora no gelo"),
        Rotulo("penas", "tem penas", "não tem penas"),
        Rotulo("quatroPatas", "tem quatro patas", "não tem quatro patas"),
        Rotulo("fruta", "é fruta", "não é fruta"),
        Rotulo("veiculo", "é um veículo", "não é um veículo"),
        Rotulo("bicho", "é um bicho", "não é um bicho"),
        Rotulo("planta", "é uma planta", "não é uma planta"),
        Rotulo("vermelho", "é vermelho", "não é vermelho"),
        Rotulo("amarelo", "é amarelo", "não é amarelo"),
        Rotulo("verde", "é verde", "não é verde"),
        Rotulo("azul", "é azul", "não é azul"),
    ).associateBy { it.id }

    /** Uma figura com as características que ela tem (as que não estão na lista, ela não tem). */
    data class Coisa(val emoji: String, val nome: String, val tem: Set<String>)

    private fun c(emoji: String, nome: String, vararg tem: String) = Coisa(emoji, nome, tem.toSet())

    /** Bichos: onde moram (um lugar só) e como são. Só bichos sem dúvida para a criança. */
    val bichos = listOf(
        c("🐄", "a vaca", "fazenda", "quatroPatas"),
        c("🐷", "o porco", "fazenda", "quatroPatas"),
        c("🐑", "a ovelha", "fazenda", "quatroPatas"),
        c("🐴", "o cavalo", "fazenda", "quatroPatas"),
        c("🐔", "a galinha", "fazenda", "penas"),
        c("🦆", "o pato", "fazenda", "penas"),
        c("🦃", "o peru", "fazenda", "penas"),
        c("🐟", "o peixe", "mar"),
        c("🐬", "o golfinho", "mar"),
        c("🐙", "o polvo", "mar"),
        c("🐳", "a baleia", "mar"),
        c("🐯", "o tigre", "selva", "quatroPatas"),
        c("🐘", "o elefante", "selva", "quatroPatas"),
        c("🐊", "o jacaré", "selva", "quatroPatas"),
        c("🦜", "o papagaio", "selva", "penas"),
        c("🐧", "o pinguim", "gelo", "penas"),
        c("🦭", "a foca", "gelo"),
    )

    /** Coisas de cada tipo e de cada cor. */
    val coisas = listOf(
        c("🍎", "a maçã", "fruta", "vermelho"),
        c("🍓", "o morango", "fruta", "vermelho"),
        c("🍒", "a cereja", "fruta", "vermelho"),
        c("🍌", "a banana", "fruta", "amarelo"),
        c("🍋", "o limão siciliano", "fruta", "amarelo"),
        c("🍏", "a maçã verde", "fruta", "verde"),
        c("🍐", "a pera", "fruta", "verde"),
        c("🚗", "o carro", "veiculo", "vermelho"),
        c("🚒", "o caminhão de bombeiro", "veiculo", "vermelho"),
        c("🚕", "o táxi", "veiculo", "amarelo"),
        c("🚙", "o jipe", "veiculo", "azul"),
        c("🐞", "a joaninha", "bicho", "vermelho"),
        c("🦀", "o caranguejo", "bicho", "vermelho"),
        c("🐥", "o pintinho", "bicho", "amarelo"),
        c("🐸", "o sapo", "bicho", "verde"),
        c("🐳", "a baleia", "bicho", "azul"),
        c("🌹", "a rosa", "planta", "vermelho"),
        c("🌻", "o girassol", "planta", "amarelo"),
        c("🌳", "a árvore", "planta", "verde"),
        c("🍀", "o trevo", "planta", "verde"),
        c("🌵", "o cacto", "planta", "verde"),
        c("❤️", "o coração", "vermelho"),
        c("💛", "o coração amarelo", "amarelo"),
        c("💙", "o coração azul", "azul"),
        c("⭐", "a estrela", "amarelo"),
    )

    /** Onde cada bicho mora, para ligar o bicho à casa dele. */
    val lugares = mapOf("fazenda" to ("🏡" to "a fazenda"), "mar" to ("🌊" to "o mar"), "selva" to ("🌴" to "a selva"), "gelo" to ("🧊" to "o gelo"))

    // ------------------------------------------------------------------ histórias e causas

    /** Passos de uma história, na ordem em que acontecem (todos os emojis são diferentes entre as histórias). */
    val historias = listOf(
        listOf("🥚" to "o ovo", "🐣" to "o pintinho saindo do ovo", "🐥" to "o pintinho", "🐔" to "a galinha"),
        listOf("🌰" to "a semente", "🌱" to "o broto", "🌿" to "a plantinha", "🌳" to "a árvore"),
        listOf("🌅" to "a manhã", "☀️" to "o meio do dia", "🌇" to "o fim da tarde", "🌙" to "a noite"),
        listOf("👶" to "o bebê", "🧒" to "a criança", "🧑" to "o adulto", "🧓" to "o vovô"),
        listOf("🥣" to "a massa do bolo", "🎂" to "o bolo pronto", "😋" to "comer o bolo"),
        listOf("☁️" to "a nuvem", "🌧️" to "a chuva", "🌈" to "o arco-íris"),
        listOf("🍽️" to "o jantar", "🪥" to "escovar os dentes", "🛌" to "dormir"),
        listOf("❄️" to "a neve", "⛄" to "o boneco de neve", "💧" to "o boneco derretido"),
    )

    /** Causa e efeito: o que acontece depois. Os efeitos são todos diferentes. */
    data class Causa(val cena: String, val causa: String, val efeito: String, val nomeEfeito: String)

    val causas = listOf(
        Causa("🧊☀️", "o gelo fica no sol quente", "💧", "o gelo derrete e vira água"),
        Causa("🎈📌", "o balão encosta no alfinete", "💥", "o balão estoura"),
        Causa("🌽🔥", "o milho vai para a panela quente", "🍿", "o milho vira pipoca"),
        Causa("🥚🔥", "o ovo vai para a frigideira quente", "🍳", "vira ovo frito"),
        Causa("🌧️☀️", "chove e o sol aparece ao mesmo tempo", "🌈", "aparece o arco-íris"),
        Causa("🐛💤", "a lagarta dorme no casulo muito tempo", "🦋", "ela vira borboleta"),
        Causa("🌱💧", "a sementinha ganha água e sol", "🌻", "nasce uma flor"),
        Causa("🐔🥚", "a galinha choca o ovo", "🐣", "nasce um pintinho"),
        Causa("💧❄️", "a água vai para o congelador", "🧊", "a água vira gelo"),
        Causa("🪥🦷", "a criança escova os dentes", "✨", "os dentes ficam limpinhos"),
    )

    // ------------------------------------------------------------------ pessoas e bichinhos

    /** Crianças das pistas: (nome, é menina). */
    val criancas = listOf("Ana" to true, "Leo" to false, "Bia" to true, "Davi" to false, "Lia" to true, "Theo" to false, "Malu" to true, "Gabi" to true)

    /** Bichinhos de estimação das pistas: (emoji, nome com artigo). */
    val bichinhos = listOf("🐶" to "o cachorro", "🐱" to "o gato", "🐰" to "o coelho", "🐟" to "o peixinho", "🐢" to "a tartaruga", "🐦" to "o passarinho", "🐹" to "o hamster")

    /** Doces para a balança das trocas (todos com nome masculino: "um bolo", "dois bolos"). */
    val doces = listOf("🍍" to "abacaxi", "🎂" to "bolo", "🧁" to "bolinho", "🍫" to "chocolate", "🍭" to "pirulito", "🍦" to "sorvete", "🍪" to "biscoito")

    // ------------------------------------------------------------------ desenhos para pintar

    /**
     * Desenhos quadriculados simétricos (o lado direito é o espelho do esquerdo): "." é casinha branca e
     * cada algarismo é uma cor da [paleta] (1 é a primeira). Larguras pares, para o espelho dividir no meio.
     */
    data class Desenho(val nome: String, val linhas: List<String>, val paleta: List<Int>) {
        val colunas: Int get() = linhas.first().length
    }

    val desenhos = listOf(
        Desenho("o coração", listOf("11..11", "122221", "122221", ".1221.", "..11.."), listOf(0, 5)),
        Desenho("o cogumelo", listOf(".1111.", "121121", "111111", "..22..", "..22.."), listOf(0, 2)),
        Desenho("a árvore", listOf("..11..", ".1111.", "111111", ".1111.", "..22..", "..22.."), listOf(3, 5)),
        Desenho("a casinha", listOf("..11..", ".1111.", "111111", ".2332.", ".2332.", ".2222."), listOf(0, 2, 1)),
        Desenho("a borboleta", listOf("1....1", "11..11", "123321", "11..11", "1....1"), listOf(4, 2, 5)),
        Desenho("o foguete", listOf(".11.", "1221", "1221", "1111", "3113", "3..3"), listOf(1, 2, 0)),
        Desenho("a coroa", listOf("1.11.1", "111111", "121121", "111111"), listOf(2, 0)),
        Desenho("o coração grande", listOf(".11..11.", "11111111", "11211211", "11111111", ".111111.", "..1111..", "...11..."), listOf(0, 2)),
        Desenho("o robô", listOf(".1111.", "121121", "111111", ".3333.", ".3..3."), listOf(1, 2, 5)),
        Desenho("a estrela", listOf("...11...", "..1111..", "11111111", ".111111.", "..1221..", ".11..11."), listOf(2, 5)),
    )

    /** Símbolos que viram código de cor no pintar por símbolo. */
    val simbolos = listOf("●", "▲", "■", "★")
}
