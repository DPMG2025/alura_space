package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Cria as perguntas de cada fase. As perguntas são sorteadas a cada partida,
 * então a criança pode repetir uma fase sem decorar a ordem das respostas.
 *
 * Cada trilha tem [FASES_POR_TRILHA] fases, da mais fácil para a mais difícil.
 */
class GeradorDeFases(private val random: Random = Random.Default) {

    private class Modelo(val titulo: String, val emoji: String, val criar: () -> Pergunta)

    fun fases(trilha: Trilha): List<Fase> =
        modelos(trilha).mapIndexed { i, m -> Fase(trilha, i, m.titulo, m.emoji) }

    fun perguntas(fase: Fase, quantidade: Int = PERGUNTAS_POR_FASE): List<Pergunta> {
        val modelo = modelos(fase.trilha)[fase.numero]
        val lista = mutableListOf<Pergunta>()
        val chaves = mutableSetOf<String>()
        var tentativas = 0
        while (lista.size < quantidade && tentativas < 400) {
            tentativas++
            val p = modelo.criar()
            val resposta = p.opcoes[p.indiceCorreto].visual
            val chave = p.instrucao + "|" + chaveDaCena(p.cena) + "|" + resposta
            val repetida = chave in chaves
            val mesmaRespostaSeguida = lista.lastOrNull()?.let { it.opcoes[it.indiceCorreto].visual } == resposta
            // Primeiro tenta perguntas inéditas; fases com poucas variações (3 vogais, por exemplo)
            // podem repetir, mas nunca a mesma resposta duas vezes seguidas.
            val aceitar = when {
                tentativas <= 100 -> !repetida && !mesmaRespostaSeguida
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

    private fun modelos(trilha: Trilha): List<Modelo> = when (trilha) {
        Trilha.MATEMATICA -> matematica
        Trilha.PORTUGUES -> portugues
        Trilha.INGLES -> ingles
        Trilha.LOGICA -> logica
    }

    // ------------------------------------------------------------------ utilidades

    private fun <T> List<T>.sorteia(n: Int): List<T> = shuffled(random).take(n)
    private fun <T> List<T>.um(): T = random(random)

    /** Monta a pergunta embaralhando as opções e guardando onde ficou a certa. */
    private fun montar(
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
    private fun numerosErrados(correto: Int, min: Int, max: Int, qtd: Int): List<Int> {
        val perto = (min..max).filter { it != correto }.sortedBy { kotlin.math.abs(it - correto) + random.nextDouble() }
        return perto.take(qtd).shuffled(random)
    }

    private fun opcaoNumero(n: Int) = Opcao(Visual.Texto(n.toString()), listOf(pt(porExtenso(n))))

    // ------------------------------------------------------------------ MATEMÁTICA

    private fun contar(min: Int, max: Int, opcoes: Int): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val n = (min..max).random(random)
        val qual = if (obj.feminino) "Quantas" else "Quantos"
        return montar(
            instrucao = "$qual ${obj.plural} você vê?",
            fala = listOf(pt("$qual ${obj.plural} você vê? Conte comigo!")),
            cena = Cena.Emojis(obj.emoji.repeat(n)),
            certa = opcaoNumero(n),
            erradas = numerosErrados(n, 1, maxOf(max, opcoes + 1), opcoes - 1).map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(n, obj.feminino)} ${obj.nome(n)}!")),
        )
    }

    private fun acharQuantidade(): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val n = (1..5).random(random)
        val grupo = { k: Int -> Opcao(Visual.Emoji(obj.emoji.repeat(k)), listOf(pt(porExtenso(k, obj.feminino)))) }
        return montar(
            instrucao = "Toque onde tem $n",
            fala = listOf(pt("Toque onde tem ${porExtenso(n, obj.feminino)} ${obj.nome(n)}.")),
            cena = Cena.Palavra(n.toString()),
            certa = grupo(n),
            erradas = numerosErrados(n, 1, 5, 2).map(grupo),
            reforco = listOf(pt("Isso! ${porExtenso(n, obj.feminino)}!")),
        )
    }

    private fun maisOuMenos(): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val mais = random.nextBoolean()
        val qtds = (1..6).toList().sorteia(3)
        val certo = if (mais) qtds.max() else qtds.min()
        val grupo = { k: Int -> Opcao(Visual.Emoji(obj.emoji.repeat(k)), listOf(pt(porExtenso(k, obj.feminino)))) }
        val palavra = if (mais) "mais" else "menos"
        return montar(
            instrucao = "Onde tem $palavra?",
            fala = listOf(pt("Onde tem $palavra ${obj.plural}?")),
            cena = Cena.Nenhuma,
            certa = grupo(certo),
            erradas = qtds.filter { it != certo }.map(grupo),
            reforco = listOf(pt("Aqui tem ${porExtenso(certo, obj.feminino)}. É o que tem $palavra!")),
        )
    }

