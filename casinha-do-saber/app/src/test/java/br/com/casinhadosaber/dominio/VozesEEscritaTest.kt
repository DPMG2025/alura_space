package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.fases.FasesIngles
import br.com.casinhadosaber.dominio.fases.FasesLetras
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.random.Random

/** Vozes da família (frases graváveis e a escolha entre gravação e voz do aparelho) e escrever com o dedo. */
class VozesEEscritaTest {

    // ------------------------------------------------------------------ vozes da família

    @Test
    fun `frases da familia tem chaves unicas e cerca de vinte frases`() {
        val f = VozesDaFamilia.frases
        assertEquals(f.size, f.map { it.chave }.toSet().size)
        assertTrue(f.size in 18..24)
        assertTrue(f.all { it.texto.isNotBlank() && it.chave.matches(Regex("[a-z_]+")) })
        // Só a saudação é gravada com o nome; o nome nunca está no código.
        assertEquals(listOf("oi_nome"), f.filter { it.comNome }.map { it.chave })
        assertEquals("Oi, Ana!", VozesDaFamilia.textoParaGravar(VozesDaFamilia.frase("oi_nome")!!, "Ana"))
        assertEquals("Oi!", VozesDaFamilia.textoParaGravar(VozesDaFamilia.frase("oi_nome")!!, ""))
    }

    @Test
    fun `elogios incentivos e vitorias sem nome sao exatamente as frases gravaveis`() {
        for (i in 0 until 12) {
            val e = VozesDaFamilia.elogio(null, i)
            assertEquals(FrasesComNome.elogio(null, i), e.trecho.texto)
            assertEquals(e.trecho.texto, VozesDaFamilia.frase(e.chave!!)!!.texto)
            val inc = VozesDaFamilia.incentivo(null, i)
            assertEquals(FrasesComNome.incentivo(null, i), inc.trecho.texto)
            assertEquals(inc.trecho.texto, VozesDaFamilia.frase(inc.chave!!)!!.texto)
        }
        for (estrelas in 2..3) {
            val v = VozesDaFamilia.vitoria(null, estrelas)
            assertEquals(v.trecho.texto, VozesDaFamilia.frase(v.chave!!)!!.texto)
        }
        // Com nome, a voz do aparelho diz o nome; a chave é a mesma.
        assertEquals("Muito bem, Ana!", VozesDaFamilia.elogio("Ana", 0).trecho.texto)
        assertEquals("muito_bem", VozesDaFamilia.elogio("Ana", 0).chave)
        assertEquals("tente_outra_vez", VozesDaFamilia.parte("Tente outra vez!").chave)
        assertEquals(null, VozesDaFamilia.parte("Isso aí!").chave)
    }

    @Test
    fun `saudacao separa o oi do resto`() {
        val p = VozesDaFamilia.saudacao(FrasesComNome.chegada("Ana", 0))
        assertEquals(listOf("Oi, Ana!", "Que bom ver você!"), p.map { it.trecho.texto })
        assertEquals(listOf("oi_nome", "que_bom_te_ver"), p.map { it.chave })
        val sem = VozesDaFamilia.saudacao(FrasesComNome.chegada(null, 1))
        assertEquals(listOf("Olá!", "Vamos brincar e aprender?"), sem.map { it.trecho.texto })
        assertEquals("vamos_brincar", sem[1].chave)
    }

    @Test
    fun `sem gravacao tudo vai para a voz do aparelho como antes`() {
        val partes = listOf(VozesDaFamilia.elogio("Ana", 0), ParteDaFala(pt("Gato. Cat.")), ParteDaFala(en("cat")))
        val plano = VozesDaFamilia.planejar(partes, emptySet())
        assertEquals(listOf(PassoDaFala.Voz(partes.map { it.trecho })), plano)
        // Gravações de outras frases não mudam nada.
        assertEquals(plano, VozesDaFamilia.planejar(partes, setOf("boa_noite", "quase")))
    }

