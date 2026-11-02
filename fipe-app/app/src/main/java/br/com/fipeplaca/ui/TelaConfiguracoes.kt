package br.com.fipeplaca.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.fipeplaca.FipeApp
import br.com.fipeplaca.data.Preferencias

@Composable
fun TelaConfiguracoes(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val prefs = (ctx.applicationContext as FipeApp).prefs
    var tokenPlaca by remember { mutableStateOf(prefs.tokenPlaca) }
    var urlPlaca by remember { mutableStateOf(prefs.urlPlaca) }
    var tokenFipe by remember { mutableStateOf(prefs.tokenFipe) }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Configurações", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Text("Consulta pela placa", style = MaterialTheme.typography.titleMedium)
        Text(
            "A placa não faz parte da tabela FIPE. Para descobrir marca, modelo e código FIPE a partir dela, o app usa um " +
                "serviço de consulta veicular. O padrão é a API Placas (apiplacas.com.br): crie uma conta, copie o token e cole abaixo.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = tokenPlaca, onValueChange = { tokenPlaca = it },
            label = { Text("Token do serviço de placas") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = urlPlaca, onValueChange = { urlPlaca = it },
            label = { Text("URL da consulta") },
            supportingText = { Text("Use {placa} e {token} onde devem entrar a placa e o token.") },
            modifier = Modifier.fillMaxWidth(),
        )
        TextButton(onClick = { urlPlaca = Preferencias.URL_PADRAO }) { Text("Restaurar URL padrão") }

        Text("Tabela FIPE", style = MaterialTheme.typography.titleMedium)
        Text(
            "Marcas, modelos, anos e valores vêm da API pública da FIPE (parallelum.com.br), sem token. " +
                "O histórico usa a versão 2 da API, que sem token permite cerca de 500 consultas por dia " +
                "(cada mês do histórico é uma consulta). Um token gratuito em fipe.online aumenta esse limite.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = tokenFipe, onValueChange = { tokenFipe = it },
            label = { Text("Token da FIPE (opcional)") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                prefs.tokenPlaca = tokenPlaca
                prefs.urlPlaca = urlPlaca
                prefs.tokenFipe = tokenFipe
                Toast.makeText(ctx, "Configurações salvas", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Salvar") }
    }
}
