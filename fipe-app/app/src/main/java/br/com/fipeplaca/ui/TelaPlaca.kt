package br.com.fipeplaca.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.fipeplaca.data.Placa
import br.com.fipeplaca.data.TipoVeiculo
import br.com.fipeplaca.data.VeiculoPlaca

@Composable
fun TelaPlaca(
    vm: PlacaViewModel,
    modifier: Modifier = Modifier,
    abrirConfiguracoes: () -> Unit,
    buscarManual: (TipoVeiculo, String?) -> Unit,
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val historico by vm.historico.estado.collectAsStateWithLifecycle()
    val foco = LocalFocusManager.current

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Consulta pela placa", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Digite a placa (antiga ou Mercosul) para ver o valor de mercado pela tabela FIPE e a evolução do preço.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = estado.placa,
            onValueChange = vm::alterarPlaca,
            label = { Text("Placa") },
            placeholder = { Text("ABC1D23") },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = FontFamily.Monospace, letterSpacing = 4.sp, textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Search,
                autoCorrectEnabled = false,
            ),
            keyboardActions = KeyboardActions(onSearch = { foco.clearFocus(); vm.consultar() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { foco.clearFocus(); vm.consultar() },
            enabled = !estado.carregando && estado.placa.length == 7,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text("Consultar") }

        if (estado.semToken) {
            CartaoErro(
                "Para consultar pela placa é preciso informar o token do serviço de placas (ex.: API Placas, apiplacas.com.br). " +
                    "Você também pode buscar pela marca, modelo e ano sem token.",
                "Abrir configurações",
                abrirConfiguracoes,
            )
        }
        estado.erro?.let { CartaoErro(it) }

        if (estado.carregando) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }

        estado.veiculo?.let { v ->
            CartaoVeiculo(v)
            if (v.fipe.isEmpty()) {
                CartaoErro(
                    "O serviço de placas não informou o código FIPE deste veículo. Continue pela busca de marca e modelo.",
                    "Buscar por marca e modelo",
                ) { buscarManual(v.tipo, v.marca) }
            } else {
                if (v.fipe.size > 1) {
                    Text(
                        "Versões possíveis na FIPE (${v.fipe.size}). Toque para ver o histórico de cada uma:",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                v.fipe.forEachIndexed { i, r ->
                    if (v.fipe.size > 1) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = estado.selecionado == i, onClick = { vm.selecionarVersao(i) })
                            Text(r.modelo, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    CartaoValorFipe(r, destaque = estado.selecionado == i)
                }
                CartaoHistorico(
                    estado = historico,
                    onPeriodo = vm.historico::mudarPeriodo,
                    onTentar = { vm.selecionarVersao(estado.selecionado) },
                )
                OutlinedButton(onClick = { buscarManual(v.tipo, v.marca) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Não é este modelo? Buscar por marca e modelo")
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CartaoVeiculo(v: VeiculoPlaca) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(Placa.formatar(v.placa), style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Info("Marca", v.marca)
            Info("Modelo", v.modelo)
            Info("Ano fab./mod.", listOfNotNull(v.anoFabricacao, v.anoModelo).joinToString(" / ").ifBlank { null })
            Info("Cor", v.cor)
            Info("Combustível", v.combustivel)
            Info("Cidade", listOfNotNull(v.municipio, v.uf).joinToString(" / ").ifBlank { null })
            Info("Situação", v.situacao)
            Info("Final do chassi", v.chassiFinal)
        }
    }
}
