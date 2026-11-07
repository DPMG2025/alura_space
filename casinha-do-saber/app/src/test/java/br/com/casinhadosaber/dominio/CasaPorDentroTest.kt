package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs

class CasaPorDentroTest {

    private val tudo: EstadoJogo = EstadoJogo(estrelas = Trilha.entries.flatMap { t -> (0 until 10).map { "${t.name}:$it" to 3 } }.toMap())

    private fun mapa(e: EstadoJogo, a: Andar) = Mapa(PlantaDaCasa.moveisDe(e), a)

    @Test
    fun `os andares sao os mesmos da casa de fora`() {
        assertEquals(listOf(Comodo.QUARTO, Comodo.BANHEIRO), PlantaDaCasa.comodosDo(Andar.DE_CIMA))
        assertEquals(listOf(Comodo.SALA, Comodo.COZINHA), PlantaDaCasa.comodosDo(Andar.DE_BAIXO))
        for (c in Comodo.entries) {
            val l = PlantaDaCasa.limitesDo(c)
            val centro = PlantaDaCasa.centroDo(c)
            assertEquals(c, PlantaDaCasa.comodoEm(c.andar, centro.x, centro.y))
            assertTrue(l.x1 > l.x0 && l.y1 > l.y0)
        }
        // Como na fachada: a sala é mais larga que a cozinha; o quarto e o banheiro dividem o andar ao meio.
        assertTrue(PlantaDaCasa.meio(Andar.DE_BAIXO) > PlantaDaCasa.LARGURA / 2)
        assertEquals(PlantaDaCasa.LARGURA / 2, PlantaDaCasa.meio(Andar.DE_CIMA))
        assertEquals(Andar.DE_CIMA, Andar.DE_BAIXO.outro)
    }

    @Test
    fun `moveis de dentro aparecem no comodo e no andar certos`() {
        for (m in PlantaDaCasa.moveis) {
            val c = PlantaDaCasa.limitesDo(m.comodo)
            assertTrue("${m.id} fora do cômodo", m.x0 >= c.x0 && m.x1 <= c.x1 && m.y0 >= c.y0 && m.y1 <= c.y1)
            assertTrue("${m.id} ao contrário", m.x1 > m.x0 && m.y1 > m.y0)
            val certo = m.itemId?.let { ItensDaCasa.comodoDe(it) }
            if (certo != null) assertEquals(m.id, certo, m.comodo)
            if (m.itemId != null) assertTrue("${m.id} mora lá fora", ItensDaCasa.porId.getValue(m.itemId!!).lugar != Lugar.FORA)
        }
        // Todo móvel "de dentro" da casa ganha lugar aqui.
        for (i in ItensDaCasa.ordem.filter { it.lugar == Lugar.DENTRO }) assertTrue(i.id, i.id in PlantaDaCasa.pecasDeDentro)
        // Cama e banheiro em cima; sofá, geladeira e fogão embaixo (igual à fachada).
        for (id in listOf("cama", "banheira", "espelho", "guardaroupa")) assertEquals(id, Andar.DE_CIMA, PlantaDaCasa.porId.getValue(id).andar)
        for (id in listOf("sofa", "tv", "geladeira", "fogao", "mesa")) assertEquals(id, Andar.DE_BAIXO, PlantaDaCasa.porId.getValue(id).andar)
    }

    @Test
    fun `moveis grandes ficam encostados na parede do fundo do seu comodo`() {
        val noFundo = listOf("geladeira", "fogao", "sofa", "tv", "cama", "pia", "banheira", "criado")
        for (id in noFundo) {
            val m = PlantaDaCasa.porId.getValue(id)
            assertTrue("$id longe da parede", m.y0 <= 0.15f)
            assertEquals(id, Frente.Y, m.frente)
        }
        // A geladeira fica no canto da cozinha, encostada na parede, não solta no meio.
        val geladeira = PlantaDaCasa.porId.getValue("geladeira")
        assertEquals(Comodo.COZINHA, PlantaDaCasa.comodoEm(Andar.DE_BAIXO, geladeira.centroX, geladeira.centroY))
        assertTrue(geladeira.x1 >= PlantaDaCasa.LARGURA - 0.15f)
        // O guarda-roupa encosta na outra parede do fundo (x = 0) e abre para a frente dele.
        val gr = PlantaDaCasa.porId.getValue("guardaroupa")
        assertTrue(gr.x0 <= 0.15f)
        assertEquals(Frente.X, gr.frente)
    }

