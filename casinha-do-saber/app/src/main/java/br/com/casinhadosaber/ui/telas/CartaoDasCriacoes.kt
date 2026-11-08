package br.com.casinhadosaber.ui.telas

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import br.com.casinhadosaber.dominio.ResumoDasCriacoes

/** Área dos pais: o que a criança pintou e montou no Livro de Colorir e nos quebra cabeças. */
@Composable
fun CartaoDasCriacoes(resumo: ResumoDasCriacoes, modifier: Modifier = Modifier) {
    CartaoDosPais(modifier.testTag("cartao_das_criacoes")) {
        Text("🎨 Criações da criança", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        if (!resumo.comecou) {
            Text(
                "Ainda não começou. O Livro de colorir e os quebra cabeças ficam no cantinho criativo da casa.",
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            Text(
                "Livro de colorir: ${resumo.desenhosTerminados} de ${resumo.desenhosNoLivro} desenhos terminados, " +
                    "${resumo.estrelasDeColorir} estrelas.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Quebra cabeças: ${resumo.quebraCabecasVencidos} vencidos, ${resumo.pecasDouradas} peças douradas.",
                style = MaterialTheme.typography.bodyLarge,
            )
            if (resumo.selosTotal > 0) {
                Spacer(Modifier.height(4.dp))
                Text("Selos: ${resumo.selos} de ${resumo.selosTotal}.", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
