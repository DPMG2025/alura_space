package br.com.casinhadosaber.dominio

/** Tipos de enfeite que a loja vende. */
enum class Categoria { TELHADO, PORTA, PAREDE, CHAPEU, VISITANTE, CLIMA }

/**
 * Um enfeite da loja, pago com moedinhas 🪙 (ganhas a cada acerto de primeira, até em fases repetidas).
 * [valor] é o índice da cor/estilo, ou o emoji do chapéu/visitante.
 */
data class Produto(
    val id: String,
    val nome: String,
    val icone: String,
    val precoBase: Int,
    val categoria: Categoria,
    val valor: String,
)

object Loja {
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
    }

    val porId = produtos.associateBy { it.id }

    /** O preço sobe um pouquinho a cada compra: sempre há um próximo objetivo. */
    fun preco(p: Produto, e: EstadoJogo): Int = p.precoBase + e.comprados.size

    /** Primeiro o que ainda não tem (do mais barato ao mais caro), depois o que já tem. */
    fun vitrine(e: EstadoJogo): List<Produto> =
        produtos.sortedWith(compareBy({ it.id in e.comprados }, { it.precoBase }))

    /** O produto está em uso agora? */
    fun emUso(p: Produto, e: EstadoJogo): Boolean = when (p.categoria) {
        Categoria.TELHADO -> e.corTelhado.toString() == p.valor
        Categoria.PORTA -> e.corPorta.toString() == p.valor
        Categoria.PAREDE -> e.papelDeParede.toString() == p.valor
        Categoria.CHAPEU -> e.perfil?.chapeu == p.valor
        Categoria.VISITANTE -> p.valor in e.visitantes
        Categoria.CLIMA -> e.clima.toString() == p.valor
    }
}
