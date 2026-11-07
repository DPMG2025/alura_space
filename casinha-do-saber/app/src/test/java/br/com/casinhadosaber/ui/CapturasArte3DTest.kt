package br.com.casinhadosaber.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.componentes.ArteDoApp
import br.com.casinhadosaber.ui.componentes.CamadasDosPersonagens
import br.com.casinhadosaber.ui.componentes.ComposicaoDosPersonagens
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Humor
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.Irmaozinho
import br.com.casinhadosaber.ui.componentes.PROPORCAO_CRIANCA
import br.com.casinhadosaber.ui.componentes.PersonagemDaArte
import br.com.casinhadosaber.ui.componentes.PoseDosBracos
import br.com.casinhadosaber.ui.componentes.QuadroDoPersonagem
import br.com.casinhadosaber.ui.componentes.desenharPersonagemEmCamadas
import br.com.casinhadosaber.ui.tema.TemaCasinha
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
 * Arte 3D pré-renderizada (Fase 0 do PRD visual 3D): as imagens de teste de `assets/arte/` aparecem
 * numa captura, no tamanho de uso e reduzidas, sobre o céu em degradê das maquetes.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w393dp-h852dp-xhdpi")
class CapturasArte3DTest {

    @get:Rule
    val regra = createComposeRule()

    private val ids = listOf("estrela3d", "moeda", "patinho")

