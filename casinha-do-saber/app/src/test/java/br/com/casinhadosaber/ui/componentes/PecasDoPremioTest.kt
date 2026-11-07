package br.com.casinhadosaber.ui.componentes

import br.com.casinhadosaber.dominio.EstadoJogo
import br.com.casinhadosaber.dominio.ItensDaCasa
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Prêmio e Quase em 3D (Fase 5): a arte que as telas pedem está no manifesto (com as âncoras usadas), as moedas
 * viram pilhas que se contam, a semente cresce com os acertos sem virar flor e o palco põe cada um no seu lugar.
 * JUnit puro (lê o manifesto versionado em `assets/arte/`).
 */
class PecasDoPremioTest {

    private val pasta: File = listOf("src/main/assets/arte", "app/src/main/assets/arte")
        .map(::File).firstOrNull { it.isDirectory }
        ?: error("pasta assets/arte não encontrada a partir de ${File(".").absolutePath}")

    private val manifesto by lazy { ManifestoDaArte.ler(File(pasta, "manifesto.json").readText()) }

    @Test fun aArteDoPremioEstaToda() {
        val faltam = PecasDoPremio.idsDaArte.filter { it !in manifesto.imagens }
        assertTrue("falta arte do prêmio no manifesto: $faltam", faltam.isEmpty())
        assertEquals(24, PecasDoPremio.idsDoConfete.size)
        // Toda peça da casa de fora (os 40 prêmios) tem a imagem 3D que sai da caixa.
        val semPremio = ItensDaCasa.ordem.map { PecasDoPremio.idDoPremio(it.id) }.filter { it !in manifesto.imagens }
        assertTrue("peças da casa sem item 3D: $semPremio", semPremio.isEmpty())
    }

    @Test fun ancorasUsadasExistem() {
        val podio = manifesto.imagens.getValue(PecasDoPremio.PODIO)
        for (a in listOf("centro", "esquerda", "direita")) assertTrue("pódio sem âncora $a", a in podio.ancoras)
        val (xe, ye) = podio.fracao(podio.ancoras.getValue("esquerda"))
        val (xd, yd) = podio.fracao(podio.ancoras.getValue("direita"))
        assertTrue("esquerda à esquerda do meio", xe < 0.4f && xd > 0.6f)
        assertEquals("os dois lados na mesma altura", ye, yd, 0.02f)
        // Os 5 quadros do presente: mesmo recorte e a boca da caixa (de onde o prêmio sai) em todos.
        val quadros = (0 until PecasDoPremio.QUADROS_DO_PRESENTE).map { manifesto.imagens.getValue(PecasDoPremio.quadroDoPresente(it)) }
        for (q in quadros) {
            assertTrue("${q.id} sem a âncora boca", "boca" in q.ancoras)
            assertEquals(quadros[0].largura, q.largura)
            assertEquals(quadros[0].altura, q.altura)
            val (bx, by) = q.fracao(q.ancoras.getValue("boca"))
            assertTrue("${q.id}: boca fora do lugar ($bx, $by)", bx in 0.25f..0.6f && by in 0.45f..0.75f)
        }
    }

    @Test fun numeroSoAoLadoDeMoedasQueDaoAConta() {
        assertEquals(null, PecasDoPremio.rotuloDasMoedas(0))
        for (n in (1..13) + 15) assertEquals("+$n", PecasDoPremio.rotuloDasMoedas(n))
        // 14 moedas em até 3 pilhas não dá a conta (5 + 5 + 3): nenhum número, para não mentir.
        assertEquals(null, PecasDoPremio.rotuloDasMoedas(14))
        assertEquals(null, PecasDoPremio.rotuloDasMoedas(30))
        // As moedas do prêmio, do brinde, da missão e do relembrar sempre têm o número certo ao lado.
        for (n in listOf(EstadoJogo.MOEDAS_DO_BRINDE, EstadoJogo.MOEDAS_DA_MISSAO, br.com.casinhadosaber.dominio.Relembrar.MOEDAS)) {
            assertEquals("+$n", PecasDoPremio.rotuloDasMoedas(n))
        }
    }

    @Test fun moedasEmPilhasQueSeContam() {
        assertEquals(emptyList<Int>(), PecasDoPremio.pilhasDeMoedas(0))
        assertEquals(listOf(1), PecasDoPremio.pilhasDeMoedas(1))
        assertEquals(listOf(3, 1), PecasDoPremio.pilhasDeMoedas(4))
        assertEquals(listOf(5), PecasDoPremio.pilhasDeMoedas(5))
        assertEquals(listOf(5, 5, 2), PecasDoPremio.pilhasDeMoedas(12))
        for (n in (1..13) + 15) {
            val p = PecasDoPremio.pilhasDeMoedas(n)
            assertEquals("pilhas de $n", n, p.sum())
            assertTrue(p.size <= 3 && p.all { it in PecasDoPremio.PILHAS })
            assertEquals("da maior para a menor", p.sortedDescending(), p)
        }
        assertEquals(listOf(5, 5, 5), PecasDoPremio.pilhasDeMoedas(40))
        assertTrue(PecasDoPremio.PILHAS.all { PecasDoPremio.idDasMoedas(it) in manifesto.imagens })
    }

    @Test fun sementeCresceSemVirarFlor() {
        val minimo = EstadoJogo.MINIMO_PARA_VENCER
        val estagios = (0 until minimo).map { PecasDoPremio.estagioDaSemente(it, minimo) }
        assertEquals(1, estagios.first())
        assertTrue("nunca a flor no Quase", estagios.all { it in 1 until PecasDoPremio.ESTAGIOS_DA_SEMENTE })
        assertEquals("mais acertos, planta maior (ou igual)", estagios.sorted(), estagios)
        assertEquals(3, PecasDoPremio.estagioDaSemente(minimo - 1, minimo))
        assertEquals("quase_semente_1", PecasDoPremio.idDaSemente(0))
        assertEquals("quase_semente_4", PecasDoPremio.idDaSemente(9))
    }

    @Test fun palcoPoeCadaUmNaAncora() {
        // Pódio 300 x 100; criança de 80 x 120 com os pés em 95% sobre (0,2; 0,3); presente flutuando 10 acima do meio.
        val palco = montarPalco(
            300f, 100f,
            listOf(
                PecaDoPalco(0.2f, 0.3f, 80f, 120f, apoioY = 0.95f),
                PecaDoPalco(0.5f, 0.3f, 100f, 110f, apoioX = 0.4f, apoioY = 0.85f, acima = 10f),
            ),
        )
        val (crianca, presente) = palco.caixas
        // O mais alto encosta no topo do palco; ninguém é cortado em cima.
        assertEquals(0f, minOf(crianca.y, presente.y), 0.01f)
        assertEquals(palco.topoDoPodio + 100f, palco.altura, 0.01f)
        // Os pés da criança caem na âncora.
        assertEquals(60f, crianca.x + crianca.largura / 2, 0.01f)
        assertEquals(palco.topoDoPodio + 30f, crianca.y + crianca.altura * 0.95f, 0.01f)
        // O apoio do presente fica 10 acima do meio do pódio.
        assertEquals(150f, presente.x + presente.largura * 0.4f, 0.01f)
        assertEquals(palco.topoDoPodio + 20f, presente.y + presente.altura * 0.85f, 0.01f)
        // Sem peças, só o pódio.
        assertEquals(Palco(100f, 0f, emptyList()), montarPalco(300f, 100f, emptyList()))
    }
}
