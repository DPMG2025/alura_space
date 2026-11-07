package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Quebra-cabeças (Fase 11): níveis, ímã, vitória, deslizante com solução, progressão adaptativa e prêmios. */
class QuebraCabecaTest {

    private fun montagem(nivel: Int = 3, semente: Long = 7) = QuebraCabeca.montar("modelo:gato", nivel, semente)

    private fun resolver(m0: QuebraCabeca.Montagem): QuebraCabeca.Montagem {
        var m = m0
        for (p in m0.pecas) m = QuebraCabeca.soltar(m, p.id, QuebraCabeca.alvoX(m, p.id), QuebraCabeca.alvoY(m, p.id))
        return m
    }

    @Test
    fun `oito niveis de 4 a 36 pecas, sempre mais pecas que o anterior`() {
        assertEquals(8, QuebraCabeca.niveis.size)
        assertEquals(4, QuebraCabeca.niveis.first().pecas)
        assertEquals(36, QuebraCabeca.niveis.last().pecas)
        assertTrue(QuebraCabeca.niveis.zipWithNext().all { (a, b) -> b.pecas > a.pecas && b.numero == a.numero + 1 })
        assertEquals(1, QuebraCabeca.nivel(-5).numero)
        assertEquals(8, QuebraCabeca.nivel(99).numero)
    }

    @Test
    fun `montar cria todas as pecas soltas na bandeja e a mesma semente da a mesma bagunca`() {
        for (n in QuebraCabeca.niveis) {
            val m = QuebraCabeca.montar("modelo:gato", n.numero, 1)
            assertEquals(n.pecas, m.pecas.size)
            assertEquals((0 until n.pecas).toSet(), m.pecas.map { it.id }.toSet())
            assertTrue(m.pecas.none { it.colocada })
            assertFalse(m.venceu)
            assertTrue("peça fora da bandeja", m.pecas.all { it.y >= QuebraCabeca.BANDEJA_DE - 0.0001f && it.y + m.altura <= QuebraCabeca.BANDEJA_ATE + 0.0001f && it.x >= 0f && it.x + m.largura <= 1.0001f })
        }
        assertEquals(montagem(4, 5), montagem(4, 5))
        assertNotEquals(montagem(4, 5).pecas, montagem(4, 6).pecas)
    }

    @Test
    fun `peca solta perto da casa certa encaixa exata e fica parada`() {
        val m = montagem(3)
        val id = 4
        val perto = QuebraCabeca.soltar(m, id, QuebraCabeca.alvoX(m, id) + m.largura * 0.2f, QuebraCabeca.alvoY(m, id) - m.altura * 0.2f)
        val p = perto.pecas.first { it.id == id }
        assertTrue(p.colocada)
        assertEquals(QuebraCabeca.alvoX(m, id), p.x, 0.0001f)
        assertEquals(QuebraCabeca.alvoY(m, id), p.y, 0.0001f)
        // Colocada não mexe mais.
        assertEquals(perto, QuebraCabeca.soltar(perto, id, 0.9f, 0.9f))
    }

    @Test
    fun `peca solta longe da casa nao encaixa e fica onde foi solta, sem sair da area`() {
        val m = montagem(3)
        val longe = QuebraCabeca.soltar(m, 0, 0.5f, 1.2f)
        val p = longe.pecas.first { it.id == 0 }
        assertFalse(p.colocada)
        assertEquals(0.5f, p.x, 0.0001f)
        assertEquals(longe.pecas.last().id, 0) // fica por cima
        val fora = QuebraCabeca.soltar(m, 1, 5f, 9f).pecas.first { it.id == 1 }
        assertTrue(fora.x + m.largura <= 1.0001f && fora.y + m.altura <= QuebraCabeca.BANDEJA_ATE + 0.0001f)
        // Soltar na casa errada de outra peça não encaixa.
        val errada = QuebraCabeca.soltar(m, 0, QuebraCabeca.alvoX(m, 5), QuebraCabeca.alvoY(m, 5))
        assertFalse(errada.pecas.first { it.id == 0 }.colocada)
    }

    @Test
    fun `ima largo ajuda quem tem dificuldade`() {
        val m = montagem(5)
        val x = QuebraCabeca.alvoX(m, 6) + m.largura * 0.5f
        val y = QuebraCabeca.alvoY(m, 6)
        assertFalse(QuebraCabeca.soltar(m, 6, x, y).pecas.first { it.id == 6 }.colocada)
        assertTrue(QuebraCabeca.soltar(m, 6, x, y, imaLargo = true).pecas.first { it.id == 6 }.colocada)
    }

