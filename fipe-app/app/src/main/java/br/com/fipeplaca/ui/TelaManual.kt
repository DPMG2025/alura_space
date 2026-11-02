@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package br.com.fipeplaca.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.fipeplaca.data.FipeResultado
import br.com.fipeplaca.data.ItemFipe
import br.com.fipeplaca.data.PeriodoHistorico
import br.com.fipeplaca.data.Texto
import br.com.fipeplaca.data.TipoVeiculo

data class AcoesManual(
    val selecionarTipo: (TipoVeiculo) -> Unit = {},
    val alterarFiltro: (String) -> Unit = {},
    val selecionarMarca: (ItemFipe) -> Unit = {},
    val selecionarModelo: (ItemFipe) -> Unit = {},
    val selecionarAno: (ItemFipe) -> Unit = {},
    val voltar: () -> Unit = {},
    val irPara: (Etapa) -> Unit = {},
    val tentarNovamente: () -> Unit = {},
    val novaConsulta: () -> Unit = {},
    val favoritar: () -> Unit = {},
    val compartilhar: (FipeResultado) -> Unit = {},
    val copiarCodigo: (String) -> Unit = {},
    val mudarPeriodo: (PeriodoHistorico) -> Unit = {},
    val recarregarHistorico: () -> Unit = {},
    val abrirConfiguracoes: () -> Unit = {},
)

private val POPULARES = mapOf(
    TipoVeiculo.CARROS to listOf("chevrolet", "volkswagen", "fiat", "toyota", "hyundai", "jeep", "honda", "renault", "ford", "nissan", "byd", "bmw"),
    TipoVeiculo.MOTOS to listOf("honda", "yamaha", "suzuki", "kawasaki", "bmw", "harley", "triumph", "royal"),
    TipoVeiculo.CAMINHOES to listOf("mercedes", "volvo", "scania", "volkswagen", "iveco", "ford", "daf"),
)

@Composable
fun TelaManual(estado: ManualUi, historico: HistoricoUi, favorito: Boolean, acoes: AcoesManual, modifier: Modifier = Modifier) {
    val s = estado
    Column(modifier.fillMaxSize()) {
        Cabecalho(
            titulo = "Tabela FIPE",
            subtitulo = "Todas as marcas, modelos e anos",
        ) {
            Spacer(Modifier.height(14.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                TipoVeiculo.entries.forEachIndexed { i, t ->
                    SegmentedButton(
                        selected = s.tipo == t,
                        onClick = { acoes.selecionarTipo(t) },
                        shape = SegmentedButtonDefaults.itemShape(i, TipoVeiculo.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Color.White,
                            activeContentColor = Extras.cores.gradienteInicio,
                            inactiveContainerColor = Color.White.copy(alpha = 0.08f),
                            inactiveContentColor = Color.White,
                            activeBorderColor = Color.White.copy(alpha = 0.6f),
                            inactiveBorderColor = Color.White.copy(alpha = 0.4f),
                        ),
                        icon = {},
                    ) { Text(t.rotulo, maxLines = 1) }
                }
            }
            Spacer(Modifier.height(14.dp))
            IndicadorEtapas(s, acoes.irPara)
        }

        Row(Modifier.padding(start = 4.dp, end = 16.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (s.etapa != Etapa.MARCAS) {
                IconButton(onClick = acoes.voltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
            } else Spacer(Modifier.width(12.dp))
            Text(
                when (s.etapa) {
                    Etapa.MARCAS -> "Escolha a marca"
                    Etapa.MODELOS -> s.marca?.nome?.let(Texto::marca).orEmpty()
                    Etapa.ANOS -> s.modelo?.nome.orEmpty()
                    Etapa.RESULTADO -> listOfNotNull(s.modelo?.nome, s.ano?.nome).joinToString(" · ")
                },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                modifier = Modifier.weight(1f).padding(vertical = 8.dp),
            )
        }

        AnimatedContent(
            targetState = s.etapa,
            transitionSpec = {
                val avanca = targetState.ordinal > initialState.ordinal
                (slideInHorizontally(tween(260)) { if (avanca) it / 4 else -it / 4 } + fadeIn(tween(260))) togetherWith
                    (slideOutHorizontally(tween(200)) { if (avanca) -it / 4 else it / 4 } + fadeOut(tween(200)))
            },
            label = "etapa",
            modifier = Modifier.weight(1f),
        ) { etapa ->
            Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                if (etapa != Etapa.RESULTADO) {
                    CampoFiltro(s.filtro, etapa, acoes.alterarFiltro)
                    Spacer(Modifier.height(10.dp))
                }
                when {
                    s.erro != null -> CartaoErro(s.erro, "Tentar novamente", acoes.tentarNovamente)
                    s.carregando && etapa != Etapa.RESULTADO -> ListaCarregando()
                    etapa == Etapa.MARCAS -> ListaItens(
                        s.marcas, s.filtro, acoes.selecionarMarca,
                        populares = POPULARES[s.tipo].orEmpty(), mostrarAvatar = true,
                    )
                    etapa == Etapa.MODELOS -> ListaItens(s.modelos, s.filtro, acoes.selecionarModelo)
                    etapa == Etapa.ANOS -> ListaItens(s.anos, s.filtro, acoes.selecionarAno, destacarZeroKm = true)
                    else -> ResultadoManual(s, historico, favorito, acoes)
                }
            }
        }
    }
}

@Composable
private fun IndicadorEtapas(s: ManualUi, irPara: (Etapa) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Etapa.entries.forEachIndexed { i, e ->
            val feita = e.ordinal < s.etapa.ordinal
            val atual = e == s.etapa
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = feita) { irPara(e) }.padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(26.dp).clip(CircleShape)
                        .background(if (feita || atual) Color.White else Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (feita) "✓" else "${i + 1}", style = MaterialTheme.typography.labelMedium,
                        color = if (feita || atual) Extras.cores.gradienteInicio else Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(4.dp))
                Text(e.rotulo, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = if (feita || atual) 1f else 0.85f))
            }
        }
    }
}

