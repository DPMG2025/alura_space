package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.Estrela
import br.com.casinhadosaber.ui.componentes.EstrelasDeProgresso
import br.com.casinhadosaber.ui.componentes.ID_ESTRELA_CHEIA
import br.com.casinhadosaber.ui.componentes.IconeDeTrilha
import br.com.casinhadosaber.ui.componentes.IconesDaBarra
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.MaoGuia
import br.com.casinhadosaber.ui.componentes.PilulaDeValor
import br.com.casinhadosaber.ui.componentes.ProgressoVisivel
import br.com.casinhadosaber.ui.componentes.TextoComContorno
import br.com.casinhadosaber.ui.componentes.faceDeBrinquedo
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores

/** Conta as chegadas ao início (só enquanto o app está aberto) para variar a saudação sem sorteio. */
private var chegadasNoInicio = 0

@Composable
fun TelaInicio(
    estado: EstadoJogo,
    somLigado: Boolean,
    onAlternarSom: () -> Unit,
    onAbrirCasa: () -> Unit,
    onAbrirTrilha: (Trilha) -> Unit,
    onAreaDosPais: () -> Unit,
    missaoDisponivel: Boolean = false,
    onMissao: () -> Unit = {},
    /** Espelho 🪞: abre o perfil para trocar a aparência e volta para o início. */
    onEspelho: () -> Unit = {},
    /** Quantas fases estão para relembrar hoje: com alguma, a Corujinha aparece no canto da casa (nunca abre sozinha). */
    relembrar: Int = 0,
    onRelembrar: () -> Unit = {},
    /** Estante dos livrinhos para ler sozinho (null = sem o botão 📚). */
    onEstante: (() -> Unit)? = null,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val nome = estado.perfil?.nome.orEmpty()
    val noBau = estado.itensNoBau.size

    LaunchedEffect(Unit) {
        val oi = br.com.casinhadosaber.dominio.FrasesComNome.chegada(nome, chegadasNoInicio++)
        val resto = when {
            noBau > 0 -> "Tem presente no baú! Toque na casinha para montar."
            missaoDisponivel -> "Tem missão do dia! Toque no pergaminho para ganhar moedinhas."
            relembrar > 0 -> "A Corujinha quer relembrar umas coisinhas com você. É só tocar nela, se quiser."
            estado.fasesVencidas == 0 -> "Vamos construir a sua casinha? Escolha um caminho para jogar."
            else -> "Escolha um caminho para ganhar mais coisas para a sua casa!"
        }
        // A saudação ("Oi, Ana!") e "Que bom ver você!" podem sair com a voz gravada da família.
        narrador.falarPartes(br.com.casinhadosaber.dominio.VozesDaFamilia.saudacao(oi) + br.com.casinhadosaber.dominio.ParteDaFala(pt(resto)))
    }

    // Maquete 1: céu em degradê (azul em cima, pêssego e rosa embaixo) com a casa na ilha de grama, sem moldura.
    Box(Modifier.fillMaxSize().background(CeuDoInicio)) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
    // Tela deitada (tablet em paisagem): a casa à esquerda e os quatro caminhos numa coluna à direita.
    val deitada = maxWidth > maxHeight * 1.15f
    // Tela baixa (celular pequeno, tablet deitado): o título vai na linha dos botões, sem a pílula das estrelas.
    val baixa = deitada || maxHeight < 700.dp
    val tituloGrande = maxWidth >= 380.dp
    val totalDeEstrelas = estado.estrelas.values.sum()
    Column(Modifier.fillMaxSize()) {
        // Cabeçalho: ajustes (área dos pais), as estrelas ganhas numa pílula e o som; o título embaixo.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BotaoAdulto(onAreaDosPais)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (baixa) {
                    TextoComContorno("Casinha do Saber", estilo = MaterialTheme.typography.headlineMedium, espessura = 5.dp)
                } else {
                    PilulaDeValor(
                        ID_ESTRELA_CHEIA,
                        "$totalDeEstrelas",
                        descricao = "Você já ganhou $totalDeEstrelas estrelas",
                    ) { Estrela(true, tamanho = 26.dp) }
                }
            }
            BotaoRedondo(
                emoji = if (somLigado) "🔊" else "🔇",
                descricao = if (somLigado) "Desligar som" else "Ligar som",
                onClick = onAlternarSom,
                tamanho = 48.dp,
            )
        }
        if (!baixa) {
            // Título de brinquedo: Baloo 2 branca com contorno roxo e lábio embaixo (maquete 1).
            TextoComContorno(
                "Casinha do Saber",
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                estilo = if (tituloGrande) MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp, lineHeight = 42.sp) else MaterialTheme.typography.headlineLarge,
                espessura = 6.dp,
            )
        }

        // A casinha na ilha (toque para montar): a cena sem céu (o céu é o da tela) e sem moldura.
        @Composable
        fun Casinha(modifier: Modifier) {
            Box(
                modifier
                    .semantics {
                        contentDescription = "Minha casinha. Toque para montar."
                        role = Role.Button
                        onClick { onAbrirCasa(); true }
                    }
                    .clickable { sons.toque(); onAbrirCasa() },
            ) {
                CenaDaCasa(estado, Modifier.fillMaxSize(), preencherAltura = true, semCeu = true)
                if (!deitada) {
                    // A grama se desfaz no rosado do céu de baixo, onde ficam os caminhos (como na maquete).
                    Spacer(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, RosaDoChao))),
                    )
                }
                Column(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (estado.perfil != null) {
                        BotaoRedondo(
                            "🪞",
                            "Espelho: trocar a aparência",
                            { sons.toque(); onEspelho() },
                            tamanho = 52.dp,
                        )
                    }
                    if (onEstante != null) BotaoEstante(onEstante)
                }
                if (relembrar > 0) {
                    // Relembrar: a Corujinha no canto de cima (embaixo da missão, quando há missão).
                    BotaoRelembrar(
                        relembrar,
                        onRelembrar,
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = if (missaoDisponivel) 72.dp else 10.dp, end = 10.dp),
                        comTexto = false,
                    )
                }
                if (missaoDisponivel) {
                    // Missão do dia: um pergaminho que pula no canto da casa, valendo moedinhas.
                    BotaoFofo(
                        onClick = { sons.toque(); onMissao() },
                        cor = Color(0xFFFFF3C4),
                        corBase = Color(0xFFE0A800),
                        descricao = "Missão do dia, vale moedinhas",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .pulsando(),
                    ) {
                        Text("📜 +${EstadoJogo.MOEDAS_DA_MISSAO} 🪙", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
                    }
                }
                if (noBau > 0) {
                    // O aviso e a mãozinha ficam juntos: a mão aponta para o aviso.
                    Box(Modifier.align(Alignment.TopCenter).padding(top = 10.dp)) {
                        Box(
                            Modifier
                                .pulsando()
                                .background(Cores.Estrela, RoundedCornerShape(50))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                if (noBau == 1) "🎁 1 presente!" else "🎁 $noBau presentes!",
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        MaoGuia(Modifier.align(Alignment.BottomEnd).offset(x = 28.dp, y = 40.dp), espera = 2500)
                    }
                }
            }
        }

        @Composable
        fun Caminho(t: Trilha, modifier: Modifier, alto: Boolean) {
            Box(modifier) {
                BotaoTrilha(t, estado.vencidasNa(t), Modifier.fillMaxSize(), alto) {
                    sons.toque()
                    onAbrirTrilha(t)
                }
                // Primeira vez: a mãozinha mostra por onde começar.
                if (estado.fasesVencidas == 0 && t == Trilha.MATEMATICA) {
                    MaoGuia(Modifier.align(Alignment.BottomEnd).offset(x = 8.dp, y = 12.dp), espera = 2500)
                }
            }
        }

        if (deitada) {
            Row(Modifier.weight(1f).fillMaxWidth().padding(bottom = 12.dp)) {
                Casinha(Modifier.weight(1f).fillMaxHeight())
                Column(
                    Modifier.width(LARGURA_DOS_CAMINHOS_DEITADA).fillMaxHeight().padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    for (t in Trilha.entries) Caminho(t, Modifier.weight(1f).fillMaxWidth(), alto = false)
                }
            }
        } else {
            Casinha(Modifier.weight(1f).fillMaxWidth())
            // Caminhos
            // Em telas altas, os botões dos caminhos crescem (mais fáceis de acertar com o dedo).
            val alto = LocalConfiguration.current.screenHeightDp > 760
            val alturaBotao = if (alto) 118.dp else 88.dp
            Column(
                Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (linha in Trilha.entries.chunked(2)) {
                    Row(Modifier.height(alturaBotao), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (t in linha) Caminho(t, Modifier.weight(1f).fillMaxHeight(), alto)
                    }
                }
            }
        }
    }
    }
    }
}

