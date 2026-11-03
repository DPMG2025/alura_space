package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.*
import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Fases do caminho Lógica: 250 fases em 25 mundos de 10 ([Mundos]).
 * As fases 1 a 50 são as de sempre (o progresso salvo continua valendo). As fases 51 a 250 (mundos 6
 * a 25) seguem o plano (docs/plano-1000-fases.md, seção 4.4), com o vocabulário em [VocabularioLogica].
 */
class FasesLogica(random: Random = Random.Default) : FasesDoCaminho(random) {

    override val trilha = Trilha.LOGICA

    override fun fase(numero: Int): ModeloDeFase {
        conferir(numero)
        return if (numero <= FASES_PRONTAS) ate50[numero - 1] else depois50[numero - FASES_PRONTAS - 1]
    }

    /** As 50 fases de antes, na ordem (a fase 1 é o índice 0). */
    private val ate50: List<ModeloDeFase> by lazy { logica + logicaMundos }

    private fun igual(): Pergunta {
        val (certa, e1, e2) = Vocabulario.figurasSimples.sorteia(3)
        val e3 = Vocabulario.figurasSimples.filter { it != certa && it != e1 && it != e2 }.um()
        return montar(
            instrucao = "Toque no igual",
            fala = listOf(pt("Toque na figura igual a esta.")),
            cena = Cena.Emojis(certa),
            certa = opEmoji(certa),
            erradas = listOf(opEmoji(e1), opEmoji(e2), opEmoji(e3)),
            reforco = listOf(pt("Igualzinho!")),
        )
    }

    private fun diferente(): Pergunta {
        val (comum, intruso) = Vocabulario.figurasSimples.sorteia(2)
        return montar(
            instrucao = "Qual é diferente?",
            fala = listOf(pt("Qual é diferente dos outros?")),
            cena = Cena.Nenhuma,
            certa = opEmoji(intruso),
            erradas = List(3) { opEmoji(comum) },
            reforco = listOf(pt("Esse é diferente!")),
        )
    }

    private fun tamanho(maior: Boolean): Pergunta {
        val e = Vocabulario.figurasSimples.um()
        val escalas = listOf(0.45f, 0.7f, 0.95f, 1.25f)
        val certa = if (maior) escalas.max() else escalas.min()
        return montar(
            instrucao = if (maior) "Qual é o maior?" else "Qual é o menor?",
            fala = listOf(pt(if (maior) "Qual é o maior de todos?" else "Qual é o menor de todos?")),
            cena = Cena.Nenhuma,
            certa = opEmoji(e, certa),
            erradas = escalas.filter { it != certa }.map { opEmoji(e, it) },
            reforco = listOf(pt(if (maior) "Esse é o maior!" else "Esse é o menor!")),
        )
    }

    private fun sequencia(padrao: String): Pergunta {
        val figuras = Vocabulario.figurasDeSequencia.sorteia(3)
        val (a, b, c) = figuras
        val base = when (padrao) {
            "AB" -> listOf(a, b)
            "ABC" -> listOf(a, b, c)
            "AAB" -> listOf(a, a, b)
            else -> listOf(a, b, b)
        }
        // O tamanho varia para a resposta não ser sempre a primeira figura do padrão.
        val tamanho = base.size * 2 + random.nextInt(1, base.size + 1)
        val completa = List(tamanho) { base[it % base.size] }
        val resposta = completa.last()
        val outras = (figuras + Vocabulario.figurasDeSequencia.filter { it !in figuras }.sorteia(2))
            .distinct()
            .filter { it != resposta }
            .take(3)
        return montar(
            instrucao = "O que vem depois?",
            fala = listOf(pt("Olhe a fila. Qual figura vem no lugar da interrogação?")),
            cena = Cena.Sequencia(completa.dropLast(1) + "?"),
            certa = opEmoji(resposta),
            erradas = outras.map { opEmoji(it) },
            reforco = listOf(pt("Isso! A fila continua igual.")),
        )
    }

    private fun combina(): Pergunta {
        val par = Vocabulario.pares.um()
        val erradas = Vocabulario.pares.filter { it != par }.sorteia(3)
        return montar(
            instrucao = "O que combina?",
            fala = listOf(pt("O que combina com ${par.nomeDe}?")),
            cena = Cena.Emojis(par.de),
            certa = Opcao(Visual.Emoji(par.para), listOf(pt(par.nomePara))),
            erradas = erradas.map { Opcao(Visual.Emoji(it.para), listOf(pt(it.nomePara))) },
            reforco = listOf(pt("${par.nomeDe} combina com ${par.nomePara}!")),
        )
    }

    private fun numerosEmOrdem(): Pergunta {
        val crescente = random.nextInt(3) != 0
        val inicio = if (crescente) (1..6).random(random) else (4..9).random(random)
        val passo = if (crescente) 1 else -1
        val fila = List(4) { inicio + it * passo }
        val resposta = fila.last()
        return montar(
            instrucao = "Que número vem depois?",
            fala = listOf(pt(fila.dropLast(1).joinToString(", ") { porExtenso(it) } + ". Que número vem depois?")),
            cena = Cena.Sequencia(fila.dropLast(1).map { it.toString() } + "?"),
            certa = opcaoNumero(resposta),
            erradas = numerosErrados(resposta, 0, 10, 3).map(::opcaoNumero),
            reforco = listOf(pt(fila.joinToString(", ") { porExtenso(it) } + "!")),
        )
    }

    private val logica: List<ModeloDeFase> = listOf(
        ModeloDeFase("Igualzinho", "👯") { igual() },
        ModeloDeFase("O diferente", "🔍") { diferente() },
        ModeloDeFase("Memória fácil", "🃏") { memoria(2) },
        ModeloDeFase("Grande e pequeno", "🐘") { tamanho(maior = random.nextBoolean()) },
        ModeloDeFase("Filas de 2", "🔴") { sequencia("AB") },
        ModeloDeFase("Sombras", "🌑") { sombra() },
        ModeloDeFase("O que combina", "🔑") { combina() },
        ModeloDeFase("Cesta das coisas", "🧺") { cestaDoGrupo() },
        ModeloDeFase("Memória", "🃏") { memoria(3) },
        ModeloDeFase("Cada um no seu grupo", "🗂️") { grupo() },
        ModeloDeFase("Filas de 3", "⭐") { sequencia("ABC") },
        ModeloDeFase("Sombras difíceis", "🌚") { sombra() },
        ModeloDeFase("Filas espertas", "🧠") { sequencia(if (random.nextBoolean()) "AAB" else "ABB") },
        ModeloDeFase("Números em fila", "🔢") { numerosEmOrdem() },
        ModeloDeFase("Memória grande", "🃏") { memoria(4) },
        ModeloDeFase("Separar na cesta", "🧺") { cestaDoGrupo() },
        ModeloDeFase("Combina e grupos", "🔗") { when (random.nextInt(2)) { 0 -> combina(); else -> grupo() } },
        ModeloDeFase("Tamanhos e sombras", "🔦") { when (random.nextInt(2)) { 0 -> tamanho(random.nextBoolean()); else -> sombra() } },
        ModeloDeFase("Filas misturadas", "🎨") { when (random.nextInt(4)) { 0 -> sequencia("AB"); 1 -> sequencia("ABC"); 2 -> sequencia("AAB"); else -> sequencia("ABB") } },
        ModeloDeFase("Memória gigante", "🃏") { memoria(5) },
        ModeloDeFase("Detetive", "🕵️") { when (random.nextInt(3)) { 0 -> diferente(); 1 -> sombra(); else -> combina() } },
        ModeloDeFase("Pensa rápido", "⚡") { when (random.nextInt(3)) { 0 -> igual(); 1 -> grupo(); else -> numerosEmOrdem() } },
        ModeloDeFase("Cesta e memória", "🧩") { when (random.nextInt(2)) { 0 -> cestaDoGrupo(); else -> memoria(4) } },
        ModeloDeFase("Desafio da lógica", "🏆") { when (random.nextInt(5)) { 0 -> sequencia(listOf("ABC", "AAB", "ABB").um()); 1 -> combina(); 2 -> grupo(); 3 -> numerosEmOrdem(); else -> sombra() } },
        ModeloDeFase("Supercampeão da lógica", "👑") { when (random.nextInt(5)) { 0 -> memoria(5); 1 -> sequencia("ABC"); 2 -> cestaDoGrupo(); 3 -> sombra(); else -> diferente() } },
    )

    /** Classificar: só as figuras do grupo entram na cesta. */
    private fun cestaDoGrupo(): Pergunta {
        val g = Vocabulario.grupos.um()
        val certos = g.itens.sorteia(3)
        val errados = Vocabulario.grupos.filter { it != g }.flatMap { it.itens }.sorteia(3)
        val nome = g.pergunta.removePrefix("Qual destes é ").removeSuffix("?")
        return Pergunta.deAtividade(
            instrucao = "Só $nome na cesta",
            fala = listOf(pt("Coloque na cesta só o que é $nome.")),
            atividade = Atividade.Cesta(certos, certos.size, errados),
            reforco = listOf(pt("Muito bem! Tudo certinho na cesta.")),
        )
    }

    /** Jogo das sombras: qual silhueta é desta figura? */
    private fun sombra(): Pergunta {
        val (certa, e1, e2, e3) = (Vocabulario.animais.map { it.emoji } + Vocabulario.brinquedos.map { it.emoji }).distinct().sorteia(4)
        val op = { e: String -> Opcao(Visual.Sombra(e)) }
        return montar(
            instrucao = "Qual é a sombra?",
            fala = listOf(pt("Qual é a sombra desta figura?")),
            cena = Cena.Emojis(certa),
            certa = op(certa),
            erradas = listOf(op(e1), op(e2), op(e3)),
            reforco = listOf(pt("Isso! É a sombra dela!")),
        )
    }

    // ------------------------------------------------------------------ LÓGICA

    /** Fila com qualquer padrão de letras (ABCD, AABB, ABCC...). */
    private fun filaComPadrao(padrao: String): Pergunta {
        val distintas = padrao.toSet().size
        val figuras = Vocabulario.figurasDeSequencia.sorteia(distintas)
        val base = padrao.map { figuras[it - 'A'] }
        val tamanho = base.size * 2 + random.nextInt(1, base.size + 1)
        val completa = List(tamanho) { base[it % base.size] }
        val resposta = completa.last()
        val outras = (figuras + Vocabulario.figurasDeSequencia.filter { it !in figuras }.sorteia(3)).distinct().filter { it != resposta }.take(3)
        return montar(
            instrucao = "O que vem depois?",
            fala = listOf(pt("Olhe a fila comprida. Qual figura vem no lugar da interrogação?")),
            cena = Cena.Sequencia(completa.dropLast(1) + "?"),
            certa = opEmoji(resposta),
            erradas = outras.map { opEmoji(it) },
            reforco = listOf(pt("Isso! A fila continua igualzinha.")),
        )
    }

    /** Qual não é da turma? Três do mesmo grupo e um intruso. */
    private fun intruso(): Pergunta {
        val (nome, itens) = V.turmas.um()
        val certos = itens.sorteia(3)
        val intruso = V.turmas.filter { it.first != nome }.um().second.filter { it !in itens }.um()
        return montar(
            instrucao = "Qual não é da turma?",
            fala = listOf(pt("Três são da mesma turma. Qual não combina com os outros?")),
            cena = Cena.Nenhuma,
            certa = opEmoji(intruso),
            erradas = certos.map { opEmoji(it) },
            reforco = listOf(pt("Isso! Os outros são $nome.")),
        )
    }

    /** Intruso esperto: três são "frutas vermelhas" e um é fruta de outra cor ou coisa vermelha. */
    private fun intrusoEsperto(): Pergunta {
        val c = V.criteriosDuplos.um()
        val certos = c.certos.sorteia(3)
        val intruso = c.quaseCertos.um()
        return montar(
            instrucao = "Qual não é da turma?",
            fala = listOf(pt("Pense bem! Três são ${c.nome}. Qual não é?")),
            cena = Cena.Nenhuma,
            certa = opEmoji(intruso),
            erradas = certos.map { opEmoji(it) },
            reforco = listOf(pt("Muito esperto! Os outros são ${c.nome}.")),
        )
    }

    /** Classificação dupla: cor e forma ao mesmo tempo ("o quadrado azul"). */
    private fun corEForma(dificil: Boolean): Pergunta {
        val cores = V.coresEFormas.sorteia(if (dificil) 3 else 2)
        val alvo = cores[0]
        val circulo = random.nextBoolean()
        val certa = if (circulo) alvo.circulo else alvo.quadrado
        val erradas = if (dificil) {
            listOf(if (circulo) alvo.quadrado else alvo.circulo, if (circulo) cores[1].circulo else cores[1].quadrado, if (circulo) cores[2].quadrado else cores[2].circulo)
        } else {
            listOf(if (circulo) alvo.quadrado else alvo.circulo, cores[1].circulo, cores[1].quadrado)
        }
        val nome = "${if (circulo) "círculo" else "quadrado"} ${alvo.nomeCor}"
        return montar(
            instrucao = "Toque no $nome",
            fala = listOf(pt("Preste atenção na cor e na forma. Toque no $nome.")),
            cena = Cena.Nenhuma,
            certa = opEmoji(certa),
            erradas = erradas.map { opEmoji(it) },
            reforco = listOf(pt("Isso! ${nome.replaceFirstChar { it.uppercase() }}!")),
        )
    }

    private fun cestaDupla(): Pergunta {
        val c = V.criteriosDuplos.um()
        val certos = c.certos.sorteia(3)
        val errados = c.quaseCertos.sorteia(3)
        return Pergunta.deAtividade(
            instrucao = "Só ${c.nome} na cesta",
            fala = listOf(pt("Pense bem! Coloque na cesta só ${c.nome}.")),
            atividade = Atividade.Cesta(certos, certos.size, errados),
            reforco = listOf(pt("Muito bem! Só ${c.nome} na cesta.")),
        )
    }
    private enum class Direcao(val dl: Int, val dc: Int, val seta: DirecaoDaSeta, val nome: String) {
        CIMA(-1, 0, DirecaoDaSeta.CIMA, "para cima"), BAIXO(1, 0, DirecaoDaSeta.BAIXO, "para baixo"),
        ESQUERDA(0, -1, DirecaoDaSeta.ESQUERDA, "para a esquerda"), DIREITA(0, 1, DirecaoDaSeta.DIREITA, "para a direita"),
    }

    /**
     * Caminho do labirinto: casinhas vizinhas, sem encostar em si mesmo (sem atalhos), da criança até a casinha.
     * O quadro é recortado em volta do caminho e, entre muitos sorteios, fica o mais cheio: o caminho ocupa
     * o quadro inteiro, sem fileiras só de árvores. Devolve (linhas, colunas, caminho em casinhas (linha, coluna)).
     */
    private fun caminhoDoLabirinto(maxLinhas: Int, maxColunas: Int, passos: Int): Triple<Int, Int, List<Pair<Int, Int>>> {
        var melhor: Triple<Int, Int, List<Pair<Int, Int>>>? = null
        var melhorNota = -1f
        repeat(400) {
            val caminho = mutableListOf(random.nextInt(maxLinhas) to random.nextInt(maxColunas))
            while (caminho.size <= passos) {
                val (l, c) = caminho.last()
                val proximas = Direcao.entries.map { (l + it.dl) to (c + it.dc) }.filter { (nl, nc) ->
                    nl in 0 until maxLinhas && nc in 0 until maxColunas && (nl to nc) !in caminho &&
                        // A casinha nova só pode encostar na de antes (assim o caminho é um só, sem atalho).
                        Direcao.entries.count { d -> (nl + d.dl to nc + d.dc) in caminho } == 1
                }
                if (proximas.isEmpty()) break
                caminho += proximas.um()
            }
            if (caminho.size != passos + 1) return@repeat
            val l0 = caminho.minOf { it.first }
            val c0 = caminho.minOf { it.second }
            val linhas = caminho.maxOf { it.first } - l0 + 1
            val colunas = caminho.maxOf { it.second } - c0 + 1
            if (linhas < 3 || colunas < 3) return@repeat
            // Nota: quanto mais cheio de caminho, melhor; e um pouco de preferência por quadros "deitados" ou quadrados.
            val nota = caminho.size.toFloat() / (linhas * colunas) - (if (linhas > colunas) 0.05f else 0f)
            if (nota > melhorNota) {
                melhorNota = nota
                melhor = Triple(linhas, colunas, caminho.map { (l, c) -> (l - l0) to (c - c0) })
            }
        }
        // Reserva (nunca deve acontecer): um zigue-zague de 3 por 3.
        return melhor ?: Triple(3, 3, listOf(0 to 0, 0 to 1, 0 to 2, 1 to 2, 2 to 2, 2 to 1, 2 to 0))
    }

