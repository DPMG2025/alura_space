package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.casinhadosaber.dominio.EstourarBaloes
import br.com.casinhadosaber.dominio.Trecho

/**
 * Balões (regras na data class [EstourarBaloes] e no catálogo de docs/plano-1000-fases.md).
 * Ainda vazia: o gerador não sorteia este tipo até esta tela ficar pronta.
 * Contrato igual ao das outras atividades de mexer: [onErro] a cada movimento errado,
 * [onConcluir] uma vez só no fim (true se fez tudo sem errar); só aceita toques com [ativa];
 * com [ajudaInicial] (exemplo da rodada facilitada) a mãozinha e o brilho aparecem desde o começo.
 */
@Composable
internal fun AtividadeBaloes(
    estourarBaloes: EstourarBaloes,
    fala: List<Trecho>,
    ativa: Boolean,
    ajudaInicial: Boolean,
    cor: Color,
    onErro: () -> Unit,
    onConcluir: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AtividadeProvisoria("Balões", ativa, cor, onConcluir, modifier)
}
