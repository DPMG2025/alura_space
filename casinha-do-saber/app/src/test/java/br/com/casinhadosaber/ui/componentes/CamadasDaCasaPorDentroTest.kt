package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Andar
import br.com.casinhadosaber.dominio.Comodo
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Movel
import br.com.casinhadosaber.dominio.PlantaDaCasa
import br.com.casinhadosaber.dominio.Trilha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs

/**
 * A casa por dentro com a arte 3D (Fase 6): todos os ids que a tela pede estão no manifesto, a projeção é a mesma
 * do app ([GradeIsometrica], usada por [Projecao]) e cada imagem, posta pelo apoio na referência da planta
 * (canto = Projecao.ponto(referência) - apoio x k), cai em cima da caixa do móvel, do piso, da parede, da janela e da
 * porta que ela substitui. JUnit puro, contra o `manifesto.json` versionado.
 */
class CamadasDaCasaPorDentroTest {

    private val pasta: File = listOf("src/main/assets/arte", "app/src/main/assets/arte")
        .map(::File).firstOrNull { it.isDirectory }
        ?: error("pasta assets/arte não encontrada a partir de ${File(".").absolutePath}")

    private val manifesto by lazy { ManifestoDaArte.ler(File(pasta, "manifesto.json").readText()) }

    private fun info(id: String): InfoDaArte = manifesto.imagens[id] ?: error("falta $id no manifesto")

    /** Ladrilho da arte (128 px): com ele, k = 1 e a imagem cai no pixel exato da grade. */
    private val l = 128f

    /** Retângulo (esq, topo, dir, baixo) na tela que contém os pontos da planta, com ladrilho [l] e origem (0, 0). */
    private fun caixaNaTela(pontos: List<PontoDaPlanta>): FloatArray {
        val xs = pontos.map { GradeIsometrica.x(l, 0f, it.x, it.y) }
        val ys = pontos.map { GradeIsometrica.y(l, 0f, it.x, it.y, it.z) }
        return floatArrayOf(xs.min(), ys.min(), xs.max(), ys.max())
    }

    private fun cantos(x0: Float, y0: Float, x1: Float, y1: Float, z0: Float, z1: Float) =
        listOf(x0, x1).flatMap { x -> listOf(y0, y1).flatMap { y -> listOf(z0, z1).map { z -> PontoDaPlanta(x, y, z) } } }

    /** A imagem cobre a caixa dos lados e embaixo (com [folga] px de tolerância); em cima pode ser mais baixa. */
    private fun cobre(nome: String, lugar: LugarDaArte, caixa: FloatArray, folga: Float = 6f, emCima: Boolean = false) {
        assertTrue("$nome: começa ${caixa[0] - lugar.esquerda} px dentro da caixa (esquerda)", lugar.esquerda <= caixa[0] + folga)
        assertTrue("$nome: termina antes da caixa (direita)", lugar.direita >= caixa[2] - folga)
        assertTrue("$nome: termina antes da caixa (embaixo)", lugar.baixo >= caixa[3] - folga)
        if (emCima) assertTrue("$nome: começa abaixo da caixa (em cima)", lugar.topo <= caixa[1] + folga)
        // E não sobra demais: no máximo meio ladrilho de folga em volta (sombra de contato e recorte justo).
        assertTrue("$nome: imagem larga demais para a caixa", lugar.esquerda >= caixa[0] - 0.75f * l && lugar.direita <= caixa[2] + 0.75f * l)
    }

    @Test
    fun `a grade e a mesma do app`() {
        // Projecao (ui/componentes/Isometrico.kt) usa estas contas: x = o + (gx - gy) * l / 2, y = o + (gx + gy) * l / 4 - z * 0,56 * l.
        assertEquals(0.56f, GradeIsometrica.UNIDADE_Z, 0f)
        assertEquals(10f + (3f - 1f) * 64f, GradeIsometrica.x(128f, 10f, 3f, 1f), 1e-4f)
        assertEquals(20f + (3f + 1f) * 32f - 2f * 71.68f, GradeIsometrica.y(128f, 20f, 3f, 1f, 2f), 1e-3f)
        // Uma célula do piso tem 128 x 64 px e uma unidade de altura 71,68 px, como diz o relatório da arte.
        assertEquals(64f, GradeIsometrica.x(l, 0f, 1f, 0f) - GradeIsometrica.x(l, 0f, 0f, 0f), 1e-4f)
        assertEquals(64f, GradeIsometrica.y(l, 0f, 1f, 1f) - GradeIsometrica.y(l, 0f, 0f, 0f), 1e-4f)
        assertEquals(71.68f, GradeIsometrica.y(l, 0f, 0f, 0f) - GradeIsometrica.y(l, 0f, 0f, 0f, 1f), 1e-3f)
    }

