package br.com.casinhadosaber.dominio

/** Onde fica uma área nova da casa: no jardim (aparece ao lado da casa) ou dentro (um cômodo novo). */
enum class TipoDeArea { FORA, DENTRO }

/**
 * Faixa de uma área onde cada tipo de coisa fica de início: [ALTO] é o céu ou a parede, [CENTRO] é o
 * miolo da área (a água da piscina, o tapete, o aquário), [CHAO] é o resto do chão e [BICHO] é um
 * bichinho de chão que passeia sozinho (ver [Passeio]).
 */
enum class Zona { ALTO, CENTRO, CHAO, BICHO }

/** Um tipo de coisa de uma área (a boia da piscina, o ossinho da área do pet). */
data class TipoDeCoisa(
    val id: String,
    val nome: String,
    /** "o", "a", "os" ou "as". */
    val artigo: String,
    val nomeEn: String,
    val emoji: String,
    val zona: Zona,
)

/**
 * Uma área que a casa ganha depois das 40 peças da casa de sempre: piscina, área do pet, quarto novo...
 *
 * A área chega pela [obra] (a criança toca no 🔨 do baú para construir) e depois cada fase nova vencida
 * dá uma coisa dela, até completar [tamanho] prêmios (a obra e as coisas). As coisas giram pelos [tipos]:
 * depois do último tipo, vem outro do primeiro ("outra boia").
 */
data class AreaDaCasa(
    val id: String,
    val nome: String,
    val comArtigo: String,
    val nomeEn: String,
    val icone: String,
    val tipo: TipoDeArea,
    /** Quantos prêmios a área tem, contando a obra. */
    val tamanho: Int,
    val tipos: List<TipoDeCoisa>,
) {
    /** Fases vencidas antes da obra: a obra é o prêmio da fase número [limiar] + 1. Calculado em [CasaQueCresce]. */
    val limiar: Int get() = CasaQueCresce.limiarDe(id)

    val obra: ItemCasa get() = CasaQueCresce.obraDe(id)
    val coisas: List<ItemCasa> get() = CasaQueCresce.coisasDe(id)
}

/**
 * A casa que cresce: depois das 40 peças, a cada 40 a 50 fases a casa ganha uma área nova, até a fase 1000.
 * Tudo é calculado só pelo número de fases vencidas (determinístico): um save antigo com 60 fases já ganha,
 * na hora, a obra do quarto novo e as 19 coisas que vieram depois dela. As posições guardadas em
 * [EstadoJogo.colocados] continuam valendo (as peças antigas não mudam de id).
 */
object CasaQueCresce {

    private fun tipos(vararg linhas: String): List<TipoDeCoisa> = linhas.map { l ->
        val p = l.split("|")
        require(p.size == 6) { l }
        val zona = when (p[5]) {
            "P" -> Zona.ALTO
            "A" -> Zona.CENTRO
            "B" -> Zona.BICHO
            else -> Zona.CHAO
        }
        TipoDeCoisa(p[0], p[1], p[2], p[3], p[4], zona)
    }

