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
        val lista = perguntasDoModelo(modelos(fase.trilha)[fase.numero], quantidade)
        // Revisão: a partir da 2ª fase, a pergunta do meio vem de uma fase anterior do mesmo caminho.
        if (fase.numero > 0 && quantidade >= 3 && lista.size == quantidade) {
            val anterior = modelos(fase.trilha)[random.nextInt(fase.numero)]
            lista[quantidade / 2] = anterior.criar()
        }
        return lista
    }

    /** Missão do dia: 3 perguntas das fases que a criança mais precisa revisar. */
    fun perguntasDaMissao(fases: List<Fase>): List<Pergunta> {
        if (fases.isEmpty()) return emptyList()
        return List(3) { i -> modelos(fases[i % fases.size].trilha)[fases[i % fases.size].numero].criar() }
    }

    private fun perguntasDoModelo(modelo: Modelo, quantidade: Int): MutableList<Pergunta> {
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
            erradas = numerosErrados(n, 1, 6, 3).map(grupo),
            reforco = listOf(pt("Isso! ${porExtenso(n, obj.feminino)}!")),
        )
    }

    private fun maisOuMenos(): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val mais = random.nextBoolean()
        // Uma das opções erradas sempre fica a 1 de distância da certa, para a criança comparar de verdade.
        val base = (2..5).random(random)
        val vizinho = if (random.nextBoolean()) base + 1 else base - 1
        val outros = (1..6).filter { it != base && it != vizinho }.shuffled(random).take(2)
        val qtds = listOf(base, vizinho) + outros
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
        Modelo("Contar até 3", "🍎") { contar(1, 3, 4) },
        Modelo("Contar até 5", "⭐") { contar(1, 5, 4) },
        Modelo("Cesta: até 5", "🧺") { if (random.nextInt(3) == 0) contar(1, 5, 4) else cesta(1, 5) },
        Modelo("Achar a quantidade", "👆") { acharQuantidade() },
        Modelo("Memória dos números", "🃏") { if (random.nextBoolean()) memoriaDeNumeros() else acharQuantidade() },
        Modelo("Contar até 10", "🔟") { contar(4, 10, 4) },
        Modelo("Cesta: até 10", "🧺") { if (random.nextInt(3) == 0) contar(5, 10, 4) else cesta(4, 9) },
        Modelo("Mais ou menos", "⚖️") { maisOuMenos() },
        Modelo("Maior e menor número", "🔢") { maiorNumero(10) },
        Modelo("Formas", "🔺") { formas(emIngles = false) },
        Modelo("Somar até 5", "➕") { somar(5, 4) },
        Modelo("Tirar até 5", "➖") { subtrair(5, 4) },
        Modelo("Antes e depois", "↔️") { antesDepois(10) },
        Modelo("Historinhas de somar", "📖") { historia(soma = true, max = 8) },
        Modelo("Somar até 10", "🧮") { somar(10, 4) },
        Modelo("Tirar até 10", "🎈") { subtrair(10, 4) },
        Modelo("Historinhas de tirar", "📕") { historia(soma = false, max = 10) },
        Modelo("Números até 20", "2️⃣") { ouvirNumero(10..20) },
        Modelo("Antes e depois até 20", "🔁") { antesDepois(20) },
        Modelo("De 2 em 2", "👣") { deDoisEmDois() },
        Modelo("Maior até 20", "🏔️") { maiorNumero(20) },
        Modelo("Contas misturadas", "🎲") { when (random.nextInt(3)) { 0 -> somar(10, 4); 1 -> subtrair(10, 4); else -> historia(random.nextBoolean(), 10) } },
        Modelo("Cesta e memória", "🧩") { when (random.nextInt(3)) { 0 -> cesta(5, 10); 1 -> memoriaDeNumeros(); else -> maisOuMenos() } },
        Modelo("Desafio dos números", "🏆") { when (random.nextInt(5)) { 0 -> contar(5, 10, 4); 1 -> somar(10, 4); 2 -> subtrair(10, 4); 3 -> antesDepois(20); else -> deDoisEmDois() } },
        Modelo("Supercampeão dos números", "👑") { when (random.nextInt(5)) { 0 -> historia(random.nextBoolean(), 10); 1 -> maiorNumero(20); 2 -> ouvirNumero(10..20); 3 -> cesta(5, 10); else -> memoriaDeNumeros() } },
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
        val erradas = letrasPossiveis.filter { it != p.inicial }.sorteia(3)
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
            .sorteia(3)
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
        // Só palavras que começam com consoante + vogal (GA, BO, MA...), e opções bem parecidas:
        // mesma consoante com outra vogal (GO) e outra consoante com a mesma vogal (PA).
        val p = Vocabulario.todasAsPalavras.filter { pal ->
            val s = pal.silabas.first()
            pal.silabas.size >= 2 && s.length == 2 && s[0].toString() in Vocabulario.consoantes && s[1].toString() in Vocabulario.vogais
        }.um()
        val primeira = p.silabas.first()
        val consoante = primeira[0].toString()
        val vogal = primeira[1].toString()
        val outraVogal = Vocabulario.vogais.filter { it != vogal }.um()
        val outraConsoante = listOf("B", "D", "F", "L", "M", "N", "P", "T", "V").filter { it != consoante }.um()
        val outraVogal2 = Vocabulario.vogais.filter { it != vogal && it != outraVogal }.um()
        val erradas = listOf(consoante + outraVogal, outraConsoante + vogal, outraConsoante + outraVogal2)
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
        val erradas = comRima.filter { it.rima != base.rima }.distinctBy { it.rima }.sorteia(3)
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
        Modelo("Vogais A, E, I, O", "🅰️") { tocarNaLetra(listOf("A", "E", "I", "O"), 4) },
        Modelo("Traçar vogais", "✍️") { if (random.nextInt(3) == 0) tocarNaLetra(Vocabulario.vogais, 4) else tracar(Vocabulario.vogais) },
        Modelo("Todas as vogais", "🔤") { tocarNaLetra(Vocabulario.vogais, 4) },
        Modelo("Começa com vogal", "🐝") { letraInicial(Vocabulario.palavrasComVogal, Vocabulario.vogais) },
        Modelo("Grande e pequenina", "🔡") { minuscula() },
        Modelo("Outras letras", "🅱️") { tocarNaLetra(Vocabulario.consoantes, 4) },
        Modelo("Traçar letras", "✏️") { if (random.nextInt(3) == 0) tocarNaLetra(Vocabulario.consoantes, 4) else tracar(listOf("L", "T", "V", "M", "N", "Z", "E", "F")) },
        Modelo("Primeira letra", "🐱") { letraInicial(Vocabulario.palavrasComConsoante, Vocabulario.consoantes) },
        Modelo("Memória das letras", "🃏") { if (random.nextBoolean()) memoriaDeLetras() else letraInicial(Vocabulario.palavrasComConsoante, Vocabulario.consoantes) },
        Modelo("Começa igual", "👂") { comecaIgual() },
        Modelo("Lendo palavras", "📖") { lerPalavra() },
        Modelo("Pedacinhos", "🧩") { silabaInicial() },
        Modelo("Quantos pedacinhos", "👏") { quantasSilabas() },
        Modelo("Montar palavras", "🧱") { if (random.nextInt(3) == 0) silabaInicial() else montarPalavra(2) },
        Modelo("Complete a palavra", "✏️") { completarPalavra() },
        Modelo("Rimas", "🎵") { rima() },
        Modelo("Montar palavras grandes", "🏗️") { if (random.nextInt(3) == 0) montarPalavra(2) else montarPalavra(3) },
        Modelo("Leitura caprichada", "🔍") { lerPalavraDificil() },
        Modelo("Traçar e ler", "📝") { when (random.nextInt(3)) { 0 -> tracar(Vocabulario.vogais + listOf("L", "T", "V", "M", "N")); 1 -> lerPalavra(); else -> minuscula() } },
        Modelo("Palavras e rimas", "🎶") { when (random.nextInt(3)) { 0 -> rima(); 1 -> comecaIgual(); else -> quantasSilabas() } },
        Modelo("Montar e completar", "🔧") { when (random.nextInt(3)) { 0 -> montarPalavra(2); 1 -> montarPalavra(3); else -> completarPalavra() } },
        Modelo("Leitor esperto", "🦉") { when (random.nextInt(3)) { 0 -> lerPalavraDificil(); 1 -> lerPalavra(); else -> silabaInicial() } },
        Modelo("Memória e letras", "🃏") { when (random.nextInt(3)) { 0 -> memoriaDeLetras(); 1 -> letraInicial(Vocabulario.todasAsPalavras, Vocabulario.vogais + Vocabulario.consoantes); else -> minuscula() } },
        Modelo("Desafio das letras", "🏆") { when (random.nextInt(5)) { 0 -> lerPalavra(); 1 -> silabaInicial(); 2 -> completarPalavra(); 3 -> rima(); else -> montarPalavra(2) } },
        Modelo("Supercampeão das letras", "👑") { when (random.nextInt(5)) { 0 -> lerPalavraDificil(); 1 -> montarPalavra(3); 2 -> quantasSilabas(); 3 -> comecaIgual(); else -> tracar(Vocabulario.vogais + Vocabulario.consoantes.take(6)) } },
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
        Modelo("Colors", "🎨") { encontreEmIngles(Vocabulario.coresBasicas, 4, "Toque na cor") },
        Modelo("Animals", "🐶") { encontreEmIngles(Vocabulario.animais.take(6), 4) },
        Modelo("Numbers 1 a 5", "🖐️") { numeroEmIngles(1..5, 1..6, 4) },
        Modelo("Memory: animals", "🃏") { if (random.nextBoolean()) memoria(3, Vocabulario.animais) else encontreEmIngles(Vocabulario.animais.take(6), 4) },
        Modelo("Fruits", "🍎") { encontreEmIngles(Vocabulario.frutas, 4) },
        Modelo("More colors", "🌈") { encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor") },
        Modelo("Shapes", "🔺") { formas(emIngles = true) },
        Modelo("My body", "👂") { encontreEmIngles(Vocabulario.corpo, 4) },
        Modelo("Numbers 6 a 10", "🔟") { numeroEmIngles(6..10, 1..10, 4) },
        Modelo("Clothes", "👕") { encontreEmIngles(Vocabulario.roupas, 4) },
        Modelo("Memory: fruits", "🃏") { if (random.nextBoolean()) memoria(3, Vocabulario.frutas) else encontreEmIngles(Vocabulario.frutas, 4) },
        Modelo("My house", "🏠") { encontreEmIngles(Vocabulario.casa, 4) },
        Modelo("Food", "🍕") { encontreEmIngles(Vocabulario.comidas, 4) },
        Modelo("Toys", "🧸") { encontreEmIngles(Vocabulario.brinquedos + Vocabulario.animais.drop(6), 4) },
        Modelo("Weather", "🌦️") { encontreEmIngles(Vocabulario.tempo, 4) },
        Modelo("Family", "👨‍👩‍👧") { encontreEmIngles(Vocabulario.familia, 4) },
        Modelo("Vehicles", "🚌") { encontreEmIngles(Vocabulario.veiculos, 4) },
        Modelo("Actions", "🏃") { encontreEmIngles(Vocabulario.acoes, 4) },
        Modelo("Memory: big", "🃏") { when (random.nextInt(2)) { 0 -> memoria(4, Vocabulario.animais + Vocabulario.frutas); else -> memoria(4, Vocabulario.roupas + Vocabulario.comidas) } },
        Modelo("Colors and shapes", "🟣") { when (random.nextInt(2)) { 0 -> encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor"); else -> formas(emIngles = true) } },
        Modelo("Food and clothes", "👗") { when (random.nextInt(2)) { 0 -> encontreEmIngles(Vocabulario.comidas, 4); else -> encontreEmIngles(Vocabulario.roupas, 4) } },
        Modelo("Family and actions", "💃") { when (random.nextInt(2)) { 0 -> encontreEmIngles(Vocabulario.familia, 4); else -> encontreEmIngles(Vocabulario.acoes, 4) } },
        Modelo("All the numbers", "🔢") { numeroEmIngles(1..10, 1..10, 4) },
        Modelo("English challenge", "🏆") { when (random.nextInt(5)) { 0 -> encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor"); 1 -> encontreEmIngles(Vocabulario.animais, 4); 2 -> numeroEmIngles(1..10, 1..10, 4); 3 -> encontreEmIngles(Vocabulario.casa + Vocabulario.brinquedos, 4); else -> encontreEmIngles(Vocabulario.frutas + Vocabulario.corpo, 4) } },
        Modelo("English super star", "👑") { when (random.nextInt(4)) { 0 -> encontreEmIngles(Vocabulario.veiculos + Vocabulario.tempo, 4); 1 -> encontreEmIngles(Vocabulario.acoes + Vocabulario.familia, 4); 2 -> encontreEmIngles(Vocabulario.roupas + Vocabulario.comidas, 4); else -> memoria(4, Vocabulario.veiculos + Vocabulario.animais) } },
    )

    // ------------------------------------------------------------------ LÓGICA

    private fun opEmoji(e: String, escala: Float = 1f) = Opcao(Visual.Emoji(e, escala))

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

    private fun grupo(): Pergunta {
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

    private val logica: List<Modelo> = listOf(
        Modelo("Igualzinho", "👯") { igual() },
        Modelo("O diferente", "🔍") { diferente() },
        Modelo("Memória fácil", "🃏") { memoria(2) },
        Modelo("Grande e pequeno", "🐘") { tamanho(maior = random.nextBoolean()) },
        Modelo("Filas de 2", "🔴") { sequencia("AB") },
        Modelo("Sombras", "🌑") { sombra() },
        Modelo("O que combina", "🔑") { combina() },
        Modelo("Cesta das coisas", "🧺") { cestaDoGrupo() },
        Modelo("Memória", "🃏") { memoria(3) },
        Modelo("Cada um no seu grupo", "🗂️") { grupo() },
        Modelo("Filas de 3", "⭐") { sequencia("ABC") },
        Modelo("Sombras difíceis", "🌚") { sombra() },
        Modelo("Filas espertas", "🧠") { sequencia(if (random.nextBoolean()) "AAB" else "ABB") },
        Modelo("Números em fila", "🔢") { numerosEmOrdem() },
        Modelo("Memória grande", "🃏") { memoria(4) },
        Modelo("Separar na cesta", "🧺") { cestaDoGrupo() },
        Modelo("Combina e grupos", "🔗") { when (random.nextInt(2)) { 0 -> combina(); else -> grupo() } },
        Modelo("Tamanhos e sombras", "🔦") { when (random.nextInt(2)) { 0 -> tamanho(random.nextBoolean()); else -> sombra() } },
        Modelo("Filas misturadas", "🎨") { when (random.nextInt(4)) { 0 -> sequencia("AB"); 1 -> sequencia("ABC"); 2 -> sequencia("AAB"); else -> sequencia("ABB") } },
        Modelo("Memória gigante", "🃏") { memoria(5) },
        Modelo("Detetive", "🕵️") { when (random.nextInt(3)) { 0 -> diferente(); 1 -> sombra(); else -> combina() } },
        Modelo("Pensa rápido", "⚡") { when (random.nextInt(3)) { 0 -> igual(); 1 -> grupo(); else -> numerosEmOrdem() } },
        Modelo("Cesta e memória", "🧩") { when (random.nextInt(2)) { 0 -> cestaDoGrupo(); else -> memoria(4) } },
        Modelo("Desafio da lógica", "🏆") { when (random.nextInt(5)) { 0 -> sequencia(listOf("ABC", "AAB", "ABB").um()); 1 -> combina(); 2 -> grupo(); 3 -> numerosEmOrdem(); else -> sombra() } },
        Modelo("Supercampeão da lógica", "👑") { when (random.nextInt(5)) { 0 -> memoria(5); 1 -> sequencia("ABC"); 2 -> cestaDoGrupo(); 3 -> sombra(); else -> diferente() } },
    )


    // ------------------------------------------------------------------ ATIVIDADES DE MEXER E NOVOS JOGOS

    /** Arrastar N figuras para a cesta (sobram algumas de propósito: é preciso contar e parar). */
    private fun cesta(min: Int, max: Int): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val n = (min..max).random(random)
        val disponiveis = n + (2..3).random(random)
        val f = obj.feminino
        return Pergunta.deAtividade(
            instrucao = "Coloque ${n} na cesta",
            fala = listOf(pt("Coloque ${porExtenso(n, f)} ${obj.nome(n)} na cesta. Depois toque no certinho.")),
            atividade = Atividade.Cesta(List(disponiveis) { obj.emoji }, n),
            reforco = listOf(pt("${porExtenso(n, f)} ${obj.nome(n)} na cesta!")),
        )
    }

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

    private fun memoria(paresQtd: Int, fonte: List<PalavraEn>? = null, iguais: List<String>? = null): Pergunta {
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

    /** Memória de quantidade: o número com o grupo que tem aquela quantidade. */
    private fun memoriaDeNumeros(): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val nums = (1..5).toList().sorteia(3)
        return Pergunta.deAtividade(
            instrucao = "Número com a quantidade",
            fala = listOf(pt("Jogo da memória! Junte cada número com o grupo que tem aquela quantidade.")),
            atividade = Atividade.Memoria(nums.map { it.toString() to obj.emoji.repeat(it) }),
            reforco = listOf(pt("Isso! Cada número com a sua quantidade.")),
        )
    }

    /** Memória de letras: a letra com a figura que começa com ela. */
    private fun memoriaDeLetras(): Pergunta {
        val palavras = Vocabulario.palavrasComConsoante.distinctBy { it.inicial }.sorteia(3)
        return Pergunta.deAtividade(
            instrucao = "Letra com a figura",
            fala = listOf(pt("Jogo da memória! Junte cada letra com a figura que começa com ela.")),
            atividade = Atividade.Memoria(palavras.map { it.inicial to it.emoji }, palavras.associate { it.emoji to pt(it.texto.lowercase()) }),
            reforco = listOf(pt("Muito bem!")),
        )
    }

    private fun montarPalavra(silabas: Int): Pergunta {
        val p = Vocabulario.todasAsPalavras.filter { it.silabas.size == silabas && it.silabas.all { s -> s.length <= 3 } }.um()
        val extras = Vocabulario.todasAsPalavras.flatMap { it.silabas }.distinct().filter { it !in p.silabas && it.length == 2 }.sorteia(2)
        return Pergunta.deAtividade(
            instrucao = "Monte: ${p.texto}",
            fala = listOf(pt("Monte a palavra ${p.texto.lowercase()}. Arraste os pedacinhos na ordem.")),
            atividade = Atividade.MontarPalavra(p.emoji, p.texto, p.silabas, extras),
            reforco = listOf(pt(p.silabas.joinToString(", ") { it.lowercase() } + ". " + p.texto.lowercase() + "!")),
        )
    }

    private fun tracar(letras: List<String>): Pergunta {
        val l = letras.um()
        return Pergunta.deAtividade(
            instrucao = "Trace a letra $l",
            fala = listOf(pt("Passe o dedo por cima da letra ${falaDaLetra(l)}, seguindo os pontinhos.")),
            atividade = Atividade.Tracar(l),
            reforco = listOf(pt("Você escreveu a letra ${falaDaLetra(l)}!")),
        )
    }

    private fun formas(emIngles: Boolean): Pergunta {
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

    private fun maiorNumero(max: Int): Pergunta {
        val nums = (1..max).toList().sorteia(4)
        val maior = random.nextBoolean()
        val certo = if (maior) nums.max() else nums.min()
        return montar(
            instrucao = if (maior) "Qual é o maior número?" else "Qual é o menor número?",
            fala = listOf(pt(if (maior) "Qual é o maior número?" else "Qual é o menor número?")),
            cena = Cena.Nenhuma,
            certa = opcaoNumero(certo),
            erradas = nums.filter { it != certo }.map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(certo)} é o ${if (maior) "maior" else "menor"}!")),
        )
    }

    private fun antesDepois(max: Int): Pergunta {
        val depois = random.nextBoolean()
        val n = if (depois) (1 until max).random(random) else (2..max).random(random)
        val resposta = if (depois) n + 1 else n - 1
        return montar(
            instrucao = if (depois) "Depois do $n vem?" else "Antes do $n vem?",
            fala = listOf(pt(if (depois) "Que número vem depois do ${porExtenso(n)}?" else "Que número vem antes do ${porExtenso(n)}?")),
            cena = Cena.Sequencia(if (depois) listOf("$n", "?") else listOf("?", "$n")),
            certa = opcaoNumero(resposta),
            erradas = numerosErrados(resposta, 0, max + 1, 3).map(::opcaoNumero),
            reforco = listOf(pt(if (depois) "${porExtenso(n)}, ${porExtenso(resposta)}!" else "${porExtenso(resposta)}, ${porExtenso(n)}!")),
        )
    }

    private fun ouvirNumero(faixa: IntRange): Pergunta {
        val n = faixa.random(random)
        return montar(
            instrucao = "Toque no $n",
            fala = listOf(pt("Toque no número ${porExtenso(n)}.")),
            cena = Cena.Nenhuma,
            certa = opcaoNumero(n),
            erradas = numerosErrados(n, faixa.first, faixa.last, 3).map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(n)}!")),
        )
    }

    /** Continhas contadas como historinha, com nomes de crianças. */
    private fun historia(soma: Boolean, max: Int): Pergunta {
        val nome = Vocabulario.nomesDeCriancas.um()
        val obj = Vocabulario.objetosParaContar.um()
        val f = obj.feminino
        val a = (2..max - 1).random(random)
        val b = if (soma) (1..(max - a)).random(random) else (1 until a).random(random)
        val resp = if (soma) a + b else a - b
        val quantos = if (f) "Quantas" else "Quantos"
        val texto = if (soma) {
            "$nome tinha ${porExtenso(a, f)} ${obj.nome(a)} e ganhou mais ${porExtenso(b, f)}. $quantos $nome tem agora?"
        } else {
            "$nome tinha ${porExtenso(a, f)} ${obj.nome(a)} e deu ${porExtenso(b, f)} para um amigo. $quantos sobraram?"
        }
        return montar(
            instrucao = "${if (soma) "$a + $b" else "$a − $b"} = ?",
            fala = listOf(pt(texto)),
            cena = Cena.Conta(obj.emoji, a, b, soma),
            certa = opcaoNumero(resp),
            erradas = numerosErrados(resp, 0, max, 3).map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(resp, f)} ${obj.nome(resp)}!")),
        )
    }

    private fun deDoisEmDois(): Pergunta {
        val passo = if (random.nextInt(3) == 0) 5 else 2
        val inicio = if (passo == 2) listOf(0, 2, 4, 1, 3).um() else 0
        val fila = List(4) { inicio + it * passo }
        val resposta = fila.last()
        return montar(
            instrucao = "Que número vem depois?",
            fala = listOf(pt("Contando de ${porExtenso(passo)} em ${porExtenso(passo)}: " + fila.dropLast(1).joinToString(", ") { porExtenso(it) } + ". Qual vem depois?")),
            cena = Cena.Sequencia(fila.dropLast(1).map { it.toString() } + "?"),
            certa = opcaoNumero(resposta),
            erradas = listOf(resposta - 1, resposta + 1, resposta + passo).filter { it >= 0 }.distinct().take(3).map(::opcaoNumero),
            reforco = listOf(pt(fila.joinToString(", ") { porExtenso(it) } + "!")),
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

    private fun quantasSilabas(): Pergunta {
        val p = Vocabulario.todasAsPalavras.filter { it.silabas.size in 1..4 }.um()
        val n = p.silabas.size
        return montar(
            instrucao = "Quantos pedacinhos?",
            fala = listOf(pt("Bata palmas comigo: " + p.silabas.joinToString(", ") { it.lowercase() } + ". Quantos pedacinhos tem ${p.texto.lowercase()}?")),
            cena = Cena.Palavra(p.emoji + " " + p.texto),
            certa = opcaoNumero(n),
            erradas = (1..5).filter { it != n }.sorteia(3).map(::opcaoNumero),
            reforco = listOf(pt(p.silabas.joinToString(", ") { it.lowercase() } + ". ${porExtenso(n)} pedacinhos!")),
        )
    }

    private fun minuscula(): Pergunta {
        val letras = listOf("A", "E", "I", "O", "U", "B", "D", "M", "P", "T")
        val (certa, e1, e2, e3) = letras.sorteia(4)
        val op = { l: String -> Opcao(Visual.Texto(l.lowercase()), listOf(pt(falaDaLetra(l)))) }
        return montar(
            instrucao = "Ache o ${certa.lowercase()} pequenino",
            fala = listOf(pt("Esta é a letra ${falaDaLetra(certa)} grande. Toque na letra ${falaDaLetra(certa)} pequenininha.")),
            cena = Cena.Palavra(certa),
            certa = op(certa),
            erradas = listOf(op(e1), op(e2), op(e3)),
            reforco = listOf(pt("${certa} grande e ${certa.lowercase()} pequeno!")),
        )
    }

    private fun comecaIgual(): Pergunta {
        val porLetra = Vocabulario.palavrasComConsoante.groupBy { it.inicial }.filter { it.value.size >= 2 }
        val (base, certa) = porLetra.values.toList().um().sorteia(2)
        val erradas = Vocabulario.palavrasComConsoante.filter { it.inicial != base.inicial }.distinctBy { it.inicial }.sorteia(3)
        val op = { p: Palavra -> Opcao(Visual.Emoji(p.emoji), listOf(pt(p.texto.lowercase()))) }
        return montar(
            instrucao = "Começa igual a ${base.texto}?",
            fala = listOf(pt("${base.texto.lowercase()}. Qual destes começa com o mesmo som de ${base.texto.lowercase()}?")),
            cena = Cena.Emojis(base.emoji),
            certa = op(certa),
            erradas = erradas.map(op),
            reforco = listOf(pt("${base.texto.lowercase()} e ${certa.texto.lowercase()}: os dois começam com ${falaDaLetra(base.inicial)}!")),
        )
    }

    /** Leitura mais difícil: as opções começam com a mesma letra, então é preciso ler a palavra toda. */
    private fun lerPalavraDificil(): Pergunta {
        val porLetra = Vocabulario.palavrasComConsoante.groupBy { it.inicial }.filter { it.value.size >= 2 }
        val grupo = porLetra.values.toList().um()
        val certa = grupo.um()
        val mesmas = grupo.filter { it != certa }
        val outras = Vocabulario.palavrasComConsoante.filter { it.inicial != certa.inicial }.sorteia(3 - minOf(mesmas.size, 2))
        val erradas = (mesmas.take(2) + outras).take(3)
        val op = { p: Palavra -> Opcao(Visual.Emoji(p.emoji), listOf(pt(p.texto.lowercase()))) }
        return montar(
            instrucao = "Leia: ${certa.texto}",
            fala = listOf(pt("Leia com calma. Qual figura é esta palavra?")),
            cena = Cena.Palavra(certa.texto),
            certa = op(certa),
            erradas = erradas.map(op),
            reforco = listOf(pt("Aqui está escrito ${certa.texto.lowercase()}!")),
        )
    }

    companion object {
        const val FASES_POR_TRILHA = 25
        const val PERGUNTAS_POR_FASE = 5
    }
}
