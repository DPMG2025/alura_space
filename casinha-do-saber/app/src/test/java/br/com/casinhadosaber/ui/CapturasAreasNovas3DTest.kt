package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.ArteDasAreas
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.CenaDaArea
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaCasa
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Áreas novas da casa que cresce no visual brinquedo 3D (Fase 8 do PRD visual 3D): céu em degradê, ilha de grama
 * com a beirada de massinha, nuvens 3D e as coisas em 3D quando a arte existe (bichos, itens da casa, figuras das
 * perguntas; o resto continua emoji). Captura nova `v3d_18_areas_novas`: quatro áreas lado a lado (área do pet,
 * horta, parquinho e quarto novo) e a casa aberta na área do pet. Nenhuma captura antiga foi renomeada.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasAreasNovas3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)

    /** [fases] vencidas, as 40 peças colocadas e cada área ganha construída com as coisas no lugar de partida. */
    private fun estado(fases: Int): EstadoJogo {
        val estrelas = (0 until fases).associate { "${Trilha.entries[it % 4].name}:${it / 4}" to 3 }
        var e = EstadoJogo(perfil = menina, estrelas = estrelas)
        for (item in ItensDaCasa.ordem) e = e.colocar(item)
        for (item in e.itensGanhos.filter { it.area != null }) e = e.colocar(item)
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

    @Test
    fun areasNovas() {
        val e = estado(600)
        val manifesto = ArteDoApp.manifesto(RuntimeEnvironment.getApplication())
        // Antes de capturar: as áreas da foto já estão construídas e cada uma tem alguma coisa em 3D.
        val ids = listOf("pet", "horta", "parquinho", "quarto2")
        for (id in ids) {
            val area = CasaQueCresce.porId.getValue(id)
            assertTrue("$id não foi construída", area.obra.id in e.colocados)
            val com3D = area.coisas.filter { it.id in e.colocados }.count { ArteDasAreas.para(manifesto, it) != null }
            assertTrue("$id sem nenhuma coisa em 3D", com3D > 0)
        }
        assertTrue("faltam as nuvens 3D", "ceu_nuvem_1" in manifesto.imagens && "ceu_nuvem_2" in manifesto.imagens)
        montar {
            Column(Modifier.fillMaxSize().padding(6.dp)) {
                for (linha in ids.chunked(2)) {
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        for (id in linha) {
                            CenaDaArea(
                                area = CasaQueCresce.porId.getValue(id),
                                estado = e,
                                modifier = Modifier.weight(1f).fillMaxSize().padding(4.dp).clip(RoundedCornerShape(18.dp)),
                                interativa = false,
                            )
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(2500)
        for (id in ids) {
            assertTrue("$id não aparece", regra.onAllNodes(hasContentDescription(CasaQueCresce.porId.getValue(id).nome)).fetchSemanticsNodes().isNotEmpty())
        }
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_18_areas_novas.png")
    }

    /** A casa aberta na área do pet: título com o nome da área, estrelas no lugar do "x/y" e nada de "coisas". */
    @Test
    fun casaNaAreaDoPet() {
        montar { TelaCasa(estado(600), {}, {}, {}, paginaInicial = "pet") }
        regra.mainClock.advanceTimeBy(2500)
        assertTrue(regra.onAllNodes(hasText("Área do pet")).fetchSemanticsNodes().isNotEmpty())
        assertTrue("texto com '/' na casa", regra.onAllNodes(hasText("/", substring = true)).fetchSemanticsNodes().isEmpty())
        assertTrue("texto 'coisas' na casa", regra.onAllNodes(hasText("coisas", substring = true)).fetchSemanticsNodes().isEmpty())
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_18b_casa_na_area_do_pet.png")
    }
}