    @Test
    fun `todos os ids da casa por dentro estao no manifesto com a mesma camera`() {
        val ids = CamadasDaCasaPorDentro.idsPossiveis()
        assertTrue(ids.size >= 50)
        for (id in ids) {
            val i = info(id)
            assertEquals("$id: câmera", "isometrico", i.tipo)
            assertEquals("$id: ladrilho", CamadasDaCasaPorDentro.LADRILHO_DA_ARTE_DP.toDouble(), i.numeroIso("ladrilho")!!, 0.0)
            assertEquals("$id: unidade z", GradeIsometrica.UNIDADE_Z.toDouble(), i.numeroIso("unidadeZ")!!, 1e-6)
            assertEquals("$id: ladrilho da arte em px", l, CamadasDaCasaPorDentro.ladrilhoDaArte(i, manifesto.escala), 0f)
            assertTrue("$id: arquivo existe", File(pasta, i.arquivo).isFile)
        }
        // Nenhuma imagem dentro_* sobrando sem uso no app.
        val sobrando = manifesto.imagens.keys.filter { it.startsWith(CamadasDaCasaPorDentro.PREFIXO) } - ids
        assertTrue("imagens dentro_* que o app não usa: $sobrando", sobrando.isEmpty())
    }

    @Test
    fun `canto da imagem e a referencia projetada menos o apoio vezes k`() {
        val i = info("dentro_sofa")
        val ref = PontoDaPlanta(1.875f, 0.54f, 0f)
        for (ladrilho in listOf(128f, 64f, 150f, 37.5f)) {
            val k = ladrilho / 128f
            assertEquals(k, CamadasDaCasaPorDentro.fator(ladrilho, i, manifesto.escala), 1e-6f)
            val lugar = CamadasDaCasaPorDentro.lugar(i, manifesto.escala, ladrilho, 40f, 90f, ref)
            assertEquals(GradeIsometrica.x(ladrilho, 40f, ref.x, ref.y) - i.apoio.x * k, lugar.esquerda, 1e-3f)
            assertEquals(GradeIsometrica.y(ladrilho, 90f, ref.x, ref.y, ref.z) - i.apoio.y * k, lugar.topo, 1e-3f)
            assertEquals(i.largura * k, lugar.largura, 1e-3f)
            assertEquals(i.altura * k, lugar.altura, 1e-3f)
        }
    }

    @Test
    fun `cada movel da planta tem a imagem da sua pegada e a referencia do app`() {
        for (m in PlantaDaCasa.moveis) {
            val estados = if (CamadasDaCasaPorDentro.abre(m)) listOf(false, true) else listOf(false)
            for (aberto in estados) {
                val id = CamadasDaCasaPorDentro.idDoMovel(m, aberto)
                val i = info(id)
                assertEquals("$id: móvel", m.id, i.textoIso("movel"))
                assertEquals("$id: cômodo", CamadasDaCasaPorDentro.nomeDoComodo(m.comodo), i.textoIso("comodo"))
                assertEquals("$id: frente", m.frente.name, i.textoIso("frente"))
                val estado = if (!aberto) "normal" else if (m.id == "geladeira") "aberta" else "aberto"
                assertEquals("$id: estado", estado, i.textoIso("estado"))
                // A pegada da imagem é a caixa de oclusão do app (a mesma que PlantaDaCasa.ordenarParaDesenho usa).
                val c = PlantaDaCasa.caixaDo(m)
                for ((campo, valor) in listOf("x0" to c.x0, "y0" to c.y0, "x1" to c.x1, "y1" to c.y1, "z" to c.z0, "altura" to c.z1 - c.z0)) {
                    assertEquals("$id: pegada.$campo", valor.toDouble(), i.numeroIso("pegada.$campo")!!, 1e-4)
                }
                val ref = CamadasDaCasaPorDentro.referenciaDoMovel(m)
                assertEquals("$id: referência x", ref.x.toDouble(), i.numeroIso("referencia.x")!!, 1e-4)
                assertEquals("$id: referência y", ref.y.toDouble(), i.numeroIso("referencia.y")!!, 1e-4)
                assertEquals("$id: referência z", ref.z.toDouble(), i.numeroIso("referencia.z")!!, 1e-4)
            }
        }
    }

