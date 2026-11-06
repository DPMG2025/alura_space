package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.Familia
import br.com.casinhadosaber.dominio.Genero
import br.com.casinhadosaber.dominio.Irmao
import br.com.casinhadosaber.dominio.Perfil
import br.com.casinhadosaber.ui.componentes.BotaoFofo
import br.com.casinhadosaber.ui.componentes.Crianca
import br.com.casinhadosaber.ui.componentes.Irmaozinho
import br.com.casinhadosaber.ui.tema.Cores

private val LilasDoIrmao = Color(0xFFD9CFF0)

/** Troca o irmão da criança que está jogando (sem perfil, nada muda). */
fun EstadoJogo.comIrmao(i: Irmao): EstadoJogo = copy(perfil = perfil?.copy(irmao = i))

/**
 * Cartão do irmãozinho na área dos pais: irmãozinho, irmãzinha ou sem irmão, e o nome (o do irmão de verdade,
 * se quiser). Mostra a criança e o irmão desenhados juntos, como aparecem no jogo.
 */
@Composable
fun CartaoDoIrmao(perfil: Perfil?, onSalvar: (Irmao) -> Unit) {
    CartaoDosPais {
        Text("👶 Irmãozinho no jogo", style = MaterialTheme.typography.titleLarge)
        Text(
            "Um irmão menor mora na casinha, comemora os acertos e fala quando a criança toca nele. " +
                "Dê o nome do irmão de verdade, troque para irmãzinha ou tire.",
            style = MaterialTheme.typography.bodyMedium,
            color = Cores.TextoSuave,
        )
        if (perfil == null) return@CartaoDosPais
        val irmao = perfil.irmao
        if (irmao.ligado) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Bottom) {
                Crianca(perfil, Modifier.height(120.dp), animado = false)
                Spacer(Modifier.width(6.dp))
                Irmaozinho(perfil, Modifier.height(80.dp), animado = false)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            val opcoes = listOf(
                Triple("Irmãozinho", "irmao_menino", irmao.ligado && irmao.genero == Genero.MENINO),
                Triple("Irmãzinha", "irmao_menina", irmao.ligado && irmao.genero == Genero.MENINA),
                Triple("Sem irmão", "irmao_sem", !irmao.ligado),
            )
            for ((rotulo, tag, marcado) in opcoes) {
                BotaoFofo(
                    onClick = {
                        onSalvar(
                            when (tag) {
                                "irmao_menino" -> irmao.copy(ligado = true, genero = Genero.MENINO)
                                "irmao_menina" -> irmao.copy(ligado = true, genero = Genero.MENINA)
                                else -> irmao.copy(ligado = false)
                            },
                        )
                    },
                    cor = if (marcado) Cores.Roxo else Color.White,
                    corBase = if (marcado) Color(0xFF5B3FA8) else LilasDoIrmao,
                    profundidade = 4.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag(tag)
                        .semantics { selected = marcado },
                ) {
                    Text(
                        rotulo,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (marcado) Color.White else Cores.Texto,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .then(if (marcado) Modifier else Modifier.border(2.dp, LilasDoIrmao, RoundedCornerShape(24.dp)))
                            .padding(horizontal = 6.dp, vertical = 14.dp),
                    )
                }
            }
        }
        if (!irmao.ligado) return@CartaoDosPais
        Spacer(Modifier.height(12.dp))
        var nome by rememberSaveable(irmao.nome) { mutableStateOf(irmao.nome) }
        Column {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = Familia.cortarNome(it, Irmao.TAMANHO_DO_NOME) },
                label = { Text(if (irmao.genero == Genero.MENINA) "Nome da irmãzinha (opcional)" else "Nome do irmãozinho (opcional)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth().testTag("irmao_nome"),
            )
            Spacer(Modifier.height(10.dp))
            val mudou = nome.trim() != irmao.nome
            BotaoFofo(
                onClick = { onSalvar(irmao.copy(nome = nome.trim())) },
                cor = Cores.Roxo,
                habilitado = mudou,
                modifier = Modifier.testTag("irmao_salvar"),
            ) {
                Text(
                    if (mudou) "Salvar o nome" else "Salvo ✔",
                    color = if (mudou) Color.White else Cores.Texto,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
    }
}
