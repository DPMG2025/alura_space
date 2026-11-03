package br.com.casinhadosaber.ui.telas.atividades.novas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import br.com.casinhadosaber.dominio.Trem
import br.com.casinhadosaber.dominio.Trecho

/**
 * Trem (regras na data class [Trem] e no catálogo de docs/plano-1000-fases.md).
 * Ainda vazia: o gerador não sorteia este tipo até esta tela ficar pronta.
 * Contrato igual ao das outras atividades de mexer: [onErro] a cada movimento errado,
 * [onConcluir] uma vez só no fim (true se fez tudo sem errar); só aceita toques com [ativa];
 * com [ajudaInicial] (exemplo da rodada facilitada) a mãozinha e o brilho aparecem desde o começo.
 */
@Composable
internal fun AtividadeTrem(
    trem: Trem,
    fala: List<Trecho>,
    ativa: Boolean,
    ajudaInicial: Boolean,
    cor: Color,
    onErro: () -> Unit,
    onConcluir: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AtividadeProvisoria("Trem", ativa, cor, onConcluir, modifier)
}
