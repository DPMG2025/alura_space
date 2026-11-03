package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaCasa
import br.com.casinhadosaber.ui.telas.TelaInicio
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.TelaPais
import br.com.casinhadosaber.ui.telas.TelaPerfil
import br.com.casinhadosaber.ui.telas.TelaPremio
import br.com.casinhadosaber.ui.telas.TelaTrilha
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

/**
 * Gera imagens de todas as telas em build/outputs/roborazzi, para revisar o visual sem emulador.
 * O CI copia as imagens para docs/capturas.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val menino = Perfil("Leo", Genero.MENINO, pele = 0, cabelo = 3)
    private val gerador = GeradorDeFases(Random(4))

    /** Estado com [n] fases vencidas, espalhadas pelas trilhas, e os [colocados] primeiros itens na casa. */
    private fun estado(n: Int, colocados: Int, perfil: Perfil = menina): EstadoJogo {
        var e = EstadoJogo(perfil = perfil)
        val fases = (0 until 10).flatMap { i -> Trilha.entries.map { gerador.fases(it)[i] } }
        for ((k, f) in fases.take(n).withIndex()) e = e.registrarVitoria(f, 1 + k % 3).first
        for (item in ItensDaCasa.ordem.take(colocados)) e = e.colocar(item)
        return e
    }

    private fun capturar(nome: String, conteudo: @Composable () -> Unit) {
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    Box(Modifier.fillMaxSize()) { conteudo() }
                }
            }
        }
        regra.mainClock.advanceTimeBy(2500)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    @Test fun perfil() = capturar("01_perfil") { TelaPerfil(null) {} }

    @Test fun perfilEscolhendo() = capturar("01b_perfil_escolhendo") {
        TelaPerfil(menina.copy(estiloCabelo = 2)) {}
    }

    @Config(qualifiers = "w800dp-h1280dp-mdpi")
    @Test fun tablet() = capturar("22_tablet_inicio") {
        androidx.compose.foundation.layout.Box(
            Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter,
        ) {
            androidx.compose.foundation.layout.Box(Modifier.widthIn(max = 640.dp).fillMaxSize()) {
                TelaInicio(estado(20, 20), true, {}, {}, {}, {})
            }
        }
    }

    @Test fun inicioVazio() = capturar("02_inicio_comeco") {
        TelaInicio(estado(0, 0), true, {}, {}, {}, {})
    }

    @Test fun inicioComPresentes() = capturar("03_inicio_presentes") {
        TelaInicio(estado(9, 6), true, {}, {}, {}, {})
    }

    @Test fun inicioComMissao() = capturar("03b_inicio_missao_do_dia") {
        TelaInicio(estado(12, 12), true, {}, {}, {}, {}, missaoDisponivel = true)
    }

    @Test fun caminho() = capturar("04_caminho_numeros") {
        TelaTrilha(Trilha.MATEMATICA, estado(13, 13), {}, {})
    }

    @Test fun jogoContar() = capturar("05_jogo_contar") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[1], menina, {}, { })
    }

    @Test fun jogoSubtrair() = capturar("06_jogo_subtrair") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[8], menino, {}, { })
    }

    @Test fun jogoMaisOuMenos() = capturar("07_jogo_mais_ou_menos") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[4], menina, {}, { })
    }

    @Test fun jogoLetras() = capturar("08_jogo_silabas") {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[6], menino, {}, { })
    }

    @Test fun jogoIngles() = capturar("09_jogo_cores_ingles") {
        TelaJogo(gerador.fases(Trilha.INGLES)[4], menina, {}, { })
    }

    @Test fun jogoLogica() = capturar("10_jogo_sequencia") {
        TelaJogo(gerador.fases(Trilha.LOGICA)[6], menino, {}, { })
    }

    @Test fun premio() = capturar("11_premio") {
        TelaPremio(gerador.fases(Trilha.INGLES)[2], 3, ItensDaCasa.porId.getValue("cama"), menina, 5, true, {}, {}, {})
    }

    @Test fun quase() = capturar("11b_quase") {
        br.com.casinhadosaber.ui.telas.TelaQuase(gerador.fases(Trilha.LOGICA)[3], 2, menino, {}, {})
    }

    @Test fun casaComeco() = capturar("12_casa_comeco") {
        TelaCasa(estado(3, 0, menino), {}, {}, {})
    }

    @Test fun casaMeio() = capturar("13_casa_meio") {
        TelaCasa(estado(18, 14), {}, {}, {})
    }

    @Test fun casaCompleta() = capturar("14_casa_completa") {
        val e = estado(40, 40).let { it.pintar(br.com.casinhadosaber.dominio.Comodo.SALA) }
            .copy(posicaoCrianca = Posicao(0.45f, 0.99f))
        TelaCasa(e, {}, {}, {})
    }

    @Test fun casaDeNoite() = capturar("14b_casa_de_noite") {
        TelaCasa(estado(40, 40), {}, {}, {}, noiteInicial = true)
    }

    @Test fun dormir() = capturar("15b_hora_de_dormir") {
        br.com.casinhadosaber.ui.telas.TelaDormir(estado(20, 20)) {}
    }

    @Test fun jogoFacilitado() = capturar("17b_jogo_facilitado") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[3], menina, {}, { }, facilitada = true)
    }

    @Test fun loja() = capturar("18_loja") {
        val e = estado(30, 30).copy(moedas = 14, comprados = setOf("telhado1", "visitante🐰"), corTelhado = 1, visitantes = setOf("🐰"))
        TelaCasa(e, {}, {}, {}, lojaInicial = true)
    }

    @Test fun chapeus() = capturar("19_chapeus") {
        // Grade 2 x 4: menina e menino com cada chapéu, todos do mesmo tamanho.
        androidx.compose.foundation.layout.Column(
            Modifier.fillMaxSize().padding(top = 24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            for (chapeu in listOf("👑", "🧢", "🎩", "🌸")) {
                androidx.compose.foundation.layout.Row {
                    for (p in listOf(menina, menino)) {
                        br.com.casinhadosaber.ui.componentes.Crianca(
                            p.copy(chapeu = chapeu),
                            Modifier.height(170.dp).padding(horizontal = 30.dp, vertical = 14.dp),
                        )
                    }
                }
            }
        }
    }

    @Test fun chuva() = capturar("20_chuva") {
        TelaCasa(estado(40, 40).copy(clima = 1), {}, {}, {})
    }

    @Test fun neve() = capturar("20b_neve") {
        TelaCasa(estado(40, 40).copy(clima = 2), {}, {}, {}, noiteInicial = true)
    }

    @Test fun enfeites() = capturar("21_papel_de_parede_e_visitantes") {
        TelaCasa(
            estado(40, 40).copy(papelDeParede = 2, corPorta = 1, corTelhado = 2, visitantes = setOf("🦔", "🦊", "🐢")),
            {}, {}, {},
        )
    }

    /** Regressão: chuva e neve nunca podem aparecer dentro dos cômodos. */
    @Test fun chuvaNuncaCaiDentroDeCasa() {
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        val base = estado(40, 40)
        regra.mainClock.autoAdvance = false
        // As três cenas no mesmo quadro (mesmo instante das animações): sem clima, chuva e neve.
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    androidx.compose.foundation.layout.Row {
                        for (c in 0..2) {
                            androidx.compose.foundation.layout.Box(Modifier.size(200.dp, 230.dp)) {
                                br.com.casinhadosaber.ui.componentes.CenaDaCasa(base.copy(clima = c), Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(700)
        // Mesma captura das outras telas (Roborazzi); depois lê os pixels do arquivo.
        val arquivo = "build/outputs/roborazzi/23_teste_chuva_fora_de_casa.png"
        regra.onRoot().captureRoboImage(arquivo)
        val img = android.graphics.BitmapFactory.decodeFile(java.io.File(arquivo).absolutePath)
        val w = img.width / 3
        val h = img.height
        // Paredes da casa: x de 10% a 90%, y de 30% a 78% (com margem para o contorno).
        val x0 = (w * 0.12f).toInt(); val x1 = (w * 0.88f).toInt()
        val y0 = (h * 0.32f).toInt(); val y1 = (h * 0.76f).toInt()
        for (c in 1..2) {
            var diferentes = 0
            for (y in y0 until y1) for (x in x0 until x1) if (img.getPixel(x, y) != img.getPixel(x + c * w, y)) diferentes++
            org.junit.Assert.assertEquals("clima $c desenhou dentro de casa", 0, diferentes)
        }
    }

    @Test fun pais() = capturar("15_area_dos_pais") {
        TelaPais(estado(12, 12), true, {}, true, {}, {}, {}, {})
    }

    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    @Test fun inicioCelularPequeno() = capturar("16_inicio_celular_pequeno") {
        TelaInicio(estado(20, 20, menino), true, {}, {}, {}, {})
    }

    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    @Test fun jogoCelularPequeno() = capturar("17_jogo_celular_pequeno") {
        TelaJogo(gerador.fases(Trilha.INGLES)[7], menino, {}, { })
    }
}
