package br.com.casinhadosaber.dominio

/**
 * "Descobrir o nível" (opcional): umas 8 a 10 perguntas que sobem pelos mundos de um caminho enquanto a criança
 * acerta, para quem já sabe bastante não precisar passar por tudo de novo.
 *
 * Como sobe: começa no mundo 1 e pula de 3 em 3 mundos (1, 4, 7...) enquanto acerta. Errar uma vez traz outra
 * pergunta do mesmo mundo; com 2 erros no mesmo mundo, volta para o mundo logo depois do último que passou e
 * sobe de 1 em 1 até errar 2 vezes de novo (ou chegar no mundo que já tinha errado). No máximo [MAXIMO] perguntas.
 *
 * O resultado só abre as fases até o começo do mundo sugerido ([EstadoJogo.liberadaAte]): nada é marcado como
 * vencido, então estrelas, álbum, diplomas e peças da casa continuam sendo ganhos jogando. Tudo é calculado
 * dos acertos (sem sorteio) e os pais podem desfazer ([EstadoJogo.desfazerNivel]).
 */
data class Nivelamento(
    val trilha: Trilha,
    /** Mundo da pergunta da vez (1 a 25). */
    val mundo: Int = 1,
    val salto: Int = SALTO_INICIAL,
    /** Maior mundo em que a criança acertou (0 = nenhum). */
    val aprovado: Int = 0,
    /** Menor mundo com 2 erros (26 = nenhum). */
    val limite: Int = Mundos.todos.size + 1,
    val errosNoMundo: Int = 0,
    val perguntasNoMundo: Int = 0,
    val feitas: Int = 0,
    val terminou: Boolean = false,
) {
    /** Fase (índice) da pergunta da vez: o marco do mundo e, na segunda pergunta do mundo, a terceira fase dele. */
    val faseDaPergunta: Int get() = (mundo - 1) * Mundos.FASES_POR_MUNDO + if (perguntasNoMundo == 0) 4 else 2

    /** Mundo para começar: o seguinte ao último que passou (no máximo o 25). */
    val mundoSugerido: Int get() = (aprovado + 1).coerceAtMost(Mundos.todos.size)

    /** Fase (índice) até onde o caminho abre: o começo do mundo sugerido (0 = nada muda). */
    val liberarAte: Int get() = (mundoSugerido - 1) * Mundos.FASES_POR_MUNDO

    fun responder(certo: Boolean): Nivelamento {
        if (terminou) return this
        val n = feitas + 1
        val proximo = if (certo) {
            val passou = maxOf(aprovado, mundo)
            val seguinte = mundo + salto
            copy(aprovado = passou, mundo = seguinte, errosNoMundo = 0, perguntasNoMundo = 0, feitas = n,
                terminou = seguinte > Mundos.todos.size || seguinte >= limite)
        } else {
            val erros = errosNoMundo + 1
            when {
                erros < 2 -> copy(errosNoMundo = erros, perguntasNoMundo = perguntasNoMundo + 1, feitas = n)
                // Errou 2 vezes num mundo pulando de 3 em 3: volta e sobe de 1 em 1 a partir do último que passou.
                salto > 1 && mundo > aprovado + 1 ->
                    copy(mundo = aprovado + 1, salto = 1, limite = mundo, errosNoMundo = 0, perguntasNoMundo = 0, feitas = n)
                else -> copy(limite = minOf(limite, mundo), errosNoMundo = erros, feitas = n, terminou = true)
            }
        }
        return if (proximo.feitas >= MAXIMO) proximo.copy(terminou = true) else proximo
    }

    /** Texto da Corujinha no fim. */
    fun resultado(nome: String?): String {
        val m = Mundos.todos[mundoSugerido - 1]
        val voce = if (nome.isNullOrBlank()) "Você" else nome
        return if (liberarAte == 0) {
            "$voce foi muito bem! Vamos começar pelo mundo 1, ${m.nome}, e ir subindo juntos."
        } else {
            "Uau! $voce já sabe bastante! O caminho abriu até o mundo ${m.numero}, ${m.nome}. As estrelas e os presentes você ganha jogando cada fase."
        }
    }

    companion object {
        const val SALTO_INICIAL = 3
        const val MAXIMO = 10

        /** O estado depois das respostas [resultados] (acertou de primeira?), na ordem. */
        fun de(trilha: Trilha, resultados: List<Boolean>): Nivelamento =
            resultados.fold(Nivelamento(trilha)) { n, r -> n.responder(r) }
    }
}

/** Guarda o resultado do "Descobrir o nível": abre as fases até [Nivelamento.liberarAte] e marca o caminho como feito. */
fun EstadoJogo.aplicarNivel(n: Nivelamento): EstadoJogo {
    val atual = liberadaAte[n.trilha] ?: 0
    val ate = maxOf(atual, n.liberarAte)
    return copy(
        liberadaAte = if (ate > 0) liberadaAte + (n.trilha to ate) else liberadaAte,
        nivelFeito = nivelFeito + n.trilha,
    )
}

/** Os pais desfazem: o caminho volta a abrir só fase por fase (as fases vencidas continuam vencidas). */
fun EstadoJogo.desfazerNivel(trilha: Trilha): EstadoJogo =
    copy(liberadaAte = liberadaAte - trilha, nivelFeito = nivelFeito - trilha)

/** O caminho ainda pode oferecer o "Descobrir o nível" na primeira visita (nenhuma fase vencida e não fez ainda). */
fun EstadoJogo.ofereceNivel(trilha: Trilha): Boolean = trilha !in nivelFeito && vencidasNa(trilha) == 0

/** A fase da vez no caminho: a primeira aberta e não vencida, começando do mundo aberto pelo nível. */
fun EstadoJogo.faseDaVez(todas: List<Fase>): Fase? {
    val desde = todas.firstOrNull()?.let { liberadaAte[it.trilha] } ?: 0
    return todas.firstOrNull { it.numero >= desde && liberada(it) && estrelasDa(it) == 0 }
        ?: todas.firstOrNull { liberada(it) && estrelasDa(it) == 0 }
}

object SerializacaoDoNivel {
    fun liberadaParaTexto(m: Map<Trilha, Int>): String = m.entries.joinToString(";") { "${it.key.name}=${it.value}" }

    fun textoParaLiberada(s: String?): Map<Trilha, Int> = s.orEmpty().split(";").mapNotNull { parte ->
        val (k, v) = parte.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
        val t = Trilha.entries.firstOrNull { it.name == k } ?: return@mapNotNull null
        val n = v.toIntOrNull()?.takeIf { it in 1 until GeradorDeFases.FASES_POR_TRILHA } ?: return@mapNotNull null
        t to n
    }.toMap()

    fun feitoParaTexto(s: Set<Trilha>): String = s.joinToString(",") { it.name }

    fun textoParaFeito(s: String?): Set<Trilha> = s.orEmpty().split(",").mapNotNull { n -> Trilha.entries.firstOrNull { it.name == n.trim() } }.toSet()
}
