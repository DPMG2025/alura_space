package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.GeradorDeFases
import br.com.casinhadosaber.dominio.ItensDaCasa
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.CenaDaCasa
import br.com.casinhadosaber.ui.componentes.MaoGuia
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
        narrador.falar(
            when {
                noBau > 0 -> "$oi Tem presente no baú! Toque na casinha para montar."
                missaoDisponivel -> "$oi Tem missão do dia! Toque no pergaminho para ganhar moedinhas."
                relembrar > 0 -> "$oi A Corujinha quer relembrar umas coisinhas com você. É só tocar nela, se quiser."
                estado.fasesVencidas == 0 -> "$oi Vamos construir a sua casinha? Escolha um caminho para jogar."
                else -> "$oi Escolha um caminho para ganhar mais coisas para a sua casa!"
            },
        )
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Cores.Ceu, Cores.Fundo, Cores.Fundo)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
    // Tela deitada (tablet em paisagem): a casa à esquerda e os quatro caminhos numa coluna à direita.
    val deitada = maxWidth > maxHeight * 1.15f
    Column(Modifier.fillMaxSize()) {
        // Cabeçalho
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BotaoAdulto(onAreaDosPais)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Casinha do Saber", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(
                    "🏠 ${estado.itensGanhos.size} de ${br.com.casinhadosaber.dominio.CasaQueCresce.metaAtual(estado.fasesVencidas)} coisas",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
            }
            Spacer(Modifier.width(8.dp))
            BotaoRedondo(
                emoji = if (somLigado) "🔊" else "🔇",
                descricao = if (somLigado) "Desligar som" else "Ligar som",
                onClick = onAlternarSom,
                tamanho = 48.dp,
            )
        }

        // A casinha (toque para montar)
        @Composable
        fun Casinha(modifier: Modifier) {
        Box(
            modifier.padding(horizontal = 12.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .semantics {
                        contentDescription = "Minha casinha. Toque para montar."
                        role = Role.Button
                        onClick { onAbrirCasa(); true }
                    }
                    .clickable { sons.toque(); onAbrirCasa() },
            ) {
                CenaDaCasa(estado, Modifier.fillMaxSize(), preencherAltura = true)
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

        }

        @Composable
        fun Caminho(t: Trilha, modifier: Modifier) {
            Box(modifier) {
                BotaoTrilha(t, estado.vencidasNa(t), Modifier.fillMaxSize()) {
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
                    for (t in Trilha.entries) Caminho(t, Modifier.weight(1f).fillMaxWidth())
                }
            }
        } else {
            Casinha(Modifier.weight(1f).fillMaxWidth())
            // Caminhos
            // Em telas altas, os botões dos caminhos crescem (mais fáceis de acertar com o dedo).
            val alturaBotao = if (LocalConfiguration.current.screenHeightDp > 760) 118.dp else 88.dp
            Column(
                Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (linha in Trilha.entries.chunked(2)) {
                    Row(Modifier.height(alturaBotao), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (t in linha) Caminho(t, Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
        }
    }
    }
}

/** Largura da coluna dos caminhos quando a tela está deitada. */
private val LARGURA_DOS_CAMINHOS_DEITADA = 320.dp

@Composable
private fun BotaoTrilha(trilha: Trilha, vencidas: Int, modifier: Modifier, onClick: () -> Unit) {
    val total = GeradorDeFases.FASES_POR_TRILHA
    BotaoFofo(
        onClick = onClick,
        modifier = modifier,
        cor = Color(trilha.corArgb),
        corBase = Color(trilha.corEscuraArgb),
        descricao = "Caminho ${trilha.titulo}, $vencidas de $total fases",
        preencher = true,
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(trilha.emoji, style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(trilha.titulo, style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp), color = Color.White, maxLines = 1, softWrap = false)
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x40FFFFFF)),
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(vencidas / total.toFloat())
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White),
                    )
                }
                Text("$vencidas/$total", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            }
        }
    }
}

/** Abre a área dos pais só com um toque longo, para a criança não entrar sem querer. */
@Composable
private fun BotaoAdulto(onAbrir: () -> Unit) {
    val narrador = LocalNarrador.current
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0x33FFFFFF))
            .semantics { contentDescription = "Área dos pais. Toque e segure." }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { narrador.falar("Essa parte é para os adultos.") },
                    onLongPress = { onAbrir() },
                )
            }
            .padding(12.dp),
    ) {
        Text("⚙️", style = MaterialTheme.typography.titleLarge)
    }
}
