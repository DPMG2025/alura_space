package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Quebra-cabeças (Fase 11): níveis, ímã, vitória, deslizante com solução, progressão adaptativa e prêmios. */
class PuzzleTest {

    private fun montagem(nivel: Int = 3, semente: Long = 7) = Puzzle.montar("modelo:gato", nivel, semente)

    private fun resolver(m0: Puzzle.Montagem): Puzzle.Montagem {
        var m = m0
        for (p in m0.pecas) m = Puzzle.soltar(m, p.id, Puzzle.alvoX(m, p.id), Puzzle.alvoY(m, p.id))
        return m
    }

    @Test
    fun `oito niveis de 4 a 36 pecas, sempre mais pecas que o anterior`() {
        assertEquals(8, Puzzle.niveis.size)
        assertEquals(4, Puzzle.niveis.first().pecas)
        assertEquals(36, Puzzle.niveis.last().pecas)
        assertTrue(Puzzle.niveis.zipWithNext().all { (a, b) -> b.pecas > a.pecas && b.numero == a.numero + 1 })
        assertEquals(1, Puzzle.nivel(-5).numero)
        assertEquals(8, Puzzle.nivel(99).numero)
    }

    @Test
    fun `montar cria todas as pecas soltas na bandeja e a mesma semente da a mesma bagunca`() {
        for (n in Puzzle.niveis) {
            val m = Puzzle.montar("modelo:gato", n.numero, 1)
            assertEquals(n.pecas, m.pecas.size)
            assertEquals((0 until n.pecas).toSet(), m.pecas.map { it.id }.toSet())
            assertTrue(m.pecas.none { it.colocada })
            assertFalse(m.venceu)
            assertTrue("peça fora da bandeja", m.pecas.all { it.y >= Puzzle.BANDEJA_DE - 0.0001f && it.y + m.altura <= Puzzle.BANDEJA_ATE + 0.0001f && it.x >= 0f && it.x + m.largura <= 1.0001f })
        }
        assertEquals(montagem(4, 5), montagem(4, 5))
        assertNotEquals(montagem(4, 5).pecas, montagem(4, 6).pecas)
    }

    @Test
    fun `nenhuma peca da bandeja fica em cima de outra`() {
        for (n in Puzzle.niveis) {
            val m = Puzzle.montar("modelo:gato", n.numero, 3)
            val caixas = m.pecas.map { it.x to it.y }
            assertEquals("nível ${n.numero}: casas repetidas", caixas.size, caixas.toSet().size)
            for (a in m.pecas) for (b in m.pecas) if (a.id < b.id) {
                val separados = a.x + m.largura <= b.x + 0.0001f || b.x + m.largura <= a.x + 0.0001f ||
                    a.y + m.altura <= b.y + 0.0001f || b.y + m.altura <= a.y + 0.0001f
                assertTrue("nível ${n.numero}: peças ${a.id} e ${b.id} se sobrepõem", separados)
            }
        }
    }

    @Test
    fun `peca solta perto da casa certa encaixa exata e fica parada`() {
        val m = montagem(3)
        val id = 4
        val perto = Puzzle.soltar(m, id, Puzzle.alvoX(m, id) + m.largura * 0.2f, Puzzle.alvoY(m, id) - m.altura * 0.2f)
        val p = perto.pecas.first { it.id == id }
        assertTrue(p.colocada)
        assertEquals(Puzzle.alvoX(m, id), p.x, 0.0001f)
        assertEquals(Puzzle.alvoY(m, id), p.y, 0.0001f)
        // Colocada não mexe mais.
        assertEquals(perto, Puzzle.soltar(perto, id, 0.9f, 0.9f))
    }

    @Test
    fun `peca solta longe da casa nao encaixa e fica onde foi solta, sem sair da area`() {
        val m = montagem(3)
        val longe = Puzzle.soltar(m, 0, 0.5f, 1.2f)
        val p = longe.pecas.first { it.id == 0 }
        assertFalse(p.colocada)
        assertEquals(0.5f, p.x, 0.0001f)
        assertEquals(longe.pecas.last().id, 0) // fica por cima
        val fora = Puzzle.soltar(m, 1, 5f, 9f).pecas.first { it.id == 1 }
        assertTrue(fora.x + m.largura <= 1.0001f && fora.y + m.altura <= Puzzle.BANDEJA_ATE + 0.0001f)
        // Soltar na casa errada de outra peça não encaixa.
        val errada = Puzzle.soltar(m, 0, Puzzle.alvoX(m, 5), Puzzle.alvoY(m, 5))
        assertFalse(errada.pecas.first { it.id == 0 }.colocada)
    }

