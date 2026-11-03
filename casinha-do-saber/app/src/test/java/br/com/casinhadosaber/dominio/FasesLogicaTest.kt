package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.fases.FasesLogica
import br.com.casinhadosaber.dominio.fases.VocabularioLogica
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** As fases 51 a 250 do caminho Lógica (mundos 6 a 25): perguntas válidas, solução única e progressão. */
class FasesLogicaTest {

    private val logica = FasesLogica(Random(42))

    /** Cada fase de 51 a 250 sorteada [vezes] vezes. */
    private fun sorteios(vezes: Int, fases: IntRange = 51..250): Sequence<Pair<Int, Pergunta>> =
        fases.asSequence().flatMap { n -> val m = logica.fase(n); (0 until vezes).asSequence().map { n to m.criar() } }

    private fun tipo(p: Pergunta) = p.atividade::class.simpleName ?: "?"

    private fun emojiDe(o: Opcao) = (o.visual as? Visual.Emoji)?.emoji

    // ------------------------------------------------------------------ validade

    @Test
    fun `fases 51 a 250 sao validas em 200 sorteios`() {
        for ((n, p) in sorteios(200)) {
            val onde = "fase $n (${logica.fase(n).titulo}): ${p.instrucao}"
            assertTrue("$onde sem fala", p.fala.isNotEmpty() && p.fala.all { it.texto.isNotBlank() })
            assertTrue("$onde sem instrução", p.instrucao.isNotBlank())
            validar(onde, p)
        }
    }

    private fun validar(onde: String, p: Pergunta) {
        when (val a = p.atividade) {
            Atividade.Escolha -> {
                assertEquals("$onde: 4 opções", 4, p.opcoes.size)
                val certa = p.opcoes[p.indiceCorreto].visual
                assertEquals("$onde: certa repetida", 1, p.opcoes.count { it.visual == certa })
                if (p.instrucao != "Qual é diferente?") assertEquals("$onde: opções repetidas ${p.opcoes}", 4, p.opcoes.map { it.visual }.toSet().size)
                for (o in p.opcoes) when (val v = o.visual) {
                    is Visual.Texto -> assertTrue(onde, v.texto.isNotBlank())
                    is Visual.Emoji -> assertTrue(onde, v.emoji.isNotBlank())
                    is Visual.Sombra -> assertTrue(onde, v.emoji.isNotBlank())
                    is Visual.Seta -> assertTrue("$onde: seta sem fala", o.fala.isNotEmpty())
                    is Visual.Cor -> Unit
                }
            }
            is Atividade.Cesta -> {
                assertTrue(onde, a.alvo in 1..a.certos.size && a.certos.none { it in a.errados })
                assertEquals("$onde: figura repetida na cesta", a.certos.size, a.certos.toSet().size)
            }
            is Atividade.Memoria -> assertTrue(onde, a.pares.size in 2..6 && a.pares.map { it.first }.toSet().size == a.pares.size)
            is LigarPares -> {
                // Sombras: cada figura vai para a sombra dela.
                for ((e, d) in a.pares) {
                    val sombra = a.direita[d].visual as? Visual.Sombra ?: continue
                    assertEquals(onde, emojiDe(a.esquerda[e]), sombra.emoji)
                }
                if (a.esquerda.all { it.visual is Visual.Texto }) {
                    // Quem é quem: as pistas da fala deixam um jeito só de ligar, e é o da resposta.
                    conferirQuemEQuem(onde, p.fala.joinToString(" ") { it.texto }, a.esquerda.map { (it.visual as Visual.Texto).texto }, a.direita, a.pares)
                }
            }
            is Domino -> assertEquals(onde, a.jogadas, a.ordem.size)
            is Trem -> assertEquals("$onde: vagões iguais", a.vagoes.size, a.vagoes.map { it.visual }.toSet().size)
            is CartasViradas -> assertEquals("$onde: cartas iguais", a.cartas.size, a.cartas.map { it.visual }.toSet().size)
            is SigaASequencia -> assertTrue(onde, a.sequencia.toSet().size >= 2)
            is EscondeEsconde -> assertEquals("$onde: esconderijos iguais", a.esconderijos.size, a.esconderijos.map { it.visual }.toSet().size)
            is ProgramarRobo -> conferirRobo(onde, a)
            is Pescaria, is QuebraCabeca, is Balanca, is CacaNaCena, is PintarPorCodigo -> Unit
            else -> org.junit.Assert.fail("$onde: tipo inesperado ${a::class.simpleName}")
        }
    }

