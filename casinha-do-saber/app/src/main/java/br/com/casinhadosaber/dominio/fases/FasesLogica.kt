package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.*
import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Fases do caminho Lógica: 250 fases em 25 mundos de 10 ([Mundos]).
 * As fases 1 a 50 são as de sempre (o progresso salvo continua valendo). As fases 51 a 250 ainda
 * são provisórias ([provisoria]) até cada mundo novo ganhar o conteúdo do plano (docs/plano-1000-fases.md).
 */
class FasesLogica(random: Random = Random.Default) : FasesDoCaminho(random) {

    override val trilha = Trilha.LOGICA

    override fun fase(numero: Int): ModeloDeFase {
        conferir(numero)
        return if (numero <= FASES_PRONTAS) ate50[numero - 1] else provisoria(numero, ate50)
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
}
