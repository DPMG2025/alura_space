package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** As 200 fases (5 mundos por caminho): perguntas válidas, variadas e com a resposta certa de verdade. */
class CinquentaFasesTest {

    @Test
    fun `cada caminho tem cinquenta fases em cinco mundos de dez`() {
        val g = GeradorDeFases(Random(1))
        assertEquals(50, GeradorDeFases.FASES_POR_TRILHA)
        assertEquals(5, Mundos.todos.size)
        assertEquals(GeradorDeFases.FASES_POR_TRILHA, Mundos.todos.size * Mundos.FASES_POR_MUNDO)
        for (t in Trilha.entries) {
            val fases = g.fases(t)
            assertEquals(50, fases.size)
            fases.forEachIndexed { i, f ->
                assertEquals(i, f.numero)
                assertTrue("$t $i sem título", f.titulo.isNotBlank() && f.emoji.isNotBlank())
            }
            assertEquals(Mundos.todos[0], Mundos.de(fases[0]))
            assertEquals(Mundos.todos[2], Mundos.de(fases[25]))
            assertEquals(Mundos.todos[4], Mundos.de(fases[49]))
            assertTrue(Mundos.comecaMundo(fases[40]))
            assertFalse(Mundos.comecaMundo(fases[41]))
        }
    }

    @Test
    fun `cada fase gera cinco perguntas validas com quatro opcoes distintas e a certa presente`() {
        for (semente in 0 until 40) {
            val g = GeradorDeFases(Random(1000 + semente))
            for (t in Trilha.entries) for (fase in g.fases(t)) {
                val perguntas = g.perguntas(fase)
                assertEquals("$fase", GeradorDeFases.PERGUNTAS_POR_FASE, perguntas.size)
                perguntas.forEach { validar(fase, it) }
            }
        }
    }

    @Test
    fun `fases novas nao repetem demais a mesma pergunta`() {
        for (semente in 0 until 30) {
            val g = GeradorDeFases(Random(semente))
            for (t in Trilha.entries) for (fase in g.fases(t).drop(25)) {
                val chaves = g.perguntas(fase).map { p -> p.instrucao + "|" + p.cena + "|" + p.opcoes[p.indiceCorreto].visual + "|" + p.atividade }
                assertTrue("$fase repetiu demais: $chaves", chaves.toSet().size >= 4)
                assertTrue("$fase repetiu seguida", chaves.zipWithNext().none { (a, b) -> a == b })
            }
        }
    }

    @Test
    fun `fases novas sao variadas entre escolha e de mexer`() {
        val g = GeradorDeFases(Random(9))
        for (t in Trilha.entries) {
            val tipos = mutableSetOf<String>()
            repeat(20) { for (f in g.fases(t).drop(25)) g.perguntas(f).forEach { tipos += it.atividade::class.simpleName.orEmpty() } }
            assertTrue("$t só tem $tipos", tipos.size >= 2)
        }
    }

