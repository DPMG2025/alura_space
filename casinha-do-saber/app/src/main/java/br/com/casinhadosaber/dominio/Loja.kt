package br.com.casinhadosaber.dominio

/** Tipos de enfeite que a loja vende. */
enum class Categoria { TELHADO, PORTA, PAREDE, CHAPEU, VISITANTE, CLIMA, COMIDA, ROUPA, ACESSORIO }

/**
 * Um enfeite da loja, pago com moedinhas 🪙 (ganhas a cada acerto de primeira, até em fases repetidas).
 * [valor] é o índice da cor/estilo, o emoji do chapéu/visitante/comida, ou o traje da roupa (ver [Loja.trajes]).
 * [nomeEn] é o nome em inglês (comidas e roupas), falado devagar pelo narrador.
 */
data class Produto(
    val id: String,
    val nome: String,
    val icone: String,
    val precoBase: Int,
    val categoria: Categoria,
    val valor: String,
    val nomeEn: String = "",
)

/** Roupa especial do guarda-roupa. [id] é guardado em [Perfil.traje]. */
data class Traje(val id: String, val nome: String, val nomeEn: String, val icone: String, val preco: Int)

/** Onde o acessório fica no corpo. A criança usa um acessório de cada lugar ao mesmo tempo. */
enum class ParteDoCorpo(val nome: String, val icone: String) {
    ROSTO("No rosto", "👓"),
    PESCOCO("No pescoço", "🧣"),
    COSTAS("Nas costas", "🎒"),
    MAO("Na mão", "🎈"),
    PES("Nos pés", "👟"),
    CABELO("No cabelo", "🎀"),
}

/** Acessório do guarda-roupa. [id] é guardado em [Perfil.acessorios], na [parte] do corpo dele. */
data class Acessorio(val id: String, val parte: ParteDoCorpo, val nome: String, val nomeEn: String, val icone: String, val preco: Int)

/**
 * Gavetas do guarda-roupa e do espelho: a roupa (traje), o chapéu e uma gaveta para cada parte do corpo.
 * Cada gaveta tem um item vestido por vez (ou nenhum).
 */
enum class Gaveta(val nome: String, val icone: String, val categoria: Categoria, val parte: ParteDoCorpo? = null) {
    ROUPAS("Roupas", "👗", Categoria.ROUPA),
    CHAPEUS("Chapéus", "🎩", Categoria.CHAPEU),
    ROSTO(ParteDoCorpo.ROSTO.nome, ParteDoCorpo.ROSTO.icone, Categoria.ACESSORIO, ParteDoCorpo.ROSTO),
    PESCOCO(ParteDoCorpo.PESCOCO.nome, ParteDoCorpo.PESCOCO.icone, Categoria.ACESSORIO, ParteDoCorpo.PESCOCO),
    COSTAS(ParteDoCorpo.COSTAS.nome, ParteDoCorpo.COSTAS.icone, Categoria.ACESSORIO, ParteDoCorpo.COSTAS),
    MAO(ParteDoCorpo.MAO.nome, ParteDoCorpo.MAO.icone, Categoria.ACESSORIO, ParteDoCorpo.MAO),
    PES(ParteDoCorpo.PES.nome, ParteDoCorpo.PES.icone, Categoria.ACESSORIO, ParteDoCorpo.PES),
    CABELO(ParteDoCorpo.CABELO.nome, ParteDoCorpo.CABELO.icone, Categoria.ACESSORIO, ParteDoCorpo.CABELO),
}

object Loja {
    /** Comidas da geladeira: emoji, nome e nome em inglês. */
    val comidas: List<Triple<String, String, String>> = listOf(
        Triple("🍎", "Maçã", "apple"),
        Triple("🍌", "Banana", "banana"),
        Triple("🥕", "Cenoura", "carrot"),
        Triple("🍓", "Morango", "strawberry"),
        Triple("🥛", "Leite", "milk"),
        Triple("🧀", "Queijo", "cheese"),
        Triple("🍉", "Melancia", "watermelon"),
        Triple("🥚", "Ovos", "eggs"),
        Triple("🍦", "Sorvete", "ice cream"),
        Triple("🍕", "Pizza", "pizza"),
    )