    @Test
    fun `moveis do mesmo andar nao se atravessam`() {
        val solidos = PlantaDaCasa.moveis.filter { it.bloqueia && !it.naParede && !it.noChao }
        for (a in solidos) for (b in solidos) {
            if (a.id >= b.id || a.andar != b.andar) continue
            val cruza = a.x0 < b.x1 && b.x0 < a.x1 && a.y0 < b.y1 && b.y0 < a.y1
            assertFalse("${a.id} e ${b.id} se atravessam", cruza)
        }
    }

    @Test
    fun `cada andar tem sua escada e o pe da escada esta livre`() {
        for (a in Andar.entries) {
            val escada = PlantaDaCasa.escadaDo(a)
            assertEquals(Acao.ESCADA, escada.acao)
            assertEquals(a, escada.andar)
            assertTrue(escada.fixo)
            for (e in listOf(EstadoJogo(), tudo)) {
                val m = mapa(e, a)
                val pe = PlantaDaCasa.peDaEscada
                assertTrue("pé da escada ocupado em $a", m.livre(pe.x.toInt(), pe.y.toInt()))
            }
        }
        // As duas escadas ficam no mesmo lugar da planta (uma em cima da outra).
        val baixo = PlantaDaCasa.escadaDo(Andar.DE_BAIXO)
        val cima = PlantaDaCasa.escadaDo(Andar.DE_CIMA)
        assertEquals(baixo.x0, cima.x0, 0f)
        assertEquals(baixo.y1, cima.y1, 0f)
    }

    @Test
    fun `so aparecem as pecas ja ganhas`() {
        val nada = PlantaDaCasa.moveisDe(EstadoJogo())
        assertTrue(nada.all { it.fixo })
        assertTrue(nada.any { it.acao == Acao.GUARDA_ROUPA })
        assertEquals(2, nada.count { it.acao == Acao.ESCADA })
        val todos = PlantaDaCasa.moveisDe(tudo)
        assertEquals(PlantaDaCasa.moveis.size, todos.size)
        assertTrue(todos.any { it.acao == Acao.GELADEIRA })
        for (a in Andar.entries) assertTrue(PlantaDaCasa.moveisDe(tudo, a).all { it.andar == a })
    }

    @Test
    fun `a crianca alcanca os dois comodos do andar pela passagem`() {
        for (e in listOf(EstadoJogo(), tudo)) for (a in Andar.entries) {
            val m = mapa(e, a)
            val inicio = if (a == Andar.DE_BAIXO) PlantaDaCasa.entrada else PlantaDaCasa.peDaEscada
            assertTrue("início ocupado", m.livre(inicio.x.toInt(), inicio.y.toInt()))
            val alcance = m.alcancaveis(inicio)
            for (y in 0 until PlantaDaCasa.FUNDO) for (x in 0 until PlantaDaCasa.LARGURA) {
                if (m.livre(x, y)) assertTrue("ladrilho ($x,$y) isolado em $a", (x to y) in alcance)
            }
            for (c in PlantaDaCasa.comodosDo(a)) assertTrue(c.name, alcance.any { (x, y) -> PlantaDaCasa.comodoEm(a, x + 0.5f, y + 0.5f) == c })
        }
    }

