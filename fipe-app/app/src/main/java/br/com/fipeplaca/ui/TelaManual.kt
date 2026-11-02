@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package br.com.fipeplaca.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fipeplaca.data.ItemFipe
import br.com.fipeplaca.data.TipoVeiculo
import br.com.fipeplaca.data.Texto

@Composable
fun TelaManual(vm: ManualViewModel, modifier: Modifier = Modifier) {
    val s by vm.estado.collectAsStateWithLifecycle()
    val historico by vm.historico.estado.collectAsStateWithLifecycle()

    BackHandler(enabled = s.etapa != Etapa.MARCAS) { vm.voltar() }

    Column(modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("Busca por marca e modelo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoVeiculo.entries.forEach { t ->
                FilterChip(selected = s.tipo == t, onClick = { vm.selecionarTipo(t) }, label = { Text(t.rotulo) })
            }
        }

        // Trilha: Marca > Modelo > Ano
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (s.etapa != Etapa.MARCAS) {
                IconButton(onClick = { vm.voltar() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
            }
            Text(
                listOfNotNull(s.marca?.nome, s.modelo?.nome, s.ano?.nome)
                    .takeIf { s.etapa != Etapa.MARCAS }?.joinToString("  ›  ")
                    ?: "Escolha a marca",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        }

        if (s.etapa != Etapa.RESULTADO) {
            val titulo = when (s.etapa) {
                Etapa.MARCAS -> "Filtrar marcas"
                Etapa.MODELOS -> "Filtrar modelos"
                else -> "Filtrar anos"
            }
            OutlinedTextField(
                value = s.filtro,
                onValueChange = vm::alterarFiltro,
                label = { Text(titulo) },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (s.filtro.isNotEmpty()) IconButton(onClick = { vm.alterarFiltro("") }) { Icon(Icons.Filled.Clear, "Limpar") }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
        }

        when {
            s.carregando && s.etapa != Etapa.RESULTADO ->
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            s.erro != null -> CartaoErro(s.erro!!, "Tentar novamente") { vm.tentarNovamente() }
            else -> when (s.etapa) {
                Etapa.MARCAS -> ListaItens(s.marcas, s.filtro, vm::selecionarMarca)
                Etapa.MODELOS -> ListaItens(s.modelos, s.filtro, vm::selecionarModelo)
                Etapa.ANOS -> ListaItens(s.anos, s.filtro, vm::selecionarAno)
                Etapa.RESULTADO -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (s.carregando || s.resultado == null) {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    }
                    s.resultado?.let { r ->
                        CartaoValorFipe(r)
                        CartaoHistorico(historico, vm.historico::mudarPeriodo) { vm.historico.carregar(r) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ListaItens(itens: List<ItemFipe>, filtro: String, onClick: (ItemFipe) -> Unit) {
    val chave = Texto.chave(filtro.trim())
    val filtrados = remember(itens, chave) {
        if (chave.isEmpty()) itens else itens.filter { Texto.chave(it.nome).contains(chave) }
    }
    if (filtrados.isEmpty() && itens.isNotEmpty()) {
        Text("Nada encontrado para \"$filtro\".", modifier = Modifier.padding(16.dp))
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(filtrados, key = { it.codigo }) { item ->
            Text(
                item.nome,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().clickable { onClick(item) }.padding(vertical = 14.dp, horizontal = 4.dp),
            )
            HorizontalDivider()
        }
    }
}
