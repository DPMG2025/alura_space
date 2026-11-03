package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.fases.FasesNumeros
import br.com.casinhadosaber.dominio.fases.VocabularioNumeros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.random.Random

/** As fases 51 a 250 do caminho Números (mundos 6 a 25, plano em docs/plano-1000-fases.md, seção 4.1). */
class FasesNumerosTest {

    private val g = GeradorDeFases(Random(2025))
    private val fases = g.fases(Trilha.MATEMATICA)
    private val novas = fases.drop(50)

    // ------------------------------------------------------------------ contas

    private val sinais = setOf("+", "−", "×", "÷")

    /** Calcula "12 + 30 − 5" (da esquerda para a direita), ou null se não for uma conta. Aceita "40+7" sem espaços. */
    private fun calcular(texto: String): Int? {
        val tokens = Regex("\\d+|[+−×÷]").findAll(texto.replace("R$ ", "")).map { it.value }.toList()
        if (tokens.size < 3 || tokens.size % 2 == 0) return null
        if (texto.replace("R$ ", "").replace(Regex("[\\d+−×÷ ]"), "").isNotEmpty()) return null
        var total = tokens[0].toIntOrNull() ?: return null
        for (i in 1 until tokens.size step 2) {
            val n = tokens[i + 1].toIntOrNull() ?: return null
            total = when (tokens[i]) {
                "+" -> total + n
                "−" -> total - n
                "×" -> total * n
                "÷" -> { assertEquals("divisão com resto em $texto", 0, total % n); total / n }
                else -> return null
            }
        }
        return total
    }

    /** O número da opção certa ("12", "R$ 7", "3 h"); null quando não é um número inteiro simples. */
    private fun numeroDa(o: Opcao): Int? {
        val t = (o.visual as? Visual.Texto)?.texto?.removePrefix("R$ ") ?: return null
        if (',' in t) return null
        val m = Regex("^(\\d+)(.*)$").find(t) ?: return null
        val resto = m.groupValues[2]
        return if (resto.isEmpty() || resto.startsWith(" ")) m.groupValues[1].toInt() else null
    }

    private fun textoDe(o: Opcao) = (o.visual as? Visual.Texto)?.texto

    private var contasConferidas = 0

    /** Confere a conta da instrução contra a opção certa. */
    private fun conferirConta(fase: Fase, p: Pergunta) {
        if (p.atividade != Atividade.Escolha) return
        val certa = numeroDa(p.opcoes[p.indiceCorreto]) ?: return
        val instr = p.instrucao.replace("R$ ", "")
        Regex("^(?:Quanto é )?(\\d+(?: [+−×÷] \\d+)+)(?: = \\?|\\?)$").find(instr)?.let {
            assertEquals("$fase: ${p.instrucao}", calcular(it.groupValues[1]), certa)
            contasConferidas++
            return
        }
        Regex("^(\\d+) ([+−×÷]) \\? = (\\d+)$").find(instr)?.let {
            val (a, s, c) = it.destructured
            assertEquals("$fase: ${p.instrucao}", c.toInt(), calcular("$a $s $certa"))
            contasConferidas++
            return
        }
        Regex("^\\? ([+−×÷]) (\\d+) = (\\d+)$").find(instr)?.let {
            val (s, b, c) = it.destructured
            assertEquals("$fase: ${p.instrucao}", c.toInt(), calcular("$certa $s $b"))
            contasConferidas++
        }
    }

    // ------------------------------------------------------------------ validação de cada pergunta