    @Test
    fun `caminho nao atravessa paredes nem moveis`() {
        for (a in Andar.entries) {
            val m = mapa(tudo, a)
            val livres = (0 until PlantaDaCasa.FUNDO).flatMap { y -> (0 until PlantaDaCasa.LARGURA).map { x -> x to y } }.filter { (x, y) -> m.livre(x, y) }
            for ((ax, ay) in livres) for ((bx, by) in livres) {
                val de = Posicao(ax + 0.5f, ay + 0.5f)
                val para = Posicao(bx + 0.5f, by + 0.5f)
                val caminho = m.caminho(de, para)
                if (ax == bx && ay == by) {
                    assertTrue(caminho.isEmpty())
                    continue
                }
                assertTrue("sem caminho de ($ax,$ay) a ($bx,$by) em $a", caminho.isNotEmpty())
                assertEquals(para, caminho.last())
                var atual = de
                for (p in caminho) {
                    assertTrue("atravessou de $atual a $p", m.linhaLivre(atual, p))
                    atual = p
                }
            }
        }
    }

    @Test
    fun `da sala para a cozinha passa pela passagem`() {
        val m = mapa(tudo, Andar.DE_BAIXO)
        val meio = PlantaDaCasa.meio(Andar.DE_BAIXO)
        // Reto, da sala (meio-1, 1) para a cozinha (meio, 1), atravessaria a parede: a passagem fica na frente.
        val de = Posicao(meio - 0.5f, 1.5f)
        val para = Posicao(meio + 0.5f, 1.5f)
        assertFalse(m.linhaLivre(de, para))
        val caminho = m.caminho(de, para)
        assertTrue(caminho.size >= 2)
        assertTrue(caminho.any { it.y >= PlantaDaCasa.portasNaParedeInterna.min() })
    }

    @Test
    fun `tocar num movel leva para o ladrilho livre mais perto do mesmo lado da parede`() {
        val m = mapa(tudo, Andar.DE_BAIXO)
        val geladeira = PlantaDaCasa.porId.getValue("geladeira")
        val perto = m.pertoDo(geladeira)
        assertEquals(Comodo.COZINHA, PlantaDaCasa.comodoEm(Andar.DE_BAIXO, perto.x, perto.y))
        assertTrue(m.livre(perto.x.toInt(), perto.y.toInt()))
        // Tocar em cima da cama: vai para perto dela, no quarto.
        val cima = mapa(tudo, Andar.DE_CIMA)
        val fim = cima.caminho(PlantaDaCasa.peDaEscada, Posicao(1.8f, 1.0f)).last()
        assertTrue(cima.livre(fim.x.toInt(), fim.y.toInt()))
        assertEquals(Comodo.QUARTO, PlantaDaCasa.comodoEm(Andar.DE_CIMA, fim.x, fim.y))
    }

    @Test
    fun `ordem de desenho poe o que esta atras primeiro`() {
        for (a in Andar.entries) {
            val m = PlantaDaCasa.moveisDe(tudo, a).filter { !it.naParede && !it.noChao }
            val caixas = m.map { PlantaDaCasa.caixaDo(it) } + PlantaDaCasa.paredesInternas(a)
            val ordem = PlantaDaCasa.ordenarParaDesenho(caixas)
            assertEquals(caixas.size, ordem.toSet().size)
            val posicao = IntArray(caixas.size)
            ordem.forEachIndexed { k, i -> posicao[i] = k }
            for (i in caixas.indices) for (j in caixas.indices) {
                if (i != j && PlantaDaCasa.atras(caixas[i], caixas[j]) && !PlantaDaCasa.atras(caixas[j], caixas[i])) {
                    assertTrue("${caixas[i]} deveria vir antes de ${caixas[j]}", posicao[i] < posicao[j])
                }
            }
            fun idx(id: String) = posicao[m.indexOfFirst { it.id == id }]
            if (a == Andar.DE_CIMA) assertTrue(idx("ursinho") > idx("cama")) else assertTrue(idx("bolo") > idx("mesa"))
        }
    }

    @Test
    fun `crianca fica na frente do que esta atras dela`() {
        val cama = PlantaDaCasa.caixaDo(PlantaDaCasa.porId.getValue("cama"))
        assertTrue(PlantaDaCasa.criancaNaFrente(3.0f, 1.0f, cama))
        assertTrue(PlantaDaCasa.criancaNaFrente(1.5f, 2.5f, cama))
        val mesa = PlantaDaCasa.caixaDo(PlantaDaCasa.porId.getValue("mesa"))
        assertFalse(PlantaDaCasa.criancaNaFrente(5.5f, 1.5f, mesa))
    }

