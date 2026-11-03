package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.*
import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Ordena calculando a chave uma vez só por elemento. O sortedBy chama a chave a cada comparação,
 * e uma chave com sorteio deixa a ordenação inconsistente (o TimSort pode lançar exceção com listas grandes).
 */
internal inline fun <T> Iterable<T>.ordenarUmaVez(chave: (T) -> Number): List<T> =
    map { it to chave(it).toDouble() }.sortedBy { it.second }.map { it.first }

/** Uma fase de um caminho: nome, emoji e como sortear uma pergunta nova dela. */
class ModeloDeFase(val titulo: String, val emoji: String, val criar: () -> Pergunta)

/**
 * Base dos geradores de cada caminho ([FasesNumeros], [FasesLetras], [FasesIngles], [FasesLogica]).
 * Aqui ficam só as utilidades usadas por mais de um caminho; o que é de um caminho só fica no
 * arquivo dele (assim cada caminho pode crescer sem mexer nos outros).
 */
abstract class FasesDoCaminho(protected val random: Random) {

    abstract val trilha: Trilha

    /** A fase [numero], de 1 a [TOTAL_DE_FASES] (a fase 1 é a primeira bolinha do caminho). */
    abstract fun fase(numero: Int): ModeloDeFase

    /**
     * Fases 51 a 250 provisórias: enquanto um mundo novo não ganha conteúdo próprio (ver
     * docs/plano-1000-fases.md), cada fase repete a fase correspondente do mundo 5 (41 a 50), com a
     * 10ª de cada mundo sendo o desafio que mistura tudo (o mestre do mundo 5).
     */
    protected fun provisoria(numero: Int, ate50: List<ModeloDeFase>): ModeloDeFase {
        val base = ate50[40 + (numero - 1) % 10]
        return ModeloDeFase(base.titulo, base.emoji, base.criar)
    }

    protected fun conferir(numero: Int) {
        require(numero in 1..TOTAL_DE_FASES) { "Fase $numero fora de 1..$TOTAL_DE_FASES" }
    }

    // ------------------------------------------------------------------ utilidades

    protected fun <T> List<T>.sorteia(n: Int): List<T> = shuffled(random).take(n)
    protected fun <T> List<T>.um(): T = random(random)

    /** Monta a pergunta embaralhando as opções e guardando onde ficou a certa. */
    protected fun montar(
        instrucao: String,
        fala: List<Trecho>,
        cena: Cena,
        certa: Opcao,
        erradas: List<Opcao>,
        reforco: List<Trecho> = emptyList(),
    ): Pergunta {
        val opcoes = (erradas + certa).shuffled(random)
        return Pergunta(instrucao, fala, cena, opcoes, opcoes.indexOf(certa), reforco)
    }

    /** Números diferentes do correto, próximos dele, dentro de [min]..[max]. */
    protected fun numerosErrados(correto: Int, min: Int, max: Int, qtd: Int): List<Int> {
        val perto = (min..max).filter { it != correto }.ordenarUmaVez { kotlin.math.abs(it - correto) + random.nextDouble() }
        return perto.take(qtd).shuffled(random)
    }

    protected fun opcaoNumero(n: Int) = Opcao(Visual.Texto(n.toString()), listOf(pt(porExtenso(n))))

    // ------------------------------------------------------------------ LÓGICA

    protected fun opEmoji(e: String, escala: Float = 1f) = Opcao(Visual.Emoji(e, escala))

    protected fun grupo(): Pergunta {
        val g = Vocabulario.grupos.um()
        val certa = g.itens.um()
        val erradas = Vocabulario.grupos.filter { it != g }.sorteia(3).map { it.itens.um() }
        return montar(
            instrucao = g.pergunta,
            fala = listOf(pt(g.pergunta)),
            cena = Cena.Nenhuma,
            certa = opEmoji(certa),
            erradas = erradas.map { opEmoji(it) },
            reforco = listOf(pt("Muito bem pensado!")),
        )
    }

