package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Relembrar (revisão espaçada), Descobrir o nível e Livrinhos para ler sozinho. */
class RitmoELivrinhosTest {

    private val dia0 = Data(2026, 11, 2).diaEpoca
    private fun sessao(dia: Long, fase: Int, acertos: Int, t: Trilha = Trilha.MATEMATICA) =
        RegistroDeSessao(dia, t, fase, if (acertos >= 4) 2 else 0, acertos, 5, 200)

    // ------------------------------------------------------------------ Relembrar

    @Test fun erroDePrimeiraEntraNaFilaParaOutroDia() {
        val fila = Relembrar.fila(listOf(sessao(dia0, 3, 4)), emptyList())
        assertEquals(1, fila.size)
        assertEquals(dia0 + 1, fila[0].proximoDia)
        assertTrue(Relembrar.paraHoje(fila, dia0).isEmpty())
        assertEquals(1, Relembrar.paraHoje(fila, dia0 + 1).size)
        // Partida sem erro não põe nada na fila.
        assertTrue(Relembrar.fila(listOf(sessao(dia0, 2, 5)), emptyList()).isEmpty())
    }

    @Test fun intervalosCrescemESaiSoDepoisDaRevisaoDe14Dias() {
        val s = listOf(sessao(dia0, 3, 3))
        val r1 = RegistroDeRevisao(dia0 + 1, Trilha.MATEMATICA, 3, true)
        val depois1 = Relembrar.fila(s, listOf(r1))
        assertEquals(dia0 + 1 + 3, depois1.single().proximoDia)
        // Antes da hora, a revisão não conta.
        val cedo = RegistroDeRevisao(dia0 + 2, Trilha.MATEMATICA, 3, true)
        assertEquals(depois1, Relembrar.fila(s, listOf(r1, cedo)))
        // 1, 3, 7 e 14 dias, como diz o texto dos pais: só sai acertando a revisão de 14 dias.
        val r2 = RegistroDeRevisao(dia0 + 4, Trilha.MATEMATICA, 3, true)
        assertEquals(dia0 + 4 + 7, Relembrar.fila(s, listOf(r1, r2)).single().proximoDia)
        val r3 = RegistroDeRevisao(dia0 + 11, Trilha.MATEMATICA, 3, true)
        val depois3 = Relembrar.fila(s, listOf(r1, r2, r3)).single()
        assertEquals(Relembrar.INTERVALOS.lastIndex, depois3.passo)
        assertEquals(dia0 + 11 + 14, depois3.proximoDia)
        val r4 = RegistroDeRevisao(dia0 + 25, Trilha.MATEMATICA, 3, true)
        assertTrue(Relembrar.fila(s, listOf(r1, r2, r3, r4)).isEmpty())
    }

    @Test fun errarNaRevisaoNaoTiraODegrauEChamaNoDiaSeguinte() {
        val s = listOf(sessao(dia0, 7, 2))
        val revs = listOf(
            RegistroDeRevisao(dia0 + 1, Trilha.MATEMATICA, 7, true), // degrau 1: +3
            RegistroDeRevisao(dia0 + 4, Trilha.MATEMATICA, 7, false), // volta amanhã, degrau fica
        )
        val i = Relembrar.fila(s, revs).single()
        assertEquals(1, i.passo)
        assertEquals(0, i.acertosSeguidos)
        assertEquals(dia0 + 5, i.proximoDia)
        // Acerta: sobe para o degrau de 7 dias; depois o de 14; acertando o de 14, sai.
        val r3 = RegistroDeRevisao(dia0 + 5, Trilha.MATEMATICA, 7, true)
        assertEquals(dia0 + 5 + 7, Relembrar.fila(s, revs + r3).single().proximoDia)
        val r4 = RegistroDeRevisao(dia0 + 12, Trilha.MATEMATICA, 7, true)
        assertEquals(dia0 + 12 + 14, Relembrar.fila(s, revs + r3 + r4).single().proximoDia)
        val r5 = RegistroDeRevisao(dia0 + 26, Trilha.MATEMATICA, 7, true)
        assertTrue(Relembrar.fila(s, revs + r3 + r4 + r5).isEmpty())
    }

