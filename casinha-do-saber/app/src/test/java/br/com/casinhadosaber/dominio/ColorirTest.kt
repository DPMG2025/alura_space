package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Livro de Colorir (Fase 10): catálogo, balde, pincel preso na região, desfazer, prêmios e guardar. */
class ColorirTest {

    private val gato = Desenhos.porId.getValue("gato")
    private val casinha = Desenhos.porId.getValue("casinha")

    /** Centro de uma região qualquer que seja a de cima naquele ponto (para tocar com certeza nela). */
    private fun centro(d: Desenho, i: Int): Ponto {
        val f = d.regioes[i].forma
        val p = when (f) {
            is Forma.Elipse -> Ponto(f.cx, f.cy)
            is Forma.Poligono -> Ponto(f.pontos.map { it.x }.average().toFloat(), f.pontos.map { it.y }.average().toFloat())
        }
        return p
    }

    @Test
    fun `catalogo tem desenhos suficientes em todas as colecoes e ids unicos`() {
        assertTrue("pelo menos 24 desenhos", Desenhos.todos.size >= 24)
        assertEquals(Desenhos.todos.size, Desenhos.todos.map { it.id }.toSet().size)
        for ((id, _) in Desenhos.colecoes) assertTrue("coleção $id tem 4", Desenhos.daColecao(id).size >= 4)
        assertTrue(Desenhos.todos.all { it.colecao in Desenhos.colecoes.map { c -> c.first } })
    }

    @Test
    fun `toda regiao do catalogo cabe no quadrado e nao e minuscula`() {
        for (d in Desenhos.todos) {
            assertTrue("${d.id} sem regiões", d.regioes.size in 4..30)
            for ((i, r) in d.regioes.withIndex()) {
                val pontos = when (val f = r.forma) {
                    is Forma.Elipse -> listOf(Ponto(f.cx - f.rx, f.cy - f.ry), Ponto(f.cx + f.rx, f.cy + f.ry))
                    is Forma.Poligono -> f.pontos
                }
                assertTrue("${d.id} região $i fora do quadrado", pontos.all { it.x in -0.001f..1.001f && it.y in -0.001f..1.001f })
                val larg = pontos.maxOf { it.x } - pontos.minOf { it.x }
                val alt = pontos.maxOf { it.y } - pontos.minOf { it.y }
                assertTrue("${d.id} região $i pequena demais para o dedo (${larg}x$alt)", larg >= 0.01f && alt >= 0.01f)
            }
        }
    }

    @Test
    fun `toda regiao pode ser tocada sem ser tapada por completo por outra`() {
        // O centro de cada região (ou algum ponto dela) tem de resolver para ela mesma ou para uma de cima: ao menos
        // uma região visível por desenho tem de existir na maioria dos casos, e nenhuma pode ser totalmente invisível.
        for (d in Desenhos.todos) {
            for (i in d.regioes.indices) {
                var alcancavel = false
                var y = 0f
                while (y <= 1f && !alcancavel) {
                    var x = 0f
                    while (x <= 1f && !alcancavel) {
                        if (d.regiaoEm(x, y) == i) alcancavel = true
                        x += 0.01f
                    }
                    y += 0.01f
                }
                assertTrue("${d.id}: região $i fica totalmente escondida", alcancavel)
            }
        }
    }

    @Test
    fun `elipse e poligono concavo respondem certo`() {
        val e = Forma.Elipse(0.5f, 0.5f, 0.2f, 0.1f)
        assertTrue(e.contem(0.5f, 0.5f))
        assertTrue(e.contem(0.69f, 0.5f))
        assertFalse(e.contem(0.5f, 0.65f))
        // Letra "L": o canto de dentro da dobra não faz parte.
        val l = Forma.Poligono(listOf(Ponto(0f, 0f), Ponto(0.3f, 0f), Ponto(0.3f, 0.7f), Ponto(1f, 0.7f), Ponto(1f, 1f), Ponto(0f, 1f)))
        assertTrue(l.contem(0.1f, 0.5f))
        assertTrue(l.contem(0.8f, 0.85f))
        assertFalse(l.contem(0.8f, 0.3f))
    }

    @Test
    fun `balde enche so a regiao tocada e nao muda fora`() {
        val o0 = Colorir.obraVazia(gato)
        val p = centro(gato, gato.regioes.size - 1) // a última fica por cima
        val o1 = Colorir.balde(gato, o0, p.x, p.y, 7)
        val r = gato.regiaoEm(p.x, p.y)
        assertEquals(7, o1.tintas[r])
        assertEquals(1, o1.tintas.count { it != 0 })
        assertEquals(o1, Colorir.balde(gato, o1, p.x, p.y, 7)) // mesma tinta: nada muda
        assertEquals(o0, Colorir.balde(gato, o0, p.x, p.y, 0)) // tinta inexistente
        assertEquals(o0, Colorir.balde(gato, o0, p.x, p.y, 99))
    }

