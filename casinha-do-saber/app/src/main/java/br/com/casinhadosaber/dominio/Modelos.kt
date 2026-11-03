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

    /** A silhueta escura de um emoji (jogo das sombras). */
    data class Sombra(val emoji: String) : Visual
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

    /**
     * Quadro em linhas e colunas (sudoku de figuras, gráfico de figuras).
     * "" é uma casinha vazia e "?" é a casinha da pergunta. [blocos] > 0 separa o quadro em
     * blocos daquele tamanho (no sudoku 4 por 4, blocos de 2 por 2).
     */
    data class Grade(val colunas: Int, val itens: List<String>, val blocos: Int = 0) : Cena {
        init {
            require(colunas > 0 && itens.size % colunas == 0) { "Quadro incompleto" }
        }
        val linhas: Int get() = itens.size / colunas

        companion object {
            /** Casinha tampada (sudoku): desenhada como uma tampinha fechada, nunca como uma casa sem figura. */
            const val TAMPADA = "#"
        }
    }

    /** Material dourado: [dezenas] barrinhas de dez cubinhos e [unidades] cubinhos soltos. */
    data class MaterialDourado(val dezenas: Int, val unidades: Int) : Cena {
        init {
            require(dezenas in 0..9 && unidades in 0..9) { "Material dourado só até 99" }
        }
        val valor: Int get() = dezenas * 10 + unidades
    }

    /**
     * Labirinto de [linhas] por [colunas] casinhas: [caminho] são as casinhas (linha * colunas + coluna), na ordem,
     * da criança (primeira) até a casinha de chegada (última). O resto é mato com árvores.
     */
    data class Labirinto(val linhas: Int, val colunas: Int, val caminho: List<Int>) : Cena {
        init {
            require(caminho.size >= 2 && caminho.all { it in 0 until linhas * colunas }) { "Caminho fora do labirinto" }
        }
    }
}

/**
 * Como a criança responde. Além de tocar na resposta certa, há atividades de mexer:
 * arrastar para a cesta, jogo da memória, montar a palavra e traçar a letra.
 */
sealed interface Atividade {
    /** Tocar numa das opções (o padrão). */
    data object Escolha : Atividade

    /** Arrastar figuras para a cesta: contar ([alvo] figuras) ou classificar (só as do grupo). */
    data class Cesta(
        /** Figuras que podem ir para a cesta. */
        val certos: List<String>,
        /** Quantas devem ir (contar). Em classificação, todas as [certos]. */
        val alvo: Int,
        /** Figuras que não pertencem ao grupo (classificar): se forem para a cesta, voltam. */
        val errados: List<String> = emptyList(),
    ) : Atividade {
        val classificar get() = errados.isNotEmpty()
    }

    /** Jogo da memória: achar os pares. [falas] diz o nome (em inglês, por exemplo) de cada figura ao virar. */
    data class Memoria(val pares: List<Pair<String, String>>, val falas: Map<String, Trecho> = emptyMap()) : Atividade

    /** Arrastar as sílabas na ordem certa para formar a palavra da figura. */
    data class MontarPalavra(val emoji: String, val palavra: String, val silabas: List<String>, val extras: List<String>) : Atividade

    /** Passar o dedo por cima da letra pontilhada. */
    data class Tracar(val letra: String) : Atividade
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
    val atividade: Atividade = Atividade.Escolha,
) {
    init {
        require(indiceCorreto in opcoes.indices) { "Índice correto fora das opções" }
    }

    companion object {
        /** Pergunta de mexer (sem opções para tocar). */
        fun deAtividade(instrucao: String, fala: List<Trecho>, atividade: Atividade, reforco: List<Trecho> = emptyList(), cena: Cena = Cena.Nenhuma) =
            Pergunta(instrucao, fala, cena, listOf(Opcao(Visual.Texto("✔"))), 0, reforco, atividade)
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
