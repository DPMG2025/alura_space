package br.com.casinhadosaber.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** Sugestões 1 a 7: personalização, mascote, história, eventos, ateliê, conquistas e pais. */
class SugestoesTest {

    private fun d(a: Int, m: Int, dia: Int) = Data(a, m, dia)

    /** Progresso com as fases [fases] do caminho [t] vencidas com [estrelas]. */
    private fun progresso(t: Trilha, fases: IntRange, estrelas: Int = 3): Map<String, Int> =
        fases.associate { EstadoJogo.chave(t, it) to estrelas }

    // ================================================================== datas

    @Test
    fun `dia epoca vai e volta e bate com datas conhecidas`() {
        assertEquals(0L, d(1970, 1, 1).diaEpoca)
        assertEquals(19723L, d(2024, 1, 1).diaEpoca)
        assertEquals(d(2000, 2, 29), Data.deDiaEpoca(d(2000, 2, 29).diaEpoca))
        for (dia in -800L..30000L step 7) assertEquals(dia, Data.deDiaEpoca(dia).diaEpoca)
        assertEquals(4, d(1970, 1, 1).diaDaSemana) // quinta feira
        assertEquals(2, d(2026, 11, 3).diaDaSemana) // terça feira
        assertEquals(d(2026, 3, 1), d(2026, 2, 28).maisDias(1))
    }

    @Test
    fun `pascoa de varios anos`() {
        val conhecidas = mapOf(
            2000 to d(2000, 4, 23), 2008 to d(2008, 3, 23), 2019 to d(2019, 4, 21), 2024 to d(2024, 3, 31),
            2025 to d(2025, 4, 20), 2026 to d(2026, 4, 5), 2027 to d(2027, 3, 28), 2030 to d(2030, 4, 21),
            2038 to d(2038, 4, 25),
        )
        conhecidas.forEach { (ano, data) -> assertEquals("Páscoa de $ano", data, Calendario.pascoa(ano)) }
        // É sempre domingo.
        (1990..2100).forEach { assertEquals(7, Calendario.pascoa(it).diaDaSemana) }
    }

    @Test
    fun `janelas dos eventos`() {
        fun tem(t: TipoDeEvento, data: Data, a: Aniversario? = null) = Eventos.acontece(t, data, a)
        assertTrue(tem(TipoDeEvento.NATAL, d(2026, 12, 1)))
        assertTrue(tem(TipoDeEvento.NATAL, d(2026, 12, 26)))
        assertFalse(tem(TipoDeEvento.NATAL, d(2026, 12, 27)))
        assertFalse(tem(TipoDeEvento.NATAL, d(2026, 11, 30)))
        // Páscoa 2026: 5 de abril. Domingo de Ramos (29/3) até a segunda (6/4).
        assertFalse(tem(TipoDeEvento.PASCOA, d(2026, 3, 28)))
        assertTrue(tem(TipoDeEvento.PASCOA, d(2026, 3, 29)))
        assertTrue(tem(TipoDeEvento.PASCOA, d(2026, 4, 6)))
        assertFalse(tem(TipoDeEvento.PASCOA, d(2026, 4, 7)))
        assertTrue(tem(TipoDeEvento.FESTA_JUNINA, d(2026, 6, 1)))
        assertTrue(tem(TipoDeEvento.FESTA_JUNINA, d(2026, 6, 30)))
        assertFalse(tem(TipoDeEvento.FESTA_JUNINA, d(2026, 7, 1)))
        assertTrue(tem(TipoDeEvento.DIA_DAS_CRIANCAS, d(2026, 10, 12)))
        assertTrue(tem(TipoDeEvento.DIA_DAS_CRIANCAS, d(2026, 10, 9)))
        assertFalse(tem(TipoDeEvento.DIA_DAS_CRIANCAS, d(2026, 10, 8)))
        assertFalse(tem(TipoDeEvento.DIA_DAS_CRIANCAS, d(2026, 10, 13)))
        assertTrue(Eventos.doDia(d(2026, 8, 15)).isEmpty())
        assertNull(Eventos.principal(d(2026, 8, 15)))
    }

