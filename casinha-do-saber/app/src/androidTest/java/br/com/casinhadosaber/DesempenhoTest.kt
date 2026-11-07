package br.com.casinhadosaber

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.services.storage.TestStorage
import br.com.casinhadosaber.ui.telas.PartidaNaTela
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Fase 9 do PRD do visual 3D: desempenho num Android antigo (o CI roda este teste no API 24 e no API 26).
 *
 * Mede o tempo de abertura (da `MainActivity` lançada até a tela inicial composta, com o "Caminho Números" na
 * árvore de semântica) e a memória do processo (PSS total, via `Debug.MemoryInfo`) na tela inicial, na casa, na
 * casa por dentro com a lojinha aberta (roupas experimentadas na criança) e numa pergunta do jogo. Os números vão
 * para `desempenho.txt` (TestStorage; o CI versiona em docs/emulador/apiNN/) e para o logcat (tag DESEMPENHO).
 *
 * As asserções são frouxas, só para pegar regressão grosseira: abrir em menos de [LIMITE_ABERTURA_MS] e PSS abaixo
 * de [LIMITE_PSS_MB] em todas as telas. O processo do teste é o mesmo do app (instrumentação), então o PSS inclui
 * um pouco do executor de testes; o `Application` já foi criado antes do teste, então a abertura medida é a da
 * atividade (o que a criança espera depois do ícone, menos o início do processo).
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class DesempenhoTest {

    @get:Rule
    val regra = createEmptyComposeRule()

    private val contexto: Context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val prefs get() = contexto.getSharedPreferences("casinha_do_saber", Context.MODE_PRIVATE)
    private var cenario: ActivityScenario<MainActivity>? = null
    private val armazenamento = TestStorage()
    private val relatorio = StringBuilder()

    @Before
    fun prepararSave() {
        prefs.edit().clear().commit()
        contexto.getSharedPreferences("casinha_aparelho", Context.MODE_PRIVATE).edit().clear().commit()
        PartidaNaTela.limpar()
        // Uma criança com a casa construída, o mascote já escolhido e as 6 primeiras fases dos números vencidas.
        prefs.edit()
            .putString("genero", "MENINA").putString("nome", "Ana")
            .putString("mascote", "GATINHO||")
            .putInt("moedas", 30)
            .putString("estrelas", (0 until 6).joinToString(";") { "MATEMATICA:$it=3" })
            .putString("colocados", "terreno,0.5,0.5;paredes,0.5,0.5;telhado,0.5,0.5;porta,0.5,0.5;cama,0.27,0.528;janelas,0.5,0.5")
            .commit()
    }

    @After
    fun fechar() {
        cenario?.close()
        cenario = null
    }

    private fun esperar(descricao: String, timeout: Long = 20_000) =
        regra.waitUntilAtLeastOneExists(hasContentDescription(descricao, substring = true), timeout)

    private fun tocar(descricao: String) {
        val no = regra.onAllNodes(hasContentDescription(descricao, substring = true)).onFirst()
        runCatching { no.performSemanticsAction(SemanticsActions.OnClick) }.onFailure { no.performClick() }
    }

    private fun avancar(ms: Long) {
        regra.mainClock.advanceTimeBy(ms)
        regra.waitForIdle()
    }

    /** Lança a atividade e devolve quanto tempo levou até a tela inicial composta (ms). */
    private fun abrir(): Long {
        cenario?.close()
        cenario = null
        PartidaNaTela.limpar()
        val inicio = SystemClock.elapsedRealtime()
        cenario = ActivityScenario.launch(MainActivity::class.java)
        esperar("Caminho Números", 30_000)
        return SystemClock.elapsedRealtime() - inicio
    }

    private class Memoria(val pssMb: Int, val javaMb: Int, val nativoMb: Int, val graficosMb: Int)

    private fun medirMemoria(): Memoria {
        val mi = Debug.MemoryInfo()
        Debug.getMemoryInfo(mi)
        fun stat(nome: String) = (mi.getMemoryStat(nome)?.toIntOrNull() ?: 0) / 1024
        return Memoria(
            pssMb = mi.totalPss / 1024,
            javaMb = stat("summary.java-heap"),
            nativoMb = stat("summary.native-heap"),
            graficosMb = stat("summary.graphics"),
        )
    }

    private val memorias = LinkedHashMap<String, Memoria>()

    private fun registrar(linha: String) {
        Log.w(TAG, linha)
        relatorio.appendLine(linha)
        // Grava a cada linha: se um passo falhar, o que já foi medido fica no arquivo.
        armazenamento.openOutputFile("desempenho.txt").use { it.write(relatorio.toString().toByteArray()) }
    }

    private fun registrarMemoria(tela: String) {
        // Deixa a tela assentar (imagens decodificadas, animação de entrada) antes de medir.
        avancar(1_500)
        Thread.sleep(500)
        val m = medirMemoria()
        memorias[tela] = m
        registrar("memoria $tela: pss=${m.pssMb}MB java=${m.javaMb}MB nativo=${m.nativoMb}MB graficos=${m.graficosMb}MB")
    }

    @Test
    fun aberturaEMemoriaNasTelasPrincipais() {
        val am = contexto.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        registrar("aparelho: API ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE}), ${Build.MANUFACTURER} ${Build.MODEL}")
        registrar("limites: memoryClass=${am.memoryClass}MB largeMemoryClass=${am.largeMemoryClass}MB maxMemory=${Runtime.getRuntime().maxMemory() / 1048576}MB")

        // 1. Abrir o app até a tela inicial (a primeira atividade do processo).
        val abertura = abrir()
        registrar("abertura (1a, atividade ate a tela inicial composta): ${abertura}ms")
        registrarMemoria("inicio")

        // 2. A casa (por fora).
        val abertura2 = abrir()
        registrar("abertura (2a, de novo no mesmo processo): ${abertura2}ms")
        tocar("Minha casinha")
        esperar("Entrar na casa")
        registrarMemoria("casa")

        // 3. Casa por dentro com a lojinha aberta: as roupas da loja são experimentadas na criança (o provador).
        tocar("Entrar na casa")
        esperar("Lojinha de comidas e roupas")
        avancar(1_000)
        tocar("Lojinha de comidas e roupas")
        esperar("Fechar a lojinha")
        registrarMemoria("casa_por_dentro_lojinha")

        // 4. Uma pergunta do jogo: a fase 7 dos números (a próxima, já liberada).
        abrir()
        tocar("Caminho Números")
        esperar("Fase 7,")
        avancar(1_500)
        tocar("Fase 7,")
        // Se a fase abrir com a história do mundo, o botão grande começa a fase.
        regra.waitUntil(20_000) {
            PartidaNaTela.pergunta != null ||
                regra.onAllNodes(hasTestTag("historia_continuar")).fetchSemanticsNodes().isNotEmpty()
        }
        if (PartidaNaTela.pergunta == null) {
            regra.onAllNodes(hasTestTag("historia_continuar")).onFirst().performClick()
            regra.waitUntil(20_000) { PartidaNaTela.pergunta != null }
        }
        registrarMemoria("jogo")

        val maior = memorias.maxByOrNull { it.value.pssMb }!!
        registrar("resumo: abertura=${abertura}ms pss_maximo=${maior.value.pssMb}MB (${maior.key})")

        // Asserções frouxas: só regressão grosseira.
        assertTrue("o app levou ${abertura}ms para abrir (limite ${LIMITE_ABERTURA_MS}ms)", abertura < LIMITE_ABERTURA_MS)
        for ((tela, m) in memorias) {
            assertTrue("PSS de ${m.pssMb}MB em $tela (limite ${LIMITE_PSS_MB}MB)", m.pssMb < LIMITE_PSS_MB)
        }
    }

    private companion object {
        const val TAG = "DESEMPENHO"
        const val LIMITE_ABERTURA_MS = 10_000L
        const val LIMITE_PSS_MB = 400
    }
}
