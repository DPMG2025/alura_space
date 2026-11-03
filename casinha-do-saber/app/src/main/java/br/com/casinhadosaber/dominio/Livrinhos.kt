package br.com.casinhadosaber.dominio

/**
 * Livrinhos para ler sozinho: historinhas curtas em que toda palavra dá para ler com as sílabas que a criança
 * já encontrou no caminho Letras ([Leitura]). Cada livrinho abre quando a criança vence a fase [Livrinho.fase]
 * de Letras (a que ensina a sílaba mais nova que ele usa; o teste confere que é exatamente essa).
 *
 * Em cada página: uma frase curta em letras grandes e uma cena ([CenarioDoLivrinho] desenhado em Canvas e
 * figuras em emoji). Tocar numa palavra faz o narrador ler a palavra (segurar lê pedacinho por pedacinho) e
 * "Ler para mim" lê a página. No fim, 1 ou 2 perguntas de tocar na figura certa e um marcador para a estante.
 */
enum class CenarioDoLivrinho(val ceu: Long, val chao: Long) {
    JARDIM(0xFFBDE8FF, 0xFF8BD17C),
    MATA(0xFFCDEFD8, 0xFF5FAE6A),
    LAGO(0xFFCFEFFF, 0xFF7FCB8C),
    PRAIA(0xFFBFE9FF, 0xFFF6DFA4),
    MAR(0xFFBFE9FF, 0xFF4FA9DB),
    CASA(0xFFFFF1DC, 0xFFD9B48C),
    ESCOLA(0xFFFFF6E0, 0xFFB98B5E),
    NOITE(0xFF2E3A7A, 0xFF3F6E4E),
    CEU(0xFF1E2A5A, 0xFF6B6F8F),
    FESTA(0xFFFFE3F1, 0xFFE7B6D3),
}

/** Uma página: a frase e a cena (as figuras aparecem da esquerda para a direita). */
data class PaginaDoLivrinho(val texto: String, val cenario: CenarioDoLivrinho, val figuras: List<String>)

/** Uma figura para tocar na pergunta do fim, com o nome que o narrador fala. */
data class FiguraDoLivrinho(val emoji: String, val nome: String)

/** Pergunta do fim (narrada): a [certa] e as outras figuras, que aparecem embaralhadas pela posição fixa [ordem]. */
data class PerguntaDoLivrinho(val pergunta: String, val certa: FiguraDoLivrinho, val erradas: List<FiguraDoLivrinho>) {
    /** As opções numa ordem fixa (sem sorteio): a certa muda de lugar de uma pergunta para outra. */
    fun opcoes(posicaoDaCerta: Int): List<FiguraDoLivrinho> =
        erradas.toMutableList().apply { add(Math.floorMod(posicaoDaCerta, erradas.size + 1), certa) }
}

data class Livrinho(
    val id: String,
    val titulo: String,
    val capa: String,
    /** Fase de Letras (índice, como [Fase.numero]) que precisa estar vencida para abrir. */
    val fase: Int,
    /** Marcador de página que vai para a estante quando a criança termina. */
    val marcador: String,
    val paginas: List<PaginaDoLivrinho>,
    val perguntas: List<PerguntaDoLivrinho>,
) {
    /** Tudo o que a criança lê sozinha (título e páginas); as perguntas são narradas. */
    val textosParaLer: List<String> get() = listOf(titulo) + paginas.map { it.texto }
}

object Livrinhos {

    private fun p(texto: String, cenario: CenarioDoLivrinho, vararg figuras: String) = PaginaDoLivrinho(texto, cenario, figuras.toList())
    private fun f(emoji: String, nome: String) = FiguraDoLivrinho(emoji, nome)
    private fun q(pergunta: String, certa: FiguraDoLivrinho, vararg erradas: FiguraDoLivrinho) = PerguntaDoLivrinho(pergunta, certa, erradas.toList())

