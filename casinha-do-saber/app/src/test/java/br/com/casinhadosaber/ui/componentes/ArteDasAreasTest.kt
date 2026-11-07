package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.CasaQueCresce
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * A arte 3D das coisas das áreas novas (Fase 8 do PRD visual 3D) contra o manifesto real: o cachorrinho e o
 * gatinho 3D, o item da casa quando é a mesma coisa (mesmo tipo e mesmo emoji), a figura das perguntas pelo emoji,
 * e o emoji de reserva para o resto. Toda arte escolhida existe no manifesto.
 */
class ArteDasAreasTest {

    private val manifesto by lazy {
        val pasta = listOf("src/main/assets/arte", "app/src/main/assets/arte").map(::File).firstOrNull { it.isDirectory }
            ?: error("pasta assets/arte não encontrada a partir de ${File(".").absolutePath}")
        ManifestoDaArte.ler(File(pasta, "manifesto.json").readText())
    }

    private fun coisa(area: String, tipo: String) =
        CasaQueCresce.porId.getValue(area).coisas.first { CasaQueCresce.tipoDe(it.id) == tipo }

    @Test fun bichosUsamOs3DDaCasa() {
        assertEquals(ArteDaCoisa("item_cachorro", comoItem = true, bicho = true), ArteDasAreas.para(manifesto, coisa("pet", "cachorro")))
        assertEquals(ArteDaCoisa("item_gato", comoItem = true, bicho = true), ArteDasAreas.para(manifesto, coisa("pet", "gato")))
    }

    @Test fun mesmaCoisaDaCasaUsaOItem() {
        // Cama e ursinho do quarto novo: mesmo tipo e mesmo emoji das peças da casa.
        assertEquals(ArteDaCoisa("item_cama", comoItem = true), ArteDasAreas.para(manifesto, coisa("quarto2", "cama")))
        assertEquals(ArteDaCoisa("item_ursinho", comoItem = true), ArteDasAreas.para(manifesto, coisa("quarto2", "ursinho")))
        // A estrela do quarto novo (🌟) não é a estrela de campeão (⭐): vai pela figura do emoji.
        assertEquals(ArteDaCoisa("fig_estrela", comoItem = false), ArteDasAreas.para(manifesto, coisa("quarto2", "estrela")))
    }

    @Test fun figuraPeloEmojiESemModeloFicaEmoji() {
        assertEquals(ArteDaCoisa("fig_maca", comoItem = false), ArteDasAreas.para(manifesto, coisa("horta", "maca")))
        assertEquals(ArteDaCoisa("fig_coelho", comoItem = false), ArteDasAreas.para(manifesto, coisa("pet", "coelho")))
        // A boia 🛟 ganhou figura no lote 2 da Fase 9; sem modelo (a bolinha de tênis 🎾 do pet), continua emoji.
        assertEquals(ArteDaCoisa("fig_boia", comoItem = false), ArteDasAreas.para(manifesto, coisa("piscina", "boia")))
        assertNull(ArteDasAreas.para(manifesto, coisa("pet", "bolinha")))
        // A obra e as 40 peças de sempre não passam por aqui.
        assertNull(ArteDasAreas.para(manifesto, CasaQueCresce.porId.getValue("piscina").obra))
    }

    @Test fun todaArteEscolhidaExisteEUmaParteDasCoisasJaTem3D() {
        var com3D = 0
        var total = 0
        for (area in CasaQueCresce.areas) for (item in area.coisas) {
            total++
            val arte = ArteDasAreas.para(manifesto, item) ?: continue
            com3D++
            assertTrue("${item.id}: ${arte.id} fora do manifesto", arte.id in manifesto.imagens)
            assertEquals("${item.id}: item 3D com prefixo errado", arte.comoItem, arte.id.startsWith("item_"))
        }
        assertTrue("quase nenhuma coisa das áreas com arte 3D ($com3D de $total)", com3D * 10 >= total)
    }

    @Test fun silhuetaDoProximoPresenteUsaAArteDeVerdade() {
        // Peça da casa: o item 3D; coisa de área: a mesma arte da cena; obra: a figura do emoji ou o ícone da área.
        assertEquals("item_cama", ArteDasAreas.silhuetaK2(manifesto, br.com.casinhadosaber.dominio.ItensDaCasa.porId.getValue("cama")))
        assertEquals("fig_maca", ArteDasAreas.silhuetaK2(manifesto, coisa("horta", "maca")))
        for (area in CasaQueCresce.areas) {
            val id = ArteDasAreas.silhuetaK2(manifesto, area.obra)
            assertTrue("obra de ${area.id} sem silhueta 3D", id != null && id in manifesto.imagens)
        }
        for (item in br.com.casinhadosaber.dominio.ItensDaCasa.ordem) {
            val id = ArteDasAreas.silhuetaK2(manifesto, item) ?: continue
            assertTrue("${item.id}: $id fora do manifesto", id in manifesto.imagens)
        }
    }

    @Test fun lugarDePartidaGanhaUmDesvioPequenoEOArrastadoNao() {
        var mexidas = 0
        for (area in CasaQueCresce.areas) for (item in area.coisas) {
            val (x, y) = ArteDasAreas.posicaoNaIlhaK2(item, item.x, item.y)
            assertTrue("${item.id} saiu da ilha ($x, $y)", x in 0.05f..0.95f && y in 0.08f..0.98f)
            assertTrue("${item.id} andou demais", kotlin.math.abs(x - item.x) <= 0.036f + 0.05f && kotlin.math.abs(y - item.y) <= 0.023f + 0.06f)
            // O que é de chão continua abaixo do horizonte.
            if (item.y > CasaQueCresce.HORIZONTE) assertTrue("${item.id} subiu para o céu", y > CasaQueCresce.HORIZONTE)
            if (x != item.x || y != item.y) mexidas++
        }
        assertTrue("quase nada saiu da grade ($mexidas)", mexidas > 100)
        // Arrastada pela criança: fica exatamente onde ela soltou.
        val c = coisa("horta", "maca")
        assertEquals(0.5f to 0.7f, ArteDasAreas.posicaoNaIlhaK2(c, 0.5f, 0.7f))
    }
}