    @Test fun arteDeTeste() {
        // O manifesto e os WebP com alfa são lidos dos assets do app (falha aqui = arte não empacotada ou WebP não decodificado).
        val contexto = RuntimeEnvironment.getApplication()
        for (id in ids) {
            val info = ArteDoApp.info(contexto, id)
            assertNotNull("'$id' fora do manifesto de assets/arte", info)
            val bmp = ArteDoApp.bitmap(contexto, info!!, 1)
            assertNotNull("'$id' não decodificou", bmp)
            assertEquals(info.largura, bmp!!.width)
            assertTrue("'$id' deveria ter transparência", bmp.hasAlpha())
        }

        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF7FC8FF), Color(0xFFBFE3FF), Color(0xFFFFD6C4), Color(0xFFFFC2DC)))),
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
                    ) {
                        Text("Arte 3D de teste", fontSize = 30.sp, color = Color(0xFF3A2766))
                        // tamanho de uso do manifesto
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                            for (id in ids) ImagemDeArte(id, descricao = id)
                        }
                        // pequenas: decodificadas com redução (inSampleSize 2)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            for (id in ids) ImagemDeArte(id, Modifier.size(48.dp), descricao = "$id pequeno")
                        }
                        // grande, acima do tamanho de uso
                        ImagemDeArte("patinho", Modifier.size(220.dp), descricao = "patinho grande")
                        // id que não existe: mostra a reserva
                        ImagemDeArte("nao_existe", Modifier.size(48.dp)) { Text("🦆", fontSize = 32.sp) }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(500)
        for (id in ids) {
            assertEquals(1, regra.onAllNodesWithContentDescription(id).fetchSemanticsNodes().size)
            assertEquals(1, regra.onAllNodesWithContentDescription("$id pequeno").fetchSemanticsNodes().size)
        }
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_00_arte_de_teste.png")
    }

    /**
     * Personagens em camadas (Fase 1): os 6 cabelos com peles, cores de cabelo e roupas diferentes, o irmãozinho e
     * a irmãzinha, os 4 quadros do tchau e o piscar. Antes da captura, confere a composição: camadas lidas,
     * fundo transparente, figura opaca, e a pele tingida (pele escura dá uma figura mais escura).
     */
    @Test fun personagensEmCamadas() {
        val app = RuntimeEnvironment.getApplication()
        val manifesto = ArteDoApp.manifesto(app)
        val parado = QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ)
        fun composto(p: Perfil): android.graphics.Bitmap {
            val camadas = CamadasDosPersonagens.empilhar(manifesto, CamadasDosPersonagens.idsDaCrianca(p, parado))
            assertNotNull("faltam camadas para $p", camadas)
            val img = ComposicaoDosPersonagens.composto(app, camadas!!, p, 170, 260)
            assertNotNull("a criança $p não foi composta (camada ilegível?)", img)
            return img!!.asAndroidBitmap()
        }
        fun brilho(b: android.graphics.Bitmap): Double {
            var soma = 0.0
            var n = 0
            for (y in 0 until b.height step 2) for (x in 0 until b.width step 2) {
                val c = b.getPixel(x, y)
                if (android.graphics.Color.alpha(c) < 200) continue
                soma += android.graphics.Color.red(c) + android.graphics.Color.green(c) + android.graphics.Color.blue(c)
                n++
            }
            return soma / n.coerceAtLeast(1)
        }
        val clara = composto(Perfil("A", Genero.MENINA, pele = 0, cabelo = 2))
        val escura = composto(Perfil("A", Genero.MENINA, pele = 3, cabelo = 2))
        assertEquals(170, clara.width)
        assertEquals(0, android.graphics.Color.alpha(clara.getPixel(2, 2)))
        assertTrue("o corpo deveria ser opaco", android.graphics.Color.alpha(clara.getPixel(85, 165)) > 200)
        assertTrue("a pele não foi tingida: ${brilho(clara)} x ${brilho(escura)}", brilho(escura) < brilho(clara) - 20)
        for (p in listOf(Perfil("A", Genero.MENINA), Perfil("B", Genero.MENINO))) {
            assertTrue(ComposicaoDosPersonagens.podeUsar(app, PersonagemDaArte.CRIANCA, p))
            assertTrue(ComposicaoDosPersonagens.podeUsar(app, PersonagemDaArte.IRMAO, p))
        }
        // Traje da loja ainda não tem camadas: fica o desenho antigo.
        assertFalse(ComposicaoDosPersonagens.podeUsar(app, PersonagemDaArte.CRIANCA, Perfil("A", Genero.MENINA, traje = "listras")))

        val criancas = listOf(
            Perfil("Ana", Genero.MENINA, pele = 3, cabelo = 1, estiloCabelo = 0, roupa = 1),
            Perfil("Bia", Genero.MENINA, pele = 0, cabelo = 2, estiloCabelo = 1, roupa = 4, irmao = Irmao(genero = Genero.MENINA)),
            Perfil("Cris", Genero.MENINA, pele = 1, cabelo = 3, estiloCabelo = 2, roupa = 2),
            Perfil("Leo", Genero.MENINO, pele = 2, cabelo = 0, estiloCabelo = 0, roupa = 0),
            Perfil("Davi", Genero.MENINO, pele = 3, cabelo = 1, estiloCabelo = 1, roupa = 5),
            Perfil("Gui", Genero.MENINO, pele = 0, cabelo = 2, estiloCabelo = 2, roupa = 3, chapeu = "👑"),
        )
        val quadros = listOf(PoseDosBracos.TCHAU1, PoseDosBracos.TCHAU2, PoseDosBracos.TCHAU3, PoseDosBracos.TCHAU4)
            .map { QuadroDoPersonagem(it, Humor.FELIZ) } + QuadroDoPersonagem(PoseDosBracos.PARADO, Humor.FELIZ, olhosFechados = true)
        regra.mainClock.autoAdvance = false
        regra.setContent {
            TemaCasinha {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color(0xFF7FC8FF), Color(0xFFBFE3FF), Color(0xFFFFD6C4), Color(0xFFFFC2DC)))),
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Personagens em camadas", fontSize = 24.sp, color = Color(0xFF3A2766))
                        Row(verticalAlignment = Alignment.Bottom) {
                            for (p in criancas.take(3)) Crianca(p, Modifier.height(150.dp), animado = false)
                            Irmaozinho(criancas[1], Modifier.height(100.dp), animado = false)
                        }
                        Row(verticalAlignment = Alignment.Bottom) {
                            for (p in criancas.drop(3)) Crianca(p, Modifier.height(150.dp), animado = false)
                            Irmaozinho(criancas[3], Modifier.height(100.dp), animado = false)
                        }
                        // Tchau em 4 quadros e o piscar (olhos fechados), desenhados direto.
                        val contexto = LocalContext.current
                        Row {
                            for (q in quadros) {
                                Canvas(Modifier.size(110.dp * PROPORCAO_CRIANCA, 110.dp)) {
                                    desenharPersonagemEmCamadas(contexto, PersonagemDaArte.CRIANCA, criancas[0], q)
                                }
                            }
                        }
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(500)
        regra.onRoot().captureRoboImage("build/outputs/roborazzi/v3d_01_personagens.png")
    }
}
