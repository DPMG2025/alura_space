package br.com.casinhadosaber.dominio

/** Tipos de enfeite que a loja vende. */
enum class Categoria { TELHADO, PORTA, PAREDE, CHAPEU, VISITANTE, CLIMA, COMIDA, ROUPA }

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
    )

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
    }

    val porId = produtos.associateBy { it.id }

    /** Categorias vendidas na loja do lado de fora (a tela da casa); comidas e roupas ficam na casa por dentro. */
    val categoriasDeFora: Set<Categoria> = setOf(Categoria.TELHADO, Categoria.PORTA, Categoria.PAREDE, Categoria.CHAPEU, Categoria.VISITANTE, Categoria.CLIMA)

    /** Categorias vendidas na lojinha da casa por dentro. */
    val categoriasDeDentro: Set<Categoria> = setOf(Categoria.COMIDA, Categoria.ROUPA, Categoria.CHAPEU)

    /** Comidas compradas (o que aparece dentro da geladeira), na ordem da loja. */
    fun naGeladeira(e: EstadoJogo): List<Produto> = produtos.filter { it.categoria == Categoria.COMIDA && it.id in e.comprados }

    /** Roupas compradas (o que aparece no guarda-roupa), na ordem da loja. */
    fun noGuardaRoupa(e: EstadoJogo): List<Produto> = produtos.filter { it.categoria == Categoria.ROUPA && it.id in e.comprados }

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
    }
}
