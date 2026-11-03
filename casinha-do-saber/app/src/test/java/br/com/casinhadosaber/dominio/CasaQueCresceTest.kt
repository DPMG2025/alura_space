package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class CasaQueCresceTest {

    private fun comFases(n: Int, e: EstadoJogo = EstadoJogo()) =
        e.copy(estrelas = (0 until n).associate { "LOGICA:${10_000 + it}" to 3 })

    @Test
    fun `uma coisa nova para a casa em cada uma das mil fases`() {
        val fases = Trilha.entries.size * GeradorDeFases.FASES_POR_TRILHA
        assertEquals(1000, fases)
        assertEquals(fases, ItensDaCasa.total)
        assertEquals("ids repetidos", ItensDaCasa.todos.size, ItensDaCasa.todos.map { it.id }.toSet().size)
        // Os ids vão para as preferências separados por vírgula e ponto e vírgula.
        for (i in ItensDaCasa.todos) assertTrue(i.id, i.id.none { it == ',' || it == ';' || it == '=' })
        // As 40 peças de sempre continuam iguais e na mesma ordem (saves antigos).
        assertEquals(ItensDaCasa.ordem, ItensDaCasa.todos.take(40))
        assertEquals("estrela", ItensDaCasa.todos[39].id)
    }

    @Test
    fun `areas chegam espalhadas pelas mil fases`() {
        val limiares = CasaQueCresce.areas.map { it.limiar }
        assertEquals(
            listOf(40, 80, 125, 170, 215, 260, 305, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 900, 950),
            limiares,
        )
        assertEquals(1000, CasaQueCresce.areas.last().limiar + CasaQueCresce.areas.last().tamanho)
        for (a in CasaQueCresce.areas) {
            // A obra é o prêmio da fase seguinte ao limiar.
            assertEquals(a.obra, ItensDaCasa.todos[a.limiar])
            assertTrue(a.id, a.obra.ehEstrutura)
            assertEquals(a.tamanho - 1, a.coisas.size)
            assertTrue(a.id, a.tipos.size >= 15)
            assertEquals("tipos repetidos em ${a.id}", a.tipos.size, a.tipos.map { it.id }.toSet().size)
            for (c in a.coisas) assertEquals(a, CasaQueCresce.areaDe(c.id))
        }
        val nomes = CasaQueCresce.areas.map { it.id }
        for (pedido in listOf("quarto2", "pet", "piscina", "parquinho", "horta", "brinquedos", "arvore", "estudio", "gourmet", "biblioteca", "observatorio", "flores")) {
            assertTrue(pedido, pedido in nomes)
        }
        assertTrue(CasaQueCresce.areas.any { it.tipo == TipoDeArea.DENTRO })
        assertTrue(CasaQueCresce.areas.any { it.tipo == TipoDeArea.FORA })
    }

    @Test
    fun `fase depois das quarenta da a obra do quarto novo e nao mais o brinde`() {
        val fases = Trilha.entries.flatMap { GeradorDeFases(Random(4)).fases(it) }
        var e = EstadoJogo()
        for (f in fases.take(40)) e = e.registrarPartida(f, 5, 5).estado
        val r = e.registrarPartida(fases[40], 5, 5)
        assertEquals("quarto2.obra", r.item?.id)
        assertNull(r.brinde)
        val r2 = r.estado.registrarPartida(fases[41], 5, 5)
        assertEquals("quarto2.cama", r2.item?.id)
        // Repetir uma fase não dá outra coisa.
        assertNull(r2.estado.registrarPartida(fases[41], 5, 5).item)
    }

    @Test
    fun `save antigo com sessenta fases recebe na hora a obra e as coisas que ficou devendo`() {
        // Save de antes da casa que cresce: 60 fases, as 40 peças colocadas e brindes da loja já ganhos.
        val pecas = ItensDaCasa.ordem.associate { it.id to Posicao(it.x, it.y) }
        val texto = Serializacao.posicoesParaTexto(pecas)
        val antigo = comFases(60).copy(colocados = Serializacao.textoParaPosicoes(texto), comprados = setOf("telhado1"))
        assertEquals(40, antigo.colocados.size)
        assertEquals(60, antigo.itensGanhos.size)
        // Antes de construir, o baú mostra só a obra.
        assertEquals(listOf("quarto2.obra"), antigo.itensNoBau.map { it.id })
        val construida = antigo.colocar(CasaQueCresce.porId.getValue("quarto2").obra)
        assertEquals(19, construida.itensNoBau.size)
        assertTrue(construida.itensNoBau.all { it.area == "quarto2" && !it.obra })
        assertEquals(listOf("quarto2"), CasaQueCresce.construidas(construida).map { it.id })
        // A obra não volta para o baú.
        assertEquals(construida, construida.guardar(CasaQueCresce.porId.getValue("quarto2").obra))
        // As posições das coisas novas também são guardadas e lidas de volta.
        val cama = ItensDaCasa.porId.getValue("quarto2.cama")
        val comCama = construida.colocar(cama, Posicao(0.3f, 0.7f))
        val lido = Serializacao.textoParaPosicoes(Serializacao.posicoesParaTexto(comCama.colocados))
        assertEquals(Posicao(0.3f, 0.7f), lido["quarto2.cama"])
        assertEquals(comCama.colocados, lido)
        assertEquals("telhado1", comCama.comprados.single())
    }

    @Test
    fun `progresso por area e meta que nunca para`() {
        val e = comFases(130)
        val pet = CasaQueCresce.porId.getValue("pet")
        val piscina = CasaQueCresce.porId.getValue("piscina")
        assertEquals(44, CasaQueCresce.progresso(e, pet).ganhas)
        assertTrue(CasaQueCresce.progresso(e, pet).completa)
        // 130 fases: a obra da piscina veio na 126ª e mais 4 coisas depois.
        assertEquals(4, CasaQueCresce.progresso(e, piscina).ganhas)
        assertEquals("Piscina 0/44", CasaQueCresce.progresso(e, piscina).rotulo)
        assertEquals(listOf("quarto2", "pet", "piscina"), CasaQueCresce.ganhas(130).map { it.id })
        assertEquals("parquinho" to 41, CasaQueCresce.proxima(130)!!.let { it.first.id to it.second })
        assertEquals(40, CasaQueCresce.metaAtual(0))
        assertEquals(80, CasaQueCresce.metaAtual(40))
        assertEquals(170, CasaQueCresce.metaAtual(130))
        assertEquals(1000, CasaQueCresce.metaAtual(999))
        assertEquals(1000, CasaQueCresce.metaAtual(1000))
        assertNull(CasaQueCresce.proxima(1000))
        var meta = 0
        for (n in 0..1000) {
            val m = CasaQueCresce.metaAtual(n)
            assertTrue(m >= meta && (m > n || n == 1000))
            meta = m
        }
    }

    @Test
    fun `copias tem nome de outra e tipo igual`() {
        val pet = CasaQueCresce.porId.getValue("pet")
        val segundo = pet.coisas[pet.tipos.size]
        assertEquals("pet.cachorro.2", segundo.id)
        assertEquals("outro cachorrinho", segundo.comArtigo)
        assertEquals("cachorro", CasaQueCresce.tipoDe(segundo.id))
        assertEquals("a boia", ItensDaCasa.porId.getValue("piscina.boia").comArtigo)
        assertTrue(CasaQueCresce.passeia("cachorro"))
        assertTrue(CasaQueCresce.passeia("pet.gato.2"))
        assertFalse(CasaQueCresce.passeia("pet.osso"))
        assertFalse(CasaQueCresce.passeia("cama"))
    }

    @Test
    fun `coisas das areas comecam sem se amontoar e dentro da area`() {
        for (a in CasaQueCresce.areas) {
            val caixas = a.coisas.map { it.id to ItensDaCasa.caixaNaCena(it) }
            for ((id, c) in caixas) {
                assertTrue(id, c.esq >= 0f && c.dir <= 1f && c.topo >= 0.05f && c.base <= 1f)
            }
            for (i in caixas.indices) for (j in i + 1 until caixas.size) {
                val f = caixas[i].second.sobreposicao(caixas[j].second)
                assertTrue("${caixas[i].first} e ${caixas[j].first}: $f", f <= 0.25f)
            }
            // A primeira de cada tipo do alto começa no céu ou na parede.
            for (c in a.coisas.take(a.tipos.size)) {
                val t = a.tipos.first { it.id == CasaQueCresce.tipoDe(c.id) }
                if (t.zona == Zona.ALTO) assertTrue(c.id, c.y < CasaQueCresce.HORIZONTE)
            }
        }
    }

    @Test
    fun `fim da casa e brinde so depois da fase mil`() {
        val e = comFases(999)
        assertEquals(1, e.itensGanhos.size - 998)
        assertNotNull(ItensDaCasa.proximo(999))
        assertNull(ItensDaCasa.proximo(1000))
    }

    @Test
    fun `passeio nunca sai do lugar e anda sem pulos`() {
        for (semente in listOf(0, 7, 12345, -3)) {
            var anterior = Passeio.em(0, semente)
            val vistos = mutableSetOf<Gesto>()
            var viradas = 0
            for (ms in 0L..Passeio.CICLO_MS * 2 step 16) {
                val p = Passeio.em(ms, semente)
                assertTrue(p.deslocamento in -1f..1f)
                assertTrue("pulo em $ms", abs(p.deslocamento - anterior.deslocamento) < 0.02f)
                if (p.paraDireita != anterior.paraDireita) viradas++
                vistos += p.gesto
                anterior = p
            }
            assertEquals(Gesto.entries.toSet(), vistos)
            assertTrue(viradas >= 2)
        }
        assertEquals(0.03f, Passeio.alcance(0.08f, 0.1f), 0.0001f)
        assertEquals(0.1f, Passeio.alcance(0.5f, 0.1f), 0.0001f)
        assertEquals("🐇", Passeio.corpoInteiro("🐰"))
        assertEquals("🦔", Passeio.corpoInteiro("🦔"))
    }
}
