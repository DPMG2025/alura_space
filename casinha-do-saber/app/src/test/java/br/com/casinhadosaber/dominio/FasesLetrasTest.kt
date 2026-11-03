package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.fases.FasesLetras
import br.com.casinhadosaber.dominio.fases.VocabularioLetras
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** As fases 51 a 250 do caminho Letras (mundos 6 a 25, plano em docs/plano-1000-fases.md, seção 4.2). */
class FasesLetrasTest {

    private val L = VocabularioLetras
    private val novas = 51..250

    private fun tipo(p: Pergunta) = p.atividade::class.simpleName ?: "?"

    @Test
    fun `cada fase de 51 a 250 gera cinco perguntas validas em 200 sorteios`() {
        for (semente in 0 until 200) {
            val g = GeradorDeFases(Random(50_000 + semente))
            val fases = g.fases(Trilha.PORTUGUES)
            for (n in novas) {
                val fase = fases[n - 1]
                val perguntas = g.perguntas(fase)
                assertEquals("$fase", 5, perguntas.size)
                for (p in perguntas) validar(fase, p)
                val tipos = perguntas.map { GeradorDeFases.tipoDeMexer(it) }
                for (i in 2 until tipos.size) {
                    assertFalse("$fase: 3 iguais seguidas $tipos", tipos[i] != null && tipos[i] == tipos[i - 1] && tipos[i] == tipos[i - 2])
                }
            }
        }
    }

    private fun textoDe(o: Opcao) = when (val v = o.visual) {
        is Visual.Texto -> v.texto
        is Visual.Emoji -> v.emoji
        else -> v.toString()
    }

    private fun distintas(fase: Fase, nome: String, opcoes: List<Opcao>, comFala: Boolean = true) {
        assertEquals("$fase: $nome repetidos $opcoes", opcoes.size, opcoes.map { it.visual }.toSet().size)
        assertTrue("$fase: $nome vazio", opcoes.all { textoDe(it).isNotBlank() })
        // Nos desafios novos cada peça diz o próprio nome quando a criança toca (as perguntas de revisão das fases 1 a 50 podem não ter).
        if (comFala) assertTrue("$fase: $nome sem fala", opcoes.all { o -> o.fala.isNotEmpty() && o.fala.all { it.texto.isNotBlank() } })
    }

    private fun validar(fase: Fase, p: Pergunta) {
        assertTrue("$fase: sem fala", p.fala.isNotEmpty() && p.fala.all { it.texto.isNotBlank() })
        for (t in p.fala + p.reforco) assertFalse("$fase: fala com lacuna: ${t.texto}", "__" in t.texto)
        when (val a = p.atividade) {
            Atividade.Escolha -> {
                assertEquals("$fase: 4 opções em ${p.instrucao}", 4, p.opcoes.size)
                distintas(fase, "opções de ${p.instrucao}", p.opcoes, comFala = false)
            }
            is Atividade.MontarPalavra -> {
                assertEquals(a.palavra, a.silabas.joinToString(""))
                assertTrue("$fase: extra igual a peça", a.extras.none { it in a.silabas })
            }
            is Atividade.Tracar -> assertTrue(a.letra.length == 1)
            is Atividade.Memoria, is Atividade.Cesta -> Unit
            is Pescaria -> distintas(fase, "peixes", a.peixes)
            is EstourarBaloes -> distintas(fase, "balões", a.baloes)
            is LigarPares -> {
                distintas(fase, "esquerda", a.esquerda)
                distintas(fase, "direita", a.direita)
            }
            is QuebraCabeca -> assertEquals("$fase: rótulos repetidos ${a.rotulos}", a.rotulos.size, a.rotulos.toSet().size)
            is Trem -> distintas(fase, "vagões", a.vagoes + a.extras)
            is CartasViradas -> distintas(fase, "cartas", a.cartas)
            is SigaASequencia -> distintas(fase, "botões", a.botoes)
            is CacaNaCena -> {
                distintas(fase, "figuras da cena", a.itens.map { it.opcao })
                // A cena é a do mundo da fase (ou de um mundo anterior, na pergunta de revisão).
                assertTrue("$fase: cena de outro mundo ${a.mundo}", a.mundo in 5..Mundos.de(fase).indice)
            }
            is Ditado -> {
                assertTrue("$fase: ditado vazio", a.palavra.isNotBlank() && a.extras.distinct().size == a.extras.size)
                assertTrue("$fase: a instrução entrega a palavra", a.palavra !in p.instrucao.uppercase())
            }
            is EscreverPalavra -> {
                assertTrue("$fase: palavra para escrever ${a.palavra}", a.palavra.length in Escrita.TAMANHOS && Escrita.podeEscrever(a.palavra))
                assertTrue("$fase: a instrução entrega a palavra", a.palavra !in p.instrucao.uppercase())
                assertTrue("$fase: a fala não diz a palavra", p.fala.any { a.palavra.lowercase() in it.texto })
            }
            is Domino -> assertEquals(a.jogadas, a.ordem.size)
            is MontarFrase -> {
                assertEquals("$fase: palavra repetida ${a.palavras}", a.palavras.size, a.palavras.toSet().size)
                assertTrue("$fase: frase sem maiúscula ${a.frase}", a.palavras.first()[0].isUpperCase())
                assertTrue("$fase: frase sem sinal ${a.frase}", a.frase.last() in ".!?")
            }
            else -> org.junit.Assert.fail("$fase: tipo inesperado $a")
        }
    }

