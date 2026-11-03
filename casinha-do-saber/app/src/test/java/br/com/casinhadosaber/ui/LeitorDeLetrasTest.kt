package br.com.casinhadosaber.ui

import br.com.casinhadosaber.dominio.LeitorDeLetras
import br.com.casinhadosaber.dominio.Ponto
import br.com.casinhadosaber.ui.telas.atividades.ModelosDasLetras
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * O leitor das letras escritas com o dedo, com os modelos de verdade (os traços de TracosDasLetras).
 * Teste de JVM puro (sem Robolectric): a criança "escreve" o modelo com tremidinha, tamanho, inclinação,
 * ordem e sentido dos traços diferentes.
 */
class LeitorDeLetrasTest {

    private val leitor = ModelosDasLetras.leitor

    /** Traços do modelo com mais pontinhos (como o dedo de verdade, que manda muitos pontos). */
    private fun denso(t: List<Ponto>): List<Ponto> {
        if (t.size < 2) return t
        val r = mutableListOf(t.first())
        for ((a, b) in t.zipWithNext()) {
            val n = 8
            for (i in 1..n) r += Ponto(a.x + (b.x - a.x) * i / n, a.y + (b.y - a.y) * i / n)
        }
        return r
    }

    /**
     * Uma letra "escrita à mão": ondinha de [tremido] (fração do tamanho), um pouco mais larga ou
     * estreita, inclinada, em outro lugar e tamanho da tela, com traços fora de ordem e às vezes ao contrário.
     */
    private fun escrever(c: Char, r: Random, tremido: Float = 0.025f, deformar: Boolean = true): List<List<Ponto>> {
        val tracos = ModelosDasLetras.tracos(c).map(::denso)
        val escalaX = if (deformar) 0.85f + r.nextFloat() * 0.3f else 1f
        val angulo = if (deformar) (r.nextFloat() - 0.5f) * 0.2f else 0f
        val tamanho = 200f + r.nextFloat() * 400f
        val dx = r.nextFloat() * 300f
        val dy = r.nextFloat() * 300f
        val ordem = if (deformar) tracos.shuffled(r) else tracos
        return ordem.map { t ->
            val sentido = if (deformar && r.nextBoolean()) t.reversed() else t
            // O dedo balança devagar (ondinha ao longo do traço), mais uma tremidinha bem pequena em cada ponto.
            val fx = 1f + r.nextFloat() * 2f
            val fy = 1f + r.nextFloat() * 2f
            val px = r.nextFloat() * 6.28f
            val py = r.nextFloat() * 6.28f
            sentido.mapIndexed { k, p ->
                val s = k / sentido.size.toFloat()
                val x = (p.x - 0.5f) * escalaX + tremido * sin(6.28f * fx * s + px) + (r.nextFloat() - 0.5f) * 0.008f
                val y = (p.y - 0.6f) + tremido * sin(6.28f * fy * s + py) + (r.nextFloat() - 0.5f) * 0.008f
                val xr = x * cos(angulo) - y * sin(angulo)
                val yr = x * sin(angulo) + y * cos(angulo)
                Ponto(dx + xr * tamanho, dy + yr * tamanho)
            }
        }
    }

    @Test
    fun `cada modelo sem tremido e a propria letra`() {
        for (c in 'A'..'Z') {
            val l = leitor.ler(ModelosDasLetras.tracos(c), c)
            assertEquals("modelo de $c", c, l.melhor)
            assertTrue("modelo de $c não foi aceito ($l)", l.aceita)
        }
    }

    @Test
    fun `letras escritas com tremido e fora de ordem sao aceitas`() {
        val r = Random(11)
        var aceitas = 0
        var total = 0
        var primeiraMelhor = 0
        val recusadas = mutableListOf<String>()
        for (c in 'A'..'Z') repeat(20) {
            val l = leitor.ler(escrever(c, r), c)
            total++
            if (l.aceita) aceitas++ else recusadas += "$c(${l.melhor} ${"%.2f".format(l.distanciaEsperada)}/${"%.2f".format(l.distanciaMelhor)})"
            if (l.melhor == c) primeiraMelhor++
        }
        // Generoso: quase todas valem, mesmo tortas; e a letra certa é quase sempre a mais parecida.
        assertTrue("só $aceitas de $total aceitas: $recusadas", aceitas >= total * 0.95)
        assertTrue("só $primeiraMelhor de $total reconhecidas como a mais parecida", primeiraMelhor >= total * 0.85)
    }

    @Test
    fun `cada letra e aceita em pelo menos tres de cada quatro tentativas tortas`() {
        val r = Random(5)
        for (c in 'A'..'Z') {
            val aceitas = (1..12).count { leitor.ler(escrever(c, r, tremido = 0.035f), c).aceita }
            assertTrue("$c aceita só $aceitas de 12", aceitas >= 9)
        }
    }

    @Test
    fun `letra bem diferente da pedida nao vale`() {
        val r = Random(3)
        val pares = listOf('O' to 'L', 'X' to 'O', 'I' to 'M', 'S' to 'H', 'A' to 'O', 'T' to 'C', 'E' to 'V', 'Z' to 'U', 'B' to 'W', 'L' to 'A')
        for ((escrita, pedida) in pares) {
            repeat(5) {
                val l = leitor.ler(escrever(escrita, r), pedida)
                assertFalse("escreveu $escrita e valeu como $pedida ($l)", l.aceita)
            }
        }
    }

    @Test
    fun `letras parecidas sao distinguidas`() {
        // Escrever a parecida, bem feita, não vale como a pedida: o leitor vê a diferença.
        val confundiveis = listOf('P' to 'R', 'R' to 'B', 'E' to 'F', 'O' to 'Q', 'C' to 'G', 'V' to 'W', 'M' to 'N', 'P' to 'B', 'U' to 'V', 'I' to 'T')
        for ((escrita, pedida) in confundiveis) {
            val l = leitor.ler(ModelosDasLetras.tracos(escrita), pedida)
            assertEquals("escreveu $escrita, pedida $pedida", escrita, l.melhor)
            assertFalse("escreveu $escrita (bem feita) e valeu como $pedida ($l)", l.aceita)
        }
    }

    @Test
    fun `rabisco e toque nao valem`() {
        val r = Random(9)
        val rabisco = listOf(List(60) { Ponto(r.nextFloat() * 300f, r.nextFloat() * 300f) })
        for (c in "AEOS") assertFalse("rabisco valeu como $c", leitor.ler(rabisco, c).aceita)
        val toque = listOf(listOf(Ponto(10f, 10f)))
        assertFalse(leitor.ler(toque, 'I').aceita)
        assertFalse(leitor.ler(emptyList(), 'A').aceita)
    }

    @Test
    fun `I de um risco so tambem vale`() {
        val risco = listOf(List(20) { Ponto(100f, 50f + it * 10f) })
        assertTrue(leitor.ler(risco, 'I').aceita)
    }

    @Test
    fun `a nuvem tem sempre N pontos e o comprimento soma os tracos`() {
        val t = listOf(listOf(Ponto(0f, 0f), Ponto(3f, 4f)), listOf(Ponto(10f, 10f), Ponto(10f, 12f)))
        assertEquals(7f, LeitorDeLetras.comprimento(t), 1e-4f)
        assertEquals(LeitorDeLetras.N, LeitorDeLetras.nuvem(t).size)
        assertEquals(LeitorDeLetras.N, LeitorDeLetras.nuvem(listOf(listOf(Ponto(1f, 1f)))).size)
    }
}
