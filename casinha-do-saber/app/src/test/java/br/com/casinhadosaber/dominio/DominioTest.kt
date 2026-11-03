package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GeradorDeFasesTest {

    @Test
    fun `cada trilha tem cinquenta fases`() {
        val g = GeradorDeFases(Random(1))
        for (t in Trilha.entries) assertEquals(GeradorDeFases.FASES_POR_TRILHA, g.fases(t).size)
    }

    @Test
    fun `fases suficientes para a casa inteira`() {
        assertTrue(Trilha.entries.size * GeradorDeFases.FASES_POR_TRILHA >= ItensDaCasa.total)
        assertEquals("ids repetidos", ItensDaCasa.ordem.size, ItensDaCasa.porId.size)
    }

    @Test
    fun `depois das quarenta pecas cada fase nova da um presente da loja`() {
        val todas = Trilha.entries.flatMap { t -> GeradorDeFases(Random(2)).fases(t) }
        var e = EstadoJogo()
        for (f in todas.take(ItensDaCasa.total)) e = e.registrarPartida(f, 5, 5).estado
        assertEquals(ItensDaCasa.total, e.itensGanhos.size)
        val r = e.registrarPartida(todas[ItensDaCasa.total], 5, 5)
        assertNull(r.item)
        assertNotNull(r.brinde)
        assertTrue(r.brinde!!.id in r.estado.comprados)
        assertTrue(r.estado.moedas >= e.moedas + EstadoJogo.MOEDAS_DO_BRINDE)
    }

    @Test
    fun `todas as fases geram perguntas validas em muitas partidas`() {
        for (semente in 0 until 60) {
            val g = GeradorDeFases(Random(semente))
            for (t in Trilha.entries) for (fase in g.fases(t)) {
                val perguntas = g.perguntas(fase)
                assertEquals("$fase", GeradorDeFases.PERGUNTAS_POR_FASE, perguntas.size)
                for (p in perguntas) validar(fase, p)
            }
        }
    }

    private fun validar(fase: Fase, p: Pergunta) {
        when (val a = p.atividade) {
            is Atividade.Cesta -> {
                assertTrue("$fase: cesta impossível", a.alvo in 1..a.certos.size)
                return
            }
            is Atividade.Memoria -> {
                assertTrue("$fase: memória com poucos pares", a.pares.size >= 2)
                assertEquals("$fase: cartas repetidas", a.pares.size, a.pares.map { it.first }.toSet().size)
                return
            }
            is Atividade.MontarPalavra -> {
                assertEquals("$fase: sílabas não formam a palavra", a.palavra, a.silabas.joinToString(""))
                assertTrue("$fase: extra igual a sílaba", a.extras.none { it in a.silabas })
                return
            }
            is Atividade.Tracar -> {
                assertTrue("$fase: letra sem traçado", a.letra.length == 1)
                return
            }
            Atividade.Escolha -> Unit
        }
        assertEquals("$fase: deve ter 4 opções (${p.instrucao})", 4, p.opcoes.size)
        assertTrue("$fase: sem fala", p.fala.isNotEmpty() && p.fala.all { it.texto.isNotBlank() })
        val certa = p.opcoes[p.indiceCorreto].visual
        // A resposta certa não pode aparecer de novo entre as erradas.
        val iguais = p.opcoes.count { it.visual == certa }
        assertEquals("$fase: resposta certa duplicada em ${p.instrucao} ${p.opcoes}", 1, iguais)
        if (p.instrucao != "Qual é diferente?") {
            // Fora da fase "o diferente", as opções devem ser todas distintas.
            assertEquals("$fase: opções repetidas ${p.opcoes}", p.opcoes.size, p.opcoes.map { it.visual }.toSet().size)
        }
        p.opcoes.forEach { o ->
            when (val v = o.visual) {
                is Visual.Texto -> assertTrue(v.texto.isNotBlank())
                is Visual.Emoji -> assertTrue(v.emoji.isNotBlank())
                is Visual.Cor -> Unit
                is Visual.Sombra -> assertTrue(v.emoji.isNotBlank())
                is Visual.Seta -> assertTrue(o.fala.isNotEmpty())
            }
        }
    }

    @Test
    fun `silabas erradas sao parecidas com a certa`() {
        val g = GeradorDeFases(Random(11))
        val fase = g.fases(Trilha.PORTUGUES)[11]
        repeat(40) {
            for (p in g.perguntas(fase).filter { it.instrucao == "Com que pedacinho começa?" }) {
                val textos = p.opcoes.map { (it.visual as Visual.Texto).texto }
                assertTrue("tamanhos diferentes: $textos", textos.all { it.length == 2 })
                val certa = textos[p.indiceCorreto]
                assertTrue("distratores longe demais: $textos", textos.count { it[0] == certa[0] || it[1] == certa[1] } >= 3)
            }
        }
    }

    @Test
    fun `mais ou menos sempre tem quantidades vizinhas`() {
        val g = GeradorDeFases(Random(5))
        val fase = g.fases(Trilha.MATEMATICA)[7]
        repeat(40) {
            for (p in g.perguntas(fase).filter { it.instrucao.startsWith("Onde tem") }) {
                val qtds = p.opcoes.map { (it.visual as Visual.Emoji).emoji.codePointCount(0, (it.visual as Visual.Emoji).emoji.length) }.sorted()
                assertTrue("sem vizinhos: $qtds", qtds.zipWithNext().any { (a, b) -> b - a == 1 })
            }
        }
    }

    @Test
    fun `contas tem resultado correto`() {
        val g = GeradorDeFases(Random(7))
        val fases = g.fases(Trilha.MATEMATICA)
        repeat(30) {
            for (n in listOf(5, 6, 7, 8)) for (p in g.perguntas(fases[n])) {
                val c = p.cena as? Cena.Conta ?: continue
                val esperado = if (c.soma) c.a + c.b else c.a - c.b
                assertEquals((p.opcoes[p.indiceCorreto].visual as Visual.Texto).texto, esperado.toString())
                assertTrue("resultado fora da faixa", esperado in 0..10)
            }
        }
    }

    @Test
    fun `contagem mostra a quantidade certa de figuras`() {
        val g = GeradorDeFases(Random(3))
        for (p in g.perguntas(g.fases(Trilha.MATEMATICA)[1])) {
            val cena = p.cena as? Cena.Emojis ?: continue
            val resposta = (p.opcoes[p.indiceCorreto].visual as Visual.Texto).texto.toInt()
            val emoji = Vocabulario.objetosParaContar.first { cena.texto.startsWith(it.emoji) }.emoji
            assertEquals(cena.texto, emoji.repeat(resposta))
        }
    }
}