    /** Trajes do guarda-roupa (desenhados em código por cima da criança, em Crianca.kt). */
    val trajes: List<Traje> = listOf(
        Traje("listras", "Camiseta listrada", "striped shirt", "👕", 8),
        Traje("pijama", "Pijama de estrelinhas", "pajamas", "🌙", 9),
        Traje("heroi", "Capa de herói", "cape", "🦸", 12),
        Traje("abelha", "Fantasia de abelha", "bee costume", "🐝", 12),
        Traje("fada", "Asinhas de fada", "fairy wings", "🧚", 12),
        Traje("astronauta", "Roupa de astronauta", "space suit", "🚀", 14),
        // Qualquer criança pode vestir qualquer roupa: nenhuma é só de menino ou só de menina.
        Traje("vestido", "Vestido de flores", "dress", "👗", 9),
        Traje("macacao", "Macacão jeans", "overalls", "👖", 9),
        Traje("futebol", "Uniforme de futebol", "soccer uniform", "⚽", 11),
        Traje("bailarina", "Roupa de bailarina", "ballet outfit", "🩰", 11),
        Traje("chuva", "Capa de chuva", "raincoat", "☔", 10),
        Traje("medico", "Jaleco de médico", "doctor", "🩺", 12),
        Traje("chef", "Roupa de chef", "chef", "🍳", 12),
        Traje("pirata", "Roupa de pirata", "pirate", "🏴‍☠️", 13),
        Traje("realeza", "Roupa de príncipe e princesa", "royal outfit", "🏰", 14),
        Traje("dinossauro", "Fantasia de dinossauro", "dinosaur costume", "🦖", 14),
        // Gatinha fofa (desenho original, só no espírito de bichinhos de laço e bigodes).
        Traje("gatinha", "Fantasia de gatinha", "kitty costume", "🐱", 12),
        Traje("lacos", "Vestido de lacinhos", "bow dress", "🎀", 10),
    )