    @Test
    fun `aniversario vem primeiro e 29 de fevereiro comemora no dia 28`() {
        val niver = Aniversario(10, 12)
        val eventos = Eventos.doDia(d(2026, 12, 10), niver)
        assertEquals(listOf(TipoDeEvento.ANIVERSARIO, TipoDeEvento.NATAL), eventos.map { it.tipo })
        assertEquals(TipoDeEvento.ANIVERSARIO, Eventos.principal(d(2026, 12, 10), niver)?.tipo)
        assertFalse(Eventos.acontece(TipoDeEvento.ANIVERSARIO, d(2026, 12, 11), niver))
        assertFalse(Eventos.acontece(TipoDeEvento.ANIVERSARIO, d(2026, 12, 10), null))
        val bissexto = Aniversario(29, 2)
        assertTrue(bissexto.ehNoDia(d(2027, 2, 28)))
        assertFalse(bissexto.ehNoDia(d(2028, 2, 28)))
        assertTrue(bissexto.ehNoDia(d(2028, 2, 29)))
    }

    @Test
    fun `presente do evento vem uma vez por ano e gira a cada ano`() {
        val natal26 = EventoAtivo(TipoDeEvento.NATAL, 2026)
        val (e1, p1) = EstadoJogo().receberPresenteDoEvento(natal26)
        assertNotNull(p1)
        assertTrue(p1!!.id in e1.presentesEspeciais)
        val (e2, p2) = e1.receberPresenteDoEvento(natal26)
        assertNull(p2)
        assertEquals(e1, e2)
        val (e3, p3) = e2.receberPresenteDoEvento(EventoAtivo(TipoDeEvento.NATAL, 2027))
        assertNotNull(p3)
        assertNotEquals(p1, p3)
        assertEquals(2, e3.presentesGanhos.size)
        assertEquals(Eventos.todosOsPresentes.size, Eventos.presentePorId.size)
    }

    @Test
    fun `saudacao do evento usa o nome so quando existe`() {
        val niver = EventoAtivo(TipoDeEvento.ANIVERSARIO, 2026)
        assertTrue(niver.saudacao("Bia").contains("Bia"))
        assertFalse(niver.saudacao("  ").contains("{nome}"))
        assertFalse(niver.saudacao(null).contains(","))
        TipoDeEvento.entries.forEach { assertFalse(EventoAtivo(it, 2026).saudacao("").contains("{")) }
    }

    @Test
    fun `surpresa da semana e deterministica, troca na segunda e nunca some da loja`() {
        assertNull(Surpresas.daSemana(d(2026, 11, 1)))
        assertTrue(Surpresas.liberadas(d(2026, 11, 1)).isEmpty())
        val segunda = Surpresas.daSemana(d(2026, 11, 2))!!
        assertEquals(segunda, Surpresas.daSemana(d(2026, 11, 8))) // mesma semana até domingo
        val proxima = Surpresas.daSemana(d(2026, 11, 9))!!
        assertNotEquals(segunda.visitante, proxima.visitante)
        assertEquals(1, Surpresas.liberadas(d(2026, 11, 8)).size)
        assertEquals(2, Surpresas.liberadas(d(2026, 11, 9)).size)
        // Cresce sem perder nada, até o catálogo inteiro.
        var antes = emptyList<Produto>()
        for (semana in 0 until 40) {
            val agora = Surpresas.liberadas(d(2026, 11, 2).maisDias(7L * semana))
            assertTrue(agora.containsAll(antes))
            antes = agora
        }
        assertEquals(Surpresas.catalogo, antes)
        // Depois do catálogo, só visitas (nada novo, mas a visita continua girando).
        val depois = Surpresas.daSemana(d(2026, 11, 2).maisDias(7L * 40))!!
        assertNull(depois.novidade)
        assertEquals(Surpresas.catalogo.size, Surpresas.catalogo.map { it.id }.toSet().size)
        assertTrue(Surpresas.catalogo.none { it.id in Loja.porId })
        // Comprar um visitante da semana usa o fluxo normal da loja.
        val comprado = EstadoJogo(moedas = 100).usar(segunda.visitante)!!
        assertTrue(segunda.visitante.valor in comprado.visitantes)
    }

