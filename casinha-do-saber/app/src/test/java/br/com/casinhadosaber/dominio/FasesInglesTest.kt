package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.fases.FasesIngles
import br.com.casinhadosaber.dominio.fases.VocabularioIngles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** As fases 51 a 250 do caminho Inglês (mundos 6 a 25): perguntas válidas, voz certa e progressão. */
class FasesInglesTest {

    private val sorteios = 200

    /** Mundo (1 a 25) da fase [n] (1 a 250). */
    private fun mundo(n: Int) = (n - 1) / 10 + 1

    /** Letras do português que nunca aparecem numa fala em inglês. */
    private val letrasDoPortugues = "ãõçáéíóúâêôàÃÕÇÁÉÍÓÚÂÊÔ"

    private fun tipo(p: Pergunta) = p.atividade::class.simpleName.orEmpty()

    /** Todas as falas de uma pergunta: instrução, reforço, opções e as falas de dentro da atividade. */
    private fun falas(p: Pergunta): List<Trecho> {
        val dentro: List<Trecho> = when (val a = p.atividade) {
            is Pescaria -> a.peixes.flatMap { it.fala }
            is EstourarBaloes -> a.baloes.flatMap { it.fala }
            is LigarPares -> (a.esquerda + a.direita).flatMap { it.fala }
            is SigaASequencia -> a.botoes.flatMap { it.fala }
            is CacaNaCena -> a.itens.flatMap { it.opcao.fala }
            is CartasViradas -> a.cartas.flatMap { it.fala }
            is Trem -> (a.vagoes + a.extras).flatMap { it.fala }
            is EscondeEsconde -> a.esconderijos.flatMap { it.fala }
            is Domino -> (listOf(a.ponta) + a.mao.flatMap { listOf(it.a, it.b) }).flatMap { it.fala }
            is PintarPorCodigo -> a.legenda.flatMap { it.fala }
            is Atividade.Memoria -> a.falas.values.toList()
            else -> emptyList()
        }
        return p.fala + p.reforco + p.opcoes.flatMap { it.fala } + dentro
    }

    private fun validar(n: Int, p: Pergunta) {
        val onde = "fase $n (${p.instrucao})"
        assertTrue("$onde: sem fala", p.fala.isNotEmpty() && p.fala.all { it.texto.isNotBlank() })
        assertTrue("$onde: instrução vazia", p.instrucao.isNotBlank())
        if (p.atividade == Atividade.Escolha) {
            assertEquals("$onde: 4 opções", 4, p.opcoes.size)
            assertTrue("$onde: resposta certa fora", p.indiceCorreto in p.opcoes.indices)
            assertEquals("$onde: opções repetidas ${p.opcoes}", 4, p.opcoes.map { it.visual }.toSet().size)
        }
        (p.cena as? Cena.Palavra)?.texto?.takeIf { it.trim().split(' ').size >= 3 }?.let {
            assertFalse("$onde: o balão de cima entrega a frase \"$it\"", p.instrucao.contains(it, ignoreCase = true))
        }
        when (val a = p.atividade) {
            is MontarFrase -> assertEquals("$onde: frase em inglês com voz em português", Idioma.EN, a.idioma)
            is Ditado -> assertEquals("$onde: ditado em inglês com voz em português", Idioma.EN, a.idioma)
            // O grande chefão final (250) traz perguntas de todos os mundos, cada uma com a cena do seu mundo.
            is CacaNaCena -> if (n < 250) assertEquals("$onde: cena de outro mundo", mundo(n) - 1, a.mundo)
            else -> Unit
        }
        val todas = falas(p)
        assertTrue("$onde: nada é falado em inglês", todas.any { it.idioma == Idioma.EN })
        for (t in todas.filter { it.idioma == Idioma.EN }) {
            assertTrue("$onde: \"${t.texto}\" marcado como inglês tem letra do português", t.texto.none { it in letrasDoPortugues })
        }
    }

    @Test
    fun `fases 51 a 250 sao validas em duzentos sorteios`() {
        val fases = FasesIngles(Random(51))
        for (n in 51..250) {
            val m = fases.fase(n)
            assertTrue("fase $n sem nome", m.titulo.isNotBlank() && m.emoji.isNotBlank())
            repeat(sorteios) { validar(n, m.criar()) }
        }
    }

