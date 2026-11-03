package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.fases.FasesDoCaminho
import br.com.casinhadosaber.dominio.fases.FasesIngles
import br.com.casinhadosaber.dominio.fases.FasesLetras
import br.com.casinhadosaber.dominio.fases.FasesLogica
import br.com.casinhadosaber.dominio.fases.FasesNumeros
import br.com.casinhadosaber.dominio.fases.ModeloDeFase
import kotlin.random.Random

/**
 * Cria as perguntas de cada fase. As perguntas são sorteadas a cada partida,
 * então a criança pode repetir uma fase sem decorar a ordem das respostas.
 *
 * Cada trilha tem [FASES_POR_TRILHA] fases, da mais fácil para a mais difícil, em 25 mundos de 10 ([Mundos]).
 * O conteúdo de cada caminho fica num arquivo próprio em `dominio/fases/` ([FasesNumeros], [FasesLetras],
 * [FasesIngles], [FasesLogica]); aqui fica só o que vale para todos: montar a partida, a revisão e a missão.
 */
class GeradorDeFases(private val random: Random = Random.Default) {

    private val caminhos: Map<Trilha, FasesDoCaminho> by lazy {
        listOf(FasesNumeros(random), FasesLetras(random), FasesIngles(random), FasesLogica(random)).associateBy { it.trilha }
    }

    /** O modelo da fase de índice [indice] (começa em 0, como [Fase.numero]). */
    private fun modelo(trilha: Trilha, indice: Int): ModeloDeFase = caminhos.getValue(trilha).fase(indice + 1)

    fun fases(trilha: Trilha): List<Fase> =
        List(FASES_POR_TRILHA) { i -> modelo(trilha, i).let { m -> Fase(trilha, i, m.titulo, m.emoji) } }

    fun perguntas(fase: Fase, quantidade: Int = PERGUNTAS_POR_FASE): List<Pergunta> {
        val lista = perguntasDoModelo(modelo(fase.trilha, fase.numero), quantidade)
        // Revisão: a partir da 2ª fase, a pergunta do meio vem de uma fase anterior do mesmo caminho.
        if (fase.numero > 0 && quantidade >= 3 && lista.size == quantidade) {
            val meio = quantidade / 2
            val vizinhas = listOf(lista[meio - 1], lista[meio + 1])
            val respostasVizinhas = vizinhas.map { it.opcoes[it.indiceCorreto].visual to it.instrucao }
            // A revisão não pode ter a mesma resposta de uma vizinha, nem formar 3 atividades de mexer iguais seguidas.
            fun serve(p: Pergunta) = (p.opcoes[p.indiceCorreto].visual to p.instrucao) !in respostasVizinhas &&
                vizinhas.none { tipoDeMexer(it) != null && tipoDeMexer(it) == tipoDeMexer(p) }
            var revisao = modelo(fase.trilha, random.nextInt(fase.numero)).criar()
            var tentativas = 0
            while (!serve(revisao) && tentativas++ < 40) {
                revisao = modelo(fase.trilha, random.nextInt(fase.numero)).criar()
            }
            // A primeira fase de cada caminho é sempre de tocar (Escolha): nunca forma sequência de mexer.
            if (!serve(revisao)) revisao = modelo(fase.trilha, 0).criar()
            lista[meio] = revisao
        }
        return lista
    }

    /** Missão do dia: 3 perguntas das fases que a criança mais precisa revisar. */
    fun perguntasDaMissao(fases: List<Fase>): List<Pergunta> {
        if (fases.isEmpty()) return emptyList()
        val lista = MutableList(3) { i -> perguntaDaFase(fases[i % fases.size]) }
        // Nunca 3 atividades de mexer iguais seguidas: a última vira uma pergunta de tocar da primeira fase.
        if (tipoDeMexer(lista[0]) != null && lista.all { tipoDeMexer(it) == tipoDeMexer(lista[0]) }) {
            lista[2] = modelo(fases[0].trilha, 0).criar()
        }
        return lista
    }

    private fun perguntasDoModelo(modelo: ModeloDeFase, quantidade: Int): MutableList<Pergunta> {
        val lista = mutableListOf<Pergunta>()
        val chaves = mutableSetOf<String>()
        var tentativas = 0
        while (lista.size < quantidade && tentativas < 400) {
            tentativas++
            val p = modelo.criar()
            // Nas atividades de mexer a "resposta" é sempre o ✔: o que diferencia uma da outra é a própria atividade.
            fun respostaDe(q: Pergunta): Any = if (q.atividade != Atividade.Escolha) q.atividade else q.opcoes[q.indiceCorreto].visual
            val resposta = respostaDe(p)
            val chave = p.instrucao + "|" + chaveDaCena(p.cena) + "|" + resposta
            val repetida = chave in chaves
            val mesmaRespostaSeguida = lista.lastOrNull()?.let { respostaDe(it) } == resposta
            // Fases que misturam tipos evitam 3 atividades de mexer iguais seguidas.
            val terceiraIgual = lista.size >= 2 && tipoDeMexer(p) != null &&
                lista.takeLast(2).all { tipoDeMexer(it) == tipoDeMexer(p) }
            // Primeiro tenta perguntas inéditas; fases com poucas variações (3 vogais, por exemplo)
            // podem repetir, mas nunca a mesma resposta duas vezes seguidas.
            val aceitar = when {
                tentativas <= 60 -> !repetida && !mesmaRespostaSeguida && !terceiraIgual
                tentativas <= 160 -> !repetida && !mesmaRespostaSeguida
                tentativas <= 350 -> !mesmaRespostaSeguida
                else -> true
            }
            if (!aceitar) continue
            chaves += chave
            lista += p
        }
        return lista
    }

    private fun chaveDaCena(c: Cena): String = when (c) {
        is Cena.Conta -> "${c.a}${if (c.soma) "+" else "-"}${c.b}"
        else -> c.toString()
    }

    /** Uma pergunta nova de uma fase (treino e revisão). */
    fun perguntaDaFase(fase: Fase): Pergunta = modelo(fase.trilha, fase.numero).criar()

    companion object {
        const val FASES_POR_TRILHA = FasesDoCaminho.TOTAL_DE_FASES
        const val PERGUNTAS_POR_FASE = 5

        /** O tipo da atividade de mexer (cesta, memória...), ou null para as perguntas de tocar. */
        fun tipoDeMexer(p: Pergunta): String? =
            if (p.atividade == Atividade.Escolha) null else p.atividade::class.simpleName
    }
}