    @Test
    fun `a planta cabe na largura da tela e os andares empilhados nao se encostam`() {
        val enq = PlantaDaCasa.enquadramento()
        // Celular de 360 e 393 dp (menos 16 dp de margem de cada lado) e tablet.
        for ((w, h) in listOf(328f to 560f, 361f to 700f, 608f to 900f, 328f to 300f)) {
            val dois = PlantaDaCasa.mostrarDoisAndares(w, h, enq)
            val tw = PlantaDaCasa.ladrilhoQueCabe(w, h, dois, enq)
            assertTrue("larga demais em $w", tw * enq.largura <= w + 0.01f)
            assertTrue("alta demais em $h", tw * (enq.altura + if (dois) enq.entreAndares else 0f) <= h + 0.01f)
        }
        // Celular comum: os dois andares aparecem juntos. Painel grande aberto: um andar só.
        assertTrue(PlantaDaCasa.mostrarDoisAndares(361f, 700f, enq))
        assertFalse(PlantaDaCasa.mostrarDoisAndares(361f, 340f, enq))
        // Entre os andares cabe o alto da parede de baixo e a espessura do piso de cima.
        val z = 0.56f
        assertTrue(enq.entreAndares > PlantaDaCasa.FUNDO / 2f + PlantaDaCasa.ALTURA_PAREDE * z)
    }

    @Test
    fun `cada comida mora numa prateleira da geladeira`() {
        val comidas = Loja.produtos.filter { it.categoria == Categoria.COMIDA }
        for (c in comidas) {
            val dono = PrateleiraDaGeladeira.entries.filter { c.valor in it.emojis }
            assertEquals(c.nome, 1, dono.size)
        }
        for (p in PrateleiraDaGeladeira.entries) assertTrue(p.name, PrateleiraDaGeladeira.filtrar(comidas, p).isNotEmpty())
        assertEquals(comidas.size, PrateleiraDaGeladeira.entries.sumOf { PrateleiraDaGeladeira.filtrar(comidas, it).size })
        assertEquals(PrateleiraDaGeladeira.FRUTAS, PrateleiraDaGeladeira.inicial(emptyList()))
        val soLeite = comidas.filter { it.valor == "🥛" }
        assertEquals(PrateleiraDaGeladeira.LEITE, PrateleiraDaGeladeira.inicial(soLeite))
    }

    // ---------------------------------------------------------------- arrastar móveis

    private fun daLista(l: List<Movel>, id: String) = l.first { it.id == id }

    @Test
    fun `o lugar de sempre de cada movel e valido para arrastar`() {
        for (a in Andar.entries) {
            val m = PlantaDaCasa.moveisDe(tudo, a)
            for (x in m) if (PlantaDaCasa.arrastavel(x)) assertTrue("${x.id} fora do lugar", PlantaDaCasa.cabeNoLugar(m, x))
            assertTrue(PlantaDaCasa.tudoAlcancavel(m, a))
        }
        // Escada, guarda-roupa, pia e vaso ficam presos; enfeites vão com o móvel de baixo.
        for (id in listOf("escada", "escadaDeCima", "guardaroupa", "pia", "vaso", "quadro", "espelho", "tapete")) {
            assertFalse(id, PlantaDaCasa.arrastavel(PlantaDaCasa.porId.getValue(id)))
        }
        assertEquals("sofa", PlantaDaCasa.quemArrasta(PlantaDaCasa.porId.getValue("gato")))
        assertEquals("criado", PlantaDaCasa.quemArrasta(PlantaDaCasa.porId.getValue("abajur")))
        assertEquals(null, PlantaDaCasa.quemArrasta(PlantaDaCasa.porId.getValue("pia")))
    }

