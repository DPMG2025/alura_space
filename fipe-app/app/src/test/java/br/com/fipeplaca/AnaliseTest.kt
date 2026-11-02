package br.com.fipeplaca

import br.com.fipeplaca.data.Compartilhar
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.Estatisticas
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.Meses
import br.com.fipeplaca.data.OrganizarSalvos
import br.com.fipeplaca.data.PontoHistorico
import br.com.fipeplaca.data.TipoVeiculo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnaliseTest {
    private val pontos = listOf(
        PontoHistorico("janeiro de 2026", "", 50000.0),
        PontoHistorico("fevereiro de 2026", "", 52000.0),
        PontoHistorico("março de 2026", "", 45000.0),
    )

    @Test fun estatisticas() {
        val e = Estatisticas.de(pontos)!!
        assertEquals(52000.0, e.maximo.valor, 0.0)
        assertEquals("março de 2026", e.minimo.mesReferencia)
        assertEquals(49000.0, e.media, 0.001)
        assertEquals(-10.0, e.variacaoPercentual, 0.001)
        assertEquals(-5000.0, e.diferenca, 0.001)
        assertEquals(-5.0, e.variacaoMensalMedia, 0.001)
        assertNull(Estatisticas.de(emptyList()))
        assertEquals(0.0, Estatisticas.de(pontos.take(1))!!.variacaoMensalMedia, 0.0)
    }

    @Test fun percentual() {
        assertEquals("+2,50%", Estatisticas.percentual(2.5, 2))
        assertEquals("-1,0%", Estatisticas.percentual(-1.0))
        assertEquals("0,0%", Estatisticas.percentual(0.01))
    }

    @Test fun meses() {
        assertEquals("nov/26", Meses.curto("novembro de 2026"))
        assertEquals("mar/25", Meses.curto("março/2025 "))
        assertEquals("Novembro de 2026", Meses.capitalizar(" novembro de 2026"))
        assertEquals("abc", Meses.curto("abc"))
    }

    @Test fun textoCompartilhado() {
        val r = FipeResultado(TipoVeiculo.CARROS, "Fiat", "Uno Mille", "2012", "Gasolina", "001234-5", "novembro de 2026", "R$ 20.000,00")
        val t = Compartilhar.texto(r, historico = pontos)
        assertTrue(t.contains("Valor FIPE: R$ 20.000,00"))
        assertTrue(t.contains("Código FIPE: 001234-5"))
        assertTrue(t.contains("-10,0%"))
        assertTrue(t.contains("jan/26 a mar/26"))
    }

    @Test fun organizarSalvos() {
        fun c(id: String, fav: Boolean = false) = ConsultaSalva(id, 0, favorito = fav)
        var lista = listOf(c("a", fav = true), c("b"), c("c"), c("d"))
        lista = OrganizarSalvos.inserir(lista, c("c"), limite = 2)
        assertEquals(listOf("c", "a", "b"), lista.map { it.id }) // sem duplicar, favorito mantido, limite de 2 recentes
        lista = OrganizarSalvos.inserir(lista, c("e"), limite = 2)
        assertEquals(listOf("e", "c", "a"), lista.map { it.id })
    }
}