    private fun direcaoEntre(a: Pair<Int, Int>, b: Pair<Int, Int>) =
        Direcao.entries.first { a.first + it.dl == b.first && a.second + it.dc == b.second }

    /** Labirinto: para que lado a criança vai, ou quantos passos até a casinha. */
    private fun labirinto(grande: Boolean): Pergunta {
        val passos = if (grande) (8..11).random(random) else (5..7).random(random)
        val (linhas, colunas, caminho) = caminhoDoLabirinto(if (grande) 5 else 4, if (grande) 5 else 4, passos)
        val cena = Cena.Labirinto(linhas, colunas, caminho.map { (l, c) -> l * colunas + c })
        val tipo = random.nextInt(if (grande) 3 else 2)
        if (tipo == 1) {
            // Uma pedrinha em cada casinha do caminho, entre o começo e a chegada.
            val n = caminho.size - 2
            return montar(
                instrucao = "Quantas pedrinhas tem no caminho?",
                fala = listOf(pt("Você vai andar até a casinha pisando nas pedrinhas. Conte as pedrinhas do caminho, uma por uma. Quantas são?")),
                cena = cena,
                certa = opcaoNumero(n),
                erradas = erradosDe(n, 2..12).map(::opcaoNumero),
                reforco = listOf(pt("${porExtenso(n, true)} pedrinhas até a casinha!")),
            )
        }
        val primeiro = tipo == 0
        val d = if (primeiro) direcaoEntre(caminho[0], caminho[1]) else direcaoEntre(caminho[caminho.size - 2], caminho.last())
        val op = { x: Direcao -> Opcao(Visual.Seta(x.seta), listOf(pt(x.nome))) }
        return montar(
            instrucao = if (primeiro) "Para que lado você vai primeiro?" else "Qual é o último passo?",
            fala = listOf(
                pt(
                    if (primeiro) "Você quer chegar na casinha pelo caminho de pedrinhas. Para que lado você vai primeiro?"
                    else "Você está quase chegando na casinha. Para que lado é o último passo?",
                ),
            ),
            cena = cena,
            certa = op(d),
            erradas = Direcao.entries.filter { it != d }.map(op),
            reforco = listOf(pt("Isso! ${d.nome.replaceFirstChar { it.uppercase() }}!")),
        )
    }

    /** Sudoku de figuras 4 por 4: cada linha, coluna e quadradinho de 2 por 2 tem uma figura de cada. */
    private fun sudoku(escondidas: Int): Pergunta {
        val figuras = Vocabulario.figurasDeSequencia.take(6).sorteia(4)
        // Base válida; trocar linhas dentro da faixa, colunas dentro da pilha e as faixas mantém as regras.
        val base = listOf(listOf(0, 1, 2, 3), listOf(2, 3, 0, 1), listOf(1, 0, 3, 2), listOf(3, 2, 1, 0))
        val ordemLinhas = listOf(listOf(0, 1), listOf(2, 3)).shuffled(random).flatMap { it.shuffled(random) }
        val ordemColunas = listOf(listOf(0, 1), listOf(2, 3)).shuffled(random).flatMap { it.shuffled(random) }
        val quadro = ordemLinhas.map { l -> ordemColunas.map { c -> figuras[base[l][c]] } }
        val celulas = (0 until 16).toList()
        var pergunta = celulas.um()
        var vazias = emptySet<Int>()
        // Esconde outras casinhas, mas só aceita quando a interrogação continua com uma resposta só.
        for (tentativa in 0 until 200) {
            val p = celulas.um()
            val v = (celulas - p).sorteia(escondidas).toSet()
            val (l, c) = p / 4 to p % 4
            val vizinhas = celulas.filter { i ->
                i != p && i !in v && (i / 4 == l || i % 4 == c || (i / 4 / 2 == l / 2 && i % 4 / 2 == c / 2))
            }
            if (vizinhas.map { quadro[it / 4][it % 4] }.toSet().size == 3) {
                pergunta = p
                vazias = v
                break
            }
        }
        val resposta = quadro[pergunta / 4][pergunta % 4]
        // Só a casinha da pergunta fica para descobrir; as outras escondidas viram tampinhas fechadas.
        val itens = celulas.map { i -> if (i == pergunta) "?" else if (i in vazias) Cena.Grade.TAMPADA else quadro[i / 4][i % 4] }
        val tampas = if (vazias.isEmpty()) "" else " As tampinhas fechadas guardam um segredo: não precisa delas."
        return montar(
            instrucao = "Qual figura vai na casinha amarela?",
            fala = listOf(
                pt(
                    "Olhe a casinha amarela. Na linha dela, na coluna dela e no quadrado dela, cada figura aparece uma vez só.$tampas" +
                        " Qual figura vai na casinha amarela?",
                ),
            ),
            cena = Cena.Grade(4, itens, blocos = 2),
            certa = opEmoji(resposta),
            erradas = figuras.filter { it != resposta }.map { opEmoji(it) },
            reforco = listOf(pt("Isso! Agora não repete nenhuma figura.")),
        )
    }

    private val logicaMundos: List<ModeloDeFase> = listOf(
        // Mundo 3 (Praia), fases 26 a 30
        ModeloDeFase("Filas de 4", "🔷") { filaComPadrao(listOf("ABCD", "AABB", "ABCC").um()) },
        ModeloDeFase("Quem não é da turma?", "🕵️") { intruso() },
        ModeloDeFase("Cor e forma", "🟦") { corEForma(dificil = false) },
        ModeloDeFase("Memória de 6", "🃏") { memoria(6) },
        ModeloDeFase("Desafio da praia", "🏆") { when (random.nextInt(4)) { 0 -> filaComPadrao("ABCD"); 1 -> intruso(); 2 -> corEForma(false); else -> memoria(5) } },
        // Mundo 4 (Montanha), fases 31 a 40
        ModeloDeFase("Labirinto", "🐭") { labirinto(grande = false) },
        ModeloDeFase("Sudoku fácil", "🧩") { sudoku(escondidas = 0) },
        ModeloDeFase("Pulando números", "🦘") { sequenciaNumerica(30) },
        ModeloDeFase("Cesta esperta", "🧺") { cestaDupla() },
        ModeloDeFase("Intrusos espertos", "🔎") { intrusoEsperto() },
        ModeloDeFase("Labirinto dos passos", "👣") { labirinto(grande = random.nextBoolean()) },
        ModeloDeFase("Sudoku", "🧩") { sudoku(escondidas = 2) },
        ModeloDeFase("Cor e forma difícil", "🎨") { corEForma(dificil = true) },
        ModeloDeFase("Combina e sombras", "🔦") { when (random.nextInt(3)) { 0 -> combina(); 1 -> sombra(); else -> intruso() } },
        ModeloDeFase("Desafio da montanha", "🏆") { when (random.nextInt(5)) { 0 -> labirinto(false); 1 -> sudoku(1); 2 -> cestaDupla(); 3 -> intrusoEsperto(); else -> filaComPadrao("AABB") } },
        // Mundo 5 (Espaço), fases 41 a 50
        ModeloDeFase("Filas gigantes", "🎨") { filaComPadrao(listOf("ABCD", "AABC", "ABBC", "AABBC").um()) },
        ModeloDeFase("Labirinto grande", "🌀") { labirinto(grande = true) },
        ModeloDeFase("Sudoku difícil", "🧠") { sudoku(escondidas = 4) },
        ModeloDeFase("Memória gigante de 6", "🃏") { memoria(6, Vocabulario.animais + V.animaisSelvagens) },
        ModeloDeFase("Cesta dupla", "🧺") { if (random.nextBoolean()) cestaDupla() else cestaDoGrupo() },
        ModeloDeFase("Detetive do espaço", "🔭") { when (random.nextInt(3)) { 0 -> intrusoEsperto(); 1 -> sombra(); else -> corEForma(true) } },
        ModeloDeFase("Números e figuras", "🔢") { if (random.nextBoolean()) sequenciaNumerica(50) else filaComPadrao("ABCD") },
        ModeloDeFase("Pensa rápido 2", "⚡") { when (random.nextInt(4)) { 0 -> intruso(); 1 -> corEForma(true); 2 -> labirinto(false); else -> numerosEmOrdem() } },
        ModeloDeFase("Grande desafio", "🏅") { when (random.nextInt(4)) { 0 -> sudoku(3); 1 -> labirinto(true); 2 -> filaComPadrao("AABBC"); else -> intrusoEsperto() } },
        ModeloDeFase("Mestre da lógica", "🌟") { when (random.nextInt(6)) { 0 -> sudoku(4); 1 -> labirinto(true); 2 -> memoria(6); 3 -> cestaDupla(); 4 -> corEForma(true); else -> filaComPadrao(listOf("ABCD", "AABBC").um()) } },
    )

    // ================================================================== MUNDOS 6 A 25 (fases 51 a 250)

    private val VL = VocabularioLogica

    /** As fases 51 a 250, na ordem (a fase 51 é o índice 0). */
    private val depois50: List<ModeloDeFase> by lazy {
        (6..25).flatMap { m -> mundoNovo(m).also { check(it.size == 10) { "Mundo $m sem 10 fases" } } }
    }

    /** Uma fase que sorteia, a cada pergunta, um dos [geradores] (repetir um gerador dá mais peso a ele). */
    private fun f(titulo: String, emoji: String, vararg geradores: () -> Pergunta) =
        ModeloDeFase(titulo, emoji) { geradores.toList().um()() }

    /** Pergunta de tocar com 4 opções distintas, conferidas aqui. */
    private fun tocar(instrucao: String, fala: String, cena: Cena, certa: Opcao, erradas: List<Opcao>, reforco: String): Pergunta {
        require(erradas.size == 3 && (erradas + certa).map { it.visual }.toSet().size == 4) { "Opções repetidas em $instrucao" }
        return montar(instrucao, listOf(pt(fala)), cena, certa, erradas, listOf(pt(reforco)))
    }

    private fun mexer(instrucao: String, fala: String, atividade: Atividade, reforco: String, cena: Cena = Cena.Nenhuma) =
        Pergunta.deAtividade(instrucao, listOf(pt(fala)), atividade, listOf(pt(reforco)), cena)

    /** As 3 primeiras de [candidatos] diferentes de [certa] e entre si (a ordem diz a preferência). */
    private fun <T> tresErradas(certa: T, candidatos: List<T>): List<T> {
        val r = candidatos.filter { it != certa }.distinct().take(3)
        require(r.size == 3) { "Poucas opções erradas" }
        return r
    }

    private fun opNome(emoji: String, nome: String) = Opcao(Visual.Emoji(emoji), if (nome.isBlank()) emptyList() else listOf(pt(nome)))
    private fun opTema(t: VocabularioLogica.Tema, e: String) = opNome(e, t.nomeDe(e))
    private fun opFigura(forma: Int, cor: Int, escala: Float = 1f) =
        Opcao(Visual.Emoji(VL.figura(forma, cor), escala), listOf(pt("${VL.formas[forma]} ${VL.cores[cor]}")))

    private fun mmc(a: Int, b: Int): Int {
        var x = a
        while (x % b != 0) x += a
        return x
    }

    // ------------------------------------------------------------------ filas (padrões)

    /** Fila com duas regras ao mesmo tempo: a cor segue um padrão e a forma segue outro. */
    private fun filaDuasRegras(dificil: Boolean): Pergunta {
        val (pc, pf) = if (dificil) listOf("ABC" to "AB", "AB" to "ABC", "ABB" to "AB", "AB" to "AAB", "AAB" to "ABB").um()
        else listOf("AB" to "AABB", "AABB" to "AB").um()
        val cores = (0 until 6).toList().sorteia(pc.toSet().size)
        val formas = (0 until 3).toList().sorteia(pf.toSet().size)
        val periodo = mmc(pc.length, pf.length)
        val mostrar = periodo + random.nextInt(1, minOf(periodo, 4) + 1)
        fun item(i: Int) = formas[pf[i % pf.length] - 'A'] to cores[pc[i % pc.length] - 'A']
        val (fr, cr) = item(mostrar)
        val outrasCores = cores.filter { it != cr } + (0 until 6).filter { it !in cores }.shuffled(random)
        val outrasFormas = formas.filter { it != fr } + (0 until 3).filter { it !in formas }.shuffled(random)
        val candidatos = listOf(fr to outrasCores[0], outrasFormas[0] to cr, outrasFormas[0] to outrasCores[0], fr to outrasCores[1], outrasFormas[1 % outrasFormas.size] to cr)
        return tocar(
            instrucao = "O que vem depois?",
            fala = "Olhe a cor e olhe a forma: cada uma muda do seu jeito. Qual figura vem no lugar da interrogação?",
            cena = Cena.Sequencia(List(mostrar) { item(it).let { (f, c) -> VL.figura(f, c) } } + "?"),
            certa = opFigura(fr, cr),
            erradas = tresErradas(fr to cr, candidatos).map { (f, c) -> opFigura(f, c) },
            reforco = "Isso! A cor e a forma seguem as duas regras.",
        )
    }

    /** Fila que cresce: cada grupo tem mais figuras que o anterior, ou a figura B aparece uma vez a mais. */
    private fun filaCrescente(mundo: Int, dificil: Boolean): Pergunta {
        val t = VL.tema(mundo)
        if (!dificil || random.nextBoolean()) {
            val e = t.emojis.um()
            val passo = if (dificil && random.nextBoolean()) 2 else 1
            val inicio = random.nextInt(1, 3)
            val fila = List(4) { inicio + it * passo }
            val resposta = fila.last()
            val candidatos = listOf(resposta + 1, resposta - 1, resposta + passo + 1, resposta - passo, resposta + 2, resposta - 2).filter { it in 1..10 }
            val op = { n: Int -> Opcao(Visual.Emoji(e.repeat(n)), listOf(pt(porExtenso(n, true)))) }
            return tocar(
                instrucao = "Quantas vêm depois?",
                fala = "Cada grupo tem mais figuras que o grupo de antes. Quantas figuras vêm no próximo grupo?",
                cena = Cena.Sequencia(fila.dropLast(1).map { e.repeat(it) } + "?"),
                certa = op(resposta),
                erradas = tresErradas(resposta, candidatos).map(op),
                reforco = "Isso! A fila cresce de ${porExtenso(passo)} em ${porExtenso(passo)}.",
            )
        }
        // A, B, A, B, B, A, B, B, B...: a figura B aparece uma vez a mais em cada volta.
        val (a, b) = t.emojis.sorteia(2)
        val completa = (1..5).flatMap { k -> listOf(a) + List(k) { b } }
        val corte = random.nextInt(7, 15)
        val resposta = completa[corte]
        val outras = t.emojis.filter { it != a && it != b }.sorteia(2)
        return tocar(
            instrucao = "O que vem depois?",
            fala = "Esta fila cresce: a segunda figura aparece uma vez a mais em cada volta. Qual vem no lugar da interrogação?",
            cena = Cena.Sequencia(completa.take(corte) + "?"),
            certa = opTema(t, resposta),
            erradas = tresErradas(resposta, listOf(a, b) + outras).map { opTema(t, it) },
            reforco = "Muito bem! A fila cresce um pouquinho a cada volta.",
        )
    }

    // ------------------------------------------------------------------ quadros (matriz, espelho, sudoku)

    /** Quadro em que todas as linhas são iguais (ou todas as colunas): qual figura falta? */
    private fun quadroQueFalta(mundo: Int, lado: Int): Pergunta {
        val t = VL.tema(mundo)
        val figuras = t.emojis.sorteia(lado)
        val porLinha = random.nextBoolean()
        val itens = List(lado * lado) { i -> if (porLinha) figuras[i % lado] else figuras[i / lado] }
        val p = itens.indices.toList().um()
        val resposta = itens[p]
        return tocar(
            instrucao = "Qual figura falta no quadro?",
            fala = "Olhe o quadro: ${if (porLinha) "todas as linhas são iguais" else "todas as colunas são iguais"}. Qual figura vai na casinha amarela?",
            cena = Cena.Grade(lado, itens.mapIndexed { i, s -> if (i == p) "?" else s }),
            certa = opTema(t, resposta),
            erradas = tresErradas(resposta, figuras.shuffled(random) + t.emojis.shuffled(random)).map { opTema(t, it) },
            reforco = "Isso! Agora o quadro está completo.",
        )
    }