    private fun somar(maxTotal: Int, opcoes: Int): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val a = (1 until maxTotal).random(random)
        val b = (1..(maxTotal - a)).random(random)
        val total = a + b
        val f = obj.feminino
        return montar(
            instrucao = "$a + $b = ?",
            fala = listOf(pt("${porExtenso(a, f)} ${obj.nome(a)} mais ${porExtenso(b, f)}. ${if (f) "Quantas" else "Quantos"} ficam juntos?")),
            cena = Cena.Conta(obj.emoji, a, b, soma = true),
            certa = opcaoNumero(total),
            erradas = numerosErrados(total, 1, maxOf(maxTotal, opcoes + 1), opcoes - 1).map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(a)} mais ${porExtenso(b)} é igual a ${porExtenso(total)}!")),
        )
    }

    private fun subtrair(maxInicio: Int, opcoes: Int): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val a = (2..maxInicio).random(random)
        val b = (1 until a).random(random).coerceAtMost(5)
        val resto = a - b
        val f = obj.feminino
        val quantos = if (f) "Quantas" else "Quantos"
        return montar(
            instrucao = "$a − $b = ?",
            fala = listOf(
                pt("Tinha ${porExtenso(a, f)} ${obj.nome(a)}. Foram embora ${porExtenso(b, f)}. $quantos ficaram?"),
            ),
            cena = Cena.Conta(obj.emoji, a, b, soma = false),
            certa = opcaoNumero(resto),
            erradas = numerosErrados(resto, 0, maxOf(maxInicio, opcoes), opcoes - 1).map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(a)} menos ${porExtenso(b)} é igual a ${porExtenso(resto)}!")),
        )
    }

    private val matematica: List<Modelo> = listOf(
        Modelo("Contar até 3", "🍎") { contar(1, 3, 3) },
        Modelo("Contar até 5", "⭐") { contar(1, 5, 3) },
        Modelo("Achar a quantidade", "👆") { acharQuantidade() },
        Modelo("Contar até 10", "🔟") { contar(4, 10, 3) },
        Modelo("Mais ou menos", "⚖️") { maisOuMenos() },
        Modelo("Somar até 5", "➕") { somar(5, 3) },
        Modelo("Tirar até 5", "➖") { subtrair(5, 3) },
        Modelo("Somar até 10", "🧮") { somar(10, 4) },
        Modelo("Tirar até 10", "🎈") { subtrair(10, 4) },
        Modelo("Desafio dos números", "🏆") {
            when (random.nextInt(4)) {
                0 -> contar(5, 10, 4)
                1 -> somar(10, 4)
                2 -> subtrair(10, 4)
                else -> maisOuMenos()
            }
        },
    )

    // ------------------------------------------------------------------ PORTUGUÊS

    private fun opcaoLetra(l: String) = Opcao(Visual.Texto(l), listOf(pt(falaDaLetra(l))))

    private fun tocarNaLetra(conjunto: List<String>, opcoes: Int): Pergunta {
        val letra = conjunto.um()
        return montar(
            instrucao = "Toque na letra $letra",
            fala = listOf(pt("Toque na letra ${falaDaLetra(letra)}.")),
            cena = Cena.Nenhuma,
            certa = opcaoLetra(letra),
            erradas = conjunto.filter { it != letra }.sorteia(opcoes - 1).map(::opcaoLetra),
            reforco = listOf(pt("Essa é a letra ${falaDaLetra(letra)}!")),
        )
    }

    private fun letraInicial(palavras: List<Palavra>, letrasPossiveis: List<String>): Pergunta {
        val p = palavras.um()
        val erradas = letrasPossiveis.filter { it != p.inicial }.sorteia(2)
        return montar(
            instrucao = "Com que letra começa?",
            fala = listOf(pt("${p.texto.lowercase()}. Com que letra começa ${p.texto.lowercase()}?")),
            cena = Cena.Emojis(p.emoji),
            certa = opcaoLetra(p.inicial),
            erradas = erradas.map(::opcaoLetra),
            reforco = listOf(pt("${falaDaLetra(p.inicial)}, de ${p.texto.lowercase()}!")),
        )
    }

    /** Mostra a palavra escrita, sem falar qual é: a criança usa a primeira letra como pista. */
    private fun lerPalavra(): Pergunta {
        val certa = Vocabulario.palavrasComConsoante.um()
        val erradas = Vocabulario.todasAsPalavras
            .filter { it.inicial != certa.inicial }
            .distinctBy { it.inicial }
            .sorteia(2)
        val op = { p: Palavra -> Opcao(Visual.Emoji(p.emoji), listOf(pt(p.texto.lowercase()))) }
        return montar(
            instrucao = "Que figura é esta palavra?",
            fala = listOf(pt("Olhe a palavra. Qual figura combina com ela? Dica: veja a primeira letra.")),
            cena = Cena.Palavra(certa.texto),
            certa = op(certa),
            erradas = erradas.map(op),
            reforco = listOf(pt("Aqui está escrito ${certa.texto.lowercase()}!")),
        )
    }

    private fun silabaInicial(): Pergunta {
        val p = Vocabulario.todasAsPalavras.filter { it.silabas.size >= 2 }.um()
        val primeira = p.silabas.first()
        val erradas = Vocabulario.todasAsPalavras
            .map { it.silabas.first() }
            .distinct()
            .filter { it != primeira && it.first() != primeira.first() }
            .sorteia(2)
        val op = { s: String -> Opcao(Visual.Texto(s), listOf(pt(s.lowercase()))) }
        return montar(
            instrucao = "Com que pedacinho começa?",
            fala = listOf(pt("${p.texto.lowercase()}. Com que pedacinho começa ${p.texto.lowercase()}?")),
            cena = Cena.Emojis(p.emoji),
            certa = op(primeira),
            erradas = erradas.map(op),
            reforco = listOf(pt(p.silabas.joinToString(", ") { it.lowercase() } + ". " + p.texto.lowercase() + "!")),
        )
    }

    private fun completarPalavra(): Pergunta {
        val p = Vocabulario.palavrasComConsoante.um()
        val erradas = Vocabulario.consoantes.filter { it != p.inicial }.sorteia(3)
        return montar(
            instrucao = "Que letra está faltando?",
            fala = listOf(pt("${p.texto.lowercase()}. Que letra está faltando?")),
            cena = Cena.Palavra(p.emoji + " _" + p.texto.drop(1)),
            certa = opcaoLetra(p.inicial),
            erradas = erradas.map(::opcaoLetra),
            reforco = listOf(pt("${falaDaLetra(p.inicial)}! ${p.texto.lowercase()}!")),
        )
    }

    private fun rima(): Pergunta {
        val comRima = Vocabulario.todasAsPalavras.filter { it.rima != null }
        val base = comRima.um()
        val certa = comRima.filter { it.rima == base.rima && it != base }.um()
        val erradas = comRima.filter { it.rima != base.rima }.distinctBy { it.rima }.sorteia(2)
        val op = { p: Palavra -> Opcao(Visual.Emoji(p.emoji), listOf(pt(p.texto.lowercase()))) }
        return montar(
            instrucao = "O que rima com ${base.texto}?",
            fala = listOf(pt("O que rima com ${base.texto.lowercase()}?")),
            cena = Cena.Emojis(base.emoji),
            certa = op(certa),
            erradas = erradas.map(op),
            reforco = listOf(pt("${base.texto.lowercase()} e ${certa.texto.lowercase()}! Rimou!")),
        )
    }

    private val portugues: List<Modelo> = listOf(
        Modelo("Vogais A, E, I", "🅰️") { tocarNaLetra(listOf("A", "E", "I"), 3) },
        Modelo("Todas as vogais", "🔤") { tocarNaLetra(Vocabulario.vogais, 4) },
        Modelo("Começa com vogal", "🐝") { letraInicial(Vocabulario.palavrasComVogal, Vocabulario.vogais) },
        Modelo("Outras letras", "🅱️") { tocarNaLetra(Vocabulario.consoantes, 3) },
        Modelo("Primeira letra", "🐱") {
            letraInicial(Vocabulario.palavrasComConsoante, Vocabulario.consoantes)
        },
        Modelo("Lendo palavras", "📖") { lerPalavra() },
        Modelo("Pedacinhos", "🧩") { silabaInicial() },
        Modelo("Complete a palavra", "✏️") { completarPalavra() },
        Modelo("Rimas", "🎵") { rima() },
        Modelo("Desafio das letras", "🏆") {
            when (random.nextInt(4)) {
                0 -> lerPalavra()
                1 -> silabaInicial()
                2 -> completarPalavra()
                else -> rima()
            }
        },
    )

    // ------------------------------------------------------------------ INGLÊS

    private fun visualEn(p: PalavraEn): Visual =
        if (p.cor != null) Visual.Cor(p.cor) else Visual.Emoji(p.emoji)

    private fun encontreEmIngles(lista: List<PalavraEn>, opcoes: Int, dica: String = "Encontre"): Pergunta {
        val certa = lista.um()
        val erradas = lista.filter { it.en != certa.en }.sorteia(opcoes - 1)
        val op = { p: PalavraEn -> Opcao(visualEn(p), listOf(en(p.en))) }
        return montar(
            instrucao = "$dica: ${certa.en}",
            fala = listOf(pt("$dica:"), en(certa.en)),
            cena = Cena.Palavra(certa.en),
            certa = op(certa),
            erradas = erradas.map(op),
            reforco = listOf(en(certa.en.replaceFirstChar { it.uppercase() } + "!"), pt("Em português, ${certa.pt}.")),
        )
    }

    private fun numeroEmIngles(faixaCerta: IntRange, faixaOpcoes: IntRange, opcoes: Int): Pergunta {
        val n = faixaCerta.random(random)
        val erradas = numerosErrados(n, faixaOpcoes.first, faixaOpcoes.last, opcoes - 1)
        val op = { k: Int -> Opcao(Visual.Texto(k.toString()), listOf(en(Vocabulario.numerosEn[k]))) }
        val palavra = Vocabulario.numerosEn[n]
        return montar(
            instrucao = "Encontre: $palavra",
            fala = listOf(pt("Encontre o número"), en(palavra)),
            cena = Cena.Palavra(palavra),
            certa = op(n),
            erradas = erradas.map(op),
            reforco = listOf(en(palavra.replaceFirstChar { it.uppercase() } + "!"), pt("${porExtenso(n)}!")),
        )
    }

    private val ingles: List<Modelo> = listOf(
        Modelo("Colors", "🎨") { encontreEmIngles(Vocabulario.coresBasicas, 3, "Toque na cor") },
        Modelo("Animals", "🐶") { encontreEmIngles(Vocabulario.animais.take(6), 3) },
        Modelo("Numbers 1 a 5", "🖐️") { numeroEmIngles(1..5, 1..5, 3) },
        Modelo("Fruits", "🍎") { encontreEmIngles(Vocabulario.frutas, 3) },
        Modelo("More colors", "🌈") { encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor") },
        Modelo("My body", "👂") { encontreEmIngles(Vocabulario.corpo, 4) },
        Modelo("Numbers 6 a 10", "🔟") { numeroEmIngles(6..10, 1..10, 4) },
        Modelo("My house", "🏠") { encontreEmIngles(Vocabulario.casa, 4) },
        Modelo("Toys", "🧸") { encontreEmIngles(Vocabulario.brinquedos + Vocabulario.animais.drop(6), 4) },
        Modelo("English challenge", "🏆") {
            when (random.nextInt(5)) {
                0 -> encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor")
                1 -> encontreEmIngles(Vocabulario.animais, 4)
                2 -> numeroEmIngles(1..10, 1..10, 4)
                3 -> encontreEmIngles(Vocabulario.casa + Vocabulario.brinquedos, 4)
                else -> encontreEmIngles(Vocabulario.frutas + Vocabulario.corpo, 4)
            }
        },
    )

    // ------------------------------------------------------------------ LÓGICA

    private fun opEmoji(e: String, escala: Float = 1f) = Opcao(Visual.Emoji(e, escala))

    private fun igual(): Pergunta {
        val (certa, e1, e2) = Vocabulario.figurasSimples.sorteia(3)
        return montar(
            instrucao = "Toque no igual",
            fala = listOf(pt("Toque na figura igual a esta.")),
            cena = Cena.Emojis(certa),
            certa = opEmoji(certa),
            erradas = listOf(opEmoji(e1), opEmoji(e2)),
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
        val escalas = listOf(0.5f, 0.8f, 1.2f)
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
        val outras = (figuras + Vocabulario.figurasDeSequencia.filter { it !in figuras }.sorteia(1))
            .distinct()
            .filter { it != resposta }
            .take(2)
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
        val erradas = Vocabulario.pares.filter { it != par }.sorteia(2)
        return montar(
            instrucao = "O que combina?",
            fala = listOf(pt("O que combina com ${par.nomeDe}?")),
            cena = Cena.Emojis(par.de),
            certa = Opcao(Visual.Emoji(par.para), listOf(pt(par.nomePara))),
            erradas = erradas.map { Opcao(Visual.Emoji(it.para), listOf(pt(it.nomePara))) },
            reforco = listOf(pt("${par.nomeDe} combina com ${par.nomePara}!")),
        )
    }

    private fun grupo(): Pergunta {
        val g = Vocabulario.grupos.um()
        val certa = g.itens.um()
        val erradas = Vocabulario.grupos.filter { it != g }.sorteia(2).map { it.itens.um() }
        return montar(
            instrucao = g.pergunta,
            fala = listOf(pt(g.pergunta)),
            cena = Cena.Nenhuma,
            certa = opEmoji(certa),
            erradas = erradas.map { opEmoji(it) },
            reforco = listOf(pt("Muito bem pensado!")),
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
            erradas = numerosErrados(resposta, 0, 10, 2).map(::opcaoNumero),
            reforco = listOf(pt(fila.joinToString(", ") { porExtenso(it) } + "!")),
        )
    }

    private val logica: List<Modelo> = listOf(
        Modelo("Igualzinho", "👯") { igual() },
        Modelo("O diferente", "🔍") { diferente() },
        Modelo("Grande e pequeno", "🐘") { tamanho(maior = random.nextBoolean()) },
        Modelo("Filas de 2", "🔴") { sequencia("AB") },
        Modelo("O que combina", "🔑") { combina() },
        Modelo("Cada um no seu grupo", "🗂️") { grupo() },
        Modelo("Filas de 3", "⭐") { sequencia("ABC") },
        Modelo("Filas espertas", "🧠") { sequencia(if (random.nextBoolean()) "AAB" else "ABB") },
        Modelo("Números em fila", "🔢") { numerosEmOrdem() },
        Modelo("Desafio da lógica", "🏆") {
            when (random.nextInt(4)) {
                0 -> sequencia(listOf("ABC", "AAB", "ABB").um())
                1 -> combina()
                2 -> grupo()
                else -> numerosEmOrdem()
            }
        },
    )

    companion object {
        const val FASES_POR_TRILHA = 10
        const val PERGUNTAS_POR_FASE = 5
    }
}
