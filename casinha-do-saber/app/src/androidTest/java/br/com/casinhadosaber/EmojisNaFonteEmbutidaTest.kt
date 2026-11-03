package br.com.casinhadosaber

import android.graphics.Bitmap
import android.os.Build
import android.text.Spanned
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.EmojiSpan
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.services.storage.TestStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Confere, num Android de verdade (o CI roda também no Android 8, API 26), que todos os emojis
 * usados no app têm desenho na fonte Noto embutida (emoji2-bundled) e viram EmojiSpan,
 * em vez de cair na fonte do sistema (que nos celulares antigos mostra quadradinho).
 * A lista vem do código do app (tarefa Gradle listarEmojis).
 */
@RunWith(AndroidJUnit4::class)
class EmojisNaFonteEmbutidaTest {

    @get:Rule
    val regra = createAndroidComposeRule<ComponentActivity>()

    private val armazenamento = TestStorage()
    private val api = Build.VERSION.SDK_INT

    private fun emojisDoApp(): List<String> =
        InstrumentationRegistry.getInstrumentation().context.assets.open("emojis_do_app.txt")
            .bufferedReader().readLines().filter { it.isNotBlank() }

    private fun esperarFonte(): EmojiCompat {
        val compat = EmojiCompat.get()
        val carregou = CountDownLatch(1)
        compat.registerInitCallback(object : EmojiCompat.InitCallback() {
            override fun onInitialized() { carregou.countDown() }
            override fun onFailed(throwable: Throwable?) { carregou.countDown() }
        })
        carregou.await(30, TimeUnit.SECONDS)
        assertEquals("a fonte de emojis embutida carregou", EmojiCompat.LOAD_STATE_SUCCEEDED, compat.loadState)
        return compat
    }

    /** O texto inteiro virou um único EmojiSpan? */
    private fun virouEmojiSpan(compat: EmojiCompat, e: String): Boolean {
        val processado = compat.process(e) as? Spanned ?: return false
        val spans = processado.getSpans(0, processado.length, EmojiSpan::class.java)
        return spans.size == 1 && processado.getSpanStart(spans[0]) == 0 && processado.getSpanEnd(spans[0]) == processado.length
    }

    private fun codigos(e: String) = e.codePoints().toArray().joinToString(" ") { "U+%04X".format(it) }

    @OptIn(ExperimentalLayoutApi::class)
    @Test
    fun todosOsEmojisDoAppTemDesenhoNaFonteEmbutida() {
        val compat = esperarFonte()
        val emojis = emojisDoApp()
        assertTrue("a lista de emojis do app não está vazia", emojis.size > 100)

        val comSpan = mutableListOf<String>()
        val estiloTexto = mutableListOf<String>()
        val semGlifo = mutableListOf<String>()
        for (e in emojis) {
            val temGlifo = compat.getEmojiMatch(e, Int.MAX_VALUE) == EmojiCompat.EMOJI_SUPPORTED ||
                compat.getEmojiMatch(e + "️", Int.MAX_VALUE) == EmojiCompat.EMOJI_SUPPORTED
            when {
                virouEmojiSpan(compat, e) -> comSpan += e
                temGlifo -> estiloTexto += e
                else -> semGlifo += e
            }
        }

        val relatorio = buildString {
            appendLine("Emojis do app na fonte embutida (emoji2-bundled), Android API $api (${Build.VERSION.RELEASE})")
            appendLine("Verificados: ${emojis.size}")
            appendLine("Viraram EmojiSpan (desenho da fonte embutida): ${comSpan.size}")
            appendLine("Com glifo na fonte, mas sem FE0F (estilo texto, desenho do sistema): ${estiloTexto.size}")
            estiloTexto.forEach { appendLine("  $it  ${codigos(it)}") }
            appendLine("Sem glifo na fonte embutida: ${semGlifo.size}")
            semGlifo.forEach { appendLine("  $it  ${codigos(it)}") }
            appendLine()
            appendLine("Os novos: " + listOf("🪙", "🪶", "🪟").joinToString(" ") { "$it=${if (it in comSpan) "EmojiSpan" else "FALTA"}" })
        }
        armazenamento.openOutputFile("emojis_api$api.txt").use { it.write(relatorio.toByteArray()) }

        // Captura de uma tela com todos eles, desenhados pelo Compose como no app.
        regra.setContent {
            Column(Modifier.fillMaxSize().background(Color(0xFFFFF8E7)).padding(8.dp)) {
                Text("Android API $api: ${emojis.size} emojis do app", fontSize = 16.sp, color = Color.Black)
                Text("🪙 🪶 🪟", fontSize = 56.sp)
                FlowRow {
                    emojis.forEach { Text(it, fontSize = 21.sp, modifier = Modifier.padding(1.dp)) }
                }
            }
        }
        regra.waitForIdle()
        // A captura do Compose só existe a partir do Android 8 (API 26).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val imagem = regra.onRoot().captureToImage().asAndroidBitmap()
            armazenamento.openOutputFile("emojis_api$api.png").use { imagem.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }

        assertTrue("🪙 🪶 🪟 viram EmojiSpan", listOf("🪙", "🪶", "🪟").all { it in comSpan })
        assertTrue("emojis sem glifo na fonte embutida: $semGlifo", semGlifo.isEmpty())
    }
}