    /** Matriz de 3 por 3: cada linha tem uma forma e cada coluna uma cor (ou uma quantidade e uma figura). */
    private fun matriz(mundo: Int, quantidade: Boolean = random.nextBoolean()): Pergunta {
        val p = random.nextInt(9)
        val (l, c) = p / 3 to p % 3
        if (quantidade) {
            val t = VL.tema(mundo)
            val figuras = t.emojis.sorteia(4)
            val itens = List(9) { i -> figuras[i % 3].repeat(i / 3 + 1) }
            val resposta = itens[p]
            val candidatos = listOf(figuras[c].repeat((l + 1) % 3 + 1), figuras[(c + 1) % 3].repeat(l + 1), figuras[c].repeat((l + 2) % 3 + 1), figuras[3].repeat(l + 1))
            return tocar(
                instrucao = "O que falta no quadro?",
                fala = "Em cada coluna a figura é a mesma, e em cada linha a quantidade é a mesma. O que vai na casinha amarela?",
                cena = Cena.Grade(3, itens.mapIndexed { i, s -> if (i == p) "?" else s }),
                certa = Opcao(Visual.Emoji(resposta)),
                erradas = tresErradas(resposta, candidatos).map { Opcao(Visual.Emoji(it)) },
                reforco = "Isso! A figura da coluna, na quantidade da linha.",
            )
        }
        val formas = (0 until 3).toList().shuffled(random)
        val cores = (0 until 6).toList().sorteia(4)
        val itens = List(9) { i -> VL.figura(formas[i / 3], cores[i % 3]) }
        val certo = formas[l] to cores[c]
        val candidatos = listOf(formas[l] to cores[(c + 1) % 3], formas[(l + 1) % 3] to cores[c], formas[l] to cores[3], formas[(l + 2) % 3] to cores[(c + 2) % 3])
        return tocar(
            instrucao = "O que falta no quadro?",
            fala = "Em cada linha a forma é a mesma, e em cada coluna a cor é a mesma. O que vai na casinha amarela?",
            cena = Cena.Grade(3, itens.mapIndexed { i, s -> if (i == p) "?" else s }),
            certa = opFigura(certo.first, certo.second),
            erradas = tresErradas(certo, candidatos.shuffled(random)).map { (fo, co) -> opFigura(fo, co) },
            reforco = "Isso! ${VL.formas[certo.first].replaceFirstChar { it.uppercase() }} ${VL.cores[certo.second]}!",
        )
    }

    /** Quadro espelhado: o lado direito é o espelho do esquerdo. Qual figura completa o espelho? */
    private fun espelhoGrade(mundo: Int, linhas: Int, colunas: Int = 4): Pergunta {
        val t = VL.tema(mundo)
        val usadas = t.emojis.sorteia(3)
        val metade = colunas / 2
        val esquerda = List(linhas * metade) { usadas.um() }
        val itens = List(linhas * colunas) { i ->
            val l = i / colunas
            val c = i % colunas
            if (c < metade) esquerda[l * metade + c] else esquerda[l * metade + (colunas - 1 - c)]
        }
        val p = (0 until linhas * colunas).filter { it % colunas >= metade }.um()
        val resposta = itens[p]
        return tocar(
            instrucao = "Qual figura completa o espelho?",
            fala = "O lado direito do quadro é o espelho do lado esquerdo, como quando você se olha no espelho. Qual figura vai na casinha amarela?",
            cena = Cena.Grade(colunas, itens.mapIndexed { i, s -> if (i == p) "?" else s }),
            certa = opTema(t, resposta),
            erradas = tresErradas(resposta, usadas.shuffled(random) + t.emojis.shuffled(random)).map { opTema(t, it) },
            reforco = "Isso! Igualzinho ao espelho.",
        )
    }

    /** Quantas soluções tem o quadro (-1 é casinha escondida), parando em [limite]. [bloco] 0 = sem quadradinhos. */
    private fun solucoes(grade: IntArray, n: Int, bloco: Int, limite: Int = 2): Int {
        val i = grade.indexOf(-1)
        if (i < 0) return 1
        val l = i / n
        val c = i % n
        var total = 0
        for (v in 0 until n) {
            val ok = (0 until n).none { grade[l * n + it] == v || grade[it * n + c] == v } &&
                (bloco == 0 || (0 until n).none { j -> j / n / bloco == l / bloco && j % n / bloco == c / bloco && grade[j] == v })
            if (!ok) continue
            grade[i] = v
            total += solucoes(grade, n, bloco, limite - total)
            grade[i] = -1
            if (total >= limite) break
        }
        return total
    }

    /**
     * Sudoku de figuras com uma solução só: 4 por 4 (linhas, colunas e quadradinhos de 2 por 2) ou 6 por 6
     * (só linhas e colunas). [vazias] casinhas brancas, além da amarela da pergunta, também escondem figuras.
     */
    private fun sudokuUnico(lado: Int, vazias: Int): Pergunta {
        val figuras = if (lado == 4) Vocabulario.figurasDeSequencia.take(6).sorteia(4) else Vocabulario.figurasDeSequencia.sorteia(6)
        val completo: IntArray = if (lado == 4) {
            val base = listOf(listOf(0, 1, 2, 3), listOf(2, 3, 0, 1), listOf(1, 0, 3, 2), listOf(3, 2, 1, 0))
            val ordemLinhas = listOf(listOf(0, 1), listOf(2, 3)).shuffled(random).flatMap { it.shuffled(random) }
            val ordemColunas = listOf(listOf(0, 1), listOf(2, 3)).shuffled(random).flatMap { it.shuffled(random) }
            IntArray(16) { base[ordemLinhas[it / 4]][ordemColunas[it % 4]] }
        } else {
            val linhas = (0 until 6).shuffled(random)
            val colunas = (0 until 6).shuffled(random)
            IntArray(36) { (linhas[it / 6] + colunas[it % 6]) % 6 }
        }
        val bloco = if (lado == 4) 2 else 0
        val grade = completo.copyOf()
        val escondidas = mutableListOf<Int>()
        for (i in grade.indices.shuffled(random)) {
            if (escondidas.size == vazias + 1) break
            // Toda figura continua aparecendo pelo menos uma vez (a criança precisa ver todas).
            if (grade.count { it == completo[i] } == 1) continue
            grade[i] = -1
            if (solucoes(grade.copyOf(), lado, bloco) == 1) escondidas += i else grade[i] = completo[i]
        }
        val p = escondidas.um()
        val resposta = figuras[completo[p]]
        val itens = grade.indices.map { i -> if (i == p) "?" else if (grade[i] < 0) "" else figuras[grade[i]] }
        val regra = if (lado == 4) "Em cada linha, em cada coluna e em cada quadrado" else "Em cada linha e em cada coluna"
        return tocar(
            instrucao = "Qual figura vai na casinha amarela?",
            fala = "$regra, cada figura aparece uma vez só." + (if (vazias > 0) " As casinhas brancas também escondem figuras: pense nelas também." else "") +
                " Qual figura vai na casinha amarela?",
            cena = Cena.Grade(lado, itens, blocos = bloco),
            certa = opEmoji(resposta),
            erradas = tresErradas(resposta, figuras.shuffled(random)).map { opEmoji(it) },
            reforco = "Isso! Nenhuma figura repete.",
        )
    }

    // ------------------------------------------------------------------ setas, lados e labirintos

    private val setaEmoji = mapOf(Direcao.CIMA to "⬆️", Direcao.BAIXO to "⬇️", Direcao.ESQUERDA to "⬅️", Direcao.DIREITA to "➡️")
    private fun opSeta(d: Direcao) = Opcao(Visual.Seta(d.seta), listOf(pt(d.nome)))

    /** Espelho e giro da seta: para onde ela aponta depois? */
    private fun setaGira(dificil: Boolean): Pergunta {
        val horario = listOf(Direcao.CIMA, Direcao.DIREITA, Direcao.BAIXO, Direcao.ESQUERDA)
        val inicio: Direcao
        val fim: Direcao
        val instrucao: String
        val fala: String
        when (random.nextInt(if (dificil) 4 else 2)) {
            0 -> {
                inicio = listOf(Direcao.ESQUERDA, Direcao.DIREITA).um()
                fim = if (inicio == Direcao.ESQUERDA) Direcao.DIREITA else Direcao.ESQUERDA
                instrucao = "A seta no espelho"
                fala = "A seta se olha no espelho, que fica do lado dela. Para onde aponta a seta do espelho?"
            }
            1 -> {
                inicio = listOf(Direcao.CIMA, Direcao.BAIXO).um()
                fim = if (inicio == Direcao.CIMA) Direcao.BAIXO else Direcao.CIMA
                instrucao = "A seta no lago"
                fala = "A seta se olha na água do lago, que fica embaixo dela. Para onde aponta a seta do lago?"
            }
            2 -> {
                inicio = horario.um()
                fim = horario[(horario.indexOf(inicio) + 1) % 4]
                instrucao = "A seta gira um pouquinho"
                fala = "A seta gira um quarto de volta, para o mesmo lado do ponteiro do relógio. Para onde ela aponta agora?"
            }
            else -> {
                inicio = horario.um()
                fim = horario[(horario.indexOf(inicio) + 2) % 4]
                instrucao = "A seta dá meia volta"
                fala = "A seta dá meia volta. Para onde ela aponta agora?"
            }
        }
        return tocar(instrucao, fala, Cena.Emojis(setaEmoji.getValue(inicio)), opSeta(fim), Direcao.entries.filter { it != fim }.map(::opSeta), "Isso! ${fim.nome.replaceFirstChar { it.uppercase() }}!")
    }

    /** Direita e esquerda a partir do personagem: quem está de frente para nós tem a direita do nosso lado esquerdo. */
    private fun ladoDoPersonagem(mundo: Int): Pergunta {
        val t = VL.tema(mundo)
        val (bicho, nome) = t.figuras.take(6).um()
        val deFrente = random.nextBoolean()
        val direita = random.nextBoolean()
        val certo = if (deFrente == direita) Direcao.ESQUERDA else Direcao.DIREITA
        return tocar(
            instrucao = "De que lado fica a mão?",
            fala = "${nome.replaceFirstChar { it.uppercase() }} está ${if (deFrente) "de frente para você" else "de costas para você"} e levanta a mão ${if (direita) "direita" else "esquerda"}. " +
                "Olhando daqui, de que lado fica essa mão?",
            cena = Cena.Emojis(bicho),
            certa = opSeta(certo),
            erradas = Direcao.entries.filter { it != certo }.map(::opSeta),
            reforco = if (deFrente) "Isso! Quem está de frente tem a direita e a esquerda trocadas com as nossas." else "Isso! De costas, o lado é o mesmo que o nosso.",
        )
    }

    /** Labirinto grande: primeiro passo, último passo ou quantas curvas o caminho faz. */
    private fun labirintoGrande(lado: Int, passos: IntRange): Pergunta {
        val (linhas, colunas, caminho) = caminhoDoLabirinto(lado, lado, passos.random(random))
        val cena = Cena.Labirinto(linhas, colunas, caminho.map { (l, c) -> l * colunas + c })
        val direcoes = caminho.zipWithNext { a, b -> direcaoEntre(a, b) }
        when (random.nextInt(3)) {
            0 -> {
                val curvas = direcoes.zipWithNext().count { (a, b) -> a != b }
                return tocar(
                    instrucao = "Quantas curvas tem o caminho?",
                    fala = "Siga o caminho de pedrinhas com o dedo, da criança até a casinha. Cada vez que o caminho vira, é uma curva. Quantas curvas ele tem?",
                    cena = cena,
                    certa = opcaoNumero(curvas),
                    erradas = erradosDe(curvas, 0..12).map(::opcaoNumero),
                    reforco = "Isso! ${porExtenso(curvas, true).replaceFirstChar { it.uppercase() }} curvas até a casinha.",
                )
            }
            1 -> {
                val d = direcoes.first()
                return tocar("Para que lado você vai primeiro?", "O labirinto ficou grande! Para que lado você vai primeiro para chegar na casinha?", cena, opSeta(d), Direcao.entries.filter { it != d }.map(::opSeta), "Isso! ${d.nome.replaceFirstChar { it.uppercase() }}!")
            }
            else -> {
                val d = direcoes.last()
                return tocar("Qual é o último passo?", "Siga o caminho todo com o dedo. Para que lado é o último passo até a casinha?", cena, opSeta(d), Direcao.entries.filter { it != d }.map(::opSeta), "Isso! ${d.nome.replaceFirstChar { it.uppercase() }}!")
            }
        }
    }

    // ------------------------------------------------------------------ robô

    /** Distância de [inicio] a cada casinha andando para os lados (-1 = não chega). */
    private fun distancias(linhas: Int, colunas: Int, inicio: Int, bloqueadas: Set<Int>): IntArray {
        val dist = IntArray(linhas * colunas) { -1 }
        dist[inicio] = 0
        val fila = ArrayDeque(listOf(inicio))
        while (fila.isNotEmpty()) {
            val atual = fila.removeFirst()
            for (d in Direcao.entries) {
                val l = atual / colunas + d.dl
                val c = atual % colunas + d.dc
                if (l !in 0 until linhas || c !in 0 until colunas) continue
                val n = l * colunas + c
                if (n in bloqueadas || dist[n] >= 0) continue
                dist[n] = dist[atual] + 1
                fila.addLast(n)
            }
        }
        return dist
    }

    /** Robô para programar numa grade [linhas] por [colunas] com [pedras] pedras e caminho de [passos] passos. */
    private fun robo(linhas: Int, colunas: Int, passos: IntRange, pedras: Int = 0, repetir: Boolean = false, folga: Int = 1): Pergunta {
        val total = linhas * colunas
        var montado: ProgramarRobo? = null
        for (tentativa in 0 until 300) {
            val inicio = random.nextInt(total)
            val chegada = random.nextInt(total)
            if (inicio == chegada) continue
            val obst = (0 until total).filter { it != inicio && it != chegada }.sorteia(pedras).toSet()
            val d = distancias(linhas, colunas, inicio, obst)[chegada]
            if (d !in passos) continue
            montado = ProgramarRobo(linhas, colunas, inicio, chegada, obst, maxComandos = d + folga, repetir = repetir)
            break
        }
        val r = montado ?: ProgramarRobo(linhas, colunas, 0, total - 1, maxComandos = linhas + colunas - 2 + folga, repetir = repetir)
        val pedrasFala = if (r.obstaculos.isEmpty()) "" else " sem bater nas pedras"
        val laco = if (repetir) " Use o bloco duas vezes para o programa ficar mais curto." else ""
        return mexer(
            instrucao = "Leve o robô até a bateria",
            fala = "Arraste as setas para a fila de comandos e leve o robô até a bateria$pedrasFala. Depois toque no botão de começar.$laco",
            atividade = r,
            reforco = "O robô chegou! Você programou certinho.",
        )
    }

    /** Comandos em [trechos] retas a partir de [inicio], sem sair da grade (null se não deu ou se voltou ao início). */
    private fun passeioDoRobo(lado: Int, inicio: Int, trechos: Int, maxPorTrecho: Int): List<Direcao>? {
        val comandos = mutableListOf<Direcao>()
        var l = inicio / lado
        var c = inicio % lado
        var anterior: Direcao? = null
        repeat(trechos) {
            val possiveis = Direcao.entries.filter { d ->
                d != anterior && (anterior == null || !(d.dl == -anterior!!.dl && d.dc == -anterior!!.dc)) &&
                    l + d.dl in 0 until lado && c + d.dc in 0 until lado
            }
            if (possiveis.isEmpty()) return null
            val d = possiveis.um()
            val cabe = (1..maxPorTrecho).filter { k -> l + d.dl * k in 0 until lado && c + d.dc * k in 0 until lado }
            val k = cabe.um()
            repeat(k) { comandos += d }
            l += d.dl * k
            c += d.dc * k
            anterior = d
        }
        return if (l * lado + c == inicio) null else comandos
    }