    /** Acessórios do guarda-roupa, desenhados em código na criança (Crianca.kt), um por parte do corpo. */
    val acessorios: List<Acessorio> = listOf(
        Acessorio("oculos", ParteDoCorpo.ROSTO, "Óculos redondos", "glasses", "👓", 6),
        Acessorio("oculosdesol", ParteDoCorpo.ROSTO, "Óculos de sol", "sunglasses", "🕶️", 7),
        Acessorio("mascara", ParteDoCorpo.ROSTO, "Máscara de herói", "mask", "🎭", 8),
        Acessorio("colar", ParteDoCorpo.PESCOCO, "Colar de bolinhas", "necklace", "📿", 6),
        Acessorio("cachecol", ParteDoCorpo.PESCOCO, "Cachecol", "scarf", "🧣", 6),
        Acessorio("gravata", ParteDoCorpo.PESCOCO, "Gravata borboleta", "bow tie", "👔", 6),
        Acessorio("mochila", ParteDoCorpo.COSTAS, "Mochila", "backpack", "🎒", 7),
        Acessorio("asas", ParteDoCorpo.COSTAS, "Asas de borboleta", "butterfly wings", "🦋", 9),
        Acessorio("capa", ParteDoCorpo.COSTAS, "Capa mágica", "magic cape", "✨", 9),
        Acessorio("varinha", ParteDoCorpo.MAO, "Varinha mágica", "magic wand", "🪄", 7),
        Acessorio("balao", ParteDoCorpo.MAO, "Balão", "balloon", "🎈", 5),
        Acessorio("ursinho", ParteDoCorpo.MAO, "Ursinho", "teddy bear", "🧸", 8),
        Acessorio("tenis", ParteDoCorpo.PES, "Tênis colorido", "sneakers", "👟", 6),
        Acessorio("botas", ParteDoCorpo.PES, "Botas", "boots", "👢", 7),
        Acessorio("pantufas", ParteDoCorpo.PES, "Pantufas de coelhinho", "slippers", "🐰", 6),
        Acessorio("laco", ParteDoCorpo.CABELO, "Laço", "bow", "🎀", 5),
        Acessorio("tiara", ParteDoCorpo.CABELO, "Tiara de brilhantes", "tiara", "💎", 8),
        Acessorio("presilha", ParteDoCorpo.CABELO, "Presilha de estrela", "hair clip", "⭐", 5),
        // Gatinha fofa (desenhos originais): orelhinhas, boné e laço grande no cabelo; bigodes e máscara no rosto.
        Acessorio("orelhasgato", ParteDoCorpo.CABELO, "Orelhinhas de gatinho", "kitty ears", "🐱", 7),
        Acessorio("bonegato", ParteDoCorpo.CABELO, "Boné de gatinho", "kitty cap", "🧢", 8),
        Acessorio("lacogrande", ParteDoCorpo.CABELO, "Laço grande rosa", "big pink bow", "🎀", 6),
        Acessorio("bigodes", ParteDoCorpo.ROSTO, "Bigodinhos de gatinho", "kitty whiskers", "😺", 6),
        Acessorio("mascaragata", ParteDoCorpo.ROSTO, "Máscara de gatinha", "kitty mask", "😸", 8),
    )

    val acessorioPorId: Map<String, Acessorio> = acessorios.associateBy { it.id }

    val coresDoTelhado = listOf(0xFFEF476F, 0xFF4D96FF, 0xFF3DBE6B, 0xFF9B6BFF, 0xFFFFA62B, 0xFF8D5A3B)
    val coresDoTelhadoEscuras = listOf(0xFFC7365A, 0xFF2F6FCC, 0xFF2A9452, 0xFF7048CC, 0xFFCC7F1A, 0xFF6B3E22)
    val nomesDasCores = listOf("vermelho", "azul", "verde", "roxo", "laranja", "marrom")
    private val bolinhas = listOf("🔴", "🔵", "🟢", "🟣", "🟠", "🟤")

    val coresDaPorta = listOf(0xFFA0522D, 0xFF4D96FF, 0xFFEF476F, 0xFF3DBE6B, 0xFFFFC93C)
    private val nomesDaPorta = listOf("marrom", "azul", "vermelha", "verde", "amarela")

    /** 0 = lisa (de graça), 1 = listras, 2 = bolinhas, 3 = estrelinhas. */
    val papeis = listOf("Parede lisa", "Parede de listras", "Parede de bolinhas", "Parede de estrelinhas")

    /** 0 = sol (de graça), 1 = chuva, 2 = neve. */
    val climas = listOf("Sol", "Chuva", "Neve")