    private val J = CenarioDoLivrinho.JARDIM
    private val M = CenarioDoLivrinho.MATA
    private val L = CenarioDoLivrinho.LAGO
    private val P = CenarioDoLivrinho.PRAIA
    private val MAR = CenarioDoLivrinho.MAR
    private val C = CenarioDoLivrinho.CASA
    private val E = CenarioDoLivrinho.ESCOLA
    private val N = CenarioDoLivrinho.NOITE
    private val CEU = CenarioDoLivrinho.CEU
    private val F = CenarioDoLivrinho.FESTA

    val todos: List<Livrinho> = listOf(
        // ------------------------------------------------ famílias silábicas (fase 27)
        Livrinho(
            "pato", "O pato nada", "🦆", 26, "🦆",
            listOf(
                p("O pato nada no lago.", L, "🦆", "🌿"),
                p("A pata nada e nada.", L, "🦆", "🦆"),
                p("O pato pula na lama.", L, "🦆", "🟤"),
                p("A pata lava o pato.", L, "🦆", "💦", "🦆"),
                p("O pato fica bonito.", L, "🦆", "✨"),
            ),
            listOf(q("O que o pato faz no lago?", f("🏊", "ele nada"), f("😴", "ele dorme"), f("🍽️", "ele come"))),
        ),
        Livrinho(
            "bola", "A bola da Lia", "⚽", 26, "⚽",
            listOf(
                p("A bola da Lia rola.", J, "🐛", "⚽"),
                p("A bola rola no mato.", M, "⚽", "🌿"),
                p("Tito pega a bola.", M, "🐿️", "⚽"),
                p("Tito joga a bola.", J, "🐿️", "⚽"),
                p("A bola sobe, sobe.", J, "⚽", "☁️"),
                p("Lia pega a bola!", J, "🐛", "⚽", "🎉"),
            ),
            listOf(
                q("Quem pega a bola no mato?", f("🐿️", "Tito"), f("🐛", "Lia"), f("🦉", "a coruja")),
                q("Para onde a bola sobe?", f("☁️", "para o céu"), f("🕳️", "para o buraco"), f("🛏️", "para a cama")),
            ),
        ),
        Livrinho(
            "sopa", "A sopa de batata", "🥣", 26, "🥔",
            listOf(
                p("A panela fica no fogo.", C, "🍲", "🔥"),
                p("Pipo lava a batata.", C, "🤖", "🥔", "💦"),
                p("Lia coloca o tomate.", C, "🐛", "🍅", "🍲"),
                p("Tito mexe a sopa.", C, "🐿️", "🥄", "🍲"),
                p("A sopa fica boa.", C, "🍲", "😋"),
                p("Lia toma a sopa toda.", C, "🐛", "🥣"),
            ),
            listOf(
                q("O que Pipo lava?", f("🥔", "batata"), f("🍌", "banana"), f("🧦", "meia")),
                q("Onde fica a panela?", f("🔥", "no fogo"), f("🛏️", "na cama"), f("🌊", "no mar")),
            ),
        ),
        Livrinho(
            "gato", "O gato e o rato", "🐱", 26, "🐭",
            listOf(
                p("O gato mora na casa.", C, "🐱", "🏠"),
                p("O rato mora na toca.", C, "🐭", "🕳️"),
                p("O gato mia.", C, "🐱", "🎵"),
                p("O rato pula na mesa.", C, "🐭", "🍽️"),
                p("O rato pega o bolo.", C, "🐭", "🎂"),
                p("O gato pula na mesa.", C, "🐱", "🍽️"),
                p("O rato pula na toca. Ufa!", C, "🐭", "🕳️", "🎂"),
            ),
            listOf(
                q("O que o rato pega?", f("🎂", "bolo"), f("🍎", "maçã"), f("🥕", "cenoura")),
                q("Onde mora o rato?", f("🕳️", "na toca"), f("🌳", "na árvore"), f("🚗", "no carro")),
            ),
        ),
        Livrinho(
            "coruja-lua", "A coruja e a lua", "🦉", 26, "🌙",
            listOf(
                p("A lua sobe no mato.", N, "🌙", "🌲"),
                p("A coruja pia: u, u.", N, "🦉", "🎵"),
                p("A coruja voa no mato.", N, "🦉", "🌲", "🌲"),
                p("A coruja fica no muro.", N, "🦉", "🧱"),
                p("A lua ilumina o mato.", N, "🌙", "✨", "🌲"),
                p("Nana, coruja!", N, "🦉", "🌙", "⭐"),
            ),
            listOf(q("Onde a coruja fica?", f("🧱", "no muro"), f("🚗", "no carro"), f("🛏️", "na cama"))),
        ),
        Livrinho(
            "fada-sapo", "A fada e o sapo", "🧚", 26, "🐸",
            listOf(
                p("O sapo mora no lago.", L, "🐸", "🪷"),
                p("A fada voa no lago.", L, "🧚", "✨"),
                p("O sapo pede uma pipa.", L, "🐸", "🪁"),
                p("A fada pega uma pipa.", L, "🧚", "🪁"),
                p("O sapo voa na pipa!", J, "🐸", "🪁", "☁️"),
                p("O sapo ri e ri.", J, "🐸", "😄"),
            ),
            listOf(q("O que o sapo pede?", f("🪁", "pipa"), f("⚽", "bola"), f("🎂", "bolo"))),
        ),

        // ------------------------------------------------ CH, LH e NH (fase 33)
        Livrinho(
            "chuva", "A chuva", "🌧️", 32, "☔",
            listOf(
                p("Olha a chuva!", C, "🌧️", "🪟"),
                p("A chuva molha a janela.", C, "🌧️", "🪟", "💧"),
                p("Lia pega a capa e a bota.", C, "🐛", "🧥", "👢"),
                p("Tito acha a capa dele.", C, "🐿️", "🧥"),
                p("Na rua, a chuva molha tudo.", J, "🌧️", "🏘️"),
                p("Lia pula na lama.", J, "🐛", "🟤", "💦"),
                p("A chuva acaba.", J, "🌈", "🐛", "🐿️"),
            ),
            listOf(
                q("O que a chuva molha?", f("🪟", "a janela"), f("📚", "o livro"), f("🍰", "o bolo")),
                q("Onde Lia pula?", f("🟤", "na lama"), f("🛏️", "na cama"), f("🏊", "na piscina")),
            ),
        ),
        Livrinho(
            "galinha", "A galinha e o ninho", "🐔", 32, "🐣",
            listOf(
                p("A galinha mora no ninho.", M, "🐔", "🪹"),
                p("A galinha bota ovo.", M, "🐔", "🥚"),
                p("O ovo fica no ninho.", M, "🥚", "🪹"),
                p("A galinha choca o ovo.", M, "🐔", "🥚"),
                p("O ovo mexe!", M, "🥚", "✨"),
                p("Olha o filhote!", M, "🐔", "🐣"),
            ),
            listOf(
                q("O que a galinha bota?", f("🥚", "ovo"), f("🍎", "maçã"), f("⚽", "bola")),
                q("Quem sai do ovo?", f("🐣", "o filhote"), f("🐟", "o peixe"), f("🐶", "o cachorro")),
            ),
        ),

        // ------------------------------------------------ BR, PR e TR (fase 39)
        Livrinho(
            "prima", "A prima da Lia", "👧", 38, "🏖️",
            listOf(
                p("A prima da Lia mora na ilha.", P, "👧", "🏖️"),
                p("Lia visita a prima.", P, "🐛", "🌊"),
                p("A prima pega a bola.", P, "👧", "⚽"),
                p("Lia joga a bola para a prima.", P, "🐛", "⚽", "👧"),
                p("A prima pega a bola. Bravo!", P, "👧", "⚽", "👏"),
                p("Lia acha uma pena na ilha.", P, "🐛", "🪶"),
            ),
            listOf(q("Onde mora a prima?", f("🏝️", "na ilha"), f("🏔️", "na montanha"), f("🏙️", "na cidade"))),
        ),
        Livrinho(
            "cabra", "A cabra", "🐐", 38, "🐐",
            listOf(
                p("A cabra mora no mato.", M, "🐐", "🌿"),
                p("A cabra sobe a trilha.", M, "🐐", "⛰️"),
                p("A cabra come a folha.", M, "🐐", "🍃"),
                p("A cabra bebe no rio.", L, "🐐", "💧"),
                p("A cabra fica na trilha.", M, "🐐", "🌄"),
                p("A cabra cochila.", N, "🐐", "🌙"),
            ),
            listOf(q("O que a cabra come?", f("🍃", "folha"), f("🍕", "pizza"), f("🍦", "sorvete"))),
        ),

        // ------------------------------------------------ palavras da fazenda (fase 51)
        Livrinho(
            "girafa", "A girafa", "🦒", 50, "🦒",
            listOf(
                p("A girafa mora na savana.", M, "🦒", "🌳"),
                p("A girafa come a folha do galho.", M, "🦒", "🍃"),
                p("O macaco sobe na girafa.", M, "🐒", "🦒"),
                p("O macaco pega a banana.", M, "🐒", "🍌"),
                p("A girafa ri do macaco.", M, "🦒", "😄", "🐒"),
            ),
            listOf(q("Quem sobe na girafa?", f("🐒", "o macaco"), f("🐘", "o elefante"), f("🐊", "o jacaré"))),
        ),
        Livrinho(
            "salada", "A salada do Pipo", "🥗", 50, "🥗",
            listOf(
                p("Pipo lava a cebola.", C, "🤖", "🧅", "💦"),
                p("Pipo lava o tomate e o pepino.", C, "🤖", "🍅", "🥒"),
                p("Pipo pica a cebola.", C, "🤖", "🥕"),
                p("Pipo coloca tudo na tigela.", C, "🤖", "🥣"),
                p("A salada fica colorida!", C, "🥗", "🌈"),
            ),
            listOf(
                q("O que Pipo faz?", f("🥗", "salada"), f("🍰", "bolo"), f("🍕", "pizza")),
                q("Onde Pipo coloca tudo?", f("🥣", "na tigela"), f("👟", "no sapato"), f("🛏️", "na cama")),
            ),
        ),

        // ------------------------------------------------ som nasal (fase 61)
        Livrinho(
            "balao", "O balão do Tito", "🎈", 60, "🎈",
            listOf(
                p("Tito ganha o balão.", J, "🐿️", "🎈"),
                p("O vento leva o balão.", J, "🌬️", "🎈"),
                p("Tito pula e pula.", J, "🐿️", "🎈"),
                p("O balão para na ponte.", L, "🎈", "🌉"),
                p("Lia pega o balão na mão.", L, "🐛", "🎈"),
                p("Tito canta e dança.", J, "🐿️", "🎵", "🎈"),
            ),
            listOf(
                q("Quem leva o balão?", f("🌬️", "o vento"), f("🐶", "o cão"), f("🚗", "o carro")),
                q("Onde o balão para?", f("🌉", "na ponte"), f("🏠", "na casa"), f("🌳", "na árvore")),
            ),
        ),
        Livrinho(
            "pao", "O pão da mamãe", "🍞", 60, "🍞",
            listOf(
                p("Mamãe pega a farinha.", C, "👩", "🌾"),
                p("Lia ajuda a mamãe.", C, "🐛", "👩"),
                p("O pão fica no fogão.", C, "🍞", "🔥"),
                p("Mamãe tira o pão do fogão.", C, "👩", "🍞"),
                p("Lia ganha o pão na mão.", C, "🐛", "🍞"),
                p("Obrigada, mamãe!", C, "🐛", "❤️", "👩"),
            ),
            listOf(
                q("Quem ajuda a mamãe?", f("🐛", "Lia"), f("🐿️", "Tito"), f("🤖", "Pipo")),
                q("Onde o pão fica?", f("🔥", "no fogão"), f("🛁", "na banheira"), f("🚗", "no carro")),
            ),
        ),
        Livrinho(
            "leao", "O leão cansado", "🦁", 60, "🦁",
            listOf(
                p("O leão anda na savana.", M, "🦁", "🌳"),
                p("O leão cansa.", M, "🦁", "💤"),
                p("O leão cochila na savana.", P, "🦁", "🏜️"),
                p("O leão ronca: ron, ron.", P, "🦁", "💤"),
                p("O vento bate na juba.", P, "🌬️", "🦁"),
                p("O leão abre o olho.", P, "🦁", "👀"),
            ),
            listOf(q("Onde o leão cochila?", f("🌳", "na savana"), f("🛏️", "na cama"), f("🌊", "no mar"))),
        ),

        // ------------------------------------------------ R, S e L no fim (fase 81)
        Livrinho(
            "escola", "A escola da coruja", "🏫", 80, "✏️",
            listOf(
                p("A coruja abre a escola.", E, "🦉", "🏫"),
                p("A turma chega cedo.", E, "🐛", "🐿️", "🤖"),
                p("A coruja desenha uma estrela.", E, "🦉", "⭐"),
                p("Tito conta as estrelas.", E, "🐿️", "⭐", "⭐"),
                p("Lia pinta a estrela de azul.", E, "🐛", "🔵", "⭐"),
                p("Pipo pinta a estrela de amarelo.", E, "🤖", "🟡", "⭐"),
                p("A coruja gosta da turma.", E, "🦉", "❤️"),
            ),
            listOf(
                q("O que a coruja desenha?", f("⭐", "uma estrela"), f("🌙", "a lua"), f("🐟", "um peixe")),
                q("De que cor Lia pinta a estrela?", f("🔵", "azul"), f("🔴", "vermelho"), f("🟢", "verde")),
            ),
        ),
        Livrinho(
            "barco", "O barco no mar", "⛵", 80, "⛵",
            listOf(
                p("Pipo e Tito vão para o mar.", MAR, "🤖", "🐿️", "⛵"),
                p("O barco anda no mar azul.", MAR, "⛵", "🌊"),
                p("O sol brilha.", MAR, "☀️", "⛵"),
                p("O peixe pula na onda.", MAR, "🐟", "🌊"),
                p("Tito pesca, mas o peixe foge.", MAR, "🐿️", "🎣", "🐟"),
                p("O barco volta para a areia.", P, "⛵", "🏖️"),
            ),
            listOf(q("Quem pula na onda?", f("🐟", "o peixe"), f("🐶", "o cão"), f("🐱", "o gato"))),
        ),
        Livrinho(
            "festa", "A festa do Tito", "🎉", 80, "🎂",
            listOf(
                p("A festa do Tito vai ser no domingo.", F, "🐿️", "🎉"),
                p("Lia leva o bolo de banana.", F, "🐛", "🎂"),
                p("Pipo leva o suco de uva.", F, "🤖", "🧃"),
                p("A coruja leva a vela.", F, "🦉", "🕯️"),
                p("A turma canta: viva o Tito!", F, "🎵", "🐿️", "🎈"),
                p("Tito sopra a vela.", F, "🐿️", "🕯️", "🎂"),
                p("Viva a festa!", F, "🎉", "🎈", "🎊"),
            ),
            listOf(
                q("O que Pipo leva?", f("🧃", "suco"), f("🎂", "bolo"), f("🕯️", "vela")),
                q("Quem sopra a vela?", f("🐿️", "Tito"), f("🐛", "Lia"), f("🦉", "a coruja")),
            ),
        ),
        Livrinho(
            "sol-lua", "O sol e a lua", "☀️", 80, "☀️",
            listOf(
                p("De dia, o sol brilha.", J, "☀️", "🌻"),
                p("O sol seca a roupa.", J, "☀️", "👕"),
                p("De noite, a lua sobe.", N, "🌙", "🏠"),
                p("A lua ilumina a rua.", N, "🌙", "🏘️"),
                p("Cada estrela pisca.", N, "⭐", "✨", "⭐"),
                p("Boa noite, lua!", N, "🌙", "😴"),
            ),
            listOf(q("O que o sol seca?", f("👕", "a roupa"), f("🍦", "o sorvete"), f("🐟", "o peixe"))),
        ),

        // ------------------------------------------------ RR, SS, QU e GU (fase 91)
        Livrinho(
            "esquilo", "O esquilo esquecido", "🐿️", 90, "🌰",
            listOf(
                p("O esquilo Tito mora no alto da mata.", M, "🐿️", "🌳"),
                p("Tito esconde o coquinho.", M, "🐿️", "🥥"),
                p("Tito não acha o coquinho.", M, "🐿️", "❓"),
                p("Pipo ajuda o esquilo.", M, "🤖", "🐿️"),
                p("Pipo acha o coquinho debaixo da folha.", M, "🤖", "🍃", "🥥"),
                p("Tito pula e ri.", M, "🐿️", "😄"),
            ),
            listOf(q("O que Tito esconde?", f("🥥", "o coquinho"), f("🍌", "a banana"), f("🧸", "o brinquedo"))),
        ),
        Livrinho(
            "cachorro", "O cachorro e o osso", "🐶", 90, "🦴",
            listOf(
                p("O cachorro cava na terra.", J, "🐶", "🟫"),
                p("O cachorro acha o osso.", J, "🐶", "🦴"),
                p("O osso vai para a casinha.", J, "🦴", "🏠"),
                p("O gato quer o osso.", J, "🐱", "🦴"),
                p("O cachorro late: au, au!", J, "🐶", "🎵"),
                p("O gato pula no muro.", J, "🐱", "🧱"),
            ),
            listOf(
                q("O que o cachorro acha?", f("🦴", "o osso"), f("🧦", "a meia"), f("⚽", "a bola")),
                q("Onde o gato pula?", f("🧱", "no muro"), f("🛁", "na banheira"), f("🚗", "no carro")),
            ),
        ),
        Livrinho(
            "foguete", "O foguete do Pipo", "🚀", 90, "🚀",
            listOf(
                p("Pipo monta o foguete.", CEU, "🤖", "🚀"),
                p("O foguete sobe, sobe, sobe.", CEU, "🚀", "☁️"),
                p("Pipo passa pela lua.", CEU, "🚀", "🌙"),
                p("Pipo acena para a Terra.", CEU, "🤖", "👋", "🌍"),
                p("Pipo volta para casa.", CEU, "🚀", "🏠"),
                p("Pipo quer ir de novo!", C, "🤖", "😄"),
            ),
            listOf(q("Por onde Pipo passa?", f("🌙", "pela lua"), f("🏖️", "pela praia"), f("🏫", "pela escola"))),
        ),

        // ------------------------------------------------ Ç, GE, GI e Z (fase 101)
        Livrinho(
            "palhaco", "O palhaço", "🤡", 100, "🤡",
            listOf(
                p("O circo chegou na cidade.", F, "🎪", "🏙️"),
                p("O palhaço usa o nariz vermelho.", F, "🤡", "🔴"),
                p("O palhaço faz a turma rir.", F, "🤡", "😄"),
                p("O macaco anda na corda.", F, "🐒", "🪢"),
                p("A girafa dança.", F, "🦒", "🎵"),
                p("Lia bate palmas.", F, "🐛", "👏"),
            ),
            listOf(q("De que cor é o nariz do palhaço?", f("🔴", "vermelho"), f("🔵", "azul"), f("🟡", "amarelo"))),
        ),
        Livrinho(
            "tesouro", "A caça ao tesouro", "🗺️", 100, "🗺️",
            listOf(
                p("A coruja esconde o tesouro.", P, "🦉", "📦"),
                p("A turma faz o mapa.", P, "🗺️", "🐿️", "🐛"),
                p("Dez passos para a esquerda.", P, "👣", "⬅️"),
                p("Tito cava na areia.", P, "🐿️", "🏖️"),
                p("Lia acha a caixa.", P, "🐛", "📦"),
                p("Na caixa, doce de coco!", P, "📦", "🍬", "🥥"),
            ),
            listOf(
                q("O que Lia acha?", f("📦", "a caixa"), f("👟", "o sapato"), f("🎈", "o balão")),
                q("O que tem na caixa?", f("🍬", "doce"), f("🧸", "um urso"), f("🪁", "uma pipa")),
            ),
        ),

        // ------------------------------------------------ encontros consonantais (fase 111)
        Livrinho(
            "flor", "A flor da Lia", "🌸", 110, "🌸",
            listOf(
                p("Lia planta a semente.", J, "🐛", "🌰"),
                p("Pipo rega a terra.", J, "🤖", "💧"),
                p("A chuva cai devagar.", J, "🌧️", "🌱"),
                p("Brota uma folha.", J, "🌱"),
                p("A planta cresce.", J, "🪴"),
                p("Nasce uma flor amarela!", J, "🌼", "🐛"),
            ),
            listOf(
                q("O que Lia planta?", f("🌰", "a semente"), f("🍕", "a pizza"), f("🧦", "a meia")),
                q("O que nasce no fim?", f("🌼", "uma flor"), f("🐟", "um peixe"), f("🚗", "um carro")),
            ),
        ),
        Livrinho(
            "dragao", "O dragão do castelo", "🐉", 110, "🐉",
            listOf(
                p("No castelo mora o dragão.", N, "🏰", "🐉"),
                p("O dragão sopra fogo.", N, "🐉", "🔥"),
                p("O dragão acende a fogueira.", N, "🐉", "🔥", "🪵"),
                p("A turma esquenta as mãos.", N, "🐛", "🐿️", "🤖"),
                p("O dragão gosta de abraço.", N, "🐉", "🤗"),
                p("Obrigado, dragão!", N, "🐉", "❤️"),
            ),
            listOf(q("O que o dragão acende?", f("🔥", "a fogueira"), f("💡", "a lâmpada"), f("🕯️", "a vela"))),
        ),

        // ------------------------------------------------ X, CH e H (fase 121)
        Livrinho(
            "hora-dormir", "A hora de dormir", "🌙", 120, "🛏️",
            listOf(
                p("Chegou a hora de dormir.", N, "⏰", "🌙"),
                p("Lia escova os dentes.", C, "🐛", "🪥"),
                p("Lia veste o pijama.", C, "🐛", "👚"),
                p("Mamãe canta baixinho.", N, "👩", "🎵"),
                p("Lia fecha os olhos.", N, "🐛", "😴"),
                p("Boa noite, Lia!", N, "🌙", "⭐"),
            ),
            listOf(q("O que Lia escova?", f("🦷", "os dentes"), f("👟", "o sapato"), f("🐶", "o cachorro"))),
        ),
        Livrinho(
            "hotel", "O hotel dos bichos", "🏨", 120, "🔑",
            listOf(
                p("O hotel dos bichos abre cedo.", C, "🏨", "🌅"),
                p("O urso pede a cama grande.", C, "🐻", "🛏️"),
                p("A gata pede leite.", C, "🐱", "🥛"),
                p("O peixe pede a banheira.", C, "🐟", "🛁"),
                p("A coruja toma conta do hotel.", C, "🦉", "🏨"),
                p("Boa noite, bichos!", N, "🐻", "🐱", "🐟"),
            ),
            listOf(q("O que a gata pede?", f("🥛", "leite"), f("🥕", "cenoura"), f("🍉", "melancia"))),
        ),

        // ------------------------------------------------ M e N no fim (fase 131)
        Livrinho(
            "trem", "O trem da turma", "🚂", 130, "🚂",
            listOf(
                p("O trem apita: piu, piu!", J, "🚂", "🎵"),
                p("Tito, Lia e Pipo entram no trem.", J, "🐿️", "🐛", "🤖", "🚂"),
                p("O trem passa pela ponte.", L, "🚂", "🌉"),
                p("Lia olha a montanha.", M, "🐛", "⛰️"),
                p("Tito come um bolinho.", J, "🐿️", "🧁"),
                p("O trem para na cidade.", J, "🚂", "🏙️"),
                p("Que passeio bom!", J, "😄", "🚂"),
            ),
            listOf(
                q("O que Lia olha?", f("⛰️", "a montanha"), f("🌊", "o mar"), f("🎪", "o circo")),
                q("O que Tito come?", f("🧁", "um bolinho"), f("🍕", "uma pizza"), f("🍉", "uma melancia")),
            ),
        ),
        Livrinho(
            "jardim", "O jardim", "🌷", 130, "🐝",
            listOf(
                p("No jardim tem muita flor.", J, "🌷", "🌼", "🌸"),
                p("A abelha voa de flor em flor.", J, "🐝", "🌼"),
                p("A borboleta pousa na folha.", J, "🦋", "🍃"),
                p("Um caracol anda bem devagar.", J, "🐌", "🌱"),
                p("Lia rega o jardim com o balde.", J, "🐛", "🪣", "💧"),
                p("O jardim fica lindo.", J, "🌷", "🌈"),
            ),
            listOf(q("Quem voa de flor em flor?", f("🐝", "a abelha"), f("🐌", "o caracol"), f("🐢", "a tartaruga"))),
        ),

        // ------------------------------------------------ acentos (fase 204)
        Livrinho(
            "primeiro-dia", "O primeiro dia de aula", "🏫", 203, "🎒",
            listOf(
                p("Hoje é o primeiro dia de aula.", E, "🏫", "🎒"),
                p("Lia está com medo.", E, "🐛", "😟"),
                p("A coruja diz: olá, Lia!", E, "🦉", "👋", "🐛"),
                p("Tito senta do lado da Lia.", E, "🐿️", "🐛"),
                p("Pipo mostra o lápis novo.", E, "🤖", "✏️"),
                p("Lia já sorri.", E, "🐛", "😊"),
                p("A escola é muito legal!", E, "🏫", "⭐"),
            ),
            listOf(
                q("Quem diz olá para a Lia?", f("🦉", "a coruja"), f("🐶", "o cão"), f("🐱", "o gato")),
                q("Como Lia fica no fim?", f("😊", "feliz"), f("😢", "triste"), f("😴", "com sono")),
            ),
        ),
        Livrinho(
            "vovos", "O vovô e a vovó", "👵", 203, "🧀",
            listOf(
                p("No domingo, Lia visita o vovô e a vovó.", C, "👴", "👵", "🏠"),
                p("A vovó faz pão de queijo.", C, "👵", "🧀"),
                p("O vovô conta piada.", C, "👴", "😂"),
                p("Lia ri até chorar.", C, "🐛", "😂"),
                p("Na hora de ir, tem abraço apertado.", C, "🤗", "❤️"),
                p("Até domingo!", C, "👋", "🏠"),
            ),
            listOf(
                q("O que a vovó faz?", f("🧀", "pão de queijo"), f("🍕", "pizza"), f("🍦", "sorvete")),
                q("O que o vovô conta?", f("😂", "piada"), f("🔢", "números"), f("⭐", "estrelas")),
            ),
        ),
    )

