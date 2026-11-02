package br.com.fipeplaca.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel

private data class Aba(val titulo: String, val icone: ImageVector)

private val ABAS = listOf(
    Aba("Placa", Icons.Filled.Search),
    Aba("Marca e modelo", Icons.AutoMirrored.Filled.List),
    Aba("Configurações", Icons.Filled.Settings),
)

@Composable
fun AppRaiz() {
    var aba by rememberSaveable { mutableIntStateOf(0) }
    val placaVm: PlacaViewModel = viewModel()
    val manualVm: ManualViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                ABAS.forEachIndexed { i, a ->
                    NavigationBarItem(
                        selected = aba == i,
                        onClick = { aba = i },
                        icon = { Icon(a.icone, contentDescription = null) },
                        label = { Text(a.titulo) },
                    )
                }
            }
        },
    ) { padding ->
        val mod = Modifier.padding(padding)
        when (aba) {
            0 -> TelaPlaca(
                vm = placaVm,
                modifier = mod,
                abrirConfiguracoes = { aba = 2 },
                buscarManual = { tipo, marca ->
                    manualVm.preencherPorPlaca(tipo, marca)
                    aba = 1
                },
            )
            1 -> TelaManual(vm = manualVm, modifier = mod)
            else -> TelaConfiguracoes(modifier = mod)
        }
    }
}
