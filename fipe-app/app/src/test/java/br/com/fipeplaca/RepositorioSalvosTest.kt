package br.com.fipeplaca

import android.content.Context
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.RepositorioSalvos
import br.com.fipeplaca.data.TipoVeiculo
import br.com.fipeplaca.data.VariacaoSalvo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Integridade dos salvos com SharedPreferences reais (Robolectric). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RepositorioSalvosTest {
    private val ctx: Context get() = RuntimeEnvironment.getApplication()
    private val sp get() = ctx.getSharedPreferences("salvos", Context.MODE_PRIVATE)

    private fun r(valor: String, mes: String) =
        FipeResultado(TipoVeiculo.CARROS, "Fiat", "Uno", "2012", "Gasolina", "001234-5", mes, valor, codigoAno = "2012-1")

    @Before fun limpar() { sp.edit().clear().commit() }

    @Test fun jsonCorrompidoEPreservadoEmCopiaDeSeguranca() {
        sp.edit().putString("consultas", "{isto nao e json").commit()
        val repo = RepositorioSalvos(ctx)
        repo.aguardar()
        assertTrue(repo.itens.value.isEmpty())
        val copia = sp.all.keys.firstOrNull { it.startsWith("consultas_seguranca_") }
        assertEquals("{isto nao e json", sp.getString(copia, null)) // nada se perde
    }

    @Test fun registrosAntigosSaoLidosEIncompletosDescartados() {
        // Formato antigo (sem versao, historico local e valor ao salvar) e um item incompleto
        sp.edit().putString("consultas", """[
            {"id":"M:CARROS:001234-5:2012-1","data":1,"favorito":true,
             "resultado":{"tipo":"CARROS","marca":"Fiat","modelo":"Uno","anoModelo":"2012","combustivel":"Gasolina",
                          "codigoFipe":"001234-5","mesReferencia":"outubro de 2026","valor":"R$ 20.000,00"}},
            {"id":"quebrado","data":2,"resultado":{"marca":"X"}}
        ]""").commit()
        val repo = RepositorioSalvos(ctx)
        repo.aguardar()
        assertEquals(listOf("M:CARROS:001234-5:2012-1"), repo.itens.value.map { it.id })
        assertTrue(repo.itens.value.single().favorito)
    }

    @Test fun variacaoDesdeQueFoiSalvoUsaOValorDoDia() {
        val repo = RepositorioSalvos(ctx)
        repo.aguardar()
        val id = "M:CARROS:001234-5:2012-1"
        repo.registrar(ConsultaSalva(id, 1, resultado = r("R$ 20.000,00", "outubro de 2026")))
        // Historico da API traz meses anteriores: nao muda a base
        repo.guardarPontos(id, listOf(br.com.fipeplaca.data.PontoHistorico("junho de 2026", "", 25000.0)))
        repo.aguardar()
        assertNull(VariacaoSalvo.de(repo.buscar(id)!!)) // mesmo mes: sem variacao ainda
        repo.registrar(ConsultaSalva(id, 2, resultado = r("R$ 19.000,00", "novembro de 2026")))
        repo.aguardar()
        val c = repo.buscar(id)!!
        assertEquals("outubro de 2026", c.valorAoSalvar!!.mesReferencia)
        assertEquals(-5.0, VariacaoSalvo.de(c)!!, 0.0001)
    }

    @Test fun favoritarDuasVezesRapidoVoltaAoInicio() {
        val repo = RepositorioSalvos(ctx)
        repo.aguardar()
        val novo = ConsultaSalva("M:x", 1, resultado = r("R$ 1,00", "outubro de 2026"))
        repo.alternarOuCriarFavorito(novo) // cria como favorito
        repo.alternarOuCriarFavorito(novo) // desfaz, sem corrida com a gravacao anterior
        repo.aguardar()
        assertEquals(false, repo.buscar("M:x")!!.favorito)
    }

    @Test fun gravacaoSobreviveANovaInstancia() {
        val repo = RepositorioSalvos(ctx)
        repo.aguardar()
        repo.registrar(ConsultaSalva("M:y", 1, resultado = r("R$ 1,00", "outubro de 2026")))
        repo.aguardar()
        val outro = RepositorioSalvos(ctx)
        outro.aguardar()
        assertEquals(listOf("M:y"), outro.itens.value.map { it.id })
    }
}