    // ------------------------------------------------------------------ robô

    private fun conferirRobo(onde: String, r: ProgramarRobo) {
        val programa = FasesLogica.programaDoRobo(r)
        assertEquals("$onde: programa mais curto", r.menorCaminho, programa.size)
        assertTrue("$onde: programa maior que a fila", programa.size <= r.maxComandos)
        var l = r.inicio / r.colunas
        var c = r.inicio % r.colunas
        for (seta in programa) {
            when (seta) {
                DirecaoDaSeta.CIMA -> l--
                DirecaoDaSeta.BAIXO -> l++
                DirecaoDaSeta.ESQUERDA -> c--
                DirecaoDaSeta.DIREITA -> c++
            }
            assertTrue("$onde: robô saiu da grade", l in 0 until r.linhas && c in 0 until r.colunas)
            assertFalse("$onde: robô bateu na pedra", l * r.colunas + c in r.obstaculos)
        }
        assertEquals("$onde: robô não chegou na bateria", r.chegada, l * r.colunas + c)
    }

    @Test
    fun `robo alcanca a bateria com a resposta certa e as perguntas do robo tem a casinha certa`() {
        var robos = 0
        var paradas = 0
        var menores = 0
        for ((n, p) in sorteios(60)) {
            val onde = "fase $n: ${p.instrucao}"
            (p.atividade as? ProgramarRobo)?.let { conferirRobo(onde, it); robos++ }
            val grade = p.cena as? Cena.Grade ?: continue
            if (p.instrucao.startsWith("Onde o robô para?")) {
                paradas++
                // "Onde o robô para? ➡️ ×2  ⬇️": anda pelas setas e confere a figura onde ele para.
                val setas = mapOf("⬆️" to (-1 to 0), "⬇️" to (1 to 0), "⬅️" to (0 to -1), "➡️" to (0 to 1))
                val partes = p.instrucao.removePrefix("Onde o robô para?").trim().split(" ").filter { it.isNotBlank() }
                var pos = grade.itens.indexOf("🤖")
                var ultima: Pair<Int, Int>? = null
                for (parte in partes) {
                    if (parte.startsWith("×")) {
                        repeat(parte.removePrefix("×").toInt() - 1) { pos = andar(grade, pos, ultima!!, onde) }
                    } else {
                        ultima = setas.getValue(parte)
                        pos = andar(grade, pos, ultima!!, onde)
                    }
                }
                assertEquals(onde, emojiDe(p.opcoes[p.indiceCorreto]), grade.itens[pos])
            }
            if (p.instrucao == "Quantos passos até a bateria?") {
                menores++
                val inicio = grade.itens.indexOf("🤖")
                val bloqueadas = grade.itens.indices.filter { grade.itens[it] == "🧱" }.toSet()
                val r = ProgramarRobo(grade.linhas, grade.colunas, inicio, grade.itens.indexOf("🔋"), bloqueadas, repetir = true)
                assertEquals(onde, r.menorCaminho.toString(), (p.opcoes[p.indiceCorreto].visual as Visual.Texto).texto)
            }
        }
        assertTrue("poucos robôs: $robos", robos > 500)
        assertTrue("poucas perguntas de onde o robô para: $paradas", paradas > 100)
        assertTrue("poucas perguntas de caminho mais curto: $menores", menores > 50)
    }

    private fun andar(g: Cena.Grade, pos: Int, d: Pair<Int, Int>, onde: String): Int {
        val l = pos / g.colunas + d.first
        val c = pos % g.colunas + d.second
        assertTrue("$onde: robô saiu do quadro", l in 0 until g.linhas && c in 0 until g.colunas)
        return l * g.colunas + c
    }

