package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Copia
import br.com.casinhadosaber.dominio.CopiaDoProgresso
import br.com.casinhadosaber.dominio.CriancaDaCasa
import br.com.casinhadosaber.dominio.CriancaGuardada
import br.com.casinhadosaber.dominio.Data
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.ValorGuardado
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.AcoesDaCopia
import br.com.casinhadosaber.ui.telas.AcoesDaFamilia
import br.com.casinhadosaber.ui.telas.CaixaDeDialogo
import br.com.casinhadosaber.ui.telas.CartaoCopiaDoProgresso
import br.com.casinhadosaber.ui.telas.ConfirmarRecuperarCopia
import br.com.casinhadosaber.ui.telas.TelaPais
import br.com.casinhadosaber.ui.telas.TelaQuemVaiJogar
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
 * Capturas das várias crianças e da cópia do progresso (prefixo s9_): "Quem vai jogar?", os cartões da área dos
 * pais e a confirmação de recuperar a cópia. Mesmo jeito de CapturasPaisTest: relógio parado, sem som.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasFamiliaTest {

    @get:Rule
    val regra = createComposeRule()

    private val ana = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val leo = Perfil("Leo", Genero.MENINO, pele = 4, cabelo = 0, estiloCabelo = 2)
    private val criancas = listOf(
        CriancaDaCasa("1", ana),
        CriancaDaCasa("2", leo),
        CriancaDaCasa("3", Perfil("", Genero.MENINA, pele = 1, cabelo = 3, estiloCabelo = 1)),
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
            Column(Modifier.fillMaxSize().background(Cores.Fundo).padding(20.dp)) { conteudo() }
        }
        regra.mainClock.advanceTimeBy(1500)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

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

    private fun passarPeloPortao() {
        val conta = regra.onNode(hasText("Quanto é", substring = true)).fetchSemanticsNode()
            .config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }.orEmpty()
        val (a, b) = Regex("""(\d+) × (\d+)""").find(conta)!!.destructured
        for (d in (a.toInt() * b.toInt()).toString()) {
            regra.onAllNodes(hasContentDescription(d.toString())).onFirst().performClick()
        }
        regra.onAllNodes(hasContentDescription("OK")).onFirst().performClick()
        regra.mainClock.advanceTimeBy(800)
    }

    private val copiaDeExemplo = CopiaDoProgresso(
        criancas = listOf(
            CriancaGuardada("1", mapOf("nome" to ValorGuardado.Texto("Ana"), "estrelas" to ValorGuardado.Texto("MATEMATICA:0=3;MATEMATICA:1=3;LOGICA:0=2"))),
            CriancaGuardada("2", mapOf("nome" to ValorGuardado.Texto("Leo"), "estrelas" to ValorGuardado.Texto("PORTUGUES:0=3"))),
        ),
        ativa = "1",
        feitaEm = Data(2026, 11, 3),
    )

    @Test
    fun quemVaiJogar() {
        var escolhida: String? = null
        montar { TelaQuemVaiJogar(criancas) { escolhida = it } }
        regra.mainClock.advanceTimeBy(1500)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s9_01_quem_vai_jogar.png")
        // O nome em branco vira "Criança 3" (nunca um nome fixo no código).
        assertTrue(regra.onAllNodes(hasText("Criança 3")).fetchSemanticsNodes().isNotEmpty())
        regra.onNodeWithTag("crianca_2").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals("2", escolhida)
    }

    @Test
    fun quemVaiJogarComCriancaNova() {
        montar { TelaQuemVaiJogar(listOf(CriancaDaCasa("1", ana), CriancaDaCasa("2", null))) {} }
        regra.mainClock.advanceTimeBy(1500)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s9_02_quem_vai_jogar_crianca_nova.png")
    }

    @Test
    fun cartaoDaCopia() = capturar("s9_03_cartao_da_copia") {
        CartaoCopiaDoProgresso(AcoesDaCopia(onGuardar = {}, onEnviar = {}, onRecuperar = {}))
    }

    @Test
    fun confirmarRecuperar() = capturar("s9_04_confirmar_recuperar_copia") {
        ConfirmarRecuperarCopia(copiaDeExemplo, onConfirmar = {}, onCancelar = {})
    }

    @Test
    fun copiaRecusada() = capturar("s9_05_copia_recusada") {
        CaixaDeDialogo("Cópia do progresso", Copia.VERSAO_NOVA, "OK", "copia_aviso_ok", onConfirmar = {}, onCancelar = null)
    }

    /**
     * Área dos pais com as crianças e a cópia: trocar de criança, adicionar, guardar e recuperar chamam o app;
     * a cópia lida aparece para confirmar e só é aplicada no "Recuperar".
     */
    @Test
    fun areaDosPaisComCriancasECopia() {
        var ativa by mutableStateOf("1")
        var lista by mutableStateOf(criancas)
        var pendente by mutableStateOf<CopiaDoProgresso?>(null)
        var guardou = 0
        var pediuRecuperar = 0
        var recuperada: CopiaDoProgresso? = null
        var apagada: String? = null
        var adicionou = 0
        montar {
            TelaPais(
                EstadoJogo(perfil = lista.first { it.id == ativa }.perfil), true, {}, true, {}, {}, {}, {},
                familia = AcoesDaFamilia(
                    criancas = lista,
                    ativa = ativa,
                    onAdicionar = { adicionou++ },
                    onJogarCom = { ativa = it },
                    onRenomear = { id, nome -> lista = lista.map { if (it.id == id) it.copy(perfil = it.perfil?.copy(nome = nome)) else it } },
                    onApagar = { id -> apagada = id; lista = lista.filter { it.id != id } },
                ),
                copia = AcoesDaCopia(
                    onGuardar = { guardou++ },
                    onEnviar = {},
                    onRecuperar = { pediuRecuperar++; pendente = copiaDeExemplo },
                    pendente = pendente,
                    onConfirmar = { recuperada = pendente; pendente = null },
                    onCancelar = { pendente = null },
                ),
            )
        }
        regra.mainClock.advanceTimeBy(500)
        passarPeloPortao()
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s9_06_pais_criancas.png")

        rolarAte("familia_jogar_2").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals("2", ativa)

        rolarAte("familia_adicionar").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals(1, adicionou)

        // Apagar pede confirmação (atrás do portão e de uma pergunta).
        rolarAte("familia_apagar_3").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals(null, apagada)
        regra.onNodeWithTag("familia_confirmar_apagar").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals("3", apagada)
        assertEquals(2, lista.size)

        rolarAte("copia_guardar")
        regra.mainClock.advanceTimeBy(300)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s9_07_pais_copia.png")
        regra.onNodeWithTag("copia_guardar").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals(1, guardou)

        // Recuperar: a cópia lida espera a confirmação; cancelar não muda nada, confirmar aplica.
        rolarAte("copia_recuperar").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals(1, pediuRecuperar)
        assertTrue(regra.onAllNodes(hasText("Isso troca o progresso atual pelo da cópia", substring = true)).fetchSemanticsNodes().isNotEmpty())
        regra.onNodeWithTag("dialogo_cancelar").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals(null, recuperada)

        rolarAte("copia_recuperar").performClick()
        regra.mainClock.advanceTimeBy(300)
        regra.onNodeWithTag("copia_confirmar").performClick()
        regra.mainClock.advanceTimeBy(300)
        assertEquals(copiaDeExemplo, recuperada)
    }
}