class EstadoJogoTest {

    private val g = GeradorDeFases(Random(0))

    @Test
    fun `primeira vitoria da um item e repetir nao da outro`() {
        val fase = g.fases(Trilha.INGLES)[0]
        val (e1, item1) = EstadoJogo().registrarVitoria(fase, 2)
        assertEquals("terreno", item1?.id)
        val (e2, item2) = e1.registrarVitoria(fase, 3)
        assertNull(item2)
        assertEquals(3, e2.estrelasDa(fase))
        val (_, item3) = e2.registrarVitoria(g.fases(Trilha.LOGICA)[0], 1)
        assertEquals("paredes", item3?.id)
    }

    @Test
    fun `todo item tem lugar e moveis tem comodo valido`() {
        for (i in ItensDaCasa.ordem.filter { !it.ehEstrutura }) {
            if (i.comodo != null) assertEquals("${i.id} tem cômodo mas não mora dentro", true, i.lugar == Lugar.DENTRO || i.lugar == Lugar.QUALQUER)
        }
    }

    @Test
    fun `moedinhas vem de acertos de primeira com limite por dia`() {
        val fase = g.fases(Trilha.MATEMATICA)[0]
        var e = EstadoJogo()
        // Fase nova vale o dobro: 5 acertos = 10 moedas.
        e = e.registrarPartida(fase, 5, 5, dia = 100, resultados = List(5) { true }).estado
        assertEquals(10, e.moedas)
        repeat(10) { e = e.registrarPartida(fase, 5, 5, dia = 100, resultados = List(5) { true }).estado }
        assertEquals(EstadoJogo.MOEDAS_POR_DIA, e.moedas)
        // Fase já dominada (100% de primeira) vale metade no dia seguinte: 4 acertos = 2 moedas.
        e = e.registrarPartida(fase, 4, 5, dia = 101).estado
        assertEquals(EstadoJogo.MOEDAS_POR_DIA + 2, e.moedas)
    }