    @Test
    fun `ima largo ajuda quem tem dificuldade`() {
        val m = montagem(5)
        val x = Puzzle.alvoX(m, 6) + m.largura * 0.5f
        val y = Puzzle.alvoY(m, 6)
        assertFalse(Puzzle.soltar(m, 6, x, y).pecas.first { it.id == 6 }.colocada)
        assertTrue(Puzzle.soltar(m, 6, x, y, imaLargo = true).pecas.first { it.id == 6 }.colocada)
    }

    @Test
    fun `vence so quando todas as pecas estao no lugar e dica conta`() {
        var m = montagem(2)
        val (id, comDica) = Puzzle.dica(m)
        assertEquals(m.pecas.minOf { it.id }, id)
        assertEquals(1, comDica.dicas)
        m = resolver(comDica)
        assertTrue(m.venceu)
        assertEquals(0, m.faltam)
        assertEquals(-1 to m, Puzzle.dica(m))
        // Falta uma: ainda não venceu.
        val quase = resolver(montagem(2)).let { it.copy(pecas = it.pecas.mapIndexed { i, p -> if (i == 0) p.copy(colocada = false) else p }) }
        assertFalse(quase.venceu)
    }

    @Test
    fun `estrelas do montar - uma com dica, duas sem dica, tres sem dica e sem desperdicio`() {
        val sem = resolver(montagem(3))
        assertEquals(3, Puzzle.estrelas(sem))
        assertEquals(1, Puzzle.estrelas(sem.copy(dicas = 1)))
        assertEquals(2, Puzzle.estrelas(sem.copy(arrastos = sem.pecas.size * 2 + 1)))
        assertEquals(0, Puzzle.estrelas(montagem(3)))
    }

    @Test
    fun `bordas sao as pecas do contorno da moldura`() {
        val m = montagem(5) // 4 x 4
        assertEquals(12, m.pecas.count { Puzzle.ehBorda(m, it.id) })
        assertFalse(Puzzle.ehBorda(m, 5))
        assertTrue(Puzzle.ehBorda(m, 0))
    }

    @Test
    fun `deslizante sempre embaralhado, com solucao e so troca com o vizinho do vazio`() {
        for (lado in 2..5) for (semente in 1L..20L) {
            val d = Puzzle.deslizar(lado, semente)
            assertEquals(lado * lado, d.casas.size)
            assertEquals((0 until lado * lado).toSet(), d.casas.toSet())
            assertFalse("já veio resolvido: lado $lado semente $semente", d.resolvido)
        }
        val d = Puzzle.deslizar(3, 4)
        val longe = d.casas.indices.first { it != d.vazio && it !in listOf(d.vazio - 1, d.vazio + 1, d.vazio - 3, d.vazio + 3) }
        assertEquals(d, Puzzle.tocar(d, longe))
        assertEquals(d, Puzzle.tocar(d, d.vazio))
        assertEquals(d, Puzzle.tocar(d, 99))
    }

    @Test
    fun `seguindo a dica o deslizante pequeno chega ao fim`() {
        // A dica é gulosa, não ótima; num 2x2 ela resolve sempre dentro de um número generoso de jogadas.
        for (semente in 1L..15L) {
            var d = Puzzle.deslizar(2, semente)
            var jogadas = 0
            while (!d.resolvido && jogadas < 60) {
                val c = Puzzle.dicaDeslizante(d)
                assertTrue(c >= 0)
                d = Puzzle.tocar(d, c)
                jogadas++
            }
            assertTrue("2x2 semente $semente não resolveu em 60 jogadas", d.resolvido)
        }
        assertEquals(-1, Puzzle.dicaDeslizante(Puzzle.Deslizante(3, Puzzle.meta(3))))
    }

    @Test
    fun `tocar conta jogada so quando troca e estrelas do deslizante`() {
        val d = Puzzle.deslizar(3, 9)
        val vizinho = Puzzle.dicaDeslizante(d)
        val d2 = Puzzle.tocar(d, vizinho)
        assertEquals(1, d2.toques)
        val feito = Puzzle.Deslizante(3, Puzzle.meta(3), toques = 20)
        assertEquals(3, Puzzle.estrelasDoDeslizante(feito, 0))
        assertEquals(1, Puzzle.estrelasDoDeslizante(feito, 2))
        assertEquals(2, Puzzle.estrelasDoDeslizante(feito.copy(toques = 200), 0))
        assertEquals(0, Puzzle.estrelasDoDeslizante(d, 0))
    }

