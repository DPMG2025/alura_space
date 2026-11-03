package br.com.casinhadosaber.dominio

/**
 * Cada caminho tem 50 fases em 5 mundos de 10 fases, do mais fácil para o mais difícil.
 * O mundo é só um jeito de a criança (e os pais) se orientarem: o número da fase não muda,
 * então o progresso salvo de antes (25 fases por caminho) continua valendo.
 */
data class Mundo(val indice: Int, val nome: String, val emoji: String, val corArgb: Long) {
    val numero: Int get() = indice + 1
    val primeiraFase: Int get() = indice * Mundos.FASES_POR_MUNDO
    val ultimaFase: Int get() = primeiraFase + Mundos.FASES_POR_MUNDO - 1
    val titulo: String get() = "Mundo $numero: $nome"
}

object Mundos {
    const val FASES_POR_MUNDO = 10

    val todos = listOf(
        Mundo(0, "Jardim", "🌷", 0xFFB9F6CA),
        Mundo(1, "Floresta", "🌳", 0xFFC8E6C9),
        Mundo(2, "Praia", "🏖️", 0xFFFFF3C4),
        Mundo(3, "Montanha", "⛰️", 0xFFE1D5F5),
        Mundo(4, "Espaço", "🚀", 0xFFCFD8FF),
    )

    fun daFase(numero: Int): Mundo = todos[(numero / FASES_POR_MUNDO).coerceIn(0, todos.lastIndex)]

    fun de(fase: Fase): Mundo = daFase(fase.numero)

    /** A fase abre um mundo novo (a primeira de cada mundo). */
    fun comecaMundo(fase: Fase): Boolean = fase.numero % FASES_POR_MUNDO == 0
}