    @Test fun errarDeNovoNaFaseVoltaParaOComeco() {
        val s = listOf(sessao(dia0, 1, 3), sessao(dia0 + 5, 1, 2))
        val revs = listOf(RegistroDeRevisao(dia0 + 1, Trilha.MATEMATICA, 1, true))
        val i = Relembrar.fila(s, revs).single()
        assertEquals(0, i.passo)
        assertEquals(dia0 + 6, i.proximoDia)
        // Jogar a fase inteira sem erro, quando ela está para relembrar, conta como revisão certa.
        val perfeitas = s + sessao(dia0 + 6, 1, 5) + sessao(dia0 + 9, 1, 5) + sessao(dia0 + 16, 1, 5) + sessao(dia0 + 30, 1, 5)
        assertTrue(Relembrar.fila(perfeitas, revs).isEmpty())
        assertEquals(1, Relembrar.fila(perfeitas.dropLast(1), revs).size)
    }

    @Test fun mesmaEntradaMesmaFila() {
        val s = (0 until 12).map { sessao(dia0 + it % 4, it, it % 6, Trilha.entries[it % 4]) }
        assertEquals(Relembrar.fila(s, emptyList()), Relembrar.fila(s, emptyList()))
        val hoje = Relembrar.paraHoje(Relembrar.fila(s, emptyList()), dia0 + 10)
        assertEquals(hoje.sortedWith(compareBy({ it.proximoDia }, { it.trilha.ordinal }, { it.fase })), hoje)
    }

    @Test fun rodadaTemCincoPerguntasEUmaRevisaoPorFase() {
        val fila = Relembrar.fila(listOf(sessao(dia0, 3, 4), sessao(dia0, 9, 3, Trilha.LOGICA)), emptyList())
        val fases = Relembrar.fasesDaRodada(Relembrar.paraHoje(fila, dia0 + 1))
        assertEquals(Relembrar.PERGUNTAS, fases.size)
        assertEquals(setOf(Trilha.MATEMATICA to 3, Trilha.LOGICA to 9), fases.toSet())
        val revs = Relembrar.revisoesDaRodada(fases, listOf(true, true, true, false, true), dia0 + 1)
        assertEquals(2, revs.size)
        assertTrue(revs.first { it.trilha == Trilha.MATEMATICA }.certo)
        assertFalse(revs.first { it.trilha == Trilha.LOGICA }.certo)
        // Só de um caminho.
        assertEquals(listOf(Trilha.LOGICA to 9), Relembrar.paraHoje(fila, dia0 + 1, Trilha.LOGICA).map { it.trilha to it.fase })
    }

    @Test fun premioFixoDentroDoLimiteENuncaTira() {
        val e = EstadoJogo(moedas = 10, sessoes = listOf(sessao(dia0, 3, 4)))
        val fases = Relembrar.fasesDaRodada(e.paraRelembrar(dia0 + 1))
        val (tudoErrado, m) = e.concluirRelembrar(fases, List(5) { false }, dia0 + 1)
        assertEquals(Relembrar.MOEDAS, m)
        assertEquals(10 + Relembrar.MOEDAS, tudoErrado.moedas)
        // Limite do dia: só o que falta para 30.
        val quase = e.copy(moedasHoje = EstadoJogo.MOEDAS_POR_DIA - 1, diaDasMoedas = dia0 + 1)
        assertEquals(1, quase.concluirRelembrar(fases, List(5) { true }, dia0 + 1).second)
        val cheio = e.copy(moedasHoje = EstadoJogo.MOEDAS_POR_DIA, diaDasMoedas = dia0 + 1)
        assertEquals(0, cheio.concluirRelembrar(fases, List(5) { true }, dia0 + 1).second)
        // Nada de estrelas ou presentes mexidos.
        assertEquals(e.estrelas, tudoErrado.estrelas)
    }

    @Test fun registroDasRevisoesVaiEVoltaEToleraTextoEstragado() {
        val r = listOf(RegistroDeRevisao(dia0, Trilha.INGLES, 12, true), RegistroDeRevisao(dia0 + 1, Trilha.LOGICA, 0, false))
        assertEquals(r, Relembrar.deTexto(Relembrar.paraTexto(r)))
        assertEquals(emptyList<RegistroDeRevisao>(), Relembrar.deTexto(null))
        assertEquals(r.take(1), Relembrar.deTexto(Relembrar.paraTexto(r.take(1)) + ";lixo;1,XX,2,1;3,LOGICA,-1,1"))
    }