    // ================================================================== personalização

    @Test
    fun `frases com nome e sem nome`() {
        assertEquals("Muito bem, Bia!", FrasesComNome.elogio("Bia", 0))
        assertEquals("Muito bem!", FrasesComNome.elogio("", 0))
        assertEquals("Muito bem!", FrasesComNome.elogio(null, 0))
        assertEquals("Muito bem, Bia!", FrasesComNome.elogio(" Bia ", 0))
        for (i in -3..20) {
            listOf(FrasesComNome.elogio("", i), FrasesComNome.incentivo("", i), FrasesComNome.chegada("", i)).forEach {
                assertFalse(it, it.contains("{nome}") || it.contains(" ,") || it.contains(", !"))
            }
            assertTrue(FrasesComNome.incentivo("Davi", i).contains("Davi"))
        }
        assertTrue(FrasesComNome.vitoria("Bia", 3).contains("Bia"))
    }

    @Test
    fun `temas favoritos no maximo 3 e leitura tolerante`() {
        var temas = emptyList<TemaFavorito>()
        TemaFavorito.entries.forEach { temas = TemaFavorito.alternar(temas, it) }
        assertEquals(3, temas.size)
        temas = TemaFavorito.alternar(temas, temas.first())
        assertEquals(2, temas.size)
        assertEquals(listOf(TemaFavorito.ANIMAIS, TemaFavorito.ESPACO), TemaFavorito.deTexto("ANIMAIS,XYZ,ESPACO,ANIMAIS"))
        assertEquals(3, TemaFavorito.deTexto(TemaFavorito.entries.joinToString(",") { it.name }).size)
        assertTrue(TemaFavorito.deTexto(null).isEmpty())
        assertEquals(Aniversario(5, 3), Aniversario.deTexto(Aniversario.paraTexto(Aniversario(5, 3))))
        assertNull(Aniversario.deTexto("31/2"))
        assertNull(Aniversario.deTexto("abc"))
        assertNull(Aniversario.deTexto(null))
    }

    @Test
    fun `sem temas o sorteio e igual ao de antes`() {
        val g1 = GeradorDeFases(Random(7))
        val g2 = GeradorDeFases(Random(7), emptyList())
        Trilha.entries.forEach { t ->
            listOf(0, 3, 12, 30, 60).forEach { n ->
                val f = Fase(t, n, "", "")
                assertEquals(g1.perguntas(f).map { it.instrucao + it.cena }, g2.perguntas(f).map { it.instrucao + it.cena })
            }
        }
    }

    @Test
    fun `tema favorito aparece mais nas perguntas de contar e de letras`() {
        val r = Random(3)
        val dino = (1..400).map { PreferenciaDeTema.objetoParaContar(listOf(TemaFavorito.DINOSSAUROS), r) }
        assertTrue(dino.count { it.emoji in TemaFavorito.DINOSSAUROS.emojis } > 120)
        val semTema = (1..400).map { PreferenciaDeTema.objetoParaContar(emptyList(), r) }
        assertTrue(semTema.all { it in Vocabulario.objetosParaContar })
        // A lista nunca muda: a escolha é sempre dentro dela.
        val lista = Vocabulario.palavrasComConsoante
        val escolhidas = (1..400).map { PreferenciaDeTema.escolher(lista, listOf(TemaFavorito.ANIMAIS), r) { it.emoji } }
        assertTrue(escolhidas.all { it in lista })
        // Fases de contar com o tema: continuam válidas e mostram figuras do tema.
        val g = GeradorDeFases(Random(5), listOf(TemaFavorito.ESPACO))
        val perguntas = (1..30).flatMap { g.perguntas(Fase(Trilha.MATEMATICA, 0, "", "")) }
        assertTrue(perguntas.any { p -> TemaFavorito.ESPACO.objetosExtras.any { o -> p.cena.toString().contains(o.emoji) } })
    }

    // ================================================================== mascote