    // ------------------------------------------------------------------ sudoku e quadros

    /** Todas as soluções (até [limite]) do quadro: linhas, colunas e, com [bloco] > 0, quadradinhos. */
    private fun solucoes(grade: IntArray, n: Int, bloco: Int, limite: Int, achadas: MutableList<IntArray>) {
        if (achadas.size >= limite) return
        val i = grade.indexOf(-1)
        if (i < 0) {
            achadas += grade.copyOf()
            return
        }
        val l = i / n
        val c = i % n
        for (v in 0 until n) {
            val ok = (0 until n).none { grade[l * n + it] == v || grade[it * n + c] == v } &&
                (bloco == 0 || (0 until n * n).none { j -> j / n / bloco == l / bloco && j % n / bloco == c / bloco && grade[j] == v })
            if (!ok) continue
            grade[i] = v
            solucoes(grade, n, bloco, limite, achadas)
            grade[i] = -1
        }
    }

    @Test
    fun `sudoku tem solucao unica e a resposta certa`() {
        var vistos = 0
        var seis = 0
        for ((n, p) in sorteios(40)) {
            val g = p.cena as? Cena.Grade ?: continue
            if (p.instrucao != "Qual figura vai na casinha amarela?") continue
            vistos++
            val lado = g.colunas
            assertEquals(lado, g.linhas)
            assertTrue("fase $n: sudoku $lado", lado == 4 || lado == 6)
            if (lado == 6) seis++
            val figuras = g.itens.filter { it !in listOf("?", "", Cena.Grade.TAMPADA) }.distinct()
            assertTrue("fase $n: figuras demais", figuras.size <= lado)
            val todas = (figuras + p.opcoes.mapNotNull { emojiDe(it) }).distinct()
            val quadro = IntArray(lado * lado) { i -> g.itens[i].let { if (it in listOf("?", "", Cena.Grade.TAMPADA)) -1 else todas.indexOf(it) } }
            val achadas = mutableListOf<IntArray>()
            // Com mais de `lado` figuras nas opções, as que sobram nunca cabem: o solver só usa as do quadro e a resposta.
            val usadas = (figuras + listOfNotNull(emojiDe(p.opcoes[p.indiceCorreto]))).distinct()
            assertEquals("fase $n: a resposta precisa ser uma das figuras do sudoku", lado, usadas.size)
            val q = IntArray(lado * lado) { i -> if (quadro[i] < 0) -1 else usadas.indexOf(todas[quadro[i]]) }
            solucoes(q, lado, if (lado == 4) 2 else 0, 50, achadas)
            val pergunta = g.itens.indexOf("?")
            assertTrue("fase $n: sudoku sem solução", achadas.isNotEmpty())
            val respostas = achadas.map { usadas[it[pergunta]] }.toSet()
            assertEquals("fase $n: mais de uma figura cabe na casinha amarela", 1, respostas.size)
            assertEquals("fase $n: resposta errada", emojiDe(p.opcoes[p.indiceCorreto]), respostas.single())
            // Sem tampinhas, o quadro inteiro tem uma solução só.
            if (Cena.Grade.TAMPADA !in g.itens) assertEquals("fase $n: sudoku com mais de uma solução", 1, achadas.size)
        }
        assertTrue("poucos sudokus: $vistos", vistos > 200)
        assertTrue("nenhum sudoku 6 por 6", seis > 30)
    }

