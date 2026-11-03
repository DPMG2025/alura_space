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
class GeradorDeFases(
    private val random: Random = Random.Default,
    /** Temas favoritos do perfil ([Perfil.temasFavoritos]): contar e palavras das letras preferem figuras desses temas. */
    private val temasFavoritos: Collection<TemaFavorito> = emptyList(),
) {

    private val caminhos: Map<Trilha, FasesDoCaminho> by lazy {
        listOf(FasesNumeros(random), FasesLetras(random), FasesIngles(random), FasesLogica(random))
            .onEach { it.temasFavoritos = temasFavoritos.toSet() }
            .associateBy { it.trilha }
    }

    /** O modelo da fase de índice [indice] (começa em 0, como [Fase.numero]). */
    private fun modelo(trilha: Trilha, indice: Int): ModeloDeFase = caminhos.getValue(trilha).fase(indice + 1)

    fun fases(trilha: Trilha): List<Fase> =
        List(FASES_POR_TRILHA) { i -> modelo(trilha, i).let { m -> Fase(trilha, i, m.titulo, m.emoji) } }

    fun perguntas(fase: Fase, quantidade: Int = PERGUNTAS_POR_FASE): List<Pergunta> {
        // As regras de variedade valem para a partida inteira; a partida curta (pergunta facilitada) não tem regras.
        val regras = if (quantidade == PERGUNTAS_POR_FASE) regrasDa(fase) else Regras.LIVRE
        // Revisão: a partir da 2ª fase, a pergunta do meio vem de uma fase anterior do mesmo caminho.
        val comRevisao = fase.numero > 0 && quantidade >= 3
        val proprias = if (comRevisao) quantidade - 1 else quantidade
        val lista = perguntasDoModelo(fase, proprias, regras, vagasDepois = quantidade - proprias)
        if (comRevisao && lista.size == proprias) {
            val meio = quantidade / 2
            lista.add(meio, revisao(fase, lista, meio, regras))
            if (!regras.cumpre(lista)) consertar(fase, lista, meio, regras)
        }
        return lista
    }

    /**
     * Quando a revisão não trouxe o jeito de jogar que faltava (os mundos de antes de um caminho podem não ter
     * nenhum desafio diferente), troca uma pergunta da própria fase, de trás para frente, por outra da fase ou
     * de uma fase de apoio que complete as regras.
     */
    private fun consertar(fase: Fase, lista: MutableList<Pergunta>, meio: Int, regras: Regras) {
        val modelo = modelo(fase.trilha, fase.numero)
        val apoio = fasesDeApoio(fase)
        for (i in lista.indices.reversed()) {
            if (i == meio || i == 0) continue
            fun serve(p: Pergunta): Boolean {
                val nova = lista.toMutableList().also { it[i] = p }
                val tipos = nova.map(::tipoDeMexer)
                val tresIguais = (2 until tipos.size).any { k -> tipos[k] != null && tipos[k] == tipos[k - 1] && tipos[k] == tipos[k - 2] }
                val repete = listOfNotNull(lista.getOrNull(i - 1), lista.getOrNull(i + 1)).any { it.opcoes[it.indiceCorreto].visual == p.opcoes[p.indiceCorreto].visual && it.instrucao == p.instrucao }
                return regras.cumpre(nova) && !tresIguais && !repete
            }
            val troca = sortear(60, { modelo.criar() }, ::serve)
                ?: apoio.takeIf { it.isNotEmpty() }?.let { fases -> sortear(120, { fases.random(random).criar() }, ::serve) }
            if (troca != null) {
                lista[i] = troca
                return
            }
        }
    }

    /**
     * A pergunta de revisão do meio. Não pode ter a mesma resposta de uma vizinha, nem formar 3 atividades de mexer
     * iguais seguidas, e prefere um jeito de jogar que ainda falta na partida ([Regras]). No chefão dos mundos novos
     * a revisão vem do próprio mundo (docs/plano-1000-fases.md, seção 5).
     */
    private fun revisao(fase: Fase, lista: List<Pergunta>, meio: Int, regras: Regras): Pergunta {
        val vizinhas = listOf(lista[meio - 1], lista[meio])
        val respostasVizinhas = vizinhas.map { it.opcoes[it.indiceCorreto].visual to it.instrucao }
        fun serve(p: Pergunta) = (p.opcoes[p.indiceCorreto].visual to p.instrucao) !in respostasVizinhas &&
            vizinhas.none { tipoDeMexer(it) != null && tipoDeMexer(it) == tipoDeMexer(p) }
        fun anterior(): Pergunta = modelo(fase.trilha, random.nextInt(fase.numero)).criar()
        val doMundo: () -> Pergunta = if (fase.numero >= FasesDoCaminho.FASES_PRONTAS && Mundos.ehChefao(fase)) {
            { modelo(fase.trilha, Mundos.de(fase).primeiraFase + random.nextInt(Mundos.FASES_POR_MUNDO - 1)).criar() }
        } else {
            ::anterior
        }
        val cabe = { p: Pergunta -> serve(p) && regras.cabe(lista, p, vagasDepois = 0) }
        sortear(80, doMundo, cabe)?.let { return it }
        sortear(80, ::anterior, cabe)?.let { return it }
        sortear(40, doMundo, ::serve)?.let { return it }
        // A primeira fase de cada caminho é sempre de tocar (Escolha): nunca forma sequência de mexer.
        return modelo(fase.trilha, 0).criar()
    }

    /** Até [vezes] sorteios de [gerar]; devolve o primeiro que [aceita], ou null. */
    private inline fun sortear(vezes: Int, gerar: () -> Pergunta, aceita: (Pergunta) -> Boolean): Pergunta? {
        repeat(vezes) { gerar().let { if (aceita(it)) return it } }
        return null
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

    /**
     * As [quantidade] perguntas da própria fase, cumprindo as [regras] (deixando [vagasDepois] perguntas para
     * completar depois, a revisão). Quando a fase sozinha não consegue cumprir as regras (uma fase só de tocar
     * precisa de um terceiro jeito de jogar, por exemplo), a pergunta que falta vem de uma fase de apoio: as fases
     * anteriores do mesmo mundo, que treinam a mesma ideia, ou as do mundo anterior. Assim a dificuldade continua a
     * do mundo e a fase mantém a maioria das perguntas dela.
     */
    private fun perguntasDoModelo(fase: Fase, quantidade: Int, regras: Regras, vagasDepois: Int): MutableList<Pergunta> {
        val modelo = modelo(fase.trilha, fase.numero)
        val apoio by lazy { fasesDeApoio(fase) }
        val lista = mutableListOf<Pergunta>()
        val chaves = mutableSetOf<String>()
        // Nas atividades de mexer a "resposta" é sempre o ✔: o que diferencia uma da outra é a própria atividade.
        fun respostaDe(q: Pergunta): Any = if (q.atividade != Atividade.Escolha) q.atividade else q.opcoes[q.indiceCorreto].visual
        fun chaveDe(q: Pergunta) = q.instrucao + "|" + chaveDaCena(q.cena) + "|" + respostaDe(q)
        while (lista.size < quantidade) {
            val depois = quantidade - lista.size - 1 + vagasDepois
            val cabe = { p: Pergunta -> regras.cabe(lista, p, depois) }
            val inedita = { p: Pergunta -> chaveDe(p) !in chaves }
            val semRepetirSeguida = { p: Pergunta -> lista.lastOrNull()?.let { respostaDe(it) } != respostaDe(p) }
            // Fases que misturam tipos evitam 3 atividades de mexer iguais seguidas.
            val semTerceiraIgual = { p: Pergunta ->
                !(lista.size >= 2 && tipoDeMexer(p) != null && lista.takeLast(2).all { tipoDeMexer(it) == tipoDeMexer(p) })
            }
            var algumaCabe = false
            val daFase = { modelo.criar() }
            // Primeiro tenta perguntas inéditas; fases com poucas variações (3 vogais, por exemplo)
            // podem repetir, mas nunca a mesma resposta duas vezes seguidas.
            val p = sortear(60, daFase) { q -> cabe(q).also { algumaCabe = algumaCabe || it } && inedita(q) && semRepetirSeguida(q) && semTerceiraIgual(q) }
                ?: (if (algumaCabe) sortear(100, daFase) { q -> cabe(q) && inedita(q) && semRepetirSeguida(q) } else null)
                ?: (if (algumaCabe) sortear(190, daFase) { q -> cabe(q) && semRepetirSeguida(q) } else null)
                ?: apoio.takeIf { it.isNotEmpty() }?.let { fases ->
                    // As fases de apoio do próprio mundo vêm primeiro na lista e têm mais chance.
                    val doApoio = { fases[minOf(random.nextInt(fases.size), random.nextInt(fases.size))].criar() }
                    sortear(120, doApoio) { q -> cabe(q) && inedita(q) && semRepetirSeguida(q) && semTerceiraIgual(q) }
                        ?: sortear(60, doApoio) { q -> cabe(q) && semRepetirSeguida(q) }
                }
                ?: sortear(40, daFase, cabe)
                ?: modelo.criar()
            chaves += chaveDe(p)
            lista += p
        }
        return lista
    }

    /** Fases que podem completar uma fase sem variedade: as anteriores do mesmo mundo e depois as do mundo anterior (sem o chefão). */
    private fun fasesDeApoio(fase: Fase): List<ModeloDeFase> {
        val inicio = Mundos.de(fase).primeiraFase
        val doMundo = (fase.numero - 1 downTo inicio).map { modelo(fase.trilha, it) }
        val doAnterior = if (inicio == 0) emptyList() else (inicio - 2 downTo inicio - Mundos.FASES_POR_MUNDO).map { modelo(fase.trilha, it) }
        return doMundo + doAnterior
    }

    private fun chaveDaCena(c: Cena): String = when (c) {
        is Cena.Conta -> "${c.a}${if (c.soma) "+" else "-"}${c.b}"
        else -> c.toString()
    }

    /** Uma pergunta nova de uma fase (treino e revisão). */
    fun perguntaDaFase(fase: Fase): Pergunta = modelo(fase.trilha, fase.numero).criar()

    /**
     * Regras de variedade de uma partida inteira:
     * no máximo [maxMesmoMexer] perguntas do mesmo desafio de mexer, no máximo [maxToques] perguntas de tocar,
     * pelo menos [minTipos] jeitos de jogar diferentes (tocar conta como um) e pelo menos [minTiposDeMexer]
     * desafios de mexer diferentes.
     */
    class Regras(val maxMesmoMexer: Int, val maxToques: Int, val minTipos: Int, val minTiposDeMexer: Int) {

        /** A pergunta [p] entra depois de [lista] e ainda dá para cumprir as regras com [vagasDepois] perguntas a mais? */
        fun cabe(lista: List<Pergunta>, p: Pergunta, vagasDepois: Int): Boolean {
            val tipos = lista.map(::tipoDeJogo) + tipoDeJogo(p)
            val t = tipos.last()
            val limite = if (t == TOCAR) maxToques else maxMesmoMexer
            if (tipos.count { it == t } > limite) return false
            if (tipos.toSet().size + vagasDepois < minTipos) return false
            return tipos.filter { it != TOCAR }.toSet().size + vagasDepois >= minTiposDeMexer
        }

        /** A partida inteira [lista] cumpre as regras? */
        fun cumpre(lista: List<Pergunta>): Boolean {
            val tipos = lista.map(::tipoDeJogo)
            val contagem = tipos.groupingBy { it }.eachCount()
            return contagem.all { (t, n) -> n <= if (t == TOCAR) maxToques else maxMesmoMexer } &&
                contagem.size >= minTipos && contagem.keys.count { it != TOCAR } >= minTiposDeMexer
        }

        companion object {
            val LIVRE = Regras(Int.MAX_VALUE, Int.MAX_VALUE, 0, 0)
        }
    }

    companion object {
        const val FASES_POR_TRILHA = FasesDoCaminho.TOTAL_DE_FASES
        const val PERGUNTAS_POR_FASE = 5

        /** O tipo da atividade de mexer (cesta, memória...), ou null para as perguntas de tocar. */
        fun tipoDeMexer(p: Pergunta): String? =
            if (p.atividade == Atividade.Escolha) null else p.atividade::class.simpleName

        /** O jeito de jogar da pergunta: o tipo da atividade de mexer, ou [TOCAR] para as perguntas de escolher. */
        fun tipoDeJogo(p: Pergunta): String = tipoDeMexer(p) ?: TOCAR

        const val TOCAR = "Tocar"

        /**
         * Regras de variedade de cada fase (docs/plano-1000-fases.md, seção 5):
         * nos mundos 1 a 5 (a criança ainda aprendendo a jogar, só com os desafios de antes) a fase pode treinar um
         * desafio só, como a cesta das coisas; do mundo 6 em diante, no máximo 2 do mesmo desafio e pelo menos 3 jeitos de jogar; e o chefão dos mundos
         * 6 a 25 tem no máximo 2 perguntas de tocar (40%) e pelo menos 3 desafios de mexer diferentes.
         */
        fun regrasDa(fase: Fase): Regras = when {
            fase.numero < FasesDoCaminho.FASES_PRONTAS -> Regras.LIVRE
            Mundos.ehChefao(fase) -> Regras(maxMesmoMexer = 2, maxToques = 2, minTipos = 3, minTiposDeMexer = 3)
            else -> Regras(maxMesmoMexer = 2, maxToques = Int.MAX_VALUE, minTipos = 3, minTiposDeMexer = 0)
        }
    }
}
