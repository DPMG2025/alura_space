package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.ui.componentes.BotaoFofo

/**
 * Tela provisória dos tipos novos de desafio, enquanto a tela de verdade não fica pronta.
 * Os geradores de fase ainda não sorteiam esses tipos; se algum aparecer por engano, a criança
 * não fica presa: um botão grande conclui a pergunta (sem contar como acerto de primeira).
 */
@Composable
internal fun AtividadeProvisoria(
    nome: String,
    ativa: Boolean,
    cor: Color,
    onConcluir: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        BotaoFofo(onClick = { if (ativa) onConcluir(false) }, cor = cor, descricao = "$nome: continuar", habilitado = ativa) {
            Text("$nome ▶", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.padding(24.dp))
        }
    }
}
