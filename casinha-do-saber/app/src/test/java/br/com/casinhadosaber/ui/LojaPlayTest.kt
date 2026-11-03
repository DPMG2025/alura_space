package br.com.casinhadosaber.ui

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.R
import br.com.casinhadosaber.audio.Narrador
import br.com.casinhadosaber.audio.Sons
import br.com.casinhadosaber.dominio.Atividade
import br.com.casinhadosaber.dominio.Categoria
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Loja
import br.com.casinhadosaber.dominio.ParteDoCorpo
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Pergunta
import br.com.casinhadosaber.dominio.Posicao
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.tema.Cores
import br.com.casinhadosaber.ui.tema.Fredoka
import br.com.casinhadosaber.ui.tema.TemaCasinha
import br.com.casinhadosaber.ui.telas.TelaCasa
import br.com.casinhadosaber.ui.telas.TelaCasaPorDentro
import br.com.casinhadosaber.ui.telas.TelaInicio
import br.com.casinhadosaber.ui.telas.TelaJogo
import br.com.casinhadosaber.ui.telas.TelaPerfil
import br.com.casinhadosaber.ui.telas.TelaPremio
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
import java.io.File
import kotlin.random.Random

/**
 * Imagens da ficha da Google Play, no tamanho exato em pixels, gravadas em docs/loja:
 * ícone 512 x 512, imagem de destaque 1024 x 500 e capturas de celular (1080 x 1920),
 * tablet de 7 polegadas (1200 x 1920) e de 10 polegadas (1920 x 1200).
 *
 * Cada imagem é montada em Compose numa tela com a densidade que dá o tamanho pedido, capturada
 * pelo Roborazzi e regravada como PNG sem transparência (RGB de 24 bits); só o ícone é RGBA,
 * todo opaco, como a Play pede (PNG de 32 bits).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = CELULAR)
class LojaPlayTest {

    @get:Rule
    val regra = createComposeRule()

    private val menina = Perfil("Ana", Genero.MENINA, pele = 2, cabelo = 1)
    private val menino = Perfil("Leo", Genero.MENINO, pele = 0, cabelo = 3)
    private val gerador = GeradorDeFases(Random(4))

    private fun estado(n: Int, colocados: Int, perfil: Perfil = menina): EstadoJogo {
        var e = EstadoJogo(perfil = perfil)
        val fases = (0 until 10).flatMap { i -> Trilha.entries.map { gerador.fases(it)[i] } }
        for ((k, f) in fases.take(n).withIndex()) e = e.registrarVitoria(f, 1 + k % 3).first
        for (item in ItensDaCasa.ordem.take(colocados)) e = e.colocar(item)
        return e
    }

    private fun trilhaVencida(trilha: Trilha, n: Int): EstadoJogo {
        var e = estado(40, 40)
        for (f in gerador.fases(trilha).take(n)) e = e.registrarVitoria(f, 1 + f.numero % 3).first
        return e
    }

    private fun casaCheia(): EstadoJogo {
        val compras = Loja.produtos.filter { it.categoria == Categoria.COMIDA }.take(6).map { it.id } +
            Loja.produtos.filter { it.categoria == Categoria.ROUPA }.take(3).map { it.id }
        return estado(40, 40).copy(moedas = 23, comprados = compras.toSet())
    }

    private fun deMexer(instrucao: String, atividade: Atividade) =
        Pergunta.deAtividade(instrucao, listOf(pt(instrucao)), atividade, listOf(pt("Muito bem!")))

    private val cestaContar = deMexer("Coloque 4 na cesta", Atividade.Cesta(List(7) { "🍎" }, 4))
    private val cestaClassificar = deMexer(
        "Só animais na cesta",
        Atividade.Cesta(listOf("🐶", "🐱", "🐮"), 3, listOf("🍎", "⚽", "👕")),
    )
    private val tracar = deMexer("Trace a letra A", Atividade.Tracar("A"))

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
     * Captura a tela inteira e grava em docs/loja/[nome] com exatamente [largura] x [altura] pixels.
     * Sem [comAlfa], o PNG sai em RGB (24 bits, sem canal alfa), como a Play exige nas capturas e no destaque.
     * O java.awt não existe nos testes Android, então o PNG é escrito aqui mesmo (cabeçalho, dados e CRC).
     */
    private fun gravar(nome: String, largura: Int, altura: Int, comAlfa: Boolean = false) {
        val bruto = File("build/outputs/loja_bruto/$nome")
        bruto.parentFile?.mkdirs()
        bruto.delete()
        regra.onRoot().captureRoboImage(bruto.path)
        val lida = android.graphics.BitmapFactory.decodeFile(bruto.absolutePath) ?: throw AssertionError("captura $nome ilegível")
        // A tela já tem o tamanho certo; se o Robolectric arredondar algum pixel, a proporção continua a mesma.
        assertEquals("proporção de $nome", largura.toFloat() / altura, lida.width.toFloat() / lida.height, 0.01f)
        val final = if (lida.width == largura && lida.height == altura) lida
        else android.graphics.Bitmap.createScaledBitmap(lida, largura, altura, true)
        val pixels = IntArray(largura * altura)
        final.getPixels(pixels, 0, largura, 0, 0, largura, altura)
        // Fundo opaco por baixo de qualquer pixel translúcido (cor de fundo do app).
        val fundo = intArrayOf(0xFF, 0xF8, 0xE7)
        val destino = File("../docs/loja/$nome")
        destino.parentFile?.mkdirs()
        destino.writeBytes(png(largura, altura, comAlfa) { i ->
            val p = pixels[i]
            val a = p ushr 24
            IntArray(3) { c ->
                val v = (p shr (16 - 8 * c)) and 0xFF
                (v * a + fundo[c] * (255 - a)) / 255
            }
        })

        // Conferências do arquivo gravado: tamanho, tipo de cor e conteúdo.
        val cabecalho = destino.readBytes()
        fun inteiro(pos: Int) = java.nio.ByteBuffer.wrap(cabecalho, pos, 4).int
        assertEquals("largura de $nome", largura, inteiro(16))
        assertEquals("altura de $nome", altura, inteiro(20))
        assertEquals("tipo de cor de $nome (2 = RGB, 6 = RGBA)", if (comAlfa) 6 else 2, cabecalho[25].toInt())
        val relida = android.graphics.BitmapFactory.decodeFile(destino.absolutePath) ?: throw AssertionError("$nome gravada ilegível")
        assertEquals(largura, relida.width)
        assertEquals(altura, relida.height)
        // Regras da Play: lado entre 320 e 3840 px e o lado maior no máximo o dobro do menor.
        assertTrue(minOf(largura, altura) >= 320 || nome.startsWith("icone"))
        assertTrue(maxOf(largura, altura) <= 3840)
        assertTrue(maxOf(largura, altura) <= 2 * minOf(largura, altura) + 24)
        // Uma imagem toda de uma cor só seria sinal de captura vazia.
        val cores = HashSet<Int>()
        for (y in 0 until altura step 37) for (x in 0 until largura step 37) cores += relida.getPixel(x, y)
        assertTrue("$nome parece vazia", cores.size > 20)
    }

    /** PNG simples, 8 bits por canal, sem filtro: RGB (cor 2) ou RGBA todo opaco (cor 6). */
    private fun png(largura: Int, altura: Int, comAlfa: Boolean, rgb: (Int) -> IntArray): ByteArray {
        val canais = if (comAlfa) 4 else 3
        val linha = 1 + largura * canais
        val cru = ByteArray(linha * altura)
        for (y in 0 until altura) {
            var k = y * linha
            cru[k++] = 0
            for (x in 0 until largura) {
                val c = rgb(y * largura + x)
                cru[k++] = c[0].toByte(); cru[k++] = c[1].toByte(); cru[k++] = c[2].toByte()
                if (comAlfa) cru[k++] = 0xFF.toByte()
            }
        }
        val saida = java.io.ByteArrayOutputStream()
        fun bloco(tipo: String, dados: ByteArray) {
            val d = java.io.DataOutputStream(saida)
            d.writeInt(dados.size)
            val corpo = tipo.toByteArray(Charsets.US_ASCII) + dados
            d.write(corpo)
            d.writeInt(java.util.zip.CRC32().apply { update(corpo) }.value.toInt())
        }
        saida.write(byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte(), 13, 10, 26, 10))
        val ihdr = java.nio.ByteBuffer.allocate(13).putInt(largura).putInt(altura)
            .put(8.toByte()).put((if (comAlfa) 6 else 2).toByte()).put(0.toByte()).put(0.toByte()).put(0.toByte()).array()
        bloco("IHDR", ihdr)
        val comprimido = java.io.ByteArrayOutputStream()
        java.util.zip.DeflaterOutputStream(comprimido, java.util.zip.Deflater(9)).use { it.write(cru) }
        bloco("IDAT", comprimido.toByteArray())
        bloco("IEND", ByteArray(0))
        return saida.toByteArray()
    }

    // ------------------------------------------------------------------ ícone e destaque

    /** Ícone da loja: o mesmo desenho do ícone adaptativo, com o céu de fundo e um gramado embaixo, sem cantos arredondados. */
    @Config(qualifiers = "w256dp-h256dp-xhdpi")
    @Test fun icone() {
        montar {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawRect(Brush.verticalGradient(listOf(Color(0xFF7EC8FF), Color(0xFFBFE6FF))))
                    // Janela de 84 unidades (de 12 a 96) do desenho de 108: o gramado, da mesma cor da grama
                    // do desenho, começa onde ela começa (78) e vai até embaixo, sem degrau.
                    val topo = size.height * (78f - 12f) / 84f
                    drawRect(
                        Color(0xFF7CC56B),
                        topLeft = Offset(0f, topo),
                        size = Size(size.width, size.height - topo),
                    )
                }
                Image(
                    painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.requiredSize((256f * 108f / 84f).dp),
                )
            }
        }
        regra.mainClock.advanceTimeBy(500)
        gravar("icone_512.png", 512, 512, comAlfa = true)
    }

    /** Imagem de destaque: a casinha completa, a criança acenando e o nome grande, longe das bordas. */
    @Config(qualifiers = "w512dp-h250dp-land-xhdpi")
    @Test fun destaque() {
        val casa = estado(40, 40).copy(perfil = null)
        montar {
            Box(Modifier.fillMaxSize()) {
                // Mesmo céu e mesma grama da cena (preencherAltura), para a casa se emendar no fundo.
                Canvas(Modifier.fillMaxSize()) {
                    val chao = size.height - (250.dp * 0.22f).toPx()
                    drawRect(Brush.verticalGradient(listOf(Color(0xFF7EC8FF), Color(0xFFCDEBFF)), endY = chao))
                    drawRect(
                        Brush.verticalGradient(listOf(Color(0xFF7CC56B), Color(0xFF4F9B44)), startY = chao, endY = size.height),
                        topLeft = Offset(0f, chao),
                        size = Size(size.width, size.height - chao),
                    )
                }
                Box(Modifier.align(Alignment.BottomStart).size(217.dp, 250.dp)) {
                    CenaDaCasa(casa, Modifier.fillMaxSize(), preencherAltura = true)
                }
                Crianca(
                    menina.copy(chapeu = "👑"),
                    Modifier.align(Alignment.BottomStart).offset(x = 190.dp, y = (-10).dp).height(128.dp),
                    acenando = true,
                )
                Column(
                    Modifier.align(Alignment.TopEnd).padding(top = 34.dp, end = 26.dp).width(204.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Casinha\ndo Saber",
                        style = TextStyle(
                            fontFamily = Fredoka,
                            fontWeight = FontWeight.Bold,
                            fontSize = 46.sp,
                            lineHeight = 48.sp,
                            color = Cores.Texto,
                            textAlign = TextAlign.Center,
                            shadow = Shadow(Color.White, Offset(0f, 3f), 6f),
                        ),
                    )
                    Box(
                        Modifier.clip(RoundedCornerShape(50)).background(Cores.Roxo).padding(horizontal = 16.dp, vertical = 6.dp),
                    ) {
                        Text(
                            "Aprender brincando!",
                            style = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White),
                        )
                    }
                }
            }
        }
        regra.mainClock.advanceTimeBy(2500)
        gravar("destaque_1024x500.png", 1024, 500)
    }

    // ------------------------------------------------------------------ moldura das capturas

    /**
     * Mostra [conteudo] como se fosse uma tela de [larguraDp] x [alturaDp] (densidade e configuração
     * próprias), encolhida para caber no espaço, com borda branca e cantos arredondados.
     */
    @Composable
    private fun TelaNaMoldura(larguraDp: Int, alturaDp: Int, modifier: Modifier, conteudo: @Composable () -> Unit) {
        BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
            val borda = 5.dp
            val escala = minOf((maxWidth - borda * 2).value / larguraDp, (maxHeight - borda * 2).value / alturaDp)
            val largura = (larguraDp * escala).dp
            val altura = (alturaDp * escala).dp
            val densidade = LocalDensity.current
            val base = LocalConfiguration.current
            val config = Configuration(base).apply {
                screenWidthDp = larguraDp
                screenHeightDp = alturaDp
                smallestScreenWidthDp = minOf(larguraDp, alturaDp)
                orientation = if (larguraDp > alturaDp) Configuration.ORIENTATION_LANDSCAPE else Configuration.ORIENTATION_PORTRAIT
            }
            // Sombra simples (a elevação não aparece no Robolectric) e a moldura branca.
            Box(
                Modifier.offset(y = 6.dp).size(largura + borda * 2, altura + borda * 2)
                    .clip(RoundedCornerShape(30.dp)).background(Color(0x33203050)),
            )
            Box(
                Modifier.size(largura + borda * 2, altura + borda * 2).clip(RoundedCornerShape(30.dp)).background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(largura, altura).clip(RoundedCornerShape(25.dp)).background(Cores.Fundo)) {
                    CompositionLocalProvider(
                        LocalDensity provides Density(densidade.density * escala, densidade.fontScale),
                        LocalConfiguration provides config,
                    ) {
                        Box(Modifier.fillMaxSize()) { conteudo() }
                    }
                }
            }
        }
    }

    /** Fundo colorido, título curto no alto (estilo loja) e a tela do app na moldura. */
    @Composable
    private fun Vitrine(
        titulo: String,
        cores: Pair<Long, Long>,
        larguraDp: Int,
        alturaDp: Int,
        alturaTitulo: Dp,
        tamanhoTitulo: TextUnit,
        margem: Dp,
        conteudo: @Composable () -> Unit,
    ) {
        Column(
            Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(cores.first), Color(cores.second))))
                .padding(start = margem, end = margem, bottom = margem),
        ) {
            Box(Modifier.fillMaxWidth().height(alturaTitulo), contentAlignment = Alignment.Center) {
                Text(
                    titulo,
                    style = TextStyle(
                        fontFamily = Fredoka,
                        fontWeight = FontWeight.Bold,
                        fontSize = tamanhoTitulo,
                        lineHeight = tamanhoTitulo * 1.12f,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        shadow = Shadow(Color(0x55203050), Offset(0f, 4f), 10f),
                    ),
                    maxLines = 2,
                )
            }
            TelaNaMoldura(larguraDp, alturaDp, Modifier.fillMaxWidth().weight(1f), conteudo)
        }
    }

    /** Tela como no app num tablet: céu atrás, escala de tablet e largura máxima (igual à MainActivity). */
    @Composable
    private fun NoTablet(larga: Boolean, conteudo: @Composable () -> Unit) {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                drawRect(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo)))
            }
            MolduraDoApp(larga = larga) { conteudo() }
        }
    }

    private fun celular(nome: String, titulo: String, cores: Pair<Long, Long>, acao: (() -> Unit)? = null, tela: @Composable () -> Unit) {
        montar { Vitrine(titulo, cores, 393, 852, 112.dp, 29.sp, 22.dp, tela) }
        regra.mainClock.advanceTimeBy(2500)
        if (acao != null) {
            acao()
            regra.mainClock.advanceTimeBy(1200)
        }
        gravar(nome, 1080, 1920)
    }

    private fun tablet7(nome: String, titulo: String, cores: Pair<Long, Long>, tela: @Composable () -> Unit) {
        montar { Vitrine(titulo, cores, 800, 1280, 150.dp, 40.sp, 34.dp) { NoTablet(larga = false, tela) } }
        regra.mainClock.advanceTimeBy(2500)
        gravar(nome, 1200, 1920)
    }

    private fun tablet10(nome: String, titulo: String, cores: Pair<Long, Long>, larga: Boolean, tela: @Composable () -> Unit) {
        montar { Vitrine(titulo, cores, 1280, 800, 92.dp, 36.sp, 24.dp) { NoTablet(larga, tela) } }
        regra.mainClock.advanceTimeBy(2500)
        gravar(nome, 1920, 1200)
    }

    private val roxo = 0xFF6C63FF to 0xFFA79FFF
    private val laranja = 0xFFFF8F1C to 0xFFFFC66B
    private val rosa = 0xFFEF476F to 0xFFFF9BB3
    private val verde = 0xFF2FA65A to 0xFF86DB9C
    private val azul = 0xFF2C86D9 to 0xFF7CC8FF
    private val turquesa = 0xFF17A398 to 0xFF6FDACB

    private fun abrirPresente() {
        repeat(3) {
            regra.onAllNodes(hasContentDescription("Presente. Toque para abrir.")).onFirst().performClick()
            regra.mainClock.advanceTimeBy(400)
        }
    }

    // ------------------------------------------------------------------ celular (1080 x 1920)

    @Test fun celular01Inicio() = celular("loja_celular_01.png", "Aprenda brincando e monte a sua casinha", azul) {
        TelaInicio(estado(12, 12), true, {}, {}, {}, {}, missaoDisponivel = true)
    }

    @Test fun celular02Cesta() = celular("loja_celular_02.png", "Arraste, conte e separe com o dedo", laranja) {
        TelaJogo(gerador.fases(Trilha.LOGICA)[7], menino, {}, {}, perguntasProntas = listOf(cestaClassificar))
    }

    @Test fun celular03Premio() = celular("loja_celular_03.png", "Cada fase vencida vale um presente", rosa, acao = ::abrirPresente) {
        TelaPremio(gerador.fases(Trilha.LOGICA)[20], 3, null, menina, 12, true, {}, {}, {}, brinde = Loja.porId.getValue("chapeu👑"))
    }

    @Test fun celular04Casa() = celular("loja_celular_04.png", "40 peças para montar a sua casa", verde) {
        val e = estado(40, 40).pintar(br.com.casinhadosaber.dominio.Comodo.SALA).copy(posicaoCrianca = Posicao(0.45f, 0.99f))
        TelaCasa(e, {}, {}, {}, onEspelho = {}, onEntrar = {})
    }

    @Test fun celular05PorDentro() = celular("loja_celular_05.png", "Entre na casa e mude os móveis de lugar", roxo) {
        TelaCasaPorDentro(estado(40, 40), {}, {})
    }

    @Test fun celular06Espelho() = celular("loja_celular_06.png", "16 roupas e 18 acessórios", rosa) {
        val comprados = setOf(
            "acessoriooculos", "acessoriobalao", "acessoriotenis", "chapeu👑",
            "roupafutebol", "roupaastronauta", "roupafada", "roupadinossauro", "roupapirata", "roupaabelha",
        )
        val p = menina.copy(traje = "futebol", acessorios = mapOf(ParteDoCorpo.ROSTO to "oculos", ParteDoCorpo.MAO to "balao"))
        TelaPerfil(p, espelho = true, comprados = comprados, onVoltar = {}) {}
    }

    @Test fun celular07Caminho() = celular("loja_celular_07.png", "200 fases em 5 mundos", laranja) {
        TelaTrilha(Trilha.MATEMATICA, trilhaVencida(Trilha.MATEMATICA, 20), {}, {}, onTreinar = {})
    }

    @Test fun celular08Tracar() = celular("loja_celular_08.png", "Trace letras e números", turquesa) {
        TelaJogo(gerador.fases(Trilha.PORTUGUES)[1], menina, {}, {}, perguntasProntas = listOf(tracar))
    }

    // ------------------------------------------------------------------ tablet de 7 polegadas (1200 x 1920)

    @Config(qualifiers = TABLET7)
    @Test fun tablet7Inicio() = tablet7("loja_tablet7_01.png", "Aprenda brincando e monte a sua casinha", azul) {
        TelaInicio(estado(20, 20), true, {}, {}, {}, {}, missaoDisponivel = true)
    }

    @Config(qualifiers = TABLET7)
    @Test fun tablet7Cesta() = tablet7("loja_tablet7_02.png", "Arraste, conte e aprenda", laranja) {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[2], menina, {}, {}, perguntasProntas = listOf(cestaContar))
    }

    @Config(qualifiers = TABLET7)
    @Test fun tablet7PorDentro() = tablet7("loja_tablet7_03.png", "Uma casa de dois andares para arrumar", roxo) {
        TelaCasaPorDentro(estado(40, 40), {}, {})
    }

    @Config(qualifiers = TABLET7)
    @Test fun tablet7Geladeira() = tablet7("loja_tablet7_04.png", "Geladeira, guarda roupa e lojinha", verde) {
        TelaCasaPorDentro(casaCheia(), {}, {}, abrirInicial = br.com.casinhadosaber.dominio.Acao.GELADEIRA)
    }

    // ------------------------------------------------------------------ tablet de 10 polegadas (1920 x 1200)

    @Config(qualifiers = TABLET10)
    @Test fun tablet10Inicio() = tablet10("loja_tablet10_01.png", "Aprenda brincando e monte a sua casinha", azul, larga = true) {
        TelaInicio(estado(20, 20), true, {}, {}, {}, {}, missaoDisponivel = true)
    }

    @Config(qualifiers = TABLET10)
    @Test fun tablet10Casa() = tablet10("loja_tablet10_02.png", "40 peças para montar a sua casa", verde, larga = true) {
        TelaCasa(estado(40, 40), {}, {}, {}, onEspelho = {}, onEntrar = {})
    }

    @Config(qualifiers = TABLET10)
    @Test fun tablet10Caminho() = tablet10("loja_tablet10_03.png", "Números, letras, inglês e lógica", laranja, larga = true) {
        TelaTrilha(Trilha.INGLES, trilhaVencida(Trilha.INGLES, 22), {}, {}, onTreinar = {})
    }

    @Config(qualifiers = TABLET10)
    @Test fun tablet10Jogo() = tablet10("loja_tablet10_04.png", "Perguntas faladas para quem ainda não lê", roxo, larga = false) {
        TelaJogo(gerador.fases(Trilha.MATEMATICA)[1], menina, {}, {})
    }

    /** As imagens de docs/loja são só as geradas aqui (nenhuma sobra de nome antigo). */
    @Test fun nomesConhecidos() {
        val pasta = File("../docs/loja")
        if (!pasta.exists()) return
        val esperados = setOf("icone_512.png", "destaque_1024x500.png") +
            (1..8).map { "loja_celular_%02d.png".format(it) } +
            (1..4).map { "loja_tablet7_%02d.png".format(it) } +
            (1..4).map { "loja_tablet10_%02d.png".format(it) }
        for (f in pasta.listFiles().orEmpty().filter { it.extension == "png" }) {
            assertTrue("imagem desconhecida em docs/loja: ${f.name}", f.name in esperados)
        }
    }
}

/** Celular 1080 x 1920 (9:16): 360 x 640 dp com densidade 3. */
private const val CELULAR = "w360dp-h640dp-xxhdpi"

/** Tablet de 7 polegadas em pé, 1200 x 1920: 600 x 960 dp com densidade 2. */
private const val TABLET7 = "w600dp-h960dp-port-xhdpi"

/** Tablet de 10 polegadas deitado, 1920 x 1200: 960 x 600 dp com densidade 2. */
private const val TABLET10 = "w960dp-h600dp-land-xhdpi"