    /** Onde o robô para depois dos comandos? (com [repeticao], os comandos vêm agrupados: "direita 2 vezes"). */
    private fun ondeORoboPara(mundo: Int, lado: Int, trechos: Int, repeticao: Boolean = false): Pergunta {
        val t = VL.tema(mundo)
        var inicio = 0
        var comandos: List<Direcao>? = null
        while (comandos == null) {
            inicio = random.nextInt(lado * lado)
            comandos = passeioDoRobo(lado, inicio, trechos, if (repeticao) 3 else 2)
        }
        val fim = comandos.fold(inicio) { p, d -> (p / lado + d.dl) * lado + p % lado + d.dc }
        val figuras = t.emojis.filter { it != "🤖" }.sorteia(4)
        val outras = (0 until lado * lado).filter { it != inicio && it != fim }.sorteia(3)
        val itens = MutableList(lado * lado) { "" }
        itens[inicio] = "🤖"
        itens[fim] = figuras[0]
        outras.forEachIndexed { i, casa -> itens[casa] = figuras[i + 1] }
        val grupos = mutableListOf<Pair<Direcao, Int>>()
        for (d in comandos) if (grupos.lastOrNull()?.first == d) grupos[grupos.lastIndex] = d to grupos.last().second + 1 else grupos += d to 1
        val escrito = if (repeticao) grupos.joinToString("  ") { (d, k) -> setaEmoji.getValue(d) + if (k > 1) " ×$k" else "" } else comandos.joinToString(" ") { setaEmoji.getValue(it) }
        val falado = if (repeticao) grupos.joinToString(", depois ") { (d, k) -> d.nome + if (k > 1) " ${porExtenso(k, true)} vezes" else " uma vez" }
        else comandos.joinToString(", ") { it.nome }
        return tocar(
            instrucao = "Onde o robô para? $escrito",
            fala = "O robô anda uma casinha em cada comando: $falado. Onde ele para?",
            cena = Cena.Grade(lado, itens),
            certa = opTema(t, figuras[0]),
            erradas = figuras.drop(1).map { opTema(t, it) },
            reforco = "Isso! O robô parou ali.",
        )
    }

    /** Menor caminho com tijolos no meio: quantos passos, no mínimo, até a bateria? */
    private fun menorCaminho(lado: Int, tijolos: Int): Pergunta {
        while (true) {
            val inicio = random.nextInt(lado * lado)
            val chegada = random.nextInt(lado * lado)
            if (inicio == chegada) continue
            val bloqueadas = (0 until lado * lado).filter { it != inicio && it != chegada }.sorteia(tijolos).toSet()
            val d = distancias(lado, lado, inicio, bloqueadas)[chegada]
            if (d < 3) continue
            val itens = List(lado * lado) { i -> when (i) { inicio -> "🤖"; chegada -> "🔋"; in bloqueadas -> "🧱"; else -> "" } }
            return tocar(
                instrucao = "Quantos passos até a bateria?",
                fala = "O robô anda para os lados, para cima e para baixo, e não passa pelos tijolos. Quantos passos, no mínimo, ele precisa dar até a bateria?",
                cena = Cena.Grade(lado, itens),
                certa = opcaoNumero(d),
                erradas = erradosDe(d, 1..(d + 4)).map(::opcaoNumero),
                reforco = "Isso! ${porExtenso(d, true).replaceFirstChar { it.uppercase() }} passos pelo caminho mais curto.",
            )
        }
    }

    // ------------------------------------------------------------------ grupos e diagramas

    private class Diagrama(val itens: List<VocabularioLogica.Coisa>, val a: VocabularioLogica.Rotulo, val b: VocabularioLogica.Rotulo) {
        val dois = itens.filter { a.id in it.tem && b.id in it.tem }
        val soA = itens.filter { a.id in it.tem && b.id !in it.tem }
        val soB = itens.filter { a.id !in it.tem && b.id in it.tem }
        val nenhum = itens.filter { a.id !in it.tem && b.id !in it.tem }
    }

    /** Um par de características (lugar e jeito do bicho, ou tipo e cor da coisa) que serve para a pergunta. */
    private fun diagrama(serve: (Diagrama) -> Boolean): Diagrama {
        val todos = listOf("fazenda", "mar", "selva", "gelo").flatMap { a -> listOf("penas", "quatroPatas").map { b -> Diagrama(VL.bichos, VL.rotulos.getValue(a), VL.rotulos.getValue(b)) } } +
            listOf("fruta", "veiculo", "bicho", "planta").flatMap { a -> listOf("vermelho", "amarelo", "verde", "azul").map { b -> Diagrama(VL.coisas, VL.rotulos.getValue(a), VL.rotulos.getValue(b)) } }
        return todos.filter(serve).um()
    }

    private fun opCoisa(c: VocabularioLogica.Coisa) = opNome(c.emoji, c.nome)

    /** Diagrama de dois grupos: o que está nos dois, só num deles ou fora dos dois. */
    private fun vennEscolha(tipo: Int = random.nextInt(3)): Pergunta {
        val d = diagrama { g ->
            val certos = listOf(g.dois, g.soA, g.nenhum)[tipo]
            certos.isNotEmpty() && g.itens.size - certos.size >= 3 && (tipo != 0 || g.soA.size + g.soB.size >= 2)
        }
        val certos = listOf(d.dois, d.soA, d.nenhum)[tipo]
        val certa = certos.um()
        val perto = when (tipo) { 0 -> d.soA + d.soB; 1 -> d.dois + d.soB; else -> d.soA + d.soB }.shuffled(random)
        val resto = d.itens.filter { it !in certos }.shuffled(random)
        val pergunta = when (tipo) {
            0 -> "${d.a.sim} e ${d.b.sim}"
            1 -> "${d.a.sim} e ${d.b.nao}"
            else -> "${d.a.nao} e ${d.b.nao}"
        }
        return tocar(
            instrucao = "Qual $pergunta?",
            fala = "Pense nos dois grupos. Qual $pergunta?",
            cena = Cena.Nenhuma,
            certa = opCoisa(certa),
            erradas = tresErradas(certa, perto.take(2) + resto).map(::opCoisa),
            reforco = "Isso! ${certa.nome.replaceFirstChar { it.uppercase() }} $pergunta.",
        )
    }

    /** Três são dos dois grupos ao mesmo tempo; o intruso é de um grupo só. */
    private fun intrusoVenn(): Pergunta {
        val d = diagrama { it.dois.size >= 3 && it.soA.size + it.soB.size >= 1 }
        val certos = d.dois.sorteia(3)
        val intruso = (d.soA + d.soB).um()
        return tocar(
            instrucao = "Qual não é da turma?",
            fala = "Três são da mesma turma por dois motivos. Qual não é?",
            cena = Cena.Nenhuma,
            certa = opCoisa(intruso),
            erradas = certos.map(::opCoisa),
            reforco = "Muito esperto! Os outros ${plural(d.a.sim)} e ${plural(d.b.sim)}.",
        )
    }

    /** "mora na fazenda" vira "moram na fazenda" (para falar dos três de uma vez). */
    private fun plural(frase: String): String = when {
        frase.startsWith("mora ") -> "moram " + frase.removePrefix("mora ")
        frase.startsWith("tem ") -> "têm " + frase.removePrefix("tem ")
        frase.startsWith("é um ") -> "são " + frase.removePrefix("é um ") + "s"
        frase.startsWith("é uma ") -> "são " + frase.removePrefix("é uma ") + "s"
        frase.startsWith("é ") -> "são " + frase.removePrefix("é ") + "s"
        else -> frase
    }

    /** Cesta dos dois grupos: só o que está dentro dos dois círculos do diagrama. */
    private fun cestaVenn(): Pergunta {
        val d = diagrama { it.dois.size >= 2 && it.soA.size + it.soB.size >= 3 }
        val certos = d.dois.sorteia(minOf(3, d.dois.size)).map { it.emoji }
        val errados = ((d.soA + d.soB).sorteia(3) + d.nenhum.sorteia(1)).map { it.emoji }
        return mexer(
            instrucao = "Na cesta: o que ${d.a.sim} e ${d.b.sim}",
            fala = "Coloque na cesta só o que ${d.a.sim} e também ${d.b.sim}. As duas coisas ao mesmo tempo!",
            atividade = Atividade.Cesta(certos, certos.size, errados),
            reforco = "Muito bem! Na cesta, só o que está nos dois grupos.",
        )
    }

    /** Toque na figura com três características ao mesmo tempo: forma, cor e tamanho. */
    private fun tresCriterios(): Pergunta {
        val (forma, outraForma) = (0 until 3).toList().sorteia(2)
        val (cor, outraCor) = (0 until 6).toList().sorteia(2)
        val grande = random.nextBoolean()
        val esc = { g: Boolean -> if (g) 1.3f else 0.65f }
        val nome = "${VL.formas[forma]} ${VL.cores[cor]} ${if (grande) "grande" else "pequeno"}"
        return tocar(
            instrucao = "Toque no $nome",
            fala = "Três coisas ao mesmo tempo: a forma, a cor e o tamanho. Toque no $nome.",
            cena = Cena.Nenhuma,
            certa = opFigura(forma, cor, esc(grande)),
            erradas = listOf(opFigura(forma, cor, esc(!grande)), opFigura(forma, outraCor, esc(grande)), opFigura(outraForma, cor, esc(grande))),
            reforco = "Isso! ${nome.replaceFirstChar { it.uppercase() }}!",
        )
    }

    /** Pescaria com critério: uma forma (fácil) ou os dois grupos ao mesmo tempo (difícil). */
    private fun pescaria(dificil: Boolean): Pergunta {
        if (!dificil) {
            val forma = random.nextInt(3)
            val certos = (0 until 6).toList().sorteia(random.nextInt(2, 4)).map { forma to it }
            val errados = (0 until 3).filter { it != forma }.flatMap { fo -> (0 until 6).map { fo to it } }.sorteia(random.nextInt(3, 5))
            val peixes = (certos + errados).shuffled(random)
            return mexer(
                instrucao = "Pesque os ${VL.formasPlural[forma]}",
                fala = "Toque nos peixinhos que levam um ${VL.formas[forma]}, de qualquer cor. Os outros deixe nadando.",
                atividade = Pescaria(peixes.map { (fo, co) -> opFigura(fo, co) }, peixes.indices.filter { peixes[it] in certos }.toSet()),
                reforco = "Pescou todos os ${VL.formasPlural[forma]}!",
            )
        }
        val d = diagrama { it.dois.size >= 2 && it.soA.size + it.soB.size >= 3 }
        val certos = d.dois.sorteia(minOf(3, d.dois.size))
        val errados = (d.soA + d.soB).sorteia(minOf(4, d.soA.size + d.soB.size))
        val peixes = (certos + errados).shuffled(random)
        return mexer(
            instrucao = "Pesque o que ${d.a.sim} e ${d.b.sim}",
            fala = "Toque nos peixinhos que levam o que ${d.a.sim} e também ${d.b.sim}.",
            atividade = Pescaria(peixes.map(::opCoisa), peixes.indices.filter { peixes[it] in certos }.toSet()),
            reforco = "Pescaria esperta!",
        )
    }

    /** Lugares espalhados na cena, sem uma figura em cima da outra. */
    private fun espalhar(opcoes: List<Opcao>): List<ItemNaCena> {
        val colunas = 4
        val linhas = (opcoes.size + colunas - 1) / colunas
        val casas = (0 until colunas * maxOf(linhas, 3)).shuffled(random).take(opcoes.size)
        val altura = maxOf(linhas, 3)
        return opcoes.mapIndexed { i, o ->
            val casa = casas[i]
            val x = (casa % colunas + 0.5f) / colunas + (random.nextFloat() - 0.5f) * 0.08f
            val y = 0.12f + (casa / colunas + 0.5f) / altura * 0.8f + (random.nextFloat() - 0.5f) * 0.06f
            ItemNaCena(o, x.coerceIn(0.05f, 0.95f), y.coerceIn(0.05f, 0.95f), 0.8f + random.nextFloat() * 0.4f)
        }
    }

    /** Caça na cena: achar as figuras iguais à pedida (fácil) ou o que é dos dois grupos (difícil). */
    private fun caca(mundo: Int, dificil: Boolean): Pergunta {
        val t = VL.tema(mundo)
        if (!dificil) {
            val (alvo, nomeAlvo) = t.figuras.um()
            val quantos = random.nextInt(2, 4)
            val outras = t.emojis.filter { it != alvo }.sorteia(random.nextInt(5, 8))
            val ops = (List(quantos) { opTema(t, alvo) } + outras.map { opTema(t, it) }).shuffled(random)
            return mexer(
                instrucao = "Ache todos: $alvo",
                fala = "Procure na cena e toque em todas as figuras de ${nomeAlvo.substringAfter(' ')}. São ${if (quantos == 2) "duas" else "três"}, e algumas estão escondidinhas!",
                atividade = CacaNaCena(mundo - 1, espalhar(ops), ops.indices.filter { (ops[it].visual as Visual.Emoji).emoji == alvo }.toSet()),
                reforco = "Achou todos!",
            )
        }
        val d = diagrama { it.dois.size >= 2 && it.soA.size + it.soB.size >= 3 }
        val certos = d.dois.sorteia(minOf(3, d.dois.size))
        val outros = (d.soA + d.soB).sorteia(minOf(5, d.soA.size + d.soB.size)) + d.nenhum.sorteia(minOf(3, d.nenhum.size))
        val todos = (certos + outros).shuffled(random)
        return mexer(
            instrucao = "Ache o que ${d.a.sim} e ${d.b.sim}",
            fala = "Procure na cena e toque em tudo o que ${d.a.sim} e também ${d.b.sim}.",
            atividade = CacaNaCena(mundo - 1, espalhar(todos.map(::opCoisa)), todos.indices.filter { todos[it] in certos }.toSet()),
            reforco = "Muito bem, detetive!",
        )
    }

    // ------------------------------------------------------------------ ligar pares

    /** Ligar cada figura à sua sombra (com uma sombra sobrando quando [sobra]). */
    private fun ligarSombras(mundo: Int, n: Int, sobra: Boolean): Pergunta {
        val t = VL.tema(mundo)
        val figuras = t.emojis.sorteia(n + 1)
        val esquerda = figuras.take(n)
        val direita = (if (sobra) figuras else esquerda).shuffled(random)
        return mexer(
            instrucao = "Ligue cada figura à sua sombra",
            fala = "Arraste o dedo de cada figura até a sombra dela.",
            atividade = LigarPares(esquerda.map { opTema(t, it) }, direita.map { Opcao(Visual.Sombra(it)) }, esquerda.indices.associateWith { direita.indexOf(esquerda[it]) }),
            reforco = "Cada um achou a sua sombra!",
        )
    }

    /** Ligar o círculo ao coração da mesma cor. */
    private fun ligarCores(n: Int): Pergunta {
        val cores = (0 until 6).toList().sorteia(n)
        val direita = cores.shuffled(random)
        return mexer(
            instrucao = "Ligue as figuras da mesma cor",
            fala = "Arraste o dedo de cada círculo até o coração da mesma cor.",
            atividade = LigarPares(cores.map { opFigura(0, it) }, direita.map { opFigura(2, it) }, cores.indices.associateWith { direita.indexOf(cores[it]) }),
            reforco = "Cores combinadas!",
        )
    }

    /** Ligar a causa ao que acontece depois. */
    private fun ligarCausas(n: Int): Pergunta {
        val causas = VL.causas.sorteia(n)
        val direita = causas.shuffled(random)
        return mexer(
            instrucao = "Ligue ao que acontece depois",
            fala = "Arraste o dedo de cada cena até o que acontece depois dela.",
            atividade = LigarPares(
                causas.map { opNome(it.cena, it.causa) },
                direita.map { opNome(it.efeito, it.nomeEfeito) },
                causas.indices.associateWith { direita.indexOf(causas[it]) },
            ),
            reforco = "Isso! Uma coisa leva à outra.",
        )
    }

    /** Ligar cada bicho ao lugar onde ele mora. */
    private fun ligarLugar(n: Int): Pergunta {
        val lugares = VL.lugares.keys.toList().sorteia(n)
        val bichos = lugares.map { l -> VL.bichos.filter { l in it.tem }.um() }
        val direita = lugares.shuffled(random)
        return mexer(
            instrucao = "Ligue cada bicho à sua casa",
            fala = "Arraste o dedo de cada bicho até o lugar onde ele mora.",
            atividade = LigarPares(
                bichos.map(::opCoisa),
                direita.map { VL.lugares.getValue(it).let { (e, nome) -> opNome(e, nome) } },
                bichos.indices.associateWith { direita.indexOf(lugares[it]) },
            ),
            reforco = "Cada bicho na sua casa!",
        )
    }

    /** Ligar cada bicho à comida dele (fazenda). */
    private fun ligarComida(n: Int): Pergunta {
        val pares = Vocabulario.pares.sorteia(n)
        val direita = pares.shuffled(random)
        return mexer(
            instrucao = "Ligue o que combina",
            fala = "Arraste o dedo de cada figura até o que combina com ela.",
            atividade = LigarPares(pares.map { opNome(it.de, it.nomeDe) }, direita.map { opNome(it.para, it.nomePara) }, pares.indices.associateWith { direita.indexOf(pares[it]) }),
            reforco = "Tudo combinado!",
        )
    }