    @Test
    fun `vence so quando todas as pecas estao no lugar e dica conta`() {
        var m = montagem(2)
        val (id, comDica) = QuebraCabeca.dica(m)
        assertEquals(m.pecas.minOf { it.id }, id)
        assertEquals(1, comDica.dicas)
        m = resolver(comDica)
        assertTrue(m.venceu)
        assertEquals(0, m.faltam)
        assertEquals(-1 to m, QuebraCabeca.dica(m))
        // Falta uma: ainda não venceu.
        val quase = resolver(montagem(2)).let { it.copy(pecas = it.pecas.mapIndexed { i, p -> if (i == 0) p.copy(colocada = false) else p }) }
        assertFalse(quase.venceu)
    }

    @Test
    fun `estrelas do montar - uma com dica, duas sem dica, tres sem dica e sem desperdicio`() {
        val sem = resolver(montagem(3))
        assertEquals(3, QuebraCabeca.estrelas(sem))
        assertEquals(1, QuebraCabeca.estrelas(sem.copy(dicas = 1)))
        assertEquals(2, QuebraCabeca.estrelas(sem.copy(arrastos = sem.pecas.size * 2 + 1)))
        assertEquals(0, QuebraCabeca.estrelas(montagem(3)))
    }

    @Test
    fun `bordas sao as pecas do contorno da moldura`() {
        val m = montagem(5) // 4 x 4
        assertEquals(12, m.pecas.count { QuebraCabeca.ehBorda(m, it.id) })
        assertFalse(QuebraCabeca.ehBorda(m, 5))
        assertTrue(QuebraCabeca.ehBorda(m, 0))
    }

    @Test
    fun `deslizante sempre embaralhado, com solucao e so troca com o vizinho do vazio`() {
        for (lado in 2..5) for (semente in 1L..20L) {
            val d = QuebraCabeca.deslizar(lado, semente)
            assertEquals(lado * lado, d.casas.size)
            assertEquals((0 until lado * lado).toSet(), d.casas.toSet())
            assertFalse("já veio resolvido: lado $lado semente $semente", d.resolvido)
        }
        val d = QuebraCabeca.deslizar(3, 4)
        val longe = d.casas.indices.first { it != d.vazio && it !in listOf(d.vazio - 1, d.vazio + 1, d.vazio - 3, d.vazio + 3) }
        assertEquals(d, QuebraCabeca.tocar(d, longe))
        assertEquals(d, QuebraCabeca.tocar(d, d.vazio))
        assertEquals(d, QuebraCabeca.tocar(d, 99))
    }

    @Test
    fun `seguindo a dica o deslizante pequeno chega ao fim`() {
        // A dica é gulosa, não ótima; num 2x2 ela resolve sempre dentro de um número generoso de jogadas.
        for (semente in 1L..15L) {
            var d = QuebraCabeca.deslizar(2, semente)
            var jogadas = 0
            while (!d.resolvido && jogadas < 60) {
                val c = QuebraCabeca.dicaDeslizante(d)
                assertTrue(c >= 0)
                d = QuebraCabeca.tocar(d, c)
                jogadas++
            }
            assertTrue("2x2 semente $semente não resolveu em 60 jogadas", d.resolvido)
        }
        assertEquals(-1, QuebraCabeca.dicaDeslizante(QuebraCabeca.Deslizante(3, QuebraCabeca.meta(3))))
    }

    @Test
    fun `tocar conta jogada so quando troca e estrelas do deslizante`() {
        val d = QuebraCabeca.deslizar(3, 9)
        val vizinho = QuebraCabeca.dicaDeslizante(d)
        val d2 = QuebraCabeca.tocar(d, vizinho)
        assertEquals(1, d2.toques)
        val feito = QuebraCabeca.Deslizante(3, QuebraCabeca.meta(3), toques = 20)
        assertEquals(3, QuebraCabeca.estrelasDoDeslizante(feito, 0))
        assertEquals(1, QuebraCabeca.estrelasDoDeslizante(feito, 2))
        assertEquals(2, QuebraCabeca.estrelasDoDeslizante(feito.copy(toques = 200), 0))
        assertEquals(0, QuebraCabeca.estrelasDoDeslizante(d, 0))
    }