    @Test
    fun `quadros de espelho e matriz tem a resposta pela regra`() {
        var espelhos = 0
        var matrizes = 0
        for ((n, p) in sorteios(30)) {
            val g = p.cena as? Cena.Grade ?: continue
            val i = g.itens.indexOf("?")
            val certa = emojiDe(p.opcoes[p.indiceCorreto]) ?: continue
            when (p.instrucao) {
                "Qual figura completa o espelho?" -> {
                    espelhos++
                    val l = i / g.colunas
                    val c = i % g.colunas
                    assertEquals("fase $n", g.itens[l * g.colunas + g.colunas - 1 - c], certa)
                }
                "O que falta no quadro?" -> {
                    matrizes++
                    // Linha e coluna da interrogação: a resposta repete o padrão das outras casinhas.
                    val l = i / 3
                    val c = i % 3
                    val outraLinha = (l + 1) % 3
                    val outraColuna = (c + 1) % 3
                    val mesmaColuna = g.itens[outraLinha * 3 + c]
                    val mesmaLinha = g.itens[l * 3 + outraColuna]
                    val diagonal = g.itens[outraLinha * 3 + outraColuna]
                    // Na mesma coluna e na mesma linha, a resposta troca exatamente o que a diagonal troca.
                    assertTrue("fase $n: matriz ${g.itens} $certa", certa != mesmaColuna && certa != mesmaLinha && diagonal != certa)
                }
            }
        }
        assertTrue(espelhos > 50 && matrizes > 50)
    }

    // ------------------------------------------------------------------ labirinto

    @Test
    fun `labirintos dos mundos novos tem um caminho so e a resposta certa`() {
        val setas = mapOf(DirecaoDaSeta.CIMA to (-1 to 0), DirecaoDaSeta.BAIXO to (1 to 0), DirecaoDaSeta.ESQUERDA to (0 to -1), DirecaoDaSeta.DIREITA to (0 to 1))
        var vistos = 0
        var grandes = 0
        for ((n, p) in sorteios(30)) {
            val lab = p.cena as? Cena.Labirinto ?: continue
            vistos++
            val caminho = lab.caminho.map { it / lab.colunas to it % lab.colunas }
            assertEquals("fase $n: casinha repetida", caminho.size, caminho.toSet().size)
            for (i in caminho.indices) for (j in i + 1 until caminho.size) {
                val dist = kotlin.math.abs(caminho[i].first - caminho[j].first) + kotlin.math.abs(caminho[i].second - caminho[j].second)
                if (j == i + 1) assertEquals("fase $n: passo comprido", 1, dist) else assertTrue("fase $n: atalho, o caminho não é único", dist > 1)
            }
            if (n in 241..249) {
                grandes++
                assertTrue("fase $n: labirinto pequeno demais (${caminho.size})", caminho.size >= 10)
            }
            val certa = p.opcoes[p.indiceCorreto].visual
            val direcoes = caminho.zipWithNext { a, b -> b.first - a.first to b.second - a.second }
            when (p.instrucao) {
                "Quantas curvas tem o caminho?" -> assertEquals("fase $n", direcoes.zipWithNext().count { (a, b) -> a != b }.toString(), (certa as Visual.Texto).texto)
                "Quantas pedrinhas tem no caminho?" -> assertEquals((caminho.size - 2).toString(), (certa as Visual.Texto).texto)
                "Para que lado você vai primeiro?" -> assertEquals("fase $n", direcoes.first(), setas.getValue((certa as Visual.Seta).direcao))
                else -> assertEquals("fase $n", direcoes.last(), setas.getValue((certa as Visual.Seta).direcao))
            }
        }
        assertTrue("poucos labirintos: $vistos", vistos > 60)
        assertTrue("poucos labirintos grandes: $grandes", grandes > 15)
    }

    // ------------------------------------------------------------------ dedução

    private fun conferirQuemEQuem(onde: String, fala: String, criancas: List<String>, bichos: List<Opcao>, pares: Map<Int, Int>) {
        val nomes = bichos.map { it.fala.first().texto }
        // Pistas: "Ana tem o gato." e "Leo não tem o coelho."
        val pistas = Regex("(\\p{L}+) (não tem|tem) ([^.]+)\\.").findAll(fala).map { it.groupValues }.filter { it[1] in criancas }.toList()
        assertTrue("$onde: sem pistas", pistas.isNotEmpty())
        fun permutacoes(n: Int): List<List<Int>> = if (n == 0) listOf(emptyList()) else permutacoes(n - 1).flatMap { p -> (0..p.size).map { i -> p.take(i) + (n - 1) + p.drop(i) } }
        val validas = permutacoes(criancas.size).filter { perm ->
            pistas.all { (_, quem, tem, bicho) -> (nomes[perm[criancas.indexOf(quem)]] == bicho) == (tem == "tem") }
        }
        assertEquals("$onde: as pistas não deixam um jeito só ($fala)", 1, validas.size)
        assertEquals(onde, criancas.indices.associateWith { validas.single()[it] }, pares)
    }