    // ------------------------------------------------------------------ dedução (pistas)

    private fun doDa(nome: String) = if (VL.criancas.first { it.first == nome }.second) "da $nome" else "do $nome"

    /** Quem é quem: as crianças e os bichinhos, com pistas que deixam um jeito só de combinar. */
    private data class QuemEQuem(val criancas: List<String>, val bichos: List<Pair<String, String>>, val dono: List<Int>, val pistas: List<String>, val perguntada: Int)

    private fun montarQuemEQuem(n: Int): QuemEQuem {
        while (true) {
            val criancas = VL.criancas.sorteia(n).map { it.first }
            val bichos = VL.bichinhos.sorteia(n)
            val dono = bichos.indices.shuffled(random)
            data class Pista(val crianca: Int, val bicho: Int, val tem: Boolean)
            val candidatas = criancas.indices.flatMap { c -> bichos.indices.map { b -> Pista(c, b, dono[c] == b) } }
            val pistas = candidatas.sorteia(n - 1)
            if (n > 2 && pistas.all { it.tem }) continue
            val perms = permutacoes(n).filter { p -> pistas.all { (p[it.crianca] == it.bicho) == it.tem } }
            if (perms.size != 1) continue
            val semPistaDireta = criancas.indices.filter { c -> pistas.none { it.crianca == c && it.tem } }
            val perguntada = semPistaDireta.um()
            val falas = pistas.map { p -> "${criancas[p.crianca]} ${if (p.tem) "tem" else "não tem"} ${bichos[p.bicho].second}." }
            return QuemEQuem(criancas, bichos, dono, falas, perguntada)
        }
    }

    private fun permutacoes(n: Int): List<List<Int>> =
        if (n == 0) listOf(emptyList()) else permutacoes(n - 1).flatMap { p -> (0..p.size).map { i -> p.take(i) + (n - 1) + p.drop(i) } }

    private fun quemEQuem(n: Int): Pergunta {
        val q = montarQuemEQuem(n)
        val certa = q.bichos[q.dono[q.perguntada]]
        val extras = VL.bichinhos.filter { it !in q.bichos }.sorteia(4 - n)
        val nome = q.criancas[q.perguntada]
        return tocar(
            instrucao = "Qual é o bichinho ${doDa(nome)}?",
            fala = "Cada criança tem um bichinho: ${q.criancas.joinToString(", ")}. Pistas: ${q.pistas.joinToString(" ")} Qual é o bichinho ${doDa(nome)}?",
            cena = Cena.Emojis(q.bichos.joinToString(" ") { it.first }),
            certa = opNome(certa.first, certa.second),
            erradas = (q.bichos + extras).filter { it != certa }.map { opNome(it.first, it.second) },
            reforco = "Isso! O bichinho ${doDa(nome)} é ${certa.second}.",
        )
    }

    private fun ligarQuemEQuem(n: Int): Pergunta {
        val q = montarQuemEQuem(n)
        val direita = q.bichos.shuffled(random)
        return mexer(
            instrucao = "Ligue cada criança ao seu bichinho",
            fala = "Pistas: ${q.pistas.joinToString(" ")} Cada criança tem um bichinho só. Ligue cada criança ao bichinho dela.",
            atividade = LigarPares(
                q.criancas.map { Opcao(Visual.Texto(it), listOf(pt(it))) },
                direita.map { opNome(it.first, it.second) },
                q.criancas.indices.associateWith { direita.indexOf(q.bichos[q.dono[it]]) },
            ),
            reforco = "Detetive de verdade! Cada um com o seu bichinho.",
        )
    }

    /** As 4 a 6 figuras do detetive e as pistas: é um [forma]; não é da cor [semCor]. */
    private fun suspeitos(n: Int): Triple<List<Pair<Int, Int>>, Pair<Int, Int>, String> {
        val formas = (0 until 3).toList().shuffled(random)
        val (cor, semCor) = (0 until 6).toList().sorteia(2)
        val certo = formas[0] to cor
        val outros = listOf(formas[0] to semCor, formas[1] to cor, formas[1] to semCor, formas[2] to cor, formas[2] to semCor).take(n - 1)
        val pistas = "Pista um: é um ${VL.formas[formas[0]]}. Pista dois: não é ${VL.cores[semCor]}."
        return Triple((outros + certo).shuffled(random), certo, pistas)
    }

    private fun detetive(): Pergunta {
        val (todos, certo, pistas) = suspeitos(4)
        return tocar(
            instrucao = "Onde está o tesouro?",
            fala = "O tesouro está escondido numa destas figuras. $pistas Onde está o tesouro?",
            cena = Cena.Emojis("💰"),
            certa = opFigura(certo.first, certo.second),
            erradas = todos.filter { it != certo }.map { (fo, co) -> opFigura(fo, co) },
            reforco = "Achou! As duas pistas levam ao mesmo lugar.",
        )
    }

    private fun escondePistas(mundo: Int, n: Int): Pergunta {
        val (todos, certo, pistas) = suspeitos(n)
        val (bicho, nome) = VL.tema(mundo).figuras.first()
        return mexer(
            instrucao = "Onde ${nome.substringAfter(' ')} se escondeu?",
            fala = "${nome.replaceFirstChar { it.uppercase() }} se escondeu atrás de uma figura. $pistas Toque onde ele está.",
            atividade = EscondeEsconde(bicho, todos.map { (fo, co) -> opFigura(fo, co) }, todos.indexOf(certo)),
            reforco = "Achou com as pistas!",
        )
    }

    private fun escondeTrocas(mundo: Int, n: Int, trocas: Int): Pergunta {
        val t = VL.tema(mundo)
        val (bicho, nome) = t.figuras.first()
        val lugares = t.emojis.drop(1).sorteia(n)
        return mexer(
            instrucao = "Siga com os olhos",
            fala = "Olhe bem onde ${nome} se esconde. As coisas vão trocar de lugar ${porExtenso(trocas, true)} ${if (trocas == 1) "vez" else "vezes"}. Siga com os olhos e depois toque onde ele está!",
            atividade = EscondeEsconde(bicho, lugares.map { opTema(t, it) }, random.nextInt(n), trocas),
            reforco = "Achou! Que olhos espertos!",
        )
    }

    /** Se... então: cada figura tocada faz aparecer um bicho. */
    private fun seEntao(mundo: Int, regras: Int): Pergunta {
        val t = VL.tema(mundo)
        val cores = (0 until 6).toList().sorteia(regras)
        val bichos = t.figuras.sorteia(4)
        val r = random.nextInt(regras)
        val certa = bichos[r]
        val cena = cores.indices.joinToString("   ") { VL.figura(0, cores[it]) + "➡️" + bichos[it].first }
        val falaRegras = cores.indices.joinToString(" ") { "Se tocar no círculo ${VL.cores[cores[it]]}, aparece ${bichos[it].second}." }
        return tocar(
            instrucao = "Se tocar no ${VL.figura(0, cores[r])}, o que aparece?",
            fala = "$falaRegras A fada tocou no círculo ${VL.cores[cores[r]]}. O que aparece?",
            cena = Cena.Emojis(cena),
            certa = opNome(certa.first, certa.second),
            erradas = bichos.filter { it != certa }.map { opNome(it.first, it.second) },
            reforco = "Isso! Se tocar no ${VL.cores[cores[r]]}, então aparece ${certa.second}.",
        )
    }

    private fun causaEfeito(): Pergunta {
        val c = VL.causas.um()
        return tocar(
            instrucao = "O que acontece depois?",
            fala = "Quando ${c.causa}, o que acontece depois?",
            cena = Cena.Emojis(c.cena),
            certa = opNome(c.efeito, c.nomeEfeito),
            erradas = VL.causas.filter { it != c }.sorteia(3).map { opNome(it.efeito, it.nomeEfeito) },
            reforco = "Isso! ${c.nomeEfeito.replaceFirstChar { it.uppercase() }}.",
        )
    }

    /** Pistas de peso: a caixa A pesa mais que a B, a B mais que a C... qual é a mais pesada (ou a mais leve)? */
    private fun transitividade(embaralhar: Boolean): Pergunta {
        val ordem = (0 until 6).toList().sorteia(4)
        val nome = { c: Int -> "a caixa ${VL.coresFeminino[c]}" }
        val pistas = ordem.zipWithNext { a, b -> "${nome(a).replaceFirstChar { it.uppercase() }} pesa mais que ${nome(b)}." }
        val maisPesada = random.nextBoolean()
        val certo = if (maisPesada) ordem.first() else ordem.last()
        val op = { c: Int -> opNome(VL.caixa(c), nome(c)) }
        return tocar(
            instrucao = if (maisPesada) "Qual caixa é a mais pesada?" else "Qual caixa é a mais leve?",
            fala = (if (embaralhar) pistas.shuffled(random) else pistas).joinToString(" ") + if (maisPesada) " Qual caixa é a mais pesada de todas?" else " Qual caixa é a mais leve de todas?",
            cena = Cena.Emojis("⚖️"),
            certa = op(certo),
            erradas = ordem.filter { it != certo }.map(op),
            reforco = "Isso! ${nome(certo).replaceFirstChar { it.uppercase() }} é a mais ${if (maisPesada) "pesada" else "leve"}.",
        )
    }

    /** Seriação: o segundo maior ou o segundo menor de quatro tamanhos. */
    private fun seriacao(mundo: Int): Pergunta {
        val t = VL.tema(mundo)
        val e = t.emojis.take(6).um()
        val escalas = listOf(0.5f, 0.8f, 1.1f, 1.4f)
        val segundoMaior = random.nextBoolean()
        val certa = if (segundoMaior) escalas[2] else escalas[1]
        return tocar(
            instrucao = if (segundoMaior) "Qual é o segundo maior?" else "Qual é o segundo menor?",
            fala = if (segundoMaior) "Imagine todos em fila, do maior para o menor. Qual vem logo depois do maior?" else "Imagine todos em fila, do menor para o maior. Qual vem logo depois do menor?",
            cena = Cena.Nenhuma,
            certa = opEmoji(e, certa),
            erradas = escalas.filter { it != certa }.map { opEmoji(e, it) },
            reforco = "Isso! Você colocou todos em ordem na cabeça.",
        )
    }

    /** Ordem de uma história de 4 passos: o que vem depois, ou o que aconteceu primeiro (opções da mesma história). */
    private fun historia(): Pergunta {
        val h = VL.historias.filter { it.size == 4 }.um()
        if (random.nextInt(3) == 0) {
            val certa = h.first()
            return tocar(
                instrucao = "O que aconteceu primeiro?",
                fala = "Isto aconteceu no fim da história: ${h.last().second}. O que aconteceu primeiro, no começo de tudo?",
                cena = Cena.Emojis(h.last().first),
                certa = opNome(certa.first, certa.second),
                erradas = h.drop(1).map { opNome(it.first, it.second) },
                reforco = "Isso! Tudo começou com ${certa.second}.",
            )
        }
        val k = random.nextInt(1, h.size)
        val certa = h[k]
        return tocar(
            instrucao = "O que acontece depois?",
            fala = "Olhe a história: " + h.take(k).joinToString(", ") { it.second } + ". O que acontece depois?",
            cena = Cena.Sequencia(h.take(k).map { it.first } + "?"),
            certa = opNome(certa.first, certa.second),
            erradas = h.filter { it != certa }.map { opNome(it.first, it.second) },
            reforco = "Isso! Primeiro " + h.take(k + 1).joinToString(", depois ") { it.second } + ".",
        )
    }

    /** Balança de trocas: quantos doces pesam o mesmo que outro doce? */
    private fun balancaTrocas(passos: Int): Pergunta {
        val (a, b, c) = VL.doces.sorteia(3)
        if (passos == 1) {
            val k = random.nextInt(2, 4)
            val m = random.nextInt(1, 4)
            val resposta = k * m
            return tocar(
                instrucao = "Quantos ${b.first} pesam o mesmo que ${a.first.repeat(m)}?",
                fala = "Na balança, um ${a.second} pesa o mesmo que ${porExtenso(k)} ${b.second}s. Quantos ${b.second}s pesam o mesmo que ${if (m == 1) "um ${a.second}" else "${porExtenso(m)} ${a.second}s"}?",
                cena = Cena.Emojis("${a.first} ⚖️ ${b.first.repeat(k)}"),
                certa = opcaoNumero(resposta),
                erradas = erradosDe(resposta, listOf(resposta + 1, resposta - 1, k, m, k + m, resposta + k, resposta - k).filter { it in 1..12 }).map(::opcaoNumero),
                reforco = "Isso! ${porExtenso(resposta).replaceFirstChar { it.uppercase() }} ${b.second}s.",
            )
        }
        val k1 = random.nextInt(2, 4)
        val k2 = random.nextInt(2, 4)
        val resposta = k1 * k2
        return tocar(
            instrucao = "Quantos ${c.first} pesam o mesmo que um ${a.first}?",
            fala = "Um ${a.second} pesa o mesmo que ${porExtenso(k1)} ${b.second}s. Um ${b.second} pesa o mesmo que ${porExtenso(k2)} ${c.second}s. Quantos ${c.second}s pesam o mesmo que um ${a.second}?",
            cena = Cena.Emojis("${a.first} ⚖️ ${b.first.repeat(k1)}   ${b.first} ⚖️ ${c.first.repeat(k2)}"),
            certa = opcaoNumero(resposta),
            erradas = erradosDe(resposta, listOf(k1 + k2, resposta + 1, resposta - 1, resposta + 2, k1, k2).filter { it in 1..12 }).map(::opcaoNumero),
            reforco = "Isso! Troca, troca: ${porExtenso(resposta)} ${c.second}s.",
        )
    }

    /** Balança de pesos com número: deixar os dois pratos iguais. */
    private fun balanca(mundo: Int, dificil: Boolean): Pergunta {
        val esquerda = if (dificil) List(2) { random.nextInt(2, 7) } else listOf(random.nextInt(3, 9))
        val alvo = esquerda.sum()
        val primeiro = random.nextInt(1, alvo)
        val certos = if (dificil && alvo - primeiro > 2 && random.nextBoolean()) {
            val segundo = random.nextInt(1, alvo - primeiro)
            listOf(primeiro, segundo, alvo - primeiro - segundo)
        } else listOf(primeiro, alvo - primeiro)
        val pesos = (certos + List(if (dificil) 3 else 2) { random.nextInt(1, 10) }).shuffled(random)
        return mexer(
            instrucao = "Deixe a balança reta",
            fala = "Coloque pesos no prato vazio até a balança ficar retinha, com o mesmo peso dos dois lados.",
            atividade = Balanca(esquerda, pesos, figura = VL.tema(mundo).emojis.first()),
            reforco = "A balança ficou reta! Os dois lados pesam ${porExtenso(alvo)}.",
        )
    }

    /** Coordenadas: letra da coluna e número da linha (o que está na casa B2, ou onde está a figura). */
    private fun coordenadas(mundo: Int, lado: Int, inversa: Boolean): Pergunta {
        val t = VL.tema(mundo)
        val figuras = (t.emojis + Vocabulario.figurasSimples).distinct().take(lado * lado + 4).shuffled(random).take(lado * lado)
        val letras = listOf("A", "B", "C", "D")
        val itens = listOf("") + letras.take(lado) + (0 until lado).flatMap { l -> listOf("${l + 1}") + figuras.subList(l * lado, l * lado + lado) }
        val casa = random.nextInt(lado * lado)
        val (l, c) = casa / lado to casa % lado
        val nomeDaCasa = { i: Int -> letras[i % lado] + (i / lado + 1) }
        val falaDaCasa = { i: Int -> falaDaLetra(letras[i % lado]) + " " + porExtenso(i / lado + 1) }
        val cena = Cena.Grade(lado + 1, itens)
        if (!inversa) {
            val perto = (0 until lado * lado).filter { it != casa && (it / lado == l || it % lado == c) }.shuffled(random)
            val certa = figuras[casa]
            return tocar(
                instrucao = "O que está na casa ${nomeDaCasa(casa)}?",
                fala = "As letras ficam em cima e os números do lado. Ache a coluna da letra e a linha do número. O que está na casa ${falaDaCasa(casa)}?",
                cena = cena,
                certa = opNome(certa, t.nomeDe(certa)),
                erradas = tresErradas(certa, (perto + (0 until lado * lado).shuffled(random)).map { figuras[it] }).map { opNome(it, t.nomeDe(it)) },
                reforco = "Isso! Na casa ${falaDaCasa(casa)}.",
            )
        }
        val perto = listOf((l * lado + (c + 1) % lado), (((l + 1) % lado) * lado + c), (c * lado + l)).filter { it != casa } + (0 until lado * lado).shuffled(random)
        val op = { i: Int -> Opcao(Visual.Texto(nomeDaCasa(i)), listOf(pt(falaDaCasa(i)))) }
        return tocar(
            instrucao = "Em que casa está ${figuras[casa]}?",
            fala = "Procure a figura no mapa. Em que casa ela está? Primeiro a letra de cima, depois o número do lado.",
            cena = cena,
            certa = op(casa),
            erradas = tresErradas(casa, perto).map(op),
            reforco = "Isso! Casa ${falaDaCasa(casa)}.",
        )
    }

