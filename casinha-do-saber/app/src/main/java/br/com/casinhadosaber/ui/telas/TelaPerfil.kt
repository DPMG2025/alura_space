package br.com.casinhadosaber.ui.telas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Aparencia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.dominio.Produto
import br.com.casinhadosaber.dominio.Serializacao
import br.com.casinhadosaber.dominio.tirando
import br.com.casinhadosaber.dominio.vestindo
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.CeuDaEntrada
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.IlhaAtras
import br.com.casinhadosaber.ui.componentes.LabioDoVerde
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.NuvensDoCeu
import br.com.casinhadosaber.ui.componentes.Provador
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.IconesDaCasaK2
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.TextoDoBotao
import br.com.casinhadosaber.ui.componentes.TituloDeBrinquedo
import br.com.casinhadosaber.ui.componentes.VerdeDoPronto
import br.com.casinhadosaber.ui.componentes.faceDeBrinquedo
import br.com.casinhadosaber.ui.componentes.pilula
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

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(CeuDaEntrada)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        val alturaDaTela = maxHeight
        NuvensDoCeu()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                // A primeira escolha fica no meio da tela (sem o rodapé vazio de antes); as outras rolam.
                .heightIn(min = alturaDaTela)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (g == null) Arrangement.Center else Arrangement.Top,
        ) {
            if (onVoltar != null) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    BotaoRedondo("⬅️", "Voltar", onVoltar, Modifier.align(Alignment.CenterStart), tamanho = 52.dp)
                    TituloDeBrinquedo(
                        if (espelho) "Espelho" else "Casinha do Saber",
                        Modifier.padding(horizontal = 60.dp),
                        estilo = MaterialTheme.typography.headlineMedium,
                        maxLinhas = 2,
                    )
                }
            } else {
                TituloDeBrinquedo("Casinha do Saber", maxLinhas = 1)
            }
            Spacer(Modifier.height(8.dp))

            if (g == null) {
                // Ainda não escolheu: o menino e a menina de pé na ilhinha, e um botão com o nome de cada um.
                EscolhaDoPersonagem(pele, cabelo, alturaDaTela) { opcao, rotulo -> escolher("$rotulo!") { genero = opcao } }
                return@Column
            }

            // Personagem grande, que pula a cada escolha, de pé na ilhinha. No espelho, dentro de uma moldura dourada.
            Box(Modifier.size(width = 260.dp, height = 236.dp), contentAlignment = Alignment.BottomCenter) {
                if (espelho) {
                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .size(230.dp)
                            .border(8.dp, Color(0xFFD4A24C), CircleShape)
                            .background(Color(0xFFDDF3FF), CircleShape),
                    )
                } else {
                    IlhaAtras(largura = 210.dp, pes = 40.dp)
                }
                Crianca(
                    perfil,
                    Modifier
                        .padding(bottom = if (espelho) 20.dp else 36.dp)
                        .height(190.dp)
                        .graphicsLayer { translationY = pulo.value * density },
                    acenando = true,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (opcao in Genero.entries) {
                    val rotulo = if (opcao == Genero.MENINO) "Menino" else "Menina"
                    BotaoFofo(
                        onClick = { escolher("$rotulo!") { genero = opcao } },
                        cor = if (opcao == g) Cores.Estrela else Color.White,
                        forma = RoundedCornerShape(50),
                        descricao = rotulo,
                        profundidade = 4.dp,
                    ) {
                        Text(
                            rotulo,
                            style = MaterialTheme.typography.titleMedium,
                            color = Cores.TextoDaPilula,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            Linha("🧑", "pele", IconesDaCasaK2.PELE) {
                Aparencia.peles.forEachIndexed { i, c ->
                    Bolinha(Color(c), i == pele, "Cor de pele ${i + 1}") { escolher("Que lindo!") { pele = i } }
                }
            }
            Linha("💇", "cor do cabelo", IconesDaCasaK2.COR_DO_CABELO) {
                Aparencia.cabelos.forEachIndexed { i, c ->
                    Bolinha(Color(c), i == cabelo, "Cabelo ${nomesDoCabelo[i]}") { escolher("Cabelo ${nomesDoCabelo[i]}!") { cabelo = i } }
                }
            }
            Linha("✂️", "penteado", IconesDaCasaK2.PENTEADO) {
                val nomesEstilo = if (g == Genero.MENINA) listOf("liso", "cacheado", "trancinhas") else listOf("liso", "cacheado", "espetadinho")
                for (i in 0..2) {
                    val escolhido = i == estilo
                    Box(
                        Modifier
                            .size(62.dp)
                            .shadow(if (escolhido) 6.dp else 3.dp, CircleShape)
                            .clip(CircleShape)
                            .faceDeBrinquedo(if (escolhido) Color(0xFFFFF3C4) else Color.White, CircleShape, redonda = true)
                            .border(if (escolhido) 4.dp else 2.dp, if (escolhido) Cores.Estrela else Color.White, CircleShape)
                            .semantics { contentDescription = "Cabelo ${nomesEstilo[i]}" }
                            .clickable { escolher("Cabelo ${nomesEstilo[i]}!") { estilo = i } },
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        // Só a cabeça: o personagem grande, recortado no círculo.
                        Crianca(perfil.copy(estiloCabelo = i, chapeu = ""), Modifier.height(110.dp).padding(top = 6.dp), animado = false)
                    }
                }
            }
            Linha("👕", "roupa", IconesDaCasaK2.ROUPA) {
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

            Spacer(Modifier.height(14.dp))
            // O nome é com o adulto (a criança pequena ainda não escreve).
            if (mostrarNome) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = br.com.casinhadosaber.dominio.Familia.cortarNome(it) },
                    label = { Text("Nome da criança (opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp)),
                    textStyle = MaterialTheme.typography.titleLarge,
                )
            } else {
                // O lápis 3D e o texto (o mesmo de antes, sem o ✏️ de sistema); o toque junta os dois num nó só.
                val textoDoNome = if (nome.isBlank()) "Adulto: escrever o nome" else nome
                Row(
                    Modifier
                        .pilula()
                        .clickable { mostrarNome = true }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ImagemDeArte(IconesDaBarra.ESCREVER, Modifier.size(28.dp)) { Text("✏️", style = MaterialTheme.typography.titleMedium) }
                    Text(
                        textoDoNome,
                        style = MaterialTheme.typography.titleMedium,
                        color = Cores.TextoDaPilula,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(22.dp))
            Box {
                BotaoFofo(
                    onClick = {
                        sons.acerto()
                        onPronto(perfil.copy(genero = g))
                    },
                    cor = VerdeDoPronto,
                    corBase = LabioDoVerde,
                    modifier = Modifier.pulsando(),
                    descricao = "Pronto",
                ) {
                    TextoDoBotao("Pronto!", Modifier.padding(horizontal = 40.dp, vertical = 10.dp))
                }
                MaoGuia(Modifier.align(Alignment.BottomEnd), espera = 9000)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * A primeira escolha: o menino e a menina de pé numa ilhinha de grama no céu, dando tchau (tocar num deles
 * escolhe), e embaixo um botão com o nome de cada um (o adulto lê; os testes do emulador tocam em "Menina").
 * A mãozinha aponta o botão da menina depois de um tempinho sem toque.
 */
@Composable
private fun EscolhaDoPersonagem(pele: Int, cabelo: Int, alturaDaTela: Dp, onEscolher: (Genero, String) -> Unit) {
    Spacer(Modifier.height(8.dp))
    TituloDeBrinquedo("Quem vai morar na casinha?", Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    BoxWithConstraints(Modifier.fillMaxWidth().widthIn(max = 460.dp)) {
        val largura = maxWidth
        // Os bonecos crescem com a tela, sem passar da largura de cada metade.
        val alturaDoBoneco = minOf((alturaDaTela * 0.32f).coerceIn(150.dp, 280.dp), largura * 0.6f)
        val ilha = largura * 0.92f
        // Quanto da ilha fica embaixo dos pés (da grama até a base da terra).
        val sobIlha = ilha / 0.77f * 0.2f
        Box(Modifier.fillMaxWidth().height(alturaDoBoneco + sobIlha + 4.dp)) {
            IlhaAtras(ilha, pes = sobIlha)
            Row(
                Modifier.align(Alignment.BottomCenter).padding(bottom = sobIlha - 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                for (opcao in Genero.entries) {
                    val rotulo = if (opcao == Genero.MENINO) "Menino" else "Menina"
                    // O boneco afunda um pouquinho ao tocar e volta com mola.
                    val toque = remember { MutableInteractionSource() }
                    val apertado by toque.collectIsPressedAsState()
                    val escala by animateFloatAsState(if (apertado) 0.94f else 1f, spring(dampingRatio = 0.4f), label = "boneco")
                    Box(
                        Modifier
                            .width(largura * 0.44f)
                            .height(alturaDoBoneco)
                            .graphicsLayer { scaleX = escala; scaleY = escala; transformOrigin = TransformOrigin(0.5f, 1f) }
                            .semantics { contentDescription = rotulo }
                            .clickable(interactionSource = toque, indication = null, role = Role.RadioButton) { onEscolher(opcao, rotulo) },
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Crianca(Perfil("", opcao, pele, cabelo, -1), Modifier.height(alturaDoBoneco).pulsando(), acenando = true)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(6.dp))
    Row(Modifier.fillMaxWidth().widthIn(max = 460.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        for (opcao in Genero.entries) {
            val rotulo = if (opcao == Genero.MENINO) "Menino" else "Menina"
            Box(Modifier.weight(1f)) {
                BotaoFofo(
                    onClick = { onEscolher(opcao, rotulo) },
                    modifier = Modifier.fillMaxWidth(),
                    cor = if (opcao == Genero.MENINO) Color(0xFF4FA8F5) else Color(0xFFFF7EB3),
                    corBase = if (opcao == Genero.MENINO) Color(0xFF2B78C9) else Color(0xFFD94F8A),
                    descricao = rotulo,
                ) {
                    TextoDoBotao(rotulo, Modifier.padding(vertical = 8.dp), estilo = MaterialTheme.typography.headlineSmall)
                }
                if (opcao == Genero.MENINA) MaoGuia(Modifier.align(Alignment.BottomEnd).offset(x = (-8).dp, y = 30.dp), espera = 2500)
            }
        }
    }
    Spacer(Modifier.height(24.dp))
}

/** Uma linha de escolhas com um ícone na frente (sem depender de leitura). */
@Composable
private fun Linha(icone: String, descricao: String, idDoIcone: String, conteudo: @Composable () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .semantics(mergeDescendants = false) { contentDescription = "Escolher $descricao" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // O ícone num botãozinho redondo claro, como os da barra (decorativo: a linha já diz o que é).
        Box(Modifier.width(44.dp)) {
            Box(
                Modifier
                    .size(38.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .faceDeBrinquedo(Color.White, CircleShape, redonda = true)
                    .clearAndSetSemantics { },
                contentAlignment = Alignment.Center,
            ) {
                // O ícone 3D da linha (pelo que a linha escolhe, não pelo emoji: 👕 também é conteúdo).
                ImagemDeArte(idDoIcone, Modifier.size(30.dp)) { Text(icone, fontSize = 20.sp) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { conteudo() }
    }
}

/** Uma conta de plástico da cor: brilho em cima à esquerda, borda branca; a escolhida ganha o anel amarelo. */
@Composable
private fun Bolinha(cor: Color, escolhida: Boolean, descricao: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .shadow(if (escolhida) 5.dp else 2.dp, CircleShape)
            .background(if (escolhida) Cores.Estrela else Color.White, CircleShape)
            .padding(if (escolhida) 4.dp else 3.dp)
            .drawBehind {
                drawCircle(
                    Brush.radialGradient(
                        0f to Color.White.copy(alpha = 0.55f).compositeSobre(cor),
                        0.45f to cor,
                        1f to cor.escurecida(),
                        center = Offset(size.width * 0.35f, size.height * 0.3f),
                        radius = size.width * 0.8f,
                    ),
                )
            }
            .semantics { contentDescription = descricao }
            .clickable(role = Role.RadioButton, onClick = onClick),
    )
}

private fun Color.compositeSobre(fundo: Color): Color =
    Color(fundo.red + (red - fundo.red) * alpha, fundo.green + (green - fundo.green) * alpha, fundo.blue + (blue - fundo.blue) * alpha)

private fun Color.escurecida(): Color = Color(red * 0.78f, green * 0.78f, blue * 0.78f, alpha)
