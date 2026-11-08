package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Cena
import br.com.casinhadosaber.dominio.Fase
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaJogo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Relato do dono: o app fechava no tablet ao vencer. Causa (lida no registro de fechamentos do aparelho):
 * depois de 2 tentativas sem vencer, a fase vem "facilitada" (uma pergunta de exemplo a mais, 6 no total). Ao
 * vencer, o jogo grava o resultado e a fase deixa de ser facilitada; a lista de perguntas era refeita com 5 e a
 * partida, já na sexta (posição 5), lia `perguntas[5]`: "IndexOutOfBoundsException: index: 5, size: 5".
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class VitoriaNaRodadaFacilitadaTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)

    private val pergunta = Pergunta(
        "Quantos patinhos tem na lagoa?",
        listOf(pt("Quantos patinhos tem na lagoa?")),
        Cena.Emojis("🦆🦆🦆🦆"),
        listOf("3", "5", "4", "6").map { Opcao(Visual.Texto(it), listOf(pt(it))) },
        indiceCorreto = 2,
    )

    @Test
    fun vencerARodadaFacilitadaNaoFechaOApp() {
        var facilitada by mutableStateOf(true)
        var resultados: List<Boolean>? = null
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    Box(Modifier.fillMaxSize()) {
                        TelaJogo(
                            Fase(Trilha.MATEMATICA, 1, "Contar até 5", "🦆"),
                            menina,
                            {},
                            {
                                // Como no app: ao concluir, o resultado é gravado e a fase deixa de ser facilitada
                                // enquanto a tela do jogo ainda está composta.
                                resultados = it
                                facilitada = false
                            },
                            facilitada = facilitada,
                            perguntasProntas = List(5) { pergunta },
                        )
                    }
                }
            }
        }
        // O exemplo + 5 perguntas = 6 respostas certas.
        repeat(6) {
            regra.mainClock.advanceTimeBy(1_500)
            regra.onAllNodesWithTag("opcao_certa").onFirst().performClick()
            Snapshot.sendApplyNotifications()
            regra.mainClock.advanceTimeBy(6_500)
        }
        regra.mainClock.advanceTimeBy(3_000)
        assertNotNull("a partida deveria ter terminado", resultados)
        assertEquals("o exemplo não conta: 5 resultados", 5, resultados!!.size)
    }
}