    /** Quantas vezes cada tipo de atividade sai em [n] sorteios da fase. */
    private fun tiposDaFase(numero: Int, n: Int = 300): Map<String, Int> {
        val f = FasesLetras(Random(numero)).fase(numero)
        return List(n) { tipo(f.criar()) }.groupingBy { it }.eachCount()
    }

    @Test
    fun `fases misturam atividades e vizinhas tem atividade principal diferente`() {
        val porFase = novas.associateWith { tiposDaFase(it) }
        val principal = porFase.mapValues { (_, c) -> c.maxBy { it.value }.key }
        for (n in novas) {
            val tipos = porFase.getValue(n)
            assertTrue("fase $n só tem $tipos", tipos.size >= 2)
            assertTrue("fase $n sem desafio de mexer: $tipos", tipos.keys.any { it != "Escolha" })
            if (n > 51 && n != 250) assertNotEquals("fases ${n - 1} e $n com a mesma atividade principal", principal.getValue(n - 1), principal.getValue(n))
        }
        for (mundo in 6..25) {
            val fases = (mundo - 1) * 10 + 1..mundo * 10
            val doMundo = fases.flatMap { porFase.getValue(it).keys }.toSet()
            assertTrue("mundo $mundo com poucos tipos: $doMundo", doMundo.size >= 4)
            assertTrue("chefão do mundo $mundo: ${porFase.getValue(mundo * 10)}", porFase.getValue(mundo * 10).size >= 3)
            // Fase 1 de cada mundo apresenta a ideia com tocar.
            assertEquals("fase 1 do mundo $mundo", "Escolha", principal.getValue(fases.first))
        }
        assertEquals(200, novas.map { FasesLetras(Random(0)).fase(it).titulo }.size)
        assertTrue(novas.all { FasesLetras(Random(0)).fase(it).titulo.none { c -> c == '-' || c == '—' } })
    }

    /**
     * Tamanho do que a criança lê ou escreve na pergunta, em letras: a palavra do ditado, a frase de
     * montar, a palavra ou frase em destaque, o texto do balão (frase, bilhete, história) e as respostas escritas.
     */
    private fun tamanhoDoConteudo(p: Pergunta): Double {
        val opcoes = when (val a = p.atividade) {
            Atividade.Escolha -> p.opcoes
            is Pescaria -> a.peixes
            is EstourarBaloes -> a.baloes
            is LigarPares -> a.esquerda + a.direita
            is Trem -> a.vagoes
            is CartasViradas -> a.cartas
            is SigaASequencia -> a.botoes
            else -> emptyList()
        }
        val escritas = opcoes.mapNotNull { (it.visual as? Visual.Texto)?.texto?.length }
        val texto = when (val a = p.atividade) {
            is Ditado -> a.palavra.length
            is EscreverPalavra -> a.palavra.length
            is Atividade.MontarPalavra -> a.palavra.length
            is MontarFrase -> a.frase.length
            is QuebraCabeca -> a.rotulos.joinToString(" ").length
            else -> 0
        }
        val destaque = (p.cena as? Cena.Palavra)?.texto?.length ?: 0
        val balao = if (p.instrucao.contains('.') || p.instrucao.contains('_')) p.instrucao.length else 0
        return texto + destaque + balao + (if (escritas.isEmpty()) 0.0 else escritas.average())
    }