    @Test
    fun `loja compra com moedas e o que foi comprado continua seu`() {
        val telhado = Loja.porId.getValue("telhado1")
        var e = EstadoJogo(moedas = 6)
        e = e.usar(telhado)!!
        assertEquals(0, e.moedas)
        assertEquals(1, e.corTelhado)
        // Tocar de novo no que já é seu não cobra: volta ao padrão e depois usa outra vez.
        e = e.usar(telhado)!!
        assertEquals(0, e.corTelhado)
        e = e.usar(telhado)!!
        assertEquals(1, e.corTelhado)
        assertEquals(0, e.moedas)
        // O preço sobe a cada compra.
        val porta = Loja.porId.getValue("porta1")
        assertEquals(porta.precoBase + 1, Loja.preco(porta, e))
        assertNull("sem moedas", e.usar(porta))
    }

    @Test
    fun `historico guarda os ultimos dez resultados da fase`() {
        val fase = g.fases(Trilha.LOGICA)[2]
        var e = EstadoJogo()
        repeat(3) { e = e.registrarPartida(fase, 4, 5, resultados = listOf(true, true, false, true, true)).estado }
        val k = EstadoJogo.chave(fase.trilha, fase.numero)
        assertEquals(10, e.historico[k]?.length)
        assertEquals(80, e.precisaoDaFase(k))
    }

    @Test
    fun `missao do dia revisa as fases mais dificeis uma vez por dia`() {
        val fases = g.fases(Trilha.MATEMATICA)
        var e = EstadoJogo()
        assertFalse(e.missaoDisponivel(10))
        e = e.registrarPartida(fases[0], 5, 5, dia = 10, resultados = List(5) { true }).estado
        e = e.registrarPartida(fases[1], 4, 5, dia = 10, resultados = listOf(true, false, true, true, true)).estado
        assertTrue(e.missaoDisponivel(10))
        val revisar = e.fasesParaRevisar(Trilha.entries.flatMap { g.fases(it) })
        assertEquals(fases[1], revisar.first())
        assertEquals(3, g.perguntasDaMissao(revisar).size)
        val (depois, moedas) = e.concluirMissao(10, acertos = 3)
        assertEquals(EstadoJogo.MOEDAS_DA_MISSAO, moedas)
        assertFalse(depois.missaoDisponivel(10))
        assertTrue(depois.missaoDisponivel(11))
    }

    @Test
    fun `estrelas nunca pioram`() {
        val fase = g.fases(Trilha.MATEMATICA)[0]
        val (e1, _) = EstadoJogo().registrarVitoria(fase, 3)
        val (e2, _) = e1.registrarVitoria(fase, 1)
        assertEquals(3, e2.estrelasDa(fase))
    }

    @Test
    fun `fases abrem em ordem`() {
        val fases = g.fases(Trilha.PORTUGUES)
        val e0 = EstadoJogo()
        assertTrue(e0.liberada(fases[0]))
        assertFalse(e0.liberada(fases[1]))
        val (e1, _) = e0.registrarVitoria(fases[0], 3)
        assertTrue(e1.liberada(fases[1]))
        assertFalse(e1.liberada(fases[2]))
    }

    @Test
    fun `bau mostra itens ganhos que ainda nao foram colocados`() {
        var e = EstadoJogo()
        for (f in g.fases(Trilha.MATEMATICA).take(3)) e = e.registrarVitoria(f, 3).first
        assertEquals(listOf("terreno", "paredes", "telhado"), e.itensNoBau.map { it.id })
        e = e.colocar(ItensDaCasa.porId.getValue("paredes"))
        assertEquals(listOf("terreno", "telhado"), e.itensNoBau.map { it.id })
        assertTrue(e.temEstrutura(Estrutura.PAREDES))
        // Estruturas não voltam para o baú.
        assertEquals(e, e.guardar(ItensDaCasa.porId.getValue("paredes")))
    }