    /** As áreas, na ordem em que chegam. Os tamanhos somam 960: com as 40 peças, uma coisa nova por fase até a 1000. */
    val areas: List<AreaDaCasa> = listOf(
        AreaDaCasa(
            "quarto2", "Quarto novo", "o quarto novo", "new bedroom", "🛏️", TipoDeArea.DENTRO, 40,
            tipos(
                "cama|Cama|a|bed|🛏️|C", "ursinho|Ursinho|o|teddy bear|🧸|C", "boneca|Boneca|a|doll|🪆|C",
                "robo|Robô|o|robot|🤖|C", "dino|Dinossauro|o|dinosaur|🦕|C", "unicornio|Unicórnio|o|unicorn|🦄|C",
                "foguete|Foguete|o|rocket|🚀|C", "quadro|Quadro|o|picture|🖼️|P", "relogio|Relógio|o|clock|⏰|P",
                "estrela|Estrela que brilha|a|shining star|🌟|P", "lua|Lua|a|moon|🌙|P", "mochila|Mochila|a|backpack|🎒|C",
                "livro|Livro de história|o|storybook|📖|C", "lanterna|Lanterninha|a|lantern|🏮|P",
                "planta|Plantinha|a|plant|🪴|C", "presente|Presente|o|present|🎁|C", "coracao|Almofada de coração|a|heart pillow|💖|A",
                "sapatilha|Sapatilha|a|slipper|🥿|A",
            ),
        ),
        AreaDaCasa(
            "pet", "Área do pet", "a área do pet", "pet area", "🐾", TipoDeArea.FORA, 45,
            tipos(
                "cachorro|Cachorrinho|o|puppy|🐕|B", "gato|Gatinho|o|kitten|🐈|B", "coelho|Coelhinho|o|bunny|🐇|B",
                "tartaruga|Tartaruga|a|turtle|🐢|B", "poodle|Poodle|o|poodle|🐩|B", "papagaio|Papagaio|o|parrot|🦜|P",
                "passarinho|Passarinho|o|little bird|🐦|P", "osso|Ossinho|o|bone|🦴|C", "pote|Potinho de ração|o|food bowl|🥣|A",
                "bolinha|Bolinha|a|ball|🎾|C", "novelo|Novelo de lã|o|ball of yarn|🧶|A", "cesta|Caminha do gato|a|cat bed|🧺|A",
                "patinhas|Pegadinhas|as|paw prints|🐾|C", "bebedouro|Bebedouro|o|water bowl|💧|A", "sabonete|Sabonete do banho|o|soap|🧼|C",
                "disco|Disco|o|flying disc|🥏|C", "ratinho|Ratinho de brinquedo|o|toy mouse|🐁|B",
            ),
        ),
        AreaDaCasa(
            "piscina", "Piscina", "a piscina", "swimming pool", "🏊", TipoDeArea.FORA, 45,
            tipos(
                "boia|Boia|a|swim ring|🛟|A", "pato|Patinho de borracha|o|rubber duck|🦆|A", "golfinho|Golfinho de boia|o|dolphin float|🐬|A",
                "baleia|Baleia de boia|a|whale float|🐳|A", "bola|Bola de praia|a|beach ball|🏐|C", "guardasol|Guarda-sol|o|beach umbrella|⛱️|C",
                "oculos|Óculos de sol|os|sunglasses|🕶️|C", "mascara|Máscara de mergulho|a|diving mask|🤿|A", "maio|Maiô|o|swimsuit|🩱|C",
                "sunga|Sunga|a|swim trunks|🩳|C", "sorvete|Sorvete|o|ice cream|🍦|C", "melancia|Melancia|a|watermelon|🍉|C",
                "suco|Suco|o|juice|🧃|C", "coqueiro|Coqueiro|o|palm tree|🌴|C", "chinelo|Chinelo|o|flip-flop|🩴|C",
                "protetor|Protetor solar|o|sunscreen|🧴|C", "tubarao|Tubarão de boia|o|shark float|🦈|A", "peixe|Peixinho de boia|o|fish float|🐠|A",
            ),
        ),
        AreaDaCasa(
            "parquinho", "Parquinho", "o parquinho", "playground", "🛝", TipoDeArea.FORA, 45,
            tipos(
                "escorregador|Escorregador|o|slide|🛝|C", "pipa|Pipa|a|kite|🪁|P", "balao|Balão|o|balloon|🎈|P",
                "bola|Bola|a|ball|⚽|C", "carrinho|Carrinho|o|toy car|🚗|C", "patinete|Patinete|o|scooter|🛴|C",
                "skate|Skate|o|skateboard|🛹|C", "baldinho|Baldinho|o|bucket|🪣|A", "castelo|Castelo de areia|o|sandcastle|🏰|A",
                "ioio|Ioiô|o|yo-yo|🪀|C", "bolhas|Bolhas de sabão|as|bubbles|🫧|P", "trem|Trenzinho|o|toy train|🚂|C",
                "carrossel|Carrossel|o|carousel|🎠|C", "rodagigante|Roda-gigante|a|Ferris wheel|🎡|C",
                "basquete|Bola de basquete|a|basketball|🏀|C", "bicicleta|Bicicleta|a|bike|🚲|C", "pirulito|Pirulito|o|lollipop|🍭|C",
            ),
        ),
        AreaDaCasa(
            "horta", "Horta e pomar", "a horta", "vegetable garden", "🥕", TipoDeArea.FORA, 45,
            tipos(
                "cenoura|Cenoura|a|carrot|🥕|A", "alface|Alface|a|lettuce|🥬|A", "tomate|Tomate|o|tomato|🍅|A",
                "milho|Milho|o|corn|🌽|A", "abobora|Abóbora|a|pumpkin|🎃|C", "morango|Morango|o|strawberry|🍓|A",
                "maca|Maçã|a|apple|🍎|C", "laranja|Laranja|a|orange|🍊|C", "banana|Banana|a|banana|🍌|C",
                "uva|Uva|a|grapes|🍇|C", "pera|Pera|a|pear|🍐|C", "brocolis|Brócolis|o|broccoli|🥦|A",
                "batata|Batata|a|potato|🥔|A", "pimentao|Pimentão|o|bell pepper|🫑|A", "girassol|Girassol|o|sunflower|🌻|C",
                "abelha|Abelhinha|a|bee|🐝|P", "joaninha|Joaninha|a|ladybug|🐞|C", "cesta|Cesta|a|basket|🧺|C",
            ),
        ),
        AreaDaCasa(
            "brinquedos", "Sala de brinquedos", "a sala de brinquedos", "playroom", "🧸", TipoDeArea.DENTRO, 45,
            tipos(
                "blocos|Blocos de montar|os|blocks|🧱|A", "quebracabeca|Quebra-cabeça|o|puzzle|🧩|A", "dado|Dado|o|die|🎲|A",
                "trem|Trenzinho|o|toy train|🚂|C", "aviao|Aviãozinho|o|airplane|✈️|P", "barquinho|Barquinho|o|boat|⛵|C",
                "tambor|Tambor|o|drum|🥁|C", "boneca|Boneca|a|doll|🪆|C", "robo|Robô|o|robot|🤖|C",
                "carro|Carro de corrida|o|race car|🏎️|A", "ursinho|Ursinho|o|teddy bear|🧸|C", "coroa|Coroa|a|crown|👑|C",
                "varinha|Varinha mágica|a|magic wand|🪄|C", "balao|Balão|o|balloon|🎈|P", "videogame|Videogame|o|video game|🎮|A",
                "ima|Ímã|o|magnet|🧲|C", "pinguim|Pinguim de pelúcia|o|toy penguin|🐧|C", "cartola|Cartola de mágico|a|magic hat|🎩|C",
            ),
        ),
        AreaDaCasa(
            "arvore", "Casa na árvore", "a casa na árvore", "tree house", "🌳", TipoDeArea.FORA, 45,
            tipos(
                "coruja|Corujinha|a|owl|🦉|P", "esquilo|Esquilo|o|squirrel|🐿️|B", "passarinho|Passarinho|o|bird|🐦|P",
                "ninho|Ninho|o|nest|🪺|P", "luneta|Luneta|a|telescope|🔭|C", "lanterna|Lanterna|a|flashlight|🔦|C",
                "mapa|Mapa do tesouro|o|treasure map|🗺️|C", "bussola|Bússola|a|compass|🧭|C", "bandeira|Bandeira|a|flag|🚩|P",
                "corda|Corda|a|rope|🪢|C", "escada|Escada|a|ladder|🪜|C", "folhas|Folhas|as|leaves|🍃|P",
                "castanha|Castanha|a|chestnut|🌰|C", "cogumelo|Cogumelo|o|mushroom|🍄|C", "lagarta|Lagarta|a|caterpillar|🐛|C",
                "borboleta|Borboleta|a|butterfly|🦋|P", "ourico|Ouriço|o|hedgehog|🦔|B", "barraca|Barraca|a|tent|⛺|A",
            ),
        ),
        AreaDaCasa(
            "flores", "Jardim de flores", "o jardim de flores", "flower garden", "🌷", TipoDeArea.FORA, 50,
            tipos(
                "tulipa|Tulipa|a|tulip|🌷|C", "rosa|Rosa|a|rose|🌹|C", "girassol|Girassol|o|sunflower|🌻|C",
                "margarida|Margarida|a|daisy|🌼|C", "cerejeira|Flor de cerejeira|a|cherry blossom|🌸|C", "hibisco|Hibisco|o|hibiscus|🌺|C",
                "buque|Buquê|o|bouquet|💐|C", "fonte|Fonte|a|fountain|⛲|A", "abelha|Abelhinha|a|bee|🐝|P",
                "borboleta|Borboleta|a|butterfly|🦋|P", "joaninha|Joaninha|a|ladybug|🐞|C", "caracol|Caracol|o|snail|🐌|B",
                "sapinho|Sapinho|o|frog|🐸|C", "trevo|Trevo|o|clover|🍀|C", "cacto|Cacto|o|cactus|🌵|C",
                "vaso|Vaso de planta|o|potted plant|🪴|C", "cisne|Cisne|o|swan|🦢|A", "lotus|Flor de lótus|a|lotus|🪷|A",
            ),
        ),
        AreaDaCasa(
            "estudio", "Estúdio de música e arte", "o estúdio de música e arte", "music and art studio", "🎨", TipoDeArea.DENTRO, 50,
            tipos(
                "teclado|Teclado|o|keyboard|🎹|A", "guitarra|Guitarra|a|guitar|🎸|A", "bateria|Bateria|a|drums|🥁|A",
                "trompete|Trompete|o|trumpet|🎺|C", "violino|Violino|o|violin|🎻|C", "saxofone|Saxofone|o|saxophone|🎷|C",
                "banjo|Banjo|o|banjo|🪕|C", "atabaque|Atabaque|o|long drum|🪘|C", "microfone|Microfone|o|microphone|🎤|A",
                "fone|Fone de ouvido|o|headphones|🎧|C", "paleta|Paleta de tintas|a|paint palette|🎨|C", "pincel|Pincel|o|paintbrush|🖌️|C",
                "giz|Giz de cera|o|crayon|🖍️|C", "lapis|Lápis|o|pencil|✏️|C", "quadro|Quadro|o|painting|🖼️|P",
                "mascaras|Máscaras de teatro|as|theater masks|🎭|P", "nota|Nota musical|a|musical note|🎵|P", "radio|Rádio|o|radio|📻|C",
                "claquete|Claquete|a|clapperboard|🎬|C",
            ),
        ),
        AreaDaCasa(
            "gourmet", "Varanda gourmet", "a varanda gourmet", "outdoor kitchen", "🍢", TipoDeArea.FORA, 50,
            tipos(
                "espetinho|Espetinho|o|skewer|🍢|A", "milho|Milho assado|o|roasted corn|🌽|A", "coxinha|Coxa de frango|a|chicken leg|🍗|A",
                "salada|Salada|a|salad|🥗|A", "pizza|Pizza|a|pizza|🍕|A", "pao|Pão|o|bread|🍞|C",
                "queijo|Queijo|o|cheese|🧀|C", "abacaxi|Abacaxi|o|pineapple|🍍|C", "copo|Copo de suco|o|cup of juice|🥤|C",
                "sorvete|Sorvete|o|ice cream|🍦|C", "bolo|Bolo|o|cake|🍰|C", "cupcake|Bolinho|o|cupcake|🧁|C",
                "pipoca|Pipoca|a|popcorn|🍿|C", "panquecas|Panquecas|as|pancakes|🥞|C", "frigideira|Frigideira|a|frying pan|🍳|C",
                "bule|Bule|o|teapot|🫖|C", "lanterna|Lanterninha|a|lantern|🏮|P", "cadeira|Cadeira|a|chair|🪑|C",
            ),
        ),
        AreaDaCasa(
            "biblioteca", "Biblioteca", "a biblioteca", "library", "📚", TipoDeArea.DENTRO, 50,
            tipos(
                "livros|Pilha de livros|a|pile of books|📚|C", "aberto|Livro aberto|o|open book|📖|A", "vermelho|Livro vermelho|o|red book|📕|C",
                "verde|Livro verde|o|green book|📗|C", "azul|Livro azul|o|blue book|📘|C", "laranja|Livro laranja|o|orange book|📙|C",
                "globo|Globo|o|globe|🌍|C", "mapa|Mapa|o|map|🗺️|P", "lupa|Lupa|a|magnifying glass|🔍|A",
                "pena|Pena|a|feather|🪶|C", "pergaminho|Pergaminho|o|scroll|📜|A", "vela|Vela|a|candle|🕯️|P",
                "poltrona|Poltrona|a|armchair|🛋️|C", "coruja|Coruja sábia|a|wise owl|🦉|P", "caderno|Caderno|o|notebook|📝|A",
                "lampada|Lâmpada|a|lamp|💡|P", "oculos|Óculos de leitura|os|reading glasses|👓|C", "lapis|Lápis|o|pencil|✏️|C",
            ),
        ),
        AreaDaCasa(
            "observatorio", "Observatório no telhado", "o observatório", "rooftop observatory", "🔭", TipoDeArea.FORA, 50,
            tipos(
                "telescopio|Telescópio|o|telescope|🔭|A", "lua|Lua|a|moon|🌙|P", "estrela|Estrela|a|star|⭐|P",
                "brilho|Estrela brilhante|a|shining star|🌟|P", "planeta|Planeta|o|planet|🪐|P", "cometa|Cometa|o|comet|☄️|P",
                "foguete|Foguete|o|rocket|🚀|C", "disco|Disco voador|o|flying saucer|🛸|P", "et|Amiguinho do espaço|o|space friend|👽|C",
                "satelite|Satélite|o|satellite|🛰️|P", "cadente|Estrela cadente|a|shooting star|🌠|P", "terra|Planeta Terra|o|planet Earth|🌍|P",
                "luacheia|Lua cheia|a|full moon|🌕|P", "bussola|Bússola|a|compass|🧭|C", "mapa|Mapa das estrelas|o|star map|🗺️|C",
                "lanterna|Lanterna|a|flashlight|🔦|C",
            ),
        ),
        AreaDaCasa(
            "fazendinha", "Fazendinha", "a fazendinha", "little farm", "🐄", TipoDeArea.FORA, 50,
            tipos(
                "vaca|Vaquinha|a|cow|🐄|B", "porco|Porquinho|o|pig|🐖|B", "ovelha|Ovelha|a|sheep|🐑|B",
                "cabra|Cabrinha|a|goat|🐐|B", "galinha|Galinha|a|hen|🐔|B", "pintinho|Pintinho|o|chick|🐤|B",
                "galo|Galo|o|rooster|🐓|B", "cavalo|Cavalo|o|horse|🐎|B", "pato|Pato|o|duck|🦆|A",
                "coelho|Coelho|o|rabbit|🐇|B", "ovo|Ovo|o|egg|🥚|A", "trigo|Trigo|o|wheat|🌾|C",
                "trator|Trator|o|tractor|🚜|C", "cesto|Cesto|o|basket|🧺|C", "leite|Leite|o|milk|🥛|C",
                "abelha|Abelhinha|a|bee|🐝|P", "girassol|Girassol|o|sunflower|🌻|C",
            ),
        ),
        AreaDaCasa(
            "quadra", "Quadra de esportes", "a quadra de esportes", "sports court", "⚽", TipoDeArea.FORA, 50,
            tipos(
                "futebol|Bola de futebol|a|soccer ball|⚽|A", "basquete|Bola de basquete|a|basketball|🏀|A", "rugbi|Bola de rúgbi|a|rugby ball|🏈|C",
                "beisebol|Bola de beisebol|a|baseball|⚾|C", "tenis|Bola de tênis|a|tennis ball|🎾|C", "volei|Bola de vôlei|a|volleyball|🏐|A",
                "pingue|Pingue-pongue|o|ping-pong|🏓|C", "peteca|Peteca|a|shuttlecock|🏸|C", "gol|Gol|o|goal|🥅|C",
                "trofeu|Troféu|o|trophy|🏆|C", "medalha|Medalha|a|medal|🥇|C", "alvo|Alvo|o|target|🎯|P",
                "patins|Patins|os|roller skates|🛼|C", "disco|Disco|o|flying disc|🥏|A", "hoquei|Taco de hóquei|o|hockey stick|🏒|C",
                "bandeira|Bandeira|a|flag|🚩|P", "cronometro|Cronômetro|o|stopwatch|⏱️|P", "tenis2|Tênis de correr|o|sneaker|👟|C",
            ),
        ),
        AreaDaCasa(
            "aquario", "Sala do aquário", "a sala do aquário", "aquarium room", "🐠", TipoDeArea.DENTRO, 50,
            tipos(
                "peixe|Peixinho colorido|o|tropical fish|🐠|A", "peixe2|Peixinho azul|o|blue fish|🐟|A", "baiacu|Baiacu|o|pufferfish|🐡|A",
                "polvo|Polvo|o|octopus|🐙|A", "caranguejo|Caranguejo|o|crab|🦀|C", "concha|Concha|a|shell|🐚|C",
                "coral|Coral|o|coral|🪸|A", "lula|Lula|a|squid|🦑|A", "tartaruga|Tartaruga marinha|a|sea turtle|🐢|A",
                "golfinho|Golfinho|o|dolphin|🐬|A", "baleia|Baleia|a|whale|🐳|P", "lagosta|Lagosta|a|lobster|🦞|C",
                "camarao|Camarão|o|shrimp|🦐|C", "bolhas|Bolhas|as|bubbles|🫧|P", "alga|Alga|a|seaweed|🌿|A",
                "foca|Foca|a|seal|🦭|C", "pescaria|Vara de pescar|a|fishing rod|🎣|C",
            ),
        ),
        AreaDaCasa(
            "cinema", "Cinema em casa", "o cinema", "home cinema", "🎬", TipoDeArea.DENTRO, 50,
            tipos(
                "pipoca|Pipoca|a|popcorn|🍿|C", "refri|Copo de suco|o|cup of juice|🥤|C", "claquete|Claquete|a|clapperboard|🎬|C",
                "camera|Câmera|a|camera|🎥|C", "projetor|Projetor|o|projector|📽️|C", "ingresso|Ingresso|o|ticket|🎟️|A",
                "chocolate|Chocolate|o|chocolate|🍫|C", "bala|Bala|a|candy|🍬|C", "sofa|Sofá|o|sofa|🛋️|A",
                "oculos|Óculos de cinema|os|movie glasses|🕶️|C", "filme|Rolo de filme|o|film|🎞️|P", "brilho|Estrelinha|a|little star|🌟|P",
                "heroi|Super-herói|o|superhero|🦸|A", "mago|Mago|o|wizard|🧙|A", "fada|Fada|a|fairy|🧚|A",
                "dragao|Dragão|o|dragon|🐉|C", "tirano|Dinossauro rex|o|T. rex|🦖|C", "princesa|Princesa|a|princess|👸|C",
                "principe|Príncipe|o|prince|🤴|C",
            ),
        ),
        AreaDaCasa(
            "lago", "Lago dos peixes", "o lago dos peixes", "fish pond", "🎣", TipoDeArea.FORA, 50,
            tipos(
                "peixe|Peixinho|o|fish|🐟|A", "peixe2|Peixinho colorido|o|tropical fish|🐠|A", "pato|Pato|o|duck|🦆|A",
                "cisne|Cisne|o|swan|🦢|A", "sapo|Sapinho|o|frog|🐸|C", "lotus|Flor de lótus|a|lotus|🪷|A",
                "vara|Vara de pescar|a|fishing rod|🎣|C", "canoa|Canoa|a|canoe|🛶|A", "veleiro|Veleiro|o|sailboat|⛵|A",
                "tartaruga|Tartaruga|a|turtle|🐢|B", "castor|Castor|o|beaver|🦫|B", "flamingo|Flamingo|o|flamingo|🦩|B",
                "junco|Junco|o|reed|🌾|C", "folhas|Folhas|as|leaves|🍃|P", "lontra|Lontra|a|otter|🦦|A",
                "pedra|Pedra|a|rock|🪨|C", "caracol|Caracol|o|snail|🐌|B",
            ),
        ),
        AreaDaCasa(
            "laboratorio", "Laboratório de ciências", "o laboratório", "science lab", "🔬", TipoDeArea.DENTRO, 50,
            tipos(
                "microscopio|Microscópio|o|microscope|🔬|A", "tubo|Tubo de ensaio|o|test tube|🧪|A", "frasco|Frasco|o|flask|⚗️|A",
                "placa|Placa de cultura|a|petri dish|🧫|C", "dna|Fita de DNA|a|DNA|🧬|P", "ima|Ímã|o|magnet|🧲|C",
                "luneta|Luneta|a|telescope|🔭|C", "lampada|Lâmpada|a|light bulb|💡|P", "pilha|Pilha|a|battery|🔋|C",
                "engrenagem|Engrenagem|a|gear|⚙️|P", "vulcao|Vulcão de mentirinha|o|toy volcano|🌋|C", "planeta|Planeta|o|planet|🪐|P",
                "termometro|Termômetro|o|thermometer|🌡️|C", "robo|Robô|o|robot|🤖|A", "satelite|Satélite|o|satellite|🛰️|P",
                "cristal|Cristal|o|crystal|💎|C", "fossil|Fóssil|o|fossil|🦴|C", "esquadro|Esquadro|o|set square|📐|C",
                "abaco|Ábaco|o|abacus|🧮|C",
            ),
        ),
        AreaDaCasa(
            "estufa", "Estufa de borboletas", "a estufa de borboletas", "butterfly house", "🦋", TipoDeArea.FORA, 50,
            tipos(
                "borboleta|Borboleta|a|butterfly|🦋|P", "lagarta|Lagarta|a|caterpillar|🐛|C", "abelha|Abelhinha|a|bee|🐝|P",
                "joaninha|Joaninha|a|ladybug|🐞|C", "hibisco|Hibisco|o|hibiscus|🌺|A", "cerejeira|Flor de cerejeira|a|cherry blossom|🌸|A",
                "erva|Erva|a|herb|🌿|C", "folhas|Folhas|as|leaves|🍃|P", "palmeira|Palmeira|a|palm tree|🌴|C",
                "cacto|Cacto|o|cactus|🌵|C", "cogumelo|Cogumelo|o|mushroom|🍄|C", "grilo|Grilo|o|cricket|🦗|C",
                "caracol|Caracol|o|snail|🐌|B", "lagartixa|Lagartixa|a|lizard|🦎|B", "sapo|Sapinho|o|frog|🐸|C",
                "arcoiris|Arco-íris|o|rainbow|🌈|P", "ninho|Ninho|o|nest|🪺|P", "tulipa|Tulipa|a|tulip|🌷|A",
            ),
        ),
        AreaDaCasa(
            "festa", "Salão de festas", "o salão de festas", "party room", "🎉", TipoDeArea.DENTRO, 50,
            tipos(
                "balao|Balão|o|balloon|🎈|P", "confete|Confete|o|confetti|🎉|P", "bola|Bola de confete|a|confetti ball|🎊|P",
                "bolo|Bolo de aniversário|o|birthday cake|🎂|A", "cupcake|Bolinho|o|cupcake|🧁|C", "pirulito|Pirulito|o|lollipop|🍭|C",
                "presente|Presente|o|present|🎁|A", "pinata|Piñata|a|piñata|🪅|P", "laco|Laço|o|bow|🎀|P",
                "coroa|Coroa|a|crown|👑|C", "globo|Globo de luz|o|disco ball|🪩|P", "musica|Música|a|music|🎶|P",
                "torta|Fatia de bolo|a|slice of cake|🍰|A", "bala|Bala|a|candy|🍬|C", "suco|Copo de suco|o|cup of juice|🥤|C",
                "microfone|Microfone|o|microphone|🎤|C", "estrelinhas|Estrelinhas|as|sparkler|🎇|P", "chapeu|Chapéu de festa|o|party hat|🥳|C",
            ),
        ),
    )

