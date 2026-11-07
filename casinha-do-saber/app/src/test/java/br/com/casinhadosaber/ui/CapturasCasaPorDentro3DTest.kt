package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Andar
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.PlantaDaCasa
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.AparenciaDoAndar
import br.com.casinhadosaber.ui.componentes.ArteDaCasaPorDentro
import br.com.casinhadosaber.ui.componentes.CamadasDaCasaPorDentro
import br.com.casinhadosaber.ui.componentes.GradeIsometrica
import br.com.casinhadosaber.ui.componentes.PontoDaPlanta
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaCasaPorDentro
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * A casa por dentro com a arte 3D (Fase 6 do PRD visual 3D). Captura nova: `v3d_13_casa_por_dentro` (a casa cheia,
 * de dia) e `v3d_13b_casa_por_dentro_noite` (de noite, com o papel de bolinhas e outras cores de parede). As capturas
 * 32 a 37 (`CapturasTest`) mostram a casa nova sem mudar de código. Antes de capturar, confere que o fundo do andar
 * é composto pela arte (e não pelo desenho antigo) e que as imagens dos móveis são lidas.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasCasaPorDentro3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val gerador = GeradorDeFases(Random(4))

    private fun estado(n: Int, colocados: Int): EstadoJogo {
        var e = EstadoJogo(perfil = menina)
        val fases = (0 until 10).flatMap { i -> Trilha.entries.map { gerador.fases(it)[i] } }
        for ((k, f) in fases.take(n).withIndex()) e = e.registrarVitoria(f, 1 + k % 3).first
        for (item in ItensDaCasa.ordem.take(colocados)) e = e.colocar(item)
        return e
    }

    private fun montar(conteudo: @Composable () -> Unit) {
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
    }

    /** O fundo dos dois andares sai da arte (bitmap opaco no meio do piso e da parede) e os móveis têm imagem. */
    private fun conferirArte(e: EstadoJogo, noite: Boolean) {
        val arte = ArteDaCasaPorDentro(RuntimeEnvironment.getApplication())
        for (a in Andar.entries) {
            val moveis = PlantaDaCasa.moveisDe(e, a)
            val ap = AparenciaDoAndar.de(e, a, moveis, noite)
            var vetoriais = 0
            val fundo = arte.fundo(ap, 100f, Color.Black, 1f) { c, _ -> if (c.id != null) vetoriais++; emptyList() }
            assertNotNull("fundo do $a não foi composto", fundo)
            assertEquals("cascas do $a desenhadas pelo desenho antigo (WebP ilegível?)", 0, vetoriais)
            val bmp = fundo!!.imagem.asAndroidBitmap()
            fun alfa(p: PontoDaPlanta): Int {
                val x = GradeIsometrica.x(fundo.ladrilho, -fundo.esquerda, p.x, p.y).roundToInt()
                val y = GradeIsometrica.y(fundo.ladrilho, -fundo.topo, p.x, p.y, p.z).roundToInt()
                return android.graphics.Color.alpha(bmp.getPixel(x, y))
            }
            assertEquals("meio do piso do $a", 255, alfa(PontoDaPlanta(2.5f, 2.5f, 0f)))
            assertEquals("parede do fundo do $a", 255, alfa(PontoDaPlanta(1.0f, 0f, 2.2f)))
            for (m in moveis) {
                val id = CamadasDaCasaPorDentro.idDoMovel(m)
                assertNotNull("sem imagem para $id", arte.sprite(id, 100f, 0f, 0f, CamadasDaCasaPorDentro.referenciaDoMovel(m)))
            }
        }
        arte.limpar()
    }

    private fun capturar(nome: String, e: EstadoJogo, noite: Boolean) {
        conferirArte(e, noite)
        montar { TelaCasaPorDentro(e, {}, {}, noite = noite) }
        regra.mainClock.advanceTimeBy(2500)
        // A cena continua tocável e descrita como antes (os testes do emulador procuram por ela).
        assertTrue(regra.onAllNodes(hasContentDescription("Casa por dentro", substring = true)).fetchSemanticsNodes().isNotEmpty())
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    @Test fun casaPorDentro() = capturar("v3d_13_casa_por_dentro", estado(40, 40), noite = false)

    @Test fun casaPorDentroDeNoite() {
        val e = estado(40, 40)
        capturar(
            "v3d_13b_casa_por_dentro_noite",
            e.copy(papelDeParede = 2, corPorta = 2, coresComodos = mapOf(br.com.casinhadosaber.dominio.Comodo.SALA to 2, br.com.casinhadosaber.dominio.Comodo.QUARTO to 1)),
            noite = true,
        )
    }
}
