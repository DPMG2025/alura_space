package br.com.casinhadosaber.dominio

import br.com.casinhadosaber.dominio.fases.FasesDoCaminho
import br.com.casinhadosaber.dominio.fases.FasesIngles
import br.com.casinhadosaber.dominio.fases.FasesLetras
import br.com.casinhadosaber.dominio.fases.FasesLogica
import br.com.casinhadosaber.dominio.fases.FasesNumeros
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Variedade dentro da partida (pendências do revisor no PRD): nenhuma fase dos mundos novos repete o mesmo
 * desafio de mexer mais de 2 vezes, toda fase dos mundos novos tem pelo menos 3 jeitos de jogar, os chefões
 * dos mundos 6 a 25 têm no máximo 40% de perguntas de tocar e o pintar cabe num celular de 360 dp.
 */
class VariedadeDasFasesTest {

    private val sementes = listOf(1L, 22L, 333L, 4444L, 55555L, 666666L)

    private fun tipo(p: Pergunta) = GeradorDeFases.tipoDeMexer(p) ?: "Tocar"

    /** Joga cada uma das 1000 fases com várias sementes (e com temas favoritos, que mudam o sorteio). */
    private fun cadaPartida(bloco: (Fase, List<Pergunta>) -> Unit) {
        for (s in sementes) {
            val temas = if (s % 2 == 0L) TemaFavorito.entries.take(2) else emptyList()
            val g = GeradorDeFases(Random(s), temas)
            for (t in Trilha.entries) for (fase in g.fases(t)) bloco(fase, g.perguntas(fase))
        }
    }

    @Test
    fun `nenhuma fase dos mundos novos repete o mesmo desafio de mexer mais de duas vezes`() {
        var pior = 0
        cadaPartida { fase, perguntas ->
            assertEquals("$fase", GeradorDeFases.PERGUNTAS_POR_FASE, perguntas.size)
            if (fase.numero < FasesDoCaminho.FASES_PRONTAS) return@cadaPartida
            val maisRepetido = perguntas.mapNotNull { GeradorDeFases.tipoDeMexer(it) }.groupingBy { it }.eachCount().maxByOrNull { it.value }
            pior = maxOf(pior, maisRepetido?.value ?: 0)
            assertTrue("$fase: ${maisRepetido?.key} ${maisRepetido?.value} vezes em ${perguntas.map(::tipo)}", (maisRepetido?.value ?: 0) <= 2)
        }
        assertEquals("algum desafio deveria aparecer duas vezes em alguma fase", 2, pior)
    }

    @Test
    fun `toda fase dos mundos novos tem pelo menos tres jeitos de jogar`() {
        cadaPartida { fase, perguntas ->
            if (fase.numero < FasesDoCaminho.FASES_PRONTAS) return@cadaPartida
            val tipos = perguntas.map(::tipo)
            assertTrue("$fase: só ${tipos.toSet()} em $tipos", tipos.toSet().size >= 3)
        }
    }

    @Test
    fun `chefoes dos mundos 6 a 25 tem no maximo 40 por cento de tocar e tres desafios de mexer diferentes`() {
        var toques = 0
        var total = 0
        cadaPartida { fase, perguntas ->
            if (fase.numero < FasesDoCaminho.FASES_PRONTAS || !Mundos.ehChefao(fase)) return@cadaPartida
            val tipos = perguntas.map(::tipo)
            val deTocar = tipos.count { it == "Tocar" }
            toques += deTocar
            total += tipos.size
            assertTrue("$fase: $deTocar de ${tipos.size} de tocar em $tipos", deTocar * 100 <= 40 * tipos.size)
            assertTrue("$fase: poucos desafios de mexer em $tipos", tipos.filter { it != "Tocar" }.toSet().size >= 3)
        }
        assertEquals(20 * 4 * sementes.size * GeradorDeFases.PERGUNTAS_POR_FASE, total)
        assertTrue("chefões com tocar demais: $toques de $total", toques * 100 <= 40 * total)
    }

    @Test
    fun `o grande chefao final de cada caminho tem desafios de mexer variados`() {
        for (s in sementes) {
            val g = GeradorDeFases(Random(s))
            for (t in Trilha.entries) {
                val final = g.fases(t).last()
                val desafios = mutableSetOf<String>()
                repeat(5) {
                    val tipos = g.perguntas(final).map(::tipo)
                    assertTrue("$final: $tipos", tipos.count { it == "Tocar" } <= 2)
                    desafios += tipos.filter { it != "Tocar" }
                }
                assertTrue("$final: só $desafios em 5 partidas", desafios.size >= 6)
            }
        }
    }

    @Test
    fun `pintar por codigo nunca passa de seis colunas`() {
        assertEquals(6, PintarPorCodigo.MAX_COLUNAS)
        // Celular de 360 dp: margens de 16 dp e folga de 20 dp da tela do pintar.
        assertTrue((360 - 2 * 16 - 20) / PintarPorCodigo.MAX_COLUNAS >= 48)
        var vistos = 0
        for (s in sementes) {
            val r = Random(s)
            val caminhos: List<FasesDoCaminho> = listOf(FasesNumeros(r), FasesLetras(r), FasesIngles(r), FasesLogica(r))
            for (c in caminhos) for (n in 51..FasesDoCaminho.TOTAL_DE_FASES) repeat(12) {
                val p = c.fase(n).criar().atividade as? PintarPorCodigo ?: return@repeat
                vistos++
                assertTrue("${c.trilha} $n: ${p.colunas} colunas", p.colunas in 3..PintarPorCodigo.MAX_COLUNAS)
                assertEquals(p.casas.size, p.colunas * p.linhas)
            }
        }
        assertTrue("poucos desenhos de pintar conferidos: $vistos", vistos > 200)
    }
}