    // ------------------------------------------------------------------ Descobrir o nível

    /** Responde como uma criança que sabe tudo até o mundo [sabe]. */
    private fun criancaQueSabe(sabe: Int, trilha: Trilha = Trilha.MATEMATICA): Nivelamento {
        var n = Nivelamento(trilha)
        var vezes = 0
        while (!n.terminou) {
            n = n.responder(n.mundo <= sabe)
            vezes++
            assertTrue("perguntas demais", vezes <= Nivelamento.MAXIMO)
        }
        return n
    }

    @Test fun nivelSobeEnquantoAcertaEParaComDoisErros() {
        val n = criancaQueSabe(5)
        assertEquals(6, n.mundoSugerido)
        assertEquals(50, n.liberarAte)
        assertTrue(n.feitas in 5..Nivelamento.MAXIMO)
        assertEquals(1, criancaQueSabe(0).mundoSugerido)
        assertEquals(0, criancaQueSabe(0).liberarAte)
        assertEquals(2, criancaQueSabe(0).feitas)
        assertEquals(25, criancaQueSabe(25).mundoSugerido)
        for (s in 0..25) {
            val r = criancaQueSabe(s)
            assertTrue("sabe $s, sugeriu ${r.mundoSugerido}", r.mundoSugerido in (s - 2).coerceAtLeast(1)..(s + 1).coerceAtMost(25))
        }
    }

    @Test fun umErroSoNaoParaOMundo() {
        val n = Nivelamento(Trilha.LOGICA).responder(true).responder(false)
        assertEquals(4, n.mundo)
        assertEquals(32, n.faseDaPergunta) // segunda pergunta do mundo 4: terceira fase dele
        assertFalse(n.terminou)
        assertEquals(n, Nivelamento.de(Trilha.LOGICA, listOf(true, false)))
    }

    @Test fun nivelAbreSemVencerEPaisDesfazem() {
        val g = GeradorDeFases()
        val fases = g.fases(Trilha.MATEMATICA)
        val n = criancaQueSabe(5)
        val e = EstadoJogo().aplicarNivel(n)
        assertTrue(e.liberada(fases[50]))
        assertTrue(e.liberada(fases[23]))
        assertFalse(e.liberada(fases[51]))
        assertEquals(0, e.fasesVencidas)
        assertEquals(0, e.totalDeEstrelas)
        assertEquals(fases[50], e.faseDaVez(fases))
        assertFalse(e.ofereceNivel(Trilha.MATEMATICA))
        assertTrue(e.ofereceNivel(Trilha.LOGICA))
        // Vencer a fase 50 abre a 51, como sempre.
        assertTrue(e.registrarVitoria(fases[50], 3).first.liberada(fases[51]))
        val desfeito = e.desfazerNivel(Trilha.MATEMATICA)
        assertFalse(desfeito.liberada(fases[1]))
        assertTrue(desfeito.ofereceNivel(Trilha.MATEMATICA))
        // Saves antigos: sem a chave, nada muda.
        assertEquals(emptyMap<Trilha, Int>(), SerializacaoDoNivel.textoParaLiberada(null))
        assertEquals(mapOf(Trilha.MATEMATICA to 50), SerializacaoDoNivel.textoParaLiberada(SerializacaoDoNivel.liberadaParaTexto(e.liberadaAte) + ";X=3;LOGICA=999"))
        assertEquals(setOf(Trilha.MATEMATICA), SerializacaoDoNivel.textoParaFeito(SerializacaoDoNivel.feitoParaTexto(e.nivelFeito)))
    }

    @Test fun nivelNuncaFechaOQueJaEstavaAberto() {
        val e = EstadoJogo(liberadaAte = mapOf(Trilha.INGLES to 80)).aplicarNivel(criancaQueSabe(2, Trilha.INGLES))
        assertEquals(80, e.liberadaAte[Trilha.INGLES])
    }