    @Test
    fun `quem e quem e detetive tem resposta unica`() {
        var vistos = 0
        for ((n, p) in sorteios(30, 121..250)) {
            val fala = p.fala.joinToString(" ") { it.texto }
            if (p.instrucao.startsWith("Qual é o bichinho")) {
                vistos++
                val criancas = Regex("Cada criança tem um bichinho: ([^.]+)\\.").find(fala)!!.groupValues[1].split(", ")
                val pistas = Regex("(\\p{L}+) (não tem|tem) ([^.]+)\\.").findAll(fala).map { it.groupValues }.filter { it[1] in criancas }.toList()
                val bichos = VocabularioLogica.bichinhos.filter { (e, _) -> e in (p.cena as Cena.Emojis).texto.split(" ") }
                val validas = mutableSetOf<String>()
                val perguntada = criancas.first { p.instrucao.endsWith("$it?") }
                fun perms(l: List<Pair<String, String>>): List<List<Pair<String, String>>> = if (l.isEmpty()) listOf(emptyList()) else l.flatMap { x -> perms(l - x).map { listOf(x) + it } }
                for (perm in perms(bichos)) {
                    if (pistas.all { (_, quem, tem, bicho) -> (perm[criancas.indexOf(quem)].second == bicho) == (tem == "tem") }) validas += perm[criancas.indexOf(perguntada)].first
                }
                assertEquals("fase $n: $fala", setOf(emojiDe(p.opcoes[p.indiceCorreto])), validas)
            }
            if (p.instrucao == "Onde está o tesouro?" || p.atividade is EscondeEsconde && "Pista um" in fala) {
                // "Pista um: é um coração. Pista dois: não é azul." Uma figura só cumpre as duas.
                val forma = VocabularioLogica.formas.indexOf(Regex("é um (\\p{L}+)\\.").find(fala)!!.groupValues[1])
                val semCor = VocabularioLogica.cores.indexOf(Regex("não é (\\p{L}+)\\.").find(fala)!!.groupValues[1])
                val ops = (p.atividade as? EscondeEsconde)?.esconderijos ?: p.opcoes
                val certo = (p.atividade as? EscondeEsconde)?.certo ?: p.indiceCorreto
                val servem = ops.indices.filter { i ->
                    val e = emojiDe(ops[i])
                    (0 until 6).any { VocabularioLogica.figura(forma, it) == e } && e != VocabularioLogica.figura(forma, semCor)
                }
                assertEquals("fase $n: $fala", listOf(certo), servem)
            }
        }
        assertTrue(vistos > 50)
    }

    @Test
    fun `vocabulario da logica e consistente`() {
        assertEquals(20, VocabularioLogica.temas.size)
        for (t in VocabularioLogica.temas) assertEquals(t.nome, t.figuras.size, t.emojis.toSet().size)
        for (d in VocabularioLogica.desenhos) {
            assertTrue(d.nome, d.linhas.all { it.length == d.colunas } && d.colunas % 2 == 0)
            assertTrue("${d.nome} não é simétrico", d.linhas.all { it == it.reversed() })
            val codigos = d.linhas.joinToString("").filter { it != '.' }.map { it - '0' }.toSet()
            assertEquals(d.nome, (1..d.paleta.size).toSet(), codigos)
        }
        assertEquals("histórias com figura repetida", VocabularioLogica.historias.flatten().size, VocabularioLogica.historias.flatten().map { it.first }.toSet().size)
        assertEquals("efeitos repetidos", VocabularioLogica.causas.size, VocabularioLogica.causas.map { it.efeito }.toSet().size)
    }

