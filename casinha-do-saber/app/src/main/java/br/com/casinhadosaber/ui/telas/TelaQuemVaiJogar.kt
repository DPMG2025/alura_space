package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.CriancaDaCasa
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.CeuDaEntrada
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.IlhaAtras
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.NuvensDoCeu
import br.com.casinhadosaber.ui.componentes.PecasDoPremio
import br.com.casinhadosaber.ui.componentes.TituloDeBrinquedo
import br.com.casinhadosaber.ui.tema.Cores

/*
 * "Quem vai jogar?": aparece ao abrir o app quando o aparelho tem mais de uma criança. Cada criança é um
 * bloco de brinquedo com o próprio personagem de pé numa ilhinha de grama e o nome (o nome vem só do perfil;
 * em branco vira "Criança 2"). Marcas de teste: "quem_vai_jogar", "crianca_<id>".
 */

/** Faces clarinhas dos blocos (uma cor por criança) e o lábio de cada uma. */
private val CoresDosCartoes = listOf(
    Color(0xFFFFE3EC) to Color(0xFFF2A9C2), Color(0xFFDFF3FF) to Color(0xFF9DCDF0),
    Color(0xFFE6F8DC) to Color(0xFFA6D98F), Color(0xFFFFF1CC) to Color(0xFFF0CC6E),
    Color(0xFFEDE6FF) to Color(0xFFBBA8F0), Color(0xFFFFE6D5) to Color(0xFFF2B48C),
    Color(0xFFD9F5F0) to Color(0xFF8FD3C6), Color(0xFFF6E1FF) to Color(0xFFD3A6EC),
)

@Composable
fun TelaQuemVaiJogar(criancas: List<CriancaDaCasa>, onEscolher: (String) -> Unit) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    LaunchedEffect(Unit) { narrador.falar("Quem vai jogar?") }
    Box(Modifier.fillMaxSize().background(CeuDaEntrada)) {
        NuvensDoCeu()
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("quem_vai_jogar"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TituloDeBrinquedo("Quem vai jogar?")
            Spacer(Modifier.height(24.dp))
            // Duas por fileira (uma só fica sozinha no meio).
            for (fileira in criancas.withIndex().toList().chunked(2)) {
                Row(
                    Modifier.fillMaxWidth().widthIn(max = 560.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                ) {
                    for ((i, c) in fileira) {
                        val (face, labio) = CoresDosCartoes[i % CoresDosCartoes.size]
                        BotaoFofo(
                            onClick = {
                                sons.toque()
                                narrador.parar()
                                onEscolher(c.id)
                            },
                            modifier = Modifier.weight(1f, fill = false).widthIn(max = 200.dp).testTag("crianca_${c.id}"),
                            cor = face,
                            corBase = labio,
                            descricao = c.nomeParaMostrar(),
                        ) {
                            Column(
                                Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                // O personagem de pé na ilhinha (a ilha passa um pouco do quadro, sem cortar o boneco).
                                Box(Modifier.size(width = 136.dp, height = 186.dp), contentAlignment = Alignment.BottomCenter) {
                                    IlhaAtras(largura = 128.dp, pes = 36.dp)
                                    val perfil = c.perfil
                                    if (perfil != null) {
                                        Crianca(perfil, Modifier.padding(bottom = 33.dp).height(150.dp))
                                    } else {
                                        // Criança que o adulto acabou de adicionar e ainda não ganhou personagem: a sementinha.
                                        ImagemDeArte(PecasDoPremio.idDaSemente(2), Modifier.padding(bottom = 32.dp).size(96.dp)) {
                                            Box(contentAlignment = Alignment.Center) { Text("🌱", style = MaterialTheme.typography.displayLarge) }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    c.nomeParaMostrar(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Cores.Texto,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
