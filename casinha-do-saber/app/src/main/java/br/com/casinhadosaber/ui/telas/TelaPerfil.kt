package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import br.com.casinhadosaber.ui.componentes.caminhoEstrela
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Produto
import br.com.casinhadosaber.dominio.Serializacao
import br.com.casinhadosaber.dominio.tirando
import br.com.casinhadosaber.dominio.vestindo
import br.com.casinhadosaber.ui.componentes.Provador
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

private val nomesDoCabelo = listOf("castanho", "preto", "loiro", "ruivo")
private val nomesDaRoupa = listOf("azul", "rosa", "verde", "laranja", "roxa", "vermelha")

/**
 * Primeira tela: a criança monta quem vai morar na casinha. Tudo é falado e mostrado
 * num personagem grande que pula a cada escolha; o nome fica com o adulto.
 *
 * Com [espelho], é o espelho (🪞) da casa e do início: muda a aparência atual, mostra o provador com as
 * roupas, chapéus e acessórios do guarda-roupa ([roupas] e o que está em [comprados]) e volta para onde
 * estava ([onVoltar]), sem repetir as boas vindas.
 */
@Composable
fun TelaPerfil(
    atual: Perfil?,
    espelho: Boolean = false,
    roupas: List<Produto> = emptyList(),
    /** Ids comprados na loja: o provador do espelho mostra o que é da criança. */
    comprados: Set<String> = emptySet(),
    onVoltar: (() -> Unit)? = null,
    onPronto: (Perfil) -> Unit,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var genero by rememberSaveable { mutableStateOf(atual?.genero) }
    var pele by rememberSaveable { mutableStateOf(atual?.pele ?: 0) }
    var cabelo by rememberSaveable { mutableStateOf(atual?.cabelo ?: 0) }
    var estilo by rememberSaveable { mutableStateOf(atual?.estiloCabelo ?: 0) }
    var roupa by rememberSaveable { mutableStateOf(atual?.roupa ?: -1) }
    var nome by rememberSaveable { mutableStateOf(atual?.nome.orEmpty()) }
    var traje by rememberSaveable { mutableStateOf(atual?.traje.orEmpty()) }
    var chapeu by rememberSaveable { mutableStateOf(atual?.chapeu.orEmpty()) }
    // Acessórios guardados como texto (simples de salvar na rotação da tela).
    var acessorios by rememberSaveable { mutableStateOf(Serializacao.acessoriosParaTexto(atual?.acessorios.orEmpty())) }
    var mostrarNome by rememberSaveable { mutableStateOf(false) }
    var mudancas by remember { mutableIntStateOf(0) }
    val pulo = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        narrador.falar(
            if (espelho) "Espelho, espelho meu! Escolha o cabelo, a pele, a roupa e os acessórios. Depois toque em pronto."
            else "Olá! Quem vai morar na casinha? Escolha um menino ou uma menina.",
        )
    }
    LaunchedEffect(mudancas) {
        if (mudancas > 0) {
            pulo.animateTo(-24f, tween(120))
            pulo.animateTo(0f, spring(dampingRatio = 0.35f))
        }
    }

    fun escolher(fala: String, mudar: () -> Unit) {
        mudar()
        mudancas++
        sons.toque()
        narrador.falar(fala)
    }

    val g = genero
    val perfil = Perfil(
        nome.trim(), g ?: Genero.MENINA, pele, cabelo, roupa, chapeu = chapeu, estiloCabelo = estilo, traje = traje,
        acessorios = Serializacao.textoParaAcessorios(acessorios),
    )

    /** Veste ou tira no provador: muda roupa, chapéu e acessórios de uma vez, e a criança pula. */
    fun provar(novo: Perfil) {
        traje = novo.traje
        chapeu = novo.chapeu
        acessorios = Serializacao.acessoriosParaTexto(novo.acessorios)
        mudancas++
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (onVoltar != null) {
            Box(Modifier.fillMaxWidth()) {
                BotaoRedondo("⬅️", "Voltar", onVoltar, Modifier.align(Alignment.CenterStart), tamanho = 52.dp)
                Text(if (espelho) "🪞" else "Casinha do Saber", style = MaterialTheme.typography.headlineLarge, color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
        } else {
            Text("Casinha do Saber", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        }
        Spacer(Modifier.height(8.dp))

        if (g == null) {
            // Ainda não escolheu: os dois cartões grandes, com a mãozinha mostrando.
            Spacer(Modifier.height(72.dp))
            Text("Quem vai morar na casinha?", style = MaterialTheme.typography.headlineMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            Box {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (opcao in Genero.entries) {
                        val rotulo = if (opcao == Genero.MENINO) "Menino" else "Menina"
                        // O cartão afunda um pouquinho ao tocar e volta com mola.
                        val toque = remember { MutableInteractionSource() }
                        val apertado by toque.collectIsPressedAsState()
                        val escala by animateFloatAsState(if (apertado) 0.94f else 1f, spring(dampingRatio = 0.4f), label = "cartao")
                        Column(
                            Modifier
                                .weight(1f)
                                .graphicsLayer { scaleX = escala; scaleY = escala }
                                .shadow(6.dp, RoundedCornerShape(28.dp))
                                .background(Color.White, RoundedCornerShape(28.dp))
                                .clickable(interactionSource = toque, indication = null, role = Role.RadioButton) { escolher("$rotulo!") { genero = opcao } }
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // A largura manda no tamanho: o braço que dá tchau fica dentro do cartão.
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(Brush.radialGradient(listOf(Color(0xFFE3F4FF), Color.White)), RoundedCornerShape(22.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Crianca(Perfil("", opcao, pele, cabelo, -1), Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp).pulsando(), acenando = true)
                            }
                            Text(rotulo, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
                MaoGuia(Modifier.align(Alignment.Center), espera = 2000)
            }
            // Embaixo, o terreno onde a casinha vai nascer (só desenho, para a tela não ficar vazia).
            Spacer(Modifier.height(18.dp))
            TerrenoDaCasinha(Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(28.dp)))
            return@Column
        }

        // Personagem grande, que pula a cada escolha. No espelho, fica dentro de uma moldura dourada.
        Box(
            Modifier
                .size(230.dp)
                .then(if (espelho) Modifier.border(8.dp, Color(0xFFD4A24C), CircleShape) else Modifier)
                .background(if (espelho) Color(0xFFDDF3FF) else Color.White.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Crianca(perfil, Modifier.height(190.dp).graphicsLayer { translationY = pulo.value * density }, acenando = true)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            for (opcao in Genero.entries) {
                val rotulo = if (opcao == Genero.MENINO) "Menino" else "Menina"
                Text(
                    if (opcao == Genero.MENINO) "👦" else "👧",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .background(if (opcao == g) Cores.Estrela else Color.White, CircleShape)
                        .semantics { contentDescription = rotulo }
                        .clickable { escolher("$rotulo!") { genero = opcao } }
                        .padding(10.dp),
                )
            }
        }

        Linha("🧑", "pele") {
            Aparencia.peles.forEachIndexed { i, c ->
                Bolinha(Color(c), i == pele, "Cor de pele ${i + 1}") { escolher("Que lindo!") { pele = i } }
            }
        }
        Linha("💇", "cor do cabelo") {
            Aparencia.cabelos.forEachIndexed { i, c ->
                Bolinha(Color(c), i == cabelo, "Cabelo ${nomesDoCabelo[i]}") { escolher("Cabelo ${nomesDoCabelo[i]}!") { cabelo = i } }
            }
        }
        Linha("✂️", "penteado") {
            val nomesEstilo = if (g == Genero.MENINA) listOf("liso", "cacheado", "trancinhas") else listOf("liso", "cacheado", "espetadinho")
            for (i in 0..2) {
                Box(
                    Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .border(if (i == estilo) 4.dp else 2.dp, if (i == estilo) Cores.Estrela else Color.White, CircleShape)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                        .semantics { contentDescription = "Cabelo ${nomesEstilo[i]}" }
                        .clickable { escolher("Cabelo ${nomesEstilo[i]}!") { estilo = i } },
                    contentAlignment = Alignment.TopCenter,
                ) {
                    // Só a cabeça: o personagem grande, recortado no círculo.
                    Crianca(perfil.copy(estiloCabelo = i, chapeu = ""), Modifier.height(110.dp).padding(top = 6.dp))
                }
            }
        }
        Linha("👕", "roupa") {
            val roupaAtual = if (roupa >= 0) roupa else if (g == Genero.MENINA) 1 else 0
            Aparencia.roupas.forEachIndexed { i, c ->
                Bolinha(Color(c), i == roupaAtual, "Roupa ${nomesDaRoupa[i]}") { escolher("Roupa ${nomesDaRoupa[i]}!") { roupa = i } }
            }
        }

        if (espelho) {
            // Provador: roupas, chapéus e acessórios do guarda-roupa, uma gaveta por parte do corpo.
            val possui: (Produto) -> Boolean = { p -> p in roupas || p.id in comprados }
            Spacer(Modifier.height(14.dp))
            Provador(
                perfil = perfil,
                possui = possui,
                onVestir = { p ->
                    if (possui(p)) {
                        sons.toque()
                        provar(perfil.vestindo(p))
                    }
                },
                onTirar = { gaveta -> provar(perfil.tirando(gaveta)) },
            )
        }

        Spacer(Modifier.height(12.dp))
        // O nome é com o adulto (a criança pequena ainda não escreve).
        if (mostrarNome) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = br.com.casinhadosaber.dominio.Familia.cortarNome(it) },
                label = { Text("Nome da criança (opcional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.titleLarge,
            )
        } else {
            Text(
                if (nome.isBlank()) "✏️ Adulto: escrever o nome" else "✏️ $nome",
                style = MaterialTheme.typography.titleMedium,
                color = Cores.TextoSuave,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .clickable { mostrarNome = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        Spacer(Modifier.height(20.dp))
        Box {
            BotaoFofo(
                onClick = {
                    sons.acerto()
                    onPronto(perfil.copy(genero = g))
                },
                cor = Cores.Certo,
                modifier = Modifier.pulsando(),
                descricao = "Pronto",
            ) {
                Text(
                    "Pronto! ✔",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 40.dp, vertical = 14.dp),
                )
            }
            MaoGuia(Modifier.align(Alignment.BottomEnd), espera = 9000)
        }
        Spacer(Modifier.height(24.dp))
    }
}

/** Uma linha de escolhas com um ícone na frente (sem depender de leitura). */
@Composable
private fun Linha(icone: String, descricao: String, conteudo: @Composable () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .semantics(mergeDescendants = false) { contentDescription = "Escolher $descricao" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icone, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.width(44.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { conteudo() }
    }
}

@Composable
private fun Bolinha(cor: Color, escolhida: Boolean, descricao: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .border(if (escolhida) 4.dp else 2.dp, if (escolhida) Cores.Estrela else Color.White, CircleShape)
            .padding(4.dp)
            .background(cor, CircleShape)
            .semantics { contentDescription = descricao }
            .clickable(role = Role.RadioButton, onClick = onClick),
    )
}

/**
 * O terreno vazio da primeira tela: morrinhos, um caminho de pedrinhas, flores e a casinha ainda tracejada,
 * com estrelinhas que piscam (é ali que ela vai nascer).
 */
@Composable
private fun TerrenoDaCasinha(modifier: Modifier) {
    val brilho by rememberInfiniteTransition(label = "terreno").animateFloat(
        0.35f, 1f, infiniteRepeatable(androidx.compose.animation.core.tween(1100), RepeatMode.Reverse), label = "brilho",
    )
    Canvas(modifier.semantics { contentDescription = "O terreno onde a casinha vai nascer" }) {
        val w = size.width
        val h = size.height
        // Morro de trás (mais claro) e o da frente.
        val tras = Path().apply {
            moveTo(0f, h * 0.55f)
            cubicTo(w * 0.25f, h * 0.38f, w * 0.55f, h * 0.5f, w * 0.75f, h * 0.42f)
            cubicTo(w * 0.88f, h * 0.37f, w, h * 0.45f, w, h * 0.45f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(tras, Color(0xFFA9DE8F))
        val frente = Path().apply {
            moveTo(0f, h * 0.72f)
            cubicTo(w * 0.3f, h * 0.6f, w * 0.7f, h * 0.62f, w, h * 0.7f)
            lineTo(w, h); lineTo(0f, h); close()
        }
        drawPath(frente, Brush.verticalGradient(listOf(Color(0xFF8ACF6E), Color(0xFF6DB85A)), startY = h * 0.6f, endY = h))
        // A casinha que ainda vai nascer: só o contorno tracejado, em cima do morro.
        val base = Offset(w * 0.5f, h * 0.56f)
        val lw = h * 0.42f
        val lh = h * 0.26f
        val casa = Path().apply {
            moveTo(base.x - lw / 2f, base.y)
            lineTo(base.x - lw / 2f, base.y - lh)
            lineTo(base.x, base.y - lh - h * 0.2f)
            lineTo(base.x + lw / 2f, base.y - lh)
            lineTo(base.x + lw / 2f, base.y)
            close()
        }
        drawPath(casa, Color.White.copy(alpha = 0.45f))
        drawPath(
            casa, Color.White,
            style = Stroke(h * 0.018f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(h * 0.05f, h * 0.035f))),
        )
        drawRoundRect(
            Color.White.copy(alpha = 0.8f), Offset(base.x - lw * 0.1f, base.y - lh * 0.55f), Size(lw * 0.2f, lh * 0.55f),
            androidx.compose.ui.geometry.CornerRadius(h * 0.02f), style = Stroke(h * 0.012f),
        )
        drawOval(Color(0x22000000), Offset(base.x - lw * 0.62f, base.y - h * 0.015f), Size(lw * 1.24f, h * 0.05f))
        // Caminho de pedrinhas até a porta.
        for (k in 0 until 5) {
            val t = k / 4f
            val c = Offset(base.x - w * 0.02f + t * w * 0.06f, base.y + h * 0.06f + t * h * 0.32f)
            val r = h * (0.025f + t * 0.025f)
            drawOval(Color(0xFFEFE6D2), Offset(c.x - r * 1.5f, c.y - r * 0.6f), Size(r * 3f, r * 1.2f))
        }
        // Florzinhas e moitinhas nos morros.
        val flores = listOf(0.08f to 0.8f, 0.2f to 0.74f, 0.33f to 0.86f, 0.72f to 0.8f, 0.86f to 0.74f, 0.93f to 0.9f, 0.62f to 0.92f)
        val cores = listOf(Color(0xFFFF7AA2), Color(0xFFFFD166), Color(0xFFFFFFFF), Color(0xFFB79CFF))
        for ((i, f) in flores.withIndex()) {
            val c = Offset(w * f.first, h * f.second)
            val r = h * 0.022f
            drawLine(Color(0xFF4E9A3E), c, Offset(c.x, c.y + r * 2.4f), r * 0.45f, StrokeCap.Round)
            for (k in 0 until 5) {
                val a = k * 1.2566f
                drawCircle(cores[i % cores.size], r * 0.7f, Offset(c.x + kotlin.math.cos(a) * r, c.y + kotlin.math.sin(a) * r))
            }
            drawCircle(Color(0xFFFFB13C), r * 0.55f, c)
        }
        for (u in listOf(0.14f, 0.82f)) {
            val c = Offset(w * u, h * (if (u < 0.5f) 0.6f else 0.55f))
            drawCircle(Color(0xFF5FAE4E), h * 0.06f, c)
            drawCircle(Color(0xFF5FAE4E), h * 0.05f, Offset(c.x - h * 0.06f, c.y + h * 0.02f))
            drawCircle(Color(0xFF5FAE4E), h * 0.05f, Offset(c.x + h * 0.06f, c.y + h * 0.02f))
            drawCircle(Color(0x33FFFFFF), h * 0.025f, Offset(c.x - h * 0.02f, c.y - h * 0.025f))
        }
        // Estrelinhas mágicas em volta da casinha.
        for ((k, e) in listOf(-0.36f to 0.18f, 0.34f to 0.12f, -0.2f to 0.02f, 0.22f to 0.36f).withIndex()) {
            val a = if (k % 2 == 0) brilho else 1.35f - brilho
            drawPath(caminhoEstrela(Offset(base.x + w * e.first, h * e.second + h * 0.08f), h * 0.04f), Color(0xFFFFE27A).copy(alpha = a.coerceIn(0f, 1f)))
        }
    }
}
