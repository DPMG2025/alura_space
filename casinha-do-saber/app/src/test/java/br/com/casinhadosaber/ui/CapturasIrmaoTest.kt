package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.Irmaozinho
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.CartaoDoIrmao
import br.com.casinhadosaber.ui.telas.TelaCasa
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.TelaPremio
import br.com.casinhadosaber.ui.telas.comIrmao
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

/** Capturas do visual novo: rostos das crianças, o irmãozinho nas telas e o cartão dele na área dos pais. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasIrmaoTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val comIrma = Perfil("Bia", Genero.MENINA, pele = 0, cabelo = 2, estiloCabelo = 1, irmao = Irmao(genero = Genero.MENINA, nome = "Lia"))
    private val menino = Perfil("Leo", Genero.MENINO, pele = 3, cabelo = 1, estiloCabelo = 1)
    private val gerador = GeradorDeFases(Random(4))

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

    private fun capturar(nome: String, tempo: Long = 2500, conteudo: @Composable () -> Unit) {
        montar(conteudo)
        regra.mainClock.advanceTimeBy(tempo)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    /** Todas as crianças e irmãos lado a lado, em cada humor (feliz, torcendo, pensando, falando). */
    @Test fun rostos() = capturar("70_rostos_e_irmaos") {
        Column(
            Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Dois humores por fileira (os quatro numa fileira só passavam da borda direita em 393 dp e se
            // sobrepunham): cada par criança e irmão numa metade da largura, sem encostar no vizinho.
            for (p in listOf(menina, comIrma, menino)) {
                for (humores in listOf(Humor.FELIZ, Humor.TORCENDO, Humor.PENSANDO, Humor.SURPRESO).chunked(2)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                        for (h in humores) {
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                                Crianca(p, Modifier.height(122.dp), humor = h, animado = false)
                                Irmaozinho(p, Modifier.height(80.dp), humor = h, animado = false)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test fun jogoComIrmao() = capturar("71_jogo_com_irmao") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[1], menina, {}, { })
    }

    @Test fun jogoComIrmaNoCaminhoDasLetras() = capturar("72_jogo_letras_com_irma") {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[4], comIrma, {}, { })
    }

    @Test fun premioComIrmao() = capturar("73_premio_com_irmao") {
        TelaPremio(gerador.fases(Trilha.INGLES)[2], 3, ItensDaCasa.porId.getValue("cama"), menino, 5, true, {}, {}, {})
    }

    @Test fun casaComIrmao() = capturar("74_casa_com_irmao") {
        var e = EstadoJogo(perfil = menina)
        for (item in ItensDaCasa.ordem.take(20)) e = e.colocar(item)
        TelaCasa(e, {}, {}, {})
    }

    /** O cartão dos pais troca para irmãzinha, dá o nome e tira o irmão; o estado guardado acompanha. */
    @Test fun cartaoDosPais() {
        var estado by mutableStateOf(EstadoJogo(perfil = menina))
        montar {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                CartaoDoIrmao(estado.perfil, onSalvar = { i -> estado = estado.comIrmao(i) })
            }
        }
        regra.mainClock.advanceTimeBy(600)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/75_pais_irmaozinho.png")
        regra.onNodeWithTag("irmao_menina").performClick()
        regra.mainClock.advanceTimeBy(600)
        assertEquals(Genero.MENINA, estado.perfil!!.irmao.genero)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/75b_pais_irmazinha.png")
        regra.onNodeWithTag("irmao_sem").performClick()
        regra.mainClock.advanceTimeBy(600)
        assertFalse(estado.perfil!!.irmao.ligado)
    }
}