    val produtos: List<Produto> = buildList {
        coresDoTelhado.indices.drop(1).forEach { add(Produto("telhado$it", "Telhado ${nomesDasCores[it]}", bolinhas[it] + "🏠", 6, Categoria.TELHADO, "$it")) }
        coresDaPorta.indices.drop(1).forEach { add(Produto("porta$it", "Porta ${nomesDaPorta[it]}", bolinhas[listOf(0, 1, 0, 2, 4)[it]] + "🚪", 5, Categoria.PORTA, "$it")) }
        add(Produto("parede1", papeis[1], "〰️", 8, Categoria.PAREDE, "1"))
        add(Produto("parede2", papeis[2], "⚪", 8, Categoria.PAREDE, "2"))
        add(Produto("parede3", papeis[3], "✨", 10, Categoria.PAREDE, "3"))
        add(Produto("chapeu👑", "Coroa", "👑", 10, Categoria.CHAPEU, "👑"))
        add(Produto("chapeu🧢", "Boné", "🧢", 8, Categoria.CHAPEU, "🧢"))
        add(Produto("chapeu🎩", "Cartola", "🎩", 10, Categoria.CHAPEU, "🎩"))
        add(Produto("chapeu🌸", "Flor no cabelo", "🌸", 6, Categoria.CHAPEU, "🌸"))
        listOf("🦔" to "Ouriço", "🐰" to "Coelhinho", "🦊" to "Raposinha", "🐢" to "Tartaruga", "🦜" to "Papagaio", "🐿️" to "Esquilo")
            .forEach { (e, n) -> add(Produto("visitante$e", n, e, 8, Categoria.VISITANTE, e)) }
        add(Produto("clima1", "Chuva", "🌧️", 12, Categoria.CLIMA, "1"))
        add(Produto("clima2", "Neve", "❄️", 12, Categoria.CLIMA, "2"))
        // Comidas: vão para a geladeira da casa por dentro (e continuam lá: a criança "come" de mentirinha).
        comidas.forEach { (e, n, en) -> add(Produto("comida$e", n, e, 3, Categoria.COMIDA, e, en)) }
        // Roupas: vão para o guarda-roupa; o traje é desenhado por cima da cor da roupa escolhida no perfil.
        trajes.forEach { t -> add(Produto("roupa${t.id}", t.nome, t.icone, t.preco, Categoria.ROUPA, t.id, t.nomeEn)) }
        // Acessórios: também vão para o guarda-roupa, cada um na gaveta da sua parte do corpo.
        acessorios.forEach { a -> add(Produto("acessorio${a.id}", a.nome, a.icone, a.preco, Categoria.ACESSORIO, a.id, a.nomeEn)) }
    }

    val porId = produtos.associateBy { it.id }

    /** Categorias vendidas na loja do lado de fora (a tela da casa); comidas e roupas ficam na casa por dentro. */
    val categoriasDeFora: Set<Categoria> = setOf(Categoria.TELHADO, Categoria.PORTA, Categoria.PAREDE, Categoria.CHAPEU, Categoria.VISITANTE, Categoria.CLIMA)

    /** Categorias vendidas na lojinha da casa por dentro. */
    val categoriasDeDentro: Set<Categoria> = setOf(Categoria.COMIDA, Categoria.ROUPA, Categoria.ACESSORIO, Categoria.CHAPEU)

    /** Comidas compradas (o que aparece dentro da geladeira), na ordem da loja. */
    fun naGeladeira(e: EstadoJogo): List<Produto> = produtos.filter { it.categoria == Categoria.COMIDA && it.id in e.comprados }

    /** Roupas compradas (o que aparece no guarda-roupa), na ordem da loja. */
    fun noGuardaRoupa(e: EstadoJogo): List<Produto> = produtos.filter { it.categoria == Categoria.ROUPA && it.id in e.comprados }

    /** Acessórios comprados, na ordem da loja. */
    fun acessoriosComprados(e: EstadoJogo): List<Produto> = produtos.filter { it.categoria == Categoria.ACESSORIO && it.id in e.comprados }

    /** Tudo o que a loja tem para a [gaveta] (comprado ou não), na ordem da loja. */
    fun daGaveta(gaveta: Gaveta): List<Produto> = produtos.filter {
        it.categoria == gaveta.categoria && (gaveta.parte == null || acessorioPorId[it.valor]?.parte == gaveta.parte)
    }

    /** A gaveta do guarda-roupa onde [p] fica (null para o que não se veste). */
    fun gavetaDe(p: Produto): Gaveta? = when (p.categoria) {
        Categoria.ROUPA -> Gaveta.ROUPAS
        Categoria.CHAPEU -> Gaveta.CHAPEUS
        Categoria.ACESSORIO -> acessorioPorId[p.valor]?.parte?.let { parte -> Gaveta.entries.firstOrNull { it.parte == parte } }
        else -> null
    }