    @Test
    fun `mascote so cresce e libera acessorios`() {
        assertEquals(EstagioMascote.BEBE, Crescimento.estagio(0))
        assertEquals(EstagioMascote.BEBE, Crescimento.estagio(-5))
        assertEquals(EstagioMascote.BEBE, Crescimento.estagio(9))
        assertEquals(EstagioMascote.PEQUENO, Crescimento.estagio(10))
        assertEquals(EstagioMascote.CRIANCA, Crescimento.estagio(40))
        assertEquals(EstagioMascote.GRANDE, Crescimento.estagio(100))
        assertEquals(EstagioMascote.CAMPEAO, Crescimento.estagio(1000))
        var anterior = EstagioMascote.BEBE
        for (n in 0..1000) {
            val e = Crescimento.estagio(n)
            assertTrue(e.ordinal >= anterior.ordinal)
            anterior = e
            val p = Crescimento.progressoNoEstagio(n)
            assertTrue(p in 0f..1f)
        }
        assertEquals(5, Crescimento.faltamParaCrescer(5))
        assertEquals(0, Crescimento.faltamParaCrescer(300))
        assertTrue(Crescimento.cresceu(9, 10))
        assertFalse(Crescimento.cresceu(10, 11))
        assertEquals(listOf(AcessorioMascote.LACINHO), Crescimento.acessoriosNovos(2, 3))
        assertEquals(AcessorioMascote.entries, Crescimento.acessoriosLiberados(1000))
    }

    @Test
    fun `mascote escolhido, nomeado e vestido, e save tolerante`() {
        val estrelas = progresso(Trilha.MATEMATICA, 0..19)
        var e = EstadoJogo(estrelas = estrelas)
        assertFalse(e.mascote.escolhido)
        e = e.escolherMascote(EspecieMascote.DRAGAOZINHO).nomearMascote("  Fogo|zinho ")
        assertEquals("Fogozinho", e.mascote.nomeParaMostrar)
        assertEquals(EstagioMascote.PEQUENO, e.estagioDoMascote)
        e = e.vestirMascote(AcessorioMascote.BONE)
        assertEquals(AcessorioMascote.BONE, e.mascote.acessorioEmUso)
        assertEquals(e, e.vestirMascote(AcessorioMascote.COROA).copy(mascote = e.mascote)) // ainda não liberada
        assertEquals("", e.vestirMascote(AcessorioMascote.BONE).mascote.acessorio)
        assertEquals(e.mascote, Mascote.deTexto(Mascote.paraTexto(e.mascote)))
        assertEquals(Mascote(), Mascote.deTexto(null))
        assertEquals(Mascote(), Mascote.deTexto("PEIXE|x|y"))
        assertEquals(Mascote(EspecieMascote.GATINHO), Mascote.deTexto("GATINHO"))
        assertEquals("Gatinho", Mascote(EspecieMascote.GATINHO).nomeParaMostrar)
        (0..10).forEach { i -> assertFalse(Crescimento.fala(e.mascote, 20, "", i).contains("{")) }
    }

    // ================================================================== história

    @Test
    fun `cada caminho tem 25 capitulos com textos curtos e diferentes`() {
        Trilha.entries.forEach { t ->
            val caps = Historia.capitulos(t)
            assertEquals(25, caps.size)
            assertEquals(25, caps.map { it.introducao }.toSet().size)
            assertEquals(25, caps.map { it.cenaDoChefao }.toSet().size)
            caps.forEach { c ->
                listOf(c.introducao, c.cenaDoChefao).forEach { texto ->
                    val frases = texto.split(Regex("[.!?]+")).count { it.isNotBlank() }
                    assertTrue("$texto: $frases frases", frases in 1..4)
                    assertTrue(texto.length < 260)
                }
                assertEquals(Personagem.CORUJINHA, c.narradora)
                assertEquals(Historia.amigoDe(t), c.amigo)
            }
        }
        val primeira = Fase(Trilha.LOGICA, 0, "", "")
        assertNotNull(Historia.introducao(primeira))
        assertNull(Historia.cenaDoChefao(primeira))
        val chefao = Fase(Trilha.LOGICA, 9, "", "")
        assertNull(Historia.introducao(chefao))
        assertNotNull(Historia.cenaDoChefao(chefao))
        assertEquals(Historia.capitulo(Trilha.INGLES, 24), Historia.capitulo(Fase(Trilha.INGLES, 249, "", "")))
    }