    // ------------------------------------------------------------------ Livrinhos

    @Test fun livrinhosSaoMuitosECurtos() {
        assertTrue(Livrinhos.todos.size >= 30)
        assertEquals(Livrinhos.todos.size, Livrinhos.porId.size)
        for (l in Livrinhos.todos) {
            assertTrue("${l.id}: ${l.paginas.size} páginas", l.paginas.size in 4..8)
            assertTrue(l.id, l.perguntas.size in 1..2)
            for (p in l.paginas) {
                assertTrue("${l.id}: frase longa demais: ${p.texto}", Leitura.palavras(p.texto).size <= 9)
                assertTrue(l.id, p.figuras.isNotEmpty())
            }
            for (q in l.perguntas) {
                assertEquals(l.id, 2, q.erradas.size)
                assertEquals(l.id, 3, (q.erradas + q.certa).map { it.emoji }.distinct().size)
            }
        }
        // Ordem da estante: do mais fácil para o mais difícil.
        assertEquals(Livrinhos.todos.sortedBy { it.fase }, Livrinhos.todos)
    }

    @Test fun todaPalavraDeCadaLivrinhoDaParaLerNaFaseDele() {
        val problemas = Livrinhos.todos.mapNotNull { l ->
            val dificeis = Leitura.palavrasDificeis(l.textosParaLer, l.fase)
            val minima = Leitura.faseMinima(l.textosParaLer)
            when {
                dificeis.isNotEmpty() -> "${l.id} (fase ${l.fase}): ainda não dá para ler $dificeis"
                minima != l.fase -> "${l.id}: marcado na fase ${l.fase}, mas já dá para ler na fase $minima"
                else -> null
            }
        }
        assertTrue(problemas.joinToString("\n"), problemas.isEmpty())
    }

    @Test fun leituraUsaAsSilabasDoCaminhoLetras() {
        // Antes da fase das famílias silábicas, nada.
        assertNull(Leitura.silabas("PATO", 10))
        assertEquals(listOf("PA", "TO"), Leitura.silabas("pato", 26))
        assertEquals(listOf("CA", "SA"), Leitura.silabas("Casa", 200))
        assertNull(Leitura.silabas("chuva", 26))
        assertEquals(listOf("CHU", "VA"), Leitura.silabas("chuva", 32))
        assertNull(Leitura.silabas("mão", 50))
        assertEquals(listOf("MÃO"), Leitura.silabas("mão", 60))
        assertNull(Leitura.silabas("sol", 70))
        assertEquals(listOf("SOL"), Leitura.silabas("sol", 80))
        assertEquals(listOf("ES", "CO", "LA"), Leitura.silabas("escola", 80))
        assertNull(Leitura.silabas("trem", 100))
        assertEquals(listOf("TREM"), Leitura.silabas("trem", 110))
        assertNull(Leitura.silabas("bom", 120))
        assertEquals(listOf("BOM"), Leitura.silabas("bom", 130))
        assertEquals(32, Leitura.faseMinima(listOf("Olha a chuva!")))
        assertEquals(listOf("Olha"), Leitura.palavrasDificeis(listOf("Olha a lua."), 26))
        assertNull(Leitura.silabas("é", 150))
        assertEquals(listOf("É"), Leitura.silabas("é", 203))
        // Cada etapa vem de uma fase de verdade do caminho Letras.
        val letras = GeradorDeFases().fases(Trilha.PORTUGUES)
        assertEquals("Famílias: BA BE BI", letras[Leitura.etapas.first().fase].titulo)
        assertEquals("CH, LH e NH", letras[32].titulo)
        assertEquals("Pescaria de acentos", letras[Leitura.etapas.last().fase].titulo)
        val fala = Leitura.falaDosPedacinhos("piano")
        assertEquals(4, fala.size)
        assertEquals("pi", fala.first())
        assertEquals("piano", fala.last())
    }