    // ------------------------------------------------------------------ atividades de mexer

    private fun quebra(mundo: Int, linhas: Int, colunas: Int, fixas: Int = 0, numeros: Boolean = false): Pergunta {
        val t = VL.tema(mundo)
        val (figura, nome) = t.figuras.take(5).um()
        val pecas = linhas * colunas
        return mexer(
            instrucao = "Monte o quebra cabeça",
            fala = "Arraste cada pedaço para o seu lugar e monte ${nome}." + if (numeros) " Os números ajudam: um, dois, três..." else "",
            atividade = QuebraCabeca(figura, linhas, colunas, if (numeros) List(pecas) { "${it + 1}" } else emptyList(), (0 until pecas).toList().sorteia(fixas).toSet()),
            reforco = "Montou ${nome}!",
        )
    }

    private fun siga(mundo: Int, botoes: Int, tamanho: Int): Pergunta {
        val t = VL.tema(mundo)
        val figuras = t.figuras.sorteia(botoes)
        var sequencia: List<Int>
        do {
            sequencia = List(tamanho) { random.nextInt(botoes) }
        } while (sequencia.toSet().size < 2 || sequencia.zipWithNext().count { (a, b) -> a == b } > 1)
        return mexer(
            instrucao = "Repita a sequência",
            fala = "Olhe e escute: os botões vão acender. Depois toque neles na mesma ordem.",
            atividade = SigaASequencia(figuras.map { (e, n) -> opNome(e, n) }, VL.coresArgb.take(botoes), sequencia),
            reforco = "Que memória! ${porExtenso(tamanho, true).replaceFirstChar { it.uppercase() }} toques certinhos.",
        )
    }

    private fun memoriaTema(mundo: Int, pares: Int) = memoria(pares, iguais = VL.tema(mundo).emojis)

    /** Trem em ordem de tamanho (do menor para o maior ou ao contrário). */
    private fun tremTamanho(mundo: Int, n: Int, pistas: Int): Pergunta {
        val t = VL.tema(mundo)
        val (e, nome) = t.figuras.take(6).um()
        val crescente = random.nextBoolean()
        val escalas = List(n) { 0.5f + it * (1f / (n - 1)) }.let { if (crescente) it else it.reversed() }
        return mexer(
            instrucao = if (crescente) "Do menor para o maior" else "Do maior para o menor",
            fala = "Engate os vagões ${if (crescente) "do menor para o maior" else "do maior para o menor"}, começando perto da locomotiva.",
            atividade = Trem(escalas.map { Opcao(Visual.Emoji(e, it), listOf(pt(nome))) }, (0 until n).toList().sorteia(pistas).toSet()),
            reforco = "O trem está em ordem! Piuí!",
        )
    }

    /** Trem da história: os passos na ordem em que acontecem. */
    private fun tremHistoria(n: Int, extra: Boolean): Pergunta {
        val h = VL.historias.filter { it.size >= n }.um().take(n)
        val extras = if (extra) listOf(VL.historias.filter { it.first() != h.first() }.flatten().um()) else emptyList()
        return mexer(
            instrucao = "Coloque a história em ordem",
            fala = "Engate os vagões na ordem em que as coisas acontecem: primeiro, depois, por último.",
            atividade = Trem(h.map { opNome(it.first, it.second) }, extras = extras.map { opNome(it.first, it.second) }),
            reforco = "Isso! " + h.joinToString(", depois ") { it.second } + ".",
        )
    }

    /** Trem que cresce: 1, 2, 3... figuras em cada vagão. */
    private fun tremCrescente(mundo: Int, n: Int): Pergunta {
        val e = VL.tema(mundo).emojis.um()
        return mexer(
            instrucao = "Do vagão com menos para o com mais",
            fala = "Engate os vagões do que tem menos figuras para o que tem mais.",
            atividade = Trem(List(n) { Opcao(Visual.Emoji(e.repeat(it + 1)), listOf(pt(porExtenso(it + 1)))) }, setOf(0)),
            reforco = "A fila cresce de um em um!",
        )
    }

    private fun tremArcoIris(n: Int): Pergunta {
        val cores = VL.arcoIris.take(n)
        return mexer(
            instrucao = "As cores do arco-íris em ordem",
            fala = "Engate os vagões com as cores do arco-íris na ordem: vermelho, laranja, amarelo, verde, azul e roxo.",
            atividade = Trem(cores.map { Opcao(Visual.Cor(VL.coresArgb[it]), listOf(pt(VL.cores[it]))) }, setOf(0)),
            reforco = "Um arco-íris de trem!",
        )
    }

    /** Cartas viradas para achar em ordem: números, letras, tamanhos, histórias ou cores do arco-íris. */
    private fun cartas(mundo: Int, n: Int, tipo: Int = random.nextInt(4)): Pergunta {
        val (ops, fala) = when (tipo) {
            0 -> List(n) { opcaoNumero(it + 1) } to "Vire as cartas e ache os números em ordem: um, dois, três..."
            1 -> List(n) { i -> ('A' + i).toString().let { Opcao(Visual.Texto(it), listOf(pt(falaDaLetra(it)))) } } to "Vire as cartas e ache as letras em ordem: A, B, C..."
            2 -> {
                val (e, nome) = VL.tema(mundo).figuras.take(6).um()
                val k = minOf(n, 5)
                List(k) { Opcao(Visual.Emoji(e, 0.5f + it * (1f / (k - 1))), listOf(pt(nome))) } to "Vire as cartas e ache as figuras do menor para o maior."
            }
            else -> VL.arcoIris.take(minOf(n, 6)).map { Opcao(Visual.Cor(VL.coresArgb[it]), listOf(pt(VL.cores[it]))) } to "Vire as cartas e ache as cores do arco-íris em ordem: vermelho, laranja, amarelo, verde, azul e roxo."
        }
        val embaralhadas = ops.indices.shuffled(random)
        return mexer(
            instrucao = "Ache as cartas em ordem",
            fala = fala + " Se virar outra, lembre onde ela fica!",
            atividade = CartasViradas(embaralhadas.map { ops[it] }, ops.indices.map { embaralhadas.indexOf(it) }),
            reforco = "Achou todas em ordem!",
        )
    }

    /** Dominó: cada peça combina figura com sombra, círculo com coração da mesma cor, ou bicho com o que ele come. */
    private fun domino(mundo: Int, jogadas: Int, sobras: Int, tipo: Int = random.nextInt(3)): Pergunta {
        val chaves: List<String>
        val lado1: (String) -> MeiaPeca
        val lado2: (String) -> MeiaPeca
        val fala: String
        when (tipo) {
            0 -> {
                val t = VL.tema(mundo)
                chaves = t.emojis.sorteia(jogadas + 1 + 2 * sobras)
                lado1 = { k -> MeiaPeca(k, Visual.Emoji(k), listOf(pt(t.nomeDe(k)))) }
                lado2 = { k -> MeiaPeca(k, Visual.Sombra(k)) }
                fala = "Dominó das sombras: encaixe a peça que tem a sombra da figura da ponta."
            }
            1 -> {
                chaves = (0 until 6).toList().sorteia(minOf(6, jogadas + 1 + 2 * sobras)).map { "$it" }
                lado1 = { k -> MeiaPeca(k, Visual.Emoji(VL.figura(0, k.toInt())), listOf(pt("círculo ${VL.cores[k.toInt()]}"))) }
                lado2 = { k -> MeiaPeca(k, Visual.Emoji(VL.figura(2, k.toInt())), listOf(pt("coração ${VL.cores[k.toInt()]}"))) }
                fala = "Dominó das cores: encaixe a peça que tem o coração da mesma cor do círculo da ponta."
            }
            else -> {
                val pares = Vocabulario.pares.sorteia(jogadas + 1 + 2 * sobras)
                chaves = pares.map { it.de }
                lado1 = { k -> pares.first { it.de == k }.let { MeiaPeca(k, Visual.Emoji(it.de), listOf(pt(it.nomeDe))) } }
                lado2 = { k -> pares.first { it.de == k }.let { MeiaPeca(k, Visual.Emoji(it.para), listOf(pt(it.nomePara))) } }
                fala = "Dominó do que combina: encaixe a peça com o que combina com a figura da ponta."
            }
        }
        val fila = chaves.take(jogadas + 1)
        val pecas = (0 until jogadas).map { PecaDomino(lado2(fila[it]), lado1(fila[it + 1])) }
        val livres = chaves.drop(jogadas + 1)
        val extras = (0 until minOf(sobras, livres.size / 2)).map { PecaDomino(lado2(livres[2 * it]), lado1(livres[2 * it + 1])) }
        return mexer(
            instrucao = "Complete o dominó",
            fala = fala,
            atividade = Domino(lado1(fila[0]), (pecas + extras).shuffled(random), jogadas),
            reforco = "Dominó completo!",
        )
    }

    /** Pintar o espelho: o lado esquerdo tem os números das cores; o direito é pintado igual ao espelho. */
    private fun pintarEspelho(maxColunas: Int): Pergunta {
        val d = VL.desenhos.filter { it.colunas <= maxColunas }.um()
        val metade = d.colunas / 2
        val casas = d.linhas.flatMap { linha ->
            linha.mapIndexed { c, ch ->
                when {
                    ch == '.' -> CasaDePintar("", null)
                    c < metade -> CasaDePintar("$ch", "$ch")
                    else -> CasaDePintar("?", "$ch")
                }
            }
        }
        return mexer(
            instrucao = "Pinte e espelhe",
            fala = "Pinte o lado esquerdo pelos números. O lado direito é o espelho: pinte cada casinha com a mesma cor da casinha do outro lado.",
            atividade = PintarPorCodigo(d.colunas, casas, d.paleta.mapIndexed { i, cor -> CorDaLegenda("${i + 1}", VL.coresArgb[cor], listOf(pt(VL.cores[cor]))) }),
            reforco = "Que lindo! ${d.nome.replaceFirstChar { it.uppercase() }} ficou igualzinho dos dois lados.",
        )
    }

    /** Pintar por símbolo: cada símbolo (bolinha, triângulo, quadrado) é uma cor. */
    private fun pintarSimbolo(maxColunas: Int, distrator: Boolean): Pergunta {
        val d = VL.desenhos.filter { it.colunas <= maxColunas }.um()
        val casas = d.linhas.flatMap { linha ->
            linha.map { ch -> if (ch == '.') CasaDePintar("", null) else VL.simbolos[ch - '1'].let { CasaDePintar(it, it) } }
        }
        val legenda = d.paleta.mapIndexed { i, cor -> CorDaLegenda(VL.simbolos[i], VL.coresArgb[cor], listOf(pt(VL.cores[cor]))) }
        val sobra = (0 until 6).filter { it !in d.paleta }.um()
        return mexer(
            instrucao = "Pinte pelo símbolo",
            fala = "Cada símbolo tem uma cor: olhe a legenda dos potes de tinta e pinte cada casinha com a cor do seu símbolo.",
            atividade = PintarPorCodigo(d.colunas, casas, legenda + if (distrator) listOf(CorDaLegenda(VL.simbolos[3], VL.coresArgb[sobra], listOf(pt(VL.cores[sobra])))) else emptyList()),
            reforco = "Pintou ${d.nome}!",
        )
    }

    // ------------------------------------------------------------------ os mundos