    // ------------------------------------------------------------------ variedade e progressão

    @Test
    fun `cada fase mistura tipos e cada mundo tem pelo menos quatro tipos`() {
        val tiposPorFase = (51..250).associateWith { n -> sorteios(80, n..n).map { tipo(it.second) }.toSet() }
        for ((n, tipos) in tiposPorFase) assertTrue("fase $n (${logica.fase(n).titulo}) só tem $tipos", tipos.size >= 2)
        for (m in 6..25) {
            val fases = (m * 10 - 9)..(m * 10)
            val tipos = fases.flatMap { tiposPorFase.getValue(it) }.toSet()
            assertTrue("mundo $m só tem $tipos", tipos.size >= 4)
            assertTrue("chefão do mundo $m: ${tiposPorFase.getValue(m * 10)}", tiposPorFase.getValue(m * 10).size >= 3)
        }
    }

    @Test
    fun `a dificuldade sobe de mundo em mundo`() {
        fun porMundo(m: Int) = sorteios(40, (m * 10 - 9)..(m * 10)).map { it.second }.toList()
        val mundos = (6..25).associateWith(::porMundo)
        fun maximo(m: Int, valor: (Pergunta) -> Int?) = mundos.getValue(m).mapNotNull(valor).maxOrNull() ?: 0
        val simon = { p: Pergunta -> (p.atividade as? SigaASequencia)?.sequencia?.size }
        val pecas = { p: Pergunta -> (p.atividade as? QuebraCabeca)?.pecas }
        val passos = { p: Pergunta -> (p.atividade as? ProgramarRobo)?.menorCaminho }
        val sudoku = { p: Pergunta -> (p.cena as? Cena.Grade)?.takeIf { p.instrucao == "Qual figura vai na casinha amarela?" }?.itens?.count { it == "" } }
        assertTrue(maximo(7, simon) <= 3 && maximo(17, simon) >= 5 && maximo(22, simon) >= 6 && maximo(25, simon) == 7)
        assertTrue(maximo(6, pecas) == 4 && maximo(9, pecas) == 6 && maximo(18, pecas) == 9 && maximo(20, pecas) == 12 && maximo(24, pecas) == 16)
        assertTrue(maximo(8, passos) <= 3 && maximo(12, passos) in 4..5 && maximo(22, passos) == 8)
        assertTrue(maximo(11, sudoku) in 1..4 && maximo(24, sudoku) >= 5)
        // O bloco "duas vezes" só aparece a partir do Vulcão (mundo 18).
        for (m in 6..17) assertTrue("laço cedo no mundo $m", mundos.getValue(m).none { (it.atividade as? ProgramarRobo)?.repetir == true })
        assertTrue(mundos.getValue(18).any { (it.atividade as? ProgramarRobo)?.repetir == true })
        // Os tipos novos aparecem conforme o plano.
        val tiposDe = { m: Int -> mundos.getValue(m).map(::tipo).toSet() }
        assertTrue(tiposDe(6).containsAll(listOf("QuebraCabeca", "Cesta", "LigarPares")))
        assertTrue(tiposDe(7).containsAll(listOf("SigaASequencia", "Memoria", "Pescaria")))
        assertTrue(tiposDe(8).containsAll(listOf("ProgramarRobo", "EscondeEsconde", "CacaNaCena")))
        assertTrue(tiposDe(10).containsAll(listOf("PintarPorCodigo", "Domino", "CartasViradas")))
        assertTrue(tiposDe(14).containsAll(listOf("Trem", "Balanca", "CartasViradas")))
    }

    @Test
    fun `grande chefao passa por todas as faixas de mundos`() {
        val g = GeradorDeFases(Random(5))
        val fase = g.fases(Trilha.LOGICA)[249]
        assertEquals("Grande chefão da lógica", fase.titulo)
        val tipos = mutableSetOf<String>()
        repeat(30) { g.perguntas(fase).forEach { tipos += tipo(it) } }
        assertTrue("chefão final pouco variado: $tipos", tipos.size >= 5)
    }
}