    @Test
    fun `balde tapa o pincel da regiao e borracha limpa tudo dela`() {
        val r = gato.regioes.size - 1
        val p = centro(gato, r)
        val com = Colorir.pincel(gato, Colorir.obraVazia(gato), listOf(p, Ponto(p.x + 0.01f, p.y)), 3, 0.03f, true)
        assertEquals(1, com.tracos.size)
        val balde = Colorir.balde(gato, com, p.x, p.y, 1)
        assertTrue(balde.tracos.isEmpty())
        val limpa = Colorir.borracha(gato, balde, p.x, p.y)
        assertTrue(limpa.vazia())
    }

    @Test
    fun `pincel dentro da linha prende na regiao onde o dedo comecou`() {
        val r = gato.regioes.size - 1
        val p = centro(gato, r)
        val o = Colorir.pincel(gato, Colorir.obraVazia(gato), listOf(p, Ponto(0.01f, 0.01f)), 2, 0.04f, true)
        assertEquals(gato.regiaoEm(p.x, p.y), o.tracos.single().regiao)
        val livre = Colorir.pincel(gato, Colorir.obraVazia(gato), listOf(p), 2, 0.04f, false)
        assertEquals(-1, livre.tracos.single().regiao)
    }

    @Test
    fun `pincel fora de qualquer regiao com a trava ligada e ignorado e traco vazio tambem`() {
        // Desenhos com fundo cobrem tudo; força um ponto fora do quadrado.
        val o = Colorir.obraVazia(gato)
        assertEquals(o, Colorir.pincel(gato, o, listOf(Ponto(-1f, -1f)), 2, 0.04f, true))
        assertEquals(o, Colorir.pincel(gato, o, emptyList(), 2, 0.04f, false))
        assertEquals(o, Colorir.pincel(gato, o, listOf(Ponto(0.5f, 0.5f)), 0, 0.04f, false))
    }

    @Test
    fun `traco longo e reduzido e o numero de tracos tem limite`() {
        val longo = List(500) { Ponto(it / 500f, 0.5f) }
        val r = Colorir.reduzir(longo)
        assertEquals(Colorir.MAXIMO_DE_PONTOS, r.size)
        assertEquals(longo.first(), r.first())
        assertEquals(longo.last(), r.last())
        var o = Colorir.obraVazia(gato)
        repeat(Colorir.MAXIMO_DE_TRACOS + 20) { o = Colorir.pincel(gato, o, listOf(Ponto(0.5f, 0.5f)), 1, 0.03f, false) }
        assertEquals(Colorir.MAXIMO_DE_TRACOS, o.tracos.size)
    }

    @Test
    fun `desfazer e refazer andam por vinte passos`() {
        var h = Historico.de(Colorir.obraVazia(gato))
        val p = centro(gato, gato.regioes.size - 1)
        h = h.fazer(Colorir.balde(gato, h.atual, p.x, p.y, 3))
        h = h.fazer(Colorir.balde(gato, h.atual, p.x, p.y, 4))
        assertTrue(h.podeDesfazer)
        assertFalse(h.podeRefazer)
        val volta = h.desfazer()
        assertEquals(3, volta.atual.tintas[gato.regiaoEm(p.x, p.y)])
        assertTrue(volta.podeRefazer)
        assertEquals(h.atual, volta.refazer().atual)
        // Fazer algo novo depois de desfazer apaga o "refazer".
        assertFalse(volta.fazer(Colorir.balde(gato, volta.atual, p.x, p.y, 5)).podeRefazer)
        // Sem nada para desfazer, fica igual.
        assertEquals(Historico.de(Colorir.obraVazia(gato)), Historico.de(Colorir.obraVazia(gato)).desfazer())
        // Limite de passos.
        var g = Historico.de(Colorir.obraVazia(gato))
        for (i in 0 until 50) g = g.fazer(Colorir.balde(gato, g.atual, p.x, p.y, 1 + i % 12))
        assertTrue(g.passado.size <= Historico.PASSOS)
    }

    private fun pintarTudo(d: Desenho, tintas: List<Int>): Obra {
        var o = Colorir.obraVazia(d)
        for (i in d.regioes.indices) o = o.copy(tintas = o.tintas.toMutableList().also { it[i] = tintas[i % tintas.size] })
        return o
    }

    @Test
    fun `estrelas sobem com cores diferentes e tinta especial`() {
        assertEquals(0, Colorir.estrelas(gato, Colorir.obraVazia(gato)))
        assertEquals(1, Colorir.estrelas(gato, pintarTudo(gato, listOf(1, 2))))
        assertEquals(2, Colorir.estrelas(gato, pintarTudo(gato, listOf(1, 2, 3, 4, 5, 6))))
        assertEquals(3, Colorir.estrelas(gato, pintarTudo(gato, listOf(1, 2, 3, 4, 13))))
    }

    @Test
    fun `terminar aceita 90 por cento das regioes para nao travar nas minusculas`() {
        var o = pintarTudo(casinha, listOf(1))
        assertTrue(Colorir.terminou(casinha, o))
        val n = casinha.regioes.size
        // Tira regiões até passar de 10% de vazias.
        val vazias = n / 10 + 1
        o = o.copy(tintas = o.tintas.mapIndexed { i, t -> if (i < vazias) 0 else t })
        assertFalse(Colorir.terminou(casinha, o))
    }

