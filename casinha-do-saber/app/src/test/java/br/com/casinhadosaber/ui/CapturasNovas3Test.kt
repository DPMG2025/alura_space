package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Balanca
import br.com.casinhadosaber.dominio.CasaDePintar
import br.com.casinhadosaber.dominio.CorDaLegenda
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Mercadinho
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.PintarPorCodigo
import br.com.casinhadosaber.dominio.Relogio
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.atividades.novas.escolhaQueSomaN3
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Capturas (prefixo n3_) e testes de concluir das telas de Balança, Mercadinho, Relógio e Pintar por
 * código. Cada teste joga a atividade inteira pelas marcas de teste (testTag) e confere que a pergunta
 * termina como acerto de primeira, ou que o erro não termina nada.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasNovas3Test {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val menino = Perfil("Leo", Genero.MENINO, pele = 0, cabelo = 3)
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

    private fun foto(nome: String) = regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")

    private fun deMexer(instrucao: String, atividade: Atividade) =
        Pergunta.deAtividade(instrucao, listOf(pt(instrucao)), atividade, listOf(pt("Muito bem!")))

    /** Joga uma pergunta só, numa fase de [trilha]; devolve onde o resultado da fase vai aparecer. */
    private fun jogar(trilha: Trilha, pergunta: Pergunta, perfil: Perfil = menina): () -> List<Boolean>? {
        var resultado: List<Boolean>? = null
        montar { TelaJogo(gerador.fases(trilha)[2], perfil, {}, { resultado = it }, perguntasProntas = listOf(pergunta)) }
        regra.mainClock.advanceTimeBy(2000)
        return { resultado }
    }

    /** Arrasta o nó [de] até o centro do nó [para]. */
    private fun arrastar(de: String, para: String) {
        val destino = regra.onNodeWithTag(para).fetchSemanticsNode().boundsInRoot
        val no = regra.onNodeWithTag(de)
        val origem = no.fetchSemanticsNode().boundsInRoot
        no.performTouchInput { swipe(center, destino.center - origem.topLeft, durationMillis = 400) }
        regra.mainClock.advanceTimeBy(900)
    }

    // ------------------------------------------------------------------ balança

    private val balanca = Balanca(ladoEsquerdo = listOf(4, 3), pesos = listOf(2, 5, 1, 4, 6))
    private val balancaFiguras = Balanca(ladoEsquerdo = listOf(3), pesos = listOf(1, 2, 4), figura = "🍎", mostrarNumeros = false)

    @Test fun balanca() {
        jogar(Trilha.MATEMATICA, deMexer("Deixe a balança reta", balanca))
        regra.mainClock.advanceTimeBy(1500)
        foto("n3_01_balanca")
    }

    @Test fun balancaConclui() {
        val resultado = jogar(Trilha.MATEMATICA, deMexer("Deixe a balança reta", balanca))
        val certos = escolhaQueSomaN3(balanca.pesos, balanca.alvo)!!
        for (i in certos) arrastar("balanca_peso_$i", "balanca_prato")
        regra.mainClock.advanceTimeBy(800)
        foto("n3_02_balanca_equilibrada")
        regra.mainClock.advanceTimeBy(7000)
        assertEquals("a balança equilibrada sem erro deveria concluir de primeira", listOf(true), resultado())
    }

    /** Passar do peso conta erro: a balança pende para a direita e nada termina; tirando o peso, dá para acertar. */
    @Test fun balancaPassaETira() {
        val resultado = jogar(Trilha.MATEMATICA, deMexer("Deixe a balança reta", balanca))
        arrastar("balanca_peso_1", "balanca_prato") // 5
        arrastar("balanca_peso_4", "balanca_prato") // 5 + 6 = 11, passou
        regra.mainClock.advanceTimeBy(1500)
        foto("n3_03_balanca_passou")
        assertNull(resultado())
        // Tira o 6 tocando nele e põe o 2: 5 + 2 = 7.
        regra.onNodeWithTag("balanca_peso_4").performClick()
        regra.mainClock.advanceTimeBy(900)
        arrastar("balanca_peso_0", "balanca_prato")
        regra.mainClock.advanceTimeBy(8000)
        assertEquals("com um erro não é de primeira", listOf(false), resultado())
    }

    @Test fun balancaFiguras() {
        jogar(Trilha.LOGICA, deMexer("Ponha maçãs até a balança ficar reta", balancaFiguras), menino)
        regra.mainClock.advanceTimeBy(5000)
        foto("n3_04_balanca_figuras_mao")
    }

    // ------------------------------------------------------------------ mercadinho

    private val mercadinho = Mercadinho("🍦", "sorvete", 350, listOf(200, 100, 50, 25, 25, 500, 10, 1000))
    private val mercadinhoTroco = Mercadinho("🍪", "biscoito", 350, listOf(100, 50, 100, 25, 200, 5), troco = true, pago = 500)

    @Test fun mercadinho() {
        jogar(Trilha.MATEMATICA, deMexer("Pague o sorvete", mercadinho))
        regra.mainClock.advanceTimeBy(1500)
        foto("n3_05_mercadinho")
    }

    @Test fun mercadinhoConclui() {
        val resultado = jogar(Trilha.MATEMATICA, deMexer("Pague o sorvete", mercadinho))
        val certos = escolhaQueSomaN3(mercadinho.dinheiro, mercadinho.alvo)!!
        for (i in certos) arrastar("mercado_dinheiro_$i", "mercado_balcao")
        regra.mainClock.advanceTimeBy(500)
        foto("n3_06_mercadinho_pago")
        regra.mainClock.advanceTimeBy(7000)
        assertEquals("pagar o valor exato sem erro deveria concluir de primeira", listOf(true), resultado())
    }

    /** Passar do preço duas vezes: a cédula volta para a carteira e a ajuda acende (moedas certas brilham). */
    @Test fun mercadinhoAjuda() {
        val resultado = jogar(Trilha.MATEMATICA, deMexer("Pague o sorvete", mercadinho))
        arrastar("mercado_dinheiro_5", "mercado_balcao") // 5 reais, passou
        arrastar("mercado_dinheiro_7", "mercado_balcao") // 10 reais, passou
        arrastar("mercado_dinheiro_0", "mercado_balcao") // 2 reais, fica
        regra.mainClock.advanceTimeBy(2500)
        foto("n3_07_mercadinho_ajuda")
        assertNull(resultado())
    }

    @Test fun mercadinhoTroco() {
        val resultado = jogar(Trilha.MATEMATICA, deMexer("Dê o troco", mercadinhoTroco), menino)
        regra.mainClock.advanceTimeBy(1500)
        foto("n3_08_mercadinho_troco")
        val certos = escolhaQueSomaN3(mercadinhoTroco.dinheiro, mercadinhoTroco.alvo)!!
        for (i in certos) arrastar("mercado_dinheiro_$i", "mercado_balcao")
        regra.mainClock.advanceTimeBy(7000)
        assertEquals(listOf(true), resultado())
    }

    // ------------------------------------------------------------------ relógio

    /** Leva a bolinha do ponteiro [tag] até o ângulo [graus] (a partir do 12), a [fracao] do raio. */
    private fun girar(tag: String, graus: Float, fracao: Float) {
        val mostrador = regra.onNodeWithTag("relogio_mostrador")
        val caixa = mostrador.fetchSemanticsNode().boundsInRoot
        val bolinha = regra.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
        val raio = caixa.width / 2f
        val r = graus * PI.toFloat() / 180f
        val alvo = Offset(raio + raio * fracao * sin(r), raio - raio * fracao * cos(r))
        val inicio = bolinha.center - caixa.topLeft
        // Dá a volta por fora (em dois trechos) para não passar pelo meio do relógio.
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
        regra.mainClock.advanceTimeBy(900)
    }

    private val relogioMeia = Relogio(3, 30, digital = true)
    private val relogioCheio = Relogio(8, 0)

    @Test fun relogio() {
        jogar(Trilha.MATEMATICA, deMexer("Acerte o relógio: três e meia", relogioMeia))
        regra.mainClock.advanceTimeBy(1500)
        foto("n3_09_relogio_digital")
    }

    @Test fun relogioConclui() {
        val resultado = jogar(Trilha.MATEMATICA, deMexer("Acerte o relógio: três e meia", relogioMeia))
        girar("relogio_ponteiro_minutos", 180f, 0.78f)
        girar("relogio_ponteiro_horas", 105f, 0.5f)
        regra.mainClock.advanceTimeBy(700)
        foto("n3_10_relogio_cuco")
        regra.mainClock.advanceTimeBy(7000)
        assertEquals("acertar os dois ponteiros sem erro deveria concluir de primeira", listOf(true), resultado())
    }

    /** Dois ponteiros no lugar errado: os ponteiros-fantasma mostram a hora pedida. */
    @Test fun relogioAjuda() {
        val resultado = jogar(Trilha.MATEMATICA, deMexer("Acerte o relógio: oito horas", relogioCheio), menino)
        girar("relogio_ponteiro_horas", 150f, 0.5f) // 5 horas: erro
        girar("relogio_ponteiro_minutos", 90f, 0.78f) // 15 minutos: erro
        regra.mainClock.advanceTimeBy(4000)
        foto("n3_11_relogio_ajuda")
        assertNull(resultado())
    }

    // ------------------------------------------------------------------ pintar por código

    /** Coração vermelho (código 2, às vezes escrito 1+1) com brilho rosa (3), num céu azul (1). */
    private val coracao = run {
        val mapa = listOf(
            "221122",
            "232222",
            "222222",
            "122221",
            "112211",
            "111111",
        )
        val casas = mapa.flatMapIndexed { l, linha ->
            linha.mapIndexed { k, ch ->
                val codigo = ch.toString()
                val rotulo = if (codigo == "2" && (l + k) % 3 == 0) "1+1" else codigo
                CasaDePintar(rotulo, codigo)
            }
        }
        PintarPorCodigo(
            6,
            casas,
            listOf(
                CorDaLegenda("1", 0xFF9AD7FF),
                CorDaLegenda("2", 0xFFEF476F),
                CorDaLegenda("3", 0xFFFFC2D4),
            ),
        )
    }

    /** Vogais de amarelo, consoantes de verde; as casinhas vazias ficam brancas. */
    private val letras = PintarPorCodigo(
        4,
        listOf("A", "B", "", "E", "C", "I", "D", "", "", "O", "F", "U", "G", "", "H", "A").map {
            CasaDePintar(it, if (it.isEmpty()) null else if (it in "AEIOU") "vogal" else "outra")
        },
        listOf(
            CorDaLegenda("vogal", 0xFFFFD23F, listOf(pt("vogais"))),
            CorDaLegenda("outra", 0xFF59C173, listOf(pt("consoantes"))),
        ),
    )

    private fun pintarTudo(p: PintarPorCodigo, ate: Int = p.casas.size) {
        var pote: String? = null
        for ((i, c) in p.casas.withIndex().take(ate)) {
            val codigo = c.codigo ?: continue
            if (codigo != pote) {
                regra.onNodeWithTag("pintar_pote_$codigo").performClick()
                regra.mainClock.advanceTimeBy(100)
                pote = codigo
            }
            regra.onNodeWithTag("pintar_casa_$i").performClick()
            regra.mainClock.advanceTimeBy(60)
        }
    }

    @Test fun pintar() {
        jogar(Trilha.LOGICA, deMexer("Pinte pelo número", coracao))
        regra.mainClock.advanceTimeBy(1500)
        foto("n3_12_pintar")
    }

    @Test fun pintarMeio() {
        jogar(Trilha.LOGICA, deMexer("Pinte pelo número", coracao))
        pintarTudo(coracao, 24)
        regra.mainClock.advanceTimeBy(800)
        foto("n3_13_pintar_meio")
    }

    @Test fun pintarConclui() {
        val resultado = jogar(Trilha.LOGICA, deMexer("Pinte pelo número", coracao))
        pintarTudo(coracao)
        regra.mainClock.advanceTimeBy(900)
        foto("n3_14_pintar_pronto")
        regra.mainClock.advanceTimeBy(7000)
        assertEquals("pintar tudo certo deveria concluir de primeira", listOf(true), resultado())
    }

    /** Duas casinhas com a tinta errada: a tinta escorre, e a ajuda mostra as casinhas da tinta escolhida. */
    @Test fun pintarErroEAjuda() {
        val resultado = jogar(Trilha.PORTUGUES, deMexer("Vogais de amarelo, consoantes de verde", letras), menino)
        regra.onNodeWithTag("pintar_pote_vogal").performClick()
        regra.mainClock.advanceTimeBy(100)
        regra.onNodeWithTag("pintar_casa_0").performClick() // A: certo
        regra.mainClock.advanceTimeBy(100)
        regra.onNodeWithTag("pintar_casa_1").performClick() // B com amarelo: erro
        regra.mainClock.advanceTimeBy(300)
        regra.onNodeWithTag("pintar_casa_4").performClick() // C com amarelo: erro
        regra.mainClock.advanceTimeBy(350)
        foto("n3_15_pintar_escorrendo")
        regra.mainClock.advanceTimeBy(3500)
        foto("n3_16_pintar_ajuda")
        assertNull(resultado())
    }
}