    private fun validar(fase: Fase, p: Pergunta) {
        assertTrue("$fase: sem fala", p.fala.isNotEmpty() && p.fala.all { it.texto.isNotBlank() })
        assertTrue("$fase: sem instrução", p.instrucao.isNotBlank())
        assertTrue("$fase: índice certo", p.indiceCorreto in p.opcoes.indices)
        when (val a = p.atividade) {
            Atividade.Escolha -> {
                assertEquals("$fase: 4 opções em ${p.instrucao}", 4, p.opcoes.size)
                assertEquals("$fase: opções repetidas em ${p.instrucao}: ${p.opcoes}", 4, p.opcoes.map { it.visual }.toSet().size)
                conferirConta(fase, p)
            }
            is Atividade.Cesta -> assertTrue("$fase: cesta impossível", a.alvo in 1..a.certos.size)
            is Atividade.Memoria -> {
                assertTrue(a.pares.size in 2..6)
                assertEquals(a.pares.size, a.pares.map { it.first }.toSet().size)
                for ((x, y) in a.pares) {
                    calcular(x)?.let { assertEquals("$fase: memória $x", y.toInt(), it) }
                    calcular(y)?.let { assertEquals("$fase: memória $y", x.toInt(), it) }
                }
            }
            is Atividade.Tracar -> assertTrue(a.letra.length == 1 && a.letra[0].isDigit())
            is Pescaria -> conferirAlvo(fase, p.instrucao, a.peixes, a.certos)
            is EstourarBaloes -> {
                conferirAlvo(fase, p.instrucao, a.baloes, a.certos.toSet())
                if (a.emOrdem) {
                    val valores = a.certos.map { textoDe(a.baloes[it])!!.toInt() }
                    assertEquals("$fase: balões fora de ordem", valores.sorted(), valores)
                }
            }
            is LigarPares -> for ((e, d) in a.pares) conferirPar(fase, p.instrucao, a.esquerda[e], a.direita[d])
            is Trem -> conferirOrdem(fase, a.vagoes.map { textoDe(it)!! })
            is CartasViradas -> conferirOrdem(fase, a.ordem.map { textoDe(a.cartas[it])!! })
            is QuebraCabeca -> if (a.rotulos.isNotEmpty()) conferirOrdem(fase, a.rotulos)
            is Domino -> {
                assertEquals(a.jogadas, a.ordem.size)
                for (peca in a.mao) for (meia in listOf(peca.a, peca.b)) {
                    val t = (meia.visual as Visual.Texto).texto
                    assertEquals("$fase: dominó $t", meia.chave.toInt(), calcular(t) ?: t.toInt())
                }
            }
            is PintarPorCodigo -> for (c in a.casas) if (c.codigo != null) {
                val v = if (c.rotulo.all { it == 'I' }) c.rotulo.length else calcular(c.rotulo)
                assertEquals("$fase: casinha ${c.rotulo}", c.codigo!!.toInt(), v)
            }
            is MontarFrase -> {
                val i = a.palavras.indexOf("=")
                assertTrue("$fase: conta sem igual", i > 0)
                assertEquals("$fase: ${a.frase}", a.palavras[i + 1].toInt(), calcular(a.palavras.take(i).joinToString(" ")))
            }
            is CacaNaCena -> {
                val certos = a.certos.map { a.itens[it].opcao.visual }.toSet()
                val outros = a.itens.indices.filter { it !in a.certos }.map { a.itens[it].opcao.visual }.toSet()
                assertTrue("$fase: figura certa e errada iguais", certos.none { it in outros })
            }
            is Balanca, is Mercadinho, is Relogio, is EscondeEsconde, is SigaASequencia, is Ditado, is ProgramarRobo -> Unit // regras no init
            else -> fail("$fase: atividade inesperada $a")
        }
    }