    @Test
    fun `mover para um ladrilho livre funciona e leva o que esta em cima`() {
        val antes = PlantaDaCasa.moveisDe(tudo)
        val depois = PlantaDaCasa.mover(antes, "mesa", -1, 0)
        // A mesa da cozinha não pode ir para a sala atravessando a parede...
        assertEquals(null, depois)
        // ...nem tapar a passagem entre os cômodos (logo na frente dela)...
        assertEquals(null, PlantaDaCasa.mover(antes, "mesa", 0, 1))
        // ...mas pode ir um ladrilho para a direita, com o bolo junto.
        val frente = PlantaDaCasa.mover(antes, "mesa", 1, 0)!!
        val mesa = daLista(frente, "mesa")
        val bolo = daLista(frente, "bolo")
        assertEquals(daLista(antes, "mesa").x0 + 1, mesa.x0, 0.0001f)
        assertEquals(daLista(antes, "bolo").x0 + 1, bolo.x0, 0.0001f)
        assertEquals(Comodo.COZINHA, mesa.comodo)
        // O caminho é recalculado: o ladrilho novo fica ocupado e o velho, livre.
        val mapa = Mapa(frente, Andar.DE_BAIXO)
        assertFalse(mapa.livre(7, 2))
        assertTrue(mapa.livre(5, 2))
        assertFalse(Mapa(antes, Andar.DE_BAIXO).livre(5, 2))
        assertTrue(PlantaDaCasa.tudoAlcancavel(frente, Andar.DE_BAIXO))
        // O violão vai do quarto para o banheiro pela grade e muda de cômodo.
        val violao = PlantaDaCasa.mover(antes, "violao", 1, 0)!!.let { daLista(it, "violao") }
        assertEquals(Comodo.BANHEIRO, violao.comodo)
    }

    @Test
    fun `mover para lugar proibido nao vale`() {
        val m = PlantaDaCasa.moveisDe(tudo)
        // Em cima de outro móvel (sofá na TV).
        assertEquals(null, PlantaDaCasa.mover(m, "sofa", 2, 0))
        // Para fora do piso.
        assertEquals(null, PlantaDaCasa.mover(m, "geladeira", 1, 0))
        assertEquals(null, PlantaDaCasa.mover(m, "livros", 0, -1))
        // Atravessando a parede interna (a TV fica metade na sala, metade na cozinha).
        assertEquals(null, PlantaDaCasa.mover(m, "tv", 1, 0))
        // Tapando o pé da escada, a porta da rua ou a passagem entre os cômodos.
        val livros = daLista(m, "livros")
        for ((x, y) in PlantaDaCasa.reservados(Andar.DE_BAIXO)) {
            val dx = x - floorInt(livros.x0)
            val dy = y - floorInt(livros.y0)
            assertEquals("livros em ($x,$y)", null, PlantaDaCasa.mover(m, "livros", dx, dy))
        }
        // Presos não saem do lugar.
        assertEquals(null, PlantaDaCasa.mover(m, "pia", 0, 1))
        assertEquals(null, PlantaDaCasa.mover(m, "escada", 1, 0))
        // Nada pode deixar um canto da casa sem caminho: nenhuma mudança válida isola um ladrilho.
        for (a in Andar.entries) for (x in PlantaDaCasa.moveisDe(tudo, a)) if (PlantaDaCasa.arrastavel(x)) {
            for (dx in -7..7) for (dy in -4..4) {
                val r = PlantaDaCasa.mover(m, x.id, dx, dy) ?: continue
                assertTrue("${x.id} $dx,$dy", PlantaDaCasa.tudoAlcancavel(r, a))
                val novo = daLista(r, x.id)
                assertTrue(PlantaDaCasa.ladrilhosDe(novo).none { it in PlantaDaCasa.reservados(a) })
            }
        }
    }

    private fun floorInt(v: Float) = kotlin.math.floor(v).toInt()

