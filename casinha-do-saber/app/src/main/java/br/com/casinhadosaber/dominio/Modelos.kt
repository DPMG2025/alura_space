package br.com.casinhadosaber.dominio

/** Idioma de um trecho falado pelo narrador. */
enum class Idioma { PT, EN }

/** Um pedaço de fala. Frases em inglês são lidas com voz em inglês. */
data class Trecho(val texto: String, val idioma: Idioma = Idioma.PT)

fun pt(texto: String) = Trecho(texto, Idioma.PT)
fun en(texto: String) = Trecho(texto, Idioma.EN)

/** Como uma opção de resposta é desenhada na tela. */
sealed interface Visual {
    /** Um ou mais emojis. [escala] permite mostrar tamanhos diferentes (lógica: maior e menor). */
    data class Emoji(val emoji: String, val escala: Float = 1f) : Visual

    /** Letra, sílaba, número ou palavra escrita. */
    data class Texto(val texto: String) : Visual

    /** Um círculo colorido (cores em inglês). [argb] no formato 0xAARRGGBB. */
    data class Cor(val argb: Long) : Visual
}

data class Opcao(
    val visual: Visual,
    /** O que o narrador diz quando a criança toca nesta opção. */
    val fala: List<Trecho> = emptyList(),
)

/** O que aparece em destaque acima das opções. */
sealed interface Cena {
    data object Nenhuma : Cena
    data class Emojis(val texto: String) : Cena
    data class Palavra(val texto: String) : Cena

    /** Conta visual: [a] (sinal) [b]. Na subtração, os itens de [b] aparecem riscados dentro de [a]. */
    data class Conta(val emoji: String, val a: Int, val b: Int, val soma: Boolean) : Cena

    /** Sequência com o último item escondido por um "?". */
    data class Sequencia(val itens: List<String>) : Cena
}

data class Pergunta(
    /** Texto curto exibido para o adulto que acompanha (a criança ouve). */
    val instrucao: String,
    val fala: List<Trecho>,
    val cena: Cena,
    val opcoes: List<Opcao>,
    val indiceCorreto: Int,
    /** Fala extra depois do acerto, para reforçar o aprendizado (por exemplo, "Cat. Gato."). */
    val reforco: List<Trecho> = emptyList(),
) {
    init {
        require(indiceCorreto in opcoes.indices) { "Índice correto fora das opções" }
    }
}

enum class Trilha(
    val titulo: String,
    val emoji: String,
    val corArgb: Long,
    val corEscuraArgb: Long,
    val chamada: String,
) {
    MATEMATICA("Números", "🔢", 0xFFFF9F1C, 0xFFC76B00, "Vamos brincar com os números!"),
    PORTUGUES("Letras", "🔤", 0xFF2EC4B6, 0xFF14857B, "Vamos brincar com as letras!"),
    INGLES("Inglês", "🌎", 0xFF6C63FF, 0xFF4038C2, "Let's play! Vamos brincar em inglês!"),
    LOGICA("Lógica", "🧩", 0xFFEF476F, 0xFFB5244A, "Vamos pensar e descobrir!"),
}

data class Fase(
    val trilha: Trilha,
    /** Começa em 0. */
    val numero: Int,
    val titulo: String,
    val emoji: String,
)
