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
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.services.storage.TestStorage
import br.com.casinhadosaber.ui.telas.atividades.TracosDasLetras
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

    /** Avança o relógio do Compose (as esperas do jogo usam esse relógio, não o tempo real). */
    private fun avancar(ms: Long) {
        regra.mainClock.advanceTimeBy(ms)
        regra.waitForIdle()
    }

    private fun esperar(descricao: String, timeout: Long = 10_000) =
        regra.waitUntilAtLeastOneExists(hasContentDescription(descricao, substring = true), timeout)

    // ------------------------------------------------------------------ responder qualquer pergunta

    private fun dp(v: Float) = with(regra.density) { v.dp.toPx() }

    private fun existe(tag: String) = regra.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    /** Marcas que aparecem em cada tipo de pergunta: tocar, cesta, memória, montar a palavra e traçar. */
    private val qualquerPergunta = hasTestTag("opcao_certa") or hasTestTag("figura_certa") or
        hasTestTag("carta_0") or hasTestTag("silaba_certa") or hasTestTag("area_tracar")

    /** Texto do balão com a instrução da pergunta (o balão começa com 🔊). */
    private fun textoDaInstrucao(): String =
        regra.onAllNodes(hasText("🔊", substring = true)).fetchSemanticsNodes()
            .flatMap { it.config.getOrElse(SemanticsProperties.Text) { emptyList() } }
            .joinToString(" ") { it.text }

    /**
     * Responde a pergunta da vez sem errar, seja de tocar ou de mexer (as fases sorteiam os dois tipos).
     * Devolve o tipo da pergunta, para conferir depois.
     */
    private fun responderSemErrar(): String {
        regra.waitUntilAtLeastOneExists(qualquerPergunta, 10_000)
        return when {
            existe("opcao_certa") -> {
                regra.onAllNodes(hasTestTag("opcao_certa")).onFirst().performClick()
                "escolha"
            }
            existe("figura_certa") -> { fazerCesta(); "cesta" }
            existe("carta_0") -> { fazerMemoria(); "memoria" }
            existe("silaba_certa") -> { fazerMontarPalavra(); "montar" }
            else -> { fazerTracar(); "tracar" }
        }
    }

    /**
     * Arrasta a figura [i] para dentro da cesta. A cesta fica embaixo; tenta alguns pontos (soltar
     * fora da cesta não conta como erro, a figura só volta) até a figura ficar lá dentro.
     */
    private fun soltarNaCesta(i: Int): Boolean {
        val raiz = regra.onRoot().fetchSemanticsNode().boundsInRoot
        // A cesta tem uma marca invisível do tamanho dela ("cesta"); o certinho só aparece com fruta lá dentro.
        val cesta = regra.onAllNodes(hasTestTag("cesta")).fetchSemanticsNodes().firstOrNull()?.boundsInRoot
        val alvos = buildList {
            if (cesta != null) {
                add(cesta.center)
                add(Offset(cesta.center.x, cesta.top + cesta.height * 0.3f))
            }
            for (d in listOf(100f, 140f, 70f, 180f)) add(Offset(raiz.center.x, raiz.bottom - dp(d)))
        }
        for (alvo in alvos) {
            val figura = regra.onAllNodes(hasTestTag("figura_certa"))[i]
            val antes = figura.fetchSemanticsNode().boundsInRoot
            figura.performTouchInput { swipe(center, alvo - antes.topLeft, durationMillis = 500) }
            avancar(900)
            val depois = regra.onAllNodes(hasTestTag("figura_certa"))[i].fetchSemanticsNode().boundsInRoot
            if (depois.center.y > antes.center.y + dp(40f)) return true
        }
        return false
    }

    /** Cesta de contar ("Coloque N na cesta" e tocar no certinho) ou de classificar (todas as do grupo). */
    private fun fazerCesta() {
        avancar(500)
        val alvo = Regex("""Coloque (\d+) na cesta""").find(textoDaInstrucao())?.groupValues?.get(1)?.toInt()
        val total = regra.onAllNodes(hasTestTag("figura_certa")).fetchSemanticsNodes().size
        val quantas = alvo ?: total
        var postas = 0
        for (i in 0 until total) {
            if (postas == quantas) break
            if (soltarNaCesta(i)) postas++
        }
        assertEquals("não consegui pôr as figuras na cesta", quantas, postas)
        if (alvo != null) regra.onAllNodes(hasTestTag("atividade_pronto")).onFirst().performClick()
    }

    /** Memória: espera a espiadinha e vira cada par (as duas cartas de um par têm a marca carta_N). */
    private fun fazerMemoria() {
        avancar(3_000)
        var par = 0
        while (regra.onAllNodes(hasTestTag("carta_$par")).fetchSemanticsNodes().size == 2) {
            val cartas = regra.onAllNodes(hasTestTag("carta_$par"))
            cartas[0].performClick()
            avancar(300)
            cartas[1].performClick()
            avancar(600)
            par++
        }
        assertTrue("nenhum par de cartas encontrado", par > 0)
    }

    /** Ordem das sílabas ([textos]) que forma a [palavra]; null se não houver. */
    private fun ordemDasSilabas(textos: List<String>, palavra: String, usados: List<Int> = emptyList()): List<Int>? {
        val feito = usados.joinToString("") { textos[it] }
        if (feito == palavra) return usados
        if (!palavra.startsWith(feito)) return null
        for (i in textos.indices) {
            if (i !in usados) ordemDasSilabas(textos, palavra, usados + i)?.let { return it }
        }
        return null
    }

    /**
     * Montar a palavra: a instrução diz a palavra ("Monte: OVO") e cada sílaba diz o seu som.
     * Os encaixes ficam logo abaixo da figura; soltar em qualquer ponto deles põe a sílaba no próximo.
     */
    private fun fazerMontarPalavra() {
        avancar(500)
        val palavra = Regex("""Monte: (\S+)""").find(textoDaInstrucao())?.groupValues?.get(1)?.lowercase()
            ?: throw AssertionError("instrução de montar sem a palavra: ${textoDaInstrucao()}")
        val textos = regra.onAllNodes(hasTestTag("silaba_certa")).fetchSemanticsNodes()
            .map { it.config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }.joinToString("") }
        val ordem = ordemDasSilabas(textos, palavra)
            ?: throw AssertionError("as sílabas $textos não formam $palavra")
        val figura = regra.onAllNodes(hasContentDescription(palavra)).onFirst().fetchSemanticsNode().boundsInRoot
        for (i in ordem) {
            val silaba = regra.onAllNodes(hasTestTag("silaba_certa"))[i]
            val antes = silaba.fetchSemanticsNode().boundsInRoot
            val alvo = Offset(figura.center.x, figura.bottom + dp(8f) + antes.height / 2f)
            silaba.performTouchInput { swipe(center, alvo - antes.topLeft, durationMillis = 500) }
            avancar(900)
        }
    }

    /**
     * Traçar: a instrução diz a letra ("Trace a letra A"); passa o dedo por cada traço da letra, na
     * ordem e no sentido certos, como a criança (os traços vêm de TracosDasLetras, no mesmo quadro).
     */
    private fun fazerTracar() {
        avancar(500)
        val letra = Regex("""Trace a letra (\S+)""").find(textoDaInstrucao())?.groupValues?.get(1)
            ?: throw AssertionError("instrução de traçar sem a letra: ${textoDaInstrucao()}")
        for (traco in TracosDasLetras.tracosDe(letra)) {
            if (!existe("area_tracar")) break
            regra.onAllNodes(hasTestTag("area_tracar")).onFirst().performTouchInput {
                val pontos = TracosDasLetras.pontosDoTraco(traco, 0.02f).map { TracosDasLetras.paraTela(it, width.toFloat(), height.toFloat()) }
                down(pontos.first())
                for (p in pontos.drop(1)) moveTo(p)
                up()
            }
            avancar(300)
        }
        avancar(900)
    }

    @Test
    fun jogarUmaFaseEColocarOPremioNaCasa() {
        cenario = ActivityScenario.launch(MainActivity::class.java)

        // Escolher a menina
        regra.waitUntilAtLeastOneExists(hasText("Menina"), 15_000)
        regra.onAllNodes(hasText("Menina")).onFirst().performClick()
        capturar("e01_perfil")
        regra.onAllNodes(hasContentDescription("Pronto")).onFirst().performClick()

        // Boas-vindas: a criança chega ao terreno; um toque pula a cena.
        regra.waitUntilAtLeastOneExists(hasText("Vamos construir a sua casinha!"), 10_000)
        avancar(1_500)
        capturar("e01b_boas_vindas")
        regra.onAllNodes(hasText("Vamos construir a sua casinha!")).onFirst().performClick()

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
        avancar(800)
        capturar("e05_erro")
        repeat(5) { i ->
            responderSemErrar()
            if (i == 0) {
                avancar(400)
                capturar("e06_acerto")
            }
            avancar(5_500)
        }

        // Prêmio: abrir o presente e levar para a casa
        esperar("Presente. Toque para abrir.", 15_000)
        capturar("e07_presente")
        // O presente abre com três toques
        repeat(3) {
            regra.onAllNodes(hasContentDescription("Presente. Toque para abrir.")).onFirst().performClick()
            avancar(400)
        }
        esperar("Colocar na casa")
        avancar(1_000)
        capturar("e08_premio_aberto")
        regra.onAllNodes(hasContentDescription("Colocar na casa")).onFirst().performClick()

        // Casa: o terreno ganho entra sozinho
        regra.waitUntilAtLeastOneExists(hasText("Minha casinha"), 10_000)
        avancar(2_500)
        capturar("e10_casa_com_terreno")
        regra.waitUntil(5_000) { prefs.getString("colocados", "").orEmpty().contains("terreno") }
        assertTrue("fase vencida não foi salva", prefs.getString("estrelas", "").orEmpty().startsWith("MATEMATICA:0="))
        // Errou uma vez: duas estrelas
        assertEquals("MATEMATICA:0=2", prefs.getString("estrelas", ""))
    }

    /**
     * "Cesta das coisas" (8ª fase da lógica) sorteia só cesta de classificar; a pergunta do meio é
     * revisão de uma fase anterior (tocar ou memória). Joga a fase inteira sem errar, com o dedo.
     */
    @Test
    fun jogarFaseComAtividadesDeMexer() {
        prefs.edit()
            .putString("genero", "MENINA").putString("nome", "Ana")
            .putString("estrelas", (0 until 7).joinToString(";") { "LOGICA:$it=3" })
            .putString("colocados", "terreno,0.5,0.5;paredes,0.5,0.5;telhado,0.5,0.5;porta,0.5,0.5;cama,0.27,0.528;janelas,0.5,0.5")
            .commit()
        cenario = ActivityScenario.launch(MainActivity::class.java)

        esperar("Caminho Lógica")
        regra.onAllNodes(hasContentDescription("Caminho Lógica", substring = true)).onFirst().performClick()
        esperar("Fase 8, Cesta das coisas")
        regra.onAllNodes(hasContentDescription("Fase 8, Cesta das coisas", substring = true)).onFirst().performClick()

        regra.waitUntilAtLeastOneExists(hasTestTag("figura_certa"), 10_000)
        avancar(1_000)
        capturar("e14_cesta_classificar")
        val tipos = mutableListOf<String>()
        repeat(5) { i ->
            tipos += responderSemErrar()
            if (i == 0) {
                avancar(300)
                capturar("e15_cesta_completa")
            }
            avancar(6_500)
        }
        assertTrue("a fase deveria sortear cestas: $tipos", tipos.count { it == "cesta" } >= 4)

        esperar("Presente. Toque para abrir.", 15_000)
        capturar("e16_premio_das_atividades")
        regra.waitUntil(5_000) { prefs.getString("estrelas", "").orEmpty().contains("LOGICA:7=") }
        // Tudo certo de primeira: três estrelas
        assertTrue(
            "a fase de mexer deveria valer 3 estrelas: ${prefs.getString("estrelas", "")}",
            prefs.getString("estrelas", "").orEmpty().split(";").contains("LOGICA:7=3"),
        )
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

        // Arrasta a cama para o andar de baixo (para baixo e para a direita, ainda dentro de casa)
        regra.onAllNodes(hasContentDescription("Cama")).onFirst().performTouchInput {
            swipe(center, center + Offset(width * 2.5f, height * 1.8f), durationMillis = 600)
        }
        regra.waitUntil(5_000) { prefs.getString("colocados", "") != antes }
        avancar(600)
        capturar("e12_casa_depois_de_arrastar")
        val cama = prefs.getString("colocados", "").orEmpty().split(";").first { it.startsWith("cama,") }.split(",")
        assertNotEquals("a cama não saiu do lugar", "0.528", cama[2])
        assertTrue("a cama deveria descer", cama[2].toFloat() > 0.6f)
        assertEquals("a cama deveria pousar no piso de baixo", 0.775f, cama[2].toFloat(), 0.001f)

        // Levar a cama para o jardim não deixa: ela volta para dentro
        val dentro = prefs.getString("colocados", "")
        regra.onAllNodes(hasContentDescription("Cama")).onFirst().performTouchInput {
            swipe(center, center + Offset(0f, height * 1.2f), durationMillis = 500)
        }
        avancar(1_500)
        capturar("e13_cama_nao_vai_para_o_jardim")
        assertEquals("a cama não pode ir para o jardim", dentro, prefs.getString("colocados", ""))
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
        // Qual voz o narrador do app escolheu (a de melhor qualidade instalada).
        lateinit var narrador: br.com.casinhadosaber.audio.Narrador
        InstrumentationRegistry.getInstrumentation().runOnMainSync { narrador = br.com.casinhadosaber.audio.Narrador(contexto) }
        Thread.sleep(4_000)
        relatorio.appendLine("Narrador do app: ${narrador.descricaoDasVozes()}")
        relatorio.appendLine("Vozes de boa qualidade: ${narrador.vozesBoas}")
        InstrumentationRegistry.getInstrumentation().runOnMainSync { narrador.desligar() }
        armazenamento.openOutputFile("vozes.txt").use { it.write(relatorio.toString().toByteArray()) }
        // Não falha o teste: o relatório só registra o que o aparelho tem.
    }
}
