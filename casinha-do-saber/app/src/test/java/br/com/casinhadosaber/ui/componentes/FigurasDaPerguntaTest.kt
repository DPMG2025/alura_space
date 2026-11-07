package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.Trilha
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Figuras 3D das perguntas (Fase 4 do PRD visual 3D) contra o manifesto real: que emojis já têm modelo (`fig_*`,
 * campo `emojis`), quais continuam emoji, e a geometria do cenário do jogo (ilha e cenas comuns).
 */
class FigurasDaPerguntaTest {

    private val manifesto by lazy {
        val pasta = listOf("src/main/assets/arte", "app/src/main/assets/arte").map(::File).firstOrNull { it.isDirectory }
            ?: error("pasta assets/arte não encontrada a partir de ${File(".").absolutePath}")
        ManifestoDaArte.ler(File(pasta, "manifesto.json").readText())
    }

    private val figuras get() = manifesto.imagens.values.filter { it.id.startsWith(FigurasDaPergunta.PREFIXO) }

    @Test fun emojiComModeloViraFiguraESemModeloFicaEmoji() {
        val casos = mapOf(
            "🦆" to "fig_pato", "🍎" to "fig_maca", "⚽" to "fig_bola", "🐶" to "fig_cachorro", "🐕" to "fig_cachorro",
            "🐮" to "fig_vaca", "🐄" to "fig_vaca", "🧦" to "fig_meia", "🔴" to "fig_bolinha_vermelha", "🪁" to "fig_pipa",
            "🪙" to "moeda",
        )
        for ((emoji, id) in casos) assertEquals("figura de $emoji", id, FigurasDaPergunta.idPara(manifesto, emoji))
        // Seletor de variação (U+FE0F) não muda a figura.
        assertEquals("fig_estrela", FigurasDaPergunta.idPara(manifesto, "⭐"))
        assertEquals("fig_estrela", FigurasDaPergunta.idPara(manifesto, "⭐️"))
        assertEquals("fig_estrela", FigurasDaPergunta.idPara(manifesto, "🌟"))
        assertEquals("fig_coracao", FigurasDaPergunta.idPara(manifesto, "❤"))
        assertEquals("fig_coracao", FigurasDaPergunta.idPara(manifesto, "❤️"))
        assertEquals("fig_aviao", FigurasDaPergunta.idPara(manifesto, "✈"))
        // Sem modelo ainda (lista da fase 4A): continuam emoji.
        for (e in listOf("🦓", "🐬", "🐙", "🍋", "🟦", "🦃", "❄️", "🏠", "☀️")) {
            assertNull("$e não tem modelo", FigurasDaPergunta.idPara(manifesto, e))
            assertFalse(FigurasDaPergunta.temModelo(manifesto, e))
        }
        // Letras, números, sinais e vazio nunca viram figura.
        for (t in listOf("A", "BA", "3", "?", "+", "=", "", " ", "÷")) assertNull("'$t'", FigurasDaPergunta.idPara(manifesto, t))
    }

    @Test fun todaFiguraDoManifestoTemEmojisLugarEVoltaPeloIndice() {
        assertTrue("o manifesto tem as figuras da fase 4A", figuras.size >= 50)
        val lugares = QuadroDoJogo.LUGARES_COM_CENA + listOf("ceu", "grama")
        val dono = HashMap<String, String>()
        for (f in figuras) {
            assertTrue("${f.id} sem emojis", f.emojis.isNotEmpty())
            assertTrue("${f.id} com lugar '${f.lugar}'", f.lugar in lugares)
            assertTrue("${f.id}: tipo ${f.tipo}", f.tipo == "objeto" || f.tipo == "item")
            assertTrue("${f.id} sem âncora topo", "topo" in f.ancoras)
            for (e in f.emojis) {
                val n = FigurasDaPergunta.normalizar(e)
                val antes = dono.put(n, f.id)
                assertNull("o emoji $e está em ${f.id} e em $antes", antes)
                assertEquals(f.id, FigurasDaPergunta.idPara(manifesto, e))
            }
        }
        // Todo id do índice existe no manifesto, e só as figuras (e as reservas) estão nele.
        for ((e, id) in FigurasDaPergunta.indice(manifesto)) {
            assertNotNull("$e -> $id fora do manifesto", manifesto.imagens[id])
            assertTrue(id.startsWith(FigurasDaPergunta.PREFIXO) || id in FigurasDaPergunta.RESERVAS.values)
        }
        for ((_, id) in FigurasDaPergunta.RESERVAS) assertNotNull("reserva $id fora do manifesto", manifesto.imagens[id])
    }

    @Test fun lugarDaCenaVemDaPrimeiraFiguraComModelo() {
        assertEquals("lagoa", FigurasDaPergunta.lugarDaCena(manifesto, List(4) { "🦆" }))
        assertEquals("mesa", FigurasDaPergunta.lugarDaCena(manifesto, listOf("🦓", "🍌", "🍌")))
        assertEquals("varal", FigurasDaPergunta.lugarDaCena(manifesto, listOf("👕")))
        assertEquals("cercado", FigurasDaPergunta.lugarDaCena(manifesto, listOf("🐷")))
        assertEquals("tapete", FigurasDaPergunta.lugarDaCena(manifesto, listOf("🧸")))
        assertEquals("ceu", FigurasDaPergunta.lugarDaCena(manifesto, listOf("🦋")))
        assertEquals("grama", FigurasDaPergunta.lugarDaCena(manifesto, listOf("🐱")))
        assertNull(FigurasDaPergunta.lugarDaCena(manifesto, listOf("🦓", "🐬")))
        assertNull(FigurasDaPergunta.lugarDaCena(manifesto, emptyList()))
    }