@Composable
private fun CampoFiltro(filtro: String, etapa: Etapa, onFiltro: (String) -> Unit) {
    TextField(
        value = filtro,
        onValueChange = onFiltro,
        placeholder = {
            Text(when (etapa) {
                Etapa.MARCAS -> "Buscar marca"
                Etapa.MODELOS -> "Buscar modelo ou versão"
                else -> "Buscar ano"
            })
        },
        leadingIcon = { Icon(Icons.Filled.Search, null) },
        trailingIcon = { if (filtro.isNotEmpty()) IconButton(onClick = { onFiltro("") }) { Icon(Icons.Filled.Clear, "Limpar busca") } },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ListaCarregando() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 4.dp)) {
        repeat(8) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Esqueleto(Modifier.size(40.dp))
                Spacer(Modifier.width(14.dp))
                Esqueleto(Modifier.fillMaxWidth(0.4f + (it % 3) * 0.15f).height(18.dp))
            }
        }
    }
}

@Composable
private fun ListaItens(
    itens: List<ItemFipe>,
    filtro: String,
    onClick: (ItemFipe) -> Unit,
    populares: List<String> = emptyList(),
    mostrarAvatar: Boolean = false,
    destacarZeroKm: Boolean = false,
) {
    val chave = Texto.chave(filtro.trim())
    val filtrados = remember(itens, chave) {
        if (chave.isEmpty()) itens
        else {
            val termos = chave.split(' ').filter { it.isNotBlank() }
            itens.filter { item -> val n = Texto.chave(item.nome); termos.all { n.contains(it) } }
        }
    }
    val destaque = remember(itens, populares) {
        populares.mapNotNull { p -> itens.firstOrNull { Texto.chave(it.nome).contains(p) } }.distinct()
    }
    if (filtrados.isEmpty() && itens.isNotEmpty()) {
        EstadoVazio(Icons.Filled.Search, "Nada encontrado", "Nenhum resultado para \"$filtro\". Tente outro termo.")
        return
    }
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize()) {
            if (chave.isEmpty() && destaque.isNotEmpty()) {
                item(key = "populares") {
                    Column(Modifier.padding(vertical = 12.dp)) {
                        Text("Populares", style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
                        Spacer(Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
                            items(destaque, key = { "p" + it.codigo }) { m ->
                                Column(
                                    Modifier.width(68.dp).clip(RoundedCornerShape(12.dp)).clickable { onClick(m) },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Avatar(nomeCurto(m.nome), tamanho = 52.dp)
                                    Spacer(Modifier.height(6.dp))
                                    Text(nomeCurto(m.nome), style = MaterialTheme.typography.labelSmall, maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text("Todas (${itens.size})", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp))
                }
            }
            items(filtrados, key = { it.codigo }) { item ->
                val zeroKm = destacarZeroKm && item.nome.contains("Zero KM", ignoreCase = true)
                ListItem(
                    headlineContent = { Text(item.nome, fontWeight = if (zeroKm) FontWeight.Bold else FontWeight.Normal) },
                    leadingContent = if (mostrarAvatar) ({ Avatar(nomeCurto(item.nome)) }) else null,
                    supportingContent = if (zeroKm) ({ Text("Veículo novo", color = MaterialTheme.colorScheme.secondary) }) else null,
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onClick(item) },
                )
                HorizontalDivider(Modifier.padding(start = if (mostrarAvatar) 72.dp else 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

/** "GM - Chevrolet" vira "Chevrolet"; "VW - VolksWagen" vira "Volkswagen". */
private fun nomeCurto(nome: String): String = Texto.marca(nome)

@Composable
private fun ResultadoManual(s: ManualUi, historico: HistoricoUi, favorito: Boolean, acoes: AcoesManual) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
        item {
            val r = s.resultado
            when {
                r != null -> CartaoValorFipe(
                    r, favorito, acoes.favoritar,
                    onCompartilhar = { acoes.compartilhar(r) },
                    onCopiarCodigo = { acoes.copiarCodigo(r.codigoFipe) },
                )
                else -> CartaoCarregando()
            }
        }
        if (s.resultado != null) {
            item { CartaoHistorico(historico, acoes.mudarPeriodo, acoes.recarregarHistorico, acoes.abrirConfiguracoes) }
        }
        item {
            OutlinedButton(onClick = acoes.novaConsulta, modifier = Modifier.fillMaxWidth().height(50.dp), shape = MaterialTheme.shapes.medium) {
                Icon(Icons.Filled.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Nova consulta")
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