    // ================================================================== ateliê e piano

    @Test
    fun `desenho pinta, traca sem buracos, enche e volta do texto`() {
        var d = Pintura.vazio("d1")
        assertTrue(d.vazio)
        d = d.tracar(0, 0, 11, 11, 2)
        (0..11).forEach { assertEquals(2, d.cor(it, it)) }
        d = d.tracar(5, 0, 5, 11, 6)
        (0..11).forEach { assertEquals(6, d.cor(5, it)) }
        d = d.pintar(20, 3, 4).pintar(1, 1, 99)
        assertEquals(2, d.cor(1, 1))
        val cheio = Pintura.vazio("d2").encher(3, 3, 5)
        assertTrue(cheio.celulas.all { it == 5 })
        val texto = Atelie.desenhosParaTexto(listOf(d, cheio))
        assertEquals(listOf(d, cheio), Atelie.textoParaDesenhos(texto))
        assertTrue(Atelie.textoParaDesenhos(null).isEmpty())
        assertEquals(listOf(cheio), Atelie.textoParaDesenhos("d1:abc;" + texto.substringAfter(";")))
        assertEquals(listOf("d2"), Atelie.textoParaParede("d9,d2,d2", listOf(d, cheio)))
    }

    @Test
    fun `ateliê guarda no maximo 12 e parede com 3 quadros`() {
        var e = EstadoJogo()
        repeat(Atelie.MAXIMO_DE_DESENHOS) { e = e.salvarDesenho(Pintura.vazio(Atelie.novoId(e.desenhos)))!! }
        assertEquals(12, e.desenhos.map { it.id }.toSet().size)
        assertNull(e.salvarDesenho(Pintura.vazio(Atelie.novoId(e.desenhos))))
        // Salvar por cima de um que já existe sempre pode.
        val editado = e.desenhos[0].pintar(0, 0, 3)
        e = e.salvarDesenho(editado)!!
        assertEquals(editado, e.desenhos[0])
        listOf("d1", "d2", "d3", "d4").forEach { e = e.alternarNaParede(it) }
        assertEquals(listOf("d2", "d3", "d4"), e.quadrosNaParede)
        e = e.alternarNaParede("d3").alternarNaParede("nao-existe")
        assertEquals(listOf("d2", "d4"), e.quadrosNaParede)
        e = e.apagarDesenho("d2")
        assertEquals(listOf("d4"), e.quadrosNaParede)
        assertEquals(1, e.quadrosPendurados.size)
    }

    @Test
    fun `piano tem notas em ordem e musicas para seguir`() {
        Nota.entries.zipWithNext().forEach { (a, b) -> assertTrue(b.frequencia > a.frequencia) }
        assertEquals(440.0, Nota.LA.frequencia, 0.01)
        assertTrue(Piano.musicas.size >= 3)
        val m = Piano.porId.getValue("brilha")
        assertEquals(Nota.DO, m.notas.first().nota)
        var pos = 0
        pos = Piano.seguir(m, pos, Nota.SI)
        assertEquals(0, pos) // tecla errada não volta nem avança
        m.notas.forEach { pos = Piano.seguir(m, pos, it.nota) }
        assertTrue(Piano.terminou(m, pos))
    }

    // ================================================================== conquistas

    @Test
    fun `diploma quando as 10 fases do mundo tem estrela`() {
        val quase = progresso(Trilha.PORTUGUES, 0..8, 1)
        assertFalse(Conquistas.mundoCompleto(quase, Trilha.PORTUGUES, 0))
        assertTrue(Conquistas.diplomas(quase).isEmpty())
        val completo = quase + progresso(Trilha.PORTUGUES, 9..9, 1)
        assertEquals(listOf(Diploma(Trilha.PORTUGUES, 0)), Conquistas.diplomas(completo))
        assertEquals(listOf(Diploma(Trilha.PORTUGUES, 0)), Conquistas.diplomasNovos(quase, completo))
        assertTrue(Diploma(Trilha.PORTUGUES, 0).texto("Bia").contains("Bia"))
        assertFalse(Diploma(Trilha.PORTUGUES, 0).texto("").contains("{"))
    }

