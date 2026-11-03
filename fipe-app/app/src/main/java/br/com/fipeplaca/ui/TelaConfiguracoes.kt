@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package br.com.fipeplaca.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import br.com.fipeplaca.data.Preferencias

data class Configuracoes(val tokenPlaca: String, val urlPlaca: String, val tokenFipe: String)

@Composable
fun TelaConfiguracoes(
    inicial: Configuracoes,
    versao: String,
    onSalvar: (Configuracoes) -> Unit,
    modifier: Modifier = Modifier,
    tema: String = "sistema",
    onTema: (String) -> Unit = {},
    tokenIlegivel: Boolean = false,
) {
    // rememberSaveable: o que foi digitado e ainda nao salvo sobrevive ao girar a tela
    // Tokens fora do estado salvo da tela (o Bundle nao e cifrado); a URL pode ser preservada
    var tokenPlaca by remember { mutableStateOf(inicial.tokenPlaca) }
    var urlPlaca by rememberSaveable { mutableStateOf(inicial.urlPlaca) }
    var tokenFipe by remember { mutableStateOf(inicial.tokenFipe) }
    val erroUrl = br.com.fipeplaca.data.FipeRepositorio.validarUrlPlaca(urlPlaca)
    val links = LocalUriHandler.current
    fun abrir(url: String) = runCatching { links.openUri(url) } // sem navegador instalado, nao fecha o app
    var mostrarPlaca by rememberSaveable { mutableStateOf(false) }
    var mostrarFipe by rememberSaveable { mutableStateOf(false) }
    val teclado = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false)
    val alterado = Configuracoes(tokenPlaca, urlPlaca, tokenFipe) != inicial

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Cabecalho(titulo = "Ajustes", subtitulo = "Conexões com os serviços de dados")
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (tokenIlegivel) {
                CartaoErro("Um token salvo não pôde ser lido (por exemplo, depois de restaurar o app em outro aparelho). Informe-o de novo abaixo.")
            }
            Secao(titulo = "Aparência") {
                val opcoes = listOf("sistema" to "Sistema", "claro" to "Claro", "escuro" to "Escuro")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    opcoes.forEachIndexed { i, (valor, rotulo) ->
                        SegmentedButton(
                            selected = tema == valor,
                            onClick = { onTema(valor) },
                            shape = SegmentedButtonDefaults.itemShape(i, opcoes.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        ) { Text(rotulo) }
                    }
                }
            }

            Secao(titulo = "Consulta pela placa") {
                // Status pelo que esta salvo, com a mesma regra usada na consulta
                val placaAtiva = inicial.tokenPlaca.isNotBlank() || !inicial.urlPlaca.contains("{token}")
                Status(placaAtiva, if (placaAtiva) "Busca pela placa ativa" else "Sem token: busca pela placa desativada")
                Spacer(Modifier.height(8.dp))
                Text(
                    "A placa não faz parte da tabela FIPE. Para identificar o veículo, o app usa um serviço de consulta " +
                        "veicular. O padrão é a API Placas: crie uma conta, copie o token e cole abaixo.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = tokenPlaca, onValueChange = { tokenPlaca = it.trim() },
                    label = { Text("Token de placas") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                    singleLine = true,
                    visualTransformation = if (mostrarPlaca) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = teclado,
                    trailingIcon = { TextButton(onClick = { mostrarPlaca = !mostrarPlaca }) { Text(if (mostrarPlaca) "Ocultar" else "Mostrar") } },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = urlPlaca, onValueChange = { urlPlaca = it },
                    label = { Text("URL da consulta") },
                    supportingText = { Text(erroUrl ?: "Use {placa} e {token} onde entram a placa e o token.") },
                    isError = erroUrl != null,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row {
                    TextButton(onClick = { abrir("https://apiplacas.com.br") }) { Text("Obter token") }
                    TextButton(onClick = { urlPlaca = Preferencias.URL_PADRAO }) { Text("Restaurar URL padrão") }
                }
            }

            Secao(titulo = "Tabela FIPE") {
                Text(
                    "Marcas, modelos, anos e o valor atual vêm da API pública da FIPE, sem custo e sem token. " +
                        "Sem token, o histórico mês a mês cobre só os 2 ou 3 meses mais recentes. Para 12 ou 24 meses completos " +
                        "é preciso um token da FIPE de um plano com acesso ao histórico. Os valores dos veículos salvos também " +
                        "ficam guardados neste aparelho, mês a mês.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = tokenFipe, onValueChange = { tokenFipe = it.trim() },
                    label = { Text("Token FIPE") },
                    supportingText = { Text("Opcional") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null) },
                    singleLine = true,
                    visualTransformation = if (mostrarFipe) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = teclado,
                    trailingIcon = { TextButton(onClick = { mostrarFipe = !mostrarFipe }) { Text(if (mostrarFipe) "Ocultar" else "Mostrar") } },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = { abrir("https://fipe.online") }) { Text("Ver planos com histórico") }
            }

            Button(
                onClick = { onSalvar(Configuracoes(tokenPlaca, urlPlaca.trim(), tokenFipe)) },
                enabled = alterado && erroUrl == null,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            ) { Text("Salvar alterações") }

            Secao(titulo = "Sobre") {
                Text(
                    "Valores de referência da Tabela FIPE (Fundação Instituto de Pesquisas Econômicas), que expressa o preço " +
                        "médio de veículos anunciados no mercado nacional. Os tokens ficam só neste aparelho, cifrados com uma chave do sistema, e não entram em backup.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text("Versão $versao", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Status(ok: Boolean, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Filled.CheckCircle, null,
            tint = if (ok) Extras.cores.alta else MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.width(8.dp))
        Text(texto, style = MaterialTheme.typography.labelLarge, color = if (ok) Extras.cores.alta else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
