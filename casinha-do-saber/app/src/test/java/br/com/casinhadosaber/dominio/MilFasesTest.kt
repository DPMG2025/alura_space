package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.fases.FasesDoCaminho
import br.com.casinhadosaber.dominio.fases.FasesIngles
import br.com.casinhadosaber.dominio.fases.FasesLetras
import br.com.casinhadosaber.dominio.fases.FasesLogica
import br.com.casinhadosaber.dominio.fases.FasesNumeros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.random.Random

/** As 1000 fases (250 por caminho, 25 mundos de 10) e a base para os tipos novos de desafio. */
class MilFasesTest {

    private val tiposConhecidos = setOf("Cesta", "Memoria", "MontarPalavra", "Tracar") +
        setOf("Pescaria", "EstourarBaloes", "LigarPares", "QuebraCabeca", "Trem", "Balanca", "CacaNaCena", "SigaASequencia", "PintarPorCodigo", "Ditado", "Domino", "EscondeEsconde", "CartasViradas", "Mercadinho", "Relogio", "ProgramarRobo", "MontarFrase")

    private fun caminhos(r: Random): List<FasesDoCaminho> = listOf(FasesNumeros(r), FasesLetras(r), FasesIngles(r), FasesLogica(r))

    @Test
    fun `cada caminho tem um gerador proprio com as fases de 1 a 250`() {
        val r = Random(1)
        val porTrilha = caminhos(r).associateBy { it.trilha }
        assertEquals(Trilha.entries.toSet(), porTrilha.keys)
        val g = GeradorDeFases(Random(1))
        for ((t, c) in porTrilha) {
            val fases = g.fases(t)
            assertEquals(250, fases.size)
            for (n in 1..250) {
                val m = c.fase(n)
                assertTrue("$t $n sem nome", m.titulo.isNotBlank() && m.emoji.isNotBlank())
                assertEquals(m.titulo, fases[n - 1].titulo)
                assertNotNull(m.criar())
            }
            for (fora in listOf(0, 251)) {
                try {
                    c.fase(fora)
                    fail("$t aceitou a fase $fora")
                } catch (_: IllegalArgumentException) {
                }
            }
        }
    }

    @Test
    fun `toda fase de 1 a 250 gera cinco perguntas validas sem tres atividades de mexer iguais seguidas`() {
        for (semente in 0 until 6) {
            val g = GeradorDeFases(Random(7000 + semente))
            for (t in Trilha.entries) for (fase in g.fases(t)) {
                val perguntas = g.perguntas(fase)
                assertEquals("$fase", GeradorDeFases.PERGUNTAS_POR_FASE, perguntas.size)
                for (p in perguntas) {
                    assertTrue("$fase: sem fala", p.fala.isNotEmpty() && p.fala.all { it.texto.isNotBlank() })
                    assertTrue("$fase: índice certo", p.indiceCorreto in p.opcoes.indices)
                    if (p.atividade == Atividade.Escolha) {
                        assertEquals("$fase: 4 opções em ${p.instrucao}", 4, p.opcoes.size)
                    } else {
                        assertTrue("$fase: tipo novo sorteado cedo: ${p.atividade}", p.atividade::class.simpleName in tiposConhecidos)
                    }
                }
                val tipos = perguntas.map { GeradorDeFases.tipoDeMexer(it) }
                for (i in 2 until tipos.size) {
                    assertFalse("$fase: 3 iguais seguidas $tipos", tipos[i] != null && tipos[i] == tipos[i - 1] && tipos[i] == tipos[i - 2])
                }
            }
        }
    }

    @Test
    fun `missao do dia nunca tem tres atividades de mexer iguais`() {
        val g = GeradorDeFases(Random(3))
        val memoria = g.fases(Trilha.LOGICA)[2] // "Memória fácil": sempre jogo da memória
        repeat(20) {
            val tipos = g.perguntasDaMissao(listOf(memoria)).map { GeradorDeFases.tipoDeMexer(it) }
            assertFalse("$tipos", tipos.all { it == "Memoria" })
        }
    }

