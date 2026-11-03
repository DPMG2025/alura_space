package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.*
import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Fases do caminho Números (matemática): 250 fases em 25 mundos de 10 ([Mundos]).
 * As fases 1 a 50 são as de sempre (o progresso salvo continua valendo). As fases 51 a 250 ainda
 * são provisórias ([provisoria]) até cada mundo novo ganhar o conteúdo do plano (docs/plano-1000-fases.md).
 */
class FasesNumeros(random: Random = Random.Default) : FasesDoCaminho(random) {

    override val trilha = Trilha.MATEMATICA

    override fun fase(numero: Int): ModeloDeFase {
        conferir(numero)
        return if (numero <= FASES_PRONTAS) ate50[numero - 1] else provisoria(numero, ate50)
    }

    /** As 50 fases de antes, na ordem (a fase 1 é o índice 0). */
    private val ate50: List<ModeloDeFase> by lazy { matematica + matematicaMundos }

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

    private val matematica: List<ModeloDeFase> = listOf(
        ModeloDeFase("Contar até 3", "🍎") { contar(1, 3, 4) },
        ModeloDeFase("Contar até 5", "⭐") { contar(1, 5, 4) },
        ModeloDeFase("Cesta: até 5", "🧺") { if (random.nextInt(3) == 0) contar(1, 5, 4) else cesta(1, 5) },
        ModeloDeFase("Achar a quantidade", "👆") { acharQuantidade() },
        ModeloDeFase("Memória dos números", "🃏") { if (random.nextBoolean()) memoriaDeNumeros() else acharQuantidade() },
        ModeloDeFase("Contar até 10", "🔟") { contar(4, 10, 4) },
        ModeloDeFase("Cesta: até 10", "🧺") { if (random.nextInt(3) == 0) contar(5, 10, 4) else cesta(4, 9) },
        ModeloDeFase("Mais ou menos", "⚖️") { maisOuMenos() },
        ModeloDeFase("Maior e menor número", "🔢") { maiorNumero(10) },
        ModeloDeFase("Formas", "🔺") { formas(emIngles = false) },
        ModeloDeFase("Somar até 5", "➕") { somar(5, 4) },
        ModeloDeFase("Tirar até 5", "➖") { subtrair(5, 4) },
        ModeloDeFase("Antes e depois", "↔️") { antesDepois(10) },
        ModeloDeFase("Historinhas de somar", "📖") { historia(soma = true, max = 8) },
        ModeloDeFase("Somar até 10", "🧮") { somar(10, 4) },
        ModeloDeFase("Tirar até 10", "🎈") { subtrair(10, 4) },
        ModeloDeFase("Historinhas de tirar", "📕") { historia(soma = false, max = 10) },
        ModeloDeFase("Números até 20", "2️⃣") { ouvirNumero(10..20) },
        ModeloDeFase("Antes e depois até 20", "🔁") { antesDepois(20) },
        ModeloDeFase("De 2 em 2", "👣") { deDoisEmDois() },
        ModeloDeFase("Maior até 20", "🏔️") { maiorNumero(20) },
        ModeloDeFase("Contas misturadas", "🎲") { when (random.nextInt(3)) { 0 -> somar(10, 4); 1 -> subtrair(10, 4); else -> historia(random.nextBoolean(), 10) } },
        ModeloDeFase("Cesta e memória", "🧩") { when (random.nextInt(3)) { 0 -> cesta(5, 10); 1 -> memoriaDeNumeros(); else -> maisOuMenos() } },
        ModeloDeFase("Desafio dos números", "🏆") { when (random.nextInt(5)) { 0 -> contar(5, 10, 4); 1 -> somar(10, 4); 2 -> subtrair(10, 4); 3 -> antesDepois(20); else -> deDoisEmDois() } },
        ModeloDeFase("Supercampeão dos números", "👑") { when (random.nextInt(5)) { 0 -> historia(random.nextBoolean(), 10); 1 -> maiorNumero(20); 2 -> ouvirNumero(10..20); 3 -> cesta(5, 10); else -> memoriaDeNumeros() } },
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

    private val matematicaMundos: List<ModeloDeFase> = listOf(
        // Mundo 3 (Praia), fases 26 a 30
        ModeloDeFase("Contar até 20", "🧮") { if (random.nextInt(3) == 0) ouvirNumero(11..20) else contar(11, 20, 4) },
        ModeloDeFase("Escrever os números", "✍️") { if (random.nextInt(3) == 0) ouvirNumero(0..10) else tracarNumero() },
        ModeloDeFase("Somar até 20", "➕") { somarAte20() },
        ModeloDeFase("Tirar até 20", "➖") { tirarAte20() },
        ModeloDeFase("Desafio da praia", "🏆") { when (random.nextInt(4)) { 0 -> contar(11, 20, 4); 1 -> somarAte20(); 2 -> tirarAte20(); else -> tracarNumero() } },
        // Mundo 4 (Montanha), fases 31 a 40
        ModeloDeFase("De 10 em 10", "🔟") { deDezEmDez(50) },
        ModeloDeFase("Dezenas e unidades", "📦") { dezenasEUnidades() },
        ModeloDeFase("Números até 50", "5️⃣") { ateCinquenta() },
        ModeloDeFase("O dobro", "👯") { dobro(6) },
        ModeloDeFase("A metade", "🍕") { metade(12) },
        ModeloDeFase("Pesado, leve, alto", "⚖️") { medidas() },
        ModeloDeFase("Horas cheias", "🕒") { horas() },
        ModeloDeFase("Moedinhas", "🪙") { dinheiro() },
        ModeloDeFase("Gráfico de figuras", "📊") { grafico() },
        ModeloDeFase("Desafio da montanha", "🏆") { when (random.nextInt(5)) { 0 -> dezenasEUnidades(); 1 -> dobro(6); 2 -> horas(); 3 -> dinheiro(); else -> grafico() } },
        // Mundo 5 (Espaço), fases 41 a 50
        ModeloDeFase("Até 100", "💯") { ateCem() },
        ModeloDeFase("Somar dezenas", "🚀") { somarDezenas() },
        ModeloDeFase("Problemas de somar", "📖") { problema(soma = true) },
        ModeloDeFase("Problemas de tirar", "📕") { problema(soma = false) },
        ModeloDeFase("Filas e formas", "📍") { posicoesEFormas() },
        ModeloDeFase("Pulando números", "🦘") { sequenciaNumerica(50) },
        ModeloDeFase("Comparar até 100", "🏔️") { compararAte100() },
        ModeloDeFase("Dobro, metade e horas", "🕐") { when (random.nextInt(3)) { 0 -> dobro(10); 1 -> metade(20); else -> horas() } },
        ModeloDeFase("Problemas espertos", "🧠") { when (random.nextInt(3)) { 0 -> problema(true, doisPassos = true); 1 -> memoriaDeQuantidades(); else -> problema(random.nextBoolean()) } },
        ModeloDeFase("Mestre dos números", "🌟") { when (random.nextInt(6)) { 0 -> somarDezenas(); 1 -> problema(true, true); 2 -> compararAte100(); 3 -> dinheiro(); 4 -> grafico(); else -> sequenciaNumerica(100) } },
    )
}
