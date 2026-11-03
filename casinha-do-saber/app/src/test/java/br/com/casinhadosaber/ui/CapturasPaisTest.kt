package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Aniversario
import br.com.casinhadosaber.dominio.Data
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Pais
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.RegistroDeSessao
import br.com.casinhadosaber.dominio.TemaFavorito
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.desafioDosPaisFeito
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.CartaoAniversario
import br.com.casinhadosaber.ui.telas.CartaoDesafioDosPais
import br.com.casinhadosaber.ui.telas.CartaoRelatorioDaSemana
import br.com.casinhadosaber.ui.telas.CartaoTemasFavoritos
import br.com.casinhadosaber.ui.telas.TelaPais
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Capturas dos cartões novos da área dos pais (prefixo s7_): desafio do dia, resumo da semana,
 * aniversário e temas favoritos. Mesmo jeito de CapturasTest: relógio parado, narrador e sons desligados.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasPaisTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val gerador = GeradorDeFases()

    /** Quarta feira, 4 de novembro de 2026: a semana vai de 2 a 8 de novembro. */
    private val hoje = Data(2026, 11, 4).diaEpoca

    private fun titulo(t: Trilha, n: Int) = gerador.fases(t)[n].titulo

    /** Uma semana com partidas em 3 dias: pontos fortes em Números, reforço em Letras, um pouco de Inglês. */
    private val sessoes: List<RegistroDeSessao> = listOf(
        RegistroDeSessao(hoje - 2, Trilha.MATEMATICA, 0, 3, 5, 5, 240, faseNova = true),
        RegistroDeSessao(hoje - 2, Trilha.MATEMATICA, 1, 3, 5, 5, 260, faseNova = true),
        RegistroDeSessao(hoje - 1, Trilha.PORTUGUES, 3, 0, 2, 5, 300),
        RegistroDeSessao(hoje - 1, Trilha.PORTUGUES, 3, 2, 4, 5, 280, faseNova = true),
        RegistroDeSessao(hoje, Trilha.INGLES, 0, 3, 5, 5, 200, faseNova = true),
        RegistroDeSessao(hoje, Trilha.LOGICA, 2, 0, 1, 5, 310),
    )

    private fun montar(conteudo: @Composable () -> Unit) {
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) { conteudo() }
            }
        }
    }

    private fun capturar(nome: String, conteudo: @Composable () -> Unit) {
        montar {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Cores.Fundo)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
            ) { conteudo() }
        }
        regra.mainClock.advanceTimeBy(1500)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    /**
     * performScrollTo espera a rolagem animada terminar: com o relógio parado ela nunca termina e o teste trava.
     * Liga o relógio só durante a rolagem (a área dos pais não tem animação sem fim).
     */
    private fun rolarAte(tag: String): androidx.compose.ui.test.SemanticsNodeInteraction {
        regra.mainClock.autoAdvance = true
        try {
            regra.onNodeWithTag(tag).performScrollTo()
            regra.waitForIdle()
        } finally {
            regra.mainClock.autoAdvance = false
        }
        return regra.onNodeWithTag(tag)
    }

    private fun relatorio(registros: List<RegistroDeSessao>) = Pais.relatorioDaSemana(registros, hoje, ::titulo)

    @Test fun desafioEResumoDaSemana() = capturar("s7_01_desafio_e_resumo") {
        CartaoDesafioDosPais(Pais.desafioDoDia(hoje), feito = false, onFizemos = {})
        CartaoRelatorioDaSemana(relatorio(sessoes))
    }

    @Test fun desafioFeitoEResumoVazio() = capturar("s7_02_desafio_feito_resumo_vazio") {
        CartaoDesafioDosPais(Pais.desafioDoDia(hoje + 1), feito = true, onFizemos = {})
        CartaoRelatorioDaSemana(relatorio(emptyList()))
    }

    @Test fun aniversarioETemas() = capturar("s7_03_aniversario_e_temas") {
        CartaoAniversario(Aniversario(12, 3), onSalvar = {})
        CartaoTemasFavoritos(listOf(TemaFavorito.DINOSSAUROS, TemaFavorito.ESPACO, TemaFavorito.FUNDO_DO_MAR), onAlternar = {})
    }

    @Test fun aniversarioAindaNaoEscolhido() = capturar("s7_04_aniversario_vazio") {
        CartaoAniversario(null, onSalvar = {})
        CartaoTemasFavoritos(listOf(TemaFavorito.ANIMAIS), onAlternar = {})
    }

    /**
     * Área dos pais inteira, depois do portão: o adulto toca em "Fizemos!", a criança ganha as moedinhas uma vez,
     * o cartão mostra "feito"; o aniversário e um tema escolhidos com os botões grandes ficam no perfil.
     */
    @Test fun areaDosPaisFizemos() {
        var estado by mutableStateOf(EstadoJogo(perfil = menina, moedas = 7, sessoes = sessoes))
        montar {
            TelaPais(
                estado, true, {}, true, {}, {}, {}, {},
                hoje = hoje,
                onAtualizar = { mudanca -> estado = mudanca(estado) },
            )
        }
        regra.mainClock.advanceTimeBy(500)
        val conta = regra.onNode(hasText("Quanto é", substring = true)).fetchSemanticsNode()
            .config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }.orEmpty()
        val (a, b) = Regex("""(\d+) × (\d+)""").find(conta)!!.destructured
        for (d in (a.toInt() * b.toInt()).toString()) {
            regra.onAllNodes(hasContentDescription(d.toString())).onFirst().performClick()
        }
        regra.onAllNodes(hasContentDescription("OK")).onFirst().performClick()
        regra.mainClock.advanceTimeBy(800)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s7_05_pais_com_desafio.png")

        rolarAte("fizemos").performClick()
        regra.mainClock.advanceTimeBy(800)
        assertTrue(estado.desafioDosPaisFeito(hoje))
        assertEquals(7 + Pais.MOEDAS_DO_DESAFIO, estado.moedas)
        assertEquals(1, regra.onAllNodesWithTag("desafio_feito").fetchSemanticsNodes().size)
        assertEquals(0, regra.onAllNodesWithTag("fizemos").fetchSemanticsNodes().size)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s7_06_pais_desafio_feito.png")

        // Aniversário: 1 de janeiro, dois toques no "mais" do dia e um no "menos" do mês = 3 de dezembro.
        rolarAte("aniversario_dia_mais").performClick()
        regra.mainClock.advanceTimeBy(100)
        regra.onNodeWithTag("aniversario_dia_mais").performClick()
        regra.mainClock.advanceTimeBy(100)
        regra.onNodeWithTag("aniversario_mes_menos").performClick()
        regra.mainClock.advanceTimeBy(100)
        regra.onNodeWithTag("aniversario_salvar").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals(Aniversario(3, 12), estado.perfil?.aniversario)

        // Temas: escolher dinossauros e depois tirar.
        rolarAte("tema_DINOSSAUROS").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals(listOf(TemaFavorito.DINOSSAUROS), estado.perfil?.temasFavoritos)
        regra.onNodeWithTag("tema_ESPACO").performClick()
        regra.mainClock.advanceTimeBy(100)
        regra.onNodeWithTag("tema_DINOSSAUROS").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals(listOf(TemaFavorito.ESPACO), estado.perfil?.temasFavoritos)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s7_07_pais_aniversario_e_temas.png")

        // Tocar de novo não dá moedas de novo (o botão sumiu; o domínio também protege).
        assertEquals(7 + Pais.MOEDAS_DO_DESAFIO, estado.moedas)
    }
}
