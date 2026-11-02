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

class FormatosExtrasTest {
    @Test fun marcas() {
        assertEquals("Volkswagen", br.com.fipeplaca.data.Texto.marca("VW - VolksWagen"))
        assertEquals("Chevrolet", br.com.fipeplaca.data.Texto.marca("GM - Chevrolet"))
        assertEquals("BMW", br.com.fipeplaca.data.Texto.marca("BMW"))
        assertEquals("CAOA Chery", br.com.fipeplaca.data.Texto.marca("CAOA Chery"))
        assertEquals("Kia Motors", br.com.fipeplaca.data.Texto.marca("Kia Motors"))
    }

    @Test fun datas() {
        val z = java.time.ZoneId.of("America/Sao_Paulo")
        val agora = java.time.ZonedDateTime.of(2026, 11, 2, 15, 0, 0, 0, z).toInstant().toEpochMilli()
        val h = 3_600_000L
        assertEquals("Hoje, 12:00", br.com.fipeplaca.data.Datas.relativa(agora - 3 * h, agora, z))
        assertEquals("Ontem, 15:00", br.com.fipeplaca.data.Datas.relativa(agora - 24 * h, agora, z))
        assertEquals("30/10/26", br.com.fipeplaca.data.Datas.relativa(agora - 72 * h, agora, z))
    }

    @Test fun projecao() {
        val queda = (0 until 12).map { PontoHistorico("m$it", "", 50000.0 - it * 500) }
        assertEquals(50000.0 - 23 * 500, br.com.fipeplaca.data.Projecao.estimar(queda)!!, 0.01)
        assertNull(br.com.fipeplaca.data.Projecao.estimar(queda.take(5)))
        val estavel = (0 until 8).map { PontoHistorico("m$it", "", 30000.0) }
        assertEquals(30000.0, br.com.fipeplaca.data.Projecao.estimar(estavel)!!, 0.01)
    }
}

class MesclarHistoricoTest {
    private fun p(mes: String, v: Double) = PontoHistorico(mes, "", v)

    @Test fun indiceDosMeses() {
        assertEquals(2026 * 12 + 10, br.com.fipeplaca.data.Meses.indice("novembro de 2026"))
        assertEquals(2026 * 12 + 2, br.com.fipeplaca.data.Meses.indice("março/2026 "))
        assertEquals(2025 * 12 + 11, br.com.fipeplaca.data.Meses.indice("12/2025"))
        assertNull(br.com.fipeplaca.data.Meses.indice("ref 300"))
    }

    @Test fun mesclaSemRepetirERecortaPeriodo() {
        val api = listOf(p("setembro de 2026", 3.0), p("outubro de 2026", 4.0))
        val locais = listOf(p("setembro de 2026", 99.0), p("junho de 2025", 1.0), p("janeiro de 2026", 2.0))
        val doze = br.com.fipeplaca.data.MesclarHistorico.mesclar(api, locais, br.com.fipeplaca.data.PeriodoHistorico.MESES_12)
        assertEquals(listOf(2.0, 3.0, 4.0), doze.map { it.valor }) // junho/2025 fora dos 12 meses; API prevalece em setembro
        val vinte4 = br.com.fipeplaca.data.MesclarHistorico.mesclar(api, locais, br.com.fipeplaca.data.PeriodoHistorico.MESES_24)
        assertEquals(4, vinte4.size)
        val ano = br.com.fipeplaca.data.MesclarHistorico.mesclar(api, locais, br.com.fipeplaca.data.PeriodoHistorico.ANO_ATUAL)
        assertEquals(3, ano.size)
    }

    @Test fun acumulaComLimite() {
        val antigos = (1..12).map { p("$it/2025", it.toDouble()) }
        val r = br.com.fipeplaca.data.MesclarHistorico.acumular(antigos, listOf(p("1/2026", 13.0), p("12/2025", 50.0)), limite = 10)
        assertEquals(10, r.size)
        assertEquals(13.0, r.last().valor, 0.0)
        assertEquals(50.0, r[r.size - 2].valor, 0.0) // novo valor do mes substitui o antigo
    }
}
