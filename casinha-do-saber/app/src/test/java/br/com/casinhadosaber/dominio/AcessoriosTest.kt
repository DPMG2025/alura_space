package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Roupinhas e acessórios: loja, guarda-roupa (um por parte do corpo), espelho e saves antigos. */
class AcessoriosTest {

    private val menino = Perfil("Leo", Genero.MENINO, pele = 3)
    private fun produto(id: String) = Loja.porId.getValue(id)

    @Test
    fun `catalogo tem roupas variadas e acessorios em todas as partes do corpo`() {
        assertTrue("pelo menos 14 roupas", Loja.trajes.size >= 14)
        assertEquals(Loja.trajes.size, Loja.trajes.map { it.id }.toSet().size)
        assertEquals(Loja.acessorios.size, Loja.acessorioPorId.size)
        for (parte in ParteDoCorpo.entries) {
            assertTrue("$parte com pelo menos 3 acessórios", Loja.acessorios.count { it.parte == parte } >= 3)
        }
        for (p in Loja.produtos.filter { it.categoria == Categoria.ROUPA || it.categoria == Categoria.ACESSORIO }) {
            assertTrue("${p.id} sem nome em inglês", p.nomeEn.isNotBlank())
            assertTrue("${p.id} com preço fora do equilíbrio", p.precoBase in 5..14)
        }
        // Cada gaveta mostra só o que é dela, e as gavetas juntas cobrem tudo o que se veste.
        val vestiveis = Loja.produtos.filter { it.categoria in setOf(Categoria.ROUPA, Categoria.CHAPEU, Categoria.ACESSORIO) }
        assertEquals(vestiveis.toSet(), Gaveta.entries.flatMap { Loja.daGaveta(it) }.toSet())
        assertEquals(vestiveis.size, Gaveta.entries.sumOf { Loja.daGaveta(it).size })
        assertTrue(Loja.daGaveta(Gaveta.MAO).all { Loja.acessorioPorId.getValue(it.valor).parte == ParteDoCorpo.MAO })
    }

    @Test
    fun `comprar acessorio cobra moedinhas e ja veste na parte certa`() {
        val oculos = produto("acessoriooculos")
        var e = EstadoJogo(perfil = menino, moedas = 40)
        val preco = Loja.preco(oculos, e)
        e = e.usar(oculos)!!
        assertEquals(40 - preco, e.moedas)
        assertTrue(oculos.id in e.comprados)
        assertEquals("oculos", e.perfil!!.acessorios[ParteDoCorpo.ROSTO])
        assertTrue(Loja.emUso(oculos, e))
        assertEquals(listOf(oculos), Loja.acessoriosComprados(e))
        // Sem moedas não compra.
        assertNull(EstadoJogo(perfil = menino, moedas = 1).usar(oculos))
    }

    @Test
    fun `um acessorio por parte do corpo mais chapeu e roupa ao mesmo tempo`() {
        var e = EstadoJogo(perfil = menino, moedas = 500)
        for (id in listOf("acessoriooculos", "acessoriomascara", "acessoriobalao", "acessoriobotas", "acessoriotiara", "acessoriocachecol", "acessoriomochila", "roupabailarina", "chapeu👑")) {
            e = e.usar(produto(id))!!
        }
        val a = e.perfil!!.acessorios
        // A máscara trocou os óculos (mesma parte); os outros ficaram juntos.
        assertEquals("mascara", a[ParteDoCorpo.ROSTO])
        assertEquals("balao", a[ParteDoCorpo.MAO])
        assertEquals("botas", a[ParteDoCorpo.PES])
        assertEquals("tiara", a[ParteDoCorpo.CABELO])
        assertEquals("cachecol", a[ParteDoCorpo.PESCOCO])
        assertEquals("mochila", a[ParteDoCorpo.COSTAS])
        assertEquals(ParteDoCorpo.entries.size, a.size)
        assertEquals("bailarina", e.perfil!!.traje)
        assertEquals("👑", e.perfil!!.chapeu)
        // Qualquer criança veste qualquer roupa: o menino de bailarina continua menino.
        assertEquals(Genero.MENINO, e.perfil!!.genero)
        assertFalse(Loja.emUso(produto("acessoriooculos"), e))

        // No guarda-roupa, vestir de novo os óculos (já são seus) troca a máscara, de graça.
        val moedas = e.moedas
        e = e.vestirAcessorio(ParteDoCorpo.ROSTO, "oculos")
        assertEquals("oculos", e.perfil!!.acessorios[ParteDoCorpo.ROSTO])
        assertEquals(moedas, e.moedas)
    }