    @Test
    fun `o lugar novo fica salvo e saves antigos continuam iguais`() {
        // Save antigo: sem lugares, tudo no lugar de sempre.
        assertEquals(PlantaDaCasa.moveis.filter { it.id in PlantaDaCasa.moveisDe(tudo).map { m -> m.id } }, PlantaDaCasa.moveisDe(tudo))
        // Um ladrilho só para a frente deixaria o cantinho atrás do sofá sem caminho: dois, pode.
        assertEquals(null, PlantaDaCasa.mover(PlantaDaCasa.moveisDe(tudo), "sofa", 0, 1))
        val movido = PlantaDaCasa.mover(PlantaDaCasa.moveisDe(tudo), "sofa", 0, 2)!!.let { daLista(it, "sofa") }
        val e = tudo.comMovelEm("sofa", movido.x0, movido.y0)
        assertEquals(1, e.moveisDentro.size)
        val texto = Serializacao.posicoesParaTexto(e.moveisDentro)
        val lido = tudo.copy(moveisDentro = Serializacao.textoParaMoveisDentro(texto))
        val sofa = daLista(PlantaDaCasa.moveisDe(lido), "sofa")
        assertEquals(movido.y0, sofa.y0, 0.0001f)
        // O gato vai junto com o sofá.
        assertEquals(PlantaDaCasa.porId.getValue("gato").y0 + 2, daLista(PlantaDaCasa.moveisDe(lido), "gato").y0, 0.0001f)
        // Voltar para o lugar de sempre apaga o registro.
        val padrao = PlantaDaCasa.porId.getValue("sofa")
        assertTrue(e.comMovelEm("sofa", padrao.x0, padrao.y0).moveisDentro.isEmpty())
        // Texto estragado, id desconhecido ou lugar que não vale mais: ignorado, móvel no lugar de sempre.
        assertTrue(Serializacao.textoParaMoveisDentro("xyz;foguete,1,2;sofa,a,b").isEmpty())
        assertTrue(Serializacao.textoParaMoveisDentro(null).isEmpty())
        val ruim = tudo.copy(moveisDentro = mapOf("sofa" to Posicao(padrao.x0 + 2, padrao.y0)))
        assertEquals(padrao.x0, daLista(PlantaDaCasa.moveisDe(ruim), "sofa").x0, 0.0001f)
    }

    @Test
    fun `peca nova que chega onde um movel foi posto manda o movel de volta`() {
        // Sem o fogão ainda, a lixeira vai para o lugar dele; quando o fogão chega, a lixeira volta.
        val m = PlantaDaCasa.moveisDe(EstadoJogo())
        assertTrue(m.none { it.id == "fogao" })
        val r = PlantaDaCasa.mover(m, "lixeira", 1, 0)!!
        val l = daLista(r, "lixeira")
        val salvo = tudo.copy(moveisDentro = mapOf("lixeira" to Posicao(l.x0, l.y0)))
        assertEquals(l.x0, daLista(PlantaDaCasa.moveisDe(EstadoJogo(moveisDentro = salvo.moveisDentro)), "lixeira").x0, 0.0001f)
        val todos = PlantaDaCasa.moveisDe(salvo, Andar.DE_BAIXO)
        assertEquals(PlantaDaCasa.porId.getValue("lixeira").x0, daLista(todos, "lixeira").x0, 0.0001f)
        assertTrue(todos.filter { PlantaDaCasa.arrastavel(it) }.all { PlantaDaCasa.cabeNoLugar(todos, it) })
    }

    @Test
    fun `o violao nao encosta no abajur e o patinho so vem com a banheira`() {
        val violao = PlantaDaCasa.porId.getValue("violao")
        val abajur = PlantaDaCasa.porId.getValue("abajur")
        // Na tela (x - y), o violão fica bem longe do abajur, ou bem mais na frente (x + y).
        val longeNaTela = abs((violao.centroX - violao.centroY) - (abajur.centroX - abajur.centroY)) > 1f
        val naFrente = (violao.centroX + violao.centroY) - (abajur.centroX + abajur.centroY) > 1.5f
        assertTrue(longeNaTela || naFrente)
        assertTrue(PlantaDaCasa.moveisDe(EstadoJogo()).none { it.id == "patinho" })
        assertTrue(PlantaDaCasa.moveisDe(tudo).any { it.id == "patinho" })
        assertTrue(PlantaDaCasa.moveisDe(EstadoJogo()).any { it.id == "lixeira" })
    }

