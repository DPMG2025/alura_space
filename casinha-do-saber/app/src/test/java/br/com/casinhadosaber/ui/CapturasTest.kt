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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.platform.testTag
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

    /**
     * Regressão: chuva e neve nunca podem aparecer dentro dos cômodos.
     *
     * As três cenas (sem clima, chuva e neve) ficam lado a lado no mesmo quadro, então as
     * animações estão no mesmo instante. Cada cena tem 120 x 138 dp (a proporção exata da cena,
     * 1,15) e as três cabem juntas na largura da tela (360 de 393 dp). Cada uma é capturada
     * sozinha, pelo seu testTag, e comparada pixel a pixel com a cena sem clima só dentro da casa
     * (paredes e telhado, com margem para o contorno). Qualquer diferença ali é chuva ou neve.
     */
    @Test fun chuvaNuncaCaiDentroDeCasa() {
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        val base = estado(40, 40)
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    androidx.compose.foundation.layout.Row {
                        for (c in 0..2) {
                            Box(Modifier.size(120.dp, 138.dp).testTag("clima$c")) {
                                br.com.casinhadosaber.ui.componentes.CenaDaCasa(base.copy(clima = c), Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(700)
        // Imagem das três cenas juntas, só para conferir a olho em docs/capturas.
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/23_teste_chuva_fora_de_casa.png")
        // Uma captura por cena, lida do arquivo recém gravado (apagado antes para não ler um antigo).
        val cenas = (0..2).map { c ->
            val arquivo = java.io.File("build/outputs/roborazzi/teste_chuva/clima_$c.png")
            arquivo.parentFile?.mkdirs()
            arquivo.delete()
            regra.onNodeWithTag("clima$c").captureRoboImage(arquivo.path)
            org.junit.Assert.assertTrue("captura da cena $c não foi gravada", arquivo.exists())
            android.graphics.BitmapFactory.decodeFile(arquivo.absolutePath)
                ?: throw AssertionError("captura da cena $c ilegível")
        }
        val w = cenas[0].width
        val h = cenas[0].height
        for (img in cenas) org.junit.Assert.assertTrue("cenas com tamanhos diferentes", img.width == w && img.height == h)
        // A cena precisa ter a proporção certa, senão as frações abaixo não apontam para a casa.
        org.junit.Assert.assertEquals("proporção da cena", w * 1.15f, h.toFloat(), 3f)

        // Medidas da casa (Planta em CenaDaCasa.kt): paredes de x 0,10 a 0,90 e y 0,30 a 0,78;
        // telhado é o triângulo (0,04; 0,315), (0,5; 0,075), (0,96; 0,315).
        val m = 3f // margem em pixels, longe do contorno e da camadinha de neve do telhado
        fun dentroDasParedes(x: Int, y: Int) =
            x > 0.10f * w + m && x < 0.90f * w - m && y > 0.30f * h + m && y < 0.78f * h - m
        fun dentroDoTelhado(x: Int, y: Int): Boolean {
            // Abaixo da camada de neve (que vai da borda até a linha (0,07; 0,318)-(0,5; 0,095)).
            val fx = x.toFloat() / w
            val fy = y.toFloat() / h
            if (fy > 0.30f) return false
            val linhaDaNeve = 0.095f + (0.318f - 0.095f) * (kotlin.math.abs(fx - 0.5f) / 0.43f)
            return y > (linhaDaNeve * h) + m && fx > 0.07f && fx < 0.93f
        }
        val referencia = cenas[0]
        for (c in 1..2) {
            val img = cenas[c]
            var dentro = 0
            var fora = 0
            for (y in 0 until h) for (x in 0 until w) {
                if (img.getPixel(x, y) == referencia.getPixel(x, y)) continue
                if (dentroDasParedes(x, y) || dentroDoTelhado(x, y)) dentro++ else fora++
            }
            // Sem isto o teste passaria à toa se o clima não fosse desenhado.
            org.junit.Assert.assertTrue("clima $c não desenhou nada do lado de fora", fora > 50)
            org.junit.Assert.assertEquals("clima $c desenhou dentro de casa", 0, dentro)
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
