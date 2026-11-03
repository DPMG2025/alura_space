package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Cria as perguntas de cada fase. As perguntas são sorteadas a cada partida,
 * então a criança pode repetir uma fase sem decorar a ordem das respostas.
 *
 * Cada trilha tem [FASES_POR_TRILHA] fases, da mais fácil para a mais difícil, em 5 mundos de 10 ([Mundos]).
 */
class GeradorDeFases(private val random: Random = Random.Default) {

    private class Modelo(val titulo: String, val emoji: String, val criar: () -> Pergunta)

    fun fases(trilha: Trilha): List<Fase> =
        modelos(trilha).mapIndexed { i, m -> Fase(trilha, i, m.titulo, m.emoji) }

    fun perguntas(fase: Fase, quantidade: Int = PERGUNTAS_POR_FASE): List<Pergunta> {
        val lista = perguntasDoModelo(modelos(fase.trilha)[fase.numero], quantidade)
        // Revisão: a partir da 2ª fase, a pergunta do meio vem de uma fase anterior do mesmo caminho.
        if (fase.numero > 0 && quantidade >= 3 && lista.size == quantidade) {
            // Sorteia de novo se a revisão tiver a mesma resposta da pergunta vizinha.
            val vizinhas = listOf(lista[quantidade / 2 - 1], lista[quantidade / 2 + 1]).map { it.opcoes[it.indiceCorreto].visual to it.instrucao }
            var revisao = modelos(fase.trilha)[random.nextInt(fase.numero)].criar()
            var tentativas = 0
            while ((revisao.opcoes[revisao.indiceCorreto].visual to revisao.instrucao) in vizinhas && tentativas++ < 20) {
                revisao = modelos(fase.trilha)[random.nextInt(fase.numero)].criar()
            }
            lista[quantidade / 2] = revisao
        }
        return lista
    }

    /** Missão do dia: 3 perguntas das fases que a criança mais precisa revisar. */
    fun perguntasDaMissao(fases: List<Fase>): List<Pergunta> {
        if (fases.isEmpty()) return emptyList()
        return List(3) { i -> perguntaDaFase(fases[i % fases.size]) }
    }