    @Test
    fun `nivel aberto sobe com as vitorias da colecao e o um esta sempre aberto`() {
        assertEquals(1, Puzzle.nivelAberto("bichos", emptyMap()))
        val f = mapOf("gato:1" to 2, "coelho:2" to 1, "peixe:5" to 3) // peixe é do mar
        assertEquals(3, Puzzle.nivelAberto("bichos", f))
        assertEquals(6, Puzzle.nivelAberto("mar", f))
        assertEquals(8, Puzzle.nivelAberto("bichos", mapOf("gato:8" to 1)))
    }

    @Test
    fun `nivel adaptativo sobe com tres seguidas sem dica, desce se travou e respeita o aberto`() {
        assertEquals(4, Puzzle.sugerir(3, 3, false, aberto = 8))
        assertEquals(3, Puzzle.sugerir(3, 2, false, aberto = 8))
        assertEquals(2, Puzzle.sugerir(3, 0, true, aberto = 8))
        assertEquals(3, Puzzle.sugerir(3, 5, false, aberto = 3)) // não passa do aberto
        assertEquals(1, Puzzle.sugerir(1, 0, true, aberto = 1))
        assertEquals(8, Puzzle.sugerir(8, 9, false, aberto = 8))
    }

    @Test
    fun `premio da primeira vez, melhoria e repeticao`() {
        val p = Puzzle.premio("gato", 2, 2, emptyMap())!!
        assertTrue(p.primeiraVez)
        assertEquals(3 + 2 * 2 + 2 * 2 + 5, p.moedas) // base + marco "Primeiro quebra-cabeça"
        assertEquals(1, p.pecaDourada)
        assertNotNull(p.marco)
        val feitos = mapOf("gato:2" to 2)
        val repetido = Puzzle.premio("gato", 2, 2, feitos)!!
        assertFalse(repetido.primeiraVez)
        assertEquals(0, repetido.moedas)
        assertEquals(0, repetido.pecaDourada)
        val melhor = Puzzle.premio("gato", 2, 3, feitos)!!
        assertEquals(2, melhor.moedas)
        assertEquals(1, melhor.pecaDourada) // chegou às 3 estrelas
        assertNull(Puzzle.premio("naoexiste", 1, 1, emptyMap()))
        assertNull(Puzzle.premio("gato", 1, 0, emptyMap()))
    }

    @Test
    fun `marcos pagam no total de quebra-cabecas diferentes e a colecao inteira da bonus`() {
        val nove = (1..9).associate { "gato:${(it - 1) % 8 + 1}".let { k -> if (k == "gato:1" && it > 8) "coelho:1" else k } to 1 }
        assertEquals(9, nove.size)
        val dez = Puzzle.premio("passaro", 1, 1, nove)!!
        assertEquals("Montador", dez.marco?.nome)
        // Colecao inteira: todos os níveis de bichos, falta o oitavo.
        val ate7 = (1..7).associate { "gato:$it" to 1 }
        val fim = Puzzle.premio("gato", 8, 1, ate7)!!
        assertTrue(fim.colecaoCompleta)
        assertTrue(fim.moedas >= 25)
        assertFalse(Puzzle.premio("gato", 8, 1, ate7 + ("coelho:8" to 1))!!.colecaoCompleta)
    }

    @Test
    fun `pecas douradas saem dos feitos`() {
        assertEquals(0, Puzzle.pecasDouradas(emptyMap()))
        assertEquals(3, Puzzle.pecasDouradas(mapOf("gato:1" to 3, "gato:2" to 1)))
    }

    @Test
    fun `guardar e ler feitos tolera lixo`() {
        val m = mapOf("gato:3" to 2, "peixe:8" to 3)
        assertEquals(m, Puzzle.textoParaFeitos(Puzzle.feitosParaTexto(m)))
        assertTrue(Puzzle.textoParaFeitos(null).isEmpty())
        assertTrue(Puzzle.textoParaFeitos("gato:9:1,gato:0:1,gato:1:7,x:1:1,gato:1,gato:a:1,,").isEmpty())
    }
}