    @Test
    fun `pintar comodo troca de cor em ciclo`() {
        var e = EstadoJogo()
        val cores = Comodo.SALA.cores
        repeat(cores.size) { e = e.pintar(Comodo.SALA) }
        assertEquals(cores[0], e.corDo(Comodo.SALA))
        e = e.pintar(Comodo.SALA)
        assertEquals(cores[1], e.corDo(Comodo.SALA))
    }

    @Test
    fun `estrelas por acertos de primeira`() {
        assertEquals(3, EstadoJogo.estrelasPorAcertos(5, 5))
        assertEquals(2, EstadoJogo.estrelasPorAcertos(4, 5))
        assertEquals(0, EstadoJogo.estrelasPorAcertos(3, 5))
    }

    @Test
    fun `chutar nao vence a fase nem da presente`() {
        val fase = g.fases(Trilha.MATEMATICA)[0]
        val r = EstadoJogo().registrarPartida(fase, acertos = 3, total = 5)
        assertFalse(r.venceu)
        assertNull(r.item)
        assertEquals(0, r.estado.fasesVencidas)
        assertFalse(r.estado.liberada(g.fases(Trilha.MATEMATICA)[1]))
        // Mas a estatística para os pais é guardada.
        assertEquals(60, r.estado.precisao(Trilha.MATEMATICA))
        assertFalse(r.estado.facilitada(fase))
        // Duas derrotas seguidas deixam a fase mais fácil; vencer volta ao normal.
        val r1b = r.estado.registrarPartida(fase, acertos = 1, total = 5)
        assertTrue(r1b.estado.facilitada(fase))
        val r2 = r1b.estado.registrarPartida(fase, acertos = 4, total = 5)
        assertTrue(r2.venceu)
        assertEquals("terreno", r2.item?.id)
        assertFalse(r2.estado.facilitada(fase))
        assertEquals(53, r2.estado.precisao(Trilha.MATEMATICA))
    }
}

class SerializacaoTest {

    @Test
    fun `ida e volta preserva os dados`() {
        val estrelas = mapOf("MATEMATICA:0" to 3, "INGLES:4" to 1)
        assertEquals(estrelas, Serializacao.textoParaEstrelas(Serializacao.estrelasParaTexto(estrelas)))

        val pos = mapOf("cama" to Posicao(0.2f, 0.5f), "terreno" to Posicao(0.5f, 0.5f))
        assertEquals(pos, Serializacao.textoParaPosicoes(Serializacao.posicoesParaTexto(pos)))

        val cores = mapOf(Comodo.QUARTO to 2, Comodo.SALA to 4)
        assertEquals(cores, Serializacao.textoParaCores(Serializacao.coresParaTexto(cores)))
    }

    @Test
    fun `texto estragado nao derruba o app`() {
        assertTrue(Serializacao.textoParaEstrelas("lixo;;X:1=a;FOO:2=3").isEmpty())
        assertTrue(Serializacao.textoParaPosicoes(null).isEmpty())
        assertTrue(Serializacao.textoParaPosicoes("naoexiste,1,2;cama,a,b").isEmpty())
        assertNotNull(Serializacao.textoParaCores(""))
    }
}

class LojaDaCasaPorDentroTest {

    private val menina = Perfil("Ana", Genero.MENINA)

    @Test
    fun `comida comprada vai para a geladeira e continua la`() {
        val maca = Loja.produtos.first { it.categoria == Categoria.COMIDA }
        var e = EstadoJogo(perfil = menina, moedas = 50)
        assertTrue(Loja.naGeladeira(e).isEmpty())
        val preco = Loja.preco(maca, e)
        e = e.usar(maca)!!
        assertEquals(50 - preco, e.moedas)
        assertEquals(listOf(maca), Loja.naGeladeira(e))
        assertTrue(Loja.emUso(maca, e))
        // Tocar de novo não cobra nem tira da geladeira.
        val de_novo = e.usar(maca)!!
        assertEquals(e.moedas, de_novo.moedas)
        assertEquals(listOf(maca), Loja.naGeladeira(de_novo))
    }

