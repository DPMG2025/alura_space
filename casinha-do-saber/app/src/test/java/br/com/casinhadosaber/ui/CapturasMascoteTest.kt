package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.AcessorioMascote
import br.com.casinhadosaber.dominio.Aniversario
import br.com.casinhadosaber.dominio.Data
import br.com.casinhadosaber.dominio.EspecieMascote
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.EstagioMascote
import br.com.casinhadosaber.dominio.Eventos
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.Mascote
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Surpresas
import br.com.casinhadosaber.dominio.TipoDeEvento
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.componentes.MascoteParado
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaCasa
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.TelaPremio
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
import kotlin.random.Random

/**
 * Capturas do mascote (s2_), da história (s3_) e dos eventos e da surpresa da semana (s4_).
 * Mesmo jeito de CapturasTest: relógio parado, narrador e sons desligados. As datas são fixas
 * (nunca o relógio do computador), então as capturas saem iguais em qualquer dia.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasMascoteTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val menino = Perfil("Leo", Genero.MENINO, pele = 0, cabelo = 3)
    private val gerador = GeradorDeFases(Random(4))

    /** Casa completa, com [vencidas] fases vencidas (o estágio do mascote vem daí) e o [mascote]. */
    private fun estado(vencidas: Int, mascote: Mascote = Mascote(), perfil: Perfil = menina): EstadoJogo {
        var e = EstadoJogo(perfil = perfil)
        for (item in ItensDaCasa.ordem) e = e.colocar(item)
        val estrelas = (0 until vencidas).associate { "${Trilha.entries[it % 4].name}:${it / 4}" to 3 }
        return e.copy(estrelas = estrelas, mascote = mascote)
    }

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

    private fun tocar(tag: String) {
        regra.onAllNodesWithTag(tag).onFirst().performClick()
    }

    // ------------------------------------------------------------------ s2: mascote

    @Test fun escolhaDoMascote() = capturar("s2_01_escolha_do_mascote") {
        TelaCasa(estado(3), {}, {}, {}, perguntarMascote = true)
    }

    @Test fun escolhaDoMascoteMarcada() = capturar("s2_02_escolha_gatinho_marcado", acao = { tocar("mascote_GATINHO") }) {
        TelaCasa(estado(3), {}, {}, {}, perguntarMascote = true)
    }

    @Test fun escolherGuardaOMascote() {
        var e = estado(3)
        montar { TelaCasa(e, { mudanca -> e = mudanca(e) }, {}, {}, perguntarMascote = true) }
        regra.mainClock.advanceTimeBy(1500)
        tocar("mascote_DRAGAOZINHO")
        regra.mainClock.advanceTimeBy(500)
        tocar("mascote_pronto")
        regra.mainClock.advanceTimeBy(500)
        assertEquals(EspecieMascote.DRAGAOZINHO, e.mascote.especie)
        assertTrue("a escolha some depois de pronto", regra.onAllNodesWithTag("mascote_pronto").fetchSemanticsNodes().isEmpty())
    }

    /** Estágios 1, 3 e 5 de cada espécie, lado a lado, do jeito que aparecem na casa (mesmo espaço, tamanhos diferentes). */
    @Composable
    private fun Estagios(especie: EspecieMascote) {
        Column(
            Modifier.fillMaxSize().background(Color(0xFFFFF8E7)).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(especie.nome, style = MaterialTheme.typography.headlineLarge)
            for (st in listOf(EstagioMascote.BEBE, EstagioMascote.CRIANCA, EstagioMascote.CAMPEAO)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    MascoteParado(especie, st, Modifier.size(200.dp), if (st == EstagioMascote.CAMPEAO) AcessorioMascote.COROA else null)
                    Text("${st.ordinal + 1}. ${st.nome}", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }

    @Test fun estagiosCachorrinho() = capturar("s2_10_cachorrinho_estagios_1_3_5") { Estagios(EspecieMascote.CACHORRINHO) }

    @Test fun estagiosGatinho() = capturar("s2_11_gatinho_estagios_1_3_5") { Estagios(EspecieMascote.GATINHO) }

    @Test fun estagiosDragaozinho() = capturar("s2_12_dragaozinho_estagios_1_3_5") { Estagios(EspecieMascote.DRAGAOZINHO) }

    @Test fun casaComCachorrinhoBebe() = capturar("s2_20_casa_cachorrinho_bebe") {
        TelaCasa(estado(2, Mascote(EspecieMascote.CACHORRINHO, "Totó")), {}, {}, {}, perguntarMascote = true)
    }

    @Test fun casaComGatinhoCrianca() = capturar("s2_21_casa_gatinho_crianca") {
        TelaCasa(estado(45, Mascote(EspecieMascote.GATINHO, "", AcessorioMascote.OCULOS.name), menino), {}, {}, {}, perguntarMascote = true)
    }

    @Test fun casaComDragaoCampeao() = capturar("s2_22_casa_dragaozinho_campeao") {
        TelaCasa(estado(250, Mascote(EspecieMascote.DRAGAOZINHO, "Faísca", AcessorioMascote.MEDALHA.name)), {}, {}, {})
    }

    @Test fun cantinhoDoMascote() = capturar(
        "s2_30_cantinho_do_mascote",
        acao = { regra.onAllNodesWithTag("mascote_na_casa").onFirst().performClick() },
    ) {
        TelaCasa(estado(70, Mascote(EspecieMascote.GATINHO, "Mimi", AcessorioMascote.LACINHO.name)), {}, {}, {})
    }

    @Test fun festaDeCrescer() = capturar("s2_40_premio_mascote_cresceu") {
        val depois = estado(40, Mascote(EspecieMascote.CACHORRINHO, "Totó"))
        TelaPremio(gerador.fases(Trilha.MATEMATICA)[5], 3, null, menina, 4, true, {}, {}, {}, estado = depois, fasesAntes = 39)
    }

    @Test fun premioComMascote() = capturar("s2_41_premio_com_mascote", tempo = 4000) {
        val depois = estado(12, Mascote(EspecieMascote.GATINHO, "Mimi"))
        TelaPremio(gerador.fases(Trilha.INGLES)[2], 3, ItensDaCasa.porId.getValue("cama"), menina, 5, true, {}, {}, {}, estado = depois, fasesAntes = 12)
    }

    // ------------------------------------------------------------------ s3: história

    @Test fun aberturaDoJardim() = capturar("s3_01_abertura_jardim_numeros") {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[0], menina, {}, {}, comHistoria = true)
    }

    @Test fun aberturaDoEspacoIngles() = capturar("s3_02_abertura_espaco_ingles") {
        TelaJogo(gerador.fases(Trilha.INGLES)[40], menino, {}, {}, comHistoria = true)
    }

    @Test fun pularAbertura() = capturar("s3_03_abertura_pulada_comeca_o_jogo", acao = { tocar("historia_continuar") }) {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[10], menina, {}, {}, comHistoria = true)
    }

    @Test fun cenaDoChefao() = capturar("s3_04_chefao_vencido") {
        // Sem mascote: só a cena do chefão (a festa do mascote viria depois dela).
        val depois = estado(10)
        TelaPremio(gerador.fases(Trilha.LOGICA)[9], 3, null, menino, 6, true, {}, {}, {}, estado = depois, fasesAntes = 9)
    }

    // ------------------------------------------------------------------ s4: eventos e surpresa da semana

    private fun casaNoDia(nome: String, data: Data, perfil: Perfil = menina, tipo: TipoDeEvento? = null) = capturar(nome) {
        if (tipo != null) assertEquals(tipo, Eventos.principal(data, perfil.aniversario)?.tipo)
        TelaCasa(estado(20, Mascote(EspecieMascote.CACHORRINHO), perfil), {}, {}, {}, hoje = data)
    }

    @Test fun natal() = casaNoDia("s4_01_natal", Data(2026, 12, 20), tipo = TipoDeEvento.NATAL)

    @Test fun pascoa() = casaNoDia("s4_02_pascoa", Data(2027, 3, 28), tipo = TipoDeEvento.PASCOA)

    @Test fun festaJunina() = casaNoDia("s4_03_festa_junina", Data(2027, 6, 15), tipo = TipoDeEvento.FESTA_JUNINA)

    @Test fun diaDasCriancas() = casaNoDia("s4_04_dia_das_criancas", Data(2027, 10, 11), menino, TipoDeEvento.DIA_DAS_CRIANCAS)

    @Test fun aniversario() =
        casaNoDia("s4_05_aniversario", Data(2027, 8, 5), menina.copy(aniversario = Aniversario(5, 8)), TipoDeEvento.ANIVERSARIO)

    @Test fun visitaDaSemanaEPresentes() = capturar("s4_06_visita_da_semana_e_presentes") {
        // Dia comum: o visitante da semana passeia no jardim e os presentes de eventos antigos ficam na prateleira.
        val e = estado(20).copy(presentesEspeciais = setOf("natal_estrela", "pascoa_pintinho", "junina_milho", "criancas_pipa"))
        TelaCasa(e, {}, {}, {}, hoje = Data(2027, 2, 3))
    }

    @Test fun lojaComNovidadesDaSemana() {
        val hoje = Data(2026, 11, 23)
        assertEquals(4, Surpresas.liberadas(hoje).size)
        capturar("s4_07_loja_com_visitantes_da_semana") {
            // Tudo da loja de sempre já comprado: os bichinhos da semana aparecem primeiro na vitrine.
            val comprados = Loja.produtos.filter { it.categoria in Loja.categoriasDeFora }.map { it.id }.toSet()
            TelaCasa(estado(20).copy(moedas = 30, comprados = comprados), {}, {}, {}, lojaInicial = true, hoje = hoje)
        }
    }
}