    @Test
    fun `a dica de arrastar acha um movel que cabe nos dois andares`() {
        for (e in listOf(tudo, EstadoJogo())) for (andar in Andar.entries) {
            val moveis = PlantaDaCasa.moveisDe(e)
            val temArrastavel = moveis.any { it.andar == andar && PlantaDaCasa.arrastavel(it) }
            val s = PlantaDaCasa.sugestaoDeArraste(moveis, andar, 0 to 4)
            if (temArrastavel) {
                println("dica $andar: ${s?.first?.id} ${s?.second}")
                val (m, passo) = s!!
                assertTrue(PlantaDaCasa.mover(moveis, m.id, passo.first, passo.second) != null)
            }
        }
    }
}

class OcultacaoNaCasaTest {

    private val tudo: EstadoJogo = EstadoJogo(estrelas = Trilha.entries.flatMap { t -> (0 until 10).map { "${t.name}:$it" to 3 } }.toMap())

    // Mesma projeção de ui/componentes/Isometrico.kt, com ladrilho = 1.
    private fun projetar(x: Float, y: Float, z: Float) = Pair((x - y) * 0.5, (x + y) * 0.25 - z * 0.56)

    /** O contorno da caixa na tela: o fecho convexo dos 8 cantos (um hexágono). */
    private fun silhueta(c: Caixa): List<Pair<Double, Double>> {
        val p = ArrayList<Pair<Double, Double>>()
        for (x in listOf(c.x0, c.x1)) for (y in listOf(c.y0, c.y1)) for (z in listOf(c.z0, c.z1)) p += projetar(x, y, z)
        val s = p.distinct().sortedWith(compareBy({ it.first }, { it.second }))
        fun giro(o: Pair<Double, Double>, a: Pair<Double, Double>, b: Pair<Double, Double>) =
            (a.first - o.first) * (b.second - o.second) - (a.second - o.second) * (b.first - o.first)
        val baixo = ArrayList<Pair<Double, Double>>()
        for (q in s) { while (baixo.size >= 2 && giro(baixo[baixo.size - 2], baixo.last(), q) <= 0) baixo.removeAt(baixo.size - 1); baixo += q }
        val cima = ArrayList<Pair<Double, Double>>()
        for (q in s.reversed()) { while (cima.size >= 2 && giro(cima[cima.size - 2], cima.last(), q) <= 0) cima.removeAt(cima.size - 1); cima += q }
        return baixo.dropLast(1) + cima.dropLast(1)
    }

    private fun area(p: List<Pair<Double, Double>>): Double {
        var s = 0.0
        for (i in p.indices) { val a = p[i]; val b = p[(i + 1) % p.size]; s += a.first * b.second - b.first * a.second }
        return kotlin.math.abs(s) / 2
    }

    /** Recorta o polígono [alvo] pelo polígono convexo [faca] (Sutherland-Hodgman). */
    private fun recortar(alvo: List<Pair<Double, Double>>, faca: List<Pair<Double, Double>>): List<Pair<Double, Double>> {
        var saida = alvo
        val sentido = if (faca.indices.sumOf { i -> val a = faca[i]; val b = faca[(i + 1) % faca.size]; a.first * b.second - b.first * a.second } > 0) 1.0 else -1.0
        for (i in faca.indices) {
            if (saida.isEmpty()) break
            val a = faca[i]; val b = faca[(i + 1) % faca.size]
            fun dentro(p: Pair<Double, Double>) = sentido * ((b.first - a.first) * (p.second - a.second) - (b.second - a.second) * (p.first - a.first)) >= 0
            fun corte(p: Pair<Double, Double>, q: Pair<Double, Double>): Pair<Double, Double> {
                val d1 = (b.first - a.first) * (p.second - a.second) - (b.second - a.second) * (p.first - a.first)
                val d2 = (b.first - a.first) * (q.second - a.second) - (b.second - a.second) * (q.first - a.first)
                val t = d1 / (d1 - d2)
                return Pair(p.first + (q.first - p.first) * t, p.second + (q.second - p.second) * t)
            }
            val entrada = saida
            val nova = ArrayList<Pair<Double, Double>>()
            for (k in entrada.indices) {
                val p = entrada[k]; val q = entrada[(k + 1) % entrada.size]
                if (dentro(q)) { if (!dentro(p)) nova += corte(p, q); nova += q } else if (dentro(p)) nova += corte(p, q)
            }
            saida = nova
        }
        return saida
    }