    @Test
    fun `figurinhas por 3 estrelas e dourada do chefao, retroativas`() {
        // Save antigo: as 10 primeiras fases de Números, metade com 3 estrelas, chefão com 2.
        val estrelas = (0..9).associate { EstadoJogo.chave(Trilha.MATEMATICA, it) to if (it % 2 == 0) 3 else 2 }
        val ganhas = Conquistas.figurinhasGanhas(estrelas)
        assertEquals(5 + 1, ganhas.size)
        assertTrue("MATEMATICA:m0:ouro" in ganhas)
        val pagina = Conquistas.album(Trilha.MATEMATICA, estrelas).first()
        assertEquals(11, pagina.figurinhas.size)
        assertEquals(6, pagina.quantasGanhas)
        assertFalse(pagina.completa)
        assertEquals(25, Conquistas.album(Trilha.LOGICA, estrelas).size)
        assertEquals(1100, Conquistas.totalDeFigurinhas)
        // Ids únicos no álbum inteiro.
        val todas = Trilha.entries.flatMap { t -> (0..24).flatMap { Conquistas.figurinhasDoMundo(t, it) } }
        assertEquals(todas.size, todas.map { it.id }.toSet().size)
        // Melhorar de 2 para 3 estrelas dá a figurinha nova.
        val depois = estrelas + (EstadoJogo.chave(Trilha.MATEMATICA, 1) to 3)
        assertEquals(listOf("MATEMATICA:1"), Conquistas.figurinhasNovas(estrelas, depois).map { it.id })
    }

    // ================================================================== pais

    @Test
    fun `desafio do dia gira pelos 60 sem repetir`() {
        assertEquals(60, Pais.desafios.size)
        assertEquals(60, Pais.desafios.map { it.texto }.toSet().size)
        AreaDoDesafio.entries.forEach { a -> assertEquals(15, Pais.desafios.count { it.area == a }) }
        val dias = (20000L until 20060L).map { Pais.desafioDoDia(it) }
        assertEquals(60, dias.toSet().size)
        assertEquals(Pais.desafioDoDia(20000L), Pais.desafioDoDia(20060L))
        // Dias seguidos trocam de área.
        assertNotEquals(Pais.desafioDoDia(20000L).area, Pais.desafioDoDia(20001L).area)
    }

    @Test
    fun `desafio feito da moedinhas uma vez por dia`() {
        val (e1, m1) = EstadoJogo(moedas = 2).concluirDesafioDosPais(100L)
        assertEquals(Pais.MOEDAS_DO_DESAFIO, m1)
        assertEquals(2 + Pais.MOEDAS_DO_DESAFIO, e1.moedas)
        val (e2, m2) = e1.concluirDesafioDosPais(100L)
        assertEquals(0, m2)
        assertEquals(e1, e2)
        assertTrue(e2.desafioDosPaisFeito(100L))
        val (e3, _) = e2.concluirDesafioDosPais(200L)
        assertEquals(setOf(200L), e3.desafiosDosPaisFeitos) // 100 ficou para trás de 60 dias
        assertEquals(setOf(1L, 5L), Pais.textoParaDias(Pais.diasParaTexto(setOf(5L, 1L))))
        assertTrue(Pais.textoParaDias("x,,").isEmpty())
    }

    @Test
    fun `registro de partidas corta 60 dias e le saves estragados`() {
        var e = EstadoJogo()
        for (dia in 0L until 100L) e = e.registrarSessao(RegistroDeSessao(dia, Trilha.LOGICA, 1, 3, 5, 5, 60))
        assertEquals(60, e.sessoes.size)
        assertEquals(40L, e.sessoes.first().dia)
        val texto = Pais.registrosParaTexto(e.sessoes)
        assertEquals(e.sessoes, Pais.textoParaRegistros(texto))
        assertEquals(1, Pais.textoParaRegistros("lixo;1,XX,1,1,1,1,1,0;5,LOGICA,2,3,5,5,90,1;5,LOGICA,2,3,-5,5,90,1").size)
        assertTrue(Pais.textoParaRegistros(null).isEmpty())
        val fase = Fase(Trilha.MATEMATICA, 4, "", "")
        val r = EstadoJogo().registrarPartida(fase, 5, 5, 300L)
        val comSessao = r.estado.registrarSessao(fase, r, 5, 5, 120L, 300L)
        assertEquals(RegistroDeSessao(300L, Trilha.MATEMATICA, 4, 3, 5, 5, 120L, true), comSessao.sessoes.single())
    }

