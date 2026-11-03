package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.FiguraDoLivrinho
import br.com.casinhadosaber.dominio.Leitura
import br.com.casinhadosaber.dominio.Livrinho
import br.com.casinhadosaber.dominio.Livrinhos
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.Confete
import br.com.casinhadosaber.ui.componentes.Corujinha
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Papel = Color(0xFFFFFBF2)
private val MadeiraEscura = Color(0xFF8A5A36)

/** Cores das lombadas, girando pela estante (fixas, sem sorteio). */
private val coresDosLivros = listOf(
    0xFFFF8A80, 0xFFFFC94D, 0xFF7FD48B, 0xFF6FB7FF, 0xFFB79CFF, 0xFFFF9FD0, 0xFF5FD0C7, 0xFFFFAB66,
).map { Color(it) }

/**
 * "Minha estante": os livrinhos para ler sozinho, do mais fácil ao mais difícil, em prateleiras de madeira.
 * Lido = com o marcador; aberto = colorido; fechado = esmaecido com um cadeado e a fase de Letras que abre.
 * Marcas de teste: "livrinho_<id>" em cada livro e "estante_voltar".
 */
@Composable
fun TelaEstante(estado: EstadoJogo, onAbrir: (Livrinho) -> Unit, onVoltar: () -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val livros = Livrinhos.todos
    val lidos = livros.filter { it.id in estado.livrinhosLidos }
    val sugerido = Livrinhos.sugerido(estado)
    LaunchedEffect(Unit) {
        narrador.falar(
            when {
                sugerido != null -> "Minha estante! Toque num livrinho para ler sozinho. Que tal ${sugerido.titulo}?"
                lidos.isEmpty() -> "Minha estante! Os livrinhos abrem quando você aprende as sílabas no caminho das Letras."
                else -> "Minha estante! Você já leu ${lidos.size} livrinhos. Os próximos abrem no caminho das Letras."
            },
        )
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFE3C2), Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            BotaoRedondo("⬅️", "Voltar", onVoltar, Modifier.testTag("estante_voltar"), tamanho = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("📚 Minha estante", style = MaterialTheme.typography.headlineMedium, color = Cores.Texto)
                Text("${lidos.size} de ${livros.size} livrinhos lidos", style = MaterialTheme.typography.titleMedium, color = Cores.TextoSuave)
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
        ) {
            // Os marcadores ganhos, numa fileira (a recompensa de terminar cada livrinho).
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .semantics(mergeDescendants = true) { contentDescription = "Marcadores: ${lidos.size}" },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("🔖", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(8.dp))
                if (lidos.isEmpty()) {
                    Text("Leia um livrinho até o fim para ganhar um marcador.", style = MaterialTheme.typography.bodyLarge, color = Cores.TextoSuave)
                } else {
                    Text(lidos.joinToString(" ") { it.marcador }, style = MaterialTheme.typography.headlineSmall)
                }
            }
            Spacer(Modifier.height(16.dp))
            for ((i, fileira) in livros.chunked(3).withIndex()) {
                Prateleira {
                    for ((j, livro) in fileira.withIndex()) {
                        val n = i * 3 + j
                        CapaDoLivrinho(
                            livro = livro,
                            cor = coresDosLivros[n % coresDosLivros.size],
                            lido = livro.id in estado.livrinhosLidos,
                            aberto = Livrinhos.liberado(livro, estado),
                            sugerido = livro == sugerido,
                            modifier = Modifier.weight(1f),
                        ) {
                            if (Livrinhos.liberado(livro, estado)) {
                                sons.toque()
                                onAbrir(livro)
                            } else {
                                sons.erro()
                                narrador.falar("Esse livrinho abre na fase ${livro.fase + 1} do caminho das Letras. Falta pouquinho!")
                            }
                        }
                    }
                    repeat(3 - fileira.size) { Spacer(Modifier.weight(1f)) }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun Prateleira(conteudo: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
            conteudo()
        }
        // A tábua de madeira da prateleira.
        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(Brush.verticalGradient(listOf(Cores.Madeira, MadeiraEscura)), RoundedCornerShape(6.dp)),
        )
    }
}