    /** Pescaria e balões: "contas que dão N", "números pares", "de 5 em 5". */
    private fun conferirAlvo(fase: Fase, instrucao: String, ops: List<Opcao>, certos: Set<Int>) {
        val textos = ops.map { textoDe(it)!! }
        Regex("contas que dão (\\d+)").find(instrucao)?.let { m ->
            val alvo = m.groupValues[1].toInt()
            textos.forEachIndexed { i, t -> assertEquals("$fase: $instrucao: $t", i in certos, calcular(t) == alvo) }
        }
        if ("números pares" in instrucao) textos.forEachIndexed { i, t -> assertEquals("$fase: par $t", i in certos, t.toInt() % 2 == 0) }
        if ("números ímpares" in instrucao) textos.forEachIndexed { i, t -> assertEquals("$fase: ímpar $t", i in certos, t.toInt() % 2 == 1) }
        Regex("^Estoure os números de (\\d+) em").find(instrucao)?.let { m ->
            val passo = m.groupValues[1].toInt()
            textos.forEachIndexed { i, t -> assertEquals("$fase: múltiplo $t", i in certos, t.toInt() % passo == 0) }
        }
    }

    private fun valorDeParte(t: String): Int? {
        Regex("^(metade|terço|quarto) de (\\d+)$").find(t)?.let { m ->
            val k = mapOf("metade" to 2, "terço" to 3, "quarto" to 4).getValue(m.groupValues[1])
            return m.groupValues[2].toInt() / k
        }
        return null
    }

    private fun centavos(t: String): Int? {
        val partes = t.removePrefix("R$ ").split(" + ")
        return partes.sumOf { p ->
            val (r, c) = if (',' in p) p.split(",").let { it[0] to it[1] } else p to "0"
            (r.toIntOrNull() ?: return null) * 100 + (c.toIntOrNull() ?: return null)
        }
    }

    private fun conferirPar(fase: Fase, instrucao: String, e: Opcao, d: Opcao) {
        val te = textoDe(e)
        val td = textoDe(d)
        when {
            "amigos do 10" in instrucao -> assertEquals("$fase: amigos do 10", 10, te!!.toInt() + td!!.toInt())
            "dinheiro" in instrucao -> assertEquals("$fase: $te e $td", centavos(te!!), centavos(td!!))
            te != null && td != null && valorDeParte(te) != null -> assertEquals("$fase: $te", td.toInt(), valorDeParte(te))
            te != null && td != null && calcular(te) != null -> assertEquals("$fase: $te", td.toInt(), calcular(te))
            te != null && td != null && calcular(td) != null -> assertEquals("$fase: $td", te.toInt(), calcular(td))
            else -> Unit // figura e nome da forma
        }
    }

    /** Vagões, cartas e peças em ordem: números sempre crescendo (ou sempre diminuindo); dias e meses seguidos. */
    private fun conferirOrdem(fase: Fase, textos: List<String>) {
        val dias = VocabularioNumeros.diasDaSemana
        val meses = VocabularioNumeros.meses
        when {
            textos.all { it in dias } -> textos.zipWithNext().forEach { (a, b) -> assertEquals("$fase: $textos", (dias.indexOf(a) + 1) % 7, dias.indexOf(b)) }
            textos.all { it in meses } -> textos.zipWithNext().forEach { (a, b) -> assertEquals("$fase: $textos", (meses.indexOf(a) + 1) % 12, meses.indexOf(b)) }
            else -> {
                val nums = textos.map { Regex("^\\d+").find(it)!!.value.toInt() }
                val subindo = nums.zipWithNext().all { (a, b) -> b > a }
                val descendo = nums.zipWithNext().all { (a, b) -> b < a }
                assertTrue("$fase: fora de ordem $textos", subindo || descendo)
            }
        }
    }

    // ------------------------------------------------------------------ testes

    @Test
    fun `cada fase de 51 a 250 gera cinco perguntas validas em 200 sorteios`() {
        for (fase in novas) {
            repeat(200) {
                val perguntas = g.perguntas(fase)
                assertEquals("$fase", GeradorDeFases.PERGUNTAS_POR_FASE, perguntas.size)
                perguntas.forEach { validar(fase, it) }
                val tipos = perguntas.map { GeradorDeFases.tipoDeMexer(it) }
                for (i in 2 until tipos.size) {
                    assertFalse("$fase: 3 iguais seguidas $tipos", tipos[i] != null && tipos[i] == tipos[i - 1] && tipos[i] == tipos[i - 2])
                }
            }
        }
        assertTrue("poucas contas conferidas: $contasConferidas", contasConferidas > 20_000)
    }