    @Test
    fun `com gravacao a frase toca a gravacao e o resto continua na voz do aparelho`() {
        val partes = listOf(ParteDaFala(pt("Barco.")), VozesDaFamilia.elogio("Ana", 0), ParteDaFala(pt("B de barco.")), ParteDaFala(en("boat")))
        val plano = VozesDaFamilia.planejar(partes, setOf("muito_bem"))
        assertEquals(
            listOf(
                PassoDaFala.Voz(listOf(pt("Barco."))),
                PassoDaFala.Gravacao("muito_bem"),
                PassoDaFala.Voz(listOf(pt("B de barco."), en("boat"))),
            ),
            plano,
        )
        // Duas gravações seguidas (saudação da chegada).
        val chegada = VozesDaFamilia.saudacao("Oi, Ana! Que bom ver você!")
        assertEquals(
            listOf(PassoDaFala.Gravacao("oi_nome"), PassoDaFala.Gravacao("que_bom_te_ver")),
            VozesDaFamilia.planejar(chegada, setOf("oi_nome", "que_bom_te_ver")),
        )
        assertTrue(VozesDaFamilia.planejar(emptyList(), setOf("x")).isEmpty())
    }

    private fun pastaTemporaria(): File = Files.createTempDirectory("vozes").toFile().also { it.deleteOnExit() }

    @Test
    fun `guarda das vozes lembra o conjunto e so lista gravacoes completas`() {
        val pasta = pastaTemporaria()
        val g = GuardaDasVozes(pasta)
        assertEquals(ConjuntoDeVozes.MAMAE, g.ativo)
        assertTrue(g.paraTocar().isEmpty())
        // Gravação em andamento não conta; confirmada, conta.
        g.provisorio(ConjuntoDeVozes.PAPAI, "muito_bem").apply { parentFile.mkdirs(); writeBytes(ByteArray(10)) }
        assertTrue(g.gravadas(ConjuntoDeVozes.PAPAI).isEmpty())
        assertTrue(g.confirmar(ConjuntoDeVozes.PAPAI, "muito_bem"))
        assertEquals(setOf("muito_bem"), g.gravadas(ConjuntoDeVozes.PAPAI))
        assertTrue("o conjunto ativo é o da mamãe", g.paraTocar().isEmpty())
        g.escolher(ConjuntoDeVozes.PAPAI)
        assertEquals(setOf("muito_bem"), g.paraTocar())
        // O índice sobrevive a abrir o app de novo.
        val outra = GuardaDasVozes(pasta)
        assertEquals(ConjuntoDeVozes.PAPAI, outra.ativo)
        outra.ligar(false)
        assertTrue(outra.paraTocar().isEmpty())
        assertFalse(GuardaDasVozes(pasta).ligadas)
        // Gravação vazia não substitui a antiga.
        outra.provisorio(ConjuntoDeVozes.PAPAI, "muito_bem").writeBytes(ByteArray(0))
        assertFalse(outra.confirmar(ConjuntoDeVozes.PAPAI, "muito_bem"))
        assertEquals(setOf("muito_bem"), outra.gravadas(ConjuntoDeVozes.PAPAI))
        outra.apagar(ConjuntoDeVozes.PAPAI, "muito_bem")
        assertTrue(outra.gravadas(ConjuntoDeVozes.PAPAI).isEmpty())
        outra.apagarTudo()
        assertFalse(pasta.exists())
    }

    // ------------------------------------------------------------------ escrever com o dedo

    @Test
    fun `escrever aceita so palavras de A a Z`() {
        EscreverPalavra("BOLA", "⚽")
        for (ruim in listOf("bola", "AVIÃO", "MAÇÃ", "A", "ABACAXIS", "BO LA")) {
            assertFalse(ruim, runCatching { EscreverPalavra(ruim) }.isSuccess)
        }
        assertEquals(listOf("BOLA", "GATO"), Escrita.palavrasParaEscrever(listOf(
            Palavra("⚽", "BOLA", listOf("BO", "LA")), Palavra("🐱", "GATO", listOf("GA", "TO")),
            Palavra("✈️", "AVIÃO", listOf("A", "VI", "ÃO")), Palavra("🍍", "ABACAXI", listOf("A", "BA", "CA", "XI")),
        )).map { it.texto })
    }

    @Test
    fun `escrever aparece nas letras do mundo 6 em diante e as vezes no ingles`() {
        val letras = FasesLetras(Random(2))
        val ondeLetras = (51..250).filter { n -> (0 until 60).any { letras.fase(n).criar().atividade is EscreverPalavra } }
        assertTrue("só em $ondeLetras", ondeLetras.size >= 8)
        assertTrue("começa no meio do caminho: $ondeLetras", ondeLetras.first() in 51..60)
        val ingles = FasesIngles(Random(2))
        val ondeIngles = (51..250).filter { n -> (0 until 60).any { ingles.fase(n).criar().atividade is EscreverPalavra } }
        assertTrue("inglês: $ondeIngles", ondeIngles.size in 1..4)
        for (n in ondeLetras + ondeIngles) repeat(40) {
            val p = (if (n in ondeLetras) letras else ingles).fase(n).criar()
            val a = p.atividade as? EscreverPalavra ?: return@repeat
            assertTrue("fase $n: ${a.palavra}", a.palavra.length in 3..5 && Escrita.podeEscrever(a.palavra))
        }
    }