    @Test
    fun `vinte e cinco mundos de dez com chefao na decima fase`() {
        assertEquals(25, Mundos.todos.size)
        assertEquals((0 until 25).toList(), Mundos.todos.map { it.indice })
        assertEquals(25, Mundos.todos.map { it.nome }.toSet().size)
        assertEquals(listOf("Jardim", "Floresta", "Praia", "Montanha", "Espaço"), Mundos.todos.take(5).map { it.nome })
        val fases = GeradorDeFases(Random(0)).fases(Trilha.MATEMATICA)
        assertEquals(Mundos.todos[5], Mundos.de(fases[50]))
        assertEquals(Mundos.todos[24], Mundos.de(fases[249]))
        assertEquals(249, Mundos.todos.last().ultimaFase)
        assertTrue(Mundos.ehChefao(fases[9]) && Mundos.ehChefao(fases[249]))
        assertFalse(Mundos.ehChefao(fases[10]))
        assertTrue(Mundos.todos.none { it.nome.contains('-') })
    }

    @Test
    fun `progresso salvo com cinquenta fases continua valendo e libera a fase 51`() {
        val g = GeradorDeFases(Random(0))
        val texto = Trilha.entries.joinToString(";") { t -> (0 until 50).joinToString(";") { "${t.name}:$it=2" } }
        val e = EstadoJogo(estrelas = Serializacao.textoParaEstrelas(texto))
        for (t in Trilha.entries) {
            val fases = g.fases(t)
            assertEquals(50, e.vencidasNa(t))
            assertEquals(2, e.estrelasDa(fases[49]))
            assertTrue(e.liberada(fases[50]))
            assertFalse(e.liberada(fases[51]))
        }
        // As 50 fases de antes mantêm o nome.
        assertEquals("Mestre das letras", g.fases(Trilha.PORTUGUES)[49].titulo)
        assertEquals("Mestre da lógica", g.fases(Trilha.LOGICA)[49].titulo)
        val r = e.registrarPartida(g.fases(Trilha.INGLES)[50], 5, 5)
        assertTrue(r.faseNova)
        assertTrue(r.estado.liberada(g.fases(Trilha.INGLES)[51]))
        assertEquals(r.estado.estrelas, Serializacao.textoParaEstrelas(Serializacao.estrelasParaTexto(r.estado.estrelas)))
    }

    @Test
    fun `treino funciona com fases depois da 50`() {
        val g = GeradorDeFases(Random(0))
        var e = EstadoJogo()
        for (f in g.fases(Trilha.LOGICA).take(70)) e = e.registrarVitoria(f, 3).first
        val treino = Treino(Trilha.LOGICA, e, g, Random(1))
        assertEquals(70, treino.fases.size)
        repeat(40) { treino.proxima(List(it) { true }) }
        assertTrue(treino.feitas.any { it.numero >= 50 })
    }
}

/** Os tipos novos de desafio: as regras de cada data class (as telas ficam para depois). */
class AtividadesNovasTest {

    private fun op(s: String) = Opcao(Visual.Texto(s), listOf(pt(s)))