    private fun silabasDe(p: Pergunta): Int? = when (val a = p.atividade) {
        is Ditado -> if (a.partes.all { it.length == 1 }) null else a.partes.size
        is Atividade.MontarPalavra -> a.silabas.size
        is QuebraCabeca -> if (a.rotulos.any { it.first().isUpperCase() && it.length > 4 }) null else a.pecas
        else -> null
    }

    @Test
    fun `a dificuldade sobe de faixa em faixa`() {
        val faixas = listOf(6..10, 11..15, 16..20, 21..25)
        val tamanho = mutableListOf<Double>()
        val silabas = mutableListOf<Double>()
        for (faixa in faixas) {
            val t = mutableListOf<Double>()
            val s = mutableListOf<Int>()
            for (mundo in faixa) for (n in (mundo - 1) * 10 + 1..mundo * 10) {
                val f = FasesLetras(Random(n)).fase(n)
                repeat(60) { f.criar().let { p -> t += tamanhoDoConteudo(p); silabasDe(p)?.let(s::add) } }
            }
            tamanho += t.average()
            if (s.size >= 50) silabas += s.average()
        }
        println("Letras: tamanho médio do que se lê por faixa $tamanho, sílabas médias $silabas")
        // Palavras (mundos 6 a 10), depois palavras maiores e frases (11 a 15), depois frases e textos (16 a 25).
        assertTrue("tamanho do texto: $tamanho", tamanho[0] < tamanho[1] && tamanho[1] < tamanho[2] && tamanho[1] < tamanho[3])
        assertTrue("tamanho do texto: $tamanho", tamanho[0] + tamanho[1] < tamanho[2] + tamanho[3])
        for (i in 1 until silabas.size) assertTrue("número médio de sílabas caiu: $silabas", silabas[i] >= silabas[i - 1] - 0.05)
        // Do mundo 6 ao 14 a criança lê palavras; do 15 em diante, frases e textos.
        val frasesAte14 = (51..140).sumOf { n -> val f = FasesLetras(Random(n)).fase(n); (0 until 30).count { f.criar().atividade is MontarFrase } }
        val frasesDepois = (141..250).sumOf { n -> val f = FasesLetras(Random(n)).fase(n); (0 until 30).count { f.criar().atividade is MontarFrase } }
        assertEquals(0, frasesAte14)
        assertTrue(frasesDepois > 200)
    }

    // ------------------------------------------------------------------ escrita das palavras

    private val todasAsListas: Map<String, List<Palavra>> = mapOf(
        "cv2" to L.cv2, "cv3" to L.cv3, "cv4" to L.cv4, "fazenda" to L.fazenda, "nasaisAo" to L.nasaisAo, "nasaisN" to L.nasaisN,
        "ditongos" to L.ditongos, "travadasR" to L.travadasR, "travadasS" to L.travadasS, "travadasL" to L.travadasL,
        "rr" to L.rr, "rFraco" to L.rFraco, "ss" to L.ss, "qu" to L.qu, "gu" to L.gu, "cedilha" to L.cedilha,
        "cSuave" to L.cSuave, "gSuave" to L.gSuave, "jota" to L.jota, "sComSomDeZ" to L.sComSomDeZ, "zeta" to L.zeta,
        "encontrosL" to L.encontrosL, "encontrosR" to L.encontrosR, "xis" to L.xis, "che" to L.che, "aga" to L.aga,
        "semAga" to L.semAga, "mpmb" to L.mpmb, "nAntes" to L.nAntes, "mFim" to L.mFim, "acentos" to L.acentos,
        "forteNoFim" to L.forteNoFim, "forteNoMeio" to L.forteNoMeio, "forteNoComeco" to L.forteNoComeco, "lOuU" to L.lOuU,
    ) + L.dicionario.mapIndexed { i, l -> "dicionario$i" to l } + L.dicionarioTerceira.mapIndexed { i, l -> "terceira$i" to l } +
        L.rimas.mapIndexed { i, l -> "rimas$i" to l } + L.trocas.mapIndexed { i, l -> "trocas$i" to l }

    private val vogais = "AEIOUÁÉÍÓÚÂÊÔÃÕ"
    private val acentosGraficos = "ÁÉÍÓÚÂÊÔ"