    @Test fun ilhasECenasDoJogoEstaoNoManifestoComAsAncoras() {
        for (t in Trilha.entries) {
            val ilha = manifesto.imagens[QuadroDoJogo.idDaIlha(t)]
            assertNotNull("ilha de $t", ilha)
            assertEquals(QuadroDoJogo.LARGURA, ilha!!.largura)
            assertEquals(QuadroDoJogo.ALTURA, ilha.altura)
            for (k in 1..6) assertTrue("ilha de $t sem vaga_$k", "vaga_$k" in ilha.ancoras)
            assertEquals(QuadroDoJogo.CENTRO_DA_ILHA, ilha.ancoras.getValue("centro").y.toFloat() / ilha.altura, 0.001f)
        }
        for (lugar in QuadroDoJogo.LUGARES_COM_CENA) {
            val cena = manifesto.imagens[QuadroDoJogo.idDaCena(lugar)]
            assertNotNull("cena $lugar", cena)
            assertEquals(QuadroDoJogo.LARGURA, cena!!.largura)
            assertEquals(QuadroDoJogo.ALTURA, cena.altura)
            assertTrue("cena $lugar com menos de 5 vagas", (1..5).all { "vaga_$it" in cena.ancoras })
            assertNotNull(QuadroDoJogo.desenhoDaCena(lugar))
        }
    }

    @Test fun vagasDasFiguras() {
        val lagoa = manifesto.imagens.getValue(QuadroDoJogo.idDaCena("lagoa")).ancoras
        val varal = manifesto.imagens.getValue(QuadroDoJogo.idDaCena("varal")).ancoras
        val ilha = manifesto.imagens.getValue(QuadroDoJogo.idDaIlha(Trilha.MATEMATICA)).ancoras
        // Até as vagas, uma figura por vaga, de trás para a frente.
        val quatro = QuadroDoJogo.vagas(4, lagoa, "lagoa")
        assertEquals(4, quatro.size)
        assertEquals(quatro.sortedBy { it.y }, quatro)
        assertEquals((1..4).map { lagoa.getValue("vaga_$it").x.toFloat() }.toSet(), quatro.map { it.x }.toSet())
        // No varal, penduradas.
        assertTrue(QuadroDoJogo.vagas(5, varal, "varal").all { it.pendurada })
        // No céu, acima da grama.
        val noCeu = QuadroDoJogo.vagas(3, ilha, "ceu")
        val naGrama = QuadroDoJogo.vagas(3, ilha, null)
        assertTrue(noCeu.zip(naGrama).all { (c, g) -> c.y < g.y })
        // Mais que as vagas: todas espalhadas dentro da área útil, sem duas no mesmo lugar e menores.
        for (n in listOf(7, 10, 12, 20)) {
            for ((ancoras, nome) in listOf(lagoa to "lagoa", ilha to null)) {
                val v = QuadroDoJogo.vagas(n, ancoras, nome)
                val area = QuadroDoJogo.areaUtil(ancoras)
                assertEquals(n, v.size)
                assertEquals(n, v.map { it.x to it.y }.toSet().size)
                assertTrue("$n figuras fora da área", v.all { it.x in area.esquerda - 1f..area.direita + 1f && it.y in area.topo - 1f..area.base + 1f })
                assertTrue(v.all { it.lado in 40f..170f })
            }
        }
        assertTrue(QuadroDoJogo.vagas(0, lagoa, "lagoa").isEmpty())
    }

    @Test fun quadrosDaIlhaEDasFiguras() {
        // Celular em pé (maquete 2): ilha com a largura da tela e o centro a 43% da altura.
        val emPe = QuadroDoJogo.daIlha(393f, 852f)
        assertEquals(393f, emPe.largura, 0.01f)
        assertEquals(0f, emPe.x, 0.01f)
        assertEquals(852f * 0.43f, emPe.emY(534f), 0.5f)
        // Deitado: a ilha cabe na altura, centrada na largura.
        val deitado = QuadroDoJogo.daIlha(800f, 360f)
        assertTrue(deitado.largura <= 360f)
        assertEquals(400f, deitado.emX(400f), 0.5f)
        // A cena e as figuras cabem na caixa dada, centradas.
        val lagoa = manifesto.imagens.getValue(QuadroDoJogo.idDaCena("lagoa")).ancoras
        val vagas = QuadroDoJogo.vagas(4, lagoa, "lagoa")
        val caixa = QuadroDoJogo.caixaVisivel(vagas, QuadroDoJogo.desenhoDaCena("lagoa"))
        for ((w, h) in listOf(1000f to 1000f, 990f to 520f, 400f to 2000f)) {
            val q = QuadroDoJogo.paraCaber(caixa, w, h, larguraMaxima = w * 1.1f)
            assertTrue(q.emX(caixa.esquerda) >= -0.5f && q.emX(caixa.direita) <= w + 0.5f)
            assertTrue(q.emY(caixa.topo) >= -0.5f && q.emY(caixa.base) <= h + 0.5f)
            assertEquals(w / 2f, (q.emX(caixa.esquerda) + q.emX(caixa.direita)) / 2f, 0.5f)
        }
    }
}