    /** Onde as 40 peças da casa de sempre terminam (a primeira área chega na fase seguinte). */
    val INICIO: Int get() = ItensDaCasa.ordem.size

    private val limiares: Map<String, Int> by lazy {
        var n = INICIO
        areas.associate { a -> (a.id to n).also { n += a.tamanho } }
    }

    fun limiarDe(id: String): Int = limiares.getValue(id)

    val porId: Map<String, AreaDaCasa> by lazy { areas.associateBy { it.id } }

    /** Item da obra (a criança toca para construir). */
    private val obras: Map<String, ItemCasa> by lazy {
        areas.associate { a ->
            a.id to ItemCasa("${a.id}.obra", a.nome, a.comArtigo, a.nomeEn, a.icone, 0.5f, 0.5f, 0.2f, area = a.id, obra = true)
        }
    }

    fun obraDe(id: String): ItemCasa = obras.getValue(id)

    private val coisas: Map<String, List<ItemCasa>> by lazy { areas.associate { it.id to montarCoisas(it) } }

    fun coisasDe(id: String): List<ItemCasa> = coisas.getValue(id)

    /** Tamanho (fração da largura da área) de cada zona. */
    fun tamanhoDa(z: Zona): Float = when (z) {
        Zona.ALTO -> 0.075f
        Zona.CENTRO -> 0.08f
        Zona.CHAO, Zona.BICHO -> 0.085f
    }