    @Test
    fun `a imagem de cada movel cai em cima da caixa dele`() {
        for (m in PlantaDaCasa.moveis) {
            val i = info(CamadasDaCasaPorDentro.idDoMovel(m))
            val lugar = CamadasDaCasaPorDentro.lugar(i, manifesto.escala, l, 0f, 0f, CamadasDaCasaPorDentro.referenciaDoMovel(m))
            val c = PlantaDaCasa.caixaDo(m)
            cobre(i.id, lugar, caixaNaTela(cantos(c.x0, c.y0, c.x1, c.y1, c.z0, c.z1)))
            // Centrada na caixa (o patinho boia e afunda um pouco: tolerância maior embaixo, já coberta acima).
            val caixa = caixaNaTela(cantos(c.x0, c.y0, c.x1, c.y1, c.z0, c.z1))
            val meioDaCaixa = (caixa[0] + caixa[2]) / 2f
            val meioDaImagem = (lugar.esquerda + lugar.direita) / 2f
            if (!CamadasDaCasaPorDentro.abre(m) && !m.naParede) assertTrue("${i.id}: fora do meio da caixa", abs(meioDaCaixa - meioDaImagem) < 3f)
        }
    }

    @Test
    fun `movel arrastado usa a mesma imagem so deslocada`() {
        val sofa = PlantaDaCasa.porId.getValue("sofa")
        val i = info(CamadasDaCasaPorDentro.idDoMovel(sofa))
        val antes = CamadasDaCasaPorDentro.lugar(i, manifesto.escala, l, 0f, 0f, CamadasDaCasaPorDentro.referenciaDoMovel(sofa))
        val movido: Movel = sofa.deslocado(1, 2)
        val depois = CamadasDaCasaPorDentro.lugar(i, manifesto.escala, l, 0f, 0f, CamadasDaCasaPorDentro.referenciaDoMovel(movido))
        assertEquals((1 - 2) * 64f, depois.esquerda - antes.esquerda, 1e-3f)
        assertEquals((1 + 2) * 32f, depois.topo - antes.topo, 1e-3f)
        assertEquals(antes.largura, depois.largura, 0f)
    }

    @Test
    fun `pisos e paredes cobrem o comodo, mesmo com o apoio fora da imagem`() {
        val h = PlantaDaCasa.ALTURA_PAREDE
        val fundo = PlantaDaCasa.FUNDO.toFloat()
        for (c in Comodo.entries) {
            val lim = PlantaDaCasa.limitesDo(c)
            val piso = info("dentro_piso_${CamadasDaCasaPorDentro.nomeDoComodo(c)}")
            val parede = info("dentro_parede_${CamadasDaCasaPorDentro.nomeDoComodo(c)}")
            for (i in listOf(piso, parede)) {
                assertEquals(0.0, i.numeroIso("referencia.x")!!, 0.0)
                assertEquals(0.0, i.numeroIso("referencia.y")!!, 0.0)
                assertEquals(0.0, i.numeroIso("referencia.z")!!, 0.0)
                assertEquals(CamadasDaCasaPorDentro.nomeDoAndar(c.andarDaPlanta()), i.textoIso("andar"))
            }
            assertEquals(CamadasDaCasaPorDentro.nomeDoComodo(c), parede.textoIso("tingir"))
            val lugarPiso = CamadasDaCasaPorDentro.lugar(piso, manifesto.escala, l, 0f, 0f, CamadasDaCasaPorDentro.ORIGEM)
            cobre(piso.id, lugarPiso, caixaNaTela(cantos(lim.x0, lim.y0, lim.x1, lim.y1, 0f, 0f)), emCima = true)
            val lugarParede = CamadasDaCasaPorDentro.lugar(parede, manifesto.escala, l, 0f, 0f, CamadasDaCasaPorDentro.ORIGEM)
            val doFundo = cantos(lim.x0, 0f, lim.x1, 0f, 0f, h) + if (lim.x0 == 0f) cantos(0f, 0f, 0f, fundo, 0f, h) else emptyList()
            cobre(parede.id, lugarParede, caixaNaTela(doFundo), emCima = true)
        }
        // As quatro cascas com o apoio fora do recorte justo continuam alinhadas pela referência (0, 0, 0).
        for (id in listOf("dentro_piso_banheiro", "dentro_piso_cozinha", "dentro_parede_banheiro", "dentro_parede_cozinha")) {
            val i = info(id)
            assertTrue("$id: apoio fora da imagem", i.apoio.x !in 0..i.largura || i.apoio.y !in 0..i.altura)
        }
    }

