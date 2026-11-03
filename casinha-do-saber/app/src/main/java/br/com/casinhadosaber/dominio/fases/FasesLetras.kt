package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.*
import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Fases do caminho Letras (português): 250 fases em 25 mundos de 10 ([Mundos]).
 * As fases 1 a 50 são as de sempre (o progresso salvo continua valendo). As fases 51 a 250 (mundos 6 a
 * 25) seguem o plano de docs/plano-1000-fases.md, seção 4.2: alfabetização até o fim do 2º ano (BNCC),
 * com o vocabulário em [VocabularioLetras] e o esqueleto de cada mundo (fase 1 de tocar, desafios de
 * mexer, marco na fase 5, revisão do mundo anterior na fase 8 e chefão na fase 10).
 */
class FasesLetras(random: Random = Random.Default) : FasesDoCaminho(random) {

    override val trilha = Trilha.PORTUGUES

    override fun fase(numero: Int): ModeloDeFase {
        conferir(numero)
        return if (numero <= FASES_PRONTAS) ate50[numero - 1] else mundosNovos[numero - FASES_PRONTAS - 1]
    }

    /** As 50 fases de antes, na ordem (a fase 1 é o índice 0). */
    private val ate50: List<ModeloDeFase> by lazy { portugues + portuguesMundos }

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
        val p = palavras.umDoTema { it.emoji }
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
        val certa = Vocabulario.palavrasComConsoante.umDoTema { it.emoji }
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
        val p = Vocabulario.palavrasComConsoante.umDoTema { it.emoji }
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

    private val portugues: List<ModeloDeFase> = listOf(
        ModeloDeFase("Vogais A, E, I, O", "🅰️") { tocarNaLetra(listOf("A", "E", "I", "O"), 4) },
        ModeloDeFase("Traçar vogais", "✍️") { if (random.nextInt(3) == 0) tocarNaLetra(Vocabulario.vogais, 4) else tracar(Vocabulario.vogais) },
        ModeloDeFase("Todas as vogais", "🔤") { tocarNaLetra(Vocabulario.vogais, 4) },
        ModeloDeFase("Começa com vogal", "🐝") { letraInicial(Vocabulario.palavrasComVogal, Vocabulario.vogais) },
        ModeloDeFase("Grande e pequenina", "🔡") { minuscula() },
        ModeloDeFase("Outras letras", "🅱️") { tocarNaLetra(Vocabulario.consoantes, 4) },
        ModeloDeFase("Traçar letras", "✏️") { if (random.nextInt(3) == 0) tocarNaLetra(Vocabulario.consoantes, 4) else tracar(listOf("L", "T", "V", "M", "N", "Z", "E", "F")) },
        ModeloDeFase("Primeira letra", "🐱") { letraInicial(Vocabulario.palavrasComConsoante, Vocabulario.consoantes) },
        ModeloDeFase("Memória das letras", "🃏") { if (random.nextBoolean()) memoriaDeLetras() else letraInicial(Vocabulario.palavrasComConsoante, Vocabulario.consoantes) },
        ModeloDeFase("Começa igual", "👂") { comecaIgual() },
        ModeloDeFase("Lendo palavras", "📖") { lerPalavra() },
        ModeloDeFase("Pedacinhos", "🧩") { silabaInicial() },
        ModeloDeFase("Quantos pedacinhos", "👏") { quantasSilabas() },
        ModeloDeFase("Montar palavras", "🧱") { if (random.nextInt(3) == 0) silabaInicial() else montarPalavra(2) },
        ModeloDeFase("Complete a palavra", "✏️") { completarPalavra() },
        ModeloDeFase("Rimas", "🎵") { rima() },
        ModeloDeFase("Montar palavras grandes", "🏗️") { if (random.nextInt(3) == 0) montarPalavra(2) else montarPalavra(3) },
        ModeloDeFase("Leitura caprichada", "🔍") { lerPalavraDificil() },
        ModeloDeFase("Traçar e ler", "📝") { when (random.nextInt(3)) { 0 -> tracar(Vocabulario.vogais + listOf("L", "T", "V", "M", "N")); 1 -> lerPalavra(); else -> minuscula() } },
        ModeloDeFase("Palavras e rimas", "🎶") { when (random.nextInt(3)) { 0 -> rima(); 1 -> comecaIgual(); else -> quantasSilabas() } },
        ModeloDeFase("Montar e completar", "🔧") { when (random.nextInt(3)) { 0 -> montarPalavra(2); 1 -> montarPalavra(3); else -> completarPalavra() } },
        ModeloDeFase("Leitor esperto", "🦉") { when (random.nextInt(3)) { 0 -> lerPalavraDificil(); 1 -> lerPalavra(); else -> silabaInicial() } },
        ModeloDeFase("Memória e letras", "🃏") { when (random.nextInt(3)) { 0 -> memoriaDeLetras(); 1 -> letraInicial(Vocabulario.todasAsPalavras, Vocabulario.vogais + Vocabulario.consoantes); else -> minuscula() } },
        ModeloDeFase("Desafio das letras", "🏆") { when (random.nextInt(5)) { 0 -> lerPalavra(); 1 -> silabaInicial(); 2 -> completarPalavra(); 3 -> rima(); else -> montarPalavra(2) } },
        ModeloDeFase("Supercampeão das letras", "👑") { when (random.nextInt(5)) { 0 -> lerPalavraDificil(); 1 -> montarPalavra(3); 2 -> quantasSilabas(); 3 -> comecaIgual(); else -> tracar(Vocabulario.vogais + Vocabulario.consoantes.take(6)) } },
    )

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
        val p = Vocabulario.todasAsPalavras.filter { it.silabas.size == silabas && it.silabas.all { s -> s.length <= 3 } }.umDoTema { it.emoji }
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