    /**
     * O miolo da área (água, tapete, canteiro), em frações da largura (x) e da altura (y): os objetos de
     * [Zona.CENTRO] começam aqui; os de chão, em volta.
     */
    const val MIOLO_ESQ = 0.22f
    const val MIOLO_DIR = 0.78f
    const val MIOLO_TOPO = 0.50f
    const val MIOLO_BASE = 0.80f

    /** Linha do horizonte (ou do rodapé, nos cômodos): acima dela fica o céu ou a parede. */
    const val HORIZONTE = 0.34f

    /** Lugares de partida, por zona, em ordem de preferência. */
    private val lugaresAlto: List<Posicao> = (0 until 2).flatMap { linha ->
        (0 until 6).map { i -> Posicao(0.09f + i * 0.165f + linha * 0.08f, 0.15f + linha * 0.12f) }
    }.filter { it.x <= 0.93f }

    /** Linhas do chão (o pé dos objetos), de 0,07 em 0,07: um pouco mais que a altura de um objeto. */
    private val linhas = listOf(0.42f, 0.49f, 0.56f, 0.63f, 0.70f, 0.77f, 0.84f, 0.91f, 0.98f)

    private fun noMiolo(y: Float) = y > MIOLO_TOPO + 0.02f && y < MIOLO_BASE