    @Test
    fun `nenhuma fase nova e provisoria e todas tem nome proprio`() {
        val nomes = novas.map { it.titulo }
        assertTrue(nomes.all { it.isNotBlank() })
        assertTrue("nomes repetidos demais: ${nomes.size - nomes.toSet().size}", nomes.toSet().size >= 185)
        // As fases provisórias repetiam os nomes do mundo 5.
        val mundo5 = fases.subList(40, 50).map { it.titulo }.toSet()
        assertTrue(nomes.none { it in mundo5 })
        assertTrue(novas.filter { Mundos.ehChefao(it) }.all { it.emoji == "👑" || it.numero == 249 })
    }

    /** Números de uma pergunta: os da pergunta e da resposta (núcleo) e os das opções erradas. */
    private fun numerosDe(p: Pergunta): Pair<List<Int>, List<Int>> {
        val ints = { t: String -> Regex("\\d+").findAll(t).map { it.value.toInt() }.toList() }
        val textos = { ops: List<Opcao> -> ops.mapNotNull { textoDe(it) }.flatMap(ints) }
        return when (val a = p.atividade) {
            Atividade.Escolha -> (ints(p.instrucao) + textos(listOf(p.opcoes[p.indiceCorreto]))) to textos(p.opcoes)
            is Pescaria -> textos(a.peixes) to emptyList()
            is EstourarBaloes -> textos(a.baloes) to emptyList()
            is LigarPares -> textos(a.esquerda + a.direita) to emptyList()
            is Trem -> textos(a.vagoes + a.extras) to emptyList()
            is CartasViradas -> textos(a.cartas) to emptyList()
            is Balanca -> (a.ladoEsquerdo + a.pesos) to emptyList()
            is Domino -> (a.mao.flatMap { listOf(it.a, it.b) } + a.ponta).mapNotNull { (it.visual as? Visual.Texto)?.texto }.flatMap(ints) to emptyList()
            is PintarPorCodigo -> a.legenda.map { it.codigo.toInt() } to emptyList()
            is Mercadinho -> listOf(a.preco / 100, if (a.troco) a.pago / 100 else 0) to emptyList()
            is Atividade.Cesta -> listOf(a.alvo, a.certos.size) to emptyList()
            is Atividade.Memoria -> a.pares.flatMap { ints(it.first) + ints(it.second) } to emptyList()
            is QuebraCabeca -> a.rotulos.flatMap(ints) to emptyList()
            is EscondeEsconde -> textos(a.esconderijos) to emptyList()
            is SigaASequencia -> textos(a.botoes) to emptyList()
            is MontarFrase -> (a.palavras + a.extras).flatMap(ints) to emptyList()
            else -> emptyList<Int>() to emptyList()
        }
    }

