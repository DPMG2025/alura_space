package br.com.casinhadosaber.dominio

/**
 * Liga o Livro de Colorir (Fase 10) e os quebra-cabeças (Fase 11) ao [EstadoJogo]: guardar a obra, pagar as
 * moedinhas e lembrar das melhores estrelas. Nada aqui tira moeda, estrela ou desenho da criança.
 */

/** Guarda a obra do desenho (uma por desenho; pintar de novo no mesmo desenho continua a mesma folha). */
fun EstadoJogo.guardarObra(o: Obra): EstadoJogo =
    if (o.desenhoId !in Desenhos.porId || o.tintas.size != Desenhos.porId.getValue(o.desenhoId).regioes.size) this
    else copy(obrasDeColorir = obrasDeColorir + (o.desenhoId to o))

/** A obra guardada do desenho, ou uma folha em branco. */
fun EstadoJogo.obraDe(d: DesenhoDeColorir): Obra = obrasDeColorir[d.id] ?: Colorir.obraVazia(d)

/** Recomeça o desenho do zero (a criança pediu "pintar de novo"). As estrelas e moedinhas já ganhas ficam. */
fun EstadoJogo.recomecarObra(d: DesenhoDeColorir): EstadoJogo = copy(obrasDeColorir = obrasDeColorir - d.id)

/** Quantos desenhos já foram terminados (abre as cores especiais). */
val EstadoJogo.desenhosTerminados: Int get() = coloridos.size

/** Cores que a criança pode usar agora. */
val EstadoJogo.tintasLiberadas: List<Tinta> get() = Tintas.liberadas(desenhosTerminados)

/**
 * "Pronto!": guarda a obra e, se o desenho está terminado, paga o prêmio. Devolve o estado novo e o prêmio
 * (null se ainda não terminou). Terminar de novo só paga se melhorar as estrelas.
 */
fun EstadoJogo.terminarDesenho(d: DesenhoDeColorir, o: Obra, hoje: Long = -1): Pair<EstadoJogo, Colorir.Premio?> {
    val guardado = guardarObra(o)
    val base = Colorir.premio(d, o, coloridos) ?: return guardado to null
    // Pintor do dia: terminar o desenho sugerido de hoje paga moedinhas extras, uma vez por dia.
    val doDia = hoje >= 0 && d.id == Colorir.desenhoDoDia(hoje).id && diaDoPintor != hoje
    val p = if (doDia) base.copy(moedas = base.moedas + Colorir.MOEDAS_DO_PINTOR_DO_DIA, pintorDoDia = true) else base
    val melhores = if (p.estrelas > (coloridos[d.id] ?: 0)) coloridos + (d.id to p.estrelas) else coloridos
    return guardado.copy(moedas = moedas + p.moedas, coloridos = melhores, diaDoPintor = if (doDia) hoje else diaDoPintor) to p
}

/** Nível do quebra-cabeça que a criança pode escolher na coleção, no máximo. */
fun EstadoJogo.nivelDePuzzleAberto(colecao: String): Int = Puzzle.nivelAberto(colecao, puzzlesFeitos)

/**
 * Vitória no quebra-cabeça: paga o prêmio, guarda as melhores estrelas e conta as vitórias seguidas sem dica
 * (3 seguidas sobem o nível sugerido). [usouDica] zera a sequência.
 */
fun EstadoJogo.venceuPuzzle(desenhoId: String, nivel: Int, estrelas: Int, usouDica: Boolean): Pair<EstadoJogo, Puzzle.Premio?> {
    val seguidas = if (usouDica) 0 else (puzzleSeguidas + 1).coerceAtMost(99)
    val p = Puzzle.premio(desenhoId, nivel, estrelas, puzzlesFeitos)
        ?: return copy(puzzleSeguidas = seguidas) to null
    val k = Puzzle.chave(desenhoId, nivel)
    val melhores = if (estrelas > (puzzlesFeitos[k] ?: 0)) puzzlesFeitos + (k to estrelas) else puzzlesFeitos
    return copy(moedas = moedas + p.moedas, puzzlesFeitos = melhores, puzzleSeguidas = seguidas) to p
}

/** Peças douradas ganhas até agora. */
val EstadoJogo.pecasDouradas: Int get() = Puzzle.pecasDouradas(puzzlesFeitos)

/** O que a criança já fez no Livro de Colorir e nos quebra cabeças (para a área dos pais). */
data class ResumoDasCriacoes(
    val desenhosTerminados: Int,
    val desenhosNoLivro: Int,
    val estrelasDeColorir: Int,
    val quebraCabecasVencidos: Int,
    val pecasDouradas: Int,
    val selos: Int = 0,
    val selosTotal: Int = 0,
) {
    val comecou: Boolean get() = desenhosTerminados > 0 || quebraCabecasVencidos > 0 || estrelasDeColorir > 0
}

val EstadoJogo.resumoDasCriacoes: ResumoDasCriacoes
    get() = ResumoDasCriacoes(
        desenhosTerminados = coloridos.size,
        desenhosNoLivro = Desenhos.todos.size,
        estrelasDeColorir = coloridos.values.sum(),
        quebraCabecasVencidos = puzzlesFeitos.size,
        pecasDouradas = Puzzle.pecasDouradas(puzzlesFeitos),
        selos = selosDeCriacao.count { it.ganho },
        selosTotal = selosDeCriacao.size,
    )

/** Um selo das criações: um por coleção de desenhos terminada e um por marco de quebra cabeças. */
data class SeloDeCriacao(val id: String, val titulo: String, val emoji: String, val ganho: Boolean)

val EstadoJogo.selosDeCriacao: List<SeloDeCriacao>
    get() {
        val colecoes = Desenhos.colecoes.map { (id, nome) ->
            val dela = Desenhos.daColecao(id)
            SeloDeCriacao("colorir:$id", "Coleção " + nome.substringAfter(' '), "🏅", dela.isNotEmpty() && dela.all { it.id in coloridos })
        }
        val marcos = Puzzle.marcos.map { SeloDeCriacao("puzzle:${it.total}", it.nome, "🏆", puzzlesFeitos.size >= it.total) }
        return colecoes + marcos
    }