    private inline fun invalido(bloco: () -> Unit) {
        try {
            bloco()
            fail("deveria recusar")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun `todos os tipos novos sao atividades de mexer`() {
        val novas: List<Atividade> = listOf(
            Pescaria(listOf(op("1"), op("2"), op("3")), setOf(1)),
            EstourarBaloes(listOf(op("A"), op("B"), op("C")), listOf(0, 1, 2), emOrdem = true),
            LigarPares(listOf(op("1"), op("2")), listOf(op("um"), op("dois"), op("três")), mapOf(0 to 0, 1 to 1)),
            QuebraCabeca("🐶", 2, 2),
            Trem(listOf(op("1"), op("2"), op("3")), fixos = setOf(0)),
            Balanca(listOf(5), listOf(2, 3, 4)),
            CacaNaCena(5, List(5) { ItemNaCena(op("$it"), it / 5f, 0.5f) }, setOf(2)),
            SigaASequencia(listOf(op("A"), op("B")), listOf(0xFFFF0000, 0xFF0000FF), listOf(0, 1, 1)),
            PintarPorCodigo(3, List(9) { CasaDePintar("${it % 2 + 1}", "${it % 2 + 1}") }, listOf(CorDaLegenda("1", 0xFFFF0000), CorDaLegenda("2", 0xFF00FF00))),
            Ditado(listOf("G", "A", "T", "O"), listOf("B")),
            Domino(MeiaPeca("3", Visual.Texto("3")), listOf(PecaDomino(MeiaPeca("3", Visual.Emoji("🍎🍎🍎")), MeiaPeca("1", Visual.Texto("1"))), PecaDomino(MeiaPeca("2", Visual.Texto("2")), MeiaPeca("4", Visual.Texto("4")))), 1),
            EscondeEsconde("🐰", listOf(op("1"), op("2"), op("3")), 2, trocas = 2),
            CartasViradas(listOf(op("1"), op("2"), op("3")), listOf(0, 1, 2)),
            Mercadinho("🍎", "maçã", 300, listOf(100, 200, 50)),
            Relogio(3, 30),
            ProgramarRobo(3, 3, 0, 8),
            MontarFrase(listOf("O", "gato", "pula"), listOf("bola")),
        )
        assertEquals(17, novas.map { it::class }.toSet().size)
        assertTrue(novas.none { it == Atividade.Escolha })
    }

    @Test
    fun `domino acha a ordem das pecas e recusa jogada ambigua`() {
        val m = { k: String -> MeiaPeca(k, Visual.Texto(k)) }
        val d = Domino(m("1"), listOf(PecaDomino(m("2"), m("3")), PecaDomino(m("1"), m("2")), PecaDomino(m("5"), m("6"))), 2)
        assertEquals(listOf(1, 0), d.ordem)
        invalido { Domino(m("1"), listOf(PecaDomino(m("1"), m("2")), PecaDomino(m("1"), m("3"))), 1) }
    }

    @Test
    fun `balanca e mercadinho precisam de uma combinacao exata`() {
        assertEquals(7, Balanca(listOf(3, 4), listOf(5, 2, 6)).alvo)
        invalido { Balanca(listOf(7), listOf(5, 3)) }
        assertEquals(150, Mercadinho("🍞", "pão", 350, listOf(100, 50, 200), troco = true, pago = 500).alvo)
        invalido { Mercadinho("🍞", "pão", 300, listOf(200, 200)) }
        invalido { Mercadinho("🍞", "pão", 300, listOf(300, 200)) }
    }

    @Test
    fun `robo precisa de caminho e cabe na fila de comandos`() {
        assertEquals(4, ProgramarRobo(3, 3, 0, 8).menorCaminho)
        assertEquals(6, ProgramarRobo(3, 3, 0, 6, obstaculos = setOf(3, 4)).menorCaminho)
        invalido { ProgramarRobo(3, 3, 0, 8, obstaculos = setOf(1, 3)) }
        invalido { ProgramarRobo(4, 4, 0, 15, maxComandos = 3) }
    }

    @Test
    fun `regras basicas recusam montagens impossiveis`() {
        invalido { Pescaria(listOf(Opcao(Visual.Texto("1")), Opcao(Visual.Texto("2")), Opcao(Visual.Texto("3"))), setOf(0, 1, 2)) }
        invalido { LigarPares(listOf(op("1"), op("2")), listOf(op("a"), op("b")), mapOf(0 to 0, 1 to 0)) }
        invalido { PintarPorCodigo(3, List(9) { CasaDePintar("9", "9") }, listOf(CorDaLegenda("1", 0), CorDaLegenda("2", 1))) }
        invalido { Ditado(listOf("G", "A"), listOf("A")) }
        invalido { Relogio(13, 0) }
        invalido { MontarFrase(listOf("Oi")) }
    }
}