    private val lugaresCentro: List<Posicao> = linhas.filter(::noMiolo).flatMap { y ->
        listOf(0.31f, 0.44f, 0.57f, 0.70f).map { x -> Posicao(x, y) }
    }

    private val lugaresChao: List<Posicao> = linhas.flatMapIndexed { linha, y ->
        (0 until 7).map { i -> Posicao(0.075f + i * 0.14f + if (linha % 2 == 1) 0.065f else 0f, y) }
            .filter { it.x <= 0.94f }
            // Em volta do miolo: só os lugares que não encostam nele.
            .filter { p -> !(noMiolo(y) && p.x > MIOLO_ESQ - 0.07f && p.x < MIOLO_DIR + 0.07f) }
            // O canto de baixo à esquerda é do mascote (ele fica por cima e pegaria o toque).
            .filter { p -> !(p.x < 0.25f && y > 0.86f) }
    }

    /**
     * Os lugares numa ordem que espalha: pula de [passo] em [passo] pela lista, então as primeiras coisas
     * ganhas já ficam por toda a área (e não todas na primeira fileira).
     */
    private fun espalhar(lugares: List<Posicao>): List<Posicao> {
        val n = lugares.size
        if (n < 3) return lugares
        var passo = maxOf(2, (n * 0.38f).toInt())
        while (mdc(passo, n) != 1) passo++
        return List(n) { i -> lugares[(i * passo) % n] }
    }

