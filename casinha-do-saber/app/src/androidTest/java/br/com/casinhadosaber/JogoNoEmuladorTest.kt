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
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.services.storage.TestStorage
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Balanca
import br.com.casinhadosaber.dominio.CacaNaCena
import br.com.casinhadosaber.dominio.CartasViradas
import br.com.casinhadosaber.dominio.DirecaoDaSeta
import br.com.casinhadosaber.dominio.Ditado
import br.com.casinhadosaber.dominio.Domino
import br.com.casinhadosaber.dominio.EscondeEsconde
import br.com.casinhadosaber.dominio.EstourarBaloes
import br.com.casinhadosaber.dominio.LigarPares
import br.com.casinhadosaber.dominio.Mercadinho
import br.com.casinhadosaber.dominio.MontarFrase
import br.com.casinhadosaber.dominio.Pescaria
import br.com.casinhadosaber.dominio.PintarPorCodigo
import br.com.casinhadosaber.dominio.ProgramarRobo
import br.com.casinhadosaber.dominio.QuebraCabeca
import br.com.casinhadosaber.dominio.Relogio
import br.com.casinhadosaber.dominio.SigaASequencia
import br.com.casinhadosaber.dominio.Trem
import br.com.casinhadosaber.ui.telas.PartidaNaTela
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
        PartidaNaTela.limpar()
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
            // Figura que já estava baixinha (cesta com muitas) pode não descer 40 dp: vale estar dentro da cesta.
            if (cesta != null && cesta.inflate(dp(12f)).contains(depois.center) && !cesta.contains(antes.center)) return true
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
        val faltaram = mutableListOf<Int>()
        for (i in 0 until total) {
            if (postas == quantas) break
            if (soltarNaCesta(i)) postas++ else faltaram += i
        }
        // Segunda volta para as que não entraram (a cesta cheia muda de lugar o que já está dentro).
        for (i in faltaram) {
            if (postas == quantas) break
            if (soltarNaCesta(i)) postas++
        }
        if (postas != quantas) capturar("e29_cesta_falhou")
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
        fun silabas() = regra.onAllNodes(hasTestTag("silaba_certa")).fetchSemanticsNodes()
        fun texto(n: androidx.compose.ui.semantics.SemanticsNode) =
            n.config.getOrElse(SemanticsProperties.ContentDescription) { emptyList() }.joinToString("")
        val ordem = ordemDasSilabas(silabas().map(::texto), palavra)
            ?: throw AssertionError("as sílabas ${silabas().map(::texto)} não formam $palavra")
        val textos = ordem.map { texto(silabas()[it]) }
        val figura = regra.onAllNodes(hasContentDescription(palavra)).onFirst().fetchSemanticsNode().boundsInRoot
        val raiz = regra.onRoot().fetchSemanticsNode().boundsInRoot
        for (silaba in textos) {
            // A peça solta (mais embaixo) com esta sílaba; a fila de nós pode mudar de ordem a cada encaixe.
            val alvoY = { h: Float -> figura.bottom + dp(8f) + h / 2f }
            fun encaixadas() = silabas().count { texto(it) == silaba && it.boundsInRoot.top < alvoY(it.boundsInRoot.height) + dp(20f) }
            val jaEstavam = encaixadas()
            var encaixou = false
            for (x in listOf(raiz.center.x, raiz.left + raiz.width * 0.3f, raiz.left + raiz.width * 0.7f)) {
                val no = silabas().withIndex().filter { texto(it.value) == silaba }.maxByOrNull { it.value.boundsInRoot.top }
                    ?: throw AssertionError("sílaba $silaba sumiu")
                val antes = no.value.boundsInRoot
                regra.onAllNodes(hasTestTag("silaba_certa"))[no.index].performTouchInput {
                    swipe(center, Offset(x, alvoY(antes.height)) - antes.topLeft, durationMillis = 600)
                }
                avancar(1_000)
                if (encaixadas() > jaEstavam) { encaixou = true; break }
            }
            assertTrue("não consegui encaixar a sílaba $silaba de $palavra", encaixou)
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


    // ------------------------------------------------------------------ desafios novos (fases 51 a 250)

    private fun no(tag: String): SemanticsNodeInteraction = regra.onAllNodes(hasTestTag(tag)).onFirst()

    private fun tocarNo(tag: String, depois: Long = 500) {
        no(tag).performClick()
        avancar(depois)
    }

    /** Toque pela ação de acessibilidade (o alvo se mexe: peixe, balão, esconderijo). */
    private fun tocarPorSemantica(tag: String, depois: Long = 700) {
        no(tag).performSemanticsAction(SemanticsActions.OnClick)
        avancar(depois)
    }

    /** Arrasta com o dedo o centro da marca [de] até o centro da marca [para]. */
    private fun arrastarAte(de: String, para: String) {
        val destino = no(para).fetchSemanticsNode().boundsInRoot
        val origemNo = no(de)
        val origem = origemNo.fetchSemanticsNode().boundsInRoot
        origemNo.performTouchInput { swipe(center, destino.center - origem.topLeft, durationMillis = 500) }
        avancar(900)
    }

    /** Índices de [valores] (cada um uma vez) que somam [alvo]. */
    private fun somaExata(valores: List<Int>, alvo: Int, de: Int = 0): List<Int>? {
        if (alvo == 0) return emptyList()
        for (i in de until valores.size) {
            if (valores[i] <= alvo) somaExata(valores, alvo - valores[i], i + 1)?.let { return listOf(i) + it }
        }
        return null
    }

    /** Leva a bolinha do ponteiro [tag] até [graus] (a partir do 12), a [fracao] do raio, dando a volta por fora. */
    private fun girarPonteiro(tag: String, graus: Float, fracao: Float) {
        val mostrador = no("relogio_mostrador")
        val caixa = mostrador.fetchSemanticsNode().boundsInRoot
        val bolinha = no(tag).fetchSemanticsNode().boundsInRoot
        val raio = caixa.width / 2f
        val r = graus * PI.toFloat() / 180f
        val alvo = Offset(raio + raio * fracao * sin(r), raio - raio * fracao * cos(r))
        val inicio = bolinha.center - caixa.topLeft
        val meio = Offset(raio, raio) + ((inicio + alvo) / 2f - Offset(raio, raio)).let { v ->
            val d = v.getDistance()
            if (d < raio * 0.3f) Offset(raio * 0.6f, 0f) else v * (raio * 0.6f / d)
        }
        mostrador.performTouchInput {
            down(inicio)
            for (k in 1..10) moveTo(inicio + (meio - inicio) * (k / 10f), delayMillis = 30)
            for (k in 1..10) moveTo(meio + (alvo - meio) * (k / 10f), delayMillis = 30)
            up()
        }
        avancar(900)
    }

    /** Menor caminho do robô (sem pedras nem borda), em setas. */
    private fun caminhoDoRobo(r: ProgramarRobo): List<DirecaoDaSeta> {
        val passos = mapOf(DirecaoDaSeta.CIMA to (-1 to 0), DirecaoDaSeta.BAIXO to (1 to 0), DirecaoDaSeta.ESQUERDA to (0 to -1), DirecaoDaSeta.DIREITA to (0 to 1))
        val veio = HashMap<Int, Pair<Int, DirecaoDaSeta>>()
        val fila = ArrayDeque(listOf(r.inicio))
        val vistos = mutableSetOf(r.inicio)
        while (fila.isNotEmpty()) {
            val c = fila.removeFirst()
            if (c == r.chegada) break
            for ((d, p) in passos) {
                val l = c / r.colunas + p.first
                val k = c % r.colunas + p.second
                if (l !in 0 until r.linhas || k !in 0 until r.colunas) continue
                val n = l * r.colunas + k
                if (n in r.obstaculos || !vistos.add(n)) continue
                veio[n] = c to d
                fila.addLast(n)
            }
        }
        val setas = mutableListOf<DirecaoDaSeta>()
        var c = r.chegada
        while (c != r.inicio) {
            val (antes, d) = veio.getValue(c)
            setas.add(0, d)
            c = antes
        }
        return setas
    }

    private fun habilitado(tag: String) = regra.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes()
        .firstOrNull()?.config?.contains(SemanticsProperties.Disabled) == false

    /** Joga a atividade [a] sem errar, pelas marcas de teste de cada tela. Devolve o nome do tipo. */
    private fun jogarDesafioNovo(a: Atividade): String {
        when (a) {
            is Pescaria -> repeat(a.certos.size) { tocarPorSemantica("peixe_certo", 1_000) }
            is EstourarBaloes -> for (k in a.certos.indices) tocarPorSemantica("balao_certo_$k", 700)
            is CacaNaCena -> repeat(a.certos.size) { tocarPorSemantica("cena_certo", 1_000) }
            is EscondeEsconde -> {
                // Só aceita toque depois de acenar, esconder e trocar de lugar (toque antes é ignorado).
                avancar(5_000L + a.trocas * 1_600L)
                var tentativas = 0
                while (!PartidaNaTela.acertou && tentativas++ < 8) tocarPorSemantica("esconderijo_certo", 1_500)
            }
            is LigarPares -> for ((i, j) in a.pares) arrastarAte("ligar_esquerda_$i", "ligar_direita_$j")
            is QuebraCabeca -> for (i in 0 until a.pecas) if (i !in a.fixas) arrastarAte("quebra_peca_$i", "quebra_lugar_$i")
            is Trem -> for (i in a.vagoes.indices) if (i !in a.fixos) arrastarAte("trem_vagao_$i", "trem_engate_$i")
            is Domino -> for (i in a.ordem) arrastarAte("domino_peca_$i", "domino_alvo")
            is Balanca -> {
                val certos = somaExata(a.pesos, a.alvo) ?: throw AssertionError("balança sem solução: $a")
                for (i in certos) arrastarAte("balanca_peso_$i", "balanca_prato")
            }
            is Mercadinho -> {
                val certos = somaExata(a.dinheiro, a.alvo) ?: throw AssertionError("mercadinho sem solução: $a")
                for (i in certos) arrastarAte("mercado_dinheiro_$i", "mercado_balcao")
            }
            is Relogio -> {
                if (a.minuto != 0) girarPonteiro("relogio_ponteiro_minutos", a.minuto * 6f, 0.78f)
                girarPonteiro("relogio_ponteiro_horas", ((a.hora % 12) + a.minuto / 60f) * 30f, 0.5f)
            }
            is PintarPorCodigo -> {
                var pote: String? = null
                for ((i, c) in a.casas.withIndex()) {
                    val codigo = c.codigo ?: continue
                    if (codigo != pote) {
                        tocarNo("pintar_pote_$codigo", 150)
                        pote = codigo
                    }
                    tocarNo("pintar_casa_$i", 80)
                }
            }
            is SigaASequencia -> {
                // Espera a sequência terminar de ser mostrada (o 👀 só fica ativo na vez da criança).
                regra.waitUntil(40_000) { habilitado("seq_repetir") }
                for (i in a.sequencia) tocarNo("seq_botao_$i", 450)
            }
            is Ditado -> for (k in a.partes.indices) tocarNo("ditado_certa_$k", 700)
            is CartasViradas -> {
                avancar(5_000)
                for (k in a.ordem.indices) tocarNo("virada_ordem_$k", 1_300)
            }
            is ProgramarRobo -> {
                avancar(1_000)
                for (d in caminhoDoRobo(a)) tocarNo("robo_seta_${d.name}", 400)
                tocarNo("robo_play", 1_000)
            }
            is MontarFrase -> for (k in a.palavras.indices) arrastarAte("frase_certa_$k", "frase_lugar_$k")
            else -> return responderSemErrar()
        }
        return a::class.simpleName ?: "?"
    }

    /** Uma fase alta já liberada: todas as anteriores do caminho vencidas com 3 estrelas. */
    private fun abrirFase(chaveTrilha: String, caminho: String, numero: Int) {
        // Fecha a partida anterior antes de mexer no progresso: ao pausar, o app grava o estado dele por cima.
        cenario?.close()
        cenario = null
        prefs.edit().clear()
            .putString("genero", "MENINO").putString("nome", "Leo")
            .putString("estrelas", (0 until numero - 1).joinToString(";") { "$chaveTrilha:$it=3" })
            .putString("colocados", "terreno,0.5,0.5;paredes,0.5,0.5;telhado,0.5,0.5;porta,0.5,0.5;cama,0.27,0.528;janelas,0.5,0.5")
            .commit()
        PartidaNaTela.limpar()
        cenario = ActivityScenario.launch(MainActivity::class.java)
        esperar("Caminho $caminho", 15_000)
        regra.onAllNodes(hasContentDescription("Caminho $caminho", substring = true)).onFirst().performClick()
        try {
            esperar("Fase $numero,", 15_000)
        } catch (e: Throwable) {
            capturar("e29_sem_fase_${chaveTrilha.lowercase()}_$numero")
            throw AssertionError("a fase $numero de $caminho não apareceu no caminho", e)
        }
        avancar(1_500)
        val fase = regra.onAllNodes(hasContentDescription("Fase $numero,", substring = true)).onFirst()
        runCatching { fase.performSemanticsAction(SemanticsActions.OnClick) }.onFailure { fase.performClick() }
    }

    /**
     * Joga as 5 perguntas da fase sem errar, lendo de [PartidaNaTela] a atividade sorteada; cada pergunta
     * só termina quando a tela do jogo marca o acerto. Devolve os tipos jogados.
     */
    private fun jogarFaseAlta(chaveTrilha: String, caminho: String, numero: Int, captura: String): List<String> {
        abrirFase(chaveTrilha, caminho, numero)
        val tipos = mutableListOf<String>()
        for (i in 0 until 5) {
            regra.waitUntil(20_000) { PartidaNaTela.indice == i && !PartidaNaTela.acertou && PartidaNaTela.pergunta != null }
            // Passa a troca de pergunta (as duas telas convivem durante a animação).
            avancar(1_200)
            val pergunta = PartidaNaTela.pergunta!!
            if (i == 0) capturar("${captura}_inicio")
            val tipo = jogarDesafioNovo(pergunta.atividade)
            tipos += tipo
            try {
                regra.waitUntil(45_000) { PartidaNaTela.indice == i && PartidaNaTela.acertou }
            } catch (e: Throwable) {
                capturar("${captura}_falhou_$i")
                throw AssertionError("fase $numero de $caminho: a pergunta ${i + 1} ($tipo, \"${pergunta.instrucao}\") não terminou", e)
            }
        }
        regra.waitUntil(20_000) {
            regra.onAllNodes(hasContentDescription("Presente. Toque para abrir.")).fetchSemanticsNodes().isNotEmpty() ||
                prefs.getString("estrelas", "").orEmpty().contains("$chaveTrilha:${numero - 1}=")
        }
        capturar("${captura}_fim")
        val estrelas = prefs.getString("estrelas", "").orEmpty().split(";").firstOrNull { it.startsWith("$chaveTrilha:${numero - 1}=") }
        assertEquals("fase $numero de $caminho jogada sem erro deveria valer 3 estrelas ($tipos)", "$chaveTrilha:${numero - 1}=3", estrelas)
        return tipos
    }

    private val relatorioDasFases = StringBuilder()

    private fun registrar(arquivo: String, caminho: String, numero: Int, tipos: List<String>) {
        relatorioDasFases.appendLine("$caminho fase $numero: ${tipos.joinToString(", ")}")
        armazenamento.openOutputFile("$arquivo.txt").use { it.write(relatorioDasFases.toString().toByteArray()) }
    }

    /** Fase 100 (chefão do Circo) de cada caminho: sorteia os desafios novos num Android de verdade. */
    @Test
    fun jogarFase100DeCadaCaminho() {
        val novos = mutableSetOf<String>()
        for ((chave, caminho, captura) in listOf(
            Triple("MATEMATICA", "Números", "e20_numeros_100"),
            Triple("PORTUGUES", "Letras", "e21_letras_100"),
            Triple("INGLES", "Inglês", "e22_ingles_100"),
            Triple("LOGICA", "Lógica", "e23_logica_100"),
        )) {
            val tipos = jogarFaseAlta(chave, caminho, 100, captura)
            registrar("fases_100", caminho, 100, tipos)
            novos += tipos.filter { it !in TIPOS_ANTIGOS }
        }
        assertTrue("as fases 100 deveriam sortear desafios novos: $novos", novos.size >= 3)
    }

    /** Fases onde os outros desafios novos mais aparecem (relógio, robô, mercadinho, esconde esconde...). */
    @Test
    fun jogarFasesAltasComOsOutrosDesafios() {
        val novos = mutableSetOf<String>()
        for ((chave, caminho, numero) in listOf(
            Triple("MATEMATICA", "Números", 54),
            Triple("MATEMATICA", "Números", 132),
            Triple("MATEMATICA", "Números", 197),
            Triple("MATEMATICA", "Números", 222),
            Triple("LOGICA", "Lógica", 54),
            Triple("LOGICA", "Lógica", 114),
            Triple("LOGICA", "Lógica", 204),
            Triple("LOGICA", "Lógica", 213),
            Triple("INGLES", "Inglês", 127),
        )) {
            val tipos = jogarFaseAlta(chave, caminho, numero, "e24_${chave.lowercase()}_$numero")
            registrar("fases_altas", caminho, numero, tipos)
            novos += tipos.filter { it !in TIPOS_ANTIGOS }
        }
        assertTrue("as fases altas deveriam sortear vários desafios novos: $novos", novos.size >= 6)
    }

    private companion object {
        val TIPOS_ANTIGOS = setOf("escolha", "cesta", "memoria", "montar", "tracar")
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

        // Primeira fase do mundo: a Corujinha Professora conta a história; o botão grande começa a fase.
        regra.waitUntilAtLeastOneExists(hasTestTag("historia_continuar"), 10_000)
        avancar(1_000)
        capturar("e03b_historia_do_mundo")
        regra.onAllNodes(hasTestTag("historia_continuar")).onFirst().performClick()

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

        // Primeira vez na casa: escolher o mascote (aparece uma vez só, enquanto não há mascote).
        regra.waitUntilAtLeastOneExists(hasTestTag("mascote_GATINHO"), 10_000)
        regra.onAllNodes(hasTestTag("mascote_GATINHO")).onFirst().performClick()
        avancar(800)
        capturar("e11a_escolha_do_mascote")
        regra.onAllNodes(hasTestTag("mascote_pronto")).onFirst().performClick()
        regra.waitUntil(5_000) { prefs.getString("mascote", "").orEmpty().startsWith("GATINHO") }
        regra.waitUntil(5_000) { regra.onAllNodes(hasTestTag("mascote_pronto")).fetchSemanticsNodes().isEmpty() }
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