    @Test
    fun `nivel aberto sobe com as vitorias da colecao e o um esta sempre aberto`() {
        assertEquals(1, QuebraCabeca.nivelAberto("bichos", emptyMap()))
        val f = mapOf("gato:1" to 2, "coelho:2" to 1, "peixe:5" to 3) // peixe é do mar
        assertEquals(3, QuebraCabeca.nivelAberto("bichos", f))
        assertEquals(6, QuebraCabeca.nivelAberto("mar", f))
        assertEquals(8, QuebraCabeca.nivelAberto("bichos", mapOf("gato:8" to 1)))
    }

    @Test
    fun `nivel adaptativo sobe com tres seguidas sem dica, desce se travou e respeita o aberto`() {
        assertEquals(4, QuebraCabeca.sugerir(3, 3, false, aberto = 8))
        assertEquals(3, QuebraCabeca.sugerir(3, 2, false, aberto = 8))
        assertEquals(2, QuebraCabeca.sugerir(3, 0, true, aberto = 8))
        assertEquals(3, QuebraCabeca.sugerir(3, 5, false, aberto = 3)) // não passa do aberto
        assertEquals(1, QuebraCabeca.sugerir(1, 0, true, aberto = 1))
        assertEquals(8, QuebraCabeca.sugerir(8, 9, false, aberto = 8))
    }

    @Test
    fun `premio da primeira vez, melhoria e repeticao`() {
        val p = QuebraCabeca.premio("gato", 2, 2, emptyMap())!!
        assertTrue(p.primeiraVez)
        assertEquals(3 + 2 * 2 + 2 * 2 + 5, p.moedas) // base + marco "Primeiro quebra-cabeça"
        assertEquals(1, p.pecaDourada)
        assertNotNull(p.marco)
        val feitos = mapOf("gato:2" to 2)
        val repetido = QuebraCabeca.premio("gato", 2, 2, feitos)!!
        assertFalse(repetido.primeiraVez)
        assertEquals(0, repetido.moedas)
        assertEquals(0, repetido.pecaDourada)
        val melhor = QuebraCabeca.premio("gato", 2, 3, feitos)!!
        assertEquals(2, melhor.moedas)
        assertEquals(1, melhor.pecaDourada) // chegou às 3 estrelas
        assertNull(QuebraCabeca.premio("naoexiste", 1, 1, emptyMap()))
        assertNull(QuebraCabeca.premio("gato", 1, 0, emptyMap()))
    }

    @Test
    fun `marcos pagam no total de quebra-cabecas diferentes e a colecao inteira da bonus`() {
        val nove = (1..9).associate { "gato:${(it - 1) % 8 + 1}".let { k -> if (k == "gato:1" && it > 8) "coelho:1" else k } to 1 }
        assertEquals(9, nove.size)
        val dez = QuebraCabeca.premio("passaro", 1, 1, nove)!!
        assertEquals("Montador", dez.marco?.nome)
        // Colecao inteira: todos os níveis de bichos, falta o oitavo.
        val ate7 = (1..7).associate { "gato:$it" to 1 }
        val fim = QuebraCabeca.premio("gato", 8, 1, ate7)!!
        assertTrue(fim.colecaoCompleta)
        assertTrue(fim.moedas >= 25)
        assertFalse(QuebraCabeca.premio("gato", 8, 1, ate7 + ("coelho:8" to 1))!!.colecaoCompleta)
    }

    @Test
    fun `pecas douradas saem dos feitos`() {
        assertEquals(0, QuebraCabeca.pecasDouradas(emptyMap()))
        assertEquals(3, QuebraCabeca.pecasDouradas(mapOf("gato:1" to 3, "gato:2" to 1)))
    }

    @Test
    fun `guardar e ler feitos tolera lixo`() {
        val m = mapOf("gato:3" to 2, "peixe:8" to 3)
        assertEquals(m, QuebraCabeca.textoParaFeitos(QuebraCabeca.feitosParaTexto(m)))
        assertTrue(QuebraCabeca.textoParaFeitos(null).isEmpty())
        assertTrue(QuebraCabeca.textoParaFeitos("gato:9:1,gato:0:1,gato:1:7,x:1:1,gato:1,gato:a:1,,").isEmpty())
    }
}