/** Céu da tela inicial (maquete 1): azul em cima, clarinho no meio, pêssego e rosa embaixo. */
private val CeuDoInicio = Brush.verticalGradient(
    0f to Color(0xFF7CC4FA),
    0.38f to Color(0xFFB8E2FF),
    0.66f to Color(0xFFFFE6D6),
    1f to Color(0xFFFFD0DC),
)

/** O rosado de baixo, onde a grama se desfaz atrás dos caminhos. */
private val RosaDoChao = Color(0xFFFFDCDC)

/** Quantas estrelas cada caminho mostra (a última acende só com o caminho inteiro). */
private const val ESTRELAS_DO_CAMINHO = 5

/** Largura da coluna dos caminhos quando a tela está deitada. */
private val LARGURA_DOS_CAMINHOS_DEITADA = 320.dp

/**
 * O bloco do caminho (maquete 1): o ícone 3D grande no canto de cima, o nome em branco com contorno e o progresso
 * em estrelas que se veem (5 por caminho), nunca "3/250". O número fica só na descrição, para o leitor de tela.
 */
@Composable
private fun BotaoTrilha(trilha: Trilha, vencidas: Int, modifier: Modifier, alto: Boolean, onClick: () -> Unit) {
    val total = GeradorDeFases.FASES_POR_TRILHA
    val acesas = ProgressoVisivel.estrelasAcesas(vencidas, total, ESTRELAS_DO_CAMINHO)
    BotaoFofo(
        onClick = onClick,
        modifier = modifier,
        cor = Color(trilha.corArgb),
        corBase = Color(trilha.corEscuraArgb),
        descricao = "Caminho ${trilha.titulo}, $vencidas de $total fases",
        preencher = true,
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 8.dp)) {
            // O ícone cresce com o bloco, sem passar de metade da largura (o nome precisa caber ao lado).
            val icone = minOf(maxHeight * 0.66f, maxWidth * 0.48f)
            IconeDeTrilha(trilha, Modifier.align(Alignment.TopEnd).size(icone))
            Column(Modifier.align(Alignment.BottomStart)) {
                TextoComContorno(
                    trilha.titulo,
                    estilo = MaterialTheme.typography.headlineMedium.copy(fontSize = if (alto) 23.sp else 19.sp, lineHeight = if (alto) 27.sp else 22.sp),
                    corDoContorno = Cores.ContornoEscuro,
                    espessura = 3.dp,
                    profundidade = 2.dp,
                    alinhamento = TextAlign.Start,
                )
                EstrelasDeProgresso(
                    cheias = acesas,
                    total = ESTRELAS_DO_CAMINHO,
                    tamanho = if (alto) 20.dp else 15.dp,
                    espaco = 1.dp,
                )
            }
        }
    }
}