    private fun mundoNovo(m: Int): List<ModeloDeFase> = when (m) {
        6 -> listOf( // Fazenda: classificar por dois atributos, filas de cor e forma
            f("Fila de cor e forma", "🔴", { filaDuasRegras(false) }, { corEForma(false) }, { ligarCores(3) }),
            f("Cesta da fazenda", "🧺", { cestaVenn() }, { cestaVenn() }, { filaDuasRegras(false) }),
            f("Intrusos da fazenda", "🐄", { intrusoVenn() }, { corEForma(true) }, { cestaVenn() }),
            f("Quebra cabeça da vaca", "🧩", { quebra(m, 2, 2, fixas = 1) }, { quebra(m, 2, 2, fixas = 1) }, { ligarComida(3) }),
            f("Marco da fazenda", "🚩", { filaDuasRegras(false) }, { cestaVenn() }, { intrusoVenn() }, { quebra(m, 2, 2) }),
            f("Ligue o que combina", "🔗", { ligarComida(3) }, { ligarComida(4) }, { vennEscolha(0) }),
            f("Monte a fazenda", "🐷", { quebra(m, 2, 2) }, { quebra(m, 2, 2) }, { cestaVenn() }, { vennEscolha(0) }),
            f("Fazenda e o espaço", "🚀", { filaDuasRegras(false) }, { filaComPadrao("AABBC") }, { sudoku(2) }, { ligarCores(3) }),
            f("Fazenda difícil", "🧠", { filaDuasRegras(true) }, { intrusoVenn() }, { cestaVenn() }, { vennEscolha(0) }),
            f("Chefão da fazenda", "👑", { quebra(m, 2, 2) }, { cestaVenn() }, { filaDuasRegras(true) }, { intrusoVenn() }),
        )
        7 -> listOf( // Fundo do Mar: memória de sequência e pares
            f("Fila do mar", "🐠", { filaComPadrao(listOf("ABC", "AAB", "ABB").um()) }, { filaDuasRegras(false) }, { memoriaTema(m, 3) }),
            f("Siga os peixes", "🐟", { siga(m, 2, 2) }, { siga(m, 2, 3) }, { filaDuasRegras(false) }),
            f("Pescaria esperta", "🎣", { pescaria(false) }, { pescaria(false) }, { intrusoVenn() }),
            f("Memória do mar", "🃏", { memoriaTema(m, 4) }, { memoriaTema(m, 4) }, { siga(m, 2, 3) }),
            f("Marco do mar", "🚩", { siga(m, 3, 3) }, { pescaria(false) }, { memoriaTema(m, 4) }, { filaDuasRegras(false) }),
            f("Siga as cores", "🌈", { siga(m, 3, 3) }, { siga(m, 3, 3) }, { filaComPadrao("ABCD") }),
            f("Pescaria de grupos", "🐡", { pescaria(true) }, { pescaria(true) }, { memoriaTema(m, 5) }),
            f("Mar e fazenda", "🐄", { siga(m, 3, 3) }, { cestaVenn() }, { filaDuasRegras(true) }),
            f("Mar difícil", "🧠", { siga(m, 4, 3) }, { memoriaTema(m, 6) }, { pescaria(true) }, { filaDuasRegras(true) }),
            f("Chefão do mar", "🐙", { siga(m, 3, 3) }, { memoriaTema(m, 5) }, { pescaria(true) }),
        )
        8 -> listOf( // Selva: direita e esquerda a partir do personagem, labirintos médios
            f("Setas da selva", "➡️", { setaGira(false) }, { labirinto(false) }, { robo(3, 3, 2..2) }),
            f("Robô na selva", "🤖", { robo(3, 3, 2..3) }, { robo(3, 3, 2..3) }, { setaGira(false) }),
            f("A mão do macaco", "🐒", { ladoDoPersonagem(m) }, { ladoDoPersonagem(m) }, { labirinto(false) }, { escondeTrocas(m, 3, 1) }),
            f("Esconde esconde", "🙈", { escondeTrocas(m, 3, 1) }, { escondeTrocas(m, 3, 2) }, { robo(3, 3, 2..3) }),
            f("Marco da selva", "🚩", { ladoDoPersonagem(m) }, { robo(3, 3, 2..3) }, { escondeTrocas(m, 3, 2) }, { labirinto(false) }),
            f("Caça na selva", "🔍", { caca(m, false) }, { caca(m, false) }, { labirinto(false) }),
            f("Robô e pedras", "🪨", { robo(4, 4, 3..3, pedras = 2) }, { robo(4, 4, 3..3, pedras = 2) }, { caca(m, false) }, { ladoDoPersonagem(m) }),
            f("Selva e mar", "🐠", { labirinto(false) }, { siga(m, 3, 3) }, { escondeTrocas(m, 3, 2) }),
            f("Selva difícil", "🧠", { robo(4, 4, 3..3, pedras = 3, folga = 0) }, { ladoDoPersonagem(m) }, { labirinto(true) }),
            f("Chefão da selva", "🐯", { robo(4, 4, 3..3, pedras = 2) }, { escondeTrocas(m, 3, 2) }, { ladoDoPersonagem(m) }, { labirinto(true) }),
        )
        9 -> listOf( // Polo Norte: quebra cabeça e completar a figura
            f("Complete o quadro", "🧊", { quadroQueFalta(m, 2) }, { quadroQueFalta(m, 3) }, { sombra() }, { quebra(m, 2, 2, fixas = 1) }),
            f("Quebra cabeça do gelo", "🧩", { quebra(m, 2, 2) }, { quebra(m, 2, 2) }, { quadroQueFalta(m, 3) }),
            f("Sombras do gelo", "🌑", { sombra() }, { quadroQueFalta(m, 3) }, { matriz(m, quantidade = true) }, { ligarSombras(m, 3, false) }),
            f("Ligue as sombras", "🔗", { ligarSombras(m, 3, false) }, { ligarSombras(m, 4, false) }, { sombra() }),
            f("Marco do gelo", "🚩", { quadroQueFalta(m, 3) }, { quebra(m, 2, 3, fixas = 1) }, { ligarSombras(m, 3, true) }, { sombra() }),
            f("Quebra cabeça de 6", "🐧", { quebra(m, 2, 3) }, { quebra(m, 2, 3) }, { memoriaTema(m, 5) }),
            f("Memória polar", "🃏", { memoriaTema(m, 5) }, { memoriaTema(m, 5) }, { ligarSombras(m, 4, true) }, { quadroQueFalta(m, 3) }),
            f("Gelo e selva", "🐒", { quebra(m, 2, 3) }, { escondeTrocas(m, 3, 2) }, { robo(3, 3, 3..4) }, { ladoDoPersonagem(m) }),
            f("Polo difícil", "🧠", { ligarSombras(m, 5, true) }, { quebra(m, 2, 3) }, { matriz(m) }),
            f("Chefão do gelo", "🐻", { quebra(m, 2, 3) }, { ligarSombras(m, 4, true) }, { matriz(m) }, { quadroQueFalta(m, 3) }),
        )
        10 -> listOf( // Circo: simetria e espelho
            f("O espelho", "🪞", { espelhoGrade(m, 2) }, { setaGira(false) }, { pintarEspelho(6) }),
            f("Pintar o espelho", "🎨", { pintarEspelho(6) }, { pintarEspelho(6) }, { espelhoGrade(m, 2) }),
            f("Setas no espelho", "↔️", { setaGira(false) }, { espelhoGrade(m, 3) }, { domino(m, 2, 0, tipo = 0) }),
            f("Dominó das sombras", "🁡", { domino(m, 2, 1, tipo = 0) }, { domino(m, 3, 1, tipo = 0) }, { setaGira(false) }),
            f("Marco do circo", "🚩", { espelhoGrade(m, 3) }, { pintarEspelho(6) }, { domino(m, 3, 1) }, { setaGira(false) }),
            f("Cartas do circo", "🃏", { cartas(m, 4) }, { cartas(m, 4) }, { espelhoGrade(m, 3) }),
            f("Dominó do circo", "🎪", { domino(m, 3, 1) }, { domino(m, 3, 1) }, { cartas(m, 5) }, { setaGira(true) }),
            f("Circo e gelo", "🧊", { pintarEspelho(6) }, { quebra(m, 2, 3) }, { ligarSombras(m, 4, true) }, { quadroQueFalta(m, 3) }),
            f("Circo difícil", "🧠", { espelhoGrade(m, 4) }, { pintarEspelho(8) }, { cartas(m, 6) }, { setaGira(true) }),
            f("Chefão do circo", "🤡", { pintarEspelho(8) }, { domino(m, 4, 1) }, { espelhoGrade(m, 4) }, { setaGira(true) }),
        )
        11 -> listOf( // Deserto: sudoku 4 por 4 com 2 casas a descobrir
            f("Sudoku do deserto", "🧩", { sudokuUnico(4, 1) }, { sudokuUnico(4, 1) }, { filaDuasRegras(true) }, { escondeTrocas(m, 3, 2) }),
            f("Cadê o camelo?", "🐫", { escondeTrocas(m, 3, 3) }, { escondeTrocas(m, 3, 3) }, { sudokuUnico(4, 1) }),
            f("Duas casas", "🌵", { sudokuUnico(4, 2) }, { sudokuUnico(4, 2) }, { matriz(m) }, { siga(m, 3, 4) }),
            f("Siga o deserto", "☀️", { siga(m, 4, 4) }, { siga(m, 4, 4) }, { sudokuUnico(4, 2) }),
            f("Marco do deserto", "🚩", { sudokuUnico(4, 2) }, { escondeTrocas(m, 3, 3) }, { siga(m, 4, 4) }, { cestaVenn() }),
            f("Cesta do deserto", "🧺", { cestaVenn() }, { cestaVenn() }, { siga(m, 4, 4) }, { vennEscolha(0) }),
            f("Esconde ligeiro", "🦂", { escondeTrocas(m, 4, 3) }, { escondeTrocas(m, 4, 3) }, { cestaVenn() }, { sudokuUnico(4, 2) }),
            f("Deserto e circo", "🎪", { sudokuUnico(4, 2) }, { domino(m, 3, 1) }, { pintarEspelho(6) }, { espelhoGrade(m, 3) }),
            f("Deserto difícil", "🧠", { sudokuUnico(4, 4) }, { escondeTrocas(m, 4, 3) }, { siga(m, 4, 4) }),
            f("Chefão do deserto", "🐪", { sudokuUnico(4, 3) }, { escondeTrocas(m, 4, 3) }, { siga(m, 4, 4) }),
        )
        12 -> listOf( // Cidade: programar, sequência de comandos
            f("Onde o robô para?", "🏙️", { ondeORoboPara(m, 3, 2) }, { historia() }, { robo(3, 3, 2..3) }),
            f("Programe o robô", "🤖", { robo(3, 4, 3..4) }, { robo(4, 4, 3..4) }, { ondeORoboPara(m, 3, 2) }),
            f("Passo a passo", "🚦", { tremHistoria(3, false) }, { tremHistoria(3, false) }, { historia() }),
            f("Robô e prédios", "🏢", { robo(4, 4, 4..5, pedras = 3) }, { robo(4, 4, 4..5, pedras = 3) }, { tremHistoria(4, false) }),
            f("Marco da cidade", "🚩", { ondeORoboPara(m, 4, 3) }, { robo(4, 4, 4..5, pedras = 2) }, { tremHistoria(3, false) }, { historia() }),
            f("Caça na cidade", "🔍", { caca(m, false) }, { caca(m, false) }, { ondeORoboPara(m, 4, 3) }),
            f("Trem das tarefas", "🚂", { tremHistoria(4, true) }, { tremHistoria(4, true) }, { caca(m, false) }, { historia() }),
            f("Cidade e deserto", "🐫", { robo(4, 4, 4..5, pedras = 2) }, { sudokuUnico(4, 2) }, { escondeTrocas(m, 4, 3) }),
            f("Cidade difícil", "🧠", { robo(5, 5, 5..5, pedras = 4, folga = 0) }, { ondeORoboPara(m, 4, 4) }, { tremHistoria(4, true) }),
            f("Chefão da cidade", "🚌", { robo(4, 5, 4..5, pedras = 3) }, { tremHistoria(4, true) }, { caca(m, true) }),
        )
        13 -> listOf( // Castelo: se e então; detetive com duas pistas
            f("Se e então", "🏰", { seEntao(m, 2) }, { causaEfeito() }, { ligarCausas(3) }),
            f("Onde está o dragão?", "🐉", { escondePistas(m, 4) }, { escondePistas(m, 4) }, { seEntao(m, 2) }),
            f("Detetive do castelo", "🕵️", { detetive() }, { quemEQuem(2) }, { escondePistas(m, 4) }),
            f("Quem é quem", "👑", { ligarQuemEQuem(2) }, { ligarQuemEQuem(2) }, { detetive() }),
            f("Marco do castelo", "🚩", { seEntao(m, 3) }, { escondePistas(m, 4) }, { ligarCausas(3) }, { detetive() }),
            f("Cartas do castelo", "🃏", { cartas(m, 5) }, { cartas(m, 5) }, { seEntao(m, 3) }),
            f("Pistas e cartas", "🔑", { escondePistas(m, 5) }, { cartas(m, 5) }, { ligarCausas(3) }, { causaEfeito() }),
            f("Castelo e cidade", "🏙️", { detetive() }, { robo(4, 4, 4..5, pedras = 2) }, { tremHistoria(4, false) }, { ondeORoboPara(m, 4, 3) }),
            f("Castelo difícil", "🧠", { quemEQuem(3) }, { ligarQuemEQuem(3) }, { escondePistas(m, 6) }),
            f("Chefão do castelo", "🧙", { escondePistas(m, 5) }, { quemEQuem(3) }, { ligarQuemEQuem(3) }, { seEntao(m, 3) }),
        )
        14 -> listOf( // Vale dos Dinossauros: seriação de 5 e ordem da história
            f("Do menor ao maior", "🦕", { seriacao(m) }, { historia() }, { tremTamanho(m, 4, 1) }),
            f("Trem dos dinossauros", "🚂", { tremTamanho(m, 4, 1) }, { tremTamanho(m, 5, 1) }, { seriacao(m) }),
            f("O que veio antes", "🥚", { historia() }, { transitividade(false) }, { cartas(m, 4, tipo = 2) }),
            f("Balança dos dinos", "⚖️", { balanca(m, false) }, { balanca(m, false) }, { transitividade(false) }),
            f("Marco dos dinos", "🚩", { seriacao(m) }, { tremTamanho(m, 5, 1) }, { historia() }, { balanca(m, false) }),
            f("Cartas em ordem", "🃏", { cartas(m, 5, tipo = 2) }, { cartas(m, 5, tipo = 2) }, { historia() }),
            f("Trem da história", "📖", { tremHistoria(4, false) }, { tremHistoria(4, true) }, { cartas(m, 5, tipo = 2) }, { transitividade(false) }),
            f("Dinos e castelo", "🏰", { tremTamanho(m, 5, 0) }, { escondePistas(m, 5) }, { detetive() }, { robo(4, 4, 4..5, pedras = 2) }),
            f("Dinos difícil", "🧠", { transitividade(true) }, { tremTamanho(m, 5, 0) }, { balanca(m, true) }),
            f("Chefão dos dinos", "🦖", { tremTamanho(m, 5, 0) }, { balanca(m, true) }, { cartas(m, 5, tipo = 2) }, { transitividade(true) }),
        )
        15 -> listOf( // Ilha do Tesouro: mapas e coordenadas
            f("Mapa do tesouro", "🗺️", { coordenadas(m, 3, false) }, { ondeORoboPara(m, 3, 2) }, { caca(m, false) }),
            f("Caça ao tesouro", "💰", { caca(m, false) }, { caca(m, false) }, { coordenadas(m, 3, false) }),
            f("Onde está?", "📍", { coordenadas(m, 3, true) }, { coordenadas(m, 4, false) }, { robo(4, 4, 4..5, pedras = 2) }),
            f("Robô pirata", "🏴‍☠️", { robo(4, 4, 4..6, pedras = 3) }, { robo(4, 4, 4..6, pedras = 3) }, { coordenadas(m, 4, false) }),
            f("Marco da ilha", "🚩", { coordenadas(m, 4, random.nextBoolean()) }, { caca(m, false) }, { robo(4, 4, 4..6, pedras = 2) }, { pintarSimbolo(6, false) }),
            f("Pintar o mapa", "🎨", { pintarSimbolo(6, false) }, { pintarSimbolo(6, false) }, { coordenadas(m, 4, true) }),
            f("Caça esperta", "🦜", { caca(m, true) }, { caca(m, true) }, { robo(4, 4, 4..6, pedras = 3) }, { coordenadas(m, 3, true) }),
            f("Ilha e dinos", "🦕", { coordenadas(m, 4, false) }, { tremTamanho(m, 5, 0) }, { siga(m, 4, 4) }, { transitividade(true) }),
            f("Ilha difícil", "🧠", { coordenadas(m, 4, true) }, { robo(5, 5, 5..6, pedras = 5) }, { pintarSimbolo(8, true) }),
            f("Chefão da ilha", "⚓", { caca(m, true) }, { robo(5, 5, 5..6, pedras = 4) }, { pintarSimbolo(8, true) }, { coordenadas(m, 4, true) }),
        )
        16 -> listOf( // Fábrica de Doces: balança lógica com trocas
            f("Pesa igual", "⚖️", { balancaTrocas(1) }, { transitividade(false) }, { balanca(m, false) }),
            f("Balança de doces", "🍭", { balanca(m, false) }, { balanca(m, true) }, { balancaTrocas(1) }),
            f("Troca troca", "🔄", { balancaTrocas(2) }, { balancaTrocas(1) }, { intrusoVenn() }, { domino(m, 3, 1) }),
            f("Dominó de doces", "🁡", { domino(m, 3, 1) }, { domino(m, 4, 1) }, { balancaTrocas(1) }),
            f("Marco dos doces", "🚩", { balancaTrocas(1) }, { balanca(m, true) }, { domino(m, 3, 1) }, { transitividade(true) }),
            f("Cesta de doces", "🧺", { cestaVenn() }, { cestaVenn() }, { balancaTrocas(2) }),
            f("Balança difícil", "🍫", { balanca(m, true) }, { balanca(m, true) }, { domino(m, 4, 2) }, { vennEscolha(1) }),
            f("Doces e ilha", "🏝️", { balancaTrocas(2) }, { coordenadas(m, 4, true) }, { cartas(m, 5) }, { pintarSimbolo(6, true) }),
            f("Doces difícil", "🧠", { balancaTrocas(2) }, { domino(m, 5, 1) }, { cestaVenn() }),
            f("Chefão dos doces", "🎂", { balanca(m, true) }, { domino(m, 5, 2) }, { balancaTrocas(2) }, { transitividade(true) }),
        )
        17 -> listOf( // Bosque Encantado: memória de sequência até 5 e cartas viradas
            f("Fila encantada", "🍄", { filaCrescente(m, false) }, { filaDuasRegras(true) }, { siga(m, 4, 4) }),
            f("Siga as fadas", "🧚", { siga(m, 4, 4) }, { siga(m, 4, 5) }, { filaCrescente(m, false) }),
            f("Cartas encantadas", "🃏", { cartas(m, 6, tipo = 0) }, { cartas(m, 7, tipo = 0) }, { filaCrescente(m, true) }),
            f("Memória do bosque", "🦉", { memoriaTema(m, 6) }, { memoriaTema(m, 6) }, { siga(m, 4, 5) }),
            f("Marco do bosque", "🚩", { filaCrescente(m, true) }, { siga(m, 4, 5) }, { cartas(m, 7) }, { memoriaTema(m, 6) }),
            f("Siga de cinco", "🌸", { siga(m, 4, 5) }, { siga(m, 4, 5) }, { cartas(m, 7) }, { filaCrescente(m, true) }),
            f("Cartas do A ao H", "🔤", { cartas(m, 8, tipo = 1) }, { cartas(m, 8, tipo = 1) }, { memoriaTema(m, 6) }),
            f("Bosque e doces", "🍭", { siga(m, 4, 5) }, { balancaTrocas(2) }, { domino(m, 4, 1) }),
            f("Bosque difícil", "🧠", { siga(m, 4, 5) }, { cartas(m, 8, tipo = random.nextInt(2)) }, { memoriaTema(m, 6) }, { filaCrescente(m, true) }),
            f("Chefão do bosque", "🦄", { siga(m, 4, 5) }, { cartas(m, 8, tipo = random.nextInt(2)) }, { memoriaTema(m, 6) }),
        )
        18 -> listOf( // Vulcão: programar com repetição
            f("Repete, repete", "🔁", { ondeORoboPara(m, 4, 2, repeticao = true) }, { filaCrescente(m, true) }, { robo(4, 4, 4..5, pedras = 1, repetir = true) }),
            f("Robô no vulcão", "🤖", { robo(4, 4, 4..6, pedras = 2, repetir = true) }, { robo(4, 4, 4..6, pedras = 2, repetir = true) }, { ondeORoboPara(m, 4, 2, repeticao = true) }),
            f("Trem do vulcão", "🚂", { tremTamanho(m, 6, 1) }, { tremHistoria(4, true) }, { ondeORoboPara(m, 4, 3, repeticao = true) }),
            f("Quebra cabeça de 9", "🧩", { quebra(m, 3, 3, numeros = true) }, { quebra(m, 3, 3, fixas = 2) }, { robo(4, 4, 4..6, pedras = 2, repetir = true) }),
            f("Marco do vulcão", "🚩", { ondeORoboPara(m, 4, 3, repeticao = true) }, { robo(4, 5, 5..6, pedras = 3, repetir = true) }, { quebra(m, 3, 3, fixas = 1) }, { tremTamanho(m, 6, 1) }),
            f("Robô maior", "🌋", { robo(5, 5, 5..6, pedras = 3, repetir = true) }, { robo(5, 5, 5..6, pedras = 3, repetir = true) }, { tremHistoria(4, true) }),
            f("Lava e pedras", "🔥", { quebra(m, 3, 3) }, { quebra(m, 3, 3) }, { ondeORoboPara(m, 5, 3, repeticao = true) }),
            f("Vulcão e bosque", "🍄", { robo(5, 5, 5..6, pedras = 4, repetir = true) }, { siga(m, 4, 5) }, { cartas(m, 7) }, { filaCrescente(m, true) }),
            f("Vulcão difícil", "🧠", { robo(5, 5, 6..6, pedras = 5, repetir = true, folga = 0) }, { quebra(m, 3, 3) }, { tremTamanho(m, 6, 0) }, { ondeORoboPara(m, 5, 4, repeticao = true) }),
            f("Chefão do vulcão", "🐉", { robo(5, 5, 6..6, pedras = 4, repetir = true) }, { quebra(m, 3, 3) }, { tremHistoria(4, true) }),
        )
        19 -> listOf( // Reino das Nuvens: padrões de crescimento e matriz 3 por 3
            f("Matriz das nuvens", "☁️", { matriz(m, quantidade = false) }, { filaCrescente(m, false) }, { pintarSimbolo(6, false) }),
            f("Pintar o céu", "🎨", { pintarSimbolo(6, false) }, { pintarSimbolo(6, true) }, { matriz(m) }),
            f("Fila que cresce", "📈", { filaCrescente(m, true) }, { matriz(m, quantidade = true) }, { tremCrescente(m, 4) }),
            f("Trem que cresce", "🚂", { tremCrescente(m, 4) }, { tremCrescente(m, 5) }, { filaCrescente(m, true) }),
            f("Marco das nuvens", "🚩", { matriz(m) }, { pintarSimbolo(6, true) }, { tremCrescente(m, 5) }, { filaCrescente(m, true) }),
            f("Ligue as cores", "🔗", { ligarCores(4) }, { ligarCores(5) }, { matriz(m) }),
            f("Pintar padrões", "🖌️", { pintarSimbolo(8, true) }, { ligarCores(5) }, { filaDuasRegras(true) }),
            f("Nuvens e vulcão", "🌋", { matriz(m) }, { robo(5, 5, 5..6, pedras = 3, repetir = true) }, { quebra(m, 3, 3) }),
            f("Nuvens difícil", "🧠", { matriz(m) }, { pintarSimbolo(8, true) }, { tremCrescente(m, 6) }, { filaDuasRegras(true) }),
            f("Chefão das nuvens", "🌈", { matriz(m) }, { pintarSimbolo(8, true) }, { tremCrescente(m, 6) }, { filaCrescente(m, true) }),
        )
        20 -> listOf( // Caverna de Cristal: quebra cabeça de 9 a 12 peças, encaixar formas
            f("Encaixe a forma", "🔷", { sombra() }, { matriz(m) }, { quebra(m, 3, 3, fixas = 3) }),
            f("Quebra cabeça de cristal", "💎", { quebra(m, 3, 3) }, { quebra(m, 3, 3) }, { sombra() }),
            f("Caça aos cristais", "🔦", { caca(m, false) }, { caca(m, false) }, { sombra() }, { matriz(m) }),
            f("Dominó de cristal", "🁡", { domino(m, 4, 1) }, { domino(m, 4, 1) }, { quebra(m, 3, 3) }),
            f("Marco da caverna", "🚩", { quebra(m, 3, 3) }, { caca(m, true) }, { domino(m, 4, 1) }, { sombra() }),
            f("Quebra cabeça de 12", "🧩", { quebra(m, 3, 4, fixas = 2) }, { quebra(m, 3, 4, fixas = 1) }, { domino(m, 4, 2) }),
            f("Caça difícil", "🦇", { caca(m, true) }, { caca(m, true) }, { quebra(m, 3, 3) }, { matriz(m) }),
            f("Caverna e nuvens", "☁️", { quebra(m, 3, 4) }, { robo(5, 5, 5..6, pedras = 3, repetir = true) }, { matriz(m) }, { pintarSimbolo(8, true) }),
            f("Caverna difícil", "🧠", { quebra(m, 3, 4) }, { domino(m, 5, 2) }, { caca(m, true) }, { sombra() }),
            f("Chefão da caverna", "🔮", { quebra(m, 3, 4) }, { domino(m, 5, 2) }, { caca(m, true) }),
        )
        21 -> listOf( // Parque de Diversões: quem é quem e intrusos com 3 critérios
            f("Quem é quem", "🎡", { quemEQuem(2) }, { tresCriterios() }, { ligarQuemEQuem(2) }),
            f("Ligue quem é quem", "🔗", { ligarQuemEQuem(3) }, { ligarQuemEQuem(2) }, { quemEQuem(2) }),
            f("Três coisas ao mesmo tempo", "🎯", { tresCriterios() }, { detetive() }, { escondePistas(m, 5) }),
            f("Esconde no parque", "🎠", { escondeTrocas(m, 4, 5) }, { escondeTrocas(m, 4, 4) }, { tresCriterios() }),
            f("Marco do parque", "🚩", { quemEQuem(3) }, { ligarQuemEQuem(3) }, { escondeTrocas(m, 4, 5) }, { tresCriterios() }),
            f("Cesta de duas regras", "🧺", { cestaVenn() }, { cestaVenn() }, { quemEQuem(3) }),
            f("Esconde esperto", "🎢", { escondeTrocas(m, 5, 5) }, { escondeTrocas(m, 5, 5) }, { cestaVenn() }, { tresCriterios() }),
            f("Parque e caverna", "💎", { quemEQuem(3) }, { quebra(m, 3, 4) }, { domino(m, 5, 1) }, { tresCriterios() }),
            f("Parque difícil", "🧠", { quemEQuem(3) }, { ligarQuemEQuem(3) }, { escondeTrocas(m, 5, 5) }),
            f("Chefão do parque", "🤡", { ligarQuemEQuem(3) }, { escondeTrocas(m, 5, 5) }, { quemEQuem(3) }, { tresCriterios() }),
        )
        22 -> listOf( // Cidade dos Robôs: programar com obstáculos e mais de um caminho
            f("O caminho mais curto", "🔋", { menorCaminho(4, 4) }, { ondeORoboPara(m, 4, 4) }, { robo(5, 5, 5..6, pedras = 4) }),
            f("Robô e obstáculos", "🤖", { robo(5, 5, 6..8, pedras = 5, folga = 0) }, { robo(5, 5, 6..8, pedras = 5, folga = 0) }, { menorCaminho(4, 4) }),
            f("Siga o robô", "💡", { siga(m, 4, 6) }, { siga(m, 4, 5) }, { ondeORoboPara(m, 5, 4) }),
            f("Trem de peças", "🚂", { tremCrescente(m, 5) }, { tremTamanho(m, 6, 0) }, { robo(5, 5, 6..8, pedras = 5) }),
            f("Marco dos robôs", "🚩", { menorCaminho(5, 6) }, { robo(5, 5, 6..8, pedras = 6) }, { siga(m, 4, 6) }, { tremTamanho(m, 6, 0) }),
            f("Robô esperto", "⚙️", { robo(5, 5, 7..8, pedras = 6, repetir = true) }, { robo(5, 5, 7..8, pedras = 6, repetir = true) }, { siga(m, 4, 6) }),
            f("Siga de seis", "📡", { siga(m, 4, 6) }, { siga(m, 4, 6) }, { tremCrescente(m, 6) }, { menorCaminho(5, 6) }),
            f("Robôs e parque", "🎡", { robo(5, 5, 6..8, pedras = 5) }, { quemEQuem(3) }, { pintarSimbolo(8, true) }, { tresCriterios() }),
            f("Robôs difícil", "🧠", { robo(5, 5, 8..8, pedras = 6, folga = 0) }, { menorCaminho(5, 7) }, { siga(m, 4, 6) }),
            f("Chefão dos robôs", "🦾", { robo(5, 5, 7..8, pedras = 6) }, { siga(m, 4, 6) }, { tremTamanho(m, 6, 0) }, { menorCaminho(5, 7) }),
        )
        23 -> listOf( // Savana: diagramas de grupos
            f("Nos dois grupos", "🦁", { vennEscolha(0) }, { intrusoVenn() }, { cestaVenn() }),
            f("Cesta dos dois grupos", "🧺", { cestaVenn() }, { cestaVenn() }, { vennEscolha(0) }),
            f("Só de um grupo", "🦒", { vennEscolha(1) }, { vennEscolha(0) }, { caca(m, true) }),
            f("Ligue à casa", "🔗", { ligarLugar(3) }, { ligarLugar(4) }, { vennEscolha(1) }),
            f("Marco da savana", "🚩", { vennEscolha() }, { cestaVenn() }, { ligarLugar(4) }, { intrusoVenn() }),
            f("Caça na savana", "🔍", { caca(m, true) }, { caca(m, true) }, { vennEscolha(1) }),
            f("Fora dos grupos", "🦓", { vennEscolha(2) }, { ligarLugar(4) }, { vennEscolha(1) }),
            f("Savana e robôs", "🤖", { cestaVenn() }, { robo(5, 5, 6..8, pedras = 5) }, { siga(m, 4, 6) }, { menorCaminho(5, 6) }),
            f("Savana difícil", "🧠", { vennEscolha() }, { cestaVenn() }, { caca(m, true) }, { pescaria(true) }),
            f("Chefão da savana", "🐘", { cestaVenn() }, { caca(m, true) }, { ligarLugar(4) }, { vennEscolha() }),
        )
        24 -> listOf( // Vale das Cores: sudoku com várias casas e matriz de cor e forma
            f("Sudoku colorido", "🌈", { sudokuUnico(4, 4) }, { matriz(m, quantidade = false) }, { pintarSimbolo(8, true) }),
            f("Pintar as cores", "🎨", { pintarSimbolo(8, true) }, { pintarSimbolo(8, true) }, { sudokuUnico(4, 4) }),
            f("Sudoku de seis", "🧩", { sudokuUnico(6, 2) }, { sudokuUnico(6, 3) }, { matriz(m) }, { cartas(m, 6, tipo = 3) }),
            f("Quebra cabeça de 16", "🖼️", { quebra(m, 4, 4, fixas = 4) }, { quebra(m, 4, 4, numeros = true) }, { sudokuUnico(4, 5) }),
            f("Marco das cores", "🚩", { sudokuUnico(4, 5) }, { pintarSimbolo(8, true) }, { quebra(m, 4, 4, fixas = 3) }, { matriz(m) }),
            f("Cartas coloridas", "🃏", { cartas(m, 6, tipo = 3) }, { tremArcoIris(6) }, { matriz(m) }),
            f("Pintar a matriz", "🖍️", { pintarEspelho(8) }, { cartas(m, 6, tipo = 3) }, { sudokuUnico(6, 3) }),
            f("Cores e savana", "🦁", { sudokuUnico(4, 5) }, { vennEscolha() }, { cestaVenn() }, { robo(5, 5, 6..8, pedras = 5) }),
            f("Cores difícil", "🧠", { sudokuUnico(6, 5) }, { quebra(m, 4, 4, fixas = 2) }, { pintarSimbolo(8, true) }),
            f("Chefão das cores", "🎨", { sudokuUnico(6, 4) }, { pintarEspelho(8) }, { quebra(m, 4, 4, fixas = 2) }, { sudokuUnico(4, 6) }),
        )
        25 -> listOf( // Castelo das Estrelas: revisão de tudo
            f("Estrelas e padrões", "🌟", { filaDuasRegras(true) }, { matriz(m) }, { filaCrescente(m, true) }, { tremCrescente(m, 6) }),
            f("Robô das estrelas", "🚀", { robo(5, 5, 6..8, pedras = 5, repetir = true) }, { robo(5, 5, 6..8, pedras = 5) }, { ondeORoboPara(m, 5, 4, repeticao = true) }),
            f("Siga as estrelas", "✨", { siga(m, 4, 7) }, { siga(m, 4, 6) }, { labirintoGrande(5, 9..11) }),
            f("Quebra cabeça estrelado", "🧩", { quebra(m, 4, 4) }, { quebra(m, 4, 4, fixas = 2) }, { sudokuUnico(6, 4) }),
            f("Marco das estrelas", "🚩", { balancaTrocas(2) }, { siga(m, 4, 7) }, { quebra(m, 4, 4) }, { quemEQuem(3) }),
            f("Balança das estrelas", "⚖️", { balanca(m, true) }, { balanca(m, true) }, { balancaTrocas(2) }, { transitividade(true) }),
            f("Detetive das estrelas", "🔭", { quemEQuem(3) }, { escondePistas(m, 6) }, { transitividade(true) }, { detetive() }),
            f("Labirinto gigante", "🌀", { labirintoGrande(6, 12..14) }, { cartas(m, 8) }, { sudokuUnico(6, 5) }, { ligarCausas(4) }),
            f("Estrelas difícil", "🧠", { labirintoGrande(6, 13..15) }, { robo(6, 6, 8..8, pedras = 8, folga = 0) }, { siga(m, 4, 7) }, { menorCaminho(6, 9) }),
            grandeChefao(m),
        )
        else -> error("Mundo $m fora de 6..25")
    }

