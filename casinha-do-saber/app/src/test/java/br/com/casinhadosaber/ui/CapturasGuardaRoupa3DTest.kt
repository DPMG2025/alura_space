package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.ParteDoCorpo
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.CamadasDosPersonagens
import br.com.casinhadosaber.ui.componentes.ComposicaoDosPersonagens
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.PersonagemDaArte
import br.com.casinhadosaber.ui.componentes.PoseDosBracos
import br.com.casinhadosaber.ui.componentes.QuadroDoPersonagem
import br.com.casinhadosaber.ui.tema.TemaCasinha
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Guarda-roupa em camadas 3D (Fase 7 do PRD visual 3D): os 16 trajes, os 18 acessórios e os chapéus vestidos
 * na criança pelas camadas (`roupa_*`, `acessorio_*`, `chapeu_*`), em miniaturas paradas (`animado = false`).
 * Antes de cada captura confere que a criança vestida continua nas camadas e que a peça aparece mesmo (o
 * composto com a peça difere do composto sem ela). Capturas novas: v3d_08, v3d_09 e v3d_10.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasGuardaRoupa3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val parado = QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ)

    /** Pixels (de um a cada dois) em que a criança com a peça difere da criança [sem] ela, num composto pequeno. */
    private fun diferenca(com: Perfil, sem: Perfil): Int {
        val app = RuntimeEnvironment.getApplication()
        val manifesto = ArteDoApp.manifesto(app)
        fun composto(p: Perfil): android.graphics.Bitmap {
            val camadas = CamadasDosPersonagens.camadas(manifesto, PersonagemDaArte.CRIANCA, p, parado)
            assertNotNull("faltam camadas para $p", camadas)
            val img = ComposicaoDosPersonagens.composto(app, camadas!!, p, 85, 130)
            assertNotNull("a criança $p não foi composta (camada ilegível?)", img)
            return img!!.asAndroidBitmap()
        }
        val a = composto(com)
        val b = composto(sem)
        var n = 0
        for (y in 0 until a.height step 2) for (x in 0 until a.width step 2) {
            val c1 = a.getPixel(x, y)
            val c2 = b.getPixel(x, y)
            val d = kotlin.math.abs(android.graphics.Color.red(c1) - android.graphics.Color.red(c2)) +
                kotlin.math.abs(android.graphics.Color.green(c1) - android.graphics.Color.green(c2)) +
                kotlin.math.abs(android.graphics.Color.blue(c1) - android.graphics.Color.blue(c2)) +
                kotlin.math.abs(android.graphics.Color.alpha(c1) - android.graphics.Color.alpha(c2))
            if (d > 60) n++
        }
        return n
    }

    /** Confere que cada criança usa as camadas e que a peça aparece (mais de [minimo] pixels diferentes). */
    private fun confere(vestidas: List<Pair<String, Perfil>>, semAPeca: (Perfil) -> Perfil, minimo: Int) {
        val app = RuntimeEnvironment.getApplication()
        for ((nome, p) in vestidas) {
            assertTrue("$nome devia usar as camadas 3D", ComposicaoDosPersonagens.podeUsar(app, PersonagemDaArte.CRIANCA, p))
            val d = diferenca(p, semAPeca(p))
            assertTrue("$nome quase não aparece na criança ($d pixels diferentes)", d > minimo)
        }
    }

    /** Uma folha com um título e as crianças em linhas (altura da criança e quem vai nela), cada uma com o nome embaixo. */
    private fun capturar(nome: String, titulo: String, linhas: List<Pair<Dp, List<Triple<String, Perfil, Humor>>>>) {
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF7FC8FF), Color(0xFFBFE3FF), Color(0xFFFFD6C4), Color(0xFFFFC2DC)))),
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(titulo, fontSize = 22.sp, color = Color(0xFF3A2766))
                        for ((altura, criancas) in linhas) {
                            // Cada criança numa coluna de mesma largura (a linha inteira), para o nome ter espaço.
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                                for ((rotulo, p, humor) in criancas) Miniatura(rotulo, p, humor, altura, Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(500)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/$nome.png")
    }

    @Composable
    private fun Miniatura(rotulo: String, p: Perfil, humor: Humor, altura: Dp, modifier: Modifier) {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            Crianca(p, Modifier.height(altura), humor = humor, animado = false)
            // Nomes longos ("Fantasia de dinossauro") em até duas linhas, centralizados, sem reticências e com
            // folga dos lados para não encostar no vizinho.
            Text(
                rotulo,
                Modifier.padding(horizontal = 2.dp),
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = Color(0xFF3A2766),
                maxLines = 2,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
            )
        }
    }

    /** Crianças variadas (gênero, pele, cabelo, estilo e cor da roupa), como as prévias de `arte3d/previa_fase7.js`. */
    private fun crianca(i: Int, menina: Boolean = i % 2 == 0) =
        Perfil("", if (menina) Genero.MENINA else Genero.MENINO, pele = i % 4, cabelo = (i * 3) % 4, estiloCabelo = (i / 2) % 3, roupa = i % 6)

    /** Os 16 trajes, cada um numa criança diferente (os dois gêneros, as quatro peles). */
    @Test fun guardaRoupaTrajes() {
        val vestidas = Loja.trajesComArte3d.mapIndexed { i, t -> t.nome to crianca(i).copy(traje = t.id) }
        confere(vestidas, { it.copy(traje = "") }, minimo = 60)
        val linhas = vestidas.chunked(4).map { linha -> 136.dp to linha.map { (n, p) -> Triple(n, p, Humor.FELIZ) } }
        capturar("v3d_08_guarda_roupa", "Guarda-roupa: 16 roupas", linhas)
    }

    /** Os 18 acessórios sozinhos e três crianças com um de cada parte (uma torcendo: o objeto da mão sobe). */
    @Test fun guardaRoupaAcessorios() {
        val sozinhos = Loja.acessoriosComArte3d.mapIndexed { i, a -> a.nome to crianca(i + 1).copy(acessorios = mapOf(a.parte to a.id)) }
        // Mínimo baixo: a gravata borboleta é pequenina (uns 9 pixels de amostra no composto de 85 x 130).
        confere(sozinhos, { it.copy(acessorios = emptyMap()) }, minimo = 4)
        val combinados = (0 until 3).map { k ->
            val p = crianca(k + 5, menina = k == 1).copy(
                acessorios = ParteDoCorpo.entries.associateWith { parte -> Loja.acessoriosComArte3d.filter { it.parte == parte }[k].id },
                traje = listOf("", "fada", "medico")[k],
            )
            Triple(listOf("um de cada", "fada + um de cada", "médico + um de cada")[k], p, if (k == 2) Humor.TORCENDO else Humor.FELIZ)
        }
        val linhas = listOf(160.dp to combinados) +
            sozinhos.chunked(6).map { linha -> 88.dp to linha.map { (n, p) -> Triple(n, p, Humor.FELIZ) } }
        capturar("v3d_09_acessorios", "Acessórios", linhas)
    }

    /** Os 4 chapéus em meninas e meninos com cabelos diferentes, e chapéus por cima de trajes que têm cabeça. */
    @Test fun guardaRoupaChapeus() {
        val chapeus = CamadasDosPersonagens.CAMADAS_DOS_CHAPEUS.keys.toList()
        val nomes = Loja.produtos.filter { it.categoria == br.com.casinhadosaber.dominio.Categoria.CHAPEU }.associate { it.valor to it.nome }
        val meninas = chapeus.mapIndexed { i, c -> nomes.getValue(c) to crianca(i, menina = true).copy(estiloCabelo = i % 3, chapeu = c) }
        val meninos = chapeus.mapIndexed { i, c -> nomes.getValue(c) to crianca(i + 2, menina = false).copy(estiloCabelo = (i + 1) % 3, chapeu = c) }
        // Com traje: o chapéu toma o lugar da cabeça do traje (capacete, touca, bandana, capuz).
        val comTraje = listOf("astronauta" to "👑", "chef" to "🧢", "pirata" to "🎩", "dinossauro" to "🌸").mapIndexed { i, (t, c) ->
            "$t + ${nomes.getValue(c)}" to crianca(i + 1).copy(traje = t, chapeu = c)
        }
        confere(meninas + meninos, { it.copy(chapeu = "") }, minimo = 20)
        confere(comTraje, { it.copy(chapeu = "") }, minimo = 20)
        val linhas = listOf(meninas, meninos, comTraje).map { linha -> 136.dp to linha.map { (n, p) -> Triple(n, p, Humor.FELIZ) } }
        capturar("v3d_10_chapeus", "Chapéus", linhas)
    }
}