/** Abre a área dos pais só com um toque longo, para a criança não entrar sem querer. */
@Composable
private fun BotaoAdulto(onAbrir: () -> Unit) {
    val narrador = LocalNarrador.current
    // Disco branco sólido com base lilás, igual aos outros botões redondos (o translúcido parecia desligado).
    // O toque continua sendo o longo: por isso não é um BotaoFofo (o clique dele comeria o toque).
    Box(
        Modifier
            .semantics { contentDescription = "Área dos pais. Toque e segure." }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { narrador.falar("Essa parte é para os adultos.") },
                    onLongPress = { onAbrir() },
                )
            }
            .drawBehind {
                val p = 4.dp.toPx()
                val r = (size.width) / 2f
                val c = Offset(size.width / 2f, r)
                drawCircle(Cores.Sombra, r, c.copy(y = c.y + p + 2.dp.toPx()))
                drawCircle(BASE_DO_BOTAO, r, c.copy(y = c.y + p))
                drawCircle(Cores.Contorno, r, c.copy(y = c.y + p), style = Stroke(2.dp.toPx()))
            }
            .padding(bottom = 4.dp)
            .size(48.dp)
            .clip(CircleShape)
            // Mesma face clara do BotaoRedondo (brinquedo 3D), com a engrenagem da arte no lugar do emoji.
            .faceDeBrinquedo(Color.White, CircleShape, redonda = true),
        contentAlignment = Alignment.Center,
    ) {
        ImagemDeArte(IconesDaBarra.AJUSTES, Modifier.size(27.dp)) { Text("⚙️", fontSize = 22.sp) }
    }
}

/** Base lilás dos botões brancos (a mesma do `BotaoRedondo`). */
private val BASE_DO_BOTAO = Cores.LabioLilas
