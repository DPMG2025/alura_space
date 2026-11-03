package br.com.casinhadosaber.dominio.fases

import br.com.casinhadosaber.dominio.*
import br.com.casinhadosaber.dominio.Vocabulario.falaDaLetra
import br.com.casinhadosaber.dominio.Vocabulario.porExtenso
import kotlin.random.Random

/**
 * Fases do caminho Números (matemática): 250 fases em 25 mundos de 10 ([Mundos]).
 * As fases 1 a 50 são as de sempre (o progresso salvo continua valendo). As fases 51 a 250 (mundos 6 a 25)
 * seguem o plano (docs/plano-1000-fases.md, seção 4.1) e ficam em [de51a250], com o vocabulário em
 * [VocabularioNumeros].
 */
class FasesNumeros(random: Random = Random.Default) : FasesDoCaminho(random) {

    override val trilha = Trilha.MATEMATICA

    override fun fase(numero: Int): ModeloDeFase {
        conferir(numero)
        return if (numero <= FASES_PRONTAS) ate50[numero - 1] else de51a250[numero - FASES_PRONTAS - 1]
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


    // ================================================================== MUNDOS 6 A 25 (fases 51 a 250)
    //
    // Cada mundo segue o esqueleto do plano: 1 apresenta a ideia (tocar), 2 a mesma ideia num desafio de
    // mexer, 3 opções parecidas, 4 outro desafio de mexer, 5 marco (mistura de 1 a 4), 6 segunda ideia,
    // 7 segunda ideia de mexer, 8 ideia do mundo com revisão do mundo anterior, 9 versão difícil e 10 chefão
    // (as três atividades principais do mundo). Toda fase de mexer também sorteia perguntas de tocar, para
    // nunca formar três desafios iguais seguidos.

    private val VN = VocabularioNumeros

    /** As fases 51 a 250, na ordem (a fase 51 é o índice 0). */
    private val de51a250: List<ModeloDeFase> by lazy {
        (mundo6 + mundo7 + mundo8 + mundo9 + mundo10 + mundo11 + mundo12 + mundo13 + mundo14 + mundo15 +
            mundo16 + mundo17 + mundo18 + mundo19 + mundo20 + mundo21 + mundo22 + mundo23 + mundo24 + mundo25)
            .also { require(it.size == TOTAL_DE_FASES - FASES_PRONTAS) { "Faltam fases: ${it.size}" } }
    }

    // ------------------------------------------------------------------ utilidades dos mundos novos

    private fun ext(n: Int, f: Boolean = false) = VN.extenso(n, f)
    private fun opN(n: Int) = Opcao(Visual.Texto(n.toString()), listOf(pt(ext(n))))
    private fun opT(texto: String, fala: String = texto) = Opcao(Visual.Texto(texto), listOf(pt(fala)))
    private fun opE(emoji: String, fala: String) = Opcao(Visual.Emoji(emoji), listOf(pt(fala)))
    private fun opDinheiro(c: Int) = opT(VN.dinheiroEscrito(c), VN.dinheiroFalado(c))
    private fun maiusc(s: String) = s.replaceFirstChar { it.uppercase() }
    private fun Objeto.quantos() = if (feminino) "Quantas" else "Quantos"
    private fun Objeto.qtd(n: Int) = "${ext(n, feminino)} ${nome(n)}"
    private fun tema(mundo: Int) = VN.tema(mundo)

    /** Três números errados, os mais perto do certo primeiro, sempre dentro de [min]..[max]. */
    private fun errados(certo: Int, candidatos: Iterable<Int>, min: Int = 0, max: Int = 1000): List<Int> {
        val lista = erradosDe(certo, candidatos.filter { it in min..max }).toMutableList()
        var d = 1
        while (lista.size < 3) {
            for (c in listOf(certo + d, certo - d)) if (lista.size < 3 && c in min..max && c != certo && c !in lista) lista += c
            d++
        }
        return lista
    }

    /** Pergunta de tocar: confere aqui que são 4 opções diferentes (a certa e 3 erradas). */
    private fun tocar(instrucao: String, fala: String, cena: Cena, certa: Opcao, erradas: List<Opcao>, reforco: String): Pergunta {
        require(erradas.size == 3 && (erradas + certa).map { it.visual }.toSet().size == 4) { "4 opções distintas em $instrucao: $certa $erradas" }
        return montar(instrucao, listOf(pt(fala)), cena, certa, erradas, listOf(pt(reforco)))
    }

    private fun tocarNumero(instrucao: String, fala: String, cena: Cena, certo: Int, candidatos: Iterable<Int>, reforco: String, min: Int = 0, max: Int = 1000) =
        tocar(instrucao, fala, cena, opN(certo), errados(certo, candidatos, min, max).map(::opN), reforco)

    private fun mexer(instrucao: String, fala: String, atividade: Atividade, reforco: String, cena: Cena = Cena.Nenhuma) =
        Pergunta.deAtividade(instrucao, listOf(pt(fala)), atividade, listOf(pt(reforco)), cena)

    /** Uma pergunta de um dos geradores, sorteado. */
    private fun sorteio(vararg g: () -> Pergunta): Pergunta = g[random.nextInt(g.size)]()

    /** Marco do mundo (bandeirinha): mistura das fases 1 a 4. */
    private fun marco(titulo: String, emoji: String, vararg fases: ModeloDeFase) = ModeloDeFase(titulo, emoji) { fases.toList().um().criar() }

    /** Uma pergunta de uma das 9 primeiras fases do mundo anterior. */
    private fun revisao(anterior: List<ModeloDeFase>): Pergunta = anterior.take(9).um().criar()

    // ------------------------------------------------------------------ contas

    private fun sinalFalado(s: String) = when (s) { "+" -> "mais"; "−" -> "menos"; "×" -> "vezes"; else -> "dividido por" }

    private fun calcular(termos: List<Int>, sinais: List<String>): Int {
        var total = termos[0]
        sinais.forEachIndexed { i, s ->
            val t = termos[i + 1]
            total = when (s) { "+" -> total + t; "−" -> total - t; "×" -> total * t; else -> total / t }
        }
        return total
    }

    private fun partesDaConta(termos: List<Int>, sinais: List<String>): List<String> =
        buildList { add("${termos[0]}"); sinais.forEachIndexed { i, s -> add(s); add("${termos[i + 1]}") } }

    private fun falaDaConta(termos: List<Int>, sinais: List<String>): String =
        buildString { append(ext(termos[0])); sinais.forEachIndexed { i, s -> append(" ${sinalFalado(s)} ${ext(termos[i + 1])}") } }

    private fun candidatosDe(resp: Int, termos: List<Int>) =
        listOf(resp + 1, resp - 1, resp + 2, resp - 2, resp + 10, resp - 10) + termos

    /**
     * Conta escrita. A resposta é sempre calculada aqui. Até 100 a conta aparece em quadradinhos
     * (Cena.Sequencia); com centenas, numa plaquinha (Cena.Palavra).
     */
    private fun conta(termos: List<Int>, sinais: List<String>, dica: String = "", antes: String = "", extras: Iterable<Int> = emptyList()): Pergunta {
        val resp = calcular(termos, sinais)
        val partes = partesDaConta(termos, sinais)
        val texto = partes.joinToString(" ")
        val falada = falaDaConta(termos, sinais)
        val cabe = resp in 0..100 && (sinais.size == 1 || "×" !in sinais)
        val cena = if (cabe) Cena.Sequencia(partes + listOf("=", "?")) else Cena.Palavra("$texto = ?")
        val instrucao = if (cabe) "$texto = ?" else "Quanto é $texto?"
        val fala = listOf(antes, "${maiusc(falada)}. Quanto dá?", dica).filter { it.isNotBlank() }.joinToString(" ")
        return tocarNumero(instrucao, fala, cena, resp, candidatosDe(resp, termos) + extras, "${maiusc(falada)} é igual a ${ext(resp)}!")
    }

    /** Problema contado como historinha: a conta fica na instrução (para o adulto) e a figura do tema na cena. */
    private fun historinha(texto: String, termos: List<Int>, sinais: List<String>, emoji: String, obj: Objeto? = null, extras: Iterable<Int> = emptyList()): Pergunta {
        val resp = calcular(termos, sinais)
        val final = obj?.let { " ${maiusc(it.qtd(resp))}!" } ?: ""
        return tocarNumero(
            "${partesDaConta(termos, sinais).joinToString(" ")} = ?", texto, Cena.Emojis(emoji), resp,
            candidatosDe(resp, termos) + extras, "${maiusc(falaDaConta(termos, sinais))} é igual a ${ext(resp)}.$final",
        )
    }

    /** Conta com um número escondido: "7 + ? = 10" ou "? − 5 = 8". */
    private fun numeroEscondido(a: Int, sinal: String, b: Int, escondePrimeiro: Boolean, antes: String = ""): Pergunta {
        val c = calcular(listOf(a, b), listOf(sinal))
        val certo = if (escondePrimeiro) a else b
        val partes = if (escondePrimeiro) listOf("?", sinal, "$b", "=", "$c") else listOf("$a", sinal, "?", "=", "$c")
        val pergunta = if (escondePrimeiro) "Que número ${sinalFalado(sinal)} ${ext(b)} dá ${ext(c)}?"
        else "${maiusc(ext(a))} ${sinalFalado(sinal)} que número dá ${ext(c)}?"
        return tocarNumero(
            partes.joinToString(" "), listOf(antes, pergunta).filter { it.isNotBlank() }.joinToString(" "), Cena.Sequencia(partes),
            certo, listOf(certo + 1, certo - 1, certo + 10, certo - 10, c, a + b), "${maiusc(falaDaConta(listOf(a, b), listOf(sinal)))} é igual a ${ext(c)}!",
        )
    }

    /** Uma conta "x sinal y" que dá [valor], ou null se o jeito pedido não consegue. */
    private fun interface JeitoDeConta { fun fazer(valor: Int): Pair<Int, Int>? }

    private fun opConta(x: Int, sinal: String, y: Int) = opT("$x $sinal $y", "${ext(x)} ${sinalFalado(sinal)} ${ext(y)}")

    private fun contasQueDao(valor: Int, qtd: Int, sinal: String, jeito: JeitoDeConta): List<Opcao> {
        val r = LinkedHashSet<Opcao>()
        repeat(40) { if (r.size < qtd) jeito.fazer(valor)?.let { (x, y) -> r += opConta(x, sinal, y) } }
        return r.toList()
    }

    /** Somas de duas parcelas de pelo menos [min] cada. */
    private fun somas(min: Int = 1) = JeitoDeConta { v -> if (v < 2 * min) null else (min..v - min).random(random).let { it to v - it } }

    /** Subtrações que dão o valor, começando de no máximo [maxInicio]. */
    private fun subtracoes(maxInicio: Int, minTirar: Int = 1) =
        JeitoDeConta { v -> if (v + minTirar > maxInicio) null else ((v + minTirar)..maxInicio).random(random).let { it to it - v } }

    /** Somas de dois números de dois algarismos sem reagrupar (sem "vai um"). */
    private val somasSemReagrupar = JeitoDeConta { v ->
        val d = v / 10
        val u = v % 10
        if (d < 2 || v > 99) null else {
            val x = (1 until d).random(random) * 10 + (0..u).random(random)
            x to v - x
        }
    }

    /** Somas de dois números de dois algarismos com "vai um" (unidades passam de 10). */
    private val somasComReagrupar = JeitoDeConta { v ->
        val opcoes = (11..v - 11).filter { x -> x % 10 + (v - x) % 10 >= 10 && (v - x) >= 11 }
        if (opcoes.isEmpty()) null else opcoes.random(random).let { it to v - it }
    }

    /** Subtrações de dois algarismos sem reagrupar. */
    private val subtracoesSemReagrupar = JeitoDeConta { v ->
        val opcoes = (v + 11..99).filter { a -> a % 10 >= v % 10 && (a - v) >= 10 }
        if (opcoes.isEmpty()) null else opcoes.random(random).let { it to it - v }
    }

    /** Subtrações de dois algarismos com reagrupar (empresta uma dezena). */
    private val subtracoesComReagrupar = JeitoDeConta { v ->
        val opcoes = (v + 11..99).filter { a -> a % 10 < (a - v) % 10 }
        if (opcoes.isEmpty()) null else opcoes.random(random).let { it to it - v }
    }

    // ------------------------------------------------------------------ desafios de mexer

    private fun pescaria(instrucao: String, fala: String, certos: List<Opcao>, errados: List<Opcao>, reforco: String): Pergunta {
        val peixes = (certos.map { it to true } + errados.map { it to false }).shuffled(random)
        return mexer(instrucao, fala, Pescaria(peixes.map { it.first }, peixes.indices.filter { peixes[it].second }.toSet()), reforco)
    }

    private fun baloes(instrucao: String, fala: String, certos: List<Opcao>, errados: List<Opcao>, reforco: String, emOrdem: Boolean = false): Pergunta {
        val todos = (certos.map { it to true } + errados.map { it to false }).shuffled(random)
        val lista = todos.map { it.first }
        val indices = if (emOrdem) certos.map { lista.indexOf(it) } else lista.indices.filter { todos[it].second }
        return mexer(instrucao, fala, EstourarBaloes(lista, indices, emOrdem), reforco)
    }

    /** Contas que dão [alvo] (certas) misturadas com contas parecidas que dão outro número. */
    private fun contasDoAlvo(alvo: Int, sinal: String, jeito: JeitoDeConta, total: Int = 6): Pair<List<Opcao>, List<Opcao>> {
        val certas = contasQueDao(alvo, (2..3).random(random), sinal, jeito)
        val vizinhos = (listOf(alvo + 1, alvo - 1, alvo + 2, alvo - 2, alvo + 10, alvo - 10, alvo + 3).shuffled(random) + listOf(alvo + 20, alvo - 20, alvo + 5, alvo - 5, alvo + 30)).filter { it >= 0 }
        val erradas = vizinhos.mapNotNull { v -> contasQueDao(v, 1, sinal, jeito).firstOrNull() }.filter { it !in certas }.distinct().take(total - certas.size)
        check(certas.isNotEmpty() && erradas.isNotEmpty() && certas.size + erradas.size >= 3) { "Poucas contas para $alvo" }
        return certas to erradas
    }

    private fun pescariaDeContas(alvo: Int, sinal: String, jeito: JeitoDeConta, tema: Objeto = VN.mar[0]): Pergunta {
        val (certas, erradas) = contasDoAlvo(alvo, sinal, jeito)
        return pescaria(
            "Pesque as contas que dão $alvo",
            "Pesque só os ${tema.plural} com as contas que dão ${ext(alvo)}.",
            certas, erradas, "Todas essas contas dão ${ext(alvo)}!",
        )
    }

    private fun baloesDeContas(alvo: Int, sinal: String, jeito: JeitoDeConta): Pergunta {
        val (certas, erradas) = contasDoAlvo(alvo, sinal, jeito)
        return baloes("Estoure as contas que dão $alvo", "Estoure só os balões com as contas que dão ${ext(alvo)}.", certas, erradas, "Pof! Todas davam ${ext(alvo)}!")
    }

    private fun ligar(instrucao: String, fala: String, pares: List<Pair<Opcao, Opcao>>, sobra: Opcao?, reforco: String): Pergunta {
        val esquerda = pares.map { it.first }
        val direita = (pares.map { it.second } + listOfNotNull(sobra?.takeIf { s -> pares.none { it.second.visual == s.visual } })).shuffled(random)
        val mapa = pares.indices.associateWith { i -> direita.indexOf(pares[i].second) }
        return mexer(instrucao, fala, LigarPares(esquerda, direita, mapa), reforco)
    }

    /** Ligar cada conta ao resultado: [contas] são pares (x, y) com o mesmo [sinal] e resultados diferentes. */
    private fun ligarContas(contas: List<Pair<Int, Int>>, sinal: String, sobra: Int?): Pergunta {
        val pares = contas.map { (x, y) -> opConta(x, sinal, y) to opN(calcular(listOf(x, y), listOf(sinal))) }
        return ligar(
            "Ligue a conta ao resultado", "Ligue cada conta ao seu resultado. Arraste o dedo da conta até o número.",
            pares, sobra?.let(::opN), "Cada conta com o seu resultado!",
        )
    }

    private fun balanca(instrucao: String, fala: String, lado: List<Int>, partes: List<Int>, distratores: List<Int>, reforco: String, figura: String = "🧱", numeros: Boolean = true): Pergunta {
        val pesos = (partes + distratores.filter { it > 0 }.take(8 - partes.size)).shuffled(random)
        return mexer(instrucao, fala, Balanca(lado, pesos, figura, numeros), reforco)
    }

    /** Dois números que somam [alvo] e dois ou três distratores. */
    private fun balancaDaSoma(alvo: Int, minParte: Int, maxDistrator: Int, fala: String, figura: String = "🧱"): Pergunta {
        val x = (minParte..alvo - minParte).random(random)
        val distratores = (1..maxDistrator).filter { it != x && it != alvo - x }.sorteia((2..3).random(random))
        return balanca("Deixe a balança reta: $alvo", fala, listOf(alvo), listOf(x, alvo - x), distratores, "A balança ficou reta: ${ext(x)} mais ${ext(alvo - x)} é ${ext(alvo)}!", figura)
    }

    /**
     * Dominó de contas: a ponta mostra um número; cada peça junta a conta que dá aquele número com o
     * número seguinte da corrente. Os números da corrente são diferentes, então em cada jogada só uma peça serve.
     */
    private fun dominoDeContas(valores: List<Int>, sinal: String, jeito: JeitoDeConta, sobras: List<Int>, fala: String = ""): Pergunta {
        val numero = { v: Int -> MeiaPeca("$v", Visual.Texto("$v"), listOf(pt(ext(v)))) }
        val contaDe = { v: Int ->
            val (x, y) = jeito.fazer(v) ?: error("Sem conta para $v")
            MeiaPeca("$v", Visual.Texto("$x $sinal $y"), listOf(pt("${ext(x)} ${sinalFalado(sinal)} ${ext(y)}")))
        }
        val corrente = (0 until valores.size - 1).map { i -> PecaDomino(contaDe(valores[i]), numero(valores[i + 1])) }
        val livres = sobras.filter { it !in valores }.distinct()
        val extras = livres.chunked(2).filter { it.size == 2 }.take(2).map { (a, b) -> PecaDomino(contaDe(a), numero(b)) }
        return mexer(
            "Dominó das contas",
            fala.ifBlank { "Dominó! Ache a peça com a conta que dá o número da ponta e encaixe. A outra metade vira a nova ponta." },
            Domino(numero(valores[0]), (corrente + extras).shuffled(random), valores.size - 1),
            "Fileira completa!",
        )
    }

    private fun cartasEmOrdem(instrucao: String, fala: String, emOrdem: List<Opcao>, reforco: String): Pergunta {
        val cartas = emOrdem.shuffled(random)
        return mexer(instrucao, fala, CartasViradas(cartas, emOrdem.map { cartas.indexOf(it) }), reforco)
    }

    private fun cartasDeNumeros(nums: List<Int>, crescente: Boolean = true): Pergunta {
        val ordem = if (crescente) nums.sorted() else nums.sortedDescending()
        val palavra = if (crescente) "do menor para o maior" else "do maior para o menor"
        return cartasEmOrdem(
            "Vire as cartas $palavra",
            "As cartas estão viradas. Vire uma por vez, $palavra. Comece pelo ${ext(ordem.first())}.",
            ordem.map(::opN), maiusc(ordem.joinToString(", ") { ext(it) }) + "!",
        )
    }

    private fun tremDeNumeros(nums: List<Int>, fixos: Int, instrucao: String, fala: String, extras: List<Int> = emptyList()): Pergunta {
        val fx = nums.indices.toList().sorteia(fixos).toSet()
        val ex = extras.filter { it !in nums }.distinct().take(3).map(::opN)
        return mexer(instrucao, fala, Trem(nums.map(::opN), fx, ex), maiusc(nums.joinToString(", ") { ext(it) }) + "! Piuí!")
    }

    /** Pintar por código: cada cor do desenho ganha um número; as casinhas mostram contas (ou marcas) que dão aquele número. */
    private fun pintar(instrucao: String, fala: String, faixa: List<Int>, rotulo: (Int) -> String, reforco: String = ""): Pergunta {
        val d = VN.desenhos.um()
        val letras = d.cores.keys.sorted()
        val valores = faixa.distinct().sorteia(letras.size + 1)
        val codigoDe = letras.zip(valores).toMap()
        val casas = d.linhas.joinToString("").map { ch ->
            codigoDe[ch]?.let { CasaDePintar(rotulo(it), "$it") } ?: CasaDePintar("", null)
        }
        val nomeDaCor = VN.tintas.toMap()
        val usadas = letras.map { d.cores.getValue(it) }
        val extra = VN.tintas.first { it.first !in usadas }
        val legenda = letras.map { l ->
            val argb = d.cores.getValue(l)
            CorDaLegenda("${codigoDe.getValue(l)}", argb, listOf(pt("${nomeDaCor[argb] ?: "cor"}: ${ext(codigoDe.getValue(l))}")))
        } + CorDaLegenda("${valores.last()}", extra.first, listOf(pt("${extra.second}: ${ext(valores.last())}")))
        return mexer(instrucao, fala, PintarPorCodigo(d.linhas[0].length, casas, legenda.shuffled(random)), reforco.ifBlank { "Olha só: ${d.nome}!" })
    }

    /** Moedas e cédulas que pagam [centavos] certinho (do maior valor para o menor). */
    private fun decompor(centavos: Int): List<Int> {
        var resto = centavos
        val r = mutableListOf<Int>()
        for (v in Mercadinho.VALORES.sortedDescending()) while (resto >= v) { r += v; resto -= v }
        return r
    }

    private fun mercadinho(preco: Int, troco: Boolean = false, pago: Int = 0): Pergunta {
        val p = VN.produtos.um()
        val alvo = if (troco) pago - preco else preco
        val base = decompor(alvo)
        val extras = Mercadinho.VALORES.filter { it <= maxOf(200, alvo) }.toList().sorteia(2)
        val dinheiro = (base + extras).take(maxOf(base.size, minOf(10, base.size + 2))).shuffled(random)
        val fala = if (troco) {
            "No mercadinho, ${p.nome} custa ${VN.dinheiroFalado(preco)}. O cliente pagou com ${VN.dinheiroFalado(pago)}. Arraste o troco para o balcão."
        } else {
            "No mercadinho, ${p.nome} custa ${VN.dinheiroFalado(preco)}. Arraste as moedas e as notas para pagar certinho."
        }
        return mexer(
            if (troco) "Troco de ${VN.dinheiroEscrito(pago)} para ${VN.dinheiroEscrito(preco)}" else "Pague ${VN.dinheiroEscrito(preco)}",
            fala, Mercadinho(p.emoji, p.nome.substringAfter(' '), preco, dinheiro, troco, if (troco) pago else 0),
            if (troco) "O troco é ${VN.dinheiroFalado(alvo)}!" else "Pagou ${VN.dinheiroFalado(preco)}!",
        )
    }

    private fun relogio(h: Int, m: Int, digital: Boolean): Pergunta = mexer(
        "Marque ${VN.horaEscrita(h, m)}",
        "Arraste os ponteiros do relógio até marcar ${VN.falaDaHora(h, m)}. O ponteiro pequeno mostra a hora e o grande, os minutos.",
        Relogio(h, m, digital), "${maiusc(VN.falaDaHora(h, m))}! Cuco!",
    )

    /** Caça na cena do mundo [mundo] (de 6 a 25): figuras espalhadas numa grade de 4 por 3, com um tremidinho. */
    private fun caca(mundo: Int, instrucao: String, fala: String, certos: List<Opcao>, outros: List<Opcao>, reforco: String): Pergunta {
        val todos = (certos.map { it to true } + outros.map { it to false }).shuffled(random)
        val casas = (0 until 12).toList().sorteia(todos.size)
        val itens = todos.mapIndexed { i, (op, _) ->
            val c = casas[i]
            val x = ((c % 4) + 0.5f) / 4f + (random.nextFloat() - 0.5f) * 0.08f
            val y = ((c / 4) + 0.5f) / 3f + (random.nextFloat() - 0.5f) * 0.08f
            ItemNaCena(op, x.coerceIn(0.05f, 0.95f), y.coerceIn(0.05f, 0.95f), 0.8f + random.nextFloat() * 0.4f)
        }
        return mexer(instrucao, fala, CacaNaCena(mundo - 1, itens, todos.indices.filter { todos[it].second }.toSet()), reforco)
    }

    private fun cestaDe(emoji: String, disponiveis: Int, alvo: Int, instrucao: String, fala: String, reforco: String) =
        mexer(instrucao, fala, Atividade.Cesta(List(disponiveis) { emoji }, alvo), reforco)

    // ================================================================== MUNDO 6: FAZENDA (51 a 60)
    // Números ordinais (1º ao 10º) e comparar com os sinais maior, menor e igual, até 30.

    /** Fila de bichos da fazenda: quem está em tal lugar, ou em que lugar está tal bicho. */
    private fun ordinalNaFila(tamanhos: IntRange, parecidas: Boolean): Pergunta {
        val n = tamanhos.random(random)
        val fila = VN.fazenda.sorteia(n)
        val pos = (0 until n).random(random)
        val certo = fila[pos]
        val cena = Cena.Sequencia(fila.map { it.emoji })
        return if (random.nextBoolean()) {
            val outros = (0 until n).filter { it != pos }
                .ordenarUmaVez { if (parecidas) kotlin.math.abs(it - pos) + random.nextDouble() else random.nextDouble() }
                .take(3).map { fila[it] }
            tocar(
                "Quem é o ${VN.ordinalEscrito(pos + 1)} da fila?",
                "A fila começa aqui na esquerda. Quem é o ${VN.ordinal(pos + 1)} da fila?",
                cena, opE(certo.emoji, certo.singular), outros.map { opE(it.emoji, it.singular) },
                "Isso! ${maiusc(if (certo.feminino) "a" else "o")} ${certo.singular} é ${if (certo.feminino) "a" else "o"} ${VN.ordinal(pos + 1, certo.feminino)}!",
            )
        } else {
            val certoLugar = pos + 1
            val outros = errados(certoLugar, if (parecidas) listOf(certoLugar - 1, certoLugar + 1, certoLugar + 2, certoLugar - 2) else (1..maxOf(n, 5)).toList(), 1, 10)
            val op = { k: Int -> opT(VN.ordinalEscrito(k), "${VN.ordinal(k)} lugar") }
            tocar(
                "Em que lugar está ${if (certo.feminino) "a" else "o"} ${certo.singular}?",
                "A fila começa aqui na esquerda. Em que lugar está ${if (certo.feminino) "a" else "o"} ${certo.singular}?",
                cena, op(certoLugar), outros.map(op), "No ${VN.ordinal(certoLugar)} lugar!",
            )
        }
    }

    private fun tremDosLugares(ate: Int): Pergunta {
        val n = (4..ate).random(random)
        val vagoes = (1..n).map { opT(VN.ordinalEscrito(it), VN.ordinal(it)) }
        return mexer(
            "Trem dos lugares", "Coloque os vagões em ordem: primeiro, segundo, terceiro... O primeiro vai logo atrás da locomotiva.",
            Trem(vagoes, setOf(0)), maiusc((1..n).joinToString(", ") { VN.ordinal(it) }) + "! Piuí!",
        )
    }

    private fun tremCrescente(max: Int, qtd: Int, fixos: Int): Pergunta {
        val nums = (1..max).toList().sorteia(qtd).sorted()
        return tremDeNumeros(nums, fixos, "Do menor para o maior", "Coloque os vagões do menor número para o maior. O menor vai logo atrás da locomotiva.")
    }

    private fun pescariaDosLugares(): Pergunta {
        val n = (5..7).random(random)
        val peixes = (1..n).map { opT(VN.ordinalEscrito(it), VN.ordinal(it)) }
        val certos = (1..n).toList().sorteia(2).sorted()
        return pescaria(
            "Pesque o ${VN.ordinalEscrito(certos[0])} e o ${VN.ordinalEscrito(certos[1])}",
            "Pesque os peixes que levam o ${VN.ordinal(certos[0])} e o ${VN.ordinal(certos[1])}.",
            certos.map { peixes[it - 1] }, peixes.filterIndexed { i, _ -> i + 1 !in certos }, "Pescou o ${VN.ordinal(certos[0])} e o ${VN.ordinal(certos[1])}!",
        )
    }

    private fun sinalDe(a: Int, b: Int) = when { a < b -> "<"; a > b -> ">"; else -> "=" }
    private fun falaDoSinal(s: String) = when (s) { "<" -> "é menor que"; ">" -> "é maior que"; else -> "é igual a" }

    /** Qual frase com sinal está certa? Quatro frases, só uma verdadeira. A esquerda pode ser uma conta. */
    private fun fraseComSinal(valorEsq: Int, textoEsq: String, falaEsq: String, b: Int, max: Int): Pergunta {
        val certoSinal = sinalDe(valorEsq, b)
        val frase = { s: String, d: Int -> opT("$textoEsq $s $d", "$falaEsq ${falaDoSinal(s)} ${ext(d)}") }
        val mesmas = listOf("<", ">", "=").filter { it != certoSinal }.map { frase(it, b) }
        val outra = listOf(b + 1, b - 1, b + 2, b - 2).filter { it in 1..max }.flatMap { d ->
            listOf("<", ">", "=").filter { sinalDe(valorEsq, d) != it }.map { frase(it, d) }
        }.um()
        return tocar("Qual está certo?", "Qual frase está certa? Ouça cada uma.", Cena.Nenhuma, frase(certoSinal, b), mesmas + outra, "Isso! ${maiusc(falaEsq)} ${falaDoSinal(certoSinal)} ${ext(b)}.")
    }

    private fun compararSinais(max: Int, perto: Boolean): Pergunta {
        if (random.nextInt(3) == 0) {
            // Qual número deixa a frase certa: a > ?
            val a = (2..max - 3).random(random)
            val certo = (1 until a).random(random)
            val erradas = (a..a + 3).toList().sorteia(3)
            return tocar(
                "$a > ?", "${maiusc(ext(a))} é maior que que número? A boca do sinal fica aberta para o maior.",
                Cena.Sequencia(listOf("$a", ">", "?")), opN(certo), erradas.map(::opN), "Isso! ${maiusc(ext(a))} é maior que ${ext(certo)}.",
            )
        }
        val a = (1..max).random(random)
        val b = if (perto) (a - 3..a + 3).filter { it in 1..max }.random(random) else (1..max).random(random)
        return fraseComSinal(a, "$a", ext(a), b, max)
    }

    private fun fraseComConta(max: Int): Pergunta {
        val x = (2..max / 2 - 2).random(random)
        val y = (1..max / 2 - 2).random(random)
        val b = (x + y + (-2..2).random(random)).coerceIn(1, max)
        return fraseComSinal(x + y, "$x + $y", "${ext(x)} mais ${ext(y)}", b, max)
    }

    /** Revisão do mundo 5: fila de números de 2 em 2, 5 em 5 ou 10 em 10, sem passar de [max]. */
    private fun pulosAte(max: Int): Pergunta {
        val passo = listOf(2, 5, 10).um()
        val inicio = (0..max - passo * 3).filter { it % passo == 0 || passo == 2 }.random(random)
        val fila = List(4) { inicio + it * passo }
        val resp = fila.last()
        return tocarNumero(
            "Que número vem depois?", "Pulando de ${ext(passo)} em ${ext(passo)}: " + fila.dropLast(1).joinToString(", ") { ext(it) } + ". Qual vem depois?",
            Cena.Sequencia(fila.dropLast(1).map { "$it" } + "?"), resp, listOf(resp + 1, resp - 1, resp + passo, resp - passo), maiusc(fila.joinToString(", ") { ext(it) }) + "!", 0, max,
        )
    }

    private fun lugarDepois(): Pergunta {
        val n = (1..9).random(random)
        val depois = random.nextBoolean() || n == 1
        val certo = if (depois) n + 1 else n - 1
        val op = { k: Int -> opT(VN.ordinalEscrito(k), VN.ordinal(k)) }
        return tocar(
            if (depois) "Depois do ${VN.ordinalEscrito(n)}?" else "Antes do ${VN.ordinalEscrito(n)}?",
            "Que lugar vem ${if (depois) "depois" else "antes"} do ${VN.ordinal(n)}?",
            Cena.Nenhuma, op(certo), errados(certo, listOf(n, certo + 1, certo - 1, n + 2), 1, 10).map(op), "${maiusc(VN.ordinal(certo))}!",
        )
    }

    private val mundo6: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Primeiro, segundo, terceiro", "🥇") { ordinalNaFila(4..5, parecidas = false) }
        val f2 = ModeloDeFase("Trem dos lugares", "🚂") { sorteio({ tremDosLugares(6) }, { tremDosLugares(5) }, { ordinalNaFila(4..5, false) }) }
        val f3 = ModeloDeFase("Lugares até o décimo", "🐄") { ordinalNaFila(7..10, parecidas = true) }
        val f4 = ModeloDeFase("Pescaria dos lugares", "🎣") { sorteio({ pescariaDosLugares() }, { pescariaDosLugares() }, { ordinalNaFila(6..8, true) }) }
        val f5 = marco("Bandeirinha da fazenda", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Maior, menor ou igual", "🐷") { compararSinais(20, perto = false) }
        val f7 = ModeloDeFase("Balança da fazenda", "⚖️") {
            sorteio(
                { balancaDaSoma((6..15).random(random), 1, 9, "A balança da fazenda precisa ficar reta. Coloque pesos do outro lado até ficar igual.", "🥚") },
                { balancaDaSoma((6..15).random(random), 1, 9, "Deixe os dois pratos com o mesmo peso. Igual dos dois lados!", "🌽") },
                { compararSinais(20, perto = true) },
            )
        }
        val f8 = ModeloDeFase("Lugares e pulos", "🔁") {
            sorteio({ ordinalNaFila(5..8, true) }, { compararSinais(30, true) }, { pulosAte(30) }, { problema(soma = random.nextBoolean()) })
        }
        val f9 = ModeloDeFase("Sinais espertos", "🧠") { sorteio({ fraseComConta(20) }, { lugarDepois() }, { compararSinais(30, perto = true) }) }
        val f10 = ModeloDeFase("Chefão: a vaca gigante", "👑") {
            sorteio({ tremCrescente(30, 5, 1) }, { tremDosLugares(7) }, { balancaDaSoma((8..18).random(random), 2, 9, "A vaca gigante quer a balança reta! Coloque os pesos certinhos.", "🐄") }, { fraseComConta(30) }, { ordinalNaFila(8..10, true) })
        }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 7: FUNDO DO MAR (61 a 70)
    // Fatos de 10 (pares que somam 10) e completar a dezena; somas até 20.

    private fun faltaParaDez(comFiguras: Boolean): Pergunta {
        val obj = VN.mar.um()
        val a = (1..9).random(random)
        val cena = if (comFiguras) Cena.Grade(5, List(10) { if (it < a) obj.emoji else "" }) else Cena.Sequencia(listOf("$a", "+", "?", "=", "10"))
        return tocarNumero(
            "$a + ? = 10",
            if (comFiguras) "Tem ${obj.qtd(a)} nas dez casinhas. ${obj.quantos()} faltam para completar dez?" else "${maiusc(ext(a))} mais quanto dá dez?",
            cena, 10 - a, listOf(10 - a + 1, 10 - a - 1, 10 - a + 2, a, 10), "${maiusc(ext(a))} mais ${ext(10 - a)} é igual a dez! São amigos do dez.", 0, 10,
        )
    }

    private fun balancaDoDez(): Pergunta = balancaDaSoma(10, 1, 9, "Do outro lado tem dez. Coloque dois pesos que juntos fazem dez.", "🐚")

    private fun completarDezena(): Pergunta {
        val alvo = listOf(10, 20).um()
        val a = (alvo - 9..alvo - 1).random(random)
        return if (random.nextBoolean()) numeroEscondido(a, "+", alvo - a, escondePrimeiro = false, antes = "Complete a dezena.")
        else tocarNumero(
            "Quanto falta de $a para $alvo?", "Quanto falta de ${ext(a)} para chegar no ${ext(alvo)}?",
            Cena.Sequencia(listOf("$a", "→", "$alvo")), alvo - a, listOf(alvo - a + 1, alvo - a - 1, alvo - a + 2), "Faltam ${ext(alvo - a)}!", 0, 20,
        )
    }

    /** Somar passando pelo 10: 8 + 7 = 8 + 2 + 5. */
    private fun somarPassandoDez(): Pergunta {
        val a = (6..9).random(random)
        val b = (11 - a..9).random(random)
        return conta(listOf(a, b), listOf("+"), dica = "Dica: ${ext(a)} mais ${ext(10 - a)} faz dez, e ainda sobram ${ext(b - (10 - a))}.")
    }

    private fun ligarAmigosDoDez(): Pergunta {
        val nums = (1..9).toList().sorteia((3..4).random(random))
        val sobra = (1..9).filter { it !in nums.map { n -> 10 - n } }.randomOrNull(random)
        return ligar(
            "Ligue os amigos do 10", "Ligue cada número ao amigo que, junto com ele, faz dez.",
            nums.map { opN(it) to opN(10 - it) }, sobra?.let(::opN), "Todos os amigos do dez juntos!",
        )
    }

    private fun dominoDoMar(max: Int): Pergunta {
        val valores = (4..max).toList().sorteia((3..4).random(random))
        val sobras = (4..max).filter { it !in valores }.sorteia(2)
        return dominoDeContas(valores, "+", somas(1), sobras)
    }

    private val mundo7: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Amigos do 10", "🐠") { faltaParaDez(comFiguras = true) }
        val f2 = ModeloDeFase("Balança do 10", "⚖️") { sorteio({ balancaDoDez() }, { balancaDoDez() }, { faltaParaDez(true) }) }
        val f3 = ModeloDeFase("Quanto falta para 10", "🐙") { faltaParaDez(comFiguras = false) }
        val f4 = ModeloDeFase("Pescaria do 10", "🎣") { sorteio({ pescariaDeContas(10, "+", somas(1)) }, { pescariaDeContas(10, "+", somas(1)) }, { faltaParaDez(false) }) }
        val f5 = marco("Bandeirinha do mar", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Completar a dezena", "🦀") { completarDezena() }
        val f7 = ModeloDeFase("Dominó do mar", "🁫") { sorteio({ dominoDoMar(12) }, { dominoDoMar(15) }, { completarDezena() }) }
        val f8 = ModeloDeFase("Dez e revisão", "🔁") { sorteio({ faltaParaDez(false) }, { completarDezena() }, { revisao(mundo6) }) }
        val f9 = ModeloDeFase("Passando pelo 10", "🐬") { sorteio({ somarPassandoDez() }, { somarPassandoDez() }, { completarDezena() }) }
        val f10 = ModeloDeFase("Chefão: o polvo", "👑") {
            sorteio({ balancaDaSoma((10..18).random(random), 3, 9, "O polvo quer a balança reta! Coloque dois pesos.", "🐙") }, { pescariaDeContas(listOf(10, 12, 15).um(), "+", somas(2)) }, { ligarAmigosDoDez() })
        }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 8: SELVA (71 a 80)
    // Subtração até 20 e família de fatos; metade das perguntas sem figuras.

    private fun tirarNaSelva(comFiguras: Boolean): Pergunta {
        val obj = VN.selva.um()
        val a = if (comFiguras) (5..12).random(random) else (11..20).random(random)
        val b = (1..minOf(9, a - 1)).random(random)
        if (comFiguras) {
            return tocarNumero(
                "$a − $b = ?", "Tinha ${obj.qtd(a)}. ${if (b == 1) "Foi" else "Foram"} embora ${ext(b, obj.feminino)}. ${obj.quantos()} ficaram?",
                Cena.Conta(obj.emoji, a, b, soma = false), a - b, listOf(a - b + 1, a - b - 1, a - b + 2, a, b), "${maiusc(ext(a))} menos ${ext(b)} é igual a ${ext(a - b)}!", 0, 20,
            )
        }
        return conta(listOf(a, b), listOf("−"), dica = "Dica: comece no ${ext(a)} e conte para trás.")
    }

    private fun cestaDoQueSobrou(): Pergunta {
        val obj = listOf(VN.selva[1], VN.selva[5]).um()
        val a = (8..15).random(random)
        val b = (2..minOf(8, a - 2)).random(random)
        return cestaDe(
            obj.emoji, a, a - b, "$a − $b: coloque o que sobrou",
            "O macaco tinha ${obj.qtd(a)} e comeu ${ext(b, obj.feminino)}. Coloque na cesta ${if (obj.feminino) "as" else "os"} que sobraram. Depois toque no certinho.",
            "Sobraram ${obj.qtd(a - b)}!",
        )
    }

    /** Família de fatos: 8 + 5 = 13, então 13 − 5 = 8. */
    private fun familiaDeFatos(): Pergunta {
        val x = (3..9).random(random)
        val y = (2..9).filter { it != x }.random(random)
        val s = x + y
        val tiraX = random.nextBoolean()
        val tirado = if (tiraX) x else y
        val resp = s - tirado
        return tocarNumero(
            "$s − $tirado = ?", "Se ${ext(x)} mais ${ext(y)} é ${ext(s)}, quanto é ${ext(s)} menos ${ext(tirado)}?",
            Cena.Sequencia(listOf("$s", "−", "$tirado", "=", "?")), resp, listOf(s, tirado, resp + 1, resp - 1),
            "${maiusc(ext(s))} menos ${ext(tirado)} é ${ext(resp)}. É a mesma família: ${ext(x)}, ${ext(y)} e ${ext(s)}!", 0, 20,
        )
    }

    private fun ligarSubtracoes(): Pergunta {
        val resultados = (1..10).toList().sorteia((3..4).random(random))
        val contas = resultados.map { r -> val a = (r + 1..20).random(random); a to a - r }
        val sobra = (1..10).filter { it !in resultados }.um()
        return ligarContas(contas, "−", sobra)
    }

    private fun montarFamilia(): Pergunta {
        val x = (3..9).random(random)
        val y = (2..9).filter { it != x }.random(random)
        val s = x + y
        val palavras = listOf("$s", "−", "$y", "=", "$x")
        val extras = listOf("+", "${x + 1}", "${s + 1}").filter { it !in palavras }.take(2)
        return mexer(
            "Monte a conta: $s − $y = $x", "Monte a conta da família: ${ext(s)} menos ${ext(y)} é igual a ${ext(x)}. Arraste os cartões na ordem.",
            MontarFrase(palavras, extras), "${maiusc(ext(s))} menos ${ext(y)} é igual a ${ext(x)}!",
        )
    }

    private fun dominoDaSelva(): Pergunta {
        val valores = (1..12).toList().sorteia((3..4).random(random))
        return dominoDeContas(valores, "−", subtracoes(20, 2), (1..12).filter { it !in valores }.sorteia(2))
    }

    private val mundo8: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Tirar na selva", "🐒") { tirarNaSelva(comFiguras = true) }
        val f2 = ModeloDeFase("Balões de tirar", "🎈") { sorteio({ baloesDeContas((3..9).random(random), "−", subtracoes(15, 2)) }, { baloesDeContas((3..9).random(random), "−", subtracoes(15, 2)) }, { tirarNaSelva(true) }) }
        val f3 = ModeloDeFase("Tirar sem figuras", "🦜") { tirarNaSelva(comFiguras = false) }
        val f4 = ModeloDeFase("Cesta das bananas", "🍌") { sorteio({ cestaDoQueSobrou() }, { cestaDoQueSobrou() }, { tirarNaSelva(false) }) }
        val f5 = marco("Bandeirinha da selva", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Família de fatos", "👨‍👩‍👧") { familiaDeFatos() }
        val f7 = ModeloDeFase("Ligar as contas", "🔗") { sorteio({ ligarSubtracoes() }, { ligarSubtracoes() }, { familiaDeFatos() }) }
        val f8 = ModeloDeFase("Tirar e revisão", "🔁") { sorteio({ tirarNaSelva(random.nextBoolean()) }, { familiaDeFatos() }, { revisao(mundo7) }) }
        val f9 = ModeloDeFase("Quanto foi tirado?", "🐍") {
            sorteio(
                { val a = (11..20).random(random); numeroEscondido(a, "−", (2..9).random(random), escondePrimeiro = false) },
                { montarFamilia() }, { tirarNaSelva(false) },
            )
        }
        val f10 = ModeloDeFase("Chefão: o macaco rei", "👑") {
            sorteio({ baloesDeContas((4..12).random(random), "−", subtracoes(20, 3)) }, { dominoDaSelva() }, { familiaDeFatos() }, { tirarNaSelva(false) })
        }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 9: POLO NORTE (81 a 90)
    // Números até 100: ler, escrever, sucessor, antecessor e reta numérica.

    private fun lerAte100(): Pergunta {
        val n = (21..99).random(random)
        val trocado = (n % 10) * 10 + n / 10
        val cand = listOfNotNull(trocado.takeIf { it != n && n % 10 != 0 }) + listOf(n + 1, n - 1, n + 10, n - 10)
        return tocarNumero("Toque no $n", "Toque no número ${ext(n)}.", Cena.Nenhuma, n, cand, "${maiusc(ext(n))}!", 10, 100)
    }

    private fun tremConsecutivo(max: Int): Pergunta {
        val inicio = (11..max - 4).random(random)
        return tremDeNumeros((inicio until inicio + 5).toList(), 2, "Trem de $inicio a ${inicio + 4}", "Os vagões vão em ordem, de um em um, começando no ${ext(inicio)}. Arraste os que faltam.")
    }

    private fun sucessorAntecessor(parecidas: Boolean): Pergunta {
        val n = if (parecidas) (listOf(19, 29, 39, 49, 59, 69, 79, 89, 99, 20, 30, 40, 50, 60, 70, 80, 90) + (21..98).toList().sorteia(4)).um() else (15..98).random(random)
        val depois = if (n == 99) true else random.nextBoolean()
        val certo = if (depois) n + 1 else n - 1
        val cand = listOf(n, certo + 1, certo - 1, certo + 10, certo - 10, n + 10)
        return tocarNumero(
            if (depois) "Depois do $n vem?" else "Antes do $n vem?",
            if (depois) "Que número vem logo depois do ${ext(n)}?" else "Que número vem logo antes do ${ext(n)}?",
            Cena.Sequencia(if (depois) listOf("$n", "?") else listOf("?", "$n")), certo, cand,
            if (depois) "Depois do ${ext(n)} vem o ${ext(certo)}!" else "Antes do ${ext(n)} vem o ${ext(certo)}!", 10, 100,
        )
    }

    /** Reta numérica: um número escondido entre outros, de 1 em 1 ou de 10 em 10. */
    private fun retaNumerica(): Pergunta {
        val passo = listOf(1, 1, 10, 5).um()
        val inicio = if (passo == 1) (10..95).random(random) else passo * (1..(100 / passo - 3)).random(random)
        val reta = List(4) { inicio + it * passo }.filter { it <= 100 }
        val i = (1 until reta.size).random(random)
        val certo = reta[i]
        return tocarNumero(
            "Que número falta na reta?", "Na reta os números vão de ${ext(passo)} em ${ext(passo)}. Que número está escondido?",
            Cena.Sequencia(reta.mapIndexed { k, v -> if (k == i) "?" else "$v" }), certo, listOf(certo + 1, certo - 1, certo + passo, certo - passo, certo + 10), "É o ${ext(certo)}!", 0, 100,
        )
    }

    private fun cartasAte100(): Pergunta {
        val inicio = (10..94).random(random)
        return cartasDeNumeros((inicio until inicio + (4..6).random(random)).toList())
    }

    private fun escreverNumero(): Pergunta = sorteio(
        {
            val n = (21..99).random(random)
            val dezenas = random.nextBoolean()
            val alg = if (dezenas) n / 10 else n % 10
            mexer(
                "Trace o algarismo das ${if (dezenas) "dezenas" else "unidades"} de $n",
                "No número ${ext(n)}, qual é o algarismo das ${if (dezenas) "dezenas" else "unidades"}? Passe o dedo por cima dele.",
                Atividade.Tracar("$alg"), "O algarismo das ${if (dezenas) "dezenas" else "unidades"} é ${ext(alg)}!", Cena.Palavra("$n"),
            )
        },
        {
            val (n, partes) = VN.nomesEmPedacos.entries.map { it.toPair() }.um()
            val extras = VN.nomesEmPedacos.values.flatten().distinct().filter { it !in partes }.sorteia(2)
            mexer("Escreva o $n", "Escreva o número ${ext(n)} com as peças.", Ditado(partes, extras), "${maiusc(ext(n))}: ${partes.joinToString("")}!", Cena.Palavra("$n"))
        },
    )

    private fun escondeNoGelo(): Pergunta {
        val n = (15..98).random(random)
        val depois = random.nextBoolean()
        val certo = if (depois) n + 1 else n - 1
        val outros = errados(certo, listOf(n, certo + 1, certo - 1, certo + 10, certo - 10), 10, 100)
        val todos = (outros + certo).shuffled(random)
        return mexer(
            "Onde está o pinguim?", "O pinguim se escondeu atrás do iglu com o número que vem ${if (depois) "depois" else "antes"} do ${ext(n)}. Toque nesse iglu.",
            EscondeEsconde("🐧", todos.map(::opN), todos.indexOf(certo)), "Achou! Era o ${ext(certo)}.",
        )
    }

    private fun sigaOsNumeros(): Pergunta {
        val nums = (1..9).toList().sorteia(4).map { it * 10 }
        val seq = List((3..4).random(random)) { (0..3).random(random) }
        return mexer(
            "Repita os números", "Escute e repita na mesma ordem os números que acenderem.",
            SigaASequencia(nums.map(::opN), VN.coresDosBotoes, seq), "Que memória! " + seq.joinToString(", ") { ext(nums[it]) } + ".",
        )
    }

    private val mundo9: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Números até 100", "🐧") { lerAte100() }
        val f2 = ModeloDeFase("Trem do gelo", "🚂") { sorteio({ tremConsecutivo(100) }, { tremConsecutivo(100) }, { lerAte100() }) }
        val f3 = ModeloDeFase("Antes e depois até 100", "❄️") { sucessorAntecessor(parecidas = true) }
        val f4 = ModeloDeFase("Cartas geladas", "🃏") { sorteio({ cartasAte100() }, { cartasAte100() }, { sucessorAntecessor(true) }) }
        val f5 = marco("Bandeirinha do polo", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Reta numérica", "📏") { retaNumerica() }
        val f7 = ModeloDeFase("Escrever números", "✍️") { sorteio({ escreverNumero() }, { escondeNoGelo() }, { retaNumerica() }) }
        val f8 = ModeloDeFase("Até 100 e revisão", "🔁") { sorteio({ lerAte100() }, { sigaOsNumeros() }, { revisao(mundo8) }) }
        val f9 = ModeloDeFase("Vizinhos difíceis", "🧊") { sorteio({ sucessorAntecessor(true) }, { retaNumerica() }, { escondeNoGelo() }) }
        val f10 = ModeloDeFase("Chefão: o urso polar", "👑") { sorteio({ tremConsecutivo(100) }, { cartasAte100() }, { sucessorAntecessor(true) }, { lerAte100() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 10: CIRCO (91 a 100)
    // Valor posicional: decompor 45 em 40 + 5, material dourado até 99.

    private fun materialDouradoAte99(): Pergunta {
        val d = (1..9).random(random)
        val u = (0..9).random(random)
        val n = d * 10 + u
        val trocado = u * 10 + d
        val cand = listOfNotNull(trocado.takeIf { u > 0 && it != n }) + listOf(n + 10, n - 10, n + 1, n - 1, d + u)
        return tocarNumero(
            "Quanto é?", "Cada barrinha vale dez e cada cubinho vale um. Que número o palhaço montou?",
            Cena.MaterialDourado(d, u), n, cand, "${maiusc(ext(n))}! ${maiusc(ext(d, true))} ${if (d == 1) "dezena" else "dezenas"} e ${ext(u, true)} ${if (u == 1) "unidade" else "unidades"}.", 1, 99,
        )
    }

    private fun balancaDecompor(): Pergunta {
        val d = (2..9).random(random)
        val u = (1..9).random(random)
        val n = d * 10 + u
        val distr = listOf((d - 1) * 10, (d + 1) * 10, u + 1, u - 1).filter { it in 1..90 && it != d * 10 && it != u }.sorteia(2)
        return balanca(
            "Equilibre o $n", "Do outro lado tem ${ext(n)}. Coloque uma dezena redonda e as unidades para deixar a balança reta.",
            listOf(n), listOf(d * 10, u), distr, "${maiusc(ext(n))} é ${ext(d * 10)} mais ${ext(u)}!",
        )
    }

    private fun decomporTocar(): Pergunta {
        val d = (1..9).random(random)
        val u = (1..9).random(random)
        val n = d * 10 + u
        return if (random.nextBoolean()) numeroEscondido(d * 10, "+", u, escondePrimeiro = false, antes = "Desmonte o ${ext(n)} em dezenas e unidades.")
        else {
            val certo = d * 10
            val cand = listOf(d, n, u, u * 10, certo + 10).filter { it != certo && it <= 99 }.distinct()
            tocar(
                "Quanto vale o $d em $n?", "No número ${ext(n)}, quanto vale o algarismo ${ext(d)}?",
                Cena.Palavra("$n"), opN(certo), errados(certo, cand, 0, 99).map(::opN), "O ${ext(d)} está nas dezenas: vale ${ext(certo)}!",
            )
        }
    }

    private fun ligarDecomposicao(): Pergunta {
        val nums = (11..99).filter { it % 10 != 0 }.sorteia((3..4).random(random))
        val pares = nums.map { n -> opN(n) to opT("${n / 10 * 10} + ${n % 10}", "${ext(n / 10 * 10)} mais ${ext(n % 10)}") }
        val s = (11..99).filter { it % 10 != 0 && it !in nums }.um()
        return ligar("Ligue o número às partes", "Ligue cada número com as suas dezenas e unidades.", pares, opT("${s / 10 * 10} + ${s % 10}", "${ext(s / 10 * 10)} mais ${ext(s % 10)}"), "Cada número com as suas partes!")
    }

    private fun comporNumero(dificil: Boolean): Pergunta {
        val d = (1..9).random(random)
        val u = (0..9).filter { it != d }.random(random)
        val n = d * 10 + u
        val trocado = u * 10 + d
        val ordemTrocada = dificil && random.nextBoolean()
        val texto = if (ordemTrocada) "${ext(u, true)} ${if (u == 1) "unidade" else "unidades"} e ${ext(d, true)} ${if (d == 1) "dezena" else "dezenas"}"
        else "${ext(d, true)} ${if (d == 1) "dezena" else "dezenas"} e ${ext(u, true)} ${if (u == 1) "unidade" else "unidades"}"
        return tocarNumero(
            "${if (ordemTrocada) "$u u e $d d" else "$d d e $u u"} = ?", "Que número tem $texto?",
            Cena.Nenhuma, n, listOf(trocado, d + u, n + 10, n - 10, n + 1), "${maiusc(ext(n))}!", 1, 99,
        )
    }

    private fun pintarDoCirco(): Pergunta = pintar(
        "Pinte o desenho do circo", "Cada pote de tinta tem um número. Descubra quanto dá em cada casinha e pinte com a cor daquele número.",
        (21..89).filter { it % 10 != 0 }, { v -> "${v / 10 * 10}+${v % 10}" },
    )

    private val mundo10: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Material dourado até 99", "🤡") { materialDouradoAte99() }
        val f2 = ModeloDeFase("Balança do circo", "⚖️") { sorteio({ balancaDecompor() }, { balancaDecompor() }, { materialDouradoAte99() }) }
        val f3 = ModeloDeFase("Quanto vale cada algarismo", "🎩") { decomporTocar() }
        val f4 = ModeloDeFase("Ligar as partes", "🔗") { sorteio({ ligarDecomposicao() }, { ligarDecomposicao() }, { decomporTocar() }) }
        val f5 = marco("Bandeirinha do circo", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Montar números", "🎪") { sorteio({ comporNumero(false) }, { conta(listOf((1..9).random(random) * 10, (1..9).random(random)), listOf("+")) }) }
        val f7 = ModeloDeFase("Pintar do circo", "🎨") { sorteio({ pintarDoCirco() }, { pintarDoCirco() }, { comporNumero(false) }) }
        val f8 = ModeloDeFase("Dezenas e revisão", "🔁") { sorteio({ materialDouradoAte99() }, { decomporTocar() }, { revisao(mundo9) }) }
        val f9 = ModeloDeFase("Dezenas espertas", "🧠") { comporNumero(dificil = true) }
        val f10 = ModeloDeFase("Chefão: o mágico", "👑") { sorteio({ pintarDoCirco() }, { balancaDecompor() }, { materialDouradoAte99() }, { comporNumero(true) }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 11: DESERTO (101 a 110)
    // Somar e tirar dezenas inteiras e dezena mais unidade sem reagrupar (23 + 4, 30 + 20).

    private fun dezenasInteiras(soma: Boolean): Pergunta {
        val a = (2..9).random(random)
        val b = if (soma) (1..10 - a).random(random) else (1 until a).random(random)
        return conta(listOf(a * 10, b * 10), listOf(if (soma) "+" else "−"), dica = "Dica: pense em ${ext(a, true)} dezenas ${if (soma) "mais" else "menos"} ${ext(b, true)}.")
    }

    private val somasDeDezenas = JeitoDeConta { v -> if (v % 10 != 0 || v < 20) null else (1 until v / 10).random(random).let { it * 10 to v - it * 10 } }

    private fun dezenaMaisUnidade(): Pergunta {
        val d = (1..9).random(random)
        return if (random.nextBoolean()) {
            val u = (0..8).random(random)
            conta(listOf(d * 10 + u, (1..9 - u).random(random)), listOf("+"), dica = "Só as unidades mudam.")
        } else {
            val u = (1..9).random(random)
            conta(listOf(d * 10 + u, (1..u).random(random)), listOf("−"), dica = "Só as unidades mudam.")
        }
    }

    private fun tremDeDezEmDez(): Pergunta {
        val inicio = (11..49).filter { it % 10 != 0 }.random(random)
        val nums = List(5) { inicio + it * 10 }
        return tremDeNumeros(nums, 2, "De 10 em 10 a partir do $inicio", "Comece no ${ext(inicio)} e vá somando dez: as unidades não mudam! Arraste os vagões que faltam.", listOf(inicio + 1, inicio + 11))
    }

    private fun escondidoDeDezenas(): Pergunta {
        val a = (1..8).random(random) * 10
        val b = (1..9 - a / 10).random(random) * 10
        return if (random.nextBoolean()) numeroEscondido(a, "+", b, escondePrimeiro = false)
        else numeroEscondido(a + b + (1..9).random(random), "−", b, escondePrimeiro = true)
    }

    private fun dezenasMisturadas(): Pergunta {
        val a = (2..7).random(random) * 10 + (0..9).random(random)
        return if (random.nextBoolean()) conta(listOf(a, (1..9 - a / 10).random(random) * 10), listOf("+"), dica = "Só as dezenas mudam.")
        else conta(listOf(a, (1 until a / 10).random(random).coerceAtLeast(1) * 10), listOf("−"), dica = "Só as dezenas mudam.")
    }

    private val mundo11: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Somar dezenas redondas", "🐫") { dezenasInteiras(soma = true) }
        val f2 = ModeloDeFase("Balões do deserto", "🎈") { sorteio({ baloesDeContas((3..9).random(random) * 10, "+", somasDeDezenas) }, { baloesDeContas((3..9).random(random) * 10, "+", somasDeDezenas) }, { dezenasInteiras(true) }) }
        val f3 = ModeloDeFase("Dezena e unidade", "🌵") { dezenaMaisUnidade() }
        val f4 = ModeloDeFase("Dominó do deserto", "🁫") {
            sorteio(
                { val v = (2..9).toList().sorteia(4).map { it * 10 }; dominoDeContas(v, "+", somasDeDezenas, (2..9).map { it * 10 }.filter { it !in v }) },
                { val v = (2..9).toList().sorteia(3).map { it * 10 }; dominoDeContas(v, "+", somasDeDezenas, (2..9).map { it * 10 }.filter { it !in v }) },
                { dezenaMaisUnidade() },
            )
        }
        val f5 = marco("Bandeirinha do deserto", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Tirar dezenas", "🦂") { sorteio({ dezenasInteiras(soma = false) }, { dezenasMisturadas() }) }
        val f7 = ModeloDeFase("Trem de 10 em 10", "🚂") { sorteio({ tremDeDezEmDez() }, { tremDeDezEmDez() }, { dezenasMisturadas() }) }
        val f8 = ModeloDeFase("Dezenas e revisão", "🔁") { sorteio({ dezenasMisturadas() }, { dezenaMaisUnidade() }, { revisao(mundo10) }) }
        val f9 = ModeloDeFase("Número escondido", "🔍") { escondidoDeDezenas() }
        val f10 = ModeloDeFase("Chefão: o camelo", "👑") {
            sorteio(
                { val v = (2..9).toList().sorteia(4).map { it * 10 }; dominoDeContas(v, "+", somasDeDezenas, (2..9).map { it * 10 }.filter { it !in v }) },
                { baloesDeContas((3..9).random(random) * 10, "+", somasDeDezenas) },
                { pescariaDeContas((3..8).random(random) * 10, "+", somasDeDezenas, VN.deserto[0]) },
            )
        }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 12: CIDADE (111 a 120)
    // Dinheiro: moedas e cédulas até 20 reais, pagar o valor exato, equivalências.

    private val dinheirosDaCidade = listOf(5, 10, 25, 50, 100, 200, 500, 1000, 2000)

    private fun reconhecerDinheiro(): Pergunta {
        val certo = dinheirosDaCidade.um()
        val outros = dinheirosDaCidade.filter { it != certo }.ordenarUmaVez { kotlin.math.abs(it - certo) + random.nextInt(300) }.take(3)
        val tipo = if (certo >= 200) "a nota" else "a moeda"
        return tocar(
            "Toque em ${VN.dinheiroEscrito(certo)}", "Toque n${tipo.substring(0, 1)} ${tipo.substringAfter(' ')} de ${VN.dinheiroFalado(certo)}.",
            Cena.Nenhuma, opDinheiro(certo), outros.map(::opDinheiro), "Isso! ${maiusc(tipo)} de ${VN.dinheiroFalado(certo)}.",
        )
    }

    private fun somarDinheiro(max: Int): Pergunta {
        val a = (1..max - 1).random(random)
        val b = (1..minOf(10, max - a)).random(random)
        val s = a + b
        val cand = listOf(s + 1, s - 1, s + 2, s + 10, a, b).filter { it in 1..max + 10 }
        return tocar(
            "R$ $a + R$ $b = ?", "Juntando ${VN.dinheiroFalado(a * 100)} com ${VN.dinheiroFalado(b * 100)}, quanto dá?",
            Cena.Sequencia(listOf("R$ $a", "+", "R$ $b", "=", "?")), opDinheiro(s * 100), errados(s, cand, 1, max + 10).map { opDinheiro(it * 100) },
            "Dá ${VN.dinheiroFalado(s * 100)}!",
        )
    }

    private fun cestaDeMoedas(): Pergunta {
        val k = (3..9).random(random)
        val p = VN.produtos.um()
        return cestaDe(
            "🪙", k + (2..3).random(random), k, "Pague R$ $k com moedas de 1",
            "${maiusc(p.nome)} custa ${VN.dinheiroFalado(k * 100)}. Cada moeda vale um real. Coloque na cesta as moedas certinhas. Depois toque no certinho.",
            "Pagou ${VN.dinheiroFalado(k * 100)}!",
        )
    }

    /** Equivalência: quantas moedas (ou notas) de um valor dão outro valor. */
    private fun equivalencia(): Pergunta {
        val (unidade, total) = listOf(50 to 100, 25 to 100, 50 to 200, 100 to 500, 200 to 1000, 500 to 1000, 500 to 2000, 1000 to 2000, 200 to 2000, 10 to 50).um()
        val n = total / unidade
        val tipo = if (unidade >= 200) "notas" else "moedas"
        return tocarNumero(
            "Quantas de ${VN.dinheiroEscrito(unidade)} dão ${VN.dinheiroEscrito(total)}?",
            "Quantas $tipo de ${VN.dinheiroFalado(unidade)} valem o mesmo que ${VN.dinheiroFalado(total)}?",
            Cena.Nenhuma, n, listOf(n + 1, n - 1, n + 2, n * 2), "${maiusc(ext(n, true))} $tipo de ${VN.dinheiroFalado(unidade)} valem ${VN.dinheiroFalado(total)}!", 1, 20,
        )
    }

    /** Um jeito de pagar [valor] (em centavos) com 2 ou 3 moedas ou notas menores. */
    private fun jeitoDePagar(valor: Int): List<Int> {
        val menores = Mercadinho.VALORES.filter { it < valor && valor % it == 0 && valor / it <= 5 }.sortedDescending()
        val u = menores.take(2).um()
        return List(valor / u) { u }
    }

    private fun ligarDinheiro(): Pergunta {
        val valores = listOf(100, 200, 1000, 2000, 500).sorteia((3..4).random(random))
        val texto = { v: Int -> jeitoDePagar(v).let { l -> l.joinToString(" + ") { VN.dinheiroEscrito(it).removePrefix("R$ ") } to "${ext(l.size, true)} ${if (l[0] < 200) "moedas" else "notas"} de ${VN.dinheiroFalado(l[0])}" } }
        val pares = valores.map { v -> val (t, f) = texto(v); opDinheiro(v) to opT(t, f) }
        return ligar("Ligue o dinheiro que vale igual", "Ligue cada valor com o jeito de pagar que vale a mesma coisa.", pares, null, "Valem a mesma coisa!")
    }

    private fun precoJunto(): Pergunta {
        val (p1, p2) = VN.produtos.sorteia(2)
        val a = (2..9).random(random)
        val b = (1..minOf(9, 20 - a)).random(random)
        val s = a + b
        return tocar(
            "R$ $a + R$ $b = ?", "${maiusc(p1.nome)} custa ${VN.dinheiroFalado(a * 100)} e ${p2.nome} custa ${VN.dinheiroFalado(b * 100)}. Quanto custam os dois juntos?",
            Cena.Emojis(p1.emoji + " " + p2.emoji), opDinheiro(s * 100), errados(s, listOf(s + 1, s - 1, s + 2, s - 2, s + 10), 1, 30).map { opDinheiro(it * 100) },
            "Os dois juntos custam ${VN.dinheiroFalado(s * 100)}!",
        )
    }

    private val mundo12: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Moedas e notas", "🪙") { reconhecerDinheiro() }
        val f2 = ModeloDeFase("Mercadinho", "🛒") { sorteio({ mercadinho((2..10).random(random) * 100) }, { mercadinho((2..10).random(random) * 100) }, { reconhecerDinheiro() }) }
        val f3 = ModeloDeFase("Somar dinheiro", "💵") { somarDinheiro(20) }
        val f4 = ModeloDeFase("Cesta de moedas", "🧺") { sorteio({ cestaDeMoedas() }, { cestaDeMoedas() }, { somarDinheiro(15) }) }
        val f5 = marco("Bandeirinha da cidade", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Vale o mesmo", "🔄") { equivalencia() }
        val f7 = ModeloDeFase("Ligar o dinheiro", "🔗") { sorteio({ ligarDinheiro() }, { ligarDinheiro() }, { equivalencia() }) }
        val f8 = ModeloDeFase("Dinheiro e revisão", "🔁") { sorteio({ precoJunto() }, { somarDinheiro(20) }, { revisao(mundo11) }) }
        val f9 = ModeloDeFase("Centavos", "🏪") { sorteio({ mercadinho((2..9).random(random) * 100 + listOf(50, 25, 75).um()) }, { precoJunto() }, { equivalencia() }) }
        val f10 = ModeloDeFase("Chefão: o ônibus da cidade", "👑") { sorteio({ mercadinho((5..20).random(random) * 100) }, { ligarDinheiro() }, { precoJunto() }, { reconhecerDinheiro() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 13: CASTELO (121 a 130)
    // Medidas: comprimento em palmos e centímetros, massa na balança, capacidade.

    private fun unidadeCerta(): Pergunta {
        val m = VN.medidas.um()
        return tocar(
            "Como medir ${m.coisa}?", "Para medir ${m.coisa}, a gente usa o quê?",
            Cena.Emojis(m.emoji), opT(m.unidade), VN.unidades.filter { it != m.unidade }.map { opT(it) }, "Isso! ${maiusc(m.coisa)} a gente mede em ${m.unidade}.",
        )
    }

    /** Régua: os quadradinhos são centímetros, e a minhoca não começa no 1. */
    private fun regua(): Pergunta {
        val l = (10..12).random(random)
        val tam = (3..8).random(random)
        val ini = (0..l - tam).random(random)
        val obj = listOf("🐛" to "a minhoca", "🖍️" to "o giz", "🥖" to "o pão").um()
        val itens = (1..l).map { "$it" } + (0 until l).map { if (it in ini until ini + tam) obj.first else "" }
        return tocarNumero(
            "Quantos centímetros?", "Cada quadradinho da régua é um centímetro. Quantos centímetros tem ${obj.second}? Conte os quadradinhos.",
            Cena.Grade(l, itens), tam, listOf(tam + 1, tam - 1, ini + tam, tam + 2), "${maiusc(obj.second)} tem ${ext(tam)} centímetros!", 1, 30,
        )
    }

    private fun tremDosTamanhos(): Pergunta {
        val medidas = (2..30).toList().sorteia((4..5).random(random)).sorted()
        val vagoes = medidas.map { opT("$it cm", "${ext(it)} centímetros") }
        return mexer(
            "Do mais curto ao mais comprido", "Cada vagão leva uma fita. Coloque do mais curto para o mais comprido.",
            Trem(vagoes, setOf(0)), "Do mais curto ao mais comprido. Piuí!",
        )
    }

    private fun maisPesado(): Pergunta {
        val pesos = (1..20).toList().sorteia(4)
        val mais = random.nextBoolean()
        val certo = if (mais) pesos.max() else pesos.min()
        val op = { k: Int -> opT("$k kg", "${ext(k)} ${if (k == 1) "quilo" else "quilos"}") }
        return tocar(
            if (mais) "Qual é o mais pesado?" else "Qual é o mais leve?", "Os sacos do castelo têm pesos diferentes. Qual é o mais ${if (mais) "pesado" else "leve"}?",
            Cena.Emojis("🎒"), op(certo), pesos.filter { it != certo }.map(op), "${maiusc(ext(certo))} ${if (certo == 1) "quilo" else "quilos"}: é o mais ${if (mais) "pesado" else "leve"}!",
        )
    }

    private fun balancaDeQuilos(): Pergunta {
        val (emoji, nome) = VN.pesados.um()
        val p = (5..15).random(random)
        val x = (1..p - 1).random(random)
        val distr = (1..9).filter { it != x && it != p - x }.sorteia(2)
        return balanca(
            "Equilibre: $p kg", "${maiusc(nome)} pesa ${ext(p)} quilos. Coloque os pesos de quilo do outro lado até a balança ficar reta.",
            listOf(p), listOf(x, p - x), distr, "${maiusc(nome)} pesa ${ext(p)} quilos!", emoji,
        )
    }

    private fun capacidade(): Pergunta = if (random.nextBoolean()) {
        val escolhidos = VN.recipientes.sorteia(4)
        val mais = random.nextBoolean()
        val certo = if (mais) escolhidos.maxBy { VN.recipientes.indexOf(it) } else escolhidos.minBy { VN.recipientes.indexOf(it) }
        tocar(
            if (mais) "Onde cabe mais água?" else "Onde cabe menos água?", "Onde cabe ${if (mais) "mais" else "menos"} água?",
            Cena.Emojis("💧"), opE(certo.first, certo.second), (escolhidos - certo).map { opE(it.first, it.second) }, "Isso! N${certo.second.substring(0, 1)} ${certo.second.substringAfter(' ')} cabe ${if (mais) "mais" else "menos"}.",
        )
    } else {
        val k = (2..8).random(random)
        tocarNumero(
            "Quantos copos enchem a jarra?", "Para encher a jarra, a rainha usou estes copos de água. Quantos copos cabem na jarra?",
            Cena.Emojis("🫙 " + "🥤".repeat(k)), k, listOf(k + 1, k - 1, k + 2), "Cabem ${ext(k)} copos!", 1, 12,
        )
    }

    private fun cacaDosLitros(): Pergunta {
        val certos = VN.liquidos.sorteia(3)
        val outros = VN.naoLiquidos.sorteia((4..5).random(random))
        return caca(
            13, "Ache as 3 coisas que medimos em litros", "No castelo tem muita coisa. Ache as três que a gente mede em litros.",
            certos.map { opE(it.first, it.second) }, outros.map { opE(it.first, it.second) }, "Leite, suco, água: a gente mede em litros!",
        )
    }

    private fun diferencaDeMedidas(): Pergunta {
        val a = (10..30).random(random)
        val b = (2..a - 2).random(random)
        val (c1, c2) = listOf("azul", "vermelha", "verde", "amarela").sorteia(2)
        return historinha(
            "A fita $c1 tem ${ext(a)} centímetros e a fita $c2 tem ${ext(b)}. Quantos centímetros a fita $c1 tem a mais?",
            listOf(a, b), listOf("−"), "🎀",
        )
    }

    private val mundo13: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Como a gente mede", "🏰") { unidadeCerta() }
        val f2 = ModeloDeFase("Trem do curto ao comprido", "🚂") { sorteio({ tremDosTamanhos() }, { tremDosTamanhos() }, { regua() }) }
        val f3 = ModeloDeFase("Régua de centímetros", "📏") { regua() }
        val f4 = ModeloDeFase("Balança do castelo", "⚖️") { sorteio({ balancaDeQuilos() }, { balancaDeQuilos() }, { maisPesado() }) }
        val f5 = marco("Bandeirinha do castelo", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Cabe mais, cabe menos", "🫖") { capacidade() }
        val f7 = ModeloDeFase("Caça dos litros", "🔎") { sorteio({ cacaDosLitros() }, { cacaDosLitros() }, { capacidade() }) }
        val f8 = ModeloDeFase("Medidas e revisão", "🔁") { sorteio({ maisPesado() }, { unidadeCerta() }, { revisao(mundo12) }) }
        val f9 = ModeloDeFase("Quanto a mais?", "🎀") { sorteio({ diferencaDeMedidas() }, { regua() }) }
        val f10 = ModeloDeFase("Chefão: o dragão", "👑") { sorteio({ balancaDeQuilos() }, { tremDosTamanhos() }, { cacaDosLitros() }, { diferencaDeMedidas() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 14: VALE DOS DINOSSAUROS (131 a 140)
    // Tempo: horas cheias e meias, dias da semana, meses, calendário.

    private fun emojiDaHora(h: Int, m: Int) = if (m == 0) V.relogios[h - 1] else VN.relogiosMeia[h - 1]

    private fun lerHora(meia: Boolean): Pergunta {
        val h = (1..12).random(random)
        val m = if (meia && random.nextBoolean()) 30 else 0
        val outras = listOf(h to 30 - m, (h % 12 + 1) to m, (h % 12 + 1) to 30 - m, ((h + 10) % 12 + 1) to m).distinct().filter { it != h to m }.take(3)
        val op = { (hh, mm): Pair<Int, Int> -> opT(VN.horaEscrita(hh, mm), VN.falaDaHora(hh, mm)) }
        return tocar(
            "Que horas são?", "O ponteiro pequeno mostra a hora. Se o grande está no seis, é meia hora. Que horas são?",
            Cena.Emojis(emojiDaHora(h, m)), op(h to m), outras.map(op), "${if (h == 1 || h == 12) "É" else "São"} ${VN.falaDaHora(h, m)}!",
        )
    }

    private fun relogioDaHora(parecidas: Boolean): Pergunta {
        val h = (1..12).random(random)
        val m = listOf(0, 30).um()
        val outras = if (parecidas) listOf(h to 30 - m, (h % 12 + 1) to m, (h % 12 + 1) to 30 - m)
        else (1..12).filter { it != h }.sorteia(3).map { it to listOf(0, 30).um() }
        return tocar(
            "Relógio ${if (h == 1) "da" else "das"} ${VN.horaEscrita(h, m)}", "Toque no relógio que marca ${VN.falaDaHora(h, m)}.",
            Cena.Nenhuma, opE(emojiDaHora(h, m), VN.falaDaHora(h, m)), outras.map { opE(emojiDaHora(it.first, it.second), VN.falaDaHora(it.first, it.second)) },
            "Esse marca ${VN.falaDaHora(h, m)}!",
        )
    }

    private fun tremDosDias(): Pergunta {
        val inicio = (0..6).random(random)
        val n = (4..5).random(random)
        val dias = List(n) { VN.diasDaSemana[(inicio + it) % 7] }
        return mexer(
            "Trem dos dias", "Os vagões levam os dias da semana. Comece em ${dias[0]} e coloque os outros dias na ordem.",
            Trem(dias.map { opT(it) }, setOf(0)), maiusc(dias.joinToString(", ")) + "!",
        )
    }

    private fun diaOuMes(): Pergunta {
        val meses = random.nextBoolean()
        val lista = if (meses) VN.meses else VN.diasDaSemana
        val i = lista.indices.random(random)
        val depois = random.nextBoolean()
        val certo = lista[(i + if (depois) 1 else lista.size - 1) % lista.size]
        val outros = lista.filter { it != certo && it != lista[i] }.ordenarUmaVez { kotlin.math.abs(lista.indexOf(it) - i) + random.nextDouble() * 2 }.take(2) + lista[i]
        val nome = if (meses) "mês" else "dia"
        return tocar(
            "Que $nome vem ${if (depois) "depois" else "antes"} de ${lista[i]}?", "Que $nome vem ${if (depois) "depois" else "antes"} de ${lista[i]}?",
            Cena.Nenhuma, opT(certo), outros.map { opT(it) }, "${maiusc(certo)}!",
        )
    }

    private fun cartasDosMeses(): Pergunta {
        val inicio = (0..11).random(random)
        val n = (4..6).random(random)
        val lista = List(n) { VN.meses[(inicio + it) % 12] }
        return cartasEmOrdem("Vire os meses em ordem", "Vire as cartas na ordem dos meses, começando em ${lista[0]}.", lista.map { opT(it) }, maiusc(lista.joinToString(", ")) + "!")
    }

    private fun duracao(): Pergunta {
        val ini = (1..8).random(random)
        val dur = (1..4).random(random)
        val obj = VN.dinossauros.um()
        return tocar(
            "${ini + dur} − $ini = ?", "O passeio dos dinossauros começou às ${VN.falaDaHora(ini)} e terminou às ${VN.falaDaHora(ini + dur)}. Quantas horas durou?",
            Cena.Emojis(V.relogios[ini - 1] + " ➡️ " + V.relogios[ini + dur - 1] + " " + obj.emoji),
            opT("$dur h", if (dur == 1) "uma hora" else "${ext(dur, true)} horas"),
            errados(dur, listOf(dur + 1, dur - 1, dur + 2, ini), 1, 12).map { opT("$it h", if (it == 1) "uma hora" else "${ext(it, true)} horas") },
            "Durou ${if (dur == 1) "uma hora" else "${ext(dur, true)} horas"}!",
        )
    }

    private fun calendario(): Pergunta {
        val diasNoMes = listOf(30, 31).um()
        val comeco = (0..6).random(random)
        val itens = List(comeco) { "" } + (1..diasNoMes).map { "$it" }
        val completo = itens + List((7 - itens.size % 7) % 7) { "" }
        val hoje = (1..diasNoMes - 7).random(random)
        val (soma, texto) = listOf(7 to "daqui a uma semana", 1 to "amanhã", 2 to "depois de amanhã").um()
        val certo = hoje + soma
        return tocarNumero(
            "$hoje + $soma = ?", "Este é o calendário do mês. Hoje é dia ${ext(hoje)}. Que dia vai ser $texto?",
            Cena.Grade(7, completo), certo, listOf(certo + 1, certo - 1, hoje + 1, hoje + 6, hoje + 8), "Vai ser dia ${ext(certo)}!", 1, 31,
        )
    }

    private val mundo14: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Horas e meias horas", "🦕") { lerHora(meia = true) }
        val f2 = ModeloDeFase("Acertar o relógio", "⏰") { sorteio({ relogio((1..12).random(random), listOf(0, 30).um(), false) }, { relogio((1..12).random(random), listOf(0, 30).um(), false) }, { lerHora(true) }) }
        val f3 = ModeloDeFase("Relógios parecidos", "🕞") { relogioDaHora(parecidas = true) }
        val f4 = ModeloDeFase("Trem dos dias", "🚂") { sorteio({ tremDosDias() }, { tremDosDias() }, { diaOuMes() }) }
        val f5 = marco("Bandeirinha dos dinossauros", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Dias e meses", "📅") { diaOuMes() }
        val f7 = ModeloDeFase("Cartas dos meses", "🃏") { sorteio({ cartasDosMeses() }, { cartasDosMeses() }, { diaOuMes() }) }
        val f8 = ModeloDeFase("Tempo e revisão", "🔁") { sorteio({ duracao() }, { relogioDaHora(false) }, { revisao(mundo13) }) }
        val f9 = ModeloDeFase("Calendário", "🗓️") { sorteio({ calendario() }, { calendario() }, { duracao() }) }
        val f10 = ModeloDeFase("Chefão: o tiranossauro", "👑") { sorteio({ relogio((1..12).random(random), listOf(0, 30).um(), true) }, { tremDosDias() }, { lerHora(true) }, { calendario() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 15: ILHA DO TESOURO (141 a 150)
    // Geometria: figuras planas e espaciais (cubo, esfera, cilindro, cone), lados e cantos, espelho.

    private fun qualSolido(): Pergunta {
        val s = VN.solidos.sorteia(4)
        val certo = s[0]
        return if (random.nextBoolean()) tocar(
            "Qual é ${if (certo.nome == "esfera") "a" else "o"} ${certo.nome}?", "Toque n${if (certo.nome == "esfera") "a" else "o"} ${certo.nome}.",
            Cena.Nenhuma, opE(certo.emoji, certo.objeto), s.drop(1).map { opE(it.emoji, it.objeto) }, "Isso! ${maiusc(certo.objeto)} tem forma de ${certo.nome}.",
        ) else tocar(
            "Que forma tem ${certo.objeto}?", "${maiusc(certo.objeto)} parece com que forma?",
            Cena.Emojis(certo.emoji), opT(certo.nome), s.drop(1).map { opT(it.nome) }, "${maiusc(certo.objeto)} parece ${if (certo.nome == "esfera") "uma" else "um"} ${certo.nome}!",
        )
    }

    private fun ligarSolidos(): Pergunta {
        val s = VN.solidos.sorteia(4)
        val sobra = VN.solidos.first { it !in s }
        return ligar("Ligue o objeto à forma", "Ligue cada objeto ao nome da forma que ele tem.", s.map { opE(it.emoji, it.objeto) to opT(it.nome) }, opT(sobra.nome), "Cada objeto com a sua forma!")
    }

    private fun ladosECantos(): Pergunta {
        val f = VN.formasPlanas.um()
        val cantos = random.nextBoolean()
        val certo = f.lados
        return tocarNumero(
            "Quantos ${if (cantos) "cantos" else "lados"} tem o ${f.nome}?", "Quantos ${if (cantos) "cantos" else "lados"} tem o ${f.nome}?",
            Cena.Emojis(f.emoji), certo, listOf(3, 4, 5, 0, 6, 2), if (certo == 0) "O ${f.nome} não tem ${if (cantos) "cantos" else "lados retos"}: é redondinho!" else "O ${f.nome} tem ${ext(certo)} ${if (cantos) "cantos" else "lados"}!", 0, 8,
        )
    }

    private fun cacaDasFormas(): Pergunta {
        val alvo = VN.formasPlanas.um()
        val k = (2..4).random(random)
        val outros = VN.formasPlanas.filter { it != alvo }
        return caca(
            15, "Ache ${ext(k)} ${alvo.nome}s", "Na ilha do tesouro tem formas escondidas. Ache os ${ext(k)} ${alvo.nome}s.",
            List(k) { opE(alvo.emoji, alvo.nome) }, List((4..6).random(random)) { outros.um().let { opE(it.emoji, it.nome) } },
            "Achou os ${ext(k)} ${alvo.nome}s!",
        )
    }

    /** Espelho: a fila se repete ao contrário do outro lado do espelho. */
    private fun espelho(): Pergunta {
        val formas = VN.formasPlanas.sorteia(4)
        val n = (2..3).random(random)
        val lado = formas.take(n)
        val refletido = lado.reversed()
        val falta = refletido.indices.random(random)
        val certo = refletido[falta]
        val cena = Cena.Sequencia(lado.map { it.emoji } + "🪞" + refletido.mapIndexed { i, f -> if (i == falta) "?" else f.emoji })
        return tocar(
            "Complete o espelho", "Do outro lado do espelho, as formas aparecem ao contrário, como no espelho do banheiro. Que forma falta?",
            cena, opE(certo.emoji, certo.nome), VN.formasPlanas.filter { it != certo }.sorteia(3).map { opE(it.emoji, it.nome) }, "Isso! O espelho mostra ${certo.nome}.",
        )
    }

    private fun quebraCabecaDaIlha(): Pergunta {
        val fig = VN.figurasDaIlha.um()
        val (l, c) = listOf(2 to 2, 2 to 3, 3 to 3).um()
        val rotulos = if (random.nextBoolean()) (1..l * c).map { "$it" } else emptyList()
        return mexer(
            "Monte o quebra-cabeça", if (rotulos.isEmpty()) "Arraste as peças para montar a figura do tesouro." else "Arraste as peças para a moldura. Os números das peças vão de um em um e ajudam!",
            QuebraCabeca(fig, l, c, rotulos), "Montou a figura!",
        )
    }

    private fun roboDoTesouro(): Pergunta {
        repeat(30) {
            val inicio = (0 until 16).random(random)
            val chegada = (0 until 16).filter { it != inicio }.random(random)
            val pedras = (0 until 16).filter { it != inicio && it != chegada }.sorteia((1..3).random(random)).toSet()
            try {
                val r = ProgramarRobo(4, 4, inicio, chegada, pedras, maxComandos = 6)
                if (r.menorCaminho >= 2) return mexer(
                    "Leve o robô até o tesouro", "Arraste as setas para a fila e toque no botão. Conte as casinhas do caminho e desvie das pedras!",
                    r, "O robô chegou em ${ext(r.menorCaminho, true)} casinhas!",
                )
            } catch (_: IllegalArgumentException) {
            }
        }
        return mexer("Leve o robô até o tesouro", "Arraste as setas para a fila e toque no botão. O tesouro está do outro lado!", ProgramarRobo(4, 4, 0, 5, setOf(1)), "O robô chegou!")
    }

    private fun naoRola(): Pergunta {
        val rolam = VN.solidos.filter { it.rola }
        val naoRolam = VN.solidos.filter { !it.rola }
        val certo = naoRolam.um()
        return tocar(
            "Qual não rola?", "Qual destas coisas não rola quando a gente empurra?",
            Cena.Nenhuma, opE(certo.emoji, certo.objeto), rolam.map { opE(it.emoji, it.objeto) }, "${maiusc(certo.objeto)} não rola: tem faces retinhas!",
        )
    }

    private val mundo15: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Cubo, esfera e cilindro", "🏝️") { qualSolido() }
        val f2 = ModeloDeFase("Ligar as formas", "🔗") { sorteio({ ligarSolidos() }, { ligarSolidos() }, { qualSolido() }) }
        val f3 = ModeloDeFase("Lados e cantos", "🔺") { ladosECantos() }
        val f4 = ModeloDeFase("Caça das formas", "🔎") { sorteio({ cacaDasFormas() }, { cacaDasFormas() }, { ladosECantos() }) }
        val f5 = marco("Bandeirinha da ilha", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Espelho mágico", "🪞") { espelho() }
        val f7 = ModeloDeFase("Quebra-cabeça do tesouro", "🧩") { sorteio({ quebraCabecaDaIlha() }, { quebraCabecaDaIlha() }, { espelho() }) }
        val f8 = ModeloDeFase("Formas e revisão", "🔁") { sorteio({ ladosECantos() }, { qualSolido() }, { revisao(mundo14) }) }
        val f9 = ModeloDeFase("Caminho do tesouro", "🤖") { sorteio({ roboDoTesouro() }, { naoRola() }, { espelho() }) }
        val f10 = ModeloDeFase("Chefão: o pirata papagaio", "👑") { sorteio({ cacaDasFormas() }, { quebraCabecaDaIlha() }, { ligarSolidos() }, { naoRola() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 16: FÁBRICA DE DOCES (151 a 160)
    // Adição com dois algarismos sem reagrupar e problemas de juntar.

    private fun parSemReagrupar(min: Int = 30): Pair<Int, Int> {
        val v = (min..99).random(random)
        return somasSemReagrupar.fazer(v)!!
    }

    private fun somarSemReagrupar(parecidas: Boolean = false): Pergunta {
        val (a, b) = parSemReagrupar()
        if (parecidas && random.nextBoolean()) return numeroEscondido(a, "+", b, escondePrimeiro = false)
        return conta(listOf(a, b), listOf("+"), dica = if (parecidas) "" else "Some as unidades com as unidades e as dezenas com as dezenas.", extras = listOf(a + b + 10, a + b - 10))
    }

    private fun juntar(): Pergunta {
        val (a, b) = parSemReagrupar()
        val obj = VN.doces.um()
        val nome = Vocabulario.nomesDeCriancas.um()
        return historinha(
            "Na fábrica, $nome fez ${obj.qtd(a)} de manhã e mais ${obj.qtd(b)} de tarde. ${obj.quantos()} ${obj.plural} $nome fez no dia todo?",
            listOf(a, b), listOf("+"), obj.emoji, obj,
        )
    }

    private fun tresParcelas(): Pergunta {
        val termos = List(3) { (1..2).random(random) * 10 + (0..3).random(random) }
        return conta(termos, listOf("+", "+"), dica = "Some as unidades, depois as dezenas.")
    }

    private fun pintarDeSomas(jeito: JeitoDeConta, faixa: List<Int>, titulo: String): Pergunta = pintar(
        titulo, "Cada pote de tinta tem um número. Faça a conta de cada casinha e pinte com a cor do resultado.",
        faixa, { v -> jeito.fazer(v)!!.let { (x, y) -> "$x+$y" } },
    )

    private val mundo16: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Somar dezenas e unidades", "🍭") { somarSemReagrupar() }
        val f2 = ModeloDeFase("Balões de doce", "🎈") { sorteio({ baloesDeContas((35..99).filter { it % 10 < 9 }.random(random), "+", somasSemReagrupar) }, { baloesDeContas((35..99).random(random), "+", somasSemReagrupar) }, { somarSemReagrupar() }) }
        val f3 = ModeloDeFase("Somas parecidas", "🍬") { somarSemReagrupar(parecidas = true) }
        val f4 = ModeloDeFase("Pintar da fábrica", "🎨") { sorteio({ pintarDeSomas(somasSemReagrupar, (30..99).toList(), "Pinte com as somas") }, { pintarDeSomas(somasSemReagrupar, (30..99).toList(), "Pinte com as somas") }, { somarSemReagrupar() }) }
        val f5 = marco("Bandeirinha dos doces", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Problemas de juntar", "🧁") { juntar() }
        val f7 = ModeloDeFase("Lojinha de doces", "🛒") { sorteio({ mercadinho((11..99).random(random) * 100) }, { mercadinho((11..60).random(random) * 100) }, { juntar() }) }
        val f8 = ModeloDeFase("Somas e revisão", "🔁") { sorteio({ somarSemReagrupar() }, { juntar() }, { revisao(mundo15) }) }
        val f9 = ModeloDeFase("Três parcelas", "🍫") {
            sorteio({ tresParcelas() }, { val (a, b) = parSemReagrupar(); numeroEscondido(a, "+", b, escondePrimeiro = random.nextBoolean()) })
        }
        val f10 = ModeloDeFase("Chefão: o confeiteiro", "👑") { sorteio({ pintarDeSomas(somasSemReagrupar, (30..99).toList(), "Pinte com as somas") }, { baloesDeContas((35..99).random(random), "+", somasSemReagrupar) }, { juntar() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 17: BOSQUE ENCANTADO (161 a 170)
    // Subtração com dois algarismos sem reagrupar, problemas de tirar e comparar.

    private fun parSubSemReagrupar(): Pair<Int, Int> {
        repeat(50) { subtracoesSemReagrupar.fazer((10..77).random(random))?.let { return it } }
        return 58 to 23
    }

    private fun tirarSemReagrupar(parecidas: Boolean = false): Pergunta {
        val (a, b) = parSubSemReagrupar()
        if (parecidas && random.nextBoolean()) return numeroEscondido(a, "−", b, escondePrimeiro = false)
        return conta(listOf(a, b), listOf("−"), dica = if (parecidas) "" else "Tire as unidades das unidades e as dezenas das dezenas.", extras = listOf(a - b + 10, a + b))
    }

    private fun tirarOuComparar(): Pergunta {
        val (a, b) = parSubSemReagrupar()
        val obj = VN.bosque.um()
        val (n1, n2) = Vocabulario.nomesDeCriancas.sorteia(2)
        val texto = if (random.nextBoolean()) "$n1 achou ${obj.qtd(a)} no bosque e deu ${ext(b, obj.feminino)} para as fadas. ${obj.quantos()} ficaram?"
        else "$n1 tem ${obj.qtd(a)} e $n2 tem ${obj.qtd(b)}. ${obj.quantos()} $n1 tem a mais que $n2?"
        return historinha(texto, listOf(a, b), listOf("−"), obj.emoji, obj)
    }

    private fun balancaQueFalta(): Pergunta {
        val (a, b) = parSubSemReagrupar()
        val distr = listOf(a - b + 10, a - b - 10, b + 10, 10).filter { it in 1..99 && it != b && it != a - b }.distinct().sorteia(2)
        return balanca(
            "Equilibre o $a usando o $b", "De um lado tem ${ext(a)}. Coloque o ${ext(b)} e descubra o peso que falta para a balança ficar reta.",
            listOf(a), listOf(b, a - b), distr, "${maiusc(ext(a))} menos ${ext(b)} dá ${ext(a - b)}: era esse o peso que faltava!", "🍄",
        )
    }

    private fun dominoDoBosque(): Pergunta {
        val valores = (11..66).filter { it % 10 in 1..6 }.sorteia((3..4).random(random))
        return dominoDeContas(valores, "−", subtracoesSemReagrupar, (11..66).filter { it % 10 in 1..6 && it !in valores }.sorteia(2))
    }

    private fun doisPassosSemReagrupar(): Pergunta {
        val a = (6..9).random(random) * 10 + (5..9).random(random)
        val b = (1..2).random(random) * 10 + (0..2).random(random)
        val c = (1..2).random(random) * 10 + (0..2).random(random)
        return conta(listOf(a, b, c), listOf("−", "−"), dica = "Faça uma conta de cada vez.")
    }

    private val mundo17: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Tirar dezenas e unidades", "🍄") { tirarSemReagrupar() }
        val f2 = ModeloDeFase("Pescaria encantada", "🎣") { sorteio({ pescariaDeContas((11..66).filter { it % 10 in 1..6 }.random(random), "−", subtracoesSemReagrupar, VN.bosque[0]) }, { pescariaDeContas((11..55).random(random), "−", subtracoesSemReagrupar, VN.bosque[0]) }, { tirarSemReagrupar() }) }
        val f3 = ModeloDeFase("Subtrações parecidas", "🦉") { tirarSemReagrupar(parecidas = true) }
        val f4 = ModeloDeFase("Dominó do bosque", "🁫") { sorteio({ dominoDoBosque() }, { dominoDoBosque() }, { tirarSemReagrupar() }) }
        val f5 = marco("Bandeirinha do bosque", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Tirar e comparar", "🧚") { tirarOuComparar() }
        val f7 = ModeloDeFase("Balança das fadas", "⚖️") { sorteio({ balancaQueFalta() }, { balancaQueFalta() }, { tirarOuComparar() }) }
        val f8 = ModeloDeFase("Tirar e revisão", "🔁") { sorteio({ tirarSemReagrupar() }, { tirarOuComparar() }, { revisao(mundo16) }) }
        val f9 = ModeloDeFase("Contas encantadas", "✨") {
            sorteio({ doisPassosSemReagrupar() }, { val (a, b) = parSubSemReagrupar(); numeroEscondido(a, "−", b, escondePrimeiro = random.nextBoolean()) })
        }
        val f10 = ModeloDeFase("Chefão: a coruja sábia", "👑") { sorteio({ dominoDoBosque() }, { pescariaDeContas((11..55).random(random), "−", subtracoesSemReagrupar, VN.bosque[0]) }, { tirarOuComparar() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 18: VULCÃO (171 a 180)
    // Centenas: contar de 100 em 100, números até 1000, compor e decompor.

    private fun deCemEmCem(): Pergunta {
        val crescente = random.nextInt(3) != 0
        val inicio = (1..6).random(random) * 100
        val fila = List(4) { inicio + it * 100 }.let { if (crescente) it else it.reversed() }
        val resp = fila.last()
        return tocarNumero(
            "Que número vem depois?", "Contando de cem em cem: " + fila.dropLast(1).joinToString(", ") { ext(it) } + ". Qual vem depois?",
            Cena.Sequencia(fila.dropLast(1).map { "$it" } + "?"), resp, listOf(resp + 10, resp - 10, resp + 100, resp - 100, resp + 1), maiusc(fila.joinToString(", ") { ext(it) }) + "!", 0, 1000,
        )
    }

    private fun tremDasCentenas(): Pergunta {
        val inicio = (1..5).random(random)
        val nums = List((4..5).random(random)) { (inicio + it) * 100 }
        return tremDeNumeros(nums, 1, "Trem das centenas", "Os vagões vão de cem em cem, começando no ${ext(nums[0])}. Coloque em ordem.", listOf(nums.last() + 10, nums[0] + 50))
    }

    private fun lerAte1000(): Pergunta {
        val c = (1..9).random(random)
        val d = (0..9).random(random)
        val u = (0..9).random(random)
        val n = c * 100 + d * 10 + u
        val trocas = listOf(c * 100 + u * 10 + d, d * 100 + c * 10 + u, u * 100 + d * 10 + c, n + 100, n + 10, n - 1).filter { it in 100..999 && it != n }
        return tocarNumero("Toque no $n", "Toque no número ${ext(n)}.", Cena.Nenhuma, n, trocas, "${maiusc(ext(n))}!", 100, 999)
    }

    private fun cartasDeCentenas(): Pergunta {
        val nums = (101..999).toList().sorteia((4..5).random(random))
        return cartasDeNumeros(nums, crescente = random.nextInt(3) != 0)
    }

    private fun decomporCentenas(): Pergunta {
        val c = (1..9).random(random)
        val d = (0..9).random(random)
        val u = (1..9).random(random)
        val n = c * 100 + d * 10 + u
        return when (random.nextInt(3)) {
            0 -> conta(listOf(c * 100, d * 10, u).filter { it > 0 }, List(if (d == 0) 1 else 2) { "+" })
            1 -> tocarNumero(
                "Quantas centenas tem $n?", "Quantas centenas tem o número ${ext(n)}? Cada centena vale cem.",
                Cena.Palavra("$n"), c, listOf(c + 1, c - 1, d, u, n / 10), "${maiusc(ext(c, true))} ${if (c == 1) "centena" else "centenas"}!", 0, 99,
            )
            else -> tocarNumero(
                "Quanto vale o $c em $n?", "No número ${ext(n)}, quanto vale o algarismo ${ext(c)}?",
                Cena.Palavra("$n"), c * 100, listOf(c, c * 10, n, c * 100 + 10, (c + 1) * 100).filter { it <= 1000 }, "O ${ext(c)} está nas centenas: vale ${ext(c * 100)}!", 0, 1000,
            )
        }
    }

    private fun ligarCentenas(): Pergunta {
        val nums = (101..999).filter { it % 10 != 0 && it / 10 % 10 != 0 }.sorteia((3..4).random(random))
        val partes = { n: Int -> opT("${n / 100 * 100} + ${n / 10 % 10 * 10} + ${n % 10}", "${ext(n / 100 * 100)} mais ${ext(n / 10 % 10 * 10)} mais ${ext(n % 10)}") }
        val s = (101..999).filter { it % 10 != 0 && it / 10 % 10 != 0 && it !in nums }.um()
        return ligar("Ligue o número às partes", "Ligue cada número com as suas centenas, dezenas e unidades.", nums.map { opN(it) to partes(it) }, partes(s), "Cada número com as suas partes!")
    }

    private fun memoriaDeCentenas(): Pergunta {
        val nums = (1..9).toList().sorteia(4).map { it * 100 + (1..9).random(random) * 10 }
        return Pergunta.deAtividade(
            "Número com as partes", listOf(pt("Jogo da memória! Junte cada número com as suas partes.")),
            Atividade.Memoria(nums.map { "$it" to "${it / 100 * 100}+${it % 100}" }), listOf(pt("Cada número com as suas partes!")),
        )
    }

    private fun compararCentenas(): Pergunta {
        val c = (1..9).random(random)
        val (d, u) = (0..9).toList().sorteia(2)
        val a = c * 100 + d * 10 + u
        val b = c * 100 + u * 10 + d
        val outros = listOf(a, b, a - 100, a + 1).filter { it in 100..999 }.distinct()
        val nums = (outros + (100..999).toList().sorteia(4)).distinct().take(4)
        val maior = random.nextBoolean()
        val certo = if (maior) nums.max() else nums.min()
        return tocar(
            if (maior) "Qual é o maior?" else "Qual é o menor?", "Qual é o ${if (maior) "maior" else "menor"} número? Olhe primeiro as centenas, depois as dezenas.",
            Cena.Nenhuma, opN(certo), nums.filter { it != certo }.map(::opN), "${maiusc(ext(certo))} é o ${if (maior) "maior" else "menor"}!",
        )
    }

    private fun vizinhosDaCentena(): Pergunta {
        val c = (2..9).random(random) * 100
        return if (random.nextBoolean()) tocarNumero(
            "Antes do $c vem?", "Que número vem logo antes do ${ext(c)}?", Cena.Sequencia(listOf("?", "$c")), c - 1,
            listOf(c - 10, c + 1, c - 100, c - 2, c - 101 + 10), "Antes do ${ext(c)} vem o ${ext(c - 1)}!", 100, 1000,
        ) else {
            val fila = listOf(c - 30, c - 20, c - 10)
            tocarNumero(
                "Que número vem depois?", "De dez em dez: " + fila.joinToString(", ") { ext(it) } + ". Qual vem depois?",
                Cena.Sequencia(fila.map { "$it" } + "?"), c, listOf(c + 10, c - 1, c + 100, c - 100, c + 1), "${maiusc(ext(c))}!", 100, 1000,
            )
        }
    }

    private fun quebraCabecaDoVulcao(): Pergunta {
        val passo = listOf(100, 10).um()
        val inicio = if (passo == 100) 100 else (1..9).random(random) * 100
        return mexer(
            "Quebra-cabeça das centenas", "Monte o vulcão! As peças vão de ${ext(passo)} em ${ext(passo)}, começando no ${ext(inicio)}.",
            QuebraCabeca("🌋", 3, 3, List(9) { "${inicio + it * passo}" }, setOf(0)), "Montou o vulcão!",
        )
    }

    private val mundo18: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("De 100 em 100", "🌋") { deCemEmCem() }
        val f2 = ModeloDeFase("Trem das centenas", "🚂") { sorteio({ tremDasCentenas() }, { quebraCabecaDoVulcao() }, { deCemEmCem() }) }
        val f3 = ModeloDeFase("Números até 1000", "🔥") { lerAte1000() }
        val f4 = ModeloDeFase("Cartas de lava", "🃏") { sorteio({ cartasDeCentenas() }, { cartasDeCentenas() }, { lerAte1000() }) }
        val f5 = marco("Bandeirinha do vulcão", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Centenas, dezenas e unidades", "🧱") { decomporCentenas() }
        val f7 = ModeloDeFase("Ligar as partes", "🔗") { sorteio({ ligarCentenas() }, { memoriaDeCentenas() }, { decomporCentenas() }) }
        val f8 = ModeloDeFase("Centenas e revisão", "🔁") { sorteio({ lerAte1000() }, { decomporCentenas() }, { revisao(mundo17) }) }
        val f9 = ModeloDeFase("Comparar até 1000", "🏔️") { sorteio({ compararCentenas() }, { vizinhosDaCentena() }) }
        val f10 = ModeloDeFase("Chefão: o dragão de lava", "👑") { sorteio({ tremDasCentenas() }, { ligarCentenas() }, { compararCentenas() }, { decomporCentenas() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 19: REINO DAS NUVENS (181 a 190)
    // Adição com reagrupamento (vai um) até 99, cálculo mental e problemas de dois passos.

    private fun parComReagrupar(): Pair<Int, Int> {
        repeat(50) { somasComReagrupar.fazer((30..99).random(random))?.let { return it } }
        return 27 to 15
    }

    private fun somarComReagrupar(parecidas: Boolean = false): Pergunta {
        val (a, b) = parComReagrupar()
        if (parecidas && random.nextBoolean()) return numeroEscondido(a, "+", b, escondePrimeiro = random.nextBoolean())
        val semVaiUm = (a / 10 + b / 10) * 10 + (a % 10 + b % 10) % 10
        return conta(listOf(a, b), listOf("+"), dica = if (parecidas) "Cuidado com o vai um!" else "As unidades passam de dez: dez unidades viram uma dezena. Vai um!", extras = listOf(semVaiUm, semVaiUm + 20))
    }

    private fun balancaVaiUm(): Pergunta {
        val (a, b) = parComReagrupar()
        val distr = listOf(a + 1, b + 10, b - 1, a - 10).filter { it in 1..99 && it != a && it != b }.sorteia(2)
        return balanca(
            "Equilibre o ${a + b}", "Do outro lado tem ${ext(a + b)}. Ache os dois pesos que juntos dão ${ext(a + b)}.",
            listOf(a + b), listOf(a, b), distr, "${maiusc(ext(a))} mais ${ext(b)} é ${ext(a + b)}!", "☁️",
        )
    }

    private fun calculoMental(): Pergunta {
        val n = (12..80).random(random)
        val (k, dica) = listOf(9 to "Somar nove é somar dez e tirar um.", 19 to "Somar dezenove é somar vinte e tirar um.", 11 to "Somar onze é somar dez e mais um.", 8 to "Somar oito é somar dez e tirar dois.").um()
        return conta(listOf(n.coerceAtMost(99 - k), k), listOf("+"), dica = dica)
    }

    private fun doisPassosComReagrupar(): Pergunta {
        val a = (12..40).random(random)
        val b = (12..30).random(random)
        val c = (5..minOf(29, 99 - a - b)).random(random)
        return if (random.nextBoolean()) conta(listOf(a, b, c), listOf("+", "+"), dica = "Some dois primeiro, depois o terceiro.")
        else {
            val obj = VN.nuvens.um()
            historinha(
                "No céu tinha ${obj.qtd(a)}. Chegaram mais ${ext(b, obj.feminino)} e depois mais ${ext(c, obj.feminino)}. ${obj.quantos()} ficaram no céu?",
                listOf(a, b, c), listOf("+", "+"), obj.emoji, obj,
            )
        }
    }

    private val mundo19: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Vai um", "☁️") { somarComReagrupar() }
        val f2 = ModeloDeFase("Balões nas nuvens", "🎈") { sorteio({ baloesDeContas((40..98).filter { it % 10 != 9 }.random(random), "+", somasComReagrupar) }, { baloesDeContas((40..98).filter { it % 10 != 9 }.random(random), "+", somasComReagrupar) }, { somarComReagrupar() }) }
        val f3 = ModeloDeFase("Vai um parecido", "🌈") { somarComReagrupar(parecidas = true) }
        val f4 = ModeloDeFase("Balança das nuvens", "⚖️") { sorteio({ balancaVaiUm() }, { balancaVaiUm() }, { somarComReagrupar() }) }
        val f5 = marco("Bandeirinha das nuvens", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Conta de cabeça", "🧠") { calculoMental() }
        val f7 = ModeloDeFase("Pintar o céu", "🎨") { sorteio({ pintarDeSomas(somasComReagrupar, (40..98).filter { it % 10 != 9 }, "Pinte com as somas de vai um") }, { pintarDeSomas(somasComReagrupar, (40..98).filter { it % 10 != 9 }, "Pinte com as somas de vai um") }, { calculoMental() }) }
        val f8 = ModeloDeFase("Vai um e revisão", "🔁") { sorteio({ somarComReagrupar() }, { calculoMental() }, { revisao(mundo18) }) }
        val f9 = ModeloDeFase("Dois passos", "🪁") { doisPassosComReagrupar() }
        val f10 = ModeloDeFase("Chefão: o gigante das nuvens", "👑") { sorteio({ balancaVaiUm() }, { pintarDeSomas(somasComReagrupar, (40..98).filter { it % 10 != 9 }, "Pinte com as somas de vai um") }, { baloesDeContas((40..98).filter { it % 10 != 9 }.random(random), "+", somasComReagrupar) }, { doisPassosComReagrupar() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 20: CAVERNA DE CRISTAL (191 a 200)
    // Subtração com reagrupamento, quantos a mais e quantos faltam; troco.

    private fun parSubComReagrupar(): Pair<Int, Int> {
        repeat(50) { subtracoesComReagrupar.fazer((5..70).random(random))?.let { return it } }
        return 42 to 17
    }

    private fun tirarComReagrupar(parecidas: Boolean = false): Pergunta {
        val (a, b) = parSubComReagrupar()
        if (parecidas && random.nextBoolean()) return numeroEscondido(a, "−", b, escondePrimeiro = random.nextBoolean())
        // Erro clássico: tirar o menor do maior em cada coluna (42 − 17 vira 35).
        val classico = kotlin.math.abs(a / 10 - b / 10) * 10 + kotlin.math.abs(a % 10 - b % 10)
        return conta(listOf(a, b), listOf("−"), dica = "Não dá para tirar as unidades: empreste uma dezena, que vira dez unidades.", extras = listOf(classico, a - b + 10))
    }

    private fun dominoDaCaverna(): Pergunta {
        val faixa = (5..60).filter { it % 10 != 0 }
        val valores = faixa.sorteia((3..4).random(random))
        return dominoDeContas(valores, "−", subtracoesComReagrupar, faixa.filter { it !in valores }.sorteia(2))
    }

    private fun quantosFaltam(): Pergunta {
        val (a, b) = parSubComReagrupar()
        val obj = VN.cristal.um()
        val nome = Vocabulario.nomesDeCriancas.um()
        val texto = if (random.nextBoolean()) "$nome quer juntar ${obj.qtd(a)}. Já tem ${ext(b, obj.feminino)}. ${obj.quantos()} faltam?"
        else "Na caverna tem ${obj.qtd(a)} brilhando e ${obj.qtd(b)} apagad${if (obj.feminino) "as" else "os"}. ${obj.quantos()} brilhando a mais?"
        return historinha(texto, listOf(a, b), listOf("−"), obj.emoji, obj)
    }

    private fun trocoDaCaverna(): Pergunta {
        val pago = listOf(1000, 2000, 5000).um()
        val preco = (1 until pago / 100).random(random) * 100
        return mercadinho(preco, troco = true, pago = pago)
    }

    private fun doisPassosDaCaverna(): Pergunta {
        val a = (50..99).random(random)
        val b = (11..29).random(random)
        val c = (5..minOf(25, a - b)).random(random)
        return if (random.nextBoolean()) conta(listOf(a, b, c), listOf("−", "−"), dica = "Faça uma conta de cada vez.")
        else conta(listOf(a, b, c), listOf("−", "+"), dica = "Primeiro tire, depois some.")
    }

    private val mundo20: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Emprestar uma dezena", "💎") { tirarComReagrupar() }
        val f2 = ModeloDeFase("Dominó de cristal", "🁫") { sorteio({ dominoDaCaverna() }, { dominoDaCaverna() }, { tirarComReagrupar() }) }
        val f3 = ModeloDeFase("Cuidado com a armadilha", "🦇") { tirarComReagrupar(parecidas = true) }
        val f4 = ModeloDeFase("Pescaria no lago escuro", "🎣") { sorteio({ pescariaDeContas((5..60).filter { it % 10 != 0 }.random(random), "−", subtracoesComReagrupar, VN.mar[0]) }, { pescariaDeContas((5..60).filter { it % 10 != 0 }.random(random), "−", subtracoesComReagrupar, VN.mar[0]) }, { tirarComReagrupar() }) }
        val f5 = marco("Bandeirinha da caverna", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Quantos faltam?", "🔦") { quantosFaltam() }
        val f7 = ModeloDeFase("Troco da caverna", "🪙") { sorteio({ trocoDaCaverna() }, { trocoDaCaverna() }, { quantosFaltam() }) }
        val f8 = ModeloDeFase("Emprestar e revisão", "🔁") { sorteio({ tirarComReagrupar() }, { quantosFaltam() }, { revisao(mundo19) }) }
        val f9 = ModeloDeFase("Dois passos na caverna", "⛏️") { doisPassosDaCaverna() }
        val f10 = ModeloDeFase("Chefão: o guardião de cristal", "👑") { sorteio({ trocoDaCaverna() }, { dominoDaCaverna() }, { tirarComReagrupar() }, { doisPassosDaCaverna() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 21: PARQUE DE DIVERSÕES (201 a 210)
    // Ideia de multiplicação: grupos iguais, dobro e triplo, contar de 2, 5 e 10.

    private fun gruposIguais(): Pergunta {
        val obj = VN.parque.um()
        val grupos = (2..5).random(random)
        val cada = (2..5).random(random)
        val total = grupos * cada
        return tocarNumero(
            List(grupos) { "$cada" }.joinToString(" + ") + " = ?",
            "Tem ${ext(grupos)} grupos com ${obj.qtd(cada)} em cada um. ${obj.quantos()} ${obj.plural} ao todo?",
            Cena.Emojis(List(grupos) { obj.emoji.repeat(cada) }.joinToString("  ")), total,
            listOf(total + cada, total - cada, grupos + cada, total + 1, total - 1), "${maiusc(ext(grupos))} vezes ${ext(cada)}: ${obj.qtd(total)}!", 1, 50,
        )
    }

    private fun cestaDeGrupos(): Pergunta {
        val obj = VN.parque.um()
        val grupos = (2..4).random(random)
        val cada = (2..3).random(random)
        val total = grupos * cada
        return cestaDe(
            obj.emoji, total + (2..3).random(random), total, "$grupos grupos de $cada",
            "Cada criança do parque ganha ${obj.qtd(cada)}. São ${ext(grupos, true)} crianças. Coloque na cesta ${obj.qtd(total)} para todas. Depois toque no certinho.",
            "${maiusc(ext(grupos))} vezes ${ext(cada)} é ${ext(total)}!",
        )
    }

    private fun vezes(max: Int): Pergunta {
        val a = (2..5).random(random)
        val b = (2..max).random(random)
        return conta(listOf(a, b), listOf("×"), dica = "${maiusc(ext(a))} vezes ${ext(b)} é somar ${ext(b)} ${ext(a, true)} vezes.", extras = listOf(a + b, a * b + a, a * b - b))
    }

    private fun tremDeSaltos(): Pergunta {
        val passo = listOf(2, 5, 10).um()
        val inicio = (0..4).random(random) * passo
        val nums = List(5) { inicio + (it + 1) * passo }
        return tremDeNumeros(nums, 2, "De $passo em $passo", "Conte de ${ext(passo)} em ${ext(passo)}, começando no ${ext(nums[0])}. Arraste os vagões que faltam.", listOf(nums.last() + 1, nums[0] + 1))
    }

    private fun dobroOuTriplo(): Pergunta {
        val triplo = random.nextBoolean()
        val n = if (triplo) (2..9).random(random) else (3..25).random(random)
        val k = if (triplo) 3 else 2
        return tocarNumero(
            "$k × $n = ?", "Qual é o ${if (triplo) "triplo" else "dobro"} de ${ext(n)}? O ${if (triplo) "triplo é três vezes" else "dobro é duas vezes"} a mesma quantidade.",
            Cena.Sequencia(listOf("$k", "×", "$n", "=", "?")), k * n, listOf(k * n + 1, k * n - 1, n + k, (5 - k) * n, k * n + n), "O ${if (triplo) "triplo" else "dobro"} de ${ext(n)} é ${ext(k * n)}!", 1, 75,
        )
    }

    private fun baloesDeMultiplos(): Pergunta {
        val passo = listOf(2, 5, 10).um()
        val certos = (1..10).map { it * passo }.filter { it <= 50 }.sorteia(3)
        val errados = (1..50).filter { it % passo != 0 }.sorteia(3)
        return baloes(
            "Estoure os números de $passo em $passo", "Estoure só os balões com números que aparecem quando a gente conta de ${ext(passo)} em ${ext(passo)}.",
            certos.map(::opN), errados.map(::opN), "Isso! Contando de ${ext(passo)} em ${ext(passo)} aparecem esses números.",
        )
    }

    private fun memoriaDeVezes(): Pergunta {
        val contas = (2..5).flatMap { a -> (2..5).map { b -> a to b } }.shuffled(random).distinctBy { it.first * it.second }.take(4)
        return Pergunta.deAtividade(
            "Conta de vezes com o resultado", listOf(pt("Jogo da memória! Junte cada conta de vezes com o seu resultado.")),
            Atividade.Memoria(contas.map { (a, b) -> "$a×$b" to "${a * b}" }), listOf(pt("Cada conta com o seu resultado!")),
        )
    }

    private fun problemaDeVezes(): Pergunta {
        val cada = (2..5).random(random)
        val vezes = (2..9).random(random)
        val (emoji, frase) = listOf(
            "🎡" to "Cada cabine da roda gigante leva ${ext(cada, true)} pessoas. Quantas pessoas cabem em ${ext(vezes, true)} cabines?",
            "🎠" to "Cada cavalinho leva ${ext(cada, true)} crianças. Quantas crianças vão em ${ext(vezes)} cavalinhos?",
            "🎟️" to "Cada ingresso custa ${ext(cada)} reais. Quanto custam ${ext(vezes)} ingressos?",
        ).um()
        return historinha(frase, listOf(vezes, cada), listOf("×"), emoji)
    }

    private val mundo21: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Grupos iguais", "🎡") { gruposIguais() }
        val f2 = ModeloDeFase("Cesta de grupos", "🧺") { sorteio({ cestaDeGrupos() }, { cestaDeGrupos() }, { gruposIguais() }) }
        val f3 = ModeloDeFase("Vezes", "✖️") { vezes(5) }
        val f4 = ModeloDeFase("Trem dos pulos", "🚂") { sorteio({ tremDeSaltos() }, { tremDeSaltos() }, { vezes(5) }) }
        val f5 = marco("Bandeirinha do parque", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Dobro e triplo", "👯") { dobroOuTriplo() }
        val f7 = ModeloDeFase("Balões de pular", "🎈") { sorteio({ baloesDeMultiplos() }, { memoriaDeVezes() }, { dobroOuTriplo() }) }
        val f8 = ModeloDeFase("Vezes e revisão", "🔁") { sorteio({ vezes(5) }, { gruposIguais() }, { revisao(mundo20) }) }
        val f9 = ModeloDeFase("Problemas de vezes", "🎠") { sorteio({ problemaDeVezes() }, { vezes(10) }) }
        val f10 = ModeloDeFase("Chefão: o palhaço da roda gigante", "👑") { sorteio({ cestaDeGrupos() }, { tremDeSaltos() }, { baloesDeMultiplos() }, { problemaDeVezes() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 22: CIDADE DOS ROBÔS (211 a 220)
    // Ideia de divisão: repartir igual, metade, terça parte e quarta parte.

    private fun repartir(): Pergunta {
        val obj = VN.robos.drop(1).um()
        val pessoas = (2..4).random(random)
        val cada = (2..6).random(random)
        val total = pessoas * cada
        return tocarNumero(
            "$total ÷ $pessoas = ?", "Vamos repartir ${obj.qtd(total)} entre ${ext(pessoas)} robôs, todos com a mesma quantidade. ${obj.quantos()} cada robô ganha?",
            Cena.Emojis(obj.emoji.repeat(total)), cada, listOf(cada + 1, cada - 1, pessoas, total - pessoas, cada * 2), "${maiusc(obj.qtd(cada))} para cada robô!", 1, 40,
        )
    }

    private fun cestaDeRepartir(): Pergunta {
        val obj = VN.robos.drop(1).um()
        val pessoas = (2..4).random(random)
        val cada = (2..4).random(random)
        val total = pessoas * cada
        return cestaDe(
            obj.emoji, total, cada, "Reparta $total entre $pessoas",
            "Tem ${obj.qtd(total)} para repartir igual entre ${ext(pessoas)} robôs. Coloque na cesta só a parte de um robô. Depois toque no certinho.",
            "Cada robô fica com ${obj.qtd(cada)}!",
        )
    }

    private fun parteDe(): Pergunta {
        val (k, nome) = listOf(2 to "a metade", 3 to "a terça parte", 4 to "a quarta parte").um()
        val parte = (2..10).random(random)
        val total = parte * k
        return tocarNumero(
            "$total ÷ $k = ?", "Quanto é ${nome} de ${ext(total)}? É repartir em ${ext(k, true)} partes iguais.",
            Cena.Sequencia(listOf("$total", "÷", "$k", "=", "?")), parte, listOf(parte + 1, parte - 1, total - parte, parte * 2, k), "${maiusc(nome)} de ${ext(total)} é ${ext(parte)}!", 1, 40,
        )
    }

    private fun balancaDePesosIguais(): Pergunta {
        val peso = (2..5).random(random)
        val quantos = (2..6).random(random)
        val total = peso * quantos
        return mexer(
            "Equilibre $total com pesos de $peso", "De um lado tem ${ext(total)}. Equilibre usando só pesos de ${ext(peso)}. Quantos pesos você vai precisar?",
            Balanca(listOf(total), List(minOf(8, quantos + 2)) { peso }, "🔩"), "${maiusc(ext(quantos))} pesos de ${ext(peso)}: ${ext(total)} dividido por ${ext(peso)} é ${ext(quantos)}!",
        )
    }

    private fun fracaoDaFigura(): Pergunta {
        val partes = (2..4).random(random)
        val inteiro = random.nextInt(5) == 0
        val itens = List(partes) { if (inteiro || it == 0) "🟧" else "⬜" }
        val nomes = mapOf(2 to "metade", 3 to "um terço", 4 to "um quarto")
        val certo = if (inteiro) "inteiro" else nomes.getValue(partes)
        val todas = listOf("metade", "um terço", "um quarto", "inteiro")
        return tocar(
            "Que parte está pintada?", "A barra foi cortada em ${ext(partes, true)} partes iguais. Que parte está pintada de laranja?",
            Cena.Grade(partes, itens), opT(certo), todas.filter { it != certo }.map { opT(it) },
            if (inteiro) "Está toda pintada: é o inteiro!" else "Uma parte de ${ext(partes, true)}: $certo!",
        )
    }

    private fun ligarPartes(): Pergunta {
        val nomes = listOf(2 to "metade de", 3 to "terço de", 4 to "quarto de")
        val itens = (1..3).map { _ -> val (k, n) = nomes.um(); val p = (2..10).random(random); Triple(k, n, p) }.distinctBy { it.third }
        if (itens.size < 2) return parteDe()
        val pares = itens.map { (k, n, p) -> opT("$n ${p * k}", "$n ${ext(p * k)}") to opN(p) }.distinctBy { it.first.visual }
        if (pares.size < 2) return parteDe()
        val sobra = (2..10).filter { s -> itens.none { it.third == s } }.um()
        return ligar("Ligue a parte ao resultado", "Ligue cada parte ao seu resultado.", pares, opN(sobra), "Cada parte com o seu resultado!")
    }

    private fun problemaDeDividir(): Pergunta {
        val obj = VN.doces.um()
        val criancas = (2..5).random(random)
        val cada = (2..8).random(random)
        val total = criancas * cada
        return historinha(
            "O robô tem ${obj.qtd(total)} para repartir igual entre ${ext(criancas, true)} crianças. ${obj.quantos()} cada criança ganha?",
            listOf(total, criancas), listOf("÷"), "🤖" + obj.emoji, obj,
        )
    }

    private val mundo22: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Repartir igual", "🤖") { repartir() }
        val f2 = ModeloDeFase("Cesta de repartir", "🧺") { sorteio({ cestaDeRepartir() }, { cestaDeRepartir() }, { repartir() }) }
        val f3 = ModeloDeFase("Metade, terço e quarto", "🔩") { parteDe() }
        val f4 = ModeloDeFase("Balança dos robôs", "⚖️") { sorteio({ balancaDePesosIguais() }, { balancaDePesosIguais() }, { parteDe() }) }
        val f5 = marco("Bandeirinha dos robôs", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Partes da barra", "🍫") { fracaoDaFigura() }
        val f7 = ModeloDeFase("Ligar as partes", "🔗") { sorteio({ ligarPartes() }, { ligarPartes() }, { fracaoDaFigura() }) }
        val f8 = ModeloDeFase("Repartir e revisão", "🔁") { sorteio({ repartir() }, { fracaoDaFigura() }, { revisao(mundo21) }) }
        val f9 = ModeloDeFase("Problemas de repartir", "⚙️") { sorteio({ problemaDeDividir() }, { parteDe() }) }
        val f10 = ModeloDeFase("Chefão: o robô gigante", "👑") { sorteio({ cestaDeRepartir() }, { balancaDePesosIguais() }, { ligarPartes() }, { problemaDeDividir() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 23: SAVANA (221 a 230)
    // Tabelas e gráficos de colunas; chance: impossível, pouco provável, muito provável, certo.

    /** Gráfico de colunas: cada coluna é um bicho, empilhado até o número de votos. */
    private fun graficoDeColunas(grande: Boolean = false): Pergunta {
        val bichos = VN.savana.sorteia(if (grande) 5 else 4)
        val k = if (grande) (4..5).random(random) else (3..4).random(random)
        val noGrafico = bichos.take(k)
        val votos = (1..if (grande) 9 else 7).toList().sorteia(k)
        val alto = votos.max()
        val itens = (alto downTo 1).flatMap { linha -> noGrafico.indices.map { c -> if (votos[c] >= linha) noGrafico[c].emoji else "" } }
        val cena = Cena.Grade(k, itens)
        val fala0 = "No gráfico, cada coluna mostra os votos de um bicho. "
        return when (random.nextInt(3)) {
            0 -> {
                val mais = random.nextBoolean()
                val i = if (mais) votos.indexOf(votos.max()) else votos.indexOf(votos.min())
                val certo = noGrafico[i]
                val outros = (bichos - certo).take(3)
                tocar(
                    if (mais) "Quem tem mais votos?" else "Quem tem menos votos?", fala0 + "Quem ganhou ${if (mais) "mais" else "menos"} votos?",
                    cena, opE(certo.emoji, certo.singular), outros.map { opE(it.emoji, it.singular) }, "${maiusc(if (certo.feminino) "a" else "o")} ${certo.singular}: ${ext(votos[i])} votos!",
                )
            }
            1 -> {
                val i = noGrafico.indices.random(random)
                val o = noGrafico[i]
                tocarNumero(
                    "Quantos votos tem ${if (o.feminino) "a" else "o"} ${o.singular}?", fala0 + "Quantos votos tem ${if (o.feminino) "a" else "o"} ${o.singular}?",
                    cena, votos[i], votos + listOf(votos[i] + 1, votos[i] - 1), "${maiusc(ext(votos[i]))} votos!", 0, 10,
                )
            }
            else -> {
                val (i, j) = noGrafico.indices.toList().sorteia(2).sortedByDescending { votos[it] }
                val a = noGrafico[i]
                val b = noGrafico[j]
                tocarNumero(
                    "${votos[i]} − ${votos[j]} = ?", fala0 + "Quantos votos ${if (a.feminino) "a" else "o"} ${a.singular} tem a mais que ${if (b.feminino) "a" else "o"} ${b.singular}?",
                    cena, votos[i] - votos[j], listOf(votos[i], votos[j], votos[i] + votos[j], votos[i] - votos[j] + 1), "${maiusc(ext(votos[i] - votos[j]))} a mais!", 0, 15,
                )
            }
        }
    }

    private fun cacaDosBichos(): Pergunta {
        val alvo = VN.savana.um()
        val k = (3..5).random(random)
        val outros = VN.savana.filter { it != alvo }
        return caca(
            23, "Ache ${if (alvo.feminino) "as" else "os"} ${ext(k, alvo.feminino)} ${alvo.plural}", "Na savana tem muitos bichos. Ache e conte ${if (alvo.feminino) "as" else "os"} ${alvo.plural}: são ${ext(k, alvo.feminino)}.",
            List(k) { opE(alvo.emoji, alvo.singular) }, List((4..6).random(random)) { outros.um().let { opE(it.emoji, it.singular) } },
            "Achou ${if (alvo.feminino) "as" else "os"} ${alvo.qtd(k)}!",
        )
    }

    /** Marcas de contagem: IIII é quatro. */
    private fun pintarDeMarcas(): Pergunta = pintar(
        "Pinte pelas marcas", "Cada casinha tem risquinhos de contagem. Conte os risquinhos e pinte com a cor daquele número.",
        (1..6).toList(), { v -> "I".repeat(v) },
    )

    private fun chance(): Pergunta {
        val (cor, nomeCor) = VN.bolinhas.um()
        val outras = VN.bolinhas.filter { it.first != cor }
        val caso = (0..3).random(random)
        val qtd = when (caso) { 0 -> 0; 1 -> 1; 2 -> (4..5).random(random); else -> 6 }
        val saco = (List(qtd) { cor } + List(6 - qtd) { outras.um().first }).shuffled(random)
        val certo = VN.chances[caso]
        val plural = if (nomeCor == "azul") "azuis" else nomeCor + "s"
        return tocar(
            "Tirar uma bolinha $nomeCor é...", "O saquinho tem estas bolinhas. Sem olhar, a gente tira uma. Tirar uma bolinha $nomeCor é impossível, pouco provável, muito provável ou certo?",
            Cena.Emojis(saco.joinToString("")), opT(certo), VN.chances.filter { it != certo }.map { opT(it) },
            when (caso) { 0 -> "Impossível: não tem nenhuma $nomeCor!"; 1 -> "Pouco provável: só tem uma $nomeCor."; 2 -> "Muito provável: quase todas são $plural."; else -> "Certo: todas são $plural!" },
        )
    }

    private fun tabelaSoma(): Pergunta {
        val k = (3..5).random(random)
        val bichos = VN.savana.sorteia(k)
        val nums = List(k) { (2..12).random(random) }
        val itens = bichos.zip(nums).flatMap { (b, n) -> listOf(b.emoji, "$n") }
        val total = nums.sum()
        return tocarNumero(
            nums.joinToString(" + ") + " = ?", "A tabela mostra quantos bichos de cada tipo foram vistos no passeio. Quantos bichos ao todo?",
            Cena.Grade(2, itens), total, listOf(total + 1, total - 1, total + 10, total - 10, nums.max()), "Ao todo, ${ext(total)} bichos!", 0, 100,
        )
    }

    private val mundo23: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Gráfico de colunas", "🦁") { graficoDeColunas() }
        val f2 = ModeloDeFase("Caça na savana", "🔎") { sorteio({ cacaDosBichos() }, { cacaDosBichos() }, { graficoDeColunas() }) }
        val f3 = ModeloDeFase("Ler o gráfico", "📊") { graficoDeColunas(grande = true) }
        val f4 = ModeloDeFase("Pintar pelas marcas", "🎨") { sorteio({ pintarDeMarcas() }, { pintarDeMarcas() }, { graficoDeColunas() }) }
        val f5 = marco("Bandeirinha da savana", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("É provável?", "🎲") { chance() }
        val f7 = ModeloDeFase("Cartas da savana", "🃏") { sorteio({ cartasDeNumeros((1..50).toList().sorteia((4..6).random(random))) }, { cartasDeNumeros((1..50).toList().sorteia(5), crescente = false) }, { chance() }) }
        val f8 = ModeloDeFase("Gráficos e revisão", "🔁") { sorteio({ graficoDeColunas() }, { chance() }, { revisao(mundo22) }) }
        val f9 = ModeloDeFase("Tabelas grandes", "🦒") { sorteio({ tabelaSoma() }, { tabelaSoma() }, { graficoDeColunas() }) }
        val f10 = ModeloDeFase("Chefão: o leão rei", "👑") { sorteio({ pintarDeMarcas() }, { cacaDosBichos() }, { graficoDeColunas() }, { tabelaSoma() }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 24: VALE DAS CORES (231 a 240)
    // Padrões e regularidades (regra mais 3, menos 2), pares e ímpares, ordem até 1000.

    private fun regraFalada(r: Int) = if (r > 0) "mais ${ext(r)}" else "menos ${ext(-r)}"

    private fun padrao(centenas: Boolean, descendo: Boolean = false): Pergunta {
        val regra = if (centenas) listOf(50, 100, -50, 25, 20).um() else if (descendo) listOf(-2, -3, -4, -5).um() else listOf(2, 3, 4, 5, -2, -3).um()
        val inicio = if (centenas) (if (regra > 0) (1..5).random(random) * 100 else (6..9).random(random) * 100) else (if (regra > 0) (1..20).random(random) else (20..40).random(random))
        val fila = List(5) { inicio + it * regra }
        val resp = fila.last()
        return if (random.nextInt(3) == 0) {
            val certa = regraFalada(regra)
            val outras = (if (centenas) listOf(50, 100, -50, 25, 20, 10, -100) else listOf(2, 3, 4, 5, -2, -3, 1, -1)).filter { it != regra }.ordenarUmaVez { kotlin.math.abs(it - regra) + random.nextDouble() }.take(3)
            tocar(
                "Qual é a regra?", "Olhe os números: " + fila.joinToString(", ") { ext(it) } + ". Qual é a regra do padrão?",
                Cena.Sequencia(fila.map { "$it" }), opT(certa), outras.map { opT(regraFalada(it)) }, "A regra é $certa!",
            )
        } else tocarNumero(
            "Que número vem depois?", "A regra é ${regraFalada(regra)}: " + fila.dropLast(1).joinToString(", ") { ext(it) } + ". Qual vem depois?",
            Cena.Sequencia(fila.dropLast(1).map { "$it" } + "?"), resp, listOf(resp + 1, resp - 1, resp + regra, resp - regra, resp + 10), maiusc(fila.joinToString(", ") { ext(it) }) + "!", 0, 1000,
        )
    }

    private fun tremDoPadrao(): Pergunta {
        val regra = listOf(3, 4, 5, 25, 50, 100).um()
        val inicio = if (regra >= 25) (0..4).random(random) * 100 + regra else (1..20).random(random)
        val nums = List(5) { inicio + it * regra }
        return tremDeNumeros(nums, 2, "Trem da regra ${regraFalada(regra)}", "A regra do trem é ${regraFalada(regra)}. Comece no ${ext(nums[0])} e arraste os vagões que faltam.", listOf(nums.last() + 1, nums[1] + 1))
    }

    private fun pescariaParOuImpar(grande: Boolean): Pergunta {
        val pares = random.nextBoolean()
        val faixa = if (grande) (100..999) else (1..50)
        val certos = faixa.filter { (it % 2 == 0) == pares }.shuffled(random).take((2..3).random(random))
        val errados = faixa.filter { (it % 2 == 0) != pares }.shuffled(random).take(3)
        val nome = if (pares) "pares" else "ímpares"
        return pescaria(
            "Pesque os números $nome", "Pesque só os peixes com números $nome. Olhe o último algarismo: ${if (pares) "zero, dois, quatro, seis ou oito" else "um, três, cinco, sete ou nove"}.",
            certos.map(::opN), errados.map(::opN), "Pescou os $nome!",
        )
    }

    private fun parOuImparTocar(): Pergunta {
        val impar = random.nextBoolean()
        val faixa = (10..999)
        val certo = faixa.filter { (it % 2 == 1) == impar }.um()
        val outros = faixa.filter { (it % 2 == 1) != impar }.sorteia(3)
        return tocar(
            "Qual é ${if (impar) "ímpar" else "par"}?", "Qual destes números é ${if (impar) "ímpar" else "par"}? Olhe o último algarismo.",
            Cena.Nenhuma, opN(certo), outros.map(::opN), "${maiusc(ext(certo))} é ${if (impar) "ímpar" else "par"}!",
        )
    }

    private fun ordemAte1000(): Pergunta {
        val a = (100..997).random(random)
        return if (random.nextBoolean()) tocarNumero(
            "Entre ${a} e ${a + 2}?", "Que número fica entre ${ext(a)} e ${ext(a + 2)}?", Cena.Sequencia(listOf("$a", "?", "${a + 2}")), a + 1,
            listOf(a, a + 2, a + 10, a + 11, a - 1), "${maiusc(ext(a + 1))}!", 100, 1000,
        ) else compararCentenas()
    }

    private fun baloesEmOrdem(): Pergunta {
        val nums = (100..999).toList().sorteia((4..5).random(random)).sorted()
        return baloes(
            "Estoure do menor para o maior", "Estoure os balões em ordem, do menor número para o maior. Olhe primeiro as centenas!",
            nums.map(::opN), emptyList(), maiusc(nums.joinToString(", ") { ext(it) }) + "!", emOrdem = true,
        )
    }

    private val mundo24: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Qual é a regra?", "🌈") { padrao(centenas = false) }
        val f2 = ModeloDeFase("Trem da regra", "🚂") { sorteio({ tremDoPadrao() }, { tremDoPadrao() }, { padrao(false) }) }
        val f3 = ModeloDeFase("Padrões que descem", "🖍️") { padrao(centenas = false, descendo = true) }
        val f4 = ModeloDeFase("Pescaria de pares e ímpares", "🎣") { sorteio({ pescariaParOuImpar(false) }, { pescariaParOuImpar(false) }, { parOuImparTocar() }) }
        val f5 = marco("Bandeirinha das cores", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Ordem até 1000", "🎨") { ordemAte1000() }
        val f7 = ModeloDeFase("Balões em ordem", "🎈") { sorteio({ baloesEmOrdem() }, { baloesEmOrdem() }, { ordemAte1000() }) }
        val f8 = ModeloDeFase("Padrões e revisão", "🔁") { sorteio({ padrao(false) }, { parOuImparTocar() }, { revisao(mundo23) }) }
        val f9 = ModeloDeFase("Padrões de centenas", "🖌️") { sorteio({ padrao(centenas = true) }, { pescariaParOuImpar(true) }, { parOuImparTocar() }) }
        val f10 = ModeloDeFase("Chefão: o camaleão", "👑") { sorteio({ tremDoPadrao() }, { pescariaParOuImpar(true) }, { baloesEmOrdem() }, { padrao(true) }) }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }

    // ================================================================== MUNDO 25: CASTELO DAS ESTRELAS (241 a 250)
    // Revisão do 2º ano: problemas de dois passos com dinheiro, tempo e medidas; fase 250, o grande chefão final.

    private fun trocoDoisPassos(): Pergunta {
        val (p1, p2) = VN.produtos.sorteia(2)
        val a = (2..9).random(random)
        val b = (2..9).random(random)
        val pago = listOf(20, 50).first { it > a + b }
        val troco = pago - a - b
        return tocar(
            "R$ $pago − R$ $a − R$ $b = ?",
            "Comprei ${p1.nome} por ${VN.dinheiroFalado(a * 100)} e ${p2.nome} por ${VN.dinheiroFalado(b * 100)}. Paguei com uma nota de ${VN.dinheiroFalado(pago * 100)}. Quanto recebo de troco?",
            Cena.Emojis(p1.emoji + p2.emoji + " 💵"), opDinheiro(troco * 100),
            errados(troco, listOf(troco + 1, troco - 1, a + b, pago - a, troco + 10), 1, 60).map { opDinheiro(it * 100) },
            "${maiusc(VN.dinheiroFalado(pago * 100))} menos ${VN.dinheiroFalado((a + b) * 100)}: o troco é ${VN.dinheiroFalado(troco * 100)}!",
        )
    }

    private fun mercadinhoFinal(): Pergunta = if (random.nextBoolean()) {
        val pago = listOf(1000, 2000).um()
        mercadinho((1 until pago / 50).random(random) * 50, troco = true, pago = pago)
    } else mercadinho((2..30).random(random) * 100 + listOf(0, 50, 25).um())

    private fun tempoDoisPassos(): Pergunta {
        val h = (1..8).random(random)
        val meia = random.nextBoolean()
        val m = if (meia) 30 else 0
        val dur = (1..3).random(random)
        val fim = h + dur
        val op = { hh: Int -> opT(VN.horaEscrita(hh, m), VN.falaDaHora(hh, m)) }
        return tocar(
            "$h + $dur = ?", "O filme começou às ${VN.falaDaHora(h, m)} e durou ${if (dur == 1) "uma hora" else "${ext(dur, true)} horas"}. A que horas acabou?",
            Cena.Emojis("🎬 " + emojiDaHora(h, m)), op(fim), errados(fim, listOf(fim + 1, fim - 1, h, fim + 2), 1, 12).map(op), "Acabou às ${VN.falaDaHora(fim, m)}!",
        )
    }

    private fun relogioFinal(): Pergunta = relogio((1..12).random(random), listOf(0, 15, 30, 45, (1..11).random(random) * 5).um(), digital = true)

    private fun medidasDoisPassos(): Pergunta {
        val a = (30..90).random(random)
        val b = (5..20).random(random)
        val c = (5..minOf(20, a - b - 1)).random(random)
        return historinha(
            "A fita mágica tinha ${ext(a)} centímetros. A fada cortou ${ext(b)} centímetros e depois mais ${ext(c)}. Quantos centímetros sobraram?",
            listOf(a, b, c), listOf("−", "−"), "🎀✂️",
        )
    }

    private fun balancaDeDois(): Pergunta {
        val x = (2..9).random(random)
        val y = (2..9).random(random)
        val alvo = x + y
        val p1 = (1 until alvo).random(random)
        val distr = (1..12).filter { it != p1 && it != alvo - p1 }.sorteia(2)
        return balanca(
            "Equilibre $x kg + $y kg", "De um lado tem um pacote de ${ext(x)} quilos e outro de ${ext(y)} quilos. Coloque pesos do outro lado até a balança ficar reta.",
            listOf(x, y), listOf(p1, alvo - p1), distr, "${maiusc(ext(x))} mais ${ext(y)} é ${ext(alvo)} quilos dos dois lados!", "📦",
        )
    }

    private fun dominoMisturado(): Pergunta {
        val faixa = (12..40).toList()
        val valores = faixa.sorteia(4)
        val sinal = listOf("+", "−").um()
        val jeito = if (sinal == "+") somas(5) else subtracoes(60, 5)
        return dominoDeContas(valores, sinal, jeito, faixa.filter { it !in valores }.sorteia(2))
    }

    private fun problemaMisto(): Pergunta = sorteio(
        { val a = (2..5).random(random); val b = (3..9).random(random); val c = (2..9).random(random)
            historinha("Cada pacote tem ${ext(b)} estrelinhas. A princesa comprou ${ext(a)} pacotes e ganhou mais ${ext(c)}. Quantas estrelinhas ela tem?", listOf(a, b, c), listOf("×", "+"), "⭐") },
        { val a = (20..60).random(random); val c = (5..19).random(random); val d = (5..30).random(random)
            historinha("O mago tinha ${ext(a, true)} poções. Deu ${ext(c, true)} para a fada e depois fez mais ${ext(d, true)}. Quantas poções ele tem agora?", listOf(a, c, d), listOf("−", "+"), "🧙🧪") },
        { conta(listOf((20..60).random(random), (15..39).random(random), (5..20).random(random)), listOf("+", "−")) },
    )

    /** Grande chefão final: as perguntas giram pelas cinco faixas de mundos (1 a 5, 6 a 10, 11 a 15, 16 a 20, 21 a 25). */
    private var faixaDoChefao = 0

    private fun grandeChefao(): Pergunta {
        val faixa = faixaDoChefao++ % 5
        return when (faixa) {
            0 -> ate50.um().criar()
            1 -> (mundo6 + mundo7 + mundo8 + mundo9 + mundo10).um().criar()
            2 -> (mundo11 + mundo12 + mundo13 + mundo14 + mundo15).um().criar()
            3 -> (mundo16 + mundo17 + mundo18 + mundo19 + mundo20).um().criar()
            else -> (mundo21 + mundo22 + mundo23 + mundo24).um().criar()
        }
    }

    private val mundo25: List<ModeloDeFase> by lazy {
        val f1 = ModeloDeFase("Troco em dois passos", "🌟") { trocoDoisPassos() }
        val f2 = ModeloDeFase("Mercadinho das estrelas", "🛒") { sorteio({ mercadinhoFinal() }, { mercadinhoFinal() }, { trocoDoisPassos() }) }
        val f3 = ModeloDeFase("Que horas acaba?", "🎬") { tempoDoisPassos() }
        val f4 = ModeloDeFase("Relógio de quartos de hora", "⏰") { sorteio({ relogioFinal() }, { relogioFinal() }, { tempoDoisPassos() }) }
        val f5 = marco("Bandeirinha das estrelas", "🚩", f1, f2, f3, f4)
        val f6 = ModeloDeFase("Medidas em dois passos", "📏") { medidasDoisPassos() }
        val f7 = ModeloDeFase("Balança das estrelas", "⚖️") { sorteio({ balancaDeDois() }, { balancaDeDois() }, { medidasDoisPassos() }) }
        val f8 = ModeloDeFase("Dominó e revisão", "🔁") { sorteio({ dominoMisturado() }, { problemaMisto() }, { revisao(mundo24) }) }
        val f9 = ModeloDeFase("Problemas das estrelas", "🧠") { problemaMisto() }
        val f10 = ModeloDeFase("Grande chefão final", "🏆") { grandeChefao() }
        listOf(f1, f2, f3, f4, f5, f6, f7, f8, f9, f10)
    }
}