    private fun quantasSilabas(): Pergunta {
        val p = Vocabulario.todasAsPalavras.filter { it.silabas.size in 1..4 }.umDoTema { it.emoji }
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
            .ordenarUmaVez { if (dificil && segunda(it) == segunda(f)) 0.0 else 1.0 + random.nextDouble() }
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
                    erradas = abc.filter { it != resposta && it !in fila }.ordenarUmaVez { kotlin.math.abs(abc.indexOf(it) - abc.indexOf(resposta)) + random.nextDouble() * 3 }.take(3).map(::opcaoLetra),
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
                    erradas = abc.filter { it != resposta && it != abc[i] }.ordenarUmaVez { kotlin.math.abs(abc.indexOf(it) - i) + random.nextDouble() * 3 }.take(3).map(::opcaoLetra),
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

    private val portuguesMundos: List<ModeloDeFase> = listOf(
        // Mundo 3 (Praia), fases 26 a 30
        ModeloDeFase("Som das letras", "👂") { somDaLetra() },
        ModeloDeFase("Famílias: BA BE BI", "👨‍👩‍👧") { familiaSilabica() },
        ModeloDeFase("Completar a família", "🧩") { if (random.nextInt(3) == 0) familiaSilabica() else completarFamilia() },
        ModeloDeFase("Pedacinho que falta", "✂️") { silabaQueFalta(Vocabulario.todasAsPalavras) },
        ModeloDeFase("Desafio da praia", "🏆") { when (random.nextInt(4)) { 0 -> somDaLetra(); 1 -> completarFamilia(); 2 -> silabaQueFalta(Vocabulario.todasAsPalavras); else -> montarPalavra(2) } },
        // Mundo 4 (Montanha), fases 31 a 40
        ModeloDeFase("Letras pequenas", "🔡") { minusculas() },
        ModeloDeFase("Traçar letras pequenas", "✍️") { if (random.nextInt(4) == 0) minusculas() else tracarMinuscula(minusculasFaceis) },
        ModeloDeFase("CH, LH e NH", "🔑") { if (random.nextBoolean()) comecoDificil() else silabaQueFalta(V.palavrasComplexas, soDificil = true) },
        ModeloDeFase("Palavras grandes", "🐛") { when (random.nextInt(3)) { 0 -> quantasSilabasGrandes(); else -> montarGrande(3) } },
        ModeloDeFase("Montar com letras", "🔠") { montarComLetras() },
        ModeloDeFase("Ler frases", "📜") { lerFrase(dificil = false) },
        ModeloDeFase("Ordem do alfabeto", "🔤") { ordemAlfabetica() },
        ModeloDeFase("Grande e pequena", "🃏") { if (random.nextBoolean()) memoriaGrandePequena() else minusculas() },
        ModeloDeFase("BR, PR e TR", "🚂") { if (random.nextBoolean()) comecoDificil() else silabaQueFalta(V.palavrasComplexas.filter { it.silabas.any { s -> s.length >= 3 && s[1] in "RL" } }, soDificil = true) },
        ModeloDeFase("Desafio da montanha", "🏆") { when (random.nextInt(5)) { 0 -> lerFrase(false); 1 -> montarComLetras(); 2 -> ordemAlfabetica(); 3 -> comecoDificil(); else -> tracarMinuscula(minusculasFaceis) } },
        // Mundo 5 (Espaço), fases 41 a 50
        ModeloDeFase("Montar 4 pedacinhos", "🏗️") { if (random.nextInt(3) == 0) quantasSilabasGrandes() else montarGrande(4) },
        ModeloDeFase("Quantas letras?", "🔢") { quantasLetras() },
        ModeloDeFase("Como se escreve?", "✏️") { comoSeEscreve() },
        ModeloDeFase("Última letra", "🔚") { ultimaLetra() },
        ModeloDeFase("Traçar o alfabeto", "📝") { if (random.nextBoolean()) tracarMinuscula(minusculasTodas) else tracar(Vocabulario.alfabeto) },
        ModeloDeFase("Rimas e sons", "🎵") { when (random.nextInt(3)) { 0 -> rima(); 1 -> somDaLetra(); else -> comecaIgual() } },
        ModeloDeFase("Frases caprichadas", "📖") { lerFrase(dificil = true) },
        ModeloDeFase("Palavras em ordem", "📚") { if (random.nextBoolean()) ordemDasPalavras() else ordemAlfabetica() },
        ModeloDeFase("Sílabas difíceis", "🧠") { when (random.nextInt(3)) { 0 -> comecoDificil(); 1 -> silabaQueFalta(V.palavrasComplexas, soDificil = true); else -> silabaQueFalta(V.palavrasGrandes) } },
        ModeloDeFase("Mestre das letras", "🌟") { when (random.nextInt(6)) { 0 -> lerFrase(true); 1 -> comoSeEscreve(); 2 -> montarGrande(4); 3 -> quantasLetras(); 4 -> ordemDasPalavras(); else -> tracarMinuscula(minusculasTodas) } },
    )

    // ================================================================== MUNDOS 6 A 25 (fases 51 a 250)

    private val L = VocabularioLetras

    /** Uma fase nova: a atividade [principal] sai em 6 de cada 10 perguntas, as [outras] dividem o resto. */
    private fun nova(titulo: String, emoji: String, principal: () -> Pergunta, vararg outras: () -> Pergunta) =
        ModeloDeFase(titulo, emoji) {
            if (outras.isEmpty() || random.nextInt(10) < 6) principal() else outras[random.nextInt(outras.size)]()
        }

    /** Uma pergunta de uma das fases 1 a 9 do mundo [mundo] (a revisão do mundo anterior, na fase 8). */
    private fun revisao(mundo: Int): Pergunta = fase((mundo - 1) * 10 + 1 + random.nextInt(9)).criar()

    private fun fpal(p: Palavra) = p.texto.lowercase()
    private fun falaS(s: String) = L.falaDaSilaba(s)
    private fun opFig(p: Palavra) = Opcao(Visual.Emoji(p.emoji), listOf(pt(fpal(p))))
    private fun opPal(p: Palavra) = Opcao(Visual.Texto(p.texto), listOf(pt(fpal(p))))
    private fun opSil(s: String) = Opcao(Visual.Texto(s), listOf(pt(falaS(s))))
    private fun opTx(t: String, fala: String = t.lowercase()) = Opcao(Visual.Texto(t), listOf(pt(fala)))
    private fun opEmo(e: String, fala: String) = Opcao(Visual.Emoji(e), listOf(pt(fala)))
    private fun pedacos(p: Palavra) = p.silabas.joinToString(", ") { falaS(it) }
    private fun soletrar(t: String) = t.map { L.falaDaLetra(it.toString()) }.joinToString(", ")

    /** Junta listas sem figura nem palavra repetida. */
    private fun juntar(vararg listas: List<Palavra>): List<Palavra> =
        listas.toList().flatten().distinctBy { it.emoji }.distinctBy { it.texto }

    /** Três erradas distintas entre si e diferentes da certa (comparando por [chave]). */
    private fun <T> tres(certa: T, candidatos: List<T>, chave: (T) -> Any? = { it }): List<T> {
        val r = mutableListOf<T>()
        val vistos = mutableSetOf(chave(certa))
        for (c in candidatos) if (vistos.add(chave(c))) {
            r += c
            if (r.size == 3) break
        }
        require(r.size == 3) { "Faltam opções erradas para $certa" }
        return r
    }

    /** Ordem de dicionário: sem acento e sem cedilha. */
    private fun chaveDeOrdem(s: String) = s.uppercase().map {
        when (it) {
            'Á', 'Â', 'Ã', 'À' -> 'A'
            'É', 'Ê' -> 'E'
            'Í' -> 'I'
            'Ó', 'Ô', 'Õ' -> 'O'
            'Ú' -> 'U'
            'Ç' -> 'C'
            else -> it
        }
    }.joinToString("")

    private fun parecenca(a: Palavra, b: Palavra): Int = when {
        a.silabas.first() == b.silabas.first() -> 0
        a.inicial == b.inicial -> 1
        a.silabas.size == b.silabas.size -> 2
        else -> 3
    }

    // ------------------------------------------------------------------ escritas parecidas

    private val vogaisTodas = "AEIOUÁÉÍÓÚÂÊÔÃÕ"

    /** Trocas que a criança costuma fazer ao escrever (RR por R, Ç por SS, X por CH, sem o til...). */
    private val trocasDeGrafia = listOf(
        "RR" to "R", "SS" to "S", "SS" to "Ç", "Ç" to "SS", "Ç" to "S", "CH" to "X", "X" to "CH", "LH" to "LI", "LH" to "L",
        "NH" to "NI", "NH" to "N", "QU" to "C", "QU" to "Q", "GU" to "G", "ÃO" to "AM", "ÃO" to "AO", "ÃE" to "AE",
        "Ã" to "A", "ÕE" to "OE", "MP" to "NP", "MB" to "NB", "CE" to "SE", "CI" to "SI", "GE" to "JE", "GI" to "JI",
        "JE" to "GE", "JI" to "GI", "Z" to "S", "OU" to "O", "EI" to "E", "AI" to "A", "ÉU" to "ÉL", "EU" to "EL",
        "BL" to "BR", "PL" to "PR", "CL" to "CR", "FL" to "FR", "GL" to "GR", "BR" to "BL", "PR" to "PL", "CR" to "CL",
        "FR" to "FL", "GR" to "GL", "TR" to "TL", "DR" to "DL", "VR" to "VL",
        "Á" to "A", "É" to "E", "Í" to "I", "Ó" to "O", "Ú" to "U", "Â" to "A", "Ê" to "E", "Ô" to "O",
        "É" to "Ê", "Ê" to "É", "Ó" to "Ô", "Ô" to "Ó", "AM" to "ÃO",
        "AN" to "A", "AN" to "AM", "EN" to "E", "EN" to "EM", "IN" to "I", "IN" to "IM", "ON" to "O", "ON" to "OM", "UN" to "U", "UN" to "UM",
    )

    private val trocasPorRegra = listOf(
        Regex("(?<=[$vogaisTodas])S(?=[$vogaisTodas])") to "Z",
        Regex("(?<=[$vogaisTodas])S(?=[$vogaisTodas])") to "SS",
        Regex("(?<=[$vogaisTodas])R(?=[$vogaisTodas])") to "RR",
        Regex("L(?=[^${vogaisTodas}H]|$)") to "U",
        Regex("M$") to "N",
        Regex("N(?=[BCDFGJKLMPQRSTVXZÇ])") to "M",
        Regex("^H") to "",
        Regex("^(?=[AEIOUÁÉÍÓÚÂÊÔ])") to "H",
    )

    /** Escritas erradas e parecidas de [palavra]: primeiro as do ponto difícil dela, depois trocas comuns. */
    private fun grafiasErradas(palavra: String): List<String> {
        val r = mutableListOf<String>()
        for ((de, para) in trocasDeGrafia) if (palavra.contains(de)) r += palavra.replaceFirst(de, para)
        for ((re, para) in trocasPorRegra) re.find(palavra)?.let { r += palavra.replaceRange(it.range, para) }
        r.shuffle(random)
        val simples = listOf(palavra + palavra.last(), palavra.first() + palavra, palavra.dropLast(1))
        return (r + escritasErradas(palavra) + simples).distinct()
            .filter { it != palavra && it.length >= 2 && it !in L.palavrasQueExistem }
    }

    private val bancoDeSilabas: List<String> by lazy {
        (L.familias.values.flatten() + juntar(L.cv2, L.cv3, L.nasaisN, L.ditongos, L.travadasR, L.travadasS, L.travadasL, L.encontrosR).flatMap { it.silabas }).distinct()
    }

    /** Pedacinhos parecidos com [s] (QUEI: CEI, QEI; ÇA: SA, SSA; BRA: BLA, BA). */
    private fun silabasParecidasL(s: String, evitar: Collection<String> = emptyList()): List<String> {
        val regras = mutableListOf<String>()
        for ((de, para) in trocasDeGrafia) if (s.contains(de)) regras += s.replaceFirst(de, para)
        regras.shuffle(random)
        val vogais = if (s.last() in "AEIOU") "AEIOU".filter { it != s.last() }.map { s.dropLast(1) + it }.shuffled(random) else emptyList()
        val reserva = bancoDeSilabas.filter { it.length == s.length && it.first() == s.first() }.shuffled(random) +
            bancoDeSilabas.filter { it.length == s.length }.shuffled(random)
        return (regras + vogais + silabasParecidas(s, evitar) + reserva).distinct()
            .filter { it != s && it.isNotBlank() && it !in evitar && it != "CU" && it.any { c -> c in vogaisTodas } }
    }

    // ------------------------------------------------------------------ ler e escrever palavras

    /** Lê a palavra escrita e toca na figura. Com [parecidas], as figuras começam parecido. */
    private fun lerFigura(lista: List<Palavra>, parecidas: Boolean = false): Pergunta {
        val certa = lista.um()
        val outras = lista.shuffled(random).let { l -> if (parecidas) l.sortedBy { parecenca(certa, it) } else l }
        val erradas = tres(certa, outras.filter { it.texto != certa.texto }) { it.emoji }
        return montar(
            instrucao = "Que figura é esta palavra?",
            fala = listOf(pt("Leia a palavra com calma. Qual figura combina com ela?")),
            cena = Cena.Palavra(certa.texto),
            certa = opFig(certa),
            erradas = erradas.map(::opFig),
            reforco = listOf(pt("${pedacos(certa)}. Aqui está escrito ${fpal(certa)}!")),
        )
    }

    /** Vê a figura e toca na palavra escrita. */
    private fun palavraDaFigura(lista: List<Palavra>, parecidas: Boolean = false): Pergunta {
        val certa = lista.um()
        val outras = lista.shuffled(random).let { l -> if (parecidas) l.sortedBy { parecenca(certa, it) } else l }
        val erradas = tres(certa, outras) { it.texto }
        return montar(
            instrucao = "Qual é a palavra?",
            fala = listOf(pt("Que figura é esta? Toque na palavra que diz o nome dela.")),
            cena = Cena.Emojis(certa.emoji),
            certa = opPal(certa),
            erradas = erradas.map(::opPal),
            reforco = listOf(pt("${fpal(certa)}! ${pedacos(certa)}.")),
        )
    }

    /** Qual está escrita certinho? Com [figura] a figura ajuda; sem ela, só a voz (ditado de escolher). */
    private fun comoSeEscreveL(lista: List<Palavra>, figura: Boolean): Pergunta {
        val p = lista.um()
        val erradas = tres(p.texto, grafiasErradas(p.texto))
        val palavra = fpal(p)
        return montar(
            instrucao = "Como se escreve?",
            fala = if (figura) listOf(pt("$palavra. Qual está escrita certinho?"))
            else listOf(pt("Escute com atenção: $palavra. $palavra. Qual está escrita certinho?")),
            cena = if (figura) Cena.Emojis(p.emoji) else Cena.Nenhuma,
            certa = opTx(p.texto, soletrar(p.texto)),
            erradas = erradas.map { opTx(it, soletrar(it)) },
            reforco = listOf(pt("$palavra se escreve assim: ${soletrar(p.texto)}.")),
        )
    }

    /** Que pedacinho falta (CA__O)? [foco] escolhe o pedacinho difícil da palavra. */
    private fun silabaFaltaL(lista: List<Palavra>, foco: (String) -> Boolean = { true }): Pergunta {
        val p = lista.filter { it.silabas.size >= 2 }.um()
        val focos = p.silabas.indices.filter { foco(p.silabas[it]) }
        val i = if (focos.isNotEmpty()) focos.um() else p.silabas.indices.random(random)
        val certa = p.silabas[i]
        val escrita = p.silabas.mapIndexed { j, s -> if (j == i) "__" else s }.joinToString("")
        val erradas = tres(certa, silabasParecidasL(certa, p.silabas))
        return montar(
            instrucao = "Que pedacinho falta?",
            fala = listOf(pt("${fpal(p)}. Que pedacinho está faltando em ${fpal(p)}?")),
            cena = Cena.Palavra(p.emoji + " " + escrita),
            certa = opSil(certa),
            erradas = erradas.map(::opSil),
            reforco = listOf(pt("${pedacos(p)}. ${fpal(p)}!")),
        )
    }

    private fun quantosPedacinhos(lista: List<Palavra>): Pergunta {
        val p = lista.um()
        val n = p.silabas.size
        return montar(
            instrucao = "Quantos pedacinhos?",
            fala = listOf(pt("Bata palmas comigo: ${pedacos(p)}. Quantos pedacinhos tem ${fpal(p)}?")),
            cena = Cena.Palavra(p.emoji + " " + p.texto),
            certa = opcaoNumero(n),
            erradas = (1..6).filter { it != n }.ordenarUmaVez { kotlin.math.abs(it - n) + random.nextDouble() * 2 }.take(3).map(::opcaoNumero),
            reforco = listOf(pt("${pedacos(p)}. ${porExtenso(n)} pedacinhos!")),
        )
    }

    private fun montarPalavraL(lista: List<Palavra>): Pergunta {
        val p = lista.filter { it.silabas.size in 2..5 }.um()
        val extras = p.silabas.shuffled(random).flatMap { silabasParecidasL(it, p.silabas).take(2) }.distinct().take(2)
        return Pergunta.deAtividade(
            instrucao = "Monte: ${p.texto}",
            fala = listOf(pt("Monte a palavra ${fpal(p)}. Arraste os pedacinhos na ordem: ${pedacos(p)}.")),
            atividade = Atividade.MontarPalavra(p.emoji, p.texto, p.silabas, extras),
            reforco = listOf(pt("${pedacos(p)}. ${fpal(p)}!")),
        )
    }

    /** Ditado com pedacinhos: escuta a palavra e monta. Sem [figura], só pela voz. */
    private fun ditadoSilabas(lista: List<Palavra>, figura: Boolean): Pergunta {
        val p = lista.filter { it.silabas.size in 2..5 }.um()
        val extras = p.silabas.shuffled(random).flatMap { silabasParecidasL(it, p.silabas).take(2) }.distinct().take(2 + random.nextInt(2))
        val palavra = fpal(p)
        return Pergunta.deAtividade(
            instrucao = "Ditado: escute e monte",
            fala = listOf(pt("Ditado! Escute: $palavra. Monte a palavra $palavra com os pedacinhos.")),
            atividade = Ditado(p.silabas, extras, figura = if (figura) p.emoji else null),
            reforco = listOf(pt("${pedacos(p)}. $palavra!")),
        )
    }

    private val letrasParecidas = mapOf(
        'R' to "L", 'L' to "RU", 'S' to "ZÇ", 'Z' to "S", 'Ç' to "SC", 'C' to "ÇSQ", 'G' to "J", 'J' to "G", 'X' to "S",
        'M' to "N", 'N' to "M", 'E' to "I", 'O' to "U", 'U' to "OL", 'I' to "E", 'Á' to "AÂ", 'É' to "EÊ", 'Ê' to "EÉ",
        'Ó' to "OÔ", 'Ô' to "OÓ", 'Ã' to "AÂ", 'Õ' to "O", 'Í' to "I", 'Ú' to "U", 'Â' to "AÃ", 'Q' to "CK", 'B' to "DP",
        'D' to "BT", 'P' to "BQ", 'T' to "D", 'F' to "V", 'V' to "F", 'A' to "O", 'H' to "",
    )

    /** Ditado com letras: escuta a palavra e escreve letra por letra. */
    private fun ditadoLetras(lista: List<Palavra>, figura: Boolean): Pergunta {
        val p = lista.filter { it.texto.length in 2..8 }.um()
        val letras = p.texto.map { it.toString() }
        val parecidas = letras.flatMap { l -> letrasParecidas[l[0]].orEmpty().map { it.toString() } }.distinct().filter { it !in letras }.shuffled(random)
        val reserva = Vocabulario.consoantes.filter { it !in letras }.shuffled(random)
        val extras = (parecidas + reserva).distinct().take(2 + random.nextInt(2))
        val palavra = fpal(p)
        return Pergunta.deAtividade(
            instrucao = "Ditado: escreva com letras",
            fala = listOf(pt("Ditado! Escute com atenção: $palavra. Escreva $palavra com as letras, uma de cada vez.")),
            atividade = Ditado(letras, extras, figura = if (figura) p.emoji else null),
            reforco = listOf(pt("$palavra: ${soletrar(p.texto)}!")),
        )
    }

    /**
     * Escrever a palavra com o dedo, letra por letra, em letra de forma maiúscula (só palavras de 3 a 5
     * letras sem acento nem cedilha; sem nenhuma na [lista], as da fazenda). A instrução não entrega a palavra.
     */
    private fun escreverPalavra(lista: List<Palavra>, figura: Boolean = true): Pergunta {
        val boas = Escrita.palavrasParaEscrever(lista).ifEmpty { Escrita.palavrasParaEscrever(L.cv2) }
        val p = boas.umDoTema { it.emoji }
        val palavra = fpal(p)
        return Pergunta.deAtividade(
            instrucao = "Escreva a palavra com o dedo",
            fala = listOf(pt("Vamos escrever! Escreva $palavra com o dedo, uma letra de cada vez.")),
            atividade = EscreverPalavra(p.texto, if (figura) p.emoji else null),
            reforco = listOf(pt("$palavra: ${soletrar(p.texto)}!")),
        )
    }

    // ------------------------------------------------------------------ pescar, estourar, ligar, achar

    private fun pescar(instrucao: String, fala: String, certas: List<Opcao>, erradas: List<Opcao>, reforco: String, total: Int = 6): Pergunta {
        val c = certas.distinctBy { it.visual }.sorteia(2 + random.nextInt(2))
        val e = erradas.filter { x -> c.none { it.visual == x.visual } }.distinctBy { it.visual }.sorteia(total - c.size)
        val peixes = (c + e).shuffled(random)
        return Pergunta.deAtividade(instrucao, listOf(pt(fala)), Pescaria(peixes, peixes.indices.filter { peixes[it] in c }.toSet()), listOf(pt(reforco)))
    }

    private fun estourar(instrucao: String, fala: String, certas: List<Opcao>, erradas: List<Opcao>, reforco: String, total: Int = 6): Pergunta {
        val c = certas.distinctBy { it.visual }.sorteia(2 + random.nextInt(2))
        val e = erradas.filter { x -> c.none { it.visual == x.visual } }.distinctBy { it.visual }.sorteia(total - c.size)
        val baloes = (c + e).shuffled(random)
        return Pergunta.deAtividade(instrucao, listOf(pt(fala)), EstourarBaloes(baloes, baloes.indices.filter { baloes[it] in c }), listOf(pt(reforco)))
    }

    /** Posições espalhadas pela cena, sem uma figura em cima da outra. */
    private fun posicoes(n: Int): List<Pair<Float, Float>> =
        (0 until 16).shuffled(random).take(n).map { c ->
            val x = (0.13f + (c % 4) * 0.245f + (random.nextFloat() - 0.5f) * 0.07f).coerceIn(0.08f, 0.92f)
            val y = (0.14f + (c / 4) * 0.235f + (random.nextFloat() - 0.5f) * 0.07f).coerceIn(0.08f, 0.92f)
            x to y
        }

    private fun cacar(mundo: Int, instrucao: String, fala: String, certas: List<Opcao>, erradas: List<Opcao>, reforco: String, todasAsCertas: Boolean = false): Pergunta {
        val c = certas.distinctBy { it.visual }.let { if (todasAsCertas) it else it.sorteia(2 + random.nextInt(2)) }
        val e = erradas.filter { x -> c.none { it.visual == x.visual } }.distinctBy { it.visual }.sorteia(5 + random.nextInt(3))
        val todas = (c + e).shuffled(random)
        val pos = posicoes(todas.size)
        val itens = todas.mapIndexed { i, o -> ItemNaCena(o, pos[i].first, pos[i].second, 0.8f + random.nextFloat() * 0.4f) }
        return Pergunta.deAtividade(instrucao, listOf(pt(fala)), CacaNaCena(mundo - 1, itens, todas.indices.filter { todas[it] in c }.toSet()), listOf(pt(reforco)))
    }

    /** Ligar a palavra escrita à figura (uma figura sobra). */
    private fun ligarFiguras(lista: List<Palavra>, n: Int = 3 + random.nextInt(2)): Pergunta {
        val ps = lista.distinctBy { it.emoji }.distinctBy { it.texto }.sorteia(n + 1)
        val esq = ps.take(n)
        val dir = ps.shuffled(random)
        return Pergunta.deAtividade(
            instrucao = "Ligue a palavra à figura",
            fala = listOf(pt("Leia cada palavra e ligue com a figura certa. Uma figura vai sobrar.")),
            atividade = LigarPares(esq.map(::opPal), dir.map(::opFig), esq.indices.associateWith { dir.indexOf(esq[it]) }),
            reforco = listOf(pt(esq.joinToString(", ") { fpal(it) } + "!")),
        )
    }

    /** Ligar textos em pares (antônimos, sinônimos, rimas, pergunta e resposta), com uma sobra à direita. */
    private fun ligarTextos(pares: List<Pair<String, String>>, instrucao: String, fala: String, n: Int = 3, sobra: String? = null): Pergunta {
        val ps = pares.distinctBy { it.first }.distinctBy { it.second }.sorteia(n + (if (sobra == null) 1 else 0))
        val esq = ps.take(n)
        val direita = (esq.map { it.second } + (sobra ?: ps.last().second)).distinct().shuffled(random)
        return Pergunta.deAtividade(
            instrucao = instrucao,
            fala = listOf(pt(fala)),
            atividade = LigarPares(esq.map { opTx(it.first, it.first.lowercase()) }, direita.map { opTx(it, it.lowercase()) }, esq.indices.associateWith { direita.indexOf(esq[it].second) }),
            reforco = listOf(pt(esq.joinToString(". ") { "${it.first.lowercase()} e ${it.second.lowercase()}" } + "!")),
        )
    }

    /** Dominó da figura com a palavra: a ponta é uma figura e cada peça tem uma palavra e a próxima figura. */
    private fun dominoFiguras(lista: List<Palavra>): Pergunta {
        val jogadas = 2 + random.nextInt(2)
        val ps = lista.distinctBy { it.emoji }.distinctBy { it.texto }.sorteia(jogadas + 3)
        val cadeia = ps.take(jogadas + 1)
        val fora = ps.drop(jogadas + 1)
        val fig = { p: Palavra -> MeiaPeca(p.texto, Visual.Emoji(p.emoji), listOf(pt(fpal(p)))) }
        val pal = { p: Palavra -> MeiaPeca(p.texto, Visual.Texto(p.texto), listOf(pt(fpal(p)))) }
        val pecas = (0 until jogadas).map { PecaDomino(pal(cadeia[it]), fig(cadeia[it + 1])) } + PecaDomino(pal(fora[0]), fig(fora[1]))
        val mao = pecas.map { if (random.nextBoolean()) it else PecaDomino(it.b, it.a) }.shuffled(random)
        return Pergunta.deAtividade(
            instrucao = "Dominó: figura e palavra",
            fala = listOf(pt("Dominó! Encaixe a peça com a palavra que combina com a figura da ponta.")),
            atividade = Domino(fig(cadeia[0]), mao, jogadas),
            reforco = listOf(pt(cadeia.joinToString(", ") { fpal(it) } + "!")),
        )
    }

    /** Dominó de pares de palavras (singular e plural, masculino e feminino, contrários). */
    private fun dominoTextos(pares: List<Pair<String, String>>, instrucao: String, fala: String): Pergunta {
        val jogadas = 2 + random.nextInt(2)
        val ps = pares.distinctBy { it.first }.distinctBy { it.second }.sorteia(jogadas + 3)
        val cadeia = ps.take(jogadas + 1)
        val fora = ps.drop(jogadas + 1)
        val meia = { chave: String, t: String -> MeiaPeca(chave, Visual.Texto(t), listOf(pt(t.lowercase()))) }
        val pecas = (0 until jogadas).map { PecaDomino(meia(cadeia[it].first, cadeia[it].second), meia(cadeia[it + 1].first, cadeia[it + 1].first)) } +
            PecaDomino(meia(fora[0].first, fora[0].second), meia(fora[1].first, fora[1].first))
        val mao = pecas.map { if (random.nextBoolean()) it else PecaDomino(it.b, it.a) }.shuffled(random)
        return Pergunta.deAtividade(
            instrucao = instrucao,
            fala = listOf(pt(fala)),
            atividade = Domino(meia(cadeia[0].first, cadeia[0].first), mao, jogadas),
            reforco = listOf(pt(cadeia.joinToString(". ") { "${it.first.lowercase()}, ${it.second.lowercase()}" } + "!")),
        )
    }

    /** Quebra cabeça da figura com os pedacinhos da palavra nas peças. */
    private fun quebraSilabas(lista: List<Palavra>): Pergunta {
        val p = lista.filter { it.silabas.size in 2..4 && it.silabas.distinct().size == it.silabas.size }.um()
        val n = p.silabas.size
        return Pergunta.deAtividade(
            instrucao = "Monte o quebra cabeça: ${p.texto}",
            fala = listOf(pt("Quebra cabeça! Monte ${fpal(p)} com os pedacinhos na ordem: ${pedacos(p)}.")),
            atividade = QuebraCabeca(p.emoji, 1, n, rotulos = p.silabas, fixas = if (n == 4 && random.nextBoolean()) setOf(0) else emptySet()),
            reforco = listOf(pt("${pedacos(p)}. ${fpal(p)}!")),
        )
    }

    private fun frasesBoas(frases: List<VocabularioLetras.FraseL>, tamanhos: IntRange) =
        frases.filter { it.palavras.size in tamanhos && it.palavras.distinct().size == it.palavras.size }

    /** Quebra cabeça com as palavras de uma frase curta nas peças. */
    private fun quebraFrase(frases: List<VocabularioLetras.FraseL>): Pergunta {
        val f = frasesBoas(frases, 2..4).um()
        return Pergunta.deAtividade(
            instrucao = "Monte a frase no quebra cabeça",
            fala = listOf(pt("Quebra cabeça de frase! Escute: ${f.texto} Monte as palavras na ordem.")),
            atividade = QuebraCabeca(f.emoji, 1, f.palavras.size, rotulos = f.palavras),
            reforco = listOf(pt(f.texto)),
        )
    }

    /** Montar a frase com os cartões, com [extras] cartões a mais. */
    private fun montarFraseL(frases: List<VocabularioLetras.FraseL>, extras: Int = 0, tamanhos: IntRange = 2..8): Pergunta {
        val f = frasesBoas(frases, tamanhos).um()
        val sobras = frases.flatMap { it.palavras }.distinct().filter { it !in f.palavras }.sorteia(extras)
        return Pergunta.deAtividade(
            instrucao = "Monte a frase",
            fala = listOf(pt("Escute: ${f.texto} Monte a frase na ordem certa. Começa com letra maiúscula e termina com o sinal.")),
            atividade = MontarFrase(f.palavras, sobras, figura = f.emoji),
            reforco = listOf(pt(f.texto)),
        )
    }

    // ------------------------------------------------------------------ mundo 6: famílias silábicas

    private fun tocarSilabaFamilia(): Pergunta {
        val c = L.familias.keys.toList().um()
        val fam = L.familias.getValue(c)
        val certa = fam.um()
        val mesmaVogal = L.familias.filterKeys { it != c }.values.mapNotNull { f -> f.firstOrNull { it.last() == certa.last() } }.shuffled(random)
        val erradas = tres(certa, fam.shuffled(random) + mesmaVogal)
        return montar(
            instrucao = "Toque no pedacinho que você ouviu",
            fala = listOf(pt("A família do ${L.falaDaLetra(c)}: ${fam.joinToString(", ") { falaS(it) }}. Toque no ${falaS(certa)}.")),
            cena = Cena.Nenhuma,
            certa = opSil(certa),
            erradas = erradas.map(::opSil),
            reforco = listOf(pt("${falaS(certa)}! ${L.falaDaLetra(c)} com ${L.falaDaLetra(certa.last().toString())}: ${falaS(certa)}.")),
        )
    }

    private fun pescariaFamilia(): Pergunta {
        val c = L.familias.keys.toList().um()
        val fam = L.familias.getValue(c)
        val outros = L.familias.filterKeys { it != c }.values.flatten().map(::opSil)
        return pescar(
            instrucao = "Pesque a família do $c",
            fala = "Pescaria! Pesque os peixinhos da família do ${L.falaDaLetra(c)}: ${fam.joinToString(", ") { falaS(it) }}.",
            certas = fam.map(::opSil),
            erradas = outros,
            reforco = "A família do ${L.falaDaLetra(c)}: ${fam.joinToString(", ") { falaS(it) }}!",
        )
    }

    /** Troca uma letra e a palavra vira outra (GATO, PATO): consciência fonêmica. */
    private fun trocaLetra(): Pergunta {
        val (base, alvo) = L.trocas.um().sorteia(2)
        val i = base.texto.indices.first { base.texto[it] != alvo.texto[it] }
        val de = base.texto[i].toString()
        val para = alvo.texto[i].toString()
        val outras = L.cv2.filter { o -> L.trocas.none { g -> g.any { it.texto == o.texto } && g.any { it.texto == base.texto } } }
        val erradas = tres(alvo, listOf(base) + outras.shuffled(random)) { it.emoji }
        return montar(
            instrucao = "Troque o $de pelo $para",
            fala = listOf(pt("${fpal(base)}. Troque o ${L.falaDaLetra(de)} pelo ${L.falaDaLetra(para)}. Que palavra fica?")),
            cena = Cena.Palavra(base.texto),
            certa = opFig(alvo),
            erradas = erradas.map(::opFig),
            reforco = listOf(pt("${fpal(base)} virou ${fpal(alvo)}!")),
        )
    }

    // ------------------------------------------------------------------ sons e regras de escrita (mundos 7 a 14)

    private fun temSom(certas: List<Palavra>, erradas: List<Palavra>, instrucao: String, fala: String): Pergunta {
        val c = certas.um()
        val e = tres(c, erradas.shuffled(random)) { it.emoji }
        return montar(instrucao, listOf(pt(fala)), Cena.Nenhuma, opFig(c), e.map(::opFig), listOf(pt("${fpal(c)}! ${pedacos(c)}.")))
    }

    private val comAo get() = L.nasaisAo.filter { "ÃO" in it.texto }
    private val nasais get() = juntar(L.nasaisAo, L.nasaisN)

    private fun baloesAo(): Pergunta = estourar(
        instrucao = "Estoure as palavras com ÃO",
        fala = "Estoure os balões com as palavras que têm ão, com o til, como em mão.",
        certas = comAo.map(::opPal),
        erradas = L.cv2.map(::opPal),
        reforco = "Ão, com til!",
    )

    private fun baloesOrdem(lista: List<Palavra>): Pergunta {
        val p = lista.filter { it.silabas.size in 3..4 && it.silabas.distinct().size == it.silabas.size }.um()
        val extras = p.silabas.shuffled(random).flatMap { silabasParecidasL(it, p.silabas).take(1) }.distinct().take(2)
        val baloes = (p.silabas + extras).shuffled(random)
        return Pergunta.deAtividade(
            instrucao = "Estoure na ordem: ${p.emoji}",
            fala = listOf(pt("Estoure os balões na ordem para formar ${fpal(p)}: ${pedacos(p)}.")),
            atividade = EstourarBaloes(baloes.map(::opSil), p.silabas.map { baloes.indexOf(it) }, emOrdem = true),
            reforco = listOf(pt("${pedacos(p)}. ${fpal(p)}!")),
        )
    }

    private val sonsDosDitongos = listOf("AI" to "ai", "EI" to "ei", "OI" to "ói", "OU" to "ou", "EU" to "eu")

    private fun cacaDitongo(mundo: Int): Pergunta {
        val opcoes = sonsDosDitongos.filter { (d, _) -> L.ditongos.count { d in it.texto.replace("É", "E") } >= 2 }
        val (d, som) = opcoes.um()
        val certas = L.ditongos.filter { d in it.texto.replace("É", "E") }
        return cacar(mundo, "Ache as figuras com $d", "Caça na selva! Ache as figuras que têm o som $som, como em ${fpal(certas.first())}.",
            certas.map(::opFig), juntar(L.cv2, L.cv3).map(::opFig), "O som $som!")
    }

    private val trav get() = juntar(L.travadasR, L.travadasS, L.travadasL)
    private fun travada(s: String) = s.length >= 2 && s.last() in "RSL" && s[s.length - 2] in vogaisTodas

    private fun pescariaTravada(): Pergunta {
        val (letra, lista) = listOf("R" to L.travadasR, "S" to L.travadasS, "L" to L.travadasL).um()
        return pescar(
            instrucao = "Pesque as palavras com $letra no fim do pedacinho",
            fala = "Pescaria no gelo! Pesque as palavras que têm um pedacinho terminado em ${L.falaDaLetra(letra)}, como ${fpal(lista.first())}.",
            certas = lista.map(::opPal),
            erradas = juntar(L.cv2, L.cv3).map(::opPal),
            reforco = "Pedacinhos com ${L.falaDaLetra(letra)} no fim!",
        )
    }

    private val rrss get() = juntar(L.rr, L.ss)
    private val qugu get() = juntar(L.qu, L.gu)
    private val circo get() = juntar(L.rr, L.ss, L.qu, L.gu)

    private fun baloesDigrafo(): Pergunta {
        val (d, fala) = listOf("RR" to "érre, érre", "SS" to "ésse, ésse", "QU" to "quê, u", "GU" to "gê, u").um()
        return estourar(
            instrucao = "Estoure as palavras com $d",
            fala = "Estoure os balões com as palavras que têm $fala juntinhos.",
            certas = circo.filter { d in it.texto }.map(::opPal),
            erradas = juntar(L.cv2, L.cv3, L.rFraco).filter { d !in it.texto }.map(::opPal),
            reforco = "Palavras com $fala!",
        )
    }

    private val cc get() = juntar(L.cedilha, L.cSuave)
    private val gj get() = juntar(L.gSuave, L.jota)
    private val sz get() = juntar(L.sComSomDeZ, L.zeta)

    private fun pescariaCedilha(): Pergunta = pescar(
        instrucao = "Pesque as palavras com Ç",
        fala = "Pescaria no oásis! Pesque as palavras com cê cedilha, como em maçã.",
        certas = L.cedilha.map(::opPal),
        erradas = L.cSuave.map(::opPal),
        reforco = "Cê cedilha, com o rabinho!",
    )

    private val sEntreVogais = Regex("[$vogaisTodas]S[$vogaisTodas]")

    private fun pescariaSZ(): Pergunta = pescar(
        instrucao = "Pesque o S com som de Z",
        fala = "Pesque as palavras em que o ésse tem som de zê, como em casa.",
        certas = L.sComSomDeZ.map(::opPal),
        erradas = juntar(L.cv2, L.cv3, L.travadasL).filter { it.texto.startsWith("S") && !sEntreVogais.containsMatchIn(it.texto) }.map(::opPal),
        reforco = "Ésse entre vogais tem som de zê!",
    )

    private val enc get() = juntar(L.encontrosL, L.encontrosR)
    private fun temEncontro(s: String) = s.length >= 3 && s[0] !in vogaisTodas && s[1] in "LR"

    /** Encontros (BR, FL...) que aparecem em pelo menos duas palavras. */
    private val gruposDeEncontro: Map<String, List<Palavra>> by lazy {
        enc.flatMap { p -> p.silabas.filter(::temEncontro).map { it.take(2) to p } }
            .groupBy({ it.first }, { it.second }).mapValues { it.value.distinct() }.filter { it.value.size >= 2 }
    }

    private fun cacaEncontro(mundo: Int): Pergunta {
        val (grupo, palavras) = gruposDeEncontro.entries.toList().um().toPair()
        val nome = "${L.falaDaLetra(grupo.take(1))} com ${L.falaDaLetra(grupo.drop(1))}"
        return cacar(mundo, "Ache as figuras com $grupo", "Caça na cidade! Ache as figuras que têm $nome juntinhos, como em ${fpal(palavras.first())}.",
            palavras.map(::opFig), juntar(L.cv2, L.cv3).map(::opFig), "$grupo, $nome!")
    }

    private val xch get() = juntar(L.xis, L.che)

    private fun baloesXCH(): Pergunta {
        val x = random.nextBoolean()
        return estourar(
            instrucao = if (x) "Estoure as palavras com X" else "Estoure as palavras com CH",
            fala = if (x) "Estoure os balões com as palavras escritas com xis, como em caixa." else "Estoure os balões com as palavras escritas com cê agá, como em chave.",
            certas = (if (x) L.xis else L.che).map(::opPal),
            erradas = (if (x) L.che else L.xis).map(::opPal),
            reforco = if (x) "Com xis!" else "Com cê agá!",
        )
    }

    private fun comecaComH(): Pergunta {
        val c = L.aga.um()
        val e = tres(c, L.semAga.shuffled(random)) { it.texto }
        return montar(
            instrucao = "Qual começa com H?",
            fala = listOf(pt("Qual destas palavras começa com a letra agá? O agá do começo não tem som.")),
            cena = Cena.Nenhuma,
            certa = opPal(c),
            erradas = e.map(::opPal),
            reforco = listOf(pt("${fpal(c)} começa com agá!")),
        )
    }

    private val mn get() = juntar(L.mpmb, L.nAntes, L.mFim)

    private fun pescariaMN(): Pergunta = pescar(
        instrucao = "Pesque o M antes de P e B",
        fala = "Pescaria dos dinossauros! Pesque as palavras com ême antes de pê ou bê, como em tambor.",
        certas = L.mpmb.map(::opPal),
        erradas = L.nAntes.map(::opPal),
        reforco = "Antes de pê e bê, só ême!",
    )

    // ------------------------------------------------------------------ frases (mundos 15 e 16)

    /** Lê a frase e toca na figura. Frases compridas aparecem no balão. */
    private fun fraseFigura(frases: List<VocabularioLetras.FraseL>): Pergunta {
        val f = frases.um()
        val erradas = tres(f, frases.shuffled(random)) { it.emoji }
        val curta = f.texto.length <= 22
        return montar(
            instrucao = if (curta) "Leia a frase" else f.texto,
            fala = listOf(pt("Leia a frase com calma. Qual figura combina com ela?")),
            cena = if (curta) Cena.Palavra(f.texto) else Cena.Nenhuma,
            certa = opEmo(f.emoji, f.texto),
            erradas = erradas.map { opEmo(it.emoji, it.texto) },
            reforco = listOf(pt("Está escrito: ${f.texto}")),
        )
    }

    private fun ehNome(w: String) = w.trimEnd('.', '!', '?', ',') in L.nomes

    private val artigos = setOf("O", "A", "Um", "Uma", "Os", "As")

    /**
     * Qual frase está na ordem certa? Só frases de 4 palavras que começam com artigo (O papagaio é verde);
     * as erradas sempre ficam sem sentido: o artigo no fim (de trás para a frente, ou a primeira palavra
     * levada para o fim) ou depois do nome (papagaio o é verde).
     */
    private fun ordemCerta(frases: List<VocabularioLetras.FraseL>): Pergunta {
        val f = frasesBoas(frases, 4..4).filter { it.palavras.first() in artigos }.um()
        val ws = f.semSinal.split(" ").mapIndexed { i, w -> if (i == 0) w.lowercase() else w }
        val certa = ws.joinToString(" ")
        val outras = listOf(ws.reversed(), ws.drop(1) + ws.first(), listOf(ws[1], ws[0]) + ws.drop(2)).map { it.joinToString(" ") }
        return montar(
            instrucao = "Qual frase está na ordem certa?",
            fala = listOf(pt("Leia as frases. Qual delas está com as palavras na ordem certa?")),
            cena = Cena.Emojis(f.emoji),
            certa = opTx(certa, f.texto),
            erradas = outras.map { opTx(it, it) },
            reforco = listOf(pt(f.texto)),
        )
    }

    /** Qual frase está escrita certinho (maiúscula no começo e nos nomes, sinal no fim)? */
    private fun fraseCertinha(frases: List<VocabularioLetras.FraseL>): Pergunta {
        val f = frases.filter { it.texto.length <= 30 }.um()
        val ws = f.palavras
        val r = mutableListOf<String>()
        r += f.texto.replaceFirstChar { it.lowercase() }
        r += f.semSinal
        ws.indices.firstOrNull { it > 0 && ehNome(ws[it]) }?.let { i -> r += ws.mapIndexed { j, w -> if (j == i) w.lowercase() else w }.joinToString(" ") }
        ws.indices.filter { it > 0 && !ehNome(ws[it]) && ws[it][0].isLowerCase() }.shuffled(random).firstOrNull()?.let { i ->
            r += ws.mapIndexed { j, w -> if (j == i) w.replaceFirstChar { c -> c.uppercase() } else w }.joinToString(" ")
        }
        r += f.semSinal.replaceFirstChar { it.lowercase() } + (if (f.sinal == '.') "?" else ".")
        val erradas = tres(f.texto, r.shuffled(random))
        return montar(
            instrucao = "Qual frase está escrita certinho?",
            fala = listOf(pt("Qual frase está escrita certinho? Olhe a letra maiúscula no começo e nos nomes, e o sinal no fim.")),
            cena = Cena.Emojis(f.emoji),
            certa = opTx(f.texto, f.texto),
            erradas = erradas.map { opTx(it, it) },
            reforco = listOf(pt("Maiúscula no começo e o sinal no fim: ${f.texto}")),
        )
    }

    private fun cacaFrase(mundo: Int, itens: List<Pair<String, String>>, quem: String?): Pergunta {
        val escolhidos = itens.sorteia(2 + random.nextInt(2))
        val lista = escolhidos.map { it.second }.let { l -> l.dropLast(1).joinToString(", ") + " e " + l.last() }
        val frase = if (quem == null) "Ache $lista." else "$quem achou $lista. Ache o que $quem achou."
        val op = { it: Pair<String, String> -> opEmo(it.first, it.second.substringAfter(" ")) }
        return cacar(mundo, frase, "Leia: $frase", escolhidos.map(op), itens.filter { it !in escolhidos }.map(op), "Achou tudo!", todasAsCertas = true)
    }

    private val sinais = listOf("." to "ponto final", "?" to "ponto de interrogação", "!" to "ponto de exclamação", "," to "vírgula")

    private fun pontuacao(frases: List<VocabularioLetras.FraseL>, narrar: Boolean): Pergunta {
        val f = frases.filter { it.sinal in ".?!" }.um()
        val certa = sinais.first { it.first == f.sinal.toString() }
        return montar(
            instrucao = "${f.semSinal} __",
            fala = if (narrar) listOf(pt(f.texto), pt("Que sinal termina esta frase?")) else listOf(pt("Leia a frase. Que sinal termina esta frase?")),
            cena = Cena.Emojis(f.emoji),
            certa = opTx(certa.first, certa.second),
            erradas = sinais.filter { it != certa }.map { opTx(it.first, it.second) },
            reforco = listOf(pt("${certa.second}! ${f.texto}")),
        )
    }

    private fun ligarSinais(): Pergunta {
        val curtas = L.frasesDoces.filter { it.semSinal.length <= 22 }
        val escolhidas = listOf('.', '?', '!').map { s -> curtas.filter { it.sinal == s }.um() }
        val direita = (escolhidas.map { it.sinal.toString() } + ",").shuffled(random)
        return Pergunta.deAtividade(
            instrucao = "Ligue a frase ao sinal",
            fala = listOf(pt("Ligue cada frase ao sinal que termina ela: ponto final, ponto de interrogação ou ponto de exclamação.")),
            atividade = LigarPares(
                escolhidas.map { opTx(it.semSinal, it.texto) },
                direita.map { s -> opTx(s, sinais.first { it.first == s }.second) },
                escolhidas.indices.associateWith { direita.indexOf(escolhidas[it].sinal.toString()) },
            ),
            reforco = listOf(pt("Muito bem!")),
        )
    }

    /** Pergunta de compreensão sobre um texto curto (o texto aparece no balão; com [narrar], a voz lê). */
    private fun textoPergunta(textos: List<VocabularioLetras.TextoCurto>, narrar: Boolean): Pergunta {
        val t = textos.um()
        val q = t.perguntas.um()
        return montar(
            instrucao = "${t.texto} ${q.pergunta}",
            fala = if (narrar) listOf(pt(t.texto), pt(q.pergunta)) else listOf(pt("Leia o texto e responda: ${q.pergunta}")),
            cena = Cena.Emojis(t.emoji),
            certa = opTx(q.certa, q.certa),
            erradas = q.erradas.map { opTx(it, it) },
            reforco = listOf(pt("Isso! ${q.certa}.")),
        )
    }

    private fun baloesLista(): Pergunta {
        val (titulo, itens) = L.listasDeCompras.um()
        val op = { it: Pair<String, String> -> opEmo(it.first, it.second) }
        return estourar(
            instrucao = titulo,
            fala = "Leia a lista: $titulo Estoure os balões com o que está na lista.",
            certas = itens.map(op),
            erradas = L.foraDaLista.filter { f -> itens.none { it.first == f.first } }.map(op),
            reforco = "Tudo da lista!",
            total = 7,
        )
    }

    private fun ligarPerguntas(): Pergunta {
        val t = (L.historias + L.bilhetes).filter { it.perguntas.size >= 3 }.um()
        val qs = t.perguntas.sorteia(3)
        val certas = qs.map { it.certa }
        val sobra = qs.flatMap { it.erradas }.filter { it !in certas }.um()
        return ligarTextos(qs.map { it.pergunta to it.certa }, t.texto, "Leia o texto: ${t.texto} Ligue cada pergunta à resposta.", n = 3, sobra = sobra)
    }

    // ------------------------------------------------------------------ flexões e sentidos (mundos 17 e 18)

    private val emPares = { l: List<VocabularioLetras.Par> -> l.map { it.a to it.b } }

    private fun pluraisErrados(a: String, b: String): List<String> {
        val r = mutableListOf(a, a + "S", a + "ES", b + "S", b.dropLast(1))
        if (a.endsWith("ÃO")) r += listOf(a.dropLast(2) + "ÕES", a.dropLast(2) + "ÃES", a + "S")
        if (a.endsWith("L")) r += listOf(a.dropLast(1) + "IS", a + "ES")
        if (a.endsWith("M")) r += listOf(a.dropLast(1) + "NS", a + "S")
        return r.shuffled(random).distinct().filter { it != b }
    }

    private fun pluralEscolha(lista: List<VocabularioLetras.Par>): Pergunta {
        val p = lista.um()
        val erradas = tres(p.b, pluraisErrados(p.a, p.b))
        return montar(
            instrucao = "Plural de ${p.a}",
            fala = listOf(pt("${p.a.lowercase()}. Se forem muitos, como se escreve?")),
            cena = Cena.Palavra(p.a),
            certa = opTx(p.b),
            erradas = erradas.map { opTx(it) },
            reforco = listOf(pt("${p.a.lowercase()}, ${p.b.lowercase()}!")),
        )
    }

    private fun pescariaPlural(): Pergunta {
        val todos = L.pluraisFaceis + L.pluraisDificeis
        return pescar("Pesque as palavras no plural", "Pesque as palavras que falam de muitos, no plural.",
            todos.map { opTx(it.b) }, todos.map { opTx(it.a) }, "Plural: mais de um!")
    }

    private fun femininoEscolha(): Pergunta {
        val p = L.masculinoFeminino.um()
        val fem = random.nextBoolean()
        val (de, para) = if (fem) p.a to p.b else p.b to p.a
        val outras = L.masculinoFeminino.filter { it != p }.map { if (fem) it.b else it.a }.shuffled(random)
        return montar(
            instrucao = if (fem) "Feminino de ${de}" else "Masculino de ${de}",
            fala = listOf(pt(if (fem) "${de.lowercase()}. E se for menina ou fêmea, como fica?" else "${de.lowercase()}. E se for menino ou macho, como fica?")),
            cena = Cena.Palavra(de),
            certa = opTx(para),
            erradas = tres(para, outras).map { opTx(it) },
            reforco = listOf(pt("${p.a.lowercase()} e ${p.b.lowercase()}!")),
        )
    }

    private fun pescariaFeminino(): Pergunta = pescar("Pesque as palavras no feminino", "Pesque as palavras no feminino, que falam de menina ou de fêmea, como menina e leoa.",
        L.masculinoFeminino.map { opTx(it.b) }, L.masculinoFeminino.map { opTx(it.a) }, "Feminino!")

    private fun concordanciaEscolha(): Pergunta {
        val c = L.concordancias.um()
        val plural = random.nextBoolean()
        val sujeito = if (plural) c.sujeitoPlural else c.sujeito
        val certa = c.verbo[if (plural) 1 else 0]
        return montar(
            instrucao = "$sujeito ___ ${c.resto}",
            fala = listOf(pt("Leia: ${sujeito.lowercase()}, alguma coisa, ${c.resto} Qual palavra combina?")),
            cena = Cena.Nenhuma,
            certa = opTx(certa),
            erradas = c.verbo.filter { it != certa }.map { opTx(it) },
            reforco = listOf(pt("$sujeito $certa ${c.resto}")),
        )
    }

    private fun montarFraseConcordancia(): Pergunta {
        if (random.nextBoolean()) {
            val c = L.concordancias.um()
            val plural = random.nextBoolean()
            val palavras = (if (plural) c.sujeitoPlural else c.sujeito).split(" ") + c.verbo[if (plural) 1 else 0] + c.resto.split(" ")
            val extras = listOf(c.verbo[if (plural) 0 else 1], (if (plural) c.sujeito else c.sujeitoPlural).split(" ").first()).filter { it !in palavras }
            val frase = palavras.joinToString(" ")
            return Pergunta.deAtividade("Monte a frase", listOf(pt("Escute: $frase Monte a frase. Cuidado com as palavras que não combinam.")),
                MontarFrase(palavras, extras), listOf(pt(frase)))
        }
        val g = L.frasesGenero.um()
        val fem = random.nextBoolean()
        val palavras = (if (fem) g[1] else g[0]).split(" ") + g[2] + (if (fem) g[4] else g[3])
        val extras = listOf(if (fem) g[3] else g[4], (if (fem) g[0] else g[1]).split(" ").first()).filter { it !in palavras }.distinct()
        val frase = palavras.joinToString(" ")
        return Pergunta.deAtividade("Monte a frase", listOf(pt("Escute: $frase Monte a frase. Cuidado com as palavras que não combinam.")),
            MontarFrase(palavras, extras), listOf(pt(frase)))
    }

    private fun sentidoEscolha(lista: List<VocabularioLetras.Par>, contrario: Boolean, narrar: Boolean = true): Pergunta {
        val p = lista.um()
        val (de, para) = if (random.nextBoolean()) p.a to p.b else p.b to p.a
        val outras = lista.filter { it != p }.flatMap { listOf(it.a, it.b) }.shuffled(random)
        val pergunta = if (contrario) "Qual é o contrário de ${de.lowercase()}?" else "Qual palavra quer dizer o mesmo que ${de.lowercase()}?"
        return montar(
            instrucao = if (contrario) "Contrário de $de" else "O mesmo que $de",
            fala = listOf(pt(if (narrar) pergunta else "Leia e escolha. ${if (contrario) "O contrário" else "O mesmo sentido"}.")),
            cena = Cena.Palavra(de),
            certa = opTx(para),
            erradas = tres(para, outras).map { opTx(it) },
            reforco = listOf(pt(if (contrario) "${de.lowercase()} é o contrário de ${para.lowercase()}!" else "${de.lowercase()} e ${para.lowercase()} querem dizer o mesmo!")),
        )
    }

    private fun tamanhoEscolha(): Pergunta {
        val t = L.tamanhos.um()
        val pequeno = random.nextBoolean()
        val certa = if (pequeno) t.pequeno else t.grande
        val outras = listOf(if (pequeno) t.grande else t.pequeno, t.normal) + L.tamanhos.filter { it != t }.map { if (pequeno) it.pequeno else it.grande }.shuffled(random)
        return montar(
            instrucao = if (pequeno) "${t.normal} pequenininho" else "${t.normal} bem grande",
            fala = listOf(pt(if (pequeno) "${t.normal.lowercase()}. E se for bem pequenininho? É o diminutivo." else "${t.normal.lowercase()}. E se for bem grandão? É o aumentativo.")),
            cena = Cena.Palavra(t.normal),
            certa = opTx(certa),
            erradas = tres(certa, outras).map { opTx(it) },
            reforco = listOf(pt("${t.pequeno.lowercase()}, ${t.normal.lowercase()}, ${t.grande.lowercase()}!")),
        )
    }

    private fun baloesTamanho(): Pergunta {
        val pequeno = random.nextBoolean()
        return estourar(
            instrucao = if (pequeno) "Estoure o diminutivo" else "Estoure o aumentativo",
            fala = if (pequeno) "Estoure os balões com as palavras de coisa pequenininha, o diminutivo, como casinha." else "Estoure os balões com as palavras de coisa grandona, o aumentativo, como casarão.",
            certas = L.tamanhos.map { opTx(if (pequeno) it.pequeno else it.grande) },
            erradas = L.tamanhos.flatMap { listOf(opTx(it.normal), opTx(if (pequeno) it.grande else it.pequeno)) },
            reforco = if (pequeno) "Diminutivo!" else "Aumentativo!",
        )
    }

    private fun cartasTamanhos(): Pergunta {
        val t = L.tamanhos.um()
        val serie = listOf(t.pequeno, t.normal, t.grande)
        val extras = L.tamanhos.filter { it != t }.sorteia(random.nextInt(3)).map { it.pequeno }
        val cartas = (serie + extras).shuffled(random)
        return Pergunta.deAtividade(
            instrucao = "Do menor para o maior",
            fala = listOf(pt("Vire as cartas do menor para o maior: ${serie.joinToString(", ") { it.lowercase() }}.")),
            atividade = CartasViradas(cartas.map { opTx(it) }, serie.map { cartas.indexOf(it) }),
            reforco = listOf(pt(serie.joinToString(", ") { it.lowercase() } + "!")),
        )
    }

    // ------------------------------------------------------------------ dicionário (mundo 19)

    /** Palavras para pôr em ordem: [nivel] 1 com primeiras letras diferentes, 2 pela segunda letra, 3 pela terceira. */
    private fun palavrasParaOrdenar(nivel: Int, n: Int): List<Palavra> = when (nivel) {
        1 -> L.dicionario.sorteia(n).map { it.um() }
        2 -> L.dicionario.um().sorteia(n)
        else -> L.dicionarioTerceira.um().let { it.sorteia(minOf(n, it.size)) }
    }

    private fun dicaDeOrdem(nivel: Int) = when (nivel) {
        1 -> "Olhe a primeira letra."
        2 -> "A primeira letra é igual: olhe a segunda."
        else -> "As duas primeiras letras são iguais: olhe a terceira."
    }

    private fun primeiroNoDicionario(nivel: Int): Pergunta {
        val ws = palavrasParaOrdenar(nivel, 4)
        val certa = ws.minBy { chaveDeOrdem(it.texto) }
        return montar(
            instrucao = "Qual vem primeiro no dicionário?",
            fala = listOf(pt("Qual destas palavras vem primeiro no dicionário? ${dicaDeOrdem(nivel)}")),
            cena = Cena.Nenhuma,
            certa = opPal(certa),
            erradas = ws.filter { it != certa }.map(::opPal),
            reforco = listOf(pt("${fpal(certa)} vem primeiro!")),
        )
    }

    private fun tremAlfabetico(nivel: Int): Pergunta {
        val ws = palavrasParaOrdenar(nivel, if (nivel == 3) 4 else 3 + random.nextInt(2)).sortedBy { chaveDeOrdem(it.texto) }
        return Pergunta.deAtividade(
            instrucao = "Trem do alfabeto",
            fala = listOf(pt("Ponha os vagões em ordem alfabética, como no dicionário. ${dicaDeOrdem(nivel)}")),
            atividade = Trem(ws.map(::opPal), fixos = if (nivel == 1 && random.nextBoolean()) setOf(0) else emptySet()),
            reforco = listOf(pt(ws.joinToString(", ") { fpal(it) } + "!")),
        )
    }

    private fun cartasAlfabeticas(nivel: Int): Pergunta {
        val ws = palavrasParaOrdenar(nivel, 4)
        val ordem = ws.sortedBy { chaveDeOrdem(it.texto) }
        return Pergunta.deAtividade(
            instrucao = "Cartas em ordem alfabética",
            fala = listOf(pt("Vire as cartas na ordem do dicionário. ${dicaDeOrdem(nivel)}")),
            atividade = CartasViradas(ws.map(::opPal), ordem.map { ws.indexOf(it) }),
            reforco = listOf(pt(ordem.joinToString(", ") { fpal(it) } + "!")),
        )
    }

    private fun entreNoDicionario(): Pergunta {
        val g = L.dicionario.filter { it.size >= 3 }.um().sortedBy { chaveDeOrdem(it.texto) }
        val i = random.nextInt(g.size - 2)
        val (a, certa, b) = Triple(g[i], g[i + 1], g[i + 2])
        val outras = L.dicionario.filter { it != g && it.none { w -> w.inicial == a.inicial } }.flatten().shuffled(random)
        return montar(
            instrucao = "Entre ${a.texto} e ${b.texto}",
            fala = listOf(pt("No dicionário, qual palavra fica entre ${fpal(a)} e ${fpal(b)}?")),
            cena = Cena.Nenhuma,
            certa = opPal(certa),
            erradas = tres(certa, outras) { it.texto }.map(::opPal),
            reforco = listOf(pt("${fpal(a)}, ${fpal(certa)}, ${fpal(b)}!")),
        )
    }

    private fun cacaDicionario(mundo: Int): Pergunta {
        val g = L.dicionario.um()
        val letra = g.first().inicial
        return cacar(mundo, "Ache o que fica na página do $letra", "Dicionário ilustrado! Ache as figuras que ficam na página da letra ${L.falaDaLetra(letra)}.",
            g.map(::opFig), L.dicionario.filter { it != g }.flatten().map(::opFig), "Página do ${L.falaDaLetra(letra)}!")
    }

    // ------------------------------------------------------------------ mundo 20: textos

    private fun cacaTexto(mundo: Int): Pergunta = cacaFrase(mundo, L.itensDaCaverna, L.nomes.um())

    // ------------------------------------------------------------------ mundo 21: sílabas, sílaba forte e acentos

    private val separaveis get() = juntar(L.cv3, L.cv4, L.travadasR, L.ditongos, L.forteNoComeco, L.nasaisN)
        .filter { it.silabas.size in 3..4 && it.silabas.distinct().size == it.silabas.size }

    private fun separarSilabas(lista: List<Palavra>): Pergunta {
        val p = lista.filter { it.silabas.size in 3..4 }.um()
        val certa = p.silabas.joinToString("-")
        val cortes = p.silabas.runningFold(0) { acc, s -> acc + s.length }.drop(1).dropLast(1)
        val texto = p.texto
        val variantes = mutableSetOf<String>()
        fun com(c: List<Int>) = (listOf(0) + c.sorted() + texto.length).zipWithNext { a, b -> texto.substring(a, b) }.joinToString("-")
        for (i in cortes.indices) for (d in listOf(-1, 1, 2, -2)) {
            val novo = cortes.toMutableList().also { it[i] = it[i] + d }
            if (novo.all { it in 1 until texto.length } && novo.distinct().size == novo.size) variantes += com(novo)
        }
        for (i in cortes.indices) variantes += com(cortes.filterIndexed { j, _ -> j != i })
        val erradas = tres(certa, variantes.shuffled(random))
        return montar(
            instrucao = "Como separar ${p.texto}?",
            fala = listOf(pt("${fpal(p)}. Bata palmas e separe os pedacinhos. Qual está separado certinho?")),
            cena = Cena.Emojis(p.emoji),
            certa = opTx(certa, pedacos(p)),
            erradas = erradas.map { opTx(it, it.split("-").joinToString(", ") { s -> falaS(s) }) },
            reforco = listOf(pt("${pedacos(p)}!")),
        )
    }

    private fun tonicaEscolha(): Pergunta {
        val listas = listOf(L.forteNoFim to "no fim", L.forteNoMeio to "no meio", L.forteNoComeco to "no começo")
        val exemplo = mapOf("no fim" to "em jacaré", "no meio" to "em cavalo", "no começo" to "em pássaro")
        val (lista, onde) = listas.um()
        val certa = lista.um()
        val outras = listas.filter { it.second != onde }.flatMap { it.first }.shuffled(random)
        return montar(
            instrucao = "Pedacinho forte $onde",
            fala = listOf(pt("Fale devagar e escute o pedacinho mais forte. Qual palavra tem o pedacinho forte $onde, como ${exemplo.getValue(onde)}?")),
            cena = Cena.Nenhuma,
            certa = opPal(certa),
            erradas = tres(certa, outras) { it.texto }.map(::opPal),
            reforco = listOf(pt("${fpal(certa)}: o pedacinho forte fica $onde!")),
        )
    }

    private fun pescariaAcento(): Pergunta = pescar("Pesque as palavras com acento", "Pesque as palavras que têm acento, aquele risquinho ou chapeuzinho em cima da vogal.",
        L.acentos.map(::opPal), L.semAcento.map(::opPal), "Palavras com acento!")

    // ------------------------------------------------------------------ mundo 22: ditado com dificuldades

    private val dificeis get() = juntar(L.rr, L.rFraco, L.ss, L.sComSomDeZ, L.nasaisAo, L.lOuU)

    private fun baloesCertinhas(lista: List<Palavra>): Pergunta {
        val certas = lista.sorteia(3)
        val todas = lista.map { it.texto }.toSet()
        val erradas = lista.filter { it !in certas }.shuffled(random).mapNotNull { p -> grafiasErradas(p.texto).firstOrNull { it !in todas } }
        return estourar(
            instrucao = "Estoure as escritas certinhas",
            fala = "Leia com atenção! Estoure só os balões com as palavras escritas certinho.",
            certas = certas.map { opTx(it.texto, fpal(it)) },
            erradas = erradas.map { opTx(it, soletrar(it)) },
            reforco = "Escritas certinho: ${certas.joinToString(", ") { fpal(it) }}!",
        )
    }

    private fun aoOuAm(): Pergunta {
        if (random.nextBoolean()) return comoSeEscreveL(comAo, figura = false)
        val (frase, verbo) = L.verbosAm.um()
        val erradas = listOf(verbo.dropLast(2) + "ÃO", verbo.dropLast(1) + "N", verbo.dropLast(1)).map { it.lowercase() }
        val certa = verbo.lowercase()
        return montar(
            instrucao = frase,
            fala = listOf(pt(frase.replace("___", certa)), pt("Como se escreve $certa nesta frase?")),
            cena = Cena.Nenhuma,
            certa = opTx(certa, soletrar(verbo)),
            erradas = erradas.map { opTx(it, soletrar(it.uppercase())) },
            reforco = listOf(pt("$certa termina com á, ême: eles fazem agora.")),
        )
    }

    // ------------------------------------------------------------------ mundo 23: parlendas, versos e rimas

    private fun completarVerso(): Pergunta {
        val q = L.versosParaCompletar.um()
        return montar(
            instrucao = q.pergunta,
            fala = listOf(pt(q.pergunta.replace("___", "hum hum hum")), pt("Qual palavra completa a rima?")),
            cena = Cena.Nenhuma,
            certa = opTx(q.certa),
            erradas = q.erradas.map { opTx(it) },
            reforco = listOf(pt(q.pergunta.replace("___", q.certa))),
        )
    }

    private fun ligarRimas(): Pergunta {
        val grupos = L.rimas.sorteia(4)
        val pares = grupos.take(3).map { g -> g.sorteia(2).let { it[0].texto to it[1].texto } }
        return ligarTextos(pares, "Ligue as palavras que rimam", "Ligue cada palavra com a que rima com ela. Uma vai sobrar.", n = 3, sobra = grupos[3].um().texto)
    }

    private val coresDaSequencia = listOf(0xFFEF476F, 0xFF06D6A0, 0xFF118AB2, 0xFFFFD166)

    private fun sigaParlenda(): Pergunta {
        val (verso, palavras) = L.parlendas.um()
        val botoes = palavras.shuffled(random)
        return Pergunta.deAtividade(
            instrucao = "Siga a parlenda",
            fala = listOf(pt("Escute a parlenda: $verso. Depois toque nas palavras na mesma ordem.")),
            atividade = SigaASequencia(botoes.map { opTx(it) }, coresDaSequencia, palavras.map { botoes.indexOf(it) }),
            reforco = listOf(pt("$verso!")),
        )
    }

    // ------------------------------------------------------------------ mundo 24: começo, meio e fim

    private val momentos = listOf("primeiro", "no meio", "no fim")

    private fun historiaEscolha(narrar: Boolean): Pergunta {
        val h = L.historiasEmQuadros.um()
        val i = random.nextInt(3)
        val q = h.quadros[i]
        val outra = L.historiasEmQuadros.filter { it != h }.flatMap { it.quadros }.filter { o -> h.quadros.none { it.first == o.first } }.um()
        val texto = h.quadros.joinToString(" ") { it.second }
        return montar(
            instrucao = if (narrar) "O que aconteceu ${momentos[i]}?" else "$texto O que aconteceu ${momentos[i]}?",
            fala = if (narrar) listOf(pt("Escute a história: $texto"), pt("O que aconteceu ${momentos[i]}?")) else listOf(pt("Leia a história. O que aconteceu ${momentos[i]}?")),
            cena = Cena.Nenhuma,
            certa = opEmo(q.first, q.second),
            erradas = (h.quadros.filter { it != q } + outra).map { opEmo(it.first, it.second) },
            reforco = listOf(pt(q.second)),
        )
    }

    private fun cartasHistoria(extra: Boolean): Pergunta {
        val h = L.historiasEmQuadros.um()
        val extras = if (extra) listOf(L.historiasEmQuadros.filter { it != h }.flatMap { it.quadros }.filter { o -> h.quadros.none { it.first == o.first } }.um()) else emptyList()
        val cartas = (h.quadros + extras).shuffled(random)
        return Pergunta.deAtividade(
            instrucao = "Vire na ordem da história",
            fala = listOf(pt("Escute: ${h.quadros.joinToString(" ") { it.second }} Vire as cartas na ordem: começo, meio e fim.")),
            atividade = CartasViradas(cartas.map { opEmo(it.first, it.second) }, h.quadros.map { cartas.indexOf(it) }),
            reforco = listOf(pt(h.quadros.joinToString(" ") { it.second })),
        )
    }

    private fun tremHistoria(): Pergunta {
        val h = L.historiasEmQuadros.um()
        val extras = L.historiasEmQuadros.filter { it != h }.flatMap { it.quadros }.filter { o -> h.quadros.none { it.first == o.first } }.sorteia(random.nextInt(2))
        return Pergunta.deAtividade(
            instrucao = "Trem da história",
            fala = listOf(pt("Ponha os vagões na ordem da história: começo, meio e fim. ${h.quadros.joinToString(" ") { it.second }}")),
            atividade = Trem(h.quadros.map { opEmo(it.first, it.second) }, extras = extras.map { opEmo(it.first, it.second) }),
            reforco = listOf(pt(h.quadros.joinToString(" ") { it.second })),
        )
    }

    private fun conectivoEscolha(narrar: Boolean): Pergunta {
        val q = L.conectivos.um()
        return montar(
            instrucao = q.pergunta,
            fala = if (narrar) listOf(pt(q.pergunta.replace("___", "hum hum")), pt("Qual palavra liga as ideias: e, mas ou porque?")) else listOf(pt("Leia e escolha a palavra que liga as ideias.")),
            cena = Cena.Nenhuma,
            certa = opTx(q.certa),
            erradas = q.erradas.map { opTx(it) },
            reforco = listOf(pt(q.pergunta.replace("___", q.certa))),
        )
    }

    // ------------------------------------------------------------------ mundo 25: revisão

    private val revisaoGeral get() = juntar(L.rr, L.ss, L.qu, L.gu, L.cedilha, L.gSuave, L.che, L.xis, L.aga, L.mpmb, L.ditongos, L.acentos, L.encontrosR)

    private fun cacaRevisao(mundo: Int): Pergunta {
        val regras = listOf(
            Triple("CH", "cê agá", L.che to juntar(L.cv2, L.cv3)),
            Triple("Ç", "cê cedilha", L.cedilha to L.cSuave),
            Triple("RR", "dois érres", L.rr to L.rFraco),
            Triple("ÃO", "ão com til", comAo to juntar(L.cv2, L.cv3)),
            Triple("H", "agá no começo", L.aga to L.semAga),
        )
        val (marca, nome, listas) = regras.um()
        return cacar(mundo, "Ache as figuras com $marca", "Caça das estrelas! Ache as figuras que se escrevem com $nome.",
            listas.first.map(::opFig), listas.second.filter { marca !in it.texto }.map(::opFig), "Com $nome!")
    }

    private fun mistoDificil(): Pergunta = when (random.nextInt(6)) {
        0 -> fraseCertinha(L.frasesComNome)
        1 -> pontuacao(L.frasesDoces, narrar = false)
        2 -> pluralEscolha(L.pluraisDificeis)
        3 -> sentidoEscolha(L.antonimos, contrario = true, narrar = false)
        4 -> conectivoEscolha(narrar = false)
        else -> concordanciaEscolha()
    }

    private fun dominoMisto(): Pergunta = when (random.nextInt(3)) {
        0 -> dominoFiguras(revisaoGeral)
        1 -> dominoTextos(emPares(L.antonimos), "Dominó dos contrários", "Dominó! Encaixe cada palavra com o seu contrário.")
        else -> dominoTextos(emPares(L.pluraisDificeis), "Dominó do plural", "Dominó! Encaixe cada palavra com o seu plural.")
    }

    /** O grande chefão final: cada pergunta vem de uma faixa de 50 fases (mundos 1 a 5, 6 a 10...), em rodízio. */
    private var faixaDoChefaoFinal = 0

    private fun grandeChefao(): Pergunta {
        val faixa = faixaDoChefaoFinal++ % 5
        val fim = if (faixa == 4) 49 else 50
        return fase(faixa * 50 + 1 + random.nextInt(fim)).criar()
    }

    // ------------------------------------------------------------------ os mundos

    private val mundosNovos: List<ModeloDeFase> by lazy {
        mundo6 + mundo7 + mundo8 + mundo9 + mundo10 + mundo11 + mundo12 + mundo13 + mundo14 + mundo15 +
            mundo16 + mundo17 + mundo18 + mundo19 + mundo20 + mundo21 + mundo22 + mundo23 + mundo24 + mundo25
    }

    private val cvTodas get() = juntar(L.cv2, L.cv3)

    /** Mundo 6, Fazenda: todas as famílias silábicas simples, palavras de 2 e 3 sílabas. */
    private val mundo6 by lazy {
        listOf(
            nova("Famílias da fazenda", "🐄", ::tocarSilabaFamilia, { lerFigura(L.fazenda) }, ::pescariaFamilia),
            nova("Pescaria de sílabas", "🎣", ::pescariaFamilia, ::tocarSilabaFamilia),
            nova("Sílabas parecidas", "🧐", { silabaFaltaL(cvTodas) }, { completarFamilia() }, ::pescariaFamilia),
            nova("Ditado de sílabas", "👂", { ditadoSilabas(L.cv2, figura = true) }, { lerFigura(L.cv2) }, { escreverPalavra(L.cv2) }),
            nova("Marco da fazenda", "🚩", ::pescariaFamilia, ::tocarSilabaFamilia, { silabaFaltaL(cvTodas) }, { ditadoSilabas(L.cv2, true) }),
            nova("Palavras de três pedacinhos", "📖", { lerFigura(L.cv3) }, { quantosPedacinhos(cvTodas) }, { montarPalavraL(L.cv3) }),
            nova("Montar na fazenda", "🧱", { montarPalavraL(juntar(L.cv3, L.fazenda)) }, { lerFigura(L.cv3) }, { escreverPalavra(L.fazenda) }),
            nova("Fazenda e espaço", "🔁", { ditadoSilabas(L.cv3, true) }, { revisao(5) }, { palavraDaFigura(cvTodas, parecidas = true) }),
            nova("Leitura caprichada", "🔍", { lerFigura(juntar(L.cv3, L.cv4), parecidas = true) }, ::trocaLetra, { ditadoSilabas(L.cv3, false) }),
            nova("Chefão: a vaca gigante", "👑", { ditadoSilabas(juntar(L.cv3, L.cv4), false) }, ::pescariaFamilia, { lerFigura(juntar(L.cv3, L.cv4), true) }),
        )
    }

    /** Mundo 7, Fundo do Mar: ão, ãe, an, en, in, on, un e o til. */
    private val mundo7 by lazy {
        val somAo = { temSom(comAo, L.semNasal, "Qual tem o som ão?", "Escute: ão, como em mão. Qual figura tem o som ão?") }
        listOf(
            nova("O som do til", "🐠", somAo, ::baloesAo),
            nova("Balões do ÃO", "🎈", ::baloesAo, somAo),
            nova("Com til ou sem til?", "〰️", { comoSeEscreveL(L.nasaisAo, figura = true) }, { ligarFiguras(L.nasaisAo) }),
            nova("Ligar no mar", "🔗", { ligarFiguras(nasais) }, { lerFigura(nasais) }),
            nova("Marco do mar", "🚩", ::baloesAo, somAo, { comoSeEscreveL(L.nasaisAo, true) }, { ligarFiguras(nasais) }),
            nova("AN, EN, IN, ON, UN", "🐬", { silabaFaltaL(L.nasaisN) { s -> s.length >= 2 && s.last() == 'N' } }, { montarPalavraL(L.nasaisN) }),
            nova("Montar com som nasal", "🧱", { montarPalavraL(nasais) }, { lerFigura(L.nasaisN, parecidas = true) }, { escreverPalavra(L.nasaisN) }),
            nova("Mar e fazenda", "🔁", { ligarFiguras(nasais) }, { revisao(6) }, { palavraDaFigura(nasais, parecidas = true) }),
            nova("Til caprichado", "🔍", { comoSeEscreveL(nasais, figura = false) }, { baloesOrdem(nasais) }),
            nova("Chefão: o polvo", "👑", { baloesOrdem(nasais) }, { ligarFiguras(nasais) }, { ditadoSilabas(nasais, false) }),
        )
    }

    /** Mundo 8, Selva: ditongos ai, ei, oi, au, eu, ou; palavras de 3 sílabas. */
    private val mundo8 by lazy {
        val tresSilabas = juntar(L.ditongos, L.cv3).filter { it.silabas.size == 3 }
        listOf(
            nova("Dois sons juntinhos", "🐒", { lerFigura(L.ditongos) }, { dominoFiguras(L.ditongos) }),
            nova("Dominó da selva", "🎴", { dominoFiguras(L.ditongos) }, { lerFigura(L.ditongos) }),
            nova("Ditongos parecidos", "🧐", { comoSeEscreveL(L.ditongos, figura = true) }, { cacaDitongo(8) }),
            nova("Caça na selva", "🔎", { cacaDitongo(8) }, { silabaFaltaL(L.ditongos) { s -> sonsDosDitongos.any { it.first in s } } }),
            nova("Marco da selva", "🚩", { dominoFiguras(L.ditongos) }, { lerFigura(L.ditongos) }, { comoSeEscreveL(L.ditongos, true) }, { cacaDitongo(8) }),
            nova("Três pedacinhos", "🐍", { lerFigura(tresSilabas, parecidas = true) }, { quantosPedacinhos(juntar(L.ditongos, L.cv3)) }, { ditadoSilabas(L.ditongos, true) }),
            nova("Ditado da selva", "👂", { ditadoSilabas(L.ditongos, true) }, { silabaFaltaL(L.ditongos) }),
            nova("Selva e mar", "🔁", { cacaDitongo(8) }, { revisao(7) }, { palavraDaFigura(L.ditongos, true) }),
            nova("Leitura da selva", "🔍", { palavraDaFigura(juntar(L.ditongos, nasais), parecidas = true) }, ::trocaLetra, { dominoFiguras(L.ditongos) }),
            nova("Chefão: o macaco rei", "👑", { dominoFiguras(juntar(L.ditongos, L.cv3)) }, { cacaDitongo(8) }, { ditadoSilabas(L.ditongos, false) }),
        )
    }

    /** Mundo 9, Polo Norte: sílabas terminadas em R, S e L (mar, pés, sol). */
    private val mundo9 by lazy {
        val curtas = { trav.filter { it.texto.length <= 6 } }
        listOf(
            nova("R, S e L no fim", "🐧", { lerFigura(trav) }, { ditadoLetras(curtas(), true) }),
            // Escrever com o dedo é a atividade principal desta fase (a palavra inteira, letra por letra).
            nova("Ditado de letras", "✏️", { escreverPalavra(trav) }, { ditadoLetras(curtas(), true) }, { lerFigura(trav) }),
            nova("Parecidas no gelo", "🧊", { silabaFaltaL(trav, ::travada) }, ::pescariaTravada),
            nova("Pescaria no gelo", "🎣", ::pescariaTravada, { comoSeEscreveL(trav, true) }, { escreverPalavra(trav) }),
            nova("Marco do gelo", "🚩", { ditadoLetras(curtas(), true) }, { lerFigura(trav) }, { silabaFaltaL(trav, ::travada) }, ::pescariaTravada),
            nova("O S e o L no fim", "🎿", { comoSeEscreveL(juntar(L.travadasS, L.travadasL), true) }, { quebraSilabas(trav) }),
            nova("Quebra cabeça de sílabas", "🧩", { quebraSilabas(trav) }, { quantosPedacinhos(trav) }),
            nova("Gelo e selva", "🔁", ::pescariaTravada, { revisao(8) }, { palavraDaFigura(trav, true) }),
            nova("Desafio do gelo", "🔍", { comoSeEscreveL(trav, figura = false) }, { quebraSilabas(trav) }),
            nova("Chefão: o pinguim", "👑", { ditadoLetras(trav.filter { it.texto.length <= 7 }, false) }, { quebraSilabas(trav) }, { palavraDaFigura(trav, true) }),
        )
    }

    /** Mundo 10, Circo: RR, SS, QU, GU; pares parecidos (caro e carro). */
    private val mundo10 by lazy {
        listOf(
            nova("R ou RR?", "🎪", { lerFigura(juntar(L.rr, L.rFraco), parecidas = true) }, { ligarFiguras(juntar(L.rr, L.rFraco)) }),
            nova("Ligar RR e SS", "🔗", { ligarFiguras(rrss) }, { lerFigura(rrss) }),
            nova("Caro ou carro?", "✍️", { comoSeEscreveL(juntar(L.rr, L.ss, L.rFraco), true) }, ::baloesDigrafo),
            nova("Balões do circo", "🎈", ::baloesDigrafo, { silabaFaltaL(rrss) { s -> s.startsWith("R") || s.startsWith("S") } }),
            nova("Marco do circo", "🚩", { ligarFiguras(circo) }, { lerFigura(circo) }, { comoSeEscreveL(circo, true) }, ::baloesDigrafo),
            nova("QU e GU", "🤹", { silabaFaltaL(qugu) { s -> "QU" in s || "GU" in s } }, { ditadoSilabas(qugu, true) }),
            nova("Ditado do circo", "👂", { ditadoSilabas(circo, true) }, { palavraDaFigura(qugu, true) }),
            nova("Circo e gelo", "🔁", ::baloesDigrafo, { revisao(9) }, { comoSeEscreveL(qugu, true) }),
            nova("Malabarismo de letras", "🔍", { comoSeEscreveL(circo, figura = false) }, { ligarFiguras(circo) }),
            nova("Chefão: o palhaço", "👑", { ligarFiguras(circo) }, { ditadoSilabas(circo, false) }, ::baloesDigrafo),
        )
    }

    /** Mundo 11, Deserto: C e Ç, G e J (ge, gi), S com som de Z entre vogais. */
    private val mundo11 by lazy {
        val gjCurtas = { juntar(gj, sz).filter { it.texto.length <= 7 } }
        listOf(
            nova("O cê cedilha", "🐫", { comoSeEscreveL(L.cedilha, figura = true) }, ::pescariaCedilha),
            nova("Pescaria do Ç", "🎣", ::pescariaCedilha, { lerFigura(cc) }),
            nova("ÇA, SA ou CA?", "🧐", { silabaFaltaL(cc) { s -> 'Ç' in s || s.startsWith("CE") || s.startsWith("CI") } }, { dominoFiguras(cc) }),
            nova("Dominó do deserto", "🎴", { dominoFiguras(juntar(cc, sz)) }, { palavraDaFigura(cc, true) }),
            nova("Marco do deserto", "🚩", ::pescariaCedilha, { comoSeEscreveL(cc, true) }, { silabaFaltaL(cc) { s -> 'Ç' in s } }, { dominoFiguras(cc) }),
            nova("GE, GI, JE, JI", "🌵", { silabaFaltaL(gj) { s -> s.startsWith("G") || s.startsWith("J") } }, { ditadoLetras(gjCurtas(), true) }),
            nova("Ditado do deserto", "👂", { ditadoLetras(gjCurtas(), true) }, { lerFigura(gj) }, { escreverPalavra(gjCurtas()) }),
            nova("Deserto e circo", "🔁", { dominoFiguras(juntar(gj, sz)) }, { revisao(10) }, { comoSeEscreveL(gj, true) }),
            nova("S com som de Z", "🏜️", { comoSeEscreveL(sz, figura = false) }, ::pescariaSZ),
            nova("Chefão: o camelo", "👑", ::pescariaCedilha, { dominoFiguras(juntar(cc, gj, sz)) }, { ditadoLetras(gjCurtas(), false) }),
        )
    }

    /** Mundo 12, Cidade: encontros consonantais BL, CL, FL, GL, PL, CR, DR, FR, GR, VR. */
    private val mundo12 by lazy {
        listOf(
            nova("Duas consoantes juntas", "🏙️", { silabaFaltaL(enc, ::temEncontro) }, { montarPalavraL(enc) }),
            nova("Montar na cidade", "🧱", { montarPalavraL(enc) }, { lerFigura(enc) }),
            nova("BL ou BR?", "🧐", { comoSeEscreveL(enc, figura = true) }, { cacaEncontro(12) }),
            nova("Caça na cidade", "🔎", { cacaEncontro(12) }, { palavraDaFigura(enc, true) }),
            nova("Marco da cidade", "🚩", { montarPalavraL(enc) }, { silabaFaltaL(enc, ::temEncontro) }, { comoSeEscreveL(enc, true) }, { cacaEncontro(12) }),
            nova("CR, DR, FR, GR, VR", "🚦", { silabaFaltaL(L.encontrosR, ::temEncontro) }, { ditadoSilabas(L.encontrosR, true) }),
            nova("Ditado da cidade", "👂", { ditadoSilabas(enc, true) }, { lerFigura(enc, parecidas = true) }),
            nova("Cidade e deserto", "🔁", { cacaEncontro(12) }, { revisao(11) }, { comoSeEscreveL(enc, true) }),
            nova("Leitura na cidade", "🔍", { comoSeEscreveL(enc, figura = false) }, { montarPalavraL(enc) }),
            nova("Chefão: o prédio gigante", "👑", { cacaEncontro(12) }, { ditadoSilabas(enc, false) }, { montarPalavraL(enc) }),
        )
    }

    /** Mundo 13, Castelo: X e CH, H no começo da palavra, Z e S. */
    private val mundo13 by lazy {
        val curtas = { juntar(L.zeta, L.sComSomDeZ, L.aga, xch).filter { it.texto.length <= 7 } }
        listOf(
            nova("X ou CH?", "🏰", { silabaFaltaL(xch) { s -> 'X' in s || "CH" in s } }, ::baloesXCH),
            nova("Balões do CH", "🎈", ::baloesXCH, { lerFigura(xch) }),
            nova("Xícara ou chícara?", "✍️", { comoSeEscreveL(xch, figura = true) }, { ligarFiguras(xch) }),
            nova("Ligar no castelo", "🔗", { ligarFiguras(juntar(L.aga, L.xis)) }, { palavraDaFigura(xch, true) }),
            nova("Marco do castelo", "🚩", ::baloesXCH, { silabaFaltaL(xch) { s -> 'X' in s || "CH" in s } }, { comoSeEscreveL(xch, true) }, { ligarFiguras(xch) }),
            nova("O H que não tem som", "🤫", { comoSeEscreveL(L.aga, figura = true) }, { ditadoLetras(L.aga.filter { it.texto.length <= 7 }, true) }),
            nova("Ditado do castelo", "👂", { ditadoLetras(curtas(), true) }, ::comecaComH, { escreverPalavra(curtas()) }),
            nova("Castelo e cidade", "🔁", { ligarFiguras(juntar(L.zeta, L.sComSomDeZ)) }, { revisao(12) }, { comoSeEscreveL(L.zeta, true) }),
            nova("Z ou S?", "🔍", { comoSeEscreveL(juntar(L.zeta, L.sComSomDeZ, xch, L.aga), figura = false) }, ::baloesXCH),
            nova("Chefão: o dragão", "👑", { ligarFiguras(juntar(xch, L.aga)) }, ::baloesXCH, { ditadoLetras(curtas(), false) }),
        )
    }

    /** Mundo 14, Vale dos Dinossauros: M antes de P e B, M e N no fim da sílaba. */
    private val mundo14 by lazy {
        val grandes = { mn.filter { it.silabas.size in 3..4 } }
        listOf(
            nova("M antes de P e B", "🦕", { comoSeEscreveL(L.mpmb, figura = true) }, { dominoFiguras(L.mpmb) }),
            nova("Dominó dos dinossauros", "🎴", { dominoFiguras(mn) }, { lerFigura(L.mpmb) }),
            nova("TAM, TAN ou TA?", "🧐", { silabaFaltaL(juntar(L.mpmb, L.nAntes)) { s -> s.length >= 2 && s.last() in "MN" } }, ::pescariaMN),
            nova("Pescaria do M", "🎣", ::pescariaMN, { palavraDaFigura(mn, true) }),
            nova("Marco dos dinossauros", "🚩", { dominoFiguras(mn) }, { comoSeEscreveL(L.mpmb, true) }, { silabaFaltaL(L.mpmb) { s -> s.last() == 'M' } }, ::pescariaMN),
            nova("M e N no fim", "🦖", { comoSeEscreveL(L.mFim, figura = true) }, { ditadoSilabas(mn, true) }),
            nova("Ditado grande", "👂", { ditadoSilabas(grandes(), true) }, { quantosPedacinhos(mn) }),
            nova("Dinossauros e castelo", "🔁", ::pescariaMN, { revisao(13) }, { lerFigura(mn, parecidas = true) }),
            nova("Palavras compridas", "🔍", { comoSeEscreveL(mn, figura = false) }, { dominoFiguras(mn) }),
            nova("Chefão: o tiranossauro", "👑", { dominoFiguras(mn) }, ::pescariaMN, { ditadoSilabas(grandes(), false) }),
        )
    }

    /** Mundo 15, Ilha do Tesouro: frases, ordem das palavras, maiúscula no começo e ponto final. */
    private val mundo15 by lazy {
        val ilha = L.frasesIlha
        listOf(
            nova("Ler frases da ilha", "🏝️", { fraseFigura(ilha) }, { montarFraseL(ilha, 0, 3..5) }),
            nova("Montar a frase", "🧩", { montarFraseL(ilha, 0, 3..5) }, { fraseFigura(ilha) }),
            nova("Frase na ordem", "🔀", { ordemCerta(ilha) }, { cacaFrase(15, L.itensDaIlha, null) }),
            nova("Caça ao tesouro", "💰", { cacaFrase(15, L.itensDaIlha, null) }, { fraseFigura(ilha) }),
            nova("Marco da ilha", "🚩", { montarFraseL(ilha, 1, 3..5) }, { fraseFigura(ilha) }, { ordemCerta(ilha) }, { cacaFrase(15, L.itensDaIlha, null) }),
            nova("Maiúscula e ponto", "🔠", { fraseCertinha(ilha) }, { quebraFrase(ilha) }),
            nova("Quebra cabeça de palavras", "🧩", { quebraFrase(ilha) }, { fraseCertinha(ilha) }),
            nova("Ilha e dinossauros", "🔁", { montarFraseL(ilha, 2, 3..5) }, { revisao(14) }, { ordemCerta(ilha) }),
            nova("Nomes com letra grande", "🔍", { fraseCertinha(L.frasesComNome) }, { cacaFrase(15, L.itensDaIlha, L.nomes.um()) }),
            nova("Chefão: o pirata", "👑", { montarFraseL(ilha, 2, 3..5) }, { cacaFrase(15, L.itensDaIlha, L.nomes.um()) }, { fraseCertinha(ilha) }),
        )
    }

    /** Mundo 16, Fábrica de Doces: ponto, interrogação, exclamação; bilhete e lista. */
    private val mundo16 by lazy {
        val doces = L.frasesDoces
        listOf(
            nova("Ponto, pergunta ou espanto?", "🍭", { pontuacao(doces, narrar = true) }, { montarFraseL(doces, 0, 3..6) }),
            nova("Montar frases doces", "🍬", { montarFraseL(doces, 1, 3..6) }, { pontuacao(doces, true) }),
            nova("Qual é o sinal?", "❓", { pontuacao(doces, narrar = false) }, ::ligarSinais),
            nova("Ligar frase e sinal", "🔗", ::ligarSinais, { fraseFigura(doces) }),
            nova("Marco da fábrica", "🚩", { montarFraseL(doces, 1, 3..6) }, { pontuacao(doces, false) }, ::ligarSinais, { fraseFigura(doces) }),
            nova("Bilhetes e listas", "📝", { textoPergunta(L.bilhetes, narrar = true) }, ::baloesLista),
            nova("Balões da lista", "🎈", ::baloesLista, { textoPergunta(L.bilhetes, true) }),
            nova("Fábrica e ilha", "🔁", ::ligarSinais, { revisao(15) }, { pontuacao(doces, false) }),
            nova("Leitura de bilhete", "🔍", { textoPergunta(L.bilhetes, narrar = false) }, { montarFraseL(doces, 2, 3..6) }),
            nova("Chefão: o pirulito gigante", "👑", { montarFraseL(doces, 2, 3..6) }, ::ligarSinais, { textoPergunta(L.bilhetes, false) }),
        )
    }

    /** Mundo 17, Bosque Encantado: singular e plural, masculino e feminino. */
    private val mundo17 by lazy {
        val todosPlurais = L.pluraisFaceis + L.pluraisDificeis
        val dominoPlural = { dominoTextos(emPares(todosPlurais), "Dominó do plural", "Dominó! Encaixe cada palavra com o seu plural.") }
        val dominoGenero = { dominoTextos(emPares(L.masculinoFeminino), "Dominó do masculino e feminino", "Dominó! Encaixe cada palavra com o seu par: menino com menina, gato com gata.") }
        listOf(
            nova("Um ou muitos?", "🍄", { pluralEscolha(L.pluraisFaceis) }, dominoPlural),
            nova("Dominó do plural", "🎴", dominoPlural, { pluralEscolha(L.pluraisFaceis) }),
            nova("Plurais difíceis", "🧐", { pluralEscolha(L.pluraisDificeis) }, ::pescariaPlural),
            nova("Pescaria do plural", "🎣", ::pescariaPlural, { pluralEscolha(todosPlurais) }),
            nova("Marco do bosque", "🚩", dominoPlural, { pluralEscolha(L.pluraisFaceis) }, { pluralEscolha(L.pluraisDificeis) }, ::pescariaPlural),
            nova("Menino e menina", "👧", ::femininoEscolha, ::montarFraseConcordancia),
            nova("Frases que combinam", "🧩", ::montarFraseConcordancia, ::femininoEscolha),
            nova("Bosque e fábrica", "🔁", ::pescariaFeminino, { revisao(16) }, ::concordanciaEscolha),
            nova("Combinar as palavras", "🔍", ::concordanciaEscolha, dominoGenero),
            nova("Chefão: a fada", "👑", { if (random.nextBoolean()) dominoPlural() else dominoGenero() }, ::pescariaPlural, ::montarFraseConcordancia),
        )
    }

    /** Mundo 18, Vulcão: sinônimos, antônimos, aumentativo e diminutivo. */
    private val mundo18 by lazy {
        val ligarContrarios = { ligarTextos(emPares(L.antonimos), "Ligue os contrários", "Ligue cada palavra com o seu contrário. Uma vai sobrar.") }
        val ligarAmigas = { ligarTextos(emPares(L.sinonimos), "Ligue as palavras amigas", "Ligue as palavras que querem dizer a mesma coisa. Uma vai sobrar.") }
        listOf(
            nova("Contrários", "🌋", { sentidoEscolha(L.antonimos, contrario = true) }, ligarContrarios),
            nova("Ligar os contrários", "🔗", ligarContrarios, { sentidoEscolha(L.antonimos, true) }),
            nova("Palavras amigas", "🤝", { sentidoEscolha(L.sinonimos, contrario = false) }, ::baloesTamanho),
            nova("Balões pequenininhos", "🎈", ::baloesTamanho, { sentidoEscolha(L.sinonimos, false) }),
            nova("Marco do vulcão", "🚩", { if (random.nextBoolean()) ligarContrarios() else ligarAmigas() }, { sentidoEscolha(L.antonimos, true) }, { sentidoEscolha(L.sinonimos, false) }, ::baloesTamanho),
            nova("Inho e ão", "🐭", ::tamanhoEscolha, ::cartasTamanhos),
            nova("Do menor para o maior", "🃏", ::cartasTamanhos, ::tamanhoEscolha),
            nova("Vulcão e bosque", "🔁", ::baloesTamanho, { revisao(17) }, { sentidoEscolha(L.sinonimos, false) }),
            nova("Ler e pensar", "🔍", { val contrario = random.nextBoolean(); sentidoEscolha(if (contrario) L.antonimos else L.sinonimos, contrario, narrar = false) }, ligarAmigas),
            nova("Chefão: o dragão de fogo", "👑", { if (random.nextBoolean()) ligarContrarios() else ligarAmigas() }, ::baloesTamanho, { sentidoEscolha(L.antonimos, true, narrar = false) }),
        )
    }

    /** Mundo 19, Reino das Nuvens: ordem alfabética pela segunda letra, dicionário ilustrado. */
    private val mundo19 by lazy {
        listOf(
            nova("Dicionário", "☁️", { primeiroNoDicionario(1) }, { tremAlfabetico(1) }),
            nova("Trem do alfabeto", "🚂", { tremAlfabetico(1) }, { primeiroNoDicionario(1) }),
            nova("Pela segunda letra", "🔤", { primeiroNoDicionario(2) }, { cartasAlfabeticas(2) }),
            nova("Cartas em ordem", "🃏", { cartasAlfabeticas(2) }, ::entreNoDicionario),
            nova("Marco das nuvens", "🚩", { tremAlfabetico(2) }, { primeiroNoDicionario(1) }, { primeiroNoDicionario(2) }, { cartasAlfabeticas(2) }),
            nova("Dicionário ilustrado", "📖", ::entreNoDicionario, { cacaDicionario(19) }),
            nova("Caça no dicionário", "🔎", { cacaDicionario(19) }, { primeiroNoDicionario(2) }),
            nova("Nuvens e vulcão", "🔁", { tremAlfabetico(2) }, { revisao(18) }, ::entreNoDicionario),
            nova("Pela terceira letra", "🔍", { primeiroNoDicionario(3) }, { cartasAlfabeticas(3) }),
            nova("Chefão: o gigante das nuvens", "👑", { tremAlfabetico(3) }, { cartasAlfabeticas(2) }, { primeiroNoDicionario(3) }),
        )
    }

    /** Mundo 20, Caverna de Cristal: texto curto, quem, onde, o que aconteceu. */
    private val mundo20 by lazy {
        val hist = L.historias
        val frases = L.frasesLongas
        listOf(
            nova("Quem é quem?", "💎", { textoPergunta(hist, narrar = true) }, { montarFraseL(frases, 0) }),
            nova("Montar frases da caverna", "🧩", { montarFraseL(frases, 0) }, { textoPergunta(hist, true) }),
            nova("Onde e o quê?", "🗺️", { textoPergunta(hist, narrar = true) }, { cacaTexto(20) }),
            nova("Caça na caverna", "🔎", { cacaTexto(20) }, { textoPergunta(hist, true) }),
            nova("Marco da caverna", "🚩", { montarFraseL(frases, 1) }, { textoPergunta(hist, true) }, { cacaTexto(20) }, ::ligarPerguntas),
            nova("O que aconteceu?", "📜", { textoPergunta(hist, narrar = false) }, ::ligarPerguntas),
            nova("Quem, onde e o quê", "🔗", ::ligarPerguntas, { textoPergunta(hist, false) }),
            nova("Caverna e nuvens", "🔁", { cacaTexto(20) }, { revisao(19) }, { textoPergunta(hist, false) }),
            nova("Leitor da caverna", "🔍", { textoPergunta(hist + L.bilhetes, narrar = false) }, { montarFraseL(frases, 1) }),
            nova("Chefão: o dragão de cristal", "👑", { montarFraseL(frases, 2) }, { textoPergunta(hist, false) }, { cacaTexto(20) }),
        )
    }

    /** Mundo 21, Parque de Diversões: separar sílabas, sílaba mais forte, acentos. */
    private val mundo21 by lazy {
        val acentoCurtas = { L.acentos.filter { it.texto.length in 3..8 } }
        val dificeisDeSeparar = { juntar(enc, L.ss, L.rr, L.nasaisN, L.qu).filter { it.silabas.size in 3..4 && it.silabas.distinct().size == it.silabas.size } }
        listOf(
            nova("Separar os pedacinhos", "🎡", { separarSilabas(separaveis) }, { quebraSilabas(separaveis) }),
            nova("Quebra cabeça de sílabas", "🧩", { quebraSilabas(separaveis) }, { separarSilabas(separaveis) }),
            nova("Pedacinho mais forte", "💪", ::tonicaEscolha, ::pescariaAcento),
            nova("Pescaria de acentos", "🎣", ::pescariaAcento, ::tonicaEscolha),
            nova("Marco do parque", "🚩", { quebraSilabas(separaveis) }, { separarSilabas(separaveis) }, ::tonicaEscolha, ::pescariaAcento),
            nova("Acentos", "🎢", { comoSeEscreveL(L.acentos, figura = true) }, { ditadoLetras(acentoCurtas(), true) }),
            nova("Ditado com acento", "👂", { ditadoLetras(acentoCurtas(), true) }, { comoSeEscreveL(L.acentos, true) }),
            nova("Parque e caverna", "🔁", ::pescariaAcento, { revisao(20) }, ::tonicaEscolha),
            nova("Sílabas difíceis", "🔍", { separarSilabas(dificeisDeSeparar()) }, { quebraSilabas(dificeisDeSeparar()) }),
            nova("Chefão: a montanha russa", "👑", { ditadoLetras(acentoCurtas(), false) }, { quebraSilabas(separaveis) }, ::pescariaAcento),
        )
    }

    /** Mundo 22, Cidade dos Robôs: ditado com R e RR, S e SS, ão e am, L e U no fim; só pela voz. */
    private val mundo22 by lazy {
        val rs = juntar(L.rr, L.rFraco)
        listOf(
            nova("Escute e escolha", "🤖", { comoSeEscreveL(rs, figura = false) }, { ditadoLetras(rs, false) }),
            nova("Ditado do R", "👂", { ditadoLetras(rs, false) }, { comoSeEscreveL(rs, false) }, { escreverPalavra(rs, figura = false) }),
            nova("S ou SS?", "🧐", { comoSeEscreveL(juntar(L.ss, L.sComSomDeZ), figura = false) }, { baloesCertinhas(dificeis) }),
            nova("Balões escritos certinho", "🎈", { baloesCertinhas(dificeis) }, { comoSeEscreveL(dificeis, false) }),
            nova("Marco dos robôs", "🚩", { ditadoLetras(dificeis, false) }, { comoSeEscreveL(rs, false) }, { comoSeEscreveL(L.ss, false) }, { baloesCertinhas(dificeis) }),
            nova("ÃO ou AM?", "🔊", ::aoOuAm, { dominoFiguras(juntar(L.lOuU, L.nasaisAo)) }),
            nova("Dominó dos robôs", "🎴", { dominoFiguras(juntar(L.lOuU, L.rr, L.ss)) }, { comoSeEscreveL(L.lOuU, false) }),
            nova("Robôs e parque", "🔁", { baloesCertinhas(dificeis) }, { revisao(21) }, ::aoOuAm),
            nova("Ditado caprichado", "🔍", { comoSeEscreveL(dificeis, figura = false) }, { ditadoLetras(dificeis, false) }),
            nova("Chefão: o robô gigante", "👑", { ditadoLetras(dificeis, false) }, { dominoFiguras(dificeis) }, { baloesCertinhas(dificeis) }),
        )
    }

    /** Mundo 23, Savana: receita, convite, poema com rimas, parlendas. */
    private val mundo23 by lazy {
        val textos = L.textosDoDia
        listOf(
            nova("Convite e receita", "🦁", { textoPergunta(textos, narrar = true) }, { montarFraseL(L.versos, 0) }),
            nova("Montar versos", "🧩", { montarFraseL(L.versos, 0) }, ::completarVerso),
            nova("Complete a rima", "🎵", ::completarVerso, ::ligarRimas),
            nova("Ligar as rimas", "🔗", ::ligarRimas, ::completarVerso),
            nova("Marco da savana", "🚩", { montarFraseL(L.versos, 1) }, { textoPergunta(textos, true) }, ::completarVerso, ::ligarRimas),
            nova("Parlendas", "🐘", { textoPergunta(textos, narrar = false) }, ::sigaParlenda),
            nova("Siga a parlenda", "🎶", ::sigaParlenda, ::completarVerso),
            nova("Savana e robôs", "🔁", ::ligarRimas, { revisao(22) }, { textoPergunta(textos, true) }),
            nova("Leitura da savana", "🔍", { textoPergunta(textos + L.bilhetes + L.historias, narrar = false) }, { montarFraseL(L.versos, 2) }),
            nova("Chefão: o leão poeta", "👑", { montarFraseL(L.versos, 2) }, ::ligarRimas, { textoPergunta(textos, false) }),
        )
    }

    /** Mundo 24, Vale das Cores: começo, meio e fim; ligar ideias com e, mas e porque. */
    private val mundo24 by lazy {
        val frases = L.frasesComConectivo
        listOf(
            nova("Começo, meio e fim", "🌈", { historiaEscolha(narrar = true) }, { cartasHistoria(false) }),
            nova("Cartas da história", "🃏", { cartasHistoria(false) }, { historiaEscolha(true) }),
            nova("O que veio depois?", "🎨", { historiaEscolha(narrar = false) }, { montarFraseL(frases, 0) }),
            nova("Montar com e, mas e porque", "🧩", { montarFraseL(frases, 1) }, { conectivoEscolha(true) }),
            nova("Marco das cores", "🚩", { cartasHistoria(true) }, { historiaEscolha(true) }, { montarFraseL(frases, 0) }, { conectivoEscolha(true) }),
            nova("E, mas, porque", "🔗", { conectivoEscolha(narrar = true) }, ::tremHistoria),
            nova("Trem da história", "🚂", ::tremHistoria, { conectivoEscolha(true) }),
            nova("Cores e savana", "🔁", { montarFraseL(frases, 2) }, { revisao(23) }, { historiaEscolha(false) }),
            nova("Ligar ideias", "🔍", { conectivoEscolha(narrar = false) }, { cartasHistoria(true) }),
            nova("Chefão: o arco das cores", "👑", { cartasHistoria(true) }, { montarFraseL(frases, 2) }, ::tremHistoria),
        )
    }

    /** Mundo 25, Castelo das Estrelas: revisão do 2º ano; a fase 250 é o grande chefão final. */
    private val mundo25 by lazy {
        val longas = L.frasesLongas
        val textos = L.historias + L.textosDoDia + L.bilhetes
        val dificeisTodas = { revisaoGeral.filter { it.texto.length in 4..8 } }
        listOf(
            nova("Leitura fluente", "🌟", { fraseFigura(longas) }, { ditadoLetras(dificeisTodas(), false) }),
            nova("Ditado das estrelas", "👂", { ditadoLetras(dificeisTodas(), false) }, { fraseFigura(longas) }, { escreverPalavra(revisaoGeral, figura = false) }),
            nova("Texto das estrelas", "📜", { textoPergunta(textos, narrar = false) }, { montarFraseL(longas, 1) }),
            nova("Frases das estrelas", "🧩", { montarFraseL(longas, 2) }, { textoPergunta(textos, false) }),
            nova("Marco das estrelas", "🚩", { ditadoSilabas(revisaoGeral.filter { it.silabas.size >= 3 }, false) }, { fraseFigura(longas) }, { textoPergunta(textos, false) }, { montarFraseL(longas, 2) }),
            nova("Escrever certinho", "✍️", { comoSeEscreveL(revisaoGeral, figura = false) }, { cacaRevisao(25) }),
            nova("Caça das estrelas", "🔎", { cacaRevisao(25) }, { comoSeEscreveL(revisaoGeral, false) }),
            nova("Dominó das estrelas", "🎴", ::dominoMisto, { revisao(24) }, { fraseCertinha(L.frasesComNome) }),
            nova("Quase lá!", "🔍", { ditadoLetras(dificeisTodas(), false) }, ::mistoDificil),
            ModeloDeFase("Grande chefão final", "🏆", ::grandeChefao),
        )
    }
}
