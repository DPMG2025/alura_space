package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Livro de Colorir e quebra-cabeças ligados ao estado do jogo: moedas, estrelas, obra guardada, cores novas. */
class ProgressoCriativoTest {

    private val gato = Desenhos.porId.getValue("gato")

    private fun terminado(d: DesenhoDeColorir, tintas: List<Int>): Obra =
        Obra(d.id, List(d.regioes.size) { tintas[it % tintas.size] })

    @Test
    fun `estado novo comeca sem obras, sem quebra-cabecas e com as doze cores`() {
        val e = EstadoJogo()
        assertTrue(e.obrasDeColorir.isEmpty())
        assertEquals(0, e.desenhosTerminados)
        assertEquals(12, e.tintasLiberadas.size)
        assertEquals(0, e.pecasDouradas)
        assertEquals(1, e.nivelDePuzzleAberto("bichos"))
        assertTrue(e.obraDe(gato).vazia())
    }

    @Test
    fun `guardar obra aceita so obra de desenho que existe com o tamanho certo`() {
        val e = EstadoJogo()
        val ok = e.guardarObra(Colorir.obraVazia(gato))
        assertEquals(1, ok.obrasDeColorir.size)
        assertEquals(e, e.guardarObra(Obra("naoexiste", listOf(0, 0))))
        assertEquals(e, e.guardarObra(Obra(gato.id, listOf(0, 0))))
        assertTrue(ok.recomecarObra(gato).obrasDeColorir.isEmpty())
    }

    @Test
    fun `terminar o desenho paga moedinhas uma vez, guarda estrelas e abre o dourado`() {
        val e0 = EstadoJogo(moedas = 10)
        val (e1, p) = e0.terminarDesenho(gato, terminado(gato, listOf(1, 2)))
        assertNotNull(p)
        assertEquals(10 + p!!.moedas, e1.moedas)
        assertEquals(1, e1.coloridos[gato.id])
        assertEquals(13, e1.tintasLiberadas.size)
        assertTrue(e1.obrasDeColorir.containsKey(gato.id))
        // De novo, igual: nada a mais.
        val (e2, p2) = e1.terminarDesenho(gato, terminado(gato, listOf(1, 2)))
        assertEquals(0, p2!!.moedas)
        assertEquals(e1.moedas, e2.moedas)
        // Melhorou para 2 estrelas: paga a diferença e guarda.
        val (e3, p3) = e2.terminarDesenho(gato, terminado(gato, listOf(1, 2, 3, 4, 5, 6)))
        assertEquals(3, p3!!.moedas)
        assertEquals(2, e3.coloridos[gato.id])
    }

    @Test
    fun `desenho incompleto so guarda a obra e nao paga nada`() {
        val e0 = EstadoJogo(moedas = 7)
        val (e1, p) = e0.terminarDesenho(gato, Colorir.obraVazia(gato))
        assertNull(p)
        assertEquals(7, e1.moedas)
        assertTrue(e1.coloridos.isEmpty())
        assertTrue(e1.obrasDeColorir.containsKey(gato.id))
    }

    @Test
    fun `vencer quebra-cabeca paga, guarda estrelas e conta seguidas sem dica`() {
        var e = EstadoJogo(moedas = 0)
        val (e1, p1) = e.venceuPuzzle("gato", 1, 3, usouDica = false)
        assertNotNull(p1)
        assertEquals(p1!!.moedas, e1.moedas)
        assertEquals(3, e1.puzzlesFeitos["gato:1"])
        assertEquals(1, e1.puzzleSeguidas)
        assertEquals(2, e1.nivelDePuzzleAberto("bichos"))
        assertEquals(2, e1.pecasDouradas) // 1 do primeiro + 1 das 3 estrelas
        e = e1
        // Com dica zera a sequência e não tira nada.
        val (e2, _) = e.venceuPuzzle("gato", 1, 1, usouDica = true)
        assertEquals(0, e2.puzzleSeguidas)
        assertEquals(3, e2.puzzlesFeitos["gato:1"])
        assertFalse(e2.moedas < e1.moedas)
        // Desenho que não existe: só conta a sequência.
        val (e3, p3) = e1.venceuPuzzle("naoexiste", 1, 3, usouDica = false)
        assertNull(p3)
        assertEquals(e1.moedas, e3.moedas)
    }