    @Test
    fun `roupa comprada vai para o guarda roupa e a crianca veste`() {
        val capa = Loja.porId.getValue("roupaheroi")
        val abelha = Loja.porId.getValue("roupaabelha")
        var e = EstadoJogo(perfil = menina, moedas = 100)
        // Não dá para vestir o que não é seu.
        assertEquals("", e.vestir("heroi").perfil!!.traje)
        e = e.usar(capa)!!
        assertEquals("heroi", e.perfil!!.traje)
        assertEquals(listOf(capa), Loja.noGuardaRoupa(e))
        assertTrue(Loja.emUso(capa, e))
        // No guarda-roupa: tira e põe de novo, de graça.
        val moedas = e.moedas
        e = e.vestir("")
        assertEquals("", e.perfil!!.traje)
        e = e.vestir("heroi")
        assertEquals("heroi", e.perfil!!.traje)
        assertEquals(moedas, e.moedas)
        assertEquals("heroi", e.vestir("abelha").perfil!!.traje)
        e = e.usar(abelha)!!
        assertEquals("abelha", e.perfil!!.traje)
        assertEquals(2, Loja.noGuardaRoupa(e).size)
        // A roupa é desenhada por cima: a cor escolhida no perfil continua.
        assertEquals(menina.corDaRoupa, e.perfil!!.corDaRoupa)
    }

    @Test
    fun `experimentar antes de comprar nao muda o estado de verdade`() {
        val capa = Loja.porId.getValue("roupaheroi")
        val e = EstadoJogo(perfil = menina, moedas = 2)
        assertNull("sem moedas", e.usar(capa))
        val previa = e.copy(moedas = Int.MAX_VALUE / 2).usar(capa)!!
        assertEquals("heroi", previa.perfil!!.traje)
        assertEquals("", e.perfil!!.traje)
        assertTrue(capa.id !in e.comprados)
    }

    @Test
    fun `traje desconhecido vira roupa de sempre`() {
        assertNull(Loja.trajeDe(menina.copy(traje = "nao-existe")))
        assertNull(Loja.trajeDe(null))
        assertEquals("pijama", Loja.trajeDe(menina.copy(traje = "pijama"))?.id)
        // Dados antigos (sem o campo) usam o padrão vazio.
        assertEquals("", Perfil().traje)
    }

    @Test
    fun `ids da loja sao unicos e seguros para salvar`() {
        assertEquals(Loja.produtos.size, Loja.porId.size)
        for (p in Loja.produtos) assertFalse(p.id, p.id.any { it == ',' || it == ';' || it == '=' })
        for (p in Loja.produtos.filter { it.categoria == Categoria.COMIDA || it.categoria == Categoria.ROUPA }) {
            assertTrue("${p.id} sem nome em inglês", p.nomeEn.isNotBlank())
        }
    }

    @Test
    fun `vitrines de fora e de dentro separam as categorias`() {
        val e = EstadoJogo()
        val fora = Loja.vitrine(e, Loja.categoriasDeFora)
        val dentro = Loja.vitrine(e, Loja.categoriasDeDentro)
        assertTrue(fora.none { it.categoria == Categoria.COMIDA || it.categoria == Categoria.ROUPA })
        assertTrue(dentro.any { it.categoria == Categoria.COMIDA })
        assertTrue(dentro.any { it.categoria == Categoria.ROUPA })
        assertEquals(Loja.produtos.size, Loja.vitrine(e).size)
    }