    private tailrec fun mdc(a: Int, b: Int): Int = if (b == 0) a else mdc(b, a % b)

    private fun montarCoisas(a: AreaDaCasa): List<ItemCasa> {
        val livres = mutableMapOf(
            Zona.ALTO to espalhar(lugaresAlto).toMutableList(),
            Zona.CENTRO to espalhar(lugaresCentro).toMutableList(),
            Zona.CHAO to espalhar(lugaresChao).toMutableList(),
        )
        val ordemDeTroca = listOf(Zona.CHAO, Zona.ALTO, Zona.CENTRO)
        val quantas = a.tamanho - 1
        return (0 until quantas).map { j ->
            val t = a.tipos[j % a.tipos.size]
            val copia = j / a.tipos.size + 1
            val zona = if (t.zona == Zona.BICHO) Zona.CHAO else t.zona
            val lista = livres.getValue(zona).takeIf { it.isNotEmpty() } ?: ordemDeTroca.map { livres.getValue(it) }.firstOrNull { it.isNotEmpty() }
            val pos = lista?.removeAt(0) ?: Posicao(0.1f + (j % 9) * 0.1f, 0.9f)
            val id = if (copia == 1) "${a.id}.${t.id}" else "${a.id}.${t.id}.$copia"
            val comArtigo = if (copia == 1) "${t.artigo} ${t.nome.lowercase()}" else "${outro(t.artigo)} ${t.nome.lowercase()}"
            ItemCasa(id, t.nome, comArtigo, t.nomeEn, t.emoji, pos.x, pos.y, tamanhoDa(t.zona), area = a.id)
        }
    }