    @Test
    fun `janelas porta e parede interna caem nos lugares do desenho antigo`() {
        for (a in Andar.entries) for (cx in CamadasDaCasaPorDentro.janelasRetas(a)) {
            val i = info("dentro_janela")
            val lugar = CamadasDaCasaPorDentro.lugar(i, manifesto.escala, l, 0f, 0f, PontoDaPlanta(cx, 0f, CamadasDaCasaPorDentro.BASE_DA_JANELA))
            val m = CamadasDaCasaPorDentro.MEIA_JANELA + 0.08f
            cobre("janela em $cx", lugar, caixaNaTela(cantos(cx - m, 0f, cx + m, 0f, 1.32f, 2.58f)), emCima = true)
        }
        // A janela redonda do banheiro: raio 0,5 em volta de (4,975; 2,05).
        val r = CamadasDaCasaPorDentro.centroDaJanelaRedonda
        assertEquals(4.975f, r, 1e-4f)
        val redonda = CamadasDaCasaPorDentro.lugar(info("dentro_janela_redonda"), manifesto.escala, l, 0f, 0f, PontoDaPlanta(r, 0f, CamadasDaCasaPorDentro.BASE_DA_JANELA_REDONDA))
        cobre("janela redonda", redonda, caixaNaTela(cantos(r - 0.5f, 0f, r + 0.5f, 0f, 1.55f, 2.55f)), folga = 8f)
        // A porta da rua, de 3,35 a 4,55 na parede x = 0, com a moldura até 2,25.
        assertEquals(PontoDaPlanta(0f, 3.95f, 0f), CamadasDaCasaPorDentro.referenciaDaPorta)
        for (cor in 0..4) {
            val porta = CamadasDaCasaPorDentro.lugar(info("dentro_porta_$cor"), manifesto.escala, l, 0f, 0f, CamadasDaCasaPorDentro.referenciaDaPorta)
            cobre("porta $cor", porta, caixaNaTela(cantos(0f, 3.27f, 0f, 4.63f, 0f, 2.25f)), emCima = true)
        }
        // Cada pedaço da parede interna.
        for (a in Andar.entries) for (c in PlantaDaCasa.paredesInternas(a)) {
            val lugar = CamadasDaCasaPorDentro.lugar(
                info(CamadasDaCasaPorDentro.PAREDE_INTERNA), manifesto.escala, l, 0f, 0f,
                CamadasDaCasaPorDentro.referenciaDaParedeInterna(c.x0, c.y0, c.x1, c.y1),
            )
            cobre("parede interna $a ${c.y0}", lugar, caixaNaTela(cantos(c.x0, c.y0, c.x1, c.y1, c.z0, c.z1)), emCima = true)
        }
    }

    @Test
    fun `lampada pendurada no ponto do app e ancoras dentro da imagem`() {
        for (acesa in listOf(true, false)) {
            val i = info(CamadasDaCasaPorDentro.idDaLampada(acesa))
            // O apoio é a própria lâmpada (a âncora "luz"): o toque de acender continua no mesmo ponto.
            assertEquals(i.apoio, i.ancoras["luz"])
            val lugar = CamadasDaCasaPorDentro.lugar(i, manifesto.escala, l, 0f, 0f, CamadasDaCasaPorDentro.luzDo(Comodo.SALA))
            val luz = CamadasDaCasaPorDentro.ancora(i, lugar, "luz")!!
            val ref = CamadasDaCasaPorDentro.luzDo(Comodo.SALA)
            assertEquals(GradeIsometrica.x(l, 0f, ref.x, ref.y), luz.first, 1e-3f)
            assertEquals(GradeIsometrica.y(l, 0f, ref.x, ref.y, ref.z), luz.second, 1e-3f)
        }
        assertEquals(PontoDaPlanta(7.6f, 2.0f, 2.85f), CamadasDaCasaPorDentro.luzDo(Comodo.BANHEIRO))
        // Pontos usados pelas animações que continuam no app.
        for ((id, nomes) in listOf(
            "dentro_geladeira_aberta" to (0..8).map { "comida_$it" },
            "dentro_guardaroupa_aberto" to (0..5).map { "cabide_$it" },
            "dentro_pia" to listOf("bico", "bacia"),
            "dentro_banheira" to listOf("agua"),
        )) {
            val i = info(id)
            for (n in nomes) {
                val a = i.ancoras[n]
                assertNotNull("$id sem a âncora $n", a)
                assertTrue("$id.$n fora da imagem", a!!.x in 0..i.largura && a.y in 0..i.altura)
            }
        }
        assertNull(CamadasDaCasaPorDentro.ancora(info("dentro_sofa"), LugarDaArte(0f, 0f, 1f, 1f), "luz"))
    }