    /** Regras de escrita do português que valem para todas as palavras das listas. */
    private fun erroDeGrafia(w: String): String? {
        val r = listOf(
            Regex("^[A-ZÁÉÍÓÚÂÊÔÃÕÇ]+$") to "letra estranha",
            Regex("^(?!.*Ç[EI])") to "Ç antes de E ou I",
            Regex("^(?!Ç)") to "começa com Ç",
            Regex("^(?!.*N[PB])") to "N antes de P ou B",
            Regex("^(?!.*M[^PBAEIOUÁÉÍÓÚÂÊÔÃÕ])") to "M antes de consoante que não é P nem B",
            Regex("^(?!RR|SS)") to "começa com RR ou SS",
            Regex("^(?!.*(RR|SS)$)") to "termina com RR ou SS",
            Regex("^(?!.*Q[^U])") to "Q sem U",
            Regex("^(?!.*Q$)") to "termina com Q",
            Regex("^(?!.*N$)") to "termina com N",
            Regex("^(?!.*[^CLN]H)") to "H no meio sem ser CH, LH ou NH",
            Regex("^(?!.*([^AEIOUÁÉÍÓÚÂÊÔÃÕRSLMNZ])$)") to "termina com consoante que o português não usa no fim",
        )
        for ((re, erro) in r) if (!re.containsMatchIn(w)) return erro
        if (w.count { it in acentosGraficos } > 1) return "dois acentos"
        if (w.count { it in "ÃÕ" } > 1) return "dois tis"
        return null
    }

    @Test
    fun `palavras do vocabulario escritas certinho e separadas em silabas`() {
        for ((nome, lista) in todasAsListas) {
            assertEquals("$nome com figura repetida", lista.size, lista.map { it.emoji }.toSet().size)
            assertEquals("$nome com palavra repetida", lista.size, lista.map { it.texto }.toSet().size)
            for (p in lista) {
                assertEquals("$nome: ${p.texto}", p.texto, p.silabas.joinToString(""))
                assertEquals("$nome: ${p.texto} com erro: ${erroDeGrafia(p.texto)}", null, erroDeGrafia(p.texto))
                for (s in p.silabas) {
                    assertTrue("$nome: sílaba sem vogal $s em ${p.texto}", s.any { it in vogais })
                    assertFalse("$nome: RR ou SS na mesma sílaba em ${p.texto}", "RR" in s || "SS" in s)
                    assertTrue("$nome: fala da sílaba $s", L.falaDaSilaba(s).isNotBlank())
                }
                // RR e SS só entre vogais, e divididos entre duas sílabas (CAR-RO, PÁS-SA-RO).
                for (d in listOf("RR", "SS")) {
                    val i = p.texto.indexOf(d)
                    if (i >= 0) assertTrue("$nome: $d fora de lugar em ${p.texto}", p.texto[i - 1] in vogais && p.texto[i + 2] in vogais)
                }
            }
        }
        // A sílaba forte de cada lista bate com o acento: proparoxítonas sempre têm acento na primeira sílaba.
        assertTrue(L.forteNoComeco.all { it.silabas.size == 3 && it.silabas[0].any { c -> c in acentosGraficos } })
        assertTrue(L.forteNoMeio.all { it.silabas.size == 3 && it.texto.none { c -> c in acentosGraficos + "ÃÕ" } && it.texto.last() in "AEO" })
        assertTrue(L.forteNoFim.all { it.silabas.size == 3 && (it.silabas[2].any { c -> c in acentosGraficos + "Ã" } || it.texto.last() in "LRZ") })
        assertTrue(L.acentos.all { it.texto.any { c -> c in acentosGraficos } })
        assertTrue(L.semAcento.none { it.texto.any { c -> c in acentosGraficos } })
    }

