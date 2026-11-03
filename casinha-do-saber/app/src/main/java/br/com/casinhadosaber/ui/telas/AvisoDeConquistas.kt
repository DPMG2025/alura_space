package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.Conquistas
import br.com.casinhadosaber.dominio.Diploma
import br.com.casinhadosaber.dominio.Figurinha
import br.com.casinhadosaber.dominio.Mundos
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.pulsando
import br.com.casinhadosaber.ui.tema.Cores
import kotlinx.coroutines.delay

/** Diplomas e figurinhas ganhos nas últimas partidas, esperando para serem mostrados. */
data class NovasConquistas(val diplomas: List<Diploma> = emptyList(), val figurinhas: List<Figurinha> = emptyList()) {
    val vazia: Boolean get() = diplomas.isEmpty() && figurinhas.isEmpty()

    operator fun plus(outra: NovasConquistas): NovasConquistas =
        NovasConquistas((diplomas + outra.diplomas).distinct(), (figurinhas + outra.figurinhas).distinctBy { it.id })

    companion object {
        /** O que apareceu entre o progresso [antes] e [depois] de uma partida. */
        fun entre(antes: Map<String, Int>, depois: Map<String, Int>): NovasConquistas =
            NovasConquistas(Conquistas.diplomasNovos(antes, depois), Conquistas.figurinhasNovas(antes, depois))
    }
}

/**
 * Faixa que aparece embaixo da tela quando a criança volta de uma vitória com figurinha ou diploma novo
 * (depois do prêmio, para não atrapalhar o presente). Tocar abre o álbum ou a parede dos diplomas; sozinha,
 * some depois de alguns segundos. Não cobre a tela inteira: o resto continua tocável.
 */
@Composable
fun AvisoDeConquistas(
    novas: NovasConquistas,
    nome: String?,
    onAbrir: () -> Unit,
    onFim: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    val fim by rememberUpdatedState(onFim)
    val diploma = novas.diplomas.firstOrNull()
    LaunchedEffect(novas) {
        sons.colocar()
        narrador.falar(
            when {
                diploma != null -> diploma.texto(nome) + " Ganhou um diploma!"
                novas.figurinhas.size == 1 -> "Figurinha nova no álbum!"
                else -> "${novas.figurinhas.size} figurinhas novas no álbum!"
            },
        )
        delay(9_000)
        fim()
    }
    BotaoFofo(
        onClick = { sons.toque(); onAbrir() },
        modifier = modifier
            .padding(16.dp)
            .widthIn(max = 460.dp)
            .fillMaxWidth()
            .testTag("aviso_conquista"),
        cor = Color(0xFFFFF6DC),
        corBase = Cores.Estrela,
        descricao = if (diploma != null) "Diploma novo! Toque para ver" else "Figurinha nova! Toque para ver o álbum",
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (diploma != null) "📜" else novas.figurinhas.take(3).joinToString(" ") { it.emoji },
                fontSize = 34.sp,
                modifier = Modifier.pulsando(),
                maxLines = 1,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (diploma != null) "Diploma novo!" else if (novas.figurinhas.size == 1) "Figurinha nova!" else "${novas.figurinhas.size} figurinhas novas!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (diploma != null) "${Mundos.todos[diploma.mundo].emoji} ${Mundos.todos[diploma.mundo].titulo}" else "Toque para ver o álbum 📒",
                    style = MaterialTheme.typography.titleSmall,
                    color = Cores.TextoSuave,
                    maxLines = 1,
                )
            }
        }
    }
}