    @Test
    fun `fundo do andar na ordem do manifesto, com as variantes do estado`() {
        val e = EstadoJogo(estrelas = Trilha.entries.flatMap { t -> (0 until 10).map { "${t.name}:$it" to 3 } }.toMap()).let { tudo -> br.com.casinhadosaber.dominio.ItensDaCasa.ordem.fold(tudo) { acc, item -> acc.colocar(item) } }
        for (a in Andar.entries) for (noite in listOf(false, true)) {
            val moveis = PlantaDaCasa.moveisDe(e, a)
            val ap = AparenciaDoAndar.de(e.copy(corPorta = 3, papelDeParede = 2), a, moveis, noite)
            val cascas = CamadasDaCasaPorDentro.cascas(ap)
            // laje, pisos, (tapete), paredes, papel, janelas, (porta), acabamento
            val ordens = cascas.mapNotNull { c -> c.id?.let { info(it).numeroIso("ordem") } }
            assertEquals("ordem de desenho do andar $a", ordens.sorted(), ordens)
            assertEquals(GrupoDaCasca.LAJE, cascas.first().grupo)
            assertEquals(GrupoDaCasca.ACABAMENTO, cascas.last().grupo)
            assertTrue(cascas.any { it.grupo == GrupoDaCasca.PAPEL && it.id == null })
            val janelas = cascas.filter { it.grupo == GrupoDaCasca.JANELA }
            // de cima, a reta do quarto e a redonda do banheiro; de baixo, as duas retas
            assertEquals(2, janelas.size)
            assertTrue(janelas.all { (it.id!!.endsWith("_noite")) == noite })
            if (a == Andar.DE_BAIXO) {
                assertEquals("dentro_porta_3", cascas.single { it.grupo == GrupoDaCasca.PORTA }.id)
                assertNotNull(cascas.singleOrNull { it.grupo == GrupoDaCasca.TAPETE })
            } else {
                assertTrue(cascas.none { it.grupo == GrupoDaCasca.PORTA || it.grupo == GrupoDaCasca.TAPETE })
            }
            // Paredes tingidas com a cor de cada cômodo; o resto como está.
            for (c in cascas) {
                if (c.grupo == GrupoDaCasca.PAREDE) assertEquals(e.corDo(c.comodo!!), c.cor) else assertNull(c.cor)
            }
            for (c in cascas) c.id?.let { id -> info(id).textoIso("andar")?.let { assertEquals(CamadasDaCasaPorDentro.nomeDoAndar(a), it) } }
        }
        // Sem janelas nem porta (casa no começo): só laje, pisos, paredes e acabamento.
        val inicio = AparenciaDoAndar.de(EstadoJogo(), Andar.DE_BAIXO, PlantaDaCasa.moveisDe(EstadoJogo(), Andar.DE_BAIXO))
        assertEquals(
            listOf(GrupoDaCasca.LAJE, GrupoDaCasca.PISO, GrupoDaCasca.PISO, GrupoDaCasca.TAPETE, GrupoDaCasca.PAREDE, GrupoDaCasca.PAREDE, GrupoDaCasca.ACABAMENTO),
            CamadasDaCasaPorDentro.cascas(inicio).map { it.grupo },
        )
    }

    private fun Comodo.andarDaPlanta(): Andar = if (this == Comodo.QUARTO || this == Comodo.BANHEIRO) Andar.DE_CIMA else Andar.DE_BAIXO
}