    @Test
    fun `brinde funciona com comidas e roupas`() {
        // Tudo comprado, menos uma comida e uma roupa: o brinde vem delas.
        val g = GeradorDeFases(Random(3))
        val todas = Trilha.entries.flatMap { g.fases(it) }
        var e = EstadoJogo(perfil = menina)
        for (f in todas.take(ItensDaCasa.total)) e = e.registrarPartida(f, 5, 5).estado
        val comida = Loja.produtos.first { it.categoria == Categoria.COMIDA }
        val roupa = Loja.produtos.first { it.categoria == Categoria.ROUPA }
        e = e.copy(comprados = Loja.produtos.map { it.id }.toSet() - comida.id - roupa.id)
        val r1 = e.registrarPartida(todas[ItensDaCasa.total], 5, 5)
        assertEquals(comida, r1.brinde)
        assertTrue(comida in Loja.naGeladeira(r1.estado))
        assertTrue(r1.faseNova)
        val r2 = r1.estado.registrarPartida(todas[ItensDaCasa.total + 1], 5, 5)
        assertEquals(roupa, r2.brinde)
        assertTrue(roupa in Loja.noGuardaRoupa(r2.estado))
        assertEquals(roupa.valor, r2.estado.perfil!!.traje)
    }

    @Test
    fun `fase nova so na primeira vitoria`() {
        val fase = GeradorDeFases(Random(1)).fases(Trilha.LOGICA)[0]
        val perdeu = EstadoJogo().registrarPartida(fase, 2, 5)
        assertFalse(perdeu.faseNova)
        val venceu = perdeu.estado.registrarPartida(fase, 5, 5)
        assertTrue(venceu.faseNova)
        val repetiu = venceu.estado.registrarPartida(fase, 5, 5)
        assertTrue(repetiu.venceu)
        assertFalse(repetiu.faseNova)
    }
}

/** Regressão visual pelo domínio: as posições padrão da casa completa não podem amontoar objetos. */
class SobreposicaoDaCasaTest {

    @Test
    fun `objetos padrao da casa completa nao se sobrepoem`() {
        val achadas = ItensDaCasa.sobreposicoesPadrao(limite = 0.25f)
        assertTrue(
            "objetos amontoados nas posições padrão: " +
                achadas.joinToString { (a, b, f) -> "$a e $b (${(f * 100).toInt()}%)" },
            achadas.isEmpty(),
        )
    }

    @Test
    fun `o teste de sobreposicao enxerga um amontoado de verdade`() {
        // Sem isto o teste acima passaria à toa se a conta das caixas quebrasse.
        val cama = ItensDaCasa.porId.getValue("cama")
        val sofa = ItensDaCasa.porId.getValue("sofa")
        val f = ItensDaCasa.caixaNaCena(cama).sobreposicao(ItensDaCasa.caixaNaCena(sofa, Posicao(cama.x + 0.02f, cama.y)))
        assertTrue("cama e sofá no mesmo lugar deveriam se cobrir: $f", f > 0.5f)
        assertEquals(0f, ItensDaCasa.caixaNaCena(cama).sobreposicao(ItensDaCasa.caixaNaCena(sofa)), 0.0001f)
    }

    @Test
    fun `empilhados de proposito ficam mesmo um sobre o outro`() {
        for (par in ItensDaCasa.empilhadosDeProposito) {
            val (a, b) = par.map { ItensDaCasa.caixaNaCena(ItensDaCasa.porId.getValue(it)) }
            assertTrue("$par deveria estar empilhado", a.sobreposicao(b) > 0.25f)
        }
    }

    @Test
    fun `coisas do jardim de baixo nao se amontoam`() {
        // Jardim de baixo de altura 0,3 da cena (tela alta típica): y de 1 a 2 vira 1 a 1,3.
        val extra = 0.3f
        val caixas = ItensDaCasa.noJardimDeBaixo.map { (id, p) ->
            id to ItensDaCasa.caixaNaCena(ItensDaCasa.porId.getValue(id), Posicao(p.x, 1f + (p.y - 1f) * extra))
        }
        for (i in caixas.indices) for (j in i + 1 until caixas.size) {
            val f = caixas[i].second.sobreposicao(caixas[j].second)
            assertTrue("${caixas[i].first} e ${caixas[j].first} amontoados no jardim de baixo ($f)", f <= 0.25f)
        }
    }
}