    @Test
    fun `palavras e frases inglesas do vocabulario sao faladas com a voz em ingles`() {
        val frasesEn = (VocabularioIngles.haUmNoComodo + VocabularioIngles.meuSeu + VocabularioIngles.fazendoAgora +
            VocabularioIngles.podeNaoPode + VocabularioIngles.quemTrabalhaOnde).map { it.en.lowercase() }
        val fases = FasesIngles(Random(7))
        for (n in 51..250) repeat(sorteios / 4) {
            val p = fases.fase(n).criar()
            for (t in falas(p).filter { it.idioma == Idioma.PT }) {
                val texto = t.texto.lowercase()
                assertFalse("fase $n: frase em inglês com voz em português: \"${t.texto}\"", frasesEn.any { it in texto })
            }
            // A frase para montar é sempre falada em inglês quando aparece na instrução.
            (p.atividade as? MontarFrase)?.let { a ->
                p.fala.filter { it.texto.startsWith(a.frase) }.forEach { assertEquals("fase $n", Idioma.EN, it.idioma) }
            }
        }
        assertEquals("forty-two", VocabularioIngles.numeroEn(42))
        assertEquals("one hundred", VocabularioIngles.numeroEn(100))
        assertEquals("thirteen", VocabularioIngles.numeroEn(13))
        assertEquals("balls", VocabularioIngles.plural("ball"))
        assertEquals("boxes", VocabularioIngles.plural("box"))
    }

    @Test
    fun `instrucao passa do portugues para o ingles aos poucos`() {
        val fases = FasesIngles(Random(3))
        for (n in 51..249) repeat(40) {
            val p = fases.fase(n).criar()
            val m = mundo(n)
            when {
                m <= 12 -> assertTrue("fase $n: mundo $m deveria ter instrução em português: ${p.fala}", p.fala.any { it.idioma == Idioma.PT })
                m >= 19 -> assertEquals("fase $n: mundo $m deveria começar em inglês: ${p.fala}", Idioma.EN, p.fala.first().idioma)
            }
        }
    }

    @Test
    fun `cada mundo usa pelo menos quatro tipos e o chefao mistura tres desafios`() {
        val fases = FasesIngles(Random(11))
        for (mundo in 6..25) {
            val tiposDoMundo = mutableSetOf<String>()
            for (n in (mundo - 1) * 10 + 1..mundo * 10) {
                val tipos = List(80) { tipo(fases.fase(n).criar()) }.toSet()
                tiposDoMundo += tipos
                assertTrue("fase $n usa um tipo só: $tipos", tipos.size >= 2)
            }
            assertTrue("mundo $mundo com poucos tipos: $tiposDoMundo", tiposDoMundo.size >= 4)
            val chefao = mundo * 10
            val tiposDoChefao = List(120) { tipo(fases.fase(chefao).criar()) }.toSet()
            assertTrue("chefão $chefao: $tiposDoChefao", tiposDoChefao.size >= 3)
        }
    }

    @Test
    fun `os desafios novos aparecem no caminho ingles`() {
        val fases = FasesIngles(Random(5))
        val vistos = mutableSetOf<String>()
        for (n in 51..250) repeat(30) { vistos += tipo(fases.fase(n).criar()) }
        val esperados = setOf("Pescaria", "EstourarBaloes", "LigarPares", "CacaNaCena", "MontarFrase", "Ditado", "SigaASequencia",
            "Domino", "EscondeEsconde", "CartasViradas", "Trem", "PintarPorCodigo", "QuebraCabeca", "ProgramarRobo", "Mercadinho")
        assertTrue("faltam ${esperados - vistos}", vistos.containsAll(esperados))
    }

    @Test
    fun `vizinhas tem nomes diferentes e a partida tem cinco perguntas sem tres desafios iguais seguidos`() {
        val fases = FasesIngles(Random(1))
        for (n in 52..250) assertTrue("fases ${n - 1} e $n com o mesmo nome", fases.fase(n).titulo != fases.fase(n - 1).titulo)
        val g = GeradorDeFases(Random(77))
        val lista = g.fases(Trilha.INGLES)
        repeat(10) {
            for (f in lista.drop(50)) {
                val perguntas = g.perguntas(f)
                assertEquals("$f", 5, perguntas.size)
                val tipos = perguntas.map { GeradorDeFases.tipoDeMexer(it) }
                for (i in 2 until tipos.size) {
                    assertFalse("$f: 3 iguais $tipos", tipos[i] != null && tipos[i] == tipos[i - 1] && tipos[i] == tipos[i - 2])
                }
            }
        }
    }
}
