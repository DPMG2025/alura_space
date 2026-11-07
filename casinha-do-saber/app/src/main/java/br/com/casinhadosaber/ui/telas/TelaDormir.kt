package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.ui.componentes.IconeDaBarra
import br.com.casinhadosaber.ui.componentes.IconeDeArteDaBarra
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.PecasDasTelas
import br.com.casinhadosaber.ui.componentes.TextoComContorno
import br.com.casinhadosaber.ui.componentes.faceDeBrinquedo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.PortaoDeAdulto
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.tema.Formas
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Fim do tempo do dia: a casinha vai dormir até amanhã (ou até um adulto liberar). */
@Composable
fun TelaDormir(estado: EstadoJogo, onLiberar: () -> Unit) {
    val narrador = LocalNarrador.current

    LaunchedEffect(Unit) {
        // "Good night!" vira o "Boa noite!" gravado pela família, quando houver (área dos pais).
        narrador.falarPartes(
            listOf(
                br.com.casinhadosaber.dominio.ParteDaFala(pt("Que dia divertido! Agora a casinha vai dormir. Até amanhã!")),
                br.com.casinhadosaber.dominio.ParteDaFala(en("Good night!"), "boa_noite"),
            ),
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize().ceuDaNoite()) {
        val baixa = maxHeight < 700.dp
        // Estrelinhas 3D penduradas no céu, piscando devagar (uma imagem só, do cache, em vários lugares).
        EstrelasDaNoite()
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // A lua de touca dormindo, com o "Zzz" subindo devagar.
            LuaDormindo(if (baixa) 96.dp else 140.dp)
            TextoComContorno(
                "Boa noite!",
                estilo = MaterialTheme.typography.displayMedium,
                corDoContorno = AzulDaNoite,
                corDoLabio = Color(0xFF0B1030),
            )
            Spacer(Modifier.height(10.dp))
            CenaDaCasa(
                estado,
                Modifier
                    .shadow(12.dp, Formas.Grande, ambientColor = Color(0xFF05081A), spotColor = Color(0xFF05081A))
                    .clip(Formas.Grande)
                    .border(3.dp, Color.White.copy(alpha = 0.25f), Formas.Grande)
                    .weight(1f, fill = false),
                noite = true,
            )
            Spacer(Modifier.height(14.dp))
            TextoComContorno(
                "A casinha volta amanhã!",
                estilo = MaterialTheme.typography.titleLarge,
                corDoContorno = AzulDaNoite,
                espessura = 3.dp,
                profundidade = 0.dp,
                maxLinhas = 2,
            )
            Spacer(Modifier.height(14.dp))
            // Só um adulto libera (continha de multiplicação). Sem isso, a casinha acorda sozinha amanhã.
            var mostrarPortao by remember { mutableStateOf(false) }
            if (mostrarPortao) {
                PortaoDeAdulto(onLiberar = onLiberar)
            } else {
                // Botão sólido (o translúcido de antes parecia desligado), num azul da noite, com o cadeado 3D.
                BotaoFofo(onClick = { mostrarPortao = true }, cor = Color(0xFF3B4C8C), corBase = Color(0xFF26336A), profundidade = 4.dp, descricao = "Adulto") {
                    Row(Modifier.padding(start = 14.dp, end = 22.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .faceDeBrinquedo(Color.White, CircleShape, redonda = true),
                            contentAlignment = Alignment.Center,
                        ) {
                            IconeDeArteDaBarra(IconeDaBarra(IconesDaBarra.PAIS), Modifier.size(22.dp)) { Text("🔒") }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Adulto", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

/** Contorno dos títulos da noite. */
private val AzulDaNoite = Color(0xFF1B2556)

/** A lua 3D de touca (`tela_lua_dormindo`) balançando devagar, com o `tela_zzz` subindo e sumindo. */
@Composable
private fun LuaDormindo(tamanho: Dp) {
    val tempo = rememberInfiniteTransition(label = "lua")
    val balanco by tempo.animateFloat(-1f, 1f, infiniteRepeatable(tween(2600), RepeatMode.Reverse), label = "balanco")
    val sobe by tempo.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing)), label = "sobe")
    Box(Modifier.size(width = tamanho * 1.45f, height = tamanho).clearAndSetSemantics { }) {
        ImagemDeArte(
            PecasDasTelas.LUA_DORMINDO,
            Modifier
                .align(Alignment.BottomStart)
                .size(tamanho)
                .graphicsLayer { rotationZ = 4f * balanco },
        ) { Box(contentAlignment = Alignment.Center) { Text("🌙", fontSize = (tamanho.value * 0.6f).sp) } }
        ImagemDeArte(
            PecasDasTelas.ZZZ,
            Modifier
                .align(Alignment.TopEnd)
                .size(tamanho * 0.5f)
                .graphicsLayer {
                    translationY = -sobe * size.height * 0.35f + size.height * 0.15f
                    alpha = ((1f - sobe) * 1.4f).coerceIn(0f, 1f)
                    val e = 0.8f + 0.25f * sobe
                    scaleX = e
                    scaleY = e
                },
        )
    }
}

/** Lugares (fração da tela) e tamanho (dp) das estrelas 3D da noite; as do [ceuDaNoite] continuam no fundo. */
private val ESTRELAS_3D = listOf(
    Triple(0.10f, 0.08f, 30f), Triple(0.84f, 0.12f, 36f), Triple(0.70f, 0.30f, 22f),
    Triple(0.06f, 0.46f, 24f), Triple(0.90f, 0.62f, 26f), Triple(0.14f, 0.90f, 28f),
)

@Composable
private fun BoxWithConstraintsScope.EstrelasDaNoite() {
    val pisca by rememberInfiniteTransition(label = "estrelas3d")
        .animateFloat(0f, 1f, infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "pisca")
    for ((k, e) in ESTRELAS_3D.withIndex()) {
        val (fx, fy, lado) = e
        ImagemDeArte(
            PecasDasTelas.ESTRELA_NOITE,
            Modifier
                .offset(x = maxWidth * fx - (lado / 2).dp, y = maxHeight * fy - (lado / 2).dp)
                .size(lado.dp)
                .graphicsLayer {
                    val a = 2f * PI.toFloat() * (pisca + k * 0.27f)
                    alpha = 0.6f + 0.4f * (0.5f + 0.5f * sin(a))
                    rotationZ = 8f * sin(a)
                },
        )
    }
}

/** Céu da noite: azul fundo que clareia um pouco embaixo, com estrelinhas que piscam devagar. */
@Composable
private fun Modifier.ceuDaNoite(): Modifier {
    val fase by rememberInfiniteTransition(label = "noite")
        .animateFloat(0f, 1f, infiniteRepeatable(tween(6000, easing = LinearEasing)), label = "pisca")
    return drawWithCache {
        val ceu = Brush.verticalGradient(listOf(Color(0xFF0F1638), Color(0xFF16204A), Color(0xFF2B3A78)))
        val estrela = Path()
        onDrawBehind {
            drawRect(ceu)
            for (k in 0 until ESTRELAS_DA_NOITE.size / 3) {
                val a = 2f * PI.toFloat() * (fase + k * 0.23f)
                val alfa = 0.35f + 0.35f * (0.5f + 0.5f * sin(a))
                val cx = ESTRELAS_DA_NOITE[k * 3] * size.width
                val cy = ESTRELAS_DA_NOITE[k * 3 + 1] * size.height
                val r = ESTRELAS_DA_NOITE[k * 3 + 2].dp.toPx()
                estrela.reset()
                estrela.moveTo(cx, cy - r)
                estrela.quadraticTo(cx, cy, cx + r, cy)
                estrela.quadraticTo(cx, cy, cx, cy + r)
                estrela.quadraticTo(cx, cy, cx - r, cy)
                estrela.quadraticTo(cx, cy, cx, cy - r)
                estrela.close()
                drawPath(estrela, Color.White.copy(alpha = alfa))
            }
        }
    }
}

/** x, y (fração da tela) e tamanho (dp) das estrelinhas. */
private val ESTRELAS_DA_NOITE = floatArrayOf(
    0.08f, 0.05f, 6f, 0.30f, 0.11f, 4f, 0.86f, 0.06f, 7f, 0.62f, 0.03f, 5f,
    0.94f, 0.22f, 4f, 0.05f, 0.30f, 5f, 0.18f, 0.82f, 4f, 0.90f, 0.78f, 6f,
    0.50f, 0.93f, 4f, 0.74f, 0.90f, 5f,
)