    @Test
    fun `dificuldade nunca cai de um mundo para o seguinte e o teto de cada mundo vale nas perguntas`() {
        val regua = VocabularioNumeros.regua
        assertEquals((6..25).toList(), regua.map { it.mundo })
        regua.zipWithNext().forEach { (a, b) ->
            assertTrue("o nível caiu do mundo ${a.mundo} para o ${b.mundo}", b.nivel > a.nivel)
            assertTrue(b.teto >= a.teto && b.passos >= a.passos)
        }
        val caminho = FasesNumeros(Random(77))
        val maiorPorMundo = mutableMapOf<Int, Int>()
        val doisPassos = mutableSetOf<Int>()
        for (r in regua) for (n in (r.mundo - 1) * 10 + 1..r.mundo * 10) repeat(150) {
            val p = caminho.fase(n).criar()
            val (nucleo, opcoes) = numerosDe(p)
            nucleo.forEach { assertTrue("fase $n (mundo ${r.mundo}): $it passa do teto ${r.teto} em ${p.instrucao}", it <= r.teto) }
            opcoes.forEach { assertTrue("fase $n: opção $it longe do teto ${r.teto} em ${p.instrucao}", it <= r.teto + maxOf(3, r.teto / 10)) }
            maiorPorMundo[r.mundo] = maxOf(maiorPorMundo[r.mundo] ?: 0, nucleo.maxOrNull() ?: 0)
            if (Regex("\\d+ [+−×÷] \\d+ [+−×÷] \\d+").containsMatchIn(p.instrucao)) doisPassos += r.mundo
        }
        // A régua aparece de verdade: números maiores que 30 a partir do mundo 9 e maiores que 100 a partir do 18.
        assertTrue(maiorPorMundo.getValue(9) > 30 && maiorPorMundo.getValue(10) > 30)
        for (m in listOf(18, 24)) assertTrue("mundo $m sem centenas: ${maiorPorMundo[m]}", maiorPorMundo.getValue(m) > 100)
        for (m in listOf(19, 20, 25)) assertTrue("mundo $m sem conta de dois passos", m in doisPassos)
    }

    @Test
    fun `cada mundo mistura pelo menos quatro atividades e o chefao tres`() {
        val caminho = FasesNumeros(Random(5))
        val novosTipos = mutableSetOf<String>()
        for (m in 6..25) {
            val tiposDoMundo = mutableSetOf<String>()
            for (n in (m - 1) * 10 + 1..m * 10) {
                val tipos = mutableSetOf<String>()
                repeat(120) { tipos += caminho.fase(n).criar().atividade::class.simpleName.orEmpty() }
                tiposDoMundo += tipos
                if (n % 10 == 0 && n != 250) assertTrue("chefão $n com poucos tipos: $tipos", tipos.size >= 3)
                // As fases de mexer do esqueleto (2, 4, 7) e o chefão misturam tipos de resposta.
                if (n % 10 in listOf(2, 4, 7)) assertTrue("fase $n sem mistura: $tipos", tipos.size >= 2)
            }
            assertTrue("mundo $m com poucos tipos: $tiposDoMundo", tiposDoMundo.size >= 4)
            novosTipos += tiposDoMundo
        }
        val dezessete = setOf(
            "Pescaria", "EstourarBaloes", "LigarPares", "QuebraCabeca", "Trem", "Balanca", "CacaNaCena", "SigaASequencia", "PintarPorCodigo",
            "Ditado", "Domino", "EscondeEsconde", "CartasViradas", "Mercadinho", "Relogio", "ProgramarRobo", "MontarFrase",
        )
        assertEquals("tipos novos que não aparecem", emptySet<String>(), dezessete - novosTipos)
    }

    @Test
    fun `numeros por extenso ate mil e dinheiro falado`() {
        assertEquals("trezentos e quarenta e cinco", VocabularioNumeros.extenso(345))
        assertEquals("duzentas e uma", VocabularioNumeros.extenso(201, feminino = true))
        assertEquals("cento e dez", VocabularioNumeros.extenso(110))
        assertEquals("quinhentos", VocabularioNumeros.extenso(500))
        assertEquals("mil", VocabularioNumeros.extenso(1000))
        assertEquals("cem", VocabularioNumeros.extenso(100))
        for (n in 0..1000) assertFalse(VocabularioNumeros.extenso(n).any { it.isDigit() })
        assertEquals("terceira", VocabularioNumeros.ordinal(3, feminino = true))
        assertEquals("R$ 2,50", VocabularioNumeros.dinheiroEscrito(250))
        assertEquals("dois reais e cinquenta centavos", VocabularioNumeros.dinheiroFalado(250))
        assertEquals("cinquenta centavos", VocabularioNumeros.dinheiroFalado(50))
        assertEquals("três e meia", VocabularioNumeros.falaDaHora(3, 30))
        assertEquals("3h05", VocabularioNumeros.horaEscrita(3, 5))
    }
}
