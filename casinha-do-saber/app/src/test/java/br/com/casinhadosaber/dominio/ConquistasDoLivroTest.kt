package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Figurinhas e diplomas do Livro de Colorir: saem dos desenhos terminados, sem campo novo no save. */
class ConquistasDoLivroTest {

    private val bichos = Desenhos.daColecao("bichos")

    @Test
    fun `uma pagina por colecao com uma figurinha por desenho e a dourada no fim`() {
        val paginas = ConquistasDoLivro.paginas(emptyMap())
        assertEquals(Desenhos.colecoes.size, paginas.size)
        for (p in paginas) {
            assertEquals(Desenhos.daColecao(p.colecao).size + 1, p.figurinhas.size)
            assertTrue(p.figurinhas.last().dourada)
            assertEquals(0, p.quantasGanhas)
        }
        assertEquals(Desenhos.todos.size + Desenhos.colecoes.size, ConquistasDoLivro.totalDeFigurinhas)
        val ids = paginas.flatMap { it.figurinhas }.map { it.id }
        assertEquals("ids únicos", ids.size, ids.toSet().size)
        assertEquals("🐾", paginas.first().emoji)
        assertEquals("Bichos", paginas.first().nome)
    }

    @Test
    fun `desenho terminado cola a figurinha e a colecao inteira cola a dourada e da o diploma`() {
        val um = mapOf(bichos.first().id to 1)
        val p1 = ConquistasDoLivro.paginas(um).first()
        assertEquals(1, p1.quantasGanhas)
        assertTrue(p1.ganhou(p1.figurinhas.first()))
        assertFalse(p1.ganhou(p1.figurinhas.last()))
        assertTrue(ConquistasDoLivro.diplomas(um).isEmpty())

        val todos = bichos.associate { it.id to 2 }
        val p2 = ConquistasDoLivro.paginas(todos).first()
        assertTrue(p2.completa)
        assertEquals(listOf(DiplomaDeColecao("bichos")), ConquistasDoLivro.diplomas(todos))
        assertEquals("Diploma da coleção Bichos", ConquistasDoLivro.diplomas(todos).single().titulo)
        assertTrue(DiplomaDeColecao("bichos").texto("Ana").contains("Ana"))
        assertFalse(DiplomaDeColecao("bichos").texto(null).contains("{nome}"))
    }

    @Test
    fun `figurinhas novas mostram o desenho e a dourada que acabaram de entrar`() {
        val antes = bichos.dropLast(1).associate { it.id to 1 }
        val depois = antes + (bichos.last().id to 3)
        val novas = ConquistasDoLivro.figurinhasNovas(antes, depois)
        assertEquals(listOf("livro:${bichos.last().id}", "livro:bichos:ouro"), novas.map { it.id })
        assertTrue(ConquistasDoLivro.figurinhasNovas(depois, depois).isEmpty())
        // Melhorar as estrelas de um desenho já feito não dá figurinha de novo.
        assertTrue(ConquistasDoLivro.figurinhasNovas(depois, depois + (bichos.first().id to 3)).isEmpty())
    }

    @Test
    fun `como ganhar diz o desenho ou a colecao`() {
        val f = ConquistasDoLivro.figurinhasDaColecao("bichos")
        assertTrue(f.first().comoGanhar.startsWith("Termine o desenho "))
        assertEquals("Termine todos os desenhos da coleção", f.last().comoGanhar)
    }

    @Test
    fun `galeria mostra so os desenhos terminados com pintura guardada na ordem do livro`() {
        val gato = Desenhos.porId.getValue("gato")
        val segundo = bichos[1]
        val pintado = Colorir.obraModelo(gato)
        var e = EstadoJogo(obrasDeColorir = mapOf(gato.id to pintado, segundo.id to Colorir.obraModelo(segundo)), coloridos = mapOf(gato.id to 1))
        assertEquals(listOf(gato.id), e.obrasDaGaleria.map { it.first.id }) // o segundo está pintado, mas não terminado
        e = e.copy(coloridos = e.coloridos + (segundo.id to 2))
        assertEquals(Desenhos.todos.filter { it.id == gato.id || it.id == segundo.id }.map { it.id }, e.obrasDaGaleria.map { it.first.id })
        assertEquals(pintado, e.obrasDaGaleria.first { it.first.id == gato.id }.second)
        // Recomeçar tira da galeria, sem tirar as estrelas.
        e = e.recomecarObra(gato)
        assertEquals(listOf(segundo.id), e.obrasDaGaleria.map { it.first.id })
        assertEquals(1, e.coloridos[gato.id])
    }
}
