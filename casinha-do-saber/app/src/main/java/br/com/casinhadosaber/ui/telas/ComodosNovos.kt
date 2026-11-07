package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.casinhadosaber.dominio.CasaQueCresce
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.TipoDeArea
import br.com.casinhadosaber.dominio.en
import br.com.casinhadosaber.dominio.pt
import br.com.casinhadosaber.ui.LocalNarrador
import br.com.casinhadosaber.ui.LocalSons
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.BotaoRedondo
import br.com.casinhadosaber.ui.componentes.CenaDaArea
import br.com.casinhadosaber.ui.componentes.EstrelasDeProgresso
import br.com.casinhadosaber.ui.componentes.IconeDaCoisa
import br.com.casinhadosaber.ui.componentes.ImagemDeArte
import br.com.casinhadosaber.ui.componentes.ProgressoVisivel
import br.com.casinhadosaber.ui.componentes.TextoComContorno
import androidx.compose.foundation.layout.BoxWithConstraints

/**
 * Cômodos novos da casa que cresce, dentro da casa por dentro: uma portinha por cômodo já construído
 * (quarto novo, sala de brinquedos, biblioteca...) na lateral da tela. Tocar abre o cômodo por cima da
 * casa, com as coisas dele para arrastar e, embaixo, as que ainda esperam no baú.
 *
 * Fica num arquivo separado e entra na [TelaCasaPorDentro] por uma chamada só (perto do confete).
 * Sem cômodo novo construído, não aparece nada.
 */
@Composable
fun BoxScope.ComodosNovosDaCasa(
    estado: EstadoJogo,
    onAtualizar: ((EstadoJogo) -> EstadoJogo) -> Unit,
    /** Para as capturas: já abre neste cômodo. */
    abertoInicial: String? = null,
) {
    val comodos = CasaQueCresce.construidas(estado).filter { it.tipo == TipoDeArea.DENTRO }
    if (comodos.isEmpty()) return
    val narrador = LocalNarrador.current
    val sons = LocalSons.current
    var aberto by rememberSaveable { mutableStateOf(abertoInicial) }
    var destaque by remember { mutableStateOf<String?>(null) }
    val area = comodos.firstOrNull { it.id == aberto }

    if (area == null) {
        Column(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (c in comodos.take(5)) {
                BotaoFofo(
                    onClick = {
                        sons.toque()
                        aberto = c.id
                        narrador.falar(listOf(pt("${c.nome}! Em inglês:"), en(c.nomeEn)))
                    },
                    cor = Color(0xFFFFF3C4),
                    corBase = Color(0xFFE0A800),
                    descricao = "Entrar: ${c.nome}",
                    modifier = Modifier.testTag("comodo_novo_${c.id}"),
                ) {
                    Column(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        ImagemDeArte("icone_avancar", Modifier.size(26.dp)) { Text("🚪", fontSize = 22.sp) }
                        IconeDaAreaK2(c, 30.dp)
                    }
                }
            }
        }
        return
    }

    // O cômodo aberto, por cima da casa.
    val pendentes = estado.itensNoBau.filter { it.area == area.id }
    Column(
        Modifier
            .fillMaxSize()
            // Céu do mundo de brinquedo (maquete 1): azul em cima, pêssego e rosa embaixo.
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF7CC4FA), 0.38f to Color(0xFFB8E2FF), 0.70f to Color(0xFFFFE6D6), 1f to Color(0xFFFFD0DC),
                ),
            )
            .testTag("comodo_aberto_${area.id}"),
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            BotaoRedondo("⬅️", "Voltar para a casa", { sons.toque(); aberto = null }, tamanho = 52.dp)
            BoxWithConstraints(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                // Nome com contorno, que diminui para caber (360 dp), e o progresso em estrelas (sem "x de y coisas").
                val base = MaterialTheme.typography.headlineMedium
                val cabe = ((maxWidth.value - 8f) / (area.nome.length.coerceAtLeast(1) * 0.55f)).coerceIn(16f, base.fontSize.value)
                val progresso = CasaQueCresce.progresso(estado, area)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextoComContorno(area.nome, estilo = base.copy(fontSize = cabe.sp, lineHeight = (cabe * 1.15f).sp), espessura = 4.dp, profundidade = 2.dp)
                    EstrelasDeProgresso(
                        ProgressoVisivel.estrelasAcesas(progresso.colocadas, progresso.total, 5),
                        total = 5,
                        tamanho = 20.dp,
                        brilharUltima = progresso.colocadas >= progresso.total,
                        descricao = "${progresso.colocadas} de ${progresso.total} coisas",
                    )
                }
            }
            Spacer(Modifier.size(52.dp))
        }
        Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp)) {
            CenaDaArea(
                area = area,
                estado = estado,
                modifier = Modifier.fillMaxSize().shadow(6.dp, RoundedCornerShape(24.dp)).clip(RoundedCornerShape(24.dp)),
                destaqueId = destaque,
                onTocarItem = { item ->
                    sons.toque()
                    narrador.falar(listOf(pt("${item.nome}!"), en(item.nomeEn)))
                },
                onMoverItem = { item, pos -> onAtualizar { it.colocar(item, pos) } },
                onGuardarItem = { item ->
                    onAtualizar { it.guardar(item) }
                    sons.toque()
                    narrador.falar("Guardei no baú.")
                },
            )
        }
        if (pendentes.isNotEmpty()) {
            LazyRow(
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(pendentes, key = { it.id }) { item ->
                    BotaoFofo(
                        onClick = {
                            onAtualizar { it.colocar(item) }
                            destaque = item.id
                            sons.colocar()
                            narrador.falar(listOf(pt("${item.nome}! Em inglês:"), en(item.nomeEn)))
                        },
                        cor = Color.White,
                        corBase = Color(0xFF8E5A34),
                        descricao = "${item.nome}. Toque para colocar no cômodo.",
                    ) {
                        Box(Modifier.padding(8.dp).width(64.dp), contentAlignment = Alignment.Center) { IconeDaCoisa(item, 56.dp) }
                    }
                }
            }
        } else {
            Spacer(Modifier.size(10.dp))
        }
    }
}