    @Test
    fun `fases com escrever seguem as regras de variedade`() {
        for (semente in 0 until 4) {
            val g = GeradorDeFases(Random(500 + semente))
            for (t in listOf(Trilha.PORTUGUES, Trilha.INGLES)) for (fase in g.fases(t).drop(50)) {
                val perguntas = g.perguntas(fase)
                if (perguntas.none { it.atividade is EscreverPalavra }) continue
                assertTrue("$fase: mais de 2 escritas", perguntas.count { it.atividade is EscreverPalavra } <= 2)
                assertTrue("$fase: regras", GeradorDeFases.regrasDa(fase).cumpre(perguntas))
            }
        }
    }

    // ------------------------------------------------------------------ meu caderno

    private fun pagina(palavra: String, dia: Long) = PaginaDoCaderno(
        palavra,
        dia,
        palavra.map { listOf(listOf(Ponto(0.2f, 0.15f), Ponto(0.5f, 0.85f)), listOf(Ponto(0.3f, 0.5f), Ponto(0.7f, 0.5f))) },
    )

    @Test
    fun `caderno guarda as ultimas cem palavras e le de volta`() {
        var paginas = emptyList<PaginaDoCaderno>()
        for (i in 0 until 130) paginas = Caderno.adicionar(paginas, pagina("BOLA", 20_000L + i))
        assertEquals(Caderno.MAXIMO, paginas.size)
        assertEquals(20_030L, paginas.first().dia)
        val lidas = Caderno.ler(Caderno.escrever(paginas))
        assertEquals(paginas.size, lidas.size)
        assertEquals("BOLA", lidas.last().palavra)
        assertEquals(4, lidas.last().letras.size)
        val p = lidas.last().letras[0][0]
        assertEquals(0.2f, p[0].x, 0.011f)
        assertEquals(0.85f, p[1].y, 0.011f)
    }

    @Test
    fun `caderno pula linhas estragadas e simplifica tracos compridos`() {
        val texto = "BOLA|20000|10,20,30,40\nlixo\n|1|\nGATO|x|1,2\nSOL|20001|\n"
        assertEquals(listOf("BOLA", "SOL"), Caderno.ler(texto).map { it.palavra })
        val longo = List(200) { Ponto(it / 199f, 0.5f) }
        val s = Caderno.simplificar(longo)
        assertEquals(Caderno.PONTOS_POR_TRACO, s.size)
        assertEquals(longo.first(), s.first())
        assertEquals(longo.last(), s.last())
    }

    @Test
    fun `caderno em arquivo guarda e apaga`() {
        val pasta = pastaTemporaria()
        val c = CadernoEmArquivo.em(pasta)
        assertTrue(c.ler().isEmpty())
        c.guardar(pagina("GATO", 20_000L))
        c.guardar(pagina("SAPO", 20_001L))
        assertEquals(listOf("GATO", "SAPO"), CadernoEmArquivo.em(pasta).ler().map { it.palavra })
        c.apagarTudo()
        assertTrue(c.ler().isEmpty())
    }

    @Test
    fun `leitor com modelos simples acha a letra certa`() {
        val modelos = mapOf(
            'L' to listOf(listOf(listOf(Ponto(0f, 0f), Ponto(0f, 1f), Ponto(0.6f, 1f)))),
            'O' to listOf(listOf(List(25) { i -> val a = i / 24.0 * 2 * Math.PI; Ponto((0.4 * Math.cos(a)).toFloat(), (0.5 * Math.sin(a)).toFloat()) })),
            'I' to listOf(listOf(listOf(Ponto(0f, 0f), Ponto(0f, 1f)))),
        )
        val leitor = LeitorDeLetras(modelos)
        val l = listOf(listOf(Ponto(10f, 10f), Ponto(10f, 110f), Ponto(70f, 112f)))
        assertTrue(leitor.ler(l, 'L').aceita)
        assertEquals('L', leitor.ler(l, 'O').melhor)
        assertFalse(leitor.ler(l, 'O').aceita)
        // O traço ao contrário (de baixo para cima) também vale.
        assertTrue(leitor.ler(listOf(l[0].reversed()), 'L').aceita)
    }
}
