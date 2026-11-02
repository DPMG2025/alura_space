@file:OptIn(ExperimentalMaterial3Api::class)

package br.com.fipeplaca.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.fipeplaca.data.ConsultaSalva
import br.com.fipeplaca.data.Datas

data class AcoesSalvos(
    val abrir: (ConsultaSalva) -> Unit = {},
    val alternarFavorito: (String) -> Unit = {},
    val remover: (String) -> Unit = {},
    val limparRecentes: () -> Unit = {},
)

@Composable
fun TelaSalvos(itens: List<ConsultaSalva>, acoes: AcoesSalvos, modifier: Modifier = Modifier, abaInicial: Int = 0) {
    var aba by rememberSaveable { mutableIntStateOf(abaInicial) }
    val favoritos = itens.filter { it.favorito }
    val lista = if (aba == 0) favoritos else itens.sortedByDescending { it.data }

    Column(modifier.fillMaxSize()) {
        Cabecalho(titulo = "Salvos", subtitulo = "Favoritos e consultas recentes") {
            Spacer(Modifier.height(14.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf("Favoritos (${favoritos.size})", "Recentes (${itens.size})").forEachIndexed { i, rotulo ->
                    SegmentedButton(
                        selected = aba == i, onClick = { aba = i },
                        shape = SegmentedButtonDefaults.itemShape(i, 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Color.White,
                            activeContentColor = Extras.cores.gradienteInicio,
                            inactiveContainerColor = Color.White.copy(alpha = 0.08f),
                            inactiveContentColor = Color.White,
                            activeBorderColor = Color.White.copy(alpha = 0.6f),
                            inactiveBorderColor = Color.White.copy(alpha = 0.4f),
                        ),
                        icon = {},
                    ) { Text(rotulo, maxLines = 1) }
                }
            }
        }

        if (lista.isEmpty()) {
            if (aba == 0) EstadoVazio(Icons.Filled.FavoriteBorder, "Nenhum favorito ainda",
                "Toque no coração do resultado para guardar um veículo e acompanhar o preço dele.")
            else EstadoVazio(Icons.Filled.DateRange, "Nenhuma consulta recente",
                "As consultas feitas pela placa ou pela marca e modelo aparecem aqui, mesmo sem internet.")
        } else LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(lista, key = { it.id }) { c -> ItemSalvo(c, acoes) }
            if (aba == 1 && itens.any { !it.favorito }) {
                item {
                    TextButton(onClick = acoes.limparRecentes, modifier = Modifier.fillMaxWidth()) {
                        Text("Limpar recentes (favoritos são mantidos)")
                    }
                }
            }
        }
    }
}


@Composable
private fun ItemSalvo(c: ConsultaSalva, acoes: AcoesSalvos) {
    Surface(
        onClick = { acoes.abrir(c) },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (c.veiculo != null) PlacaMercosul(c.veiculo.placa, Modifier.width(96.dp), altura = 34.dp)
            else Avatar(br.com.fipeplaca.data.Texto.marca(c.principal?.marca.orEmpty()), tamanho = 44.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(c.titulo, style = MaterialTheme.typography.titleSmall, maxLines = 2)
                Text(
                    listOfNotNull(c.principal?.anoModelo?.takeIf { it.isNotBlank() }, c.principal?.mesReferencia?.takeIf { it.isNotBlank() })
                        .joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                )
                Text(c.principal?.valor ?: "Sem valor", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(Datas.relativa(c.data), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { acoes.alternarFavorito(c.id) }) {
                    Icon(
                        if (c.favorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (c.favorito) "Remover dos favoritos" else "Favoritar",
                        tint = if (c.favorito) Color(0xFFE53958) else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { acoes.remover(c.id) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
