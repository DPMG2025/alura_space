package br.com.fipeplaca

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.services.storage.TestStorage

/**
 * Testes de ponta a ponta no emulador, com a internet real: o app e usado como um usuario usaria.
 * Grava capturas e relatorios pelo TestStorage (o CI copia para docs/emulador).
 */
@RunWith(AndroidJUnit4::class)
class FluxosNoAparelhoTest {
    @get:Rule val regra = createAndroidComposeRule<MainActivity>()

    /** Grava a pilha completa de qualquer falha no relatorio (o log do CI corta a mensagem). */
    @get:Rule val registroDeFalhas = object : org.junit.rules.TestWatcher() {
        override fun failed(e: Throwable, d: org.junit.runner.Description) {
            runCatching {
                saida.openOutputFile("falha-${d.methodName}.txt", true).use {
                    it.write((e.stackTraceToString() + "\n" + (e.cause?.stackTraceToString() ?: "")).toByteArray())
                }
            }
        }
    }

    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as FipeApp
    /** Saida oficial dos testes: o Gradle copia para build/outputs/connected_android_test_additional_output. */
    private val saida = TestStorage()
    private var servidor: MockWebServer? = null

    @After fun parar() { servidor?.shutdown() }

    private fun relatorio(texto: String) {
        android.util.Log.i("FluxosNoAparelho", texto)
        saida.openOutputFile("relatorio-${nomeTeste()}.txt", true).use { it.write((texto + "\n").toByteArray()) }
    }

    private fun nomeTeste() = Thread.currentThread().stackTrace.firstOrNull { it.methodName.startsWith("busca") }?.methodName ?: "geral"

    private fun captura(nome: String) {
        regra.waitForIdle()
        val bmp = regra.onRoot().captureToImage().asAndroidBitmap()
        saida.openOutputFile("$nome.png").use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun esperar(descricao: String, ms: Long = 30_000, condicao: () -> Boolean) {
        try {
            regra.waitUntil(ms) { runCatching(condicao).getOrDefault(false) }
        } catch (e: Throwable) {
            captura("erro_${descricao.replace(' ', '_')}")
            relatorio("FALHOU esperando: $descricao")
            throw AssertionError("Tempo esgotado esperando: $descricao", e)
        }
    }

    private fun existe(m: SemanticsMatcher) = regra.onAllNodes(m).fetchSemanticsNodes().isNotEmpty()

    private fun textoDe(m: SemanticsMatcher): String = regra.onAllNodes(m).fetchSemanticsNodes()
        .first().config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString().orEmpty()

    private val anoDaLista = SemanticsMatcher("item de ano") { n ->
        n.config.getOrNull(SemanticsProperties.Text).orEmpty().any { Regex("^(19|20)\\d{2} ").containsMatchIn(it.text) }
    }

    @Test fun buscaPorMarcaModeloAnoComHistorico() {
        relatorio("=== Busca por marca, modelo e ano (APIs reais) ===")
        regra.onNodeWithText("Tabela").performClick()
        esperar("lista de marcas") { existe(hasText("Fiat")) }
        captura("e01_marcas")
        regra.onAllNodesWithText("Fiat")[0].performClick()

        esperar("lista de modelos") { existe(hasText("Buscar modelo ou versão")) && !existe(hasText("Buscar marca")) }
        regra.waitForIdle()
        regra.onNode(hasSetTextAction()).performTextInput("Mobi")
        esperar("modelo filtrado") { existe(hasClickAction() and hasText("Mobi", substring = true, ignoreCase = true) and !hasSetTextAction()) }
        captura("e02_modelos_filtrados")
        regra.onAllNodes(hasClickAction() and hasText("Mobi", substring = true, ignoreCase = true) and !hasSetTextAction())[0].performClick()

        esperar("lista de anos") { existe(anoDaLista) }
        captura("e03_anos")
        regra.onAllNodes(anoDaLista and hasClickAction(), useUnmergedTree = false)[0].performClick()

        esperar("valor FIPE") { existe(hasContentDescription("Valor FIPE R$", substring = true)) }
        relatorio("Valor exibido: " + textoDe(hasContentDescription("Valor FIPE", substring = true)))
        captura("e04_valor")

        esperar("historico carregado", 60_000) {
            existe(hasText("Variação")) || existe(hasText("Tentar novamente"))
        }
        regra.onNode(hasScrollToNodeAction()).performScrollToNode(hasText("Evolução do preço"))
        captura("e05_historico")
        relatorio("Historico: " + if (existe(hasText("Variação"))) "exibido" else "erro")
        relatorio("OK")
    }

    @Test fun buscaPelaPlacaFavoritaEApareceNosSalvos() {
        relatorio("=== Busca pela placa (provedor simulado, valor FIPE real) ===")
        // Provedor de placas simulado no proprio aparelho, no formato da API Placas.
        val mock = MockWebServer().also { servidor = it }
        mock.enqueue(
            MockResponse().setHeader("Content-Type", "application/json").setBody(
                """{"MARCA":"VW","MODELO":"GOL 1.0","ano":"2022","anoModelo":"2023","cor":"Prata","municipio":"Divinopolis","uf":"MG",
                   "placa":"RTA2C19","situacao":"Sem restrição",
                   "fipe":{"dados":[{"codigo_fipe":"005490-9","ano_modelo":"2023","combustivel":"Flex","score":100,
                   "texto_marca":"VW - VolksWagen","texto_modelo":"Gol 1.0 Flex 12V 5p","tipo_modelo":1}]}}"""
            )
        )
        mock.start()
        app.prefs.tokenPlaca = "teste"
        app.prefs.urlPlaca = "http://127.0.0.1:${mock.port}/consulta/{placa}/{token}"

        regra.onNodeWithText("Placa").performClick()
        regra.onNode(hasSetTextAction()).performTextInput("rta2c19")
        captura("p01_placa_digitada")
        regra.onNodeWithText("Consultar valor").performClick()

        esperar("valor FIPE pela placa", 45_000) { existe(hasContentDescription("Valor FIPE R$", substring = true)) }
        val pedido = mock.takeRequest()
        relatorio("Pedido ao provedor: ${pedido.path}")
        relatorio("Valor exibido: " + textoDe(hasContentDescription("Valor FIPE", substring = true)))
        captura("p02_resultado_placa")

        regra.onNode(hasContentDescription("Salvar nos favoritos")).performClick()
        esperar("favorito marcado") { existe(hasContentDescription("Remover dos favoritos")) }
        regra.onNodeWithText("Salvos").performClick()
        esperar("placa nos salvos") { existe(hasText("Gol 1.0 Flex 12V 5p")) }
        captura("p03_salvos")
        relatorio("OK")
    }
}