    @Test
    fun `nao veste o que nao e seu nem na parte errada`() {
        var e = EstadoJogo(perfil = menino, moedas = 100)
        assertEquals(e, e.vestirAcessorio(ParteDoCorpo.MAO, "varinha"))
        assertEquals(e, e.vestirProduto(produto("acessoriovarinha")))
        e = e.usar(produto("acessoriovarinha"))!!.tirar(Gaveta.MAO)
        assertEquals(e, e.vestirAcessorio(ParteDoCorpo.PES, "varinha"))
        assertEquals(e, e.vestirAcessorio(ParteDoCorpo.MAO, "nao-existe"))
        assertEquals("varinha", e.vestirAcessorio(ParteDoCorpo.MAO, "varinha").perfil!!.acessorios[ParteDoCorpo.MAO])
        assertEquals("varinha", e.vestirProduto(produto("acessoriovarinha")).perfil!!.acessorios[ParteDoCorpo.MAO])
        // Sem perfil, nada muda.
        val sem = EstadoJogo(comprados = setOf("acessoriovarinha"))
        assertEquals(sem, sem.vestirAcessorio(ParteDoCorpo.MAO, "varinha"))
    }

    @Test
    fun `tirar deixa a parte livre e o item continua seu`() {
        var e = EstadoJogo(perfil = menino, moedas = 200)
        for (id in listOf("acessoriocolar", "acessoriotenis", "roupadinossauro", "chapeu🧢")) e = e.usar(produto(id))!!
        e = e.vestirAcessorio(ParteDoCorpo.PESCOCO, "")
        assertNull(e.perfil!!.acessorios[ParteDoCorpo.PESCOCO])
        assertEquals("tenis", e.perfil!!.acessorios[ParteDoCorpo.PES])
        e = e.tirar(Gaveta.PES).tirar(Gaveta.ROUPAS).tirar(Gaveta.CHAPEUS)
        assertTrue(e.perfil!!.acessorios.isEmpty())
        assertEquals("", e.perfil!!.traje)
        assertEquals("", e.perfil!!.chapeu)
        for (g in Gaveta.entries) assertFalse(e.perfil!!.temNa(g))
        assertTrue(setOf("acessoriocolar", "acessoriotenis", "roupadinossauro", "chapeu🧢").all { it in e.comprados })
        // Tocar na loja no que já é seu e está vestido tira (como chapéu e roupa).
        val tenis = produto("acessoriotenis")
        e = e.usar(tenis)!!
        assertTrue(e.perfil!!.temNa(Gaveta.PES))
        e = e.usar(tenis)!!
        assertFalse(e.perfil!!.temNa(Gaveta.PES))
    }

    @Test
    fun `experimentar antes de comprar nao muda o estado de verdade`() {
        val asas = produto("acessorioasas")
        val e = EstadoJogo(perfil = menino, moedas = 0)
        val previa = e.copy(moedas = Int.MAX_VALUE / 2).usar(asas)!!
        assertEquals("asas", previa.perfil!!.acessorios[ParteDoCorpo.COSTAS])
        assertTrue(e.perfil!!.acessorios.isEmpty())
        assertTrue(asas.id !in e.comprados)
        // O espelho e o guarda-roupa mostram a criança vestida sem comprar nada.
        assertTrue(menino.vestindo(asas).usa(asas))
        assertFalse(menino.usa(asas))
        assertEquals(menino, menino.vestindo(asas).tirando(Gaveta.COSTAS))
    }

