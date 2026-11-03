package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.*
import br.com.casinhadosaber.dominio.VocabularioDosMundos.FraseEn
import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Fases do caminho Inglês: 250 fases em 25 mundos de 10 ([Mundos]).
 * As fases 1 a 50 são as de sempre (o progresso salvo continua valendo). As fases 51 a 250 seguem o
 * plano (docs/plano-1000-fases.md, seção 4.3): um tema por mundo, a 10ª fase de cada mundo é o chefão,
 * e a instrução falada passa do português (mundos 6 a 12) para inglês com português (13 a 18) e só
 * inglês simples (19 a 25). Palavras e frases em inglês são sempre faladas com a voz em inglês.
 */
class FasesIngles(random: Random = Random.Default) : FasesDoCaminho(random) {

    override val trilha = Trilha.INGLES

    override fun fase(numero: Int): ModeloDeFase {
        conferir(numero)
        return if (numero <= FASES_PRONTAS) ate50[numero - 1] else depois50[numero - FASES_PRONTAS - 1]
    }

    /** As 50 fases de antes, na ordem (a fase 1 é o índice 0). */
    private val ate50: List<ModeloDeFase> by lazy { ingles + inglesMundos }

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

    private val ingles: List<ModeloDeFase> = listOf(
        ModeloDeFase("Colors", "🎨") { encontreEmIngles(Vocabulario.coresBasicas, 4, "Toque na cor") },
        ModeloDeFase("Animals", "🐶") { encontreEmIngles(Vocabulario.animais.take(6), 4) },
        ModeloDeFase("Numbers 1 a 5", "🖐️") { numeroEmIngles(1..5, 1..6, 4) },
        ModeloDeFase("Memory: animals", "🃏") { if (random.nextBoolean()) memoria(3, Vocabulario.animais) else encontreEmIngles(Vocabulario.animais.take(6), 4) },
        ModeloDeFase("Fruits", "🍎") { encontreEmIngles(Vocabulario.frutas, 4) },
        ModeloDeFase("More colors", "🌈") { encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor") },
        ModeloDeFase("Shapes", "🔺") { formas(emIngles = true) },
        ModeloDeFase("My body", "👂") { encontreEmIngles(Vocabulario.corpo, 4) },
        ModeloDeFase("Numbers 6 a 10", "🔟") { numeroEmIngles(6..10, 1..10, 4) },
        ModeloDeFase("Clothes", "👕") { encontreEmIngles(Vocabulario.roupas, 4) },
        ModeloDeFase("Memory: fruits", "🃏") { if (random.nextBoolean()) memoria(3, Vocabulario.frutas) else encontreEmIngles(Vocabulario.frutas, 4) },
        ModeloDeFase("My house", "🏠") { encontreEmIngles(Vocabulario.casa, 4) },
        ModeloDeFase("Food", "🍕") { encontreEmIngles(Vocabulario.comidas, 4) },
        ModeloDeFase("Toys", "🧸") { encontreEmIngles(Vocabulario.brinquedos + Vocabulario.animais.drop(6), 4) },
        ModeloDeFase("Weather", "🌦️") { encontreEmIngles(Vocabulario.tempo, 4) },
        ModeloDeFase("Family", "👨‍👩‍👧") { encontreEmIngles(Vocabulario.familia, 4) },
        ModeloDeFase("Vehicles", "🚌") { encontreEmIngles(Vocabulario.veiculos, 4) },
        ModeloDeFase("Actions", "🏃") { encontreEmIngles(Vocabulario.acoes, 4) },
        ModeloDeFase("Memory: big", "🃏") { when (random.nextInt(2)) { 0 -> memoria(4, Vocabulario.animais + Vocabulario.frutas); else -> memoria(4, Vocabulario.roupas + Vocabulario.comidas) } },
        ModeloDeFase("Colors and shapes", "🟣") { when (random.nextInt(2)) { 0 -> encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor"); else -> formas(emIngles = true) } },
        ModeloDeFase("Food and clothes", "👗") { when (random.nextInt(2)) { 0 -> encontreEmIngles(Vocabulario.comidas, 4); else -> encontreEmIngles(Vocabulario.roupas, 4) } },
        ModeloDeFase("Family and actions", "💃") { when (random.nextInt(2)) { 0 -> encontreEmIngles(Vocabulario.familia, 4); else -> encontreEmIngles(Vocabulario.acoes, 4) } },
        ModeloDeFase("All the numbers", "🔢") { numeroEmIngles(1..10, 1..10, 4) },
        ModeloDeFase("English challenge", "🏆") { when (random.nextInt(5)) { 0 -> encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor"); 1 -> encontreEmIngles(Vocabulario.animais, 4); 2 -> numeroEmIngles(1..10, 1..10, 4); 3 -> encontreEmIngles(Vocabulario.casa + Vocabulario.brinquedos, 4); else -> encontreEmIngles(Vocabulario.frutas + Vocabulario.corpo, 4) } },
        ModeloDeFase("English super star", "👑") { when (random.nextInt(4)) { 0 -> encontreEmIngles(Vocabulario.veiculos + Vocabulario.tempo, 4); 1 -> encontreEmIngles(Vocabulario.acoes + Vocabulario.familia, 4); 2 -> encontreEmIngles(Vocabulario.roupas + Vocabulario.comidas, 4); else -> memoria(4, Vocabulario.veiculos + Vocabulario.animais) } },
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

    private val inglesMundos: List<ModeloDeFase> = listOf(
        // Mundo 3 (Praia), fases 26 a 30
        ModeloDeFase("School", "🏫") { encontreEmIngles(V.escola, 4) },
        ModeloDeFase("Numbers 11 to 15", "🔢") { numeroEmIngles(11..15, 9..17, 4) },
        ModeloDeFase("Nature", "🌳") { encontreEmIngles(V.natureza, 4) },
        ModeloDeFase("Feelings", "😀") { encontreEmIngles(V.sentimentos, 4) },
        ModeloDeFase("Beach challenge", "🏆") { when (random.nextInt(4)) { 0 -> encontreEmIngles(V.escola, 4); 1 -> encontreEmIngles(V.natureza, 4); 2 -> encontreEmIngles(V.sentimentos, 4); else -> numeroEmIngles(11..15, 9..17, 4) } },
        // Mundo 4 (Montanha), fases 31 a 40
        ModeloDeFase("Numbers 16 to 20", "🔢") { numeroEmIngles(16..20, 13..20, 4) },
        ModeloDeFase("Jobs", "👷") { encontreEmIngles(V.profissoes, 4) },
        ModeloDeFase("Verbs", "🚶") { encontreEmIngles(V.verbos, 4) },
        ModeloDeFase("I like", "💛") { fraseEmIngles(V.gosto) },
        ModeloDeFase("What color is it?", "🎨") { corDaCoisa() },
        ModeloDeFase("Memory: school", "🃏") { if (random.nextBoolean()) memoria(4, V.escola) else encontreEmIngles(V.escola, 4) },
        ModeloDeFase("How many?", "🖐️") { quantosEmIngles(12) },
        ModeloDeFase("Wild animals", "🦒") { encontreEmIngles(V.animaisSelvagens, 4) },
        ModeloDeFase("I am, I can", "💪") { fraseEmIngles(V.souEPosso) },
        ModeloDeFase("Mountain challenge", "🏆") { when (random.nextInt(5)) { 0 -> encontreEmIngles(V.profissoes, 4); 1 -> encontreEmIngles(V.verbos, 4); 2 -> fraseEmIngles(V.gosto); 3 -> corDaCoisa(); else -> numeroEmIngles(11..20, 11..20, 4) } },
        // Mundo 5 (Espaço), fases 41 a 50
        ModeloDeFase("Opposites", "↕️") { encontreEmIngles(V.opostos, 4) },
        ModeloDeFase("Colors and numbers", "🌈") { when (random.nextInt(3)) { 0 -> corDaCoisa(); 1 -> encontreEmIngles(Vocabulario.todasAsCores, 4, "Toque na cor"); else -> numeroEmIngles(1..20, 1..20, 4) } },
        ModeloDeFase("School and jobs", "🎒") { encontreEmIngles(V.escola + V.profissoes, 4) },
        ModeloDeFase("Feelings and verbs", "🎭") { encontreEmIngles(V.sentimentos + V.verbos, 4) },
        ModeloDeFase("Memory: wild animals", "🃏") { if (random.nextBoolean()) memoria(5, V.animaisSelvagens) else encontreEmIngles(V.animaisSelvagens, 4) },
        ModeloDeFase("I like, I can", "🗣️") { fraseEmIngles(V.gosto + V.souEPosso) },
        ModeloDeFase("Numbers 1 to 20", "💯") { if (random.nextBoolean()) numeroEmIngles(1..20, 1..20, 4) else quantosEmIngles(15) },
        ModeloDeFase("Nature and weather", "🌦️") { encontreEmIngles(V.natureza + Vocabulario.tempo, 4) },
        ModeloDeFase("Little sentences", "💬") { when (random.nextInt(3)) { 0 -> fraseEmIngles(V.gosto); 1 -> fraseEmIngles(V.souEPosso); else -> corDaCoisa() } },
        ModeloDeFase("English master", "🌟") { when (random.nextInt(6)) { 0 -> encontreEmIngles(V.animaisSelvagens + V.natureza, 4); 1 -> encontreEmIngles(V.opostos + V.sentimentos, 4); 2 -> fraseEmIngles(V.gosto + V.souEPosso); 3 -> numeroEmIngles(1..20, 1..20, 4); 4 -> corDaCoisa(); else -> memoria(5, V.escola + V.profissoes) } },
    )

    // ================================================================== MUNDOS 6 A 25 (fases 51 a 250)

    private val I = VocabularioIngles

    /** As fases 51 a 250, na ordem (a fase 51 é o índice 0): 20 mundos de 10, o 10º de cada é o chefão. */
    private val depois50: List<ModeloDeFase> by lazy {
        val mundos = listOf(
            mundo6(), mundo7(), mundo8(), mundo9(), mundo10(), mundo11(), mundo12(), mundo13(), mundo14(), mundo15(),
            mundo16(), mundo17(), mundo18(), mundo19(), mundo20(), mundo21(), mundo22(), mundo23(), mundo24(), mundo25(),
        )
        mundos.forEachIndexed { i, m -> check(m.size == 10) { "O mundo ${i + 6} do Inglês precisa de 10 fases" } }
        mundos.flatten()
    }

    // ------------------------------------------------------------------ fala e opções

    /**
     * A instrução falada de cada mundo: só em português até o mundo 12, inglês simples seguido do
     * português do 13 ao 18, e só inglês simples do 19 em diante.
     */
    private fun pedir(m: Int, ptx: String, enx: String): List<Trecho> = when {
        m <= 12 -> listOf(pt(ptx))
        m <= 18 -> listOf(en(enx), pt(ptx))
        else -> listOf(en(enx))
    }

    /** O texto curto para o adulto, no mesmo idioma da instrução. */
    private fun rotulo(m: Int, ptx: String, enx: String) = if (m <= 12) ptx else enx

    /** O reforço depois do acerto: a palavra em inglês e, até o mundo 22, a tradução. */
    private fun reforco(m: Int, enx: String, ptx: String): List<Trecho> =
        if (m <= 22) listOf(en(enx), pt(ptx)) else listOf(en(enx), en("Great job!"))

    private fun maiuscula(s: String) = s.replaceFirstChar { it.uppercase() }

    private fun opFig(p: PalavraEn) = Opcao(Visual.Emoji(p.emoji), listOf(en(p.en)))
    private fun opPal(p: PalavraEn) = Opcao(Visual.Texto(p.en), listOf(en(p.en)))
    private fun opTxt(s: String) = Opcao(Visual.Texto(s), listOf(en(s)))
    private fun opCor(c: PalavraEn) = Opcao(Visual.Cor(c.cor ?: 0xFF000000), listOf(en(c.en)))
    private fun opNum(n: Int) = Opcao(Visual.Texto(n.toString()), listOf(en(I.numeroEn(n))))

    /** [n] palavras de [lista] diferentes (na figura e no nome) das de [fora]. */
    private fun outras(lista: List<PalavraEn>, fora: List<PalavraEn>, n: Int): List<PalavraEn> =
        lista.filter { p -> fora.none { it.emoji == p.emoji || it.en == p.en } }
            .distinctBy { it.emoji }.distinctBy { it.en }.sorteia(n)

    /** [n] palavras diferentes entre si (figura e nome). */
    private fun distintas(lista: List<PalavraEn>, n: Int): List<PalavraEn> =
        lista.distinctBy { it.emoji }.distinctBy { it.en }.sorteia(n)

    /** Três números de [candidatos] diferentes de [certo], os mais perto dele primeiro (com um pouco de sorteio). */
    private fun vizinhos(certo: Int, candidatos: Iterable<Int>): List<Int> =
        candidatos.filter { it != certo }.distinct()
            .map { it to kotlin.math.abs(it - certo) + random.nextDouble() * 1.5 }
            .sortedBy { it.second }.take(3).map { it.first }

    /** Escolhe um dos geradores ao acaso (cada fase mistura pelo menos dois jeitos de responder). */
    private fun ou(vararg geradores: () -> Pergunta): Pergunta = geradores.toList().um()()

    // ------------------------------------------------------------------ tocar (escolha)

    /** Escuta a palavra e toca na figura. Até o mundo 9 a palavra também aparece escrita. */
    private fun ouvirETocar(m: Int, lista: List<PalavraEn>): Pergunta {
        val certa = lista.um()
        val erradas = outras(lista, listOf(certa), 3)
        return montar(
            instrucao = rotulo(m, "Encontre: ${certa.en}", "Find: ${certa.en}"),
            fala = pedir(m, "Encontre:", "Find:") + en(certa.en),
            cena = if (m <= 9) Cena.Palavra(certa.en) else Cena.Nenhuma,
            certa = opFig(certa),
            erradas = erradas.map(::opFig),
            reforco = reforco(m, maiuscula(certa.en) + "!", "Em português, ${certa.pt}."),
        )
    }

    /** Vê a figura e lê o nome certo em inglês entre quatro palavras escritas. */
    private fun lerONome(m: Int, lista: List<PalavraEn>): Pergunta {
        val certa = lista.um()
        val erradas = outras(lista, listOf(certa), 3)
        return montar(
            instrucao = rotulo(m, "Qual é o nome em inglês?", "What is it?"),
            fala = pedir(m, "Qual é o nome desta figura em inglês? Leia as palavras.", "What is it? Read the words."),
            cena = Cena.Emojis(certa.emoji),
            certa = opPal(certa),
            erradas = erradas.map(::opPal),
            reforco = reforco(m, maiuscula(certa.en) + "!", "${maiuscula(certa.pt)}."),
        )
    }

    /** "Como se diz gato em inglês?": a figura e o nome em português, quatro palavras em inglês. */
    private fun comoSeDiz(m: Int, lista: List<PalavraEn>): Pergunta {
        val certa = lista.um()
        val erradas = outras(lista, listOf(certa), 3)
        val fala = if (m <= 18) listOf(pt("Como se diz ${certa.pt} em inglês?")) else listOf(en("How do you say"), pt(certa.pt), en("in English?"))
        return montar(
            instrucao = rotulo(m, "Como se diz em inglês?", "How do you say it?"),
            fala = fala,
            cena = Cena.Emojis(certa.emoji),
            certa = opPal(certa),
            erradas = erradas.map(::opPal),
            reforco = reforco(m, maiuscula(certa.en) + "!", "${maiuscula(certa.pt)} é ${certa.en}."),
        )
    }

    /** Lê (ou escuta) a frase e toca na figura que combina. */
    private fun fraseFigura(m: Int, frases: List<FraseEn>): Pergunta {
        val f = frases.um()
        val erradas = frases.filter { it.emoji != f.emoji }.distinctBy { it.emoji }.sorteia(3)
        return montar(
            instrucao = rotulo(m, "Leia e escolha a figura", "Read and choose"),
            fala = pedir(m, "Leia a frase, ou escute:", "Read or listen:") + en(f.en + ".") +
                (if (m <= 12) listOf(pt("Qual figura combina?")) else emptyList()),
            cena = Cena.Palavra(f.en),
            certa = opcaoFigura(f.emoji),
            erradas = erradas.map { opcaoFigura(it.emoji) },
            reforco = reforco(m, f.en + "!", f.pt + "."),
        )
    }

    /** Vê a figura e escolhe a frase em inglês que combina com ela. */
    private fun figuraFrase(m: Int, frases: List<FraseEn>): Pergunta {
        val f = frases.um()
        val erradas = frases.filter { it.emoji != f.emoji && it.en != f.en }.distinctBy { it.en }.sorteia(3)
        return montar(
            instrucao = rotulo(m, "Qual frase combina?", "Which sentence?"),
            fala = pedir(m, "Qual frase em inglês combina com a figura?", "Which sentence matches the picture?"),
            cena = Cena.Emojis(f.emoji),
            certa = opTxt(f.en),
            erradas = erradas.map { opTxt(it.en) },
            reforco = reforco(m, f.en + "!", f.pt + "."),
        )
    }

    /** Escuta o número em inglês e toca no algarismo. */
    private fun numeroOuvido(m: Int, certos: List<Int>, candidatos: List<Int>): Pergunta {
        val n = certos.um()
        val palavra = I.numeroEn(n)
        return montar(
            instrucao = rotulo(m, "Encontre: $palavra", "Find: $palavra"),
            fala = pedir(m, "Encontre o número", "Find the number") + en(palavra),
            cena = if (m <= 10) Cena.Palavra(palavra) else Cena.Nenhuma,
            certa = opNum(n),
            erradas = vizinhos(n, candidatos).map(::opNum),
            reforco = reforco(m, maiuscula(palavra) + "!", "${porExtenso(n)}!"),
        )
    }

    /** Lê o número escrito por extenso em inglês ("forty-two") e toca no algarismo, sem ouvir. */
    private fun lerNumero(m: Int, certos: List<Int>, candidatos: List<Int>): Pergunta {
        val n = certos.um()
        val palavra = I.numeroEn(n)
        return montar(
            instrucao = rotulo(m, "Que número é este?", "What number is it?"),
            fala = pedir(m, "Leia o número em inglês e toque no algarismo.", "Read the number."),
            cena = Cena.Palavra(palavra),
            certa = opNum(n),
            erradas = vizinhos(n, candidatos).map(::opNum),
            reforco = reforco(m, maiuscula(palavra) + "!", "${porExtenso(n)}!"),
        )
    }

    /** Conta as figuras e escolhe o número escrito em inglês. */
    private fun quantosEscrito(m: Int, lista: List<PalavraEn>, faixa: IntRange): Pergunta {
        val obj = lista.um()
        val n = faixa.random(random)
        val op = { k: Int -> opTxt(I.numeroEn(k)) }
        return montar(
            instrucao = "How many?",
            fala = pedir(m, "Conte e responda em inglês.", "How many?") + (if (m <= 12) listOf(en("How many?")) else emptyList()),
            cena = Cena.Emojis(obj.emoji.repeat(n)),
            certa = op(n),
            erradas = vizinhos(n, (1..maxOf(faixa.last + 2, 6)).toList()).map(op),
            reforco = reforco(m, maiuscula(I.numeroEn(n)) + "!", "${porExtenso(n)}."),
        )
    }

    /** Escolhe a cor certa (círculo colorido) da coisa mostrada: "What color is it?". */
    private fun qualCor(m: Int): Pergunta {
        val (emoji, nome) = V.coisasColoridas.um()
        val certa = I.cores.first { it.en == nome }
        val erradas = I.cores.filter { it.en != nome && it.en != "white" }.sorteia(3)
        return montar(
            instrucao = "What color is it?",
            fala = listOf(en("What color is it?")) + (if (m <= 18) listOf(pt("Que cor é esta?")) else emptyList()),
            cena = Cena.Emojis(emoji),
            certa = opCor(certa),
            erradas = erradas.map(::opCor),
            reforco = reforco(m, "It is ${certa.en}!", "É ${certa.pt}."),
        )
    }

    // ------------------------------------------------------------------ desafios de mexer

    private val coresDosBotoes = listOf(0xFFE53935, 0xFF1E88E5, 0xFFFDD835, 0xFF43A047)

    /** Pescaria: pesque os peixinhos que levam a figura pedida (um ou dois iguais). */
    private fun pescarPalavra(m: Int, lista: List<PalavraEn>): Pergunta {
        val alvo = lista.um()
        val iguais = if (m >= 8) (1..2).random(random) else 2
        val resto = outras(lista, listOf(alvo), (if (m >= 9) 6 else 5) - iguais)
        val peixes = (List(iguais) { alvo } + resto).shuffled(random)
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Pesque: ${alvo.en}", "Fish: ${alvo.en}"),
            fala = pedir(m, "Pesque os peixinhos que levam:", "Fish:") + en(alvo.en),
            atividade = Pescaria(peixes.map(::opFig), peixes.indices.filter { peixes[it] == alvo }.toSet()),
            reforco = reforco(m, maiuscula(alvo.en) + "!", "Em português, ${alvo.pt}."),
        )
    }

    /** Pescaria por grupo: só os do [grupo] (os bichos da fazenda, as roupas de frio...). */
    private fun pescarGrupo(m: Int, grupo: List<PalavraEn>, fora: List<PalavraEn>, ptx: String, enx: String): Pergunta {
        val certos = distintas(grupo, (2..3).random(random))
        val errados = outras(fora, certos, 3)
        val peixes = (certos + errados).shuffled(random)
        return Pergunta.deAtividade(
            instrucao = rotulo(m, ptx, enx),
            fala = pedir(m, ptx, enx) + (if (m <= 12) listOf(en(enx)) else emptyList()),
            atividade = Pescaria(peixes.map(::opFig), peixes.indices.filter { peixes[it] in certos }.toSet()),
            reforco = listOf(en(certos.joinToString(", ") { it.en } + "!")),
        )
    }

    /** Ligar pares: a palavra em inglês (à esquerda) com a figura (à direita), com uma figura sobrando a partir do mundo 10. */
    private fun ligarPalavras(m: Int, lista: List<PalavraEn>, n: Int): Pergunta {
        val itens = distintas(lista, n)
        val sobra = if (m >= 10) outras(lista, itens, 1) else emptyList()
        return ligar(m, itens.map(::opPal), itens.map(::opFig), sobra.map(::opFig),
            "Ligue cada palavra à figura", "Match the words and the pictures")
    }

    /** Ligar pares genérico: [esquerda][i] combina com [direita][i]; [sobras] entram embaralhadas à direita. */
    private fun ligar(m: Int, esquerda: List<Opcao>, direita: List<Opcao>, sobras: List<Opcao>, ptx: String, enx: String): Pergunta {
        val ordem = (direita + sobras).indices.shuffled(random)
        val todas = direita + sobras
        val dir = ordem.map { todas[it] }
        val pares = esquerda.indices.associateWith { i -> ordem.indexOf(i) }
        return Pergunta.deAtividade(
            instrucao = rotulo(m, ptx, enx),
            fala = pedir(m, "$ptx. Arraste o dedo de um lado até o outro.", "$enx."),
            atividade = LigarPares(esquerda, dir, pares),
            reforco = listOf(en("Well done!")) + (if (m <= 18) listOf(pt("Ligou tudo certinho!")) else emptyList()),
        )
    }

    /** Estourar balões: estoure o balão (ou os dois balões) com o que o narrador falou. */
    private fun baloesPalavra(m: Int, lista: List<PalavraEn>, escrito: Boolean = false, cena: Cena = Cena.Nenhuma): Pergunta {
        val alvos = distintas(lista, if (m >= 14) (1..2).random(random) else 1)
        val resto = outras(lista, alvos, 5 - alvos.size)
        val baloes = (alvos + resto).shuffled(random)
        val nomes = alvos.joinToString(" and ") { it.en }
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Estoure: $nomes", "Pop: $nomes"),
            fala = pedir(m, "Estoure o balão:", "Pop the balloon:") + en(nomes),
            atividade = EstourarBaloes(baloes.map { if (escrito) opPal(it) else opFig(it) }, alvos.map { baloes.indexOf(it) }),
            reforco = listOf(en("Pop! $nomes!")),
            cena = cena,
        )
    }

    /** Estourar balões na ordem pedida (números, letras de uma palavra). */
    private fun baloesEmOrdem(m: Int, emOrdem: List<Opcao>, extras: List<Opcao>, ptx: String, enx: String, falaDaOrdem: String): Pergunta {
        val baloes = (emOrdem + extras).shuffled(random)
        return Pergunta.deAtividade(
            instrucao = rotulo(m, ptx, enx),
            fala = pedir(m, ptx, enx) + en(falaDaOrdem),
            atividade = EstourarBaloes(baloes, emOrdem.map { baloes.indexOf(it) }, emOrdem = true),
            reforco = listOf(en(falaDaOrdem + "!")),
        )
    }

    /** Siga a sequência: botões com figura e palavra em inglês; o jogo toca a sequência e a criança repete. */
    private fun siga(m: Int, botoes: List<Pair<String, String>>, tamanho: IntRange, ptx: String, enx: String, prefixo: String? = null): Pergunta {
        val usados = botoes.distinctBy { it.first }.sorteia(if (m >= 14) 4 else 3)
        val n = tamanho.random(random)
        val seq = mutableListOf<Int>()
        while (seq.size < n) {
            val k = usados.indices.random(random)
            if (seq.lastOrNull() != k) seq += k
        }
        val falaSeq = seq.joinToString(", ") { usados[it].second }
        return Pergunta.deAtividade(
            instrucao = rotulo(m, ptx, enx),
            fala = pedir(m, ptx, enx) + listOfNotNull(prefixo?.let { en(it) }) + en(falaSeq),
            atividade = SigaASequencia(usados.map { (e, f) -> Opcao(Visual.Emoji(e), listOf(en(f))) }, coresDosBotoes.take(usados.size), seq),
            reforco = listOf(en(maiuscula(falaSeq) + "!")),
        )
    }

    private fun sigaPalavras(m: Int, lista: List<PalavraEn>, tamanho: IntRange) =
        siga(m, lista.map { it.emoji to it.en }, tamanho, "Escute e repita a sequência", "Listen and repeat")

    /** Caça na cena: espalha as figuras pela cena do mundo; a criança acha as certas. */
    private fun caca(m: Int, certos: List<Opcao>, errados: List<Opcao>, ptx: String, enx: String, falaAlvo: String, reforcoEn: String): Pergunta {
        val todos = (certos.map { it to true } + errados.map { it to false }).shuffled(random)
        val casas = (0 until 12).shuffled(random)
        val itens = todos.mapIndexed { i, (op, _) ->
            val c = casas[i]
            val x = ((c % 4) + 0.5f) / 4f + (random.nextFloat() - 0.5f) * 0.08f
            val y = 0.2f + (c / 4) * 0.3f + (random.nextFloat() - 0.5f) * 0.08f
            val escala = if (m >= 19) 0.7f + random.nextFloat() * 0.5f else 0.9f + random.nextFloat() * 0.3f
            ItemNaCena(op, x.coerceIn(0.05f, 0.95f), y.coerceIn(0.05f, 0.95f), escala)
        }
        return Pergunta.deAtividade(
            instrucao = rotulo(m, ptx, enx),
            fala = pedir(m, ptx, enx) + en(falaAlvo),
            atividade = CacaNaCena(m - 1, itens, todos.indices.filter { todos[it].second }.toSet()),
            reforco = listOf(en(reforcoEn)),
        )
    }

    /** Caça: ache a figura pedida (uma, duas ou três iguais escondidas entre outras). */
    private fun cacaPalavra(m: Int, lista: List<PalavraEn>, contar: Boolean = m >= 10): Pergunta {
        val alvo = lista.um()
        val k = if (contar) (2..3).random(random) else 1
        val resto = outras(lista, listOf(alvo), if (m >= 13) 7 else 5)
        val nome = if (k == 1) "the ${alvo.en}" else "${I.numeroEn(k)}: ${alvo.en}"
        return caca(m, List(k) { opFig(alvo) }, resto.map(::opFig), "Ache na cena", "Find", nome, "You found ${if (k == 1) "the ${alvo.en}" else "${I.numeroEn(k)}"}!")
    }

    /** Caça por grupo: ache todas as coisas de um grupo (as roupas de frio, os bichos do mar...). */
    private fun cacaGrupo(m: Int, grupo: List<PalavraEn>, fora: List<PalavraEn>, ptx: String, enx: String, nomeEn: String): Pergunta {
        val certos = distintas(grupo, (2..3).random(random))
        val errados = outras(fora, certos, 5)
        return caca(m, certos.map(::opFig), errados.map(::opFig), ptx, enx, nomeEn, certos.joinToString(", ") { it.en } + "!")
    }

    /** Montar a frase em inglês: cartões embaralhados e um ou dois cartões a mais. */
    private fun montarFrase(m: Int, frases: List<FraseEn>, comAudio: Boolean = true): Pergunta {
        val f = frases.um()
        val palavras = f.en.split(" ")
        val candidatos = frases.flatMap { it.en.split(" ") }.map { it.lowercase() }.distinct()
            .filter { c -> palavras.none { it.trimEnd('?', '.').equals(c.trimEnd('?', '.'), ignoreCase = true) } }
        val extras = candidatos.sorteia(if (m >= 13) 2 else 1)
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Monte a frase", "Make the sentence"),
            fala = if (comAudio) pedir(m, "Escute e monte a frase em inglês:", "Listen and make the sentence:") + en(f.en + ".")
            else pedir(m, "Olhe a figura e monte a frase em inglês.", "Look at the picture and make the sentence."),
            atividade = MontarFrase(palavras, extras, Idioma.EN, f.emoji),
            reforco = reforco(m, f.en + "!", f.pt + "."),
        )
    }

    /** Ditado em inglês: escuta a palavra e monta com as letras ([comFigura]: a figura ajuda). */
    private fun ditado(m: Int, lista: List<PalavraEn>, comFigura: Boolean): Pergunta {
        val p = lista.filter { ' ' !in it.en && '-' !in it.en }.um()
        val letras = p.en.map { it.toString() }
        val extras = ('a'..'z').map { it.toString() }.filter { it !in p.en.lowercase() }.sorteia(if (m >= 20) 3 else 2)
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Escute e monte a palavra", "Listen and spell"),
            fala = pedir(m, "Escute e monte a palavra em inglês:", "Listen and spell:") + en(p.en),
            atividade = Ditado(letras, extras, Idioma.EN, if (comFigura) p.emoji else null),
            reforco = listOf(en(p.en.uppercase().toList().joinToString(", ") + ". " + maiuscula(p.en) + "!")) +
                (if (m <= 22) listOf(pt(maiuscula(p.pt) + ".")) else emptyList()),
        )
    }

    /**
     * Dominó da palavra e da figura: a ponta mostra uma figura; cada peça tem a palavra dessa figura de
     * um lado e a próxima figura do outro. As peças que sobram não combinam com nenhuma ponta.
     */
    private fun dominoPalavras(m: Int, lista: List<PalavraEn>, jogadas: Int): Pergunta {
        val sobrando = if (m >= 15) 2 else 1
        val usadas = distintas(lista, jogadas + 1 + 2 * sobrando)
        val cadeia = usadas.take(jogadas + 1)
        val soltas = usadas.drop(jogadas + 1)
        val fig = { p: PalavraEn -> MeiaPeca(p.en, Visual.Emoji(p.emoji), listOf(en(p.en))) }
        val txt = { p: PalavraEn -> MeiaPeca(p.en, Visual.Texto(p.en), listOf(en(p.en))) }
        val pecas = (0 until jogadas).map { PecaDomino(txt(cadeia[it]), fig(cadeia[it + 1])) } +
            (0 until sobrando).map { PecaDomino(txt(soltas[2 * it]), fig(soltas[2 * it + 1])) }
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Dominó das palavras", "Word dominoes"),
            fala = pedir(m, "Dominó! Encaixe a palavra em inglês que combina com a figura da ponta.", "Dominoes! Match the word and the picture."),
            atividade = Domino(fig(cadeia[0]), pecas.shuffled(random), jogadas),
            reforco = listOf(en(cadeia.joinToString(", ") { it.en } + "!")),
        )
    }

    /** Esconde esconde com a pista falada em inglês ("behind the red box"). */
    private fun esconde(m: Int, esconderijos: List<Opcao>, certo: Int, pista: String, bicho: String = listOf("🐰", "🐱", "🐶", "🐹", "🐼").um()): Pergunta =
        Pergunta.deAtividade(
            instrucao = rotulo(m, "Onde ele se escondeu?", "Where is it?"),
            fala = pedir(m, "O bichinho se escondeu! Escute a pista:", "Where is it? Listen:") + en(pista),
            atividade = EscondeEsconde(bicho, esconderijos, certo),
            reforco = listOf(en(pista)),
        )

    /** Cartas viradas: vire as cartas na ordem ([emOrdem]); [extras] não entram na ordem. */
    private fun cartas(m: Int, emOrdem: List<Opcao>, extras: List<Opcao>, ptx: String, enx: String, falaDaOrdem: String): Pergunta {
        val cartas = (emOrdem + extras).shuffled(random)
        return Pergunta.deAtividade(
            instrucao = rotulo(m, ptx, enx),
            fala = pedir(m, ptx, enx) + en(falaDaOrdem),
            atividade = CartasViradas(cartas, emOrdem.map { cartas.indexOf(it) }),
            reforco = listOf(en(falaDaOrdem + "!")),
        )
    }

    /** Trem: os vagões na ordem, com [fixos] vagões já engatados como pista. */
    private fun trem(m: Int, emOrdem: List<Opcao>, extras: List<Opcao>, fixos: Int, ptx: String, enx: String, falaDaOrdem: String): Pergunta =
        Pergunta.deAtividade(
            instrucao = rotulo(m, ptx, enx),
            fala = pedir(m, ptx, enx) + en(falaDaOrdem),
            atividade = Trem(emOrdem, emOrdem.indices.shuffled(random).take(fixos).toSet(), extras),
            reforco = listOf(en(falaDaOrdem + "!")),
        )

    // ------------------------------------------------------------------ pintar por código

    /** Desenhos para pintar: cada letra é uma cor, o ponto fica branco (no máximo [PintarPorCodigo.MAX_COLUNAS] colunas). */
    private val desenhosDePintar = listOf(
        listOf(".aaa..", "aaaaab", "acaabb", "aaaaab", ".aaa.."), // peixe
        listOf("..a..", ".aaa.", "aaaaa", "bbcbb", "bbcbb"), // casa
        listOf(".a.a.", "aabaa", ".a.a.", "..c..", ".cc..", "..c.."), // flor
        listOf("..a...", ".aa...", "aaa...", "..b...", "cccccc", ".cccc."), // barco
        listOf("aa..aa", "aaaaaa", "aaaaaa", ".aaaa.", "..aa.."), // coração
        listOf("..aa..", ".aaaa.", "aaaaaa", "..bb..", "..bb..", "..bb.."), // árvore
    )

    private fun casasDe(desenho: List<String>, codigoDe: (Char) -> String?): List<CasaDePintar> =
        desenho.flatMap { linha -> linha.map { ch -> codigoDe(ch).let { CasaDePintar(it ?: "", it) } } }

    /** Pintar por código com o nome da cor em inglês escrito em cada casinha ("red", "blue"). */
    private fun pintarCores(m: Int): Pergunta {
        val desenho = desenhosDePintar.um()
        val letras = desenho.joinToString("").filter { it != '.' }.toSet().toList()
        val cores = I.cores.filter { it.en != "white" }.sorteia(letras.size + if (m >= 7) 1 else 0)
        val corDa = letras.indices.associate { letras[it] to cores[it] }
        val legenda = cores.map { CorDaLegenda(it.en, it.cor ?: 0xFF000000, listOf(en(it.en))) }
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Pinte pelo nome da cor", "Paint by color"),
            fala = pedir(m, "Cada casinha tem o nome de uma cor em inglês. Toque na tinta e pinte!", "Read the colors and paint!"),
            atividade = PintarPorCodigo(desenho[0].length, casasDe(desenho) { ch -> corDa[ch]?.en }, legenda.shuffled(random)),
            reforco = listOf(en(cores.take(letras.size).joinToString(", ") { it.en } + "! Beautiful!")),
        )
    }

