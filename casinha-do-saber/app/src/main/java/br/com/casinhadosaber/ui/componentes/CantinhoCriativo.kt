package br.com.casinhadosaber.ui.componentes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Andar
import br.com.casinhadosaber.dominio.Atelie
import br.com.casinhadosaber.dominio.Conquistas
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Pintura
import br.com.casinhadosaber.dominio.quadrosPendurados

/**
 * Pedaços do modo criativo usados em mais de uma tela: a cor de cada lápis, o desenho de uma pintura em
 * miniatura, os quadros e os diplomas nas paredes da sala (casa por dentro) e os botões do cantinho criativo.
 */

/** Cor do lápis [indice] de [Atelie.paleta] (fora da paleta vira branco). */
fun corDaPaleta(indice: Int): Color = Color(Atelie.paleta.getOrElse(indice) { Atelie.paleta[0] }.first)

/** Nome em inglês de cada cor da paleta (a criança ouve as duas línguas, como no resto do app). */
val coresEmIngles = listOf(
    "white", "black", "red", "orange", "yellow", "green", "blue", "purple", "pink", "brown", "gray", "light blue",
)

/** Uma pintura desenhada em quadradinhos (miniatura da galeria, quadro do diploma, prévia). */
@Composable
fun DesenhoDaPintura(pintura: Pintura, modifier: Modifier = Modifier, grade: Boolean = false) {
    Canvas(modifier) {
        val lado = Pintura.LADO
        val c = size.minDimension / lado
        val ox = (size.width - c * lado) / 2
        val oy = (size.height - c * lado) / 2
        drawRect(Color.White, Offset(ox, oy), Size(c * lado, c * lado))
        for (l in 0 until lado) for (k in 0 until lado) {
            val cor = pintura.cor(l, k)
            if (cor != 0) drawRect(corDaPaleta(cor), Offset(ox + k * c, oy + l * c), Size(c + 0.5f, c + 0.5f))
        }
        if (grade) {
            val linha = Color(0x1F000000)
            for (i in 0..lado) {
                drawLine(linha, Offset(ox + i * c, oy), Offset(ox + i * c, oy + c * lado), 1f)
                drawLine(linha, Offset(ox, oy + i * c), Offset(ox + c * lado, oy + i * c), 1f)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Paredes da sala (casa por dentro)
// ---------------------------------------------------------------------------------------------

/**
 * Onde cada quadro fica na sala, sem tapar janela, porta, TV nem escada: dois na parede do fundo (y = 0), um
 * entre a janela e a TV e outro em cima da TV, e o terceiro na parede da esquerda (x = 0), entre a escada e a
 * porta da rua. ([naEsquerda], a0, a1, z0, z1), em ladrilhos e na altura da parede.
 */
private class LugarDoQuadro(val naEsquerda: Boolean, val a0: Float, val a1: Float, val z0: Float, val z1: Float)

private val lugaresDosQuadros = listOf(
    LugarDoQuadro(false, 2.82f, 3.56f, 1.55f, 2.29f),
    LugarDoQuadro(false, 3.92f, 4.72f, 1.62f, 2.42f),
    LugarDoQuadro(true, 2.66f, 3.18f, 1.45f, 1.97f),
)

/**
 * Formas dos quadros pendurados ([quadrosPendurados]) e dos diplomas (rolinhos em cima da porta da rua) na
 * sala do andar de baixo, prontas para a casa por dentro desenhar junto com as paredes.
 */
fun formasDaSalaCriativa(proj: Projecao, estado: EstadoJogo): List<Forma> {
    val formas = ArrayList<Forma>()
    val moldura = Color(0xFFC98B5B)
    for ((q, lugar) in estado.quadrosPendurados.zip(lugaresDosQuadros)) {
        // Face da parede: na do fundo, a "largura" anda em x; na da esquerda, anda em y (de trás para a frente,
        // da direita para a esquerda na tela, por isso a coluna é contada do fim para o desenho não sair espelhado).
        fun face(d: Float, a0: Float, a1: Float, z0: Float, z1: Float): Path =
            if (lugar.naEsquerda) proj.paredeX(d, a0, a1, z0, z1) else proj.paredeY(d, a0, a1, z0, z1)
        val m = 0.07f
        formas += Forma(face(0.004f, lugar.a0 - m, lugar.a1 + m, lugar.z0 - m, lugar.z1 + m), moldura)
        formas += Forma(face(0.005f, lugar.a0, lugar.a1, lugar.z0, lugar.z1), Color.White, contorno = false)
        val lado = Pintura.LADO
        val w = (lugar.a1 - lugar.a0) / lado
        val h = (lugar.z1 - lugar.z0) / lado
        val porCor = HashMap<Int, Path>()
        for (l in 0 until lado) for (k in 0 until lado) {
            val cor = q.cor(l, k)
            if (cor == 0) continue
            val coluna = if (lugar.naEsquerda) lado - 1 - k else k
            val a = lugar.a0 + coluna * w
            val z = lugar.z1 - (l + 1) * h
            porCor.getOrPut(cor) { Path() }.addPath(face(0.006f, a - 0.002f, a + w + 0.002f, z - 0.002f, z + h + 0.002f))
        }
        for ((cor, caminho) in porCor) formas += Forma(caminho, corDaPaleta(cor), contorno = false)
    }
    // Diplomas: até 4 rolinhos pendurados em cima da porta da rua (parede da esquerda).
    val diplomas = Conquistas.diplomas(estado.estrelas).size.coerceAtMost(4)
    for (i in 0 until diplomas) {
        val a0 = 3.33f + i * 0.31f
        val a1 = a0 + 0.26f
        formas += Forma(proj.paredeX(0.004f, a0, a1, 2.36f, 2.84f), Color(0xFFFFF1C9))
        formas += Forma(proj.paredeX(0.005f, a0, a1, 2.36f, 2.42f), Color(0xFFE0B865), contorno = false)
        formas += Forma(proj.paredeX(0.005f, a0, a1, 2.78f, 2.84f), Color(0xFFE0B865), contorno = false)
        formas += Forma(proj.paredeX(0.006f, a0 + 0.08f, a1 - 0.08f, 2.46f, 2.56f), Color(0xFFEF476F), contorno = false)
    }
    return formas
}

/** Só o andar de baixo tem a sala. */
fun formasDaSalaCriativa(andar: Andar, proj: Projecao, estado: EstadoJogo): List<Forma> =
    if (andar == Andar.DE_BAIXO) formasDaSalaCriativa(proj, estado) else emptyList()

// ---------------------------------------------------------------------------------------------
// Botões do cantinho criativo (casa por dentro)
// ---------------------------------------------------------------------------------------------

/** O que os botões do cantinho criativo da sala abrem. */
class CantinhoDaSala(
    val onAtelie: () -> Unit,
    val onPiano: () -> Unit,
    val onDiplomas: () -> Unit,
    val onAlbum: () -> Unit,
    /** Livro de Colorir e quebra cabeças (Fases 10 e 11): uma segunda fileira de botões, se vierem. */
    val onColorir: (() -> Unit)? = null,
    val onPuzzle: (() -> Unit)? = null,
)

/**
 * Cavalete, piano, diplomas e álbum: quatro botões grandes no canto da sala. O de diplomas mostra quantos
 * a criança já tem (o mesmo número dos rolinhos em cima da porta).
 */
@Composable
fun BotoesDoCantinho(
    estado: EstadoJogo,
    onAtelie: () -> Unit,
    onPiano: () -> Unit,
    onDiplomas: () -> Unit,
    onAlbum: () -> Unit,
    modifier: Modifier = Modifier,
    onColorir: (() -> Unit)? = null,
    onPuzzle: (() -> Unit)? = null,
) {
    val diplomas = Conquistas.diplomas(estado.estrelas).size
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
    if (onColorir != null || onPuzzle != null) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
            if (onColorir != null) {
                BotaoDoCantinho("🖍️", "Colorir", "Livro de colorir", Color(0xFFFFE5D0), onColorir, Modifier.testTag("abrir_colorir"))
            }
            if (onPuzzle != null) {
                BotaoDoCantinho("🧩", "Peças", "Quebra cabeças", Color(0xFFD9F0FF), onPuzzle, Modifier.testTag("abrir_puzzle"))
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
        BotaoDoCantinho("🎨", "Ateliê", "Ateliê de pintura", Color(0xFFFFE0EC), onAtelie, Modifier.testTag("abrir_atelie"))
        BotaoDoCantinho("🎹", "Piano", "Piano", Color(0xFFE3E0FF), onPiano, Modifier.testTag("abrir_piano"))
        BotaoDoCantinho(
            "📜", "Diplomas",
            if (diplomas > 0) "Diplomas, você tem $diplomas" else "Diplomas", Color(0xFFFFF1C9), onDiplomas,
            Modifier.testTag("abrir_diplomas"),
        )
        BotaoDoCantinho("📒", "Álbum", "Álbum de figurinhas", Color(0xFFDDF5E3), onAlbum, Modifier.testTag("abrir_album"))
    }
    }
}

@Composable
private fun BotaoDoCantinho(emoji: String, rotulo: String, descricao: String, cor: Color, onClick: () -> Unit, modifier: Modifier, tamanho: Dp = 64.dp) {
    BotaoFofo(onClick = onClick, modifier = modifier, cor = cor, corBase = escurecer(cor, 0.75f), descricao = descricao, profundidade = 4.dp) {
        Column(Modifier.size(tamanho).padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 28.sp)
            Text(rotulo, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, color = Color(0xFF3D2C5E))
        }
    }
}
