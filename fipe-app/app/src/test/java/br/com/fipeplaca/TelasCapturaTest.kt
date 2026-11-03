package br.com.fipeplaca

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.ItemFipe
import br.com.fipeplaca.data.PeriodoHistorico
import br.com.fipeplaca.data.PontoHistorico
import br.com.fipeplaca.data.TipoVeiculo
import br.com.fipeplaca.data.VeiculoPlaca
import br.com.fipeplaca.ui.AcoesManual
import br.com.fipeplaca.ui.AcoesPlaca
import br.com.fipeplaca.ui.AcoesSalvos
import br.com.fipeplaca.ui.BarraNavegacao
import br.com.fipeplaca.ui.Configuracoes
import br.com.fipeplaca.ui.Etapa
import br.com.fipeplaca.ui.HistoricoUi
import br.com.fipeplaca.ui.ManualUi
import br.com.fipeplaca.ui.PlacaUi
import br.com.fipeplaca.ui.TelaConfiguracoes
import br.com.fipeplaca.ui.TelaManual
import br.com.fipeplaca.ui.TelaPlaca
import br.com.fipeplaca.ui.TelaSalvos
import br.com.fipeplaca.ui.TemaFipe
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Gera capturas das telas com dados de exemplo em build/outputs/roborazzi.
 * Servem para revisar o visual sem emulador (o CI publica as imagens).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class TelasCapturaTest {

    private val gol = FipeResultado(
        tipo = TipoVeiculo.CARROS, marca = "VW - VolksWagen", modelo = "Gol 1.0 Flex 12V 5p",
        anoModelo = "2019", combustivel = "Gasolina", codigoFipe = "005340-6",
        mesReferencia = "novembro de 2026", valor = "R$ 48.512,00", codigoAno = "2019-1", score = 92,
    )
    private val gol2 = gol.copy(modelo = "Gol Track 1.0 Flex 12V 5p", codigoFipe = "005524-7", valor = "R$ 50.903,00", score = 71)
    private val veiculo = VeiculoPlaca(
        placa = "RTA2C19", marca = "VW", modelo = "GOL 1.0L MC4", anoFabricacao = "2018", anoModelo = "2019",
        cor = "Prata", combustivel = "Álcool/Gasolina", municipio = "Divinópolis", uf = "MG",
        situacao = "Sem restrição", chassiFinal = "045123", tipo = TipoVeiculo.CARROS, fipe = listOf(gol, gol2),
    )
    private val meses = listOf("dezembro de 2025", "janeiro de 2026", "fevereiro de 2026", "março de 2026", "abril de 2026",
        "maio de 2026", "junho de 2026", "julho de 2026", "agosto de 2026", "setembro de 2026", "outubro de 2026", "novembro de 2026")
    private val valores = listOf(51200.0, 51050.0, 50720.0, 50810.0, 50300.0, 49950.0, 49870.0, 49400.0, 49210.0, 48990.0, 48700.0, 48512.0)
    private val historico = HistoricoUi(
        alvo = gol, periodo = PeriodoHistorico.MESES_12,
        pontos = meses.zip(valores).map { (m, v) -> PontoHistorico(m, "", v) },
    )
    private val salvos = listOf(
        ConsultaSalva(ConsultaSalva.idPlaca("RTA2C19"), 1793620800000, favorito = true, veiculo = veiculo,
            historicoLocal = listOf(PontoHistorico("agosto de 2026", "", 49210.0), PontoHistorico("setembro de 2026", "", 48990.0),
                PontoHistorico("outubro de 2026", "", 48700.0), PontoHistorico("novembro de 2026", "", 48512.0)),
            valorAoSalvar = PontoHistorico("agosto de 2026", "", 49210.0)),
        ConsultaSalva(ConsultaSalva.idPlaca("HKP4410"), 1793534400000, veiculo = veiculo.copy(placa = "HKP4410",
            fipe = listOf(gol.copy(modelo = "Onix 1.0 Turbo LT", marca = "GM - Chevrolet", valor = "R$ 79.340,00")))),
        ConsultaSalva("M:CARROS:1", 1793448000000, favorito = true,
            resultado = gol.copy(marca = "Toyota", modelo = "Corolla XEi 2.0 Flex Aut.", valor = "R$ 132.870,00", anoModelo = "2022")),
    )
    private val marcas = listOf("Acura", "Audi", "BMW", "BYD", "CAOA Chery", "Citroën", "Fiat", "Ford", "GM - Chevrolet", "Honda",
        "Hyundai", "Jeep", "Kia Motors", "Mitsubishi", "Nissan", "Peugeot", "Renault", "Toyota", "VW - VolksWagen")
        .mapIndexed { i, n -> ItemFipe("${i + 1}", n) }

    @Composable
    private fun Moldura(aba: Int, conteudo: @Composable (Modifier) -> Unit) {
        Scaffold(
            contentWindowInsets = WindowInsets(0),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { BarraNavegacao(aba) {} },
        ) { p -> conteudo(Modifier.padding(p)) }
    }

    private fun captura(nome: String, escuro: Boolean = false, conteudo: @Composable () -> Unit) =
        captureRoboImage("build/outputs/roborazzi/$nome.png") { TemaFipe(escuro = escuro) { conteudo() } }

    @Test fun placaInicial() = captura("01_placa_inicial") {
        Moldura(0) { TelaPlaca(PlacaUi(placa = "RTA2C1"), HistoricoUi(), salvos, false, AcoesPlaca(), it) }
    }

    @Test fun placaVazia() = captura("02_placa_primeiro_uso") {
        Moldura(0) { TelaPlaca(PlacaUi(), HistoricoUi(), emptyList(), false, AcoesPlaca(), it) }
    }

    @Config(qualifiers = "w393dp-h2600dp-xhdpi")
    @Test fun placaResultado() = captura("03_placa_resultado") {
        Moldura(0) { TelaPlaca(PlacaUi(placa = "RTA2C19", veiculo = veiculo), historico, salvos, true, AcoesPlaca(), it) }
    }

    @Config(qualifiers = "w393dp-h2600dp-xhdpi")
    @Test fun placaResultadoEscuro() = captura("04_placa_resultado_escuro", escuro = true) {
        Moldura(0) { TelaPlaca(PlacaUi(placa = "RTA2C19", veiculo = veiculo), historico, salvos, false, AcoesPlaca(), it) }
    }

    @Test fun placaErro() = captura("05_placa_sem_token") {
        Moldura(0) { TelaPlaca(PlacaUi(placa = "RTA2C19", semToken = true), HistoricoUi(), emptyList(), false, AcoesPlaca(), it) }
    }

    @Test fun manualMarcas() = captura("06_tabela_marcas") {
        Moldura(1) { TelaManual(ManualUi(marcas = marcas), HistoricoUi(), false, AcoesManual(), it) }
    }

    @Test fun manualModelos() = captura("07_tabela_modelos_escuro", escuro = true) {
        val modelos = listOf("Gol 1.0 Flex 12V 5p", "Gol 1.6 MSI Flex 8V 5p", "Gol Last Edition 1.0 Flex", "Gol Track 1.0 Flex 12V 5p",
            "Polo 1.0 Flex 12V 5p", "Polo Highline 200 TSI 1.0", "T-Cross 200 TSI 1.0 Flex Aut.", "Virtus 1.0 200 TSI Flex Aut.")
            .mapIndexed { i, n -> ItemFipe("${i + 10}", n) }
        Moldura(1) {
            TelaManual(ManualUi(etapa = Etapa.MODELOS, marca = marcas.last(), modelos = modelos, filtro = ""), HistoricoUi(), false, AcoesManual(), it)
        }
    }

    @Config(qualifiers = "w393dp-h2000dp-xhdpi")
    @Test fun manualResultado() = captura("08_tabela_resultado") {
        Moldura(1) {
            TelaManual(
                ManualUi(etapa = Etapa.RESULTADO, marca = marcas.last(), modelo = ItemFipe("10", gol.modelo),
                    ano = ItemFipe("2019-1", "2019 Gasolina"), resultado = gol),
                historico.copy(periodo = PeriodoHistorico.MESES_24, pontos = historico.pontos.drop(1), solicitados = 24, exigemToken = 13), true, AcoesManual(), it,
            )
        }
    }

    @Config(qualifiers = "w393dp-h1500dp-xhdpi")
    @Test fun historicoReal3Meses() = captura("12_historico_3_meses_sem_token") {
        Moldura(1) {
            TelaManual(
                ManualUi(etapa = Etapa.RESULTADO, marca = marcas.last(), modelo = ItemFipe("10", gol.modelo),
                    ano = ItemFipe("2019-1", "2019 Gasolina"), resultado = gol),
                historico.copy(pontos = historico.pontos.takeLast(3), solicitados = 12, exigemToken = 9), false, AcoesManual(), it,
                semTokenFipe = true,
            )
        }
    }

    @Config(qualifiers = "w393dp-h1300dp-xhdpi")
    @Test fun historicoReal1Mes() = captura("13_historico_1_mes_escuro", escuro = true) {
        Moldura(1) {
            TelaManual(
                ManualUi(etapa = Etapa.RESULTADO, marca = marcas.last(), modelo = ItemFipe("10", gol.modelo),
                    ano = ItemFipe("2019-1", "2019 Gasolina"), resultado = gol),
                historico.copy(pontos = historico.pontos.takeLast(1), solicitados = 12, exigemToken = 11), false, AcoesManual(), it,
                semTokenFipe = true,
            )
        }
    }

    @Test fun salvosFavoritos() = captura("09_salvos") {
        Moldura(2) { TelaSalvos(salvos, AcoesSalvos(), it, abaInicial = 1, agora = 1793635200000) }
    }

    @Test fun salvosVazio() = captura("10_salvos_vazio_escuro", escuro = true) {
        Moldura(2) { TelaSalvos(emptyList(), AcoesSalvos(), it, agora = 1793635200000) }
    }

    @Config(qualifiers = "w393dp-h1900dp-xhdpi")
    @Test fun ajustes() = captura("11_ajustes") {
        Moldura(3) {
            TelaConfiguracoes(Configuracoes("abc123", "https://wdapi2.com.br/consulta/{placa}/{token}", ""), BuildConfig.VERSION_NAME, {}, it, tema = "claro")
        }
    }
}
