package br.com.casinhadosaber.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Domino
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.LigarPares
import br.com.casinhadosaber.dominio.MeiaPeca
import br.com.casinhadosaber.dominio.Opcao
import br.com.casinhadosaber.dominio.PecaDomino
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.QuebraCabeca
import br.com.casinhadosaber.dominio.Trem
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.Visual
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaJogo
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.random.Random

/**
 * Capturas e testes de jogar até o fim de Ligar pares, Quebra cabeça, Trem e Dominó (prefixo n2_).
 * Cada atividade é concluída arrastando pelas marcas de teste:
 *  - ligar_esquerda_i e ligar_direita_j (o dedo vai de um cartão ao outro);
 *  - quebra_peca_i até quebra_lugar_i (i é a casinha da peça na moldura);
 *  - trem_vagao_i até trem_engate_i (trem_extra_k são os vagões que sobram);
 *  - domino_peca_i (índice na mão) até domino_alvo, na ordem de Domino.ordem.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasNovas2Test {

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

    /** Arrasta com o dedo do centro da marca [de] até o centro da marca [para] (em coordenadas da tela). */
    private fun arrastar(de: String, para: String) {
        val origem = regra.onNodeWithTag(de).fetchSemanticsNode().boundsInRoot.center
        val destino = regra.onNodeWithTag(para).fetchSemanticsNode().boundsInRoot.center
        regra.onRoot().performTouchInput { swipe(origem, destino, durationMillis = 450) }
        regra.mainClock.advanceTimeBy(1200)
    }

    private fun deMexer(instrucao: String, atividade: Atividade) =
        Pergunta.deAtividade(instrucao, listOf(pt(instrucao)), atividade, listOf(pt("Muito bem!")))

    /** Joga a pergunta com [jogar] e confere o resultado da fase (um item: acertou de primeira?). */
    private fun jogarAteOFim(pergunta: Pergunta, esperado: Boolean, foto: String? = null, jogar: () -> Unit) {
        var resultado: List<Boolean>? = null
        montar { TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, { resultado = it }, perguntasProntas = listOf(pergunta)) }
        regra.mainClock.advanceTimeBy(1500)
        jogar()
        if (foto != null) foto(foto)
        regra.mainClock.advanceTimeBy(8000)
        assertEquals("a atividade deveria terminar com acertou de primeira = $esperado", listOf(esperado), resultado)
    }

    private fun texto(t: String) = Opcao(Visual.Texto(t), listOf(pt(t.lowercase())))
    private fun emoji(e: String, fala: String) = Opcao(Visual.Emoji(e), listOf(pt(fala)))

    // ------------------------------------------------------------------ ligar pares

    private val ligarNumeros = LigarPares(
        esquerda = listOf(texto("1"), texto("2"), texto("3")),
        direita = listOf(emoji("🍎🍎🍎", "três"), emoji("🍎", "uma"), emoji("🍎🍎🍎🍎", "quatro"), emoji("🍎🍎", "duas")),
        pares = mapOf(0 to 1, 1 to 3, 2 to 0),
    )
    private val ligarLetras = LigarPares(
        esquerda = listOf(texto("A"), texto("B"), texto("C"), texto("D")),
        direita = listOf(texto("b"), texto("d"), texto("a"), texto("c")),
        pares = mapOf(0 to 2, 1 to 0, 2 to 3, 3 to 1),
    )

    @Test fun ligarParesInicio() {
        montar { TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, {}, perguntasProntas = listOf(deMexer("Ligue o número à quantidade", ligarNumeros))) }
        regra.mainClock.advanceTimeBy(4500)
        foto("n2_01_ligar_pares_inicio")
    }

    @Test fun ligarParesMeio() {
        montar { TelaJogo(gerador.fases(Trilha.PORTUGUES)[3], menino, {}, {}, perguntasProntas = listOf(deMexer("Ligue a letra grande à pequena", ligarLetras))) }
        regra.mainClock.advanceTimeBy(1500)
        arrastar("ligar_esquerda_0", "ligar_direita_2")
        arrastar("ligar_direita_0", "ligar_esquerda_1")
        foto("n2_02_ligar_pares_dois_ligados")
    }

    /** Dois pares errados: a linha some, e o par certo passa a brilhar com a mãozinha ligando. */
    @Test fun ligarParesAjuda() {
        var resultado: List<Boolean>? = null
        montar { TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, { resultado = it }, perguntasProntas = listOf(deMexer("Ligue o número à quantidade", ligarNumeros))) }
        regra.mainClock.advanceTimeBy(1500)
        arrastar("ligar_esquerda_0", "ligar_direita_0")
        arrastar("ligar_esquerda_0", "ligar_direita_2")
        regra.mainClock.advanceTimeBy(3500)
        foto("n2_03_ligar_pares_ajuda")
        for ((i, j) in ligarNumeros.pares) arrastar("ligar_esquerda_$i", "ligar_direita_$j")
        regra.mainClock.advanceTimeBy(8000)
        assertEquals(listOf(false), resultado)
    }

    @Test fun ligarParesAteOFim() = jogarAteOFim(deMexer("Ligue a letra grande à pequena", ligarLetras), true, "n2_04_ligar_pares_pronto") {
        for ((i, j) in ligarLetras.pares) arrastar("ligar_esquerda_$i", "ligar_direita_$j")
    }

    // ------------------------------------------------------------------ quebra cabeça

    private val quebraNumeros = QuebraCabeca("🦁", 2, 3, rotulos = listOf("1", "2", "3", "4", "5", "6"))
    private val quebraFixas = QuebraCabeca("🐢", 3, 3, fixas = setOf(0, 4))
    private val quebraGrande = QuebraCabeca("🚀", 4, 4, rotulos = (1..16).map { (it * 10).toString() })

    @Test fun quebraCabecaInicio() {
        montar { TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menino, {}, {}, perguntasProntas = listOf(deMexer("Monte o leão: 1, 2, 3...", quebraNumeros))) }
        regra.mainClock.advanceTimeBy(4500)
        foto("n2_05_quebra_cabeca_inicio")
    }

    @Test fun quebraCabecaMeio() {
        montar { TelaJogo(gerador.fases(Trilha.LOGICA)[3], menina, {}, {}, perguntasProntas = listOf(deMexer("Monte a tartaruga", quebraFixas))) }
        regra.mainClock.advanceTimeBy(1500)
        arrastar("quebra_peca_1", "quebra_lugar_1")
        arrastar("quebra_peca_2", "quebra_lugar_2")
        arrastar("quebra_peca_3", "quebra_lugar_3")
        // Uma peça na casinha errada: volta tremendo (um erro).
        arrastar("quebra_peca_5", "quebra_lugar_8")
        foto("n2_06_quebra_cabeca_meio")
    }

    @Test fun quebraCabecaGrande() {
        montar { TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, {}, perguntasProntas = listOf(deMexer("Monte o foguete: 10, 20, 30...", quebraGrande))) }
        regra.mainClock.advanceTimeBy(1500)
        arrastar("quebra_peca_0", "quebra_lugar_1")
        arrastar("quebra_peca_0", "quebra_lugar_2")
        regra.mainClock.advanceTimeBy(3500)
        foto("n2_07_quebra_cabeca_16_ajuda")
    }

    @Test fun quebraCabecaAteOFim() = jogarAteOFim(deMexer("Monte o leão", quebraNumeros), true, "n2_08_quebra_cabeca_pronto") {
        for (i in 0 until quebraNumeros.pecas) arrastar("quebra_peca_$i", "quebra_lugar_$i")
    }

    // ------------------------------------------------------------------ trem

    private val tremNumeros = Trem(
        vagoes = listOf("2", "4", "6", "8", "10").map { texto(it) },
        fixos = setOf(0),
        extras = listOf(texto("7")),
    )
    private val tremDias = Trem(
        vagoes = listOf("DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB").map { texto(it) },
        fixos = setOf(0, 3),
    )
    private val tremTamanhos = Trem(
        vagoes = listOf(0.5f, 0.75f, 1f, 1.25f).map { Opcao(Visual.Emoji("🐘", it), listOf(pt("elefante"))) },
    )

    @Test fun tremInicio() {
        montar { TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menino, {}, {}, perguntasProntas = listOf(deMexer("Conte de 2 em 2", tremNumeros))) }
        regra.mainClock.advanceTimeBy(4500)
        foto("n2_09_trem_inicio")
    }

    @Test fun tremSeteVagoes() {
        montar { TelaJogo(gerador.fases(Trilha.PORTUGUES)[3], menina, {}, {}, perguntasProntas = listOf(deMexer("Os dias da semana", tremDias))) }
        regra.mainClock.advanceTimeBy(1500)
        arrastar("trem_vagao_1", "trem_engate_1")
        arrastar("trem_vagao_5", "trem_engate_2")
        foto("n2_10_trem_sete_vagoes")
    }

    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    @Test fun tremCelularPequeno() {
        montar { TelaJogo(gerador.fases(Trilha.LOGICA)[3], menino, {}, {}, perguntasProntas = listOf(deMexer("Do menor para o maior", tremTamanhos))) }
        regra.mainClock.advanceTimeBy(4500)
        foto("n2_11_trem_celular_pequeno")
    }

    @Test fun tremAteOFim() = jogarAteOFim(deMexer("Conte de 2 em 2", tremNumeros), true) {
        for (i in 1 until tremNumeros.vagoes.size) arrastar("trem_vagao_$i", "trem_engate_$i")
        // O trem apita e sai andando.
        regra.mainClock.advanceTimeBy(500)
        foto("n2_12_trem_saindo")
    }

    @Test fun tremComErroNaoEhDePrimeira() = jogarAteOFim(deMexer("Conte de 2 em 2", tremNumeros), false) {
        arrastar("trem_extra_0", "trem_engate_2")
        for (i in 1 until tremNumeros.vagoes.size) arrastar("trem_vagao_$i", "trem_engate_$i")
    }

    // ------------------------------------------------------------------ dominó

    /** Fileira: 2 → [🍎🍎 | 3] → [⭐⭐⭐ | 1] (entra virada) → [🐟 | 4]; a peça das flores sobra. */
    private val domino = Domino(
        ponta = MeiaPeca("2", Visual.Texto("2"), listOf(pt("dois"))),
        mao = listOf(
            PecaDomino(MeiaPeca("2", Visual.Emoji("🍎🍎"), listOf(pt("duas maçãs"))), MeiaPeca("3", Visual.Texto("3"), listOf(pt("três")))),
            PecaDomino(MeiaPeca("1", Visual.Texto("1"), listOf(pt("um"))), MeiaPeca("3", Visual.Emoji("⭐⭐⭐"), listOf(pt("três estrelas")))),
            PecaDomino(MeiaPeca("1", Visual.Emoji("🐟"), listOf(pt("um peixe"))), MeiaPeca("4", Visual.Texto("4"), listOf(pt("quatro")))),
            PecaDomino(MeiaPeca("4", Visual.Emoji("🌸🌸🌸🌸"), listOf(pt("quatro flores"))), MeiaPeca("5", Visual.Texto("5"), listOf(pt("cinco")))),
        ),
        jogadas = 3,
    )

    @Test fun dominoInicio() {
        montar { TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, {}, perguntasProntas = listOf(deMexer("Qual peça combina com o 2?", domino))) }
        regra.mainClock.advanceTimeBy(4500)
        foto("n2_13_domino_inicio")
    }

    @Test fun dominoPecaVirada() {
        montar { TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menino, {}, {}, perguntasProntas = listOf(deMexer("Qual peça combina?", domino))) }
        regra.mainClock.advanceTimeBy(1500)
        arrastar("domino_peca_0", "domino_alvo")
        arrastar("domino_peca_1", "domino_alvo")
        foto("n2_14_domino_virada")
    }

    @Test fun dominoAjuda() {
        montar { TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, {}, perguntasProntas = listOf(deMexer("Qual peça combina?", domino))) }
        regra.mainClock.advanceTimeBy(1500)
        arrastar("domino_peca_3", "domino_alvo")
        arrastar("domino_peca_2", "domino_alvo")
        regra.mainClock.advanceTimeBy(3500)
        foto("n2_15_domino_ajuda")
    }

    @Test fun dominoAteOFim() = jogarAteOFim(deMexer("Qual peça combina?", domino), true, "n2_16_domino_pronto") {
        for (i in domino.ordem) arrastar("domino_peca_$i", "domino_alvo")
    }
}