    /** Fase 250: uma pergunta de cada faixa de cinco mundos, em rodízio. */
    private fun grandeChefao(m: Int): ModeloDeFase {
        var vez = 0
        val faixas: List<List<() -> Pergunta>> = listOf(
            listOf({ ate50[49].criar() }, { ate50[47].criar() }),
            listOf({ quebra(m, 2, 3) }, { pintarEspelho(6) }, { domino(m, 3, 1) }, { siga(m, 3, 3) }, { ligarSombras(m, 4, true) }),
            listOf({ sudokuUnico(4, 3) }, { robo(4, 4, 4..6, pedras = 3) }, { coordenadas(m, 4, true) }, { tremTamanho(m, 5, 0) }, { escondePistas(m, 5) }),
            listOf({ balancaTrocas(2) }, { siga(m, 4, 5) }, { matriz(m) }, { quebra(m, 3, 3) }, { cartas(m, 8) }),
            listOf({ quemEQuem(3) }, { vennEscolha() }, { robo(5, 5, 6..8, pedras = 5, repetir = true) }, { sudokuUnico(6, 4) }, { quebra(m, 4, 4) }),
        )
        return ModeloDeFase("Grande chefão da lógica", "🏆") { faixas[vez++ % faixas.size].um()() }
    }

    companion object {
        /** Um programa com o menor número de setas que leva o robô do início à chegada (para conferir a fase). */
        fun programaDoRobo(r: ProgramarRobo): List<DirecaoDaSeta> {
            val total = r.linhas * r.colunas
            val veioDe = IntArray(total) { -1 }
            val passo = arrayOfNulls<DirecaoDaSeta>(total)
            val fila = ArrayDeque(listOf(r.inicio))
            veioDe[r.inicio] = r.inicio
            val movimentos = listOf(DirecaoDaSeta.CIMA to (-1 to 0), DirecaoDaSeta.BAIXO to (1 to 0), DirecaoDaSeta.ESQUERDA to (0 to -1), DirecaoDaSeta.DIREITA to (0 to 1))
            while (fila.isNotEmpty()) {
                val atual = fila.removeFirst()
                for ((seta, d) in movimentos) {
                    val l = atual / r.colunas + d.first
                    val c = atual % r.colunas + d.second
                    if (l !in 0 until r.linhas || c !in 0 until r.colunas) continue
                    val n = l * r.colunas + c
                    if (n in r.obstaculos || veioDe[n] >= 0) continue
                    veioDe[n] = atual
                    passo[n] = seta
                    fila.addLast(n)
                }
            }
            val programa = mutableListOf<DirecaoDaSeta>()
            var p = r.chegada
            while (p != r.inicio) {
                programa += passo[p] ?: return emptyList()
                p = veioDe[p]
            }
            return programa.reversed()
        }
    }
}
