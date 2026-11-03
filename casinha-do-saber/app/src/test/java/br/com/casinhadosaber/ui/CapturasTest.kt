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

    /**
     * Arrasta a figura [indice] da cesta (contar) para dentro da cesta. O alvo fica um pouco à
     * esquerda do botão "certinho", que fica colado ao lado direito da cesta, na altura do meio dela.
     */
    private fun arrastarParaACesta(indice: Int) {
        val pronto = regra.onNodeWithTag("atividade_pronto").fetchSemanticsNode().boundsInRoot
        val figura = regra.onAllNodesWithTag("figura_certa")[indice]
        val origem = figura.fetchSemanticsNode().boundsInRoot
        val recuo = with(regra.density) { 40.dp.toPx() }
        val alvo = Offset(pronto.left - recuo, pronto.center.y) - origem.topLeft
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
        val referencia = cenas[0]
        for (c in 1..2) {
            val img = cenas[c]
            var dentro = 0
            var fora = 0
            for (y in 0 until h) for (x in 0 until w) {
                if (img.getPixel(x, y) == referencia.getPixel(x, y)) continue
                if (dentroDasParedes(x, y) || dentroDoTelhado(x, y)) dentro++ else fora++
            }
            // Sem isto o teste passaria à toa se o clima não fosse desenhado.
            org.junit.Assert.assertTrue("clima $c não desenhou nada do lado de fora", fora > 50)
            org.junit.Assert.assertEquals("clima $c desenhou dentro de casa", 0, dentro)
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
        regra.onNodeWithTag("atividade_pronto").assertIsNotEnabled()

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

    @Test fun casaComEspelhoEPorta() = capturar("37_casa_espelho_e_porta") {
        TelaCasa(estado(40, 40), {}, {}, {}, onEspelho = {}, onEntrar = {})
    }

    private fun abrirPresente() {
        repeat(3) {
            regra.onAllNodes(hasContentDescription("Presente. Toque para abrir.")).onFirst().performClick()
            regra.mainClock.advanceTimeBy(400)
        }
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
}
