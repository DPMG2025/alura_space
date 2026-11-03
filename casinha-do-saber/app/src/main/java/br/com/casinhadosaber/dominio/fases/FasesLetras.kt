package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.*
import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Fases do caminho Letras (português): 250 fases em 25 mundos de 10 ([Mundos]).
 * As fases 1 a 50 são as de sempre (o progresso salvo continua valendo). As fases 51 a 250 ainda
 * são provisórias ([provisoria]) até cada mundo novo ganhar o conteúdo do plano (docs/plano-1000-fases.md).
 */
class FasesLetras(random: Random = Random.Default) : FasesDoCaminho(random) {

    override val trilha = Trilha.PORTUGUES

    override fun fase(numero: Int): ModeloDeFase {
        conferir(numero)
        return if (numero <= FASES_PRONTAS) ate50[numero - 1] else provisoria(numero, ate50)
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
}