    /** "outro patinho", "outra boia", "outros óculos", "outras bolhas". */
    private fun outro(artigo: String) = when (artigo) {
        "a" -> "outra"
        "os" -> "outros"
        "as" -> "outras"
        else -> "outro"
    }

    /** Todos os prêmios das áreas, na ordem: a obra e depois as coisas de cada área. */
    val premios: List<ItemCasa> by lazy { areas.flatMap { listOf(it.obra) + it.coisas } }

    /** Tipo da coisa (o id sem a área e sem o número da cópia), ou o próprio id para as peças de sempre. */
    fun tipoDe(id: String): String = if ('.' in id) id.split('.')[1] else id

    /** A área de um item (null para as 40 peças de sempre). */
    fun areaDe(id: String): AreaDaCasa? = if ('.' in id) porId[id.substringBefore('.')] else null

    /** Bichinhos de chão que passeiam sozinhos (o cachorrinho e o gatinho da casa e os bichos das áreas). */
    fun passeia(id: String): Boolean {
        if (id == "cachorro" || id == "gato") return true
        val area = areaDe(id) ?: return false
        return area.tipos.firstOrNull { it.id == tipoDe(id) }?.zona == Zona.BICHO
    }

    /** Áreas já ganhas (a obra já veio), na ordem. */
    fun ganhas(fasesVencidas: Int): List<AreaDaCasa> = areas.filter { fasesVencidas > it.limiar }

