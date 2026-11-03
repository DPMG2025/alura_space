package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.*
import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Fases do caminho Inglês: 250 fases em 25 mundos de 10 ([Mundos]).
 * As fases 1 a 50 são as de sempre (o progresso salvo continua valendo). As fases 51 a 250 ainda
 * são provisórias ([provisoria]) até cada mundo novo ganhar o conteúdo do plano (docs/plano-1000-fases.md).
 */
class FasesIngles(random: Random = Random.Default) : FasesDoCaminho(random) {

    override val trilha = Trilha.INGLES

    override fun fase(numero: Int): ModeloDeFase {
        conferir(numero)
        return if (numero <= FASES_PRONTAS) ate50[numero - 1] else provisoria(numero, ate50)
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
}
