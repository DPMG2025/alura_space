package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Livro de Colorir (Fase 10): carimbos e fundos como enfeite, borracha que tira carimbo, guardar e marcos. */
class CarimbosEFundosTest {

    private val gato = Desenhos.porId.getValue("gato")

    @Test
    fun `catalogo de carimbos tem os quatro basicos abertos e ids em ordem`() {
        assertEquals(Carimbos.todos.indices.toList(), Carimbos.todos.map { it.id })
        assertEquals(listOf("estrelinha", "coração", "flor", "pegadas"), Carimbos.liberados(0).map { it.nome })
        assertTrue(Carimbos.liberados(10).any { it.nome == "lua" })
        assertEquals(Carimbos.todos.size, Carimbos.liberados(60).size)
        assertEquals("lua", Carimbos.novoCom(10)?.nome)
        assertEquals("borboleta", Carimbos.novoCom(60)?.nome)
        assertNull(Carimbos.novoCom(0))
        assertNull(Carimbos.novoCom(11))
    }

    @Test
    fun `carimbar poe o enfeite no toque e recusa tipo ou tinta que nao existem`() {
        val o = Colorir.obraVazia(gato)
        val c = Colorir.carimbar(o, 1, 0.3f, 0.4f, 0.11f, 9)
        assertEquals(listOf(Carimbo(1, 0.3f, 0.4f, 0.11f, 9)), c.carimbos)
        assertSame(o, Colorir.carimbar(o, 99, 0.3f, 0.4f, 0.11f, 9))
        assertSame(o, Colorir.carimbar(o, -1, 0.3f, 0.4f, 0.11f, 9))
        assertSame(o, Colorir.carimbar(o, 0, 0.3f, 0.4f, 0.11f, 0))
        assertSame(o, Colorir.carimbar(o, 0, 0.3f, 0.4f, 0.11f, 999))
        // Fora do quadrado e tamanho exagerado ficam presos aos limites.
        val fora = Colorir.carimbar(o, 0, 1.5f, -2f, 9f, 1).carimbos.single()
        assertEquals(1f, fora.x)
        assertEquals(0f, fora.y)
        assertEquals(0.3f, fora.tamanho)
        // Carimbo não conta como região pintada nem termina o desenho.
        assertEquals(0, c.pintadas())
        assertFalse(Colorir.terminou(gato, c))
        assertFalse(c.vazia())
    }

    @Test
    fun `guarda no maximo quarenta carimbos e tira os mais antigos`() {
        var o = Colorir.obraVazia(gato)
        repeat(Colorir.MAXIMO_DE_CARIMBOS + 5) { o = Colorir.carimbar(o, it % 4, it / 100f, 0.5f, 0.07f, 1) }
        assertEquals(Colorir.MAXIMO_DE_CARIMBOS, o.carimbos.size)
        assertEquals(0.05f, o.carimbos.first().x, 1e-6f)
    }

    @Test
    fun `borracha tira primeiro o carimbo de cima e depois limpa a regiao`() {
        val r = gato.regiaoEm(0.5f, 0.5f)
        assertTrue("o centro do gato cai numa região", r >= 0)
        var o = Colorir.balde(gato, Colorir.obraVazia(gato), 0.5f, 0.5f, 3)
        o = Colorir.carimbar(o, 0, 0.5f, 0.5f, 0.11f, 1)
        o = Colorir.carimbar(o, 2, 0.5f, 0.5f, 0.11f, 9)
        assertEquals(1, Colorir.carimboEm(o, 0.52f, 0.5f))
        assertEquals(-1, Colorir.carimboEm(o, 0.9f, 0.9f))
        o = Colorir.borracha(gato, o, 0.5f, 0.5f)
        assertEquals(listOf(0), o.carimbos.map { it.tipo }) // saiu o de cima (a flor)
        assertEquals(3, o.tintas[r]) // a tinta ficou
        o = Colorir.borracha(gato, o, 0.5f, 0.5f)
        assertTrue(o.carimbos.isEmpty())
        assertEquals(3, o.tintas[r])
        o = Colorir.borracha(gato, o, 0.5f, 0.5f)
        assertEquals(0, o.tintas[r])
    }