    /** Pintar o rosto: cada casinha tem uma parte do rosto em inglês e a legenda diz a cor de cada parte. */
    private fun pintarRosto(m: Int): Pergunta {
        val desenho = listOf("hhhhhh", "hffffh", "feffef", "ffnnff", "fmmmmf", "ffffff")
        val partes = mapOf('h' to "hair", 'f' to "face", 'e' to "eyes", 'n' to "nose", 'm' to "mouth")
        val cabelo = listOf(0xFF795548, 0xFF212121, 0xFFFDD835, 0xFFFB8C00).um()
        val cores = mapOf("hair" to cabelo, "face" to 0xFFFFCC80, "eyes" to 0xFF1E88E5, "nose" to 0xFFF48FB1, "mouth" to 0xFFE53935)
        val legenda = partes.values.map { CorDaLegenda(it, cores.getValue(it), listOf(en(it))) }.shuffled(random)
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Pinte o rosto", "Paint the face"),
            fala = pedir(m, "Pinte o rosto! Cada casinha diz uma parte: hair, face, eyes, nose, mouth.", "Paint the face: hair, face, eyes, nose, mouth."),
            atividade = PintarPorCodigo(6, casasDe(desenho) { partes[it] }, legenda),
            reforco = listOf(en("Hair, eyes, nose, mouth. What a nice face!")),
        )
    }

    /** Pintar o leão da savana com as cores em inglês. */
    private fun pintarLeao(m: Int): Pergunta {
        val desenho = listOf(".bbbb.", "byyyyb", "bkyykb", "byyyyb", "bbkkbb", ".bbbb.")
        val nomes = mapOf('b' to "brown", 'y' to "yellow", 'k' to "black")
        val usadas = nomes.values.map { n -> I.cores.first { it.en == n } }
        val extra = I.cores.filter { it.en !in nomes.values && it.en != "white" }.um()
        val legenda = (usadas + extra).map { CorDaLegenda(it.en, it.cor ?: 0xFF000000, listOf(en(it.en))) }.shuffled(random)
        return Pergunta.deAtividade(
            instrucao = "Paint the lion",
            fala = listOf(en("Paint the lion! Brown, yellow and black.")),
            atividade = PintarPorCodigo(desenho[0].length, casasDe(desenho) { nomes[it] }, legenda),
            reforco = listOf(en("A big yellow lion! Roar!")),
        )
    }

    // ------------------------------------------------------------------ outros desafios

    /** Jogo da memória com o nome de cada figura falado em inglês. */
    private fun memoriaEn(m: Int, lista: List<PalavraEn>, pares: Int): Pergunta {
        val escolhidos = distintas(lista, pares)
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Ache os pares", "Find the pairs"),
            fala = pedir(m, "Jogo da memória! Vire duas cartas, ache as iguais e escute o nome em inglês.", "Memory game! Find the pairs."),
            atividade = Atividade.Memoria(escolhidos.map { it.emoji to it.emoji }, escolhidos.associate { it.emoji to en(it.en) }),
            reforco = listOf(en(escolhidos.joinToString(", ") { it.en } + "!")),
        )
    }

    /** Programar o robô até a bateria, com as setas ditas em inglês (up, down, left, right). */
    private fun novoRobo(m: Int): ProgramarRobo {
        while (true) {
            val linhas = (3..4).random(random)
            val colunas = (3..4).random(random)
            val total = linhas * colunas
            val inicio = random.nextInt(total)
            val chegada = random.nextInt(total)
            val pedras = (0 until total).filter { it != inicio && it != chegada }.sorteia(if (m >= 20) 2 else (0..1).random(random)).toSet()
            val robo = runCatching { ProgramarRobo(linhas, colunas, inicio, chegada, pedras, maxComandos = 6) }.getOrNull()
            if (robo != null && robo.menorCaminho in 2..5) return robo
        }
    }

    private fun robo(m: Int): Pergunta {
        val lugar = if (m <= 12) I.lugares.um() else null
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Programe o robô", "Program the robot"),
            fala = if (lugar != null) listOf(pt("A bateria é o caminho até ${lugar.pt}. Monte as setas e toque no play."), en("Go to the ${lugar.en}! Up, down, left, right."))
            else pedir(m, "Monte as setas e leve o robô até a bateria.", "Go to the battery! Up, down, left, right."),
            atividade = novoRobo(m),
            reforco = listOf(en("Beep beep! Well done!")),
        )
    }

    /** Mercadinho: o preço é falado em inglês ("The cake costs five reais") e a criança paga o valor exato. */
    private fun mercado(m: Int): Pergunta {
        val p = I.doces.um()
        val reais = (2..9).random(random)
        val notas = mutableListOf<Int>()
        var falta = reais
        for (v in listOf(5, 2, 1)) while (falta >= v) { notas += v * 100; falta -= v }
        repeat((1..3).random(random)) { notas += listOf(100, 200, 500).um() }
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Pague: $reais reais", "Pay $reais reais"),
            fala = pedir(m, "Escute o preço em inglês e pague certinho.", "Listen and pay.") + en("The ${p.en} costs ${I.numeroEn(reais)} reais."),
            atividade = Mercadinho(p.emoji, p.en, reais * 100, notas.shuffled(random)),
            reforco = listOf(en("${maiuscula(I.numeroEn(reais))} reais. Thank you!")),
        )
    }

    /** Cesta: "Put three balls in the basket" (número com plural). */
    private fun cestaPlural(m: Int): Pergunta {
        val t = I.brinquedos.um()
        val n = (2..5).random(random)
        val frase = "Put ${I.numeroEn(n)} ${I.plural(t.en)} in the basket."
        return Pergunta.deAtividade(
            instrucao = "Put $n ${I.plural(t.en)} in the basket",
            fala = listOf(en(frase), en("Then touch the check mark.")),
            atividade = Atividade.Cesta(List(n + (2..3).random(random)) { t.emoji }, n),
            reforco = listOf(en("${maiuscula(I.numeroEn(n))} ${I.plural(t.en)}!")),
        )
    }

    /** Quebra cabeça do rosto: monta o rosto e escuta as partes em inglês. */
    private fun quebraRosto(m: Int): Pergunta {
        val rosto = listOf("😀", "🧒", "👧", "👦", "👵", "🐻", "🐱", "🐶").um()
        val (linhas, colunas) = if (m >= 17 && random.nextBoolean()) 2 to 3 else 2 to 2
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Monte o rosto", "Make the face"),
            fala = pedir(m, "Monte o rosto arrastando as peças.", "Make the face!"),
            atividade = QuebraCabeca(rosto, linhas, colunas),
            reforco = listOf(en("Eyes, nose, mouth. A happy face!")),
        )
    }

    // ================================================================== MUNDO 6: FAZENDA (51 a 60)

    private val itIsA: List<FraseEn> by lazy { I.fazenda.map { FraseEn("It is a ${it.en}", "É ${if (it.pt.endsWith("a")) "uma" else "um"} ${it.pt}", it.emoji) } }

    private fun somDoBicho(m: Int): Pergunta {
        val s = I.sonsDaFazenda.um()
        val erradas = I.sonsDaFazenda.filter { it != s }.sorteia(3)
        return montar(
            instrucao = rotulo(m, "Quem faz ${s.som}?", "Who says ${s.som}?"),
            fala = pedir(m, "Quem faz este som em inglês?", "Who says") + en(maiuscula(s.som) + "!"),
            cena = Cena.Palavra(s.som),
            certa = opFig(s.bicho),
            erradas = erradas.map { opFig(it.bicho) },
            reforco = reforco(m, "The ${s.bicho.en} says ${s.som}!", "Em inglês, ${s.bicho.pt} faz ${s.som}."),
        )
    }

    private fun oQueEleDiz(m: Int): Pergunta {
        val s = I.sonsDaFazenda.um()
        val erradas = I.sonsDaFazenda.filter { it != s }.sorteia(3)
        return montar(
            instrucao = rotulo(m, "O que ele diz?", "What does it say?"),
            fala = pedir(m, "O que este bicho diz em inglês?", "What does it say?"),
            cena = Cena.Emojis(s.bicho.emoji),
            certa = opTxt(s.som),
            erradas = erradas.map { opTxt(it.som) },
            reforco = reforco(m, "The ${s.bicho.en} says ${s.som}!", "Em inglês, ${s.bicho.pt} faz ${s.som}."),
        )
    }

    private fun sigaSons(m: Int, tamanho: IntRange) =
        siga(m, I.sonsDaFazenda.map { it.bicho.emoji to it.som }, tamanho, "Escute os bichos e repita a sequência", "Listen to the animals and repeat")

    private fun mundo6(): List<ModeloDeFase> {
        val m = 6
        val f = I.fazenda
        return listOf(
            ModeloDeFase("Farm animals", "🐄") { ou({ ouvirETocar(m, f) }, { ouvirETocar(m, f) }, { memoriaEn(m, f, 3) }, { ligarPalavras(m, f, 2) }) },
            ModeloDeFase("Farm fishing", "🎣") { ou({ pescarPalavra(m, f) }, { pescarPalavra(m, f) }, { ouvirETocar(m, f) }) },
            ModeloDeFase("Who says moo?", "🔊") { ou({ somDoBicho(m) }, { somDoBicho(m) }, { sigaSons(m, 2..2) }) },
            ModeloDeFase("Animal sounds", "🎵") { ou({ sigaSons(m, 2..3) }, { sigaSons(m, 2..3) }, { oQueEleDiz(m) }) },
            ModeloDeFase("Farm party", "🚩") { ou({ ouvirETocar(m, f) }, { pescarPalavra(m, f) }, { somDoBicho(m) }, { sigaSons(m, 2..3) }) },
            ModeloDeFase("It is a cow", "🐮") { ou({ fraseFigura(m, itIsA) }, { fraseFigura(m, itIsA) }, { ligarPalavras(m, f, 2) }) },
            ModeloDeFase("Match the farm", "🔗") { ou({ ligarPalavras(m, f, 3) }, { ligarPalavras(m, f, 3) }, { fraseFigura(m, itIsA) }) },
            ModeloDeFase("Farm and space", "🌟") {
                ou({ pescarGrupo(m, f, I.mar + V.animaisSelvagens, "Pesque só os bichos da fazenda", "Farm animals") },
                    { ouvirETocar(m, V.opostos) }, { numeroOuvido(m, (1..20).toList(), (1..20).toList()) }, { lerONome(m, f) })
            },
            ModeloDeFase("Farm expert", "⭐") { ou({ lerONome(m, f) }, { oQueEleDiz(m) }, { sigaSons(m, 3..4) }) },
            ModeloDeFase("Boss: Big Cow", "👑") { ou({ sigaSons(m, 3..4) }, { pescarPalavra(m, f) }, { somDoBicho(m) }, { lerONome(m, f) }) },
        )
    }

    // ================================================================== MUNDO 7: FUNDO DO MAR (61 a 70)

    private val bichoColorido: List<FraseEn> by lazy { I.marColorido.map { FraseEn(it.en, it.pt, it.bicho.emoji) } }

    /** Pesque os bichos do mar de uma cor ("Fish the blue ones"). */
    private fun pescarCor(m: Int): Pergunta {
        var peixes: List<VocabularioIngles.BichoColorido>
        var cor: PalavraEn
        do {
            peixes = I.marColorido.distinctBy { it.bicho.emoji }.sorteia(5)
            cor = peixes.um().cor
        } while (peixes.all { it.cor == cor })
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Pesque os ${cor.en}", "Fish the ${cor.en} ones"),
            fala = pedir(m, "Pesque os bichos desta cor:", "Fish the") + en(if (m <= 12) cor.en else "${cor.en} ones!"),
            atividade = Pescaria(peixes.map { opEmojiEn(it.bicho.emoji, it.en) }, peixes.indices.filter { peixes[it].cor == cor }.toSet()),
            reforco = reforco(m, maiuscula(cor.en) + "!", "${maiuscula(cor.pt)}!"),
        )
    }

    private fun opEmojiEn(emoji: String, fala: String) = Opcao(Visual.Emoji(emoji), listOf(en(fala)))

    /** "A blue whale": escuta a cor e o bicho e toca na figura. */
    private fun bichoDaCor(m: Int): Pergunta {
        val certo = I.marColorido.um()
        val erradas = I.marColorido.filter { it.bicho.emoji != certo.bicho.emoji }.sorteia(3)
        return montar(
            instrucao = rotulo(m, "Encontre o bicho", "Find the animal"),
            fala = pedir(m, "Encontre:", "Find") + en(certo.en),
            cena = if (m <= 9) Cena.Palavra(certo.en) else Cena.Nenhuma,
            certa = opEmojiEn(certo.bicho.emoji, certo.en),
            erradas = erradas.map { opEmojiEn(it.bicho.emoji, it.en) },
            reforco = reforco(m, maiuscula(certo.en) + "!", maiuscula(certo.pt) + "."),
        )
    }

    private fun mundo7(): List<ModeloDeFase> {
        val m = 7
        val mar = I.mar
        return listOf(
            ModeloDeFase("Sea animals", "🐠") { ou({ ouvirETocar(m, mar) }, { ouvirETocar(m, mar) }, { pescarPalavra(m, mar) }) },
            ModeloDeFase("Sea fishing", "🎣") { ou({ pescarPalavra(m, mar) }, { pescarPalavra(m, mar) }, { lerONome(m, mar) }) },
            ModeloDeFase("A blue fish", "🐟") { ou({ bichoDaCor(m) }, { bichoDaCor(m) }, { pescarCor(m) }) },
            ModeloDeFase("Paint the sea", "🎨") { ou({ pintarCores(m) }, { pintarCores(m) }, { qualCor(m) }) },
            ModeloDeFase("Sea party", "🚩") { ou({ ouvirETocar(m, mar) }, { pescarPalavra(m, mar) }, { bichoDaCor(m) }, { pintarCores(m) }) },
            ModeloDeFase("Fish the colors", "🌊") { ou({ pescarCor(m) }, { pescarCor(m) }, { fraseFigura(m, bichoColorido) }) },
            ModeloDeFase("Sea dominoes", "🎲") { ou({ dominoPalavras(m, mar, 2) }, { dominoPalavras(m, mar, 2) }, { figuraFrase(m, bichoColorido) }) },
            ModeloDeFase("Sea and farm", "🐚") { ou({ pescarGrupo(m, mar, I.fazenda, "Pesque só os bichos do mar", "Sea animals") }, { somDoBicho(m) }, { bichoDaCor(m) }, { ligarPalavras(m, I.fazenda, 3) }) },
            ModeloDeFase("Deep sea", "🦑") { ou({ lerONome(m, mar) }, { figuraFrase(m, bichoColorido) }, { dominoPalavras(m, mar, 3) }) },
            ModeloDeFase("Boss: Octopus", "👑") { ou({ pintarCores(m) }, { pescarCor(m) }, { dominoPalavras(m, mar, 3) }, { bichoDaCor(m) }) },
        )
    }

    // ================================================================== MUNDO 8: SELVA (71 a 80)

    /** A mesma figura em 4 tamanhos: toque no grande (big) ou no pequeno (small). */
    private fun tamanho(m: Int): Pergunta {
        val e = listOf("🐘", "🐒", "🦜", "🐯", "🌴", "🐸", "🦍").um()
        val escalas = listOf(0.55f, 0.85f, 1.15f, 1.5f)
        val grande = random.nextBoolean()
        val op = { s: Float -> Opcao(Visual.Emoji(e, s), when (s) { 1.5f -> listOf(en("big")); 0.55f -> listOf(en("small")); else -> emptyList() }) }
        val certa = if (grande) 1.5f else 0.55f
        val palavra = if (grande) "big" else "small"
        return montar(
            instrucao = rotulo(m, "Toque no $palavra", "Touch the $palavra one"),
            fala = pedir(m, "Toque no", "Touch the") + en(if (m <= 12) palavra else "$palavra one"),
            cena = Cena.Palavra(palavra),
            certa = op(certa),
            erradas = escalas.filter { it != certa }.map(op),
            reforco = reforco(m, maiuscula(palavra) + "!", if (grande) "Grande!" else "Pequeno!"),
        )
    }

    /** Esconde esconde atrás da árvore ou da pedra grande ou pequena. */
    private fun escondeTamanho(m: Int): Pergunta {
        val lugares = listOf(Triple("🌳", "tree", 1.5f), Triple("🌳", "tree", 0.7f), Triple("🪨", "rock", 1.4f), Triple("🪨", "rock", 0.7f), Triple("🌵", "cactus", 1.4f), Triple("🌵", "cactus", 0.7f))
        val usados = lugares.take(4 + if (m >= 11) 2 else 0).shuffled(random)
        val certo = usados.indices.random(random)
        val (_, nome, escala) = usados[certo]
        val tam = if (escala > 1f) "big" else "small"
        return esconde(m, usados.map { (e, n, s) -> Opcao(Visual.Emoji(e, s), listOf(en("the ${if (s > 1f) "big" else "small"} $n"))) }, certo,
            "It is behind the $tam $nome.", listOf("🐒", "🐯", "🦜", "🐸").um())
    }

    private fun oposto(m: Int): Pergunta {
        val o = I.opostos.um()
        val (pergunta, resposta) = if (random.nextBoolean()) o.a to o.b else o.b to o.a
        val erradas = I.opostos.filter { it != o }.flatMap { listOf(it.a, it.b) }.sorteia(3)
        return montar(
            instrucao = rotulo(m, "O contrário de ${pergunta.en}", "The opposite of ${pergunta.en}"),
            fala = pedir(m, "Qual é o contrário de", "What is the opposite of") + en(pergunta.en + "?"),
            cena = Cena.Emojis(pergunta.emoji),
            certa = opPal(resposta),
            erradas = erradas.map(::opPal),
            reforco = reforco(m, "${maiuscula(pergunta.en)} and ${resposta.en}!", "${maiuscula(pergunta.pt)} e ${resposta.pt}."),
        )
    }

    private fun ligarOpostos(m: Int): Pergunta {
        val ops = I.opostos.sorteia(3)
        return ligar(m, ops.map { opPal(it.a) }, ops.map { opPal(it.b) }, emptyList(), "Ligue os contrários", "Match the opposites")
    }

    private fun mundo8(): List<ModeloDeFase> {
        val m = 8
        val adj = I.frasesDaSelva
        return listOf(
            ModeloDeFase("Big and small", "🐘") { ou({ tamanho(m) }, { tamanho(m) }, { baloesPalavra(m, I.selva) }) },
            ModeloDeFase("Hide and seek", "🌳") { ou({ escondeTamanho(m) }, { escondeTamanho(m) }, { tamanho(m) }) },
            ModeloDeFase("A long snake", "🐍") { ou({ fraseFigura(m, adj) }, { fraseFigura(m, adj) }, { escondeTamanho(m) }) },
            ModeloDeFase("Jungle balloons", "🎈") { ou({ baloesPalavra(m, I.selva) }, { baloesPalavra(m, I.selva) }, { ouvirETocar(m, I.selva) }) },
            ModeloDeFase("Jungle party", "🚩") { ou({ tamanho(m) }, { escondeTamanho(m) }, { fraseFigura(m, adj) }, { baloesPalavra(m, I.selva) }) },
            ModeloDeFase("Opposites", "↔️") { ou({ oposto(m) }, { oposto(m) }, { baloesPalavra(m, I.opostos.flatMap { listOf(it.a, it.b) }, escrito = true) }) },
            ModeloDeFase("Match the opposites", "🔗") { ou({ ligarOpostos(m) }, { ligarOpostos(m) }, { figuraFrase(m, adj) }) },
            ModeloDeFase("Jungle and sea", "🐒") { ou({ ligarPalavras(m, I.selva, 3) }, { bichoDaCor(m) }, { pescarCor(m) }, { ouvirETocar(m, I.selva) }) },
            ModeloDeFase("Jungle expert", "⭐") { ou({ figuraFrase(m, adj) }, { oposto(m) }, { escondeTamanho(m) }, { lerONome(m, I.selva) }) },
            ModeloDeFase("Boss: Gorilla", "👑") { ou({ escondeTamanho(m) }, { baloesPalavra(m, I.selva) }, { tamanho(m) }, { oposto(m) }) },
        )
    }

    // ================================================================== MUNDO 9: POLO NORTE (81 a 90)

    private val euVisto: List<FraseEn> by lazy {
        I.roupas.map { r ->
            val artigo = if (r.en.endsWith("s")) "" else "a "
            FraseEn("I wear ${artigo}${r.en}", "Eu visto ${r.pt}", r.emoji)
        }
    }

    private fun mundo9(): List<ModeloDeFase> {
        val m = 9
        val roupas = I.roupas
        val frio = I.roupasDeFrio
        val calor = I.roupasDeCalor
        return listOf(
            ModeloDeFase("Winter clothes", "🧥") { ou({ ouvirETocar(m, roupas) }, { ouvirETocar(m, roupas) }, { memoriaEn(m, roupas, 3) }) },
            ModeloDeFase("Find the clothes", "🔎") { ou({ cacaPalavra(m, roupas) }, { cacaPalavra(m, roupas) }, { ouvirETocar(m, roupas) }) },
            ModeloDeFase("I wear a coat", "🧣") { ou({ fraseFigura(m, euVisto) }, { fraseFigura(m, euVisto) }, { cacaPalavra(m, roupas) }) },
            ModeloDeFase("Make the sentence", "🧩") { ou({ montarFrase(m, euVisto) }, { montarFrase(m, euVisto) }, { fraseFigura(m, euVisto) }) },
            ModeloDeFase("Snow party", "🚩") { ou({ ouvirETocar(m, roupas) }, { cacaPalavra(m, roupas) }, { fraseFigura(m, euVisto) }, { montarFrase(m, euVisto) }) },
            ModeloDeFase("Hot or cold?", "🥶") {
                ou({ cacaGrupo(m, frio, calor, "Ache as roupas de frio", "Find the clothes for the cold", "Cold!") },
                    { cacaGrupo(m, calor, frio, "Ache as roupas de calor", "Find the clothes for the heat", "Hot!") }, { lerONome(m, roupas) })
            },
            ModeloDeFase("Polar memory", "🐧") { ou({ memoriaEn(m, I.poloNorte, 4) }, { memoriaEn(m, I.poloNorte, 4) }, { ouvirETocar(m, I.poloNorte) }) },
            ModeloDeFase("Snow and jungle", "❄️") { ou({ ouvirETocar(m, I.poloNorte) }, { tamanho(m) }, { oposto(m) }, { montarFrase(m, euVisto) }) },
            ModeloDeFase("Polar expert", "⭐") { ou({ figuraFrase(m, euVisto) }, { lerONome(m, roupas) }, { cacaPalavra(m, I.poloNorte, contar = true) }) },
            ModeloDeFase("Boss: Polar Bear", "👑") { ou({ cacaPalavra(m, roupas) }, { montarFrase(m, euVisto) }, { fraseFigura(m, euVisto) }, { lerONome(m, roupas) }) },
        )
    }

    // ================================================================== MUNDO 10: CIRCO (91 a 100)

    private val dezenas = listOf(10, 20, 30, 40, 50)

    private fun idade(m: Int): Pergunta {
        val n = I.idades.random(random)
        val erradas = vizinhos(n, I.idades.toList())
        val op = { k: Int -> opTxt("I am ${I.numeroEn(k)}.") }
        return montar(
            instrucao = "How old are you?",
            fala = pedir(m, "Conte as velinhas e responda:", "Count the candles:") + en("How old are you?"),
            cena = Cena.Emojis("🎂" + "🕯️".repeat(n)),
            certa = op(n),
            erradas = erradas.map(op),
            reforco = reforco(m, "I am ${I.numeroEn(n)} years old!", "Eu tenho ${porExtenso(n)} anos."),
        )
    }

    private fun tremDezenas(m: Int, ate: Int): Pergunta {
        val todas = (1..ate / 10).map { it * 10 }
        val inicio = random.nextInt(todas.size - 3)
        val n = minOf(if (m >= 15) 5 else 4, todas.size - inicio)
        val emOrdem = todas.subList(inicio, inicio + n)
        val extra = emOrdem.first() + 5
        return trem(m, emOrdem.map(::opNum), listOf(opNum(extra)), 1, "Monte o trem de 10 em 10", "Count by tens", emOrdem.joinToString(", ") { I.numeroEn(it) })
    }

    private fun baloesNumeros(m: Int, faixa: IntRange): Pergunta {
        val inicio = (faixa.first..faixa.last - 2).random(random)
        val emOrdem = (inicio..inicio + 2).toList()
        val extras = (faixa.toList() - emOrdem.toSet()).sorteia(2)
        return baloesEmOrdem(m, emOrdem.map(::opNum), extras.map(::opNum), "Estoure os balões na ordem", "Pop the balloons in order", emOrdem.joinToString(", ") { I.numeroEn(it) })
    }

    private fun cartasDezenas(m: Int, ate: Int): Pergunta {
        val todas = (1..ate / 10).map { it * 10 }
        val n = minOf(4, todas.size)
        val inicio = random.nextInt(todas.size - n + 1)
        val emOrdem = todas.subList(inicio, inicio + n)
        val extras = listOf(emOrdem.first() + 5, emOrdem.last() - 5).distinct().take(if (m >= 15) 2 else 1)
        return cartas(m, emOrdem.map(::opNum), extras.map(::opNum), "Vire as cartas de 10 em 10", "Find the numbers in order", emOrdem.joinToString(", ") { I.numeroEn(it) })
    }

    private fun mundo10(): List<ModeloDeFase> {
        val m = 10
        val ate50n = (1..50).toList()
        return listOf(
            ModeloDeFase("Twenty to fifty", "🔢") { ou({ numeroOuvido(m, dezenas.drop(1), ate50n) }, { numeroOuvido(m, (20..50).toList(), ate50n) }, { baloesNumeros(m, 20..30) }) },
            ModeloDeFase("Circus balloons", "🎈") { ou({ baloesNumeros(m, 20..50) }, { baloesNumeros(m, 20..50) }, { numeroOuvido(m, (20..50).toList(), ate50n) }) },
            ModeloDeFase("Thirty or forty?", "🤹") { ou({ numeroOuvido(m, (21..49).toList(), (20..50).toList()) }, { lerNumero(m, dezenas, ate50n) }, { tremDezenas(m, 50) }) },
            ModeloDeFase("Circus train", "🚂") { ou({ tremDezenas(m, 50) }, { tremDezenas(m, 50) }, { lerNumero(m, dezenas, ate50n) }) },
            ModeloDeFase("Circus party", "🚩") { ou({ numeroOuvido(m, (20..50).toList(), ate50n) }, { baloesNumeros(m, 20..50) }, { tremDezenas(m, 50) }, { lerNumero(m, dezenas, ate50n) }) },
            ModeloDeFase("How old are you?", "🎂") { ou({ idade(m) }, { idade(m) }, { cartasDezenas(m, 50) }) },
            ModeloDeFase("Magic cards", "🃏") { ou({ cartasDezenas(m, 50) }, { cartasDezenas(m, 50) }, { idade(m) }) },
            ModeloDeFase("Circus and snow", "🎪") { ou({ ouvirETocar(m, I.circo) }, { fraseFigura(m, euVisto) }, { quantosEscrito(m, I.circo, 3..12) }, { baloesNumeros(m, 20..50) }) },
            ModeloDeFase("Number expert", "⭐") { ou({ lerNumero(m, (20..50).toList(), (20..50).toList()) }, { quantosEscrito(m, I.circo, 6..15) }, { idade(m) }, { cartasDezenas(m, 50) }) },
            ModeloDeFase("Boss: Clown", "👑") { ou({ tremDezenas(m, 50) }, { baloesNumeros(m, 20..50) }, { cartasDezenas(m, 50) }, { lerNumero(m, (20..50).toList(), (20..50).toList()) }) },
        )
    }

    // ================================================================== MUNDO 11: DESERTO (101 a 110)

    /** Esconde esconde com preposição: "The cat is under the bed". */
    private fun escondePreposicao(m: Int): Pergunta {
        val lugares = distintas(I.esconderijos, if (m >= 13) 4 else 3)
        val certo = lugares.indices.random(random)
        val prep = listOf("in", "under", "behind", "next to", "on").um()
        val (bicho, nome) = listOf("🐱" to "cat", "🐶" to "dog", "🐰" to "rabbit", "🦎" to "lizard").um()
        return esconde(m, lugares.map(::opFig), certo, "The $nome is $prep the ${lugares[certo].en}.", bicho)
    }

    private val frasesDeLugar: List<FraseEn> by lazy {
        listOf(
            FraseEn("The cat is in the box", "O gato está dentro da caixa", "🐱📦"),
            FraseEn("The ball is under the chair", "A bola está embaixo da cadeira", "⚽🪑"),
            FraseEn("The bird is on the tree", "O passarinho está na árvore", "🐦🌳"),
            FraseEn("The dog is next to the car", "O cachorro está ao lado do carro", "🐶🚗"),
            FraseEn("The lizard is on the rock", "O lagarto está em cima da pedra", "🦎🪨"),
            FraseEn("The camel is next to the cactus", "O camelo está ao lado do cacto", "🐫🌵"),
            FraseEn("The book is on the bed", "O livro está em cima da cama", "📕🛏️"),
        )
    }

    /** "Como se diz embaixo em inglês?": in, on, under, next to, behind. */
    private fun preposicao(m: Int) = comoSeDiz(m, I.preposicoes)

    /** Escuta "The ball is under the chair" e toca no lugar. */
    private fun ondeEsta(m: Int): Pergunta {
        val f = frasesDeLugar.um()
        val lugar = I.esconderijos.firstOrNull { f.emoji.endsWith(it.emoji) } ?: I.esconderijos.first()
        val erradas = outras(I.esconderijos, listOf(lugar), 3)
        return montar(
            instrucao = rotulo(m, "Onde está?", "Where is it?"),
            fala = pedir(m, "Escute e toque no lugar:", "Where is it? Listen:") + en(f.en + "."),
            cena = Cena.Nenhuma,
            certa = opFig(lugar),
            erradas = erradas.map(::opFig),
            reforco = reforco(m, f.en + "!", f.pt + "."),
        )
    }

    private fun mundo11(): List<ModeloDeFase> {
        val m = 11
        return listOf(
            ModeloDeFase("In, on, under", "📦") { ou({ preposicao(m) }, { preposicao(m) }, { escondePreposicao(m) }) },
            ModeloDeFase("Desert hide and seek", "🐫") { ou({ escondePreposicao(m) }, { escondePreposicao(m) }, { preposicao(m) }) },
            ModeloDeFase("Where is the ball?", "⚽") { ou({ ondeEsta(m) }, { ondeEsta(m) }, { escondePreposicao(m) }) },
            ModeloDeFase("Desert treasure hunt", "🔎") { ou({ cacaPalavra(m, I.deserto) }, { cacaPalavra(m, I.deserto) }, { ouvirETocar(m, I.deserto) }) },
            ModeloDeFase("Desert party", "🚩") { ou({ preposicao(m) }, { escondePreposicao(m) }, { ondeEsta(m) }, { cacaPalavra(m, I.deserto) }) },
            ModeloDeFase("The cat is in the box", "🐱") { ou({ fraseFigura(m, frasesDeLugar) }, { fraseFigura(m, frasesDeLugar) }, { cacaPalavra(m, I.deserto) }) },
            ModeloDeFase("Make the sentence", "🧩") { ou({ montarFrase(m, frasesDeLugar) }, { montarFrase(m, frasesDeLugar) }, { fraseFigura(m, frasesDeLugar) }) },
            ModeloDeFase("Desert and circus", "🌵") { ou({ escondePreposicao(m) }, { idade(m) }, { baloesNumeros(m, 20..50) }, { ondeEsta(m) }) },
            ModeloDeFase("Desert expert", "⭐") { ou({ ondeEsta(m) }, { figuraFrase(m, frasesDeLugar) }, { montarFrase(m, frasesDeLugar) }) },
            ModeloDeFase("Boss: Camel", "👑") { ou({ escondePreposicao(m) }, { cacaPalavra(m, I.deserto) }, { ondeEsta(m) }, { preposicao(m) }) },
        )
    }

    // ================================================================== MUNDO 12: CIDADE (111 a 120)

    private fun dominoCidade(m: Int) = dominoPalavras(m, if (random.nextBoolean()) I.lugares else I.transportes, 3)

    private fun mundo12(): List<ModeloDeFase> {
        val m = 12
        val lugares = I.lugares
        val transp = I.transportes
        return listOf(
            ModeloDeFase("Places in town", "🏙️") { ou({ ouvirETocar(m, lugares) }, { ouvirETocar(m, lugares) }, { ligarPalavras(m, lugares, 3) }) },
            ModeloDeFase("Match the places", "🔗") { ou({ ligarPalavras(m, lugares, 3) }, { ligarPalavras(m, lugares, 4) }, { lerONome(m, lugares) }) },
            ModeloDeFase("School or park?", "🏫") { ou({ lerONome(m, lugares) }, { comoSeDiz(m, lugares) }, { robo(m) }) },
            ModeloDeFase("Go to the park", "🤖") { ou({ robo(m) }, { robo(m) }, { ouvirETocar(m, lugares) }) },
            ModeloDeFase("City party", "🚩") { ou({ ouvirETocar(m, lugares) }, { ligarPalavras(m, lugares, 3) }, { robo(m) }, { lerONome(m, lugares) }) },
            ModeloDeFase("Bus and taxi", "🚕") { ou({ ouvirETocar(m, transp) }, { ouvirETocar(m, transp) }, { dominoCidade(m) }) },
            ModeloDeFase("City dominoes", "🎲") { ou({ dominoCidade(m) }, { dominoCidade(m) }, { lerONome(m, transp) }) },
            ModeloDeFase("City and desert", "🚦") { ou({ fraseFigura(m, V.gosto) }, { escondePreposicao(m) }, { ligarPalavras(m, I.profissoes, 3) }, { ouvirETocar(m, I.profissoes) }) },
            ModeloDeFase("Who works here?", "⭐") { ou({ fraseFigura(m, I.quemTrabalhaOnde) }, { lerONome(m, transp) }, { comoSeDiz(m, I.profissoes) }, { ligarPalavras(m, I.profissoes, 4) }) },
            ModeloDeFase("Boss: Big Bus", "👑") { ou({ robo(m) }, { ligarPalavras(m, lugares + transp, 4) }, { lerONome(m, lugares) }, { ouvirETocar(m, transp) }) },
        )
    }

    // ================================================================== MUNDO 13: CASTELO (121 a 130)

    private fun pescarCasa(m: Int) = pescarGrupo(m, I.moveis, I.mar + I.doces, "Pesque só as coisas da casa", "Fish only the things in a house")

    private fun mundo13(): List<ModeloDeFase> {
        val m = 13
        val moveis = I.moveis
        val frases = I.haUmNoComodo
        return listOf(
            ModeloDeFase("Rooms", "🛏️") { ou({ ouvirETocar(m, I.comodos) }, { ouvirETocar(m, moveis) }, { pescarPalavra(m, moveis) }) },
            ModeloDeFase("Castle hunt", "🔎") { ou({ cacaPalavra(m, moveis) }, { cacaPalavra(m, moveis) }, { ouvirETocar(m, moveis) }) },
            ModeloDeFase("Chair or sofa?", "🛋️") { ou({ lerONome(m, moveis) }, { comoSeDiz(m, moveis) }, { cacaPalavra(m, moveis) }) },
            ModeloDeFase("Castle fishing", "🎣") { ou({ pescarCasa(m) }, { pescarPalavra(m, moveis) }, { lerONome(m, moveis) }) },
            ModeloDeFase("Castle party", "🚩") { ou({ ouvirETocar(m, I.comodos) }, { cacaPalavra(m, moveis) }, { pescarCasa(m) }, { comoSeDiz(m, moveis) }) },
            ModeloDeFase("There is a bed", "🏰") { ou({ fraseFigura(m, frases) }, { fraseFigura(m, frases) }, { montarFrase(m, frases) }) },
            ModeloDeFase("Make the sentence", "🧩") { ou({ montarFrase(m, frases) }, { montarFrase(m, frases) }, { figuraFrase(m, frases) }) },
            ModeloDeFase("Castle and city", "🗝️") { ou({ ouvirETocar(m, I.castelo) }, { robo(m) }, { lerONome(m, I.lugares) }, { pescarPalavra(m, I.castelo) }) },
            ModeloDeFase("Castle expert", "⭐") { ou({ figuraFrase(m, frases) }, { comoSeDiz(m, I.castelo + moveis) }, { cacaGrupo(m, moveis, I.castelo, "Ache os móveis", "Find the furniture", "Furniture!") }) },
            ModeloDeFase("Boss: Dragon", "👑") { ou({ cacaPalavra(m, moveis) }, { montarFrase(m, frases) }, { pescarCasa(m) }, { fraseFigura(m, frases) }) },
        )
    }

    // ================================================================== MUNDO 14: DINOSSAUROS (131 a 140)

    private fun quemPode(m: Int): Pergunta {
        val q = I.quemPode.um()
        val certa = q.podem.um()
        val erradas = q.naoPodem.sorteia(3)
        return montar(
            instrucao = "Who can ${q.acao.en}?",
            fala = listOf(en("Who can ${q.acao.en}?")) + (if (m <= 18) listOf(pt("Quem sabe ${q.acao.pt}?")) else emptyList()),
            cena = Cena.Emojis(q.acao.emoji),
            certa = opcaoFigura(certa),
            erradas = erradas.map { opcaoFigura(it) },
            reforco = reforco(m, "Yes! It can ${q.acao.en}!", "Ele sabe ${q.acao.pt}!"),
        )
    }

    private fun simonSays(m: Int, tamanho: IntRange) =
        siga(m, I.comandos.map { it.emoji to it.en }, tamanho, "O mestre mandou! Repita os gestos", "Simon says! Do it in order", "Simon says:")

    private fun comando(m: Int): Pergunta {
        val c = I.comandos.um()
        val erradas = outras(I.comandos, listOf(c), 3)
        return montar(
            instrucao = "Simon says: ${c.en}",
            fala = listOf(en("Simon says: ${c.en}!")) + (if (m <= 18) listOf(pt("Toque no gesto certo.")) else emptyList()),
            cena = Cena.Nenhuma,
            certa = opFig(c),
            erradas = erradas.map(::opFig),
            reforco = reforco(m, maiuscula(c.en) + "!", maiuscula(c.pt) + "!"),
        )
    }

    private fun ligarAcoes(m: Int): Pergunta {
        val frases = I.podeNaoPode.filter { it.en.startsWith("I can") }.sorteia(3)
        return ligar(m, frases.map { opTxt(it.en) }, frases.map { opcaoFigura(it.emoji) }, emptyList(), "Ligue a frase à figura", "Match the sentences")
    }

    private fun mundo14(): List<ModeloDeFase> {
        val m = 14
        val acoes = I.acoes
        return listOf(
            ModeloDeFase("I can jump", "🦖") { ou({ ouvirETocar(m, acoes) }, { ouvirETocar(m, acoes) }, { sigaPalavras(m, acoes, 2..3) }) },
            ModeloDeFase("Dino moves", "🎵") { ou({ sigaPalavras(m, acoes, 3..4) }, { sigaPalavras(m, acoes, 3..3) }, { ouvirETocar(m, acoes) }) },
            ModeloDeFase("Who can fly?", "🦅") { ou({ quemPode(m) }, { quemPode(m) }, { ligarAcoes(m) }) },
            ModeloDeFase("Match the actions", "🔗") { ou({ ligarAcoes(m) }, { ligarPalavras(m, acoes, 4) }, { quemPode(m) }) },
            ModeloDeFase("Dino party", "🚩") { ou({ ouvirETocar(m, acoes) }, { sigaPalavras(m, acoes, 3..4) }, { quemPode(m) }, { ligarAcoes(m) }) },
            ModeloDeFase("Can or can't?", "🙅") { ou({ fraseFigura(m, I.podeNaoPode) }, { figuraFrase(m, I.podeNaoPode) }, { baloesPalavra(m, acoes) }) },
            ModeloDeFase("Dino balloons", "🎈") { ou({ baloesPalavra(m, I.dinossauros + acoes) }, { baloesPalavra(m, acoes, escrito = true) }, { fraseFigura(m, I.podeNaoPode) }) },
            ModeloDeFase("Simon says", "🗣️") { ou({ comando(m) }, { simonSays(m, 2..3) }, { fraseFigura(m, I.haUmNoComodo) }, { cacaPalavra(m, I.moveis) }) },
            ModeloDeFase("Dino expert", "⭐") { ou({ figuraFrase(m, I.podeNaoPode) }, { comando(m) }, { simonSays(m, 3..4) }, { lerONome(m, acoes) }) },
            ModeloDeFase("Boss: T-rex", "👑") { ou({ sigaPalavras(m, acoes, 3..4) }, { baloesPalavra(m, acoes) }, { quemPode(m) }, { simonSays(m, 3..4) }) },
        )
    }

    // ================================================================== MUNDO 15: ILHA DO TESOURO (141 a 150)

    private val dias = VocabularioIngles.diasDaSemana

    private fun opDia(p: PalavraEn) = Opcao(Visual.Texto(p.en), listOf(en(p.en)))

    private fun diasSeguidos(n: Int): List<PalavraEn> {
        val inicio = random.nextInt(7)
        return List(n) { dias[(inicio + it) % 7] }
    }

    private fun tremDias(m: Int): Pergunta {
        val emOrdem = diasSeguidos(5)
        val extras = (dias - emOrdem.toSet()).sorteia(1)
        return trem(m, emOrdem.map(::opDia), extras.map(::opDia), 1, "Monte o trem dos dias da semana", "Put the days in order", emOrdem.joinToString(", ") { it.en })
    }

    private fun cartasDias(m: Int): Pergunta {
        val emOrdem = diasSeguidos(4)
        val extras = (dias - emOrdem.toSet()).sorteia(2)
        return cartas(m, emOrdem.map(::opDia), extras.map(::opDia), "Vire os dias na ordem", "Find the days in order", emOrdem.joinToString(", ") { it.en })
    }

    private fun diaDepois(m: Int): Pergunta {
        val i = random.nextInt(7)
        val certo = dias[(i + 1) % 7]
        val erradas = (dias - certo - dias[i]).sorteia(3)
        return montar(
            instrucao = "After ${dias[i].en}?",
            fala = listOf(en("What day comes after ${dias[i].en}?")) + (if (m <= 18) listOf(pt("Que dia vem depois de ${dias[i].pt}?")) else emptyList()),
            cena = Cena.Palavra(dias[i].en + " →"),
            certa = opDia(certo),
            erradas = erradas.map(::opDia),
            reforco = reforco(m, "${dias[i].en}, ${certo.en}!", "Depois de ${dias[i].pt} vem ${certo.pt}."),
        )
    }

    private fun hojeE(m: Int): Pergunta {
        val d = dias.um()
        val erradas = (dias - d).sorteia(3)
        return montar(
            instrucao = rotulo(m, "Que dia é hoje?", "What day is it?"),
            fala = listOf(en("Today is ${d.en}.")) + (if (m <= 18) listOf(pt("Toque no dia que você ouviu.")) else emptyList()),
            cena = Cena.Emojis("📅"),
            certa = opDia(d),
            erradas = erradas.map(::opDia),
            reforco = reforco(m, "Today is ${d.en}!", "Hoje é ${d.pt}."),
        )
    }

    private fun ditadoDia(m: Int) = ditado(m, dias.filter { it.en.length <= 7 }, comFigura = false)

    private fun mundo15(): List<ModeloDeFase> {
        val m = 15
        val ate100 = (10..100).toList()
        return listOf(
            ModeloDeFase("Days of the week", "📅") { ou({ hojeE(m) }, { hojeE(m) }, { cartasDias(m) }) },
            ModeloDeFase("Days train", "🚂") { ou({ tremDias(m) }, { tremDias(m) }, { hojeE(m) }) },
            ModeloDeFase("After Monday", "🗓️") { ou({ diaDepois(m) }, { diaDepois(m) }, { tremDias(m) }) },
            ModeloDeFase("Treasure cards", "🃏") { ou({ cartasDias(m) }, { cartasDias(m) }, { diaDepois(m) }) },
            ModeloDeFase("Island party", "🚩") { ou({ hojeE(m) }, { tremDias(m) }, { diaDepois(m) }, { cartasDias(m) }) },
            ModeloDeFase("Spell the day", "✏️") { ou({ ditadoDia(m) }, { ditadoDia(m) }, { ouvirETocar(m, I.ilha) }) },
            ModeloDeFase("Up to one hundred", "💯") { ou({ tremDezenas(m, 100) }, { cartasDezenas(m, 100) }, { numeroOuvido(m, (50..100).toList(), ate100) }) },
            ModeloDeFase("Island and dinos", "🏝️") { ou({ ouvirETocar(m, I.ilha) }, { quemPode(m) }, { comando(m) }, { tremDias(m) }) },
            ModeloDeFase("Pirate expert", "⭐") { ou({ lerNumero(m, (50..100).toList(), ate100) }, { diaDepois(m) }, { ditadoDia(m) }, { lerONome(m, I.ilha) }) },
            ModeloDeFase("Boss: Pirate", "👑") { ou({ tremDias(m) }, { cartasDias(m) }, { diaDepois(m) }, { hojeE(m) }) },
        )
    }

    // ================================================================== MUNDO 16: FÁBRICA DE DOCES (151 a 160)

    private val gostoOuNao: List<FraseEn> by lazy {
        I.comidas.flatMap { c ->
            listOf(FraseEn("I like ${c.en}", "Eu gosto de ${c.pt}", "👍" + c.emoji), FraseEn("I don't like ${c.en}", "Eu não gosto de ${c.pt}", "👎" + c.emoji))
        }
    }

    /** "I like cake" ou "I don't like broccoli": quatro figuras com 👍 e 👎. */
    private fun gostaOuNao(m: Int): Pergunta {
        val (c, outra) = distintas(I.comidas, 2)
        val gosta = random.nextBoolean()
        val frase = if (gosta) "I like ${c.en}." else "I don't like ${c.en}."
        val certa = (if (gosta) "👍" else "👎") + c.emoji
        val erradas = listOf((if (gosta) "👎" else "👍") + c.emoji, "👍" + outra.emoji, "👎" + outra.emoji)
        return montar(
            instrucao = rotulo(m, "Gosta ou não gosta?", "Like or don't like?"),
            fala = pedir(m, "Escute e toque na figura certa:", "Listen:") + en(frase),
            cena = Cena.Nenhuma,
            certa = opcaoFigura(certa),
            erradas = erradas.map { opcaoFigura(it) },
            reforco = reforco(m, frase, if (gosta) "Eu gosto de ${c.pt}." else "Eu não gosto de ${c.pt}."),
        )
    }

    private fun pescarDoces(m: Int) =
        if (random.nextBoolean()) pescarGrupo(m, I.doces, I.saudaveis, "Pesque só os doces", "Fish only the sweets")
        else pescarPalavra(m, I.comidas)

    private fun mundo16(): List<ModeloDeFase> {
        val m = 16
        val doces = I.doces
        return listOf(
            ModeloDeFase("Sweets", "🍭") { ou({ ouvirETocar(m, doces) }, { ouvirETocar(m, I.comidas) }, { pescarDoces(m) }) },
            ModeloDeFase("Candy shop", "🛒") { ou({ mercado(m) }, { mercado(m) }, { ouvirETocar(m, doces) }) },
            ModeloDeFase("Cake or pie?", "🍰") { ou({ lerONome(m, doces) }, { comoSeDiz(m, I.comidas) }, { mercado(m) }) },
            ModeloDeFase("Sweet fishing", "🎣") { ou({ pescarDoces(m) }, { pescarDoces(m) }, { lerONome(m, I.comidas) }) },
            ModeloDeFase("Sweet party", "🚩") { ou({ ouvirETocar(m, doces) }, { mercado(m) }, { pescarDoces(m) }, { comoSeDiz(m, I.comidas) }) },
            ModeloDeFase("I like, I don't like", "👍") { ou({ gostaOuNao(m) }, { gostaOuNao(m) }, { montarFrase(m, gostoOuNao) }) },
            ModeloDeFase("Make the sentence", "🧩") { ou({ montarFrase(m, gostoOuNao) }, { montarFrase(m, gostoOuNao) }, { gostaOuNao(m) }) },
            ModeloDeFase("Sweets and days", "🍬") { ou({ hojeE(m) }, { diaDepois(m) }, { pescarDoces(m) }, { gostaOuNao(m) }) },
            ModeloDeFase("Sweet expert", "⭐") { ou({ figuraFrase(m, gostoOuNao) }, { lerONome(m, I.comidas) }, { montarFrase(m, gostoOuNao, comAudio = false) }) },
            ModeloDeFase("Boss: Candy Queen", "👑") { ou({ montarFrase(m, gostoOuNao) }, { pescarDoces(m) }, { gostaOuNao(m) }, { mercado(m) }) },
        )
    }

    // ================================================================== MUNDO 17: BOSQUE ENCANTADO (161 a 170)

    private val euTenho: List<FraseEn> by lazy {
        I.quantasPartes.map { FraseEn("I have ${I.numeroEn(it.quantas)} ${it.en}", "Eu tenho ${it.quantas} ${it.pt}", it.emoji) }
    }

    private fun quantasPartes(m: Int): Pergunta {
        val q = I.quantasPartes.um()
        val op = { k: Int -> opTxt(I.numeroEn(k)) }
        val pergunta = if (q.quantas == 1) "How many ${q.en}s do you have?" else "How many ${q.en} do you have?"
        return montar(
            instrucao = rotulo(m, "Quantos você tem?", "How many?"),
            fala = listOf(en(pergunta)) + (if (m <= 18) listOf(pt("Quantos ${q.pt} você tem?")) else emptyList()),
            cena = Cena.Emojis(q.emoji),
            certa = op(q.quantas),
            erradas = vizinhos(q.quantas, listOf(1, 2, 3, 4, 5, 10, 8)).map(op),
            reforco = reforco(m, "I have ${I.numeroEn(q.quantas)} ${q.en}!", "Eu tenho ${q.quantas} ${q.pt}."),
        )
    }

    private fun mundo17(): List<ModeloDeFase> {
        val m = 17
        val corpo = I.rostoECorpo
        return listOf(
            ModeloDeFase("My face", "🙂") { ou({ ouvirETocar(m, corpo) }, { ouvirETocar(m, corpo) }, { ligarPalavras(m, corpo, 3) }) },
            ModeloDeFase("Paint the face", "🎨") { ou({ pintarRosto(m) }, { pintarRosto(m) }, { ouvirETocar(m, corpo) }) },
            ModeloDeFase("Nose or mouth?", "👃") { ou({ lerONome(m, corpo) }, { comoSeDiz(m, corpo) }, { quebraRosto(m) }) },
            ModeloDeFase("Face puzzle", "🧩") { ou({ quebraRosto(m) }, { quebraRosto(m) }, { lerONome(m, corpo) }) },
            ModeloDeFase("Forest party", "🚩") { ou({ ouvirETocar(m, corpo) }, { pintarRosto(m) }, { quebraRosto(m) }, { comoSeDiz(m, corpo) }) },
            ModeloDeFase("I have two eyes", "👀") { ou({ quantasPartes(m) }, { fraseFigura(m, euTenho) }, { ligarPalavras(m, corpo, 4) }) },
            ModeloDeFase("Match the body", "🔗") { ou({ ligarPalavras(m, corpo, 4) }, { ligarPalavras(m, corpo, 4) }, { quantasPartes(m) }) },
            ModeloDeFase("Forest and sweets", "🍄") { ou({ ouvirETocar(m, I.bosque) }, { gostaOuNao(m) }, { mercado(m) }, { pintarRosto(m) }) },
            ModeloDeFase("Body expert", "⭐") { ou({ figuraFrase(m, euTenho) }, { comoSeDiz(m, corpo) }, { quantasPartes(m) }, { memoriaEn(m, I.bosque, 5) }) },
            ModeloDeFase("Boss: Unicorn", "👑") { ou({ pintarRosto(m) }, { quebraRosto(m) }, { quantasPartes(m) }, { lerONome(m, corpo) }) },
        )
    }

    // ================================================================== MUNDO 18: VULCÃO (171 a 180)

    private fun comoVoceEsta(m: Int): Pergunta {
        val s = I.sentimentos.um()
        val erradas = outras(I.sentimentos, listOf(s), 3)
        return montar(
            instrucao = "How are you?",
            fala = listOf(en("How are you?")) + (if (m <= 18) listOf(pt("Olhe o rosto e escolha a resposta.")) else emptyList()),
            cena = Cena.Emojis(s.emoji),
            certa = opTxt("I am ${s.en}."),
            erradas = erradas.map { opTxt("I am ${it.en}.") },
            reforco = reforco(m, "I am ${s.en}!", "Eu estou ${s.pt}."),
        )
    }

    private fun baloesCarinhas(m: Int): Pergunta {
        val alvo = I.sentimentos.um()
        val resto = outras(I.sentimentos, listOf(alvo), 4)
        val baloes = (listOf(alvo, alvo) + resto).shuffled(random)
        val certos = baloes.indices.filter { baloes[it] == alvo }
        return Pergunta.deAtividade(
            instrucao = rotulo(m, "Estoure: ${alvo.en}", "Pop the ${alvo.en} faces"),
            fala = pedir(m, "Estoure os balões com a carinha:", "Pop the balloons:") + en(alvo.en),
            atividade = EstourarBaloes(baloes.map(::opFig), certos),
            reforco = listOf(en(maiuscula(alvo.en) + "!")),
        )
    }

    private fun mundo18(): List<ModeloDeFase> {
        val m = 18
        val sent = I.sentimentos
        return listOf(
            ModeloDeFase("Feelings", "😀") { ou({ ouvirETocar(m, sent) }, { ouvirETocar(m, sent) }, { ligarPalavras(m, sent, 3) }) },
            ModeloDeFase("Match the faces", "🔗") { ou({ ligarPalavras(m, sent, 4) }, { ligarPalavras(m, sent, 3) }, { ouvirETocar(m, sent) }) },
            ModeloDeFase("Happy or excited?", "🤩") { ou({ lerONome(m, sent) }, { comoSeDiz(m, sent) }, { dominoPalavras(m, sent, 2) }) },
            ModeloDeFase("Volcano dominoes", "🎲") { ou({ dominoPalavras(m, sent, 3) }, { dominoPalavras(m, sent, 2) }, { lerONome(m, sent) }) },
            ModeloDeFase("Volcano party", "🚩") { ou({ ouvirETocar(m, sent) }, { ligarPalavras(m, sent, 4) }, { dominoPalavras(m, sent, 3) }, { comoSeDiz(m, sent) }) },
            ModeloDeFase("How are you?", "🙋") { ou({ comoVoceEsta(m) }, { comoVoceEsta(m) }, { baloesCarinhas(m) }) },
            ModeloDeFase("Feeling balloons", "🎈") { ou({ baloesCarinhas(m) }, { baloesCarinhas(m) }, { comoVoceEsta(m) }) },
            ModeloDeFase("Volcano and forest", "🌋") { ou({ quantasPartes(m) }, { ligarPalavras(m, I.rostoECorpo, 4) }, { comoVoceEsta(m) }, { oposto(m) }) },
            ModeloDeFase("Feelings expert", "⭐") { ou({ comoVoceEsta(m) }, { figuraFrase(m, V.souEPosso) }, { dominoPalavras(m, sent, 4) }, { comoSeDiz(m, sent) }) },
            ModeloDeFase("Boss: Fire Dragon", "👑") { ou({ dominoPalavras(m, sent, 3) }, { ligarPalavras(m, sent, 4) }, { comoVoceEsta(m) }, { baloesCarinhas(m) }) },
        )
    }

    // ================================================================== MUNDO 19: REINO DAS NUVENS (181 a 190)

    private val itIsTempo: List<FraseEn> by lazy {
        I.tempo.map { FraseEn("It is ${it.en}", "Está ${it.pt}", it.emoji) } + I.estacoes.map { FraseEn("It is ${it.en}", "É ${it.pt}", it.emoji) }
    }

    private val frasesDoTempo: List<FraseEn> by lazy {
        itIsTempo + listOf(
            FraseEn("It is sunny today", "Hoje está ensolarado", "😎"),
            FraseEn("I like the rain", "Eu gosto da chuva", "☔"),
            FraseEn("I wear a coat in winter", "Eu visto casaco no inverno", "🧥"),
            FraseEn("We go to the beach in summer", "Nós vamos à praia no verão", "🏝️"),
        )
    }

    private fun mundo19(): List<ModeloDeFase> {
        val m = 19
        val tempo = I.tempo + I.estacoes
        return listOf(
            ModeloDeFase("Weather", "🌦️") { ou({ ouvirETocar(m, I.tempo) }, { ouvirETocar(m, I.tempo) }, { cacaPalavra(m, I.ceu) }) },
            ModeloDeFase("Sky hunt", "🔎") { ou({ cacaPalavra(m, I.ceu) }, { cacaPalavra(m, I.ceu) }, { ouvirETocar(m, I.ceu) }) },
            ModeloDeFase("It is sunny", "☀️") { ou({ fraseFigura(m, itIsTempo) }, { figuraFrase(m, itIsTempo) }, { sigaPalavras(m, I.tempo, 3..4) }) },
            ModeloDeFase("Weather song", "🎵") { ou({ sigaPalavras(m, I.tempo, 3..4) }, { sigaPalavras(m, I.estacoes, 3..4) }, { fraseFigura(m, itIsTempo) }) },
            ModeloDeFase("Cloud party", "🚩") { ou({ ouvirETocar(m, tempo) }, { cacaPalavra(m, I.ceu) }, { fraseFigura(m, itIsTempo) }, { sigaPalavras(m, I.tempo, 3..4) }) },
            ModeloDeFase("Four seasons", "🍂") { ou({ ouvirETocar(m, I.estacoes) }, { lerONome(m, tempo) }, { montarFrase(m, frasesDoTempo) }) },
            ModeloDeFase("Make the sentence", "🧩") { ou({ montarFrase(m, frasesDoTempo) }, { montarFrase(m, frasesDoTempo) }, { figuraFrase(m, frasesDoTempo) }) },
            ModeloDeFase("Clouds and volcano", "☁️") { ou({ comoVoceEsta(m) }, { baloesCarinhas(m) }, { fraseFigura(m, itIsTempo) }, { cacaPalavra(m, I.ceu) }) },
            ModeloDeFase("Weather expert", "⭐") { ou({ figuraFrase(m, frasesDoTempo) }, { lerONome(m, tempo) }, { montarFrase(m, frasesDoTempo, comAudio = false) }) },
            ModeloDeFase("Boss: Cloud Giant", "👑") { ou({ cacaGrupo(m, I.tempo, I.doces, "Ache o tempo", "Find the weather", "the weather") }, { montarFrase(m, frasesDoTempo) }, { fraseFigura(m, itIsTempo) }, { cacaPalavra(m, I.ceu) }) },
        )
    }

    // ================================================================== MUNDO 20: CAVERNA DE CRISTAL (181 a 200)

    private val alfabeto = ('A'..'Z').map { it.toString() }

    private fun letraOuvida(m: Int): Pergunta {
        val l = alfabeto.um()
        val erradas = (alfabeto - l).sorteia(3)
        return montar(
            instrucao = "Find the letter $l",
            fala = listOf(en("Find the letter"), en(l)),
            cena = Cena.Nenhuma,
            certa = opTxt(l),
            erradas = erradas.map(::opTxt),
            reforco = listOf(en("$l! The letter $l!")),
        )
    }

    private fun comecaCom(m: Int): Pergunta {
        val (letra, p) = I.letraEPalavra.um()
        val erradas = I.letraEPalavra.map { it.first }.filter { it != letra }.sorteia(3)
        return montar(
            instrucao = "${maiuscula(p.en)} starts with...",
            fala = listOf(en("What letter does ${p.en} start with?")),
            cena = Cena.Emojis(p.emoji),
            certa = opTxt(letra),
            erradas = erradas.map(::opTxt),
            reforco = listOf(en("$letra is for ${p.en}!"), pt(maiuscula(p.pt) + ".")),
        )
    }

    private fun sightWord(m: Int): Pergunta {
        val w = I.sightWords.um()
        val erradas = (I.sightWords - w).sorteia(3)
        return montar(
            instrucao = "Find the word: $w",
            fala = listOf(en("Find the word:"), en(w)),
            cena = Cena.Nenhuma,
            certa = opTxt(w),
            erradas = erradas.map(::opTxt),
            reforco = listOf(en("$w! Great reading!")),
        )
    }

    private fun baloesSoletrar(m: Int): Pergunta {
        val p = I.palavrasCvc.um()
        val letras = p.en.map { it.toString() }
        val extras = ('a'..'z').map { it.toString() }.filter { it !in letras }.sorteia(3)
        return baloesEmOrdem(m, letras.map(::opTxt), extras.map(::opTxt), "Estoure as letras na ordem", "Spell the word! Pop the letters", "${p.en}: ${letras.joinToString(", ")}")
    }

    private fun cartasAlfabeto(m: Int): Pergunta {
        val inicio = random.nextInt(alfabeto.size - 5)
        val emOrdem = alfabeto.subList(inicio, inicio + 5)
        val extras = (alfabeto - emOrdem.toSet()).sorteia(2)
        return cartas(m, emOrdem.map(::opTxt), extras.map(::opTxt), "Vire as letras na ordem", "Find the letters in order", emOrdem.joinToString(", "))
    }

    private fun mundo20(): List<ModeloDeFase> {
        val m = 20
        val cvc = I.palavrasCvc
        return listOf(
            ModeloDeFase("ABC", "🔤") { ou({ letraOuvida(m) }, { letraOuvida(m) }, { cartasAlfabeto(m) }) },
            ModeloDeFase("Spell cat", "🐱") { ou({ ditado(m, cvc, comFigura = true) }, { ditado(m, cvc, comFigura = true) }, { letraOuvida(m) }) },
            ModeloDeFase("C is for cat", "🔡") { ou({ comecaCom(m) }, { comecaCom(m) }, { baloesSoletrar(m) }) },
            ModeloDeFase("Letter balloons", "🎈") { ou({ baloesSoletrar(m) }, { baloesSoletrar(m) }, { comecaCom(m) }) },
            ModeloDeFase("Crystal party", "🚩") { ou({ letraOuvida(m) }, { ditado(m, cvc, comFigura = true) }, { comecaCom(m) }, { baloesSoletrar(m) }) },
            ModeloDeFase("Letter cards", "🃏") { ou({ cartasAlfabeto(m) }, { cartasAlfabeto(m) }, { sightWord(m) }) },
            ModeloDeFase("Sight words", "👀") { ou({ sightWord(m) }, { sightWord(m) }, { ditado(m, cvc, comFigura = true) }) },
            ModeloDeFase("Crystal and clouds", "💎") { ou({ fraseFigura(m, itIsTempo) }, { cacaPalavra(m, I.ceu) }, { comecaCom(m) }, { cartasAlfabeto(m) }) },
            ModeloDeFase("Spelling expert", "⭐") { ou({ ditado(m, cvc, comFigura = false) }, { ditado(m, I.palavrasDeQuatro, comFigura = false) }, { sightWord(m) }, { comecaCom(m) }) },
            ModeloDeFase("Boss: Crystal Bat", "👑") { ou({ ditado(m, cvc + I.palavrasDeQuatro, comFigura = false) }, { baloesSoletrar(m) }, { cartasAlfabeto(m) }, { comecaCom(m) }) },
        )
    }

    // ================================================================== MUNDO 21: PARQUE DE DIVERSÕES (201 a 210)

    private fun quantosPlural(m: Int): Pergunta {
        val t = I.brinquedos.um()
        val n = (2..6).random(random)
        val op = { k: Int -> opTxt("${I.numeroEn(k)} ${I.plural(t.en)}") }
        return montar(
            instrucao = "How many?",
            fala = listOf(en("How many?")),
            cena = Cena.Emojis(t.emoji.repeat(n)),
            certa = op(n),
            erradas = vizinhos(n, (2..8).toList()).map(op),
            reforco = reforco(m, "${maiuscula(I.numeroEn(n))} ${I.plural(t.en)}!", "${maiuscula(porExtenso(n))}."),
        )
    }

    private fun pluralFigura(m: Int): Pergunta {
        val t = I.brinquedos.um()
        val n = (2..5).random(random)
        val outro = outras(I.brinquedos, listOf(t), 1).first()
        val certa = t.emoji.repeat(n)
        val erradas = listOf(t.emoji.repeat(if (n == 2) 4 else n - 1), t.emoji.repeat(n + 1), outro.emoji.repeat(n))
        return montar(
            instrucao = "Find: $n ${I.plural(t.en)}",
            fala = listOf(en("Find ${I.numeroEn(n)} ${I.plural(t.en)}.")),
            cena = Cena.Nenhuma,
            certa = opcaoFigura(certa),
            erradas = erradas.map { opcaoFigura(it) },
            reforco = reforco(m, "${maiuscula(I.numeroEn(n))} ${I.plural(t.en)}!", "${maiuscula(porExtenso(n))} ${t.pt}."),
        )
    }

    private fun mundo21(): List<ModeloDeFase> {
        val m = 21
        val br = I.brinquedos
        val esp = I.esportesEMusica
        return listOf(
            ModeloDeFase("Toys", "🧸") { ou({ ouvirETocar(m, br) }, { ouvirETocar(m, br) }, { cestaPlural(m) }) },
            ModeloDeFase("Toy basket", "🧺") { ou({ cestaPlural(m) }, { cestaPlural(m) }, { quantosPlural(m) }) },
            ModeloDeFase("Two balls", "⚽") { ou({ quantosPlural(m) }, { pluralFigura(m) }, { pescarPalavra(m, br) }) },
            ModeloDeFase("Toy fishing", "🎣") { ou({ pescarPalavra(m, br) }, { pescarPalavra(m, br) }, { pluralFigura(m) }) },
            ModeloDeFase("Park party", "🚩") { ou({ ouvirETocar(m, br) }, { cestaPlural(m) }, { quantosPlural(m) }, { pescarPalavra(m, br) }) },
            ModeloDeFase("Sports and music", "🎸") { ou({ ouvirETocar(m, esp) }, { lerONome(m, esp) }, { ligarPalavras(m, esp, 3) }) },
            ModeloDeFase("Match the sports", "🔗") { ou({ ligarPalavras(m, esp, 4) }, { ligarPalavras(m, esp + I.parque, 4) }, { ouvirETocar(m, esp) }) },
            ModeloDeFase("Park and crystal", "🎡") { ou({ ditado(m, I.palavrasCvc, comFigura = true) }, { comecaCom(m) }, { quantosPlural(m) }, { ouvirETocar(m, I.parque) }) },
            ModeloDeFase("Park expert", "⭐") { ou({ pluralFigura(m) }, { comoSeDiz(m, br + esp) }, { quantosPlural(m) }, { memoriaEn(m, esp, 5) }) },
            ModeloDeFase("Boss: Carousel", "👑") { ou({ cestaPlural(m) }, { pescarPalavra(m, br) }, { quantosPlural(m) }, { pluralFigura(m) }) },
        )
    }

    // ================================================================== MUNDO 22: CIDADE DOS ROBÔS (211 a 220)

    private fun dominoFamilia(m: Int) = dominoPalavras(m, I.familia, 3)

    private fun mundo22(): List<ModeloDeFase> {
        val m = 22
        val meu = I.meuSeu
        val agora = I.fazendoAgora
        return listOf(
            ModeloDeFase("This is my robot", "🤖") { ou({ fraseFigura(m, meu) }, { fraseFigura(m, meu) }, { montarFrase(m, meu) }) },
            ModeloDeFase("Make the sentence", "🧩") { ou({ montarFrase(m, meu) }, { montarFrase(m, meu) }, { fraseFigura(m, meu) }) },
            ModeloDeFase("He or she?", "👫") { ou({ figuraFrase(m, meu) }, { ouvirETocar(m, I.familia) }, { robo(m) }) },
            ModeloDeFase("Program the robot", "🔋") { ou({ robo(m) }, { robo(m) }, { figuraFrase(m, meu) }) },
            ModeloDeFase("Robot party", "🚩") { ou({ fraseFigura(m, meu) }, { montarFrase(m, meu) }, { robo(m) }, { ouvirETocar(m, I.familia) }) },
            ModeloDeFase("My family", "👨‍👩‍👧") { ou({ ouvirETocar(m, I.familia) }, { lerONome(m, I.familia) }, { dominoFamilia(m) }) },
            ModeloDeFase("Family dominoes", "🎲") { ou({ dominoFamilia(m) }, { dominoPalavras(m, I.robos, 3) }, { lerONome(m, I.familia) }) },
            ModeloDeFase("He is running", "🏃") { ou({ fraseFigura(m, agora) }, { figuraFrase(m, agora) }, { montarFrase(m, agora) }, { quantosPlural(m) }) },
            ModeloDeFase("Robot expert", "⭐") { ou({ figuraFrase(m, agora + meu) }, { montarFrase(m, agora, comAudio = false) }, { simonSays(m, 3..5) }, { comoSeDiz(m, I.robos) }) },
            ModeloDeFase("Boss: Mega Robot", "👑") { ou({ robo(m) }, { montarFrase(m, meu + agora) }, { fraseFigura(m, agora) }, { figuraFrase(m, meu) }) },
        )
    }

    // ================================================================== MUNDO 23: SAVANA (221 a 230)

    private val descricoes: List<FraseEn> by lazy { I.descricoes.map { FraseEn(it.en, it.pt, it.bicho.emoji) } }

    private fun escondeSavana(m: Int): Pergunta {
        val usados = I.descricoes.distinctBy { it.bicho.emoji }.sorteia(4)
        val certo = usados.indices.random(random)
        return esconde(m, usados.map { Opcao(Visual.Emoji(it.bicho.emoji), listOf(en(it.en))) }, certo,
            "The bird is behind ${usados[certo].en}.", "🐦")
    }

    private fun cacaSavana(m: Int): Pergunta {
        val alvo = I.descricoes.um()
        val k = (1..2).random(random)
        val resto = outras(I.savana, listOf(alvo.bicho), 6)
        return caca(m, List(k) { opEmojiEn(alvo.bicho.emoji, alvo.en) }, resto.map(::opFig), "Ache", "Find",
            if (k == 1) alvo.en else "two: ${alvo.en}", maiuscula(alvo.en) + "!")
    }

    private fun mundo23(): List<ModeloDeFase> {
        val m = 23
        val sav = I.savana
        return listOf(
            ModeloDeFase("Savanna animals", "🦁") { ou({ ouvirETocar(m, sav) }, { ouvirETocar(m, sav) }, { escondeSavana(m) }) },
            ModeloDeFase("Savanna hide and seek", "🌾") { ou({ escondeSavana(m) }, { escondeSavana(m) }, { ouvirETocar(m, sav) }) },
            ModeloDeFase("A big gray elephant", "🐘") { ou({ fraseFigura(m, descricoes) }, { fraseFigura(m, descricoes) }, { cacaSavana(m) }) },
            ModeloDeFase("Savanna hunt", "🔎") { ou({ cacaSavana(m) }, { cacaSavana(m) }, { figuraFrase(m, descricoes) }) },
            ModeloDeFase("Savanna party", "🚩") { ou({ ouvirETocar(m, sav) }, { escondeSavana(m) }, { fraseFigura(m, descricoes) }, { cacaSavana(m) }) },
            ModeloDeFase("Paint the lion", "🎨") { ou({ pintarLeao(m) }, { pintarCores(m) }, { qualCor(m) }) },
            ModeloDeFase("Colors and animals", "🖌️") { ou({ pintarCores(m) }, { pintarLeao(m) }, { figuraFrase(m, descricoes) }) },
            ModeloDeFase("Savanna and robots", "🦒") { ou({ fraseFigura(m, I.fazendoAgora) }, { dominoFamilia(m) }, { lerONome(m, sav) }, { cacaSavana(m) }) },
            ModeloDeFase("Savanna expert", "⭐") { ou({ figuraFrase(m, descricoes) }, { montarFrase(m, descricoes) }, { escondeSavana(m) }, { comoSeDiz(m, sav) }) },
            ModeloDeFase("Boss: Lion King", "👑") { ou({ cacaSavana(m) }, { pintarLeao(m) }, { escondeSavana(m) }, { fraseFigura(m, descricoes) }) },
        )
    }

    // ================================================================== MUNDO 24: VALE DAS CORES (231 a 240)

    private fun perguntaEResposta(m: Int): Pergunta {
        val q = I.perguntasERespostas.um()
        val erradas = I.perguntasERespostas.filter { it.emoji != q.emoji && it.resposta != q.resposta }.distinctBy { it.resposta }.sorteia(3)
        return montar(
            instrucao = "Choose the answer",
            fala = listOf(en(q.pergunta)),
            cena = Cena.Emojis(q.emoji),
            certa = opTxt(q.resposta),
            erradas = erradas.map { opTxt(it.resposta) },
            reforco = listOf(en("${q.pergunta} ${q.resposta}")),
        )
    }

    private fun ligarPerguntas(m: Int): Pergunta {
        val qs = I.perguntasERespostas.groupBy { it.pergunta }.values.map { it.um() }.sorteia(if (m >= 24) 4 else 3)
        return ligar(m, qs.map { opTxt(it.pergunta) }, qs.map { opTxt(it.resposta) }, emptyList(), "Ligue a pergunta à resposta", "Match the questions and the answers")
    }

    private val frasesDePergunta: List<FraseEn> by lazy {
        listOf(
            FraseEn("What color is it?", "Que cor é?", "🎨"),
            FraseEn("What is it?", "O que é?", "❓"),
            FraseEn("Where is the cat?", "Onde está o gato?", "🐱"),
            FraseEn("It is a red apple.", "É uma maçã vermelha.", "🍎"),
            FraseEn("The ball is in the box.", "A bola está dentro da caixa.", "📦"),
            FraseEn("How old are you?", "Quantos anos você tem?", "🎂"),
            FraseEn("It is a yellow banana.", "É uma banana amarela.", "🍌"),
            FraseEn("How are you today?", "Como você está hoje?", "🙂"),
        )
    }

    private fun baloesResposta(m: Int): Pergunta {
        val (emoji, nome) = V.coisasColoridas.um()
        val cor = I.cores.first { it.en == nome }
        val outras = I.cores.filter { it.en != nome }.sorteia(4)
        val baloes = (outras + cor).shuffled(random)
        return Pergunta.deAtividade(
            instrucao = "What color is it?",
            fala = listOf(en("What color is it? Pop the answer!")),
            atividade = EstourarBaloes(baloes.map(::opPal), listOf(baloes.indexOf(cor))),
            reforco = listOf(en("It is ${cor.en}!")),
            cena = Cena.Emojis(emoji),
        )
    }

    private fun historia(m: Int): Pergunta {
        val h = I.historias.um()
        return montar(
            instrucao = "Listen to the story",
            fala = listOf(en("Listen to the story.")) + h.frases.map { en(it) } + en(h.pergunta),
            cena = Cena.Palavra(h.pergunta),
            certa = opcaoFigura(h.certa),
            erradas = h.erradas.map { opcaoFigura(it) },
            reforco = listOf(en("Yes! Great listening!")) + (if (m <= 24) listOf(pt(h.pt)) else emptyList()),
        )
    }

    private fun mundo24(): List<ModeloDeFase> {
        val m = 24
        return listOf(
            ModeloDeFase("What is it?", "❓") { ou({ perguntaEResposta(m) }, { perguntaEResposta(m) }, { ligarPerguntas(m) }) },
            ModeloDeFase("Questions and answers", "🔗") { ou({ ligarPerguntas(m) }, { ligarPerguntas(m) }, { perguntaEResposta(m) }) },
            ModeloDeFase("What color is it?", "🎨") { ou({ qualCor(m) }, { perguntaEResposta(m) }, { baloesResposta(m) }) },
            ModeloDeFase("Color balloons", "🎈") { ou({ baloesResposta(m) }, { baloesResposta(m) }, { qualCor(m) }) },
            ModeloDeFase("Rainbow party", "🚩") { ou({ perguntaEResposta(m) }, { ligarPerguntas(m) }, { qualCor(m) }, { baloesResposta(m) }) },
            ModeloDeFase("Make the question", "🧩") { ou({ montarFrase(m, frasesDePergunta) }, { montarFrase(m, frasesDePergunta) }, { perguntaEResposta(m) }) },
            ModeloDeFase("Little stories", "📖") { ou({ historia(m) }, { historia(m) }, { montarFrase(m, frasesDePergunta) }) },
            ModeloDeFase("Colors and savanna", "🌈") { ou({ fraseFigura(m, descricoes) }, { escondeSavana(m) }, { historia(m) }, { ligarPerguntas(m) }) },
            ModeloDeFase("Question expert", "⭐") { ou({ historia(m) }, { perguntaEResposta(m) }, { montarFrase(m, frasesDePergunta, comAudio = false) }) },
            ModeloDeFase("Boss: Rainbow", "👑") { ou({ montarFrase(m, frasesDePergunta) }, { ligarPerguntas(m) }, { baloesResposta(m) }, { historia(m) }) },
        )
    }

    // ================================================================== MUNDO 25: CASTELO DAS ESTRELAS (241 a 250)

    private val todasAsFrases: List<FraseEn> by lazy {
        (V.gosto + V.souEPosso + euVisto + I.haUmNoComodo + gostoOuNao + euTenho + I.meuSeu + I.fazendoAgora + frasesDoTempo).distinctBy { it.en }
    }

    /** Grande chefão final: cada pergunta vem de uma faixa de 5 mundos (1 a 5, 6 a 10, 11 a 15, 16 a 20, 21 a 25). */
    private var faixaDoFinal = 0

    private fun perguntaDoFinal(): Pergunta {
        val faixa = faixaDoFinal++ % 5
        val numero = when (faixa) {
            0 -> (1..50).random(random)
            4 -> (201..249).random(random)
            else -> (faixa * 50 + 1..faixa * 50 + 50).random(random)
        }
        return fase(numero).criar()
    }

    private fun mundo25(): List<ModeloDeFase> {
        val m = 25
        val esp = I.espaco
        return listOf(
            ModeloDeFase("Space words", "🪐") { ou({ ouvirETocar(m, esp) }, { lerONome(m, esp) }, { sigaPalavras(m, esp, 4..5) }) },
            ModeloDeFase("Star song", "🎵") { ou({ sigaPalavras(m, esp, 4..5) }, { simonSays(m, 4..5) }, { ouvirETocar(m, esp) }) },
            ModeloDeFase("Space stories", "📖") { ou({ historia(m) }, { historia(m) }, { cacaPalavra(m, esp) }) },
            ModeloDeFase("Find the stars", "🔎") { ou({ cacaPalavra(m, esp) }, { cacaPalavra(m, esp) }, { historia(m) }) },
            ModeloDeFase("Star party", "🚩") { ou({ ouvirETocar(m, esp) }, { sigaPalavras(m, esp, 4..5) }, { historia(m) }, { cacaPalavra(m, esp) }) },
            ModeloDeFase("All the sentences", "💬") { ou({ montarFrase(m, todasAsFrases) }, { fraseFigura(m, todasAsFrases) }, { dominoPalavras(m, esp, 4) }) },
            ModeloDeFase("Star dominoes", "🎲") { ou({ dominoPalavras(m, esp, 4) }, { dominoPalavras(m, I.savana + I.mar, 4) }, { figuraFrase(m, todasAsFrases) }) },
            ModeloDeFase("Stars and colors", "🌈") { ou({ perguntaEResposta(m) }, { ligarPerguntas(m) }, { montarFrase(m, todasAsFrases) }, { comecaCom(m) }) },
            ModeloDeFase("English star", "⭐") { ou({ historia(m) }, { montarFrase(m, todasAsFrases, comAudio = false) }, { cacaSavana(m) }, { comoVoceEsta(m) }) },
            ModeloDeFase("Grand final: Star King", "🏆") { perguntaDoFinal() },
        )
    }
}
