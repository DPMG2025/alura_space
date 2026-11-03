package br.com.casinhadosaber.dominio

/**
 * Cada caminho tem 250 fases em 25 mundos de 10 fases, do mais fácil para o mais difícil
 * (plano completo em docs/plano-1000-fases.md). O mundo é só um jeito de a criança (e os pais)
 * se orientarem: o número da fase não muda, então o progresso salvo de antes (25 e depois
 * 50 fases por caminho) continua valendo. A 10ª fase de cada mundo é o chefão do mundo.
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
        Mundo(5, "Fazenda", "🐄", 0xFFFFE0B2),
        Mundo(6, "Fundo do Mar", "🐠", 0xFFB3E5FC),
        Mundo(7, "Selva", "🐒", 0xFFDCEDC8),
        Mundo(8, "Polo Norte", "🐧", 0xFFE3F2FD),
        Mundo(9, "Circo", "🎪", 0xFFFFCDD2),
        Mundo(10, "Deserto", "🐫", 0xFFFFECB3),
        Mundo(11, "Cidade", "🏙️", 0xFFCFD8DC),
        Mundo(12, "Castelo", "🏰", 0xFFD1C4E9),
        Mundo(13, "Vale dos Dinossauros", "🦕", 0xFFC5E1A5),
        Mundo(14, "Ilha do Tesouro", "🏝️", 0xFFB2EBF2),
        Mundo(15, "Fábrica de Doces", "🍭", 0xFFF8BBD0),
        Mundo(16, "Bosque Encantado", "🍄", 0xFFE1BEE7),
        Mundo(17, "Vulcão", "🌋", 0xFFFFCCBC),
        Mundo(18, "Reino das Nuvens", "☁️", 0xFFE1F5FE),
        Mundo(19, "Caverna de Cristal", "💎", 0xFFB2DFDB),
        Mundo(20, "Parque de Diversões", "🎡", 0xFFFFF9C4),
        Mundo(21, "Cidade dos Robôs", "🤖", 0xFFD7CCC8),
        Mundo(22, "Savana", "🦁", 0xFFFFE57F),
        Mundo(23, "Vale das Cores", "🌈", 0xFFF3E5F5),
        Mundo(24, "Castelo das Estrelas", "🌟", 0xFFC5CAE9),
    )

    fun daFase(numero: Int): Mundo = todos[(numero / FASES_POR_MUNDO).coerceIn(0, todos.lastIndex)]

    fun de(fase: Fase): Mundo = daFase(fase.numero)

    /** A fase abre um mundo novo (a primeira de cada mundo). */
    fun comecaMundo(fase: Fase): Boolean = fase.numero % FASES_POR_MUNDO == 0

    /** A fase é o chefão do mundo (a 10ª de cada mundo): desafio que mistura o mundo inteiro. */
    fun ehChefao(fase: Fase): Boolean = fase.numero % FASES_POR_MUNDO == FASES_POR_MUNDO - 1
}