@Composable
private fun CapaDoLivrinho(
    livro: Livrinho,
    cor: Color,
    lido: Boolean,
    aberto: Boolean,
    sugerido: Boolean,
    modifier: Modifier,
    onTocar: () -> Unit,
) {
    val descricao = when {
        lido -> "Livrinho ${livro.titulo}, já lido"
        aberto -> "Livrinho ${livro.titulo}"
        else -> "Livrinho ${livro.titulo}, abre na fase ${livro.fase + 1} das Letras"
    }
    BotaoFofo(
        onClick = onTocar,
        cor = if (aberto) cor else Color(0xFFE6E0EE),
        corBase = if (aberto) br.com.casinhadosaber.ui.componentes.escurecer(cor) else Color(0xFFC9C0D6),
        forma = RoundedCornerShape(topStart = 8.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 8.dp),
        descricao = descricao,
        modifier = modifier.testTag("livrinho_${livro.id}").then(if (sugerido) Modifier.pulsando() else Modifier),
    ) {
        Box(Modifier.fillMaxWidth().height(150.dp)) {
            // Lombada à esquerda, como um livro de verdade.
            Box(Modifier.width(10.dp).height(150.dp).background(Color.Black.copy(alpha = 0.12f)))
            Column(
                Modifier.fillMaxSize().padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 6.dp).alpha(if (aberto) 1f else 0.55f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(livro.capa, fontSize = 44.sp, lineHeight = 50.sp)
                Text(
                    livro.titulo,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 18.sp),
                    color = if (aberto) Color.White else Cores.TextoSuave,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                )
            }
            when {
                lido -> Text(
                    livro.marcador,
                    fontSize = 22.sp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).background(Color.White, CircleShape).padding(4.dp),
                )
                !aberto -> Text("🔒", fontSize = 22.sp, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
            }
        }
    }
}

/** Em que parte do livrinho a criança está. */
private sealed interface Parte {
    data class Pagina(val n: Int) : Parte
    data class Pergunta(val n: Int) : Parte
    data object Fim : Parte
}

/**
 * Um livrinho aberto. Cada página: a cena, a frase em letras grandes (tocar numa palavra lê a palavra, segurar
 * lê pedacinho por pedacinho), "Ler para mim" e as setas de virar a página. Depois, 1 ou 2 perguntas de tocar
 * na figura certa (errar só faz tentar de novo; com 2 erros a certa brilha) e o marcador para a estante.
 * [onTerminar] é chamado uma vez, ao chegar no fim.
 * Marcas de teste: "palavra_<i>", "ler_para_mim", "pagina_seguinte", "pagina_anterior", "resposta_certa",
 * "resposta", "livrinho_estante".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TelaLivrinho(
    livro: Livrinho,
    nome: String?,
    onTerminar: () -> Unit,
    onVoltar: () -> Unit,
    /** Começar numa parte (para as capturas): -1 = primeira página; de 0 a n-1 a página; n em diante as perguntas. */
    inicio: Int = -1,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var parte by remember(livro.id) {
        mutableStateOf<Parte>(
            when {
                inicio < 0 -> Parte.Pagina(0)
                inicio < livro.paginas.size -> Parte.Pagina(inicio)
                inicio - livro.paginas.size < livro.perguntas.size -> Parte.Pergunta(inicio - livro.paginas.size)
                else -> Parte.Fim
            },
        )
    }
    val cor = coresDosLivros[Livrinhos.todos.indexOf(livro).coerceAtLeast(0) % coresDosLivros.size]
    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cor.copy(alpha = 0.55f), Papel)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BotaoRedondo("✖️", "Fechar o livrinho", { narrador.parar(); onVoltar() }, tamanho = 48.dp)
            Spacer(Modifier.width(10.dp))
            Text(
                livro.titulo,
                style = MaterialTheme.typography.titleLarge,
                color = Cores.Texto,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            val p = parte
            if (p is Parte.Pagina) {
                Text("${p.n + 1}/${livro.paginas.size}", style = MaterialTheme.typography.titleMedium, color = Cores.TextoSuave)
            }
        }
        Spacer(Modifier.height(10.dp))
        when (val p = parte) {
            is Parte.Pagina -> PaginaAberta(
                livro = livro,
                n = p.n,
                onAnterior = { if (p.n > 0) { narrador.parar(); sons.toque(); parte = Parte.Pagina(p.n - 1) } },
                onSeguinte = {
                    narrador.parar()
                    sons.toque()
                    parte = if (p.n + 1 < livro.paginas.size) Parte.Pagina(p.n + 1) else Parte.Pergunta(0)
                },
            )
            is Parte.Pergunta -> PerguntaAberta(livro, p.n, nome) {
                parte = if (p.n + 1 < livro.perguntas.size) Parte.Pergunta(p.n + 1) else Parte.Fim
            }
            Parte.Fim -> FimDoLivrinho(livro, nome, onTerminar, onVoltar)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun androidx.compose.foundation.layout.ColumnScope.PaginaAberta(
    livro: Livrinho,
    n: Int,
    onAnterior: () -> Unit,
    onSeguinte: () -> Unit,
) {
    val narrador = LocalNarrador.current
    val pagina = livro.paginas[n]
    // Na primeira página, a Corujinha lembra como funciona (só uma vez por livrinho aberto).
    LaunchedEffect(livro.id, n) {
        if (n == 0) narrador.falar("${livro.titulo}. Toque numa palavra para ouvir. Segure para ouvir pedacinho por pedacinho.")
    }
    CenaDoLivrinho(pagina.cenario, pagina.figuras, Modifier.fillMaxWidth().weight(1f).heightIn(min = 160.dp))
    Spacer(Modifier.height(14.dp))
    // A frase, em letras grandes: cada palavra é um botão.
    val palavras = pagina.texto.split(" ")
    FlowRow(
        Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for ((i, escrita) in palavras.withIndex()) {
            val limpa = Leitura.palavras(escrita).firstOrNull() ?: escrita
            Palavra(escrita, limpa, Modifier.testTag("palavra_$i"))
        }
    }
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BotaoFofo(
            onClick = onAnterior,
            cor = Color.White,
            corBase = Color(0x33000000),
            descricao = "Página anterior",
            habilitado = n > 0,
            modifier = Modifier.testTag("pagina_anterior").alpha(if (n > 0) 1f else 0.35f),
        ) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) { Text("⬅️", fontSize = 28.sp) }
        }
        BotaoFofo(
            onClick = { narrador.falar(pagina.texto) },
            cor = Color(0xFFEDE6FF),
            corBase = Cores.Roxo,
            descricao = "Ler para mim",
            modifier = Modifier.weight(1f).testTag("ler_para_mim"),
        ) {
            Row(Modifier.heightIn(min = 64.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("🔊", fontSize = 26.sp)
                Spacer(Modifier.width(6.dp))
                Text("Ler para mim", style = MaterialTheme.typography.titleLarge, color = Cores.Roxo, maxLines = 1)
            }
        }
        BotaoFofo(
            onClick = onSeguinte,
            cor = Cores.Certo,
            descricao = "Virar a página",
            modifier = Modifier.testTag("pagina_seguinte"),
        ) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) { Text("➡️", fontSize = 28.sp) }
        }
    }
}