    private val vizinhosNaCozinha = setOf("lixeira" to "fogao", "fogao" to "geladeira")

    /**
     * Na projeção isométrica, nenhum objeto fica escondido atrás de outro que é desenhado depois dele:
     * mede quanto do contorno de cada caixa na tela fica coberto pelo contorno de quem está na frente.
     */
    @Test
    fun `nenhum objeto fica escondido atras de outro`() {
        for (a in Andar.entries) {
            val m = PlantaDaCasa.moveisDe(tudo, a).filter { !it.naParede && !it.noChao }
            val caixas = m.map { PlantaDaCasa.caixaDo(it) }
            val ordem = PlantaDaCasa.ordenarParaDesenho(caixas)
            val posicao = IntArray(caixas.size)
            ordem.forEachIndexed { k, i -> posicao[i] = k }
            for (i in m.indices) {
                val atras = silhueta(caixas[i])
                val total = area(atras)
                var soma = 0.0
                for (j in m.indices) {
                    if (i == j || posicao[j] < posicao[i]) continue
                    // Enfeite em cima do móvel (o gato no sofá) cobre o móvel de propósito.
                    if (PlantaDaCasa.apoios[m[j].id] == m[i].id || PlantaDaCasa.apoios[m[i].id] == m[j].id) continue
                    val coberto = area(recortar(atras, silhueta(caixas[j]))) / total
                    // Na parede da cozinha, lixeira, fogão e geladeira ficam lado a lado: ali cada um
                    // tapa só a beirada do vizinho. Em todo o resto, quase nada fica atrás de outro.
                    val limite = if (m[i].id to m[j].id in vizinhosNaCozinha) 0.4 else 0.2
                    assertTrue("${m[i].id} fica ${(coberto * 100).toInt()}% atrás de ${m[j].id}", coberto < limite)
                    soma += coberto
                }
                assertTrue("${m[i].id} fica ${(soma * 100).toInt()}% escondido", soma < 0.5)
            }
        }
    }

    /**
     * Com a arte 3D (Fase 6) a ordem de desenho continua a mesma: cada imagem dentro_* foi encaixada na pegada do
     * seu móvel, então a caixa de oclusão da planta é a pegada da imagem (o teste acima vale para as imagens).
     */
    @Test
    fun `as imagens 3D dos moveis usam as mesmas caixas da oclusao`() {
        val pasta = listOf("src/main/assets/arte", "app/src/main/assets/arte").map(::File).first { it.isDirectory }
        val manifesto = br.com.casinhadosaber.ui.componentes.ManifestoDaArte.ler(File(pasta, "manifesto.json").readText())
        for (a in Andar.entries) for (m in PlantaDaCasa.moveisDe(tudo, a).filter { !it.naParede && !it.noChao }) {
            val id = br.com.casinhadosaber.ui.componentes.CamadasDaCasaPorDentro.idDoMovel(m)
            val info = manifesto.imagens[id] ?: error("falta $id")
            val c = PlantaDaCasa.caixaDo(m)
            for ((campo, valor) in listOf("x0" to c.x0, "y0" to c.y0, "x1" to c.x1, "y1" to c.y1, "z" to c.z0, "altura" to c.z1 - c.z0)) {
                assertEquals("$id: pegada.$campo", valor.toDouble(), info.numeroIso("pegada.$campo")!!, 1e-4)
            }
        }
    }
}