    /** Áreas já construídas (a criança tocou na obra). */
    fun construidas(e: EstadoJogo): List<AreaDaCasa> = areas.filter { it.obra.id in e.colocados }

    /** A próxima área que ainda vai chegar e quantas fases faltam para ela (null depois da última). */
    fun proxima(fasesVencidas: Int): Pair<AreaDaCasa, Int>? =
        areas.firstOrNull { fasesVencidas <= it.limiar }?.let { it to (it.limiar + 1 - fasesVencidas) }

    /** Progresso de uma área: coisas ganhas e coisas colocadas, de [total] (sem contar a obra). */
    data class Progresso(val area: AreaDaCasa, val ganhas: Int, val colocadas: Int, val total: Int) {
        val completa get() = ganhas == total
        /** O que aparece no cartãozinho: "Piscina 3/44". */
        val rotulo get() = "${area.nome} $colocadas/$total"
    }

    fun progresso(e: EstadoJogo, a: AreaDaCasa): Progresso {
        val ganhas = (e.fasesVencidas - a.limiar - 1).coerceIn(0, a.tamanho - 1)
        val colocadas = a.coisas.count { it.id in e.colocados }
        return Progresso(a, ganhas, colocadas, a.tamanho - 1)
    }

    /**
     * Meta de agora para a barrinha da casa: o fim do pedaço em que a criança está (40 na casa de sempre,
     * depois o fim de cada área, até 1000). Assim a barra sempre tem um objetivo perto, nunca para.
     */
    fun metaAtual(fasesVencidas: Int): Int {
        if (fasesVencidas < INICIO) return INICIO
        for (a in areas) if (fasesVencidas < a.limiar + a.tamanho) return a.limiar + a.tamanho
        return ItensDaCasa.total
    }
}