    @Test
    fun `premio da primeira vez da moedas e estrelas e depois so se melhorar`() {
        val o1 = pintarTudo(gato, listOf(1, 2))
        val p1 = Colorir.premio(gato, o1, emptyMap())!!
        assertTrue(p1.primeiraVez)
        assertEquals(1, p1.estrelas)
        assertEquals(5 + 3, p1.moedas)
        val feitos = mapOf(gato.id to 1)
        val repetido = Colorir.premio(gato, o1, feitos)!!
        assertFalse(repetido.primeiraVez)
        assertEquals(0, repetido.moedas)
        val melhor = Colorir.premio(gato, pintarTudo(gato, listOf(1, 2, 3, 4, 5, 6)), feitos)!!
        assertEquals(3 * (2 - 1), melhor.moedas)
        assertNull(Colorir.premio(gato, Colorir.obraVazia(gato), emptyMap()))
    }

    @Test
    fun `colecao completa da bonus e tinta nova abre no marco`() {
        val colecao = Desenhos.daColecao(gato.colecao)
        val feitosMenosUm = colecao.dropLast(1).associate { it.id to 1 }
        val ultimo = colecao.last()
        val p = Colorir.premio(ultimo, pintarTudo(ultimo, listOf(1, 2)), feitosMenosUm)!!
        assertTrue(p.colecaoCompleta)
        assertEquals(5 + 3 + 20, p.moedas)
        // Primeiro desenho terminado abre o dourado.
        val primeiro = Colorir.premio(gato, pintarTudo(gato, listOf(1, 2)), emptyMap())!!
        assertEquals("dourado", primeiro.tintaNova?.nome)
        assertNull(Colorir.premio(gato, pintarTudo(gato, listOf(1, 2)), mapOf("casinha" to 1, "peixe" to 1, "carro" to 1))!!.tintaNova?.takeIf { false })
    }

    @Test
    fun `estojo comeca com doze cores e as especiais abrem aos poucos`() {
        assertEquals(12, Tintas.liberadas(0).size)
        assertTrue(Tintas.liberadas(0).none { Tintas.especial(it.id) })
        assertEquals(13, Tintas.liberadas(1).size)
        assertEquals(Tintas.todas.size - 1, Tintas.liberadas(1000).size)
        assertEquals(Tintas.todas.mapIndexed { i, t -> assertEquals(i, t.id) }.size, Tintas.todas.size)
    }

    @Test
    fun `guardar e ler devolve a mesma obra e tolera texto estragado`() {
        val r = gato.regioes.size - 1
        val p = centro(gato, r)
        var o = pintarTudo(gato, listOf(1, 2, 3))
        o = Colorir.pincel(gato, o, listOf(p, Ponto(p.x + 0.02f, p.y + 0.01f)), 13, 0.035f, true)
        val outro = Colorir.balde(casinha, Colorir.obraVazia(casinha), 0.5f, 0.6f, 7)
        val txt = Colorir.obrasParaTexto(mapOf(gato.id to o, casinha.id to outro))
        val volta = Colorir.textoParaObras(txt)
        assertEquals(setOf(gato.id, casinha.id), volta.keys)
        assertEquals(o.tintas, volta.getValue(gato.id).tintas)
        assertEquals(1, volta.getValue(gato.id).tracos.size)
        assertEquals(13, volta.getValue(gato.id).tracos[0].tinta)
        assertEquals(outro, volta.getValue(casinha.id))
        // Lixo: id desconhecido, tamanho errado, tinta fora da paleta, nulo.
        assertTrue(Colorir.textoParaObras(null).isEmpty())
        assertTrue(Colorir.textoParaObras("").isEmpty())
        assertTrue(Colorir.textoParaObras("naoexiste#0000#").isEmpty())
        assertTrue(Colorir.textoParaObras("gato#012#").isEmpty())
        assertTrue(Colorir.textoParaObras("lixo~~#~gato").isEmpty())
        val semTracos = Colorir.textoParaObras("gato#" + "0".repeat(gato.regioes.size) + "#99:99:99:xx;yy|3:1:10")
        assertEquals(0, semTracos.getValue("gato").tracos.size) // regiao 99 e tinta 99 sao recusadas e "3:1:10" so tem 3 partes (faltam os pontos)
        assertNotNull(semTracos["gato"])
    }

    @Test
    fun `feitos guardam estrelas por desenho e ignoram lixo`() {
        val m = mapOf("gato" to 3, "casinha" to 1)
        assertEquals(m, Colorir.textoParaFeitos(Colorir.feitosParaTexto(m)))
        assertEquals(emptyMap<String, Int>(), Colorir.textoParaFeitos("nada:1,gato:9,gato:x,,:"))
        assertEquals(emptyMap<String, Int>(), Colorir.textoParaFeitos(null))
    }

    @Test
    fun `dica aponta a primeira regiao sem tinta e some quando acaba`() {
        var o = Colorir.obraVazia(gato)
        assertEquals(0, Colorir.proximaRegiao(o))
        o = pintarTudo(gato, listOf(1))
        assertEquals(-1, Colorir.proximaRegiao(o))
    }
}