    @Test fun livrinhoAbreComAFaseDeLetrasEMarcadorUmaVezSo() {
        val primeiro = Livrinhos.todos.first()
        val e = EstadoJogo()
        assertFalse(Livrinhos.liberado(primeiro, e))
        assertEquals(primeiro.fase + 1, Livrinhos.faltam(primeiro, e))
        val venceu = e.copy(estrelas = mapOf(EstadoJogo.chave(Trilha.PORTUGUES, primeiro.fase) to 2))
        assertTrue(Livrinhos.liberado(primeiro, venceu))
        assertFalse(Livrinhos.liberado(Livrinhos.todos.last(), venceu))
        // Fase vencida em outro caminho não abre livrinho.
        assertFalse(Livrinhos.liberado(primeiro, e.copy(estrelas = mapOf(EstadoJogo.chave(Trilha.MATEMATICA, 100) to 3))))
        // O nível descoberto em Letras também conta.
        assertTrue(Livrinhos.liberado(Livrinhos.porId.getValue("escola"), e.copy(liberadaAte = mapOf(Trilha.PORTUGUES to 90))))
        assertEquals(primeiro, Livrinhos.sugerido(venceu))
        val lido = venceu.terminarLivrinho(primeiro.id)
        assertEquals(setOf(primeiro.id), lido.livrinhosLidos)
        assertEquals(lido, lido.terminarLivrinho(primeiro.id))
        assertEquals(lido, lido.terminarLivrinho("nao-existe"))
        assertEquals(setOf(primeiro.id), Livrinhos.textoParaLidos(Livrinhos.lidosParaTexto(lido.livrinhosLidos) + ",velho"))
        // As opções da pergunta: a certa muda de lugar, sem sorteio.
        val q = primeiro.perguntas.first()
        assertEquals(q.certa, q.opcoes(0)[0])
        assertEquals(q.certa, q.opcoes(2)[2])
    }

    @Test
    fun digrafosNuncaSeparam() {
        val esperado = mapOf(
            "OLHA" to listOf("O", "LHA"),
            "GALINHA" to listOf("GA", "LI", "NHA"),
            "NINHO" to listOf("NI", "NHO"),
            "FOLHA" to listOf("FO", "LHA"),
            "FILHOTE" to listOf("FI", "LHO", "TE"),
            "DESENHA" to listOf("DE", "SE", "NHA"),
            "MONTANHA" to listOf("MON", "TA", "NHA"),
            "ABELHA" to listOf("A", "BE", "LHA"),
            "CHUVA" to listOf("CHU", "VA"),
            "AQUI" to listOf("A", "QUI"),
            "CARRO" to listOf("CAR", "RO"),
        )
        for ((palavra, silabas) in esperado) assertEquals(palavra, silabas, Leitura.silabas(palavra, 300))
        for (palavra in listOf("PALHAÇO", "BANHEIRA")) {
            val s = Leitura.silabas(palavra, 300)
            assertTrue("$palavra: $s", s != null && s.none { it.startsWith("H") })
        }
    }

    @Test
    fun duasVogaisJuntasSoDepoisDosDitongos() {
        for (palavra in listOf("vai", "noite", "areia", "cai", "pousa", "deita", "cenoura")) {
            assertNull("$palavra antes da fase 71", Leitura.silabas(palavra, 69))
            assertTrue("$palavra na fase 71", Leitura.silabas(palavra, 70) != null)
        }
        // Uma vogal sozinha no começo continua valendo cedo (A-BE-LHA, E-LE), e duas sílabas também (LI-A, BO-A).
        assertEquals(listOf("E", "LE"), Leitura.silabas("ele", 26))
        assertEquals(listOf("LI", "A"), Leitura.silabas("Lia", 26))
        assertEquals(listOf("BO", "A"), Leitura.silabas("boa", 26))
    }

    @Test
    fun faseFechadaDeNovoNaoApareceParaRelembrar() {
        // Fase 30 de Lógica aberta pelo nível, errada de primeira; depois os pais desfazem o nível.
        val e = EstadoJogo(liberadaAte = mapOf(Trilha.LOGICA to 30), sessoes = listOf(sessao(dia0, 30, 3, Trilha.LOGICA)))
        assertEquals(1, e.paraRelembrar(dia0 + 1).size)
        assertTrue(e.desfazerNivel(Trilha.LOGICA).paraRelembrar(dia0 + 1).isEmpty())
    }
}
