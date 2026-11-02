package br.com.casinhadosaber

import android.content.Context
import android.graphics.Bitmap
import android.speech.tts.TextToSpeech
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.services.storage.TestStorage
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Testes de ponta a ponta num Android de verdade (emulador do CI):
 * jogar uma fase inteira, abrir o presente, colocar na casa e arrastar um móvel com o dedo.
 * As capturas são gravadas pelo TestStorage e o CI copia para docs/emulador.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class JogoNoEmuladorTest {

    @get:Rule
    val regra = createEmptyComposeRule()

    private val contexto: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs get() = contexto.getSharedPreferences("casinha_do_saber", Context.MODE_PRIVATE)
    private var cenario: ActivityScenario<MainActivity>? = null
    private val armazenamento = TestStorage()

    @Before
    fun limpar() {
        prefs.edit().clear().commit()
    }

    @After
    fun fechar() {
        cenario?.close()
    }

    private fun capturar(nome: String) {
        regra.waitForIdle()
        val imagem = regra.onRoot().captureToImage().asAndroidBitmap()
        armazenamento.openOutputFile("$nome.png").use { imagem.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun esperar(descricao: String, timeout: Long = 10_000) =
        regra.waitUntilAtLeastOneExists(hasContentDescription(descricao, substring = true), timeout)

    @Test
    fun jogarUmaFaseEColocarOPremioNaCasa() {
        cenario = ActivityScenario.launch(MainActivity::class.java)

        // Escolher a menina
        regra.waitUntilAtLeastOneExists(hasText("Menina"), 15_000)
        regra.onAllNodes(hasText("Menina")).onFirst().performClick()
        capturar("e01_perfil")
        regra.onAllNodes(hasContentDescription("Pronto")).onFirst().performClick()

        // Início: abrir o caminho dos números
        esperar("Caminho Números")
        capturar("e02_inicio")
        regra.onAllNodes(hasContentDescription("Caminho Números", substring = true)).onFirst().performClick()

        // Primeira fase
        esperar("Fase 1, Contar até 3")
        capturar("e03_caminho")
        regra.onAllNodes(hasContentDescription("Fase 1, Contar até 3", substring = true)).onFirst().performClick()

        // Responder as 5 perguntas, errando a primeira de propósito
        regra.waitUntilAtLeastOneExists(hasTestTag("opcao_certa"), 10_000)
        capturar("e04_pergunta")
        regra.onAllNodes(hasTestTag("opcao")).onFirst().performClick()
        Thread.sleep(800)
        capturar("e05_erro")
        repeat(5) { i ->
            regra.waitUntilAtLeastOneExists(hasTestTag("opcao_certa"), 10_000)
            regra.onAllNodes(hasTestTag("opcao_certa")).onFirst().performClick()
            if (i == 0) {
                Thread.sleep(400)
                capturar("e06_acerto")
            }
            Thread.sleep(5_500)
        }

        // Prêmio: abrir o presente e levar para a casa
        esperar("Presente. Toque para abrir.", 15_000)
        capturar("e07_presente")
        regra.onAllNodes(hasContentDescription("Presente. Toque para abrir.")).onFirst().performClick()
        esperar("Colocar na casa")
        Thread.sleep(1_000)
        capturar("e08_premio_aberto")
        regra.onAllNodes(hasContentDescription("Colocar na casa")).onFirst().performClick()

        // Casa: tocar no terreno no baú constrói o terreno
        esperar("Terreno. Toque para colocar na casa.")
        capturar("e09_casa_bau")
        regra.onAllNodes(hasContentDescription("Terreno. Toque para colocar na casa.")).onFirst().performClick()
        Thread.sleep(1_500)
        capturar("e10_casa_com_terreno")
        regra.waitUntil(5_000) { prefs.getString("colocados", "").orEmpty().contains("terreno") }
        assertTrue("fase vencida não foi salva", prefs.getString("estrelas", "").orEmpty().startsWith("MATEMATICA:0="))
        // Errou uma vez: duas estrelas
        assertEquals("MATEMATICA:0=2", prefs.getString("estrelas", ""))
    }

    @Test
    fun arrastarACamaComODedo() {
        // Casa já construída, com a cama no quarto
        prefs.edit()
            .putString("genero", "MENINO").putString("nome", "Leo")
            .putString("estrelas", (0 until 6).joinToString(";") { "MATEMATICA:$it=3" })
            .putString("colocados", "terreno,0.5,0.5;paredes,0.5,0.5;telhado,0.5,0.5;porta,0.5,0.5;cama,0.27,0.528;janelas,0.5,0.5")
            .commit()
        cenario = ActivityScenario.launch(MainActivity::class.java)

        esperar("Minha casinha")
        regra.onAllNodes(hasContentDescription("Minha casinha", substring = true)).onFirst().performClick()
        esperar("Cama")
        capturar("e11_casa_antes_de_arrastar")
        val antes = prefs.getString("colocados", "")

        // Arrasta a cama para a sala (para baixo e para a direita)
        regra.onAllNodes(hasContentDescription("Cama")).onFirst().performTouchInput {
            swipe(center, center + Offset(width * 1.2f, height * 2.5f), durationMillis = 600)
        }
        regra.waitUntil(5_000) { prefs.getString("colocados", "") != antes }
        Thread.sleep(600)
        capturar("e12_casa_depois_de_arrastar")
        val cama = prefs.getString("colocados", "").orEmpty().split(";").first { it.startsWith("cama,") }.split(",")
        assertNotEquals("a cama não saiu do lugar", "0.528", cama[2])
        assertTrue("a cama deveria descer", cama[2].toFloat() > 0.6f)
    }

    @Test
    fun vozesEmPortuguesEIngles() {
        val pronto = CountDownLatch(1)
        var status = -1
        lateinit var tts: TextToSpeech
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            tts = TextToSpeech(contexto) { status = it; pronto.countDown() }
        }
        pronto.await(20, TimeUnit.SECONDS)
        val relatorio = StringBuilder()
        relatorio.appendLine("Motor de voz iniciado: ${status == TextToSpeech.SUCCESS} (motor: ${tts.defaultEngine})")
        if (status == TextToSpeech.SUCCESS) {
            for (l in listOf(Locale("pt", "BR"), Locale.US)) {
                val r = tts.isLanguageAvailable(l)
                relatorio.appendLine("$l: ${if (r >= TextToSpeech.LANG_AVAILABLE) "disponível" else "indisponível ($r)"}")
            }
        }
        tts.shutdown()
        armazenamento.openOutputFile("vozes.txt").use { it.write(relatorio.toString().toByteArray()) }
        // Não falha o teste: o relatório só registra o que o aparelho tem.
    }
}
