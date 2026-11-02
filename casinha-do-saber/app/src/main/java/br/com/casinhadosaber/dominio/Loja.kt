package br.com.casinhadosaber.dominio

/**
 * Loja de estrelas: as estrelas ganhas nas fases compram enfeites para a casa.
 * Assim, mesmo depois das 40 peças, sempre há um próximo objetivo.
 */
sealed interface ProdutoDaLoja {
    val id: String
    val nome: String
    val icone: String
    val preco: Int

    /** Troca a cor do telhado (cada compra passa para a próxima cor da paleta). */
    data class CorDoTelhado(val indice: Int) : ProdutoDaLoja {
        override val id = "telhado$indice"
        override val nome = "Telhado ${Loja.nomesDasCores[indice]}"
        override val icone = Loja.bolinhasDasCores[indice]
        override val preco = 5
    }

    /** Um bichinho visitante que passeia pelo jardim. */
    data class Visitante(override val icone: String, override val nome: String) : ProdutoDaLoja {
        override val id = "visitante$icone"
        override val preco = 8
    }
}

object Loja {
    val coresDoTelhado = listOf(0xFFEF476F, 0xFF4D96FF, 0xFF3DBE6B, 0xFF9B6BFF, 0xFFFFA62B, 0xFF8D5A3B)
    val coresDoTelhadoEscuras = listOf(0xFFC7365A, 0xFF2F6FCC, 0xFF2A9452, 0xFF7048CC, 0xFFCC7F1A, 0xFF6B3E22)
    val nomesDasCores = listOf("vermelho", "azul", "verde", "roxo", "laranja", "marrom")
    val bolinhasDasCores = listOf("🔴", "🔵", "🟢", "🟣", "🟠", "🟤")

    val visitantes = listOf(
        ProdutoDaLoja.Visitante("🦔", "Ouriço"),
        ProdutoDaLoja.Visitante("🐰", "Coelhinho"),
        ProdutoDaLoja.Visitante("🦊", "Raposinha"),
        ProdutoDaLoja.Visitante("🐢", "Tartaruga"),
        ProdutoDaLoja.Visitante("🦜", "Papagaio"),
        ProdutoDaLoja.Visitante("🐿️", "Esquilo"),
    )

    /** O que aparece na loja agora: a próxima cor de telhado e os visitantes que ainda não vieram. */
    fun vitrine(e: EstadoJogo): List<ProdutoDaLoja> {
        val proximaCor = ProdutoDaLoja.CorDoTelhado((e.corTelhado + 1) % coresDoTelhado.size)
        return listOf(proximaCor) + visitantes.filter { it.icone !in e.visitantes }
    }
}
