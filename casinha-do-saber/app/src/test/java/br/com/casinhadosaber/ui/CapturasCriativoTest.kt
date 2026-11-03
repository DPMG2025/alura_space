package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Conquistas
import br.com.casinhadosaber.dominio.Diploma
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Piano
import br.com.casinhadosaber.dominio.Pintura
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.alternarNaParede
import br.com.casinhadosaber.dominio.salvarDesenho
import br.com.casinhadosaber.ui.componentes.CantinhoDaSala
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.AvisoDeConquistas
import br.com.casinhadosaber.ui.telas.NovasConquistas
import br.com.casinhadosaber.ui.telas.TelaAlbum
import br.com.casinhadosaber.ui.telas.TelaAtelie
import br.com.casinhadosaber.ui.telas.TelaCasaPorDentro
import br.com.casinhadosaber.ui.telas.TelaDiplomas
import br.com.casinhadosaber.ui.telas.TelaMapa
import br.com.casinhadosaber.ui.telas.TelaPiano
import br.com.casinhadosaber.ui.telas.TelaTrilha
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Capturas do modo criativo (prefixo s5_: ateliê, piano e quadros na sala) e do domínio visível (s6_: mapa dos
 * mundos, diplomas, álbum e o aviso de conquista). Mesmo jeito de CapturasTest: relógio parado, narrador e sons
 * desligados, progresso semeado direto nas estrelas.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasCriativoTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)

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

    private fun capturar(nome: String, tempo: Long = 2500, acao: (() -> Unit)? = null, conteudo: @Composable () -> Unit) {
        montar(conteudo)
        regra.mainClock.advanceTimeBy(tempo)
        if (acao != null) {
            acao()
            regra.mainClock.advanceTimeBy(1200)
        }
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    // ------------------------------------------------------------------ progresso semeado

    /**
     * Estrelas semeadas: em Números os mundos 1 e 2 completos (o 1 todo com 3 estrelas) e o 3 pela metade;
     * em Letras o mundo 1 completo; em Inglês e Lógica algumas fases. A casa fica montada com 40 coisas.
     */
    private fun progresso(): EstadoJogo {
        val estrelas = HashMap<String, Int>()
        for (n in 0 until 25) estrelas[EstadoJogo.chave(Trilha.MATEMATICA, n)] = if (n < 10) 3 else 1 + n % 3
        for (n in 0 until 10) estrelas[EstadoJogo.chave(Trilha.PORTUGUES, n)] = if (n % 2 == 0) 3 else 2
        for (n in 0 until 4) estrelas[EstadoJogo.chave(Trilha.INGLES, n)] = 3
        for (n in 0 until 3) estrelas[EstadoJogo.chave(Trilha.LOGICA, n)] = 2
        var e = EstadoJogo(perfil = menina, estrelas = estrelas)
        for (item in ItensDaCasa.ordem.take(40)) e = e.colocar(item)
        return e
    }

    /** Desenho a partir de 12 linhas de 12 caracteres (o índice da cor de Atelie.paleta, em base 36). */
    private fun desenho(id: String, vararg linhas: String): Pintura =
        Pintura(id, linhas.joinToString("").map { "0123456789ab".indexOf(it) })

    private val casinha = desenho(
        "d1",
        "bbbbbbbbbbbb",
        "bbbbb22bbbbb",
        "bbbb2222bbbb",
        "bbb222222bbb",
        "bb22222222bb",
        "bb44444444bb",
        "bb44664444bb",
        "bb44664499bb",
        "bb44444499bb",
        "bb44444499bb",
        "555555555555",
        "555555555555",
    )
    private val sol = desenho(
        "d2",
        "000000000000",
        "040004000400",
        "004044404000",
        "000444444000",
        "044443344440",
        "000443344000",
        "000444444000",
        "004044404000",
        "040004000400",
        "000000000000",
        "666666666666",
        "666666666666",
    )
    private val flor = desenho(
        "d3",
        "000000000000",
        "000088880000",
        "000884488000",
        "000844448000",
        "000884488000",
        "000088880000",
        "000000500000",
        "000550500000",
        "000055500000",
        "000000500000",
        "999999999999",
        "999999999999",
    )
    private val arcoIris = desenho(
        "d4",
        "000000000000",
        "000222222000",
        "002233332200",
        "022344443220",
        "023455554320",
        "234566665432",
        "234560065432",
        "234500005432",
        "234000000432",
        "230000000032",
        "000000000000",
        "000000000000",
    )

    private fun comDesenhos(e: EstadoJogo): EstadoJogo {
        var r = e
        for (d in listOf(casinha, sol, flor, arcoIris)) r = r.salvarDesenho(d)!!
        return r.alternarNaParede("d1").alternarNaParede("d2").alternarNaParede("d3")
    }

    // ------------------------------------------------------------------ s5: modo criativo

    @Test fun atelieVazio() = capturar("s5_atelie_papel_novo") {
        TelaAtelie(EstadoJogo(perfil = menina), {}, {})
    }

    @Test fun atelieComDesenhos() = capturar("s5_atelie_galeria") {
        TelaAtelie(comDesenhos(progresso()), {}, {}, abrirDesenho = "d1")
    }

    /** Arrastar o dedo no quadro pinta e o desenho fica guardado sozinho (sem botão de salvar). */
    @Test fun atelieArrastarPintaEGuarda() {
        var estado by mutableStateOf(EstadoJogo(perfil = menina))
        montar { TelaAtelie(estado, { f -> estado = f(estado) }, {}) }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithTag("cor_6").performClick()
        regra.onNodeWithTag("quadro_de_pintar").performTouchInput {
            swipe(Offset(width * 0.1f, height * 0.5f), Offset(width * 0.9f, height * 0.5f), durationMillis = 500)
        }
        regra.mainClock.advanceTimeBy(600)
        regra.onNodeWithTag("quadro_de_pintar").performTouchInput {
            swipe(Offset(width * 0.5f, height * 0.1f), Offset(width * 0.5f, height * 0.9f), durationMillis = 500)
        }
        regra.mainClock.advanceTimeBy(1200)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s5_atelie_pintando.png")
        assertEquals("o desenho deveria ficar guardado sozinho", 1, estado.desenhos.size)
        val d = estado.desenhos.single()
        // Uma linha inteira de azul, sem buracos (o dedo arrastado vira traço).
        val linha = (0 until Pintura.LADO).map { d.cor(6, it) }
        assertTrue("o traço deveria atravessar o quadro: $linha", linha.count { it == 6 } >= 10)
    }

    @Test fun pianoLivre() = capturar("s5_piano_livre") {
        TelaPiano(onVoltar = {})
    }

    @Test fun pianoSeguindoAMusica() = capturar("s5_piano_seguir_musica") {
        TelaPiano(onVoltar = {}, musicaInicial = "brilha", posicaoInicial = 2)
    }

    /** Tocar a tecla que brilha avança a música; tocar outra não muda nada (nunca é erro). */
    @Test fun pianoSeguirAvancaSoNaTeclaCerta() {
        montar { TelaPiano(onVoltar = {}, musicaInicial = "escala") }
        regra.mainClock.advanceTimeBy(1000)
        val escala = Piano.porId.getValue("escala")
        // Tecla errada: continua esperando o dó.
        regra.onNodeWithTag("tecla_mi").performSemanticsAction(SemanticsActions.OnClick)
        for (n in escala.notas) {
            regra.onNodeWithTag("tecla_${n.nota.name.lowercase()}").performSemanticsAction(SemanticsActions.OnClick)
            regra.mainClock.advanceTimeBy(100)
        }
        regra.mainClock.advanceTimeBy(1500)
        regra.onNodeWithText("Você tocou", substring = true).assertExists()
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/s5_piano_musica_terminada.png")
    }

    @Test fun quadrosNaSala() = capturar("s5_casa_quadros_na_parede") {
        TelaCasaPorDentro(comDesenhos(progresso()), {}, {}, cantinho = CantinhoDaSala({}, {}, {}, {}))
    }

    // ------------------------------------------------------------------ s6: mapa, diplomas e álbum

    @Test fun caminhoComBotaoDoMapa() = capturar("s6_caminho_com_mapa") {
        TelaTrilha(Trilha.MATEMATICA, progresso(), {}, {}, onMapa = {})
    }

    @Test fun mapaDosMundos() = capturar("s6_mapa_numeros") {
        TelaMapa(Trilha.MATEMATICA, progresso(), {}, {})
    }

    @Test fun mapaComeco() = capturar("s6_mapa_comeco") {
        TelaMapa(Trilha.LOGICA, EstadoJogo(perfil = menina), {}, {})
    }

    @Test fun diplomasVazio() = capturar("s6_diplomas_vazio") {
        TelaDiplomas(EstadoJogo(perfil = menina, estrelas = mapOf(EstadoJogo.chave(Trilha.INGLES, 0) to 3)), {})
    }

    @Test fun diplomas() = capturar("s6_diplomas") {
        val e = progresso()
        assertEquals(3, Conquistas.diplomas(e.estrelas).size)
        TelaDiplomas(e, {})
    }

    @Test fun diplomaAberto() = capturar("s6_diploma_aberto") {
        TelaDiplomas(progresso(), {}, abrirDiploma = Diploma(Trilha.MATEMATICA, 1))
    }

    @Test fun albumPaginaCompleta() = capturar("s6_album_pagina_completa") {
        TelaAlbum(progresso(), Trilha.MATEMATICA, 0, onVoltar = {})
    }

    @Test fun albumComSilhuetas() = capturar(
        "s6_album_silhuetas",
        acao = { regra.onAllNodesWithTag("figurinha_falta").onFirst().performClick() },
    ) {
        TelaAlbum(progresso(), Trilha.MATEMATICA, 1, onVoltar = {})
    }

    @Test fun avisoDeConquista() = capturar("s6_aviso_figurinha_e_diploma") {
        val antes = progresso().estrelas
        val depois = antes + (EstadoJogo.chave(Trilha.PORTUGUES, 10) to 3)
        val novas = NovasConquistas.entre(antes - EstadoJogo.chave(Trilha.PORTUGUES, 9), depois)
        assertTrue(novas.diplomas.isNotEmpty() && novas.figurinhas.isNotEmpty())
        Box(Modifier.fillMaxSize().background(Color(0xFF8FD3FF))) {
            AvisoDeConquistas(novas, "Ana", {}, {}, Modifier.align(Alignment.BottomCenter))
        }
    }

    /** O álbum vem só das estrelas: o mundo 1 de Números (10 fases com 3 estrelas) tem as 11 figurinhas. */
    @Test fun albumContaAsFigurinhasDasEstrelas() {
        val pagina = Conquistas.album(Trilha.MATEMATICA, progresso().estrelas)[0]
        assertTrue(pagina.completa)
        assertEquals(Mundos.FASES_POR_MUNDO + 1, pagina.quantasGanhas)
    }
}