    @Test
    fun `fundo troca sem mexer na pintura e balde nao apaga carimbo`() {
        val o = Colorir.carimbar(Colorir.obraVazia(gato), 3, 0.2f, 0.2f, 0.07f, 10)
        val f = Colorir.fundo(o, Fundo.MAR)
        assertEquals(Fundo.MAR, f.fundo)
        assertEquals(o.carimbos, f.carimbos)
        assertSame(f, Colorir.fundo(f, Fundo.MAR))
        assertFalse(Colorir.obraVazia(gato).copy(fundo = Fundo.CEU).vazia())
        val pintada = Colorir.balde(gato, f, 0.5f, 0.5f, 4)
        assertEquals(f.carimbos, pintada.carimbos)
        assertEquals(Fundo.MAR, pintada.fundo)
    }

    @Test
    fun `guardar e abrir mantem fundo e carimbos e le o formato antigo`() {
        var o = Colorir.balde(gato, Colorir.obraVazia(gato), 0.5f, 0.5f, 3)
        o = Colorir.carimbar(o, 1, 0.25f, 0.75f, 0.11f, 9)
        o = Colorir.carimbar(o, 4, 0.8f, 0.1f, 0.16f, 13)
        o = Colorir.fundo(o, Fundo.CAMPO)
        val volta = Colorir.textoParaObras(Colorir.obrasParaTexto(mapOf(gato.id to o))).getValue(gato.id)
        assertEquals(o, volta)
        // Sem enfeite, o texto continua no formato antigo de três partes.
        val simples = Colorir.balde(gato, Colorir.obraVazia(gato), 0.5f, 0.5f, 3)
        assertEquals(2, Colorir.obrasParaTexto(mapOf(gato.id to simples)).count { it == '#' })
        // Formato antigo lido pela versão nova: sem fundo e sem carimbos.
        val antigo = Colorir.textoParaObras("gato#" + "0".repeat(gato.regioes.size) + "#").getValue("gato")
        assertEquals(Fundo.NENHUM, antigo.fundo)
        assertTrue(antigo.carimbos.isEmpty())
        // Lixo nos enfeites é ignorado sem perder a pintura.
        val lixo = Colorir.textoParaObras("gato#" + "3".repeat(gato.regioes.size) + "##9#99:1:1:100:1|0:500:500:100:0|0:500:500:5:1|x|1:2000:1:100:1|2:500:500:110:9")
            .getValue("gato")
        assertEquals(Fundo.NENHUM, lixo.fundo)
        assertEquals(listOf(Carimbo(2, 0.5f, 0.5f, 0.11f, 9)), lixo.carimbos)
        assertTrue(lixo.tintas.all { it == 3 })
    }

    @Test
    fun `marcos de dez e sessenta desenhos abrem carimbo novo no premio`() {
        fun premioCom(feitos: Int): Colorir.Premio? {
            val outros = Desenhos.todos.filter { it.id != gato.id }.take(feitos - 1).associate { it.id to 1 }
            var o = Colorir.obraVazia(gato)
            for (i in gato.regioes.indices) o = o.copy(tintas = o.tintas.toMutableList().also { it[i] = 1 })
            return Colorir.premio(gato, o, outros)
        }
        assertEquals("lua", premioCom(10)?.carimboNovo?.nome)
        assertNull(premioCom(9)?.carimboNovo)
        assertNull(premioCom(11)?.carimboNovo)
        assertEquals("borboleta", premioCom(60)?.carimboNovo?.nome)
    }

    @Test
    fun `estado libera os carimbos pelos desenhos terminados`() {
        val e = EstadoJogo()
        assertEquals(4, e.carimbosLiberados.size)
        val dez = e.copy(coloridos = Desenhos.todos.take(10).associate { it.id to 1 })
        assertEquals(5, dez.carimbosLiberados.size)
    }
}
