package br.com.fipeplaca

import br.com.fipeplaca.data.PlacaParser
import br.com.fipeplaca.data.TipoVeiculo
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacaParserTest {
    private val respostaApiPlacas = """
    {
      "MARCA": "VW", "MODELO": "GOL 1.0", "ano": "2015", "anoModelo": "2016",
      "cor": "Branca", "municipio": "Divinopolis", "uf": "MG", "placa": "ABC1D23",
      "situacao": "Sem restrição", "chassi": "9BWAA05U0GP012345",
      "extra": { "combustivel": "Alcool / Gasolina" },
      "fipe": { "dados": [
        { "ano_modelo": "2016", "codigo_fipe": "005340-6", "combustivel": "Gasolina",
          "mes_referencia": "outubro de 2026", "score": 80, "texto_marca": "VW - VolksWagen",
          "texto_modelo": "Gol 1.0 Flex 12V 5p", "texto_valor": "R$ 38.512,00", "tipo_modelo": 1 },
        { "ano_modelo": "2016", "codigo_fipe": "005339-2", "combustivel": "Gasolina",
          "mes_referencia": "outubro de 2026", "score": 95, "texto_marca": "VW - VolksWagen",
          "texto_modelo": "Gol 1.0 Mi Total Flex 8V 4p", "texto_valor": "R$ 36.100,00", "tipo_modelo": 1 }
      ] }
    }
    """.trimIndent()

    @Test fun leRespostaCompleta() {
        val v = PlacaParser.parse(JsonParser.parseString(respostaApiPlacas), "abc1d23")
        assertEquals("VW", v.marca)
        assertEquals("2016", v.anoModelo)
        assertEquals("Alcool / Gasolina", v.combustivel)
        assertEquals(TipoVeiculo.CARROS, v.tipo)
        assertEquals(2, v.fipe.size)
        assertEquals("005339-2", v.fipe.first().codigoFipe) // maior score primeiro
        assertEquals("R$ 36.100,00", v.fipe.first().valor)
        assertEquals("012345", v.chassiFinal)
    }

    @Test fun moto() {
        val json = """{"marca":"HONDA","modelo":"CG 160","fipe":{"dados":[{"codigo_fipe":"811150-0","tipo_modelo":2}]}}"""
        val v = PlacaParser.parse(JsonParser.parseString(json), "ABC1234")
        assertEquals(TipoVeiculo.MOTOS, v.tipo)
        assertEquals(TipoVeiculo.MOTOS, v.fipe.first().tipo)
    }

    @Test fun erroDoProvedor() {
        val e = runCatching { PlacaParser.parse(JsonParser.parseString("""{"message":"Token inválido"}"""), "ABC1234") }
            .exceptionOrNull()
        assertTrue(e is PlacaParser.ErroProvedor)
        assertEquals("Token inválido", e!!.message)
    }
}