    protected fun memoria(paresQtd: Int, fonte: List<PalavraEn>? = null, iguais: List<String>? = null): Pergunta {
        val pares: List<Pair<String, String>>
        val falas = mutableMapOf<String, Trecho>()
        if (fonte != null) {
            val escolhidos = fonte.distinctBy { it.emoji }.sorteia(paresQtd)
            pares = escolhidos.map { it.emoji to it.emoji }
            escolhidos.forEach { falas[it.emoji] = en(it.en) }
        } else {
            pares = (iguais ?: Vocabulario.figurasSimples).sorteia(paresQtd).map { it to it }
        }
        return Pergunta.deAtividade(
            instrucao = "Ache os pares",
            fala = listOf(pt("Jogo da memória! Vire duas cartas e ache as figuras iguais.")),
            atividade = Atividade.Memoria(pares, falas),
            reforco = listOf(pt("Achou todos os pares!")),
        )
    }

    protected fun formas(emIngles: Boolean): Pergunta {
        val certa = Vocabulario.formas.um()
        val erradas = Vocabulario.formas.filter { it != certa }.sorteia(3)
        val op = { f: PalavraEn -> Opcao(Visual.Emoji(f.emoji), listOf(if (emIngles) en(f.en) else pt(f.pt))) }
        return montar(
            instrucao = if (emIngles) "Encontre: ${certa.en}" else "Toque no ${certa.pt}",
            fala = if (emIngles) listOf(pt("Encontre a forma"), en(certa.en)) else listOf(pt("Toque na forma: ${certa.pt}.")),
            cena = if (emIngles) Cena.Palavra(certa.en) else Cena.Nenhuma,
            certa = op(certa),
            erradas = erradas.map(op),
            reforco = if (emIngles) listOf(en(certa.en + "!"), pt("${certa.pt}!")) else listOf(pt("Isso! ${certa.pt}!")),
        )
    }

    // ================================================================== MUNDOS 3, 4 E 5 (fases 26 a 50)

    protected val V = VocabularioDosMundos

    /** Três números errados tirados de [candidatos], os mais perto do certo primeiro. */
    protected fun erradosDe(certo: Int, candidatos: Iterable<Int>): List<Int> =
        candidatos.filter { it != certo }.distinct()
            .ordenarUmaVez { kotlin.math.abs(it - certo) + random.nextDouble() * 1.5 }
            .take(3)

    protected fun opcaoTexto(texto: String, fala: String = "") =
        Opcao(Visual.Texto(texto), if (fala.isBlank()) emptyList() else listOf(pt(fala)))

    protected fun opcaoFigura(emoji: String, fala: String = "") =
        Opcao(Visual.Emoji(emoji), if (fala.isBlank()) emptyList() else listOf(pt(fala)))

    /** Fila de números de 2 em 2, 3 em 3, 5 em 5 ou 10 em 10, para frente ou para trás. */
    protected fun sequenciaNumerica(max: Int): Pergunta {
        val passo = listOf(2, 3, 5, 10).filter { it * 4 <= max }.um()
        val crescente = random.nextInt(3) != 0
        val inicioMax = max - passo * 3
        val inicio = (0..inicioMax).random(random)
        val fila = List(4) { inicio + it * passo }.let { if (crescente) it else it.reversed() }
        val resposta = fila.last()
        val candidatos = listOf(resposta + 1, resposta - 1, resposta + passo, resposta - passo, resposta + 2 * passo, resposta + 10, resposta - 10)
        return montar(
            instrucao = "Que número vem depois?",
            fala = listOf(pt(fila.dropLast(1).joinToString(", ") { porExtenso(it) } + ". Que número vem depois?")),
            cena = Cena.Sequencia(fila.dropLast(1).map { it.toString() } + "?"),
            certa = opcaoNumero(resposta),
            erradas = erradosDe(resposta, candidatos.filter { it >= 0 }).map(::opcaoNumero),
            reforco = listOf(pt("De ${porExtenso(passo)} em ${porExtenso(passo)}: " + fila.joinToString(", ") { porExtenso(it) } + "!")),
        )
    }

    companion object {
        /** Fases de cada caminho: 25 mundos de 10. */
        const val TOTAL_DE_FASES = 250

        /** Fases que já têm conteúdo próprio (as outras usam [provisoria]). */
        const val FASES_PRONTAS = 50
    }
}