    @Test
    fun `contas escritas tem o resultado certo`() {
        val g = GeradorDeFases(Random(21))
        val fases = g.fases(Trilha.MATEMATICA).drop(25)
        repeat(30) {
            for (f in fases) for (p in g.perguntas(f)) {
                val certa = (p.opcoes[p.indiceCorreto].visual as? Visual.Texto)?.texto?.toIntOrNull() ?: continue
                when (val c = p.cena) {
                    is Cena.Conta -> assertEquals(p.instrucao, if (c.soma) c.a + c.b else c.a - c.b, certa)
                    is Cena.Sequencia -> {
                        val s = c.itens
                        if (s.size >= 5 && s[1] in listOf("+", "−", "÷") && s.last() == "?") {
                            var total = s[0].toInt()
                            var i = 1
                            while (s[i] != "=") {
                                val n = s[i + 1].toInt()
                                total = when (s[i]) { "+" -> total + n; "−" -> total - n; else -> total / n }
                                i += 2
                            }
                            assertEquals("${p.instrucao} $s", total, certa)
                            assertTrue("fora de 0 a 100: $s", total in 0..100)
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    @Test
    fun `dezenas e unidades contam certinho`() {
        val g = GeradorDeFases(Random(4))
        val fase = g.fases(Trilha.MATEMATICA)[31]
        repeat(30) {
            for (p in g.perguntas(fase).filter { it.instrucao == "Quanto é?" }) {
                val cena = (p.cena as Cena.Emojis).texto
                val dezenas = cena.split("🔟").size - 1
                val unidades = cena.split("🟡").size - 1
                assertEquals(dezenas * 10 + unidades, (p.opcoes[p.indiceCorreto].visual as Visual.Texto).texto.toInt())
            }
        }
    }

    @Test
    fun `sudoku tem resposta unica e quadro valido`() {
        val g = GeradorDeFases(Random(8))
        val fases = listOf(31, 36, 42, 48, 49).map { g.fases(Trilha.LOGICA)[it] }
        var vistos = 0
        repeat(40) {
            for (f in fases) for (p in g.perguntas(f)) {
                val grade = p.cena as? Cena.Grade ?: continue
                if (grade.colunas != 4) continue
                vistos++
                val resposta = (p.opcoes[p.indiceCorreto].visual as Visual.Emoji).emoji
                val i = grade.itens.indexOf("?")
                val completo = grade.itens.toMutableList().also { it[i] = resposta }
                val (l, c) = i / 4 to i % 4
                val vizinhas = (0 until 16).filter { j -> j != i && (j / 4 == l || j % 4 == c || (j / 8 == i / 8 && j % 4 / 2 == c / 2)) }
                // A resposta não repete em nenhuma vizinha mostrada, e as mostradas deixam só ela de fora.
                assertTrue("resposta repetida", vizinhas.none { completo[it] == resposta })
                assertEquals("mais de uma resposta possível", 3, vizinhas.map { grade.itens[it] }.filter { it.isNotEmpty() }.toSet().size)
                // Linhas cheias têm uma figura de cada.
                for (linha in completo.chunked(4).filter { r -> r.none { it.isEmpty() } }) assertEquals(4, linha.toSet().size)
            }
        }
        assertTrue("nenhum sudoku sorteado", vistos > 20)
    }

    @Test
    fun `labirinto tem um caminho do ratinho ao queijo e a seta certa`() {
        val g = GeradorDeFases(Random(12))
        val fases = listOf(30, 35, 41).map { g.fases(Trilha.LOGICA)[it] }
        val setas = mapOf("⬆️" to (-1 to 0), "⬇️" to (1 to 0), "⬅️" to (0 to -1), "➡️" to (0 to 1))
        var vistos = 0
        repeat(40) {
            for (f in fases) for (p in g.perguntas(f)) {
                val grade = p.cena as? Cena.Grade ?: continue
                if (grade.colunas != 5) continue
                vistos++
                val livre = { l: Int, c: Int -> l in 0 until grade.linhas && c in 0 until 5 && grade.itens[l * 5 + c] != "🌳" }
                // Segue o caminho: sem atalhos, cada casinha livre tem no máximo 2 vizinhas livres.
                val inicio = grade.itens.indexOf("🐭")
                val fim = grade.itens.indexOf("🧀")
                assertTrue(inicio >= 0 && fim >= 0)
                val caminho = mutableListOf(inicio / 5 to inicio % 5)
                while (caminho.last() != (fim / 5 to fim % 5)) {
                    val (l, c) = caminho.last()
                    val proximas = setas.values.map { (dl, dc) -> (l + dl) to (c + dc) }
                        .filter { (nl, nc) -> livre(nl, nc) && (nl to nc) !in caminho }
                    assertEquals("caminho com atalho ou sem saída", 1, proximas.size)
                    caminho += proximas.first()
                }
                assertEquals("casinhas livres fora do caminho", grade.itens.count { it != "🌳" }, caminho.size)
                val certa = p.opcoes[p.indiceCorreto].visual
                when (p.instrucao) {
                    "Quantos passos até o queijo?" -> assertEquals((caminho.size - 1).toString(), (certa as Visual.Texto).texto)
                    "Para que lado ele vai primeiro?" -> {
                        val (dl, dc) = setas.getValue((certa as Visual.Emoji).emoji)
                        assertEquals(caminho[1], caminho[0].first + dl to caminho[0].second + dc)
                    }
                    else -> {
                        val (dl, dc) = setas.getValue((certa as Visual.Emoji).emoji)
                        val a = caminho[caminho.size - 2]
                        assertEquals(caminho.last(), a.first + dl to a.second + dc)
                    }
                }
            }
        }
        assertTrue("nenhum labirinto sorteado", vistos > 20)
    }

    @Test
    fun `numeros por extenso ate cem`() {
        assertEquals("vinte e um", Vocabulario.porExtenso(21))
        assertEquals("vinte e uma", Vocabulario.porExtenso(21, feminino = true))
        assertEquals("trinta e duas", Vocabulario.porExtenso(32, feminino = true))
        assertEquals("cinquenta", Vocabulario.porExtenso(50))
        assertEquals("noventa e nove", Vocabulario.porExtenso(99))
        assertEquals("cem", Vocabulario.porExtenso(100))
        assertEquals("dezenove", Vocabulario.porExtenso(19))
        assertEquals(21, Vocabulario.numerosEn.size)
        assertEquals("twenty", Vocabulario.numerosEn[20])
        for (n in 0..100) assertFalse(Vocabulario.porExtenso(n).any { it.isDigit() })
    }

    @Test
    fun `tracar usa so letras e algarismos`() {
        val g = GeradorDeFases(Random(3))
        val letras = mutableSetOf<String>()
        repeat(30) {
            for (t in Trilha.entries) for (f in g.fases(t)) for (p in g.perguntas(f)) {
                (p.atividade as? Atividade.Tracar)?.let { letras += it.letra }
            }
        }
        assertTrue(letras.all { it.length == 1 && it[0].isLetterOrDigit() })
        assertTrue("faltam minúsculas: $letras", letras.any { it[0].isLowerCase() })
        assertTrue("faltam números: $letras", letras.any { it[0].isDigit() })
    }

    private fun validar(fase: Fase, p: Pergunta) {
        assertTrue("$fase: sem fala", p.fala.isNotEmpty() && p.fala.all { it.texto.isNotBlank() })
        when (val a = p.atividade) {
            is Atividade.Cesta -> {
                assertTrue("$fase: cesta impossível", a.alvo in 1..a.certos.size)
                assertTrue("$fase: figura certa e errada iguais", a.certos.none { it in a.errados })
                return
            }
            is Atividade.Memoria -> {
                assertTrue("$fase: memória com poucos pares", a.pares.size in 2..6)
                val cartas = a.pares.flatMap { listOf(it.first, it.second) }
                assertEquals("$fase: cartas repetidas", a.pares.size, a.pares.map { it.first }.toSet().size)
                assertTrue("$fase: carta vazia", cartas.all { it.isNotBlank() })
                return
            }
            is Atividade.MontarPalavra -> {
                assertEquals("$fase: peças não formam a palavra", a.palavra, a.silabas.joinToString(""))
                assertTrue("$fase: extra igual a peça", a.extras.none { it in a.silabas })
                return
            }
            is Atividade.Tracar -> {
                assertTrue("$fase: traçado de ${a.letra}", a.letra.length == 1 && a.letra[0].isLetterOrDigit())
                return
            }
            Atividade.Escolha -> Unit
        }
        assertEquals("$fase: deve ter 4 opções (${p.instrucao})", 4, p.opcoes.size)
        val certa = p.opcoes[p.indiceCorreto].visual
        assertEquals("$fase: certa duplicada em ${p.instrucao} ${p.opcoes}", 1, p.opcoes.count { it.visual == certa })
        if (p.instrucao != "Qual é diferente?") {
            assertEquals("$fase: opções repetidas ${p.instrucao} ${p.opcoes}", 4, p.opcoes.map { it.visual }.toSet().size)
        }
        for (o in p.opcoes) when (val v = o.visual) {
            is Visual.Texto -> assertTrue("$fase: texto vazio", v.texto.isNotBlank())
            is Visual.Emoji -> assertTrue("$fase: figura vazia", v.emoji.isNotBlank())
            is Visual.Sombra -> assertTrue(v.emoji.isNotBlank())
            is Visual.Cor -> Unit
        }
        (p.cena as? Cena.Grade)?.let { assertEquals("$fase: quadro torto", 0, it.itens.size % it.colunas) }
    }
}

class TreinoTest {

    private val g = GeradorDeFases(Random(0))

    private fun vencer(e0: EstadoJogo, trilha: Trilha, quantas: Int, historico: (Int) -> String = { "1111111111" }): EstadoJogo {
        var e = e0
        for (f in g.fases(trilha).take(quantas)) {
            e = e.registrarVitoria(f, 3).first
            e = e.copy(historico = e.historico + (EstadoJogo.chave(f.trilha, f.numero) to historico(f.numero)))
        }
        return e
    }

    @Test
    fun `treino so sorteia fases ja vencidas do caminho`() {
        val e = vencer(EstadoJogo(), Trilha.PORTUGUES, 8)
        val treino = Treino(Trilha.PORTUGUES, e, g, Random(1))
        assertEquals(8, treino.fases.size)
        repeat(60) { treino.proxima(List(it) { true }) }
        assertTrue(treino.feitas.all { it.trilha == Trilha.PORTUGUES && e.estrelasDa(it) > 0 })
    }

    @Test
    fun `treino pesa mais as fases com menor precisao`() {
        // Fase 2 com 20% de primeira; as outras com 100%.
        val e = vencer(EstadoJogo(), Trilha.MATEMATICA, 6) { if (it == 2) "1100000000" else "1111111111" }
        val treino = Treino(Trilha.MATEMATICA, e, g, Random(2))
        val pesos = treino.pesos()
        val fraca = g.fases(Trilha.MATEMATICA)[2]
        assertTrue(pesos.getValue(fraca) > 4 * pesos.filterKeys { it != fraca }.values.max())
        // No sorteio, a fase fraca cai bem mais que a média.
        val contagem = mutableMapOf<Fase, Int>()
        repeat(600) { contagem.merge(treino.sortearFase(), 1, Int::plus) }
        assertTrue("fase fraca saiu pouco: $contagem", contagem.getValue(fraca) > 600 / treino.teto * 2)
    }

    @Test
    fun `acertos seguidos abrem fases mais dificeis e erros seguidos voltam`() {
        val e = vencer(EstadoJogo(), Trilha.LOGICA, 20)
        val treino = Treino(Trilha.LOGICA, e, g, Random(3))
        val inicio = treino.teto
        assertEquals(Treino.inicioDoTeto(20), inicio)
        assertTrue(inicio < 20)
        val resultados = mutableListOf<Boolean>()
        treino.proxima(resultados)
        repeat(Treino.ACERTOS_PARA_SUBIR) { resultados += true; treino.proxima(resultados) }
        assertEquals(inicio + Treino.PASSO_DO_TETO, treino.teto)
        assertEquals(2, treino.nivel)
        // A pergunta que vem logo depois de subir avisa a criança.
        assertTrue(treino.proxima(resultados).fala.isNotEmpty())
        repeat(30) { resultados += true; treino.proxima(resultados) }
        assertEquals(20, treino.teto)
        repeat(Treino.ERROS_PARA_DESCER) { resultados += false; treino.proxima(resultados) }
        assertEquals(20 - Treino.PASSO_DO_TETO, treino.teto)
        // Nunca abaixo do mínimo.
        repeat(40) { resultados += false; treino.proxima(resultados) }
        assertEquals(Treino.MINIMO_DO_TETO, treino.teto)
        // Uma pergunta por resultado, mais a do aviso de subir e a que está na tela.
        assertEquals(resultados.size + 2, treino.feitas.size)
    }

    @Test
    fun `fases de cima pesam mais com acertos em sequencia`() {
        val e = vencer(EstadoJogo(), Trilha.INGLES, 10)
        val treino = Treino(Trilha.INGLES, e, g, Random(4))
        val antes = treino.pesos().let { p -> p.values.last() / p.values.first() }
        treino.registrar(true); treino.registrar(true)
        val depois = treino.pesos().let { p -> p.values.last() / p.values.first() }
        assertTrue("antes $antes depois $depois", depois > antes)
    }

    @Test
    fun `treino da moedinhas dentro do limite do dia e guarda o historico`() {
        val e = vencer(EstadoJogo(), Trilha.MATEMATICA, 3).copy(moedasHoje = EstadoJogo.MOEDAS_POR_DIA - 2, diaDasMoedas = 7, moedas = 40)
        val f = g.fases(Trilha.MATEMATICA)
        val respostas = listOf(f[0] to true, f[1] to true, f[1] to false, f[2] to true)
        val r = e.registrarTreino(respostas, dia = 7)
        assertEquals(2, r.moedas)
        assertEquals(42, r.estado.moedas)
        assertEquals(EstadoJogo.MOEDAS_POR_DIA, r.estado.moedasHoje)
        assertEquals("1111111110", r.estado.historico[EstadoJogo.chave(Trilha.MATEMATICA, 1)])
        assertEquals(3, r.acertos)
        // Num dia novo, o limite recomeça.
        val outroDia = r.estado.registrarTreino(respostas, dia = 8)
        assertEquals(3, outroDia.moedas)
        // Treino não dá estrelas nem peças da casa.
        assertEquals(e.estrelas, outroDia.estado.estrelas)
        assertEquals(0, e.registrarTreino(emptyList(), 7).moedas)
    }

    @Test
    fun `treino sem fase vencida usa a primeira`() {
        val treino = Treino(Trilha.LOGICA, EstadoJogo(), g, Random(5))
        assertEquals(listOf(g.fases(Trilha.LOGICA)[0]), treino.fases)
        assertNotNull(treino.proxima())
    }
}

/** O progresso salvo com 25 fases por caminho continua valendo: a fase N de antes é a fase N de agora. */
class CompatibilidadeDoProgressoTest {

    private val g = GeradorDeFases(Random(0))

    @Test
    fun `fases antigas continuam com o mesmo numero e nome`() {
        val antigas = mapOf(
            Trilha.MATEMATICA to listOf(0 to "Contar até 3", 9 to "Formas", 17 to "Números até 20", 24 to "Supercampeão dos números"),
            Trilha.PORTUGUES to listOf(0 to "Vogais A, E, I, O", 11 to "Pedacinhos", 15 to "Rimas", 24 to "Supercampeão das letras"),
            Trilha.INGLES to listOf(0 to "Colors", 8 to "Numbers 6 a 10", 22 to "All the numbers", 24 to "English super star"),
            Trilha.LOGICA to listOf(0 to "Igualzinho", 5 to "Sombras", 13 to "Números em fila", 24 to "Supercampeão da lógica"),
        )
        for ((t, lista) in antigas) for ((n, titulo) in lista) assertEquals("$t $n", titulo, g.fases(t)[n].titulo)
    }

    @Test
    fun `save antigo carrega e libera a fase vinte e seis`() {
        // Texto salvo por uma versão antiga: todas as 25 fases de números vencidas.
        val texto = (0 until 25).joinToString(";") { "MATEMATICA:$it=3" } + ";INGLES:3=2"
        val estrelas = Serializacao.textoParaEstrelas(texto)
        assertEquals(26, estrelas.size)
        val e = EstadoJogo(estrelas = estrelas)
        val fases = g.fases(Trilha.MATEMATICA)
        assertEquals(25, e.vencidasNa(Trilha.MATEMATICA))
        assertTrue(e.liberada(fases[25]))
        assertFalse(e.liberada(fases[26]))
        assertEquals(3, e.estrelasDa(fases[24]))
        // Vencer a fase 26 conta como fase nova (peça ou presente).
        val r = e.registrarPartida(fases[25], 5, 5)
        assertTrue(r.faseNova)
        assertTrue(r.estado.liberada(fases[26]))
        // Ida e volta do texto com as fases novas.
        assertEquals(r.estado.estrelas, Serializacao.textoParaEstrelas(Serializacao.estrelasParaTexto(r.estado.estrelas)))
    }

    @Test
    fun `revisao das fases novas pode puxar perguntas dos mundos de antes`() {
        val fase = g.fases(Trilha.PORTUGUES)[45]
        repeat(10) { assertEquals(GeradorDeFases.PERGUNTAS_POR_FASE, g.perguntas(fase).size) }
        assertEquals(3, g.perguntasDaMissao(listOf(fase, g.fases(Trilha.LOGICA)[30])).size)
    }
}