/** Uma palavra da frase: tocar lê a palavra; segurar lê os pedacinhos e depois a palavra inteira. */
@Composable
private fun Palavra(escrita: String, limpa: String, modifier: Modifier) {
    val narrador = LocalNarrador.current
    var tocada by remember { mutableIntStateOf(0) }
    fun ler() { tocada++; narrador.falar(limpa.lowercase()) }
    fun lerPedacinhos() { tocada++; narrador.falar(Leitura.falaDosPedacinhos(limpa).map { pt(it) }) }
    Text(
        escrita,
        fontSize = 34.sp,
        lineHeight = 42.sp,
        fontWeight = FontWeight.Bold,
        color = Cores.Texto,
        modifier = modifier
            .background(if (tocada > 0) Color(0xFFFFF3C4) else Color.Transparent, RoundedCornerShape(10.dp))
            .heightIn(min = 48.dp)
            .padding(horizontal = 4.dp)
            .semantics {
                contentDescription = limpa
                role = Role.Button
                onClick(label = "Ouvir a palavra") { ler(); true }
                onLongClick(label = "Ouvir pedacinho por pedacinho") { lerPedacinhos(); true }
            }
            .pointerInput(limpa) { detectTapGestures(onTap = { ler() }, onLongPress = { lerPedacinhos() }) },
    )
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.PerguntaAberta(livro: Livrinho, n: Int, nome: String?, onCerta: () -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val escopo = rememberCoroutineScope()
    val q = livro.perguntas[n]
    // A certa muda de lugar de um livrinho e de uma pergunta para outra, sem sorteio.
    val opcoes = remember(livro.id, n) { q.opcoes(Livrinhos.todos.indexOf(livro) + n) }
    var erradas by remember(livro.id, n) { mutableStateOf(setOf<String>()) }
    var acertou by remember(livro.id, n) { mutableStateOf(false) }
    var confete by remember { mutableIntStateOf(0) }
    LaunchedEffect(livro.id, n) { narrador.falar(q.pergunta) }
    fun tocar(f: FiguraDoLivrinho) {
        if (acertou || f.emoji in erradas) return
        if (f == q.certa) {
            acertou = true
            confete++
            sons.acerto()
            val elogio = br.com.casinhadosaber.dominio.FrasesComNome.elogio(nome, n)
            narrador.falar(listOf(pt(f.nome), pt(elogio)))
            escopo.launch {
                delay(2200)
                onCerta()
            }
        } else {
            erradas = erradas + f.emoji
            sons.erro()
            narrador.falar(listOf(pt(f.nome), pt(if (erradas.size >= 2) "Olhe a que está brilhando." else "Tente outra vez!")))
        }
    }
    Box(Modifier.fillMaxWidth().weight(1f)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Corujinha(Modifier.height(96.dp), falando = narrador.falando)
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .weight(1f)
                        .shadow(4.dp, RoundedCornerShape(22.dp))
                        .background(Color.White, RoundedCornerShape(22.dp))
                        .semantics(mergeDescendants = true) { onClick(label = "Ouvir de novo") { narrador.falar(q.pergunta); true } }
                        .pointerInput(q.pergunta) { detectTapGestures(onTap = { narrador.falar(q.pergunta) }) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text("🔊 " + q.pergunta, style = MaterialTheme.typography.titleLarge, color = Cores.Texto)
                }
            }
            for (f in opcoes) {
                val certa = f == q.certa
                val brilha = certa && (acertou || erradas.size >= 2)
                BotaoFofo(
                    onClick = { tocar(f) },
                    cor = when {
                        brilha -> Color(0xFFDFF7E6)
                        f.emoji in erradas -> Color(0xFFF1EDF6)
                        else -> Color.White
                    },
                    corBase = if (brilha) Cores.Certo else Color(0xFFD9CFF0),
                    descricao = f.nome,
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .fillMaxWidth()
                        .testTag(if (certa) "resposta_certa" else "resposta")
                        .then(if (brilha && !acertou) Modifier.pulsando() else Modifier)
                        .border(if (brilha) 4.dp else 0.dp, if (brilha) Cores.Certo else Color.Transparent, RoundedCornerShape(24.dp)),
                ) {
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 96.dp).padding(horizontal = 18.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(f.emoji, fontSize = 52.sp, lineHeight = 60.sp, modifier = Modifier.alpha(if (f.emoji in erradas) 0.45f else 1f))
                    }
                }
            }
        }
        Confete(confete, Modifier.fillMaxSize())
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.FimDoLivrinho(livro: Livrinho, nome: String?, onTerminar: () -> Unit, onVoltar: () -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var confete by remember { mutableIntStateOf(0) }
    LaunchedEffect(livro.id) {
        onTerminar()
        sons.premio()
        confete++
        val quem = if (nome.isNullOrBlank()) "Você" else nome
        narrador.falar("$quem leu o livrinho ${livro.titulo}! O marcador vai para a sua estante.")
    }
    Box(Modifier.fillMaxWidth().weight(1f)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
            Text("Você leu sozinho!", style = MaterialTheme.typography.headlineMedium, color = Cores.Texto, textAlign = TextAlign.Center)
            // O marcador de página ganho, grande, numa fitinha.
            Box(
                Modifier
                    .size(width = 140.dp, height = 190.dp)
                    .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFFFFE08A), Color(0xFFFFB84D))), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .semantics { contentDescription = "Marcador ${livro.titulo}" },
                contentAlignment = Alignment.Center,
            ) {
                Text(livro.marcador, fontSize = 72.sp, modifier = Modifier.pulsando())
            }
            Text("🔖 Novo marcador na estante", style = MaterialTheme.typography.titleLarge, color = Cores.Roxo)
            BotaoFofo(onClick = { narrador.parar(); sons.toque(); onVoltar() }, cor = Cores.Certo, descricao = "Ver a estante", modifier = Modifier.testTag("livrinho_estante")) {
                Text("📚 Ver a estante", style = MaterialTheme.typography.headlineSmall, color = Color.White, modifier = Modifier.padding(horizontal = 26.dp, vertical = 16.dp))
            }
        }
        Confete(confete, Modifier.fillMaxSize())
    }
}

/** Botão da estante (no início e no caminho das Letras). */
@Composable
fun BotaoEstante(onClick: () -> Unit, modifier: Modifier = Modifier, tamanho: androidx.compose.ui.unit.Dp = 52.dp) {
    val sons = LocalSons.current
    BotaoRedondo("📚", "Minha estante de livrinhos", { sons.toque(); onClick() }, modifier.testTag("abrir_estante"), tamanho = tamanho)
}