    /** O acessório usado nesta [parte] do corpo, se for conhecido e for mesmo desta parte. */
    fun acessorioDe(perfil: Perfil?, parte: ParteDoCorpo): Acessorio? =
        perfil?.acessorios?.get(parte)?.let { id -> acessorioPorId[id]?.takeIf { it.parte == parte } }

    /** O traje vestido, se for conhecido (dados antigos ou estranhos viram "sem traje"). */
    fun trajeDe(perfil: Perfil?): Traje? = perfil?.traje?.takeIf { it.isNotEmpty() }?.let { id -> trajes.firstOrNull { it.id == id } }

    /** O preço sobe um pouquinho a cada compra: sempre há um próximo objetivo. */
    fun preco(p: Produto, e: EstadoJogo): Int = p.precoBase + e.comprados.size

    /** Primeiro o que ainda não tem (do mais barato ao mais caro), depois o que já tem. */
    fun vitrine(e: EstadoJogo, categorias: Set<Categoria> = Categoria.entries.toSet()): List<Produto> =
        produtos.filter { it.categoria in categorias }.sortedWith(compareBy({ it.id in e.comprados }, { it.precoBase }))

    /** O produto está em uso agora? */
    fun emUso(p: Produto, e: EstadoJogo): Boolean = when (p.categoria) {
        Categoria.TELHADO -> e.corTelhado.toString() == p.valor
        Categoria.PORTA -> e.corPorta.toString() == p.valor
        Categoria.PAREDE -> e.papelDeParede.toString() == p.valor
        Categoria.CHAPEU -> e.perfil?.chapeu == p.valor
        Categoria.VISITANTE -> p.valor in e.visitantes
        Categoria.CLIMA -> e.clima.toString() == p.valor
        Categoria.COMIDA -> p.id in e.comprados
        Categoria.ROUPA -> e.perfil?.traje == p.valor
        Categoria.ACESSORIO -> e.perfil?.usa(p) == true
    }
}

/** O perfil com [p] vestido (roupa, chapéu ou acessório na sua parte do corpo). Não confere se é seu. */
fun Perfil.vestindo(p: Produto): Perfil = when (p.categoria) {
    Categoria.ROUPA -> copy(traje = p.valor)
    Categoria.CHAPEU -> copy(chapeu = p.valor)
    Categoria.ACESSORIO -> Loja.acessorioPorId[p.valor]?.let { copy(acessorios = acessorios + (it.parte to it.id)) } ?: this
    else -> this
}

/** A criança está usando [p] agora? */
fun Perfil.usa(p: Produto): Boolean = when (p.categoria) {
    Categoria.ROUPA -> traje == p.valor
    Categoria.CHAPEU -> chapeu == p.valor
    Categoria.ACESSORIO -> Loja.acessorioPorId[p.valor]?.let { acessorios[it.parte] == it.id } ?: false
    else -> false
}

/** O perfil sem nada na [gaveta] (a roupa volta a ser a de sempre). */
fun Perfil.tirando(gaveta: Gaveta): Perfil = when {
    gaveta == Gaveta.ROUPAS -> copy(traje = "")
    gaveta == Gaveta.CHAPEUS -> copy(chapeu = "")
    gaveta.parte != null -> copy(acessorios = acessorios - gaveta.parte)
    else -> this
}

/** Tem alguma coisa vestida na [gaveta]? */
fun Perfil.temNa(gaveta: Gaveta): Boolean = when {
    gaveta == Gaveta.ROUPAS -> Loja.trajeDe(this) != null
    gaveta == Gaveta.CHAPEUS -> chapeu.isNotEmpty()
    gaveta.parte != null -> Loja.acessorioDe(this, gaveta.parte) != null
    else -> false
}
