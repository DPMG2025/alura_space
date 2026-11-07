package br.com.casinhadosaber.ui.componentes

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import br.com.casinhadosaber.dominio.Trilha
import br.com.casinhadosaber.ui.tema.Cores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Acabamento (Fase 9): botões desligados legíveis (WCAG 3:1 para o texto branco grande, até no alto da face, a
 * parte mais clara do degradê) e sem cara de ativos; estrelas vazias tingidas na cor do cartão. JUnit puro.
 */
class ContrasteTest {

    private val coloridos = listOf(
        Cores.Roxo, Cores.Certo, Color(0xFF34C46A), Color(0xFFFFA020), Color(0xFFFFB020), Color(0xFFFF5A4E),
        Color(0xFF3E8EF7), Color(0xFFFFD040), Color(0xFFFF7EB6),
    ) + Trilha.entries.map { Color(it.corArgb) }

    @Test fun contrasteDaWcag() {
        assertEquals(21f, contraste(Color.Black, Color.White), 0.01f)
        assertEquals(1f, contraste(Cores.Roxo, Cores.Roxo), 0.001f)
    }

    @Test fun botaoColoridoDesligadoLeBem() {
        for (cor in coloridos) {
            if (botaoClaro(cor)) continue
            val face = faceDesligada(cor)
            val alto = lerp(face, Color.White, 0.22f)
            assertTrue("desligado $cor: contraste ${contraste(alto, Color.White)}", contraste(alto, Color.White) >= 3f)
            // Não parece ativo: a face desligada é diferente da ligada e menos saturada.
            assertTrue("desligado $cor igual ao ligado", face != cor)
            assertTrue("desligado $cor mais saturado que o ligado", saturacao(face) < saturacao(cor) + 0.001f)
            // O lábio fica mais escuro que a face (o bloco de brinquedo continua com o pé).
            assertTrue(contraste(baseDesligada(cor, cor), Color.White) > contraste(face, Color.White))
        }
    }

    @Test fun botaoClaroDesligadoSoClareia() {
        assertTrue(botaoClaro(Color.White))
        assertEquals(clarear(Color.White, 0.5f), faceDesligada(Color.White))
        val creme = Color(0xFFFFF3C4)
        assertTrue(botaoClaro(creme))
        assertEquals(clarear(creme, 0.5f), faceDesligada(creme))
    }

    @Test fun estrelaVaziaNaCorDoCartao() {
        // O filtro sai para a cor de cada caminho (a cor em si aparece nas capturas v3d_03, v3d_04 e v3d_19).
        for (t in Trilha.entries) filtroDaEstrelaVazia(Color(t.corArgb))
    }

    private fun saturacao(c: Color): Float {
        val mx = maxOf(c.red, c.green, c.blue)
        val mn = minOf(c.red, c.green, c.blue)
        return if (mx <= 0f) 0f else (mx - mn) / mx
    }
}