    val porId: Map<String, Livrinho> = todos.associateBy { it.id }

    /**
     * A fase de Letras mais adiantada que a criança já alcançou: a maior vencida ou, depois do "Descobrir o nível",
     * a última antes do mundo sugerido ([EstadoJogo.liberadaAte]). -1 se ainda não venceu nenhuma.
     */
    fun faseAlcancada(estado: EstadoJogo): Int {
        val vencida = estado.estrelas.keys.filter { it.startsWith(Trilha.PORTUGUES.name + ":") }
            .mapNotNull { it.substringAfter(":").toIntOrNull() }.maxOrNull() ?: -1
        val pelaDescoberta = (estado.liberadaAte[Trilha.PORTUGUES] ?: 0) - 1
        return maxOf(vencida, pelaDescoberta)
    }

    fun liberado(livro: Livrinho, estado: EstadoJogo): Boolean = faseAlcancada(estado) >= livro.fase

    /** Quantas fases de Letras faltam para abrir (0 se já abriu). */
    fun faltam(livro: Livrinho, estado: EstadoJogo): Int = (livro.fase - faseAlcancada(estado)).coerceAtLeast(0)

    /** O primeiro livrinho aberto que a criança ainda não leu (para a estante sugerir). */
    fun sugerido(estado: EstadoJogo): Livrinho? = todos.firstOrNull { liberado(it, estado) && it.id !in estado.livrinhosLidos }

    fun lidosParaTexto(ids: Set<String>): String = ids.sorted().joinToString(",")

    /** Saves antigos não têm a chave; ids que não existem mais somem. */
    fun textoParaLidos(s: String?): Set<String> = s.orEmpty().split(",").map { it.trim() }.filter { it in porId }.toSet()
}

/** Terminou o livrinho: o marcador vai para a estante (uma vez só; ler de novo não muda nada). */
fun EstadoJogo.terminarLivrinho(id: String): EstadoJogo =
    if (id !in Livrinhos.porId || id in livrinhosLidos) this else copy(livrinhosLidos = livrinhosLidos + id)
