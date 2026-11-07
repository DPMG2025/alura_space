package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.platform.testTag
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.dominio.Acao
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.ui.telas.TelaCasa
import br.com.casinhadosaber.ui.telas.TelaCasaPorDentro
import br.com.casinhadosaber.ui.telas.TelaInicio
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.TelaPais
import br.com.casinhadosaber.ui.telas.TelaPerfil
import br.com.casinhadosaber.ui.telas.TelaPremio
import br.com.casinhadosaber.ui.telas.TelaTrilha
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

/**
 * Gera imagens de todas as telas em build/outputs/roborazzi, para revisar o visual sem emulador.
 * O CI copia as imagens para docs/capturas.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val menino = Perfil("Leo", Genero.MENINO, pele = 0, cabelo = 3)
    private val gerador = GeradorDeFases(Random(4))

    /** Estado com [n] fases vencidas, espalhadas pelas trilhas, e os [colocados] primeiros itens na casa. */
    private fun estado(n: Int, colocados: Int, perfil: Perfil = menina): EstadoJogo {
        var e = EstadoJogo(perfil = perfil)
        val fases = (0 until 10).flatMap { i -> Trilha.entries.map { gerador.fases(it)[i] } }
        for ((k, f) in fases.take(n).withIndex()) e = e.registrarVitoria(f, 1 + k % 3).first
        for (item in ItensDaCasa.ordem.take(colocados)) e = e.colocar(item)
        return e
    }

    /** Mostra [conteudo] com narrador e sons desligados e o relógio parado (avança só quando o teste manda). */
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

    /**
     * Captura a tela depois de [tempo] ms. Se houver [acao] (tocar, arrastar), ela roda depois
     * desse tempo e a captura sai 1,2 s mais tarde, com as animações assentadas.
     */
    private fun capturar(nome: String, tempo: Long = 2500, acao: (() -> Unit)? = null, conteudo: @Composable () -> Unit) {
        montar(conteudo)
        regra.mainClock.advanceTimeBy(tempo)
        if (acao != null) {
            acao()
            regra.mainClock.advanceTimeBy(1200)
        }
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    /** Pergunta de mexer, do jeito que o gerador de fases monta. */
    private fun deMexer(instrucao: String, atividade: Atividade) =
        Pergunta.deAtividade(instrucao, listOf(pt(instrucao)), atividade, listOf(pt("Muito bem!")))

    private val cestaContar = deMexer("Coloque 4 na cesta", Atividade.Cesta(List(7) { "🍎" }, 4))
    private val cestaClassificar = deMexer(
        "Só animais na cesta",
        Atividade.Cesta(listOf("🐶", "🐱", "🐮"), 3, listOf("🍎", "⚽", "👕")),
    )
    private val memoria = deMexer(
        "Ache os pares",
        Atividade.Memoria(listOf("🐶" to "🐶", "🍎" to "🍎", "⚽" to "⚽", "🚗" to "🚗")),
    )
    private val montarPalavra = deMexer("Monte: OVO", Atividade.MontarPalavra("🥚", "OVO", listOf("O", "VO"), listOf("PA", "LU")))
    private val tracar = deMexer("Trace a letra A", Atividade.Tracar("A"))

    /** Arrasta a figura [indice] da cesta (contar) até o meio da cesta (marca invisível "cesta"). */
    private fun arrastarParaACesta(indice: Int) {
        val cesta = regra.onNodeWithTag("cesta").fetchSemanticsNode().boundsInRoot
        val figura = regra.onAllNodesWithTag("figura_certa")[indice]
        val origem = figura.fetchSemanticsNode().boundsInRoot
        val alvo = cesta.center - origem.topLeft
        figura.performTouchInput { swipe(center, alvo, durationMillis = 400) }
        regra.mainClock.advanceTimeBy(1000)
    }

    @Test fun perfil() = capturar("01_perfil") { TelaPerfil(null) {} }

    @Test fun perfilEscolhendo() = capturar("01b_perfil_escolhendo") {
        TelaPerfil(menina.copy(estiloCabelo = 2)) {}
    }

    @Config(qualifiers = "w800dp-h1280dp-mdpi")
    @Test fun tablet() = capturar("22_tablet_inicio") {
        androidx.compose.foundation.layout.Box(
            Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.TopCenter,
        ) {
            androidx.compose.foundation.layout.Box(Modifier.widthIn(max = 640.dp).fillMaxSize()) {
                TelaInicio(estado(20, 20), true, {}, {}, {}, {})
            }
        }
    }

    @Test fun inicioVazio() = capturar("02_inicio_comeco") {
        TelaInicio(estado(0, 0), true, {}, {}, {}, {})
    }

    @Test fun inicioComPresentes() = capturar("03_inicio_presentes") {
        TelaInicio(estado(9, 6), true, {}, {}, {}, {})
    }

    @Test fun inicioComMissao() = capturar("03b_inicio_missao_do_dia") {
        TelaInicio(estado(12, 12), true, {}, {}, {}, {}, missaoDisponivel = true)
    }

    @Test fun caminho() = capturar("04_caminho_numeros") {
        TelaTrilha(Trilha.MATEMATICA, estado(13, 13), {}, {})
    }

    @Test fun jogoContar() = capturar("05_jogo_contar") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[1], menina, {}, { })
    }

    @Test fun jogoSubtrair() = capturar("06_jogo_subtrair") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[8], menino, {}, { })
    }

    @Test fun jogoMaisOuMenos() = capturar("07_jogo_mais_ou_menos") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[4], menina, {}, { })
    }

    @Test fun jogoLetras() = capturar("08_jogo_silabas") {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[6], menino, {}, { })
    }

    @Test fun jogoIngles() = capturar("09_jogo_cores_ingles") {
        TelaJogo(gerador.fases(Trilha.INGLES)[4], menina, {}, { })
    }

    @Test fun jogoLogica() = capturar("10_jogo_sequencia") {
        TelaJogo(gerador.fases(Trilha.LOGICA)[6], menino, {}, { })
    }

    @Test fun premio() = capturar("11_premio") {
        TelaPremio(gerador.fases(Trilha.INGLES)[2], 3, ItensDaCasa.porId.getValue("cama"), menina, 5, true, {}, {}, {})
    }

    @Test fun quase() = capturar("11b_quase") {
        br.com.casinhadosaber.ui.telas.TelaQuase(gerador.fases(Trilha.LOGICA)[3], 2, menino, {}, {})
    }

    @Test fun casaComeco() = capturar("12_casa_comeco") {
        TelaCasa(estado(3, 0, menino), {}, {}, {})
    }

    @Test fun casaMeio() = capturar("13_casa_meio") {
        TelaCasa(estado(18, 14), {}, {}, {})
    }

    @Test fun casaCompleta() = capturar("14_casa_completa") {
        val e = estado(40, 40).let { it.pintar(br.com.casinhadosaber.dominio.Comodo.SALA) }
            .copy(posicaoCrianca = Posicao(0.45f, 0.99f))
        TelaCasa(e, {}, {}, {})
    }

    @Test fun casaDeNoite() = capturar("14b_casa_de_noite") {
        TelaCasa(estado(40, 40), {}, {}, {}, noiteInicial = true)
    }

    @Test fun dormir() = capturar("15b_hora_de_dormir") {
        br.com.casinhadosaber.ui.telas.TelaDormir(estado(20, 20)) {}
    }

    @Test fun jogoFacilitado() = capturar("17b_jogo_facilitado") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[3], menina, {}, { }, facilitada = true)
    }

    @Test fun loja() = capturar("18_loja") {
        val e = estado(30, 30).copy(moedas = 14, comprados = setOf("telhado1", "visitante🐰"), corTelhado = 1, visitantes = setOf("🐰"))
        TelaCasa(e, {}, {}, {}, lojaInicial = true)
    }

    @Test fun chapeus() = capturar("19_chapeus") {
        // Grade 2 x 4: menina e menino com cada chapéu, todos do mesmo tamanho.
        androidx.compose.foundation.layout.Column(
            Modifier.fillMaxSize().padding(top = 24.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            for (chapeu in listOf("👑", "🧢", "🎩", "🌸")) {
                androidx.compose.foundation.layout.Row {
                    for (p in listOf(menina, menino)) {
                        br.com.casinhadosaber.ui.componentes.Crianca(
                            p.copy(chapeu = chapeu),
                            Modifier.height(170.dp).padding(horizontal = 30.dp, vertical = 14.dp),
                        )
                    }
                }
            }
        }
    }

    @Test fun chuva() = capturar("20_chuva") {
        TelaCasa(estado(40, 40).copy(clima = 1), {}, {}, {})
    }

    @Test fun neve() = capturar("20b_neve") {
        TelaCasa(estado(40, 40).copy(clima = 2), {}, {}, {}, noiteInicial = true)
    }

    @Test fun enfeites() = capturar("21_papel_de_parede_e_visitantes") {
        TelaCasa(
            estado(40, 40).copy(papelDeParede = 2, corPorta = 1, corTelhado = 2, visitantes = setOf("🦔", "🦊", "🐢")),
            {}, {}, {},
        )
    }

    /**
     * Regressão: chuva e neve nunca podem aparecer dentro dos cômodos.
     *
     * As três cenas (sem clima, chuva e neve) ficam lado a lado no mesmo quadro, então as
     * animações estão no mesmo instante. Cada cena tem 120 x 138 dp (a proporção exata da cena,
     * 1,15) e as três cabem juntas na largura da tela (360 de 393 dp). Cada uma é capturada
     * sozinha, pelo seu testTag, e comparada pixel a pixel com a cena sem clima só dentro da casa
     * (paredes e telhado, com margem para o contorno). Qualquer diferença ali é chuva ou neve.
     * Com a casa em camadas 3D, a chuva e a neve caem por cima de tudo (itens também), recortadas nas mesmas
     * paredes e no mesmo telhado; a neve parada no telhado (camada da arte) é descontada pelo alfa dela.
     */
    @Test fun chuvaNuncaCaiDentroDeCasa() {
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        val base = estado(40, 40)
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    androidx.compose.foundation.layout.Row {
                        for (c in 0..2) {
                            Box(Modifier.size(120.dp, 138.dp).testTag("clima$c")) {
                                br.com.casinhadosaber.ui.componentes.CenaDaCasa(base.copy(clima = c), Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(700)
        // Imagem das três cenas juntas, só para conferir a olho em docs/capturas.
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/23_teste_chuva_fora_de_casa.png")
        // Uma captura por cena, lida do arquivo recém gravado (apagado antes para não ler um antigo).
        val cenas = (0..2).map { c ->
            val arquivo = java.io.File("build/outputs/roborazzi/teste_chuva/clima_$c.png")
            arquivo.parentFile?.mkdirs()
            arquivo.delete()
            regra.onNodeWithTag("clima$c").captureRoboImage(arquivo.path)
            org.junit.Assert.assertTrue("captura da cena $c não foi gravada", arquivo.exists())
            android.graphics.BitmapFactory.decodeFile(arquivo.absolutePath)
                ?: throw AssertionError("captura da cena $c ilegível")
        }
        val w = cenas[0].width
        val h = cenas[0].height
        for (img in cenas) org.junit.Assert.assertTrue("cenas com tamanhos diferentes", img.width == w && img.height == h)
        // A cena precisa ter a proporção certa, senão as frações abaixo não apontam para a casa.
        org.junit.Assert.assertEquals("proporção da cena", w * 1.15f, h.toFloat(), 3f)

        // Medidas da casa (Planta em CenaDaCasa.kt): paredes de x 0,10 a 0,90 e y 0,30 a 0,78;
        // telhado é o triângulo (0,04; 0,315), (0,5; 0,075), (0,96; 0,315).
        val m = 3f // margem em pixels, longe do contorno e da camadinha de neve do telhado
        fun dentroDasParedes(x: Int, y: Int) =
            x > 0.10f * w + m && x < 0.90f * w - m && y > 0.30f * h + m && y < 0.78f * h - m
        fun dentroDoTelhado(x: Int, y: Int): Boolean {
            // Abaixo da camada de neve (que vai da borda até a linha (0,07; 0,318)-(0,5; 0,095)).
            val fx = x.toFloat() / w
            val fy = y.toFloat() / h
            if (fy > 0.30f) return false
            val linhaDaNeve = 0.095f + (0.318f - 0.095f) * (kotlin.math.abs(fx - 0.5f) / 0.43f)
            return y > (linhaDaNeve * h) + m && fx > 0.07f && fx < 0.93f
        }
        // Casa em camadas 3D (Fase 3): a neve no telhado é uma camada da arte (`casa_neve_no_telhado`) que cobre
        // a cumeeira e desce um pouco nos beirais. Onde ela tem cor (com a mesma margem) não conta como "dentro":
        // é a neve parada em cima do telhado, não neve caindo. A camada tem o quadro da cena (alinhada no topo).
        val neveNoTelhado: android.graphics.Bitmap? = runCatching {
            RuntimeEnvironment.getApplication().assets.open("arte/casa_neve_no_telhado.webp").use {
                android.graphics.BitmapFactory.decodeStream(it)
            }?.let { android.graphics.Bitmap.createScaledBitmap(it, w, h, true) }
        }.getOrNull()
        fun naNeveDoTelhado(x: Int, y: Int): Boolean {
            val neve = neveNoTelhado ?: return false
            val k = m.toInt()
            for (dy in -k..k step k) for (dx in -k..k step k) {
                val px = (x + dx).coerceIn(0, w - 1)
                val py = (y + dy).coerceIn(0, h - 1)
                if (android.graphics.Color.alpha(neve.getPixel(px, py)) > 0) return true
            }
            return false
        }
        val referencia = cenas[0]
        for (c in 1..2) {
            val img = cenas[c]
            var dentro = 0
            var fora = 0
            for (y in 0 until h) for (x in 0 until w) {
                if (img.getPixel(x, y) == referencia.getPixel(x, y)) continue
                val naCasa = dentroDasParedes(x, y) || dentroDoTelhado(x, y)
                if (naCasa && !(c == 2 && naNeveDoTelhado(x, y))) dentro++ else fora++
            }
            // Sem isto o teste passaria à toa se o clima não fosse desenhado.
            org.junit.Assert.assertTrue("clima $c não desenhou nada do lado de fora", fora > 50)
            // Uns poucos pixels de borda (antisserrilhado do contorno) podem variar; chuva de verdade dá centenas.
            org.junit.Assert.assertTrue("clima $c desenhou dentro de casa ($dentro pixels)", dentro <= 12)
        }
    }

    @Test fun pais() = capturar("15_area_dos_pais") {
        TelaPais(estado(12, 12), true, {}, true, {}, {}, {}, {})
    }

    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    @Test fun inicioCelularPequeno() = capturar("16_inicio_celular_pequeno") {
        TelaInicio(estado(20, 20, menino), true, {}, {}, {}, {})
    }

    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    @Test fun jogoCelularPequeno() = capturar("17_jogo_celular_pequeno") {
        TelaJogo(gerador.fases(Trilha.INGLES)[7], menino, {}, { })
    }

    // ------------------------------------------------------------------ atividades de mexer

    @Test fun atividadeCestaContar() = capturar("24_atividade_cesta_contar") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, {}, perguntasProntas = listOf(cestaContar))
    }

    @Test fun atividadeCestaClassificar() = capturar("25_atividade_cesta_classificar") {
        TelaJogo(gerador.fases(Trilha.LOGICA)[7], menino, {}, {}, perguntasProntas = listOf(cestaClassificar))
    }

    /** Depois da espiadinha (2,6 s com 8 cartas) as cartas viram; um par já achado aparece verde. */
    @Test fun atividadeMemoria() = capturar(
        "26_atividade_memoria",
        tempo = 3500,
        acao = {
            val par = regra.onAllNodesWithTag("carta_0")
            par[0].performClick()
            regra.mainClock.advanceTimeBy(200)
            par[1].performClick()
        },
    ) {
        TelaJogo(gerador.fases(Trilha.LOGICA)[14], menina, {}, {}, perguntasProntas = listOf(memoria))
    }

    @Test fun atividadeMontarPalavra() = capturar("27_atividade_montar_palavra") {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[13], menino, {}, {}, perguntasProntas = listOf(montarPalavra))
    }

    @Test fun atividadeTracar() = capturar("28_atividade_tracar") {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[1], menina, {}, {}, perguntasProntas = listOf(tracar))
    }

    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    @Test fun atividadeCestaCelularPequeno() = capturar("29_atividade_cesta_celular_pequeno") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menino, {}, {}, perguntasProntas = listOf(cestaContar))
    }

    /**
     * Rodada facilitada: a primeira pergunta é um exemplo resolvido junto, sorteado da própria fase.
     * A fase "Cesta das coisas" (lógica, 8ª) só tem cesta de classificar, então o exemplo é sempre
     * uma cesta, com as figuras do grupo brilhando e a mãozinha mostrando o gesto.
     */
    @Test fun atividadeFacilitada() = capturar("30_atividade_facilitada_ajuda") {
        TelaJogo(gerador.fases(Trilha.LOGICA)[7], menina, {}, {}, facilitada = true, perguntasProntas = listOf(cestaClassificar))
    }

    /**
     * Regressão da lógica da cesta: arrastar figuras com o dedo faz a contagem subir, e com a
     * quantidade pedida o "certinho" conclui a pergunta como acerto de primeira.
     */
    @Test fun cestaContaAoArrastar() {
        var resultado: List<Boolean>? = null
        montar {
            TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, { resultado = it }, perguntasProntas = listOf(cestaContar))
        }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithText("0").assertExists()
        // O certinho só aparece quando há fruta na cesta (apagado, parecia um botão para tocar).
        regra.onNodeWithTag("atividade_pronto").assertDoesNotExist()

        arrastarParaACesta(0)
        regra.onNodeWithText("1").assertExists()
        regra.onNodeWithTag("atividade_pronto").assertIsEnabled()

        for (i in 1 until 4) arrastarParaACesta(i)
        regra.onNodeWithText("4").assertExists()
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/31_teste_cesta_arrastar.png")

        regra.onNodeWithTag("atividade_pronto").performClick()
        regra.mainClock.advanceTimeBy(6000)
        org.junit.Assert.assertEquals("a cesta com 4 deveria concluir como acerto de primeira", listOf(true), resultado)
    }

    // ------------------------------------------------------------------ telas novas

    /** As [n] primeiras fases de [trilha] vencidas (para ver a trilha de 25 fases quase no fim). */
    private fun trilhaVencida(trilha: Trilha, n: Int): EstadoJogo {
        var e = estado(40, 40)
        for (f in gerador.fases(trilha).take(n)) e = e.registrarVitoria(f, 1 + f.numero % 3).first
        return e
    }

    /** Casa completa, com comidas na geladeira e roupas no guarda-roupa. */
    private fun casaCheia(): EstadoJogo {
        val compras = Loja.produtos.filter { it.categoria == br.com.casinhadosaber.dominio.Categoria.COMIDA }.take(6).map { it.id } +
            Loja.produtos.filter { it.categoria == br.com.casinhadosaber.dominio.Categoria.ROUPA }.take(3).map { it.id }
        return estado(40, 40).copy(moedas = 23, comprados = compras.toSet())
    }

    @Test fun casaPorDentro() = capturar("32_casa_por_dentro") {
        TelaCasaPorDentro(estado(40, 40), {}, {})
    }

    /** Primeira visita: depois de uns segundos parada, a mãozinha segura um móvel e leva para o lado (realce verde). */
    @Test fun casaPorDentroDicaDeArrastar() = capturar("32b_casa_por_dentro_dica_arrastar", tempo = 3200) {
        TelaCasaPorDentro(estado(40, 40), {}, {}, dicaLogo = true)
    }

    @Test fun casaPorDentroGeladeira() = capturar("33_casa_por_dentro_geladeira") {
        TelaCasaPorDentro(casaCheia(), {}, {}, abrirInicial = Acao.GELADEIRA)
    }

    @Test fun casaPorDentroGuardaRoupa() = capturar("34_casa_por_dentro_guarda_roupa") {
        val e = casaCheia().let { it.copy(perfil = it.perfil?.copy(traje = "listras")) }
        TelaCasaPorDentro(e, {}, {}, abrirInicial = Acao.GUARDA_ROUPA)
    }

    @Test fun casaPorDentroLoja() = capturar("35_casa_por_dentro_loja") {
        TelaCasaPorDentro(casaCheia(), {}, {}, lojaInicial = true)
    }

    @Test fun espelho() = capturar("36_espelho") {
        val e = casaCheia()
        TelaPerfil(menina.copy(chapeu = "👑", traje = "listras"), espelho = true, roupas = Loja.noGuardaRoupa(e), onVoltar = {}) {}
    }

    /** Todas as roupas, cada uma numa criança diferente (os dois gêneros e todos os tons de pele). */
    @Test fun roupasEmTodasAsCriancas() = capturar("58_roupas_em_todas_as_criancas") {
        val todas = Loja.trajes.mapIndexed { i, t ->
            Perfil("", if (i % 2 == 0) Genero.MENINA else Genero.MENINO, pele = i % 4, cabelo = (i / 2) % 4, estiloCabelo = i % 3, traje = t.id)
        }
        androidx.compose.foundation.layout.Column(Modifier.padding(4.dp)) {
            for (linha in todas.chunked(4)) {
                androidx.compose.foundation.layout.Row {
                    for (p in linha) br.com.casinhadosaber.ui.componentes.Crianca(p, Modifier.height(150.dp).padding(6.dp))
                }
            }
        }
    }

    /** Um acessório em cada parte do corpo ao mesmo tempo, mais chapéu e roupa, e cada acessório sozinho. */
    @Test fun acessoriosNaCrianca() = capturar("59_acessorios_na_crianca") {
        val partes = br.com.casinhadosaber.dominio.ParteDoCorpo.entries
        val combinados = (0 until 3).map { k ->
            val base = if (k % 2 == 0) menino else menina
            base.copy(
                acessorios = partes.associateWith { parte -> Loja.acessorios.filter { it.parte == parte }[k].id },
                traje = listOf("macacao", "vestido", "")[k],
                chapeu = listOf("", "", "🧢")[k],
            )
        }
        val sozinhos = Loja.acessorios.mapIndexed { i, a ->
            Perfil("", if (i % 2 == 0) Genero.MENINO else Genero.MENINA, pele = (i + 1) % 4, cabelo = i % 4, acessorios = mapOf(a.parte to a.id))
        }
        androidx.compose.foundation.layout.Column(Modifier.padding(4.dp)) {
            androidx.compose.foundation.layout.Row {
                for (p in combinados) br.com.casinhadosaber.ui.componentes.Crianca(p, Modifier.height(190.dp).padding(8.dp), acenando = true)
            }
            for (linha in sozinhos.chunked(5)) {
                androidx.compose.foundation.layout.Row {
                    for (p in linha) br.com.casinhadosaber.ui.componentes.Crianca(p, Modifier.height(112.dp).padding(4.dp))
                }
            }
        }
    }

    /** Espelho com acessórios comprados: o provador com as gavetas. */
    @Test fun espelhoComAcessorios() = capturar("60_espelho_acessorios") {
        val comprados = setOf("acessoriooculos", "acessoriobalao", "acessoriotenis", "roupafutebol", "chapeu👑")
        val p = menina.copy(traje = "futebol", acessorios = mapOf(br.com.casinhadosaber.dominio.ParteDoCorpo.ROSTO to "oculos", br.com.casinhadosaber.dominio.ParteDoCorpo.MAO to "balao"))
        TelaPerfil(p, espelho = true, comprados = comprados, onVoltar = {}) {}
    }

    @Test fun casaComEspelhoEPorta() = capturar("37_casa_espelho_e_porta") {
        TelaCasa(estado(40, 40), {}, {}, {}, onEspelho = {}, onEntrar = {})
    }

    private fun abrirPresente() {
        // Um toque só abre o presente.
        regra.onAllNodes(hasContentDescription("Presente. Toque para abrir.")).onFirst().performClick()
        regra.mainClock.advanceTimeBy(400)
    }

    @Test fun premioBrindeChapeu() = capturar("38_premio_brinde_chapeu", acao = ::abrirPresente) {
        TelaPremio(gerador.fases(Trilha.LOGICA)[20], 3, null, menina, 12, true, {}, {}, {}, brinde = Loja.porId.getValue("chapeu👑"))
    }

    @Test fun premioBrindeTelhado() = capturar("39_premio_brinde_telhado", acao = ::abrirPresente) {
        TelaPremio(gerador.fases(Trilha.MATEMATICA)[21], 2, null, menino, 9, false, {}, {}, {}, brinde = Loja.porId.getValue("telhado2"))
    }

    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    @Test fun trilhaQuaseNoFim() = capturar("40_trilha_22_fases") {
        TelaTrilha(Trilha.PORTUGUES, trilhaVencida(Trilha.PORTUGUES, 22), {}, {})
    }

    @Config(qualifiers = "w800dp-h1280dp-mdpi")
    @Test fun trilhaTablet() = capturar("41_trilha_tablet") {
        TelaTrilha(Trilha.INGLES, trilhaVencida(Trilha.INGLES, 22), {}, {})
    }

    /** Área dos pais já liberada (respondendo a continha do portão), com histórico de várias fases. */
    @Test fun paisComHistorico() {
        var e = EstadoJogo(perfil = menina)
        val resultados = listOf(
            listOf(true, true, true, true, true), listOf(true, false, true, true, true),
            listOf(false, true, false, true, true), listOf(true, true, true, true, false),
        )
        for (t in Trilha.entries) for (i in 0 until 6) {
            val r = resultados[(i + t.ordinal) % resultados.size]
            e = e.registrarPartida(gerador.fases(t)[i], r.count { it }, r.size, resultados = r).estado
        }
        e = e.copy(segundosJogados = 47 * 60)
        capturar("42_pais_com_historico", acao = {
            val conta = regra.onNode(hasText("Quanto é", substring = true)).fetchSemanticsNode()
                .config.getOrNull(SemanticsProperties.Text)?.joinToString { it.text }.orEmpty()
            val (a, b) = Regex("""(\d+) × (\d+)""").find(conta)!!.destructured
            for (d in (a.toInt() * b.toInt()).toString()) {
                regra.onAllNodes(hasContentDescription(d.toString())).onFirst().performClick()
            }
            regra.onAllNodes(hasContentDescription("OK")).onFirst().performClick()
        }) {
            TelaPais(e, true, {}, true, {}, {}, {}, {})
        }
    }

    // ------------------------------------------------------------------ regressões visuais e tablet deitado

    /**
     * Regressão: o chapéu fica na cabeça da criança. Cada criança é desenhada com e sem cada chapéu,
     * no mesmo quadro (o relógio parado deixa as animações iguais) e com medidas em pixels inteiros.
     * A diferença entre as duas imagens é o chapéu; a caixa dela tem que encostar na cabeça, centrada, sem
     * cobrir o rosto. A cabeça vem da âncora do manifesto da arte 3D (`crianca_cabeca`: `cabeca` e
     * `topoDaCabeca`), posta na caixa da criança pela mesma conta que o componente usa.
     *
     * Fase 7: os chapéus são camadas 3D com sombra macia na cabeça (a aba do boné faz sombra no rosto). Por
     * isso o "não cobre o rosto" olha só a diferença forte (o corpo do chapéu, não a sombra), que não pode
     * passar da linha dos olhos; e a flor fica de lado, no cabelo: confere que está dentro da largura da
     * cabeça, em vez de centrada. A mesma régua cobre um traje (macacão: no corpo, entre a boca e os pés,
     * centrado) e um acessório (óculos: na altura dos olhos, cobrindo os dois).
     */
    // Tela larga: com a criança 3D (85 dp de largura) a fila de chapéus não cabe nos 393 dp e os nós saem cortados.
    @Config(qualifiers = "w1280dp-h800dp-land-xhdpi")
    @Test fun chapeuFicaNaCabeca() {
        val narrador = Narrador(RuntimeEnvironment.getApplication()).apply { ativo = false }
        val sons = Sons().apply { ativo = false }
        val chapeus = listOf("") + Loja.produtos.filter { it.categoria == br.com.casinhadosaber.dominio.Categoria.CHAPEU }.map { it.valor }
        // Menina com tranças (o laço dos coques some quando há chapéu e entraria na diferença).
        val perfis = listOf(menina.copy(estiloCabelo = 2), menino)
        // Peças conferidas pela mesma régua (colunas depois dos chapéus): um traje e um acessório.
        val comTraje = { p: Perfil -> p.copy(traje = "macacao") }
        val comAcessorio = { p: Perfil -> p.copy(acessorios = mapOf(br.com.casinhadosaber.dominio.ParteDoCorpo.ROSTO to "oculos")) }
        val app = RuntimeEnvironment.getApplication()
        for (p in perfis.flatMap { p -> chapeus.map { p.copy(chapeu = it) } + comTraje(p) + comAcessorio(p) }) {
            // Vestida (chapéu, traje ou acessório), a criança continua nas camadas 3D (e não no desenho antigo).
            org.junit.Assert.assertTrue(
                "a criança $p devia usar as camadas 3D",
                br.com.casinhadosaber.ui.componentes.ComposicaoDosPersonagens.podeUsar(app, br.com.casinhadosaber.ui.componentes.PersonagemDaArte.CRIANCA, p),
            )
        }
        val cabeca = br.com.casinhadosaber.ui.componentes.ArteDoApp.info(app, "crianca_cabeca")
            ?: throw AssertionError("crianca_cabeca fora do manifesto")
        val centroDaCabeca = cabeca.ancoras["cabeca"] ?: throw AssertionError("sem a âncora 'cabeca'")
        val topoDaCabeca = cabeca.ancoras["topoDaCabeca"] ?: throw AssertionError("sem a âncora 'topoDaCabeca'")
        val olhoEsquerdo = cabeca.ancoras["olhoEsquerdo"] ?: throw AssertionError("sem a âncora 'olhoEsquerdo'")
        val olhoDireito = cabeca.ancoras["olhoDireito"] ?: throw AssertionError("sem a âncora 'olhoDireito'")
        val boca = cabeca.ancoras["boca"] ?: throw AssertionError("sem a âncora 'boca'")
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                CompositionLocalProvider(LocalNarrador provides narrador, LocalSons provides sons) {
                    androidx.compose.foundation.layout.Column {
                        for ((i, p) in perfis.withIndex()) {
                            androidx.compose.foundation.layout.Row {
                                for ((j, c) in chapeus.withIndex()) {
                                    // 130 dp de altura dá 85 dp de largura (quadro 170 x 260): 170 x 380 px inteiros em xhdpi.
                                    Box(Modifier.testTag("chapeu_${i}_$j").padding(top = 60.dp)) {
                                        br.com.casinhadosaber.ui.componentes.Crianca(p.copy(chapeu = c), Modifier.height(130.dp), animado = false)
                                    }
                                }
                                Box(Modifier.testTag("traje_$i").padding(top = 60.dp)) {
                                    br.com.casinhadosaber.ui.componentes.Crianca(comTraje(p), Modifier.height(130.dp), animado = false)
                                }
                                Box(Modifier.testTag("acessorio_$i").padding(top = 60.dp)) {
                                    br.com.casinhadosaber.ui.componentes.Crianca(comAcessorio(p), Modifier.height(130.dp), animado = false)
                                }
                            }
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(300)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/56_teste_chapeus_na_cabeca.png")
        val topoDaCrianca = with(regra.density) { 60.dp.toPx() }
        fun imagem(tag: String): android.graphics.Bitmap {
            val arquivo = java.io.File("build/outputs/roborazzi/teste_chapeu/$tag.png")
            arquivo.parentFile?.mkdirs()
            arquivo.delete()
            regra.onNodeWithTag(tag).captureRoboImage(arquivo.path)
            return android.graphics.BitmapFactory.decodeFile(arquivo.absolutePath) ?: throw AssertionError("captura $tag ilegível")
        }
        fun diferenca(a: Int, b: Int): Int =
            kotlin.math.abs(android.graphics.Color.red(a) - android.graphics.Color.red(b)) +
                kotlin.math.abs(android.graphics.Color.green(a) - android.graphics.Color.green(b)) +
                kotlin.math.abs(android.graphics.Color.blue(a) - android.graphics.Color.blue(b)) +
                kotlin.math.abs(android.graphics.Color.alpha(a) - android.graphics.Color.alpha(b))
        fun diferente(a: Int, b: Int): Boolean = diferenca(a, b) > 60
        /** A caixa da diferença entre [com] e [sem] nas linhas antes de [ate] (e a última linha com diferença forte). */
        class Caixa(val x0: Int, val y0: Int, val x1: Int, val y1: Int, val n: Int, val y1Forte: Int)
        fun caixaDaDiferenca(com: android.graphics.Bitmap, sem: android.graphics.Bitmap, ate: Int): Caixa {
            org.junit.Assert.assertTrue("tamanhos diferentes", com.width == sem.width && com.height == sem.height)
            var x0 = Int.MAX_VALUE; var y0 = Int.MAX_VALUE; var x1 = -1; var y1 = -1; var n = 0; var y1Forte = -1
            for (y in 0 until minOf(sem.height, ate)) {
                // Linha com só um ou dois pixels diferentes é borda antisserrilhada, não peça.
                val xs = (0 until sem.width).filter { x -> diferente(com.getPixel(x, y), sem.getPixel(x, y)) }
                if (xs.size < 3) continue
                n += xs.size
                if (xs.first() < x0) x0 = xs.first()
                if (xs.last() > x1) x1 = xs.last()
                if (y < y0) y0 = y
                if (y > y1) y1 = y
                // Diferença forte: a peça em si (a sombra macia que ela faz escurece pouco).
                if (xs.count { x -> diferenca(com.getPixel(x, y), sem.getPixel(x, y)) > 150 } >= 3) y1Forte = y
            }
            return Caixa(x0, y0, x1, y1, n, y1Forte)
        }
        for (i in perfis.indices) {
            val sem = imagem("chapeu_${i}_0")
            val w = sem.width
            val alturaCrianca = sem.height - topoDaCrianca
            // A cabeça na captura: o quadro da arte posto na caixa da criança (pés em apoioNaCaixa da altura).
            val caixa = br.com.casinhadosaber.ui.componentes.CamadasDosPersonagens.caixaDoQuadro(
                cabeca, w.toFloat(), alturaCrianca, br.com.casinhadosaber.ui.componentes.PersonagemDaArte.CRIANCA.apoioNaCaixa,
            )
            val centroX = caixa.x(centroDaCabeca.x.toFloat())
            val centroY = topoDaCrianca + caixa.y(centroDaCabeca.y.toFloat())
            val topoY = topoDaCrianca + caixa.y(topoDaCabeca.y.toFloat())
            val raio = centroY - topoY
            org.junit.Assert.assertTrue("cabeça pequena demais na captura (raio $raio px)", raio > 20f)
            val olhoY = topoDaCrianca + caixa.y(olhoEsquerdo.y.toFloat())
            for (j in 1 until chapeus.size) {
                // Só a faixa da cabeça e acima conta (o chapéu mora ali; o corpo parado pode variar um pouco).
                val c = caixaDaDiferenca(imagem("chapeu_${i}_$j"), sem, (centroY + raio).toInt())
                val qual = "chapéu ${chapeus[j]} no perfil $i"
                org.junit.Assert.assertTrue("$qual não foi desenhado", c.n > 50)
                org.junit.Assert.assertTrue("$qual cortado no alto (sai da área da criança)", c.y0 > 1)
                org.junit.Assert.assertTrue(
                    "$qual flutua acima da cabeça (base ${c.y1}, topo da cabeça $topoY)",
                    c.y1 >= topoY - raio * 0.15f,
                )
                org.junit.Assert.assertTrue(
                    "$qual cobre o rosto (base do chapéu ${c.y1Forte}, linha dos olhos $olhoY)",
                    c.y1Forte >= 0 && c.y1Forte <= olhoY + raio * 0.04f,
                )
                if (chapeus[j] == "🌸") {
                    // A flor fica de lado, presa no cabelo: dentro da largura da cabeça.
                    org.junit.Assert.assertTrue(
                        "$qual fora da cabeça (${c.x0}..${c.x1})",
                        c.x0 >= centroX - raio * 1.2f && c.x1 <= centroX + raio * 1.2f,
                    )
                } else {
                    org.junit.Assert.assertEquals("$qual fora do centro da cabeça", centroX, (c.x0 + c.x1) / 2f, raio * 0.3f)
                }
                org.junit.Assert.assertTrue("$qual largo demais para a cabeça", c.x1 - c.x0 <= raio * 2.6f)
            }
            // Traje (macacão): no corpo, entre a boca e os pés, centrado.
            val bocaY = topoDaCrianca + caixa.y(boca.y.toFloat())
            val apoioY = topoDaCrianca + caixa.y(cabeca.apoio.y.toFloat())
            val traje = caixaDaDiferenca(imagem("traje_$i"), sem, sem.height)
            org.junit.Assert.assertTrue("o macacão não foi desenhado no perfil $i", traje.n > 200)
            org.junit.Assert.assertTrue("o macacão sobe no rosto (topo ${traje.y0}, boca $bocaY)", traje.y0 > bocaY)
            org.junit.Assert.assertTrue("o macacão passa dos pés (base ${traje.y1}, apoio $apoioY)", traje.y1 <= apoioY)
            org.junit.Assert.assertEquals("o macacão fora do centro do corpo", centroX, (traje.x0 + traje.x1) / 2f, raio * 0.3f)
            // Acessório (óculos): na altura dos olhos, cobrindo os dois, sem descer até a boca.
            val oculos = caixaDaDiferenca(imagem("acessorio_$i"), sem, sem.height)
            org.junit.Assert.assertTrue("os óculos não foram desenhados no perfil $i", oculos.n > 50)
            org.junit.Assert.assertTrue(
                "os óculos não cobrem os dois olhos (${oculos.x0}..${oculos.x1})",
                oculos.x0 <= caixa.x(olhoEsquerdo.x.toFloat()) && oculos.x1 >= caixa.x(olhoDireito.x.toFloat()),
            )
            org.junit.Assert.assertEquals("os óculos fora da linha dos olhos", olhoY, (oculos.y0 + oculos.y1) / 2f, raio * 0.3f)
            org.junit.Assert.assertTrue("os óculos altos demais (${oculos.y0}..${oculos.y1})", oculos.y1 - oculos.y0 <= raio)
            org.junit.Assert.assertEquals("os óculos fora do centro do rosto", centroX, (oculos.x0 + oculos.x1) / 2f, raio * 0.3f)
        }
    }

    /**
     * Regressão: na casa completa, os objetos nas posições padrão não se amontoam (a cama com o
     * ursinho e a mesa com o bolo são de propósito). As caixas vêm do domínio, com a mesma proporção
     * de desenho que a cena usa, e o tamanho da cena confere com o da interface.
     */
    @Test fun objetosDaCasaCompletaNaoSeAmontoam() {
        org.junit.Assert.assertEquals(br.com.casinhadosaber.ui.componentes.PROPORCAO_CENA, ItensDaCasa.PROPORCAO_DA_CENA, 0.0001f)
        for (item in ItensDaCasa.ordem.filter { !it.ehEstrutura }) {
            org.junit.Assert.assertEquals(item.id, br.com.casinhadosaber.ui.componentes.proporcaoDoItem(item), ItensDaCasa.proporcaoDe(item), 0.0001f)
        }
        val achadas = ItensDaCasa.sobreposicoesPadrao(limite = 0.25f)
        org.junit.Assert.assertTrue(
            "objetos amontoados: " + achadas.joinToString { (a, b, f) -> "$a e $b (${(f * 100).toInt()}%)" },
            achadas.isEmpty(),
        )
    }

    /**
     * Cama solta no jardim: ela volta tremendo, um balão "não" desenhado aparece em cima dela e o
     * quarto brilha (o lugar certo). Captura para conferir a olho.
     */
    @Test fun camaRecusadaMostraBalaoEBrilho() {
        montar { TelaCasa(estado(40, 40), {}, {}, {}) }
        regra.mainClock.advanceTimeBy(1500)
        // Pega a cama pela beirada esquerda (o ursinho fica no meio dela) e solta no gramado.
        regra.onAllNodes(hasContentDescription("Cama")).onFirst().performTouchInput {
            val inicio = Offset(width * 0.12f, height * 0.7f)
            swipe(inicio, inicio + Offset(0f, height * 2.6f), durationMillis = 500)
        }
        regra.mainClock.advanceTimeBy(900)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/55_casa_cama_recusada.png")
        regra.onAllNodes(hasContentDescription("Não pode aqui")).onFirst().assertExists()
        regra.onAllNodes(hasContentDescription("Lugar certo brilhando")).onFirst().assertExists()
        // O balão some sozinho.
        regra.mainClock.advanceTimeBy(4000)
        org.junit.Assert.assertEquals(0, regra.onAllNodes(hasContentDescription("Não pode aqui")).fetchSemanticsNodes().size)
    }

    /** Tela como no app: céu atrás, escala de tablet e largura máxima (MainActivity usa a mesma moldura). */
    @Composable
    private fun NoTablet(larga: Boolean, conteudo: @Composable () -> Unit) {
        Box(Modifier.fillMaxSize()) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                drawRect(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(br.com.casinhadosaber.ui.tema.Cores.Ceu, br.com.casinhadosaber.ui.tema.Cores.Fundo)))
            }
            MolduraDoApp(larga = larga) { conteudo() }
        }
    }

    @Config(qualifiers = "w1280dp-h800dp-land-mdpi")
    @Test fun paisagemInicio() = capturar("50_paisagem_inicio") {
        NoTablet(larga = true) { TelaInicio(estado(20, 20), true, {}, {}, {}, {}, missaoDisponivel = true) }
    }

    @Config(qualifiers = "w1280dp-h800dp-land-mdpi")
    @Test fun paisagemTrilha() = capturar("51_paisagem_trilha") {
        NoTablet(larga = false) { TelaTrilha(Trilha.INGLES, trilhaVencida(Trilha.INGLES, 22), {}, {}) }
    }

    @Config(qualifiers = "w1280dp-h800dp-land-mdpi")
    @Test fun paisagemJogo() = capturar("52_paisagem_jogo") {
        NoTablet(larga = false) { TelaJogo(gerador.fases(Trilha.MATEMATICA)[1], menina, {}, { }) }
    }

    @Config(qualifiers = "w1280dp-h800dp-land-mdpi")
    @Test fun paisagemCasa() = capturar("53_paisagem_casa") {
        NoTablet(larga = true) { TelaCasa(estado(40, 40), {}, {}, {}, onEspelho = {}, onEntrar = {}) }
    }

    @Config(qualifiers = "w1280dp-h800dp-land-mdpi")
    @Test fun paisagemLoja() = capturar("54_paisagem_loja") {
        val e = estado(30, 30).copy(moedas = 14, comprados = setOf("telhado1"), corTelhado = 1)
        NoTablet(larga = true) { TelaCasa(e, {}, {}, {}, lojaInicial = true) }
    }

    @Config(qualifiers = "w1280dp-h800dp-land-mdpi")
    @Test fun paisagemPremio() = capturar("57_paisagem_premio") {
        NoTablet(larga = false) {
            TelaPremio(gerador.fases(Trilha.INGLES)[2], 3, ItensDaCasa.porId.getValue("cama"), menina, 5, true, {}, {}, {})
        }
    }

    /** Caminho com o mundo 2 vencido, entrando no mundo 3 (Praia): plaquinhas de mundo e o botão Treinar embaixo. */
    @Test fun trilhaComTreinoEMundos() = capturar("61_trilha_treinar_mundos") {
        TelaTrilha(Trilha.MATEMATICA, trilhaVencida(Trilha.MATEMATICA, 20), {}, {}, onTreinar = {})
    }

    /** Fases novas dos mundos 4 e 5, uma de cada caminho, e o labirinto. */
    @Test fun faseDezenasEUnidades() = capturar("62_fase_dezenas_e_unidades") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[31], menina, {}, { })
    }

    @Test fun faseLerFrases() = capturar("63_fase_ler_frases") {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[35], menino, {}, { })
    }

    @Test fun faseILike() = capturar("64_fase_i_like") {
        TelaJogo(gerador.fases(Trilha.INGLES)[33], menina, {}, { })
    }

    @Test fun faseSudoku() = capturar("65_fase_sudoku") {
        TelaJogo(gerador.fases(Trilha.LOGICA)[36], menino, {}, { })
    }

    @Test fun faseLabirinto() = capturar("66_fase_labirinto") {
        TelaJogo(gerador.fases(Trilha.LOGICA)[41], menina, {}, { })
    }

    /** Pergunta de lado do labirinto: as quatro respostas são setas desenhadas (não emoji). */
    @Test fun faseLabirintoSetas() = capturar("66b_fase_labirinto_setas") {
        val g = GeradorDeFases(Random(7))
        val fase = g.fases(Trilha.LOGICA)[41]
        val comSetas = generateSequence { g.perguntas(fase) }.take(50).flatten()
            .first { p -> p.opcoes.all { it.visual is br.com.casinhadosaber.dominio.Visual.Seta } }
        TelaJogo(fase, menina, {}, { }, treino = { comSetas })
    }

    /** No treino, o voltar do Android termina como o ✖: entrega os resultados (moedinhas e histórico) e não só sai. */
    @Test fun voltarNoTreinoTerminaComoOX() {
        val fase = gerador.fases(Trilha.MATEMATICA)[1]
        var concluiu: List<Boolean>? = null
        var vezes = 0
        var saiu = false
        var dono: androidx.activity.OnBackPressedDispatcherOwner? = null
        montar {
            dono = androidx.activity.compose.LocalOnBackPressedDispatcherOwner.current
            TelaJogo(
                fase, menina,
                onSair = { saiu = true },
                onConcluir = { concluiu = it; vezes++ },
                treino = { gerador.perguntas(fase, 1).first() },
            )
        }
        regra.mainClock.advanceTimeBy(1000)
        regra.runOnIdle {
            dono!!.onBackPressedDispatcher.onBackPressed()
            dono!!.onBackPressedDispatcher.onBackPressed()
        }
        regra.mainClock.advanceTimeBy(500)
        org.junit.Assert.assertEquals("o voltar no treino deveria entregar os resultados", emptyList<Boolean>(), concluiu)
        org.junit.Assert.assertEquals("o treino deve terminar uma vez só", 1, vezes)
        org.junit.Assert.assertFalse("o voltar no treino não pode só sair", saiu)
    }
}