    @Test
    fun `save antigo sem acessorios abre sem nada e o novo vai e volta`() {
        // Saves antigos: a chave não existe (null) ou está vazia.
        assertTrue(Serializacao.textoParaAcessorios(null).isEmpty())
        assertTrue(Serializacao.textoParaAcessorios("").isEmpty())
        assertTrue(Perfil().acessorios.isEmpty())
        assertTrue(Perfil("Ana", Genero.MENINA, 1, 2, 3, "👑", 1, "pijama").acessorios.isEmpty())

        val vestidos = mapOf(ParteDoCorpo.ROSTO to "oculosdesol", ParteDoCorpo.MAO to "ursinho", ParteDoCorpo.CABELO to "laco")
        val texto = Serializacao.acessoriosParaTexto(vestidos)
        assertEquals(vestidos, Serializacao.textoParaAcessorios(texto))
        assertEquals("", Serializacao.acessoriosParaTexto(emptyMap()))
        assertTrue(Serializacao.textoParaAcessorios(Serializacao.acessoriosParaTexto(emptyMap())).isEmpty())

        // Texto estragado, parte desconhecida, id desconhecido ou na parte errada: some sem derrubar o app.
        assertEquals(
            mapOf(ParteDoCorpo.PES to "botas"),
            Serializacao.textoParaAcessorios("lixo;;ORELHA=oculos;ROSTO=naoexiste;MAO=oculos;PES=botas;CABELO"),
        )
        // Mesmo um perfil com dado estranho desenha só o que é conhecido.
        val estranho = menino.copy(acessorios = mapOf(ParteDoCorpo.MAO to "oculos", ParteDoCorpo.ROSTO to "oculos"))
        assertNull(Loja.acessorioDe(estranho, ParteDoCorpo.MAO))
        assertEquals("oculos", Loja.acessorioDe(estranho, ParteDoCorpo.ROSTO)?.id)
    }

    @Test
    fun `brinde pode ser roupa ou acessorio e ja vem vestido`() {
        val g = GeradorDeFases(Random(5))
        val todas = Trilha.entries.flatMap { g.fases(it) }
        var e = EstadoJogo(perfil = menino)
        for (f in todas.take(ItensDaCasa.total)) e = e.registrarPartida(f, 5, 5).estado
        val roupa = produto("roupachef")
        val acessorio = produto("acessorioursinho")
        e = e.copy(comprados = Loja.produtos.map { it.id }.toSet() - roupa.id - acessorio.id)
        val r1 = e.registrarPartida(todas[ItensDaCasa.total], 5, 5)
        assertEquals(roupa, r1.brinde)
        assertEquals("chef", r1.estado.perfil!!.traje)
        val r2 = r1.estado.registrarPartida(todas[ItensDaCasa.total + 1], 5, 5)
        assertNotNull(r2.brinde)
        assertEquals(acessorio, r2.brinde)
        assertTrue(acessorio in Loja.acessoriosComprados(r2.estado))
        assertEquals("ursinho", r2.estado.perfil!!.acessorios[ParteDoCorpo.MAO])
        // A roupa do brinde anterior continua vestida junto com o acessório novo.
        assertEquals("chef", r2.estado.perfil!!.traje)
        assertTrue(r2.estado.moedas >= r1.estado.moedas + EstadoJogo.MOEDAS_DO_BRINDE)
    }

    @Test
    fun `lojinha de dentro vende acessorios e a de fora nao`() {
        val e = EstadoJogo()
        assertTrue(Loja.vitrine(e, Loja.categoriasDeDentro).any { it.categoria == Categoria.ACESSORIO })
        assertTrue(Loja.vitrine(e, Loja.categoriasDeFora).none { it.categoria == Categoria.ACESSORIO })
    }
}