    private fun perguntasDoModelo(modelo: Modelo, quantidade: Int): MutableList<Pergunta> {
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

    /**
     * As 25 fases de antes continuam nos mesmos números (o progresso salvo vale igual) e as fases
     * 26 a 50 completam os mundos 3, 4 e 5.
     */
    private val todosOsModelos: Map<Trilha, List<Modelo>> by lazy {
        mapOf(
            Trilha.MATEMATICA to matematica + matematicaMundos,
            Trilha.PORTUGUES to portugues + portuguesMundos,
            Trilha.INGLES to ingles + inglesMundos,
            Trilha.LOGICA to logica + logicaMundos,
        )
    }

    private fun modelos(trilha: Trilha): List<Modelo> = todosOsModelos.getValue(trilha)

    /** Uma pergunta nova de uma fase (treino e revisão). */
    fun perguntaDaFase(fase: Fase): Pergunta = modelos(fase.trilha)[fase.numero].criar()

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

    // ================================================================== MUNDOS 3, 4 E 5 (fases 26 a 50)

    private val V = VocabularioDosMundos

    /** Três números errados tirados de [candidatos], os mais perto do certo primeiro. */
    private fun erradosDe(certo: Int, candidatos: Iterable<Int>): List<Int> =
        candidatos.filter { it != certo }.distinct()
            .sortedBy { kotlin.math.abs(it - certo) + random.nextDouble() * 1.5 }
            .take(3)

    private fun opcaoTexto(texto: String, fala: String = "") =
        Opcao(Visual.Texto(texto), if (fala.isBlank()) emptyList() else listOf(pt(fala)))

    private fun opcaoFigura(emoji: String, fala: String = "") =
        Opcao(Visual.Emoji(emoji), if (fala.isBlank()) emptyList() else listOf(pt(fala)))

    /** Conta com figuras quando é pequena; com números grandes, a conta escrita em quadradinhos. */
    private fun cenaDaConta(emoji: String, a: Int, b: Int, soma: Boolean): Cena =
        if ((if (soma) a + b else a) <= 12) Cena.Conta(emoji, a, b, soma)
        else Cena.Sequencia(listOf("$a", if (soma) "+" else "−", "$b", "=", "?"))

    // ------------------------------------------------------------------ MATEMÁTICA

    private fun tracarNumero(): Pergunta {
        val n = (0..9).random(random)
        return Pergunta.deAtividade(
            instrucao = "Trace o número $n",
            fala = listOf(pt("Passe o dedo por cima do número ${porExtenso(n)}, seguindo os pontinhos.")),
            atividade = Atividade.Tracar(n.toString()),
            reforco = listOf(pt("Você escreveu o número ${porExtenso(n)}!")),
        )
    }

    private fun somarAte20(): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val a = (4..15).random(random)
        val b = (2..(20 - a)).random(random)
        val total = a + b
        return montar(
            instrucao = "$a + $b = ?",
            fala = listOf(pt("${porExtenso(a)} mais ${porExtenso(b)}. Quanto dá? Dica: guarde o ${porExtenso(a)} na cabeça e conte mais ${porExtenso(b)}.")),
            cena = cenaDaConta(obj.emoji, a, b, true),
            certa = opcaoNumero(total),
            erradas = erradosDe(total, 2..20).map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(a)} mais ${porExtenso(b)} é igual a ${porExtenso(total)}!")),
        )
    }

    private fun tirarAte20(): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val a = (11..20).random(random)
        val b = (2..minOf(10, a - 1)).random(random)
        val resto = a - b
        return montar(
            instrucao = "$a − $b = ?",
            fala = listOf(pt("${porExtenso(a)} menos ${porExtenso(b)}. Quanto sobra?")),
            cena = cenaDaConta(obj.emoji, a, b, false),
            certa = opcaoNumero(resto),
            erradas = erradosDe(resto, 0..20).map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(a)} menos ${porExtenso(b)} é igual a ${porExtenso(resto)}!")),
        )
    }

    private fun dobro(max: Int): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val n = (1..max).random(random)
        return montar(
            instrucao = "O dobro de $n",
            fala = listOf(pt("O dobro é a mesma quantidade duas vezes. Qual é o dobro de ${porExtenso(n)}? ${porExtenso(n)} mais ${porExtenso(n)}.")),
            cena = cenaDaConta(obj.emoji, n, n, true),
            certa = opcaoNumero(2 * n),
            erradas = erradosDe(2 * n, 1..20).map(::opcaoNumero),
            reforco = listOf(pt("O dobro de ${porExtenso(n)} é ${porExtenso(2 * n)}!")),
        )
    }

    private fun metade(max: Int): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val n = 2 * (1..max / 2).random(random)
        val f = obj.feminino
        val quantos = if (f) "Quantas" else "Quantos"
        return montar(
            instrucao = "A metade de $n",
            fala = listOf(pt("Vamos dividir ${porExtenso(n, f)} ${obj.nome(n)} entre dois amigos, cada um com a mesma quantidade. $quantos cada um ganha?")),
            cena = if (n <= 12) Cena.Emojis(obj.emoji.repeat(n)) else Cena.Sequencia(listOf("$n", "÷", "2", "=", "?")),
            certa = opcaoNumero(n / 2),
            erradas = erradosDe(n / 2, (1..10) + listOf(n)).map(::opcaoNumero),
            reforco = listOf(pt("A metade de ${porExtenso(n)} é ${porExtenso(n / 2)}. ${porExtenso(n / 2)} para cada um!")),
        )
    }

    private fun deDezEmDez(max: Int): Pergunta {
        val dezenas = (10..max step 10).toList()
        return if (random.nextBoolean()) {
            val crescente = random.nextInt(3) != 0
            val passos = dezenas.size - 3
            val inicio = (0 until passos).random(random)
            val fila = dezenas.subList(inicio, inicio + 4).let { if (crescente) it else it.reversed() }
            val resposta = fila.last()
            montar(
                instrucao = "Que número vem depois?",
                fala = listOf(pt("Contando de dez em dez: " + fila.dropLast(1).joinToString(", ") { porExtenso(it) } + ". Qual vem depois?")),
                cena = Cena.Sequencia(fila.dropLast(1).map { it.toString() } + "?"),
                certa = opcaoNumero(resposta),
                erradas = erradosDe(resposta, dezenas + listOf(resposta + 1, resposta - 1)).map(::opcaoNumero),
                reforco = listOf(pt(fila.joinToString(", ") { porExtenso(it) } + "!")),
            )
        } else {
            val n = dezenas.um()
            montar(
                instrucao = "Toque no $n",
                fala = listOf(pt("Toque no número ${porExtenso(n)}.")),
                cena = Cena.Nenhuma,
                certa = opcaoNumero(n),
                erradas = erradosDe(n, dezenas + listOf(n / 10)).map(::opcaoNumero),
                reforco = listOf(pt("${porExtenso(n)}! São ${porExtenso(n / 10, feminino = true)} ${if (n == 10) "dezena" else "dezenas"}.")),
            )
        }
    }

    /**
     * Dezenas e unidades com material dourado (barrinhas de dez cubinhos e cubinhos soltos): quanto é?
     * Uma das erradas troca os algarismos (34 e 43).
     */
    private fun dezenasEUnidades(): Pergunta {
        val d = (1..4).random(random)
        val u = (0..9).random(random)
        val n = d * 10 + u
        val trocado = u * 10 + d
        val candidatos = listOfNotNull(trocado.takeIf { u > 0 && it != n }) + erradosDe(n, 10..59)
        val barrinhas = "${porExtenso(d, true)} ${if (d == 1) "barrinha" else "barrinhas"}"
        val soltos = if (u == 0) "nenhum cubinho solto" else "${porExtenso(u)} ${if (u == 1) "cubinho solto" else "cubinhos soltos"}"
        return montar(
            instrucao = "Quanto é?",
            fala = listOf(
                pt("Cada barrinha tem dez cubinhos: vale dez. Cada cubinho solto vale um. Aqui tem $barrinhas e $soltos. Que número é?"),
            ),
            cena = Cena.MaterialDourado(d, u),
            certa = opcaoNumero(n),
            erradas = candidatos.distinct().filter { it != n }.take(3).map(::opcaoNumero),
            reforco = listOf(
                pt(
                    "${porExtenso(n)}! ${porExtenso(d, true).replaceFirstChar { it.uppercase() }} ${if (d == 1) "dezena" else "dezenas"}" +
                        (if (u == 0) "." else " e ${porExtenso(u, true)} ${if (u == 1) "unidade" else "unidades"}."),
                ),
            ),
        )
    }

    private fun ateCinquenta(): Pergunta = when (random.nextInt(3)) {
        0 -> ouvirNumero(20..50)
        1 -> antesDepois(50)
        else -> maiorNumero(50)
    }

    /** Medidas: o mais pesado, o mais leve, o mais alto, o mais baixo. */
    private fun medidas(): Pergunta {
        val peso = random.nextBoolean()
        val lista = if (peso) V.doLeveAoPesado else V.doBaixoAoAlto
        val escolhidos = lista.sorteia(4)
        val mais = random.nextBoolean()
        val certo = if (mais) escolhidos.maxBy { lista.indexOf(it) } else escolhidos.minBy { lista.indexOf(it) }
        val pergunta = when {
            peso && mais -> "Qual é o mais pesado?"
            peso -> "Qual é o mais leve?"
            mais -> "Qual é o mais alto?"
            else -> "Qual é o mais baixinho?"
        }
        val comparacao = when {
            peso && mais -> "pesa mais que os outros"
            peso -> "pesa menos que os outros"
            mais -> "é mais alto que os outros"
            else -> "é mais baixinho que os outros"
        }
        return montar(
            instrucao = pergunta,
            fala = listOf(pt(pergunta)),
            cena = Cena.Nenhuma,
            certa = opcaoFigura(certo.first, certo.second),
            erradas = escolhidos.filter { it != certo }.map { opcaoFigura(it.first, it.second) },
            reforco = listOf(pt("Isso! Quem ${comparacao}: ${certo.second}!")),
        )
    }

    private fun falaDaHora(h: Int) = when (h) {
        1 -> "uma hora"
        12 -> "doze horas, meio dia"
        else -> "${porExtenso(h, true)} horas"
    }

    /** Horas cheias: ler o relógio ou achar o relógio da hora pedida. */
    private fun horas(): Pergunta {
        val h = (1..12).random(random)
        val erradas = erradosDe(h, 1..12)
        return if (random.nextBoolean()) {
            montar(
                instrucao = "Que horas são?",
                fala = listOf(pt("O ponteiro pequeno mostra a hora. Que horas são?")),
                cena = Cena.Emojis(V.relogios[h - 1]),
                certa = opcaoTexto("${h}h", falaDaHora(h)),
                erradas = erradas.map { opcaoTexto("${it}h", falaDaHora(it)) },
                reforco = listOf(pt("São ${falaDaHora(h)}!")),
            )
        } else {
            montar(
                instrucao = "Relógio das ${h}h",
                fala = listOf(pt("Toque no relógio que marca ${falaDaHora(h)}.")),
                cena = Cena.Nenhuma,
                certa = opcaoFigura(V.relogios[h - 1], falaDaHora(h)),
                erradas = erradas.map { opcaoFigura(V.relogios[it - 1], falaDaHora(it)) },
                reforco = listOf(pt("Esse marca ${falaDaHora(h)}!")),
            )
        }
    }

    private fun reais(v: Int) = if (v == 1) "um real" else "${porExtenso(v)} reais"

    /** Dinheiro simples: contar moedinhas de um real, pagar na cesta e ver o que vale mais. */
    private fun dinheiro(): Pergunta = when (random.nextInt(3)) {
        0 -> {
            val k = (2..9).random(random)
            montar(
                instrucao = "Quantos reais?",
                fala = listOf(pt("Cada moedinha vale um real. Quantos reais tem aqui?")),
                cena = Cena.Emojis(Vocabulario.moedinha.emoji.repeat(k)),
                certa = opcaoTexto("R$ $k", reais(k)),
                erradas = erradosDe(k, 1..10).map { opcaoTexto("R$ $it", reais(it)) },
                reforco = listOf(pt("${porExtenso(k, true)} moedinhas: ${reais(k)}!")),
            )
        }
        1 -> {
            val k = (2..8).random(random)
            val comprar = listOf("o sorvete" to "🍦", "o pirulito" to "🍭", "a bola" to "⚽", "o livro" to "📕").um()
            Pergunta.deAtividade(
                instrucao = "Pague ${k} reais",
                fala = listOf(pt("${comprar.first.replaceFirstChar { it.uppercase() }} custa ${reais(k)}. Coloque ${porExtenso(k, true)} moedinhas na cesta para pagar. Depois toque no certinho.")),
                atividade = Atividade.Cesta(List(k + (2..3).random(random)) { Vocabulario.moedinha.emoji }, k),
                reforco = listOf(pt("Pagou ${reais(k)}! ${comprar.first.replaceFirstChar { it.uppercase() }} é seu!")),
                cena = Cena.Emojis(comprar.second),
            )
        }
        else -> {
            val valores = listOf(1, 2, 5, 10, 20, 50).sorteia(4)
            val mais = random.nextBoolean()
            val certo = if (mais) valores.max() else valores.min()
            montar(
                instrucao = if (mais) "Qual vale mais?" else "Qual vale menos?",
                fala = listOf(pt(if (mais) "Qual destes vale mais dinheiro?" else "Qual destes vale menos dinheiro?")),
                cena = Cena.Nenhuma,
                certa = opcaoTexto("R$ $certo", reais(certo)),
                erradas = valores.filter { it != certo }.map { opcaoTexto("R$ $it", reais(it)) },
                reforco = listOf(pt("${reais(certo).replaceFirstChar { it.uppercase() }} vale ${if (mais) "mais" else "menos"}!")),
            )
        }
    }

    /** Gráfico de figuras: cada linha mostra quantas coisas de um tipo. */
    private fun grafico(): Pergunta {
        val objs = Vocabulario.objetosParaContar.distinctBy { it.emoji }.sorteia(4)
        val noGrafico = objs.take(3)
        val qtds = (1..5).toList().sorteia(3)
        val itens = noGrafico.zip(qtds).flatMap { (o, q) -> List(q) { o.emoji } + List(5 - q) { "" } }
        val cena = Cena.Grade(5, itens)
        return if (random.nextBoolean()) {
            val mais = random.nextBoolean()
            val iCerto = if (mais) qtds.indexOf(qtds.max()) else qtds.indexOf(qtds.min())
            val certo = noGrafico[iCerto]
            val palavra = if (mais) "mais" else "menos"
            montar(
                instrucao = "Qual tem $palavra?",
                fala = listOf(pt("Este é um gráfico. Cada linha mostra uma coisa. Qual linha tem $palavra?")),
                cena = cena,
                certa = opcaoFigura(certo.emoji, certo.plural),
                erradas = (noGrafico - certo).map { opcaoFigura(it.emoji, it.plural) } + opcaoFigura(objs[3].emoji, objs[3].plural),
                reforco = listOf(pt("${certo.plural.replaceFirstChar { it.uppercase() }}: ${porExtenso(qtds[iCerto], certo.feminino)}! É a linha que tem $palavra.")),
            )
        } else {
            val i = (0..2).random(random)
            val o = noGrafico[i]
            val n = qtds[i]
            montar(
                instrucao = "Quantos no gráfico?",
                fala = listOf(pt("Olhe o gráfico. ${if (o.feminino) "Quantas" else "Quantos"} ${o.plural} tem?")),
                cena = cena,
                certa = opcaoNumero(n),
                erradas = erradosDe(n, 0..6).map(::opcaoNumero),
                reforco = listOf(pt("${porExtenso(n, o.feminino)} ${o.nome(n)}!")),
            )
        }
    }

    private fun ateCem(): Pergunta = when (random.nextInt(3)) {
        0 -> deDezEmDez(100)
        1 -> ouvirNumero(50..100)
        else -> antesDepois(99)
    }

    /** Somar dezenas inteiras (20 + 30) ou dezena com unidade (40 + 6). */
    private fun somarDezenas(): Pergunta {
        val a = 10 * (1..5).random(random)
        val inteiras = random.nextBoolean()
        val b = if (inteiras) 10 * (1..(10 - a / 10)).random(random) else (1..9).random(random)
        val total = a + b
        val candidatos = if (inteiras) (10..100 step 10) else ((total - 4)..(total + 4)) + listOf(total + 10, total - 10)
        return montar(
            instrucao = "$a + $b = ?",
            fala = listOf(pt("${porExtenso(a)} mais ${porExtenso(b)}. Quanto dá?")),
            cena = Cena.Sequencia(listOf("$a", "+", "$b", "=", "?")),
            certa = opcaoNumero(total),
            erradas = erradosDe(total, candidatos.filter { it in 1..100 }).map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(a)} mais ${porExtenso(b)} é igual a ${porExtenso(total)}!")),
        )
    }

    /** Problemas narrados até 20. Com [doisPassos], ganha e depois dá (ou o contrário). */
    private fun problema(soma: Boolean, doisPassos: Boolean = false): Pergunta {
        val nome = Vocabulario.nomesDeCriancas.um()
        val obj = Vocabulario.objetosParaContar.um()
        val f = obj.feminino
        val quantos = if (f) "Quantas" else "Quantos"
        if (doisPassos) {
            val a = (3..10).random(random)
            val b = (1..6).random(random)
            val c = (1..minOf(a + b - 1, 6)).random(random)
            val resp = a + b - c
            return montar(
                instrucao = "$a + $b − $c = ?",
                fala = listOf(pt("$nome tinha ${porExtenso(a, f)} ${obj.nome(a)}. Ganhou mais ${porExtenso(b, f)} e depois deu ${porExtenso(c, f)} para a vovó. $quantos ficaram?")),
                cena = Cena.Sequencia(listOf("$a", "+", "$b", "−", "$c", "=", "?")),
                certa = opcaoNumero(resp),
                erradas = erradosDe(resp, (0..20) + listOf(a + b, a + b + c)).map(::opcaoNumero),
                reforco = listOf(pt("${porExtenso(a)} mais ${porExtenso(b)} dá ${porExtenso(a + b)}. Tirando ${porExtenso(c)}, ficam ${porExtenso(resp)}!")),
            )
        }
        val a = if (soma) (5..14).random(random) else (11..20).random(random)
        val b = if (soma) (2..(20 - a)).random(random) else (2..minOf(9, a - 1)).random(random)
        val resp = if (soma) a + b else a - b
        val texto = if (soma) {
            "$nome tinha ${porExtenso(a, f)} ${obj.nome(a)} e ganhou mais ${porExtenso(b, f)}. $quantos $nome tem agora?"
        } else {
            "$nome tinha ${porExtenso(a, f)} ${obj.nome(a)} e deu ${porExtenso(b, f)} para os amigos. $quantos sobraram?"
        }
        return montar(
            instrucao = "${if (soma) "$a + $b" else "$a − $b"} = ?",
            fala = listOf(pt(texto)),
            cena = cenaDaConta(obj.emoji, a, b, soma),
            certa = opcaoNumero(resp),
            erradas = erradosDe(resp, 0..20).map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(resp, f)} ${obj.nome(resp)}!")),
        )
    }

    /** Posições na fila (começo, fim, meio, depois de) e formas pelo número de lados. */
    private fun posicoesEFormas(): Pergunta {
        if (random.nextInt(3) == 0) {
            val formas = listOf(
                Triple("🔺", "triângulo", "Qual forma tem três lados?"),
                Triple("🟦", "quadrado", "Qual forma tem quatro lados iguais?"),
                Triple("🔴", "círculo", "Qual forma é redondinha, sem nenhum lado reto?"),
                Triple("⭐", "estrela", "Qual forma tem cinco pontas?"),
            )
            val certa = formas.um()
            return montar(
                instrucao = certa.third,
                fala = listOf(pt(certa.third)),
                cena = Cena.Nenhuma,
                certa = opcaoFigura(certa.first, certa.second),
                erradas = (formas - certa).map { opcaoFigura(it.first, it.second) },
                reforco = listOf(pt("Isso! ${certa.second.replaceFirstChar { it.uppercase() }}!")),
            )
        }
        val bichos = Vocabulario.animais.distinctBy { it.emoji }.sorteia(5)
        val fila = bichos.take(if (random.nextBoolean()) 3 else 5)
        val (pergunta, certo) = when (random.nextInt(4)) {
            0 -> "Quem está no começo da fila?" to fila.first()
            1 -> "Quem está no fim da fila?" to fila.last()
            2 -> "Quem está no meio da fila?" to fila[fila.size / 2]
            else -> {
                val i = (0 until fila.size - 1).random(random)
                "Quem está logo depois do ${fila[i].pt}?" to fila[i + 1]
            }
        }
        val outros = (bichos - certo).take(3)
        return montar(
            instrucao = pergunta,
            fala = listOf(pt("A fila começa aqui na esquerda. $pergunta")),
            cena = Cena.Sequencia(fila.map { it.emoji }),
            certa = opcaoFigura(certo.emoji, certo.pt),
            erradas = outros.map { opcaoFigura(it.emoji, it.pt) },
            reforco = listOf(pt("Isso! ${certo.pt.replaceFirstChar { it.uppercase() }}!")),
        )
    }

    /** Fila de números de 2 em 2, 3 em 3, 5 em 5 ou 10 em 10, para frente ou para trás. */
    private fun sequenciaNumerica(max: Int): Pergunta {
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

    /** Comparar até 100: duas opções com a mesma dezena e, às vezes, os algarismos trocados. */
    private fun compararAte100(): Pergunta {
        val d = (2..9).random(random)
        val (u1, u2) = (0..9).toList().sorteia(2)
        val a = d * 10 + u1
        val b = d * 10 + u2
        val trocado = (u1 * 10 + d).takeIf { it >= 10 && it != a && it != b }
        val nums = (listOf(a, b) + listOfNotNull(trocado) + (10..99).toList().sorteia(6)).distinct().take(4)
        val maior = random.nextBoolean()
        val certo = if (maior) nums.max() else nums.min()
        return montar(
            instrucao = if (maior) "Qual é o maior número?" else "Qual é o menor número?",
            fala = listOf(pt(if (maior) "Qual é o maior número? Olhe primeiro as dezenas." else "Qual é o menor número? Olhe primeiro as dezenas.")),
            cena = Cena.Nenhuma,
            certa = opcaoNumero(certo),
            erradas = nums.filter { it != certo }.map(::opcaoNumero),
            reforco = listOf(pt("${porExtenso(certo)} é o ${if (maior) "maior" else "menor"}!")),
        )
    }

    /** Memória: o número com o grupo que tem aquela quantidade (até 9). */
    private fun memoriaDeQuantidades(): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val nums = (1..9).toList().sorteia(4)
        return Pergunta.deAtividade(
            instrucao = "Número com a quantidade",
            fala = listOf(pt("Jogo da memória! Junte cada número com o grupo que tem aquela quantidade.")),
            atividade = Atividade.Memoria(nums.map { it.toString() to obj.emoji.repeat(it) }),
            reforco = listOf(pt("Isso! Cada número com a sua quantidade.")),
        )
    }

    private val matematicaMundos: List<Modelo> = listOf(
        // Mundo 3 (Praia), fases 26 a 30
        Modelo("Contar até 20", "🧮") { if (random.nextInt(3) == 0) ouvirNumero(11..20) else contar(11, 20, 4) },
        Modelo("Escrever os números", "✍️") { if (random.nextInt(3) == 0) ouvirNumero(0..10) else tracarNumero() },
        Modelo("Somar até 20", "➕") { somarAte20() },
        Modelo("Tirar até 20", "➖") { tirarAte20() },
        Modelo("Desafio da praia", "🏆") { when (random.nextInt(4)) { 0 -> contar(11, 20, 4); 1 -> somarAte20(); 2 -> tirarAte20(); else -> tracarNumero() } },
        // Mundo 4 (Montanha), fases 31 a 40
        Modelo("De 10 em 10", "🔟") { deDezEmDez(50) },
        Modelo("Dezenas e unidades", "📦") { dezenasEUnidades() },
        Modelo("Números até 50", "5️⃣") { ateCinquenta() },
        Modelo("O dobro", "👯") { dobro(6) },
        Modelo("A metade", "🍕") { metade(12) },
        Modelo("Pesado, leve, alto", "⚖️") { medidas() },
        Modelo("Horas cheias", "🕒") { horas() },
        Modelo("Moedinhas", "🪙") { dinheiro() },
        Modelo("Gráfico de figuras", "📊") { grafico() },
        Modelo("Desafio da montanha", "🏆") { when (random.nextInt(5)) { 0 -> dezenasEUnidades(); 1 -> dobro(6); 2 -> horas(); 3 -> dinheiro(); else -> grafico() } },
        // Mundo 5 (Espaço), fases 41 a 50
        Modelo("Até 100", "💯") { ateCem() },
        Modelo("Somar dezenas", "🚀") { somarDezenas() },
        Modelo("Problemas de somar", "📖") { problema(soma = true) },
        Modelo("Problemas de tirar", "📕") { problema(soma = false) },
        Modelo("Filas e formas", "📍") { posicoesEFormas() },
        Modelo("Pulando números", "🦘") { sequenciaNumerica(50) },
        Modelo("Comparar até 100", "🏔️") { compararAte100() },
        Modelo("Dobro, metade e horas", "🕐") { when (random.nextInt(3)) { 0 -> dobro(10); 1 -> metade(20); else -> horas() } },
        Modelo("Problemas espertos", "🧠") { when (random.nextInt(3)) { 0 -> problema(true, doisPassos = true); 1 -> memoriaDeQuantidades(); else -> problema(random.nextBoolean()) } },
        Modelo("Mestre dos números", "🌟") { when (random.nextInt(6)) { 0 -> somarDezenas(); 1 -> problema(true, true); 2 -> compararAte100(); 3 -> dinheiro(); 4 -> grafico(); else -> sequenciaNumerica(100) } },
    )

    // ------------------------------------------------------------------ PORTUGUÊS

    /** Fonético: o som esticado da letra (mmmm, ssss) e a figura que começa com ele. */
    private fun somDaLetra(): Pergunta {
        val sons = V.sonsDasLetras
        val s = sons.um()
        val outros = sons.filter { it.letra != s.letra }.sorteia(3)
        return if (random.nextBoolean()) {
            montar(
                instrucao = "Que letra faz este som?",
                fala = listOf(pt("Escute: ${s.som}. ${s.palavra}. Que letra faz o som ${s.som}?")),
                cena = Cena.Emojis(s.emoji),
                certa = opcaoLetra(s.letra),
                erradas = outros.map { opcaoLetra(it.letra) },
                reforco = listOf(pt("${s.som}... ${s.palavra}! É a letra ${falaDaLetra(s.letra)}.")),
            )
        } else {
            montar(
                instrucao = "Começa com o som ${s.som.take(3)}",
                fala = listOf(pt("Escute: ${s.som}. Qual figura começa com esse som?")),
                cena = Cena.Nenhuma,
                certa = opcaoFigura(s.emoji, s.palavra),
                erradas = outros.map { opcaoFigura(it.emoji, it.palavra) },
                reforco = listOf(pt("${s.som}... ${s.palavra}!")),
            )
        }
    }

    private fun opcaoSilaba(s: String) = Opcao(Visual.Texto(s), listOf(pt(s.lowercase())))

    /** Famílias silábicas: BA BE BI BO BU. */
    private fun familiaSilabica(): Pergunta {
        val c = V.familias.um()
        val familia = Vocabulario.vogais.map { c + it }
        val certa = familia.um()
        return montar(
            instrucao = "Toque no $certa",
            fala = listOf(pt("A família do ${falaDaLetra(c)}: ${familia.joinToString(", ") { it.lowercase() }}. Toque no ${certa.lowercase()}.")),
            cena = Cena.Nenhuma,
            certa = opcaoSilaba(certa),
            erradas = familia.filter { it != certa }.sorteia(3).map(::opcaoSilaba),
            reforco = listOf(pt("${certa.lowercase()}! ${falaDaLetra(c)} com ${falaDaLetra(certa.last().toString())}, ${certa.lowercase()}.")),
        )
    }

    /** Completar a família: BA BE ? BO BU. As erradas têm a mesma vogal com outra consoante. */
    private fun completarFamilia(): Pergunta {
        val c = V.familias.um()
        val familia = Vocabulario.vogais.map { c + it }
        val falta = familia.indices.random(random)
        val certa = familia[falta]
        val vogal = certa.last()
        val erradas = V.familias.filter { it != c }.sorteia(3).map { it + vogal }
        return montar(
            instrucao = "Qual pedacinho falta?",
            fala = listOf(pt("Esta é a família do ${falaDaLetra(c)}. Qual pedacinho está faltando?")),
            cena = Cena.Sequencia(familia.mapIndexed { i, s -> if (i == falta) "?" else s }),
            certa = opcaoSilaba(certa),
            erradas = erradas.map(::opcaoSilaba),
            reforco = listOf(pt(familia.joinToString(", ") { it.lowercase() } + "!")),
        )
    }

    /**
     * Três pedacinhos parecidos com [s], para as opções erradas (CHA: CA, XA, SA; LHA: LA, NHA;
     * PRA: PA, PLA). Nunca devolve [s] nem os pedacinhos de [evitar].
     */
    private fun silabasParecidas(s: String, evitar: Collection<String> = emptyList()): List<String> {
        val r = mutableListOf<String>()
        when {
            s.startsWith("CH") -> r += listOf("C", "X", "S").map { it + s.drop(2) }
            s.startsWith("LH") -> r += listOf("L", "NH", "LI").map { it + s.drop(2) }
            s.startsWith("NH") -> r += listOf("N", "LH", "NI").map { it + s.drop(2) }
            s.length >= 3 && s[1] in "RL" -> {
                r += s[0] + s.drop(2)
                r += s[0] + (if (s[1] == 'R') "L" else "R") + s.drop(2)
                r += listOf("B", "P", "T", "G", "F", "C").filter { it[0] != s[0] }.um() + s.drop(1)
            }
        }
        if (s.last().toString() in Vocabulario.vogais) {
            r += Vocabulario.vogais.filter { it.last() != s.last() }.sorteia(3).map { s.dropLast(1) + it }
        }
        val banco = (Vocabulario.todasAsPalavras + V.palavrasGrandes).flatMap { it.silabas } + V.silabasComplexas
        r += banco.filter { it.length == s.length }.shuffled(random)
        r += banco.shuffled(random)
        return r.distinct().filter { it != s && it.isNotBlank() && it !in evitar }.take(3)
    }

    /** Sílaba que falta numa palavra (MA__CO). [lista] decide se são palavras simples ou difíceis. */
    private fun silabaQueFalta(lista: List<Palavra>, soDificil: Boolean = false): Pergunta {
        val p = lista.filter { it.silabas.size >= 2 || soDificil }.um()
        val dificeis = p.silabas.indices.filter { i -> val s = p.silabas[i]; s.contains('H') || (s.length >= 3 && s[1] in "RL") }
        val i = if (soDificil && dificeis.isNotEmpty()) dificeis.um() else p.silabas.indices.random(random)
        val certa = p.silabas[i]
        val escrita = p.silabas.mapIndexed { j, s -> if (j == i) "__" else s }.joinToString("")
        return montar(
            instrucao = "Que pedacinho falta?",
            fala = listOf(pt("${p.texto.lowercase()}. Que pedacinho está faltando?")),
            cena = Cena.Palavra(p.emoji + " " + escrita),
            certa = opcaoSilaba(certa),
            erradas = silabasParecidas(certa, evitar = p.silabas).map(::opcaoSilaba),
            reforco = listOf(pt(p.silabas.joinToString(", ") { it.lowercase() } + ". " + p.texto.lowercase() + "!")),
        )
    }

    /** Sílabas complexas: com que pedacinho começa CHAVE, PRATO, BRUXA? */
    private fun comecoDificil(): Pergunta {
        val p = V.palavrasComplexas.filter { val s = it.silabas.first(); s.startsWith("CH") || (s.length >= 3 && s[1] in "RL") }.um()
        val primeira = p.silabas.first()
        return montar(
            instrucao = "Com que pedacinho começa?",
            fala = listOf(pt("${p.texto.lowercase()}. Com que pedacinho começa ${p.texto.lowercase()}?")),
            cena = Cena.Emojis(p.emoji),
            certa = opcaoSilaba(primeira),
            erradas = silabasParecidas(primeira).map(::opcaoSilaba),
            reforco = listOf(pt(p.silabas.joinToString(", ") { it.lowercase() } + ". " + p.texto.lowercase() + "!")),
        )
    }

    /** Minúsculas: achar a letra pequena (com as parecidas b, d, p, q juntas) ou a grande da pequena. */
    private fun minusculas(): Pergunta {
        val confundem = listOf("B", "D", "P", "Q")
        val outras = listOf("A", "E", "O", "M", "N", "H", "F", "T", "L", "G", "R", "U")
        val conjunto = if (random.nextBoolean()) confundem else outras
        val (certa, e1, e2, e3) = conjunto.sorteia(4)
        val opMin = { l: String -> Opcao(Visual.Texto(l.lowercase()), listOf(pt(falaDaLetra(l)))) }
        return if (random.nextBoolean()) {
            montar(
                instrucao = "Ache o ${certa.lowercase()} pequeno",
                fala = listOf(pt("Toque na letra ${falaDaLetra(certa)} pequena.")),
                cena = Cena.Nenhuma,
                certa = opMin(certa),
                erradas = listOf(opMin(e1), opMin(e2), opMin(e3)),
                reforco = listOf(pt("Essa é a letra ${falaDaLetra(certa)} pequena!")),
            )
        } else {
            montar(
                instrucao = "Qual é a grande?",
                fala = listOf(pt("Esta é uma letra pequena. Qual é a letra grande igual a ela?")),
                cena = Cena.Palavra(certa.lowercase()),
                certa = opcaoLetra(certa),
                erradas = listOf(e1, e2, e3).map(::opcaoLetra),
                reforco = listOf(pt("${certa.lowercase()} pequeno e $certa grande: letra ${falaDaLetra(certa)}!")),
            )
        }
    }

    private fun tracarMinuscula(letras: List<String>): Pergunta {
        val l = letras.um().lowercase()
        return Pergunta.deAtividade(
            instrucao = "Trace a letra $l",
            fala = listOf(pt("Passe o dedo por cima da letra ${falaDaLetra(l)} pequena, seguindo os pontinhos.")),
            atividade = Atividade.Tracar(l),
            reforco = listOf(pt("Você escreveu a letra ${falaDaLetra(l)} pequena!")),
        )
    }

    private fun quantasSilabasGrandes(): Pergunta {
        val p = V.palavrasGrandes.um()
        val n = p.silabas.size
        return montar(
            instrucao = "Quantos pedacinhos?",
            fala = listOf(pt("Bata palmas comigo: " + p.silabas.joinToString(", ") { it.lowercase() } + ". Quantos pedacinhos tem ${p.texto.lowercase()}?")),
            cena = Cena.Palavra(p.emoji + " " + p.texto),
            certa = opcaoNumero(n),
            erradas = (1..6).filter { it != n }.sorteia(3).map(::opcaoNumero),
            reforco = listOf(pt(p.silabas.joinToString(", ") { it.lowercase() } + ". ${porExtenso(n)} pedacinhos!")),
        )
    }

    private fun montarGrande(silabas: Int): Pergunta {
        val p = V.palavrasGrandes.filter { it.silabas.size == silabas }.um()
        val extras = (Vocabulario.todasAsPalavras + V.palavrasGrandes).flatMap { it.silabas }.distinct()
            .filter { it !in p.silabas && it.length == 2 }.sorteia(2)
        return Pergunta.deAtividade(
            instrucao = "Monte: ${p.texto}",
            fala = listOf(pt("Monte a palavra ${p.texto.lowercase()}. Arraste os pedacinhos na ordem.")),
            atividade = Atividade.MontarPalavra(p.emoji, p.texto, p.silabas, extras),
            reforco = listOf(pt(p.silabas.joinToString(", ") { it.lowercase() } + ". " + p.texto.lowercase() + "!")),
        )
    }

    /** Montar a palavra letra por letra (S, O, L). */
    private fun montarComLetras(): Pergunta {
        val p = V.palavrasCurtas.filter { it.texto.length <= 5 }.um()
        val letras = p.texto.map { it.toString() }
        val extras = (Vocabulario.vogais + Vocabulario.consoantes).filter { it !in letras }.sorteia(2)
        return Pergunta.deAtividade(
            instrucao = "Monte com letras: ${p.texto}",
            fala = listOf(pt("Monte a palavra ${p.texto.lowercase()} com as letras. Escute: ${p.texto.lowercase()}. Arraste uma letra de cada vez, na ordem.")),
            atividade = Atividade.MontarPalavra(p.emoji, p.texto, letras, extras),
            reforco = listOf(pt(letras.joinToString(", ") { falaDaLetra(it) } + ". " + p.texto.lowercase() + "!")),
        )
    }

    /** Ler uma frase curta e escolher a figura. Com [dificil], as erradas começam parecido. */
    private fun lerFrase(dificil: Boolean): Pergunta {
        val f = V.frases.um()
        val segunda = { fr: VocabularioDosMundos.Frase -> fr.texto.split(" ").getOrNull(1)?.firstOrNull() }
        val outras = V.frases.filter { it.emoji != f.emoji }
            .sortedBy { if (dificil && segunda(it) == segunda(f)) 0.0 else 1.0 + random.nextDouble() }
            .distinctBy { it.emoji }
            .take(3)
        return montar(
            instrucao = "Leia a frase",
            fala = listOf(pt("Leia a frase com calma. Qual figura combina com ela?")),
            cena = Cena.Palavra(f.texto),
            certa = opcaoFigura(f.emoji),
            erradas = outras.map { opcaoFigura(it.emoji) },
            reforco = listOf(pt("Está escrito: ${f.texto.lowercase()}!")),
        )
    }

    /** Ordem alfabética: depois, antes, ou qual vem primeiro. */
    private fun ordemAlfabetica(): Pergunta {
        val abc = Vocabulario.alfabeto
        return when (random.nextInt(3)) {
            0 -> {
                val i = (0..abc.size - 4).random(random)
                val fila = abc.subList(i, i + 4)
                val resposta = fila.last()
                montar(
                    instrucao = "Que letra vem depois?",
                    fala = listOf(pt(fila.dropLast(1).joinToString(", ") { falaDaLetra(it) } + ". Que letra vem depois?")),
                    cena = Cena.Sequencia(fila.dropLast(1) + "?"),
                    certa = opcaoLetra(resposta),
                    erradas = abc.filter { it != resposta && it !in fila }.sortedBy { kotlin.math.abs(abc.indexOf(it) - abc.indexOf(resposta)) + random.nextDouble() * 3 }.take(3).map(::opcaoLetra),
                    reforco = listOf(pt(fila.joinToString(", ") { falaDaLetra(it) } + "!")),
                )
            }
            1 -> {
                val i = (1 until abc.size).random(random)
                val resposta = abc[i - 1]
                montar(
                    instrucao = "Que letra vem antes do ${abc[i]}?",
                    fala = listOf(pt("Que letra vem antes do ${falaDaLetra(abc[i])}?")),
                    cena = Cena.Sequencia(listOf("?", abc[i])),
                    certa = opcaoLetra(resposta),
                    erradas = abc.filter { it != resposta && it != abc[i] }.sortedBy { kotlin.math.abs(abc.indexOf(it) - i) + random.nextDouble() * 3 }.take(3).map(::opcaoLetra),
                    reforco = listOf(pt("${falaDaLetra(resposta)}, ${falaDaLetra(abc[i])}!")),
                )
            }
            else -> {
                val letras = abc.sorteia(4)
                val certa = letras.minBy { abc.indexOf(it) }
                montar(
                    instrucao = "Qual vem primeiro no alfabeto?",
                    fala = listOf(pt("Qual destas letras vem primeiro no alfabeto? Lembre: á, bê, cê, dê...")),
                    cena = Cena.Nenhuma,
                    certa = opcaoLetra(certa),
                    erradas = letras.filter { it != certa }.map(::opcaoLetra),
                    reforco = listOf(pt("${falaDaLetra(certa)} vem primeiro!")),
                )
            }
        }
    }

    /** Memória da letra grande com a pequena (A com a). */
    private fun memoriaGrandePequena(): Pergunta {
        val letras = listOf("A", "B", "D", "E", "G", "M", "N", "Q", "R", "T").sorteia(4)
        val falas = letras.flatMap { listOf(it to pt(falaDaLetra(it)), it.lowercase() to pt(falaDaLetra(it))) }.toMap()
        return Pergunta.deAtividade(
            instrucao = "Grande com pequena",
            fala = listOf(pt("Jogo da memória! Junte cada letra grande com a mesma letra pequena.")),
            atividade = Atividade.Memoria(letras.map { it to it.lowercase() }, falas),
            reforco = listOf(pt("Muito bem! Letras grandes e pequenas juntinhas.")),
        )
    }

    private fun quantasLetras(): Pergunta {
        val p = (V.palavrasCurtas + V.palavrasComplexas.filter { it.texto.length <= 7 }).um()
        val n = p.texto.length
        return montar(
            instrucao = "Quantas letras?",
            fala = listOf(pt("${p.texto.lowercase()}. Conte as letras. Quantas letras tem ${p.texto.lowercase()}?")),
            cena = Cena.Palavra(p.emoji + " " + p.texto),
            certa = opcaoNumero(n),
            erradas = erradosDe(n, 2..9).map(::opcaoNumero),
            reforco = listOf(pt(p.texto.map { falaDaLetra(it.toString()) }.joinToString(", ") + ". ${porExtenso(n, true)} letras!")),
        )
    }

    /** Escritas erradas parecidas: troca uma letra por outra parecida, tira uma letra ou inverte duas. */
    private fun escritasErradas(palavra: String): List<String> {
        val trocas = mapOf('B' to "DP", 'D' to "BT", 'P' to "BQ", 'T' to "DL", 'F' to "V", 'V' to "F", 'M' to "N", 'N' to "M", 'L' to "R", 'R' to "L",
            'G' to "C", 'C' to "G", 'S' to "Z", 'Z' to "S", 'A' to "O", 'O' to "U", 'E' to "I", 'U' to "O", 'I' to "E")
        val r = mutableSetOf<String>()
        var tentativas = 0
        while (r.size < 3 && tentativas < 60) {
            tentativas++
            val i = palavra.indices.random(random)
            val nova = when (random.nextInt(3)) {
                0 -> trocas[palavra[i]]?.let { palavra.replaceRange(i, i + 1, it.random(random).toString()) }
                1 -> if (palavra.length > 3) palavra.removeRange(i, i + 1) else null
                else -> if (i < palavra.lastIndex) palavra.replaceRange(i, i + 2, "${palavra[i + 1]}${palavra[i]}") else null
            }
            if (nova != null && nova != palavra) r += nova
        }
        return r.toList()
    }

    private fun comoSeEscreve(): Pergunta {
        val p = V.palavrasCurtas.um()
        val erradas = escritasErradas(p.texto)
        return montar(
            instrucao = "Como se escreve?",
            fala = listOf(pt("${p.texto.lowercase()}. Qual destas está escrita certinho? Leia com atenção.")),
            cena = Cena.Emojis(p.emoji),
            certa = opcaoTexto(p.texto),
            erradas = erradas.map { opcaoTexto(it) },
            reforco = listOf(pt("${p.texto.lowercase()}: " + p.texto.map { falaDaLetra(it.toString()) }.joinToString(", ") + "!")),
        )
    }

    private fun ultimaLetra(): Pergunta {
        val p = (V.palavrasCurtas + Vocabulario.palavrasComConsoante).um()
        val ultima = p.texto.last().toString()
        val erradas = (Vocabulario.vogais + listOf("L", "R", "S", "M")).filter { it != ultima }.sorteia(3)
        return montar(
            instrucao = "Com que letra termina?",
            fala = listOf(pt("${p.texto.lowercase()}. Com que letra termina ${p.texto.lowercase()}?")),
            cena = Cena.Palavra(p.emoji + " " + p.texto.dropLast(1) + "_"),
            certa = opcaoLetra(ultima),
            erradas = erradas.map(::opcaoLetra),
            reforco = listOf(pt("${p.texto.lowercase()} termina com ${falaDaLetra(ultima)}!")),
        )
    }

    /** Qual palavra vem primeiro no dicionário (iniciais diferentes)? */
    private fun ordemDasPalavras(): Pergunta {
        val palavras = (V.palavrasCurtas + Vocabulario.palavrasComConsoante).filter { it.texto.length <= 6 }.distinctBy { it.inicial }.sorteia(4)
        val certa = palavras.minBy { it.texto }
        val op = { p: Palavra -> opcaoTexto(p.texto, p.texto.lowercase()) }
        return montar(
            instrucao = "Qual vem primeiro?",
            fala = listOf(pt("Qual destas palavras vem primeiro no alfabeto? Olhe a primeira letra de cada uma.")),
            cena = Cena.Nenhuma,
            certa = op(certa),
            erradas = palavras.filter { it != certa }.map(op),
            reforco = listOf(pt("${certa.texto.lowercase()} começa com ${falaDaLetra(certa.inicial)}, que vem primeiro!")),
        )
    }

    private val minusculasFaceis = listOf("a", "c", "e", "i", "l", "o", "u", "t", "v", "x", "z")
    private val minusculasTodas = ('a'..'z').map { it.toString() }

    private val portuguesMundos: List<Modelo> = listOf(
        // Mundo 3 (Praia), fases 26 a 30
        Modelo("Som das letras", "👂") { somDaLetra() },
        Modelo("Famílias: BA BE BI", "👨‍👩‍👧") { familiaSilabica() },
        Modelo("Completar a família", "🧩") { if (random.nextInt(3) == 0) familiaSilabica() else completarFamilia() },
        Modelo("Pedacinho que falta", "✂️") { silabaQueFalta(Vocabulario.todasAsPalavras) },
        Modelo("Desafio da praia", "🏆") { when (random.nextInt(4)) { 0 -> somDaLetra(); 1 -> completarFamilia(); 2 -> silabaQueFalta(Vocabulario.todasAsPalavras); else -> montarPalavra(2) } },
        // Mundo 4 (Montanha), fases 31 a 40
        Modelo("Letras pequenas", "🔡") { minusculas() },
        Modelo("Traçar letras pequenas", "✍️") { if (random.nextInt(4) == 0) minusculas() else tracarMinuscula(minusculasFaceis) },
        Modelo("CH, LH e NH", "🔑") { if (random.nextBoolean()) comecoDificil() else silabaQueFalta(V.palavrasComplexas, soDificil = true) },
        Modelo("Palavras grandes", "🐛") { when (random.nextInt(3)) { 0 -> quantasSilabasGrandes(); else -> montarGrande(3) } },
        Modelo("Montar com letras", "🔠") { montarComLetras() },
        Modelo("Ler frases", "📜") { lerFrase(dificil = false) },
        Modelo("Ordem do alfabeto", "🔤") { ordemAlfabetica() },
        Modelo("Grande e pequena", "🃏") { if (random.nextBoolean()) memoriaGrandePequena() else minusculas() },
        Modelo("BR, PR e TR", "🚂") { if (random.nextBoolean()) comecoDificil() else silabaQueFalta(V.palavrasComplexas.filter { it.silabas.any { s -> s.length >= 3 && s[1] in "RL" } }, soDificil = true) },
        Modelo("Desafio da montanha", "🏆") { when (random.nextInt(5)) { 0 -> lerFrase(false); 1 -> montarComLetras(); 2 -> ordemAlfabetica(); 3 -> comecoDificil(); else -> tracarMinuscula(minusculasFaceis) } },
        // Mundo 5 (Espaço), fases 41 a 50
        Modelo("Montar 4 pedacinhos", "🏗️") { if (random.nextInt(3) == 0) quantasSilabasGrandes() else montarGrande(4) },
        Modelo("Quantas letras?", "🔢") { quantasLetras() },
        Modelo("Como se escreve?", "✏️") { comoSeEscreve() },
        Modelo("Última letra", "🔚") { ultimaLetra() },
        Modelo("Traçar o alfabeto", "📝") { if (random.nextBoolean()) tracarMinuscula(minusculasTodas) else tracar(Vocabulario.alfabeto) },
        Modelo("Rimas e sons", "🎵") { when (random.nextInt(3)) { 0 -> rima(); 1 -> somDaLetra(); else -> comecaIgual() } },
        Modelo("Frases caprichadas", "📖") { lerFrase(dificil = true) },
        Modelo("Palavras em ordem", "📚") { if (random.nextBoolean()) ordemDasPalavras() else ordemAlfabetica() },
        Modelo("Sílabas difíceis", "🧠") { when (random.nextInt(3)) { 0 -> comecoDificil(); 1 -> silabaQueFalta(V.palavrasComplexas, soDificil = true); else -> silabaQueFalta(V.palavrasGrandes) } },
        Modelo("Mestre das letras", "🌟") { when (random.nextInt(6)) { 0 -> lerFrase(true); 1 -> comoSeEscreve(); 2 -> montarGrande(4); 3 -> quantasLetras(); 4 -> ordemDasPalavras(); else -> tracarMinuscula(minusculasTodas) } },
    )

    // ------------------------------------------------------------------ INGLÊS

    private fun quantosEmIngles(max: Int): Pergunta {
        val obj = Vocabulario.objetosParaContar.um()
        val n = (2..max).random(random)
        val op = { k: Int -> Opcao(Visual.Texto(k.toString()), listOf(en(Vocabulario.numerosEn[k]))) }
        return montar(
            instrucao = "How many?",
            fala = listOf(pt("Conte em inglês!"), en("How many?")),
            cena = Cena.Emojis(obj.emoji.repeat(n)),
            certa = op(n),
            erradas = erradosDe(n, 1..maxOf(max, 6)).map(op),
            reforco = listOf(en(Vocabulario.numerosEn[n].replaceFirstChar { it.uppercase() } + "!"), pt("${porExtenso(n, obj.feminino)} ${obj.nome(n)}.")),
        )
    }

    private fun corDaCoisa(): Pergunta {
        val (emoji, cor) = V.coisasColoridas.um()
        val certa = Vocabulario.todasAsCores.first { it.en == cor }
        val erradas = Vocabulario.todasAsCores.filter { it.en != cor }.sorteia(3)
        val op = { c: PalavraEn -> Opcao(Visual.Cor(c.cor ?: 0xFF000000), listOf(en(c.en))) }
        return montar(
            instrucao = "What color is it?",
            fala = listOf(en("What color is it?"), pt("Que cor é esta?")),
            cena = Cena.Emojis(emoji),
            certa = op(certa),
            erradas = erradas.map(op),
            reforco = listOf(en(certa.en.replaceFirstChar { it.uppercase() } + "!"), pt("${certa.pt}!")),
        )
    }

    /** Frase simples em inglês ("I like pizza") e a figura que combina. */
    private fun fraseEmIngles(lista: List<VocabularioDosMundos.FraseEn>): Pergunta {
        val f = lista.um()
        val erradas = lista.filter { it.emoji != f.emoji }.distinctBy { it.emoji }.sorteia(3)
        // A frase aparece só no cartão do meio (é ali que a criança lê); o balão de cima dá a instrução.
        return montar(
            instrucao = "Leia e escolha a figura",
            fala = listOf(pt("Leia a frase em inglês, ou escute:"), en(f.en + "."), pt("Qual figura combina com a frase?")),
            cena = Cena.Palavra(f.en),
            certa = opcaoFigura(f.emoji),
            erradas = erradas.map { opcaoFigura(it.emoji) },
            reforco = listOf(en(f.en + "!"), pt(f.pt + ".")),
        )
    }

    private val inglesMundos: List<Modelo> = listOf(
        // Mundo 3 (Praia), fases 26 a 30
        Modelo("School", "🏫") { encontreEmIngles(V.escola, 4) },
        Modelo("Numbers 11 to 15", "🔢") { numeroEmIngles(11..15, 9..17, 4) },
        Modelo("Nature", "🌳") { encontreEmIngles(V.natureza, 4) },
        Modelo("Feelings", "😀") { encontreEmIngles(V.sentimentos, 4) },
        Modelo("Beach challenge", "🏆") { when (random.nextInt(4)) { 0 -> encontreEmIngles(V.escola, 4); 1 -> encontreEmIngles(V.natureza, 4); 2 -> encontreEmIngles(V.sentimentos, 4); else -> numeroEmIngles(11..15, 9..17, 4) } },
        // Mundo 4 (Montanha), fases 31 a 40
        Modelo("Numbers 16 to 20", "🔢") { numeroEmIngles(16..20, 13..20, 4) },
        Modelo("Jobs", "👷") { encontreEmIngles(V.profissoes, 4) },
        Modelo("Verbs", "🚶") { encontreEmIngles(V.verbos, 4) },
        Modelo("I like", "💛") { fraseEmIngles(V.gosto) },
        Modelo("What color is it?", "🎨") { corDaCoisa() },
        Modelo("Memory: school", "🃏") { if (random.nextBoolean()) memoria(4, V.escola) else encontreEmIngles(V.escola, 4) },
        Modelo("How many?", "🖐️") { quantosEmIngles(12) },
        Modelo("Wild animals", "🦒") { encontreEmIngles(V.animaisSelvagens, 4) },
        Modelo("I am, I can", "💪") { fraseEmIngles(V.souEPosso) },
        Modelo("Mountain challenge", "🏆") { when (random.nextInt(5)) { 0 -> encontreEmIngles(V.profissoes, 4); 1 -> encontreEmIngles(V.verbos, 4); 2 -> fraseEmIngles(V.gosto); 3 -> corDaCoisa(); else -> numeroEmIngles(11..20, 11..20, 4) } },
        // Mundo 5 (Espaço), fases 41 a 50
        Modelo("Opposites", "↕️") { encontreEmIngles(V.opostos, 4) },
        Modelo("Colors and numbers", "🌈") { when (random.nextInt(3)) { 0 -> corDaCoisa(); 1 -> encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor"); else -> numeroEmIngles(1..20, 1..20, 4) } },
        Modelo("School and jobs", "🎒") { encontreEmIngles(V.escola + V.profissoes, 4) },
        Modelo("Feelings and verbs", "🎭") { encontreEmIngles(V.sentimentos + V.verbos, 4) },
        Modelo("Memory: wild animals", "🃏") { if (random.nextBoolean()) memoria(5, V.animaisSelvagens) else encontreEmIngles(V.animaisSelvagens, 4) },
        Modelo("I like, I can", "🗣️") { fraseEmIngles(V.gosto + V.souEPosso) },
        Modelo("Numbers 1 to 20", "💯") { if (random.nextBoolean()) numeroEmIngles(1..20, 1..20, 4) else quantosEmIngles(15) },
        Modelo("Nature and weather", "🌦️") { encontreEmIngles(V.natureza + Vocabulario.tempo, 4) },
        Modelo("Little sentences", "💬") { when (random.nextInt(3)) { 0 -> fraseEmIngles(V.gosto); 1 -> fraseEmIngles(V.souEPosso); else -> corDaCoisa() } },
        Modelo("English master", "🌟") { when (random.nextInt(6)) { 0 -> encontreEmIngles(V.animaisSelvagens + V.natureza, 4); 1 -> encontreEmIngles(V.opostos + V.sentimentos, 4); 2 -> fraseEmIngles(V.gosto + V.souEPosso); 3 -> numeroEmIngles(1..20, 1..20, 4); 4 -> corDaCoisa(); else -> memoria(5, V.escola + V.profissoes) } },
    )

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

    private enum class Direcao(val dl: Int, val dc: Int, val seta: String, val nome: String) {
        CIMA(-1, 0, "⬆️", "para cima"), BAIXO(1, 0, "⬇️", "para baixo"), ESQUERDA(0, -1, "⬅️", "para a esquerda"), DIREITA(0, 1, "➡️", "para a direita"),
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
        val op = { x: Direcao -> opcaoFigura(x.seta, x.nome) }
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

    private val logicaMundos: List<Modelo> = listOf(
        // Mundo 3 (Praia), fases 26 a 30
        Modelo("Filas de 4", "🔷") { filaComPadrao(listOf("ABCD", "AABB", "ABCC").um()) },
        Modelo("Quem não é da turma?", "🕵️") { intruso() },
        Modelo("Cor e forma", "🟦") { corEForma(dificil = false) },
        Modelo("Memória de 6", "🃏") { memoria(6) },
        Modelo("Desafio da praia", "🏆") { when (random.nextInt(4)) { 0 -> filaComPadrao("ABCD"); 1 -> intruso(); 2 -> corEForma(false); else -> memoria(5) } },
        // Mundo 4 (Montanha), fases 31 a 40
        Modelo("Labirinto", "🐭") { labirinto(grande = false) },
        Modelo("Sudoku fácil", "🧩") { sudoku(escondidas = 0) },
        Modelo("Pulando números", "🦘") { sequenciaNumerica(30) },
        Modelo("Cesta esperta", "🧺") { cestaDupla() },
        Modelo("Intrusos espertos", "🔎") { intrusoEsperto() },
        Modelo("Labirinto dos passos", "👣") { labirinto(grande = random.nextBoolean()) },
        Modelo("Sudoku", "🧩") { sudoku(escondidas = 2) },
        Modelo("Cor e forma difícil", "🎨") { corEForma(dificil = true) },
        Modelo("Combina e sombras", "🔦") { when (random.nextInt(3)) { 0 -> combina(); 1 -> sombra(); else -> intruso() } },
        Modelo("Desafio da montanha", "🏆") { when (random.nextInt(5)) { 0 -> labirinto(false); 1 -> sudoku(1); 2 -> cestaDupla(); 3 -> intrusoEsperto(); else -> filaComPadrao("AABB") } },
        // Mundo 5 (Espaço), fases 41 a 50
        Modelo("Filas gigantes", "🎨") { filaComPadrao(listOf("ABCD", "AABC", "ABBC", "AABBC").um()) },
        Modelo("Labirinto grande", "🌀") { labirinto(grande = true) },
        Modelo("Sudoku difícil", "🧠") { sudoku(escondidas = 4) },
        Modelo("Memória gigante de 6", "🃏") { memoria(6, Vocabulario.animais + V.animaisSelvagens) },
        Modelo("Cesta dupla", "🧺") { if (random.nextBoolean()) cestaDupla() else cestaDoGrupo() },
        Modelo("Detetive do espaço", "🔭") { when (random.nextInt(3)) { 0 -> intrusoEsperto(); 1 -> sombra(); else -> corEForma(true) } },
        Modelo("Números e figuras", "🔢") { if (random.nextBoolean()) sequenciaNumerica(50) else filaComPadrao("ABCD") },
        Modelo("Pensa rápido 2", "⚡") { when (random.nextInt(4)) { 0 -> intruso(); 1 -> corEForma(true); 2 -> labirinto(false); else -> numerosEmOrdem() } },
        Modelo("Grande desafio", "🏅") { when (random.nextInt(4)) { 0 -> sudoku(3); 1 -> labirinto(true); 2 -> filaComPadrao("AABBC"); else -> intrusoEsperto() } },
        Modelo("Mestre da lógica", "🌟") { when (random.nextInt(6)) { 0 -> sudoku(4); 1 -> labirinto(true); 2 -> memoria(6); 3 -> cestaDupla(); 4 -> corEForma(true); else -> filaComPadrao(listOf("ABCD", "AABBC").um()) } },
    )

    companion object {
        const val FASES_POR_TRILHA = 50
        const val PERGUNTAS_POR_FASE = 5
    }
}