    @Test
    fun `estado guarda e le obras e quebra-cabecas pelos textos do repositorio`() {
        val e = EstadoJogo()
            .terminarDesenho(gato, terminado(gato, listOf(1, 2, 3, 4, 13))).first
            .venceuPuzzle("peixe", 2, 2, usouDica = false).first
        val obras = Colorir.textoParaObras(Colorir.obrasParaTexto(e.obrasDeColorir))
        val colo = Colorir.textoParaFeitos(Colorir.feitosParaTexto(e.coloridos))
        val puz = Puzzle.textoParaFeitos(Puzzle.feitosParaTexto(e.puzzlesFeitos))
        assertEquals(e.obrasDeColorir, obras)
        assertEquals(e.coloridos, colo)
        assertEquals(e.puzzlesFeitos, puz)
        assertEquals(3, colo[gato.id])
    }

    @Test
    fun `resumo para os pais conta desenhos, estrelas, quebra cabecas e pecas douradas`() {
        val vazio = EstadoJogo().resumoDasCriacoes
        assertFalse(vazio.comecou)
        assertEquals(Desenhos.todos.size, vazio.desenhosNoLivro)
        val e = EstadoJogo()
            .terminarDesenho(gato, terminado(gato, listOf(1, 2, 3, 4, 13))).first
            .venceuPuzzle("peixe", 2, 3, usouDica = false).first
        val r = e.resumoDasCriacoes
        assertTrue(r.comecou)
        assertEquals(1, r.desenhosTerminados)
        assertEquals(3, r.estrelasDeColorir)
        assertEquals(1, r.quebraCabecasVencidos)
        assertEquals(2, r.pecasDouradas)
    }

    @Test
    fun `pintor do dia muda a cada dia, percorre o livro e paga extra uma vez so`() {
        val hoje = 20_000L
        val d = Colorir.desenhoDoDia(hoje)
        assertTrue(Colorir.desenhoDoDia(hoje + 1) != d)
        assertEquals(d, Colorir.desenhoDoDia(hoje + Desenhos.todos.size))
        assertEquals(Desenhos.todos.first(), Colorir.desenhoDoDia(0))
        assertEquals(Desenhos.todos.first(), Colorir.desenhoDoDia(-Desenhos.todos.size.toLong()))
        val feito = Obra(d.id, List(d.regioes.size) { 1 + it % 2 })
        // Primeira vez do dia: bônus.
        val (e1, p1) = EstadoJogo().terminarDesenho(d, feito, hoje)
        assertTrue(p1!!.pintorDoDia)
        assertEquals(Colorir.premio(d, feito, emptyMap())!!.moedas + Colorir.MOEDAS_DO_PINTOR_DO_DIA, p1.moedas)
        assertEquals(hoje, e1.diaDoPintor)
        // Repetir no mesmo dia: sem bônus de novo.
        val (_, p2) = e1.terminarDesenho(d, feito, hoje)
        assertFalse(p2!!.pintorDoDia)
        // Outro desenho no mesmo dia: sem bônus.
        val outro = Desenhos.todos.first { it.id != d.id }
        val (_, p3) = EstadoJogo().terminarDesenho(outro, Obra(outro.id, List(outro.regioes.size) { 1 + it % 2 }), hoje)
        assertFalse(p3!!.pintorDoDia)
        // Sem informar o dia (-1): nunca tem bônus.
        assertFalse(EstadoJogo().terminarDesenho(d, feito).second!!.pintorDoDia)
        // No dia seguinte o desenho do dia é outro e o bônus volta.
        val d2 = Colorir.desenhoDoDia(hoje + 1)
        val (_, p4) = e1.terminarDesenho(d2, Obra(d2.id, List(d2.regioes.size) { 1 + it % 2 }), hoje + 1)
        assertTrue(p4!!.pintorDoDia)
    }
}
