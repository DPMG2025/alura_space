package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.casinhadosaber.dominio.QuebraCabeca
import br.com.casinhadosaber.dominio.Trecho

/**
 * Quebra cabeça (regras na data class [QuebraCabeca] e no catálogo de docs/plano-1000-fases.md).
 * Ainda vazia: o gerador não sorteia este tipo até esta tela ficar pronta.
 * Contrato igual ao das outras atividades de mexer: [onErro] a cada movimento errado,
 * [onConcluir] uma vez só no fim (true se fez tudo sem errar); só aceita toques com [ativa];
 * com [ajudaInicial] (exemplo da rodada facilitada) a mãozinha e o brilho aparecem desde o começo.
 */
@Composable
internal fun AtividadeQuebraCabeca(
    quebraCabeca: QuebraCabeca,
    fala: List<Trecho>,
    ativa: Boolean,
    ajudaInicial: Boolean,
    cor: Color,
    onErro: () -> Unit,
    onConcluir: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AtividadeProvisoria("Quebra cabeça", ativa, cor, onConcluir, modifier)
}
