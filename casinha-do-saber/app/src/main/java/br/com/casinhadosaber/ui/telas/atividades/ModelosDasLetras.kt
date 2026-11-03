package br.com.casinhadosaber.ui.telas.atividades

import androidx.compose.ui.geometry.Offset
import br.com.casinhadosaber.dominio.LeitorDeLetras
import br.com.casinhadosaber.dominio.Ponto

/**
 * Os modelos das letras maiúsculas para o leitor de letras escritas com o dedo ([LeitorDeLetras]):
 * os mesmos traços de [TracosDasLetras] (o app ensina a escrever assim), na proporção em que aparecem
 * na tela (a letra é [TracosDasLetras.PROPORCAO] vezes mais alta que larga), e alguns jeitos comuns a
 * mais de escrever a mesma letra (o I de um risco só, o J com chapéu).
 */
internal object ModelosDasLetras {

    private fun Offset.ponto() = Ponto(x, y * TracosDasLetras.PROPORCAO)

    /** Os traços da letra [c] em [Ponto], na proporção da tela. */
    fun tracos(c: Char): List<List<Ponto>> = TracosDasLetras.tracosDe(c.toString()).map { t -> t.map { it.ponto() } }

    private fun reta(vararg xy: Float): List<Ponto> = xy.toList().chunked(2).map { (x, y) -> Ponto(x, y * TracosDasLetras.PROPORCAO) }

    /** Outros jeitos de escrever (as crianças também escrevem assim e está certo). */
    private val outrosJeitos: Map<Char, List<List<List<Ponto>>>> = mapOf(
        'I' to listOf(listOf(reta(0.5f, 0.15f, 0.5f, 0.85f))),
        'J' to listOf(tracos('J') + listOf(reta(0.4f, 0.15f, 0.84f, 0.15f))),
    )

    val modelos: Map<Char, List<List<List<Ponto>>>> by lazy {
        ('A'..'Z').associateWith { c -> listOf(tracos(c)) + outrosJeitos[c].orEmpty() }
    }

    val leitor: LeitorDeLetras by lazy { LeitorDeLetras(modelos) }
}