    @Test
    fun `relatorio da semana`() {
        val segunda = d(2026, 11, 2).diaEpoca
        val registros = listOf(
            RegistroDeSessao(segunda - 1, Trilha.MATEMATICA, 0, 3, 5, 5, 600), // domingo anterior: fora
            RegistroDeSessao(segunda, Trilha.MATEMATICA, 3, 3, 5, 5, 180, faseNova = true),
            RegistroDeSessao(segunda, Trilha.MATEMATICA, 3, 3, 5, 5, 120),
            RegistroDeSessao(segunda + 2, Trilha.PORTUGUES, 7, 0, 2, 5, 240),
            RegistroDeSessao(segunda + 3, Trilha.PORTUGUES, 7, 2, 4, 5, 200, faseNova = true),
            RegistroDeSessao(segunda + 6, Trilha.INGLES, 1, 2, 4, 5, 100),
            RegistroDeSessao(segunda + 7, Trilha.INGLES, 1, 3, 5, 5, 100), // segunda seguinte: fora
        )
        val r = Pais.relatorioDaSemana(registros, segunda + 4) { t, n -> "${t.name}-$n" }
        assertEquals(d(2026, 11, 2), r.inicio)
        assertEquals(d(2026, 11, 8), r.fim)
        assertEquals(5, r.partidas)
        assertEquals((180 + 120 + 240 + 200 + 100) / 60, r.minutosJogados)
        assertEquals(4, r.diasJogados)
        assertEquals(2, r.fasesNovas)
        assertEquals(3, r.fasesVencidas)
        assertEquals(listOf("MATEMATICA-3"), r.pontosFortes.map { it.nome })
        assertEquals(listOf("PORTUGUES-7"), r.paraReforcar.map { it.nome })
        assertEquals(60, r.paraReforcar.single().precisao)
        assertTrue(r.sugestoes.size in 2..3)
        assertEquals(r.sugestoes.size, r.sugestoes.toSet().size)
        assertEquals(r, Pais.relatorioDaSemana(registros, segunda + 6) { t, n -> "${t.name}-$n" })
        val vazio = Pais.relatorioDaSemana(emptyList(), segunda)
        assertTrue(vazio.vazio)
        assertTrue(vazio.sugestoes.size >= 2)
        assertEquals("Números, fase 4", Pais.nomePadrao(Trilha.MATEMATICA, 3))
    }

    // ================================================================== saves antigos

    @Test
    fun `estado antigo continua valendo com os campos novos no padrao`() {
        val antigo = EstadoJogo(perfil = Perfil("", Genero.MENINO), estrelas = progresso(Trilha.LOGICA, 0..9))
        assertEquals(Mascote(), antigo.mascote)
        assertNull(antigo.perfil?.aniversario)
        assertTrue(antigo.perfil!!.temasFavoritos.isEmpty())
        assertTrue(antigo.sessoes.isEmpty() && antigo.desenhos.isEmpty() && antigo.presentesEspeciais.isEmpty())
        // Diplomas e figurinhas aparecem na hora, só com as estrelas antigas.
        assertEquals(listOf(Diploma(Trilha.LOGICA, 0)), Conquistas.diplomas(antigo.estrelas))
        assertEquals(11, Conquistas.figurinhasGanhas(antigo.estrelas).size)
        val comTemas = antigo.alternarTema(TemaFavorito.CARROS).comAniversario(Aniversario(1, 1))
        assertEquals(listOf(TemaFavorito.CARROS), comTemas.perfil?.temasFavoritos)
        assertEquals(Aniversario(1, 1), comTemas.perfil?.aniversario)
        assertEquals(EstadoJogo(), EstadoJogo().alternarTema(TemaFavorito.CARROS)) // sem perfil não muda nada
    }
}
