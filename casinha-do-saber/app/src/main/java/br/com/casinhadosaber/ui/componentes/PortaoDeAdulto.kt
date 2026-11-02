package br.com.casinhadosaber.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.ui.tema.Cores
import kotlin.random.Random

/** Continha de multiplicação com teclado: separa o que é de adulto do que é da criança. */
@Composable
fun PortaoDeAdulto(onLiberar: () -> Unit) {
    val (a, b) = remember { Random.nextInt(6, 10) to Random.nextInt(5, 10) }
    var resposta by rememberSaveable { mutableStateOf("") }
    var errou by remember { mutableStateOf(false) }
    fun conferir() {
        if (resposta.toIntOrNull() == a * b) onLiberar() else { errou = true; resposta = "" }
    }
    Column(
        Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Só para adultos", style = MaterialTheme.typography.titleLarge)
        Text("Quanto é $a × $b?", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            resposta.ifEmpty { if (errou) "Tente de novo" else "?" },
            style = MaterialTheme.typography.displayMedium,
            color = if (errou && resposta.isEmpty()) Cores.Errado else Cores.Texto,
        )
        Spacer(Modifier.height(12.dp))
        val teclas = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0", "OK")
        for (linha in teclas.chunked(3)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                for (t in linha) {
                    BotaoFofo(
                        onClick = {
                            errou = false
                            when (t) {
                                "⌫" -> resposta = resposta.dropLast(1)
                                "OK" -> conferir()
                                else -> if (resposta.length < 3) resposta += t
                            }
                        },
                        cor = if (t == "OK") Cores.Roxo else Color(0xFFF3EEFF),
                        corBase = if (t == "OK") Color(0xFF4038C2) else Color(0xFFD9CFF0),
                        descricao = if (t == "⌫") "Apagar" else t,
                    ) {
                        Box(Modifier.size(72.dp, 56.dp), contentAlignment = Alignment.Center) {
                            Text(t, style = MaterialTheme.typography.headlineMedium, color = if (t == "OK") Color.White else Cores.Texto)
                        }
                    }
                }
            }
        }
    }
}