    @Test
    fun `listas de regras de escrita tem o que dizem ter`() {
        assertTrue(L.nasaisAo.all { it.texto.contains('Ã') })
        assertTrue(L.nasaisN.all { p -> p.silabas.any { it.length >= 2 && it.last() == 'N' } })
        assertTrue(L.rr.all { "RR" in it.texto } && L.rFraco.none { "RR" in it.texto })
        assertTrue(L.ss.all { "SS" in it.texto } && L.qu.all { "QU" in it.texto } && L.gu.all { "GU" in it.texto })
        assertTrue(L.cedilha.all { 'Ç' in it.texto } && L.cSuave.none { 'Ç' in it.texto })
        assertTrue(L.cSuave.all { Regex("C[EI]").containsMatchIn(it.texto) } && L.gSuave.all { Regex("G[EI]").containsMatchIn(it.texto) })
        assertTrue(L.sComSomDeZ.all { Regex("[$vogais]S[$vogais]").containsMatchIn(it.texto) })
        assertTrue(L.xis.all { 'X' in it.texto } && L.che.all { "CH" in it.texto } && L.aga.all { it.texto.startsWith("H") })
        assertTrue(L.mpmb.all { Regex("M[PB]").containsMatchIn(it.texto) } && L.nAntes.none { Regex("M[PB]").containsMatchIn(it.texto) })
        assertTrue(L.mFim.all { it.texto.endsWith("M") })
        assertTrue((L.travadasR + L.travadasS + L.travadasL).all { p -> p.silabas.any { it.last() in "RSL" } })
        assertTrue((L.encontrosL + L.encontrosR).all { p -> p.silabas.any { it.length >= 3 && it[0] !in vogais && it[1] in "LR" } })
        // Ordem do dicionário: cada grupo com a mesma primeira letra e as segundas (ou terceiras) diferentes.
        for (g in L.dicionario) assertEquals(g.size, g.map { it.texto[1] }.toSet().size)
        for (g in L.dicionarioTerceira) assertEquals(g.size, g.map { it.texto[2] }.toSet().size)
        assertTrue(L.dicionario.all { g -> g.map { it.inicial }.toSet().size == 1 })
        assertTrue(L.dicionarioTerceira.all { g -> g.map { it.texto.take(2) }.toSet().size == 1 })
    }

    @Test
    fun `frases e textos com maiuscula, sinal e sem palavra repetida`() {
        val frases = L.frasesIlha + L.frasesDoces + L.frasesLongas + L.versos + L.frasesComConectivo
        for (f in frases) {
            assertTrue(f.texto, f.texto[0].isUpperCase() && f.texto.last() in ".?!")
            assertFalse(f.texto, "  " in f.texto)
            for (w in f.palavras) {
                val limpa = w.trimEnd('.', '!', '?', ',')
                if (limpa.lowercase().replaceFirstChar { it.uppercase() } in L.nomes) assertTrue("nome sem maiúscula: ${f.texto}", limpa in L.nomes)
            }
        }
        assertTrue(L.frasesDoces.map { it.sinal }.toSet() == setOf('.', '?', '!'))
        assertTrue(L.frasesComNome.size >= 5)
        for (t in L.bilhetes + L.historias + L.textosDoDia) for (q in t.perguntas) {
            assertEquals(t.texto, 3, q.erradas.size)
            assertEquals(t.texto, 4, (q.erradas + q.certa).toSet().size)
        }
        for (q in L.versosParaCompletar + L.conectivos) {
            assertTrue(q.pergunta, "___" in q.pergunta)
            assertEquals(4, (q.erradas + q.certa).toSet().size)
        }
        for (c in L.concordancias) assertEquals(4, c.verbo.toSet().size)
        for (h in L.historiasEmQuadros) assertEquals(3, h.quadros.map { it.first }.toSet().size)
        for (par in L.antonimos + L.sinonimos + L.pluraisFaceis + L.pluraisDificeis + L.masculinoFeminino) {
            assertNotEquals(par.a, par.b)
            assertEquals(null, erroDeGrafia(par.a) ?: erroDeGrafia(par.b))
        }
        // Cada palavra aparece num par só, para nunca haver duas respostas certas.
        for (lista in listOf(L.antonimos, L.sinonimos, L.masculinoFeminino)) {
            val ws = lista.flatMap { listOf(it.a, it.b) }
            assertEquals(ws.size, ws.toSet().size)
        }
    }

    @Test
    fun `narracao de silabas e letras soa como na escola`() {
        assertEquals("dê", L.falaDaSilaba("DE"))
        assertEquals("dô", L.falaDaSilaba("DO"))
        assertEquals("quê", L.falaDaSilaba("QUE"))
        assertEquals("guê", L.falaDaSilaba("GUE"))
        assertEquals("sá", L.falaDaSilaba("ÇA"))
        assertEquals("ba", L.falaDaSilaba("BA"))
        assertEquals("cê cedilha", L.falaDaLetra("Ç"))
        assertEquals("bê", L.falaDaLetra("B"))
    }
}
