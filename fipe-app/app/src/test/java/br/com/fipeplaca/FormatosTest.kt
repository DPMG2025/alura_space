package br.com.fipeplaca

import br.com.fipeplaca.data.Dinheiro
import br.com.fipeplaca.data.PeriodoHistorico
import br.com.fipeplaca.data.Placa
import br.com.fipeplaca.data.ReferenciaV2
import br.com.fipeplaca.data.Referencias
import br.com.fipeplaca.data.Texto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatosTest {
    @Test fun placas() {
        assertTrue(Placa.valida("ABC1234"))
        assertTrue(Placa.valida("abc-1d23"))
        assertFalse(Placa.valida("AB12345"))
        assertFalse(Placa.valida("ABC12"))
        assertEquals("ABC1D23", Placa.normalizar(" abc 1d23 "))
        assertEquals("ABC-1234", Placa.formatar("abc1234"))
        assertEquals("ABC1D23", Placa.formatar("abc1d23"))
    }

    @Test fun dinheiro() {
        assertEquals(45320.0, Dinheiro.paraDouble("R$ 45.320,00")!!, 0.001)
        assertEquals(1234567.89, Dinheiro.paraDouble("R$ 1.234.567,89")!!, 0.001)
        assertNull(Dinheiro.paraDouble("indisponível"))
    }

    @Test fun texto() {
        assertEquals("caminhoes", Texto.chave("Caminhões"))
        assertEquals("Zero KM Gasolina", Texto.nomeAno("32000 Gasolina"))
    }

    @Test fun periodos() {
        // Mais recente primeiro, como a API devolve
        val refs = (0 until 30).map { i ->
            val indice = 10 - i // novembro de 2026 = indice 10
            val ano = 2026 + Math.floorDiv(indice, 12)
            val mes = Math.floorMod(indice, 12) + 1
            ReferenciaV2("${400 - i}", "$mes/$ano")
        }
        val doze = Referencias.filtrar(refs, PeriodoHistorico.MESES_12)
        assertEquals(12, doze.size)
        assertEquals("400", doze.last().codigo) // cronologico: mais recente no fim
        assertEquals(24, Referencias.filtrar(refs, PeriodoHistorico.MESES_24).size)
        val ano = Referencias.filtrar(refs, PeriodoHistorico.ANO_ATUAL)
        assertTrue(ano.all { Referencias.ano(it.mes) == 2026 })
        assertEquals(11, ano.size) // janeiro a novembro
    }
}
